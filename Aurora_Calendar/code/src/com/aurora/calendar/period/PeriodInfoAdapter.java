package com.aurora.calendar.period;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class PeriodInfoAdapter {

	public static final String TABLE_NAME = "period_info";

	public static final String _ID = "id";
	public static final String START_DAY = "start_day";
	public static final String FINISH_DAY = "finish_day";

	public static final String[] COLUMNS = new String[] {
		_ID,
		START_DAY,
		FINISH_DAY
	};

	public static final int INDEX_ID = 0;
	public static final int INDEX_START_DAY = 1;
	public static final int INDEX_FINISH_DAY = 2;

	private Context context;
	private SQLiteDatabase db;

	public PeriodInfoAdapter(Context context) {
		this.context = context;
	}

	public void open() {
		PeriodDatabaseHelper databaseHelper = new PeriodDatabaseHelper(context);
		try {
			db = databaseHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			db = databaseHelper.getReadableDatabase();
		}
	}

	public void close() {
		if (db != null) {
			db.close();
			db = null;
		}
	}

	public long insert(PeriodInfo info) {
		ContentValues values = new ContentValues();
		values.put(START_DAY, info.getStartDay());
		values.put(FINISH_DAY, info.getFinishDay());

		return db.insert(TABLE_NAME, null, values);
	}

	public ArrayList<Long> insert(ArrayList<PeriodInfo> periodInfos) {
		ArrayList<Long> infoIds = new ArrayList<Long>();

		db.beginTransaction();
		try {
			for (PeriodInfo info : periodInfos) {
				infoIds.add(insert(info));
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}

		return infoIds;
	}

	public int update(PeriodInfo info) {
		ContentValues values = new ContentValues();
		values.put(START_DAY, info.getStartDay());
		values.put(FINISH_DAY, info.getFinishDay());

		String id = String.valueOf(info.getId());
		return db.update(TABLE_NAME, values, _ID + "=?", new String[] { id });
	}

	public int delete(int id) {
		return db.delete(TABLE_NAME, _ID + "=?", new String[] { String.valueOf(id) });
	}

	public void deleteAll() {
		db.delete(TABLE_NAME, null, null);
	}

	public ArrayList<PeriodInfo> queryAll() {
		Cursor cursor = db.query(TABLE_NAME, COLUMNS, null, null, null, null, START_DAY + " DESC");

		if (cursor == null || cursor.getCount() == 0) {
			return null;
		}

		ArrayList<PeriodInfo> periodInfos = new ArrayList<PeriodInfo>();

		cursor.moveToPosition(-1);
		while (cursor.moveToNext()) {
			PeriodInfo info = new PeriodInfo();
			info.setId(cursor.getInt(INDEX_ID));
			info.setStartDay(cursor.getInt(INDEX_START_DAY));
			info.setFinishDay(cursor.getInt(INDEX_FINISH_DAY));

			periodInfos.add(info);
		}

		if (cursor != null) {
			cursor.close();
			cursor = null;
		}

		return periodInfos;
	}

	public PeriodInfo queryByJulianDay(int julianDay) {
		String julianDayStr = String.valueOf(julianDay);
		Cursor cursor = db.query(TABLE_NAME, COLUMNS, 
				START_DAY + "<=? AND " + FINISH_DAY + ">=?", 
				new String[] { julianDayStr, julianDayStr}, null, null, null);

		if (cursor == null || !cursor.moveToFirst()) {
			return null;
		}

		PeriodInfo info = new PeriodInfo();
		info.setId(cursor.getInt(INDEX_ID));
		info.setStartDay(cursor.getInt(INDEX_START_DAY));
		info.setFinishDay(cursor.getInt(INDEX_FINISH_DAY));

		if (cursor != null) {
			cursor.close();
			cursor = null;
		}

		return info;
	}

}
