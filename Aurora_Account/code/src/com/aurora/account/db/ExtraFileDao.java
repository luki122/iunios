package com.aurora.account.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public abstract class ExtraFileDao {
	
	public static final int TYPE_DOWNLOAD = 1;
	public static final int TYPE_UPLOAD = 2;
	
	private ExtraFileDbHelper dbHelper; // 数据库帮助类
	protected SQLiteDatabase db; // 数据库对象
	

	
	public ExtraFileDao(Context context) {
	
		
		dbHelper = new ExtraFileDbHelper(context);
	}
	
	protected boolean checkDb() {
		if (db == null) {
			openDatabase();
		}
		if (db != null && db.isOpen()) {
			return true;
		}
		return false;
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
	 * 关闭资源
	 */
	public void close() {
		closeDatabase();
		if (dbHelper != null) {
			dbHelper.close();
		}
	}
	
	//=======================已下是上传及下载的共用方法=======================//
	
//	/**
//	 * 删除所有数据库记录
//	 */
//	public void deleteAll() {
//		if (checkDb()) {
//			db.delete(TABLE_NAME, null, null);
//		}
//	}
//	
//	/**
//	 * 判断是否已存在记录
//	 * 
//	 * @param uriOrPath
//	 * @return
//	 */
//	public abstract boolean isExists(String uriOrPath);
//	
//
//	/**
//	 * 插入一条数据库记录
//	 * 
//	 * @param data
//	 * @param status
//	 * @param filesize
//	 */
//	public abstract void insert(ExtraFileData data, long createTime, long status,
//			long filesize);
//	
//	
//	/**
//	 * 删除数据库相应记录
//	 * 
//	 * @param uriOrPath
//	 */
//	public abstract void delete(String uriOrPath);
//	
//	
//	/**
//	 * 获取创建时间
//	 * 
//	 * @param uriOrPath
//	 * @return
//	 */
//	public abstract long getCreateTime(String uriOrPath);
//
//	
//	/**
//	 * 更新状态
//	 * 
//	 * @param uriOrPath
//	 * @param status
//	 */
//	public abstract void updateStatus(String uriOrPath, int status);
//
//	
//	/**
//	 * 获取状态信息
//	 * 
//	 * @param uriOrPath
//	 * @return
//	 */
//	public abstract int getStatus(String uriOrPath);
//	
//	/**
//	 * 更新文件大小
//	 * 
//	 * @param uriOrPath
//	 * @param fileSize
//	 */
//	public abstract void updateFileSize(String uriOrPath, long fileSize);
//
//
//	/**
//	 * 获取文件大小
//	 * 
//	 * @param uri
//	 * @return
//	 */
//	public abstract long getFileSize(String uriOrPath);
//	
//	
//	/**
//	 * 更新文件存放名称
//	 * 
//	 * @param uriOrPath
//	 * @param fileName
//	 */
//	public abstract void updateFileName(String uriOrPath, String fileName);
//	
//	
//	/**
//	 * 更新文件存放目录及名称
//	 * 
//	 * @param uriOrPath
//	 * @param dir
//	 * @param fileName
//	 */
//	public abstract void updateFileDirAndName(String uriOrPath, String dir, String fileName);
//	
//	
//	/**
//	 * 获取文件存放名称
//	 * 
//	 * @param uriOrPath
//	 * @return
//	 */
//	public abstract String getFileName(String uriOrPath);
//	
//	
//	/**
//	 * 获取文件存放目录
//	 * 
//	 * @param uriOrPath
//	 * @return
//	 */
//	public abstract String getFileDir(String uriOrPath);
//	
//	
//	/**
//	 * 更新任务完成时间
//	 * 
//	 * @param uriOrPath
//	 * @param dir
//	 * @param fileName
//	 */
//	public abstract void updateFileFinishTime(String uriOrPath, long finishTime);
//	
//	
//	/**
//	 * 返回一个ExtraFileData对象, 如果找不到则返回null
//	 * 
//	 * @param uriOrPath
//	 * @return
//	 */
//	public abstract ExtraFileData getExtraFileData(String uriOrPath);

}
