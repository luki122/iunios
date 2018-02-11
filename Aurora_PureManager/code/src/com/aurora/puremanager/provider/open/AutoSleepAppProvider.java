package com.aurora.puremanager.provider.open;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.aurora.puremanager.data.MyArrayList;
import com.aurora.puremanager.sqlite.OpenDataSqlite;

import java.util.HashSet;

public class AutoSleepAppProvider extends BaseOpenContentProvider {
    private static final String TAG = AutoSleepAppProvider.class.getName();

    private static final String URL_STR = "content://com.aurora.puremanager.provider.open.AuroraAutoSleepAppProvider";
    public static final Uri CONTENT_URI = Uri.parse(URL_STR);

    private final static HashSet<String> mAutoSleepAppList = new HashSet<String>();

    /**
     * 从数据库中获取所有存储应用包名
     *
     * @param context
     * @return
     */
    public static HashSet<String> loadAutoSleepAppListInDB(Context context) {
        MyArrayList<String> autoSleepAppList = queryAllAppsInfo(context);
        mAutoSleepAppList.clear();
        for (String app : autoSleepAppList.getDataList()) {
            mAutoSleepAppList.add(app);
        }
        return mAutoSleepAppList;
    }

    /**
     * 在数据库中添加应用包名
     *
     * @param context
     * @param pkgName
     */
    public static void addAppInDB(Context context, String pkgName) {
        insertOrUpdateDate(context, pkgName);
        return;
    }

    /**
     * 在数据库中删除应用包名
     *
     * @param context
     * @param pkgName
     */
    public static void deleteAppInDB(Context context, String pkgName) {
        deleteDate(context, pkgName);
        return;
    }

    /**
     * 应用包名是否已在数据库中
     * @param context
     * @param pkgName
     * @return
     */
    public static boolean isInDB(Context context, String pkgName) {
        return isHave(context, getQueryWhere(), getQueryValue(pkgName), CONTENT_URI);
    }

    private static void insertOrUpdateDate(Context context, String pkgName) {
        if (context == null ||
                pkgName == null) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(OpenDataSqlite.PACKAGE_NAME, pkgName);

        if (isHave(context,
                getQueryWhere(),
                getQueryValue(pkgName),
                CONTENT_URI)) {
            //do nothing
        } else {
            Log.i(TAG, "insert " + pkgName);
            context.getContentResolver().insert(CONTENT_URI, values);
        }
    }

    private static void deleteDate(Context context, String packageName) {
        if (context == null || packageName == null) {
            return;
        }

        context.getContentResolver().delete(
                CONTENT_URI,
                getQueryWhere(),
                getQueryValue(packageName));
        Log.i(TAG, "delete " + packageName);
    }

    private static MyArrayList<String> queryAllAppsInfo(Context context) {
        MyArrayList<String> appInfoList = new MyArrayList<String>();

        if (context == null) {
            return appInfoList;
        }

        String[] columns = {OpenDataSqlite.PACKAGE_NAME}; //需要返回的列名

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(CONTENT_URI, columns, null, null, null);
        } catch (Exception e) {
            //nothing
        }

        synchronized (CONTENT_URI) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String pkgName = cursor.getString(
                            cursor.getColumnIndexOrThrow(OpenDataSqlite.PACKAGE_NAME));
                    appInfoList.add(pkgName);
                }
                cursor.close();
            }
        }
        return appInfoList;
    }

    private static String getQueryWhere() {
        return OpenDataSqlite.PACKAGE_NAME + " = ?";
    }

    private static String[] getQueryValue(String packageName) {
        String[] whereValue = {packageName};
        return whereValue;
    }

    @Override
    public String getTableName() {
        return OpenDataSqlite.TABLE_NAME_OF_AutoSleepApp;
    }

    @Override
    public Uri getContentUri() {
        return CONTENT_URI;
    }
}
