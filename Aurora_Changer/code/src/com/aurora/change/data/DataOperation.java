package com.aurora.change.data;

import com.aurora.change.receiver.ChangeReceiver;
import com.aurora.change.utils.Consts;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class DataOperation {
	private static final String CHANGE_PREF_NAME = "aurora_change";
//    private static DataOperation mDataOperation;
//    private static SharedPreferences mSharedPreferences;
//    private static SharedPreferences.Editor mEditor;

    /*private DataOperation(Context context) {
        mSharedPreferences = context.getSharedPreferences(CHANGE_PREF_NAME, Context.MODE_PRIVATE);//PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mSharedPreferences.edit();
		  // Aurora liugj 2014-08-20 modified for pre-preferences change to new start
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        if (sp.contains(Consts.IS_FIRST_START)) {
			mEditor.putBoolean(Consts.IS_FIRST_START, sp.getBoolean(Consts.IS_FIRST_START, false));
			mEditor.commit();
			editor.remove(Consts.IS_FIRST_START);
			editor.commit();
		}
        if (sp.contains(Consts.CURRENT_LOCKPAPER_GROUP)) {
        	mEditor.putString(Consts.CURRENT_LOCKPAPER_GROUP, sp.getString(Consts.CURRENT_LOCKPAPER_GROUP, Consts.DEFAULT_LOCKPAPER_GROUP));
        	mEditor.commit();
        	editor.remove(Consts.CURRENT_LOCKPAPER_GROUP);
        	editor.commit();
		}
		// Aurora liugj 2014-08-20 modified for pre-preferences change to new end
    }*/
    
	 public static void setBooleanPreference(Context context, String key, boolean value) {
        SharedPreferences preferences = context.getSharedPreferences(CHANGE_PREF_NAME, Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    
    public static boolean getBooleanPreference(Context context, String key, boolean defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(CHANGE_PREF_NAME, Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
        boolean result = preferences.getBoolean(key, defaultValue);
        return result;
    }
    
    public static void setIntPreference(Context context, String key, int value) {
        SharedPreferences preferences = context.getSharedPreferences(CHANGE_PREF_NAME, Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    
    public static int getIntPreference(Context context, String key, int defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(CHANGE_PREF_NAME, Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
        int result = preferences.getInt(key, defaultValue);
        return result;
    }
    
    public static void setStringPreference(Context context, String key, String value) {
        SharedPreferences preferences = context.getSharedPreferences(CHANGE_PREF_NAME, Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
    
    public static String getStringPreference(Context context, String key, String defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(CHANGE_PREF_NAME, Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
        String result = preferences.getString(key, defaultValue);
        return result;
    }
    
}