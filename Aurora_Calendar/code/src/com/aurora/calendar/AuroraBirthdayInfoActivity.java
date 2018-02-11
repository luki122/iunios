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
package com.aurora.calendar;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraListView;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.android.calendar.AsyncQueryService;
import com.android.calendar.CalendarController;
import com.android.calendar.Event;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.android.calendar.CalendarController.EventType;

public class AuroraBirthdayInfoActivity extends AuroraActivity {

    public static final String BIRTHDAY_START_MILLIS = "birthday_start_millis";

    private static final int BIRTHDAY_INFO_MENU_MORE = 9;

    private static final String WHERE = Calendars.OWNER_ACCOUNT + "=?";
    private static final String[] WHERE_ARGS = {Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME};
    private static final String SORT_EVENTS_BY = "begin ASC, end DESC, title ASC";

    private static final String DISPLAY_AS_ALLDAY = "dispAllday";
    // The projection to use when querying instances to build a list of events
    public static final String[] EVENT_PROJECTION = new String[] {
            Instances.TITLE,                 // 0
            Instances.EVENT_LOCATION,        // 1
            Instances.ALL_DAY,               // 2
            Instances.DISPLAY_COLOR,         // 3 If SDK < 16, set to Instances.CALENDAR_COLOR.
            Instances.EVENT_TIMEZONE,        // 4
            Instances.EVENT_ID,              // 5
            Instances.BEGIN,                 // 6
            Instances.END,                   // 7
            Instances._ID,                   // 8
            Instances.START_DAY,             // 9
            Instances.END_DAY,               // 10
            Instances.START_MINUTE,          // 11
            Instances.END_MINUTE,            // 12
            Instances.HAS_ALARM,             // 13
            Instances.RRULE,                 // 14
            Instances.RDATE,                 // 15
            Instances.SELF_ATTENDEE_STATUS,  // 16
            Events.ORGANIZER,                // 17
            Events.GUESTS_CAN_MODIFY,        // 18
            Instances.ALL_DAY + "=1 OR (" + Instances.END + "-" + Instances.BEGIN + ")>="
            + DateUtils.DAY_IN_MILLIS + " AS " + DISPLAY_AS_ALLDAY, // 19
            Instances.OWNER_ACCOUNT,         // 20
            Instances.DTSTART,               // 21
    };

    // The indices for the projection array above.
    public static final int PROJECTION_TITLE_INDEX = 0;
    public static final int PROJECTION_LOCATION_INDEX = 1;
    public static final int PROJECTION_ALL_DAY_INDEX = 2;
    public static final int PROJECTION_COLOR_INDEX = 3;
    public static final int PROJECTION_TIMEZONE_INDEX = 4;
    public static final int PROJECTION_EVENT_ID_INDEX = 5;
    public static final int PROJECTION_BEGIN_INDEX = 6;
    public static final int PROJECTION_END_INDEX = 7;
    public static final int PROJECTION_START_DAY_INDEX = 9;
    public static final int PROJECTION_END_DAY_INDEX = 10;
    public static final int PROJECTION_START_MINUTE_INDEX = 11;
    public static final int PROJECTION_END_MINUTE_INDEX = 12;
    public static final int PROJECTION_HAS_ALARM_INDEX = 13;
    public static final int PROJECTION_RRULE_INDEX = 14;
    public static final int PROJECTION_RDATE_INDEX = 15;
    public static final int PROJECTION_SELF_ATTENDEE_STATUS_INDEX = 16;
    public static final int PROJECTION_ORGANIZER_INDEX = 17;
    public static final int PROJECTION_GUESTS_CAN_INVITE_OTHERS_INDEX = 18;
    public static final int PROJECTION_DISPLAY_AS_ALLDAY = 19;
    public static final int PROJECTION_OWNER_ACCOUNT_INDEX = 20;
    public static final int PROJECTION_DTSTART_INDEX = 21;

    static {
        if (!Utils.isJellybeanOrLater()) {
            EVENT_PROJECTION[PROJECTION_COLOR_INDEX] = Instances.CALENDAR_COLOR;
        }
    }

    private long mStartMillis;
    private int mStartDay;

    private Context mContext;
    private ContentResolver mResolver;
    private AuroraBirthdayInfoAdapter mAdapter;
    private QueryHandler mQueryHandler;
    private Cursor mCursor;
    private AuroraListView mListView;

    private ArrayList<Event> mEvents = new ArrayList<Event>();
    private String[] mConstellationArray;

    private ImageView mBirthdayBannerView;
    private TextView mBirthdayNoticeView;
    private TextView mBirthdayTodayView;
    private TextView mBirthdayYearView;
    private TextView mBirthdayMonthDayView;
    private TextView mBirthdayConstellationView;

    // private Typeface typeface1 = Typeface.createFromFile("system/fonts/DroidSansFallback.ttf");
    private Typeface typeface2 = Typeface.createFromFile("system/fonts/RobotoCondensed-Bold.ttf");

    private TranslateAnimation translate;
    private TranslateAnimation translate2;

    private class QueryHandler extends AsyncQueryService {
        public QueryHandler(Context context) {
            super(context);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            // Only set mCursor if the AuroraActivity is not finishing. Otherwise close the cursor.
            if (!isFinishing()) {
                mCursor = cursor;
                mAdapter.changeCursor(cursor);

                mBirthdayNoticeView.setText(
                        mContext.getResources().getString(R.string.aurora_birthday_notice, cursor.getCount()));

                mEvents.clear();
                Event.buildEventsFromCursor(mEvents, mCursor, mContext, mStartDay, mStartDay);
            } else {
                cursor.close();
            }
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            // Ignore
        }
    }

    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            Uri.Builder builder = Instances.CONTENT_BY_DAY_URI.buildUpon();
            ContentUris.appendId(builder, mStartDay);
            ContentUris.appendId(builder, mStartDay);
            mQueryHandler.startQuery(0, null, builder.build(), Event.EVENT_PROJECTION, WHERE, WHERE_ARGS, SORT_EVENTS_BY);
        }
    };

    private AuroraListView.AuroraBackOnClickListener mDeleteEventListener = new AuroraListView.AuroraBackOnClickListener() {
        public void auroraOnClick(int position) {
            if (position >= mEvents.size()) return;

            Event event = mEvents.get(position);
            sendEvent(EventType.DELETE_EVENT, event);
        }

        public void auroraPrepareDraged(int positon) {
        
        }

        public void auroraDragedSuccess(int position) {
        
        }

        public void auroraDragedUnSuccess(int position) {
        
        }
    };

    private AnimationListener mAnimationListener = new AnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            mBirthdayBannerView.startAnimation(translate2);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

    };

    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
        public void onAuroraActionBarItemClicked(int itemId) {
            switch (itemId) {
            case BIRTHDAY_INFO_MENU_MORE:
                showAuroraMenu();
                break;
            default:
                break;
            }
        }
    };

    private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {
        public void auroraMenuItemClick(int arg0) {
            switch (arg0) {
            case R.id.event_edit: {
                break;
            }
            case R.id.event_delete: {
                break;
            }
            default:
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mContext = this;
        mResolver = mContext.getContentResolver();

        Intent intent = getIntent();
        if (icicle != null) {
            mStartMillis = icicle.getInt(BIRTHDAY_START_MILLIS);
        } else if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            mStartMillis = intent.getLongExtra(BIRTHDAY_START_MILLIS, System.currentTimeMillis());
        }
        Time time = new Time();
        time.set(mStartMillis);
        mStartDay = Utils.getJulianDayInGeneral(time, true);

        setAuroraContentView(R.layout.aurora_birthday_info_layout, AuroraActionBar.Type.Normal);      
        initAuroraActionBar();
        initResources();
        initViews();
        updateViews();

        mQueryHandler = new QueryHandler(this);
        // mAdapter = new AuroraBirthdayInfoAdapter(this, com.aurora.R.layout.aurora_slid_listview, mStartMillis);
        mAdapter = new AuroraBirthdayInfoAdapter(this, R.layout.aurora_birthday_info_item, mStartMillis);

        mListView = (AuroraListView) findViewById(R.id.birthday_list);
        mListView.setAdapter(mAdapter);
        // mListView.auroraSetNeedSlideDelete(true);
        // mListView.auroraSetAuroraBackOnClickListener(mDeleteEventListener);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // From the Android Dev Guide: "It's important to note that when
        // onNewIntent(Intent) is called, the AuroraActivity has not been restarted,
        // so the getIntent() method will still return the Intent that was first
        // received with onCreate(). This is why setIntent(Intent) is called
        // inside onNewIntent(Intent) (just in case you call getIntent() at a
        // later time)."
        setIntent(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mResolver.registerContentObserver(CalendarContract.Events.CONTENT_URI, true, mObserver);

        // If the cursor is null, start the async handler. If it is not null just requery.
        if (mCursor == null) {
            Uri.Builder builder = Instances.CONTENT_BY_DAY_URI.buildUpon();
            ContentUris.appendId(builder, mStartDay);
            ContentUris.appendId(builder, mStartDay);
            mQueryHandler.startQuery(0, null, builder.build(), Event.EVENT_PROJECTION, WHERE, WHERE_ARGS, SORT_EVENTS_BY);
        } else {
            if (!mCursor.requery()) {
                mCursor.close();
                mCursor = null;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mResolver.unregisterContentObserver(mObserver);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mCursor != null) {
            mCursor.deactivate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void initAuroraActionBar() {
        AuroraActionBar actionBar = getAuroraActionBar();
        actionBar.setTitle(R.string.aurora_birthday_info);

        /*addAuroraActionBarItem(AuroraActionBarItem.Type.More, BIRTHDAY_INFO_MENU_MORE);
        actionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
        setAuroraMenuCallBack(auroraMenuCallBack);
        setAuroraMenuItems(R.menu.aurora_event_detail);*/
    }

    private void initResources() {
        mConstellationArray = getResources().getStringArray(R.array.constellation_labels);
    }

    private void initViews() {
        mBirthdayBannerView = (ImageView) findViewById(R.id.birthday_banner);
        mBirthdayNoticeView = (TextView) findViewById(R.id.birthday_notice);
        mBirthdayTodayView = (TextView) findViewById(R.id.birthday_today);
        mBirthdayYearView = (TextView) findViewById(R.id.birthday_year);
        mBirthdayMonthDayView = (TextView) findViewById(R.id.birthday_month_day);
        mBirthdayConstellationView = (TextView) findViewById(R.id.birthday_constellation);

        // mBirthdayNoticeView.setTypeface(typeface1);
        // mBirthdayTodayView.setTypeface(typeface1);
        mBirthdayYearView.setTypeface(typeface2);
        mBirthdayMonthDayView.setTypeface(typeface2);
        // mBirthdayConstellationView.setTypeface(typeface1);
    }

    private void updateViews() {
        Time time = new Time();
        time.setJulianDay(mStartDay);
        time.normalize(true);

        mBirthdayYearView.setText("" + time.year);
        mBirthdayMonthDayView.setText("" + (time.month + 1) + "." + time.monthDay);
        mBirthdayConstellationView.setText(caculateConstellation(time.month + 1, time.monthDay));

        float scale = getResources().getDisplayMetrics().density;
        float halfInvisibleHeight = 139 * scale;

        translate = new TranslateAnimation(0, 0, 0, -halfInvisibleHeight);
        translate.setDuration(10000);

        translate2 = new TranslateAnimation(0, 0, -halfInvisibleHeight, halfInvisibleHeight);
        translate2.setDuration(20000);
        translate2.setRepeatCount(-1);
        translate2.setRepeatMode(Animation.REVERSE);
        translate2.setFillAfter(true);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(translate);
        animationSet.setFillAfter(true);
        animationSet.setAnimationListener(mAnimationListener);

        mBirthdayBannerView.startAnimation(animationSet);
    }

    private String caculateConstellation(int month, int monthDay) {
        if (month == 1) {
            if (monthDay <= 20) {
                return mConstellationArray[0];
            } else {
                return mConstellationArray[1];
            }
        }

        if (month == 2) {
            if (monthDay <= 19) {
                return mConstellationArray[1];
            } else {
                return mConstellationArray[2];
            }
        }

        if (month == 3) {
            if (monthDay <= 20) {
                return mConstellationArray[2];
            } else {
                return mConstellationArray[3];
            }
        }

        if (month == 4) {
            if (monthDay <= 20) {
                return mConstellationArray[3];
            } else {
                return mConstellationArray[4];
            }
        }

        if (month == 5) {
            if (monthDay <= 21) {
                return mConstellationArray[4];
            } else {
                return mConstellationArray[5];
            }
        }

        if (month == 6) {
            if (monthDay <= 21) {
                return mConstellationArray[5];
            } else {
                return mConstellationArray[6];
            }
        }

        if (month == 7) {
            if (monthDay <= 22) {
                return mConstellationArray[6];
            } else {
                return mConstellationArray[7];
            }
        }

        if (month == 8) {
            if (monthDay <= 23) {
                return mConstellationArray[7];
            } else {
                return mConstellationArray[8];
            }
        }

        if (month == 9) {
            if (monthDay <= 23) {
                return mConstellationArray[8];
            } else {
                return mConstellationArray[9];
            }
        }

        if (month == 10) {
            if (monthDay <= 23) {
                return mConstellationArray[9];
            } else {
                return mConstellationArray[10];
            }
        }

        if (month == 11) {
            if (monthDay <= 22) {
                return mConstellationArray[10];
            } else {
                return mConstellationArray[11];
            }
        }

        if (month == 12) {
            if (monthDay <= 21) {
                return mConstellationArray[11];
            } else {
                return mConstellationArray[0];
            }
        }

        return mConstellationArray[9];
    }

    private void sendEvent(long eventType, Event event) {
        CalendarController controller = CalendarController.getInstance(mContext);
        Time now = new Time();
        now.set(controller.getTime());
        now.normalize(true);

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
    }
}