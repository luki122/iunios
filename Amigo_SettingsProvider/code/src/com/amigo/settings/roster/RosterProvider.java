// Gionee <liuyb> <2013-11-7> add for CR00948264 begin
package com.amigo.settings.roster;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class RosterProvider extends ContentProvider {

    private static final String TAG = "RosterProvider";

    private final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private RosterHelper mDbHelper;

    private static final int CODE_VND_DIR = 1;
    private static final int CODE_VND_ITEM = 2;

    @Override
    public boolean onCreate() {

        mUriMatcher.addURI(AUTHOURITY, Roster.TABLE, CODE_VND_DIR);
        mUriMatcher.addURI(AUTHOURITY, Roster.TABLE + "/#", CODE_VND_ITEM);

        mDbHelper = new RosterHelper(getContext());

        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case CODE_VND_DIR:
                return "vnd.android.cursor.dir/" + Roster.TABLE;
            case CODE_VND_ITEM:
                return "vnd.android.cursor.item/" + Roster.TABLE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sort) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (mUriMatcher.match(uri)) {
            case CODE_VND_DIR:
                qb.setTables(Roster.TABLE);
                break;

            case CODE_VND_ITEM:
                qb.setTables(Roster.TABLE);
                qb.appendWhere(Roster.Column._ID + "=" + uri.getPathSegments().get(1));

                break;
        }

        return qb.query(mDbHelper.getWritableDatabase(), projection, selection, selectionArgs, null, null,
                null);
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        long rowId = -1;
        Uri contentUri = null;

        switch (mUriMatcher.match(uri)) {
            case CODE_VND_DIR:
                rowId = mDbHelper.getWritableDatabase().insert(Roster.TABLE, null, contentValues);
                contentUri = Roster.CONTENT_URI;
                break;
            default:
                break;
        }

        if (rowId > 0) {
            Uri newUri = ContentUris.withAppendedId(contentUri, rowId);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {

        int count = 0;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        switch (mUriMatcher.match(uri)) {
            case CODE_VND_DIR:
                count = db.delete(Roster.TABLE, where, whereArgs);
                break;
            case CODE_VND_ITEM:
                String id = uri.getPathSegments().get(1);
                count = db.delete(Roster.TABLE, Roster.Column._ID + "=" + id
                        + (!TextUtils.isEmpty(where) ? (" AND (" + where + ")") : ""), whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unsupport URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {

        int count = 0;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        switch (mUriMatcher.match(uri)) {

            case CODE_VND_DIR:
                count = db.update(Roster.TABLE, values, where, whereArgs);
                break;
            case CODE_VND_ITEM:
                String id = uri.getPathSegments().get(1);
                count = db.update(Roster.TABLE, values,
                        Roster.Column._ID + "=" + id
                                + (!TextUtils.isEmpty(where) ? (" AND (" + where + ")") : ""), whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unsupport URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    private class RosterHelper extends SQLiteOpenHelper {

        private static final String TAG = "RosterHelper";

        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "rosters.db";

        public RosterHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
            initData(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + Roster.TABLE);
            onCreate(db);
        }

        private final String CREATE_TABLE = "create table " + Roster.TABLE + " (" + Roster.Column._ID
                + " integer primary key autoincrement," + Roster.Column.USER_TYPE + " TEXT,"
                + Roster.Column.PACKAGE_NAME + " TEXT," + Roster.Column.STATUS + " INTEGER" + " );";

    }

    private static final String AUTHOURITY = "com.amigo.settings.RosterProvider";

    private static class Roster {

        public static final String TABLE = "rosters";

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHOURITY + "/" + TABLE);

        public static class Column {
            public static final String _ID = "_id";
            public static final String USER_TYPE = "usertype";
            public static final String PACKAGE_NAME = "packagename";
            public static final String STATUS = "status";
        }
    }
    
    private static void initData(SQLiteDatabase db) {
        db.execSQL("insert into rosters(usertype,packagename,status) values('test','com.test',0);");
    }
}
//Gionee <liuyb> <2013-11-7> add for CR00948264 end