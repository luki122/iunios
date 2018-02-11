package com.aurora.thememanager.utils.download;

import java.util.ArrayList;
import java.util.List;

import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.utils.download.DatabaseController.DbHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class DatabaseUpdateController extends DatabaseController {

	public DatabaseUpdateController(Context context, String tableName) {
		super(context,DbHelper.DOWNLOAD_DATA_AUTOUPDATE_TABLE);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void insert(DownloadData data, long createTime, long status,
			long filesize) {
		// TODO Auto-generated method stub

	}

	@Override
	public DownloadData getDownloadData(int downloadDataId) {
		// TODO Auto-generated method stub
		Cursor cursor = getDownloadDataCursor(downloadDataId);
		if(cursor != null){
			try {
				
			} finally {
				cursor.close();
			}
		}
		return null;
	}

	@Override
	public List<DownloadData> getAppliedDatas() {
		Cursor cursor = getAppliedDatasCursor();
		if(cursor != null){
			try {
				
			} finally {
				cursor.close();
			}
		}
		return null;
	}

	@Override
	public List<DownloadData> getDownloadedDatas() {
		// TODO Auto-generated method stub
		Cursor cursor = getDownloadedDatasCursor();
		if(cursor != null){
			try {
				
			} finally {
				cursor.close();
			}
		}
		return null;
	}
	
	
	
	
	
	@Override
	public boolean hasNewVersion(int downloadDataId) {
		// TODO Auto-generated method stub
		Cursor cursor = getUpdateCursor(downloadDataId);
		int state = 0;
		if(cursor != null){
			try {
				if( cursor.moveToFirst()){
					 state = cursor.getInt(cursor.getColumnIndex(DbHelper.HAS_NEW_VERSION));
				}
				
			} finally {
				cursor.close();
				cursor = null;
			}
		}
		return state == 1;
	}
	
	
	
	@Override
	public void updateNewVersionState(int downloadId, int state) {
		// TODO Auto-generated method stub
		if(!databaseIsOpened()){
			openDatabase();
		}
		ContentValues values = new ContentValues();
		values.put(DbHelper.DOWNLOAD_DATA_ID, downloadId);
		values.put(DbHelper.HAS_NEW_VERSION, state);
		insert(values);
	}
	
	
	
	
	
	
	

}
