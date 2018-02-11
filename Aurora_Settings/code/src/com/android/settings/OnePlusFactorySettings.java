package com.android.settings;

import android.os.Bundle;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceScreen;
import android.content.Intent;
import android.content.ComponentName;
import android.util.Log;

public class OnePlusFactorySettings extends SettingsPreferenceFragment
		implements AuroraPreference.OnPreferenceClickListener {

	private static final String KEY_SMART_APPERCEIVE_SETTINGS = "smart_apperceive_control";
	private static final String KEY_BREATHING_LED = "breathing_led";

	private AuroraPreferenceScreen mSmartScreenPref;
	private AuroraPreferenceScreen mBreathingLed;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.one_plus_factory_settings);
		mSmartScreenPref = (AuroraPreferenceScreen) findPreference(KEY_SMART_APPERCEIVE_SETTINGS);
		mSmartScreenPref.setOnPreferenceClickListener(this);

		mBreathingLed = (AuroraPreferenceScreen) findPreference(KEY_BREATHING_LED);
		mBreathingLed.setFragment("com.android.settings.OnePlusBreathLed");
	}

	@Override
	public boolean onPreferenceClick(AuroraPreference preference) {
		if (preference.getKey().equals(KEY_SMART_APPERCEIVE_SETTINGS)) {
			Intent intent = new Intent();
			ComponentName name = new ComponentName("com.oppo.gestureguide",
					"com.oppo.gestureguide.OppoGestureControlSettings");
			intent.setComponent(name);
			try {
				startActivity(intent);
			} catch (Exception e) {
				Log.e("gary", "Unable to start activity " + intent.toString());
			}
		} 

		return true;
	}
}