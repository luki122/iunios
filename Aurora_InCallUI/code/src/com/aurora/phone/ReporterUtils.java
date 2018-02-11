package com.android.incallui;


import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.CallLog;
import android.text.TextUtils;
import android.util.Log;

public class ReporterUtils {
    private static final String LOG_TAG = "ReporterUtils";
    
	private final static Uri report_uri = Uri.parse("content://com.iuni.reporter/module/");	
	
    private static final String MODULE = "module_key"; 
    private static final String ITEM = "item_tag"; 
    private static final String VALUE = "value"; 
    
    private static final String module_key = "100";     
     
//    通话录音的开启状态
    private static final String item_record = "002";  
//    “设置”的点击频次 
    private static final String item_call_settings = "003"; 
	
    public static void addRecordCount(){
  		Log.v(LOG_TAG, "addRecordCount");
    	addInternal(item_record);
    }
    	
    public static int getRecordCount() {
    	return queryInternal(item_record);
    }
    
    public static void addSettingCount(){
  		Log.v(LOG_TAG, "addSettingCount");
    	addInternal(item_call_settings);
    }
    	
    public static int getSettingCount() {
    	return queryInternal(item_call_settings);
    }
    
    private static void addInternal(String item){
       	ContentValues values = new ContentValues(3);
        values.put(MODULE, module_key);
        values.put(ITEM, item);
        values.put(VALUE, Integer.valueOf(1));
        int id = InCallApp.getInstance().getContentResolver().update(report_uri, values, null, null);
		Log.v(LOG_TAG, " id =" + id);
    }
    
    private static int queryInternal(String item){
    	int count = -2;	
		Cursor cursor = InCallApp.getInstance().getContentResolver().query(report_uri,
				null,
				MODULE + " = '"  + module_key + "' and " + ITEM + " = '" + item + "'" ,
				null,
				null);	 
			
    	if (cursor != null){
    		if(cursor.moveToFirst()){
    			count =  cursor.getInt(cursor.getColumnIndex(VALUE)); 
        		Log.v(LOG_TAG, " count =" + count);
    		}   
    		cursor.close();  
    	}	
		
    	return count ;
    }
	
}
