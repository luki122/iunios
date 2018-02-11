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

package com.android.settings.deviceinfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import aurora.app.AuroraAlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.os.storage.StorageManager;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;

import com.android.settings.AuroraUsbPreference;
import com.android.settings.GnSettingsUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import aurora.preference.AuroraPreferenceActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;

/**
 * USB storage settings.
 */
public class UsbSettings extends SettingsPreferenceFragment implements AdapterView.OnItemClickListener,OnPreferenceChangeListener,
DialogInterface.OnClickListener, DialogInterface.OnDismissListener,OnPreferenceClickListener{

    private static final String TAG = "UsbSettings";

    private static final String KEY_MTP = "usb_mtp";
    private static final String KEY_PTP = "usb_ptp";

    private UsbManager mUsbManager;
    private AuroraCheckBoxPreference mMtp;
    private AuroraCheckBoxPreference mPtp;
    private boolean mUsbConnected;
    
    // qy 2014 03 27
    private ListView mUsbListview;
    private List<String> mTitle;
	private List<String> mSummary;	
	private StorageManager mStorageManager = null;
	
	private boolean DEBUG_LOG = true;
	
	 // We could not know what's the usb charge mode config of each device, which
    // may be defined in some sh source file. So here use a hard code for reference,
    // you should modify this value according to device usb init config.
    private static final String USB_FUNCTION_CHARGING =
            "diag,serial_smd,serial_tty,rmnet_bam,mass_storage";
    private static final String AURORA_USB_MTP ="mtp,diag";
    private boolean isClickItem =false;
    private static final String ENABLE_ADB = "enable_adb";
    private AuroraSwitchPreference mEnableAdb;
    private static final boolean mGNUsbUISupport = SystemProperties.get("ro.gn.usb.ui.support").equals("yes");
    private boolean mOkClicked;
    private Dialog mOkDialog;
    private static final String KEY_MTP_AURORA="mtp_aurora";
    private static final String KEY_PTP_AURORA="ptp_aurora";
    private AuroraUsbPreference mAuroraMtpPref;
    private AuroraUsbPreference mAuroraPtpPref;
    

    private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UsbManager.ACTION_USB_STATE)) {
            	mUsbConnected = intent.getExtras().getBoolean(UsbManager.USB_CONNECTED);
              
               if(!mUsbConnected){
            	mStorageManager.disableUsbMassStorage();

            	setMtpMode();
        	    getActivity().finish();               
       			return;
       			
               }else{
            	  
            	   updateCheckedPostion(mUsbManager.getDefaultFunction());
               }
            }
//            updateToggles(mUsbManager.getDefaultFunction()); // qy 2014 03 27

           
        }
    };
    
    private void setMtpMode(){
        Log.i(TAG, " setMtpMode ");
    	/*String buildModel = Build.MODEL;
        if (buildModel.contains("U810")) {*/
    	String deviceName = SystemProperties.get("ro.product.name");
        if (deviceName.contains("IUNI")) {
        	SharedPreferences iuniSP = getActivity().getSharedPreferences("iuni", Context.MODE_PRIVATE);
        	boolean isComEnable = iuniSP.getBoolean("imei", false);
            Log.v(TAG, "*****setMtpMode**** = " + isComEnable);
        	if(isComEnable){
        		mUsbManager.setCurrentFunction(AURORA_USB_MTP, true);
        	}else{
        		mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MTP, true);
        	}
        	        	
        	
        }else{
        	mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MTP, true);
        }
    }
    
   

    private AuroraPreferenceScreen createPreferenceHierarchy() {
        AuroraPreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.usb_settings);
        root = getPreferenceScreen();

        mMtp = (AuroraCheckBoxPreference)root.findPreference(KEY_MTP);
        mPtp = (AuroraCheckBoxPreference)root.findPreference(KEY_PTP);

        return root;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        mTitle = new ArrayList<String>();
        mSummary = new ArrayList<String>();
        
        mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        
     // ACTION_USB_STATE is sticky so this will call updateToggles
        getActivity().registerReceiver(mStateReceiver,
                new IntentFilter(UsbManager.ACTION_USB_STATE));
        // qy add 2014 05 19
        AuroraActionBar actionbar = ((AuroraPreferenceActivity)getActivity()).getAuroraActionBar();
        if(actionbar != null){
        	actionbar.setDisplayHomeAsUpEnabled(false);
        }
        
    }
    
    // add qy 2014 03 27
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	String[] title =getActivity().getResources().getStringArray(R.array.usb_conn_title_entries);
    	String[] summary = getActivity().getResources().getStringArray(R.array.usb_conn_summary_entries);
    	final View view = inflater.inflate(R.layout.aurora_usb_settings_layout, container, false);
    	
        
        addPreferencesFromResource(R.xml.aurora_usb_settings_prefs);

        mEnableAdb = (AuroraSwitchPreference) findPreference(ENABLE_ADB);
        mEnableAdb.setOnPreferenceChangeListener(this);
        
       /* for(int i =0; i<2; i++){
        	newPref((i-2)+"", title[i],summary[i],i-2);
        }*/
        mAuroraMtpPref =  newPref(KEY_MTP_AURORA, title[0],summary[0],-2);
        mAuroraPtpPref = newPref(KEY_PTP_AURORA, title[1],summary[1],-1);
        updateCheckedPostion(mUsbManager.getDefaultFunction()); // update
//        mAuroraMtpPref.setChecked(true);
//        mAuroraPtpPref.setChecked(false);
        return view;
    }
    
    @Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
    	Log.i("qy", "onItemClick(");
    	view.setBackgroundColor(android.R.color.white);    	

    	isClickItem = true;
    	switch (position) {
		
		
		case 0:
			mStorageManager.disableUsbMassStorage();
//			mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MTP, true);
			setMtpMode();
			break;
		case 1:	
			mStorageManager.disableUsbMassStorage();
			mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_PTP, true);
			break;
		case 2:
//			mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MASS_STORAGE, true);	
			mUsbManager.setCurrentFunction(USB_FUNCTION_CHARGING, true);
			mStorageManager.enableUsbMassStorage();
			break;

		default:
			break;
		}
    	
    	
    }
    
    private void updateCheckedPostion(String function){
    	Log.i("qy", ""+function);
    	if (UsbManager.USB_FUNCTION_MTP.equals(function) || AURORA_USB_MTP.equals(function)) {
    		if(mAuroraMtpPref !=null && mAuroraPtpPref!=null){
    	        mAuroraMtpPref.setChecked(true);
    	        mAuroraPtpPref.setChecked(false);
    		}

    		mStorageManager.disableUsbMassStorage();
        } else if (UsbManager.USB_FUNCTION_PTP.equals(function)) {

        	if(mAuroraMtpPref !=null && mAuroraPtpPref!=null){
    	        mAuroraMtpPref.setChecked(false);
    	        mAuroraPtpPref.setChecked(true);
    		}
        	mStorageManager.disableUsbMassStorage();
        } else{

        	mStorageManager.enableUsbMassStorage();
        }
    	
    	

    }
    
    private AuroraUsbPreference newPref(String key, String title,String summary,int order){
    	AuroraUsbPreference pref = new AuroraUsbPreference(getActivity());
        

        pref.setKey(key);

        pref.setTitle(title);
        pref.setSummary(summary);
        pref.setPersistent(false);
        pref.setOnPreferenceClickListener(this);
        pref.setOrder(order);
        getPreferenceScreen().addPreference(pref);
        return pref;
    }
    // qy add end

    @Override
    public void onPause() {
        super.onPause();
        isClickItem =false;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make sure we reload the preference hierarchy since some of these settings
        // depend on others...
//        createPreferenceHierarchy(); // qy 2014 03 27
        
        
        final ContentResolver cr = getActivity().getContentResolver();
        mEnableAdb.setChecked(Settings.Secure.getInt(cr,
                Settings.Secure.ADB_ENABLED, 0) != 0);
	    if (mGNUsbUISupport) {
			
			mEnableAdb.setChecked(Settings.Secure.getInt(cr,
					"real_debug_state",0) != 0);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mStateReceiver);
    }

   /* private void updateToggles(String function) {
        if (UsbManager.USB_FUNCTION_MTP.equals(function)) {
            mMtp.setChecked(true);
            mPtp.setChecked(false);
        } else if (UsbManager.USB_FUNCTION_PTP.equals(function)) {
            mMtp.setChecked(false);
            mPtp.setChecked(true);
        } else  {
            mMtp.setChecked(false);
            mPtp.setChecked(false);
        }

        if (!mUsbAccessoryMode) {
            //Enable MTP and PTP switch while USB is not in Accessory Mode, otherwise disable it
            Log.e(TAG, "USB Normal Mode");
            mMtp.setEnabled(true);
            mPtp.setEnabled(true);
        } else {
            Log.e(TAG, "USB Accessory Mode");
            mMtp.setEnabled(false);
            mPtp.setEnabled(false);
        }

    }*/

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {

       /* // Don't allow any changes to take effect as the USB host will be disconnected, killing
        // the monkeys
        if (Utils.isMonkeyRunning()) {
            return true;
        }
        // temporary hack - using check boxes as radio buttons
        // don't allow unchecking them
        if (preference instanceof AuroraCheckBoxPreference) {
            AuroraCheckBoxPreference checkBox = (AuroraCheckBoxPreference)preference;
            if (!checkBox.isChecked()) {
                checkBox.setChecked(true);
                return true;
            }
        }
        if (preference == mMtp) {
            mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MTP, true);
            updateToggles(UsbManager.USB_FUNCTION_MTP);
        } else if (preference == mPtp) {
            mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_PTP, true);
            updateToggles(UsbManager.USB_FUNCTION_PTP);
        }*/ // modify qy 2014 03 27
        return true;
    }
    
    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {

    	// qy modify
    	if (preference == mEnableAdb) {
    		boolean defState = Settings.Secure.getInt(getActivity().getContentResolver(),Settings.Secure.ADB_ENABLED, 0) != 0 ? true:false;
    		if (mGNUsbUISupport) {
    			defState =  Settings.Secure.getInt(getActivity().getContentResolver(), "real_debug_state",0) != 0 && defState? true:false;
               
            }
    		if(defState == (Boolean) newValue){
    			return true;
    		}
    		
    		
            if ((Boolean) newValue) {
                mOkClicked = false;
                if (mOkDialog != null) dismissDialog();
                
                if (GnSettingsUtils.sGnSettingSupport) {
                    mOkDialog = new AuroraAlertDialog.Builder(getActivity(), AuroraAlertDialog.THEME_AMIGO_FULLSCREEN).setMessage(
                            getActivity().getResources().getString(R.string.adb_warning_message))
                            .setTitle(R.string.adb_warning_title)
                            .setPositiveButton(android.R.string.yes, this)
                            .setNegativeButton(android.R.string.no, this)
                            .show();
                    mOkDialog.setCanceledOnTouchOutside(false);
                } else {
                    mOkDialog = new AuroraAlertDialog.Builder(getActivity()).setMessage(
                            getActivity().getResources().getString(R.string.adb_warning_message))
                            .setTitle(R.string.adb_warning_title)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, this)
                            .setNegativeButton(android.R.string.no, this)
                            .show();
                }
                
                mOkDialog.setOnDismissListener(this);
            } else {

                Settings.Secure.putInt(getActivity().getContentResolver(),
                        Settings.Secure.ADB_ENABLED, 0);
               
                if (mGNUsbUISupport) {
                    Settings.Secure.putInt(getActivity().getContentResolver(),
                        "real_debug_state", 0);
                    Log.d("DevelopmentSettings", "onPreferenceTreeClick() set real_debug_state 0");
                }
                
            }
          }
    	
    	/*if(preference.getKey().equals(KEY_MTP_AURORA)){
    		Log.i("qy", TAG+"---->KEY_MTP_AURORA");
    		mStorageManager.disableUsbMassStorage();
    		setMtpMode();
    	}else if(preference.getKey().equals(KEY_PTP_AURORA)){
    		
    		mStorageManager.disableUsbMassStorage();
			mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_PTP, true);
    	}*/

        return true;
    }
    
    private void dismissDialog() {
        if (mOkDialog == null) return;
        mOkDialog.dismiss();
        mOkDialog = null;
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            mOkClicked = true;
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.ADB_ENABLED, 1);
            
            if (mGNUsbUISupport) {
                Settings.Secure.putInt(getActivity().getContentResolver(),
                    "real_debug_state", 1);
                Log.d("DevelopmentSettings", "onClick() set real_debug_state 1");
            }
            
        } else {
            // Reset the toggle
            mEnableAdb.setChecked(false);
        }
    }

    public void onDismiss(DialogInterface dialog) {
        // Assuming that onClick gets called first
        if (!mOkClicked) {
            mEnableAdb.setChecked(false);
        }
    }



	@Override
	public boolean onPreferenceClick(AuroraPreference preference) {
		// TODO Auto-generated method stub
    	if(preference.getKey().equals(KEY_MTP_AURORA)){
    		Log.i("qy", TAG+"---->KEY_MTP_AURORA");
//    		mStorageManager.disableUsbMassStorage();
    		setMtpMode();
    		((AuroraUsbPreference)preference).setChecked(true);
    		mAuroraPtpPref.setChecked(false);
//    		mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MTP, true);
    	}else if(preference.getKey().equals(KEY_PTP_AURORA)){
    		mAuroraMtpPref.setChecked(false);
    		((AuroraUsbPreference)preference).setChecked(true);
//    		mStorageManager.disableUsbMassStorage();
			mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_PTP, true);
    	}

		return false;
	}
}
