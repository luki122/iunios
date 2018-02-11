package com.iuni.st.torch;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

public class AuroraTorchView extends FrameLayout {

	private final String TAG = "TorchView";

	private Camera mCamera;
	private Camera.Parameters mParam;
	private Context mContext;

	private SurfaceView mSurface;

	public static final String ACTION_TOGGLE_TORCH = "iuni.intent.action.TOGGLE_TORCH";

	public static final String EXTRA_TORCH_STATE = "torch_state";
	public static final int TORCH_STATE_ON = 1;
	public static final int TORCH_STATE_OFF = 0;
	public static final int TORCH_STATE_ERROR = -1;

	private Callback mCallBack;
	private boolean addFlag = false;
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case AuroraTorchService.MESSAGE_TURN_ON_TORCH:
				toggleFlashLight(TORCH_STATE_ON);
				break;
			case AuroraTorchService.MESSAGE_TURN_OFF_TORCH:
				toggleFlashLight(TORCH_STATE_OFF);
				break;
			}
		}
	};

	public void setCallBack(Callback callback) {
		mCallBack = callback;
		// give handler to service
		mCallBack.processTorch(mHandler);
	}

	private void processErrorState() {
		
		//at the begining, i use broadcast to tell service the state is error
		//as u3's broadcast has some problem, so change to us callback
		/*
		 * Intent intent = new
		 * Intent(AuroraTorchReceiver.ACTION_CHANGE_TORCH_STATE);
		 * intent.putExtra(EXTRA_TORCH_STATE, TORCH_STATE_ERROR);
		 * mContext.sendBroadcast(intent);
		 */
		if (mCallBack != null)
			mCallBack.doErrorState();

	}

	// this receiver use to receive the action to turn on/off torch
	// now use view's lifestyle to control torch on/off
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context contesxt, Intent intent) {
			String action = intent.getAction();
			if (ACTION_TOGGLE_TORCH.equals(action)) {
				int state = intent.getIntExtra(EXTRA_TORCH_STATE, TORCH_STATE_OFF);
				if (state == 1 || state == 0) {
					toggleFlashLight(state);
				} else {
					AuroraDebugUtils.torchLoge(TAG, "get error state param.");
					processErrorState();
				}
			}
		}
	};

	Runnable openTorchRunnable = new Runnable() {

		@Override
		public void run() {
			openTorch();
		}
	};

	public AuroraTorchView(Context context) {
		super(context);
		mContext = context;
	}

	public AuroraTorchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public AuroraTorchView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	/**
	 * use to control flashlight state
	 * 
	 * @param state
	 *                1 means torch on, 0 means off;
	 * */
	public void toggleFlashLight(int state) {
		AuroraDebugUtils.torchLoge(TAG, "Start toggleFlashLight: state: " + state);
		if (mCamera == null && state == 0)
			return;
		if (mCamera == null) {
			try {
				mCamera = Camera.open();
			} catch (Exception e) {
				AuroraDebugUtils.torchLoge(TAG,
						"camera can not open! reason: "
								+ e.toString());
				processErrorState();
				Toast.makeText(mContext,
						R.string.camera_open_error,
						Toast.LENGTH_SHORT).show();
				return;
			}
		}
		if (state == 0) {
			removeCallbacks(openTorchRunnable);
			mParam = mCamera.getParameters();
			mParam.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(mParam);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
			invalidate();
			if (mSurface != null) {
				addFlag = false;
				AuroraDebugUtils.torchLoge(TAG, "remove mSurface");
				removeView(mSurface);
				mSurface = null;
			}
			setVisibility(View.GONE);
		} else {
			AuroraDebugUtils.torchLoge(TAG,
					"toggleFlashLight: setVisibility");
			setVisibility(View.VISIBLE);
			if (mSurface == null)
				mSurface = new SurfaceView(mContext);
			// flag to avoid addView twice
			if(addFlag == false) {
				AuroraDebugUtils.torchLoge(TAG, "add mSurface");
				addView(mSurface, 1, 1);
				addFlag = true;
			} 
			AuroraDebugUtils.torchLoge(TAG, "toggleFlashLight: post");
			// why need post,why can't use 'openTorch()' directly, i
			// don't know.
			post(openTorchRunnable);
		}

	}

	/**
	 * turn on flash light
	 * */
	//why this method must use post? i do not know.
	private void openTorch() {

		// post() ???

		AuroraDebugUtils.torchLoge(TAG, "Start Open Torch");
		mParam = mCamera.getParameters();
		mParam.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

		List<Camera.Size> localList = mParam.getSupportedPreviewSizes();
		Camera.Size localSize = (Camera.Size) localList.get(0);
		Iterator localIterator = localList.iterator();
		Camera.Size size3 = null;
		
		//get the smallest camera preview size 
		for (Camera.Size size2 = localSize;; size2 = size3) {
			if (localIterator.hasNext()) {
				size3 = (Camera.Size) localIterator.next();
				AuroraDebugUtils.torchLogd(TAG, "size3: "
						+ size3.width + " x "
						+ size3.height);
				if (size3.height * size3.width < size2.height
						* size2.width)
					continue;
			} else {
				mParam.setPreviewSize(size2.width, size2.height);
				mCamera.setParameters(mParam);
				try {
					mCamera.setPreviewDisplay(mSurface.getHolder());
				} catch (IOException e) {
					e.printStackTrace();
				}
				mCamera.startPreview();
				AuroraDebugUtils.torchLoge(TAG, "Finish Open Torch");
				break;
			}
		}
	}

	//do not use view's life cycle to control torch on/off
	/*@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		AuroraDebugUtils.torchLogd(TAG, "onAttachedToWindow turn on torch");
		toggleFlashLight(TORCH_STATE_ON);
	}*/
	
	//do not use view's life cycle to control torch on/off
	/*@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		AuroraDebugUtils.torchLogd(TAG, "onDetachedFromWindow turn off torch");
		toggleFlashLight(TORCH_STATE_OFF);
		// //use view's lifestyle to control torch
		// mContext.unregisterReceiver(mReceiver);
	}*/

	interface Callback {
		public void doErrorState();
		public void processTorch(Handler mHandler);
	}

}
