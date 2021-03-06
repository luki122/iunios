/*
 * @author zw
 */
package com.aurora.reject.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;


public class DataCount {
	
	private DataCount() {
	}
	
	public static ContentResolver resolver = null;
	private static String TAG = "DataCount";
	public final static Uri m_uri = Uri.parse("content://com.iuni.reporter/module/");
	public static ContentResolver getResolver(Context context)
	{
		if(null == resolver)
			resolver = context.getContentResolver();
		return resolver;
		
	}

	public static int updataData(Context context,String module_id,String action_id,int value)
	{
		int count = 0;
		resolver = getResolver(context);
		ContentValues values = new ContentValues();
		values.put("module_key", module_id);
		values.put("item_tag", action_id);
		values.put("value", value);
		try {
			count = resolver.update(m_uri, values, null, null);
		} catch (SQLiteException ex) {
			Log.e(TAG ,"SQLiteException: " + ex.getMessage());
			return 0;
		} catch (Exception ex) {
			Log.e(TAG ,"Exception: " + ex.getMessage());
			return 0;
		}
		
		
		Cursor cursor = context.getContentResolver().query(
				m_uri,
				null,
				"module_key" + " = '" + module_id + "' and " + "item_tag" + " = '"
						+ action_id + "'", null, null);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				Log.v("qiaohu"," count ="+ cursor.getInt(cursor.getColumnIndex("value")));
			}
			cursor.close();
		}
		return count;
	}
	
	
}