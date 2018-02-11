package com.aurora.timer;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.BroadcastReceiver.PendingResult;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.PowerManager.WakeLock;

import com.android.deskclock.AlarmClock;
import com.android.deskclock.R;

import android.util.Log;

public class TimerReceiver extends BroadcastReceiver {
	
	public static final String TAG = "TimerReceiver";
	public static final String COUNTDOWN_TIME = "countdownTime";
	public static final String ALARM_ALERT_ACTION = "com.android.intent.chronometer.ALARM.ALERT";
	private static final int ALARM_RINGTONE = 1;
	private Context mContext;
	private Handler handler = new Handler();
	
    @Override
    public void onReceive(final Context context, final Intent intent) {
    	Log.i(TAG, "onReceive");
    	mContext = context;
		ChronometerAlarmAlertWakeLock.acquireScreenOnLock(context,
				ChronometerAlarmAlertWakeLock.CHRONMENTER);
		Intent intent1 = new Intent(context,AlarmClock.class);	
		intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent1.putExtra("isFromTimerReceiver", true);
		context.startActivity(intent1);
//		startMusic();
//		showAlertDialog();
    }

    
	private void startMusic() {
		Log.v(TAG, "startService!");
		Intent playAlarm = new Intent(ALARM_ALERT_ACTION);
		mContext.startService(playAlarm);
		handler.removeCallbacks(mOneEndMusicRunnable);
		handler.postDelayed(mOneEndMusicRunnable, 3 * 60 * 1000);
		Log.v(TAG, "startService end!");
	}
	
    Runnable mOneEndMusicRunnable = new Runnable() {
		public void run() {
			mContext.stopService(new Intent(TimerFragment.ALARM_ALERT_ACTION));
		}
    };


	private void showAlertDialog() {
		// Close dialogs and window shade
		Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		mContext.sendBroadcast(closeDialogs);
	
		Class<?> clazz = ChronometerAlarmAlert.class;
		KeyguardManager km = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
		if (km.inKeyguardRestrictedInputMode()) {
			Log.v(TAG, "KeyguardManager.inKeyguardRestrictedInputMode(): "
					+ km.inKeyguardRestrictedInputMode());
			// Use the full screen activity for security.
			clazz = ChronometerAlertFullScreen.class;
		} else {
			Log.v(TAG, "KeyguardManager.inKeyguardRestrictedInputMode(): "
					+ km.inKeyguardRestrictedInputMode());
		}
	
		Intent intent = new Intent(mContext, clazz);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	//	if (isAdded()) {
		SharedPreferences sharedPreferences = mContext.getSharedPreferences("Chronometer",Activity.MODE_PRIVATE);
		long time = sharedPreferences.getLong("time", -1);
			intent.putExtra(COUNTDOWN_TIME, getTime(time));
			mContext.startActivity(intent);
	//	}
	
		TimerFragment.alertDismiss = false;
		saveData();
		Log.v(TAG, "show dialog end!");
	}
	
	private void saveData() {
		Log.v(TAG, "---saveData start!---");
		SharedPreferences sharedPreferences = mContext.getSharedPreferences("Chronometer",Activity.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putLong("time", 0);
		editor.putLong("timeLeft", 0);
		editor.putLong("stopTime", 0);
		editor.putBoolean("normalExit", true);
		editor.putInt("state", 3);
		editor.commit();

		Log.v(TAG, "---saveData end!---");
	}
	
	private String getTime(long time) {

		long second = (time / 1000) % 60;
		long minute = (time / 1000 / 60) % 60;
		long hour = time / 1000 / 60 / 60;

		String result = mContext.getResources().getString(
				R.string.chronometer_alart_title);
		result += hour <= 0 ? "" : hour
				+ mContext.getResources().getString(
						R.string.chronometer_alart_title_hour);
		result += minute <= 0 ? "" : minute
				+ mContext.getResources().getString(
						R.string.chronometer_alart_title_minute);
		result += second <= 0 ? "" : second
				+ mContext.getResources().getString(
						R.string.chronometer_alart_title_second);

		return result;
	}

}