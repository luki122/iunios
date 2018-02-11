/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */

package com.gionee.mms.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

public class RecommendManagerDbAdater {
    public static final String TAG = "RecommendManagerDbAdater";

    public static final String DATABASE_NAME = "recommendmanager.db";
    public static final int DATABASE_VERSON = 1;
    public static final String TABLE_RECOMMEND = "recommend_text";
    public static final String TABLE_GROUPS = "groups";

    public static final String TABLERECOMMENDS = "CREATE TABLE recommend_text ("
            + "_id INTEGER PRIMARY KEY," + "group_id INTEGER," + "text TEXT);";
    public static final String TABLEGROUPS = "CREATE TABLE groups (" + "_id INTEGER PRIMARY KEY,"
            + "groupName TEXT," + "count INTEGER);";

    public final static class RecommendGroup {
        public static String mColumId = "_id";
        public static String mColumName = "groupName";
        public static String mColumCount = "count";
    }

    public final static class RecommendItem {
        public static String mColumId = "_id";
        public static String mColumGroupId = "group_id";
        public static String mColumText = "text";
    }

    private Context mContext;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase mSQLiteDatabase = null;

    public RecommendManagerDbAdater(Context context) {
        this.mContext = context;
    }

    public void open() {
        dbHelper = new DatabaseHelper(mContext);
        mSQLiteDatabase = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
        System.out.println(">>>>>>>>>>>>>>>>>>>>>> CLOSE <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        Context mContext;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSON);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i("TAG", "create table start...");

            db.execSQL(TABLEGROUPS);
            db.execSQL(TABLERECOMMENDS);

            ContentValues recommendValues = new ContentValues(2);
            String[] default_recommend = mContext.getResources().getStringArray(
                    com.android.mms.R.array.default_recommend);

            long currentGroupId = 0;
            for (int i = 0; i < default_recommend.length; i++) {
                recommendValues.clear();
                if (default_recommend[i].startsWith("group:")) {
                    String groupName = default_recommend[i].substring(6);
                    recommendValues = new ContentValues(1);
                    recommendValues.put(RecommendGroup.mColumName, groupName);
                    currentGroupId = db.insert(TABLE_GROUPS, null, recommendValues);
                } else {
                    recommendValues.put(RecommendItem.mColumGroupId, currentGroupId);
                    recommendValues.put(RecommendItem.mColumText, default_recommend[i]);
                    db.insert(TABLE_RECOMMEND, null, recommendValues);
                }
            }

            Log.i("TAG", "create table over...");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECOMMEND);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
            onCreate(db);
        }
    }

    public byte[] getBitmapByte(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "transform byte exception");
        }
        return out.toByteArray();
    }

    public Cursor getAllGroups() {
        return mSQLiteDatabase.query(TABLE_GROUPS, null, null, null, null, null, null);
    }

    public Cursor getItemByGroupId(long id) {
        return mSQLiteDatabase.query(TABLE_RECOMMEND, null, "group_id='" + id + "'", null, null,
                null, null);
    }

    private long getGroupIdByGroupName(String groupName) {

        long retValue = -1;
        Cursor cursor = mSQLiteDatabase.query(TABLE_GROUPS, new String[] {
            "_id"
        }, RecommendGroup.mColumName + "='" + groupName + "'", null, null, null, null);

        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            retValue = cursor.getInt(0);
        }

        if (cursor != null) {
            cursor.close();
        }

        return retValue;
    }

    public int getCountItemsByGroupName(String groupName) {
        int count = 0;
        long groupId = getGroupIdByGroupName(groupName);
        String sql = "select count(*) from " + TABLE_RECOMMEND + " where "
                + RecommendItem.mColumGroupId + "=" + groupId;
        Cursor cursor = mSQLiteDatabase.rawQuery(sql, null);

        if (cursor != null && cursor.getCount() != 0) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        return count;
    }

    public void updateSyncData(String sql, Object[] Args) {
        mSQLiteDatabase.execSQL(sql, Args);
    }

    public String checkContactGroup(String sql, String selectionArgs[]) {
        String groupName = "";
        Cursor cursor = mSQLiteDatabase.rawQuery(sql, selectionArgs);
        if (cursor.moveToFirst()) {
            groupName = cursor.getString(0);
        }
        cursor.close();
        return groupName;
    }

    public Cursor getCursorBySql(String sql, String selectionArgs[]) {
        return mSQLiteDatabase.rawQuery(sql, selectionArgs);
    }

    public long inserDataToGroups(String groupName) {

        ContentValues content = new ContentValues();
        content.put(RecommendGroup.mColumName, groupName);
        return mSQLiteDatabase.insert(TABLE_GROUPS, null, content);

    }

    public int deleteDataFromGroups(String groupName) {
        return mSQLiteDatabase.delete(TABLE_GROUPS, RecommendGroup.mColumName + "='" + groupName
                + "'", null);
    }

    public int updateDataToGroups(String newgroupName, String oldgroupName) {
        ContentValues content = new ContentValues();
        content.put(RecommendGroup.mColumName, newgroupName);
        return mSQLiteDatabase.update(TABLE_GROUPS, content, RecommendGroup.mColumName + "='"
                + oldgroupName + "'", null);
    }

    public long inserItemToGroups(String text, String groupName) {
        ContentValues content = new ContentValues();

        long groupId = getGroupIdByGroupName(groupName);
        content.put(RecommendItem.mColumGroupId, groupId);
        content.put(RecommendItem.mColumText, text);

        return mSQLiteDatabase.insert(TABLE_RECOMMEND, null, content);
    }
}
