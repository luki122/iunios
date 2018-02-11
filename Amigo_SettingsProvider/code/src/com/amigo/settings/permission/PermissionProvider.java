package com.amigo.settings.permission;

// Gionee <liuyb> <2013-12-11> add for CR00964937 begin
import com.amigo.settings.utils.FileUtil;
import android.util.AndroidRuntimeException;
// Gionee <liuyb> <2013-12-11> add for CR00964937 end

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
import android.util.Log;

public class PermissionProvider extends ContentProvider {
    
	private static final String TAG = "PermissionProvider";
    
    private final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    // Gionee <liuyb> <2013-12-11> add for CR00964937 begin
    private final static String OLD_DATABASE_PATH = "/data/data/com.android.providers.settings/databases/permission.db";
    // Gionee <liuyb> <2013-12-11> add for CR00964937 end
    //private SQLiteDatabase mPushDb;
    private PermissionHelper mDbHelper;
    //uri matcher code
    // Gionee <liuyb> <2013-11-19> modify for CR00954879 begin
    private static final int CODE_PERM_APP_VND_DIR = 1;
    private static final int CODE_PERM_APP_VND_ITEM = 2;
    private static final int CODE_WHITE_LIST_VND_DIR = 3;
    private static final int CODE_WHITE_LIST_VND_ITEM = 4;
    
    @Override
    public boolean onCreate() {
    	
    	mUriMatcher.addURI(AUTHOURITY, PermissionApp.TABLE, CODE_PERM_APP_VND_DIR);
    	mUriMatcher.addURI(AUTHOURITY, PermissionApp.TABLE + "/#", CODE_PERM_APP_VND_ITEM);

    	mUriMatcher.addURI(AUTHOURITY, PermissionApp.WHITELIST_TABLE, CODE_WHITE_LIST_VND_DIR);
        mUriMatcher.addURI(AUTHOURITY, PermissionApp.WHITELIST_TABLE + "/#", CODE_WHITE_LIST_VND_ITEM);
    	
    	mDbHelper = new PermissionHelper(getContext());
    	
    	return true;
    }
    
    @Override
    public String getType(Uri uri) {
    	switch (mUriMatcher.match(uri)) {
	    	case CODE_PERM_APP_VND_DIR:
	    		return "vnd.android.cursor.dir/" + PermissionApp.TABLE;
	    	case CODE_PERM_APP_VND_ITEM:
                return "vnd.android.cursor.item/" + PermissionApp.TABLE;
            case CODE_WHITE_LIST_VND_DIR:
                return "vnd.android.cursor.dir/" + PermissionApp.WHITELIST_TABLE;
            case CODE_WHITE_LIST_VND_ITEM:
                return "vnd.android.cursor.item/" + PermissionApp.WHITELIST_TABLE;
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
                qb.setTables(PermissionApp.TABLE);
                break;

            case CODE_PERM_APP_VND_ITEM:
                qb.setTables(PermissionApp.TABLE);
                qb.appendWhere(PermissionApp.Column._ID + "=" + uri.getPathSegments().get(1));
                break;

            case CODE_WHITE_LIST_VND_DIR:
                qb.setTables(PermissionApp.WHITELIST_TABLE);
                break;

            case CODE_WHITE_LIST_VND_ITEM:
                qb.setTables(PermissionApp.WHITELIST_TABLE);
                qb.appendWhere(PermissionApp.Column._ID + "=" + uri.getPathSegments().get(1));
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
	    		rowId = mDbHelper.getWritableDatabase().insert(PermissionApp.TABLE, null, contentValues);
	    		contentUri = PermissionApp.CONTENT_URI;
	    		break;
	    	case CODE_WHITE_LIST_VND_DIR:
                rowId = mDbHelper.getWritableDatabase().insert(PermissionApp.WHITELIST_TABLE, null, contentValues);
                contentUri = PermissionApp.WHITELIST_CONTENT_URI;
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
	    	case CODE_PERM_APP_VND_DIR:
	    		count = db.delete(PermissionApp.TABLE, where, whereArgs);
	    		break;
	    	case CODE_PERM_APP_VND_ITEM:
	    		String id = uri.getPathSegments().get(1);
	    		count = db.delete(PermissionApp.TABLE, 
	    				PermissionApp.Column._ID + "=" + id 
	    					+ (!TextUtils.isEmpty(where) ? (" AND (" + where + ")") : ""), 
	    				whereArgs);
	    		break;
	    	case CODE_WHITE_LIST_VND_DIR:
                count = db.delete(PermissionApp.WHITELIST_TABLE, where, whereArgs);
                break;
            case CODE_WHITE_LIST_VND_ITEM:
                id = uri.getPathSegments().get(1);
                count = db.delete(PermissionApp.WHITELIST_TABLE, 
                        PermissionApp.Column._ID + "=" + id 
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
    		count = db.update(PermissionApp.TABLE, values, where, whereArgs);
    		break;
    	case CODE_PERM_APP_VND_ITEM:
    		String id = uri.getPathSegments().get(1);
    		count = db.update(PermissionApp.TABLE, values,
    		        PermissionApp.Column._ID + "=" + id 
    					+ (!TextUtils.isEmpty(where) ? (" AND (" + where + ")") : ""), 
    				whereArgs);
    		break;
    	case CODE_WHITE_LIST_VND_DIR:
            count = db.update(PermissionApp.WHITELIST_TABLE, values, where, whereArgs);
            break;
        case CODE_WHITE_LIST_VND_ITEM:
            id = uri.getPathSegments().get(1);
            count = db.update(PermissionApp.WHITELIST_TABLE, values,
                    PermissionApp.Column._ID + "=" + id 
                        + (!TextUtils.isEmpty(where) ? (" AND (" + where + ")") : ""), 
                    whereArgs);
    		break;
    		    		
    	default:
    		throw new IllegalArgumentException("Unsupport URI: " + uri);
    	}
    	
    	getContext().getContentResolver().notifyChange(uri, null);
    	
    	return count;
    }
    
    
    private class PermissionHelper extends SQLiteOpenHelper {

    	private static final String TAG = "PermissionHelper";
    	
    	//the underlying database
    	public static final int DATABASE_VERSION = 2;
    	public static final String DATABASE_NAME = "permission.db";
    	
        public PermissionHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
    	@Override
        public void onCreate(SQLiteDatabase db) {                 
            db.execSQL(CREATE_TABLE_PERMISSION);
            db.execSQL(CREATE_WHITELIST_TABLE);
            // Gionee <liuyb> <2013-12-11> add for CR00964937 begin
            copyOldProviderData(db);
            // Gionee <liuyb> <2013-12-11> add for CR00964937 end
        }
    	
    	@Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == 1 && newVersion == 2) {
                db.execSQL(CREATE_WHITELIST_TABLE);
            }
        }
    	
    	private final String CREATE_TABLE_PERMISSION = "create table " + PermissionApp.TABLE + " ("
    			+ PermissionApp.Column._ID + " integer primary key autoincrement,"
    			+ PermissionApp.Column.PACKAGE_NAME + " TEXT,"
    			+ PermissionApp.Column.PERMISSION + " TEXT,"
                        + PermissionApp.Column.PERMISSION_GROUP +" TEXT,"
    			+ PermissionApp.Column.STATUS + " INTEGER"
    			+ " );";
    	
    	private final String CREATE_WHITELIST_TABLE = "create table " + PermissionApp.WHITELIST_TABLE + " ("
                + PermissionApp.Column._ID + " integer primary key autoincrement,"
                + PermissionApp.Column.PACKAGE_NAME + " TEXT,"
                + PermissionApp.Column.STATUS + " INTEGER"
                + " );";
    	
    }
    
    //------------------------------------------------------------//
    //!!!!!!!!!!!!!!!!ATTENTION PLEASE!!!!!!!!!!!!!!!!!
    //IF YOU DECIDE TO MODIFY BELOW STUFF,YOU SHOULD MODIFY A COPY
    //FILE WHICH IS PLACED IN APP SETTINGS FOR SYNC,KEEP IT IN MIND
    //------------------------------------------------------------//
    
    private static final String AUTHOURITY = "com.amigo.settings.PermissionProvider";
    
	private static class PermissionApp {
		
		public static final String TABLE = "permissions";
		
		public static final String WHITELIST_TABLE = "whitelist";
		
		public static final Uri CONTENT_URI = Uri.parse("content://" 
				+ AUTHOURITY + "/"+ TABLE);
		
		public static final Uri WHITELIST_CONTENT_URI = Uri.parse("content://" 
                + AUTHOURITY + "/"+ WHITELIST_TABLE);
		
		public static class Column{
			public static final String _ID = "_id";
			public static final String PACKAGE_NAME = "packagename";
			public static final String PERMISSION = "permission";
                        public static final String PERMISSION_GROUP = "permissiongroup";
			public static final String STATUS = "status";
		}
	}
	
	// Gionee <liuyb> <2013-11-19> modify for CR00954879 end
	
	// Gionee <liuyb> <2013-12-11> add for CR00964937 begin
    private void copyOldProviderData(SQLiteDatabase db) {
        SQLiteDatabase oldDB;
        if (!FileUtil.isExists(OLD_DATABASE_PATH)) {
            Log.v(TAG, "/data/data/com.android.providers.settings/databases/permission.db  not Exists");
            return;
        }
        oldDB = SQLiteDatabase.openDatabase(OLD_DATABASE_PATH, null, SQLiteDatabase.OPEN_READONLY);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(PermissionApp.TABLE);
        Cursor c = null;
        try {
            c = qb.query(oldDB, null, null, null, null, null, null);
            if (c != null && c.moveToFirst()) {
                do {
                    String mPackage = c.getString(c.getColumnIndex(PermissionApp.Column.PACKAGE_NAME));
                    String permission = c.getString(c.getColumnIndex(PermissionApp.Column.PERMISSION));
                    String group = c.getString(c.getColumnIndex(PermissionApp.Column.PERMISSION_GROUP));
                    String status = c.getString(c.getColumnIndex(PermissionApp.Column.STATUS));

                    ContentValues cv = new ContentValues();
                    cv.put(PermissionApp.Column.PACKAGE_NAME, mPackage);
                    cv.put(PermissionApp.Column.PERMISSION, permission);
                    cv.put(PermissionApp.Column.PERMISSION_GROUP, group);
                    cv.put(PermissionApp.Column.STATUS, status);

                    db.insert(PermissionApp.TABLE, null, cv);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        oldDB.close();
    }
    // Gionee <liuyb> <2013-12-11> add for CR00964937 end

}
