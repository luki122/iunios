/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.bluetooth;

import aurora.widget.AuroraActionBar;
import android.app.ActionBar;
import aurora.app.AuroraActivity;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraPreferenceActivity;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceGroup;
import aurora.preference.AuroraPreferenceScreen;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import aurora.widget.AuroraSwitch;
import android.widget.TextView;
import android.preference.PreferenceManager;
import com.android.settings.ProgressCategory;
import com.android.settings.R;
import com.aurora.featureoption.FeatureOption;
//Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
import com.android.settings.Settings;
//Gionee <wangguojing> <2013-07-25> add for CR00837650 end




import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import aurora.widget.CustomAuroraActionBarItem;
import aurora.app.AuroraActivity;
import android.os.UserHandle;
import android.widget.Toast;
import android.text.TextUtils;

/**
 * BluetoothSettings is the Settings screen for Bluetooth configuration and
 * connection management.
 */
// Aurora liugj 2013-10-22 modified for aurora's new feature
public final class BluetoothSettings extends DeviceListPreferenceFragment implements AuroraPreference.OnPreferenceClickListener{
    private static final String TAG = "BluetoothSettings";
    
    private static final String BLUETOOTH_SWITCH_PREFERENCE_KEY = "bluetooth_switch";
    private static final String BLUETOOTH_SWITCH_SCAN_KEY = "bluetooth_scan";
    private static final String BLUETOOTH_DEVICE_NAME_KEY = "bt_device_name";
    private static final String KEY_BT_ADDRESS = "bt_address";
    
    private static final int MENU_ID_SCAN = Menu.FIRST;
    private static final int MENU_ID_RENAME_DEVICE = Menu.FIRST + 1;
    private static final int MENU_ID_VISIBILITY_TIMEOUT = Menu.FIRST + 2;
    private static final int MENU_ID_SHOW_RECEIVED = Menu.FIRST + 3;
    /// M: The id for option menu "Advanced Settings"
    private static final int MENU_ID_ADVANCED_SETTING = Menu.FIRST + 4;
    //gionee wangyy 20120626 modify for CR00627723 begin
    private static final int MENU_ID_STOP_SCAN = Menu.FIRST + 5;
    //gionee wangyy 20120626 modify for CR00627723 end

    /* Private intent to show the list of received files */
    private static final String BTOPP_ACTION_OPEN_RECEIVED_FILES =
            "android.btopp.intent.action.OPEN_RECEIVED_FILES";

    private BluetoothEnabler mBluetoothEnabler;

    private BluetoothDiscoverableEnabler mDiscoverableEnabler;

    private AuroraPreferenceGroup mPairedDevicesCategory;

    private AuroraPreferenceGroup mAvailableDevicesCategory;
    private boolean mAvailableDevicesCategoryIsPresent;
    private boolean mActivityStarted;

    private TextView mEmptyView;

    private final IntentFilter mIntentFilter;
    //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
    private  static BluetoothSettings mBluetoothSettings ;
    //Gionee <wangguojing> <2013-07-25> add for CR00837650 end

    // accessed from inner class (not private to avoid thunks)
    AuroraPreference mMyDevicePreference;
    AuroraPreference btAddressPref;
    private AuroraSwitchPreference mBluetoothSwitchPreference;
    private AuroraSwitchPreference mBluetoothScanPreference;
    private AuroraPreferenceScreen mMyDeviceName;
    
    Button scanButton;
    private ProgressBar scanBar;
    // qy
    private boolean isCreate =false;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals("android.intent.action.SERVICE_STATE")) {
            updateContent(mLocalAdapter.getBluetoothState(), mActivityStarted);
            }

            if (action.equals(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED)) {
                updateDeviceName();
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                updateProgressUi(false);
                scanButton.setEnabled(true);
            }

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                RefreshDeviceInfo();
            }
        }

        private void updateDeviceName() {
            if (mLocalAdapter.isEnabled() && mMyDevicePreference != null) {
                mMyDevicePreference.setTitle(mLocalAdapter.getName());
            }
        }
    };
	
    //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
    public static BluetoothSettings getBluetoothSettingsInstance() {
        return mBluetoothSettings ;
		
    }
    //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin

    public BluetoothSettings() {
        //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
        mBluetoothSettings = this;
        Settings.mKey = TAG ;
        //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
        mIntentFilter = new IntentFilter();
        
        //qy add
        mIntentFilter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
        mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mIntentFilter.addAction("android.intent.action.SERVICE_STATE");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivityStarted = (savedInstanceState == null);    // don't auto start scan after rotation
        isCreate = true;
        /*mEmptyView = (TextView) getView().findViewById(android.R.id.empty);
        getListView().setEmptyView(mEmptyView);*/
    }

    @Override
    void addPreferencesForActivity() {
        addPreferencesFromResource(R.xml.aurora_bluetooth_settings);
        
        mBluetoothSwitchPreference = (AuroraSwitchPreference) findPreference(BLUETOOTH_SWITCH_PREFERENCE_KEY);
        mBluetoothScanPreference = (AuroraSwitchPreference) findPreference(BLUETOOTH_SWITCH_SCAN_KEY);
        mMyDeviceName = (AuroraPreferenceScreen) findPreference(BLUETOOTH_DEVICE_NAME_KEY);
        btAddressPref = findPreference(KEY_BT_ADDRESS);
        
        mMyDeviceName.setOnPreferenceClickListener(this);

        if (mBluetoothSwitchPreference != null && mBluetoothScanPreference != null) {
            if (!mBluetoothSwitchPreference.isChecked()) {
                mBluetoothScanPreference.setChecked(false);
            }
            
        }
        /*AuroraActivity activity = (AuroraActivity)getActivity();

        AuroraSwitch actionBarSwitch = new AuroraSwitch(activity);

        if (activity instanceof AuroraPreferenceActivity) {
            AuroraPreferenceActivity preferenceActivity = (AuroraPreferenceActivity) activity;
            if (preferenceActivity.onIsHidingHeaders() || !preferenceActivity.onIsMultiPane()) {
                final int padding = activity.getResources().getDimensionPixelSize(
                        R.dimen.action_bar_switch_padding);
                actionBarSwitch.setPadding(0, 0, padding, 0);
		//AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
      	 	 /*
                activity.getAuroraActionBar().setDisplayOptions(AuroraActionBar.DISPLAY_SHOW_CUSTOM,
                        AuroraActionBar.DISPLAY_SHOW_CUSTOM);
                activity.getAuroraActionBar().setCustomView(actionBarSwitch, new AuroraActionBar.LayoutParams(
                        AuroraActionBar.LayoutParams.WRAP_CONTENT,
                        AuroraActionBar.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_VERTICAL | Gravity.END));
               
               //AURORA-END::delete temporarily for compile::waynelin::2013-9-14  
            }
        }

        mBluetoothEnabler = new BluetoothEnabler(activity, actionBarSwitch);*/

        mBluetoothEnabler = new BluetoothEnabler(getActivity(), mBluetoothSwitchPreference);

        ((AuroraActivity)getActivity()).getAuroraActionBar().addItem(R.layout.aurora_actionbar_scan, 0);
        CustomAuroraActionBarItem item = (CustomAuroraActionBarItem) ((AuroraActivity) getActivity()).getAuroraActionBar().getItem(0);
        View view = item.getItemView();
        scanButton = (Button) view.findViewById(R.id.scan_btn);
        scanBar = (ProgressBar) view.findViewById(R.id.scan_progressbar);

		 // Aurora liugj 2013-10-24 added for aurora's new feature start
        scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON) {
                    startScanning();                    
                    updateProgressUi(true);
                   
                }
			}
		 });
		 // Aurora liugj 2013-10-24 added for aurora's new feature end
        setHasOptionsMenu(true);
        
        RefreshDeviceInfo();
        
    }

    @Override
    public void onResume() {
        // resume BluetoothEnabler before calling super.onResume() so we don't get
        // any onDeviceAdded() callbacks before setting up view in updateContent()
        if (mBluetoothEnabler != null) {
            mBluetoothEnabler.resume();
        }
        super.onResume();

        if (mDiscoverableEnabler != null) {
            mDiscoverableEnabler.resume();
        }
        getActivity().registerReceiver(mReceiver, mIntentFilter);
        
        initDeviceName();
 
            updateContent(mLocalAdapter.getBluetoothState(), mActivityStarted);
//            updateProgressUi(mLocalAdapter.isDiscovering());

            RefreshDeviceInfo();
            
        /*
            // qy add adapt to s4
            if(mBluetoothSwitchPreference.isChecked() && isCreate){
            	startScanning();
            	
        		updateProgressUi(true);         	
        		isCreate =false ; //qy    end          	
            }
       */ 
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBluetoothEnabler != null) {
            mBluetoothEnabler.pause();
        }
        getActivity().unregisterReceiver(mReceiver);
        if (mDiscoverableEnabler != null) {
            mDiscoverableEnabler.pause();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if(mPairedDevicesCategory != null) {
            mPairedDevicesCategory.removeAll();
        }
        if(mAvailableDevicesCategory != null) {
            mAvailableDevicesCategory.removeAll();
        }
        //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin        
        Settings.mKey = null ;        
        //Gionee <wangguojing> <2013-07-25> add for CR00837650 end
        
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu");
        if (mLocalAdapter == null) return;
        boolean bluetoothIsEnabled = mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON;
        boolean isDiscovering = mLocalAdapter.isDiscovering();
        Log.d(TAG, "isDiscovering " + isDiscovering);
        int textId = isDiscovering ? R.string.bluetooth_searching_for_devices :
            R.string.bluetooth_search_for_devices;
        menu.add(Menu.NONE, MENU_ID_SCAN, 0, textId)
                .setIcon(R.drawable.ic_search)
                .setEnabled(bluetoothIsEnabled && !isDiscovering)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(Menu.NONE, MENU_ID_RENAME_DEVICE, 0, R.string.bluetooth_rename_device)
                .setEnabled(bluetoothIsEnabled)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(Menu.NONE, MENU_ID_VISIBILITY_TIMEOUT, 0, R.string.bluetooth_visibility_timeout)
                .setEnabled(bluetoothIsEnabled)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(Menu.NONE, MENU_ID_SHOW_RECEIVED, 0, R.string.bluetooth_show_received_files)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        /// M: Add for Advance Settings
        if (doesAdvancedFeatureExist()) {
        	menu.add(Menu.NONE, MENU_ID_ADVANCED_SETTING, 0, R.string.bluetooth_advanced_settings)
                .setEnabled(bluetoothIsEnabled)    
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        //gionee wangyy 20120626 modify for CR00627723 begin
        menu.add(Menu.NONE, MENU_ID_STOP_SCAN, 0, R.string.gn_bluetooth_stop_scan)
                .setVisible(bluetoothIsEnabled && isDiscovering)	
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        //gionee wangyy 20120626 modify for CR00627723 end
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_SCAN:
                if (mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON) {
                    startScanning();
                }
                return true;

            case MENU_ID_RENAME_DEVICE:
                new BluetoothNameDialogFragment().show(
                        getFragmentManager(), "rename device");
                return true;

            case MENU_ID_VISIBILITY_TIMEOUT:
                new BluetoothVisibilityTimeoutFragment().show(
                        getFragmentManager(), "visibility timeout");
                return true;

            case MENU_ID_SHOW_RECEIVED:
                Intent intent = new Intent(BTOPP_ACTION_OPEN_RECEIVED_FILES);
                getActivity().sendBroadcastAsUser(intent, UserHandle.CURRENT);
//                getActivity().sendBroadcast(intent);
                return true;
            
            /// M: when click menu "Advanced settings", start corresponding activity @{
            case MENU_ID_ADVANCED_SETTING:
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setClassName("com.mediatek.bluetooth",
                    "com.mediatek.bluetooth.settings.BluetoothAdvancedSettingsActivity");
                try {
                    startActivity(i);
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "Unable to start activity " + i.toString());
                    return false;
                }
                return true;
            /// @}
            //gionee wangyy 20120626 modify for CR00627723 begin
            case MENU_ID_STOP_SCAN:
                if (mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON && mLocalAdapter.isDiscovering()) {
                    mLocalAdapter.stopScanning();
                }
             return true;
            //gionee wangyy 20120626 modify for CR00627723 end
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onPreferenceClick(AuroraPreference preference) {
		if (preference.getKey().equals(BLUETOOTH_DEVICE_NAME_KEY)) {
			((AuroraPreferenceActivity) getActivity()).startPreferencePanel(
					BluetoothDeviceNameFragment.class.getCanonicalName(), null,
					R.string.bluetooth_title_device_name, null, null, 0);
		}
    	
    	return true;
    }
    
    private void startScanning() {
        if (!mAvailableDevicesCategoryIsPresent && mAvailableDevicesCategory != null) {
            getPreferenceScreen().addPreference(mAvailableDevicesCategory);
        }
        mLocalAdapter.startScanning(true);
    }

	// Aurora liugj 2013-10-24 added for aurora's new feature start    
    private void refreshScanBar() {
    	if (mLocalAdapter == null) return;
        boolean bluetoothIsEnabled = mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON;
        boolean isDiscovering = mLocalAdapter.isDiscovering();
//       scanButton.setEnabled(bluetoothIsEnabled && !isDiscovering);
       
       // qy  add
       if(bluetoothIsEnabled && isDiscovering){       	
       	updateProgressUi(true);       	
       }else{
       	updateProgressUi(false);
       }
       scanButton.setEnabled(bluetoothIsEnabled && !isDiscovering);
    }
	// Aurora liugj 2013-10-24 added for aurora's new feature end
    
    @Override
    void onDevicePreferenceClick(BluetoothDevicePreference btPreference) {
        mLocalAdapter.stopScanning();
        super.onDevicePreferenceClick(btPreference);
    }

    private void addDeviceCategory(AuroraPreferenceGroup preferenceGroup, int titleId,
            BluetoothDeviceFilter.Filter filter) {
        preferenceGroup.setTitle(titleId);
        getPreferenceScreen().addPreference(preferenceGroup);
        setFilter(filter);
        setDeviceListGroup(preferenceGroup);
        addCachedDevices();
        preferenceGroup.setEnabled(true);
    }
    
    private void initDeviceName(){
    	// note 3 qy add 2014 04 03
        SharedPreferences preferences = getActivity().getSharedPreferences("iuni", Context.MODE_PRIVATE);
        
        String dn = preferences.getString("DEVICE_NAME", "");
        if(dn.trim().equals("")){
        	mMyDeviceName.auroraSetArrowText(mLocalAdapter.getName());
        }else{
        	mLocalAdapter.setName(dn);
        	mMyDeviceName.auroraSetArrowText(dn);
        	
        } // note3 end 2014 04 03
    }

    private void RefreshDeviceInfo()
    {
         if(mMyDeviceName != null){
            /*
               if (mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON) {
                 mMyDeviceName.setEnabled(true);
                 mMyDeviceName.setSelectable(true);
            } else {
                mMyDeviceName.setEnabled(false);
                mMyDeviceName.setSelectable(false);
            }*/
                 mMyDeviceName.setEnabled(true);
                 mMyDeviceName.setSelectable(true);
        }
    }

    private void updateContent(int bluetoothState, boolean scanState) {
    	final AuroraPreferenceScreen preferenceScreen = getPreferenceScreen();
        int messageId = 0;
        switch (bluetoothState) {
        case BluetoothAdapter.STATE_ON:
        	initDeviceName();
            if(btAddressPref == null)	
                btAddressPref = new AuroraPreference(getActivity());
            String address = mLocalAdapter.isEnabled() ? mLocalAdapter.getAddress() : null;
            btAddressPref.setSummary(!TextUtils.isEmpty(address) ? address
                    : getString(R.string.status_unavailable));
            preferenceScreen.addPreference(btAddressPref);
        	// This device
            /*if (mMyDevicePreference == null) {
                mMyDevicePreference = new AuroraPreference(getActivity());
            }
            mMyDevicePreference.setTitle(mLocalAdapter.getName());
            if (getResources().getBoolean(R.bool.config_voice_capable)) {
                mMyDevicePreference.setIcon(R.drawable.ic_bt_cellphone);    // for phones
            } else {
                mMyDevicePreference.setIcon(R.drawable.ic_bt_laptop);   // for tablets, etc.
            }
            mMyDevicePreference.setPersistent(false);
            mMyDevicePreference.setEnabled(true);
            preferenceScreen.addPreference(mMyDevicePreference);*/
        	if (mPairedDevicesCategory != null) {
            	preferenceScreen.removePreference(mPairedDevicesCategory);
    		}
            if (mAvailableDevicesCategory != null) {
            	preferenceScreen.removePreference(mAvailableDevicesCategory);
    		}
        	mDevicePreferenceMap.clear();
        	mBluetoothScanPreference.setPersistent(false);
        	mBluetoothScanPreference.setEnabled(true);
            
        	if (mDiscoverableEnabler == null) {
                mDiscoverableEnabler = new BluetoothDiscoverableEnabler(getActivity(),
                        mLocalAdapter, mBluetoothScanPreference);
                mDiscoverableEnabler.resume();
                LocalBluetoothManager.getInstance(getActivity()).setDiscoverableEnabler(
                        mDiscoverableEnabler);
            }
        	// Paired devices category
            if (mPairedDevicesCategory == null) {
                mPairedDevicesCategory = new AuroraPreferenceCategory(getActivity());
            } else {
                mPairedDevicesCategory.removeAll();
            }
            addDeviceCategory(mPairedDevicesCategory,
                    R.string.bluetooth_preference_paired_devices,
                    BluetoothDeviceFilter.BONDED_DEVICE_FILTER);
            int numberOfPairedDevices = mPairedDevicesCategory.getPreferenceCount();
            if (numberOfPairedDevices == 0) {
                preferenceScreen.removePreference(mPairedDevicesCategory);
            }
            
            mDiscoverableEnabler.setNumberOfPairedDevices(numberOfPairedDevices);

            // Available devices category
            if (mAvailableDevicesCategory == null) {
                mAvailableDevicesCategory = new AuroraPreferenceCategory(getActivity());
            } else {
                mAvailableDevicesCategory.removeAll();
            }
            addDeviceCategory(mAvailableDevicesCategory,
                    R.string.bluetooth_preference_found_devices,
                    BluetoothDeviceFilter.UNBONDED_DEVICE_FILTER);
            int numberOfAvailableDevices = mAvailableDevicesCategory.getPreferenceCount();
            mAvailableDevicesCategoryIsPresent = true;
            if (numberOfAvailableDevices == 0) {
                preferenceScreen.removePreference(mAvailableDevicesCategory);
                mAvailableDevicesCategoryIsPresent = false;
            }

         /*   if (scanState == true) {
                mActivityStarted = false;
                startScanning();
            } else */
            {
                if (!mAvailableDevicesCategoryIsPresent) {
                    getPreferenceScreen().addPreference(mAvailableDevicesCategory);
                }
            }
            messageId = R.string.bluetooth_turned_on;
//        	mBluetoothSwitchPreference.setSummary(messageId);
            getActivity().invalidateOptionsMenu();
            
            if(mLocalAdapter.isDiscovering()){
            	
            	updateProgressUi(true);
            	
            }else{
            	updateProgressUi(false);
            	scanButton.setEnabled(true);
            }
            
			  // Aurora liugj 2013-10-24 added for aurora's new feature start
//            refreshScanBar();
			  // Aurora liugj 2013-10-24 added for aurora's new feature end
            return; // not break
        	
        case BluetoothAdapter.STATE_TURNING_OFF:
            messageId = R.string.bluetooth_turning_off;
            updateProgressUi(false);
            break;

            case BluetoothAdapter.STATE_OFF:
                messageId = R.string.bluetooth_empty_list_bluetooth_off;
                
                break;

            case BluetoothAdapter.STATE_TURNING_ON:
                messageId = R.string.bluetooth_turning_on;
                
                break;
        }
//        mBluetoothSwitchPreference.setSummary(messageId);      
        if (mPairedDevicesCategory != null) {
        	preferenceScreen.removePreference(mPairedDevicesCategory);
		}
        if (mAvailableDevicesCategory != null) {
        	preferenceScreen.removePreference(mAvailableDevicesCategory);
		}
        removeAllDevices();
		  // Aurora liugj 2013-10-24 added for aurora's new feature start
        refreshScanBar();
		  // Aurora liugj 2013-10-24 added for aurora's new feature end
        if(btAddressPref != null) 
            preferenceScreen.removePreference(btAddressPref);
	}
       

    @Override
    public void onBluetoothStateChanged(int bluetoothState) {
        super.onBluetoothStateChanged(bluetoothState);
        updateContent(bluetoothState, true);
    }

    @Override
    public void onScanningStateChanged(boolean started) {
        Log.d(TAG, "started" + started);
        super.onScanningStateChanged(started);
        // Update options' enabled state
        getActivity().invalidateOptionsMenu();
	     // Aurora liugj 2013-10-24 added for aurora's new feature start
        refreshScanBar();
		  // Aurora liugj 2013-10-24 added for aurora's new feature end
        updateContent(mLocalAdapter.getBluetoothState(), false); 
                
    }

    public void onDeviceAttributesChanged() {

        Log.d(TAG, "onDeviceAttributesChanged" );
        updateContent(mLocalAdapter.getBluetoothState(), false); 
    }

    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        setDeviceListGroup(getPreferenceScreen());
        removeAllDevices();
        updateContent(mLocalAdapter.getBluetoothState(), true); 
    }

    private final View.OnClickListener mDeviceProfilesListener = new View.OnClickListener() {
        public void onClick(View v) {
            // User clicked on advanced options icon for a device in the list
            if (v.getTag() instanceof CachedBluetoothDevice) {
                CachedBluetoothDevice device = (CachedBluetoothDevice) v.getTag();

                Bundle args = new Bundle(1);
                args.putParcelable(DeviceProfilesSettings.EXTRA_DEVICE, device.getDevice());

                ((AuroraPreferenceActivity) getActivity()).startPreferencePanel(
                        DeviceProfilesSettings.class.getName(), args,
                        R.string.bluetooth_device_advanced_title, null, null, 0);
            } else {
                Log.w(TAG, "onClick() called for other View: " + v); // TODO remove
            }
        }
    };

    /**
     * Add a listener, which enables the advanced settings icon.
     * @param preference the newly added preference
     */
    @Override
    void initDevicePreference(BluetoothDevicePreference preference) {
        CachedBluetoothDevice cachedDevice = preference.getCachedDevice();
        if (cachedDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            // Only paired device have an associated advanced settings screen
            preference.setOnSettingsClickListener(mDeviceProfilesListener);
        }
         cachedDevice.refresh();
    }
    
    /// M: remove help menu because we can not provide such help web @{
    /*@Override
    protected int getHelpResource() {
        return R.string.help_url_bluetooth;
    }*/
    /// @}

	private boolean doesAdvancedFeatureExist() {
		return FeatureOption.MTK_BT_PROFILE_FTP ||
				FeatureOption.MTK_BT_PROFILE_MAPS ||
				FeatureOption.MTK_BT_PROFILE_PRXR ||
				FeatureOption.MTK_BT_PROFILE_SIMAP;
    }
	
    private void updateProgressUi(boolean start) {
        Log.d(TAG, "updateProgressUi " + start);
        // Aurora liugj 2013-10-24 modified for aurora's new feature start
        if (mDeviceListGroup instanceof AuroraPreferenceCategory) {
            //((AuroraPreferenceCategory) mDeviceListGroup).setProgress(start);
          //scanBar.setProgress(start);
          scanBar.setVisibility(start ? View.VISIBLE : View.GONE);
          scanButton.setVisibility(start ? View.GONE : View.VISIBLE);
          Log.d(TAG, "setProgress " + start);
        }
        // Aurora liugj 2013-10-24 modified for aurora's new feature end
    }

}
