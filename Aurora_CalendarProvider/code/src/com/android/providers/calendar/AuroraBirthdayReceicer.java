package com.android.providers.calendar;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;

public class AuroraBirthdayReceicer extends BroadcastReceiver {

    private static final String PC_SYNC_ACCOUNT_NAME = "PC Sync";
    public static final String BIRTHDAY_REMINDER_ACCOUNT_NAME = "Birthday Reminder";

    @Override
    public void onReceive(Context context, Intent intent) {
        ContentResolver cr = context.getContentResolver();
        addLocalAccountIfNot(cr);
        addBirthdayAccountIfNot(cr);
        context.startService(new Intent(context, AuroraBirthdayService.class));
    }

    private void addLocalAccountIfNot(ContentResolver resolver) {
        Cursor cursor = resolver.query(
                Calendars.CONTENT_URI,
                new String[] {Calendars._ID},
                Calendars.ACCOUNT_NAME + "='" + PC_SYNC_ACCOUNT_NAME + "'",
                null,
                null);
        if (cursor != null && cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(Calendars.ACCOUNT_NAME, PC_SYNC_ACCOUNT_NAME);
            values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
            values.put(Calendars.CALENDAR_DISPLAY_NAME, PC_SYNC_ACCOUNT_NAME);
            values.put(Calendars.CALENDAR_COLOR, -9215145);
            values.put(Calendars.CALENDAR_ACCESS_LEVEL, 700);
            values.put(Calendars.SYNC_EVENTS, 1);
            values.put(Calendars.OWNER_ACCOUNT, PC_SYNC_ACCOUNT_NAME);
            resolver.insert(Calendars.CONTENT_URI, values);
        }
        if (cursor != null) cursor.close();
    }

    private void addBirthdayAccountIfNot(ContentResolver resolver) {
        Cursor cursor = resolver.query(
                Calendars.CONTENT_URI,
                new String[] {Calendars._ID},
                Calendars.ACCOUNT_NAME + "='" + BIRTHDAY_REMINDER_ACCOUNT_NAME + "'",
                null,
                null);
        if (cursor != null && cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(Calendars.ACCOUNT_NAME, BIRTHDAY_REMINDER_ACCOUNT_NAME);
            values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
            values.put(Calendars.CALENDAR_DISPLAY_NAME, BIRTHDAY_REMINDER_ACCOUNT_NAME);
            values.put(Calendars.CALENDAR_COLOR, -9215145);
            values.put(Calendars.CALENDAR_ACCESS_LEVEL, 700);
            values.put(Calendars.SYNC_EVENTS, 1);
            values.put(Calendars.OWNER_ACCOUNT, BIRTHDAY_REMINDER_ACCOUNT_NAME);
            resolver.insert(Calendars.CONTENT_URI, values);
        }
        if (cursor != null) cursor.close();
    }
}