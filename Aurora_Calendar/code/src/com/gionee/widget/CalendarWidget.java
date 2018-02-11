/** 
 * Copyright (c) 2012 Qualcomm Technologies, Inc.  All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

/*
** Copyright 2010 The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

package com.gionee.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.util.Log;
import android.widget.RemoteViewsService;

import com.gionee.legalholiday.HolidayData;
import com.gionee.legalholiday.ILegalHoliday;
import com.gionee.legalholiday.LegalHolidayUtils;

public class CalendarWidget extends AppWidgetProvider {
    private static final String TAG = "CalendarWidget";

    @Override
    public void onEnabled(Context context) {
        if (Utility.DEBUG) {
            Log.i(TAG, "Enable this widget.");
        }

        // If we enable this widget, we will set the date to today.
        WidgetManager.gotoToday();
        
        // Gionee <lilg><2013-06-03> add for init calendar holiday data begin 
        LegalHolidayUtils.initHolidayData(context);
        // Gionee <lilg><2013-06-03> add for init calendar holiday data end
        
        super.onEnabled(context);
    }
    
    @Override
    public void onDisabled(Context context) {
        if (Utility.DEBUG) {
            Log.i(TAG, "Disable this widget.");
        }

        // If we disable this widget, we need stop the services.
        context.stopService(new Intent(context, DateService.class));
        context.stopService(new Intent(context, CommonService.class));
        context.stopService(new Intent(context, WeekNumberService.class));
        super.onDisabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Utility.DEBUG) {
            Log.d(TAG, "receive the action:" + action);
        }

        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        int[] ids = awm.getAppWidgetIds(new ComponentName(context, CalendarWidget.class));
        if (Utility.COMMAND_DATE_CHANGED.equals(action) || Intent.ACTION_DATE_CHANGED.equals(action) || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            WidgetManager.updateWidgets(context, awm, ids);
        } else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
            // we need to go to today if we changed date manually.
            if (!WidgetManager.isToday()) {
                WidgetManager.gotoToday();
            }
            WidgetManager.updateWidgets(context, awm, ids);
        } else if (Utility.COMMAND_GOTO_TODAY.equals(action)) {
            if (!WidgetManager.isToday()) {
                WidgetManager.gotoToday();
                WidgetManager.updateWidgets(context, awm, ids);
            }
        } else if (Utility.COMMAND_REFRESH.equals(action)) {
            // we need to request sync.
            if (Utility.getAccountCount(context) > 0) {
                Bundle extras = new Bundle();
                extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                extras.putBoolean("metafeedonly", true);
                ContentResolver.requestSync(null /* all accounts */, Calendars.CONTENT_URI.getAuthority(), extras);
            } else {
                Intent noAccount = new Intent();
                noAccount.setClass(context, NoAccountAlert.class);
                noAccount.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(noAccount);
            }
        } else if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            if (Utility.CALENDAR_PACKAGE_NAME.equals(packageName)) {
                Log.d(TAG, "Receive the calendar application update action. Update the widget state.");
                // we need update the add event button's state.
                WidgetManager.updateWidgets(context, awm, ids);
            }
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	if (Utility.DEBUG) {
            Log.i(TAG, "onUpdate.");
        }
        WidgetManager.updateWidgets(context, appWidgetManager, appWidgetIds);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /**
     * We use the DateService for:
     *  1) To provide a widget factory for RemoteViews.
     *  2) Catch our command, and make the DateViews to process the intent.
     */
    public static class DateService extends RemoteViewsService {

        @Override
        public RemoteViewsFactory onGetViewFactory(Intent intent) {
        	if (Utility.DEBUG) {
                Log.i(TAG, "DateService, onGetViewFactory.");
            }
            int[] widgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_ID);
            if (widgetIds == null) return null;
            // When the system is low memory, the process may be killed and
            // dateViews will be recreate, but its status is original, it has no
            // date. So we get date from the intent and set them into widgets.
            Bundle date = intent.getBundleExtra(Utility.KEY_EXTRA_DATE);
            if (null != date) {
                int year = date.getInt(Utility.KEY_EXTRA_YEAR, -1);
                int month = date.getInt(Utility.KEY_EXTRA_MONTH, -1);
                if (Utility.DEBUG) {
                    Log.d(TAG, "year: " + year + ", month: " + month);
                }
                if (year >= 0 && month >= 0) {
                    // Set the date the widgets want to show.
                    WidgetManager.setDate(this, year, month);
                }
            }
            // Find the existing widget or create it
            return DateViews.getOrCreateViews(this, widgetIds);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
        	if (Utility.DEBUG) {
                Log.i(TAG, "DateService, onStartCommand.");
            }
        	
        	if (intent.getData() != null) {
        		// DateViews creates intents, so it knows how to handle them.
        		DateViews.processIntent(this, intent);
        	}
        	return Service.START_NOT_STICKY;
        }

    }

    public static class CommonService extends RemoteViewsService {

        @Override
        public RemoteViewsFactory onGetViewFactory(Intent intent) {
        	if (Utility.DEBUG) {
                Log.i(TAG, "CommonService, onGetViewFactory.");
            }
            int[] widgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_ID);
            if (widgetIds == null) return null;
            // Find the existing widget or create it
            return CommonViews.getOrCreateViews(this, widgetIds);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
        	if (Utility.DEBUG) {
                Log.i(TAG, "CommonService, onStartCommand.");
            }
        	// Gionee <lilg><2013-04-22> modify for Gionee widget begin
        	// if (intent.getData() != null) {
        	
        	// CommonViews creates intents, so it knows how to handle them.
        	CommonViews.processIntent(this, intent);

        	// }
        	// Gionee <lilg><2013-04-22> modify for Gionee widget end
        	return Service.START_NOT_STICKY;
        }

    }

    public static class WeekNumberService extends RemoteViewsService {

        @Override
        public RemoteViewsFactory onGetViewFactory(Intent intent) {
        	if (Utility.DEBUG) {
                Log.i(TAG, "WeekNumberService, onGetViewFactory.");
            }
            int[] widgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_ID);
            if (widgetIds == null) return null;
            // Find the existing widget or create it
            return WeekNumberViews.getOrCreateViews(this, widgetIds);
        }

    }
    
}
