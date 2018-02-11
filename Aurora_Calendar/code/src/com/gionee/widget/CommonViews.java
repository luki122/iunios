/** 
 * Copyright (c) 2013, Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.gionee.widget;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncInfo;
import android.content.SyncStatusObserver;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import com.android.calendar.AllInOneActivity;
import com.android.calendar.CalendarController;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;

import com.gionee.widget.CalendarWidget.CommonService;

import java.util.Date;

/**
 * This class is implements {@link RemoteViewsService.RemoteViewsFactory}, and it
 * provide the views for {@link CalendarWidget.CommonService}. It will provide the
 * weeks' view according to the position {@link #getViewAt(int)}, and bind the common
 * views as added event button and so on.
 */
public class CommonViews implements RemoteViewsService.RemoteViewsFactory, SyncStatusObserver,
        OnAccountsUpdateListener {
    private static final String TAG = "CommonViews";

    private static CommonViews sInstance;

    private Context mContext;
    private AppWidgetManager mAppWidgetManager;
    private int[] mAppWidgetIds;
    private Resources mRes;

    private int mYear;
    private int mMonth;
    private RemoteViews mViews;

    private Object mObserverHandle = null;

    /**
     * We use the following convention for our add event commands:
     *     widget://command/add_event
     */
    private static final String MIME_TYPE = "com.qualcomm.calendarwidget/widget_data/add_event";
    private static final String ADD_EVENT = "add_event";
    private static final Uri COMMAND_URI = Uri.parse("widget://command/add_event");
    
    // Gionee <lilg><2013-04-23> modify for Gionee widget begin
    private SimpleDateFormat monthFormat = null;
    // Gionee <lilg><2013-04-23> modify for Gionee widget end
    
    // Gionee <lilg><2013-04-23> modify begin
    private int firstDayOfWeek = Utility.FIRST_DAY_OF_WEEK;
    // Gionee <lilg><2013-04-23> modify begin
    
    // Gionee <lilg><2013-04-23> add begin
    public SimpleDateFormat mHourFormat;
    public SimpleDateFormat mMinuteFormat;
    private int mHour;
    private int mMinute;
    // Gionee <lilg><2013-04-23> add end
    
    public CommonViews(Context context, int[] appWidgetIds) {
        sInstance = this;
        mContext = context;
        mAppWidgetManager = AppWidgetManager.getInstance(mContext);
        mAppWidgetIds = appWidgetIds;
        mRes = context.getResources();
        // Gionee <lilg><2013-04-23> modify for Gionee widget begin
        monthFormat = new SimpleDateFormat(".MM", Locale.CHINA);
        // Gionee <lilg><2013-04-23> modify for Gionee widget end
        
        // Gionee <lilg><2013-04-23> add begin
        mHourFormat = new SimpleDateFormat("HH");
        mMinuteFormat = new SimpleDateFormat("mm");
        // Gionee <lilg><2013-04-23> add begin
    }

    public static synchronized CommonViews getOrCreateViews(Context context, int[] appWidgetIds) {
        if (sInstance != null) {
            return sInstance;
        } else {
            return new CommonViews(context, appWidgetIds);
        }
    }

    public static void update(Context context, int[] appWidgetIds, RemoteViews views, int year, int month) {
    	if (Utility.DEBUG) {
            Log.i(TAG, "CommonViews update.");
        }
        CommonViews cv = getOrCreateViews(context, appWidgetIds);
        cv.start(views, appWidgetIds, year, month);
    }

    /**
     * To handle the intent which is created by this.
     * And this will called by {@link CalendarWidget.CommonService}.
     */
    public static boolean processIntent(Context context, Intent intent) {
    	if (Utility.DEBUG) {
            Log.i(TAG, "CommonViews processIntent.");
        }
    	
    	// Gionee <lilg><2013-04-22> modify for Gionee widget begin
    	if (intent != null && intent.getAction() != null && intent.getAction().equals(Utility.COMMAND_GOTO_LAST_MONTH)){
    		if (Utility.DEBUG) {
    			Log.i(TAG, "COMMAND_GOTO_LAST_MONTH.");
        	}

    		int currentYear = intent.getIntExtra(Utility.KEY_EXTRA_YEAR, -1);
    		int currentMonth = intent.getIntExtra(Utility.KEY_EXTRA_MONTH, -1);
    		if (Utility.DEBUG) {
    			Log.d(TAG, "current year: " + currentYear + ", current month: " + currentMonth);
        	}
    		
    		if(currentYear != -1 && currentMonth != -1){
        		int toYear = -1;
        		int toMonth = -1;
        		if(currentMonth <= 0){
        			toMonth = 11;
        			toYear = currentYear - 1;
        		}else{
        			toMonth = currentMonth - 1;
        			toYear = currentYear;
        		}
        		if (Utility.DEBUG) {
        			Log.d(TAG, "toYear: " + toYear + ", toMonth: " + toMonth);
            	}
        		if(toYear != -1 && toMonth != -1){
        			WidgetManager.setDate(context, toYear, toMonth);
        		}
        	}
    	}else if (intent != null && intent.getAction() != null && intent.getAction().equals(Utility.COMMAND_GOTO_NEXT_MONTH)) {
    		if (Utility.DEBUG) {
    			Log.i(TAG, "COMMAND_GOTO_NEXT_MONTH");
        	}
    		
    		int currentYear = intent.getIntExtra(Utility.KEY_EXTRA_YEAR, -1);
    		int currentMonth = intent.getIntExtra(Utility.KEY_EXTRA_MONTH, -1);
    		if (Utility.DEBUG) {
    			Log.d(TAG, "current year: " + currentYear + ", current month: " + currentMonth);
        	}

    		if(currentYear != -1 && currentMonth != -1){
        		int toYear = -1;
        		int toMonth = -1;
        		if(currentMonth >= 11){
        			toMonth = 0;
        			toYear = currentYear + 1;
        		}else{
        			toMonth = currentMonth + 1;
        			toYear = currentYear;
        		}
        		if (Utility.DEBUG) {
        			Log.d(TAG, "toYear: " + toYear + ", toMonth: " + toMonth);
            	}
        		WidgetManager.setDate(context, toYear, toMonth);
        	}
    	}else{
    		if (Utility.DEBUG) {
    			Log.i(TAG, "COMMAND_ADD_EVENT");
        	}
	        final Uri data = intent.getData();
	        if (data == null) return false;
	
	        List<String> pathSegments = data.getPathSegments();
	        String command = pathSegments.get(0);
	        if (ADD_EVENT.equals(command)) {
	            try {
	                Intent addEvent = new Intent(Intent.ACTION_EDIT);
	                addEvent.setClassName(Utility.CALENDAR_PACKAGE_NAME, Utility.CALENDAR_EDITEVENTACTIVITY);
	                addEvent.setData(Events.CONTENT_URI);
	                addEvent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                context.startActivity(addEvent);
	                return true;
	            } catch (ActivityNotFoundException e) {
	                Log.w(TAG, "Catch the ActivityNotFoundException, return true.");
	                if (!Utility.isCalendarEnabled(context)) {
	                    Toast.makeText(context, R.string.no_target_activity, Toast.LENGTH_LONG).show();
	                }
	                return true;
	            }
	        }
    	}
    	// Gionee <lilg><2013-04-22> modify for Gionee widget end
    	
        return false;
    }

    /**
     * Start to update the views.
     */
    private void start(RemoteViews views, int[] appWidgetIds, int year, int month) {
    	if (Utility.DEBUG) {
			Log.i(TAG, "CommonViews start.");
			Log.d(TAG, "year: " + year + ", month: " + month);
    	}
        mViews = views;
        mAppWidgetIds = appWidgetIds;
        mYear = year;
        mMonth = month;
        
        // Gionee <lilg><2013-06-08> midify begin
        firstDayOfWeek = Utils.getFirstDayOfWeek(mContext) + 1;
        
        mHour = Integer.parseInt(mHourFormat.format(new Date()));
        mMinute = Integer.parseInt(mMinuteFormat.format(new Date()));
        // Gionee <lilg><2013-06-08> midify end

        // update the views.
        updateHeader(false);
        updateWeekTitle();
    }

    /**
     * Update the common views, such as goto_today, choose_date, add_event and request_sync.
     * And update the intent for these views.
     *
     * @param syncActive the sync status, true if the sync for calendar authority is active.
     */
    private void updateHeader(boolean syncActive) {
    	if (Utility.DEBUG) {
			Log.i(TAG, "CommonViews updateHeader.");
    	}
    	
    	if (mViews == null) return;

    	// Gionee <lilg><2013-04-22> modify for Gionee widget begin

    	// Set the goto last month intent.
    	Intent gotoLastMonth = new Intent(Utility.COMMAND_GOTO_LAST_MONTH);
    	gotoLastMonth.setClass(mContext, CommonService.class);
    	gotoLastMonth.putExtra(Utility.KEY_EXTRA_YEAR, mYear);
    	gotoLastMonth.putExtra(Utility.KEY_EXTRA_MONTH, mMonth);
    	mViews.setOnClickPendingIntent(R.id.widget_goto_last_month, PendingIntent.getService(mContext, 0, gotoLastMonth, PendingIntent.FLAG_UPDATE_CURRENT));
    	
    	// Set the goto last month intent.
    	Intent gotoNextMonth = new Intent(Utility.COMMAND_GOTO_NEXT_MONTH);
    	gotoNextMonth.setClass(mContext, CommonService.class);
    	gotoNextMonth.putExtra(Utility.KEY_EXTRA_YEAR, mYear);
    	gotoNextMonth.putExtra(Utility.KEY_EXTRA_MONTH, mMonth);
    	mViews.setOnClickPendingIntent(R.id.widget_goto_next_month, PendingIntent.getService(mContext, 0, gotoNextMonth, PendingIntent.FLAG_UPDATE_CURRENT));
    	
    	// Set the goto the target month intent.
    	// Gionee <lilg><2013-05-02> modify for gionee widget begin
    	Time time = new Time(Utils.getTimeZone(mContext, null));
    	// Gionee <lilg><2013-06-08> modify begin
    	time.set(0, mMinute, mHour, 1, mMonth, mYear);
    	Log.d(TAG, "minute: " + mMinute + ", hour: " + mHour);
    	// Gionee <lilg><2013-06-08> modify end
        long millis = time.toMillis(true);
        
		Intent gotoTargetMonth = new Intent(Intent.ACTION_VIEW);
		gotoTargetMonth.setClass(mContext, AllInOneActivity.class);
    	gotoTargetMonth.setData(Uri.parse("content://com.android.calendar/time/" + millis));
    	gotoTargetMonth.putExtra(Utility.KEY_EXTRA_VIEW_TYPE, ViewType.MONTH);
    	mViews.setOnClickPendingIntent(R.id.widget_layout_year_and_month, PendingIntent.getActivity(mContext, 0, gotoTargetMonth, 0));
        // Gionee <lilg><2013-05-02> modify for gionee widget end
        
    	// Update the year and month info which in the header.
    	mViews.setTextViewText(R.id.widget_text_year, String.valueOf(mYear));
    	
    	Calendar cal = Calendar.getInstance();
    	cal.clear();
    	cal.set(Calendar.YEAR, mYear);
    	cal.set(Calendar.MONTH, mMonth);
    	String monthTitle = monthFormat.format(cal.getTime());
    	mViews.setTextViewText(R.id.widget_text_month, monthTitle);
    	// Gionee <lilg><2013-04-22> modify for Gionee widget end 

    	// Gionee <lilg><2013-04-22> modify for Gionee widget begin 

    	// Set the goto today intent. And we will deal with this command in CalendarWidget.
    	/*Intent gotoToday = new Intent(Utility.COMMAND_GOTO_TODAY);
        mViews.setOnClickPendingIntent(R.id.go_to_today,
                PendingIntent.getBroadcast(mContext, 0, gotoToday,
                        PendingIntent.FLAG_CANCEL_CURRENT));*/


    	// Set the date title intent. And we want to start the activity.
    	/*Intent chooseDate = new Intent();
        chooseDate.setClass(mContext, ChooseDateActivity.class);
        chooseDate.putExtra(ChooseDateActivity.EXTRA_YEAR, mYear);
        chooseDate.putExtra(ChooseDateActivity.EXTRA_MONTH, mMonth);
        mViews.setOnClickPendingIntent(R.id.choose_date,
                PendingIntent.getActivity(mContext, 0, chooseDate,
                        PendingIntent.FLAG_UPDATE_CURRENT));*/

    	// Update the date title text.
    	/*Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, mYear);
        calendar.set(Calendar.MONTH, mMonth);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        String dateTitle = DateUtils.formatDateTime(mContext, calendar.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_MONTH_DAY
                        | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_ABBREV_MONTH).toString();
        mViews.setTextViewText(R.id.choose_date, dateTitle);*/

    	// set today
    	/*Time today = new Time();
    	today.setToNow();
    	if (today.monthDay < 10) {
    		mViews.setTextViewText(R.id.today, " " + today.monthDay);
    	} else {
    		mViews.setTextViewText(R.id.today, String.valueOf(today.monthDay));
    	}*/

    	// Set the add event intent. And we want to go to calendar.
    	/*Intent addEvent = new Intent();
        addEvent.setClass(mContext, CommonService.class);
        addEvent.setDataAndType(COMMAND_URI, MIME_TYPE);
        mViews.setOnClickPendingIntent(R.id.add_event, PendingIntent.getService(mContext, 0, addEvent,
                PendingIntent.FLAG_UPDATE_CURRENT));*/

    	// Set the refresh intent. And we will try to start sync.
    	/*if (syncActive) {
            mViews.setViewVisibility(R.id.refreshing, View.VISIBLE);
            mViews.setViewVisibility(R.id.refresh, View.INVISIBLE);
        } else {
            mViews.setViewVisibility(R.id.refresh, View.VISIBLE);
            mViews.setViewVisibility(R.id.refreshing, View.INVISIBLE);
        }
        Intent refresh = new Intent(Utility.COMMAND_REFRESH);
        mViews.setOnClickPendingIntent(R.id.refresh,
                PendingIntent.getBroadcast(mContext, 0, refresh, PendingIntent.FLAG_CANCEL_CURRENT));*/

    	// Gionee <lilg><2013-04-22> modify for Gionee widget end

    }

    private void updateWeekTitle() {
    	if (Utility.DEBUG) {
			Log.i(TAG, "CommonViews updateWeekTitle.");
    	}
        if (mViews == null) return;

        mAppWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetIds, R.id.week_name);

        Intent intent = new Intent(mContext, CommonService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetIds);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        mViews.setRemoteAdapter(R.id.week_name, intent);
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.gn_widget_week_name);

        // Gionee <lilg><2013-06-08> midify begin
        //      int index = (position + Utility.FIRST_DAY_OF_WEEK) % 7;
        int index = (position + firstDayOfWeek) % 7;
        // Gionee <lilg><2013-06-08> midify end
        
        String[] weeks = mRes.getStringArray(R.array.gn_widget_weeks);
        views.setTextViewText(R.id.week, weeks[index]);

        // Gionee <lilg><2013-04-24> add for Gionee widget begin
        if(index == 0 || index == 1){
        	// saturday or sunday
        	views.setTextColor(R.id.week, mRes.getColor(R.color.widget_week_saturday_and_sunday_text_color));
        }else{
        	// else week
        	views.setTextColor(R.id.week, mRes.getColor(R.color.widget_week_monday_to_friday_text_color));
        }

        // the week of the current day, set the week color.
        // Gionee <lilg><2013-06-08> modify for update UI begin
        /*Calendar cal = Calendar.getInstance();
    	cal.clear();
    	cal.setTimeInMillis(System.currentTimeMillis());
        
		if (index == cal.get(Calendar.DAY_OF_WEEK) || (index == 0 && cal.get(Calendar.DAY_OF_WEEK) == 7)) {
			if(mYear == cal.get(Calendar.YEAR) && mMonth == cal.get(Calendar.MONDAY)){
				views.setTextColor(R.id.week, mRes.getColor(R.color.widget_week_current_week_text_color));
			}
		}*/ 
		// Gionee <lilg><2013-06-08> modify for update UI end
        // Gionee <lilg><2013-04-24> add for Gionee widget end
        
        if (Utility.DEBUG) {
            Log.d(TAG, "get the view at: " + position + ", the week is: " + weeks[index] + ", the index is: " + index);
        }
        return views;
    }

    @Override
    public void onCreate() {
    	if (Utility.DEBUG) {
            Log.i(TAG, "onCreate.");
        }
        /**
         *  We want to monitor all the type of status changed, so we must set the mask as
         *  {@link ContentResolver#SYNC_OBSERVER_TYPE_ALL}. But it is hide, so we set the
         *  mask value same as the {@link ContentResolver#SYNC_OBSERVER_TYPE_ALL}. Or we
         *  could set this application as platform.
         */
        mObserverHandle = ContentResolver.addStatusChangeListener(
                0x7fffffff/* SYNC_OBSERVER_TYPE_ALL */, this);
        // Gionee <lilg><2013-07-03> modify for CR00831140 begin
        // AccountManager.get(mContext).addOnAccountsUpdatedListener(this, null, true);
        // Gionee <lilg><2013-07-03> modify for CR00831140 end
    }

    @Override
    public void onDataSetChanged() {
    	if (Utility.DEBUG) {
            Log.i(TAG, "onDataSetChanged.");
        }
        // We are doing nothing in onDataSetChanged()
    }

    @Override
    public void onDestroy() {
    	if (Utility.DEBUG) {
            Log.i(TAG, "onDestroy.");
        }
        if (mObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mObserverHandle);
        }
        // Gionee <lilg><2013-07-03> modify for CR00831140 begin
        // AccountManager.get(mContext).removeOnAccountsUpdatedListener(this);
        // Gionee <lilg><2013-07-03> modify for CR00831140 end
    }

    @Override
    public int getCount() {
    	if (Utility.DEBUG) {
            Log.i(TAG, "getCount.");
        }
        // Caused by there are 7 days in one week. So we will return 7.
        return 7;
    }

    @Override
    public RemoteViews getLoadingView() {
    	if (Utility.DEBUG) {
            Log.i(TAG, "getLoadingView.");
        }
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.gn_widget_week_name);
        views.setTextViewText(R.id.week, null);
        return views;
    }

    @Override
    public int getViewTypeCount() {
    	if (Utility.DEBUG) {
            Log.i(TAG, "getViewTypeCount.");
        }
        return 1;
    }

    @Override
    public long getItemId(int position) {
    	if (Utility.DEBUG) {
            Log.d(TAG, "getItemId, position: " + position);
        }
        return position;
    }

    @Override
    public boolean hasStableIds() {
    	if (Utility.DEBUG) {
            Log.i(TAG, "hasStableIds.");
        }
        return false;
    }

    @Override
    public void onStatusChanged(int which) {
        if (Utility.DEBUG) {
            Log.d(TAG, "Handle the status changed, which:" + which);
        }

        if (mAppWidgetIds == null || mViews == null) return;

        if (which == ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE) {
            List<SyncInfo> syncs = ContentResolver.getCurrentSyncs();
            for (SyncInfo sync : syncs) {
                if (Utility.DEBUG) {
                    Log.d(TAG, "sync.authority: " + sync.authority);
                    Log.d(TAG, "sync.account: " + sync.account);
                }
                if (CalendarContract.CONTENT_URI.getAuthority().equals(sync.authority)) {
                    updateHeader(true);
                    mAppWidgetManager.updateAppWidget(mAppWidgetIds, mViews);
                }
            }
        } else {
            updateHeader(false);
            mAppWidgetManager.updateAppWidget(mAppWidgetIds, mViews);
        }
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        if (Utility.DEBUG) {
            Log.i(TAG, "Catch the account update message.");
        }
        if (mAppWidgetIds == null || mViews == null) return;

        if (Utility.getAccountCount(mContext) < 1) {
            updateHeader(false);
            mAppWidgetManager.updateAppWidget(mAppWidgetIds, mViews);
        }
    }

}
