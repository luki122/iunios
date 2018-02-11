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

package com.android.settings.deviceinfo;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Settings;
//import android.preference.CheckBoxPreference;
//import android.preference.AuroraPreferenceScreen;
import android.util.Log;
import android.widget.Toast;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import android.os.SystemProperties;
import aurora.app.AuroraAlertDialog;





//import com.android.settings.AuroraCheckBoxPreference;
import com.android.settings.AuroraSettingsPreferenceFragment;
import com.android.settings.GnSettingsUtils;
import com.android.settings.R;
//import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
//import com.mediatek.settings.deviceinfo.AuroraCheckBoxPreference;
import com.mediatek.settings.deviceinfo.UsbSettingsExts;

import aurora.preference.*;
/**
 * USB storage settings.
 */
public class UsbSettings extends AuroraSettingsPreferenceFragment implements OnPreferenceChangeListener, DialogInterface.OnDismissListener,
DialogInterface.OnClickListener,AuroraPreference.OnPreferenceClickListener {

    private static final String TAG = "UsbSettings";

    private static final String KEY_MTP = "usb_mtp";
    private static final String KEY_PTP = "usb_ptp";
    
    private UsbManager mUsbManager;
    private boolean mUsbAccessoryMode;

    private UsbSettingsExts mUsbExts;
    
    private static final String ENABLE_ADB = "enable_adb";
    private AuroraSwitchPreference mEnableAdb;
    private static final boolean mGNUsbUISupport = SystemProperties.get("ro.gn.usb.ui.support").equals("yes");
    private boolean mOkClicked;
    private Dialog mOkDialog;
    

    private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UsbManager.ACTION_USB_STATE)) {
               mUsbAccessoryMode = intent.getBooleanExtra(UsbManager.USB_FUNCTION_ACCESSORY, false);
               Log.e(TAG, "UsbAccessoryMode " + mUsbAccessoryMode);
            }
            mUsbExts.dealWithBroadcastEvent(intent);
            if (mUsbExts.isNeedExit()) {
                finish();
            } else if (mUsbExts.isNeedUpdate()) {
                updateToggles(mUsbExts.getCurrentFunction());
            }
        }
    };

    private AuroraPreferenceScreen createPreferenceHierarchy() {
        AuroraPreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.aurora_usb_settings);
        root = mUsbExts.addUsbSettingsItem(this);

        UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
            mUsbExts.updateEnableStatus(false);
        }
        
        Log.e(TAG, "mEnableAdb111111111111111111111111111111111111111111111111111111122222222222222222222222222 ");
        mEnableAdb = (AuroraSwitchPreference) this.findPreference(ENABLE_ADB);
        
        Log.e(TAG, "mEnableAdb1111111111111111111111111111111111111111111111111111111--------------------------------------> " + mEnableAdb);
        
        Log.e(TAG, "mEnableAdb1111111111111111111111111111111111111111111111111111111 " + mEnableAdb);
        mEnableAdb.setOnPreferenceChangeListener(this);

        return root;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        mUsbExts = new UsbSettingsExts();
        
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mStateReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make sure we reload the preference hierarchy since some of these settings
        // depend on others...
        createPreferenceHierarchy();

        // ACTION_USB_STATE is sticky so this will call updateToggles
        getActivity().registerReceiver(mStateReceiver,
                mUsbExts.getIntentFilter());
        
        
        final ContentResolver cr = getActivity().getContentResolver();
        mEnableAdb.setChecked(Settings.Secure.getInt(cr,
                Settings.Secure.ADB_ENABLED, 0) != 0);
	    if (mGNUsbUISupport) {
			
			mEnableAdb.setChecked(Settings.Secure.getInt(cr,
					"real_debug_state",0) != 0);
        }
    }

    private void updateToggles(String function) {

        mUsbExts.updateCheckedStatus(function);

        UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
            Log.e(TAG, "USB is locked down");
            mUsbExts.updateEnableStatus(false);
        } else if (!mUsbAccessoryMode) {
            //Enable MTP and PTP switch while USB is not in Accessory Mode, otherwise disable it
            Log.e(TAG, "USB Normal Mode");
            mUsbExts.updateEnableStatus(true);
        } else {
            Log.e(TAG, "USB Accessory Mode");
            mUsbExts.updateEnableStatus(false);
        }

        mUsbExts.setCurrentFunction(function);
    }

    @Override
    public boolean onPreferenceClick(AuroraPreference preference) {

        // Don't allow any changes to take effect as the USB host will be disconnected, killing
        // the monkeys
        if (Utils.isMonkeyRunning()) {
            return true;
        }
        // If this user is disallowed from using USB, don't handle their attempts to change the
        // setting.
        UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
            return true;
        }

        String function = mUsbExts.getFunction(preference);
        boolean makeDefault = mUsbExts.isMakeDefault(preference);
        mUsbManager.setCurrentFunction(function, makeDefault);
        updateToggles(function);

        mUsbExts.setNeedUpdate(false);
        return true;
    }
    
    private void dismissDialog() {
        if (mOkDialog == null) return;
        mOkDialog.dismiss();
        mOkDialog = null;
    }
    
    
    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
    	
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
                /**
                 * If confirm dialog is showing,return
                 */
                if (mOkDialog != null && mOkDialog.isShowing()){
                	return true;
                }
                
                if (GnSettingsUtils.sGnSettingSupport) {
                	if(mOkDialog != null){
                		mOkDialog = new AuroraAlertDialog.Builder(getActivity(), AuroraAlertDialog.THEME_AMIGO_FULLSCREEN).setMessage(
                                getActivity().getResources().getString(R.string.adb_warning_message))
                                .setTitle(R.string.adb_warning_title)
                                .setPositiveButton(android.R.string.yes, this)
                                .setNegativeButton(android.R.string.no, this)
                                .create();
                	}
                    if(!mOkDialog.isShowing()){
                    	mOkDialog.show();
                    }
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
    	
    	return true;
    	
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
    
    
	@Override
	public void onDismiss(DialogInterface dialog) {
		// TODO Auto-generated method stub
        if (!mOkClicked) {
            mEnableAdb.setChecked(false);
        }
	}
}
