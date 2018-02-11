package com.aurora.account.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.aurora.account.bean.UploadDataResult;
import com.aurora.account.bean.accessoryInfo;
import com.aurora.account.download.FileDownloader;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class ExtraFileUploadDao extends ExtraFileDao {

	public static final String TAG = "ExtraFileUploadDao";
	public static final String TABLE_NAME = "upload";// 数据库表名
	public static final String UPLOAD_ACCESSORY_ID = "accessoryid"; // 附件id
	public static final String UPLOAD_SYNC_ID = "id"; // 客户端模块一条数据的id标示
	public static final String UPLOAD_PACKAGE_NAME = "package_name"; // 属于哪个package
	public static final String UPLOAD_FILE_PATH = "file_path";
	public static final String UPLOAD_STATUS = "status"; // 下载状态
	public static final String UPLOAD_CREATE_TIME = "create_time";
	public static final String UPLOAD_FINISH_TIME = "finish_time";
	public static final String UPLOAD_FILE_SIZE = "file_size";
	public static final String UPLOAD_SIZE = "upload_size";

	public ExtraFileUploadDao(Context context) {
		super(context);
	}

	// =======================已下是上传专用方法 start=======================//

	/**
	 * 获取文件已下载大小
	 * 
	 * @param accessoryId
	 * @return
	 */
	public long getUploadSize(String accessoryId) {
		long downloadSize = 0;
		if (checkDb()) {
			Cursor cursor = db.query(TABLE_NAME, new String[] { UPLOAD_SIZE },
					UPLOAD_ACCESSORY_ID + "=?",
					new String[] { accessoryId + "" }, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					downloadSize = cursor.getLong(cursor
							.getColumnIndex(UPLOAD_SIZE));
				}
				cursor.close();
			}
		}
		return downloadSize;
	}

	/**
	 * 更新文件已下载大小
	 * 
	 * @param accessoryId
	 * @param fileSize
	 */
	public synchronized void updateUploadSize(String accessoryId, long uploadSize) {
		if (checkDb()) {
			ContentValues values = new ContentValues();
			values.put(UPLOAD_SIZE, uploadSize);
			db.update(TABLE_NAME, values, UPLOAD_ACCESSORY_ID + "=?",
					new String[] { accessoryId + "" });
		}
	}

	/**
	 * 获取数据库中所有accessoryId
	 * 
	 * @return
	 */
	public List<String> getAllAccessoryId() {
		List<String> list = new ArrayList<String>();
		if (checkDb()) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { UPLOAD_ACCESSORY_ID }, null, null, null,
					null, null);
			if (cursor != null) {
				if (cursor.getCount() > 0) {
					while (cursor.moveToNext()) {
						list.add(cursor.getString(cursor
								.getColumnIndex(UPLOAD_ACCESSORY_ID)));
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

	// =======================已下是下载专用方法 end=======================//

	public boolean isExists(String accessoryId) {
		boolean exists = false;
		if (checkDb()) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { UPLOAD_ACCESSORY_ID }, UPLOAD_ACCESSORY_ID
							+ "=?", new String[] { accessoryId + "" }, null,
					null, null);
			if (cursor != null) {
				if (cursor.getCount() > 0) {
					exists = true;
				}
				cursor.close();
			}
		}
		return exists;
	}

	public synchronized void insert(UploadDataResult upResult) {

		if (checkDb()) {
			ContentValues values = new ContentValues();
			values.put(UPLOAD_ACCESSORY_ID, upResult.getAccessoryid());
			values.put(UPLOAD_SYNC_ID, upResult.getId());
			values.put(UPLOAD_PACKAGE_NAME, upResult.getPackage_name());

			values.put(UPLOAD_FILE_PATH, upResult.getFile_path());
			values.put(UPLOAD_STATUS, upResult.getStatus());
			values.put(UPLOAD_CREATE_TIME, upResult.getCreate_time());

			db.insert(TABLE_NAME, null, values);
		}
	}

	public synchronized void delete(String accessoryId) {
		if (checkDb()) {
			db.delete(TABLE_NAME, UPLOAD_ACCESSORY_ID + "=?",
					new String[] { accessoryId + "" });
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
	public long getCreateTime(String accessoryId) {
		long createTime = 0;
		if (checkDb()) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { UPLOAD_CREATE_TIME }, UPLOAD_ACCESSORY_ID
							+ "=?", new String[] { accessoryId + "" }, null,
					null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					createTime = cursor.getLong(cursor
							.getColumnIndex(UPLOAD_CREATE_TIME));
				}
				cursor.close();
			}
		}
		return createTime;
	}

	public synchronized void updateStatus(String accessoryId, int status) {
		if (checkDb()) {
			ContentValues values = new ContentValues();
			values.put(UPLOAD_STATUS, status);
			db.update(TABLE_NAME, values, UPLOAD_ACCESSORY_ID + "=?",
					new String[] { accessoryId + "" });
		}
	}

	public synchronized int getStatus(String accessoryId) {
		int status = FileDownloader.STATUS_DEFAULT;
		if (checkDb()) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { UPLOAD_STATUS }, UPLOAD_ACCESSORY_ID + "=?",
					new String[] { accessoryId + "" }, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					status = cursor
							.getInt(cursor.getColumnIndex(UPLOAD_STATUS));
				}
				cursor.close();
			}
		}
		return status;
	}

	public synchronized void updateFileSize(String accessoryId, long fileSize) {
		if (checkDb()) {
			ContentValues values = new ContentValues();
			values.put(UPLOAD_FILE_SIZE, fileSize);
			db.update(TABLE_NAME, values, UPLOAD_ACCESSORY_ID + "=?",
					new String[] { accessoryId + "" });
		}
	}

	public synchronized long getFileSize(String accessoryId) {
		long fileSize = 0;
		if (checkDb()) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { UPLOAD_FILE_SIZE }, UPLOAD_ACCESSORY_ID
							+ "=?", new String[] { accessoryId + "" }, null,
					null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					fileSize = cursor.getLong(cursor
							.getColumnIndex(UPLOAD_FILE_SIZE));
				}
				cursor.close();
			}
		}
		return fileSize;
	}

	public synchronized void updateFilePath(String accessoryId, String filePath) {
		if (checkDb()) {
			ContentValues values = new ContentValues();
			values.put(UPLOAD_FILE_PATH, filePath);
			db.update(TABLE_NAME, values, UPLOAD_ACCESSORY_ID + "=?",
					new String[] { accessoryId + "" });
		}
	}

	public synchronized void updateFileFinishTime(String accessoryId, long finishTime) {
		if (checkDb()) {
			ContentValues values = new ContentValues();
			values.put(UPLOAD_FINISH_TIME, finishTime);
			db.update(TABLE_NAME, values, UPLOAD_ACCESSORY_ID + "=?",
					new String[] { accessoryId + "" });
		}
	}

	public UploadDataResult getUpDataInfo(String accessoryId) {
		UploadDataResult info = null;

		if (checkDb()) {
			String[] columns;
			columns = new String[] { UPLOAD_ACCESSORY_ID, UPLOAD_SYNC_ID,
					UPLOAD_PACKAGE_NAME, UPLOAD_FILE_PATH, UPLOAD_STATUS,
					UPLOAD_CREATE_TIME, UPLOAD_FINISH_TIME, UPLOAD_FILE_SIZE,
					UPLOAD_SIZE };
			Cursor cursor = db
					.query(TABLE_NAME, columns, UPLOAD_ACCESSORY_ID + "=?",
							new String[] { accessoryId + "" }, null, null, null);

			if (cursor != null) {
				if (cursor.moveToFirst()) {
					info = new UploadDataResult();
					info.setAccessoryid(cursor.getString(cursor
							.getColumnIndex(UPLOAD_ACCESSORY_ID)));
					info.setId(cursor.getString(cursor
							.getColumnIndex(UPLOAD_SYNC_ID)));

					info.setPackage_name(cursor.getString(cursor
							.getColumnIndex(UPLOAD_PACKAGE_NAME)));
					info.setFile_path(cursor.getString(cursor
							.getColumnIndex(UPLOAD_FILE_PATH)));

					info.setStatus(cursor.getInt(cursor
							.getColumnIndex(UPLOAD_STATUS)));

					info.setCreate_time(cursor.getString(cursor
							.getColumnIndex(UPLOAD_CREATE_TIME)));

					info.setFinish_time(cursor.getString(cursor
							.getColumnIndex(UPLOAD_FINISH_TIME)));

					info.setFile_size(cursor.getString(cursor
							.getColumnIndex(UPLOAD_FILE_SIZE)));

					info.setUpload_size(cursor.getString(cursor
							.getColumnIndex(UPLOAD_SIZE)));
				}
				cursor.close();
			}
		}

		return info;
	}
	
	
	/**
	 * 判断是否已存在数据
	 * 
	 * @param appId
	 * @return
	 */
	public boolean isExist(String accessoryId) {
		
		if (checkDb()) {
			String[] columns;
			columns = new String[] { UPLOAD_ACCESSORY_ID, UPLOAD_SYNC_ID,
					UPLOAD_PACKAGE_NAME, UPLOAD_FILE_PATH, UPLOAD_STATUS,
					UPLOAD_CREATE_TIME, UPLOAD_FINISH_TIME, UPLOAD_FILE_SIZE,
					UPLOAD_SIZE };
			Cursor cursor = db
					.query(TABLE_NAME, columns, UPLOAD_ACCESSORY_ID + "=?",
							new String[] { accessoryId + "" }, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.close();
				return true;
			}
			cursor.close();
		}
		
		return false;
		
	}
}
