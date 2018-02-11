/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.settings.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManagerNative;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.TextUtils.SimpleStringSplitter;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;
import android.os.SystemProperties;

import com.android.internal.content.PackageMonitor;
import com.android.internal.view.RotationPolicy;
import com.android.internal.view.RotationPolicy.RotationPolicyListener;
import com.android.settings.DialogCreatable;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.fingerprint.AuroraFingerprintUtils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.mediatek.settings.FeatureOption;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreferenceGroup;

import com.android.settings.AuroraSettingsPreferenceFragment;
import com.aurora.somatosensory.SomatosensorySettings;

import android.widget.LinearLayout;
import android.view.Gravity;

/**
 * Activity with the accessibility settings.
 */
public class FingerAndBodySettings extends AuroraSettingsPreferenceFragment implements DialogCreatable,
        AuroraPreference.OnPreferenceChangeListener, Indexable {

    private static final String TAG = "AccessibilitySettings";
    /// M: MTK fix fonts problem, CR ALPS00261477
    private static final float LARGE_FONT_SCALE_PHONE = 1.15f;
    private float LARGE_FONT_SCALE_TABLET = 1.30f;
    private boolean mIsScreenLarge = false;

    static final char ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ':';

    // Preference categories
    private static final String SYSTEM_CATEGORY = "system_category";

    // Preferences
    private static final String TOGGLE_LARGE_TEXT_PREFERENCE =
            "toggle_large_text_preference";
    private static final String TOGGLE_HIGH_TEXT_CONTRAST_PREFERENCE =
            "toggle_high_text_contrast_preference";
    private static final String TOGGLE_INVERSION_PREFERENCE =
            "toggle_inversion_preference";
    private static final String TOGGLE_POWER_BUTTON_ENDS_CALL_PREFERENCE =
            "toggle_power_button_ends_call_preference";
    private static final String TOGGLE_LOCK_SCREEN_ROTATION_PREFERENCE =
            "toggle_lock_screen_rotation_preference";
    private static final String TOGGLE_SPEAK_PASSWORD_PREFERENCE =
            "toggle_speak_password_preference";
    private static final String SELECT_LONG_PRESS_TIMEOUT_PREFERENCE =
            "select_long_press_timeout_preference";
    private static final String ENABLE_ACCESSIBILITY_GESTURE_PREFERENCE_SCREEN =
            "enable_global_gesture_preference_screen";
    private static final String CAPTIONING_PREFERENCE_SCREEN =
            "captioning_preference_screen";
    private static final String DISPLAY_MAGNIFICATION_PREFERENCE_SCREEN =
            "screen_magnification_preference_screen";
    private static final String DISPLAY_DALTONIZER_PREFERENCE_SCREEN =
            "daltonizer_preference_screen";
	// Add by jiyouguang begin
	private static final String TOGGLE_THREE_FINGERS_SCREENSHOTS = "three_fingers_screenshots";
	private static final String THREE_FINGERS_SCREENSHOTS = "three_finger_screenshot_enable";
	public static final String KEY_TWICE_CLICK_SCREEN_AWAKE_PREFERENCE ="toggle_twice_click_screen_awake_preference";
	public static final String KEY_GLOVE = "glove";
	public static final String KEY_SOMATOSENSORY_MUSIC = "somatosensory_music";
	
	private static final String TOGGLE_SOMATOSENSORY_SMART_PAUSE="somatosensory_pause";
	private static final String SOMATOSENSORY_SMART_PAUSE="somatosensory_smart_pause";
    private static final String CN_SOMATOSENSORY_SMART_PAUSE="cn_somatosensory_smart_pause";
    private static final String PHYFLIP="Phyflip";
    
	 public static final String LP_TWICE_CLICK_SCREEN_AWAKE = "/sys/bus/platform/devices/tp_wake_switch/double_wake"; 
	 public static final String GLOVE_PATH = "/sys/bus/platform/devices/tp_wake_switch/glove_enable"; 
	 public static final String FILE_SOMATOSENSORY_MUSIC = "/sys/bus/platform/devices/tp_wake_switch/gesture_for_music";
	// Add by jiyouguang end
    /// M: MTK add ipo settings
    private static final String IPO_SETTING_PREFERENCE = "ipo_setting";

    // Extras passed to sub-fragments.
    static final String EXTRA_PREFERENCE_KEY = "preference_key";
    static final String EXTRA_CHECKED = "checked";
    static final String EXTRA_TITLE = "title";
    static final String EXTRA_SUMMARY = "summary";
    static final String EXTRA_SETTINGS_TITLE = "settings_title";
    static final String EXTRA_COMPONENT_NAME = "component_name";
    static final String EXTRA_SETTINGS_COMPONENT_NAME = "settings_component_name";

    // Timeout before we update the services if packages are added/removed
    // since the AccessibilityManagerService has to do that processing first
    // to generate the AccessibilityServiceInfo we need for proper
    // presentation.
    private static final long DELAY_UPDATE_SERVICES_MILLIS = 1000;

    // Auxiliary members.
    final static SimpleStringSplitter sStringColonSplitter =
            new SimpleStringSplitter(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);

    static final Set<ComponentName> sInstalledServices = new HashSet<ComponentName>();

    private final Map<String, String> mLongPressTimeoutValuetoTitleMap =
            new HashMap<String, String>();

    private final Configuration mCurConfig = new Configuration();

    private final Handler mHandler = new Handler();

    private final Runnable mUpdateRunnable = new Runnable() {
        @Override
        public void run() {
        }
    };

    private final PackageMonitor mSettingsPackageMonitor = new PackageMonitor() {
        @Override
        public void onPackageAdded(String packageName, int uid) {
            sendUpdate();
        }

        @Override
        public void onPackageAppeared(String packageName, int reason) {
            sendUpdate();
        }

        @Override
        public void onPackageDisappeared(String packageName, int reason) {
            sendUpdate();
        }

        @Override
        public void onPackageRemoved(String packageName, int uid) {
            sendUpdate();
        }

        private void sendUpdate() {
            mHandler.postDelayed(mUpdateRunnable, DELAY_UPDATE_SERVICES_MILLIS);
        }
    };

    private final SettingsContentObserver mSettingsContentObserver =
            new SettingsContentObserver(mHandler) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    updateAllPreferences();
                }
            };

    private final RotationPolicyListener mRotationPolicyListener = new RotationPolicyListener() {
        @Override
        public void onChange() {
            updateLockScreenRotationCheckbox();
        }
    };

	// Preference controls.
	private AuroraPreferenceCategory mSystemsCategory;

	private AuroraCheckBoxPreference mToggleLargeTextPreference;
	private AuroraCheckBoxPreference mToggleHighTextContrastPreference;
	private AuroraSwitchPreference mTogglePowerButtonEndsCallPreference;
	private AuroraCheckBoxPreference mToggleLockScreenRotationPreference;
	private AuroraCheckBoxPreference mToggleSpeakPasswordPreference;
	private AuroraListPreference mSelectLongPressTimeoutPreference;
	private AuroraPreference mNoServicesMessagePreference;
	private AuroraPreferenceScreen mCaptioningPreferenceScreen;
	private AuroraPreferenceScreen mDisplayMagnificationPreferenceScreen;
	private AuroraPreferenceScreen mGlobalGesturePreferenceScreen;
	private AuroraPreferenceScreen mDisplayDaltonizerPreferenceScreen;
	private AuroraSwitchPreference mToggleInversionPreference;

	private AuroraSwitchPreference mSwitchLeds;

	private AuroraSwitchPreference mToggleThreeFingersScreenshots;
	private AuroraSwitchPreference mToggleTwiceClickScreenAwakePreference;
	private AuroraSwitchPreference mGlovePreference;
	private AuroraSwitchPreference mMusicPreference;
	private AuroraSwitchPreference mToggleSomatoSensorySmartPause;
	
	// / M: IPO preference
	private AuroraCheckBoxPreference mIpoSetting;

    private int mLongPressTimeoutDefault;

    private DevicePolicyManager mDpm;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        /** M: MTK fix fonts problem, CR ALPS00261477 @{ */
        int screenSize = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
        mIsScreenLarge = ((screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE)
                || (screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE));
         /** @} */

        addPreferencesFromResource(R.xml.finger_and_body_settings);
        initializeAllPreferences();
        mDpm = (DevicePolicyManager) (getActivity()
                .getSystemService(Context.DEVICE_POLICY_SERVICE));
				
        /** M: MTK fix fonts problem, CR ALPS01846045 @{ */		
        String[] array = getResources().getStringArray(R.array.entryvalues_font_size);
        if(array.length>0){
            LARGE_FONT_SCALE_TABLET = Float.parseFloat(array[array.length-1]);
            Log.d(TAG,"LARGE_FONT_SCALE_TABLET "+LARGE_FONT_SCALE_TABLET);
        }
		/** @} */
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAllPreferences();

        mSettingsPackageMonitor.register(getActivity(), getActivity().getMainLooper(), false);
        mSettingsContentObserver.register(getContentResolver());
        if (RotationPolicy.isRotationSupported(getActivity())) {
            RotationPolicy.registerRotationPolicyListener(getActivity(),
                    mRotationPolicyListener);
        }
        //modify by jiyouguang
        AuroraPreferenceCategory mCategory = (AuroraPreferenceCategory)getPreferenceScreen().findPreference("display_category");
        if(null != mCategory){
        	getPreferenceScreen().removePreference(mCategory);
        }
        //modify end
    }

    @Override
    public void onPause() {
        mSettingsPackageMonitor.unregister();
        mSettingsContentObserver.unregister(getContentResolver());
        if (RotationPolicy.isRotationSupported(getActivity())) {
            RotationPolicy.unregisterRotationPolicyListener(getActivity(),
                    mRotationPolicyListener);
        }
        super.onPause();
    }

    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
        if (mSelectLongPressTimeoutPreference == preference) {
            handleLongPressTimeoutPreferenceChange((String) newValue);
            return true;
        } else if (mToggleInversionPreference == preference) {
            handleToggleInversionPreferenceChange((Boolean) newValue);
            return true;
        } else if (preference.getKey().equals(TOGGLE_THREE_FINGERS_SCREENSHOTS)) {
			final boolean isChecked = (Boolean) newValue;
			if (isChecked) {
				Settings.System.putInt(getContentResolver(),
						THREE_FINGERS_SCREENSHOTS, 1);
			} else {
				Settings.System.putInt(getContentResolver(),
						THREE_FINGERS_SCREENSHOTS, 0);
			}
			return true;
		} else if(preference.getKey().equals(KEY_TWICE_CLICK_SCREEN_AWAKE_PREFERENCE)){
			final boolean isChecked = (Boolean) newValue;
			if (isChecked) {
				writePreferenceClick(true, LP_TWICE_CLICK_SCREEN_AWAKE,KEY_TWICE_CLICK_SCREEN_AWAKE_PREFERENCE);
			} else {
				writePreferenceClick(false, LP_TWICE_CLICK_SCREEN_AWAKE,KEY_TWICE_CLICK_SCREEN_AWAKE_PREFERENCE);
			}
			return true;
			
		}else if(preference.getKey().equals(KEY_GLOVE)){
			final boolean isChecked = (Boolean) newValue;
			if (isChecked) {
				writePreferenceClick(true, GLOVE_PATH,KEY_GLOVE);
			} else {
				writePreferenceClick(false, GLOVE_PATH,KEY_GLOVE);
			}
			return true;
			
		}else if(preference.getKey().equals(KEY_SOMATOSENSORY_MUSIC)){
			final boolean isChecked = (Boolean) newValue;
			
			if (AuroraFingerprintUtils.isFingerPrintSupported()) {
				if (isChecked) {
					Settings.System.putInt(getContentResolver(), KEY_SOMATOSENSORY_MUSIC,1);
				} else {
					Settings.System.putInt(getContentResolver(), KEY_SOMATOSENSORY_MUSIC, 0);
				}
			}else{
				if (isChecked) {
					writePreferenceClick(true, FILE_SOMATOSENSORY_MUSIC,KEY_SOMATOSENSORY_MUSIC);
				} else {
					writePreferenceClick(false, FILE_SOMATOSENSORY_MUSIC,KEY_SOMATOSENSORY_MUSIC);
				}
			}
			
			return true;
			
		}else if(preference.getKey().equals(TOGGLE_SOMATOSENSORY_SMART_PAUSE)){
        	boolean isChecked = (Boolean) newValue;
        	Intent gdintent=new Intent(CN_SOMATOSENSORY_SMART_PAUSE);
        	if(isChecked)
        	{
                Settings.Secure.putInt(getContentResolver(),SOMATOSENSORY_SMART_PAUSE, 1);
                gdintent.putExtra(PHYFLIP, true);
        	}else
        	{
        		Settings.Secure.putInt(getContentResolver(),SOMATOSENSORY_SMART_PAUSE, 0);
        		gdintent.putExtra(PHYFLIP, false);
        	}
        	getActivity().sendBroadcast(gdintent);
        	return true;
        }else if (mIpoSetting == preference) {
            /** M: mtk add ipo settings @{ */
            Settings.System.putInt(getContentResolver(), Settings.System.IPO_SETTING,
                    (Boolean) newValue ? 1 : 0);
                return true;
             /** @} */
                //-----add by jiyouguang-------------
        }else if(mTogglePowerButtonEndsCallPreference == preference){
            handleTogglePowerButtonEndsCallPreferenceClick((Boolean) newValue);
            return true;
        }
        //-------------end ---------------------
        return false;
    }
    // add by jiyouguang 
    private  void writePreferenceClick(boolean isChecked, String filePath,String key) {
    	FileOutputStream out = null;
    	File outFile = null;
    	try {
	    	outFile = new File(filePath);
	    	if(!outFile.exists()){
	    		return;
	    	}
	    	out = new FileOutputStream(outFile);
	    	if(isChecked) {
	    		
	    		out.write("1\n".getBytes());
	    	} else {
	    		out.write("0\n".getBytes());
	    	}

			if(key !=null){
		    	//获取到sharepreference 对象， 参数一为xml文件名，参数为文件的可操作模式  
				SharedPreferences iuniSP = getActivity().getSharedPreferences("iuni", Context.MODE_PRIVATE);  
				//获取到编辑对象  
				SharedPreferences.Editor edit = iuniSP.edit();  
				edit.putBoolean(key, isChecked);
				//提交.  
				edit.commit();
			}

	    	if (null != out) {  
				out.flush();  
				out.close();
		    }
	    } catch (Exception e) {
	    
			e.printStackTrace();
		}
    }
    //add end 

    private void handleLongPressTimeoutPreferenceChange(String stringValue) {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.LONG_PRESS_TIMEOUT, Integer.parseInt(stringValue));
        mSelectLongPressTimeoutPreference.setSummary(
                mLongPressTimeoutValuetoTitleMap.get(stringValue));
    }

    private void handleToggleInversionPreferenceChange(boolean checked) {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED, (checked ? 1 : 0));
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        if (mToggleLargeTextPreference == preference) {
            handleToggleLargeTextPreferenceClick();
            return true;
        } else if (mToggleHighTextContrastPreference == preference) {
            handleToggleTextContrastPreferenceClick();
            return true;
        } else if (mTogglePowerButtonEndsCallPreference == preference) {
           /* handleTogglePowerButtonEndsCallPreferenceClick();*/
            return true;
        } else if (mToggleLockScreenRotationPreference == preference) {
            handleLockScreenRotationPreferenceClick();
            return true;
        } else if (mToggleSpeakPasswordPreference == preference) {
            handleToggleSpeakPasswordPreferenceClick();
            return true;
        } else if (mGlobalGesturePreferenceScreen == preference) {
            handleToggleEnableAccessibilityGesturePreferenceClick();
            return true;
        } else if (mDisplayMagnificationPreferenceScreen == preference) {
            handleDisplayMagnificationPreferenceScreenClick();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void handleToggleLargeTextPreferenceClick() {
        /// M: MTK fix fonts problem, CR ALPS00261477 @{
        float updateFontScale = Settings.System.getFloat(getContentResolver(),
                Settings.System.FONT_SCALE_EXTRALARGE, -1);
        float fontScale = LARGE_FONT_SCALE_PHONE;
        if (updateFontScale == -1) {
            fontScale = mIsScreenLarge ? LARGE_FONT_SCALE_TABLET : LARGE_FONT_SCALE_PHONE;
        } else {
            fontScale = updateFontScale;
        }
        /// @}

        try {
            mCurConfig.fontScale = mToggleLargeTextPreference.isChecked() ? fontScale : 1;
            ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
        } catch (RemoteException re) {
            /* ignore */
        }
    }

    private void handleToggleTextContrastPreferenceClick() {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED,
                (mToggleHighTextContrastPreference.isChecked() ? 1 : 0));
    }

    private void handleTogglePowerButtonEndsCallPreferenceClick(boolean isChecked) {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                (/*mTogglePowerButtonEndsCallPreference.isChecked()*/isChecked
                        ? Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP
                        : Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_SCREEN_OFF));
    }

    private void handleLockScreenRotationPreferenceClick() {
        RotationPolicy.setRotationLockForAccessibility(getActivity(),
                !mToggleLockScreenRotationPreference.isChecked());
    }

    private void handleToggleSpeakPasswordPreferenceClick() {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_SPEAK_PASSWORD,
                mToggleSpeakPasswordPreference.isChecked() ? 1 : 0);
    }

    private void handleToggleEnableAccessibilityGesturePreferenceClick() {
        Bundle extras = mGlobalGesturePreferenceScreen.getExtras();
        extras.putString(EXTRA_TITLE, getString(
                R.string.accessibility_global_gesture_preference_title));
        extras.putString(EXTRA_SUMMARY, getString(
                R.string.accessibility_global_gesture_preference_description));
        extras.putBoolean(EXTRA_CHECKED, Settings.Global.getInt(getContentResolver(),
                Settings.Global.ENABLE_ACCESSIBILITY_GLOBAL_GESTURE_ENABLED, 0) == 1);
        super.onPreferenceTreeClick(mGlobalGesturePreferenceScreen,
                mGlobalGesturePreferenceScreen);
    }

    private void handleDisplayMagnificationPreferenceScreenClick() {
        Bundle extras = mDisplayMagnificationPreferenceScreen.getExtras();
        extras.putString(EXTRA_TITLE, getString(
                R.string.accessibility_screen_magnification_title));
        extras.putCharSequence(EXTRA_SUMMARY, getActivity().getResources().getText(
                R.string.accessibility_screen_magnification_summary));
        extras.putBoolean(EXTRA_CHECKED, Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED, 0) == 1);
        super.onPreferenceTreeClick(mDisplayMagnificationPreferenceScreen,
                mDisplayMagnificationPreferenceScreen);
    }

	private void initializeAllPreferences() {
		mSystemsCategory = (AuroraPreferenceCategory) findPreference(SYSTEM_CATEGORY);

		// Large text.
		mToggleLargeTextPreference = (AuroraCheckBoxPreference) findPreference(TOGGLE_LARGE_TEXT_PREFERENCE);
		//-------------remove "toggle_large_text_preference" from by JM begin-------------
		if(null != mToggleLargeTextPreference){
			mSystemsCategory.removePreference(mToggleLargeTextPreference);
		}
		//-------------remove "toggle_large_text_preference" from by JM end-------------

		// Text contrast.
		mToggleHighTextContrastPreference = (AuroraCheckBoxPreference) findPreference(TOGGLE_HIGH_TEXT_CONTRAST_PREFERENCE);
		//-------------remove ""toggle_high_text_contrast_preference"" from by JM begin-------------
		if(null != mToggleHighTextContrastPreference){
		   mSystemsCategory.removePreference(mToggleHighTextContrastPreference);
		}
	    //-------------remove ""toggle_high_text_contrast_preference"" from by JM end-------------

		// Display inversion.
		mToggleInversionPreference = (AuroraSwitchPreference) findPreference(TOGGLE_INVERSION_PREFERENCE);
		mToggleInversionPreference.setOnPreferenceChangeListener(this);

		// Power button ends calls.
		mTogglePowerButtonEndsCallPreference = (AuroraSwitchPreference) findPreference(TOGGLE_POWER_BUTTON_ENDS_CALL_PREFERENCE);
		if (!KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER)
				|| !Utils.isVoiceCapable(getActivity())) {
			mSystemsCategory
					.removePreference(mTogglePowerButtonEndsCallPreference);
		}
		
		//Aurora hujianwei 20160216 modify for remove  power button ends call start
		mSystemsCategory.removePreference(mTogglePowerButtonEndsCallPreference);
		//Aurora hujianwei 20160216 modify for remove  power button ends call end

		
		// ---------------------add by jiyouguang -----------
		if(null != mTogglePowerButtonEndsCallPreference){
			mTogglePowerButtonEndsCallPreference.setOnPreferenceChangeListener(this);
		}
     //--------------------end ---------------------------
        // Lock screen rotation.
        mToggleLockScreenRotationPreference =
                (AuroraCheckBoxPreference) findPreference(TOGGLE_LOCK_SCREEN_ROTATION_PREFERENCE);
        if (!RotationPolicy.isRotationSupported(getActivity())) {
            mSystemsCategory.removePreference(mToggleLockScreenRotationPreference);
        }

		mSystemsCategory.removePreference(mToggleLockScreenRotationPreference);

		// Speak passwords.
		mToggleSpeakPasswordPreference = (AuroraCheckBoxPreference) findPreference(TOGGLE_SPEAK_PASSWORD_PREFERENCE);

        // Long press timeout.
        mSelectLongPressTimeoutPreference =
                (AuroraListPreference) findPreference(SELECT_LONG_PRESS_TIMEOUT_PREFERENCE);
        //modify by jiyouguang
        if(null != mSelectLongPressTimeoutPreference){
        	mSelectLongPressTimeoutPreference.setOnPreferenceChangeListener(this);
        }
        // end
        if (mLongPressTimeoutValuetoTitleMap.size() == 0) {
            String[] timeoutValues = getResources().getStringArray(
                    R.array.long_press_timeout_selector_values);
            mLongPressTimeoutDefault = Integer.parseInt(timeoutValues[0]);
            String[] timeoutTitles = getResources().getStringArray(
                    R.array.long_press_timeout_selector_titles);
            final int timeoutValueCount = timeoutValues.length;
            for (int i = 0; i < timeoutValueCount; i++) {
                mLongPressTimeoutValuetoTitleMap.put(timeoutValues[i], timeoutTitles[i]);
            }
        }

		// Captioning.
		mCaptioningPreferenceScreen = (AuroraPreferenceScreen) findPreference(CAPTIONING_PREFERENCE_SCREEN);
		//-------------remove "captioning_preference_screen" from by JM begin-------------
				mSystemsCategory.removePreference(mCaptioningPreferenceScreen);
		//-------------remove "captioning_preference_screen" from by JM end-------------

		// Display magnification.
		mDisplayMagnificationPreferenceScreen = (AuroraPreferenceScreen) findPreference(DISPLAY_MAGNIFICATION_PREFERENCE_SCREEN);
		//-------------remove "screen_magnification_preference_screen" from by JM begin-------------
			mSystemsCategory.removePreference(mDisplayMagnificationPreferenceScreen);
		//-------------remove "screen_magnification_preference_screen" from by JM end-------------

		// Display color adjustments.
		mDisplayDaltonizerPreferenceScreen = (AuroraPreferenceScreen) findPreference(DISPLAY_DALTONIZER_PREFERENCE_SCREEN);

		// Global gesture.
		mGlobalGesturePreferenceScreen = (AuroraPreferenceScreen) findPreference(ENABLE_ACCESSIBILITY_GESTURE_PREFERENCE_SCREEN);
		//-------------remove "enable_global_gesture_preference_screen" from by JM begin-------------
			mSystemsCategory.removePreference(mGlobalGesturePreferenceScreen);
	    //-------------remove "enable_global_gesture_preference_screen" from by JM end-------------
		final int longPressOnPowerBehavior = getActivity()
				.getResources()
				.getInteger(
						com.android.internal.R.integer.config_longPressOnPowerBehavior);
		final int LONG_PRESS_POWER_GLOBAL_ACTIONS = 1;
		if (!KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER)
				|| longPressOnPowerBehavior != LONG_PRESS_POWER_GLOBAL_ACTIONS) {
			// Remove accessibility shortcut if power key is not present
			// nor long press power does not show global actions menu.
			mSystemsCategory.removePreference(mGlobalGesturePreferenceScreen);
		}

        /// M: IPO settings
        mIpoSetting = (AuroraCheckBoxPreference) findPreference(IPO_SETTING_PREFERENCE);
        mIpoSetting.setOnPreferenceChangeListener(this);
        if (!FeatureOption.MTK_IPO_SUPPORT || UserHandle.myUserId() != UserHandle.USER_OWNER) {
            mSystemsCategory.removePreference(mIpoSetting);
        }
        //----------------------------add by  jiyouguang -------------------------------------
			mToggleThreeFingersScreenshots = (AuroraSwitchPreference) findPreference(TOGGLE_THREE_FINGERS_SCREENSHOTS);
		if (mToggleThreeFingersScreenshots != null) {
			mToggleThreeFingersScreenshots.setOnPreferenceChangeListener(this);
		}
		
		final boolean isExitThreeFingersScreenshots = SystemProperties
				.getBoolean("ro.aurora.threefingercapture", true);
		if (isExitThreeFingersScreenshots) {
			boolean isOpen = false;
			isOpen = Settings.System.getInt(getContentResolver(),
					THREE_FINGERS_SCREENSHOTS, -1) == 1 ? true : false;
			mToggleThreeFingersScreenshots.setChecked(isOpen);
		} else {
			mSystemsCategory.removePreference(mToggleThreeFingersScreenshots);
		}
		
		mToggleTwiceClickScreenAwakePreference = (AuroraSwitchPreference) findPreference(KEY_TWICE_CLICK_SCREEN_AWAKE_PREFERENCE);
		if (mToggleTwiceClickScreenAwakePreference != null) {
			mToggleTwiceClickScreenAwakePreference.setOnPreferenceChangeListener(this);
			mToggleTwiceClickScreenAwakePreference.setChecked(SomatosensorySettings.readPreferenceClick(LP_TWICE_CLICK_SCREEN_AWAKE));
		}
		
		mGlovePreference = (AuroraSwitchPreference) findPreference(KEY_GLOVE);
		if (mGlovePreference != null) {
			mGlovePreference.setOnPreferenceChangeListener(this);
			mGlovePreference.setChecked(SomatosensorySettings.readPreferenceClick(GLOVE_PATH));
		}
		
		mMusicPreference = (AuroraSwitchPreference) findPreference(KEY_SOMATOSENSORY_MUSIC);
		if (mMusicPreference != null) {
			mMusicPreference.setOnPreferenceChangeListener(this);
			
			if (AuroraFingerprintUtils.isFingerPrintSupported()) {
				mMusicPreference.setChecked(Settings.System.getInt(getContentResolver(),KEY_SOMATOSENSORY_MUSIC, 0) != 0);
			}else{
				mMusicPreference.setChecked(SomatosensorySettings.readPreferenceClick(FILE_SOMATOSENSORY_MUSIC));
			}
			
		}
		
		mToggleSomatoSensorySmartPause = (AuroraSwitchPreference) findPreference(TOGGLE_SOMATOSENSORY_SMART_PAUSE);
		if (mToggleSomatoSensorySmartPause != null) {
			mToggleSomatoSensorySmartPause.setOnPreferenceChangeListener(this);
			mToggleSomatoSensorySmartPause.setChecked(Settings.Secure.getInt(getContentResolver(),SOMATOSENSORY_SMART_PAUSE, -1) == 1);
		}
		//-----------------------------------------------add end -----------------------------------------------------------
    }

    private void updateAllPreferences() {
        updateSystemPreferences();
    }


    private void updateSystemPreferences() {
        // Large text.
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException re) {
            /* ignore */
        }

        mToggleHighTextContrastPreference.setChecked(
                Settings.Secure.getInt(getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED, 0) == 1);

        /// M: MTK fix fonts problem, CR ALPS00261477 @{
        float updateFontScale = Settings.System.getFloat(getContentResolver(),
                Settings.System.FONT_SCALE_EXTRALARGE, -1);
        boolean isChecked = false;
        if (updateFontScale == -1) {
            isChecked = mIsScreenLarge ? (mCurConfig.fontScale == LARGE_FONT_SCALE_TABLET)
                            : (mCurConfig.fontScale == LARGE_FONT_SCALE_PHONE);
        } else {
            isChecked = (mCurConfig.fontScale == updateFontScale);
        }
        mToggleLargeTextPreference.setChecked(isChecked);
        /// @}

        // If the quick setting is enabled, the preference MUST be enabled.
        mToggleInversionPreference.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED, 0) == 1);

        // Power button ends calls.
        if (KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER)
                && Utils.isVoiceCapable(getActivity())) {
            final int incallPowerBehavior = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_DEFAULT);
            final boolean powerButtonEndsCall =
                    (incallPowerBehavior == Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP);
            mTogglePowerButtonEndsCallPreference.setChecked(powerButtonEndsCall);
        }

        // Auto-rotate screen
        updateLockScreenRotationCheckbox();

        // Speak passwords.
        final boolean speakPasswordEnabled = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_SPEAK_PASSWORD, 0) != 0;
        // modify by jiyouguang
        if(null != mToggleSpeakPasswordPreference){
        	mToggleSpeakPasswordPreference.setChecked(speakPasswordEnabled);
        }
        //  add end 
        
        // Long press timeout.
        final int longPressTimeout = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LONG_PRESS_TIMEOUT, mLongPressTimeoutDefault);
        String value = String.valueOf(longPressTimeout);
        // modify by jiyouguang
        if(null != mSelectLongPressTimeoutPreference){
	        mSelectLongPressTimeoutPreference.setValue(value);
	        mSelectLongPressTimeoutPreference.setSummary(mLongPressTimeoutValuetoTitleMap.get(value));
        }
        //end
        updateFeatureSummary(Settings.Secure.ACCESSIBILITY_CAPTIONING_ENABLED,
                mCaptioningPreferenceScreen);
        updateFeatureSummary(Settings.Secure.ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED,
                mDisplayMagnificationPreferenceScreen);
        
      updateFeatureSummary(Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED,
                mDisplayDaltonizerPreferenceScreen);
  

        // Global gesture
        final boolean globalGestureEnabled = Settings.Global.getInt(getContentResolver(),
                Settings.Global.ENABLE_ACCESSIBILITY_GLOBAL_GESTURE_ENABLED, 0) == 1;
        if (globalGestureEnabled) {
            mGlobalGesturePreferenceScreen.setSummary(
                    R.string.accessibility_global_gesture_preference_summary_on);
        } else {
            mGlobalGesturePreferenceScreen.setSummary(
                    R.string.accessibility_global_gesture_preference_summary_off);
        }

        /// M: IPO Setting @{
        boolean ipoSettingEnabled = Settings.System.getInt(getContentResolver(),
                Settings.System.IPO_SETTING, 1) == 1;
        if (mIpoSetting != null) {
            mIpoSetting.setChecked(ipoSettingEnabled);
        }
        /// @} end
    }

    private void updateFeatureSummary(String prefKey, AuroraPreference pref) {
        final boolean enabled = Settings.Secure.getInt(getContentResolver(), prefKey, 0) == 1;
        pref.setSummary(enabled ? R.string.accessibility_feature_state_on
                : R.string.accessibility_feature_state_off);
    }

    private void updateLockScreenRotationCheckbox() {
        Context context = getActivity();
        if (context != null) {
            mToggleLockScreenRotationPreference.setChecked(
                    !RotationPolicy.isRotationLocked(context));
        }
    }


    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
               boolean enabled) {
            List<SearchIndexableResource> indexables = new ArrayList<SearchIndexableResource>();
            SearchIndexableResource indexable = new SearchIndexableResource(context);
            indexable.xmlResId = R.xml.finger_and_body_settings;
            indexables.add(indexable);
            return indexables;
        }
    };
}
