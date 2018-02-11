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

import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.RateController;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.GenericPdu;
import com.aurora.android.mms.pdu.NotificationInd;
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.android.mms.pdu.PduParser;
import com.aurora.android.mms.pdu.PduPersister;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.gionee.internal.telephony.GnPhone;
import com.android.internal.telephony.TelephonyIntents;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import gionee.provider.GnTelephony.Sms;
import gionee.provider.GnTelephony;
import gionee.provider.GnTelephony.MmsSms.PendingMessages;
import gionee.provider.GnTelephony.SIMInfo;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import gionee.net.GnConnectivityManager;
import android.net.NetworkInfo;
import gionee.net.GnNetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.text.TextUtils;
//add for 81452
import android.telephony.PhoneStateListener;
import gionee.telephony.GnTelephonyManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephony.Stub;

// add for gemini
import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.Settings;
import gionee.provider.GnSettings.System;
import android.database.sqlite.SqliteWrapper;
import com.aurora.featureoption.FeatureOption;

import com.gionee.internal.telephony.GnTelephonyManagerEx;
// Aurora xuyong 2014-11-10 added for bug #9329 start
import android.os.SystemProperties;
import com.android.internal.telephony.Phone;
// Aurora xuyong 2014-11-10 added for bug #9329 end
//gionee gaoj 2012-3-22 added for CR00555790 start
import android.os.Bundle;
import android.provider.Telephony.ThreadsColumns;
import com.gionee.mms.popup.PopUpUtils;
import com.android.mms.data.Conversation;
//gionee gaoj 2012-3-22 added for CR00555790 end
/**
 * The TransactionService of the MMS Client is responsible for handling requests
 * to initiate client-transactions sent from:
 * <ul>
 * <li>The Proxy-Relay (Through Push messages)</li>
 * <li>The composer/viewer activities of the MMS Client (Through intents)</li>
 * </ul>
 * The TransactionService runs locally in the same process as the application.
 * It contains a HandlerThread to which messages are posted from the
 * intent-receivers of this application.
 * <p/>
 * <b>IMPORTANT</b>: This is currently the only instance in the system in
 * which simultaneous connectivity to both the mobile data network and
 * a Wi-Fi network is allowed. This makes the code for handling network
 * connectivity somewhat different than it is in other applications. In
 * particular, we want to be able to send or receive MMS messages when
 * a Wi-Fi connection is active (which implies that there is no connection
 * to the mobile data network). This has two main consequences:
 * <ul>
 * <li>Testing for current network connectivity ({@link android.net.NetworkInfo#isConnected()} is
 * not sufficient. Instead, the correct test is for network availability
 * ({@link android.net.NetworkInfo#isAvailable()}).</li>
 * <li>If the mobile data network is not in the connected state, but it is available,
 * we must initiate setup of the mobile data connection, and defer handling
 * the MMS transaction until the connection is established.</li>
 * </ul>
 */
public class TransactionService extends Service implements Observer {
    private static final String TAG = "Mms/TransactionService";

    /**
     * Used to identify notification intents broadcasted by the
     * TransactionService when a Transaction is completed.
     */
    public static final String TRANSACTION_COMPLETED_ACTION =
            "android.intent.action.TRANSACTION_COMPLETED_ACTION";

    /**
     * Action for the Intent which is sent by Alarm service to launch
     * TransactionService.
     */
    public static final String ACTION_ONALARM = "android.intent.action.ACTION_ONALARM";

    /**
     * Used as extra key in notification intents broadcasted by the TransactionService
     * when a Transaction is completed (TRANSACTION_COMPLETED_ACTION intents).
     * Allowed values for this key are: TransactionState.INITIALIZED,
     * TransactionState.SUCCESS, TransactionState.FAILED.
     */
    public static final String STATE = "state";

    /**
     * Used as extra key in notification intents broadcasted by the TransactionService
     * when a Transaction is completed (TRANSACTION_COMPLETED_ACTION intents).
     * Allowed values for this key are any valid content uri.
     */
    public static final String STATE_URI = "uri";

    /**
     * Used to identify notification intents broadcasted by the
     * TransactionService when a Transaction is Start. add for gemini smart 
     */
    public static final String TRANSACTION_START = "com.android.mms.transaction.START";

    /**
     * Used to identify notification intents broadcasted by the
     * TransactionService when a Transaction is Stop. add for gemini smart
     */
    public static final String TRANSACTION_STOP = "com.android.mms.transaction.STOP";

    private static final int EVENT_TRANSACTION_REQUEST = 1;
    private static final int EVENT_DATA_STATE_CHANGED = 2;
    private static final int EVENT_CONTINUE_MMS_CONNECTIVITY = 3;
    private static final int EVENT_HANDLE_NEXT_PENDING_TRANSACTION = 4;
    //add for time out mechanism
    private static final int EVENT_PENDING_TIME_OUT = 5;
    //add for 81452
    private static final int EVENT_SCAN_PENDING_MMS = 6;
    private static final int EVENT_QUIT = 100;

    private static final int TOAST_MSG_QUEUED = 1;
    private static final int TOAST_DOWNLOAD_LATER = 2;
    private static final int TOAST_NONE = -1;

    private static final int FAILE_TYPE_PERMANENT = 1;
    private static final int FAILE_TYPE_TEMPORARY = 2;

    private static final int REQUEST_SIM_NONE = -1;

    // temp for distinguish smart switch or dialog
    private static final boolean SMART = true;

    // M: this is used for test only.
    public static boolean sServiceAlive = false;
    // 
    private boolean bWaitingConxn = false;

    //avoid stop TransactionService incorrectly.
    private boolean mNeedWait = false; 
    
    // How often to extend the use of the MMS APN while a transaction
    // is still being processed.
    private static final int APN_EXTENSION_WAIT = 8 * 30 * 1000;

    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    private final ArrayList<Transaction> mProcessing  = new ArrayList<Transaction>();
    private final ArrayList<Transaction> mPending  = new ArrayList<Transaction>();
    private ConnectivityManager mConnMgr;
    private ConnectivityBroadcastReceiver mReceiver;
    private PowerManager.WakeLock mWakeLock;

    //add for 81452
    private PhoneStateListener mPhoneStateListener;
    private PhoneStateListener mPhoneStateListener2;
    //phone state in single mode, in gemini mode slot0 state
    private int mPhoneState = TelephonyManager.CALL_STATE_IDLE;
    //phone state of slot1 in gemini mode
    private int mPhoneState2 = TelephonyManager.CALL_STATE_IDLE;
    private Object mPhoneStateLock = new Object();
    private int mLastIdleSlot = GnPhone.GEMINI_SIM_1;
    private Object mIdleLock = new Object();
    private boolean mEnableCallbackIdle = false;

    private long triggerMsgId = 0;
    //Add for time out mechanism
    private final long REQUEST_CONNECTION_TIME_OUT_LENGTH = 3*60*1000;
    //this member is used to ignore status message sent by framework between time out happened and
    //a new data connection request which need wait.
    private boolean mIgnoreMsg = false;

    private int mMaxServiceId = Integer.MIN_VALUE;
    
    // add for gemini
    private int mSimIdForEnd = 0;

    // for handling framework sticky intent issue
    private Intent mFWStickyIntent = null;
    // Aurora xuyong 2014-11-08 added for reject new feature start
    private boolean mAvoidReject = false;
    // Aurora xuyong 2014-11-08 added for reject new feature en
    public Handler mToastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String str = null;

            if (msg.what == TOAST_MSG_QUEUED) {
                str = getString(R.string.message_queued);
            } else if (msg.what == TOAST_DOWNLOAD_LATER) {
                str = getString(R.string.download_later);
            }

            if (str != null) {
                Toast.makeText(TransactionService.this, str,
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onCreate() {
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "Creating TransactionService");
        }

        Log.d(MmsApp.TXN_TAG, "Creating Transaction Service");
        sServiceAlive = true;
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        HandlerThread thread = new HandlerThread("TransactionService");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mReceiver = new ConnectivityBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION_IMMEDIATE);
        mFWStickyIntent = registerReceiver(mReceiver, intentFilter);
        //add for 81452
        registerPhoneCallListener();
        Log.d(MmsApp.TXN_TAG, "Sticky Intent would be received:" + (mFWStickyIntent!=null?"true":"false"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(MmsApp.TXN_TAG, "onStartCommand");
        if (intent == null) {
            return Service.START_NOT_STICKY;
        }
        //Gionee guoyx 20130311 add for CR00778966 begin
        int simId = -1;
        // Aurora xuyong 2014-11-08 added for reject new feature start
        mAvoidReject = intent.getBooleanExtra("avoidReject", false);
        // Aurora xuyong 2014-11-08 added for reject new feature end
        if (MmsApp.mGnMultiSimMessage) {
             simId = intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY,-1);
             Log.d(MmsApp.TXN_TAG, "TransactionService:onStartCommand get from the intent simId:" + simId);
        }
        //Gionee guoyx 20130311 add for CR00778966 end
        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean noNetwork = !isNetworkAvailable();

        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "onStart: #" + startId + ": " + intent.getExtras() + " intent=" + intent);
            Log.v(TAG, "    networkAvailable=" + !noNetwork);
        }

        Uri uri = null;
        String str = intent.getStringExtra(TransactionBundle.URI);
        if (null != str) {
            Log.d(MmsApp.TXN_TAG, "onStartCommand, URI in Bundle.");
            uri = Uri.parse(str);
            if (null != uri) {
                triggerMsgId = ContentUris.parseId(uri);
                Log.d(MmsApp.TXN_TAG, "Trigger Message ID = " + triggerMsgId);
            }
        }
        
        mMaxServiceId = (startId > mMaxServiceId)?startId:mMaxServiceId;

        if (ACTION_ONALARM.equals(intent.getAction()) || (intent.getExtras() == null)) {
            if (ACTION_ONALARM.equals(intent.getAction())) {
                Log.d(MmsApp.TXN_TAG, "onStartCommand: ACTION_ONALARM");
            } else {
                Log.d(MmsApp.TXN_TAG, "onStartCommand: Intent has no Extras data.");
            }
            // add for gemini
            if (MmsApp.mGnMultiSimMessage) {//guoyx 20130226
                if (SMART) {
                    scanPendingMessages(startId, noNetwork, -1, false);
                } else {
                    //Gionee guoyx 20130311 modified CR00778966 begin
                    // 0: no data connect, 1:sim1,  2:sim2
                    simId = System.getInt(getContentResolver(), 
                            System.GPRS_CONNECTION_SETTING, 
                            System.GPRS_CONNECTION_SETTING_DEFAULT); 
                    //Gionee guoyx 20130311 modified CR00778966 end
                    Log.d(MmsApp.TXN_TAG, "onStartCommand:  0:no data connect, 1:sim1,  2:sim2,  current="+simId);
                    if (0 != simId) {
                        scanPendingMessages(startId, noNetwork, simId-1, false);
                    }
                }
            } else {
                scanPendingMessages(startId, noNetwork, -1, false);
            }
        //add this case for read report.    
        } else if (Transaction.READREC_TRANSACTION == intent.getIntExtra("type",-1)) {
            //specific process for read report.
            TransactionBundle args = null;
            if (MmsApp.mGnMultiSimMessage) {//guoyx 20130226
                args = new TransactionBundle(intent.getIntExtra("type",3),
                                             intent.getStringExtra("uri"));
                //Gionee guoyx 20130326 added for CR00786416 begin
                if (MmsApp.mQcMultiSimEnabled) {
                    // Aurora xuyong 2014-06-20 modified for bug #5380 start
                    txnRequestsMap.add(new TxnRequest(startId, SIMInfo.getSlotById(MmsApp.getApplication().getApplicationContext(), simId)));
                    // Aurora xuyong 2014-06-20 modified for bug #5380 end
                }
                //Gionee guoyx 20130326 added for CR00786416 end
                //Gionee guoyx 20130311 modified CR00778966 begin                                             
                launchTransactionGemini(startId, simId, args);
                //Gionee guoyx 20130311 modified for CR00778966 end
            } else {
                args = new TransactionBundle(intent.getExtras());
                launchTransaction(startId, args, noNetwork);
            }
            Log.d(MmsApp.TXN_TAG,"transaction type:"+args.getTransactionType()+",uri:"+args.getUri());
        } else {
            // For launching NotificationTransaction and test purpose.
            TransactionBundle args = null;
            //add this for sync
            //Aurora yudingmin 2014-10-22 deleted for bug #9211 start
//            int pendingSize = getPendingSize();
            //Aurora yudingmin 2014-10-22 deleted for bug #9211 end
            if (MmsApp.mGnMultiSimMessage) { //guoyx 20130226
                args = new TransactionBundle(intent.getIntExtra(TransactionBundle.TRANSACTION_TYPE, 0), 
                                             intent.getStringExtra(TransactionBundle.URI));
                // 1. for gemini, do not cear noNetwork param
                // 2. check URI
                if (null != intent.getStringExtra(TransactionBundle.URI)) {
                    
                    if (-1 != simId) {
                         //Gionee guoyx 20130326 added for CR00786416 begin
                        if (MmsApp.mQcMultiSimEnabled) {
                            // Aurora xuyong 2014-06-20 modified for bug #5380 start
                            txnRequestsMap.add(new TxnRequest(startId, SIMInfo.getSlotById(MmsApp.getApplication().getApplicationContext(), simId)));
                            // Aurora xuyong 2014-06-20 modified for bug #5380 end
                        }
                        //Gionee guoyx 20130326 added for CR00786416 end
                        launchTransactionGemini(startId, simId, args);
                    } else {
                        // for handling third party 
                        long connectSimId = Settings.System.getLong(getContentResolver(), 
                                System.GPRS_CONNECTION_SIM_SETTING, System.DEFAULT_SIM_NOT_SET); 
                        Log.d(MmsApp.TXN_TAG, "onStartCommand before launch transaction:  current data settings: " + connectSimId);
                        if (System.DEFAULT_SIM_NOT_SET != connectSimId 
                                && System.GPRS_CONNECTION_SIM_SETTING_NEVER != connectSimId) {
                            //Gionee guoyx 20130326 added for CR00786416 begin
                            if (MmsApp.mQcMultiSimEnabled) {
                                // Aurora xuyong 2014-06-20 modified for bug #5380 start
                                txnRequestsMap.add(new TxnRequest(startId, (int)(SIMInfo.getSlotById(MmsApp.getApplication().getApplicationContext(), connectSimId))));
                                // Aurora xuyong 2014-06-20 modified for bug #5380 end
                            }
                            //Gionee guoyx 20130326 added for CR00786416 end
                            launchTransactionGemini(startId, (int)connectSimId, args);
                        }  
                    }
                }
            }else {
                args = new TransactionBundle(intent.getExtras());
                launchTransaction(startId, args, noNetwork);
            }
        }
        return Service.START_NOT_STICKY;
    }

    /*    
    * this method is used to scan pending messages in database to re-process them one by one.    
    * startId: useless now.    * noNetwork: whether the network is ok.    
    * simId: for single mode use -1, for gemini mode use -1 means no filter.    
    * scanAll: control scan scope.       
    */
    private void scanPendingMessages(int startId, boolean noNetwork, int simId, boolean scanAll) {
        Log.d(MmsApp.TXN_TAG, "scanPendingMessagesGemini: startid=" + startId 
            + ", Request simId=" + simId+ ", noNetwork=" + noNetwork + "scanAll:" + scanAll);
        // Scan database to find all pending operations.
        Cursor cursor = PduPersister.getPduPersister(this).getPendingMessages(
                scanAll?Long.MAX_VALUE:SystemClock.elapsedRealtime());
        if (cursor != null) {
            try {
                int count = cursor.getCount();
                Log.d(MmsApp.TXN_TAG, "scanPendingMessages: Pending Message Size=" + count);
                if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                    Log.v(TAG, "scanPendingMessages: cursor.count=" + count);
                }

                if (count == 0 && triggerMsgId == 0) {
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "scanPendingMessages: no pending messages. Stopping service.");
                    }
                    if (scanAll == false) {
                        RetryScheduler.setRetryAlarm(this);
                    }
                    stopSelfIfIdle(startId);
                    return;
                }

                int columnIndexOfMsgId = cursor.getColumnIndexOrThrow(PendingMessages.MSG_ID);
                int columnIndexOfMsgType = cursor.getColumnIndexOrThrow(PendingMessages.MSG_TYPE);
                /*gemini specific*/
                int columnIndexOfSimId = cursor.getColumnIndexOrThrow(PendingMessages.SIM_ID);
                int columnIndexOfErrorType = cursor.getColumnIndexOrThrow(PendingMessages.ERROR_TYPE);                

                if (noNetwork) {
                    // Make sure we register for connection state changes.
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "scanPendingMessages: registerForConnectionStateChanges");
                    }
                    Log.d(MmsApp.TXN_TAG, "scanPendingMessagesGemini: registerForConnectionStateChanges");
                    MmsSystemEventReceiver.registerForConnectionStateChanges(
                            getApplicationContext());
                }
                int msgType = 0;
                int transactionType = 0;
                /*gemini specific*/
                int pendingMsgSimId = 0;
                
                while (cursor.moveToNext()) {
                    msgType = cursor.getInt(columnIndexOfMsgType);
                    transactionType = getTransactionType(msgType);
                    if (noNetwork && (!MmsApp.mGnMultiSimMessage)/*only single card mode show toast*/) {
                        onNetworkUnavailable(startId, transactionType);
                        return;
                    }
                    /*gemini specific*/
                    if (MmsApp.mGnMultiSimMessage) {//guoyx 20130226
                        pendingMsgSimId = cursor.getInt(columnIndexOfSimId);
                        Log.d(MmsApp.TXN_TAG, "scanPendingMessagesGemini: pendingMsgSimId=" + pendingMsgSimId);
                        if ((simId != -1) && (simId != pendingMsgSimId)) {
                            Log.d(MmsApp.TXN_TAG, "Gemini mode, request only process simId:"+simId+",current simId is:"+pendingMsgSimId);
                            continue;
                        }                        
                        if (!SMART) {
                            if (pendingMsgSimId != simId) {
                                Log.d(MmsApp.TXN_TAG, "scanPendingMessagesGemini: pendingMsgSimId!=simId, Continue!");
                                continue;
                            }
                        }
                        if (MmsSms.ERR_TYPE_GENERIC_PERMANENT == cursor.getInt(columnIndexOfErrorType)) {
                            Log.d(MmsApp.TXN_TAG, "scanPendingMessagesGemini: Error type = Permanent, Continue!");
                            continue;
                        }
                        if (triggerMsgId == cursor.getLong(columnIndexOfMsgId)) {
                            Log.d(MmsApp.TXN_TAG, "scanPendingMessagesGemini: Message ID = Trigger message ID, Continue!");
                            continue;
                        }
                    }
                    
                    switch (transactionType) {
                        case -1:
                            Log.d(MmsApp.TXN_TAG, "scanPendingMessagesGemini: transaction Type= -1");
                            break;
                        case Transaction.RETRIEVE_TRANSACTION:
                            Log.d(MmsApp.TXN_TAG, "scanPendingMessagesGemini: transaction Type= RETRIEVE");       
                            // If it's a transiently failed transaction,
                            // we should retry it in spite of current
                            // downloading mode.
                            int failureType = cursor.getInt(
                                    cursor.getColumnIndexOrThrow(
                                            PendingMessages.ERROR_TYPE));
                            if (!isTransientFailure(failureType)) {
                                Log.d(MmsApp.TXN_TAG, cursor.getLong(columnIndexOfMsgId) +  "this RETRIEVE not transient failure");
                                break;
                            }
                            // fall-through
                        default:
                            Uri uri = ContentUris.withAppendedId(
                                    Mms.CONTENT_URI,
                                    cursor.getLong(columnIndexOfMsgId));
                            Log.d(MmsApp.TXN_TAG, "scanPendingMessages: Pending Message uri=" + uri);
                            
                            TransactionBundle args = new TransactionBundle(
                                    transactionType, uri.toString());
                            // FIXME: We use the same startId for all MMs.
                            if (MmsApp.mGnMultiSimMessage) {//guoyx 20130226
                                if (SMART) {
                                    if (pendingMsgSimId > 0) {
                                        //Gionee guoyx 20130326 added for CR00786416 begin
                                        if (MmsApp.mQcMultiSimEnabled) {
                                            // Aurora xuyong 2014-06-20 modified for bug #5380 start
                                            txnRequestsMap.add(new TxnRequest(startId, SIMInfo.getSlotById(MmsApp.getApplication().getApplicationContext(), pendingMsgSimId)));
                                            // Aurora xuyong 2014-06-20 modified for bug #5380 end
                                        }
                                        //Gionee guoyx 20130326 added for CR00786416 end
                                        launchTransactionGemini(startId, pendingMsgSimId, args);
                                    } else {
                                        // for handling third party 
                                        long connectSimId = Settings.System.getLong(getContentResolver(), System.GPRS_CONNECTION_SIM_SETTING, System.DEFAULT_SIM_NOT_SET); 
                                        Log.v(MmsApp.TXN_TAG, "Scan Pending message:  current data settings: " + connectSimId);
                                        if (System.DEFAULT_SIM_NOT_SET != connectSimId && System.GPRS_CONNECTION_SIM_SETTING_NEVER != connectSimId) {
                                            //Gionee guoyx 20130326 added for CR00786416 begin
                                            if (MmsApp.mQcMultiSimEnabled) {
                                                // Aurora xuyong 2014-06-20 modified for bug #5380 start
                                                txnRequestsMap.add(new TxnRequest(startId, (int)(SIMInfo.getSlotById(MmsApp.getApplication().getApplicationContext(), connectSimId))));
                                                // Aurora xuyong 2014-06-20 modified for bug #5380 end
                                            }
                                            //Gionee guoyx 20130326 added for CR00786416 end
                                            launchTransactionGemini(startId, (int)connectSimId, args);
                                        }
                                    }
                                } else {
                                    //Gionee guoyx 20130326 added for CR00786416 begin
                                    if (MmsApp.mQcMultiSimEnabled) {
                                        // Aurora xuyong 2014-06-20 modified for bug #5380 start
                                        txnRequestsMap.add(new TxnRequest(startId, SIMInfo.getSlotById(MmsApp.getApplication().getApplicationContext(), simId)));
                                        // Aurora xuyong 2014-06-20 modified for bug #5380 end
                                    }
                                    //Gionee guoyx 20130326 added for CR00786416 end
                                    launchTransactionGemini(startId, simId, args);
                                }
                            } else {
                                launchTransaction(startId, args, false);
                            }
                            break;
                    }
                }
            } finally {
                cursor.close();
            }
        } else {
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "scanPendingMessages: no pending messages. Stopping service.");
            } 
            Log.d(MmsApp.TXN_TAG, "scanPendingMessagesGemini: no pending messages. Stopping service.");
            if (triggerMsgId == 0) {
                if (scanAll == false) {
                    RetryScheduler.setRetryAlarm(this);
                }
                stopSelfIfIdle(startId);
            }
        }
    }
    //Gionee guoyx 20130326 added for CR00786416 begin
    private void updateTxnRequestStatus(int servId, boolean status) {
        Log.d(TAG, "updateTxnRequestStatus servId="+servId);
        for (TxnRequest req : txnRequestsMap ) {
            if (req.serviceId == servId) {
                Log.d(TAG, "updateTxnRequestStatus txn="+req);
                if (status == true) {
                    req.anyRequestFailed = 0;
                } else {
                    req.anyRequestFailed = 1;
                }
            }
        }
    }
    
    class TxnRequest {
        int serviceId;
        int requestedSubId;
        int anyRequestFailed = 0; //1 == error with atleast one transaction for current sub

        TxnRequest(int srvId, int reqSubId) {
            this.serviceId = srvId;
            this.requestedSubId = reqSubId;
        }

        public String toString() {
            return "TxnRequest=[ServiceId="+serviceId+", reqeustedSubId="+requestedSubId+", anyRequestFailed="+anyRequestFailed+"]";
        }

    };

    ArrayList<TxnRequest> txnRequestsMap = new ArrayList();
    
    private void removeNotification(int startId) {
        Log.d(TAG, "removeNotification, startId=" + startId);
        for (TxnRequest req : txnRequestsMap ) {
            if (req.serviceId == startId) {
                if (req.requestedSubId == -1) {
                    Log.d(TAG, "Notification cleanup not required since subId is -1");
                    return;
                }
                if (req.anyRequestFailed ==1) {
                    // dont remove notification.
                    Log.d(TAG, "Some transaction failed for this sub, notification not cleared.");
                } else {
                    // remove notification
                    String ns = Context.NOTIFICATION_SERVICE;
                    NotificationManager mNotificationManager = (NotificationManager)
                            getApplicationContext().getSystemService(ns);
                    mNotificationManager.cancel(req.requestedSubId);

                    if (!MmsConfig.needDataSwitchBack()) {
                        return;
                    }
                    int nextSub = (req.requestedSubId ==1) ?0:1;
                    if ((GnTelephonyManager.getSimStateGemini(nextSub) == TelephonyManager.SIM_STATE_ABSENT)
                            || (GnTelephonyManager.getSimStateGemini(nextSub) == GnTelephonyManager.SIM_STATE_DEACTIVATED)) {
                        Log.d(TAG, "MMS transaction finished. next Sub=" + nextSub + " invalid state!");
                        return;
                    }
                    Log.d(TAG, "MMS transaction finished. Going to switch for sub="+nextSub);
                    Intent silentIntent = new Intent(getApplicationContext(),
                            com.android.mms.ui.SelectMmsSubscription.class);
                    silentIntent.putExtra(GnTelephony.Mms.SIM_ID, nextSub);
                    silentIntent.putExtra("TRIGGER_SWITCH_ONLY", 1);
                    getApplicationContext().startService(silentIntent);

                }
            }
        }
    }
    //Gionee guoyx 20130326 added for CR00786416 end

    private void stopSelfIfIdle(int startId) {
        //TransactionService need keep alive to wait call end and process pending mms in db. add for 81452. 
        if (mEnableCallbackIdle) {
            Log.d(MmsApp.TXN_TAG, "need wait call end, no stop.");
            return;
        }
        synchronized (mProcessing) {
            if (mProcessing.isEmpty() && mPending.isEmpty() && mNeedWait == false/*avoid incorrectly stop service*/) {
                if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                    Log.v(TAG, "stopSelfIfIdle: STOP!");
                }
                // Make sure we're no longer listening for connection state changes.
                //if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                //    Log.v(TAG, "stopSelfIfIdle: unRegisterForConnectionStateChanges");
                //}
                //MmsSystemEventReceiver.unRegisterForConnectionStateChanges(getApplicationContext());
                Log.d(MmsApp.TXN_TAG, "stop TransactionService.");
                //Gionee guoyx 20130326 added for CR00786416 begin 
                if (MmsApp.mQcMultiSimEnabled) {
                    removeNotification(startId);
                }
                //Gionee guoyx 20130326 added for CR00786416 end
                sServiceAlive = false;
                stopSelf(startId);
            }
        }
    }

    private static boolean isTransientFailure(int type) {
        return (type < MmsSms.ERR_TYPE_GENERIC_PERMANENT) && (type > MmsSms.NO_ERROR);
    }

    private boolean isNetworkAvailable() {
        NetworkInfo ni = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
        return (ni == null ? false : ni.isAvailable());
    }

    private int getTransactionType(int msgType) {
        switch (msgType) {
            case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
                return Transaction.RETRIEVE_TRANSACTION;
            case PduHeaders.MESSAGE_TYPE_READ_REC_IND:
                return Transaction.READREC_TRANSACTION;
            case PduHeaders.MESSAGE_TYPE_SEND_REQ:
                return Transaction.SEND_TRANSACTION;
            default:
                Log.w(TAG, "Unrecognized MESSAGE_TYPE: " + msgType);
                return -1;
        }
    }

    private void launchTransaction(int serviceId, TransactionBundle txnBundle, boolean noNetwork) {
        if (noNetwork) {
            Log.w(TAG, "launchTransaction: no network error!");
            MmsSystemEventReceiver.registerForConnectionStateChanges(getApplicationContext());
            onNetworkUnavailable(serviceId, txnBundle.getTransactionType());
            return;
        }
        Message msg = mServiceHandler.obtainMessage(EVENT_TRANSACTION_REQUEST);
        msg.arg1 = serviceId;
        msg.obj = txnBundle;

        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "launchTransaction: sending message " + msg);
        }
        mServiceHandler.sendMessage(msg);
    }

    private void onNetworkUnavailable(int serviceId, int transactionType) {
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "onNetworkUnavailable: sid=" + serviceId + ", type=" + transactionType);
        }

        int toastType = TOAST_NONE;
        if (transactionType == Transaction.RETRIEVE_TRANSACTION) {
            toastType = TOAST_DOWNLOAD_LATER;
        } else if (transactionType == Transaction.SEND_TRANSACTION) {
            toastType = TOAST_MSG_QUEUED;
        }
        if (toastType != TOAST_NONE) {
            mToastHandler.sendEmptyMessage(toastType);
        }
        //Gionee guoyx 20130326 added for CR00786416 begin
        if (MmsApp.mQcMultiSimEnabled) {
            updateTxnRequestStatus(serviceId, false);
        }
        //Gionee guoyx 20130326 added for CR00786416 end
        stopSelfIfIdle(serviceId);
    }

    @Override
    public void onDestroy() {
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "Destroying TransactionService");
        }
        Log.d(MmsApp.TXN_TAG, "Destroying Transaction Service");
        if (!mPending.isEmpty()) {
            Log.w(MmsApp.TXN_TAG, "onDestroy: TransactionService exiting with transaction still pending");
        }


        if (MmsApp.mGnMultiSimMessage) {
            bWaitingConxn = false;
            GnTelephonyManager.listenGemini(mPhoneStateListener,
                    PhoneStateListener.LISTEN_NONE, GnPhone.GEMINI_SIM_1);
            mPhoneStateListener = null;
            GnTelephonyManager.listenGemini(mPhoneStateListener2,
                    PhoneStateListener.LISTEN_NONE, GnPhone.GEMINI_SIM_2);          
            mPhoneStateListener2 = null;
        } else {
            MmsApp.getApplication().getTelephonyManager().listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            mPhoneStateListener = null;
        }
        releaseWakeLock();
        unregisterReceiver(mReceiver);

        mServiceHandler.sendEmptyMessage(EVENT_QUIT);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Handle status change of Transaction (The Observable).
     */
    public void update(Observable observable) {
        Log.d(MmsApp.TXN_TAG, "Transaction Service update");
        Transaction transaction = (Transaction) observable;
        int serviceId = transaction.getServiceId();

        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "update transaction " + serviceId);
        }

        try {
            synchronized (mProcessing) {
                mProcessing.remove(transaction);
                if (mPending.size() > 0) {
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "update: handle next pending transaction...");
                    }
                    Log.d(MmsApp.TXN_TAG, "TransactionService: update: mPending.size()=" + mPending.size());
                    Message msg = mServiceHandler.obtainMessage(
                            EVENT_HANDLE_NEXT_PENDING_TRANSACTION,
                            transaction.getConnectionSettings());
                    // add for gemini
                    if (MmsApp.mGnMultiSimMessage) {//guoyx 20130228
                        msg.arg2 = transaction.mSimId;
                    }
                    mServiceHandler.sendMessage(msg);
                }
                //else {
                else if (0 == mProcessing.size()) {
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "update: endMmsConnectivity");
                    }

                    // add for gemini
                    if (MmsApp.mGnMultiSimMessage) {//guoyx 20130228
                        endMmsConnectivityGemini(transaction.mSimId);
                        Log.d(MmsApp.TXN_TAG, "update endMmsConnectivityGemini Param = " + transaction.mSimId);
                    } else {
                        Log.d(MmsApp.TXN_TAG, "update call endMmsConnectivity");
                        endMmsConnectivity();
                    }
                }
            }

            Intent intent = new Intent(TRANSACTION_COMPLETED_ACTION);
            TransactionState state = transaction.getState();
            int result = state.getState();
            intent.putExtra(STATE, result);

            switch (result) {
                case TransactionState.SUCCESS:
                    //Gionee guoyx 20130326 added for CR00786416 begin
                    if (MmsApp.mQcMultiSimEnabled) {
                        updateTxnRequestStatus(serviceId, true);
                    }
                    //Gionee guoyx 20130326 added for CR00786416 end
                    Log.d(MmsApp.TXN_TAG, "update: result=SUCCESS");
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "Transaction complete: " + serviceId);
                    }

                    intent.putExtra(STATE_URI, state.getContentUri());

                    // Notify user in the system-wide notification area.
                    switch (transaction.getType()) {
                        case Transaction.NOTIFICATION_TRANSACTION:
                        case Transaction.RETRIEVE_TRANSACTION:
                            // We're already in a non-UI thread called from
                            // NotificationTransacation.run(), so ok to block here.
                            //MessagingNotification.blockingUpdateNewMessageIndicator(this, true,
                            //        false);
                            //MessagingNotification.updateDownloadFailedNotification(this);
                            
                            //gionee gaoj 2012-10-12 added for CR00711168 start
                            if (MmsApp.mIsSafeModeSupport) {
                                break;
                            }
                            //gionee gaoj 2012-10-12 added for CR00711168 end
                            //gionee gaoj 2012-3-22 added for CR00555790 start
                            //gionee <gaoj> <2013-06-07> modify for CR00793353 begin
                            if (MmsApp.mGnPopupMsgSupport && transaction.getType() == Transaction.NOTIFICATION_TRANSACTION) {
                                //gionee <gaoj> <2013-06-07> modify for CR00793353 end
                                showPopUpView(state.getContentUri());
                            }
                            //gionee gaoj 2012-3-22 added for CR00555790 end
                            break;
                        case Transaction.SEND_TRANSACTION:
                            RateController.getInstance().update();
                            break;
                    }
                    break;
                case TransactionState.FAILED:
                    //Gionee guoyx 20130319 added for switch back sub begin
                    if (MmsApp.mQcMultiSimEnabled) {
                        updateTxnRequestStatus(serviceId, false);
                    }
                    //Gionee guoyx 20130319 added for switch back sub end
                    Log.d(MmsApp.TXN_TAG, "update: result=FAILED");
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "Transaction failed: " + serviceId);
                    }
                    break;
                default:
                    Log.d(MmsApp.TXN_TAG, "update: result=default");
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "Transaction state unknown: " +
                                serviceId + " " + result);
                    }
                    break;
            }

            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "update: broadcast transaction result " + result);
            }
            // Broadcast the result of the transaction.
            sendBroadcast(intent);
        } finally {
            transaction.detach(this);
            MmsSystemEventReceiver.unRegisterForConnectionStateChanges(getApplicationContext());
            //add this if to fix a bug. some transaction may be not processed.
            //here there is a precondition: the serviceId[startId] of each transaction got from framewrok is increasing by order.
            //so the max service id is recorded. and must invoked as the last transaction finish. 
            if (transaction.getServiceId() == mMaxServiceId) {
                stopSelfIfIdle(mMaxServiceId);
            }
            //Gionee guoyx 20130221 add for CR00773050 begin
            if (transaction.getType() == Transaction.RETRIEVE_TRANSACTION ||
                    transaction.getType() == Transaction.SEND_TRANSACTION) {
                // start: Mms data slot switch back
                if (MmsApp.mQcMultiSimEnabled && MmsConfig.needDataSwitchBack()) {
                    // delay DATA_SWITCH_DELAY seconds to switch back data subscription due to network limitation
                    mDataSwitchHandler.sendEmptyMessageDelayed(EVENT_DATA_RESET, DATA_SWITCH_DELAY);
                    mDataSwitchHandler.sendEmptyMessageDelayed(EVENT_DATA_RESET_TIMEOUT, DATA_RESET_TIMEOUT);
                }
                // end
            }
            //Gionee guoyx 20130221 add for CR00773050 end
        }
    }
    
  //Gionee guoyx 20130221 add for CR00773050 begin
    private static final int EVENT_DATA_RESET = 1;
    private static final int EVENT_DATA_RESET_TIMEOUT = 2;
    public static final int DATA_SWITCH_DELAY = 15 * 1000; // delay 15 seconds to switch data subscription back
    public static final int DATA_RESET_TIMEOUT = 45 * 1000; // 45 seconds to check whether data subscription is switched back successfully

    public Handler mDataSwitchHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == EVENT_DATA_RESET) {
                Log.d(TAG, "switch back data sub");
                // start a new thread to switch data subscription back because it is a blocking operation.
                new Thread() {
                    @Override
                    public void run() {
                        boolean result = MmsConfig.restoreDataSubscription();
                        if(!result) {
//                            mToastHandler.sendEmptyMessage(TOAST_SWITCH_DATA_BACK_FAILED);
                            Log.e("gyx", "TOAST_SWITCH_DATA_BACK_FAILED");
                        }
                        removeMessages(EVENT_DATA_RESET_TIMEOUT);
                        MmsConfig.clearDefaultDataSubscription();
                    }
                }.start();
            } else if (msg.what == EVENT_DATA_RESET_TIMEOUT) {
                removeMessages(EVENT_DATA_RESET);
                if (MmsConfig.needDataSwitchBack()) {
                    Log.e(TAG, "switch back data failed. need switch it manually.");
//                    mToastHandler.sendEmptyMessage(TOAST_SWITCH_DATA_BACK_TIMEOUT);
                }
                MmsConfig.clearDefaultDataSubscription();
            }
        }
    };
  //Gionee guoyx 20130221 add for CR00773050 end

    //gionee gaoj 2012-3-22 added for CR00555790 start
    private void showPopUpView(Uri uri) {
        if (PopUpUtils.mPopUpShowing && PopUpUtils.getPopNotfiSetting(this)) {
            Intent intent = new Intent(PopUpUtils.MSG_INFO_RECEIVER_ACTION);
            if (MmsApp.mEncryption) {
                if (getPopUpInfoBundle(uri) != null) {
                    intent.putExtras(getPopUpInfoBundle(uri));
                    sendBroadcast(intent);
                }
            } else {
                intent.putExtras(getPopUpInfoBundle(uri));
                sendBroadcast(intent);
            }

        } else if ((PopUpUtils.isLauncherView(this) || (PopUpUtils.isLockScreen(this) && !PopUpUtils.isMmsView(this))) && PopUpUtils.getPopNotfiSetting(this)) {
            Intent intents = new Intent(PopUpUtils.POPUP_ACTION);
            intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intents.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (MmsApp.mEncryption) {
                if (getPopUpInfoBundle(uri) != null) {
                    intents.putExtras(getPopUpInfoBundle(uri));
                    startActivity(intents);
                }
            } else {
                intents.putExtras(getPopUpInfoBundle(uri));
                startActivity(intents);
            }
        }
    }

    private Bundle getPopUpInfoBundle(Uri uri) {
        // Gionee fangbin 20120619 modified for CR00625563 start
        String[] pduArgs = new String[]{Sms.THREAD_ID, Sms.SIM_ID, Sms.DATE};
        // Gionee fangbin 20120619 modified for CR00625563 end
        String[] threadArgs = new String[]{ThreadsColumns.RECIPIENT_IDS};
        Cursor cursor = SqliteWrapper.query(this, getContentResolver(), uri, pduArgs, null, null, null);
        Bundle bundle = new Bundle();
        //Gionee guoyx 20130329 modified for CR00788768 begin
        if (null != cursor && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int thread_id = cursor.getInt(cursor.getColumnIndexOrThrow(Sms.THREAD_ID));
            int sim_id = cursor.getInt(cursor.getColumnIndexOrThrow(Sms.SIM_ID));
            // Gionee fangbin 20120619 added for CR00625563 start
            long date = cursor.getLong(cursor.getColumnIndexOrThrow(Sms.DATE)) * 1000;
            // Gionee fangbin 20120619 added for CR00625563 end
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
            cursor = SqliteWrapper.query(this, getContentResolver(), PopUpUtils.TABLE_THREADS_URI, threadArgs, Sms._ID + " = " + thread_id, null, null);
            if (null != cursor) {
                cursor.moveToFirst();
                int address_id = cursor.getInt(cursor.getColumnIndexOrThrow(ThreadsColumns.RECIPIENT_IDS));
                if (null != cursor && !cursor.isClosed()) {
                    cursor.close();
                }
                cursor = SqliteWrapper.query(this, getContentResolver(), PopUpUtils.TABLE_CANONICAL_ADDRESS_URI, null, Sms._ID + " = " + address_id, null, null);
                if (null != cursor) {
                    cursor.moveToFirst();
                    int addressIndex = cursor.getColumnIndexOrThrow(Sms.ADDRESS);
                    String address = cursor.getString(addressIndex);
                    bundle.putString(PopUpUtils.POPUP_INFO_ADDRESS, address);
                    bundle.putInt(PopUpUtils.POPUP_INFO_SIM_ID, sim_id);
                    bundle.putInt(PopUpUtils.POPUP_INFO_MSG_TYPE, PopUpUtils.POPUP_TYPE_MMS);
                    bundle.putString(PopUpUtils.POPUP_INFO_MSG_URI, uri.toString());
                    bundle.putInt(PopUpUtils.POPUP_INFO_THREAD_ID, thread_id);
                     // Gionee fangbin 20120619 added for CR00625563 start
                    bundle.putLong(PopUpUtils.POPUP_INFO_DATE, date);
                     // Gionee fangbin 20120619 added for CR00625563 end

                    if (MmsApp.mEncryption) {
                        Conversation conversation = Conversation.get(this,
                                (long)thread_id, false);
                        if (conversation.getEncryption()) {
                            return null;
                        }
                    }
                }
                if (null != cursor && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
        //Gionee guoyx 20130329 modified for CR00788768 end
        return bundle;
    }
    //gionee gaoj 2012-3-22 added for CR00555790 end
    private synchronized void createWakeLock() {
        // Create a new wake lock if we haven't made one yet.
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MMS Connectivity");
            mWakeLock.setReferenceCounted(false);
        }
    }

    private void acquireWakeLock() {
        // It's okay to double-acquire this because we are not using it
        // in reference-counted mode.
        mWakeLock.acquire();
    }

    private void releaseWakeLock() {
        // Don't release the wake lock if it hasn't been created and acquired.
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }
    // Aurora xuyong 2014-04-19 added for bug #3367 start
    private boolean mMobleStatusIsEnable = false;
    private ArrayList<Boolean> mDefaultMobileStatus = new ArrayList<Boolean>();
    
    private synchronized int startUsingNetworkFeature(ConnectivityManager connMgr) {
        mMobleStatusIsEnable = connMgr.getMobileDataEnabled();
        mDefaultMobileStatus.add(new Boolean(mMobleStatusIsEnable));
        if (mMobleStatusIsEnable) {
            return connMgr.startUsingNetworkFeature( 
                    ConnectivityManager.TYPE_MOBILE, GnPhone.FEATURE_ENABLE_MMS);
        }
        if (!mMobleStatusIsEnable && connMgr != null) {
            setMobileDataEnabled(true);
            mMobleStatusIsEnable = true;
        }
        return connMgr.startUsingNetworkFeature( 
                ConnectivityManager.TYPE_MOBILE, GnPhone.FEATURE_ENABLE_MMS);
    }
    
    private synchronized int stopUsingNetworkFeature(ConnectivityManager connMgr) {
        if (mMobleStatusIsEnable && mDefaultMobileStatus != null && mDefaultMobileStatus.size() > 0 && !(mDefaultMobileStatus.get(0))) {
            setMobileDataEnabled(false);
            mMobleStatusIsEnable = false;
            mDefaultMobileStatus.clear();
        }
        return connMgr.stopUsingNetworkFeature( 
                ConnectivityManager.TYPE_MOBILE, GnPhone.FEATURE_ENABLE_MMS);
    }
    // Aurora xuyong 2014-04-19 added for bug #3367 end
    protected int beginMmsConnectivity() throws IOException {
        // Take a wake lock so we don't fall asleep before the message is downloaded.
        createWakeLock();
        // Aurora xuyong 2014-04-19 modified for bug #3367 start
        int result = startUsingNetworkFeature(mConnMgr);
        // Aurora xuyong 2014-04-19 modified for bug #3367 end
        Log.d(MmsApp.TXN_TAG, "startUsingNetworkFeature: result=" + result);

        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "beginMmsConnectivity: result=" + result);
        }

        switch (result) {
            case GnPhone.APN_ALREADY_ACTIVE:
            case GnPhone.APN_REQUEST_STARTED:
                acquireWakeLock();
                //add this for time out mechanism
                setDataConnectionTimer(result);                
                return result;
            case GnPhone.APN_TYPE_NOT_AVAILABLE:
            case GnPhone.APN_REQUEST_FAILED:
                return result;
        }

        throw new IOException("Cannot establish MMS connectivity");
    }

    protected void endMmsConnectivity() {
        try {
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "endMmsConnectivity");
            }

            // cancel timer for renewal of lease
            mServiceHandler.removeMessages(EVENT_CONTINUE_MMS_CONNECTIVITY);
            if (mConnMgr != null) {
             // Aurora xuyong 2014-04-19 modified for bug #3367 start
                stopUsingNetworkFeature(mConnMgr);
             // Aurora xuyong 2014-04-19 modified for bug #3367 end
                Log.d(MmsApp.TXN_TAG, "stopUsingNetworkFeature");
            }
        } finally {
            releaseWakeLock();
            triggerMsgId = 0;
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        private String decodeMessage(Message msg) {
            if (msg.what == EVENT_QUIT) {
                return "EVENT_QUIT";
            } else if (msg.what == EVENT_CONTINUE_MMS_CONNECTIVITY) {
                return "EVENT_CONTINUE_MMS_CONNECTIVITY";
            } else if (msg.what == EVENT_TRANSACTION_REQUEST) {
                return "EVENT_TRANSACTION_REQUEST";
            } else if (msg.what == EVENT_HANDLE_NEXT_PENDING_TRANSACTION) {
                return "EVENT_HANDLE_NEXT_PENDING_TRANSACTION";
            }
            return "unknown message.what";
        }

        private String decodeTransactionType(int transactionType) {
            if (transactionType == Transaction.NOTIFICATION_TRANSACTION) {
                return "NOTIFICATION_TRANSACTION";
            } else if (transactionType == Transaction.RETRIEVE_TRANSACTION) {
                return "RETRIEVE_TRANSACTION";
            } else if (transactionType == Transaction.SEND_TRANSACTION) {
                return "SEND_TRANSACTION";
            } else if (transactionType == Transaction.READREC_TRANSACTION) {
                return "READREC_TRANSACTION";
            }
            return "invalid transaction type";
        }

        /**
         * Handle incoming transaction requests.
         * The incoming requests are initiated by the MMSC Server or by the
         * MMS Client itself.
         */
        @Override
        public void handleMessage(Message msg) {
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "Handling incoming message: " + msg + " = " + decodeMessage(msg));
            }
            Log.d(MmsApp.TXN_TAG, "handleMessage :" + msg);

            Transaction transaction = null;

            switch (msg.what) {
                case EVENT_QUIT:
                    Log.d(MmsApp.TXN_TAG, "EVENT_QUIT");
                    if (MmsApp.mGnMultiSimMessage && SMART) {//guoyx 20130228
                        bWaitingConxn = false;
                    }
                    releaseWakeLock();
                    getLooper().quit();
                    return;

                case EVENT_CONTINUE_MMS_CONNECTIVITY:
                    Log.d(MmsApp.TXN_TAG, "EVENT_CONTINUE_MMS_CONNECTIVITY");
                    synchronized (mProcessing) {
                        if (mProcessing.isEmpty()) {
                            return;
                        }
                    }

                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "handle EVENT_CONTINUE_MMS_CONNECTIVITY event...");
                    }

                    try {
                        // add for gemini
                        int result = 0;
                        if (MmsApp.mGnMultiSimMessage) {//guoyx 20130226
                            SIMInfo si = SIMInfo.getSIMInfoBySlot(getApplicationContext(), msg.arg2);
                            if (null == si) {
                                Log.e(MmsApp.TXN_TAG, "TransactionService:SIMInfo is null for slot " + msg.arg2);
                                return;
                            }
                            int simId = (int)si.mSimId;
                            result = beginMmsConnectivityGemini(simId/*msg.arg2*/);
                        } else {
                            result = beginMmsConnectivity();
                        }

                        if (result != GnPhone.APN_ALREADY_ACTIVE) {
                            if (result == GnPhone.APN_REQUEST_STARTED && mServiceHandler.hasMessages(EVENT_PENDING_TIME_OUT)) {
                                //the timer is not for this case, remove it.
                                mServiceHandler.removeMessages(EVENT_PENDING_TIME_OUT);
                                Log.d(MmsApp.TXN_TAG, "remove an invalid timer.");
                            }                            
                            // Just wait for connectivity startup without
                            // any new request of APN switch.
                            return;
                        }
                    } catch (IOException e) {
                        Log.w(TAG, "Attempt to extend use of MMS connectivity failed");
                        return;
                    }

                    // Restart timer
                    renewMmsConnectivity(0, msg.arg2);
                    return;

                case EVENT_DATA_STATE_CHANGED:
                    Log.d(MmsApp.TXN_TAG, "EVENT_DATA_STATE_CHANGED! slot=" + msg.arg2);

                    //add for time out mechanism
                    if (mIgnoreMsg == true) {
                        Log.d(MmsApp.TXN_TAG, "between time out over and a new connection request, ignore msg.");
                        return;
                    }

                    if (mServiceHandler.hasMessages(EVENT_PENDING_TIME_OUT)) {
                        if (MmsApp.mGnMultiSimMessage) {//guoyx 20130228
                            Transaction trxn = null;
                            synchronized (mProcessing) {
                                if (mPending.size() != 0) {
                                    trxn = mPending.get(0);
                                } else {
                                    Log.d(MmsApp.TXN_TAG, "a timer is created but pending is null!");
                                }
                            }
                            /*state change but pending is null, this may happened.
                                this is just for framework abnormal case*/
                            if (trxn == null) {
                                Log.d(MmsApp.TXN_TAG, "remove a timer which may be created by EVENT_CONTINUE_MMS_CONNECTIVITY");
                                mServiceHandler.removeMessages(EVENT_PENDING_TIME_OUT);
                                //return;//since the pending is null, we can return. if not, it should ok too.
                                
                            } else {
                                int slotId = SIMInfo.getSlotById(getApplicationContext(), trxn.mSimId);
                                if (slotId == msg.arg2) {
                                    mServiceHandler.removeMessages(EVENT_PENDING_TIME_OUT);
                                    Log.d(MmsApp.TXN_TAG, "gemini normal get msg, remove timer.");
                                }
                            }
                        } else {
                            mServiceHandler.removeMessages(EVENT_PENDING_TIME_OUT);
                            Log.d(MmsApp.TXN_TAG, "normal get msg, remove timer.");
                        }
                    }

                    if (mConnMgr == null) {                        
                        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);                    
                    }                    
                    if (mConnMgr == null) {                        
                        Log.d(MmsApp.TXN_TAG, "mConnMgr == null ");                      
                        return;                    
                    }
                    NetworkInfo info = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
                    if (info == null) {
                        Log.d(MmsApp.TXN_TAG, "NetworkInfo == null.");
                        return;
                    }
                    int slotOfInfo = GnNetworkInfo.getSimId(info);
                    
                    Log.d(MmsApp.TXN_TAG, "Newwork info,reason: " + info.getReason() 
                                            + ",state:" + info.getState()
                                            + ",extra info:" + info.getExtraInfo()
                                            + ",slot:" + slotOfInfo);
                    //add for sync
                    int pendingSize = getPendingSize();
                    // Check connection state : connect or disconnect
                    if (!info.isConnected()) {
                        if (MmsApp.mGnMultiSimMessage //guoyx 20130226 
                                && (ConnectivityManager.TYPE_MOBILE == info.getType()
                                    ||ConnectivityManager.TYPE_MOBILE_MMS == info.getType())) {
                            if (pendingSize != 0) {
                                if (SMART) {
                                    //change for sync
                                    Transaction trxn = null;
                                    synchronized (mProcessing) {
                                        trxn = mPending.get(0);
                                    }
                                    int slotId = SIMInfo.getSlotById(getApplicationContext(), trxn.mSimId);
                                    if (slotId != slotOfInfo) {
                                        return;
                                    }
                                } else {
                                    int simId = System.getInt(getContentResolver(), 
                                            System.GPRS_CONNECTION_SETTING, 
                                            System.GPRS_CONNECTION_SETTING_DEFAULT); 
                                    //add for sync
                                    Transaction trxn = null;
                                    synchronized (mProcessing) {
                                        trxn = mPending.get(0);
                                    }
                                    // Aurora xuyong 2014-06-20 modified for bug #5380 start
                                    if (trxn.mSimId != SIMInfo.getSlotById(MmsApp.getApplication().getApplicationContext(), simId)){
                                    // Aurora xuyong 2014-06-20 modified for bug #5380 end
                                        //add for sync
                                        setTransactionFail(removePending(0), FAILE_TYPE_PERMANENT);
                                    }
                                }
                            }
                        }
                        
                        // check type and reason, 
                        if (ConnectivityManager.TYPE_MOBILE_MMS == info.getType() 
                            && GnPhone.REASON_NO_SUCH_PDP.equals(info.getReason())) {
                            if (0 != pendingSize){
                                //add for sync
                                setTransactionFail(removePending(0), FAILE_TYPE_PERMANENT);
                                return;
                            }
                        } else if (ConnectivityManager.TYPE_MOBILE_MMS == info.getType()
                            && NetworkInfo.State.DISCONNECTED == info.getState()) {
                            if (0 != pendingSize){
                                Log.d(MmsApp.TXN_TAG, "setTransactionFail TEMPORARY because NetworkInfo.State.DISCONNECTED");
                                //add for sync
                                setTransactionFail(removePending(0), FAILE_TYPE_TEMPORARY);
                                return;
                            }             
                        } else if ((ConnectivityManager.TYPE_MOBILE_MMS == info.getType() 
                                && GnPhone.REASON_APN_FAILED.equals(info.getReason())) 
                                || GnPhone.REASON_RADIO_TURNED_OFF.equals(info.getReason())) {
                            if (0 != pendingSize){
                                //add for sync
                                setTransactionFail(removePending(0), FAILE_TYPE_TEMPORARY);
                                return;
                            }
                            Log.d(MmsApp.TXN_TAG, "No pending message.");
                        }
                        return;
                    } else {
                        if (MmsApp.mGnMultiSimMessage && pendingSize != 0) {//guoyx 20130301
                            Transaction trxn = null;
                            synchronized (mProcessing) {
                                trxn = mPending.get(0);
                            }
                            int slotId = SIMInfo.getSlotById(getApplicationContext(), trxn.mSimId);
                            if (slotId != slotOfInfo) {
                                Log.d(MmsApp.TXN_TAG, "the connected slot not the one needed.");
                                return;
                            }
                        }
                    }

                    if (GnPhone.REASON_VOICE_CALL_ENDED.equals(info.getReason())){
                        if (0 != pendingSize){
                            Transaction trxn = null;
                            synchronized (mProcessing) {
                                trxn = mPending.get(0);
                            }
                            // add for gemini
                            if (MmsApp.mGnMultiSimMessage) {
                                processPendingTransactionGemini(transaction,trxn.getConnectionSettings(),trxn.mSimId);
                            } else {
                                processPendingTransaction(transaction, trxn.getConnectionSettings());
                            }
                        }
                    }

                    TransactionSettings settings = null;
                    if (MmsApp.mGnMultiSimMessage) {//guoyx 20130228
                        if (SMART) {
                            if (GnPhone.GEMINI_SIM_1 == slotOfInfo || GnPhone.GEMINI_SIM_2 == slotOfInfo) {
                                settings = new TransactionSettings(TransactionService.this, info.getExtraInfo(), slotOfInfo);
                            } else {
                                return;
                            }
                        } else {
                            int simId = System.getInt(getContentResolver(), 
                                    System.GPRS_CONNECTION_SETTING, 
                                    System.GPRS_CONNECTION_SETTING_DEFAULT);
                            Log.d(MmsApp.TXN_TAG, "handleMessage:  0:no data connect, 1:sim1,  2:sim2,  current=" + simId);
                            if (0 != simId) {
                                settings = new TransactionSettings(TransactionService.this, info.getExtraInfo(), simId-1);
                            } else {
                                return;
                            }
                        }
                    } else {
                        settings = new TransactionSettings(TransactionService.this, info.getExtraInfo());
                    }

                    // If this APN doesn't have an MMSC, wait for one that does.
                    if (TextUtils.isEmpty(settings.getMmscUrl())) {
                        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                            Log.v(TAG, "   empty MMSC url, bail");
                        }
                        Log.d(MmsApp.TXN_TAG, "empty MMSC url, bail");
                        if (0 != pendingSize){
                            setTransactionFail(removePending(0), FAILE_TYPE_TEMPORARY);
                        }
                        return;
                    }

                    if (MmsApp.mGnMultiSimMessage) {//guoyx 20130228
                        if (SMART) {
                            if (GnPhone.GEMINI_SIM_1 == slotOfInfo || GnPhone.GEMINI_SIM_2 == slotOfInfo) {
                                SIMInfo si = SIMInfo.getSIMInfoBySlot(getApplicationContext(), slotOfInfo);
                                if (null == si) {
                                    Log.e(MmsApp.TXN_TAG, "TransactionService:SIMInfo is null for slot " + slotOfInfo);
                                    return;
                                }
                                int simId = (int)si.mSimId;
                                processPendingTransactionGemini(transaction, settings, simId/*msg.arg2*/);
                            }else {
                                return;
                            }
                        } else {
                            int simId = System.getInt(getContentResolver(), 
                                    System.GPRS_CONNECTION_SETTING, 
                                    System.GPRS_CONNECTION_SETTING_DEFAULT);
                            Log.d(MmsApp.TXN_TAG, "handleMessage:  0:no data connect, 1:sim1,  2:sim2,  current="+simId);
                            if (0 != simId) {
                                processPendingTransactionGemini(transaction, settings, simId-1);                        
                            } else {                            
                                return;                        
                            }
                        }
                    }else {
                        processPendingTransaction(transaction, settings);
                    }
                    return;

                case EVENT_TRANSACTION_REQUEST:
                    Log.d(MmsApp.TXN_TAG, "EVENT_TRANSACTION_REQUEST");

                    int serviceId = msg.arg1;
                    try {
                        TransactionBundle args = (TransactionBundle) msg.obj;
                        TransactionSettings transactionSettings;

                        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                            Log.v(TAG, "EVENT_TRANSACTION_REQUEST MmscUrl=" +
                                    args.getMmscUrl() + " proxy port: " + args.getProxyAddress());
                        }

                        // Set the connection settings for this transaction.
                        // If these have not been set in args, load the default settings.
                        String mmsc = args.getMmscUrl();
                        if (mmsc != null) {
                            transactionSettings = new TransactionSettings(
                                    mmsc, args.getProxyAddress(), args.getProxyPort());
                        } else {
                            // add for gemini
                            if (MmsApp.mGnMultiSimMessage) {//guoyx 20130226
                                // convert sim id to slot id
                                int slotId = SIMInfo.getSlotById(getApplicationContext(), msg.arg2);
                                transactionSettings = new TransactionSettings(
                                                    TransactionService.this, null, slotId/*msg.arg2*/);
                            } else {
                                transactionSettings = new TransactionSettings(
                                                    TransactionService.this, null);
                            }
                        }

                        int transactionType = args.getTransactionType();

                        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                            Log.v(TAG, "handle EVENT_TRANSACTION_REQUEST: transactionType=" +
                                    transactionType + " " + decodeTransactionType(transactionType));
                        }

                        // Create appropriate transaction
                        switch (transactionType) {
                            case Transaction.NOTIFICATION_TRANSACTION:
                                String uri = args.getUri();
                                Log.d(MmsApp.TXN_TAG, "TRANSACTION REQUEST: NOTIFICATION_TRANSACTION, uri="+uri);
                                if (uri != null) {
                                    // add for gemini
                                    if (MmsApp.mGnMultiSimMessage) {
                                        //Gionee guoyx 20130326 added for CR00786416 begin
                                        if (MmsApp.mQcMultiSimEnabled) {
                                            // Aurora xuyong 2014-06-20 modified for bug #5380 start
                                            int subId = SIMInfo.getSlotById(MmsApp.getApplication().getApplicationContext(), msg.arg2);
                                            // Aurora xuyong 2014-06-20 modified for bug #5380 end
                                            if (subId != MmsConfig.getUserPreferDataSubscription()) {
                                                MmsConfig.backupDataSubscription(subId == 1 ? 0 : 1);
                                                int result1 = (GnTelephonyManagerEx.getDefault()
                                                        .setPreferredDataSubscription(subId)) ? 1
                                                        : 0;
                                                if (result1 == 1) { // Success.
                                                    Log.d(MmsApp.TXN_TAG,
                                                            "Subscription switch done.");
                                                    sleep(1000);
                    
                                                    while (!isNetworkAvailable()) {
                                                        Log.d(MmsApp.TXN_TAG,
                                                                "isNetworkAvailable = false, sleep..");
                                                        sleep(1000);
                                                    }
                                                }
                                            }
                                        }
                                        //Gionee guoyx 20130326 added for CR00786416 end
                                        transaction = new NotificationTransaction(
                                            TransactionService.this, serviceId, msg.arg2,
                                            transactionSettings, uri);
                                        // Aurora xuyong 2014-11-08 added for reject new feature start
                                        transaction.setAvoidReject(mAvoidReject);
                                        // Aurora xuyong 2014-11-08 added for reject new feature end
                                    } else {
                                        transaction = new NotificationTransaction(
                                            TransactionService.this, serviceId,
                                            transactionSettings, uri);
                                        // Aurora xuyong 2014-11-08 added for reject new feature start
                                        transaction.setAvoidReject(mAvoidReject);
                                        // Aurora xuyong 2014-11-08 added for reject new feature end
                                    }
                                } else {
                                    // Now it's only used for test purpose.
                                    byte[] pushData = args.getPushData();
                                    PduParser parser = new PduParser(pushData);
                                    GenericPdu ind = parser.parse();

                                    int type = PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND;
                                    if ((ind != null) && (ind.getMessageType() == type)) {
                                        // add for gemini
                                        if (MmsApp.mGnMultiSimMessage) {//guoyx 20130228
                                            transaction = new NotificationTransaction(
                                                TransactionService.this, serviceId, msg.arg2,
                                                transactionSettings, (NotificationInd) ind);
                                            // Aurora xuyong 2014-11-08 added for reject new feature start
                                            transaction.setAvoidReject(mAvoidReject);
                                            // Aurora xuyong 2014-11-08 added for reject new feature end
                                        } else {
                                            transaction = new NotificationTransaction(
                                                TransactionService.this, serviceId,
                                                transactionSettings, (NotificationInd) ind);
                                            // Aurora xuyong 2014-11-08 added for reject new feature start
                                            transaction.setAvoidReject(mAvoidReject);
                                            // Aurora xuyong 2014-11-08 added for reject new feature end
                                        }
                                    } else {
                                        Log.e(MmsApp.TXN_TAG, "Invalid PUSH data.");
                                        transaction = null;
                                        return;
                                    }
                                }
                                break;
                            case Transaction.RETRIEVE_TRANSACTION:
                                Log.d(MmsApp.TXN_TAG,
                                        "TRANSACTION REQUEST: RETRIEVE_TRANSACTION uri="
                                                + args.getUri());
                             // Gionee guoyx 20130226 add for CR00773050 begin
                                if (MmsApp.mQcMultiSimEnabled) {
                                    int subId = SIMInfo.getSlotById(MmsApp.getApplication().getApplicationContext(), msg.arg2);
                                    Log.d(MmsApp.TXN_TAG, "subId = " + subId);
                                    //Gionee guoyx 20130407 modified for CR00773050 begin
                                    if (subId != MmsConfig.getUserPreferDataSubscription()) {
                                        MmsConfig.backupDataSubscription(subId == 1 ? 0 : 1);
                                        int result1 = (GnTelephonyManagerEx.getDefault()
                                            .setPreferredDataSubscription(subId)) ? 1
                                            : 0;
                                        if (result1 == 1) { // Success.
                                            Log.d(MmsApp.TXN_TAG,
                                                    "Subscription switch done.");
                                            sleep(1000);
            
                                            while (!isNetworkAvailable()) {
                                                Log.d(MmsApp.TXN_TAG,
                                                        "isNetworkAvailable = false, sleep..");
                                                sleep(1000);
                                            }
                                        }
                                    }
                                    //Gionee guoyx 20130407 modified for CR00773050 end
                                }
                                //Gionee guoyx 20130226 add for CR00773050 end
                                // add for gemini
                                if (MmsApp.mGnMultiSimMessage) {//guoyx 20130226
                                    transaction = new RetrieveTransaction(
                                        TransactionService.this, serviceId, msg.arg2,
                                        transactionSettings, args.getUri());
                                    // Aurora xuyong 2014-11-08 added for reject new feature start
                                    transaction.setAvoidReject(mAvoidReject);
                                    // Aurora xuyong 2014-11-08 added for reject new feature end
                                } else {
                                    transaction = new RetrieveTransaction(
                                        TransactionService.this, serviceId,
                                        transactionSettings, args.getUri());
                                    // Aurora xuyong 2014-11-08 added for reject new feature start
                                    transaction.setAvoidReject(mAvoidReject);
                                    // Aurora xuyong 2014-11-08 added for reject new feature end
                                }
                                break;
                            case Transaction.SEND_TRANSACTION:
                                Log.d(MmsApp.TXN_TAG, "TRANSACTION REQUEST: SEND_TRANSACTION");
                                // add for gemini
                                if (MmsApp.mGnMultiSimMessage) {//guoyx 20130226
                                    //Gionee guoyx 20130326 added for CR00786416 begin
                                    // Aurora xuyong 2014-06-10 deleted for aurora's feature start
                                    if (MmsApp.mQcMultiSimEnabled) {
                                                //Aurora hujunming 2014-5-15 modify for multiSim start
//                                      int subId = msg.arg2 - 1;
                                        int subId = SIMInfo.getSlotById(MmsApp.getApplication().getApplicationContext(), msg.arg2);
                                                //Aurora hujunming 2014-5-15 modify for multiSim end
                                        if (subId != MmsConfig.getUserPreferDataSubscription()) {
                                            MmsConfig.backupDataSubscription(subId == 1 ? 0 : 1);
                                            int result1 = (GnTelephonyManagerEx.getDefault()
                                                    .setPreferredDataSubscription(subId)) ? 1
                                                    : 0;
                                            if (result1 == 1) { // Success.
                                                Log.d(MmsApp.TXN_TAG,
                                                        "Subscription switch done.");
                                                sleep(1000);
                
                                                while (!isNetworkAvailable()) {
                                                    Log.d(MmsApp.TXN_TAG,
                                                            "isNetworkAvailable = false, sleep..");
                                                    sleep(1000);
                                                }
                                            }
                                        }
                                    }
                                    // Aurora xuyong 2014-06-10 deleted for aurora's feature end
                                    //Gionee guoyx 20130326 added for CR00786416 end
                                    transaction = new SendTransaction(
                                        TransactionService.this, serviceId, msg.arg2,
                                        transactionSettings, args.getUri());
                                } else {
                                    transaction = new SendTransaction(
                                        TransactionService.this, serviceId,
                                        transactionSettings, args.getUri());
                                }
                                break;
                            case Transaction.READREC_TRANSACTION:
                                Log.d(MmsApp.TXN_TAG, "TRANSACTION REQUEST: READREC_TRANSACTION");
                                // add for gemini
                                if (MmsApp.mGnMultiSimMessage) {//guoyx 20130228
                                    transaction = new ReadRecTransaction(
                                        TransactionService.this, serviceId, msg.arg2,
                                        transactionSettings, args.getUri());
                                    // Aurora xuyong 2014-11-08 added for reject new feature start
                                    transaction.setAvoidReject(mAvoidReject);
                                    // Aurora xuyong 2014-11-08 added for reject new feature end
                                } else {
                                    transaction = new ReadRecTransaction(
                                        TransactionService.this, serviceId,
                                        transactionSettings, args.getUri());
                                    // Aurora xuyong 2014-11-08 added for reject new feature start
                                    transaction.setAvoidReject(mAvoidReject);
                                    // Aurora xuyong 2014-11-08 added for reject new feature end
                                }
                                break;
                            default:
                                Log.w(MmsApp.TXN_TAG, "Invalid transaction type: " + serviceId);
                                transaction = null;
                                return;
                        }

                        if (!processTransaction(transaction)) {
                            // add for gemini
                            if (MmsApp.mGnMultiSimMessage && null != transaction) {//guoyx 20130301
                                mSimIdForEnd = transaction.mSimId;
                            }
                            transaction = null;
                            return;
                        }

                        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                            Log.v(TAG, "Started processing of incoming message: " + msg);
                        }
                    } catch (Exception ex) {
                        Log.e(MmsApp.TXN_TAG, "Exception occurred while handling message: " + msg, ex);

                        if (transaction != null) {
                            // add for gemini
                            if (MmsApp.mGnMultiSimMessage) {//guoyx 20130301
                                mSimIdForEnd = transaction.mSimId;
                            }
                            try {
                                transaction.detach(TransactionService.this);
                                //change this sync.
                                synchronized (mProcessing) {
                                    if (mProcessing.contains(transaction)) {
                                        mProcessing.remove(transaction);
                                    }
                                }
                            } catch (Throwable t) {
                                Log.e(TAG, "Unexpected Throwable.", t);
                            } finally {
                                // Set transaction to null to allow stopping the
                                // transaction service.
                                transaction = null;
                            }
                        }
                    } finally {
                        if (transaction == null) {
                            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                                Log.v(TAG, "Transaction was null. Stopping self: " + serviceId);
                            }
                            Log.d(MmsApp.TXN_TAG, "finally call endMmsConnectivity");
                            //add this for sync
                            boolean canEnd = false;
                            synchronized (mProcessing) {
                                canEnd = (mProcessing.size() == 0 && mPending.size() == 0);
                            }
                            if (canEnd == true){
                                // add for gemini
                                if (MmsApp.mGnMultiSimMessage) {//guoyx 20130301
                                    endMmsConnectivityGemini(mSimIdForEnd);
                                } else {
                                    endMmsConnectivity();
                                }
                            }
                            //change for 81452
                            stopSelfIfIdle(serviceId);
                        }
                    }
                    return;
                case EVENT_HANDLE_NEXT_PENDING_TRANSACTION:
                    Log.d(MmsApp.TXN_TAG, "EVENT_HANDLE_NEXT_PENDING_TRANSACTION");
                    // add for gemini
                    if (MmsApp.mGnMultiSimMessage) {//guoyx 20130301
                        processPendingTransactionGemini(transaction, (TransactionSettings) msg.obj, msg.arg2);
                    } else {
                        processPendingTransaction(transaction, (TransactionSettings) msg.obj);
                    }
                    return;
                //add for time out mechanism    
                case EVENT_PENDING_TIME_OUT:
                    //make the pending transaction temporary failed.
                    int pendSize = getPendingSize();
                    if (0 != pendSize){
                        Log.d(MmsApp.TXN_TAG, "a pending connection request time out, mark temporary failed.");
                        mIgnoreMsg = true;
                        setTransactionFail(removePending(0), FAILE_TYPE_TEMPORARY);
                    }
                    return;                    
                //add for 81452
                case EVENT_SCAN_PENDING_MMS:
                    {
                        Log.d(MmsApp.TXN_TAG, "EVENT_SCAN_PENDING_MMS");
                        if (MmsApp.mGnMultiSimMessage) {//guoyx 20130301
                            int firstSlot = mLastIdleSlot;
                            int secondSlot;
                            if (firstSlot == GnPhone.GEMINI_SIM_1) {
                                secondSlot = GnPhone.GEMINI_SIM_2;
                            } else {
                                secondSlot = GnPhone.GEMINI_SIM_1;
                            }
                            Log.d(MmsApp.TXN_TAG, "scan first slot:"+firstSlot+",second slot:"+secondSlot);
                            SIMInfo si = SIMInfo.getSIMInfoBySlot(getApplicationContext(), firstSlot);
                            if (null != si) {
                                scanPendingMessages(1, false, (int)si.mSimId, true);
                            }
                            si = null;
                            si = SIMInfo.getSIMInfoBySlot(getApplicationContext(), secondSlot);
                            if (null != si) {
                                scanPendingMessages(1, false, (int)si.mSimId, true);
                            }
                        } else {
                            scanPendingMessages(1, false, -1, true);
                        }
                    }
                    break;
                default:
                    Log.d(MmsApp.TXN_TAG, "handleMessage : default");
                    Log.w(TAG, "what=" + msg.what);
                    return;
            }
        }

        public void processPendingTransaction(Transaction transaction,
                                               TransactionSettings settings) {
            Log.v(MmsApp.TXN_TAG, "processPendingTxn: transaction=" + transaction);
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "processPendingTxn: transaction=" + transaction);
            }

            int numProcessTransaction = 0;
            synchronized (mProcessing) {
                if (mPending.size() != 0) {
                    Log.d(MmsApp.TXN_TAG, "processPendingTransaction: mPending.size()=" + mPending.size());
                    transaction = mPending.remove(0);
                    //avoid stop TransactionService incorrectly.
                    mNeedWait = true;
                }
                numProcessTransaction = mProcessing.size();
            }

            if (transaction != null) {
                if (settings != null) {
                    transaction.setConnectionSettings(settings);
                }

                /*
                 * Process deferred transaction
                 */
                try {
                    int serviceId = transaction.getServiceId();

                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "processPendingTxn: process " + serviceId);
                    }

                    if (processTransaction(transaction)) {
                        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                            Log.v(TAG, "Started deferred processing of transaction  "
                                    + transaction);
                        }
                    } else {
                        transaction = null;
                        //change for 81452
                        stopSelfIfIdle(serviceId);
                    }
                } catch (IOException e) {
                    Log.w(TAG, e.getMessage(), e);
                }
            } else {
                if (numProcessTransaction == 0) {
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "processPendingTxn: no more transaction, endMmsConnectivity");
                    }
                    Log.d(MmsApp.TXN_TAG, "processPendingTransaction:no more transaction, endMmsConnectivity");
                    endMmsConnectivity();
                }
            }
        }

        // add for gemini
        public void processPendingTransactionGemini(Transaction transaction,
                                               TransactionSettings settings, int simId) {
            Log.d(MmsApp.TXN_TAG, "processPendingTxn for Gemini: transaction=" + transaction + " sim ID="+simId);

            int numProcessTransaction = 0;
            synchronized (mProcessing) {
                if (mPending.size() != 0) {
                    Log.d(MmsApp.TXN_TAG, "processPendingTxn for Gemini: Pending size=" + mPending.size());
                    Transaction transactiontemp = null;
                    int pendingSize = mPending.size();
                    for (int i = 0; i < pendingSize; ++i){
                        transactiontemp = mPending.remove(0);
                        if (simId == transactiontemp.mSimId){
                            transaction = transactiontemp;
                            Log.d(MmsApp.TXN_TAG, "processPendingTxn for Gemini, get transaction with same simId");
                            //avoid stop TransactionService incorrectly.
                            mNeedWait = true;
                            break;
                        }else{
                            mPending.add(transactiontemp);
                            Log.d(MmsApp.TXN_TAG, "processPendingTxn for Gemini, diffrent simId, add to tail");
                        }
                    }
                    if (SMART) {
                        if (null == transaction) {
                            transaction = mPending.remove(0);
                            //avoid stop TransactionService incorrectly.
                            mNeedWait = true;
                            endMmsConnectivityGemini(simId);
                            Log.d(MmsApp.TXN_TAG, "Another SIM:" + transaction.mSimId);
                        }
                    }
                }
                numProcessTransaction = mProcessing.size();
            }

            if (transaction != null) {
                if (settings != null) {
                    transaction.setConnectionSettings(settings);
                }

                if (MmsApp.mGnMultiSimMessage && SMART) {//guoyx 20130301
                    bWaitingConxn = false;
                }

                try {
                    int serviceId = transaction.getServiceId();
                    Log.d(MmsApp.TXN_TAG, "processPendingTxnGemini: process " + serviceId);

                    if (processTransaction(transaction)) {
                        Log.d(MmsApp.TXN_TAG, "Started deferred processing of transaction  " + transaction);
                    } else {
                        transaction = null;
                        //Gionee guoyx 20130326 added for CR00786416 begin
                        if (MmsApp.mQcMultiSimEnabled) {
                            updateTxnRequestStatus(serviceId, false);
                        }
                        //Gionee guoyx 20130326 added for CR00786416 end
                        //change for 81452
                        stopSelfIfIdle(serviceId);
                    }
                } catch (IOException e) {
                    Log.e(MmsApp.TXN_TAG, e.getMessage(), e);
                }
            } else {
                if (numProcessTransaction == 0) {
                    Log.d(MmsApp.TXN_TAG, "processPendingTxnGemini:no more transaction, endMmsConnectivity");
                    endMmsConnectivityGemini(simId);
                }
            }
        }

        //Gionee guoyx 20130111 added for Qualcomm Multi Sim CR00754375 begin
        private boolean isDuplicateRetrieve(Transaction t1, Transaction t2) {
            if (t1 == null || t2 == null) {
                return false;
            }

            if ((t1 instanceof NotificationTransaction)
                    && (t2 instanceof RetrieveTransaction)) {
                if (((NotificationTransaction) t1).getNotTrxnUri().equals(
                        ((RetrieveTransaction) t2).getRtrTrxnUri())) {
                    return true;
                }
            }

            if ((t1 instanceof RetrieveTransaction)
                    && (t2 instanceof RetrieveTransaction)) {
                if (((RetrieveTransaction) t1).getRtrTrxnUri().equals(
                        ((RetrieveTransaction) t2).getRtrTrxnUri())) {
                    return true;
                }
            }

            return false;
        }
        //Gionee guoyx 20130111 added for Qualcomm Multi Sim CR00754375 end
        /**
         * Internal method to begin processing a transaction.
         * @param transaction the transaction. Must not be {@code null}.
         * @return {@code true} if process has begun or will begin. {@code false}
         * if the transaction should be discarded.
         * @throws IOException if connectivity for MMS traffic could not be
         * established.
         */
        private boolean processTransaction(Transaction transaction) throws IOException {
            Log.v(MmsApp.TXN_TAG, "process Transaction");
            int requestResult = GnPhone.APN_REQUEST_FAILED;
            // Check if transaction already processing
            synchronized (mProcessing) {
                //avoid stop TransactionService incorrectly.
                mNeedWait = false;
                for (Transaction t : mPending) {
                    if (t.isEquivalent(transaction)) {
                        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                            Log.v(TAG, "Transaction already pending: " +
                                    transaction.getServiceId());
                        }
                        Log.d(MmsApp.TXN_TAG, "Process Transaction: already pending " + transaction.getServiceId());
                        return true;
                    } else if (MmsApp.mQcMultiSimEnabled && isDuplicateRetrieve(t, transaction)) { //Gionee guoyx 20130111 CR00754375
                        Log.v(TAG, "Duplicate Retrieve Transaction pending: " +
                                    transaction.getServiceId());
                        // Aurora xuyong 2014-06-20 modified for bug #5380 start
                        if (MmsApp.mGnMultiSimMessage) {
                            beginMmsConnectivityGemini(transaction.mSimId);
                        } else {
                            beginMmsConnectivity();
                        }
                        return true;
                    }
                    // Aurora xuyong 2014-06-20 modified for bug #5380 end
                }
                for (Transaction t : mProcessing) {
                    if (t.isEquivalent(transaction)) {
                        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                            Log.v(TAG, "Duplicated transaction: " + transaction.getServiceId());
                        }
                        Log.d(MmsApp.TXN_TAG, "Process Transaction: Duplicated transaction" + transaction.getServiceId());
                        return true;
                    }
                }

                // add for gemini
                //if(FeatureOption.MTK_GEMINI_SUPPORT && SMART && (mProcessing.size() > 0 || mPending.size() > 0)){
                if (MmsApp.mGnMultiSimMessage && SMART && (mProcessing.size() > 0 || bWaitingConxn)) {//guoyx 20130301
                    mPending.add(transaction);
                    Log.d(MmsApp.TXN_TAG, "add to pending, Processing size=" + mProcessing.size() 
                        + ",is waiting conxn=" + bWaitingConxn);
                    return true;
                }

                /*
                * Make sure that the network connectivity necessary
                * for MMS traffic is enabled. If it is not, we need
                * to defer processing the transaction until
                * connectivity is established.
                */
                if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                    Log.v(TAG, "processTransaction: call beginMmsConnectivity...");
                }
                
                int connectivityResult = 0;
                if (MmsApp.mGnMultiSimMessage) {//guoyx 20130226
                    connectivityResult = beginMmsConnectivityGemini(transaction.mSimId);
                } else {
                    connectivityResult = beginMmsConnectivity();
                }
                requestResult = connectivityResult;
                if (connectivityResult == GnPhone.APN_REQUEST_STARTED) {
                    mPending.add(transaction);
                    if (MmsApp.mGnMultiSimMessage && SMART) {//guoyx 20130226
                        bWaitingConxn = true;
                    }
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "processTransaction: connResult=APN_REQUEST_STARTED, " +
                                "defer transaction pending MMS connectivity");
                    }
                    Log.d(MmsApp.TXN_TAG, "mPending.size()=" + mPending.size());
                    return true;
                } 
                // add for gemini and open
                else if (connectivityResult == GnPhone.APN_TYPE_NOT_AVAILABLE
                        ||connectivityResult == GnPhone.APN_REQUEST_FAILED){
                    //add for read report, make it easy.
                    if (transaction instanceof ReadRecTransaction) {
                        setTransactionFail(transaction, FAILE_TYPE_PERMANENT);
                        return false;
                    }

                    if (transaction instanceof SendTransaction
                        || transaction instanceof RetrieveTransaction){
                        //add for 81452
                        //if failed becaused of call, moniter call end, and go on process.
                        if (isDuringCall()) {
                            synchronized (mIdleLock) {
                                mEnableCallbackIdle = true;
                            }
                            setTransactionFail(transaction, FAILE_TYPE_TEMPORARY);
                        } else {
                            setTransactionFail(transaction, FAILE_TYPE_PERMANENT);
                        }
                        return false;
                    }
                }

                Log.d(MmsApp.TXN_TAG, "Adding Processing list: " + transaction);
                mProcessing.add(transaction);
            }

            if (requestResult == GnPhone.APN_ALREADY_ACTIVE) {
                Log.d(MmsApp.TXN_TAG, "request ok, renew connection.");
                // Set a timer to keep renewing our "lease" on the MMS connection
                if (MmsApp.mGnMultiSimMessage) {//guoyx 20130226
                    int slotId = SIMInfo.getSlotById(getApplicationContext(), transaction.mSimId);
                    renewMmsConnectivity(0, slotId);
                } else {
                    renewMmsConnectivity(0, 0);
                }
            }

            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "processTransaction: starting transaction " + transaction);
            }

            // Attach to transaction and process it
            transaction.attach(TransactionService.this);
            transaction.process();
            return true;
        }
    }


    // add for gemini and open
    private void setTransactionFail(Transaction txn, int failType) {
        Log.v(MmsApp.TXN_TAG, "set Transaction Fail. fail Type=" + failType);

        if (MmsApp.mGnMultiSimMessage && SMART) {//guoyx 20130226
            bWaitingConxn = false;
        }
        
        Uri uri = null;
        if (txn instanceof SendTransaction) {
            Log.d(MmsApp.TXN_TAG, "set Transaction Fail. :Send");
            uri = ((SendTransaction)txn).getSendReqUri();
        } else if (txn instanceof NotificationTransaction) {
            Log.d(MmsApp.TXN_TAG, "set Transaction Fail. :Notification");
            uri = ((NotificationTransaction)txn).getNotTrxnUri();
        } else if (txn instanceof RetrieveTransaction) {
            Log.d(MmsApp.TXN_TAG, "set Transaction Fail. :Retrieve");
            uri = ((RetrieveTransaction)txn).getRtrTrxnUri();
        } else if (txn instanceof ReadRecTransaction) {
            Log.d(MmsApp.TXN_TAG, "set Transaction Fail. :ReadRec");
            uri = ((ReadRecTransaction)txn).getRrecTrxnUri();
            // add this for read report.
            //if the read report is failed to open connection.mark it sent(129).i.e. only try to send once.
            //[or mark 128, this is another policy, this will resend next time into UI and out.]
            ContentValues values = new ContentValues(1);
            values.put(Mms.READ_REPORT, 129);
            SqliteWrapper.update(getApplicationContext(), getApplicationContext().getContentResolver(),
                                uri,
                                values,
                                null, null);
            txn.mTransactionState.setState(TransactionState.FAILED);
            txn.mTransactionState.setContentUri(uri);            
            txn.attach(TransactionService.this);
            txn.notifyObservers();
            return;
        } else {
            Log.d(MmsApp.TXN_TAG, "set Transaction Fail. type cann't be recognised");
        }

        if (null != uri) {
            txn.mTransactionState.setContentUri(uri);
        }

        if (txn instanceof NotificationTransaction) {
            DownloadManager downloadManager = DownloadManager.getInstance();
            boolean autoDownload = false;
            // add for gemini
            if (MmsApp.mGnMultiSimMessage) { //Gionee guoyx 20130226
                autoDownload = downloadManager.isAuto(txn.mSimId);
            } else {
                autoDownload = downloadManager.isAuto();
            }

            if (!autoDownload) {
                txn.mTransactionState.setState(TransactionState.SUCCESS);
            } else {
                txn.mTransactionState.setState(TransactionState.FAILED);
            }
        } else {
            txn.mTransactionState.setState(TransactionState.FAILED);
        }

        txn.attach(TransactionService.this);
        Log.d(MmsApp.TXN_TAG, "attach this transaction.");
        
        long msgId = ContentUris.parseId(uri);

        Uri.Builder uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
        uriBuilder.appendQueryParameter("protocol", "mms");
        uriBuilder.appendQueryParameter("message", String.valueOf(msgId));

        Cursor cursor = SqliteWrapper.query(getApplicationContext(), 
                                            getApplicationContext().getContentResolver(),
                                            uriBuilder.build(), 
                                            null, null, null, null);

        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    // Mark the failed message as unread.
                    ContentValues readValues = new ContentValues(1);
                    readValues.put(Mms.READ, 0);
                    SqliteWrapper.update(getApplicationContext(), getApplicationContext().getContentResolver(),
                                    uri, readValues, null, null);
                            
                    DefaultRetryScheme scheme = new DefaultRetryScheme(getApplicationContext(), 100);
                    
                    ContentValues values = null;
                    if (FAILE_TYPE_PERMANENT == failType) {
                        values = new ContentValues(2);
                        values.put(PendingMessages.ERROR_TYPE,  MmsSms.ERR_TYPE_GENERIC_PERMANENT);
                        values.put(PendingMessages.RETRY_INDEX, scheme.getRetryLimit());

                        int columnIndex = cursor.getColumnIndexOrThrow(PendingMessages._ID);
                        long id = cursor.getLong(columnIndex);
                                            
                        SqliteWrapper.update(getApplicationContext(), 
                                            getApplicationContext().getContentResolver(),
                                            PendingMessages.CONTENT_URI,
                                            values, PendingMessages._ID + "=" + id, null);
                    }
                }
            }finally {
                cursor.close();
            }
        }

        txn.notifyObservers();
    }
    

    // add for gemini
    private void launchTransactionGemini(int serviceId, int simId, TransactionBundle txnBundle) {
        Message msg = mServiceHandler.obtainMessage(EVENT_TRANSACTION_REQUEST);
        msg.arg1 = serviceId;
        msg.arg2 = simId;
        msg.obj = txnBundle;

        Log.d(MmsApp.TXN_TAG, "launchTransactionGemini: sending message " + msg);
        Log.d(MmsApp.TXN_TAG, "launchTransactionGemini: simId=" + simId);
        mServiceHandler.sendMessage(msg);
    }

    //Gionee guoyx 20130226 add for CR00773050 begin
    void sleep(int ms) {
        try {
            Log.d(TAG, "Sleeping for "+ms+"(ms)...");
            Thread.currentThread().sleep(ms);
            Log.d(TAG, "Sleeping...Done!");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    //Gionee guoyx 20130226 add for CR00773050 end
    // add for gemini
    protected int beginMmsConnectivityGemini(int simId) throws IOException {
        // Take a wake lock so we don't fall asleep before the message is downloaded.
        createWakeLock();
        //Gionee guoyx 20130326 added for CR00786416 begin
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
        String detailState = ni.getDetailedState().toString();
        int dataState = GnTelephonyManager.getDataState();
        Log.d(MmsApp.TXN_TAG, "before=> NetworkInfo DetailedState:" + detailState 
                + ", State:" + ni.getState() 
                + ", Current data connection:" + dataState);
        //Gionee guoyx 20130326 added for CR00786416 end

        // convert sim id to slot id
        int slotId = SIMInfo.getSlotById(getApplicationContext(), simId);
        // Aurora xuyong 2014-06-20 modified for bug #5380 start
        //int result = GnConnectivityManager.startUsingNetworkFeatureGemini(mConnMgr, slotId);
        boolean dataEnabeld = connMgr.getMobileDataEnabled();
        while (!dataEnabeld) {
            setMobileDataEnabled(true);
            dataEnabeld = connMgr.getMobileDataEnabled();
        }
        sleep(5000);
        int result = connMgr.startUsingNetworkFeature( 
                ConnectivityManager.TYPE_MOBILE, GnPhone.FEATURE_ENABLE_MMS);
        // result == 2 tells us that we haven't open the mms channel
        int count = 0;
        while (dataEnabeld && result == 2 && count < 8) {
            sleep(5000);
            result = connMgr.startUsingNetworkFeature( 
                    ConnectivityManager.TYPE_MOBILE, GnPhone.FEATURE_ENABLE_MMS);
            count++;
        }
        // Aurora xuyong 2014-06-20 modified for bug #5380 end
        Log.d(MmsApp.TXN_TAG, "beginMmsConnectivityGemini: simId=" + simId + "\t slotId=" + slotId + "\t result=" + result);
        
        //Gionee guoyx 20130326 added for CR00786416 begin
        NetworkInfo ni1 = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
        String detailState1 = ni1.getDetailedState().toString();
        int dataState1 = GnTelephonyManager.getDataState();
        Log.d(MmsApp.TXN_TAG, "after=> NetworkInfo DetailedState:" + detailState1 
                + ", State:" + ni.getState() 
                + ", Current data connection:" + dataState1);
        //Gionee guoyx 20130326 added for CR00786416 end

        switch (result) {
            case GnPhone.APN_ALREADY_ACTIVE:
            case GnPhone.APN_REQUEST_STARTED:
                acquireWakeLock();
                if (SMART) {
                    sendBroadcast(new Intent(TRANSACTION_START));
                }
                //add this for time out mechanism
                setDataConnectionTimer(result);                
                return result;
            case GnPhone.APN_TYPE_NOT_AVAILABLE:
            case GnPhone.APN_REQUEST_FAILED:
                return result;
            default:
                throw new IOException("Cannot establish MMS connectivity");
        }
    }

    // add for gemini
    protected void endMmsConnectivityGemini(int simId) {
        try {
            // convert sim id to slot id
            int slotId = SIMInfo.getSlotById(getApplicationContext(), simId);
            
            Log.d(MmsApp.TXN_TAG, "endMmsConnectivityGemini: slot id = " + slotId);

            // cancel timer for renewal of lease
            mServiceHandler.removeMessages(EVENT_CONTINUE_MMS_CONNECTIVITY);
            if (mConnMgr != null) {
                // Aurora xuyong 2014-11-10 modified for bug #9329 start
                if ("NBL8910A".equals(SystemProperties.get("ro.gn.gnprojectid"))) {
                    GnConnectivityManager.stopUsingNetworkFeatureGemini(mConnMgr, slotId);
                } else {
                    setMobileDataEnabled(false);
                    mConnMgr.stopUsingNetworkFeature(
                            ConnectivityManager.TYPE_MOBILE, Phone.FEATURE_ENABLE_MMS);
                    MmsConfig.restoreDataSubscription();
                }
                // Aurora xuyong 2014-11-10 modified for bug #9329 end
                if (SMART) {
                    sendBroadcast(new Intent(TRANSACTION_STOP));
                }
            }
        } finally {
            releaseWakeLock();
            triggerMsgId = 0;
        }
    }

    //add for time out mechanism
    private void setDataConnectionTimer(int result) {
        if (result == GnPhone.APN_REQUEST_STARTED) {
            mIgnoreMsg = false;
            if (mServiceHandler.hasMessages(EVENT_PENDING_TIME_OUT) == false) {
                Log.d(MmsApp.TXN_TAG, "a timer is created.");
                Message msg = mServiceHandler.obtainMessage(EVENT_PENDING_TIME_OUT);
                mServiceHandler.sendMessageDelayed(msg, REQUEST_CONNECTION_TIME_OUT_LENGTH);
            }
        }
    }

    private int getPendingSize() {
        int pendingSize = 0;
        synchronized (mProcessing) {
            pendingSize = mPending.size();
        }
        return pendingSize;
    }

    private Transaction removePending(int index) {
        Transaction trxn = null;
        synchronized (mProcessing) {
            trxn = mPending.remove(index);
        }
        return trxn;
    }
    
    //add for 81452
    /*
    *    check whether the request data connection fail is caused by calling going on.
    */
    private boolean isDuringCall() {
        if(MmsApp.mGnMultiSimMessage){//guoyx 20130301
            synchronized (mPhoneStateLock) {
                mPhoneState = GnTelephonyManager.getCallStateGemini(GnPhone.GEMINI_SIM_1);
                mPhoneState2 = GnTelephonyManager.getCallStateGemini(GnPhone.GEMINI_SIM_2);
            }
            return (mPhoneState != TelephonyManager.CALL_STATE_IDLE)
                    ||(mPhoneState2 != TelephonyManager.CALL_STATE_IDLE);
        } else {
            synchronized (mPhoneStateLock) {
                mPhoneState = MmsApp.getApplication().getTelephonyManager().getCallState();
            }
            return mPhoneState != TelephonyManager.CALL_STATE_IDLE;
        }
    }
    
    private void callbackState() {
        if(MmsApp.mGnMultiSimMessage){//guoyx 20130301
            if (mPhoneState == TelephonyManager.CALL_STATE_IDLE &&
                mPhoneState2 == TelephonyManager.CALL_STATE_IDLE) {
                synchronized (mIdleLock) {
                    if (mEnableCallbackIdle) {
                        Message msg = mServiceHandler.obtainMessage(EVENT_SCAN_PENDING_MMS);
                        mServiceHandler.sendMessage(msg);
                        mEnableCallbackIdle = false;
                    }
                }
            }
        } else {
            if (mPhoneState == TelephonyManager.CALL_STATE_IDLE) {
                synchronized (mIdleLock) {
                    if (mEnableCallbackIdle) {
                        Message msg = mServiceHandler.obtainMessage(EVENT_SCAN_PENDING_MMS);
                        mServiceHandler.sendMessage(msg);
                        mEnableCallbackIdle = false;
                    }
                }
            }
        }
    }

    /*
    * register phone call listener
    */
    private void registerPhoneCallListener() {
        if(MmsApp.mGnMultiSimMessage){
            mPhoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    synchronized (mPhoneStateLock) {
                        mPhoneState = state;
                    }
                    if (mPhoneState == TelephonyManager.CALL_STATE_IDLE &&
                        mPhoneState2 == TelephonyManager.CALL_STATE_IDLE) {
                        mLastIdleSlot = GnPhone.GEMINI_SIM_1;
                    }
                    Log.d(MmsApp.TXN_TAG, "get slot0 new state:"+state+",slot1 current state:"+mPhoneState2
                        +",mEnableCallbackIdle:"+mEnableCallbackIdle+",mLastIdleSlot:"+mLastIdleSlot);
                    callbackState();
                }
            };
            mPhoneStateListener2 = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    synchronized (mPhoneStateLock) {
                        mPhoneState2 = state;
                    }
                    if (mPhoneState == TelephonyManager.CALL_STATE_IDLE &&
                        mPhoneState2 == TelephonyManager.CALL_STATE_IDLE) {
                        mLastIdleSlot = GnPhone.GEMINI_SIM_2;
                    }
                    Log.d(MmsApp.TXN_TAG, "get slot1 new state:"+state+",slot0 current state:"+mPhoneState
                        +",mEnableCallbackIdle:"+mEnableCallbackIdle+",mLastIdleSlot:"+mLastIdleSlot);
                    callbackState();
                }
            };
            GnTelephonyManager.listenGemini(mPhoneStateListener,
                            PhoneStateListener.LISTEN_CALL_STATE, GnPhone.GEMINI_SIM_1);
            
            GnTelephonyManager.listenGemini(mPhoneStateListener2,
                            PhoneStateListener.LISTEN_CALL_STATE, GnPhone.GEMINI_SIM_2);
        }/* else if (MmsApp.mQcMultiSimEnabled) {
            mPhoneStateListener = new PhoneStateListener(){//GnPhone.GEMINI_SIM_1) {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    synchronized (mPhoneStateLock) {
                        mPhoneState = state;
                    }
                    if (mPhoneState == TelephonyManager.CALL_STATE_IDLE &&
                        mPhoneState2 == TelephonyManager.CALL_STATE_IDLE) {
                        mLastIdleSlot = GnPhone.GEMINI_SIM_1;
                    }
                    Log.d(MmsApp.TXN_TAG, "get slot0 new state:"+state+",slot1 current state:"+mPhoneState2
                        +",mEnableCallbackIdle:"+mEnableCallbackIdle+",mLastIdleSlot:"+mLastIdleSlot);
                    callbackState();
                }
            };
            mPhoneStateListener2 = new PhoneStateListener(){//GnPhone.GEMINI_SIM_2) {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    synchronized (mPhoneStateLock) {
                        mPhoneState2 = state;
                    }
                    if (mPhoneState == TelephonyManager.CALL_STATE_IDLE &&
                        mPhoneState2 == TelephonyManager.CALL_STATE_IDLE) {
                        mLastIdleSlot = GnPhone.GEMINI_SIM_2;
                    }
                    Log.d(MmsApp.TXN_TAG, "get slot1 new state:"+state+",slot0 current state:"+mPhoneState
                        +",mEnableCallbackIdle:"+mEnableCallbackIdle+",mLastIdleSlot:"+mLastIdleSlot);
                    callbackState();
                }
            };
            GnTelephonyManager.listenGemini(mPhoneStateListener,
                            PhoneStateListener.LISTEN_CALL_STATE, GnPhone.GEMINI_SIM_1);
            
            GnTelephonyManager.listenGemini(mPhoneStateListener2,
                            PhoneStateListener.LISTEN_CALL_STATE, GnPhone.GEMINI_SIM_2);
        }*/ else {
            mPhoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    synchronized (mPhoneStateLock) {
                        mPhoneState = state;
                    }
                    Log.d(MmsApp.TXN_TAG, "get new state:"+state+",mEnableCallbackIdle:"+mEnableCallbackIdle);
                    callbackState();
                }
            };
            MmsApp.getApplication().getTelephonyManager()
                    .listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }        
    }

    /*
    *   arg1 useless currently
    *   arg2 slotId in gemini mode
    */
    private void renewMmsConnectivity(int arg1, int arg2) {
        Log.d(MmsApp.TXN_TAG, "renewMmsConnectivity arg1=" + arg1 + ", slot=" + arg2);
        // Set a timer to keep renewing our "lease" on the MMS connection
        if (MmsApp.mGnMultiSimMessage) {//guoyx 20130226
            mServiceHandler.sendMessageDelayed(
                                mServiceHandler.obtainMessage(EVENT_CONTINUE_MMS_CONNECTIVITY, arg1, arg2),
                                APN_EXTENSION_WAIT);            
        } else {
            mServiceHandler.sendMessageDelayed(
                                mServiceHandler.obtainMessage(EVENT_CONTINUE_MMS_CONNECTIVITY),
                                        APN_EXTENSION_WAIT);
        }
    }

    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.w(TAG, "ConnectivityBroadcastReceiver.onReceive() action: " + action);
            }

            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION_IMMEDIATE)) {
                return;
            }
            
            // M: check sticky intent
            if (mFWStickyIntent != null) {
                Log.d(MmsApp.TXN_TAG, "get sticky intent" + mFWStickyIntent);
                mFWStickyIntent = null;
                return;
            }

            boolean noConnectivity =
                intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            NetworkInfo networkInfo = (NetworkInfo)
                intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

            /*
             * If we are being informed that connectivity has been established
             * to allow MMS traffic, then proceed with processing the pending
             * transaction, if any.
             */

            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "Handle ConnectivityBroadcastReceiver.onReceive(): " + networkInfo);
            }

            // Check availability of the mobile network.
            if ((networkInfo == null) || (networkInfo.getType() !=
                    ConnectivityManager.TYPE_MOBILE_MMS)) {
                if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                    Log.v(TAG, "   type is not TYPE_MOBILE_MMS, bail");
                }
                Log.d(MmsApp.TXN_TAG, "ignore a none mobile mms status message.");
                return;
            }

            // M: we still keep EVENT_DATA_STATE_CHANGED case and use it.
            Message msg = mServiceHandler.obtainMessage(EVENT_DATA_STATE_CHANGED);
            msg.arg2 = intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY/*simId*/, -1);
            mServiceHandler.sendMessage(msg);
        }
    };
    
    private void setMobileDataEnabled(boolean enable) {
        try {
            ITelephony iTel = ITelephony.Stub.asInterface(
                    ServiceManager.getService(Context.TELEPHONY_SERVICE));

            if (null == iTel) {
                return;
            }

            iTel.setDataEnabled(enable);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }
}
