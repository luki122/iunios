package com.gionee.calendar.day;

/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.animation.Animator;

import com.android.calendar.R;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import aurora.app.AuroraAlertDialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.text.Layout.Alignment;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.text.style.StyleSpan;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EdgeEffect;
import android.widget.ImageView;
import android.widget.LinearLayout;
import aurora.widget.AuroraListView;
import android.widget.OverScroller;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.android.calendar.AllInOneActivity;
import com.android.calendar.DayView;
import com.android.calendar.DeleteEventHelper;
import com.android.calendar.Event;
import com.android.calendar.EventGeometry;
import com.android.calendar.EventLoader;
import com.android.calendar.GeneralPreferences;
import com.android.calendar.OtherPreferences;
import com.android.calendar.Utils;
import com.gionee.almanac.GNAlmanacUtils;
import com.gionee.almanac.GNAlmanacUtils.AlmanacInfo;
import com.gionee.astro.GNAstroUtils;
import com.gionee.astro.GNAstroUtils.DayAstroInfo;
import com.gionee.calendar.GNCalendarUtils;
import com.gionee.calendar.statistics.StatisticalName;
import com.gionee.calendar.statistics.Statistics;
import com.gionee.calendar.view.Log;
import com.mediatek.calendar.LogUtil;
import com.mediatek.calendar.MTKUtils;
import com.mediatek.calendar.extension.ExtensionFactory;
import com.mediatek.calendar.extension.ICalendarThemeExt;
import com.mediatek.calendar.lunar.LunarUtil;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.util.DisplayMetrics;
import com.gionee.legalholiday.ILegalHoliday;
import com.gionee.legalholiday.LegalHolidayUtils;
import java.util.Vector;
//Gionee <pengwei><2013-04-12> modify for DayView begin
//Gionee <pengwei><2013-05-20> modify for CR00813693 begin
//Gionee <pengwei><2013-06-07> modify for CR00000000 begin
//Gionee <pengwei><2013-06-22> modify for CR00829033 begin
//Gionee <pengwei><20130807> modify for CR00850530 begin

/**
 * View for multi-day view. So far only 1 andoScrolld 7 day have been tested.
 */
public class GNDayView extends LinearLayout implements MessageDispose{
	private final GestureDetector mGestureDetector;
	private final ViewSwitcher mViewSwitcher;
	private boolean mStartingScroll = false;
	private float mInitialScrollX;
	private float mInitialScrollY;
	private int mPreviousDirection;
	private int mViewStartX;
	private int mViewStartY;
	private int scrollCount = 0;
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
	private static final int TOUCH_MODE_DVSCROLL = 2;

	private static final int TOUCH_MODE_UVSCROLL = 3;
	/**
	 * Indicates the touch gesture is a horizontal scroll
	 */
	private static final int TOUCH_MODE_HSCROLL = 4;

	private int mTouchMode = TOUCH_MODE_INITIAL_STATE;
	private Context mContext;
	private DisplayMetrics mDm;
	private float mAnimationDistance = 0;
	public int mViewWidth;
	private final CalendarController mController;
	protected int mNumDays = 1;
	private final EventLoader mEventLoader;
	private Animation mAnimationReduce;
	private Animation mAnimationEnlagement;
	private Animation mAnimationListEnlagerment;
	private int mAtroName;
	private static final int MESSAGE_ID_EVENTS_LOADED = 1;
    ArrayList<Event> events = new ArrayList<Event>();
	public GNDayView(Context context, ViewSwitcher mViewSwitcher,
			CalendarController controller, EventLoader eventLoader,
			DisplayMetrics dm,int astroName) {
		super(context);
		// TODO Auto-generated constructor stub
		mGestureDetector = new GestureDetector(context,
				new CalendarGestureListener());
		this.mViewSwitcher = mViewSwitcher;
		this.mController = controller;
		this.mContext = context;
		this.mDm = dm;
		this.mAtroName = astroName;
		mHScrollInterpolator = new ScrollInterpolator();
		mEventLoader = eventLoader;
//		setWillNotDraw(false);//With sliding
		init();
	}

	private View mDayView = null;
	private LinearLayout mDayLinear;
	private LinearLayout mDayShowLinear;
	private TextView mDayLunarTv = null;
	private TextView mDayHolidayTv = null;
	private TextView mDayDateTv = null;
	private TextView mDayWeekTv = null;
	private AuroraListView mDayPlanListView = null;
	private ImageView mDayPlanTv = null;
	private LinearLayout mDayPlanListLinear;
	private LinearLayout mDayPlanTvLinear;
	private RelativeLayout mDayPlanListRel;
	private LunarUtil mLunarUtil;
	Time mBaseDate;
	private Time mCurrentTime;
	private Vector<DayScheduleInterface> daySchedules;
	private Vector<DayScheduleInterface> daySchedulesDatas;
	private DayScheduleAdapter dayScheduleAdapter;
	private int mScrollState = 0;
	private int mDayHei = 0;
	private boolean mScrollBool = false;
	private int firstItem = -1;
	private int lastItem = -1;
	private int mDayshowHei;
	private int mFirstItem = 0;
	private static final Interpolator DEFAULT_INTERPOLATER = new AccelerateDecelerateInterpolator();
	private boolean eBool = false;
	private final static int HEIGHT = 2000;
	private RelativeLayout mDayHolidayLinear;
	private ImageView mDayHolidayLinearImg;
	private TextView mDayHolidayLinearTv;
	private Animation alphaAnimation;
//	private View mDayBottomView;
	private void init() {
		mLunarUtil = LunarUtil.getInstance(mContext);
		mDayView = LayoutInflater.from(mContext).inflate(
				R.layout.gn_day_fragment_view, null);
		mDayLinear = (LinearLayout) mDayView.findViewById(R.id.gn_day_linear);
		mDayShowLinear = (LinearLayout) mDayView
				.findViewById(R.id.date_show_linear);
		mDayLunarTv = (TextView) mDayView.findViewById(R.id.gn_day_lunar_tv);
		mDayHolidayTv = (TextView) mDayView
				.findViewById(R.id.gn_day_holiday_tv);
		mDayHolidayLinear = (RelativeLayout) mDayView
		.findViewById(R.id.gn_day_holiday_linear);
		mDayHolidayLinearImg = (ImageView) mDayView
		.findViewById(R.id.gn_day_holiday_linear_img);
		mDayHolidayLinearTv = (TextView) mDayView
		.findViewById(R.id.gn_day_holiday_linear_tv);
		mDayDateTv = (TextView) mDayView.findViewById(R.id.gn_day_date_tv);
		mDayWeekTv = (TextView) mDayView.findViewById(R.id.gn_day_week_tv);
		mDayPlanListView = (AuroraListView) mDayView
				.findViewById(R.id.gn_day_plan_lv);
		mDayPlanListView.setOverScrollMode(OVER_SCROLL_NEVER);
		mDayPlanTv = (ImageView) mDayView.findViewById(R.id.gn_day_plan_tv);
		// mDayPlanTv.setVisibility(View.GONE);
		mDayPlanListLinear = (LinearLayout) mDayView
				.findViewById(R.id.gn_day_list_linear);
		mDayPlanTvLinear = (LinearLayout) mDayView
				.findViewById(R.id.gn_day_list_tv_linear);
		mDayPlanListRel = (RelativeLayout) mDayView
				.findViewById(R.id.gn_day_plan_list_day);
//		mDayBottomView = (View) mDayView
//		.findViewById(R.id.gn_day_bottom_view);
		// Log.v("GNDayView---init---mDm.heightPixels---lp.height---" +
		// lp.height);
		Log.v("GNDayView---init---mDayLinear.getHeight()---"
				+ mDayLinear.getHeight());
		mBaseDate = new Time(Utils.getTimeZone(mContext, mTZUpdater));
		long millis = System.currentTimeMillis();
		mBaseDate.set(millis);
		mCurrentTime = new Time(Utils.getTimeZone(mContext, mTZUpdater));
		long currentTime = System.currentTimeMillis();
		mCurrentTime.set(currentTime);
		daySchedules = new Vector<DayScheduleInterface>();
		daySchedulesDatas = new Vector<DayScheduleInterface>();
		dayScheduleAdapter = new DayScheduleAdapter(mContext, daySchedulesDatas);
		alphaAnimation = AnimationUtils.loadAnimation(mContext, R.anim.gn_day_list_alpha_in); 
		mDayPlanListView.setAdapter(dayScheduleAdapter);
		mDayPlanListView.setAnimation(alphaAnimation);
		mDayPlanTv.setAnimation(alphaAnimation);
		mDayPlanListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				mScrollState = scrollState;

				Log.v("GNDayView---Changed---mScrollState---" + mScrollState);
				Log.v("GNDayView---Changed---firstItem---" + firstItem);
				Log.v("GNDayView---Changed---mScrollBool---" + mScrollBool);
				Log.v("GNDayView---Changed---mDayShowLinear.getVisibility()---"
						+ mDayShowLinear.getVisibility());
				mFirstItem = firstItem;
				Log.v("GNDayView---init---deltaYInt---" + deltaYInt);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				firstItem = firstVisibleItem;

			}
		});
		mDayPlanListView.setOnTouchListener(new OnTouchListener() {
			private int x1 = -999;
			private int y1 = -999;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int action = event.getActionMasked();
				Log.v("GNDayView---doScrolling---eBool1---" + eBool);
				if (!eBool) {
					eBool = true;
					mStartingScroll = true;
					x1 = (int) event.getX();
					y1 = (int) event.getY();
					Log.v("GNDayView---doScrolling---eBool2---" + eBool);
				}
				if (mTouchMode == TOUCH_MODE_INITIAL_STATE) {
					mTouchMode = TOUCH_MODE_DOWN;
				}
				// Log.v("onTouch---x1=" + x1);
				switch (action) {
				case MotionEvent.ACTION_CANCEL:
					Log.v("DISPATCHED ACTION_CANCEL ListView");
					if (mDayShowLinear.getVisibility() == View.VISIBLE) {
						return true;
					} else {
						return false;
					}

				case MotionEvent.ACTION_DOWN:
					Log.v("DISPATCHED ACTION_DOWN ListView");

					if (mDayShowLinear.getVisibility() == View.VISIBLE) {
						return true;
					} else {
						return false;
					}

				case MotionEvent.ACTION_MOVE:
					Log.v("DISPATCHED ACTION_MOVE ListView");
					// int x2 = (int)event.getX();
					// int y2 = (int)event.getY();
					// doscrolling(x1, y1,x2 ,y2);
					if (mDayShowLinear.getVisibility() == View.VISIBLE) {
						return true;
					} else {
						return false;
					}

				case MotionEvent.ACTION_UP:
					Log.v("DISPATCHED ACTION_UP ListView");
					int x3 = (int) event.getX();
					int y3 = (int) event.getY();
					doscrolling(x1, y1, x3, y3);
					doFilling(x1, y1, x3, y3);
					if (mStartingScroll) {
						mStartingScroll = false;
						recalc();
						// invalidate();
						mViewStartX = 0;
					}
					if (mDayShowLinear.getVisibility() == View.VISIBLE) {
						return true;
					} else {
						return false;
					}
				default:
					// Log.d(LOG_TAG, "DISPATCHED default");
					if (mDayShowLinear.getVisibility() == View.VISIBLE) {
						return true;
					} else {
						return false;
					}
				}
			}
		});
		setListViewHei(HEIGHT);
		addView(mDayView);
	}

	private void setListViewHei(int height) {
		android.view.ViewGroup.LayoutParams lp;
		lp = mDayPlanListRel.getLayoutParams();
		lp.height = height;
		mDayPlanListView.setLayoutParams(lp);
	}

	private VelocityTracker mVelocityTracker = null;

	private void doscrolling(int x1, int y1, int x2, int y2) {
		if(DayUtils.alertDialog != null && DayUtils.alertDialog.isShowing()){
			return;
		}
		int distanceX = 0;
		int distanceY = 0;
		// final float focusY = getAverageY(e2);

		// If we haven't figured out the predominant scroll direction yet,
		// then do it now.
		Log.v("GNDayView---doScrolling---mTouchMode---" + mTouchMode);
		// cancelAnimation();
		int absDistanceX = 0;
		int absDistanceY = 0;
		mPreviousDirection = 0;
		int deltaXInt = 0;
		deltaYInt = 0;
		int mathAbsX = 0;
		int mathAbsY = 0;
		int YInt = 0;
		int YIntE1 = 0;
		Log.v("GNDayView---doScrolling---deltaYInt---" + deltaYInt);
		Log.v("GNDayView---doScrolling---mDayShowLinear.getMeasuredHeight()---"
				+ mDayShowLinear.getMeasuredHeight());
		int mDayShowHei = 0;
		int viewItemCount = 0;
		if (mTouchMode == TOUCH_MODE_DOWN) {
			mDayHei = mDayLinear.getHeight();
			Log.v("doScroll");
			// final float focusY = getAverageY(e2);

			// If we haven't figured out the predominant scroll direction yet,
			// then do it now.
			Log.v("GNDayView---doScrolling---mTouchMode---" + mTouchMode);
			// cancelAnimation();
			mPreviousDirection = 0;
			deltaXInt = x2 - x1;
			deltaYInt = y2 - y1;
			Log.v("GNDayView---doScrolling---x1---" + x1);
			Log.v("GNDayView---doScrolling---y1---" + y1);
			Log.v("GNDayView---doScrolling---x2---" + x2);
			Log.v("GNDayView---doScrolling---y2---" + y2);
			mathAbsX = Math.abs(deltaXInt);
			mathAbsY = Math.abs(deltaYInt);
			YInt = y2;
			YIntE1 = y1;
			Log.v("GNDayView---doScrolling---deltaXInt---" + deltaXInt);
			Log.v("GNDayView---doScrolling---deltaYInt---" + deltaYInt);
			Log.v("GNDayView---doScrolling---mDayShowLinear.getMeasuredHeight()---"
					+ mDayShowLinear.getMeasuredHeight());
			mDayShowHei = 0;
			viewItemCount = 0;
			if (mDayShowLinear.getVisibility() == View.VISIBLE) {
				mDayShowHei = mDayShowLinear.getMeasuredHeight();
			}
		}
		Log.v("GNDayView---doScrolling---YInt > mDayShowHei==="
				+ (YInt > mDayShowHei));
		Log.v("GNDayView---doScrolling---mathAbsX < mathAbsY==="
				+ (mathAbsX < mathAbsY));
		Log.v("GNDayView---doScrolling---mathAbsX > mathAbsY==="
				+ (mathAbsX > mathAbsY));
		Log.v("GNDayView---doScrolling---mathAbsX = mathAbsY==="
				+ (mathAbsX == mathAbsY) + "===" + mathAbsX);
		Log.v("GNDayView---doScrolling---mDayShowLinear.getVisibility()==="
				+ mDayShowLinear.getVisibility());
		Log.v("GNDayView---doScrolling---daySchedules.size()==="
				+ daySchedules.size());
		int heightNoBottom = mDayHei - mDayShowHei;
		Log.v("GNDayView---doScrolling---heightNoBottom0==="
				+ heightNoBottom);
		Log.v("GNDayView---doScrolling---heightNoBottom0===YInt == "
				+ YInt);
		Log.v("GNDayView---doScrolling---heightNoBottom0===YIntE1 =="
				+ YIntE1);
		if (mathAbsX > mathAbsY && YInt < heightNoBottom && YIntE1 < heightNoBottom) {
			Log.v("doScroll---velocity > XSabsDistanceYcollerMinVelocity");
			// mScrollBool = true;
			mTouchMode = TOUCH_MODE_HSCROLL;
		} else if (mathAbsX < mathAbsY
				&& mDayShowLinear.getVisibility() != View.GONE && deltaYInt < 0
				&& daySchedules.size() > viewItemCount && YInt < heightNoBottom && YIntE1 < heightNoBottom) {
//			mScrollBool = true;
			mTouchMode = TOUCH_MODE_DVSCROLL;

		} else if (mathAbsX < mathAbsY
				&& mDayShowLinear.getVisibility() == View.GONE && deltaYInt > 0
				&& mFirstItem == 0 && YInt < heightNoBottom && YIntE1 < heightNoBottom) {
//			mScrollBool = true;
			mTouchMode = TOUCH_MODE_UVSCROLL;
		}
	}


	private void doFilling(int x1, int y1, int x2, int y2) {
		if(DayUtils.alertDialog != null && DayUtils.alertDialog.isShowing()){
			return;
		}
		int mPreviousDirection = 0;
		int deltaXInt = x2 - x1;
		int deltaYInt = y2 - y1;
		int mathAbsX = Math.abs(deltaXInt);
		int mathAbsY = Math.abs(deltaYInt);
		int YInt = y2;
		Log.v("GNDayView---doScroll---deltaYInt---" + deltaYInt);
		Log.v("GNDayView---doScroll---mDayShowLinear.getMeasuredHeight()---"
				+ mDayShowLinear.getMeasuredHeight());
		int mDayShowHei = 0;
		int viewItemCount = 0;
		if (mDayShowLinear.getVisibility() == View.VISIBLE) {
			mDayShowHei = mDayShowLinear.getMeasuredHeight();
		}
		Log.v("GNDayView---doScroll---mScrollBool---" + mScrollBool);
		if (mTouchMode == TOUCH_MODE_HSCROLL && !mScrollBool) {
			mStartingScroll = false;
//			mScrollBool = true;
			// mTouchMode = TOUCH_MODE_INITIAL_STATE;
			mViewStartX = deltaXInt;
			boolean initFlag = initNextView(mViewStartX);
			if(initFlag){
				if(deltaXInt < 0){
					Statistics.onEvent(mContext, Statistics.DAY_VIEW_SCROLL_NEXT_DAY);
				}else if(deltaXInt > 0){
					Statistics.onEvent(mContext, Statistics.DAY_VIEW_SCROLL_PRE_DAY);
				}
				switchViews(deltaXInt < 0, mViewStartX, mViewWidth, 500);
			}
			mViewStartX = 0;
		} else if (mTouchMode == TOUCH_MODE_DVSCROLL && !mScrollBool) {
			mStartingScroll = false;
			mScrollBool = true;
			// mTouchMode = TOUCH_MODE_INITIAL_STATE;

			mDayHei = mDayLinear.getHeight();
			setListViewHei(mDayHei);
			Log.v("GNDayView---init---mDayHei---" + mDayHei);
			Log.v("GNDayView---initdoFilling---mDayShowLinear.getHeight()---"
					+ mDayShowLinear.getHeight());
			// set ListView height end
			mDayshowHei = mDayShowLinear.getHeight();
			Animation dayShowOut = initDayAnimation(mContext, 0, 0, 0,
					-mDayshowHei, View.VISIBLE);
			mDayShowLinear.startAnimation(dayShowOut);
			Animation listIn = initListAnimation(mContext, 0, 0, 0,
					-mDayshowHei, View.VISIBLE);
			mDayPlanListRel.startAnimation(listIn);
		} else if (mTouchMode == TOUCH_MODE_UVSCROLL && !mScrollBool) {
			mStartingScroll = false;
			mScrollBool = true;
			// mTouchMode = TOUCH_MODE_INITIAL_STATE;
			Animation dayShowOut = initDayAnimation(mContext, 0, 0,
					-mDayshowHei, 0, View.GONE);
			mDayShowLinear.startAnimation(dayShowOut);
			Animation listIn = initListAnimation(mContext, 0, 0, 0,
					mDayshowHei, View.GONE);
			mDayPlanListRel.startAnimation(listIn);
		}
		eBool = false;
		mTouchMode = TOUCH_MODE_INITIAL_STATE;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		switch (action) {
		case MotionEvent.ACTION_CANCEL:
			Log.v("DISPATCHED ACTION_CANCEL");
			return true;

		case MotionEvent.ACTION_DOWN:
			Log.v("DISPATCHED ACTION_DOWN");
			if(mDayShowLinear.getVisibility() == View.VISIBLE){
				mStartingScroll = true;
				mGestureDetector.onTouchEvent(event);
			}
			return true;

		case MotionEvent.ACTION_MOVE:
			Log.v("DISPATCHED ACTION_MOVE");
			if(mDayShowLinear.getVisibility() == View.VISIBLE){
				mGestureDetector.onTouchEvent(event);
			}
			return true;

		case MotionEvent.ACTION_UP:
			Log.v("DISPATCHED ACTION_UP");
			if(mDayShowLinear.getVisibility() == View.VISIBLE){
				mGestureDetector.onTouchEvent(event);
				if (mStartingScroll) {
					mStartingScroll = false;
					recalc();
					// invalidate();
					mViewStartX = 0;
				}
			}
	
			return true;

		default:
			// Log.d(LOG_TAG, "DISPATCHED default");
			mGestureDetector.onTouchEvent(event);
			return true;
		}
	}

	private final float XScollerMinVelocity = 200;
	private final int XScollerMinDistance = 100;
	private int deltaYInt = 0;

	class CalendarGestureListener extends
			GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapUp(MotionEvent ev) {

			return true;
		}

		@Override
		public void onLongPress(MotionEvent ev) {

		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			Log.v("GNDayView---CalendarGestureListener---mTouchMode---"
					+ mTouchMode);
			Log.v("GNDayView---onScroll---scrollCount---" + ++scrollCount);
			GNDayView.this.doScroll(e1, e2, distanceX, distanceY);
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			doFill(e1, e2, velocityX, velocityY);
			return true;
		}

		@Override
		public boolean onDown(MotionEvent ev) {
			mTouchMode = TOUCH_MODE_DOWN;
			Log.v("onDown---TOUCH_MODE_DOWN---" + mTouchMode);
			return true;
		}
	}

	private void doFill(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if(DayUtils.alertDialog != null && DayUtils.alertDialog.isShowing()){
			return;
		}
		int mPreviousDirection = 0;
		Log.v("e1===" + e1 + ":e2===" + e2);
		Log.v("e1===" + e1 + ":e2===" + e2);
		int deltaXInt = (int) e2.getX() - (int) e1.getX();
		int deltaYInt = (int) e2.getY() - (int) e1.getY();
		int mathAbsX = Math.abs(deltaXInt);
		int mathAbsY = Math.abs(deltaYInt);
		int YInt = (int) e2.getY();
		Log.v("GNDayView---doScroll---deltaYInt---" + deltaYInt);
		Log.v("GNDayView---doScroll---mDayShowLinear.getMeasuredHeight()---"
				+ mDayShowLinear.getMeasuredHeight());
		int mDayShowHei = 0;
		int viewItemCount = 0;
		if (mDayShowLinear.getVisibility() == View.VISIBLE) {
			mDayShowHei = mDayShowLinear.getMeasuredHeight();
		}
		if (mTouchMode == TOUCH_MODE_HSCROLL) {
			mStartingScroll = false;
			// mTouchMode = TOUCH_MODE_INITIAL_STATE;
			mViewStartX = deltaXInt;
			boolean initFlag = initNextView(mViewStartX);
			if(initFlag){
				//Gionee <pengwei><2013-05-20> modify for CR00813693 begin
				if(deltaXInt < 0){
				Statistics.onEvent(mContext, Statistics.DAY_VIEW_SCROLL_NEXT_DAY);
				}else if(deltaXInt > 0){
				Statistics.onEvent(mContext, Statistics.DAY_VIEW_SCROLL_PRE_DAY);
				}
				//Gionee <pengwei><2013-05-20> modify for CR00813693 end 
				switchViews(deltaXInt < 0, mViewStartX, mViewWidth, 500);
			}
			mViewStartX = 0;
		} 
		mTouchMode = TOUCH_MODE_INITIAL_STATE;
	}

	private void doScroll(MotionEvent e1, MotionEvent e2, float deltaX,
			float deltaY) {
		if(DayUtils.alertDialog != null && DayUtils.alertDialog.isShowing()){
			return;
		}
		int distanceX = 0;
		int distanceY = 0;
		// final float focusY = getAverageY(e2);

		// If we haven't figured out the predominant scroll direction yet,
		// then do it now.
		Log.v("GNDayView---doScroll---mTouchMode---" + mTouchMode);
		// cancelAnimation();
        if (mStartingScroll) {
            mInitialScrollX = 0;
            mInitialScrollY = 0;
            mStartingScroll = false;
        }
		int absDistanceX = 0;
		int absDistanceY = 0;
        mInitialScrollX += deltaX;
        distanceX = (int) mInitialScrollX;
		mPreviousDirection = 0;
		int deltaXInt = 0;
		deltaYInt = 0;
		int mathAbsX = 0;
		int mathAbsY = 0;
		int YInt = 0;
		int YIntE1 = 0;
		Log.v("GNDayView---doScroll---deltaYInt---" + deltaYInt);
		Log.v("GNDayView---doScroll---mDayShowLinear.getMeasuredHeight()---"
				+ mDayShowLinear.getMeasuredHeight());
		int mDayShowHei = 0;
		int viewItemCount = 0;
		if (mTouchMode == TOUCH_MODE_DOWN) {
			Log.v("doScroll");
			// final float focusY = getAverageY(e2);

			// If we haven't figured out the predominant scroll direction yet,
			// then do it now.
			Log.v("GNDayView---doScroll---mTouchMode---" + mTouchMode);
			// cancelAnimation();
			mPreviousDirection = 0;
			deltaXInt = (int) e2.getX() - (int) e1.getX();
			deltaYInt = (int) e2.getY() - (int) e1.getY();
			mathAbsX = Math.abs(deltaXInt);
			mathAbsY = Math.abs(deltaYInt);
			YInt = (int) e2.getY();
			YIntE1 = (int) e1.getY();
			Log.v("GNDayView---doScroll---deltaYInt---" + deltaYInt);
			Log.v("GNDayView---doScroll---mDayShowLinear.getMeasuredHeight()---"
					+ mDayShowLinear.getMeasuredHeight());
			mDayShowHei = 0;
			viewItemCount = 0;
			if (mDayShowLinear.getVisibility() == View.VISIBLE) {
				mDayShowHei = mDayShowLinear.getMeasuredHeight();
			}
		}
		Log.v("GNDayView---doScroll---YInt > mDayShowHei==="
				+ (YInt > mDayShowHei));
		Log.v("GNDayView---doScroll---mathAbsX < mathAbsY==="
				+ (mathAbsX < mathAbsY));
		Log.v("GNDayView---doScroll---mathAbsX > mathAbsY==="
				+ (mathAbsX > mathAbsY));
		Log.v("GNDayView---doScroll---mathAbsX = mathAbsY==="
				+ (mathAbsX == mathAbsY) + "===" + mathAbsX);
		Log.v("GNDayView---doScroll---mDayShowLinear.getVisibility()==="
				+ mDayShowLinear.getVisibility());
		Log.v("GNDayView---doScroll---deltaY===" + deltaY);
		Log.v("GNDayView---doScroll---daySchedules.size()==="
				+ daySchedules.size());
		Log.v("GNDayView---doScrolling---heightNoBottom01===YInt == "
				+ YInt);
		Log.v("GNDayView---doScrolling---heightNoBottom01===YIntE1 =="
				+ YIntE1);
		mViewStartX = distanceX;
		Log.v("GNDayView---doScroll---mViewStartX == " + mViewStartX);
		if (mathAbsX > mathAbsY) {
			Log.v("doScroll---velocity > XSabsDistanceYcollerMinVelocity");
			// mScrollBool = true;
			mTouchMode = TOUCH_MODE_HSCROLL;
		} /*else if (YInt > mDayShowHei && mathAbsX < mathAbsY
				&& mDayShowLinear.getVisibility() != View.GONE && deltaY > 0
				&& daySchedules.size() > viewItemCount) {
			mScrollBool = true;
			mTouchMode = TOUCH_MODE_DVSCROLL;

		} else if (mathAbsX < mathAbsY
				&& mDayShowLinear.getVisibility() == View.GONE && deltaY < 0
				&& mFirstItem == 0) {
			mScrollBool = true;
			mTouchMode = TOUCH_MODE_UVSCROLL;
		}*/
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

	private float getAverageY(MotionEvent me) {
		int count = me.getPointerCount();
		float focusY = 0;
		for (int i = 0; i < count; i++) {
			focusY += me.getY(i);
		}
		focusY /= count;
		return focusY;
	}
	private String mToastTimeOutOfRange = "";
	private boolean initNextView(int deltaX) {
		// Change the view to the previous day or week
		GNDayView nextView = (GNDayView) mViewSwitcher.getNextView();
		Time date = getTimeOfController();
		Log.v("GNDayView---initNextView---deltaX ===" + deltaX);
		if (deltaX > 0) {
			date.monthDay = date.monthDay - 1;
		} else {
			date.monthDay = date.monthDay + 1;
		}
		Log.v("GNDayView---initNextView---date.monthDay1---" + date.monthDay);
		date.normalize(true);
		Log.v("GNDayView---initNextView---date---" + date);
		
		// Gionee <jiangxiao> <2013-06-19> add for CRxxxxxxxx begin
	  	// use a unique Toast object to avoid multiple Toast popup
//		mToastTimeOutOfRange = mContext.getResources().getString(R.string.gn_day_time_out_of_range);
//		if(date.year < 1970 || date.year > 2036) {
//			Toast.makeText(mContext, mToastTimeOutOfRange, Toast.LENGTH_SHORT).show();
//			return false;
//		}

		if(date.year < GNCalendarUtils.MIN_YEAR_NUM || date.year > GNCalendarUtils.MAX_YEAR_NUM) {
			AllInOneActivity.showOutOfRangeToast(R.string.time_out_of_range);
			
			return false;
		}
		// Gionee <jiangxiao> <2013-06-19> add for CRxxxxxxxx end
		nextView.initView(date);
//		nextView.reloadEvents();
		return true;
	}

	/**
	 * Initialize the state for another view. The given view is one that has its
	 * own bitmap and will use an animation to replace the current view. The
	 * current view and new view are either both Week views or both Day views.
	 * They differ in their base date.
	 * 
	 * @param view
	 *            the view to initialize.
	 */
	private void initView(Time time) {
		// /M:if mFirstHour is letter than 0,set it to 0.So when the next view
		// /showing will not overScroll@{
		// /@}
		loadView(time);
	}

	private final ScrollInterpolator mHScrollInterpolator;

	private class ScrollInterpolator implements Interpolator {
		public ScrollInterpolator() {
		}

		public float getInterpolation(float t) {
			t -= 1.0f;
			t = t * t * t * t * t + 1;

			if ((1 - t) * mAnimationDistance < 1) {
				cancelAnimation();
			}
			// Log.v("GNDayView---t---" + t);
			return t;
		}
	}

	public void switchViews(boolean forward, float xOffSet, float width,
			float velocity) {
		Log.v("GNDayView---switch---1");
		Log.v("GNDayView---onDraw---switchViews == " + (-mViewStartX));
		Log.v("GNDayView---EventTime-1---" + System.currentTimeMillis());
		GNDayView view = (GNDayView) mViewSwitcher.getCurrentView();
		view.cleanup();
		mAnimationDistance = width - xOffSet;
		float progress = 0.0f;
		progress = 0.0f;
		float inFromXValue, inToXValue;
		float outFromXValue, outToXValue;
		if (forward) {
			inFromXValue = 1.0f - progress;
			inToXValue = 0.0f;
			outFromXValue = -progress;
			outToXValue = -1.0f;
		} else {
			inFromXValue = progress - 1.0f;
			inToXValue = 0.0f;
			outFromXValue = progress;
			outToXValue = 1.0f;
		}

		// We have to allocate these animation objects each time we switch views
		// because that is the only way to set the animation parameters.
		TranslateAnimation inAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, inFromXValue,
				Animation.RELATIVE_TO_SELF, inToXValue, Animation.ABSOLUTE,
				0.0f, Animation.ABSOLUTE, 0.0f);

		TranslateAnimation outAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, outFromXValue,
				Animation.RELATIVE_TO_SELF, outToXValue, Animation.ABSOLUTE,
				0.0f, Animation.ABSOLUTE, 0.0f);
		long duration = calculateDuration(width - Math.abs(xOffSet), width,
				velocity);
		Log.v("GNDayView---duration---" + duration);
		inAnimation.setDuration(duration);
		inAnimation.setInterpolator(mHScrollInterpolator);
		inAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				GNDayView view = (GNDayView) mViewSwitcher.getCurrentView();
				Log.v("GNDayView---switchViews---showNext---"
						+ view.mDayShowLinear.getVisibility());
				view.requestFocus();
//				view.reloadEvents();
//				view.getAstroInfo(GNAstroUtils.ASTRO_INFO_TYPE_DAY,GNAstroUtils.ASTRO_NAME_ARIES);
//				mTouchMode = TOUCH_MODE_INITIAL_STATE;
				mScrollBool = false;

			}
		});
		outAnimation.setInterpolator(mHScrollInterpolator);
		outAnimation.setDuration(duration);
		// outAnimation.setAnimationListener(new GotoBroadcaster());
		mViewSwitcher.setInAnimation(inAnimation);
		mViewSwitcher.setOutAnimation(outAnimation);
		GNDayView viewNext = (GNDayView) mViewSwitcher.getNextView();
		viewNext.updateTitle();
		viewNext.reloadEvents();
		mViewSwitcher.showNext();
		Log.v("GNDayView---switch---2");
	}

	public void cleanup() {
		setListViewHei(HEIGHT);
		Log.v("GNDayView---cleanup1---mDayshowHei---" + mDayshowHei);

		mDayShowLinear.setVisibility(View.VISIBLE);
		daySchedules.clear();
		daySchedulesDatas.clear();
		dayScheduleAdapter.notifyDataSetChanged();
		Log.v("GNDayView---cleanup1---" + mDayShowLinear.getVisibility());
		mDayPlanListLinear.setVisibility(View.INVISIBLE);
		mDayPlanTvLinear.setVisibility(View.GONE);
		astroView = null;
		mReloadBool = false;
		almanacBool = false;
//		mDayHolidayLinear.setVisibility(View.INVISIBLE);
		// mStartingScroll = false;
	}

	public void updateTitle() {
		Time start = new Time(mBaseDate);
		start.normalize(true);
		Log.v("GNDayView---updateTitle---" + start);
		Time end = new Time(start);
		// Move it forward one minute so the formatter doesn't lose a day
		end.minute += 1;
		end.normalize(true);
		long formatFlags = DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_SHOW_YEAR;
		mController.sendEvent(this, EventType.UPDATE_TITLE, start, end, null,
				-1, ViewType.CURRENT, formatFlags, null, null);
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldw, int oldh) {
		// TODO Auto-generated method stub
		mViewWidth = width;
		Log.v("GNDayView---onSizeChanged---mViewWidth---" + mViewWidth);
		Log.v("GNDayView---onSizeChanged---mDayPlanListView.mDayPlanTv.getWidth()---"
				+ mDayPlanListView.getWidth());
		Log.v("GNDayView---onSizeChanged---mDayPlanListView.getMeasuredWidth()---"
				+ mDayPlanListView.getMeasuredWidth());
//		android.view.ViewGroup.LayoutParams lp;
//		lp = mDayPlanListRel.getLayoutParams();
//		lp.height = AllInOneActivity.getBottomBar();
//		mDayBottomView.setLayoutParams(lp);
//		Log.v("GNDayView---onSizeChanged---lp.height---" + lp.height);
	}

	// The rest of this file was borrowed from Launcher2 - PagedView.java
	private static final int MINIMUM_SNAP_VELOCITY = 2200;

	private long calculateDuration(float delta, float width, float velocity) {
		/*
		 * Here we compute a "distance" that will be used in the computation of
		 * the overall snap duration. This is a function of the actual distance
		 * that needs to be traveled; we keep this value close to half screen
		 * size in order to reduce the variance in snap duration as a function
		 * of the distance the page needs to travel.
		 */
		final float halfScreenSize = width / 2;
		float distanceRatio = delta / width;
		float distanceInfluenceForSnapDuration = distanceInfluenceForSnapDuration(distanceRatio);
		float distance = halfScreenSize + halfScreenSize
				* distanceInfluenceForSnapDuration;

		velocity = Math.abs(velocity);
		velocity = Math.max(MINIMUM_SNAP_VELOCITY, velocity);

		/*
		 * we want the page's snap velocity to approximately match the velocity
		 * at which the user flings, so we scale the duration by a value near to
		 * the derivative of the scroll interpolator at zero, ie. 5. We use 6 to
		 * make it a little slower.
		 */
		long duration = 6 * Math.round(1000 * Math.abs(distance / velocity));
		Log.e("halfScreenSize:" + halfScreenSize + " delta:" + delta
				+ " distanceRatio:" + distanceRatio + " distance:" + distance
				+ " velocity:" + velocity + " duration:" + duration
				+ " distanceInfluenceForSnapDuration:"
				+ distanceInfluenceForSnapDuration);
		return duration;
	}

	/*
	 * We want the duration of the page snap animation to be influenced by the
	 * distance that the screen has to travel, however, we don't want this
	 * duration to be effected in a purely linear fashion. Instead, we use this
	 * method to moderate the effect that the distance of travel has on the
	 * overall snap duration.
	 */
	private float distanceInfluenceForSnapDuration(float f) {
		f -= 0.5f; // center the values about 0.
		f *= 0.3f * Math.PI / 2.0f;
		return (float) Math.sin(f);
	}

	private static int sCounter = 0;

	private class GotoBroadcaster implements Animation.AnimationListener {
		private final int mCounter;

		// private final Time mStart;
		// private final Time mEnd;

		public GotoBroadcaster(Time start, Time end) {
			mCounter = ++sCounter;
			// mStart = start;
			// mEnd = end;
		}

		public GotoBroadcaster() {
			mCounter = ++sCounter;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			GNDayView view = (GNDayView) mViewSwitcher.getCurrentView();
			view.mViewStartX = 0;
			view = (GNDayView) mViewSwitcher.getNextView();
			view.mViewStartX = 0;

			if (mCounter == sCounter) {
				// mController.sendEvent(this, EventType.GO_TO, mStart, mEnd,
				// null, -1,
				// ViewType.CURRENT, CalendarController.EXTRA_GOTO_DATE, null,
				// null);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}
	}

	public void loadView(Time time) {
		Log.v("GNDayView---switch---3");
		mBaseDate.set(time);
		recalc();
		Log.v("GNDayView---loadView---time.year---" + time.year);
		Log.v("GNDayView---loadView---time.month---" + time.month);
		Log.v("GNDayView---loadView---time.monthDay---" + time.monthDay);
		String mLunarDayStr = mLunarUtil.getLunarDayString(time.monthDay);
		if(DayUtils.isChinsesLunarSetting()){
			String mLunarDate = mLunarUtil.getLunarDateString(time.year,
				time.month + 1, time.monthDay);
			if(mLunarDate != null){
			String DayLunarStr = DayUtils.getLunarDate(mContext, mLunarDate);
			mDayLunarTv.setText(DayLunarStr);
//			String mHolidayStr = mLunarUtil.getFestivalChineseString(time.year,
//					time.month + 1, time.monthDay);
			String mHolidayStr = mLunarUtil.getMixedFestivalChineseString(
					time.year, time.month + 1, time.monthDay);
			mHolidayStr = mHolidayStr.replace(LunarUtil.DELIM, " ");
			mDayHolidayTv.setText(mHolidayStr);
			//Gionee <pengwei><2013-05-21> modify for CR00813681 begin
			LegalHolidayUtils legalHolidayUtils = LegalHolidayUtils.getInstance();
			if(legalHolidayUtils != null){
			int dayInt = legalHolidayUtils.getDayType(mFirstJulianDay);
			Log.v("GNDayView---loadView---mFirstJulianDay == " + mFirstJulianDay);
			Log.v("GNDayView---loadView---dayInt == " + dayInt);
			Bitmap bitmap = null;
			String holidayAndWorkString = "";
			if(dayInt == ILegalHoliday.DAY_TYPE_HOLIDAY){
				bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.gn_calendar_holiday);
				holidayAndWorkString = mContext.getResources().getString(R.string.gn_week_text_holiday);
			}else if(dayInt == ILegalHoliday.DAY_TYPE_WORK_SHIFT){
				bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.gn_calendar_work);
				holidayAndWorkString = mContext.getResources().getString(R.string.gn_week_text_work);
			}
			Log.v("GNDayView---loadView---bitmap == " + bitmap);
			if (bitmap != null) {
				Log.v("GNDayView---loadView---bitmap1 == " + bitmap);
				mDayHolidayLinear.setVisibility(View.VISIBLE);
				mDayHolidayLinearImg.setImageBitmap(bitmap);
				mDayHolidayLinearTv.setText(holidayAndWorkString);
			}else{
				mDayHolidayLinear.setVisibility(View.INVISIBLE);
			}
		  }
		}
		}
		//Gionee <pengwei><2013-05-21> modify for CR00813681 end
		Typeface face = Typeface.createFromAsset(mContext.getAssets(),"fonts/Roboto-Bold.ttf");
		mDayDateTv.setTypeface(face);
		mDayDateTv.setText(time.monthDay + "");
		mDayWeekTv.setText(DayUtils.getLunarWeek(mContext,time.weekDay));
		// mDayShowLinear.setVisibility(View.VISIBLE);
		
		// invalidate();
		Log.v("GNDayView---loadView---mAllDayEvents---" + mAllDayEvents);
		Log.v("GNDayView---switch---4");
	}

	private final Runnable mTZUpdater = new Runnable() {
		@Override
		public void run() {
			String tz = Utils.getTimeZone(mContext, this);
			mBaseDate.timezone = tz;
			mBaseDate.normalize(true);
			mCurrentTime.switchTimezone(tz);
			// invalidate();
		}
	};

	/**
	 * return a negative number if "time" is comes before the visible time
	 * range, a positive number if "time" is after the visible time range, and 0
	 * if it is in the visible time range.
	 */
	public int compareToVisibleTimeRange(Time time) {
		try {
			// Compare beginning of range
			Log.v("GNDayView---compareToVisibleTimeRange---" + "time=" + time
					+ "---mBaseDate=" + mBaseDate);
			String time1 = time.toString().substring(0,8);
			String time2 = mBaseDate.toString().substring(0,8);
//			int diff = Time.compare(time, mBaseDate);
			int diff = time1.compareTo(time2);
			Log.v("GNDayView---compareToVisibleTimeRange---" +
					"time1---" + time1);
			Log.v("GNDayView---compareToVisibleTimeRange---" +
					"time2---" + time2);
			return diff;
		} catch (Exception e) {
			// TODO: handle exception
			Log.v("GNDayView---compareToVisibleTimeRange---" +
					"Exception  " + e);
			return 0;
		}
	}
	
	private boolean mReloadBool = false;
	private Handler mEventLoaderHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case MESSAGE_ID_EVENTS_LOADED:
					// update events ListView
					Log.v("DayView---initList---daySchedules.contains(astroView) == MESSAGE_ID_EVENTS_LOADED");
					Log.v("GNDayView---EventTime2---" + System.currentTimeMillis());
					daySchedules.clear();
					Log.v("GNDayView---handleMessage---daySchedules.size() == " + daySchedules.size());
					mReloadBool = true;
					almanacBool = true;
					updateEventsListView();
					break;
				default:
					break;
			}
            super.handleMessage(msg);
		}
	};
	
	private int mLoadedFirstJulianDay = -1;
	private ArrayList<Event> mEvents = new ArrayList<Event>();
	private ArrayList<Event> mAllDayEvents = new ArrayList<Event>();

	/* package */void reloadEvents() {
		Log.v("GNDayView---EventTime0---" + System.currentTimeMillis());
		// Protect against this being called before this view has been
		// initialized.
		Log.v("GNDayView---reloadEvents---start---");
		if (mContext == null) {
			Log.v("GNDayView---loadEventsInBackground---mContext---" + mContext);
			return;
		}

		// Make sure our time zones are up to date
		mTZUpdater.run();
		// The start date is the beginning of the week at 12am
		Time weekStart = new Time(Utils.getTimeZone(mContext, mTZUpdater));
		weekStart.set(mBaseDate);
		weekStart.hour = 0;
		weekStart.minute = 0;
		weekStart.second = 0;
		long millis = weekStart.normalize(true /* ignore isDst */);
		// Avoid reloading events unnecessarily.
		Log.v("GNDayView---loadEventsInBackground---1---millis---" + millis);
		Log.v("GNDayView---loadEventsInBackground---1---mLastReloadMillis---"
				+ mLastReloadMillis);
		// if (millis == mLastReloadMillis) {
		// Log.v("GNDayView---loadEventsInBackground---millis == mLastReloadMillis");
		// return;
		// }
		mLastReloadMillis = millis;
		Log.v("GNDayView---loadEventsInBackground---2---millis---"
				+ mLastReloadMillis);
		// load events in the background
		// mContext.startProgressSpinner();
		events.clear();
		Log.v("GNDayView---EventTime0.1---" + System.currentTimeMillis());
		mEventLoader.loadEventsInBackground(mNumDays, events, mFirstJulianDay,
				new Runnable() {
					public void run() {
						mEventLoaderHandler.removeMessages(MESSAGE_ID_EVENTS_LOADED);
						Log.v("GNDayView---loadEventsInBackground---run");
						Log.v("GNDayView---EventTime1---" + System.currentTimeMillis());
//						mEventLoaderHandler.obtainMessage(MESSAGE_ID_EVENTS_LOADED).sendToTarget();
						mEventLoaderHandler.dispatchMessage(mEventLoaderHandler.obtainMessage(MESSAGE_ID_EVENTS_LOADED));
//						mEventLoaderHandler.sendEmptyMessage(MESSAGE_ID_EVENTS_LOADED);
					}
				}, mCancelCallback);
	}
	//Gionee <pengwei><20130618> modify for CR00826864 begin
	private void updateEventsListView(){
		try {
		Log.v("GNDayView---EventTime3---" + System.currentTimeMillis());

		Log.v("GNDayView---loadEventsInBackground---mNumDays---"
				+ mNumDays);
		Log.v("GNDayView---loadEventsInBackground---mFirstJulianDay---"
				+ mFirstJulianDay);
		Log.v("GNDayView---loadEventsInBackground1---"
				+ System.currentTimeMillis());
		boolean fadeinEvents = mFirstJulianDay != mLoadedFirstJulianDay;
		mEvents.clear();
		mEvents.addAll(events);
		mLoadedFirstJulianDay = mFirstJulianDay;
		// Create a shorter array for all day events
		for(int i = mEvents.size() - 1;i >= 0;i--){
			Log.v("GNDayView---loadEventsInBackground---e---"
					+ mEvents.get(i));
			DayScheduleInterface scheduleView = new ScheduleView(
					mContext, mEvents.get(i),mBaseDate);
			daySchedules.add(0,scheduleView);
		}
		mDayPlanListLinear.setVisibility(View.VISIBLE);
		mDayPlanTvLinear.setVisibility(View.GONE);
		mDayPlanListView.startAnimation(alphaAnimation);
		daySchedulesDatas.clear();
		daySchedulesDatas.addAll(daySchedules);
		dayScheduleAdapter.notifyDataSetChanged();
		recalc();
		getAlmanacInfo(ALMANAC_INFO);
		Log.v("GNDayView---EventTime4---" + System.currentTimeMillis());
		} catch (Exception e) {
			// TODO: handle exception
			Log.v("GNDayView---reloadEvents---e---" + e);
		}
	}
	//Gionee <pengwei><20130618> modify for CR00826864 end
	private final Runnable mCancelCallback = new Runnable() {
		public void run() {
			clearCachedEvents();
		}
	};

	private long mLastReloadMillis;

	void clearCachedEvents() {
		mLastReloadMillis = 0;
	}

	private int mFirstJulianDay;
	private int mLastJulianDay;

	private void recalc() {
		// / M: normalize time, or the weekday/monthday part maybe incorrect @{
		mBaseDate.normalize(true);
		// / @}
		// Set the base date to the beginning of the week if we are displaying
		// 7 days at a time.
		// / M: use getJulianDayInGeneral to solve the problems which happens
		// before 1970-1-1 @{
		mFirstJulianDay = Utils.getJulianDayInGeneral(mBaseDate, false);
		// / @}
		mLastJulianDay = mFirstJulianDay + mNumDays - 1;
	}

	public Animation initListAnimation(Context context, int startX, int toX,
			int startY, int toY, final int viewIsShow) {
		Log.i("GNDayView---initListAnimation---" + "startX=" + startX + "toX="
				+ toX + "startY=" + startY + "toY=" + toY);
		Animation mAnimation = new TranslateAnimation(startX, toX, startY, toY);
		mAnimation.setInterpolator(AnimationUtils.loadInterpolator(context,
				android.R.anim.accelerate_decelerate_interpolator));
		mAnimation.setDuration(300);
		mAnimation.setFillEnabled(true);
		// mAnimation.setFillBefore(true);
		mAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				Log.i("onAnimationStart");

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				Log.i("onAnimationRepeat");
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				Log.i("onAnimationEnd");
				mScrollBool = false;
				if (viewIsShow == View.VISIBLE) {
					mDayShowLinear.setVisibility(View.GONE);
					Log.v("GNDayView---cleanup4---"
							+ mDayShowLinear.getVisibility());
				} else if (viewIsShow == View.GONE) {
					mDayShowLinear.setVisibility(View.VISIBLE);
					mDayPlanListView.setSelection(0);
					setListViewHei(HEIGHT);
					Log.v("GNDayView---cleanup3---"
							+ mDayShowLinear.getVisibility());
				}
				Log.v("GNDayView---cleanup31---"
						+ mDayShowLinear.getVisibility());
				Log.v("GNDayView---cleanup42---"
						+ mDayShowLinear.getVisibility());
//				mTouchMode = TOUCH_MODE_INITIAL_STATE;
				mScrollBool = false;
				// mDayPlanListRel.layout(0, 0,
				// mDayLinear.getWidth(),mDayLinear.getHeight());
			}
		});
		return mAnimation;
	}

	public Animation initDayAnimation(Context context, int startX, int toX,
			int startY, int toY, final int viewIsShow) {
		Log.i("GNDayView---initListAnimation---" + "startX=" + startX + "toX="
				+ toX + "startY=" + startY + "toY=" + toY);
		Animation mAnimation = new TranslateAnimation(startX, toX, startY, toY);
		mAnimation.setInterpolator(AnimationUtils.loadInterpolator(context,
				android.R.anim.accelerate_decelerate_interpolator));
		mAnimation.setDuration(300);
		// mAnimation.setFillAfter(true);
		mDayPlanListView.setSelection(0);
		mAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				Log.i("onAnimationStart");

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				Log.i("onAnimationRepeat");
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				Log.i("onAnimationEnd");
				Log.v("GnDayView---onAnimationEnd---" + mDayHei);
				// mDayShowLinear.layout(0,-mDayHei,mDayLinear.getWidth(),0);
				Log.v("GNDayView---onAnimationEnd---"
						+ mDayShowLinear.getVisibility());

				// if (viewIsShow == View.VISIBLE) {
				// mDayShowLinear.setVisibility(View.GONE);
				// mDayPlanListView.setEnabled(true);
				// Log.v("GNDayView---cleanup4---"
				// + mDayShowLinear.getVisibility());
				// }
				// Log.v("GNDayView---cleanup42---"
				// + mDayShowLinear.getVisibility());

			}
		});
		return mAnimation;
	}

	// @Override
	// public boolean dispatchTouchEvent(MotionEvent event) {
	// mGestureDetector.onTouchEvent(event);
	// if(mDayShowLinear.getVisibility() == View.GONE){
	// return super.dispatchTouchEvent(event);
	// }else{
	// return true;
	// }
	// }
	private Time getTimeOfController() {
		CalendarController controller = CalendarController
				.getInstance(mContext);
		Time time = new Time();
		time.set(controller.getTime());
		time.normalize(true);

		return time;
	}

	public static final int ALMANAC_INFO = 99;
	private boolean almanacBool = false;
	
	
	
	private Handler mDayAstroHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
        	this.obtainMessage();
			switch(msg.what) {
				case GNAstroUtils.ASTRO_INFO_TYPE_DAY:
					// update UI
					Log.v("GNDayView---EventTime5---" + System.currentTimeMillis());
					Log.v("GNDayView---mReloadBool---" + mReloadBool);
					if(mReloadBool){
						initAstroList(msg);
					}
					break;
				default:
					break;
			}
			super.handleMessage(msg);
		}
	};
	
	private Handler mDayAlmanacHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
        	this.obtainMessage();
			switch(msg.what) {
				case ALMANAC_INFO:
					Log.v("GNDayView---ALMANAC_INFO---");
					Log.v("GNDayView---ALMANAC_INFO---" + mReloadBool);
					if(almanacBool){
						initAlmanacList(msg);
					}
					break;
				default:
					break;
			}
			super.handleMessage(msg);
		}
	};
	
	private DayScheduleInterface astroView = null;
	private void initAstroList(Message msg){
		Object object = msg.obj;
		DayAstroInfo dayAstroInfo = null;
		String astroName = "";
		Log.v("DayView---initList---object == " + object);
		try {
			if (object != null) {
				dayAstroInfo = (DayAstroInfo) msg.obj;
				if(astroView != null && daySchedules.contains(astroView)){
					daySchedules.remove(astroView);
				}
				astroView = new AstroView(mContext,
						dayAstroInfo,astroName);
				Log.v("DayView---initList---astroView == " + astroView);
				Log.v("DayView---initList---daySchedules.contains(astroView) == " + daySchedules.contains(astroView));
				daySchedules.add(astroView);
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.v("DayView---initList---e == " + e);
		}
		Log.v("DayView---initList---daySchedules.size() == " + daySchedules.size());
		if (daySchedules.size() == 0) {
			mDayPlanTv.startAnimation(alphaAnimation);
			mDayPlanListLinear.setVisibility(View.GONE);
			mDayPlanTvLinear.setVisibility(View.VISIBLE);
			if(DayUtils.isChinsesLunarSetting()){
				mDayPlanTv.setImageResource(R.drawable.gn_day_view_no_content);
			}else{
				mDayPlanTv.setImageResource(R.drawable.gn_day_view_no_content_eg);
			}
		} else {
			daySchedulesDatas.clear();
			daySchedulesDatas.addAll(daySchedules);
			dayScheduleAdapter.notifyDataSetChanged();
			mDayPlanListView.startAnimation(alphaAnimation);
			mDayPlanListLinear.setVisibility(View.VISIBLE);
			mDayPlanTvLinear.setVisibility(View.GONE);
		}
		mReloadBool = false;
		Log.v("GNDayView---EventTime6---" + System.currentTimeMillis());
	}
	
	private DayScheduleInterface almancView = null;
	private void initAlmanacList(Message msg){
		Object object = msg.obj;
		GNAlmanacUtils.AlmanacInfo almanacInfo = null;
		String astroName = "";
		Log.v("DayView---initAlmanacList---object == " + object);
		try {
			if (object != null) {
				almanacInfo = (GNAlmanacUtils.AlmanacInfo) msg.obj;
				if(almancView != null && daySchedules.contains(almancView)){
					daySchedules.remove(almancView);
				}
				AlmanacView almanacView = new AlmanacView(mContext,
						almanacInfo.todayFitted, almanacInfo.todayUnfitted,
						GNAlmanacUtils.getInstance()
								.getFiveElementFromAlmanacInfo(almanacInfo));
				almanacView.setQueryDate(mBaseDate);
				Log.v("DayView---initList---astroView == " + astroView);
				Log.v("DayView---initList---daySchedules.contains(astroView) == " + daySchedules.contains(astroView));
				daySchedules.add(almanacView);
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.v("DayView---initAlmanacList---e == " + e);
		}
		Log.v("DayView---initAlmanacList---daySchedules.size() == " + daySchedules.size());
//		if (daySchedules.size() == 0) {
//			mDayPlanTv.startAnimation(alphaAnimation);
//			mDayPlanListLinear.setVisibility(View.GONE);
//			mDayPlanTvLinear.setVisibility(View.VISIBLE);
//			if(DayUtils.isChinsesLunarSetting()){
//				mDayPlanTv.setImageResource(R.drawable.gn_day_view_no_content);
//			}else{
//				mDayPlanTv.setImageResource(R.drawable.gn_day_view_no_content_eg);
//			}
//		} else {
//			mDayPlanListView.startAnimation(alphaAnimation);
//			mDayPlanListLinear.setVisibility(View.VISIBLE);
//			mDayPlanTvLinear.setVisibility(View.GONE);
//			dayScheduleAdapter.notifyDataSetChanged();
//		}
		almanacBool = false;
		String astroStr = GNAstroUtils.ASTRO_NAME_ARIES;
		int astroNameInt = GNAstroUtils.getAstroIndexFromPref(mContext);
		try {
			Log.v("GNDayView---reloadEvents---mAtroName---" + astroNameInt);
			astroStr = GNAstroUtils.getAstroNameById(astroNameInt);
			Log.v("GNDayView---reloadEvents---astroStr---" + astroStr);
		} catch (Exception e) {
			// TODO: handle exception
			Log.v("GNDayView---reloadEvents---e---" + e);
		}
		getAstroInfo(GNAstroUtils.ASTRO_INFO_TYPE_DAY,astroStr);
		Log.v("GNDayView---EventTime6---" + System.currentTimeMillis());
	}
	
	private void initAlmanacList(GNAlmanacUtils.AlmanacInfo almanacInfo){
		String astroName = "";
		try {
			if (almanacInfo != null) {
				if(almancView != null && daySchedules.contains(almancView)){
					daySchedules.remove(almancView);
				}
				AlmanacView almanacView = new AlmanacView(mContext,
						almanacInfo.todayFitted, almanacInfo.todayUnfitted,
						GNAlmanacUtils.getInstance()
								.getFiveElementFromAlmanacInfo(almanacInfo));
				almanacView.setQueryDate(mBaseDate);
				Log.v("DayView---initList---astroView == " + astroView);
				Log.v("DayView---initList---daySchedules.contains(astroView) == " + daySchedules.contains(astroView));
				daySchedules.add(almanacView);
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.v("DayView---initAlmanacList---e == " + e);
		}
		Log.v("DayView---initAlmanacList---daySchedules.size() == " + daySchedules.size());
		almanacBool = false;
		String astroStr = GNAstroUtils.ASTRO_NAME_ARIES;
		int astroNameInt = GNAstroUtils.getAstroIndexFromPref(mContext);
		try {
			Log.v("GNDayView---reloadEvents---mAtroName---" + astroNameInt);
			astroStr = GNAstroUtils.getAstroNameById(astroNameInt);
			Log.v("GNDayView---reloadEvents---astroStr---" + astroStr);
		} catch (Exception e) {
			// TODO: handle exception
			Log.v("GNDayView---reloadEvents---e---" + e);
		}
		getAstroInfo(GNAstroUtils.ASTRO_INFO_TYPE_DAY,astroStr);
		Log.v("GNDayView---EventTime6---" + System.currentTimeMillis());
	}
	
	private void getAstroInfo(int infoType, String astroName) {
//		mDayAstroHandler.removeMessages(infoType);
//		mDayAstroHandler.removeMessages(ALMANAC_INFO);
		if (mDayAstroHandler.obtainMessage(infoType) != null) {
			Time date = getTimeOfController();
			Log.v("GNDayView---getAstroInfo---date---" + date);
			Log.v("GNDayView---getAstroInfo---infoType---" + infoType);
			GNAstroUtils.getAstroInfo(astroName, date.year,(date.month + 1),date.monthDay,
					infoType,mContext,mDayAstroHandler.obtainMessage(infoType));
		}else{
			mReloadBool = false;
		}
	}

	private void getAlmanacInfo(int infoType) {
//		mDayAstroHandler.removeMessages(infoType);
//		mDayAstroHandler.removeMessages(GNAstroUtils.ASTRO_INFO_TYPE_DAY);
//		if (mDayAlmanacHandler.obtainMessage(infoType) != null) {
			Time date = getTimeOfController();
			// Gionee <jiangxiao> <2013-07-22> modify for CR00837096 begin
			// use another method to query almanac info
			
			// Log.v("GNDayView---getAlmanacInfo---date---" + date);
			// String queryDate = date.year + "/" + date.month + "/" + date.monthDay;
			// Log.v("GNDayView---getAlmanacInfo---infoType---" + infoType);
			// Log.v("GNDayView---getAlmanacInfo---queryDate---" + queryDate);
			
			// GNAlmanacUtils.getAlmanacInfo(queryDate, response);
			GNAlmanacUtils.getInstance().getAlmanacInfo(date.year, date.month + 1, date.monthDay,this,-1,-1);
			// Gionee <jiangxiao> <2013-07-22> modify for CR00837096 end
//		}else{
//			almanacBool = false;
//		}
	}

	@Override
	public void sendMessage(int msg, AlmanacInfo almanacInfo, Time time,int action,int type) {
		// TODO Auto-generated method stub
		Log.v("GNDayView---ALMANAC_INFO---GNAlmanacUtils.ERROR_CODE_ALMANAC_QUERY_OK == " + GNAlmanacUtils.ERROR_CODE_ALMANAC_QUERY_OK);
		Log.v("GNDayView---ALMANAC_INFO---" + mReloadBool);
		Log.v("GNDayView---ALMANAC_INFO---almanacInfo == " + almanacInfo);
		initAlmanacList(almanacInfo);
	}

	
	
//	@Override
//	protected void onDraw(Canvas canvas) {
//		// TODO With sliding
//		super.onDraw(canvas);
//		Log.v("GNDayView---onDraw---mViewStartX == " + (-mViewStartX));
//        canvas.translate(-mViewStartX,0);
//	}
	
}

//Gionee <pengwei><2013-06-22> modify for CR00829033 end
// Gionee <pengwei><2013-04-12> modify for DayView end
//Gionee <pengwei><2013-05-20> modify for CR00813693 end
//Gionee <pengwei><2013-06-07> modify for CR00000000 end
//Gionee <pengwei><20130807> modify for CR00850530 end