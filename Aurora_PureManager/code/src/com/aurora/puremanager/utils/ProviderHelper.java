package com.aurora.puremanager.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.aurora.puremanager.provider.SharedPreferencesProvider;

public class ProviderHelper {
    private Context mContext;
    private static final boolean DEBUG = true;
    private static final String TAG = "SystemManager/ProviderHelper";

    public ProviderHelper(Context context) {
        mContext = context;
    }

    public int getInt(String key, int defValue) {
        int retValue = defValue;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(getUri("int"), null, key,
                    new String[] {"" + defValue}, null);
            if (cursor != null && cursor.moveToFirst()) {
                String value = cursor.getString(0);
                retValue = Integer.parseInt(value);
            }
        } catch (Exception e) {
        } finally {
            closeCursor(cursor);
        }

        return retValue;
    }

    public void putInt(String key, int value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(key, value);
        try {
            mContext.getContentResolver().update(getUri("int"), contentValues, null, null);
        } catch (Exception e) {
            Log.e(TAG, "ProviderHelper->putInt() throw Exception, " + e.toString());
        }
    }

    public float getFloat(String key, float defValue) {
        float retValue = defValue;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(getUri("float"), null, key,
                    new String[] {"" + defValue}, null);
            if (cursor != null && cursor.moveToFirst()) {
                String value = cursor.getString(0);
                retValue = Float.parseFloat(value);
            }
        } catch (Exception e) {
            Log.e(TAG, "ProviderHelper getFloat() query dababase throw exception");
        } finally {
            closeCursor(cursor);
        }

        return retValue;
    }

    public void putFloat(String key, float value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(key, value);
        try {
            mContext.getContentResolver().update(getUri("float"), contentValues, null, null);
        } catch (Exception e) {
            Log.e(TAG, "ProviderHelper->putFloat() throw Exception, " + e.toString());
        }
    }

    public boolean getBoolean(String key, boolean defValue) {

        boolean retValue = defValue;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(getUri("boolean"), null, key,
                    new String[] {"" + defValue}, null);
            if (cursor != null && cursor.moveToFirst()) {
                String value = cursor.getString(0);
                retValue = "true".equals(value) ? true : false;
            }
        } catch (Exception e) {
            Log.e(TAG, "ProviderHelper getBoolean(), query dababase throw exception");
        } finally {
            closeCursor(cursor);
        }
        return retValue;
    }

    public void putBoolean(String key, boolean value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(key, value);
        try {
            mContext.getContentResolver().update(getUri("boolean"), contentValues, null, null);
        } catch (Exception e) {
            Log.e(TAG, "ProviderHelper->putBoolean() throw Exception, " + e.toString());
        }
    }

    public String getString(String key, String defValue) {

        String retValue = defValue;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(getUri("string"), null, key,
                    new String[] {defValue}, null);
            if (cursor != null && cursor.moveToFirst()) {
                String value = cursor.getString(0);
                retValue = value;
            }
        } catch (Exception e) {
            Log.e(TAG, "ProviderHelper getString(), query dababase throw exception");
        } finally {
            closeCursor(cursor);
        }
        return retValue;
    }

    public void putString(String key, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(key, value);
        try {
            mContext.getContentResolver().update(getUri("string"), contentValues, null, null);
        } catch (Exception e) {
            Log.e(TAG, "ProviderHelper->putString() throw Exception, " + e.toString());
        }
    }

    private Uri getUri(String str) {
        return Uri.parse("content://" + SharedPreferencesProvider.AUTHORITY + "/" + str);
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

}
