/*
 * Copyright (C) 2008 The Android Open Source Project
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

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import aurora.widget.AuroraActionBar;
import android.app.ActionBar;
import aurora.app.AuroraActivity;
import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import aurora.app.AuroraAlertDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.admin.DevicePolicyManager;
import android.app.backup.IBackupManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.os.Trace;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraMultiCheckPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreferenceFragment;
import aurora.preference.AuroraPreferenceGroup;
import aurora.preference.AuroraPreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.HardwareRenderer;
import android.view.IWindowManager;
import android.view.View;
import android.widget.CompoundButton;
import aurora.widget.AuroraSwitch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

// Add begin by aurora.jiangmx
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraSwitchPreference;
import android.os.PowerManager;
import android.content.DialogInterface.OnDismissListener;
import dalvik.system.VMRuntime;
import java.io.File;
import android.view.KeyEvent;
// Add end

/*
 * Displays preferences for application developers.
 */
public class DevelopmentSettings extends AuroraPreferenceFragment
        implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener,
                OnPreferenceChangeListener, CompoundButton.OnCheckedChangeListener {

    /**
     * AuroraPreference file were development settings prefs are stored.
     */
    public static final String PREF_FILE = "development";

    /**
     * Whether to show the development settings to the user.  Default is false.
     */
    public static final String PREF_SHOW = "show";

    private static final String ENABLE_ADB = "enable_adb";
    private static final String KEEP_SCREEN_ON = "keep_screen_on";
    private static final String ALLOW_MOCK_LOCATION = "allow_mock_location";
    private static final String HDCP_CHECKING_KEY = "hdcp_checking";
    private static final String HDCP_CHECKING_PROPERTY = "persist.sys.hdcp_checking";
    private static final String ENFORCE_READ_EXTERNAL = "enforce_read_external";
    private static final String LOCAL_BACKUP_PASSWORD = "local_backup_password";
    private static final String HARDWARE_UI_PROPERTY = "persist.sys.ui.hw";
    private static final String MSAA_PROPERTY = "debug.egl.force_msaa";
    private static final String BUGREPORT = "bugreport";
    private static final String BUGREPORT_IN_POWER_KEY = "bugreport_in_power";
    private static final String OPENGL_TRACES_PROPERTY = "debug.egl.trace";

    private static final String DEBUG_APP_KEY = "debug_app";
    private static final String WAIT_FOR_DEBUGGER_KEY = "wait_for_debugger";
    private static final String VERIFY_APPS_OVER_USB_KEY = "verify_apps_over_usb";
    private static final String STRICT_MODE_KEY = "strict_mode";
    private static final String POINTER_LOCATION_KEY = "pointer_location";
    private static final String SHOW_TOUCHES_KEY = "show_touches";
    private static final String SHOW_SCREEN_UPDATES_KEY = "show_screen_updates";
    private static final String DISABLE_OVERLAYS_KEY = "disable_overlays";
    private static final String SHOW_CPU_USAGE_KEY = "show_cpu_usage";
    private static final String FORCE_HARDWARE_UI_KEY = "force_hw_ui";
    private static final String FORCE_MSAA_KEY = "force_msaa";
    private static final String TRACK_FRAME_TIME_KEY = "track_frame_time";
    private static final String SHOW_HW_SCREEN_UPDATES_KEY = "show_hw_screen_udpates";
    private static final String SHOW_HW_LAYERS_UPDATES_KEY = "show_hw_layers_udpates";
    private static final String SHOW_HW_OVERDRAW_KEY = "show_hw_overdraw";
    private static final String DEBUG_LAYOUT_KEY = "debug_layout";
    private static final String WINDOW_ANIMATION_SCALE_KEY = "window_animation_scale";
    private static final String TRANSITION_ANIMATION_SCALE_KEY = "transition_animation_scale";
    private static final String ANIMATOR_DURATION_SCALE_KEY = "animator_duration_scale";
    private static final String OVERLAY_DISPLAY_DEVICES_KEY = "overlay_display_devices";
    private static final String DEBUG_DEBUGGING_CATEGORY_KEY = "debug_debugging_category";
    private static final String OPENGL_TRACES_KEY = "enable_opengl_traces";

    private static final String ENABLE_TRACES_KEY = "enable_traces";
    
    private static final String IMMEDIATELY_DESTROY_ACTIVITIES_KEY
            = "immediately_destroy_activities";
    private static final String APP_PROCESS_LIMIT_KEY = "app_process_limit";

    private static final String SHOW_ALL_ANRS_KEY = "show_all_anrs";

    private static final String TAG_CONFIRM_ENFORCE = "confirm_enforce";

    private static final String PACKAGE_MIME_TYPE = "application/vnd.android.package-archive";

    private static final int RESULT_DEBUG_APP = 1000;

    public static final int TRACE_FLAGS_START_BIT = 1;
    public static final String[] TRACE_TAGS = {
        "Graphics", "Input", "View", "WebView", "Window Manager",
        "Activity Manager", "Sync Manager", "Audio", "Video", "Camera",
    };
    public static final String PROPERTY_TRACE_TAG_ENABLEFLAGS = "debug.atrace.tags.enableflags";

    private static final String KEY_DEV_AURORA_BUILD_NUMBER = "dev_aurora_build_number";

    private IWindowManager mWindowManager;
    private IBackupManager mBackupManager;
    private DevicePolicyManager mDpm;

    private AuroraSwitch mEnabledSwitch;
    private boolean mLastEnabledState;
    private boolean mHaveDebugSettings;
    private boolean mDontPokeProperties;
    // Delete begin by aurora.jiangmx
    //private AuroraCheckBoxPreference mEnableAdb;
	// Delete end
    private AuroraPreference mBugreport;
    private AuroraCheckBoxPreference mBugreportInPower;
    private AuroraCheckBoxPreference mKeepScreenOn;
//    private AuroraCheckBoxPreference mEnforceReadExternal;
    private AuroraCheckBoxPreference mAllowMockLocation;
    private AuroraPreferenceScreen mPassword;

    private String mDebugApp;
    private AuroraPreference mDebugAppPref;
    private AuroraCheckBoxPreference mWaitForDebugger;
    private AuroraCheckBoxPreference mVerifyAppsOverUsb;

    private AuroraCheckBoxPreference mStrictMode;
    private AuroraCheckBoxPreference mPointerLocation;
    private AuroraCheckBoxPreference mShowTouches;
    private AuroraCheckBoxPreference mShowScreenUpdates;
    private AuroraCheckBoxPreference mDisableOverlays;
    private AuroraCheckBoxPreference mShowCpuUsage;
    private AuroraCheckBoxPreference mForceHardwareUi;
    private AuroraCheckBoxPreference mForceMsaa;
    private AuroraCheckBoxPreference mTrackFrameTime;
    private AuroraCheckBoxPreference mShowHwScreenUpdates;
    private AuroraCheckBoxPreference mShowHwLayersUpdates;
    private AuroraCheckBoxPreference mShowHwOverdraw;
    private AuroraCheckBoxPreference mDebugLayout;
    private AuroraListPreference mWindowAnimationScale;
    private AuroraListPreference mTransitionAnimationScale;
    private AuroraListPreference mAnimatorDurationScale;
    private AuroraListPreference mOverlayDisplayDevices;
    private AuroraListPreference mOpenGLTraces;
    private AuroraMultiCheckPreference mEnableTracesPref;

    private AuroraCheckBoxPreference mImmediatelyDestroyActivities;
    private AuroraListPreference mAppProcessLimit;

    private AuroraCheckBoxPreference mShowAllANRs;

    private final ArrayList<AuroraPreference> mAllPrefs = new ArrayList<AuroraPreference>();
    private final ArrayList<AuroraCheckBoxPreference> mResetCbPrefs
            = new ArrayList<AuroraCheckBoxPreference>();

    private final HashSet<AuroraPreference> mDisabledPrefs = new HashSet<AuroraPreference>();

    // To track whether a confirmation dialog was clicked.
    private boolean mDialogClicked;
    private Dialog mEnableDialog;
    private Dialog mAdbDialog;

    //gionee wangyy 20120710 modify for CR00643075 begin
    private static final boolean mGNUsbUISupport = SystemProperties.get("ro.gn.usb.ui.support").equals("yes");
    //gionee wangyy 20120710 modify for CR00643075 end
    private AuroraCheckBoxPreference mEnableCom;
    private UsbManager mUsbManager;
    private static final String AURORA_USB_MTP ="mtp,diag";
    private static final String USB_FUNCTION_CHARGING =
            "diag,serial_smd,serial_tty,rmnet_bam";
    private static final String NO_USB_FUNCTION_CHARGING = "serial_smd,serial_tty,rmnet_bam";

    // Add begin by aurora.jiangmx
    private static final String DEBUG_INPUT_KEY = "debug_input_category";
    private static final String DEBUG_MONITORING_KEY = "debug_monitoring_category";
    private static final String DEBUG_APP_CATEGORY_KEY = "debug_applications_category";
    private static final String GN_ENABLE_KEY = "gn_enable_debug";
    private static final String DEBUG_DRAWING_KEY = "debug_drawing_category";
    
    private AuroraPreferenceCategory mDebugCategory;
    private AuroraPreference mHdcpChecking;
    private AuroraPreferenceCategory mInputCategory;
    private AuroraPreferenceCategory mMonitoringCategory;
    private AuroraPreferenceCategory mAppCategory;
    private AuroraPreferenceScreen mGNEnableScreen;
    private AuroraPreference mDevBuildNumPref;
    private AuroraPreference mDebugDrawPref;
    
    // user mode
    //private static final String ENABLE_ADB = "enable_adb";

	private static final String SELECT_RUNTIME_PROPERTY = "persist.sys.dalvik.vm.lib";
	private static final String RUNTIME_DVM_MODE = "libdvm.so";
	private static final String RUNTIME_ART_MODE = "libart.so";
    private static final String ENABLE_ART = "enable_art";
    private static final String USER_MODE_KEY = "dev_user_mode";
    private AuroraSwitchPreference mEnableArt;

    private static final String VERIFIER_DEVICE_IDENTIFIER = "verifier_device_identifier";

    private AuroraSwitchPreference mEnableAdb;
    
    private boolean mOkClicked;

    private Dialog mOkDialog;
    private Dialog mArtDialog;

	// Add end
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        mBackupManager = IBackupManager.Stub.asInterface(
                ServiceManager.getService(Context.BACKUP_SERVICE));
        mDpm = (DevicePolicyManager)getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);

        addPreferencesFromResource(R.xml.development_prefs);
        // Delete begin by aurora.jiangmx
        //mEnableAdb = findAndInitCheckboxPref(ENABLE_ADB);
		// Delete end
        mBugreport = findPreference(BUGREPORT);
        mBugreportInPower = findAndInitCheckboxPref(BUGREPORT_IN_POWER_KEY);
        mKeepScreenOn = findAndInitCheckboxPref(KEEP_SCREEN_ON);
//        mEnforceReadExternal = findAndInitCheckboxPref(ENFORCE_READ_EXTERNAL);
        mAllowMockLocation = findAndInitCheckboxPref(ALLOW_MOCK_LOCATION);
        mPassword = (AuroraPreferenceScreen) findPreference(LOCAL_BACKUP_PASSWORD);
        mAllPrefs.add(mPassword);

        mDebugAppPref = findPreference(DEBUG_APP_KEY);
        mAllPrefs.add(mDebugAppPref);
        mWaitForDebugger = findAndInitCheckboxPref(WAIT_FOR_DEBUGGER_KEY);
        mVerifyAppsOverUsb = findAndInitCheckboxPref(VERIFY_APPS_OVER_USB_KEY);
        if (!showVerifierSetting()) {
            AuroraPreferenceGroup debugDebuggingCategory = (AuroraPreferenceGroup)
                    findPreference(DEBUG_DEBUGGING_CATEGORY_KEY);
            if (debugDebuggingCategory != null) {
                debugDebuggingCategory.removePreference(mVerifyAppsOverUsb);
            } else {
                mVerifyAppsOverUsb.setEnabled(false);
            }
        }
        mStrictMode = findAndInitCheckboxPref(STRICT_MODE_KEY);
        mPointerLocation = findAndInitCheckboxPref(POINTER_LOCATION_KEY);
        mShowTouches = findAndInitCheckboxPref(SHOW_TOUCHES_KEY);
        mShowScreenUpdates = findAndInitCheckboxPref(SHOW_SCREEN_UPDATES_KEY);
        mDisableOverlays = findAndInitCheckboxPref(DISABLE_OVERLAYS_KEY);
        mShowCpuUsage = findAndInitCheckboxPref(SHOW_CPU_USAGE_KEY);
        mForceHardwareUi = findAndInitCheckboxPref(FORCE_HARDWARE_UI_KEY);
        mForceMsaa = findAndInitCheckboxPref(FORCE_MSAA_KEY);
        mTrackFrameTime = findAndInitCheckboxPref(TRACK_FRAME_TIME_KEY);
        mShowHwScreenUpdates = findAndInitCheckboxPref(SHOW_HW_SCREEN_UPDATES_KEY);
        mShowHwLayersUpdates = findAndInitCheckboxPref(SHOW_HW_LAYERS_UPDATES_KEY);
        mShowHwOverdraw = findAndInitCheckboxPref(SHOW_HW_OVERDRAW_KEY);  
        mDebugLayout = findAndInitCheckboxPref(DEBUG_LAYOUT_KEY);
        mWindowAnimationScale = (AuroraListPreference) findPreference(WINDOW_ANIMATION_SCALE_KEY);
        mAllPrefs.add(mWindowAnimationScale);
        mWindowAnimationScale.setOnPreferenceChangeListener(this);
        mTransitionAnimationScale = (AuroraListPreference) findPreference(TRANSITION_ANIMATION_SCALE_KEY);
        mAllPrefs.add(mTransitionAnimationScale);
        mTransitionAnimationScale.setOnPreferenceChangeListener(this);
        mAnimatorDurationScale = (AuroraListPreference) findPreference(ANIMATOR_DURATION_SCALE_KEY);
        mAllPrefs.add(mAnimatorDurationScale);
        mAnimatorDurationScale.setOnPreferenceChangeListener(this);
        mOverlayDisplayDevices = (AuroraListPreference) findPreference(OVERLAY_DISPLAY_DEVICES_KEY);
        mAllPrefs.add(mOverlayDisplayDevices);
        mOverlayDisplayDevices.setOnPreferenceChangeListener(this);
        mOpenGLTraces = (AuroraListPreference) findPreference(OPENGL_TRACES_KEY);
        mAllPrefs.add(mOpenGLTraces);
        mOpenGLTraces.setOnPreferenceChangeListener(this);
        mEnableTracesPref = (AuroraMultiCheckPreference)findPreference(ENABLE_TRACES_KEY);
        String[] traceValues = new String[TRACE_TAGS.length];
        for (int i=TRACE_FLAGS_START_BIT; i<traceValues.length; i++) {
            traceValues[i] = Integer.toString(1<<i);
        }
        mEnableTracesPref.setEntries(TRACE_TAGS);
        mEnableTracesPref.setEntryValues(traceValues);
        mAllPrefs.add(mEnableTracesPref);
        mEnableTracesPref.setOnPreferenceChangeListener(this);

        mImmediatelyDestroyActivities = (AuroraCheckBoxPreference) findPreference(
                IMMEDIATELY_DESTROY_ACTIVITIES_KEY);
        mAllPrefs.add(mImmediatelyDestroyActivities);
        mResetCbPrefs.add(mImmediatelyDestroyActivities);
        mAppProcessLimit = (AuroraListPreference) findPreference(APP_PROCESS_LIMIT_KEY);
        mAllPrefs.add(mAppProcessLimit);
        mAppProcessLimit.setOnPreferenceChangeListener(this);

        mShowAllANRs = (AuroraCheckBoxPreference) findPreference(
                SHOW_ALL_ANRS_KEY);
        mAllPrefs.add(mShowAllANRs);
        mResetCbPrefs.add(mShowAllANRs);

        AuroraPreference hdcpChecking = findPreference(HDCP_CHECKING_KEY);
        if (hdcpChecking != null) {
            mAllPrefs.add(hdcpChecking);
        }
        removeHdcpOptionsForProduction();

        String iuniBuildNumber = SystemProperties.get("ro.gn.iuniznvernumber",
                getResources().getString(R.string.device_info_default));
        findPreference(KEY_DEV_AURORA_BUILD_NUMBER).setSummary(iuniBuildNumber);
        
        // qy add 2014 05 19 begin
        mUsbManager = (UsbManager)getActivity().getSystemService(Context.USB_SERVICE);
        mEnableCom = (AuroraCheckBoxPreference)findPreference("enable_com");
//        String buildModel = Build.MODEL;
        String deviceName = SystemProperties.get("ro.product.name");        
        Log.i("qy", "deviceName ="+deviceName);		
        if (deviceName.contains("IUNI")) {
        	mEnableCom.setOnPreferenceChangeListener(this);
        	SharedPreferences iuniSP = getActivity().getSharedPreferences("iuni", Context.MODE_PRIVATE);
        	mEnableCom.setChecked(iuniSP.getBoolean("imei", false));
        	
        }else{
        	AuroraPreferenceGroup debugDebuggingCategory = (AuroraPreferenceGroup)
                    findPreference(DEBUG_DEBUGGING_CATEGORY_KEY);
            if (debugDebuggingCategory != null && mEnableCom !=null) {
                debugDebuggingCategory.removePreference(mEnableCom);
            }
        }
        //qy add 2014 05 19 end 
        
        // Add begin by aurora.jiangmx
        initDevUserModePrefs();
		
		boolean lIsAdbEnabled = Settings.Secure.getInt(getActivity().getContentResolver(),
    			Settings.Secure.ADB_ENABLED, 0) != 0 ? true:false;
    	
		if(!lIsAdbEnabled){
			doRemoveDevPreferences();
		}
		// Add end
    }

    // Add begin by aurora.jiangmx
    private void initDevUserModePrefs(){
        mEnableAdb = (AuroraSwitchPreference) findPreference(ENABLE_ADB);
        mEnableAdb.setOnPreferenceChangeListener(this);

		mEnableArt = (AuroraSwitchPreference) findPreference(ENABLE_ART);
        mEnableArt.setOnPreferenceChangeListener(this);
		filterRuntimeOptions();
		updateRuntimeValue();

        /*AuroraPreferenceCategory prefC = (AuroraPreferenceCategory)findPreference("usb_adb");
        getPreferenceScreen().removePreference(prefC);*/
    	
        mHdcpChecking = findPreference(HDCP_CHECKING_KEY);
        mInputCategory = (AuroraPreferenceCategory)findPreference(DEBUG_INPUT_KEY);
    	mDebugCategory = (AuroraPreferenceCategory) findPreference(DEBUG_DEBUGGING_CATEGORY_KEY);
    	mMonitoringCategory = (AuroraPreferenceCategory) findPreference(DEBUG_MONITORING_KEY);
    	mAppCategory = (AuroraPreferenceCategory) findPreference(DEBUG_APP_CATEGORY_KEY);
    	mGNEnableScreen = (AuroraPreferenceScreen) findPreference(GN_ENABLE_KEY);
    	mDevBuildNumPref = findPreference(KEY_DEV_AURORA_BUILD_NUMBER);
    	mDebugDrawPref = (AuroraPreferenceCategory) findPreference(DEBUG_DRAWING_KEY);
    }
	// Add end
    
    private AuroraCheckBoxPreference findAndInitCheckboxPref(String key) {
        AuroraCheckBoxPreference pref = (AuroraCheckBoxPreference) findPreference(key);
        if (pref == null) {
            throw new IllegalArgumentException("Cannot find preference with key = " + key);
        }
        mAllPrefs.add(pref);
        mResetCbPrefs.add(pref);
        return pref;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();
        mEnabledSwitch = new AuroraSwitch(activity);

        final int padding = activity.getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        mEnabledSwitch.setPadding(0, 0, padding, 0);
        mEnabledSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
	//AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
      	/*
        final AuroraActivity activity = (AuroraActivity)getActivity();
	
        activity.getAuroraActionBar().setDisplayOptions(AuroraActionBar.DISPLAY_SHOW_CUSTOM,
                AuroraActionBar.DISPLAY_SHOW_CUSTOM);
        activity.getAuroraActionBar().setCustomView(mEnabledSwitch, new AuroraActionBar.LayoutParams(
                AuroraActionBar.LayoutParams.WRAP_CONTENT,
                AuroraActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | Gravity.END));
	*/
        //AURORA-END::delete temporarily for compile::waynelin::2013-9-14
    }

    @Override
    public void onStop() {
        super.onStop();
        final AuroraActivity activity = (AuroraActivity)getActivity();
	//AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
      	/*
        activity.getAuroraActionBar().setDisplayOptions(0, AuroraActionBar.DISPLAY_SHOW_CUSTOM);
        activity.getAuroraActionBar().setCustomView(null);
	*/
        //AURORA-END::delete temporarily for compile::waynelin::2013-9-14
    }

    private void removeHdcpOptionsForProduction() {
        if ("user".equals(Build.TYPE)) {
            AuroraPreference hdcpChecking = findPreference(HDCP_CHECKING_KEY);
            if (hdcpChecking != null) {
                // Remove the preference
                getPreferenceScreen().removePreference(hdcpChecking);
                mAllPrefs.remove(hdcpChecking);
            }
        }
    }

    private void setPrefsEnabledState(boolean enabled) {
        for (int i = 0; i < mAllPrefs.size(); i++) {
            AuroraPreference pref = mAllPrefs.get(i);
            pref.setEnabled(enabled && !mDisabledPrefs.contains(pref));
        }
        updateAllOptions();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mDpm.getMaximumTimeToLock(null) > 0) {
            // A DeviceAdmin has specified a maximum time until the device
            // will lock...  in this case we can't allow the user to turn
            // on "stay awake when plugged in" because that would defeat the
            // restriction.
            mDisabledPrefs.add(mKeepScreenOn);
        } else {
            mDisabledPrefs.remove(mKeepScreenOn);
        }

        final ContentResolver cr = getActivity().getContentResolver();
        // Modify begin by jiyouguang
//       mLastEnabledState = Settings.Global.getInt(cr,
//                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;
        mLastEnabledState = true; //调试开关默认打开，不再去判断是否已经打开
        mEnabledSwitch.setChecked(mLastEnabledState);
        setPrefsEnabledState(mLastEnabledState);
		// ---------end-----------
	    mEnableAdb.setChecked(Settings.Secure.getInt(cr,
                Settings.Secure.ADB_ENABLED, 0) != 0);
	    if (mGNUsbUISupport) {
			
			mEnableAdb.setChecked(Settings.Secure.getInt(cr,
					"real_debug_state",0) != 0);
        }
	    
	    boolean lIsAdbEnabled = Settings.Secure.getInt(getActivity().getContentResolver(),
    			Settings.Secure.ADB_ENABLED, 0) != 0 ? true:false;
    	
		if(!lIsAdbEnabled){
			doRemoveDevPreferences();
		}
		// Modify end

        if (mHaveDebugSettings && !mLastEnabledState) {
            // Overall debugging is disabled, but there are some debug
            // settings that are enabled.  This is an invalid state.  AuroraSwitch
            // to debug settings being enabled, so the user knows there is
            // stuff enabled and can turn it all off if they want.
            Settings.Global.putInt(getActivity().getContentResolver(),
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
            mLastEnabledState = true;
            mEnabledSwitch.setChecked(mLastEnabledState);
            setPrefsEnabledState(mLastEnabledState);
        }
        
    }

    void updateCheckBox(AuroraCheckBoxPreference checkBox, boolean value) {
        checkBox.setChecked(value);
        mHaveDebugSettings |= value;
    }

    private void updateAllOptions() {
        final Context context = getActivity();
        final ContentResolver cr = context.getContentResolver();
        mHaveDebugSettings = false;
        // Delete begin by aurora.jiangmx
        /*updateCheckBox(mEnableAdb, Settings.Global.getInt(cr,
        Settings.Global.ADB_ENABLED, 0) != 0);*/
		// Delete end
        updateCheckBox(mBugreportInPower, Settings.Secure.getInt(cr,
                Settings.Secure.BUGREPORT_IN_POWER_MENU, 0) != 0);
        updateCheckBox(mKeepScreenOn, Settings.Global.getInt(cr,
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN, 0) != 0);
//        updateCheckBox(mEnforceReadExternal, isPermissionEnforced(READ_EXTERNAL_STORAGE));
        updateCheckBox(mAllowMockLocation, Settings.Secure.getInt(cr,
                Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0);
        updateHdcpValues();
        updatePasswordSummary();
        updateDebuggerOptions();
        updateStrictModeVisualOptions();
        updatePointerLocationOptions();
        updateShowTouchesOptions();
        updateFlingerOptions();
        updateCpuUsageOptions();
        updateHardwareUiOptions();
        updateMsaaOptions();
        updateTrackFrameTimeOptions();
        updateShowHwScreenUpdatesOptions();
        updateShowHwLayersUpdatesOptions();
        updateShowHwOverdrawOptions();
        updateDebugLayoutOptions();
        updateAnimationScaleOptions();
        updateOverlayDisplayDevicesOptions();
        updateOpenGLTracesOptions();
        updateEnableTracesOptions();
		
        updateImmediatelyDestroyActivitiesOptions();
        updateAppProcessLimitOptions();
        updateShowAllANRsOptions();
        updateVerifyAppsOverUsbOptions();
        updateBugreportOptions();
    }

    private void resetDangerousOptions() {
        mDontPokeProperties = true;
        for (int i=0; i<mResetCbPrefs.size(); i++) {
            AuroraCheckBoxPreference cb = mResetCbPrefs.get(i);
            if (cb.isChecked()) {
                cb.setChecked(false);
                onPreferenceTreeClick(null, cb);
            }
        }
        resetDebuggerOptions();
        writeAnimationScaleOption(0, mWindowAnimationScale, null);
        writeAnimationScaleOption(1, mTransitionAnimationScale, null);
        writeAnimationScaleOption(2, mAnimatorDurationScale, null);
        writeOverlayDisplayDevicesOptions(null);
        writeEnableTracesOptions(0);
        writeAppProcessLimitOptions(null);
        mHaveDebugSettings = false;
        updateAllOptions();
        mDontPokeProperties = false;
        pokeSystemProperties();
    }

    private void updateHdcpValues() {
        int index = 1; // Defaults to drm-only. Needs to match with R.array.hdcp_checking_values
        AuroraListPreference hdcpChecking = (AuroraListPreference) findPreference(HDCP_CHECKING_KEY);
        if (hdcpChecking != null) {
            String currentValue = SystemProperties.get(HDCP_CHECKING_PROPERTY);
            String[] values = getResources().getStringArray(R.array.hdcp_checking_values);
            String[] summaries = getResources().getStringArray(R.array.hdcp_checking_summaries);
            for (int i = 0; i < values.length; i++) {
                if (currentValue.equals(values[i])) {
                    index = i;
                    break;
                }
            }
            hdcpChecking.setValue(values[index]);
            hdcpChecking.setSummary(summaries[index]);
            hdcpChecking.setOnPreferenceChangeListener(this);
        }
    }

    private void updatePasswordSummary() {
        try {
            if (mBackupManager.hasBackupPassword()) {
                mPassword.setSummary(R.string.local_backup_password_summary_change);
            } else {
                mPassword.setSummary(R.string.local_backup_password_summary_none);
            }
        } catch (RemoteException e) {
            // Not much we can do here
        }
    }

    private void writeDebuggerOptions() {
        try {
            ActivityManagerNative.getDefault().setDebugApp(
                mDebugApp, mWaitForDebugger.isChecked(), true);
        } catch (RemoteException ex) {
        }
    }

    private static void resetDebuggerOptions() {
        try {
            ActivityManagerNative.getDefault().setDebugApp(
                    null, false, true);
        } catch (RemoteException ex) {
        }
    }

    private void updateDebuggerOptions() {
        mDebugApp = Settings.Global.getString(
                getActivity().getContentResolver(), Settings.Global.DEBUG_APP);
        updateCheckBox(mWaitForDebugger, Settings.Global.getInt(
                getActivity().getContentResolver(), Settings.Global.WAIT_FOR_DEBUGGER, 0) != 0);
        if (mDebugApp != null && mDebugApp.length() > 0) {
            String label;
            try {
                ApplicationInfo ai = getActivity().getPackageManager().getApplicationInfo(mDebugApp,
                        PackageManager.GET_DISABLED_COMPONENTS);
                CharSequence lab = getActivity().getPackageManager().getApplicationLabel(ai);
                label = lab != null ? lab.toString() : mDebugApp;
            } catch (PackageManager.NameNotFoundException e) {
                label = mDebugApp;
            }
            mDebugAppPref.setSummary(getResources().getString(R.string.debug_app_set, label));
            mWaitForDebugger.setEnabled(true);
            mHaveDebugSettings = true;
        } else {
            mDebugAppPref.setSummary(getResources().getString(R.string.debug_app_not_set));
            mWaitForDebugger.setEnabled(false);
        }
    }

    private void updateVerifyAppsOverUsbOptions() {
        updateCheckBox(mVerifyAppsOverUsb, Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.PACKAGE_VERIFIER_INCLUDE_ADB, 1) != 0);
        mVerifyAppsOverUsb.setEnabled(enableVerifierSetting());
    }

    private void writeVerifyAppsOverUsbOptions() {
        Settings.Global.putInt(getActivity().getContentResolver(),
              Settings.Global.PACKAGE_VERIFIER_INCLUDE_ADB, mVerifyAppsOverUsb.isChecked() ? 1 : 0);
    }

    private boolean enableVerifierSetting() {
        final ContentResolver cr = getActivity().getContentResolver();
        if (Settings.Global.getInt(cr, Settings.Global.ADB_ENABLED, 0) == 0) {
            return false;
        }
        if (Settings.Global.getInt(cr, Settings.Global.PACKAGE_VERIFIER_ENABLE, 1) == 0) {
            return false;
        } else {
            final PackageManager pm = getActivity().getPackageManager();
            final Intent verification = new Intent(Intent.ACTION_PACKAGE_NEEDS_VERIFICATION);
            verification.setType(PACKAGE_MIME_TYPE);
            verification.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            final List<ResolveInfo> receivers = pm.queryBroadcastReceivers(verification, 0);
            if (receivers.size() == 0) {
                return false;
            }
        }
        return true;
    }

    private boolean showVerifierSetting() {
        return Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.PACKAGE_VERIFIER_SETTING_VISIBLE, 1) > 0;
    }

    private void updateBugreportOptions() {
        if ("user".equals(Build.TYPE)) {
            final ContentResolver resolver = getActivity().getContentResolver();
            final boolean adbEnabled = Settings.Global.getInt(
                    resolver, Settings.Global.ADB_ENABLED, 0) != 0;
            if (adbEnabled) {
                mBugreport.setEnabled(true);
                mBugreportInPower.setEnabled(true);
            } else {
                mBugreport.setEnabled(false);
                mBugreportInPower.setEnabled(false);
                mBugreportInPower.setChecked(false);
                Settings.Secure.putInt(resolver, Settings.Secure.BUGREPORT_IN_POWER_MENU, 0);
            }
        } else {
            mBugreportInPower.setEnabled(true);
        }
    }

    // Returns the current state of the system property that controls
    // strictmode flashes.  One of:
    //    0: not explicitly set one way or another
    //    1: on
    //    2: off
    private static int currentStrictModeActiveIndex() {
        if (TextUtils.isEmpty(SystemProperties.get(StrictMode.VISUAL_PROPERTY))) {
            return 0;
        }
        boolean enabled = SystemProperties.getBoolean(StrictMode.VISUAL_PROPERTY, false);
        return enabled ? 1 : 2;
    }

    private void writeStrictModeVisualOptions() {
        try {
            mWindowManager.setStrictModeVisualIndicatorPreference(mStrictMode.isChecked()
                    ? "1" : "");
        } catch (RemoteException e) {
        }
    }

    private void updateStrictModeVisualOptions() {
        updateCheckBox(mStrictMode, currentStrictModeActiveIndex() == 1);
    }

    private void writePointerLocationOptions() {
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.POINTER_LOCATION, mPointerLocation.isChecked() ? 1 : 0);
    }

    private void updatePointerLocationOptions() {
        updateCheckBox(mPointerLocation, Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.POINTER_LOCATION, 0) != 0);
    }

    private void writeShowTouchesOptions() {
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.SHOW_TOUCHES, mShowTouches.isChecked() ? 1 : 0);
    }

    private void updateShowTouchesOptions() {
        updateCheckBox(mShowTouches, Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SHOW_TOUCHES, 0) != 0);
    }

    private void updateFlingerOptions() {
        // magic communication with surface flinger.
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                flinger.transact(1010, data, reply, 0);
                @SuppressWarnings("unused")
                int showCpu = reply.readInt();
                @SuppressWarnings("unused")
                int enableGL = reply.readInt();
                int showUpdates = reply.readInt();
                updateCheckBox(mShowScreenUpdates, showUpdates != 0);
                @SuppressWarnings("unused")
                int showBackground = reply.readInt();
                int disableOverlays = reply.readInt();
                updateCheckBox(mDisableOverlays, disableOverlays != 0);
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException ex) {
        }
    }

    private void writeShowUpdatesOption() {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                final int showUpdates = mShowScreenUpdates.isChecked() ? 1 : 0; 
                data.writeInt(showUpdates);
                flinger.transact(1002, data, null, 0);
                data.recycle();

                updateFlingerOptions();
            }
        } catch (RemoteException ex) {
        }
    }

    private void writeDisableOverlaysOption() {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                final int disableOverlays = mDisableOverlays.isChecked() ? 1 : 0; 
                data.writeInt(disableOverlays);
                flinger.transact(1008, data, null, 0);
                data.recycle();

                updateFlingerOptions();
            }
        } catch (RemoteException ex) {
        }
    }

    private void updateHardwareUiOptions() {
        updateCheckBox(mForceHardwareUi, SystemProperties.getBoolean(HARDWARE_UI_PROPERTY, false));
    }
    
    private void writeHardwareUiOptions() {
        SystemProperties.set(HARDWARE_UI_PROPERTY, mForceHardwareUi.isChecked() ? "true" : "false");
        pokeSystemProperties();
    }

    private void updateMsaaOptions() {
        updateCheckBox(mForceMsaa, SystemProperties.getBoolean(MSAA_PROPERTY, false));
    }

    private void writeMsaaOptions() {
        SystemProperties.set(MSAA_PROPERTY, mForceMsaa.isChecked() ? "true" : "false");
        pokeSystemProperties();
    }

    private void updateTrackFrameTimeOptions() {
        updateCheckBox(mTrackFrameTime,
                SystemProperties.getBoolean(HardwareRenderer.PROFILE_PROPERTY, false));
    }

    private void writeTrackFrameTimeOptions() {
        SystemProperties.set(HardwareRenderer.PROFILE_PROPERTY,
                mTrackFrameTime.isChecked() ? "true" : "false");
        pokeSystemProperties();
    }

    private void updateShowHwScreenUpdatesOptions() {
        updateCheckBox(mShowHwScreenUpdates,
                SystemProperties.getBoolean(HardwareRenderer.DEBUG_DIRTY_REGIONS_PROPERTY, false));
    }

    private void writeShowHwScreenUpdatesOptions() {
        SystemProperties.set(HardwareRenderer.DEBUG_DIRTY_REGIONS_PROPERTY,
                mShowHwScreenUpdates.isChecked() ? "true" : null);
        pokeSystemProperties();
    }

    private void updateShowHwLayersUpdatesOptions() {
        updateCheckBox(mShowHwLayersUpdates, SystemProperties.getBoolean(
                HardwareRenderer.DEBUG_SHOW_LAYERS_UPDATES_PROPERTY, false));
    }

    private void writeShowHwLayersUpdatesOptions() {
        SystemProperties.set(HardwareRenderer.DEBUG_SHOW_LAYERS_UPDATES_PROPERTY,
                mShowHwLayersUpdates.isChecked() ? "true" : null);
        pokeSystemProperties();
    }

    private void updateShowHwOverdrawOptions() {
    	//HardwareRenderer.DEBUG_SHOW_OVERDRAW_PROPERTY = "debug.hwui.show_overdraw"
//        updateCheckBox(mShowHwOverdraw, SystemProperties.getBoolean(
//                HardwareRenderer.DEBUG_SHOW_OVERDRAW_PROPERTY, false));
        updateCheckBox(mShowHwOverdraw, SystemProperties.getBoolean(
        		"debug.hwui.show_overdraw", false));
    }

    private void writeShowHwOverdrawOptions() {
    	//HardwareRenderer.DEBUG_SHOW_OVERDRAW_PROPERTY = "debug.hwui.show_overdraw"
//        SystemProperties.set(HardwareRenderer.DEBUG_SHOW_OVERDRAW_PROPERTY,
//                mShowHwOverdraw.isChecked() ? "true" : null);
    	SystemProperties.set("debug.hwui.show_overdraw",
                mShowHwOverdraw.isChecked() ? "true" : null);
    	// qy add begin 2014 06 30
    	SystemProperties.set("debug.hwui.overdraw",
    			mShowHwOverdraw.isChecked() ? "show" : null);
    	// qy add end
        pokeSystemProperties();
    }

    private void updateDebugLayoutOptions() {
        updateCheckBox(mDebugLayout,
                SystemProperties.getBoolean(View.DEBUG_LAYOUT_PROPERTY, false));
    }

    private void writeDebugLayoutOptions() {
        SystemProperties.set(View.DEBUG_LAYOUT_PROPERTY,
                mDebugLayout.isChecked() ? "true" : "false");
        pokeSystemProperties();
    }

    private void updateCpuUsageOptions() {
        updateCheckBox(mShowCpuUsage, Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.SHOW_PROCESSES, 0) != 0);
    }
    
    private void writeCpuUsageOptions() {
        boolean value = mShowCpuUsage.isChecked();
        Settings.Global.putInt(getActivity().getContentResolver(),
                Settings.Global.SHOW_PROCESSES, value ? 1 : 0);
        Intent service = (new Intent())
                .setClassName("com.android.systemui", "com.android.systemui.LoadAverageService");
        if (value) {
            getActivity().startService(service);
        } else {
            getActivity().stopService(service);
        }
    }

    private void writeImmediatelyDestroyActivitiesOptions() {
        try {
            ActivityManagerNative.getDefault().setAlwaysFinish(
                    mImmediatelyDestroyActivities.isChecked());
        } catch (RemoteException ex) {
        }
    }

    private void updateImmediatelyDestroyActivitiesOptions() {
        updateCheckBox(mImmediatelyDestroyActivities, Settings.Global.getInt(
            getActivity().getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES, 0) != 0);
    }

    private void updateAnimationScaleValue(int which, AuroraListPreference pref) {
        try {
            float scale = mWindowManager.getAnimationScale(which);
            if (scale != 1) {
                mHaveDebugSettings = true;
            }
            CharSequence[] values = pref.getEntryValues();
            for (int i=0; i<values.length; i++) {
                float val = Float.parseFloat(values[i].toString());
                if (scale <= val) {
                    pref.setValueIndex(i);
                    pref.setSummary(pref.getEntries()[i]);
                    return;
                }
            }
            pref.setValueIndex(values.length-1);
            pref.setSummary(pref.getEntries()[0]);
        } catch (RemoteException e) {
        }
    }

    private void updateAnimationScaleOptions() {
        updateAnimationScaleValue(0, mWindowAnimationScale);
        updateAnimationScaleValue(1, mTransitionAnimationScale);
        updateAnimationScaleValue(2, mAnimatorDurationScale);
    }

    private void writeAnimationScaleOption(int which, AuroraListPreference pref, Object newValue) {
        try {
            float scale = newValue != null ? Float.parseFloat(newValue.toString()) : 1;
            mWindowManager.setAnimationScale(which, scale);
            updateAnimationScaleValue(which, pref);
        } catch (RemoteException e) {
        }
    }

    private void updateOverlayDisplayDevicesOptions() {
        String value = Settings.Global.getString(getActivity().getContentResolver(),
                Settings.Global.OVERLAY_DISPLAY_DEVICES);
        if (value == null) {
            value = "";
        }

        CharSequence[] values = mOverlayDisplayDevices.getEntryValues();
        for (int i = 0; i < values.length; i++) {
            if (value.contentEquals(values[i])) {
                mOverlayDisplayDevices.setValueIndex(i);
                mOverlayDisplayDevices.setSummary(mOverlayDisplayDevices.getEntries()[i]);
                return;
            }
        }
        mOverlayDisplayDevices.setValueIndex(0);
        mOverlayDisplayDevices.setSummary(mOverlayDisplayDevices.getEntries()[0]);
    }

    private void writeOverlayDisplayDevicesOptions(Object newValue) {
        Settings.Global.putString(getActivity().getContentResolver(),
                Settings.Global.OVERLAY_DISPLAY_DEVICES, (String)newValue);
        updateOverlayDisplayDevicesOptions();
    }

    private void updateOpenGLTracesOptions() {
        String value = SystemProperties.get(OPENGL_TRACES_PROPERTY);
        if (value == null) {
            value = "";
        }

        CharSequence[] values = mOpenGLTraces.getEntryValues();
        for (int i = 0; i < values.length; i++) {
            if (value.contentEquals(values[i])) {
                mOpenGLTraces.setValueIndex(i);
                mOpenGLTraces.setSummary(mOpenGLTraces.getEntries()[i]);
                return;
            }
        }
        mOpenGLTraces.setValueIndex(0);
        mOpenGLTraces.setSummary(mOpenGLTraces.getEntries()[0]);
    }

    private void writeOpenGLTracesOptions(Object newValue) {
        SystemProperties.set(OPENGL_TRACES_PROPERTY, newValue == null ? "" : newValue.toString());
        pokeSystemProperties();
        updateOpenGLTracesOptions();
    }

    private void updateAppProcessLimitOptions() {
        try {
            int limit = ActivityManagerNative.getDefault().getProcessLimit();
            CharSequence[] values = mAppProcessLimit.getEntryValues();
            for (int i=0; i<values.length; i++) {
                int val = Integer.parseInt(values[i].toString());
                if (val >= limit) {
                    if (i != 0) {
                        mHaveDebugSettings = true;
                    }
                    mAppProcessLimit.setValueIndex(i);
                    mAppProcessLimit.setSummary(mAppProcessLimit.getEntries()[i]);
                    return;
                }
            }
            mAppProcessLimit.setValueIndex(0);
            mAppProcessLimit.setSummary(mAppProcessLimit.getEntries()[0]);
        } catch (RemoteException e) {
        }
    }

    private void writeAppProcessLimitOptions(Object newValue) {
        try {
            int limit = newValue != null ? Integer.parseInt(newValue.toString()) : -1;
            ActivityManagerNative.getDefault().setProcessLimit(limit);
            updateAppProcessLimitOptions();
        } catch (RemoteException e) {
        }
    }

    private void writeShowAllANRsOptions() {
        Settings.Secure.putInt(getActivity().getContentResolver(),
                Settings.Secure.ANR_SHOW_BACKGROUND,
                mShowAllANRs.isChecked() ? 1 : 0);
    }

    private void updateShowAllANRsOptions() {
        updateCheckBox(mShowAllANRs, Settings.Secure.getInt(
            getActivity().getContentResolver(), Settings.Secure.ANR_SHOW_BACKGROUND, 0) != 0);
    }

    private void updateEnableTracesOptions() {
        long flags = SystemProperties.getLong(PROPERTY_TRACE_TAG_ENABLEFLAGS, 0);
        String[] values = mEnableTracesPref.getEntryValues();
        int numSet = 0;
        for (int i=TRACE_FLAGS_START_BIT; i<values.length; i++) {
            boolean set = (flags&(1<<i)) != 0;
            mEnableTracesPref.setValue(i-TRACE_FLAGS_START_BIT, set);
            if (set) {
                numSet++;
            }
        }
        if (numSet == 0) {
            mEnableTracesPref.setSummary(R.string.enable_traces_summary_none);
        } else if (numSet == values.length) {
            mHaveDebugSettings = true;
            mEnableTracesPref.setSummary(R.string.enable_traces_summary_all);
        } else {
            mHaveDebugSettings = true;
            mEnableTracesPref.setSummary(getString(R.string.enable_traces_summary_num, numSet));
        }
    }

    private void writeEnableTracesOptions() {
        long value = 0;
        String[] values = mEnableTracesPref.getEntryValues();
        for (int i=TRACE_FLAGS_START_BIT; i<values.length; i++) {
            if (mEnableTracesPref.getValue(i-TRACE_FLAGS_START_BIT)) {
                value |= 1<<i;
            }
        }
        writeEnableTracesOptions(value);
        // Make sure summary is updated.
        updateEnableTracesOptions();
    }

    private void writeEnableTracesOptions(long value) {
        SystemProperties.set(PROPERTY_TRACE_TAG_ENABLEFLAGS,
                "0x" + Long.toString(value, 16));
        pokeSystemProperties();
    }
	

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	// Delete begin by aurora.jiangmx
        /*if (buttonView == mEnabledSwitch) {
            if (isChecked != mLastEnabledState) {
                if (isChecked) {
                    mDialogClicked = false;
                    if (mEnableDialog != null) dismissDialogs();
                    mEnableDialog = new AuroraAlertDialog.Builder(getActivity()).setMessage(
                            getActivity().getResources().getString(
                                    R.string.dev_settings_warning_message))
                            .setTitle(R.string.dev_settings_warning_title)
                            //.setIconAttribute(android.R.attr.alertDialogIcon)
                            .setPositiveButton(android.R.string.yes, this)
                            .setNegativeButton(android.R.string.no, this)
                            .show();
                    mEnableDialog.setOnDismissListener(this);
                } else {
                    resetDangerousOptions();
                    Settings.Global.putInt(getActivity().getContentResolver(),
                            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
                    mLastEnabledState = isChecked;
                    setPrefsEnabledState(mLastEnabledState);
                }
            }
        }*/
       // Delete end
    }

    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_DEBUG_APP) {
            if (resultCode == AuroraActivity.RESULT_OK) {
                mDebugApp = data.getAction();
                writeDebuggerOptions();
                updateDebuggerOptions();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {

        if (Utils.isMonkeyRunning()) {
            return false;
        }
       // Add begin by aurora.jiangmx
       /*if (preference == mEnableAdb) {
            if (mEnableAdb.isChecked()) {
                mDialogClicked = false;
                if (mAdbDialog != null) dismissDialogs();
                //Gionee:zhang_xin 2012-12-18 modify for CR00746738 start
                
                mAdbDialog = new AuroraAlertDialog.Builder(getActivity()).setMessage(
                        getActivity().getResources().getString(R.string.adb_warning_message))
                        .setTitle(R.string.adb_warning_title)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setPositiveButton(android.R.string.yes, this)
                        .setNegativeButton(android.R.string.no, this)
                        .show();
                
                mAdbDialog = new AuroraAlertDialog.Builder(getActivity(), AuroraAlertDialog.THEME_AMIGO_FULLSCREEN).setMessage(
                         getActivity().getResources().getString(R.string.adb_warning_message))
                         .setTitle(R.string.adb_warning_title)
                         .setPositiveButton(android.R.string.yes, this)
                         .setNegativeButton(android.R.string.no, this)
                         .show();
                mAdbDialog.setCanceledOnTouchOutside(false);
                //Gionee:zhang_xin 2012-12-18 modify for CR00746738 end
                mAdbDialog.setOnDismissListener(this);
            } else {
                Settings.Global.putInt(getActivity().getContentResolver(),
                        Settings.Global.ADB_ENABLED, 0);
               //gionee wangyy 20120714 modify for CR00643075 begin
                if (mGNUsbUISupport) {
                    Settings.Secure.putInt(getActivity().getContentResolver(),
                        "real_debug_state", 0);
                    Log.d("DevelopmentSettings", "onPreferenceTreeClick() set real_debug_state 0");
                }
                //gionee wangyy 20120714 modify for CR00643075 end
                mVerifyAppsOverUsb.setEnabled(false);
                mVerifyAppsOverUsb.setChecked(false);
                updateBugreportOptions();
            }
        } else */
        // Add end
        if (preference == mBugreportInPower) {
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.BUGREPORT_IN_POWER_MENU, 
                    mBugreportInPower.isChecked() ? 1 : 0);
        } else if (preference == mKeepScreenOn) {
            Settings.Global.putInt(getActivity().getContentResolver(),
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    mKeepScreenOn.isChecked() ? 
                    (BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB) : 0);
            /*
        } else if (preference == mEnforceReadExternal) {
            if (mEnforceReadExternal.isChecked()) {
                ConfirmEnforceFragment.show(this);
            } else {
                setPermissionEnforced(getActivity(), READ_EXTERNAL_STORAGE, false);
            }
            */
        } else if (preference == mAllowMockLocation) {
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION,
                    mAllowMockLocation.isChecked() ? 1 : 0);
        } else if (preference == mDebugAppPref) {
            startActivityForResult(new Intent(getActivity(), AppPicker.class), RESULT_DEBUG_APP);
        } else if (preference == mWaitForDebugger) {
            writeDebuggerOptions();
        } else if (preference == mVerifyAppsOverUsb) {
            writeVerifyAppsOverUsbOptions();
        } else if (preference == mStrictMode) {
            writeStrictModeVisualOptions();
        } else if (preference == mPointerLocation) {
            writePointerLocationOptions();
        } else if (preference == mShowTouches) {
            writeShowTouchesOptions();
        } else if (preference == mShowScreenUpdates) {
            writeShowUpdatesOption();
        } else if (preference == mDisableOverlays) {
            writeDisableOverlaysOption();
        } else if (preference == mShowCpuUsage) {
            writeCpuUsageOptions();
        } else if (preference == mImmediatelyDestroyActivities) {
            writeImmediatelyDestroyActivitiesOptions();
        } else if (preference == mShowAllANRs) {
            writeShowAllANRsOptions();
        } else if (preference == mForceHardwareUi) {
            writeHardwareUiOptions();
        } else if (preference == mForceMsaa) {
            writeMsaaOptions();
        } else if (preference == mTrackFrameTime) {
            writeTrackFrameTimeOptions();
        } else if (preference == mShowHwScreenUpdates) {
            writeShowHwScreenUpdatesOptions();
        } else if (preference == mShowHwLayersUpdates) {
            writeShowHwLayersUpdatesOptions();
        } else if (preference == mShowHwOverdraw) {
            writeShowHwOverdrawOptions();
        } else if (preference == mDebugLayout) {
            writeDebugLayoutOptions();
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
        if (HDCP_CHECKING_KEY.equals(preference.getKey())) {
            SystemProperties.set(HDCP_CHECKING_PROPERTY, newValue.toString());
            updateHdcpValues();
            pokeSystemProperties();
            return true;
        } else if (preference == mWindowAnimationScale) {
            writeAnimationScaleOption(0, mWindowAnimationScale, newValue);
            return true;
        } else if (preference == mTransitionAnimationScale) {
            writeAnimationScaleOption(1, mTransitionAnimationScale, newValue);
            return true;
        } else if (preference == mAnimatorDurationScale) {
            writeAnimationScaleOption(2, mAnimatorDurationScale, newValue);
            return true;
        } else if (preference == mOverlayDisplayDevices) {
            writeOverlayDisplayDevicesOptions(newValue);
            return true;
        } else if (preference == mOpenGLTraces) {
            writeOpenGLTracesOptions(newValue);
            return true;
        } else if (preference == mEnableTracesPref) {
            writeEnableTracesOptions();
            return true;
        } else if (preference == mAppProcessLimit) {
            writeAppProcessLimitOptions(newValue);
            return true;
        }
        // Add begin by aurora.jiangmx
         else if (preference == mEnableAdb) {
    		boolean defState = Settings.Secure.getInt(getActivity().getContentResolver(),Settings.Secure.ADB_ENABLED, 0) != 0 ? true:false;
    		if (mGNUsbUISupport) {
    			defState =  Settings.Secure.getInt(getActivity().getContentResolver(), "real_debug_state",0) != 0 && defState? true:false;
               
            }
    		if(defState == (Boolean) newValue){
    			return true;
    		}
    		
    		
            if ((Boolean) newValue) {
                mOkClicked = false;
                if (mOkDialog != null) dismissDialogs();
                //Gionee:zhang_xin 2012-12-18 modify for CR00746738 start
                if (GnSettingsUtils.sGnSettingSupport) {
                    mOkDialog = new AuroraAlertDialog.Builder(getActivity(), AuroraAlertDialog.THEME_AMIGO_FULLSCREEN).setMessage(
                            getActivity().getResources().getString(R.string.adb_warning_message))
                            .setTitle(R.string.adb_warning_title)
                            .setPositiveButton(android.R.string.yes, this)
                            .setNegativeButton(android.R.string.no, this)
                            .setOnDismissListener(this)
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
                doRemoveDevPreferences();
            }
            
             return true;
          } else if (ENABLE_ART.equals(preference.getKey())) {

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
               int warning_message_id;
               
	           if((Boolean)newValue){
	        	   warning_message_id = R.string.select_runtime_warning_message;
	           }else{
	        	   warning_message_id = R.string.leave_art_runtime_warning_message;
	           }
	           
	            mArtDialog = new AuroraAlertDialog.Builder(getActivity(), AuroraAlertDialog.THEME_AMIGO_FULLSCREEN).setMessage(
	                    getActivity().getResources().getString(warning_message_id,oldRuntimeValue,newRuntimeValue))
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
	                }).show();
                    mArtDialog.setCanceledOnTouchOutside(false);
                    mArtDialog.setOnDismissListener(new OnDismissListener(){
                @Override
                public void onDismiss(DialogInterface dialog) {
                        updateRuntimeValue();
                        }
                 });

            }
            return true;
        }
		// Add end
        
        // qy add 2014 05 19 begin
        if(preference ==mEnableCom){        	
        	boolean isChecked = (Boolean)(newValue);
        	Log.i("qy", "isChecked = "+isChecked);
        	SharedPreferences iuniSP = getActivity().getSharedPreferences("iuni", Context.MODE_PRIVATE);
        	Editor et = iuniSP.edit();
        	et.putBoolean("imei", isChecked).commit();
        	String function = mUsbManager.getDefaultFunction();
        	if(isChecked){
        		
        		if (UsbManager.USB_FUNCTION_MTP.equals(function) || AURORA_USB_MTP.equals(function)) {
        			mUsbManager.setCurrentFunction(AURORA_USB_MTP, true);
        		}else if(UsbManager.USB_FUNCTION_PTP.equals(function)){
//        			mUsbManager.setCurrentFunction("ptp", true); // if config
        		}else{
        			mUsbManager.setCurrentFunction(USB_FUNCTION_CHARGING, true);
        		}
        	}else{
        		if (UsbManager.USB_FUNCTION_MTP.equals(function) || AURORA_USB_MTP.equals(function)) {
        			mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MTP, true);
        		}else if(UsbManager.USB_FUNCTION_PTP.equals(function)){
        			mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_PTP, true);
        		}else{
        			mUsbManager.setCurrentFunction(NO_USB_FUNCTION_CHARGING, true);
        		}
        	}
        	return true;
        }
        
     // qy add 2014 05 19 end
        return false;
    }
    // Add begin by aurora.jiangmx
    private void doRemoveDevPreferences(){
    	if(null != mBugreport)
    	  getPreferenceScreen().removePreference(mBugreport);
    	
    	if(null != mPassword)
    	  getPreferenceScreen().removePreference(mPassword);
    	
    	if(null != mKeepScreenOn)
    	  getPreferenceScreen().removePreference(mKeepScreenOn);
    	
    	if(null != mHdcpChecking)
    	  getPreferenceScreen().removePreference(mHdcpChecking);
    	
//    	if( null != mEnforceReadExternal)
//    	  getPreferenceScreen().removePreference(mEnforceReadExternal);
    	
    	if( null != mDebugCategory)
    	  getPreferenceScreen().removePreference(mDebugCategory);
    	
    	if( null != mInputCategory)
    	  getPreferenceScreen().removePreference(mInputCategory);
    	
    	if( null != mMonitoringCategory)
    	  getPreferenceScreen().removePreference(mMonitoringCategory);
    	
    	if( null != mAppCategory)
    	  getPreferenceScreen().removePreference(mAppCategory);
    	
    	if( null != mGNEnableScreen)
    	  getPreferenceScreen().removePreference(mGNEnableScreen);
    	
    	if( null != mDevBuildNumPref)
    	  getPreferenceScreen().removePreference(mDevBuildNumPref);
    	
    	if( null != mDebugDrawPref)
    	  getPreferenceScreen().removePreference(mDebugDrawPref);
    	
    }
    
    private void doAddPreferences(){
    	
    	if(null != mBugreport)
    	   getPreferenceScreen().addPreference(mBugreport);
    	
    	if(null != mPassword)
    	   getPreferenceScreen().addPreference(mPassword);
    	
    	if(null != mKeepScreenOn)
    	   getPreferenceScreen().addPreference(mKeepScreenOn);
    	
    	if( null != mHdcpChecking)
    	  getPreferenceScreen().addPreference(mHdcpChecking);
    	
//    	if( null != mEnforceReadExternal)
//    	  getPreferenceScreen().addPreference(mEnforceReadExternal);
    	
    	if( null != mDebugCategory)
    	  getPreferenceScreen().addPreference(mDebugCategory);
    	
    	if( null != mInputCategory)
    	  getPreferenceScreen().addPreference(mInputCategory);
    	
    	if( null != mMonitoringCategory)
    	  getPreferenceScreen().addPreference(mMonitoringCategory);
    	
    	if( null != mAppCategory)
    	  getPreferenceScreen().addPreference(mAppCategory);
    	
    	if( null != mGNEnableScreen)
    	  getPreferenceScreen().removePreference(mGNEnableScreen);
    	
    	if( null != mDevBuildNumPref)
    	  getPreferenceScreen().removePreference(mDevBuildNumPref);
    	
    	if( null != mDebugDrawPref)
    	  getPreferenceScreen().removePreference(mDebugDrawPref);
    }
	// Add end
    
    private void dismissDialogs() {
        if (mAdbDialog != null) {
            mAdbDialog.dismiss();
            mAdbDialog = null;
        }
        if (mEnableDialog != null) {
            mEnableDialog.dismiss();
            mEnableDialog = null;
        }
        
        // Add begin by aurora.jiangmx
        if (mOkDialog != null) {
            mOkDialog.dismiss();
            mOkDialog = null;
        }
		// Add end
    }

    public void onClick(DialogInterface dialog, int which) {
    	// Add begin by aurora.jiangmx
    	if (which == DialogInterface.BUTTON_POSITIVE) {
            mOkClicked = true;
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.ADB_ENABLED, 1);
            if (mGNUsbUISupport) {
                Settings.Secure.putInt(getActivity().getContentResolver(),
                    "real_debug_state", 1);
                Log.d("DevelopmentSettings", "onClick() set real_debug_state 1");
            }
          
            doAddPreferences();
        } else {
            // Reset the toggle
            mEnableAdb.setChecked(false);
        }
		// Add end
    	
        if (dialog == mAdbDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                mDialogClicked = true;
                Settings.Global.putInt(getActivity().getContentResolver(),
                        Settings.Global.ADB_ENABLED, 1);
           //gionee wangyy 20120714 modify for CR00643075 begin
            if (mGNUsbUISupport) {
                Settings.Secure.putInt(getActivity().getContentResolver(),
                    "real_debug_state", 1);
                Log.d("DevelopmentSettings", "onClick() set real_debug_state 1");
            }
            //gionee wangyy 20120714 modify for CR00643075 end
                mVerifyAppsOverUsb.setEnabled(true);
                updateVerifyAppsOverUsbOptions();
                updateBugreportOptions();
            } else {
                // Reset the toggle
                mEnableAdb.setChecked(false);
            }
        } else if (dialog == mEnableDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                mDialogClicked = true;
                Settings.Global.putInt(getActivity().getContentResolver(),
                        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
                mLastEnabledState = true;
                setPrefsEnabledState(mLastEnabledState);
            } else {
                // Reset the toggle
                mEnabledSwitch.setChecked(false);
            }
        }
    }

    public void onDismiss(DialogInterface dialog) {
        // Assuming that onClick gets called first
        if (dialog == mAdbDialog) {
            if (!mDialogClicked) {
                mEnableAdb.setChecked(false);
            }
            mAdbDialog = null;
        } else if (dialog == mEnableDialog) {
            if (!mDialogClicked) {
                mEnabledSwitch.setChecked(false);
            }
            mEnableDialog = null;
        } 
		// Add begin by aurora.jiangmx
		else if( dialog == mOkDialog ){
        	if(!mOkClicked){
        		mEnableAdb.setChecked(false);
        	}
        	mOkDialog = null;
        }
		// Add end
    }

    @Override
    public void onDestroy() {
        dismissDialogs();
        super.onDestroy();
    }
    
    // Add begin by aurora.jiangmx
    private void filterRuntimeOptions() {
//		File artModeFile = new File("/system/lib/" + RUNTIME_ART_MODE);
        String mDeviceName = SystemProperties.get("ro.product.name");
//		boolean isSupportArtMode = artModeFile.exists();
        if(!mDeviceName.contains("IUNI"))
//		if(!isSupportArtMode)
        {
			((AuroraPreferenceCategory)findPreference(USER_MODE_KEY)).removePreference(mEnableArt);
            mEnableArt = null ;
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
    // Add end
    
    void pokeSystemProperties() {
        if (!mDontPokeProperties) {
            //noinspection unchecked
            (new SystemPropPoker()).execute();
        }
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
                        Log.i("DevSettings", "Somone wrote a bad service '" + service
                                + "' that doesn't like to be poked: " + e);
                    }
                    data.recycle();
                }
            }
            return null;
        }
    }

    /**
     * Dialog to confirm enforcement of {@link android.Manifest.permission#READ_EXTERNAL_STORAGE}.
     */
    public static class ConfirmEnforceFragment extends DialogFragment {
        public static void show(DevelopmentSettings parent) {
            final ConfirmEnforceFragment dialog = new ConfirmEnforceFragment();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_ENFORCE);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(context);
            builder.setTitle(R.string.enforce_read_external_confirm_title);
            builder.setMessage(R.string.enforce_read_external_confirm_message);

            builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setPermissionEnforced(context, READ_EXTERNAL_STORAGE, true);
                    ((DevelopmentSettings) getTargetFragment()).updateAllOptions();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((DevelopmentSettings) getTargetFragment()).updateAllOptions();
                }
            });

            return builder.create();
        }
    }

    private static boolean isPermissionEnforced(String permission) {
        try {
            return ActivityThread.getPackageManager().isPermissionEnforced(permission);
        } catch (RemoteException e) {
            throw new RuntimeException("Problem talking with PackageManager", e);
        }
    }

    private static void setPermissionEnforced(
            Context context, String permission, boolean enforced) {
        try {
            // TODO: offload to background thread
            ActivityThread.getPackageManager()
                    .setPermissionEnforced(READ_EXTERNAL_STORAGE, enforced);
        } catch (RemoteException e) {
            throw new RuntimeException("Problem talking with PackageManager", e);
        }
    }

}
