/** 
 * Copyright (c) 2012 Qualcomm Technologies, Inc.  All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.gionee.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CalendarContract.SyncState;
import android.util.Log;

import java.util.Calendar;

public class Utility {
    // Please do not check in it as true.
    public static final boolean DEBUG = false;
    private static final String TAG = "Utility";

    // The default calendar package name
    public static final String CALENDAR_PACKAGE_NAME = "com.android.calendar";
    public static final String CALENDAR_ALLINONEACTIVITY = "com.android.calendar.AllInOneActivity";
    public static final String CALENDAR_EDITEVENTACTIVITY = "com.android.calendar.EditEventActivity";

    /**
     * The command for the buttons. And we will deal with this command in {@link CalendarWidget}.
     * So we need add these to AndroidManifest.xml which make the CalendarWidget could handle them.
     */
    public static final String COMMAND_GOTO_TODAY = "com.qualcomm.calendarwidget.goto_today";
    public static final String COMMAND_DATE_CHANGED = "com.qualcomm.calendarwidget.date_changed";
    public static final String COMMAND_REFRESH = "com.qualcomm.calendarwidget.refresh";

    // Gionee <lilg><2013-04-22> modify for Gionee widget begin
    public static final String COMMAND_GOTO_LAST_MONTH = "com.qualcomm.calendarwidget.goto_last_month";
    public static final String COMMAND_GOTO_NEXT_MONTH = "com.qualcomm.calendarwidget.goto_next_month";
    public static final String COMMAND_GOTO_TARGET_MONTH = "com.qualcomm.calendarwidget.goto_target_month";
    public static final String COMMAND_GOTO_CURRENT_DAY = "com.qualcomm.calendarwidget.goto_current_day";
    // Gionee <lilg><2013-04-22> modify for Gionee widget end
    
    public static final String KEY_EXTRA_DATE = "key_extra_date";
    public static final String KEY_EXTRA_YEAR = "key_extra_year";
    public static final String KEY_EXTRA_MONTH = "key_extra_month";
    // Gionee <lilg><2013-05-03> modify for Gionee widget begin
    public static final String KEY_EXTRA_MILLIS = "key_extra_millis";
    public static final String KEY_EXTRA_VIEW_TYPE = "key_extra_view_type";
    // Gionee <lilg><2013-05-03> modify for Gionee widget end
    
    /**
     * Set which day of week as the first day of week.
     * For default, we set it as Monday, if you want the first day of week is Sunday, you could set
     * this value as {@link Calendar#SUNDAY}.
     */
    // Gionee <lilg><2013-04-23> modify for Gionee widget begin
//     public static final int FIRST_DAY_OF_WEEK = Calendar.MONDAY;
    public static final int FIRST_DAY_OF_WEEK = Calendar.SUNDAY;
    // Gionee <lilg><2013-04-23> modify for Gionee widget end
    
    // The rows and the columns of the grid view.
    public static final int DATE_VIEW_ROWS = 6;
    public static final int DATE_VIEW_COLUMNS = 7;

    /**
     * Get the Calendar sync account count.
     */
    public static int getAccountCount(Context context) {
    	if (Utility.DEBUG) {
            Log.i(TAG, "getAccountCount.");
        }
        int count = 0;
        Cursor cursor = context.getContentResolver().query(SyncState.CONTENT_URI, null, null, null, null);
        try {
            if (cursor != null) {
                count = cursor.getCount();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return count;
    }

    /**
     * Check if the calendar application enable or disable.
     * @return true if enable
     */
    public static boolean isCalendarEnabled(Context context) {
    	if (Utility.DEBUG) {
            Log.i(TAG, "isCalendarEnabled.");
        }
        PackageManager pm = context.getPackageManager();
        try {
            int state = pm.getApplicationEnabledSetting(Utility.CALENDAR_PACKAGE_NAME);
            if (DEBUG) {
                Log.d("CalendarWidget", "The Calendar application state is: " + state);
            }

            if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
                return false;
            }
        } catch (IllegalArgumentException e) {
            // If the package couldn't be found, we will catch this exception.
            Log.w("CalendarWidget", "ifCalendarEnabled? The calendar package couldn't be found by name.");
            return false;
        }
        return true;
    }
}
