/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.bluetooth.LocalBluetoothManager;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.mediatek.bluetooth.ConfigHelper;

import aurora.app.AuroraActivity;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceGroup;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenuBase;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraSystemMenu;
import static android.os.UserManager.DISALLOW_CONFIG_BLUETOOTH;


/**
 * BluetoothSettings is the Settings screen for Bluetooth configuration and
 * connection management.
 */
public final class AuroraBluetoothSettings extends AuroraDeviceListPreferenceFragment implements Indexable{
    private static final String TAG = "AuroraBluetoothSettings";
    
    private static final String BLUETOOTH_SWITCH_PREFERENCE_KEY = "bluetooth_switch";
    private static final String BLUETOOTH_DEVICE_NAME_KEY = "bt_device_name";
    private static final String KEY_BT_ADDRESS = "bt_address";
    private static final int MENU_ID_SCAN = Menu.FIRST;
    private static final int MENU_ID_RENAME_DEVICE = Menu.FIRST + 1;
    private static final int MENU_ID_SHOW_RECEIVED = Menu.FIRST + 2;

    /// M: The id for option menu "Advanced Settings"
    private static final int MENU_ID_ADVANCED_SETTING = Menu.FIRST + 4;

    private static final int MENU_ID_MORE = Menu.FIRST + 5;
    /// M: The action of launch BluetoothAdvancedSettings in com.mediatek.bluetooth
    private static final String LAUNCH_BT_ADVANCED_SETTINGS_ACTION =
        "com.mediatek.bluetooth.settings.action.START_BT_ADV_SETTING";

    /* Private intent to show the list of received files */
    private static final String BTOPP_ACTION_OPEN_RECEIVED_FILES =
            "android.btopp.intent.action.OPEN_RECEIVED_FILES";


    private AuroraBluetoothEnabler mBluetoothEnabler;

    private AuroraPreferenceGroup mPairedDevicesCategory;
    private AuroraPreferenceGroup mAvailableDevicesCategory;
    private boolean mAvailableDevicesCategoryIsPresent;

    private boolean mInitialScanStarted;
    private boolean mInitiateDiscoverable;


    private TextView mEmptyView;

    private final IntentFilter mIntentFilter;

    ///M: ALPS01849536
    private AlertDialog mProfileDialog;

    // accessed from inner class (not private to avoid thunks)
    AuroraPreference mMyDevicePreference;
    private AuroraSwitchPreference mBluetoothSwitchPreference;
    //private AuroraPreference mDeviceNamePref;
    //private AuroraPreference mBtAddress;
    
    //private Button scanButton;
    //private ProgressBar scanBar;

    private AuroraActionBar mActionBar;
    private AuroraSystemMenu mAuroraMenu;
    private AuroraActivity mActivity;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final int state =
                intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            if (action.equals(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED)) {
                updateDeviceName(context);
            }

            if (state == BluetoothAdapter.STATE_ON) {
                mInitiateDiscoverable = true;
            }
        }

        private void updateDeviceName(Context context) {
            if (mLocalAdapter.isEnabled() && mMyDevicePreference != null) {
                mMyDevicePreference.setSummary(context.getResources().getString(
                            R.string.bluetooth_is_visible_message, mLocalAdapter.getName()));
            }
        }
    };

    public AuroraBluetoothSettings() {
        super(DISALLOW_CONFIG_BLUETOOTH);
        mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mInitialScanStarted = (savedInstanceState != null);    // don't auto start scan after rotation
        mInitiateDiscoverable = true;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    void addPreferencesForActivity() {
        Log.d(TAG, "addPreferencesForActivity : ");
//        addPreferencesFromResource(R.xml.bluetooth_settings);
//        addPreferencesFromResource(R.xml.gn_bluetooth_settings);
        addPreferencesFromResource(R.xml.aurora_bluetooth_settings);

        mBluetoothSwitchPreference = (AuroraSwitchPreference) findPreference(BLUETOOTH_SWITCH_PREFERENCE_KEY);
/*
        mDeviceNamePref = (AuroraPreference) findPreference(BLUETOOTH_DEVICE_NAME_KEY);
        mBtAddress = (AuroraPreference) findPreference(KEY_BT_ADDRESS);

        //mDeviceNamePref.auroraSetArrowText(mLocalAdapter.getName(),true);
        mDeviceNamePref.setOnPreferenceClickListener(new AuroraPreference.OnPreferenceClickListener(){
        @Override
        public boolean onPreferenceClick(AuroraPreference preference) {
                ((SettingsActivity) getActivity()).startPreferencePanel(
                        AuroraBluetoothDeviceNameFragment.class.getCanonicalName(), null,
                        R.string.bluetooth_title_device_name, null, null, 0);
            return true;
        }
    }
        );
*/
        mActivity = (AuroraActivity) getActivity();
        mBluetoothEnabler = new AuroraBluetoothEnabler(mActivity, mBluetoothSwitchPreference);
        mActionBar = mActivity.getAuroraActionBar();
        if(mActionBar != null) {
    	    mActionBar.addItem(AuroraActionBarItem.Type.More, MENU_ID_MORE);
    	    mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
        }

        mActivity.setAuroraMenuItems(R.menu.aurora_bt_settings_menu);
        mAuroraMenu = mActivity.getAuroraMenu();
        if (mAuroraMenu != null) {
               mAuroraMenu.setAnimationStyle(com.aurora.R.style.AuroraMenuRightTopAnimation);
               mActivity.setAuroraSystemMenuCallBack(auroraSystemMenuCallBack);
        }
        
        setHasOptionsMenu(true);
    }

    /*
     * 点击 aurora action bar 弹出菜单
     */
    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int arg0) {
			switch (arg0) {
			case MENU_ID_MORE:
                                if (mLocalAdapter == null) return;
                                //If the user is not allowed to configure bluetooth, do not show the menu.
                                if (isUiRestricted()) return;

                                /* 根据手机的分辨率从 dp 的单位 转成为 px(像素)*/
                                float scale = mActivity.getResources().getDisplayMetrics().density;
                                int x = (int) (33.0 * scale + 0.5f);
                                int y = (int) (37.0 * scale + 0.5f);
                                boolean bluetoothIsEnabled = mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON;
                                boolean isDiscovering = mLocalAdapter.isDiscovering();
                                int textId = isDiscovering ? R.string.bluetooth_searching_for_devices :
                                                             R.string.bluetooth_search_for_devices;
                                if (!bluetoothIsEnabled) {
                                     mAuroraMenu.removeMenuByItemId(R.id.scan_bt);
                                     mAuroraMenu.removeMenuByItemId(R.id.rename_bt);
                                } else {
                                     mAuroraMenu.addMenu(R.id.scan_bt, textId, 0, 0);
                                     mAuroraMenu.addMenu(R.id.rename_bt, R.string.bluetooth_rename_device, 0, 1);
                                }
                                mActivity.showAuroraMenu(mActionBar, Gravity.RIGHT | Gravity.TOP, 0, 0);
				break;
			default:
				break;
			}
		}
	};

    /*
     * aurora菜单点击事件处理方法
     */
    private OnAuroraMenuItemClickListener auroraSystemMenuCallBack = new AuroraMenuBase.OnAuroraMenuItemClickListener() {

        @Override
        public void auroraMenuItemClick(int itemId) {
            switch (itemId) {
            case R.id.scan_bt: {
                boolean isDiscovering = mLocalAdapter.isDiscovering();
                if (!isDiscovering && mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON) {
                           startScanning();
                           mAuroraMenu.setMenuTextByItemId(R.string.bluetooth_searching_for_devices, R.id.scan_bt);
                }
                break;
            }
            
            case R.id.rename_bt: {
                new BluetoothNameDialogFragment().show(
                        getFragmentManager(), "rename device");
            	break;
            }

            case R.id.received_files_bt: {
                Intent intent = new Intent(BTOPP_ACTION_OPEN_RECEIVED_FILES);
                getActivity().sendBroadcast(intent);
            	break;
            }
            default:
                break;
            }
        }
    };

    @Override
    public void onResume() {
        // resume BluetoothEnabler before calling super.onResume() so we don't get
        // any onDeviceAdded() callbacks before setting up view in updateContent()
        if (mBluetoothEnabler != null) {
            mBluetoothEnabler.resume(getActivity());
        }
        super.onResume();

        mInitiateDiscoverable = true;

        if (isUiRestricted()) {
            setDeviceListGroup(getPreferenceScreen());
            removeAllDevices();
            //mEmptyView.setText(R.string.bluetooth_empty_list_user_restricted);
            return;
        }

        getActivity().registerReceiver(mReceiver, mIntentFilter);
        if (mLocalAdapter != null) {
            updateContent(mLocalAdapter.getBluetoothState());
        }

        mActivity.setAuroraSystemMenuCallBack(auroraSystemMenuCallBack);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBluetoothEnabler != null) {
            mBluetoothEnabler.pause();
        }

        // Make the device only visible to connected devices.
        //mLocalAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE); Aurora linchunhui 20160324 modify for BUG #21025

        if (isUiRestricted())  return;

        mActivity.setAuroraSystemMenuCallBack(null);

        getActivity().unregisterReceiver(mReceiver);
    }

    public void onDestroy() {
        super.onDestroy();

        ///M: ALPS01849536 {@
        if (mProfileDialog != null) {
            mProfileDialog.dismiss();
            mProfileDialog = null;
        }
        /// @}

        if (mPairedDevicesCategory != null) {
            mPairedDevicesCategory.removeAll();
        }
        if (mAvailableDevicesCategory != null) {
            mAvailableDevicesCategory.removeAll();
        }
    }

    private void startScanning() {
       if (isUiRestricted()) return;

        if (!mAvailableDevicesCategoryIsPresent) {
            if (mAvailableDevicesCategory != null)
                getPreferenceScreen().addPreference(mAvailableDevicesCategory);
            mAvailableDevicesCategoryIsPresent = true;
        }

        if (mAvailableDevicesCategory != null) {
            setDeviceListGroup(mAvailableDevicesCategory);
            removeAllDevices();
        }
        else return;
        mLocalManager.getCachedDeviceManager().clearNonBondedDevices();
        mAvailableDevicesCategory.removeAll();
        mInitialScanStarted = true;
        mLocalAdapter.startScanning(true);
    }

    @Override
    void onDevicePreferenceClick(AuroraBluetoothDevicePreference btPreference) {
        mLocalAdapter.stopScanning();
        super.onDevicePreferenceClick(btPreference);
    }

    private void addDeviceCategory(AuroraPreferenceGroup preferenceGroup, int titleId,
            BluetoothDeviceFilter.Filter filter, boolean addCachedDevices) {
        preferenceGroup.setTitle(titleId);
        getPreferenceScreen().addPreference(preferenceGroup);
        setFilter(filter);
        setDeviceListGroup(preferenceGroup);
        if (addCachedDevices) {
            addCachedDevices();
        }
        preferenceGroup.setEnabled(true);
    }

    private void updateContent(int bluetoothState) {
        final AuroraPreferenceScreen preferenceScreen = getPreferenceScreen();
        int messageId = 0;

        Log.d(TAG, "updateContent : " + bluetoothState);

        preferenceScreen.removeAll();
        preferenceScreen.setOrderingAsAdded(true);
        mDevicePreferenceMap.clear();

        if (mBluetoothSwitchPreference == null) {
            mBluetoothSwitchPreference = new AuroraSwitchPreference(getActivity());
        }
        preferenceScreen.addPreference(mBluetoothSwitchPreference);

        switch (bluetoothState) {
            case BluetoothAdapter.STATE_ON:

                if (isUiRestricted()) {
                    messageId = R.string.bluetooth_empty_list_user_restricted;
                    break;
                }
/*
                if (mDeviceNamePref == null) {
                    mDeviceNamePref = new AuroraPreference(getActivity());
                }
                else mDeviceNamePref.auroraSetArrowText(mLocalAdapter.getName(),true);
                preferenceScreen.addPreference(mDeviceNamePref);
                if (mBtAddress == null) {
                    mBtAddress = new AuroraPreference(getActivity());
                }
                else setBtStatus();
                preferenceScreen.addPreference(mBtAddress);
*/

                // Paired devices category
                if (mPairedDevicesCategory == null) {
                    mPairedDevicesCategory = new AuroraPreferenceCategory(getActivity());
                } else {
                    mPairedDevicesCategory.removeAll();
                }
                addDeviceCategory(mPairedDevicesCategory,
                        R.string.bluetooth_preference_paired_devices,
                        BluetoothDeviceFilter.BONDED_DEVICE_FILTER, true);
                int numberOfPairedDevices = mPairedDevicesCategory.getPreferenceCount();

                if (isUiRestricted() || numberOfPairedDevices <= 0) {
                    preferenceScreen.removePreference(mPairedDevicesCategory);
                }

                // Available devices category
                if (mAvailableDevicesCategory == null) {
                    mAvailableDevicesCategory = new AuroraPreferenceCategoryBT(getActivity());
                    mAvailableDevicesCategory.setSelectable(false);
                } else {
                    mAvailableDevicesCategory.removeAll();
                }
                addDeviceCategory(mAvailableDevicesCategory,
                        R.string.bluetooth_preference_found_devices,
                        BluetoothDeviceFilter.UNBONDED_DEVICE_FILTER, mInitialScanStarted);
                int numberOfAvailableDevices = mAvailableDevicesCategory.getPreferenceCount();

                if (!mInitialScanStarted) {
                    startScanning();
                }
/*
                if (mMyDevicePreference == null) {
                    mMyDevicePreference = new AuroraPreference(getActivity());
                }

                mMyDevicePreference.setSummary(getResources().getString(
                            R.string.bluetooth_is_visible_message, mLocalAdapter.getName()));
                mMyDevicePreference.setSelectable(false);
                preferenceScreen.addPreference(mMyDevicePreference);
*/
                getActivity().invalidateOptionsMenu();

                // mLocalAdapter.setScanMode is internally synchronized so it is okay for multiple
                // threads to execute.
//                if (mInitiateDiscoverable)
                {
                    // Make the device visible to other devices.
                    mLocalAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
                    mInitiateDiscoverable = false;
                }
                return; // not break

            case BluetoothAdapter.STATE_TURNING_OFF:
                messageId = R.string.bluetooth_turning_off;
                break;

            case BluetoothAdapter.STATE_OFF:
                messageId = R.string.bluetooth_empty_list_bluetooth_off;
                if (isUiRestricted()) {
                    messageId = R.string.bluetooth_empty_list_user_restricted;
                }

                break;

            case BluetoothAdapter.STATE_TURNING_ON:
                messageId = R.string.bluetooth_turning_on;
                mInitialScanStarted = false;
                break;
        }
        if (mPairedDevicesCategory != null) {
            preferenceScreen.removePreference(mPairedDevicesCategory);
        }
        if (mAvailableDevicesCategory != null) {
            preferenceScreen.removePreference(mAvailableDevicesCategory);
        }
        if (mMyDevicePreference != null) {
            preferenceScreen.removePreference(mMyDevicePreference);
        }
        setDeviceListGroup(preferenceScreen);
        //removeAllDevices();
        // mEmptyView.setText(messageId);
       if (!isUiRestricted()) {
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void onBluetoothStateChanged(int bluetoothState) {
        super.onBluetoothStateChanged(bluetoothState);
        updateContent(bluetoothState);
    }

    @Override
    public void onScanningStateChanged(boolean started) {
        Log.d(TAG, "onScanningStateChanged() started : " + started);
        super.onScanningStateChanged(started);
        refreshScanBar();
        // Update options' enabled state
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
        boolean isDiscovering = mLocalAdapter.isDiscovering();
        int textId = isDiscovering ? R.string.bluetooth_searching_for_devices :
                                     R.string.bluetooth_search_for_devices;
        mAuroraMenu.setMenuTextByItemId(textId, R.id.scan_bt);
    }

    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        setDeviceListGroup(getPreferenceScreen());
        removeAllDevices();
        updateContent(mLocalAdapter.getBluetoothState());
    }

    private final View.OnClickListener mDeviceProfilesListener = new View.OnClickListener() {
        public void onClick(View v) {
            // User clicked on advanced options icon for a device in the list
            if (!(v.getTag() instanceof CachedBluetoothDevice)) {
                Log.w(TAG, "onClick() called for other View: " + v);
                return;
            }

            final CachedBluetoothDevice device = (CachedBluetoothDevice) v.getTag();
            Log.d(TAG, "onClick " + device.getName());

            Bundle args = new Bundle(1);
            args.putParcelable(AuroraDeviceProfilesSettings.EXTRA_DEVICE, device.getDevice());
//wolfu change
           ((SettingsActivity) getActivity()).startPreferencePanel(AuroraDeviceProfilesSettings.class.getName(), args, R.string.bluetooth_device_advanced_title, null, null, 0);

        }
    };

    /**
     * Add a listener, which enables the advanced settings icon.
     * @param preference the newly added preference
     */
    @Override
    void initDevicePreference(AuroraBluetoothDevicePreference preference) {
        CachedBluetoothDevice cachedDevice = preference.getCachedDevice();
        if (cachedDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            // Only paired device have an associated advanced settings screen
            preference.setOnSettingsClickListener(mDeviceProfilesListener);
        }
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_url_bluetooth;
    }


    /*
     * @ 原生创建菜单键弹出menu的方法, 已被屏蔽
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mLocalAdapter == null) return;
        // If the user is not allowed to configure bluetooth, do not show the menu.
        if (isUiRestricted()) return;

        boolean bluetoothIsEnabled = mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON;
        boolean isDiscovering = mLocalAdapter.isDiscovering();
        Log.d(TAG, "onCreateOptionsMenu, isDiscovering " + isDiscovering);
        int textId = isDiscovering ? R.string.bluetooth_searching_for_devices :
            R.string.bluetooth_search_for_devices;
        menu.add(Menu.NONE, MENU_ID_SCAN, 0, textId)
                .setEnabled(bluetoothIsEnabled && !isDiscovering)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(Menu.NONE, MENU_ID_RENAME_DEVICE, 0, R.string.bluetooth_rename_device)
                .setEnabled(bluetoothIsEnabled)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(Menu.NONE, MENU_ID_SHOW_RECEIVED, 0, R.string.bluetooth_show_received_files)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        /// M: Add for Advance Settings
        if (ConfigHelper.isAdvanceSettingEnabled()) {
            menu.add(Menu.NONE, MENU_ID_ADVANCED_SETTING, 0, R.string.bluetooth_advanced_settings)
                .setEnabled(bluetoothIsEnabled)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    /*
     * @ 原生menu item 点击事件的处理方法, 已被屏蔽
     */
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

            case MENU_ID_SHOW_RECEIVED:
                Intent intent = new Intent(BTOPP_ACTION_OPEN_RECEIVED_FILES);
                getActivity().sendBroadcast(intent);
                return true;

            /// M: when click menu "Advanced settings", start corresponding activity @{
            case MENU_ID_ADVANCED_SETTING:
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setAction(LAUNCH_BT_ADVANCED_SETTINGS_ACTION);
                try {
                    startActivity(i);
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "Unable to start activity " + i.toString());
                    return false;
                }
                return true;
            /// @}
        }
        return super.onOptionsItemSelected(item);
    }
/*
    private void updateProgressUi(boolean start) {
        Log.d(TAG, "updateProgressUi " + start);

 //       if (mDeviceListGroup instanceof AuroraPreferenceCategory)
        {
            //((AuroraPreferenceCategory) mDeviceListGroup).setProgress(start);
            //scanBar.setProgress(start);
            scanBar.setVisibility(start ? View.VISIBLE : View.GONE);
            scanButton.setVisibility(start ? View.GONE : View.VISIBLE);
            Log.d(TAG, "setProgress " + start);
        }

    }

    private void refreshScanBar() {
        if (mLocalAdapter == null) return;
        boolean bluetoothIsEnabled = mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON;
        boolean isDiscovering = mLocalAdapter.isDiscovering();



        if(bluetoothIsEnabled && isDiscovering){
            updateProgressUi(true);
        }else{
            updateProgressUi(false);
        }
        scanButton.setEnabled(bluetoothIsEnabled && !isDiscovering);
    }

    private void setBtStatus() {
        Log.d(TAG, "setBtStatus : ");
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        if (bluetooth != null &&  mBtAddress != null) {
            String address = bluetooth.isEnabled() ? bluetooth.getAddress() : null;
            if (!TextUtils.isEmpty(address)) {
                // Convert the address to lowercase for consistency with the wifi MAC address.
                //mBtAddress.setSummary(address.toUpperCase());
                mBtAddress.auroraSetArrowText(address.toUpperCase(),false);
        Log.d(TAG, "setBtStatus : " + address);
                return;
            } 
        }
        mBtAddress.auroraSetArrowText(getString(R.string.status_unavailable),false);
        //mBtAddress.setSummary(getString(R.string.status_unavailable));
    }
*/
    
    private void refreshScanBar() {
        if (mLocalAdapter == null) return;
        boolean bluetoothIsEnabled = mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON;
        boolean isDiscovering = mLocalAdapter.isDiscovering();

        if(mAvailableDevicesCategory!=null && mAvailableDevicesCategory instanceof AuroraPreferenceCategoryBT){
        	((AuroraPreferenceCategoryBT)mAvailableDevicesCategory).viewProgressBar(bluetoothIsEnabled && isDiscovering);
        }
    }
    

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {

                final List<SearchIndexableRaw> result = new ArrayList<SearchIndexableRaw>();

                final Resources res = context.getResources();

                // Add fragment title
                SearchIndexableRaw data = new SearchIndexableRaw(context);
                data.title = res.getString(R.string.bluetooth_settings);
                data.screenTitle = res.getString(R.string.bluetooth_settings);
                result.add(data);

                // Add cached paired BT devices
                LocalBluetoothManager lbtm = LocalBluetoothManager.getInstance(context);
                // LocalBluetoothManager.getInstance can return null if the device does not
                // support bluetooth (e.g. the emulator).
                if (lbtm != null) {
                    Set<BluetoothDevice> bondedDevices =
                            lbtm.getBluetoothAdapter().getBondedDevices();

                    for (BluetoothDevice device : bondedDevices) {
                        data = new SearchIndexableRaw(context);
                        data.title = device.getName();
                        data.screenTitle = res.getString(R.string.bluetooth_settings);
                        data.enabled = enabled;
                        result.add(data);
                    }
                }
                return result;
            }
        };
    

}
