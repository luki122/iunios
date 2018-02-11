package gn.com.android.update.utils;

import gn.com.android.update.business.Config;
import android.app.DownloadManager;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Downloads;

public class CursorUtil {
    public static final String COLUMN_NOTIFICATION_PACKAGE = "notificationpackage";
    public static final Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");
    public static final String _ID = "_id";
    public static final String CURRENT_BYTES = "current_bytes";
    private static final String TAG = "CursorUtil";

    public static void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
    }

    public static long getDownloadIdByUrl(Context context, String url) {
        String selection = COLUMN_NOTIFICATION_PACKAGE + "=? AND " + DownloadManager.COLUMN_URI + "=?";
        String[] selectionArgs = new String[] {context.getPackageName(), url};

        long downloadId =Config.ERROR_DOWNLOAD_ID;
        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(CONTENT_URI, null, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                downloadId = getLong(cursor, _ID);
            }

        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.loge(TAG, "queryDownloadId()->" + e.getMessage());
        } finally {
            closeCursor(cursor);
        }

        return downloadId;
    }
    
    public static long getCurrentBytesById(Context context,long id) {
    	LogUtils.loge(TAG, "getCurrentBytesById()->id  =  " + id);
        String selection = _ID + "=?";
        String[] selectionArgs = new String[] {String.valueOf(id)};

        long currentBytes = 0;
        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(CONTENT_URI, null, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
            	currentBytes = getLong(cursor, CURRENT_BYTES);
            }
            LogUtils.loge(TAG, "getCurrentBytesById()->cursor  =  " + cursor);

        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.loge(TAG, "getCurrentBytesById()->" + e.getMessage());
        } finally {
            closeCursor(cursor);
        }
        LogUtils.loge(TAG, "getCurrentBytesById()->currentBytes  =  " + currentBytes);
        return currentBytes;
    }
    public static boolean deleteOneRecordById(Context context,long downloadId){
    	return context.getContentResolver().delete(ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, downloadId), null, null)==0?false:true;
    }
    public static int getInt(Cursor cursor, String column) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(column));
    }

    public static Long getLong(Cursor cursor, String column) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(column));

    }

    public static String getString(Cursor cursor, String column) {

        return cursor.getString(cursor.getColumnIndexOrThrow(column));
    }
}
