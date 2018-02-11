
package com.aurora.note.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.aurora.note.NoteApp;

import java.util.Map;
import java.util.Set;

/**
 * 备忘录配置文件操作API
 * @author JimXia
 * 2014-7-29 下午4:27:48
 */
public class NotePerfencesUtil {

    private final static String TAG = "NotePerfencesUtil";

    private final static SharedPreferences sp = NoteApp.ysApp.getSharedPreferences(
            Globals.SHARE_PREF_NAME, Context.MODE_PRIVATE);
    private final static SharedPreferences.Editor editor = sp.edit();

    public synchronized static void putStringValue(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public synchronized static void putIntValue(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public synchronized static void putLongValue(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public synchronized static void putBooleanValue(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static String getString(String key) {
        return getString(key, "");
    }

    public static String getString(String key, String defaultValue) {
        String temp = sp.getString(key, defaultValue);
        return temp;
    }

    public synchronized static void removeString(String key) {
        editor.remove(key);
        editor.commit();
    }

    public static int getInt(String key) {
        return getInt(key, 0);
    }

    public static int getInt(String key, int defaultValues) {
        int temp = sp.getInt(key, defaultValues);
        return temp;
    }

    public static long getLong(String key) {
        return getLong(key, 0L);
    }

    private static long getLong(String key, long defValue) {
        long temp = sp.getLong(key, defValue);
        return temp;
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        boolean temp = sp.getBoolean(key, defaultValue);
        return temp;
    }

    public static void dump() {
        Set<String> keySet = sp.getAll().keySet();
        Map<String, ?> keyToValuesMap = (Map<String, ?>) sp.getAll();
        for (String key : keySet) {
            Log.v(TAG, key + " = " + keyToValuesMap.get(key));
        }
    }

}