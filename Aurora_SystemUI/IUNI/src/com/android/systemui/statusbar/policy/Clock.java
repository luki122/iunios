/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.View;
import android.widget.TextView;

import com.aurora.R;

import com.gionee.featureoption.FeatureOption;
import com.android.systemui.Xlog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

// Aurora <zhanggp> <2013-10-23> added for systemui begin
import com.android.systemui.statusbar.util.AuroraFontUtils;
// Aurora <zhanggp> <2013-10-23> added for systemui end
/**
 * This widget display an analogic clock with two hands for hours and
 * minutes.
 */
public class Clock extends TextView {
    private static final String TAG = "Clock";

    private boolean mAttached;
    private Calendar mCalendar;
    private String mClockFormatString;
    private SimpleDateFormat mClockFormat;

    private static final int AM_PM_STYLE_NORMAL  = 0;
    private static final int AM_PM_STYLE_SMALL   = 1;
    private static final int AM_PM_STYLE_GONE    = 2;
// Aurora <zhanggp> <2013-10-17> modified for systemui begin
	private static final int AM_PM_STYLE_EN    = 3;
    private int AM_PM_STYLE = AM_PM_STYLE_EN;
// Aurora <zhanggp> <2013-10-17> modified for systemui end
    
    private int leftPadding = 0;
    private int topPadding = 0;
    
    public Clock(Context context) {
        this(context, null);
    }

    public Clock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Clock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
		// Aurora <zhanggp> <2013-10-17> added for systemui begin
        TypedArray a = context.obtainStyledAttributes(attrs, com.android.systemui.R.styleable.ClockDisplay,
                defStyle, 0);
		AM_PM_STYLE = a.getInt(com.android.systemui.R.styleable.ClockDisplay_displayStyle, AM_PM_STYLE);
		a.recycle();
		// Aurora <zhanggp> <2013-10-17> added for systemui end
//		auroraSetDefaultBaseLinePadding();
	    leftPadding = mContext.getResources()
        .getDimensionPixelOffset(com.android.systemui.R.dimen.status_bar_clock_padding_left);
	    topPadding = mContext.getResources()
        .getDimensionPixelOffset(com.android.systemui.R.dimen.status_bar_clock_padding_top);
	    IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);

        context.registerReceiver(mIntentReceiver, filter, null, getHandler());
        mCalendar = Calendar.getInstance(TimeZone.getDefault());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        /*if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();

            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);

            getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
			// Aurora <zhanggp> <2013-10-23> added for systemui begin
			//AuroraFontUtils.setNumType(this);
			// Aurora <zhanggp> <2013-10-23> added for systemui end
        }*/

        // NOTE: It's safe to do these after registering the receiver since the receiver always runs
        // in the main thread, therefore the receiver can't run before this method returns.

        // The time zone may have changed while the receiver wasn't registered, so update the Time
//        mCalendar = Calendar.getInstance(TimeZone.getDefault());

        // Make sure we update to the current time
        updateClock();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        if (mAttached) {
//            getContext().unregisterReceiver(mIntentReceiver);
//            mAttached = false;
//        }
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Xlog.d(TAG, "action =" + action);
            if (action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = Calendar.getInstance(TimeZone.getTimeZone(tz));
                if (mClockFormat != null) {
                    mClockFormat.setTimeZone(mCalendar.getTimeZone());
                }
                Xlog.d(TAG, "mCalendar =" + mCalendar + "TimeZone.getTimeZone(tz) =" + TimeZone.getTimeZone(tz));
            }
            updateClock();
        }
    };

    final void updateClock() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        
        if(DateFormat.is24HourFormat(getContext())){
        	setPadding(leftPadding,0,0,0);
        }else{
        	setPadding(leftPadding,topPadding,0,0);
        }
        setText(getSmallTime());
    }

    private final CharSequence getSmallTime() {
        Context context = getContext();
        boolean b24 = DateFormat.is24HourFormat(context);
        int res;
		// Aurora <zhanggp> <2013-10-17> modified for systemui begin
        if (b24) {
            res = com.android.systemui.R.string.aurora_twenty_four_hour_time_format;
        } else {
            res = com.android.systemui.R.string.aurora_twelve_hour_time_format;
        }
		/*
        if (b24) {
            res = R.string.twenty_four_hour_time_format;
        } else {
            res = R.string.twelve_hour_time_format;
        }
		*/
		// Aurora <zhanggp> <2013-10-17> modified for systemui begin
        final char MAGIC1 = '\uEF00';
        final char MAGIC2 = '\uEF01';

        SimpleDateFormat sdf;
        String format = context.getString(res);
        if (!format.equals(mClockFormatString)) {
            /*
             * Search for an unquoted "a" in the format string, so we can
             * add dummy characters around it to let us find it again after
             * formatting and change its size.
             */
            if (AM_PM_STYLE != AM_PM_STYLE_NORMAL) {
                int a = -1;
                boolean quoted = false;
                for (int i = 0; i < format.length(); i++) {
                    char c = format.charAt(i);

                    if (c == '\'') {
                        quoted = !quoted;
                    }
                    if (!quoted && c == 'a') {
                        a = i;
                        break;
                    }
                }

                if (a >= 0) {
                    // Move a back so any whitespace before AM/PM is also in the alternate size.
                    final int b = a;
                    while (a > 0 && Character.isWhitespace(format.charAt(a-1))) {
                        a--;
                    }
                    format = format.substring(0, a) + MAGIC1 + format.substring(a, b)
                        + "a" + MAGIC2 + format.substring(b + 1);
                }
            }
            mClockFormat = sdf = new SimpleDateFormat(format);
            mClockFormatString = format;
        } else {
            sdf = mClockFormat;
        }
        String result = sdf.format(mCalendar.getTime());

        if (AM_PM_STYLE != AM_PM_STYLE_NORMAL) {
            int magic1 = result.indexOf(MAGIC1);
            int magic2 = result.indexOf(MAGIC2);
            if (magic1 >= 0 && magic2 > magic1) {
                SpannableStringBuilder formatted = new SpannableStringBuilder(result);
                if (AM_PM_STYLE == AM_PM_STYLE_GONE) {
                    formatted.delete(magic1, magic2+1);
				// Aurora <zhanggp> <2013-10-17> added for systemui begin
                } else if (AM_PM_STYLE == AM_PM_STYLE_EN) {
                    formatted.delete(magic1, magic2+1);
					String ampmStr = null;
					if(Calendar.AM == mCalendar.get(Calendar.AM_PM)){
						ampmStr = "AM";
					}else{
						ampmStr = "PM";
					}
					formatted.append(ampmStr);
				// Aurora <zhanggp> <2013-10-17> added for systemui begin	
				} else {
                    if (AM_PM_STYLE == AM_PM_STYLE_SMALL) {
                        CharacterStyle style = new RelativeSizeSpan(0.7f);
                        formatted.setSpan(style, magic1, magic2,
                                          Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                    formatted.delete(magic2, magic2 + 1);
                    formatted.delete(magic1, magic1 + 1);
                }
                return formatted;
            }
        }
 
        return result;

    }
}

