// Gionee <jiangxiao> <2013-04-11> add for CR000000 begin
package com.gionee.calendar.month;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract.Attendees;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.android.calendar.AllInOneActivity;
import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.android.calendar.Event;
import com.android.calendar.EventLoader;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.android.calendar.month.MonthByWeekAdapter;
import com.android.calendar.month.MonthByWeekFragment;
import com.android.calendar.month.MonthWeekEventsView;
import com.android.calendar.wxapi.WXEntryActivity;
import com.aurora.calendar.AuroraMonthAgendaAdapter;
import com.aurora.calendar.period.AuroraPeriodExplanationActivity;
import com.aurora.calendar.period.PeriodInfo;
import com.aurora.calendar.report.ReportCommand;
import com.aurora.calendar.report.ReportUtil;
import com.aurora.commemoration.model.RememberDayInfo;
import com.gionee.calendar.GNCalendarUtils;
import com.gionee.calendar.statistics.Statistics;
import com.mediatek.calendar.lunar.LunarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraListView;

public class GNMonthView extends LinearLayout implements AuroraListView.AuroraHandleOnTouchEvnetSelf {
	private static final String LOG_TAG = "GNMonthView";
	
	private static int sViewId = 0;
	private int mViewId = -1;
	
	public int getViewId() {
		return mViewId;
	}
	
	// fields for initial state start
	private static final int DEFAULT_WEEK_COUNT = 6;
	private static final int DAYS_PER_WEEK = 7;
	private static final int DEFAULT_FIRST_WEEK_DAY = Time.MONDAY;
	// fields for initial state end

	// fields for params config state start
	private static final int PARAMS_TYPE_CORE 	= 0x01;
	private static final int PARAMS_TYPE_SOLAR 	= (0x1 << 1);
	private static final int PARAMS_TYPE_LUNAR 	= (0x1 << 2);
	private static final int PARAMS_TYPE_EVENTS = (0x1 << 3);

	private int mParamsConfigState = 0;
	// fields for params config state end

	private boolean isChineseEnvironment = true;

	private static final int MAX_PERIOD_LASTDAYS = 15;
	private ArrayList<PeriodInfo> mPeriodInfos = null;
	private boolean isPeriod = false;
	private int lastPeriodStartDay;
	private int lastPeriodFinishDay;
	private int defaultPeriodLastDays;
	private int defaultPeriodCycle;

	private Context mContext = null;
	private ViewSwitcher mViewSwitcher = null;
	private MonthByWeekFragment mParentFragment = null;

	// fields for layout & view start
	private ViewGroup mMonthHeaderView = null;
	private BaseAdapter mAdapter = null;
	private LayoutInflater mLayoutInflater = null;
	private MonthWeekEventsView[] mWeekViews = null;
	private MonthWeekEventsView mTappedView = null;

	private String[] mDayNameLabels;
	// fields for layout & view end
	
	// fields for date start
	private Time mBaseDate = new Time();
	private int mFirstMonthJulianDay = -1;
	private int mLastMonthJulianDay = -1;
	
	private int mLastSecondaryDayInFirstWeek = -1;
	private int mFirstSecondaryDayInLastWeek = -1;
	
	private boolean mHasToday = false;
	private int mTodayJulianDay = -1;
	private boolean mIsTodaySecondary = false;
	
	private Time mSelectedDate = new Time();
	private Time mPreviousSelectedDate = new Time();
	private int mSelectedJulianDay = -1;
	
	private int mHighlightWeekIndex = -1;
	private int mHighlightWeekDayIndex = -1;
	private int mActualWeekCount = DEFAULT_WEEK_COUNT;
	
	private int mPrevHighlightWeekIndex = -2;
	private int mPrevHighlightWeekDayIndex = -2;

	private void resetPrevHighlightIndex() {
		mPrevHighlightWeekIndex = -2;
		mPrevHighlightWeekDayIndex = -2;
	}
	
	private void resetHighlightIndex() {
		mHighlightWeekIndex = -1;
		mHighlightWeekDayIndex = -1;
	}

	private boolean mShowWeekNum = false;
	// fields for date end
	
	// fields for color start
	private int mSundayColor = 0;
	private int mSaturdayColor = 0;
	private int mColorDayNames = 0;
	private int mColorDayNamesWeekend = 0;
	private int mColorDayNamesToday = 0;
	private int mBgColorMonthHeader = 0;
	private int mBgColorMonthView = 0;
	
	private int mColorTest = 0;
	private int mColorTest2 = 0;
	// fields for color end
	
	// Gionee <jiangxiao> <2013-06-20> modify for CR00827533 begin
	// this constant is used to adjust the height of agenda list
	// fields for measurement start
	private static final int BAR_HEIGHT = 47 + 27; // 56(TOP BAR) + 32(MONTH HEADER) + 48(BOTTOM BAR)
	// Gionee <jiangxiao> <2013-06-20> modify for CR00827533 end
	
	private float mScale = 0;
	
	private int mViewPadding = 0;
	private int mViewWidth = 0;
	private int mViewHeight = 0;
	
	private int mWeekViewWidth = 0;
	private int mWeekViewHeight = 0;
	private int mWeekViewLeft = 0;
	private int mWeekViewTop = 0;
	// fields for measurement end
	
	private boolean mIsFirstLoaded = true;
	
	// fields for touch events start
	private static final int TOUCH_MODE_UNKNOWN = 0;
	private static final int TOUCH_MODE_DOWN = 0x01;
	private static final int TOUCH_MODE_HSCROLL = 0x02;
	private static final int TOUCH_MODE_VSCROLL = 0x04;
	
	private static final float SCROLL_SENSITIVITY = 1.7f;
	// the distance on Y-axis should bigger than this value
	private static final float MIN_VSCROLL_DISTANCE = 37.0f/*60.0f*/;
	private static final float MIN_HSCROLL_VELOCITY = 1300.0f;
	
	private static final long DURATION_MONTH_SWITCHIGN = 450;
	private static final Interpolator DEFAULT_INTERPOLATER = new AccelerateDecelerateInterpolator();
	
	private int mTouchMode = TOUCH_MODE_UNKNOWN;
    private boolean mIsWeekviewMode = false;
    private boolean staticMove = false;
    private static final long DURATION_WEEK_SWITCH = 150;
	
	private GestureDetector mGestureDetector = null;
	private VelocityTracker mVelocityTracker = null;
	private boolean mIsNewScrolling = false;
	private float mSumScrollX = 0;
	private float mSumScrollY = 0;
	private float mFirstTouchedX = 0;
	private float mFirstTouchedY = 0;
	// fields for touch events end

	// fields for events start
	private EventLoader mEventLoader = null;
	private ArrayList<Event> mEvents = new ArrayList<Event>();
	private ArrayList<Event> mAdapterEvents = new ArrayList<Event>();
    private ArrayList<Event> mBirthdayReminderEvents = new ArrayList<Event>();

    private List<RememberDayInfo> mRememberDays = new ArrayList<RememberDayInfo>();

	// private AuroraListView mSelectedDateAgendaListView = null;
	private String[] mLongPressMenuItems = null;
	private GNMonthAgendaArrayAdapter mMonthDayAgendaAdapter = null;
	private AuroraMonthAgendaAdapter mMonthDayAdapter = null;

	private static final int MESSAGE_ID_EVENTS_LOADED = 1;

	private Handler mEventLoaderHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
        	this.obtainMessage();
			switch(msg.what) {
				case MESSAGE_ID_EVENTS_LOADED:
					// update events ListView
                    // updateActionBarTime(mSelectedDate);
					updateEventsListView();
					break;
				default:
					break;
			}
            super.handleMessage(msg);
		}
	};

	private boolean isTodaySelected() {
		if (mSelectedDate == null) return false;

		int selectedJulianDay = Time.getJulianDay(mSelectedDate.toMillis(true), mSelectedDate.gmtoff);

		Time now = new Time();
		now.setToNow();
		now.normalize(true);
		int todayJulianDay = Time.getJulianDay(now.toMillis(true), now.gmtoff);

		return todayJulianDay == selectedJulianDay;
	}

	private void updateEventsListView() {
		Log.d("DEBUG", "mEventsLoadingFinishedCallback");

        if (mAgendaList == null) return;

        mAdapterEvents.clear();
        mBirthdayReminderEvents.clear();
        for (Event event : mEvents) {
            if (Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME.equals(event.ownerAccount)) {
                mBirthdayReminderEvents.add(event);
            } else {
                mAdapterEvents.add(event);
            }
        }

    if (Utils.isIndiaVersion()) {

        if (!mBirthdayReminderEvents.isEmpty()) {
            mAgendaList.setAdapter(null);
            mAgendaList.removeHeaderView(mBirthdayReminderView);
            mAgendaList.addHeaderView(mBirthdayReminderView);

            CharSequence name = mBirthdayReminderEvents.get(0).title;
            if (mBirthdayReminderEvents.size() == 1) {
                mBirthdayReminderContentView.setText(mContext.getString(R.string.aurora_birthday_only_one, name));
            } else {
                mBirthdayReminderContentView.setText(mContext.getString(R.string.aurora_birthday_have_other, name,
                        mBirthdayReminderEvents.size()));
            }
        } else {
            mAgendaList.removeHeaderView(mBirthdayReminderView);
        }

		if (mMonthDayAgendaAdapter == null) {
			mMonthDayAgendaAdapter = new GNMonthAgendaArrayAdapter(mContext, mAdapterEvents, mParentFragment);
		}

		Log.d("DEBUG", "mAdapterEvents size = " + mAdapterEvents.size());
		Log.d("DEBUG", "mEvents size = " + mEvents.size());
		// mAdapterEvents.clear();
		// mAdapterEvents.addAll(mEvents);
		if (mAgendaList != null) {
            mAgendaList.auroraHideHeaderView();
            mAgendaList.auroraSetRubbishBack();
			if (mAgendaList.getAdapter() == mMonthDayAgendaAdapter) {
				mMonthDayAgendaAdapter.notifyDataSetChanged();
			} else {
				mAgendaList.setAdapter(mMonthDayAgendaAdapter);
			}
		} else {
			throw new RuntimeException("mAgendaList should not be null");
		}

		hideAgendaList(mEvents.size() == 0);

		return;
    }

        mRememberDays.clear();
        if (isTodaySelected() && mParentFragment.getRememberDays() != null) {
            mRememberDays.addAll(mParentFragment.getRememberDays());
        }

		boolean isEmpty = mEvents.isEmpty() && (mRememberDays == null || mRememberDays.isEmpty());

		if (!isEmpty) {
        	mAgendaList.setAdapter(null);
            mAgendaList.removeHeaderView(mAddEventView);
            mAgendaList.addHeaderView(mAddEventView);
        } else {
        	mAgendaList.removeHeaderView(mAddEventView);
        }

		if (mMonthDayAdapter == null) {
			mMonthDayAdapter = new AuroraMonthAgendaAdapter(mContext, mParentFragment, 
					mRememberDays, mBirthdayReminderEvents, mAdapterEvents);
		}

		mAgendaList.auroraHideHeaderView();
        mAgendaList.auroraSetRubbishBack();
		if (mAgendaList.getAdapter() == mMonthDayAdapter) {
			mMonthDayAdapter.notifyDataSetChanged();
		} else {
			mAgendaList.setAdapter(mMonthDayAdapter);
		}

		hideAgendaList(isEmpty);
	}

	private Runnable mEventsLoadingFinishedCallback = new Runnable() {
		@Override
		public void run() {
			mEventLoaderHandler.sendEmptyMessage(MESSAGE_ID_EVENTS_LOADED);
		}
	};
	
	private Runnable mEventsLoadingCanceledCallback = new Runnable() {
		@Override
		public void run() {
			Log.d(LOG_TAG, "events query canceled");
			// TODO
		}
	};
	// fields for events end

	// fields for CalendarController begin
	private static final long DEFAULT_EVENT_ID = -1;

	private void updatePeriodStartFinish(final int selectedJulianDay) {
		Time now = new Time();
		now.setToNow();
		now.normalize(true);
		final int todayJulianDay = Time.getJulianDay(now.toMillis(true), now.gmtoff);

		if (selectedJulianDay > todayJulianDay) {
			setPeriodStartFinish(false, false);
			return;
		}

		mPeriodStartListener = null;
		mPeriodFinishListener = null;

		if (lastPeriodStartDay == 0) {

			setPeriodStartFinish(true, false);

			mPeriodStartListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					/*PeriodInfo info = new PeriodInfo();
					info.setStartDay(selectedJulianDay);
					info.setFinishDay(selectedJulianDay + defaultPeriodLastDays - 1);
					long id = mParentFragment.getPeriodInfoAdatper().insert(info);
					info.setId((int) id);
					if (mPeriodInfos == null) {
						mPeriodInfos = new ArrayList<PeriodInfo>();
					}
					mPeriodInfos.add(info);

					updatePeriodInfos();
					refreshPeriodView(selectedJulianDay, todayJulianDay);*/

					if (mPeriodInfos == null) {
						mPeriodInfos = new ArrayList<PeriodInfo>();
					}
					handlePeriodAddNew(-1, selectedJulianDay, defaultPeriodLastDays, todayJulianDay);
				}
			};
			mPeriodStart.setOnClickListener(mPeriodStartListener);

		} else if (selectedJulianDay > lastPeriodFinishDay) {

			if (selectedJulianDay == lastPeriodFinishDay + 1) {
				setPeriodStartFinish(false, true);
			} else if (selectedJulianDay < lastPeriodStartDay + MAX_PERIOD_LASTDAYS) {
				setPeriodStartFinish(true, true);
			} else {
				setPeriodStartFinish(true, false);
			}

			mPeriodStartListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					/*PeriodInfo info = new PeriodInfo();
					info.setStartDay(selectedJulianDay);
					info.setFinishDay(selectedJulianDay + defaultPeriodLastDays - 1);
					long id = mParentFragment.getPeriodInfoAdatper().insert(info);
					info.setId((int) id);
					mPeriodInfos.add(0, info);

					updatePeriodInfos();
					refreshPeriodView(selectedJulianDay, todayJulianDay);*/

					handlePeriodAddNew(0, selectedJulianDay, defaultPeriodLastDays, todayJulianDay);
				}
			};
			mPeriodStart.setOnClickListener(mPeriodStartListener);

			mPeriodFinishListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					/*PeriodInfo info = mPeriodInfos.get(0);
					info.setFinishDay(selectedJulianDay);
					mParentFragment.getPeriodInfoAdatper().update(info);
					mPeriodInfos.set(0, info);

					updatePeriodInfos();
					refreshPeriodView(selectedJulianDay, todayJulianDay);*/

					handlePeriodStartFinishUpdate(0, selectedJulianDay, todayJulianDay, false);
				}
			};
			mPeriodFinish.setOnClickListener(mPeriodFinishListener);

		} else if (selectedJulianDay >= lastPeriodStartDay) {

			if (selectedJulianDay == lastPeriodStartDay && selectedJulianDay == lastPeriodFinishDay) {
				setPeriodStartFinish(false, false);
			} else if (selectedJulianDay == lastPeriodStartDay) {
				setPeriodStartFinish(false, true);
			} else if (selectedJulianDay == lastPeriodFinishDay) {
				setPeriodStartFinish(true, false);
			} else {
				setPeriodStartFinish(true, true);
			}

			mPeriodStartListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					/*PeriodInfo info = mPeriodInfos.get(0);
					info.setStartDay(selectedJulianDay);
					mParentFragment.getPeriodInfoAdatper().update(info);
					mPeriodInfos.set(0, info);

					updatePeriodInfos();
					refreshPeriodView(selectedJulianDay, todayJulianDay);*/

					handlePeriodStartFinishUpdate(0, selectedJulianDay, todayJulianDay, true);
				}
			};
			mPeriodStart.setOnClickListener(mPeriodStartListener);

			mPeriodFinishListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					/*PeriodInfo info = mPeriodInfos.get(0);
					info.setFinishDay(selectedJulianDay);
					mParentFragment.getPeriodInfoAdatper().update(info);
					mPeriodInfos.set(0, info);

					updatePeriodInfos();
					refreshPeriodView(selectedJulianDay, todayJulianDay);*/

					handlePeriodStartFinishUpdate(0, selectedJulianDay, todayJulianDay, false);
				}
			};
			mPeriodFinish.setOnClickListener(mPeriodFinishListener);

		} else if (mPeriodInfos.size() == 1) {

			int type = 0;
			if (selectedJulianDay > lastPeriodFinishDay - MAX_PERIOD_LASTDAYS) {
				type = 1;
				setPeriodStartFinish(true, false);
			} else if (selectedJulianDay < lastPeriodStartDay - defaultPeriodLastDays) {
				type = 2;
				setPeriodStartFinish(true, false);
			}  else {
				setPeriodStartFinish(false, false);
			}

			 if (type == 1) {
				 mPeriodStartListener = new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						/*PeriodInfo info = mPeriodInfos.get(0);
						info.setStartDay(selectedJulianDay);
						mParentFragment.getPeriodInfoAdatper().update(info);
						mPeriodInfos.set(0, info);

						updatePeriodInfos();
						refreshPeriodView(selectedJulianDay, todayJulianDay);*/

						handlePeriodStartFinishUpdate(0, selectedJulianDay, todayJulianDay, true);
					}
				};
			} else if (type == 2) {
				mPeriodStartListener = new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						/*PeriodInfo info = new PeriodInfo();
						info.setStartDay(selectedJulianDay);
						info.setFinishDay(selectedJulianDay + defaultPeriodLastDays - 1);
						long id = mParentFragment.getPeriodInfoAdatper().insert(info);
						info.setId((int) id);
						mPeriodInfos.add(info);

						updatePeriodInfos();
						refreshPeriodView(selectedJulianDay, todayJulianDay);*/

						handlePeriodAddNew(-1, selectedJulianDay, defaultPeriodLastDays, todayJulianDay);
					}
				};
			}
			mPeriodStart.setOnClickListener(mPeriodStartListener);

		} else {
			int prePeriodStartDay = 0;
			int prePeriodFinishday = 0;
			int nextPeriodStartDay = 0;
			int nextPeriodFinishDay = 0;

			for (int i = 1; i < mPeriodInfos.size(); i++) {
				prePeriodStartDay = mPeriodInfos.get(i).getStartDay();
				prePeriodFinishday = mPeriodInfos.get(i).getFinishDay();

				final int index = i;

				if (selectedJulianDay >= prePeriodStartDay) {

					nextPeriodStartDay = mPeriodInfos.get(i - 1).getStartDay();
					nextPeriodFinishDay = mPeriodInfos.get(i - 1).getFinishDay();

					if (selectedJulianDay > prePeriodFinishday) {

						if (selectedJulianDay == prePeriodFinishday + 1 && 
								selectedJulianDay == nextPeriodStartDay - 1) {

							setPeriodStartFinish(false, false);

						} else if (selectedJulianDay >= prePeriodStartDay + MAX_PERIOD_LASTDAYS && 
								selectedJulianDay <= nextPeriodFinishDay - MAX_PERIOD_LASTDAYS) {

							if (selectedJulianDay < nextPeriodStartDay - 5) {
								setPeriodStartFinish(true, false);

								mPeriodStartListener = new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										/*PeriodInfo info = new PeriodInfo();
										info.setStartDay(selectedJulianDay);
										info.setFinishDay(selectedJulianDay + 4);
										long id = mParentFragment.getPeriodInfoAdatper().insert(info);
										info.setId((int) id);
										mPeriodInfos.add(index, info);

										updatePeriodInfos();
										refreshPeriodView(selectedJulianDay, todayJulianDay);*/

										handlePeriodAddNew(index, selectedJulianDay, 5, todayJulianDay);
									}
								};
								mPeriodStart.setOnClickListener(mPeriodStartListener);
							} else {
								setPeriodStartFinish(false, false);
							}

						} else if (selectedJulianDay < nextPeriodStartDay - 1 && 
								selectedJulianDay < prePeriodStartDay + MAX_PERIOD_LASTDAYS && 
								selectedJulianDay > prePeriodFinishday + 1 && 
								selectedJulianDay > nextPeriodFinishDay - MAX_PERIOD_LASTDAYS) {

							setPeriodStartFinish(true, true);

							mPeriodStartListener = new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									/*PeriodInfo info = mPeriodInfos.get(index - 1);
									info.setStartDay(selectedJulianDay);
									mParentFragment.getPeriodInfoAdatper().update(info);
									mPeriodInfos.set(index - 1, info);

									updatePeriodInfos();
									refreshPeriodView(selectedJulianDay, todayJulianDay);*/

									handlePeriodStartFinishUpdate(index - 1, selectedJulianDay, todayJulianDay, true);
								}
							};
							mPeriodStart.setOnClickListener(mPeriodStartListener);
							
							mPeriodFinishListener = new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									/*PeriodInfo info = mPeriodInfos.get(index);
									info.setFinishDay(selectedJulianDay);
									mParentFragment.getPeriodInfoAdatper().update(info);
									mPeriodInfos.set(index, info);

									updatePeriodInfos();
									refreshPeriodView(selectedJulianDay, todayJulianDay);*/

									handlePeriodStartFinishUpdate(index, selectedJulianDay, todayJulianDay, false);
								}
							};
							mPeriodFinish.setOnClickListener(mPeriodFinishListener);

						} else if (selectedJulianDay < nextPeriodStartDay - 1 && 
								selectedJulianDay < prePeriodStartDay + MAX_PERIOD_LASTDAYS) {

							setPeriodStartFinish(false, true);

							mPeriodFinishListener = new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									/*PeriodInfo info = mPeriodInfos.get(index);
									info.setFinishDay(selectedJulianDay);
									mParentFragment.getPeriodInfoAdatper().update(info);
									mPeriodInfos.set(index, info);

									updatePeriodInfos();
									refreshPeriodView(selectedJulianDay, todayJulianDay);*/

									handlePeriodStartFinishUpdate(index, selectedJulianDay, todayJulianDay, false);
								}
							};
							mPeriodFinish.setOnClickListener(mPeriodFinishListener);

						} else if (selectedJulianDay > prePeriodFinishday + 1 && 
								selectedJulianDay > nextPeriodFinishDay - MAX_PERIOD_LASTDAYS) {

							setPeriodStartFinish(true, false);

							mPeriodStartListener = new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									/*PeriodInfo info = mPeriodInfos.get(index - 1);
									info.setStartDay(selectedJulianDay);
									mParentFragment.getPeriodInfoAdatper().update(info);
									mPeriodInfos.set(index - 1, info);

									updatePeriodInfos();
									refreshPeriodView(selectedJulianDay, todayJulianDay);*/

									handlePeriodStartFinishUpdate(index - 1, selectedJulianDay, todayJulianDay, true);
								}
							};
							mPeriodStart.setOnClickListener(mPeriodStartListener);

						} else {
							setPeriodStartFinish(false, false);
						}

					} else {

						if (selectedJulianDay == prePeriodStartDay && selectedJulianDay == prePeriodFinishday) {
							setPeriodStartFinish(false, false);
						} else if (selectedJulianDay == prePeriodStartDay) {
							setPeriodStartFinish(false, true);
						} else if (selectedJulianDay == prePeriodFinishday) {
							setPeriodStartFinish(true, false);
						} else {
							setPeriodStartFinish(true, true);
						}

						mPeriodStartListener = new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								/*PeriodInfo info = mPeriodInfos.get(index);
								info.setStartDay(selectedJulianDay);
								mParentFragment.getPeriodInfoAdatper().update(info);
								mPeriodInfos.set(index, info);

								updatePeriodInfos();
								refreshPeriodView(selectedJulianDay, todayJulianDay);*/

								handlePeriodStartFinishUpdate(index, selectedJulianDay, todayJulianDay, true);
							}
						};
						mPeriodStart.setOnClickListener(mPeriodStartListener);

						mPeriodFinishListener = new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								/*PeriodInfo info = mPeriodInfos.get(index);
								info.setFinishDay(selectedJulianDay);
								mParentFragment.getPeriodInfoAdatper().update(info);
								mPeriodInfos.set(index, info);

								updatePeriodInfos();
								refreshPeriodView(selectedJulianDay, todayJulianDay);*/

								handlePeriodStartFinishUpdate(index, selectedJulianDay, todayJulianDay, false);
							}
						};
						mPeriodFinish.setOnClickListener(mPeriodFinishListener);

					}

					break;
				} else if (i == mPeriodInfos.size() - 1) {

					int type = 0;
					if (selectedJulianDay > prePeriodFinishday - MAX_PERIOD_LASTDAYS) {
						type = 1;
						setPeriodStartFinish(true, false);
					} else if (selectedJulianDay < prePeriodStartDay - defaultPeriodLastDays) {
						type = 2;
						setPeriodStartFinish(true, false);
					}  else {
						setPeriodStartFinish(false, false);
					}

					if (type == 1) {
						mPeriodStartListener = new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								/*PeriodInfo info = mPeriodInfos.get(index);
								info.setStartDay(selectedJulianDay);
								mParentFragment.getPeriodInfoAdatper().update(info);
								mPeriodInfos.set(index, info);

								updatePeriodInfos();
								refreshPeriodView(selectedJulianDay, todayJulianDay);*/

								handlePeriodStartFinishUpdate(index, selectedJulianDay, todayJulianDay, true);
							}
						};
					} else if (type == 2) {
						mPeriodStartListener = new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								/*PeriodInfo info = new PeriodInfo();
								info.setStartDay(selectedJulianDay);
								info.setFinishDay(selectedJulianDay + defaultPeriodLastDays - 1);
								long id = mParentFragment.getPeriodInfoAdatper().insert(info);
								info.setId((int) id);
								mPeriodInfos.add(info);

								updatePeriodInfos();
								refreshPeriodView(selectedJulianDay, todayJulianDay);*/

								handlePeriodAddNew(-1, selectedJulianDay, defaultPeriodLastDays, todayJulianDay);
							}
						};
					}
					mPeriodStart.setOnClickListener(mPeriodStartListener);
				}
			}
		}
	}

	private void setPeriodStartFinish(boolean startEnable, boolean finishEnable) {
		if (mPeriodStart != null && mPeriodFinish != null) {
			mPeriodStart.setEnabled(startEnable);
			mPeriodStart.setClickable(startEnable);
			mPeriodStart.setBackgroundResource(startEnable ? R.drawable.aurora_btn_blue : R.drawable.aurora_btn_grey);

			mPeriodFinish.setEnabled(finishEnable);
			mPeriodFinish.setClickable(finishEnable);
			mPeriodFinish.setBackgroundResource(finishEnable ? R.drawable.aurora_btn_green : R.drawable.aurora_btn_grey);
		}
	}

	private void refreshPeriodView(int selectedJulianDay, int todayJulianDay) {
		updatePeriodTitleSummary(todayJulianDay);
		updatePeriodStartFinish(selectedJulianDay);
		updateWeekViews();
	}

	private void handlePeriodAddNew(final int index, final int selectedJulianDay, final int periodLastDays, 
			final int todayJulianDay) {

		String message = mContext.getString(R.string.aurora_period_add_new, getDateString(selectedJulianDay));

		new AuroraAlertDialog.Builder(mContext)
				.setMessage(message)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						PeriodInfo info = new PeriodInfo();
						info.setStartDay(selectedJulianDay);
						info.setFinishDay(selectedJulianDay + periodLastDays - 1);
						long id = mParentFragment.getPeriodInfoAdatper().insert(info);
						info.setId((int) id);

						if (index == -1) {
							mPeriodInfos.add(info);
						} else {
							mPeriodInfos.add(index, info);
						}

						updatePeriodInfos();
						refreshPeriodView(selectedJulianDay, todayJulianDay);
					}
				}).show();
	}

	private void handlePeriodStartFinishUpdate(final int index, final int selectedJulianDay, final int todayJulianDay, 
			final boolean updateStart) {

		String message = mContext.getString(
				updateStart ? R.string.aurora_period_start_update : R.string.aurora_period_finish_update, 
						getDateString(selectedJulianDay));

		new AuroraAlertDialog.Builder(mContext)
				.setMessage(message)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
                    public void onClick(DialogInterface dialog, int which) {
						PeriodInfo info = mPeriodInfos.get(index);

						if (updateStart) {
							info.setStartDay(selectedJulianDay);
						} else {
							info.setFinishDay(selectedJulianDay);
						}

						mParentFragment.getPeriodInfoAdatper().update(info);
						mPeriodInfos.set(index, info);

						updatePeriodInfos();
						refreshPeriodView(selectedJulianDay, todayJulianDay);
					}
				}).show();
	}

	private void updatePeriodTitleSummary(int todayJulianDay) {
		String title = mContext.getString(R.string.aurora_period_title);
		String summary = mContext.getString(R.string.aurora_period_summary, 0);
		int number = 0;

		if (lastPeriodStartDay == 0) {
			setPeriodTitleSummary(title, summary, number);
			return;
		}

		if (todayJulianDay < lastPeriodStartDay) {
			if (mPeriodInfos.size() == 1) {
				title = mContext.getString(R.string.aurora_period_title_3,
						getDateString(lastPeriodStartDay));
				summary = mContext.getString(R.string.aurora_period_summary_2,
						lastPeriodStartDay - todayJulianDay);

				number = lastPeriodStartDay - todayJulianDay;
			} else {
				for (int i = 1; i < mPeriodInfos.size(); i++) {
					int periodStartDay = mPeriodInfos.get(i).getStartDay();
					int periodFinishDay = mPeriodInfos.get(i).getFinishDay();

					if (todayJulianDay < periodStartDay) {
						if (i == mPeriodInfos.size() - 1) {
							title = mContext.getString(R.string.aurora_period_title_3,
									getDateString(periodStartDay));
							summary = mContext.getString(R.string.aurora_period_summary_2,
									periodStartDay - todayJulianDay);

							number = periodStartDay - todayJulianDay;
						}
					} else if (todayJulianDay <= periodFinishDay) {
						title = mContext.getString(R.string.aurora_period_title_2,
								getDateString(periodStartDay));
						summary = mContext.getString(R.string.aurora_period_summary,
								todayJulianDay - periodStartDay + 1);

						number = todayJulianDay - periodStartDay + 1;
					} else {
						int nextPeriodStartDay = mPeriodInfos.get(i - 1).getStartDay();
						title = mContext.getString(R.string.aurora_period_title_3,
								getDateString(nextPeriodStartDay));
						summary = mContext.getString(R.string.aurora_period_summary_2,
								nextPeriodStartDay - todayJulianDay);

						number = nextPeriodStartDay - todayJulianDay;
					}
				}
			}
		} else if (todayJulianDay <= lastPeriodFinishDay) {
			title = mContext.getString(R.string.aurora_period_title_2,
					getDateString(lastPeriodStartDay));
			summary = mContext.getString(R.string.aurora_period_summary,
					todayJulianDay - lastPeriodStartDay + 1);

			number = todayJulianDay - lastPeriodStartDay + 1;
		} else {
			int nextPeriodStartDay = lastPeriodStartDay;
    		while (nextPeriodStartDay <= todayJulianDay) {
    			nextPeriodStartDay += defaultPeriodCycle;
    		}
    		int thisPeriodStartDay = nextPeriodStartDay - defaultPeriodCycle;
    		int thisPeriodFinishDay = thisPeriodStartDay + defaultPeriodLastDays - 1;

    		if (thisPeriodStartDay == lastPeriodStartDay) {
    			thisPeriodFinishDay = lastPeriodFinishDay;
    		}

    		if (todayJulianDay <= thisPeriodFinishDay) {
    			title = mContext.getString(R.string.aurora_period_title_2,
    					getDateString(thisPeriodStartDay));
    			summary = mContext.getString(R.string.aurora_period_summary_3,
    					todayJulianDay - thisPeriodStartDay);

    			number = todayJulianDay - thisPeriodStartDay;
    		} else {
    			title = mContext.getString(R.string.aurora_period_title_3,
						getDateString(nextPeriodStartDay));
				summary = mContext.getString(R.string.aurora_period_summary_2,
						nextPeriodStartDay - todayJulianDay);

				number = nextPeriodStartDay - todayJulianDay;
    		}
		}

		setPeriodTitleSummary(title, summary, number);
	}

	private String getDateString(int julianDay) {
		Time now = new Time();
		now.setToNow();
		now.normalize(true);

		Time time = new Time();
		time.setJulianDay(julianDay);
		long millis = time.normalize(true);

		int flags = DateUtils.FORMAT_SHOW_DATE;
        if (time.year == now.year) {
            flags |= DateUtils.FORMAT_ABBREV_MONTH;
        }
        return DateUtils.formatDateTime(mContext, millis, flags);
	}

	private void setPeriodTitleSummary(String title, String summary, int number) {
		if (mPeriodTitle != null && mPeriodSummary != null) {
			mPeriodTitle.setText(title);
			mPeriodSummary.setText(summary);
			/*String numberStr = String.valueOf(number);
			SpannableStringBuilder ssb = new SpannableStringBuilder(summary);
			ssb.setSpan(new RelativeSizeSpan(34), summary.indexOf(numberStr), 
					summary.indexOf(numberStr) + numberStr.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			mPeriodSummary.setText(ssb);*/
		}
	}

	private void updateActionBarTime(Time targetDate) {
		if(targetDate == null || targetDate.year < 1970 || targetDate.year > 2036) {
			return;
		}
		
		Time now = new Time(targetDate.timezone);
		now.setToNow();
		now.normalize(true);
		targetDate.hour = now.hour;
		targetDate.minute = now.minute;
		targetDate.second = now.second;
		long millis = targetDate.normalize(true);
		// Log.d("update_time", "update title bar time to " + GNCalendarUtils.printTime(targetDate));

        int todayJulianDay = Utils.getJulianDayInGeneral(now, true);
        int selectJulianDay = Utils.getJulianDayInGeneral(targetDate, true);

        if (isPeriod) {
        	updatePeriodTitleSummary(todayJulianDay);
        } else {
        	int flags = DateUtils.FORMAT_SHOW_DATE;
            if (targetDate.year == now.year) {
                flags |= DateUtils.FORMAT_ABBREV_MONTH;
            }
            String solarDateString = DateUtils.formatDateTime(mContext, millis, flags);
            if (selectJulianDay == todayJulianDay) {
            	solarDateString = mContext.getString(R.string.today);
            }

            if (isChineseEnvironment) {
                mSolarDateView.setText(solarDateString.replace(" ", ""));

                String lunarDateString = mLunarUtil.getLunarDateString(targetDate.year, targetDate.month + 1,
                		targetDate.monthDay);
                mLunarDateView.setText(lunarDateString);
            } else {
            	mSolarDateView.setText(solarDateString);
                mLunarDateView.setVisibility(View.GONE);
            }
        }

		Log.d("DEBUG", "2 - update time " + GNCalendarUtils.printDate(targetDate));
		CalendarController controller = CalendarController.getInstance(mContext);
		controller.sendEvent(mContext, EventType.UPDATE_TITLE, null, null, targetDate, 
				DEFAULT_EVENT_ID, ViewType.CURRENT, 0, null, null);
	}
	
	private Time getTimeOfController() {
		CalendarController controller = CalendarController.getInstance(mContext);
		Time time = new Time();
		time.set(controller.getTime());
		time.normalize(true);
		
		return time;
	}
	
	private void setTimeOfController(long millis) {
		CalendarController controller = CalendarController.getInstance(mContext);
		
		Time t = new Time();
		t.set(millis);
		t.normalize(true);
		// if(t.year < GNCalendarUtils.MIN_YEAR_NUM) {
		// t.year = GNCalendarUtils.MIN_YEAR_NUM;
		// t.month = 0;
		// t.monthDay = 1;
		// t.normalize(true);
		//
		// controller.setTime(t.toMillis(true));
		// } else if(t.year > GNCalendarUtils.MAX_YEAR_NUM) {
		// t.year = GNCalendarUtils.MAX_YEAR_NUM;
		// t.month = 11;
		// t.monthDay = 31;
		// t.normalize(true);
		//
		// controller.setTime(t.toMillis(true));
		// } else {
		// controller.setTime(millis);
		// }
		controller.setTime(millis);
	}
	
	// fields for CalendarController end
	
	public GNMonthView(Context context) {
		super(context);
		initMonthView(context);
		this.mContext = context;
	}
	
	public GNMonthView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initMonthView(context);
		this.mContext = context;
	}
	
	public GNMonthView(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
		initMonthView(context);
		this.mContext = context;
	}
	
	// methods for params start
	private void resetParamsConfigState() {
		mParamsConfigState = 0;
	}
	
	private void setParamsConfigState(int type) {
		mParamsConfigState &= type;
	}
	
	private boolean checkParamsConfigState(int type) {
		return ((mParamsConfigState & type) != 0);
	}
	// methods for params end
	
	// impl methods about gesture & touch event start
	private class MonthGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent event) {
			// Log.d(LOG_TAG, "DETECT onDown");
			mTouchMode = TOUCH_MODE_DOWN;
			// selectMonthDay(event.getX(0), event.getY(0));

			return true;
		}
		
		@Override
		public boolean onSingleTapUp(MotionEvent event) {
			// Log.d(LOG_TAG, "DETECT onSingleTapUp");
			selectMonthDay(event.getX(0), event.getY(0));
			
			return true;
		}
		
		@Override
		public void onShowPress(MotionEvent event) {
			Log.d(LOG_TAG, "DETECT onShowPress");
			selectMonthDay(event.getX(0), event.getY(0));
		}
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float deltaX, float deltaY) {
			// Log.d(LOG_TAG, "DETECT onScroll");
			if(mIsNewScrolling) {
				mSumScrollX = 0;
				mSumScrollY = 0;
				mIsNewScrolling = false;
			}
			mSumScrollX += deltaX;
			mSumScrollY += deltaY;
			
			if(mTouchMode == TOUCH_MODE_DOWN) {
				float absSumScrollX = Math.abs(mSumScrollX);
				float absSumScrollY = Math.abs(mSumScrollY);
				
				if(absSumScrollX * SCROLL_SENSITIVITY > absSumScrollY) {
					if(absSumScrollX > MIN_VSCROLL_DISTANCE * mScale) {
//						mVelocityTracker.computeCurrentVelocity(1000);
//						float velocity = Math.abs(mVelocityTracker.getXVelocity());
//						mVelocityTracker.recycle();

						Log.d("debug2", "HSCROLL start Y " + e1.getY());
						Log.d("debug2", "HSCROLL total Y " + mActualWeekCount * mWeekViewHeight);
                        // if(e1.getY() < mActualWeekCount * mWeekViewHeight) {
                        if ((e1.getY() < mActualWeekCount * mWeekViewHeight && !mIsWeekviewMode) ||
                                (e1.getY() < mWeekViewHeight + 37 * mScale && mIsWeekviewMode)) {
							mTouchMode = TOUCH_MODE_HSCROLL;
						}
					}
				} else if(absSumScrollY < MIN_VSCROLL_DISTANCE * mScale) {
					mTouchMode = TOUCH_MODE_DOWN;
				} else {
					mTouchMode = TOUCH_MODE_VSCROLL;
				}
			} else if(mTouchMode == TOUCH_MODE_HSCROLL) {
				//TODO: switch to adjacent month
                // handleHScroll(e2);
                if (!mIsWeekviewMode) {
                    handleHScroll(e2);
                } else {
                    handleHorizontalScroll();
                }
            } else if(mTouchMode == TOUCH_MODE_VSCROLL) {
                handleVerticalScroll();
			}
			
			return true;
		}
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		if(mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		
		switch(action) {
			case MotionEvent.ACTION_CANCEL:
				//Log.d(LOG_TAG, "INTERCEPT ACTION_CANCEL");
				return false;
				
			case MotionEvent.ACTION_DOWN:
				//Log.d(LOG_TAG, "INTERCEPT ACTION_DOWN");
				mIsNewScrolling = true;
				mGestureDetector.onTouchEvent(event);
				return false;
				
//			case MotionEvent.ACTION_MOVE:
//				// Log.d(LOG_TAG, "INTERCEPT ACTION_MOVE");
//				mGestureDetector.onTouchEvent(event);
//				return false;
				
			case MotionEvent.ACTION_UP:
				//Log.d(LOG_TAG, "INTERCEPT ACTION_UP");
				mIsNewScrolling = false;
				mGestureDetector.onTouchEvent(event);
				
				return false;
			
			default:
				//Log.d(LOG_TAG, "INTERCEPT default");
				mGestureDetector.onTouchEvent(event);
				return false;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		
		switch(action) {
			case MotionEvent.ACTION_CANCEL:
				//Log.d(LOG_TAG, "DISPATCHED ACTION_CANCEL");
				return true;
				
			case MotionEvent.ACTION_DOWN:
				//Log.d(LOG_TAG, "DISPATCHED ACTION_DOWN");
				return true;
	
			case MotionEvent.ACTION_MOVE:
				//Log.d(LOG_TAG, "DISPATCHED ACTION_MOVE");
				mGestureDetector.onTouchEvent(event);
				return true;
	
			case MotionEvent.ACTION_UP:
				//Log.d(LOG_TAG, "DISPATCHED ACTION_UP");
				// mGestureDetector.onTouchEvent(event);
				return true;
				
			default:
				//Log.d(LOG_TAG, "DISPATCHED default");
				mGestureDetector.onTouchEvent(event);
				return true;
		}
	}
	// impl methods about gesture & touch event end

    public void setInOutAnimation() {
        setInOutAnimation(true, DEFAULT_INTERPOLATER, 0);
    }

	private void setInOutAnimation(boolean gotoFuture, Interpolator interpolator, long duration) {
		// init params for switching animation
		float inFromXValue, inToXValue;
		float outFromXValue, outToXValue;
		float progress = 0;
		
		if(gotoFuture) {
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
		
		TranslateAnimation inAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, inFromXValue,
                Animation.RELATIVE_TO_SELF, inToXValue,
                Animation.ABSOLUTE, 0.0f,
                Animation.ABSOLUTE, 0.0f);

        TranslateAnimation outAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, outFromXValue,
                Animation.RELATIVE_TO_SELF, outToXValue,
                Animation.ABSOLUTE, 0.0f,
                Animation.ABSOLUTE, 0.0f);

        if (staticMove) {
            staticMove = false;
            duration = 0;
        }

		if(duration < 0) {
			duration = 0;
		}
        inAnimation.setDuration(duration);
        outAnimation.setDuration(duration);
        
        if(interpolator != null) {
            inAnimation.setInterpolator(interpolator);
            outAnimation.setInterpolator(interpolator);
        }
        inAnimation.setAnimationListener(new AnimationListener() {
        	@Override
        	public void onAnimationEnd(Animation animation) {
        		GNMonthView currentView = (GNMonthView) mViewSwitcher.getCurrentView();

        		Log.d("DEBUG", "onAnimationEnd() current view date is " + GNCalendarUtils.printDate(currentView.getSelectedDate()));
        		mParentFragment.onMonthViewSwitched(currentView.getSelectedDate());
        	}
        	
        	@Override
        	public void onAnimationRepeat(Animation animation) {
        		// do nothing
        	}
        	
        	@Override
        	public void onAnimationStart(Animation animation) {
                GNMonthView currentView = (GNMonthView) mViewSwitcher.getCurrentView();
                currentView.initMonthView();
        	}
        });
        
        mViewSwitcher.setInAnimation(inAnimation);
        mViewSwitcher.setOutAnimation(outAnimation);
	}
	
	private void handleHScroll(MotionEvent event) {
		mIsNewScrolling = false;
		mTouchMode = TOUCH_MODE_UNKNOWN;
		
		// get current time by CalendarController
		Time temp = getTimeOfController();

		// decide horizontal scroll direction
		boolean gotoNextMonth = (mSumScrollX > 0) ? true : false;
		setInOutAnimation(gotoNextMonth, DEFAULT_INTERPOLATER, DURATION_MONTH_SWITCHIGN);

		//Log.d("DEBUG", "BEFORE SWITCH: " + (temp.month + 1));
		if(gotoNextMonth) {
			temp.month += 1;
			// Statistics.onEvent(mContext, Statistics.MONTH_VIEW_FLING_TO_NEXT);

		} else {
			temp.month -= 1;
			// Statistics.onEvent(mContext, Statistics.MONTH_VIEW_FLING_TO_PREVIOUS);
		}
		temp.monthDay = 1;
		temp.normalize(true);
		
		// Gionee <jiangxiao> <2013-06-19> add for CRxxxxxxxx begin
	  	// use a unique Toast object to avoid multiple Toast popup
		if(temp.year < GNCalendarUtils.MIN_YEAR_NUM || temp.year > GNCalendarUtils.MAX_YEAR_NUM) {
			AllInOneActivity.showOutOfRangeToast(R.string.time_out_of_range);
			return;
		}
		// Gionee <jiangxiao> <2013-06-19> add for CRxxxxxxxx end
		Log.d("DEBUG", "handleHScroll() goto date " + GNCalendarUtils.printDate(temp));
		
		GNMonthView nextView = (GNMonthView)mViewSwitcher.getNextView();
		nextView.setParams(mContext, mViewSwitcher, mAdapter, mParentFragment, temp, mEventLoader, mMonthHeaderView);
		nextView.setPeriodInfos(mPeriodInfos);
		nextView.setRootSize(this.mRootWidth, this.mRootHeight);
		nextView.setWeekviewMode(mIsWeekviewMode);
		mViewSwitcher.showNext();
		
		// TODO: this method modified mSelectedDate of next view to previous month
		nextView.highlightToday();
	}

    private void handleHorizontalScroll() {
        mIsNewScrolling = false;
        mTouchMode = TOUCH_MODE_UNKNOWN;

        int selectJulianDay = 0;
        boolean leftScroll = (mSumScrollX > 0) ? true : false;

        if (leftScroll && mHighlightWeekIndex < 5) {
            selectJulianDay = mWeekViews[mHighlightWeekIndex + 1].getFirstJulianDay();
        } else if (leftScroll && mHighlightWeekIndex == 5) {
            int secondaryDayIndex = mWeekViews[mHighlightWeekIndex].getSecondaryDayIndex();
            selectJulianDay = mLastMonthJulianDay - secondaryDayIndex + 8;
        } else if (!leftScroll && mHighlightWeekIndex > 0) {
            selectJulianDay = mWeekViews[mHighlightWeekIndex - 1].getFirstJulianDay();
        } else if (!leftScroll && mHighlightWeekIndex == 0) {
            int secondaryDayIndex = mWeekViews[mHighlightWeekIndex].getSecondaryDayIndex();
            selectJulianDay = mFirstMonthJulianDay - secondaryDayIndex - 8;
        }

        Time selectTime = new Time();
        Utils.setJulianDayInGeneral(selectTime, selectJulianDay);

        if (selectJulianDay >= mFirstMonthJulianDay && selectJulianDay <= mLastMonthJulianDay) {
            final Time time = selectTime;
        	final MonthWeekEventsView currentWeekView = mWeekViews[mHighlightWeekIndex];
            ObjectAnimator outAnimator = ObjectAnimator.ofFloat(currentWeekView, "x", (leftScroll ? -mViewWidth : mViewWidth));
            outAnimator.setDuration(450);
            outAnimator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                    
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    gotoDate(time);
                    ObjectAnimator backAnimator = ObjectAnimator.ofFloat(currentWeekView, "x", 0);
                    backAnimator.setDuration(0);
                    backAnimator.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    
                }
            });

            MonthWeekEventsView nextWeekView = mWeekViews[mHighlightWeekIndex + (leftScroll ? 1 : -1)];

            ObjectAnimator xAnimator = ObjectAnimator.ofFloat(nextWeekView, "x", (leftScroll ? mViewWidth : -mViewHeight));
            xAnimator.setDuration(0);

            ObjectAnimator yAnimator = ObjectAnimator.ofFloat(nextWeekView, "y", 0);
            yAnimator.setDuration(0);

            AnimatorSet initAnimator = new AnimatorSet();
            initAnimator.play(xAnimator).after(yAnimator);

            ObjectAnimator inAnimator = ObjectAnimator.ofFloat(nextWeekView, "x", 0);
            if (leftScroll) {
            	inAnimator.setDuration(450);
            } else {
            	inAnimator.setDuration(400);
            }

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(initAnimator).before(inAnimator);
            animatorSet.play(inAnimator).with(outAnimator);
            animatorSet.start();
        } else {
            gotoDate(selectTime);
        }
    }

    private void handleVerticalScroll() {
        mIsNewScrolling = false;
        mTouchMode = TOUCH_MODE_UNKNOWN;

        if (isPeriod) return;

        if (mHighlightWeekIndex < 0) {
            highlightMonthDate(mSelectedDate);
            if (mHighlightWeekIndex < 0) mHighlightWeekIndex = 0;
        }

        boolean upScroll = (mSumScrollY > 0) ? true : false;
        if (upScroll && !mIsWeekviewMode) {
            mIsWeekviewMode = true;
            for (int i = 0; i < mActualWeekCount; i ++) {
                View weekView = mWeekViews[i];
                ObjectAnimator animator = ObjectAnimator.ofFloat(weekView, "y", (i - mHighlightWeekIndex) * mWeekViewHeight);
                animator.setDuration(mHighlightWeekIndex * DURATION_WEEK_SWITCH);
                animator.start();
            }

            ObjectAnimator animator = ObjectAnimator.ofFloat(mAgendaField, "y", mWeekViewHeight);
            animator.setDuration((mActualWeekCount - 1) * DURATION_WEEK_SWITCH);
            animator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                    // resetAgendaField();
                    hideHeaderView();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    hideHeaderView();
                    updateWeekViews();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    
                }
            });
            animator.start();
        } else if (!upScroll && mIsWeekviewMode) {
            mIsWeekviewMode = false;
            for (int i = 0; i < mActualWeekCount; i++) {
                View weekView = mWeekViews[i];
                ObjectAnimator animator = ObjectAnimator.ofFloat(weekView, "y", i * mWeekViewHeight);
                animator.setDuration(mHighlightWeekIndex * DURATION_WEEK_SWITCH);
                animator.setStartDelay((mActualWeekCount - mHighlightWeekIndex - 1) * DURATION_WEEK_SWITCH); 
                animator.start();
            }

            ObjectAnimator animator = ObjectAnimator.ofFloat(mAgendaField, "y", mActualWeekCount * mWeekViewHeight);
            animator.setDuration((mActualWeekCount - 1) * DURATION_WEEK_SWITCH);
            animator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                    updateWeekViews();
                    hideHeaderView();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    hideHeaderView();
                    // resetAgendaField();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    
                }
            });
            animator.start();
        }
    }

    private void resetAgendaField() {
        int showWeekCount = mIsWeekviewMode ? 1 : mActualWeekCount;
        int wantedHeight = (mRootHeight - mWeekViewHeight * showWeekCount - dp2px(BAR_HEIGHT));
        int agendaFieldWidth = getMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, mViewWidth);
        int agendaFieldHeight = getMeasureSpec(wantedHeight, 0);
        mAgendaField.measure(agendaFieldWidth, agendaFieldHeight);

        int left = 0;
        int right = mViewWidth;
        int top = mWeekViewHeight * mActualWeekCount;
        int bottom = mViewHeight +  mWeekViewHeight * (mIsWeekviewMode ? mActualWeekCount - 1 : 0);
        mAgendaField.layout(left, top, right, bottom);
    }

    public void setWeekviewMode(boolean weekviewMode) {
        mIsWeekviewMode = weekviewMode;
    }

    public boolean getWeekviewMode() {
        return mIsWeekviewMode;
    }

    private void initMonthView() {
        if (mHighlightWeekIndex < 0) {
            highlightMonthDate(mSelectedDate);
            if (mHighlightWeekIndex < 0) mHighlightWeekIndex = 0;
        }

        if (mIsWeekviewMode) {
            for (int i = 0; i < mActualWeekCount; i ++) {
                MonthWeekEventsView weekView = mWeekViews[i];
                ObjectAnimator animator = ObjectAnimator.ofFloat(weekView, "y", (i - mHighlightWeekIndex) * mWeekViewHeight);
                animator.setDuration(0);
                animator.start();
            }

            ObjectAnimator animator = ObjectAnimator.ofFloat(mAgendaField, "y", mWeekViewHeight);
            animator.setDuration(0);
            animator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                    // resetAgendaField();
                    hideHeaderView();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    hideHeaderView();
                    updateWeekViews();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    
                }
            });
            animator.start();
        } else {
            for (int i = 0; i < mActualWeekCount; i++) {
            	MonthWeekEventsView weekView = mWeekViews[i];
                ObjectAnimator animator = ObjectAnimator.ofFloat(weekView, "y", i * mWeekViewHeight);
                animator.setDuration(0);
                animator.start();
            }

            ObjectAnimator animator = ObjectAnimator.ofFloat(mAgendaField, "y", mActualWeekCount * mWeekViewHeight);
            animator.setDuration(0);
            animator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                    hideHeaderView();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    hideHeaderView();
                    // resetAgendaField();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    
                }
            });
            animator.start();
        }
    }

    private void updateWeekViews() {
    	for (int i = 0; i < mActualWeekCount; i++) {
    		MonthWeekEventsView weekView = mWeekViews[i];
            weekView.setWeekMode(mIsWeekviewMode);
            weekView.invalidate();
        }
    }

    public MonthWeekEventsView getFirstWeekView() {
        return mWeekViews == null ? null : mWeekViews[0];
    }

    public void onResume() {
        if (mAgendaList != null) mAgendaList.auroraOnResume();
    }

    public void onPause() {
        if (mAgendaList != null) mAgendaList.auroraOnPause();
    }

    public void hideHeaderView() {
        if (mAgendaList != null) mAgendaList.auroraHideHeaderView();
    }

	private boolean isSecondaryMonthDay(Time date) {
		if(date == null) {
			return false;
		}
		Log.d(LOG_TAG, "isSecondaryMonthDay: check " + GNCalendarUtils.printDate(date));
		
		int diff = (mBaseDate.month - date.month);
		if(Math.abs(diff) != 1) {
			Log.d(LOG_TAG, "isSecondaryMonthDay: INVALID diff " + diff);
			return false;
		}
		
		int julianDay = Utils.getJulianDayInGeneral(date, true);
		if(mWeekViews[mActualWeekCount - 1].containJulianDay(julianDay) 
				|| mWeekViews[0].containJulianDay(julianDay)) {
			Log.d(LOG_TAG, "isSecondaryMonthDay: true");
			return true;
		}
		
		return false;
	}
	
	private boolean switchMonth(Time targetDate) {
		int action = GNCalendarUtils.compareMonth(targetDate, mSelectedDate);

		boolean donotMove = (action == 0);
		if (donotMove) {
			// gionee <jiangxiao> <2013-08-13> add for CR00845772 begin
			if(!GNCalendarUtils.isYearInRange(targetDate)) {
				return false;
			}
			// gionee <jiangxiao> <2013-08-13> add for CR00845772 end
			// no switching
			// Log.d("DEBUG", "stay in current month, call highlightMonthDate() with " + GNCalendarUtils.printDate(targetDate));
			GNMonthView currView = (GNMonthView) mViewSwitcher.getCurrentView();
			currView.highlightMonthDate(targetDate);
		} else {
			// Gionee <jiangxiao> <2013-06-19> add for CRxxxxxxxx begin
		  	// use a unique Toast object to avoid multiple Toast popup
			// gionee <jiangxiao> <2013-08-13> add for CR00845772 begin
			if(!GNCalendarUtils.isYearInRange(targetDate)) {
			// gionee <jiangxiao> <2013-08-13> add for CR00845772 begin
				AllInOneActivity.showOutOfRangeToast(R.string.time_out_of_range);
				return false;
			}
			// Gionee <jiangxiao> <2013-06-19> add for CRxxxxxxxx begin
			boolean gotoFuture = (action > 0);
			// Log.d("DEBUG", "goto " + (gotoFuture ? "next month" : "previous month"));
			setInOutAnimation(gotoFuture, DEFAULT_INTERPOLATER, DURATION_MONTH_SWITCHIGN);
			GNMonthView nextView = (GNMonthView) mViewSwitcher.getNextView();
			// Log.d("DEBUG", "invoked GNMonthView.setParams() 4");
			nextView.setParams(mContext, mViewSwitcher, mAdapter, 
					mParentFragment, targetDate, mEventLoader, mMonthHeaderView);
			nextView.setPeriodInfos(mPeriodInfos);
			nextView.setRootSize(this.mRootWidth, this.mRootHeight);
			nextView.setWeekviewMode(mIsWeekviewMode);
			mViewSwitcher.showNext();
			// nextView.highlightToday();
			nextView.highlightMonthDate(targetDate);
			// Log.d("DEBUG", "showNext() has been invoked 2");
			// updateMonthHeader();
		}
		
		// update date which is displayed on action bar
		// switchMonth() is not the right owner to call updateActionBarTime(),
		// we should call this method after highlight month day
		// updateActionBarTime(targetDate);
		
		return !donotMove;
	}
	
	private void initMonthView(Context context) {
		mViewId = (++sViewId) & 0x1;

		mContext = context;
		mWeekViews = new MonthWeekEventsView[DEFAULT_WEEK_COUNT];
		setMonthHeaderNames();
		mSelectedDate = new Time();
        mLunarUtil = LunarUtil.getInstance(context);
        isChineseEnvironment = Utils.isChineseEnvironment();

        isPeriod = Utils.displayPeriod(context);
        defaultPeriodLastDays = Utils.getPeriodSharePreference(context, Utils.PERIOD_TIME, 5);
        defaultPeriodCycle = Utils.getPeriodSharePreference(context, Utils.PERIOD_CYCLE, 28);

		this.initDensity(context);
	}
	
	public void setMonthWeekAdapter(BaseAdapter adapter) {
		mAdapter = adapter;
		
		for(int i = 0; i < mActualWeekCount; ++i) {
			((MonthByWeekAdapter)mAdapter).sendEventsToView(mWeekViews[i]);
			mWeekViews[i].invalidate();
		}
	}

	private void updatePeriodInfos() {
		for (int i = 0; i < mActualWeekCount; i++) {
			mWeekViews[i].setPeriodInfos(mPeriodInfos);
		}
		if (mPeriodInfos != null && !mPeriodInfos.isEmpty()) {
    		setLastPeriodInfo(mPeriodInfos.get(0));
    	}
	}

	public void setPeriodInfos(ArrayList<PeriodInfo> periodInfos) {
		mPeriodInfos = periodInfos;
		for (int i = 0; i < mActualWeekCount; i++) {
			mWeekViews[i].setPeriodInfos(periodInfos);
		}
		if (periodInfos != null && !periodInfos.isEmpty()) {
    		setLastPeriodInfo(periodInfos.get(0));
    	}
	}

	private void setLastPeriodInfo(PeriodInfo info) {
    	if (info != null) {
    		lastPeriodStartDay = info.getStartDay();
    		lastPeriodFinishDay = info.getFinishDay();
    	}
    }

	public void setParams(Context context, ViewSwitcher switcher, BaseAdapter adapter, 
			MonthByWeekFragment fragment, Time selectedDay, EventLoader eventLoader,
			ViewGroup monthHeaderView) {
		// impl
		mContext = context;
		mViewSwitcher = switcher;
		mAdapter = adapter;
		mParentFragment = fragment;

		this.initRes();

		if(mGestureDetector == null) {
			mGestureDetector = new GestureDetector(
					mParentFragment.getActivity(),
					new MonthGestureDetector());
		}

        if (listGestureDetector == null) {
            listGestureDetector = new GestureDetector(mParentFragment.getActivity(), new ListGestureListenr());
        }

		mMonthHeaderView = monthHeaderView;

		// setSelectedDate(selectedDay, false);
//		mBaseDate.set(selectedDay);
//		mBaseDate.normalize(true);
		setBaseDate(selectedDay);
		mSelectedDate.set(selectedDay);
		Log.d("DEBUG", "update mSelectedDate as " + GNCalendarUtils.printTime(mSelectedDate));
		mSelectedDate.normalize(true);
		mSelectedJulianDay = Utils.getJulianDayInGeneral(mSelectedDate, true);

        // this.initLayout();
		if (isPeriod) {
			initPeriodLayout();
		} else {
			initLayout();
		}

		int weekNum = getWeekNumOfFirstMonthDay(mSelectedDate);
		// Log.d("DEBUG", "addWeekViewsByWeekNum() invoked by 1");
		addWeekViewsByWeekNum(weekNum);

		// set secondary day index after week views init
		setSecondaryDayIndex();

		setEventParams(mContext, eventLoader);

		setParamsConfigState(PARAMS_TYPE_CORE);

		// this.invalidate();

//		if(mStatistics == null) {
//			mStatistics = Statistics.getInstance();
//		}
        // Gionee <jiangxiao> <2013-0702> modify for CR00831637 begin
		this.resetHighlightIndex();
		this.resetPrevHighlightIndex();
		// Gionee <jiangxiao> <2013-0702> modify for CR00831637 end
	}

	private void gotoPeriodExplanation() {
		Intent intent = new Intent(mContext, AuroraPeriodExplanationActivity.class);
        mContext.startActivity(intent);
	}

	private void gotoCreateEvent() {
		ReportCommand command = new ReportCommand(mContext, ReportUtil.TAG_ADDEVENT);
        command.updateData();

        CalendarController controller = CalendarController.getInstance(mContext);

        Time t = new Time();
        t.set(controller.getTime());

        Time temp = new Time();
        temp.set(controller.getTime());

        Time now = new Time();
        now.setToNow();
        t.hour = now.hour;
        t.minute = now.minute;
        temp.hour = now.hour;
        temp.minute = now.minute;

        t.second = 0;
        if (t.minute > 30) {
            t.hour++;
            t.minute = 0;
        } else if (t.minute > 0 && t.minute < 30) {
            t.minute = 30;
        }

        controller.sendEventRelatedEvent(
                this, EventType.CREATE_EVENT, -1, t.toMillis(true), 0, 0, 0, temp.toMillis(true));
	}

	private void initPeriodLayout() {
		if (mAgendaField == null) {
			if (mLayoutInflater == null) {
				mLayoutInflater = (LayoutInflater) mParentFragment.getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}

			mAgendaField = (ViewGroup) mLayoutInflater.inflate(R.layout.aurora_month_period_field, null);
			mPeriodTitle = (TextView) mAgendaField.findViewById(R.id.period_title);
			mPeriodSummary = (TextView) mAgendaField.findViewById(R.id.period_summary);
			mPeriodStart = (Button) mAgendaField.findViewById(R.id.period_start);
			mPeriodFinish = (Button) mAgendaField.findViewById(R.id.period_finish);
			mPeriodExplanation = mAgendaField.findViewById(R.id.period_explanation);
			mPeriodExplanation.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					gotoPeriodExplanation();
				}
			});

			mLayoutInflater = null;
		}
	}

	private void initLayout() {
		if (mAgendaField == null) {
			if (mLayoutInflater == null) {
				mLayoutInflater = (LayoutInflater) mParentFragment.getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}

			// mAgendaField = (ViewGroup) mLayoutInflater.inflate(R.layout.gn_month_agenda_field, null, false);
			if (Utils.isIndiaVersion()) {
				mAgendaField = (ViewGroup) mLayoutInflater.inflate(R.layout.aurora_month_agenda_field_2, null, false);
			} else {
				mAgendaField = (ViewGroup) mLayoutInflater.inflate(R.layout.aurora_month_agenda_field, null, false);
			}
            mSolarDateView = (TextView) mAgendaField.findViewById(R.id.month_day_solar_date);
            mLunarDateView = (TextView) mAgendaField.findViewById(R.id.month_day_lunar_date);
            mNoAgendaPrompt = (TextView) mAgendaField.findViewById(R.id.no_agenda_prompt);

            if (!Utils.isIndiaVersion()) {
                mNoAgendaPrompt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gotoCreateEvent();
                    }
                });
            }

            mAgendaList = (AuroraListView) mAgendaField.findViewById(R.id.month_agenda_list);
            mAgendaList.setDivider(null);
            mAgendaList.auroraSetOnTouchEventSelf(true);
            mAgendaList.auroraSetHandleOnTouchEvnetSelfListener(this);

            mAddEventView = (View) mLayoutInflater.inflate(R.layout.aurora_month_add_event, null);
            mAddEventView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotoCreateEvent();
                }
            });

            mBirthdayReminderView = (View) mLayoutInflater.inflate(R.layout.aurora_month_birthday_reminder, null, false);
            mBirthdayReminderContentView = (TextView) mBirthdayReminderView.findViewById(R.id.birthday_reminder_content);
            mBirthdayReminderWishView = (ImageView) mBirthdayReminderView.findViewById(R.id.birthday_reminder_wish);
            mBirthdayReminderWishView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.aurora_happy_birthday));
                    intent.putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.aurora_happy_birthday));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.aurora_wish_birthday)));
                }
            });

			mLayoutInflater = null;
		}
	}

    public boolean auroraHandleOnTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        switch(action) {
            case MotionEvent.ACTION_CANCEL:
                return false;

            case MotionEvent.ACTION_DOWN:
                mIsNewScrolling = true;
                listGestureDetector.onTouchEvent(event);
                return false;

            case MotionEvent.ACTION_MOVE:
                return listGestureDetector.onTouchEvent(event);

            case MotionEvent.ACTION_UP:
                mIsNewScrolling = false;

                if (reachListTop && !singleToTop) {
                    singleToTop = true;
                } else {
                    singleToTop = false;
                }
                reachListTop = false;

                return false;

            default:
            	listGestureDetector.onTouchEvent(event);
                return false;
        }
    }

    private GestureDetector listGestureDetector = null;
    private boolean reachListTop = false;
    private boolean singleToTop = false;

    private class ListGestureListenr extends SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            mTouchMode = TOUCH_MODE_DOWN;
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return false;
        }

        @Override
		public void onShowPress(MotionEvent event) {
            
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float deltaX, float deltaY) {
            if (mIsNewScrolling) {
                mSumScrollX = 0;
                mSumScrollY = 0;
                mIsNewScrolling = false;
            }
            mSumScrollX += deltaX;
            mSumScrollY += deltaY;

            if (mTouchMode == TOUCH_MODE_DOWN) {
                float absSumScrollX = Math.abs(mSumScrollX);
                float absSumScrollY = Math.abs(mSumScrollY);

                if (absSumScrollX * SCROLL_SENSITIVITY < absSumScrollY && absSumScrollY > MIN_VSCROLL_DISTANCE * mScale) {
                    mTouchMode = TOUCH_MODE_VSCROLL;
                }
            } else if (mTouchMode == TOUCH_MODE_VSCROLL) {
                if (!mIsWeekviewMode && mSumScrollY > 0) {
                	handleVerticalScroll();
                } else if (mIsWeekviewMode && mSumScrollY < 0 &&
                		mAgendaList != null && mAgendaList.getFirstVisiblePosition() == 0) {
                	if (mAgendaList.getScrollY() < 0) {
                        reachListTop = true;
                    } else {
                        reachListTop = false;
                    }

                    if (mAgendaList.getCount() <= 6) {
                    	handleVerticalScroll();
                    } else if (reachListTop && singleToTop) {
                        handleVerticalScroll();
                    }
                }
            }

            return false;
        }
    }

	private boolean mResLoaded = false;
	private void initRes() {
		if(!mResLoaded) {
			Resources res = mParentFragment.getActivity().getResources();
	//		mSundayColor = res.getColor(R.color.gn_text_month_header_weekend);
	//		mSaturdayColor = res.getColor(R.color.gn_text_month_header_weekend);
			mColorDayNames = res.getColor(R.color.gn_text_month_header_default);
			mColorDayNamesWeekend = res.getColor(R.color.gn_text_month_header_weekend);
			mColorDayNamesToday = res.getColor(R.color.gn_text_month_header_today);
			mBgColorMonthHeader = res.getColor(R.color.gn_bg_month_header);
	//		mBgColorMonthView = res.getColor(R.color.gn_bg_month_view);
			
			mColorTest = res.getColor(R.color.gn_test);
			mColorTest2 = res.getColor(R.color.gn_test2);
			
			mWeekDayText = new String[DAYS_PER_WEEK];
			mWeekDayText[Time.SUNDAY] = res.getString(R.string.gn_day_Sunday);
			mWeekDayText[Time.MONDAY] = res.getString(R.string.gn_day_Monday);
			mWeekDayText[Time.TUESDAY] = res.getString(R.string.gn_day_Tuesday);
			mWeekDayText[Time.WEDNESDAY] = res.getString(R.string.gn_day_Wednesday);
			mWeekDayText[Time.THURSDAY] = res.getString(R.string.gn_day_Thursday);
			mWeekDayText[Time.FRIDAY] = res.getString(R.string.gn_day_Friday);
			mWeekDayText[Time.SATURDAY] = res.getString(R.string.gn_day_Saturday);
			mScale = res.getDisplayMetrics().density;
			
			// for event long click
			mLongPressMenuItems = new String[] {
				res.getString(R.string.gn_day_event_view),
				res.getString(R.string.gn_day_event_edit),
				res.getString(R.string.gn_day_event_del),
				res.getString(R.string.gn_day_event_share)
	        };
			
			mResLoaded = true;
		}
	}
	
	private void setBaseDate(Time date) {
		if(date == null) return;
		
		mBaseDate.set(date);
		mBaseDate.normalize(true);
		
		date.monthDay = 1;
		mFirstMonthJulianDay = Utils.getJulianDayInGeneral(date, true);
		
		date.month += 1;
		date.monthDay = 1;
		date.monthDay -= 1;
		date.normalize(true);
		mLastMonthJulianDay = Utils.getJulianDayInGeneral(date, true);
		
		date.set(mBaseDate);
		date.normalize(true);
		
		// should not call this method here, because mWeekViews init has not been finished
		// call this method after addWeekViewsByWeekNum()
		// setSecondaryDayIndex();
	}
	
	private void setSecondaryDayIndex() {
		Log.d(LOG_TAG, "setSecondaryDayIndex() has been invoked");
		if(mActualWeekCount > 0 && mFirstMonthJulianDay > 0 && mLastMonthJulianDay > 0) {
			int firstIndex = mWeekViews[0].getWeekDayIndexByJulianDay(mFirstMonthJulianDay);
			if(firstIndex > 0) {
				mLastSecondaryDayInFirstWeek = (firstIndex - 1);
				mWeekViews[0].setSecondaryIndex(mLastSecondaryDayInFirstWeek, false);
				if(mWeekViews[0].hasToday()) {
					this.mIsTodaySecondary = mWeekViews[0].isTodaySecondary();
				}
			} else {
				mLastSecondaryDayInFirstWeek = -1;
			}
			
			/*int lastIndex = mWeekViews[mActualWeekCount - 1].getWeekDayIndexByJulianDay(mLastMonthJulianDay);
			if(lastIndex < (DAYS_PER_WEEK - 1)) {
				mFirstSecondaryDayInLastWeek = (lastIndex + 1);
				mWeekViews[mActualWeekCount - 1].setSecondaryIndex(mFirstSecondaryDayInLastWeek, true);
				if(mWeekViews[mActualWeekCount - 1].hasToday()) {
					this.mIsTodaySecondary = mWeekViews[mActualWeekCount - 1].isTodaySecondary();
				}
			} else {
				mFirstSecondaryDayInLastWeek = -1;
			}*/
			int index = mWeekViews[4].getWeekDayIndexByJulianDay(mLastMonthJulianDay);
            if (index >= 0 && index < (DAYS_PER_WEEK - 1)) {
                mFirstSecondaryDayInLastWeek = index + 1;
                mWeekViews[4].setSecondaryIndex(mFirstSecondaryDayInLastWeek, true);
                if (mWeekViews[4].hasToday()) {
                    this.mIsTodaySecondary = mWeekViews[4].isTodaySecondary();
                }
            } else {
                mFirstSecondaryDayInLastWeek = -1;
                if (!mWeekViews[4].hasFocusMonthDay()) {
                    mWeekViews[4].setSecondaryIndex(0, true);
                    if (mWeekViews[4].hasToday()) {
                        this.mIsTodaySecondary = true;
                    }
                }
            }

            int lastIndex = mWeekViews[5].getWeekDayIndexByJulianDay(mLastMonthJulianDay);
            if (lastIndex >= 0 && lastIndex < (DAYS_PER_WEEK - 1)) {
                mFirstSecondaryDayInLastWeek = lastIndex + 1;
                mWeekViews[5].setSecondaryIndex(mFirstSecondaryDayInLastWeek, true);
                if (mWeekViews[5].hasToday()) {
                    this.mIsTodaySecondary = mWeekViews[5].isTodaySecondary();
                }
            } else {
                mFirstSecondaryDayInLastWeek = mWeekViews[5].getOffsetByJulianDay(mLastMonthJulianDay) + 1;
                mWeekViews[5].setSecondaryIndex(mFirstSecondaryDayInLastWeek, true);
                if (mWeekViews[5].hasToday()) {
                    this.mIsTodaySecondary = true;
                }
            }

			Log.d(LOG_TAG, "setSecondaryDayIndex() result: " + mLastSecondaryDayInFirstWeek + ", " + mFirstSecondaryDayInLastWeek);
		}
	}
	
	public void setSelectedDate(Time time, boolean viewChanged) {
		if(time == null) {
			return;
		}
		
		Log.d(LOG_TAG, "set mSelectedDate in view " + mViewId);
		mSelectedDate.set(time);
		mSelectedDate.normalize(true);
		
		mSelectedJulianDay = Utils.getJulianDayInGeneral(mSelectedDate, true);
		
		// Log.d("DEBUG", "invoke updateActionBarTime() 4, " + GNCalendarUtils.printDate(mSelectedDate));
		updateActionBarTime(mSelectedDate);
		
		if(viewChanged) {
			int weekNum = getWeekNumOfFirstMonthDay(mSelectedDate);
			// Log.d("DEBUG", "addWeekViewsByWeekNum() invoked by 3");
			addWeekViewsByWeekNum(weekNum);
		}
	}
	
	public void setSelectedDate(int julianDay, boolean viewChanged) {
		if(julianDay < 0) {
			throw new RuntimeException("invalid julian day: " + julianDay);
		}
		
		Log.d(LOG_TAG, "set mSelectedDate in view " + mViewId);
		mSelectedDate.setJulianDay(julianDay);
		mSelectedDate.normalize(true);
		
		mSelectedJulianDay = julianDay;
		
		if(viewChanged) {
			int weekNum = getWeekNumOfFirstMonthDay(mSelectedDate);
			// Log.d("DEBUG", "addWeekViewsByWeekNum() invoked by 2");
			addWeekViewsByWeekNum(weekNum);
		}
	}
	
	// set mDayLabels as locale week day 
	private void setMonthHeaderNames() {
		mDayNameLabels = new String[DAYS_PER_WEEK];
		int abbrev = DateUtils.LENGTH_MEDIUM;
//		if(mIsMiniMonth) {
//			abbrev = DateUtils.LENGTH_SHORTEST;
//		}
		
		for(int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; ++i) {
			mDayNameLabels[i - Calendar.SUNDAY] = 
					DateUtils.getDayOfWeekString(i, abbrev).toUpperCase();
		}
	}
    
    private void updateMonthHeader() {
    	mMonthHeaderView.setBackgroundColor(mBgColorMonthHeader);
    	
    	TextView label = (TextView) mMonthHeaderView.findViewById(R.id.wk_label);
    	
    	// In default, we don't display week number column
		label.setVisibility(View.GONE);
		
		CalendarController controller = CalendarController.getInstance(mContext);
		Time today = new Time();
		today.setToNow();
		today.normalize(true);
		int todayWeekDay = today.weekDay;
		
		int firstDayOfWeek = Utils.getFirstDayOfWeek(mContext);
		// TODO: set different first day to test this
		for(int i = 1; i < DAYS_PER_WEEK + 1; ++i) {
			int pos = (i - 1 + firstDayOfWeek) % DAYS_PER_WEEK;
			label = (TextView) mMonthHeaderView.getChildAt(i);
			// just use orange color in the month which contain today
			if(pos == todayWeekDay && ((GNMonthView)mViewSwitcher.getCurrentView()).hasToday()) {
				label.setTextColor(mColorDayNamesToday);
			} else {
				if(pos == Time.SUNDAY || pos == Time.SATURDAY) {
					label.setTextColor(mColorDayNamesWeekend);
				} else {
					label.setTextColor(mColorDayNames);
				}
			}
			label.setText(mDayNameLabels[pos]);
            label.setVisibility(View.VISIBLE);
			label.setVisibility(View.VISIBLE);
		}
    	
    	mMonthHeaderView.invalidate();
    }
	
	public void setSelectedDay(Time selectedDay) {
		// Log.d("DEBUG", "MonthView.setSelectedDay() has been invoked");
		if(mSelectedDate == null) {
			mSelectedDate = new Time();
		}
		mSelectedDate.set(selectedDay);
		mSelectedDate.normalize(true);
	}
	
	public Time getSelectedDate() {
		return mSelectedDate;
	}
	
	private int getWeekNumOfFirstMonthDay(Time date) {
		int firstDayOfWeek = Utils.getFirstDayOfWeek(mContext);
		int weekNum = GNCalendarUtils.getWeekNumOfFirstMonthDay(date, firstDayOfWeek);
		
		return weekNum;
	}

	// TODO: investigate the purpose of this method
//	@Override
//	protected void dispatchDraw(Canvas canvas) {
//		canvas.setDrawFilter(new PaintFlagsDrawFilter
//				(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
//		
//		super.dispatchDraw(canvas);
//		Rect dstRect = new Rect(0, 0, 0, mViewWidth);
//		Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.gn_date_text_light);
//		Paint paint = new Paint();
//		paint.setColor(Color.BLACK);
//		paint.setTextAlign(Paint.Align.CENTER);
//		paint.setTextSize((int)(16 * mScale));
//		canvas.drawBitmap(bitmap, null, dstRect, paint);
//	}
	
	public void addWeekViewsByWeekNum(int weekNum) {
		// Log.d("DEBUG", "GNMonthView.addWeekViewsByWeekNum() has been invoked in view " + this.mViewId);
		this.removeAllViews();
		mActualWeekCount = DEFAULT_WEEK_COUNT;
		
		// don't add this one, or a duplicated header will appear
		// this.addView(mDayNamesHeader);
		
		// before call getView(), we should update mFocusMonth of Adapter,
		// or the date next/previous month won't be drawn
		
		Time currMonth = GNCalendarUtils.getJulianMondayTimeFromWeekNum(weekNum + 1);
		((MonthByWeekAdapter) mAdapter).updateFocusMonth(currMonth.month);
		boolean foundTodayView = false;
		for(int i = 0; i < mWeekViews.length; ++i) {
			// TODO: sometime mAdapter.getView() may return SimpleWeekView
			mWeekViews[i] = (MonthWeekEventsView) mAdapter.getView(weekNum + i, null, this);
			
			/*boolean isFebruray = (currMonth.month == GNCalendarUtils.MonthName.FEBRURAY);
			int lastWeekIndex = (mWeekViews.length - 1);
			if(isFebruray) {
				// maybe February just contains 4 weeks, so we should
				// also check 5th week firstly
				lastWeekIndex -= 1;
			}
			
			if(i < lastWeekIndex || mWeekViews[i].hasFocusMonthDay()) {
				this.addView(mWeekViews[i]);
				if(mWeekViews[i].hasToday()) {
					mHasToday = true;
					mTodayJulianDay = mWeekViews[i].getTodayJulianDay();
					// can not set mIsTodaySecondary here, because init not finished
					// mIsTodaySecondary = mWeekViews[i].isTodaySecondary();
					// Log.d("DEBUG", "set mIsTodaySecondary as " + mIsTodaySecondary);
					
					foundTodayView = true;
				}
				
				mWeekViews[i].setMonthWeekViewIndex(i);
			} else {
				mActualWeekCount -= 1;
			}*/

            this.addView(mWeekViews[i]);
            if (mWeekViews[i].hasToday()) {
                mHasToday = true;
                mTodayJulianDay = mWeekViews[i].getTodayJulianDay();
                foundTodayView = true;
            }

			mWeekViews[i].setMonthWeekViewIndex(i);
		} // end of for loop
		if(!foundTodayView) {
			mHasToday = false;
			mTodayJulianDay = -1;
		}
		
		Log.d("AgendaField", "add mAgendaField into ViewGroup");
		this.addView(mAgendaField);
	}
	
	private int getMeasureSpec(int sizeWanted, int sizeMeasured) {
		int result = -1;
		if(sizeWanted > 0) {
			result = MeasureSpec.makeMeasureSpec(sizeWanted, MeasureSpec.EXACTLY);
		} else if(sizeWanted == ViewGroup.LayoutParams.MATCH_PARENT) {
			result = MeasureSpec.makeMeasureSpec(sizeMeasured, MeasureSpec.EXACTLY);
		} else if(sizeWanted == ViewGroup.LayoutParams.WRAP_CONTENT) {
			result = MeasureSpec.makeMeasureSpec(sizeMeasured, MeasureSpec.AT_MOST);
		}
		
		return result;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredWidth = this.getMeasuredWidth();
		int measuredHeight = this.getMeasuredHeight();
		if (mRootView != null) {
			this.setRootSize(mRootView.getMeasuredWidth(), mRootView.getMeasuredHeight());
		}

		int count = this.getChildCount();
		if(count > mActualWeekCount) {
			count = mActualWeekCount;
		}

		for (int i = 0; i < count; ++i) {
			View child = this.getChildAt(i);
			ViewGroup.LayoutParams params = child.getLayoutParams();
			int childWidth = getMeasureSpec(params.width, measuredWidth);
			int childHeight = getMeasureSpec(params.height, measuredHeight);
			child.measure(childWidth, childHeight);
		}

		if (count - mActualWeekCount < 0) {
			// throw new RuntimeException("last view is not mAgendaField");
		} else {
			int agendaFieldWidth = -1;
			int agendaFieldHeight = -1;
			
			View weekView = this.getChildAt(0);
			if (weekView != null) {
				agendaFieldWidth = getMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, mViewWidth);

				// int wantedHeight = (mRootHeight - weekView.getMeasuredHeight() * mActualWeekCount - dp2px(BAR_HEIGHT));
                // int wantedHeight = (mRootHeight - weekView.getMeasuredHeight() - dp2px(BAR_HEIGHT));
				int wantedHeight = 0;
				if (isPeriod) {
					wantedHeight = (mRootHeight - weekView.getMeasuredHeight() * mActualWeekCount - dp2px(BAR_HEIGHT));
				} else {
					wantedHeight = (mRootHeight - weekView.getMeasuredHeight() - dp2px(BAR_HEIGHT));
				}

				agendaFieldHeight = getMeasureSpec(wantedHeight, 0);

				mAgendaField.measure(agendaFieldWidth, agendaFieldHeight);
			}
		}

		int wantedTotalHeight = heightMeasureSpec;
		if (count > 0) {
			// GNMonthView currentView = (GNMonthView) this.mViewSwitcher.getCurrentView();
			wantedTotalHeight = count * this.getChildAt(0).getMeasuredHeight();
			wantedTotalHeight += mAgendaField.getMeasuredHeight();
		}

		this.setMeasuredDimension(widthMeasureSpec, wantedTotalHeight);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// Log.d(LOG_TAG, "onSizeChanged: " + oldh + " -> " + h);
		mViewWidth = w;
		mViewHeight = h;
		
		// updateDayNamesHeader();
//		if(mIsFirstLoaded) {
//			mIsFirstLoaded = false;
//			
//			Time selectedDay = ((MonthByWeekAdapter)mAdapter).getSelectedDay();
//			int weekNum = getWeekNumOfFirstMonthDay(selectedDay);
//			
//			GNMonthView currView = (GNMonthView)mViewSwitcher.getCurrentView();
//			
//            // should not call setSelectedDay() method here, because this
//            // will cause an error that no switching animation when we press
//            // back to today button
//			// currView.setSelectedDay(selectedDay);
//			// unnecessarily call
//			// currView.addViewsByWeekNum(weekNum);
//			// currView.measure(getWidth(), getHeight());
//            // currView.layout(getLeft(), getTop(), getRight(), getBottom());
//		}
	}
	
	// must re-layout the child views, or all week views will be
	// displayed on a single line
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int baseLeft = 0;
		int baseTop = 0;
		
		View child = null;
//		boolean layoutAgendaList = false;
//		if(count - mActualWeekCount == 1) {
//			count -= 1;
//			layoutAgendaList = true;
//		}
//		Log.d(LOG_TAG, "layout week views count: " + count);
		int count = this.getChildCount();
		if(count > mActualWeekCount) {
			count = mActualWeekCount;
		}
		for(int i = 0; i < count; i++) {
			child = this.getChildAt(i);
			// child.measure(r - l, b - t);

			int left   = baseLeft;
			int right  = baseLeft + mViewWidth;
			int top    = baseTop + child.getMeasuredHeight() * (i);
			int bottom = baseTop + child.getMeasuredHeight() * (i + 1);
			child.layout(left, top, right, bottom);
		}
		
		View firstChild = this.getChildAt(0);
		mWeekViewWidth = firstChild.getMeasuredWidth();
		mWeekViewHeight = firstChild.getMeasuredHeight();
		
		setWeekViewLT();
		
//		if(layoutAgendaList) {
//			int left = baseLeft;
//			int right = baseLeft + mViewWidth;
//			int top = baseTop + mWeekViewHeight * mActualWeekCount;
//			int bottom = mViewHeight;
//			View agendaList = this.getChildAt(mActualWeekCount);
//			// agendaList.measure(right - left, bottom - top);
//			//Log.d("DEBUG", "measured agenda list " + agendaList.getMeasuredWidth() + ", " + agendaList.getMeasuredHeight());
//			//Log.d("DEBUG", "to layout agenda list at " + top + "," + bottom);
//			//Log.d("DEBUG", "to layout agenda list at " + left + "," + right);
//			agendaList.layout(left, top, right, bottom);
//		}
		// layout agenda field
		if (this.mAgendaField != null) {
			int left = baseLeft;
			int right = baseLeft + mViewWidth;
			int top = baseTop + mWeekViewHeight * mActualWeekCount;
			int bottom = mViewHeight;
			Log.d("AgendaField", "layout mAgendaField top/bottom " + top + "/" + bottom);
			
			mAgendaField.layout(left, top, right, bottom);
		}

		if (isPeriod && layoutAgain) {
			layoutAgain = false;
			resetAgendaField();
		}
	} // end of onLayout()
	
	private void setSelectedDateIndex(float x, float y) {
		int totalHeight = mWeekViewHeight * mActualWeekCount;
		if (y < mWeekViewTop || y > totalHeight) {
			Log.d(LOG_TAG, "y is out of range");
			return;
		}
		
		mHighlightWeekIndex = (int)((y - mWeekViewTop)/ mWeekViewHeight);
		
		if (x < mWeekViewLeft || x > (mViewWidth - mViewPadding)) {
			Log.d(LOG_TAG, "x is out of range");
			return;
		}
		
		mHighlightWeekDayIndex = (int)((x - mWeekViewLeft) / (mViewWidth - mWeekViewLeft - mViewPadding) * DAYS_PER_WEEK);
		Log.d("DEBUG", "set selected index as " + mHighlightWeekIndex + ", " + mHighlightWeekDayIndex);
	}
	
	private void setWeekViewLT() {
		mWeekViewLeft = mViewPadding;
		if(mShowWeekNum) {
			mWeekViewLeft += ((float)(mViewWidth - mViewPadding * 2) / (DAYS_PER_WEEK + 1));
		}
		
		mWeekViewTop = 0;
	}
	
	private static final int ACTION_STAY = 0;
	private static final int ACTION_NEXT = 1;
	private static final int ACTION_PREVIOUS = -1;
	
	// methods for highlight selected date begin
	private void highlightSelectedDate() {
		if(mHighlightWeekIndex >= 0 && mHighlightWeekIndex < mActualWeekCount) {
			mTappedView = mWeekViews[mHighlightWeekIndex];
			// set selected day
			// should do highlight according to index, but not X coordinate
			// mSelectedDate.setJulianDay(mTappedView.setAndReturnClickedDay(mFirstTouchedX));
			
			// TODO: to check whether the tapped date is secondary date, if so switch to
			// the corresponding month
			int action = ACTION_STAY;
			int secondaryDateIndex = -1;
			// if(mHighlightWeekIndex == 0 || mHighlightWeekIndex == mActualWeekCount - 1) { // if 1
			if (mHighlightWeekIndex == 0 || mHighlightWeekIndex == 4) { 
				// may switch to previous month
				secondaryDateIndex = mTappedView.getSecondaryDayIndex();
				if(secondaryDateIndex >= 0) { // if 2
					// Log.d("DEBUG", "tapped secondary date");
					// Log.d("DEBUG", "S INDEX: " + secondaryDateIndex + ", S DIRECTION: " + (mTappedView.getSecondaryDayDirection() ? 1 : -1) + ", T INDEX: " + mHighlightWeekDayIndex);
					if(mTappedView.getSecondaryDayDirection()) {
						if(mHighlightWeekDayIndex >= secondaryDateIndex) {
							action = ACTION_NEXT;
						}
					} else {
						if(mHighlightWeekDayIndex <= secondaryDateIndex) {
							action = ACTION_PREVIOUS;
						}
					}
				} // end if 2
			} // end if 1

            if (mHighlightWeekIndex == 5) {
            	secondaryDateIndex = mTappedView.getSecondaryDayIndex();
                if (mHighlightWeekDayIndex >= secondaryDateIndex) {
                    action = ACTION_NEXT;
                }
            }

			if(action == ACTION_STAY) {
				if(mHighlightWeekIndex == mPrevHighlightWeekIndex
						&& mHighlightWeekDayIndex == mPrevHighlightWeekDayIndex) {
					Log.d("DEBUG", "tapped the same date, return");
					return;
				}
				
				int julianDay = mTappedView.setAndReturnClickedDay(mHighlightWeekDayIndex);
				Utils.setJulianDayInGeneral(mSelectedDate, julianDay);
				// to update title
				// Log.d("DEBUG", "invoke updateActionBarTime() 2, " + GNCalendarUtils.printDate(mSelectedDate));
				Log.d("DEBUG", "1 - update time " + GNCalendarUtils.printDate(mSelectedDate));
				updateActionBarTime(mSelectedDate);

				setTimeOfController(mSelectedDate.toMillis(false));

				Log.d("DEBUG", "to invoke loadEventsOfSelectedDay()");
				loadEventsOfSelectedDay();

                if (mIsWeekviewMode) {
                    initMonthView();
                }

				// clear highlight effect of other week views
				// Log.d("DEBUG", "highlightSelectedDate() to clear un-selected week view, except " + mHighlightWeekIndex);
				for(int i = 0; i < mActualWeekCount; ++i) {
					if(i != mHighlightWeekIndex) {
						mWeekViews[i].clearClickedDay();
					}
				}
				// Statistics.onEvent(mContext, Statistics.MONTH_DAY_TAPPED_CURRENT);

			} else {
				boolean gotoNextMonth = (action == ACTION_NEXT);
				Time targetDate = new Time();
				targetDate.set(mBaseDate);
				// Log.d("DEBUG", "Switch to " + (gotoNextMonth ? "next" : "previous") + " month");
//				Log.d("DEBUG", "mHighlightWeekDayIndex = " + mHighlightWeekDayIndex);
//				Log.d("DEBUG", "secondaryDateIndex = " + secondaryDateIndex);
				int dayOffset = (Math.abs(mHighlightWeekDayIndex - secondaryDateIndex) + 1);
				if(gotoNextMonth) {
					targetDate.month += 1;
					targetDate.monthDay = dayOffset;
					// Statistics.onEvent(mContext, Statistics.MONTH_DAY_TAPPED_NEXT);

				} else {
					// targetDate.month -= 1;
					targetDate.monthDay = 1;
					targetDate.monthDay -= dayOffset;
					// Statistics.onEvent(mContext, Statistics.MONTH_DAY_TAPPED_PREVIOUS);
				}
				targetDate.normalize(true);
				Log.d("DEBUG", "switch secondary month: " + GNCalendarUtils.printDate(targetDate));

                if (targetDate.year < GNCalendarUtils.MIN_YEAR_NUM || targetDate.year > GNCalendarUtils.MAX_YEAR_NUM) {
		            AllInOneActivity.showOutOfRangeToast(R.string.time_out_of_range);
		            mHighlightWeekIndex = mPrevHighlightWeekIndex;
		            mHighlightWeekDayIndex = mPrevHighlightWeekIndex;
		            return;
		        }

                if (mIsWeekviewMode) {
                    staticMove = true;
                }

				switchMonth(targetDate);
				// Log.d("DEBUG", "invoke updateActionBarTime() 3, " + GNCalendarUtils.printDate(targetDate));
				updateActionBarTime(targetDate);
			}
		}
	} // end of highlightSelectedDate()
	
	// select the date tapped
	private void selectMonthDay(float x, float y) {
        if (mIsWeekviewMode) {
            if (y <= mWeekViewHeight) {
                y += mHighlightWeekIndex * mWeekViewHeight;
            } else {
                return;
            }
        } else {
            if (y > mWeekViewHeight * mActualWeekCount) {
                return;
            }
        }

		mFirstTouchedX = x;
		mFirstTouchedY = y;
		
		// save prev highlight index
		Log.d("DEBUG", "save selected index: " + mPrevHighlightWeekIndex + ", " + mPrevHighlightWeekDayIndex);
		mPrevHighlightWeekIndex = mHighlightWeekIndex;
		mPrevHighlightWeekDayIndex = mHighlightWeekDayIndex;
		
		// reset the following fields, or click other place
		// will also trigger highlight selected
		mHighlightWeekIndex = -1;
		mHighlightWeekDayIndex = -1;
		setSelectedDateIndex(mFirstTouchedX, mFirstTouchedY);
		highlightSelectedDate();
	} // end of selectMonthDay()
	
	protected void highlightMonthDate(Time targetDate) {
		Log.d(LOG_TAG, "highlightMonthDate() has been invoked in view " + mViewId);
		
		int julianDay = Utils.getJulianDayInGeneral(targetDate, true);
		
		boolean foundHighlightGrid = false;
		for(int i = 0; i < mActualWeekCount; ++i) {
			int index = mWeekViews[i].getWeekDayIndexByJulianDay(julianDay);
			if (index >= 0) {
			    // Gionee <jiangxiao> <2013-0702> modify for CR00831637 begin
				// highlight date has been found
				// mHighlightWeekIndex = i;
				// mHighlightWeekDayIndex = index;
				// mPrevHighlightWeekIndex = mHighlightWeekIndex;
				// mPrevHighlightWeekDayIndex = mHighlightWeekDayIndex;
				// use set method to replace assignment
				this.setSelectedWeekIndex(i);
				this.setSelectedWeekDayIndex(index);
				// Gionee <jiangxiao> <2013-0702> modify for CR00831637 end

				foundHighlightGrid = true;
				highlightSelectedDate();
				break;
			}
		} // end for loop
		if(!foundHighlightGrid) {
			mHighlightWeekIndex = -1;
			mHighlightWeekDayIndex = -1;
		}
		Log.d(LOG_TAG, "DEBUG: not found grid to highlight");
	} // end of highlightMonthDate()
	
	private boolean mHighlightGivenDay = false;
	public void setHighlightFlag(boolean flag) {
		mHighlightGivenDay = flag;
	}
	
	public void highlightToday() {
		Log.d("DEBUG", "highlightToday() mHighlightGivenDay = " + mHighlightGivenDay);
		if(!mHighlightGivenDay) {
			if(mHasToday && !mIsTodaySecondary) {
				// highlight today
				Log.d("DEBUG", "highlightToday() 1");
				Utils.setJulianDayInGeneral(mSelectedDate, mTodayJulianDay);
			} else {
				Log.d("DEBUG", "highlightToday() 2");
				// highlight 1st date of current month
				mSelectedDate.monthDay = 1;
			}
		}
		mHighlightGivenDay = false;
		
		Log.d("DEBUG", "to highlight selected day: " + GNCalendarUtils.printDate(mSelectedDate));
		highlightMonthDate(mSelectedDate);
	}
	
	// methods for highlight selected date end
	
	// methods for events loading begin
//	public void setEventParams(Context context, EventLoader eventLoader) {
//		mEventLoader = eventLoader;
//		mSelectedDateAgendaListView = new ListView(context);
//		mSelectedDateAgendaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				Event event = mEvents.get(position);
//				sendEventForAgendaListItem(EventType.VIEW_EVENT, event);
//			}
//		});
//		
//		Resources res = mContext.getResources();
//		mLongPressMenuItems = new String[] {
//			res.getString(R.string.gn_day_event_view),
//			res.getString(R.string.gn_day_event_edit),
//			res.getString(R.string.gn_day_event_del),
//			res.getString(R.string.gn_day_event_share)
//        };
//		
//		mSelectedDateAgendaListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//			private final int OPERATION_VIEW_EVENT = 0;
//			private final int OPERATION_EDIT_EVENT = 1;
//			private final int OPERATION_DELETE_EVENT = 2;
//			private final int OPERATION_SHARE_EVENT = 3;
//			@Override
//			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//				String dialogTitle = "just a test";
//				final Event event = mEvents.get(position);
//				new AuroraAlertDialog.Builder(mContext).setTitle(dialogTitle)
//					.setItems(mLongPressMenuItems, new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							CalendarController controller = CalendarController.getInstance(mContext);
//							Time now = new Time();
//							now.setToNow();
//							switch(which) {
//								case OPERATION_VIEW_EVENT:
//									sendEventForAgendaListItem(EventType.VIEW_EVENT, event);
//									break;
//								case OPERATION_EDIT_EVENT:
//									sendEventForAgendaListItem(EventType.EDIT_EVENT, event);
//									break;
//								case OPERATION_DELETE_EVENT:
//									sendEventForAgendaListItem(EventType.DELETE_EVENT, event);
//									break;
//								case OPERATION_SHARE_EVENT:
//									DayUtils.sendShareEvent(mContext, event.id);
//									break;
//								default:
//									break;
//							}
//						}
//					}).show();
//				
//				return true;
//			} // end of onItemLongClick()
//		});
//		
//		setParamsConfigState(PARAMS_TYPE_EVENTS);
//	}

	public void setEventParams(Context context, EventLoader eventLoader) {
		mEventLoader = eventLoader;

		if (mAgendaList == null) return;

		if (mAgendaList.getOnItemClickListener() == null) {
			mAgendaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					/*if(position >= mEvents.size()) return;
					
					Event event = mEvents.get(position);*/

				int headersCount = 0;
				if (Utils.isIndiaVersion()) {
                    if (!mBirthdayReminderEvents.isEmpty()) {
                        headersCount += 1;
                        if (position == headersCount - 1) {
                            Event event = mBirthdayReminderEvents.get(0);
                            sendEventForAgendaListItem(EventType.VIEW_BIRTHDAY, event);
                            return;
                        }
                    }
				} else {
                    headersCount = 1 + (mRememberDays == null ? 0 : mRememberDays.size()) + 
                    		(mBirthdayReminderEvents.isEmpty() ? 0 : 1);

                    if (!mBirthdayReminderEvents.isEmpty() && position == headersCount - 1) {
                        Event event = mBirthdayReminderEvents.get(0);
                        sendEventForAgendaListItem(EventType.VIEW_BIRTHDAY, event);
                        return;
                    }

                    if (mRememberDays != null && !mRememberDays.isEmpty() && position > 0 && position < headersCount) {
                    	int rememberDayId = mRememberDays.get(position - 1).getId();

                    	Intent intent = new Intent(mContext, WXEntryActivity.class);
                    	intent.putExtra("new", false);
                    	intent.putExtra("index", -1);
						intent.putExtra("id", mRememberDays.get(position - 1).getMillTime());
                    	mParentFragment.startActivity(intent);
                    	return;
                    }
				}

                    if (position < headersCount || position >= mAdapterEvents.size() + headersCount) return;

                    Event event = mAdapterEvents.get(position - headersCount);
					// sendEventForAgendaListItem(EventType.VIEW_EVENT, event);
                    if (Utils.NOTE_REMINDER_ACCOUNT_NAME.equals(event.ownerAccount)) {
                        Uri uri = Uri.parse("openewnote://com.aurora.note.newnoteactivity");

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        intent.putExtra("note_type", 1);
                        intent.putExtra("note_id", Integer.parseInt(event.location.toString()));
                        mContext.startActivity(intent);
                    } else {
                        sendEventForAgendaListItem(EventType.VIEW_EVENT, event);
                    }
					// Statistics.onEvent(mContext, Statistics.MONTH_AGENDA_LIST_CLICK);
				}
			});
		}

        mAgendaList.auroraSetNeedSlideDelete(true);
        mAgendaList.auroraSetAuroraBackOnClickListener(new AuroraListView.AuroraBackOnClickListener() {
            public void auroraOnClick(int position) {
                int headersCount = 0;
                if (Utils.isIndiaVersion()) {
                    if (!mBirthdayReminderEvents.isEmpty()) {
                        headersCount += 1;
                    }
                } else {
                    headersCount = 1 + (mRememberDays == null ? 0 : mRememberDays.size()) + 
                            (mBirthdayReminderEvents.isEmpty() ? 0 : 1);
                }

                if (position < headersCount || position >= mAdapterEvents.size() + headersCount) return;

                Event event = mAdapterEvents.get(position - headersCount);
                sendEventForAgendaListItem(EventType.DELETE_EVENT, event);
                mParentFragment.setCanEventChange();
                // Statistics.onEvent(mContext, Statistics.MONTH_AGENDA_LIST_LONG_CLICK_DELETE);
            }

            public void auroraPrepareDraged(int positon) {
            
            }

            public void auroraDragedSuccess(int position) {
            
            }

            public void auroraDragedUnSuccess(int position) {
            
            }
        });

		// init the following string res in initRes()
//		Resources res = mContext.getResources();
//		mLongPressMenuItems = new String[] {
//			res.getString(R.string.gn_day_event_view),
//			res.getString(R.string.gn_day_event_edit),
//			res.getString(R.string.gn_day_event_del),
//			res.getString(R.string.gn_day_event_share)
//        };
		
		/*if(mAgendaList.getOnItemLongClickListener() == null) {
			mAgendaList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				private final int OPERATION_VIEW_EVENT = 0;
				private final int OPERATION_EDIT_EVENT = 1;
				private final int OPERATION_DELETE_EVENT = 2;
				private final int OPERATION_SHARE_EVENT = 3;
				
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					if(position >= mEvents.size()) {
						return false;
					}
					Statistics.onEvent(mContext, Statistics.MONTH_AGENDA_LIST_LONG_CLICK);
					
					final Event event = mEvents.get(position);
					
					Time eventTime = new Time();
					eventTime.set(event.startMillis);
					eventTime.normalize(true);
					String dialogTitle = mWeekDayText[eventTime.weekDay];
					
					new AuroraAlertDialog.Builder(mContext).setTitle(dialogTitle)
						.setItems(mLongPressMenuItems, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								CalendarController controller = CalendarController.getInstance(mContext);
								Time now = new Time();
								now.setToNow();
								switch(which) {
									case OPERATION_VIEW_EVENT:
										sendEventForAgendaListItem(EventType.VIEW_EVENT, event);
										Statistics.onEvent(mContext, Statistics.MONTH_AGENDA_LIST_LONG_CLICK_VIEW);
										break;
									case OPERATION_EDIT_EVENT:
										sendEventForAgendaListItem(EventType.EDIT_EVENT, event);
										Statistics.onEvent(mContext, Statistics.MONTH_AGENDA_LIST_LONG_CLICK_EDIT);
										break;
									case OPERATION_DELETE_EVENT:
										sendEventForAgendaListItem(EventType.DELETE_EVENT, event);
										Statistics.onEvent(mContext, Statistics.MONTH_AGENDA_LIST_LONG_CLICK_DELETE);
										break;
									case OPERATION_SHARE_EVENT:
										DayUtils.sendShareEvent(mContext, event.id);
										Statistics.onEvent(mContext, Statistics.MONTH_AGENDA_LIST_LONG_CLICK_SHARE);
										break;
									default:
										break;
								}
							}
						}).show();
					
					return true;
				} // end of onItemLongClick()
			});
		}*/
	} // end of setEventParams()

	private void sendEventForAgendaListItem(long eventType, Event event) {
		CalendarController controller = CalendarController.getInstance(mContext);
		Time now = new Time();
		// Gionee <jiangxiao> <2013-06-20> add for CR00828108 begin
		// now.setToNow();
		now.set(controller.getTime());
		now.normalize(true);
		// Gionee <jiangxiao> <2013-06-20> add for CR00828108 end
		
		controller.sendEventRelatedEventWithExtra(
				mContext,
				eventType, 
				event.id, 
				event.startMillis,
				event.endMillis, 
				0, 0, 
				CalendarController.EventInfo.buildViewExtraLong(
					Attendees.ATTENDEE_STATUS_NONE, event.allDay), 
				now.toMillis(true));
	} // end of sendEventForAgendaListItem()
	
	public void loadEventsOfSelectedDay() {
		if (mEventLoader == null) {
			// throw new RuntimeException("Can not query Event table, because mEventLoader is null!");
			Log.d("DEBUG", "GNMonthView.loadEventsOfSelectedDay() Can not query Event table, because mEventLoader is null!");
			return;
		}

        mSelectedDate = getTimeOfController();

		int selectedJulianDay = Utils.getJulianDayInGeneral(mSelectedDate, true);
		Log.d("DEBUG", "load event date " + GNCalendarUtils.printDate(mSelectedDate));
		if(mEvents == null) {
			mEvents = new ArrayList<Event>();
		} else {
			mEvents.clear();
		}

        // mEventLoader.loadEventsInBackground(1, mEvents, selectedJulianDay, 
		// 		mEventsLoadingFinishedCallback, mEventsLoadingCanceledCallback);

        if (isPeriod) {
        	updatePeriodStartFinish(selectedJulianDay);
        } else {
        	mEventLoader.loadEventsInBackground(1, mEvents, selectedJulianDay, 
                    mEventsLoadingFinishedCallback, mEventsLoadingCanceledCallback);
        }
	}
	
	// methods for events loading end
	
	// methods for get root view measurement begin
	private View mRootView = null;
	
	public void setRootView(View root) {
		mRootView = root;
	}
	// methods for get root view measurement end

	// methods to response "go back to today" button
	public void gotoDate(Time targetDate) {
        if (targetDate.year < GNCalendarUtils.MIN_YEAR_NUM || targetDate.year > GNCalendarUtils.MAX_YEAR_NUM) {
            AllInOneActivity.showOutOfRangeToast(R.string.time_out_of_range);
            return;
        }

		// Log.d("AgendaField", "GNMonthView.gotoToday() has been invoked in view " + mViewId);
		// switch month to today's month
		switchMonth(targetDate);
		
		// no need to check the result of method switchMonth(), because
		// the next view has been switched to the foreground
		GNMonthView view = (GNMonthView) mViewSwitcher.getCurrentView();
		Log.d("DEBUG", "to highlight date " + GNCalendarUtils.printDate(targetDate));
		// 
		view.setSelectedDate(targetDate, false);
		view.highlightMonthDate(targetDate);
		
		// should not invoke highlightMonthDate() here, because
		// the real caller may be next month view 
		// highlight month day of targetDate
		// highlightMonthDate(targetDate);
	}
	
	public boolean hasToday() {
		for(int i = 0; i < this.mActualWeekCount; ++i) {
			if(this.mWeekViews[i].hasToday()) {
				return true;
			}
		}
		
		return false;
	}

    private ViewGroup mAgendaField;

    private boolean layoutAgain = true;
    private TextView mPeriodTitle;
    private TextView mPeriodSummary;
    private Button mPeriodStart;
    private Button mPeriodFinish;
    private View mPeriodExplanation;

    private View.OnClickListener mPeriodStartListener = null;
    private View.OnClickListener mPeriodFinishListener = null;

    private TextView mSolarDateView;
    private TextView mLunarDateView;
    private TextView mNoAgendaPrompt;
    private AuroraListView mAgendaList;

    private View mAddEventView;
    private View mBirthdayReminderView;
    private TextView mBirthdayReminderContentView;
    private ImageView mBirthdayReminderWishView;

    private LunarUtil mLunarUtil = null;

	private void hideAgendaList(boolean hide) {
		Log.d("DEBUG", "hideAgendaList(): " + hide);
		if (hide) {
			mNoAgendaPrompt.setVisibility(View.VISIBLE);
			mAgendaList.setVisibility(View.GONE);
			// Statistics.onEvent(mContext, Statistics.MONTH_DAY_TAPPED_WITHOUT_AGENDA);
		} else {
			mAgendaList.setVisibility(View.VISIBLE);
			mNoAgendaPrompt.setVisibility(View.GONE);
			
			// startAgendaListAnim();
			// Statistics.onEvent(mContext, Statistics.MONTH_DAY_TAPPED_WITH_AGENDA);
		}
	}

	private Animation mAnimAgendaFieldFadeIn = null;
	private static final long ANIM_DURATION_AGENDA_LIST_FADE_IN = 600L;
	
	private void startAgendaListAnim() {
		if(mHighlightWeekIndex == mPrevHighlightWeekIndex
				&& mHighlightWeekDayIndex == mPrevHighlightWeekDayIndex) {
			// no need run animation multiple times for the same day
			return;
		}
		
		if(mAnimAgendaFieldFadeIn == null) {
			mAnimAgendaFieldFadeIn = new AlphaAnimation(0.0f, 1.0f);
			mAnimAgendaFieldFadeIn.setDuration(ANIM_DURATION_AGENDA_LIST_FADE_IN);
		}
		
		mAgendaList.startAnimation(mAnimAgendaFieldFadeIn);
	}
	
	private int mRootWidth = -1;
	private int mRootHeight = -1;
	
	private void setRootSize(int w, int h) {
		if(mRootWidth < w) mRootWidth = w;
		if(mRootHeight < h) mRootHeight = h;
	}
	
	private String[] mWeekDayText = null;
	
	private Statistics mStatistics = null;
	
	private float mDensity = 1.0f;
	private float mScaledDensity = 1.0f;
	
	private void initDensity(Context ctx) {
		Resources res = ctx.getResources();
		mDensity = res.getDisplayMetrics().density;
		mScaledDensity = res.getDisplayMetrics().scaledDensity;
	}
	
	private int dp2px(int dp) {
		return (int) (dp * mDensity + 0.5f);
	}
	
	private int sp2px(int sp) {
		return (int) (sp * mScaledDensity + 0.5f);
	}

    // Gionee <jiangxiao> <2013-0702> modify for CR00831637 begin
	private void setSelectedWeekIndex(int index) {
		mPrevHighlightWeekIndex = mHighlightWeekIndex;
		mHighlightWeekIndex = index;
	}

	private void setSelectedWeekDayIndex(int index) {
		mPrevHighlightWeekDayIndex = mHighlightWeekDayIndex;
		mHighlightWeekDayIndex = index;
	}
	// Gionee <jiangxiao> <2013-0702> modify for CR00831637 end
}
//Gionee <jiangxiao> <2013-04-11> add for CR000000 end
