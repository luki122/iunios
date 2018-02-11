package com.aurora.voiceassistant.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONObject;

import com.google.gson.JsonObject;

import com.aurora.voiceassistant.*;
import android.R.integer;
import android.content.Context;
import android.text.format.Time;
import android.util.Log;

public class RspJsonReminderAlarm {
	private JSONObject jsonObject;
	private String description;
	private String content;
	private String result;
	private String time;
	private String cmd;
	private String contentRespStat;
	private String date;
	private String dateType;
	private ALARM_TYPE mAlarmType = ALARM_TYPE.ONCE;
	
	private Integer dateYear;
	private Integer dateMonth;
	private Integer dateDay;
	private Integer dateDayOfWeek = 1;
	private Integer daysDuration = 1;
	private Integer dayOfWeek;
	private String timeHour;
	private String timeMinute;
	private String[] ruleStrings;
	private String rRule;
	
	public enum ALARM_TYPE {
		ONCE, EVERY_DAY, EVERY_WEEK, EVERY_MONTH, EVERY_YEAR
	};
	
	private HashMap<ALARM_TYPE, String> ruleMap = new HashMap<RspJsonReminderAlarm.ALARM_TYPE, String>();
	
	public RspJsonReminderAlarm(Context context, JSONObject object) {
		// TODO Auto-generated constructor stub
		jsonObject = object;
		ruleStrings = context.getResources().getStringArray(R.array.vs_reminder_alarm_rule);
		rRule = ruleStrings[ALARM_TYPE.ONCE.ordinal()];
//		initRuleMap();
		init();
	}
	
	public void initRuleMap() {
		ruleMap.put(ALARM_TYPE.ONCE, "一次性");
		ruleMap.put(ALARM_TYPE.EVERY_DAY, "每天");
		ruleMap.put(ALARM_TYPE.EVERY_WEEK, "每周");
		ruleMap.put(ALARM_TYPE.EVERY_MONTH, "每月");
	}
	
	public void init() {
		Log.d("DEBUG", "RspJsonReminderAlarm init ============================ ");
		Calendar mCalendar = Calendar.getInstance();
		dateYear = mCalendar.get(Calendar.YEAR);
		dateMonth = mCalendar.get(Calendar.MONTH);
		dateDay = mCalendar.get(Calendar.DAY_OF_MONTH);
		timeHour = String.valueOf(Calendar.HOUR_OF_DAY);
		timeMinute = String.valueOf(Calendar.MINUTE);
		
		cmd = jsonObject.optString(CFG.CMD);
		if (cmd == null) {
			cmd = "add";
		}
		
		content = jsonObject.optString(CFG.CONTENT);
		dateType = jsonObject.optString(CFG.DATETYPE);
		Log.d("DEBUG", "dateType ============================ "+dateType);
		date = jsonObject.optString(CFG.DATE);
		time = jsonObject.optString(CFG.TIME);
		if (time != null && !time.toLowerCase().equals("null")) {
			timeHour = time.substring(0, 2);
			timeMinute = time.substring(3, 5);
		}
		
		if ("add".equals(cmd)) {
			if ("0".equals(dateType) || "null".equals(dateType)) {		//Today/some day-->dateType = 0, date = today;
				mAlarmType = ALARM_TYPE.ONCE;
				rRule = ruleStrings[ALARM_TYPE.ONCE.ordinal()];
				Log.d("DEBUG", "ALARMTYPE.ONCE ============================ ");
				
				if (date != null) {
					Date mDate = null;
					date = date.replace("|", "");
					Log.d("DEBUG", "date after ============================ "+date);
					
					try {
						mDate = new SimpleDateFormat("yyyyMMdd").parse(date);
						
						Calendar tempCalendar = Calendar.getInstance();
						tempCalendar.setTime(mDate);
						Log.d("DEBUG", "the date.getYear()2 = "+tempCalendar.get(Calendar.YEAR));
						Log.d("DEBUG", "the date.getMonth()2 = "+tempCalendar.get(Calendar.MONTH));
						Log.d("DEBUG", "the date.getDay()2 = "+tempCalendar.get(Calendar.DAY_OF_MONTH));
						
						dateYear = tempCalendar.get(Calendar.YEAR);
						dateMonth = tempCalendar.get(Calendar.MONTH);
						dateDay = tempCalendar.get(Calendar.DAY_OF_MONTH);
						
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						Log.d("DEBUG", "ALARMTYPE.ONCE ============================ e = "+e);
					}
				}
				
			} else if ("4".equals(dateType) ) {							//everyWeek-->dateType = 4, date = weekday;
				mAlarmType = ALARM_TYPE.EVERY_WEEK;
				rRule = ruleStrings[ALARM_TYPE.EVERY_WEEK.ordinal()];
				
				if (date == null) {
					dayOfWeek = 1;
				} else {
					date = date.replace("|", "");
					dayOfWeek = Integer.valueOf(date);
				}
				
				Calendar tempCalendar = Calendar.getInstance();
				dateDay = tempCalendar.get(Calendar.DAY_OF_MONTH);
				dateDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK);
				
				int delta = dayOfWeek - dateDayOfWeek + 1;
				if (delta < 0) {
					delta = delta + 7;
				}
				dateDay = dateDay + delta;
				
			} else if ("3".equals(dateType)) {							//everyMonth-->dateType = 3, date = monthday;
				mAlarmType = ALARM_TYPE.EVERY_MONTH;
				rRule = ruleStrings[ALARM_TYPE.EVERY_MONTH.ordinal()];
				
				if (date == null) {
					dateDay = 1;
				} else {
					if (date.trim().length() == 2) {
						dateDay = Integer.valueOf(date.substring(0, 1));
					} else if (date.trim().length() == 3) {
						dateDay = Integer.valueOf(date.substring(0, 2));
					}
				}
				
			} else if ("2".equals(dateType)) {							//everyYear-->dateType = 2, date = month + monthday;
				mAlarmType = ALARM_TYPE.EVERY_YEAR;
				rRule = ruleStrings[ALARM_TYPE.EVERY_YEAR.ordinal()];
				
				if (date == null) {
					dateMonth = 0;
					dateDay = 0;
				} else {
					String month = "0";
					String day = "0";
					
					if (date.trim().length() == 4) {
						month = date.substring(0, 1);
						day = date.substring(1, 3);
					} else if (date.trim().length() == 5) {
						month = date.substring(0, 2);
						day = date.substring(2, 4);
					}
					
					dateMonth = Integer.valueOf(month) - 1;		//the month = month -1;
					dateDay = Integer.valueOf(day);
				}
				
			} else if ("5".equals(dateType)) {							//everyDay-->dateType = 5, date = null;
				mAlarmType = ALARM_TYPE.EVERY_DAY;
				rRule = ruleStrings[ALARM_TYPE.EVERY_DAY.ordinal()];
			}
			
		} else if ("view".equals(cmd)) {
			Log.d("DEBUG", "cmd = View ============================ ");
			if (dateType == null) {										//view alarm-->dateType = null, date = null;
				//to be defined
				
			} else if ("0".equals(dateType)) {							//Today-->dateType = 0, date = today;
				if (date != null) {
					Date mDate = null;
					date = date.replace("|", "");
					
					try {
						mDate = new SimpleDateFormat("yyyyMMdd").parse(date);
						
						Calendar tempCalendar = Calendar.getInstance();
						tempCalendar.setTime(mDate);
						Log.d("DEBUG", "cmd = View some day the date.getYear() = "+tempCalendar.get(Calendar.YEAR));
						Log.d("DEBUG", "cmd = View some day the date.getMonth() = "+tempCalendar.get(Calendar.MONTH));
						Log.d("DEBUG", "cmd = View some day the date.getDay() = "+tempCalendar.get(Calendar.DAY_OF_MONTH));
						Log.d("DEBUG", "cmd = View some day the date.getDayOfWeek() = "+tempCalendar.get(Calendar.DAY_OF_WEEK));
						
						dateYear = tempCalendar.get(Calendar.YEAR);
						dateMonth = tempCalendar.get(Calendar.MONTH);
						dateDay = tempCalendar.get(Calendar.DAY_OF_MONTH);
						dateDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK);
						
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						Log.d("DEBUG", "cmd = View some day ============================ e = "+e);
					}
				}
				
			} else if ("1".equals(dateType)) {							//thisWeek/thisMonth-->dateType = 1, date = thisweek/month;
				if (date != null) {
					Date mDate = null;
					date = date.replace("|", "");
										
					int index = date.indexOf(":");
					Log.d("DEBUG", "index of : = "+index);
					if (index == -1) index = date.length();
					String begintime = date.substring(0, index);
					String endtime = date.substring(index + 1, date.length());
					Log.d("DEBUG", "begintime = "+begintime);
					Log.d("DEBUG", "endtime = "+endtime);
					
					try {
						mDate = new SimpleDateFormat("yyyyMMdd").parse(begintime);
						
						Calendar tempCalendar = Calendar.getInstance();
						tempCalendar.setTime(mDate);
						Log.d("DEBUG", "cmd = View week/month the date.getYear() = "+tempCalendar.get(Calendar.YEAR));
						Log.d("DEBUG", "cmd = View week/month the date.getMonth() = "+tempCalendar.get(Calendar.MONTH));
						Log.d("DEBUG", "cmd = View week/month the date.getDay() = "+tempCalendar.get(Calendar.DAY_OF_MONTH));
						
						dateYear = tempCalendar.get(Calendar.YEAR);
						dateMonth = tempCalendar.get(Calendar.MONTH);
						dateDay = tempCalendar.get(Calendar.DAY_OF_MONTH);
						
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						Log.d("DEBUG", "cmd = View week/month ============================ e = "+e);
					}
					
					String endMonth = endtime.substring(4, 6);		//get month
					String endDay = endtime.substring(6, 8);		//get day
					
					int monthDuration = Integer.valueOf(endMonth) - 1 - dateMonth;
					int dayDuration = 7;
					if (dateMonth == 1) {
						dayDuration = Integer.valueOf(endDay) - dateDay + 1 - 2;	//there is only 28 days in february
					} else {
						dayDuration = Integer.valueOf(endDay) - dateDay + 1;
					}
					
					Log.d("DEBUG", "dayDuration ============================ = "+dayDuration);
					daysDuration = monthDuration * 30 + dayDuration;
					Log.d("DEBUG", "daysDuration = "+daysDuration);
				}
				
			}
		}		
	}
	
	public String getReminderDate() {
		return date;
	}
	
	public String getReminderContent() {
		return content;
	}
	
	public String getDateType() {
		return dateType;
	}
	
	public Integer getReminderDateYear() {
		return dateYear;
	}
	
	public Integer getReminderDateMonth() {
		return dateMonth;
	}
	
	public Integer getReminderDateDay() {
		return dateDay;
	}
	
	public Integer getReminderDateDayOfWeek() {
		return dateDayOfWeek;
	}
	
	public Integer getReminderDaysDuration() {
		return daysDuration;
	}
	
	public String getReminderTimeHour() {
		return timeHour;
	}
	
	public String getReminderTimeMinute() {
		return timeMinute;
	}
	
	public String getReminderType() {
		return cmd;
	}
	
	public ALARM_TYPE getAlarmType() {
		return mAlarmType;
	}
	
	public String getAlarmRule() {
		return rRule;
	}
	
	
//	if (jsonObject.optString(CFG.CONTENT) != null) {
//		Log.d("DEBUG", "the mReminderContent = "+jsonObject.optString(CFG.CONTENT));
//	}
	/*String content = data.optString(CFG.CONTENT);
	String date = data.optString(CFG.DATE);
	String dateYear = null;
	String dateMonth = null;
	String dateDay = null;
	if (date != null) {
		dateYear = date.substring(0, 4);
		dateMonth = date.substring(4, 6);
		dateMonth = String.valueOf(Integer.valueOf(dateMonth));
		dateDay = date.substring(6, 8);
		dateDay = String.valueOf(Integer.valueOf(dateDay));
		mReminderDate.setText(dateMonth + "月" + dateDay + "日");
	}
	Log.d("DEBUG", "the mReminderDate = "+data.optString(CFG.DATE));
	
	String time = data.optString(CFG.TIME);
	String timeHour = time.substring(0, 2);
	String timeMinute = time.substring(3, 5);
	mReminderTime.setText(time.substring(0, 5));
	Log.d("DEBUG", "the mReminderTime = "+data.optString(CFG.TIME));
	
	Log.d("DEBUG", "the mReminderDateType = "+data.optString(CFG.DATETYPE));
	
	String cmdType = data.optString(CFG.CMD);
	Log.d("DEBUG", "the mReminderCmd = "+cmdType);*/
}
