package gn.com.android.audioprofile;



import java.util.Timer;
import java.util.TimerTask;

import android.R.string;
import android.media.AudioManager;
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
import android.os.Parcelable;

import com.aurora.featureoption.FeatureOption;

import android.media.RingtoneManager;
import android.os.Vibrator;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.media.AudioSystem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.SystemProperties;
import android.os.SystemVibrator;

public class AudioProfileActivity extends AuroraPreferenceActivity implements AuroraPreference.OnPreferenceChangeListener{
	private boolean mDarkTheme, mLightTheme;
	private AudioManager mAudioManager = null;
	private AuroraActionBar auroraActionBar;
	
	public static final int MUTE_CHECKED = 1 ;
	public static final int MUTE_NOT_CHECKED = 0 ;
	public static final int RING_MODE_VIBRATE = 1;
	public static final int RING_MODE_RING = 2;
	public static final int RING_MODE_VIBRITE_WHEN_RING = 3;
	
	public static final String KEY_MUTE = "mute_swich";
	public static final String KEY_VOLUME_RING = "volum_ring_seekbar";
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
	
	
	public static final String KEY_DTMF_TONE = "dtmf_tone";
	public static final String KEY_SOUND_EFFECTS = "sound_effects";
	public static final String KEY_LOCK_SOUND = "lock_sounds";
	public static final String KEY_HAPTIC_FEEDBACK = "haptic_feedback";
	
	private AuroraVolumePreference mVolumRing;
	private AuroraRingtonePickerPreference mPhoneRingtone;
	private AuroraRingtonePickerPreference mMsgRingtone;
	private AuroraRingtonePickerPreference mNotificationRingtone;
	
	private AuroraSwitchPreference mMuteSwitchPreference;
	private AuroraSwitchPreference mVibrateSwitchPreference;
	private int mSaveMusicVolue;
	private AuroraSwitchPreference mDtmfTone;
	private AuroraSwitchPreference mSoundEffects;
	private AuroraSwitchPreference mLockSounds;
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
						case RING_MODE_VIBRATE:
							Log.v(TAG, "--mUpdateUIReceiver---RINGER_MODE_VIBRATE");
							if(mMuteSwitchPreference.isChecked()){
								mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
							}else{
								mMuteSwitchPreference.setChecked(true);
							}
							break;
						case RING_MODE_RING:
							Log.v(TAG, "--mUpdateUIReceiver---RING_MODE_RING");
							mMuteSwitchPreference.setChecked(false);
							if(mVolumRing != null ){
			            		mVolumRing.stopRingTone();
							}
							break;
						default:
							break;
						}
	            }else if(action.equals(ACTION_RECENTS_PANEL_HIDDEN)){
	            	Log.v(TAG, "----updateUI===  ");
	            	updateUI();
	            }
	        }
	    };
		
		
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(com.aurora.R.style.Theme_aurora_Light);
		super.onCreate(savedInstanceState);
		Log.v(TAG,"-----------onCreate---");
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
        mVibrator = new SystemVibrator();
        mHandler = new Handler();
        
		IntentFilter mUIFilter = new IntentFilter();
		mUIFilter.addAction(ACTION_UPDATE_MUTE_VIBRATE);
		mUIFilter.addAction(ACTION_RECENTS_PANEL_HIDDEN);
		registerReceiver(mUpdateUIReceiver, mUIFilter);
		
		addPreferencesFromResource(R.xml.aurora_sound_and_vibrate);
		// set title
		auroraActionBar = getAuroraActionBar();
		auroraActionBar.setTitle(R.string.sound_settings_title);  //test title
		auroraActionBar.setmOnActionBarBackItemListener(auroActionBarItemBackListener);
		 // init mute state
		mMuteSwitchPreference= (AuroraSwitchPreference) findPreference(KEY_MUTE);
		mMuteSwitchPreference.setOnPreferenceChangeListener(this);
		
		// volume setting
		mVolumRing = (AuroraVolumePreference)findPreference(KEY_VOLUME_RING);
		mVolumRing.setStreamType(AudioManager.STREAM_RING);	
		mVolumRing.setVolumeProgress(mVolumRing.getSavedVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_RING) * 10));
		
		//vibrate switch on and off 
		mVibrateSwitchPreference = (AuroraSwitchPreference)findPreference(KEY_VIBRATE);
		mVibrateSwitchPreference.setOnPreferenceChangeListener(this);
		
		//ringtone setting 
		mPhoneRingtone = (AuroraRingtonePickerPreference)findPreference(KEY_RINGTONE_PHONE);
		mPhoneRingtone.setRingtoneType(RingtoneManager.TYPE_RINGTONE);
		mPhoneRingtone.auroraSetArrowText("",true);

		mNotificationRingtone = (AuroraRingtonePickerPreference)findPreference(KEY_RINGTONE_NOTIFICATION);
		mNotificationRingtone.setRingtoneType(RingtoneManager.TYPE_NOTIFICATION);
		mNotificationRingtone.auroraSetArrowText("",true);
		
		mMsgRingtone = (AuroraRingtonePickerPreference)findPreference(KEY_RINGTONE_SMS);
		mMsgRingtone.setRingtoneType(TYPE_RINGTONE_SMS);
		mMsgRingtone.auroraSetArrowText("",true);
		
		//  system feedback		
		mDtmfTone = (AuroraSwitchPreference)findPreference(KEY_DTMF_TONE);
		mDtmfTone.setOnPreferenceChangeListener(this);
		
		mSoundEffects = (AuroraSwitchPreference)findPreference(KEY_SOUND_EFFECTS);
		mSoundEffects.setOnPreferenceChangeListener(this);
		
		mLockSounds = (AuroraSwitchPreference)findPreference(KEY_LOCK_SOUND);
		mLockSounds.setOnPreferenceChangeListener(this);
		
		mHapticFeedback = (AuroraSwitchPreference)findPreference(KEY_HAPTIC_FEEDBACK);
		mHapticFeedback.setOnPreferenceChangeListener(this);
		mHapticFeedback.setChecked(Settings.System.getInt(getContentResolver(),  Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0);
		
		 Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	         if (vibrator == null || !vibrator.hasVibrator()) {
	             getPreferenceScreen().removePreference(mHapticFeedback);
	         }

	    // dts
    	mEnableDTS = (AuroraSwitchPreference)findPreference("enable_dts");
       
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
        
        String deviceName = SystemProperties.get("ro.product.name");        
        if (!deviceName.contains("IUNI")) {
        	 getPreferenceScreen().removePreference(mEnableDTS); 
        }
        
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
    		boolean mDtmfToneState = Settings.System.getInt(getContentResolver(),  Settings.System.DTMF_TONE_WHEN_DIALING, 1) != 0;
    		boolean mSoundState = Settings.System.getInt(getContentResolver(),  Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0;
    		boolean mLockScreenState = Settings.System.getInt(getContentResolver(),  Settings.System.LOCKSCREEN_SOUNDS_ENABLED, 1) != 0;
    		
    		 Log.v(TAG, "---setDtmfSoundLockScreenState----mDtmfToneState = "  + mDtmfToneState  + "  mSoundState = " + mSoundState  + "  mLockScreenState = " + mLockScreenState);
    		mDtmfTone.setChecked(mDtmfToneState);
    		mSoundEffects.setChecked(mSoundState);
    		mLockSounds.setChecked(mLockScreenState);
    		mDtmfTone.setEnabled(true);
    		mSoundEffects.setEnabled(true);
    		mLockSounds.setEnabled(true); 
    	}else{
    		mDtmfTone.setChecked(false);
    		mSoundEffects.setChecked(false);
    		mLockSounds.setChecked(false);
    		mDtmfTone.setEnabled(false);
    		mSoundEffects.setEnabled(false);
    		mLockSounds.setEnabled(false); 
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
    	mVolumRing.stopRingTone();
    	boolean isChecked = (Boolean) newValue;
    	if(mMuteSwitchPreference == preference){
             if(isChecked){
         	    Log.v(TAG, "---onPreferenceChange----mMuteSwitchPreference----true---");
            	 mVolumRing.setSaveCurrentVolume();
            	 //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        		 setDtmfSoundLockScreenState(false);
        		 setRingtoneState(false);
        		 if(mVibrateSwitchPreference.isChecked()){
        			 mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        		 } else{
        			 mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        		 }
        		 mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,0);
        		 
             }else{
            	 Log.v(TAG, "---onPreferenceChange----mMuteSwitchPreference----false---");
            	 mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            	 if(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0){
            		 mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2, 0);
            	 }
            	 //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mSaveMusicVolue, 0);
        		 setDtmfSoundLockScreenState(true);
        		 setRingtoneState(true);
        		 int temVolume = mVolumRing.getSavedVolume(-1);
        		 Log.v(TAG, "---onPreferenceChange----mMuteSwitchPreference----temVolume == "  + temVolume);
     			 if(temVolume == -1){
     				// set the defalut value
     				mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 5, 0);
     				mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 5, 0);
     				mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 5, 0);
         			mVolumRing.setVolumeProgress(5 * 10);
     			 }else if(temVolume == 0){
     				mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 1, 0);
     				mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 1, 0);
     				mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 1, 0);
         			mVolumRing.setVolumeProgress(1 * 10);
     			 }else{
     				//mAudioManager.setStreamVolume(AudioManager.STREAM_RING, temVolume, 0);
     				//mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, temVolume, 0);
         			mVolumRing.setVolumeProgress(temVolume);
     			}
        		 if(mVibrateSwitchPreference.isChecked()){
        			 Settings.System.putInt(getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,1);
        		 } else{
        			 Settings.System.putInt(getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,0);
        		 }
             }
    	}
    	
    	else if(mVibrateSwitchPreference == preference ){
    	   int ringMode = mAudioManager.getRingerMode();
    	   Log.v(TAG, "---onPreferenceChange----mVibrateSwitchPreference--enter--ringMode===="+ringMode + "  isChecked = " + isChecked);
    	   if(isChecked){
    		   if(ringMode == AudioManager.RINGER_MODE_NORMAL){
          			Settings.System.putInt(getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,1);
          		 }else if(ringMode == AudioManager.RINGER_MODE_SILENT || ringMode == AudioManager.RINGER_MODE_VIBRATE){
          			mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
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
         			mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
         		 }
    	   }
    	}
    	
    	else if (mHapticFeedback == preference){
    		if(isChecked){				
				 Settings.System.putInt(getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED,1 );				
			}else{				
				Settings.System.putInt(getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED,0 );
			}
    	}else if (preference == mEnableDTS) {
            if ((Boolean) newValue) {
            	mAudioManager.setParameters("srs_cfg:trumedia_enable=1;srs_cfg:trumedia_preset=0");
            } else {
            	mAudioManager.setParameters("srs_cfg:trumedia_enable=0");                
            }
        }
    
    	else if(mDtmfTone == preference || mSoundEffects == preference ||mLockSounds == preference){
        	int ringMode = mAudioManager.getRingerMode();
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
    	 }else if (mSoundEffects == preference){
    		if(isChecked){
				mAudioManager.loadSoundEffects();				
				Settings.System.putInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED,1 );
			}else{
				mAudioManager.unloadSoundEffects();
				Settings.System.putInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED,0 );
			}
    	 }else if (mLockSounds == preference){
    		if(isChecked){				
				 Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_SOUNDS_ENABLED,1 );				
			}else{				
				Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_SOUNDS_ENABLED,0 );
			}
    	  }
        }
        
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
	   int ringMode = mAudioManager.getRingerMode(); 
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
		if(mVolumRing != null ){
    		mVolumRing.stopRingTone();
		}
	}
    
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mUpdateUIReceiver);
        mVolumRing.stopRingRes();
	}
	
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
	    	switch (keyCode) {
	    		case KeyEvent.KEYCODE_BACK:
	    		finish();
	    		overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter,com.aurora.R.anim.aurora_activity_close_exit);
	    		return true;
	    		case KeyEvent.KEYCODE_VOLUME_DOWN:
	    			return true;
	    		case KeyEvent.KEYCODE_VOLUME_UP:
	    			return true;
	    		case KeyEvent.KEYCODE_VOLUME_MUTE:
	    			return true;
	    		default:
	    			
				return super.onKeyDown(keyCode, event);
	    	}
	    }
}

