package com.gionee.autommi;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer; 
import android.media.AudioManager;
import android.app.Activity;

public class AuxiliaryMicTest extends BaseActivity {

	private AudioManager am;
	private static final String TAG = "AuxiliaryMicTest";
	protected static final int START_AUDIO_CAP = 0;
	protected static final int STOP_AUDIO_CAP = 1;
	protected static final int PLAY_BACK = 2;
	private MediaRecorder recorder;
	private MediaPlayer player;
	private String filePath = "/data/amt/audio.mp4";
	private int duration = 2000;
	private final int switchDelay = 500;
	private static final String DURA = "dura";

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case START_AUDIO_CAP:
				startCaptureAudio();
				break;
			case STOP_AUDIO_CAP:
				stopCaptureAudio();
				break;
			case PLAY_BACK:
				playBack();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		am.setParameters("ForceUseSpecificMic=2");
		Intent it = this.getIntent();
		duration = Integer.parseInt(it.getStringExtra(DURA)) * 1000;
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		handler.sendEmptyMessageDelayed(START_AUDIO_CAP,switchDelay);
		handler.sendEmptyMessageDelayed(STOP_AUDIO_CAP, duration + switchDelay);
		handler.sendEmptyMessageDelayed(PLAY_BACK, duration + switchDelay + 500);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		handler.removeMessages(START_AUDIO_CAP);
		handler.removeMessages(STOP_AUDIO_CAP);
		handler.removeMessages(PLAY_BACK);
		if (null != player) {
			player.stop();
			player.release();
		}
		am.setParameters("ForceUseSpecificMic=1");
		this.finish();
	}

	private void startCaptureAudio() {
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setOutputFile(filePath);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		try {
			recorder.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		recorder.start();
		/*
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		am.setParameters("MMI_MIC_REG=0");
		*/

	}

	private void stopCaptureAudio() {
		// TODO Auto-generated method stub
		if (null != recorder) {
			recorder.stop();
			recorder.release();
		}
	}

	private void playBack() {
		player = new MediaPlayer();	
		try {
			player.setDataSource(filePath);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			player.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(TAG, "---player.start()---");
		player.start();
		Log.d(TAG, "---player  end---");
	}
}

