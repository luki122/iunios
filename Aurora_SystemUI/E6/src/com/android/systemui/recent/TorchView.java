package com.android.systemui.recent;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

public class TorchView extends FrameLayout {

	private static final String TAG = "TorchView";

	private Camera mCamera;
	private Camera.Parameters mParam;
	private Context mContext;

	private SurfaceView mSurface;
	
	public static final String ACTION_TOGGLE_TORCH = "iuni.intent.action.TOGGLE_TORCH";
	
	public static final String EXTRA_TORCH_STATE = "torch_state";
	public static final int TORCH_STATE_ON = 1;
	public static final int TORCH_STATE_OFF = 0;

	private BroadcastReceiver mReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context contesxt, Intent intent) {
			String action = intent.getAction();
			if(ACTION_TOGGLE_TORCH.equals(action)){
				int state = intent.getIntExtra(EXTRA_TORCH_STATE, TORCH_STATE_OFF);
				if(state == 1 || state ==0){
					toggleFlashLight(state);
				}
			}
		}
	};
	
	public TorchView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		toggleFlashLight(TORCH_STATE_OFF);
		IntentFilter mFileter = new IntentFilter(ACTION_TOGGLE_TORCH);
		mContext.registerReceiver(mReceiver, mFileter);
	}
	
	public TorchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public TorchView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}
	
	/**
	 * @param state
	 *            1 means torch 0 means off
	 * */
	public void toggleFlashLight(int state) {
		if (mCamera == null) {
			try {
				mCamera = Camera.open();
			} catch (Exception e) {
				return;
			}
		}
		if (state == 0) {
			logd("turn off torch");
			mParam = mCamera.getParameters();
			// mParam.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			mParam.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(mParam);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
			invalidate();
			if (mSurface != null) {
				removeView(mSurface);
				mSurface = null;
			}
			setVisibility(View.GONE);

		} else {
			logd("turn on torch");
			setVisibility(View.VISIBLE);
			if (mSurface == null)
			mSurface = new SurfaceView(mContext);
			addView(mSurface, 1, 1);
			post(new Runnable() {

				@Override
				public void run() {
					logd("turn on torch start");
					openTorch();
				}
			});
		}

	}

	private void openTorch() {
			mParam = mCamera.getParameters();
			mParam.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			

			List localList = mParam.getSupportedPreviewSizes();
			Camera.Size localSize = (Camera.Size) localList.get(0);
			Iterator localIterator = localList.iterator();
			Object o3 = null;

			for (Object o2 = localSize;; o2 = o3) {
				if (localIterator.hasNext()) {
					o3 = (Camera.Size) localIterator.next();
					if (((Camera.Size) o3).height * ((Camera.Size) o3).width < ((Camera.Size) o2).height
							* ((Camera.Size) o2).width)
						continue;
				} else {
					mParam.setPreviewSize(((Camera.Size) o2).width,
							((Camera.Size) o2).height);
					mCamera.setParameters(mParam);
					try {
						mCamera.setPreviewDisplay(mSurface.getHolder());
					} catch (IOException e) {
						e.printStackTrace();
					}
					mCamera.startPreview();
					logd("turn on torch over");
					break;
				}
			}
	}

	private void logd(String log){
		android.util.Log.d(TAG, log);
	}
/*
	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
android.util.Log.e("haha","5------------------>onAttachedToWindow");
		toggleFlashLight(TORCH_STATE_OFF);
		IntentFilter mFileter = new IntentFilter(ACTION_TOGGLE_TORCH);
		mContext.registerReceiver(mReceiver, mFileter);
	}

	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
android.util.Log.e("haha","6------------------>onDetachedFromWindow");
		toggleFlashLight(TORCH_STATE_OFF);
		mContext.unregisterReceiver(mReceiver);
	}
*/	
	

}
