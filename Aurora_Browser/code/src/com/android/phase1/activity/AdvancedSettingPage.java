/**
 * Vulcan created this file in 2015年1月19日 下午2:12:46 .
 */
package com.android.phase1.activity;

import java.util.ArrayList;
import java.util.List;
import com.android.browser.R;
import com.android.phase1.preference.AuroraPreferenceKeys;

import android.os.Bundle;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraSwitchPreference;

/**
 * Vulcan created AdvancedSettingPage in 2015年1月19日 .
 * 
 */
public class AdvancedSettingPage extends SimpleActivity {

	/**
	 * 
	 */
	public AdvancedSettingPage() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see aurora.preference.AuroraPreferenceActivity#onCreate(android.os.Bundle)
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		getAuroraActionBar().setTitle(R.string.advanced_setting);

		addPreferencesFromResource(R.xml.aurora_website_advanced_setting_pref_page);
		
		setupPreferences();
		
	}

	@Override
	protected List<String> getKeysPreference() {
		//PREF_HOMEPAGE_PICKER
		ArrayList<String> keys  = new ArrayList<String>();
		keys.add(AuroraPreferenceKeys.PREF_HOMEPAGE_PICKER);
		keys.add(AuroraPreferenceKeys.PREF_SAVE_FORMDATA);
		keys.add(AuroraPreferenceKeys.PREF_REMEMBER_PASSWORDS);
		return keys;
	}

	@Override
	protected void restorePreference() {
		List<String> keys = getKeysPreference();
		for(String key: keys) {
			@SuppressWarnings("deprecation")
			AuroraPreference e = getPreferenceScreen().findPreference(key);
			if(AuroraPreferenceKeys.PREF_HOMEPAGE_PICKER.equals(key)) {
				((AuroraListPreference)e).setValue("current");
			}
			else if(AuroraPreferenceKeys.PREF_SAVE_FORMDATA.equals(key)) {
				((AuroraSwitchPreference)e).setChecked(true);
			}
			else if(AuroraPreferenceKeys.PREF_REMEMBER_PASSWORDS.equals(key)) {
				((AuroraSwitchPreference)e).setChecked(true);
			}
		}
		
	}

}
