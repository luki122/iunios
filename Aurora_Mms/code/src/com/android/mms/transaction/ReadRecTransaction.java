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
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
import com.aurora.android.mms.pdu.PduComposer;
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.android.mms.pdu.PduPersister;
import com.aurora.android.mms.pdu.ReadRecInd;
import com.aurora.android.mms.pdu.EncodedStringValue;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.android.mms.ui.MessageUtils;

import android.content.Context;
// add this for read report
import android.content.ContentValues;
import android.net.Uri;
import android.provider.Telephony.Mms.Sent;
// add this for read report
import android.provider.Telephony.Mms;
import android.util.Log;
// add these two for read report
import android.database.sqlite.SqliteWrapper;
import android.database.Cursor;

import java.io.IOException;
import com.android.mms.MmsApp;

import com.aurora.featureoption.FeatureOption;


/**
 * The ReadRecTransaction is responsible for sending read report
 * notifications (M-read-rec.ind) to clients that have requested them.
 * It:
 *
 * <ul>
 * <li>Loads the read report indication from storage (Outbox).
 * <li>Packs M-read-rec.ind and sends it.
 * <li>Notifies the TransactionService about succesful completion.
 * </ul>
 */
public class ReadRecTransaction extends Transaction implements Runnable {
    private static final String TAG = "ReadRecTransaction";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;
    // add this member and this class implements Runnable.
    private Thread mThread;

    private final Uri mReadReportURI;

    public ReadRecTransaction(Context context,
            int transId,
            TransactionSettings connectionSettings,
            String uri) {
        super(context, transId, connectionSettings);
        mReadReportURI = Uri.parse(uri);
        mId = uri;

        // Attach the transaction to the instance of RetryScheduler.
        // currently read report only try once, so don't need this.        
        //attach(RetryScheduler.getInstance(context));
    }

    // add for gemini
    public ReadRecTransaction(Context context,
            int transId, int simId,
            TransactionSettings connectionSettings,
            String uri) {
        super(context, transId, connectionSettings);
        mReadReportURI = Uri.parse(uri);
        mId = uri;
        mSimId = simId;

        // Attach the transaction to the instance of RetryScheduler.
        // currently read report only try once, so don't need this.        
        //attach(RetryScheduler.getInstance(context));
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
        Log.d(MmsApp.TXN_TAG, "ReadRecTransaction: process()");
        int readReportState = 0;
        try {
            String messageId = null;
            long msgId = 0;
            EncodedStringValue[] sender = new EncodedStringValue[1];
            Cursor cursor = null;
            try {
                cursor = SqliteWrapper.query(mContext, mContext.getContentResolver(),
                                            mReadReportURI,
                                            new String[] {Mms.MESSAGE_ID, Mms.READ_REPORT, Mms._ID},
                                            // Aurora xuyong 2014-11-05 modified for privacy feature start
                                            "is_privacy >= 0", null, null);
                                            // Aurora xuyong 2014-11-05 modified for privacy feature end
                cursor.moveToFirst();
                messageId = cursor.getString(0);
                readReportState = cursor.getInt(1);
                msgId = cursor.getLong(2);
                //if curosr==null, this means the mms is deleted during processing.
                //exception will happened. catched by out catch clause.
                //so do not catch exception here.
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            Log.d(MmsApp.TXN_TAG,"messageid:"+messageId+",and readreport flag:"+readReportState);
            if (readReportState != 130) {
                Log.d("MMslog","processed. ignore this:"+mId);
                return;//read report only try to send once.
            }
            cursor = null;
            try {
                cursor = SqliteWrapper.query(mContext, mContext.getContentResolver(),
                                        Uri.parse("content://mms/" + msgId + "/addr"),
                                        new String[] { Mms.Addr.ADDRESS, Mms.Addr.CHARSET},
                                        // Aurora xuyong 2014-11-05 modified for privacy feature start
                                        Mms.Addr.TYPE+" = "+PduHeaders.FROM + " AND is_privacy >= 0", null, null);
                                        // Aurora xuyong 2014-11-05 modified for privacy feature end
                cursor.moveToFirst();
                String address = cursor.getString(0);
                int charSet= cursor.getInt(1);
                Log.d(MmsApp.TXN_TAG,"find address:"+address+",charset:"+charSet);
                sender[0] = new EncodedStringValue(charSet, PduPersister.getBytes(address));
                //if cursor == null exception will catched by out catch clause.
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            ReadRecInd readRecInd = new ReadRecInd(                    
                    new EncodedStringValue(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR.getBytes()),
                    messageId.getBytes(),
                    PduHeaders.CURRENT_MMS_VERSION,
                    PduHeaders.READ_STATUS_READ,//always set read.
                    sender);
    
            readRecInd.setDate(System.currentTimeMillis() / 1000);
            byte[] postingData = new PduComposer(mContext, readRecInd).make();
            Log.d(MmsApp.TXN_TAG,"before send read report pdu.");
            sendPdu(postingData);            
            Log.d(MmsApp.TXN_TAG,"after send read report pdu.ok");
            mTransactionState.setState(TransactionState.SUCCESS);
        } catch (Throwable t) {
            Log.e(MmsApp.TXN_TAG, Log.getStackTraceString(t));
        } finally {
            if (mTransactionState.getState() != TransactionState.SUCCESS) {
                mTransactionState.setState(TransactionState.FAILED);
                Log.e(MmsApp.TXN_TAG, "send read report failed.uri:"+mId);
            }
            mTransactionState.setContentUri(mReadReportURI);//useless.
            //whether success or fail, update database, this read report will not send any more.
            ContentValues values = new ContentValues(1);
            values.put(Mms.READ_REPORT, PduHeaders.READ_STATUS__DELETED_WITHOUT_BEING_READ);
            SqliteWrapper.update(mContext, mContext.getContentResolver(),
                                 mReadReportURI, values, null, null);            
            notifyObservers();
        }        
    }

    @Override
    public int getType() {
        return READREC_TRANSACTION;
    }

    // add for gemini
    public Uri getRrecTrxnUri() {
        return mReadReportURI;
    }
}
