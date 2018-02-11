package com.aurora.calendar.period;

import android.content.Intent;
import android.os.Bundle;

import com.android.calendar.R;

import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceScreen;

public class AuroraPeriodSettingActivity extends AuroraPreferenceActivity
		implements AuroraPreference.OnPreferenceClickListener{

	public final String PERIOD_TIME = "period_time";
	public final String PERIOD_CYCLE = "period_circle";

	private AuroraPreferenceScreen mViewTimePref;
	private AuroraPreferenceScreen mViewCyclePref;

	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		getAuroraActionBar().setTitle(R.string.aurora_period_detail);

		addPreferencesFromResource(R.xml.aurora_view_period_preferences);

		mViewTimePref = (AuroraPreferenceScreen)findPreference(PERIOD_TIME);
		mViewCyclePref = (AuroraPreferenceScreen)findPreference(PERIOD_CYCLE);
		mViewTimePref.setOnPreferenceClickListener(this);
		mViewCyclePref.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(AuroraPreference mPreference) {
		String key = mPreference.getKey();

		if (key != null) {
			if (key.equals(PERIOD_TIME)) {
				Intent intent = new Intent(this, PeriodTimeChooseActivity.class);
				startActivity(intent);
			} else if (key.equals(PERIOD_CYCLE)) {
				Intent intent = new Intent(this, PeriodCycleChooseActivity.class);
				startActivity(intent);
			}
		}
		return false;
	}

}
