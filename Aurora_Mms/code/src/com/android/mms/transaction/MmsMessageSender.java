/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

import com.android.mms.data.Conversation;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.MessagingPreferenceActivity;
import com.android.mms.util.SendingProgressTokenManager;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.InvalidHeaderValueException;
import com.aurora.android.mms.MmsException;
import com.aurora.android.mms.pdu.EncodedStringValue;
import com.aurora.android.mms.pdu.GenericPdu;
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.android.mms.pdu.PduPersister;
import com.aurora.android.mms.pdu.ReadRecInd;
import com.aurora.android.mms.pdu.SendReq;
//Aurora xuyong 2013-11-15 modified for google adapt end
import gionee.provider.GnTelephony.SIMInfo;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import aurora.preference.AuroraPreferenceManager;
import android.provider.Telephony.Mms;
import gionee.provider.GnTelephony;
import gionee.provider.GnTelephony.MmsSms;
import android.util.Log;
import com.gionee.internal.telephony.GnPhone;

// add for gemini
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.util.SqliteWrapper;
//Aurora xuyong 2013-11-15 modified for google adapt end
import android.content.ContentValues;
import com.aurora.featureoption.FeatureOption;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.database.Cursor;
import com.android.mms.MmsApp;
import com.android.mms.ui.SelectMmsSubscription;


public class MmsMessageSender implements MessageSender {
    private static final String TAG = "MmsMessageSender";

    private final Context mContext;
    private final Uri mMessageUri;
    private final long mMessageSize;
    // Aurora xuyong 2014-10-23 added for privacy feature end
    private static Conversation mConversation;
    // Aurora xuyong 2014-10-23 added for privacy feature end
    // Default preference values
    private static final boolean DEFAULT_DELIVERY_REPORT_MODE  = false;
    private static final boolean DEFAULT_READ_REPORT_MODE      = false;
    private static final long    DEFAULT_EXPIRY_TIME     = 7 * 24 * 60 * 60;
    private static final int     DEFAULT_PRIORITY        = PduHeaders.PRIORITY_NORMAL;
    private static final String  DEFAULT_MESSAGE_CLASS   = PduHeaders.MESSAGE_CLASS_PERSONAL_STR;
    private static final String  INFORMATIONAL_MESSAGE_CLASS = PduHeaders.MESSAGE_CLASS_INFORMATIONAL_STR;

    // lemei flag
    private boolean bLeMei = false;

    public void setLeMeiFlag(boolean lemei){
        bLeMei = lemei;
    }

    public MmsMessageSender(Context context, Uri location, long messageSize) {
        mContext = context;
        mMessageUri = location;
        mMessageSize = messageSize;

        if (mMessageUri == null) {
            throw new IllegalArgumentException("Null message URI.");
        }
        //gionee gaoj 2012-8-21 added for CR00678315 start
        if (MmsApp.mGnRegularlyMsgSend) {
            mIsRegularlyMms = false;
        }
        //gionee gaoj 2012-8-21 added for CR00678315 end
    }
    // Aurora xuyong 2014-10-23 modified for privacy feature start
    public MmsMessageSender(Context context, Uri location, long messageSize, Conversation conv) {
        mContext = context;
        mMessageUri = location;
        mMessageSize = messageSize;
        mConversation = conv;
        
        if (mMessageUri == null) {
            throw new IllegalArgumentException("Null message URI.");
        }
        //gionee gaoj 2012-8-21 added for CR00678315 start
        if (MmsApp.mGnRegularlyMsgSend) {
            mIsRegularlyMms = false;
        }
        //gionee gaoj 2012-8-21 added for CR00678315 end
    }
    // Aurora xuyong 2014-10-23 modified for privacy feature end
    //gionee gaoj 2012-8-21 added for CR00678315 start
    private boolean mIsRegularlyMms = false;
    private long mTime = -1;
    public MmsMessageSender(Context context, Uri location, long messageSize, long time) {
        mContext = context;
        mMessageUri = location;
        mMessageSize = messageSize;

        if (mMessageUri == null) {
            throw new IllegalArgumentException("Null message URI.");
        }
        mIsRegularlyMms = true;
        mTime = time;
    }
    //gionee gaoj 2012-8-21 added for CR00678315 end
    // Aurora xuyong 2014-10-23 added for privacy feature start
    public MmsMessageSender(Context context, Uri location, long messageSize, long time, Conversation conv) {
        mContext = context;
        mMessageUri = location;
        mMessageSize = messageSize;
        mConversation = conv;

        if (mMessageUri == null) {
            throw new IllegalArgumentException("Null message URI.");
        }
        mIsRegularlyMms = true;
        mTime = time;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end

    public boolean sendMessage(long token) throws MmsException {
        // Load the MMS from the message uri
        PduPersister p = PduPersister.getPduPersister(mContext);
        GenericPdu pdu = p.load(mMessageUri);

        if (pdu.getMessageType() != PduHeaders.MESSAGE_TYPE_SEND_REQ) {
            throw new MmsException("Invalid message: " + pdu.getMessageType());
        }

        SendReq sendReq = (SendReq) pdu;

        // Update headers.
        updatePreferencesHeaders(sendReq);

        // MessageClass.
        //sendReq.setMessageClass(DEFAULT_MESSAGE_CLASS.getBytes());
        if (bLeMei){
            sendReq.setMessageClass(INFORMATIONAL_MESSAGE_CLASS.getBytes());
            Log.d(MmsApp.TXN_TAG, "For Le Mei, set class INFORMATIONAL");
        } else {
            sendReq.setMessageClass(DEFAULT_MESSAGE_CLASS.getBytes());
        }

        // Update the 'date' field of the message before sending it.
        //Gionee <guoyx> <2013-05-17> modify for CR00813219 begin
        if (MmsApp.mGnRegularlyMsgSend && mIsRegularlyMms) {
            sendReq.setDate(mTime / 1000L);
        } else {
            sendReq.setDate(System.currentTimeMillis() / 1000L);
        }
        //Gionee <guoyx> <2013-05-17> modify for CR00813219 end
        
        sendReq.setMessageSize(mMessageSize);
        // Aurora xuyong 2014-10-23 modified for privacy feature start
        if (MmsApp.sHasPrivacyFeature) {
            p.updateHeaders(mMessageUri, sendReq, mConversation.getRecipients().get(0).getPrivacy());
        } else {
            p.updateHeaders(mMessageUri, sendReq);
        }
        // Aurora xuyong 2014-10-23 modified for privacy feature end
        // Move the message into MMS Outbox
        Uri sendUri = p.move(mMessageUri, Mms.Outbox.CONTENT_URI);
        
        //Gionee <guoyx> <2013-05-23> add for CR00813219 begin
        if (isGnRegularMsg()) {
            return true;
        }
        //Gionee <guoyx> <2013-05-23> add for CR00813219 end

        // Start MMS transaction service
        SendingProgressTokenManager.put(ContentUris.parseId(mMessageUri), token);
        Intent transactionIntent = new Intent(mContext, TransactionService.class);
        transactionIntent.putExtra(TransactionBundle.URI, sendUri.toString());
        transactionIntent.putExtra(TransactionBundle.TRANSACTION_TYPE, Transaction.SEND_TRANSACTION);
        mContext.startService(transactionIntent);

        return true;
    }

    // Update the headers which are stored in SharedPreferences.
    private void updatePreferencesHeaders(SendReq sendReq) throws MmsException {
        SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(mContext);

        // Expiry.
        sendReq.setExpiry(prefs.getLong(
                MessagingPreferenceActivity.EXPIRY_TIME, DEFAULT_EXPIRY_TIME));

        // Priority.
        if (bLeMei){
            sendReq.setPriority(PduHeaders.PRIORITY_HIGH);
            Log.d(MmsApp.TXN_TAG, "For Le Mei, set priority high");
        } else {
            String priority = prefs.getString(MessagingPreferenceActivity.PRIORITY, "Normal");
            if (priority.equals("High")) {
                sendReq.setPriority(PduHeaders.PRIORITY_HIGH);
            } else if (priority.equals("Low")) {
                sendReq.setPriority(PduHeaders.PRIORITY_LOW);
            } else {
                sendReq.setPriority(PduHeaders.PRIORITY_NORMAL);
            }
        }

        // Delivery report.
        boolean dr = prefs.getBoolean(MessagingPreferenceActivity.MMS_DELIVERY_REPORT_MODE,
                DEFAULT_DELIVERY_REPORT_MODE);
        sendReq.setDeliveryReport(dr?PduHeaders.VALUE_YES:PduHeaders.VALUE_NO);

        // Read report.
        boolean rr = prefs.getBoolean(MessagingPreferenceActivity.READ_REPORT_MODE,
                DEFAULT_READ_REPORT_MODE);
        sendReq.setReadReport(rr?PduHeaders.VALUE_YES:PduHeaders.VALUE_NO);
        Log.d(MmsApp.TXN_TAG, "MMS DR request=" + dr + "; MMS RR request=" + rr);
    }

    // Update the headers which are stored in SharedPreferences.
    private void updatePreferencesHeadersGemini(SendReq sendReq, int simId) throws MmsException {
        SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(mContext);

        // Expiry.
        sendReq.setExpiry(prefs.getLong(
                MessagingPreferenceActivity.EXPIRY_TIME, DEFAULT_EXPIRY_TIME));

        // Priority.
        String priority = prefs.getString(MessagingPreferenceActivity.PRIORITY, "Normal");
        if (priority.equals("High")) {
            sendReq.setPriority(PduHeaders.PRIORITY_HIGH);
        } else if (priority.equals("Low")) {
            sendReq.setPriority(PduHeaders.PRIORITY_LOW);
        } else {
            sendReq.setPriority(PduHeaders.PRIORITY_NORMAL);
        }

        // Delivery report.
        // Aurora xuyong 2014-06-09 modified for bug #5554 start
        boolean dr = prefs.getBoolean(MessagingPreferenceActivity.MMS_DELIVERY_REPORT_MODE,
        // Aurora xuyong 2014-06-09 modified for bug #5554 end
                DEFAULT_DELIVERY_REPORT_MODE);
        sendReq.setDeliveryReport(dr?PduHeaders.VALUE_YES:PduHeaders.VALUE_NO);

        // Read report.
        boolean rr = prefs.getBoolean(Integer.toString(simId)+ "_" + MessagingPreferenceActivity.READ_REPORT_MODE,
                DEFAULT_READ_REPORT_MODE);
        sendReq.setReadReport(rr?PduHeaders.VALUE_YES:PduHeaders.VALUE_NO);
        Log.d(MmsApp.TXN_TAG, "MMS DR request=" + dr + "; MMS RR request=" + rr);
    }

    public static void sendReadRec(Context context, String to, String messageId, int status) {
        EncodedStringValue[] sender = new EncodedStringValue[1];
        sender[0] = new EncodedStringValue(to);

        try {
            final ReadRecInd readRec = new ReadRecInd(
                    new EncodedStringValue(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR.getBytes()),
                    messageId.getBytes(),
                    PduHeaders.CURRENT_MMS_VERSION,
                    status,
                    sender);

            readRec.setDate(System.currentTimeMillis() / 1000);
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            if (MmsApp.sHasPrivacyFeature) {
                PduPersister.getPduPersister(context).persist(readRec, Mms.Outbox.CONTENT_URI, mConversation.getRecipients().get(0).getPrivacy());
            } else {
                PduPersister.getPduPersister(context).persist(readRec, Mms.Outbox.CONTENT_URI);
            }
            // Aurora xuyong 2014-10-23 modified for privacy feature end
            context.startService(new Intent(context, TransactionService.class));
        } catch (InvalidHeaderValueException e) {
            Log.e(TAG, "Invalide header value", e);
        } catch (MmsException e) {
            Log.e(TAG, "Persist message failed", e);
        }
    }

    // add for gemini
        public static void sendReadRecGemini(Context context, String to, String messageId, int status, int simId) {
            Log.d(MmsApp.TXN_TAG, "RR to:" + to + "\tMid:" + messageId + "\tstatus:" + status + "\tsimId:" + simId);
            EncodedStringValue[] sender = new EncodedStringValue[1];
            sender[0] = new EncodedStringValue(to);
    
            try {
                final ReadRecInd readRec = new ReadRecInd(
                        new EncodedStringValue(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR.getBytes()),
                        messageId.getBytes(),
                        PduHeaders.CURRENT_MMS_VERSION,
                        status,
                        sender);
    
                readRec.setDate(System.currentTimeMillis() / 1000);
                // Aurora xuyong 2014-10-23 modified for privacy feature start
                Uri uri = null;
                if (MmsApp.sHasPrivacyFeature) {
                    uri = PduPersister.getPduPersister(context).persist(readRec, Mms.Outbox.CONTENT_URI, mConversation.getRecipients().get(0).getPrivacy());
                } else {
                    uri = PduPersister.getPduPersister(context).persist(readRec, Mms.Outbox.CONTENT_URI);
                }
                // Aurora xuyong 2014-10-23 modified for privacy feature end
                // update sim id
                ContentValues simIdValues = new ContentValues(1);
                simIdValues.put(GnTelephony.Mms.SIM_ID, simId);
                SqliteWrapper.update(context, context.getContentResolver(),
                                uri, simIdValues, null, null);
    
                context.startService(new Intent(context, TransactionService.class));
            } catch (InvalidHeaderValueException e) {
                Log.e(TAG, "Invalide header value", e);
            } catch (MmsException e) {
                Log.e(TAG, "Persist message failed", e);
            }
        }

    // add for gemini
    public boolean sendMessageGemini(long token, int simId) throws MmsException {
        // Load the MMS from the message uri
        PduPersister p = PduPersister.getPduPersister(mContext);
        GenericPdu pdu = p.load(mMessageUri);

        if (pdu.getMessageType() != PduHeaders.MESSAGE_TYPE_SEND_REQ) {
            throw new MmsException("Invalid message: " + pdu.getMessageType());
        }

        SendReq sendReq = (SendReq) pdu;

        // Update headers.
        updatePreferencesHeadersGemini(sendReq, simId);

        // MessageClass.
        //sendReq.setMessageClass(DEFAULT_MESSAGE_CLASS.getBytes());
        if (bLeMei){
            sendReq.setMessageClass(INFORMATIONAL_MESSAGE_CLASS.getBytes());
            Log.d(MmsApp.TXN_TAG, "For Le Mei, set class INFORMATIONAL");
        } else {
            sendReq.setMessageClass(DEFAULT_MESSAGE_CLASS.getBytes());
        }

        // Update the 'date' field of the message before sending it.
        //Gionee <guoyx> <2013-05-23> modify for CR00813219 begin
        //gionee gaoj 2012-8-21 added for CR00678315 start
        if (isGnRegularMsg()) {
            sendReq.setDate(mTime / 1000L);
        } else {
        //gionee gaoj 2012-8-21 added for CR00678315 end
            sendReq.setDate(System.currentTimeMillis() / 1000L);
        //gionee gaoj 2012-8-21 added for CR00678315 start
        }
        //gionee gaoj 2012-8-21 added for CR00678315 end
        //Gionee <guoyx> <2013-05-23> modify for CR00813219 end
        
        sendReq.setMessageSize(mMessageSize);
        if (MmsApp.sHasPrivacyFeature) {
            p.updateHeaders(mMessageUri, sendReq, mConversation.getRecipients().get(0).getPrivacy());
        } else {
            p.updateHeaders(mMessageUri, sendReq);
        }

        // Move the message into MMS Outbox
        Uri sendUri = p.move(mMessageUri, Mms.Outbox.CONTENT_URI);
        Log.d(MmsApp.TXN_TAG, "sendMessageGemini(). sendUri=" + sendUri);

        // add for gemini
        if(MmsApp.mGnMultiSimMessage){
            // set sim id 
            ContentValues values = new ContentValues(1);
            values.put(GnTelephony.Mms.SIM_ID, simId);
            SqliteWrapper.update(mContext, mContext.getContentResolver(), sendUri, values, null, null);

            // set pending message sim id
            long msgId = ContentUris.parseId(sendUri);

            Uri.Builder uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
            uriBuilder.appendQueryParameter("protocol", "mms");
            uriBuilder.appendQueryParameter("message", String.valueOf(msgId));

            Cursor cr = SqliteWrapper.query(mContext, mContext.getContentResolver(),
                    uriBuilder.build(), null, null, null, null);
            if (cr != null) {
                try {
                    if ((cr.getCount() == 1) && cr.moveToFirst()) {
                        ContentValues valuesforPending = new ContentValues();
                        valuesforPending.put(MmsSms.PendingMessages.SIM_ID, simId);
                        int columnIndex = cr.getColumnIndexOrThrow(
                                        PendingMessages._ID);
                        long id = cr.getLong(columnIndex);
                        SqliteWrapper.update(mContext, mContext.getContentResolver(),
                                        PendingMessages.CONTENT_URI,
                                        valuesforPending, PendingMessages._ID + "=" + id, null);
                    }else{
                        Log.w(MmsApp.TXN_TAG, "can not find message to set pending sim id, msgId=" + msgId);
                    }
                }finally {
                    cr.close();
                }
            }
        }

        //Gionee <guoyx> <2013-05-23> modify for CR00813219 begin
        //gionee gaoj 2012-8-21 added for CR00678315 start
        if (isGnRegularMsg()) {
            return true;
        }
        //gionee gaoj 2012-8-21 added for CR00678315 end
        //Gionee <guoyx> <2013-05-23> modify for CR00813219 end
        // Start MMS transaction service
        SendingProgressTokenManager.put(ContentUris.parseId(mMessageUri), token);
        //Gionee guoyx 20130221 modified for CR00773050 begin
        if (MmsApp.mQcMultiSimEnabled) {
                //Aurora hujunming 2014-5-15 modified for multiSim start
            int slot_id=SIMInfo.getSlotById(MmsApp.getApplication().getApplicationContext(), simId);
            Intent intent = new Intent(mContext, TransactionService.class);
            intent.putExtra(GnTelephony.Mms.SIM_ID, slot_id);
                //Aurora hujunming 2014-5-15 modified for multiSim end
            intent.putExtra(GnPhone.GEMINI_SIM_ID_KEY, simId);
            Log.d(MmsApp.TXN_TAG, "sendMessageGemini sub:" + slot_id + " sim:" + simId);
            intent.putExtra(TransactionBundle.URI, sendUri.toString());
            intent.putExtra(TransactionBundle.TRANSACTION_TYPE, Transaction.SEND_TRANSACTION);
            // Aurora xuyong 2014-06-10 modified for aurora's feature start
            //Intent silentIntent = new Intent(mContext, SelectMmsSubscription.class);
            //silentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //silentIntent.putExtras(intent); //copy all extras
            mContext.startService(intent);
            // Aurora xuyong 2014-06-10 modified for aurora's feature end
        } else {
            Intent transactionIntent = new Intent(mContext, TransactionService.class);
            transactionIntent.putExtra(TransactionBundle.URI, sendUri.toString());
            transactionIntent.putExtra(TransactionBundle.TRANSACTION_TYPE, Transaction.SEND_TRANSACTION);
            transactionIntent.putExtra(GnPhone.GEMINI_SIM_ID_KEY, simId);
            mContext.startService(transactionIntent);    
        }
        //Gionee guoyx 20130221 modified for CR00773050 end

        return true;
    }

    //Gionee <guoyx> <2013-05-23> add for CR00813219 begin
    public boolean isGnRegularMsg() {
        return MmsApp.mGnRegularlyMsgSend 
                && mIsRegularlyMms && mTime != -1;
    }
    //Gionee <guoyx> <2013-05-23> add for CR00813219 end
}
