/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
// Gionee baorui 2012-12-11 modify for CR00733082 begin
import java.io.File;
// Gionee baorui 2012-12-11 modify for CR00733082 end

public class AlarmProvider extends ContentProvider {
    private AlarmDatabaseHelper mOpenHelper;

    private static final int ALARMS = 1;
    private static final int ALARMS_ID = 2;
    // Gionee <baorui><2013-05-04> modify for CR00803588 begin
    private static final int ALERT_INFO = 3;
    private static final int ALERT_ID = 4;
    // Gionee <baorui><2013-05-04> modify for CR00803588 end
    private static final UriMatcher sURLMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    // Gionee baorui 2013-01-17 modify for CR00763726 begin
    private File mDbFile = null;
    // Gionee baorui 2013-01-17 modify for CR00763726 end

    static {
        sURLMatcher.addURI("com.android.deskclock", "alarm", ALARMS);
        sURLMatcher.addURI("com.android.deskclock", "alarm/#", ALARMS_ID);
        // Gionee <baorui><2013-05-04> modify for CR00803588 begin
        sURLMatcher.addURI("com.android.deskclock", "alert_info", ALERT_INFO);
        sURLMatcher.addURI("com.android.deskclock", "alert_info/#", ALERT_ID);
        // Gionee <baorui><2013-05-04> modify for CR00803588 end
    }

    public AlarmProvider() {
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new AlarmDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection,
            String[] selectionArgs, String sort) {
        // Gionee baorui 2012-12-11 modify for CR00733082 begin
        checkDataBase();
        // Gionee baorui 2012-12-11 modify for CR00733082 end
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // Generate the body of the query
        int match = sURLMatcher.match(url);
        switch (match) {
            case ALARMS:
                qb.setTables("alarms");
                break;
            case ALARMS_ID:
                qb.setTables("alarms");
                qb.appendWhere("_id=");
                qb.appendWhere(url.getPathSegments().get(1));
                break;
            // Gionee <baorui><2013-05-04> modify for CR00803588 begin
            case ALERT_INFO:
                qb.setTables("alert_info");
                break;
            case ALERT_ID:
                qb.setTables("alert_info");
                qb.appendWhere("_id=");
                qb.appendWhere(url.getPathSegments().get(1));
                break;
            // Gionee <baorui><2013-05-04> modify for CR00803588 end
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor ret = qb.query(db, projectionIn, selection, selectionArgs,
                              null, null, sort);

        if (ret == null) {
             Log.v("Alarms.query: failed");
        } else {
            ret.setNotificationUri(getContext().getContentResolver(), url);
        }

        return ret;
    }

    @Override
    public String getType(Uri url) {
        int match = sURLMatcher.match(url);
        switch (match) {
            case ALARMS:
                return "vnd.android.cursor.dir/alarms";
            case ALARMS_ID:
                return "vnd.android.cursor.item/alarms";
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        int count;
        long rowId = 0;
        int match = sURLMatcher.match(url);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (match) {
            case ALARMS_ID: {
                String segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                count = db.update("alarms", values, "_id=" + rowId, null);
                break;
            }
            // Gionee <baorui><2013-05-04> modify for CR00803588 begin
            case ALERT_ID: {
                String segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                count = db.update("alert_info", values, "_id=" + rowId, null);
                break;
            }
            // Gionee <baorui><2013-05-04> modify for CR00803588 end
            default: {
                throw new UnsupportedOperationException(
                        "Cannot update URL: " + url);
            }
        }
        Log.v("*** notifyChange() rowId: " + rowId + " url " + url);
        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        // Gionee <baorui><2013-05-04> modify for CR00803588 begin
        // if (sURLMatcher.match(url) != ALARMS) {
        if (sURLMatcher.match(url) != ALARMS && sURLMatcher.match(url) != ALERT_INFO) {
        // Gionee <baorui><2013-05-04> modify for CR00803588 end
            throw new IllegalArgumentException("Cannot insert into URL: " + url);
        }

        // Gionee <baorui><2013-05-04> modify for CR00803588 begin
        // Uri newUrl = mOpenHelper.commonInsert(initialValues);
        Uri newUrl = null;
        if (sURLMatcher.match(url) == ALERT_INFO) {
            newUrl = mOpenHelper.gnCommonInsert(initialValues);
        } else {
            newUrl = mOpenHelper.commonInsert(initialValues);
        }
        // Gionee <baorui><2013-05-04> modify for CR00803588 end
        getContext().getContentResolver().notifyChange(newUrl, null);
        return newUrl;
    }

    public int delete(Uri url, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        long rowId = 0;
        switch (sURLMatcher.match(url)) {
            case ALARMS:
                count = db.delete("alarms", where, whereArgs);
                break;
            case ALARMS_ID:
                String segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                if (TextUtils.isEmpty(where)) {
                    where = "_id=" + segment;
                } else {
                    where = "_id=" + segment + " AND (" + where + ")";
                }
                count = db.delete("alarms", where, whereArgs);
                break;
            // Gionee <baorui><2013-05-04> modify for CR00803588 begin
            case ALERT_INFO:
                count = db.delete("alert_info", where, whereArgs);
                break;
            case ALERT_ID:
                String mSegment = url.getPathSegments().get(1);
                rowId = Long.parseLong(mSegment);
                if (TextUtils.isEmpty(where)) {
                    where = "_id=" + mSegment;
                } else {
                    where = "_id=" + mSegment + " AND (" + where + ")";
                }
                count = db.delete("alert_info", where, whereArgs);
                break;
            // Gionee <baorui><2013-05-04> modify for CR00803588 end
            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + url);
        }

        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }
    
    // Gionee baorui 2012-12-11 modify for CR00733082 begin
    private void checkDataBase() {
        // Gionee baorui 2013-01-17 modify for CR00763726 begin
        if (mDbFile == null) {
            mDbFile = new File("/data/data/com.android.deskclock/databases/alarms.db");
        }
        // Gionee baorui 2013-01-17 modify for CR00763726 end
        if (!mDbFile.exists()) {
            mOpenHelper = new AlarmDatabaseHelper(getContext());
        }
    }
    // Gionee baorui 2012-12-11 modify for CR00733082 end
}
