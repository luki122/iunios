/*
 * Copyright (C) 2009 The Android Open Source Project
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

import java.text.DateFormatSymbols;
import java.util.Calendar;



import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import android.provider.BaseColumns;

import java.text.DateFormatSymbols;
import java.util.Calendar;



public final class Alarm implements Parcelable {

    //////////////////////////////
    // Parcelable apis
    //////////////////////////////
    public static final Parcelable.Creator<Alarm> CREATOR
            = new Parcelable.Creator<Alarm>() {
                public Alarm createFromParcel(Parcel p) {
                    return new Alarm(p);
                }

                public Alarm[] newArray(int size) {
                    return new Alarm[size];
                }
            };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel p, int flags) {
        p.writeInt(id);
        p.writeInt(enabled ? 1 : 0);
        p.writeInt(hour);
        p.writeInt(minutes);
        p.writeInt(daysOfWeek.getCoded());
        p.writeLong(time);
        p.writeInt(vibrate ? 1 : 0);
        p.writeString(label);
        p.writeParcelable(alert, flags);
        p.writeInt(silent ? 1 : 0);
    }
    //////////////////////////////
    // end Parcelable apis
    //////////////////////////////

    //////////////////////////////
    // Column definitions
    //////////////////////////////
    public static class Columns implements BaseColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.parse("content://com.android.deskclock/alarm");

        // Gionee <baorui><2013-05-04> modify for CR00803588 begin
        /**
         * The content:// style URL for this table of alert_info
         */
        public static final Uri ALERTINFO_URI = Uri.parse("content://com.android.deskclock/alert_info");
        public static final String DATA = "_data";
        public static final String VOLUMES = "volumes";
        // Gionee <baorui><2013-05-04> modify for CR00803588 end

        /**
         * Hour in 24-hour localtime 0 - 23.
         * <P>Type: INTEGER</P>
         */
        public static final String HOUR = "hour";

        /**
         * Minutes in localtime 0 - 59
         * <P>Type: INTEGER</P>
         */
        public static final String MINUTES = "minutes";

        /**
         * Days of week coded as integer
         * <P>Type: INTEGER</P>
         */
        public static final String DAYS_OF_WEEK = "daysofweek";

        /**
         * Alarm time in UTC milliseconds from the epoch.
         * <P>Type: INTEGER</P>
         */
        public static final String ALARM_TIME = "alarmtime";

        /**
         * True if alarm is active
         * <P>Type: BOOLEAN</P>
         */
        public static final String ENABLED = "enabled";

        /**
         * True if alarm should vibrate
         * <P>Type: BOOLEAN</P>
         */
        public static final String VIBRATE = "vibrate";

        /**
         * Message to show when alarm triggers
         * Note: not currently used
         * <P>Type: STRING</P>
         */
        public static final String MESSAGE = "message";

        /**
         * Audio alert to play when alarm triggers
         * <P>Type: STRING</P>
         */
        public static final String ALERT = "alert";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER =
                HOUR + ", " + MINUTES + " ASC";

        // Used when filtering enabled alarms.
        public static final String WHERE_ENABLED = ENABLED + "=1";

        static final String[] ALARM_QUERY_COLUMNS = {
            _ID, HOUR, MINUTES, DAYS_OF_WEEK, ALARM_TIME,
            ENABLED, VIBRATE, MESSAGE, ALERT };

        /**
         * These save calls to cursor.getColumnIndexOrThrow()
         * THEY MUST BE KEPT IN SYNC WITH ABOVE QUERY COLUMNS
         */
        public static final int ALARM_ID_INDEX = 0;
        public static final int ALARM_HOUR_INDEX = 1;
        public static final int ALARM_MINUTES_INDEX = 2;
        public static final int ALARM_DAYS_OF_WEEK_INDEX = 3;
        public static final int ALARM_TIME_INDEX = 4;
        public static final int ALARM_ENABLED_INDEX = 5;
        public static final int ALARM_VIBRATE_INDEX = 6;
        public static final int ALARM_MESSAGE_INDEX = 7;
        public static final int ALARM_ALERT_INDEX = 8;
    }
    //////////////////////////////
    // End column definitions
    //////////////////////////////

    // Public fields
    public int        id;
    public boolean    enabled;
    public int        hour;
    public int        minutes;
    public DaysOfWeek daysOfWeek;
    public long       time;
    public boolean    vibrate;
    public String     label;
    public Uri        alert;
    public boolean    silent;

    public Alarm(Cursor c) {
        id = c.getInt(Columns.ALARM_ID_INDEX);
        enabled = c.getInt(Columns.ALARM_ENABLED_INDEX) == 1;
        hour = c.getInt(Columns.ALARM_HOUR_INDEX);
        minutes = c.getInt(Columns.ALARM_MINUTES_INDEX);
        daysOfWeek = new DaysOfWeek(c.getInt(Columns.ALARM_DAYS_OF_WEEK_INDEX));
        time = c.getLong(Columns.ALARM_TIME_INDEX);
        vibrate = c.getInt(Columns.ALARM_VIBRATE_INDEX) == 1;
        label = c.getString(Columns.ALARM_MESSAGE_INDEX);
        String alertString = c.getString(Columns.ALARM_ALERT_INDEX);
        if (Alarms.ALARM_ALERT_SILENT.equals(alertString)) {
            silent = true;
            alert = Uri.parse(alertString);
        } else {
            if (alertString != null && alertString.length() != 0) {
                alert = Uri.parse(alertString);
            }

            // If the database alert is null or it failed to parse, use the
            // default alert.
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_ALARM);
            }
        }
    }

    public Alarm(Parcel p) {
        id = p.readInt();
        enabled = p.readInt() == 1;
        hour = p.readInt();
        minutes = p.readInt();
        daysOfWeek = new DaysOfWeek(p.readInt());
        time = p.readLong();
        vibrate = p.readInt() == 1;
        label = p.readString();
        alert = (Uri) p.readParcelable(null);
        silent = p.readInt() == 1;
    }

    // Creates a default alarm at the current time.
    public Alarm() {
        id = -1;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        hour = c.get(Calendar.HOUR_OF_DAY);
        minutes = c.get(Calendar.MINUTE);
        vibrate = true;
        daysOfWeek = new DaysOfWeek(0);
        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    }
    
 // Creates a default alarm at the current time.
	public Alarm(Context context) {
		id = -1;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		hour = c.get(Calendar.HOUR_OF_DAY);
		minutes = c.get(Calendar.MINUTE);
		vibrate = true;
		daysOfWeek = new DaysOfWeek(0);
		alert = Alarms.getAlarmAlertUri(context);
		if (alert == null) {
			silent = true;
		}
	}
//modify by zhanjiandong 2014 12 23
    public String getLabelOrDefault(Context context) {
//        if (label == null || label.length() == 0) {
            return context.getString(R.string.default_label);
//        }
//        return label;
    }

    //add by zhanjiandong 2014 12 23
    public String getRemakrs(Context context){
    	 if (label == null || label.length() == 0) {
           return context.getString(R.string.empty_label);
         }
    	 return label;
    }
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Alarm)) return false;
        final Alarm other = (Alarm) o;
        return id == other.id;
    }


    /*
     * Days of week code as a single int.
     * 0x00: no day
     * 0x01: Monday
     * 0x02: Tuesday
     * 0x04: Wednesday
     * 0x08: Thursday
     * 0x10: Friday
     * 0x20: Saturday
     * 0x40: Sunday
     */
    static final class DaysOfWeek {

        private static int[] DAY_MAP = new int[] {
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY,
        };
        
        private static String[] WEEK_CHINESE = new String[] {
        	"日",
        	"一",
        	"二",
        	"三",
        	"四",
        	"五",
        	"六"
        };
     

        // Bitmask of all repeating days
        private int mDays;

        DaysOfWeek(int days) {
            mDays = days;
        }

        public String toString(Context context, boolean showNever) {
            StringBuilder ret = new StringBuilder();

            // no days
            if (mDays == 0) {
                return showNever ?
                        context.getText(R.string.onlyonce).toString() : "";
            }

            // every day
            if (mDays == 0x7f) {
                return context.getText(R.string.every_day).toString();
            }
            //weekend
            if (mDays == 0x60) {
                return context.getText(R.string.weekend).toString();
            }
            //weekday
            if(mDays == 0x1f) {
            	return context.getText(R.string.weekday).toString();
            }
            
            //official weekday
            if(mDays == 0x9f) {
            	return context.getText(R.string.officialweekday).toString();
            }

            // count selected days
            int dayCount = 0, days = mDays;
            while (days > 0) {
                if ((days & 1) == 1) dayCount++;
                days >>= 1;
            }

            // short or long form?
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] dayList = (dayCount > 1) ?
                    dfs.getShortWeekdays() :
                    dfs.getWeekdays();

            // selected days
			for (int i = 0; i < 7; i++) {
				if ((mDays & (1 << i)) != 0) {
					// ret.append(dayList[DAY_MAP[i]]);
					// ret.append(WEEK_CHINESE[DAY_MAP[i]-1]);

					Resources res = context.getResources();

					String[] planets = res.getStringArray(R.array.weeked);

					ret.append(planets[DAY_MAP[i] - 1]);

					// Log.e("dayList = " + dayList[DAY_MAP[i]]);
					dayCount -= 1;
					if (dayCount > 0)
						ret.append(context.getText(R.string.day_concat));
				}
			}
			return ret.toString();
		}

        private boolean isSet(int day) {
            return ((mDays & (1 << day)) > 0);
        }

        public void set(int day, boolean set) {
            if (set) {
                mDays |= (1 << day);
            } else {
                mDays &= ~(1 << day);
            }
        }

        public void set(DaysOfWeek dow) {
            mDays = dow.mDays;
        }

        public int getCoded() {
            return mDays;
        }

        // Returns days of week encoded in an array of booleans.
        public boolean[] getBooleanArray() {
            boolean[] ret = new boolean[7];
            for (int i = 0; i < 7; i++) {
                ret[i] = isSet(i);
            }
            return ret;
        }

        public boolean isRepeatSet() {
            return mDays != 0;
        }

        //aurora add by tangjun 2013.12.30 start
//        int yearDayArray[]={0,25,30,31,32,33,34,35,36,38, 94,95,96, 120,121,122,123, 150,151,152, 
//        		248,249,250, 270,273,274,275,276,277,278,279, 283};
//		int dayTypeArray[]={3,1,3,3,3,3,3,3,3,1, 3,3,3, 3,3,3,1, 3,3,3, 
//				3,3,3, 1,3,3,3,3,3,3,3, 1};
        //aurora add by tangjun 2013.12.30 end
        //aurora modify by jadonZhan 2015 1 4
        int yearDayArray[]={0,1,3,//1月
        		45,48,49,50,53,54,58//2月
        		,95,//4月
        		120,//5月
        		172,//6月
        		273,274,277,278,279,282//10月
        		};
        //3代表放假的工作日，1代表上班的周末
		int dayTypeArray[]={3,3,1,
				1,3,3,3,3,3,1,
				3,
				3,
				3,
				3,3,3,3,3,1
				};       
        /**
         * @param c  判断法定节假日以及节假日变工作日的周末
         * @return
         */
        private int judgeHolidays(Calendar c, int dayCount) {
        	for ( int i = 0; i < yearDayArray.length; i++ ) {
        		if ( (c.get(Calendar.DAY_OF_YEAR) + dayCount - 1) == yearDayArray[i] ) {
        			if ( dayTypeArray[i] == 3 ) {
        				return 1;	//1代表节假日
        			} else if ( dayTypeArray[i] == 1) {
        				return 2;	//2代表上班的周末
        			}
        		}
        	}
        	return 0;
        }
        
        /**
         * returns number of days from today until next alarm
         * @param c must be set to today
         */
        public int getNextAlarm(Calendar c, int id) {
        	
        	boolean forwardflag = false;
        	
            if (mDays == 0) {
                return -1;
            }
            
            Calendar cc = Calendar.getInstance();
            cc.setTimeInMillis(System.currentTimeMillis());

            int nowHour = cc.get(Calendar.HOUR_OF_DAY);
            int nowMinute = cc.get(Calendar.MINUTE);

            int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            
            // if the alarm is behind current time and not need to alarm today, advance one day
            if ( !(c.get(Calendar.HOUR_OF_DAY) == nowHour && c.get(Calendar.MINUTE) == nowMinute)&& AlarmReceiver.wakeupAlarmId == id) {
            	forwardflag = true;
            }
            int day = 0;
            int dayCount = 0;
            do {
            	day = (today + dayCount) % 7;
            	//法定工作日模式
            	if ( mDays == 0x9f ) {
	            	int rtx = judgeHolidays( c, dayCount );
	            	if ( rtx == 2 || (isSet(day) && rtx == 0) ) {
	            		if ( !forwardflag ) {
	            			break;
	            		} else {
	            			forwardflag = false;
	            		}
	            	}
            	} else {
            		if (isSet(day)) {
            			if ( !forwardflag ) {
	            			break;
	            		} else {
	            			forwardflag = false;
	            		}
                    }
            	}
            	dayCount++;
			} while (true);
            return dayCount;
        }
        
        /**
         * returns number of days from today until next alarm
         * @param c must be set to today
         */
        public int getNextAlarm(Calendar c) {
            if (mDays == 0) {
                return -1;
            }

            int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            
            //Log.e("getNextAlarm ---c.get(Calendar.DAY_OF_WEEK) = " + c.get(Calendar.DAY_OF_WEEK));
            //Log.e("getNextAlarm ---c.get(Calendar.DAY_OF_YEAR) = " + c.get(Calendar.DAY_OF_YEAR));
            //Log.e("getNextAlarm ---c.get(Calendar.DAY_OF_MONTH) = " + c.get(Calendar.DAY_OF_MONTH));
            //Log.e("getNextAlarm ---c.get(Calendar.DAY_OF_WEEK_IN_MONTH) = " + c.get(Calendar.DAY_OF_WEEK_IN_MONTH));

            int day = 0;
            int dayCount = 0;
            do {
            	day = (today + dayCount) % 7;
            	//法定工作日模式
            	if ( mDays == 0x9f ) {
	            	int rtx = judgeHolidays( c, dayCount );
	            	if ( rtx == 2 || (isSet(day) && rtx == 0) ) {
	            		break;
	            	}
            	} else {
            		if (isSet(day)) {
                        break;
                    }
            	}
            	dayCount++;
			} while (true);
            return dayCount;
        }
        /*
        public int getNextAlarm(Calendar c) {
            if (mDays == 0) {
                return -1;
            }

            int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;

            int day = 0;
            int dayCount = 0;
            for (; dayCount < 7; dayCount++) {
                day = (today + dayCount) % 7;
                if (isSet(day)) {
                    break;
                }
            }
            return dayCount;
        }
        */
    }
}
