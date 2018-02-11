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

import static com.android.mms.transaction.TransactionState.FAILED;
import static com.android.mms.transaction.TransactionState.INITIALIZED;
import static com.android.mms.transaction.TransactionState.SUCCESS;
//Aurora xuyong 2013-11-15 modified for google adapt start
import static com.aurora.android.mms.pdu.PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF;
import static com.aurora.android.mms.pdu.PduHeaders.STATUS_DEFERRED;
import static com.aurora.android.mms.pdu.PduHeaders.STATUS_REJECTED;
import static com.aurora.android.mms.pdu.PduHeaders.STATUS_RETRIEVED;
import static com.aurora.android.mms.pdu.PduHeaders.STATUS_UNRECOGNIZED;
//Aurora xuyong 2013-11-15 modified for google adapt end

import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessagingPreferenceActivity;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.Recycler;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.InvalidHeaderValueException;
import com.aurora.android.mms.MmsException;
import com.aurora.android.mms.pdu.GenericPdu;
import com.aurora.android.mms.pdu.NotificationInd;
import com.aurora.android.mms.pdu.NotifyRespInd;
import com.aurora.android.mms.pdu.PduComposer;
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.android.mms.pdu.PduParser;
import com.aurora.android.mms.pdu.PduPersister;
//Aurora xuyong 2013-11-15 modified for google adapt end
import android.database.sqlite.SqliteWrapper;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import aurora.preference.AuroraPreferenceManager;
import gionee.provider.GnTelephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import gionee.provider.GnTelephony.SIMInfo;
import android.telephony.TelephonyManager;
import gionee.telephony.GnTelephonyManager;
import android.util.Config;
import android.util.Log;

import java.io.IOException;
// add for gemini
import com.aurora.featureoption.FeatureOption;
// Aurora xuyong 2014-07-16 added for bug #6446 start
import com.aurora.mms.util.Utils;
// Aurora xuyong 2014-07-16 added for bug #6446 end
import android.content.ContentResolver;
import android.content.ContentValues;
import com.android.mms.MmsApp;
import java.util.Date;
import android.os.SystemProperties;
import com.android.internal.telephony.TelephonyProperties;


//gionee gaoj 2013-3-11 added for CR00782858 start
import com.android.mms.widget.MmsWidgetProvider;
//gionee gaoj 2013-3-11 added for CR00782858 end
/**
 * The NotificationTransaction is responsible for handling multimedia
 * message notifications (M-Notification.ind).  It:
 *
 * <ul>
 * <li>Composes the notification response (M-NotifyResp.ind).
 * <li>Sends the notification response to the MMSC server.
 * <li>Stores the notification indication.
 * <li>Notifies the TransactionService about succesful completion.
 * </ul>
 *
 * NOTE: This MMS client handles all notifications with a <b>deferred
 * retrieval</b> response.  The transaction service, upon succesful
 * completion of this transaction, will trigger a retrieve transaction
 * in case the client is in immediate retrieve mode.
 */
public class NotificationTransaction extends Transaction implements Runnable {
    private static final String TAG = "NotificationTransaction";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private Uri mUri;
    private NotificationInd mNotificationInd;
    private String mContentLocation;

    public NotificationTransaction(
            Context context, int serviceId,
            TransactionSettings connectionSettings, String uriString) {
        super(context, serviceId, connectionSettings);

        mUri = Uri.parse(uriString);

        try {
            mNotificationInd = (NotificationInd)
                    PduPersister.getPduPersister(context).load(mUri);
        } catch (MmsException e) {
            Log.e(TAG, "Failed to load NotificationInd from: " + uriString, e);
            throw new IllegalArgumentException();
        }

        mId = new String(mNotificationInd.getTransactionId());
        mContentLocation = new String(mNotificationInd.getContentLocation());

        // Attach the transaction to the instance of RetryScheduler.
        attach(RetryScheduler.getInstance(context));
    }

    // add for gemini
    public NotificationTransaction(
            Context context, int serviceId, int simId,
            TransactionSettings connectionSettings, String uriString) {
        super(context, serviceId, connectionSettings);

        mUri = Uri.parse(uriString);

        try {
            mNotificationInd = (NotificationInd)
                    PduPersister.getPduPersister(context).load(mUri);
        } catch (MmsException e) {
            Log.e(TAG, "Failed to load NotificationInd from: " + uriString, e);
            throw new IllegalArgumentException();
        }

        mId = new String(mNotificationInd.getTransactionId());
        mSimId = simId;
        mContentLocation = new String(mNotificationInd.getContentLocation());

        // Attach the transaction to the instance of RetryScheduler.
        attach(RetryScheduler.getInstance(context));
    }

    /**
     * This constructor is only used for test purposes.
     */
    public NotificationTransaction(
            Context context, int serviceId,
            TransactionSettings connectionSettings, NotificationInd ind) {
        super(context, serviceId, connectionSettings);

        try {
          // Aurora xuyong 2014-10-23 modified for privacy feature start
          if (MmsApp.sHasPrivacyFeature) {
              long privacy = 0;
              String address = ind.getFrom().getString();
              if (!Mms.isEmailAddress(address)) {
                  privacy = Utils.getFristPrivacyId(context, address);
              }
              mUri = PduPersister.getPduPersister(context).persist(
                        ind, Inbox.CONTENT_URI, privacy);
          } else {
              mUri = PduPersister.getPduPersister(context).persist(
                        ind, Inbox.CONTENT_URI);
          }
          // Aurora xuyong 2014-10-23 modified for privacy feature end
        } catch (MmsException e) {
            Log.e(TAG, "Failed to save NotificationInd in constructor.", e);
            throw new IllegalArgumentException();
        }

        mNotificationInd = ind;
        mId = new String(ind.getTransactionId());
    }

    // add for gemini
    public NotificationTransaction(
            Context context, int serviceId, int simId,
            TransactionSettings connectionSettings, NotificationInd ind) {
        super(context, serviceId, connectionSettings);

        try {
            // Aurora xuyong 2014-10-23 modified for privacy feature start
          if (MmsApp.sHasPrivacyFeature) {
              long privacy = 0;
              String address = ind.getFrom().getString();
              if (!Mms.isEmailAddress(address)) {
                  privacy = Utils.getFristPrivacyId(context, address);
              }
              mUri = PduPersister.getPduPersister(context).persist(
                      ind, Inbox.CONTENT_URI, privacy);
          } else {
            mUri = PduPersister.getPduPersister(context).persist(
                        ind, Inbox.CONTENT_URI);
          }
          // Aurora xuyong 2014-10-23 modified for privacy feature end
        } catch (MmsException e) {
            Log.e(TAG, "Failed to save NotificationInd in constructor.", e);
            throw new IllegalArgumentException();
        }

        mNotificationInd = ind;
        mId = new String(ind.getTransactionId());
        mSimId = simId;
    }

    /*
     * (non-Javadoc)
     * @see com.google.android.mms.pdu.Transaction#process()
     */
    @Override
    public void process() {
        new Thread(this).start();
    }

    public void run() {
        Log.d(MmsApp.TXN_TAG, "NotificationTransaction: run()");
        DownloadManager downloadManager = DownloadManager.getInstance();
        //boolean autoDownload = downloadManager.isAuto();
        //boolean dataSuspended = (MmsApp.getApplication().getTelephonyManager().getDataState() ==
        //        TelephonyManager.DATA_SUSPENDED);
        boolean autoDownload = false;
        boolean dataSuspended = false;
        // add for gemini
        if (MmsApp.mGnMultiSimMessage) {
            autoDownload = downloadManager.isAuto(mSimId);
            int datastate = GnTelephonyManager.getDataStateGemini(SIMInfo.getSlotById(mContext, mSimId));
            dataSuspended = (datastate == TelephonyManager.DATA_SUSPENDED);
        } else {
            autoDownload = downloadManager.isAuto();
            dataSuspended = (MmsApp.getApplication().getTelephonyManager().getDataState() == TelephonyManager.DATA_SUSPENDED);
        }
        
        try {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Notification transaction launched: " + this);
            }

            // By default, we set status to STATUS_DEFERRED because we
            // should response MMSC with STATUS_DEFERRED when we cannot
            // download a MM immediately.
            int status = STATUS_DEFERRED;
            
            // Check expiry state
            Date CurrentDate = new Date(System.currentTimeMillis());
            Date ExpiryDate = new Date(mNotificationInd.getExpiry()*1000);
            Log.d(MmsApp.TXN_TAG, "expiry time=" + ExpiryDate.toLocaleString()
                    +"\t current=" + CurrentDate.toLocaleString());
            
            //MTK_OP01_PROTECT_START
            /*
            if (MmsApp.isTelecomOperator()) {
                // Check Message size
                int msgSize = 0;
                Cursor cursor = SqliteWrapper.query(mContext, mContext.getContentResolver(),
                                 mUri, new String[] {Mms.MESSAGE_SIZE}, null, null, null);
                if (cursor != null && cursor.getCount() == 1 && cursor.moveToFirst()) {
                    try{
                        msgSize = cursor.getInt(0);
                        Log.v(MmsApp.TXN_TAG, "msg Size = " + msgSize);
                    }finally{
                        cursor.close();
                    }
                }

                String netWorkType = null;
                int slotId = -1;
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    // convert sim id to slot id
                    slotId = SIMInfo.getSlotById(mContext, mSimId);
                    netWorkType = SystemProperties.get(slotId == 0 ? 
                            TelephonyProperties.PROPERTY_CS_NETWORK_TYPE : TelephonyProperties.PROPERTY_CS_NETWORK_TYPE_2);
                } else {
                    netWorkType = SystemProperties.get(TelephonyProperties.PROPERTY_CS_NETWORK_TYPE);
                }
                
                boolean bTDNetwork = Integer.parseInt(netWorkType) > 2 ? true : false;
                if ((!bTDNetwork && MmsConfig.getReceiveMmsLimitFor2G() < msgSize/1024)
                        || (bTDNetwork && MmsConfig.getReceiveMmsLimitForTD() < msgSize/1024)) {
                    Log.v(MmsApp.TXN_TAG, "Message size exceed limitation, rejected.");

                    status = STATUS_REJECTED;
                    sendNotifyRespInd(status);
                    return;
                }
            }
            */
            //MTK_OP01_PROTECT_END
            // Don't try to download when data is suspended, as it will fail, so defer download
            if (!autoDownload || dataSuspended) {
                Log.d(MmsApp.TXN_TAG, "Not autoDownload! autoDonwload=" 
                        + autoDownload + ", dataSuspended=" + dataSuspended);
                if (MmsApp.mGnMultiSimMessage) {
                    downloadManager.markState(mUri, DownloadManager.STATE_UNSTARTED, mSimId);
                } else {
                    downloadManager.markState(mUri, DownloadManager.STATE_UNSTARTED);                
                }
                MessagingNotification.blockingUpdateNewMessageIndicator(mContext, true, false);
                MessagingNotification.updateDownloadFailedNotification(mContext);
                sendNotifyRespInd(status);
                return;
            }

            if (MmsApp.mGnMultiSimMessage) {//guoyx 20130116
                downloadManager.markState(mUri, DownloadManager.STATE_DOWNLOADING, mSimId);
            } else {
                downloadManager.markState(mUri, DownloadManager.STATE_DOWNLOADING);
            }

            if (LOCAL_LOGV) {
                Log.v(TAG, "Content-Location: " + mContentLocation);
            }

            byte[] retrieveConfData = null;
            // We should catch exceptions here to response MMSC
            // with STATUS_DEFERRED.
            try {
                Log.d(MmsApp.TXN_TAG, "NotificationTransaction: before getpdu");
                retrieveConfData = getPdu(mContentLocation);
                Log.d(MmsApp.TXN_TAG, "NotificationTransaction: after getpdu");
            } catch (IOException e) {
                mTransactionState.setState(FAILED);
            }

            if (retrieveConfData != null) {
                GenericPdu pdu = new PduParser(retrieveConfData).parse();
                if ((pdu == null) || (pdu.getMessageType() != MESSAGE_TYPE_RETRIEVE_CONF)) {
                    Log.e(TAG, "Invalid M-RETRIEVE.CONF PDU. " +
                            (pdu != null ? "message type: " + pdu.getMessageType() : "null pdu"));
                    mTransactionState.setState(FAILED);
                    status = STATUS_UNRECOGNIZED;
                } else {
                    // Save the received PDU (must be a M-RETRIEVE.CONF).
                    PduPersister p = PduPersister.getPduPersister(mContext);
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
                    Log.d(MmsApp.TXN_TAG, "PDU Saved, Uri=" + uri + "\nDelete Notify Ind, Uri=" + mUri);
                    // add for gemini
                    if (MmsApp.mGnMultiSimMessage) { //guoyx 20130116
                        ContentResolver cr = mContext.getContentResolver();
                        ContentValues values = new ContentValues(1);
                        values.put(Mms.SIM_ID, mSimId);
                        SqliteWrapper.update(mContext, cr, uri, values, null, null);
                    }
                    // Aurora xuyong 2014-07-16 added for bug #6446 start
                    if (MmsApp.sHasRejectFeature) {
                        ContentResolver reCr = mContext.getContentResolver();
                        ContentValues rejectValue = new ContentValues();
                        // Aurora xuyong 2014-11-08 modified for reject new feature start
                        if (Utils.needRejectBlackMms(pdu, mContext) || (Utils.needRejectRubMms(pdu, mContext) && !mAvoidReject)) {
                        // Aurora xuyong 2014-11-08 modified for reject new feature end
                            rejectValue.put("reject", 1);
                          // Aurora xuyong 2014-09-19 added for aurora reject feature start
                          rejectValue.put("newComing", 1);
                          // Aurora xuyong 2014-09-19 added for aurora reject feature end
                          // Aurora xuyong 2014-08-23 added for bug #7909 start
                            MessagingNotification.setIsRejectMsg(true);
                          // Aurora xuyong 2014-08-23 added for bug #7909 end
                          // Aurora xuyong 2014-08-06 added for bug #6895 start
                            SqliteWrapper.update(mContext, reCr, uri, rejectValue, null, null);
                          // Aurora xuyong 2014-08-06 added for bug #6895 end
                        }
                        // Aurora xuyong 2014-08-06 deleted for bug #6895 start
                        //SqliteWrapper.update(mContext, reCr, uri, rejectValue, null, null);
                        // Aurora xuyong 2014-08-06 deleted for bug #6895 end
                    }
                    // Aurora xuyong 2014-07-16 added for bug #6446 end
                    // set message size
                    int messageSize = retrieveConfData.length;
                    ContentValues sizeValue = new ContentValues();
                    sizeValue.put(Mms.MESSAGE_SIZE, messageSize);
                    SqliteWrapper.update(mContext, mContext.getContentResolver(), uri, sizeValue, null, null);
                
                    // We have successfully downloaded the new MM. Delete the
                    // M-NotifyResp.ind from Inbox.
                    String notifId = mUri.getLastPathSegment();
                    String msgId = uri.getLastPathSegment();
                    if (!notifId.equals(msgId)) {
                        SqliteWrapper.delete(mContext, mContext.getContentResolver(),
                                             mUri, null, null);
                    }
                    // Notify observers with newly received MM.
                    mUri = uri;
                    status = STATUS_RETRIEVED;
                }
            }else{
                Log.e(MmsApp.TXN_TAG, "retrieveConfData is null");
                mTransactionState.setState(FAILED);
                status = STATUS_UNRECOGNIZED;                
            }

            // Check the status and update the result state of this Transaction.
            switch (status) {
                case STATUS_RETRIEVED:
                    mTransactionState.setState(SUCCESS);
                    break;
                case STATUS_DEFERRED:
                    // STATUS_DEFERRED, may be a failed immediate retrieval.
                    if (mTransactionState.getState() == INITIALIZED) {
                        mTransactionState.setState(SUCCESS);
                    }
                    break;
            }

            if (mTransactionState.getState() == SUCCESS) {
                MessagingNotification.blockingUpdateNewMessageIndicator(mContext, true, false);
                MessagingNotification.updateDownloadFailedNotification(mContext);
            }
            // if the status is STATUS_UNRECOGNIZED, this happened when we don't get mms pdu from server,
            //this may be a server problem or network problem.
            //our policy is will retry later, so we must response a deferred status not this one.
            //otherwise the server may delete this mms, and when we retry it, it's another mms created by server to 
            //inform us that the old mms is not exist yet.
            if (status == STATUS_UNRECOGNIZED) {
                status = STATUS_DEFERRED;
            }
            sendNotifyRespInd(status);

            // Make sure this thread isn't over the limits in message count.
            Recycler.getMmsRecycler().deleteOldMessagesInSameThreadAsMessage(mContext, mUri);

            //gionee gaoj 2013-3-11 added for CR00782858 start           
                // Aurora liugj 2013-11-07 deleted for hide widget start
                //MmsWidgetProvider.notifyDatasetChanged(mContext);
                  // Aurora liugj 2013-11-07 deleted for hide widget end
            //gionee gaoj 2013-3-11 added for CR00782858 end
            
        } catch (Throwable t) {
            Log.e(TAG, Log.getStackTraceString(t));
            if (null != mUri){
                Recycler.getMmsRecycler().deleteOldMessagesInSameThreadAsMessage(mContext, mUri);
            }
        } finally {
            mTransactionState.setContentUri(mUri);
            if (!autoDownload /*|| dataSuspended*//*comment this case for 81452*/) {
                // Always mark the transaction successful for deferred
                // download since any error here doesn't make sense.
                mTransactionState.setState(SUCCESS);
            }
            if (mTransactionState.getState() != SUCCESS) {
                mTransactionState.setState(FAILED);
                Log.w(MmsApp.TXN_TAG, "NotificationTransaction failed.");
            }
            notifyObservers();
        }
    }

    private void sendNotifyRespInd(int status) throws MmsException, IOException {
        // Create the M-NotifyResp.ind
        Log.v(MmsApp.TXN_TAG, "NotificationTransaction: sendNotifyRespInd()");
        NotifyRespInd notifyRespInd = new NotifyRespInd(
                PduHeaders.CURRENT_MMS_VERSION,
                mNotificationInd.getTransactionId(),
                status);

        //MTK_OP01_PROTECT_START
        if (MmsApp.isTelecomOperator()) {
            // X-Mms-Report-Allowed Optional
            SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(mContext);
            boolean reportAllowed = true;
            if (MmsApp.mGnMultiSimMessage) { //guoyx 20130116
                reportAllowed = prefs.getBoolean(Integer.toString(mSimId)+ "_" + 
                        MessagingPreferenceActivity.MMS_ENABLE_TO_SEND_DELIVERY_REPORT,
                        true);
            } else {
                reportAllowed = prefs.getBoolean(MessagingPreferenceActivity.MMS_ENABLE_TO_SEND_DELIVERY_REPORT, true);
            }

            Log.d(MmsApp.TXN_TAG, "reportAllowed: " + reportAllowed);
            
            try {
                notifyRespInd.setReportAllowed(reportAllowed ? PduHeaders.VALUE_YES : PduHeaders.VALUE_NO);
            } catch(InvalidHeaderValueException ihve) {
                // do nothing here
                Log.e(MmsApp.TXN_TAG, "notifyRespInd.setReportAllowed Failed !!");
            }
        }
        //MTK_OP01_PROTECT_END

        // Pack M-NotifyResp.ind and send it
        if(MmsConfig.getNotifyWapMMSC()) {
            sendPdu(new PduComposer(mContext, notifyRespInd).make(), mContentLocation);
        } else {
            sendPdu(new PduComposer(mContext, notifyRespInd).make());
        }
    }

    @Override
    public int getType() {
        return NOTIFICATION_TRANSACTION;
    }

    // add for gemini
    public Uri getNotTrxnUri(){
        return mUri;
    }
}
