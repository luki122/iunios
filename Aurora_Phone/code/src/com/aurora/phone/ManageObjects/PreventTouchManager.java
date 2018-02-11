package com.android.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Connection;
import android.os.Handler;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.PhoneConstants;

public class PreventTouchManager extends Handler {
	private static final String LOG_TAG = "PreventTouchManager";

	private static SharedPreferences mSP = null;
	private SensorManager mSensorMgr;
	private boolean mProximitySensorFlg = false;
	private Sensor mProximitySensor;
	private float mProximityThreshold = 5.0f;
	public static volatile boolean mIsTouchEnable = true;
    public static volatile boolean mIsCancelSensorByUser = false;
    public static volatile boolean mIsProximityOn = false;
	public static volatile long mLastProximityActionTime;
	private PowerManager mPowerManager;
	private Call.State mPreviousCallState = Call.State.IDLE;
	private Context mContext;

	private CallManager mCM;
	private static final int PHONE_STATE_CHANGED = 1;

	public PreventTouchManager(CallManager cm, Context context) {
		mCM = cm;
		mContext = context;
		mCM.registerForPreciseCallStateChanged(this, PHONE_STATE_CHANGED,null);
		mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		mSP = mContext.getSharedPreferences("com.android.phone_preferences", Context.MODE_PRIVATE);
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
	}

	public void handleMessage(Message msg) {
		switch (msg.what) {
		case PHONE_STATE_CHANGED:
			handleScreenReceiver();
			handleProSensor();
			break;
		}
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

			boolean defIncomingTouchSwitch = mContext.getResources().getBoolean(
					R.bool.aurora_def_incoming_touch_switch);
			boolean touchSwitch = mSP != null
					&& mSP.getBoolean("aurora_incoming_touch_switch",
							defIncomingTouchSwitch);
			log("onSensorChanged: touchSwitch =" + touchSwitch);

			if (mCM.getState() == PhoneConstants.State.RINGING && touchSwitch
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

			if (mPreviousCallState == Call.State.DIALING
					|| mPreviousCallState == Call.State.ALERTING) {
				if (positive) {
//					getInCallScreen().mCallCardAnimController.stopGuangQuanAnimation();
				} else {
//					getInCallScreen().mCallCardAnimController.startGuangQuanAnimationIfNotStart();
				}
			}

		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	};

	private void handleProSensor() {
	    mPreviousCallState = mCM.getFirstActiveRingingCall().getState();
        if(mPreviousCallState == Call.State.IDLE) {
        	mPreviousCallState = mCM.getActiveFgCallState();
        }
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
			reset();
		}
	}

	private boolean isUsePro() {
		PhoneConstants.State state = mCM.getState();
		log("handleProSensor: mProximitySensorFlg" + mProximitySensorFlg
				+ " mProximitySensor" + mProximitySensor
				+ " mPowerManager.isScreenOn() = " + mPowerManager.isScreenOn());
		if (Build.VERSION.SDK_INT >= 18) {
			return state == PhoneConstants.State.RINGING
					&& mPowerManager.isScreenOn();
		} else {
			return state != PhoneConstants.State.IDLE
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
		PhoneConstants.State state = mCM.getState();
		if (state != PhoneConstants.State.IDLE) {
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
    
    private InCallScreen getInCallScreen() {
    	return  PhoneGlobals.getInCallScreen();
    }
}