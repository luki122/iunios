package com.gionee.autommi;



import android.app.Activity;

import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;

public class FlashTest extends BaseActivity {

	static private final String TAG = "FlashTest";
	String targeF;
    private Camera mCamera;
    Camera.Parameters mParameters = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);


	}
	
    
    public void openFlash() {
        if (null == mCamera) {
            try {
            	Log.i(TAG,"openFlash");
                mCamera = Camera.open();     
                mParameters = mCamera.getParameters();
                String currFlashMode = mParameters.getFlashMode();
                if (currFlashMode == null
                        || (!currFlashMode.equals(Camera.Parameters.FLASH_MODE_TORCH))) {
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(mParameters);
                    mCamera.startPreview();
                } 
             }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
    	Log.i(TAG,"onResume");
		openFlash();

	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
    	Log.i(TAG,"onPause");
		if(mCamera!= null){
	    Log.i(TAG,"onPausemCamera!= null");
	    		mCamera.stopPreview();
                mCamera.release();
                mCamera = null;}
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
        Log.d("aaaa", "onStop");
		this.finish();
	}
}
