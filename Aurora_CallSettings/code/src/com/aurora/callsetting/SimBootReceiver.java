/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.util.Log;

import java.util.List;

public class SimBootReceiver extends BroadcastReceiver {
    private static final String TAG = "SimBootReceiver";
    private static final int SLOT_EMPTY = -1;
    private static final int NOTIFICATION_ID = 1;
    private static final String SHARED_PREFERENCES_NAME = "sim_state";
    private static final String SLOT_PREFIX = "sim_slot_";
    private static final int INVALID_SLOT = -2; // Used when upgrading from K to LMR1

    private SharedPreferences mSharedPreferences = null;
    private TelephonyManager mTelephonyManager;
    private Context mContext;
    private SubscriptionManager mSubscriptionManager;
    /// @}
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive()... action: " + intent.getAction());
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        mContext = context;
        mSubscriptionManager = SubscriptionManager.from(mContext);
        mSharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
//      mSubscriptionManager.addOnSubscriptionsChangedListener(mSubscriptionListener);
        detectChangeAndNotify();
    }

    private void detectChangeAndNotify() {
        final int numSlots = mTelephonyManager.getSimCount();
        final boolean isInProvisioning = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.DEVICE_PROVISIONED, 0) == 0;
        boolean notificationSent = false;
        int numSIMsDetected = 0;
        int lastSIMSlotDetected = -1;
        int lastSubIdDetected = -1;
        Log.d(TAG,"detectChangeAndNotify numSlots = " + numSlots + 
                " isInProvisioning = " + isInProvisioning);
        // Do not create notifications on single SIM devices or when provisiong.
        if (numSlots < 2 || isInProvisioning) {
            return;
        }

        // We wait until SubscriptionManager returns a valid list of Subscription informations
        // by checking if the list is empty.
        // This is not completely correct, but works for most cases.
        // See Bug: 18377252
        List<SubscriptionInfo> sil = mSubscriptionManager.getActiveSubscriptionInfoList();
        if (sil == null || sil.size() < 1) {
            Log.d(TAG,"do nothing since no cards inserted");
            return;
        }

        for (int i = 0; i < numSlots; i++) {
            final SubscriptionInfo sir = Utils.findRecordBySlotId(mContext, i);
            Log.d(TAG,"sir = " + sir);
            final String key = SLOT_PREFIX+i;
            final int lastSubId = getLastSubId(key);
            if (sir != null) {
                numSIMsDetected++;
                final int currentSubId = sir.getSubscriptionId();
                if (lastSubId == INVALID_SLOT) {
                    setLastSubId(key, currentSubId);
                } else if (lastSubId != currentSubId) {
                    setLastSubId(key, currentSubId);
                    notificationSent = true;
                }
                lastSubIdDetected = currentSubId;
                lastSIMSlotDetected = i;
                Log.d(TAG,"key = " + key + " lastSubId = " + lastSubId + 
                        " currentSubId = " + currentSubId + 
                        " lastSIMSlotDetected = " + lastSIMSlotDetected);
            } else if (lastSubId != SLOT_EMPTY) {      
                setLastSubId(key, SLOT_EMPTY);
                notificationSent = true;
            }
        }
        Log.d(TAG,"notificationSent = " + notificationSent + " numSIMsDetected = " + numSIMsDetected);
        
        
        if(mSubscriptionManager.getDefaultDataSubId() != lastSubIdDetected) {
        	notificationSent = true;
        }
        
        if (notificationSent) {
//            Intent intent = new Intent(mContext, SimDialogActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (numSIMsDetected == 1) {
//                intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, SimDialogActivity.PREFERRED_PICK);
//                intent.putExtra(SimDialogActivity.PREFERRED_SIM, lastSIMSlotDetected);
                mSubscriptionManager.setDefaultDataSubId(lastSubIdDetected);
                mSubscriptionManager.setDefaultSmsSubId(lastSubIdDetected);
                TelephonyUtils.setTelecomPreferSub(lastSubIdDetected);
                mSubscriptionManager.setDefaultVoiceSubId(lastSubIdDetected);
//            } else if (!isDefaultDataSubInserted()) {
//                intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, SimDialogActivity.DATA_PICK);
            }
//            mContext.startActivity(intent);
        }
        
       if(numSIMsDetected == numSlots && numSlots > 1) {
	        int lastSubId0 = getLastSubId(SLOT_PREFIX + 0);
	        int lastSubId1 = getLastSubId(SLOT_PREFIX + 1);
	        if(mSubscriptionManager.getDefaultDataSubId() != lastSubId0 && mSubscriptionManager.getDefaultDataSubId() != lastSubId1){
	            mSubscriptionManager.setDefaultDataSubId(lastSubId0);
	            mSubscriptionManager.setDefaultSmsSubId(lastSubId0);
	            TelephonyUtils.setTelecomPreferSub(lastSubId0);
	            mSubscriptionManager.setDefaultVoiceSubId(lastSubId0);
	        }
       }
        
    }

    private int getLastSubId(String strSlotId) {
        return mSharedPreferences.getInt(strSlotId, INVALID_SLOT);
    }

    private void setLastSubId(String strSlotId, int value) {
        Editor editor = mSharedPreferences.edit();
        editor.putInt(strSlotId, value);
        editor.commit();
    }


    private final OnSubscriptionsChangedListener mSubscriptionListener =
            new OnSubscriptionsChangedListener() {
        @Override
        public void onSubscriptionsChanged() {
            detectChangeAndNotify();
        }
    };

    private boolean isDefaultDataSubInserted() {
        boolean isInserted = false;
        int defaultDataSub = SubscriptionManager.getDefaultDataSubId();
        if (SubscriptionManager.isValidSubscriptionId(defaultDataSub)) {
            final int numSlots = mTelephonyManager.getSimCount();
            for (int i = 0; i < numSlots; ++i) {
                final SubscriptionInfo sir = Utils.findRecordBySlotId(mContext, i);
                if (sir != null) {
                    if (sir.getSubscriptionId() == defaultDataSub) {
                        isInserted = true;
                        break;
                    }
                }
            }
        }
        Log.d(TAG, "defaultDataSub: " + defaultDataSub + ", isInsert: " + isInserted);
        return isInserted;
    }

}
