package com.aurora.powersaver.launcher;

import android.content.Context;

/**
 * 日期时间帮助类，主要为String类型的数据
 * 
 * @author dengchukun
 * 
 */
public class CalendarUtil {

    private static String[] WEEK_ZHOU = { "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    private static String[] WEEK_XINGQI = { "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期天"};

    /**
     * 得到周几
     * 
     * @return ”周一“格式
     */
    public static String getWeekNamedByZhou() {
        return WEEK_ZHOU[CalendarHelper.getWeek() - 1];
    }

    /**
     * 得到星期几
     * 
     * @return ”星期一“格式
     */
    public static String getWeekNamedByXingQi() {
        return WEEK_XINGQI[CalendarHelper.getWeek() - 1];
    }

    public static String getHour() {
        int hour = CalendarHelper.getHour();
        String hourStr = "" + hour;
        if (hour < 10) {
            hourStr = "0" + hour;
        }
        return hourStr;
    }

    public static String getMinute() {
        int minute = CalendarHelper.getMinute();
        String minuteStr = "" + minute;
        if (minute < 10) {
            minuteStr = "0" + minute;
        }
        return minuteStr;
    }

   
    public static String getDay() {
        int day = CalendarHelper.getDay();
        String dayStr = "" + day;
        return dayStr;
    }

    public static String getAMPM() {
        int amPm = CalendarHelper.getAMPM();
        String amOrPm = "";
        if (amPm == 0) {
            amOrPm = "AM";
        } else {
            amOrPm = "PM";
        }
        return amOrPm;
    }

    public static String getDate() {
        return CalendarHelper.getFormatDate("yyyy.MM.dd");
    }

    public static String getDate1() {
        return CalendarHelper.getFormatDate("yyyy/MM/dd");
    }
    
    public static String getWeek(Context context, int arrayNameID) {
        int weekday = CalendarHelper.getWeek();
        String[] weekStr = context.getResources().getStringArray(arrayNameID);
        return weekStr[weekday - 1];
    }
}
