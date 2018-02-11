package com.android.gallery3d.xcloudalbum.uploaddownload;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class DbUtil {
	
	public static final boolean DEBUG = false;
	public static final String TAG = "SQF_LOG";
	
	public static void debugCursor(Context context, Cursor cursor) {
		if(! DEBUG) {
			return;
		}
		int clmCount = cursor.getColumnCount();
		Log.i(TAG, " ===========================debugCursor BEGIN=================================== ");
		while(cursor.moveToNext()) {
			
			for(int columnIndex = 0; columnIndex <  clmCount; columnIndex ++) {
				String colName = cursor.getColumnName(columnIndex);
				switch(cursor.getType(columnIndex)) {
				case Cursor.FIELD_TYPE_INTEGER:
					int i = cursor.getInt(columnIndex);
					Log.i(TAG, cursor.getColumnName(columnIndex) + " " + i);
					/*
					if(colName.equals("_id")) {
						context.getContentResolver().delete(uri, "_id=?", new String[]{String.valueOf(i)});
					}
					*/
					
					break;
				case Cursor.FIELD_TYPE_STRING:
					String str = cursor.getString(columnIndex);
					Log.i(TAG, cursor.getColumnName(columnIndex) + ":" + str);
					break;
				case Cursor.FIELD_TYPE_FLOAT:
					float f = cursor.getFloat(columnIndex);
					Log.i(TAG, cursor.getColumnName(columnIndex) + ":" + f);
					break;
				case Cursor.FIELD_TYPE_BLOB:
					Log.i(TAG, cursor.getColumnName(columnIndex) + "blob");
					break;
				case Cursor.FIELD_TYPE_NULL:
					Log.i(TAG, cursor.getColumnName(columnIndex) + "null");
					break;
				}
			}
		}
		Log.i(TAG, " ===========================debugCursor END=================================== ");
	}
	
	
	public static void debugUri(Context context, Uri uri) {
		if(! DEBUG) {
			return;
		}
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		debugCursor(context, cursor);
	}
	
}
