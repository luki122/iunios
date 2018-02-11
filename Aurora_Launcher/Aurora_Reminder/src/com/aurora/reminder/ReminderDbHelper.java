package com.aurora.reminder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ReminderDbHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "aurora_reminder.db";
	private static final int DATABASE_VERSION = 1;

	private static final String SQL_CREATE_TABLE = "CREATE TABLE " + ReminderUtils.TABLE_NAME + " (" +
			ReminderUtils.ID + " INTEGER PRIMARY KEY," +
			ReminderUtils.TITLE + " TEXT," +
			ReminderUtils.ACTION + " TEXT," +
			ReminderUtils.PACKAGE + " TEXT," +
			ReminderUtils.LEVEL + " INTEGER," +
			ReminderUtils.VISIBLE + " INTEGER" +
			");";

	private static ReminderDbHelper sSingleton = null;

	public static ReminderDbHelper getInstance(Context context) {
		if (sSingleton == null) {
			sSingleton = new ReminderDbHelper(context);
		}
		return sSingleton;
	}

	ReminderDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		super.onDowngrade(db, oldVersion, newVersion);
	}

}
