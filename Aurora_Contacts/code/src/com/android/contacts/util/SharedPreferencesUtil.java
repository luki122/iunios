package com.android.contacts.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreferencesUtil {
    private static SharedPreferencesUtil spu;
    private static SharedPreferences preference;

    private SharedPreferencesUtil(Context context) {
        preference = context.getSharedPreferences("com.android.contacts",
                context.MODE_PRIVATE);
    }

    public static SharedPreferencesUtil getInstance(Context context) {
        if (spu == null) {
            spu = new SharedPreferencesUtil(context);
        }
        return spu;
    }


    public void putString(String key, String value) {
        Editor editor = preference.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void putInt(String key, int value) {
        Editor editor = preference.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public String getString(String key) {
        String address = preference.getString(key, "");
        return address;
    }

    public int getInt(String key) {
        int address = preference.getInt(key, -1);
        return address;
    }
}
