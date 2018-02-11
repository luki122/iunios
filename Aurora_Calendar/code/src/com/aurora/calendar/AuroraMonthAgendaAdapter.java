package com.aurora.calendar;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.android.calendar.Event;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.android.calendar.month.MonthByWeekFragment;
import com.aurora.calendar.util.TimeUtils;
import com.aurora.commemoration.model.RememberDayInfo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract.Reminders;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AuroraMonthAgendaAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private MonthByWeekFragment mParentFragment;
	private List<RememberDayInfo> mRememberDays;
	private ArrayList<Event> mBirthdayEvents;
	private ArrayList<Event> mEvents;

	private final StringBuilder mStringBuilder;
	private final Formatter mFormatter;

	private HashMap<Integer, Boolean> mEventAlarmMap = new HashMap<Integer, Boolean>();

	public AuroraMonthAgendaAdapter(Context context, MonthByWeekFragment fragment, 
			List<RememberDayInfo> rememberDays, ArrayList<Event> birthdayEvents, ArrayList<Event> events) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mParentFragment = fragment;
		mRememberDays = rememberDays;
		mBirthdayEvents = birthdayEvents;
		mEvents = events;

		mStringBuilder = new StringBuilder(50);
		mFormatter = new Formatter(mStringBuilder, Locale.getDefault());

		mEventAlarmMap.clear();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(
					com.aurora.R.layout.aurora_slid_listview, null);
			RelativeLayout front = (RelativeLayout) convertView.findViewById(
					com.aurora.R.id.aurora_listview_front);
			if (mRememberDays == null || mRememberDays.isEmpty()) {
				mInflater.inflate(R.layout.aurora_month_agenda_item, front);
			} else {
				if (position < mRememberDays.size()) {
					mInflater.inflate(R.layout.aurora_month_remember_day, front);
				} else {
					mInflater.inflate(R.layout.aurora_month_agenda_item, front);
				}
			}
		}

		convertView.findViewById(com.aurora.R.id.control_padding).setPadding(0, 0, 0, 0);

		View slidingSwitchView = convertView.findViewById(com.aurora.R.id.aurora_item_sliding_switch);

		if (mRememberDays == null || mRememberDays.isEmpty()) {
			TextView titleView = (TextView) convertView.findViewById(R.id.month_agenda_title);
			TextView summaryView = (TextView) convertView.findViewById(R.id.month_agenda_time);
			ImageView emailView = (ImageView) convertView.findViewById(R.id.month_agenda_email);
			ImageView reminderView = (ImageView) convertView.findViewById(R.id.month_agenda_reminder);

			if (mBirthdayEvents.isEmpty()) {
				Event event = mEvents.get(position);
				updateView(position, event, titleView, summaryView, emailView, reminderView, slidingSwitchView);
			} else if (position == 0) {
				titleView.setText(R.string.aurora_birthday_reminder);

				String name = mBirthdayEvents.get(0).title.toString();
				if (mBirthdayEvents.size() == 1) {
					summaryView.setText(mContext.getString(R.string.aurora_birthday_only_one, name));
				} else {
					summaryView.setText(mContext.getString(R.string.aurora_birthday_have_other, name,
							mBirthdayEvents.size()));
				}

				reminderView.setBackgroundResource(R.drawable.aurora_birthday_reminder);
				reminderView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						sendBirthdayWish();
					}
				});

				slidingSwitchView.setVisibility(View.VISIBLE);
			} else {
				Event event = mEvents.get(position - 1);
				updateView(position, event, titleView, summaryView, emailView, reminderView, slidingSwitchView);
			}
		} else {
			int rememberDaysCount = mRememberDays.size();
			if (position < rememberDaysCount) {
				RememberDayInfo rememberDay = mRememberDays.get(position);

				ImageView iconView = (ImageView) convertView.findViewById(R.id.remember_day_icon);
				TextView titleView = (TextView) convertView.findViewById(R.id.remember_day_title);

				String title = rememberDay.getTitle();
				String date = rememberDay.getDay();

				int offDay = getOffDay(TimeUtils.getLongFromStrTime1(date));
				if (offDay >= 0) {
					iconView.setImageResource(R.drawable.aurora_agenda_icon_pink);
					titleView.setText(mContext.getString(R.string.aurora_remember_day_futrue, title, offDay));
				} else {
					iconView.setImageResource(R.drawable.aurora_agenda_icon_blue);
					titleView.setText(mContext.getString(R.string.aurora_remember_day_past, title, -offDay));
				}

				slidingSwitchView.setVisibility(View.VISIBLE);
			} else {
				TextView titleView = (TextView) convertView.findViewById(R.id.month_agenda_title);
				TextView summaryView = (TextView) convertView.findViewById(R.id.month_agenda_time);
				ImageView emailView = (ImageView) convertView.findViewById(R.id.month_agenda_email);
				ImageView reminderView = (ImageView) convertView.findViewById(R.id.month_agenda_reminder);

				if (mBirthdayEvents.isEmpty()) {
					Event event = mEvents.get(position - rememberDaysCount);
					updateView(position, event, titleView, summaryView, emailView, reminderView, slidingSwitchView);
				} else if (position == rememberDaysCount) {
					titleView.setText(R.string.aurora_birthday_reminder);

					String name = mBirthdayEvents.get(0).title.toString();
					if (mBirthdayEvents.size() == 1) {
						summaryView.setText(mContext.getString(R.string.aurora_birthday_only_one, name));
					} else {
						summaryView.setText(mContext.getString(R.string.aurora_birthday_have_other, name,
								mBirthdayEvents.size()));
					}

					reminderView.setBackgroundResource(R.drawable.aurora_birthday_reminder);
					reminderView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View arg0) {
							sendBirthdayWish();
						}
					});

					slidingSwitchView.setVisibility(View.VISIBLE);
				} else {
					Event event = mEvents.get(position - rememberDaysCount - 1);
					updateView(position, event, titleView, summaryView, emailView, reminderView, slidingSwitchView);
				}
			}
		}

		return convertView;
	}

	@Override
	public int getCount() {
		return mRememberDays == null ? 0 : mRememberDays.size() + 
				(mBirthdayEvents.isEmpty() ? 0 : 1) + mEvents.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	private void updateView(final int position, Event event, TextView titleView, TextView summaryView, ImageView emailView,
			final ImageView reminderView, View slidingSwitchView) {

		titleView.setText(event.title);
		summaryView.setText(getWhenString(event));

		if (Utils.EMAIL_REMINDER_ACCOUNT_NAME.equals(event.ownerAccount)) {
			emailView.setVisibility(View.VISIBLE);
		} else {
			emailView.setVisibility(View.GONE);
		}

        if (event.hasAlarm) {
            reminderView.setBackgroundResource(R.drawable.aurora_agenda_reminder_open);
        } else {
            reminderView.setBackgroundResource(R.drawable.aurora_agenda_reminder_close);
        }

        mEventAlarmMap.put(position, event.hasAlarm);
        final long eventId = event.id;

        reminderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // mParentFragment.setCanEventChange();
            	mParentFragment.setNoEventChange(2000);

                ContentResolver cr = mContext.getContentResolver();
                if (mEventAlarmMap.get(position)) {
                    cr.delete(Reminders.CONTENT_URI, "event_Id=" + eventId, null);
                    Toast.makeText(mContext, R.string.aurora_agenda_reminder_close, Toast.LENGTH_SHORT).show();

                    mEventAlarmMap.put(position, false);
                    reminderView.setBackgroundResource(R.drawable.aurora_agenda_reminder_close);
                } else {
                    ContentValues values = new ContentValues();
                    values.put(Reminders.EVENT_ID, eventId);
                    values.put(Reminders.MINUTES, Utils.getDefaultReminderMinutes(mContext));
                    values.put(Reminders.METHOD, Reminders.METHOD_ALERT);
                    cr.insert(Reminders.CONTENT_URI, values);
                    Toast.makeText(mContext, R.string.aurora_agenda_reminder_open, Toast.LENGTH_SHORT).show();

                    mEventAlarmMap.put(position, true);
                    reminderView.setBackgroundResource(R.drawable.aurora_agenda_reminder_open);
                }
            }
        });

        if (Utils.NOTE_REMINDER_ACCOUNT_NAME.equals(event.ownerAccount)) {
            summaryView.setText(event.description);
            reminderView.setVisibility(View.GONE);
            slidingSwitchView.setVisibility(View.VISIBLE);
        } else {
            reminderView.setVisibility(View.VISIBLE);
            slidingSwitchView.setVisibility(View.GONE);
        }
	}

	private String getWhenString(Event event) {
		long begin = event.startMillis;
        long end = event.endMillis;
        boolean allDay = event.allDay;
        String eventTz = event.timeZone;
        int flags = 0;
        String whenString;
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

        return whenString;
	}

	private void sendBirthdayWish() {
		Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.aurora_happy_birthday));
        intent.putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.aurora_happy_birthday));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.aurora_wish_birthday)));
	}

	private int getOffDay(long millis) {
		Time time = new Time();
		time.setToNow();
		time.normalize(true);
		int todayJulianDay = Time.getJulianDay(time.toMillis(true), time.gmtoff);

		time.set(millis);
		time.normalize(true);
		int theDay = Time.getJulianDay(time.toMillis(true), time.gmtoff);

		return theDay - todayJulianDay;
	}

}
