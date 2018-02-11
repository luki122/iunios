package com.aurora.account.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ExtraFileDbHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "extrafile.db"; // 数据库名
	public static final int DATABASE_VERSION = 1; // 数据库版本

	public ExtraFileDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// 下载数据表
	private static final String DB_CREATE_DOWNLOAD = "CREATE TABLE "
			+ ExtraFileDownloadDao.TABLE_NAME + " ("
			+ ExtraFileDownloadDao.DOWN_PATH + " TEXT, "
			+ ExtraFileDownloadDao.DOWN_SYNC_ID + " INTEGER, "
			+ ExtraFileDownloadDao.DOWN_PACKAGE_NAME + " TEXT, "
			+ ExtraFileDownloadDao.DOWN_URL + " TEXT, "
			+ ExtraFileDownloadDao.DOWN_STATUS + " TEXT, "
			+ ExtraFileDownloadDao.DOWN_CREATE_TIME + " LONG, "
			+ ExtraFileDownloadDao.DOWN_FINISH_TIME + " TEXT, "
			+ ExtraFileDownloadDao.DOWN_FILE_SIZE + " TEXT, "
			+ ExtraFileDownloadDao.DOWN_DOWNLOAD_SIZE + " TEXT DEFAULT  '0') ";

	// 上传数据表
	private static final String DB_CREATE_UPLOAD = "CREATE TABLE "
			+ ExtraFileUploadDao.TABLE_NAME + " ("
			+ ExtraFileUploadDao.UPLOAD_ACCESSORY_ID + " TEXT, "
			+ ExtraFileUploadDao.UPLOAD_SYNC_ID + " TEXT, "
			+ ExtraFileUploadDao.UPLOAD_PACKAGE_NAME + " TEXT, "
			+ ExtraFileUploadDao.UPLOAD_FILE_PATH + " TEXT, "
			+ ExtraFileUploadDao.UPLOAD_STATUS + " INTEGER, "
			+ ExtraFileUploadDao.UPLOAD_CREATE_TIME + " TEXT, "
			+ ExtraFileUploadDao.UPLOAD_FINISH_TIME + " TEXT, "
			+ ExtraFileUploadDao.UPLOAD_FILE_SIZE + "  TEXT DEFAULT  '0', "
			+ ExtraFileUploadDao.UPLOAD_SIZE + " TEXT DEFAULT  '0') ";

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 创建附件下载表
		db.execSQL(DB_CREATE_DOWNLOAD);

		// 创建附件上传表
		db.execSQL(DB_CREATE_UPLOAD);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + ExtraFileDownloadDao.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ExtraFileUploadDao.TABLE_NAME);
		onCreate(db);
	}

}
