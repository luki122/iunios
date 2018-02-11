/*
 * Copyright (C) 2008 The Android Open Source Project
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
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;

import com.android.systemui.R;

import java.util.Date;

// Gionee <fengjianyi><2013-05-10> add for CR00800567 start
import com.android.systemui.statusbar.util.ToolbarIconUtils;
// Gionee <fengjianyi><2013-05-10> add for CR00800567 end
// Aurora <zhanggp> <2013-11-02> added for systemui begin
import android.content.res.TypedArray;
// Aurora <zhanggp> <2013-11-02> added for systemui end

public class DateView extends TextView {
    private static final String TAG = "DateView";

    private boolean mAttachedToWindow;
    private boolean mWindowVisible;
    private boolean mUpdating;

	// Aurora <zhanggp> <2013-11-02> added for systemui begin
	private int mStyle = 0;
	// Aurora <zhanggp> <2013-11-02> added for systemui end
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_TIME_TICK.equals(action)
                    || Intent.ACTION_TIME_CHANGED.equals(action)
                    || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
                updateClock();
            }
        }
    };
	// Aurora <zhanggp> <2013-11-02> modified for systemui begin
    public DateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

	public DateView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, com.android.systemui.R.styleable.DateDisplay,
                defStyle, 0);
		mStyle = a.getInt(com.android.systemui.R.styleable.DateDisplay_dateStyle, mStyle);
		a.recycle();
	}
	/*
    public DateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
	*/
	// Aurora <zhanggp> <2013-11-02> modified for systemui end
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
        setUpdates();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttachedToWindow = false;
        setUpdates();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mWindowVisible = visibility == VISIBLE;
        setUpdates();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        setUpdates();
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        // makes the large background bitmap not force us to full width
        return 0;
    }

    public void updateClock() {
        // Gionee <fengjianyi><2013-05-10> modify for CR00800567 start
        /*
        final String dateFormat = getContext().getString(R.string.abbrev_wday_month_day_no_year);
        setText(DateFormat.format(dateFormat, new Date()));
        */
    	if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
            final String dateFormat = getContext().getString(R.string.zzzzz_gn_abbrev_wday_month_day_no_year);
            setText(DateFormat.format(dateFormat, new Date()));
    	} else {
		    // Aurora <zhanggp> <2013-10-08> modified for systemui begin
            String dateFormat;
			if(mStyle == 1){
				dateFormat = getContext().getString(R.string.aurora_abbrev_wday_month_day_no_week);
			}else if(mStyle == 2){
				dateFormat = getContext().getString(R.string.aurora_abbrev_wday_month_day_no_mouthday);
			}else{
				dateFormat = getContext().getString(R.string.aurora_abbrev_wday_month_day_no_year);
			}
            //final String dateFormat = getContext().getString(R.string.abbrev_wday_month_day_no_year);
			// Aurora <zhanggp> <2013-10-08> modified for systemui end
            setText(DateFormat.format(dateFormat, new Date()));
    	}
        // Gionee <fengjianyi><2013-05-10> modify for CR00800567 end
    }

    private boolean isVisible() {
        View v = this;
        while (true) {
            if (v.getVisibility() != VISIBLE) {
                return false;
            }
            final ViewParent parent = v.getParent();
            if (parent instanceof View) {
                v = (View)parent;
            } else {
                return true;
            }
        }
    }

    private void setUpdates() {
        boolean update = mAttachedToWindow && mWindowVisible && isVisible();
        if (update != mUpdating) {
            mUpdating = update;
            if (update) {
                // Register for Intent broadcasts for the clock and battery
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_TIME_TICK);
                filter.addAction(Intent.ACTION_TIME_CHANGED);
                filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
                mContext.registerReceiver(mIntentReceiver, filter, null, null);
                updateClock();
            } else {
                mContext.unregisterReceiver(mIntentReceiver);
            }
        }
    }
}
