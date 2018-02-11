package com.gionee.mms.online.data;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.gionee.mms.online.LogUtils;

import java.util.ArrayList;

public class MyContentProvider extends ContentProvider {

    public final static String AUTHORITY = "com.gionee.mms.inline";
    public final static Uri COLUMN_URI = Uri.parse("content://" + AUTHORITY
            + "/column");
    public final static Uri INFORMATION_URI = Uri.parse("content://"
            + AUTHORITY + "/information");
    private final static int COLUMNS = 1;
    private final static int COLUMN = 2;
    private final static int INFORMATIONS = 3;
    private final static int INFORMATION = 4;
    private final static String TAG = "MyContentProvider";
    private DatebaseHelper mDbHelper;

    private static final UriMatcher pMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        pMatcher.addURI(AUTHORITY, "column", COLUMNS);
        pMatcher.addURI(AUTHORITY, "column/#", COLUMN);
        pMatcher.addURI(AUTHORITY, "information", INFORMATIONS);
        pMatcher.addURI(AUTHORITY, "information/#", INFORMATION);

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        Log.d(TAG, "delete...DatebaseHelper.RECOM_TABLE_NAME = "
                + DatebaseHelper.RECOM_TABLE_NAME + " ,selection = "
                + selection);
        int count = 0;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (pMatcher.match(uri)) {
        case COLUMNS:
            count = db.delete(DatebaseHelper.RECOM_TABLE_NAME, selection,
                    selectionArgs);
            break;
        case INFORMATIONS:
            count = db.delete(DatebaseHelper.INFOR_TABLE_NAME, selection,
                    selectionArgs);
            break;
        }

        Log.d(TAG, "delete...selection ... ");
        return count;
    }

    @Override
    public String getType(Uri uri) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        Log.d("data1", "enter");
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor c = null;
        long pid = 0;
        int tmp = pMatcher.match(uri);

        Log.d("data1", "tmp = " + tmp);
        switch (pMatcher.match(uri)) {
        case COLUMNS:

            c = db.query(DatebaseHelper.RECOM_TABLE_NAME, null,
                    RecommendColumns.CAT_ID + " = ?",
                    new String[] { String.valueOf(values.get("cat_id")) },
                    null, null, null);
            try {
                if (c != null && c.getCount() == 0) {
                    pid = db.insert(DatebaseHelper.RECOM_TABLE_NAME, null,
                            values);
                }
            } catch (Exception e) {
            } finally {
                if ((null != c) && !(c.isClosed())) {
                    c.close();
                }
            }

            return ContentUris.withAppendedId(uri, pid);
        case COLUMN:
            pid = db.insert(DatebaseHelper.RECOM_TABLE_NAME, null, values);
            String path = uri.toString();

            return Uri
                    .parse(path.substring(0, path.lastIndexOf('/') + 1) + pid);

        case INFORMATIONS:
            c = db.query(DatebaseHelper.INFOR_TABLE_NAME, null,
                    InformationColumns.MSG_ID + " = ?",
                    new String[] { String.valueOf(values.get("msg_id")) },
                    null, null, null);
            try {
                if (c != null && c.getCount() == 0) {
                    pid = db.insert(DatebaseHelper.INFOR_TABLE_NAME, null,
                            values);
                }
            } catch (Exception e) {
            } finally {
                if ((null != c) && !(c.isClosed())) {
                    c.close();
                }
            }
            // pid = db.insert(DatebaseHelper.INFOR_TABLE_NAME, null, values);
            return ContentUris.withAppendedId(uri, pid);
        case INFORMATION:
            pid = db.insert(DatebaseHelper.INFOR_TABLE_NAME, null, values);
            path = uri.toString();

            return Uri
                    .parse(path.substring(0, path.lastIndexOf('/') + 1) + pid);
        default:
            throw new IllegalArgumentException("Unknow Uri:" + uri);
        }

    }

    @Override
    public boolean onCreate() {
        LogUtils.log(TAG, LogUtils.getThreadName());
        mDbHelper = new DatebaseHelper(this.getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = null;
        switch (pMatcher.match(uri)) {
        case COLUMNS:
            Log.d("net1", "query ");
            c = db.query(DatebaseHelper.RECOM_TABLE_NAME, projection,
                    selection, selectionArgs, null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);

            break;
        case INFORMATIONS:

            c = db.query(DatebaseHelper.INFOR_TABLE_NAME, projection,
                    selection, selectionArgs, null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            break;
        }
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;
        switch(pMatcher.match(uri)) {
        case COLUMNS:
            count = db.update(DatebaseHelper.RECOM_TABLE_NAME, values, selection, selectionArgs);
            break;
        case INFORMATIONS:
            count = 0;
            break;
        default:
            throw new IllegalArgumentException("Provider update() Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public ContentProviderResult[] applyBatch(
            ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        LogUtils.log(TAG, LogUtils.getThreadName());
        Uri uri = operations.get(0).getUri();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentProviderResult[] results = null;
        db.beginTransaction();
        try {
            results = super.applyBatch(operations);
            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
            getContext().getContentResolver().notifyChange(uri, null);
            LogUtils.log(TAG, "notify cursor");
        }
        return results;
    }
}
