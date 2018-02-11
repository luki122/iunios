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

package com.android.deskclock;

//import com.android.stopwatch.StopWatchActivity;

import aurora.app.AuroraActivity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.content.BroadcastReceiver.PendingResult;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.widget.Toast;

public class AlarmInitReceiver extends BroadcastReceiver {
	private static final String IPO_BOOT_ACTION = "android.intent.action.ACTION_BOOT_IPO";
	private static boolean mBlockTimeChange = false;
	private Handler handler = new Handler();


	/**
	 * Sets alarm on ACTION_BOOT_COMPLETED. Resets alarm on TIME_SET,
	 * TIMEZONE_CHANGED
	 */
	@Override
	public void onReceive(final Context context, Intent intent) {

		final String action = intent.getAction();



		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			SharedPreferences sharedPreferences = context.getSharedPreferences(
					"somesettingstate", Context.MODE_PRIVATE);

			AudioManager mAudioManager = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);

			int volum = sharedPreferences.getInt("systemvolume", 7);
			if (volum != mAudioManager
					.getStreamVolume(AudioManager.STREAM_ALARM)) {
				mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, volum,
						0);
			}

			Context systemUI = null;
			try {
				systemUI = context.createPackageContext("com.android.systemui",
						Context.CONTEXT_IGNORE_SECURITY);
			} catch (NameNotFoundException e) {
				// TODO: handle exception
				android.util.Log.d("cjslog", "com.android.systemui not found");
			}
			SharedPreferences sharedPreferences2 = systemUI
					.getSharedPreferences("alarmboot",
							Context.MODE_WORLD_READABLE
									+ Context.MODE_WORLD_WRITEABLE);

			boolean poweroffalarm = sharedPreferences2.getBoolean(
					"bootfromalarm", false);
			if (poweroffalarm) {
				android.util.Log.d("cjslog", "bootFromPoweroffAlarm");
				// SharedPreferences sharedPreferences =
				// context.getSharedPreferences("somesettingstate",
				// Context.MODE_PRIVATE);
				int nextAlarmId = sharedPreferences
						.getInt("shutdownalarmid", 0);

				final Alarm nextAlarm = Alarms.getAlarm(
						context.getContentResolver(), nextAlarmId);

				final PendingResult result = goAsync();
				final WakeLock wl = AlarmAlertWakeLock
						.createPartialWakeLock(context);

				wl.acquire();

				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						// Disable the snooze alert if this alarm is the snooze.
						Alarms.disableSnoozeAlert(context, nextAlarm.id);
						// Disable this alarm if it does not repeat.
						if (!nextAlarm.daysOfWeek.isRepeatSet()) {
							Alarms.enableAlarm(context, nextAlarm.id, false);
						} else {
							// Enable the next alert if there is one. The above
							// call to
							// enableAlarm will call setNextAlert so avoid
							// calling it twice.
							Alarms.setNextAlert(context);
						}

						// Maintain a cpu wake lock until the AlarmAlert and
						// AlarmKlaxon can
						// pick it up.
						AlarmAlertWakeLock.acquireCpuWakeLock(context);

						/* Close dialogs and window shade */
						Intent closeDialogs = new Intent(
								Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
						context.sendBroadcast(closeDialogs);
						Class c = AlarmAlert.class;
						KeyguardManager km = (KeyguardManager) context
								.getSystemService(Context.KEYGUARD_SERVICE);
						if (km.inKeyguardRestrictedInputMode()) {
							// Use the full screen activity for security.
							android.util.Log.d("cjslog", "1111");
							c = AlarmAlertFullScreen.class;
						}

						Intent playAlarm = new Intent(Alarms.ALARM_ALERT_ACTION);
						playAlarm.setPackage(context.getPackageName());
						playAlarm
								.putExtra(Alarms.ALARM_INTENT_EXTRA, nextAlarm);
						// playAlarm.putExtra(POWER_OFF_FROM_ALARM, true);
						context.startService(playAlarm);

						Alarms.mIfDismiss = true;
						Alarms.mAlarmId = nextAlarm.id;
						// Trigger a notification that, when clicked, will show
						// the alarm alert
						// dialog. No need to check for fullscreen since this
						// will always be
						// launched from a user action.
						Intent notify = new Intent(context, c);// AlarmAlert.class);
						notify.putExtra(Alarms.ALARM_INTENT_EXTRA, nextAlarm);

						Log.e("111----c = " + c.toString());

						// lable rename jiating 201206151132
						PendingIntent pendingNotify = PendingIntent
								.getActivity(context, nextAlarm.id, notify,
										PendingIntent.FLAG_UPDATE_CURRENT);

						// Use the alarm's label or the default label as the
						// ticker text and
						// main text of the notification.
						String label = nextAlarm.getLabelOrDefault(context);
						Notification n = new Notification(
								R.drawable.stat_notify_alarm_white, label,
								nextAlarm.time);

						n.setLatestEventInfo(context, label,
								context.getString(R.string.alarm_notify_text),
								pendingNotify);
						n.flags |= Notification.FLAG_SHOW_LIGHTS
								| Notification.FLAG_ONGOING_EVENT;
						n.defaults |= Notification.DEFAULT_LIGHTS;

						// NEW: Embed the full-screen UI here. The notification
						// manager will
						// take care of displaying it if it's OK to do so.
						Intent alarmAlert = new Intent(context, c);
						alarmAlert.putExtra(Alarms.ALARM_INTENT_EXTRA,
								nextAlarm);
						alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						alarmAlert.setPackage(context.getPackageName()); // cjs
																			// add
						context.startActivity(alarmAlert);
						// lable rename jiating 201206151132
						n.fullScreenIntent = PendingIntent.getActivity(context,
								nextAlarm.id, alarmAlert,
								PendingIntent.FLAG_UPDATE_CURRENT);

						// Send the notification using the alarm id to easily
						// identify the
						// correct notification.
						NotificationManager nm = (NotificationManager) context
								.getSystemService(Context.NOTIFICATION_SERVICE);
						nm.cancel(nextAlarm.id);
						nm.notify(nextAlarm.id, n);
						
				    	int isAirplaneMode = Settings.Global.getInt(context.getContentResolver(),  
				                Settings.Global.AIRPLANE_MODE_ON, 0);
				    	//Log.e("------isAirplaneMode = --------------------" + isAirplaneMode);
				    	//开启了飞行模式就关闭
				    	if ( isAirplaneMode ==  1 ) {
				    		Toast.makeText(context, context.getResources().getString(R.string.airplanemodeoff), Toast.LENGTH_SHORT).show();
					        Settings.Global.putInt(context.getContentResolver(),  
					                             Settings.Global.AIRPLANE_MODE_ON, 0);  
					        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);  
					        intent.putExtra("state", 0);  
					        context.sendBroadcast(intent);  
				    	}

						result.finish();
						wl.release();

						context.sendBroadcast(new Intent(
								"com.android.deskclock.ALARM_DELETE_BOOTFLAG"));
					}
				}, 1000); // delay 1000ms waiting for register

			}
		}

		/*
		 * if ("android.intent.action.SERVICE_STATE".equals(intent.getAction())
		 * && Alarms.bootFromPoweroffAlarm()) { Editor editor =
		 * sharedPreferences.edit(); editor.putBoolean("bootfrompoweroffalarm",
		 * true); editor.apply();
		 * context.getApplicationContext().sendBroadcast(new
		 * Intent("android.intent.action.normal.boot")); }
		 */
		
		if (action.equals(Intent.ACTION_SHUTDOWN)) {
			AudioManager mAudioManager = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);

			SharedPreferences sharedPreferences = context.getSharedPreferences(
					"somesettingstate", Context.MODE_PRIVATE);
			Editor editor = sharedPreferences.edit();
			editor.putInt("systemvolume",
					mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM));
			editor.apply();
			
	        if (Alarms.mIfDismiss == true && Alarms.mAlarmId != -1 ) {
	        	android.util.Log.d("cjslog", "stopService");
	            context.sendBroadcast(new Intent("com.android.deskclock.stoptalarmring"));
	        }
		}

		if (AlarmClock.mIsProcessExist
				&& !(Intent.ACTION_LOCALE_CHANGED.equals(action)
						|| Intent.ACTION_TIME_CHANGED.equals(action) || Intent.ACTION_TIMEZONE_CHANGED
							.equals(action))) {
			return;
		}

		Log.v("AlarmInitReceiver: action = " + action + ",mBlockTimeChange = "
				+ mBlockTimeChange);
		// Gionee <jiating><2013-08-15> modify for CR00852446 begin
		if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
			SharedPreferences sharedPreferencesStop = context
					.getSharedPreferences("stopWatchStateData",
							AuroraActivity.MODE_PRIVATE);
			Editor editor = sharedPreferencesStop.edit();
			// editor.putBoolean(StopWatchActivity.iS_LOCALE_CHANED, true);
			editor.commit();
			SharedPreferences sharedPreferencesChronometer = context
					.getSharedPreferences("Chronometer",
							AuroraActivity.MODE_PRIVATE);
			Editor editorChronometer = sharedPreferencesChronometer.edit();
			// editorChronometer.putBoolean(StopWatchActivity.iS_LOCALE_CHANED,
			// true);
			editorChronometer.commit();
		}
		// Gionee <jiating><2013-08-15> modify for CR00852446 end
		/*
		 * Note: Never call setNextAlert when the device is boot from power off
		 * alarm, since it would make the power off alarm dismiss the wrong
		 * alarm.
		 */
		if (IPO_BOOT_ACTION.equals(action)) {
			Log.v("Receive android.intent.action.ACTION_BOOT_IPO intent.");
			mBlockTimeChange = true;
			return;
		}

		if (mBlockTimeChange && Intent.ACTION_TIME_CHANGED.equals(action)) {
			Log.v("Ignore time change broadcast because it is sent from ipo.");
			return;
		}

		final PendingResult result = goAsync();
		AsyncHandler.post(new Runnable() {
			@Override
			public void run() {
				WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
				android.util.Log.e("333333", "--444--wl.acquire()-----");
				wl.acquire();
				// Remove the snooze alarm after a boot.
				if (action.equals(Intent.ACTION_BOOT_COMPLETED)
						|| !AlarmClock.mIsProcessExist) {
					android.util.Log.e("jadon3", "1111111111111111");
					mBlockTimeChange = false;
					if (Alarms.bootFromPoweroffAlarm()) {
						/*
						 * If the boot complete is from power off alarm, do not
						 * call setNextAlert and disableExpiredAlarms, because
						 * it will change the nearest alarm in the preference,
						 * and the power off alarm receiver may get the wrong
						 * alarm.
						 */
						Log.v("AlarmInitReceiver recieves boot complete because power off alarm.");
						Alarms.disableAllSnoozedAlarms(context);
						Alarms.disableExpiredAlarms(context);
					} else {
						Alarms.saveSnoozeAlert(context,
								Alarms.INVALID_ALARM_ID, -1);
						Alarms.disableExpiredAlarms(context);
						// Alarms.setNextAlert(context);
					}
				} else {
					android.util.Log.e("jadon3", "22222222222222222222222");
					Alarms.disableExpiredAlarms(context);
					/*
					 * If time changes, we need to reset the time column of
					 * alarms in database.
					 */
					if (Intent.ACTION_TIME_CHANGED.equals(action)
							|| Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
						Alarms.resetAlarmTimes(context);
					}
					if (!Alarms.bootFromPoweroffAlarm()) {
						Alarms.setNextAlert(context);
					}
				}
				result.finish();
				Log.v("AlarmInitReceiver finished");
				wl.release();
				android.util.Log.e("333333", "--555--wl.release()-----");
			}
		});

		AlarmClock.mIsProcessExist = true;

	}
}
