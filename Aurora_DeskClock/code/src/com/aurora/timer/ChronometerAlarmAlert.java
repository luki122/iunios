/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.aurora.timer;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.android.deskclock.R;
//Gionee baorui 2012-12-26 modify for CR00753181 begin
import android.view.Window;
//Gionee baorui 2012-12-26 modify for CR00753181 end

/**
 * The reference of alarm clock to achieve
 * Full screen Chronometer alert: pops visible indicator and plays alarm tone. This
 * activity shows the alert as a dialog.
 */
public class ChronometerAlarmAlert extends ChronometerAlertFullScreen {

	// If we try to check the keyguard more than 5 times, just launch the full
	// screen activity.
	private int mKeyguardRetryCount;
	private final int MAX_KEYGUARD_CHECKS = 5;
	private String countdownTimeAlert;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			handleScreenOff((KeyguardManager) msg.obj);
		}
	};

	private final BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			KeyguardManager km = (KeyguardManager) context
					.getSystemService(Context.KEYGUARD_SERVICE);
			handleScreenOff(km);
		}
	};

	@Override
	protected void onCreate(Bundle icicle) {
        // Gionee baorui 2012-12-26 modify for CR00753181 begin
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Gionee baorui 2012-12-26 modify for CR00753181 end
		super.onCreate(icicle);

//		String tempTime = getIntent().getStringExtra(
//				ChronometerActivity.COUNTDOWN_TIME);
//		if (!"计时".equals(tempTime) && tempTime != null) {
//			countdownTimeAlert = tempTime;
//		}
		// countdownTime =
		// getIntent().getStringExtra(ChronometerActivity.COUNTDOWN_TIME);
		// Listen for the screen turning off so that when the screen comes back
		// on, the user does not need to unlock the phone to dismiss the alarm.
		registerReceiver(mScreenOffReceiver, new IntentFilter(
				Intent.ACTION_SCREEN_OFF));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mScreenOffReceiver);
		// Remove any of the keyguard messages just in case
		mHandler.removeMessages(0);
	}

	@Override
	public void onBackPressed() {
		dismiss(false);
		finish();
	}

//	@Override
//	protected int getLayoutResId() {
//	
//			return R.layout.chronometer_alarm_alert;
//		
//		
//	}

	private boolean checkRetryCount() {
		if (mKeyguardRetryCount++ >= MAX_KEYGUARD_CHECKS) {
			return false;
		}
		return true;
	}

	private void handleScreenOff(final KeyguardManager km) {
		if (!km.inKeyguardRestrictedInputMode() && checkRetryCount()) {
			if (checkRetryCount()) {
				mHandler.sendMessageDelayed(mHandler.obtainMessage(0, km), 500);
			}
		} else {
			// Launch the full screen activity but do not turn the screen on.
			Intent i = new Intent(this, ChronometerAlertFullScreen.class);
//			i.putExtra(ChronometerActivity.COUNTDOWN_TIME, countdownTimeAlert);
			i.putExtra(SCREEN_OFF, true);
			startActivity(i);
			finish();
		}
	}
}
