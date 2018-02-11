package com.aurora.note.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.text.format.DateUtils;
import android.text.format.Time;

import com.aurora.note.NoteApp;
import com.aurora.note.R;
import com.aurora.note.bean.LabelResult;
import com.aurora.note.bean.NoteResult;
import com.aurora.note.db.LabelAdapter;
import com.aurora.note.db.NoteAdapter;
import com.aurora.note.util.Globals;
import com.aurora.note.util.SystemUtils;

import java.util.TimeZone;
import java.util.UUID;

public class NoteAlarmManager {

    protected static final String TAG = "NoteAlarmManager";

    public static final int ACTION_INSERT = 0;
    public static final int ACTION_UPDATE = 1;
    public static final int ACTION_DELETE = 2;

    public static final String NOTE_REMINDER_ACCOUNT_NAME = "Note Reminder";
    public static final String ACTION_NOTE_ALARM = "com.aurora.note.NOTE_ALARM";
    public static final String ACTION_NOTE_ALARM_CANCEL = "com.aurora.note.NOTE_ALARM_CANCEL";

    protected Context mContext = NoteApp.ysApp;
    protected ContentResolver mResolver;
    private AlarmManager mAlarmManager;
    private NoteAdapter mNoteDb;

    private static NoteAlarmManager mManager = null;
    public static synchronized NoteAlarmManager getInstance() {
        if (mManager == null) {
            mManager = new NoteAlarmManager();
        }
        return mManager;
    }

    public NoteAlarmManager() {
        initializeWithContext();
    }

    protected void initializeWithContext() {
        Context context = mContext;
        mResolver = context.getContentResolver();
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mNoteDb = new NoteAdapter(context);
        mNoteDb.open();
    }

    void scheduleAlarms() {
        Cursor cursor = mNoteDb.queryDataForAlarm();
        if (cursor != null) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                int noteId = cursor.getInt(cursor.getColumnIndex(NoteAdapter.ID));
                long alarmTime = cursor.getLong(cursor.getColumnIndex(NoteAdapter.WARN_TIME));

                Intent intent = new Intent(ACTION_NOTE_ALARM);
                intent.putExtra(NoteAdapter.ID, noteId);
                PendingIntent pi = PendingIntent.getBroadcast(mContext, noteId, intent, PendingIntent.FLAG_NO_CREATE);
                if (pi != null) {
                    mAlarmManager.cancel(pi);
                }
                pi = PendingIntent.getBroadcast(mContext, noteId, intent, 0);
                mAlarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pi);
            }
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    public void scheduleAlarmById(int noteId, int actionType) {
        if (actionType == ACTION_INSERT) {
            NoteResult note = mNoteDb.queryDataByID(noteId);
            scheduleAlarmWhenInsert(noteId, note);
            insertNoteReminder(noteId, note);
        } else if (actionType == ACTION_UPDATE) {
            NoteResult note = mNoteDb.queryDataByID(noteId);
            scheduleAlarmWhenUpdate(noteId, note);
            updateNoteReminder(noteId, note);
        } else if (actionType == ACTION_DELETE) {
            scheduleAlarmWhenDelete(noteId);
            deleteNoteReminder(noteId);
        }
    }

    void scheduleAlarmWhenInsert(int noteId, NoteResult note) {
        if (note == null || note.getIs_warn() == 0 || note.getWarn_time() < System.currentTimeMillis()) return;

        Intent intent = new Intent(ACTION_NOTE_ALARM);
        intent.putExtra(NoteAdapter.ID, noteId);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, noteId, intent, 0);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, note.getWarn_time(), pi);
    }

    void scheduleAlarmWhenUpdate(int noteId, NoteResult note) {
        if (note == null) return;

        Intent intent = new Intent(ACTION_NOTE_ALARM);
        intent.putExtra(NoteAdapter.ID, noteId);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, noteId, intent, PendingIntent.FLAG_NO_CREATE);
        if (pi != null) {
            mAlarmManager.cancel(pi);
        }

        pi = PendingIntent.getBroadcast(mContext, noteId, intent, 0);
        long currentMillis = System.currentTimeMillis();
        if (note.getIs_warn() == 1 && note.getWarn_time() >= currentMillis) {
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, note.getWarn_time(), pi);
        }

        scheduleCancelAlarm(noteId);
    }

    void scheduleAlarmWhenDelete(int noteId) {
        Intent intent = new Intent(ACTION_NOTE_ALARM);
        intent.putExtra(NoteAdapter.ID, noteId);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, noteId, intent, PendingIntent.FLAG_NO_CREATE);
        if (pi != null) {
            mAlarmManager.cancel(pi);
        }

        scheduleCancelAlarm(noteId);
    }

    void scheduleCancelAlarm(int noteId) {
        Intent intent = new Intent(ACTION_NOTE_ALARM_CANCEL);
        intent.putExtra(NoteAdapter.ID, noteId);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        long triggerAtTime = SystemClock.elapsedRealtime() + DateUtils.SECOND_IN_MILLIS;
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
    }

    void insertNoteReminder(int noteId, NoteResult note) {
        if (note == null || note.getIs_warn() == 0) return;

        Cursor cursor = mResolver.query(
                Calendars.CONTENT_URI,
                new String[] { Calendars._ID},
                Calendars.ACCOUNT_NAME + "='" + NOTE_REMINDER_ACCOUNT_NAME + "'",
                null,
                null);

        long calendarId = 0;
        if (cursor != null && cursor.moveToFirst()) {
            calendarId = cursor.getLong(0);
        } else {
            ContentValues values = new ContentValues();
            values.put(Calendars.ACCOUNT_NAME, NOTE_REMINDER_ACCOUNT_NAME);
            values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
            values.put(Calendars.CALENDAR_DISPLAY_NAME, NOTE_REMINDER_ACCOUNT_NAME);
            values.put(Calendars.CALENDAR_COLOR, Color.parseColor("#019C73"));
            values.put(Calendars.CALENDAR_ACCESS_LEVEL, 700);
            values.put(Calendars.SYNC_EVENTS, 1);
            values.put(Calendars.OWNER_ACCOUNT, NOTE_REMINDER_ACCOUNT_NAME);

            Uri uri = mResolver.insert(Calendars.CONTENT_URI, values);
            calendarId = ContentUris.parseId(uri);
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        Time time = new Time();
        time.set(note.getWarn_time());
        time.hour = 0;
        time.minute = 0;
        time.second = 0;
        long startMillis = time.normalize(true);

        String contentTitle = NoteAlarmReceiver.buildName(mContext, note.getContent(),
                note.getImage_count(), note.getVideo_count(), note.getSound_count());

        ContentValues values = new ContentValues();
        values.put(Events.CALENDAR_ID, calendarId);
        values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        values.put(Events.TITLE, mContext.getString(R.string.note_reminder));
        values.put(Events.DESCRIPTION, contentTitle);
        values.put(Events.EVENT_LOCATION, "" + noteId);
        values.put(Events.ALL_DAY, 0);
        values.put(Events.DTSTART, startMillis);
        values.put(Events.DTEND, startMillis + DateUtils.DAY_IN_MILLIS - DateUtils.SECOND_IN_MILLIS * 5);
        values.put(Events.HAS_ATTENDEE_DATA, 1);
        values.put(Events.STATUS, Events.STATUS_CONFIRMED);

        mResolver.insert(Events.CONTENT_URI, values);
    }

    void updateNoteReminder(int noteId, NoteResult note) {
        if (note == null) return;

        if (note.getIs_warn() == 0) {
            deleteNoteReminder(noteId);
        } else {
            Cursor cursor = mResolver.query(
                    Events.CONTENT_URI,
                    new String[] { Events._ID},
                    Events.ACCOUNT_NAME + "='" + NOTE_REMINDER_ACCOUNT_NAME + "' AND " +
                    Events.EVENT_LOCATION + "='" + noteId + "'",
                    null,
                    null);

            if (cursor != null && cursor.moveToFirst()) {
                Time time = new Time();
                time.set(note.getWarn_time());
                time.hour = 0;
                time.minute = 0;
                time.second = 0;
                long startMillis = time.normalize(true);

                String contentTitle = NoteAlarmReceiver.buildName(mContext, note.getContent(),
                        note.getImage_count(), note.getVideo_count(), note.getSound_count());

                ContentValues values = new ContentValues();
                values.put(Events.DESCRIPTION, contentTitle);
                values.put(Events.DTSTART, startMillis);
                values.put(Events.DTEND, startMillis + DateUtils.DAY_IN_MILLIS - DateUtils.SECOND_IN_MILLIS * 5);

                mResolver.update(
                        Events.CONTENT_URI,
                        values,
                        Events._ID + "=" + cursor.getInt(0),
                        null);
            } else {
                insertNoteReminder(noteId, note);
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    void deleteNoteReminder(int noteId) {
        mResolver.delete(
                Events.CONTENT_URI,
                Events.ACCOUNT_NAME + "='" + NOTE_REMINDER_ACCOUNT_NAME + "' AND " +
                Events.EVENT_LOCATION + "='" + noteId + "'",
                null);
    }

    void initNoteData() {
        long currentMillis = System.currentTimeMillis();
        String firstNoteContent = mContext.getString(R.string.note_first) +
                "\n\n" +
                mContext.getString(R.string.note_second);

        if (SystemUtils.isIndiaVersion()) {
            firstNoteContent = mContext.getString(R.string.note_first_2);
        }

        NoteResult presetNote = new NoteResult();

        presetNote.setIs_preset(1);
        presetNote.setUuid(UUID.randomUUID().toString());
        presetNote.setContent(firstNoteContent);
        presetNote.setCharacter(firstNoteContent);
        presetNote.setIs_warn(0);
        presetNote.setCreate_time(currentMillis);
        presetNote.setUpdate_time(currentMillis);
        mNoteDb.insert(presetNote);

        presetNote.setUuid(UUID.randomUUID().toString());
        presetNote.setContent(mContext.getString(R.string.note_third));
        presetNote.setCharacter(mContext.getString(R.string.note_third));
        mNoteDb.insert(presetNote);
    }

    void initLabelData() {
        LabelAdapter labelDb = new LabelAdapter(mContext);
        labelDb.open();

        long currentMillis = System.currentTimeMillis();

        LabelResult label = new LabelResult();
        label.setContent(mContext.getString(R.string.label_meetting));
        label.setUpdate_time(currentMillis);
        labelDb.insert(label);

        label.setContent(mContext.getString(R.string.label_life));
        labelDb.insert(label);

        label.setContent(mContext.getString(R.string.label_idea));
        labelDb.insert(label);

        labelDb.close();
    }

    void initFestivalData() {
        long currentMillis = System.currentTimeMillis();
        String presetChunjieContent = mContext.getString(R.string.preset_chunjie_content);
        String presetQingrenjieContent = mContext.getString(R.string.preset_qingrenjie_content);

        NoteResult presetNote = new NoteResult();

        presetNote.setIs_preset(2);
        presetNote.setUuid(UUID.randomUUID().toString());
        presetNote.setContent(Globals.PRESET_IMAGE_QINGRENJIE_TEXT + Globals.NEW_LINE + presetQingrenjieContent);
        presetNote.setCharacter(presetQingrenjieContent);
        presetNote.setImage_count(1);
        presetNote.setIs_warn(0);
        presetNote.setCreate_time(currentMillis);
        presetNote.setUpdate_time(currentMillis);
        mNoteDb.insert(presetNote);

        presetNote.setUuid(UUID.randomUUID().toString());
        presetNote.setContent(Globals.PRESET_IMAGE_CHUNJIE_TEXT + Globals.NEW_LINE + presetChunjieContent);
        presetNote.setCharacter(presetChunjieContent);
        presetNote.setImage_count(1);
        mNoteDb.insert(presetNote);
    }

    void addNoteReminders() {
        
    }

}