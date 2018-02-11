package com.aurora.weatherdata.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter {

	public static final String DB_NAME = "weather_cache.db"; // 数据库名
	private static final int DB_VERSION = 1; // 数据库版本号

	/**
	 * 
	 * 静态Helper类，用于建立、更新和打开数据库
	 */
	public class DBOpenHelper extends SQLiteOpenHelper {
		/*
		 * 
		 * 手动创建表的SQL命令
		 */

		// 缓存数据表
		private static final String DB_CREATE_CACHE = "CREATE TABLE "
				+ CacheDataAdapter.TABLE_NAME + " ("
				+ CacheDataAdapter.CITY_ID + " TEXT not null, "
				+ CacheDataAdapter.CITY_NAME + " TEXT, "
				+ CacheDataAdapter.WEATHER_DATA	+ " TEXT not null, " 
				+ CacheDataAdapter.UPDATE_TIME	+ " LONG not null) ";

		public Context mContext;

		public DBOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			mContext = context;
		}

		/*
		 * 
		 * 函数在数据库第一次建立时被调用，
		 * 
		 * 一般用来用来创建数据库中的表，并做适当的初始化工作
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DB_CREATE_CACHE);
		}

		/*
		 * 
		 * SQL命令。onUpgrade()函数在数据库需要升级时被调用，
		 * 
		 * 通过调用SQLiteDatabase对象的execSQL()方法，
		 * 
		 * 执行创建表的一般用来删除旧的数据库表，并将数据转移到新版本的数据库表中
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// 为了简单起见，并没有做任何的的数据转移，而仅仅删除原有的表后建立新的数据库表

			db.execSQL("DROP TABLE IF EXISTS " + CacheDataAdapter.TABLE_NAME);

			onCreate(db);
		}
	}

}