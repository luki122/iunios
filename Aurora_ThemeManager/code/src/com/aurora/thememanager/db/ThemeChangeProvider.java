package com.aurora.thememanager.db;



import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ThemeChangeProvider extends ContentProvider {
	private static final String DATABASE_NAME = "themeManager.db";
	public static final String TAG = ThemeChangeProvider.class.getSimpleName();
	private static final int DATABASE_VERSION = 1;
	protected static final String PARAMETER_CHANGE = "changed";
	private static DatabaseHelper sOpenHelper;
	private Context mContext;
	@Override
	public boolean onCreate() {
		mContext = getContext();
		sOpenHelper = new DatabaseHelper(mContext);
		return true;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
		SQLiteDatabase db = sOpenHelper.getWritableDatabase();
		int count = db.delete(args.table, args.where, args.args);
		if (count > 0) {
			notifyChanged(uri);
		}
		return count;
	}

	@Override
	public String getType(Uri uri) {
		SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		SqlArguments args = new SqlArguments(uri);
        SQLiteDatabase db = sOpenHelper.getWritableDatabase();
        final long rowId = db.insert(args.table, null, initialValues);;
        if (rowId <= 0) {
            return null;
        }
        uri = ContentUris.withAppendedId(uri, rowId);
        notifyChanged(uri);

        return uri;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(args.table);
		SQLiteDatabase db = sOpenHelper.getWritableDatabase();
		Cursor result = qb.query(db, projection, args.where, args.args, null,
				null, sortOrder);
		if (result != null)
			result.setNotificationUri(getContext().getContentResolver(), uri);
		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteDatabase db = sOpenHelper.getWritableDatabase();
        int count = db.update(args.table, values, args.where, args.args);
        if (count > 0) {
            notifyChanged(uri);
        }

		return count;
	}
	
	private void notifyChanged(Uri uri) {
        String changed = uri.getQueryParameter(PARAMETER_CHANGE);
        if (changed == null || "true".equals(changed)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }
	
	
	
	
	
	
	
	class DatabaseHelper extends SQLiteOpenHelper{
		private Context mContext;
		private long mMaxId = -1;
	    DatabaseHelper(Context context){
	    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    	mContext = context;	
	    	if (mMaxId == -1) {
                mMaxId = initializeMaxId(getWritableDatabase());
				 Log.d(TAG, "DatabaseHelper_mMaxId=**"+mMaxId);
            }
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE themeManager (" + "_id INTEGER PRIMARY KEY,"
                    + "theme integer  DEFAULT 0" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
		
		private long initializeMaxId(SQLiteDatabase db) {
            Cursor c = db.rawQuery("SELECT MAX(_id) FROM themeManager", null);
            // get the result
            final int maxIdIndex = 0;
            long id = -1;
            if (c != null && c.moveToNext()) {
                id = c.getLong(maxIdIndex);
            }
            if (c != null) {
                c.close();
            }
            if (id == -1) {
                throw new RuntimeException("Error: could not query max id");
            }
            return id;
        }
		
	}
	
	
	
	
	static class SqlArguments {
        public final String table;
        public final String where;
        public final String[] args;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException(
                        "WHERE clause not supported: " + url);
            } else {
                this.table = url.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(url);
                this.args = null;
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }
	
	
	
	
	
	
	

}
