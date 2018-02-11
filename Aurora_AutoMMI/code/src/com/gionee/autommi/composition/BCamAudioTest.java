package com.gionee.autommi.composition;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.gionee.autommi.AutoMMI;
import com.gionee.autommi.BackCameraTest;
import com.gionee.autommi.BaseActivity;
import com.gionee.autommi.CameraPreview;
import com.gionee.autommi.R;

public class BCamAudioTest extends BaseActivity implements PictureCallback, AutoFocusCallback {
	protected static final int START_AUDIO_CAP = 0;
	protected static final int STOP_AUDIO_CAP = 1;
	protected static final int PLAY_BACK = 2;
	private MediaRecorder recorder;
	private MediaPlayer player;
	private AudioManager am;
	private String filePath = "/data/amt/audio.mp4";
	private int duration = 2000;
	private static final String DURA = "dura";
	
	private static final int AUTO_FOCUS = 8;
	private static final int TAKE_PIC = 9;
	private static final String TAG = "BCamAudioTest";
	private Camera bCamera;
	private CameraPreview mPreview;
	protected String picPath = "/data/amt/b.jpg";

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
			case AUTO_FOCUS:
				Log.d(TAG, "---AUTO_FOCUS---");
				bCamera.autoFocus(BCamAudioTest.this);
				break;
			case TAKE_PIC:
				bCamera.takePicture(null, null, BCamAudioTest.this);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent it = this.getIntent();
		duration = Integer.parseInt(it.getStringExtra(DURA)) * 1000;
		
		this.setContentView(R.layout.camera);
	    Button captureButton = (Button) findViewById(R.id.button_capture);
	    captureButton.setVisibility(View.GONE); 

	    captureButton.setOnClickListener(
	        new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                // get an image from the camera
	                bCamera.takePicture(null, null, BCamAudioTest.this);
	            }
	        }
	    );
	    bCamera = Camera.open(0);
	    maxCameraPicutreSize(bCamera);
	    setCameraDisplayOrientation(this, 0, bCamera);
	    mPreview = new CameraPreview(this, bCamera);
	    FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
	    preview.addView(mPreview);
	    ((AutoMMI)getApplication()).recordResult(BackCameraTest.TAG, "", "0");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		handler.sendEmptyMessageDelayed(AUTO_FOCUS, 2000);
		handler.sendEmptyMessage(START_AUDIO_CAP);
		handler.sendEmptyMessageDelayed(STOP_AUDIO_CAP, duration);
		handler.sendEmptyMessageDelayed(PLAY_BACK, duration + 500);
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
		bCamera.release();
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
		player.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
		try {
			player.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		player.start();
	}
	
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		try {
			OutputStream os = new FileOutputStream(picPath);
			os.write(data);
			os.close();
			((AutoMMI)getApplication()).recordResult(BackCameraTest.TAG, "", "1");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void setCameraDisplayOrientation(Activity activity,
	         int cameraId, android.hardware.Camera camera) {
	     android.hardware.Camera.CameraInfo info =
	             new android.hardware.Camera.CameraInfo();
	     android.hardware.Camera.getCameraInfo(cameraId, info);
	     int rotation = activity.getWindowManager().getDefaultDisplay()
	             .getRotation();
	     int degrees = 0;
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     camera.setDisplayOrientation(result);
	 }

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		// TODO Auto-generated method stub
		if (success) {
			handler.sendEmptyMessage(TAKE_PIC);
			Log.d(TAG, "auto_focus success!"); 
		} else {
			Log.d(TAG, "fail to auto_focus"); 
		}
	}
}
