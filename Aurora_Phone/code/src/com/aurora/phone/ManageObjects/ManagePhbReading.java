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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import com.android.internal.telephony.CallManager;

import android.os.AsyncResult;

public class ManagePhbReading {
	private static final String LOG_TAG = "ManagePhbReading";

	private PhoneGlobals mApp;

	public ManagePhbReading(PhoneGlobals app) {
		mApp = app;
	       
   	    IntentFilter phbFilter = new IntentFilter(AURORA_ACTION_PHB_LOAD_START); 
   	    phbFilter.addAction(AURORA_ACTION_PHB_LOAD_END);
   	    mApp.registerReceiver(mPhbLoadReceiver, phbFilter);  
	}
	
	  //aurora add liguangyu 20140918 for phb start
    private boolean mIsPhbLoadProcessing = false; 
    public boolean isInPhbLoadProcess() {
    	return mIsPhbLoadProcessing;
    }
    
    private static final String AURORA_ACTION_PHB_LOAD_START = "com.android.contacts.ACTION_PHB_LOAD_START";
    private static final String AURORA_ACTION_PHB_LOAD_END = "com.android.contacts.ACTION_PHB_LOAD_END";
    private final PhbLoadReceiver mPhbLoadReceiver = new PhbLoadReceiver();
    private class PhbLoadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	   String action = intent.getAction();
               Log.d(LOG_TAG, "PhbLoadReceiver");
               if (action.equals(AURORA_ACTION_PHB_LOAD_START)) {
                   Log.d(LOG_TAG, "PhbLoadReceiver ACTION_PHB_LOAD_START");
//                   int sub = intent.getIntExtra("slotId", 0);
                   mIsPhbLoadProcessing = true;
               } else if(action.equals(AURORA_ACTION_PHB_LOAD_END)) {
                   Log.d(LOG_TAG, "PhbLoadReceiver ACTION_PHB_LOAD_FINISHED");
//                   int sub = intent.getIntExtra("slotId", 0);
                   mIsPhbLoadProcessing = false;
               }
        }
    }
    //aurora add liguangyu 20140918 for phb end


	private void log(String msg) {
		Log.d(LOG_TAG, msg);
	}


}
