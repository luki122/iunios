package com.android.incallui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.android.incallui.CallList;
import com.android.incallui.InCallApp;
import com.android.incallui.InCallPresenter;
import com.android.incallui.InCallPresenter.InCallState;


public class PreventTouchManager {
	private static final String LOG_TAG = "PreventTouchManager";

	private static SharedPreferences mSP = null;
	private static boolean defIncomingTouchSwitch = false ;
	
	private SensorManager mSensorMgr;
	private boolean mProximitySensorFlg = false;
	private Sensor mProximitySensor;
	private float mProximityThreshold = 5.0f;
	public static volatile boolean mIsTouchEnable = true;
    public static volatile boolean mIsCancelSensorByUser = false;
    public static volatile boolean mIsProximityOn = false;
	public static volatile long mLastProximityActionTime;
	private PowerManager mPowerManager;
	private Context mContext;

	public PreventTouchManager(Context context) {
		mContext = context;
		mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		try {
			Context phoneContext = context.createPackageContext("com.aurora.callsetting", Context.CONTEXT_IGNORE_SECURITY);  
			mSP = phoneContext.getSharedPreferences("com.android.phone.settings", Context.MODE_WORLD_READABLE);
		} catch(NameNotFoundException e){
			e.printStackTrace();
		}
		defIncomingTouchSwitch = mContext.getResources().getBoolean(
				R.bool.aurora_def_incoming_touch_switch);
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
	}

	//onphonestatechange
    public void handle(){
		handleScreenReceiver();
		handleProSensor();
    }

	private final SensorEventListener mProSensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			final float distance = event.values[0];
			boolean positive = distance >= 0.0f
					&& distance < mProximityThreshold;
			if (mIsProximityOn = true && positive == false) {
				mLastProximityActionTime = SystemClock.uptimeMillis();
				// mApp.notifier.removeMessages(27);
				// //UPDATE_IN_CALL_NOTIFICATION
				// mHandler.removeCallbacks(mUpdateNotificationRunnable);
			}
			mIsProximityOn = positive;
			Log.i(LOG_TAG, " proximity onSensorChanged positive = " + positive
					+ " distance = " + distance);
		
			boolean touchSwitch = isPreventTouchSwitchOn();
			log("onSensorChanged: touchSwitch =" + touchSwitch);

			if (InCallPresenter.getInstance().getInCallState() == InCallState.INCOMING && touchSwitch
					&& !mIsCancelSensorByUser) {
				mIsTouchEnable = !positive;
				// if(mInCallTouchUi != null) {
				// mInCallTouchUi.setIncomingUiEnabled(mIsTouchEnable);
				// }
			} else {
				mIsTouchEnable = true;
				// if(mInCallTouchUi != null) {
				// mInCallTouchUi.setIncomingUiEnabled(true);
				// }
			}

			if (InCallPresenter.getInstance().getInCallState() == InCallState.PENDING_OUTGOING
					|| InCallPresenter.getInstance().getInCallState() == InCallState.OUTGOING) {
				if (positive) {
					InCallApp.getInCallActivity().mCallCardAnimationController.stopGuangQuanAnimation();
				} else {
					InCallApp.getInCallActivity().mCallCardAnimationController.startGuangQuanAnimationIfNotStart();
				}
			}

		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	};

	private void handleProSensor() {
//		if (!OverTurnManager.mIsUseSensorSwitch) {
//			return;
//		}
		if (isUsePro()) {
			if (!mProximitySensorFlg) {
				if (mProximitySensor == null) {
					mProximitySensor = mSensorMgr
							.getDefaultSensor(Sensor.TYPE_PROXIMITY);
					if (mProximitySensor != null) {
						mProximityThreshold = Math.min(
								mProximitySensor.getMaximumRange(), 5.0f);
					}
				}
				if (mProximitySensor != null) {
					boolean result = mSensorMgr.registerListener(
							mProSensorEventListener, mProximitySensor,
							SensorManager.SENSOR_DELAY_NORMAL);
					log("updateState: mProSensorEventListener registerListener result ="
							+ result);
					mProximitySensorFlg = true;
				}
			}
		} else {
			if (mProximitySensorFlg) {
				log("updateState: mProSensorEventListener unregisterListener");
				mSensorMgr.unregisterListener(mProSensorEventListener);
				mProximitySensorFlg = false;
				mIsTouchEnable = true;
			}
			mIsCancelSensorByUser = false;
		}
	}

	private boolean isUsePro() {
		InCallState state = InCallPresenter.getInstance().getInCallState();
		log("handleProSensor: mProximitySensorFlg" + mProximitySensorFlg
				+ " mProximitySensor" + mProximitySensor
				+ " mPowerManager.isScreenOn() = " + mPowerManager.isScreenOn());
		if (Build.VERSION.SDK_INT >= 18) {
			return state == InCallState.INCOMING
					&& mPowerManager.isScreenOn();
		} else {
			return state != InCallState.NO_CALLS
					&& mPowerManager.isScreenOn();
		}
	}

	private boolean isRegisterScreen = false;
	private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_SCREEN_OFF)
					|| action.equals(Intent.ACTION_SCREEN_ON)) {
				log("ACTION_SCREEN onReceive");
				handleProSensor();
			}
		}

	};

	private void handleScreenReceiver() {
		InCallState state = InCallPresenter.getInstance().getInCallState();
		if (state != InCallState.NO_CALLS) {
			if (!isRegisterScreen) {
				IntentFilter intentFilter = new IntentFilter(
						Intent.ACTION_SCREEN_OFF);
				intentFilter.addAction(Intent.ACTION_SCREEN_ON);
				mContext.registerReceiver(mScreenReceiver, intentFilter);
			}
		} else {
			if (isRegisterScreen) {
				mContext.unregisterReceiver(mScreenReceiver);
			}
		}
	}
	
	public static void cancel() {
        log("-onKeyLongPress back cancel mIsTouchEnable = " + mIsTouchEnable);
    	if(!mIsTouchEnable) {
    		mIsCancelSensorByUser = true;
        	mIsTouchEnable = true;
    	}
	}
	
	public void reset() {
		if (mProximitySensorFlg) {
			log("updateState: mProSensorEventListener unregisterListener");
			mSensorMgr.unregisterListener(mProSensorEventListener);
			mProximitySensorFlg = false;
			mIsTouchEnable = true;
		}
		mIsCancelSensorByUser = false;
	}
	
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
    
    public static boolean isPreventTouchSwitchOn(){
    	boolean touchSwitch = mSP != null && mSP.getBoolean("touch", defIncomingTouchSwitch);
    	return  touchSwitch;
    }
    
}