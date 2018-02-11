package com.android.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.os.SystemProperties;

import com.android.settings.lscreen.AuroraLSManageModel;
import com.android.settings.lscreen.DataArrayList;
import com.android.settings.lscreen.LSBootService;
import com.android.settings.lscreen.ls.LSOperator;
import com.android.settings.wifi.AuroraAutoChangeAp;
import com.android.settings.wifi.WifiNotificationController;
import com.android.settings.wifi.WifiSettings;
import com.gionee.settings.utils.GnUtils;

public class AuroraSettingsInit{
	private AudioManager mAudioManager = null;
	private static final String ACTION_BATTERY_PERCENTAGE_SWITCH = "mediatek.intent.action.BATTERY_PERCENTAGE_SWITCH";
	private UsbManager mUsbManager;
	private static final String AURORA_USB_MTP = "mtp,diag";
	public static final String FONT_BOLD = "font_bold";

	// deal with the shortcut brightness
	public static final String ACTION_SEEKBAR_BRIGHTNESS_VALUE = "com.android.settings.action.SEEKBAR_BRIGHTNESS_VALUE";
	private boolean mIsRegisterAutoLightSwitchReceiver = false;
	// adb enable
	private static final boolean mGNUsbUISupport = SystemProperties.get(
			"ro.gn.usb.ui.support").equals("yes");

	private static final String HOURS_24 = "24";
	private static final String BUTTON_LIGHT_STATE = "gn_button_light"; // E6
	private static final String BUTTON_KEY_LIGHT = "button_key_light"; // U2 s4
	private static final String MI_BUTTON_KEY = "screen_buttons_state"; // M3
	private static final String BUTTON_LIGHT_MODE_1 = "button_light_mode"; // 1+
	private static final int DEFAULT_BUTTON_KEY_LIGHT = 1500;// 1.5s

	// jhp 2014 03 21
	private String twice_click_screen_awake = "/sys/bus/platform/devices/tp_wake_switch/double_wake";
	private String screen_pick_take = "/sys/bus/platform/devices/tp_wake_switch/gesture_wake";

	private static final String KK_MUSIC_PRE_NEXT = "/sys/devices/platform/tp_wake_switch/cross_wake";
	private static final String KK_MUSIC_STOP_START = "/sys/devices/platform/tp_wake_switch/character_o_wake";

	private static final String GLOVE_PATH = "/sys/class/i2c-dev/i2c-2/device/2-0020/input/input1/glove_enable";
	private static final String KEY_GLOVE = "glove";

	// Add begin by aurora.jiangmx
	private static final String SETTINGS_DEFAULT_PREF_FILE = "com.android.settings_preferences";
	// Add end

	private  final static String TAG = "AuroraSettingsInit"; 
	private  Context mContext;
	
	public AuroraSettingsInit(Context context){
		mContext = context;
	}
	
	private ContentResolver getContentResolver(){
		return mContext.getContentResolver();
	}
	
	public void init(){
		Log.v(TAG, "-----init()----enter---");
		mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
		
		if (SystemProperties.get("persist.sys.aurora.debug").trim().equals("")
				|| SystemProperties.get("persist.sys.aurora.debug") == null) {
			SystemProperties.set("persist.sys.aurora.debug", "yes");
		}

		// jhp 2014 03 21
		SharedPreferences iuniSP = mContext.getSharedPreferences("iuni",
				Context.MODE_PRIVATE);
		// m3 for restart the value changed;
		boolean state = iuniSP.getBoolean("aurora_buttonkey_light", true);
		try {
			String deviceName = SystemProperties.get("ro.gn.iuniznvernumber");
			if (deviceName.contains("MI4")) {
				Settings.Secure.putInt(getContentResolver(), MI_BUTTON_KEY, 0);
			} else {
				Settings.System.putInt(getContentResolver(),
						BUTTON_LIGHT_STATE, state ? 0 : 1);
				Settings.System.putInt(getContentResolver(), BUTTON_KEY_LIGHT,
						state ? DEFAULT_BUTTON_KEY_LIGHT : 0);
				Settings.Secure.putInt(getContentResolver(), MI_BUTTON_KEY,
						state ? 0 : 1);
				Settings.System.putInt(getContentResolver(),
						BUTTON_LIGHT_MODE_1, state ? 1 : 2);
			}

		} catch (Exception e) {
			// TODO: handle exception
			Log.e("AuroraLightSensorService",
					"can not write value in datebase.");
		}
		// m3 end

		boolean double_wake = iuniSP.getBoolean("double_wake", false); // default
		boolean gesture_wake = iuniSP.getBoolean("gesture_wake", false);
		File twice_click_screen_awake_fileFile = new File(
				twice_click_screen_awake);
		File screen_pick_take_fileFile = new File(screen_pick_take);
		if (twice_click_screen_awake_fileFile.exists()
				&& screen_pick_take_fileFile.exists()) {
			writePreferenceClick(double_wake, twice_click_screen_awake);
			writePreferenceClick(gesture_wake, screen_pick_take);
		}

		String buildModel = Build.MODEL;
		if (buildModel.equals("GT-I9500")) {
			String srcString = "/sys/class/mdnie/mdnie/mode";
			writeFileIntoHAL(1, srcString);
			writeFileIntoHAL(0, srcString);
		}
		// add glove function
		if (buildModel.contains("U3")||buildModel.contains("i1")) {
			boolean isChecked = iuniSP.getBoolean(KEY_GLOVE, false);
			File gloveFile = new File(GLOVE_PATH);
			if (gloveFile.exists()) {
				writePreferenceClick(isChecked, GLOVE_PATH);
			}
			// music control
			boolean isMusicPreNext = iuniSP.getBoolean("music_pre_next", false); // defalut
																					// value
			boolean isMusicStopStart = iuniSP.getBoolean("music_stop_start",
					false);
			File MusicPreFile = new File(KK_MUSIC_PRE_NEXT);
			File MusicStopFile = new File(KK_MUSIC_STOP_START);
			if (MusicPreFile.exists() && MusicStopFile.exists()) {
				writePreferenceClick(isMusicPreNext, KK_MUSIC_PRE_NEXT);
				writePreferenceClick(isMusicStopStart, KK_MUSIC_STOP_START);
			}
		}

		// set some init value
		Editor et = iuniSP.edit();
		
		boolean isFirstStart = iuniSP.getBoolean("is_first_start", true);
		Log.v(TAG, "-----init()----isFirstStart---=="+isFirstStart);
		
		if (isFirstStart) {


			mAudioManager.setParameters("srs_cfg:trumedia_enable=1;srs_cfg:trumedia_preset=0");
			Settings.System.putInt(getContentResolver(),  Settings.System.SOUND_EFFECTS_ENABLED, 1);
			
			
			Settings.Global.putInt(getContentResolver(),Settings.Global.DEVICE_PROVISIONED,1);
			
			//add by jiyouguang for development setting enable
	        Settings.Global.putInt(getContentResolver(),Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
	        //end
			
			mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 5, 0);	            
            // add notification volume
            mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 5, 0);
            // add system volume 
            mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 5, 0);
            // battery_percentage 
            Settings.Secure.putInt(getContentResolver(), "battery_percentage", 0);
            Intent intent = new Intent(ACTION_BATTERY_PERCENTAGE_SWITCH);
            intent.putExtra("state", 0);            
            mContext.sendBroadcast(intent);
            // adb enable modify by jiyouguang for abroad version
            if(!GnUtils.isAbroadVersion()){
	            Settings.Secure.putInt(getContentResolver(),
	                    Settings.Secure.ADB_ENABLED, 1);
	          //wolfu add for s4 4.4 home key problem
	            Settings.Secure.putInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 1);
	            Settings.System.putInt(getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,1 );
	            if (mGNUsbUISupport) {
	                Settings.Secure.putInt(getContentResolver(),
	                    "real_debug_state", 1);
	            }
            }else{
            	 Settings.Secure.putInt(getContentResolver(), Settings.Secure.BACKUP_ENABLED, 1);
            	 if (mGNUsbUISupport) {
 	                Settings.Secure.putInt(getContentResolver(),
 	                    "real_debug_state", 0);
 	            }
            	 Settings.Secure.putInt(getContentResolver(),
 	                    Settings.Secure.ADB_ENABLED, 0);
            }
            //modify end 

            // font bold
            Settings.System.putInt(getContentResolver(), "font_bold",0);
//            Settings.System.putInt(getContentResolver(), AuroraSettings.FONT_BOLD,0);
            
            // HOURS_24
            Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME,
                    1);
            Settings.Global.putInt(
                    getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 1);
            Settings.System.putString(getContentResolver(),
                    Settings.System.TIME_12_24,
                    HOURS_24);
            
            if(GnUtils.isAbroadVersion()){
            	Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT, 30000);
            }
            
            Settings.System.putInt(getContentResolver(), "intelligent_sleep_mode",0); //for samsung
            Settings.System.putString(getContentResolver(), Settings.System.DATE_FORMAT, "yyyy-MM-dd"); //for initialize date_format


			/*
            //wolfu move this to usb state receive
            // imei
            String deviceName = SystemProperties.get("ro.product.name");

            if (deviceName.contains("IUNI")) {
            	 boolean imeiState = true;
                 TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                 String imei = tm.getDeviceId();
			Log.v(TAG, "*****setMtpMode**** imei = " + imei);
                 if(imei !=null  ){
                	 if(imei.length()>6){
                		 imeiState = false;
                	 }
             	}
                 et.putBoolean("imei", imeiState);
			Log.v(TAG, "*****setMtpMode**** imeiState = " + imeiState);
            	if(imeiState){
            		mUsbManager.setCurrentFunction(AURORA_USB_MTP, true);
            	}else{
            		mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MTP, true);
            	}
            }else{
            	mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MTP, true);
            }
            */
            // location
            Settings.Secure.setLocationProviderEnabled(getContentResolver(),
                    LocationManager.GPS_PROVIDER, false);

            Settings.Secure.setLocationProviderEnabled(getContentResolver(),
                    LocationManager.NETWORK_PROVIDER, false);

            //Begin add by gary.gou if exist nfc,disable nfc default
            NfcManager manager = (NfcManager)mContext.getSystemService(Context.NFC_SERVICE);
            if(manager != null){
            	NfcAdapter adapter = manager.getDefaultAdapter();
            	if(adapter != null){
            		adapter.disable();
            	}
            }
            //End add by gary.gou

            //remove bootanimation cache
            String bootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            if(null != bootPath) {
            	File auroraFile = new File(bootPath + "/iuni/");
        		if (auroraFile.exists()) {
        			wipeBootanimationCache(bootPath + "/iuni/");
        		}
            }

            // Add begin by aurora.jiangmx
            Settings.System.putInt(mContext.getApplicationContext().getContentResolver(), AccessibilitySettings.NAVI_KEY_HIDE_ALLOW,
                    AccessibilitySettings.OPEN_VIRTUAL_KEY_SWITCH);

            SharedPreferences lSharedPreference = mContext.getSharedPreferences(SETTINGS_DEFAULT_PREF_FILE, Context.MODE_PRIVATE);
            lSharedPreference.edit().putBoolean(AccessibilitySettings.NAVI_KEY_HIDE_ALLOW, true).commit();
            // Add end

            et.putBoolean("is_first_start", false);
    		et.commit();

    		// Add begin by aurora.penggangding
        		if(Build.VERSION.RELEASE.contains("4.3") || Build.VERSION.RELEASE.contains("4.4"))
        		{
//        		    initDefaultDate();
        		    if(PlatformUtils.isSupportDefLSAppChioce()){
        		       PlatformUtils.defLSAppChioce(mContext);
        		    }
        		}
    		// end
		}

		if (Build.MODEL.contains("I9500") || Build.MODEL.contains("SM-N90")) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					reStart();
				}
			}, 5000);

		}
        if(false)
        {
    		if(Build.VERSION.RELEASE.contains("4.3") || Build.VERSION.RELEASE.contains("4.4"))
    		{
    			mContext.startService(new Intent("com.aurora.lscreen.LSBootService"));
    		}
        }
        new OTWifiStatistics(mContext);
		Log.v(TAG, "-----init()----exit---");

        Settings.Secure.setLocationProviderEnabled(getContentResolver(),
                LocationManager.NETWORK_PROVIDER, true);
    }
	
	/**
	 *lock screen contains default app
	 *@author penggangding
	 *@Time
	 */
    private DataArrayList<String> dataArrayList;

	private void initDefaultDate()
	{
		Log.d("gd", " initDefaultDate ");
		if(dataArrayList==null)
		{
			dataArrayList=new DataArrayList<String>();
		}else
		{
			dataArrayList.clear();
		}
		dataArrayList.add(LSOperator.IUNI_CONTACTS);
		dataArrayList.add(LSOperator.IUNI_MMS);
		dataArrayList.add(LSOperator.TENCENT_WEIXIN);
        AuroraLSManageModel.getInstance(mContext).addLSApp(dataArrayList);		
	}
	
	private void reStart() {
		WifiSettings.PrintLog("pgd", "WifiNotificationController");
		WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		List<ScanResult> results = mWifiManager.getScanResults();
		if (results != null) {
			new WifiNotificationController(mContext.getApplicationContext(), results);
		}
	}
	
	private void writeFileIntoHAL(int value, String src) {
		FileOutputStream out = null;
		File outFile = null;
		try {
			outFile = new File(src);
			out = new FileOutputStream(outFile);
			if (0 == value) {
				out.write("0\n".getBytes());
			} else if (1 == value) {
				out.write("1\n".getBytes());
			} else if (2 == value) {
				out.write("2\n".getBytes());
			} else if (3 == value) {
				out.write("3\n".getBytes());
			} else if (4 == value) {
				out.write("4\n".getBytes());
			}
			if (null != out) {
				out.flush();
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writePreferenceClick(boolean isChecked, String filePath) {
		FileOutputStream out = null;
		File outFile = null;
		try {
			outFile = new File(filePath);
			out = new FileOutputStream(outFile);
			if (isChecked) {
				out.write("1\n".getBytes());
			} else {
				out.write("0\n".getBytes());
			}
			if (null != out) {
				out.flush();
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// remove bootanimation cache begin
	private void wipeBootanimationCache(String path) {
		File deleteMatchingFile = new File(path);
		try {
			File[] filenames = deleteMatchingFile.listFiles();
			if (filenames != null && filenames.length > 0) {
				for (File tempFile : filenames) {
					if (tempFile.isDirectory()) {
						wipeDirectory(tempFile.toString());
						tempFile.delete();
					} else {
						tempFile.delete();
					}
				}
			} else {
				deleteMatchingFile.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void wipeDirectory(String name) {
		File directoryFile = new File(name);
		File[] filenames = directoryFile.listFiles();
		if (filenames != null && filenames.length > 0) {
			for (File tempFile : filenames) {
				if (tempFile.isDirectory()) {
					wipeDirectory(tempFile.toString());
					tempFile.delete();
				} else {
					tempFile.delete();
				}
			}
		} else {
			directoryFile.delete();
		}
	}

}
