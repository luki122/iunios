package com.gionee.mms.online.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gionee.mms.online.LogUtils;

public class DatebaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatebaseHelper";

    public static final String DATABASE_NAME = "InformationInline.db";
    public static final String RECOM_TABLE_NAME = "column";
    public static final String INFOR_TABLE_NAME = "information";

    public static final int DATABASE_VERSION = 2;

    public DatebaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + RECOM_TABLE_NAME + "("
                + RecommendColumns._ID + " INTEGER PRIMARY KEY,"
                + RecommendColumns.CAT_ID + " INTEGER," + RecommendColumns.NAME
                + " Text," + RecommendColumns.COUNT + " INTEGER,"
                + RecommendColumns.TAP + " INTEGER DEFAULT 0" + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + INFOR_TABLE_NAME + "("
                + InformationColumns._ID + " INTEGER PRIMARY KEY,"
                + InformationColumns.CATEGORY_ID + " INTEGER,"
                + InformationColumns.MSG_ID + " INTEGER,"
                + InformationColumns.MSG + " Text" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        if (oldVersion == 1) {
            db.execSQL("DROP TABLE IF EXISTS " + RECOM_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + INFOR_TABLE_NAME);
            oldVersion++;
        }
        onCreate(db);
    }
}
