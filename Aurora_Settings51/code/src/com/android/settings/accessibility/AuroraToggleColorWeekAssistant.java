package com.android.settings.accessibility;

import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.ZenModeConfig;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import aurora.preference.AuroraPreference;

import com.android.settings.AuroraSettingsPreferenceFragment;
import com.android.settings.R;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;

//Aurora hujianwei 20160216 create for color weekness assistant start
public class AuroraToggleColorWeekAssistant extends AuroraSettingsPreferenceFragment {

	private final static String TAG = "AuroraToggleColorWeekAssistant";
    private static final String ENABLED = Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED;
    private static final String TYPE = Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER;
    private static final int DEFAULT_TYPE = AccessibilityManager.DALTONIZER_CORRECT_DEUTERANOMALY;

    private AuroraListPreference mAssistantType;
    private AuroraSwitchPreference  mAssistantKey;
    
    //辅助是否打开
    boolean mEnable = false;
    
	@Override
	public void onCreate(Bundle icicle) {
		
		super.onCreate(icicle);
		
		addPreferencesFromResource(R.xml.aurora_colorweekness_assistant);
		mAssistantType = (AuroraListPreference) findPreference("color_weekness_type");

		mAssistantType.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(AuroraPreference preference, Object object) {
		
				final int val = Integer.parseInt((String) object);
				
				if(  val ==  Settings.Secure.getInt(getContentResolver(), TYPE, DEFAULT_TYPE))  return false;
				Settings.Secure.putInt(getContentResolver(), TYPE, val);
				updateControls();
				return true;
			}
			
		});
		
		mAssistantKey = (AuroraSwitchPreference) findPreference("color_weekness_key");
		mEnable = Settings.Secure.getInt(getContentResolver(), ENABLED, 0) != 0;
		Log.d(TAG, "*** onCreate mEnable:" + mEnable);
		mAssistantKey.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
	    
			@Override
			public boolean onPreferenceChange(AuroraPreference preference, Object object) {
		
				final boolean enable = object instanceof Boolean ? ((Boolean) object) : false;
				
				Log.d(TAG, "*** onPreferenceChange enable:" + enable);
				if(  mEnable == enable ) return false;
				mEnable = enable;
				updateControls();
				return Settings.Secure.putInt(getContentResolver(), ENABLED, mEnable ? 1 : 0);
			}
		});
	}

	private void updateControls(){
		
		final String value = Integer.toString(Settings.Secure.getInt(getContentResolver(), TYPE, DEFAULT_TYPE));
		Log.d(TAG, "*** updateControls" );
		mAssistantType.setValue(value);
		mAssistantType.setSummary(mAssistantType.getEntry());
		if(  mAssistantType.findIndexOfValue(value) < 0){
        	mAssistantType.setSummary(getString(R.string.daltonizer_type_overridden, getString(R.string.simulate_color_space)));
		}
		mAssistantKey.setChecked(mEnable);
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateControls();
	}

	@Override
	public void onStop() {
		super.onStop();
	}


}
//Aurora hujianwei 20160216 create for color weekness assistant end