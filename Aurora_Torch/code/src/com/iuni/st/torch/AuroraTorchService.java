package com.iuni.st.torch;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;

public class AuroraTorchService extends Service implements AuroraTorchView.Callback{

	private final String TAG = "TorchService";

	private boolean isAdded = false; // flag for torch view added
	private static WindowManager wm;
	private static WindowManager.LayoutParams params;
	private static AuroraTorchView torchView;
	public static final int MESSAGE_TURN_ON_TORCH = 101;
	public static final int MESSAGE_TURN_OFF_TORCH = 102;
	public static final int MESSAGE_TORCH_ERROR = 103;
	private int mCurrentState = 0;
	// Handler to control torch state (torchview)
	private Handler controlTorchStateHandler;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case MESSAGE_TURN_ON_TORCH:
				controlTorch(true);
				break;
			case MESSAGE_TURN_OFF_TORCH:
				controlTorch(false);
				break;
			case MESSAGE_TORCH_ERROR:
				// as error happened, do nothing now
				//hideFloatView();
				break;
			}
		}
	};
	
	private void controlTorch(boolean on){
		AuroraDebugUtils.torchLogd(TAG, "isAdded: " + isAdded + " controlTorchStateHandler: " + controlTorchStateHandler + " On: " + on);
		if(isAdded == false || controlTorchStateHandler == null){
			Toast.makeText(this,
					R.string.camera_open_error,
					Toast.LENGTH_SHORT).show();
		} else {
			controlTorchStateHandler.sendEmptyMessage(on ? MESSAGE_TURN_ON_TORCH : MESSAGE_TURN_OFF_TORCH);
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		AuroraDebugUtils.torchLogd(TAG, "onCreate");
		initFloatView();
		// change 'control torch state' method, do not use torch view's life cycle to control torch state
		// create float view as service start
		createFloatView();
	}

	@Override
	public void onDestroy() {
		//mHandler.sendEmptyMessage(MESSAGE_TURN_OFF_TORCH);
		// as service stopped, remove float view
		hideFloatView();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null){
			int state = intent.getIntExtra(AuroraTorchView.EXTRA_TORCH_STATE,
					AuroraTorchView.TORCH_STATE_OFF);
			if(mCurrentState != state) {
				mCurrentState = state;
				AuroraDebugUtils
				.torchLogd(TAG, "start service=> state: " + state);
				
				int what = MESSAGE_TURN_OFF_TORCH;
				if(state == 0){
					what = MESSAGE_TURN_OFF_TORCH;
				}else if(state==1){
					what = MESSAGE_TURN_ON_TORCH;
				}else{
					what = MESSAGE_TORCH_ERROR;
				}
				mHandler.removeMessages(what);
				mHandler.sendEmptyMessage(what);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private void initFloatView() {

		AuroraDebugUtils.torchLogd(TAG, "initFloatView: start");

		torchView = new AuroraTorchView(this);
		torchView.setCallBack(this);
		wm = (WindowManager) getApplicationContext().getSystemService(
				Context.WINDOW_SERVICE);
		params = new WindowManager.LayoutParams();
		// set window type
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		params.format = PixelFormat.RGBA_8888; 

		// set Window flag
		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

		// set float view's location and size
		params.width = 0;
		params.height = 0;
		params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
		params.x = 0;
		params.y = 0;

		AuroraDebugUtils.torchLogd(TAG, "initFloatView: end");
	}

	/**
	 * create torch view
	 */
	private void createFloatView() {
		
		AuroraDebugUtils.torchLogd(TAG, "createFloatView: wm is null? " + (wm == null) + " isAdded: " + isAdded);
		if (wm != null ) {
			if(!isAdded){
				wm.addView(torchView, params);
				isAdded = true;
			}
		}
	}

	/**
	 * hide torch view
	 */
	private void hideFloatView() {
		AuroraDebugUtils.torchLogd(TAG, "createFloatView: wm is null? "
				+ (wm == null) + " isAdded: " + isAdded);
		if (wm != null && isAdded) {
			wm.removeView(torchView);
			isAdded = false;
		}
	}
	
	@Override
	public void doErrorState() {
		android.util.Log.e("hahaha","Should Come Here");
		mHandler.removeMessages(MESSAGE_TORCH_ERROR);
		mHandler.sendEmptyMessage(MESSAGE_TORCH_ERROR);
	}

	// TorchView will give thandler to serivce, see TorchView.setCallBack()
	@Override
	public void processTorch(Handler tHandler) {
		controlTorchStateHandler = tHandler;
	}
}
