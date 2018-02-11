package com.aurora.voiceassistant.model;

import com.sogou.tts.offline.TTSPlayer;
import com.sogou.tts.offline.listener.TTSPlayerListener;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

public class Tts {
	private String TAG = "iuni-ht";
	private Context context;
	private TTSPlayer mTTsPlayer;
	private TtsplayerListener mTTsPlayerListener;
	
	//shigq fix bug #11677 start
	private AudioManager mAudioManager;
	//shigq fix bug #11677 end
	
	public Tts(Context context) {
		this.context = context;
	}

	public boolean init(){
		boolean init  = false;
		mTTsPlayer = new TTSPlayer();
		if(mTTsPlayerListener == null){
			mTTsPlayerListener = new TtsplayerListener();
		}
		init = mTTsPlayer.init(context, mTTsPlayerListener,"");
		if(init){
				mTTsPlayer.setStreamType(TTSPlayer.STREAM_MUSIC);
				//M:liuzuo setspeed   0.8f>>1.0f begin 
				mTTsPlayer.setSpeed(0.8f);
				//M:liuzuo setspeed   0.8f>>1.0f end  
		}
		
		
		//shigq fix bug #11677 start
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		//shigq fix bug #11677 end
		
		Log.e("linp", "###########aaa######################init="+init);
		return init;
//		try {
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
	}

	public void deinit() {
		if(mTTsPlayer == null){
			return;
		}
		mTTsPlayer.release();
	}

	public void play(String text){
		if(mTTsPlayer == null){
			return;
		}
		mTTsPlayer.synthesize(text, "");
		mTTsPlayer.play();
	}
	
	public void stop() {
		if(mTTsPlayer == null){
			return;
		}
		mTTsPlayer.stop();
	}
	
	public class TtsplayerListener implements TTSPlayerListener{

		@Override
		public void onEnd() {
			// TODO Auto-generated method stub
			//shigq fix bug #11677 start
			mAudioManager.abandonAudioFocus(null);
			//shigq fix bug #11677 end
		}

		@Override
		public void onError(int arg0) {
			Log.e("linp", "########################int arg0="+arg0);
			// TODO Auto-generated method stub
		}

		@Override
		public void onStart() {
			// TODO Auto-generated method stub
			//shigq fix bug #11677 start
			mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
			//shigq fix bug #11677 end
		}
		@Override
		public void onSegSyn(String arg0, float[] arg1, byte[] arg2) {
			// TODO Auto-generated method stub
			
		}

		
		@Override
		public void onSynEnd() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSynStart() {
			// TODO Auto-generated method stub
			
		}
	} 
}
