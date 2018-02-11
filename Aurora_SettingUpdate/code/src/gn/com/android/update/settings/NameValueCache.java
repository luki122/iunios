package gn.com.android.update.settings;

import gn.com.android.update.utils.LogUtils;

import java.util.HashMap;

public class NameValueCache {
    private static final String TAG = "NameValueCache";
    private HashMap<String, Object> mValues = new HashMap<String, Object>();

    public String getString(String name, String defValue, SharePreferenceOperator sharePreferenceOperator) {
        String value = defValue;

        if (mValues.containsKey(name)) {
            value = (String) mValues.get(name);
            logd("getString from cache,  name = " + name + " value = " + value);
            return value;
        }

        if (sharePreferenceOperator.contains(name)) {
            value = sharePreferenceOperator.getStringValue(name, defValue);
            insertValue(name, value);
            return value;

        } else {
            logd("getString from cache, name = " + name + " sharepreference not contains this value");
            return defValue;
        }

    }

    public void putString(String name, String value, SharePreferenceOperator sharePreferenceOperator) {

        updateValue(name, value);
        sharePreferenceOperator.setStringValue(name, value);
    }

    public int getInt(String name, int defValue, SharePreferenceOperator sharePreferenceOperator) {
        int value = defValue;

        if (mValues.containsKey(name)) {
            value = (Integer) mValues.get(name);
            logd("getInt from cache,  name = " + name + " value = " + value);
            return value;
        }

        if (sharePreferenceOperator.contains(name)) {
            value = sharePreferenceOperator.getIntValue(name, defValue);
            insertValue(name, value);
            return value;

        } else {
            logd("getInt from cache, name = " + name + " sharepreference not contains this value");
            return defValue;
        }

    }

    public void putInt(String name, int value, SharePreferenceOperator sharePreferenceOperator) {
        updateValue(name, value);
        sharePreferenceOperator.setIntValue(name, value);
    }

    public long getLong(String name, long defValue, SharePreferenceOperator sharePreferenceOperator) {
        long value = defValue;

        if (mValues.containsKey(name)) {
            value = (Long) mValues.get(name);
            logd("getLong from cache,  name = " + name + " value = " + value);
            return value;
        }

        if (sharePreferenceOperator.contains(name)) {
            value = sharePreferenceOperator.getLongValue(name, defValue);
            insertValue(name, value);
            return value;

        } else {
            logd("getLong from cache, name = " + name + " sharepreference not contains this value");
            return defValue;
        }

    }

    public void putLong(String name, long value, SharePreferenceOperator sharePreferenceOperator) {
        updateValue(name, value);
        sharePreferenceOperator.setLongValue(name, value);
    }

    public boolean getBoolean(String name, boolean defValue, SharePreferenceOperator sharePreferenceOperator) {
        boolean value = defValue;

        if (mValues.containsKey(name)) {
            value = (Boolean) mValues.get(name);
            logd("getBoolean from cache,  name = " + name + " value = " + value);
            return value;
        }

        if (sharePreferenceOperator.contains(name)) {
            value = sharePreferenceOperator.getBooleanValue(name, defValue);
            insertValue(name, value);
            return value;

        } else {
            logd("getBoolean from cache,  name = " + name + " sharepreference not contains this value");
            return defValue;
        }

    }

    public void initial(SharePreferenceOperator sharePreferenceOperator) {

        synchronized (this) {
            mValues.clear();
        }

        sharePreferenceOperator.initial();
    }

    public void remove(String key, SharePreferenceOperator sharePreferenceOperator) {
        logd("remove,  key = " + key);
        synchronized (this) {
            mValues.remove(key);
        }
        sharePreferenceOperator.remove(key);
    }

    public void putBoolean(String name, boolean value, SharePreferenceOperator sharePreferenceOperator) {
        updateValue(name, value);
        sharePreferenceOperator.setBooleanValue(name, value);
    }

    private void insertValue(String name, Object value) {
        synchronized (this) {
            mValues.put(name, value);
        }

        logd("insertValue to cache,  name = " + name + " value = " + value);
    }

    private void updateValue(String name, Object value) {

        synchronized (this) {
            if (mValues.containsKey(name)) {
                mValues.remove(name);
                mValues.put(name, value);

            } else {
                mValues.put(name, value);
            }
        }

        logd("updateValue to cache,  name = " + name + " value = " + value);
    }

    private void logd(String msg) {
        LogUtils.logd(TAG, msg);
    }

    protected void clearCache() {
        mValues.clear();
    }

}
