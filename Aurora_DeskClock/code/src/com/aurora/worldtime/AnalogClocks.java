package com.aurora.worldtime;

import java.util.TimeZone;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;

import com.android.deskclock.R;
//Gionee baorui 2013-01-09 modify for CR00756424 begin
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
//Gionee baorui 2013-01-09 modify for CR00756424 end

public class AnalogClocks extends View {
    private Time mCalendar;//declare a time

    private Drawable mHourHand;// hands of a clock image
    private Drawable mMinuteHand;//minute hand of a clock image
    private Drawable mDial;//dial image

    private int mDialWidth;//dial image width
    private int mDialHeight;//dial image height

    private float mMinutes;//the current Minutes correct to seconds
    private float mHour;//the current Hour correct to Minutes
    private boolean mChanged;//if time changed
    Resources resources;
    // Gionee baorui 2013-01-09 modify for CR00756424 begin
    private ContentObserver mFormatChangeObserver;
    private boolean mShowDay;
    private String mFormat;
    private final static String M12 = "hh:mm";
    final static String M24 = "kk:mm";
    // Gionee baorui 2013-01-09 modify for CR00756424 end

    public AnalogClocks(Context context) {
        this(context, null);
    }

    public AnalogClocks(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnalogClocks(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        resources = context.getResources();

		setClockImage(R.drawable.world_time_dial_black_white, R.drawable.world_time_hour_black_white, R.drawable.world_time_minute_black_white);
        
        mCalendar = new Time();

    }

    /*
     * load image
     * */
    public void setClockImage(int dial, int hour, int minute) {

        mDial = resources.getDrawable(dial);
        mHourHand = resources.getDrawable(hour);
        mMinuteHand = resources.getDrawable(minute);
        mDialWidth = mDial.getIntrinsicWidth();
        mDialHeight = mDial.getIntrinsicHeight();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Gionee baorui 2013-01-09 modify for CR00756424 begin
        mFormatChangeObserver = new FormatChangeObserver();
        getContext().getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true,
                mFormatChangeObserver);
        // Gionee baorui 2013-01-09 modify for CR00756424 end
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Gionee baorui 2013-01-09 modify for CR00756424 begin
        getContext().getContentResolver().unregisterContentObserver(mFormatChangeObserver);
        // Gionee baorui 2013-01-09 modify for CR00756424 end
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
            hScale = (float) widthSize / (float) mDialWidth;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
            vScale = (float) heightSize / (float) mDialHeight;
        }

        float scale = Math.min(hScale, vScale);

        setMeasuredDimension(
                resolveSize((int) (mDialWidth * scale), widthMeasureSpec),
                resolveSize((int) (mDialHeight * scale), heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChanged = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }

        final Drawable dial = mDial;
        int w = dial.getIntrinsicWidth();
        int h = dial.getIntrinsicHeight();
        int x = w / 2;
        int y = h / 2;
        boolean scaled = false;

        if (changed) {
            dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        dial.draw(canvas);

        canvas.save();
        canvas.rotate(mHour / 12.0f * 360.0f, x, y);
        final Drawable hourHand = mHourHand;
        if (changed) {
            w = hourHand.getIntrinsicWidth();
            h = hourHand.getIntrinsicHeight();
            hourHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y
                    + (h / 2));
        }
        hourHand.draw(canvas);
        /*
         * This call balances a previous call to save(), and is used to remove
         * all modifications to the matrix/clip state since the last save call.
         * It is an error to call restore() more times than save() was called.
         */
        canvas.restore();
        /*
         * Saves the current matrix and clip onto a private stack. Subsequent
         * calls to translate,scale,rotate,skew, concat or clipRect,clipPath
         * will all operate as usual, but when the balancing call to restore()
         * is made, those calls will be forgotten, and the settings that existed
         * before the save() will be reinstated.
         */
        canvas.save();
        canvas.rotate(mMinutes / 60.0f * 360.0f, x, y);

        final Drawable minuteHand = mMinuteHand;
        if (changed) {
            w = minuteHand.getIntrinsicWidth();
            h = minuteHand.getIntrinsicHeight();
            minuteHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y
                    + (h / 2));
        }
        minuteHand.draw(canvas);
        canvas.restore();

        if (scaled) {
            canvas.restore();
        }

    }

    /*
     * when time changed then update mMinutes and mHour
     * */
    private void onTimeChanged() {
        mCalendar.setToNow();

        int hour = mCalendar.hour;
        int minute = mCalendar.minute;
        int second = mCalendar.second;

        mMinutes = minute + second / 60.0f;
        mHour = hour + mMinutes / 60.0f;
        mChanged = true;
        invalidate();
    }
    

    /*
     *the entrance for change the view
     * */
    public Time setTimeZone(String name) {
        mCalendar = new Time(TimeZone.getTimeZone(name).getID());
        onTimeChanged();       
        return mCalendar;
    }

    // Gionee baorui 2013-01-09 modify for CR00756424 begin
    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            setDateFormat();
        }
    }

    public void setmShowDay(boolean mShowDay) {
        this.mShowDay = mShowDay;
    }

    public boolean ismShowDay() {
        return mShowDay;
    }

    private void setDateFormat() {
    	//aurora mod by tangjun 2014.1.21
        mFormat = get24HourMode(getContext()) ? M24 : M12;
    	//mFormat = M24;
        setmShowDay(mFormat == M12);
    }

    static boolean get24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }
    
    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
        setDateFormat();
    }
    // Gionee baorui 2013-01-09 modify for CR00756424 end
}
