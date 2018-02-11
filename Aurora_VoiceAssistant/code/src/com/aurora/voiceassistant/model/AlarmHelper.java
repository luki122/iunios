package com.aurora.voiceassistant.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.aurora.voiceassistant.model.BaseHelper.BaseHelperEvent;

public class AlarmHelper extends BaseHelper implements BaseHelperEvent{
	
	private  final String CONTENT_URI = "content://com.android.deskclock/alarm";
	
	private  final String KEY_ALARM_HOUR = "hour";
	
	private  final String KEY_ALARM_MINUTES = "minutes";
	
	private  final String KEY_ALARM_ENABLED = "enabled";
	
	private final  String KEY_ALARM_DAYSOFWEEK = "daysofweek";
	
	private final  String KEY_ALARM_VIBRATE = "vibrate";
	
	private final  String KEY_ALARM_MESSAGE = "message";
	
	public String Hour;
	
	public String Minutes;
	
	public String Enable;
	
	public String DaysofWeek;
	
	public String Vibrate;
	
	public String  Message;
	
	
	public AlarmHelper(Context context){
		mContext = context;
	}


	@Override
	public void scheduleNewEvent() {
		// TODO Auto-generated method stub
		if(DEBUG)
			printAlarmProviderContent();
		
		Uri uri = Uri.parse(CONTENT_URI);
		 ContentValues values = new ContentValues();
		 values.put(KEY_ALARM_HOUR, Hour);
		 values.put(KEY_ALARM_MINUTES, Minutes);
		 values.put(KEY_ALARM_DAYSOFWEEK, DaysofWeek);
		 values.put(KEY_ALARM_ENABLED, Enable);
		 values.put(KEY_ALARM_VIBRATE, Vibrate);
		 values.put(KEY_ALARM_MESSAGE, Message);
		 mContext.getContentResolver().insert(uri, values);
	}
	
	
	private void printAlarmProviderContent(){
		 Uri uri = Uri.parse(CONTENT_URI);
		 Cursor c = this.mContext.getContentResolver().query(uri, null, null, null, null);
		 if(null!=c){
			 Log.i(TAG, "printAlarmProviderContent cursor getCount = " + c.getCount());
			 Log.i(TAG, "printAlarmProviderContent  columns  = " + c.getColumnCount());
			 String names[] = c.getColumnNames();
			 for(String columnName:names){
				 Log.i(TAG, "printAlarmProviderContent each column Name = "+columnName);
			 }
			 /**print record*/
		     if (c.moveToFirst()) {
	         do {
	             for (int j = 0; j < c.getColumnCount(); j++) {
	                 Log.i(TAG, c.getColumnName(j)
	                         + " which  value =  " + c.getString(j));
	             }
	         } while (c.moveToNext());
	     }
		 }

	}
	
}
