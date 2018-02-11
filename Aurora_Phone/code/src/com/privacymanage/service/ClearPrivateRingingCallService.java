package com.android.phone;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.provider.CallLog.Calls;
import android.util.*;

//add by liguangyu for black list 
public class ClearPrivateRingingCallService extends IntentService {
    private static final String TAG = "ClearPrivateRingingCallService";
    /** This action is used to clear missed calls. */
    public static final String ACTION_CLEAR_HANGUP_PRIVATE_RINGING_CALLS =
            "com.android.phone.intent.CLEAR_HANGUP_PRIVATE_RINGING_CALLS";
    
    public static final String ACTION_CLEAR_MISSED_CALLS =
            "com.android.phone.intent.CLEAR_MISSED_CALLS";
    

    private PhoneGlobals mApp;

    public ClearPrivateRingingCallService() {
        super(ClearPrivateRingingCallService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = PhoneGlobals.getInstance();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ACTION_CLEAR_HANGUP_PRIVATE_RINGING_CALLS.equals(intent.getAction())) {
        	Log.i(TAG, "ACTION_CLEAR_HANGUP_PRIVATE_RINGING_CALLS");
            mApp.mManagePrivate.notificationMgr.cancelHangupPrivateRingingCallNotification();
        } else  if (ACTION_CLEAR_MISSED_CALLS.equals(intent.getAction())) {
        	Log.i(TAG, "ACTION_CLEAR_MISSED_CALLS");
            // Clear the list of new missed calls.
        	Log.i(TAG, "ACTION_CLEAR_MISSED_CALLS update database");
            ContentValues values = new ContentValues();
            values.put(Calls.NEW, 0);
            values.put(Calls.IS_READ, 1);
            StringBuilder where = new StringBuilder();
            where.append(Calls.NEW);
            where.append(" = 1 AND ");
            where.append(Calls.TYPE);
            where.append(" = ?");
            where.append(" AND privacy_id = " + AuroraPrivacyUtils.getCurrentAccountId());
            getContentResolver().update(Calls.CONTENT_URI, values, where.toString(),
                    new String[]{ Integer.toString(Calls.MISSED_TYPE) });        
            mApp.mManagePrivate.notificationMgr.cancelPrivateMissedCallNotification();
        }
    }
}
