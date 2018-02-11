package com.android.settings;
import com.android.settings.accessibility.AccessibilitySettings;
import com.android.settings.accessibility.FingerAndBodySettings;
import android.os.ServiceManager;
import android.service.notification.Condition;
import android.service.notification.ZenModeConfig;
import android.app.INotificationManager;
import android.app.NotificationManager;
import com.aurora.audioprofile.AudioProfileActivity;
import com.aurora.somatosensory.SomatosensorySettings;
import com.mediatek.audioprofile.AudioProfileManager;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

//<Aurora><hujianwei> 20150721 add for screen saving start
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import static android.provider.Settings.System.SCREEN_SAVING_MODE;
import static android.provider.Settings.System.SCREEN_SAVING_MODE_MANUAL;
import static android.provider.Settings.System.SCREEN_SAVING_MODE_MANUAL_ENABLE;
//<Aurora><hujianwei> 20150721 add for screen saving end

public class AuroraInitService extends Service {
	
	private static final String THREE_FINGERS_SCREENSHOTS = "three_finger_screenshot_enable";
	private static final String TAG = "AuroraInitService";
	
	 private ZenModeConfig mConfig;
	
	  // Cache the content resolver for async callbacks
	//<Aurora><hujianwei> 20150721 add for screen saving start
    private ContentResolver mContentResolver;
  //<Aurora><hujianwei> 20150721 add for screen saving end
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		SharedPreferences iuniSP = getSharedPreferences("iuni",
				Context.MODE_PRIVATE);
		if(iuniSP.getBoolean(FingerAndBodySettings.KEY_TWICE_CLICK_SCREEN_AWAKE_PREFERENCE, false)){
			SomatosensorySettings.writePreferenceClick(true, FingerAndBodySettings.LP_TWICE_CLICK_SCREEN_AWAKE);
		}
		if(iuniSP.getBoolean(FingerAndBodySettings.KEY_GLOVE, false)){
			SomatosensorySettings.writePreferenceClick(true, FingerAndBodySettings.GLOVE_PATH);
		}
		if(iuniSP.getBoolean(FingerAndBodySettings.KEY_SOMATOSENSORY_MUSIC, false)){
			SomatosensorySettings.writePreferenceClick(true, FingerAndBodySettings.FILE_SOMATOSENSORY_MUSIC);
		}
		if(iuniSP.getBoolean(AudioProfileActivity.KEY_DTS, false)){
			((AudioManager) getSystemService(Context.AUDIO_SERVICE)).setParameters("srs_cfg:trumedia_enable=1;srs_cfg:trumedia_preset=0");
		}
		
		
		boolean isFirstStart = iuniSP.getBoolean("is_first_start", true);
		
		//<Aurora><hujianwei> 20150721 add for screen saving start
		setScreenSavingState();
		//<Aurora><hujianwei> 20150721 add for screen saving end
		
		//<Aurora><hujianwei> 20150817 add for init sms tone start
		AudioSmsNotifyToneInit();
		//<Aurora><hujianwei> 20150817 add for init sms tone end
		
		
		// init for SomatosensorySettings
		if(isFirstStart){
			Editor et = iuniSP.edit();
			/*Settings.System.putInt(getContentResolver(),  SomatosensorySettings.SMART_CALL, 0);
			Settings.System.putInt(getContentResolver(),  SomatosensorySettings.SMART_ANSWER, 0);
			Settings.System.putInt(getContentResolver(),  SomatosensorySettings.GESTURE_PHOTO, 1);
			Settings.System.putInt(getContentResolver(),  SomatosensorySettings.GESTURE_ANSWER, 0);
			Settings.System.putInt(getContentResolver(),  SomatosensorySettings.GESTURE_VEDIO, 1);*/
                        /*aurora linchunhui 20150930 for the settings crash begin*/
                        AudioProfileManager apm = (AudioProfileManager) getSystemService(Context.AUDIO_PROFILE_SERVICE);
			if (null != apm)
                             apm.setSoundEffectEnabled(AudioProfileActivity.KEY, true);
                        /*aurora linchunhui 20150930 for the settings crash end*/
			//((AudioManager) getSystemService(Context.AUDIO_SERVICE)).setParameters("srs_cfg:trumedia_enable=1;srs_cfg:trumedia_preset=0");
			Settings.System.putInt(getContentResolver(),THREE_FINGERS_SCREENSHOTS, 1);
			//et.putBoolean(AudioProfileActivity.KEY_DTS, true);
			mConfig = getZenModeConfig();
			 final ZenModeConfig newConfig = mConfig.copy();
			 //Aurora hujianwei modify 20160217 for default value start
             newConfig.allowCalls = false;
             newConfig.allowMessages = false;
             newConfig.allowEvents = false;
             newConfig.allowFrom = 2; // allow starred contacts only
             //Aurora hujianwei modify 20160217 for default value end
             setZenModeConfig(newConfig);
            et.putBoolean("is_first_start", false);
    		et.commit();
		}
		
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	//<Aurora><hujianwei> 20150721 add for screen saving start
	/**************************************
	 * 接口 ：getScreenSavingFlag
	 * 描述 ：设置屏幕省电状态值
	 * 参数 ：void
	 * 返回值：void
	 *  
	 *  修改记录：
	 *  
	 ***************************************/
	public  void setScreenSavingState(){
		 boolean bEnable = Settings.System.getInt(getContentResolver(),SCREEN_SAVING_MODE, SCREEN_SAVING_MODE_MANUAL) == SCREEN_SAVING_MODE_MANUAL_ENABLE ? true : false;
		 DisplaySettings.writePreferenceClick(bEnable, DisplaySettings.FILE_SAVING_SCREEN_NODE);
	}
	

	//<Aurora><hujianwei> 20150721 add for screen saving end
	
	//<Aurora><hujianwei> 20150817 add for init sms tone start
	/*********************************************
	 * 接口    ：AudioSmsNotifyToneInit
	 * 描述    ：短信铃声初始化设置
	 * 参数    ：void
	 * 返回值：void
	 * ********************************************/
	private void AudioSmsNotifyToneInit(){
		
		if( Settings.System.getString( getContentResolver(), "sms_sound") == null ){
			Cursor mSmsCursor = getContentResolver().query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.IS_NOTIFICATION + "='1'", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
			
			while (mSmsCursor.moveToNext()) {
				if(mSmsCursor.getString(mSmsCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).trim().equals("光点（默认）")	){
					int nSmsUriId = mSmsCursor.getInt(mSmsCursor.getColumnIndex(MediaStore.Audio.Media._ID));
					Uri mSmsUri =  ContentUris.withAppendedId(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, nSmsUriId);
					
					if(mSmsUri != null){
						Settings.System.putString(getContentResolver(), "sms_sound", mSmsUri.toString());
					}
					break;
				}
		   }
		   mSmsCursor.close();
		
	    }
	}
	//<Aurora><hujianwei> 20150817 add for init sms tone end
	/**
	 * 
	 * 方法描述：设置打扰模式参数
	 * 创建时间：
	 * @author jiyouguang
	 * @param config
	 * @return
	 */
	private boolean setZenModeConfig(ZenModeConfig config) {
        final INotificationManager nm = INotificationManager.Stub.asInterface(
                ServiceManager.getService(Context.NOTIFICATION_SERVICE));
        try {
            final boolean success = nm.setZenModeConfig(config);

            return success;
        } catch (Exception e) {
           Log.w(TAG, "Error calling NoMan", e);
           return false;
        }
    }
	/**
	 * 
	 * 方法描述：获取打扰模式参数
	 * 创建时间：
	 * @author jiyouguang
	 * @return
	 */
	private ZenModeConfig getZenModeConfig() {
        final INotificationManager nm = INotificationManager.Stub.asInterface(
                ServiceManager.getService(Context.NOTIFICATION_SERVICE));
        try {
            return nm.getZenModeConfig();
        } catch (Exception e) {
           Log.w(TAG, "Error calling NoMan", e);
           return new ZenModeConfig();
        }
    }
}
