package com.aurora.calendar;

import com.android.calendar.GeneralPreferences;
import com.android.calendar.Utils;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;

public class AuroraBirthdayReceicer extends BroadcastReceiver {

    private static final int PACKAGE_NAME_START_INDEX = 8;
    private static final String NOTE_APP_PACKAGE_NAME = "com.aurora.note";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_DATA_CLEARED.equals(action)) {
            String data = intent.getDataString();
            if (data == null || data.length() <= PACKAGE_NAME_START_INDEX) {
                return;
            }
            String packageName = data.substring(PACKAGE_NAME_START_INDEX);
            if (NOTE_APP_PACKAGE_NAME.equals(packageName)) {
                clearNoteReminders(context);
            }
            return;
        }

        SharedPreferences sp = GeneralPreferences.getSharedPreferences(context);
        String reminderValue = sp.getString(AuroraCalendarSettingActivity.EVENTS_REMINDER, String.valueOf(10));
        AuroraCalendarSettingActivity.setReminderValue(context, reminderValue);

        ContentResolver cr = context.getContentResolver();
        addLocalAccountIfNot(cr);
        addBirthdayAccountIfNot(cr);
        context.startService(new Intent(context, AuroraBirthdayService.class));
    }

    private void clearNoteReminders(Context context) {
        context.getContentResolver().delete(
                Events.CONTENT_URI,
                Events.ACCOUNT_NAME + "='" + Utils.NOTE_REMINDER_ACCOUNT_NAME + "'",
                null);
    }

    private void addLocalAccountIfNot(ContentResolver resolver) {
        Cursor cursor = resolver.query(
                Calendars.CONTENT_URI,
                new String[] {Calendars._ID},
                Calendars.ACCOUNT_NAME + "='" + Utils.PC_SYNC_ACCOUNT_NAME + "'",
                null,
                null);
        if (cursor != null && cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(Calendars.ACCOUNT_NAME, Utils.PC_SYNC_ACCOUNT_NAME);
            values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
            values.put(Calendars.CALENDAR_DISPLAY_NAME, Utils.PC_SYNC_ACCOUNT_NAME);
            values.put(Calendars.CALENDAR_COLOR, -9215145);
            values.put(Calendars.CALENDAR_ACCESS_LEVEL, 700);
            values.put(Calendars.SYNC_EVENTS, 1);
            values.put(Calendars.OWNER_ACCOUNT, Utils.PC_SYNC_ACCOUNT_NAME);
            resolver.insert(Calendars.CONTENT_URI, values);
        }
        if (cursor != null) cursor.close();
    }

    private void addBirthdayAccountIfNot(ContentResolver resolver) {
        Cursor cursor = resolver.query(
                Calendars.CONTENT_URI,
                new String[] {Calendars._ID},
                Calendars.ACCOUNT_NAME + "='" + Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME + "'",
                null,
                null);
        if (cursor != null && cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(Calendars.ACCOUNT_NAME, Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME);
            values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
            values.put(Calendars.CALENDAR_DISPLAY_NAME, Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME);
            values.put(Calendars.CALENDAR_COLOR, -9215145);
            values.put(Calendars.CALENDAR_ACCESS_LEVEL, 700);
            values.put(Calendars.SYNC_EVENTS, 1);
            values.put(Calendars.OWNER_ACCOUNT, Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME);
            resolver.insert(Calendars.CONTENT_URI, values);
        }
        if (cursor != null) cursor.close();
    }
}