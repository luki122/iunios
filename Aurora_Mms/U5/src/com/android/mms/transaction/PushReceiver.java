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

import static android.provider.Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION;
//Aurora xuyong 2013-11-15 modified for google adapt start
import static com.aurora.android.mms.pdu.PduHeaders.MESSAGE_TYPE_DELIVERY_IND;
import static com.aurora.android.mms.pdu.PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND;
import static com.aurora.android.mms.pdu.PduHeaders.MESSAGE_TYPE_READ_ORIG_IND;
//Aurora xuyong 2013-11-15 modified for google adapt end

import com.android.mms.MmsConfig;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.ContentType;
import com.aurora.android.mms.MmsException;
import com.aurora.android.mms.pdu.DeliveryInd;
import com.aurora.android.mms.pdu.GenericPdu;
import com.aurora.android.mms.pdu.NotificationInd;
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.android.mms.pdu.PduParser;
import com.aurora.android.mms.pdu.PduPersister;
import com.aurora.android.mms.pdu.ReadOrigInd;
// Aurora xuyong 2014-07-02 added for reject feature start
import com.aurora.android.mms.pdu.MultimediaMessagePdu;
import com.aurora.android.mms.pdu.PduBody;
import com.aurora.android.mms.pdu.PduPart;
import com.aurora.android.mms.pdu.EncodedStringValue;
// Aurora xuyong 2014-07-02 added for reject feature end
//Aurora xuyong 2013-11-15 modified for google adapt end
import android.database.sqlite.SqliteWrapper;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import gionee.provider.GnTelephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import gionee.provider.GnTelephony.SIMInfo;
import gionee.provider.GnTelephony.WapPush;
import android.util.Log;
// add for gemini
import com.aurora.featureoption.FeatureOption;
// Aurora xuyong 2014-07-02 added for reject feature start
import com.aurora.mms.util.Utils;
// Aurora xuyong 2014-07-02 added for reject feature end
import com.gionee.internal.telephony.GnPhone;
import gionee.provider.GnTelephony.MmsSms.PendingMessages;
import com.android.mms.MmsApp;
// Aurora xuyong 2014-07-02 added for reject feature start
import java.util.ArrayList;
// Aurora xuyong 2014-07-02 added for reject feature end
import java.util.HashMap;


//gionee gaoj 2012-4-26 added for CR00555790 start
import android.os.SystemProperties;
import android.provider.Telephony.Threads;
import android.text.TextUtils;
import com.android.mms.R;
import android.os.Handler;
import android.content.SharedPreferences;
import aurora.preference.AuroraPreferenceManager;

import com.android.mms.data.ContactList;
import com.android.mms.ui.MessagingPreferenceActivity;
import android.widget.Toast;
//gionee gaoj 2012-4-26 added for CR00555790 end

//GIONEE:wangfei 2012-09-03 add for CR00686851 begin
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.NotificationManager;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
//GIONEE:wangfei 2012-09-03 add for CR00686851 end
/**
 * Receives Intent.WAP_PUSH_RECEIVED_ACTION intents and starts the
 * TransactionService by passing the push-data to it.
 */
public class PushReceiver extends BroadcastReceiver {
    private static final String TAG = "PushReceiver";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    //gionee gaoj 2012-4-26 added for CR00555790 start
    private static Handler mToastHandler = new Handler();
    //gionee gaoj 2012-4-26 added for CR00555790 end

    //GIONEE:wangfei 2012-09-03 add for CR00686851 begin
    private static final boolean gnNgmflag = SystemProperties.get("ro.gn.oversea.custom").equals("ITALY_NGM");
    private static final boolean gnGeminiRingtoneSupport = SystemProperties.get("ro.gn.gemini.ringtone.support").equals("yes");
    //GIONEE:wangfei 2012-09-03 add for CR00686851 end
    private PowerManager.WakeLock wl;


    //GIONEE:tianliang 2012-09-17 modify for CR00689081 start
    public static final boolean gnOverseaflag = SystemProperties.get("ro.gn.oversea.product").equals("yes");
    //GIONEE:tianliang 2012-09-17 modify for CR00689081 end

    private class ReceivePushTask extends AsyncTask<Intent,Void,Void> {
        private Context mContext;
        public ReceivePushTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Intent... intents) {
            Intent intent = intents[0];
            // Aurora xuyong 2015-06-29 modified for android 5.1+ adapt start
            int slotId = -1;
            if (!Utils.hasLollipop()) {
            	if (MmsApp.mGnMultiSimMessage) {
                    slotId = intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1);
            	} else {
            		slotId = 0;
            	}
            } else {
            	slotId = intent.getIntExtra("slot", -1);
            	intent.putExtra(GnPhone.GEMINI_SIM_ID_KEY, slotId);
            }
            Log.d(MmsApp.TXN_TAG, "do In Background, slotId=" + slotId);
            // Aurora xuyong 2015-06-29 modfiied for android 5.1+ adapt end
            // Get raw PDU push-data from the message and parse it
            byte[] pushData = intent.getByteArrayExtra("data");
            PduParser parser = new PduParser(pushData);
            GenericPdu pdu = parser.parse();

            if (null == pdu) {
                Log.e(TAG, "Invalid PUSH data");
                return null;
            }

            PduPersister p = PduPersister.getPduPersister(mContext);
            ContentResolver cr = mContext.getContentResolver();
            int type = pdu.getMessageType();
            long threadId = -1;

            try {
                switch (type) {
                    case MESSAGE_TYPE_DELIVERY_IND:
                        Log.d(MmsApp.TXN_TAG, "type=MESSAGE_TYPE_DELIVERY_IND");
                    case MESSAGE_TYPE_READ_ORIG_IND: {
                        if (type == MESSAGE_TYPE_READ_ORIG_IND) {
                            Log.d(MmsApp.TXN_TAG, "type=MESSAGE_TYPE_READ_ORIG_IND");
                        }
                        threadId = findThreadId(mContext, pdu, type);
                        if (threadId == -1) {
                            // The associated SendReq isn't found, therefore skip
                            // processing this PDU.
                            break;
                        }
                       // Aurora xuyong 2014-10-23 modified for privacy feature start
                       Uri uri = null;
                       if (MmsApp.sHasPrivacyFeature) {
                           long privacy = 0;
                           String address = pdu.getFrom().getString();
                           if (!Mms.isEmailAddress(address)) {
                               privacy = Utils.getFristPrivacyId(mContext, address);
                           }
                           uri = p.persist(pdu, Inbox.CONTENT_URI, privacy);
                       } else {
                           uri = p.persist(pdu, Inbox.CONTENT_URI);
                       }
                       // Aurora xuyong 2014-10-23 modified for privacy feature end
                        // Update thread ID for ReadOrigInd & DeliveryInd.
                        // Aurora xuyong 2014-07-02 modified for reject feature start
                        ContentValues values = new ContentValues();
                        // Aurora xuyong 2014-07-02 modified for reject feature end
                        values.put(Mms.THREAD_ID, threadId);
                        //gionee gaoj 2012-4-26 added for CR00555790 start
                        //GIONEE:wangfei 2012-09-03 add for CR00686851
                        //GIONEE:tianliang 2012-09-17 modify for CR00689081 start
                        if (MmsApp.mGnMessageSupport || gnOverseaflag) {
                        //GIONEE:tianliang 2012-09-17 modify for CR00689081 end
                            values.put(Mms.READ, 1);
                        } else {
                            values.put(Mms.READ, 0);
                        }
                        // Aurora xuyong 2014-07-16 added for bug #6446 start
                        if (MmsApp.sHasRejectFeature) {
                             // Aurora xuyong 2014-11-08 modified for reject new feature start
                             if (Utils.needRejectBlackMms(pdu, mContext)) {
                             // Aurora xuyong 2014-11-08 modified for reject new feature end
                                    values.put("reject", 1);
                                // Aurora xuyong 2014-09-19 added for aurora reject feature start
                                    values.put("newComing", 1);
                                // Aurora xuyong 2014-09-19 added for aurora reject feature end
                                // Aurora xuyong 2014-08-23 added for bug #7909 start
                                    MessagingNotification.setIsRejectMsg(true);
                                // Aurora xuyong 2014-08-23 added for bug #7909 end
                             }
                         }
                        // Aurora xuyong 2014-07-16 added for bug #6446 end
                        //gionee gaoj 2012-4-26 added for CR00555790 end
                        SqliteWrapper.update(mContext, cr, uri, values, null, null);
                        //gionee gaoj 2012-4-26 added for CR00555790 start
                        //GIONEE:wangfei 2012-09-03 add for CR00686851
                        //GIONEE:tianliang 2012-09-17 modify for CR00689081 start
                        if (MmsApp.mGnMessageSupport || gnOverseaflag) {
                        //GIONEE:tianliang 2012-09-17 modify for CR00689081 end
                            Log.d("MMSLog", "type=%d " + type);
                            SharedPreferences sp = AuroraPreferenceManager
                                    .getDefaultSharedPreferences(mContext);
                            //gionee gaoj 2013-3-31 added for CR00791228 start
                            if (MmsApp.mGnMultiSimMessage) {
                                SIMInfo si = SIMInfo.getSIMInfoBySlot(mContext, intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1));
                                if (null == si) {
                                    Log.e(MmsApp.TXN_TAG, "PushReceiver:SIMInfo is null for slot " + intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1));
                                    break;
                                }
                                // Aurora xuyong 2014-05-26 modified for multisim feature start
                                boolean deliverReport = sp.getBoolean(
                                        // Aurora xuyong 2014-06-09 modified for bug #5554 start
                                        MessagingPreferenceActivity.MMS_DELIVERY_REPORT_MODE, false);
                                        // Aurora xuyong 2014-06-09 modified for bug #5554 end
                                Log.d("MMSLog", "deliverReport = " + deliverReport);
                                if (type == MESSAGE_TYPE_DELIVERY_IND && deliverReport == false) {
                                // Aurora xuyong 2014-05-26 modified for multisim feature end
                                    break;
                                }
                            } else {
                                //gionee gaoj 2013-3-31 added for CR00791228 end
                            boolean deliverReport = sp.getBoolean(
                                    // Aurora xuyong 2014-06-09 modified for bug #5554 start
                                    MessagingPreferenceActivity.MMS_DELIVERY_REPORT_MODE, false);
                                    // Aurora xuyong 2014-06-09 modified for bug #5554 end
                            boolean readReport = sp.getBoolean(
                                    MessagingPreferenceActivity.READ_REPORT_MODE, true);

                            if (type == MESSAGE_TYPE_DELIVERY_IND && deliverReport == false) {
                                break;
                            }

                            if (type == MESSAGE_TYPE_READ_ORIG_IND && readReport == false) {
                                break;
                            }
                            //gionee gaoj 2013-3-31 added for CR00791228 start
                            }
                            //gionee gaoj 2013-3-31 added for CR00791228 end
                            

                            final Uri sAllThreadsUri = Threads.CONTENT_URI.buildUpon()
                                    .appendQueryParameter("simple", "true").build();

                            Cursor cursor = SqliteWrapper.query(mContext,
                                    mContext.getContentResolver(), sAllThreadsUri, new String[] {
                                        Threads.RECIPIENT_IDS
                                    }, Threads._ID + "=" + threadId, null, null);

                            String toastString = null;
                            if (cursor != null && cursor.getCount() != 0) {
                                cursor.moveToFirst();
                                String recipientIds = cursor.getString(cursor.getColumnIndex(Threads.RECIPIENT_IDS));

                                if (!TextUtils.isEmpty(recipientIds)) {
                                    ContactList recipients = ContactList.getByIds(mContext, recipientIds,
                                            false);

                                    String numbers[] = recipients.getNumbers();
                                    //GIONEE:wangfei 2012-09-03 add for CR00686851 begin
                                    if(gnNgmflag){
                                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm dd-MM");       
                                        Date curDate = new Date(System.currentTimeMillis());      
                                        String curtime = mContext.getString(R.string.gn_delivery_report_at) + " " + formatter.format(curDate);                                         
                                        if (numbers != null && numbers.length == 1) {
                                            if (type == MESSAGE_TYPE_DELIVERY_IND) {
                                                toastString = String.format(mContext.getString(R.string.mms_deliver_success_with_address),numbers[0]) + " " + curtime;
                                            } else {
                                                toastString = String.format(mContext.getString(R.string.mms_read_success_with_address),numbers[0]) + " " + curtime;
                                            }
                                        } else {
                                            if (type == MESSAGE_TYPE_DELIVERY_IND) {
                                                toastString = mContext.getString(R.string.mms_deliver_success) + " " + curtime;
                                            } else {
                                                toastString = mContext.getString(R.string.mms_read_success) + " " + curtime;
                                            }
                                        }                                        
                                    }else{
                                        if (numbers != null && numbers.length == 1) {
                                            if (type == MESSAGE_TYPE_DELIVERY_IND) {
                                                toastString = String.format(mContext.getString(R.string.mms_deliver_success_with_address),numbers[0]);
                                            } else {
                                                toastString = String.format(mContext.getString(R.string.mms_read_success_with_address),numbers[0]);
                                            }
                                        } else {
                                            if (type == MESSAGE_TYPE_DELIVERY_IND) {
                                                toastString = mContext.getString(R.string.mms_deliver_success);
                                            } else {
                                                toastString = mContext.getString(R.string.mms_read_success);
                                            }
                                        }
                                    }
                                    //GIONEE:wangfei 2012-09-03 add for CR00686851 end
                                }
                            }

                            if (cursor != null) {
                                cursor.close();
                            }

                            if (toastString != null) {
                                final String toast = toastString;
                                mToastHandler.post(new Runnable() {
                                    public void run() {
                                        Toast.makeText(mContext, toast, 5000).show();
                                    }
                                });
                            }

                            //GIONEE:wangfei 2012-09-03 add for CR00686851 begin
                            if(gnNgmflag == true){
                                int delicon = R.drawable.aurora_stat_notify_sms;
                                String deltitle = mContext.getString(R.string.delivery_report_activity);
                                Notification notification = new Notification(delicon, toastString, System.currentTimeMillis()); //System.currentTimeMillis()
                                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
                            
                                // Update the notification.
                                notification.setLatestEventInfo(mContext, deltitle, toastString, pendingIntent);
                                
                                AudioManager audioManager =
                                    (AudioManager)mContext.getSystemService(mContext.AUDIO_SERVICE);
                            
                                sp = AuroraPreferenceManager.getDefaultSharedPreferences(mContext);     
                                String ringtoneStr = sp.getString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE, null);
                                if (gnGeminiRingtoneSupport == true && MessagingNotification.sentSmsSimId == GnPhone.GEMINI_SIM_2) {
                                       ringtoneStr = sp.getString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE2,
                                          null);
                                }
                            
                                notification.sound = TextUtils.isEmpty(ringtoneStr) ? null : Uri
                                        .parse(ringtoneStr);

                                // Uri ringtone = TextUtils.isEmpty(ringtoneStr) ? null : Uri.parse(ringtoneStr);
                                // processNotificationSound(context, notification, ringtone);
                                gn_processNotificationSound(mContext, notification);
                            
                                notification.defaults |= Notification.DEFAULT_LIGHTS;
                                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                            
                                NotificationManager nm = (NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
                            
                                nm.notify(131, notification);
                            }
                            //GIONEE:wangfei 2012-09-03 add for CR00686851 end

                            break;
                        }
                        //gionee gaoj 2012-4-26 added for CR00555790 end
                        break;
                    }
                    case MESSAGE_TYPE_NOTIFICATION_IND: {
                        Log.d(MmsApp.TXN_TAG, "type=MESSAGE_TYPE_NOTIFICATION_IND");
                        NotificationInd nInd = (NotificationInd) pdu;

                        if (MmsConfig.getTransIdEnabled()) {
                            byte [] contentLocation = nInd.getContentLocation();
                            if ('=' == contentLocation[contentLocation.length - 1]) {
                                byte [] transactionId = nInd.getTransactionId();
                                byte [] contentLocationWithId = new byte [contentLocation.length
                                                                          + transactionId.length];
                                System.arraycopy(contentLocation, 0, contentLocationWithId,
                                        0, contentLocation.length);
                                System.arraycopy(transactionId, 0, contentLocationWithId,
                                        contentLocation.length, transactionId.length);
                                nInd.setContentLocation(contentLocationWithId);
                            }
                        }

                        if (!isDuplicateNotification(mContext, nInd)) {
                          // Aurora xuyong 2014-10-23 modified for privacy feature start
                            Uri uri = null;
                          if (MmsApp.sHasPrivacyFeature) {
                              long privacy = 0;
                              String address = pdu.getFrom().getString();
                              if (!Mms.isEmailAddress(address)) {
                                  privacy = Utils.getFristPrivacyId(mContext, address);
                              }
                              uri = p.persist(pdu, Inbox.CONTENT_URI, privacy);
                          } else {
                              uri = p.persist(pdu, Inbox.CONTENT_URI);
                          }
                          // Aurora xuyong 2014-10-23 modified for privacy feature end
                            // add for gemini
                            if (MmsApp.mGnMultiSimMessage) {
                                // update pdu
                                // Aurora xuyong 2014-07-02 modified for reject feature start
                                ContentValues values = new ContentValues();
                                // Aurora xuyong 2014-07-02 modified for reject feature end
                                SIMInfo si = SIMInfo.getSIMInfoBySlot(mContext, intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1));
                                if (null == si) {
                                    Log.e(MmsApp.TXN_TAG, "PushReceiver:SIMInfo is null for slot " + intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1));
                                    break;
                                }
                                values.put(Mms.SIM_ID, si.mSimId);
                                values.put(WapPush.SERVICE_ADDR, intent.getStringExtra(WapPush.SERVICE_ADDR));
                                // Aurora xuyong 2014-07-02 added for reject feature start
                                if (MmsApp.sHasRejectFeature) {
                                    // Aurora xuyong 2014-07-07 modified for reject feature start
                                    // Aurora xuyong 2014-11-08 modified for reject new feature start
                                    if (Utils.needRejectBlackMms(pdu, mContext)) {
                                    // Aurora xuyong 2014-11-08 modified for reject new feature ebd
                                    // Aurora xuyong 2014-07-07 modified for reject feature end
                                            values.put("reject", 1);
                                        // Aurora xuyong 2014-09-19 added for aurora reject feature start
                                         values.put("newComing", 1);
                                        // Aurora xuyong 2014-09-19 added for aurora reject feature end
                                        // Aurora xuyong 2014-08-23 added for bug #7909 start
                                            MessagingNotification.setIsRejectMsg(true);
                                        // Aurora xuyong 2014-08-23 added for bug #7909 end
                                    }
                                }
                                // Aurora xuyong 2014-07-02 added for reject feature end
                                SqliteWrapper.update(mContext, cr, uri, values, null, null);
                                Log.d(MmsApp.TXN_TAG, "save notification slotId=" + intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, 0) 
                                        + "\tsimId=" + si.mSimId 
                                        + "\tsc=" + intent.getStringExtra(WapPush.SERVICE_ADDR)
                                        + "\taddr=" + intent.getStringExtra(WapPush.ADDR));

                                // update pending messages
                                long msgId = 0;
                                Cursor cursor = SqliteWrapper.query(mContext, mContext.getContentResolver(),
                                                                uri, new String[] {Mms._ID}, null, null, null);
                                if (cursor != null && cursor.getCount() == 1 && cursor.moveToFirst()) {
                                    try{
                                        msgId = cursor.getLong(0);
                                        Log.d(MmsApp.TXN_TAG, "msg id = " + msgId);
                                    }finally{
                                        cursor.close();
                                    }
                                }
                                
                                Uri.Builder uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
                                uriBuilder.appendQueryParameter("protocol", "mms");
                                uriBuilder.appendQueryParameter("message", String.valueOf(msgId));
                                Cursor pendingCs = SqliteWrapper.query(mContext, mContext.getContentResolver(),
                                        uriBuilder.build(), null, null, null, null);
                                if (pendingCs != null) {
                                    try {
                                        if ((pendingCs.getCount() == 1) && pendingCs.moveToFirst()) {
                                            ContentValues valuesforPending = new ContentValues();
                                            valuesforPending.put(PendingMessages.SIM_ID, si.mSimId);
                                            int columnIndex = pendingCs.getColumnIndexOrThrow(PendingMessages._ID);
                                            long id = pendingCs.getLong(columnIndex);
                                            SqliteWrapper.update(mContext, mContext.getContentResolver(),
                                                            PendingMessages.CONTENT_URI,
                                                            valuesforPending, PendingMessages._ID + "=" + id, null);
                                        }else{
                                            Log.w(MmsApp.TXN_TAG, "can not find message to set pending sim id, msgId="+msgId);
                                        }
                                    }finally {
                                        pendingCs.close();
                                    }
                                }
                            } else {
                                // Aurora xuyong 2014-07-02 modified for reject feature start
                                ContentValues value = new ContentValues();
                                // Aurora xuyong 2014-07-02 modified for reject feature end
                                value.put(WapPush.SERVICE_ADDR, intent.getStringExtra(WapPush.SERVICE_ADDR));
                                // Aurora xuyong 2014-07-02 added for reject feature start
                                if (MmsApp.sHasRejectFeature) {
                                   // Aurora xuyong 2014-07-07 modified for reject feature start
                                    // Aurora xuyong 2014-11-08 modified for reject new feature start
                                    if (Utils.needRejectBlackMms(pdu, mContext)) {
                                    // Aurora xuyong 2014-11-08 modified for reject new feature end
                                   // Aurora xuyong 2014-07-07 modified for reject feature end
                                        value.put("reject", 1);
                                    // Aurora xuyong 2014-09-19 added for aurora reject feature start
                                        value.put("newComing", 1);
                                    // Aurora xuyong 2014-09-19 added for aurora reject feature end
                                    // Aurora xuyong 2014-08-23 added for bug #7909 start
                                        MessagingNotification.setIsRejectMsg(true);
                                    // Aurora xuyong 2014-08-23 added for bug #7909 end
                                    }
                                }
                                // Aurora xuyong 2014-07-02 added for reject feature end
                                SqliteWrapper.update(mContext, cr, uri, value, null, null);
                                Log.d(MmsApp.TXN_TAG, "save notification," 
                                        + "\tsc=" + intent.getStringExtra(WapPush.SERVICE_ADDR)
                                        + "\taddr=" + intent.getStringExtra(WapPush.ADDR));
                            }
                        
                            // Start service to finish the notification transaction.
                            Intent svc = new Intent(mContext, TransactionService.class);
                            svc.putExtra(TransactionBundle.URI, uri.toString());
                            svc.putExtra(TransactionBundle.TRANSACTION_TYPE,
                                    Transaction.NOTIFICATION_TRANSACTION);
                            if (MmsApp.mGnMultiSimMessage) {
                                SIMInfo si = SIMInfo.getSIMInfoBySlot(mContext, intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1));
                                if (null == si) {
                                    Log.e(MmsApp.TXN_TAG, "PushReceiver: SIMInfo is null for slot " + intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1));
                                    break;
                                }
                                int simId = (int)si.mSimId;
                                svc.putExtra(GnPhone.GEMINI_SIM_ID_KEY, simId);
                                //svc.putExtra(Phone.GEMINI_SIM_ID_KEY, intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, 0));
                            }
                            mContext.startService(svc);
                        } else if (LOCAL_LOGV) {
                            Log.v(TAG, "Skip downloading duplicate message: "
                                    + new String(nInd.getContentLocation()));
                        }
                        break;
                    }
                    default:
                        Log.e(TAG, "Received unrecognized PDU.");
                }
            } catch (MmsException e) {
                Log.e(TAG, "Failed to save the data from PUSH: type=" + type, e);
            } catch (RuntimeException e) {
                Log.e(TAG, "Unexpected RuntimeException.", e);
            }  finally {
                raisePriority(mContext, false);
                Log.d(MmsApp.TXN_TAG, "Normal priority");
            }

            if (LOCAL_LOGV) {
                Log.v(TAG, "PUSH Intent processed.");
            }

            return null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Aurora xuyong 2014-05-30 modified for bug #5101 start
        if ((intent.getAction().equals(WAP_PUSH_RECEIVED_ACTION) 
                || intent.getAction().equals("android.provider.Telephony.WAP_PUSH_DELIVER"))
        // Aurora xuyong 2014-05-30 modified for bug #5101 end
                && ContentType.MMS_MESSAGE.equals(intent.getType())) {
            // Aurora xuyong 2014-11-05 modified for privacy feature start
            if (Utils.hasKitKat() && intent.getAction().equals(WAP_PUSH_RECEIVED_ACTION)) {
                return;
            }
            // Aurora xuyong 2014-11-05 modified for privacy feature end
          // Aurora xuyong 2014-09-09 added for 4.4 feature start
            if (Utils.hasKitKat() && !Utils.isDefaultSmsApk(context) && intent.getAction().equals(WAP_PUSH_RECEIVED_ACTION)) {
                return;
            }
          // Aurora xuyong 2014-09-09 added for 4.4 feature end
          // Aurora xuyong 2014-07-02 added for reject feature start
            if (MmsApp.sHasRejectFeature) {
                abortBroadcast();
            }
          // Aurora xuyong 2014-07-02 added for reject feature end
            if (LOCAL_LOGV) {
                Log.v(TAG, "Received PUSH Intent: " + intent);
            }

            // raise priority
            Log.d(MmsApp.TXN_TAG, "raise priority");
            raisePriority(context, true);

            // Hold a wake lock for 5 seconds, enough to give any
            // services we start time to take their own wake locks.
            PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            if (wl !=null && wl.isHeld()) {
                wl.release();
            }
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                            "MMS PushReceiver");
            wl.acquire(5000);
            // Aurora xuyong 2016-01-26 added for bug #18254 start
            Utils.updateWidget(context);
            // Aurora xuyong 2016-01-26 added for bug #18254 end
            new ReceivePushTask(context).execute(intent);
        }
    }

    private void raisePriority(Context context, boolean raise){
        final Intent it = new Intent();
        it.setAction("android.intent.action.BOOST_DOWNLOADING");
        it.putExtra("package_name", "com.android.mms");
        it.putExtra("enabled", raise);
        context.sendBroadcast(it);
    }

    private static long findThreadId(Context context, GenericPdu pdu, int type) {
        String messageId;

        if (type == MESSAGE_TYPE_DELIVERY_IND) {
            messageId = new String(((DeliveryInd) pdu).getMessageId());
        } else {
            messageId = new String(((ReadOrigInd) pdu).getMessageId());
        }

        StringBuilder sb = new StringBuilder('(');
        sb.append(Mms.MESSAGE_ID);
        sb.append('=');
        sb.append(DatabaseUtils.sqlEscapeString(messageId));
        sb.append(" AND ");
        sb.append(Mms.MESSAGE_TYPE);
        sb.append('=');
        sb.append(PduHeaders.MESSAGE_TYPE_SEND_REQ);
        // TODO ContentResolver.query() appends closing ')' to the selection argument
        // sb.append(')');

        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
                            Mms.CONTENT_URI, new String[] { Mms.THREAD_ID },
                            sb.toString(), null, null);
        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    return cursor.getLong(0);
                }
            } finally {
                cursor.close();
            }
        }

        return -1;
    }

    private static boolean isDuplicateNotification(
            Context context, NotificationInd nInd) {
        byte[] rawLocation = nInd.getContentLocation();
        if (rawLocation != null) {
            String location = new String(rawLocation);
            // Aurora xuyong 2014-08-06 modified for bug #6895 start
            String selection = Mms.CONTENT_LOCATION + " = ?" + "AND (reject = 1 OR reject = 0)";
            // Aurora xuyong 2014-08-06 modified for bug #6895 end
            String[] selectionArgs = new String[] { location };
            Cursor cursor = SqliteWrapper.query(
                    context, context.getContentResolver(),
                    Mms.CONTENT_URI, new String[] { Mms._ID },
                    selection, selectionArgs, null);
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        Log.d(MmsApp.TXN_TAG, "duplicate, location=" + location + ", id=" + cursor.getLong(0));
                        // We already received the same notification before.
                        return true;
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return false;
    }
    
    //GIONEE:wangfei 2012-09-03 add for CR00686851 begin
    protected static void gn_processNotificationSound(Context context, Notification notification) {
        int state = MmsApp.getApplication().getTelephonyManager().getCallState();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        
        //Gionee <guoyx> <2013-04-24> modified for CR00799298 begin
        if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
            Log.i(TAG, "in phone call ring the Notification.DEFAULT_SOUND");
            notification.audioStreamType = AudioManager.STREAM_VOICE_CALL;
            notification.defaults |= Notification.DEFAULT_SOUND;
        } else if (FeatureOption.MTK_BRAZIL_CUSTOMIZATION_CLARO && state != TelephonyManager.CALL_STATE_IDLE) {
            /* in call or in ringing */
            
            /* ringtone on, and into music mode */
            notification.audioStreamType = AudioManager.STREAM_MUSIC;
        }
        //Gionee <guoyx> <2013-04-24> modified for CR00799298 end
        
        if (audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION) == AudioManager.VIBRATE_SETTING_ON) {
            /* vibrate on */
        // Aurora xuyong 2014-01-08 modified for notification's vibrate start
        //notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.vibrate = new long[]{0, 400, 100, 200, 100, 200};
        // Aurora xuyong 2014-01-08 modified for notification's vibrate end
        }
        
    }
    //GIONEE:wangfei 2012-09-03 add for CR00686851 end

}
