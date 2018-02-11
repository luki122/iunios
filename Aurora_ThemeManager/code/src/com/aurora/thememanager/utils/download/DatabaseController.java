package com.aurora.thememanager.utils.download;



import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public abstract class DatabaseController {
	
	/**
	 * 根据该标志去创建下载器的数据库操作类
	 */
	public static final int TYPE_DOWNLOAD = 0;
	/**
	 * 根据该标志去创建更新器的数据库操作类
	 */
	public static final int TYPE_AUTO_UPDATE = 1;
	
	public static final int TYPE_WALLPAPER_DOWNLOAD = 2;
	
	public static final int TYPE_TIME_WALLPAPER_DOWNLOAD = 3;
	
	public static final int TYPE_RINGTONG_DOWNLOAD = 4;
	
	public static final int TYPE_THEME_PKG_UPDATE = 5;
	
	private SQLiteDatabase mDb;
	
	private DbHelper mDatabaseHelper;
	
	private Object mLock = new Object();
	
	protected String mTableName = DbHelper.DOWNLOAD_TABLE;

	public DatabaseController(Context context) {
		mDatabaseHelper = new DbHelper(context);
	}
	
	public DatabaseController(Context context, String tableName) {
		mTableName = tableName;
		mDatabaseHelper = new DbHelper(context);
	}

	/**
	 * 打开数据库
	 * @return 
	 */
	public SQLiteDatabase openDatabase() {
		synchronized (mLock) {
			mDb = mDatabaseHelper.getWritableDatabase();
		}
		return mDb;
	}

	/**
	 * 关闭数据库
	 */
	public void closeDatabase() {
		synchronized (mLock) {
			if (mDb != null && mDb.isOpen()) {
				mDb.close();
			}
		}
	}
	/**
	 * 判断数据库是否已经打开
	 * @return
	 */
	public boolean databaseIsOpened(){
		return mDb != null && mDb.isOpen();
	}
	
	/**
	 * 往数据库中插入一条下载任务
	 * @param data
	 * 				需要下载的数据对象
	 * @param createTime
	 * 				下载任务创建的时间
	 * @param status
	 * 				下载任务的下载状态
	 * @param filesize
	 * 				下载任务对于的文件总大小
	 */
	public abstract void  insert(DownloadData data, long createTime, long status,long filesize) ;
	
	public void insert(ContentValues values){
		mDb.insert(mTableName, null, values);
	}
	
	
	
	
	/**
	 * 判断是否已存在数据
	 * 
	 * @param appId
	 * @return
	 */
	public boolean exists(int downloadDataId) {
		synchronized (mLock) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			Cursor cursor = mDb.query(mTableName,
					new String[] { DbHelper.DOWNLOAD_DATA_ID },
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadDataId + "" }, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.close();
				return true;
			}
			cursor.close();
		}
		return false;
		}
	}
	
	
	public boolean hasNewVersion(int downloadDataId){
		
		return false;
	}
	
	public void updateNewVersionState(int downloadId,int state){
		
	}
	

	/**
	 * 删除数据库相应记录
	 * 
	 * @param appId
	 */
	public void delete(int downloadDataId) {
		synchronized (mLock) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			mDb.delete(mTableName,
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadDataId + "" });
		}
		}
	}



	/**
	 * 获取创建时间
	 * 
	 * @param appId
	 * @return
	 */
	public long getCreateTime(int downloadDataId) {
		synchronized (mLock) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			Cursor cursor = mDb.query(mTableName,
					new String[] { DbHelper.CREATE_TIME },
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadDataId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				long createTime = Long.parseLong(cursor.getString(0));
				cursor.close();
				return createTime;
			}
			cursor.close();
		}
		return 0;
		}
	}

	/**
	 * 更新状态
	 * 
	 * @param appId
	 * @param status
	 */
	public void updateStatus(int downloadDataId, int status) {
		synchronized (mLock) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			ContentValues values = new ContentValues();
			values.put(DbHelper.STATUS, status);
			mDb.update(mTableName, values,
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadDataId + "" });
		}
		}
	}

	/**
	 * 获取状态信息
	 * 
	 * @param appId
	 * @return
	 */
	public int getStatus(int downloadDataId) {
		synchronized (mLock) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			Cursor cursor = mDb.query(mTableName,
					new String[] { DbHelper.STATUS },
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadDataId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				int status = cursor.getInt(0);
				cursor.close();
				return status;
			}
			cursor.close();
		}
		return FileDownloader.STATUS_DEFAULT;
		}
	}

	/**
	 * 更新文件大小
	 * 
	 * @param appId
	 * @param fileSize
	 */
	public void updateFileSize(int downloadDataId, long fileSize) {
		synchronized (mLock) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			ContentValues values = new ContentValues();
			values.put(DbHelper.FILE_SIZE, fileSize);
			mDb.update(mTableName, values,
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadDataId + "" });
		}
		}
	}

	/**
	 * 获取文件大小
	 * 
	 * @param appId
	 * @return
	 */
	public long getFileSize(int downloadDataId) {
		synchronized (mLock) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			Cursor cursor = mDb.query(mTableName,
					new String[] { DbHelper.FILE_SIZE },
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadDataId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				long size = cursor.getLong(0);
				cursor.close();
				return size;
			}
			cursor.close();
		}
		return 0;
		}
	}

	/**
	 * 获取已文件下载大小
	 * 
	 * @param appId
	 * @return
	 */
	public long getDownloadSize(int downloadDataId) {
		{
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			Cursor cursor = mDb.query(mTableName,
					new String[] { DbHelper.DOWN_LENGTH },
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadDataId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				long size = cursor.getLong(0);
				cursor.close();
				return size;
			}
			cursor.close();
		}
		return 0;
		}
	}
	
	/**
	 * 获取已文件下载大小
	 * 
	 * @param appId
	 * @param fileSize
	 */
	public void updateDownloadSize(int downloadDataId, long downloadSize) {
		synchronized (mLock) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			ContentValues values = new ContentValues();
			values.put(DbHelper.DOWN_LENGTH, downloadSize);
			mDb.update(mTableName, values,
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadDataId + "" });
		}
		}
	}

	/**
	 * 更新文件存放名称
	 * 
	 * @param appId
	 * @param fileName
	 */
	public void updateFileName(int downloadDataId, String fileName) {
		synchronized (mLock) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			ContentValues values = new ContentValues();
			values.put(DbHelper.FILE_NAME, fileName);
			mDb.update(mTableName, values,
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadDataId + "" });
		}
		}
	}

	/**
	 * 更新文件存放目录及名称
	 * 
	 * @param appId
	 * @param dir
	 * @param fileName
	 */
	public void updateFileDirAndName(int downloadDataId, String dir, String fileName) {
		synchronized (mLock) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			ContentValues values = new ContentValues();
			values.put(DbHelper.FILE_DIR, dir);
			values.put(DbHelper.FILE_NAME, fileName);
			mDb.update(mTableName, values,
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadDataId + "" });
		}
		}
	}

	/**
	 * 获取文件存放名称
	 * 
	 * @param appId
	 * @return
	 */
	public String getFileName(int downloadDataId) {
		synchronized (mLock) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			Cursor cursor = mDb.query(mTableName,
					new String[] { DbHelper.FILE_NAME },
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadDataId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String fileName = cursor.getString(0);
				cursor.close();
				return fileName;
			}
			cursor.close();
		}
		return "";
		}
	}

	/**
	 * 获取文件存放目录
	 * 
	 * @param appId
	 * @return
	 */
	public String getFileDir(int downloadDataId) {
		synchronized (mLock) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			Cursor cursor = mDb.query(mTableName,
					new String[] { DbHelper.FILE_DIR },
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadDataId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String dir = cursor.getString(0);
				cursor.close();
				return dir;
			}
			cursor.close();
		}
		return "";
		}
	}
	
	/**
	 * 更新任务完成时间
	 * 
	 * @param appId
	 * @param dir
	 * @param fileName
	 */
	public void updateFileFinishTime(int downloadDataId, long finishTime) {
		synchronized (mLock) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			ContentValues values = new ContentValues();
			values.put(DbHelper.FINISH_TIME, finishTime);
			mDb.update(mTableName, values,
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadDataId + "" });
		}
		}
	}

	/**
	 * 获取数据库中所有appId
	 * 
	 * @return
	 */
	public List<Integer> getAllId() {
		synchronized (mLock) {
		List<Integer> list = new ArrayList<Integer>();
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			Cursor cursor = mDb.query(mTableName,
					new String[] { DbHelper.DOWNLOAD_DATA_ID }, null, null,
					null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					list.add(cursor.getInt(0));
				}
			}
			cursor.close();
		}

		// 删除重复元素
		HashSet<Integer> h = new HashSet<Integer>(list);
		list.clear();
		list.addAll(h);

		return list;
		}
	}

	/**
	 * 返回一个DownloadData对象的cursor, 如果找不到则返回null
	 * 
	 * @param appId
	 * @return
	 */
	public Cursor getDownloadDataCursor(int downloadDataId) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			Cursor cursor = mDb.query(mTableName,
					new String[] { DbHelper.DOWNLOAD_DATA_ID,
							DbHelper.DOWNLOAD_DATA_NAME,
							DbHelper.DOWNLOAD_DATA_URL,
							DbHelper.DOWNLOAD_DATA_VERSION, DbHelper.DOWNLOAD_DATA_VERSION_CODE,
							DbHelper.STATUS, 
							DbHelper.FILE_DIR,
							DbHelper.FILE_NAME,
							DbHelper.FINISH_TIME},
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadDataId + "" }, null, null, null);
			return cursor;
		}
		return null;
	}
	
	/**
	 * 返回一个DownloadData对象的cursor, 如果找不到则返回null
	 * 重写该方法的时候请先调用{@link public Cursor getDownloadDataCursor(int downloadDataId) },
	 * 这样就会返回一个Cursor对象给调用者操作。
	 * @param downloadDataId
	 * @return
	 */
	public abstract DownloadData getDownloadData(int downloadDataId) ;

	/**
	 * 得到是否下载完成
	 * 
	 * @param appId
	 * @return
	 */
	public boolean getIsDownloaded(int downloadDataId) {
		synchronized (mLock) {
		boolean downloaded = false;
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			if (exists(downloadDataId)) {
				Cursor cursor = mDb.query(mTableName,
						new String[] { DbHelper.STATUS },
						DbHelper.DOWNLOAD_DATA_ID + "=?", new String[] { downloadDataId
								+ "" }, null, null, null);
				while (cursor.moveToNext()) {
					if (cursor.getInt(0) >= FileDownloader.STATUS_APPLY_WAIT) {
						File file = new File(getFileDir(downloadDataId),
								getFileName(downloadDataId));
						if (file.exists()) {
							downloaded = true;
						}
					}
				}
				cursor.close();
			}
		}
		return downloaded;
		}
	}

	/**
	 * 删除所有数据库记录
	 */
	public void deleteAll() {
		synchronized (mLock) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			mDb.delete(mTableName, null, null);
		}
		}
	}



	/**
	 * 获取已完成但未使用列表的Cursor对象
	 * 
	 * @return
	 */
	public Cursor getAppliedDatasCursor() {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			Cursor cursor = mDb.query(mTableName,
					new String[] { DbHelper.DOWNLOAD_DATA_ID,
							DbHelper.DOWNLOAD_DATA_NAME,
							DbHelper.DOWNLOAD_DATA_URL,
							DbHelper.DOWNLOAD_DATA_VERSION, DbHelper.DOWNLOAD_DATA_VERSION_CODE,
							DbHelper.STATUS,
							DbHelper.FILE_DIR,
							DbHelper.FILE_NAME,
							DbHelper.FINISH_TIME},
					DbHelper.APPLIED + "=? ", new String[] {"1"}, null,
					null, null);
			return cursor;
		}
		return null;
	}

	/**
	 * 获取已完成但未使用列表的Cursor对象
	 * 
	 * @return
	 */
	public boolean getApplied(int downloadDataId) {
		synchronized (mLock) {
			if (mDb == null) {
				openDatabase();
			}
			if (mDb != null) {
				Cursor cursor = mDb.query(mTableName,
						new String[] { DbHelper.APPLIED },
						DbHelper.DOWNLOAD_DATA_ID + "=?",
						new String[] { downloadDataId + "" }, null, null, null);
				if (cursor != null && cursor.moveToFirst()) {
					int applied = cursor.getInt(0);
					cursor.close();
					return applied == 1;
				}
				cursor.close();
			}
			return false;
			}
	}
	
	/**
	 * 获取已完成但未使用列表的Cursor对象
	 * 重写该方法的时候请先调用{@link public Cursor getUnAppliedDatasCursor()  },
	 * 这样就会返回一个Cursor对象给调用者操作
	 * @return
	 */
	public abstract List<DownloadData> getAppliedDatas();
	
	
	/**
	 * 获取已完成列表
	 * 
	 * @return
	 */
	public Cursor getDownloadedDatasCursor() {
		List<DownloadData> data = new ArrayList<DownloadData>();
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			Cursor cursor = mDb.query(mTableName,
					new String[] { DbHelper.DOWNLOAD_DATA_ID,
							DbHelper.DOWNLOAD_DATA_NAME,
							DbHelper.DOWNLOAD_DATA_URL,
							DbHelper.DOWNLOAD_DATA_VERSION, DbHelper.DOWNLOAD_DATA_VERSION_CODE,
							DbHelper.STATUS, 
							DbHelper.FILE_DIR,
							DbHelper.FILE_NAME,
							DbHelper.FINISH_TIME},
					DbHelper.STATUS + ">=?",
					new String[] { FileDownloader.STATUS_APPLY_WAIT + "" }, null,
					null, null);
			return cursor;
		}
		return null;
	}

	/**
	 * 获取更新数据库Cursor
	 * @return
	 */
	public Cursor getUpdateCursor(int downloadId) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			Cursor cursor = mDb.query(mTableName,
					new String[] { DbHelper.DOWNLOAD_DATA_ID,
							DbHelper.STATUS},
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadId + "" }, null,
					null, null);
			return cursor;
		}
		return null;
	}
	
	/**
	 * 获取已完成列表
	 * 重写该方法的时候请先调用{@link public Cursor getDownloadedDatasCursor()  },
	 * 这样就会返回一个Cursor对象给调用者操作
	 * @return
	 */
	public abstract List<DownloadData> getDownloadedDatas();
	
	public void setAppApplied(int downloadDataId) {
		synchronized (mLock) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			ContentValues values = new ContentValues();
			values.put(DbHelper.APPLIED, 1);
			mDb.update(mTableName, values,
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadDataId+"" });
		}
		}
	}
	
	public void setUnAppApplied(int downloadDataId) {
		synchronized (mLock) {
		if (mDb == null) {
			openDatabase();
		}
		if (mDb != null) {
			ContentValues values = new ContentValues();
			values.put(DbHelper.APPLIED, 0);
			mDb.update(mTableName, values,
					DbHelper.DOWNLOAD_DATA_ID + "=?",
					new String[] { downloadDataId+"" });
		}
		}
	}
	
	
	/**
	 * 获取数据库控制类
	 * @param type
	 * 				数据库控制类的类型
	 * @param context
	 * @return
	 */
	public static DatabaseController getController(Context context,int type){
		if(type == TYPE_DOWNLOAD){
			return new ThemePackageDatabaseController(context);
		}else if(type == TYPE_RINGTONG_DOWNLOAD){
			return new RingtongDatabaseController(context);
		}else if(type == TYPE_TIME_WALLPAPER_DOWNLOAD){
			return new TimeWallpaperDatabaseController(context);
		}else if(type == TYPE_WALLPAPER_DOWNLOAD){
			return new WallpaperDatabaseController(context);
		}else{
			return new DatabaseUpdateController(context, DbHelper.DOWNLOAD_DATA_AUTOUPDATE_TABLE);
		}
	}

	
	
	
	class DbHelper extends SQLiteOpenHelper{

		/**
		 * 数据库名称
		 */
		public static final String DATABASE_NAME = "download";  
		/**
		 * 数据库版本号
		 */
		public static final int DATABASE_VERSION = 16;  

		/**
		 * 存放下载的数据的数据表
		 */
		public static final String DOWNLOAD_TABLE = "theme";
		
		/**
		 * 数据更新的数据库表
		 */
		public static final String DOWNLOAD_DATA_AUTOUPDATE_TABLE = "download_data_auto_update";
		
		/**
		 * 时光锁屏数据表
		 */
		public static final String DOWNLOAD_TIME_WALLPAPER_TABLE = "time_wallpaper";
		
		/**
		 * 壁纸数据表
		 */
		public static final String DOWNLOAD_WALLPAPER_TABLE = "wallpaper";
		/**
		 * 铃声数据表
		 */
		public static final String DOWNLOAD_RINGTON_TABLE = "ringtong";
		
		
		public static final String THEME_PKG_UPDATE_TABLE = "theme_pkg_update";
		
		/**
		 * 下载的数据的id
		 */
		public static final String DOWNLOAD_DATA_ID = "download_data_id"; 
		
		/**
		 * 下载的数据的名字
		 */
		public static final String DOWNLOAD_DATA_NAME = "name"; 
		/**
		 * 下载路径URL
		 */
		public static final String DOWNLOAD_DATA_URL= "download_url"; 
		
		/**
		 * 下载数据的版本名，字符串标志
		 */
		public static final String DOWNLOAD_DATA_VERSION = "verison_str"; 
		
		/**
		 * 下载数据的版本号，整形标志
		 */
		public static final String DOWNLOAD_DATA_VERSION_CODE = "version_code"; 
		/**
		 * 下载状态
		 */
		public static final String STATUS = "status"; 
		/**
		 * 已下载长度
		 */
		public static final String DOWN_LENGTH = "downlength"; 
		/**
		 * 文件大小(总)
		 */
		public static final String FILE_SIZE = "filesize";  
		/**
		 *  文件存放目录
		 */
		public static final String FILE_DIR = "filedir"; 
		/**
		 * 文件名称
		 */
		public static final String FILE_NAME = "filename"; 
		/**
		 * 任务创建时间
		 */
		public static final String CREATE_TIME = "createtime"; 
		
		/**
		 * 是否已经使用
		 */
		public static final String APPLIED = "applied"; 
		
		/**
		 * 任务完成时间
		 */
		public static final String FINISH_TIME = "finishtime";	
		
		/**
		 * 铃声类型
		 */
		public static final String RINGTONE_TYPE = "ringtone_type";	
		
		/**
		 * 是否有新版本
		 */
		public static final String HAS_NEW_VERSION = "has_new_version"; 
		
		/**
		 * 铃声作者
		 */
		public static final String RINGTONE_AUTHOR = "ringtone_author";
		
		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			/*
			 * 创建任务下载数据表
			 */
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE IF NOT EXISTS " + DOWNLOAD_TABLE + " (");
			sb.append(DOWNLOAD_DATA_ID + " INTEGER ,");
			sb.append(DOWNLOAD_DATA_NAME + " VARCHAR(100),");
			sb.append(DOWNLOAD_DATA_URL + " VARCHAR(100),");
			sb.append(DOWNLOAD_DATA_VERSION + " VARCHAR(50),");
			sb.append(DOWNLOAD_DATA_VERSION_CODE + " INTEGER,");
			sb.append(STATUS + " INTEGER,");
			sb.append(DOWN_LENGTH + " TEXT,");
			sb.append(FILE_SIZE + " TEXT,");
			sb.append(FILE_DIR + " VARCHAR(150),");
			sb.append(FILE_NAME + " VARCHAR(50),");
			sb.append(CREATE_TIME + " TEXT,");
			sb.append(APPLIED + " INGEGER DEFAULT 0,");
			sb.append(FINISH_TIME + " TEXT)");
			db.execSQL(sb.toString());
			
			/*
			 * 创建更新数据表
			 */
			/*
			 * 创建主题更新数据表
			 */
			 sb = new StringBuilder();
			sb.append("CREATE TABLE IF NOT EXISTS " + DOWNLOAD_DATA_AUTOUPDATE_TABLE + " (");
			sb.append(DOWNLOAD_DATA_ID + " INTEGER ,");
			sb.append(HAS_NEW_VERSION + " INTEGER)");
			db.execSQL(sb.toString());
			
			
			
			/*
			 * 创建时光锁屏数据表
			 */
			 sb = new StringBuilder();
			sb.append("CREATE TABLE IF NOT EXISTS " + DOWNLOAD_TIME_WALLPAPER_TABLE + " (");
			sb.append(DOWNLOAD_DATA_ID + " INTEGER ,");
			sb.append(DOWNLOAD_DATA_NAME + " VARCHAR(100),");
			sb.append(DOWNLOAD_DATA_URL + " VARCHAR(100),");
			sb.append(DOWNLOAD_DATA_VERSION + " VARCHAR(50),");
			sb.append(DOWNLOAD_DATA_VERSION_CODE + " INTEGER,");
			sb.append(STATUS + " INTEGER,");
			sb.append(DOWN_LENGTH + " TEXT,");
			sb.append(FILE_SIZE + " TEXT,");
			sb.append(FILE_DIR + " VARCHAR(150),");
			sb.append(FILE_NAME + " VARCHAR(50),");
			sb.append(CREATE_TIME + " TEXT,");
			sb.append(APPLIED + " INGEGER DEFAULT 0,");
			sb.append(FINISH_TIME + " TEXT)");
			db.execSQL(sb.toString());
			
			/*
			 * 创建壁纸数据表
			 */
			 sb = new StringBuilder();
			sb.append("CREATE TABLE IF NOT EXISTS " + DOWNLOAD_WALLPAPER_TABLE + " (");
			sb.append(DOWNLOAD_DATA_ID + " INTEGER ,");
			sb.append(DOWNLOAD_DATA_NAME + " VARCHAR(100),");
			sb.append(DOWNLOAD_DATA_URL + " VARCHAR(100),");
			sb.append(DOWNLOAD_DATA_VERSION + " VARCHAR(50),");
			sb.append(DOWNLOAD_DATA_VERSION_CODE + " INTEGER,");
			sb.append(STATUS + " INTEGER,");
			sb.append(DOWN_LENGTH + " TEXT,");
			sb.append(FILE_SIZE + " TEXT,");
			sb.append(FILE_DIR + " VARCHAR(150),");
			sb.append(FILE_NAME + " VARCHAR(50),");
			sb.append(CREATE_TIME + " TEXT,");
			sb.append(APPLIED + " INGEGER DEFAULT 0,");
			sb.append(FINISH_TIME + " TEXT)");
			db.execSQL(sb.toString());
			
			
			/*
			 * 创建铃声数据表
			 */
			 sb = new StringBuilder();
			sb.append("CREATE TABLE IF NOT EXISTS " + DOWNLOAD_RINGTON_TABLE + " (");
			sb.append(DOWNLOAD_DATA_ID + " INTEGER ,");
			sb.append(DOWNLOAD_DATA_NAME + " VARCHAR(100),");
			sb.append(DOWNLOAD_DATA_URL + " VARCHAR(100),");
			sb.append(DOWNLOAD_DATA_VERSION + " VARCHAR(50),");
			sb.append(DOWNLOAD_DATA_VERSION_CODE + " INTEGER,");
			sb.append(STATUS + " INTEGER,");
			sb.append(DOWN_LENGTH + " TEXT,");
			sb.append(FILE_SIZE + " TEXT,");
			sb.append(FILE_DIR + " VARCHAR(150),");
			sb.append(FILE_NAME + " VARCHAR(50),");
			sb.append(CREATE_TIME + " TEXT,");
			sb.append(APPLIED + " INGEGER DEFAULT 0,");
			sb.append(FINISH_TIME + " TEXT,");
			sb.append(RINGTONE_TYPE + " INTEGER,");
			sb.append(RINGTONE_AUTHOR + " TEXT)");
			db.execSQL(sb.toString());
			
			
		
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			/*
			 * 如果数据库更新就删除旧的数据表，重新创建新表
			 */
			db.execSQL("DROP TABLE IF EXISTS " + DOWNLOAD_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DOWNLOAD_DATA_AUTOUPDATE_TABLE);
			
			db.execSQL("DROP TABLE IF EXISTS " + DOWNLOAD_TIME_WALLPAPER_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DOWNLOAD_WALLPAPER_TABLE);
			
			db.execSQL("DROP TABLE IF EXISTS " + DOWNLOAD_RINGTON_TABLE);
			
			
			onCreate(db);
		}
	}

}
