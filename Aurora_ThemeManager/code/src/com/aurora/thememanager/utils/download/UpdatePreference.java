package com.aurora.thememanager.utils.download;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UpdatePreference {
	public final static String WIFI_AUTO_UPGRADE_KEY = "wifi_auto_upgrade_key";
	public final static String AUTO_UPDATE_TIP_KEY = "auto_update_tip_key";
	public final static String UPDATE_IGNORED_KEY = "update_ignored_key";
	
	public static synchronized boolean getPreferenceValue(final Context pContext,
			String key) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(pContext);
		return pref.getBoolean(key, false);
	}
	
	
	public static synchronized void setPreferenceValue(final Context pContext,
			String key,boolean value) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(pContext);
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	

}
