package com.android.deskclock;

import com.aurora.timer.ChronometerAlarmAlert;
import com.aurora.timer.ChronometerAlarmAlertWakeLock;
import com.aurora.timer.ChronometerAlertFullScreen;
import com.aurora.timer.TimerFragment;
import com.aurora.utils.NotificationOperate;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

public class CTSHandleApiReceiver extends BroadcastReceiver {

	public static final String GST_TIMER_ACTION = "com.aurora.gst.timer.action"; 
	public static final String ALERT_URI = "alert_uri";
	
	public static Uri  alertUri = null;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getAction().equals(GST_TIMER_ACTION))
		{
		// TODO Auto-generated method stub
			Class<?> clazz = ChronometerAlarmAlert.class;
			KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
			if (km.inKeyguardRestrictedInputMode()) {
				clazz = ChronometerAlertFullScreen.class;
			} else {
			}
		    android.util.Log.e("CTS", "TimerFragment.SHOW_TITLE = "+intent.getStringExtra(TimerFragment.SHOW_TITLE));
			Intent intent2 = new Intent(context, clazz);
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			SharedPreferences sharedPreferences = context.getSharedPreferences("Chronometer",Activity.MODE_PRIVATE);
			intent2.putExtra("countdownTime", getTime(30*1000,context));
			intent2.putExtra(TimerFragment.SHOW_TITLE, intent.getStringExtra(TimerFragment.SHOW_TITLE));
			context.startActivity(intent2);
			ChronometerAlarmAlertWakeLock.acquireScreenOnLock(context,
					ChronometerAlarmAlertWakeLock.CHRONMENTER);
			 Intent playAlarm = new Intent("com.android.intent.chronometer.ALARM.ALERT");
			 context.startService(playAlarm);
			 NotificationOperate.cancelNotification(context, 101);
		}else if(intent.getAction().equals(Alarms.ALARM_ALERT_ACTION))
		{
				if(alertUri != null)
				{
					ContentResolver cs = context.getContentResolver();
					cs.delete(alertUri, null, null);
					alertUri = null;
					
			    }
			
			
		}
	}

	private String getTime(long time,Context context) {

		long second = (time / 1000) % 60;
		long minute = (time / 1000 / 60) % 60;
		long hour = time / 1000 / 60 / 60;

		String result = context.getResources().getString(
				R.string.chronometer_alart_title);
		result += hour <= 0 ? "" : hour
				+ context.getResources().getString(
						R.string.chronometer_alart_title_hour);
		result += minute <= 0 ? "" : minute
				+ context.getResources().getString(
						R.string.chronometer_alart_title_minute);
		result += second <= 0 ? "" : second
				+ context.getResources().getString(
						R.string.chronometer_alart_title_second);

		return result;
	}
	
	
}
