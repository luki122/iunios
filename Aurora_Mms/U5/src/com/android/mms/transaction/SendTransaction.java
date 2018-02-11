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

import com.android.mms.util.RateController;
import com.android.mms.util.SendingProgressTokenManager;
import com.android.mms.LogTag;
import com.android.mms.ui.MessageUtils;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
import com.aurora.android.mms.pdu.PduComposer;
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.android.mms.pdu.PduParser;
import com.aurora.android.mms.pdu.PduPersister;
import com.aurora.android.mms.pdu.SendConf;
import com.aurora.android.mms.pdu.SendReq;
import com.aurora.android.mms.pdu.EncodedStringValue;
//Aurora xuyong 2013-11-15 modified for google adapt end
import android.database.sqlite.SqliteWrapper;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import gionee.provider.GnTelephony.Mms;
import android.provider.Telephony.Mms.Sent;
import android.util.Log;
import java.util.Arrays;

// add for gemini
import com.aurora.featureoption.FeatureOption;
// Aurora xuyong 2015-08-19 added for bug #15408 start
import com.aurora.mms.transaction.AuroraMmsNetworkManager;
// Aurora xuyong 2015-08-19 added for bug #15408 end
import com.android.mms.MmsApp;


/**
 * The SendTransaction is responsible for sending multimedia messages
 * (M-Send.req) to the MMSC server.  It:
 *
 * <ul>
 * <li>Loads the multimedia message from storage (Outbox).
 * <li>Packs M-Send.req and sends it.
 * <li>Retrieves confirmation data from the server  (M-Send.conf).
 * <li>Parses confirmation message and handles it.
 * <li>Moves sent multimedia message from Outbox to Sent.
 * <li>Notifies the TransactionService about successful completion.
 * </ul>
 */
public class SendTransaction extends Transaction implements Runnable {
    private static final String TAG = "SendTransaction";

    private Thread mThread;
    private final Uri mSendReqURI;
    public static boolean  sMMSSent = true;
    public SendTransaction(Context context,
            int transId, TransactionSettings connectionSettings, String uri) {
        super(context, transId, connectionSettings);
        mSendReqURI = Uri.parse(uri);
        mId = uri;

        // Attach the transaction to the instance of RetryScheduler.
        attach(RetryScheduler.getInstance(context));
    }

    public SendTransaction(Context context,
            int transId, int simId, TransactionSettings connectionSettings, String uri) {
        super(context, transId, connectionSettings);
        mSendReqURI = Uri.parse(uri);
        mId = uri;
        mSimId = simId;
        //Gionee 20130401 guoyx added for CR00790970 begin
        Log.d(MmsApp.TXN_TAG, "mSendReqURI:" + mSendReqURI.toString() 
                + " mId:" + mId + " mSimId:" + mSimId);
        //Gionee 20130401 guoyx added for CR00790970 end

        // Attach the transaction to the instance of RetryScheduler.
        attach(RetryScheduler.getInstance(context));
    }

    /*
     * (non-Javadoc)
     * @see com.android.mms.Transaction#process()
     */
    @Override
    public void process() {
        mThread = new Thread(this);
        mThread.start();
    }

    public void run() {
        Log.d(MmsApp.TXN_TAG, "SendTransaction: run");
        // Aurora xuyong 2015-08-19 added for bug #15408 start
        AuroraMmsNetworkManager networkManager = null;
        // Aurora xuyong 2015-08-19 added for bug #15408 end
        try {
            // Aurora xuyong 2015-08-19 added for bug #15408 start
        	networkManager = getNetworkManager(mContext, mSimId);
        	networkManager.acquireNetwork();
            // Aurora xuyong 2015-08-19 added for bug #15408 end
            RateController rateCtlr = RateController.getInstance();
            if (rateCtlr.isLimitSurpassed() && !rateCtlr.isAllowedByUser()) {
                Log.e(TAG, "Sending rate limit surpassed.");
                return;
            }

            // Load M-Send.req from outbox
            PduPersister persister = PduPersister.getPduPersister(mContext);
            SendReq sendReq = (SendReq) persister.load(mSendReqURI);

            // Update the 'date' field of the PDU right before sending it.
            long date = System.currentTimeMillis() / 1000L;
            sendReq.setDate(date);

            // Persist the new date value into database.
            ContentValues values = new ContentValues(1);
            values.put(Mms.DATE, date);
            SqliteWrapper.update(mContext, mContext.getContentResolver(),
                                 mSendReqURI, values, null, null);

            // fix bug 2100169: insert the 'from' address per spec
            sendReq.setFrom(new EncodedStringValue(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR));

            // Pack M-Send.req, send it, retrieve confirmation data, and parse it
            long tokenKey = ContentUris.parseId(mSendReqURI);
            byte[] response = sendPdu(SendingProgressTokenManager.get(tokenKey),
                                      new PduComposer(mContext, sendReq).make());
            SendingProgressTokenManager.remove(tokenKey);

            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                String respStr = new String(response);
                Log.d(TAG, "[SendTransaction] run: send mms msg (" + mId + "), resp=" + respStr);
            }

            SendConf conf = (SendConf) new PduParser(response).parse();
            if (conf == null) {
                Log.e(MmsApp.TXN_TAG, "No M-Send.conf received.");
            }

            // Check whether the responding Transaction-ID is consistent
            // with the sent one.
            byte[] reqId = sendReq.getTransactionId();
            byte[] confId = conf.getTransactionId();
            if (!Arrays.equals(reqId, confId)) {
                Log.w(MmsApp.TXN_TAG, "Inconsistent Transaction-ID: req="
                        + new String(reqId) + ", conf=" + new String(confId));
                return;
            }

            // From now on, we won't save the whole M-Send.conf into
            // our database. Instead, we just save some interesting fields
            // into the related M-Send.req.
            values = new ContentValues(2);
            int respStatus = conf.getResponseStatus();
            values.put(Mms.RESPONSE_STATUS, respStatus);

            // add for gemini
            if (MmsApp.mGnMultiSimMessage) {//guoyx 20130116
                values.put(Mms.SIM_ID, mSimId);
            }

            if (respStatus != PduHeaders.RESPONSE_STATUS_OK) {
                SqliteWrapper.update(mContext, mContext.getContentResolver(),
                                     mSendReqURI, values, null, null);
                Log.w(MmsApp.TXN_TAG, "Server returned an error code: " + respStatus);
                return;
            }

            String messageId = PduPersister.toIsoString(conf.getMessageId());
            values.put(Mms.MESSAGE_ID, messageId);
            SqliteWrapper.update(mContext, mContext.getContentResolver(),
                                 mSendReqURI, values, null, null);

            // Move M-Send.req from Outbox into Sent.
            Uri uri = persister.move(mSendReqURI, Sent.CONTENT_URI);

            mTransactionState.setState(TransactionState.SUCCESS);
            mTransactionState.setContentUri(uri);
        } catch (Throwable t) {
            Log.e(TAG, Log.getStackTraceString(t));
        } finally {
            if (mTransactionState.getState() != TransactionState.SUCCESS) {
                mTransactionState.setState(TransactionState.FAILED);
                mTransactionState.setContentUri(mSendReqURI);
                Log.e(MmsApp.TXN_TAG, "Delivery failed.");
            }
            sMMSSent = true;
            notifyObservers();
            // Aurora xuyong 2015-08-19 added for bug #15408 start
            networkManager.releaseNetwork();
            // Aurora xuyong 2015-08-19 added for bug #15408 end
        }
    }

    @Override
    public int getType() {
        return SEND_TRANSACTION;
    }

    public Uri getSendReqUri() {
        return mSendReqURI;
    }
}
