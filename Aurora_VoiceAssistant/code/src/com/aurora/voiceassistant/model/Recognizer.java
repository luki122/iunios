package com.aurora.voiceassistant.model;

import java.io.ByteArrayOutputStream;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.sogou.speech.framework.CoreControl;
import com.sogou.speech.listener.OutsideCallListener;

public class Recognizer {
	private Context context;
	private CoreControl mCore;
	private Handler mHandler;
	private final ByteArrayOutputStream mWaveBuffer = new ByteArrayOutputStream();
	
	//private String appId = "YHZCS001";
	//private String accessKey = "62t8HHtl";
	// applied APP ID
	private String appId = "DF465202";
	// applied Access Key
	private String accessKey = "EA382D6A";
	
	public final static int MSG_READY_START = 4;
	public final static int MSG_ON_START = 0;
	public final static int MSG_ON_ENDOFSPEECH = 1;
	public final static int MSG_ON_RESULT = 2;
	public final static int MSG_ON_ERROR = 3;
	
	
	public Recognizer(Context context,Handler mHandler) {
		this.context = context;
		this.mHandler = mHandler;
	}
	
	public void startListening() {
		mCore = new CoreControl(appId, accessKey, context, true,true);
		mCore.setRecognizingListener(mOcListener);
		mCore.startListening();
	}

	public void cancelListening() {
		if (mCore != null) {
			mCore.cancelListening();
			mCore.destroy();
			mCore = null;
		}
	}
	
	public void stopRecord() {
		if (mCore != null) {
			mCore.stopRecordTask();
		}
	}
	
	private OutsideCallListener mOcListener = new OutsideCallListener() {
		@Override
		// call it when microphone is ready for recording
		public void onReadyForSpeech(Bundle params) {
			Log.i("RE","onReadyForSpeech");
			Log.e("iuni-ht", "----------------onReadyForSpeech-------------------------------");
			if(mHandler == null){
				return;
			}
			mHandler.obtainMessage(MSG_READY_START).sendToTarget();
		}

		@Override
		// call it when recording has begun
		public void onBeginningOfSpeech() {
			Log.i("RE","onBeginningOfSpeech");
			Log.e("iuni-ht", "----------------onBeginningOfSpeech-------------------------------");
			if(null == mHandler) return ;
			mHandler.obtainMessage(MSG_ON_START).sendToTarget();
		}

		@Override
		// call it when average value of the audio has been changed
		public void onRmsChanged(float rmsdB) {
			//Log.i("RE","onRmsChanged");
		}

		@Override
		// call it when some audio has been recorded
		public void onBufferReceived(short[] voiceBuffer) {
			for (short singlebuf : voiceBuffer) {
				try {
					mWaveBuffer.write((byte) (singlebuf & 0x00ff));
					mWaveBuffer.write((byte) ((singlebuf >> 8) & 0x00ff));
				} catch (Exception e) {
					
				}
			}
			//Log.i("RE","onBufferReceived");
		}

		@Override
		// call it when recording has been stopped
		public void onEndOfSpeech() {
			Log.i("RE","onEndOfSpeech");
			Log.e("iuni-ht", "----------------onEndOfSpeech-------------------------------");
			mWaveBuffer.reset();
			if(null == mHandler) return ;
			mHandler.obtainMessage(MSG_ON_ENDOFSPEECH).sendToTarget();
		}

		@Override
		// call it when the former packages have received valid results
		public void onPartResults(List<List<String>> results) {
			Log.i("RE","onPartResults");
			Log.e("iuni-ht", "----------------onPartResults-------------------------------");
			if(null == mHandler) return ;
			mHandler.obtainMessage(MSG_ON_RESULT, results).sendToTarget();
		}

		@Override
		// call it when the last package has received valid results
		public void onResults(List<List<String>> results) {
			Log.i("RE","onResults");
			Log.e("iuni-ht", "----------------onResults-------------------------------");
			if(null == mHandler) return ;
			mHandler.obtainMessage(MSG_ON_RESULT, results).sendToTarget();
			delayQuit();
		}

		// call it when the last package has no valid result but the former
		// packages
		// have some valid results
		public void onQuitQuietly(int err) {
			Log.i("RE","onQuitQuietly");
			Log.e("iuni-ht", "----------------onQuitQuietly-------------------------------");
			if(null == mHandler) return;
			mHandler.obtainMessage(MSG_ON_ERROR,err).sendToTarget();
			delayQuit();
		}

		@Override
		// call it when some error occurs
		public void onError(int err) {
			Log.i("RE","onError");
			Log.e("iuni-ht", "----------------onError-------------------------------");
			if(null == mHandler) return;
			mHandler.obtainMessage(MSG_ON_ERROR,err).sendToTarget();
			
			if (mCore != null)
			{
				mCore.destroy();
			}
		}
	};
	
	private void delayQuit() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mWaveBuffer.reset();
				cancelListening();
			}
		}, 500);
	}
}
