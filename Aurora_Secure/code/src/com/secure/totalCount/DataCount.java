/*
 * @author zw
 */
package com.secure.totalCount;



import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.secure.utils.LogUtils;

public class DataCount {
	
	private final static String TAG = "DataCount";
	public static ContentResolver resolver = null;
	public final static Uri m_uri = Uri.parse("content://com.iuni.reporter/module/");
	
	public static final String MODULE_KEY = "200";
	
	
	public static ContentResolver getResolver(Context context)
	{
		if(null == resolver)
			resolver = context.getContentResolver();
		return resolver;
		
	}

	public static int updataData(Context context,String action_id,int value)
	{
		int count = 0;
		resolver = getResolver(context);
		ContentValues values = new ContentValues();
		values.put("module_key", MODULE_KEY);
		values.put("item_tag", action_id);
		values.put("value", value);

		count = resolver.update(m_uri, values, null, null);
		LogUtils.printWithLogCat(TAG,"zhangwei the action="+action_id+" the count="+count);
		return count;
	}
	
	
}