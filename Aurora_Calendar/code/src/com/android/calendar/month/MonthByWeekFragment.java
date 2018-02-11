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
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Instances;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.Event;
import com.android.calendar.EventLoader;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.aurora.calendar.period.PeriodInfo;
import com.aurora.calendar.period.PeriodInfoAdapter;
import com.aurora.commemoration.db.RememberDayDao;
import com.aurora.commemoration.model.RememberDayInfo;
import com.gionee.calendar.GNCalendarUtils;
import com.gionee.calendar.month.GNMonthView;
import com.gionee.calendar.statistics.Statistics;

//Gionee <pengwei><20130807> modify for CR00850530 begin
public class MonthByWeekFragment extends SimpleDayPickerFragment implements
        CalendarController.EventHandler, LoaderManager.LoaderCallbacks<Cursor>, OnScrollListener,
        OnTouchListener, ViewSwitcher.ViewFactory {
    private static final String TAG = "MonthFragment";

    // Selection and selection args for adding event queries
    private static final String WHERE_CALENDARS_VISIBLE = Calendars.VISIBLE + "=1";
    private static final String INSTANCES_SORT_ORDER = Instances.START_DAY + ","
            + Instances.START_MINUTE + "," + Instances.TITLE;
    protected static boolean mShowDetailsInMonth = false;

    protected float mMinimumTwoMonthFlingVelocity;
    protected boolean mIsMiniMonth;
    protected boolean mHideDeclined;

    protected int mFirstLoadedJulianDay;
    protected int mLastLoadedJulianDay;

    private static final int WEEKS_BUFFER = 1;
    // How long to wait after scroll stops before starting the loader
    // Using scroll duration because scroll state changes don't update
    // correctly when a scroll is triggered programmatically.
    private static final int LOADER_DELAY = 200;
    // The minimum time between requeries of the data if the db is
    // changing
    private static final int LOADER_THROTTLE_DELAY = 500;

    private CursorLoader mLoader;
    private Uri mEventUri;
    private final Time mDesiredDay = new Time();

    private volatile boolean mShouldLoad = true;
    private boolean mUserScrolled = false;

    private int mEventsLoadingDelay;
    private boolean mShowCalendarControls;
    private boolean mIsDetached;

    private boolean initNextView = true;
    private boolean updateOnResume = false;

    // Gionee <Author: jiangxiao> <2013-04-10> add for CR000000 begin
    // new variables begin
    private ViewSwitcher mViewSwitcher = null;
    // this reference may not point to the current month view
    // private GNMonthView mCurrentMonthView = null;
    private EventLoader mEventLoader = null;
    // Gionee end
    // Gionee <Author: jiangxiao> <2013-04-10> add for CR000000 end

    private PeriodInfoAdapter mPeriodAdatper = null;
    private ArrayList<PeriodInfo> mPeriodInfos = null;

    private RememberDayDao mRememberDayDao = null;
    private List<RememberDayInfo> mRememberDays = null;

    private final Runnable mTZUpdater = new Runnable() {
        @Override
        public void run() {
            String tz = Utils.getTimeZone(mContext, mTZUpdater);
            mSelectedDay.timezone = tz;
            mSelectedDay.normalize(true);
            mTempTime.timezone = tz;
            mFirstDayOfMonth.timezone = tz;
            mFirstDayOfMonth.normalize(true);
            mFirstVisibleDay.timezone = tz;
            mFirstVisibleDay.normalize(true);
            if (mAdapter != null) {
                mAdapter.refresh();
            }
        }
    };

    private final Runnable mUpdateLoader = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                if (!mShouldLoad || mLoader == null) {
                    return;
                }
                // Stop any previous loads while we update the uri
                stopLoader();

                // Start the loader again
                mEventUri = updateUri();

                mLoader.setUri(mEventUri);
                mLoader.startLoading();
                mLoader.onContentChanged();
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Started loader with uri: " + mEventUri);
                }
            }
        }
    };

    // Used to load the events when a delay is needed
    Runnable mLoadingRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mIsDetached) {
                mLoader = (CursorLoader) getLoaderManager().initLoader(0, null,
                        MonthByWeekFragment.this);
            }
        }
    };

    Runnable mEventChangeRunnable = new Runnable() {
        @Override
        public void run() {
            updateOnResume = false;
        }
    };

    public void setCanEventChange() {
        updateOnResume = false;
    }

    public void setNoEventChange(long millis) {
        updateOnResume = true;
        getView().postDelayed(mEventChangeRunnable, millis);
    }

    Runnable mShowNextRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mIsDetached) showNext();
        }
    };

    private void showNext() {
        GNMonthView nextMonthView = (GNMonthView) mViewSwitcher.getNextView();
        nextMonthView.setRootView(this.getView());
        nextMonthView.setParams(mContext, mViewSwitcher, mAdapter, this, mSelectedDay, mEventLoader, mDayNamesHeader);
        nextMonthView.setInOutAnimation();
        mViewSwitcher.showNext();
        nextMonthView.highlightToday();
    }

    /**
     * Updates the uri used by the loader according to the current position of
     * the listview.
     *
     * @return The new Uri to use
     */
    private Uri updateUri() {
        SimpleWeekView child = (SimpleWeekView) mListView.getChildAt(0);
        if (child != null) {
            int julianDay = child.getFirstJulianDay();
            mFirstLoadedJulianDay = julianDay;
        }

        MonthWeekEventsView weekView = ((GNMonthView) mViewSwitcher.getCurrentView()).getFirstWeekView();
        if (weekView != null) {
            mFirstLoadedJulianDay = weekView.getFirstJulianDay();
        }

        // -1 to ensure we get all day events from any time zone
        mTempTime.setJulianDay(mFirstLoadedJulianDay - 1);
        long start = mTempTime.toMillis(true);
        mLastLoadedJulianDay = mFirstLoadedJulianDay + (mNumWeeks + 2 * WEEKS_BUFFER) * 7;
        // +1 to ensure we get all day events from any time zone
        mTempTime.setJulianDay(mLastLoadedJulianDay + 1);
        long end = mTempTime.toMillis(true);
        Log.d("WeekView", "MonthFragment.updateUri() query start at " + GNCalendarUtils.printDate(mFirstLoadedJulianDay - 1));
        Log.d("WeekView", "MonthFragment.updateUri() query end at " + GNCalendarUtils.printDate(mLastLoadedJulianDay + 1));

        // Create a new uri with the updated times
        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, start);
        ContentUris.appendId(builder, end);
        return builder.build();
    }

    // Extract range of julian days from URI
    private void updateLoadedDays() {
        List<String> pathSegments = mEventUri.getPathSegments();
        int size = pathSegments.size();
        if (size <= 2) {
            return;
        }
        long first = Long.parseLong(pathSegments.get(size - 2));
        long last = Long.parseLong(pathSegments.get(size - 1));
        mTempTime.set(first);
        mFirstLoadedJulianDay = Time.getJulianDay(first, mTempTime.gmtoff);
        mTempTime.set(last);
        mLastLoadedJulianDay = Time.getJulianDay(last, mTempTime.gmtoff);
    }

    protected String updateWhere() {
        // TODO fix selection/selection args after b/3206641 is fixed
        String where = WHERE_CALENDARS_VISIBLE;
        if (mHideDeclined /*M: || !mShowDetailsInMonth*/) {
            where += " AND " + Instances.SELF_ATTENDEE_STATUS + "!="
                    + Attendees.ATTENDEE_STATUS_DECLINED;
        }
        return where;
    }

    private void stopLoader() {
        synchronized (mUpdateLoader) {
            mHandler.removeCallbacks(mUpdateLoader);
            if (mLoader != null) {
                mLoader.stopLoading();
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Stopped loader from loading");
                }
            }
        }
    }
    
    // Gionee <Author: jiangxiao> <2013-04-11> add for CR000000 begin
    // methods for Fragment lifecycle begin
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v;
        // Gionee <Author: jiangxiao> <2013-04-10> add for CR00000000 begin
//        if (mIsMiniMonth) {
//            v = inflater.inflate(R.layout.month_by_week, container, false);
//        } else {
//            v = inflater.inflate(R.layout.full_month_by_week, container, false);
//        }
        v = inflater.inflate(R.layout.gn_month_by_week, container, false);
        mDayNamesHeader = (ViewGroup) v.findViewById(R.id.day_names);
        
        mBgColorMonthHeader = getResources().getColor(R.color.month_header_bg_color/*gn_bg_month_header*/);
        mColorDayNames = getResources().getColor(R.color.gn_text_month_header_default);
        mColorDayNamesWeekend = getResources().getColor(R.color.gn_text_month_header_weekend);
        mColorDayNamesToday = getResources().getColor(R.color.gn_text_month_header_today);
        
        mViewSwitcher = (ViewSwitcher) v.findViewById(R.id.month_view_switcher);
        mViewSwitcher.setFactory(this);
        
        mEventLoader = new EventLoader(this.getActivity());
        
        // Log.d("DEBUG", "MonthFragment.onCreateView(): set current view time as " + GNCalendarUtils.printDate(mSelectedDay));
        GNMonthView currentMonthView = (GNMonthView)mViewSwitcher.getCurrentView();
        // Log.d("DEBUG", "invoked GNMonthView.setParams() 1");
        
        // gionee <jiangxiao> <2013-08-13> add for CR00845772 begin
        // check mSelectedDay
		if(!GNCalendarUtils.isYearInRange(mSelectedDay)) {
			GNCalendarUtils.correctInvalidTime(mSelectedDay);
		}
		// gionee <jiangxiao> <2013-08-13> add for CR00845772 begin
        currentMonthView.setParams(mContext, mViewSwitcher, mAdapter, this, 
        		mSelectedDay, mEventLoader, mDayNamesHeader);
        currentMonthView.setPeriodInfos(mPeriodInfos);
        currentMonthView.setHighlightFlag(true);
        currentMonthView.highlightToday();
        // Gionee <Author: jiangxiao> <2013-04-10> add for CR00000000 end
        
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnTouchListener(this);
        if (!mIsMiniMonth) {
            mListView.setBackgroundColor(getResources().getColor(R.color.month_bgcolor));
        }

        // To get a smoother transition when showing this fragment, delay loading of events until
        // the fragment is expended fully and the calendar controls are gone.
        if (mShowCalendarControls) {
        	Log.d("DEBUG", "to load events 1");
            mListView.postDelayed(mLoadingRunnable, mEventsLoadingDelay);
        } else {
        	Log.d("DEBUG", "to load events 2");
            mLoader = (CursorLoader) getLoaderManager().initLoader(0, null, this);
        }
        mAdapter.setListView(mListView);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mTZUpdater.run();
        if (mAdapter != null) {
            mAdapter.setSelectedDay(mSelectedDay);
        }
        mIsDetached = false;

        ViewConfiguration viewConfig = ViewConfiguration.get(activity);
        mMinimumTwoMonthFlingVelocity = viewConfig.getScaledMaximumFlingVelocity() / 2;
        Resources res = activity.getResources();
        mShowCalendarControls = Utils.getConfigBool(activity, R.bool.show_calendar_controls);
        // Synchronized the loading time of the month's events with the animation of the
        // calendar controls.
        if (mShowCalendarControls) {
            mEventsLoadingDelay = res.getInteger(R.integer.calendar_controls_animation_time);
        }
        mShowDetailsInMonth = res.getBoolean(R.bool.show_details_in_month);
        
        // Gionee <jiangxiao> <2013-04-11> add for CR000000 begin
        mContext = activity;
        // Gionee <jiangxiao> <2013-04-11> add for CR000000 end

        if (Utils.displayPeriod(mContext)) {
            mPeriodAdatper = new PeriodInfoAdapter(mContext);
            mPeriodAdatper.open();
        	mPeriodInfos = mPeriodAdatper.queryAll();
        } else {
        	mRememberDayDao = new RememberDayDao(mContext);
        	mRememberDayDao.openDatabase();
        	mRememberDays = mRememberDayDao.getScheduleRememberDayList();
        }
    }

    public PeriodInfoAdapter getPeriodInfoAdatper() {
        if (mPeriodAdatper == null) {
            mPeriodAdatper = new PeriodInfoAdapter(mContext);
            mPeriodAdatper.open();
        }
        return mPeriodAdatper;
    }

    @Override
    public void onDetach() {
        mIsDetached = true;
        super.onDetach();
        if (mShowCalendarControls) {
            if (mListView != null) {
                mListView.removeCallbacks(mLoadingRunnable);
            }
        }

        // Gionee <jiangxiao> <2013-04-11> add for CR000000 begin
        mContext = null;
        // Gionee <jiangxiao> <2013-04-11> add for CR000000 end

        if (mPeriodAdatper != null) {
        	mPeriodAdatper.close();
        	mPeriodAdatper = null;
        }
        if (mRememberDayDao != null) {
        	mRememberDayDao.closeDatabase();
        	mRememberDayDao = null;
        }
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	GNMonthView currentMonthView = (GNMonthView) mViewSwitcher.getCurrentView();
    	currentMonthView.setRootView(this.getView());
        currentMonthView.onResume();

        if (initNextView) {
            initNextView = false;
            // this.getView().post(mShowNextRunnable);
        } else {
            // setNoEventChange(8000);
        	updateRememberDays();
            eventsChanged();
        }

    	Log.d("GNMonthView", "invoke EventLoader.startBackgroundThread() in MonthByWeekFragment");
    	mEventLoader.startBackgroundThread();
    	
    	if(mStatistics == null) {
    		mStatistics = Statistics.getInstance();
    	}
    	Statistics.onResume(getActivity());
    }
    
    @Override
    public void onPause() {
    	super.onPause();

    	GNMonthView currentMonthView = (GNMonthView) mViewSwitcher.getCurrentView();
        currentMonthView.onPause();

    	Log.d("GNMonthView", "invoke EventLoader.stopBackgroundThread() in MonthByWeekFragment");
    	mEventLoader.stopBackgroundThread();
    	Statistics.onPause(getActivity());
    }
    // methods for Fragment lifecycle end
    // Gionee <Author: jiangxiao> <2013-04-11> add for CR000000 end

    @Override
    protected void setUpAdapter() {
        mFirstDayOfWeek = Utils.getFirstDayOfWeek(mContext);
        mShowWeekNumber = Utils.getShowWeekNumber(mContext);

        HashMap<String, Integer> weekParams = new HashMap<String, Integer>();
        weekParams.put(SimpleWeeksAdapter.WEEK_PARAMS_NUM_WEEKS, mNumWeeks);
        weekParams.put(SimpleWeeksAdapter.WEEK_PARAMS_SHOW_WEEK, mShowWeekNumber ? 1 : 0);
        weekParams.put(SimpleWeeksAdapter.WEEK_PARAMS_WEEK_START, mFirstDayOfWeek);
        weekParams.put(MonthByWeekAdapter.WEEK_PARAMS_IS_MINI, mIsMiniMonth ? 1 : 0);
        weekParams.put(SimpleWeeksAdapter.WEEK_PARAMS_JULIAN_DAY,
                Time.getJulianDay(mSelectedDay.toMillis(true), mSelectedDay.gmtoff));
        weekParams.put(SimpleWeeksAdapter.WEEK_PARAMS_DAYS_PER_WEEK, mDaysPerWeek);
        if (mAdapter == null) {
            mAdapter = new MonthByWeekAdapter(getActivity(), weekParams);
            mAdapter.registerDataSetObserver(mObserver);
        } else {
            mAdapter.updateParams(weekParams);
        }
        mAdapter.notifyDataSetChanged();
    }

    public MonthByWeekFragment() {
        this(System.currentTimeMillis(), true);
    }

    public MonthByWeekFragment(long initialTime, boolean isMiniMonth) {
        super(initialTime);
        mIsMiniMonth = isMiniMonth;
    }

    @Override
    protected void setUpHeader() {
        if (mIsMiniMonth) {
            super.setUpHeader();
            return;
        }

        mDayLabels = new String[7];
        /*for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
            mDayLabels[i - Calendar.SUNDAY] = DateUtils.getDayOfWeekString(i,
                    DateUtils.LENGTH_MEDIUM).toUpperCase();
        }*/
        mDayLabels = mContext.getResources().getStringArray(R.array.day_labels);
    }

    // TODO
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mIsMiniMonth) {
            return null;
        }
        CursorLoader loader;
        synchronized (mUpdateLoader) {
            mFirstLoadedJulianDay = Time.getJulianDay(mSelectedDay.toMillis(true), mSelectedDay.gmtoff) - (mNumWeeks * 7 / 2);
        	Log.d("DEBUG", "onCreateLoader() mSelectedDay is " + GNCalendarUtils.printDate(mSelectedDay));
        	Log.d("DEBUG", "onCreateLoader() mFirstLoadedJulianDay is " + GNCalendarUtils.printDate(mFirstLoadedJulianDay));
            mEventUri = updateUri();
            String where = updateWhere();

            loader = new CursorLoader(
                    getActivity(), mEventUri, Event.EVENT_PROJECTION, where,
                    null /* WHERE_CALENDARS_SELECTED_ARGS */, INSTANCES_SORT_ORDER);
            loader.setUpdateThrottle(LOADER_THROTTLE_DELAY);
        }
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Returning new loader with uri: " + mEventUri);
        }
        return loader;
    }

    @Override
    public void doResumeUpdates() {
        mFirstDayOfWeek = Utils.getFirstDayOfWeek(mContext);
        mShowWeekNumber = Utils.getShowWeekNumber(mContext);
        boolean prevHideDeclined = mHideDeclined;
        mHideDeclined = Utils.getHideDeclinedEvents(mContext);
        if (prevHideDeclined != mHideDeclined && mLoader != null) {
            mLoader.setSelection(updateWhere());
        }
        mDaysPerWeek = Utils.getDaysPerWeek(mContext);
        // Gionee <jiangxiao> <2013-04-22> add for CR000000 begin
        // updateHeader();
        updateMonthHeader();
        // Gionee <jiangxiao> <2013-04-22> add for CR000000 end
        mAdapter.setSelectedDay(mSelectedDay);
        mTZUpdater.run();
        mTodayUpdater.run();
        goTo(mSelectedDay.toMillis(true), false, true, false);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        synchronized (mUpdateLoader) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Found " + data.getCount() + " cursor entries for uri " + mEventUri);
            }
            CursorLoader cLoader = (CursorLoader) loader;
            if (mEventUri == null) {
                mEventUri = cLoader.getUri();
                updateLoadedDays();
            }
            if (cLoader.getUri().compareTo(mEventUri) != 0) {
                // We've started a new query since this loader ran so ignore the
                // result
                return;
            }
            ArrayList<Event> events = new ArrayList<Event>();
            Event.buildEventsFromCursor(
                    events, data, mContext, mFirstLoadedJulianDay, mLastLoadedJulianDay);
            ((MonthByWeekAdapter) mAdapter).setEvents(mFirstLoadedJulianDay,
                    mLastLoadedJulianDay - mFirstLoadedJulianDay + 1, events);
            // Gionee <jiangxiao> <2013-04-22> add for CR000000 begin
            // we should refresh month to draw event tag
            
            // should not invoke GNMonthView.setParams() method, because it will cause
            // today's highlight effect disappeared automatically.
            GNMonthView currentMonthView = (GNMonthView) mViewSwitcher.getCurrentView();
            // currentMonthView.setParams(mContext, mViewSwitcher, mAdapter, this, mSelectedDay, mEventLoader, mDayNamesHeader);
            currentMonthView.setMonthWeekAdapter(mAdapter);
            
            // Time today = new Time();
            // today.setToNow();
            // today.normalize(true);
            // should not invoke gotoToday(today) method directly, if current view is not today
            // this method will cause the view switch to the current month automatically
            
            // invoke gotoDate(mSelectedDay) will cause today highlight effect
            // to be moved to other cell automatically
            // currentMonthView.gotoDate(mSelectedDay);
            
            // TODO: we should just update agenda list & event tags
            // Gionee <jiangxiao> <2013-04-22> add for CR000000 end
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void eventsChanged() {
        // TODO remove this after b/3387924 is resolved
        if (mLoader != null) {
            mLoader.forceLoad();
        }
        
        GNMonthView currentMonthView = (GNMonthView) mViewSwitcher.getCurrentView();
		currentMonthView.loadEventsOfSelectedDay();
    }

    @Override
    public long getSupportedEventTypes() {
        return EventType.GO_TO | EventType.EVENTS_CHANGED;
    }

	@Override
	public void handleEvent(EventInfo event) {
		if (event.eventType == EventType.GO_TO) {
			// Gionee <jiangxiao> <2013-04-18> delete for CR000000 start
			// boolean animate = true;
			// if (mDaysPerWeek * mNumWeeks * 2 < Math.abs(
			// Time.getJulianDay(event.selectedTime.toMillis(true),
			// event.selectedTime.gmtoff)
			// - Time.getJulianDay(mFirstVisibleDay.toMillis(true),
			// mFirstVisibleDay.gmtoff)
			// - mDaysPerWeek * mNumWeeks / 2)) {
			// animate = false;
			// }
			// mDesiredDay.set(event.selectedTime);
			// mDesiredDay.normalize(true);
			// /// M: whether if animate the selected day @{
			// boolean animateSelectedDay = (event.extraLong &
			// CalendarController.EXTRA_GOTO_TODAY) != 0;
			// /// @}
			// boolean animateToday = (event.extraLong &
			// CalendarController.EXTRA_GOTO_TODAY) != 0;
			// boolean delayAnimation = goTo(event.selectedTime.toMillis(true),
			// animate, true, false);
			// /// M: if animate the selected day
			// if (animateSelectedDay) {
			// /// M: set real selected time @{
			// ((MonthByWeekAdapter)
			// mAdapter).setRealSelectedDay(event.selectedTime);
			// /// @}
			// // If we need to flash today start the animation after any
			// // movement from listView has ended.
			// mHandler.postDelayed(new Runnable() {
			// @Override
			// public void run() {
			// /// M: animate the selected day
			// ((MonthByWeekAdapter) mAdapter).animateSelectedDay();
			// mAdapter.notifyDataSetChanged();
			// }
			// }, delayAnimation ? GOTO_SCROLL_DURATION : 0);
			// }
			// Gionee <jiangxiao> <2013-04-18> delete for CR000000 end
			// Gionee <jiangxiao> <2013-04-18> add for CR000000 begin
			// handle back to today event
			Time gotoDate = new Time();
			if ((event.extraLong & CalendarController.EXTRA_GOTO_TODAY) != 0) {
				// Time todayTime = event.startTime;
				gotoDate.setToNow();
				gotoDate.normalize(true);
				Statistics.onEvent(getActivity(), Statistics.MONTH_DAY_BACK_TO_TODAY);
			} else {
				gotoDate.set(event.startTime);
				// Log.d("DEBUG", "gotoDate: " +
				// GNCalendarUtils.printDate(gotoDate));
			}
			if (mViewSwitcher != null) {
				Log.d("DEBUG", "MonthFragment.handleEvent(): gotoDate " +
						GNCalendarUtils.printDate(gotoDate));
				GNMonthView currentMonthView = (GNMonthView) mViewSwitcher
						.getCurrentView();
				currentMonthView.gotoDate(gotoDate);
			}
		} else if (event.eventType == EventType.EVENTS_CHANGED) {
			// eventsChanged();
            if (!updateOnResume) {
                // setNoEventChange(2000);
                eventsChanged();
            }
		}
	}

	public void updateRememberDays() {
		if (mRememberDayDao != null) {
			mRememberDays = mRememberDayDao.getScheduleRememberDayList();
		}
	}

	public List<RememberDayInfo> getRememberDays() {
		return mRememberDays;
	}

    @Override
    protected void setMonthDisplayed(Time time, boolean updateHighlight) {
        super.setMonthDisplayed(time, updateHighlight);
        if (!mIsMiniMonth) {
            boolean useSelected = false;
            if (time.year == mDesiredDay.year && time.month == mDesiredDay.month) {
                mSelectedDay.set(mDesiredDay);
                mAdapter.setSelectedDay(mDesiredDay);
                useSelected = true;
            } else {
                mSelectedDay.set(time);
                mAdapter.setSelectedDay(time);
            }
            CalendarController controller = CalendarController.getInstance(mContext);
            if (mSelectedDay.minute >= 30) {
                mSelectedDay.minute = 30;
            } else {
                mSelectedDay.minute = 0;
            }
            long newTime = mSelectedDay.normalize(true);
            if (newTime != controller.getTime() && mUserScrolled) {
                long offset = useSelected ? 0 : DateUtils.WEEK_IN_MILLIS * mNumWeeks / 3;
                controller.setTime(newTime + offset);
            }
            
			// Gionee <jiangxiao> <2013-06-25> delete for CR00828505 begin
			// no need to update time here
			// /M:@{
			// /M:mTempTime is the time to move to
			// controller.sendEvent(this, EventType.UPDATE_TITLE, time, time,
			// mTempTime, -1,
			// ViewType.CURRENT, DateUtils.FORMAT_SHOW_DATE |
			// DateUtils.FORMAT_NO_MONTH_DAY
			// | DateUtils.FORMAT_SHOW_YEAR, null, null);
			// /@}
			// Gionee <jiangxiao> <2013-06-25> delete for CR00828505 end
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        synchronized (mUpdateLoader) {
            if (scrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                mShouldLoad = false;
                stopLoader();
                mDesiredDay.setToNow();
            } else {
                mHandler.removeCallbacks(mUpdateLoader);
                mShouldLoad = true;
                mHandler.postDelayed(mUpdateLoader, LOADER_DELAY);
            }
        }
        if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            mUserScrolled = true;
        }

        mScrollStateChangedRunnable.doScrollStateChange(view, scrollState);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mDesiredDay.setToNow();
        return false;
        // TODO post a cleanup to push us back onto the grid if something went
        // wrong in a scroll such as the user stopping the view but not
        // scrolling
    }
    
    //Gionee <Author: jiangxiao> <2013-04-11> add for CR000000 begin
    // impl ViewSwitcher.ViewFactory
    @Override
    public View makeView() {
    	GNMonthView view = new GNMonthView(this.getActivity());
		view.setLayoutParams(new ViewSwitcher.LayoutParams(
				ViewSwitcher.LayoutParams.MATCH_PARENT, 0));
		
		return view;
    }
    
    private int mBgColorMonthHeader = 0;
    private int mColorDayNames = 0;
    private int mColorDayNamesWeekend = 0;
    private int mColorDayNamesToday = 0;
    
    private void updateMonthHeader() {
    	// mDayNamesHeader.setBackgroundColor(mBgColorMonthHeader);
    	
    	TextView label = (TextView) mDayNamesHeader.findViewById(R.id.wk_label);
    	
    	// In default, we don't display week number column
		label.setVisibility(View.GONE);
		
		CalendarController controller = CalendarController.getInstance(mContext);
		Time t = new Time();
		t.set(controller.getTime());
		t.normalize(true);
		int currentWeekDay = t.weekDay;
		
		int firstDayOfWeek = Utils.getFirstDayOfWeek(this.getActivity());
		// TODO: set different first day to test this
		for(int i = 1; i < DAYS_PER_WEEK + 1; ++i) {
			int pos = (i - 1 + firstDayOfWeek) % DAYS_PER_WEEK;
			label = (TextView) mDayNamesHeader.getChildAt(i);
			// just use special color in the month which contain today
			// gionee <jiangxiao> <2013-06-07> delete for CR00821949 begin
//			if(pos == currentWeekDay && ((GNMonthView)mViewSwitcher.getCurrentView()).hasToday()) {
//				label.setTextColor(mColorDayNamesToday);
//			} else {
				/*if(pos == Time.SUNDAY || pos == Time.SATURDAY) {
					label.setTextColor(mColorDayNamesWeekend);
				} else {
					label.setTextColor(mColorDayNames);
				}*/
//			}
			// gionee <jiangxiao> <2013-06-07> delete for CR00821949 end
			label.setText(mDayLabels[pos]);
            label.setVisibility(View.VISIBLE);
		}
    	
    	mDayNamesHeader.invalidate();
    }
    
    // callback which will be invoked after month switching animation is finished
    public void onMonthViewSwitched(Time monthDate) {
    	if(monthDate == null) throw new RuntimeException("onMonthViewSwitched() receive null Time object");
    	
    	// re-load events by monthDate
    	Log.d("DEBUG", "re-load events by monthDate " + GNCalendarUtils.printDate(monthDate));
    	
		// gionee <jiangxiao> <2013-06-08> add for CR00822662 begin
		// make sure the time range overlaps the whole month
    	monthDate.monthDay = 1;
    	monthDate.normalize(true);
    	// update load days
    	// We should load everyday's events in the current view
    	int monthDateJulianDay = Utils.getJulianDayInGeneral(monthDate, true);
    	mFirstLoadedJulianDay = monthDateJulianDay - 7;
    	mLastLoadedJulianDay = monthDateJulianDay + 6 * 7;
		// gionee <jiangxiao> <2013-06-08> add for CR00822662 end
    	
    	// update URI
    	mTempTime.setJulianDay(mFirstLoadedJulianDay);
        long startMillis = mTempTime.toMillis(true);
        mTempTime.setJulianDay(mLastLoadedJulianDay);
        long endMillis = mTempTime.toMillis(true);
        
    	Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);
        mEventUri = builder.build();
        
        // load events
        mLoader.setUri(mEventUri);
        mLoader.forceLoad();
    }
    
	Statistics mStatistics = null;
    //Gionee <Author: jiangxiao> <2013-04-11> add for CR000000 end
}
//Gionee <pengwei><20130807> modify for CR00850530 end