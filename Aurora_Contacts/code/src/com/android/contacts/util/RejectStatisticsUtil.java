package com.android.contacts.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class RejectStatisticsUtil {
    private static final String LOG_TAG = "ReporterUtils";
	private final static Uri report_uri = Uri.parse("content://com.iuni.reporter/module/");	
    private static final String MODULE = "module_key"; 
    private static final String ITEM = "item_tag"; 
    private static final String VALUE = "value"; 
    private static final String module_key = "180";     

    private static final String item1 = "addbl";  
    private static final String item2 = "deletebl"; 
    private static final String item3 = "addmark";  
    private static final String item4 = "deletemark"; 
    private static final String item5 = "antisms";  
    private static final String item6 = "notification"; 
    private static final String item7 = "restoresms";  
    private static final String item8 = "fromnotfc"; 
    
    public static void addBlackCount(Context context,int count){
    	addInternal(item1,context,count);
    }
    	

    
    public static void deleteBlackCount(Context context,int count){
    	addInternal(item2,context,count);
    }
    	

    
    public static void addMarkCount(Context context,int count){
    	addInternal(item3,context,count);
    }
    	

    
    public static void deleteMarkCount(Context context,int count){
    	addInternal(item4,context,count);
    }
    	

    
    public static void smsSwithCount(Context context,int count){
    	addInternal(item5,context,count);
    }
    	

    
    public static void notifSwithCount(Context context,int count){
    	addInternal(item6,context,count);
    }
    	

    
    
    public static void restorSmsCount(Context context,int count){
    	addInternal(item7,context,count);
    }
    	
 
    
    
    public static void fromNotiCount(Context context,int count){
    	addInternal(item8,context,count);
    }
    	
   
    
	private static void addInternal(final String item, final Context context,
			final int count) {
		new Thread() {
			@Override
			public void run() {
				ContentValues values = new ContentValues(3);
				values.put(MODULE, module_key);
				values.put(ITEM, item);
				values.put(VALUE, count);
				int id = context.getContentResolver().update(report_uri,
						values, null, null);
				Log.v(LOG_TAG, " id =" + id);

				Cursor cursor = context.getContentResolver().query(
						report_uri,
						null,
						MODULE + " = '" + module_key + "' and " + ITEM + " = '"
								+ item + "'", null, null);

				if (cursor != null) {
					if (cursor.moveToFirst()) {
						Log.v(LOG_TAG," count ="+ cursor.getInt(cursor.getColumnIndex(VALUE)));
					}
					cursor.close();
				}
			}
		}.start();

	}
    
    
	
}
