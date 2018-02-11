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
// Gionee <jiating> <2013-04-24> modify for CR00000000  begin 
package com.gionee.calendar;
//Gionee <jiating> <2013-04-24> modify for CR00000000  begin
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import com.android.calendar.CalendarController.ViewType;
import com.android.calendar.CalendarUtils.TimeZoneUtils;

import com.android.calendar.Utils;
import com.gionee.calendar.view.Log;


import android.content.Context;
import android.os.Handler;
import android.text.format.DateUtils;
import android.text.format.Time;

public class GNDateTextUtils {


	private long mMilliTime;
	private long mTodayJulianDay;
	private String mTimeZone;
	private StringBuilder mStringBuilder;
	private Formatter mFormatter;
	private Context mContext;
	static final String SHARED_PREFS_NAME = "com.android.calendar_preferences";
	private static final TimeZoneUtils mTZUtils = new TimeZoneUtils(SHARED_PREFS_NAME);


    // Updates time specific variables (time-zone, today's Julian day).
    private Runnable mTimeUpdater = new Runnable() {
        @Override
        public void run() {
            refresh(mContext);
        }
    };

	public GNDateTextUtils(Context context) {

		mContext = context;
		mStringBuilder = new StringBuilder(50);
		mFormatter = new Formatter(mStringBuilder, Locale.getDefault());
        // Sets time specific variables and starts a thread for midnight updates
		refresh(context);
	}

    // Sets the time zone and today's Julian day to be used by the adapter.
    // Also, notify listener on the change and resets the midnight update thread.
    public void refresh(Context context) {
    	mTimeZone = Utils.getTimeZone(context, mTimeUpdater);
    	Time time = new Time(mTimeZone);
        long now = System.currentTimeMillis();
        time.set(now);
        mTodayJulianDay = Time.getJulianDay(now, time.gmtoff);

    }

 

  
    
    public String []updateDateYearAndMonthTextByView(){
    	String dateText = null;
    	dateText = buildMonthYearUseFormat();
    	return dateText.split("/");
    }
    
    public String []updateDateYearMonthDayTextByView(){
    	String dateText = null;
    	dateText = buildMonthYearDayUseFormat();
    	return dateText.split("/");
    }






    public void setTime(long time) {
        mMilliTime = time;
    }

    private String buildMonthYearDate() {
    	SimpleDateFormat sdf= new SimpleDateFormat("MM/dd/yyyy");
    	Log.i("jiating"+"......"+sdf.format(new Date(mMilliTime)));
        mStringBuilder.setLength(0);
        String date = DateUtils.formatDateRange(
                mContext,
                mFormatter,
                mMilliTime,
                mMilliTime,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_MONTH_DAY
                        | DateUtils.FORMAT_SHOW_YEAR, mTimeZone)
                        .toString();
        Log.i("date="+date);
        
        return date;
    }

    @SuppressWarnings("unused")
	private String buildMonthYearUseFormat() {
    	SimpleDateFormat sdf= new SimpleDateFormat("yyyy/MM");
    	String date=sdf.format(new Date(mMilliTime));
    	Log.i("jiating"+"......"+date);
        return date;
    }
    
    private String buildMonthYearDayUseFormat() {
    	SimpleDateFormat sdf= new SimpleDateFormat("yyyy/MM/dd");
    	String date=sdf.format(new Date(mMilliTime));
    	Log.i("jiating"+"......111111111"+date);
        return date;
    }

    public static  String buildMonthYearDate(Context mContext,long time) {
    	
    	StringBuilder mStringBuilder = new StringBuilder(50);
    	Formatter mFormatter = new Formatter(mStringBuilder, Locale.getDefault());
        String date = DateUtils.formatDateRange(
                mContext,
                mFormatter,
                time,
                time,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
				| DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH
				| DateUtils.FORMAT_ABBREV_WEEKDAY|DateUtils.FORMAT_SHOW_TIME)
                        .toString();
        return date;
    }






    private String buildFullDate(long time) {
        mStringBuilder.setLength(0);
        String date = DateUtils.formatDateRange(mContext, mFormatter, time, time,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR, mTimeZone)
                .toString();
        return date;
    }


   
    /**
     * Formats a day of the week string. This is either just the name of the day
     * or a combination of yesterday/today/tomorrow and the day of the week.
     *
     * @param julianDay The julian day to get the string for
     * @param todayJulianDay The julian day for today's date
     * @param millis A utc millis since epoch time that falls on julian day
     * @param context The calling context, used to get the timezone and do the
     *            formatting
     * @return
     */
    public static String getDayOfWeekString(int julianDay, int todayJulianDay, long millis,
            Context context) {
        
        int flags = DateUtils.FORMAT_SHOW_WEEKDAY;
        String dayViewText;
       
        dayViewText = mTZUtils.formatDateRange(context, millis, millis, flags).toString();
        
        dayViewText = dayViewText.toUpperCase();
        return dayViewText;
    }
    
    public static  String buildMonthYearDayUseFormat(long timeMills) {
    	SimpleDateFormat sdf= new SimpleDateFormat("yyyy.MM.dd");
    	String date=sdf.format(new Date(timeMills));
    	Log.i("jiating"+"......111111111"+date);
        return date;
    }
    



}

//Gionee <jiating> <2013-04-24> modify for CR00000000  end
