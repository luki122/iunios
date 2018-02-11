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

package com.android.settings;


import android.accessibilityservice.AccessibilityServiceInfo;
import aurora.widget.AuroraActionBar;
import android.app.ActionBar;
import aurora.app.AuroraActivity;
import android.app.Activity;
import android.app.ActivityManagerNative;
import aurora.app.AuroraAlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreferenceGroup;
import android.preference.Preference;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.TextUtils.SimpleStringSplitter;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.LinearLayout;
import aurora.widget.AuroraSwitch;
import android.widget.TextView;

import com.android.internal.content.PackageMonitor;
import com.android.internal.view.RotationPolicy;
import com.android.settings.AccessibilitySettings.ToggleSwitch.OnBeforeCheckedChangeListener;


import android.content.IntentFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.hardware.usb.UsbManager;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraPreferenceCategory;
import android.util.Log;
import android.os.Build;

/**
 * Activity with the accessibility settings.
 */
public class AccessibilitySettings extends SettingsPreferenceFragment implements DialogCreatable,
        AuroraPreference.OnPreferenceChangeListener ,  AuroraPreference.OnPreferenceClickListener {
    private static final String TAG="AccessibilitySettings";
	
    private static final String DEFAULT_SCREENREADER_MARKET_LINK =
            "market://search?q=pname:com.google.android.marvin.talkback";

    private static final float LARGE_FONT_SCALE = 1.3f;

    private static final String SYSTEM_PROPERTY_MARKET_URL = "ro.screenreader.market";

    // Timeout before we update the services if packages are added/removed since
    // the AccessibilityManagerService has to do that processing first to
    // generate
    // the AccessibilityServiceInfo we need for proper presentation.
    private static final long DELAY_UPDATE_SERVICES_MILLIS = 1000;

    private static final char ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ':';

    private static final String KEY_INSTALL_ACCESSIBILITY_SERVICE_OFFERED_ONCE =
            "key_install_accessibility_service_offered_once";

    // AuroraPreference categories
    private static final String SERVICES_CATEGORY = "services_category";
    private static final String SYSTEM_CATEGORY = "system_category";

    // Preferences
    private static final String TOGGLE_LARGE_TEXT_PREFERENCE =
            "toggle_large_text_preference";
    private static final String TOGGLE_POWER_BUTTON_ENDS_CALL_PREFERENCE =
            "toggle_power_button_ends_call_preference";
    private static final String TOGGLE_TWICE_CLICK_SCREEN_AWAKE_PREFERENCE =
            "toggle_twice_click_screen_awake_preference";
    private static final String TOGGLE_SCREEN_PICK_TAKE_PREFERENCE =
            "toggle_screen_pick_take_preference";
    private static final String TOGGLE_LOCK_SCREEN_ROTATION_PREFERENCE =
            "toggle_lock_screen_rotation_preference";
    private static final String TOGGLE_SPEAK_PASSWORD_PREFERENCE =
            "toggle_speak_password_preference";
    private static final String SELECT_LONG_PRESS_TIMEOUT_PREFERENCE =
            "select_long_press_timeout_preference";
    private static final String TOGGLE_SCRIPT_INJECTION_PREFERENCE =
            "toggle_script_injection_preference";
    private static final String ENABLE_ACCESSIBILITY_GESTURE_PREFERENCE_SCREEN =
            "enable_global_gesture_preference_screen";
    private static final String DISPLAY_MAGNIFICATION_PREFERENCE_SCREEN =
            "screen_magnification_preference_screen";
    
    //add by penggangding
    private static final String TOGGLE_SOMATOSENSORY_SMART_PAUSE="somatosensory_smart_pause";
    private static final String SOMATOSENSORY_SMART_PAUSE="somatosensory_smart_pause";
    private static final String CN_SOMATOSENSORY_SMART_PAUSE="cn_somatosensory_smart_pause";
    private static final String PHYFLIP="Phyflip";
    
    private static final String TOGGLE_THREE_FINGERS_SCREENSHOTS ="three_fingers_screenshots";
    private static final String THREE_FINGERS_SCREENSHOTS="three_finger_screenshot_enable";
    
	//add by steve.tang 2014-06-06, for kitkat. start
	//only for music control  String deviceName = SystemProperties.get("ro.gn.iuniznvernumber");
    static final String deviceName = SystemProperties.get("ro.gn.iuniznvernumber");
	
    private static final boolean MODEL_SUPPORT_MUSIC_CONTROL = deviceName.contains("i1") || deviceName.contains("U3-");
	
    private static final String TOGGLE_LOCK_SCREEN_MUSIC_CONTROL_PREFERENCE =
            "toggle_music_control_preference";

    private AuroraSwitchPreference mToggleMusicControlPreference;

	//change string to static final
    //private String twice_click_screen_awake = "/sys/bus/platform/devices/tp_wake_switch/double_wake";
    //private String screen_pick_take = "/sys/bus/platform/devices/tp_wake_switch/gesture_wake";

	//add for jellybean project
    private static final String JB_TWICE_CLICK_SCREEN_AWAKE = "/sys/bus/platform/devices/tp_wake_switch/double_wake"; // u2
    private static final String JB_SCREEN_PICK_TAKE = "/sys/bus/platform/devices/tp_wake_switch/gesture_wake";

	//add for kitkat project
    private static final String KK_TWICE_CLICK_SCREEN_AWAKE = "/sys/devices/platform/tp_wake_switch/double_wake"; // u3
    private static final String KK_SCREEN_PICK_TAKE = "/sys/devices/platform/tp_wake_switch/gesture_wake";

	//add for judgement file exist

	private static final String JB_FILE_PATH = "/sys/bus/platform/devices/tp_wake_switch";
	private static final String KK_FILE_PATH = "/sys/devices/platform/tp_wake_switch";

	//those string value change by android version, in method onCreate
    private String twice_click_screen_awake = JB_TWICE_CLICK_SCREEN_AWAKE;
    private String screen_pick_take = JB_SCREEN_PICK_TAKE;

	private File mFilePath = new File(JB_FILE_PATH);

	//add by steve.tang 2014-06-06, for kitkat. start

	//add by steve.tang 2014-06-06, for u3 music control. start
	private static final String KK_MUSIC_PRE_NEXT = "/sys/devices/platform/tp_wake_switch/cross_wake";
	private static final String KK_MUSIC_STOP_START = "/sys/devices/platform/tp_wake_switch/character_o_wake";
	//add by steve.tang 2014-06-06, for u3 music control. end
	
	// qy add 2014 06 28 begin
	private static final String GLOVE_PATH = "/sys/class/i2c-dev/i2c-2/device/2-0020/input/input1/glove_enable";
	private static final String GLOVE_PATH_U3M = "/sys/class/i2c-dev/i2c-2/device/2-0020/input/input0/glove_enable";
	private static final String KEY_GLOVE = "glove";
	private static final String GLOVE_USB_Toggle_PATH ="sys/class/i2c-dev/i2c-2/device/2-0020/input/input1/charger_enable";
	private static final String KEY_VIRTUAL_KEYBOARD = "virtual_keyboard";
	private static final String KEY_RETURN_KEYBOARD = "back_key";
	private AuroraPreference mVirtualKeyPref = null;

	// qy add 2014 06 28 end

	// Add begin by aurora.jiangmx
	public static final String NAVI_KEY_HIDE_ALLOW = "navigation_key_hide_allow";
	private AuroraSwitchPreference mVirtualKeySwitchPref;
	
	public static final int CLOSE_VIRTUAL_KEY_SWITCH = 0;
	public static final int OPEN_VIRTUAL_KEY_SWITCH = 1;
	
	public AuroraPreference mNotificationAccessPref;
    private static final String KEY_NOTIFICATION_ACCESS = "manage_notification_access";
    
    private static final String KEY_DEVICE_ADMIN_CATEGORY = "device_admin_category";
    
    private PackageManager mPM;
    // Add end
	
    // Extras passed to sub-fragments.
    private static final String EXTRA_PREFERENCE_KEY = "preference_key";
    private static final String EXTRA_CHECKED = "checked";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_SUMMARY = "summary";
    private static final String EXTRA_ENABLE_WARNING_TITLE = "enable_warning_title";
    private static final String EXTRA_ENABLE_WARNING_MESSAGE = "enable_warning_message";
    private static final String EXTRA_DISABLE_WARNING_TITLE = "disable_warning_title";
    private static final String EXTRA_DISABLE_WARNING_MESSAGE = "disable_warning_message";
    private static final String EXTRA_SETTINGS_TITLE = "settings_title";
    private static final String EXTRA_SETTINGS_COMPONENT_NAME = "settings_component_name";
    private static final String EXTRA_SERVICE_COMPONENT_NAME = "service_component_name";

    
    // Dialog IDs.
    private static final int DIALOG_ID_NO_ACCESSIBILITY_SERVICES = 1;

    // Auxiliary members.
    private final static SimpleStringSplitter sStringColonSplitter =
            new SimpleStringSplitter(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);

    private static final Set<ComponentName> sInstalledServices = new HashSet<ComponentName>();

    private final Map<String, String> mLongPressTimeoutValuetoTitleMap =
            new HashMap<String, String>();

    private final Configuration mCurConfig = new Configuration();

    private final PackageMonitor mSettingsPackageMonitor = new SettingsPackageMonitor();

    private final Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            loadInstalledServices();
            updateServicesPreferences();
        }
    };

    private final SettingsContentObserver mSettingsContentObserver =
            new SettingsContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            loadInstalledServices();
            updateServicesPreferences();
        }
    };

    private final RotationPolicy.RotationPolicyListener mRotationPolicyListener =
            new RotationPolicy.RotationPolicyListener() {
                @Override
                public void onChange() {
                    updateLockScreenRotationCheckbox();
                }
            };

    // AuroraPreference controls.
    private AuroraPreferenceCategory mServicesCategory;
    private AuroraPreferenceCategory mSystemsCategory;

    private AuroraCheckBoxPreference mToggleLargeTextPreference;
    private AuroraSwitchPreference mTogglePowerButtonEndsCallPreference;
    private AuroraSwitchPreference mToggleTwiceClickScreenAwakePreference;
    private AuroraSwitchPreference mToggleScreenPickTakePreference;
    private AuroraCheckBoxPreference mToggleLockScreenRotationPreference;
    private AuroraCheckBoxPreference mToggleSpeakPasswordPreference;
    private AuroraListPreference mSelectLongPressTimeoutPreference;
    private AccessibilityEnableScriptInjectionPreference mToggleScriptInjectionPreference;
    private AuroraPreference mNoServicesMessagePreference;
    private AuroraPreferenceScreen mDisplayMagnificationPreferenceScreen;
    private AuroraPreferenceScreen mGlobalGesturePreferenceScreen;
    //add by penggangding
    private AuroraSwitchPreference mToggleSomatoSensorySmartPause;  
    
    private AuroraSwitchPreference mToggleThreeFingersScreenshots;
    
    private int mLongPressTimeoutDefault;
    
    //glove
    private static boolean mIsChecked;
    private boolean isSaved;
    private final BroadcastReceiver mUsbStatesChangeReceiver =  new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals(UsbManager.ACTION_USB_STATE)) {
    			boolean connected = intent.getExtras().getBoolean(UsbManager.USB_CONNECTED);
    			SharedPreferences iuniSP = context.getSharedPreferences("iuni", Context.MODE_PRIVATE);   			
    			AuroraSwitchPreference glovePref = (AuroraSwitchPreference)getPreferenceScreen().findPreference(KEY_GLOVE);
    			if(glovePref != null){
    				if(!connected){    				
    					glovePref.setChecked(mIsChecked);
        				glovePref.setEnabled(true);
        				isSaved = false;
        			}else{
        				if(!isSaved){
        					mIsChecked = iuniSP.getBoolean(KEY_GLOVE, false);
        					isSaved = true;
        				}
        				
        				glovePref.setChecked(false);
        				glovePref.setEnabled(false);
        				
        			}
    			}
    			
			}
			
		}
    	
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.accessibility_settings);
		if(Build.VERSION.SDK_INT==19){
			twice_click_screen_awake = KK_TWICE_CLICK_SCREEN_AWAKE;
			screen_pick_take = KK_SCREEN_PICK_TAKE;
			mFilePath = new File(KK_FILE_PATH);
		}
		Log.d(TAG,"  device name="+deviceName);
		// Add begin by aurora.jiangmx
		mPM = getActivity().getPackageManager();
		// Add end
		
        initializeAllPreferences();
        updateAllPreferences();

        //Begin Add by gary.gou for bug 6472 
        SharedPreferences iuniSP = getActivity().getSharedPreferences("iuni", Context.MODE_PRIVATE);  
    	mIsChecked = iuniSP.getBoolean(KEY_GLOVE, false);
    	//End Add by gary.gou for bug 6472 
    }

    @Override
    public void onResume() {
    	loadInstalledServices();
    	updateAllPreferences();
    	offerInstallAccessibilitySerivceOnce();
        mSettingsPackageMonitor.register(getActivity(), getActivity().getMainLooper(), false);
        mSettingsContentObserver.register(getContentResolver());
        RotationPolicy.registerRotationPolicyListener(getActivity(),
                mRotationPolicyListener);
        super.onResume();
        // qy add 2014 07 21 begin 
        if(mVirtualKeyPref !=null){
        	 int temp;
        	if(deviceName.contains("i1")){
        		temp = Settings.System.getInt( getContentResolver(),AuroraVirtualKeySettings.KEY_MODE, 1);
        	}else{
        		temp = Settings.System.getInt( getContentResolver(),AuroraVirtualKeySettings.KEY_MODE, 0);
        	}
           if(temp == 1){
    			mVirtualKeyPref.auroraSetArrowText(getActivity().getResources().getString(R.string.virtual_key_setting_mode_right_pref_title), true);
    		}else{
    			
    			mVirtualKeyPref.auroraSetArrowText(getActivity().getResources().getString(R.string.virtual_key_setting_mode_left_pref_title), true);
    		}
        }
        
		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_STATE);
		getActivity().registerReceiver(mUsbStatesChangeReceiver,filter);
		// qy add 2014 07 21 end
		
		Log.v(TAG, "--mSystemsCategory.getPreferenceCount()====="+mSystemsCategory.getPreferenceCount());
		if(mSystemsCategory.getPreferenceCount() == 0){
			getPreferenceScreen().removePreference(mSystemsCategory);
		}
    }

    @Override
    public void onPause() {
        mSettingsPackageMonitor.unregister();
        RotationPolicy.unregisterRotationPolicyListener(getActivity(),
                mRotationPolicyListener);
        mSettingsContentObserver.unregister(getContentResolver());
        super.onPause();
        getActivity().unregisterReceiver(mUsbStatesChangeReceiver);
    }
    
    
    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
        if (preference == mSelectLongPressTimeoutPreference) {
            String stringValue = (String) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LONG_PRESS_TIMEOUT, Integer.parseInt(stringValue));
            mSelectLongPressTimeoutPreference.setSummary(
                    mLongPressTimeoutValuetoTitleMap.get(stringValue));
            return true;
        } else if (mTogglePowerButtonEndsCallPreference == preference) {
            handleTogglePowerButtonEndsCallPreferenceClick();
            return true;
        } else if (preference.getKey().equals(mToggleTwiceClickScreenAwakePreference.getKey())) {
        	boolean isChecked = (Boolean) newValue;
        	  
            writePreferenceClick(isChecked, twice_click_screen_awake ,"double_wake");
            return true;
        } else if (preference.getKey().equals(mToggleScreenPickTakePreference.getKey())) {
        	boolean isChecked = (Boolean) newValue;
            writePreferenceClick(isChecked, screen_pick_take,"gesture_wake");
            return true;
		// add by steve.tang 2014-06-23, add for lock screen music control, start
        } else if (preference.getKey().equals(mToggleMusicControlPreference.getKey())) {
        	boolean isChecked = (Boolean) newValue;
            writePreferenceClick(isChecked, KK_MUSIC_PRE_NEXT,"music_pre_next");
            writePreferenceClick(isChecked, KK_MUSIC_STOP_START,"music_stop_start");
            return true;
		// add by steve.tang 2014-06-23, add for lock screen music control, end
            // qy add begin 2014 06 28
        }else if (preference.getKey().equals(KEY_GLOVE)){
        	boolean isChecked = (Boolean) newValue;
        	if(deviceName.contains("U3-"))
        	{
        		writePreferenceClick(isChecked,GLOVE_PATH,KEY_GLOVE );
        	}else if(deviceName.contains("i1"))
        	{
        		writePreferenceClick(isChecked,GLOVE_PATH_U3M,KEY_GLOVE );
        	}
        	// save the state
        	boolean isEnable = readPreferenceClick(GLOVE_USB_Toggle_PATH);
        	if(!isEnable){
        		mIsChecked = isChecked;
        	}
        	return true;
        }
       // Add begin by aurora.jiangmx
        else if(preference.getKey().equals(NAVI_KEY_HIDE_ALLOW)){
            if(!mVirtualKeySwitchPref.isChecked()){
                Settings.System.putInt(getActivity().getContentResolver(), NAVI_KEY_HIDE_ALLOW, OPEN_VIRTUAL_KEY_SWITCH);
            }else{
                Settings.System.putInt(getActivity().getContentResolver(), NAVI_KEY_HIDE_ALLOW, CLOSE_VIRTUAL_KEY_SWITCH);
            }
            return true;
        }
        // Add end
        //add by penggangding
        else if(preference.getKey().equals(TOGGLE_SOMATOSENSORY_SMART_PAUSE))
        {
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
        }
        else if(preference.getKey().equals(TOGGLE_THREE_FINGERS_SCREENSHOTS))
        {
        	final boolean isChecked = (Boolean) newValue;
        	if(isChecked)
        	{
        		Settings.System.putInt(getContentResolver(),THREE_FINGERS_SCREENSHOTS, 1);
        	}else
        	{
        		Settings.System.putInt(getContentResolver(),THREE_FINGERS_SCREENSHOTS, 0);
        	}
        	return true;
        }
        return true;
    }
    
    private void writePreferenceClick(boolean isChecked, String filePath ,String key) {
    	FileOutputStream out = null;
    	File outFile = null;
    	try {
	    	outFile = new File(filePath);
	    	out = new FileOutputStream(outFile);
	    	if(isChecked) {
	    		out.write("1\n".getBytes());
	    	} else {
	    		out.write("0\n".getBytes());
	    	}
	    	
	    	//获取到sharepreference 对象， 参数一为xml文件名，参数为文件的可操作模式  
			SharedPreferences iuniSP = getActivity().getSharedPreferences("iuni", Context.MODE_PRIVATE);  
			//获取到编辑对象  
			SharedPreferences.Editor edit = iuniSP.edit();  
			//添加新的值，将选择的图片地址保存 double_wake  gesture_wake modify qy
			/*if(filePath.contains("double_wake")) {
				edit.putBoolean("double_wake", isChecked);
			}  else if(filePath.contains("gesture_wake")) {
				edit.putBoolean("gesture_wake", isChecked);
			}*/
			if(key !=null){
				edit.putBoolean(key, isChecked);
			}
			
			//提交.  
			edit.commit();
	    	
	    	if (null != out) {  
				out.flush();  
				out.close();
		    }
	    } catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private boolean readPreferenceClick(String filePath) {
    	File file = new File(filePath);
        BufferedReader reader = null;
        String fileString = null;
        boolean isChecked = false;
        String resultString = null;
        try {
            // 一次读一个字节
            reader = new BufferedReader(new FileReader(file));
            
            while ((fileString = reader.readLine()) != null) {
            	resultString = fileString;
            }
            if (null != reader) {  
            	reader.close();
		    }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(resultString==null)
        {
	        return false ;
        }
		if(resultString.length() != 1){
			int index = resultString.indexOf(",");
			if(index == 1) {
				resultString = resultString.substring(0, index);
			}
		}
        if(resultString.equals("1")) {
        	isChecked = true;
        } else {
        	isChecked = false;
        }
        return isChecked;
    }
    
    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        if (mToggleLargeTextPreference == preference) {
            handleToggleLargeTextPreferenceClick();
            return true;
        }else if (mToggleLockScreenRotationPreference == preference) {
            handleLockScreenRotationPreferenceClick();
            return true;
        } else if (mToggleSpeakPasswordPreference == preference) {
            handleToggleSpeakPasswordPreferenceClick();
            return true;
        } else if (mGlobalGesturePreferenceScreen == preference) {
            handleTogglEnableAccessibilityGesturePreferenceClick();
            return true;
        } else if (mDisplayMagnificationPreferenceScreen == preference) {
            handleDisplayMagnificationPreferenceScreenClick();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void handleToggleLargeTextPreferenceClick() {
        try {
            mCurConfig.fontScale = mToggleLargeTextPreference.isChecked() ? LARGE_FONT_SCALE : 1;
            ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
        } catch (RemoteException re) {
            /* ignore */
        }
    }

    private void handleTogglePowerButtonEndsCallPreferenceClick() {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                (!mTogglePowerButtonEndsCallPreference.isChecked()
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

    private void handleTogglEnableAccessibilityGesturePreferenceClick() {
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
        mServicesCategory = (AuroraPreferenceCategory) findPreference(SERVICES_CATEGORY);
        mSystemsCategory = (AuroraPreferenceCategory) findPreference(SYSTEM_CATEGORY);

        // Large text.
        mToggleLargeTextPreference =
                (AuroraCheckBoxPreference) findPreference(TOGGLE_LARGE_TEXT_PREFERENCE);
        //Gionee <wangguojing> <2013-08-05> add for CR00846070 begin 
        if(mToggleLargeTextPreference != null){
            mSystemsCategory.removePreference(mToggleLargeTextPreference);
        }
        //Gionee <wangguojing> <2013-08-05> add for CR00846070 end 

        // Power button ends calls.
        mTogglePowerButtonEndsCallPreference =
                (AuroraSwitchPreference) findPreference(TOGGLE_POWER_BUTTON_ENDS_CALL_PREFERENCE);
		if(mTogglePowerButtonEndsCallPreference != null) {
			mTogglePowerButtonEndsCallPreference.setOnPreferenceChangeListener(this);
		}
        if (!KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER)
                || !Utils.isVoiceCapable(getActivity())) {
            mSystemsCategory.removePreference(mTogglePowerButtonEndsCallPreference);
        }
        
        mToggleTwiceClickScreenAwakePreference =
                (AuroraSwitchPreference) findPreference(TOGGLE_TWICE_CLICK_SCREEN_AWAKE_PREFERENCE);
        if(mToggleTwiceClickScreenAwakePreference != null) {
        	mToggleTwiceClickScreenAwakePreference.setOnPreferenceChangeListener(this);
    	}
        
        mToggleScreenPickTakePreference =
                (AuroraSwitchPreference) findPreference(TOGGLE_SCREEN_PICK_TAKE_PREFERENCE);
        if(mToggleScreenPickTakePreference != null) {
        	mToggleScreenPickTakePreference.setOnPreferenceChangeListener(this);
    	}
        
        if(!mFilePath.exists()){
        	mSystemsCategory.removePreference(mToggleTwiceClickScreenAwakePreference);
        	mSystemsCategory.removePreference(mToggleScreenPickTakePreference);
	    }

		// add by steve.tang 2014-06-23, add for lock screen music control, start
        mToggleMusicControlPreference =
                (AuroraSwitchPreference) findPreference(TOGGLE_LOCK_SCREEN_MUSIC_CONTROL_PREFERENCE);
        if(mToggleMusicControlPreference != null) {
        	mToggleMusicControlPreference.setOnPreferenceChangeListener(this);
    	}
		if(!mFilePath.exists() || !MODEL_SUPPORT_MUSIC_CONTROL){
        	mSystemsCategory.removePreference(mToggleMusicControlPreference);
		}
		// add by steve.tang 2014-06-23, add for lock screen music control, end
        
        // Lock screen rotation.
        mToggleLockScreenRotationPreference =
                (AuroraCheckBoxPreference) findPreference(TOGGLE_LOCK_SCREEN_ROTATION_PREFERENCE);
        //Gionee <wangguojing> <2013-08-05> add for CR00846070 begin 
        if(mToggleLockScreenRotationPreference != null){
            mSystemsCategory.removePreference(mToggleLockScreenRotationPreference);
        }
        //Gionee <wangguojing> <2013-08-05> add for CR00846070 end 

        // Speak passwords.
        mToggleSpeakPasswordPreference =
                (AuroraCheckBoxPreference) findPreference(TOGGLE_SPEAK_PASSWORD_PREFERENCE);
        //Gionee <chenml> <2013-08-21> add for CR00867998 begin 
        if(mToggleSpeakPasswordPreference !=null){
            mSystemsCategory.removePreference(mToggleSpeakPasswordPreference);
        }
        //Gionee <chenml> <2013-08-21> add for CR00867998 end 
        // Long press timeout.
        mSelectLongPressTimeoutPreference =
                (AuroraListPreference) findPreference(SELECT_LONG_PRESS_TIMEOUT_PREFERENCE);
        mSelectLongPressTimeoutPreference.setOnPreferenceChangeListener(this);
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

        // Script injection.
        mToggleScriptInjectionPreference = (AccessibilityEnableScriptInjectionPreference)
                findPreference(TOGGLE_SCRIPT_INJECTION_PREFERENCE);
        if (mToggleScriptInjectionPreference != null) {
            mSystemsCategory.removePreference(mToggleScriptInjectionPreference);
        }

        // Display magnification.
        mDisplayMagnificationPreferenceScreen = (AuroraPreferenceScreen) findPreference(
                DISPLAY_MAGNIFICATION_PREFERENCE_SCREEN);

        // Global gesture.
        mGlobalGesturePreferenceScreen =
                (AuroraPreferenceScreen) findPreference(ENABLE_ACCESSIBILITY_GESTURE_PREFERENCE_SCREEN);

        // Gionee <wangyaohui><2013-04-04> add for CR00793465 begin
        if (findPreference("tts_settings_preference") != null) {
            mSystemsCategory.removePreference(findPreference("tts_settings_preference"));
        }
        if (mSelectLongPressTimeoutPreference != null) {
            mSystemsCategory.removePreference(mSelectLongPressTimeoutPreference);
        }
        if(mDisplayMagnificationPreferenceScreen != null) {
            mSystemsCategory.removePreference(mDisplayMagnificationPreferenceScreen);
        }
        if (mGlobalGesturePreferenceScreen != null) {
            mSystemsCategory.removePreference(mGlobalGesturePreferenceScreen);
        }
        // Gionee <wangyaohui><2013-04-04> add for CR00793465 end 

        if (mSystemsCategory != null) {
//             getPreferenceScreen().removePreference(mSystemsCategory);
        }
        
        //add by penggangding
        mToggleSomatoSensorySmartPause=(AuroraSwitchPreference) findPreference
        		(TOGGLE_SOMATOSENSORY_SMART_PAUSE);
        
/*        if(!MODEL_SUPPORT_MUSIC_CONTROL)
        {
        	mSystemsCategory.removePreference(mToggleSomatoSensorySmartPause);
        }*/
        
        if(mToggleSomatoSensorySmartPause != null) {
        	mToggleSomatoSensorySmartPause.setOnPreferenceChangeListener(this);
    	}
        
        final boolean isPause =Settings.Secure.getInt(getContentResolver(),SOMATOSENSORY_SMART_PAUSE, -1) == 1;
       
        if(isPause)
        {
        	mToggleSomatoSensorySmartPause.setChecked(isPause);
        }
        
        mToggleThreeFingersScreenshots=(AuroraSwitchPreference) findPreference
		        (TOGGLE_THREE_FINGERS_SCREENSHOTS );
        
        
        if(mToggleThreeFingersScreenshots!=null)
        {
        	mToggleThreeFingersScreenshots.setOnPreferenceChangeListener(this);
        }
        final boolean isExitThreeFingersScreenshots=SystemProperties.getBoolean("ro.aurora.threefingercapture", true);
        if(isExitThreeFingersScreenshots)
        {
        	boolean isOpen=false;
        	isOpen=Settings.System.getInt(getContentResolver(),THREE_FINGERS_SCREENSHOTS, 1)==1 ? true : false;
        	mToggleThreeFingersScreenshots.setChecked(isOpen);
        }else
        {
        	mSystemsCategory.removePreference(mToggleThreeFingersScreenshots);
        }

        // qy 2014 06 28 add begin
        // Modify begin by aurora.jiangmx
        /*String buildModel = Build.MODEL; 
         
        Log.i(TAG , "buildModel = "+buildModel);
        if(buildModel.contains("U3") || buildModel.contains("U4")){*/
        // ----------div------------
        String lDeviceName = SystemProperties.get("ro.gn.iuniznvernumber");
        //add by jiyouguang 
        boolean hasNavigationBar = (Settings.System.getInt(getContentResolver(), "has_navigation_bar", 0) == 1);
        if(lDeviceName.contains("i1")){ //i1添加返回键左右切换
        	// virtual key
        	mVirtualKeyPref = new AuroraPreference(getActivity());
        	mVirtualKeyPref.setTitle(R.string.return_key_setting_pref_title);
        	mVirtualKeyPref.setKey(KEY_RETURN_KEYBOARD);
        	mVirtualKeyPref.setOnPreferenceClickListener(this);
//        	mVirtualKeyPref.auroraSetArrowText("t", true);
        	mVirtualKeyPref.setOrder(0);
        	mSystemsCategory.addPreference(mVirtualKeyPref);
        	
        }else {
        if(hasNavigationBar){
        	// virtual key
        	mVirtualKeyPref = new AuroraPreference(getActivity());
        	mVirtualKeyPref.setTitle(R.string.virtual_key_setting_pref_title);
        	mVirtualKeyPref.setKey(KEY_VIRTUAL_KEYBOARD);
        	mVirtualKeyPref.setOnPreferenceClickListener(this);
//        	mVirtualKeyPref.auroraSetArrowText("t", true);
        	mVirtualKeyPref.setOrder(0);
        	mSystemsCategory.addPreference(mVirtualKeyPref);
        	// glove mode
        	
        	// Add begin by aurora.jiangmx
        	mVirtualKeySwitchPref = new AuroraSwitchPreference(getActivity());
        	mVirtualKeySwitchPref.setTitle(R.string.virtual_keyboard_switch_title);
        	mVirtualKeySwitchPref.setSummary(R.string.virtual_keyboard_switch_summary);
        	mVirtualKeySwitchPref.setKey(NAVI_KEY_HIDE_ALLOW);
        	mVirtualKeySwitchPref.setOrder(1);
        	mVirtualKeySwitchPref.setOnPreferenceChangeListener(this);
        	mSystemsCategory.addPreference(mVirtualKeySwitchPref);
        	
        	initVirtualPref();
            // Add end
          }
    }
        //modify end by jiyouguang
        
        if( lDeviceName.contains("U3")||lDeviceName.contains("i1")){
        // Modify end
       
        	AuroraSwitchPreference glovePref = new AuroraSwitchPreference(getActivity());
        	glovePref.setKey(KEY_GLOVE);
        	glovePref.setTitle(R.string.accessibility_glove_title);
        	try{
        		if(deviceName.contains("U3-"))
        		{
        			glovePref.setChecked(readPreferenceClick(GLOVE_PATH));
        		}else if(deviceName.contains("i1"))
        		{
        			glovePref.setChecked(readPreferenceClick(GLOVE_PATH_U3M));
        		}
        	}catch(Exception e){
        		glovePref.setChecked(false);
        	}        	
        	glovePref.setOnPreferenceChangeListener(this);
        	mSystemsCategory.addPreference(glovePref);
        }
        
        // Add begin by aurora.jiangmx
        AuroraPreferenceCategory deviceAdminCategory= (AuroraPreferenceCategory)
    			getPreferenceScreen().findPreference(KEY_DEVICE_ADMIN_CATEGORY);
    	
       
    	mNotificationAccessPref = findPreference(KEY_NOTIFICATION_ACCESS);
    	PlatformUtils lPutils = new PlatformUtils();
    	
    	if (mNotificationAccessPref != null) {
    		if( lPutils.isSupportNotification() ){
	            final int total = lPutils.getListenersCount(mPM);
	            if (total == 0 ) {
	                if (deviceAdminCategory != null) {
	                    deviceAdminCategory.removePreference(mNotificationAccessPref);
	                }
	            } else {
	                final int n = lPutils.getNumEnabledNotificationListeners(getActivity());
	                if (n == 0) {
	                	mNotificationAccessPref.setSummary(getResources().getString(
	                            R.string.manage_notification_access_summary_zero));
	                } else {
	                	mNotificationAccessPref.setSummary(String.format(getResources().getQuantityString(
	                            R.plurals.manage_notification_access_summary_nonzero,
	                            n, n)));
	                }
	            }
    		}else{
    			if (deviceAdminCategory != null) {
                    deviceAdminCategory.removePreference(mNotificationAccessPref);
                }
    		}
        }
        // Add end
    }
    
    // Add begin by aurora.jiangmx
    private void initVirtualPref(){
    	boolean lIsShow = Settings.System.getInt(getContentResolver(),NAVI_KEY_HIDE_ALLOW, 1) == 1 ? true : false;
    	mVirtualKeySwitchPref.setChecked(lIsShow);
    }
    // Add end
    
	@Override
	public boolean onPreferenceClick(AuroraPreference arg0) {
		// TODO Auto-generated method stub
		if(arg0.getKey().equals(KEY_VIRTUAL_KEYBOARD)){
			startFragment(this, AuroraVirtualKeySettings.class.getName(), 0, null
					,R.string.virtual_key_setting_pref_title);
		}else if(arg0.getKey().equals(KEY_RETURN_KEYBOARD)){
			startFragment(this, AuroraVirtualKeySettings.class.getName(), 0, null
					,R.string.return_key_setting_pref_title);
		}
		
		return false;
	}

    private void updateAllPreferences() {
        updateServicesPreferences();
        updateSystemPreferences();
    }

    private void updateServicesPreferences() {
        // Since services category is auto generated we have to do a pass
        // to generate it since services can come and go and then based on
        // the global accessibility state to decided whether it is enabled.

        // Generate.
        mServicesCategory.removeAll();

        AccessibilityManager accessibilityManager = AccessibilityManager.getInstance(getActivity());

        List<AccessibilityServiceInfo> installedServices =
                accessibilityManager.getInstalledAccessibilityServiceList();
        Set<ComponentName> enabledServices = getEnabledServicesFromSettings(getActivity());

        final boolean accessibilityEnabled = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1;

        for (int i = 0, count = installedServices.size(); i < count; ++i) {
            AccessibilityServiceInfo info = installedServices.get(i);

            AuroraPreferenceScreen preference = getPreferenceManager().createPreferenceScreen(
                    getActivity());
            String title = info.getResolveInfo().loadLabel(getPackageManager()).toString();

            ServiceInfo serviceInfo = info.getResolveInfo().serviceInfo;
            ComponentName componentName = new ComponentName(serviceInfo.packageName,
                    serviceInfo.name);

            preference.setKey(componentName.flattenToString());

            preference.setTitle(title);
            final boolean serviceEnabled = accessibilityEnabled
                    && enabledServices.contains(componentName);
            if (serviceEnabled) {
                preference.setSummary(getString(R.string.accessibility_feature_state_on));
            } else {
                preference.setSummary(getString(R.string.accessibility_feature_state_off));
            }

            preference.setOrder(i);
            preference.setFragment(ToggleAccessibilityServicePreferenceFragment.class.getName());
            preference.setPersistent(true);

            Bundle extras = preference.getExtras();
            extras.putString(EXTRA_PREFERENCE_KEY, preference.getKey());
            extras.putBoolean(EXTRA_CHECKED, serviceEnabled);
            extras.putString(EXTRA_TITLE, title);

            String description = info.loadDescription(getPackageManager());
            if (TextUtils.isEmpty(description)) {
                description = getString(R.string.accessibility_service_default_description);
            }
            extras.putString(EXTRA_SUMMARY, description);

            CharSequence applicationLabel = info.getResolveInfo().loadLabel(getPackageManager());

            extras.putString(EXTRA_ENABLE_WARNING_TITLE, getString(
                    R.string.accessibility_service_security_warning_title, applicationLabel));
            extras.putString(EXTRA_ENABLE_WARNING_MESSAGE, getString(
                    R.string.accessibility_service_security_warning_summary, applicationLabel));

            extras.putString(EXTRA_DISABLE_WARNING_TITLE, getString(
                    R.string.accessibility_service_disable_warning_title,
                    applicationLabel));
            extras.putString(EXTRA_DISABLE_WARNING_MESSAGE, getString(
                    R.string.accessibility_service_disable_warning_summary,
                    applicationLabel));

            String settingsClassName = info.getSettingsActivityName();
            if (!TextUtils.isEmpty(settingsClassName)) {
                extras.putString(EXTRA_SETTINGS_TITLE,
                        getString(R.string.accessibility_menu_item_settings));
                extras.putString(EXTRA_SETTINGS_COMPONENT_NAME,
                        new ComponentName(info.getResolveInfo().serviceInfo.packageName,
                                settingsClassName).flattenToString());
            }

            extras.putString(EXTRA_SERVICE_COMPONENT_NAME, componentName.flattenToString());

            mServicesCategory.addPreference(preference);
        }

        if (mServicesCategory.getPreferenceCount() == 0) {
            if (mNoServicesMessagePreference == null) {
                mNoServicesMessagePreference = new AuroraPreference(getActivity()) {
                    @Override
                    protected void onBindView(View view) {
                        super.onBindView(view);

                        LinearLayout containerView =
                                (LinearLayout) view.findViewById(R.id.message_container);
                        containerView.setGravity(Gravity.CENTER);

                        TextView summaryView = (TextView) view.findViewById(R.id.summary);
                        String title = getString(R.string.accessibility_no_services_installed);
                        summaryView.setText(title);
                    }
                };
                mNoServicesMessagePreference.setPersistent(false);
                mNoServicesMessagePreference.setLayoutResource(
                        R.layout.text_description_preference);
                mNoServicesMessagePreference.setSelectable(false);
            }
            mServicesCategory.addPreference(mNoServicesMessagePreference);
        }
    }

    private void updateSystemPreferences() {
        // Large text.
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException re) {
            /* ignore */
        }
        mToggleLargeTextPreference.setChecked(mCurConfig.fontScale == LARGE_FONT_SCALE);

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

        if(mFilePath.exists()){
        	mToggleTwiceClickScreenAwakePreference.setChecked(readPreferenceClick(twice_click_screen_awake));
            mToggleScreenPickTakePreference.setChecked(readPreferenceClick(screen_pick_take));
	    }

		// add by steve.tang 2014-06-23, add for lock screen music control, start
		if(mFilePath.exists() && MODEL_SUPPORT_MUSIC_CONTROL){
        	mToggleMusicControlPreference.setChecked(readPreferenceClick(KK_MUSIC_PRE_NEXT) && readPreferenceClick(KK_MUSIC_STOP_START));
		}
		// add by steve.tang 2014-06-23, add for lock screen music control, end

        
        // Auto-rotate screen
        updateLockScreenRotationCheckbox();

        // Speak passwords.
        final boolean speakPasswordEnabled = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_SPEAK_PASSWORD, 0) != 0;
        mToggleSpeakPasswordPreference.setChecked(speakPasswordEnabled);

        // Long press timeout.
        final int longPressTimeout = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LONG_PRESS_TIMEOUT, mLongPressTimeoutDefault);
        String value = String.valueOf(longPressTimeout);
        mSelectLongPressTimeoutPreference.setValue(value);
        mSelectLongPressTimeoutPreference.setSummary(mLongPressTimeoutValuetoTitleMap.get(value));

        // Script injection.
        final boolean scriptInjectionAllowed = (Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_SCRIPT_INJECTION, 0) == 1);
        mToggleScriptInjectionPreference.setInjectionAllowed(scriptInjectionAllowed);

        // Screen magnification.
        final boolean magnificationEnabled = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED, 0) == 1;
        if (magnificationEnabled) {
            mDisplayMagnificationPreferenceScreen.setSummary(
                    R.string.accessibility_feature_state_on);            
        } else {
            mDisplayMagnificationPreferenceScreen.setSummary(
                    R.string.accessibility_feature_state_off);
        }

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
    }

    private void updateLockScreenRotationCheckbox() {
        Context context = getActivity();
        if (context != null) {
            mToggleLockScreenRotationPreference.setChecked(
                    !RotationPolicy.isRotationLocked(context));
        }
    }

    private void offerInstallAccessibilitySerivceOnce() {
        // There is always one preference - if no services it is just a message.
        if (mServicesCategory.getPreference(0) != mNoServicesMessagePreference) {
            return;
        }
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        final boolean offerInstallService = !preferences.getBoolean(
                KEY_INSTALL_ACCESSIBILITY_SERVICE_OFFERED_ONCE, false);
        if (offerInstallService) {
            String screenreaderMarketLink = SystemProperties.get(
                    SYSTEM_PROPERTY_MARKET_URL,
                    DEFAULT_SCREENREADER_MARKET_LINK);
            Uri marketUri = Uri.parse(screenreaderMarketLink);
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);

            if (getPackageManager().resolveActivity(marketIntent, 0) == null) {
                // Don't show the dialog if no market app is found/installed.
                return;
            }

            preferences.edit().putBoolean(KEY_INSTALL_ACCESSIBILITY_SERVICE_OFFERED_ONCE,
                    true).commit();
            // Notify user that they do not have any accessibility
            // services installed and direct them to Market to get TalkBack.
            showDialog(DIALOG_ID_NO_ACCESSIBILITY_SERVICES);
        }
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case DIALOG_ID_NO_ACCESSIBILITY_SERVICES:
                return new AuroraAlertDialog.Builder(getActivity())
                        .setTitle(R.string.accessibility_service_no_apps_title)
                        .setMessage(R.string.accessibility_service_no_apps_message)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // dismiss the dialog before launching
                                        // the activity otherwise
                                        // the dialog removal occurs after
                                        // onSaveInstanceState which
                                        // triggers an exception
                                        removeDialog(DIALOG_ID_NO_ACCESSIBILITY_SERVICES);
                                        String screenreaderMarketLink = SystemProperties.get(
                                                SYSTEM_PROPERTY_MARKET_URL,
                                                DEFAULT_SCREENREADER_MARKET_LINK);
                                        Uri marketUri = Uri.parse(screenreaderMarketLink);
                                        Intent marketIntent = new Intent(Intent.ACTION_VIEW,
                                                marketUri);
                                        startActivity(marketIntent);
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
            default:
                return null;
        }
    }

    
    private void loadInstalledServices() {
        Set<ComponentName> installedServices = sInstalledServices;
        installedServices.clear();

        List<AccessibilityServiceInfo> installedServiceInfos =
                AccessibilityManager.getInstance(getActivity())
                        .getInstalledAccessibilityServiceList();
        if (installedServiceInfos == null) {
            return;
        }

        final int installedServiceInfoCount = installedServiceInfos.size();
        for (int i = 0; i < installedServiceInfoCount; i++) {
            ResolveInfo resolveInfo = installedServiceInfos.get(i).getResolveInfo();
            ComponentName installedService = new ComponentName(
                    resolveInfo.serviceInfo.packageName,
                    resolveInfo.serviceInfo.name);
            installedServices.add(installedService);
        }
    }

    private static Set<ComponentName> getEnabledServicesFromSettings(Context context) {
        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null) {
            enabledServicesSetting = "";
        }
        Set<ComponentName> enabledServices = new HashSet<ComponentName>();
        SimpleStringSplitter colonSplitter = sStringColonSplitter;
        colonSplitter.setString(enabledServicesSetting);
        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(
                    componentNameString);
            if (enabledService != null) {
                enabledServices.add(enabledService);
            }
        }
        return enabledServices;
    }

    private class SettingsPackageMonitor extends PackageMonitor {

        @Override
        public void onPackageAdded(String packageName, int uid) {
            Message message = mHandler.obtainMessage();
            mHandler.sendMessageDelayed(message, DELAY_UPDATE_SERVICES_MILLIS);
        }

        @Override
        public void onPackageAppeared(String packageName, int reason) {
            Message message = mHandler.obtainMessage();
            mHandler.sendMessageDelayed(message, DELAY_UPDATE_SERVICES_MILLIS);
        }

        @Override
        public void onPackageDisappeared(String packageName, int reason) {
            Message message = mHandler.obtainMessage();
            mHandler.sendMessageDelayed(message, DELAY_UPDATE_SERVICES_MILLIS);
        }

        @Override
        public void onPackageRemoved(String packageName, int uid) {
            Message message = mHandler.obtainMessage();
            mHandler.sendMessageDelayed(message, DELAY_UPDATE_SERVICES_MILLIS);
        }
    }

    public static class ToggleSwitch extends AuroraSwitch {

        private OnBeforeCheckedChangeListener mOnBeforeListener;

        public static interface OnBeforeCheckedChangeListener {
            public boolean onBeforeCheckedChanged(ToggleSwitch toggleSwitch, boolean checked);
        }

        public ToggleSwitch(Context context) {
            super(context);
        }

        public void setOnBeforeCheckedChangeListener(OnBeforeCheckedChangeListener listener) {
            mOnBeforeListener = listener;
        }

        @Override
        public void setChecked(boolean checked) {
            if (mOnBeforeListener != null
                    && mOnBeforeListener.onBeforeCheckedChanged(this, checked)) {
                return;
            }
            super.setChecked(checked);
        }

        public void setCheckedInternal(boolean checked) {
            super.setChecked(checked);
        }
    }

    public static class ToggleAccessibilityServicePreferenceFragment
            extends ToggleFeaturePreferenceFragment implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener, OnPreferenceChangeListener {

        private static final int DIALOG_ID_ENABLE_WARNING = 1;
        private static final int DIALOG_ID_DISABLE_WARNING = 2;

        private final SettingsContentObserver mSettingsContentObserver =
                new SettingsContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                String settingValue = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                final boolean enabled = settingValue.contains(mComponentName);
                mToggleSwitch.setCheckedInternal(enabled);
                setSwitchChecked(enabled);
            }
        };

        private CharSequence mEnableWarningTitle;
        private CharSequence mEnableWarningMessage;
        private CharSequence mDisableWarningTitle;
        private CharSequence mDisableWarningMessage;
        private Dialog mServiceDialog;

        private String mComponentName;

        private int mShownDialogId;
        
        private boolean mIsClick = false;
        private boolean isDismiss = false;
        AuroraSwitchPreference mServiceSwitch;
        // qy 2014 05 08 add begin
        private boolean mInitState;
        // qy 2014 05 08 add end
        //add auroraswitch as toggleSwitch 20131216
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            AuroraPreferenceCategory pc = new AuroraPreferenceCategory(getActivity());
            getPreferenceScreen().addPreference(pc);

            mServiceSwitch = new AuroraSwitchPreference(getActivity()); 
            mServiceSwitch.setKey("accessibility_service_switch");
           
            mServiceSwitch.setTitle(R.string.start_serivce_pref_title);
            getPreferenceScreen().addPreference(mServiceSwitch);
            mInitState = getArguments().getBoolean(EXTRA_CHECKED);
            mServiceSwitch.setChecked(getArguments().getBoolean(EXTRA_CHECKED)); //not use setSwitchCheck onCreate
//            Log getArguments()
            mServiceSwitch.setOnPreferenceChangeListener(this);
            
        }

        @Override
        public void onResume() {
            mSettingsContentObserver.register(getContentResolver());
            super.onResume();
            
        }

        @Override
        public void onPause() {
            mSettingsContentObserver.unregister(getContentResolver());
            super.onPause();
        }

        void setSwitchChecked(boolean bChecked) {
           // mIsClick = false;
            mServiceSwitch.setChecked(bChecked);
        }

        //add auroraswitch as toggleSwitch 20131216
        public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
        	// qy 2014 05 08 add begin
        	if(mInitState == (Boolean)newValue){
        		return true;
        	}
        	// qy 2014 05 08 add end
            if (mIsClick) {
            	mIsClick = false;
                return true;
            }
         
            if (preference.getKey().equals("accessibility_service_switch")) {
                boolean enabled = ((Boolean)newValue).booleanValue();

                if (enabled) {
                    if (!TextUtils.isEmpty(mEnableWarningMessage)) {
                        //toggleSwitch.setCheckedInternal(false);
                        getArguments().putBoolean(EXTRA_CHECKED, false);
                        serviceAppDialog(DIALOG_ID_ENABLE_WARNING);
                        //showDialog(DIALOG_ID_ENABLE_WARNING);
                        return true;
                    }
                    onPreferenceToggled(mPreferenceKey, true);
                } else {
                    if (!TextUtils.isEmpty(mDisableWarningMessage)) {
                        //toggleSwitch.setCheckedInternal(true);
                        getArguments().putBoolean(EXTRA_CHECKED, true);
                        serviceAppDialog(DIALOG_ID_DISABLE_WARNING);
                        //showDialog(DIALOG_ID_DISABLE_WARNING);
                        return true;
                    }
                    onPreferenceToggled(mPreferenceKey, false);
                }
            }

            return true;
        }

        @Override
        public void onPreferenceToggled(String preferenceKey, boolean enabled) {
            // Parse the enabled services.
            Set<ComponentName> enabledServices = getEnabledServicesFromSettings(getActivity());

            // Determine enabled services and accessibility state.
            ComponentName toggledService = ComponentName.unflattenFromString(preferenceKey);
            final boolean accessibilityEnabled;
            if (enabled) {
                // Enabling at least one service enables accessibility.
                accessibilityEnabled = true;
                enabledServices.add(toggledService);
            } else {
                // Check how many enabled and installed services are present.
                int enabledAndInstalledServiceCount = 0;
                Set<ComponentName> installedServices = sInstalledServices;
                for (ComponentName enabledService : enabledServices) {
                    if (installedServices.contains(enabledService)) {
                        enabledAndInstalledServiceCount++;
                    }
                }
                // Disabling the last service disables accessibility.
                accessibilityEnabled = enabledAndInstalledServiceCount > 1
                        || (enabledAndInstalledServiceCount == 1
                        && !installedServices.contains(toggledService));
                enabledServices.remove(toggledService);
            }

            // Update the enabled services setting.
            StringBuilder enabledServicesBuilder = new StringBuilder();
            // Keep the enabled services even if they are not installed since we
            // have no way to know whether the application restore process has
            // completed. In general the system should be responsible for the
            // clean up not settings.
            for (ComponentName enabledService : enabledServices) {
                enabledServicesBuilder.append(enabledService.flattenToString());
                enabledServicesBuilder.append(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
            }
            final int enabledServicesBuilderLength = enabledServicesBuilder.length();
            if (enabledServicesBuilderLength > 0) {
                enabledServicesBuilder.deleteCharAt(enabledServicesBuilderLength - 1);
            }
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    enabledServicesBuilder.toString());

            // Update accessibility enabled.
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED, accessibilityEnabled ? 1 : 0);
        }

	private void serviceAppDialog(int dialogId) {
       	    CharSequence title = null;
            CharSequence message = null;
            switch (dialogId) {
                case DIALOG_ID_ENABLE_WARNING:
                    mShownDialogId = DIALOG_ID_ENABLE_WARNING;
                    title = mEnableWarningTitle;
                    message = mEnableWarningMessage;
                    break;
                case DIALOG_ID_DISABLE_WARNING:
                    mShownDialogId = DIALOG_ID_DISABLE_WARNING;
                    title = mDisableWarningTitle;
                    message = mDisableWarningMessage;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
		mServiceDialog = new AuroraAlertDialog.Builder(getActivity())
                    .setTitle(title)
                    //.setIconAttribute(android.R.attr.alertDialogIcon)
                    .setMessage(message)
                    //.setCancelable(true)
                    .setPositiveButton(android.R.string.ok, this)
                    .setNegativeButton(android.R.string.cancel, this)
                    .show();
		mServiceDialog.setOnDismissListener(this);
    }
/**
        @Override
        public Dialog onCreateDialog(int dialogId) {
            CharSequence title = null;
            CharSequence message = null;
            switch (dialogId) {
                case DIALOG_ID_ENABLE_WARNING:
                    mShownDialogId = DIALOG_ID_ENABLE_WARNING;
                    title = mEnableWarningTitle;
                    message = mEnableWarningMessage;
                    break;
                case DIALOG_ID_DISABLE_WARNING:
                    mShownDialogId = DIALOG_ID_DISABLE_WARNING;
                    title = mDisableWarningTitle;
                    message = mDisableWarningMessage;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
		mServiceDialog = new AuroraAlertDialog.Builder(getActivity())
                    .setTitle(title)
                    //.setIconAttribute(android.R.attr.alertDialogIcon)
                    .setMessage(message)
                    //.setCancelable(true)
                    .setPositiveButton(android.R.string.ok, this)
                    .setNegativeButton(android.R.string.cancel, this)
                    .show();
		android.util.Log.e("hanping", "mServiceDialog->");
		//mServiceDialog.setOnDismissListener(this);
            return mServiceDialog;
        }
*/
        @Override
        public void onClick(DialogInterface dialog, int which) {
            final boolean checked;
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    checked = (mShownDialogId == DIALOG_ID_ENABLE_WARNING);
                    mToggleSwitch.setCheckedInternal(checked);
                    setSwitchChecked(checked);
                    getArguments().putBoolean(EXTRA_CHECKED, checked);
                    onPreferenceToggled(mPreferenceKey, checked);
                    isDismiss = true;
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    checked = (mShownDialogId == DIALOG_ID_DISABLE_WARNING);
                    mToggleSwitch.setCheckedInternal(checked);
                    setSwitchChecked(checked);
                    getArguments().putBoolean(EXTRA_CHECKED, checked);
                    onPreferenceToggled(mPreferenceKey, checked);
				    mIsClick = true;
				    isDismiss = true;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
	
	@Override
	public void onDismiss(DialogInterface dialog) {
        // Assuming that onClick gets called first

	if(!isDismiss) {
		setSwitchChecked(getArguments().getBoolean(EXTRA_CHECKED));
		//mToggleAppInstallation.setChecked(false);
		mIsClick = true;
	}else {
		isDismiss = false;
	}
    	}

        @Override
        protected void onInstallActionBarToggleSwitch() {
            super.onInstallActionBarToggleSwitch();
            mToggleSwitch.setOnBeforeCheckedChangeListener(new OnBeforeCheckedChangeListener() {
                @Override
                public boolean onBeforeCheckedChanged(ToggleSwitch toggleSwitch, boolean checked) {
                    if (checked) {
                        if (!TextUtils.isEmpty(mEnableWarningMessage)) {
                            toggleSwitch.setCheckedInternal(false);
                            getArguments().putBoolean(EXTRA_CHECKED, false);
                            showDialog(DIALOG_ID_ENABLE_WARNING);
                            return true;
                        }
                        onPreferenceToggled(mPreferenceKey, true);
                    } else {
                        if (!TextUtils.isEmpty(mDisableWarningMessage)) {
                            toggleSwitch.setCheckedInternal(true);
                            getArguments().putBoolean(EXTRA_CHECKED, true);
                            showDialog(DIALOG_ID_DISABLE_WARNING);
                            return true;
                        }
                        onPreferenceToggled(mPreferenceKey, false);
                    }
                    return false;
                }
            });
        }

        @Override
        protected void onProcessArguments(Bundle arguments) {
            super.onProcessArguments(arguments);
            // Settings title and intent.
            String settingsTitle = arguments.getString(EXTRA_SETTINGS_TITLE);
            String settingsComponentName = arguments.getString(EXTRA_SETTINGS_COMPONENT_NAME);
            if (!TextUtils.isEmpty(settingsTitle) && !TextUtils.isEmpty(settingsComponentName)) {
                Intent settingsIntent = new Intent(Intent.ACTION_MAIN).setComponent(
                        ComponentName.unflattenFromString(settingsComponentName.toString()));
                if (!getPackageManager().queryIntentActivities(settingsIntent, 0).isEmpty()) {
                    mSettingsTitle = settingsTitle;
                    mSettingsIntent = settingsIntent;
                    setHasOptionsMenu(true);
                }
            }
            // Enable warning title.
            mEnableWarningTitle = arguments.getCharSequence(
                    AccessibilitySettings.EXTRA_ENABLE_WARNING_TITLE);
            // Enable warning message.
            mEnableWarningMessage = arguments.getCharSequence(
                    AccessibilitySettings.EXTRA_ENABLE_WARNING_MESSAGE);
            // Disable warning title.
            mDisableWarningTitle = arguments.getString(
                    AccessibilitySettings.EXTRA_DISABLE_WARNING_TITLE);
            // Disable warning message.
            mDisableWarningMessage = arguments.getString(
                    AccessibilitySettings.EXTRA_DISABLE_WARNING_MESSAGE);
            // Component name.
            mComponentName = arguments.getString(EXTRA_SERVICE_COMPONENT_NAME);
        }
    }

    public static class ToggleScreenMagnificationPreferenceFragment
            extends ToggleFeaturePreferenceFragment {
        @Override
        protected void onPreferenceToggled(String preferenceKey, boolean enabled) {
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED, enabled? 1 : 0);
        }

        @Override
        protected void onInstallActionBarToggleSwitch() {
            super.onInstallActionBarToggleSwitch();
            mToggleSwitch.setOnBeforeCheckedChangeListener(new OnBeforeCheckedChangeListener() {
                @Override
                public boolean onBeforeCheckedChanged(ToggleSwitch toggleSwitch, boolean checked) {
                    toggleSwitch.setCheckedInternal(checked);
                    getArguments().putBoolean(EXTRA_CHECKED, checked);
                    onPreferenceToggled(mPreferenceKey, checked);
                    return false;
                }
            });
        }
    }

    public static class ToggleGlobalGesturePreferenceFragment
            extends ToggleFeaturePreferenceFragment {
        @Override
        protected void onPreferenceToggled(String preferenceKey, boolean enabled) {
            Settings.Global.putInt(getContentResolver(),
                    Settings.Global.ENABLE_ACCESSIBILITY_GLOBAL_GESTURE_ENABLED, enabled ? 1 : 0);
        }

        @Override
        protected void onInstallActionBarToggleSwitch() {
            super.onInstallActionBarToggleSwitch();
            mToggleSwitch.setOnBeforeCheckedChangeListener(new OnBeforeCheckedChangeListener() {
                @Override
                public boolean onBeforeCheckedChanged(ToggleSwitch toggleSwitch, boolean checked) {
                    toggleSwitch.setCheckedInternal(checked);
                    getArguments().putBoolean(EXTRA_CHECKED, checked);
                    onPreferenceToggled(mPreferenceKey, checked);
                    return false;
                }
            });
        }
    }

    public static abstract class ToggleFeaturePreferenceFragment
            extends SettingsPreferenceFragment {

        protected ToggleSwitch mToggleSwitch;

        protected String mPreferenceKey;
        protected AuroraPreference mSummaryPreference;

        protected CharSequence mSettingsTitle;
        protected Intent mSettingsIntent;

        // TODO: Showing sub-sub fragment does not handle the activity title
        // so we do it but this is wrong. Do a real fix when there is time.
        private CharSequence mOldActivityTitle;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            AuroraPreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(
                    getActivity());
            setPreferenceScreen(preferenceScreen);
            mSummaryPreference = new AuroraPreference(getActivity()) {
                @Override
                protected void onBindView(View view) {
                    super.onBindView(view);
                    TextView summaryView = (TextView) view.findViewById(R.id.summary);
                    summaryView.setText(getSummary());
                    sendAccessibilityEvent(summaryView);
                }

                private void sendAccessibilityEvent(View view) {
                    // Since the view is still not attached we create, populate,
                    // and send the event directly since we do not know when it
                    // will be attached and posting commands is not as clean.
                    AccessibilityManager accessibilityManager =
                            AccessibilityManager.getInstance(getActivity());
                    if (accessibilityManager.isEnabled()) {
                        AccessibilityEvent event = AccessibilityEvent.obtain();
                        event.setEventType(AccessibilityEvent.TYPE_VIEW_FOCUSED);
                        view.onInitializeAccessibilityEvent(event);
                        view.dispatchPopulateAccessibilityEvent(event);
                        accessibilityManager.sendAccessibilityEvent(event);
                    }
                }
            };
            mSummaryPreference.setPersistent(false);
            mSummaryPreference.setLayoutResource(R.layout.text_description_preference);
            mSummaryPreference.setSelectable(false);
            preferenceScreen.addPreference(mSummaryPreference);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            onInstallActionBarToggleSwitch();
            onProcessArguments(getArguments());
            getListView().setDivider(null);
            //getListView().setEnabled(false);
        }

        @Override
        public void onDestroyView() {
            if (getActivity() != null && ((AuroraActivity)getActivity()).getAuroraActionBar() != null) {
//                ((AuroraActivity)getActivity()).getAuroraActionBar().setCustomView(null);
            }
            if (mOldActivityTitle != null) {
                if (getActivity() != null && ((AuroraActivity)getActivity()).getAuroraActionBar() != null) {
                    ((AuroraActivity)getActivity()).getAuroraActionBar().setTitle(mOldActivityTitle);
                }
            }
            mToggleSwitch.setOnBeforeCheckedChangeListener(null);
            super.onDestroyView();
        }

        protected abstract void onPreferenceToggled(String preferenceKey, boolean enabled);

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            MenuItem menuItem = menu.add(mSettingsTitle);
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menuItem.setIntent(mSettingsIntent);
        }

        protected void onInstallActionBarToggleSwitch() {
            mToggleSwitch = createAndAddActionBarToggleSwitch((AuroraActivity)getActivity());
        }

        private ToggleSwitch createAndAddActionBarToggleSwitch(AuroraActivity activity) {
            ToggleSwitch toggleSwitch = new ToggleSwitch(activity);
            final int padding = activity.getResources().getDimensionPixelSize(
                    R.dimen.action_bar_switch_padding);
            toggleSwitch.setPadding(0, 0, padding, 0);
            //AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
            /*
            activity.getAuroraActionBar().setDisplayOptions(AuroraActionBar.DISPLAY_SHOW_CUSTOM,
                    AuroraActionBar.DISPLAY_SHOW_CUSTOM);
            activity.getAuroraActionBar().setCustomView(toggleSwitch,
                    new AuroraActionBar.LayoutParams(AuroraActionBar.LayoutParams.WRAP_CONTENT,
                            AuroraActionBar.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER_VERTICAL | Gravity.END));
           */
             //AURORA-END::delete temporarily for compile::waynelin::2013-9-14
            return toggleSwitch;
        }

        protected void onProcessArguments(Bundle arguments) {
            // Key.
            mPreferenceKey = arguments.getString(EXTRA_PREFERENCE_KEY);
            // Enabled.
            final boolean enabled = arguments.getBoolean(EXTRA_CHECKED);
            mToggleSwitch.setCheckedInternal(enabled);
            // Title.
            AuroraPreferenceActivity activity = (AuroraPreferenceActivity) getActivity();
            if (!activity.onIsMultiPane() || activity.onIsHidingHeaders()) {
                mOldActivityTitle = getActivity().getTitle();
                String title = arguments.getString(EXTRA_TITLE);
                if (getActivity() != null && ((AuroraActivity)getActivity()).getAuroraActionBar() != null) {
                    ((AuroraActivity)getActivity()).getAuroraActionBar().setTitle(title);
                }
            }
            // Summary.
            CharSequence summary = arguments.getCharSequence(EXTRA_SUMMARY);
            mSummaryPreference.setSummary(summary);
        }
    }

    private static abstract class SettingsContentObserver extends ContentObserver {

        public SettingsContentObserver(Handler handler) {
            super(handler);
        }

        public void register(ContentResolver contentResolver) {
            contentResolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.ACCESSIBILITY_ENABLED), false, this);
            contentResolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES), false, this);
        }

        public void unregister(ContentResolver contentResolver) {
            contentResolver.unregisterContentObserver(this);
        }

        @Override
        public abstract void onChange(boolean selfChange, Uri uri);
    }


}
