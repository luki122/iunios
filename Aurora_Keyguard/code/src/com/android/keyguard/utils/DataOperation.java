package com.android.keyguard.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DataOperation {
    private static DataOperation mDataOperation;
    private static SharedPreferences mSharedPreferences;
    private static SharedPreferences.Editor mEditor;

    private DataOperation(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mSharedPreferences.edit();
    }

    public static DataOperation getInstance(Context context) {
        if (mDataOperation == null) {
            mDataOperation = new DataOperation(context);
        }
        return mDataOperation;
    }

    public boolean getBoolean(String key, boolean defValue) {

        return mSharedPreferences.getBoolean(key, defValue);
    }

    public String getString(String key, String defValue) {
        return mSharedPreferences.getString(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return mSharedPreferences.getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return mSharedPreferences.getLong(key, defValue);
    }

    public void putBoolean(String key, boolean value) {
        mEditor.putBoolean(key, value);
        mEditor.commit();
    }

    public void putString(String key, String value) {
        mEditor.putString(key, value);
        mEditor.commit();
    }

    public void putInt(String key, int value) {
        mEditor.putInt(key, value);
        mEditor.commit();
    }

    public void putLong(String key, long value) {
        mEditor.putLong(key, value);
        mEditor.commit();
    }
}
