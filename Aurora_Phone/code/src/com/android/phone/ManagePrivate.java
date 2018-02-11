/*
 * Copyright (c) 2013 The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.android.phone;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import com.privacymanage.data.AidlAccountData;

import android.database.ContentObserver;
import android.database.Cursor;
import android.os.*;
import android.net.Uri;
import android.provider.Telephony;



public class ManagePrivate {
    private static final String LOG_TAG = "ManagePrivate";

    // Key used to read and write the saved network selection numeric value
    private static final String BIND_SERVICE = "com.aurora.privacymanage.";
    private static final String ACTION_SWITCH = "com.aurora.privacymanage.SWITCH_ACCOUNT";
    private static final String ACTION_DELETE = "com.aurora.privacymanage.DELETE_ACCOUNT";

    /** The singleton ManagedRoaming instance. */
    private static ManagePrivate sInstance;

    private Context mContext;
    
    private ContentObserver mSmsObserver;
    
    PrivateNotificationMgr notificationMgr;
    
    private long mCurrentPrivateId = 0;

    static ManagePrivate init(Context context) {
        synchronized (ManagePrivate.class) {
            if (sInstance == null) {
                sInstance = new ManagePrivate(context);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            return sInstance;
        }
    }

    private ManagePrivate(Context context) {
        mContext = context;
        mCurrentPrivateId = AuroraPrivacyUtils.getCurrentAccountId();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SWITCH);
        filter.addAction(ACTION_DELETE);
        mContext.registerReceiver(mManagePrivateReceiver, filter);
        AuroraPrivacyUtils.bindService(mContext);
        mSmsObserver = new PrivateSMSObserver();
        mContext.getContentResolver().registerContentObserver(Uri.parse("content://privacy/notify"), true, mSmsObserver);	
        notificationMgr = PrivateNotificationMgr.init(PhoneGlobals.getInstance());
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable(){
        	public void run() {
                notificationMgr.updateNotificationsWhenGoToPrivateMode();
        	}
        }, 3000);
    }    

    private BroadcastReceiver mManagePrivateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(ACTION_SWITCH)) {
//            	AidlAccountData account = (AidlAccountData)intent.getParcelableExtra("account");
//            	long newId = account.getAccountId();
//            	if(mCurrentPrivateId != newId) {
//            		mCurrentPrivateId = newId;
//            		OnchangeWhenSwitch();
//            	}
//            	
//            } else if (intent.getAction().equals(ACTION_DELETE)) {
//            	AidlAccountData account = (AidlAccountData)intent.getParcelableExtra("account");
////            	true：删除隐私空间数据，false：还原隐私空间数据
//            	boolean delete = intent.getBooleanExtra("delete", true);
//            }
        	
        	

    		
    		if (intent.getExtras() != null) {
    			String action = intent.getAction();
    			AidlAccountData account = intent.getParcelableExtra("account");
            	Log.i(LOG_TAG, "PrivacyAccountChangeReceiver  "
            			+ "onReceive action: " + action 
            			+ "  account id: " + account.getAccountId() 
            			+ "  path: " + account.getHomePath());
            	
    			if (action != null && action.equals("com.aurora.privacymanage.SWITCH_ACCOUNT")) {
    	        	AuroraPrivacyUtils.mCurrentAccountId = account.getAccountId();
    	        	AuroraPrivacyUtils.mCurrentAccountHomePath = account.getHomePath();
    	        	
    	        	if (AuroraPrivacyUtils.mCurrentAccountId > 0) {
    	        		AuroraPrivacyUtils.mIsPrivacyMode = true;
    	        	} else {
    	        		AuroraPrivacyUtils.mIsPrivacyMode = false;
    	        	}
            		OnchangeWhenSwitch();
    	        	
    			} else if (action != null && action.equals("com.aurora.privacymanage.DELETE_ACCOUNT")) {
    				boolean delete = intent.getBooleanExtra("delete", false);
    				AuroraPrivacyUtils.mIsPrivacyMode = false;
    		        mHandler.postDelayed(new Runnable(){
    		        	public void run() {
    	    			 	PhoneGlobals.getInstance().notificationMgr.cancelMissedCallNotification();
    	    			 	PhoneGlobals.getInstance().notificationMgr.updateNotificationsAtStartup();
    		        	}
    		        }, 1000);
    			}
    			
    			AuroraPrivacyUtils.killPrivacyActivity();
    		}
    	
        }
    };

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
    
    public class PrivateSMSObserver extends ContentObserver {
		public final String TAG = "AuroraSMSObserver";
		private final String[] PROJECTION = new String[] {"date", "is_privacy" };


		private boolean mIsNotified = false;

		public PrivateSMSObserver()

		{
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {

			Log.i(LOG_TAG, "onChange :");
			super.onChange(selfChange);
			
			 long privateIdSms = 0;   
			
			 long dateSms = 0;
	        Cursor cursor = PhoneGlobals.getInstance().getContentResolver().query(Telephony.Sms.Inbox.CONTENT_URI, PROJECTION, " reject in ('0', '1') and is_privacy > -1", null, "_id DESC limit 0,1");
	        try {
				if (cursor != null && cursor.moveToFirst()) {
					dateSms = cursor.getLong(0);
					privateIdSms = cursor.getLong(1);
				}
			} finally {
				if(cursor != null) {
					cursor.close();
				}
			}
	        
			 long privateIdMms = 0;  
	        long dateMms = 0;
	        cursor = PhoneGlobals.getInstance().getContentResolver().query(Telephony.Mms.Inbox.CONTENT_URI, PROJECTION, " reject in ('0', '1') and is_privacy > -1", null, "_id DESC limit 0,1");	        
	        try {
				if (cursor != null && cursor.moveToFirst()) {
					dateMms = cursor.getLong(0);
					privateIdMms = cursor.getLong(1);
				}
			} finally {
				if(cursor != null) {
					cursor.close();
				}
			}
	        
	        long privateId = 0;
	        if(dateMms >  dateSms) {
	        	privateId = privateIdMms;
	        } else {
	        	privateId = privateIdSms;
	        }
			
			if(AuroraPrivacyUtils.isPrivateSendSms(privateId)) {
				notificationMgr.notifyHangupPrivateRingingCallFake(privateId);
			}

		}
    }
    
    
    private void OnchangeWhenSwitch() {
    	notificationMgr.cancelAllNotification();
    }
    
    private Handler mHandler;
    
    
    public static class PrivateBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // TODO: use "if (VDBG)" here.
            Log.d(LOG_TAG, "Broadcast from Notification: " + action);

            if(action.equals(ACTION_HANGUP_PRIVATE_RINGING_NOTIFICATION)){
            	
                closeSystemDialogs(context);
                cancelHangupPrivateRingingCallNotification(context);
                Intent hangupPrivateRingingIntent = createGotoPrivateCalllogIntentInternal();
                context.startActivity(hangupPrivateRingingIntent);
            } else if(action.equals(ACTION_HANGUP_PRIVATE_RINGING_NOTIFICATION_FAKE)){ 
                cancelHangupPrivateRingingCallNotification(context);            
            } else {
                Log.w(LOG_TAG, "Received hang-up request from notification,"
                        + " but there's no call the system can hang up.");
            }
        }

        private void closeSystemDialogs(Context context) {
            Intent intent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
   
        
        private void cancelHangupPrivateRingingCallNotification(Context context) {
        	Intent clearIntent = new Intent(context, ClearPrivateRingingCallService.class);
            clearIntent.setAction(ClearPrivateRingingCallService.ACTION_CLEAR_HANGUP_PRIVATE_RINGING_CALLS);
            context.startService(clearIntent);
        }
    }
    

    
    
    
    private static final String ACTION_HANGUP_PRIVATE_RINGING_NOTIFICATION = "com.android.phone.ACTION_HANGUP_PRIVATE_RINGING_NOTIFICATION";
    private static final String ACTION_HANGUP_PRIVATE_RINGING_NOTIFICATION_FAKE = "com.android.phone.ACTION_HANGUP_PRIVATE_RINGING_NOTIFICATION_FAKE";
    
    public static PendingIntent createGotoPrivateCalllogIntentFake(Context context) {  
        Intent intent = new Intent(ACTION_HANGUP_PRIVATE_RINGING_NOTIFICATION_FAKE, null, context, PrivateBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);   	    	
    }
    
    public static PendingIntent createGotoPrivateCalllogIntent(Context context) {  
        Intent intent = new Intent(ACTION_HANGUP_PRIVATE_RINGING_NOTIFICATION, null, context, PrivateBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);   	    	
    }
	    
    private static Intent createGotoPrivateCalllogIntentInternal() {  
        Intent intent = new Intent("com.aurora.privacymanage.GOTO_ CONTACT_PRIVACY_MODULE");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
              | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
	    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;	    	
    }    
}
