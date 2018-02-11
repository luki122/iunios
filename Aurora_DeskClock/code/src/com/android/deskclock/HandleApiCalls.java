package com.android.deskclock;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import aurora.app.AuroraActivity;

import com.android.deskclock.Alarm.DaysOfWeek;
import com.aurora.timer.ChronometerAlarmAlert;
import com.aurora.timer.ChronometerAlarmAlertWakeLock;
import com.aurora.timer.ChronometerAlertFullScreen;
import com.aurora.timer.TimerFragment;
import com.aurora.timer.TimerReceiver;
import com.aurora.utils.NotificationOperate;

public class HandleApiCalls extends AuroraActivity {

	private Intent intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		intent = getIntent();
		handle();
		finish();
	}
	
	private void handle(){
		String action = getIntent().getAction();
		boolean showUI = getIntent().hasExtra(android.provider.AlarmClock.EXTRA_SKIP_UI)&&getIntent().getBooleanExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, false) ? false:true;
		if(action.equals(AlarmClock.ACTION_SET_ALARM))
		{
			
			setAlarm(showUI);
		}else if(action.equals(AlarmClock.ACTION_SET_TIMER)){
			
			setTimer(showUI);
		}else if(action.equals(AlarmClock.ACTION_SHOW_ALARMS)){
			showAlarm(showUI);
		}
		
	}
	
	private void setTimer(boolean showUi){
		if(showUi)
		{
			int length = intent.getIntExtra(AlarmClock.EXTRA_LENGTH, 0);
			Intent intent = new Intent(this, com.android.deskclock.AlarmClock.class);
			intent.putExtra("tabNum", 3);
			intent.putExtra(AlarmClock.EXTRA_LENGTH, length);
			intent.putExtra(AlarmClock.EXTRA_MESSAGE, this.intent.getStringExtra(AlarmClock.EXTRA_MESSAGE));
			startActivity(intent);
			finish();
		}else{
			NotificationOperate.createNotifaction(this, 101, intent.getStringExtra(AlarmClock.EXTRA_MESSAGE),
					"timer");
			showAlertDialog(this.intent.getStringExtra(AlarmClock.EXTRA_MESSAGE));
		}
	}
	
	private void showAlertDialog(String title) {
		long time = 30*1000;
		Intent bi = new Intent(CTSHandleApiReceiver.GST_TIMER_ACTION);
		bi.putExtra(TimerFragment.SHOW_TITLE, title);
		PendingIntent sender = PendingIntent.getBroadcast(this, 0, bi,
		PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 30*1000, sender);
	}
	private void showAlarm(boolean showUi){
		Intent intent = new Intent(this, com.android.deskclock.AlarmClock.class);
		intent.putExtra("tabNum", 0);
		startActivity(intent);
	}
	
	private void setAlarm(boolean showUi){
		ArrayList<Integer> days = getIntent().getIntegerArrayListExtra(AlarmClock.EXTRA_DAYS);
		int day = days!=null ? 5 : 0;
		if(showUi)
		{
			Intent intent1 = null;
			if(intent.getStringExtra(AlarmClock.EXTRA_RINGTONE)!=null)
			{
				Alarm alarm = new Alarm();
				alarm.alert = Uri.parse(intent.getStringExtra(AlarmClock.EXTRA_RINGTONE));
				DaysOfWeek daysOfWeek = new DaysOfWeek(5);
				alarm.daysOfWeek = daysOfWeek;
				alarm.enabled = true;
				alarm.vibrate = true;
				alarm.hour = intent.getIntExtra(AlarmClock.EXTRA_HOUR, 0);
				alarm.minutes = intent.getIntExtra(AlarmClock.EXTRA_MINUTES, 0);
				alarm.label = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE);
				Alarms.addAlarm(this, alarm);
				intent1 = new Intent(this,com.android.deskclock.AlarmClock.class);
				intent1.putExtra("tabNum", 0);
			}else{
				intent1 = new Intent(this,AuroraSetAlarm.class);
			}
			startActivity(intent1);
			finish();
		    return;			
		}
		final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        final int hour = intent.getIntExtra(AlarmClock.EXTRA_HOUR,
                calendar.get(Calendar.HOUR_OF_DAY));
        final int minutes = intent.getIntExtra(AlarmClock.EXTRA_MINUTES,
                calendar.get(Calendar.MINUTE));
        final boolean skipUi = intent.getBooleanExtra(AlarmClock.EXTRA_SKIP_UI, false);
        String message = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE);
        if (message == null) {
            message = "";
        }
        long timeInMillis = Alarms.calculateAlarm(hour, minutes,
                new Alarm.DaysOfWeek(0)).getTimeInMillis();
        ContentValues values = new ContentValues();
        values.put(Alarm.Columns.HOUR, hour);
        values.put(Alarm.Columns.MINUTES, minutes);
        values.put(Alarm.Columns.MESSAGE, message);
        values.put(Alarm.Columns.ENABLED, 1);
        values.put(Alarm.Columns.VIBRATE, 1);
        values.put(Alarm.Columns.DAYS_OF_WEEK, 0);
        values.put(Alarm.Columns.ALARM_TIME, timeInMillis);
        values.put(Alarm.Columns.ALERT, com.android.deskclock.AlarmClock.VALUE_RINGTONE_SILENT);
        Cursor c = null;
        ContentResolver cr = getContentResolver();
        Uri result = cr.insert(Alarm.Columns.CONTENT_URI, values);
        CTSHandleApiReceiver.alertUri = result;
        if (result != null) {
            try {
                c = cr.query(result, Alarm.Columns.ALARM_QUERY_COLUMNS, null,
                        null, null);
                handleCursorResult(c, timeInMillis, true, skipUi);
            } finally {
                if (c != null) c.close();
            }
        }
        
	}
	
	
	private boolean handleCursorResult(Cursor c, long timeInMillis,
            boolean enable, boolean skipUi) {
        if (c != null && c.moveToFirst()) {
            Alarm alarm = new Alarm(c);
            if (enable) {
                Alarms.enableAlarm(this, alarm.id, true);
            }
            AuroraSetAlarm.popAlarmSetToast(this, timeInMillis);
            if (!skipUi) {
                Intent i = new Intent(this, AuroraSetAlarm.class);
                i.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
                startActivity(i);
            }
            return true;
        }
        return false;
    }
}
