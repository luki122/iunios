package com.aurora.thememanager.utils.download;

import java.util.ArrayList;
import java.util.List;

import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.entities.ThemeAudio;
import com.aurora.thememanager.utils.download.DatabaseController.DbHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class RingtongDatabaseController extends DatabaseController {

	public RingtongDatabaseController(Context context) {
		super(context,DbHelper.DOWNLOAD_RINGTON_TABLE);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void insert(DownloadData data, long createTime, long status,
			long filesize) {
		// TODO Auto-generated method stub
		if(!databaseIsOpened()){
			openDatabase();
		}
		ContentValues values = new ContentValues();
		Theme theme = (Theme)data;
		values.put(DbHelper.DOWNLOAD_DATA_ID, theme.downloadId);
		values.put(DbHelper.DOWNLOAD_DATA_NAME, theme.name);
		values.put(DbHelper.DOWNLOAD_DATA_URL,theme.downloadPath);
		values.put(DbHelper.DOWNLOAD_DATA_VERSION, theme.versionCode);
		values.put(DbHelper.DOWNLOAD_DATA_VERSION_CODE, theme.versionCode);
		values.put(DbHelper.STATUS, status);
		values.put(DbHelper.FILE_SIZE, filesize);
		values.put(DbHelper.FINISH_TIME, 0);
		values.put(DbHelper.FILE_DIR, theme.fileDir);
		values.put(DbHelper.FILE_NAME, theme.fileName);
		values.put(DbHelper.CREATE_TIME, createTime);
		if(theme instanceof ThemeAudio) {
			Log.e("101010", "----instanceof-----");
			values.put(DbHelper.RINGTONE_TYPE, ((ThemeAudio)theme).ringtongType);
		} else {
			Log.e("101010", "----no instanceof-----");
		}
		values.put(DbHelper.RINGTONE_AUTHOR, theme.author);
		insert(values);

	}
	


	@Override
	public DownloadData getDownloadData(int downloadDataId) {
		// TODO Auto-generated method stub
		Cursor cursor = getDownloadDataCursor(downloadDataId);
		Theme theme = null;
		if(cursor != null){
			try {
				if( cursor.moveToFirst()){
				 theme = new Theme();
				theme.downloadId = cursor.getInt(0);
				theme.themeId = theme.downloadId;
				theme.name=cursor.getString(1);
				theme.downloadPath = cursor.getString(2);
				theme.version = (cursor.getString(3));
				theme.versionCode = (cursor.getInt(4));
				theme.status = (cursor.getInt(5));
				theme.fileDir = (cursor.getString(6));
				theme.fileName = (cursor.getString(7));
				theme.finishTime = (cursor.getLong(8));
				}
				
			} finally {
				cursor.close();
				cursor = null;
			}
		}
		return theme;
	}

	@Override
	public List<DownloadData> getAppliedDatas() {
		Cursor cursor = getAppliedDatasCursor();
		List<DownloadData> data = new ArrayList<DownloadData>();
		if(cursor != null){
			try {
				while (cursor.moveToNext()) {
					// 由于多线程下载会出现多条下载记录，因此把重复ApkId项过滤
					boolean add = true;
					for (DownloadData t : data) {
						if (t.downloadId == cursor.getInt(0)) {
							add = false;
							break;
						}
					}
					if (add) {
						Theme theme = new Theme();
						theme.downloadId = cursor.getInt(0);
						theme.themeId = theme.downloadId;
						theme.name=cursor.getString(1);
						theme.downloadPath = cursor.getString(2);
						theme.version = (cursor.getString(3));
						theme.versionCode = (cursor.getInt(4));
						theme.status = (cursor.getInt(5));
						theme.fileDir = (cursor.getString(6));
						theme.fileName = (cursor.getString(7));
						theme.finishTime = (cursor.getLong(8));
						data.add(theme);
					}
				}
			} finally {
				cursor.close();
				cursor = null;
			}
		}
		return data;
	}

	@Override
	public List<DownloadData> getDownloadedDatas() {
		// TODO Auto-generated method stub
		Cursor cursor = getDownloadedDatasCursor();
		List<DownloadData> data = new ArrayList<DownloadData>();
		if(cursor != null){
			try {
				while (cursor.moveToNext()) {
					// 由于多线程下载会出现多条下载记录，因此把重复id项过滤
					boolean add = true;
					for (DownloadData d : data) {
						if (d.downloadId == cursor.getInt(0)) {
							add = false;
							break;
						}
					}
					if (add) {
						Theme theme = new Theme();
						theme.downloadId = cursor.getInt(0);
						theme.themeId = theme.downloadId;
						theme.name=cursor.getString(1);
						theme.downloadPath = cursor.getString(2);
						theme.version = (cursor.getString(3));
						theme.preview = (cursor.getString(4));
						theme.status = (cursor.getInt(5));
						theme.fileDir = (cursor.getString(6));
						theme.fileName = (cursor.getString(7));
						theme.finishTime = (cursor.getLong(8));
						theme.author = (cursor.getString(9));
//						theme.type = (cursor.getInt(9));
						data.add(theme);
					}
				}
			} finally {
				cursor.close();
			}
		}
		return data;
	}
	
	public List<DownloadData> getDownloadedDatasForRingToneType(int ringtone_type) {
		// TODO Auto-generated method stub
		Cursor cursor = getDownloadedDatasCursor(ringtone_type);
		List<DownloadData> data = new ArrayList<DownloadData>();
		if(cursor != null){
			try {
				while (cursor.moveToNext()) {
					// 由于多线程下载会出现多条下载记录，因此把重复id项过滤
					boolean add = true;
					for (DownloadData d : data) {
						if (d.downloadId == cursor.getInt(0)) {
							add = false;
							break;
						}
					}
					if (add) {
						ThemeAudio theme = new ThemeAudio();
						theme.downloadId = cursor.getInt(0);
						theme.themeId = theme.downloadId;
						theme.name=cursor.getString(1);
						theme.downloadPath = cursor.getString(2);
						theme.version = (cursor.getString(3));
						theme.preview = (cursor.getString(4));
						theme.status = (cursor.getInt(5));
						theme.fileDir = (cursor.getString(6));
						theme.fileName = (cursor.getString(7));
						theme.finishTime = (cursor.getLong(8));
						theme.author = (cursor.getString(9));
//						theme.type = (cursor.getInt(9));
						theme.ringtongType = (cursor.getInt(10));
						data.add(theme);
					}
				}
			} finally {
				cursor.close();
			}
		}
		return data;
	}
	
	public Cursor getDownloadedDatasCursor() {

		Cursor cursor = openDatabase().query(mTableName,
				new String[] { DbHelper.DOWNLOAD_DATA_ID,
				DbHelper.DOWNLOAD_DATA_NAME,
				DbHelper.DOWNLOAD_DATA_URL,
				DbHelper.DOWNLOAD_DATA_VERSION, DbHelper.DOWNLOAD_DATA_VERSION_CODE,
				DbHelper.STATUS, 
				DbHelper.FILE_DIR,
				DbHelper.FILE_NAME,
				DbHelper.FINISH_TIME,
				DbHelper.RINGTONE_AUTHOR},
				DbHelper.STATUS + ">=?",
						new String[] { FileDownloader.STATUS_APPLY_WAIT + "" }, null,
						null, null);
		return cursor;
	}
	
	/**
	 * 获取已完成列表
	 * 
	 * @return
	 */
	public Cursor getDownloadedDatasCursor(int ringtone_type) {
		Cursor cursor = openDatabase().query(mTableName,
				new String[] { DbHelper.DOWNLOAD_DATA_ID,
				DbHelper.DOWNLOAD_DATA_NAME,
				DbHelper.DOWNLOAD_DATA_URL,
				DbHelper.DOWNLOAD_DATA_VERSION, DbHelper.DOWNLOAD_DATA_VERSION_CODE,
				DbHelper.STATUS, 
				DbHelper.FILE_DIR,
				DbHelper.FILE_NAME,
				DbHelper.FINISH_TIME,
				DbHelper.RINGTONE_AUTHOR,
				DbHelper.RINGTONE_TYPE},
				DbHelper.STATUS + ">=? AND " + DbHelper.RINGTONE_TYPE + "=?",
						new String[] { FileDownloader.STATUS_APPLY_WAIT + "", ringtone_type + "" }, null,
						null, null);
		return cursor;
	}

}
