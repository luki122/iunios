package com.android.email;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class EmailDataCount {	
	public static ContentResolver resolver = null;
	public final static String EMAIL_PREF = "Email_Preference";
	public final static String ACCOUNT_URI = "aurora_account_uri";
	
	public final static String data_uri = "content://com.iuni.reporter/module/";
	public final static Uri m_uri = Uri.parse(data_uri);
	
	public final static String AURORA_EMAIL_MODEL_ID = "130";
	public final static String AURORA_EMAIL_ACCOUNT_ID = "130034";
	public final static String AURORA_EMAIL_OPEN_ID = "130035";
	public final static String AURORA_EMAIL_SEND_ID = "130036";
	
	public final static String MODULE_KEY = "module_key";
	public final static String ITEM_TAG = "item_tag";
	public final static String VALUE = "value";
	public static String[] projection = {MODULE_KEY,ITEM_TAG,VALUE};
	
	public static ContentResolver getResolver(Context context)
	{
		if(null == resolver)
			resolver = context.getContentResolver();
		return resolver;
		
	}

	public static String updataData(Context context,String action_id,int value)
	{
		int count = -1;
		resolver = getResolver(context);
		ContentValues values = new ContentValues();
		values.put(MODULE_KEY, AURORA_EMAIL_MODEL_ID);
		values.put(ITEM_TAG, action_id);
		values.put(VALUE, value);

		count = resolver.update(m_uri, values, null, null);
//		return count;
		return getUriString(count);
	}
	
	public static String getUriString(int count){
		return data_uri.concat(String.valueOf(count));
	}
	
	//query Account count Right now
	public static int queryAccountsCount(Context context,String request_item_tag,String uriString){
		Uri uri = null;
		if(!TextUtils.isEmpty(uriString))
			uri = Uri.parse(uriString);
		else
			uri = m_uri;
		int count = 0;
		String model_id = new String();
		String item_tag = new String();
		resolver = getResolver(context);
		
		Cursor cursor = resolver.query(uri, projection, null, null, null);
		if(cursor != null && cursor.getCount() > 0){
			cursor.moveToFirst();
			do{
				model_id = cursor.getString(cursor.getColumnIndex(MODULE_KEY));
				item_tag = cursor.getString(cursor.getColumnIndex(ITEM_TAG));
	//			Log.w("haozi","model_id ="+model_id);
	//			Log.w("haozi","item_tag ="+item_tag);
				if(model_id.equalsIgnoreCase(AURORA_EMAIL_MODEL_ID)
						&& item_tag.equalsIgnoreCase(request_item_tag)){
					count = cursor.getInt(cursor.getColumnIndex(VALUE));
					break;
				}
			}while(cursor.moveToNext());
		}
		return count;
	}
	
	//delete data recode of SQLITE before update 
	public static int deleteData(Context context,String uriString){
		int count = 0;
		if(TextUtils.isEmpty(uriString))
			return count;
		Uri uri = Uri.parse(uriString);
		resolver = getResolver(context);
		count = resolver.delete(uri, null, null);
		return count;
	}
	
	public static void editSharePreference(Context context,String uri){
		if(TextUtils.isEmpty(uri))
			return;
		SharedPreferences pref = context.getSharedPreferences(EMAIL_PREF, context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putString(ACCOUNT_URI, uri);
		editor.commit();
	}
	
	public static String getAccountCountUri(Context context){
		SharedPreferences pref = context.getSharedPreferences(EMAIL_PREF, context.MODE_PRIVATE);
		return pref.getString(ACCOUNT_URI, null);
	}
}
