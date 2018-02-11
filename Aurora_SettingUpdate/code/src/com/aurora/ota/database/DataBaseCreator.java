package com.aurora.ota.database;

import gn.com.android.update.utils.LogUtils;

import java.util.HashSet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseCreator extends SQLiteOpenHelper{
	private static final String TAG = "DataBaseCreator";
	public static final String DB_NAME = "repoter.db";
	private static final int VERSION =9;

	public interface Tables{
	    public static final String DB_TABLE = "repoter";
	    public static final String DB_MODULE_TABLE = "module";
	}
	private static DataBaseCreator sSingleton = null;
	
	private static final HashSet<String> mValidTables = new HashSet<String>();

//	{"apkVersion":"apk20130903","appName":"WJQ APP",
//		"channelName":"Ӧ�û�","imei":"ubXyyGU5Ywg8Go4fJ77VPw==","" +
//				"mobileModel":"HTC","mobileNumber":"4WwAjkZA3mXip36ddmhFGw==",
//				"registerUserId":"afs31232345432","shutdownTime":1382066411016,
//				"startupTime":1382066411016,"status":1}
	public interface RepoterColumns extends ReporterKey{
		public static final String ID = "_id";
	}
	private static final String CREATE_TABLE = "CREATE TABLE " +Tables.DB_TABLE + " (" +
			RepoterColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			RepoterColumns.KEY_APP_VERSION + " TEXT," +
			RepoterColumns.KEY_APP_NAME + " TEXT," +
			RepoterColumns.KEY_IMEI + " TEXT," +
			RepoterColumns.KEY_CHANEL + " TEXT," +
			RepoterColumns.KEY_MOBILE_MODEL + " TEXT," +
			RepoterColumns.KEY_MOBILE_NUMBER + " TEXT," +
			RepoterColumns.KEY_REGISTER_USER_ID + " TEXT," +
			RepoterColumns.KEY_SHUT_DOWN_TIME + " TEXT," +
			RepoterColumns.KEY_CREATE_ITEM_TIME + " TEXT," +
			RepoterColumns.KEY_STATUS + " INTEGER NOT NULL DEFAULT 0," +
			RepoterColumns.KEY_PHONE_SIZE +" TEXT,"+
			RepoterColumns.KEY_LOCATION + " TEXT," +
			RepoterColumns.KEY_REPORTED + " INTEGER NOT NULL DEFAULT 0," +
			RepoterColumns.KEY_APP_NUM + " INTEGER NOT NULL DEFAULT 0," +
			RepoterColumns.KEY_BOOT_TIME + " TEXT," +
			RepoterColumns.KEY_DURATION_TIME + " TEXT"+
    ");";
	
	private static final String CREATE_MODULE_TABLE = "CREATE TABLE " +Tables.DB_MODULE_TABLE + " (" +
			RepoterColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			RepoterColumns.KEY_MODULE + " TEXT," +
			RepoterColumns.KEY_MODULE_ITEM + " TEXT," +
			RepoterColumns.KEY_VALUE + " INTEGER NOT NULL DEFAULT 0" +
			/*"unique(`module_key`,`item_tag`)" +*/
    ");";
	
	static {
        mValidTables.add(Tables.DB_TABLE);
        mValidTables.add(Tables.DB_MODULE_TABLE);
    }
	public static synchronized DataBaseCreator getInstance(Context context) {
		if (sSingleton == null) {
			sSingleton = new DataBaseCreator(context, DB_NAME, true);
		}
		return sSingleton;
	}

	protected DataBaseCreator(Context context, String databaseName,
			boolean optimizationEnabled) {
		super(context, DB_NAME, null, VERSION);
		LogUtils.log(TAG, "DataBaseCreator DataBaseCreator ");
	}

	public SQLiteDatabase getDatabase(boolean writable) {
		return writable ? getWritableDatabase() : getReadableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		LogUtils.log(TAG, "DataBaseCreator onCreate ");
		try{
		db.execSQL(CREATE_TABLE);
		db.execSQL(CREATE_MODULE_TABLE);
		}catch(Exception e){
			LogUtils.log(TAG, "DataBaseCreator onCreate exception is  "  + e.getMessage());
		}
	}
	
    public static boolean isValidTable(String name) {
        return mValidTables.contains(name);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + Tables.DB_TABLE + ";");
		db.execSQL("DROP TABLE IF EXISTS " + Tables.DB_MODULE_TABLE + ";");
		onCreate(db);
		LogUtils.log(TAG, "DataBaseCreator onUpgrade ");
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + Tables.DB_TABLE + ";");
		db.execSQL("DROP TABLE IF EXISTS " + Tables.DB_MODULE_TABLE + ";");
		onCreate(db);
		LogUtils.log(TAG, "DataBaseCreator onUpgrade ");
	}

	
}
