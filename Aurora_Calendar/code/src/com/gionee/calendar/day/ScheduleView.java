package com.gionee.calendar.day;

import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController;
import com.android.calendar.Event;
import com.android.calendar.EventInfoFragment;
import com.android.calendar.Utils;

import aurora.app.AuroraAlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.CalendarContract.Attendees;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import com.android.calendar.R;
import com.gionee.calendar.statistics.StatisticalName;
import com.gionee.calendar.statistics.Statistics;
import com.gionee.calendar.view.Log;

//Gionee <pengwei>  <2013-04-12> modify for DayView begin
//Gionee <pengwei><2013-05-20> modify for CR00813693 begin
//Gionee <pengwei><20130807> modify for CR00850530 begin
public class ScheduleView implements DayScheduleInterface {
	private Event mEvent;
	private LayoutInflater mInflater;
	private Context mContext;
	private Time mCurrentTime;
	private final CharSequence[] mLongPressItems;
	ScheduleView(Context context, Event event,Time currentTime) {
		this.mEvent = event;
		this.mInflater = LayoutInflater.from(context);
		this.mContext = context;
		mCurrentTime = currentTime;
        mLongPressItems = new CharSequence[] {
        		context.getResources().getString(R.string.gn_day_event_view),
        		context.getResources().getString(R.string.gn_day_event_edit),
        		context.getResources().getString(R.string.gn_day_event_del),
        		context.getResources().getString(R.string.gn_day_event_share)
            };
	}

	@Override
	public View getView(View view) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
			Log.v("ScheduleView---view == null---");
			view = mInflater.inflate(R.layout.gn_day_agenda_list_item, null);
			holder = new ViewHolder();
			holder.line = (View) view.findViewById(R.id.day_agenda_start_line);
			holder.title = (TextView) view.findViewById(R.id.day_agenda_title);
			holder.time = (TextView) view.findViewById(R.id.day_agenda_time);
		holder.line.setBackgroundColor(mEvent.color);
		holder.title.setText(mEvent.title);
		String startTime = DayUtils.getMillisecondToTime(mEvent.startMillis);
		String endTime = DayUtils.getMillisecondToTime(mEvent.endMillis);
		holder.time.setText(startTime + "-" + endTime);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			    if (DayUtils.isFastDoubleClick()) {
			        return;
			    }
				Statistics.onEvent(mContext, Statistics.DAY_VIEW_CLICK_SCHEDULE_LIST);
				CalendarController controller = CalendarController
						.getInstance(mContext);
				controller.sendEventRelatedEventWithExtra(this,
						EventType.VIEW_EVENT, mEvent.id, mEvent.startMillis,
						mEvent.endMillis, 0, 0, CalendarController.EventInfo
								.buildViewExtraLong(
										Attendees.ATTENDEE_STATUS_NONE,
										mEvent.allDay), mCurrentTime.toMillis(true));
			}
		});
		view.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				Statistics.onEvent(mContext, Statistics.DAY_VIEW_LONG_CLICK_SCHEDULE_LIST);
				int flags = DateUtils.FORMAT_SHOW_WEEKDAY;
		        flags |= DateUtils.FORMAT_24HOUR;
		        String mLongPressTitle = formatDateRange(mEvent.startMillis);
		        
		        DayUtils.alertDialog = new AuroraAlertDialog.Builder(mContext).setTitle(mLongPressTitle)
                .setItems(mLongPressItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
        				CalendarController controller = CalendarController
        						.getInstance(mContext);
        				Log.v("ScheduleView---getView---");
                        switch (which) {
						case 0:
							Statistics.onEvent(mContext, Statistics.DAY_VIEW_LONG_CLICK_SCHEDULE_LIST_CHECK);
               				controller.sendEventRelatedEventWithExtra(this,
            						EventType.VIEW_EVENT, mEvent.id, mEvent.startMillis,
            						mEvent.endMillis, 0, 0, CalendarController.EventInfo
            								.buildViewExtraLong(
            										Attendees.ATTENDEE_STATUS_NONE,
            										mEvent.allDay), mCurrentTime.toMillis(true));
							break;
						case 1:
							Statistics.onEvent(mContext, Statistics.DAY_VIEW_LONG_CLICK_SCHEDULE_LIST_EDIT);
               				controller.sendEventRelatedEventWithExtra(this,
            						EventType.EDIT_EVENT, mEvent.id, mEvent.startMillis,
            						mEvent.endMillis, 0, 0, CalendarController.EventInfo
            								.buildViewExtraLong(
            										Attendees.ATTENDEE_STATUS_NONE,
            										mEvent.allDay), mCurrentTime.toMillis(true));
							break;
						case 2:
							Statistics.onEvent(mContext, Statistics.DAY_VIEW_LONG_CLICK_SCHEDULE_LIST_DEL);
               				controller.sendEventRelatedEventWithExtra(this,
            						EventType.DELETE_EVENT, mEvent.id, mEvent.startMillis,
            						mEvent.endMillis, 0, 0, CalendarController.EventInfo
            								.buildViewExtraLong(
            										Attendees.ATTENDEE_STATUS_NONE,
            										mEvent.allDay), mCurrentTime.toMillis(true));
							break;
						case 3:
							Statistics.onEvent(mContext, Statistics.DAY_VIEW_LONG_CLICK_SCHEDULE_LIST_SHARE);
							DayUtils.sendShareEvent(mContext,mEvent.id);
							break;
						default:
							break;
						} 
                    }
                }).create();
		        DayUtils.alertDialog.setCanceledOnTouchOutside(true);
		        DayUtils.alertDialog.show();
				return true;
			}
		});
		return view;
	}

	public final class ViewHolder {
		public View line;
		
		public TextView title;

		public TextView time;

	}

	private String formatDateRange(long timeStart){
		String timeStr = "";
		timeStr = /*DayUtils.getMillisecondToHour(timeStart) + "  " + */DayUtils.getLunarWeek(mContext,mCurrentTime.weekDay);
		return timeStr;
	}
	
}
//Gionee <pengwei><2013-05-20> modify for CR00813693 end
//Gionee <pengwei>  <2013-04-12> modify for DayView end
//Gionee <pengwei><20130807> modify for CR00850530 end