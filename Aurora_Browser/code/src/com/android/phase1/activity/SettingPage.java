package com.android.phase1.activity;


import java.util.ArrayList;
import java.util.List;

import com.android.browser.BrowserSettings;
import com.android.browser.R;
import com.android.phase1.model.AuroraBrowserSettings;
import com.android.phase1.preference.AuroraPreferenceKeys;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import aurora.app.AuroraAlertDialog;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import aurora.preference.AuroraSwitchPreference;

public class SettingPage extends SimpleActivity {

	public SettingPage() {
		super();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月16日 下午4:29:31 .
	 * @param context
	 */
	public static void start(Context context, String curUrl) {
		Intent intent = new Intent(context, SettingPage.class);
		context.startActivity(intent);
		
		mCurrentPage = curUrl;
		return;
	}

	/* (non-Javadoc)
	 * @see aurora.preference.AuroraPreferenceActivity#onCreate(android.os.Bundle)
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		//mContext = getApplicationContext();
        //mResolver = getContentResolver();
		//mCalendarController = CalendarController.getInstance(this);

		//mSharedPreferences = GeneralPreferences.getSharedPreferences(mContext);
		getAuroraActionBar().setTitle(R.string.setting);

		addPreferencesFromResource(R.xml.aurora_website_setting_pref_page);
		
		setupPreferences();
		
		AuroraPreference e = getPreferenceScreen().findPreference(AuroraPreferenceKeys.PREF_RESET_DEFAULT_PREFERENCES);
        e.setOnPreferenceClickListener(mPreferenceClickListener);

/*        AuroraPreferenceScreen websiteSettings = (AuroraPreferenceScreen) findPreference(
                AuroraPreferenceKeys.PREF_WEBSITE_SETTINGS);
        websiteSettings.setFragment(WebsiteSettingsFragment.class.getName());

        Preference e = findPreference(PreferenceKeys.PREF_DEFAULT_ZOOM);
        e.setOnPreferenceChangeListener(this);
        e.setSummary(getVisualDefaultZoomName(
                getPreferenceScreen().getSharedPreferences()
                .getString(PreferenceKeys.PREF_DEFAULT_ZOOM, null)) );

        e = findPreference(PreferenceKeys.PREF_DEFAULT_TEXT_ENCODING);
        e.setOnPreferenceChangeListener(this);

        e = findPreference(PreferenceKeys.PREF_RESET_DEFAULT_PREFERENCES);
        e.setOnPreferenceChangeListener(this);

        e = findPreference(PreferenceKeys.PREF_SEARCH_ENGINE);
        e.setOnPreferenceChangeListener(this);
        updateListPreferenceSummary((ListPreference) e);

        e = findPreference(PreferenceKeys.PREF_PLUGIN_STATE);
        e.setOnPreferenceChangeListener(this);
        updateListPreferenceSummary((ListPreference) e);*/
	}

	@Override
	protected List<String> getKeysPreference() {
		ArrayList<String> keys  = new ArrayList<String>();
		keys.add(AuroraPreferenceKeys.PREF_SEARCH_ENGINE);
		keys.add(AuroraPreferenceKeys.PREF_TEXT_SIZE);
		keys.add(AuroraPreferenceKeys.PREF_NO_PICTURE_MODE);
		keys.add(AuroraPreferenceKeys.PREF_DATA_PRELOAD);
		keys.add(AuroraPreferenceKeys.PREF_RESET_DEFAULT_PREFERENCES);
		//PREF_LOAD_IMAGES
		return keys;
	}

	@Override
	protected void restorePreference() {
		
		List<String> keys = getKeysPreference();
		for(String key: keys) {
			@SuppressWarnings("deprecation")
			AuroraPreference e = getPreferenceScreen().findPreference(key);
			if(AuroraPreferenceKeys.PREF_SEARCH_ENGINE.equals(key)) {
				((AuroraListPreference)e).setValue("baidu");
			}
			else if(AuroraPreferenceKeys.PREF_TEXT_SIZE.equals(key)) {
				((AuroraListPreference)e).setValue("NORMAL");
			}
			else if(AuroraPreferenceKeys.PREF_NO_PICTURE_MODE.equals(key)) {
				((AuroraSwitchPreference)e).setChecked(false);
			}
			else if(AuroraPreferenceKeys.PREF_DATA_PRELOAD.equals(key)) {
				((AuroraListPreference)e).setValue("WIFI_ONLY");
			}
		}
		
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月13日 下午2:10:27 .
	 */
	protected void showDialogAskIfRestore(final Context context) {
		new AuroraAlertDialog.Builder(context)
					.setTitle(R.string.ask_if_restore_factory)
					//.setMessage(R.string.ask_if_restore_factory)
					.setPositiveButton(R.string.restore, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							SimpleActivity.restorePreferences();
							AuroraBrowserSettings.getInstance(context.getApplicationContext()).restorePreferences();
							BrowserSettings.executeSetup();
						}
						
					})
					.setNegativeButton(android.R.string.cancel, null)
					.show();
		return;
	}


	//private AuroraAlertDialog pdAskIfRestore = null;
	private OnPreferenceClickListener mPreferenceClickListener =  new OnPreferenceClickListener() {
		
		@Override
		public boolean onPreferenceClick(AuroraPreference arg0) {
			showDialogAskIfRestore(SettingPage.this);
			return true;
		}
	};
	//private Context mContext;

}
