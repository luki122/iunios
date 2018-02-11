package com.android.phone;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Message;
import android.util.Log;

import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Connection;

import android.os.Handler;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.PhoneConstants;

public class AuroraHandAnswerManager extends Handler {
	private static final String LOG_TAG = "AuroraHandAnswerManager";
	
	private SharedPreferences mSP = null;
	private boolean mFlag = false;
	private SensorManager mSensorMgr;
	private Sensor mHandAnswerSensor;
	private Context mContext;
	private boolean mIsAnswer = false;

	private CallManager mCM;
	private static final int PHONE_STATE_CHANGED = 1;
	public static final int TYPE_HAND_ANSWER = 41;

	public AuroraHandAnswerManager(CallManager cm, Context context) {
		mCM = cm;
		mContext = context;
		mCM.registerForPreciseCallStateChanged(this, PHONE_STATE_CHANGED,null);
		mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
//		mHandAnswerSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_HAND_ANSWER);
		mHandAnswerSensor = mSensorMgr.getDefaultSensor(TYPE_HAND_ANSWER);
		mSP = mContext.getSharedPreferences("com.android.phone_preferences", Context.MODE_PRIVATE);
	}

	public void handleMessage(Message msg) {
		switch (msg.what) {
		case PHONE_STATE_CHANGED:
			handleEvent();
			break;
		}
	}

	private void handleEvent() {
		Log.i(LOG_TAG, "handleEvent");
		PhoneConstants.State state = mCM.getState();
		if (state == PhoneConstants.State.RINGING) {
			int enable = android.provider.Settings.System.getInt(
					PhoneGlobals.getInstance().getContentResolver(),
					"smart_phone_answer",
					0);
			if (false == mFlag && enable == 1) {
				Log.i(LOG_TAG, "handleEvent register");
				mSensorMgr.registerListener(mSensorEventListener,
						mHandAnswerSensor, SensorManager.SENSOR_DELAY_NORMAL);
				mFlag = true;
				mIsAnswer = false;
			}
		} else {
			if (true == mFlag) {
				Log.i(LOG_TAG, "handleEvent unregister");
				mSensorMgr.unregisterListener(mSensorEventListener);
				clear();
			}
		}
	}
	
	private final SensorEventListener mSensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			Log.i(LOG_TAG, "onSensorChanged");
			// TODO Auto-generated method stub
			if (PhoneGlobals.getInstance().isShowingCallScreen()
					&& !mIsAnswer) {
				mIsAnswer = true;
				Log.i(LOG_TAG, "answer by sensor");
				AuroraPhoneUtils.internalAnswerCall();
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	};

	private void clear() {
		mIsAnswer = false;
		mFlag = false;
	}
}