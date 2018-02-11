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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import gionee.content.GnIntent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.MmsSms.PendingMessages;
import gionee.provider.GnTelephony.SIMInfo;
import android.util.Log;
import com.android.internal.telephony.Phone;
import com.gionee.internal.telephony.GnPhone;
import gionee.provider.GnTelephony;
import com.android.internal.telephony.TelephonyIntents;
import com.android.mms.LogTag;
//gionee gaoj 2012-8-21 added for CR00678365 start
import com.gionee.mms.regularlysend.RegularlyMainActivity;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.SendReq;
import com.aurora.android.mms.MmsException;
import com.aurora.android.mms.pdu.GenericPdu;
//Aurora xuyong 2013-11-15 modified for google adapt end
import android.content.ContentUris;
//gionee gaoj 2012-8-21 added for CR00678365 end
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.util.PduCache;
//Aurora xuyong 2013-11-15 modified for google adapt end
// add for gemini
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.database.sqlite.SQLiteDiskIOException;

import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.MessageUtils;
//import com.android.mms.ui.MultiDeleteActivity;
import com.android.mms.util.DownloadManager;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.android.mms.pdu.PduPersister;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.aurora.featureoption.FeatureOption;
import android.os.SystemProperties;

/**
 * MmsSystemEventReceiver receives the
 * {@link android.content.intent.ACTION_BOOT_COMPLETED},
 * {@link com.android.internal.telephony.TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED}
 * and performs a series of operations which may include:
 * <ul>
 * <li>Show/hide the icon in notification area which is used to indicate
 * whether there is new incoming message.</li>
 * <li>Resend the MM's in the outbox.</li>
 * </ul>
 */
public class MmsSystemEventReceiver extends BroadcastReceiver {
    private static final String TAG = "MmsSystemEventReceiver";
    private static MmsSystemEventReceiver sMmsSystemEventReceiver;
    private OnShutDownListener saveDraft;
    //gionee zengxuanhui 20120917 add for CR00693783 begin
    private static final boolean gnGeminiRingtoneSupport = 
                SystemProperties.get("ro.gn.gemini.ringtone.support").equals("yes");
    //gionee zengxuanhui 20120917 add for CR00693783 end
    private static void wakeUpService(Context context) {
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "wakeUpService: start transaction service ...");
        }

        context.startService(new Intent(context, TransactionService.class));
    }

    // add for gemini
    private static void wakeUpServiceGemini(Context context, int simId) {
        Log.v(MmsApp.TXN_TAG, "wakeUpServiceGemini: start transaction service ... simId=" + simId);
        
        Intent it = new Intent(context, TransactionService.class);
        it.putExtra(GnPhone.GEMINI_SIM_ID_KEY, simId);

        context.startService(it);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "Intent received: " + intent);
        }

        String action = intent.getAction();
        if (action.equals(Mms.Intents.CONTENT_CHANGED_ACTION)) {
            Uri changed = (Uri) intent.getParcelableExtra(Mms.Intents.DELETED_CONTENTS);
            PduCache.getInstance().purge(changed);
            Log.d(MmsApp.TXN_TAG, "Mms.Intents.CONTENT_CHANGED_ACTION: " + changed);
        } else if (action.equals(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)) {
            // Aurora xuyong 2013-11-15 modified for S4 adapt start
            String state = intent.getStringExtra("apnType");//Phone.STATE_KEY);
            // Aurora xuyong 2013-11-15 modified for S4 adapt end

            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "ANY_DATA_STATE event received: " + state);
            }
            // Aurora xuyong 2013-11-15 modified for S4 adapt start
            String apnType = intent.getStringExtra("apnType");//Phone.DATA_APN_TYPE_KEY);
            // Aurora xuyong 2013-11-15 modified for S4 adapt end

            //if (state.equals("CONNECTED")) {
            // Aurora xuyong 2013-11-15 modified for S4 adapt start
            if (/*Phone.APN_TYPE_MMS*/"mms".equals(apnType)) {
            // Aurora xuyong 2013-11-15 modified for S4 adapt end
                Log.d(MmsApp.TXN_TAG, "TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED, type is mms.");
                // if the network is not available for mms, keep listening
                ConnectivityManager ConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = ConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
                if (ni != null && !ni.isAvailable()) {
                    Log.d(MmsApp.TXN_TAG, "network is not available for mms, keep listening.");
                    return;
                }
                
                unRegisterForConnectionStateChanges(context);
                // add for gemini
                if(MmsApp.mGnMultiSimMessage){
                    // conver slot id to sim id
                    SIMInfo si = SIMInfo.getSIMInfoBySlot(context, 
                            intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, GnPhone.GEMINI_SIM_1));
                    if (null == si) {
                        Log.e(MmsApp.TXN_TAG, "System event receiver: SIMInfo is null for slot " + intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, GnPhone.GEMINI_SIM_1));
                        return;
                    }
                    int simId = (int)si.mSimId;
                    //gionee zengxuanhui 20120917 add for CR00693783 begin
                    if(gnGeminiRingtoneSupport){
                        int slotId = SIMInfo.getSlotById(context, simId);
                        if(slotId == 1){
                            MessagingNotification.setIncomingSmsSimId(GnPhone.GEMINI_SIM_2);
                        }else{
                            MessagingNotification.setIncomingSmsSimId(GnPhone.GEMINI_SIM_1);
                        }
                    }
                    //gionee zengxuanhui 20120917 add for CR00693783 end
                    wakeUpServiceGemini(context, simId/*intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY,Phone.GEMINI_SIM_1)*/);
                } else{
                    wakeUpService(context);
                }
            }
        } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(MmsApp.TXN_TAG, "Intent.ACTION_BOOT_COMPLETED");
            final Context contxt = context;
            new Thread(new Runnable() {
                public void run() {
                    //gionee gaoj 2012-8-21 added for CR00678365 start
                    if (MmsApp.mGnRegularlyMsgSend) {
                        reSetRegularlyTime(contxt);
                    }
                    //gionee gaoj 2012-8-21 added for CR00678365 end
                    setPendingMessageFailed(contxt);
                    setNotificationIndUnstarted(contxt);
                }
            }).start();
            // We should check whether there are unread incoming
            // messages in the Inbox and then update the notification icon.
            // Called on the UI thread so don't block.
            MessagingNotification.nonBlockingUpdateNewMessageIndicator(context, false, false);
        } else if (action.equals(GnIntent.ACTION_SIM_SETTINGS_INFO_CHANGED)) {
            int simId = (int)intent.getLongExtra("simid", -1);
            MessageUtils.simInfoMap.remove(simId);
            MessageUtils.getSimInfo(context, simId);
        } else if (action.equals(Intent.ACTION_SHUTDOWN)) {
            saveDraft = (OnShutDownListener) ComposeMessageActivity.getComposeContext();
            if (saveDraft != null) {
                saveDraft.onShutDown();
            }
        } else if (action.equals(Intent.ACTION_DEVICE_STORAGE_FULL)) {
            MmsConfig.setDeviceStorageFullStatus(true);
        } else if (action.equals(Intent.ACTION_DEVICE_STORAGE_NOT_FULL)) {
            MmsConfig.setDeviceStorageFullStatus(false);
        }
    }

    public interface OnShutDownListener {
        void onShutDown();
    }
    
    public static void setPendingMessageFailed(final Context context) {
        Log.d(MmsApp.TXN_TAG, "setPendingMessageFailed");
        Cursor cursor = PduPersister.getPduPersister(context).getPendingMessages(
                Long.MAX_VALUE/*System.currentTimeMillis()*/);
        if (cursor != null) {
            try {
                int count = cursor.getCount();
                Log.d(MmsApp.TXN_TAG, "setPendingMessageFailed: Pending Message Size=" + count);

                if (count == 0 ) {
                    return;
                }
                DefaultRetryScheme scheme = new DefaultRetryScheme(context, 100);
                ContentValues values = null;
                int columnIndex = 0;
                int columnType = 0;
                int id = 0;
                int type = 0;
                while (cursor.moveToNext()) {
                    columnIndex = cursor.getColumnIndexOrThrow(PendingMessages._ID);
                    id = cursor.getInt(columnIndex);

                    columnType = cursor.getColumnIndexOrThrow(PendingMessages.MSG_TYPE);
                    type = cursor.getInt(columnType);

                    Log.d(MmsApp.TXN_TAG, "setPendingMessageFailed: type=" + type + "; MsgId=" + id);

                    //gionee gaoj 2012-8-23 added for CR00678305 start
                    boolean isRightDate = true;
                    if (MmsApp.mGnRegularlyMsgSend && type == PduHeaders.MESSAGE_TYPE_SEND_REQ) {
                        try {
                            isRightDate = CheckDate(context, id);
                        } catch (MmsException e) {
                            // TODO: handle exception
                        }
                    }
                    //gionee gaoj 2012-8-23 added for CR00678305 end
                    if (type == PduHeaders.MESSAGE_TYPE_SEND_REQ && isRightDate) {
                        values = new ContentValues(2);
                        values.put(PendingMessages.ERROR_TYPE,  MmsSms.ERR_TYPE_GENERIC_PERMANENT);
                        values.put(PendingMessages.RETRY_INDEX, scheme.getRetryLimit());
                        SqliteWrapper.update(context, 
                                context.getContentResolver(),
                                PendingMessages.CONTENT_URI,
                                values, PendingMessages._ID + "=" + id, null);
                    }
                }
            } catch (SQLiteDiskIOException e) {
                // Ignore
                Log.e(MmsApp.TXN_TAG, "SQLiteDiskIOException caught while set pending message failed", e);
            } finally {
                cursor.close();
            }
        } else {
            Log.d(MmsApp.TXN_TAG, "setPendingMessageFailed: no pending messages.");
        }
    }

    public static void setNotificationIndUnstarted(final Context context) {
        Log.d(MmsApp.TXN_TAG, "setNotificationIndUnstarted");
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),Mms.CONTENT_URI,
                // Aurora xuyong 2014-11-05 modified for privacy feature start
                new String[] {Mms._ID,Mms.STATUS}, Mms.MESSAGE_TYPE + "=" + PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND + " AND is_privacy >= 0", null, null);
                // Aurora xuyong 2014-11-05 modified for privacy feature end
        if (cursor != null) {
            try {
                int count = cursor.getCount();
                Log.d(MmsApp.TXN_TAG, "setNotificationIndUnstarted: Message Size=" + count);

                if (count == 0 ) {
                    return;
                }

                ContentValues values = null;
                int id = 0;
                int status = 0;
                while (cursor.moveToNext()) {
                    id = cursor.getInt(0);
                    status = cursor.getInt(1);
                    Log.d(MmsApp.TXN_TAG, "setNotificationIndUnstarted: MsgId=" + id + "; status=" + status);

                    //if (status == PduHeaders.STATUS_RETRIEVED || status == PduHeaders.STATUS_INDETERMINATE) {
                    if (DownloadManager.STATE_DOWNLOADING == (status &~ DownloadManager.DEFERRED_MASK)) {
                        values = new ContentValues(1);
                        values.put(Mms.STATUS,  PduHeaders.STATUS_UNRECOGNIZED);
                        SqliteWrapper.update(context, 
                                context.getContentResolver(),
                                Mms.CONTENT_URI,
                                values, Mms._ID + "=" + id, null);
                    }
                }
            } catch (SQLiteDiskIOException e) {
                // Ignore
                Log.e(MmsApp.TXN_TAG, "SQLiteDiskIOException caught while set notification ind unstart", e);
            } finally {
                cursor.close();
            }
        } else {
            Log.d(MmsApp.TXN_TAG, "setPendingMessageFailed: no pending messages.");
        }
    }

    public static void registerForConnectionStateChanges(Context context) {
        Log.d(MmsApp.TXN_TAG, "registerForConnectionStateChanges");
        unRegisterForConnectionStateChanges(context);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "registerForConnectionStateChanges");
        }
        if (sMmsSystemEventReceiver == null) {
            sMmsSystemEventReceiver = new MmsSystemEventReceiver();
        }

        context.registerReceiver(sMmsSystemEventReceiver, intentFilter);
    }

    public static void unRegisterForConnectionStateChanges(Context context) {
        Log.d(MmsApp.TXN_TAG, "unRegisterForConnectionStateChanges");
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "unRegisterForConnectionStateChanges");
        }
        if (sMmsSystemEventReceiver != null) {
            try {
                context.unregisterReceiver(sMmsSystemEventReceiver);
            } catch (IllegalArgumentException e) {
                // Allow un-matched register-unregister calls
            }
        }
    }

    //gionee gaoj 2012-8-21 added for CR00678365 start
    public static void reSetRegularlyTime(Context context) {
        Cursor cursor = null;
        long date = -1;
        String[] MMS_QUERY_COLUMNS = { "date" };
        // Aurora xuyong 2014-11-05 modified for privacy feature start
        final String selection = Mms.DATE + " > " + System.currentTimeMillis() / 1000L + " AND is_privacy >= 0";
        // Aurora xuyong 2014-11-05 modified for privacy feature end
        try {
            cursor = SqliteWrapper.query(context, context.getContentResolver(),
                    Mms.Outbox.CONTENT_URI, MMS_QUERY_COLUMNS,
                    selection, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    date = cursor.getLong(0) * 1000L;
                    RegularlyMainActivity.reSetAlarmManager(context, date);
                }
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    private static boolean CheckDate(Context context, int msgid) throws MmsException {
        long date = -1;
        Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, msgid);
        PduPersister p = PduPersister.getPduPersister(context);
        GenericPdu pdu = p.load(uri);
        //gionee gaoj 2012-9-18 added for CR00693731 start
        if (pdu.getMessageType() != PduHeaders.MESSAGE_TYPE_SEND_REQ) {
            throw new MmsException("Invalid message: " + pdu.getMessageType());
        }
        //gionee gaoj 2012-9-18 added for CR00693731 end
        SendReq sendReq = (SendReq) pdu;
        date = sendReq.getDate() * 1000L;
        if (date > System.currentTimeMillis()) {
            return false;
        }
        return true;
    }
    //gionee gaoj 2012-8-21 added for CR00678365 end
}
