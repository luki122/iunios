package com.aurora.note.db;

import java.util.UUID;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter {

	public static final String DB_NAME = "aurora_note.db"; // 数据库名
	private static final int DB_VERSION = 4; // 数据库版本号

	/**
	 * 
	 * 静态Helper类，用于建立、更新和打开数据库
	 */
	public class DBOpenHelper extends SQLiteOpenHelper {
		/*
		 * 
		 * 手动创建表的SQL命令
		 */
		private static final String DB_CREATE_NOTE = "CREATE TABLE " + NoteAdapter.TABLE_NAME + " ("
				+ NoteAdapter.ID + " INTEGER primary key autoincrement, "
				+ NoteAdapter.IS_PRESET + " INTEGER not null, "
				+ NoteAdapter.BACKGROUND_PATH + " TEXT, "
				+ NoteAdapter.UUID + " TEXT not null, "
				+ NoteAdapter.CONTENT + " TEXT not null, "
				+ NoteAdapter.CHARACTER + " TEXT not null, "
				+ NoteAdapter.IMAGE_COUNT + " INTEGER not null, "
				+ NoteAdapter.VIDEO_COUNT +" INTEGER not null, "
				+ NoteAdapter.SOUND_COUNT +" INTEGER not null, "
				+ NoteAdapter.LABEL1 + " TEXT, "
				+ NoteAdapter.LABEL2 + " TEXT, "
				+ NoteAdapter.IS_WARN + " INTEGER not null, "
				+ NoteAdapter.WARN_TIME + " LONG, "
				+ NoteAdapter.CREATE_TIME + " LONG not null, "
				+ NoteAdapter.UPDATE_TIME + " LONG not null)";

        private static final String DB_CREATE_LABEL = "CREATE TABLE " + LabelAdapter.TABLE_NAME + " ("
                + LabelAdapter.ID + " INTEGER primary key autoincrement, "
                + LabelAdapter.CONTENT + " TEXT not null, "
                + LabelAdapter.IS_ENCRYPTED + " BOOLEAN NOT NULL DEFAULT 0, "
                + LabelAdapter.UPDATE_TIME + " LONG not null)";

        private static final String DB_CREATE_RECORDER = "CREATE TABLE " + RecorderAdapter.TABLE_NAME + " ("
                + RecorderAdapter.ID + " INTEGER primary key autoincrement, "
                + RecorderAdapter.PATH + " TEXT not null, "
                + RecorderAdapter.NAME + " TEXT not null, "
                + RecorderAdapter.MARK + " TEXT, "
                + RecorderAdapter.CREATE_TIME + " LONG not null, "
                + RecorderAdapter.DURATION + " LONG, "
                + RecorderAdapter.SAMPLERATE + " INTEGER)";

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
			db.execSQL(DB_CREATE_NOTE);
            db.execSQL(DB_CREATE_LABEL);
            db.execSQL(DB_CREATE_RECORDER);
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
		public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
			// 为了简单起见，并没有做任何的的数据转移，而仅仅删除原有的表后建立新的数据库表
            /*_db.execSQL("DROP TABLE IF EXISTS " + NoteAdapter.TABLE_NAME);
            _db.execSQL("DROP TABLE IF EXISTS " + LabelAdapter.TABLE_NAME);
            onCreate(_db);*/

			if (oldVersion == 1) {
				upgradeToVersion2(_db);
				oldVersion += 1;
			}
			if (oldVersion == 2) {
				upgradeToVersion3(_db);
				oldVersion += 1;
			}
			if (oldVersion == 3) {
			    upgradeToVersion4(_db);
                oldVersion += 1;
			}
		}

		private void upgradeToVersion4(SQLiteDatabase db) {
		    // 标签表加列is_encrypted表示该标签对应的备忘录是否加密
            db.execSQL("ALTER TABLE tbl_label ADD COLUMN is_encrypted BOOLEAN NOT NULL DEFAULT 0;");
            
            // 备忘录表加列background_path表示该条备忘录设置的背景图的资源路径
            db.execSQL("ALTER TABLE tbl_note ADD COLUMN background_path TEXT;");
        }

		private void upgradeToVersion3(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + NoteAdapter.TABLE_NAME + " ADD COLUMN " + NoteAdapter.IS_PRESET + 
					" INTEGER NOT NULL DEFAULT 0;");
			db.execSQL("ALTER TABLE " + NoteAdapter.TABLE_NAME + " ADD COLUMN " + NoteAdapter.UUID + " TEXT;");

			Cursor cursor = db.rawQuery("SELECT _id FROM " + NoteAdapter.TABLE_NAME, null);
			if (cursor != null) {
				try {
					while (cursor.moveToNext()) {
						int id = cursor.getInt(0);
						db.execSQL("UPDATE " + NoteAdapter.TABLE_NAME + " SET " + NoteAdapter.UUID + "='" +
								UUID.randomUUID().toString() + "' WHERE _id=" + id);
					}
				} finally {
					cursor.close();
				}
			}
		}

		private void upgradeToVersion2(SQLiteDatabase db) {
		    db.execSQL(DB_CREATE_RECORDER);
		}

	}

}