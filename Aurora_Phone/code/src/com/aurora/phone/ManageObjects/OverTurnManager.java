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

public class OverTurnManager extends Handler {
	private static final String LOG_TAG = "OverTurnManager";

	public static boolean mIsUseSensorSwitch = true;
	private float mXData = 0;
	private float mYData = 0;
	private boolean mBoo = false;
	private boolean mBoo1 = false;
	private boolean mBoo2 = false;
	private boolean mBoo3 = false;
	private boolean mBoo4 = false;
	private boolean mBoo5 = false;
	private boolean mBoo6 = false;
	private int mCountUp = 0;
	private int mCountDown = 0;
	private boolean mIsTurnOver = false;
	private boolean mIsUpsideTurn = false;
	private boolean mIsDownsideTurn = false;
	private static SharedPreferences mSP = null;
	private boolean mGravitySensorFlg = false;
	private SensorManager mSensorMgr;
	private Sensor mGravitySensor;
	private Context mContext;

	private CallManager mCM;
	private static final int PHONE_STATE_CHANGED = 1;

	public OverTurnManager(CallManager cm, Context context) {
		mCM = cm;
		mContext = context;
		mCM.registerForPreciseCallStateChanged(this, PHONE_STATE_CHANGED,null);
		mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		mGravitySensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSP = mContext.getSharedPreferences("com.android.phone_preferences", Context.MODE_PRIVATE);
//		if (mIsUseSensorSwitch) {
//			mSensorMgr.registerListener(mSensorEventListener, mGravitySensor,
//					12000);
//			if (true == mIsTurnOver) {
//				muteIncomingCall(false);
//			}
//			clear();
//		}
	}

	public void handleMessage(Message msg) {
		switch (msg.what) {
		case PHONE_STATE_CHANGED:
			handleOverTurn();
			break;
		}
	}

	private final SensorEventListener mSensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

				boolean defOverturnMuteSwitch = mContext.getResources().getBoolean(
						R.bool.aurora_def_overturn_mute_switch);
				if (null != mSP
						&& mSP.getBoolean("aurora_overturn_mute_switch",
								defOverturnMuteSwitch)) {
					if (10 > event.values[SensorManager.DATA_Z]
							&& event.values[SensorManager.DATA_Z] > 8) {
						mCountUp++;
					} else {
						mCountUp = 0;
					}

					if (-12 < event.values[SensorManager.DATA_Z]
							&& event.values[SensorManager.DATA_Z] < -8) {
						mCountDown++;
					} else {
						mCountDown = 0;
					}
					if (mCountUp >= 5) {
						mIsUpsideTurn = true;
					}
					mXData = event.values[SensorManager.DATA_X];
					mYData = event.values[SensorManager.DATA_Y];

					if (mXData > -2 && mXData < 2) {
						mBoo1 = true;
					}

					if (true == mBoo1
							&& ((10 > mXData && mXData > 6) || (-10 < mXData && mXData < -6))) {
						mBoo2 = true;
					}

					if (true == mBoo2 && (mXData > -2 && mXData < 2)) {
						mBoo3 = true;
					}

					if (true == mBoo1 && true == mBoo2 && true == mBoo3) {
						mBoo = true;
					}

					if (mYData > -2 && mYData < 2) {
						mBoo4 = true;
					}

					if (true == mBoo4
							&& ((10 > mYData && mYData > 5) || (-10 < mYData && mYData < -5))) {
						mBoo5 = true;
					}

					if (true == mBoo5 && (mYData > -2 && mYData < 2)) {
						mBoo6 = true;
					}

					if (true == mBoo4 && true == mBoo5 && true == mBoo6) {
						mBoo = true;
					}
					if (mCountDown >= 5 && true == mIsUpsideTurn
							&& true == mBoo) {
						mIsDownsideTurn = true;
					}
					// Gionee fangbin 20130115 added for CR00763576 start
					// Log.i(LOG_TAG, "mCountUp: " + mCountUp +
					// " --mCountDown: " + mCountDown + " --mIsUpsideTurn: " +
					// mIsUpsideTurn +
					// " --mBoo: " + mBoo + " --mXData: " + mXData +
					// " --mYData: " + mYData + " --mIsDownsideTurn: " +
					// mIsDownsideTurn);
					// Gionee fangbin 20130115 added for CR00763576 end

					if (true == mIsDownsideTurn) {

						muteIncomingCall(true);
						mIsTurnOver = true;
					}
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	};

	private void handleOverTurn() {
		if (!mIsUseSensorSwitch) {
			return;
		}
		PhoneConstants.State state = mCM.getState();
		if (state == PhoneConstants.State.RINGING) {
			if (false == mGravitySensorFlg) {
				mSensorMgr.registerListener(mSensorEventListener,
						mGravitySensor, 12000);
				mGravitySensorFlg = true;
				if (true == mIsTurnOver) {
					muteIncomingCall(false);
				}
				mIsTurnOver = false;
			}
		} else {
			if (true == mGravitySensorFlg) {
				mSensorMgr.unregisterListener(mSensorEventListener);
				clear();
			}
		}
	}

	private void clear() {
		mIsUpsideTurn = false;
		mIsDownsideTurn = false;
		mCountUp = 0;
		mCountDown = 0;
		mBoo = false;
		mBoo1 = false;
		mBoo2 = false;
		mBoo3 = false;
		mBoo4 = false;
		mBoo5 = false;
		mBoo6 = false;
		mIsTurnOver = false;
		mGravitySensorFlg = false;
	}

	private void muteIncomingCall(boolean mute) {
		Log.i(LOG_TAG,"muteIncomingCall");
		CallNotifier notifier = PhoneGlobals.getInstance().notifier;
		notifier.silenceRinger();
	}
}