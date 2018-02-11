/*
 * 2012.11.06 Gionee guoyx added for CR00725060
 * 
 */

package com.android.settings;

import android.app.ActivityManagerNative;
import aurora.app.AuroraAlertDialog;
import android.app.Dialog;
import android.app.backup.IBackupManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.VerifierDeviceIdentity;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemProperties;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceFragment;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.IWindowManager;

//gionee wangyy 20120714 modify for CR00643075 begin
import android.util.Log;


//gionee wangyy 20120714 modify for CR00643075 end
import com.android.settings.R;


//gionee zhanglina 20121011 add for CR00710931 begin
import android.os.SystemProperties;
//gionee zhanglina 20121011 add for CR00710931 end

import android.media.AudioManager;

//Aurora <steveTang> <2014-06-04> modify begin
import android.os.PowerManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import dalvik.system.VMRuntime;
import java.io.File;
import android.os.AsyncTask;
//Aurora <steveTang> <2014-06-04> modify end

/*
 * Displays preferences for application developers. This is the copy from DevelopmentSettings, 
 * but make so much different from DevelopmentSettings, just leave only two functions for User.
 * ADB debug and device identifier to show. 
 */
public class DevelopmentSettingsUserMode extends AuroraPreferenceFragment
        implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener,
                OnPreferenceChangeListener {

    private static final String ENABLE_ADB = "enable_adb";

	//Aurora <steveTang> <2014-06-04> modify begin
	private static final String SELECT_RUNTIME_PROPERTY = "persist.sys.dalvik.vm.lib";
	private static final String RUNTIME_DVM_MODE = "libdvm.so";
	private static final String RUNTIME_ART_MODE = "libart.so";
    private static final String ENABLE_ART = "enable_art";
    private AuroraSwitchPreference mEnableArt;
	//Aurora <steveTang> <2014-06-04> modify end

    private static final String VERIFIER_DEVICE_IDENTIFIER = "verifier_device_identifier";
//    private static final String KEEP_SCREEN_ON = "keep_screen_on";
//    private static final String ALLOW_MOCK_LOCATION = "allow_mock_location";
//    private static final String HDCP_CHECKING_KEY = "hdcp_checking";
//    private static final String HDCP_CHECKING_PROPERTY = "persist.sys.hdcp_checking";
//    private static final String LOCAL_BACKUP_PASSWORD = "local_backup_password";
//    private static final String HARDWARE_UI_PROPERTY = "persist.sys.ui.hw";
//
//    private static final String STRICT_MODE_KEY = "strict_mode";
//    private static final String POINTER_LOCATION_KEY = "pointer_location";
//    private static final String SHOW_TOUCHES_KEY = "show_touches";
//    private static final String SHOW_SCREEN_UPDATES_KEY = "show_screen_updates";
//    private static final String SHOW_CPU_USAGE_KEY = "show_cpu_usage";
//    private static final String FORCE_HARDWARE_UI_KEY = "force_hw_ui";
//    private static final String WINDOW_ANIMATION_SCALE_KEY = "window_animation_scale";
//    private static final String TRANSITION_ANIMATION_SCALE_KEY = "transition_animation_scale";
//
//    private static final String IMMEDIATELY_DESTROY_ACTIVITIES_KEY
//            = "immediately_destroy_activities";
//    private static final String APP_PROCESS_LIMIT_KEY = "app_process_limit";
//
//    private static final String SHOW_ALL_ANRS_KEY = "show_all_anrs";

    private IWindowManager mWindowManager;
    private IBackupManager mBackupManager;

    private AuroraSwitchPreference mEnableAdb;
//    private AuroraSwitchPreference mEnableDTS;
//    private CheckBoxPreference mKeepScreenOn;
//    private CheckBoxPreference mAllowMockLocation;
//    private PreferenceScreen mPassword;
//
//    private CheckBoxPreference mStrictMode;
//    private CheckBoxPreference mPointerLocation;
//    private CheckBoxPreference mShowTouches;
//    private CheckBoxPreference mShowScreenUpdates;
//    private CheckBoxPreference mShowCpuUsage;
//    private CheckBoxPreference mForceHardwareUi;
//    private ListPreference mWindowAnimationScale;
//    private ListPreference mTransitionAnimationScale;
//
//    private CheckBoxPreference mImmediatelyDestroyActivities;
//    private ListPreference mAppProcessLimit;
//
//    private CheckBoxPreference mShowAllANRs;

    // To track whether Yes was clicked in the adb warning dialog
    private boolean mOkClicked;
    //gionee wangyy 20120710 modify for CR00643075 begin
    private static final boolean mGNUsbUISupport = SystemProperties.get("ro.gn.usb.ui.support").equals("yes");
    //gionee wangyy 20120710 modify for CR00643075 end

//	//gionee zhanglina 20121011 add for CR00710931 begin
//    private static final boolean gnAIflag = SystemProperties.get("ro.gn.oversea.custom").equals("AIRIS");
//    //gionee zhanglina 20121011 add for CR00710931 end
    private Dialog mOkDialog;
    private Dialog mArtDialog;
//    private AudioManager mAudioMan;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        mBackupManager = IBackupManager.Stub.asInterface(
                ServiceManager.getService(Context.BACKUP_SERVICE));

        addPreferencesFromResource(R.xml.development_prefs_user_mode);

        mEnableAdb = (AuroraSwitchPreference) findPreference(ENABLE_ADB);
        mEnableAdb.setOnPreferenceChangeListener(this);


		//Aurora <steveTang> <2014-06-04> modify begin
		mEnableArt = (AuroraSwitchPreference) findPreference(ENABLE_ART);
        mEnableArt.setOnPreferenceChangeListener(this);
		filterRuntimeOptions(mEnableArt);
		updateRuntimeValue();
		//Aurora <steveTang> <2014-06-04> modify end

        AuroraPreferenceCategory prefC = (AuroraPreferenceCategory)findPreference("usb_adb");
        getPreferenceScreen().removePreference(prefC);

       /* mEnableDTS = (AuroraSwitchPreference)findPreference("enable_dts");
        mAudioMan = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
        if (mEnableDTS != null && mAudioMan != null) {
            mEnableDTS.setOnPreferenceChangeListener(this);

            String inParams = mAudioMan.getParameters("srs_cfg:trumedia_enable");
            Log.v("xiaoyong", "inParams = " + inParams);
            if (inParams.contains("1")) {
                mEnableDTS.setChecked(true);
                mAudioMan.setParameters("srs_cfg:trumedia_enable=1;srs_cfg:trumedia_preset=0");
            } else {
                mEnableDTS.setChecked(false);
                mAudioMan.setParameters("srs_cfg:trumedia_enable=0");
            }
        }
        
        String buildModel = Build.MODEL;
        if (!buildModel.contains("U810")) {
            Log.v("xiaoyong", "productname is " + buildModel);
            getPreferenceScreen().removePreference(mEnableDTS); 
        }*/
        
//        mKeepScreenOn = (CheckBoxPreference) findPreference(KEEP_SCREEN_ON);
//        mAllowMockLocation = (CheckBoxPreference) findPreference(ALLOW_MOCK_LOCATION);
//        mPassword = (PreferenceScreen) findPreference(LOCAL_BACKUP_PASSWORD);
//
//        mStrictMode = (CheckBoxPreference) findPreference(STRICT_MODE_KEY);
//        mPointerLocation = (CheckBoxPreference) findPreference(POINTER_LOCATION_KEY);
//        mShowTouches = (CheckBoxPreference) findPreference(SHOW_TOUCHES_KEY);
//        mShowScreenUpdates = (CheckBoxPreference) findPreference(SHOW_SCREEN_UPDATES_KEY);
//        mShowCpuUsage = (CheckBoxPreference) findPreference(SHOW_CPU_USAGE_KEY);
//        mForceHardwareUi = (CheckBoxPreference) findPreference(FORCE_HARDWARE_UI_KEY);
//        mWindowAnimationScale = (ListPreference) findPreference(WINDOW_ANIMATION_SCALE_KEY);
//        mWindowAnimationScale.setOnPreferenceChangeListener(this);
//        mTransitionAnimationScale = (ListPreference) findPreference(TRANSITION_ANIMATION_SCALE_KEY);
//        mTransitionAnimationScale.setOnPreferenceChangeListener(this);
//
//        mImmediatelyDestroyActivities = (CheckBoxPreference) findPreference(
//                IMMEDIATELY_DESTROY_ACTIVITIES_KEY);
//        mAppProcessLimit = (ListPreference) findPreference(APP_PROCESS_LIMIT_KEY);
//        mAppProcessLimit.setOnPreferenceChangeListener(this);
//
//        mShowAllANRs = (CheckBoxPreference) findPreference(
//                SHOW_ALL_ANRS_KEY);
//Android 4.1 not show this item
//        final Preference verifierDeviceIdentifier = findPreference(VERIFIER_DEVICE_IDENTIFIER);
//        final PackageManager pm = getActivity().getPackageManager();
//        final VerifierDeviceIdentity verifierIndentity = pm.getVerifierDeviceIdentity();
//        if (verifierIndentity != null) {
//            verifierDeviceIdentifier.setSummary(verifierIndentity.toString());
//        }

//        removeHdcpOptionsForProduction();
    }

//    private void removeHdcpOptionsForProduction() {
//        if ("user".equals(Build.TYPE)) {
//            Preference hdcpChecking = findPreference(HDCP_CHECKING_KEY);
//            if (hdcpChecking != null) {
//                // Remove the preference
//                getPreferenceScreen().removePreference(hdcpChecking);
//            }
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();

        final ContentResolver cr = getActivity().getContentResolver();
     // qy modify 2014 05 08 begig
		/*if(mOkDialog!=null && mOkDialog.isShowing()) {
		    mEnableAdb.setChecked(true);
		    
		}
		else {*/
        
		    mEnableAdb.setChecked(Settings.Secure.getInt(cr,
	                Settings.Secure.ADB_ENABLED, 0) != 0);
		    if (mGNUsbUISupport) {
    			
    			mEnableAdb.setChecked(Settings.Secure.getInt(cr,
    					"real_debug_state",0) != 0);
            }
		    
//		}	
	    // qy modify 2014 05 08 end
		    
//        mKeepScreenOn.setChecked(Settings.System.getInt(cr,
//                Settings.System.STAY_ON_WHILE_PLUGGED_IN, 0) != 0);
//		//GIONEE: zhanglina 20121016 modify for airis demand begin
//		if(gnAIflag == true){
//			mAllowMockLocation.setChecked(true);
//			}
//		else{
//        mAllowMockLocation.setChecked(Settings.Secure.getInt(cr,
//                Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0);
//			}
//		//GIONEE: zhanglina 20121016 modify for airis demand end
//        updateHdcpValues();
//        updatePasswordSummary();
//        updateStrictModeVisualOptions();
//        updatePointerLocationOptions();
//        updateShowTouchesOptions();
//        updateFlingerOptions();
//        updateCpuUsageOptions();
//        updateHardwareUiOptions();
//        updateAnimationScaleOptions();
//        updateImmediatelyDestroyActivitiesOptions();
//        updateAppProcessLimitOptions();
//        updateShowAllANRsOptions();
    }

//    private void updateHdcpValues() {
//        int index = 1; // Defaults to drm-only. Needs to match with R.array.hdcp_checking_values
//        ListPreference hdcpChecking = (ListPreference) findPreference(HDCP_CHECKING_KEY);
//        if (hdcpChecking != null) {
//            String currentValue = SystemProperties.get(HDCP_CHECKING_PROPERTY);
//            String[] values = getResources().getStringArray(R.array.hdcp_checking_values);
//            String[] summaries = getResources().getStringArray(R.array.hdcp_checking_summaries);
//            for (int i = 0; i < values.length; i++) {
//                if (currentValue.equals(values[i])) {
//                    index = i;
//                    break;
//                }
//            }
//            hdcpChecking.setValue(values[index]);
//            hdcpChecking.setSummary(summaries[index]);
//            hdcpChecking.setOnPreferenceChangeListener(this);
//        }
//    }
//
//    private void updatePasswordSummary() {
//        try {
//            if (mBackupManager.hasBackupPassword()) {
//                mPassword.setSummary(R.string.local_backup_password_summary_change);
//            } else {
//                mPassword.setSummary(R.string.local_backup_password_summary_none);
//            }
//        } catch (RemoteException e) {
//            // Not much we can do here
//        }
//    }
//
//    // Returns the current state of the system property that controls
//    // strictmode flashes.  One of:
//    //    0: not explicitly set one way or another
//    //    1: on
//    //    2: off
//    private int currentStrictModeActiveIndex() {
//        if (TextUtils.isEmpty(SystemProperties.get(StrictMode.VISUAL_PROPERTY))) {
//            return 0;
//        }
//        boolean enabled = SystemProperties.getBoolean(StrictMode.VISUAL_PROPERTY, false);
//        return enabled ? 1 : 2;
//    }
//
//    private void writeStrictModeVisualOptions() {
//        try {
//            mWindowManager.setStrictModeVisualIndicatorPreference(mStrictMode.isChecked()
//                    ? "1" : "");
//        } catch (RemoteException e) {
//        }
//    }
//
//    private void updateStrictModeVisualOptions() {
//        mStrictMode.setChecked(currentStrictModeActiveIndex() == 1);
//    }
//
//    private void writePointerLocationOptions() {
//        Settings.System.putInt(getActivity().getContentResolver(),
//                Settings.System.POINTER_LOCATION, mPointerLocation.isChecked() ? 1 : 0);
//    }
//
//    private void updatePointerLocationOptions() {
//        mPointerLocation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
//                Settings.System.POINTER_LOCATION, 0) != 0);
//    }
//
//    private void writeShowTouchesOptions() {
//        Settings.System.putInt(getActivity().getContentResolver(),
//                Settings.System.SHOW_TOUCHES, mShowTouches.isChecked() ? 1 : 0);
//    }
//
//    private void updateShowTouchesOptions() {
//        mShowTouches.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
//                Settings.System.SHOW_TOUCHES, 0) != 0);
//    }
//
//    private void updateFlingerOptions() {
//        // magic communication with surface flinger.
//        try {
//            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
//            if (flinger != null) {
//                Parcel data = Parcel.obtain();
//                Parcel reply = Parcel.obtain();
//                data.writeInterfaceToken("android.ui.ISurfaceComposer");
//                flinger.transact(1010, data, reply, 0);
//                @SuppressWarnings("unused")
//                int showCpu = reply.readInt();
//                @SuppressWarnings("unused")
//                int enableGL = reply.readInt();
//                int showUpdates = reply.readInt();
//                mShowScreenUpdates.setChecked(showUpdates != 0);
//                @SuppressWarnings("unused")
//                int showBackground = reply.readInt();
//                reply.recycle();
//                data.recycle();
//            }
//        } catch (RemoteException ex) {
//        }
//    }
//
//    private void writeFlingerOptions() {
//        try {
//            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
//            if (flinger != null) {
//                Parcel data = Parcel.obtain();
//                data.writeInterfaceToken("android.ui.ISurfaceComposer");
//                data.writeInt(mShowScreenUpdates.isChecked() ? 1 : 0);
//                flinger.transact(1002, data, null, 0);
//                data.recycle();
//
//                updateFlingerOptions();
//            }
//        } catch (RemoteException ex) {
//        }
//    }
//
//    private void updateHardwareUiOptions() {
//		//gionee zhanglina 20121011 add for CR00710931 begin
//		if(gnAIflag == true){
//		mForceHardwareUi.setChecked(SystemProperties.getBoolean(HARDWARE_UI_PROPERTY, true));
//			}
//		else{
//        mForceHardwareUi.setChecked(SystemProperties.getBoolean(HARDWARE_UI_PROPERTY, false));
//			}
//		//gionee zhanglina 20121011 add for CR00710931 end
//    }
//    
//    private void writeHardwareUiOptions() {
//        SystemProperties.set(HARDWARE_UI_PROPERTY, mForceHardwareUi.isChecked() ? "true" : "false");
//    }
//
//    private void updateCpuUsageOptions() {
//        mShowCpuUsage.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
//                Settings.System.SHOW_PROCESSES, 0) != 0);
//    }
//    
//    private void writeCpuUsageOptions() {
//        boolean value = mShowCpuUsage.isChecked();
//        Settings.System.putInt(getActivity().getContentResolver(),
//                Settings.System.SHOW_PROCESSES, value ? 1 : 0);
//        Intent service = (new Intent())
//                .setClassName("com.android.systemui", "com.android.systemui.LoadAverageService");
//        if (value) {
//            getActivity().startService(service);
//        } else {
//            getActivity().stopService(service);
//        }
//    }
//
//    private void writeImmediatelyDestroyActivitiesOptions() {
//        try {
//            ActivityManagerNative.getDefault().setAlwaysFinish(
//                    mImmediatelyDestroyActivities.isChecked());
//        } catch (RemoteException ex) {
//        }
//    }
//
//    private void updateImmediatelyDestroyActivitiesOptions() {
//        mImmediatelyDestroyActivities.setChecked(Settings.System.getInt(
//            getActivity().getContentResolver(), Settings.System.ALWAYS_FINISH_ACTIVITIES, 0) != 0);
//    }
//
//    private void updateAnimationScaleValue(int which, ListPreference pref) {
//        try {
//            float scale = mWindowManager.getAnimationScale(which);
//            CharSequence[] values = pref.getEntryValues();
//            for (int i=0; i<values.length; i++) {
//                float val = Float.parseFloat(values[i].toString());
//                if (scale <= val) {
//                    pref.setValueIndex(i);
//                    pref.setSummary(pref.getEntries()[i]);
//                    return;
//                }
//            }
//            pref.setValueIndex(values.length-1);
//            pref.setSummary(pref.getEntries()[0]);
//        } catch (RemoteException e) {
//        }
//    }
//
//    private void updateAnimationScaleOptions() {
//        updateAnimationScaleValue(0, mWindowAnimationScale);
//        updateAnimationScaleValue(1, mTransitionAnimationScale);
//    }
//
//    private void writeAnimationScaleOption(int which, ListPreference pref, Object newValue) {
//        try {
//            float scale = Float.parseFloat(newValue.toString());
//            mWindowManager.setAnimationScale(which, scale);
//            updateAnimationScaleValue(which, pref);
//        } catch (RemoteException e) {
//        }
//    }
//
//    private void updateAppProcessLimitOptions() {
//        try {
//            int limit = ActivityManagerNative.getDefault().getProcessLimit();
//            CharSequence[] values = mAppProcessLimit.getEntryValues();
//            for (int i=0; i<values.length; i++) {
//                int val = Integer.parseInt(values[i].toString());
//                if (val >= limit) {
//                    mAppProcessLimit.setValueIndex(i);
//                    mAppProcessLimit.setSummary(mAppProcessLimit.getEntries()[i]);
//                    return;
//                }
//            }
//            mAppProcessLimit.setValueIndex(0);
//            mAppProcessLimit.setSummary(mAppProcessLimit.getEntries()[0]);
//        } catch (RemoteException e) {
//        }
//    }
//
//    private void writeAppProcessLimitOptions(Object newValue) {
//        try {
//            int limit = Integer.parseInt(newValue.toString());
//            ActivityManagerNative.getDefault().setProcessLimit(limit);
//            updateAppProcessLimitOptions();
//        } catch (RemoteException e) {
//        }
//    }
//
//    private void writeShowAllANRsOptions() {
//        Settings.Secure.putInt(getActivity().getContentResolver(),
//                Settings.Secure.ANR_SHOW_BACKGROUND,
//                mShowAllANRs.isChecked() ? 1 : 0);
//    }
//
//    private void updateShowAllANRsOptions() {
//        mShowAllANRs.setChecked(Settings.Secure.getInt(
//            getActivity().getContentResolver(), Settings.Secure.ANR_SHOW_BACKGROUND, 0) != 0);
//    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {

        if (Utils.isMonkeyRunning()) {
            return false;
        }

        /*if (preference == mEnableAdb) {
            if (mEnableAdb.isChecked()) {
                mOkClicked = false;
                if (mOkDialog != null) dismissDialog();
                //Gionee:zhang_xin 2012-12-18 modify for CR00746738 start
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
                //Gionee:zhang_xin 2012-12-18 modify for CR00746738 end
                mOkDialog.setOnDismissListener(this);
            } else {
                Settings.Secure.putInt(getActivity().getContentResolver(),
                        Settings.Secure.ADB_ENABLED, 0);
                //gionee wangyy 20120714 modify for CR00643075 begin
                if (mGNUsbUISupport) {
                    Settings.Secure.putInt(getActivity().getContentResolver(),
                        "real_debug_state", 0);
                    Log.d("DevelopmentSettings", "onPreferenceTreeClick() set real_debug_state 0");
                }
                //gionee wangyy 20120714 modify for CR00643075 end
            }
        }*/ 
//        else if (preference == mKeepScreenOn) {
//            Settings.System.putInt(getActivity().getContentResolver(),
//                    Settings.System.STAY_ON_WHILE_PLUGGED_IN, 
//                    mKeepScreenOn.isChecked() ? 
//                    (BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB) : 0);
//        } else if (preference == mAllowMockLocation) {
//            Settings.Secure.putInt(getActivity().getContentResolver(),
//                    Settings.Secure.ALLOW_MOCK_LOCATION,
//                    mAllowMockLocation.isChecked() ? 1 : 0);
//        } else if (preference == mStrictMode) {
//            writeStrictModeVisualOptions();
//        } else if (preference == mPointerLocation) {
//            writePointerLocationOptions();
//        } else if (preference == mShowTouches) {
//            writeShowTouchesOptions();
//        } else if (preference == mShowScreenUpdates) {
//            writeFlingerOptions();
//        } else if (preference == mShowCpuUsage) {
//            writeCpuUsageOptions();
//        } else if (preference == mImmediatelyDestroyActivities) {
//            writeImmediatelyDestroyActivitiesOptions();
//        } else if (preference == mShowAllANRs) {
//            writeShowAllANRsOptions();
//        } else if (preference == mForceHardwareUi) {
//            writeHardwareUiOptions();
//        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
//        if (HDCP_CHECKING_KEY.equals(preference.getKey())) {
//            SystemProperties.set(HDCP_CHECKING_PROPERTY, newValue.toString());
//            updateHdcpValues();
//            return true;
//        } else if (preference == mWindowAnimationScale) {
//            writeAnimationScaleOption(0, mWindowAnimationScale, newValue);
//            return true;
//        } else if (preference == mTransitionAnimationScale) {
//            writeAnimationScaleOption(1, mTransitionAnimationScale, newValue);
//            return true;
//        } else if (preference == mAppProcessLimit) {
//            writeAppProcessLimitOptions(newValue);
//            return true;
//        }
    	
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
                //Gionee:zhang_xin 2012-12-18 modify for CR00746738 start
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
                //Gionee:zhang_xin 2012-12-18 modify for CR00746738 end
                mOkDialog.setOnDismissListener(this);
            } else {

                Settings.Secure.putInt(getActivity().getContentResolver(),
                        Settings.Secure.ADB_ENABLED, 0);
                //gionee wangyy 20120714 modify for CR00643075 begin
                if (mGNUsbUISupport) {
                    Settings.Secure.putInt(getActivity().getContentResolver(),
                        "real_debug_state", 0);
                    Log.d("DevelopmentSettings", "onPreferenceTreeClick() set real_debug_state 0");
                }
                //gionee wangyy 20120714 modify for CR00643075 end
            }
          } 
			
		else if (ENABLE_ART.equals(preference.getKey())) {

			android.util.Log.e("haha","Art mode preference changed");

            final String oldRuntimeValue = VMRuntime.getRuntime().vmLibrary();
            final String newRuntimeValue = ((Boolean)newValue) ? RUNTIME_ART_MODE : RUNTIME_DVM_MODE;

			android.util.Log.e("haha","What is the new Value: " + newRuntimeValue + " And What is the old Value: " + oldRuntimeValue);

            if (!newRuntimeValue.equals(oldRuntimeValue)) {

            if (mArtDialog != null) 
            {
            mArtDialog.dismiss();
            mArtDialog = null;
            }

                    mArtDialog = new AuroraAlertDialog.Builder(getActivity(), AuroraAlertDialog.THEME_AMIGO_FULLSCREEN).setMessage(
                            getActivity().getResources().getString(R.string.select_runtime_warning_message,oldRuntimeValue,newRuntimeValue))
                            .setTitle(R.string.art_title)
                            .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SystemProperties.set(SELECT_RUNTIME_PROPERTY, newRuntimeValue);
                        pokeSystemProperties();
                        PowerManager pm = (PowerManager)
                        getActivity().getSystemService(Context.POWER_SERVICE);
                        pm.reboot(null);
                    }
                })
                            .setNegativeButton(android.R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateRuntimeValue();
                    }
                })
                            .show();
                            mArtDialog.setCanceledOnTouchOutside(false);
                            mArtDialog.setOnDismissListener(new OnDismissListener(){
                @Override
                public void onDismiss(DialogInterface dialog) {
                        updateRuntimeValue();
                        }
                            });

/*
                mArtDialog = new AuroraAlertDialog.Builder(getActivity(), AuroraAlertDialog.THEME_AMIGO_FULLSCREEN);
                mArtDialog.setCanceledOnTouchOutside(false);
                mArtDialog.setMessage(context.getResources().getString(R.string.select_runtime_warning_message,
                                                                    oldRuntimeValue, newRuntimeValue));
                mArtDialog.setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SystemProperties.set(SELECT_RUNTIME_PROPERTY, newRuntimeValue);
                        pokeSystemProperties();
                        PowerManager pm = (PowerManager)
                                context.getSystemService(Context.POWER_SERVICE);
                        pm.reboot(null);
                    }
                });
                mArtDialog.setNegativeButton(android.R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateRuntimeValue();
                    }
                });
                mArtDialog.show();
                */
            }
            return true;
        }

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
            //gionee wangyy 20120714 modify for CR00643075 begin
            if (mGNUsbUISupport) {
                Settings.Secure.putInt(getActivity().getContentResolver(),
                    "real_debug_state", 1);
                Log.d("DevelopmentSettings", "onClick() set real_debug_state 1");
            }
            //gionee wangyy 20120714 modify for CR00643075 end
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
    public void onDestroy() {
        dismissDialog();
        super.onDestroy();
    }

    private void filterRuntimeOptions(AuroraPreference selectRuntime) {
		File artModeFile = new File("/system/lib/" + RUNTIME_ART_MODE);
		boolean isSupportArtMode = artModeFile.exists();
		if(!isSupportArtMode){
			 getPreferenceScreen().removePreference(selectRuntime);
		}
    }

    private String currentRuntimeValue() {
        return SystemProperties.get(SELECT_RUNTIME_PROPERTY, VMRuntime.getRuntime().vmLibrary());
    }

    private void updateRuntimeValue() {
        if (mEnableArt != null) {
            String currentValue = currentRuntimeValue();
			if(RUNTIME_ART_MODE.equals(currentValue.trim())){
		        mEnableArt.setChecked(true);
			} else {
				mEnableArt.setChecked(false);
			}
        }
    }

    void pokeSystemProperties() {
        //if (!mDontPokeProperties) {
            //noinspection unchecked
            (new SystemPropPoker()).execute();
       // }
    }

    static class SystemPropPoker extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            String[] services;
            try {
                services = ServiceManager.listServices();
            } catch (RemoteException e) {
                return null;
            }
            for (String service : services) {
                IBinder obj = ServiceManager.checkService(service);
                if (obj != null) {
                    Parcel data = Parcel.obtain();
                    try {
                        obj.transact(IBinder.SYSPROPS_TRANSACTION, data, null, 0);
                    } catch (RemoteException e) {
                    } catch (Exception e) {
                        Log.i("haha", "Someone wrote a bad service '" + service
                                + "' that doesn't like to be poked: " + e);
                    }
                    data.recycle();
                }
            }
            return null;
        }
    }


}
