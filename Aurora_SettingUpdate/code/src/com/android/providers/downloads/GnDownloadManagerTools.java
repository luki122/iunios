package com.android.providers.downloads;

import gn.com.android.update.utils.LogUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.DownloadManager.Request;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Downloads;
import android.util.Log;

public class GnDownloadManagerTools {

    public static final String COLUMN_PRIORITY_LEVEL = "priority_level";
    public static final String COLUMN_STORAGE = "storage";

    public static final int MIM_PRIORITY = 0;
    public static final int MAX_PRIORITY = 10;

    public static final int STORAGE_INTERNAL = 1;
    public static final int STORAGE_SDCARD = 2;

    public static final int getPriority(ContentResolver resolver, long downloadId) {
        Cursor cursor = null;
        int priority = 0;
        try {
            cursor = resolver.query(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI,
                    new String[] {COLUMN_PRIORITY_LEVEL}, Downloads.Impl._ID + "=?",
                    new String[] {String.valueOf(downloadId)}, null);
            cursor.moveToFirst();
            priority = cursor.getInt(cursor.getColumnIndex(COLUMN_PRIORITY_LEVEL));
        } catch (Exception e) {
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return priority;
    }

    public static final void setPriority(ContentResolver resolver, long downloadId, int priority) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRIORITY_LEVEL, priority);
        resolver.update(ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, downloadId),
                values, null, null);
    }

    public static final void setPriorityTop(ContentResolver resolver, long downloadId) {
        setPriority(resolver, downloadId, MAX_PRIORITY);
    }

    public static final void pause(ContentResolver resolver, long downloadId) {
        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.COLUMN_CONTROL, Downloads.Impl.CONTROL_PAUSED);
        resolver.update(ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, downloadId),
                values, null, null);
    }

    public static final void restart(ContentResolver resolver, long downloadId) {
        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.COLUMN_CONTROL, Downloads.Impl.CONTROL_RUN);
        values.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_PENDING);
        int result = resolver.update(ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, downloadId),
                values, null, null);
        LogUtils.log("updateDownloadDB", "restartDownload   update result   =  " + result);
    }

    public static final long enqueueAndChoiceStorage(Context context, Request request, int storageId) {
        try {
            ContentValues values = requestToContentValues(request, context.getPackageName());
            values.put(COLUMN_STORAGE, storageId);
            Uri downloadUri = context.getContentResolver().insert(Downloads.Impl.CONTENT_URI, values);
            long id = Long.parseLong(downloadUri.getLastPathSegment());
            return id;
        } catch (Exception e) {
            Log.e("GnDownloadManagerTools", "enqueueAndChoiceStorage: storageId = " + storageId, e);
        }
        return -1;
    }

    private static final ContentValues requestToContentValues(Request request, String packageName)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//      ContentValues values = request.toContentValues(context.getPackageName());
        Class<?> clazz = request.getClass();
        Method method = clazz.getDeclaredMethod("toContentValues", new Class[] {String.class});
        method.setAccessible(true);
        ContentValues values = (ContentValues) method.invoke(request, new Object[] {packageName});
        return values;

    }
}
