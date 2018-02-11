/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.deskclock;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
//Gionee <baorui><2013-05-27> modify for CR00798633 begin
//import com.mediatek.featureoption.FeatureOption;
import com.aurora.utils.FeatureOption;
//Gionee <baorui><2013-05-27> modify for CR00798633 end

/**
 * Manages alarms and vibe. Runs as a service so that it can continue to play if
 * another activity overrides the AlarmAlert dialog.
 */
public class AlarmPhoneListenerService extends Service {

	private static final int GIMINI_SIM_1 = 0;
	private static final int GIMINI_SIM_2 = 1;
	private static final int DELAY_START_ALARM = 800;

	private TelephonyManager mTelephonyManager;
	//private ITelephony mTelephonyService;
	private int mCurrentCallState;
	private int mCurrentCallState1;
	private int mCurrentCallState2;
	private Alarm mAlarm;
	private Handler mHandler = new Handler();

	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String ignored) {
			if (state == TelephonyManager.CALL_STATE_IDLE && state != mCurrentCallState) {
				Log.v("state == TelephonyManager.CALL_STATE_IDLE && state != mCurrentCallState");
				mHandler.postDelayed(new Runnable() {
					public void run() {
						sendStartAlarmBroadcast();
					}
				}, DELAY_START_ALARM);
			}
		}
	};
	
	private PhoneStateListener mPhoneStateListener1 = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String ignored) {
			if(mCurrentCallState1 != TelephonyManager.CALL_STATE_IDLE){
				if (state == TelephonyManager.CALL_STATE_IDLE && state != mCurrentCallState1) {
					Log.v("state == TelephonyManager.CALL_STATE_IDLE && state != mCurrentCallState1");
					mHandler.postDelayed(new Runnable() {
						public void run() {
							sendStartAlarmBroadcast();
						}
					}, DELAY_START_ALARM);
				}
			}
		}
	};
	
	private PhoneStateListener mPhoneStateListener2 = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String ignored) {
			if(mCurrentCallState2 != TelephonyManager.CALL_STATE_IDLE){
				if (state == TelephonyManager.CALL_STATE_IDLE && state != mCurrentCallState2) {
					Log.v("state == TelephonyManager.CALL_STATE_IDLE && state != mCurrentCallState2");
					mHandler.postDelayed(new Runnable() {
						public void run() {
							sendStartAlarmBroadcast();
						}
					}, DELAY_START_ALARM);
				}
			}
		}
	};

	@Override
	public void onCreate() {
		// Listen for incoming calls to kill the alarm.
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		
		//aurora mod by tangjun 2013.12.25 start
		/*
		mTelephonyService = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));
		// Check if the device is gemini supported
		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			mTelephonyManager.listenGemini(mPhoneStateListener1,
					PhoneStateListener.LISTEN_CALL_STATE
							| PhoneStateListener.LISTEN_SERVICE_STATE,
					GIMINI_SIM_1);
			mTelephonyManager.listenGemini(mPhoneStateListener2,
					PhoneStateListener.LISTEN_CALL_STATE
							| PhoneStateListener.LISTEN_SERVICE_STATE,
					GIMINI_SIM_2);
		} else {
			mTelephonyManager.listen(mPhoneStateListener,
					PhoneStateListener.LISTEN_CALL_STATE);
		}
		*/
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		//aurora mod by tangjun 2013.12.25 end
	}

	@Override
	public void onDestroy() {
		// Stop listening for incoming calls.
		
		//aurora mod by tangjun 2013.12.25 start
		/*
		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			mTelephonyManager
					.listenGemini(mPhoneStateListener1, 0, GIMINI_SIM_1);
			mTelephonyManager
					.listenGemini(mPhoneStateListener2, 0, GIMINI_SIM_2);
		} else {
			mTelephonyManager.listen(mPhoneStateListener, 0);
		}
		*/
		mTelephonyManager.listen(mPhoneStateListener, 0);
		//aurora mod by tangjun 2013.12.25 end
		 
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mAlarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
		if (mAlarm == null) {
            Log.v("AlarmKlaxon failed to parse the alarm from the intent");
            stopSelf();
            return START_NOT_STICKY;
        }
		
		//aurora mod by tangjun 2013.12.25 start
		/*
		try {
			mCurrentCallState = mTelephonyService.getPreciseCallState();
			mCurrentCallState1 = mTelephonyManager.getCallStateGemini(GIMINI_SIM_1);
			mCurrentCallState2 = mTelephonyManager.getCallStateGemini(GIMINI_SIM_2);
		} catch (RemoteException ex) {
			Log.v("Catch exception when getPreciseCallState: ex = "
					+ ex.getMessage());
		}
		*/
		mCurrentCallState = mTelephonyManager.getCallState();
		//aurora mod by tangjun 2013.12.25 end
		
		return START_REDELIVER_INTENT;
	}

	private void sendStartAlarmBroadcast() {
		Intent startAlarm = new Intent(Alarms.ALARM_ALERT_ACTION);
		startAlarm.putExtra("setNextAlert", false);
		startAlarm.putExtra(Alarms.ALARM_INTENT_EXTRA, mAlarm);
		sendBroadcast(startAlarm);
		Log.v("AlarmPhoneListenerService sendStartAlarmBroadcast");
		stopSelf();
	}

}
