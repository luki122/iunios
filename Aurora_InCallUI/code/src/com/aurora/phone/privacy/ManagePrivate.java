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

package com.android.incallui;  

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
import android.telecom.TelecomManager;



public class ManagePrivate {
    private static final String LOG_TAG = "ManagePrivate";

    // Key used to read and write the saved network selection numeric value
    private static final String BIND_SERVICE = "com.aurora.privacymanage.";
    private static final String ACTION_SWITCH = "com.aurora.privacymanage.SWITCH_ACCOUNT";
    private static final String ACTION_DELETE = "com.aurora.privacymanage.DELETE_ACCOUNT";

    /** The singleton ManagedRoaming instance. */
    private static ManagePrivate sInstance;

    private Context mContext;     
    
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
        mHandler = new Handler();
    }    

    private BroadcastReceiver mManagePrivateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
    		
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
    	        	
    			} else if (action != null && action.equals("com.aurora.privacymanage.DELETE_ACCOUNT")) {
    				boolean delete = intent.getBooleanExtra("delete", false);
    				AuroraPrivacyUtils.mIsPrivacyMode = false;
    			}
    			
    			AuroraPrivacyUtils.killPrivacyActivity();
    		}
    	
        }
    };

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
    private Handler mHandler;    
    

    
    
 
}
