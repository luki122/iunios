package com.gionee.autommi;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class FrontCameraTest extends BaseActivity implements PictureCallback {
	public static final String TAG = "FrontCameraTest";
	private static final int TAKE_PIC = 0;
	private Camera fCamera;
	private CameraPreview mPreview;
	private Handler handler;
	protected String picPath = "/data/amt/f.jpg";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	    this.setContentView(R.layout.camera);
	    Button captureButton = (Button) findViewById(R.id.button_capture);
	    captureButton.setVisibility(View.GONE); 
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case TAKE_PIC:
					if(fCamera != null){
						fCamera.takePicture(null, null, FrontCameraTest.this);
					}
					break;
				}
			}
		};
	    captureButton.setOnClickListener(
	        new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                // get an image from the camera
	            	if(fCamera != null){
	            		fCamera.takePicture(null, null, FrontCameraTest.this);
	                }
	            }
	        }
	    );
	    
	    try{
	    	fCamera = Camera.open(1);
	    	maxCameraPicutreSize(fCamera);
	    	setCameraDisplayOrientation(this, 0, fCamera);
	    	mPreview = new CameraPreview(this, fCamera);
	    	FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
	    	preview.addView(mPreview);
	    	((AutoMMI)getApplication()).recordResult(TAG, "", "0");
	    }catch(Exception e){
	    	e.printStackTrace();
	    }

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		handler.sendEmptyMessageDelayed(TAKE_PIC, 2000);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop(); 
		if(fCamera != null){
			fCamera.release();
		}
		this.finish();
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		try {
			OutputStream os = new FileOutputStream(picPath);
			os.write(data);
			os.close();
			((AutoMMI)getApplication()).recordResult(TAG, "", "1");
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

}
