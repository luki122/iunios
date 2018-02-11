package com.aurora.reminder;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

public class ReminderProvider extends ContentProvider {

	private Context mContext;

	private ReminderDbHelper mDbHelper;
	private SQLiteDatabase mDb;

	@Override
	public boolean onCreate() {
		mContext = getContext();
		mDbHelper = ReminderDbHelper.getInstance(mContext);
		mDb = mDbHelper.getWritableDatabase();

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		return mDb.query(ReminderUtils.TABLE_NAME, projection, selection, selectionArgs, 
				null, null, sortOrder);
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (!values.containsKey(ReminderUtils.TITLE) || 
				!values.containsKey(ReminderUtils.PACKAGE)) return null;

		String packageName = values.getAsString(ReminderUtils.PACKAGE);
		if (TextUtils.isEmpty(packageName)) return null;

		Cursor cursor = mDb.rawQuery("SELECT count(*) FROM " + ReminderUtils.TABLE_NAME + 
				" WHERE " + ReminderUtils.PACKAGE + "=?", new String[] { packageName });
		int count = 0;
		if (cursor != null && cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}

		int level = 0;
		if (ReminderUtils.PACKAGE_OWN.equals(packageName)) {
			level = 3;
		} else if (ReminderUtils.PACKAGE_PHONE.equals(packageName)) {
			level = 1;
		} else if (ReminderUtils.PACKAGE_WEATHER.equals(packageName)) {
			level = 3;
		} else if (ReminderUtils.PACKAGE_CALENDAR.equals(packageName)) {
			level = 2;
		}
		values.put(ReminderUtils.LEVEL, level);

		if (!values.containsKey(ReminderUtils.VISIBLE)) {
			values.put(ReminderUtils.VISIBLE, 1);
		}

		if (count == 0) {
			mDb.insert(ReminderUtils.TABLE_NAME, null, values);
		} else {
			mDb.update(ReminderUtils.TABLE_NAME, values, 
					ReminderUtils.PACKAGE + "=?", new String[] { packageName });
		}

		mContext.sendBroadcast(new Intent(ReminderWidgetService.ACTION_PROVIDER_CHANGED));
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count =  mDb.delete(ReminderUtils.TABLE_NAME, selection, selectionArgs);
		mContext.sendBroadcast(new Intent(ReminderWidgetService.ACTION_PROVIDER_CHANGED));
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count =  mDb.update(ReminderUtils.TABLE_NAME, values, selection, selectionArgs);
		mContext.sendBroadcast(new Intent(ReminderWidgetService.ACTION_PROVIDER_CHANGED));
		return count;
	}

}
