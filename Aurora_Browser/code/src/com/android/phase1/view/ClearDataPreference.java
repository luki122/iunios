/**
 * Vulcan created this file in 2015年1月19日 下午3:24:01 .
 */
package com.android.phase1.view;

import com.android.browser.BrowserSettings;
import com.android.phase1.preference.AuroraPreferenceKeys;

import android.content.Context;
import android.util.AttributeSet;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraDialogPreference;

/**
 * Vulcan created ButtonPreference in 2015年1月19日 .
 * 
 */
public class ClearDataPreference extends AuroraDialogPreference {

	public ClearDataPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		//setLayoutResource(R.layout.aurora_website_button_pref);
	}

	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 下午2:02:12 .
	 * @return
	 */
	public boolean isPrefClearInputRecordChecked() {
		AuroraCheckBoxPreference prefClearInputRecord = (AuroraCheckBoxPreference) getPreferenceManager()
				.findPreference(
						AuroraPreferenceKeys.PREF_CLEAR_DATA_INPUT_RECORD);
		return prefClearInputRecord.isChecked();
	}
	
	
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 下午2:03:25 .
	 * @return
	 */
	public boolean isPrefClearBrowseRecordChecked() {
		AuroraCheckBoxPreference prefClearInputRecord = (AuroraCheckBoxPreference) getPreferenceManager()
				.findPreference(
						AuroraPreferenceKeys.PREF_CLEAR_DATA_BROWSE_RECORD);
		return prefClearInputRecord.isChecked();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 下午2:03:40 .
	 * @return
	 */
	public boolean isPrefClearPasswordChecked() {
		AuroraCheckBoxPreference prefClearInputRecord = (AuroraCheckBoxPreference) getPreferenceManager()
				.findPreference(
						AuroraPreferenceKeys.PREF_CLEAR_DATA_PASSWORD);
		return prefClearInputRecord.isChecked();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 下午2:04:09 .
	 * @return
	 */
	public boolean isPrefClearBufferedPageChecked() {
		AuroraCheckBoxPreference prefClearInputRecord = (AuroraCheckBoxPreference) getPreferenceManager()
				.findPreference(
						AuroraPreferenceKeys.PREF_CLEAR_DATA_BUFFERED_PAGE);
		return prefClearInputRecord.isChecked();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 下午2:04:14 .
	 * @return
	 */
	public boolean isPrefClearCookiesChecked() {
		AuroraCheckBoxPreference prefClearInputRecord = (AuroraCheckBoxPreference) getPreferenceManager()
				.findPreference(
						AuroraPreferenceKeys.PREF_CLEAR_DATA_COOKIES);
		return prefClearInputRecord.isChecked();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 下午2:04:14 .
	 * @return
	 */
	public boolean isPrefClearGeoAuthorizationChecked() {
		AuroraCheckBoxPreference prefClearInputRecord = (AuroraCheckBoxPreference) getPreferenceManager()
				.findPreference(
						AuroraPreferenceKeys.PREF_CLEAR_DATA_GEO_AUTHORIZATION);
		return prefClearInputRecord.isChecked();
	}

	/* (non-Javadoc)
	 * @see aurora.preference.AuroraDialogPreference#onDialogClosed(boolean)
	 */
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		
        if (positiveResult) {
            setEnabled(false);

            //AuroraBrowserSettings auroraSetttings = AuroraBrowserSettings.getInstance(getContext());
        	
			AuroraCheckBoxPreference prefClearInputRecord = (AuroraCheckBoxPreference) getPreferenceManager()
					.findPreference(
							AuroraPreferenceKeys.PREF_CLEAR_DATA_INPUT_RECORD);
			AuroraCheckBoxPreference prefClearBrowseRecord = (AuroraCheckBoxPreference) getPreferenceManager()
					.findPreference(
							AuroraPreferenceKeys.PREF_CLEAR_DATA_BROWSE_RECORD);
			AuroraCheckBoxPreference prefClearPassword = (AuroraCheckBoxPreference) getPreferenceManager()
					.findPreference(
							AuroraPreferenceKeys.PREF_CLEAR_DATA_PASSWORD);
			AuroraCheckBoxPreference prefClearBufferedPage = (AuroraCheckBoxPreference) getPreferenceManager()
					.findPreference(
							AuroraPreferenceKeys.PREF_CLEAR_DATA_BUFFERED_PAGE);
			AuroraCheckBoxPreference prefClearCookies = (AuroraCheckBoxPreference) getPreferenceManager()
					.findPreference(
							AuroraPreferenceKeys.PREF_CLEAR_DATA_COOKIES);
			AuroraCheckBoxPreference prefClearGeoAuthorization = (AuroraCheckBoxPreference) getPreferenceManager()
					.findPreference(
							AuroraPreferenceKeys.PREF_CLEAR_DATA_GEO_AUTHORIZATION);
        	
            BrowserSettings settings = BrowserSettings.getInstance();
            
            if (prefClearInputRecord.isChecked()) {
            	settings.clearFormData();
            	prefClearInputRecord.setChecked(false);
            }
            if (prefClearBrowseRecord.isChecked()) {
            	settings.clearHistory();
            	prefClearBrowseRecord.setChecked(false);
            }
            if (prefClearPassword.isChecked()) {
            	settings.clearPasswords();
            	prefClearPassword.setChecked(false);
            }
            if (prefClearBufferedPage.isChecked()) {
                settings.clearCache();
                settings.clearDatabases();
                prefClearBufferedPage.setChecked(false);
            }
            if (prefClearCookies.isChecked()) {
            	settings.clearCookies();
            	prefClearCookies.setChecked(false);
            }
            if (prefClearGeoAuthorization.isChecked()) {
            	settings.clearLocationAccess();
            	prefClearGeoAuthorization.setChecked(false);
            }
        }
	}

}
