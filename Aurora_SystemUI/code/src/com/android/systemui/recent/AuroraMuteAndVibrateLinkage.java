package com.android.systemui.recent;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.os.Handler;
import android.os.SystemVibrator;
import android.os.Vibrator;

public class AuroraMuteAndVibrateLinkage {
	private Context mContext = null ;
	private AudioManager mAudioManager = null;
	
	public AuroraMuteAndVibrateLinkage(Context context,AudioManager audioManager){
		this.mContext = context;
		this.mAudioManager = audioManager;
	}


	public boolean isSilent(){
		int ringMode = mAudioManager.getRingerMode();
		if((AudioManager.RINGER_MODE_SILENT == ringMode) || (ringMode== AudioManager.RINGER_MODE_VIBRATE)){
    		return true;
    	}
    	return false;  		
	}
		
	public boolean isVibrate(){
		int ringMode = mAudioManager.getRingerMode();
		if(ringMode == AudioManager.RINGER_MODE_VIBRATE || 
			((ringMode == AudioManager.RINGER_MODE_NORMAL) && 
				(Settings.System.getInt(mContext.getContentResolver(),Settings.System.VIBRATE_WHEN_RINGING, 0) != 0))){
			return true;
		}
		return false;
	}
	
	
	public boolean silentChecked(boolean isChecked){
		boolean mVibrate = isVibrate();

		if(isChecked){
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,0);
			if(mVibrate){
				mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			} else{
				mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			}
		}else{
			mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			if(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0){
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2, 0);
			}
			if(mVibrate){
				Settings.System.putInt(mContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,1);
			} else{
				Settings.System.putInt(mContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,0);
			}
		}
		return isVibrate();
	}

	public void vibrateChecked(boolean isChecked){

		int ringMode = mAudioManager.getRingerMode();
		if(isChecked){
			if(ringMode == AudioManager.RINGER_MODE_NORMAL){
				Settings.System.putInt(mContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,1);
			}else if(ringMode == AudioManager.RINGER_MODE_SILENT || ringMode == AudioManager.RINGER_MODE_VIBRATE){
				mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			}
		} 
		else {
			if(ringMode == AudioManager.RINGER_MODE_NORMAL){
				Settings.System.putInt(mContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,0);
			}else if(ringMode == AudioManager.RINGER_MODE_VIBRATE){
				mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			}
		}
	}
}
