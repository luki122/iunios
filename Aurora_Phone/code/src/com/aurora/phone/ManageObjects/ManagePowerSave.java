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
import android.view.View;

import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Connection;

import android.os.Handler;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.PhoneConstants;

public class ManagePowerSave extends Handler {
	private static final String LOG_TAG = "ManagePowerSave";

	private static SharedPreferences mSP = null;
	private SensorManager mSensorMgr;
	private boolean mProximitySensorFlg = false;
	private Sensor mProximitySensor;
	private float mProximityThreshold = 5.0f;
    public static volatile boolean mIsProximityOn = false;
	private PowerManager mPowerManager;
	private Context mContext;

	private CallManager mCM;
	private static final int PHONE_STATE_CHANGED = 1;
	private PowerManager.WakeLock mScreenOnLock;
	
	public static boolean sIsPowerSaving = true;
	public static boolean sIsUseSmartRinger = false;

	public ManagePowerSave(CallManager cm, Context context) {
		mCM = cm;
		mContext = context;
		mCM.registerForPreciseCallStateChanged(this, PHONE_STATE_CHANGED,null);
		mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		mSP = mContext.getSharedPreferences("com.android.phone_preferences", Context.MODE_PRIVATE);
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mScreenOnLock = mPowerManager.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK
						| PowerManager.ACQUIRE_CAUSES_WAKEUP,
				"ManagePowerSave");
		mScreenOnLock.setReferenceCounted(false);
	}

	public void handleMessage(Message msg) {
		switch (msg.what) {
		case PHONE_STATE_CHANGED:
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
			mIsProximityOn = positive;
			Log.i(LOG_TAG, " proximity onSensorChanged positive = " + positive
					+ " distance = " + distance);

			log("onSensorChanged: isPowerSaving =" + sIsPowerSaving);
			
			sIsUseSmartRinger = positive;

			if (!positive) {
				if(!mScreenOnLock.isHeld()) {
					ManagePowerSave.this.postDelayed(new Runnable() {
						@Override
						public void run() {
							mScreenOnLock.acquire();
						}

					}, 500);
		
				}
			} else {
				
			}

		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	};

	private void handleProSensor() {
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
		log("isUsePro: mProximitySensorFlg" + mProximitySensorFlg
				+ " mProximitySensor" + mProximitySensor);
	    return state == PhoneConstants.State.RINGING;
		
	}
	
	
	public void reset() {
		if (mProximitySensorFlg) {
			log("updateState: mProSensorEventListener unregisterListener");
			mSensorMgr.unregisterListener(mProSensorEventListener);
			mProximitySensorFlg = false;
		}
		if(mScreenOnLock.isHeld()) {
			mScreenOnLock.release();
		}
	}
	
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
    
    private InCallScreen getInCallScreen() {
    	return  PhoneGlobals.getInCallScreen();
    }
}