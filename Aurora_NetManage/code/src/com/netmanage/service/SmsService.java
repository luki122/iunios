package com.netmanage.service;

import com.netmanage.model.CorrectFlowBySmsModel;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class SmsService extends Service {
	 private SmsObserver mObserver;
	 
	//对这个url进行监听时，在收件箱与发件箱数据库改变时，都会回调监听事件
	 private Uri msgUri = Uri.parse("content://sms");
	 private static String TAG = SmsService.class.getName();
	 private static long lastReadMsgTime = 0;
	 
	 @Override
	 public IBinder onBind(Intent intent) {
	     return null;
	 }

	 @Override
	 public void onCreate() {
	     ContentResolver resolver = getContentResolver();  
	     mObserver = new SmsObserver(resolver, null);
	     resolver.registerContentObserver(msgUri, true, mObserver);
	 }

	 @Override
	 public void onDestroy() {
	     super.onDestroy();
	     this.getContentResolver().unregisterContentObserver(mObserver);
	 }
	 
	 static class SmsObserver extends ContentObserver{
		  private ContentResolver mResolver;
		 
		  public SmsObserver(ContentResolver mResolver,Handler handler) {
			 super(handler);
			 this.mResolver=mResolver;
		  }
		 
		  @Override
		  public void onChange(boolean selfChange) {
			 Cursor mCursor = null;
			 try{
				 mCursor=mResolver.query(Uri.parse("content://sms/inbox"), 
			    		 new String[] {"address", "body","date"}, 
			    		 "read=?", 
			    		 new String[] { "0" }, 
			    		 "date desc");
			 }catch(Exception e){
				 e.printStackTrace();
			 }	
			 if (mCursor != null){
	    		if(mCursor.moveToFirst()){
	    			String address = null,body = null;
					long msgTime = 0;
					
					int addressIndex=mCursor.getColumnIndex("address");
			        if(addressIndex!=-1) {
			            address=mCursor.getString(addressIndex);
			        }
			    
			        int bodyIndex=mCursor.getColumnIndex("body");
			        if(bodyIndex!=-1) {
			        	body=mCursor.getString(bodyIndex);
			        } 
			          
			        int dateIndex = mCursor.getColumnIndex("date");
			        if(dateIndex != -1){
			        	  msgTime = mCursor.getLong(dateIndex);
			        }
			        Log.i(TAG,"msgTime="+msgTime+",lastReadMsgTime="+lastReadMsgTime);
			        if(msgTime != lastReadMsgTime){
			        	lastReadMsgTime = msgTime;
			            CorrectFlowBySmsModel model = CorrectFlowBySmsModel.getInstance();
						if(model != null){
						    model.dealSmsFromSqlite(address, body);  
						}
			        }			          			          			          
	    		}	    		
	    		mCursor.close();      			     			    
	    	 }
		}
	 }
}