package com.aurora.thememanager.preference;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
	
	public static final String KEY_CURRENT_THEME = "current_theme";
	private static final String THEME_PREF_NAME = "themeConfig";
	
	public static final String KEY_CURRENT_TIME_WALLPAPER = "current_time_wall_paper";
	
	private static PreferenceManager mInstance;
	
	 SharedPreferences mSharePref;
	
	 SharedPreferences.Editor mEditor;
	
	private PreferenceManager(){
		
	}
	
	
	public static PreferenceManager getInstance(Context context){
		synchronized (PreferenceManager.class) {
			if(mInstance == null){
				mInstance = new PreferenceManager();
			}
			if(mInstance.mSharePref == null){
				mInstance.mSharePref = context.getSharedPreferences(THEME_PREF_NAME, Context.MODE_PRIVATE);
				mInstance.mEditor = mInstance.mSharePref.edit();
			}
			return mInstance;
		}
	}

	public void saveLong(String key,long value){
		synchronized (this) {
			if(mEditor != null){
				mEditor.putLong(key, value);
				mEditor.commit();
			}
		}
	}
	
	
	public  long getLong(String key){
		synchronized (this) {
			if(mSharePref != null){
				return mSharePref.getLong(key, 0L);
			}
			return 0;
		}
	}
	
	public void saveInt(String key,int value){
		synchronized (this) {
			if(mEditor != null){
				mEditor.putInt(key, value);
				mEditor.commit();
			}
		}
	}
	
	
	public int getInt(String key){
		synchronized (this) {
			if(mSharePref != null){
				return mSharePref.getInt(key, -2);
			}
			return 0;
		}
	}
	
	public String getString(String key){
		synchronized (this) {
			if(mSharePref != null){
				return mSharePref.getString(key, "");
			}
			return null;
		}
	}
	
	
	
	
}
