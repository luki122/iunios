/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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


import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SearchIndexableData;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.security.KeyStore;
import android.service.trust.TrustAgentService;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ListView;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.TrustAgentUtils.TrustAgentComponentInfo;
import com.android.settings.fingerprint.AuroraFingerprintUtils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Index;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.mediatek.settings.FeatureOption;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.ext.IDataProtectionExt;
import com.mediatek.settings.ext.IMdmPermissionControlExt;
import com.mediatek.settings.ext.IPermissionControlExt;
import com.mediatek.settings.ext.IPplSettingsEntryExt;
import com.mediatek.settings.ext.ISettingsMiscExt;

import java.util.ArrayList;
import java.util.List;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceGroup;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;
import aurora.widget.AuroraActionBar;
import gionee.telephony.GnTelephonyManager;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

//<Aurora><hujianwei>20150724 modify start
//<Aurora><hujianwei>20150724 modify end


/**
 * Gesture lock pattern settings.
 */
public class SecuritySettings extends AuroraSettingsPreferenceFragment
        implements AuroraPreference.OnPreferenceChangeListener, DialogInterface.OnClickListener, Indexable, DialogInterface.OnKeyListener {
    private static final String TRUST_AGENT_CLICK_INTENT = "trust_agent_click_intent";
    static final String TAG = "SecuritySettings";
    private static final Intent TRUST_AGENT_INTENT =
            new Intent(TrustAgentService.SERVICE_INTERFACE);

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
    private static final String KEY_ADVANCED_SECURITY = "advanced_security";
    private static final String KEY_MANAGE_TRUST_AGENTS = "manage_trust_agents";

    private static final int SET_OR_CHANGE_LOCK_METHOD_REQUEST = 123;
    private static final int CONFIRM_EXISTING_FOR_BIOMETRIC_WEAK_IMPROVE_REQUEST = 124;
    private static final int CONFIRM_EXISTING_FOR_BIOMETRIC_WEAK_LIVELINESS_OFF = 125;
    private static final int CHANGE_TRUST_AGENT_SETTINGS = 126;

    // Misc Settings
    private static final String KEY_SIM_LOCK = "sim_lock";
    private static final String KEY_SHOW_PASSWORD = "show_password";
    private static final String KEY_CREDENTIAL_STORAGE_TYPE = "credential_storage_type";
    private static final String KEY_RESET_CREDENTIALS = "credentials_reset";
    private static final String KEY_CREDENTIALS_INSTALL = "credentials_install";
    private static final String KEY_TOGGLE_INSTALL_APPLICATIONS = "toggle_install_applications";
    private static final String KEY_POWER_INSTANTLY_LOCKS = "power_button_instantly_locks";
    private static final String KEY_CREDENTIALS_MANAGER = "credentials_management";
    private static final String PACKAGE_MIME_TYPE = "application/vnd.android.package-archive";
    private static final String KEY_TRUST_AGENT = "trust_agent";
    private static final String KEY_SCREEN_PINNING = "screen_pinning_settings";

    // These switch preferences need special handling since they're not all stored in Settings.
    private static final String SWITCH_PREFERENCE_KEYS[] = {KEY_LOCK_AFTER_TIMEOUT,
            KEY_LOCK_ENABLED, KEY_VISIBLE_PATTERN, KEY_BIOMETRIC_WEAK_LIVELINESS,
            KEY_POWER_INSTANTLY_LOCKS, KEY_SHOW_PASSWORD, KEY_TOGGLE_INSTALL_APPLICATIONS};

    // Only allow one trust agent on the platform.
    private static final boolean ONLY_ONE_TRUST_AGENT = true;

    private DevicePolicyManager mDPM;
    private SubscriptionManager mSubscriptionManager;

    private ChooseLockSettingsHelper mChooseLockSettingsHelper;
    private LockPatternUtils mLockPatternUtils;
    private AuroraListPreference mLockAfter;

    private AuroraSwitchPreference mBiometricWeakLiveliness;
    private AuroraSwitchPreference mVisiblePattern;

    private AuroraSwitchPreference mShowPassword;

    private KeyStore mKeyStore;
    private AuroraPreference mResetCredentials;

    private AuroraSwitchPreference mToggleAppInstallation;
    private Dialog mWarnInstallApps;
    private AuroraSwitchPreference mPowerButtonInstantlyLocks;

    private boolean mIsPrimary;

    private Intent mTrustAgentClickIntent;

    /**
     * M: make sure scrolling action occurs after the binding action completed @ {
     */
    private static final String KEY_SIM_LOCK_PREF = "sim_lock_pref";
    private static final int DELAY_MILLIS = 500;
    private int mUnknownSourcesPosition;
    private boolean mScrollToUnknownSources;
    private Handler mScrollHandler = new Handler();

    private Runnable mScrollRunner = new Runnable() {

        public void run() {
            ListView lstView = getListView();
            // the index of the listview start from 0, then mUnknownSourcesPosition - 1 as the index.
            lstView.setItemChecked(mUnknownSourcesPosition - 1, true);
            lstView.smoothScrollToPosition(mUnknownSourcesPosition - 1);
        }
    };
    /**
     * @}
     */

    private static IPermissionControlExt mPermCtrlExt;
    private IPplSettingsEntryExt mPplExt;
    private IMdmPermissionControlExt mMdmPermCtrlExt;
    // add plugin for data protection
    private IDataProtectionExt mDataProectExt;

    private ISettingsMiscExt mExt;

    //<Aurora><hujanwei> 20150724 modify for simlock settings start
    private static final String SINGLE_SIM_LOCK_KEY = "sim_lock_settings";
    private String[] dual_sim_lock_key = {"sim01_lock_settings_category", "sim02_lock_settings_category"};
    //<Aurora><hujianwei> 20150724 modify for simlock settings end


    private void setMtp() {
        Log.e("JOY", "setMtp");
        UsbManager mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
            Log.e("JOY", "DISALLOW_USB_FILE_TRANSFER");
            return;
        }

        String function = UsbManager.USB_FUNCTION_MTP;
        mUsbManager.setCurrentFunction(function, true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setMtp();

        mSubscriptionManager = SubscriptionManager.from(getActivity());

        mLockPatternUtils = new LockPatternUtils(getActivity());

        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());

        if (savedInstanceState != null
                && savedInstanceState.containsKey(TRUST_AGENT_CLICK_INTENT)) {
            mTrustAgentClickIntent = savedInstanceState.getParcelable(TRUST_AGENT_CLICK_INTENT);
        }
        /**M: get the action in the intent starting the activity @ {*/
        String action = getActivity().getIntent().getAction();
        if (android.provider.Settings.ACTION_SECURITY_SETTINGS.equals(action)) {
            mScrollToUnknownSources = true;
        }
        /** @}*/

        mPermCtrlExt = (IPermissionControlExt) UtilsExt.getPermControlExtPlugin(getActivity());
        mPplExt = (IPplSettingsEntryExt) UtilsExt.getPrivacyProtectionLockExtPlugin(getActivity());
        mMdmPermCtrlExt = (IMdmPermissionControlExt) UtilsExt.getMdmPermControlExtPlugin(getActivity());
        mDataProectExt = (IDataProtectionExt) UtilsExt.getDataProectExtPlugin(getActivity());

        mExt = (ISettingsMiscExt) UtilsExt.getMiscPlugin(getActivity());
    }

    private static int getResIdForLockUnlockScreen(Context context,
                                                   LockPatternUtils lockPatternUtils) {
        int resid = 0;
        if (!lockPatternUtils.isSecure()) {
            // if there are multiple users, disable "None" setting
            UserManager mUm = (UserManager) context.getSystemService(Context.USER_SERVICE);
            List<UserInfo> users = mUm.getUsers(true);
            final boolean singleUser = users.size() == 1;

            if (singleUser && lockPatternUtils.isLockScreenDisabled()) {
                resid = R.xml.security_settings_lockscreen;
            } else {
                resid = R.xml.security_settings_chooser;
            }
        } else if (lockPatternUtils.usingBiometricWeak() &&
                lockPatternUtils.isBiometricWeakInstalled()) {
            resid = R.xml.security_settings_biometric_weak;
        } else if (lockPatternUtils.usingVoiceWeak()) {
            resid = R.xml.security_settings_voice_weak;
        } else {
            switch (lockPatternUtils.getKeyguardStoredPasswordQuality()) {
                case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
                    resid = R.xml.security_settings_pattern;
                    break;
                case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
                case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX:
                    resid = R.xml.security_settings_pin;
                    break;
                case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:
                case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
                case DevicePolicyManager.PASSWORD_QUALITY_COMPLEX:
                    resid = R.xml.security_settings_password;
                    break;
            }
        }
        return resid;
    }

    /**
     * Important!
     * <p/>
     * Don't forget to update the SecuritySearchIndexProvider if you are doing any change in the
     * logic or adding/removing preferences here.
     */
    private AuroraPreferenceScreen createPreferenceHierarchy() {
        AuroraPreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.security_settings);
        root = getPreferenceScreen();

        if (AuroraFingerprintUtils.isFingerPrintSupported()) {
            addPreferencesFromResource(R.xml.finger_settings);
        }
        // Add options for lock/unlock screen
        final int resid = getResIdForLockUnlockScreen(getActivity(), mLockPatternUtils);
        addPreferencesFromResource(resid);

        // Add options for device encryption
        mIsPrimary = UserHandle.myUserId() == UserHandle.USER_OWNER;

        if (!mIsPrimary) {
            // Rename owner info settings
            AuroraPreference ownerInfoPref = findPreference(KEY_OWNER_INFO_SETTINGS);
            if (ownerInfoPref != null) {
                if (UserManager.get(getActivity()).isLinkedUser()) {
                    ownerInfoPref.setTitle(R.string.profile_info_settings_title);
                } else {
                    ownerInfoPref.setTitle(R.string.user_info_settings_title);
                }
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

        ///M: only emmc project support the feature
        boolean isEMMC = FeatureOption.MTK_EMMC_SUPPORT && !FeatureOption.MTK_CACHE_MERGE_SUPPORT;
        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean isLowRAM = am.isLowRamDevice();
        if (isEMMC && !isLowRAM && mIsPrimary) {
            //--------------change "security_category" from by JM begin---------------------
//            if (LockPatternUtils.isDeviceEncryptionEnabled()) {
//                // The device is currently encrypted.
//                addPreferencesFromResource(R.xml.security_settings_encrypted);
//            } else {
//                // This device supports encryption but isn't encrypted.
//                addPreferencesFromResource(R.xml.security_settings_unencrypted);
//            }
            //--------------change "security_category" from by JM end---------------------
        }

        // Trust Agent preferences
        AuroraPreferenceGroup securityCategory = (AuroraPreferenceGroup)
                root.findPreference(KEY_SECURITY_CATEGORY);
        if (securityCategory != null) {
            final boolean hasSecurity = mLockPatternUtils.isSecure();
            ArrayList<TrustAgentComponentInfo> agents =
                    getActiveTrustAgents(getPackageManager(), mLockPatternUtils);
            for (int i = 0; i < agents.size(); i++) {
                final TrustAgentComponentInfo agent = agents.get(i);
                AuroraPreference trustAgentPreference =
                        new AuroraPreference(securityCategory.getContext());
                trustAgentPreference.setKey(KEY_TRUST_AGENT);
                trustAgentPreference.setTitle(agent.title);
                trustAgentPreference.setSummary(agent.summary);
                // Create intent for this preference.
                Intent intent = new Intent();
                intent.setComponent(agent.componentName);
                intent.setAction(Intent.ACTION_MAIN);
                trustAgentPreference.setIntent(intent);
                //<Aurora> <hujianwei> 20150805 modify start
                // Add preference to the settings menu.
                //securityCategory.addPreference(trustAgentPreference);
                //if (!hasSecurity) {
                //  trustAgentPreference.setEnabled(false);
                // trustAgentPreference.setSummary(R.string.disabled_because_no_backup_security);
                // }
                //<Aurora><hujianwei> 20150805 modify end
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
                (AuroraSwitchPreference) root.findPreference(KEY_BIOMETRIC_WEAK_LIVELINESS);

        // visible pattern
        mVisiblePattern = (AuroraSwitchPreference) root.findPreference(KEY_VISIBLE_PATTERN);

        //Aurora <hujianwei> <2015-07-16> modify for remove mPowerButtonInstantlyLocks begin
        // lock instantly on power key press

        //mPowerButtonInstantlyLocks = (AuroraSwitchPreference) root.findPreference(
        //        KEY_POWER_INSTANTLY_LOCKS);

        AuroraPreference trustAgentPreference = root.findPreference(KEY_TRUST_AGENT);

        /*
        if (mPowerButtonInstantlyLocks != null &&
                trustAgentPreference != null &&
                trustAgentPreference.getTitle().length() > 0) {
        	Log.d(TAG, "get String:" + getString(
                    R.string.lockpattern_settings_power_button_instantly_locks_summary,
                    trustAgentPreference.getTitle()) );
            mPowerButtonInstantlyLocks.setSummary(getString(
                    R.string.lockpattern_settings_power_button_instantly_locks_summary,
                    trustAgentPreference.getTitle()));
        }
        */
        //Aurora <hujianwei> <2015-07-16> modify for remove mPowerButtonInstantlyLocks end

        // don't display visible pattern if biometric and backup is not pattern
        ///M: Add for voice unlock.
        if ((resid == R.xml.security_settings_biometric_weak ||
                resid == R.xml.security_settings_voice_weak) &&
                mLockPatternUtils.getKeyguardStoredPasswordQuality() !=
                        DevicePolicyManager.PASSWORD_QUALITY_SOMETHING) {
            if (securityCategory != null && mVisiblePattern != null) {
                securityCategory.removePreference(root.findPreference(KEY_VISIBLE_PATTERN));
            }
        }

        // Append the rest of the settings
        if (AuroraFingerprintUtils.isFingerPrintSupported()) {
            addPreferencesFromResource(R.xml.security_settings_misc_finger);
        } else {
            addPreferencesFromResource(R.xml.security_settings_misc_normal);
           
        }

        //Aurora <hujianwei> <2015-07-24> modify for remove SIM manager begin
        /*
        ///M: feature replace sim to uim
        changeSimTitle();

        //Aurora <hujianwei> <2015-07-15> modify for remove SIM manager begin
       // root.removePreference(root.findPreference(KEY_SIM_LOCK));
        // Do not display SIM lock for devices without an Icc card
        TelephonyManager tm = TelephonyManager.getDefault();
        if (!mIsPrimary || !isSimIccReady() || Utils.isWifiOnly(getActivity())) {
            root.removePreference(root.findPreference(KEY_SIM_LOCK));
        } else {
            // Disable SIM lock if there is no ready SIM card.
            root.findPreference(KEY_SIM_LOCK).setEnabled(isSimReady());
        }*/
        if (Settings.System.getInt(getContentResolver(),
                Settings.System.LOCK_TO_APP_ENABLED, 0) != 0) {
            root.findPreference(KEY_SCREEN_PINNING).setSummary(
                    getResources().getString(R.string.switch_on_text));
        }

        AuroraPreferenceGroup simLockCategory = (AuroraPreferenceGroup) root.findPreference(KEY_SIM_LOCK);
        if (GnTelephonyManager.isMultiSimEnabled()) {
            simLockCategory.removePreference(root.findPreference(SINGLE_SIM_LOCK_KEY));
            GnTelephonyManager tm = GnTelephonyManager.getDefault();
            int numPhones = tm.getPhoneCount();
            boolean disableLock = true;
            boolean removeLock = true;
            int length = dual_sim_lock_key.length;
            boolean haseSim = false;
            boolean isRemoved = false;
            int simFlag = -1;
            for (int i = 0; i < numPhones; i++) {
                // Do not display SIM lock for devices without an Icc card
                if (i < length) {
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
                    if (i < length && !disableLock) {
                        root.findPreference(dual_sim_lock_key[i]).setEnabled(true);
                    }
                    haseSim = true;
                    simFlag = i;
                } else {
                    simLockCategory.removePreference(root.findPreference(dual_sim_lock_key[i]));
                    isRemoved = true;
                }
                if (haseSim && isRemoved && simFlag != -1) {
                    root.findPreference(dual_sim_lock_key[simFlag]).setTitle(R.string.sim_lock_settings_category);
                    root.findPreference(dual_sim_lock_key[simFlag]).getIntent().putExtra("sim_number", 1);
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
            for (String simLockKey : dual_sim_lock_key) {
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
        //Aurora <hujianwei> <2015-07-24> modify for remove SIM manager end

        // Show password
        mShowPassword = (AuroraSwitchPreference) root.findPreference(KEY_SHOW_PASSWORD);
        mResetCredentials = root.findPreference(KEY_RESET_CREDENTIALS);

        // Credential storage
        final UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        mKeyStore = KeyStore.getInstance(); // needs to be initialized for onResume()
        if (!um.hasUserRestriction(UserManager.DISALLOW_CONFIG_CREDENTIALS)) {
            AuroraPreference credentialStorageType = root.findPreference(KEY_CREDENTIAL_STORAGE_TYPE);

            final int storageSummaryRes =
                    mKeyStore.isHardwareBacked() ? R.string.credential_storage_type_hardware
                            : R.string.credential_storage_type_software;
            credentialStorageType.setSummary(storageSummaryRes);

            mResetCredentials = root.findPreference(KEY_RESET_CREDENTIALS);
        } else {
            AuroraPreferenceGroup credentialsManager = (AuroraPreferenceGroup)
                    root.findPreference(KEY_CREDENTIALS_MANAGER);
            credentialsManager.removePreference(root.findPreference(KEY_RESET_CREDENTIALS));
            credentialsManager.removePreference(root.findPreference(KEY_CREDENTIALS_INSTALL));
            credentialsManager.removePreference(root.findPreference(KEY_CREDENTIAL_STORAGE_TYPE));
        }

        // Application install
        AuroraPreferenceGroup deviceAdminCategory = (AuroraPreferenceGroup)
                root.findPreference(KEY_DEVICE_ADMIN_CATEGORY);
        mToggleAppInstallation = (AuroraSwitchPreference) findPreference(
                KEY_TOGGLE_INSTALL_APPLICATIONS);
        mToggleAppInstallation.setChecked(isNonMarketAppsAllowed());
        // Side loading of apps.
        // Disable for restricted profiles. For others, check if policy disallows it.
        mToggleAppInstallation.setEnabled(!um.getUserInfo(UserHandle.myUserId()).isRestricted());
        if (um.hasUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)
                || um.hasUserRestriction(UserManager.DISALLOW_INSTALL_APPS)) {
            mToggleAppInstallation.setEnabled(false);
        }

        // Advanced Security features
        AuroraPreferenceGroup advancedCategory =
                (AuroraPreferenceGroup) root.findPreference(KEY_ADVANCED_SECURITY);
        if (advancedCategory != null) {
            AuroraPreference manageAgents = advancedCategory.findPreference(KEY_MANAGE_TRUST_AGENTS);
            if (manageAgents != null && !mLockPatternUtils.isSecure()) {
                manageAgents.setEnabled(false);
                manageAgents.setSummary(R.string.disabled_because_no_backup_security);
            }
            //advancedCategory.removePreference()
            //--------------change "advanced_security" from by JM begin--------------------
            //getPreferenceScreen().removePreference(advancedCategory);
            //--------------change "advanced_security" from by JM end--------------------
        }

        // The above preferences come and go based on security state, so we need to update
        // the index. This call is expected to be fairly cheap, but we may want to do something
        // smarter in the future.
        Index.getInstance(getActivity())
                .updateFromClassNameResource(SecuritySettings.class.getName(), true, true);

        for (int i = 0; i < SWITCH_PREFERENCE_KEYS.length; i++) {
            final AuroraPreference pref = findPreference(SWITCH_PREFERENCE_KEYS[i]);
            if (pref != null) pref.setOnPreferenceChangeListener(this);
        }

        ///M: Add MTK Security features. @ { */
        //modify by jiyouguang 2015 06 18
        /*mPermCtrlExt.addAutoBootPrf(deviceAdminCategory);
        mPermCtrlExt.addPermSwitchPrf(deviceAdminCategory);
        mPermCtrlExt.enablerResume();
        mDataProectExt.addDataPrf(deviceAdminCategory);
        mPplExt.addPplPrf(deviceAdminCategory);
        mPplExt.enablerResume();
        mMdmPermCtrlExt.addMdmPermCtrlPrf(deviceAdminCategory);
        mMdmPermCtrlExt.enablerResume();*/
        //end 
        /* @ } */

        ///M: Add for MTK-Mutil-Storage. @ { */
        AuroraPreference credentialInstall = root.findPreference(KEY_CREDENTIALS_INSTALL);
        String description = UtilsExt.getVolumeDescription(getActivity());
        String title = UtilsExt.getVolumeString(R.string.credentials_install, description, getActivity());
        String summary = UtilsExt.getVolumeString(R.string.credentials_install_summary, description, getActivity());
        credentialInstall.setTitle(title);
        credentialInstall.setSummary(summary);
        /* @ } */

        //--------------modify by aurora--------------------
        AuroraPreferenceGroup credentialsManager = (AuroraPreferenceGroup)
                root.findPreference(KEY_CREDENTIALS_MANAGER);
        getPreferenceScreen().removePreference(credentialsManager);
        //--------------end --------------------
        return root;
    }

    /**
     * M: Replace SIM to SIM/UIM.
     */
    private void changeSimTitle() {
        findPreference(KEY_SIM_LOCK).setTitle(
                mExt.customizeSimDisplayString(
                        findPreference(KEY_SIM_LOCK).getTitle().toString(),
                        SubscriptionManager.INVALID_SUBSCRIPTION_ID));
        findPreference("sim_lock_settings").setTitle(
                mExt.customizeSimDisplayString(
                        findPreference("sim_lock_settings").getTitle().toString(),
                        SubscriptionManager.INVALID_SUBSCRIPTION_ID));
    }

    /* Return true if a there is a Slot that has Icc.
     */
    private boolean isSimIccReady() {
        TelephonyManager tm = TelephonyManager.getDefault();
        final List<SubscriptionInfo> subInfoList =
                mSubscriptionManager.getActiveSubscriptionInfoList();

        if (subInfoList != null) {
            for (SubscriptionInfo subInfo : subInfoList) {
                if (tm.hasIccCard(subInfo.getSimSlotIndex())) {
                    return true;
                }
            }
        }

        return false;
    }

    /* Return true if a SIM is ready for locking.
     * TODO: consider adding to TelephonyManager or SubscritpionManasger.
     */
    private boolean isSimReady() {
        int simState = TelephonyManager.SIM_STATE_UNKNOWN;
        final List<SubscriptionInfo> subInfoList =
                mSubscriptionManager.getActiveSubscriptionInfoList();
        if (subInfoList != null) {
            for (SubscriptionInfo subInfo : subInfoList) {
                simState = TelephonyManager.getDefault().getSimState(subInfo.getSimSlotIndex());
                if ((simState != TelephonyManager.SIM_STATE_ABSENT) &&
                        (simState != TelephonyManager.SIM_STATE_UNKNOWN)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static ArrayList<TrustAgentComponentInfo> getActiveTrustAgents(
            PackageManager pm, LockPatternUtils utils) {
        ArrayList<TrustAgentComponentInfo> result = new ArrayList<TrustAgentComponentInfo>();
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(TRUST_AGENT_INTENT,
                PackageManager.GET_META_DATA);
        List<ComponentName> enabledTrustAgents = utils.getEnabledTrustAgents();
        if (enabledTrustAgents != null && !enabledTrustAgents.isEmpty()) {
            for (int i = 0; i < resolveInfos.size(); i++) {
                ResolveInfo resolveInfo = resolveInfos.get(i);
                if (resolveInfo.serviceInfo == null) continue;
                if (!TrustAgentUtils.checkProvidePermission(resolveInfo, pm)) continue;
                TrustAgentComponentInfo trustAgentComponentInfo =
                        TrustAgentUtils.getSettingsComponent(pm, resolveInfo);
                if (trustAgentComponentInfo.componentName == null ||
                        !enabledTrustAgents.contains(
                                TrustAgentUtils.getComponentName(resolveInfo)) ||
                        TextUtils.isEmpty(trustAgentComponentInfo.title)) continue;
                result.add(trustAgentComponentInfo);
                if (ONLY_ONE_TRUST_AGENT) break;
            }
        }
        return result;
    }

    private boolean isNonMarketAppsAllowed() {
        return Settings.Global.getInt(getContentResolver(),
                Settings.Global.INSTALL_NON_MARKET_APPS, 0) > 0;
    }

    private void setNonMarketAppsAllowed(boolean enabled) {
        //去掉多用户管理
      /*  final UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)) {
            return;
        }*/
        // Change the system setting
        Settings.Global.putInt(getContentResolver(), Settings.Global.INSTALL_NON_MARKET_APPS,
                enabled ? 1 : 0);
    }

    private void warnAppInstallation() {
        //=====modify by jiyouguang ===============
        if (mWarnInstallApps != null && mWarnInstallApps.isShowing()) {
            return;
        }
        // TODO: DialogFragment?
        mWarnInstallApps = new AuroraAlertDialog.Builder(getActivity()).setTitle(
                getResources().getString(R.string.error_title))
               /* .setIcon(com.android.internal.R.drawable.ic_dialog_alert)*/
                .setMessage(getResources().getString(R.string.install_all_warning))
                .setPositiveButton(android.R.string.yes, this)
                .setNegativeButton(android.R.string.no, this)
                .setTitle(R.string.remind)
                .show();
        mWarnInstallApps.setOnKeyListener(this);
        //=======end ===================
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            mToggleAppInstallation.setChecked(false);
        }

        return false;
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mWarnInstallApps) {
            boolean turnOn = which == DialogInterface.BUTTON_POSITIVE;
            setNonMarketAppsAllowed(turnOn);
            //modify by jiyouguang
            // if (mToggleAppInstallation != null) {
            // mToggleAppInstallation.setChecked(turnOn);
            // }
            if (!turnOn) {
                mToggleAppInstallation.setChecked(false);
            }
            //end
            mWarnInstallApps.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWarnInstallApps != null) {
            mWarnInstallApps.dismiss();
        }
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

        AuroraPreference preference = getPreferenceScreen().findPreference(KEY_TRUST_AGENT);
        if (preference != null && preference.getTitle().length() > 0) {
            mLockAfter.setSummary(getString(R.string.lock_after_timeout_summary_with_exception,
                    entries[best], preference.getTitle()));
        } else {
            mLockAfter.setSummary(getString(R.string.lock_after_timeout_summary, entries[best]));
        }
        mLockAfter.setValue(String.valueOf(currentTimeout));
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mTrustAgentClickIntent != null) {
            outState.putParcelable(TRUST_AGENT_CLICK_INTENT, mTrustAgentClickIntent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make sure we reload the preference hierarchy since some of these settings
        // depend on others...
        createPreferenceHierarchy();

        /**M: scroll the screen for "Unknown Sources" @ {*/
        if (mScrollToUnknownSources) {
            mScrollToUnknownSources = false;
            // reset the position as 0 and find the position of the "unknownSource"
            mUnknownSourcesPosition = 0;
            findPreferencePosition(KEY_TOGGLE_INSTALL_APPLICATIONS, getPreferenceScreen());
            mScrollHandler.postDelayed(mScrollRunner, DELAY_MILLIS);
        }
        /** @}*/

        final LockPatternUtils lockPatternUtils = mChooseLockSettingsHelper.utils();
        if (mBiometricWeakLiveliness != null) {
            mBiometricWeakLiveliness.setChecked(
                    lockPatternUtils.isBiometricWeakLivelinessEnabled());
        }
        if (mVisiblePattern != null) {
            mVisiblePattern.setChecked(lockPatternUtils.isVisiblePatternEnabled());
        }

        //Aurora <hujianwei> <2015-07-16> modify for remove mPowerButtonInstantlyLocks begin
        /*
        if (mPowerButtonInstantlyLocks != null) {
        	Log.d(TAG, "setChecked:" + lockPatternUtils.getPowerButtonInstantlyLocks());
            mPowerButtonInstantlyLocks.setChecked(lockPatternUtils.getPowerButtonInstantlyLocks());
        }
        */
        //Aurora <hujianwei> <2015-07-16> modify for remove mPowerButtonInstantlyLocks end

        if (mShowPassword != null) {
            mShowPassword.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.TEXT_SHOW_PASSWORD, 1) != 0);
        }

        if (mResetCredentials != null) {
            mResetCredentials.setEnabled(!mKeyStore.isEmpty());
        }
        
        if(!AuroraFingerprintUtils.isFingerPrintSupported()){
        	AuroraActionBar actionBar = ((AuroraActivity)getActivity()).getAuroraActionBar();
        	if(actionBar != null){
        		actionBar.setTitle(R.string.security);
        	}
        }
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        final String key = preference.getKey();
        if (KEY_UNLOCK_SET_OR_CHANGE.equals(key)) {
            startFragment(this, "com.android.settings.ChooseLockGeneric$ChooseLockGenericFragment",
                    R.string.lock_settings_picker_title, SET_OR_CHANGE_LOCK_METHOD_REQUEST, null);
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
        } else if (KEY_TRUST_AGENT.equals(key)) {
            ChooseLockSettingsHelper helper =
                    new ChooseLockSettingsHelper(this.getActivity(), this);
            mTrustAgentClickIntent = preference.getIntent();
            if (!helper.launchConfirmationActivity(CHANGE_TRUST_AGENT_SETTINGS, null, null) &&
                    mTrustAgentClickIntent != null) {
                // If this returns false, it means no password confirmation is required.
                startActivity(mTrustAgentClickIntent);
                mTrustAgentClickIntent = null;
            }
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        return true;
    }

    /**
     * see confirmPatternThenDisableAndClear
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONFIRM_EXISTING_FOR_BIOMETRIC_WEAK_IMPROVE_REQUEST &&
                resultCode == Activity.RESULT_OK) {
            startBiometricWeakImprove();
            return;
        } else if (requestCode == CONFIRM_EXISTING_FOR_BIOMETRIC_WEAK_LIVELINESS_OFF &&
                resultCode == Activity.RESULT_OK) {
            final LockPatternUtils lockPatternUtils = mChooseLockSettingsHelper.utils();
            lockPatternUtils.setBiometricWeakLivelinessEnabled(false);
            // Setting the mBiometricWeakLiveliness checked value to false is handled when onResume
            // is called by grabbing the value from lockPatternUtils.  We can't set it here
            // because mBiometricWeakLiveliness could be null
            return;
        } else if (requestCode == CHANGE_TRUST_AGENT_SETTINGS && resultCode == Activity.RESULT_OK) {
            if (mTrustAgentClickIntent != null) {
                startActivity(mTrustAgentClickIntent);
                mTrustAgentClickIntent = null;
            }
            return;
        }
        createPreferenceHierarchy();
    }

    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object value) {
        boolean result = true;
        final String key = preference.getKey();
        final LockPatternUtils lockPatternUtils = mChooseLockSettingsHelper.utils();
        if (KEY_LOCK_AFTER_TIMEOUT.equals(key)) {
            int timeout = Integer.parseInt((String) value);
            try {
                Settings.Secure.putInt(getContentResolver(),
                        Settings.Secure.LOCK_SCREEN_LOCK_AFTER_TIMEOUT, timeout);
            } catch (NumberFormatException e) {
                Log.e("SecuritySettings", "could not persist lockAfter timeout setting", e);
            }
            updateLockAfterPreferenceSummary();
        } else if (KEY_LOCK_ENABLED.equals(key)) {
            lockPatternUtils.setLockPatternEnabled((Boolean) value);
        } else if (KEY_VISIBLE_PATTERN.equals(key)) {
            lockPatternUtils.setVisiblePatternEnabled((Boolean) value);
        } else if (KEY_BIOMETRIC_WEAK_LIVELINESS.equals(key)) {
            if ((Boolean) value) {
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
        } else if (KEY_POWER_INSTANTLY_LOCKS.equals(key)) {
            mLockPatternUtils.setPowerButtonInstantlyLocks((Boolean) value);
        } else if (KEY_SHOW_PASSWORD.equals(key)) {
            Settings.System.putInt(getContentResolver(), Settings.System.TEXT_SHOW_PASSWORD,
                    ((Boolean) value) ? 1 : 0);
        } else if (KEY_TOGGLE_INSTALL_APPLICATIONS.equals(key)) {
            if ((Boolean) value) {
                //modify by jiyouguang
                // mToggleAppInstallation.setChecked(false);
                warnAppInstallation();
                // Don't change Switch status until user makes choice in dialog, so return false.
                // result = false;
                //end
            } else {
                setNonMarketAppsAllowed(false);
            }
        }
        return result;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_url_security;
    }

    public void startBiometricWeakImprove() {
        Intent intent = new Intent();
        intent.setClassName("com.android.facelock", "com.android.facelock.AddToSetup");
        startActivity(intent);
    }

    /**
     * For Search. Please keep it in sync when updating "createPreferenceHierarchy()"
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new SecuritySearchIndexProvider();

    private static class SecuritySearchIndexProvider extends BaseSearchIndexProvider {

        boolean mIsPrimary;

        public SecuritySearchIndexProvider() {
            super();

            mIsPrimary = UserHandle.myUserId() == UserHandle.USER_OWNER;
        }

        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(
                Context context, boolean enabled) {

            List<SearchIndexableResource> result = new ArrayList<SearchIndexableResource>();

            LockPatternUtils lockPatternUtils = new LockPatternUtils(context);
            // Add options for lock/unlock screen
            int resId = getResIdForLockUnlockScreen(context, lockPatternUtils);

            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = resId;
            result.add(sir);

            if (mIsPrimary) {
                DevicePolicyManager dpm = (DevicePolicyManager)
                        context.getSystemService(Context.DEVICE_POLICY_SERVICE);

                switch (dpm.getStorageEncryptionStatus()) {
                    case DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE:
                        // The device is currently encrypted.
                        resId = R.xml.security_settings_encrypted;
                        break;
                    case DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE:
                        // This device supports encryption but isn't encrypted.
                        resId = R.xml.security_settings_unencrypted;
                        break;
                }

                sir = new SearchIndexableResource(context);
                sir.xmlResId = resId;
                //--------------change "security_category" from by JM begin---------------------
                result.add(sir);
                //--------------change "security_category" from by JM end---------------------
            }

            // Append the rest of the settings
            sir = new SearchIndexableResource(context);

            if (AuroraFingerprintUtils.isFingerPrintSupported()) {
                sir.xmlResId = R.xml.security_settings_misc_finger;
            } else {
                sir.xmlResId = R.xml.security_settings_misc_normal;
            }
            result.add(sir);

            return result;
        }

        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            final List<SearchIndexableRaw> result = new ArrayList<SearchIndexableRaw>();
            final Resources res = context.getResources();

            final String screenTitle;
            if (AuroraFingerprintUtils.isFingerPrintSupported()) {
                screenTitle = res.getString(R.string.security_settings_title);
            } else {
                screenTitle = res.getString(R.string.security);
            }
            Log.e(TAG, "screenTitle = " + screenTitle);

            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            result.add(data);

            if (!mIsPrimary) {
                int resId = (UserManager.get(context).isLinkedUser()) ?
                        R.string.profile_info_settings_title : R.string.user_info_settings_title;
                data = new SearchIndexableRaw(context);
                data.title = res.getString(resId);
                data.screenTitle = screenTitle;
                result.add(data);
            }

            // Credential storage
            final UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);

            if (!um.hasUserRestriction(UserManager.DISALLOW_CONFIG_CREDENTIALS)) {
                KeyStore keyStore = KeyStore.getInstance();

                final int storageSummaryRes = keyStore.isHardwareBacked() ?
                        R.string.credential_storage_type_hardware :
                        R.string.credential_storage_type_software;

                data = new SearchIndexableRaw(context);
                data.title = res.getString(storageSummaryRes);
                data.screenTitle = screenTitle;
                result.add(data);
            }

            // Advanced
            final LockPatternUtils lockPatternUtils = new LockPatternUtils(context);
            if (lockPatternUtils.isSecure()) {
                ArrayList<TrustAgentComponentInfo> agents =
                        getActiveTrustAgents(context.getPackageManager(), lockPatternUtils);
                for (int i = 0; i < agents.size(); i++) {
                    final TrustAgentComponentInfo agent = agents.get(i);
                    data = new SearchIndexableRaw(context);
                    data.title = agent.title;
                    data.screenTitle = screenTitle;
                    result.add(data);
                }
            }

            ///M: Add search index for PermCtrl {@.
            if (mPermCtrlExt == null) {
                Log.d(TAG, "mPermCtrlExt init firstly");
                mPermCtrlExt = (IPermissionControlExt) UtilsExt.getPermControlExtPlugin(context);
            }
            List<SearchIndexableData> permList = mPermCtrlExt.getRawDataToIndex(enabled);
            Log.d(TAG, "permList = " + permList);
            // if not enter security never, will null
            if (permList != null) {
                for (SearchIndexableData permdata : permList) {
                    Log.d(TAG, "add perm data ");
                    SearchIndexableRaw indexablePerm = new SearchIndexableRaw(
                            context);
                    String orign = permdata.toString();
                    String title = orign.substring(orign.indexOf("title:")
                            + ("title:").length());
                    Log.d(TAG, " title = " + title);
                    indexablePerm.title = title;
                    indexablePerm.intentAction = "com.mediatek.security.PERMISSION_CONTROL";
                    result.add(indexablePerm);
                }
            }
            /// @}
            return result;
        }

        @Override
        public List<String> getNonIndexableKeys(Context context) {
            final List<String> keys = new ArrayList<String>();

            LockPatternUtils lockPatternUtils = new LockPatternUtils(context);
            // Add options for lock/unlock screen
            int resId = getResIdForLockUnlockScreen(context, lockPatternUtils);

            // don't display visible pattern if biometric and backup is not pattern
            if (resId == R.xml.security_settings_biometric_weak &&
                    lockPatternUtils.getKeyguardStoredPasswordQuality() !=
                            DevicePolicyManager.PASSWORD_QUALITY_SOMETHING) {
                keys.add(KEY_VISIBLE_PATTERN);
            }

            // Do not display SIM lock for devices without an Icc card
            if (!mIsPrimary
                    || SubscriptionManager.from(context).getActiveSubscriptionInfoCount() == 0
                    || Utils.isWifiOnly(context)) {
                keys.add(KEY_SIM_LOCK);
            }

            final UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
            if (um.hasUserRestriction(UserManager.DISALLOW_CONFIG_CREDENTIALS)) {
                keys.add(KEY_CREDENTIALS_MANAGER);
            }

            // TrustAgent settings disappear when the user has no primary security.
            if (!lockPatternUtils.isSecure()) {
                keys.add(KEY_TRUST_AGENT);
                keys.add(KEY_MANAGE_TRUST_AGENTS);
            }

            return keys;
        }
    }

    ///M:
    @Override
    public void onPause() {
        super.onPause();
        //add by jiyouguang
        if (mWarnInstallApps != null) {
            mWarnInstallApps.dismiss();
        }
        //end
        mPermCtrlExt.enablerPause();
        mPplExt.enablerPause();
        mMdmPermCtrlExt.enablerPause();
    }

    ///M: Add for Scroll to speciall position.
    private AuroraPreference findPreferencePosition(CharSequence key, AuroraPreferenceGroup root) {
        if (key.equals(root.getKey())) {
            return root;
        } else {
            // if the PreferenceGroup is not preference to find,then count + 1.
            mUnknownSourcesPosition++;
        }
        final int preferenceCount = root.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            final AuroraPreference preference = root.getPreference(i);
            final String curKey = preference.getKey();
            if (curKey != null && curKey.equals(key)) {
                return preference;
            }

            if (preference instanceof AuroraPreferenceGroup) {
                AuroraPreferenceGroup group = (AuroraPreferenceGroup) preference;
                final AuroraPreference returnedPreference = findPreferencePosition(key, group);
                if (returnedPreference != null) {
                    return returnedPreference;
                }
            } else {
                // if the Preference is not preference to find,then count + 1.
                mUnknownSourcesPosition++;
            }
        }

        return null;
    }

}
