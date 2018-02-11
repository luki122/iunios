package com.android.server.telecom;

 

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
       mSharedPreferences = AuroraGlobals.getInstance().getSharedPreferences("sim_observer", Context.MODE_PRIVATE);
    }

    @Override
    public void onChange(boolean selfChange){

       Log.i(TAG, "onChange :");

       super.onChange(selfChange);
       
       if(!RejectUtils.isSupportBlack()) {
    	   return;
       }       
       
       AuroraGlobals.getInstance().mManageReject.notificationMgr.notifyHangupBlackSms();

    }

    private void saveLastId(int lastId) {
    	SharedPreferences.Editor editor =  mSharedPreferences.edit();		
      	editor.putInt("last_id",lastId);		              
  		editor.commit();
    }
}

 