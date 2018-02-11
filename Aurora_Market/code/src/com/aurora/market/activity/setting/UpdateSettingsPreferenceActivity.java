package com.aurora.market.activity.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import aurora.app.AuroraAlertDialog;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;
import aurora.widget.AuroraActionBar;

import com.aurora.market.R;
import com.aurora.market.activity.BasePreferenceActivity;
import com.aurora.market.activity.module.MarketUpdateIgnoredActivity;
import com.aurora.market.service.AutoUpdateService;

public class UpdateSettingsPreferenceActivity extends BasePreferenceActivity
		implements OnPreferenceChangeListener {

	public final static String WIFI_AUTO_UPGRADE_KEY = "wifi_auto_upgrade_key";
	public final static String SOFTWARE_AUTO_UPDATE_TIP_KEY = "software_auto_update_tip_key";
	public final static String APPS_UPDATE_IGNORED_KEY = "apps_update_ignored_key";

	private AuroraSwitchPreference mWifiAutoUpgradePref;
	private AuroraSwitchPreference mSoftwareAutoUpdateTipPref;
	private AuroraPreference mAppsUpdateIgnoredPref;

	private AuroraActionBar mActionBar;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		addPreferencesFromResource(R.xml.update_settings_prefs);

		initActionBar();
		initViews();
	}

	private void initActionBar() {
		mActionBar = getAuroraActionBar();
		mActionBar.setTitle(R.string.app_update_settings);
		mActionBar.setBackground(getResources().getDrawable(
				R.drawable.aurora_action_bar_top_bg_green));
	}

	private void initViews() {

		mWifiAutoUpgradePref = (AuroraSwitchPreference) findPreference(WIFI_AUTO_UPGRADE_KEY);
		mSoftwareAutoUpdateTipPref = (AuroraSwitchPreference) findPreference(SOFTWARE_AUTO_UPDATE_TIP_KEY);
		mAppsUpdateIgnoredPref = (AuroraPreference) findPreference(APPS_UPDATE_IGNORED_KEY);

		mWifiAutoUpgradePref.setOnPreferenceChangeListener(this);
	}

	@Override
	@Deprecated
	public boolean onPreferenceTreeClick(
			AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
		// TODO Auto-generated method stub
		if (WIFI_AUTO_UPGRADE_KEY.equals(preference.getKey())) {

		} else if (SOFTWARE_AUTO_UPDATE_TIP_KEY.equals(preference.getKey())) {

		} else if (APPS_UPDATE_IGNORED_KEY.equals(preference.getKey())) {
			Intent lInt = new Intent(this, MarketUpdateIgnoredActivity.class);
			startActivity(lInt);
		}

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	public static boolean getPreferenceValue(final Context pContext,
			String pPrefKey) {
		SharedPreferences mSharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(pContext);
		return mSharedPrefs.getBoolean(pPrefKey, false);
	}

	@Override
	public boolean onPreferenceChange(AuroraPreference pref, Object value) {
		boolean changed = (Boolean) value;
		if (pref.getKey().equals(WIFI_AUTO_UPGRADE_KEY)) {
			if (!mWifiAutoUpgradePref.isChecked() && changed) {
				mWifiAutoUpgradePref.setChecked(true);
				AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
				builder.setMessage(getString(R.string.wifi_auto_upgrade_open_tip));
				builder.setPositiveButton(getString(R.string.dialog_confirm),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								mWifiAutoUpgradePref.setChecked(true);
								AutoUpdateService.startAutoUpdate(UpdateSettingsPreferenceActivity.this, 0);
							}
						});
				builder.setNegativeButton(getString(R.string.dialog_cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								mWifiAutoUpgradePref.setChecked(false);
							}
						});

				builder.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface arg0) {
						mWifiAutoUpgradePref.setChecked(false);
					}
				});
				builder.show();
			} else {
				mWifiAutoUpgradePref.setChecked(false);
				AutoUpdateService.stopAutoUpdate(UpdateSettingsPreferenceActivity.this);
			}
		}
		return true;
	}

}
