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

package com.gionee.calendar.day;

import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.android.calendar.DayView;
import com.android.calendar.Event;
import com.android.calendar.EventLoader;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.gionee.calendar.GNCalendarUtils;
import com.gionee.calendar.statistics.Statistics;
import com.gionee.calendar.view.Log;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import android.widget.ViewSwitcher.ViewFactory;

import com.mediatek.calendar.LogUtil;
import android.content.SharedPreferences;
import com.gionee.astro.GNAstroUtils;
import static com.gionee.astro.GNAstroUtils.KEY_ASTRO_INDEX;
//Gionee <pengwei><2013-04-12> modify for DayView begin
/**
 * This is the base class for Day and Week Activities.
 */
//Gionee <pengwei><20130807> modify for CR00850530 begin
public class GNDayFragment extends Fragment implements
		CalendarController.EventHandler, ViewFactory {
	/**
	 * The view id used for all the views we create. It's OK to have all child
	 * views have the same ID. This ID is used to pick which view receives focus
	 * when a view hierarchy is saved / restore
	 */
	// /M:@{
	private static final String TAG = "DayFragment";
	// /@}
	private static final int VIEW_ID = 1;

	protected static final String BUNDLE_KEY_RESTORE_TIME = "key_restore_time";

	protected ProgressBar mProgressBar;
	protected ViewSwitcher mViewSwitcher;
	protected Animation mInAnimationForward;
	protected Animation mOutAnimationForward;
	protected Animation mInAnimationBackward;
	protected Animation mOutAnimationBackward;
	EventLoader mEventLoader;
	private static final int SELECTION_HIDDEN = 0;
	private static final int SELECTION_PRESSED = 1; // D-pad down but not up yet
	private static final int SELECTION_SELECTED = 2;
	private static final int SELECTION_LONGPRESS = 3;
	private boolean mOnFlingCalled;
	private int mViewStartX;
	private int mViewWidth;
	/**
	 * The initial state of the touch mode when we enter this view.
	 */
	private static final int TOUCH_MODE_INITIAL_STATE = 0;

	/**
	 * Indicates we just received the touch event and we are waiting to see if
	 * it is a tap or a scroll gesture.
	 */
	private static final int TOUCH_MODE_DOWN = 1;

	/**
	 * Indicates the touch gesture is a vertical scroll
	 */
	private static final int TOUCH_MODE_VSCROLL = 0x20;

	/**
	 * Indicates the touch gesture is a horizontal scroll
	 */
	private static final int TOUCH_MODE_HSCROLL = 0x40;

	private int mTouchMode = TOUCH_MODE_INITIAL_STATE;
	Time mSelectedDay = new Time();

	private final Runnable mTZUpdater = new Runnable() {
		@Override
		public void run() {
			if (!GNDayFragment.this.isAdded()) {
				return;
			}
			String tz = Utils.getTimeZone(getActivity(), mTZUpdater);
			mSelectedDay.timezone = tz;
			mSelectedDay.normalize(true);
		}
	};

	private int mNumDays;
	private float mAnimationDistance = 0;

	public GNDayFragment() {
		mSelectedDay.setToNow();
	}

	public GNDayFragment(long timeMillis, int numOfDays) {
		mNumDays = numOfDays;
		if (timeMillis == 0) {
			mSelectedDay.setToNow();
		} else {
			mSelectedDay.set(timeMillis);
		}
	}

	/**
	 * M: pass the context in to get original displayed time in our calendar
	 * 
	 * @param context
	 * @param timeMillis
	 * @param numOfDays
	 */
	public GNDayFragment(Context context, long timeMillis, int numOfDays) {
		mSelectedDay = Utils.getValidTimeInCalendar(context, timeMillis);
	}

	private Context context;
	private DisplayMetrics mDm;
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		context = getActivity();

		mInAnimationForward = AnimationUtils.loadAnimation(context,
				R.anim.slide_left_in);
		mOutAnimationForward = AnimationUtils.loadAnimation(context,
				R.anim.slide_left_out);
		mInAnimationBackward = AnimationUtils.loadAnimation(context,
				R.anim.slide_right_in);
		mOutAnimationBackward = AnimationUtils.loadAnimation(context,
				R.anim.slide_right_out);
		mHScrollInterpolator = new ScrollInterpolator();
		mEventLoader = new EventLoader(context);
		mDm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(mDm);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.gn_day_fragment, null);
		mViewSwitcher = (ViewSwitcher) v.findViewById(R.id.day_switcher);
		Log.v("GNDayFragment---onCreateView---mViewSwitcher---" + mViewSwitcher);
		mViewSwitcher.setFactory(this);
		mViewSwitcher.getCurrentView().requestFocus();
		// ((View) mViewSwitcher.getCurrentView()).updateTitle();

		return v;
	}

	private View mNewDayView;

	public View makeView() {
		// mTZUpdater.run();
		mNewDayView = CreateView();
		Log.v("GNDayFragment---makeView---view---" + mNewDayView);
		mNewDayView.setId(VIEW_ID);
		mNewDayView.setLayoutParams(new ViewSwitcher.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return mNewDayView;
	}

	private View CreateView() {
		GNDayView mDayView = new GNDayView(this.getActivity(), mViewSwitcher,
				CalendarController.getInstance(getActivity()), mEventLoader,mDm,astroNameInt);
		mDayView.loadView(mSelectedDay);
		return mDayView;
	}
	private int astroNameInt;
	@Override
	public void onResume() {
		super.onResume();
		Statistics.onResume(getActivity());
		SharedPreferences sharedPreferences = this.getActivity().
		getPreferences(Context.MODE_PRIVATE);
		Log.v("GNDayFragment---onResume---astroNameInt == " + astroNameInt);
		mEventLoader.startBackgroundThread();
		mTZUpdater.run();
		eventsChanged();
		GNDayView view = (GNDayView) mViewSwitcher.getCurrentView();
		view = (GNDayView) mViewSwitcher.getNextView();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// long time = getSelectedTimeInMillis();
		// if (time != -1) {
		// outState.putLong(BUNDLE_KEY_RESTORE_TIME, time);
		// }
	}

	@Override
	public void onPause() {
		super.onPause();
		GNDayView view = (GNDayView) mViewSwitcher.getCurrentView();
		// view.cleanup();
		view = (GNDayView) mViewSwitcher.getNextView();
		// view.cleanup();
		mEventLoader.stopBackgroundThread();

		// Stop events cross-fade animation
		// view.stopEventsAnimation();
		// ((View) mViewSwitcher.View()).stopEventsAnimation();
		Statistics.onPause(getActivity());
	}

	void startProgressSpinner() {
		// start the progress spinner
		mProgressBar.setVisibility(View.VISIBLE);
	}

	void stopProgressSpinner() {
		// stop the progress spinner
		mProgressBar.setVisibility(View.GONE);
	}

	private void goTo(Time goToTime, boolean ignoreTime, boolean animateToday) {
		Log.v("GNDayView---goto---");
		Log.v("GNDayView---goto---goToTime == " + goToTime);
		if (mViewSwitcher == null) {
			// The view hasn't been set yet. Just save the time and use it
			// later.
			mSelectedDay.set(goToTime);
			return;
		}
//		if(DayUtils.dateOutOfRange(goToTime)){
//			GNCalendarUtils.showToast(this.getActivity(),this.getResources().getString(R.string.gn_day_time_out_of_range),Toast.LENGTH_SHORT);
//			return;
//		}
		GNDayView currentView = (GNDayView) mViewSwitcher.getCurrentView();
		// /M:@{
		if (currentView == null) {
			Log.v("getCurrentView() return null,return");
			return;
		}
		// /@}
		// How does goTo time compared to what's already displaying?
		int diff = currentView.compareToVisibleTimeRange(goToTime);

		if (diff == 0) {
			// In visible range. No need to switch view
//			currentView.loadView(currentView, goToTime);
			Log.v("diff---1");
		} else {
			// Figure out which way to animate
			Log.v("diff---2===" + diff);
			GNDayView next = (GNDayView) mViewSwitcher.getNextView();
			next.loadView(goToTime);
			GNDayView currentDay = (GNDayView) mViewSwitcher.getCurrentView();
			if (diff > 0) {
				currentDay.switchViews(true, 0.0f, currentDay.mViewWidth, 0);
			} else {
				currentDay.switchViews(false, 0.0f, currentDay.mViewWidth, 0);
			}

		}
	}

	private ScrollInterpolator mHScrollInterpolator;

	private class ScrollInterpolator implements Interpolator {
		public ScrollInterpolator() {
		}

		public float getInterpolation(float t) {
			t -= 1.0f;
			t = t * t * t * t * t + 1;

			if ((1 - t) * mAnimationDistance < 1) {
				cancelAnimation();
			}
			Log.v("GNDayFragment---t---" + t);
			return 1.0f;
		}
	}

	private void cancelAnimation() {
		Animation in = mViewSwitcher.getInAnimation();
		if (in != null) {
			// cancel() doesn't terminate cleanly.
			in.scaleCurrentDuration(0);
		}
		Animation out = mViewSwitcher.getOutAnimation();
		if (out != null) {
			// cancel() doesn't terminate cleanly.
			out.scaleCurrentDuration(0);
		}
	}

	/**
	 * Returns the selected time in milliseconds. The milliseconds are measured
	 * in UTC milliseconds from the epoch and uniquely specifies any selectable
	 * time.
	 * 
	 * @return the selected time in milliseconds
	 */
	// public long getSelectedTimeInMillis() {
	// if (mViewSwitcher == null) {
	// return -1;
	// }
	// View view = (View) mViewSwitcher.getCurrentView();
	// if (view == null) {
	// return -1;
	// }
	// return view.getSelectedTimeInMillis();
	// }

	public void eventsChanged() {
		if (mViewSwitcher == null) {
			return;
		}
		GNDayView view = (GNDayView) mViewSwitcher.getCurrentView();
		view.clearCachedEvents();
		view.reloadEvents();

		view = (GNDayView) mViewSwitcher.getNextView();
		view.clearCachedEvents();
	}

	public View getNextView() {
		return (View) mViewSwitcher.getNextView();
	}

	public long getSupportedEventTypes() {
		return EventType.GO_TO | EventType.EVENTS_CHANGED;
	}

	public void handleEvent(EventInfo msg) {
		Log.v("GNDayFragment---handleEvent1---" + msg.selectedTime);
		Log.v("GNDayFragment---handleEvent2---" + msg.viewType); 
		Log.v("GNDayFragment---handleEvent---msg.eventType---" + msg.eventType);
		Log.v("GNDayFragment---handleEvent---EventType.VIEW_EVENT---" + EventType.VIEW_EVENT);
		Log.v("GNDayFragment---handleEvent---EventType.GO_TO---" + EventType.GO_TO);
		if (msg.eventType == EventType.GO_TO) {
			// TODO support a range of time
			// TODO support event_id
			// TODO support select message
			Log.v("GNDayFragment---handleEvent---" + msg.selectedTime);
			Log.v("GNDayFragment---handleEvent---" + msg.extraLong);
			goTo(msg.selectedTime,
					(msg.extraLong & CalendarController.EXTRA_GOTO_DATE) != 0,
					(msg.extraLong & CalendarController.EXTRA_GOTO_TODAY) != 0);
		} else if (msg.eventType == EventType.EVENTS_CHANGED) {
			Log.v("GNDayFragment---EventType.EVENTS_CHANGED---");
			eventsChanged();
		}
	}
}
//Gionee <pengwei><20130807> modify for CR00850530 end
// Gionee <pengwei><2013-04-12> modify for DayView end