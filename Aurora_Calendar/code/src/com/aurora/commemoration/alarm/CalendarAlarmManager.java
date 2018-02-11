package com.aurora.commemoration.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.format.DateUtils;

import com.android.calendar.CalendarApp;
import com.aurora.commemoration.db.RememberDayDBHelper;
import com.aurora.commemoration.db.RememberDayDao;
import com.aurora.commemoration.model.RememberDayInfo;

import java.util.List;

public class CalendarAlarmManager {

	protected static final String TAG = "NoteAlarmManager";

	public static final int ACTION_INSERT = 1;
	public static final int ACTION_UPDATE = 2;
	public static final int ACTION_DELETE = 3;

	public static final String ACTION_CALENDAR_ALARM = "com.aurora.calendar.CALENDAR_ALARM";
	public static final String ACTION_CALENDAR_ALARM_CANCEL = "com.aurora.calendar.CALENDAR_ALARM_CANCEL";

	protected Context mContext = CalendarApp.ysApp;
	protected ContentResolver mResolver;
	private AlarmManager mAlarmManager;
	private RememberDayDao mRememberDb;

	private static CalendarAlarmManager mManager = null;

	public static synchronized CalendarAlarmManager getInstance() {
		if (mManager == null) {
			mManager = new CalendarAlarmManager();
		}
		return mManager;
	}

	public CalendarAlarmManager() {
		initializeWithContext();
	}

	protected void initializeWithContext() {
		Context context = mContext;
		mResolver = context.getContentResolver();
		mAlarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		mRememberDb = new RememberDayDao(context);

	}

	void scheduleAlarms() {
		List<RememberDayInfo> mem = mRememberDb.getNotifiDaTaList();

		for (int i = 0; i < mem.size(); i++) {
			int noteId = mem.get(i).getId();
			long alarmTime = mem.get(i).getReminderData();

			Intent intent = new Intent(ACTION_CALENDAR_ALARM);
			intent.putExtra(RememberDayDBHelper.ID, noteId);
			PendingIntent pi = PendingIntent.getBroadcast(mContext, noteId,
					intent, PendingIntent.FLAG_NO_CREATE);
			if (pi != null) {
				mAlarmManager.cancel(pi);
			}
			pi = PendingIntent.getBroadcast(mContext, noteId, intent, 0);
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pi);
		}

	}

	public void scheduleAlarmById(int noteId, int actionType, Long reminderData) {
		if (actionType == ACTION_INSERT) {

			scheduleAlarmWhenInsert(noteId, reminderData);

		} else if (actionType == ACTION_UPDATE) {

			scheduleAlarmWhenUpdate(noteId, reminderData);

		} else if (actionType == ACTION_DELETE) {
			scheduleAlarmWhenDelete(noteId);

		}
	}

	void scheduleAlarmWhenInsert(int noteId, Long reminderData) {
		if (reminderData < System.currentTimeMillis())
			return;

		Intent intent = new Intent(ACTION_CALENDAR_ALARM);
		intent.putExtra(RememberDayDBHelper.ID, noteId);
		PendingIntent pi = PendingIntent.getBroadcast(mContext, noteId, intent,
				0);
		mAlarmManager.set(AlarmManager.RTC_WAKEUP, reminderData, pi);
	}

	void scheduleAlarmWhenUpdate(int noteId, Long reminderData) {
		if (reminderData == 0L)
			return;

		Intent intent = new Intent(ACTION_CALENDAR_ALARM);
		intent.putExtra(RememberDayDBHelper.ID, noteId);
		PendingIntent pi = PendingIntent.getBroadcast(mContext, noteId, intent,
				PendingIntent.FLAG_NO_CREATE);
		if (pi != null) {
			mAlarmManager.cancel(pi);
		}

		pi = PendingIntent.getBroadcast(mContext, noteId, intent, 0);
		long currentMillis = System.currentTimeMillis();
		if (reminderData >= currentMillis) {
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, reminderData, pi);
		}

		scheduleCancelAlarm(noteId);
	}

	void scheduleAlarmWhenDelete(int noteId) {
		Intent intent = new Intent(ACTION_CALENDAR_ALARM);
		intent.putExtra(RememberDayDBHelper.ID, noteId);
		PendingIntent pi = PendingIntent.getBroadcast(mContext, noteId, intent,
				PendingIntent.FLAG_NO_CREATE);
		if (pi != null) {
			mAlarmManager.cancel(pi);
		}

		scheduleCancelAlarm(noteId);
	}

	void scheduleCancelAlarm(int noteId) {
		Intent intent = new Intent(ACTION_CALENDAR_ALARM_CANCEL);
		intent.putExtra(RememberDayDBHelper.ID, noteId);
		PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		long triggerAtTime = SystemClock.elapsedRealtime()
				+ DateUtils.SECOND_IN_MILLIS;
		mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime,
				pi);
	}

}