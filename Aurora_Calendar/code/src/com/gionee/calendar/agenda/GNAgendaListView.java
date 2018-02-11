/*
 * Copyright (C) 2009 The Android Open Source Project
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
//Gionee <jiating> <2013-04-24> modify for CR00000000  begin 
import static android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME;
import static android.provider.CalendarContract.EXTRA_EVENT_END_TIME;
import static android.provider.CalendarContract.Attendees.ATTENDEE_STATUS;


import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.android.calendar.alerts.AlertUtils;

import com.android.calendar.AllInOneActivity;
import com.android.calendar.AsyncQueryService;
import com.android.calendar.DeleteEventHelper;
import com.android.calendar.Event;
import com.android.calendar.EventInfoActivity;
import com.android.calendar.EventLoader;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.gionee.calendar.agenda.GNAgendaByDayAdapter.ViewHolder;
import com.gionee.calendar.day.DayUtils;
import com.gionee.calendar.statistics.StatisticalName;
import com.gionee.calendar.statistics.Statistics;
import com.mediatek.calendar.LogUtil;


import aurora.app.AuroraAlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Events;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import aurora.widget.AuroraListView;
import android.widget.TextView;
import android.widget.Toast;

public class GNAgendaListView extends AuroraListView implements OnItemClickListener,OnItemLongClickListener {

	private static final String TAG = "AgendaListView";
	private static final boolean DEBUG = false;
	private static final int EVENT_UPDATE_TIME = 300000; // 5 minutes

	private GNAgendaByDayAdapter mGNAgendaByDayAdapter;
	private DeleteEventHelper mDeleteEventHelper;
	private Context mContext;
	private String mTimeZone;
	private Time mTime;
	private boolean mShowEventDetailsWithAgenda;
	private Handler mHandler = null;
	private GNAgendaFragment  agendaFragment;
	

	private final Runnable mTZUpdater = new Runnable() {
		@Override
		public void run() {
			Log.i("jiating", "AgendaListView......mTZUpdater");
			mTimeZone = Utils.getTimeZone(mContext, this);
			mTime.switchTimezone(mTimeZone);
		}
	};

	// runs every midnight and refreshes the view in order to update the
	// past/present
	// separator
	private final Runnable mMidnightUpdater = new Runnable() {
		@Override
		public void run() {
			Log.i("jiating", "AgendaListView......mMidnightUpdater");
			refresh(true);
			Utils.setMidnightUpdater(mHandler, mMidnightUpdater, mTimeZone);
		}
	};

	// Runs every EVENT_UPDATE_TIME to gray out past events
	private final Runnable mPastEventUpdater = new Runnable() {
		@Override
		public void run() {
			Log.i("jiating", "AgendaListView......mPastEventUpdater");


		}
	};

	public GNAgendaListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.i("jiating", "AgendaListView......AgendaListView");
		initView(context);
	}

	private void initView(Context context) {
		Log.i("jiating", "AgendaListView......initView");
		mContext = context;
		mTimeZone = Utils.getTimeZone(context, mTZUpdater);
		mTime = new Time(mTimeZone);
		setOnItemClickListener(this);
		setOnItemLongClickListener(this);
		setVerticalScrollBarEnabled(true);
		mGNAgendaByDayAdapter=(GNAgendaByDayAdapter) getAdapter();
		// /M: Don't show details of an event for AgendaChoiceActivity.@{

		mDeleteEventHelper = new DeleteEventHelper(context, null, false /*
																		 * don't
																		 * exit
																		 * when
																		 * done
																		 */);

		// /M: Don't show details of an event for AgendaChoiceActivity.@{
		if (context instanceof com.mediatek.calendar.selectevent.AgendaChoiceActivity) {
			mShowEventDetailsWithAgenda = false;
		} else {
			mShowEventDetailsWithAgenda = Utils.getConfigBool(mContext,
					R.bool.show_event_details_with_agenda);
		}
		// /@}

		// Hide ListView dividers, they are done in the item views themselves
		setDivider(null);
		setDividerHeight(0);

		mHandler = new Handler();
		
		 
	}

	


	// Implementation of the interface OnItemClickListener
	@Override
    public void onItemClick(AdapterView<?> a, View v, int position, long id) {
		try {
		Log.i("jiating","onItemClick");
			if (id != -1) {
				// Switch to the EventInfo view
				Event event = mGNAgendaByDayAdapter
						.getEventByPosition(position);

				// If events are shown to the side of the agenda list , do
				// nothing
				// when the same event is selected , otherwise show the selected
				// event.

				if (event != null || !mShowEventDetailsWithAgenda) {
					// Gionee <jiating><2013-07-04> modify for CR00833259 begin
					long startTime = event.startMillis;
					long endTime = event.endMillis;
					// Holder in view holds the start of the specific part of a
					// multi-day event ,
					// use it for the goto
					long holderStartTime;
					Object holder = v.getTag();
					if (holder instanceof GNAgendaByDayAdapter.ViewHolder) {
						holderStartTime = ((GNAgendaByDayAdapter.ViewHolder) holder).startTimeMilli;
					} else {
						holderStartTime = startTime;
					}
					// if (event.allDay) {
					// startTime = Utils.convertAlldayLocalToUTC(mTime,
					// startTime, mTimeZone);
					// endTime = Utils.convertAlldayLocalToUTC(mTime, endTime,
					// mTimeZone);
					// }
					// //Gionee <jiating><2013-06-27> modify for CR00830822
					// begin
					// mTime.set(startTime);
					// EventInfo info = new EventInfo();
					// info.eventType = EventType.VIEW_EVENT;
					//
					// info.id = event.id;
					// info.startTime = new Time(mTimeZone);
					// info.startTime.set(startTime);
					//
					// info.endTime = new Time(mTimeZone);
					// info.endTime.set(endTime);
					// info.x = 0;
					// info.y = 0;
					// info.extraLong =
					// CalendarController.EventInfo.buildViewExtraLong(
					// Attendees.ATTENDEE_STATUS_NONE, event.allDay);
					// Intent intent = new Intent(Intent.ACTION_VIEW);
					// Uri eventUri =
					// ContentUris.withAppendedId(Events.CONTENT_URI, event.id);
					// intent.setData(eventUri);
					// intent.setClass(mContext, EventInfoActivity.class);
					// intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
					// Intent.FLAG_ACTIVITY_SINGLE_TOP);
					// intent.putExtra(EXTRA_EVENT_BEGIN_TIME, startTime);
					// intent.putExtra(EXTRA_EVENT_END_TIME, endTime);
					// intent.putExtra(ATTENDEE_STATUS, info.getResponse());
					// AlertUtils.removeEventNotification(mContext, event.id,
					// info.startTime != null ? info.startTime.toMillis(false) :
					// -1,
					// info.endTime != null ? info.endTime.toMillis(false) :
					// -1);
					// mContext. startActivity(intent);
					// Gionee <jiating><2013-06-27> modify for CR00830822 end

					viewEvent(event);
					// Gionee <jiating><2013-07-04> modify for CR00833259 end
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.i("Calendar", "GNAgendaListView---onItemClick---e == " + e);
		}
    }
	
	//Gionee <jiating><2013-07-04> modify for CR00833259 begin
	private void viewEvent(Event event){
		long startTime = event.startMillis;
        long endTime = event.endMillis;
        // Holder in view holds the start of the specific part of a multi-day event ,
        // use it for the goto
    
        if (event.allDay) {
            startTime = Utils.convertAlldayLocalToUTC(mTime, startTime, mTimeZone);
            endTime = Utils.convertAlldayLocalToUTC(mTime, endTime, mTimeZone);
        }
		 mTime.set(startTime);
         EventInfo info = new EventInfo();
         info.eventType = EventType.VIEW_EVENT;
        
         info.id = event.id;
         info.startTime = new Time(mTimeZone);
         info.startTime.set(startTime);

         info.endTime = new Time(mTimeZone);
         info.endTime.set(endTime);
         info.x = 0;
         info.y = 0;
         info.extraLong = CalendarController.EventInfo.buildViewExtraLong(
                 Attendees.ATTENDEE_STATUS_NONE, event.allDay);
         Intent intent = new Intent(Intent.ACTION_VIEW);
         Uri eventUri = ContentUris.withAppendedId(Events.CONTENT_URI, event.id);
         intent.setData(eventUri);
         intent.setClass(mContext, EventInfoActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                 Intent.FLAG_ACTIVITY_SINGLE_TOP);
         intent.putExtra(EXTRA_EVENT_BEGIN_TIME, startTime);
         intent.putExtra(EXTRA_EVENT_END_TIME, endTime);
         intent.putExtra(ATTENDEE_STATUS, info.getResponse());
         AlertUtils.removeEventNotification(mContext, event.id,
         		info.startTime != null ? info.startTime.toMillis(false) : -1,
         				info.endTime != null ? info.endTime.toMillis(false) : -1);
         mContext. startActivity(intent);
	}
	
	//Gionee <jiating><2013-07-04> modify for CR00833259 end

	public void goTo(Time time, long id, String searchQuery, boolean forced,
			boolean refreshEventInfo) {
		Log.i("jiating", "AgendaListView......goTo");
		if (time == null) {
			time = mTime;
			long goToTime = getFirstVisibleTime(null);
			if (goToTime <= 0) {
				goToTime = System.currentTimeMillis();
			}
			time.set(goToTime);
		}
		mTime.set(time);
		mTime.switchTimezone(mTimeZone);
		mTime.normalize(true);
		if (DEBUG) {
			Log.d(TAG, "Goto with time " + mTime.toString());
		}
		if(mGNAgendaByDayAdapter!=null){
			mGNAgendaByDayAdapter=(GNAgendaByDayAdapter) getAdapter();
		}
		mGNAgendaByDayAdapter.refresh(mTime, id, searchQuery, forced,
				refreshEventInfo);
	}
	
	
	
	public void goTo(Time time) {
		Log.i("jiating", "AgendaListView......goTo"+"time="+time.toMillis(false));
		if (time == null) {
			time = mTime;
			long goToTime = getFirstVisibleTime(null);
			if (goToTime <= 0) {
				goToTime = System.currentTimeMillis();
			}
			time.set(goToTime);
		}
//		if (!isEventVisible(goToTime)) {
            int gotoPosition =mGNAgendaByDayAdapter.findEventPositionNearestTime(time);
            if (gotoPosition >= 0) {
              setSelectionFromTop(gotoPosition , -1);
           
            
            }

//            Time actualTime = new Time(mTimeZone);
//            actualTime.set(goToTime);
//            CalendarController.getInstance(mContext).sendEvent(this, EventType.UPDATE_TITLE,
//                    actualTime, actualTime, -1, ViewType.CURRENT);
//        }
	}
	
	


	public void refresh(boolean forced) {
		Log.i("jiating", "AgendaListView......refresh");
		
		mGNAgendaByDayAdapter.refresh(mTime, -1, null, forced, false);
	}

	public void deleteSelectedEvent() {
		Log.i("jiating", "AgendaListView......deleteSelectedEvent");
		int position = getSelectedItemPosition();
		Event event = mGNAgendaByDayAdapter.getEventByPosition(position);
		if (event != null) {
			mDeleteEventHelper.delete(event.startMillis, event.endMillis,
					event.id, -1);
		}
	}

	public View getFirstVisibleView() {
		Log.i("jiating", "AgendaListView......getFirstVisibleView");
		Rect r = new Rect();
		int childCount = getChildCount();
		for (int i = 0; i < childCount; ++i) {
			View listItem = getChildAt(i);
			listItem.getLocalVisibleRect(r);
			if (r.top >= 0) { // if visible
				return listItem;
			}
		}
		return null;
	}

	public long getSelectedTime() {
		int position = getSelectedItemPosition();
		if (position >= 0) {
			Event event = mGNAgendaByDayAdapter.getEventByPosition(position);
			if (event != null) {
				return event.startMillis;
			}
		}
		return getFirstVisibleTime(null);
	}

	public long getFirstVisibleTime(Event e) {
		Log.i("jiating", "AgendaListView......getFirstVisibleTime");
		Event event = e;
		if (e == null) {
			event = getFirstVisibleEvent();
		}
		if (event != null) {
			Time t = new Time(mTimeZone);
			t.set(event.startMillis);
			// Save and restore the time since setJulianDay sets the time to
			// 00:00:00
			int hour = t.hour;
			int minute = t.minute;
			int second = t.second;
			t.setJulianDay(event.startDay);
			t.hour = hour;
			t.minute = minute;
			t.second = second;
			if (DEBUG) {
				t.normalize(true);
				Log.d(TAG, "first position had time " + t.toString());
			}
			return t.normalize(false);
		}
		return 0;
	}

	public Event getFirstVisibleEvent() {
		Log.i("jiating", "AgendaListView......getFirstVisibleEvent");
		int position = getFirstVisiblePosition();
		if (DEBUG) {
			Log.v(TAG, "getFirstVisiblePosition = " + position);
		}

		return mGNAgendaByDayAdapter.getEventByPosition(position);

	}
	
	public void setFirstVisibleEvent(){
		
	}

	public int getJulianDayFromPosition(int position) {
		Event info = mGNAgendaByDayAdapter.getEventByPosition(position);
		if (info != null) {
			return info.startDay;
		}
		return 0;
	}

	// Finds is a specific event (defined by start time and id) is visible
	public boolean isEventVisible(Time startTime) {

		if ( startTime == null) {
			return false;
		}

		View child = getChildAt(0);
		// View not set yet, so not child - return
		if (child == null) {
			return false;
		}
		int start = getPositionForView(child);
		long milliTime = startTime.toMillis(true);

		int count = mGNAgendaByDayAdapter.getCount();

//		for (int i = 0; i < count; i++) {
//			   if (i + start >= eventsInAdapter) {
//	                break;
//	            }
//	            EventInfo event = mWindowAdapter.getEventByPosition(i + start);
//	            if (event == null) {
//	                continue;
//	            }
//	            if (event.id == id && event.begin == milliTime) {
//	                View listItem = getChildAt(i);
//	                if (listItem.getBottom() <= getHeight() &&
//	                        listItem.getTop() >= 0) {
//	                    return true;
//	                }
//	            }
//			
//		}
		return false;
	}



	public void onResume() {
		Log.i("jiating", "AgendaListView......onResume");
		mTZUpdater.run();
		Utils.setMidnightUpdater(mHandler, mMidnightUpdater, mTimeZone);

		if(mGNAgendaByDayAdapter==null){
			mGNAgendaByDayAdapter=(GNAgendaByDayAdapter) getAdapter();
		}
		mGNAgendaByDayAdapter.refresh();
	}

	public void onPause() {
		Utils.resetMidnightUpdater(mHandler, mMidnightUpdater);

	}

	/**
	 * M: for sub class to get event info
	 * 
	 * @param position
	 * @return
	 */
	protected long getEventIdByPosition(int position) {
		if (position > 0 && position <= mGNAgendaByDayAdapter.getCount()) {
			// / M: getEventByPosition() may return null
			Event event = mGNAgendaByDayAdapter.getEventByPosition(position);
			if (event != null) {
				return event.id;
			}
		}
		return -1;
	}

	
	private String formatDateRange(long timeStart){
		String timeStr = "";
		timeStr = DayUtils.getMillisecondToHour(timeStart);
		return timeStr;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		Log.i("jiating","onItemLongClick");
		 CharSequence[] mLongPressItems = new CharSequence[] {
				 mContext.getResources().getString(R.string.gn_day_event_view),
				 mContext.getResources().getString(R.string.gn_day_event_edit),
				 mContext.getResources().getString(R.string.gn_day_event_del),
				 mContext.getResources().getString(R.string.gn_day_event_share)
            };
		Statistics.onEvent(mContext, Statistics.SLIDING_VIEW_ALL_ACTIVITY_LONG_CLICK_ITEM);
	     if (id != -1) {
	            // Switch to the EventInfo view
	    	 final Event event= mGNAgendaByDayAdapter.getEventByPosition(position);
	        
	            // If events are shown to the side of the agenda list , do nothing
	            // when the same event is selected , otherwise show the selected event.

	            if (event != null  ) {
	                long startTime = event.startMillis;
	                long endTime = event.endMillis;
	                // Holder in view holds the start of the specific part of a multi-day event ,
	                // use it for the goto
	                long holderStartTime;
	                Object holder =view.getTag();
	                if (holder instanceof GNAgendaByDayAdapter.ViewHolder) {
	                    holderStartTime = ((GNAgendaByDayAdapter.ViewHolder) holder).startTimeMilli;
	                } else {
	                    holderStartTime = startTime;
	                }
	                if (event.allDay) {
	                    startTime = Utils.convertAlldayLocalToUTC(mTime, startTime, mTimeZone);
	                    endTime = Utils.convertAlldayLocalToUTC(mTime, endTime, mTimeZone);
	                }
	                mTime.set(startTime);
	                int flags = DateUtils.FORMAT_SHOW_WEEKDAY;
	                flags |= DateUtils.FORMAT_24HOUR;
	                String mLongPressTitle = formatDateRange(event.startMillis);
	                new AuroraAlertDialog.Builder(mContext).setTitle(mLongPressTitle)
	                .setItems(mLongPressItems, new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int which) {
	        				CalendarController controller = CalendarController
	        						.getInstance(mContext);

	                        switch (which) {
	        				case 0:
	        					Log.i("jiating","AgendaListView.....onLongItem...VIEW_EVENT");
	        					Statistics.onEvent(mContext, Statistics.SLIDING_VIEW_ALL_ACTIVITY_LONG_CLICK_LOOK);
	        					//Gionee <jiating><2013-07-04> modify for CR00833259 begin
//	               				controller.sendEventRelatedEventWithExtra(this,
//	            						EventType.VIEW_EVENT, event.id, event.startMillis,
//	            						event.endMillis, 0, 0, CalendarController.EventInfo
//	            								.buildViewExtraLong(
//	            										Attendees.ATTENDEE_STATUS_NONE,
//	            										event.allDay), System.currentTimeMillis());
	               			
	               				viewEvent(event);
	               				//Gionee <jiating><2013-07-04> modify for CR00833259 end
	        					break;
	        				case 1:
	        					Statistics.onEvent(mContext, Statistics.SLIDING_VIEW_ALL_ACTIVITY_LONG_CLICK_EDIT);
	               				controller.sendEventRelatedEventWithExtra(this,
	            						EventType.EDIT_EVENT, event.id, event.startMillis,
	            						event.endMillis, 0, 0, CalendarController.EventInfo
	            								.buildViewExtraLong(
	            										Attendees.ATTENDEE_STATUS_NONE,
	            										event.allDay), System.currentTimeMillis());
	        					break;
	        				case 2:
	        				
//	        					if(agendaFragment!=null){
//	        						agendaFragment.deleteEvent(event.id);
//	        					}
//	        					 Time t = null;
//	        			         int viewType = ViewType.CURRENT;
//	        			         long extras = CalendarController.EXTRA_GOTO_TIME;
//	        					 viewType = ViewType.CURRENT;
//	        			         t = new Time();
//	        			         t.setToNow();
//	        			         extras |= CalendarController.EXTRA_GOTO_TODAY;
//	        			         controller.sendEvent(this, EventType.GO_TO, t, null, t, -1, viewType, extras, null, null);
	        					Statistics.onEvent(mContext, Statistics.SLIDING_VIEW_ALL_ACTIVITY_LONG_CLICK_DELETE);
	               				controller.sendEventRelatedEventWithExtra(GNAgendaActivity.class,
	            						EventType.DELETE_EVENT, event.id, event.startMillis,
	            						event.endMillis, 0, 0, CalendarController.EventInfo
	            								.buildViewExtraLong(
	            										Attendees.ATTENDEE_STATUS_NONE,
	            										event.allDay),System.currentTimeMillis());
	              
	        					break;
	        				case 3:
	        					Statistics.onEvent(mContext, Statistics.SLIDING_VIEW_ALL_ACTIVITY_LONG_CLICK_SHARE);
	        					DayUtils.sendShareEvent(mContext,event.id);
	        					break;
	        				default:
	        					break;
	        				} 
	                    }
	                }).show().setCanceledOnTouchOutside(true);
//	                CalendarController controller = CalendarController.getInstance(mContext);
//	                controller.sendEventRelatedEventWithExtra(this, EventType.VIEW_EVENT, event.id,
//	                        startTime, endTime, 0, 0, CalendarController.EventInfo.buildViewExtraLong(
//	                                Attendees.ATTENDEE_STATUS_NONE, event.allDay), holderStartTime);
	            }
	        }
		
		return true;
	}

	public void setFragment(GNAgendaFragment gnAgendaFragment) {
		// TODO Auto-generated method stub
		agendaFragment=gnAgendaFragment;
	}

	

	
}


//Gionee <jiating> <2013-04-24> modify for CR00000000  end
