package com.android.settings.lscreen;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LSDataSqlite extends SQLiteOpenHelper {
	private static final String DATABASE_NAME="LScreenOpenData.db";
	
	private static final int DATABASE_VERSION=2;
	
	public static final String TABLE_NAME_LSCREEN_APP="LScreenApp";
	
	public static final String FIELD_ID="_id";
	
	public static final String PACKAGE_NAME="packageName";
	
	private static final String SQL_CREATE="CREATE TABLE " + TABLE_NAME_LSCREEN_APP + 
      		" ("+FIELD_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+ 
      		PACKAGE_NAME +" TEXT);";
	
	
	public LSDataSqlite(Context mContext) {
		super(mContext, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_LSCREEN_APP);
		onCreate(db);
	}

}
