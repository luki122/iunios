
package com.android.contacts.calllog;

import android.provider.CallLog.Calls;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.android.contacts.ContactsApplication;
import com.privacymanage.data.AidlAccountData;
import com.privacymanage.service.AuroraPrivacyUtils;

import android.database.ContentObserver;
import android.database.Cursor;
import android.os.*;
import android.net.Uri;



public class AuroraManagePrivate {
    private static final String LOG_TAG = "AuroraManagePrivate";
    
    private static final String ACTION_SWITCH = "com.aurora.privacymanage.SWITCH_ACCOUNT";
//    private static final String ACTION_DELETE = "com.aurora.privacymanage.DELETE_ACCOUNT";

    /** The singleton ManagedRoaming instance. */
    private static AuroraManagePrivate sInstance;

    private Context mContext;
    
    private PrivateCallLogCountObserver mCallLogObserver;

    public static AuroraManagePrivate init(Context context) {
        synchronized (AuroraManagePrivate.class) {
            if (sInstance == null) {
                sInstance = new AuroraManagePrivate(context);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            return sInstance;
        }
    }

    private AuroraManagePrivate(Context context) {
        mContext = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SWITCH);
        mContext.registerReceiver(mManagePrivateReceiver, filter);
        mCallLogObserver = new PrivateCallLogCountObserver();
           	
    }    
    
    public static AuroraManagePrivate getInstance() {
        return sInstance;
    }

    private BroadcastReceiver mManagePrivateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

    		
    		if (intent.getExtras() != null) {
    			String action = intent.getAction();
    			AidlAccountData account = intent.getParcelableExtra("account");
            	Log.i(LOG_TAG, "mManagePrivateReceiver  "
            			+ "onReceive action: " + action 
            			+ "  account id: " + account.getAccountId() 
            			+ "  path: " + account.getHomePath());
            	
    			if (action != null && action.equals("com.aurora.privacymanage.SWITCH_ACCOUNT")) {
    				if (account.getAccountId() > 0) {
    					updateCallLogNumber(account.getAccountId());
    					if(!isRegister) {
    						isRegister = true;
    						mContext.getContentResolver().registerContentObserver(Calls.CONTENT_URI, true, mCallLogObserver);
    					}
    				} else {
    					if(isRegister) {
    						isRegister = false;
    						mContext.getContentResolver().unregisterContentObserver(mCallLogObserver);
    					}
    				}
    			}
    		}
    	
        }
    };
    
    private class PrivateCallLogCountObserver extends ContentObserver {
    	
		public PrivateCallLogCountObserver(){
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {

			Log.i(LOG_TAG, "onChange :");
			super.onChange(selfChange);
			updateCallLogNumber(AuroraPrivacyUtils.getCurrentAccountId());
		}
		
    }
    
	private void updateCallLogNumber(long privacyid) {
    	Log.i(LOG_TAG, "updateCallLogNumber  ");
		Cursor cursor = ContactsApplication.getInstance().getContentResolver().query(Calls.CONTENT_URI, new String[]{"_id"}, 
				"privacy_id = " + privacyid, null, null);
		if (cursor != null) {
    		Log.i(LOG_TAG, "cursor.getCount()  = " + cursor.getCount());  
			AuroraPrivacyUtils.mPrivacyCallLogsNum = cursor.getCount();
		} else {
			AuroraPrivacyUtils.mPrivacyCallLogsNum = 0;
		}
		AuroraPrivacyUtils.setPrivacyNum(ContactsApplication.getInstance(),
				"com.android.contacts.activities.AuroraPrivateCallLogActivity", 
				AuroraPrivacyUtils.mPrivacyCallLogsNum, 
				privacyid);
		if(cursor != null) {
			cursor.close();
		}
	}
	
	public void updateCallLog() {
    	if (AuroraPrivacyUtils.mCurrentAccountId > 0) {
			updateCallLogNumber(AuroraPrivacyUtils.mCurrentAccountId);
			if(!isRegister) {
				isRegister = true;
				mContext.getContentResolver().registerContentObserver(Calls.CONTENT_URI, true, mCallLogObserver);
			}
		}
	}
	
	 
	private boolean isRegister = false;
    
}