package com.aurora.powersaver.launcher;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期时间帮助类，获取的数据主要为int类型
 * 
 * @author dengchukun
 * 
 */
public class CalendarHelper {

    private static Calendar getCalendar() {
        Calendar c = Calendar.getInstance();
        return c;
    }

    /**
     * 获取当前年
     * 
     * @return
     */
    public static int getYear() {
        return getCalendar().get(Calendar.YEAR);
    }

    /**
     * 获取当前月
     * 
     * @return
     */
    public static int getMonth() {
        return getCalendar().get(Calendar.MONTH) + 1;
    }

    /**
     * 获取当前日
     * 
     * @return
     */
    public static int getDay() {
        return getCalendar().get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取当前是周几
     * 
     * @return 1 2 3 4 5 6 7
     */
    public static int getWeek() {
        int foreignWeek = getCalendar().get(Calendar.DAY_OF_WEEK);
        int chinaWeek = foreignWeek - 1;
        if (chinaWeek == 0) {
            chinaWeek = 7;
        }
        return chinaWeek;
    }

    /**
     * 获取当前时
     * 
     * @return
     */
    public static int getHour() {
        return getCalendar().get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取当前分
     * 
     * @return
     */
    public static int getMinute() {
        return getCalendar().get(Calendar.MINUTE);
    }

    /**
     * 获取当前秒
     * 
     * @return int类型的秒，如果需要更具体的秒可以调用System.currentTimeMillis()
     */
    public static int getSecond() {
        return getCalendar().get(Calendar.SECOND);
    }

    public static int getAMPM() {
        return getCalendar().get(Calendar.AM_PM);
    }

    /**
     * 获取当前时间，年月日时分秒，具体格式由字符串format决定
     * 
     * @param format
     *            决定当前时间显示的格式，如yyyy-MM-dd HH:mm:ss
     * @return 如上格式 将会返回2014-11-07 16:30:20
     */
    @SuppressLint("SimpleDateFormat")
    public static String getFormatDate(String format) {
        Date aDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String formattedDate = formatter.format(aDate);
        return formattedDate;
    }
}
