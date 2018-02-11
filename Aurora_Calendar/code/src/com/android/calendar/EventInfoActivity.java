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

import static android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME;
import static android.provider.CalendarContract.EXTRA_EVENT_END_TIME;
import static android.provider.CalendarContract.Attendees.ATTENDEE_STATUS;

import com.android.calendar.event.AuroraEditEventActivity;
import com.aurora.calendar.event.AuroraEventInfoFragment;
import com.mediatek.calendar.features.Features;

import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
//Gionee <jiating> <2013-04-11>  modify for CR00000000 use AuroraEventInfoFragment instead of EventInfoFragment begin
public class EventInfoActivity extends AuroraActivity {
//        implements CalendarController.EventHandler, SearchView.OnQueryTextListener,
//        SearchView.OnCloseListener {

    private static final String TAG = "EventInfoActivity";
    private AuroraEventInfoFragment mInfoFragment;
    private long mStartMillis, mEndMillis;
    private long mEventId;
    private RelativeLayout  backButton;
    private View mActionBarView;
    // aurora ukiliu 2013-11-29 add for aurora ui begin
    private AuroraActionBar mActionBar = null;
    private static final int AURORA_EVENT_DETAIL_MORE = 1;
    private Context mContext;
    // aurora ukiliu 2013-11-29 add for aurora ui end

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mContext = EventInfoActivity.this;
        // Get the info needed for the fragment
        Intent intent = getIntent();
        int attendeeResponse = 0;
        mEventId = -1;
        boolean isDialog = false;

        if (icicle != null) {
            mEventId = icicle.getLong(AuroraEventInfoFragment.BUNDLE_KEY_EVENT_ID);
            mStartMillis = icicle.getLong(AuroraEventInfoFragment.BUNDLE_KEY_START_MILLIS);
            mEndMillis = icicle.getLong(AuroraEventInfoFragment.BUNDLE_KEY_END_MILLIS);
            attendeeResponse = icicle.getInt(AuroraEventInfoFragment.BUNDLE_KEY_ATTENDEE_RESPONSE);
            isDialog = icicle.getBoolean(AuroraEventInfoFragment.BUNDLE_KEY_IS_DIALOG);
        } else if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            mStartMillis = intent.getLongExtra(EXTRA_EVENT_BEGIN_TIME, 0);
            mEndMillis = intent.getLongExtra(EXTRA_EVENT_END_TIME, 0);
            attendeeResponse = intent.getIntExtra(ATTENDEE_STATUS,
                    Attendees.ATTENDEE_STATUS_NONE);
            Uri data = intent.getData();
            if (data != null) {
                try {
                    List<String> pathSegments = data.getPathSegments();
                    int size = pathSegments.size();
                    if (size > 2 && "EventTime".equals(pathSegments.get(2))) {
                        // Support non-standard VIEW intent format:
                        //dat = content://com.android.calendar/events/[id]/EventTime/[start]/[end]
                        mEventId = Long.parseLong(pathSegments.get(1));
                        if (size > 4) {
                            mStartMillis = Long.parseLong(pathSegments.get(3));
                            mEndMillis = Long.parseLong(pathSegments.get(4));
                        }
                    } else {
                        mEventId = Long.parseLong(data.getLastPathSegment());
                    }
                } catch (NumberFormatException e) {
                    if (mEventId == -1) {
                        // do nothing here , deal with it later
                    } else if (mStartMillis == 0 || mEndMillis ==0) {
                        // Parsing failed on the start or end time , make sure the times were not
                        // pulled from the intent's extras and reset them.
                        mStartMillis = 0;
                        mEndMillis = 0;
                    }
                }
            }
        }

        if (mEventId == -1) {
            Log.w(TAG, "No event id");
            Toast.makeText(this, R.string.event_not_found, Toast.LENGTH_SHORT).show();
            finish();
        }

        // If we do not support showing full screen event info in this configuration,
        // close the activity and show the event in AllInOne.
        Resources res = getResources();
        if (!res.getBoolean(R.bool.agenda_show_event_info_full_screen)
                && !res.getBoolean(R.bool.show_event_info_full_screen)) {
            CalendarController.getInstance(this)
                    .launchViewEvent(mEventId, mStartMillis, mEndMillis, attendeeResponse);
            finish();
            return;
        }

//        setContentView(R.layout.simple_frame_layout);
        setAuroraContentView(R.layout.simple_frame_layout,
                AuroraActionBar.Type.Normal);      
        initAuroraActionBar();

        // Get the fragment if exists
        mInfoFragment = (AuroraEventInfoFragment)
                getFragmentManager().findFragmentById(R.id.main_frame);

        //Gionee jiating 2013-04-09 modify for actionBar begin 
//        AuroraActionBar bar = getAuroraActionBar();
//        if (bar != null) {
        	
//        	bar.setIcon(R.drawable.gn_event_actionbar_icon);
//            bar.setDisplayHomeAsUpEnabled(false);
//            bar.setDisplayShowTitleEnabled(false);
//            bar.setDisplayShowHomeEnabled(false);
//            bar.setDisplayShowCustomEnabled(true);
//     		 mActionBarView = LayoutInflater.from(bar.getThemedContext())
//     				.inflate(R.layout.gn_calendar_eventinfo_action_bar_custom, null);
//     		 bar.setCustomView(mActionBarView, new AuroraActionBar.LayoutParams(
//     				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
//     		bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.gn_all_in_one_title_bg));
//     		backButton=(RelativeLayout)mActionBarView.findViewById(R.id.gn_event_info_actionbar_back);
//     		backButton.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					finish();
//				}
//			});
     		
             
//        }
        //Gionee jiating 2013-04-09 modify for actionBar end
      //Gionee <jiating><2013-07-03> modify for CR00831105 begin  
        // Remove the application title
//        AuroraActionBar bar = getAuroraActionBar();
//        if (bar != null) {
//        	 bar.setIcon(R.drawable.gn_event_actionbar_icon);
//             bar.setDisplayHomeAsUpEnabled(false);
//             bar.setDisplayShowTitleEnabled(false);
//             bar.setDisplayShowHomeEnabled(false);
//             bar.setDisplayShowCustomEnabled(true);
//     		 mActionBarView = LayoutInflater.from(bar.getThemedContext())
//     				.inflate(R.layout.gn_calendar_eventinfo_action_bar_custom, null);
//     		 bar.setCustomView(mActionBarView, new AuroraActionBar.LayoutParams(
//     				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
//     		bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.gn_all_in_one_title_bg));
//     		backButton=(RelativeLayout)mActionBarView.findViewById(R.id.gn_event_info_actionbar_back);
//     		backButton.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					EventInfoActivity.this.finish();
//				}
//			});
//        }
      //Gionee <jiating><2013-07-03> modify for CR00831105 end
        // Create a new fragment if none exists
        if (mInfoFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            mInfoFragment = new AuroraEventInfoFragment(this, mEventId, mStartMillis, mEndMillis,
                    attendeeResponse, isDialog, isDialog ?
                    		AuroraEventInfoFragment.DIALOG_WINDOW_STYLE :
                            	AuroraEventInfoFragment.FULL_WINDOW_STYLE);
            ft.replace(R.id.main_frame, mInfoFragment, AuroraEventInfoFragment.FRAGMENT_TAG);
            ft.commit();
        }
        
        ///M:NFC.
        /*if (Features.isBeamPlusEnabled()) {
            com.mediatek.calendar.nfc.NfcHandler.register(this, mInfoFragment);
        }*/
    }
    
	// aurora ukiliu 2013-11-29 add for aurora ui begin
	private void initAuroraActionBar() {
		mActionBar = getAuroraActionBar();
		mActionBar.setTitle(R.string.aurora_event_detail_title);

		addAuroraActionBarItem(AuroraActionBarItem.Type.More,
				AURORA_EVENT_DETAIL_MORE);
		mActionBar
				.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
		setAuroraMenuCallBack(auroraMenuCallBack);
		setAuroraMenuItems(R.menu.aurora_event_detail);

	}

	private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case AURORA_EVENT_DETAIL_MORE:
				showAuroraMenu();
				break;
			default:
				break;
			}
		}
	};

	private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int arg0) {
			switch (arg0) {
			case R.id.event_edit: {
				if (mEventId != -1) {
					Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI,
							mEventId);
					Intent intent = new Intent(Intent.ACTION_EDIT, uri);
					intent.putExtra(EXTRA_EVENT_BEGIN_TIME, mStartMillis);
					intent.putExtra(EXTRA_EVENT_END_TIME, mEndMillis);
					intent.setClass(mContext, AuroraEditEventActivity.class);
					intent.putExtra(CalendarController.EVENT_EDIT_ON_LAUNCH,
							true);
					startActivity(intent);
					finish();
				}
				break;
			}
			case R.id.event_delete: {
				if (mEventId != -1) {
					mInfoFragment.deleteEvent();
				}
				break;
			}
			default:
				break;
			}

		}

	};
	// aurora ukiliu 2013-11-29 add for aurora ui end
	
	// aurora ukiliu 2013-11-29 delete for aurora ui begin
//Gionee <jiating> <2013-04-11>  modify for CR00000000 use AuroraEventInfoFragment instead of EventInfoFragment end
//@Override
//public boolean onCreateOptionsMenu(Menu menu) {
//	// TODO Auto-generated method stub
//	Log.i("jiating","aurora ...EventActivity...onCreateOptionsMenu");
//	return super.onCreateOptionsMenu(menu);
//}
//@Override
//	public boolean onOptionsItemSelected(MenuItem menuItem) {
//		// TODO Auto-generated method stub
//	Log.i("jiating","aurora ...EventInfoActivity...onOptionsItemSelected");
//	 
//    if (mInfoFragment != null) {
//    	mInfoFragment.onOptionsItemSelected(menuItem);
//    }
//		return super.onOptionsItemSelected(menuItem);
//	}
//@Override
//public boolean onPrepareOptionsMenu(Menu menu) {
//	// TODO Auto-generated method stub
//	Log.i("jiating","aurora ...EventInfoActivity...onPrepareOptionsMenu");
////	  if (mInfoFragment != null) {
////		  mInfoFragment.onPrepareOptionsMenu(menu);
////      }
//	return super.onPrepareOptionsMenu(menu);
//}
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        // Handles option menu selections:
//        // Home button - close event info activity and start the main calendar one
//        // Edit button - start the event edit activity and close the info activity
//        // Delete button - start a delete query that calls a runnable that close the info activity
//
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                Intent launchIntent = new Intent();
//                launchIntent.setAction(Intent.ACTION_VIEW);
//                launchIntent.setData(Uri.parse(CalendarContract.CONTENT_URI + "/time"));
//                launchIntent.setFlags(
//                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(launchIntent);
//                finish();
//                return true;
//            case R.id.info_action_edit:
//                Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, mEventId);
//                Intent intent = new Intent(Intent.ACTION_EDIT, uri);
//                intent.putExtra(EXTRA_EVENT_BEGIN_TIME, mStartMillis);
//                intent.putExtra(EXTRA_EVENT_END_TIME, mEndMillis);
//                intent.setClass(this, EditEventActivity.class);
//                intent.putExtra(EVENT_EDIT_ON_LAUNCH, true);
//                startActivity(intent);
//                finish ();
//                break;
//            case R.id.info_action_delete:
//                DeleteEventHelper deleteHelper = new DeleteEventHelper(
//                        this, this, true /* exitWhenDone */);
//                deleteHelper.delete(mStartMillis, mEndMillis, mEventId, -1, onDeleteRunnable);
//                break;
//            default:
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    // runs at the end of a delete action and closes the activity
//    private Runnable onDeleteRunnable = new Runnable() {
//        @Override
//        public void run() {
//            finish ();
//        }
//    };
	// aurora ukiliu 2013-11-29 delete for aurora ui end

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
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ///M: To remove its CalendarController instance if exists @{
        CalendarController.removeInstance(this);
        ///@}
    }
    
 // Gionee <jiating><2013-06-25> modify for CR00829225 begin 		
	 @Override
	    public void onConfigurationChanged(Configuration newConfig) {
	    	// TODO Auto-generated method stub
	    	super.onConfigurationChanged(newConfig);
	    	Log.i("jiating","EditEventActivity....onConfigurationChanged");
	    }
//Gionee <jiating><2013-06-25> modify for CR00829225 end 
}
