package com.android.phone;

 

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.provider.Telephony.Mms;
 

public class PduObserver extends ContentObserver {
    public static final String TAG = "AuroraPduObserver";
    private static final String[] PROJECTION = new String[]{    	
    	"sim_id",       
        "reject",
        "_id"
    };

    int mLastId = -1;
    SharedPreferences mSharedPreferences;
    
    private boolean mIsNotified = false;

    public PduObserver()

    {
       super(new Handler()); 
       mSharedPreferences = PhoneGlobals.getInstance().getSharedPreferences("mms_observer", Context.MODE_PRIVATE);
    }

    @Override
    public void onChange(boolean selfChange){

       Log.i(TAG, "onChange : ");

       super.onChange(selfChange);
       
       if(!RejectUtils.isSupportBlack()) {
    	   return;
       }
       
        Cursor cursor = PhoneGlobals.getInstance().getContentResolver().query(Mms.CONTENT_URI, PROJECTION, " reject in ('0', '1')", null, Mms.DEFAULT_SORT_ORDER + " limit 0,1");
        try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();	
		        int lastId = mSharedPreferences.getInt("last_id", -1);
		        int currentId = cursor.getInt(2);	
				boolean isReject = cursor.getInt(1) == 1;
			    Log.i(TAG, "onChange : currentId = " + currentId + " reject = " + isReject);
			    saveLastId(currentId);
		        if(lastId > currentId) {
		        	return;
		        } else if(lastId == currentId) {
		        	if(mIsNotified) {
		        		return;
		        	}
		        } else {
		      		mIsNotified = false;
		        }	
				if(isReject) {
					String subTitle = cursor.getString(0);
					PhoneGlobals.getInstance().mManageReject.notificationMgr.notifyHangupBlackSms();	
					mIsNotified = true;
				}
			} else {
			    saveLastId(-1);
			}
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}

    }

    
    private void saveLastId(int lastId) {
    	SharedPreferences.Editor editor =  mSharedPreferences.edit();		
      	editor.putInt("last_id",lastId);		              
  		editor.commit();
    }
}

 