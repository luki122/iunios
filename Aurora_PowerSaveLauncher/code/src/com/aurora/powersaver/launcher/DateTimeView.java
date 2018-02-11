package com.aurora.powersaver.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeView extends LinearLayout {

    private Time mCalendar;
    private boolean mAttached;
    private final Handler mHandler = new Handler();

    private TextView mTimeView;
    private TextView mDateView;
    private TextView mHourAMPMZHTextView;
    private TextView mHourAMPMENTextView;
    private String[] mMonth;
    private static Typeface mClockTypeface = null;
    private static final String ANDROID_CLOCK_FONT_FILE = "/system/fonts/AndroidClock.ttf";

    public DateTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCalendar = new Time();
        mMonth = context.getResources().getStringArray(R.array.month);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTimeView = (TextView) findViewById(R.id.tv_time);
        mDateView = (TextView) findViewById(R.id.tv_date);
        mHourAMPMZHTextView = (TextView) findViewById(R.id.tv_am_pm_zh);
        mHourAMPMENTextView = (TextView) findViewById(R.id.tv_am_pm_en);
        mTimeView.setTypeface(getClockTypeface());
    }

    private Typeface getClockTypeface() {
        if (mClockTypeface == null) {
            mClockTypeface = Typeface.createFromFile(ANDROID_CLOCK_FONT_FILE);
        }
        return mClockTypeface;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();

            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

            getContext().registerReceiver(mIntentReceiver, filter, null, mHandler);
        }

        mCalendar = new Time();
        onTimeChanged();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            getContext().unregisterReceiver(mIntentReceiver);
            mAttached = false;
        }
    }

    private void onTimeChanged() {
        mCalendar.setToNow();

        boolean is24HourFormat = DateFormat.is24HourFormat(getContext());
        String hourString = CalendarUtil.getHour();

        if (!is24HourFormat) {
            Date d = new Date();
            SimpleDateFormat ss = new SimpleDateFormat("hh");
            hourString = ss.format(d);

            if (isZH()) {
                mHourAMPMENTextView.setVisibility(View.GONE);

                mHourAMPMZHTextView.setVisibility(View.VISIBLE);
                if (CalendarUtil.getAMPM().equals("AM")) {
                    mHourAMPMZHTextView.setText(R.string.am);
                } else {
                    mHourAMPMZHTextView.setText(R.string.pm);
                }
            } else {
                mHourAMPMZHTextView.setVisibility(View.GONE);
                mHourAMPMENTextView.setVisibility(View.VISIBLE);
                mHourAMPMENTextView.setText(CalendarUtil.getAMPM());
            }
        } else {
            mHourAMPMENTextView.setVisibility(View.GONE);
            mHourAMPMZHTextView.setVisibility(View.GONE);
        }

        int hour = Integer.valueOf(hourString);
        hourString = String.valueOf(hour);

        StringBuffer time = new StringBuffer();
        // time hour
        time.append(hourString);
        time.append(":");
        time.append(CalendarUtil.getMinute());
        // time minute
        mTimeView.setText(time.toString());

        // date
        StringBuffer date = new StringBuffer();
        String month = mMonth[mCalendar.month];
        date.append(String.format(month, CalendarUtil.getDay()));
        // week
        date.append(CalendarUtil.getWeek(getContext(), R.array.weekday));
        mDateView.setText(date.toString());
    }

    private boolean isZH() {
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh")) {
            return true;
        } else {
            return false;
        }
    }

    /*
    private void onTimeChanged() {
        mCalendar.setToNow();

        // time
        mTimeViewHour.setText(CalendarUtil.getHour());
        mTimeView.setText(FENHAO + CalendarUtil.getMinute());
        
        // date
        StringBuffer date = new StringBuffer();
        String month = mMonth[mCalendar.month];

        date.append(String.format(month, CalendarUtil.getDay()));
        date.append("    ");
        mDateView.setText(date.toString());

        // week
        StringBuffer week = new StringBuffer();
        week.append(CalendarUtil.getWeek(getContext(),R.array.weekday));
        mDateView.setText(week.toString());
    }
    */
    //Gionee <yangxinruo> <2015-07-31> modify for CR01529084 end
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
            }
            onTimeChanged();
            invalidate();
        }
    };
}
