/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aurora.callsetting;


import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.Phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.TelephonyIntents;



/**
 * Global state for the telephony subsystem when running in the primary
 * phone process.
 */
public class PhoneGlobals extends ContextWrapper implements PhoneServiceStateHandler.Listener{
	 static final String LOG_TAG = "PhoneGlobals";
	static final int DBG_LEVEL = 3;
	
    protected static PhoneGlobals sMe;
    

    public PhoneGlobals(Context context) {
        super(context);
        sMe = this;
    }
	
	 public static PhoneGlobals getInstance() {
	        if (sMe == null) {
	            throw new IllegalStateException("No PhoneGlobals here!");
	        }
	        return sMe;
	    }
	
	    public void onCreate() {
	    	  mSubscriptionManager = SubscriptionManager.from(this);
	    	  mStateHandler = new PhoneServiceStateHandler(this);
	    	     IntentFilter ddsIntentFilter = new IntentFilter(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
	             registerReceiver(mDdsReceiver, ddsIntentFilter);
	    }
	 
	 public static Phone getPhone() {
	        return getPhone(0);
	    }

	   public static Phone getPhone(int subscription) {
	    	return PhoneFactory.getPhone(subscription);
	    }
	   
	   public Phone getPhone(long subscription) {
	       // PhoneGlobals: discard the subscription.
		   return getPhone((int)subscription);
	   }
	   
	    public void setDataEnableAfterSwitch(boolean value) {
	    	mIsEnableDataWhenSwitchDone = value;
	    	needDataSwitch = true;
	    }
	   
	    private SubscriptionManager mSubscriptionManager;
	   private PhoneServiceStateHandler mStateHandler;
		private boolean needDataSwitch = false;
		private boolean mIsEnableDataWhenSwitchDone = false;
	    
	    public void onServiceStateChanged(ServiceState state, int subId) {
	        Log.d(LOG_TAG, "PhoneStateListener:onServiceStateChanged: subId: " + subId
	                + ", state: " + state);
	        if(needDataSwitch) {
	        	if(subId == mSubscriptionManager.getDefaultDataSubId()) {
	        		if(state.getState() == ServiceState.STATE_IN_SERVICE) {
	        			Log.d(LOG_TAG, "PhoneStateListener:onServiceStateChanged done");
	        			TelephonyManager mTelephonyManager = TelephonyManager.from(this);
	        			mTelephonyManager.setDataEnabled(subId, mIsEnableDataWhenSwitchDone);
	        			needDataSwitch = false;
	        		}
	        	}
	        }
	    }
	    
	    private BroadcastReceiver mDdsReceiver = new BroadcastReceiver(){

	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	int slot = mSubscriptionManager.getDefaultDataPhoneId();
	    		Log.d(LOG_TAG, "mDdsReceiver slot = " + slot);
	        	if (slot > -1) {
	    			AuroraNetworkUtils.setPreferredNetworkMode(Constants.NETWORK_MODE_LTE_GSM_WCDMA, slot);
	        	   getPhone(slot).setPreferredNetworkType(Constants.NETWORK_MODE_LTE_GSM_WCDMA, null);
	        	}
	        }
	    
	    };
	    
}
