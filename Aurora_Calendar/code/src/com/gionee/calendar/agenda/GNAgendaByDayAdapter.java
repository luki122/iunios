/*
 * Copyright (C) 2008 The Android Open Source Project
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
// Gionee <jiating> <2013-04-24> modify for CR00000000  begin 
// Gionee <jiating> <2013-04-24> modify for CR00000000  begin 
package com.gionee.calendar.agenda;
//Gionee <jiating> <2013-04-24> modify for CR00000000  begin 
import com.android.calendar.CalendarController;
import com.android.calendar.ColorChipView;
import com.android.calendar.Event;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.gionee.calendar.GNDateTextUtils;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.provider.CalendarContract.Attendees;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;

import com.mediatek.calendar.LogUtil;

public class GNAgendaByDayAdapter extends BaseAdapter {

	public static final int TYPE_DAY = 0;
	public static final int TYPE_MEETING = 1;
	static final int TYPE_LAST = 2;

	private final Context mContext;
	private final LayoutInflater mInflater;
	private ArrayList<RowInfo> mRowInfos;
	private int mTodayJulianDay;
	private final String mNoTitleLabel;
	private final Resources mResources;
	private Time mTmpTime;
	private String mTimeZone;
	// Note: Formatter is not thread safe. Fine for now as it is only used by
	// the main thread.
	private final Formatter mFormatter;
	private final StringBuilder mStringBuilder;

	static class ViewHolder {
		TextView dayView;
		TextView dateView;
		int julianDay;
		boolean grayed;

		/* Event */
		TextView title;
		TextView when;

		View selectedMarker;
		LinearLayout textContainer;
		long instanceId;
		ColorChipView colorChip;
		long startTimeMilli;
		boolean allDay;

	}

	private final Runnable mTZUpdater = new Runnable() {
		@Override
		public void run() {
			Log.i("jiating", "GNAgendaByDayAdapter......mTZUpdater");
			mTimeZone = Utils.getTimeZone(mContext, this);
			mTmpTime = new Time(mTimeZone);
			notifyDataSetChanged();
		}
	};

	public GNAgendaByDayAdapter(Context context) {
		mContext = context;
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mStringBuilder = new StringBuilder(50);
		mFormatter = new Formatter(mStringBuilder, Locale.getDefault());
		mTimeZone = Utils.getTimeZone(context, mTZUpdater);
		mTmpTime = new Time(mTimeZone);
		mResources = context.getResources();
		mRowInfos = new ArrayList<GNAgendaByDayAdapter.RowInfo>();
		mNoTitleLabel = mResources.getString(R.string.no_title_label);
	}

	public int getCount() {
		if (mRowInfos != null) {
			Log.e("jiating",
					"GNAgendaByDayAdapter....getCountmRowInfo.size= +="
							+ mRowInfos.size());
			return mRowInfos.size();
		}
		return 0;

	}

	public Object getItem(int position) {
		if (mRowInfos != null) {
			Log.e("jiating", "GNAgendaByDayAdapter....getItem="
					+ "mRowInfo != null");
			RowInfo row = mRowInfos.get(position);

		}
		return null;

	}

	public long getItemId(int position) {

		return position;

	}

	@Override
	public int getViewTypeCount() {
		return TYPE_LAST;
	}

	@Override
	public int getItemViewType(int position) {
		return mRowInfos != null && mRowInfos.size() > position ? mRowInfos
				.get(position).mType : TYPE_DAY;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Log.i("jiating", "GNAgendaByDayAdapter......getView");

		RowInfo row = mRowInfos.get(position);
		if (row.mType == TYPE_DAY) {
			Log.e("jiating", "TYPE_DAY....");
			ViewHolder holder = null;
			View agendaDayView = null;
			if ((convertView != null) && (convertView.getTag() != null)) {
				// Listview may get confused and pass in a different type of
				// view since we keep shifting data around. Not a big problem.
				Object tag = convertView.getTag();
				if (tag instanceof ViewHolder) {
					agendaDayView = convertView;
					holder = (ViewHolder) tag;
					holder.julianDay = row.mDay;
				}
			}

			if (holder == null) {
				// Create a new AgendaView with a ViewHolder for fast access to
				// views w/o calling findViewById()
				holder = new ViewHolder();
				agendaDayView = mInflater.inflate(R.layout.agenda_day, parent,
						false);
				holder.dayView = (TextView) agendaDayView
						.findViewById(R.id.day);
				holder.dateView = (TextView) agendaDayView
						.findViewById(R.id.date);
				holder.julianDay = row.mDay;
				holder.grayed = false;
				agendaDayView.setTag(holder);
			}

			// Re-use the member variable "mTime" which is set to the local
			// time zone.
			// It's difficult to find and update all these adapters when the
			// home tz changes so check it here and update if needed.
			String tz = Utils.getTimeZone(mContext, mTZUpdater);
			if (!TextUtils.equals(tz, mTmpTime.timezone)) {
				mTimeZone = tz;
				mTmpTime = new Time(tz);
			}

			// Build the text for the day of the week.
			// Should be yesterday/today/tomorrow (if applicable) + day of the
			// week

			Time date = mTmpTime;
			long millis = date.setJulianDay(row.mDay);
			int flags = DateUtils.FORMAT_SHOW_WEEKDAY;
			mStringBuilder.setLength(0);

			String dayViewText = GNDateTextUtils.getDayOfWeekString(row.mDay,
					mTodayJulianDay, millis, mContext);

			// Build text for the date
			// Format should be month day

			mStringBuilder.setLength(0);
			flags = DateUtils.FORMAT_SHOW_DATE;
			// String dateViewText = DateUtils.formatDateRange(mContext,
			// mFormatter, millis, millis,
			// flags, mTimeZone).toString();
			String dateViewText = GNDateTextUtils
					.buildMonthYearDayUseFormat(millis);
			Log.i("jiating", "GNAgendaByDayAdapter......getView...dayViewText="
					+ dayViewText + "dateViewText=" + dateViewText);
			// if (GNAgendaWindowAdapter.BASICLOG) {
			// dayViewText += " P:" + position;
			// dateViewText += " P:" + position;
			// }
			Log.i("jiating", "");
			holder.dayView.setText(dayViewText);
			holder.dateView.setText(dateViewText);

			return agendaDayView;
		} else if (row.mType == TYPE_MEETING) {
			Log.e("jiating", "TYPE_MEETING....");
			ViewHolder holder = null;
			View agendaDayView = null;
			if ((convertView != null) && (convertView.getTag() != null)) {
				// Listview may get confused and pass in a different type of
				// view since we keep shifting data around. Not a big problem.
				Object tag = convertView.getTag();
				if (tag instanceof ViewHolder) {
					agendaDayView = convertView;
					holder = (ViewHolder) tag;
					holder.julianDay = row.mDay;
				}
			}

			if (holder == null) {
				// Create a new AgendaView with a ViewHolder for fast access to
				// views w/o calling findViewById()
				holder = new ViewHolder();
				agendaDayView = mInflater.inflate(R.layout.agenda_item, parent,
						false);
				holder.title = (TextView) agendaDayView
						.findViewById(R.id.title);
				holder.when = (TextView) agendaDayView.findViewById(R.id.when);
		
				holder.textContainer = (LinearLayout) agendaDayView
						.findViewById(R.id.agenda_item_text_container);
				holder.selectedMarker = agendaDayView
						.findViewById(R.id.selected_marker);
				holder.colorChip = (ColorChipView) agendaDayView
						.findViewById(R.id.agenda_item_color);
				agendaDayView.setTag(holder);
			}

			TextView title = holder.title;
			TextView when = holder.when;

			holder.instanceId = row.event.id;

			/* Calendar Color */
			int color = Utils.getDisplayColorFromColor(row.event.color);
			holder.colorChip.setColor(color);

			// What
			String titleString = (String) row.event.title;
			if (titleString == null || titleString.length() == 0) {
				titleString = mNoTitleLabel;
			}
			title.setText(titleString);

			// When
			long begin = row.event.startMillis;
			long end = row.event.endMillis;
			String eventTz = row.event.timeZone;
			int flags = 0;
			String whenString;
			// It's difficult to update all the adapters so just query this each
			// time we need to build the view.
			boolean allDay = row.event.allDay;
			String tzString = Utils.getTimeZone(mContext, mTZUpdater);
			if (allDay) {
				tzString = Time.TIMEZONE_UTC;
			} else {
				flags = DateUtils.FORMAT_SHOW_TIME;
			}
			if (DateFormat.is24HourFormat(mContext)) {
				flags |= DateUtils.FORMAT_24HOUR;
			}

			mStringBuilder.setLength(0);
			whenString = DateUtils.formatDateRange(mContext, mFormatter, begin,
					end, flags, tzString).toString();
			//Gionee <jiating><2013-07-01> modify for CR00819533 begin
			Log.i("jiating","GNAgendaByDayAdapter....whenString="+whenString);
			if(whenString.contains(mContext.getString(R.string.gn_agenda_time_noon))){
				
				whenString=whenString.replace(mContext.getString(R.string.gn_agenda_time_noon), mContext.getString(R.string.gn_agenda_time_noon_replace));
			}
			
			//Gionee <jiating><2013-07-01> modify for CR00819533 end
			if (!allDay && !TextUtils.equals(tzString, eventTz)) {
				String displayName;
				// Figure out if this is in DST
				Time date = new Time(tzString);
				date.set(begin);

				TimeZone tz = TimeZone.getTimeZone(tzString);
				if (tz == null || tz.getID().equals("GMT")) {
					displayName = tzString;
				} else {
					displayName = tz.getDisplayName(date.isDst != 0,
							TimeZone.SHORT);
				}
				whenString += " (" + displayName + ")";
			}
			when.setText(whenString);

			return agendaDayView;

		} else {
			// Error

			throw new IllegalStateException("Unknown event type:" + row.mType);

		}
	}

	public void clearDayHeaderInfo() {
		mRowInfos = null;
	}

	public void calculateDays(ArrayList<Event> events) {
		Log.i("jiating", "AgendaListView......calculateDays");

		ArrayList<RowInfo> rowInfos = new ArrayList<RowInfo>();
		int prevStartDay = -1;

		Time tempTime = new Time(mTimeZone);
		long now = System.currentTimeMillis();
		tempTime.set(now);
		mTodayJulianDay = Time.getJulianDay(now, tempTime.gmtoff);
		int startDay = -1;
		int count = events.size();
		for (int i = 0; i < count; i++) {
			Event event = events.get(i);
			if (startDay != event.startDay) {

				rowInfos.add(new RowInfo(TYPE_DAY, event.startDay));
				startDay = event.startDay;
			}

			rowInfos.add(new RowInfo(TYPE_MEETING, event.startDay, event));

		}

		mRowInfos = rowInfos;
	}

	private static class RowInfo {
		// mType is either a day header (TYPE_DAY) or an event (TYPE_MEETING)
		final int mType;

		final int mDay; // Julian day
		final Event event;

		RowInfo(int type, int julianDay, Event event) {
			Log.i("jiating", "AgendaListView......RowInfo");
			mType = type;
			mDay = julianDay;
			this.event = event;

		}

		RowInfo(int type, int julianDay) {
			mType = type;
			mDay = julianDay;
			event = null;

		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			StringBuilder sb = new StringBuilder();
			if (mType == TYPE_DAY) {
				sb.append(mType).append(mDay);
			} else {
				sb.append(event);
			}
			return sb.toString();
		}
	}

	public void refresh(Time goToTime, long id, String searchQuery,
			boolean forced, boolean refreshEventInfo) {
		Log.i("jiating", "AgendaWindowAdapter......refresh");
		
		notifyDataSetChanged();
		return;
	}

	public void refresh() {
		Log.i("jiating", "AgendaWindowAdapter......refresh");
		notifyDataSetChanged();
		return;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		if (mRowInfos != null && position < mRowInfos.size()) {
			RowInfo row = mRowInfos.get(position);
			return row.mType == TYPE_MEETING;
		}
		return true;
	}

	public Event getEventByPosition(int position) {
		return mRowInfos.get(position).event;
	}
	
	
	
	 public  int findEventPositionNearestTime(Time time) {
        
	
	        long timeInMillis = time.normalize(true);
	        int day = Time.getJulianDay(timeInMillis, time.gmtoff);
	     
	        Log.i("jiating","findEventPositionNearestTime....day="+day+"time="+timeInMillis);
	        
	        int position=-1;
	        synchronized (mRowInfos) {
	        	int count =mRowInfos.size();
	        	int minDate = -1;
	        	
	        	boolean first=true;
	            for (int i=0;i< count;i++) {
	            	if(mRowInfos.get(i).mType==TYPE_DAY){
	            		if(mRowInfos.get(i).mDay-day==0){
	            			 position=i;
	            			break;
	            		}
	            		if(first && (mRowInfos.get(i).mDay-day)>0){
	            	
	            			 minDate=day-mRowInfos.get(i).mDay;
	            			 first=false;
	            			 position=i;
	            			 continue;
	            		
	            		}else if(first && (mRowInfos.get(i).mDay-day)<0){
	            			continue;
	            		}
	            		
	            		if(minDate > (mRowInfos.get(i).mDay-day)){
	            			 minDate=mRowInfos.get(i).mDay-day;
	            			 position=i;
	            		}
	            		
	 	                    
	 	                }
	            	}
	            
	            
	               
	            
	        }
	        if(position==-1){
            	position=mRowInfos.size()-1;
            }
            Log.i("jiating","findEventPositionNearestTime....position="+position);
            return position;
	    }
}

//Gionee <jiating> <2013-04-24> modify for CR00000000  end
