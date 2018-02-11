/**
 * Vulcan created this file in 2015年1月23日 下午6:03:13 .
 */
package com.android.phase1.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import com.android.browser.BrowserSettings;
import com.android.phase1.preference.AuroraPreferenceKeys;

/**
 * Vulcan created AuroraBrowserSettings in 2015年1月23日 .
 * 
 */
public class AuroraBrowserSettings implements AuroraPreferenceKeys {

	/**
	 * 
	 */
	private AuroraBrowserSettings(Context context) {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		mPrefs.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月23日 下午6:10:23 .
	 * @param context
	 * @return
	 */
	public static AuroraBrowserSettings getInstance(Context context) {
		if(sInstance == null) {
			sInstance = new AuroraBrowserSettings(context);
		}
		return sInstance;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月23日 下午6:20:44 .
	 * @return
	 */
	public Boolean getIfClearInputRecord() {
		Boolean b = mPrefs.getBoolean(PREF_CLEAR_DATA_INPUT_RECORD, false);
		return b;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 上午10:15:49 .
	 */
	public void setIfClearInputRecord(Boolean b) {
		mPrefs.edit().putBoolean(PREF_CLEAR_DATA_INPUT_RECORD, b).apply();
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月23日 下午6:24:17 .
	 * @return
	 */
	public Boolean getIfClearBrowseRecord() {
		Boolean b = mPrefs.getBoolean(PREF_CLEAR_DATA_BROWSE_RECORD, false);
		return b;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 上午10:15:49 .
	 */
	public void setIfClearBrowseRecord(Boolean b) {
		//apply is fit to the main thread
		//commit is fit to the worker thread
		mPrefs.edit().putBoolean(PREF_CLEAR_DATA_BROWSE_RECORD, b).apply();
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月23日 下午6:24:25 .
	 * @return
	 */
	public Boolean getIfClearPassword() {
		Boolean b = mPrefs.getBoolean(PREF_CLEAR_DATA_PASSWORD, false);
		return b;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 上午10:24:07 .
	 * @param b
	 */
	public void setIfClearPassword(Boolean b) {
		mPrefs.edit().putBoolean(PREF_CLEAR_DATA_PASSWORD, b).apply();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月23日 下午6:25:10 .
	 * @return
	 */
	public Boolean getIfClearBufferedPage() {
		Boolean b = mPrefs.getBoolean(PREF_CLEAR_DATA_BUFFERED_PAGE, false);
		return b;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 上午10:24:26 .
	 * @param b
	 */
	public void setIfClearBufferedPage(Boolean b) {
		mPrefs.edit().putBoolean(PREF_CLEAR_DATA_BUFFERED_PAGE, b).apply();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月23日 下午6:25:36 .
	 * @return
	 */
	public Boolean getIfClearCookies() {
		Boolean b = mPrefs.getBoolean(PREF_CLEAR_DATA_COOKIES, false);
		return b;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 上午10:25:30 .
	 * @param b
	 */
	public void setIfClearCookies(Boolean b) {
		mPrefs.edit().putBoolean(PREF_CLEAR_DATA_COOKIES, b).apply();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月23日 下午6:25:36 .
	 * @return
	 */
	public Boolean getIfClearGeoAuthorization() {
		Boolean b = mPrefs.getBoolean(PREF_CLEAR_DATA_GEO_AUTHORIZATION, false);
		return b;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 上午10:25:30 .
	 * @param b
	 */
	public void setIfClearGeoAuthorization(Boolean b) {
		mPrefs.edit().putBoolean(PREF_CLEAR_DATA_GEO_AUTHORIZATION, b).apply();
	}
	
	
    /**
     * 
     * Vulcan created this method in 2015年1月29日 上午11:51:01 .
     */
    public void restorePreferences() {
    	mPrefs.edit().putString(AuroraPreferenceKeys.PREF_HOMEPAGE_PICKER, "default").apply();
    	BrowserSettings.getInstance().setHomePage(BrowserSettings.getFactoryResetHomeUrl(null));
    	mPrefs.edit().putBoolean(AuroraPreferenceKeys.PREF_SAVE_FORMDATA, true).apply();
    	mPrefs.edit().putBoolean(AuroraPreferenceKeys.PREF_REMEMBER_PASSWORDS, true).apply();
    	mPrefs.edit().putString(AuroraPreferenceKeys.PREF_SEARCH_ENGINE, "baidu").apply();
    	mPrefs.edit().putString(AuroraPreferenceKeys.PREF_TEXT_SIZE, "NORMAL").apply();
    	mPrefs.edit().putBoolean(AuroraPreferenceKeys.PREF_NO_PICTURE_MODE, false).apply();
    	mPrefs.edit().putString(AuroraPreferenceKeys.PREF_DATA_PRELOAD, "WIFI_ONLY").apply();
    	return;
    }
    
    private final OnSharedPreferenceChangeListener mSharedPreferenceChangeListener =  new OnSharedPreferenceChangeListener() {
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		}
	};
	
	private SharedPreferences mPrefs;
	private static AuroraBrowserSettings sInstance = null;

}
