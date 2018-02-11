package com.mediatek.contacts.calllog;

import com.android.contacts.R;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

public class CallLogDateFormatHelper {

    private static final String TAG = "CallLogDateFormatHelper";
    protected final static long ONE_DAY_IN_MILLISECONDS = 86400000l;
    protected final static long MONTH_SHIFT_BIT = 8l;
    protected final static long YEAR_SHIFT_BIT = 16l;

    private static String msToday = "";
    private static String msYesterday = "";

    private static long mslformattedToday;
    private static long mslformattedYesterday;

    private static HashMap<Long, Long> msMapDate;
    private static HashMap<Long, String> msMapDateToString;

    private static boolean bInitilized = false;

    private static long getFormattedDate(long milliSeconds) {
        if (!bInitilized) {
            bInitilized = true;
            refreshData();
        }
        Long ret = msMapDate.get(milliSeconds);

        if (null == ret) {
            Date date = new Date(milliSeconds);
            ret = (long)((date.getYear() << YEAR_SHIFT_BIT) 
                        + ((date.getMonth() + 1) << MONTH_SHIFT_BIT) + date.getDate());
            msMapDate.put(milliSeconds, ret);
        }

        return ret;
    }

    private CallLogDateFormatHelper() {
    }

    // Today, Yesterday, MM/dd/yyyy
    public static String getFormatedDateText(Context context, final long lDate) {
        String retDate = null;
        if (lDate <= 0) {
            log("getSectionHeadText lDate:" + lDate);
            return retDate;
        }
        log("getSectionHeadText lDate:" + lDate);
        long lfmtdate = getFormattedDate(lDate);

        // Get string Today, Yesterday or not
        if (lfmtdate == mslformattedToday) {
            if ("".equals(msToday)) {
                msToday = context.getResources().getString(R.string.calllog_today);
            }
            retDate = msToday;
        } else if (lfmtdate == mslformattedYesterday) {
            if ("".equals(msYesterday)) {
                msYesterday = context.getResources().getString(R.string.calllog_yesterday);
            }
            retDate = msYesterday;
        } else {
            retDate = getDateString(lDate);
        }
        log("getFormatedDateText()  retDate===" +retDate);
        return retDate;
    }

    public static boolean isSameDay(final long firstDate, final long secondDate) {
        boolean bRet = false;

        long first = getFormattedDate(firstDate);
        long second = getFormattedDate(secondDate);

        bRet = (first == second);
//        log("isSameDay() : compareDateByDay firstDate[" + firstDate + "] secondDate[" + secondDate + "]");
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//        log("isSameDay() : compareDateByDay sd11[" + sdf.format(firstDate) + "] sd2[" + sdf.format(secondDate) + "]");
//        bRet = sdf.format(firstDate).equals(sdf.format(secondDate));
        return bRet;
    }

    private static void log(String log) {
        Log.i(TAG, log);
    }

    private static final int MAX_HASH_MAP_SIZE = 500;
    public static void refreshData() {
        long curtime = System.currentTimeMillis();

        if (null == msMapDate) {
            msMapDate = new HashMap<Long, Long>();
        }
        if (null == msMapDateToString) {
            msMapDateToString = new HashMap<Long, String>();
        }
        mslformattedToday = getFormattedDate(curtime);
        mslformattedYesterday = getFormattedDate(curtime - ONE_DAY_IN_MILLISECONDS);

		msToday = "";
		msYesterday = "";
        if (msMapDate.size() > MAX_HASH_MAP_SIZE) {
            msMapDate.clear();
        }
        if (msMapDateToString.size() > MAX_HASH_MAP_SIZE) {
            msMapDateToString.clear();
        }
        bInitilized = true;
    }

    private static String getDateString(long milliSeconds) {
        String ret = msMapDateToString.get(milliSeconds);

        if (null == ret) {
            Date date = new Date(milliSeconds);
            ret = DateFormat.format("MM/dd/yyyy", milliSeconds).toString();
            msMapDateToString.put(milliSeconds, ret);
        }

        return ret;
    }
}