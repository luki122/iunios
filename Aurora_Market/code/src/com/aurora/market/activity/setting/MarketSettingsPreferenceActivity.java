package com.aurora.market.activity.setting;

import com.aurora.market.R;
import com.aurora.market.activity.BasePreferenceActivity;
import com.aurora.market.widget.AuroraMarketPreference;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;
import aurora.widget.AuroraActionBar;

public class MarketSettingsPreferenceActivity extends BasePreferenceActivity {

	private final static String UPDATE_SETTINGS_KEY = "update_settings_key";
	private final static String WIFI_DOWNLOAD_KEY = "wifi_download_key";
	private final static String NONE_DOWNLOAD_KEY = "none_download_pic_key";
	private final static String HOLD_APP_KEY = "hold_app_key";
	
	private AuroraActionBar mActionBar;
	private static final int AURORA_NEW_MARKET = 0;
	
	private AuroraMarketPreference mUpdateSettingsPref;
	private AuroraSwitchPreference mWifiDownloadPref;
	private AuroraSwitchPreference mNoneDownloadPicKey;
	private AuroraSwitchPreference mHoldAppKey;
    
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		//this.setTheme(com.aurora.R.style.Theme_Aurora_Dark_Transparent);
		super.onCreate(arg0);
		addPreferencesFromResource(R.xml.settings_prefs);
		
		initActionBar();
		initViews();
	}

	private void initViews(){
		mUpdateSettingsPref = (AuroraMarketPreference) findPreference(UPDATE_SETTINGS_KEY);
		mWifiDownloadPref = (AuroraSwitchPreference) findPreference(WIFI_DOWNLOAD_KEY);
		mNoneDownloadPicKey = (AuroraSwitchPreference) findPreference(NONE_DOWNLOAD_KEY);
		mHoldAppKey = (AuroraSwitchPreference) findPreference(HOLD_APP_KEY);
	}

	private void initActionBar() {
		mActionBar = getAuroraActionBar();
		mActionBar.setTitle(R.string.settings_pref);
        mActionBar.setBackground(getResources().getDrawable(
                R.drawable.aurora_action_bar_top_bg_green));
		// addAuroraActionBarItem(AuroraActionBarItem.Type.Add,
		// AURORA_NEW_MARKET);
	}

	@Override
	@Deprecated
	public boolean onPreferenceTreeClick(
			AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
		// TODO Auto-generated method stub
		
		if(UPDATE_SETTINGS_KEY.equals(preference.getKey())){
			Intent lInt = new Intent(this, UpdateSettingsPreferenceActivity.class);
			startActivity(lInt);
		}else if(WIFI_DOWNLOAD_KEY.equals(preference.getKey())){
			
		}else if(NONE_DOWNLOAD_KEY.equals(preference.getKey())){
			
		}else if(HOLD_APP_KEY.equals(preference.getKey())){
			
		}
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
 
	
}
