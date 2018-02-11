/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.calendar.month;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.provider.CalendarContract.Attendees;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import com.android.calendar.Event;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.aurora.calendar.AuroraCalendarViewFilterActivity;
import com.aurora.calendar.period.PeriodInfo;
import com.gionee.legalholiday.ILegalHoliday;
import com.gionee.legalholiday.LegalHolidayUtils;
import com.mediatek.calendar.extension.ExtensionFactory;
import com.mediatek.calendar.extension.IMonthViewExt;
import com.mediatek.calendar.extension.IMonthViewForExt;
import com.mediatek.calendar.lunar.LunarUtil;

public class MonthWeekEventsView extends SimpleWeekView implements IMonthViewForExt {

    private static final String LOG_TAG = "MonthView";

    private static final boolean DEBUG_LAYOUT = false;

    public static final String VIEW_PARAMS_ORIENTATION = "orientation";
    /// tag for animate the selected day @{
    public static final String VIEW_PARAMS_ANIMATE_SELECTED_DAY = "animate_selected_day";
    /// @}
    public static final String VIEW_PARAMS_ANIMATE_TODAY = "animate_today";

    /* NOTE: these are not constants, and may be multiplied by a scale factor */
    private static int TEXT_SIZE_MONTH_NUMBER = 32;
    private static int TEXT_SIZE_EVENT = 12;
    private static int TEXT_SIZE_EVENT_TITLE = 14;
    private static int TEXT_SIZE_MORE_EVENTS = 12;
    private static int TEXT_SIZE_MONTH_NAME = 14;
    private static int TEXT_SIZE_WEEK_NUM = 12;

    private static int DNA_MARGIN = 4;
    private static int DNA_ALL_DAY_HEIGHT = 4;
    private static int DNA_MIN_SEGMENT_HEIGHT = 4;
    private static int DNA_WIDTH = 8;
    private static int DNA_ALL_DAY_WIDTH = 32;
    private static int DNA_SIDE_PADDING = 6;
    private static int CONFLICT_COLOR = Color.BLACK;
    private static int EVENT_TEXT_COLOR = Color.WHITE;

    private static int DEFAULT_EDGE_SPACING = 0;
    private static int SIDE_PADDING_MONTH_NUMBER = 4;
    private static int TOP_PADDING_MONTH_NUMBER = 4;
    private static int TOP_PADDING_WEEK_NUMBER = 4;
    private static int SIDE_PADDING_WEEK_NUMBER = 20;
    private static int DAY_SEPARATOR_OUTER_WIDTH = 0;
    private static int DAY_SEPARATOR_INNER_WIDTH = 0/*1*/;
    private static int DAY_SEPARATOR_VERTICAL_LENGTH = 53;
    private static int DAY_SEPARATOR_VERTICAL_LENGHT_PORTRAIT = 64;
    private static int MIN_WEEK_WIDTH = 50;

    private static int SIDE_PADDING_MONTH_VIEW = 2;

    private static int EVENT_X_OFFSET_LANDSCAPE = 38;
    private static int EVENT_Y_OFFSET_LANDSCAPE = 8;
    private static int EVENT_Y_OFFSET_PORTRAIT = 7;
    private static int EVENT_SQUARE_WIDTH = 10;
    private static int EVENT_SQUARE_BORDER = 2;
    private static int EVENT_LINE_PADDING = 2;
    private static int EVENT_RIGHT_PADDING = 4;
    private static int EVENT_BOTTOM_PADDING = 3;

    private static int TODAY_HIGHLIGHT_WIDTH = 2;

    private static int SPACING_WEEK_NUMBER = 24;
    private static boolean mInitialized = false;
    private static boolean mShowDetailsInMonth;

    protected Time mToday = new Time();
    /// M: for go to @{
    /// the selected day time
    protected Time mSelectedDayTime = new Time();
    /// the selected day index in the month by week view
    protected int mSelectedDayIndex = -1;
    /// @}
    protected boolean mHasToday = false;
    protected int mTodayIndex = -1;
    protected int mOrientation = Configuration.ORIENTATION_LANDSCAPE;
    protected List<ArrayList<Event>> mEvents = null;
    protected ArrayList<Event> mUnsortedEvents = null;
    HashMap<Integer, Utils.DNAStrand> mDna = null;
    // This is for drawing the outlines around event chips and supports up to 10
    // events being drawn on each day. The code will expand this if necessary.
    protected FloatRef mEventOutlines = new FloatRef(10 * 4 * 4 * 7);

    protected boolean mDisplayFestivalDay = true;
    protected boolean mDisplayWorkAndShift = true;
    protected boolean mDisplayLunarDate = false;
    protected boolean mDisplayEventTag = true;

    protected boolean isWeekMode = false;
    protected boolean isChineseEnvironment = true;

    protected ArrayList<PeriodInfo> mPeriodInfos = null;
    protected boolean isPeriod = false;
    protected int lastPeriodStartDay;
    protected int lastPeriodFinishDay;
    protected int defaultPeriodLastDays;
    protected int defaultPeriodCycle;

    protected static StringBuilder mStringBuilder = new StringBuilder(50);
    // TODO recreate formatter when locale changes
    protected static Formatter mFormatter = new Formatter(mStringBuilder, Locale.getDefault());

    protected Paint mMonthNamePaint;
    protected TextPaint mEventPaint;
    protected TextPaint mSolidBackgroundEventPaint;
    protected TextPaint mFramedEventPaint;
    protected TextPaint mDeclinedEventPaint;
    protected TextPaint mEventExtrasPaint;
    protected TextPaint mEventDeclinedExtrasPaint;
    protected Paint mWeekNumPaint;
    protected Paint mDNAAllDayPaint;
    protected Paint mDNATimePaint;
    protected Paint mEventSquarePaint;


    protected Drawable mTodayDrawable;

    protected int mMonthNumHeight;
    protected int mMonthNumAscentHeight;
    protected int mEventHeight;
    protected int mEventAscentHeight;
    protected int mExtrasHeight;
    protected int mExtrasAscentHeight;
    protected int mExtrasDescent;
    protected int mWeekNumAscentHeight;

    protected int mMonthBGColor;
    protected int mMonthBGOtherColor;
    protected int mMonthBGTodayColor;
    protected int mMonthNumColor;
    protected int mMonthNumOtherColor;
    protected int mMonthNumTodayColor;
    protected int mMonthNameColor;
    protected int mMonthNameOtherColor;
    protected int mMonthEventColor;
    protected int mMonthDeclinedEventColor;
    protected int mMonthDeclinedExtrasColor;
    protected int mMonthEventExtraColor;
    protected int mMonthEventOtherColor;
    protected int mMonthEventExtraOtherColor;
    protected int mMonthWeekNumColor;
    protected int mMonthBusyBitsBgColor;
    protected int mMonthBusyBitsBusyTimeColor;
    protected int mMonthBusyBitsConflictTimeColor;
    private int mClickedDayIndex = -1;
    private int mClickedDayColor;
    private static final int mClickedAlpha = 128;

    protected int mEventChipOutlineColor = 0xFFFFFFFF;
    protected int mDaySeparatorInnerColor;
    protected int mTodayAnimateColor;

    private boolean mAnimateToday;
    private int mAnimateTodayAlpha = 0;
    private ObjectAnimator mTodayAnimator = null;

    /// M: for go to @{
    // animate color for selected day
    protected int mSelectedDayAnimateColor;
    // do we need to animate the selected day
    private boolean mAnimateSelectedDay;
    // animate selected day alpha
    private int mAnimateSelectedDayAlpha = 0;
    // animator for selected day
    private ObjectAnimator mSelectedDayAnimator = null;
    /// @}
    /// M: animator listener for selected day
    private final SelectedDayAnimatorListener mAnimatorListener = new SelectedDayAnimatorListener();
    

    // Gionee <jiangxiao> <2013-04-11> add for CR000000 begin
    // add fields for color
    private int mColorTest; // this color is used for testing
    
    private int mBgColorCurrentMonthDay;
    private int mBgColorSecondaryMonthDay;
    // Gionee <jiangxiao> <2013-04-11> add for CR000000 end

    class TodayAnimatorListener extends AnimatorListenerAdapter {
        private volatile Animator mAnimator = null;
        private volatile boolean mFadingIn = false;

        @Override
        public void onAnimationEnd(Animator animation) {
            synchronized (this) {
                if (mAnimator != animation) {
                    animation.removeAllListeners();
                    animation.cancel();
                    return;
                }
                if (mFadingIn) {
                    if (mTodayAnimator != null) {
                        mTodayAnimator.removeAllListeners();
                        mTodayAnimator.cancel();
                    }
                    mTodayAnimator = ObjectAnimator.ofInt(MonthWeekEventsView.this,
                            "animateTodayAlpha", 255, 0);
                    mAnimator = mTodayAnimator;
                    mFadingIn = false;
                    mTodayAnimator.addListener(this);
                    mTodayAnimator.setDuration(600);
                    mTodayAnimator.start();
                } else {
                    mAnimateToday = false;
                    mAnimateTodayAlpha = 0;
                    mAnimator.removeAllListeners();
                    mAnimator = null;
                    mTodayAnimator = null;
                    Log.d(LOG_TAG, "invalidate() in TodayAnimatorListener");
                    invalidate();
                }
            }
        }

        public void setAnimator(Animator animation) {
            mAnimator = animation;
        }

        public void setFadingIn(boolean fadingIn) {
            mFadingIn = fadingIn;
        }

    }

    /// M: the animator for the selected day @{
    class SelectedDayAnimatorListener extends AnimatorListenerAdapter {
        private volatile Animator mAnimator = null;
        private volatile boolean mFadingIn = false;

        @Override
        public void onAnimationEnd(Animator animation) {
            synchronized (this) {
                if (mAnimator != animation) {
                    animation.removeAllListeners();
                    animation.cancel();
                    return;
                }
                if (mFadingIn) {
                    if (mSelectedDayAnimator != null) {
                        mSelectedDayAnimator.removeAllListeners();
                        mSelectedDayAnimator.cancel();
                    }
                    mSelectedDayAnimator = ObjectAnimator.ofInt(MonthWeekEventsView.this,
                            "animateSelectedDayAlpha", 255, 0);
                    mAnimator = mSelectedDayAnimator;
                    mFadingIn = false;
                    mSelectedDayAnimator.addListener(this);
                    mSelectedDayAnimator.setDuration(600);
                    mSelectedDayAnimator.start();
                } else {
                    mAnimateSelectedDay = false;
                    mAnimateSelectedDayAlpha = 0;
                    mAnimator.removeAllListeners();
                    mAnimator = null;
                    mSelectedDayAnimator = null;
                    Log.d(LOG_TAG, "invalidate() in SelectedDayAnimatorListener");
                    invalidate();
                }
            }
        }

        public void setAnimator(Animator animation) {
            mAnimator = animation;
        }

        public void setFadingIn(boolean fadingIn) {
            mFadingIn = fadingIn;
        }
    }
    /// @}

    private int[] mDayXs;

    /**
     * This provides a reference to a float array which allows for easy size
     * checking and reallocation. Used for drawing lines.
     */
    private class FloatRef {
        float[] array;

        public FloatRef(int size) {
            array = new float[size];
        }

        public void ensureSize(int newSize) {
            if (newSize >= array.length) {
                // Add enough space for 7 more boxes to be drawn
                array = Arrays.copyOf(array, newSize + 16 * 7);
            }
        }
    }

    public void setWeekMode(boolean isWeekMode) {
        this.isWeekMode = isWeekMode;
    }

    public boolean getWeekMode() {
        return isWeekMode;
    }

    /**
     * Shows up as an error if we don't include this.
     */
    public MonthWeekEventsView(Context context) {
        super(context);

        isChineseEnvironment = supportLunar();

        mDisplayFestivalDay = Utils.getSharedPreference(context, AuroraCalendarViewFilterActivity.FESTIVAL_KEY, true);
        mDisplayWorkAndShift = Utils.getSharedPreference(context, AuroraCalendarViewFilterActivity.HAPPY_KEY, true);
        mDisplayLunarDate = Utils.getSharedPreference(context, AuroraCalendarViewFilterActivity.LUNAR_KEY, false);
        mDisplayEventTag = Utils.getSharedPreference(context, AuroraCalendarViewFilterActivity.WORK_KEY, true);

        isPeriod = Utils.displayPeriod(context);
        defaultPeriodLastDays = Utils.getPeriodSharePreference(context, Utils.PERIOD_TIME, 5);
        defaultPeriodCycle = Utils.getPeriodSharePreference(context, Utils.PERIOD_CYCLE, 28);

        ///M: init the plugin @{
        mCellExt = ExtensionFactory.getMonthViewPlugin(context, this);
        ///@}
    }
    
    // Gionee <jiangxiao> <2013-04-22> add for CR000000 begin
    // add this constructor to avoid JE when inflate xml
    public MonthWeekEventsView(Context context, AttributeSet attr) {
    	super(context, attr);
    }
    // Gionee <jiangxiao> <2013-04-22> add for CR000000 end

    // Sets the list of events for this week. Takes a sorted list of arrays
    // divided up by day for generating the large month version and the full
    // arraylist sorted by start time to generate the dna version.
    public void setEvents(List<ArrayList<Event>> sortedEvents, ArrayList<Event> unsortedEvents) {
        setEvents(sortedEvents);
        // The MIN_WEEK_WIDTH is a hack to prevent the view from trying to
        // generate dna bits before its width has been fixed.
        createDna(unsortedEvents);
    }

    public void setPeriodInfos(ArrayList<PeriodInfo> periodInfos) {
    	mPeriodInfos = periodInfos;
    	if (periodInfos != null && !periodInfos.isEmpty()) {
    		setLastPeriodInfo(periodInfos.get(0));
    	}
    }

    public void setLastPeriodInfo(PeriodInfo info) {
    	if (info != null) {
    		lastPeriodStartDay = info.getStartDay();
    		lastPeriodFinishDay = info.getFinishDay();
    		defaultPeriodLastDays = Utils.getPeriodSharePreference(mContext, Utils.PERIOD_TIME, 5);
    	}
    }

    /**
     * Sets up the dna bits for the view. This will return early if the view
     * isn't in a state that will create a valid set of dna yet (such as the
     * views width not being set correctly yet).
     */
    public void createDna(ArrayList<Event> unsortedEvents) {
    	// Log.d("WeekView", "createDna() has been invoked");
        if (unsortedEvents == null || mWidth <= MIN_WEEK_WIDTH || getContext() == null) {
            // Stash the list of events for use when this view is ready, or
            // just clear it if a null set has been passed to this view
        	// Log.d("WeekView", "createDna() mDna has been set to null");
            mUnsortedEvents = unsortedEvents;
            mDna = null;
            return;
        } else {
            // clear the cached set of events since we're ready to build it now
            mUnsortedEvents = null;
        }
        // Create the drawing coordinates for dna
        if (!mShowDetailsInMonth) {
            int numDays = mEvents.size();
            int effectiveWidth = mWidth - mPadding * 2;
            if (mShowWeekNum) {
                effectiveWidth -= SPACING_WEEK_NUMBER;
            }
            DNA_ALL_DAY_WIDTH = effectiveWidth / numDays - 2 * DNA_SIDE_PADDING;
            mDNAAllDayPaint.setStrokeWidth(DNA_ALL_DAY_WIDTH);
            mDayXs = new int[numDays];
            for (int day = 0; day < numDays; day++) {
                mDayXs[day] = computeDayLeftPosition(day) + DNA_WIDTH / 2 + DNA_SIDE_PADDING;
            }

            int top = DAY_SEPARATOR_INNER_WIDTH + DNA_MARGIN + DNA_ALL_DAY_HEIGHT + 1;
            int bottom = mHeight - DNA_MARGIN;
            mDna = Utils.createDNAStrands(mFirstJulianDay, unsortedEvents, top, bottom,
                    DNA_MIN_SEGMENT_HEIGHT, mDayXs, getContext());
        }
    }

    public void setEvents(List<ArrayList<Event>> sortedEvents) {
        mEvents = sortedEvents;
        if (sortedEvents == null) {
            return;
        }
        if (sortedEvents.size() != mNumDays) {
        	Log.d("WeekView", "WeekView.setEvents(): invalid list size " + sortedEvents.size());
            if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
                Log.wtf(LOG_TAG, "Events size must be same as days displayed: size="
                        + sortedEvents.size() + " days=" + mNumDays);
            }
            mEvents = null;
            return;
        }
        
        // Gionee <jiangxiao> <2013-04-16> delete for CR000000 begin
        // this.invalidate();
        // Gionee <jiangxiao> <2013-04-16> delete for CR000000 end
    }

    protected void loadColors(Context context) {
        Resources res = context.getResources();
        mMonthWeekNumColor = res.getColor(R.color.month_week_num_color);
        mMonthNumColor = res.getColor(R.color.month_day_number);
        mMonthNumOtherColor = res.getColor(R.color.month_day_number_other);
        mMonthNumTodayColor = res.getColor(R.color.month_today_number);
        mMonthNameColor = mMonthNumColor;
        mMonthNameOtherColor = mMonthNumOtherColor;
        mMonthEventColor = res.getColor(R.color.month_event_color);
        mMonthDeclinedEventColor = res.getColor(R.color.agenda_item_declined_color);
        mMonthDeclinedExtrasColor = res.getColor(R.color.agenda_item_where_declined_text_color);
        mMonthEventExtraColor = res.getColor(R.color.month_event_extra_color);
        mMonthEventOtherColor = res.getColor(R.color.month_event_other_color);
        mMonthEventExtraOtherColor = res.getColor(R.color.month_event_extra_other_color);
        mMonthBGTodayColor = res.getColor(R.color.month_today_bgcolor);
        mMonthBGOtherColor = res.getColor(R.color.month_other_bgcolor);
        mMonthBGColor = res.getColor(R.color.month_bgcolor);
        mDaySeparatorInnerColor = res.getColor(R.color.month_grid_lines);
        // Gionee don't use MTK's color config start
        /*
        ///M:#Theme Manager#@{
        ICalendarThemeExt themeExt = ExtensionFactory.getCalendarTheme(getContext());
        if (themeExt.isThemeManagerEnable()) {
            int themeColor = themeExt.getThemeColor();
            mTodayAnimateColor = themeColor;
            mSelectedDayAnimateColor = themeColor;
            mClickedDayColor = themeColor;
        } else {
            mTodayAnimateColor = res.getColor(R.color.today_highlight_color);
            mSelectedDayAnimateColor = res.getColor(R.color.today_highlight_color);
            mClickedDayColor = res.getColor(R.color.day_clicked_background_color);
        }
        ///@}
        */

        mTodayAnimateColor = res.getColor(R.color.today_highlight_color);
        mSelectedDayAnimateColor = res.getColor(R.color.today_highlight_color);
        mClickedDayColor = res.getColor(R.color.day_clicked_background_color);
        // Gionee don't use MTK's color config end
        mTodayDrawable = res.getDrawable(R.drawable.today_blue_week_holo_light);
        
        // Gionee <jiangxiao> <2013-04-11> add for CR000000 begin
        mBgColorCurrentMonthDay = res.getColor(R.color.month_day_bg_color/*gn_bg_month_current*/);
        mBgColorSecondaryMonthDay = res.getColor(R.color.month_day_bg_color/*gn_bg_month_secondary*/);
        // Gionee <jiangxiao> <2013-04-11> add for CR000000 end
    }

    /**
     * Sets up the text and style properties for painting. Override this if you
     * want to use a different paint.
     */
    @Override
    protected void initView() {
        super.initView();

        if (!mInitialized) {
            Resources resources = getContext().getResources();
            mShowDetailsInMonth = Utils.getConfigBool(getContext(), R.bool.show_details_in_month);
            TEXT_SIZE_EVENT_TITLE = resources.getInteger(R.integer.text_size_event_title);
            TEXT_SIZE_MONTH_NUMBER = resources.getInteger(R.integer.text_size_month_number);
            SIDE_PADDING_MONTH_NUMBER = resources.getInteger(R.integer.month_day_number_margin);
            CONFLICT_COLOR = resources.getColor(R.color.month_dna_conflict_time_color);
            EVENT_TEXT_COLOR = resources.getColor(R.color.calendar_event_text_color);
            if (mScale != 1) {
                TOP_PADDING_MONTH_NUMBER *= mScale;
                TOP_PADDING_WEEK_NUMBER *= mScale;
                SIDE_PADDING_MONTH_NUMBER *= mScale;
                SIDE_PADDING_WEEK_NUMBER *= mScale;
                SPACING_WEEK_NUMBER *= mScale;
                TEXT_SIZE_MONTH_NUMBER *= mScale;
                TEXT_SIZE_EVENT *= mScale;
                TEXT_SIZE_EVENT_TITLE *= mScale;
                TEXT_SIZE_MORE_EVENTS *= mScale;
                TEXT_SIZE_MONTH_NAME *= mScale;
                TEXT_SIZE_WEEK_NUM *= mScale;
                DAY_SEPARATOR_OUTER_WIDTH *= mScale;
                DAY_SEPARATOR_INNER_WIDTH *= mScale;
                DAY_SEPARATOR_VERTICAL_LENGTH *= mScale;
                DAY_SEPARATOR_VERTICAL_LENGHT_PORTRAIT *= mScale;
                EVENT_X_OFFSET_LANDSCAPE *= mScale;
                EVENT_Y_OFFSET_LANDSCAPE *= mScale;
                EVENT_Y_OFFSET_PORTRAIT *= mScale;
                EVENT_SQUARE_WIDTH *= mScale;
                EVENT_SQUARE_BORDER *= mScale;
                EVENT_LINE_PADDING *= mScale;
                EVENT_BOTTOM_PADDING *= mScale;
                EVENT_RIGHT_PADDING *= mScale;
                DNA_MARGIN *= mScale;
                DNA_WIDTH *= mScale;
                DNA_ALL_DAY_HEIGHT *= mScale;
                DNA_MIN_SEGMENT_HEIGHT *= mScale;
                DNA_SIDE_PADDING *= mScale;
                DEFAULT_EDGE_SPACING *= mScale;
                DNA_ALL_DAY_WIDTH *= mScale;
                TODAY_HIGHLIGHT_WIDTH *= mScale;

                SIDE_PADDING_MONTH_VIEW *= mScale;
            }
            if (!mShowDetailsInMonth) {
                TOP_PADDING_MONTH_NUMBER += DNA_ALL_DAY_HEIGHT + DNA_MARGIN;
            }
            mInitialized = true;
        }
        mPadding = DEFAULT_EDGE_SPACING;
        loadColors(getContext());
        // TODO modify paint properties depending on isMini

        mMonthNumPaint = new Paint();
        mMonthNumPaint.setFakeBoldText(false);
        mMonthNumPaint.setAntiAlias(true);
        mMonthNumPaint.setTextSize(TEXT_SIZE_MONTH_NUMBER);
        mMonthNumPaint.setColor(mMonthNumColor);
        mMonthNumPaint.setStyle(Style.FILL);
        mMonthNumPaint.setTextAlign(Align.RIGHT);
        mMonthNumPaint.setTypeface(Typeface.DEFAULT);

        mMonthNumAscentHeight = (int) (-mMonthNumPaint.ascent() + 0.5f);
        mMonthNumHeight = (int) (mMonthNumPaint.descent() - mMonthNumPaint.ascent() + 0.5f);

        mEventPaint = new TextPaint();
        mEventPaint.setFakeBoldText(true);
        mEventPaint.setAntiAlias(true);
        mEventPaint.setTextSize(TEXT_SIZE_EVENT_TITLE);
        mEventPaint.setColor(mMonthEventColor);

        mSolidBackgroundEventPaint = new TextPaint(mEventPaint);
        mSolidBackgroundEventPaint.setColor(EVENT_TEXT_COLOR);
        mFramedEventPaint = new TextPaint(mSolidBackgroundEventPaint);

        mDeclinedEventPaint = new TextPaint();
        mDeclinedEventPaint.setFakeBoldText(true);
        mDeclinedEventPaint.setAntiAlias(true);
        mDeclinedEventPaint.setTextSize(TEXT_SIZE_EVENT_TITLE);
        mDeclinedEventPaint.setColor(mMonthDeclinedEventColor);

        mEventAscentHeight = (int) (-mEventPaint.ascent() + 0.5f);
        mEventHeight = (int) (mEventPaint.descent() - mEventPaint.ascent() + 0.5f);

        mEventExtrasPaint = new TextPaint();
        mEventExtrasPaint.setFakeBoldText(false);
        mEventExtrasPaint.setAntiAlias(true);
        mEventExtrasPaint.setStrokeWidth(EVENT_SQUARE_BORDER);
        mEventExtrasPaint.setTextSize(TEXT_SIZE_EVENT);
        mEventExtrasPaint.setColor(mMonthEventExtraColor);
        mEventExtrasPaint.setStyle(Style.FILL);
        mEventExtrasPaint.setTextAlign(Align.LEFT);
        mExtrasHeight = (int)(mEventExtrasPaint.descent() - mEventExtrasPaint.ascent() + 0.5f);
        mExtrasAscentHeight = (int)(-mEventExtrasPaint.ascent() + 0.5f);
        mExtrasDescent = (int)(mEventExtrasPaint.descent() + 0.5f);

        mEventDeclinedExtrasPaint = new TextPaint();
        mEventDeclinedExtrasPaint.setFakeBoldText(false);
        mEventDeclinedExtrasPaint.setAntiAlias(true);
        mEventDeclinedExtrasPaint.setStrokeWidth(EVENT_SQUARE_BORDER);
        mEventDeclinedExtrasPaint.setTextSize(TEXT_SIZE_EVENT);
        mEventDeclinedExtrasPaint.setColor(mMonthDeclinedExtrasColor);
        mEventDeclinedExtrasPaint.setStyle(Style.FILL);
        mEventDeclinedExtrasPaint.setTextAlign(Align.LEFT);

        mWeekNumPaint = new Paint();
        mWeekNumPaint.setFakeBoldText(false);
        mWeekNumPaint.setAntiAlias(true);
        mWeekNumPaint.setTextSize(TEXT_SIZE_WEEK_NUM);
        mWeekNumPaint.setColor(mWeekNumColor);
        mWeekNumPaint.setStyle(Style.FILL);
        mWeekNumPaint.setTextAlign(Align.RIGHT);

        mWeekNumAscentHeight = (int) (-mWeekNumPaint.ascent() + 0.5f);

        mDNAAllDayPaint = new Paint();
        mDNATimePaint = new Paint();
        mDNATimePaint.setColor(mMonthBusyBitsBusyTimeColor);
        mDNATimePaint.setStyle(Style.FILL_AND_STROKE);
        mDNATimePaint.setStrokeWidth(DNA_WIDTH);
        mDNATimePaint.setAntiAlias(false);
        mDNAAllDayPaint.setColor(mMonthBusyBitsConflictTimeColor);
        mDNAAllDayPaint.setStyle(Style.FILL_AND_STROKE);
        mDNAAllDayPaint.setStrokeWidth(DNA_ALL_DAY_WIDTH);
        mDNAAllDayPaint.setAntiAlias(false);

        mEventSquarePaint = new Paint();
        mEventSquarePaint.setStrokeWidth(EVENT_SQUARE_BORDER);
        mEventSquarePaint.setAntiAlias(false);

        if (DEBUG_LAYOUT) {
            Log.d("EXTRA", "mScale=" + mScale);
            Log.d("EXTRA", "mMonthNumPaint ascent=" + mMonthNumPaint.ascent()
                    + " descent=" + mMonthNumPaint.descent() + " int height=" + mMonthNumHeight);
            Log.d("EXTRA", "mEventPaint ascent=" + mEventPaint.ascent()
                    + " descent=" + mEventPaint.descent() + " int height=" + mEventHeight
                    + " int ascent=" + mEventAscentHeight);
            Log.d("EXTRA", "mEventExtrasPaint ascent=" + mEventExtrasPaint.ascent()
                    + " descent=" + mEventExtrasPaint.descent() + " int height=" + mExtrasHeight);
            Log.d("EXTRA", "mWeekNumPaint ascent=" + mWeekNumPaint.ascent()
                    + " descent=" + mWeekNumPaint.descent());
        }
        
        // init gn drawing params
        initGnView();
    }

    @Override
    public void setWeekParams(HashMap<String, Integer> params, String tz) {
        super.setWeekParams(params, tz);

        if (params.containsKey(VIEW_PARAMS_ORIENTATION)) {
            mOrientation = params.get(VIEW_PARAMS_ORIENTATION);
        }

        updateToday(tz);
        mNumCells = mNumDays + 1;

        /// M: make it animate for selected day @{
        if (params.containsKey(VIEW_PARAMS_SELECTED_DAY)) {
            updateSelectedDayIndex(params.get(VIEW_PARAMS_SELECTED_DAY));
        }
        /// @}
        /// M: animate parameters
        if (params.containsKey(VIEW_PARAMS_ANIMATE_SELECTED_DAY) && mHasSelectedDay) {
            synchronized (mAnimatorListener) {
                if (mSelectedDayAnimator != null) {
                    mSelectedDayAnimator.removeAllListeners();
                    mSelectedDayAnimator.cancel();
                }
                mSelectedDayAnimator = ObjectAnimator.ofInt(this, "animateSelectedDayAlpha",
                        Math.max(mAnimateSelectedDayAlpha, 80), 255);
                mSelectedDayAnimator.setDuration(150);
                mAnimatorListener.setAnimator(mSelectedDayAnimator);
                mAnimatorListener.setFadingIn(true);
                mSelectedDayAnimator.addListener(mAnimatorListener);
                mAnimateSelectedDay = true;
                mSelectedDayAnimator.start();
            }
        }
    }

    /**
     * @param tz
     */
    public boolean updateToday(String tz) {
        mToday.timezone = tz;
        mToday.setToNow();
        mToday.normalize(true);
        int julianToday = Time.getJulianDay(mToday.toMillis(false), mToday.gmtoff);
        if (julianToday >= mFirstJulianDay && julianToday < mFirstJulianDay + mNumDays) {
            mHasToday = true;
            mTodayIndex = julianToday - mFirstJulianDay;
        } else {
            mHasToday = false;
            mTodayIndex = -1;
        }
        return mHasToday;
    }

    /**
     * M: update the selected day index in our displayed view, for example, [0-6]
     * @param weekDay
     * @return the updated selected day's index
     */
    private int updateSelectedDayIndex(int weekDay) {
        if (weekDay < 0) {
            return -1;
        }
        int firstDayOfWeek = Utils.getFirstDayOfWeek(mContext);
        mSelectedDayIndex = weekDay - firstDayOfWeek;
        // fix it if index < 0
        if (mSelectedDayIndex < 0) {
            mSelectedDayIndex += mNumDays;
        }
        return mSelectedDayIndex;
    }

    /// M: set animate alpha for selected day
    public void setAnimateSelectedDayAlpha(int alpha) {
        mAnimateSelectedDayAlpha = alpha;
        Log.d(LOG_TAG, "invalidate() in setAnimateSelectedDayAlpha");
        invalidate();
    }

    public void setAnimateTodayAlpha(int alpha) {
        mAnimateTodayAlpha = alpha;
        Log.d(LOG_TAG, "invalidate() in setAnimateTodayAlpha");
        invalidate();
    }

    /*@Override
    protected void onDraw(Canvas canvas) {
        // drawBackground(canvas);
    	drawWeekViewBackground(canvas);
		// draw background first
    	drawTappedDayBackground(canvas);
        // drawWeekNums(canvas);
    	drawWeekNumber(canvas);
        //drawDaySeparators(canvas);
        /// M: animate selected day
        if (mHasSelectedDay && mAnimateSelectedDay) {
            drawSelectedDay(canvas);
        }
        // Gionee <jiangxiao> <2013-04-16> delete for CR000000 begin
        // delete code for drawing events
		// if (mShowDetailsInMonth) {
		// drawEvents(canvas);
		// } else {
		// if (mDna == null && mUnsortedEvents != null) {
		// createDna(mUnsortedEvents);
		// }
		// drawDNA(canvas);
		// }
        // TODO: maybe this method cause today highlight effect disappeared
        // not here. Actually, invoking GNMonthView.setParams() in MonthFragment.onLoadFinished()
        // is the root cause
        drawEventsTag(canvas);
        //drawLegalHolidayTag(canvas);
        // Gionee <jiangxiao> <2013-04-16> delete for CR000000 end
        // Gionee <jiangxiao> <2013-04-16> delete for CR000000 start
        // replace code for drawing tapped day background
        // drawClick(canvas);
        // drawTappedDayBackground(canvas);
        // Gionee <jiangxiao> <2013-04-16> delete for CR000000 end
    }*/

    @Override
    protected void onDraw(Canvas canvas) {
        // drawWeekViewBackground(canvas);
        drawTappedDayBackground(canvas);
        drawWeekNumber(canvas);
        drawEventsTag(canvas);
        drawPeriodTag(canvas);
    }

    protected void drawToday(Canvas canvas) {
        r.top = DAY_SEPARATOR_INNER_WIDTH + (TODAY_HIGHLIGHT_WIDTH / 2);
        r.bottom = mHeight - (int) Math.ceil(TODAY_HIGHLIGHT_WIDTH / 2.0f);
        p.setStyle(Style.STROKE);
        p.setStrokeWidth(TODAY_HIGHLIGHT_WIDTH);
        r.left = computeDayLeftPosition(mTodayIndex) + (TODAY_HIGHLIGHT_WIDTH / 2);
        r.right = computeDayLeftPosition(mTodayIndex + 1)
                - (int) Math.ceil(TODAY_HIGHLIGHT_WIDTH / 2.0f);
        p.setColor(mTodayAnimateColor | (mAnimateTodayAlpha << 24));
        canvas.drawRect(r, p);
        p.setStyle(Style.FILL);
    }

    /// M: draw the animation for selected day @{
    protected void drawSelectedDay(Canvas canvas) {
        r.top = DAY_SEPARATOR_INNER_WIDTH + (TODAY_HIGHLIGHT_WIDTH / 2);
        r.bottom = mHeight - (int) Math.ceil(TODAY_HIGHLIGHT_WIDTH / 2.0f);
        p.setStyle(Style.STROKE);
        p.setStrokeWidth(TODAY_HIGHLIGHT_WIDTH);
        r.left = computeDayLeftPosition(mSelectedDayIndex) + (TODAY_HIGHLIGHT_WIDTH / 2);
        r.right = computeDayLeftPosition(mSelectedDayIndex + 1)
                - (int) Math.ceil(TODAY_HIGHLIGHT_WIDTH / 2.0f);
        p.setColor(mSelectedDayAnimateColor | (mAnimateSelectedDayAlpha << 24));
        canvas.drawRect(r, p);
        p.setStyle(Style.FILL);
    }
    /// @}

    // TODO move into SimpleWeekView
    // Computes the x position for the left side of the given day
    private int computeDayLeftPosition(int day) {
        int effectiveWidth = mWidth;
        int x = 0;
        int xOffset = 0;
        if (mShowWeekNum) {
            xOffset = SPACING_WEEK_NUMBER + mPadding;
            effectiveWidth -= xOffset;
        }

        xOffset = (int) SIDE_PADDING_MONTH_VIEW;
        effectiveWidth -= (int) (SIDE_PADDING_MONTH_VIEW * 2);

        x = day * effectiveWidth / mNumDays + xOffset;
        return x;
    }

    @Override
    protected void drawDaySeparators(Canvas canvas) {
        float lines[] = new float[8 * 4];
        int count = 6 * 4;
        int wkNumOffset = 0;
        int i = 0;
        if (mShowWeekNum) {
            // This adds the first line separating the week number
            int xOffset = SPACING_WEEK_NUMBER + mPadding;
            count += 4;
            lines[i++] = xOffset;
            lines[i++] = 0;
            lines[i++] = xOffset;
            lines[i++] = mHeight;
            wkNumOffset++;
        }
        count += 4;
        lines[i++] = 0;
        lines[i++] = 0;
        lines[i++] = mWidth;
        lines[i++] = 0;
        int y0 = 0;
        int y1 = mHeight;

        while (i < count) {
            int x = computeDayLeftPosition(i / 4 - wkNumOffset);
            lines[i++] = x;
            lines[i++] = y0;
            lines[i++] = x;
            lines[i++] = y1;
        }
        p.setColor(mDaySeparatorInnerColor);
        p.setStrokeWidth(DAY_SEPARATOR_INNER_WIDTH);
        canvas.drawLines(lines, 0, count, p);
    }

    @Override
    protected void drawBackground(Canvas canvas) {
        // Gionee <jiangxiao> <2013-04-11> add for CR000000 begin
        /*
        int i = 0;
        int offset = 0;
        if (mShowWeekNum) {
            i++;
            offset++;
        } */
        // Gionee <jiangxiao> <2013-04-11> add for CR000000 end
        
        // Gionee <jiangxiao> <2013-04-11> add for CR000000 begin
        // draw current month background
        r.top = DAY_SEPARATOR_INNER_WIDTH;
        r.bottom = mHeight;
        r.left = 0;
        r.right = mWidth;
        
        p.setColor(mBgColorCurrentMonthDay);
        canvas.drawRect(r, p);
        // Gionee <jiangxiao> <2013-04-11> add for CR000000 end
        
        // Gionee <jiangxiao> <2013-04-11> add for CR000000 begin
        // don't draw background according to mOddMonth start
        /*
        if (!mOddMonth[i]) {
            while (++i < mOddMonth.length && !mOddMonth[i])
                ;
            r.right = computeDayLeftPosition(i - offset);
            r.left = 0;
            p.setColor(mMonthBGOtherColor);
            canvas.drawRect(r, p);
            // compute left edge for i, set up r, draw
        } else if (!mOddMonth[(i = mOddMonth.length - 1)]) {
            while (--i >= offset && !mOddMonth[i])
                ;
            i++;
            // compute left edge for i, set up r, draw
            r.right = mWidth;
            r.left = computeDayLeftPosition(i - offset);
            p.setColor(mMonthBGOtherColor);
            canvas.drawRect(r, p);
        }
        */
        // don't draw background according to mOddMonth start
        
        // don't draw background for today start
        /*
        if (mHasToday) {
            p.setColor(mMonthBGTodayColor);
            r.left = computeDayLeftPosition(mTodayIndex);
            r.right = computeDayLeftPosition(mTodayIndex + 1);
            canvas.drawRect(r, p);
        }
        */
        // don't draw background for today mOddMonth end
        // Gionee <jiangxiao> <2013-04-11> add for CR000000 end
    }

    // Draw the "clicked" color on the tapped day
    private void drawClick(Canvas canvas) {
        if (mClickedDayIndex != -1) {
        	Log.d(LOG_TAG, "call drawClick: " + this.hashCode());
            int alpha = p.getAlpha();
            p.setColor(mClickedDayColor);
            p.setAlpha(mClickedAlpha);
            r.left = computeDayLeftPosition(mClickedDayIndex);
            r.right = computeDayLeftPosition(mClickedDayIndex + 1);
            r.top = DAY_SEPARATOR_INNER_WIDTH;
            r.bottom = mHeight;
            canvas.drawRect(r, p);
            p.setAlpha(alpha);
        }
    }

    @Override
    protected void drawWeekNums(Canvas canvas) {
        int y;

        int i = 0;
        int offset = -1;
        int todayIndex = mTodayIndex;
        int x = 0;
        int numCount = mNumDays;
        if (mShowWeekNum) {
            x = SIDE_PADDING_WEEK_NUMBER + mPadding;
            y = mWeekNumAscentHeight + TOP_PADDING_WEEK_NUMBER;
            canvas.drawText(mDayNumbers[0], x, y, mWeekNumPaint);
            numCount++;
            i++;
            todayIndex++;
            offset++;

        }

        y = mMonthNumAscentHeight + TOP_PADDING_MONTH_NUMBER;

        boolean isFocusMonth = mFocusDay[i];
        boolean isBold = false;
        mMonthNumPaint.setColor(isFocusMonth ? mMonthNumColor : mMonthNumOtherColor);
        for (; i < numCount; i++) {
            if (mHasToday && todayIndex == i) {
                mMonthNumPaint.setColor(mMonthNumTodayColor);
                mMonthNumPaint.setFakeBoldText(isBold = true);
                if (i + 1 < numCount) {
                    // Make sure the color will be set back on the next iteration
                    isFocusMonth = !mFocusDay[i + 1];
                	x = computeDayLeftPosition(i - offset) - (SIDE_PADDING_MONTH_NUMBER);
                    canvas.drawText(mDayNumbers[i], x, y, mMonthNumPaint);
                }
            } else if(mFocusDay[i]) {
            	mMonthNumPaint.setColor(mMonthNumColor);
            	x = computeDayLeftPosition(i - offset) - (SIDE_PADDING_MONTH_NUMBER);
            	canvas.drawText(mDayNumbers[i], x, y, mMonthNumPaint);
            }
            // Gionee don't draw non-focus month date start
            /*
            else if (mFocusDay[i] != isFocusMonth) {
                isFocusMonth = mFocusDay[i];
                mMonthNumPaint.setColor(isFocusMonth ? mMonthNumColor : mMonthNumOtherColor);
            }
            x = computeDayLeftPosition(i - offset) - (SIDE_PADDING_MONTH_NUMBER);
            canvas.drawText(mDayNumbers[i], x, y, mMonthNumPaint);
            */
            // Gionee don't draw non-focus month date end

            ///M: do the extension here @{
            mCellExt.drawInCell(canvas, mMonthNumPaint, x, y);
            ///@}

            if (isBold) {
                mMonthNumPaint.setFakeBoldText(isBold = false);
            }
        }
    }

    protected void drawEvents(Canvas canvas) {
        if (mEvents == null) {
            return;
        }

        int day = -1;
        for (ArrayList<Event> eventDay : mEvents) {
            day++;
            if (eventDay == null || eventDay.size() == 0) {
                continue;
            }
            int ySquare;
            int xSquare = computeDayLeftPosition(day) + SIDE_PADDING_MONTH_NUMBER + 1;
            int rightEdge = computeDayLeftPosition(day + 1);

            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                ySquare = EVENT_Y_OFFSET_PORTRAIT + mMonthNumHeight + TOP_PADDING_MONTH_NUMBER;
                rightEdge -= SIDE_PADDING_MONTH_NUMBER + 1;
            } else {
                ySquare = EVENT_Y_OFFSET_LANDSCAPE;
                rightEdge -= EVENT_X_OFFSET_LANDSCAPE;
            }

            // Determine if everything will fit when time ranges are shown.
            boolean showTimes = true;
            Iterator<Event> iter = eventDay.iterator();
            int yTest = ySquare;
            while (iter.hasNext()) {
                Event event = iter.next();
                int newY = drawEvent(canvas, event, xSquare, yTest, rightEdge, iter.hasNext(),
                        showTimes, /*doDraw*/ false);
                if (newY == yTest) {
                    showTimes = false;
                    break;
                }
                yTest = newY;
            }

            int eventCount = 0;
            iter = eventDay.iterator();
            while (iter.hasNext()) {
                Event event = iter.next();
                int newY = drawEvent(canvas, event, xSquare, ySquare, rightEdge, iter.hasNext(),
                        showTimes, /*doDraw*/ true);
                if (newY == ySquare) {
                    break;
                }
                eventCount++;
                ySquare = newY;
            }

            int remaining = eventDay.size() - eventCount;
            if (remaining > 0) {
                drawMoreEvents(canvas, remaining, xSquare);
            }
        }
    }

    protected int addChipOutline(FloatRef lines, int count, int x, int y) {
        lines.ensureSize(count + 16);
        // top of box
        lines.array[count++] = x;
        lines.array[count++] = y;
        lines.array[count++] = x + EVENT_SQUARE_WIDTH;
        lines.array[count++] = y;
        // right side of box
        lines.array[count++] = x + EVENT_SQUARE_WIDTH;
        lines.array[count++] = y;
        lines.array[count++] = x + EVENT_SQUARE_WIDTH;
        lines.array[count++] = y + EVENT_SQUARE_WIDTH;
        // left side of box
        lines.array[count++] = x;
        lines.array[count++] = y;
        lines.array[count++] = x;
        lines.array[count++] = y + EVENT_SQUARE_WIDTH + 1;
        // bottom of box
        lines.array[count++] = x;
        lines.array[count++] = y + EVENT_SQUARE_WIDTH;
        lines.array[count++] = x + EVENT_SQUARE_WIDTH + 1;
        lines.array[count++] = y + EVENT_SQUARE_WIDTH;

        return count;
    }

    /**
     * Attempts to draw the given event. Returns the y for the next event or the
     * original y if the event will not fit. An event is considered to not fit
     * if the event and its extras won't fit or if there are more events and the
     * more events line would not fit after drawing this event.
     *
     * @param canvas the canvas to draw on
     * @param event the event to draw
     * @param x the top left corner for this event's color chip
     * @param y the top left corner for this event's color chip
     * @param rightEdge the rightmost point we're allowed to draw on (exclusive)
     * @param moreEvents indicates whether additional events will follow this one
     * @param showTimes if set, a second line with a time range will be displayed for non-all-day
     *   events
     * @param doDraw if set, do the actual drawing; otherwise this just computes the height
     *   and returns
     * @return the y for the next event or the original y if it won't fit
     */
    protected int drawEvent(Canvas canvas, Event event, int x, int y, int rightEdge,
            boolean moreEvents, boolean showTimes, boolean doDraw) {
        /*
         * Vertical layout:
         *   (top of box)
         * a. EVENT_Y_OFFSET_LANDSCAPE or portrait equivalent
         * b. Event title: mEventHeight for a normal event, + 2xBORDER_SPACE for all-day event
         * c. [optional] Time range (mExtrasHeight)
         * d. EVENT_LINE_PADDING
         *
         * Repeat (b,c,d) as needed and space allows.  If we have more events than fit, we need
         * to leave room for something like "+2" at the bottom:
         *
         * e. "+ more" line (mExtrasHeight)
         *
         * f. EVENT_BOTTOM_PADDING (overlaps EVENT_LINE_PADDING)
         *   (bottom of box)
         */
        final int BORDER_SPACE = EVENT_SQUARE_BORDER + 1;       // want a 1-pixel gap inside border
        final int STROKE_WIDTH_ADJ = EVENT_SQUARE_BORDER / 2;   // adjust bounds for stroke width
        boolean allDay = event.allDay;
        int eventRequiredSpace = mEventHeight;
        if (allDay) {
            // Add a few pixels for the box we draw around all-day events.
            eventRequiredSpace += BORDER_SPACE * 2;
        } else if (showTimes) {
            // Need room for the "1pm - 2pm" line.
            eventRequiredSpace += mExtrasHeight;
        }
        int reservedSpace = EVENT_BOTTOM_PADDING;   // leave a bit of room at the bottom
        if (moreEvents) {
            // More events follow.  Leave a bit of space between events.
            eventRequiredSpace += EVENT_LINE_PADDING;

            // Make sure we have room for the "+ more" line.  (The "+ more" line is expected
            // to be <= the height of an event line, so we won't show "+1" when we could be
            // showing the event.)
            reservedSpace += mExtrasHeight;
        }

        if (y + eventRequiredSpace + reservedSpace > mHeight) {
            // Not enough space, return original y
            return y;
        } else if (!doDraw) {
            return y + eventRequiredSpace;
        }

        boolean isDeclined = event.selfAttendeeStatus == Attendees.ATTENDEE_STATUS_DECLINED;
        int color = event.color;
        if (isDeclined) {
            color = Utils.getDeclinedColorFromColor(color);
        }

        int textX, textY, textRightEdge;

        if (allDay) {
            // We shift the render offset "inward", because drawRect with a stroke width greater
            // than 1 draws outside the specified bounds.  (We don't adjust the left edge, since
            // we want to match the existing appearance of the "event square".)
            r.left = x;
            r.right = rightEdge - STROKE_WIDTH_ADJ;
            r.top = y + STROKE_WIDTH_ADJ;
            r.bottom = y + mEventHeight + BORDER_SPACE * 2 - STROKE_WIDTH_ADJ;
            textX = x + BORDER_SPACE;
            textY = y + mEventAscentHeight + BORDER_SPACE;
            textRightEdge = rightEdge - BORDER_SPACE;
        } else {
            r.left = x;
            r.right = x + EVENT_SQUARE_WIDTH;
            r.bottom = y + mEventAscentHeight;
            r.top = r.bottom - EVENT_SQUARE_WIDTH;
            textX = x + EVENT_SQUARE_WIDTH + EVENT_RIGHT_PADDING;
            textY = y + mEventAscentHeight;
            textRightEdge = rightEdge;
        }

        Style boxStyle = Style.STROKE;
        boolean solidBackground = false;
        if (event.selfAttendeeStatus != Attendees.ATTENDEE_STATUS_INVITED) {
            boxStyle = Style.FILL_AND_STROKE;
            if (allDay) {
                solidBackground = true;
            }
        }
        mEventSquarePaint.setStyle(boxStyle);
        mEventSquarePaint.setColor(color);
        canvas.drawRect(r, mEventSquarePaint);

        float avail = textRightEdge - textX;
        CharSequence text = TextUtils.ellipsize(
                event.title, mEventPaint, avail, TextUtils.TruncateAt.END);
        Paint textPaint;
        if (solidBackground) {
            // Text color needs to contrast with solid background.
            textPaint = mSolidBackgroundEventPaint;
        } else if (isDeclined) {
            // Use "declined event" color.
            textPaint = mDeclinedEventPaint;
        } else if (allDay) {
            // Text inside frame is same color as frame.
            mFramedEventPaint.setColor(color);
            textPaint = mFramedEventPaint;
        } else {
            // Use generic event text color.
            textPaint = mEventPaint;
        }
        canvas.drawText(text.toString(), textX, textY, textPaint);
        y += mEventHeight;
        if (allDay) {
            y += BORDER_SPACE * 2;
        }

        if (showTimes && !allDay) {
            // show start/end time, e.g. "1pm - 2pm"
            textY = y + mExtrasAscentHeight;
            mStringBuilder.setLength(0);
            text = DateUtils.formatDateRange(getContext(), mFormatter, event.startMillis,
                    event.endMillis, DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL,
                    Utils.getTimeZone(getContext(), null)).toString();
            text = TextUtils.ellipsize(text, mEventExtrasPaint, avail, TextUtils.TruncateAt.END);
            canvas.drawText(text.toString(), textX, textY, isDeclined ? mEventDeclinedExtrasPaint
                    : mEventExtrasPaint);
            y += mExtrasHeight;
        }

        y += EVENT_LINE_PADDING;

        return y;
    }

    protected void drawMoreEvents(Canvas canvas, int remainingEvents, int x) {
        int y = mHeight - (mExtrasDescent + EVENT_BOTTOM_PADDING);
        String text = getContext().getResources().getQuantityString(
                R.plurals.month_more_events, remainingEvents);
        mEventExtrasPaint.setAntiAlias(true);
        mEventExtrasPaint.setFakeBoldText(true);
        // canvas.drawText(String.format(text, remainingEvents), x, y, mEventExtrasPaint);
        mEventExtrasPaint.setFakeBoldText(false);
    }

    /**
     * Draws a line showing busy times in each day of week The method draws
     * non-conflicting times in the event color and times with conflicting
     * events in the dna conflict color defined in colors.
     *
     * @param canvas
     */
    protected void drawDNA(Canvas canvas) {
    	// Log.d("WeekView", "drawDNA() has been invoked");
        // Draw event and conflict times
    	Log.d("WeekView", "drawDNA() mDna " + (mDna != null));
        if (mDna != null) {
            for (Utils.DNAStrand strand : mDna.values()) {
//                if (strand.color == CONFLICT_COLOR || strand.points == null
//                        || strand.points.length == 0) {
//                    continue;
//                }
            	// Log.d("WeekView", "drawDNA() non-conflict");
                mDNATimePaint.setColor(strand.color);
                canvas.drawLines(strand.points, mDNATimePaint);
            }
            // Draw black last to make sure it's on top
            Utils.DNAStrand strand = mDna.get(CONFLICT_COLOR);
            if (strand != null && strand.points != null && strand.points.length != 0) {
            	// Log.d("WeekView", "drawDNA() conflict");
                mDNATimePaint.setColor(strand.color);
                canvas.drawLines(strand.points, mDNATimePaint);
            }
            if (mDayXs == null) {
            	Log.d("WeekView", "drawDNA() mDayXs is null");
                return;
            }
            int numDays = mDayXs.length;
            int xOffset = (DNA_ALL_DAY_WIDTH - DNA_WIDTH) / 2;
            if (strand != null && strand.allDays != null && strand.allDays.length == numDays) {
                for (int i = 0; i < numDays; i++) {
                    // this adds at most 7 draws. We could sort it by color and
                    // build an array instead but this is easier.
                    if (strand.allDays[i] != 0) {
                    	// Log.d("WeekView", "drawDNA() allDays " + i);
                        mDNAAllDayPaint.setColor(strand.allDays[i]);
                        canvas.drawLine(mDayXs[i] + xOffset, DNA_MARGIN, mDayXs[i] + xOffset,
                                DNA_MARGIN + DNA_ALL_DAY_HEIGHT, mDNAAllDayPaint);
                    }
                }
            }
        }
    }

    @Override
    protected void updateSelectionPositions() {
        if (mHasSelectedDay) {
            int selectedPosition = mSelectedDay - mWeekStart;
            if (selectedPosition < 0) {
                selectedPosition += 7;
            }
            int effectiveWidth = mWidth - mPadding * 2;
            effectiveWidth -= SPACING_WEEK_NUMBER;
            mSelectedLeft = selectedPosition * effectiveWidth / mNumDays + mPadding;
            mSelectedRight = (selectedPosition + 1) * effectiveWidth / mNumDays + mPadding;
            mSelectedLeft += SPACING_WEEK_NUMBER;
            mSelectedRight += SPACING_WEEK_NUMBER;
        }
    }

    public int getDayIndexFromLocation(float x) {
        int dayStart = mShowWeekNum ? SPACING_WEEK_NUMBER + mPadding : mPadding;
        if (x < dayStart || x > mWidth - mPadding) {
            return -1;
        }
        // Selection is (x - start) / (pixels/day) == (x -s) * day / pixels
        return ((int) ((x - dayStart) * mNumDays / (mWidth - dayStart - mPadding)));
    }

    @Override
    public Time getDayFromLocation(float x) {
        int dayPosition = getDayIndexFromLocation(x);
        if (dayPosition == -1) {
            return null;
        }
        int day = mFirstJulianDay + dayPosition;

        Time time = new Time(mTimeZone);
        /// M: Delete a google walk around, which was used to correct the error 
        /// at the first day of 1970 caused by google original framework.
        /// The framework error has been corrected,so the walk around is no longer needed.
        /// @{
            /*
             *        if (mWeek == 0) {
             *             // This week is weird...
             *             if (day < Time.EPOCH_JULIAN_DAY) {
             *                 day++;
             *            } else if (day == Time.EPOCH_JULIAN_DAY) {
             *                 time.set(1, 0, 1970);
             *                 time.normalize(true);
             *                return time;
             *            }
             *        }
             */
        ///@}
        /// M: if it's before or on the epoch Julian day, Time.setJulianDay() can't work correctly,
        // however, we could compute the time by ourself @{
        Utils.setJulianDayInGeneral(time, day);
        // @}
        return time;
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        Context context = getContext();
        // only send accessibility events if accessibility and exploration are
        // on.
        AccessibilityManager am = (AccessibilityManager) context
                .getSystemService(Service.ACCESSIBILITY_SERVICE);
        if (!am.isEnabled() || !am.isTouchExplorationEnabled()) {
            return super.onHoverEvent(event);
        }
        if (event.getAction() != MotionEvent.ACTION_HOVER_EXIT) {
            Time hover = getDayFromLocation(event.getX());
            if (hover != null
                    && (mLastHoverTime == null || Time.compare(hover, mLastHoverTime) != 0)) {
                Long millis = hover.toMillis(true);
                String date = Utils.formatDateRange(context, millis, millis,
                        DateUtils.FORMAT_SHOW_DATE);
                AccessibilityEvent accessEvent = AccessibilityEvent
                        .obtain(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
                accessEvent.getText().add(date);
                if (mShowDetailsInMonth && mEvents != null) {
                    int dayStart = SPACING_WEEK_NUMBER + mPadding;
                    int dayPosition = (int) ((event.getX() - dayStart) * mNumDays / (mWidth
                            - dayStart - mPadding));
                    ArrayList<Event> events = mEvents.get(dayPosition);
                    List<CharSequence> text = accessEvent.getText();
                    for (Event e : events) {
                        text.add(e.getTitleAndLocation() + ". ");
                        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
                        if (!e.allDay) {
                            flags |= DateUtils.FORMAT_SHOW_TIME;
                            if (DateFormat.is24HourFormat(context)) {
                                flags |= DateUtils.FORMAT_24HOUR;
                            }
                        } else {
                            flags |= DateUtils.FORMAT_UTC;
                        }
                        text.add(Utils.formatDateRange(context, e.startMillis, e.endMillis,
                                flags) + ". ");
                    }
                }
                sendAccessibilityEventUnchecked(accessEvent);
                mLastHoverTime = hover;
            }
        }
        return true;
    }

    public void setClickedDay(float xLocation) {
        mClickedDayIndex = getDayIndexFromLocation(xLocation);
    	// Log.d("DEBUG", "MonthWeek.setClickedDay(): set mClickedDayIndex to " + mClickedDayIndex);
        invalidate();
    }
    
    public void clearClickedDay() {
        mClickedDayIndex = -1;
        // Log.d("DEBUG", "MonthWeek.clearClickedDay(): set mClickedDayIndex to " + mClickedDayIndex);
        // Log.d(LOG_TAG, "invalidate() in clearClickedDay");
        invalidate();
    }

    /**
     * M: the extension of MonthView
     */
    private IMonthViewExt mCellExt;

    ///M: from interface IMonthViewForExt @{
    @Override
    public Time getTimeFromLocation(int x, int y) {
        return getDayFromLocation(x);
    }
    ///@}
    
    // Gionee <jiangxiao> <2013-04-11> add for CR000000 begin
    // return the julian day of selected day
    public int setAndReturnClickedDay(float xLocation) {
    	mClickedDayIndex = getDayIndexFromLocation(xLocation);
        // Log.d("DEBUG", "MonthWeek.setAndReturnClickedDay(float): set mClickedDayIndex to " + mClickedDayIndex);
        invalidate();
        
        // return clicked day to caller
        int selectedJulianDay = mFirstJulianDay + mClickedDayIndex;
        return selectedJulianDay;
    }
    
    public int setAndReturnClickedDay(int index) {
    	mClickedDayIndex = index;
        // Log.d("DEBUG", "MonthWeek.setAndReturnClickedDay(int): set mClickedDayIndex to " + mClickedDayIndex);
    	invalidate();
        
        // return clicked day to caller
        int selectedJulianDay = mFirstJulianDay + mClickedDayIndex;
        return selectedJulianDay;
    }
    // Gionee <Author: jiangxiao> <2013-04-11> add for CR000000 end
    
    // Gionee <jiangxiao> <2013-04-18> add for CR000000 begin
    // judging whether this week contains date of focus month
    public boolean hasFocusMonthDay() {
    	return mHasFocusMonthDay;
    }
    // judging whether this week contains date of focus month
    
    public boolean containJulianDay(int julianDay) {
    	Log.d(LOG_TAG, "julian day range: [" + mFirstJulianDay + ", " + (mFirstJulianDay + mNumDays) + "]");
    	Log.d(LOG_TAG, "to check julian day: " + julianDay);
    	return (julianDay >= mFirstJulianDay && julianDay <= mFirstJulianDay + mNumDays);
    }
    
    public int getWeekDayIndexByJulianDay(int julianDay) {
    	if(julianDay >= mFirstJulianDay && julianDay < mFirstJulianDay + mNumDays) {
    		return (julianDay - mFirstJulianDay);
    	}
    	
    	return -1;
    }

    public int getOffsetByJulianDay(int julianDay) {
        return julianDay - mFirstJulianDay;
    }

    public boolean hasToday() {
    	return mHasToday;
    }
    
    public int getTodayJulianDay() {
    	if(mTodayIndex >= 0) {
    		return (mFirstJulianDay + mTodayIndex);
    	}
    	
    	return -1;
    }
    
    public boolean isTodaySecondary() {
    	boolean result = false;
		Log.d("DEBUG", "isTodaySecondary(): " + mTodayIndex + ", " + mSecondaryDayIndex);
    	if(mTodayIndex >= 0 && mSecondaryDayIndex >= 0) {
    		if(this.getSecondaryDayDirection()) {
    			result = (mTodayIndex >= mSecondaryDayIndex);
    		} else {
    			result = (mTodayIndex <= mSecondaryDayIndex);
    		}
    	}
    	
    	return result;
    }
    
    // Gionee <jiangxiao> <2013-04-18> add for CR000000 end
    
    // Gionee <jiangxiao> <2013-04-22> add for CR000000 begin
    // methods for drawing week view content
    private void drawWeekViewBackground(Canvas canvas) {
    	//Log.d("GNMonthView", "drawMonthViewBackground() has been invoked");
    	
    	r.top = DAY_SEPARATOR_INNER_WIDTH;
        r.bottom = mHeight;
        r.left = 0;
        r.right = mWidth;
        
        p.setColor(mBgColorCurrentMonthDay);
        canvas.drawRect(r, p);
        
        // draw seondary day background
        drawSecondaryDayBackground(canvas);
    }
    
    private void drawSecondaryDayBackground(Canvas canvas) {
    	//Log.d("GNMonthView", "drawSecondaryDayBackground() has been invoked");
    	if(mSecondaryDayIndex < 0 || mSecondaryDayIndex >= mNumDays) return;
    	
    	r.top = DAY_SEPARATOR_INNER_WIDTH;
        r.bottom = mHeight;
        if(mIsSecondaryDayAtTailWeek) {
        	//Log.d("GNMonthView", "drawSecondaryDayBackground(): to draw the header days");
        	r.left = computeDayLeftPosition(mSecondaryDayIndex);
        	r.right = mWidth;
        } else {
        	//Log.d("GNMonthView", "drawSecondaryDayBackground(): to draw the tail days");
        	r.left = 0;
        	r.right = computeDayLeftPosition(mSecondaryDayIndex + 1);
        }
        
        p.setColor(mBgColorSecondaryMonthDay);
        canvas.drawRect(r, p);
    }
    
    private int mSecondaryDayIndex = -1;
    private boolean mIsSecondaryDayAtTailWeek = false;
    
    // set this bit as mIsSecondaryDayAtTail ? 1 : 0
    public static final int SECONDARY_DAY_DIRECTION_MASK = 0x8;
    public static final int SECONDARY_DAY_INDEX_MASK = 0x7;
    private int mSecondaryDayLocation = -1;
    
    public void setSecondaryIndex(int index, boolean atTail) {
    	mSecondaryDayIndex = index;
    	mIsSecondaryDayAtTailWeek = atTail;
    	
    	int directionBit = (mIsSecondaryDayAtTailWeek ? SECONDARY_DAY_DIRECTION_MASK : 0);
    	int indexBit = (index & SECONDARY_DAY_INDEX_MASK);
    	mSecondaryDayLocation = (directionBit | indexBit);
    	
    	this.invalidate();
    }
    
    public int getSecondaryDayIndex() {
    	return mSecondaryDayIndex;
    }
    
    public boolean getSecondaryDayDirection() {
    	return mIsSecondaryDayAtTailWeek;
    }
    
    public int getSecondaryDayLoaction() {
    	if(mSecondaryDayIndex < 0) {
    		return -1;
    	}
    	
    	return mSecondaryDayLocation;
    }
    
    private int mTextSizeSolarMonthDay = 0;
    private int mTextSizeLunarMonthDay = 0;
    
    // text colors for solar days
    private int mColorMonthDayDefault = 0;
    private int mColorMonthDayWeekend = 0;
    private int mColorMonthDayToday = 0;
    private int mColorMonthDayTodaySelected = 0;
    private int mColorMonthDaySecondary = 0;
    // text colors for lunar days
    private int mColorLunarMonthDayDefault = 0;
    private int mColorLunarMonthDayWeekend = 0;
    private int mColorLunarMonthDayToday = 0;
    private int mColorLunarMonthDaySecondary = 0;
    // color for tapped day
    private int mColorTappedDay = 0;
    private int mColorTappedDayToday = 0;
    
    private static final int TOP_PADDING_MONTH_DAY = 10;
    private static final int LEFT_PADDING_SINGLE_NUMBER = 7;
    private static final int GAP_BETWEEN_SOLAR_AND_LUNAR = 30;
    
    private Paint mMonthDayPaint = null;
    private Paint mLunarMonthDayPaint = null;
    private Paint mEventsTagPaint = null;
    
    private int mMonthDayAscentHeight = 0;
    private int mMonthDayHeight = 0;
    
    private Bitmap mEventTagBitmapDefault = null;
    private Bitmap mEventTagBitmapToday = null;
    
    private boolean isGnViewInit = false;
    
    private float mDensity = 0;
    private float mScaledDensity = 0;
    
    private int dp2px(float dp) {
    	return (int) (dp * mDensity + 0.5f);
    }
    
    private int sp2px(float sp) {
    	return (int) (sp * mScaledDensity + 0.5f);
    }
    
    // Gionee <jiangxiao> <2013-06-28> add for CR00831078 begin
    /*private Typeface mTfRobotoThin = null;
    private Typeface mTfRobotoMedium = null;*/
    // Gionee <jiangxiao> <2013-06-28> add for CR00831078 end

    private int highlightTextColor = 0;
    private Typeface robotoLightTypeface = null;

    private static final int MONTH_DAY_TAP_BG_RADIUS = 22;
    private static final int MONTH_DAY_EVENT_TAG_RADIUS = 4;

    private int mMonthDayNumberTopPadding;
    private int mMonthDayTextTopPadding;

    // invoke this method in initView() method
    private void initGnView() {
    	Resources res = getContext().getResources();

        mMonthDayNumberTopPadding = res.getInteger(R.integer.month_day_number_top_padding);
        mMonthDayTextTopPadding = res.getInteger(R.integer.month_day_text_top_padding);

    	mTextSizeSolarMonthDay = res.getInteger(R.integer.month_day_text_size/*gn_text_size_solar_month_day*/);
    	mTextSizeLunarMonthDay = res.getInteger(R.integer.month_day_other_text_size/*gn_text_size_lunar_month_day*/);

    	mColorMonthDayDefault   = res.getColor(R.color.month_day_number_color/*gn_text_month_day_default*/);
    	mColorMonthDayWeekend   = res.getColor(R.color.month_day_number_color/*gn_text_month_day_weekend*/);
    	mColorMonthDayToday     = res.getColor(R.color.month_day_select_text_color/*gn_text_month_day_today*/);
    	mColorMonthDayTodaySelected = res.getColor(R.color.month_day_select_text_color/*gn_text_month_day_today_selected*/);
    	mColorMonthDaySecondary = res.getColor(R.color.month_day_other_number_color/*gn_text_month_day_secondary*/);
    	mColorLunarMonthDayDefault   = res.getColor(R.color.month_day_extra_text_color/*gn_text_lunar_month_day_default*/);
    	mColorLunarMonthDayWeekend   = res.getColor(R.color.month_day_extra_text_color/*gn_text_lunar_month_day_weekend*/);
    	mColorLunarMonthDayToday     = res.getColor(R.color.month_day_select_text_color/*gn_text_lunar_month_day_today*/);
    	mColorLunarMonthDaySecondary = res.getColor(R.color.month_day_other_number_color/*gn_text_lunar_month_day_secondary*/);
    	mColorTappedDay = res.getColor(R.color.month_day_tap_color/*gn_bg_month_day_tapped*/);
    	mColorTappedDayToday = res.getColor(R.color.month_day_today_color/*gn_bg_month_day_tapped_today*/);

    	mTextColorLegalHoliday = res.getColor(R.color.gn_text_legal_holiday);
    	mTextColorLegalWorkShift = res.getColor(R.color.gn_text_legal_work_shift);
    	mStrLegalHoliday = res.getString(R.string.aurora_legal_holiday/*legal_holiday*/);
    	mStrLegalWorkShift = res.getString(R.string.aurora_legal_workday/*legal_work_shift*/);

    	if(mScale > 0 && mScale != 1.0F) {
    		mTextSizeSolarMonthDay *= mScale;
    		mTextSizeLunarMonthDay *= mScale;
    	}

    	// Gionee <jiangxiao> <2013-06-28> add for CR00831078 begin
    	/*mTfRobotoThin = Typeface.createFromAsset(
    			mContext.getAssets(), "fonts/Roboto-Thin.ttf");
    	mTfRobotoMedium = Typeface.createFromAsset(
    			mContext.getAssets(), "fonts/Roboto-Medium.ttf");*/
    	// Gionee <jiangxiao> <2013-06-28> add for CR00831078 end

        highlightTextColor = res.getColor(R.color.month_day_highlight_text_color);
    	robotoLightTypeface = Typeface.createFromFile("system/fonts/Roboto-Light.ttf");

    	mMonthDayPaint = new Paint();
    	mMonthDayPaint.setFakeBoldText(false/*true*/);
    	mMonthDayPaint.setAntiAlias(true);
    	mMonthDayPaint.setTextSize(mTextSizeSolarMonthDay);
    	mMonthDayPaint.setColor(mMonthNumColor);
    	mMonthDayPaint.setStyle(Style.FILL);
    	mMonthDayPaint.setTextAlign(Align.LEFT);
    	mMonthDayPaint.setTypeface(robotoLightTypeface/*mTfRobotoThin*/);

    	mMonthDayAscentHeight = (int) (-mMonthDayPaint.ascent() + 0.5f);
        mMonthDayHeight = (int) (mMonthDayPaint.descent() - mMonthDayPaint.ascent() + 0.5f);

        mLunarMonthDayPaint = new Paint();
        mLunarMonthDayPaint.setFakeBoldText(false);
        mLunarMonthDayPaint.setAntiAlias(true);
        mLunarMonthDayPaint.setTextSize(mTextSizeLunarMonthDay);
        mLunarMonthDayPaint.setColor(mColorLunarMonthDayDefault);
        mLunarMonthDayPaint.setStyle(Style.FILL);
        mLunarMonthDayPaint.setTextAlign(Align.LEFT);
        mLunarMonthDayPaint.setTypeface(Typeface.DEFAULT);

        mEventsTagPaint = new Paint();
        mEventTagBitmapDefault = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.gn_month_event_tag_default);

        mLunarUtil = LunarUtil.getInstance(mContext);
        termQingmingString = mContext.getString(R.string.aurora_term_qingming);

        mDensity = res.getDisplayMetrics().density;
        mScaledDensity = res.getDisplayMetrics().scaledDensity;

        isGnViewInit = true;
    }

    private void drawTappedDayBackground(Canvas canvas) {
        /*if (hasToday() && mTodayIndex != -1) {
            if (!isGnViewInit) {
                initGnView();
            }

            r.left = computeDayLeftPosition(mTodayIndex);
            r.right = computeDayLeftPosition(mTodayIndex + 1);
            r.top = DAY_SEPARATOR_INNER_WIDTH;
            r.bottom = mHeight;

            int alpha = p.getAlpha();
        	p.setColor(mColorTappedDayToday);
            p.setAlpha(255);;
            canvas.drawCircle((r.left + r.right) / 2, (r.top + r.bottom) / 2, MONTH_DAY_TAP_BG_RADIUS * mScale, p);
            p.setAlpha(alpha);

            if (mTodayIndex == mClickedDayIndex) return;
        }*/

    	if (mClickedDayIndex != -1) {
        	if(!isGnViewInit) {
        		initGnView();
        	}
        	
        	// Gionee <jiangxiao> <2013-06-28> add for CR00831078 begin
        	// adjust border
            r.left = computeDayLeftPosition(mClickedDayIndex);
            r.right = computeDayLeftPosition(mClickedDayIndex + 1);
            r.top = DAY_SEPARATOR_INNER_WIDTH;
            r.bottom = mHeight;
            // Gionee <jiangxiao> <2013-06-28> add for CR00831078 end
            
            /*if(mClickedDayIndex == mTodayIndex) {
            	// p.setColor(mColorTappedDayToday);
            	Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), 
						R.drawable.gn_bg_month_tapped_today);

            	p.setFilterBitmap(true);
            	canvas.drawBitmap(bitmap, null, r, p);
            	p.setFilterBitmap(false);
            } else {
            	int alpha = p.getAlpha();
            	p.setColor(mColorTappedDay);
                p.setAlpha(mClickedAlpha);
                canvas.drawRect(r, p);
                p.setAlpha(alpha);
            }*/
            int alpha = p.getAlpha();
        	p.setColor(mColorTappedDay);
            p.setAlpha(255);
            canvas.drawCircle((r.left + r.right) / 2, (r.top + r.bottom) / 2, MONTH_DAY_TAP_BG_RADIUS * mScale, p);
            p.setAlpha(alpha);

            /*if(mHasToday && mClickedDayIndex == mTodayIndex) {
            	// change today's color as white
            	mMonthDayPaint.setColor(mColorMonthDayTodaySelected);
    			mMonthDayPaint.setFakeBoldText(true);
    			mLunarMonthDayPaint.setColor(mColorMonthDayTodaySelected);
    			//mLunarMonthDayPaint.setFakeBoldText(true);
    			
    			int x = 0;
    			int y = mMonthDayAscentHeight + TOP_PADDING_MONTH_DAY;
    			
    			// draw solar month day
    			Rect textBounds = new Rect();
        		String solarText = mDayNumbers[mTodayIndex];
        		mMonthDayPaint.getTextBounds(solarText, 0, solarText.length(), textBounds);
        		int outLeft = computeDayLeftPosition(mTodayIndex);
        		int outRight = computeDayLeftPosition(mTodayIndex + 1);
        		int outWidth = outRight - outLeft;
        		int inWidth = textBounds.width();
        		int inHeight = textBounds.height();
        		
        		int xSolarOffset = outLeft + (outWidth - inWidth) / 2;
        		xSolarOffset = adjustWeekNumberXOffset(solarText, xSolarOffset);
        		
    			canvas.drawText(solarText, xSolarOffset, y, mMonthDayPaint);
    			
    			if(this.supportLunar()) {
        			// draw lunar month day
        			Time t = new Time();
        			t.setJulianDay(mFirstJulianDay + mTodayIndex);
        			t.normalize(true);
        			// String[] temp = mLunarUtil.getLunarFestivalChineseString(t.year, t.month, t.monthDay).split(LunarUtil.DELIM);
        			String lunarText = getLunarText(t);
        			
        			mLunarMonthDayPaint.getTextBounds(lunarText, 0, lunarText.length(), textBounds);
        			int inWidthLunar = textBounds.width();
        			
        			int xLunarOffset = outLeft + (outWidth - inWidthLunar) / 2;
        			int yLunarOffset = (y + inHeight + GAP_BETWEEN_SOLAR_AND_LUNAR);
        			canvas.drawText(lunarText, xLunarOffset, yLunarOffset, mLunarMonthDayPaint);
    			}
    			
    			// reset bold
    			mMonthDayPaint.setFakeBoldText(false);
    			//mLunarMonthDayPaint.setFakeBoldText(false);
            }*/ // end of draw today as white
        }
    }
    
    private String getLunarText(Time t) {
		String[] temp = mLunarUtil.getLunarFestivalChineseString(t.year, (t.month + 1), t.monthDay).split(LunarUtil.DELIM);
		return temp[0];
    }
    
    private boolean isWeekend(int day) {
    	if(day < 0 || day >= DEFAULT_NUM_DAYS) {
    		throw new RuntimeException("isWeekend(): invalid day " + day);
    	}
    	
    	if(mWeekStart == Time.MONDAY) {
    		return (day == 5 || day == 6);
    	} else if(mWeekStart == Time.SUNDAY) {
    		return (day == 0 || day == 6);
    	} else if(mWeekStart == Time.SATURDAY) {
    		return (day == 0 || day == 1);
    	} else {
    		throw new RuntimeException("isWeekend(): doesn't support week start day " + mWeekStart);
    	}
    }
    
    private LunarUtil mLunarUtil = null;
    private String termQingmingString = null;

    private boolean supportLunar() {
    	String lang = Locale.getDefault().getLanguage();
    	Log.d("debug2", "supportLunar(): current lang is " + lang);
    	Log.d("debug2", "supportLunar(): " + Locale.CHINESE);
    	if(lang.equals(Locale.CHINA.getLanguage()) 
    			|| lang.equals(Locale.CHINESE.getLanguage()) 
    			|| lang.equals(Locale.TAIWAN.getLanguage())
    			|| lang.equals(Locale.SIMPLIFIED_CHINESE.getLanguage()) 
    			|| lang.equals(Locale.TRADITIONAL_CHINESE.getLanguage())) {
    		Log.d("debug2", "supportLunar(): true");
    		return true;
    	}
    	
    	Log.d("debug2", "supportLunar(): false");
    	return false;
    }
    
    private void drawWeekNumber(Canvas canvas) {
    	if(!isGnViewInit) {
    		initGnView();
    	}
    	
    	int x = 0;
    	int y = mMonthDayAscentHeight + TOP_PADDING_MONTH_DAY;

        int[] legalHolidayTag = new int[mNumDays];
        ILegalHoliday utils = LegalHolidayUtils.getInstance();

        for (int i = mFirstJulianDay; i < (mFirstJulianDay + mNumDays); ++i) {
            int type = utils.getDayType(i);
            legalHolidayTag[i - mFirstJulianDay] = type;
        }

    	// skip week number column
    	// int index = 0;
    	// int todayIndex = mTodayIndex;
    	boolean isBold = false;
    	Rect textBounds = new Rect();
    	boolean saveTodayOffset = false;
    	boolean useRobotoMedium = false;
    	for(int index = 0; index < mNumDays; ++index) {
    		/*if(mHasToday && mTodayIndex == index) {
    			// draw text for today
    			// Log.d("DEBUG", "mTodayIndex = " + mTodayIndex + ", mSelectedDayIndex = " + mSelectedDayIndex);
    			if(mClickedDayIndex == mTodayIndex) {
    				mMonthDayPaint.setColor(mColorMonthDayTodaySelected);
    				mLunarMonthDayPaint.setColor(mColorMonthDayTodaySelected);
    			} else {
    				mMonthDayPaint.setColor(mColorMonthDayToday);
    				mLunarMonthDayPaint.setColor(mColorLunarMonthDayToday);
    			}
    			
    			//mMonthDayPaint.setFakeBoldText(isBold = true);
    			// mLunarMonthDayPaint.setFakeBoldText(isBold = true);
    		} else if(mFocusDay[index] && isWeekend(index)) {
    			// draw text for current month weekend day
    			mMonthDayPaint.setColor(mColorMonthDayWeekend);
    			// mMonthDayPaint.setFakeBoldText(isBold = true);
    			mLunarMonthDayPaint.setColor(mColorLunarMonthDayWeekend);
    			
    			// Gionee <jiangxiao> <2013-06-28> add for CR00831078 begin
    			mMonthDayPaint.setTypeface(mTfRobotoMedium);
    			mMonthDayPaint.setFakeBoldText(false);
    			useRobotoMedium = true;
    			// Gionee <jiangxiao> <2013-06-28> add for CR00831078 end
    			// mLunarMonthDayPaint.setFakeBoldText(isBold = true);
    		} else if(mFocusDay[index]) {
    			// draw text for current month day
    			mMonthDayPaint.setColor(mColorMonthDayDefault);
    			mLunarMonthDayPaint.setColor(mColorLunarMonthDayDefault);
    		} else {
    			// draw text for secondary month day
    			mMonthDayPaint.setColor(mColorMonthDaySecondary);
    			mLunarMonthDayPaint.setColor(mColorLunarMonthDaySecondary);
    		}*/

            boolean drawBoldNumber = false;
            boolean drawSelectedDay = false;
    		if (mClickedDayIndex == index) {
    			drawBoldNumber = true;
                drawSelectedDay = true;
    			mMonthDayPaint.setFakeBoldText(true);
                mMonthDayPaint.setColor(mColorMonthDayTodaySelected);
                mLunarMonthDayPaint.setColor(mColorMonthDayTodaySelected);
            } else if (mHasToday && mTodayIndex == index) {
                drawSelectedDay = true;
                mMonthDayPaint.setColor(mColorTappedDayToday/*mColorMonthDayToday*/);
                mLunarMonthDayPaint.setColor(mColorTappedDayToday/*mColorLunarMonthDayToday*/);
            } else if (mFocusDay[index] || isWeekMode) {
                mMonthDayPaint.setColor(mColorMonthDayDefault);
                // mLunarMonthDayPaint.setColor(mColorLunarMonthDayDefault);
                if (legalHolidayTag[index] != ILegalHoliday.DAY_TYPE_NORMAL && mDisplayWorkAndShift) {
                    mLunarMonthDayPaint.setColor(highlightTextColor);
                } else {
                    mLunarMonthDayPaint.setColor(mColorLunarMonthDayDefault);
                }
            } else {
                mMonthDayPaint.setColor(mColorMonthDaySecondary);
                mLunarMonthDayPaint.setColor(mColorLunarMonthDaySecondary);
            }

			// draw solar month day
			// int offset = mCellWidth / 3;
			// Log.d(LOG_TAG, "draw text offset: " + offset);
			// x = computeDayLeftPosition(index + 1) - offset;
			// if(mDayNumbers[index].length() == 1) {
			// x -= LEFT_PADDING_SINGLE_NUMBER;
			// }
    		String solarText = mDayNumbers[index];
    		mMonthDayPaint.getTextBounds(solarText, 0, solarText.length(), textBounds);
    		int outLeft = computeDayLeftPosition(index);
    		int outRight = computeDayLeftPosition(index + 1);
    		int outWidth = outRight - outLeft;
    		int inWidth = textBounds.width();
    		int inHeight = textBounds.height();

    		int xSolarOffset = outLeft + (outWidth - inWidth) / 2;
    		xSolarOffset = adjustWeekNumberXOffset(solarText, xSolarOffset);
            int ySolarOffset = (int) (inHeight + (mMonthDayNumberTopPadding + (isChineseEnvironment ? 0 : 5)) * mScale);

			canvas.drawText(solarText, xSolarOffset, ySolarOffset/*y*/, mMonthDayPaint);

			if (drawBoldNumber) {
            	drawBoldNumber = false;
                mMonthDayPaint.setFakeBoldText(false);
            }

			// draw lunar month day
			// if(mDayNumbers[index].length() == 1) {
			// x += LEFT_PADDING_SINGLE_NUMBER;
			// }
			// mCellExt.drawInCell(canvas, mLunarMonthDayPaint, x, y +
			// GAP_BETWEEN_SOLAR_AND_LUNAR);

            if (!mDisplayFestivalDay && !mDisplayWorkAndShift && !mDisplayLunarDate && !drawSelectedDay) continue;

			if (isChineseEnvironment) {
				Time t = new Time();
				t.setJulianDay(mFirstJulianDay + index);
				t.normalize(true);
				// String[] temp = mLunarUtil.getLunarFestivalChineseString(t.year, (t.month + 1), t.monthDay).split(LunarUtil.DELIM);
                // String lunarText = getLunarText(t);

				/*String allExtraString = mLunarUtil.getLunarFestivalChineseString(t.year, (t.month + 1), t.monthDay);
                boolean isFestival = allExtraString.contains(LunarUtil.DELIM);
                String[] temp = allExtraString.split(LunarUtil.DELIM);
                String lunarText = temp[0];

                if (lunarText.equals(termQingmingString)) isFestival = true;

                if (!isFestival && legalHolidayTag[index] == ILegalHoliday.DAY_TYPE_HOLIDAY) {
                    lunarText = mStrLegalHoliday;
                } else if (!isFestival && legalHolidayTag[index] == ILegalHoliday.DAY_TYPE_WORK_SHIFT) {
                    lunarText = mStrLegalWorkShift;
                }*/

				String showText = null;
	            if (mDisplayFestivalDay && mDisplayWorkAndShift && mDisplayLunarDate) {
	                String allExtraString = mLunarUtil.getLunarFestivalChineseString(t.year, (t.month + 1), t.monthDay);
	                boolean isFestival = allExtraString.contains(LunarUtil.DELIM);
	                String[] temp = allExtraString.split(LunarUtil.DELIM);
	                String lunarText = temp[0];

	                if (lunarText.equals(termQingmingString)) isFestival = true;

	                if (!isFestival && legalHolidayTag[index] == ILegalHoliday.DAY_TYPE_HOLIDAY) {
	                    lunarText = mStrLegalHoliday;
	                } else if (!isFestival && legalHolidayTag[index] == ILegalHoliday.DAY_TYPE_WORK_SHIFT) {
	                    lunarText = mStrLegalWorkShift;
	                } else if (isFestival && legalHolidayTag[index] == ILegalHoliday.DAY_TYPE_WORK_SHIFT
	                        && (mLunarMonthDayPaint.getColor() == highlightTextColor)) {
	                    mLunarMonthDayPaint.setColor(mColorLunarMonthDayDefault);
	                }

	                showText = lunarText;
	            } else if (mDisplayFestivalDay && mDisplayWorkAndShift && !mDisplayLunarDate) {
	                String allExtraString = mLunarUtil.getLunarFestivalChineseString(t.year, (t.month + 1), t.monthDay);
	                boolean isFestival = allExtraString.contains(LunarUtil.DELIM);
	                String[] temp = allExtraString.split(LunarUtil.DELIM);
	                String lunarText = temp[0];

	                if (lunarText.equals(termQingmingString)) isFestival = true;

	                boolean isWorkShift = false;
	                if (legalHolidayTag[index] != ILegalHoliday.DAY_TYPE_NORMAL) {
	                    isWorkShift = true;
	                }

	                if (!isFestival && legalHolidayTag[index] == ILegalHoliday.DAY_TYPE_HOLIDAY) {
	                    lunarText = mStrLegalHoliday;
	                } else if (!isFestival && legalHolidayTag[index] == ILegalHoliday.DAY_TYPE_WORK_SHIFT) {
	                    lunarText = mStrLegalWorkShift;
	                } else if (isFestival && legalHolidayTag[index] == ILegalHoliday.DAY_TYPE_WORK_SHIFT
	                        && (mLunarMonthDayPaint.getColor() == highlightTextColor)) {
	                    mLunarMonthDayPaint.setColor(mColorLunarMonthDayDefault);
	                }

	                if (isFestival || isWorkShift) {
	                    showText = lunarText;
	                }
	            } else if (mDisplayFestivalDay && !mDisplayWorkAndShift && mDisplayLunarDate) {
	                String allExtraString = mLunarUtil.getLunarFestivalChineseString(t.year, (t.month + 1), t.monthDay);
	                String[] temp = allExtraString.split(LunarUtil.DELIM);
	                String lunarText = temp[0];

	                showText = lunarText;
	            } else if (mDisplayFestivalDay && !mDisplayWorkAndShift && !mDisplayLunarDate) {
	                String allExtraString = mLunarUtil.getLunarFestivalChineseString(t.year, (t.month + 1), t.monthDay);
	                boolean isFestival = allExtraString.contains(LunarUtil.DELIM);
	                String[] temp = allExtraString.split(LunarUtil.DELIM);
	                String lunarText = temp[0];

	                if (lunarText.equals(termQingmingString)) isFestival = true;

	                if (isFestival) {
	                    showText = lunarText;
	                }
	            } else if (!mDisplayFestivalDay && mDisplayWorkAndShift && mDisplayLunarDate) {
	                String lunarText = mLunarUtil.getLunarChineseString(t.year, (t.month + 1), t.monthDay);

	                if (legalHolidayTag[index] == ILegalHoliday.DAY_TYPE_HOLIDAY) {
	                    lunarText = mStrLegalHoliday;
	                } else if (legalHolidayTag[index] == ILegalHoliday.DAY_TYPE_WORK_SHIFT) {
	                    lunarText = mStrLegalWorkShift;
	                }

	                showText = lunarText;
	            } else if (!mDisplayFestivalDay && mDisplayWorkAndShift && !mDisplayLunarDate) {
	                String lunarText = null;

	                if (legalHolidayTag[index] == ILegalHoliday.DAY_TYPE_HOLIDAY) {
	                    lunarText = mStrLegalHoliday;
	                } else if (legalHolidayTag[index] == ILegalHoliday.DAY_TYPE_WORK_SHIFT) {
	                    lunarText = mStrLegalWorkShift;
	                }

	                showText = lunarText;
	            } else if (!mDisplayFestivalDay && !mDisplayWorkAndShift && mDisplayLunarDate) {
	                String lunarText = mLunarUtil.getLunarChineseString(t.year, (t.month + 1), t.monthDay);
	                showText = lunarText;
	            }

	            if (TextUtils.isEmpty(showText)) {
	                if (drawSelectedDay) {
	                    String allExtraString = mLunarUtil.getLunarFestivalChineseString(t.year, (t.month + 1), t.monthDay);
		                boolean isFestival = allExtraString.contains(LunarUtil.DELIM);
		                String[] temp = allExtraString.split(LunarUtil.DELIM);
		                String lunarText = temp[0];

		                if (lunarText.equals(termQingmingString)) isFestival = true;

		                if (!isFestival && legalHolidayTag[index] == ILegalHoliday.DAY_TYPE_HOLIDAY) {
		                    lunarText = mStrLegalHoliday;
		                } else if (!isFestival && legalHolidayTag[index] == ILegalHoliday.DAY_TYPE_WORK_SHIFT) {
		                    lunarText = mStrLegalWorkShift;
		                }

		                showText = lunarText;
	                } else {
	                    continue;
	                }
	            }

				mLunarMonthDayPaint.getTextBounds(showText, 0, showText.length(), textBounds);
				int inWidthLunar = textBounds.width();
				
				int xLunarOffset = outLeft + (outWidth - inWidthLunar) / 2;
				//int yLunarOffset = (y + inHeight/* + GAP_BETWEEN_SOLAR_AND_LUNAR*/);
				int yLunarOffset = (int) (textBounds.height() + mMonthDayTextTopPadding * mScale);

				canvas.drawText(showText, xLunarOffset, yLunarOffset, mLunarMonthDayPaint);
			}

			// Gionee <jiangxiao> <2013-06-28> add for CR00831078 begin
			/*if(useRobotoMedium) {
    			mMonthDayPaint.setTypeface(mTfRobotoThin);
    			mMonthDayPaint.setFakeBoldText(true);
    			// mLunarMonthDayPaint.setTypeface(mTfRobotoThin);
    			
    			useRobotoMedium = false;
			}*/
			// Gionee <jiangxiao> <2013-06-28> add for CR00831078 end
    		
			// if(isBold) {
			// mMonthDayPaint.setFakeBoldText(isBold = false);
			// }
    	} // end of for loop
    } // end of drawWeekNumber()
    
    private static final int EXTRA_X_OFFSET_FOR_MONTH_NUMBER = 6;
    private int adjustWeekNumberXOffset(String text, int xOffset) {
    	/*if (text.charAt(0) == '1') {
    		xOffset -= EXTRA_X_OFFSET_FOR_MONTH_NUMBER;
		}*/
    	if (text.equals("1")) {
    		xOffset -= EXTRA_X_OFFSET_FOR_MONTH_NUMBER;
    	} else if (text.charAt(0) == '1') {
    		xOffset -= 2 * mScale;
    	}

    	return xOffset;
    }
    
    // Gionee <jiangxiao> <2013-06-28> modify for CR00831078 begin
    private static final int OFFSET_FOR_GRID_LEFT_TOP_CORNER = 1;
    private static final int EXTRA_SIZE_FOR_EVENT_TAG = 5;
    // Gionee <jiangxiao> <2013-06-28> modify for CR00831078 end
    
    private void drawEventsTag(Canvas canvas) {
    	if (isPeriod) return;

    	//Log.d("DEBUG", "drawEventsTag() has been invoked, id = " + this.mMonthWeekViewIndex);
    	if(mEvents == null || mEvents.size() == 0) {
    		//Log.d("DEBUG", "drawEventsTag(): mEvent is empty");
    		return;
    	}

        if (!mDisplayEventTag && isChineseEnvironment) return;

    	boolean[] hasEvents = new boolean[mNumDays];
    	for(int i = 0; i < mNumDays; ++i) {
    		hasEvents[i] = (mEvents.get(i) != null && mEvents.get(i).size() > 0);
    	}
    	
    	//mEventsTagPaint.setFilterBitmap(true);
    	for(int i = 0; i < hasEvents.length; ++i) {
    		if(hasEvents[i]) {
    			// draw event tags
    			//Log.d("WeekView", "drawEventsTag(): draw tag for week day " + i);
    			/*int x = computeDayLeftPosition(i);
    			int y = 0;
    			Bitmap bitmap = mEventTagBitmapDefault;
    			// if(mHasToday && mTodayIndex == i) {
    			// 	bitmap = BitmapFactory.decodeResource(this.getResources(), 
    			// 			R.drawable.gn_month_event_tag_today);
    			// }
    			// canvas.drawBitmap(bitmap, x, y, mEventsTagPaint);
    			
    			// Gionee <jiangxiao> <2013-06-28> modify for CR00831078 begin
    			r.left = x + this.dp2px(OFFSET_FOR_GRID_LEFT_TOP_CORNER);
    			r.right = computeDayLeftPosition(i + 1) + this.dp2px(EXTRA_SIZE_FOR_EVENT_TAG);
    			r.top = y + this.dp2px(OFFSET_FOR_GRID_LEFT_TOP_CORNER);
    			r.bottom = this.mHeight + this.dp2px(EXTRA_SIZE_FOR_EVENT_TAG);
    			// Gionee <jiangxiao> <2013-06-28> modify for CR00831078 end
    			
    			canvas.drawBitmap(bitmap, null, r, mEventsTagPaint);*/
    			//Log.d("DEBUG", "draw event tag for [" + mMonthWeekViewIndex + ", " + i + "]");

    			int x = computeDayLeftPosition(i + 1);

                int alpha = p.getAlpha();
                if (mClickedDayIndex == i /*|| (mHasToday && mTodayIndex == i)*/) {
                    p.setColor(mColorMonthDayTodaySelected);
                } else if (mFocusDay[i] || isWeekMode) {
                    p.setColor(mColorTappedDayToday);
                } else {
                    p.setColor(mColorMonthDaySecondary);
                }
                p.setAlpha(255);;
                canvas.drawCircle(x - 13 * mScale, (mMonthDayNumberTopPadding + (isChineseEnvironment ? 0 : 5)) * mScale,
                        (float) MONTH_DAY_EVENT_TAG_RADIUS * mScale / 2, p);
                p.setAlpha(alpha);
    		}
    	}
    	//mEventsTagPaint.setFilterBitmap(false);
    } // end of drawEventsTag()

    private void drawPeriodTag(Canvas canvas) {
    	if (!isPeriod || lastPeriodStartDay == 0) return;

        int todayJulianDay = Time.getJulianDay(mToday.toMillis(true), mToday.gmtoff);

        for (int i = 0; i <= mNumDays; i++) {
        	int julianDay = mFirstJulianDay + i;

        	int dayType = 0;
        	int drawableResourceId = 0;
        	if (julianDay >= lastPeriodStartDay && julianDay <= lastPeriodFinishDay) {
        		dayType = 1;
        		drawableResourceId = R.drawable.aurora_period_water_drop;
        	} else if (julianDay > lastPeriodFinishDay) {
        		int nextPeriodStartDay = lastPeriodStartDay;
        		while (nextPeriodStartDay <= julianDay) {
        			nextPeriodStartDay += defaultPeriodCycle;
        		}
        		int thisPeriodStartDay = nextPeriodStartDay - defaultPeriodCycle;
        		int thisPeriodFinishDay = thisPeriodStartDay + defaultPeriodLastDays - 1;

        		if (thisPeriodStartDay == lastPeriodStartDay) {
        			thisPeriodFinishDay = lastPeriodFinishDay;
        		}

        		int[] b = getTypeAndResourceId(julianDay, todayJulianDay, thisPeriodStartDay,
        				thisPeriodFinishDay, nextPeriodStartDay);
        		dayType = b[0];
        		drawableResourceId = b[1];
        	} else if (mPeriodInfos.size() == 1) {
        		int[] b = getTypeAndResourceId(julianDay, todayJulianDay, lastPeriodStartDay);
            	dayType = b[0];
            	drawableResourceId = b[1];
        	} else {
        		for (int j = 1; j < mPeriodInfos.size(); j++) {
        			int periodStartDay = mPeriodInfos.get(j).getStartDay();
        			int periodFinishDay = mPeriodInfos.get(j).getFinishDay();

        			if (julianDay >= periodStartDay && julianDay <= periodFinishDay) {
        				dayType = 1;
                		drawableResourceId = R.drawable.aurora_period_water_drop;
                		break;
        			} else if (julianDay > periodFinishDay) {
        				int thisPeriodStartDay = periodStartDay;
        				int thisPeriodFinishDay = periodFinishDay;
                		int nextPeriodStartDay = mPeriodInfos.get(j - 1).getStartDay();

                		int[] b = getTypeAndResourceId(julianDay, todayJulianDay, thisPeriodStartDay,
                				thisPeriodFinishDay, nextPeriodStartDay);
                		dayType = b[0];
                		drawableResourceId = b[1];
                		break;
        			} else if (j == mPeriodInfos.size() - 1) {
        				int thisPeriodStartDay = periodStartDay;
        				int[] b = getTypeAndResourceId(julianDay, todayJulianDay, thisPeriodStartDay);
                		dayType = b[0];
                		drawableResourceId = b[1];
        			}
        		}
        	}

        	if (dayType == 1 && mClickedDayIndex == i) {
    			drawableResourceId = R.drawable.aurora_period_water_drop_white;
    		}
    		if (drawableResourceId != 0) {
    			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawableResourceId);

    			int x = computeDayLeftPosition(i + 1);
    			canvas.drawBitmap(bitmap, x - 15 * mScale,
    					(mMonthDayNumberTopPadding + (isChineseEnvironment ? 0 : 5) - 6.5f) * mScale, p);
    		}
        }
    }

    private int[] getTypeAndResourceId(int julianDay, int todayJulianDay,
    		int thisPeriodStartDay, int thisPeriodFinishDay, int nextPeriodStartDay) {

    	int dayType = 0;
    	int drawableResourceId = 0;

    	if (julianDay >= thisPeriodStartDay && julianDay <= thisPeriodFinishDay) {
			dayType = 1;
		} else if (nextPeriodStartDay - 19 > thisPeriodFinishDay) {
			int plDay = nextPeriodStartDay - 14;
			int plStart = plDay - 5;
			int plEnd = plDay + 4;

			if (julianDay == plDay) {
				dayType = 2;
			} else if (julianDay >= plStart && julianDay <= plEnd) {
				dayType = 3;
			}
		}

		boolean hasToday = todayJulianDay >= thisPeriodStartDay;
		if (dayType == 1) {
			if (hasToday) {
				drawableResourceId = R.drawable.aurora_period_water_drop;
			} else {
				drawableResourceId = R.drawable.aurora_period_water_drop_gray;
			}
		} else if (dayType == 2) {
			if (hasToday) {
				drawableResourceId = R.drawable.aurora_period_flower;
			} else {
				drawableResourceId = R.drawable.aurora_period_flower_gray;
			}
		} else if (dayType == 3) {
			if (hasToday) {
				drawableResourceId = R.drawable.aurora_period_leaf;
			} else {
				drawableResourceId = R.drawable.aurora_period_leaf_gray;
			}
		}

		int[] a = new int[2];
		a[0] = dayType;
		a[1] = drawableResourceId;
    	return a;
    }

    private int[] getTypeAndResourceId(int julianDay, int todayJulianDay, int thisPeriodStartDay) {

    	int dayType = 0;
    	int drawableResourceId = 0;

		int plDay = thisPeriodStartDay - 14;
		int plStart = plDay - 5;
		int plEnd = plDay + 4;
		if (julianDay == plDay) {
			dayType = 2;
		} else if (julianDay >= plStart && julianDay <= plEnd) {
			dayType = 3;
		}

		boolean hasToday = todayJulianDay >= thisPeriodStartDay;
		if (dayType == 1) {
			if (hasToday) {
				drawableResourceId = R.drawable.aurora_period_water_drop;
			} else {
				drawableResourceId = R.drawable.aurora_period_water_drop_gray;
			}
		} else if (dayType == 2) {
			if (hasToday) {
				drawableResourceId = R.drawable.aurora_period_flower;
			} else {
				drawableResourceId = R.drawable.aurora_period_flower_gray;
			}
		} else if (dayType == 3) {
			if (hasToday) {
				drawableResourceId = R.drawable.aurora_period_leaf;
			} else {
				drawableResourceId = R.drawable.aurora_period_leaf_gray;
			}
		}

		int[] a = new int[2];
		a[0] = dayType;
		a[1] = drawableResourceId;
    	return a;
    }

    private int mMonthWeekViewIndex = -1;
    
    public void setMonthWeekViewIndex(int index) {
    	mMonthWeekViewIndex = index;
    }
    
    private Bitmap mBitmapLegalHolida = null;
    private Bitmap mBitmapLegalWorkShift = null;
    private int mTextColorLegalHoliday = 0;
    private int mTextColorLegalWorkShift = 0;
    private String mStrLegalHoliday = null;
    private String mStrLegalWorkShift = null;

    private static final int PADDING_HOLIDAY_TAG = 2; // 2dp
    private static final int HOLIDAY_TAG_BOUNDS_SIZE = 12; // 12dp
    private static final int HOLIDAY_TAG_TEXT_SIZE = 8; // 8sp
    private static final int HOLIDAY_TAG_TEXT_PADDING_X = 2;
    private static final int HOLIDAY_TAG_TEXT_PADDING_Y = 3;
    
    private void drawLegalHolidayTag(Canvas canvas) {
    	boolean loadHolidayIcon = false;
    	boolean loadWorkShiftIcon = false;
    	int[] legalHolidayTag = new int[mNumDays];
    	ILegalHoliday utils = LegalHolidayUtils.getInstance();
    	
    	for(int i = mFirstJulianDay; i < (mFirstJulianDay + mNumDays); ++i) {
    		int type = utils.getDayType(i);
    		legalHolidayTag[i - mFirstJulianDay] = type;
    		
    		if(type == ILegalHoliday.DAY_TYPE_HOLIDAY) {
    			loadHolidayIcon = true;
    		} else if(type == ILegalHoliday.DAY_TYPE_WORK_SHIFT) {
    			loadWorkShiftIcon = true;
    		}
    	}
    	
    	if(!loadHolidayIcon && !loadWorkShiftIcon) {
    		// Log.d("DEBUG", "no need to draw legal holiday tags");
    		return;
    	}
    	
    	// load icons
    	if(loadHolidayIcon) {
    		mBitmapLegalHolida = BitmapFactory.decodeResource(this.getResources(), 
					R.drawable.gn_calendar_holiday);
    	}
    	
    	if(loadWorkShiftIcon) {
    		mBitmapLegalWorkShift = BitmapFactory.decodeResource(this.getResources(), 
					R.drawable.gn_calendar_work);
    	}
    	
    	// draw tags
    	Rect textBounds = new Rect();
    	for(int i = 0; i < mNumDays; ++i) {
    		if(legalHolidayTag[i] != ILegalHoliday.DAY_TYPE_NORMAL) {
    			int x = computeDayLeftPosition(i + 1);
    			int y = 0;
    			
    			r.right = x - this.dp2px(PADDING_HOLIDAY_TAG);
    			r.left = r.right - this.dp2px(HOLIDAY_TAG_BOUNDS_SIZE);
    			r.top = y + this.dp2px(PADDING_HOLIDAY_TAG);
    			r.bottom = r.top + this.dp2px(HOLIDAY_TAG_BOUNDS_SIZE);
    			
    			Bitmap bitmap = null;
    			int textColor = 0;
    			float textSize = this.sp2px(HOLIDAY_TAG_TEXT_SIZE);
    			String text = null;
    			if(legalHolidayTag[i] == ILegalHoliday.DAY_TYPE_HOLIDAY) {
    				bitmap = mBitmapLegalHolida;
    				textColor = mTextColorLegalHoliday;
    				text = mStrLegalHoliday;
    			} else {
    				bitmap = mBitmapLegalWorkShift;
    				textColor = mTextColorLegalWorkShift;
    				text = mStrLegalWorkShift;
    			}

				canvas.drawBitmap(bitmap, null, r, mEventsTagPaint);
				
				mEventsTagPaint.getTextBounds(text, 0, text.length(), textBounds);
				
				// Gionee <jiangxiao> <2013-06-21> modify for CR00828462 begin
				// use fixed padding to draw text
//				int wOut = r.width();
//				int wIn = textBounds.width();
//				int hOut = r.height();
//				int hIn = textBounds.height();
//				Log.d("DEBUG", "TEXT BOUNDS: " + wIn + ", " + wOut);
//				Log.d("DEBUG", "TEXT BOUNDS: " + hIn + ", " + hOut);
//
//    			int xOffset = r.left + (wOut - wIn) / 2;
//    			int yOffset = r.bottom - (hOut - hIn) / 2 - 3;
				
				int xOffset = r.left + this.dp2px(HOLIDAY_TAG_TEXT_PADDING_X);
				int yOffset = r.bottom - this.dp2px(HOLIDAY_TAG_TEXT_PADDING_Y);
				// Gionee <jiangxiao> <2013-06-21> modify for CR00828462 end
				
				mEventsTagPaint.setTextSize(textSize);
				mEventsTagPaint.setColor(textColor);
				mEventsTagPaint.setTextAlign(Paint.Align.LEFT);
				canvas.drawText(text, xOffset, yOffset, mEventsTagPaint);
    		}
    	}
    }
    
    // Gionee <jiangxiao> <2013-04-22> add for CR000000 end
}
