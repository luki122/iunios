package com.aurora.puremanager.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import java.util.Iterator;
import java.util.Set;

public class SharedPreferencesProvider extends ContentProvider {

    private static final String TAG = "SharedPreferencesProvider";

    private static final int SP_BOOLEAN_KDY_ID = 1;
    private static final int SP_STRING_KDY_ID = 2;
    private static final int SP_INT_KDY_ID = 3;
    private static final int SP_FLOAT_KEY_ID = 4;

    public static final String AUTHORITY = "com.aurora.puremanager.sp";
    private final String SP_NAME  = "powermanager_preferences_state";
    private SharedPreferences mSp = null;
    private Editor mEditor = null;

    private static final UriMatcher URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URL_MATCHER.addURI(AUTHORITY, "boolean", SP_BOOLEAN_KDY_ID);
        URL_MATCHER.addURI(AUTHORITY, "int", SP_INT_KDY_ID);
        URL_MATCHER.addURI(AUTHORITY, "string", SP_STRING_KDY_ID);
        URL_MATCHER.addURI(AUTHORITY, "float", SP_FLOAT_KEY_ID);
    }

    public SharedPreferencesProvider() {

    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri url) {
        int match = URL_MATCHER.match(url);
        switch (match) {
            case SP_BOOLEAN_KDY_ID:
                return "vnd.android.cursor.dir/boolean";
            case SP_STRING_KDY_ID:
                return "vnd.android.cursor.dir/string";
            case SP_INT_KDY_ID:
                return "vnd.android.cursor.dir/int";
            case SP_FLOAT_KEY_ID:
                return "vnd.android.cursor.dir/float";
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        try {
            mSp = getContext().getSharedPreferences(SP_NAME, Context.MODE_MULTI_PROCESS);
            mEditor = mSp.edit();
            int match = URL_MATCHER.match(url);
            Set<String> keySet = values.keySet();
            if (keySet.size() > 0) {
                Iterator<String> itr = keySet.iterator();
                String key = itr.next();
                switch (match) {
                    case SP_BOOLEAN_KDY_ID: {
                        mEditor.putBoolean(key, values.getAsBoolean(key)).commit();
                        break;
                    }
                    case SP_STRING_KDY_ID: {
                        mEditor.putString(key, values.getAsString(key)).commit();
                        break;
                    }
                    case SP_INT_KDY_ID: {
                        mEditor.putInt(key, values.getAsInteger(key).intValue()).commit();
                        break;
                    }
                    case SP_FLOAT_KEY_ID: {
                        mEditor.putFloat(key, values.getAsFloat(key).floatValue()).commit();
                        break;
                    }
                    default: {
                        throw new UnsupportedOperationException("Cannot update URL: " + url);
                    }
                }

            }
        } catch (Exception e) {
            Log.e(TAG, "update", e);
        }

        return 1;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Cursor query(Uri url, String[] projection, String selection, String[] defauleValues,
            String sortOrder) {
        String str = url.getPathSegments().get(0);
        MatrixCursor cursor = null;
        try {
            mSp = getContext().getSharedPreferences(SP_NAME, Context.MODE_MULTI_PROCESS);
            mEditor = mSp.edit();
            String[] tableCursor = new String[] {"values"};
            cursor = new MatrixCursor(tableCursor);
            if ("string".equals(str)) {
                cursor.addRow(new Object[] {mSp.getString(selection, defauleValues[0])});
            }
            if ("boolean".equals(str)) {
                cursor.addRow(new Object[] {mSp.getBoolean(selection, "true".equals(defauleValues[0]) ? true
                        : false)});
            }
            if ("int".equals(str)) {
                cursor.addRow(new Object[] {mSp.getInt(selection, Integer.parseInt(defauleValues[0]))});
            }
            if ("float".equals(str)) {
                cursor.addRow(new Object[] {mSp.getFloat(selection, Float.parseFloat(defauleValues[0]))});
            }
        } catch (Exception e) {
            Log.e(TAG, "query", e);
        }
        return cursor;
    }
}
