package com.aurora.calendar.period;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PeriodDatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "period.db";
	private static final int DATABASE_VERSION = 1;

	private static final String CREATE_TABLE_PERIOD_INFO = "CREATE TABLE " + PeriodInfoAdapter.TABLE_NAME + " (" +
			PeriodInfoAdapter._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			PeriodInfoAdapter.START_DAY + " INTEGER NOT NULL DEFAULT 0," +
			PeriodInfoAdapter.FINISH_DAY + " INTEGER NOT NULL DEFAULT 0" + ");";

	public PeriodDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_PERIOD_INFO);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		
	}

}
