/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.settings;


import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreferenceGroup;
import aurora.preference.AuroraPreferenceScreen;
import android.provider.Settings;
import android.security.KeyStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import android.security.KeyStore;

import com.android.internal.widget.LockPatternUtils;
import com.gionee.settings.utils.GnUtils;
//import android.telephony.MSimTelephonyManager;
import java.util.ArrayList;
import java.util.List;

// Gionee fengjianyi 2012-09-28 add for CR00705430 start
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
// Gionee fengjianyi 2012-09-28 add for CR00705430 end

//Gionee <qiuxd> <2013-06-15> add for CR00826405 begin
import android.view.View;
import android.view.WindowManager;
import android.view.Gravity;
import android.graphics.PixelFormat;
//Gionee <qiuxd> <2013-06-15> add for CR00826405 end
//gionee gaojt add for navil start
import android.os.SystemProperties;

import gionee.telephony.GnTelephonyManager;

//gionee gaojt add for navil end

/**
 * Gesture lock pattern settings.
 */
public class SecuritySettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener, DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
    // gionee gaojt add navil start
    private final static boolean OPEN_NAIVL = SystemProperties.get("ro.gn.navil.lock.scrn").equals("yes");
    // gionee gaojt add navil start

    static final String TAG = "SecuritySettings";

    // Aurora <likai> <2013-11-04> add begin
    private static final String KEY_PRIVACY_PROTECTION_CATEGORY = "privacy_protection_category";
    private static final String KEY_PASSWORD_CATEGORY = "password_category";
    // Aurora <likai> <2013-11-04> add end
    private static final String GN_GUEST_MODE = "gionee_guest_mode";
    private static final String GN_GUEST_PASS_ENABLE = "gionee_guest_pass_enable";
    private static final String GN_GUEST_PASS = "gionee_guest_pass";

    // Lock Settings
    private static final String KEY_UNLOCK_SET_OR_CHANGE = "unlock_set_or_change";
    private static final String KEY_BIOMETRIC_WEAK_IMPROVE_MATCHING =
            "biometric_weak_improve_matching";
    private static final String KEY_BIOMETRIC_WEAK_LIVELINESS = "biometric_weak_liveliness";
    private static final String KEY_LOCK_ENABLED = "lockenabled";
    private static final String KEY_VISIBLE_PATTERN = "visiblepattern";
    private static final String KEY_SECURITY_CATEGORY = "security_category";
    private static final String KEY_DEVICE_ADMIN_CATEGORY = "device_admin_category";
    private static final String KEY_LOCK_AFTER_TIMEOUT = "lock_after_timeout";
    private static final String KEY_OWNER_INFO_SETTINGS = "owner_info_settings";
    private static final int SET_OR_CHANGE_LOCK_METHOD_REQUEST = 123;
    private static final int CONFIRM_EXISTING_FOR_BIOMETRIC_WEAK_IMPROVE_REQUEST = 124;
    private static final int CONFIRM_EXISTING_FOR_BIOMETRIC_WEAK_LIVELINESS_OFF = 125;

    private static final String KEY_CREDENTIAL_STORAGE_TYPE = "credential_storage_type";
    
    // Misc Settings
    private static final String KEY_SIM_LOCK = "sim_lock";
    private static final String KEY_SHOW_PASSWORD = "show_password";
    private static final String KEY_RESET_CREDENTIALS = "reset_credentials";
    private static final String KEY_TOGGLE_INSTALL_APPLICATIONS = "toggle_install_applications";
    private static final String KEY_TOGGLE_VERIFY_APPLICATIONS = "toggle_verify_applications";
    private static final String KEY_POWER_INSTANTLY_LOCKS = "power_button_instantly_locks";
    private static final String KEY_CREDENTIALS_MANAGER = "credentials_management";
    private static final String PACKAGE_MIME_TYPE = "application/vnd.android.package-archive";
    //Gionee <chenml> <2013-08-02> add for CR00845848 begin
    private static final String KEY_CREDENTIALS_INSTALL = "credentials_install";
    //Gionee <chenml> <2013-08-02> add for CR00845848 end
   // Gionee fengjianyi 2012-09-28 add for CR00705430 start
    private static final boolean GN_GUEST_MODE_SUPPORT =
    	android.os.SystemProperties.get("ro.gn.guestmode.support").equals("yes");
    private static final String KEY_GUEST_MODE = "guest_mode";
    // Gionee fengjianyi 2012-09-28 add for CR00705430 end
    // Gionee <qiuxd> <2013-04-26> modify for CR00802506 begin
    private static final String KEY_GUEST_PASS_ENABLE = "password_enable";
    private static final String KEY_GUEST_ALTER_PASS = "alter_password";        
    // Gionee <qiuxd> <2013-04-26> modify for CR00802506 end
    // Gionee <qiuxd> <2013-05-08> modify for CR00809610 begin
    private static final String KEY_GUEST_HIDE_APP = "hide_app";
    private static final int HIDE_APP_TRUE = 130;
    private static final int HIDE_APP_FALSE = 131;
    // Gionee <qiuxd> <2013-05-08> modify for CR00809610 end
    
    // Gionee <qiuxd> <2013-06-15> add for CR00826405 begin
    private static final int GIONEE_DEFAULT_SCREEN = 403;
    public static final int GIONEE_LOCK_SCREEN_NAVIL = 408;
    private WindowManager mWM;
    private View mPopupView;
    // Gionee <qiuxd> <2013-06-15> modify for CR00826405 end
    
    DevicePolicyManager mDPM;

    private ChooseLockSettingsHelper mChooseLockSettingsHelper;
    private LockPatternUtils mLockPatternUtils;
    private AuroraListPreference mLockAfter;

    private AuroraCheckBoxPreference mBiometricWeakLiveliness;
    private AuroraSwitchPreference mVisiblePattern;

    private AuroraCheckBoxPreference mShowPassword;

    private KeyStore mKeyStore;
    private AuroraPreference mResetCredentials;

    private AuroraSwitchPreference mToggleAppInstallation;
    private Dialog mWarnInstallApps;
    private AuroraSwitchPreference mToggleVerifyApps;
    private AuroraCheckBoxPreference mPowerButtonInstantlyLocks;

    private boolean mIsPrimary;
    private boolean isCancel;
    private boolean isDismiss = false;

    // Gionee fengjianyi 2012-10-20 add for CR00705430 start
    private AuroraCheckBoxPreference mGuestMode;
    private AuroraAlertDialog mWarnGuestMode;
    private ContentObserver mSettingsObserver;
    private boolean mNeedResetGuestMode = true;
    // Gionee fengjianyi 2012-10-20 add for CR00705430 end
    
    // Gionee <qiuxd> <2013-04-26> modify for CR00802506 begin
    private AuroraCheckBoxPreference mEnablePassPre;
    private AuroraPreference mAlterPassPre;
    // Gionee <qiuxd> <2013-04-26> modify for CR00802506 end
    
    // Gionee <qiuxd> <2013-05-08> modify for CR00809610 begin
    private AuroraPreference mHideApp;
    // Gionee <qiuxd> <2013-05-08> modify for CR00809610 end

    //Gionee <chenml> <2013-08-02> add for CR00845848 begin
    private static final String sGnSDCardType = android.os.SystemProperties.get("ro.gn.sdcard.type", "no");
    private AuroraPreference mCredentialsInstall;
    private AuroraPreferenceCategory mCredentialsManager;


	private static final String SINGLE_SIM_LOCK_KEY = "sim_lock_settings_category";
    private String [] dual_sim_lock_key = {"sim01_lock_settings_category", "sim02_lock_settings_category"};

    private int getSDcardType(){
        if(sGnSDCardType.equals("no")){
            return 0;
        }else if(sGnSDCardType.equals("internal")){
            return 1;            
        }else if(sGnSDCardType.equals("external")){
            return 2;
        }else{
            Log.e(TAG, "SD Card Type Unknow");
            return 3;
        }
    }
    //Gionee <chenml> <2013-08-02> add for CR00845848 end
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mLockPatternUtils = new LockPatternUtils(getActivity());

        mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        
        //Gionee <qiuxd> <2013-06-15> add for CR00826405 begin
        mWM = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        //Gionee <qiuxd> <2013-06-15> add for CR00826405 end

        mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());
        // Gionee fengjianyi 2012-09-28 add for CR00705430 start
        if (GN_GUEST_MODE_SUPPORT) {
            mSettingsObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {

    			@Override
    			public void onChange(boolean selfChange) {
    				Log.d(TAG, "Guest mode changed!");
    				super.onChange(selfChange);
    				if (!mNeedResetGuestMode) {
    					Log.d(TAG, "No need to reset guest mode.");
    					mNeedResetGuestMode = true;
    					return;
    				}
        			if (mGuestMode != null) {
        		        mGuestMode.setChecked(isGuestModeEnabled());
        			}
    			}
            	
    		};
    		getContentResolver().registerContentObserver(Settings.Secure.getUriFor(
                    GN_GUEST_MODE), false, mSettingsObserver);
        }
        // Gionee fengjianyi 2012-09-28 add for CR00705430 end
    }
    private AuroraPreferenceScreen createPreferenceHierarchy() {
        AuroraPreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.security_settings);
        root = getPreferenceScreen();

        // Gionee fengjianyi 2012-09-28 add for CR00705430 start
        if (GN_GUEST_MODE_SUPPORT) {
            addPreferencesFromResource(R.xml.security_settings_guest);

            mGuestMode = (AuroraCheckBoxPreference) findPreference(KEY_GUEST_MODE);
            mGuestMode.setChecked(isGuestModeEnabled());

            // Gionee <qiuxd> <2013-04-26> modify for CR00802506 begin
            mEnablePassPre = (AuroraCheckBoxPreference) findPreference(KEY_GUEST_PASS_ENABLE);
            mAlterPassPre = (AuroraPreference) findPreference(KEY_GUEST_ALTER_PASS);
            mEnablePassPre.setChecked(isPassEnabled());
            mAlterPassPre.setEnabled(isPassSetted());
            // Gionee <qiuxd> <2013-04-26> modify for CR00802506 end
            
            // Gionee <qiuxd> <2013-05-08> modify for CR00809610 begin
            //mHideApp = (AuroraPreference) findPreference(KEY_GUEST_HIDE_APP);
            removePreference(KEY_GUEST_HIDE_APP);
            // Gionee <qiuxd> <2013-05-08> modify for CR00809610 end
            
        }
        // Gionee fengjianyi 2012-09-28 add for CR00705430 end

        // Add options for lock/unlock screen
        int resid = 0;
        if (!mLockPatternUtils.isSecure()) {
            // if there are multiple users, disable "None" setting
            UserManager mUm = (UserManager) getSystemService(Context.USER_SERVICE);
            List<UserInfo> users = mUm.getUsers(true);
            final boolean singleUser = users.size() == 1;

            if (singleUser && mLockPatternUtils.isLockScreenDisabled()) {
                resid = R.xml.security_settings_lockscreen;
            } else {
                resid = R.xml.security_settings_chooser;
            }
        } else if (mLockPatternUtils.usingBiometricWeak() &&
                mLockPatternUtils.isBiometricWeakInstalled()) {
            resid = R.xml.security_settings_biometric_weak;
        } else {
            switch (mLockPatternUtils.getKeyguardStoredPasswordQuality()) {
                case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
                    resid = R.xml.security_settings_pattern;
                    break;
                case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
                    resid = R.xml.security_settings_pin;
                    break;
                case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:
                case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
                case DevicePolicyManager.PASSWORD_QUALITY_COMPLEX:
                    resid = R.xml.security_settings_password;
                    break;
            }
        }
        addPreferencesFromResource(resid);


        // Add options for device encryption
        DevicePolicyManager dpm =
                (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        mIsPrimary = UserHandle.myUserId() == UserHandle.USER_OWNER;

        if (!mIsPrimary) {
            // Rename owner info settings
            AuroraPreference ownerInfoPref = findPreference(KEY_OWNER_INFO_SETTINGS);
            if (ownerInfoPref != null) {
                ownerInfoPref.setTitle(R.string.user_info_settings_title);
            }
        }
        //Gionee <wangguojing> <2013-08-08> add for CR00846725 begin
        AuroraPreferenceGroup gnSecurityCategory = (AuroraPreferenceGroup) root.findPreference(KEY_SECURITY_CATEGORY);
        AuroraPreference gnOwnerInfoPref = findPreference(KEY_OWNER_INFO_SETTINGS);
//           if (OPEN_NAIVL) {
        	   if (gnSecurityCategory != null && gnOwnerInfoPref != null) {
        		   gnSecurityCategory.removePreference(gnOwnerInfoPref);
        	   }
//           }
        //Gionee <wangguojing> <2013-08-08> add for CR00846725 end

        if (mIsPrimary) {
            switch (dpm.getStorageEncryptionStatus()) {
            case DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE:
                // The device is currently encrypted.
		    // gionee wangyaohui 20120703 add for CR00633706 begin
                    // addPreferencesFromResource(R.xml.security_settings_encrypted);
		    // gionee wangyaohui 20120703 add for CR00633706 end 
                break;
            case DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE:
                // This device supports encryption but isn't encrypted.
           // gionee wangyaohui 20120703 add for CR00633706 begin
		    // addPreferencesFromResource(R.xml.security_settings_unencrypted);
		    // gionee wangyaohui 20120703 add for CR00633706 end 
                break;
            }
        }

        // lock after preference
        mLockAfter = (AuroraListPreference) root.findPreference(KEY_LOCK_AFTER_TIMEOUT);
        if (mLockAfter != null) {
            setupLockAfterPreference();
            updateLockAfterPreferenceSummary();
        }

        // biometric weak liveliness
        mBiometricWeakLiveliness =
                (AuroraCheckBoxPreference) root.findPreference(KEY_BIOMETRIC_WEAK_LIVELINESS);

        // visible pattern
        mVisiblePattern = (AuroraSwitchPreference) root.findPreference(KEY_VISIBLE_PATTERN);
	if(mVisiblePattern != null) {
		mVisiblePattern.setOnPreferenceChangeListener(this);
	}

        // lock instantly on power key press
        mPowerButtonInstantlyLocks = (AuroraCheckBoxPreference) root.findPreference(
                KEY_POWER_INSTANTLY_LOCKS);
        if (gnSecurityCategory != null && mPowerButtonInstantlyLocks != null) {
            gnSecurityCategory.removePreference(mPowerButtonInstantlyLocks);
        }

        // don't display visible pattern if biometric and backup is not pattern
        if (resid == R.xml.security_settings_biometric_weak &&
                mLockPatternUtils.getKeyguardStoredPasswordQuality() !=
                DevicePolicyManager.PASSWORD_QUALITY_SOMETHING) {
            AuroraPreferenceGroup securityCategory = (AuroraPreferenceGroup)
                    root.findPreference(KEY_SECURITY_CATEGORY);
            if (securityCategory != null && mVisiblePattern != null) {
                securityCategory.removePreference(root.findPreference(KEY_VISIBLE_PATTERN));
            }
        }

        // Append the rest of the settings

        addPreferencesFromResource(R.xml.security_settings_misc);

		// add by txy, add for dual sim lock settings {@
		AuroraPreferenceGroup simLockCategory = (AuroraPreferenceGroup)root.findPreference(KEY_SIM_LOCK);
        if (GnTelephonyManager.isMultiSimEnabled()) {
			simLockCategory.removePreference(root.findPreference(SINGLE_SIM_LOCK_KEY));
            GnTelephonyManager tm = GnTelephonyManager.getDefault();
            int numPhones = tm.getPhoneCount();
            boolean disableLock = true;
            boolean removeLock = true;
	    	int length = dual_sim_lock_key.length ;
            for (int i = 0; i < numPhones; i++) {
				// Do not display SIM lock for devices without an Icc card
				if(i < length){
					root.findPreference(dual_sim_lock_key[i]).setEnabled(false);
				}
                if (tm.hasIccCardGemini(i)) {
                    // Disable SIM lock if sim card is missing or unknown
                    removeLock = false;
                    if (!((tm.getSimStateGemini(i) == TelephonyManager.SIM_STATE_ABSENT)
                            || (tm.getSimStateGemini(i) == TelephonyManager.SIM_STATE_UNKNOWN)
                            || (tm.getSimStateGemini(i) == 6))) {
                        disableLock = false;
                    }
					if(i < length && !disableLock){
						root.findPreference(dual_sim_lock_key[i]).setEnabled(true);
					}
                }
            }
            if (removeLock) {
                root.removePreference(simLockCategory);
            } else {
                if (disableLock) {
                    simLockCategory.setEnabled(false);
                }
            }
        } else {
            // Do not display SIM lock for devices without an Icc card
			for(String simLockKey : dual_sim_lock_key) {
				simLockCategory.removePreference(root.findPreference(simLockKey));
			}

            TelephonyManager tm = TelephonyManager.getDefault();
            if (!mIsPrimary || !tm.hasIccCard()) {
                root.removePreference(root.findPreference(KEY_SIM_LOCK));
            } else {
                // Disable SIM lock if sim card is missing or unknown
                if ((TelephonyManager.getDefault().getSimState() ==
                                     TelephonyManager.SIM_STATE_ABSENT) ||
                    (TelephonyManager.getDefault().getSimState() ==
                                     TelephonyManager.SIM_STATE_UNKNOWN)) {
                    root.findPreference(KEY_SIM_LOCK).setEnabled(false);
                }
            }
        }
		// @}add by Steve,tang, control dual sim card state 

        // Show password
        mShowPassword = (AuroraCheckBoxPreference) root.findPreference(KEY_SHOW_PASSWORD);

        
     // Credential storage
        if(PlatformUtils.isAddCredentialStorage()){
        	mKeyStore = KeyStore.getInstance(); // needs to be initialized for onResume()
        	if (PlatformUtils.isUserManager_HasUserRestriction(getActivity())) {
        		AuroraPreference credentialStorageType = (AuroraPreference)root.findPreference(KEY_CREDENTIAL_STORAGE_TYPE);
        		final int storageSummaryRes =
        				mKeyStore.isHardwareBacked() ? R.string.credential_storage_type_hardware
        						: R.string.credential_storage_type_software;
        		credentialStorageType.setSummary(storageSummaryRes);
        	} else {
        		removePreference(KEY_CREDENTIALS_MANAGER);
        	}
        }else{
        	removePreference(KEY_CREDENTIALS_MANAGER);
        }
        
        
        // Credential storage, only for primary user
        if (mIsPrimary) {
            mResetCredentials = root.findPreference(KEY_RESET_CREDENTIALS);
        } else {
            removePreference(KEY_CREDENTIALS_MANAGER);
        }
        //Gionee <chenml> <2013-08-02> add for CR00845848 begin
        mCredentialsInstall = (AuroraPreference)findPreference(KEY_CREDENTIALS_INSTALL);
        mCredentialsManager = (AuroraPreferenceCategory) findPreference(KEY_CREDENTIALS_MANAGER);
         if ( mCredentialsManager != null ){
             if(getSDcardType()==1 && mCredentialsInstall !=null){
                 ((AuroraPreferenceGroup) findPreference(KEY_CREDENTIALS_MANAGER)).removePreference(mCredentialsInstall);
             }
         }
         //Gionee <chenml> <2013-08-02> add for CR00845848 end
        mToggleAppInstallation = (AuroraSwitchPreference) findPreference(
                KEY_TOGGLE_INSTALL_APPLICATIONS);
	mToggleAppInstallation.setOnPreferenceChangeListener(this);
        mToggleAppInstallation.setChecked(isNonMarketAppsAllowed());

        // Package verification, only visible to primary user and if enabled
        mToggleVerifyApps = (AuroraSwitchPreference) findPreference(KEY_TOGGLE_VERIFY_APPLICATIONS);
        if (mIsPrimary && showVerifierSetting()) {
            if (isVerifierInstalled()) {
                mToggleVerifyApps.setChecked(isVerifyAppsEnabled());
            } else {
                mToggleVerifyApps.setChecked(false);
                mToggleVerifyApps.setEnabled(false);
            }
        } else {
            AuroraPreferenceGroup deviceAdminCategory= (AuroraPreferenceGroup)
                    root.findPreference(KEY_DEVICE_ADMIN_CATEGORY);
            if (deviceAdminCategory != null) {
                deviceAdminCategory.removePreference(mToggleVerifyApps);
            } else {
                mToggleVerifyApps.setEnabled(false);
            }
        }
        // Gionee <wangyaohui><2013-03-21> add for CR00786913 begin
        if(!GnUtils.isAbroadVersion()){
	        AuroraPreferenceGroup deviceAdminCategory= (AuroraPreferenceGroup)
	                 root.findPreference(KEY_DEVICE_ADMIN_CATEGORY);
			if((mToggleVerifyApps != null) && (deviceAdminCategory != null)){
	           deviceAdminCategory.removePreference(mToggleVerifyApps);
			}
        }
        // Gionee <wangyaohui><2013-03-21> add for CR00786913 end 

        // Aurora <likai> <2013-11-04> add begin
        if (findPreference(KEY_PRIVACY_PROTECTION_CATEGORY) != null) {
            root.removePreference(findPreference(KEY_PRIVACY_PROTECTION_CATEGORY));
        }
        if (findPreference(KEY_SECURITY_CATEGORY) != null) {
//            root.removePreference(findPreference(KEY_SECURITY_CATEGORY));
        }
        if (findPreference(KEY_SIM_LOCK) != null) {
//            root.removePreference(findPreference(KEY_SIM_LOCK));
        }
        if (findPreference(KEY_PASSWORD_CATEGORY) != null) {
            root.removePreference(findPreference(KEY_PASSWORD_CATEGORY));
        }
        if (findPreference(KEY_CREDENTIALS_MANAGER) != null) {
        //    root.removePreference(findPreference(KEY_CREDENTIALS_MANAGER));
        }
        // Aurora <likai> <2013-11-04> add end

        return root;
    }

    // Gionee fengjianyi 2012-09-28 add for CR00705430 start
    private boolean isGuestModeEnabled() {
    	boolean enabled = Settings.Secure.getInt(getContentResolver(),
    			GN_GUEST_MODE, 0) == 1;
    	Log.d(TAG, "Guest mode is " + enabled);
    	return enabled;
        }

    private void setGuestModeEnabled(boolean enabled) {
    	Log.d("qiuxd", "Set guest mode " + enabled);
    	Settings.Secure.putInt(getContentResolver(),
    			GN_GUEST_MODE, enabled ? 1 : 0);
    	// Gionee <qiuxd> <2013-05-13> add for CR00809610 begin
    	//mHandler.sendEmptyMessage(enabled?HIDE_APP_TRUE:HIDE_APP_FALSE); 
    	// Gionee <qiuxd> <2013-05-13> add for CR00809610 end
    }
    // Gionee fengjianyi 2012-09-28 add for CR00705430 end
    
    // Gionee <qiuxd> <2013-04-26> modify for CR00802506 begin
    private boolean isPassEnabled (){
        boolean enabled = Settings.Secure.getInt(getContentResolver(),
                GN_GUEST_PASS_ENABLE, 0) == 1;
        return enabled;
    }
    
    private void setPassEnabled (boolean enabled){
        Log.d(TAG, "Set pass enable " + enabled);
        Settings.Secure.putInt(getContentResolver(),
                GN_GUEST_PASS_ENABLE, enabled ? 1 : 0);
    }
    
    private boolean isPassSetted(){
        String pass = "";
        pass = Settings.Secure.getString(getContentResolver(), GN_GUEST_PASS);
        if(!TextUtils.isEmpty(pass)){
            return true;
        }
        return false;
    }
    // Gionee <qiuxd> <2013-04-26> modify for CR00802506 end

    private boolean isNonMarketAppsAllowed() {
        return Settings.Global.getInt(getContentResolver(),
                                      Settings.Global.INSTALL_NON_MARKET_APPS, 0) > 0;
    }

    private void setNonMarketAppsAllowed(boolean enabled) {
        // Change the system setting
        Settings.Global.putInt(getContentResolver(), Settings.Global.INSTALL_NON_MARKET_APPS,
                                enabled ? 1 : 0);
    }
 // Gionee fengjianyi 2012-10-20 add for CR00705430 start
    private void warnGuestMode() {
        if(mWarnGuestMode == null){
            //Gionee <chenml> <2013-08-23> modify for CR00857897 begin
            mWarnGuestMode = new AuroraAlertDialog.Builder(getActivity()/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/).setTitle(
            //Gionee <chenml> <2013-08-23> modify for CR00857897 end
                    getResources().getString(R.string.guest_mode))
                    .setMessage(getResources().getString(R.string.goto_guest_mode_warning))
                    .setPositiveButton(android.R.string.yes, this)
                    .setNegativeButton(android.R.string.no, this)
                    .create();
            mWarnGuestMode.show();
        }else{
            mWarnGuestMode.show();
        }
    }
    // Gionee fengjianyi 2012-10-20 add for CR00705430 end

    private boolean isVerifyAppsEnabled() {
        return Settings.Global.getInt(getContentResolver(),
                                      Settings.Global.PACKAGE_VERIFIER_ENABLE, 1) > 0;
    }

    private boolean isVerifierInstalled() {
        final PackageManager pm = getPackageManager();
        final Intent verification = new Intent(Intent.ACTION_PACKAGE_NEEDS_VERIFICATION);
        verification.setType(PACKAGE_MIME_TYPE);
        verification.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        final List<ResolveInfo> receivers = pm.queryBroadcastReceivers(verification, 0);
        return (receivers.size() > 0) ? true : false;
    }

    private boolean showVerifierSetting() {
        return Settings.Global.getInt(getContentResolver(),
                                      Settings.Global.PACKAGE_VERIFIER_SETTING_VISIBLE, 1) > 0;
    }

    private void warnAppInstallation() {
        // TODO: DialogFragment?
        //Gionee:zhang_xin 2012-12-18 modify for CR00746738 start
        /*
        mWarnInstallApps = new AuroraAlertDialog.Builder(getActivity()).setTitle(
                getResources().getString(R.string.error_title))
                .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                .setMessage(getResources().getString(R.string.install_all_warning))
                .setPositiveButton(android.R.string.yes, this)
                .setNegativeButton(android.R.string.no, null)
                .show();
        */
        //Gionee <chenml> <2013-08-23> modify for CR00857897 begin
        mWarnInstallApps = new AuroraAlertDialog.Builder(getActivity()/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/).setTitle(
        //Gionee <chenml> <2013-08-23> modify for CR00857897 end
                getResources().getString(R.string.gn_settings_warning))
                .setMessage(getResources().getString(R.string.install_all_warning))
                .setPositiveButton(android.R.string.yes, this)
                .setNegativeButton(android.R.string.no, this)
               //Gionee <chenml> <2013-04-16> modify for CR00795603 begin
               //.setCancelable(false)  
               //Gionee <chenml> <2013-04-16> modify for CR00795603 end
                .show();
	mWarnInstallApps.setOnDismissListener(this);
        //Gionee:zhang_xin 2012-12-18 modify for CR00746738 end
    }

    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mWarnInstallApps) {
		if(which == DialogInterface.BUTTON_POSITIVE) {
            		setNonMarketAppsAllowed(true);
            		if (mToggleAppInstallation != null) {
                	mToggleAppInstallation.setChecked(true);
            		}
		isDismiss = true;	
		}else if (which == DialogInterface.BUTTON_NEGATIVE){
			mToggleAppInstallation.setChecked(false);
			isCancel = true;
			isDismiss = true;		
		}
        }
        // Gionee fengjianyi 2012-09-28 add for CR00705430 start
        else if (dialog == mWarnGuestMode) {
        	if (which == DialogInterface.BUTTON_POSITIVE) {
        		setGuestModeEnabled(true);
        		mGuestMode.setChecked(true);
        		// Gionee <qiuxd> <2013-06-15> add for CR00826405 begin
        		// check if the switch has been turned on.
                if (Settings.Secure.getInt(getContentResolver(),
                        "gionee_first_open_guest"/*Settings.Secure.GN_FIRST_OPEN_GUEST_MODE*/, 0) == 0) {
                    // check wether first time turn on Guest mode in navi desktop
                    if (isUsingNaviDesktop()) {
                        // not open need to show guide toast
                        showGuideView();
                        Settings.Secure.putInt(getContentResolver(),
                                "gionee_first_open_guest"/*Settings.Secure.GN_FIRST_OPEN_GUEST_MODE*/, 1);
                    }
                }
                // Gionee <qiuxd> <2013-06-15> add for CR00826405 begin
        	} else if (which == DialogInterface.BUTTON_NEGATIVE) {
        		mNeedResetGuestMode = true;
        	}
        }
        // Gionee fengjianyi 2012-09-28 add for CR00705430 end
    }
    
    public void onDismiss(DialogInterface dialog) {
        // Assuming that onClick gets called first
	if(!isDismiss) {
		mToggleAppInstallation.setChecked(false);
		isCancel = true;
	}else {
		isDismiss = false;
	}
	//mToggleAppInstallation.setChecked(isNonMarketAppsAllowed());
	//mToggleAppInstallation.isChecked();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWarnInstallApps != null) {
            mWarnInstallApps.dismiss();
	    isCancel = true;
        }

        // Gionee fengjianyi 2012-09-28 add for CR00705430 start
        if (GN_GUEST_MODE_SUPPORT) {
        	if (mSettingsObserver != null) {
        		getContentResolver().unregisterContentObserver(mSettingsObserver);
        		mSettingsObserver = null;
        	}
        }
        // Gionee fengjianyi 2012-09-28 add for CR00705430 end
    }

    private void setupLockAfterPreference() {
        // Compatible with pre-Froyo
        long currentTimeout = Settings.Secure.getLong(getContentResolver(),
                Settings.Secure.LOCK_SCREEN_LOCK_AFTER_TIMEOUT, 5000);
        mLockAfter.setValue(String.valueOf(currentTimeout));
        mLockAfter.setOnPreferenceChangeListener(this);
        final long adminTimeout = (mDPM != null ? mDPM.getMaximumTimeToLock(null) : 0);
        final long displayTimeout = Math.max(0,
                Settings.System.getInt(getContentResolver(), SCREEN_OFF_TIMEOUT, 0));
        if (adminTimeout > 0) {
            // This setting is a slave to display timeout when a device policy is enforced.
            // As such, maxLockTimeout = adminTimeout - displayTimeout.
            // If there isn't enough time, shows "immediately" setting.
            disableUnusableTimeouts(Math.max(0, adminTimeout - displayTimeout));
        }
    }

    private void updateLockAfterPreferenceSummary() {
        // Update summary message with current value
        long currentTimeout = Settings.Secure.getLong(getContentResolver(),
                Settings.Secure.LOCK_SCREEN_LOCK_AFTER_TIMEOUT, 5000);
        final CharSequence[] entries = mLockAfter.getEntries();
        final CharSequence[] values = mLockAfter.getEntryValues();
        int best = 0;
        for (int i = 0; i < values.length; i++) {
            long timeout = Long.valueOf(values[i].toString());
            if (currentTimeout >= timeout) {
                best = i;
            }
        }
        mLockAfter.setSummary(getString(R.string.lock_after_timeout_summary, entries[best]));
    }

    private void disableUnusableTimeouts(long maxTimeout) {
        final CharSequence[] entries = mLockAfter.getEntries();
        final CharSequence[] values = mLockAfter.getEntryValues();
        ArrayList<CharSequence> revisedEntries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> revisedValues = new ArrayList<CharSequence>();
        for (int i = 0; i < values.length; i++) {
            long timeout = Long.valueOf(values[i].toString());
            if (timeout <= maxTimeout) {
                revisedEntries.add(entries[i]);
                revisedValues.add(values[i]);
            }
        }
        if (revisedEntries.size() != entries.length || revisedValues.size() != values.length) {
            mLockAfter.setEntries(
                    revisedEntries.toArray(new CharSequence[revisedEntries.size()]));
            mLockAfter.setEntryValues(
                    revisedValues.toArray(new CharSequence[revisedValues.size()]));
            final int userPreference = Integer.valueOf(mLockAfter.getValue());
            if (userPreference <= maxTimeout) {
                mLockAfter.setValue(String.valueOf(userPreference));
            } else {
                // There will be no highlighted selection since nothing in the list matches
                // maxTimeout. The user can still select anything less than maxTimeout.
                // TODO: maybe append maxTimeout to the list and mark selected.
            }
        }
        mLockAfter.setEnabled(revisedEntries.size() > 0);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make sure we reload the preference hierarchy since some of these settings
        // depend on others...
        createPreferenceHierarchy();
	
        final LockPatternUtils lockPatternUtils = mChooseLockSettingsHelper.utils();
        if (mBiometricWeakLiveliness != null) {
            mBiometricWeakLiveliness.setChecked(
                    lockPatternUtils.isBiometricWeakLivelinessEnabled());
        }
        if (mVisiblePattern != null) {
            mVisiblePattern.setChecked(lockPatternUtils.isVisiblePatternEnabled());
        }
        if (mPowerButtonInstantlyLocks != null) {
            mPowerButtonInstantlyLocks.setChecked(lockPatternUtils.getPowerButtonInstantlyLocks());
        }

        if (mShowPassword != null) {
            mShowPassword.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.TEXT_SHOW_PASSWORD, 1) != 0);
        }

        KeyStore.State state = KeyStore.getInstance().state();
        if (mResetCredentials != null) {
            mResetCredentials.setEnabled(state != KeyStore.State.UNINITIALIZED);
        }
    }

    // Gionee <baorui> <2012-08-12> add for CR00850708 begin
    @Override
    public void onPause() {
        super.onPause();
	if (mWarnInstallApps != null) {
            mWarnInstallApps.dismiss();
	    isCancel = false;
	    isDismiss = true;
        }
        if (GN_GUEST_MODE_SUPPORT) {
            if (mPopupView != null) {
                mWM.removeView(mPopupView);
                mPopupView = null;
            }
        }
    }
    // Gionee <baorui> <2012-08-12> add for CR00850708 end

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        final String key = preference.getKey();

        final LockPatternUtils lockPatternUtils = mChooseLockSettingsHelper.utils();
        if (KEY_UNLOCK_SET_OR_CHANGE.equals(key)) {
            startFragment(this, "com.android.settings.ChooseLockGeneric$ChooseLockGenericFragment",
                    SET_OR_CHANGE_LOCK_METHOD_REQUEST, null);
        } else if (KEY_BIOMETRIC_WEAK_IMPROVE_MATCHING.equals(key)) {
            ChooseLockSettingsHelper helper =
                    new ChooseLockSettingsHelper(this.getActivity(), this);
            if (!helper.launchConfirmationActivity(
                    CONFIRM_EXISTING_FOR_BIOMETRIC_WEAK_IMPROVE_REQUEST, null, null)) {
                // If this returns false, it means no password confirmation is required, so
                // go ahead and start improve.
                // Note: currently a backup is required for biometric_weak so this code path
                // can't be reached, but is here in case things change in the future
                startBiometricWeakImprove();
            }
        } else if (KEY_BIOMETRIC_WEAK_LIVELINESS.equals(key)) {
            if (isToggled(preference)) {
                lockPatternUtils.setBiometricWeakLivelinessEnabled(true);
            } else {
                // In this case the user has just unchecked the checkbox, but this action requires
                // them to confirm their password.  We need to re-check the checkbox until
                // they've confirmed their password
                mBiometricWeakLiveliness.setChecked(true);
                ChooseLockSettingsHelper helper =
                        new ChooseLockSettingsHelper(this.getActivity(), this);
                if (!helper.launchConfirmationActivity(
                                CONFIRM_EXISTING_FOR_BIOMETRIC_WEAK_LIVELINESS_OFF, null, null)) {
                    // If this returns false, it means no password confirmation is required, so
                    // go ahead and uncheck it here.
                    // Note: currently a backup is required for biometric_weak so this code path
                    // can't be reached, but is here in case things change in the future
                    lockPatternUtils.setBiometricWeakLivelinessEnabled(false);
                    mBiometricWeakLiveliness.setChecked(false);
                }
            }
        } else if (KEY_LOCK_ENABLED.equals(key)) {
            lockPatternUtils.setLockPatternEnabled(isToggled(preference));
        } else if (KEY_VISIBLE_PATTERN.equals(key)) {
            lockPatternUtils.setVisiblePatternEnabled(isToggled(preference));
        } else if (KEY_POWER_INSTANTLY_LOCKS.equals(key)) {
            lockPatternUtils.setPowerButtonInstantlyLocks(isToggled(preference));
        } else if (preference == mShowPassword) {
            Settings.System.putInt(getContentResolver(), Settings.System.TEXT_SHOW_PASSWORD,
                    mShowPassword.isChecked() ? 1 : 0);
        }else if (KEY_TOGGLE_VERIFY_APPLICATIONS.equals(key)) {
            Settings.Global.putInt(getContentResolver(), Settings.Global.PACKAGE_VERIFIER_ENABLE,
                    mToggleVerifyApps.isChecked() ? 1 : 0);
 // Gionee fengjianyi 2012-09-28 add for CR00705430 start
        } else if (preference == mGuestMode) {
        	mNeedResetGuestMode = false;
        	if (mGuestMode.isChecked()) {
        		mGuestMode.setChecked(false);
        		warnGuestMode();
        	} else {
      	    // Gionee <qiuxd> <2013-04-26> modify for CR00802506 begin
        		//setGuestModeEnabled(false);
        	    if(isPassEnabled()){
        	        Intent intent = new Intent();
                    intent.setClassName("com.android.settings","com.android.settings.GuestPasswordCenter");
                    intent.putExtra("guest_mode", "validate_pass");
                    startActivity(intent);
        	    }else{
        	        setGuestModeEnabled(false);
        	    }
                // Gionee <qiuxd> <2013-04-26> modify for CR00802506 end
        	}
        // Gionee fengjianyi 2012-09-28 add for CR00705430 end
        }       
       // Gionee <qiuxd> <2013-04-26> modify for CR00802506 begin
        else if (KEY_GUEST_PASS_ENABLE.equals(key)) {
            if (isPassEnabled()) {
                // need to validate pass
                Intent intent = new Intent();
                intent.setClassName("com.android.settings","com.android.settings.GuestPasswordCenter");
                intent.putExtra("guest_mode", "exit_pass");
                startActivity(intent);
                //mEnablePassPre.setChecked(false);
                //setPassEnabled(false);
            } else {
                if (isPassSetted()) {
                    // open password and alter settings
                    Intent intent = new Intent();
                    intent.setClassName("com.android.settings","com.android.settings.GuestPasswordCenter");
                    intent.putExtra("guest_mode", "open_pass");
                    startActivity(intent);
                    //mEnablePassPre.setChecked(true);
                    //setPassEnabled(true);
                } else {
                    // need init pass
                    //GuestPasswordCenter
                    Intent intent = new Intent();
                    intent.setClassName("com.android.settings","com.android.settings.GuestPasswordCenter");
                    intent.putExtra("guest_mode", "set_pass");
                    startActivity(intent);
                }
            }
        } else if (KEY_GUEST_ALTER_PASS.equals(key)) {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings","com.android.settings.GuestPasswordCenter");
            intent.putExtra("guest_mode", "alter_pass");
            startActivity(intent);
         // Gionee <qiuxd> <2013-04-26> modify for CR00802506 end
        // Gionee <qiuxd> <2013-05-08> add for CR00809610 begin
        //}else if(KEY_GUEST_HIDE_APP.equals(key)){
        //    if(!isGuestModeEnabled()){
        //        Intent intent = new Intent();  
        //        intent.setClassName("com.android.settings","com.android.settings.HideAppCenter");
        //        startActivity(intent);
        //    }else{
        //        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.hide_app_toast), Toast.LENGTH_SHORT).show();
        //    }
            
        // Gionee <qiuxd> <2013-05-08> add for CR00809610 end
        }else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }

    private boolean isToggled(AuroraPreference pref) {
        return ((AuroraCheckBoxPreference) pref).isChecked();
    }

    private boolean isSwitchToggled(AuroraPreference pref) {
        return ((AuroraSwitchPreference) pref).isChecked();
    }

    /**
     * see confirmPatternThenDisableAndClear
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONFIRM_EXISTING_FOR_BIOMETRIC_WEAK_IMPROVE_REQUEST &&
                resultCode == AuroraActivity.RESULT_OK) {
            startBiometricWeakImprove();
            return;
        } else if (requestCode == CONFIRM_EXISTING_FOR_BIOMETRIC_WEAK_LIVELINESS_OFF &&
                resultCode == AuroraActivity.RESULT_OK) {
            final LockPatternUtils lockPatternUtils = mChooseLockSettingsHelper.utils();
            lockPatternUtils.setBiometricWeakLivelinessEnabled(false);
            // Setting the mBiometricWeakLiveliness checked value to false is handled when onResume
            // is called by grabbing the value from lockPatternUtils.  We can't set it here
            // because mBiometricWeakLiveliness could be null
            return;
        }
        createPreferenceHierarchy();
    }

    public boolean onPreferenceChange(AuroraPreference preference, Object value) {
        if (preference == mLockAfter) {
            int timeout = Integer.parseInt((String) value);
            try {
                Settings.Secure.putInt(getContentResolver(),
                        Settings.Secure.LOCK_SCREEN_LOCK_AFTER_TIMEOUT, timeout);
            } catch (NumberFormatException e) {
                Log.e("SecuritySettings", "could not persist lockAfter timeout setting", e);
            }
            updateLockAfterPreferenceSummary();
        } else if (preference == mToggleAppInstallation) {
	    if(isCancel) {
		isCancel = false;
	}else {
		if (!mToggleAppInstallation.isChecked()) {
                mToggleAppInstallation.setChecked(true);
                warnAppInstallation();
            } else {
                setNonMarketAppsAllowed(false);
            }	
	}
        }else if (preference == mVisiblePattern) {
	    final LockPatternUtils lockPatternUtils = mChooseLockSettingsHelper.utils();
            lockPatternUtils.setVisiblePatternEnabled(!isSwitchToggled(preference));
        } 
        return true;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_url_security;
    }

    public void startBiometricWeakImprove(){
        Intent intent = new Intent();
        intent.setClassName("com.android.facelock", "com.android.facelock.AddToSetup");
        startActivity(intent);
    }
    // Gionee <qiuxd> <2013-05-13> add for CR00809610 begin
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case HIDE_APP_TRUE:
                    GnSettingsUtils.setPackageEnabled(getActivity().getApplicationContext(),true);
                    break;
                case HIDE_APP_FALSE:
                    GnSettingsUtils.setPackageEnabled(getActivity().getApplicationContext(),false);
                    break;
                default:
                    break;
            }
        }
    };
    // Gionee <qiuxd> <2013-05-13> add for CR00809610 end
    
    // Gionee <qiuxd> <2013-06-15> add for CR00826405 begin
    private boolean isUsingNaviDesktop(){
        int curLockScreen = Settings.System.getInt(
                getContentResolver(), "curlockscreen",
                android.os.SystemProperties.getInt("curlockscreen", GIONEE_DEFAULT_SCREEN));
        if(curLockScreen == GIONEE_LOCK_SCREEN_NAVIL){
            return true;
        }
        return false;
    }
    
    private void showGuideView(){
        
        if(mPopupView == null){
            mPopupView = View.inflate(getActivity(), R.layout.gn_guide_pupo_view, null);
        }
        
        mPopupView.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mWM.removeView(mPopupView);
                // Gionee <baorui><2013-08-24> modify for CR00854996 begin
                mPopupView = null;
                // Gionee <baorui><2013-08-24> modify for CR00854996 end
            }
        });
        
        android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
        lp.type = lp.TYPE_SYSTEM_ERROR;
        lp.flags = android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        lp.format= PixelFormat.RGBA_8888;
        lp.width = lp.WRAP_CONTENT;
        lp.height = lp.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        mWM.addView(mPopupView, lp); 
    }
    // Gionee <qiuxd> <2013-06-15> add for CR00826405 end
}
