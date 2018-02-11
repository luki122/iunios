/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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


package com.android.mms.transaction;

import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.provider.Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION;

import com.android.mms.data.Contact;
import com.android.mms.ui.ClassZeroActivity;
import com.android.mms.util.Recycler;
import com.android.mms.util.SendingProgressTokenManager;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.mediatek.wappush.pushparser.ParsedMessage;
import com.mediatek.wappush.pushparser.Parser;
import com.mediatek.wappush.WapPushManager;

//import android.database.sqlite.SqliteWrapper;

import aurora.app.AuroraActivity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Threads;
import android.provider.Telephony.Sms.Inbox;
import android.provider.Telephony.Sms.Intents;
import android.provider.Telephony.Sms.Outbox;
import android.provider.Telephony;
import gionee.provider.GnTelephony;
import android.telephony.ServiceState;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.gionee.internal.telephony.GnPhone;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.TelephonyIntents;
import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.LogTag;
import com.android.mms.util.ThreadCountManager;

import com.aurora.featureoption.FeatureOption;
//Gionee zengxuanhui 20120914 add for CR00692293 begin
import android.os.SystemProperties;
import gionee.provider.GnTelephony.SIMInfo;
//Gionee zengxuanhui 20120914 add for CR00692293 end
/**
 * This service essentially plays the role of a "worker thread", allowing us to store
 * incoming messages to the database, update notifications, etc. without blocking the
 * main thread that WapPushReceiver runs on.
 */
public class WapPushReceiverService extends Service {
    private static final String TAG = "Mms/WapPush";

    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    private boolean mSending;

    public static boolean sSmsSent = true;

    private int mResultCode;

    //Gionee zengxuanhui 20120914 add for CR00692293 begin
    private static final boolean gnGeminiRingtoneSupport = SystemProperties.get("ro.gn.gemini.ringtone.support").equals("yes");
    //Gionee zengxuanhui 20120914 add for CR00692293 end
    
    @Override
    public void onCreate() {
        // Temporarily removed for this duplicate message track down.
//        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
//            Log.v(TAG, "onCreate");
//        }

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Temporarily removed for this duplicate message track down.

        mResultCode = intent != null ? intent.getIntExtra("result", 0) : 0;

        if (mResultCode != 0) {
            Log.v(TAG, "onStart: #" + startId + " mResultCode: " + mResultCode +
                    " = " + translateResultCode(mResultCode));
        }

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
        return Service.START_NOT_STICKY;
    }

    private static String translateResultCode(int resultCode) {
        switch (resultCode) {
            case AuroraActivity.RESULT_OK:
                return "AuroraActivity.RESULT_OK";
            default:
                return "Unknown error code";
        }
    }

    @Override
    public void onDestroy() {
        // Temporarily removed for this duplicate message track down.
//        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
//            Log.v(TAG, "onDestroy");
//        }
        mServiceLooper.quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        /**
         * Handle incoming transaction requests.
         * The incoming requests are initiated by the MMSC Server or by the MMS Client itself.
         */
        @Override
        public void handleMessage(Message msg) {
            int serviceId = msg.arg1;
            Intent intent = (Intent)msg.obj;
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "handleMessage serviceId: " + serviceId + " intent: " + intent);
            }
            if (intent != null) {
                String action = intent.getAction();

                int error = intent.getIntExtra("errorCode", 0);

                if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                    Log.v(TAG, "handleMessage action: " + action + " error: " + error);
                }

                if (WAP_PUSH_RECEIVED_ACTION.equals(action)) {
                    handleWapPushReceived(intent, error);
                } else if (ACTION_BOOT_COMPLETED.equals(action)) {
                    handleBootCompleted();
                } 
            }
            
            sSmsSent = true;
            // NOTE: We MUST not call stopSelf() directly, since we need to
            // make sure the wake lock acquired by AlertReceiver is released.
            WapPushReceiver.finishStartingService(WapPushReceiverService.this, serviceId);
        }
    }


    private void handleWapPushReceived(Intent intent, int error) {
        
        if(!FeatureOption.MTK_WAPPUSH_SUPPORT){
            return;
        }
        
        Log.i(TAG, "handleWapPushReceived: " + intent.getAction());
        
        //get info from intent
        String mimeType = intent.getType();
        byte[] intentData = intent.getByteArrayExtra("data");
        byte []headers = intent.getByteArrayExtra("header");
        
        //get sender and service center address from intent
        String sender = intent.getStringExtra(GnTelephony.WapPush.ADDR);
        String service_center = intent.getStringExtra(GnTelephony.WapPush.SERVICE_ADDR);
        
        //get sim id
        int simId = intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY,-1);
        //Gionee zengxuanhui 20120914 add for CR00692293 begin
        int slotId = SIMInfo.getSlotById(getApplicationContext(), simId);
        if(slotId == 1){
            WapPushMessagingNotification.setIncomingSmsSimId(GnPhone.GEMINI_SIM_2);
        }else{
            WapPushMessagingNotification.setIncomingSmsSimId(GnPhone.GEMINI_SIM_1);
        }
        //Gionee zengxuanhui 20120914 add for CR00692293 end
        //sender = "+8613888888888";
        //service_center = "+8613812345678";
        Log.i(TAG, "handleWapPushReceived: " + sender + service_center + " " + simId);
        if(sender == null || service_center == null){
            Log.e(TAG, "handleWapPushReceived: sender or service center is null!");
            return;
        }
        
        /*
         * Parse the Wap Push Message
         */
        Parser parser = Parser.createParser(mimeType);
        ParsedMessage pushMsg = null;
        WapPushManager manager = null;
        
        if(parser!=null){
            pushMsg = parser.parseData(intentData);
        }else{
            Log.e(TAG,"Wap Push message parse create error!");
        }
        
        if(pushMsg != null){
            Log.i(TAG,pushMsg.toString());
            manager = WapPushManager.createManager(this,pushMsg.type());
        }else{
            Log.e(TAG,"Wap Push Message parseData error!");
        }
        
        if(manager!=null){
            
            pushMsg.setSenderAddr(sender);
            pushMsg.setServiceCenterAddr(service_center);
            pushMsg.setSimId(simId);
            
            manager.handleIncoming(pushMsg);
        }
        
        Recycler.getWapPushRecycler().deleteOldMessages(getApplicationContext());
        
//        SmsMessage[] msgs = Intents.getMessagesFromIntent(intent);
//        Uri messageUri = insertMessage(this, msgs, error);
//
//        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
//            SmsMessage sms = msgs[0];
//            Log.v(TAG, "handleSmsReceived" + (sms.isReplace() ? "(replace)" : "") +
//                    " messageUri: " + messageUri +
//                    ", address: " + sms.getOriginatingAddress() +
//                    ", body: " + sms.getMessageBody());
//        }
//
//        if (messageUri != null) {
//            // Called off of the UI thread so ok to block.
//            MessagingNotification.blockingUpdateNewMessageIndicator(this, true, false);
//        } else {
//            SmsMessage sms = msgs[0];
//            SmsMessage msg = SmsMessage.createFromPdu(sms.getPdu());
//            CharSequence messageChars = msg.getMessageBody();
//            String message = messageChars.toString();
//            if (!TextUtils.isEmpty(message)) {
//                MessagingNotification.notifyClassZeroMessage(this, msgs[0]
//                        .getOriginatingAddress());
//            }
//        }
    }

    private void handleBootCompleted() {
        //moveOutboxMessagesToQueuedBox();
        //sendFirstQueuedMessage();

        // Called off of the UI thread so ok to block.
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            WapPushMessagingNotification.blockingUpdateNewMessageIndicator(this, false);
        }
    }

    private void registerForServiceStateChanges() {
        Context context = getApplicationContext();
        unRegisterForServiceStateChanges();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED);
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "registerForServiceStateChanges");
        }

        context.registerReceiver(WapPushReceiver.getInstance(), intentFilter);
    }

    private void unRegisterForServiceStateChanges() {
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "unRegisterForServiceStateChanges");
        }
        try {
            Context context = getApplicationContext();
            context.unregisterReceiver(WapPushReceiver.getInstance());
        } catch (IllegalArgumentException e) {
            // Allow un-matched register-unregister calls
        }
    }
}


