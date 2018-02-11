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

package com.android.calendar;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SearchView;
import android.widget.SearchView.OnSuggestionListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.calendar.CalendarController.EventHandler;
import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.android.calendar.alerts.AlertUtils;
import com.android.calendar.extensions.AllInOneMenuExtensions;
import com.android.calendar.month.MonthByWeekFragment;
import com.android.calendar.selectcalendars.SelectVisibleCalendarsFragment;
import com.android.calendar.widget.InfoDialog;
import com.aurora.calendar.AuroraBirthdayService;
import com.aurora.calendar.AuroraCalendarSettingActivity;
import com.aurora.calendar.AuroraCalendarViewFilterActivity;
import com.aurora.calendar.event.AuroraEventInfoFragment;
import com.aurora.calendar.period.AuroraMorePopupWindow;
import com.aurora.calendar.report.ReportCommand;
import com.aurora.calendar.report.ReportUtil;
import com.aurora.commemoration.activity.RememberDayListActivity;
import com.gionee.almanac.GNAlmanacUtils;
import com.gionee.calendar.GNCalendarUtils;
import com.gionee.calendar.GNDateTextUtils;
import com.gionee.calendar.agenda.GNAgendaFragment;
import com.gionee.calendar.day.DayUtils;
import com.gionee.calendar.day.GNDayFragment;
import com.gionee.calendar.statistics.Statistics;
import com.gionee.calendar.view.GNAnimationutils;
import com.gionee.calendar.view.GNCustomTimeDialog;
import com.gionee.legalholiday.LegalHolidayUtils;
import com.gionee.widget.Utility;
import com.mediatek.calendar.GoToDatePickerDialogFragment;
import com.mediatek.calendar.LogUtil;
import com.mediatek.calendar.extension.ExtensionFactory;
import com.mediatek.calendar.extension.IOptionsMenuExt;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraDatePickerDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraDatePicker;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

import static android.provider.CalendarContract.Attendees.ATTENDEE_STATUS;
import static android.provider.CalendarContract.EXTRA_EVENT_ALL_DAY;
import static android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME;
import static android.provider.CalendarContract.EXTRA_EVENT_END_TIME;

////Gionee <jiating>  <2013-04-11> modify for CR00000000       new actionbar design begin
/// M: implement OnDateSetListener to listen date set
public class AllInOneActivity extends AuroraActivity implements EventHandler,
        OnSharedPreferenceChangeListener, SearchView.OnQueryTextListener, OnSuggestionListener {

    ////Gionee <jiating>  <2013-04-11> modify for CR00000000      new actionbar design end
    private static final String TAG = "AllInOneActivity";
    private static final boolean DEBUG = false;
    private static final String EVENT_INFO_FRAGMENT_TAG = "AuroraEventInfoFragment";
    private static final String BUNDLE_KEY_RESTORE_TIME = "key_restore_time";
    private static final String BUNDLE_KEY_EVENT_ID = "key_event_id";
    private static final String BUNDLE_KEY_RESTORE_VIEW = "key_restore_view";
    private static final String BUNDLE_KEY_CHECK_ACCOUNTS = "key_check_for_accounts";
    /// M: bundle key for "mIsInSearchMode" @{
    private static final String BUNDLE_KEY_IS_IN_SEARCH_MODE = "key_search_mode";
    private static final String BUNDLE_KEY_SEARCH_STRING = "key_search_string";
    // @}
    private static final int HANDLER_KEY = 0;

    // Indices of buttons for the drop down menu (tabs replacement)
    // Must match the strings in the array buttons_list in arrays.xml and the
    // OnNavigationListener
    /*private static final int BUTTON_DAY_INDEX = 0;
    private static final int BUTTON_WEEK_INDEX = 1;
    private static final int BUTTON_MONTH_INDEX = 2;
    private static final int BUTTON_AGENDA_INDEX = 3;*/

    private CalendarController mController;
    private static boolean mIsMultipane;
    private static boolean mIsTabletConfig;
    private static boolean mShowAgendaWithMonth;
    private static boolean mShowEventDetailsWithAgenda;
    private boolean mOnSaveInstanceStateCalled = false;
    private boolean mBackToPreviousView = false;
    private ContentResolver mContentResolver;
    private int mPreviousView;
    public static int mCurrentView;
    private boolean mPaused = true;
    private boolean mUpdateOnResume = false;
    private boolean mHideControls = false;
    private boolean mShowSideViews = true;
    private boolean mShowWeekNum = false;
    private TextView mHomeTime;
    private TextView mDateRange;
    private TextView mWeekTextView;
    private View mMiniMonth;
    private View mCalendarsList;
    private View mMiniMonthContainer;
    private View mSecondaryPane;
    private String mTimeZone;
    private boolean mShowCalendarControls;
    private boolean mShowEventInfoFullScreenAgenda;
    private boolean mShowEventInfoFullScreen;
    private int mWeekNum;
    private int mCalendarControlsAnimationTime;
    private int mControlsAnimateWidth;
    private int mControlsAnimateHeight;

    private long mViewEventId = -1;
    private long mIntentEventStartMillis = -1;
    private long mIntentEventEndMillis = -1;
    private int mIntentAttendeeResponse = Attendees.ATTENDEE_STATUS_NONE;
    private boolean mIntentAllDay = false;
    /// M: flag indicate whether in search mode @{
    private boolean mIsInSearchMode = false;
    private String mSearchString = null;
    // @}

    private Context mContext;

    // Action bar and Navigation bar (left side of Action bar)
    private AuroraActionBar mActionBar;
    private SearchView mSearchView;
    private MenuItem mSearchMenu;
    private MenuItem mControlsMenu;
    private Menu mOptionsMenu;
    ////Gionee <jiating>  <2013-04-11> modify for CR00000000       new actionbar design begin
//    private CalendarViewAdapter mActionBarMenuSpinnerAdapter;
    ////Gionee <jiating>  <2013-04-11> modify for CR00000000      new actionbar design end
    private QueryHandler mHandler;
    private boolean mCheckForAccounts = true;

    private String mHideString;
    private String mShowString;

    DayOfMonthDrawable mDayOfMonthIcon;

    int mOrientation;

    // Params for animating the controls on the right
    private LayoutParams mControlsParams;
    private LinearLayout.LayoutParams mVerticalControlsParams;

    private AllInOneMenuExtensions mExtensions = new AllInOneMenuExtensions();

    ////Gionee <jiating>  <2013-04-11> modify for CR00000000      new actionbar design begin
    private View mContentView;
    private View mCustomView;     //actionBar CustomView
    private TextView mActionBarTimeYearView;
    private TextView mActionBarTimeMonthView;
    private View mActionBarReturntodayButton;
    private View mAddEventButton;
    private View mActionBarChangeTimeButton;
    private GNDateTextUtils mGNDateTextUtils;
    private long mLastTimeMillis = -1;
    private final int DATE_SELECT_DIALOG = 1;
    public static int ANIMATION_NEED_MORE_DISTANCE;
    ////Gionee <jiating>  <2013-04-11> modify for CR00000000     new actionbar design end

    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;

    private boolean isChineseEnvironment = true;
    private boolean checkBirthdayReminder = true;
    private boolean first_flag = true;
    private boolean second_flag = true;
    private AuroraMorePopupWindow mMorePopupWindow;

    private final AnimatorListener mSlideAnimationDoneListener = new AnimatorListener() {

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationEnd(android.animation.Animator animation) {
            int visibility = mShowSideViews ? View.VISIBLE : View.GONE;
            mMiniMonth.setVisibility(visibility);
            mCalendarsList.setVisibility(visibility);
            mMiniMonthContainer.setVisibility(visibility);
        }

        @Override
        public void onAnimationRepeat(android.animation.Animator animation) {
        }

        @Override
        public void onAnimationStart(android.animation.Animator animation) {
        }
    };

    private class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            mCheckForAccounts = false;
            try {
                // If the query didn't return a cursor for some reason return
                if (cursor == null || cursor.getCount() > 0 || isFinishing()) {
                    return;
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            Bundle options = new Bundle();
            options.putCharSequence("introMessage",
                    getResources().getString(R.string.create_an_account_desc));
            options.putBoolean("allowSkip", true);

            AccountManager am = AccountManager.get(AllInOneActivity.this);
            am.addAccount("com.google", CalendarContract.AUTHORITY, null, options,
                    AllInOneActivity.this,
                    new AccountManagerCallback<Bundle>() {
                        @Override
                        public void run(AccountManagerFuture<Bundle> future) {
                            if (future.isCancelled()) {
                                return;
                            }
                            try {
                                Bundle result = future.getResult();
                                boolean setupSkipped = result.getBoolean("setupSkipped");

                                if (setupSkipped) {
                                    Utils.setSharedPreference(AllInOneActivity.this,
                                            GeneralPreferences.KEY_SKIP_SETUP, true);
                                }

                            } catch (OperationCanceledException ignore) {
                                // The account creation process was canceled
                            } catch (IOException ignore) {
                            } catch (AuthenticatorException ignore) {
                            }
                        }
                    }, null);
        }
    }

    private final Runnable mHomeTimeUpdater = new Runnable() {
        @Override
        public void run() {
            mTimeZone = Utils.getTimeZone(AllInOneActivity.this, mHomeTimeUpdater);
            updateSecondaryTitleFields(-1);
            AllInOneActivity.this.invalidateOptionsMenu();
            Utils.setMidnightUpdater(mHandler, mTimeChangesUpdater, mTimeZone);
        }
    };

    // runs every midnight/time changes and refreshes the today icon
    private final Runnable mTimeChangesUpdater = new Runnable() {
        @Override
        public void run() {
            mTimeZone = Utils.getTimeZone(AllInOneActivity.this, mHomeTimeUpdater);
            AllInOneActivity.this.invalidateOptionsMenu();
            Utils.setMidnightUpdater(mHandler, mTimeChangesUpdater, mTimeZone);
        }
    };


    // Create an observer so that we can update the views whenever a
    // Calendar event changes.
    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            eventsChanged();
        }
    };

    BroadcastReceiver mCalIntentReceiver;

    /**
     * M: the options menu extension
     */
    private IOptionsMenuExt mOptionsMenuExt;

    @Override
    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();
        if (DEBUG)
            Log.d(TAG, "New intent received " + intent.toString());
        // Don't change the date if we're just returning to the app's home
        if (Intent.ACTION_VIEW.equals(action)
                && !intent.getBooleanExtra(Utils.INTENT_KEY_HOME, false)) {
            // Gionee <jiangxiao> <2013-07-02> delete for CR00831115 begin
            // negative millis is ok because of gmt-off
            // long millis = parseViewAction(intent);
            // if (millis == -1) {
            // Log.d("createEvent",
            // "GO_TO get time by Utils.timeFromIntentInMillis()");
            // millis = Utils.timeFromIntentInMillis(intent);
            // }

            long millis = intent.getLongExtra(EXTRA_EVENT_BEGIN_TIME, -1);
            // Gionee <jiangxiao> <2013-07-02> delete for CR00831115 end
            if (millis != -1 && mViewEventId == -1 && mController != null) {
                Time time = new Time(mTimeZone);
                time.set(millis);
                time.normalize(true);

                // Gionee <lilg><2013-05-03> modify for get viewType from widget if exists begin
                int viewType = intent.getIntExtra(Utility.KEY_EXTRA_VIEW_TYPE, -1);
                if (DEBUG) {
                    Log.d(TAG, "viewType from intent: " + viewType);
                }
                if (viewType != -1) {
                    mController.sendEvent(this, EventType.GO_TO, time, time, -1, viewType);
                } else {
                    mController.sendEvent(this, EventType.GO_TO, time, time, -1, ViewType.CURRENT);
                }
                // Gionee <lilg><2013-05-03> modify for get viewType from widget if exists end
            }
        }

    }

    @Override
    protected void onCreate(Bundle icicle) {
        if (Utils.getSharedPreference(this, OtherPreferences.KEY_OTHER_1, false)) {
//            setTheme(R.style.CalendarTheme_WithActionBarWallpaper);
        }
        super.onCreate(icicle);

        mContext = this;

        boolean showLunar = Utils.getSharedPreference(this, "lunar_display", false);
        if (showLunar) {
            ReportCommand command = new ReportCommand(this, ReportUtil.TAG_LUNAR);
            command.updateData();
        }

        int startDay = Utils.getFirstDayOfWeek(this);
        if (startDay == 1) {
            ReportCommand command = new ReportCommand(this, ReportUtil.TAG_STARTDAY);
            command.updateData();
        }

        ////Gionee <jiating>  <2013-04-11> modify for CR00000000    CR00000000    Theme design begin
//        setTheme(R.style.CalendarAllInIneTheme_WithNoActionBar);
        ANIMATION_NEED_MORE_DISTANCE = DayUtils.dip2px(AllInOneActivity.this, 3.3f);
        ////Gionee <jiating>  <2013-04-11> modify for CR00000000    CR00000000    Theme design end 
        /**
         * M: we should restore search state while we still in search mode @{
         */
        if (icicle != null) {
            if (icicle.containsKey(BUNDLE_KEY_CHECK_ACCOUNTS)) {
                mCheckForAccounts = icicle.getBoolean(BUNDLE_KEY_CHECK_ACCOUNTS);
            }
            if (icicle.containsKey(BUNDLE_KEY_IS_IN_SEARCH_MODE)) {
                mIsInSearchMode = icicle.getBoolean(BUNDLE_KEY_IS_IN_SEARCH_MODE, false);
            }
            if (icicle.containsKey(BUNDLE_KEY_SEARCH_STRING)) {
                mSearchString = icicle.getString(BUNDLE_KEY_SEARCH_STRING, null);
            }
        }
        /** @} */
        // Launch add google account if this is first time and there are no
        // accounts yet
        if (mCheckForAccounts
                && !Utils.getSharedPreference(this, GeneralPreferences.KEY_SKIP_SETUP, false)) {
            mHandler = new QueryHandler(this.getContentResolver());
            mHandler.startQuery(0, null, Calendars.CONTENT_URI, new String[]{
                    Calendars._ID
            }, null, null /* selection args */, null /* sort order */);
        }

        ContentValues values = new ContentValues();
        values.put(Calendars.VISIBLE, 1);
        // values.put(Calendars.CALENDAR_COLOR, getResources().getColor(R.color.month_day_tap_color));
        this.getContentResolver().update(
                Calendars.CONTENT_URI,
                values,
                Calendars.ACCOUNT_TYPE + "='" + CalendarContract.ACCOUNT_TYPE_LOCAL + "' AND " +
                        Calendars.ACCOUNT_NAME + "!='" + Utils.NOTE_REMINDER_ACCOUNT_NAME + "'",
                null);

        // This needs to be created before setContentView
        mController = CalendarController.getInstance(this);
        isChineseEnvironment = Utils.isChineseEnvironment();

        // Get time from intent or icicle
        long timeMillis = -1;
        int viewType = -1;
        final Intent intent = getIntent();
        if (icicle != null) {
            timeMillis = icicle.getLong(BUNDLE_KEY_RESTORE_TIME);
            viewType = icicle.getInt(BUNDLE_KEY_RESTORE_VIEW, -1);
        } else {
            String action = intent.getAction();
            if (Intent.ACTION_VIEW.equals(action)) {
                // Open EventInfo later
                timeMillis = parseViewAction(intent);

                // Gionee <lilg><2013-05-03> add for get viewType from widget intent begin
                viewType = intent.getIntExtra(Utility.KEY_EXTRA_VIEW_TYPE, -1);
                if (DEBUG) {
                    Log.d(TAG, "viewType from widget intent is: " + viewType);
                }
                // Gionee <lilg><2013-05-03> add for get viewType from widget intent end
            }

            if (timeMillis == -1) {
                timeMillis = Utils.timeFromIntentInMillis(intent);
            }
        }
        // Gionee <jiangxiao> <2013-06-19> add for CR00827069 begin
        timeMillis = GNCalendarUtils.checkTimeRange(timeMillis);
        // Gionee <jiangxiao> <2013-06-19> add for CR00827069 end

        if (viewType == -1) {
            viewType = Utils.getViewTypeFromIntentAndSharedPref(this);
        }
        mTimeZone = Utils.getTimeZone(this, mHomeTimeUpdater);
        Time t = new Time(mTimeZone);
        t.set(timeMillis);

        if (DEBUG) {
            if (icicle != null && intent != null) {
                Log.d(TAG, "both, icicle:" + icicle.toString() + "  intent:" + intent.toString());
            } else {
                Log.d(TAG, "not both, icicle:" + icicle + " intent:" + intent);
            }
        }

        Resources res = getResources();
        mHideString = res.getString(R.string.hide_controls);
        mShowString = res.getString(R.string.show_controls);
        mOrientation = res.getConfiguration().orientation;
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            mControlsAnimateWidth = (int) res.getDimension(R.dimen.calendar_controls_width);
            if (mControlsParams == null) {
                mControlsParams = new LayoutParams(mControlsAnimateWidth, 0);
            }
            mControlsParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else {
            // Make sure width is in between allowed min and max width values
            mControlsAnimateWidth = Math.max(res.getDisplayMetrics().widthPixels * 45 / 100,
                    (int) res.getDimension(R.dimen.min_portrait_calendar_controls_width));
            mControlsAnimateWidth = Math.min(mControlsAnimateWidth,
                    (int) res.getDimension(R.dimen.max_portrait_calendar_controls_width));
        }

        mControlsAnimateHeight = (int) res.getDimension(R.dimen.calendar_controls_height);

        mHideControls = !Utils.getSharedPreference(
                this, GeneralPreferences.KEY_SHOW_CONTROLS, true);
        mIsMultipane = Utils.getConfigBool(this, R.bool.multiple_pane_config);
        mIsTabletConfig = Utils.getConfigBool(this, R.bool.tablet_config);
        mShowAgendaWithMonth = Utils.getConfigBool(this, R.bool.show_agenda_with_month);
        mShowCalendarControls =
                Utils.getConfigBool(this, R.bool.show_calendar_controls);
        mShowEventDetailsWithAgenda =
                Utils.getConfigBool(this, R.bool.show_event_details_with_agenda);
        mShowEventInfoFullScreenAgenda =
                Utils.getConfigBool(this, R.bool.agenda_show_event_info_full_screen);
        mShowEventInfoFullScreen =
                Utils.getConfigBool(this, R.bool.show_event_info_full_screen);
        mCalendarControlsAnimationTime = res.getInteger(R.integer.calendar_controls_animation_time);
        Utils.setAllowWeekForDetailView(mIsMultipane);

        // setContentView must be called before configureActionBar
        // setContentView(R.layout.all_in_one);
        setAuroraContentView(R.layout.all_in_one, AuroraActionBar.Type.Empty);
        getAuroraActionBar().setVisibility(View.GONE);

        setAuroraMenuCallBack(auroraMenuCallBack);
        setAuroraMenuItems(R.menu.aurora_all_in_one);

//        setCustomView(R.layout.all_in_one);

        if (mIsTabletConfig) {
            mDateRange = (TextView) findViewById(R.id.date_bar);
            mWeekTextView = (TextView) findViewById(R.id.week_num);
        } else {
            mDateRange = (TextView) getLayoutInflater().inflate(R.layout.date_range_title, null);
        }

        // configureActionBar auto-selects the first tab you add, so we need to
        // call it before we set up our own fragments to make sure it doesn't
        // overwrite us
        configureActionBar(viewType);

        mHomeTime = (TextView) findViewById(R.id.home_time);
        mMiniMonth = findViewById(R.id.mini_month);
        if (mIsTabletConfig && mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mMiniMonth.setLayoutParams(new LinearLayout.LayoutParams(mControlsAnimateWidth,
                    mControlsAnimateHeight));
        }
        mCalendarsList = findViewById(R.id.calendar_list);
        mMiniMonthContainer = findViewById(R.id.mini_month_container);
        mSecondaryPane = findViewById(R.id.secondary_pane);

        // Must register as the first activity because this activity can modify
        // the list of event handlers in it's handle method. This affects who
        // the rest of the handlers the controller dispatches to are.
        mController.registerFirstEventHandler(HANDLER_KEY, this);

        initFragments(timeMillis, viewType, icicle);

        // Listen for changes that would require this to be refreshed
        SharedPreferences prefs = GeneralPreferences.getSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        SharedPreferences prefs2 = getSharedPreferences(Utils.PERIOD_SP, Context.MODE_PRIVATE);
        prefs2.registerOnSharedPreferenceChangeListener(this);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mPaused) {
                    mUpdateOnResume = true;
                }
            }
        };
        mFilter = new IntentFilter(Intent.ACTION_TIME_CHANGED);
        registerReceiver(mReceiver, mFilter);

        mContentResolver = getContentResolver();

        ///M: the option menu extension @{
        mOptionsMenuExt = ExtensionFactory.getAllInOneOptionMenuExt(this);
        ///@}

        //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 begin
        /*if (CalendarApplication.isEnableUpgrade()) {
            Intent checkIntent = new Intent("android.intent.action.GN_APP_UPGRADE_CHECK_VERSION");
            checkIntent.putExtra("package", getApplicationContext().getPackageName());
            //checkIntent.putExtra("package", "com.gionee.note");
            startService(checkIntent);
            
           
            //Log.d("upgrade", "startService");
            //Log.d("upgrade", "test456"+getApplicationContext().getPackageName()+"com.gionee.note");
        }*/
        //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 end


        //Gionee <Author: lihongyu> <2013-05-17> add for CR000000 begin

        LegalHolidayUtils.initHolidayData(this);

        // Gionee <jiangxiao> <2013-07-16> add for CR00837096 begin
        GNAlmanacUtils gnAlmanacUtils = GNAlmanacUtils.getInstance();
        gnAlmanacUtils.initAlmanac(this);
        // Gionee <jiangxiao> <2013-07-16> add for CR00837096 end

        //Gionee <Author: lihongyu> <2013-05-17> add for CR000000 end

        // Gionee <jiangxiao> <2013-06-19> add for CRxxxxxxxx begin
        // use a unique Toast object to avoid multiple Toast popup
        initToast();
        // Gionee <jiangxiao> <2013-06-19> add for CRxxxxxxxx end
        
        checkBirthdayReminder = !InfoDialog.showSysAuthDialog(this, mContentResolver, mContext);
    }

    private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {
        @Override
        public void auroraMenuItemClick(int arg0) {
          if (arg0 == R.id.calendar_setting) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setClass(AllInOneActivity.this, AuroraCalendarSettingActivity.class);
//		        intent.setClass(mContext, GNCalendarSettingViewActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        }
    };

    private long parseViewAction(final Intent intent) {
        long timeMillis = -1;
        Uri data = intent.getData();
        if (data != null && data.isHierarchical()) {
            List<String> path = data.getPathSegments();
            if (path.size() == 2 && path.get(0).equals("events")) {
                try {
                    mViewEventId = Long.valueOf(data.getLastPathSegment());
                    if (mViewEventId != -1) {
                        mIntentEventStartMillis = intent.getLongExtra(EXTRA_EVENT_BEGIN_TIME, 0);
                        mIntentEventEndMillis = intent.getLongExtra(EXTRA_EVENT_END_TIME, 0);
                        mIntentAttendeeResponse = intent.getIntExtra(
                                ATTENDEE_STATUS, Attendees.ATTENDEE_STATUS_NONE);
                        mIntentAllDay = intent.getBooleanExtra(EXTRA_EVENT_ALL_DAY, false);
                        timeMillis = mIntentEventStartMillis;
                    }
                } catch (NumberFormatException e) {
                    // Ignore if mViewEventId can't be parsed
                }
            }
        }
        return timeMillis;
    }

    //Gionee <jiating>  <2013-04-11> modify for CR00000000       new actionbar design begin
    private void configureActionBar(int viewType) {
        mActionBar = getAuroraActionBar();
        mGNDateTextUtils = new GNDateTextUtils(getApplicationContext());
//        createButtonsSpinner(viewType, mIsTabletConfig);
        if (mIsMultipane) {
//            mActionBar.setDisplayOptions(
//                    AuroraActionBar.DISPLAY_SHOW_CUSTOM | AuroraActionBar.DISPLAY_SHOW_HOME);
        } else {
        	mContentView = findViewById(R.id.all_in_one_content);
            mCustomView = findViewById(R.id.all_in_one_action_bar);
//        	mActionBar.setDisplayOptions(AuroraActionBar.DISPLAY_SHOW_CUSTOM);
//        	mActionBar.setDisplayHomeAsUpEnabled(false);
//        	mActionBar.setDisplayShowTitleEnabled(false);
//        	mActionBar.setDisplayShowHomeEnabled(false);
//        	mActionBar.setDisplayShowCustomEnabled(true);
//        	mCustomView=LayoutInflater.from(mActionBar.getThemedContext()).inflate(R.layout.gn_all_in_one_actionbar_customview, null);
//        	mActionBar.setCustomView(mCustomView, new AuroraActionBar.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            mActionBarTimeYearView = (TextView) mCustomView.findViewById(R.id.gn_all_in_one_actionbar_time_year_show);
            mActionBarTimeMonthView = (TextView) mCustomView.findViewById(R.id.gn_all_in_one_actionbar_time_month_show);

            mActionBarTimeYearView.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
            mActionBarTimeYearView.getPaint().setStrokeWidth(0.8f);

            mActionBarTimeMonthView.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
            mActionBarTimeMonthView.getPaint().setStrokeWidth(0.8f);

//        	Typeface monthFace = Typeface.createFromAsset(AllInOneActivity.this.getAssets(),"fonts/Roboto-Regular.ttf");
//        	Typeface yearFace = Typeface.createFromAsset(AllInOneActivity.this.getAssets(),"fonts/Roboto-Thin.ttf");
//        	mActionBarTimeMonthView.setTypeface(monthFace);
//        	mActionBarTimeYearView.setTypeface(yearFace);

            mActionBarReturntodayButton = mCustomView.findViewById(R.id.gn_all_in_one_actionbar_return_today_view);
            mActionBarReturntodayButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                   Time t = null;
                    int viewType = ViewType.CURRENT;
                    long extras = CalendarController.EXTRA_GOTO_TIME;
                    viewType = ViewType.CURRENT;
                    t = new Time(mTimeZone);
                    t.setToNow();
                    extras |= CalendarController.EXTRA_GOTO_TODAY;
                    mController.sendEvent(this, EventType.GO_TO, t, null, t, -1, viewType, extras, null, null);
                    mActionBarReturntodayButton.setVisibility(View.GONE);
                    //Gionee <pengwei><2013-05-20> modify for CR00813693 begin
                    switch (mCurrentView) {
                        case ViewType.DAY:
                            Statistics.onEvent(AllInOneActivity.this, Statistics.DAY_VIEW_BACK_TODAY);
                            break;
                        case ViewType.WEEK:
                            Statistics.onEvent(AllInOneActivity.this, Statistics.WEEK_VIEW_BACK_TODAY);
                            break;
                        default:
                            break;
                    }
                    //Gionee <pengwei><2013-05-20> modify for CR00813693 end
                }
            });

            mActionBarChangeTimeButton = mCustomView.findViewById(R.id.gn_all_in_one_actionbar_time_operation);
            mActionBarChangeTimeButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    //Gionee <pengwei><2013-05-20> modify for CR00813693 begin
                    switch (mCurrentView) {
                        case ViewType.DAY:
                            Statistics.onEvent(AllInOneActivity.this, Statistics.DAY_VIEW_Time_JUMP);
                            break;
                        case ViewType.WEEK:
                            Statistics.onEvent(AllInOneActivity.this, Statistics.WEEK_VIEW_Time_JUMP);
                            break;
                        case ViewType.MONTH:
                            Statistics.onEvent(AllInOneActivity.this, Statistics.MONTH_DAY_PICKED);
                            break;
                        default:
                            break;
                    }
                    //Gionee <pengwei><2013-05-20> modify for CR00813693 end
                    showSelectDateDialog(DATE_SELECT_DIALOG);
                }
            });

            mAddEventButton = mCustomView.findViewById(R.id.gn_all_in_one_create_event);
            mAddEventButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.isIndiaVersion()) {
                        gotoCreateEvent();
                    } else {
                        checkPopup();
                    }
                }
            });

            if (Utils.isIndiaVersion()) {
                mCustomView.findViewById(R.id.all_in_one_right_view)
                        .setBackgroundResource(R.drawable.aurora_actionbar_create_event);
            }
        }
    }
//Gionee <jiating>  <2013-04-11> modify for CR00000000      new actionbar design end

    private void gotoCreateEvent() {
        ReportCommand command = new ReportCommand(mContext, ReportUtil.TAG_ADDEVENT);
        command.updateData();

        Time t = new Time();
        t.set(mController.getTime());

        Time temp = new Time();
        temp.set(mController.getTime());

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

        mController.sendEventRelatedEvent(
                this, EventType.CREATE_EVENT, -1, t.toMillis(true), 0, 0, 0, temp.toMillis(true));
    }

    private void checkPopup() {
        final boolean isPeroid = Utils.getPeriodSharePreference(mContext, Utils.CALENDAR_DISPLAY_MODE, Utils.MODE_NORMAL)
                == Utils.MODE_PERIOD;
        mMorePopupWindow = null;
        mMorePopupWindow = new AuroraMorePopupWindow(mContext, new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.show_or_hide_period:
                        if (mMorePopupWindow != null) {
                            mMorePopupWindow.dismiss();
                        }

                        Utils.setPeriodSharePreference(mContext, Utils.CALENDAR_DISPLAY_MODE,
                                isPeroid ? Utils.MODE_NORMAL : Utils.MODE_PERIOD);
                        initFragments(mController.getTime(), mController.getViewType(), null);
                        checkSecond();
                        break;
                    case R.id.remember_day_entry:
                        if (mMorePopupWindow != null) {
                            mMorePopupWindow.dismiss();
                        }
                        Intent intent = new Intent(mContext, RememberDayListActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        }, isPeroid);
        mMorePopupWindow.showAtLocation(mContentView, Gravity.RIGHT | Gravity.TOP, 0, 0);
    }

    private void checkSecond() {
    	if (second_flag) {
    		second_flag = false;

            if (Utils.isWomenEnvironment() && Utils.getPeriodSharePreference(mContext, Utils.IS_SECOND_KEY, 0) == 0) {
                View menuView = LayoutInflater.from(AllInOneActivity.this).inflate(R.layout.second_main_dialog, null);

                final Dialog dialog = new Dialog(AllInOneActivity.this, R.style.Root_Dialog);
                dialog.setContentView(menuView);
                Window win = dialog.getWindow();
                win.setGravity(Gravity.RIGHT | Gravity.TOP);

                View guideView = menuView.findViewById(R.id.guide_second);
                guideView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                dialog.setCanceledOnTouchOutside(true);
                dialog.setCancelable(true);
                dialog.show();

                Utils.setPeriodSharePreference(mContext, Utils.IS_SECOND_KEY, 1);
            }
        }
    }

    private void checkFirst() {
    	if (first_flag) {
    		first_flag = false;

            if (Utils.isWomenEnvironment() && Utils.getPeriodSharePreference(mContext, Utils.IS_FIRST_KEY, 0) == 0) {
                View menuView = LayoutInflater.from(AllInOneActivity.this).inflate(R.layout.first_main_dialog, null);
                ImageView hideView = (ImageView) menuView.findViewById(R.id.hide_first);

                final Dialog dialog = new Dialog(AllInOneActivity.this, R.style.Root_Dialog);
                dialog.setContentView(menuView);
                Window win = dialog.getWindow();
                win.setGravity(Gravity.RIGHT | Gravity.TOP);

                hideView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        checkPopup();
                    }
                });

                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.show();

                Utils.setPeriodSharePreference(mContext, Utils.IS_FIRST_KEY, 1);
            }
        }
    }

//Gionee <jiating>  <2013-04-11> modify for CR00000000      don't use Tab and ButtonsSpinner begin  
    /**
     * private void createTabs() {
     * mActionBar = getAuroraActionBar();
     * if (mActionBar == null) {
     * Log.w(TAG, "AuroraActionBar is null.");
     * } else {
     * mActionBar.setNavigationMode(AuroraActionBar.NAVIGATION_MODE_TABS);
     * mDayTab = mActionBar.newTab();
     * mDayTab.setText(getString(R.string.day_view));
     * mDayTab.setTabListener(this);
     * mActionBar.addTab(mDayTab);
     * mWeekTab = mActionBar.newTab();
     * mWeekTab.setText(getString(R.string.week_view));
     * mWeekTab.setTabListener(this);
     * mActionBar.addTab(mWeekTab);
     * mMonthTab = mActionBar.newTab();
     * mMonthTab.setText(getString(R.string.month_view));
     * mMonthTab.setTabListener(this);
     * mActionBar.addTab(mMonthTab);
     * mAgendaTab = mActionBar.newTab();
     * mAgendaTab.setText(getString(R.string.agenda_view));
     * mAgendaTab.setTabListener(this);
     * mActionBar.addTab(mAgendaTab);
     * }
     * }
     * <p/>
     * private void createButtonsSpinner(int viewType, boolean tabletConfig) {
     * //Gionee <jiating>  <2013-04-11> modify for CR00000000     new actionbar design don't use mActionBarMenuSpinnerAdapter begin
     * // If tablet configuration , show spinner with no dates
     * //        mActionBarMenuSpinnerAdapter = new CalendarViewAdapter (this, viewType, !tabletConfig);
     * mActionBar = getAuroraActionBar();
     * mActionBar.setNavigationMode(AuroraActionBar.NAVIGATION_MODE_LIST);
     * //        mActionBar.setListNavigationCallbacks(mActionBarMenuSpinnerAdapter, this);
     * //Gionee <jiating>  <2013-04-11> modify for CR00000000      new actionbar design don't use mActionBarMenuSpinnerAdapter end
     * switch (viewType) {
     * case ViewType.AGENDA:
     * mActionBar.setSelectedNavigationItem(BUTTON_AGENDA_INDEX);
     * break;
     * case ViewType.DAY:
     * mActionBar.setSelectedNavigationItem(BUTTON_DAY_INDEX);
     * break;
     * case ViewType.WEEK:
     * mActionBar.setSelectedNavigationItem(BUTTON_WEEK_INDEX);
     * break;
     * case ViewType.MONTH:
     * mActionBar.setSelectedNavigationItem(BUTTON_MONTH_INDEX);
     * break;
     * default:
     * mActionBar.setSelectedNavigationItem(BUTTON_DAY_INDEX);
     * break;
     * }
     * }
     */
    //Gionee <jiating>  <2013-04-11> modify for CR00000000     don't use Tab and ButtonsSpinner end

    // Clear buttons used in the agenda view
    private void clearOptionsMenu() {
        if (mOptionsMenu == null) {
            return;
        }
        MenuItem cancelItem = mOptionsMenu.findItem(R.id.action_cancel);
        if (cancelItem != null) {
            cancelItem.setVisible(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) {
            Log.i(TAG, "onResume begin.");
        }
        //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 begin
        //((CalendarApplication) getApplication()).registerVersionCallback(this);
        //Log.d("upgrade", "onResume     " + "registerVersionCallback");

        changeStatusBar(true);

        //Log
        //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 end

        ///M:#clear all events# to update the view. @{
        if (mOnSaveInstanceStateCalled && mController != null && mIsClearEventsCompleted) {
            mIsClearEventsCompleted = false;
            switch (mCurrentView) {
                case ViewType.MONTH:// send event change event to update month
                    // view(261245).
                    mController.sendEvent(this, EventType.EVENTS_CHANGED, null, null, -1, mCurrentView);
                    LogUtil.v(TAG, "After CLEAR EVENTS COMPLETED, send Event EVENTS_CHANGED.");
                    break;
            }
        }
        ///@}

        // Must register as the first activity because this activity can modify
        // the list of event handlers in it's handle method. This affects who
        // the rest of the handlers the controller dispatches to are.
        mController.registerFirstEventHandler(HANDLER_KEY, this);

        mOnSaveInstanceStateCalled = false;
        mContentResolver.registerContentObserver(CalendarContract.Events.CONTENT_URI,
                true, mObserver);
        if (mUpdateOnResume) {
            initFragments(mController.getTime(), mController.getViewType(), null);
            mUpdateOnResume = false;
        }/* else {
            // Gionee <lilg><2013-05-04> add for init fragments begin
        	if(DEBUG){
        		Log.d(TAG, "mController.getTime(): " + mController.getTime() + ", mController.getViewType(): " + mController.getViewType());
        	}
        	initFragments(mController.getTime(), mController.getViewType(), null);
        	// Gionee <lilg><2013-05-04> add for init fragments end
        }*/
        Time t = new Time(mTimeZone);
        t.set(mController.getTime());
        mController.sendEvent(this, EventType.UPDATE_TITLE, t, t, -1, ViewType.CURRENT,
                mController.getDateFlags(), null, null);
        // Make sure the drop-down menu will get its date updated at midnight
        //Gionee <jiating>  <2013-04-11> modify for CR00000000     new actionbar design don't use mActionBarMenuSpinnerAdapter begin
//        if (mActionBarMenuSpinnerAdapter != null) {
//            mActionBarMenuSpinnerAdapter.refresh(this);
//        }
        if (mGNDateTextUtils != null) {
            mGNDateTextUtils.refresh(getApplicationContext());
            String[] date = mGNDateTextUtils.updateDateYearAndMonthTextByView();
            Log.i("jiating", "onResume.......date[0]=" + date[0] + "date[1]=" + date[1]);
            setActionBarTimeViewValue(date[0], date[1]);
        }

        //Gionee <jiating>  <2013-04-11> modify for CR00000000    new actionbar design don't use mActionBarMenuSpinnerAdapter end

        if (mControlsMenu != null) {
            mControlsMenu.setTitle(mHideControls ? mShowString : mHideString);
        }
        mPaused = false;

        if (mViewEventId != -1 && mIntentEventStartMillis != -1 && mIntentEventEndMillis != -1) {
            long currentMillis = System.currentTimeMillis();
            long selectedTime = -1;
            if (currentMillis > mIntentEventStartMillis && currentMillis < mIntentEventEndMillis) {
                selectedTime = currentMillis;
            }
            mController.sendEventRelatedEventWithExtra(this, EventType.VIEW_EVENT, mViewEventId,
                    mIntentEventStartMillis, mIntentEventEndMillis, -1, -1,
                    EventInfo.buildViewExtraLong(mIntentAttendeeResponse, mIntentAllDay),
                    selectedTime);
            mViewEventId = -1;
            mIntentEventStartMillis = -1;
            mIntentEventEndMillis = -1;
            mIntentAllDay = false;
        }
        Utils.setMidnightUpdater(mHandler, mTimeChangesUpdater, mTimeZone);
        // Make sure the today icon is up to date
//Gionee <jiating> <2013-03-26> modify for CR00000000      instead menu of sliding begin       
//        invalidateOptionsMenu();
//          if(isMenuOut){
//        	  setMenuOut();
//        	  isMenuOut=!isMenuOut;
//          }   

//Gionee <jiating> <2013-03-26> modify for CR00000000     instead menu of sliding end
        mCalIntentReceiver = Utils.setTimeChangesReceiver(this, mTimeChangesUpdater);

        if (DEBUG) {
            Log.i(TAG, "onResume end.");
        }
        //Gionee <jiating><2013-06-17> modify for CR00822801 begin
        /*new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				getEventCount();
			}
		}).start();*/
        //Gionee <jiating><2013-06-17> modify for CR00822801 end

        if (checkBirthdayReminder) {
            checkBirthdayReminder = false;

            Cursor cursor = mContentResolver.query(
                    Events.CONTENT_URI,
                    new String[]{Events._ID},
                    Events.OWNER_ACCOUNT + "='" + Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME + "'",
                    null,
                    null);
            if (cursor != null && cursor.getCount() == 0) {
                startService(new Intent(this, AuroraBirthdayService.class));
            }
            if (cursor != null) cursor.close();
        }

        if (!Utils.isIndiaVersion()) {
            checkFirst();
        }

        Statistics.onResume(AllInOneActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        countViewGoto();
        //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 begin
        //((CalendarApplication) getApplication()).unregisterVersionCallback(this);

        Log.d("upgrade", "unregisterVersionCallback2");
        //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 end

        mController.deregisterEventHandler(HANDLER_KEY);
        mPaused = true;
        mHomeTime.removeCallbacks(mHomeTimeUpdater);
        //Gionee <jiating>  <2013-04-11> modify for CR00000000     new actionbar design don't use mActionBarMenuSpinnerAdapter begin
//        if (mActionBarMenuSpinnerAdapter != null) {
//            mActionBarMenuSpinnerAdapter.onPause();
//        }
        //Gionee <jiating>  <2013-04-11> modify for CR00000000     new actionbar design don't use mActionBarMenuSpinnerAdapter end
        mContentResolver.unregisterContentObserver(mObserver);
        if (isFinishing()) {
            // Stop listening for changes that would require this to be refreshed
            SharedPreferences prefs = GeneralPreferences.getSharedPreferences(this);
            prefs.unregisterOnSharedPreferenceChangeListener(this);

            SharedPreferences prefs2 = getSharedPreferences(Utils.PERIOD_SP, Context.MODE_PRIVATE);
            prefs2.unregisterOnSharedPreferenceChangeListener(this);

            unregisterReceiver(mReceiver);
        }
        // FRAG_TODO save highlighted days of the week;
        if (mController.getViewType() != ViewType.EDIT) {
            Utils.setDefaultView(this, mController.getViewType());
        }
        Utils.resetMidnightUpdater(mHandler, mTimeChangesUpdater);
        Utils.clearTimeChangesReceiver(this, mCalIntentReceiver);

        clearViewGotoCount();
        Statistics.onPause(AllInOneActivity.this);
    }

    @Override
    protected void onUserLeaveHint() {
        mController.sendEvent(this, EventType.USER_HOME, null, null, -1, ViewType.CURRENT);
        super.onUserLeaveHint();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mOnSaveInstanceStateCalled = true;
        super.onSaveInstanceState(outState);
        outState.putLong(BUNDLE_KEY_RESTORE_TIME, mController.getTime());
        outState.putInt(BUNDLE_KEY_RESTORE_VIEW, mCurrentView);
        if (mCurrentView == ViewType.EDIT) {
            outState.putLong(BUNDLE_KEY_EVENT_ID, mController.getEventId());
        }
        //Gionee <jiating> <2013-04-23>  modify for CR00000000      calcle ViewType.AGENDA begin
//        else if (mCurrentView == ViewType.AGENDA) {
//            FragmentManager fm = getFragmentManager();
//            Fragment f = fm.findFragmentById(R.id.main_pane);
//		//Gionee <jiating>  <2013-04-18>  modify for CR00000000         GNAgendaFragment instead of AgendaFragment begin 
//            if (f instanceof GNAgendaFragment) {
//                outState.putLong(BUNDLE_KEY_EVENT_ID, ((GNAgendaFragment)f).getLastShowEventId());
//            }
//        }
        //Gionee <jiating>  <2013-04-18>  modify for CR00000000      GNAgendaFragment instead of AgendaFragment end
        //Gionee <jiating> <2013-04-23>  modify for CR00000000       calcle ViewType.AGENDA end
        outState.putBoolean(BUNDLE_KEY_CHECK_ACCOUNTS, mCheckForAccounts);
        /**
         *  M: save search mode @{
         */
        if (mSearchMenu == null || !mSearchMenu.isActionViewExpanded()) {
            mIsInSearchMode = false;
        } else {
            mIsInSearchMode = true;
            mSearchString = (mSearchView != null) ? mSearchView.getQuery().toString() : null;
            outState.putString(BUNDLE_KEY_SEARCH_STRING, mSearchString);
        }
        outState.putBoolean(BUNDLE_KEY_IS_IN_SEARCH_MODE, mIsInSearchMode);
        /** @} */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences prefs = GeneralPreferences.getSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);

        SharedPreferences prefs2 = getSharedPreferences(Utils.PERIOD_SP, Context.MODE_PRIVATE);
        prefs2.unregisterOnSharedPreferenceChangeListener(this);

        mController.deregisterAllEventHandlers();

        CalendarController.removeInstance(this);
    }

    private void initFragments(long timeMillis, int viewType, Bundle icicle) {
        if (DEBUG) {
            Log.d(TAG, "Initializing to " + timeMillis + " for view " + viewType);
        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        if (mShowCalendarControls) {
            Fragment miniMonthFrag = new MonthByWeekFragment(timeMillis, true);
            ft.replace(R.id.mini_month, miniMonthFrag);
            mController.registerEventHandler(R.id.mini_month, (EventHandler) miniMonthFrag);

            Fragment selectCalendarsFrag = new SelectVisibleCalendarsFragment();
            ft.replace(R.id.calendar_list, selectCalendarsFrag);
            mController.registerEventHandler(
                    R.id.calendar_list, (EventHandler) selectCalendarsFrag);
        }
        if (!mShowCalendarControls || viewType == ViewType.EDIT) {
            mMiniMonth.setVisibility(View.GONE);
            mCalendarsList.setVisibility(View.GONE);
        }

        EventInfo info = null;
        if (viewType == ViewType.EDIT) {
            mPreviousView = GeneralPreferences.getSharedPreferences(this).getInt(
                    GeneralPreferences.KEY_START_VIEW, GeneralPreferences.DEFAULT_START_VIEW);

            long eventId = -1;
            Intent intent = getIntent();
            Uri data = intent.getData();
            if (data != null) {
                try {
                    eventId = Long.parseLong(data.getLastPathSegment());
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        Log.d(TAG, "Create new event");
                    }
                }
            } else if (icicle != null && icicle.containsKey(BUNDLE_KEY_EVENT_ID)) {
                eventId = icicle.getLong(BUNDLE_KEY_EVENT_ID);
            }

            long begin = intent.getLongExtra(EXTRA_EVENT_BEGIN_TIME, -1);
            long end = intent.getLongExtra(EXTRA_EVENT_END_TIME, -1);
            info = new EventInfo();
            if (end != -1) {
                info.endTime = new Time();
                info.endTime.set(end);
            }
            if (begin != -1) {
                info.startTime = new Time();
                info.startTime.set(begin);
            }
            info.id = eventId;
            // We set the viewtype so if the user presses back when they are
            // done editing the controller knows we were in the Edit Event
            // screen. Likewise for eventId
            mController.setViewType(viewType);
            mController.setEventId(eventId);
        } else {
            ///M:If current view is same as the next view.don't change the mPreviousView
            ///so when prefers change,it can handle right  whether back to previous view or 
            ///finish when press back key.@{
            if (mCurrentView != viewType) {
                mPreviousView = viewType;
            } else {
                LogUtil.v(TAG, "don't modify mPreviousView's value.mCurrentView:" + mCurrentView + ",viewType:"
                        + viewType + ",mPreviousView:" + mPreviousView);
            }
            ///@}

        }

        setMainPane(ft, R.id.main_pane, viewType, timeMillis, true);
        ft.commit(); // this needs to be after setMainPane()

        Time t = new Time(mTimeZone);
        t.set(timeMillis);
        if (viewType == ViewType.AGENDA && icicle != null) {
            mController.sendEvent(this, EventType.GO_TO, t, null,
                    icicle.getLong(BUNDLE_KEY_EVENT_ID, -1), viewType);
        } else if (viewType != ViewType.EDIT) {
            mController.sendEvent(this, EventType.GO_TO, t, null, -1, viewType);
        }
    }

    @Override
    public void onBackPressed() {
        Log.i("jiating", "onBackPressed....");

        if (mCurrentView == ViewType.EDIT || mBackToPreviousView) {
            mController.sendEvent(this, EventType.GO_TO, null, null, -1, mPreviousView);
        } else {
            super.onBackPressed();
        }
    }

//Gionee jiating 2013-03-26 modify for CR00000000      cancle menu begin    
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//       
//        mOptionsMenu = menu;
//        getMenuInflater().inflate(R.menu.all_in_one_title_bar, menu);
//
//        // Add additional options (if any).
//        Integer extensionMenuRes = mExtensions.getExtensionMenuResource(menu);
//        if (extensionMenuRes != null) {
//            getMenuInflater().inflate(extensionMenuRes, menu);
//        }
//
//        mSearchMenu = menu.findItem(R.id.action_search);
//        mSearchView = (SearchView) mSearchMenu.getActionView();
//        if (mSearchView != null) {
//            Utils.setUpSearchView(mSearchView, this);
//            mSearchView.setOnQueryTextListener(this);
//            mSearchView.setOnSuggestionListener(this);
//        }
//        */
//        /**
//         * M: If in search mode, enter @{
//         */
//        if (mIsInSearchMode) {
//            enterSearchMode();
//        }
//        // Note: we should set search string after enterSearchMode(), because enterSearchMode() will
//        // set it to null
//        if (mSearchView != null) {
//            // restore search string to UI
//            mSearchView.setQuery(mSearchString, false);
//        }
//        /** @} */
//        // Hide the "show/hide controls" button if this is a phone
//        // or the view type is "Month" or "Agenda".
//
//        mControlsMenu = menu.findItem(R.id.action_hide_controls);
//        if (!mShowCalendarControls) {
//            if (mControlsMenu != null) {
//                mControlsMenu.setVisible(false);
//                mControlsMenu.setEnabled(false);
//            }
//        } else if (mControlsMenu != null && mController != null
//                    && (mController.getViewType() == ViewType.MONTH ||
//                        mController.getViewType() == ViewType.AGENDA)) {
//            mControlsMenu.setVisible(false);
//            mControlsMenu.setEnabled(false);
//        } else if (mControlsMenu != null){
//            mControlsMenu.setTitle(mHideControls ? mShowString : mHideString);
//        }
////Gionee jiating 2013-03-06 modify for CR00000000      delete this item begin
////        MenuItem menuItem = menu.findItem(R.id.action_today);
////Gionee jiating 2013-03-06 modify for CR00000000       delete this item end
//        if (Utils.isJellybeanOrLater()) {
//            // replace the default top layer drawable of the today icon with a
//            // custom drawable that shows the day of the month of today
////Gionee jiating 2013-03-06 modify for CR00000000       delete this item begin
////        	LayerDrawable icon = (LayerDrawable) menuItem.getIcon();
//        	
////            Utils.setTodayIcon(icon, this, mTimeZone);
//        	//Gionee jiating 2013-03-06 modify for CR00000000      delete this item end
//        } else {
////Gionee jiating 2013-03-06 modify for CR00000000      delete this item begin
////            menuItem.setIcon(R.drawable.ic_menu_today_no_date_holo_light);
////Gionee jiating 2013-03-06 modify for CR00000000       delete this item end
//        }
//
//        ///M: options menu extension @{
//        mOptionsMenuExt.onCreateOptionsMenu(menu);
//     
//        ///@}
//        return true;
//    }

    //Gionee jiating 2013-03-26 modify for CR00000000      cancle menu end
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /**
         * M: Exit search mode if one non-search item is selected, otherwise, enter search mode@{
         */
        if (item.getItemId() != R.id.action_search) {
            exitSearchMode();
        } else {
            enterSearchMode();
        }
        /** @} */
        Time t = null;
        int viewType = ViewType.CURRENT;
        long extras = CalendarController.EXTRA_GOTO_TIME;
        final int itemId = item.getItemId();
        if (itemId == R.id.action_refresh) {
            mController.refreshCalendars();
            return true;
        } else if (itemId == R.id.action_today) {
            viewType = ViewType.CURRENT;
            t = new Time(mTimeZone);
            t.setToNow();
            extras |= CalendarController.EXTRA_GOTO_TODAY;
        } else if (itemId == R.id.action_create_event) {
            t = new Time();
            ///M: modify for month view, if user want to create event from month view, just set now to start time.@{
            int viewtype = mController.getViewType();
            if (viewtype == ViewType.MONTH) {
                t.setToNow();
            } else {
                t.set(mController.getTime());
            }
            t.second = 0;
            ///@}
            if (t.minute > 30) {
                t.hour++;
                t.minute = 0;
            } else if (t.minute > 0 && t.minute < 30) {
                t.minute = 30;
            }
            mController.sendEventRelatedEvent(
                    this, EventType.CREATE_EVENT, -1, t.toMillis(true), 0, 0, 0, -1);
            return true;
        } else if (itemId == R.id.action_select_visible_calendars) {
            mController.sendEvent(this, EventType.LAUNCH_SELECT_VISIBLE_CALENDARS, null, null,
                    0, 0);
            return true;
        } else if (itemId == R.id.action_settings) {
            mController.sendEvent(this, EventType.LAUNCH_SETTINGS, null, null, 0, 0);
            return true;
        } else if (itemId == R.id.action_hide_controls) {
            mHideControls = !mHideControls;
            Utils.setSharedPreference(
                    this, GeneralPreferences.KEY_SHOW_CONTROLS, !mHideControls);
            item.setTitle(mHideControls ? mShowString : mHideString);
            if (!mHideControls) {
                mMiniMonth.setVisibility(View.VISIBLE);
                mCalendarsList.setVisibility(View.VISIBLE);
                mMiniMonthContainer.setVisibility(View.VISIBLE);
            }
            final ObjectAnimator slideAnimation = ObjectAnimator.ofInt(this, "controlsOffset",
                    mHideControls ? 0 : mControlsAnimateWidth,
                    mHideControls ? mControlsAnimateWidth : 0);
            slideAnimation.setDuration(mCalendarControlsAnimationTime);
            ObjectAnimator.setFrameDelay(0);
            slideAnimation.start();
            return true;
        } else if (itemId == R.id.action_search) {
            return false;
            /// M: function go to @{
        } else if (itemId == R.id.action_go_to) {
            launchDatePicker();
            return true;
            /// @}
        } else {
            ///M: extension of options menu @{
            if (mOptionsMenuExt.onOptionsItemSelected(item.getItemId())) {
                return true;
            }
            ///@}
            return mExtensions.handleItemSelected(item, this);
        }
        mController.sendEvent(this, EventType.GO_TO, t, null, t, -1, viewType, extras, null, null);
        return true;
    }

    /**
     * Sets the offset of the controls on the right for animating them off/on
     * screen. ProGuard strips this if it's not in proguard.flags
     *
     * @param controlsOffset The current offset in pixels
     */
    public void setControlsOffset(int controlsOffset) {
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            mMiniMonth.setTranslationX(controlsOffset);
            mCalendarsList.setTranslationX(controlsOffset);
            mControlsParams.width = Math.max(0, mControlsAnimateWidth - controlsOffset);
            mMiniMonthContainer.setLayoutParams(mControlsParams);
        } else {
            mMiniMonth.setTranslationY(controlsOffset);
            mCalendarsList.setTranslationY(controlsOffset);
            if (mVerticalControlsParams == null) {
                mVerticalControlsParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, mControlsAnimateHeight);
            }
            mVerticalControlsParams.height = Math.max(0, mControlsAnimateHeight - controlsOffset);
            mMiniMonthContainer.setLayoutParams(mVerticalControlsParams);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(GeneralPreferences.KEY_WEEK_START_DAY)) {
            if (mPaused) {
                mUpdateOnResume = true;
            } else {
                initFragments(mController.getTime(), mController.getViewType(), null);
            }
            return;
        }

        boolean isPeriod = Utils.displayPeriod(mContext);

        if (!isPeriod && (AuroraCalendarViewFilterActivity.FESTIVAL_KEY.equals(key) ||
        		AuroraCalendarViewFilterActivity.HAPPY_KEY.equals(key) ||
        		AuroraCalendarViewFilterActivity.LUNAR_KEY.equals(key) ||
        		AuroraCalendarViewFilterActivity.WORK_KEY.equals(key))) {
            if (mPaused) {
                mUpdateOnResume = true;
            } else {
                initFragments(mController.getTime(), mController.getViewType(), null);
            }
            return;
        }

        if (isPeriod && (Utils.PERIOD_TIME.equals(key) || Utils.PERIOD_CYCLE.equals(key) || 
        		Utils.PERIOD_CLEAR_FLAG.equals(key))) {
            if (mPaused) {
                mUpdateOnResume = true;
            }/* else {
                initFragments(mController.getTime(), mController.getViewType(), null);
            }*/
        }
    }

    private void setMainPane(
            FragmentTransaction ft, int viewId, int viewType, long timeMillis, boolean force) {
        if (mOnSaveInstanceStateCalled) {
            return;
        }
        if (!force && mCurrentView == viewType) {
            return;
        }

        // Remove this when transition to and from month view looks fine.
        boolean doTransition = viewType != ViewType.MONTH && mCurrentView != ViewType.MONTH;
        FragmentManager fragmentManager = getFragmentManager();
        // Check if our previous view was an Agenda view
        // TODO remove this if framework ever supports nested fragments
        //Gionee <jiating> <2013-04-23>  modify for CR00000000      calcle ViewType.AGENDA begin
//        if (mCurrentView == ViewType.AGENDA) {
//            // If it was, we need to do some cleanup on it to prevent the
//            // edit/delete buttons from coming back on a rotation.
//            Fragment oldFrag = fragmentManager.findFragmentById(viewId);
//				//Gionee <jiating>  <2013-04-18>  modify for CR00000000       GNAgendaFragment instead of AgendaFragment begin
//            if (oldFrag instanceof GNAgendaFragment) {
//                ((GNAgendaFragment) oldFrag).removeFragments(fragmentManager);
//            }
//				//Gionee <jiating>  <2013-04-18>  modify for CR00000000     GNAgendaFragment instead of AgendaFragment end
//        }
        //Gionee <jiating> <2013-04-23>  modify for CR00000000      calcle ViewType.AGENDA end
        if (viewType != mCurrentView) {
            // The rules for this previous view are different than the
            // controller's and are used for intercepting the back button.
            if (mCurrentView != ViewType.EDIT && mCurrentView > 0) {
                mPreviousView = mCurrentView;
            }
            mCurrentView = viewType;
        }
        // Create new fragment
        Fragment frag = null;
        Fragment secFrag = null;
        switch (viewType) {
            case ViewType.AGENDA:
                //Gionee <jiating>  <2013-04-11> modify for CR00000000    new actionbar design don't use mActionBarMenuSpinnerAdapter begin
//                if (mActionBar != null && (mActionBar.getSelectedTab() != mAgendaTab)) {
//                    mActionBar.selectTab(mAgendaTab);
//                }
//                if (mActionBarMenuSpinnerAdapter != null) {
//                    mActionBar.setSelectedNavigationItem(CalendarViewAdapter.AGENDA_BUTTON_INDEX);
//                }
                //Gionee <jiating>  <2013-04-11> modify for CR00000000      new actionbar design don't use mActionBarMenuSpinnerAdapter end
                //Gionee <jiating>  <2013-04-18>  modify for CR00000000       GNAgendaFragment instead of AgendaFragment begin
                frag = new GNAgendaFragment(timeMillis, false);
                //Gionee <jiating>  <2013-04-18>  modify for CR00000000       GNAgendaFragment instead of AgendaFragment end
                break;
            case ViewType.DAY:
                //Gionee <jiating>  <2013-04-11> modify for CR00000000      new actionbar design don't use mActionBarMenuSpinnerAdapter begin
//                if (mActionBar != null && (mActionBar.getSelectedTab() != mDayTab)) {
//                    mActionBar.selectTab(mDayTab);
//                }
//                if (mActionBarMenuSpinnerAdapter != null) {
//                    mActionBar.setSelectedNavigationItem(CalendarViewAdapter.DAY_BUTTON_INDEX);
//                }
                //Gionee <jiating>  <2013-04-11> modify for CR00000000      new actionbar design don't use mActionBarMenuSpinnerAdapter end
                //Gionee <jiating>  <2013-04-11> modify for CR00000000      set  bottom dayview bg begin
                //Gionee <jiating>  <2013-04-11> modify for CR00000000      set  bottom dayview bg end
                /// M: pass in the context
                //Gionee <pengwei>  <2013-04-12> modify for CR00000000       DayView begin
                frag = new GNDayFragment(this, timeMillis, 1);
                //Gionee <pengwei>  <2013-04-12> modify for CR00000000       DayView end
                break;
            case ViewType.WEEK:
                //Gionee <jiating>  <2013-04-11> modify for CR00000000    new actionbar design don't use mActionBarMenuSpinnerAdapter begin
//                if (mActionBar != null && (mActionBar.getSelectedTab() != mWeekTab)) {
//                    mActionBar.selectTab(mWeekTab);
//                }
//                if (mActionBarMenuSpinnerAdapter != null) {
//                    mActionBar.setSelectedNavigationItem(CalendarViewAdapter.WEEK_BUTTON_INDEX);
//                }
                //Gionee <jiating>  <2013-04-11> modify for CR00000000     new actionbar design don't use mActionBarMenuSpinnerAdapter end
                //Gionee <jiating>  <2013-04-11> modify for CR00000000        set  bottom dayview bg begin
                //Gionee <jiating>  <2013-04-11> modify for CR00000000       set  bottom dayview bg end
                /// M: pass in the context
                frag = new DayFragment(this, timeMillis, 7);
                break;
            case ViewType.MONTH:
                //Gionee <jiating>  <2013-04-11> modify for CR00000000      new actionbar design don't use mActionBarMenuSpinnerAdapter begin
//                if (mActionBar != null && (mActionBar.getSelectedTab() != mMonthTab)) {
//                    mActionBar.selectTab(mMonthTab);
//                }
//                if (mActionBarMenuSpinnerAdapter != null) {
//                    mActionBar.setSelectedNavigationItem(CalendarViewAdapter.MONTH_BUTTON_INDEX);
//                }
                //Gionee <jiating>  <2013-04-11> modify for CR00000000      new actionbar design don't use mActionBarMenuSpinnerAdapter end
                //Gionee <jiating>  <2013-04-11> modify for CR00000000     set  bottom dayview bg begin
                //Gionee <jiating>  <2013-04-11> modify for CR00000000       set  bottom dayview bg end
                frag = new MonthByWeekFragment(timeMillis, false);
                if (mShowAgendaWithMonth) {
                    //Gionee <jiating>  <2013-04-18>  modify for CR00000000      GNAgendaFragment instead of AgendaFragment begin
                    secFrag = new GNAgendaFragment(timeMillis, false);
                    //Gionee <jiating>  <2013-04-18>  modify for CR00000000      GNAgendaFragment instead of AgendaFragment end
                }
                break;
            default:
                throw new IllegalArgumentException(
                        "Must be Agenda, Day, Week, or Month ViewType, not " + viewType);
        }

        // Update the current view so that the menu can update its look according to the
        // current view.
        ////Gionee <jiating>  <2013-04-11> modify for CR00000000      new actionbar design don't use mActionBarMenuSpinnerAdapter begin
//        if (mActionBarMenuSpinnerAdapter != null) {
//            mActionBarMenuSpinnerAdapter.setMainView(viewType);
//            if (!mIsTabletConfig) {
//                mActionBarMenuSpinnerAdapter.setTime(timeMillis);
//            }
//        }
        if (mGNDateTextUtils != null) {
            mGNDateTextUtils.setTime(timeMillis);
            String[] date = mGNDateTextUtils.updateDateYearAndMonthTextByView();
            Log.i("jiating", "setMainPane.......date[0]=" + date[0] + "date[1]=" + date[1]);
            setActionBarTimeViewValue(date[0], date[1]);
        }
        ////Gionee <jiating>  <2013-04-11> modify for CR00000000      new actionbar design don't use mActionBarMenuSpinnerAdapter end

        // Show date only on tablet configurations in views different than Agenda
        if (!mIsTabletConfig) {
            mDateRange.setVisibility(View.GONE);
        } else if (viewType != ViewType.AGENDA) {
            mDateRange.setVisibility(View.VISIBLE);
        } else {
            mDateRange.setVisibility(View.GONE);
        }

        // Clear unnecessary buttons from the option menu when switching from the agenda view
        if (viewType != ViewType.AGENDA) {
            clearOptionsMenu();
        }

        boolean doCommit = false;
        if (ft == null) {
            doCommit = true;
            ft = fragmentManager.beginTransaction();
        }

        if (doTransition) {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        }

        ft.replace(viewId, frag);
        if (mShowAgendaWithMonth) {

            // Show/hide secondary fragment

            if (secFrag != null) {
                ft.replace(R.id.secondary_pane, secFrag);
                mSecondaryPane.setVisibility(View.VISIBLE);
            } else {
                mSecondaryPane.setVisibility(View.GONE);
                Fragment f = fragmentManager.findFragmentById(R.id.secondary_pane);
                if (f != null) {
                    ft.remove(f);
                }
                mController.deregisterEventHandler(R.id.secondary_pane);
            }
        }
        if (DEBUG) {
            Log.d(TAG, "Adding handler with viewId " + viewId + " and type " + viewType);
        }
        // If the key is already registered this will replace it
        mController.registerEventHandler(viewId, (EventHandler) frag);
        if (secFrag != null) {
            mController.registerEventHandler(viewId, (EventHandler) secFrag);
        }

        if (doCommit) {
            if (DEBUG) {
                Log.d(TAG, "setMainPane AllInOne=" + this + " finishing:" + this.isFinishing());
            }
            ft.commit();
        }
    }

    private void setTitleInActionBar(EventInfo event) {
        if (event.eventType != EventType.UPDATE_TITLE || mActionBar == null) {
            return;
        }

        final long start = event.startTime.toMillis(false /* use isDst */);
        long end;
        if (event.endTime != null) {
            end = event.endTime.toMillis(false /* use isDst */);
        } else {
            end = start;
        }
        /// M: make sure end >= start. @{
        if (start > end) {
            end = Utils.getLastDisplayTimeInCalendar(this).toMillis(false);
        }
        /// @}
        final String msg = Utils.formatDateRange(this, start, end, (int) event.extraLong);
        CharSequence oldDate = mDateRange.getText();
        mDateRange.setText(msg);
        updateSecondaryTitleFields(event.selectedTime != null ? event.selectedTime.toMillis(true)
                : start);
        if (!TextUtils.equals(oldDate, msg)) {
            mDateRange.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            if (mShowWeekNum && mWeekTextView != null) {
                mWeekTextView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            }
        }
    }

    private void updateSecondaryTitleFields(long visibleMillisSinceEpoch) {
        mShowWeekNum = Utils.getShowWeekNumber(this);
        mTimeZone = Utils.getTimeZone(this, mHomeTimeUpdater);
        if (visibleMillisSinceEpoch != -1) {
            int weekNum = Utils.getWeekNumberFromTime(visibleMillisSinceEpoch, this);
            mWeekNum = weekNum;
        }

        if (mShowWeekNum && (mCurrentView == ViewType.WEEK) && mIsTabletConfig
                && mWeekTextView != null) {
            String weekString = getResources().getQuantityString(R.plurals.weekN, mWeekNum,
                    mWeekNum);
            mWeekTextView.setText(weekString);
            mWeekTextView.setVisibility(View.VISIBLE);
        } else if (visibleMillisSinceEpoch != -1 && mWeekTextView != null
                && mCurrentView == ViewType.DAY && mIsTabletConfig) {
            Time time = new Time(mTimeZone);
            time.set(visibleMillisSinceEpoch);
            int julianDay = Time.getJulianDay(visibleMillisSinceEpoch, time.gmtoff);
            time.setToNow();
            int todayJulianDay = Time.getJulianDay(time.toMillis(false), time.gmtoff);
            String dayString = Utils.getDayOfWeekString(julianDay, todayJulianDay,
                    visibleMillisSinceEpoch, this);
            mWeekTextView.setText(dayString);
            mWeekTextView.setVisibility(View.VISIBLE);
        } else if (mWeekTextView != null && (!mIsTabletConfig || mCurrentView != ViewType.DAY)) {
            mWeekTextView.setVisibility(View.GONE);
        }

        // Gionee <jiangxiao> <2013-06-25> modify for CR00829173 begin
        // don't show this view in day view & agenda view
        if (mHomeTime != null
                && (mCurrentView == ViewType.WEEK) // mCurrentView == ViewType.DAY || mCurrentView == ViewType.AGENDA
                && !TextUtils.equals(mTimeZone, Time.getCurrentTimezone())) {
            // Gionee <jiangxiao> <2013-06-25> modify for CR00829173 end
            Time time = new Time(mTimeZone);
            time.setToNow();
            long millis = time.toMillis(true);
            boolean isDST = time.isDst != 0;
            int flags = DateUtils.FORMAT_SHOW_TIME;
            if (DateFormat.is24HourFormat(this)) {
                flags |= DateUtils.FORMAT_24HOUR;
            }
            // Formats the time as
            String timeString = (new StringBuilder(
                    Utils.formatDateRange(this, millis, millis, flags))).append(" ").append(
                    TimeZone.getTimeZone(mTimeZone).getDisplayName(
                            isDST, TimeZone.LONG, Locale.getDefault())).toString();
            mHomeTime.setText(timeString);
            mHomeTime.setVisibility(View.VISIBLE);
            // Update when the minute changes
            mHomeTime.removeCallbacks(mHomeTimeUpdater);
            mHomeTime.postDelayed(
                    mHomeTimeUpdater,
                    DateUtils.MINUTE_IN_MILLIS - (millis % DateUtils.MINUTE_IN_MILLIS));
        } else if (mHomeTime != null) {
            mHomeTime.setVisibility(View.GONE);
        }
    }

    @Override
    public long getSupportedEventTypes() {
        return EventType.GO_TO | EventType.VIEW_EVENT | EventType.UPDATE_TITLE;
    }

    @Override
    public void handleEvent(EventInfo event) {
        long displayTime = -1;
        if (event.eventType == EventType.GO_TO) {
            if ((event.extraLong & CalendarController.EXTRA_GOTO_BACK_TO_PREVIOUS) != 0) {
                mBackToPreviousView = true;
            } else if (event.viewType != mController.getPreviousViewType()
                    && event.viewType != ViewType.EDIT) {
                // Clear the flag is change to a different view type
                mBackToPreviousView = false;
            }

            setMainPane(
                    null, R.id.main_pane, event.viewType, event.startTime.toMillis(false), false);
            if (mSearchView != null) {
                mSearchView.clearFocus();
            }
            if (mShowCalendarControls) {
                int animationSize = (mOrientation == Configuration.ORIENTATION_LANDSCAPE) ?
                        mControlsAnimateWidth : mControlsAnimateHeight;
                boolean noControlsView = event.viewType == ViewType.MONTH || event.viewType == ViewType.AGENDA;
                if (mControlsMenu != null) {
                    mControlsMenu.setVisible(!noControlsView);
                    mControlsMenu.setEnabled(!noControlsView);
                }
                if (noControlsView || mHideControls) {
                    // hide minimonth and calendar frag
                    mShowSideViews = false;
                    if (!mHideControls) {
                        final ObjectAnimator slideAnimation = ObjectAnimator.ofInt(this,
                                "controlsOffset", 0, animationSize);
                        slideAnimation.addListener(mSlideAnimationDoneListener);
                        slideAnimation.setDuration(mCalendarControlsAnimationTime);
                        ObjectAnimator.setFrameDelay(0);
                        slideAnimation.start();
                    } else {
                        mMiniMonth.setVisibility(View.GONE);
                        mCalendarsList.setVisibility(View.GONE);
                        mMiniMonthContainer.setVisibility(View.GONE);
                    }
                } else {
                    // show minimonth and calendar frag
                    mShowSideViews = true;
                    mMiniMonth.setVisibility(View.VISIBLE);
                    mCalendarsList.setVisibility(View.VISIBLE);
                    mMiniMonthContainer.setVisibility(View.VISIBLE);
                    if (!mHideControls &&
                            (mController.getPreviousViewType() == ViewType.MONTH ||
                                    mController.getPreviousViewType() == ViewType.AGENDA)) {
                        final ObjectAnimator slideAnimation = ObjectAnimator.ofInt(this,
                                "controlsOffset", animationSize, 0);
                        slideAnimation.setDuration(mCalendarControlsAnimationTime);
                        ObjectAnimator.setFrameDelay(0);
                        slideAnimation.start();
                    }
                }
            }
            displayTime = event.selectedTime != null ? event.selectedTime.toMillis(true)
                    : event.startTime.toMillis(true);
            ////Gionee <jiating>  <2013-04-11> modify for CR00000000       new actionbar design don't use mActionBarMenuSpinnerAdapter begin
            // if (!mIsTabletConfig) {
            //     mActionBarMenuSpinnerAdapter.setTime(displayTime);
            // }
            ////Gionee <jiating>  <2013-04-11> modify for CR00000000    new actionbar design don't use mActionBarMenuSpinnerAdapter end
        } else if (event.eventType == EventType.VIEW_EVENT) {

            // If in Agenda view and "show_event_details_with_agenda" is "true",
            // do not create the event info fragment here, it will be created by the Agenda
            // fragment

            if (mCurrentView == ViewType.AGENDA && mShowEventDetailsWithAgenda) {
                if (event.startTime != null && event.endTime != null) {
                    // Event is all day , adjust the goto time to local time
                    if (event.isAllDay()) {
                        Utils.convertAlldayUtcToLocal(
                                event.startTime, event.startTime.toMillis(false), mTimeZone);
                        Utils.convertAlldayUtcToLocal(
                                event.endTime, event.endTime.toMillis(false), mTimeZone);
                    }
                    mController.sendEvent(this, EventType.GO_TO, event.startTime, event.endTime,
                            event.selectedTime, event.id, ViewType.AGENDA,
                            CalendarController.EXTRA_GOTO_TIME, null, null);
                } else if (event.selectedTime != null) {
                    mController.sendEvent(this, EventType.GO_TO, event.selectedTime,
                            event.selectedTime, event.id, ViewType.AGENDA);
                }
            } else {
                // TODO Fix the temp hack below: && mCurrentView !=
                // ViewType.AGENDA
                if (event.selectedTime != null && mCurrentView != ViewType.AGENDA) {
                    mController.sendEvent(this, EventType.GO_TO, event.selectedTime,
                            event.selectedTime, -1, ViewType.CURRENT);
                }
                int response = event.getResponse();
                if ((mCurrentView == ViewType.AGENDA && mShowEventInfoFullScreenAgenda) ||
                        ((mCurrentView == ViewType.DAY || (mCurrentView == ViewType.WEEK) ||
                                mCurrentView == ViewType.MONTH) && mShowEventInfoFullScreen)) {
                    // start event info as activity
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri eventUri = ContentUris.withAppendedId(Events.CONTENT_URI, event.id);
                    intent.setData(eventUri);
                    intent.setClass(this, EventInfoActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra(EXTRA_EVENT_BEGIN_TIME, event.startTime.toMillis(false));
                    intent.putExtra(EXTRA_EVENT_END_TIME, event.endTime.toMillis(false));
                    intent.putExtra(ATTENDEE_STATUS, response);
                    startActivity(intent);
                    /// M: dismiss any notification of the given event, if any one of them exists
                    // make sure event.id != -1
                    // @{
                    AlertUtils.removeEventNotification(this, event.id,
                            event.startTime != null ? event.startTime.toMillis(false) : -1,
                            event.endTime != null ? event.endTime.toMillis(false) : -1);
                    /// @}
                } else {
                    // start event info as a dialog
                    AuroraEventInfoFragment fragment = new AuroraEventInfoFragment(this,
                            event.id, event.startTime.toMillis(false),
                            event.endTime.toMillis(false), response, true,
                            AuroraEventInfoFragment.DIALOG_WINDOW_STYLE);
                    fragment.setDialogParams(event.x, event.y, mActionBar.getHeight());
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    // if we have an old popup replace it
                    Fragment fOld = fm.findFragmentByTag(EVENT_INFO_FRAGMENT_TAG);
                    if (fOld != null && fOld.isAdded()) {
                        ft.remove(fOld);
                    }
                    ft.add(fragment, EVENT_INFO_FRAGMENT_TAG);
                    ft.commit();
                }
            }
            displayTime = event.startTime.toMillis(true);
        } else if (event.eventType == EventType.UPDATE_TITLE) {
            setTitleInActionBar(event);
            //Gionee <jiating>  <2013-04-11> modify for new actionbar danimationesign don't use mActionBarMenuSpinnerAdapter begin
//            if (!mIsTabletConfig) {
//                mActionBarMenuSpinnerAdapter.setTime(mController.getTime());
//            }
            
            /*if(!mIsTabletConfig && mGNDateTextUtils!=null){
                mGNDateTextUtils.setTime(mController.getTime());
                String []date=mGNDateTextUtils.updateDateYearAndMonthTextByView();
                Log.i("jiating","handleEvent.......date[0]="+date[0]+"date[1]="+date[1]);
                setActionBarTimeViewValue(date[0],date[1]);
            }*/
            if (!mIsTabletConfig && mGNDateTextUtils != null) {
                if (mLastTimeMillis == -1) {
                    mLastTimeMillis = mController.getTime();
                }

                long selectTimeMillis = mController.getTime();

                Time lastTime = new Time();
                lastTime.set(mLastTimeMillis);

                Time thisTime = new Time();
                thisTime.set(selectTimeMillis);

                int compareMonth = GNCalendarUtils.compareMonth(thisTime, lastTime);

                if (compareMonth > 0) {
                    mGNDateTextUtils.setTime(mLastTimeMillis);
                    String[] lastDate = mGNDateTextUtils.updateDateYearAndMonthTextByView();
                    mGNDateTextUtils.setTime(selectTimeMillis);
                    final String[] selectDate = mGNDateTextUtils.updateDateYearAndMonthTextByView();
                    // mActionBarTimeYearView.setText(lastDate[0] + "年" + lastDate[1] + "月");
                    // mActionBarTimeMonthView.setText(selectDate[0] + "年" + selectDate[1] + "月");

                    String lastDateString = lastDate[0] + "年" + lastDate[1] + "月";
                    String selectDateString = selectDate[0] + "年" + selectDate[1] + "月";
                    if (!isChineseEnvironment) {
                        lastDateString = lastDate[0] + "-" + lastDate[1];
                        selectDateString = selectDate[0] + "-" + selectDate[1];
                    }
                    mActionBarTimeYearView.setText(lastDateString);
                    mActionBarTimeMonthView.setText(selectDateString);

                    TranslateAnimation gotoFutureAnimation = (TranslateAnimation) GNAnimationutils.gotoFuture(this);
                    gotoFutureAnimation.setAnimationListener(new Animation.AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            String currentDateString = selectDate[0] + "年" + selectDate[1] + "月";
                            if (!isChineseEnvironment) {
                                currentDateString = selectDate[0] + "-" + selectDate[1];
                            }
                            mActionBarTimeYearView.setText(currentDateString);
                        }
                    });

                    AnimationSet animationSet = new AnimationSet(false);
                    animationSet.addAnimation(GNAnimationutils.changeAlphaAnimation(this));
                    animationSet.addAnimation(gotoFutureAnimation);

                    mActionBarTimeYearView.startAnimation(animationSet/*gotoFutureAnimation*/);
                    mActionBarTimeMonthView.setVisibility(View.GONE);
                    mActionBarTimeMonthView.startAnimation(GNAnimationutils.gotoFuture(this));
                } else if (compareMonth < 0) {
                    mGNDateTextUtils.setTime(mLastTimeMillis);
                    String[] lastDate = mGNDateTextUtils.updateDateYearAndMonthTextByView();
                    mGNDateTextUtils.setTime(selectTimeMillis);
                    String[] selectDate = mGNDateTextUtils.updateDateYearAndMonthTextByView();
                    // mActionBarTimeYearView.setText(selectDate[0] + "年" + selectDate[1] + "月");
                    // mActionBarTimeMonthView.setText(lastDate[0] + "年" + lastDate[1] + "月");

                    String lastDateString = lastDate[0] + "年" + lastDate[1] + "月";
                    String selectDateString = selectDate[0] + "年" + selectDate[1] + "月";
                    if (!isChineseEnvironment) {
                        lastDateString = lastDate[0] + "-" + lastDate[1];
                        selectDateString = selectDate[0] + "-" + selectDate[1];
                    }
                    mActionBarTimeYearView.setText(selectDateString);
                    mActionBarTimeMonthView.setText(lastDateString);

                    AnimationSet animationSet = new AnimationSet(false);
                    animationSet.addAnimation(GNAnimationutils.changeAlphaAnimation(this));
                    animationSet.addAnimation(GNAnimationutils.gotoLast(this));

                    mActionBarTimeYearView.startAnimation(GNAnimationutils.gotoLast(this));
                    mActionBarTimeMonthView.setVisibility(View.GONE);
                    mActionBarTimeMonthView.startAnimation(animationSet/*GNAnimationutils.gotoLast(this)*/);
                } else {
                    mGNDateTextUtils.setTime(selectTimeMillis);
                }

                mLastTimeMillis = mController.getTime();
            }

            Time currentTime = new Time(mTimeZone);
            currentTime.setToNow();
            currentTime.normalize(true);
            Time controllerTime = new Time(mTimeZone);
            controllerTime.set(mController.getTime());
            controllerTime.normalize(true);

            if (!GNCalendarUtils.isIdenticalDate(currentTime, controllerTime)) {

                if (!mActionBarReturntodayButton.isShown() && mActionBarReturntodayButton.getVisibility() == View.GONE) {
                    mActionBarReturntodayButton.setVisibility(View.VISIBLE);
                    mActionBarReturntodayButton.setAnimation(GNAnimationutils.showTodayAnimation(this));
                }
            } else {
                mActionBarReturntodayButton.setVisibility(View.GONE);
            }
            //Gionee <jiating>  <2013-04-11> modify for CR00000000     new actionbar design don't use mActionBarMenuSpinnerAdapter end
        }
        updateSecondaryTitleFields(displayTime);
    }

    // Needs to be in proguard whitelist
    // Specified as listener via android:onClick in a layout xml
    public void handleSelectSyncedCalendarsClicked(View v) {
        mController.sendEvent(this, EventType.LAUNCH_SETTINGS, null, null, null, 0, 0,
                CalendarController.EXTRA_GOTO_TIME, null,
                null);
    }

    @Override
    public void eventsChanged() {
        mController.sendEvent(this, EventType.EVENTS_CHANGED, null, null, -1, ViewType.CURRENT);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if ("TARDIS".equalsIgnoreCase(query)) {
            Utils.tardis();
        }
        /// M: exit search mode @{
        exitSearchMode();
        // @}
        mController.sendEvent(this, EventType.SEARCH, null, null, -1, ViewType.CURRENT, 0, query,
                getComponentName());
        return true;
    }

//Gionee <jiating>  <2013-04-11> modify for CR00000000      don't use Tab and ButtonsSpinner begin

    /**
     * @Override public void onTabSelected(Tab tab, FragmentTransaction ft) {
     * Log.w(TAG, "TabSelected AllInOne=" + this + " finishing:" + this.isFinishing());
     * if (tab == mDayTab && mCurrentView != ViewType.DAY) {
     * mController.sendEvent(this, EventType.GO_TO, null, null, -1, ViewType.DAY);
     * } else if (tab == mWeekTab && mCurrentView != ViewType.WEEK) {
     * mController.sendEvent(this, EventType.GO_TO, null, null, -1, ViewType.WEEK);
     * } else if (tab == mMonthTab && mCurrentView != ViewType.MONTH) {
     * mController.sendEvent(this, EventType.GO_TO, null, null, -1, ViewType.MONTH);
     * } else if (tab == mAgendaTab && mCurrentView != ViewType.AGENDA) {
     * mController.sendEvent(this, EventType.GO_TO, null, null, -1, ViewType.AGENDA);
     * } else {
     * Log.w(TAG, "TabSelected event from unknown tab: "
     * + (tab == null ? "null" : tab.getText()));
     * Log.w(TAG, "CurrentView:" + mCurrentView + " Tab:" + tab.toString() + " Day:" + mDayTab
     * + " Week:" + mWeekTab + " Month:" + mMonthTab + " Agenda:" + mAgendaTab);
     * }
     * }
     * @Override public void onTabReselected(Tab tab, FragmentTransaction ft) {
     * }
     * @Override public void onTabUnselected(Tab tab, FragmentTransaction ft) {
     * }
     * @Override public boolean onNavigationItemSelected(int itemPosition, long itemId) {
     * switch (itemPosition) {
     * case CalendarViewAdapter.DAY_BUTTON_INDEX:
     * if (mCurrentView != ViewType.DAY) {
     * mController.sendEvent(this, EventType.GO_TO, null, null, -1, ViewType.DAY);
     * }
     * break;
     * case CalendarViewAdapter.WEEK_BUTTON_INDEX:
     * if (mCurrentView != ViewType.WEEK) {
     * mController.sendEvent(this, EventType.GO_TO, null, null, -1, ViewType.WEEK);
     * }
     * break;
     * case CalendarViewAdapter.MONTH_BUTTON_INDEX:
     * if (mCurrentView != ViewType.MONTH) {
     * mController.sendEvent(this, EventType.GO_TO, null, null, -1, ViewType.MONTH);
     * }
     * break;
     * case CalendarViewAdapter.AGENDA_BUTTON_INDEX:
     * if (mCurrentView != ViewType.AGENDA) {
     * mController.sendEvent(this, EventType.GO_TO, null, null, -1, ViewType.AGENDA);
     * }
     * break;
     * default:
     * Log.w(TAG, "ItemSelected event from unknown button: " + itemPosition);
     * Log.w(TAG, "CurrentView:" + mCurrentView + " Button:" + itemPosition +
     * " Day:" + mDayTab + " Week:" + mWeekTab + " Month:" + mMonthTab +
     * " Agenda:" + mAgendaTab);
     * break;
     * }
     * return false;
     * }
     */
//Gionee <jiating>  <2013-04-11> modify for CR00000000      don't use Tab and ButtonsSpinner end
    @Override
    public boolean onSuggestionSelect(int position) {
        return false;
    }

    @Override
    public boolean onSuggestionClick(int position) {
        /// M: exit search mode @{
        exitSearchMode();
        // @}
        return false;
    }

    @Override
    public boolean onSearchRequested() {
        /// M: enter search mode @{
        enterSearchMode();
        // @}
        return false;
    }

    ///M: to mark that it finished one clear all events operation.
    private static boolean mIsClearEventsCompleted = false;

    ///M:#clear all events#.@{
    public static void setClearEventsCompletedStatus(boolean status) {
        mIsClearEventsCompleted = status;
    }
    ///@}

    ///M: launch date picker dialog fragment
    private static final String GOTO_FRAGMENT_TAG = "goto_frag";

    public void launchDatePicker() {
        Time t = new Time(mTimeZone);
        t.setToNow();

        int startOfWeek = Utils.getFirstDayOfWeek(this);
        // Utils returns Time days while CalendarView wants Calendar days
        if (startOfWeek == Time.SATURDAY) {
            startOfWeek = Calendar.SATURDAY;
        } else if (startOfWeek == Time.SUNDAY) {
            startOfWeek = Calendar.SUNDAY;
        } else {
            startOfWeek = Calendar.MONDAY;
        }

        GoToDatePickerDialogFragment df = GoToDatePickerDialogFragment
                .newInstance(t.year, t.month, t.monthDay, startOfWeek,
                        Utils.getShowWeekNumber(this), true, 0);
        df.show(getFragmentManager(), GOTO_FRAGMENT_TAG);
    }
    ///@}

    /**
     * M: Exit activity's search mode, control UI on action bar, set search mode flag
     */
    private void exitSearchMode() {
        mIsInSearchMode = false;
        if ((mSearchMenu != null) && mSearchMenu.isActionViewExpanded()) {
            mSearchMenu.collapseActionView();
        }
    }

    /**
     * M: Enter activity's search mode, control UI on action bar, set search mode flag
     */
    private void enterSearchMode() {
        mIsInSearchMode = true;
        if ((mSearchMenu != null) && !mSearchMenu.isActionViewExpanded()) {
            mSearchMenu.expandActionView();
        }
    }

    //Gionee <jiating>  <2013-04-11> modify for CR00000000      show all kinds of Dialog  begin
    private void showSelectDateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case DATE_SELECT_DIALOG:
                String[] curentShowDate = mGNDateTextUtils.updateDateYearMonthDayTextByView();
                int year = Integer.parseInt(curentShowDate[0]);
                int month = Integer.parseInt(curentShowDate[1]);
                int day = Integer.parseInt(curentShowDate[2]);
                //Gionee <jiating><2013-07-03> modify for CR00823772 begin
                dialog = new GNCustomTimeDialog(this, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN, onDateSetListener, year, month - 1, day);
                break;
            //Gionee <jiating><2013-07-03> modify for CR00823772 end
            default:
                break;
        }

        dialog.show();
    }

    AuroraDatePickerDialog.OnDateSetListener onDateSetListener = new AuroraDatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(AuroraDatePicker view, int year, int monthOfYear, int dayOfMonth) {

            if (year < GNCalendarUtils.MIN_YEAR_NUM || year > GNCalendarUtils.MAX_YEAR_NUM) {
                showOutOfRangeToast(R.string.time_out_of_range);
                return;
            }

            Log.i("jiating", "onDateSet-------------?" + year + " " + monthOfYear + "  " + dayOfMonth);
            String month;
            if (monthOfYear < 9) {
                month = "0" + (monthOfYear + 1);
            } else {
                month = String.valueOf(monthOfYear + 1);
            }
            setActionBarTimeViewValue(String.valueOf(year), month);

            Time t = null;
            int viewType = ViewType.CURRENT;
            viewType = ViewType.CURRENT;
            t = new Time(mTimeZone);
            t.year = year;
            t.month = monthOfYear;
            t.monthDay = dayOfMonth;
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String str = formatter.format(curDate);
            Log.i("jiating", "onDateSet-------------?" + "str=" + str);
            String[] currentTimes = str.split(":");
            t.hour = Integer.parseInt(currentTimes[0]);
            t.minute = Integer.parseInt(currentTimes[1]);
            t.normalize(true);
            mController.sendEvent(this, EventType.GO_TO, t, null, -1, viewType);
        }
    };
//Gionee <jiating>  <2013-04-11> modify for CR00000000     show all kinds of Dialog  end 

    //Gionee <jiating>  <2013-04-11> modify for CR00000000     AuroraActionBar showTimeView begin
    private void setActionBarTimeViewValue(String year, String month) {
        String monthString = year + "年" + month + "月";
        if (!isChineseEnvironment) {
            monthString = year + "-" + month;
        }
        mActionBarTimeYearView.setText(monthString);
        mActionBarTimeMonthView.setText("");
    }
//Gionee <jiating>  <2013-04-11> modify for CR00000000      AuroraActionBar showTimeView end  

    // Gionee <jiangxiao> <2013-06-19> add for CRxxxxxxxx begin
// use a unique Toast object to avoid multiple Toast popup
    private static Toast mToast = null;

    private void initToast() {
        if (mToast == null) {
            mToast = Toast.makeText(this, R.string.time_out_of_range, Toast.LENGTH_SHORT);
            // mToast = new Toast(this);
        }
    }

    public static void showOutOfRangeToast(int resId) {
        if (mToast != null) {
            mToast.setText(resId);
            mToast.show();
        }
    }
    // Gionee <jiangxiao> <2013-06-19> add for CRxxxxxxxx end

    // Gionee <jiating><2013-06-25> modify for CR00829225 begin
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        Log.i("jiating", "EditEventActivity....onConfigurationChanged");
    }
// Gionee <jiating><2013-06-25> modify for CR00829225 end 

    private void countViewGoto() {
        Statistics.onEvent(this, Statistics.DAY_VIEW_GOTO_MONTH_VIEW, (Statistics.DAY_VIEW_GOTO_MONTH_VIEW_NUM + ""));
        Statistics.onEvent(this, Statistics.DAY_VIEW_GOTO_WEEK_VIEW, (Statistics.DAY_VIEW_GOTO_WEEK_VIEW_NUM + ""));
        Statistics.onEvent(this, Statistics.MONTH_VIEW_GOTO_WEEK_VIEW, (Statistics.MONTH_VIEW_GOTO_WEEK_VIEW_NUM + ""));
        Statistics.onEvent(this, Statistics.MONTH_VIEW_GOTO_DAY_VIEW, (Statistics.MONTH_VIEW_GOTO_DAY_VIEW_NUM + ""));
        Statistics.onEvent(this, Statistics.WEEK_VIEW_GOTO_DAY_VIEW, (Statistics.WEEK_VIEW_GOTO_DAY_VIEW_NUM + ""));
        Statistics.onEvent(this, Statistics.WEEK_VIEW_GOTO_MONTH_VIEW, (Statistics.WEEK_VIEW_GOTO_MONTH_VIEW_NUM + ""));
    }

    private void clearViewGotoCount() {
        Statistics.DAY_VIEW_GOTO_MONTH_VIEW_NUM = 0;
        Statistics.DAY_VIEW_GOTO_WEEK_VIEW_NUM = 0;
        Statistics.MONTH_VIEW_GOTO_WEEK_VIEW_NUM = 0;
        Statistics.MONTH_VIEW_GOTO_DAY_VIEW_NUM = 0;
        Statistics.WEEK_VIEW_GOTO_DAY_VIEW_NUM = 0;
        Statistics.WEEK_VIEW_GOTO_MONTH_VIEW_NUM = 0;
    }

}
