package com.aurora.audioprofile;



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
import com.android.settings.R;
import com.aurora.featureoption.FeatureOption;
import com.mediatek.audioprofile.AudioProfileManager;

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

public class VolumeSetActivity extends AuroraPreferenceActivity {
	private AudioManager mAudioManager = null;
	private AuroraActionBar auroraActionBar;
	private AuroraVolumePreference mVolumRing;
	private AuroraVolumePreference mVolumMedia;
	private AuroraVolumePreference mVolumSystem;
	
	public static final String KEY_VOLUME_RING = "volum_ring_seekbar";
	public static final String VOLUM_ALARM_SEEKBAR = "volum_alarm_seekbar";
	public static final String VOLUM_MEDIA_SEEKBAR = "volum_media_seekbar";
	
	public static final String KEY = "mtk_audioprofile_general";  
	
	private static final String TAG = "AudioProfileActivity";
	
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
		
		private BroadcastReceiver mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
					AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
					mVolumRing.refresh(am);
					mVolumMedia.refresh(am);
					mVolumSystem.refresh(am);
				}
			}
		};
		
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.GnSettingsLightTheme);
		super.onCreate(savedInstanceState);
		Log.v(TAG,"-----------onCreate---");
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);        
		addPreferencesFromResource(R.xml.aurora_volume_settings);
		// set title
		auroraActionBar = getAuroraActionBar();
		auroraActionBar.setTitle(R.string.all_volume_title);  //test title
		auroraActionBar.setmOnActionBarBackItemListener(auroActionBarItemBackListener);
		
		// volume setting
		mVolumRing = (AuroraVolumePreference)findPreference(KEY_VOLUME_RING);
		mVolumRing.setStreamType(AudioManager.STREAM_RING);	
		mVolumRing.setVolumeProgress(mVolumRing.getSavedVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_RING) * 10));        
		
		mVolumSystem = (AuroraVolumePreference)findPreference(VOLUM_ALARM_SEEKBAR);
		mVolumSystem.setStreamType(AudioManager.STREAM_ALARM);	
		mVolumSystem.setVolumeProgress(mVolumSystem.getSavedVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM) * 10));        
		
		mVolumMedia = (AuroraVolumePreference)findPreference(VOLUM_MEDIA_SEEKBAR);
		mVolumMedia.setStreamType(AudioManager.STREAM_MUSIC);	
		mVolumMedia.setVolumeProgress(mVolumMedia.getSavedVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 10));
		IntentFilter intentFilter = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
		registerReceiver(mReceiver, intentFilter);
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
	protected void onPause() {
		super.onPause();
		finishVolum();
	}
    
	protected void onDestroy() {
		super.onDestroy();
		finishVolum();
		unregisterReceiver(mReceiver);
	}
	
	private void finishVolum(){
		if(mVolumRing != null ){
    		mVolumRing.stopRingTone();
		}
		if(mVolumMedia != null ){
			mVolumMedia.stopRingTone();
		}
		if(mVolumSystem != null ){
			mVolumSystem.stopRingTone();
		}
	}
	
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
	    	switch (keyCode) {
	    		case KeyEvent.KEYCODE_BACK:
	    		finish();
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

