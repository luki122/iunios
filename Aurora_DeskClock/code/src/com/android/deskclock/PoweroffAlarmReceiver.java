
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

        /* Close dialogs and window shade */
        Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeDialogs);

        // Play the alarm alert and vibrate the device.
        Intent playAlarm = new Intent(Alarms.ALARM_ALERT_ACTION);
        playAlarm.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        playAlarm.putExtra(POWER_OFF_FROM_ALARM, true);
        context.startService(playAlarm);

        // Trigger a notification that, when clicked, will show the alarm alert
        // dialog. No need to check for fullscreen since this will always be
        // launched from a user action.
        Intent notify = new Intent(context, AlarmAlert.class);
        notify.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        PendingIntent pendingNotify = PendingIntent.getActivity(context, alarm.id, notify, 0);

        // Use the alarm's label or the default label as the ticker text and
        // main text of the notification.
        String label = alarm.getLabelOrDefault(context);
        Notification n = new Notification(R.drawable.stat_notify_alarm, label, alarm.time);
        n.setLatestEventInfo(context, label, context.getString(R.string.alarm_notify_text),
                pendingNotify);
        n.flags |= Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_ONGOING_EVENT;
        n.defaults |= Notification.DEFAULT_LIGHTS;

        // NEW: Embed the full-screen UI here. The notification manager will
        // take care of displaying it if it's OK to do so.
        Intent alarmAlert = new Intent(context, AlarmAlertFullScreen.class);
        alarmAlert.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
		alarmAlert.putExtra(POWER_OFF_FROM_ALARM, true);
        alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        n.fullScreenIntent = PendingIntent.getActivity(context, alarm.id, alarmAlert, 0);
        
        // Send the notification using the alarm id to easily identify the
        // correct notification.
        NotificationManager nm = getNotificationManager(context);
        nm.notify(alarm.id, n);
    }

    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
