package com.gionee.calendar.month;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Reminders;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.animation.TranslateAnimation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.calendar.Event;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.android.calendar.month.MonthByWeekFragment;
import com.aurora.calendar.CycleColorDrawable;
import com.gionee.calendar.view.GNAnimationutils;

public class GNMonthAgendaArrayAdapter extends ArrayAdapter<Event> {

    private static final String LOG_TAG = "GNMonthAgendaArrayAdapter";

    private Context mContext;
    private LayoutInflater mInflater = null;
    private MonthByWeekFragment mParentFragment;
    private HashMap<Integer, Boolean> mEventAlarmMap = new HashMap<Integer, Boolean>();

    private final Formatter mFormatter;
    private final StringBuilder mStringBuilder;

    // private static final int ITEM_LAYOUT_ID = R.layout.gn_month_agenda_list_item;
    private static final int ITEM_LAYOUT_ID = R.layout.aurora_month_agenda_item;

	public GNMonthAgendaArrayAdapter(Context context, ArrayList<Event> list, MonthByWeekFragment fragment) {
		super(context, ITEM_LAYOUT_ID, list);

        mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mParentFragment = fragment;
        mEventAlarmMap.clear();

        mStringBuilder = new StringBuilder(50);
        mFormatter = new Formatter(mStringBuilder, Locale.getDefault());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		/*View view = null;

		if(convertView == null) {
			view = mInflater.inflate(ITEM_LAYOUT_ID, parent, false);
		} else {
			view = convertView;
		}*/
		View view = null;
        if (convertView == null) {
            view = mInflater.inflate(com.aurora.R.layout.aurora_slid_listview, null);
            RelativeLayout customView = (RelativeLayout) view.findViewById(com.aurora.R.id.aurora_listview_front);
            mInflater.inflate(ITEM_LAYOUT_ID, customView);
        } else {
            view = convertView;
        }

        view.findViewById(com.aurora.R.id.control_padding).setPadding(0, 0, 0, 0);
        // view.findViewById(com.aurora.R.id.aurora_listview_divider).setVisibility(View.VISIBLE);

		Event event = this.getItem(position);

		// View vColorLine = (View) view.findViewById(R.id.month_agenda_start_line);
		// vColorLine.setBackgroundColor(event.color);

		TextView tvTitle = (TextView) view.findViewById(R.id.month_agenda_title);
		tvTitle.setText(event.title);

		ImageView emailView = (ImageView) view.findViewById(R.id.month_agenda_email);
		if (Utils.EMAIL_REMINDER_ACCOUNT_NAME.equals(event.ownerAccount)) {
			emailView.setVisibility(View.VISIBLE);
		} else {
			emailView.setVisibility(View.GONE);
		}

		/*CycleColorDrawable colorIcon = new CycleColorDrawable(event.color);
        colorIcon.setCenter(8, 0);
        colorIcon.setRadius(7);
        tvTitle.setCompoundDrawables(colorIcon, null, null, null);*/

		TextView tvTime = (TextView) view.findViewById(R.id.month_agenda_time);

        // When
        long begin = event.startMillis;
        long end = event.endMillis;
        boolean allDay = event.allDay;
        String eventTz = event.timeZone;
        int flags = 0;
        String whenString;
        // It's difficult to update all the adapters so just query this each
        // time we need to build the view.
        String tzString = Utils.getTimeZone(mContext, null);
        if (allDay) {
            tzString = Time.TIMEZONE_UTC;
        } else if (end - begin < DateUtils.DAY_IN_MILLIS) {
            flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_MONTH;
        } else {
            flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH;
        }
        if (DateFormat.is24HourFormat(mContext)) {
            flags |= DateUtils.FORMAT_24HOUR;
        }
        mStringBuilder.setLength(0);
        whenString = DateUtils.formatDateRange(mContext, mFormatter, begin, end, flags, tzString).toString();
        if (!allDay && !TextUtils.equals(tzString, eventTz)) {
            String displayName;
            // Figure out if this is in DST
            Time date = new Time(tzString);
            date.set(begin);

            TimeZone tz = TimeZone.getTimeZone(tzString);
            if (tz == null || tz.getID().equals("GMT")) {
                displayName = tzString;
            } else {
                displayName = tz.getDisplayName(date.isDst != 0, TimeZone.SHORT);
            }
            whenString += " (" + displayName + ")";
        }
        tvTime.setText(whenString);

        final ImageView reminderView = (ImageView) view.findViewById(R.id.month_agenda_reminder);
        if (event.hasAlarm) {
            reminderView.setImageResource(R.drawable.aurora_event_reminder_open);
        } else {
            reminderView.setImageResource(R.drawable.aurora_event_reminder_close);
        }

        mEventAlarmMap.put(position, event.hasAlarm);
        final int pos = position;

        final long eventId = event.id;
        final boolean hasAlarm = event.hasAlarm;
        View rightView = view.findViewById(R.id.month_agenda_right);
        rightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // mParentFragment.setCanEventChange();
            	mParentFragment.setNoEventChange(2000);

                ContentResolver cr = mContext.getContentResolver();
                if (mEventAlarmMap.get(pos)/*hasAlarm*/) {
                    cr.delete(Reminders.CONTENT_URI, "event_Id=" + eventId, null);
                    Toast.makeText(mContext, R.string.aurora_agenda_reminder_close, Toast.LENGTH_SHORT).show();

                    mEventAlarmMap.put(pos, false);
                    reminderView.setImageResource(R.drawable.aurora_event_reminder_close);
                } else {
                    ContentValues values = new ContentValues();
                    values.put(Reminders.EVENT_ID, eventId);
                    values.put(Reminders.MINUTES, Utils.getDefaultReminderMinutes(mContext));
                    values.put(Reminders.METHOD, Reminders.METHOD_ALERT);
                    cr.insert(Reminders.CONTENT_URI, values);
                    Toast.makeText(mContext, R.string.aurora_agenda_reminder_open, Toast.LENGTH_SHORT).show();

                    mEventAlarmMap.put(pos, true);
                    reminderView.setImageResource(R.drawable.aurora_event_reminder_open);
                }
            }
        });

        View slidingSwitchView = view.findViewById(com.aurora.R.id.aurora_item_sliding_switch);
        if (Utils.NOTE_REMINDER_ACCOUNT_NAME.equals(event.ownerAccount)) {
            tvTime.setText(event.description);
            rightView.setVisibility(View.GONE);
            slidingSwitchView.setVisibility(View.VISIBLE);
        } else {
        	rightView.setVisibility(View.VISIBLE);
            slidingSwitchView.setVisibility(View.GONE);
        }

        // view.startAnimation(GNAnimationutils.getAgendaListItemAnimation(mContext, position));

		return view;
	}
}