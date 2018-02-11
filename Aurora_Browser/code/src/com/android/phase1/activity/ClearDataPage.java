/**
 * Vulcan created this file in 2015年1月19日 上午10:51:19 .
 */
package com.android.phase1.activity;

import java.util.ArrayList;
import java.util.List;

import com.android.browser.BrowserSettings;
import com.android.browser.R;
import com.android.phase1.preference.AuroraPreferenceKeys;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import aurora.app.AuroraAlertDialog;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;

/**
 * Vulcan created ClearDataPage in 2015年1月19日 .
 * 
 */
public class ClearDataPage extends SimpleActivity {

	/**
	 * 
	 */
	public ClearDataPage() {
	}
	
	
	@SuppressWarnings("deprecation")
	protected void performClearData() {
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
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月13日 下午2:10:27 .
	 */
	protected void showDialogAskIfClearData(final Context context) {
		new AuroraAlertDialog.Builder(context)
					.setTitle(R.string.clear_data)
					.setMessage(R.string.ask_if_clear_or_not)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

						@SuppressWarnings("deprecation")
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							AuroraPreference prefDialog = (AuroraPreference) getPreferenceManager()
									.findPreference(AuroraPreferenceKeys.PREF_DIALOG_CLEAR_DATA);
							prefDialog.setEnabled(false);
							performClearData();
						}
						
					})
					.setNegativeButton(android.R.string.cancel, null)
					.show();
		return;
	}
	


	/* (non-Javadoc)
	 * @see aurora.preference.AuroraPreferenceActivity#onCreate(android.os.Bundle)
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		getAuroraActionBar().setTitle(R.string.clear_data);

		addPreferencesFromResource(R.xml.aurora_website_clear_data_pref_page);
		
		setupPreferences();
		
		if (!isPrefClearInputRecordChecked()
				&& !isPrefClearBrowseRecordChecked()
				&& !isPrefClearPasswordChecked()
				&& !isPrefClearBufferedPageChecked()
				&& !isPrefClearCookiesChecked()) {
			AuroraPreference prefDialog = (AuroraPreference) getPreferenceManager()
					.findPreference(AuroraPreferenceKeys.PREF_DIALOG_CLEAR_DATA);
			prefDialog.setEnabled(false);
		} else {
			AuroraPreference prefDialog = (AuroraPreference) getPreferenceManager()
					.findPreference(AuroraPreferenceKeys.PREF_DIALOG_CLEAR_DATA);
			prefDialog.setEnabled(true);
			// settings.resetDefaultPreferences();
		}
		
		
		AuroraPreference dialogClearData = getPreferenceScreen().findPreference(AuroraPreferenceKeys.PREF_DIALOG_CLEAR_DATA);
		dialogClearData.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(AuroraPreference arg0) {
				showDialogAskIfClearData(ClearDataPage.this);
				return false;
			}
			
		});
	}

	@Override
	protected List<String> getKeysPreference() {
		ArrayList<String> keys  = new ArrayList<String>();
		keys.add(AuroraPreferenceKeys.PREF_CLEAR_DATA_BROWSE_RECORD);
		keys.add(AuroraPreferenceKeys.PREF_CLEAR_DATA_INPUT_RECORD);
		keys.add(AuroraPreferenceKeys.PREF_CLEAR_DATA_PASSWORD);
		keys.add(AuroraPreferenceKeys.PREF_CLEAR_DATA_BUFFERED_PAGE);
		keys.add(AuroraPreferenceKeys.PREF_CLEAR_DATA_COOKIES);
		keys.add(AuroraPreferenceKeys.PREF_CLEAR_DATA_GEO_AUTHORIZATION);
		return keys;
	}



	@Override
	protected void restorePreference() {
	}
	
	

}
