package com.aurora.calendar;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.util.Log;

import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.GeneralPreferences;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.aurora.calendar.period.AuroraPeriodSettingActivity;
import com.aurora.calendar.period.PeriodInfoAdapter;
import com.aurora.calendar.util.CalendarFeatureConstants;
import com.aurora.calendar.util.ToastUtil;
import com.gionee.calendar.statistics.Statistics;

import aurora.app.AuroraAlertDialog;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraRingtonePreference;
import aurora.preference.AuroraSwitchPreference;

public class AuroraCalendarSettingActivity extends AuroraPreferenceActivity
		implements AuroraPreference.OnPreferenceClickListener, OnPreferenceChangeListener {

	private static final String VIEW_FILTER = "view_filter";
	private static final String VIEW_PERIOD = "view_period";
	private static final String PERIOD_CATEGORY = "period_category";
    private static final String NOTE_REMINDER_SWITCH = "note_reminder_switch";
	private static final String BIRTHDAY_REMINDER_SWITCH = "birthday_reminder_switch";
	public static final String EVENTS_REMINDER = "preferences_default_reminder";
	private static final String TODO_REMINDER = "todo_reminder";
	private static final String ACCOUNT_MANAGEMENT = "account_management";
	private static final String WEEK_START_DAY = "preferences_week_start_day";
	private static final String RING_PICKER = "preferences_alerts_ringtone";

	private static final String CLEAR_PERIOD_CATEGORY = "clear_period_category";
	private static final String CLEAR_PERIOD = "clear_period";

	private static final int REQUEST_CODE_PICK_RINGTONE = 13;

	private Context mContext;
    private ContentResolver mResolver;

	private AuroraPreferenceScreen mViewFilterPref;
	private AuroraPreferenceScreen mViewPeriodPref;
	private AuroraPreference mClearPeriodPref;
	private AuroraPreferenceScreen mAccountManagementPref;
    private AuroraSwitchPreference mNoteReminderSwitchPreference;
	private AuroraSwitchPreference mBirthdayReminderSwitchPref;
	private AuroraListPreference mEventsReminderPref;
	private AuroraListPreference mWeekStartDayPref;
	// private AuroraPreferenceScreen mTodo_ReminderPref;
	private AuroraRingtonePreference mRingPickerPref;

	SharedPreferences mSharedPreferences;
	CalendarController mCalendarController;

	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		mContext = this/*.getApplicationContext()*/;
        mResolver = getContentResolver();
		mCalendarController = CalendarController.getInstance(this);

		mSharedPreferences = GeneralPreferences.getSharedPreferences(mContext);
		getAuroraActionBar().setTitle(R.string.preferences_title);

		addPreferencesFromResource(R.xml.aurora_setting_view_preferences);

		mViewFilterPref = (AuroraPreferenceScreen)findPreference(VIEW_FILTER);
		mViewPeriodPref = (AuroraPreferenceScreen)findPreference(VIEW_PERIOD);
		mAccountManagementPref = (AuroraPreferenceScreen)findPreference(ACCOUNT_MANAGEMENT);
		mWeekStartDayPref = (AuroraListPreference)findPreference(WEEK_START_DAY);
        mNoteReminderSwitchPreference = (AuroraSwitchPreference) findPreference(NOTE_REMINDER_SWITCH);
		mEventsReminderPref = (AuroraListPreference)findPreference(EVENTS_REMINDER);
		mBirthdayReminderSwitchPref = (AuroraSwitchPreference)findPreference(BIRTHDAY_REMINDER_SWITCH);
		// mTodo_ReminderPref = (AuroraPreferenceScreen)
		// findPreference(TODO_REMINDER);
		mRingPickerPref = (AuroraRingtonePreference)findPreference(RING_PICKER);
		mViewFilterPref.setOnPreferenceClickListener(this);
		mViewPeriodPref.setOnPreferenceClickListener(this);
		mAccountManagementPref.setOnPreferenceClickListener(this);
		mRingPickerPref.setOnPreferenceClickListener(this);
		updateRingtone();
		mEventsReminderPref.setOnPreferenceChangeListener(this);
		mWeekStartDayPref.setOnPreferenceChangeListener(this);
		mWeekStartDayPref.auroraSetArrowText(mWeekStartDayPref.getEntry(), true);
		mEventsReminderPref.auroraSetArrowText(mEventsReminderPref.getEntry(), true);
		// mTodo_ReminderPref.setOnPreferenceClickListener(this);

		mClearPeriodPref = findPreference(CLEAR_PERIOD);
		mClearPeriodPref.setOnPreferenceClickListener(this);

        getPreferenceScreen().removePreference(mAccountManagementPref);

        if (!Utils.isChineseEnvironment()) {
            getPreferenceScreen().removePreference(mViewFilterPref);
        }
        if (!Utils.isWomenEnvironment()) {
			getPreferenceScreen().removePreference(findPreference(PERIOD_CATEGORY));
			getPreferenceScreen().removePreference(findPreference(CLEAR_PERIOD_CATEGORY));
        }
	}

	void updateRingtone() {
		String uriStr;
		uriStr = mSharedPreferences.getString(GeneralPreferences.KEY_ALERTS_RINGTONE, null);
		String ringtoneName = CalendarFeatureConstants.getRingtoneTitle(mContext, uriStr);
		mRingPickerPref.auroraSetArrowText(ringtoneName, true);

        Cursor cursor = mResolver.query(
                Calendars.CONTENT_URI,
                new String[] { Calendars._ID, Calendars.VISIBLE },
                Calendars.ACCOUNT_NAME + "=?",
                new String[] { Utils.NOTE_REMINDER_ACCOUNT_NAME },
                null);
        if (cursor != null && cursor.moveToFirst()) {
            int visibleColumn = cursor.getColumnIndexOrThrow(Calendars.VISIBLE);
            boolean selected = cursor.getInt(visibleColumn) == 1;
            mNoteReminderSwitchPreference.setChecked(selected);
            mNoteReminderSwitchPreference.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(mNoteReminderSwitchPreference);
        }

        if (cursor != null) {
            cursor.close();
        }
	}

	@Override
	public boolean onPreferenceClick(AuroraPreference mPreference) {
		String key = mPreference.getKey();

		if (key != null) {
			if (key.equals(VIEW_FILTER)) {
				Intent intent = new Intent(this, AuroraCalendarViewFilterActivity.class);
				startActivity(intent);
			} else if (key.equals(VIEW_PERIOD)) {
				Log.i("JOY", "VIEW_PERIOD");
				Intent intent = new Intent(this, AuroraPeriodSettingActivity.class);
				startActivity(intent);
			} else if (key.equals(RING_PICKER)) {
				Statistics.onEvent(this, Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_SETTINGS_CHOOSE_RINGTONE);
				return true;
			} else if (key.equals(ACCOUNT_MANAGEMENT)) {
				mCalendarController.sendEvent(this, EventType.LAUNCH_SELECT_VISIBLE_CALENDARS, null, null, 0, 0);
			} else if (key.equals(CLEAR_PERIOD)) {
				new AuroraAlertDialog.Builder(mContext)
						.setMessage(R.string.aurora_period_clear_message)
						.setNegativeButton(android.R.string.cancel, null)
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
                            public void onClick(DialogInterface dialog, int which) {
								PeriodInfoAdapter periodAdapter = new PeriodInfoAdapter(mContext);
								periodAdapter.open();
								periodAdapter.deleteAll();
								periodAdapter.close();

								Utils.setPeriodSharePreference(mContext, Utils.PERIOD_TIME, 5);
								Utils.setPeriodSharePreference(mContext, Utils.PERIOD_CYCLE, 28);
								int flag = Utils.getPeriodSharePreference(mContext, Utils.PERIOD_CLEAR_FLAG, 0);
								Utils.setPeriodSharePreference(mContext, Utils.PERIOD_CLEAR_FLAG, flag == 0 ? 1 : 0);

								ToastUtil.shortToast(R.string.aurora_period_clear_ok);
							}
						}).show();
			}
		}
		return false;
	}

	@Override
	public boolean onPreferenceChange(AuroraPreference mPreference, Object newValue) {
		String key = mPreference.getKey();
		if (key.equals(EVENTS_REMINDER)) {
			mEventsReminderPref.setValue((String) newValue);
			mEventsReminderPref.auroraSetArrowText(mEventsReminderPref.getEntry(), true);

			setReminderValue(mContext, (String) newValue);
		} else if (key.equals(WEEK_START_DAY)) {
			mWeekStartDayPref.setValue((String) newValue);
			mWeekStartDayPref.auroraSetArrowText(mWeekStartDayPref.getEntry(), true);
		} else if (NOTE_REMINDER_SWITCH.equals(key)) {
            Boolean selected = (Boolean) newValue;
            ContentValues values = new ContentValues();
            values.put(Calendars.VISIBLE, selected ? 1 : 0);

            String selection = Calendars.ACCOUNT_NAME + "='" + Utils.NOTE_REMINDER_ACCOUNT_NAME + "'";
            mResolver.update(Calendars.CONTENT_URI, values, selection, null);
            return true;
		}
		return false;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateRingtone();
	}

	public static void setReminderValue(Context context, String value) {
		SharedPreferences sp = context.getSharedPreferences("calendar_reminder_value",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		sp.edit().putString(EVENTS_REMINDER, value).commit();
	}

}
