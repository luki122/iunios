package com.aurora.market.db;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.aurora.market.download.FileDownloader;
import com.aurora.market.model.DownloadData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class AppDownloadDao {

	private AppDownloadHelper dbHelper; // 数据库帮助类
	private SQLiteDatabase db; // 数据库对象
	protected String TABLE_NAME = AppDownloadHelper.DOWNLOAD_TABLE;

	public AppDownloadDao(Context context) {
		dbHelper = new AppDownloadHelper(context);
	}
	
	public AppDownloadDao(Context context, String tableName) {
		TABLE_NAME = tableName;
		dbHelper = new AppDownloadHelper(context);
	}

	/**
	 * 打开数据库
	 */
	public void openDatabase() {
		db = dbHelper.getWritableDatabase();
	}

	/**
	 * 关闭数据库
	 */
	public void closeDatabase() {
		if (db != null && db.isOpen()) {
			db.close();
		}
	}

	/**
	 * 判断是否已存在数据
	 * 
	 * @param appId
	 * @return
	 */
	public boolean isExist(int appId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { AppDownloadHelper.APK_ID },
					AppDownloadHelper.APK_ID + "=?",
					new String[] { appId + "" }, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.close();
				return true;
			}
			cursor.close();
		}
		return false;
	}

	/**
	 * 删除数据库相应记录
	 * 
	 * @param appId
	 */
	public void delete(int appId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			db.delete(TABLE_NAME,
					AppDownloadHelper.APK_ID + "=?",
					new String[] { appId + "" });
		}
	}

	/**
	 * 插入一条数据库记录
	 * 
	 * @param data
	 * @param threadId
	 * @param status
	 * @param filesize
	 */
	public void insert(DownloadData data, long createTime, long status,
			long filesize) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(AppDownloadHelper.APK_ID, data.getApkId());
			values.put(AppDownloadHelper.APPNAME, data.getApkName());
			values.put(AppDownloadHelper.DOWNLOAD_PATH,
					data.getApkDownloadPath());
			values.put(AppDownloadHelper.VERSION, data.getVersionName());
			values.put(AppDownloadHelper.VCODE, data.getVersionCode());
			values.put(AppDownloadHelper.PACKAGENAME, data.getPackageName());
			values.put(AppDownloadHelper.CREATE_TIME, createTime);
			values.put(AppDownloadHelper.STATUS, status);
			values.put(AppDownloadHelper.FILE_SIZE, filesize);
			values.put(AppDownloadHelper.ICON_PATH, data.getApkLogoPath());
			values.put(AppDownloadHelper.FINISH_TIME, 0);
			db.insert(TABLE_NAME, null, values);
		}
	}

	/**
	 * 获取创建时间
	 * 
	 * @param appId
	 * @return
	 */
	public long getCreateTime(int appId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { AppDownloadHelper.CREATE_TIME },
					AppDownloadHelper.APK_ID + "=?",
					new String[] { appId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				long createTime = Long.parseLong(cursor.getString(0));
				cursor.close();
				return createTime;
			}
			cursor.close();
		}
		return 0;
	}

	/**
	 * 更新状态
	 * 
	 * @param appId
	 * @param status
	 */
	public void updateStatus(int appId, int status) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(AppDownloadHelper.STATUS, status);
			db.update(TABLE_NAME, values,
					AppDownloadHelper.APK_ID + "=?",
					new String[] { appId + "" });
		}
	}

	/**
	 * 获取状态信息
	 * 
	 * @param appId
	 * @return
	 */
	public int getStatus(int appId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { AppDownloadHelper.STATUS },
					AppDownloadHelper.APK_ID + "=?",
					new String[] { appId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				int status = cursor.getInt(0);
				cursor.close();
				return status;
			}
			cursor.close();
		}
		return FileDownloader.STATUS_DEFAULT;
	}

	/**
	 * 更新文件大小
	 * 
	 * @param appId
	 * @param fileSize
	 */
	public void updateFileSize(int appId, long fileSize) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(AppDownloadHelper.FILE_SIZE, fileSize);
			db.update(TABLE_NAME, values,
					AppDownloadHelper.APK_ID + "=?",
					new String[] { appId + "" });
		}
	}

	/**
	 * 获取文件大小
	 * 
	 * @param appId
	 * @return
	 */
	public long getFileSize(int appId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { AppDownloadHelper.FILE_SIZE },
					AppDownloadHelper.APK_ID + "=?",
					new String[] { appId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				long size = cursor.getLong(0);
				cursor.close();
				return size;
			}
			cursor.close();
		}
		return 0;
	}

	/**
	 * 获取已文件下载大小
	 * 
	 * @param appId
	 * @return
	 */
	public long getDownloadSize(int appId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { AppDownloadHelper.DOWN_LENGTH },
					AppDownloadHelper.APK_ID + "=?",
					new String[] { appId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				long size = cursor.getLong(0);
				cursor.close();
				return size;
			}
			cursor.close();
		}
		return 0;
	}
	
	/**
	 * 获取已文件下载大小
	 * 
	 * @param appId
	 * @param fileSize
	 */
	public void updateDownloadSize(int appId, long downloadSize) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(AppDownloadHelper.DOWN_LENGTH, downloadSize);
			db.update(TABLE_NAME, values,
					AppDownloadHelper.APK_ID + "=?",
					new String[] { appId + "" });
		}
	}

	/**
	 * 更新文件存放名称
	 * 
	 * @param appId
	 * @param fileName
	 */
	public void updateFileName(int appId, String fileName) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(AppDownloadHelper.FILE_NAME, fileName);
			db.update(TABLE_NAME, values,
					AppDownloadHelper.APK_ID + "=?",
					new String[] { appId + "" });
		}
	}

	/**
	 * 更新文件存放目录及名称
	 * 
	 * @param appId
	 * @param dir
	 * @param fileName
	 */
	public void updateFileDirAndName(int appId, String dir, String fileName) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(AppDownloadHelper.FILE_DIR, dir);
			values.put(AppDownloadHelper.FILE_NAME, fileName);
			db.update(TABLE_NAME, values,
					AppDownloadHelper.APK_ID + "=?",
					new String[] { appId + "" });
		}
	}

	/**
	 * 获取文件存放名称
	 * 
	 * @param appId
	 * @return
	 */
	public String getFileName(int appId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { AppDownloadHelper.FILE_NAME },
					AppDownloadHelper.APK_ID + "=?",
					new String[] { appId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String fileName = cursor.getString(0);
				cursor.close();
				return fileName;
			}
			cursor.close();
		}
		return "";
	}

	/**
	 * 获取文件存放目录
	 * 
	 * @param appId
	 * @return
	 */
	public String getFileDir(int appId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { AppDownloadHelper.FILE_DIR },
					AppDownloadHelper.APK_ID + "=?",
					new String[] { appId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String dir = cursor.getString(0);
				cursor.close();
				return dir;
			}
			cursor.close();
		}
		return "";
	}
	/**
	 * 获取文件存放目录
	 * 
	 * @param appId
	 * @return
	 */
	public String getFileDir(String pkgName) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { AppDownloadHelper.FILE_DIR,AppDownloadHelper.FILE_NAME },
					AppDownloadHelper.PACKAGENAME + "=?",
					new String[] { pkgName + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String dir = cursor.getString(0);
				String name = cursor.getString(1);
				cursor.close();
				return dir+name;
			}
			cursor.close();
		}
		return "";
	}
	/**
	 * 更新任务完成时间
	 * 
	 * @param appId
	 * @param dir
	 * @param fileName
	 */
	public void updateFileFinishTime(int appId, long finishTime) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(AppDownloadHelper.FINISH_TIME, finishTime);
			db.update(TABLE_NAME, values,
					AppDownloadHelper.APK_ID + "=?",
					new String[] { appId + "" });
		}
	}

	/**
	 * 获取数据库中所有appId
	 * 
	 * @return
	 */
	public List<Integer> getAllappId() {
		List<Integer> list = new ArrayList<Integer>();
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { AppDownloadHelper.APK_ID }, null, null,
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

	/**
	 * 返回一个DownloadData对象, 如果找不到则返回null
	 * 
	 * @param appId
	 * @return
	 */
	public DownloadData getDownloadData(int appId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { AppDownloadHelper.APK_ID,
							AppDownloadHelper.APPNAME,
							AppDownloadHelper.DOWNLOAD_PATH,
							AppDownloadHelper.VERSION, AppDownloadHelper.VCODE,
							AppDownloadHelper.PACKAGENAME,
							AppDownloadHelper.ICON_PATH,
							AppDownloadHelper.STATUS, 
							AppDownloadHelper.FILE_DIR,
							AppDownloadHelper.FILE_NAME,
							AppDownloadHelper.FINISH_TIME},
					AppDownloadHelper.APK_ID + "=?",
					new String[] { appId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				DownloadData downloadData = new DownloadData();
				downloadData.setApkId(cursor.getInt(0));
				downloadData.setApkName(cursor.getString(1));
				downloadData.setApkDownloadPath(cursor.getString(2));
				downloadData.setVersionName(cursor.getString(3));
				downloadData.setVersionCode(cursor.getInt(4));
				downloadData.setPackageName(cursor.getString(5));
				downloadData.setApkLogoPath(cursor.getString(6));
				downloadData.setStatus(cursor.getInt(7));
				downloadData.setFileDir(cursor.getString(8));
				downloadData.setFileName(cursor.getString(9));
				downloadData.setFinishTime(cursor.getLong(10));
				cursor.close();
				cursor = null;
				return downloadData;
			}
			cursor.close();
		}
		return null;
	}

	/**
	 * 得到是否下载完成
	 * 
	 * @param appId
	 * @return
	 */
	public boolean getIsDownloaded(int appId) {
		boolean downloaded = false;
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			if (isExist(appId)) {
				Cursor cursor = db.query(TABLE_NAME,
						new String[] { AppDownloadHelper.STATUS },
						AppDownloadHelper.APK_ID + "=?", new String[] { appId
								+ "" }, null, null, null);
				while (cursor.moveToNext()) {
					if (cursor.getInt(0) >= FileDownloader.STATUS_INSTALL_WAIT) {
						File file = new File(getFileDir(appId),
								getFileName(appId));
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

	/**
	 * 删除所有数据库记录
	 */
	public void deleteAll() {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			db.delete(TABLE_NAME, null, null);
		}
	}

	/**
	 * 根据包名获取fileId
	 * 
	 * @param pkg
	 * @return
	 */
	public int getFileIdByPkg(String pkg) {
		int fileId = -1;
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { AppDownloadHelper.APK_ID },
					AppDownloadHelper.PACKAGENAME + "=?", new String[] { pkg
							+ "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				fileId = cursor.getInt(0);
			}
			cursor.close();
		}
		return fileId;
	}

	/**
	 * 获取已完成但未安装列表
	 * 
	 * @return
	 */
	public List<DownloadData> getUninstallApp() {
		List<DownloadData> data = new ArrayList<DownloadData>();
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { AppDownloadHelper.APK_ID,
							AppDownloadHelper.APPNAME,
							AppDownloadHelper.DOWNLOAD_PATH,
							AppDownloadHelper.VERSION, AppDownloadHelper.VCODE,
							AppDownloadHelper.PACKAGENAME,
							AppDownloadHelper.ICON_PATH,
							AppDownloadHelper.STATUS,
							AppDownloadHelper.FILE_DIR,
							AppDownloadHelper.FILE_NAME,
							AppDownloadHelper.FINISH_TIME},
					AppDownloadHelper.INSTALLED + "=? and "
							+ AppDownloadHelper.STATUS + "=? or "+ AppDownloadHelper.STATUS + "=? ", new String[] {
							"0", FileDownloader.STATUS_INSTALL_WAIT + "" , FileDownloader.STATUS_INSTALLING + ""}, null,
					null, null);
			while (cursor.moveToNext()) {
				// 由于多线程下载会出现多条下载记录，因此把重复ApkId项过滤
				boolean add = true;
				for (DownloadData d : data) {
					if (d.getApkId() == cursor.getInt(0)) {
						add = false;
						break;
					}
				}
				if (add) {
					DownloadData downloadData = new DownloadData();
					downloadData.setApkId(cursor.getInt(0));
					downloadData.setApkName(cursor.getString(1));
					downloadData.setApkDownloadPath(cursor.getString(2));
					downloadData.setVersionName(cursor.getString(3));
					downloadData.setVersionCode(cursor.getInt(4));
					downloadData.setPackageName(cursor.getString(5));
					downloadData.setApkLogoPath(cursor.getString(6));
					downloadData.setStatus(cursor.getInt(7));
					downloadData.setFileDir(cursor.getString(8));
					downloadData.setFileName(cursor.getString(9));
					downloadData.setFinishTime(cursor.getLong(10));
					data.add(downloadData);
				}
			}
			cursor.close();
		}
		return data;
	}

	
	/**
	 * 获取已完成列表
	 * 
	 * @return
	 */
	public List<DownloadData> getDownloadedApp() {
		List<DownloadData> data = new ArrayList<DownloadData>();
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { AppDownloadHelper.APK_ID,
							AppDownloadHelper.APPNAME,
							AppDownloadHelper.DOWNLOAD_PATH,
							AppDownloadHelper.VERSION, AppDownloadHelper.VCODE,
							AppDownloadHelper.PACKAGENAME,
							AppDownloadHelper.ICON_PATH,
							AppDownloadHelper.STATUS, 
							AppDownloadHelper.FILE_DIR,
							AppDownloadHelper.FILE_NAME,
							AppDownloadHelper.FINISH_TIME},
					AppDownloadHelper.STATUS + ">=?",
					new String[] { FileDownloader.STATUS_INSTALL_WAIT + "" }, null,
					null, null);
			while (cursor.moveToNext()) {
				// 由于多线程下载会出现多条下载记录，因此把重复ApkId项过滤
				boolean add = true;
				for (DownloadData d : data) {
					if (d.getApkId() == cursor.getInt(0)) {
						add = false;
						break;
					}
				}
				if (add) {
					DownloadData downloadData = new DownloadData();
					downloadData.setApkId(cursor.getInt(0));
					downloadData.setApkName(cursor.getString(1));
					downloadData.setApkDownloadPath(cursor.getString(2));
					downloadData.setVersionName(cursor.getString(3));
					downloadData.setVersionCode(cursor.getInt(4));
					downloadData.setPackageName(cursor.getString(5));
					downloadData.setApkLogoPath(cursor.getString(6));
					downloadData.setStatus(cursor.getInt(7));
					downloadData.setFileDir(cursor.getString(8));
					downloadData.setFileName(cursor.getString(9));
					downloadData.setFinishTime(cursor.getLong(10));
					data.add(downloadData);
				}
			}
			cursor.close();
		}
		return data;
	}

	public void setAppInstall(String packageName) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(AppDownloadHelper.INSTALLED, 1);
			db.update(TABLE_NAME, values,
					AppDownloadHelper.PACKAGENAME + "=?",
					new String[] { packageName });
		}
	}

	public int getApkIdByPkgName(String packageName) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { AppDownloadHelper.APK_ID },
					AppDownloadHelper.PACKAGENAME + "=?",
					new String[] { packageName }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				int apkId = cursor.getInt(0);
				cursor.close();
				return apkId;
			}
			cursor.close();
		}
		return 0;
	}

}
