/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.deskclock;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
//Gionee <baorui><2013-05-07> modify for CR00803588 begin
import android.database.Cursor;
//Gionee <baorui><2013-05-07> modify for CR00803588 end

/**
 * Helper class for opening the database from multiple providers.  Also provides
 * some common functionality.
 */
class AlarmDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "alarms.db";
    // Gionee <baorui><2013-05-07> modify for CR00803588 begin
    // private static final int DATABASE_VERSION = 5;
    private static final int DATABASE_VERSION = 6;
    private Context mContext;
    // Gionee <baorui><2013-05-07> modify for CR00803588 end

    public AlarmDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // Gionee <baorui><2013-05-07> modify for CR00803588 begin
        mContext = context;
        // Gionee <baorui><2013-05-07> modify for CR00803588 end
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE alarms (" +
                   "_id INTEGER PRIMARY KEY," +
                   "hour INTEGER, " +
                   "minutes INTEGER, " +
                   "daysofweek INTEGER, " +
                   "alarmtime INTEGER, " +
                   "enabled INTEGER, " +
                   "vibrate INTEGER, " +
                   "message TEXT, " +
                   "alert TEXT);");

        // insert default alarms
        //aurora mod by tangjun 2014.1.17  need to add default alarm start
        
        String insertMe = "INSERT INTO alarms " +
                "(hour, minutes, daysofweek, alarmtime, enabled, vibrate, " +
                " message, alert) VALUES ";
        //db.execSQL(insertMe + "(8, 30, 31, 0, 0, 1, '', '');");
        //db.execSQL(insertMe + "(9, 00, 96, 0, 0, 1, '', '');");
        
        db.execSQL(insertMe + "(7, 30, 31, 0, 0, 1, '', '');");
        db.execSQL(insertMe + "(10, 00, 96, 0, 0, 1, '', '');");
        
        //aurora mod by tangjun 2014.1.17  need to add default alarm end
        
        // Gionee <baorui><2013-05-04> modify for CR00803588 begin
        db.execSQL("CREATE TABLE alert_info (" + "_id INTEGER PRIMARY KEY," + "_data TEXT, " + "alert TEXT, "
                + "volumes INTEGER);");

        // insert default table alert info , recording media path information
        //aurora mod by tangjun 2014.1.17 don't need to add default alarm start
        /*
        String mInsertMe = "INSERT INTO alert_info (_data, alert, volumes) VALUES ";
        db.execSQL(mInsertMe + "('', '', 0);");
        db.execSQL(mInsertMe + "('', '', 0);");
        */
        //aurora mod by tangjun 2014.1.17 don't need to add default alarm end
        // Gionee <baorui><2013-05-04> modify for CR00803588 end
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
            int currentVersion) {
        // Gionee <baorui><2013-05-07> modify for CR00803588 begin
        if (6 == currentVersion) {
            Log.v("onUpgrade: oldVersion = " + oldVersion + "currentVersion = " + currentVersion);
            db.execSQL("CREATE TABLE IF NOT EXISTS alert_info(" + "_id INTEGER PRIMARY KEY," + "_data TEXT, "
                    + "alert TEXT, " + "volumes INTEGER);");
            Cursor mCursor = db.rawQuery("SELECT _id , alert FROM alarms", null);
            String mInsertMe = "INSERT INTO alert_info (_id, _data, alert, volumes) VALUES ";
            if (null != mCursor) {
                if (mCursor.moveToFirst()) {
                    do {
                        int id = mCursor.getInt(0);
                        String alert = mCursor.getString(1);
                        // Gionee <baorui><2013-05-22> modify for CR00818396 begin
                        String data = "";
                        if (alert != null && alert.length() != 0) {
                            data = Alarms.getExternalUriData(mContext, Uri.parse(alert));
                        }
                        // Gionee <baorui><2013-05-22> modify for CR00818396 end

                        int volumes = Alarms.getVolumes(mContext);

                        // Gionee <baorui><2013-07-04> modify for CR00833031 begin
                        if (null != alert) {
                            alert = alert.replace("'", "''");
                        }

                        if (null != data) {
                            data = data.replace("'", "''");
                        }

                        db.execSQL(mInsertMe + "(" + id + ", '" + data + "', '" + alert + "', " + volumes
                                + ");");
                        // Gionee <baorui><2013-07-04> modify for CR00833031 end
                    } while (mCursor.moveToNext());
                }
            }
            return;
        }
        // Gionee <baorui><2013-05-07> modify for CR00803588 end
        Log.v(
                "Upgrading alarms database from version " +
                oldVersion + " to " + currentVersion +
                ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS alarms");
        // Gionee <baorui><2013-05-04> modify for CR00803588 begin
        db.execSQL("DROP TABLE IF EXISTS alert_info");
        // Gionee <baorui><2013-05-04> modify for CR00803588 end
        onCreate(db);
    }

    // Gionee <baorui><2013-05-07> modify for CR00803588 begin
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v("Downgrade alarms database from version " + oldVersion + " to " + newVersion);
        if (6 == oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS alert_info");
        }
    };
    // Gionee <baorui><2013-05-07> modify for CR00803588 end

    Uri commonInsert(ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        long rowId = db.insert("alarms", Alarm.Columns.MESSAGE, values);
        if (rowId < 0) {
            throw new SQLException("Failed to insert row");
        }
        Log.v("Added alarm rowId = " + rowId);

        return ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, rowId);
    }

    // Gionee <baorui><2013-05-04> modify for CR00803588 begin
    Uri gnCommonInsert(ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        long rowId = db.insert("alert_info", null, values);
        if (rowId < 0) {
            throw new SQLException("Failed to insert row");
        }
        Log.v("Added alarm rowId = " + rowId);

        return ContentUris.withAppendedId(Alarm.Columns.ALERTINFO_URI, rowId);
    }
    // Gionee <baorui><2013-05-04> modify for CR00803588 end
}
