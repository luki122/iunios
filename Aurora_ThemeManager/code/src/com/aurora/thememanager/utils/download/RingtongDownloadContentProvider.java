package com.aurora.thememanager.utils.download;

import com.aurora.thememanager.utils.download.DatabaseController.DbHelper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class RingtongDownloadContentProvider extends ContentProvider{
	
	public static final String AUTHORITIES = "com.aurora.thememanager.ringtoneprovider";

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub

		Cursor cursor = DatabaseController.getController(getContext(), DatabaseController.TYPE_RINGTONG_DOWNLOAD).openDatabase().
				query(DbHelper.DOWNLOAD_RINGTON_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
