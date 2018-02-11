package com.android.settings.theme;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.text.TextUtils;
import android.util.Log;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceScreen;

import com.android.settings.R;
import com.android.settings.AuroraSettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;

public class ThemeFragment extends AuroraSettingsPreferenceFragment implements Indexable{
	private static final String KEY_THEME="aurora_theme_settings";
	private static final String KEY_WALLPAPER="aurora_wallpaper_settings";
	private static final String KEY_TIME_WALLPAPER="aurora_time_wallpaper_settings";
	
	private static final String PKG_THEME = "com.aurora.thememanager";
	
	private static final String PKG_CHANGER = "com.aurora.change";
	
	private String mCurrentPkg = PKG_THEME;
	
	private static final String ACTION_THEME="com.aurora.thememanager.activity.DownloadedThemeActivity";
	private static final String ACTION_WALLPAPER="com.aurora.change.activities.DesktopWallpaperLocalActivity";
	private static final String ACTION_TIME_WALLPAPER="com.aurora.change.activities.WallpaperLocalActivity";
	
	
	@Override
	public void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.aurora_theme_settings);
	}

	
	@Override
	public boolean onPreferenceTreeClick(
			AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
		// TODO Auto-generated method stub
		String key  = preference.getKey();
		Intent intent = new Intent();
		if(!TextUtils.isEmpty(key)){
			if(KEY_THEME.equals(key)){
				ComponentName cn = new ComponentName(mCurrentPkg, ACTION_THEME);
				intent.setComponent(cn);
			}else if(KEY_WALLPAPER.equals(key)){
				ComponentName cn = new ComponentName(mCurrentPkg, ACTION_WALLPAPER);
				intent.setComponent(cn);
			}else if(KEY_TIME_WALLPAPER.equals(key)){
				ComponentName cn = new ComponentName(mCurrentPkg, ACTION_TIME_WALLPAPER);
				intent.setComponent(cn);
			}
				startActivity(intent);
				getActivity().overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter,com.aurora.R.anim.aurora_activity_open_exit);
		}
		return true;
	}
	

}
