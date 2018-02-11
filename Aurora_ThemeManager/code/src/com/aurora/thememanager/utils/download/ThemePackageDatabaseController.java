package com.aurora.thememanager.utils.download;

import java.util.ArrayList;
import java.util.List;

import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.utils.download.DatabaseController.DbHelper;
import com.aurora.utils.Log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class ThemePackageDatabaseController extends DatabaseController {

	public ThemePackageDatabaseController(Context context) {
		super(context);
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
						theme.versionCode = (cursor.getInt(4));
						theme.status = (cursor.getInt(5));
						theme.fileDir = (cursor.getString(6));
						theme.fileName = (cursor.getString(7));
						theme.finishTime = (cursor.getLong(8));
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

	
	@Override
	public boolean hasNewVersion(int downloadDataId) {
		// TODO Auto-generated method stub
		Cursor cursor = getUpdateCursor(downloadDataId);
		int state = 0;
		if(cursor != null){
			try {
				if( cursor.moveToFirst()){
					 state = cursor.getInt(cursor.getColumnIndex(DbHelper.STATUS));
				}
				
			} finally {
				cursor.close();
				cursor = null;
			}
		}
		return state == FileDownloader.STATUS_UPDATE;
	}
	
}
