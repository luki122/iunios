package com.aurora.account.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.aurora.account.bean.DownloadDataResult;
import com.aurora.account.download.FileDownloader;

public class ExtraFileDownloadDao extends ExtraFileDao {
	
	public static final String TAG = "ExtraFileDownloadDao";
	
	public static final String TABLE_NAME = "download";				// 数据库表名
	public static final String DOWN_PATH = "path";					// 客户端附件路径
	public static final String DOWN_SYNC_ID = "syncid";				// 服务器同步id
	public static final String DOWN_PACKAGE_NAME = "package_name";	// 属于哪个package
	public static final String DOWN_URL = "url";					// 下载地址
	public static final String DOWN_STATUS = "status"; 				// 下载状态
	public static final String DOWN_CREATE_TIME = "create_time";
	public static final String DOWN_FINISH_TIME = "finish_time";
	public static final String DOWN_FILE_SIZE = "file_size";
	public static final String DOWN_DOWNLOAD_SIZE = "download_size";
	
	public ExtraFileDownloadDao(Context context) {
		super(context);
	}
	
	//=======================已下是下载专用方法 start=======================//
	
	/**
	 * 获取文件已下载大小
	 * 
	 * @param syncId
	 * @return
	 */
	public long getDownloadSize(String syncId) {
		long downloadSize = 0;
		if (checkDb()) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { DOWN_DOWNLOAD_SIZE },
					DOWN_SYNC_ID + "=?",
					new String[] { syncId + "" }, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					downloadSize = cursor.getLong(cursor.getColumnIndex(DOWN_DOWNLOAD_SIZE));
				}
				cursor.close();
			}
		}
		return downloadSize;
	}
	
	/**
	 * 更新文件已下载大小
	 * 
	 * @param syncId
	 * @param fileSize
	 */
	public synchronized void updateDownloadSize(String syncId, long downloadSize) {
		if (checkDb()) {
			ContentValues values = new ContentValues();
			values.put(DOWN_DOWNLOAD_SIZE, downloadSize);
			db.update(TABLE_NAME, values,
					DOWN_SYNC_ID + "=?",
					new String[] { syncId + "" });
		}
	}
	
	/**
	 * 获取已下载完成列表
	 * 
	 * @return
	 */
	public List<DownloadDataResult> getDownloadDataResult() {
		List<DownloadDataResult> list = new ArrayList<DownloadDataResult>();
		if (checkDb()) {
			String[] columns;
			columns = new String[] { 
					DOWN_PATH,
					DOWN_SYNC_ID,
					DOWN_PACKAGE_NAME,
					DOWN_URL,
					DOWN_STATUS, 
					DOWN_CREATE_TIME,
					DOWN_FINISH_TIME,
					DOWN_FILE_SIZE,
					DOWN_DOWNLOAD_SIZE };
			Cursor cursor = db.query(TABLE_NAME,
					columns, DOWN_STATUS + ">=?",
					new String[] { FileDownloader.STATUS_FINISH + "" }, null,
					null, null);
			if (cursor != null) {
				DownloadDataResult dataResult = null;
				while (cursor.moveToNext()) {
					dataResult = new DownloadDataResult();
					dataResult.setPath(cursor.getString(cursor.getColumnIndex(DOWN_PATH)));
					dataResult.setSyncId(cursor.getString(cursor.getColumnIndex(DOWN_SYNC_ID)));
					dataResult.setPackage_name(cursor.getString(cursor.getColumnIndex(DOWN_PACKAGE_NAME)));
					dataResult.setDownUrl(cursor.getString(cursor.getColumnIndex(DOWN_URL)));
					dataResult.setStatus(cursor.getInt(cursor.getColumnIndex(DOWN_STATUS)));
					dataResult.setCreate_time(cursor.getLong(cursor.getColumnIndex(DOWN_CREATE_TIME)));
					dataResult.setFinish_time(cursor.getLong(cursor.getColumnIndex(DOWN_FINISH_TIME)));
					dataResult.setFile_size(cursor.getLong(cursor.getColumnIndex(DOWN_FILE_SIZE)));
					dataResult.setDownloadSize(cursor.getLong(cursor.getColumnIndex(DOWN_DOWNLOAD_SIZE)));
					
					list.add(dataResult);
				}
				cursor.close();
			}
		}
		return list;
	}
	
	/**
	 * 获取数据库中所有syncId
	 * 
	 * @return
	 */
	public List<String> getAllSyncId() {
		List<String> list = new ArrayList<String>();
		if (checkDb()) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { DOWN_SYNC_ID }, null, null,
					null, null, null);
			if (cursor != null) {
				if (cursor.getCount() > 0) {
					while (cursor.moveToNext()) {
						list.add(cursor.getString(cursor.getColumnIndex(DOWN_SYNC_ID)));
					}
				}
				cursor.close();
			}
		}

		// 删除重复元素
		HashSet<String> h = new HashSet<String>(list);
		list.clear();
		list.addAll(h);

		return list;
	}
	
	
	//=======================已下是下载专用方法 end=======================//
	
	
	public boolean isExist(String syncId) {
		boolean exists = false;
		if (checkDb()) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { DOWN_SYNC_ID },
					DOWN_SYNC_ID + "=?",
					new String[] { syncId + "" }, null, null, null);
			if (cursor != null) {
				if (cursor.getCount() > 0) {
					exists = true;
				}
				cursor.close();
			}
		}
		return exists;
	}
	
	public synchronized void insert(DownloadDataResult result) {

		if (checkDb()) {
			ContentValues values = new ContentValues();
			
			values.put(DOWN_PATH, result.getPath());
			values.put(DOWN_SYNC_ID, result.getSyncId());
			values.put(DOWN_PACKAGE_NAME, result.getPackage_name());
			values.put(DOWN_URL, result.getDownUrl());
			values.put(DOWN_STATUS, result.getStatus());
			values.put(DOWN_CREATE_TIME, result.getCreate_time());
			values.put(DOWN_FINISH_TIME, result.getFinish_time());
			values.put(DOWN_FILE_SIZE, result.getFile_size());
			values.put(DOWN_DOWNLOAD_SIZE, result.getDownloadSize());
			
			db.insert(TABLE_NAME, null, values);
		}
	}
	
	public synchronized void delete(String syncId) {
		if (checkDb()) {
			db.delete(TABLE_NAME, DOWN_SYNC_ID + "=?",
					new String[] { syncId + "" });
		}
	}
	/**
	 * 删除所有数据库记录
	 */
	public synchronized void deleteAll() {

		if (checkDb()) {
			db.delete(TABLE_NAME, null, null);
		}
	}
	public long getCreateTime(String syncId) {
		long createTime = 0;
		if (checkDb()) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { DOWN_CREATE_TIME },
					DOWN_SYNC_ID + "=?",
					new String[] { syncId + "" }, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					createTime = cursor.getLong(cursor.getColumnIndex(DOWN_CREATE_TIME));
				}
				cursor.close();
			}
		}
		return createTime;
	}

	public synchronized void updateStatus(String syncId, int status) {
		if (checkDb()) {
			ContentValues values = new ContentValues();
			values.put(DOWN_STATUS, status);
			db.update(TABLE_NAME, values,
					DOWN_SYNC_ID + "=?",
					new String[] { syncId + "" });
		}
	}

	public int getStatus(String syncId) {
		int status = FileDownloader.STATUS_DEFAULT;
		if (checkDb()) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { DOWN_STATUS },
					DOWN_SYNC_ID + "=?",
					new String[] { syncId + "" }, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					status = cursor.getInt(cursor.getColumnIndex(DOWN_STATUS));
				}
				cursor.close();
			}
		}
		return status;
	}

	public synchronized void updateFileSize(String syncId, long fileSize) {
		if (checkDb()) {
			ContentValues values = new ContentValues();
			values.put(DOWN_FILE_SIZE, fileSize);
			db.update(TABLE_NAME, values,
					DOWN_SYNC_ID + "=?",
					new String[] { syncId + "" });
		}		
	}

	public long getFileSize(String syncId) {
		long fileSize = 0;
		if (checkDb()) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { DOWN_FILE_SIZE },
					DOWN_SYNC_ID + "=?",
					new String[] { syncId + "" }, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					fileSize = cursor.getLong(cursor.getColumnIndex(DOWN_FILE_SIZE));
				}
				cursor.close();
			}
		}
		return fileSize;
	}

	public synchronized void updateFileName(String syncId, String fileName) {
		if (checkDb()) {
			ContentValues values = new ContentValues();
			values.put(DOWN_FILE_SIZE, fileName);
			db.update(TABLE_NAME, values,
					DOWN_SYNC_ID + "=?",
					new String[] { syncId + "" });
		}		
	}

	public synchronized void updateFileFinishTime(String syncId, long finishTime) {
		if (checkDb()) {
			ContentValues values = new ContentValues();
			values.put(DOWN_FINISH_TIME, finishTime);
			db.update(TABLE_NAME, values,
					DOWN_SYNC_ID + "=?",
					new String[] { syncId + "" });
		}
	}

	public DownloadDataResult getDownloadDataResult(String syncId) {
		DownloadDataResult dataResult = null;
		
		if (checkDb()) {
			String[] columns;
			columns = new String[] { 
					DOWN_PATH,
					DOWN_SYNC_ID,
					DOWN_PACKAGE_NAME,
					DOWN_URL,
					DOWN_STATUS, 
					DOWN_CREATE_TIME,
					DOWN_FINISH_TIME,
					DOWN_FILE_SIZE,
					DOWN_DOWNLOAD_SIZE };
			Cursor cursor = db.query(TABLE_NAME,
					columns, DOWN_SYNC_ID + "=?",
					new String[] { syncId + "" }, null, null, null);
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					dataResult = new DownloadDataResult();
					dataResult.setPath(cursor.getString(cursor.getColumnIndex(DOWN_PATH)));
					dataResult.setSyncId(cursor.getString(cursor.getColumnIndex(DOWN_SYNC_ID)));
					dataResult.setPackage_name(cursor.getString(cursor.getColumnIndex(DOWN_PACKAGE_NAME)));
					dataResult.setDownUrl(cursor.getString(cursor.getColumnIndex(DOWN_URL)));
					dataResult.setStatus(cursor.getInt(cursor.getColumnIndex(DOWN_STATUS)));
					dataResult.setCreate_time(cursor.getLong(cursor.getColumnIndex(DOWN_CREATE_TIME)));
					dataResult.setFinish_time(cursor.getLong(cursor.getColumnIndex(DOWN_FINISH_TIME)));
					dataResult.setFile_size(cursor.getLong(cursor.getColumnIndex(DOWN_FILE_SIZE)));
					dataResult.setDownloadSize(cursor.getLong(cursor.getColumnIndex(DOWN_DOWNLOAD_SIZE)));
				}
				cursor.close();
			}
		}
		
		return dataResult;
	}
	
}
