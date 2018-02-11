/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.phone;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.android.internal.telephony.Phone;
import aurora.preference.*;
import aurora.widget.*;
//aurora change liguangyu 20131111 for BUG #581 start
public class Use2GOnlyCheckBoxPreference extends AuroraSwitchPreference {
//aurora change liguangyu 20131111 for BUG #581 end
    private static final String LOG_TAG = "Use2GOnlyCheckBoxPreference";

    private Phone mPhone;
    private MyHandler mHandler;
    AuroraSwitch mAuroraSwitch;

    public Use2GOnlyCheckBoxPreference(Context context) {
        this(context, null);
    }

    public Use2GOnlyCheckBoxPreference(Context context, AttributeSet attrs) {
    	//aurora change liguangyu 20131111 for BUG #581 start
//        this(context, attrs,com.android.internal.R.attr.checkBoxPreferenceStyle);
    	   this(context, attrs, com.android.internal.R.attr.switchPreferenceStyle);
        //aurora change liguangyu 20131111 for BUG #581 end
    }

    public Use2GOnlyCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPhone = PhoneGlobals.getPhone();
        mHandler = new MyHandler();
        mPhone.getPreferredNetworkType(
                mHandler.obtainMessage(MyHandler.MESSAGE_GET_PREFERRED_NETWORK_TYPE));
    }

    @Override
    protected void  onClick() {
        super.onClick();
	     Log.i(LOG_TAG, "onClick");
        int networkType = isChecked() ? Phone.NT_MODE_GSM_ONLY : Phone.NT_MODE_WCDMA_PREF;
        Log.i(LOG_TAG, "set preferred network type="+networkType);
        android.provider.Settings.Global.putInt(mPhone.getContext().getContentResolver(),
                android.provider.Settings.Global.PREFERRED_NETWORK_MODE, networkType);
        mPhone.setPreferredNetworkType(networkType, mHandler
                .obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE));
   }

    private class MyHandler extends Handler {

        static final int MESSAGE_GET_PREFERRED_NETWORK_TYPE = 0;
        static final int MESSAGE_SET_PREFERRED_NETWORK_TYPE = 1;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_GET_PREFERRED_NETWORK_TYPE:
                    handleGetPreferredNetworkTypeResponse(msg);
                    break;

                case MESSAGE_SET_PREFERRED_NETWORK_TYPE:
                    handleSetPreferredNetworkTypeResponse(msg);
                    break;
            }
        }

        private void handleGetPreferredNetworkTypeResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;

            if (ar.exception == null) {
                int type = ((int[])ar.result)[0];
                if (type != Phone.NT_MODE_GSM_ONLY) {
                    // Allow only NT_MODE_GSM_ONLY or NT_MODE_WCDMA_PREF
                    type = Phone.NT_MODE_WCDMA_PREF;
                }
                Log.i(LOG_TAG, "get preferred network type="+type);
                setChecked(type != Phone.NT_MODE_GSM_ONLY);
                //aurora add liguangyu 20131118 start
                if(mAuroraSwitch!=null) {
	                mAuroraSwitch.setChecked(type != Phone.NT_MODE_GSM_ONLY);
	                mAuroraSwitch.invalidate();
                }
                //aurora add liguangyu 20131118 end
                android.provider.Settings.Global.putInt(mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE, type);
            } else {
                // Weird state, disable the setting
                Log.i(LOG_TAG, "get preferred network type, exception="+ar.exception);
                setEnabled(false);
            }
        }

        private void handleSetPreferredNetworkTypeResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;

            if (ar.exception != null) {
                // Yikes, error, disable the setting
                setEnabled(false);
                // Set UI to current state
                Log.i(LOG_TAG, "set preferred network type, exception=" + ar.exception);
                mPhone.getPreferredNetworkType(obtainMessage(MESSAGE_GET_PREFERRED_NETWORK_TYPE));
            } else {
                Log.i(LOG_TAG, "set preferred network type done");
            }
        }
    }
    
    //aurora add liguangyu 20131111 for BUG #581 start
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);        
        mAuroraSwitch  = (AuroraSwitch)view.findViewById(com.aurora.R.id.aurora_switchWidget);        
        if(mAuroraSwitch != null){        	        	
        	mAuroraSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {       		
         		@Override
         		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         			int networkType = !isChecked ? Phone.NT_MODE_GSM_ONLY : Phone.NT_MODE_WCDMA_PREF;
         	        Log.i(LOG_TAG, "onCheckedChanged set preferred network type= " + networkType);
         	        android.provider.Settings.Global.putInt(mPhone.getContext().getContentResolver(),
         	                android.provider.Settings.Global.PREFERRED_NETWORK_MODE, networkType);
         	        mPhone.setPreferredNetworkType(networkType, mHandler
         	                .obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE));
         		}         	
         	});        	 
        }      

    }
    //aurora add liguangyu 20131111 for BUG #581 end    
    
    public static void updateCheckBox(Phone phone) {
    	
    }
    
    public static void updatePhone(Phone phone) {
    	
    }
}
