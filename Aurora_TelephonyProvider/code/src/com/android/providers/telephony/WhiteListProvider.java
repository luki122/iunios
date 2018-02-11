package com.android.providers.telephony;

//Aurora xuyong 2014-09-02 created for whitelist feature
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class WhiteListProvider extends ContentProvider {
    
    private static final String AUTHORITY = "white-list";
     
    private static final UriMatcher sURLMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    
    public static final String WHITELIST_TABLE = "whitelist";
    
    private static final int WHITE_RECIPIENT_PHONE_NUMBER = 0;
    
    private static final String VND_ANDROID_DIR_WHITE_LIST =
            "vnd.android-dir/white-list";
    
    private SQLiteOpenHelper mOpenHelper;
    
    static {
        sURLMatcher.addURI(AUTHORITY, "recipient", WHITE_RECIPIENT_PHONE_NUMBER);
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return VND_ANDROID_DIR_WHITE_LIST;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        switch(sURLMatcher.match(uri)) {
            case WHITE_RECIPIENT_PHONE_NUMBER:
                db.insert(WHITELIST_TABLE, null, values);
                break;
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        mOpenHelper = MmsSmsDatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        switch(sURLMatcher.match(uri)) {
            case WHITE_RECIPIENT_PHONE_NUMBER:
                return db.query(WHITELIST_TABLE, projection, selection, selectionArgs, null, null, null);
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

}
