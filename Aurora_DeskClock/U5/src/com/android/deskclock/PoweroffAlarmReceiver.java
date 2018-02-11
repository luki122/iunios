
package com.android.deskclock;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager.WakeLock;

import com.android.deskclock.R;

/**
 * This broadcast receiver intents to receive power off alarm alert broadcast
 * sent by AlarmManagerService.
 */
public class PoweroffAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "PoweroffAlarmReceiver";

    /*
     * Device boot time window, 3 mins by default, if the current time is more
     * than the time window later after the alarm time, ingore this broadcast.
     */
    private static final int BOOT_TIME_WINDOW = 60 * 3;
	private final static String POWER_OFF_FROM_ALARM = "isPoweroffAlarm";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final PendingResult result = goAsync();
        final WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
        wl.acquire();
        AsyncHandler.post(new Runnable() {
            @Override public void run() {
                handleIntent(context, intent);
                result.finish();
                wl.release();
            }
        });
    }

    private void handleIntent(final Context context, final Intent intent) {
        String pkgName = intent.getStringExtra("packageName");
        if (pkgName == null || !pkgName.equals("com.android.deskclock")){//context.getPackageName())) {
            Log.i("This power off broadcast is not for desk clock, it is for package:" + pkgName);
            return;
        }

        Alarm alarm = Alarms.getNearestAlarm(context);
        long alarmtime = Alarms.getNearestAlarmTime(context);

        if (alarm == null) {
            Log.i("PoweroffAlarmReceiver failed to get the nearest alarm from preference.");
            // Make sure we set the next alert if needed.
            Alarms.setNextAlert(context);
            return;
        }

        // Disable the snooze alert if this alarm is the snooze.
        Alarms.disableSnoozeAlert(context, alarm.id);
        // Disable this alarm if it does not repeat.
        if (alarm.daysOfWeek.isRepeatSet()) {
            // Enable the next alert if there is one. The above call to
            // enableAlarm will call setNextAlert so avoid calling it twice.
            Alarms.setNextAlert(context);
        } else {
            Alarms.enableAlarm(context, alarm.id, false);
        }

        final long now = System.currentTimeMillis();

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS aaa");
        Log.i("PoweroffAlarmReceiver.onReceive: alarm id " + alarm.id + ",alarm time = "
                + alarmtime + ",setFor " + format.format(new Date(alarm.time)));

        // If the current time is 3mins later since the alarm should alert,
        // discard it.
        if (now > alarmtime + BOOT_TIME_WINDOW * 1000) {
            Log.i("PoweroffAlarmReceiver ignoring stale alarm.");
            return;
        }

        // Maintain a cpu wake lock until the AlarmAlert and AlarmKlaxon can
        // pick it up.
        AlarmAlertWakeLock.acquireCpuWakeLock(context);

        // Play the alarm alert and vibrate the device.
        Intent playAlarm = new Intent(Alarms.ALARM_ALERT_ACTION);
        playAlarm.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        playAlarm.putExtra(POWER_OFF_FROM_ALARM, true);
        context.startService(playAlarm);

        sendDataToKeyguard(alarm,context);
    }
    private final static String EXTRA_ALARM_ID = "alarmid";
	private final static String EXTRA_ALARM_LABEL = "label";
	private final static String EXTRA_ALARM_TIME = "time";
	private final static String ACTION_ALARM_DATA = "com.aurora.boot.alarm.label.action";

    private void sendDataToKeyguard(Alarm alarm,Context context){
    	Intent intent = new Intent(ACTION_ALARM_DATA);
    	intent.putExtra(EXTRA_ALARM_ID, alarm.id);
    	intent.putExtra(EXTRA_ALARM_LABEL, alarm.label);
    	intent.putExtra(EXTRA_ALARM_TIME, alarm.time);
        context.sendBroadcast(intent);
        
    }

    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
