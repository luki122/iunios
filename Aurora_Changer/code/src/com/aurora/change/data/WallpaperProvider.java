package com.aurora.change.data;

import com.aurora.change.model.DbInfoModel;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class WallpaperProvider extends ContentProvider{
	
	private DbHelper Db = null;
	
	private static final String[] LOCAL_WALLPAPER_COLUMNS = {
		WallpaperValue.WALLPAPER_ID,
		WallpaperValue.WALLPAPER_MODIFIED,
		WallpaperValue.WALLPAPER_OLDPATH,
		WallpaperValue.WALLPAPER_FILENAME
	};
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int res = Db.mSqlDb.delete(DbInfoModel.DESKTOP_WALLPAPER_TABLE_NAME, where, whereArgs);
		if (res > 0) {
			notifyChange(uri);
		}
		return res;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return WallpaperValue.WALLPAPER_URI_TYPE;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId = Db.mSqlDb.insert(DbInfoModel.DESKTOP_WALLPAPER_TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri resUri = ContentUris.withAppendedId(uri, rowId);
			notifyChange(resUri);
			return resUri;
		}
		return null;
	}

	@Override
	public boolean onCreate() {
		Db = new DbHelper(getContext());
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor cursor = Db.mSqlDb.query(DbInfoModel.DESKTOP_WALLPAPER_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
		if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int res = Db.mSqlDb.update(DbInfoModel.DESKTOP_WALLPAPER_TABLE_NAME, values, selection, selectionArgs);
		if (res > 0) {
			notifyChange(uri);
		}
		return res;
	}
	
	private void notifyChange(Uri uri) {
		getContext().getContentResolver().notifyChange(uri, null);
	}
}
