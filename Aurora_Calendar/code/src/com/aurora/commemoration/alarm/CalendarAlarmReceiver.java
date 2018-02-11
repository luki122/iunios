package com.aurora.commemoration.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.android.calendar.R;
import com.android.calendar.wxapi.WXEntryActivity;
import com.aurora.calendar.util.SystemUtils;
import com.aurora.calendar.util.TimeUtils;
import com.aurora.commemoration.db.RememberDayDBHelper;
import com.aurora.commemoration.db.RememberDayDao;
import com.aurora.commemoration.model.RememberDayInfo;

// import android.net.Uri;


public class CalendarAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "CalendarAlarmReceiver";



    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (CalendarAlarmManager.ACTION_CALENDAR_ALARM.equals(action)) {
            Bundle bundle = intent.getExtras();
            updateNotification(context, bundle.getInt(RememberDayDBHelper.ID, 0));
        } else if (CalendarAlarmManager.ACTION_CALENDAR_ALARM_CANCEL.equals(action)) {
            Bundle bundle = intent.getExtras();
            cancelNotification(context, bundle.getInt(RememberDayDBHelper.ID, 0));
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            scheduleAlarms();
        } else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
            scheduleAlarms();
        } 
    }

    
    
    
    private void updateNotification(Context context, int noteId) {
        RememberDayDao noteDb = new RememberDayDao(context);
       
        RememberDayInfo note = noteDb.getItemById(noteId);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (note == null || note.getReminderData() == 0L) {
            nm.cancel(noteId);
            return;
        }


        String contentText = note.getDay();
        Long time = TimeUtils.getLongFromStrTime1(contentText);
        contentText = TimeUtils.formatTimestamp1(time, SystemUtils.isChineseEnvironment());
        Bundle bundle = new Bundle();
        bundle.putBoolean("new", false);
        bundle.putInt("index", -1);
        bundle.putLong("id", note.getMillTime());
        bundle.putInt("isNotifi", 1);
/*        Notification.Builder notificationBuilder = new Notification.Builder(context);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentIntent(pendingClickIntent);
        notificationBuilder.setContentTitle(note.getTitle());
        notificationBuilder.setContentText(contentText);
        notificationBuilder.setSmallIcon(R.drawable.notifi_warn);
        notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        notificationBuilder.setWhen(note.getReminderData());*/

        Notification notification = new Notification();
        notification.icon = R.drawable.notifi_warn;
        notification.contentView = new RemoteViews(
        		context.getPackageName(), R.layout.notification_installed);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_SOUND;
        // notification.sound = Uri.parse("content://settings/system/notification_sound");
        notification.contentView.setTextViewText(R.id.app_sum,
        		note.getTitle());
        notification.contentView.setTextViewText(R.id.title,
        		contentText);
        Intent clickIntent = new Intent();
        clickIntent.setClass(context, WXEntryActivity.class);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        clickIntent.putExtras(bundle);
       
        PendingIntent pendingClickIntent = PendingIntent.getActivity(context, noteId, clickIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT);
        notification.contentIntent = pendingClickIntent;
        nm.notify(noteId, notification);
        noteDb.closeDatabase();
    }

    private void cancelNotification(Context context, int noteId) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(noteId);
    }

    public static void scheduleAlarms() {
        CalendarAlarmManager noteAlarmManager = CalendarAlarmManager.getInstance();
        noteAlarmManager.scheduleAlarms();
    }
    public static void scheduleAlarmById(int noteId, int actionType,Long reminderData) {
    	CalendarAlarmManager noteAlarmManager = CalendarAlarmManager.getInstance();
        noteAlarmManager.scheduleAlarmById(noteId, actionType,reminderData);
    }
   



}