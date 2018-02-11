package com.aurora.note.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
// import android.net.Uri;
import android.os.Bundle;

import com.aurora.note.R;
import com.aurora.note.activity.NewNoteActivity;
import com.aurora.note.bean.NoteResult;
import com.aurora.note.db.NoteAdapter;
import com.aurora.note.util.Globals;
import com.aurora.note.util.SystemUtils;

public class NoteAlarmReceiver extends BroadcastReceiver {

//    private static final String TAG = "NoteAlarmReceiver";

    private static final int PACKAGE_NAME_START_INDEX = 8;
    private static final String CALENDAR_STORGE_PACKAGE_NAME = "com.android.providers.calendar";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (NoteAlarmManager.ACTION_NOTE_ALARM.equals(action)) {
            Bundle bundle = intent.getExtras();
            updateNotification(context, bundle.getInt(NoteAdapter.ID, 0));
        } else if (NoteAlarmManager.ACTION_NOTE_ALARM_CANCEL.equals(action)) {
            Bundle bundle = intent.getExtras();
            cancelNotification(context, bundle.getInt(NoteAdapter.ID, 0));
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            scheduleAlarms();
        } else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
            scheduleAlarms();
        } else if (Intent.ACTION_PACKAGE_DATA_CLEARED.equals(action)) {
            String data = intent.getDataString();
            if (data == null || data.length() <= PACKAGE_NAME_START_INDEX) {
                return;
            }
            String packageName = data.substring(PACKAGE_NAME_START_INDEX);
            if (CALENDAR_STORGE_PACKAGE_NAME.equals(packageName)) {
                addNoteReminders();
            }
        }
    }

    private void updateNotification(Context context, int noteId) {
        NoteAdapter noteDb = new NoteAdapter(context);
        noteDb.open();
        NoteResult note = noteDb.queryDataByID(noteId);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (note == null || note.getIs_warn() == 0) {
            nm.cancel(noteId);
            return;
        }

        String contentText = buildName(context, note.getContent(), note.getImage_count(),
                note.getVideo_count(), note.getSound_count());

        Bundle bundle = new Bundle();
        bundle.putInt(NewNoteActivity.TYPE_GET_DATA, 0);
        bundle.putParcelable(NewNoteActivity.NOTE_OBJ, note);

        Intent clickIntent = new Intent();
        clickIntent.setClass(context, NewNoteActivity.class);
        // clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        clickIntent.putExtras(bundle);
        PendingIntent pendingClickIntent = PendingIntent.getActivity(context, noteId, clickIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notificationBuilder = new Notification.Builder(context);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentIntent(pendingClickIntent);
        notificationBuilder.setContentTitle(context.getString(R.string.app_name));
        notificationBuilder.setContentText(contentText);
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
        notificationBuilder.setPriority(Notification.PRIORITY_DEFAULT);
        notificationBuilder.setWhen(note.getWarn_time());

        Notification notification = notificationBuilder.build();
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_SOUND;
        // notification.sound = Uri.parse("content://settings/system/notification_sound");

        nm.notify(noteId, notification);
    }

    private void cancelNotification(Context context, int noteId) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(noteId);
    }

    public static void scheduleAlarms() {
        NoteAlarmManager noteAlarmManager = NoteAlarmManager.getInstance();
        noteAlarmManager.scheduleAlarms();
    }

    public static void addNoteReminders() {
        NoteAlarmManager noteAlarmManager = NoteAlarmManager.getInstance();
        noteAlarmManager.addNoteReminders();
    }

    public static void scheduleAlarmById(int noteId, int actionType) {
        NoteAlarmManager noteAlarmManager = NoteAlarmManager.getInstance();
        noteAlarmManager.scheduleAlarmById(noteId, actionType);
    }

    public static void initNoteData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Globals.SHARE_PREF_NAME, Context.MODE_PRIVATE);
        boolean hasInited = prefs.getBoolean(Globals.PREF_HAS_INITED, false);
        if (!hasInited) {
            prefs.edit().putBoolean(Globals.PREF_HAS_INITED, true).commit();

            NoteAlarmManager noteAlarmManager = NoteAlarmManager.getInstance();
            noteAlarmManager.initNoteData();
        }

        boolean labelInited = prefs.getBoolean(Globals.PREF_LABEL_INITED, false);
        if (!labelInited) {
            prefs.edit().putBoolean(Globals.PREF_LABEL_INITED, true).commit();

            NoteAlarmManager manager = NoteAlarmManager.getInstance();
            manager.initLabelData();
        }

        boolean festivalInited = prefs.getBoolean(Globals.PREF_FESTIVAL_INITED, false);
        if (!festivalInited) {
            prefs.edit().putBoolean(Globals.PREF_FESTIVAL_INITED, true).commit();

            NoteAlarmManager manager = NoteAlarmManager.getInstance();
            manager.initFestivalData();
        }
    }

    public static String buildName(Context context, String fullText, int imageCount, int videoCount, int soundCount) {
        String temp = "";
        String[][] object = { new String[] { Globals.ATTACHMENT_ALL_PATTERN, "" } };
        String[] lines = SystemUtils.replace(fullText, object).trim().split("\n");
        if (lines.length > 0) {
            temp = lines[0].trim();
        }
        if (temp.length() > 0) {
            return temp;
        } else {
            if (imageCount > 0) {
                temp = context.getString(R.string.title_num_of_images, imageCount);
            }
            if (videoCount > 0) {
                temp += context.getString(R.string.title_num_of_videos, videoCount);
            }
            if (soundCount > 0) {
                temp += context.getString(R.string.title_num_of_sounds, soundCount);
            }
        }
        return temp;
    }

}