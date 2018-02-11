package com.gionee.autommi;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public abstract class MicTest extends BaseActivity {
	private static  String TAG = "MicTest";
	protected static final int START_AUDIO_CAP = 0;
	protected static final int STOP_AUDIO_CAP = 1;
	protected static final int PLAY_BACK = 2;
	private MediaRecorder recorder;
	private MediaPlayer player;
	protected AudioManager am;
	private String filePath = "/data/amt/audio.mp4";
	private int duration = 2000;
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
		Intent it = this.getIntent();
		String t = it.getStringExtra(DURA);
		if( null == t) {
			t = "3";
		}
                Log.v("gary","onCreate---t==="+t);
		duration = Integer.parseInt(t) * 1000;
		am  = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		chooseMic();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		handler.sendEmptyMessage(START_AUDIO_CAP);
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
                        recorder.start();
                        handler.sendEmptyMessageDelayed(STOP_AUDIO_CAP, duration);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void stopCaptureAudio() {
		// TODO Auto-generated method stub
		if (null != recorder) {
			recorder.stop();
			recorder.release();
		}
                handler.sendEmptyMessageDelayed(PLAY_BACK, 500);
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
//		player.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
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
		
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				Log.d(TAG, "---player  Completion---");
				setAudioParameters();
			}
		});
		
	}
	
	protected void setAudioParameters() {
		// TODO Auto-generated method stub
		Log.d(TAG, "setAudioParameters MMIMic=0");
		am.setParameters("MMIMic=0");
	}

	protected abstract void chooseMic();
}