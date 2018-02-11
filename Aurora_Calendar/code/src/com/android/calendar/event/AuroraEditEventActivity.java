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

package com.android.calendar.event;

import static android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME;
import static android.provider.CalendarContract.EXTRA_EVENT_END_TIME;
import static android.provider.CalendarContract.EXTRA_EVENT_ALL_DAY;
import aurora.widget.AuroraActionBar;
import aurora.app.AuroraActivity;
import android.app.FragmentTransaction;
import aurora.app.AuroraDatePickerDialog.OnDateSetListener;
import android.app.Instrumentation;
import aurora.app.AuroraTimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.format.Time;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.android.calendar.AbstractCalendarActivity;
import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.aurora.calendar.event.AuroraEditEventView;
import com.gionee.calendar.statistics.Statistics;
import com.mediatek.calendar.LogUtil;

public class AuroraEditEventActivity extends AbstractCalendarActivity {
	private static final String TAG = "EditEventActivity";

	private static final boolean DEBUG = false;

	private static final String BUNDLE_KEY_EVENT_ID = "key_event_id";

	private static boolean mIsMultipane;

	private AuroraEditEventFragment mEditFragment;
	private AuroraActionBar mActionBar = null;

	private EventInfo mEventInfo;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setAuroraContentView(R.layout.simple_frame_layout,
				AuroraActionBar.Type.Dashboard);

		mEventInfo = getEventInfoFromIntent(icicle);

		mEditFragment = (AuroraEditEventFragment) getFragmentManager()
				.findFragmentById(R.id.main_frame);
		
		if (mEditFragment == null) {
            Intent intent = null;
            if (mEventInfo.id == -1) {
                intent = getIntent();
            }

            mEditFragment = new AuroraEditEventFragment(mEventInfo, false, intent);

            mEditFragment.mShowModifyDialogOnLaunch = getIntent().getBooleanExtra(
                    CalendarController.EVENT_EDIT_ON_LAUNCH, false);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.main_frame, mEditFragment);
            ft.show(mEditFragment);
            ft.commit();
        }
		
		///M: keep AuroraDatePickerDialog and AuroraTimePickerDialog when rotate device @{
        if (icicle != null) {
            mDateTimeIdentifier = icicle.getInt(DATE_TIME_IDENTIFIER);
            LogUtil.d(DATE_TIME_TAG, "onCreate(), mDateTimeIdentifier: " + mDateTimeIdentifier);
        }
        ///@}
        
		initActionBar();

	}
	
	public void initActionBar() {
		mActionBar = getAuroraActionBar();
		getAuroraActionBar().setTitle(
				mEventInfo.id == -1 ? R.string.event_create
						: R.string.event_edit);
		mActionBar.getCancelButton().setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						 mEditFragment.doCancelAction();
					}
				});

		mActionBar.getOkButton().setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				 mEditFragment.checkForSave();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		mEditFragment.doCancelAction();
	}

	private EventInfo getEventInfoFromIntent(Bundle icicle) {
        EventInfo info = new EventInfo();
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

        boolean allDay = intent.getBooleanExtra(EXTRA_EVENT_ALL_DAY, false);

        long begin = intent.getLongExtra(EXTRA_EVENT_BEGIN_TIME, -1);
        long end = intent.getLongExtra(EXTRA_EVENT_END_TIME, -1);
        if (end != -1) {
            info.endTime = new Time();
            if (allDay) {
                info.endTime.timezone = Time.TIMEZONE_UTC;
            }
            info.endTime.set(end);
        }
        if (begin != -1) {
            info.startTime = new Time();
            if (allDay) {
                info.startTime.timezone = Time.TIMEZONE_UTC;
            }
            info.startTime.set(begin);
        }
        info.id = eventId;

        if (allDay) {
            info.extraLong = CalendarController.EXTRA_CREATE_ALL_DAY;
        } else {
            info.extraLong = 0;
        }
        return info;
    }
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        ///M: To remove its CalendarController instance if exists @{
        CalendarController.removeInstance(this);
        ///@}
    }
	
	///M: keep AuroraDatePickerDialog and AuroraTimePickerDialog when rotate device @{
    private static final String DATE_TIME_TAG = TAG + "::date_time_debug_tag";

    // id indicate which dialog should be shown among notations
    private int mDateTimeIdentifier = AuroraEditEventView.ID_INVALID;
    static final String DATE_TIME_IDENTIFIER = "date_time_identifier";

    public void setDateTimeViewId(int id) {
        LogUtil.d(DATE_TIME_TAG, "setDateTimeViewId(), id: " + id);

        mDateTimeIdentifier = id;
    }
    
    public OnDateSetListener getDateTimeOnDateSetListener() {
        LogUtil.d(DATE_TIME_TAG, "getDateTimeOnDateSetListener()");

        View v = mEditFragment.getEditEventView().getDateTimeView(
                mDateTimeIdentifier);
//Gionee <jiating>  <2013-04-11> modify for CR00000000 cancle MTK code about setDate begin
        if (v != null) {
//            return mEditFragment.getEditEventView().getOnDateSetListener(v);
        }
      //Gionee <jiating>  <2013-04-11> modify for CR00000000 cancle MTK code about setDate end
        return null;
    }

    public OnTimeSetListener getDateTimeOnTimeSetListener() {
        LogUtil.d(DATE_TIME_TAG, "getDateTimeOnTimeSetListener()");

        View v = mEditFragment.getEditEventView().getDateTimeView(
                mDateTimeIdentifier);

        if (v != null) {
        	//Gionee <jiating>  <2013-04-11> modify for CR00000000 cancle MTK code about setDate begin    	
//            return mEditFragment.getEditEventView()
//                    .getDateTimeOnTimeSetListener(v);
        	//Gionee <jiating>  <2013-04-11> modify for CR00000000 cancle MTK code about setDate end
        }

        return null;
    }

    public boolean isAnyDialogShown() {
        LogUtil.d(DATE_TIME_TAG, "isAnyDialogShown()");

        return mEditFragment.getEditEventView().isAnyDialogShown();
    }

    public void setDialogShown() {
        LogUtil.d(DATE_TIME_TAG, "setDialogShown()");

        mEditFragment.getEditEventView().setDialogShown();
    }

    public OnDismissListener getDateTimeOnDismissListener() {
        LogUtil.d(DATE_TIME_TAG, "getDateTimeOnDismissListener()");

        return mEditFragment.getEditEventView().getDateTimeOnDismissListener();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(DATE_TIME_IDENTIFIER, mDateTimeIdentifier);
        LogUtil.d(DATE_TIME_TAG, "onSaveInstanceState(), mDateTimeIdentifier: "
                + mDateTimeIdentifier);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	if(resultCode ==AuroraActivity.RESULT_OK){
//    		mEditFragment.setClickVoiceButton(false);
			switch (requestCode) {
			case AuroraEditEventFragment.REPEAT_CHOOSE_ACTION_CODE:
				Log.i("liumxxx","AuroraEditEventFragment.REPEAT_CHOOSE_ACTION_CODE--------------");
				int position = data.getExtras().getInt("position");
				if(mEditFragment!=null){
					mEditFragment.getAuroraEditEventView().setRepeat(position);
				}
				Log.e("liumxxx","AuroraEditEventFragment.REPEAT_CHOOSE_ACTION_CODE----------------------------"+position);
			default:
				Log.i("jiating","voice..."+"recognize no result!");
				break;
			}
		}else{
//			mEditFragment.setClickVoiceButton(false);
			Log.i("jiating","voice..."+"recognize no result!");
		}
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
//    	mEditFragment.setClickVoiceButton(false);
    	 InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//         imm.toggleSoftInput(showFlags, hideFlags)
    	 getWindow().setSoftInputMode(  
                 WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    	 Statistics.onResume(AuroraEditEventActivity.this);
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
   	 	Statistics.onPause(AuroraEditEventActivity.this);
    }
}