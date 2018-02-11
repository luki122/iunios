package com.aurora.thememanager.utils.download;

import java.util.List;

import android.content.Context;
import android.database.Cursor;

public class DatabaseUpdateController extends DatabaseController {

	public DatabaseUpdateController(Context context, String tableName) {
		super(context, tableName);
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

}
