package com.android.phone;

 

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.provider.Telephony.Sms;
import android.net.Uri;
 

public class SMSObserver extends ContentObserver {
    public static final String TAG = "AuroraSMSObserver";
    private static final String[] PROJECTION = new String[]{    	
    	"body",       
        "reject",
        "_id"
    };
    
    SharedPreferences mSharedPreferences;
    
    private boolean mIsNotified = false;

    public SMSObserver()

    {
       super(new Handler());  
       mSharedPreferences = PhoneGlobals.getInstance().getSharedPreferences("sim_observer", Context.MODE_PRIVATE);
    }

    @Override
    public void onChange(boolean selfChange){

       Log.i(TAG, "onChange :");

       super.onChange(selfChange);
       
       if(!RejectUtils.isSupportBlack()) {
    	   return;
       }
       
//        Cursor cursor = PhoneGlobals.getInstance().getContentResolver().query(Uri.parse("content://sms"), PROJECTION, " reject in ('0', '1')", null, "_id DESC limit 0,1");
////        Cursor cursor = PhoneGlobals.getInstance().getContentResolver().query(Uri.parse("content://sms"), PROJECTION, "reject=? and type=?", new String[] { "1","1" }, "_id desc");
//        try {
//			if (cursor != null && cursor.getCount() > 0) {
//				cursor.moveToFirst();								
//		        int lastId = mSharedPreferences.getInt("last_id", -1);
//		        int currentId = cursor.getInt(2);
//				boolean isReject = cursor.getInt(1) == 1;
//			    Log.i(TAG, "onChange : id = " + currentId + " reject = " + isReject);
//			    saveLastId(currentId);
//		        if(lastId > currentId) {
//		        	return;
//		        } else if(lastId == currentId) {
//		        	if(mIsNotified) {
//		        		return;
//		        	}
//		        } else {
//		      		mIsNotified = false;
//		        }	
//				if(isReject) {
//					String body = cursor.getString(0);
//					PhoneGlobals.getInstance().notificationMgr.notifyHangupBlackSms();
//					mIsNotified = true;
//				}
//			} else {
//			    saveLastId(-1);
//			}
//		} finally {
//			if(cursor != null) {
//				cursor.close();
//			}
//		}
       
		PhoneGlobals.getInstance().mManageReject.notificationMgr.notifyHangupBlackSms();

    }

    private void saveLastId(int lastId) {
    	SharedPreferences.Editor editor =  mSharedPreferences.edit();		
      	editor.putInt("last_id",lastId);		              
  		editor.commit();
    }
}

 