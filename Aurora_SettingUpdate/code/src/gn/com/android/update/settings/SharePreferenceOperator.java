package gn.com.android.update.settings;

import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharePreferenceOperator {
    private String mSharePreferenceName = "share_preference_name";
    private SharedPreferences mSharedPreferences = null;
    private Editor mEditor = null;

    public SharePreferenceOperator() {

    }

    public SharePreferenceOperator(Context context, String sharePreferenceName) {
        mSharePreferenceName = sharePreferenceName;
        mSharedPreferences = context.getSharedPreferences(mSharePreferenceName, Context.MODE_WORLD_READABLE|Context.MODE_WORLD_WRITEABLE);
        mEditor = mSharedPreferences.edit();

    }

    protected void initial() {
        mEditor.clear().commit();
    }

    protected String getStringValue(String key, String defValue) {
        String value = mSharedPreferences.getString(key, defValue);
        return value;
    }

    protected int getIntValue(String key, int defValue) {
        int value = mSharedPreferences.getInt(key, defValue);
        return value;
    }

    protected long getLongValue(String key, long defValue) {
        long value = mSharedPreferences.getLong(key, defValue);
        return value;

    }

    protected boolean contains(String key) {
        return mSharedPreferences.contains(key);
    }

    protected Map<String, ?> getAll() {
        return mSharedPreferences.getAll();
    }

    protected float getFloatValue(String key, float defValue) {
        float value = mSharedPreferences.getFloat(key, defValue);
        return value;
    }

    protected boolean getBooleanValue(String key, boolean defValue) {
        boolean value = mSharedPreferences.getBoolean(key, defValue);
        return value;
    }

    protected Set<String> getStringSet(String key, Set<String> defValues) {
        return mSharedPreferences.getStringSet(key, defValues);
    }

    protected void setStringValue(String key, String value) {
        mEditor.putString(key, value).commit();
    }

    protected void setIntValue(String key, int value) {
        mEditor.putInt(key, value).commit();
    }

    protected void setLongValue(String key, long value) {
        mEditor.putLong(key, value).commit();
    }

    protected void setBooleanValue(String key, boolean value) {
        mEditor.putBoolean(key, value).commit();

    }

    protected void setFloatValue(String key, float value) {
        mEditor.putFloat(key, value).commit();

    }

    protected void remove(String key) {
        mEditor.remove(key).commit();
    }

    protected void setStringSetValue(String key, Set<String> value) {
        mEditor.putStringSet(key, value);
    }
}
