package com.android.deskclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.android.deskclock.R;
import aurora.preference.AuroraPreferenceManager;
public class BootAlarmReciver extends BroadcastReceiver {

	private static final String ACTION_KAYGUARD_SNOOZE = "com.aurora.keyguard.snooze.acton";
	private static final String ACTION_KAYGUARD_DISMISS = "com.aurora.keyguard.dissmiss.acton";
	private final static String EXTRA_ALARM_ID = "alarmid";
	private final static String EXTRA_ALARM_LABEL = "label";
	private final static String EXTRA_ALARM_TIME = "time";
	private static final String NORMAL_BOOT_ACTION = "android.intent.action.normal.boot";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.e("jadon4", "接受到广播 deskclock"+intent.getAction());
        if(intent.getAction().equals(ACTION_KAYGUARD_SNOOZE))
        {
        	int alarmId = intent.getIntExtra(EXTRA_ALARM_ID, -1);
            final String snooze =AuroraPreferenceManager.getDefaultSharedPreferences(context).getString(SettingsActivity.KEY_ALARM_SNOOZE, SettingsActivity.DEFAULT_SNOOZE);
            int snoozeMinutes = Integer.parseInt(snooze);

            // Gionee <baorui><2013-04-03> modify for CR00793143 begin
            /*
            final long snoozeTime = System.currentTimeMillis()
                    + (1000 * 60 * snoozeMinutes);
            */
            // Turn off the alarm clock, we don't want to in 59 seconds time to an alarm clock, so easy to fall
            // into a dead loop
            long snoozeTime = -1;
                long mTempTime = (System.currentTimeMillis() % 2 == 0) ? System.currentTimeMillis() : (System
                        .currentTimeMillis() + 1000);
                snoozeTime = mTempTime + (1000 * 60 * snoozeMinutes);
            // Gionee <baorui><2013-04-03> modify for CR00793143 end
            Alarms.saveSnoozeAlert(context, alarmId,snoozeTime);
            String displayTime = context.getString(R.string.alarm_alert_snooze_set,snoozeMinutes);
            Toast.makeText(context, displayTime,Toast.LENGTH_LONG).show();
            context.stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
            shutDown(context);
        }else if(intent.getAction().equals(ACTION_KAYGUARD_DISMISS))
        {
        	context.stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        	context.sendBroadcast(new Intent(NORMAL_BOOT_ACTION));
        }
	}
	private static final String ALARM_REQUEST_SHUTDOWN_ACTION = "android.intent.action.ACTION_ALARM_REQUEST_SHUTDOWN";
	private static final String DISABLE_POWER_KEY_ACTION = "android.intent.action.DISABLE_POWER_KEY";
	private static final String NORMAL_SHUTDOWN_ACTION = "android.intent.action.normal.shutdown";
	// shut down the device
	private void shutDown(Context context) {
		// send normal shutdown broadcast
//		Intent shutdownIntent = new Intent(NORMAL_SHUTDOWN_ACTION);
//		context.sendBroadcast(shutdownIntent);
		
		Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
        intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
//		enablePowerKey(context);
//		// shutdown the device
//		Intent intent = new Intent(ALARM_REQUEST_SHUTDOWN_ACTION);
//		intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		context.startActivity(intent);
	}
	private void enablePowerKey(Context context) {
		Intent enablePowerKeyIntent = new Intent(DISABLE_POWER_KEY_ACTION);
		enablePowerKeyIntent.putExtra("state", true);
		context.sendBroadcast(enablePowerKeyIntent);
	}
	
	

}
