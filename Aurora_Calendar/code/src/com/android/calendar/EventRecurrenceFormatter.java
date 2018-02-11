/*
 * Copyright (C) 2006 The Android Open Source Project
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

import com.android.calendarcommon2.EventRecurrence;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.util.TimeFormatException;

import java.util.Calendar;

public class EventRecurrenceFormatter {
	
	private static int[] mMonthRepeatByDayOfWeekIds;
	private static String[][] mMonthRepeatByDayOfWeekStrs;
	
	public static String getRepeatString(Context context, Resources r, EventRecurrence recurrence
//			, boolean includeEndString
			) {

		String endString = "";
//		if (includeEndString) {
//			StringBuilder sb = new StringBuilder();
//			if (recurrence.until != null) {
//				try {
//					Time t = new Time();
//					t.parse(recurrence.until);
//					final String dateStr = DateUtils.formatDateTime(context,
//							t.toMillis(false), DateUtils.FORMAT_NUMERIC_DATE);
//					sb.append(r.getString(R.string.endByDate, dateStr));
//				} catch (TimeFormatException e) {
//				}
//			}
//
//			if (recurrence.count > 0) {
//				sb.append(r.getQuantityString(R.plurals.endByCount,
//						recurrence.count, recurrence.count));
//			}
//			endString = sb.toString();
//		}
		Time mStartTime = recurrence.startDate;
		// TODO Implement "Until" portion of string, as well as custom settings
		int interval = recurrence.interval <= 1 ? 1 : recurrence.interval;
		switch (recurrence.freq) {
		case EventRecurrence.DAILY:
			return r.getString(R.string.daily) + endString;
		case EventRecurrence.WEEKLY: {
			if (recurrence.repeatsOnEveryWeekDay()) {
				return r.getString(R.string.aurora_every_weekday) + endString;
			} else {
				String string;
				
				int dayOfWeekLength = DateUtils.LENGTH_MEDIUM;
				if (recurrence.bydayCount == 1) {
					dayOfWeekLength = DateUtils.LENGTH_LONG;
				}
				
				String format = r.getString(R.string.aurora_weekly);
				StringBuilder days = new StringBuilder();

				// Do one less iteration in the loop so the last element is
				// added out of the
				// loop. This is done so the comma is not placed after the last
				// item.
				if (recurrence.bydayCount > 0) {
					int count = recurrence.bydayCount - 1;
					
					for (int i = 0; i < count; i++) {
						days.append(dayToString(recurrence.byday[i], dayOfWeekLength));
						days.append(", ");
					}
					days.append(dayToString(recurrence.byday[count], dayOfWeekLength));

					string = days.toString();
//					return String.format(format, days.toString());
				} else {
					
					// There is no "BYDAY" specifier, so use the day of the
					// first event. For this to work, the setStartDate()
					// method must have been used by the caller to set the
					// date of the first event in the recurrence.
					if (recurrence.startDate == null) {
						return null;
					}
					
					int day = EventRecurrence
							.timeDay2Day(recurrence.startDate.weekDay);
					string = dayToString(day, DateUtils.LENGTH_LONG);
				}

//				return r.getQuantityString(R.plurals.weekly, interval, interval, string);

				Log.e("liumxxx","string-------------"+string);
//				return string;
				return String.format(format, string);
			}
		}
		case EventRecurrence.MONTHLY: {
			
			if (recurrence.bydayCount == 1) {
				int weekday = recurrence.startDate.weekDay;
				// Cache this stuff so we won't have to redo work again later.
				cacheMonthRepeatStrings(r, weekday);
				int dayNumber = (recurrence.startDate.monthDay - 1) / 7;
				StringBuilder sb = new StringBuilder();
				sb.append(r.getString(R.string.monthly));
				sb.append(" (");
				sb.append(mMonthRepeatByDayOfWeekStrs[weekday][dayNumber]);
				sb.append(" )");
				sb.append(endString);
				return sb.toString();
			}
//			if (recurrence.repeatsMonthlyOnDayCount()) {
//				String format = r
//						.getString(R.string.aurora_monthly_on_day_count);
//				String[] ordinals = r.getStringArray(R.array.ordinal_labels);
//				int dayNumber = (mStartTime.monthDay - 1) / 7;
//				return String.format(format, ordinals[dayNumber], DateUtils
//						.getDayOfWeekString(mStartTime.weekDay + 1,
//								DateUtils.LENGTH_LONG));
//			} else {
//				String format = r.getString(R.string.aurora_monthly_on_day);
//
//				return String.format(format, mStartTime.monthDay);
//			}
			return r.getString(R.string.monthly);
		}
		case EventRecurrence.YEARLY:
			return r.getString(R.string.yearly_plain) + endString;
		}

		return null;
	}
	
	private static void cacheMonthRepeatStrings(Resources r, int weekday) {
		if (mMonthRepeatByDayOfWeekIds == null) {
			mMonthRepeatByDayOfWeekIds = new int[7];
			mMonthRepeatByDayOfWeekIds[0] = R.array.aurora_repeat_by_nth_sun;
			mMonthRepeatByDayOfWeekIds[1] = R.array.aurora_repeat_by_nth_mon;
			mMonthRepeatByDayOfWeekIds[2] = R.array.aurora_repeat_by_nth_tues;
			mMonthRepeatByDayOfWeekIds[3] = R.array.aurora_repeat_by_nth_wed;
			mMonthRepeatByDayOfWeekIds[4] = R.array.aurora_repeat_by_nth_thurs;
			mMonthRepeatByDayOfWeekIds[5] = R.array.aurora_repeat_by_nth_fri;
			mMonthRepeatByDayOfWeekIds[6] = R.array.aurora_repeat_by_nth_sat;
		}
		if (mMonthRepeatByDayOfWeekStrs == null) {
			mMonthRepeatByDayOfWeekStrs = new String[7][];
		}
		if (mMonthRepeatByDayOfWeekStrs[weekday] == null) {
			mMonthRepeatByDayOfWeekStrs[weekday] = r
					.getStringArray(mMonthRepeatByDayOfWeekIds[weekday]);
		}
	}

	/**
	 * Converts day of week to a String.
	 * 
	 * @param day
	 *            a EventRecurrence constant
	 * @return day of week as a string
	 */
	private static String dayToString(int day, int dayOfWeekLength) {
		return DateUtils.getDayOfWeekString(dayToUtilDay(day), dayOfWeekLength);
	}

	/**
	 * Converts EventRecurrence's day of week to DateUtil's day of week.
	 * 
	 * @param day
	 *            of week as an EventRecurrence value
	 * @return day of week as a DateUtil value.
	 */
	private static int dayToUtilDay(int day) {
		switch (day) {
		case EventRecurrence.SU:
			return Calendar.SUNDAY;
		case EventRecurrence.MO:
			return Calendar.MONDAY;
		case EventRecurrence.TU:
			return Calendar.TUESDAY;
		case EventRecurrence.WE:
			return Calendar.WEDNESDAY;
		case EventRecurrence.TH:
			return Calendar.THURSDAY;
		case EventRecurrence.FR:
			return Calendar.FRIDAY;
		case EventRecurrence.SA:
			return Calendar.SATURDAY;
		default:
			throw new IllegalArgumentException("bad day argument: " + day);
		}
	}
}
