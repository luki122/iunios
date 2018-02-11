package com.gionee.legalholiday;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.gionee.calendar.GNCalendarUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.Time;
import android.util.Log;

public class LegalHolidayUtils implements ILegalHoliday {
	
	private Context mContext = null;
	
	private String mJsonStr = null;
	private Map<Integer, Integer> mHolidayMap = null;
	
	private static final String YEAR_KEY = "year";
	private static final String VERSION_KEY = "version";
	private int mYear = 2014;
	private int mVersion = 1;
	private int mFirstJulianDay = 2456294;
	
	
	// private static boolean sIsInited = false;
	public static void createLegalHolidayUtils(Context context, String jsonStr) {
		//if(sInstance == null) {
			sInstance = new LegalHolidayUtils(context, jsonStr);
			// sIsInited = true;
		//}
	}
	
	private LegalHolidayUtils(Context context, String jsonStr) {
		mContext = context;
		// mJsonStr = jsonStr;
		// Log.d("DEBUG", "mJsonStr: " + mJsonStr);
		
		mHolidayMap = new LinkedHashMap<Integer, Integer>(64);
		if(jsonStr != null) {
			Log.d("DEBUG", "NO data in local");
			parseJsonString(jsonStr,"null");
		} else {
			Log.d("DEBUG", "use local data");
			initWithLocalData();
		}
	}
	
	private LegalHolidayUtils() {
		mHolidayMap = new LinkedHashMap<Integer, Integer>();
	}
	
	public void parseJsonString(String jsonStr,String para) {
		Log.d("DEBUG", "parseJsonString() has been invoked");
		// to parse legal holiday json string
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(jsonStr);
		} catch (JSONException e) {
			Log.d("DEBUG", "fail to create JSONObject");
			return;
		}
		
		try {
			mYear = jsonObject.getInt(YEAR_KEY);
			Log.d("DEBUG", "got year " + mYear);
			
			Time t = new Time();
			t.year = mYear;
			t.month = 0;
			t.monthDay = 1;
			t.normalize(true);
			mFirstJulianDay = Time.getJulianDay(t.toMillis(true), t.gmtoff);
			Log.d("DEBUG", "mFirstJulianDay is " + mFirstJulianDay);
		} catch (JSONException e1) {
			Log.d("DEBUG", "no year key-value pair");
			return;
		}
		
		try {
			mVersion = jsonObject.getInt(VERSION_KEY);
			Log.d("DEBUG", "got version " + mVersion);
			
		} catch (JSONException e1) {
			Log.d("DEBUG", "no version number");
			return;
		}
		
		if (para.compareTo("update") == 0) {
			Log.d("DEBUG", "maybe need updating");
			SharedPreferences pref = mContext.getSharedPreferences(LEGAL_HOLIDAY_PREF_NAME, Context.MODE_PRIVATE);
			if(!pref.contains(LEGAL_HOLIDAY_YEAR_NUM)) {
				//throw new RuntimeException("invalid prefs");
				return;
			}
			
			int localYear = pref.getInt(LEGAL_HOLIDAY_YEAR_NUM, -1);
			if(localYear != -1) {
				return;
			}
			Log.d("DEBUG", "got year " + localYear);
			
			int localVersion = pref.getInt(LEGAL_HOLIDAY_YEAR_VERSION, -1);
			if(localVersion != -1) {
				return;
			}
			Log.d("DEBUG", "got version " + localVersion);
			
			if((localYear == mYear)&&(localVersion < mVersion)){
				//update
			}else{
				return;
			}
				
		}
		
		
		
		
		int yearDay = 0;
		//mHolidayMap.clear();
		clearYearData();
		for(; yearDay < 367; ++yearDay) {
			try {
				int value = jsonObject.getInt(String.valueOf(yearDay));
				mHolidayMap.put(yearDay + mFirstJulianDay, value);
			} catch (JSONException e) {
			}
		}
		
		// store data into preferences
		storeLegalHolidayData();
	}
	
	private void clearYearData() {
		int yearDay = 0;

		for(; yearDay < 367; ++yearDay) {
			try {
				if(mHolidayMap.containsKey(yearDay + mFirstJulianDay)) {
					mHolidayMap.remove(yearDay + mFirstJulianDay);
				}
			} catch (Exception e) {
			}
		}		
	}
	
	private void initWithLocalData() {
		Log.d("DEBUG", "initWithLocalData() has been invoked");

		mHolidayMap.clear();
		putInnerHolidayData();

		/*SharedPreferences pref = mContext.getSharedPreferences(LEGAL_HOLIDAY_PREF_NAME, Context.MODE_PRIVATE);
		if(!pref.contains(LEGAL_HOLIDAY_YEAR_NUM)) {
			//throw new RuntimeException("invalid prefs");
			mHolidayMap.clear();
			putInnerHolidayData();
			return;
		}
		
		mYear = pref.getInt(LEGAL_HOLIDAY_YEAR_NUM, -1);
		Log.d("DEBUG", "got year " + mYear);
		
		Time t = new Time();
		t.year = mYear;
		t.month = 0;
		t.monthDay = 1;
		t.normalize(true);
		mFirstJulianDay = Time.getJulianDay(t.toMillis(true), t.gmtoff);
		Log.d("DEBUG", "mFirstJulianDay is " + mFirstJulianDay);
		
		t.year = 2014;
		t.month = 0;
		t.monthDay = 1;
		t.normalize(true);
		int firstJulianDay = Time.getJulianDay(t.toMillis(true), t.gmtoff);
		Log.d("DEBUG", "firstJulianDay is " + firstJulianDay);
		
		mHolidayMap.clear();
		for(int julianDay = firstJulianDay; julianDay < (mFirstJulianDay + 367); ++julianDay) {
			int value = pref.getInt(String.valueOf(julianDay), -1);
			if(value != -1) {
				Log.d("DEBUG", "put " + GNCalendarUtils.printDate(julianDay) + ", type " + value);
				mHolidayMap.put(julianDay, value);
			}
		}
		
		//here
		putInnerHolidayData();*/
	}

	private void putInnerHolidayData() {
		Time t = new Time();		
		t.year = 2014;
		t.month = 0;
		t.monthDay = 1;
		t.normalize(true);
		int firstJulianDay2014 = Time.getJulianDay(t.toMillis(true), t.gmtoff);

		int yearDays2014[] = {0, 25, 30, 31, 32, 33, 34, 35, 36, 38, 94, 95, 96, 120, 121, 122, 123, 150, 151, 152, 
				248, 249, 250, 270, 273, 274, 275, 276, 277, 278, 279, 283};
		int dayType2014[] = {3, 1, 3, 3, 3, 3, 3, 3, 3, 1, 3, 3, 3, 3, 3, 3, 1, 3, 3, 3, 
				3, 3, 3, 1, 3, 3, 3, 3, 3, 3, 3, 1};

		for (int i = 0; i < yearDays2014.length; i++) {
			if (mHolidayMap.containsKey(firstJulianDay2014 + yearDays2014[i])) {
				Log.d("liumxxx", "it has 2014 holiday data ");
			} else {
				mHolidayMap.put(firstJulianDay2014 + yearDays2014[i], dayType2014[i]);
				Log.d("liumxxx", "put 2014 holiday data ");
			}
		}

		t.year = 2015;
		t.month = 0;
		t.monthDay = 1;
		t.normalize(true);
		int firstJulianDay2015 = Time.getJulianDay(t.toMillis(true), t.gmtoff);

		int yearDays2015[] = {0, 1, 2, 3, 45, 48, 49, 50, 51, 52, 53, 54, 58, 93, 94, 95, 120, 121, 122, 170, 171, 172, 
				245, 246, 247, 248, 268, 269, 273, 274, 275, 276, 277, 278, 279, 282};
		int dayType2015[] = {3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 
				3, 3, 3, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1};

		for (int i = 0; i < yearDays2015.length; i++) {
			if (!mHolidayMap.containsKey(firstJulianDay2015 + yearDays2015[i])) {
				mHolidayMap.put(firstJulianDay2015 + yearDays2015[i], dayType2015[i]);
			}
		}
	}

	private void storeLegalHolidayData() {
		Log.d("DEBUG", "storeLegalHolidayData() has been invoked");
		SharedPreferences pref = mContext.getSharedPreferences(LEGAL_HOLIDAY_PREF_NAME, Context.MODE_PRIVATE);

		//if(!pref.contains(LEGAL_HOLIDAY_YEAR_NUM)) {
		{
			Log.d("DEBUG", "put data into pref");
			
			SharedPreferences.Editor editor = pref.edit();
			
			editor.clear();
			editor.commit();
			
			editor.putInt(LEGAL_HOLIDAY_YEAR_NUM, mYear);
			editor.putInt(LEGAL_HOLIDAY_YEAR_VERSION, mVersion);
			
			for(Map.Entry<Integer, Integer> entry : mHolidayMap.entrySet()) {
				Log.d("DEBUG", "put " + entry.getKey() + ": " + entry.getValue());
				editor.putInt(String.valueOf(entry.getKey()), entry.getValue());
			}
			
			editor.apply();
		}
	}
	
	private static LegalHolidayUtils sInstance = null;
	
	public static LegalHolidayUtils getInstance() {
		// if(sInstance == null) throw new RuntimeException("LegalHolidayUtils instance is null!");
		if(sInstance == null) {
			sInstance = new LegalHolidayUtils();
		}
		
		return sInstance;
	}

	@Override
	public int getDayType(int julianDay) {
		if(mHolidayMap.containsKey(julianDay)) {
			return mHolidayMap.get(julianDay);
		}
		
		return DAY_TYPE_NORMAL;
	}
	
	
	public static void initHolidayData(Context context) {
		LegalHolidayUtils.createLegalHolidayUtils(context, null);

		/*Time t = new Time();
		t.setToNow();
		int year;

		
		SharedPreferences pref = context.getSharedPreferences(ILegalHoliday.LEGAL_HOLIDAY_PREF_NAME, Context.MODE_PRIVATE);
		
		year = pref.getInt(LEGAL_HOLIDAY_YEAR_NUM, -1);
		if(year != -1) {
			//error
		}
		
		if((pref.contains(ILegalHoliday.LEGAL_HOLIDAY_YEAR_NUM))&&(year == t.year)) {
			LegalHolidayUtils.createLegalHolidayUtils(context, null);
			
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
			
	        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
	        
	        if(networkInfo == null || !networkInfo.isAvailable()) {
	        	Log.i("holiday", "networkInfo is not Available 2");
	        } else {
	        	Log.i("holiday", "networkInfo is Available 2");
	        	
	        	if(1==t.monthDay) {
		        	String string = "update";
	    			HolidayData task = new HolidayData(context);
	    	        task.execute(string);
	        	}
	        }
		}
		else
		{
	        //
	        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
	        //  
	        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
	        
	        if(networkInfo == null || !networkInfo.isAvailable()) {
	        	Log.i("holiday", "networkInfo is not Available");
	        	LegalHolidayUtils.createLegalHolidayUtils(context, null);
	        } else {
	        	Log.i("holiday", "networkInfo is Available");
	        	String string = "create";
    			HolidayData task = new HolidayData(context);
    	        task.execute(string);  
	        }
		}*/
		
		
	}
	
	
}
