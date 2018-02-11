package com.aurora.calendar;

import android.os.Bundle;
import aurora.preference.AuroraPreferenceActivity;

import com.android.calendar.R;

public class AuroraCalendarViewFilterActivity extends AuroraPreferenceActivity {

	public static final String FESTIVAL_KEY = "festival_display";
	public static final String HAPPY_KEY = "happy_display";
	public static final String LUNAR_KEY = "lunar_display";
	public static final String WORK_KEY = "work_display";

//	private AuroraSwitchPreference mHappySwitchPref;
//	private AuroraSwitchPreference mFestivalSwitchPref;
//	private AuroraSwitchPreference mWorkSwitchPref;
//	private AuroraSwitchPreference mLunarSwitchPref;

	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		getAuroraActionBar().setTitle(R.string.aurora_view_filter);
		addPreferencesFromResource(R.xml.aurora_view_setting_preferences);

//		final AuroraPreferenceScreen preferenceScreen = getPreferenceScreen();
//		
//		mHappySwitchPref = (AuroraSwitchPreference) preferenceScreen.findPreference(HAPPY_DAY_KEY);
//		mFestivalSwitchPref = (AuroraSwitchPreference) preferenceScreen.findPreference(FESTIVAL_KEY);
//		mWorkSwitchPref = (AuroraSwitchPreference) preferenceScreen.findPreference(WORK_HARD_KEY);
//		mLunarSwitchPref = (AuroraSwitchPreference) preferenceScreen.findPreference(LUNAR_CALENDAR_KEY);

	}

}
