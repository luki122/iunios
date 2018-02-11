package com.aurora.voiceassistant.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.util.Log;
import com.aurora.voiceassistant.model.BaseHelper.BaseHelperEvent;

public class CalendarHelper extends BaseHelper implements BaseHelperEvent{

	//Shigq add start
	public long Calendar_ID;
	private static final String PC_SYNC_ACCOUNT_NAME = "PC Sync";
	//Shigq add end
	
	public String ProgramName;
	
	/**year,month, day of month, hour of day,minutes will set value by shiguiqiang*/
	public int DST_YEAR;
	
	public int DST_MONTH;
	
	public int DST_DAY_OF_MONTH;
	
	public int DST_HOUR_OF_DAY;
	
	public int DST_MINUTES;
	
	/** Calendar id */
	public String EVENT_CALENDAR_ID;

	/** Need alarm */
	public int EVENT_HAS_ALARM;
	
	/** alarm rule */
	public String RRULE;
	
	/** alarm duration */
	public String DURATION;

	/** Title */
	public String EVENT_TITLE;

	/** Description */
	public String EVENT_DESCRIPTION;

	/**read goal preference essential*/
	private final String CONTEXT_PACKAGE_NAME = "com.android.calendar";
	
	private final String SHAREDPRE_XML_NAME = "calendar_reminder_value";
	
	private final String SHAREDPRE_KEY_NAME = "preferences_default_reminder";
	
	/** Calendar construct function */
	public CalendarHelper(Context context) {
		mContext = context;
	}
	
	/** get Calendar id  */
	@SuppressLint("NewApi")
	public static Long getCalendarId(Context context) {
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(Calendars.CONTENT_URI,
		                		 new String[] {Calendars._ID},
		                		 Calendars.ACCOUNT_NAME + "='" + PC_SYNC_ACCOUNT_NAME + "'",
		                		 null, null);

        long calendarId = 0;
        if (cursor != null && cursor.moveToFirst()) {
            calendarId = cursor.getLong(0);
        } else {
        	ContentValues values = new ContentValues();
            values.put(Calendars.ACCOUNT_NAME, PC_SYNC_ACCOUNT_NAME);
            values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
            values.put(Calendars.CALENDAR_DISPLAY_NAME, PC_SYNC_ACCOUNT_NAME);
            values.put(Calendars.CALENDAR_COLOR, -9215145);
            values.put(Calendars.CALENDAR_ACCESS_LEVEL, 700);
            values.put(Calendars.SYNC_EVENTS, 1);
            values.put(Calendars.OWNER_ACCOUNT, PC_SYNC_ACCOUNT_NAME);
            Uri uri = cr.insert(Calendars.CONTENT_URI, values);

            calendarId = ContentUris.parseId(uri);
            }
        if (cursor != null) cursor.close();
        
        return calendarId;
	}

	/** Schedule new Program */
	@Override
	public void scheduleNewEvent() {
		long startMillis = 0;
		long endMillis = 0;

		Uri url = null;
		
		Calendar beginTime = Calendar.getInstance();
		Calendar endTime = Calendar.getInstance();
		
		if (DEBUG)
			Log.e(TAG,
					"beginTime.getTime()=" + beginTime.getTime() + ";"
							+ "beginTime.get(Calendar.YEAR)="
							+ beginTime.get(Calendar.YEAR) + ";"
							+ "beginTime.get(Calendar.MONTH)="
							+ beginTime.get(Calendar.MONTH) + ";"
							+ "beginTime.get(Calendar.DAY_OF_MONTH)="
							+ beginTime.get(Calendar.DAY_OF_MONTH) + ";"
							+ "beginTime.get(Calendar.MINUTE)="
							+ beginTime.get(Calendar.MINUTE) + ";"
							+ "beginTime.get(Calendar.HOUR_OF_DAY)="
							+ beginTime.get(Calendar.HOUR_OF_DAY) + ";"
							+ "beginTime.get(Calendar.SECOND)="
							+ beginTime.get(Calendar.SECOND) + ";"
							+ "beginTime.get(Calendar.HOUR)="
							+ beginTime.get(Calendar.HOUR));

		/**we set the time according to system provided*/
		beginTime.set(beginTime.get(Calendar.YEAR),
				beginTime.get(Calendar.MONTH),
				beginTime.get(Calendar.DAY_OF_MONTH),
				beginTime.get(Calendar.HOUR_OF_DAY),
				beginTime.get(Calendar.MINUTE));
		
		/**Test time. we will use response data later*/
		endTime.set(DST_YEAR,
				DST_MONTH,
				DST_DAY_OF_MONTH,
				DST_HOUR_OF_DAY,
				DST_MINUTES);
		
		startMillis = beginTime.getTimeInMillis();
		
		endMillis = endTime.getTimeInMillis();
		

        long calId = scheduleNewProgram(ProgramName);
		
        /**note: get reminder time from other preferences.so attention if return wrong values*/
        
        String timeReminder = getReminderMinutes();
        if(DEBUG)
        	Log.e(TAG, "Get reminder time = "+timeReminder);
        
		ContentResolver cr = mContext.getContentResolver();
		ContentValues values = new ContentValues();
		values.put(Events.CALENDAR_ID, String.valueOf(calId));
//		values.put(Events.DTSTART, startMillis);
		values.put(Events.DTSTART, endMillis);
		
//		values.put(Events.DTEND, endMillis + 10 * 60);
		values.put(Events.HAS_ALARM, EVENT_HAS_ALARM);
		if (RRULE != null) {
			values.put(Events.RRULE, RRULE);
			values.put(Events.DURATION, DURATION);
			values.put(Events.DTEND, (Long) null);
		} else {
			values.put(Events.DTEND, endMillis);
		}
		values.put(Events.TITLE, EVENT_TITLE);
		values.put(Events.DESCRIPTION, EVENT_DESCRIPTION);
		values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID().toString());
		values.put(Events.EVENT_END_TIMEZONE, TimeZone.getDefault().getID().toString());
		url = cr.insert(Events.CONTENT_URI, values);
		/**reminder*/
		Long eventId= Long.parseLong(url.getLastPathSegment());
    	ContentValues remider = new ContentValues();
    	remider.put(Reminders.EVENT_ID, eventId );
    	remider.put( Reminders.MINUTES, String.valueOf(timeReminder));
    	remider.put(Reminders.METHOD, Reminders.METHOD_ALERT );
    	mContext.getContentResolver().insert(Reminders.CONTENT_URI, remider);
        	
	}

	
	/**@desc create calendar 
	 * @return calendar id 
	 * @author hazel
	 * */
	private long scheduleNewProgram(String strName){
		ContentValues values=new ContentValues();
		values.put(Calendars.ACCOUNT_NAME,strName);
		values.put(Calendars.ACCOUNT_TYPE,"LOCAL");
		values.put(Calendars.NAME,strName);
		values.put(Calendars.CALENDAR_DISPLAY_NAME,strName);
		values.put(Calendars.CALENDAR_ACCESS_LEVEL,Calendars.CAL_ACCESS_OWNER);
		values.put(Calendars.VISIBLE,1);
		values.put(Calendars.SYNC_EVENTS,1);
		values.put(Calendars.OWNER_ACCOUNT,strName);
		Uri uri=mContext.getContentResolver().insert(CalendarContract.Calendars.CONTENT_URI, values);

		Long calId= Long.parseLong(uri.getLastPathSegment());
		return calId;
	}
	
	/**get reminder time from calendar. */
	private String getReminderMinutes(){
		Context otherAppsContext = null;
		try {
			otherAppsContext = mContext.createPackageContext(CONTEXT_PACKAGE_NAME, 0);
		} catch (NameNotFoundException e) {
			if(DEBUG)
			   Log.e(TAG, "casue about exception = "+e.getMessage());
		}
	 	SharedPreferences settings = otherAppsContext.getSharedPreferences(SHAREDPRE_XML_NAME, Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
    	return  settings.getString(SHAREDPRE_KEY_NAME, String.valueOf(10)); //default values is 10
	}
	
}
