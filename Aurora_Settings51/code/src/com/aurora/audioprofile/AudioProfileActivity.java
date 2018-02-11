package com.aurora.audioprofile;



import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.R.string;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import aurora.preference.*;
import android.text.TextUtils;
import android.util.Log;
import android.content.BroadcastReceiver;
//import android.app.ThemeManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import aurora.widget.AuroraActionBar;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Parcelable;

import com.android.settings.R;
import com.aurora.audioprofile.entity.Song;
import com.aurora.featureoption.FeatureOption;
import com.mediatek.audioprofile.AudioProfileManager;

import android.media.RingtoneManager;
import android.os.Vibrator;
import android.preference.SwitchPreference;
import android.provider.MediaStore;
import android.provider.Settings;
import android.media.AudioSystem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.SystemProperties;
import android.os.SystemVibrator;

public class AudioProfileActivity extends AuroraPreferenceActivity implements AuroraPreference.OnPreferenceChangeListener{
	static{
		MediaPlayer.stopMusic = true;
	}
	private boolean mDarkTheme, mLightTheme;
	private AudioManager mAudioManager = null;
	private AuroraActionBar auroraActionBar;
	private RingtoneManager mRingtoneManager;
	
	public static final int MUTE_CHECKED = 1 ;
	public static final int MUTE_NOT_CHECKED = 0 ;
	
	//start modify by hujianwei 20150711 for add RINGER_MODE_SILENT
	public static final int RINGER_MODE_SILENT = 0;
	//end  modify by hujianwei 20150711 for add RINGER_MODE_SILENT
	
	public static final int RING_MODE_VIBRATE = 1;
	public static final int RING_MODE_RING = 2;
	public static final int RING_MODE_VIBRITE_WHEN_RING = 3;
	
	public static final String KEY_MUTE = "mute_swich";
	public static final String KEY_VOLUME_NOTIFICATION = "volum_notify_seekbar";
	public static final String KEY_VOLUME_MEDIA = "volum_media_seekbar";
	public static final String KEY_VOLUME_ALARM = "volum_larm_seekbar";
	
	public static final String KEY_VIBRATE = "vibrat_swich";
	// ringtone setting
	public static final String KEY_RINGTONE_PHONE = "phone_ringtone";
	public static final String KEY_RINGTONE_SMS = "sms_ringtone";
	public static final String KEY_RINGTONE_NOTIFICATION = "notifications_ringtone";
	public static final String KEY_RINGTONE_ALARM = "alarm_ringtone";
	
	public static final int  TYPE_RINGTONE_SMS = 5;  //自定义一个短信铃声的type
	
	
//	public static final String KEY_DTMF_TONE = "dtmf_tone";
//	public static final String KEY_SOUND_EFFECTS = "sound_effects";
//	public static final String KEY_LOCK_SOUND = "lock_sounds";
	public static final String KEY_HAPTIC_FEEDBACK = "haptic_feedback";
	public static final String KEY_DTS = "enable_dts";
	
	
	public static final String KEY = "mtk_audioprofile_general";  
	
	  private AudioProfileManager mProfileManager;
	
	private AuroraRingtonePickerPreference mPhoneRingtone;
	private AuroraRingtonePickerPreference mMsgRingtone;
	private AuroraRingtonePickerPreference mNotificationRingtone;
	
	private AuroraSwitchPreference mMuteSwitchPreference;
	private AuroraSwitchPreference mVibrateSwitchPreference;
	private int mSaveMusicVolue;
//	private AuroraSwitchPreference mDtmfTone;
//	private AuroraSwitchPreference mSoundEffects;
//	private AuroraSwitchPreference mLockSounds;
	private AuroraSwitchPreference mHapticFeedback;
	private static boolean mHapticFeedState = true;
	
	public static final String ACTION_UPDATE_MUTE_VIBRATE = "gn.com.android.audioprofile.action.UPDATE_MUTE_VIBRATE";
	private static final String ACTION_RECENTS_PANEL_HIDDEN = "com.android.systemui.recent.aurora.RECENTS_PANEL_HIDDEN";
	
	private boolean isSaveFlag = true;
	private boolean isFeedbackFlag = true;
	private static boolean isInternalSdcardMounted;
	private boolean mIsFromReceiver;
	
	private Vibrator mVibrator;
	private Handler mHandler;

	private static final String TAG = "AudioProfileActivity";
	// dts
	private AuroraSwitchPreference mEnableDTS;
	
	private OnAuroraActionBarBackItemClickListener auroActionBarItemBackListener = new OnAuroraActionBarBackItemClickListener() {
			public void onAuroraActionBarBackItemClicked(int itemId) {
				switch (itemId) {
				case -1:
					finish();
					overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter,com.aurora.R.anim.aurora_activity_close_exit);
					break;
				default:
					break;
				}
			}
		};
   
	 private final BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	String action = intent.getAction();
	            if (action.equals(ACTION_UPDATE_MUTE_VIBRATE) ) {
	            	/*int value = Settings.System.getInt(getContentResolver(),Settings.System.VIBRATE_WHEN_RINGING, 0);
	            	Log.v(TAG, "----value===  "+value);*/
	            	
	            	int ringMode = intent.getIntExtra("RING_MODE", -1);
	            	Log.v(TAG, "----ringMode=== "+ringMode);
	            	
	            		switch (ringMode) {
	            		
	            		//start modify by hujianwei 20150711 for add RINGER_MODE_SILENT flag
	            		case RINGER_MODE_SILENT:
	            		{
	            			Log.d(TAG, "update RINGER_MODE_SILENT");
	            			//设置为静音，并且开启静音按钮,更新UI
	            			if(mAudioManager != null){
	            				mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
	            				mMuteSwitchPreference.setChecked(true);
	            				updateUI();
	            			}
	            			break;
	            		}
	            		//start modify by hujianwei 20150711 for add RINGER_MODE_SILENT flag
	            		
						case RING_MODE_VIBRATE:
							Log.v(TAG, "--mUpdateUIReceiver---RINGER_MODE_VIBRATE");
							if(mMuteSwitchPreference.isChecked()){
								mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
							}else{
								mMuteSwitchPreference.setChecked(true);
							}
							break;
						case RING_MODE_RING:
							Log.v(TAG, "--mUpdateUIReceiver---RING_MODE_RING");
							mMuteSwitchPreference.setChecked(false);
							
							//如果接收到正常铃声相应，设置系统状态,并且更新UI
							//start modify by hujianwei 20150711 for add RINGER_MODE_NORMAL flag
							mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL);
							updateUI();
							break;
						default:
							break;
						}
	            }else if(action.equals(ACTION_RECENTS_PANEL_HIDDEN)){
	            	Log.v(TAG, "----updateUI===  ");
	            	updateUI();
	            }else if(action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)){
	            	updateUI();
	            }
	        }
	    };
		
		
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.GnSettingsLightTheme);
		super.onCreate(savedInstanceState);
		Log.v(TAG,"-----------onCreate---");
		mRingtoneManager = new RingtoneManager(this);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		 mProfileManager = (AudioProfileManager) getSystemService(Context.AUDIO_PROFILE_SERVICE);
		
        mVibrator = new SystemVibrator();
        mHandler = new Handler();
        
		IntentFilter mUIFilter = new IntentFilter();
		mUIFilter.addAction(ACTION_UPDATE_MUTE_VIBRATE);
		mUIFilter.addAction(ACTION_RECENTS_PANEL_HIDDEN);
		mUIFilter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
		registerReceiver(mUpdateUIReceiver, mUIFilter);
		
		addPreferencesFromResource(R.xml.aurora_sound_and_vibrate);
		// set title
		auroraActionBar = getAuroraActionBar();
		auroraActionBar.setTitle(R.string.sound_settings_title);  //test title
		auroraActionBar.setmOnActionBarBackItemListener(auroActionBarItemBackListener);
		 // init mute state
		mMuteSwitchPreference= (AuroraSwitchPreference) findPreference(KEY_MUTE);
		mMuteSwitchPreference.setOnPreferenceChangeListener(this);
		
		//vibrate switch on and off 
		mVibrateSwitchPreference = (AuroraSwitchPreference)findPreference(KEY_VIBRATE);
		mVibrateSwitchPreference.setOnPreferenceChangeListener(this);
		
		//ringtone setting 
		mPhoneRingtone = (AuroraRingtonePickerPreference)findPreference(KEY_RINGTONE_PHONE);
		mPhoneRingtone.setRingtoneType(RingtoneManager.TYPE_RINGTONE);
		mPhoneRingtone.auroraSetArrowText("",true);
		mPhoneRingtone.setSummary(initUriName(false, RingtoneManager.TYPE_RINGTONE));

		mNotificationRingtone = (AuroraRingtonePickerPreference)findPreference(KEY_RINGTONE_NOTIFICATION);
		mNotificationRingtone.setRingtoneType(RingtoneManager.TYPE_NOTIFICATION);
		mNotificationRingtone.auroraSetArrowText("",true);
		mNotificationRingtone.setSummary(initUriName(false, RingtoneManager.TYPE_NOTIFICATION));
		
		mMsgRingtone = (AuroraRingtonePickerPreference)findPreference(KEY_RINGTONE_SMS);
		mMsgRingtone.setRingtoneType(TYPE_RINGTONE_SMS);
		mMsgRingtone.auroraSetArrowText("",true);
		mMsgRingtone.setSummary(initUriName(true, -1));
		//  system feedback		
//		 = (AuroraSwitchPreference)findPreference(KEY_DTMF_TONE);
//		mDtmfTone.setOnPreferenceChangeListener(this);
		
//		mSoundEffects = (AuroraSwitchPreference)findPreference(KEY_SOUND_EFFECTS);
//		mSoundEffects.setOnPreferenceChangeListener(this);
		
//		mLockSounds = (AuroraSwitchPreference)findPreference(KEY_LOCK_SOUND);
//		mLockSounds.setOnPreferenceChangeListener(this);
		
		mHapticFeedback = (AuroraSwitchPreference)findPreference(KEY_HAPTIC_FEEDBACK);
		mHapticFeedback.setOnPreferenceChangeListener(this);
		mHapticFeedback.setChecked(/*Settings.System.getInt(getContentResolver(),  Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0*/mProfileManager
                .isVibrateOnTouchEnabled(KEY));
		
		 Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	         if (vibrator == null || !vibrator.hasVibrator()) {
	             getPreferenceScreen().removePreference(mHapticFeedback);
	         }

	    // dts
    	mEnableDTS = (AuroraSwitchPreference)findPreference(KEY_DTS);
       
        if (mEnableDTS != null && mAudioManager != null) {
            mEnableDTS.setOnPreferenceChangeListener(this);

            String inParams = mAudioManager.getParameters("srs_cfg:trumedia_enable");
            
            if (inParams.contains("1")) {
                mEnableDTS.setChecked(true);
                mAudioManager.setParameters("srs_cfg:trumedia_enable=1;srs_cfg:trumedia_preset=0");
            } else {
                mEnableDTS.setChecked(false);
                mAudioManager.setParameters("srs_cfg:trumedia_enable=0");
            }
        }
        
        
	}
	
	//获取需要显示的副标题
	private String  initUriName(boolean isSmsList, int mType){
		Uri uri = null;
		if(isSmsList){
    		String smsUri = Settings.System.getString(getContentResolver(), "sms_sound");
    		if(smsUri!=null && smsUri.length()>0){
    			uri = Uri.parse(smsUri);
    		}
    	} else {
    		if(mProfileManager != null){
    			uri = mProfileManager.getRingtoneUri("mtk_audioprofile_general", mType, -1);
    		}else{
    			uri = RingtoneManager.getActualDefaultRingtoneUri(this, mType); 
    		}
    		
    	}
		Log.i(TAG, "uri=:"+uri );
		if(uri ==null || uri.toString().length()==0){
			return "";
		}
		String[] projection = null;
		if(uri.toString().startsWith(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())){
			projection = new String[] {MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, };
	    }else{
	      	projection = new String[] {MediaStore.Audio.Media.TITLE, };
	    }
		Cursor cr= null;
		try {
			 cr = getContentResolver().query(uri, projection, null,null, null); 
			 if(cr==null || cr.getCount()==0 || !cr.moveToPosition(0)){
				 uri = getSystemDefultRingtoneUri(mType);
				 Log.i(TAG, "uri="+uri);
				 cr = getContentResolver().query(uri, projection, null,null, null); 
			 }
		}catch(Exception e){
			e.printStackTrace();
		}
		String name = "";
		String path = "";
		if(cr!=null && cr.getCount()>0 && cr.moveToPosition(0)){
			name = cr.getString(0);
			if(uri.toString().startsWith(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())){
				path = cr.getString(1);
			}
		}
		Log.i(TAG, "path="+path);
		if(cr!=null){
			cr.close();
		}
		if(path.startsWith(AuroraRingPickerActivity.THEME_RINGTONG_DOWNLLOAD_PATH) && uri.toString().startsWith(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())){//下载铃声从
			Cursor cursor = getContentResolver().query(Uri.parse("content://com.aurora.thememanager.ringtoneprovider"), new String[]{AuroraRingPickerActivity.NAME}, AuroraRingPickerActivity.FILE_DIR + "=? and "+AuroraRingPickerActivity.FILE_NAME+"=? ", new String[] { path.substring(0, path.lastIndexOf(File.separator)) ,path.substring(path.lastIndexOf(File.separator)+1)}, null);
			Log.i(TAG, "cursor.getCount()="+cursor.getCount());
			// query the date from databse
			if(cursor==null || cursor.getCount()==0){
				return name;
			}
			try {				
				if(cursor!=null)
				{
					if (cursor.moveToFirst()) {
						name = cursor.getString(cursor.getColumnIndex(AuroraRingPickerActivity.NAME));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(cursor!=null){
					cursor.close();
				}
			}	
		}
		if(name!=null && name.length()>0){
			int indexTemp = name.lastIndexOf('.');
			if(indexTemp > 0){
				name = name.substring(0, indexTemp);					
			}
		}
		
		
		return name;
	}
	
	//获取系统默认铃声的Uri  
    private Uri getSystemDefultRingtoneUri(int mType) {  
    	Cursor mCursor = mRingtoneManager.getCursor();	
        return AuroraRingPickerActivity.getDefaultUri(mType, mCursor);  
    }  
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:
        	Log.i("qy", "*******HOME");
            onBackPressed();
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void setDtmfSoundLockScreenState(boolean state){
    	if(state){
    		boolean mDtmfToneState =/* Settings.System.getInt(getContentResolver(),  Settings.System.DTMF_TONE_WHEN_DIALING, 1) != 0*/mProfileManager.isDtmfToneEnabled(KEY);
    		boolean mSoundState = /*Settings.System.getInt(getContentResolver(),  Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0*/mProfileManager.isSoundEffectEnabled(KEY);
    		boolean mLockScreenState = /*Settings.System.getInt(getContentResolver(),  Settings.System.LOCKSCREEN_SOUNDS_ENABLED, 1) != 0*/mProfileManager.isLockScreenEnabled(KEY);

    		 Log.v(TAG, "---setDtmfSoundLockScreenState----mDtmfToneState = "  + mDtmfToneState  + "  mSoundState = " + mSoundState  + "  mLockScreenState = " + mLockScreenState);
//    		.setChecked(mDtmfToneState);
//    		mSoundEffects.setChecked(mSoundState);
//    		mLockSounds.setChecked(mLockScreenState);
//    		mDtmfTone.setEnabled(true);
//    		mSoundEffects.setEnabled(true);
//    		mLockSounds.setEnabled(true); 
    	}else{
//    		mDtmfTone.setChecked(false);
//    		mSoundEffects.setChecked(false);
//    		mLockSounds.setChecked(false);
//    		mDtmfTone.setEnabled(false);
//    		mSoundEffects.setEnabled(false);
//    		mLockSounds.setEnabled(false); 
    	}
    }
    
    private void setRingtoneState(boolean state){
    	mPhoneRingtone.setEnabled(state);
		mPhoneRingtone.setSelectable(state);
		mNotificationRingtone.setEnabled(state);
		mNotificationRingtone.setSelectable(state);
		mMsgRingtone.setEnabled(state);
		mMsgRingtone.setSelectable(state);
    }
    
    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {  
    	boolean isChecked = (Boolean) newValue;
    	if(mMuteSwitchPreference == preference){
             if(isChecked){
         	    Log.v(TAG, "---onPreferenceChange----mMuteSwitchPreference----true---");
            	 //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        		 setDtmfSoundLockScreenState(false);
        		 setRingtoneState(false);
        		 if(mVibrateSwitchPreference.isChecked()){
        			 mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_VIBRATE);
        		 } else{
        			 mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
        		 }
        		 mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,0);
        		 
        		//start modify by hujianwei 20150721 for modify start 
        		// mVolumRing.setVolumeProgress(0);
        		//start modify by hujianwei 20150721 for modify end
        		 
             }else{
            	 Log.v(TAG, "---onPreferenceChange----mMuteSwitchPreference----false---");
            	 mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL);
            	 if(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0){
            		 mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2, 0);
            	 }
            	 //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mSaveMusicVolue, 0);
        		 setDtmfSoundLockScreenState(true);
        		 setRingtoneState(true);
        		 if(mVibrateSwitchPreference.isChecked()){
        			 Settings.System.putInt(getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,1);
        		 } else{
        			 Settings.System.putInt(getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,0);
        		 }
             }
    	}
    	
    	else if(mVibrateSwitchPreference == preference ){
    	   int ringMode = mAudioManager.getRingerModeInternal();
    	   Log.v(TAG, "---onPreferenceChange----mVibrateSwitchPreference--enter--ringMode===="+ringMode + "  isChecked = " + isChecked);
    	   if(isChecked){
    		   if(ringMode == AudioManager.RINGER_MODE_NORMAL){
          			Settings.System.putInt(getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,1);
          		 }else if(ringMode == AudioManager.RINGER_MODE_SILENT || ringMode == AudioManager.RINGER_MODE_VIBRATE){
          			mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_VIBRATE);
          		//start vibrate
    				mHandler.postDelayed(new Runnable() {
    	                public void run() {
    	                	
    	                    mVibrator.vibrate(299);
    	                }
    	            }, 300);
          			//Settings.System.putInt(getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,0);
          		 }
    	   }else{
         		 if(ringMode == AudioManager.RINGER_MODE_NORMAL){
         			 Settings.System.putInt(getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,0);
         		 }else if(ringMode == AudioManager.RINGER_MODE_VIBRATE){
         			mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
         		 }
    	   }
    	}
    	
    	else if (mHapticFeedback == preference){
    		/*if(isChecked){				
				 Settings.System.putInt(getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED,1 );				
			}else{				
				Settings.System.putInt(getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED,0 );
			}*/
    		 mProfileManager.setVibrateOnTouchEnabled(KEY, isChecked);
    	}else if (preference == mEnableDTS) {
            if ((Boolean) newValue) {
            	mAudioManager.setParameters("srs_cfg:trumedia_enable=1;srs_cfg:trumedia_preset=0");
            } else {
            	mAudioManager.setParameters("srs_cfg:trumedia_enable=0");                
            }
            
	    	//获取到sharepreference 对象， 参数一为xml文件名，参数为文件的可操作模式  
			SharedPreferences iuniSP = getSharedPreferences("iuni", Context.MODE_PRIVATE);  
			//获取到编辑对象  
			SharedPreferences.Editor edit = iuniSP.edit();  
			edit.putBoolean(KEY_DTS, (Boolean) newValue);
			//提交.  
			edit.commit();
        }
    
    	/*else if(mDtmfTone == preference || mSoundEffects == preference ||mLockSounds == preference){
        	int ringMode = mAudioManager.getRingerModeInternal();
        	Log.v(TAG,"----onPreferenceChange--DtmfSoundEffectLockSounds-----ringMode==="+ringMode);
        	if(ringMode == AudioManager.RINGER_MODE_SILENT || ringMode == AudioManager.RINGER_MODE_VIBRATE){
        		return true;
        	}
        	
    	  if(mDtmfTone == preference){
    		if(isChecked){				
				 Settings.System.putInt(getContentResolver(), Settings.System.DTMF_TONE_WHEN_DIALING,1 );				
			}else{				
				Settings.System.putInt(getContentResolver(), Settings.System.DTMF_TONE_WHEN_DIALING,0 );
			}  
    		  mProfileManager.setDtmfToneEnabled(KEY, isChecked);
    	 }else if (mSoundEffects == preference){
    		if(isChecked){
				mAudioManager.loadSoundEffects();				
				Settings.System.putInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED,1 );
			}else{
				mAudioManager.unloadSoundEffects();
				Settings.System.putInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED,0 );
			}
    		 mProfileManager.setSoundEffectEnabled(KEY, isChecked);
    	 }else if (mLockSounds == preference){
    		if(isChecked){				
				 Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_SOUNDS_ENABLED,1 );				
			}else{				
				Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_SOUNDS_ENABLED,0 );
			}
    		 mProfileManager.setLockScreenEnabled(KEY, isChecked);
    	  }
        }*/
        
    	return true;
    }
    
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.v("111","---onResume----");
		//save the musci Volume state
		/*if(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0){
		   		mSaveMusicVolue = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		}*/

		//mVolumRing.setVolumeProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_RING));
		//mVolumRing.setVolumeProgress(mVolumRing.getSavedVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_RING) * 10));
		
		//mVolumRing.setSavedMusicVolue(mSaveMusicVolue);
		updateUI();
	}
	/*
	 * RINGER_MODE_SILENT 静音 , 且无振动 。
	
		RINGER_MODE_VIBRATE 静音 , 但有振动 。
	
		RINGER_MODE_NORMAL 正常声音 , 振动开关由 setVibrateSetting 决定 .
	 */
   private void updateUI(){
	   int ringMode = mAudioManager.getRingerModeInternal(); 
	   Log.i(TAG, "ringMode="+ringMode);
		if(ringMode == AudioManager.RINGER_MODE_SILENT || ringMode == AudioManager.RINGER_MODE_VIBRATE){
			if(ringMode == AudioManager.RINGER_MODE_SILENT){
			   mMuteSwitchPreference.setChecked(true);	
			   mVibrateSwitchPreference.setChecked(false);
			}else{
				mMuteSwitchPreference.setChecked(true);	
			   mVibrateSwitchPreference.setChecked(true);
			}
    		setDtmfSoundLockScreenState(false);
    		setRingtoneState(false);
    		//Settings.System.putInt(getContentResolver(),Settings.System.VIBRATE_WHEN_RINGING, 0);
    	}else {
    		mMuteSwitchPreference.setChecked(false);	
    		if(Settings.System.getInt(getContentResolver(),Settings.System.VIBRATE_WHEN_RINGING, 0) != 0){
    			 mVibrateSwitchPreference.setChecked(true);
    		}else{
    			 mVibrateSwitchPreference.setChecked(false);
    		}
    		setDtmfSoundLockScreenState(true);
    		setRingtoneState(true);
    	}
   }
    
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
    
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mUpdateUIReceiver);
	}
	
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
	    	switch (keyCode) {
	    		case KeyEvent.KEYCODE_BACK:
	    		finish();
	    		//overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter,com.aurora.R.anim.aurora_activity_close_exit);
	    		return true;
/*	    		case KeyEvent.KEYCODE_VOLUME_DOWN:
	    			return true;
	    		case KeyEvent.KEYCODE_VOLUME_UP:
	    			return true;
	    		case KeyEvent.KEYCODE_VOLUME_MUTE:
	    			return true;*/
	    		default:
	    			
				return super.onKeyDown(keyCode, event);
	    	}
	    }
}

