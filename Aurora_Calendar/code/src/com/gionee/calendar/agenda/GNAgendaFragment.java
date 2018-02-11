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
//Gionee <jiating> <2013-04-24> modify for CR00000000  begin 
package com.gionee.calendar.agenda;

import java.util.ArrayList;

import aurora.app.AuroraActivity;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Attendees;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.HeaderViewListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.android.calendar.AsyncQueryService;
import com.android.calendar.Event;
import com.android.calendar.EventInfoFragment;
import com.android.calendar.EventLoader;
import com.android.calendar.GeneralPreferences;
import com.android.calendar.R;
import com.android.calendar.StickyHeaderListView;
import com.android.calendar.Utils;
import com.gionee.calendar.GNDateTextUtils;
//import com.aurora.calendar.event.AuroraEventInfoFragment;
import com.gionee.calendar.statistics.Statistics;
import com.mediatek.calendar.LogUtil;

//Gionee <jiating> <2013-04-24> modify for CR00000000  begin 
//Gionee <pengwei><20130807> modify for CR00850530 begin
public class GNAgendaFragment extends Fragment implements
		CalendarController.EventHandler, OnScrollListener {

	private static final String TAG = GNAgendaFragment.class.getSimpleName();
	private static boolean DEBUG = false;

	protected static final String BUNDLE_KEY_RESTORE_TIME = "key_restore_time";
	protected static final String BUNDLE_KEY_RESTORE_INSTANCE_ID = "key_restore_instance_id";
	protected static final String BUNDLE_KEY_RESTORE_TOP_DEVIATION = "key_restore_top_deviation";

	private GNAgendaListView mAgendaListView;
	private Activity mActivity;
	private final Time mTime;
	private String mTimeZone;
	private final long mInitialTimeMillis;
	private boolean mShowEventDetailsWithAgenda;
	private CalendarController mController;
//	private AuroraEventInfoFragment mEventFragment;
	private String mQuery;
	private boolean mUsedForSearch = false;
	private boolean mIsTabletConfig;
	private EventInfo mOnAttachedInfo = null;
	private boolean mOnAttachAllDay = false;
	private boolean mForceReplace = true;
	private long mLastShownEventId = -1;
	private ArrayList<Event> mEvents;
	private GNAgendaByDayAdapter adapter;
	private EventLoader eventLoader;
	private RelativeLayout eventInfoLoding;
	private TextView  agendaInfo;
	 private AsyncQueryService mService;
	 private Toast mToast;
	 public boolean isSearching=false;
	 //Gionee <jiating><2013-06-26> modify for CR00829885 begin
	 private boolean isFirst=true;
	//Gionee <jiating><2013-06-26> modify for CR00829885 end
	// Tracks the time of the top visible view in order to send UPDATE_TITLE
	// messages to the action
	// bar.
	int mJulianDayOnTop = -1;

	private final Runnable mTZUpdater = new Runnable() {
		@Override
		public void run() {
			mTimeZone = Utils.getTimeZone(getActivity(), this);
			mTime.switchTimezone(mTimeZone);
		}
	};

	public GNAgendaFragment() {
		this(0, false);
	}

	// timeMillis - time of first event to show
	// usedForSearch - indicates if this fragment is used in the search fragment
	public GNAgendaFragment(long timeMillis, boolean usedForSearch) {

		Log.i("jiating", "GNAgendaFragment.....GNAgendaFragment()..timeMillis"
				+ GNDateTextUtils.buildMonthYearDayUseFormat(timeMillis));
		mInitialTimeMillis = timeMillis;
		// /M:keep the time that enter the AgendaFragment{
		mOriginalTime.set(timeMillis);
		mOriginalTime.normalize(false);
		// /@}
		mTime = new Time();
		mLastHandledEventTime = new Time();

		if (mInitialTimeMillis == 0) {
			mTime.setToNow();
		} else {
			mTime.set(mInitialTimeMillis);
		}
		mLastHandledEventTime.set(mTime);
		mUsedForSearch = usedForSearch;
		isSearching=false;
		 //Gionee <jiating><2013-06-26> modify for CR00829885 begin
		isFirst=true;
		 //Gionee <jiating><2013-06-26> modify for CR00829885 bend
		
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mTimeZone = Utils.getTimeZone(activity, mTZUpdater);
		mTime.switchTimezone(mTimeZone);
		mActivity = activity;
		 mService = new AsyncQueryService(mActivity) {
	            
	            
	            @Override
	            protected void onDeleteComplete(int token, Object cookie, int result) {
	                LogUtil.i(TAG, "Clear all events,onDeleteComplete.  result(delete number)=" + result);
	                
	                if (mToast == null) {
	                    mToast = Toast.makeText(mActivity, R.string.delete_completed, Toast.LENGTH_SHORT);
	                }
	                mToast.show();
	                
	              getAllEvent();

	                super.onDeleteComplete(token, cookie, result);
	            }
	        };

	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		mController = CalendarController.getInstance(mActivity);
		eventLoader = new EventLoader(mActivity);
		eventLoader.startBackgroundThread();
		if (icicle != null) {
			long prevTime = icicle.getLong(BUNDLE_KEY_RESTORE_TIME, -1);
			if (prevTime != -1) {
				mTime.set(prevTime);
				if (DEBUG) {
					Log.d(TAG, "Restoring time to " + mTime.toString());
				}
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		int screenWidth = mActivity.getResources().getDisplayMetrics().widthPixels;
		// /M: #multi-event share# make the sub class be able to inflate the
		// view @{
		View v = extInflateFragmentView(inflater);
		// /@}

		// /M: #multi-event share# make the sub class be able to get the list
		// view @{
		mAgendaListView = extFindListView(v);
		// /@}
		mAgendaListView.setClickable(true);
		mAgendaListView.setFragment(GNAgendaFragment.this);
		adapter = new GNAgendaByDayAdapter(mActivity);
		mAgendaListView.setAdapter(adapter);
		eventInfoLoding=(RelativeLayout)v.findViewById(R.id.event_info_loading_msg);
		agendaInfo=(TextView)v.findViewById(R.id.agenda_info);
		agendaInfo.setVisibility(View.GONE);
		eventInfoLoding.setVisibility(View.VISIBLE);
		mAgendaListView.onResume();
	
		
	
		getAllEvent();
		if (savedInstanceState != null) {
			Log.i("jiating", "savedInstanceState != null");
			long instanceId = savedInstanceState.getLong(
					BUNDLE_KEY_RESTORE_INSTANCE_ID, -1);
			// if (instanceId != -1) {
			// mAgendaListView.setSelectedInstanceId(instanceId);
			// }

			// / M: Get the data to set the deviation of the listView top, will
			// be used in
			// AgendaWindowAdapter after listView refresh finished. @{
			int topDeviation = savedInstanceState.getInt(
					BUNDLE_KEY_RESTORE_TOP_DEVIATION, 0);
			// GNAgendaWindowAdapter.setTopDeviation(topDeviation);
			// / @}
		}

		return v;
	}

	@Override
	public void onResume() {

		super.onResume();
		 //Gionee <jiating><2013-06-26> modify for CR00829885 begin
		Log.i("jiating", "GNAgendaFragment.....onResume isFirst="+isFirst);
		if(!isFirst){
			eventLoader.startBackgroundThread();
		}
		 //Gionee <jiating><2013-06-26> modify for CR00829885 end
		Statistics.onResume(mActivity);
	
	}
	
	public void getAllEvent(){
		 //Gionee <jiating><2013-06-26> modify for CR00829885 begin
		if (mEvents == null) {
			mEvents = new ArrayList<Event>();
		}else{
			mEvents.clear();
		}
		 //Gionee <jiating><2013-06-26> modify for CR00829885 end
		eventLoader.loadAllEventsInBackground(mEvents, Time.EPOCH_JULIAN_DAY,
				2465059, null, mEventsLoadingFinishedCallback,
				mEventsLoadingCanceledCallback);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

	}



	@Override
	public void onPause() {
		super.onPause();
		 //Gionee <jiating><2013-06-26> modify for CR00829885 begin
		isFirst=false;
		eventLoader.stopBackgroundThread();
		mAgendaListView.onPause();
		 //Gionee <jiating><2013-06-26> modify for CR00829885 end
		Statistics.onPause(mActivity);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}



	public  void goTo(Time time) {
		Log.i("jiating", "AgendaFragment......goTo"+"t="+time.toMillis(false));
		
		if (mAgendaListView == null) {
			// The view hasn't been set yet. Just save the time and use it
			// later.
			return;
		}
		mAgendaListView
				.goTo(time);
		mForceReplace = false;
	}
	
	public void searchEvents(String query){
		Log.i("jiating","GNAgendaFragment.....searchEvents="+query);
		 //Gionee <jiating><2013-06-26> modify for CR00829885 begin
//		agendaInfo.setVisibility(View.GONE);
		if (mEvents == null) {
			mEvents = new ArrayList<Event>();
		}else{
			mEvents.clear();
		}
		 //Gionee <jiating><2013-06-26> modify for CR00829885 end
		mUsedForSearch=true;
		eventLoader.loadAllEventsInBackground(mEvents, Time.EPOCH_JULIAN_DAY,
				2465059,query,mEventsLoadingFinishedCallback,
				mEventsLoadingCanceledCallback);
		
		

		
	}


	@Override
	public void eventsChanged() {
		Log.i("jiating", "AgendaFragment......eventsChanged");
		getAllEvent();
	}

	@Override
	public long getSupportedEventTypes() {
		Log.i("jiating", "AgendaFragment......getSupportedEventTypes");
		return EventType.GO_TO | EventType.EVENTS_CHANGED
				| ((mUsedForSearch) ? EventType.SEARCH : 0);
	}

	private long mLastHandledEventId = -1;
	private Time mLastHandledEventTime = null;

	@Override
	public void handleEvent(EventInfo event) {
		Log.i("jiating", "FNAgendaFragment....handleEvent...");
		if (event.eventType == EventType.GO_TO) {
			// TODO support a range of time
			// TODO support event_id
			// TODO figure out the animate bit
			Log.i("jiating", "FNAgendaFragment....handleEvent...goto.");
			mLastHandledEventId = event.id;
			mLastHandledEventTime = (event.selectedTime != null) ? event.selectedTime
					: event.startTime;
			goTo(mLastHandledEventTime);
		} else if (event.eventType == EventType.SEARCH) {
//			search(event.query, event.startTime);
		} else if (event.eventType == EventType.EVENTS_CHANGED) {
			Log.i("jiating", "FNAgendaFragment....handleEvent...EVENTS_CHANGED");
			eventsChanged();
		}
	}

	
	// OnScrollListener implementation to update the date on the pull-down menu
	// of the app

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}

	// Gets the time of the first visible view. If it is a new time, send a
	// message to update
	// the time on the ActionBar
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		Log.i("jiating", "AgendaFragment......onScroll");

	}

	/**
	 * M: for sub class override, to inflate the view
	 * 
	 * @param inflater
	 * @return agenda view or Events pick view
	 */
	protected View extInflateFragmentView(LayoutInflater inflater) {
		return inflater.inflate(R.layout.gn_agenda_fragment, null);
	}

	/**
	 * M: for sub class override, to get the AgendaListView
	 * 
	 * @param v
	 * @return AgendaListView or EventsListView
	 */
	protected GNAgendaListView extFindListView(View v) {
		return (GNAgendaListView) v.findViewById(R.id.agenda_events_list);
	}

	public void deleteEvent(long id){
		Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id);
		 mService.startDelete(mService.getNextToken(), null, uri, null, null, Utils.UNDO_DELAY);
	}

	// /M:The time that the event to show when enter Agenda Fragement
	private Time mOriginalTime = new Time();

	private Runnable mEventsLoadingFinishedCallback = new Runnable() {
		@Override
		public void run() {
			 //Gionee <jiating><2013-06-26> modify for CR00829885 begin
			// TODO
			// to display queried events in a list view
			eventInfoLoding.setVisibility(View.GONE);
			Log.i("jiating","mEventsLoadingFinishedCallback...mEvents="+mEvents.size());
			if(mEvents.size()<=0 ){
				agendaInfo.setVisibility(View.VISIBLE);
				mAgendaListView.setVisibility(View.GONE);
				
				if(isSearching){
					agendaInfo.setText(R.string.gn_agenda_search_nothing);
				}else{
					
					agendaInfo.setText(R.string.gn_agenda_no_event);
				}
				return;
			}else{
				agendaInfo.setVisibility(View.GONE);
				mAgendaListView.setVisibility(View.VISIBLE);
				adapter.calculateDays(mEvents);
				Log.i("jiating","mEventsLoadingFinishedCallback...mEvents="+mEvents.size());
				adapter.refresh();
				if (!mUsedForSearch) {
					goTo(mOriginalTime);
				}/* else {
					GNAgendaActivity  mAgendaActivity=(GNAgendaActivity)mActivity;
					InputMethodManager inputMethodManager = (InputMethodManager) mActivity
					.getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.showSoftInputFromInputMethod(
							mAgendaActivity.getCalendarSearchView().getWindowToken(), 0);
				}*/
			}
			 //Gionee <jiating><2013-06-26> modify for CR00829885 end
			

		}
	};

	private Runnable mEventsLoadingCanceledCallback = new Runnable() {
		@Override
		public void run() {

			// TODO
			Log.i("jiating","mEventsLoadingCanceledCallback....");
			 //Gionee <jiating><2013-06-27> modify for CR00830822 begin
//			eventInfoLoding.setVisibility(View.GONE);
//			agendaInfo.setVisibility(View.VISIBLE);
//			mAgendaListView.setVisibility(View.GONE);
//			agendaInfo.setText(R.string.gn_agenda_get_events_failed);
			 //Gionee <jiating><2013-06-27> modify for CR00830822 begin
		}
	};
}

//Gionee <jiating> <2013-04-24> modify for CR00000000  bend
//Gionee <pengwei><20130807> modify for CR00850530 end