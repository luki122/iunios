package com.aurora.change.view;

import java.util.Calendar;
import java.util.Date;

import com.aurora.change.R;
import com.aurora.change.utils.LunarUtils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TimeLayout extends LinearLayout {

    private static final String TAG = "TimeLayout";

    private static final String DATE_FORMAT = "MM-dd";
    private static final String DATE_MONTH_FORMAT = "MM";
    private static final String DATE_DAY_FORMAT = "dd";
    private static final boolean USE_UPPER_CASE = true;

    public static final int LOCK_ICON = 0; // R.drawable.ic_lock_idle_lock;
//    public static final int ALARM_ICON = R.drawable.ic_lock_idle_alarm;
    public static final int CHARGING_ICON = 0; // R.drawable.ic_lock_idle_charging;
    public static final int BATTERY_LOW_ICON = 0; // R.drawable.ic_lock_idle_low_battery;

    private CharSequence mDateFormatString;

    private TextView mDateMonthView;
    private TextView mDateDivView;
    private TextView mDateDayView;
    private TextView mAlarmStatusView;
    //private ClockView mClockView;
	private ClockImageView mClockImageView1;
	private ClockImageView mClockImageView2;
	private ClockImageView mClockImageView3;
	private ClockImageView mClockImageView4;
	private ClockImageView mClockImageView5;

    private TextView mLunarDateView;
    private TextView mWeekView;

    private Typeface mClockFace;
    private Typeface mDateFace;
    private Typeface mWeekFace;

    private Context mContext;

    private static final String AURORA_CLOCK_FONT_FILE = "/system/fonts/Roboto-Light.ttf";
    private static final String AURORA_DATE_FONT_FILE = "/system/fonts/Roboto-Light.ttf";
    private static final String AURORA_WEEK_FONT_FILE = "/system/fonts/DroidSans.ttf";

    public TimeLayout(Context context) {
        this(context, null);
    }

    public TimeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Resources res = getContext().getResources();
        mDateFormatString = DATE_FORMAT;
//                res.getText(R.string.abbrev_wday_month_day_no_year);
//        mDateView = ( TextView ) findViewById(R.id.date);
        mDateMonthView = ( TextView ) findViewById(R.id.date_month);
        mDateDivView = ( TextView ) findViewById(R.id.date_div);
        mDateDayView = ( TextView ) findViewById(R.id.date_day);
        mAlarmStatusView = ( TextView ) findViewById(R.id.alarm_status);
        //mClockView = ( ClockView ) findViewById(R.id.clock_text);
		mClockImageView1 = (ClockImageView)findViewById(R.id.clock_image_1);
		mClockImageView2 = (ClockImageView)findViewById(R.id.clock_image_2);
		mClockImageView3 = (ClockImageView)findViewById(R.id.clock_image_3);
		mClockImageView4 = (ClockImageView)findViewById(R.id.clock_image_4);
		mClockImageView5 = (ClockImageView)findViewById(R.id.clock_image_5);

//        mLunarDateView = ( TextView ) findViewById(R.id.lunar);
//        mLunarDateView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);

        mWeekView = ( TextView ) findViewById(R.id.week);

        // Use custom font in mClockView/mDateView/mWeekView
        try {
            mClockFace = Typeface.createFromFile(AURORA_CLOCK_FONT_FILE);
            mDateFace = Typeface.createFromFile(AURORA_DATE_FONT_FILE);
            mWeekFace = Typeface.createFromFile(AURORA_WEEK_FONT_FILE);
            //mClockView.setTypeface(mClockFace);
            mDateMonthView.setTypeface(mDateFace);
            mDateDivView.setTypeface(mDateFace);
            mDateDayView.setTypeface(mDateFace);
            mWeekView.setTypeface(mWeekFace);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDateDivView.setText("-");

        // Required to get Marquee to work.
        final View marqueeViews[] = {mDateMonthView, mDateDayView, mWeekView, mAlarmStatusView};
        for (int i = 0; i < marqueeViews.length; i++) {
            View v = marqueeViews[i];
            if (v == null) {
                throw new RuntimeException("Can't find widget at index " + i);
            }
            v.setSelected(true);
        }
        refresh(0);
    }

    public void refresh(int index) {
//        mClockView.updateTime(index);
        refreshDate(index);
//        refreshLunarDate();
//        refreshAlarmStatus(); // might as well
        refreshWeek(index);
    }
    
    public void setBlackStyle(boolean bool, int color) {
		mClockImageView1.setBlackStyle(bool);
		mClockImageView2.setBlackStyle(bool);
		mClockImageView3.setBlackStyle(bool);
		mClockImageView4.setBlackStyle(bool);
		mClockImageView5.setBlackStyle(bool);
    	setTextColor(color);
	}
    
    private void setTextColor(int color) {
		mDateMonthView.setTextColor(color);
		mDateDivView.setTextColor(color);
		mDateDayView.setTextColor(color);
		mWeekView.setTextColor(color);
	}
    
    void refreshAlarmStatus() {
        // Update Alarm status
        String nextAlarm = Settings.System.getString(mContext.getContentResolver(),
                Settings.System.NEXT_ALARM_FORMATTED);
        if (!TextUtils.isEmpty(nextAlarm)) {
            maybeSetUpperCaseText(mAlarmStatusView, nextAlarm);
//            mAlarmStatusView.setCompoundDrawablesWithIntrinsicBounds(ALARM_ICON, 0, 0, 0);
            mAlarmStatusView.setVisibility(View.GONE);
        } else {
            mAlarmStatusView.setVisibility(View.GONE);
        }
    }

    void refreshDate(int index) {
//        maybeSetUpperCaseText(mDateView, DateFormat.format(mDateFormatString, new Date()));
//        maybeSetUpperCaseText(mDateMonthView, mDateDayView, DateFormat.format(DATE_MONTH_FORMAT, new Date()),
//                DateFormat.format(DATE_DAY_FORMAT, new Date()));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (isChangeDate(index)) {
            calendar.add(Calendar.DATE, 1);
        }
        Date date = calendar.getTime();
        maybeSetUpperCaseText(mDateMonthView, mDateDayView, DateFormat.format(mDateFormatString, date));
    }

    void refreshLunarDate(int index) {
        setLunarDate(mLunarDateView);
    }

    void refreshWeek(int index) {
        setWeek(index);
    }

    private void maybeSetUpperCaseText(TextView textView, CharSequence text) {
        if (USE_UPPER_CASE) {
            textView.setText(text != null ? text.toString().toUpperCase() : null);
        } else {
            textView.setText(text);
        }
    }

    private void maybeSetUpperCaseText(TextView monthView, TextView dayView, CharSequence text) {
        try {
            String date = "";
            String time[] = null;
            if (USE_UPPER_CASE) {
                date = text.toString().toUpperCase();
            } else {
                date = text.toString();
            }
            if (date.length() > 0) {
                Log.d(TAG, "date=" + date);
                time = date.split("-");
                monthView.setText(time[0]);
                dayView.setText(time[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void maybeSetUpperCaseText(TextView monthView, TextView dayView, CharSequence monthText,
            CharSequence dayText) {
        if (USE_UPPER_CASE) {
            monthView.setText(monthText != null ? monthText.toString().toUpperCase() : null);
            dayView.setText(dayText != null ? dayText.toString().toUpperCase() : null);
        } else {
            monthView.setText(monthText);
            dayView.setText(dayText);
        }
    }

    private void setLunarDate(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        Time tCalendar = null;
        if (tCalendar == null) {
            tCalendar = new Time(calendar.getTimeZone().getID());
        }
        long now = System.currentTimeMillis();
        tCalendar.set(now);
        LunarUtils lunar = new LunarUtils(mContext);
        lunar.SetSolarDate(tCalendar);

        String chinaDate = lunar.GetLunarNYRString();
        String shortDate = lunar.GetLunarDateString();
        shortDate = shortDate.trim();
        String nlString = "";

        if (chinaDate != null) {
            if (chinaDate.substring(10, 11).equalsIgnoreCase(" ")) {
                nlString = chinaDate.substring(3, 10);
            } else {
                nlString = chinaDate.substring(3);
                if (shortDate != null) {
                    if (chinaDate.substring(7).contains(shortDate)) {
                        nlString = shortDate;
                    }
                }
            }
        }
        textView.setText(nlString);
    }

    private void setWeek(int index) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (isChangeDate(index)) {
            calendar.add(Calendar.DATE, 1);
        }
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        String[] dayOfWeek = mContext.getResources().getStringArray(R.array.day_of_week);
        if (week > 0 && dayOfWeek != null) {
            mWeekView.setText(dayOfWeek[week - 1]);
        }
    }

    private boolean isChangeDate(int index) {
        boolean bool = false;
        if (index > 8) {
            bool = true;
        } else {
            bool = false;
        }
        return bool;
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mClockImageView1.onPageScrolled(position, positionOffset, positionOffsetPixels);
		mClockImageView2.onPageScrolled(position, positionOffset, positionOffsetPixels);
		mClockImageView3.onPageScrolled(position, positionOffset, positionOffsetPixels);
		mClockImageView4.onPageScrolled(position, positionOffset, positionOffsetPixels);
		mClockImageView5.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    public void onPageSelected(int position) {
        onPageSelected(position, false);
    }

    public void onPageSelected(int position, boolean isSingle) {
        refresh(position);
        //mClockView.onPageSelected(position);
        mClockImageView1.onPageSelected(position);
		mClockImageView2.onPageSelected(position);
		//mClockImageView3.onPageSelected(position);
		mClockImageView4.onPageSelected(position);
		mClockImageView5.onPageSelected(position);
    }

    public void setSingle(boolean bool) {
        //mClockView.setSingle(bool);
        mClockImageView1.setSingle(bool);
		mClockImageView2.setSingle(bool);
		
		//shigq add start
		mClockImageView3.setSingle(bool);
		//shigq add end
		
		mClockImageView4.setSingle(bool);
		mClockImageView5.setSingle(bool);
    }
    
    public void updateClock() {
    	mClockImageView1.updateSingleTime();
		mClockImageView2.updateSingleTime();
		
		//shigq add start
		mClockImageView3.updateSingleTime();
		//shigq add end
		
		mClockImageView4.updateSingleTime();
		mClockImageView5.updateSingleTime();
	}
}
