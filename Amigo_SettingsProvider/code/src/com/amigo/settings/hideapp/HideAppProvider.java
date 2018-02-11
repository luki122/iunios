package com.amigo.settings.hideapp;

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

public class HideAppProvider extends ContentProvider {
    
	private static final String TAG = "HideAppProvider";
    
    private final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    
    private HideAppHelper mDbHelper;
    //uri matcher code
    private static final int CODE_PERM_APP_VND_DIR = 1;
    private static final int CODE_PERM_APP_VND_ITEM = 2;
    
    @Override
    public boolean onCreate() {
    	
    	mUriMatcher.addURI(AUTHOURITY, HideApp.TABLE, CODE_PERM_APP_VND_DIR);
    	mUriMatcher.addURI(AUTHOURITY, HideApp.TABLE + "/#", CODE_PERM_APP_VND_ITEM);

    	
    	mDbHelper = new HideAppHelper(getContext());
    	
    	return true;
    }
    
    @Override
    public String getType(Uri uri) {
    	switch (mUriMatcher.match(uri)) {
	    	case CODE_PERM_APP_VND_DIR:
	    		return "vnd.android.cursor.dir/" + HideApp.TABLE;
	    	case CODE_PERM_APP_VND_ITEM:
	    		return "vnd.android.cursor.item/" + HideApp.TABLE;
	    	default:
	    		throw new IllegalArgumentException("Unknown URI: " + uri);
    	}
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sort) 
    {
    	SQLiteQueryBuilder qb = new SQLiteQueryBuilder();   	
    	
    	switch (mUriMatcher.match(uri)) {
	    	case CODE_PERM_APP_VND_DIR:
	    		qb.setTables(HideApp.TABLE);
	    		break;
	    		
	    	case CODE_PERM_APP_VND_ITEM:
	    		qb.setTables(HideApp.TABLE);
	    		qb.appendWhere(HideApp.Column._ID + "=" + uri.getPathSegments().get(1));
	    		
	    		break;
    	}
    	
    	return qb.query(mDbHelper.getWritableDatabase(), projection, selection, selectionArgs, null, null, null);
    }
    
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
    	
    	long rowId = -1;
    	Uri contentUri = null;
    	
    	switch (mUriMatcher.match(uri)) {
	    	case CODE_PERM_APP_VND_DIR:
	    		rowId = mDbHelper.getWritableDatabase().insert(HideApp.TABLE, null, contentValues);
	    		contentUri = HideApp.CONTENT_URI;
	    		break;
	    	default:
	    		break;
    	}
    	
    	if (rowId > 0){
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
	    	case CODE_PERM_APP_VND_DIR:
	    		count = db.delete(HideApp.TABLE, where, whereArgs);
	    		break;
	    	case CODE_PERM_APP_VND_ITEM:
	    		String id = uri.getPathSegments().get(1);
	    		count = db.delete(HideApp.TABLE, 
	    				HideApp.Column._ID + "=" + id 
	    					+ (!TextUtils.isEmpty(where) ? (" AND (" + where + ")") : ""), 
	    				whereArgs);
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
    		
    	case CODE_PERM_APP_VND_DIR:
    		count = db.update(HideApp.TABLE, values, where, whereArgs);
    		break;
    	case CODE_PERM_APP_VND_ITEM:
    		String id = uri.getPathSegments().get(1);
    		count = db.update(HideApp.TABLE, values,
    		        HideApp.Column._ID + "=" + id 
    					+ (!TextUtils.isEmpty(where) ? (" AND (" + where + ")") : ""), 
    				whereArgs);
    		break;
    		    		
    	default:
    		throw new IllegalArgumentException("Unsupport URI: " + uri);
    	}
    	
    	getContext().getContentResolver().notifyChange(uri, null);
    	
    	return count;
    }
    
    
    private class HideAppHelper extends SQLiteOpenHelper {

    	private static final String TAG = "PermissionHelper";
    	
    	//the underlying database
    	public static final int DATABASE_VERSION = 1;
    	public static final String DATABASE_NAME = "hideapp.db";
    	
        public HideAppHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
    	@Override
        public void onCreate(SQLiteDatabase db) {
    		db.execSQL(CREATE_TABLE_PERMISSION);
        }
    	
    	@Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    		db.execSQL("DROP TABLE IF EXISTS " + HideApp.TABLE);
    		onCreate(db);
    	}
    	
    	private final String CREATE_TABLE_PERMISSION = "create table " + HideApp.TABLE + " ("
    			+ HideApp.Column._ID + " integer primary key autoincrement,"
    			+ HideApp.Column.PACKAGE_NAME + " TEXT,"
                        + HideApp.Column.CLASS_NAME + " TEXT,"
    			+ HideApp.Column.STATUS + " INTEGER"
    			+ " );";
    	
    }
    
    //------------------------------------------------------------//
    //!!!!!!!!!!!!!!!!ATTENTION PLEASE!!!!!!!!!!!!!!!!!
    //IF YOU DECIDE TO MODIFY BELOW STUFF,YOU SHOULD MODIFY A COPY
    //FILE WHICH IS PLACED IN APP SETTINGS FOR SYNC,KEEP IT IN MIND
    //------------------------------------------------------------//
    
    private static final String AUTHOURITY = "com.amigo.settings.HideAppProvider";
    
	private static class HideApp {
		
		public static final String TABLE = "hide";
		
		public static final Uri CONTENT_URI = Uri.parse("content://" 
				+ AUTHOURITY + "/"+ TABLE);
		
		public static class Column{
			public static final String _ID = "_id";
			public static final String PACKAGE_NAME = "package";
                        public static final String CLASS_NAME = "class";
			public static final String STATUS = "status";
		}
	}
}
