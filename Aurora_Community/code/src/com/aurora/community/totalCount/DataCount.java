/*
 * @author zw
 */
package com.aurora.community.totalCount;



import com.aurora.community.utils.Log;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;


public class DataCount {
	
	private DataCount() {
	}
	private final static String TAG = "DataCount";
	public static ContentResolver resolver = null;
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i(TAG,"zhangwei the module="+module_id+" the action="+action_id+" the count="+count);
		return count;
	}
	
	
}