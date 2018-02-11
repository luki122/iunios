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

import com.android.mms.MmsConfig;
import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.ui.MessagingPreferenceActivity;
import com.android.mms.ui.MessageUtils;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;

//Aurora xuyong 2013-11-15 modified for google adapt end
import android.database.sqlite.SqliteWrapper;
import android.app.PendingIntent;
import android.content.ContentResolver;
// Aurora xuyong 2013-12-20 added for google 4.3 adapt start
import android.content.ContentValues;
// Aurora xuyong 2013-12-20 added for google 4.3 adapt end
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import aurora.preference.AuroraPreferenceManager;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.telephony.SmsManager;
import android.text.TextUtils;

import com.gionee.internal.telephony.GnPhone;

import android.util.Log;

import java.util.ArrayList;


// add for gemini
import com.aurora.featureoption.FeatureOption;

//Gionee:linggz 2012-7-4 modify for CR00637047 begin
import android.os.SystemProperties;
//Gionee:linggz 2012-7-4 modify for CR00637047 end

public class SmsMessageSender implements MessageSender {
    protected final Context mContext;
    protected final int mNumberOfDests;
    private final String[] mDests;
    protected final String mMessageText;
    protected final String mServiceCenter;
    protected final long mThreadId;
    protected long mTimestamp;
    private static final String TAG = "SmsMessageSender";
    // add for gemini
    protected int mSimId = 0;
    // Aurora xuyong 2014-11-25 added for bug #10052 start
    private boolean mVia = false;
    // Aurora xuyong 2014-11-25 added for bug #10052 end
    
    // Aurora yudingmin 2014-12-09 added for thread's app result start
    private String mThirdResponse = null;
    
    public void setThirdResponse(String thirdResponse){
        mThirdResponse = thirdResponse;
    }
    
    public String getThirdResponse(){
        return mThirdResponse;
    }
    // Aurora yudingmin 2014-12-09 added for thread's app result end

    // Default preference values
    //Gionee:linggz 2012-7-4 modify for CR00637047 begin
     //Gionee: tangzepeng 2012-10-09 modify for polytron set the deliver report as "on" begin 
    //private static final boolean DEFAULT_DELIVERY_REPORT_MODE  = false;
    private static boolean DEFAULT_DELIVERY_REPORT_MODE  = false;
    static {
        if (SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY") 
       // Aurora xuyong 2014-09-26 modified for india requirement start
        || SystemProperties.get("ro.gn.oversea.custom").equals("INDONESIA_POLYTRON") || MmsApp.mHasIndiaFeature) {
       // Aurora xuyong 2014-09-26 modified for india requirement end
            DEFAULT_DELIVERY_REPORT_MODE  = true;
        }
    }
     //Gionee: tangzepeng 2012-10-09 modify for polytron set the deliver report as "on" end
    //Gionee:linggz 2012-7-4 modify for CR00637047 end

    private static final String[] SERVICE_CENTER_PROJECTION = new String[] {
        Sms.Conversations.REPLY_PATH_PRESENT,
        Sms.Conversations.SERVICE_CENTER,
    };

    private static final int COLUMN_REPLY_PATH_PRESENT = 0;
    private static final int COLUMN_SERVICE_CENTER     = 1;

    public SmsMessageSender(Context context, String[] dests, String msgText, long threadId) {
        mContext = context;
        mMessageText = msgText;
        if (dests != null) {
            mNumberOfDests = dests.length;
            mDests = new String[mNumberOfDests];
            System.arraycopy(dests, 0, mDests, 0, mNumberOfDests);
        } else {
            mNumberOfDests = 0;
            mDests = null;
        }
        mTimestamp = System.currentTimeMillis();
        mThreadId = threadId;
        mServiceCenter = getOutgoingServiceCenter(mThreadId);
        //gionee gaoj 2012-8-21 added for CR00678315 start
        if (MmsApp.mGnRegularlyMsgSend) {
            mIsRegularlySend = false;
        }
        //gionee gaoj 2012-8-21 added for CR00678315 end
    }
    // Aurora xuyong 2014-11-25 added for bug #10052 start
    public SmsMessageSender(Context context, String[] dests, String msgText, long threadId, boolean via) {
        this(context, dests, msgText, threadId);
        mVia = via;
    }
    // Aurora xuyong 2014-11-25 added for bug #10052 end
    // add for gemini
    public SmsMessageSender(Context context, String[] dests, String msgText, long threadId, int simId) {
        mContext = context;
        mMessageText = msgText;
        if (dests != null) {
            mNumberOfDests = dests.length;
            mDests = new String[mNumberOfDests];
            System.arraycopy(dests, 0, mDests, 0, mNumberOfDests);
        } else {
            mNumberOfDests = 0;
            mDests = null;
        }
        mTimestamp = System.currentTimeMillis();
        mThreadId = threadId;
        mServiceCenter = getOutgoingServiceCenter(mThreadId);
        mSimId = simId;
        //gionee gaoj 2012-8-21 added for CR00678315 start
        if (MmsApp.mGnRegularlyMsgSend) {
            mIsRegularlySend = false;
        }
        //gionee gaoj 2012-8-21 added for CR00678315 end
    }
    // Aurora xuyong 2014-11-25 added for bug #10052 start
    public SmsMessageSender(Context context, String[] dests, String msgText, long threadId, int simId, boolean via) {
        this(context, dests, msgText, threadId, simId);
        mVia = via;
    }
    // Aurora xuyong 2014-11-25 added for bug #10052 end
    // gionee gaoj 2012-8-21 added for CR00678315 start
    private boolean mIsRegularlySend = false;
    public SmsMessageSender(Context context, String[] dests, String msgText, long threadId, int simId, long time) {
        mContext = context;
        mMessageText = msgText;
        if (dests != null) {
            mNumberOfDests = dests.length;
            mDests = new String[mNumberOfDests];
            System.arraycopy(dests, 0, mDests, 0, mNumberOfDests);
        } else {
            mNumberOfDests = 0;
            mDests = null;
        }
        mTimestamp = time;
        mThreadId = threadId;
        mServiceCenter = getOutgoingServiceCenter(mThreadId);
        mSimId = simId;
        mIsRegularlySend = true;
    }
    //gionee gaoj 2012-8-21 added for CR00678315 end
    //Gionee <guoyx> <2013-05-16> add for CR00813219 begin
    public SmsMessageSender(Context context, String[] dests, String msgText, long threadId, long time) {
        mContext = context;
        mMessageText = msgText;
        if (dests != null) {
            mNumberOfDests = dests.length;
            mDests = new String[mNumberOfDests];
            System.arraycopy(dests, 0, mDests, 0, mNumberOfDests);
        } else {
            mNumberOfDests = 0;
            mDests = null;
        }
        mTimestamp = time;
        mThreadId = threadId;
        mServiceCenter = getOutgoingServiceCenter(mThreadId);
        mIsRegularlySend = true;
    }
    //Gionee <guoyx> <2013-05-16> add for CR00813219 end

    public boolean sendMessage(long token) throws MmsException {
        // In order to send the message one by one, instead of sending now, the message will split,
        // and be put into the queue along with each destinations
        return queueMessage(token);
    }

    private boolean queueMessage(long token) throws MmsException {
        Log.v(MmsApp.TXN_TAG, "queueMessage()");
        if ((mMessageText == null) || (mNumberOfDests == 0)) {
            // Don't try to send an empty message.
            throw new MmsException("Null message body or dest.");
        }

        SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(mContext);
        boolean requestDeliveryReport = false;
        // add for gemini
        // Aurora xuyong 2014-05-26 deleted for multisim feature start
        //if (MmsApp.mGnMultiSimMessage) { //guoyx 20130116
        //    requestDeliveryReport = prefs.getBoolean(Integer.toString(mSimId)+"_"+
        //        MessagingPreferenceActivity.SMS_DELIVERY_REPORT_MODE,
        //        DEFAULT_DELIVERY_REPORT_MODE);
        //} else {
        // Aurora xuyong 2014-05-26 modified for multisim feature end
            requestDeliveryReport = prefs.getBoolean(
                MessagingPreferenceActivity.SMS_DELIVERY_REPORT_MODE,
                DEFAULT_DELIVERY_REPORT_MODE);
        // Aurora xuyong 2014-05-26 modified for multisim feature start
        //}
        // Aurora xuyong 2014-05-26 modified for multisim feature end
        Log.d(MmsApp.TXN_TAG, "SMS DR request=" + requestDeliveryReport);

        for (int i = 0; i < mNumberOfDests; i++) {
            try {
                if (LogTag.DEBUG_SEND) {
                    Log.v(TAG, "queueMessage mDests[i]: " + mDests[i] + " mThreadId: " + mThreadId);
                }
                // add for gemini
                if (MmsApp.mGnMultiSimMessage) {
                    // Aurora xuyong 2013-11-15 modified for S4 adapt start
                    //Aurora hujunming 2014-5-14 added for multiSim start
                // Aurora xuyong 2014-07-25 modified for multiSim start
                    // Aurora xuyong 2014-11-25 added for bug #10052 start
                    Uri uri = null;
                    if (mVia) {
                        uri = Uri.parse("content://sms/queued/via");
                    } else {
                        uri = Uri.parse("content://sms/queued");
                    }
                    // Aurora xuyong 2014-11-25 added for bug #10052 end
                    gionee.provider.GnTelephony.Sms.addMessageToUri(mContext.getContentResolver(), 
                // Aurora xuyong 2014-07-25 modified for multiSim end
                        // Aurora xuyong 2014-11-25 modified for bug #10052 start
                        uri, mDests[i],
                        // Aurora xuyong 2014-11-25 modified for bug #10052 end
                        mMessageText, null, mTimestamp,
                        true /* read */,
                        requestDeliveryReport,
                        mThreadId,
                        mSimId);
//                    Log.d("ra_cma", mContext.getContentResolver().toString()+Uri.parse("content://sms/queued")+"  "+mDests[i]+"  "+mMessageText+"  "+mTimestamp+"  "+requestDeliveryReport+"  "+mThreadId+"  "+mSimId);
                    //Aurora hujunming 2014-5-14 added for multiSim end
                    // Aurora xuyong 2013-11-15 modified for S4 adapt end
                } else {
                    // Aurora xuyong 2013-12-20 modified for google 4.3 adapt start
                    // Aurora liugj 2014-01-09 modified for iuni-4.3 adapt start
                    // Aurora xuyong 2014-11-25 added for bug #10052 start
                    Uri uri = null;
                    if (mVia) {
                        uri = Uri.parse("content://sms/queued/via");
                    } else {
                        uri = Uri.parse("content://sms/queued");
                    }
                    // Aurora xuyong 2014-11-25 added for bug #10052 end
                    Sms.addMessageToUri(mContext.getContentResolver(),
                            // Aurora xuyong 2014-11-25 modified for bug #10052 start
                            uri, mDests[i],
                            // Aurora xuyong 2014-11-25 modified for bug #10052 end
                            mMessageText, null, mTimestamp, true /* read */,
                            requestDeliveryReport, mThreadId);
                    /*ContentValues values = new ContentValues(8);
                    values.put("sim_id", 0);
                    values.put("address", mDests[i]);
                    values.put("date", mTimestamp);
                    values.put("read", Integer.valueOf(1));
                    values.put("body", mMessageText);
                    if (requestDeliveryReport) {
                        values.put("status", 32);
                    }
                    if (mThreadId != -1L) {
                        values.put("thread_id", mThreadId);
                    }
                    mContext.getContentResolver().insert(Uri.parse("content://sms/queued"), values);*/
                        // Aurora liugj 2014-01-09 modified for iuni-4.3 adapt end
                    // Aurora xuyong 2013-12-20 modified for google 4.3 adapt end
                }
            } catch (SQLiteException e) {
                if (LogTag.DEBUG_SEND) {
                    Log.e(TAG, "queueMessage SQLiteException", e);
                }
                SqliteWrapper.checkSQLiteException(mContext, e);
            }
        }
        // Notify the SmsReceiverService to send the message out
        // add for gemini
        //gionee gaoj 2012-8-21 added for CR00678315 start
        if (MmsApp.mGnRegularlyMsgSend && mIsRegularlySend) {
            return false;
        }
        //gionee gaoj 2012-8-21 added for CR00678315 end
        // Aurora yudingmin 2014-12-09 modified for thread's app result start
        if (MmsApp.mGnMultiSimMessage) {
            Intent sentIt = new Intent(SmsReceiverService.ACTION_SEND_MESSAGE,
                null,
                mContext,
                SmsReceiver.class);
            sentIt.putExtra(GnPhone.GEMINI_SIM_ID_KEY, mSimId);
            if(!TextUtils.isEmpty(mThirdResponse)){
                Log.v(TAG, "Third_Response for SmsReceiver 1 is " + mThirdResponse);
                sentIt.putExtra(Third_Response, mThirdResponse);
            }
            mContext.sendBroadcast(sentIt);
        } else {
            Intent sentIt = new Intent(SmsReceiverService.ACTION_SEND_MESSAGE,
                    null,
                    mContext,
                    SmsReceiver.class);
            if(!TextUtils.isEmpty(mThirdResponse)){
                Log.v(TAG, "Third_Response for SmsReceiver 2 is " + mThirdResponse);
                sentIt.putExtra(Third_Response, mThirdResponse);
            }
            mContext.sendBroadcast(sentIt);
        }
        // Aurora yudingmin 2014-12-09 modified for thread's app result end
        return false;
    }

    /**
     * Get the service center to use for a reply.
     *
     * The rule from TS 23.040 D.6 is that we send reply messages to
     * the service center of the message to which we're replying, but
     * only if we haven't already replied to that message and only if
     * <code>TP-Reply-Path</code> was set in that message.
     *
     * Therefore, return the service center from the most recent
     * message in the conversation, but only if it is a message from
     * the other party, and only if <code>TP-Reply-Path</code> is set.
     * Otherwise, return null.
     */
    private String getOutgoingServiceCenter(long threadId) {
        Cursor cursor = null;

        try {
            cursor = SqliteWrapper.query(mContext, mContext.getContentResolver(),
                            Sms.CONTENT_URI, SERVICE_CENTER_PROJECTION,
                            // Aurora xuyong 2014-11-05 modified for privacy feature start
                            "thread_id = " + threadId + " AND type = "+Sms.MESSAGE_TYPE_INBOX + " AND is_privacy >= 0", null, "date DESC");
                            // Aurora xuyong 2014-11-05 modified for privacy feature end

            if ((cursor == null) || !cursor.moveToFirst()) {
                return null;
            }

            boolean replyPathPresent = (1 == cursor.getInt(COLUMN_REPLY_PATH_PRESENT));
            return replyPathPresent ? cursor.getString(COLUMN_SERVICE_CENTER) : null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void log(String msg) {
        Log.d(LogTag.TAG, "[SmsMsgSender] " + msg);
    }


    // add for gemini
    // 2.2  no used
    public boolean sendMessageGemini(long token, int simId) throws MmsException {
        return false;
    }
  
}
