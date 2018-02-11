package com.aurora.thememanager.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LoadedThemeDbHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "loaded_theme"; // 数据库名
	public static final int DATABASE_VERSION = 4; // 数据库版本

	public static final String TABLE = "loaded_themes";

	public static final String THEME_NAME = "theme_name"; // 名称
	public static final String THEME_ID = "theme_id"; // themeId
	public static final String VERSION = "version"; // 版本号
	public static final String SAVE_PATH = "path"; // 路径
	public static final String AUTHOR = "author";
	public static final String TYPE="type";
	public static final String SIZE="sizeStr";
	public static final String DESC = "desc";
	public LoadedThemeDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS " + TABLE + " (");
		sb.append(THEME_ID + " INTEGER,");
		sb.append(THEME_NAME + " VARCHAR(100),");
		sb.append(VERSION + " VARCHAR(50),");
		sb.append(SAVE_PATH + " VARCHAR(150),");
		sb.append(AUTHOR + " VARCHAR(150),");
		sb.append(TYPE + " VARCHAR(150),");
		sb.append(SIZE + " VARCHAR(150),");
		sb.append(DESC + " VARCHAR(150)"+")");
		db.execSQL(sb.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE);
		onCreate(db);
	}

}
