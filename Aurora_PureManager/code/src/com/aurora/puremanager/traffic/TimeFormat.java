package com.aurora.puremanager.traffic;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;

public class TimeFormat {

    public static long changeMonthToUTC() {
        return 0;
    }

    public static long getStartTime(int year, int month, int day, int hour, int mins, int sec) {
        return covertToUTC(year, month, day, hour, mins, sec);
    }

    public static long getStartTime(String date, int hour, int mins, int sec) {
        return covertToUTC(date, hour, mins, sec);
    }

    private static long covertToUTC(String date, int hour, int mins, int sec) {
        long utcTime = 0;
        String tString = date + " " + Integer.toString(hour) + ":" + Integer.toString(mins) + ":"
                + Integer.toString(sec);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date d = format.parse(tString);
            utcTime = d.getTime();
        } catch (ParseException e) {

        }

        return utcTime;
    }

    private static long covertToUTC(int year, int month, int day, int hour, int mins, int sec) {
        long utcTime = 0;
        String tString = Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day)
                + " " + Integer.toString(hour) + ":" + Integer.toString(mins) + ":" + Integer.toString(sec);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(tString);
            utcTime = date.getTime();
        } catch (ParseException e) {

        }

        return utcTime;
    }

    public static String[] getDaysOfMonth(int[] mDateInterval) {

        int[] mDayOfMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if ((mDateInterval[0] % 4 == 0 && mDateInterval[0] % 100 != 0) || mDateInterval[0] % 400 == 0) {
            mDayOfMonth[1] = 29;
        }

        ArrayList<String> list = new ArrayList<String>();

        int index;
        String result = "";
        if (mDateInterval[1] != mDateInterval[4]) {
            int size = mDayOfMonth[mDateInterval[1] - 1];
            for (index = mDateInterval[2]; index <= size; index++) {
                result = mDateInterval[0] + "-" + mDateInterval[1] + "-" + index;
                list.add(result);
            }

            for (index = 1; index <= mDateInterval[5]; index++) {
                result = mDateInterval[3] + "-" + mDateInterval[4] + "-" + index;
                list.add(result);
            }
        } else {
            for (index = mDateInterval[2]; index <= mDateInterval[5]; index++) {
                result = mDateInterval[0] + "-" + mDateInterval[1] + "-" + index;
                list.add(result);
            }
        }

        String[] days = (String[]) list.toArray(new String[0]);

        return days;
    }

    public static String[] getHoursOfDay() {
        String[] hours = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
                "15", "16", "17", "18", "19", "20", "21", "22", "23"/* , "24" */};

        return hours;
    }

    public static int getStartDay(Context context, int simId) {
        SharedPreferences sp = context.getSharedPreferences("traffic_preference" + simId,
                Context.MODE_MULTI_PROCESS);
        return Integer.parseInt(sp.getString("START_DATE", "1"));
    }

    public static int[] getNowTimeArray() {
        Calendar cal = Calendar.getInstance();
        int[] times = new int[6];
        times[0] = cal.get(Calendar.YEAR);
        times[1] = cal.get(Calendar.MONTH);
        times[2] = cal.get(Calendar.DAY_OF_MONTH);
        times[3] = cal.get(Calendar.HOUR_OF_DAY);
        times[4] = cal.get(Calendar.MINUTE);
        times[5] = cal.get(Calendar.SECOND);
        return times;
    }

    public static String[] getWeekArray() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date = format.format(System.currentTimeMillis());
        Date d = null;
        Calendar cal = Calendar.getInstance();
        try {
            d = format.parse(date);
            cal.setTime(d);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int[] types = {Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY};
        String[] timeArray = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            cal.set(Calendar.DAY_OF_WEEK, types[i]);
            timeArray[i] = format.format(cal.getTime());
        }

        return timeArray;
    }

    public static String[] getNotificationWeekArray() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String[] dateZone = new String[8];
        Long time = System.currentTimeMillis();
        for (int i = 7; i >= 0; i--) {
            dateZone[i] = dateFormat.format(time);
            time -= 24 * 60 * 60 * 1000;
        }

        return dateZone;
    }
    
    /**
     * 获取距离月结日剩余的天数
     * @param monthEndDate 月结日
     * @return
     */
    public static int getRemainderDaysToMonthEndDate(final int monthEndDate){	
    	int cur_day,cur_month,cur_year;//当前日，月，年
    	int end_day,end_month,end_year;//月结日，月，年
    	
    	Calendar cal = Calendar.getInstance();
    	cur_day = cal.get(Calendar.DATE);
        cur_month = cal.get(Calendar.MONTH)+1;//月份的起始值为０而不是１，所以要设置八月时，我们用７而不是8。
        cur_year = cal.get(Calendar.YEAR);
        
        //确定月结月，月结年
        if(cur_day >= monthEndDate){
        	end_month = cur_month+1;
        	if(end_month > 12){
        		end_month = 1;
        		end_year = cur_year+1;
        	}else{
        		end_year = cur_year;
        	}
        }else{
        	end_month = cur_month;
        	end_year = cur_year;
        }
               
        //确定月结日
        int end_month_last_day = getMonthLastDay(end_year,end_month);
        if(monthEndDate>end_month_last_day){
        	end_day = end_month_last_day;
        }else{
        	end_day = monthEndDate;
        }
        
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.set(cur_year, cur_month-1, cur_day,0,0,0);//月份的起始值为０而不是１
        calendar2.set(end_year, end_month-1, end_day,0,0,0);//月份的起始值为０而不是１
        long milliseconds1 = calendar1.getTimeInMillis();
        long milliseconds2 = calendar2.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;
        return (int)(diff/(24*60*60*1000));
    }
    
    /**
     * 得到指定月的天数
     * */
    public static int getMonthLastDay(int year, int month){
	    Calendar a = Calendar.getInstance();	
	    a.set(Calendar.YEAR, year);	
	    a.set(Calendar.MONTH, month-1);//月份的起始值为０而不是１
	    a.set(Calendar.DATE, 1);//把日期设置为当月第一天	
	    a.roll(Calendar.DATE, -1);//日期回滚一天，也就是最后一天	
	    int maxDate = a.get(Calendar.DATE);	
	    return maxDate;
    }
    
}