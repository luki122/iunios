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

package com.android.phone;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
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
import android.text.TextUtils;
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
        int detectedType = intent.getIntExtra(
                SubscriptionManager.INTENT_KEY_DETECT_STATUS, 0);
        if (detectedType == SubscriptionManager.EXTRA_VALUE_NOCHANGE) {
            return;
        }
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

        /// M: for [C2K 2 SIM Warning]
        boolean newSimInserted = false;

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
                    /// M: for [C2K 2 SIM Warning]
                    newSimInserted = true;
                } else if (lastSubId != currentSubId) {
//                    createNotification(mContext);
                    setLastSubId(key, currentSubId);
                    notificationSent = true;
                    /// M: for [C2K 2 SIM Warning]
                    newSimInserted = true;
                }
                lastSubIdDetected = currentSubId;
                lastSIMSlotDetected = i;
                Log.d(TAG,"key = " + key + " lastSubId = " + lastSubId + 
                        " currentSubId = " + currentSubId + 
                        " lastSIMSlotDetected = " + lastSIMSlotDetected);
                
                if(TextUtils.isEmpty(sir.getDisplayName())) {
                	String displayName = AuroraPhoneUtils.getOperatorTitle(AuroraSubUtils.getSlotBySubId(mContext, currentSubId));
                	 if(!TextUtils.isEmpty(displayName)) {
                     	mSubscriptionManager.setDisplayName(displayName, currentSubId,
                                SubscriptionManager.NAME_SOURCE_SIM_SOURCE);
                	 }
                }
                
//                if(TextUtils.isEmpty(sir.getNumber())) {
//                    final TelephonyManager tm =
//                            (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
//                   String number = tm.getLine1NumberForSubscriber(currentSubId);
//                	 if(!TextUtils.isEmpty(number)) {
//                         mSubscriptionManager.setDisplayNumber(number, currentSubId);
//                	 }
//                }
     
            } else if (lastSubId != SLOT_EMPTY) {      
                setLastSubId(key, SLOT_EMPTY);
//                notificationSent = true;
            }
        }
        Log.d(TAG, "notificationSent = " + notificationSent + " numSIMsDetected = "
                + numSIMsDetected + " newSimInserted = " + newSimInserted);
        
        if(newSimInserted || notificationSent) {
        	if(numSIMsDetected > 1) {
        		mSubscriptionManager.setDefaultSmsSubId(SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        		mSubscriptionManager.setDefaultVoiceSubId(SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        	}
        }
        
        if (notificationSent && numSIMsDetected > 1) {

            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            if (numSIMsDetected == 1) {
//                intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, SimDialogActivity.PREFERRED_PICK);
//                intent.putExtra(SimDialogActivity.PREFERRED_SIM, lastSIMSlotDetected);
//            } else if (!isDefaultDataSubInserted()) {
//                intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, SimDialogActivity.DATA_PICK);
//            }
            intent.setClassName("com.android.phone",
                    "com.android.phone.SetSubscriptionDialog");
//            if (isEnableShowSimDialog()) {
           	if(isNeedNotifyNewCard()) {
                mContext.startActivity(intent);
        	}

//            }
        } 
        	        
        
        if(numSIMsDetected == 1) {
    		mSubscriptionManager.setDefaultDataSubId(lastSubIdDetected);
    		mSubscriptionManager.setDefaultSmsSubId(lastSubIdDetected);
    		mSubscriptionManager.setDefaultVoiceSubId(lastSubIdDetected);
        }
        /// M: for [C2K 2 SIM Warning] @{
//        if (newSimInserted) {
//            CdmaUtils.startCdmaWaringDialog(mContext, numSIMsDetected);
//        }
        /// @}
    }

    private int getLastSubId(String strSlotId) {
        return mSharedPreferences.getInt(strSlotId, INVALID_SLOT);
    }

    private void setLastSubId(String strSlotId, int value) {
        Editor editor = mSharedPreferences.edit();
        editor.putInt(strSlotId, value);
        editor.commit();
    }


    public static void cancelNotification(Context context) {
        NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
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

	private boolean isNeedNotifyNewCard() {
		boolean isStarted = true;
		try {
			ActivityManager am = (ActivityManager) PhoneGlobals.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
			ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
			Log.v(TAG, "topActiviy = " + cn.getClassName());
			if (cn.getClassName().equalsIgnoreCase(
					"com.android.phone.MSimMobileNetworkSettings")) {
				isStarted = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.v(TAG, "isNeedNotifyNewCard = " + isStarted);
		return isStarted;
	}

}
