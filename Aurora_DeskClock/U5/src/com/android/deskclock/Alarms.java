/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.deskclock;

import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.WindowManager;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import android.os.SystemProperties;
//Gionee <baorui><2013-05-04> modify for CR00803588 begin
import android.provider.MediaStore;
import android.os.Build.VERSION;
import android.os.storage.StorageManager;
import android.os.Environment;

//Gionee <baorui><2013-05-04> modify for CR00803588 end
//Gionee <baorui><2013-07-12> modify for CR00835747 begin
import com.aurora.utils.GnRingtoneUtil;

/**
 * The Alarms provider supplies info about Alarm Clock settings
 */
public class Alarms {
    public static final String ALARM_MANAGER_TAG = "ALARM_MANAGER";
    // This action triggers the AlarmReceiver as well as the AlarmKlaxon. It
    // is a public action used in the manifest for receiving Alarm broadcasts
    // from the alarm manager.
    public static final String ALARM_ALERT_ACTION = "com.android.deskclock.ALARM_ALERT";

    // A public action sent by AlarmKlaxon when the alarm has stopped sounding
    // for any reason (e.g. because it has been dismissed from AlarmAlertFullScreen,
    // or killed due to an incoming phone call, etc).
    public static final String ALARM_DONE_ACTION = "com.android.deskclock.ALARM_DONE";

    // AlarmAlertFullScreen listens for this broadcast intent, so that other applications
    // can snooze the alarm (after ALARM_ALERT_ACTION and before ALARM_DONE_ACTION).
    public static final String ALARM_SNOOZE_ACTION = "com.android.deskclock.ALARM_SNOOZE";

    // AlarmAlertFullScreen listens for this broadcast intent, so that other applications
    // can dismiss the alarm (after ALARM_ALERT_ACTION and before ALARM_DONE_ACTION).
    public static final String ALARM_DISMISS_ACTION = "com.android.deskclock.ALARM_DISMISS";

    // This is a private action used by the AlarmKlaxon to update the UI to
    // show the alarm has been killed.
    public static final String ALARM_KILLED = "alarm_killed";

    // Extra in the ALARM_KILLED intent to indicate to the user how long the
    // alarm played before being killed.
    public static final String ALARM_KILLED_TIMEOUT = "alarm_killed_timeout";

    // This string is used to indicate a silent alarm in the db.
    public static final String ALARM_ALERT_SILENT = "silent";

    // This intent is sent from the notification when the user cancels the
    // snooze alert.
    public static final String CANCEL_SNOOZE = "cancel_snooze";

    // This string is used when passing an Alarm object through an intent.
    public static final String ALARM_INTENT_EXTRA = "intent.extra.alarm";

    // This extra is the raw Alarm object data. It is used in the
    // AlarmManagerService to avoid a ClassNotFoundException when filling in
    // the Intent extras.
    public static final String ALARM_RAW_DATA = "intent.extra.alarm_raw";

    private static final String PREF_SNOOZE_IDS = "snooze_ids";
    private static final String PREF_SNOOZE_TIME = "snooze_time";

    private final static String DM12 = "E h:mm aa";
    private final static String DM24 = "E kk:mm";

    private final static String M12 = "h:mm aa";
    // Shared with DigitalClock
    final static String M24 = "kk:mm";

    final static int INVALID_ALARM_ID = -1;
    
    //android:hxc start
	 public static final String POWER_OFF_ALARM_ALERT_ACTION = "android.intent.action.POWER_OFF_ALARM_ALERT";
    
	
    public static final int POWER_OFF_WAKE_UP = 8;
    static final String PREF_NEAREST_ALARM_ID = "nearest_id";
    static final String PREF_NEAREST_ALARM_TIME = "nearest_time";
    //android:hxc end

    //android:zjy 20120503 add for CR00576747  start  
    final static String ALARM_REPEAT_RING = "alarm_repeat_ring";
    //android:zjy 20120503 add for CR00576747  end
    
	// Gionee baorui 2012-09-06 modify for CR00683131 begin
	public static boolean mIfDismiss = false;
	// Gionee baorui 2012-09-06 modify for CR00683131 end
	// Gionee baorui 2012-09-17 modify for CR00689742 begin
	public static int mAlarmId = -1;
	// Gionee baorui 2012-09-17 modify for CR00689742 end

    // Gionee <baorui><2013-04-01> modify for CR00791036 begin
    // Gionee <baorui><2013-04-23> modify for CR00799490 begin
    // Add a function , in the system time area show the next alarm
    // Gionee <baorui><2013-04-23> modify for CR00799490 end
    public static final String NEXT_ALARM_TIME_SET = "com.android.deskclock.NEXT_ALARM_TIME_SET";
    // Gionee <baorui><2013-04-01> modify for CR00791036 end
    // Gionee <baorui><2013-05-04> modify for CR00803588 begin
    private static StorageManager mStorageManager;
    // Gionee <baorui><2013-05-04> modify for CR00803588 end

    // Gionee <baorui><2013-07-12> modify for CR00835747 begin
    public static boolean mIsGnMtkPoweroffAlarmSupport = SystemProperties.get(
            "ro.gn.mtk.poweroff.alarm.prop", "no").equals("yes");
    // Gionee <baorui><2013-07-12> modify for CR00835747 end

    /**
     * Creates a new Alarm and fills in the given alarm's id.
     */
    public static long addAlarm(Context context, Alarm alarm) {
        ContentValues values = createContentValues(alarm);
        Uri uri = context.getContentResolver().insert(
                Alarm.Columns.CONTENT_URI, values);
        // Gionee <baorui><2013-05-04> modify for CR00803588 begin
        values = gnCreateContentValues(context, alarm);
        context.getContentResolver().insert(Alarm.Columns.ALERTINFO_URI, values);
        // Gionee <baorui><2013-05-04> modify for CR00803588 end
        alarm.id = (int) ContentUris.parseId(uri);

        long timeInMillis = calculateAlarm(alarm);
        if (alarm.enabled) {
            clearSnoozeIfNeeded(context, timeInMillis);
        }
        setNextAlert(context);
        return timeInMillis;
    }

    /**
     * Removes an existing Alarm.  If this alarm is snoozing, disables
     * snooze.  Sets next alert.
     */
    public static void deleteAlarm(Context context, int alarmId) {
        if (alarmId == INVALID_ALARM_ID) return;

        ContentResolver contentResolver = context.getContentResolver();
        /* If alarm is snoozing, lose it */
        disableSnoozeAlert(context, alarmId);

        Uri uri = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId);
        contentResolver.delete(uri, "", null);
        // Gionee <baorui><2013-05-04> modify for CR00803588 begin
        uri = ContentUris.withAppendedId(Alarm.Columns.ALERTINFO_URI, alarmId);
        contentResolver.delete(uri, "", null);
        // Gionee <baorui><2013-05-04> modify for CR00803588 end

        setNextAlert(context);
    }

    /**
     * Queries all alarms
     * @return cursor over all alarms
     */
    public static Cursor getAlarmsCursor(ContentResolver contentResolver) {
        return contentResolver.query(
                Alarm.Columns.CONTENT_URI, Alarm.Columns.ALARM_QUERY_COLUMNS,
                null, null, Alarm.Columns.DEFAULT_SORT_ORDER);
    }

    // Private method to get a more limited set of alarms from the database.
    private static Cursor getFilteredAlarmsCursor(
            ContentResolver contentResolver) {
        return contentResolver.query(Alarm.Columns.CONTENT_URI,
                Alarm.Columns.ALARM_QUERY_COLUMNS, Alarm.Columns.WHERE_ENABLED,
                null, null);
    }
    
    //aurora add by tangjun start 2013.12.28
    public static Cursor getSortedEnabledAlarmsCursor(
            ContentResolver contentResolver) {
        return contentResolver.query(Alarm.Columns.CONTENT_URI,
                Alarm.Columns.ALARM_QUERY_COLUMNS, Alarm.Columns.WHERE_ENABLED,
                null, Alarm.Columns.DEFAULT_SORT_ORDER);
    }
    //aurora add by tangjun end 2013.12.28

    private static ContentValues createContentValues(Alarm alarm) {
        ContentValues values = new ContentValues(8);
        // Set the alarm_time value if this alarm does not repeat. This will be
        // used later to disable expire alarms.
        long time = 0;
        if (!alarm.daysOfWeek.isRepeatSet()) {
            time = calculateAlarm(alarm);
        }

        values.put(Alarm.Columns.ENABLED, alarm.enabled ? 1 : 0);
        values.put(Alarm.Columns.HOUR, alarm.hour);
        values.put(Alarm.Columns.MINUTES, alarm.minutes);
        values.put(Alarm.Columns.ALARM_TIME, time);
        values.put(Alarm.Columns.DAYS_OF_WEEK, alarm.daysOfWeek.getCoded());
        values.put(Alarm.Columns.VIBRATE, alarm.vibrate);
        values.put(Alarm.Columns.MESSAGE, alarm.label);

        // A null alert Uri indicates a silent alarm.
        values.put(Alarm.Columns.ALERT, alarm.alert == null ? ALARM_ALERT_SILENT
                : alarm.alert.toString());

        return values;
    }

    private static void clearSnoozeIfNeeded(Context context, long alarmTime) {
        // If this alarm fires before the next snooze, clear the snooze to
        // enable this alarm.
        SharedPreferences prefs = context.getSharedPreferences(AlarmClock.PREFERENCES, 0);

        // Get the list of snoozed alarms
        final Set<String> snoozedIds = prefs.getStringSet(PREF_SNOOZE_IDS, new HashSet<String>());
        final Set<String> snoozedIdsForCopy = new HashSet<String>();
        snoozedIdsForCopy.addAll(snoozedIds);
        for (String snoozedAlarm : snoozedIdsForCopy) {
            final long snoozeTime = prefs.getLong(getAlarmPrefSnoozeTimeKey(snoozedAlarm), 0);
            if (alarmTime < snoozeTime) {
                final int alarmId = Integer.parseInt(snoozedAlarm);
                clearSnoozePreference(context, prefs, alarmId);
            }
        }
    }

    /**
     * Return an Alarm object representing the alarm id in the database.
     * Returns null if no alarm exists.
     */
    public static Alarm getAlarm(ContentResolver contentResolver, int alarmId) {
        Cursor cursor = contentResolver.query(
                ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId),
                Alarm.Columns.ALARM_QUERY_COLUMNS,
                null, null, null);
        Alarm alarm = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                alarm = new Alarm(cursor);
            }
            cursor.close();
        }
        return alarm;
    }


    /**
     * A convenience method to set an alarm in the Alarms
     * content provider.
     * @return Time when the alarm will fire.
     */
    public static long setAlarm(Context context, Alarm alarm) {
        ContentValues values = createContentValues(alarm);
        ContentResolver resolver = context.getContentResolver();
        resolver.update(
                ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id),
                values, null, null);
        // Gionee <baorui><2013-05-04> modify for CR00803588 begin
        values = gnCreateContentValues(context, alarm);
        resolver.update(ContentUris.withAppendedId(Alarm.Columns.ALERTINFO_URI, alarm.id), values, null, null);
        // Gionee <baorui><2013-05-04> modify for CR00803588 end

        long timeInMillis = calculateAlarm(alarm);

        if (alarm.enabled) {
            // Disable the snooze if we just changed the snoozed alarm. This
            // only does work if the snoozed alarm is the same as the given
            // alarm.
            // TODO: disableSnoozeAlert should have a better name.
            disableSnoozeAlert(context, alarm.id);

            // Disable the snooze if this alarm fires before the snoozed alarm.
            // This works on every alarm since the user most likely intends to
            // have the modified alarm fire next.
            clearSnoozeIfNeeded(context, timeInMillis);
        }

        setNextAlert(context);

        return timeInMillis;
    }

    /**
     * A convenience method to enable or disable an alarm.
     *
     * @param id             corresponds to the _id column
     * @param enabled        corresponds to the ENABLED column
     */

    public static void enableAlarm(
            final Context context, final int id, boolean enabled) {
        enableAlarmInternal(context, id, enabled);
        setNextAlert(context);
    }

    private static void enableAlarmInternal(final Context context,
            final int id, boolean enabled) {
        enableAlarmInternal(context, getAlarm(context.getContentResolver(), id),
                enabled);
    }

    private static void enableAlarmInternal(final Context context,
            final Alarm alarm, boolean enabled) {
        if (alarm == null) {
            return;
        }
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues(2);
        values.put(Alarm.Columns.ENABLED, enabled ? 1 : 0);

        // If we are enabling the alarm, calculate alarm time since the time
        // value in Alarm may be old.
        if (enabled) {
            long time = 0;
            if (!alarm.daysOfWeek.isRepeatSet()) {
                time = calculateAlarm(alarm);
            }
            values.put(Alarm.Columns.ALARM_TIME, time);
        } else {
            // Clear the snooze if the id matches.
            disableSnoozeAlert(context, alarm.id);
        }

        resolver.update(ContentUris.withAppendedId(
                Alarm.Columns.CONTENT_URI, alarm.id), values, null, null);
    }

    private static Alarm calculateNextAlert(final Context context) {
        long minTime = Long.MAX_VALUE;
        long now = System.currentTimeMillis();
        final SharedPreferences prefs = context.getSharedPreferences(AlarmClock.PREFERENCES, 0);

        Set<Alarm> alarms = new HashSet<Alarm>();

        // We need to to build the list of alarms from both the snoozed list and the scheduled
        // list.  For a non-repeating alarm, when it goes of, it becomes disabled.  A snoozed
        // non-repeating alarm is not in the active list in the database.

        // first go through the snoozed alarms
        final Set<String> snoozedIds = prefs.getStringSet(PREF_SNOOZE_IDS, new HashSet<String>());
        for (String snoozedAlarm : snoozedIds) {
            final int alarmId = Integer.parseInt(snoozedAlarm);
            final Alarm a = getAlarm(context.getContentResolver(), alarmId);
            alarms.add(a);
        }

        // Now add the scheduled alarms
		//add by jiating remove final
        Cursor cursor = getFilteredAlarmsCursor(context.getContentResolver());
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        final Alarm a = new Alarm(cursor);
                        alarms.add(a);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }

        Alarm alarm = null;

        for (Alarm a : alarms) {
            // A time of 0 indicates this is a repeating alarm, so
            // calculate the time to get the next alert.
			// androide jiating 20120618 update when local time change alarm
			// which never alart will recalculate alarmTime begin
			// if (a.time == 0) {

        	if (a.time == 0) {
                a.time = calculateAlarm(a);
            }
        	
//			a.time = calculateAlarm(a);
			// }
			// androide jiating 20120618 update when local time change alarm
			// which never alart will recalculate alarmTime end

            // Update the alarm if it has been snoozed
            updateAlarmTimeForSnooze(prefs, a);

            if (a.time < now) {

                // Expired alarm, disable it and move along.
                enableAlarmInternal(context, a, false);
                continue;
            }
            if (a.time < minTime) {
                minTime = a.time;
                alarm = a;
            }
        }
        
        if (alarm != null) {
        	SharedPreferences sharedPreferences = context.getSharedPreferences("somesettingstate", Context.MODE_PRIVATE);
        	Editor editor = sharedPreferences.edit();
        	editor.putInt("shutdownalarmid", alarm.id);
        	editor.apply();
		}

        return alarm;
    }

    /**
     * Disables non-repeating alarms that have passed.  Called at
     * boot.
     */
    public static void disableExpiredAlarms(final Context context) {
        Cursor cur = getFilteredAlarmsCursor(context.getContentResolver());
        long now = System.currentTimeMillis();

        try {
            if (cur.moveToFirst()) {
                do {
                    Alarm alarm = new Alarm(cur);
                    // A time of 0 means this alarm repeats. If the time is
                    // non-zero, check if the time is before now.
                    if (alarm.time != 0 && alarm.time < now) {
                        enableAlarmInternal(context, alarm, false);
                    }
                } while (cur.moveToNext());
            }
        } finally {
            cur.close();
        }
    }

    /**
     * Called at system startup, on time/timezone change, and whenever
     * the user changes alarm settings.  Activates snooze if set,
     * otherwise loads all alarms, activates next alert.
     */
    public static void setNextAlert(final Context context) {
        final Alarm alarm = calculateNextAlert(context);
        if (alarm != null) {
            enableAlert(context, alarm, alarm.time);
        } else {
            disableAlert(context);
        }
        // Gionee <baorui><2013-04-01> modify for CR00791036 begin
        Intent i = new Intent(NEXT_ALARM_TIME_SET);
        context.sendBroadcast(i);
        // Gionee <baorui><2013-04-01> modify for CR00791036 end
    }

    private static final int RTC_POWEROFF_WAKEUP=4;//4.3之前版本没有该变量，为了使编译通过，所以定义该值
    private static String ONE_PLUS="A0001";//1加手机型号
    
    
    /**
     * Sets alert in AlarmManger and StatusBar.  This is what will
     * actually launch the alert when the alarm triggers.
     *
     * @param alarm Alarm.
     * @param atTimeInMillis milliseconds since epoch
     */
    private static void enableAlert(Context context, final Alarm alarm,
            final long atTimeInMillis) {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ALARM_ALERT_ACTION);

        // XXX: This is a slight hack to avoid an exception in the remote
        // AlarmManagerService process. The AlarmManager adds extra data to
        // this Intent which causes it to inflate. Since the remote process
        // does not know about the Alarm class, it throws a
        // ClassNotFoundException.
        //
        // To avoid this, we marshall the data ourselves and then parcel a plain
        // byte[] array. The AlarmReceiver class knows to build the Alarm
        // object from the byte[] array.
        Parcel out = Parcel.obtain();
        alarm.writeToParcel(out, 0);
        out.setDataPosition(0);
        intent.putExtra(ALARM_RAW_DATA, out.marshall());

        PendingIntent sender = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar cal=Calendar.getInstance();
        cal.setTimeInMillis(atTimeInMillis);
        Calendar now=Calendar.getInstance();
        android.util.Log.e("jadon","设置闹钟的时间是="+(cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND))
				+"  当前时间是"+now.get(Calendar.HOUR_OF_DAY)+":"+(now.get(Calendar.MINUTE)+":"+now.get(Calendar.SECOND)));
       AlarmClockInfo alarmInfo = new AlarmClockInfo(atTimeInMillis,sender);
       am.setShutDownAlarmClock(alarmInfo);
       //am.setAlarmClock(alarmInfo, sender);
        storeNearestAlarm(context, alarm);
        //android:hxc end
        setStatusBarIcon(context, true);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(atTimeInMillis);
        String timeString = formatDayAndTime(context, c);
        saveNextAlarm(context, timeString);
    }

    private static boolean isSupport(){
    	if(android.os.Build.MODEL.equals(ONE_PLUS))
    	{
    		return false;
    	}
    	return true;
    }
    
    /**
     * Disables alert in AlarmManger and StatusBar.
     *
     * @param id Alarm ID.
     */
    static void disableAlert(Context context) {
        AlarmManager am = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(
                context, 0, new Intent(ALARM_ALERT_ACTION),
                PendingIntent.FLAG_UPDATE_CURRENT);
        // Gionee <baorui><2013-07-12> modify for CR00835747 begin
        /*
        am.cancel(sender);
        // Gionee baorui 2012-12-13 modify for CR00742863 begin
        am.cancelPoweroffAlarm(context.getPackageName());
        // Gionee baorui 2012-12-13 modify for CR00742863 end
        */
        am.cancelPoweroffAlarm(context.getPackageName());
        am.cancel(sender);
        // Gionee <baorui><2013-07-12> modify for CR00835747 end
        setStatusBarIcon(context, false);
        saveNextAlarm(context, "");
    }

    static void saveSnoozeAlert(final Context context, final int id,
            final long time) {
        SharedPreferences prefs = context.getSharedPreferences(
                AlarmClock.PREFERENCES, 0);
        if (id == INVALID_ALARM_ID) {
            clearAllSnoozePreferences(context, prefs);
        } else {
            final Set<String> snoozedIds =
                    prefs.getStringSet(PREF_SNOOZE_IDS, new HashSet<String>());
			// Gionee baorui 2012-09-18 modify for CR00693305 begin
			final Alarm a = getAlarm(context.getContentResolver(), id);
			if (a == null) {
				setNextAlert(context);
				return;
			}
			// Gionee baorui 2012-09-18 modify for CR00693305 end
            snoozedIds.add(Integer.toString(id));
            final SharedPreferences.Editor ed = prefs.edit();
            ed.putStringSet(PREF_SNOOZE_IDS, snoozedIds);
            ed.putLong(getAlarmPrefSnoozeTimeKey(id), time);
            ed.apply();
        }
        // Set the next alert after updating the snooze.
        setNextAlert(context);
    }

    private static String getAlarmPrefSnoozeTimeKey(int id) {
        return getAlarmPrefSnoozeTimeKey(Integer.toString(id));
    }

    private static String getAlarmPrefSnoozeTimeKey(String id) {
        return PREF_SNOOZE_TIME + id;
    }

    /**
     * Disable the snooze alert if the given id matches the snooze id.
     */
    static void disableSnoozeAlert(final Context context, final int id) {
        SharedPreferences prefs = context.getSharedPreferences(
                AlarmClock.PREFERENCES, 0);
        if (hasAlarmBeenSnoozed(prefs, id)) {
            // This is the same id so clear the shared prefs.
            clearSnoozePreference(context, prefs, id);
        }
    }

    // Helper to remove the snooze preference. Do not use clear because that
    // will erase the clock preferences. Also clear the snooze notification in
    // the window shade.
    private static void clearSnoozePreference(final Context context,
            final SharedPreferences prefs, final int id) {
        final String alarmStr = Integer.toString(id);
        final Set<String> snoozedIds =
                prefs.getStringSet(PREF_SNOOZE_IDS, new HashSet<String>());
        if (snoozedIds.contains(alarmStr)) {
            NotificationManager nm = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(id);
        }

        final SharedPreferences.Editor ed = prefs.edit();
        snoozedIds.remove(alarmStr);
        ed.putStringSet(PREF_SNOOZE_IDS, snoozedIds);
        ed.remove(getAlarmPrefSnoozeTimeKey(alarmStr));
        ed.apply();
    }

    private static void clearAllSnoozePreferences(final Context context,
            final SharedPreferences prefs) {
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Set<String> snoozedIds =
                prefs.getStringSet(PREF_SNOOZE_IDS, new HashSet<String>());
        final SharedPreferences.Editor ed = prefs.edit();
        for (String snoozeId : snoozedIds) {
            nm.cancel(Integer.parseInt(snoozeId));
            ed.remove(getAlarmPrefSnoozeTimeKey(snoozeId));
        }

        ed.remove(PREF_SNOOZE_IDS);
        ed.apply();
    }

    private static boolean hasAlarmBeenSnoozed(final SharedPreferences prefs, final int alarmId) {
        final Set<String> snoozedIds = prefs.getStringSet(PREF_SNOOZE_IDS, null);

        // Return true if there a valid snoozed alarmId was saved
        return snoozedIds != null && snoozedIds.contains(Integer.toString(alarmId));
    }

    /**
     * Updates the specified Alarm with the additional snooze time.
     * Returns a boolean indicating whether the alarm was updated.
     */
    private static boolean updateAlarmTimeForSnooze(
            final SharedPreferences prefs, final Alarm alarm) {
        if (!hasAlarmBeenSnoozed(prefs, alarm.id)) {
            // No need to modify the alarm
            return false;
        }

        final long time = prefs.getLong(getAlarmPrefSnoozeTimeKey(alarm.id), -1);
        // The time in the database is either 0 (repeating) or a specific time
        // for a non-repeating alarm. Update this value so the AlarmReceiver
        // has the right time to compare.
        alarm.time = time;

        return true;
    }

    /**
     * Tells the StatusBar whether the alarm is enabled or disabled
     */
    private static void setStatusBarIcon(Context context, boolean enabled) {
        Intent alarmChanged = new Intent("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
        alarmChanged.putExtra("alarmSet", enabled);
        context.sendBroadcast(alarmChanged);
    }

    private static long calculateAlarm(Alarm alarm) {
        return calculateAlarm(alarm.hour, alarm.minutes, alarm.daysOfWeek)
                .getTimeInMillis();
    }

    /**
     * Given an alarm in hours and minutes, return a time suitable for
     * setting in AlarmManager.
     */
    static Calendar calculateAlarm(int hour, int minute,
            Alarm.DaysOfWeek daysOfWeek) {

        // start with now
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);

        // if alarm is behind current time, advance one day
        if (hour < nowHour  ||
            hour == nowHour && minute <= nowMinute) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        int addDays = daysOfWeek.getNextAlarm(c);
        if (addDays > 0) c.add(Calendar.DAY_OF_WEEK, addDays);
        return c;
    }
    
    /**
     * Given an alarm in hours and minutes, return a time suitable for
     * setting in AlarmManager.
     */
    static Calendar calculateAlarm(int hour, int minute,
            Alarm.DaysOfWeek daysOfWeek, int id) {

        // start with now
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);

        // if alarm is behind current time, advance one day
        if (hour < nowHour  ||
            hour == nowHour && minute <= nowMinute) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        int addDays = daysOfWeek.getNextAlarm(c, id);
        if (addDays > 0) c.add(Calendar.DAY_OF_WEEK, addDays);
        return c;
    }

    static String formatTime(final Context context, int hour, int minute,
                             Alarm.DaysOfWeek daysOfWeek) {
        Calendar c = calculateAlarm(hour, minute, daysOfWeek);
        return formatTime(context, c);
    }

    /* used by AlarmAlert */
    static String formatTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? M24 : M12;
        return (c == null) ? "" : (String)DateFormat.format(format, c);
    }

    /**
     * Shows day and time -- used for lock screen
     */
    private static String formatDayAndTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? DM24 : DM12;
        return (c == null) ? "" : (String)DateFormat.format(format, c);
    }

    /**
     * Save time of the next alarm, as a formatted string, into the system
     * settings so those who care can make use of it.
     */
    static void saveNextAlarm(final Context context, String timeString) {
        Settings.System.putString(context.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED, timeString);
    }

    /**
     * @return true if clock is set to 24-hour mode
     */
    static boolean get24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }
    
    //android:hxc start
    
    /**
     * Whether this boot is from power off alarm or schedule power on or normal boot.
     *  
     * @param context
     * @return
     */
    static boolean bootFromPoweroffAlarm() {
    	String bootReason = SystemProperties.get("sys.boot.reason");
		boolean ret = (bootReason != null && bootReason.equals("1")) ? true : false;
		Log.v("bootFromPoweroffAlarm ret is " + ret);
		return ret;
    }
    
    /**
     * Store the nearest alarm into preference.
     * 
     * @param alarm Alarm to stored.
     * @param context context.
     */
    private static void storeNearestAlarm(final Context context, final Alarm alarm) {        
        if (alarm.id == -1) {
            return;
        } else {
        	SharedPreferences prefs = context.getSharedPreferences(
                    AlarmClockFragment.NEAREST_ALARM_PREFERENCES, 0);
            SharedPreferences.Editor ed = prefs.edit();
            ed.putInt(PREF_NEAREST_ALARM_ID, alarm.id);
            ed.putLong(PREF_NEAREST_ALARM_TIME, alarm.time);
            ed.apply();
        }
    }
    /**
     * Get the nearest alarm from preference file.
     * 
     * @param context
     * @return the nearest alarm object, if not set, return null.
     */
    public static Alarm getNearestAlarm(final Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                AlarmClockFragment.NEAREST_ALARM_PREFERENCES, 0);
        int alarmId = prefs.getInt(PREF_NEAREST_ALARM_ID, -1);
        
        if (alarmId == -1) {
        	return null;
        }
        
        ContentResolver cr = context.getContentResolver();
        return Alarms.getAlarm(cr, alarmId);
    }
    
    /**
     * Get the alert time of the nearest alarm.
     * 
     * @param context
     * @return the nearest alarm alert time, if not set, return -1.
     */
    public static long getNearestAlarmTime(final Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                AlarmClockFragment.NEAREST_ALARM_PREFERENCES, 0);
        return prefs.getLong(PREF_NEAREST_ALARM_TIME, -1);
    }
	
    /**
     * Get a formatted string for the given time.
     * 
     * @param now
     * @return
     */
    private static String getTimeString(long now) {
    	Time time = new Time();
        time.set(now);
        return (time.format("%b %d %I:%M:%S %p"));
    }
	
	
	 /**
     * Clear all snoozed alarms but do not setNextAlert.
     * 
     * @param context
     */
    static void disableAllSnoozedAlarms(final Context context) {
    	SharedPreferences prefs = context.getSharedPreferences(
                AlarmClock.PREFERENCES, 0);
        clearAllSnoozePreferences(context, prefs);
    }
    
    /**
     * Reset the alarm time to 0 when TIME_SET or TIMEZONE_CHANGED.
     * 
     * @param context
     */
    static void resetAlarmTimes(Context context) {
        final Cursor cursor = getFilteredAlarmsCursor(context.getContentResolver());
        if (cursor != null) {
            ContentResolver resolver = context.getContentResolver();

            ContentValues values = new ContentValues();
            values.put(Alarm.Columns.ALARM_TIME, 0);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        final Alarm alarm = new Alarm(cursor);
                        if (alarm.time != 0) {
                            resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI,
                                    alarm.id), values, null, null);
                        }
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
    }
    
    //add by zhanjiandong 2014.12.18
    public static void saveLastSelectRingtonUri(String uri,Context context){
    	SharedPreferences prefs = context.getSharedPreferences("AlarmAlertUri", 0);
    	prefs.edit().putString("ringtoneUri", uri).apply();
    }
    
	static Uri getAlarmAlertUri(Context context) {
		try {
			SharedPreferences prefs = context.getSharedPreferences(
					"AlarmAlertUri", 0);
			if(prefs == null){
				return null;
			}
			String alarmAlertUri = prefs.getString("ringtoneUri",
					RingtoneManager.getActualDefaultRingtoneUri(context,
							RingtoneManager.TYPE_ALARM).toString());
			Uri alert = null;
			if (alarmAlertUri == null) {
				return null;
			}
			if ("silent".equals(alarmAlertUri)) {
				alert = null;
			} else {
				alert = Uri.parse(alarmAlertUri);
                // Gionee <baorui><2013-07-12> modify for CR00835747 begin
                /*
                if (RingtoneManager.getRingtone(context, alert) == null) {
                	alert = RingtoneManager.getActualDefaultRingtoneUri(
                			context, RingtoneManager.TYPE_ALARM);
                	Log.v("Ringtone is null then set alert to the default alert");
                }
                */
                if (mIsGnMtkPoweroffAlarmSupport) {
                    if (RingtoneManager.getRingtone(context, alert) == null) {
                        alert = RingtoneManager.getActualDefaultRingtoneUri(context,
                                RingtoneManager.TYPE_ALARM);
                        Log.v("Ringtone is null then set alert to the default alert");
                    }
                } else {
                    if (!GnRingtoneUtil.isRingtoneExist(alert, context.getContentResolver())) {
                        alert = RingtoneManager.getActualDefaultRingtoneUri(context,
                                RingtoneManager.TYPE_ALARM);
                        Log.v("Ringtone is null then set alert to the default alert");
                    }
                }
                // Gionee <baorui><2013-07-12> modify for CR00835747 end
			}
			Log.v("getAlarmAlertUri alert = " + alert);
			return alert;
		} catch (NullPointerException e) {
			Log.v("getAlarmAlertUri NullPointerException");
			return null;
		}
	}
	
    //android:hxc end
    // Gionee <baorui><2013-04-23> modify for CR00799490 begin
    protected static Alarm gnCalculateNextAlert(final Context context) {
        long minTime = Long.MAX_VALUE;
        long now = System.currentTimeMillis();
        final SharedPreferences prefs = context.getSharedPreferences(AlarmClock.PREFERENCES, 0);

        Set<Alarm> alarms = new HashSet<Alarm>();

        // We need to to build the list of alarms from both the snoozed list and the scheduled
        // list. For a non-repeating alarm, when it goes of, it becomes disabled. A snoozed
        // non-repeating alarm is not in the active list in the database.

        // first go through the snoozed alarms
        final Set<String> snoozedIds = prefs.getStringSet(PREF_SNOOZE_IDS, new HashSet<String>());
        for (String snoozedAlarm : snoozedIds) {
            final int alarmId = Integer.parseInt(snoozedAlarm);
            final Alarm a = getAlarm(context.getContentResolver(), alarmId);
            alarms.add(a);
        }

        // Now add the scheduled alarms
        // add by jiating remove final
        Cursor cursor = getFilteredAlarmsCursor(context.getContentResolver());
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        final Alarm a = new Alarm(cursor);
                        alarms.add(a);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }

        Alarm alarm = null;

        for (Alarm a : alarms) {
            // A time of 0 indicates this is a repeating alarm, so
            // calculate the time to get the next alert.
            // androide jiating 20120618 update when local time change alarm
            // which never alart will recalculate alarmTime begin
            // if (a.time == 0) {

            if (a.time == 0) {
                a.time = calculateAlarm(a);
            }

            // a.time = calculateAlarm(a);
            Log.v("jiating...gnCalculateNextAlert.....a1=" + a.time);
            // }
            // androide jiating 20120618 update when local time change alarm
            // which never alart will recalculate alarmTime end

            // Update the alarm if it has been snoozed
            updateAlarmTimeForSnooze(prefs, a);

            if (a.time < now) {

                // Expired alarm, disable it and move along.
                enableAlarmInternal(context, a, false);
                continue;
            }
            if (a.time < minTime) {
                minTime = a.time;
                alarm = a;
            }
        }

        return alarm;
    }
    // Gionee <baorui><2013-04-23> modify for CR00799490 end

    // Gionee <baorui><2013-05-04> modify for CR00803588 begin
    public static String getExternalUriData(Context context, Uri uri) {
        if (null == uri || uri.toString().equals(ALARM_ALERT_SILENT)) {
            return null;
        }

        if (RingtoneManager.isDefault(uri)) {
            // Gionee <baorui><2013-05-22> modify for CR00818396 begin
            String mUri = Settings.System.getString(context.getContentResolver(), Settings.System.ALARM_ALERT);
            if (mUri != null && mUri.length() != 0) {
                uri = Uri.parse(mUri);
            } else {
                return null;
            }
            // Gionee <baorui><2013-05-22> modify for CR00818396 end
        }

        String data = null;
        Cursor cursor = null;
        try {
        	cursor = context.getContentResolver().query(uri, new String[] {MediaStore.Audio.Media.DATA},
                    null, null, null);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
        
        try {
            if (null != cursor && cursor.moveToFirst()) {
                data = cursor.getString(0);
            }
        } finally {
            if (null != cursor) {
                cursor.close();
                cursor = null;
            }
        }
        Log.v("getExternalUriData for " + uri.toString() + " with data: " + data);
        return data;
    }

    private static ContentValues gnCreateContentValues(Context context, Alarm alarm) {
        ContentValues values = new ContentValues(2);
        values.put("_data", getExternalUriData(context, alarm.alert));

        // A null alert Uri indicates a silent alarm.
        values.put(Alarm.Columns.ALERT, alarm.alert == null ? ALARM_ALERT_SILENT : alarm.alert.toString());
        Log.e("----getVolumes(context) = " + getVolumes(context));
        values.put("volumes", getVolumes(context));
        return values;
    }

    public static Uri updateRintoneUri(String dataKey, Uri oldUri, Context context, int volumes) {
        if ((null != oldUri)) {
            if (RingtoneManager.isDefault(oldUri)) { // Never happen
                oldUri = Uri.parse(Settings.System.getString(context.getContentResolver(),
                        Settings.System.ALARM_ALERT));
            }
            if (oldUri.toString().startsWith(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())) {
                Uri newUri = null;
                String newUriData = dataKey;
                if (getVolumes(context) != 0 && volumes != getVolumes(context)) {
                    if (volumes == 1) {
                        if (dataKey.contains("sdcard0")) {
                            newUriData = newUriData.replaceAll("sdcard0", "sdcard1");
                        }
                    } else if (volumes == 2) {
                        if (dataKey.contains("sdcard1")) {
                            newUriData = newUriData.replaceAll("sdcard1", "sdcard0");
                        }
                    } else {
                        return null;
                    }
                }

                // If we can query this ringtone in new database, replace old uri with new one,
                // otherwise set to default ringtone
                if (newUriData != null) {
                    Cursor cursor = context.getContentResolver().query(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            new String[] {MediaStore.Audio.Media._ID}, MediaStore.Audio.Media.DATA + "=?",
                            new String[] {newUriData}, null);

                    try {
                        if (null != cursor && cursor.moveToFirst()) {
                            long id = cursor.getLong(0);
                            newUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    id);
                            Log.v("Update ringtone uri for " + dataKey + " with new uri: " + newUri);
                            return newUri;
                        } else {
                            newUri = null;
                        }
                    } finally {
                        if (null != cursor) {
                            cursor.close();
                            cursor = null;
                        }
                    }
                    Log.v("Update ringtone uri for " + dataKey + " with default uri: " + newUri);
                    return newUri;
                }
            }
        }
        return null;
    }

    public static String getAlertInfoStr(Context context, int alarmId) {
        String mStr = null;
        Cursor cursor = context.getContentResolver().query(
                ContentUris.withAppendedId(Alarm.Columns.ALERTINFO_URI, alarmId),
                new String[] {Alarm.Columns.DATA}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                mStr = cursor.getString(0);
            }
            cursor.close();
        }
        return mStr;
    }

    public static int getVolumes(Context context, int alarmId) {
        int mInt = 0;
        Cursor cursor = context.getContentResolver().query(
                ContentUris.withAppendedId(Alarm.Columns.ALERTINFO_URI, alarmId),
                new String[] {Alarm.Columns.VOLUMES}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                mInt = cursor.getInt(0);
            }
            cursor.close();
        }
        return mInt;
    }

    public static boolean isUpdateRintoneUri(String dataKey, Uri oldUri, Context context, int volumes) {
        if ((null != oldUri)) {
            if (RingtoneManager.isDefault(oldUri)) { // Never happen
                oldUri = Uri.parse(Settings.System.getString(context.getContentResolver(),
                        Settings.System.ALARM_ALERT));
            }
            if (oldUri.toString().startsWith(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())) {
                Uri newUri = null;
                String newUriData = dataKey;
                if (getVolumes(context) != 0 && volumes != getVolumes(context)) {
                    if (volumes == 1) {
                        if (dataKey.contains("sdcard0")) {
                            newUriData = newUriData.replaceAll("sdcard0", "sdcard1");
                        }
                    } else if (volumes == 2) {
                        if (dataKey.contains("sdcard1")) {
                            newUriData = newUriData.replaceAll("sdcard1", "sdcard0");
                        }
                    } else {
                        return false;
                    }
                }

                // If we can query this ringtone in new database, replace old uri with new one,
                // otherwise set to default ringtone
                if (newUriData != null) {
                    Cursor cursor = context.getContentResolver().query(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            new String[] {MediaStore.Audio.Media._ID}, MediaStore.Audio.Media.DATA + "=?",
                            new String[] {newUriData}, null);

                    try {
                        if (null != cursor && cursor.moveToFirst()) {
                            long id = cursor.getLong(0);
                            newUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    id);
                            Log.v("Update ringtone uri for " + dataKey + " with new uri: " + newUri);
                            return true;
                        } else {
                            newUri = null;
                        }
                    } finally {
                        if (null != cursor) {
                            cursor.close();
                            cursor = null;
                        }
                    }
                    Log.v("Update ringtone uri for " + dataKey + " with default uri: " + newUri);
                    return (newUri == null ? false : true);
                }
            }
        }
        return false;
    }
    
    public static int getVolumes(Context context) {
        if (null == mStorageManager) {
            mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        }
        /*
        if (!mStorageManager.getVolumeState("/mnt/sdcard").equals(Environment.MEDIA_MOUNTED)
                && !mStorageManager.getVolumeState("/mnt/sdcard2").equals(Environment.MEDIA_MOUNTED)) {
            return 0;
        } else if (mStorageManager.getVolumeState("/mnt/sdcard").equals(Environment.MEDIA_MOUNTED)
                && mStorageManager.getVolumeState("/mnt/sdcard2").equals(Environment.MEDIA_MOUNTED)) {
            return 2;
        } else {
            return 1;
        }
        */
        return 1;
    }
    // Gionee <baorui><2013-05-04> modify for CR00803588 end
    
}
