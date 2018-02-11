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

public class AuroraInCallGestureManager extends Handler {
	private static final String LOG_TAG = "AuroraInCallGestureManager";
	
	private SharedPreferences mSP = null;
	private boolean mFlag = false;
	private SensorManager mSensorMgr;
	private Sensor mSensor;
	private Context mContext;
	private boolean mIsDone = false;

	private CallManager mCM;
	private static final int PHONE_STATE_CHANGED = 1;
	public static final int TYPE_INCALL_GESTURE = 44;
    public static final int TYPE_GESTURE		= 46; 
	public static final int DIR_UP = 3;
	public static final int DIR_DOWN = 4;

	public AuroraInCallGestureManager(CallManager cm, Context context) {
		mCM = cm;
		mContext = context;
		mCM.registerForPreciseCallStateChanged(this, PHONE_STATE_CHANGED,null);
		mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorMgr.getDefaultSensor(TYPE_GESTURE);
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
		PhoneConstants.State state = mCM.getState();
		if (state == PhoneConstants.State.RINGING) {
			int enable = android.provider.Settings.System.getInt(
					PhoneGlobals.getInstance().getContentResolver(),
					"smart_gesture_answer",
					0);
			if (false == mFlag && enable == 1) {
				mSensorMgr.registerListener(mSensorEventListener,
						mSensor, 12000);
				mFlag = true;
				mIsDone = false;
			}
		} else {
			if (true == mFlag) {
				mSensorMgr.unregisterListener(mSensorEventListener);
				clear();
			}
		}
	}
	
	private final SensorEventListener mSensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			if (event.sensor.getType() == TYPE_GESTURE
					&& PhoneGlobals.getInstance().isShowingCallScreen()
					&& !mIsDone) {	
				if(event.values[0] == DIR_DOWN) {
					Log.i(LOG_TAG, "answer by gesture");
					AuroraPhoneUtils.internalAnswerCall();
					mIsDone = true;
				} else if(event.values[0] == DIR_UP){
					Log.i(LOG_TAG, "hangup by gesture");
				    PhoneUtils.hangupRingingCall(mCM.getFirstActiveRingingCall());
					mIsDone = true;
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	};

	private void clear() {
		mIsDone = false;
		mFlag = false;
	}

}