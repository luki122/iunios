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
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.MessagingPreferenceActivity;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.Recycler;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.InvalidHeaderValueException;
import com.aurora.android.mms.MmsException;
import com.aurora.android.mms.pdu.AcknowledgeInd;
import com.aurora.android.mms.pdu.NotificationInd;
import com.aurora.android.mms.pdu.NotifyRespInd;
import com.aurora.android.mms.pdu.PduComposer;
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.android.mms.pdu.PduParser;
import com.aurora.android.mms.pdu.PduPersister;
import com.aurora.android.mms.pdu.RetrieveConf;
import com.aurora.android.mms.pdu.EncodedStringValue;
//Aurora xuyong 2013-11-15 modified for google adapt end
import android.database.sqlite.SqliteWrapper;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import aurora.preference.AuroraPreferenceManager;
import gionee.provider.GnTelephony.Mms;
import gionee.provider.GnTelephony.Mms.Inbox;
import android.util.Log;

import java.io.IOException;
// add for gemini
import com.aurora.featureoption.FeatureOption;
// Aurora xuyong 2014-07-07 added for reject feature start
import com.aurora.mms.util.Utils;
// Aurora xuyong 2014-07-07 added for reject feature end
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.SystemProperties;
import com.android.mms.MmsApp;
//Aurora xuyong 2013-11-15 modified for google adapt start
import static com.aurora.android.mms.pdu.PduHeaders.STATUS_EXPIRED;
//Aurora xuyong 2013-11-15 modified for google adapt end


//gionee gaoj 2013-3-11 added for CR00782858 start
import com.android.mms.widget.MmsWidgetProvider;
//gionee gaoj 2013-3-11 added for CR00782858 end
/**
 * The RetrieveTransaction is responsible for retrieving multimedia
 * messages (M-Retrieve.conf) from the MMSC server.  It:
 *
 * <ul>
 * <li>Sends a GET request to the MMSC server.
 * <li>Retrieves the binary M-Retrieve.conf data and parses it.
 * <li>Persists the retrieve multimedia message.
 * <li>Determines whether an acknowledgement is required.
 * <li>Creates appropriate M-Acknowledge.ind and sends it to MMSC server.
 * <li>Notifies the TransactionService about succesful completion.
 * </ul>
 */
public class RetrieveTransaction extends Transaction implements Runnable {
    private static final String TAG = "RetrieveTransaction";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private final Uri mUri;
    private final String mContentLocation;
    private boolean mLocked;
    private boolean mExpiry = false;

    static final String[] PROJECTION = new String[] {
        Mms.CONTENT_LOCATION,
        Mms.LOCKED
    };

    // The indexes of the columns which must be consistent with above PROJECTION.
    static final int COLUMN_CONTENT_LOCATION      = 0;
    static final int COLUMN_LOCKED                = 1;

    public RetrieveTransaction(Context context, int serviceId,
            TransactionSettings connectionSettings, String uri)
            throws MmsException {
        super(context, serviceId, connectionSettings);

        if (uri.startsWith("content://")) {
            mUri = Uri.parse(uri); // The Uri of the M-Notification.ind
            mId = mContentLocation = getContentLocation(context, mUri);
            if (LOCAL_LOGV) {
                Log.v(TAG, "X-Mms-Content-Location: " + mContentLocation);
            }
        } else {
            throw new IllegalArgumentException(
                    "Initializing from X-Mms-Content-Location is abandoned!");
        }

        // Attach the transaction to the instance of RetryScheduler.
        attach(RetryScheduler.getInstance(context));
    }

    // add for gemini
    public RetrieveTransaction(Context context, int serviceId, int simId,
            TransactionSettings connectionSettings, String uri)
            throws MmsException {
        super(context, serviceId, connectionSettings);
        mSimId = simId;

        if (uri.startsWith("content://")) {
            mUri = Uri.parse(uri); // The Uri of the M-Notification.ind
            mId = mContentLocation = getContentLocation(context, mUri);
            if (LOCAL_LOGV) {
                Log.v(TAG, "X-Mms-Content-Location: " + mContentLocation);
            }
        } else {
            throw new IllegalArgumentException(
                    "Initializing from X-Mms-Content-Location is abandoned!");
        }

        // Attach the transaction to the instance of RetryScheduler.
        attach(RetryScheduler.getInstance(context));
    }

    private String getContentLocation(Context context, Uri uri)  throws MmsException {
        Log.v(MmsApp.TXN_TAG, "RetrieveTransaction: getContentLocation()");
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
                            uri, PROJECTION, null, null, null);
        mLocked = false;

        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    // Get the locked flag from the M-Notification.ind so it can be transferred
                    // to the real message after the download.
                    mLocked = cursor.getInt(COLUMN_LOCKED) == 1;
                    return cursor.getString(COLUMN_CONTENT_LOCATION);
                }
            } finally {
                cursor.close();
            }
        }

        throw new MmsException("Cannot get X-Mms-Content-Location from: " + uri);
    }

    /*
     * (non-Javadoc)
     * @see com.android.mms.transaction.Transaction#process()
     */
    @Override
    public void process() {
        new Thread(this).start();
    }

    public void run() {
        try {
            // Check expiery , this operation must be done before markState function,
            NotificationInd nInd = (NotificationInd) PduPersister.getPduPersister(mContext).load(mUri);
            if (nInd.getExpiry() < System.currentTimeMillis()/1000L) {
                mExpiry = true;
                Log.d(MmsApp.TXN_TAG, "The message is expired!");
                try {
                    sendNotifyRespInd(STATUS_EXPIRED);
                } catch (Throwable t) { 
                    // we should run following func to delete expired notification, no matter what happen. so, catch throwable
                    Log.e(MmsApp.TXN_TAG, Log.getStackTraceString(t));
                }
            }

            
            // Change the downloading state of the M-Notification.ind.
            DownloadManager.getInstance().markState(
                    mUri, DownloadManager.STATE_DOWNLOADING);

            // if this notification expiry, we should not download message from network
            if (mExpiry) {
                mTransactionState.setState(TransactionState.SUCCESS);
                mTransactionState.setContentUri(mUri);
                return;
            }

            // Send GET request to MMSC and retrieve the response data.
            byte[] resp = getPdu(mContentLocation);

            // Parse M-Retrieve.conf
            RetrieveConf retrieveConf = (RetrieveConf) new PduParser(resp).parse();
            if (null == retrieveConf) {
                Log.e(MmsApp.TXN_TAG, "RetrieveTransaction: run(): Invalid M-Retrieve.conf PDU!!!");
                throw new MmsException("Invalid M-Retrieve.conf PDU.");
            }

            Uri msgUri = null;
            if (isDuplicateMessage(mContext, retrieveConf)) {
                Log.w(MmsApp.TXN_TAG, "RetrieveTransaction: run, DuplicateMessage");
                // Mark this transaction as failed to prevent duplicate
                // notification to user.
                mTransactionState.setState(TransactionState.FAILED);
                mTransactionState.setContentUri(mUri);
            } else {
                Log.d(MmsApp.TXN_TAG, "RetrieveTransaction: run, Store M-Retrieve.conf into Inbox");
                // Store M-Retrieve.conf into Inbox
                PduPersister persister = PduPersister.getPduPersister(mContext);
             // Aurora xuyong 2014-10-23 modified for privacy feature start
                if (MmsApp.sHasPrivacyFeature) {
                    long privacy = 0;
                    String address = retrieveConf.getFrom().getString();
                    if (!Mms.isEmailAddress(address)) {
                        privacy = Utils.getFristPrivacyId(mContext, address);
                    }
                    msgUri = persister.persist(retrieveConf, Inbox.CONTENT_URI, privacy);
                } else {
                    msgUri = persister.persist(retrieveConf, Inbox.CONTENT_URI);
                }
             // Aurora xuyong 2014-10-23 modified for privacy feature end
                // Aurora xuyong 2014-07-07 added for reject feature start
                // add for reject feature 
                if (MmsApp.sHasRejectFeature) {
                    ContentResolver reCr = mContext.getContentResolver();
                    ContentValues rejectValue = new ContentValues();
                    // Aurora xuyong 2014-11-08 modified for reject new feature start
                    if (Utils.needRejectBlackMms(retrieveConf, mContext) || (Utils.needRejectRubMms(retrieveConf, mContext) && !mAvoidReject)) {
                    // Aurora xuyong 2014-11-08 modified for reject new feature end
                        rejectValue.put("reject", 1);
                       // Aurora xuyong 2014-09-19 added for aurora reject feature start
                        rejectValue.put("newComing", 1);
                       // Aurora xuyong 2014-09-19 added for aurora reject feature end
                       // Aurora xuyong 2014-08-23 added for bug #7909 start
                        MessagingNotification.setIsRejectMsg(true);
                       // Aurora xuyong 2014-08-23 added for bug #7909 end
                       // Aurora xuyong 2014-08-06 added for bug #6895 start
                        SqliteWrapper.update(mContext, reCr, msgUri, rejectValue, null, null);
                       // Aurora xuyong 2014-08-06 added for bug #6895 end
                    }
                    // Aurora xuyong 2014-08-06 deleted for bug #6895 start
                    //SqliteWrapper.update(mContext, reCr, msgUri, rejectValue, null, null);
                    // Aurora xuyong 2014-08-06 deleted for bug #6895 end
                }
                // Aurora xuyong 2014-07-07 added for reject feature end
                // add for gemini
                if (MmsApp.mGnMultiSimMessage) {//guoyx 20130116
                    ContentResolver cr = mContext.getContentResolver();
                    ContentValues values = new ContentValues(1);
                    values.put(Mms.SIM_ID, mSimId);
                    SqliteWrapper.update(mContext, cr, msgUri, values, null, null);
                    Log.d(MmsApp.TXN_TAG, "save retrieved mms, simId=" + mSimId);
                }

                // set message size
                int messageSize = resp.length;
                ContentValues sizeValue = new ContentValues();
                sizeValue.put(Mms.MESSAGE_SIZE, messageSize);
                SqliteWrapper.update(mContext, mContext.getContentResolver(), msgUri, sizeValue, null, null);

                // Aurora Hu Junming 2014-04-18 added for bug#4223 start
                //set pdu date  
                ContentValues dateValue = new ContentValues();
                dateValue.put(Mms.DATE, System.currentTimeMillis()/1000L);
                SqliteWrapper.update(mContext, mContext.getContentResolver(), msgUri, dateValue, null, null);
                // Aurora Hu Junming 2014-04-18 added for bug#4223 end
                
                // The M-Retrieve.conf has been successfully downloaded.
                mTransactionState.setState(TransactionState.SUCCESS);
                mTransactionState.setContentUri(msgUri);
                // Remember the location the message was downloaded from.
                // Since it's not critical, it won't fail the transaction.
                // Copy over the locked flag from the M-Notification.ind in case
                // the user locked the message before activating the download.
                updateContentLocation(mContext, msgUri, mContentLocation, mLocked);
            }

            if (msgUri != null) {
                // Delete the corresponding M-Notification.ind.
                String notifId = mUri.getLastPathSegment();
                String msgId = msgUri.getLastPathSegment();
                if (!notifId.equals(msgId)) {
                    SqliteWrapper.delete(mContext, mContext.getContentResolver(),
                                         mUri, null, null);
                }
                // Have to delete messages over limit *after* the delete above. Otherwise,
                // it would be counted as part of the total.
                Recycler.getMmsRecycler().deleteOldMessagesInSameThreadAsMessage(mContext, msgUri);

                //gionee gaoj 2013-3-11 added for CR00782858 start
                // Aurora liugj 2013-11-07 deleted for hide widget start
                    //MmsWidgetProvider.notifyDatasetChanged(mContext);
                      // Aurora liugj 2013-11-07 deleted for hide widget end
                //gionee gaoj 2013-3-11 added for CR00782858 end
            } else {
                // is Duplicate Message, delete notification
                SqliteWrapper.delete(mContext, mContext.getContentResolver(),
                                         mUri, null, null);
            }

            if (mTransactionState.getState() == TransactionState.SUCCESS) {
                MessagingNotification.blockingUpdateNewMessageIndicator(mContext, true, false);
                MessagingNotification.updateDownloadFailedNotification(mContext);
            }

            // Send ACK to the Proxy-Relay to indicate we have fetched the
            // MM successfully.
            // Don't mark the transaction as failed if we failed to send it.
            sendAcknowledgeInd(retrieveConf);
        } catch (Throwable t) {
            Log.e(TAG, Log.getStackTraceString(t));
        } finally {
            if (mTransactionState.getState() != TransactionState.SUCCESS) {
                mTransactionState.setState(TransactionState.FAILED);
                mTransactionState.setContentUri(mUri);
                Log.e(TAG, "Retrieval failed.");
            }
            notifyObservers();
        }
    }

    private boolean isDuplicateMessage(Context context, RetrieveConf rc) {
        byte[] rawMessageId = rc.getMessageId();
        if (rawMessageId != null) {
            String messageId = new String(rawMessageId);
            String selection = "(" + Mms.MESSAGE_ID + " = ? AND "
                                   + Mms.MESSAGE_TYPE + " = ?)";
            //each card has it's own mms.
            if (MmsApp.mGnMultiSimMessage) { //guoyx 20130116
                selection += " AND " + Mms.SIM_ID + " = " + mSimId;
            }
            String[] selectionArgs = new String[] { messageId,
                    String.valueOf(PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF) };
            Cursor cursor = SqliteWrapper.query(
                    context, context.getContentResolver(),
                    Mms.CONTENT_URI, new String[] { Mms._ID },
                    selection, selectionArgs, null);
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        // We already received the same message before.
                        return true;
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return false;
    }

    private void sendAcknowledgeInd(RetrieveConf rc) throws MmsException, IOException {
        Log.v(MmsApp.TXN_TAG, "RetrieveTransaction: sendAcknowledgeInd()");
        // Send M-Acknowledge.ind to MMSC if required.
        // If the Transaction-ID isn't set in the M-Retrieve.conf, it means
        // the MMS proxy-relay doesn't require an ACK.
        byte[] tranId = rc.getTransactionId();
        if (tranId != null) {
            // Create M-Acknowledge.ind
            AcknowledgeInd acknowledgeInd = new AcknowledgeInd(
                    PduHeaders.CURRENT_MMS_VERSION, tranId);

            // insert the 'from' address per spec
            String lineNumber = null;
            // add for gemini
            if (MmsApp.mGnMultiSimMessage) {//guoyx 20130116
                lineNumber = MessageUtils.getLocalNumberGemini(mSimId);
            } else {
                lineNumber = MessageUtils.getLocalNumber();
            }
            acknowledgeInd.setFrom(new EncodedStringValue(lineNumber));

            //MTK_OP01_PROTECT_START
            if (MmsApp.isTelecomOperator()) {
                // X-Mms-Report-Allowed Optional
                SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(mContext);
                boolean reportAllowed = true;
                if (MmsApp.mGnMultiSimMessage) {//guoyx 20130228
                    reportAllowed = prefs.getBoolean(Integer.toString(mSimId)+ "_" + 
                            MessagingPreferenceActivity.MMS_ENABLE_TO_SEND_DELIVERY_REPORT,
                            true);
                } else {
                    reportAllowed = prefs.getBoolean(MessagingPreferenceActivity.MMS_ENABLE_TO_SEND_DELIVERY_REPORT, true);
                }

                Log.d(MmsApp.TXN_TAG, "reportAllowed: " + reportAllowed);
            
                try {
                    acknowledgeInd.setReportAllowed(reportAllowed ? PduHeaders.VALUE_YES : PduHeaders.VALUE_NO);
                } catch(InvalidHeaderValueException ihve) {
                    // do nothing here
                    Log.e(MmsApp.TXN_TAG, "acknowledgeInd.setReportAllowed Failed !!");
                }
            }
            //MTK_OP01_PROTECT_END

            // Pack M-Acknowledge.ind and send it
            if(MmsConfig.getNotifyWapMMSC()) {
                sendPdu(new PduComposer(mContext, acknowledgeInd).make(), mContentLocation);
            } else {
                sendPdu(new PduComposer(mContext, acknowledgeInd).make());
            }
        }
    }

    /**
    * send expired in MM1_notification.Res if the notification expired
    */
    private void sendNotifyRespInd(int status) throws MmsException, IOException {
        // Create the M-NotifyResp.ind
        Log.d(MmsApp.TXN_TAG, "RetrieveTransaction: sendNotifyRespInd for expired.");
        NotificationInd notificationInd = (NotificationInd) PduPersister.getPduPersister(mContext).load(mUri);
        NotifyRespInd notifyRespInd = new NotifyRespInd(
                PduHeaders.CURRENT_MMS_VERSION,
                notificationInd.getTransactionId(),
                status);

        // Pack M-NotifyResp.ind and send it
        if(MmsConfig.getNotifyWapMMSC()) {
            sendPdu(new PduComposer(mContext, notifyRespInd).make(), mContentLocation);
        } else {
            sendPdu(new PduComposer(mContext, notifyRespInd).make());
        }
    }

    private static void updateContentLocation(Context context, Uri uri,
                                              String contentLocation,
                                              boolean locked) {
        Log.d(MmsApp.TXN_TAG, "RetrieveTransaction: updateContentLocation()");

        ContentValues values = new ContentValues(2);
        values.put(Mms.CONTENT_LOCATION, contentLocation);
        values.put(Mms.LOCKED, locked);     // preserve the state of the M-Notification.ind lock.
        SqliteWrapper.update(context, context.getContentResolver(),
                             uri, values, null, null);
    }

    @Override
    public int getType() {
        return RETRIEVE_TRANSACTION;
    }

    // add for gemini
    public Uri getRtrTrxnUri() {
        return mUri;
    }
}
