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

package com.android.mms.ui;
// Aurora xuyong 2013-12-11 added for aurora's new feature start
import java.util.ArrayList;
// Aurora xuyong 2013-12-11 added for aurora's new feature end
import java.util.regex.Pattern;

import com.android.mms.R;
import com.android.mms.data.Contact;
// Aurora xuyong 2013-12-11 added for aurora's new feature start
import com.android.mms.model.GroupItemInfoModel;
// Aurora xuyong 2013-12-11 added for aurora's new feature end
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.TextModel;
import com.android.mms.ui.MessageListAdapter.ColumnsMap;
import com.android.mms.util.AddressUtils;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
import com.aurora.android.mms.pdu.EncodedStringValue;
import com.aurora.android.mms.pdu.MultimediaMessagePdu;
import com.aurora.android.mms.pdu.NotificationInd;
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.android.mms.pdu.PduPersister;
import com.aurora.android.mms.pdu.RetrieveConf;
import com.aurora.android.mms.pdu.SendReq;
//Aurora xuyong 2013-11-15 modified for google adapt end

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import gionee.provider.GnTelephony.Mms;
import gionee.provider.GnTelephony.MmsSms;
import gionee.provider.GnTelephony.Sms;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;

//gionee gaoj 2012-4-10 added for CR00555790 start
import com.android.mms.MmsApp;
//gionee gaoj 2012-4-10 added for CR00555790 end
//a0
import android.telephony.SmsManager;
//add for gemini
import com.aurora.featureoption.FeatureOption;
// Aurora xuyong 2016-01-28 added for xy-smartsms start
import com.xy.smartsms.iface.IXYSmartMessageItem;
import java.util.HashMap;
import java.util.Map;
// Aurora xuyong 2016-01-28 added for xy-smartsms end

import android.database.sqlite.SqliteWrapper;
//a1

/**
 * Mostly immutable model for an SMS/MMS message.
 *
 * <p>The only mutable field is the cached formatted message member,
 * the formatting of which is done outside this model in MessageListItem.
 */
// Aurora xuyong 2016-01-28 modified for xy-smartsms start
public class MessageItem implements IXYSmartMessageItem {
// Aurora xuyong 2016-01-28 modified for xy-smartsms end
    private static String TAG = "MessageItem";

    public enum DeliveryStatus  { NONE, INFO, FAILED, PENDING, RECEIVED }

    final Context mContext;
    final String mType;
    final long mMsgId;
    final int mBoxId;

    DeliveryStatus mDeliveryStatus;
    // Aurora xuyong 2013-12-11 added for aurora's new feature start
    GroupItemInfoModel mGIIF;
    // Aurora xuyong 2013-12-11 added for aurora's new feature end
    boolean mReadReport;
    boolean mLocked;            // locked to prevent auto-deletion

    String mTimestamp;
    // Aurora xuyong 2015-04-23 added for aurora's new feature start
    String mBatchTimestamp;
    // Aurora xuyong 2015-04-23 added for aurora's new feature end
    // Aurora xuyong 2013-10-18 added for bug #47 start
    long mAuroraDate;
    // Aurora xuyong 2013-10-18 added for bug #47 end
    String mAddress;
    String mContact;
    String mBody; // Body of SMS, first text of MMS.
    String mTextContentType; // ContentType of text of MMS.
    Pattern mHighlight; // portion of message to highlight (from search)

    // The only non-immutable field.  Not synchronized, as access will
    // only be from the main GUI thread.  Worst case if accessed from
    // another thread is it'll return null and be set again from that
    // thread.
    CharSequence mCachedFormattedMessage;
    String mIdentifyNumber = null;

    // The last message is cached above in mCachedFormattedMessage. In the latest design, we
    // show "Sending..." in place of the timestamp when a message is being sent. mLastSendingState
    // is used to keep track of the last sending state so that if the current sending state is
    // different, we can clear the message cache so it will get rebuilt and recached.
    boolean mLastSendingState;

    // Fields for MMS only.
    Uri mMessageUri;
    int mMessageType;
    int mAttachmentType;
    String mSubject;
    SlideshowModel mSlideshow;
    int mMessageSize;
    int mErrorType;
    int mErrorCode;

    //gionee gaoj 2012-4-10 added for CR00555790 start
    boolean mStar;
    private long mMsgStatus = Sms.STATUS_NONE;
    //gionee gaoj 2012-4-10 added for CR00555790 end

    //gionee gaoj 2012-8-14 added for CR00623375 start
    public boolean mIsRegularlyMms = false;
    //gionee gaoj 2012-8-14 added for CR00623375 end
    // Aurora xuyong 2014-10-23 added for priacy feature start
    public long mPrivacy = -1;
    public String mWeatherInfo = null;
    // Aurora xuyong 2014-10-23 added for priacy feature end
    // Aurora xuyong 2015-12-29 added for aurora 2.0 new feature start
    public int mFolded = 0;
    // Aurora xuyong 2015-12-29 added for aurora 2.0 new feature end
    //Gionee <gaoj> <2013-4-11> added for CR00796538 start
    long mDelayTime = 15 * 60 * 1000;
    long mSendDate = 0;
    String mSendTimestamp = null;
    //Gionee <gaoj> <2013-4-11> added for CR00796538 end
    // Aurora xuyong 2016-01-28 added for xy-smartsms start
    private long mSmsReceiveTime;
    // Aurora xuyong 2016-01-28 added for xy-smartsms end
    // Aurora xuyong 2016-03-05 added for bug #20677 start
    MessageItem(Context context, int boxId, int messageType, int simId, int errorType,
                int locked, int charset, long msgId, String type, String subject, String serviceCenter,
                String deliveryReport, String readReport, Pattern highlight, int folded) throws MmsException {
        this(context, boxId, messageType, simId, errorType, locked, charset, msgId, type,
                subject, serviceCenter, deliveryReport, readReport, highlight);
        mFolded = folded;
    }
    // Aurora xuyong 2016-03-05 added for bug #20677 end
    MessageItem(Context context, int boxId, int messageType, int simId, int errorType,
        int locked, int charset, long msgId, String type, String subject, String serviceCenter,
        String deliveryReport, String readReport, Pattern highlight) throws MmsException {
        mContext = context;
        mBoxId = boxId;
        mMessageType = messageType;
        mSimId = simId;
        mErrorType = errorType;
        mLocked = locked != 0;
        if (!TextUtils.isEmpty(subject)) {
            EncodedStringValue v = new EncodedStringValue(charset,
                    PduPersister.getBytes(subject));
            mSubject = v.getString();
        }
        mMsgId = msgId;
        mType = type;
        mServiceCenter = serviceCenter;
        mHighlight = highlight;

        mMessageUri = ContentUris.withAppendedId(Mms.CONTENT_URI, mMsgId);
        long timestamp = 0L;
        PduPersister p = PduPersister.getPduPersister(mContext);
        if (PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND == mMessageType) {
            mDeliveryStatus = DeliveryStatus.NONE;
            NotificationInd notifInd = (NotificationInd) p.load(mMessageUri);
            interpretFrom(notifInd.getFrom(), mMessageUri);
            // Borrow the mBody to hold the URL of the message.
            mBody = new String(notifInd.getContentLocation());
            mMessageSize = (int) notifInd.getMessageSize();
            timestamp = notifInd.getExpiry() * 1000L;
        } else {
            MultimediaMessagePdu msg = (MultimediaMessagePdu) p.load(mMessageUri);
            mSlideshow = SlideshowModel.createFromPduBody(context, msg.getBody());
            mAttachmentType = MessageUtils.getAttachmentType(mSlideshow);
            //a0
            mHasDrmContent = false;
            mHasDrmContent = mSlideshow.checkDrmContent();
            //a1

            if (mMessageType == PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF) {
                RetrieveConf retrieveConf = (RetrieveConf) msg;
                interpretFrom(retrieveConf.getFrom(), mMessageUri);
                timestamp = retrieveConf.getDate() * 1000L;
            } else {
                // Use constant string for outgoing messages
                mContact = mAddress = context.getString(R.string.messagelist_sender_self);
                timestamp = ((SendReq) msg).getDate() * 1000L;
            }

            if ((deliveryReport == null) || !mAddress.equals(context.getString(
                    R.string.messagelist_sender_self))) {
                mDeliveryStatus = DeliveryStatus.NONE;
            } else {
                int reportInt;
                try {
                    reportInt = Integer.parseInt(deliveryReport);
                    if (reportInt == PduHeaders.VALUE_YES) {
                        mDeliveryStatus = DeliveryStatus.RECEIVED;
                    } else {
                        mDeliveryStatus = DeliveryStatus.NONE;
                    }
                } catch (NumberFormatException nfe) {
                    Log.e(TAG, "Value for delivery report was invalid.");
                    mDeliveryStatus = DeliveryStatus.NONE;
                }
            }

            if ((readReport == null) || !mAddress.equals(context.getString(
                    R.string.messagelist_sender_self))) {
                mReadReport = false;
            } else {
                int reportInt;
                try {
                    reportInt = Integer.parseInt(readReport);
                    mReadReport = (reportInt == PduHeaders.VALUE_YES);
                } catch (NumberFormatException nfe) {
                    Log.e(TAG, "Value for read report was invalid.");
                    mReadReport = false;
                }
            }

            SlideModel slide = mSlideshow.get(0);
            if ((slide != null) && slide.hasText()) {
                TextModel tm = slide.getText();
                if (tm.isDrmProtected()) {
                    mBody = mContext.getString(R.string.drm_protected_text);
                } else {
                    mBody = tm.getText();
                }
                mTextContentType = tm.getContentType();
            }

            mMessageSize = mSlideshow.getCurrentSlideshowSize();
        }

        if (!isOutgoingMessage()) {
            //mTimestamp = MessageUtils.formatTimeStampString(context, timestamp);
            mTimestamp = MessageUtils.formatAuroraTimeStampString(context, timestamp, false);
            // Aurora xuyong 2015-04-23 added for aurora's new feature start
            mBatchTimestamp = MessageUtils.formatAuroraTimeStampString(timestamp);
            // Aurora xuyong 2015-04-23 added for aurora's new feature end
            // Aurora xuyong 2013-10-18 added for bug #47 start
            mAuroraDate = timestamp;
            // Aurora xuyong 2013-10-18 added for bug #47 end
        }

        //gionee gaoj 2012-8-14 added for CR00623375 start
        //ginoee gaoj 2012-8-20 modified for CR00678308 start
        if (MmsApp.mGnRegularlyMsgSend && isOutgoingMessage()) {
            //ginoee gaoj 2012-8-20 modified for CR00678308 end
            mSmsDate = timestamp;
            if (timestamp > System.currentTimeMillis()) {
                mIsRegularlyMms = true;
               // mTimestamp = MessageUtils.detailFormatGNTime(context, timestamp);
               // mTimestamp = String.format(context.getString(R.string.gn_will_send_time), mTimestamp);
                mTimestamp = MessageUtils.formatAuroraTimeStampString(context, timestamp, false);
                // Aurora xuyong 2015-04-23 added for aurora's new feature start
                mBatchTimestamp = MessageUtils.formatAuroraTimeStampString(timestamp);
                // Aurora xuyong 2015-04-23 added for aurora's new feature end
                // Aurora xuyong 2013-10-18 added for bug #47 start
                mAuroraDate = timestamp;
                // Aurora xuyong 2013-10-18 added for bug #47 end
            } else {
                mIsRegularlyMms = false;
            }
        }
        //gionee gaoj 2012-8-14 added for CR00623375 end

        //Gionee <gaoj> <2013-4-11> added for CR00796538 start
        if (!isOutgoingMessage() && MmsApp.mDisplaySendTime) {
            mSendTimestamp = null;
            Cursor cursor = null;
            try {
                cursor = SqliteWrapper.query(mContext, mContext.getContentResolver(),
                        mMessageUri, new String[] {Mms.DATE_SENT},
                        null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    mSendDate = cursor.getLong(0) * 1000L;
                    Log.d(TAG, "cursor  mSendDate = "+mSendDate);
                }
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
            if (mSendDate != 0 && (mSendDate + mDelayTime) < timestamp) {
                mSendTimestamp = String.format(context.getString(R.string.gn_send_time),
                        MessageUtils.detailFormatGNTime(context, mSendDate));
                //mTimestamp = MessageUtils.detailFormatGNTime(context, timestamp);
                mTimestamp = MessageUtils.formatAuroraTimeStampString(context, timestamp, false);
                // Aurora xuyong 2015-04-23 added for aurora's new feature start
                mBatchTimestamp = MessageUtils.formatAuroraTimeStampString(timestamp);
                // Aurora xuyong 2015-04-23 added for aurora's new feature end
                // Aurora xuyong 2013-10-18 added for bug #47 start
                mAuroraDate = timestamp;
                // Aurora xuyong 2013-10-18 added for bug #47 end
            }
        }
        //Gionee <gaoj> <2013-4-11> added for CR00796538 end
    }
    // Aurora xuyong 2013-12-11 modified for aurora's new feature start
    MessageItem(Context context, String type, Cursor cursor,
            ColumnsMap columnsMap, Pattern highlight, GroupItemInfoModel model) throws MmsException {
    // Aurora xuyong 2013-12-11 modified for aurora's new feature end
        mContext = context;
        //a0
        if (cursor == null){
            throw new MmsException("Get the null cursor");
        }
        //a1
        mMsgId = cursor.getLong(columnsMap.mColumnMsgId);
        mHighlight = highlight;
        mType = type;

        //a0
        mServiceCenter = cursor.getString(columnsMap.mColumnSmsServiceCenter);
        // Aurora xuyong 2014-10-23 added for priacy feature start
        mPrivacy = cursor.getLong(columnsMap.mPrivacy);
        mWeatherInfo = cursor.getString(columnsMap.mWeatherInfo);
        // Aurora xuyong 2014-10-23 added for priacy feature end
        // Aurora xuyong 2013-12-11 modified for aurora's new feature start
        mGIIF = model;
        // Aurora xuyong 2013-12-11 modified for aurora's new feature end
        //to filter SIM Message
        mSimId = -1;
        //a1
        if ("sms".equals(type)) {
            mReadReport = false; // No read reports in sms

            long status = cursor.getLong(columnsMap.mColumnSmsStatus);
            //a0
            if (status == SmsManager.STATUS_ON_ICC_READ
                    || status == SmsManager.STATUS_ON_ICC_UNREAD
                    || status == SmsManager.STATUS_ON_ICC_SENT
                    || status == SmsManager.STATUS_ON_ICC_UNSENT) {
                mSimMsg = true;
            }
            //a1
            
            //m0
            /*
            if (status == Sms.STATUS_NONE) {
                // No delivery report requested
                mDeliveryStatus = DeliveryStatus.NONE;
            } else if (status >= Sms.STATUS_FAILED) {
                // Failure
                mDeliveryStatus = DeliveryStatus.FAILED;
            } else if (status >= Sms.STATUS_PENDING) {
                // Pending
                mDeliveryStatus = DeliveryStatus.PENDING;
            } else {
                // Success
                mDeliveryStatus = DeliveryStatus.RECEIVED;
            }*/
            if (status >= Sms.STATUS_FAILED) {
                // Failure
                mDeliveryStatus = DeliveryStatus.FAILED;
            } else if (status >= Sms.STATUS_PENDING) {
                // Pending
                mDeliveryStatus = DeliveryStatus.PENDING;
            } else if (status >= Sms.STATUS_COMPLETE && !mSimMsg) {
                // Success
                mDeliveryStatus = DeliveryStatus.RECEIVED;
            } else {
                mDeliveryStatus = DeliveryStatus.NONE;
            }
            //m1
            // Aurora xuyong 2013-12-11 modified for aurora's new feature start
            ArrayList<Integer> messageStatus = mGIIF.getStatus();
            for (Integer item : messageStatus) {
                if (item >= Sms.STATUS_FAILED) {
                    mDeliveryStatus = DeliveryStatus.FAILED;
                    break;
                }
            }
            for (Integer item : messageStatus) {
                if (item >= Sms.STATUS_PENDING && item < Sms.STATUS_FAILED) {
                    mDeliveryStatus = DeliveryStatus.PENDING;
                    break;
                }
            }
            // Aurora xuyong 2013-12-11 modified for aurora's new feature end

            mMessageUri = ContentUris.withAppendedId(Sms.CONTENT_URI, mMsgId);
            // Set contact and message body
            //m0
            //mBoxId = cursor.getInt(columnsMap.mColumnSmsType);
            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mGnMessageSupport == true) {
                if (status == SmsManager.STATUS_ON_ICC_READ
                        || status == SmsManager.STATUS_ON_ICC_UNREAD) {
                    mBoxId = Sms.MESSAGE_TYPE_INBOX;
                } else if (status == SmsManager.STATUS_ON_ICC_SENT
                        || status == SmsManager.STATUS_ON_ICC_UNSENT) {
                    mBoxId = Sms.MESSAGE_TYPE_SENT;
                } else {
                    mBoxId = cursor.getInt(columnsMap.mColumnSmsType);
                }

                mMsgStatus = status;
            } else {
                //gionee gaoj 2012-4-10 added for CR00555790 end
            if (mSimMsg) {
                if (status == SmsManager.STATUS_ON_ICC_SENT 
                        || status == SmsManager.STATUS_ON_ICC_UNSENT) {
                    mBoxId = Sms.MESSAGE_TYPE_SENT;
                } else {
                    mBoxId = Sms.MESSAGE_TYPE_INBOX;
                }
            } else {
                mBoxId = cursor.getInt(columnsMap.mColumnSmsType);
            }
            //gionee gaoj 2012-4-10 added for CR00555790 start
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
            //m1
            mAddress = cursor.getString(columnsMap.mColumnSmsAddress);
            
            //a0
            //add for gemini
            if (MmsApp.mGnMultiSimMessage){
                mSimId = cursor.getInt(columnsMap.mColumnSmsSimId);
            }
            //a1
            
            //m0
            /*if (Sms.isOutgoingFolder(mBoxId)) {
                String meString = context.getString(
                        R.string.messagelist_sender_self);

                mContact = meString;
            } else {
                // For incoming messages, the ADDRESS field contains the sender.
                mContact = Contact.get(mAddress, false).getName();
            }*/
            //gionee gaoj 2012-9-20 added for CR00699291 start
            if (MmsApp.mGnMessageSupport && mSimMsg) {
            //gionee gaoj 2012-9-20 added for CR00699291 end
            if (Sms.isOutgoingFolder(mBoxId) && !mSimMsg) {
                String meString = context.getString(
                        R.string.messagelist_sender_self);

                mContact = meString;
            } else {
                // For incoming messages, the ADDRESS field contains the sender.
                if(!TextUtils.isEmpty(mAddress)) {
                    mContact = Contact.get(mAddress, true).getName();
                } else {
                    mContact = context.getString(android.R.string.unknownName);
                }
            }
            //gionee gaoj 2012-9-20 added for CR00699291 start
            }
            //gionee gaoj 2012-9-20 added for CR00699291 end
            //m1
            
            //m0
            //mBody = cursor.getString(columnsMap.mColumnSmsBody);
            if (mSimMsg) {
                mBody = mContact + " : " + cursor.getString(columnsMap.mColumnSmsBody);
            } else {
                mBody = cursor.getString(columnsMap.mColumnSmsBody);
            }
            //m1

            // Unless the message is currently in the progress of being sent, it gets a time stamp.
            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                long date = cursor.getLong(columnsMap.mColumnSmsDate);
                // Aurora xuyong 2016-01-28 added for xy-smartsms start
                mSmsReceiveTime = date;
                // Aurora xuyong 2016-01-28 added for xy-smartsms end
                mSmsDate = date;
                if (date != 0) {
                    //mTimestamp = MessageUtils.detailFormatGNTime(context, date);
                    mTimestamp = MessageUtils.formatAuroraTimeStampString(context, date, false);
                    // Aurora xuyong 2015-04-23 added for aurora's new feature start
                    mBatchTimestamp = MessageUtils.formatAuroraTimeStampString(date);
                    // Aurora xuyong 2015-04-23 added for aurora's new feature end
                    // Aurora xuyong 2013-10-18 added for bug #47 start
                    mAuroraDate = date;
                    // Aurora xuyong 2013-10-18 added for bug #47 end
                    //gionee gaoj 2012-8-14 added for CR00623375 start
                    if (MmsApp.mGnRegularlyMsgSend && isOutgoingMessage()) {
                        if (date > System.currentTimeMillis()) {
                            mIsRegularlyMms = true;
                            //mTimestamp = String.format(context.getString(R.string.gn_will_send_time), mTimestamp);
                        } else {
                            mIsRegularlyMms = false;
                        }
                    }
                    //gionee gaoj 2012-8-14 added for CR00623375 end
                } else {
                    mTimestamp = "";
                    // Aurora xuyong 2015-04-23 added for aurora's new feature start
                    mBatchTimestamp = "";
                    // Aurora xuyong 2015-04-23 added for aurora's new feature end
                    // Aurora xuyong 2013-10-18 added for bug #47 start
                    mAuroraDate = 0;
                    // Aurora xuyong 2013-10-18 added for bug #47 end
                }
            } else {
                //gionee gaoj 2012-4-10 added for CR00555790 end
            if (!isOutgoingMessage()) {
                // Set "received" or "sent" time stamp
                long date = cursor.getLong(columnsMap.mColumnSmsDate);
                //m0
                //mTimestamp = MessageUtils.formatTimeStampString(context, date);
                mSmsDate = date;
                if (date != 0) {
                    if (isReceivedMessage()){
                        //mTimestamp = MessageUtils.formatTimeStampString(context, date);
                        mTimestamp = MessageUtils.formatAuroraTimeStampString(context, date, false);
                        // Aurora xuyong 2015-04-23 added for aurora's new feature start
                        mBatchTimestamp = MessageUtils.formatAuroraTimeStampString(date);
                        // Aurora xuyong 2015-04-23 added for aurora's new feature end
                        // Aurora xuyong 2013-10-18 added for bug #47 start
                        mAuroraDate = date;
                        // Aurora xuyong 2013-10-18 added for bug #47 end
                    } else{
                        //mTimestamp = MessageUtils.formatTimeStampString(context, date);
                        mTimestamp = MessageUtils.formatAuroraTimeStampString(context, date, false);
                        // Aurora xuyong 2015-04-23 added for aurora's new feature start
                        mBatchTimestamp = MessageUtils.formatAuroraTimeStampString(date);
                        // Aurora xuyong 2015-04-23 added for aurora's new feature end
                        // Aurora xuyong 2013-10-18 added for bug #47 start
                        mAuroraDate = date;
                        // Aurora xuyong 2013-10-18 added for bug #47 end
                    }
                } else {
                    mTimestamp = "";
                    // Aurora xuyong 2015-04-23 added for aurora's new feature start
                    mBatchTimestamp = "";
                    // Aurora xuyong 2015-04-23 added for aurora's new feature end
                    // Aurora xuyong 2013-10-18 added for bug #47 start
                    mAuroraDate = 0;
                    // Aurora xuyong 2013-10-18 added for bug #47 end
                }
            }
            //gionee gaoj 2012-4-10 added for CR00555790 start
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end

            //gionee gaoj 2012-5-16 remove locked for CRCR00600687 start
            if (!MmsApp.mGnMessageSupport) {
            mLocked = cursor.getInt(columnsMap.mColumnSmsLocked) != 0;
            }
            //gionee gaoj 2012-5-16 remove locked for CRCR00600687 end
            mErrorCode = cursor.getInt(columnsMap.mColumnSmsErrorCode);
            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                mStar = cursor.getInt(columnsMap.mColumnSmsStar) == 1;
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
            // Aurora xuyong 2016-03-05 added for bug #20677 start
            mFolded = cursor.getInt(columnsMap.mColumnSmsFolded);
            // Aurora xuyong 2016-03-05 added for bug #20677 end
            //Gionee <gaoj> <2013-4-11> added for CR00796538 start
            if (!isOutgoingMessage() && MmsApp.mDisplaySendTime) {
                mSendTimestamp = null;
                mSendDate = cursor.getLong(columnsMap.mColumnSmsDateSent);
                if (mSendDate != 0 && (mSendDate + mDelayTime) < mSmsDate) {
                    mSendTimestamp = String.format(context.getString(R.string.gn_send_time),
                            MessageUtils.detailFormatGNTime(context, mSendDate));
                   //mTimestamp = MessageUtils.detailFormatGNTime(context, mSmsDate);
                    mTimestamp = MessageUtils.formatAuroraTimeStampString(context, mSmsDate, false);
                    // Aurora xuyong 2015-04-23 added for aurora's new feature start
                    mBatchTimestamp = MessageUtils.formatAuroraTimeStampString(mSmsDate);
                    // Aurora xuyong 2015-04-23 added for aurora's new feature end
                    // Aurora xuyong 2013-10-18 added for bug #47 start
                    mAuroraDate = mSmsDate;
                    // Aurora xuyong 2013-10-18 added for bug #47 end
                }
            }
            //Gionee <gaoj> <2013-4-11> added for CR00796538 end
        } else if ("mms".equals(type)) {
            mMessageUri = ContentUris.withAppendedId(Mms.CONTENT_URI, mMsgId);
            mBoxId = cursor.getInt(columnsMap.mColumnMmsMessageBox);
            mMessageType = cursor.getInt(columnsMap.mColumnMmsMessageType);
            mErrorType = cursor.getInt(columnsMap.mColumnMmsErrorType);
            String subject = cursor.getString(columnsMap.mColumnMmsSubject);
            if (!TextUtils.isEmpty(subject)) {
                EncodedStringValue v = new EncodedStringValue(
                        cursor.getInt(columnsMap.mColumnMmsSubjectCharset),
                        PduPersister.getBytes(subject));
                mSubject = v.getString();
            }
            //gionee gaoj 2012-5-16 remove locked for CRCR00600687 start
            if (!MmsApp.mGnMessageSupport) {
            mLocked = cursor.getInt(columnsMap.mColumnMmsLocked) != 0;
            }
            //gionee gaoj 2012-5-16 remove locked for CRCR00600687 end

            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                mStar = cursor.getInt(columnsMap.mColumnMmsStar) == 1;
                mSmsDate = cursor.getLong(columnsMap.mColumnMmsDateSent);
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
            // Aurora xuyong 2016-03-05 added for bug #20677 start
            mFolded = cursor.getInt(columnsMap.mColumnMmsFolded);
            // Aurora xuyong 2016-03-05 added for bug #20677 end
            //a0
            //gionee gaoj 2012-8-21 modified for CR00678315 start
            if (MmsApp.mGnRegularlyMsgSend && isOutgoingMessage()) {
                if (mSmsDate > System.currentTimeMillis()) {
                    mIsRegularlyMms = true;
                } else {
                    mIsRegularlyMms = false;
                }
            }
            //gionee gaoj 2012-8-21 modified for CR00678315 end
            //add for gemini
            if (MmsApp.mGnMultiSimMessage == true){
                mSimId = cursor.getInt(columnsMap.mColumnMmsSimId);
            }
            //a1

            long timestamp = 0L;
            PduPersister p = PduPersister.getPduPersister(mContext);
            if (PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND == mMessageType) {
                mDeliveryStatus = DeliveryStatus.NONE;
                NotificationInd notifInd = (NotificationInd) p.load(mMessageUri);
                interpretFrom(notifInd.getFrom(), mMessageUri);
                // Borrow the mBody to hold the URL of the message.
                mBody = new String(notifInd.getContentLocation());
                mMessageSize = (int) notifInd.getMessageSize();
                //gionee gaoj 2012-4-10 added for CR00555790 start
                if (MmsApp.mGnMessageSupport) {
                    timestamp = cursor.getLong(columnsMap.mColumnMmsDate) * 1000L;
                } else {
                    //gionee gaoj 2012-4-10 added for CR00555790 end
                timestamp = notifInd.getExpiry() * 1000L;
                //gionee gaoj 2012-4-10 added for CR00555790 start
                }
                //gionee gaoj 2012-4-10 added for CR00555790 end
            } else {
                MultimediaMessagePdu msg = (MultimediaMessagePdu) p.load(mMessageUri);
                mSlideshow = SlideshowModel.createFromPduBody(context, msg.getBody());
                mAttachmentType = MessageUtils.getAttachmentType(mSlideshow);
                //a0
                mHasDrmContent = false;
                mHasDrmContent = mSlideshow.checkDrmContent();
                //a1

                if (mMessageType == PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF) {
                    //gionee gaoj 2012-4-10 added for CR00555790 start
                    if (MmsApp.mGnMessageSupport) {
                        if (msg instanceof RetrieveConf) {
                        } else {
                            throw new MmsException("not RetrieveConf");
                        }
                    }
                    //gionee gaoj 2012-4-10 added for CR00555790 end
                    RetrieveConf retrieveConf = (RetrieveConf) msg;
                    interpretFrom(retrieveConf.getFrom(), mMessageUri);
                    timestamp = retrieveConf.getDate() * 1000L;
                } else {
                    //gionee gaoj 2012-4-10 added for CR00555790 start
                    if (MmsApp.mGnMessageSupport) {
                        if (msg instanceof SendReq) {
                        } else {
                            throw new MmsException("not SendReq");
                        }
                    }
                    //gionee gaoj 2012-4-10 added for CR00555790 end
                    // Use constant string for outgoing messages
                    mContact = mAddress = context.getString(R.string.messagelist_sender_self);
                    timestamp = ((SendReq) msg).getDate() * 1000L;
                }


                String report = cursor.getString(columnsMap.mColumnMmsDeliveryReport);
                if ((report == null) || !mAddress.equals(context.getString(
                        R.string.messagelist_sender_self))) {
                    mDeliveryStatus = DeliveryStatus.NONE;
                } else {
                    int reportInt;
                    try {
                        reportInt = Integer.parseInt(report);
                        if (reportInt == PduHeaders.VALUE_YES) {
                            mDeliveryStatus = DeliveryStatus.RECEIVED;
                        } else {
                            mDeliveryStatus = DeliveryStatus.NONE;
                        }
                    } catch (NumberFormatException nfe) {
                        Log.e(TAG, "Value for delivery report was invalid.");
                        mDeliveryStatus = DeliveryStatus.NONE;
                    }
                }

                report = cursor.getString(columnsMap.mColumnMmsReadReport);
                if ((report == null) || !mAddress.equals(context.getString(
                        R.string.messagelist_sender_self))) {
                    mReadReport = false;
                } else {
                    int reportInt;
                    try {
                        reportInt = Integer.parseInt(report);
                        mReadReport = (reportInt == PduHeaders.VALUE_YES);
                    } catch (NumberFormatException nfe) {
                        Log.e(TAG, "Value for read report was invalid.");
                        mReadReport = false;
                    }
                }

                SlideModel slide = mSlideshow.get(0);
                if ((slide != null) && slide.hasText()) {
                    TextModel tm = slide.getText();
                    if (tm.isDrmProtected()) {
                        mBody = mContext.getString(R.string.drm_protected_text);
                    } else {
                        mBody = tm.getText();
                    }
                    mTextContentType = tm.getContentType();
                }

                mMessageSize = mSlideshow.getCurrentSlideshowSize();
            }

            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                //mTimestamp = MessageUtils.detailFormatGNTime(context, timestamp);
                mTimestamp = MessageUtils.formatAuroraTimeStampString(context, timestamp, false);
                // Aurora xuyong 2015-04-23 added for aurora's new feature start
                mBatchTimestamp = MessageUtils.formatAuroraTimeStampString(timestamp);
                // Aurora xuyong 2015-04-23 added for aurora's new feature end
                // Aurora xuyong 2013-10-18 added for bug #47 start
                mAuroraDate = timestamp;
                // Aurora xuyong 2013-10-18 added for bug #47 end
            } else {
                //gionee gaoj 2012-4-10 added for CR00555790 end
            if (!isOutgoingMessage()) {
               // mTimestamp = MessageUtils.formatTimeStampString(context, timestamp);
                mTimestamp = MessageUtils.formatAuroraTimeStampString(context, timestamp, false);
                // Aurora xuyong 2015-04-23 added for aurora's new feature start
                mBatchTimestamp = MessageUtils.formatAuroraTimeStampString(timestamp);
                // Aurora xuyong 2015-04-23 added for aurora's new feature end
                // Aurora xuyong 2013-10-18 added for bug #47 start
                mAuroraDate = timestamp;
                // Aurora xuyong 2013-10-18 added for bug #47 end
            }
            //gionee gaoj 2012-4-10 added for CR00555790 start
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
        } else {
            throw new MmsException("Unknown type of the message: " + type);
        }
    }

    private void interpretFrom(EncodedStringValue from, Uri messageUri) {
        if (from != null) {
            mAddress = from.getString();
        } else {
            // In the rare case when getting the "from" address from the pdu fails,
            // (e.g. from == null) fall back to a slower, yet more reliable method of
            // getting the address from the "addr" table. This is what the Messaging
            // notification system uses.
            mAddress = AddressUtils.getFrom(mContext, messageUri);
        }
        //gionee gaoj 2012-9-20 added for CR00699291 start
        mContact = TextUtils.isEmpty(mAddress) ? mContext.getString(android.R.string.unknownName) : "";// Contact.get(mAddress, false).getName();
        //gionee gaoj 2012-9-20 added for CR00699291 end
    }
    // Aurora xuyong 2013-12-11 modified for aurora's new feature start
    public GroupItemInfoModel getGroupItemInfoModel() {
        return     mGIIF;
    }
    // Aurora xuyong 2013-12-11 modified for aurora's new feature end
    
    public boolean isMms() {
        return mType.equals("mms");
    }

    public boolean isSms() {
        return mType.equals("sms");
    }

    //gionee gaoj 2012-8-14 added for CR00623375 start
    public boolean isRegularlyMms() {
        return mIsRegularlyMms;
    }
    //gionee gaoj 2012-8-14 added for CR00623375 end

    public boolean isDownloaded() {
        return (mMessageType != PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND);
    }

    public boolean isOutgoingMessage() {
        boolean isOutgoingMms = isMms() && (mBoxId == Mms.MESSAGE_BOX_OUTBOX);
        boolean isOutgoingSms = isSms()
                                    && ((mBoxId == Sms.MESSAGE_TYPE_FAILED)
                                            || (mBoxId == Sms.MESSAGE_TYPE_OUTBOX)
                                            || (mBoxId == Sms.MESSAGE_TYPE_QUEUED));
        return isOutgoingMms || isOutgoingSms;
    }

    public boolean isSending() {
        return !isFailedMessage() && isOutgoingMessage();
    }

    public boolean isFailedMessage() {
        boolean isFailedMms = isMms()
                            && (mErrorType >= Telephony.MmsSms.ERR_TYPE_GENERIC_PERMANENT);
        boolean isFailedSms = isSms()
                            && (mBoxId == Sms.MESSAGE_TYPE_FAILED);
        return isFailedMms || isFailedSms;
    }

    // Note: This is the only mutable field in this class.  Think of
    // mCachedFormattedMessage as a C++ 'mutable' field on a const
    // object, with this being a lazy accessor whose logic to set it
    // is outside the class for model/view separation reasons.  In any
    // case, please keep this class conceptually immutable.
    public void setCachedFormattedMessage(CharSequence formattedMessage) {
        mCachedFormattedMessage = formattedMessage;
    }

    public CharSequence getCachedFormattedMessage() {
        boolean isSending = isSending();
        if (isSending != mLastSendingState) {
            mLastSendingState = isSending;
            mCachedFormattedMessage = null;         // clear cache so we'll rebuild the message
                                                    // to show "Sending..." or the sent date.
        }
        return mCachedFormattedMessage;
    }
    
    public void setIdentifyNumber(String identifyNumber) {
        mIdentifyNumber = identifyNumber;
    }
    
    public String getIdentifyNumber() {
        return mIdentifyNumber;
    }

    public int getBoxId() {
        return mBoxId;
    }

    @Override
    public String toString() {
        //add for gemini
        if (FeatureOption.MTK_GEMINI_SUPPORT == true){
            return "type: " + mType +
                " box: " + mBoxId +
                " sim: " + mSimId +
                " uri: " + mMessageUri +
                " address: " + mAddress +
                " contact: " + mContact +
                " read: " + mReadReport +
                " delivery status: " + mDeliveryStatus;
        } else {
            return "type: " + mType +
                " box: " + mBoxId +
                " uri: " + mMessageUri +
                " address: " + mAddress +
                " contact: " + mContact +
                " read: " + mReadReport +
                " delivery status: " + mDeliveryStatus;
        }
    }
    
    //a0
    boolean mSimMsg = false;
    //add for gemini
    int mSimId;
    CharSequence mCachedFormattedTimestamp;
    CharSequence mCachedFormattedSimStatus;
    
    long mSmsDate = 0;
    String mServiceCenter = null;
    
    private boolean mItemSelected = false;
    private boolean mHasDrmContent = false;
    
    public boolean hasDrmContent() {
        return mHasDrmContent;
    }

    public boolean isSimMsg() {
        return mSimMsg;
    }
    
    public boolean isReceivedMessage() {
        boolean isReceivedMms = isMms() && (mBoxId == Mms.MESSAGE_BOX_INBOX);
        boolean isReceivedSms = isSms() && (mBoxId == Sms.MESSAGE_TYPE_INBOX);
        /*(mBoxId == 0 && isSms()) means it's a SIM SMS*/
        return isReceivedMms || isReceivedSms || (mBoxId == 0 && isSms());
    }

    public boolean isSentMessage() {
        boolean isSentMms = isMms() && (mBoxId == Mms.MESSAGE_BOX_SENT);
        boolean isSentSms = isSms() && (mBoxId == Sms.MESSAGE_TYPE_SENT);
        return isSentMms || isSentSms;
    }
    
    public void setCachedFormattedTimestamp(CharSequence formattedTimestamp) {
        mCachedFormattedTimestamp = formattedTimestamp;
    }

    public CharSequence getCachedFormattedTimestamp() {
        boolean isSending = isSending();
        if (isSending != mLastSendingState) {
            mLastSendingState = isSending;
            mCachedFormattedTimestamp = null;
        }
        return mCachedFormattedTimestamp;
    }

    public void setCachedFormattedSimStatus(CharSequence formattedSimStatus) {
        mCachedFormattedSimStatus = formattedSimStatus;
    }

    public CharSequence getCachedFormattedSimStatus() {
        boolean isSending = isSending();
        if (isSending != mLastSendingState) {
            mLastSendingState = isSending;
            mCachedFormattedSimStatus = null;
        }
        return mCachedFormattedSimStatus;
    }
    //Aurora xuyong 2013-10-11 added for aurora's new feature start
    // Aurora xuyong 2013-03-05 modified for aurora's new feature start
    public synchronized Uri getMessageUri() {
        return mMessageUri;
    }
    // Aurora xuyong 2013-03-05 modified for aurora's new feature end
    
    public int getMessageType() {
        return mMessageType;    
    }
    //Aurora xuyong 2013-10-11 added for aurora's new feature end
    //add for gemini
    public int getSimId() {
        return mSimId;
    }

    public boolean isSelected() {
        return mItemSelected;
    }
    
    public void setSelectedState(boolean isSelected) {
        mItemSelected = isSelected;
    }
    
    // Add for vCard begin
    public int getFileAttachmentCount() {
        if (mSlideshow != null) {
            return mSlideshow.sizeOfFilesAttach();
        }
        return 0;
    }
    // Add for vCard end

    public String getServiceCenter() {
        return mServiceCenter;
    }
    
    private int getTimestampStrId() {
        if (PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND == mMessageType) {
            return R.string.expire_on;
        } else {
            if (isReceivedMessage()){
                return R.string.received_on;
            }else{
                return R.string.sent_on;
            }
        }
    }
    //a1
    //gionee gaoj 2012-4-10 added for CR00555790 start
    public long getMsgStatus () {
        return mMsgStatus;
    }
    //gionee gaoj 2012-4-10 added for CR00555790 end

    // Aurora xuyong 2016-01-28 added for xy-smartsms start
    private HashMap mSmartSmsExtendMap =null;
    @Override
    public long getMsgId() {
        // TODO Auto-generated method stub
        return mMsgId;
    }

    @Override
    public HashMap getSmartSmsExtendMap() {
        // TODO Auto-generated method stub
        if(mSmartSmsExtendMap == null){
            mSmartSmsExtendMap = new HashMap<String, Object>();
            mSmartSmsExtendMap.put("simIndex", "-1");//卡位
        }
        return mSmartSmsExtendMap;
    }

    @Override
    public String getPhoneNum() {
        // TODO Auto-generated method stub
        return mAddress;
    }

    @Override
    public String getServiceCenterNum() {
        // TODO Auto-generated method stub
        //短信中心号码
        return null;
    }

    @Override
    public long getSmsReceiveTime() {
        // TODO Auto-generated method stub
        return mSmsReceiveTime;
    }
    /* hewengao/xiaoyuan 20150824 end>*/

    @Override
    public String getSmsBody() {
        // TODO Auto-generated method stub
        return mBody;
    }
    // Aurora xuyong 2016-01-28 added for xy-smartsms end
}
