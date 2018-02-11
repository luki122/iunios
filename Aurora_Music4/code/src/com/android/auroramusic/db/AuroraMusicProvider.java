package com.android.auroramusic.db;



import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Path;
import android.net.Uri;
import android.util.Log;



public class AuroraMusicProvider extends ContentProvider{
	
	private static final String TAG = "AuroraMusicProvider";
	public static final String AUTHORITY = "com.android.music.AuroraMusicProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	private static Uri sBaseUri;
	private AuroraDbHelper mProDbHelper;
	
	
	public static String getAuthority(Context context) {
        return context.getPackageName() + ".AuroraMusicProvider";
    }
	
	public static Uri getUriFor(Context context, Path path) {
        if (sBaseUri == null) {
            sBaseUri = Uri.parse("content://" + context.getPackageName() + ".AuroraMusicProvider");
        }
        
        return sBaseUri.buildUpon()
                .appendEncodedPath(path.toString().substring(1)) // ignore the leading '/'
                .build();
    }
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		//删除全部数据
		SQLiteDatabase db = mProDbHelper.getWritableDatabase();
		try {
			db.delete(AuroraDbData.SHARE_TABLENAME, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			db.close();
		}
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mProDbHelper.getWritableDatabase();
		try {
			long rowid = db.insert(AuroraDbData.SHARE_TABLENAME, AuroraDbData.SHARE_ISPLAYING, values);
			if (rowid > 0) {
				Uri insertUri = ContentUris.withAppendedId(uri, rowid);
				getContext().getContentResolver().notifyChange(insertUri, null); 
				return insertUri;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		throw new IllegalArgumentException("Unkwon Uri:"+ uri.toString());
	}

	@Override
	public boolean onCreate() {
		this.mProDbHelper = new AuroraDbHelper(this.getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mProDbHelper.getReadableDatabase();
		
		Cursor cursor = null;
		try {
			cursor = db.query(AuroraDbData.SHARE_TABLENAME, projection, selection, selectionArgs, null, null, sortOrder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mProDbHelper.getWritableDatabase();
		int count = 0;
		try {
			long id = ContentUris.parseId(uri);   
	        String where = AuroraDbData.SHARE_ID +" = "+ id;   
	        if(selection!=null && !"".equals(selection)){   
	            where = selection + " and " + where;   
	        }
	        
	        count = db.update(AuroraDbData.SHARE_TABLENAME, values, where, selectionArgs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return count;
	}

}
