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

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.UserHandle;
import android.util.Log;

import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.PhoneConstants;

import android.os.AsyncResult;

public class ManageReject extends Handler{
	private static final String LOG_TAG = "ManageReject";

	private PhoneGlobals mApp;
	RejectNotificationMgr notificationMgr;
	private CallManager mCM;
	private static final int PHONE_STATE_CHANGED = 1;

	public ManageReject(PhoneGlobals app) {
		mApp = app;
		mCM = app.mCM;
		if(RejectUtils.isSupportBlack()) {
			mSmsObserver = new SMSObserver();
			mPduObserver = new PduObserver();
			IntentFilter blackNotificationFilter = new IntentFilter(
					AURORA_BLACK_NOTIFICATION_ACTION);
			blackNotificationFilter.addAction(AURORA_NOTIFICATION_CANCEL_ACTION);
			mApp.registerReceiver(mBNReceiver, blackNotificationFilter);
	        SharedPreferences sp = mApp.getSharedPreferences("com.android.phone_preferences", Context.MODE_PRIVATE);  
			isBlackNoticationEnable = sp
					.getBoolean("isBlackNoticationEnable", true);
			if (isBlackNoticationEnable && RejectUtils.isSupportBlack()) {
				mApp.getContentResolver().registerContentObserver(
						Uri.parse("content://reject/notify"), true, mSmsObserver);
			}
			IntentFilter blackFilter = new IntentFilter(
					"android.intent.action.MY_BROADCAST");
			mApp.registerReceiver(mBlackClearReceiver, blackFilter);	
		}
        notificationMgr = new RejectNotificationMgr(app);
		mCM.registerForPreciseCallStateChanged(this, PHONE_STATE_CHANGED,null);
	}
	
	public void setNotificationMgr(RejectNotificationMgr mgr) {
		notificationMgr = mgr;
	}
	
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case PHONE_STATE_CHANGED:
		     PhoneConstants.State state = mCM.getState();
		     if(state == PhoneConstants.State.IDLE) {
		    	 RejectUtils.reset();
		     }
			break;
		}
	}

	protected ContentObserver mSmsObserver, mPduObserver;

	private static final String AURORA_BLACK_NOTIFICATION_ACTION = "com.android.reject.BLACK_MSG_REJECT";
	private static final String AURORA_NOTIFICATION_CANCEL_ACTION = "com.android.reject.NOTIFICATION_CANCEL";
	private final BlackNotificationReceiver mBNReceiver = new BlackNotificationReceiver();
	public static boolean isBlackNoticationEnable = true;

	private class BlackNotificationReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(AURORA_BLACK_NOTIFICATION_ACTION)) {
				isBlackNoticationEnable = intent.getBooleanExtra(
						"isRejectBlack", true);
				SharedPreferences sp = mApp.getSharedPreferences(
						"com.android.phone_preferences", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("isBlackNoticationEnable",
						isBlackNoticationEnable);
				editor.commit();
				if (isBlackNoticationEnable) {
					context.getContentResolver().registerContentObserver(
							Uri.parse("content://reject/notify"), true,
							mSmsObserver);
				} else {
					context.getContentResolver().unregisterContentObserver(
							mSmsObserver);
				}
			} else if (action.equals(AURORA_NOTIFICATION_CANCEL_ACTION)) {
				Intent clearIntent = new Intent(mApp,
						ClearBlackCallsService.class);
				clearIntent
						.setAction(ClearBlackCallsService.ACTION_CLEAR_HANGUP_BLACK_CALLS);
				context.startService(clearIntent);
			}
		}
	}

	private final BlackClearReceiver mBlackClearReceiver = new BlackClearReceiver();

	private class BlackClearReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(LOG_TAG, "BlackClearReceiver ");
			if (action.equals("android.intent.action.MY_BROADCAST")) {
				Log.d(LOG_TAG, "BlackClearReceiver 1");
				Intent clearIntent = new Intent(mApp,
						ClearBlackCallsService.class);
				clearIntent
						.setAction(ClearBlackCallsService.ACTION_CLEAR_HANGUP_BLACK_CALLS);
				context.startService(clearIntent);
			}
		}
	}

	private void log(String msg) {
		Log.d(LOG_TAG, msg);
	}
	
	
	 public static class RejectBroadcastReceiver extends BroadcastReceiver {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            String action = intent.getAction();
	            // TODO: use "if (VDBG)" here.
	            Log.d(LOG_TAG, "Broadcast from Notification: " + action);
	            if(action.equals(ACTION_ADD_BLACK_NOTIFICATION)) {
	            	
	            	int id = intent.getIntExtra("id", -1);
	            	String number = intent.getStringExtra("number");
	            	String name = intent.getStringExtra("name");            	
	                closeSystemDialogs(context);
	                cancelAddBlackNotification(context, id, number);
	                Intent addBlackIntent = createAddBlackIntentInternal(number, name);
	                context.startActivity(addBlackIntent);
	                
	            } else if (action.equals(ACTION_HANGUP_BLACK_NOTIFICATION)) {
	            	
	                closeSystemDialogs(context);
	                cancelHangupBlackCallNotification(context);
	                Intent hangupBlackIntent = createGotoRejectIntentInternal();
	                context.startActivity(hangupBlackIntent);
	             
	            } else {
	                Log.w(LOG_TAG, "Received hang-up request from notification,"
	                        + " but there's no call the system can hang up.");
	            }
	        }

	        private void closeSystemDialogs(Context context) {
	            Intent intent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
	            context.sendBroadcastAsUser(intent, UserHandle.ALL);
	        }

	        private void cancelAddBlackNotification(Context context, int id, String number) {
	        	Intent clearIntent = new Intent(context, ClearBlackCallsService.class);
	            clearIntent.setAction(ClearBlackCallsService.ACTION_CLEAR_ADD_BLACK);
	            clearIntent.putExtra("id", id);
	            clearIntent.putExtra("number", number);
	            context.startService(clearIntent);
	        }
	        
	        private void cancelHangupBlackCallNotification(Context context) {
	        	Intent clearIntent = new Intent(context, ClearBlackCallsService.class);
	            clearIntent.setAction(ClearBlackCallsService.ACTION_CLEAR_HANGUP_BLACK_CALLS);
	            context.startService(clearIntent);
	        }
	        
	    }
	
	
	 private static final String ACTION_ADD_BLACK_NOTIFICATION = "com.android.phone.ACTION_ADD_BLACK_NOTIFICATION";
	 private static final String ACTION_HANGUP_BLACK_NOTIFICATION = "com.android.phone.ACTION_HANGUP_BLACK_NOTIFICATION";

    public static PendingIntent createAddBlackIntent(Context context, int id, String number, String name) {
        Intent intent = new Intent(ACTION_ADD_BLACK_NOTIFICATION, null, context, RejectBroadcastReceiver.class);
        intent.putExtra("id", id);
        intent.putExtra("number", number);
        intent.putExtra("name", name);
        return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);    	    
     }
    
	private static Intent createAddBlackIntentInternal(String number, String name) {    	
		  Intent intent = new Intent(Intent.ACTION_MAIN, null);
		  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
		        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		  intent.setClassName("com.aurora.reject", "com.aurora.reject.AuroraManuallyAddActivity");
		  Bundle bundle = new Bundle();
		  bundle.putString("add_number", number); 
		  bundle.putString("add_name", name);
		  bundle.putBoolean("add", true);
		  intent.putExtras(bundle);
	      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		  return intent;
	  }
	  
    public static PendingIntent createGotoRejectIntent(Context context) {  
        Intent intent = new Intent(ACTION_HANGUP_BLACK_NOTIFICATION, null, context, RejectBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);   	    	
    }
	    
    private static Intent createGotoRejectIntentInternal() {  
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
              | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.putExtra("goToSms", RejectNotificationMgr.getBlackNotificationMode() == 2);
        intent.putExtra("goToCall", RejectNotificationMgr.getBlackNotificationMode() == 1);
        if(RejectNotificationMgr.getBlackNotificationMode() == 3) {
            intent.putExtra("all", true);	
        }
        intent.setClassName("com.aurora.reject", "com.aurora.reject.AuroraRejectActivity");
	    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;	    	
    }

}
