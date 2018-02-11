package com.aurora.commemoration.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RememberDayDBHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "rememberDayDB"; // 数据库名
	public static final int DATABASE_VERSION = 2; // 数据库版本

	public static final String REMEMBER_DAY_TABLE = "rememberDayTable";

	public static final String ID = "_id";  // 纪念日id
	public static final String TITLE = "dayTitle";  // 纪念日标题
	public static final String DAY = "day"; // 纪念日日期
	public static final String MILL_TIME = "millTime";
	public static final String REMINDER_DATA = "reminderData"; // 提醒数据
	public static final String PIC_PATH = "picPath"; // 背景图路径
	public static final String SCHEDULE_FLAG = "schedulFlag"; // 日程标记

	public RememberDayDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS " + REMEMBER_DAY_TABLE + " (");
		sb.append(ID + " INTEGER PRIMARY KEY AUTOINCREMENT,");                      //0
		sb.append(TITLE + " VARCHAR(100),");                                        //1
		sb.append(PIC_PATH + " VARCHAR(50),");                                      //2
		sb.append(DAY + " VARCHAR(24),");                                           //3
		sb.append(REMINDER_DATA + " LONG DEFAULT 0, ");       			    				//4
		sb.append(SCHEDULE_FLAG + " INTEGER DEFAULT 0,");                           //5
		sb.append(MILL_TIME + " TEXT)");											//6
		db.execSQL(sb.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + REMEMBER_DAY_TABLE);
		onCreate(db);
	}

}
