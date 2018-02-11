package com.android.settings;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraSwitchPreference;

public class OnePlusBreathLed extends SettingsPreferenceFragment implements
		AuroraPreference.OnPreferenceChangeListener {
	private static final String TAG = "OnePlusBreathLed";
	
	private static final String KEY_NOTIFICATION = "notification";
	private static final String KEY_LOW_POWER = "low_power";
	private static final String KEY_CHARGE = "charge";
	
	private AuroraSwitchPreference mNotification;
	private AuroraSwitchPreference mLowPower;
	private AuroraSwitchPreference mCharge;

	@Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        addPreferencesFromResource(R.xml.one_plus_breath_led);
	        
	        mNotification = (AuroraSwitchPreference)findPreference(KEY_NOTIFICATION);
	        mNotification.setOnPreferenceChangeListener(this);
	        int notification = Settings.System.getInt(getActivity().getContentResolver(), "notification_light_pulse",0);
	        Log.v(TAG, "---notification--=="+notification);
	        mNotification.setChecked(notification ==1 ? true:false);
	        
	        mLowPower = (AuroraSwitchPreference)findPreference(KEY_LOW_POWER);
	        mLowPower.setOnPreferenceChangeListener(this);
	        int lowPower = Settings.System.getInt(getActivity().getContentResolver(), "oppo_breath_led_low_power",0);
	        Log.v(TAG, "---lowPower--=="+lowPower);
	        mLowPower.setChecked(lowPower ==1 ? true:false);
	        
	        mCharge = (AuroraSwitchPreference)findPreference(KEY_CHARGE);
	        mCharge.setOnPreferenceChangeListener(this);
	        int charge = Settings.System.getInt(getActivity().getContentResolver(), "oppo_breath_led_charge",0);
	        Log.v(TAG, "---mCharge--=="+charge);
	        mCharge.setChecked(charge ==1 ? true:false);
	    }

	@Override
	public boolean onPreferenceChange(AuroraPreference preference, Object objValue) {
		// TODO Auto-generated method stub
		final String key = preference.getKey();
		boolean state = ((Boolean) objValue).booleanValue();
		Log.v(TAG, "---onPreferenceChange--state-===="+state);
		
		if(key.equals(KEY_NOTIFICATION)){
			Settings.System.putInt(getActivity().getContentResolver(), "notification_light_pulse", state ? 1:0);
		}else if(key.equals(KEY_LOW_POWER)){
			Settings.System.putInt(getActivity().getContentResolver(), "oppo_breath_led_low_power", state ? 1:0);
		}else if(key.equals(KEY_CHARGE)){
			Settings.System.putInt(getActivity().getContentResolver(), "oppo_breath_led_charge", state ? 1:0);
		}
		
		return true;
	}

}