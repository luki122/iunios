/** 
 * Copyright (c) 2012 Qualcomm Technologies, Inc.  All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.gionee.widget;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.Loader.OnLoadCompleteListener;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import com.android.calendar.AllInOneActivity;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.android.calendar.CalendarController.ViewType;

import com.android.lunar.ILunarService;

//import com.qrd.plugin.feature_query.FeatureQuery;
import com.gionee.legalholiday.ILegalHoliday;
import com.gionee.legalholiday.LegalHolidayUtils;
import com.gionee.widget.CalendarWidget.DateService;
import com.mediatek.calendar.lunar.LunarUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This class is implements {@link RemoteViewsService.RemoteViewsFactory}, and it
 * provide the views for {@link CalendarWidget.DateService}. It will provide the
 * date's view according to the position {@link #getViewAt(int)}.
 * And the date's view will display:
 *          the date {@link #bindBasicViewAt(RemoteViews, Calendar)}
 *          the lunar info {@link #bindLunarViewAt(RemoteViews, Calendar)}
 *          the event count {@link #bindEventViewAt(RemoteViews, Calendar)}
 *
 * We need listen the database change event and the account update event.
 */
public class DateViews implements RemoteViewsService.RemoteViewsFactory, OnAccountsUpdateListener {
    private static final String TAG = "DateViews";

    private static final int EVENT_LOADER = 0;
    private static final int LUNAR_LOADER = 1;

    private static final int FIELD_EVENT = 1;
    private static final int FIELD_LUNAR = 2;
    private static final int FIELD_ALL = 3;

    private static DateViews sInstance;

    private final Context mContext;
    private final AppWidgetManager mAppWidgetManager;

    private int[] mAppWidgetIds;
    private int mYear;
    private int mMonth;
    private RemoteViews mViews;
    private Resources mRes;

    private static final String EXTRA_KEY_VIEW = "VIEW";       // It is defined at Calendar(Utils.INTENT_KEY_VIEW_TYPE).
    private static final String EXTRA_VALUE_AGENDA = "AGENDA"; // It is matched defined at Calendar(ViewType.AGENDA).
    private static final String EXTRA_VALUE_MONTH = "MONTH";   // It is matched defined at Calendar(ViewType.MONTH).

    /**
     * We use the following convention for our commands:
     *     widget://command/<command>/<arg1>/<arg2>
     */
    private static final String MIME_TYPE = "com.qualcomm.calendarwidget/widget_data";

    private static final String VIEW_EVENT = "view_event";
    private static final String NEW_EVENT = "new_event";
    private static final String VIEW_CALENDAR_MONTH = "view_calendar_month";

    private static final Uri COMMAND_URI = Uri.parse("widget://command");
    private static final Uri COMMAND_URI_VIEW_EVENT = COMMAND_URI.buildUpon().appendPath(VIEW_EVENT).build();
    private static final Uri COMMAND_URI_NEW_EVENT = COMMAND_URI.buildUpon().appendPath(NEW_EVENT).build();
    private static final Uri COMMAND_URI_VIEW_CALENDAR_MONTH = COMMAND_URI.buildUpon()
            .appendPath(VIEW_CALENDAR_MONTH).build();

    // Gionee <lilg><2013-05-06> add for load lunal data begin
    private LunarUtil lunarUtil;
    // Gionee <lilg><2013-05-06> add for load lunal data end
    
    // Gionee <lilg><2013-04-23> modify begin
    private int firstDayOfWeek = Utility.FIRST_DAY_OF_WEEK;
    // Gionee <lilg><2013-04-23> modify begin
    
    // Gionee <lilg><2013-04-23> add begin
    public SimpleDateFormat mHourFormat;
    public SimpleDateFormat mMinuteFormat;
    private int mHour;
    private int mMinute;
    // Gionee <lilg><2013-04-23> add end
    
    // We use one handler to deal with the date view update event.
    private static final int MSG_NOTIFY_CHANGE = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	if (Utility.DEBUG) {
    			Log.i(TAG, "DateViews handleMessage.");
        	}
        	
        	// Gionee <lilg><2013-05-07> modify for gionee widget begin
        	
            /*if (msg.what == MSG_NOTIFY_CHANGE && mLunarLoadingFinished && mEventLoadingFinished) {
                // We will only update the date view if all the loading is finished.
                int[] ids = mAppWidgetManager.getAppWidgetIds(new ComponentName(mContext, CalendarWidget.class));
                mAppWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.date_view);
            }*/
            if (msg.what == MSG_NOTIFY_CHANGE && mEventLoadingFinished) {
            	if (Utility.DEBUG) {
        			Log.i(TAG, "notifyAppWidgetViewDataChanged.");
            	}
                // update the date view if the event loading is finished.
                int[] ids = mAppWidgetManager.getAppWidgetIds(new ComponentName(mContext, CalendarWidget.class));
                mAppWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.date_view);
            }
            
            // Gionee <lilg><2013-05-07> modify for gionee widget end
        }
    };

    // The widget's loader.
    private boolean mEventLoadingFinished;
    private final EventLoader mEventLoader;
    private EventLoader.EventCursor mEventCursor;
    private OnLoadCompleteListener<Cursor> mEventListener = new OnLoadCompleteListener<Cursor>() {

        @Override
        public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
            if (Utility.DEBUG) {
                Log.i(TAG, "event cursor load complete.");
            }

            mEventLoadingFinished = true;
            mEventCursor = (EventLoader.EventCursor) data;
            mHandler.sendEmptyMessage(MSG_NOTIFY_CHANGE);
        }
    };

    private boolean mLunarLoadingFinished;
    private final LunarLoader mLunarLoader;
    private LunarLoader.LunarCursor mLunarCursor;
    private OnLoadCompleteListener<Cursor> mLunarListener = new OnLoadCompleteListener<Cursor>() {

        @Override
        public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
            if (Utility.DEBUG) {
                Log.i(TAG, "lunar cursor load complete.");
            }

            mLunarLoadingFinished = true;
            mLunarCursor = (LunarLoader.LunarCursor) data;
            mHandler.sendEmptyMessage(MSG_NOTIFY_CHANGE);
        }

    };

    public DateViews(Context context, int[] appWidgetIds) {
        super();
        if (Utility.DEBUG) {
            Log.i(TAG, "Creating WidgetManager");
        }

        sInstance = this;
        mAppWidgetIds = appWidgetIds;

        mContext = context.getApplicationContext();
        mAppWidgetManager = AppWidgetManager.getInstance(mContext);
        mRes = mContext.getResources();

        mEventLoader = new EventLoader(mContext);
        mEventLoader.registerListener(EVENT_LOADER, mEventListener);
        mLunarLoader = new LunarLoader(mContext);
        mLunarLoader.registerListener(LUNAR_LOADER, mLunarListener);
        
        // Gionee <lilg><2013-05-06> add for load lunal data begin
        lunarUtil = LunarUtil.getInstance(mContext);
        // Gionee <lilg><2013-05-06> add for load lunal data end
        
        // Gionee <lilg><2013-04-23> add begin
        mHourFormat = new SimpleDateFormat("HH");
        mMinuteFormat = new SimpleDateFormat("mm");
        // Gionee <lilg><2013-04-23> add begin
    }

    public static synchronized DateViews getOrCreateViews(Context context, int[] appWidgetIds) {
        if (sInstance != null) {
            return sInstance;
        } else {
            return new DateViews(context, appWidgetIds);
        }
    }

    public static void update(Context context, int[] appWidgetIds, RemoteViews views, int year, int month) {
    	if (Utility.DEBUG) {
            Log.i(TAG, "DateViews update.");
        }
        DateViews wm = getOrCreateViews(context, appWidgetIds);
        wm.start(views, appWidgetIds, year, month);
    }

    /**
     * To handle the intent which is created by this.
     * And this will called by {@link CalendarWidget.DateService}.
     */
    public static boolean processIntent(Context context, Intent intent) {
    	if (Utility.DEBUG) {
    		Log.i(TAG, "DateViews processIntent.");
    	}

    	final Uri data = intent.getData();
    	if (data == null) return false;

    	List<String> pathSegments = data.getPathSegments();
    	if (pathSegments.size() != 3) {
    		Log.e(TAG, "We want to process the intent, but the list length of the segments is not right.");
    		return false;
    	}

    	String command = pathSegments.get(0);
    	long startMillis = Long.parseLong(pathSegments.get(1));
    	long endMillis = Long.parseLong(pathSegments.get(2));
    	if (Utility.DEBUG) {
    		Log.i(TAG, "handle the intent, the command is: " + command + " , the start millis is: "
    				+ startMillis + " , the end millis is: " + endMillis);
    	}

    	// Caused by the target application maybe disable or force stop by user.
    	// And the target activity will be couldn't find. So we will try to catch
    	// the ActivityNotFoundException to handle this case.
    	try {
    		if (VIEW_EVENT.equals(command)) {
    			Intent viewEvent = new Intent(Intent.ACTION_VIEW);
    			viewEvent.setClassName(Utility.CALENDAR_PACKAGE_NAME, Utility.CALENDAR_ALLINONEACTIVITY);
    			viewEvent.setData(Events.CONTENT_URI);
    			viewEvent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
    					Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
    					Intent.FLAG_ACTIVITY_CLEAR_TASK);

    			// Gionee <lilg><2013-04-22> modify for Gionee widget begin
    			/*if (FeatureQuery.FEATURE_CALENDAR_VIEW_SPECIAL_VIEW) {
                    // we suggest to open the special view if the feature is open.
                    viewEvent.putExtra(EXTRA_KEY_VIEW, EXTRA_VALUE_AGENDA);
                }*/
    			// Gionee <lilg><2013-04-22> modify for Gionee widget end

    			viewEvent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis);
    			viewEvent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis);
    			context.startActivity(viewEvent);
    		} else if (NEW_EVENT.equals(command)) {
    			Intent addEvent = new Intent(Intent.ACTION_EDIT);
    			addEvent.setClassName(Utility.CALENDAR_PACKAGE_NAME, Utility.CALENDAR_EDITEVENTACTIVITY);
    			addEvent.setData(Events.CONTENT_URI);
    			addEvent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			addEvent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis);
    			addEvent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis);
    			context.startActivity(addEvent);
    		} else if (VIEW_CALENDAR_MONTH.equals(command)) {
    			Intent viewMonth = new Intent(Intent.ACTION_VIEW);
    			viewMonth.setClassName(Utility.CALENDAR_PACKAGE_NAME, Utility.CALENDAR_ALLINONEACTIVITY);
    			viewMonth.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
    					Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
    					Intent.FLAG_ACTIVITY_CLEAR_TASK);

    			// Gionee <lilg><2013-04-22> modify for Gionee widget begin
    			/*if (FeatureQuery.FEATURE_CALENDAR_VIEW_SPECIAL_VIEW) {
                    // we suggest to open the special view if the feature is open.
                    viewMonth.putExtra(EXTRA_KEY_VIEW, EXTRA_VALUE_MONTH);
                }*/
    			// Gionee <lilg><2013-04-22> modify for Gionee widget end

    			viewMonth.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis);
    			viewMonth.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis);
    			context.startActivity(viewMonth);
    		} else {
    			Log.w(TAG, "When we process the intent, but we couldn't match the command:" + command);
    			return false;
    		}
    	} catch (ActivityNotFoundException e) {
    		Log.w(TAG, "Catch the ActivityNotFoundException, return true.");
    		if (!Utility.isCalendarEnabled(context)) {
    			Toast.makeText(context, R.string.no_target_activity, Toast.LENGTH_LONG).show();
    		}
    		return true;
    	}

    	return true;
    }

    /**
     * Start to update the views and load the cursor.
     */
    private void start(RemoteViews views, int[] appWidgetIds, int year, int month) {
    	if (Utility.DEBUG) {
			Log.i(TAG, "DateViews start.");
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
        
        Time time = new Time();
        time.set(1, month, year);

        loadCursor(FIELD_ALL, time);
        updateDateViews();
    }

    /**
     * Start to load the curso.
     *
     * @param field which cursor you want to load.
     * @param time the time you want to init the cursor.
     */
    private void loadCursor(int field, Time time) {
        switch (field) {
            case FIELD_ALL:
            case FIELD_EVENT:
                if (Utility.DEBUG) {
                    Log.d(TAG, "start to load event cursor, year(" + time.year + ") month(" + time.month + ")");
                }
                mEventLoadingFinished = false;
                mEventLoader.load(time);
            case FIELD_LUNAR:
            default:
                if (field > FIELD_EVENT) {
                    if (Utility.DEBUG) {
                        Log.d(TAG, "start to load lunar cursor, year(" + time.year + ") month(" + time.month + ")");
                    }
                    mLunarLoadingFinished = false;
                    mLunarLoader.load(time);
                }
        }
    }

    private void updateDateViews() {
    	if (Utility.DEBUG) {
			Log.i(TAG, "DateViews updateDateViews.");
    	}
    	
        if (mViews == null) return;

        // We will update the date view when the lunar and event load finished.
        // So only update the intents of the date view.

        Intent intent = new Intent(mContext, DateService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetIds);
        // Save the date into intent in order to use it again when restarting
        // the process in the situation of low memory.
        Bundle date = new Bundle();
        date.putInt(Utility.KEY_EXTRA_YEAR, mYear);
        date.putInt(Utility.KEY_EXTRA_MONTH, mMonth);
        intent.putExtra(Utility.KEY_EXTRA_DATE, date);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        mViews.setRemoteAdapter(R.id.date_view, intent);

        // Set the template pending intent.
        // We will set the DateService to receive the intent, and process the intent in this class.
        // Gionee <lilg><2013-05-02> modify for gionee widget begin

        // Intent intentTemplate = new Intent(mContext, DateService.class);
        // mViews.setPendingIntentTemplate(R.id.date_view, PendingIntent.getService(mContext, 0, intentTemplate, PendingIntent.FLAG_UPDATE_CURRENT));
        Intent intentTemplate = new Intent(mContext, AllInOneActivity.class);
        mViews.setPendingIntentTemplate(R.id.date_view, PendingIntent.getActivity(mContext, 0, intentTemplate, 0));

        // Gionee <lilg><2013-05-02> modify for gionee widget end
    }

    private boolean isEventCursorValid() {
        return mEventCursor != null && !mEventCursor.isClosed();
    }

    /**
     * If the local is not cn, we suggest return the lunar cursor is not valid.
     * @return if the lunar cursor is valid.
     */
    private boolean isLunarCursorValid() {
        return LunarLoader.showLunar() && mLunarCursor != null && !mLunarCursor.isClosed();
    }

    private void initView(RemoteViews view) {
        // We will set the event count and today as invisible now.
        // If it need show, we will reset them.
    	
    	// Gionee <lilg><2013-05-06> modify for gionee widget begin
    	//   	view.setViewVisibility(R.id.event_count, View.INVISIBLE);
    	//    	view.setViewVisibility(R.id.today, View.INVISIBLE);
    	// Gionee <lilg><2013-05-06> modify for gionee widget end
    	
    	// Gionee <lilg><2013-06-08> add begin
    	view.setViewVisibility(R.id.widget_other_day_event_bg, View.GONE);
    	// Gionee <lilg><2013-06-08> add end
        
        // We will set the date and the lunar as null first.
        view.setTextViewText(R.id.date, null);
        view.setTextColor(R.id.date, mRes.getColor(android.R.color.white));
        view.setTextViewText(R.id.lunar, null);
        view.setTextColor(R.id.lunar, mRes.getColor(android.R.color.white));
    }
    
    // Gionee <lilg><2013-05-06> add for gionee widget begin
    private void initDateViewLine(RemoteViews view, int position){
    	// init date view line visibility. set the first column line gone.
    	if(position % 7 == 0){
    		view.setViewVisibility(R.id.gn_widget_line_v, View.GONE);
    	}
    }
    // Gionee <lilg><2013-05-06> add for gionee widget end
    
    /**
     * Bind the date view, if the date is not in the main month, we will leave it as null
     * which is init by {@link #initView(RemoteViews)}.
     */
    private void bindBasicViewAt(RemoteViews view, Calendar calendar) {
    	int year = calendar.get(Calendar.YEAR);
    	int month = calendar.get(Calendar.MONTH);
    	int day = calendar.get(Calendar.DAY_OF_MONTH);
    	int day_of_week = calendar.get(Calendar.DAY_OF_WEEK);

    	if (Utility.DEBUG) {
    		Log.d(TAG, "bind the basic view, " + year + "-" + month + "-" + day + ", week: "+ day_of_week);
    	}

    	if (year == mYear && month == mMonth) {
    		// show day number
    		view.setTextViewText(R.id.date, String.valueOf(day));
    		if (day_of_week == Calendar.SATURDAY || day_of_week == Calendar.SUNDAY) {
    			view.setTextColor(R.id.date, mRes.getColor(R.color.red));
    		}

    		// if the day is today, show the today image.
    		Time today = new Time();
    		today.setToNow();
    		if (year == today.year && month == today.month && day == today.monthDay) {
    			// Gionee <lilg><2013-06-08> modify for Gionee widget begin
    			//    			 view.setViewVisibility(R.id.today, View.VISIBLE);
    			// Gionee <lilg><2013-06-08> modify for Gionee widget end
    		}
    	}
    }
    
    // Gionee <lilg><2013-04-24> modify for Gionee widget begin
    /**
     * Bind the date view.
     */
    private void bindGnBasicViewAt(RemoteViews view, Calendar calendar) {
    	int year = calendar.get(Calendar.YEAR);
    	int month = calendar.get(Calendar.MONTH);
    	int day = calendar.get(Calendar.DAY_OF_MONTH);
    	int day_of_week = calendar.get(Calendar.DAY_OF_WEEK);

    	if (Utility.DEBUG) {
    		Log.d(TAG, "bind the gionee basic view, " + year + "-" + month + "-" + day + ", week: "+ day_of_week);
    	}

    	// set day background
    	if (year == mYear && month == mMonth) {
    		// current month
    		view.setViewVisibility(R.id.widget_other_month_bg, View.GONE);
    		view.setViewVisibility(R.id.widget_current_month_bg, View.VISIBLE);
    	}else{
    		// other month
    		view.setViewVisibility(R.id.widget_other_month_bg, View.VISIBLE);
    		view.setViewVisibility(R.id.widget_current_month_bg, View.GONE);
    	}
    	
    	// show day number
    	view.setTextViewText(R.id.date, String.valueOf(day));
    	if(year == mYear && month == mMonth && (day_of_week == Calendar.SATURDAY || day_of_week == Calendar.SUNDAY)){
   			view.setTextColor(R.id.date, mRes.getColor(R.color.widget_day_current_month_saturday_and_sunday_text_color));
   			view.setTextColor(R.id.lunar, mRes.getColor(R.color.widget_lunar_current_month_saturday_and_sunday_text_color));
    	}else{
    		view.setTextColor(R.id.date, mRes.getColor(R.color.widget_day_current_month_monday_to_friday_text_color));
    		view.setTextColor(R.id.lunar, mRes.getColor(R.color.widget_lunar_current_month_monday_to_friday_text_color));
    	}

    	// if the day is today, set the today color.
    	Time today = new Time();
    	today.setToNow();
    	if (year == today.year && month == today.month && day == today.monthDay) {
    		view.setTextColor(R.id.date, mRes.getColor(R.color.widget_day_current_month_current_day_text_color));
    		view.setTextColor(R.id.lunar, mRes.getColor(R.color.widget_lunar_current_month_current_day_text_color));
    	}
    }
    // Gionee <lilg><2013-04-24> modify for Gionee widget end

    /**
     * Bind the lunar info view, if the date is not in this month, we will leave it as null
     * which is init by {@link #initView(RemoteViews)}.
     * If the lunar is special, we will set the color as green.
     * If not we will leave it as default.
     */
    private void bindLunarViewAt(RemoteViews view, Calendar calendar) {
        if (!isLunarCursorValid()) return;

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        if (year == mYear
                && month == mMonth
                && year == mLunarCursor.getYear()
                && month == mLunarCursor.getMonth()) {
            // show lunar info
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            mLunarCursor.moveToPosition(day - 1);
            if (day == mLunarCursor.getDayOfMonth()) {
                if (Utility.DEBUG) {
                    Log.d(TAG, "bind lunar view at " + year + "-" + month + "-" + day);
                }
                view.setTextViewText(R.id.lunar, mLunarCursor.getLunar());
                if (mLunarCursor.isSpecial()) {
                    view.setTextColor(R.id.lunar, mRes.getColor(R.color.green));
                }
            }
        }
    }
    
    // Gionee <lilg><2013-04-24> modify for Gionee widget begin
    private void bindGnLunarViewAt(RemoteViews view, Calendar calendar) {
        if (!LunarLoader.showLunar()) return;

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String lunar = lunarUtil.getLunarFestivalChineseString(year, month + 1, day);
        Log.d(TAG, "lunar is: " + lunar);
        if(lunar != null){
	        String[] lunarArr = lunar.split(";");
	        if(lunarArr != null){
	        	lunar = lunarArr[0];
	        }
        }
        // show lunar info
        if (Utility.DEBUG) {
        	Log.d(TAG, "bind lunar view at " + year + "-" + month + "-" + day + ", lunal: " + lunar);
        }
        view.setTextViewText(R.id.lunar, lunar);
        
        // Gionee <lilg><2013-05-04> add for test lunar begin
        /*String lunar2 = lunarUtil.getFestivalChineseString(year, month + 1, day);
        String lunar3 = lunarUtil.getLunarDateString(year, month + 1, day);
        Log.d(TAG, "lunar2: " + lunar2); 
        Log.d(TAG, "lunar3: " + lunar3);*/ 
        // Gionee <lilg><2013-05-04> add for test lunar end
        
    }
    // Gionee <lilg><2013-04-24> modify for Gionee widget end

    /**
     * Bind the event count view, if the date is not in this month, we will leave it as null
     * which is init by {@link #initView(RemoteViews)}.
     * If the event cursor is valid, we need set the fill in intent:
     *          in this month and has event, suggest to open the calendar agenda view
     *          in this month and no event, suggest to add the event
     *          in another month, suggest to open the calendar month view
     */
    private void bindEventViewAt(RemoteViews view, Calendar calendar) {
        if (!isEventCursorValid()) return;

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // get the start millis and end millis.
        Time startTime = new Time();
        startTime.set(0, 0, 0, day, month, year);

        Time endTime =  new Time();
        endTime.set(0, 0, 0, day + 1, month, year);

        if (year == mYear
                && month == mMonth
                && year == mEventCursor.getYear()
                && month == mEventCursor.getMonth()) {
            // show event info
            mEventCursor.moveToPosition(day - 1);
            if (day == mEventCursor.getDayOfMonth()) {
                if (Utility.DEBUG) {
                    Log.d(TAG, "bind event view at " + year + "-" + month + "-" + day);
                }
                // set the intent
                int count = mEventCursor.getEventCount();
                if (count > 0) {
                    // has event, set the intent as view the agenda in Calendar
                	
                	// Gionee <lilg><2013-05-06> modify for gionee widget begin
                	// view.setViewVisibility(R.id.event_count, View.VISIBLE);
                	// view.setTextViewText(R.id.event_count, String.valueOf(count));
                    // Gionee <lilg><2013-05-06> modify for gionee widget end
                    
                    setFillInIntent(view, COMMAND_URI_VIEW_EVENT, startTime.toMillis(true),
                            endTime.toMillis(true));
                } else {
                    // no event, set the intent as add event
                    setFillInIntent(view, COMMAND_URI_NEW_EVENT, startTime.toMillis(true),
                            endTime.toMillis(true));
                }
                // Gionee <lilg><2013-05-06> modify for gionee widget begin
                //view.setTextViewText(R.id.event_info, mEventCursor.getFirstEventTitle());
                // Gionee <lilg><2013-05-06> modify for gionee widget end
            }
        } else {
            // in another month, set the intent as view the month in Calendar
            Time viewStartTime = new Time();
            viewStartTime.set(0, 0, 0, 1, mMonth, mYear);
            long start = viewStartTime.toMillis(true);
            setFillInIntent(view, COMMAND_URI_VIEW_CALENDAR_MONTH, start, start + 1);
        }
    }

    /**
     * If the event cursor is not valid, this method will be called. It will set the intent for
     * the children of the grid view:
     * 			in this month, suggest to add the event
     *          in another month, suggest to open the calendar month view
     */
    private void setViewIntent(RemoteViews view, Calendar calendar) {
        if (isEventCursorValid()) return;

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        Time startTime = new Time();
        startTime.set(0, 0, 0, day, month, year);

        Time endTime =  new Time();
        endTime.set(0, 0, 0, day + 1, month, year);

        if (year == mYear && month == mMonth) {
            // in this month, set the intent as add event
            setFillInIntent(view, COMMAND_URI_NEW_EVENT, startTime.toMillis(true),
                    endTime.toMillis(true));
        } else {
            // in another month, set the intent as view the month in Calendar
            Time viewStartTime = new Time();
            viewStartTime.set(0, 0, 0, 1, mMonth, mYear);
            long start = viewStartTime.toMillis(true);
            setFillInIntent(view, COMMAND_URI_VIEW_CALENDAR_MONTH, start, start + 1);
        }
    }
    
    private void setFillInIntent(RemoteViews views, Uri baseUri, long startMillis, long endMillis) {
        Intent intent = new Intent();
        Builder builder = baseUri.buildUpon();
        builder.appendPath(String.valueOf(startMillis));
        builder.appendPath(String.valueOf(endMillis));
        intent.setDataAndType(builder.build(), MIME_TYPE);
        views.setOnClickFillInIntent(R.id.date_item, intent);
    }
    
    // Gionee <lilg><2013-04-24> modify for Gionee widget begin
    /**
     * Bind the event count view.
     * If the event cursor is valid, we need set the fill in intent:
     *          in this month or another month, suggest to open the calendar day view
     */
    private void bindGnEventViewAt(RemoteViews view, Calendar calendar) {
    	if (Utility.DEBUG) {
    		Log.d(TAG, "bindGnEventViewAt, !isEventCursorValid(): " + !isEventCursorValid());
    	}
        if (!isEventCursorValid()) return;

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if (Utility.DEBUG) {
    		Log.d(TAG, "year: " + year + ", month: " + month + ", day: " + day);
    	}
        
        // get the start millis and end millis.
        /*Time startTime = new Time();
        startTime.set(0, 0, 0, day, month, year);

        Time endTime =  new Time();
        endTime.set(0, 0, 0, day + 1, month, year);*/

        if (year == mYear && month == mMonth && year == mEventCursor.getYear() && month == mEventCursor.getMonth()) {
            // show event info
            mEventCursor.moveToPosition(day - 1);
            if (day == mEventCursor.getDayOfMonth()) {
                if (Utility.DEBUG) {
                    Log.d(TAG, "bind event view at " + year + "-" + month + "-" + day);
                }
                // set the intent
                int count = mEventCursor.getEventCount();
                if (count > 0) {
                    // has event, set the intent as view the agenda in Calendar
//                    view.setViewVisibility(R.id.event_count, View.VISIBLE);
//                    view.setTextViewText(R.id.event_count, String.valueOf(count));
                    /*setFillInIntent(view, COMMAND_URI_VIEW_EVENT, startTime.toMillis(true), endTime.toMillis(true));*/
                	Time today = new Time();
                	today.setToNow();
                	
                	// Gionee <lilg><2013-06-13> modify for event bg begin
                	// no matter today or other day, bg is same
                	/*if(day == today.monthDay){
                		// today
                		view.setViewVisibility(R.id.widget_current_day_event_bg, View.VISIBLE);
                		view.setViewVisibility(R.id.widget_other_day_event_bg, View.GONE);
                	}else{
                		// other day
                		view.setViewVisibility(R.id.widget_current_day_event_bg, View.GONE);
                		view.setViewVisibility(R.id.widget_other_day_event_bg, View.VISIBLE);
                	}*/
                	
                	view.setViewVisibility(R.id.widget_other_day_event_bg, View.VISIBLE);
                	// Gionee <lilg><2013-06-13> modify for event bg end
                	
                } else {
                    // no event, set the intent as add event
                    /*setFillInIntent(view, COMMAND_URI_NEW_EVENT, startTime.toMillis(true),endTime.toMillis(true));*/
                	view.setViewVisibility(R.id.widget_other_day_event_bg, View.GONE);
                }
            }
        } else {
            // in another month, set the intent as view the month in Calendar
            /*Time viewStartTime = new Time();
            viewStartTime.set(0, 0, 0, 1, mMonth, mYear);
            long start = viewStartTime.toMillis(true);
            setFillInIntent(view, COMMAND_URI_VIEW_CALENDAR_MONTH, start, start + 1);*/
        }
        setGnFillInIntent(view, calendar);
    }
    
    /**
     * It will set the intent for the children of the grid view:
     * 			in this month or another month, suggest to open the calendar day view
     */
    private void setGnViewIntent(RemoteViews view, Calendar calendar) {
    	if (Utility.DEBUG) {
    		Log.d(TAG, "setGnViewIntent, isEventCursorValid(): " + isEventCursorValid());
    	}
    	if (isEventCursorValid()) return;
    	
    	// set the intent as view the day in Calendar
    	// Gionee <lilg><2013-05-02> modify for gionee widget begin
    	setGnFillInIntent(view, calendar);
        // Gionee <lilg><2013-05-02> modify for gionee widget end
    	
    }
    
    private void setGnFillInIntent(RemoteViews view, Calendar calendar) {
    	if (Utility.DEBUG) {
    		Log.d(TAG, "DateViews setGnFillInIntent.");
    	}
    	int year = calendar.get(Calendar.YEAR);
    	int month = calendar.get(Calendar.MONTH);
    	int day = calendar.get(Calendar.DAY_OF_MONTH);
    	if (Utility.DEBUG) {
    		Log.d(TAG, "year: " + year + ", month: " + month + ", day: " + day);
    	}
    	
    	Time time = new Time(Utils.getTimeZone(mContext, null));
    	// Gionee <lilg><2013-06-08> modify begin
    	time.set(0, mMinute, mHour, day, month, year);
    	Log.d(TAG, "minute: " + mMinute + ", hour: " + mHour);
    	// Gionee <lilg><2013-06-08> modify end
        long millis = time.toMillis(true);
        
    	Intent gotoTargetDay = new Intent(Intent.ACTION_VIEW);
    	gotoTargetDay.setClass(mContext, AllInOneActivity.class);
    	gotoTargetDay.setData(Uri.parse("content://com.android.calendar/time/" + millis));
    	gotoTargetDay.putExtra(Utility.KEY_EXTRA_VIEW_TYPE, ViewType.DAY);
        view.setOnClickFillInIntent(R.id.date_item, gotoTargetDay);
    	
    }
    
    // Gionee <lilg><2013-04-24> modify for Gionee widget end

    // Gionee <lilg><2013-05-29> add for calendar holiday begin
    private void bindHolidayViewAt(RemoteViews view, Calendar calendar){
    	if (Utility.DEBUG) {
    		Log.d(TAG, "DateViews bindHolidayViewAt.");
    	}
    	
    	int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if (Utility.DEBUG) {
    		Log.d(TAG, "year: " + year + ", month: " + month + ", day: " + day);
    	}
        
        Time t = new Time();
		t.year = year;
		t.month = month;
		t.monthDay = day;
		t.normalize(true);
		int julianDay = Time.getJulianDay(t.toMillis(true), t.gmtoff);
        
		// 0 is DAY_TYPE_NORMAL
		// 3 is DAY_TYPE_HOLIDAY
		// 1 is DAY_TYPE_WORK_SHIFT
    	int dayType = LegalHolidayUtils.getInstance().getDayType(julianDay);
    	if (Utility.DEBUG) {
    		Log.d(TAG, "julianDay: " + julianDay + ", dayType: " + dayType);
    	}
    	if(dayType == ILegalHoliday.DAY_TYPE_NORMAL){
    		view.setViewVisibility(R.id.widget_holiday_bg, View.GONE);
    		view.setViewVisibility(R.id.widget_work_shift_bg, View.GONE);
    	}else if(dayType == ILegalHoliday.DAY_TYPE_HOLIDAY){
    		view.setViewVisibility(R.id.widget_holiday_bg, View.VISIBLE);
    	}else if(dayType == ILegalHoliday.DAY_TYPE_WORK_SHIFT){
    		view.setViewVisibility(R.id.widget_work_shift_bg, View.VISIBLE);
    	}else{
    		Log.e(TAG, "dayType: " + dayType);
    	}
    	
    }
    // Gionee <lilg><2013-05-29> add for calendar holiday end
    
    @Override
    public RemoteViews getViewAt(int position) {
    	if (Utility.DEBUG) {
    		Log.i(TAG, "get the view at position:" + position);
    	}
    	
    	Calendar calendar = Calendar.getInstance();
    	calendar.clear();
//    	calendar.setFirstDayOfWeek(Utility.FIRST_DAY_OF_WEEK);
    	calendar.setFirstDayOfWeek(firstDayOfWeek);
    	
    	calendar.set(Calendar.YEAR, mYear);
    	calendar.set(Calendar.MONTH, mMonth);
//    	calendar.set(Calendar.DAY_OF_WEEK, (position + Utility.FIRST_DAY_OF_WEEK) % 7);
    	calendar.set(Calendar.DAY_OF_WEEK, (position + firstDayOfWeek) % 7);
    	calendar.set(Calendar.WEEK_OF_MONTH, (position / 7) + 1);

    	RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.gn_widget_date);
    	initView(view);

    	// Gionee <lilg><2013-04-24> modify for Gionee widget begin
    	initDateViewLine(view, position);
    	
    	/*bindBasicViewAt(view, calendar);*/
    	bindGnBasicViewAt(view, calendar);

    	/*if (isLunarCursorValid()) {
    		bindLunarViewAt(view, calendar);
    	}*/
    	if (LunarLoader.showLunar()) {
    		bindGnLunarViewAt(view, calendar);
    	}

    	// Caused by the intent is according to the event, so we will set the intent in this.
    	if (isEventCursorValid()) {
    		/*bindEventViewAt(view, calendar);*/
    		bindGnEventViewAt(view, calendar);
    	} else {
    		/*setViewIntent(view, calendar);*/
    		setGnViewIntent(view, calendar);
    	}
    	
    	// Gionee <lilg><2013-05-29> add for calendar holiday begin
    	bindHolidayViewAt(view, calendar);
    	// Gionee <lilg><2013-05-29> add for calendar holiday end
    	
    	// Gionee <lilg><2013-04-24> modify for Gionee widget end
    	return view;
    }

    @Override
    public RemoteViews getLoadingView() {
    	if (Utility.DEBUG) {
    		Log.i(TAG, "getLoadingView.");
    	}
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.gn_widget_date);
        initView(views);
        return views;
    }

    @Override
    public int getCount() {
    	if (Utility.DEBUG) {
    		Log.d(TAG, "getCount, count: " + (Utility.DATE_VIEW_ROWS * Utility.DATE_VIEW_COLUMNS));
    	}
        // We will always show 6 rows * 7 columns, so the count will be 42.
        return Utility.DATE_VIEW_ROWS * Utility.DATE_VIEW_COLUMNS;
    }

    @Override
    public long getItemId(int position) {
    	if (Utility.DEBUG) {
    		Log.d(TAG, "getItemId, position: " + position);
    	}
        return position;
    }

    @Override
    public int getViewTypeCount() {
    	if (Utility.DEBUG) {
    		Log.i(TAG, "getViewTypeCount.");
    	}
        return 1;
    }

    @Override
    public boolean hasStableIds() {
    	if (Utility.DEBUG) {
    		Log.i(TAG, "hasStableIds.");
    	}
        return false;
    }

    private static ILunarService sLunarService = null;
    private static ServiceConnection sLunarConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sLunarService = null;
            LunarLoader.setLunarService(null);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sLunarService = ILunarService.Stub.asInterface(service);
            LunarLoader.setLunarService(sLunarService);
        }
    };

    private ContentObserver mObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            if (Utility.DEBUG) {
                Log.i(TAG, "Get the calendar database changed, we will load cursor again.");
            }
            Time time = new Time();
            time.set(1, mMonth, mYear);
            loadCursor(FIELD_EVENT, time);
        }

    };

    @Override
    public void onCreate() {
    	if (Utility.DEBUG) {
			Log.i(TAG, "onCreate.");
    	}
        if (sLunarService == null) {
            mContext.bindService(new Intent(ILunarService.class.getName()),
                    sLunarConnection, Context.BIND_AUTO_CREATE);
        }
        mContext.getContentResolver().registerContentObserver(CalendarContract.CONTENT_URI, true, mObserver);
        AccountManager.get(mContext).addOnAccountsUpdatedListener(this, null, true);
    }

    @Override
    public void onDataSetChanged() {
    	if (Utility.DEBUG) {
			Log.i(TAG, "onDataSetChanged.");
    	}
    }

    @Override
    public void onDestroy() {
    	if (Utility.DEBUG) {
			Log.i(TAG, "onDestroy.");
    	}
        if (sLunarService != null) {
            mContext.unbindService(sLunarConnection);
            // caused by we couldn't receive the onServiceDisconnected callback, we should reset them.
            sLunarService = null;
            LunarLoader.setLunarService(null);
        }
        mContext.getContentResolver().unregisterContentObserver(mObserver);
        AccountManager.get(mContext).removeOnAccountsUpdatedListener(this);

        if (mLunarCursor != null) {
            mLunarCursor.close();
            mLunarCursor = null;
        }
        if (mEventCursor != null) {
            mEventCursor.close();
            mEventCursor = null;
        }

        if (mLunarLoader != null) {
            mLunarLoader.reset();
        }
        if (mEventLoader != null) {
            mEventLoader.reset();
        }
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        // Caused by we couldn't get the database changed message immediately.
        // So we need handle the account update events.
        if (Utility.DEBUG) {
            Log.i(TAG, "Get the account update, we will load cursor again.");
        }
        Time time = new Time();
        time.set(1, mMonth, mYear);
        loadCursor(FIELD_EVENT, time);
    }

}
