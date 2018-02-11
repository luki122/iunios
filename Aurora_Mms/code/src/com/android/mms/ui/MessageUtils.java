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

import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.LogTag;
import com.android.mms.data.Contact;
import com.android.mms.TempFileProvider;
import com.android.mms.data.WorkingMessage;
import com.android.mms.model.MediaModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.transaction.MmsMessageSender;
import com.android.mms.util.AddressUtils;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.ContentType;
import com.aurora.android.mms.MmsException;
import com.aurora.android.mms.pdu.CharacterSets;
import com.aurora.android.mms.pdu.EncodedStringValue;
import com.aurora.android.mms.pdu.MultimediaMessagePdu;
import com.aurora.android.mms.pdu.NotificationInd;
import com.aurora.android.mms.pdu.PduBody;
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.android.mms.pdu.PduPart;
import com.aurora.android.mms.pdu.PduPersister;
import com.aurora.android.mms.pdu.RetrieveConf;
import com.aurora.android.mms.pdu.SendReq;
//Aurora xuyong 2013-11-15 modified for google adapt end
import android.database.sqlite.SqliteWrapper;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.location.Country;
import android.location.CountryDetector;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
//gionee gaoj 2012-8-7 added for CR00671408 start
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
//gionee gaoj 2012-8-7 added for CR00671408 end
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.CamcorderProfile;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.os.Handler;
import aurora.preference.AuroraPreferenceManager;
import android.provider.MediaStore;
import gionee.provider.GnTelephony.Mms;
import gionee.provider.GnTelephony.Sms;
import android.telephony.SmsMessage;
import com.android.mms.util.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.text.style.URLSpan;
import android.util.Log;
import android.widget.Toast;
import android.provider.Settings;

import java.io.FileNotFoundException;
// Aurora xuyong 2015-07-29 added for bug #14494 start
import java.io.FileOutputStream;
// Aurora xuyong 2015-07-29 added for bug #14494 end
import java.io.InputStream;
import java.io.IOException;
// Aurora xuyong 2015-07-29 added for bug #14494 start
import java.io.OutputStream;
// Aurora xuyong 2015-07-29 added for bug #14494 end
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//a0
import com.android.internal.telephony.ITelephony;
import android.database.sqlite.SQLiteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import gionee.provider.GnTelephony.WapPush;
import android.content.ContentResolver;
import android.os.RemoteException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Bitmap.CompressFormat;
import android.os.ServiceManager;
import android.os.StatFs;
import android.provider.BaseColumns;
import gionee.telephony.GnTelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
//import android.text.style.BackgroundImageSpan;
import com.gionee.mms.adaptor.GnBackgroundImageSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.graphics.drawable.Drawable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Locale;
import com.aurora.featureoption.FeatureOption;
// Aurora xuyong 2014-07-19 added for sougou start
import com.aurora.mms.util.Utils;
// Aurora xuyong 2014-07-19 added for sougou start
import com.gionee.internal.telephony.GnTelephonyManagerEx;
import android.drm.DrmManagerClient;
import com.gionee.internal.telephony.GnPhone;
//a1
//add for cmcc dir ui mode
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.WapPushMessagingNotification;
import com.android.mms.transaction.CBMessagingNotification;

//gionee gaoj 2012-3-22 added for CR00555790 start
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.content.res.Configuration;
//import com.gionee.aora.numarea.export.NumAreaInfo;
import com.gionee.mms.ui.TabActivity;
import com.android.vcard.VCardConfig;
import com.android.vcard.VCardEntry;
import com.android.vcard.VCardEntryConstructor;
import com.android.vcard.VCardEntryCounter;
import com.android.vcard.VCardInterpreter;
import com.android.vcard.VCardParser;
import com.android.vcard.VCardParser_V21;
import com.android.vcard.VCardParser_V30;
import com.android.vcard.VCardSourceDetector;
import com.android.vcard.exception.VCardException;
import com.android.vcard.exception.VCardNestedException;
import com.android.vcard.exception.VCardNotSupportedException;
import com.android.vcard.exception.VCardVersionException;
import android.graphics.BitmapFactory;
import android.widget.TextView;
//gionee gaoj 2012-3-22 added for CR00555790  end
//gionee zhouyj 2012-05-14 add for CR00585826 start
import com.gionee.mms.ui.SlidesBrowserActivity;
//gionee zhouyj 2012-05-14 add for CR00585826 end
// Gionee lihuafang 2012-07-24 add for CR00651588 begin
import gionee.provider.GnTelephony.SIMInfo;
// Gionee lihuafang 2012-07-24 add for CR00651588 end
import gionee.drm.GnDrmStore.DrmExtra;
import com.gionee.internal.telephony.GnITelephony;
//Gionee qinkai 2012-09-05 added for CR00679226 start
import android.os.SystemProperties;
//Gionee qinkai 2012-09-05 added for CR00679226 end
//Gionee <zhouyj> <2013-04-25> add for CR00802357 start
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
//Gionee <zhouyj> <2013-04-25> add for CR00802357 end

// gionee lwzh modify for CR00774362 20130227  

/**
 * An utility class for managing messages.
 */
public class MessageUtils {
    interface ResizeImageResultCallback {
        void onResizeResult(PduPart part, boolean append);
    }

    private static final String TAG = LogTag.TAG;
    private static String sLocalNumber;
    private static Thread mRemoveOldMmsThread;
    public static final String SDCARD_1 = "/mnt/sdcard";
    public static final String SDCARD_2 = "/mnt/sdcard2";
    //Gionee <zhouyj> <2013-05-15> add for CR00810588 begin
    public static final String VOICEHELPER_SERVICE_STOP = "com.android.mms.voicehelper_service_stop";
    //Gionee <zhouyj> <2013-05-15> add for CR00810588 end

    // Gionee: 20121107 xuyongji add for CR00725775 begin
    private static final boolean gnCommonAPM = SystemProperties.get("ro.gn.oversea.custom").equals("SOUTH_AMERICA_BLU");
    // Gionee: 20121107 xuyongji add for CR00725775 end

    // Cache of both groups of space-separated ids to their full
    // comma-separated display names, as well as individual ids to
    // display names.
    // TODO: is it possible for canonical address ID keys to be
    // re-used?  SQLite does reuse IDs on NULL id_ insert, but does
    // anything ever delete from the mmssms.db canonical_addresses
    // table?  Nothing that I could find.
    private static final Map<String, String> sRecipientAddress =
            new ConcurrentHashMap<String, String>(20 /* initial capacity */);


    /**
     * MMS address parsing data structures
     */
    // allowable phone number separators
    private static final char[] NUMERIC_CHARS_SUGAR = {
        '-', '.', ',', '(', ')', ' ', '/', '\\', '*', '#', '+'
    };

    private static HashMap numericSugarMap = new HashMap (NUMERIC_CHARS_SUGAR.length);

    static {
        for (int i = 0; i < NUMERIC_CHARS_SUGAR.length; i++) {
            numericSugarMap.put(NUMERIC_CHARS_SUGAR[i], NUMERIC_CHARS_SUGAR[i]);
        }
    }


    private MessageUtils() {
        // Forbidden being instantiated.
    }

    //m0
    /*public static String getMessageDetails(Context context, Cursor cursor, int size) {
        if (cursor == null) {
            return null;
        }

        if ("mms".equals(cursor.getString(MessageListAdapter.COLUMN_MSG_TYPE))) {
            int type = cursor.getInt(MessageListAdapter.COLUMN_MMS_MESSAGE_TYPE);
            switch (type) {
                case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
                    return getNotificationIndDetails(context, cursor);
                case PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF:
                case PduHeaders.MESSAGE_TYPE_SEND_REQ:
                    return getMultimediaMessageDetails(context, cursor, size);
                default:
                    Log.w(TAG, "No details could be retrieved.");
                    return "";
            }
        } else {
            return getTextMessageDetails(context, cursor);
        }
    }*/
    public static String getMessageDetails(Context context, MessageItem msgItem) {
        if (msgItem == null) {
            return null;
        }

        if ("mms".equals(msgItem.mType)) {
            int type = msgItem.mMessageType;
            switch (type) {
                case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
                    return getNotificationIndDetails(context, msgItem);
                case PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF:
                case PduHeaders.MESSAGE_TYPE_SEND_REQ:
                    return getMultimediaMessageDetails(context, msgItem);
                default:
                    Log.w(TAG, "No details could be retrieved.");
                    return "";
            }
        } else {
            return getTextMessageDetails(context, msgItem);
        }
    }
    //m1
 
    //m0
    /*private static String getNotificationIndDetails(Context context, Cursor cursor) {
        StringBuilder details = new StringBuilder();
        Resources res = context.getResources();

        long id = cursor.getLong(MessageListAdapter.COLUMN_ID);
        Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, id);
        NotificationInd nInd;

        try {
            nInd = (NotificationInd) PduPersister.getPduPersister(
                    context).load(uri);
        } catch (MmsException e) {
            Log.e(TAG, "Failed to load the message: " + uri, e);
            return context.getResources().getString(R.string.cannot_get_details);
        }

        // Message Type: Mms Notification.
        details.append(res.getString(R.string.message_type_label));
        details.append(res.getString(R.string.multimedia_notification));

        // From: ***
        String from = extractEncStr(context, nInd.getFrom());
        details.append('\n');
        details.append(res.getString(R.string.from_label));
        details.append(!TextUtils.isEmpty(from)? from:
                                 res.getString(R.string.hidden_sender_address));

        // Date: ***
        details.append('\n');
        details.append(res.getString(
                                R.string.expire_on,
                                MessageUtils.formatTimeStampString(
                                        context, nInd.getExpiry() * 1000L, true)));

        // Subject: ***
        details.append('\n');
        details.append(res.getString(R.string.subject_label));

        EncodedStringValue subject = nInd.getSubject();
        if (subject != null) {
            details.append(subject.getString());
        }

        // Message class: Personal/Advertisement/Infomational/Auto
        details.append('\n');
        details.append(res.getString(R.string.message_class_label));
        details.append(new String(nInd.getMessageClass()));

        // Message size: *** KB
        details.append('\n');
        details.append(res.getString(R.string.message_size_label));
        details.append(String.valueOf((nInd.getMessageSize() + 1023) / 1024));
        details.append(context.getString(R.string.kilobyte));

        return details.toString();
    }*/
    private static String getNotificationIndDetails(Context context, MessageItem msgItem) {
        StringBuilder details = new StringBuilder();
        Resources res = context.getResources();

        Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, msgItem.mMsgId);
        NotificationInd nInd;

        try {
            nInd = (NotificationInd) PduPersister.getPduPersister(
                    context).load(uri);
        } catch (MmsException e) {
            Log.e(TAG, "Failed to load the message: " + uri, e);
            return context.getResources().getString(R.string.cannot_get_details);
        }

        // Message Type: Mms Notification.
        details.append(res.getString(R.string.message_type_label));
        details.append(res.getString(R.string.multimedia_notification));

        // sc
        //gionee gaoj 2012-6-18 added for CR00625734 start
        if (MmsApp.mGnMessageSupport) {
            if (!TextUtils.isEmpty(msgItem.mServiceCenter)) {
                details.append('\n');
                details.append(res.getString(R.string.service_center_label));
                details.append(msgItem.mServiceCenter);
            }
        } else {
        //gionee gaoj 2012-6-18 added for CR00625734 end
        details.append('\n');
        details.append(res.getString(R.string.service_center_label));
        details.append(!TextUtils.isEmpty(msgItem.mServiceCenter)? msgItem.mServiceCenter: "");
        //gionee gaoj 2012-6-18 added for CR00625734 start
        }
        //gionee gaoj 2012-6-18 added for CR00625734 end

        // From: ***
        String from = extractEncStr(context, nInd.getFrom());
        details.append('\n');
        details.append(res.getString(R.string.from_label));
        details.append(!TextUtils.isEmpty(from)? from:
                                 res.getString(R.string.hidden_sender_address));

        // Date: ***
        details.append('\n');
        details.append(res.getString(
                R.string.expire_on, MessageUtils.formatTimeStampString(
                        context, nInd.getExpiry() * 1000L, true)));

        // Subject: ***
        details.append('\n');
        details.append(res.getString(R.string.subject_label));

        EncodedStringValue subject = nInd.getSubject();
        if (subject != null) {
            details.append(subject.getString());
        }

        // Message class: Personal/Advertisement/Infomational/Auto
        details.append('\n');
        details.append(res.getString(R.string.message_class_label));
        details.append(new String(nInd.getMessageClass()));

        // Message size: *** KB
        details.append('\n');
        details.append(res.getString(R.string.message_size_label));
        details.append(String.valueOf((nInd.getMessageSize() + 1023) / 1024));
        details.append(context.getString(R.string.kilobyte));

        return details.toString();
    }
    //m1

    //m0
    /*private static String getMultimediaMessageDetails(
            Context context, Cursor cursor, int size) {
        int type = cursor.getInt(MessageListAdapter.COLUMN_MMS_MESSAGE_TYPE);
        if (type == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) {
            return getNotificationIndDetails(context, cursor);
        }

        StringBuilder details = new StringBuilder();
        Resources res = context.getResources();

        long id = cursor.getLong(MessageListAdapter.COLUMN_ID);
        Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, id);
        MultimediaMessagePdu msg;

        try {
            msg = (MultimediaMessagePdu) PduPersister.getPduPersister(
                    context).load(uri);
        } catch (MmsException e) {
            Log.e(TAG, "Failed to load the message: " + uri, e);
            return context.getResources().getString(R.string.cannot_get_details);
        }

        // Message Type: Text message.
        details.append(res.getString(R.string.message_type_label));
        details.append(res.getString(R.string.multimedia_message));

        if (msg instanceof RetrieveConf) {
            // From: ***
            String from = extractEncStr(context, ((RetrieveConf) msg).getFrom());
            details.append('\n');
            details.append(res.getString(R.string.from_label));
            details.append(!TextUtils.isEmpty(from)? from:
                                  res.getString(R.string.hidden_sender_address));
        }

        // To: ***
        details.append('\n');
        details.append(res.getString(R.string.to_address_label));
        EncodedStringValue[] to = msg.getTo();
        if (to != null) {
            details.append(EncodedStringValue.concat(to));
        }
        else {
            Log.w(TAG, "recipient list is empty!");
        }


        // Bcc: ***
        if (msg instanceof SendReq) {
            EncodedStringValue[] values = ((SendReq) msg).getBcc();
            if ((values != null) && (values.length > 0)) {
                details.append('\n');
                details.append(res.getString(R.string.bcc_label));
                details.append(EncodedStringValue.concat(values));
            }
        }

        // Date: ***
        details.append('\n');
        int msgBox = cursor.getInt(MessageListAdapter.COLUMN_MMS_MESSAGE_BOX);
        if (msgBox == Mms.MESSAGE_BOX_DRAFTS) {
            details.append(res.getString(R.string.saved_label));
        } else if (msgBox == Mms.MESSAGE_BOX_INBOX) {
            details.append(res.getString(R.string.received_label));
        } else {
            details.append(res.getString(R.string.sent_label));
        }

        details.append(MessageUtils.formatTimeStampString(
                context, msg.getDate() * 1000L, true));

        // Subject: ***
        details.append('\n');
        details.append(res.getString(R.string.subject_label));

        EncodedStringValue subject = msg.getSubject();
        if (subject != null) {
            String subStr = subject.getString();
            // Message size should include size of subject.
            size += subStr.length();
            details.append(subStr);
        }

        // Priority: High/Normal/Low
        details.append('\n');
        details.append(res.getString(R.string.priority_label));
        details.append(getPriorityDescription(context, msg.getPriority()));

        // Message size: *** KB
        details.append('\n');
        details.append(res.getString(R.string.message_size_label));
        details.append((size - 1)/1000 + 1);
        details.append(" KB");

        return details.toString();
    }*/
    private static String getMultimediaMessageDetails(Context context, MessageItem msgItem) {
        if (msgItem.mMessageType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) {
            return getNotificationIndDetails(context, msgItem);
        }

        StringBuilder details = new StringBuilder();
        Resources res = context.getResources();

        Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, msgItem.mMsgId);
        MultimediaMessagePdu msg;

        try {
            msg = (MultimediaMessagePdu) PduPersister.getPduPersister(
                    context).load(uri);
        } catch (MmsException e) {
            Log.e(TAG, "Failed to load the message: " + uri, e);
            return context.getResources().getString(R.string.cannot_get_details);
        }

        // Message Type: Text message.
        details.append(res.getString(R.string.message_type_label));
        details.append(res.getString(R.string.multimedia_message));

        if (msg instanceof RetrieveConf) {
            // From: ***
            String from = extractEncStr(context, ((RetrieveConf) msg).getFrom());
            details.append('\n');
            details.append(res.getString(R.string.from_label));
            details.append(!TextUtils.isEmpty(from)? from:
                                  res.getString(R.string.hidden_sender_address));
        }

        // To: ***
        details.append('\n');
        details.append(res.getString(R.string.to_address_label));
        EncodedStringValue[] to = msg.getTo();
        if (to != null) {
            details.append(EncodedStringValue.concat(to));
        }
        else {
            Log.w(TAG, "recipient list is empty!");
        }


        // Bcc: ***
        if (msg instanceof SendReq) {
            EncodedStringValue[] values = ((SendReq) msg).getBcc();
            if ((values != null) && (values.length > 0)) {
                details.append('\n');
                details.append(res.getString(R.string.bcc_label));
                details.append(EncodedStringValue.concat(values));
            }
        }

        //Gionee <gaoj> <2013-4-11> added for CR00796538 start
        if (MmsApp.mDisplaySendTime && null != msgItem.mSendTimestamp) {
            details.append('\n');
            details.append(res.getString(R.string.gn_sent_label));
            details.append(MessageUtils.formatTimeStampString(context, msgItem.mSendDate, true));
        }
        //Gionee <gaoj> <2013-4-11> added for CR00796538 end

        // Date: ***
        details.append('\n');
        int msgBox = msgItem.mBoxId;
        if (msgBox == Mms.MESSAGE_BOX_DRAFTS) {
            details.append(res.getString(R.string.saved_label));
        } else if (msgBox == Mms.MESSAGE_BOX_INBOX) {
            details.append(res.getString(R.string.received_label));
        } else {
            details.append(res.getString(R.string.sent_label));
        }

        details.append(MessageUtils.formatTimeStampString(
                context, msg.getDate() * 1000L, true));

        // Subject: ***
        details.append('\n');
        details.append(res.getString(R.string.subject_label));

        int size = msgItem.mMessageSize;
        EncodedStringValue subject = msg.getSubject();
        if (subject != null) {
            String subStr = subject.getString();
            // Message size should include size of subject.
            size += subStr.length();
            details.append(subStr);
        }

        // Priority: High/Normal/Low
        details.append('\n');
        details.append(res.getString(R.string.priority_label));
        details.append(getPriorityDescription(context, msg.getPriority()));

        // Message size: *** KB
        details.append('\n');
        details.append(res.getString(R.string.message_size_label));
        details.append((size - 1)/1024 + 1);
        details.append(res.getString(R.string.kilobyte));

        return details.toString();
    }
    //m1

    //m0
    /*private static String getTextMessageDetails(Context context, Cursor cursor) {
        Log.d(TAG, "getTextMessageDetails");

        StringBuilder details = new StringBuilder();
        Resources res = context.getResources();

        // Message Type: Text message.
        details.append(res.getString(R.string.message_type_label));
        details.append(res.getString(R.string.text_message));

        // Address: ***
        details.append('\n');
        int smsType = cursor.getInt(MessageListAdapter.COLUMN_SMS_TYPE);
        if (Sms.isOutgoingFolder(smsType)) {
            details.append(res.getString(R.string.to_address_label));
        } else {
            details.append(res.getString(R.string.from_label));
        }
        details.append(cursor.getString(MessageListAdapter.COLUMN_SMS_ADDRESS));

        // Sent: ***
        if (smsType == Sms.MESSAGE_TYPE_INBOX) {
            long date_sent = cursor.getLong(MessageListAdapter.COLUMN_SMS_DATE_SENT);
            if (date_sent > 0) {
                details.append('\n');
                details.append(res.getString(R.string.sent_label));
                details.append(MessageUtils.formatTimeStampString(context, date_sent, true));
            }
        }

        // Received: ***
        details.append('\n');
        if (smsType == Sms.MESSAGE_TYPE_DRAFT) {
            details.append(res.getString(R.string.saved_label));
        } else if (smsType == Sms.MESSAGE_TYPE_INBOX) {
            details.append(res.getString(R.string.received_label));
        } else {
            details.append(res.getString(R.string.sent_label));
        }

        long date = cursor.getLong(MessageListAdapter.COLUMN_SMS_DATE);
        details.append(MessageUtils.formatTimeStampString(context, date, true));

        // Error code: ***
        int errorCode = cursor.getInt(MessageListAdapter.COLUMN_SMS_ERROR_CODE);
        if (errorCode != 0) {
            details.append('\n')
                .append(res.getString(R.string.error_code_label))
                .append(errorCode);
        }

        return details.toString();
    }*/
    private static String getTextMessageDetails(Context context, MessageItem msgItem) {
        StringBuilder details = new StringBuilder();
        Resources res = context.getResources();

        // Message Type: Text message.
        details.append(res.getString(R.string.message_type_label));
        details.append(res.getString(R.string.text_message));

        // Address: ***
        details.append('\n');
        int smsType = msgItem.mBoxId;
        if (Sms.isOutgoingFolder(smsType)) {
            details.append(res.getString(R.string.to_address_label));
        } else {
            details.append(res.getString(R.string.from_label));
        }
        details.append(msgItem.mAddress);

        //Gionee <gaoj> <2013-4-11> added for CR00796538 start
        if (MmsApp.mDisplaySendTime && null != msgItem.mSendTimestamp) {
            details.append('\n');
            details.append(res.getString(R.string.gn_sent_label));
            details.append(MessageUtils.formatTimeStampString(context, msgItem.mSendDate, true));
        }
        //Gionee <gaoj> <2013-4-11> added for CR00796538 end

        // Date: ***
        if (msgItem.mSmsDate > 0l) {
            details.append('\n');
            if (smsType == Sms.MESSAGE_TYPE_DRAFT) {
                details.append(res.getString(R.string.saved_label));
            } else if (smsType == Sms.MESSAGE_TYPE_INBOX) {
                details.append(res.getString(R.string.received_label));
            } else {
                details.append(res.getString(R.string.sent_label));
            }
            details.append(MessageUtils.formatTimeStampString(context, msgItem.mSmsDate, true));
        }
        
        // Message Center: ***
        if (smsType == Sms.MESSAGE_TYPE_INBOX) {
            //gionee gaoj 2012-6-18 added for CR00625734 start
            if (MmsApp.mGnMessageSupport) {
                if (!TextUtils.isEmpty(msgItem.mServiceCenter)) {
                    details.append('\n');
                    details.append(res.getString(R.string.service_center_label));
                    details.append(msgItem.mServiceCenter);
                }
            } else {
            //gionee gaoj 2012-6-18 added for CR00625734 end
            details.append('\n');
            details.append(res.getString(R.string.service_center_label));
            details.append(msgItem.mServiceCenter);
            //gionee gaoj 2012-6-18 added for CR00625734 start
            }
            //gionee gaoj 2012-6-18 added for CR00625734 end
        }

        // Error code: ***
        int errorCode = msgItem.mErrorCode;
        if (errorCode != 0) {
            details.append('\n')
                .append(res.getString(R.string.error_code_label))
                .append(errorCode);
        }

        return details.toString();
    }
    //m1

    static private String getPriorityDescription(Context context, int PriorityValue) {
        Resources res = context.getResources();
        switch(PriorityValue) {
            case PduHeaders.PRIORITY_HIGH:
                return res.getString(R.string.priority_high);
            case PduHeaders.PRIORITY_LOW:
                return res.getString(R.string.priority_low);
            case PduHeaders.PRIORITY_NORMAL:
            default:
                return res.getString(R.string.priority_normal);
        }
    }

    public static int getAttachmentType(SlideshowModel model) {
        if (model == null) {
            return WorkingMessage.TEXT;
        }

        int numberOfSlides = model.size();
        if (numberOfSlides > 1) {
            return WorkingMessage.SLIDESHOW;
        } else if (numberOfSlides == 1) {
            // Only one slide in the slide-show.
            SlideModel slide = model.get(0);
            if (slide.hasVideo()) {
                return WorkingMessage.VIDEO;
            }

            if (slide.hasAudio() && slide.hasImage()) {
                return WorkingMessage.SLIDESHOW;
            }

            if (slide.hasAudio()) {
                return WorkingMessage.AUDIO;
            }

            if (slide.hasImage()) {
                return WorkingMessage.IMAGE;
            }
            
            // add for vcard
            if (model.sizeOfFilesAttach() > 0) {
                return WorkingMessage.ATTACHMENT;
            }

            if (slide.hasText()) {
                return WorkingMessage.TEXT;
            }
        }

        if (model.sizeOfFilesAttach() > 0) {
            return WorkingMessage.ATTACHMENT;
        }

        return WorkingMessage.TEXT;
    }

    //gionee gaoj 2012-3-22 added for CR00555790 start
    public static String formatGNTime(Context context, long l) {
        Time then = new Time();
        then.set(l);
        Time now = new Time();
        now.setToNow();
        
        int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT | 
                           DateUtils.FORMAT_ABBREV_ALL | 
                           DateUtils.FORMAT_CAP_AMPM;

        String sRet = null;
        // If the message is from a different year, show the date and year.
        if (then.year != now.year) {
            sRet = DateFormat.format("yyyy-MM-dd", l).toString();
        } else {
            format_flags |= DateUtils.FORMAT_SHOW_TIME;
            if (then.yearDay == now.yearDay) {
                // the message is from today
                sRet = DateUtils.formatDateTime(context, l, format_flags);
            } else if ((then.yearDay + 1) == now.yearDay) {
                // the message is from yesterday, show the time.
                sRet = context.getResources().getString(R.string.gn_yesterday)
                        + DateUtils.formatDateTime(context, l, format_flags);
            } else {
                // the message is from the day before yesterday, show the time.
                sRet = DateFormat.format("MM-dd", l).toString();
            }
        }
        return sRet;
    }
    //gionee gaoj 2012-3-22 added for CR00555790 end
    
    public static String formatTimeStampString(Context context, long when) {
        return formatTimeStampString(context, when, false);
    }

    //gionee gaoj 2012-3-22 added for CR00555790 start
    public static String formatGnStampString(Context context, long l) {
        Time then = new Time();
        then.set(l);
        Time now = new Time();
        now.setToNow();

        int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL
                | DateUtils.FORMAT_CAP_AMPM;
        String sRet = null;
        format_flags |= DateUtils.FORMAT_SHOW_TIME;
        DateUtils.formatDateTime(context, l, format_flags);
        return DateUtils.formatDateTime(context, l, format_flags);
    }
    //gionee gaoj 2012-3-22 added for CR00555790 end
    // Aurora xuyong 2015-04-23 added for aurora's new feature start
    public static String formatAuroraTimeStampString(long when) {
        return DateFormat.format("kk:mm", when).toString();
    }
    // Aurora xuyong 2015-04-23 added for aurora's new feature end
    //Aurora xuyong 2013-09-20 added for aurora's new feature start
    public static String formatAuroraTimeStampString(Context context, long when, boolean isConv) {
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();
        String sRet;
                
        if (then.year != now.year) {
            if (isConv) {
                sRet = DateFormat.format("yyyy-MM-dd", when).toString();
            } else {
                sRet = DateFormat.format("yyyy-MM-dd  kk:mm", when).toString();
            }
        } else {            
            if (then.yearDay == now.yearDay) {
                //Aurora liugj 2013-11-26 modified for aurora's new feature start
                int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT |
                        DateUtils.FORMAT_ABBREV_ALL |
                        DateUtils.FORMAT_CAP_AMPM;
                format_flags |= DateUtils.FORMAT_SHOW_TIME;
                    //Aurora liugj 2013-11-26 modified for aurora's new feature end
                sRet = DateUtils.formatDateTime(context, when, format_flags);
            } else {
                if (isConv) {
                    sRet = DateFormat.format("MM-dd", when).toString();
                } else {
                    // Aurora xuyong 2013-09-24 added for aurora;s new feature start
                    sRet = DateFormat.format("yyyy-MM-dd  kk:mm", when).toString();
                    // Aurora xuyong 2013-09-24 added for aurora;s new feature end
                }
            }
        }
        return sRet;
    }
    //Aurora xuyong 2013-09-20 added for aurora's new feature end
    public static String formatTimeStampString(Context context, long when, boolean fullFormat) {
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();

        // Basic settings for formatDateTime() we want for all cases.
        int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT |
                           DateUtils.FORMAT_ABBREV_ALL |
                           DateUtils.FORMAT_CAP_AMPM;

        // If the message is from a different year, show the date and year.
        if (then.year != now.year) {
            format_flags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
        } else if (then.yearDay != now.yearDay) {
            // If it is from a different day than today, show only the date.
            //Gionee <gaoj> <2013-05-13> modified for CR00811367 begin
            /*format_flags |= DateUtils.FORMAT_SHOW_DATE;*/
            if ((now.yearDay - then.yearDay) == 1 && MmsApp.mGnPerfList) {
                return context.getString(R.string.str_ipmsg_yesterday);
            } else {
                format_flags |= DateUtils.FORMAT_SHOW_DATE;
            }
        } else if ((now.toMillis(false) - then.toMillis(false)) < 60000 && now.toMillis(false) > then.toMillis(false) && MmsApp.mGnPerfList) {
            return context.getString(R.string.time_now);
            //Gionee <gaoj> <2013-05-13> modified for CR00811367 end
        } else {
            // Otherwise, if the message is from today, show the time.
            format_flags |= DateUtils.FORMAT_SHOW_TIME;
        }

        // If the caller has asked for full details, make sure to show the date
        // and time no matter what we've determined above (but still make showing
        // the year only happen if it is a different year from today).
        if (fullFormat) {
            format_flags |= (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
        }

        return DateUtils.formatDateTime(context, when, format_flags);
    }

    public static void selectAudio(Context context, int requestCode) {
        if (context instanceof AuroraActivity) {
            //m0
            /*
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_INCLUDE_DRM, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
                    context.getString(R.string.select_audio));
            */
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(ContentType.AUDIO_UNSPECIFIED);
            intent.setType(ContentType.AUDIO_OGG);
            intent.setType("application/x-ogg");  
            if (FeatureOption.MTK_DRM_APP) {
                intent.putExtra(DrmExtra.EXTRA_DRM_LEVEL, DrmExtra.DRM_LEVEL_SD);
            }
            //m1
            ((AuroraActivity) context).startActivityForResult(intent, requestCode);
        }
    }

    public static void recordSound(Context context, int requestCode, long sizeLimit) {
        if (context instanceof AuroraActivity) {
            // Aurora xuyong 2013-11-13 modified for S4 adapt start
            Intent intent = new Intent(android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION);
            /*intent.setType(ContentType.AUDIO_AMR);
            intent.setClassName("com.android.soundrecorder",
                    "com.android.soundrecorder.SoundRecorder");*/
            // Aurora xuyong 2013-11-13 modified for S4 adapt end
            intent.putExtra(android.provider.MediaStore.Audio.Media.EXTRA_MAX_BYTES, sizeLimit);
            //Gionee <guoyx> <2013-06-25> modify for CR00825923 begin
            //Gionee qiuxd 2012-5-11 add for CR00561845 start
            sizeLimit /= 1024;
            if(sizeLimit > 10){
                long time = (long)(sizeLimit * 0.5); 
                Log.i(TAG, "recordSound Limit time:" + (int)time);
                intent.putExtra("MmsRecordingLength", time);
            } else {
                Toast.makeText(context,
                        context.getString(R.string.message_too_big_for_recording),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            //Gionee qiuxd 2012-5-11 add for CR00561845 end
            //Gionee <guoyx> <2013-06-25> modify for CR00825923 end

            ((AuroraActivity) context).startActivityForResult(intent, requestCode);
        }
    }

    public static void recordVideo(Context context, int requestCode, long sizeLimit) {
        if (context instanceof AuroraActivity) {
            int durationLimit = getVideoCaptureDurationLimit();
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
            intent.putExtra("android.intent.extra.sizeLimit", sizeLimit);
            intent.putExtra("android.intent.extra.durationLimit", durationLimit);
            // Aurora xuyong 2013-11-26 added for aurora's new feature start
            intent.putExtra("mms", true);
            // Aurora xuyong 2013-11-26 added for aurora's new feature end
            Uri mTempFileUri = TempFileProvider.getScrapVideoUri(context);
            if (mTempFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempFileUri);
            }

            ((AuroraActivity) context).startActivityForResult(intent, requestCode);
        }
    }

    private static int getVideoCaptureDurationLimit() {
        CamcorderProfile camcorder = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        return camcorder == null ? 0 : camcorder.duration;
    }

    public static void selectVideo(Context context, int requestCode) {
        selectMediaByType(context, requestCode, ContentType.VIDEO_UNSPECIFIED, true);
    }

    public static void selectImage(Context context, int requestCode) {
        selectMediaByType(context, requestCode, ContentType.IMAGE_UNSPECIFIED, false);
    }

    private static void selectMediaByType(
            Context context, int requestCode, String contentType, boolean localFilesOnly) {
         if (context instanceof AuroraActivity) {

            Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            // Aurora xuyong 2015-09-14 added for aurora's new feature start
            innerIntent.setAction("com.aurora.filemanager.SINGLE_GET_CONTENT");
            // Aurora xuyong 2015-09-14 added for aurora's new feature end
            innerIntent.setType(contentType);
            //a0
            if (FeatureOption.MTK_DRM_APP) {
                innerIntent.putExtra(DrmExtra.EXTRA_DRM_LEVEL, DrmExtra.DRM_LEVEL_SD);
            }
            //a1
            if (localFilesOnly) {
                innerIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            }

            Intent wrapperIntent = Intent.createChooser(innerIntent, null);

            ((AuroraActivity) context).startActivityForResult(wrapperIntent, requestCode);
        }
    }

    public static void viewSimpleSlideshow(Context context, SlideshowModel slideshow) {
        if (!slideshow.isSimple()) {
            throw new IllegalArgumentException(
                    "viewSimpleSlideshow() called on a non-simple slideshow");
        }
        SlideModel slide = slideshow.get(0);
        MediaModel mm = null;
        if (slide.hasImage()) {
            mm = slide.getImage();
        } else if (slide.hasVideo()) {
            mm = slide.getVideo();
        }
        //a0
        else if (slide.hasAudio()) {
            mm = slide.getAudio();
        }
        //a1

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra("SingleItemOnly", true); // So we don't see "surrounding" images in Gallery
        intent.putExtra("CanShare", false);// CanShare:false , Hide the videopalye's share option menu. 
                                           // CanShare:ture, Show the share option menu.

        String contentType;
        if (mm.isDrmProtected()) {
            contentType = mm.getDrmObject().getContentType();
        } else {
            contentType = mm.getContentType();
        }
        Log.e(TAG, "viewSimpleSildeshow. Uri:" + mm.getUri());
        Log.e(TAG, "viewSimpleSildeshow. contentType:" + contentType);
        // Aurora xuyong 2015-07-29 modified for bug #14494 start
        File output = copyPartsToOutputFile(context, mm.getUri(), contentType);
        intent.setDataAndType(Uri.fromFile(output), contentType);
        // Aurora xuyong 2015-07-29 modified for bug #14494 end
        try {
            context.startActivity(intent);
        } catch(ActivityNotFoundException e) {
            Message msg = Message.obtain(MmsApp.getToastHandler());
            msg.what = MmsApp.MSG_MMS_CAN_NOT_OPEN;
            msg.obj = contentType;
            msg.sendToTarget();
            //after user click view, and the error toast is shown, we must make it can press again.
            // tricky code
            if (context instanceof ComposeMessageActivity) {
                ((ComposeMessageActivity)context).mClickCanResponse = true;
            }
        }
    }
    // Aurora xuyong 2015-07-29 added for bug #14494 start
    private static String getPrefixAndSuffix(String contentType) {
    	String[] items = contentType.split("/");
    	String prefix = items[0];
    	Log.e(TAG, "viewSimpleSildeshow. prefix:" + prefix);
    	String suffix = items[items.length - 1];
    	Log.e(TAG, "viewSimpleSildeshow. suffix:" + suffix);
    	// for the special format 3gpp whose real format is 3gp 
    	if ("3gpp".equals(suffix)) {
    		suffix = "3gp";
    	}
    	return prefix + "_-." + suffix;
    }
    
    private static String formatUniqueCode(Context context, Uri uri) {
    	Cursor result = null;
    	String code = "";
    	try {
    		String partName = null;
    		result = context.getContentResolver().query(uri, null, null, null, null);
    		if (result != null && result.moveToFirst()) {
    			Log.e(TAG, "viewSimpleSildeshow. part path :" + result.getString(result.getColumnIndex("_data")));
    			partName = result.getString(result.getColumnIndex("_data"));
    		}
    		if (partName != null) {
    			// format code from such pattern ---> /data/data/com.android.providers.telephony/app_parts/PART_1438164006213
    			// we get the tail serial numbers as the new tempfile name
    			String[] item = partName.split("_");
    			code = item[item.length - 1];
    		}
    	} catch(SQLiteException e) {
    		e.printStackTrace();
    	} finally {
    		if (result != null && !result.isClosed()) {
    			result.close();
    		}
    	}
    	Log.e(TAG, "viewSimpleSildeshow. unique code:" + code);
    	return code;
    }
    
    private static String formatNewFileName(Context context, Uri inputFileUri, String contentType) {
        // Aurora xuyong 2015-11-04 modified for aurora's new feature start
    	String prefix = ""; 
    	String suffix = ""; 
        if (contentType != null) { 
    	    String[] prefixAndSuffix = getPrefixAndSuffix(contentType).split("-");
    	    prefix = prefixAndSuffix[0];
    	    suffix = prefixAndSuffix[1];
        }
        // Aurora xuyong 2015-11-04 modified for aurora's new feature end 
    	String unicode = formatUniqueCode(context, inputFileUri);
    	return prefix + unicode + suffix;
    }
    
    public static File copyPartsToOutputFile(Context context, Uri inputFileUri, String contentType) {
    	File cacheDir = context.getExternalCacheDir();
    	Log.e(TAG, "viewSimpleSildeshow. cacheDir:" + cacheDir);
    	if (cacheDir == null) {
    		return null;
    	}
    	File outputPath = new File(cacheDir, "shared_files");
    	String fileName = formatNewFileName(context, inputFileUri, contentType);
    	File outputFile = new File(outputPath, fileName);
    	Log.e(TAG, "viewSimpleSildeshow. outputFile:" + outputFile);
    	if(!outputPath.exists()) {
            outputPath.mkdirs();
        }
    	if (!outputFile.exists()) {
	    	InputStream src = null;
	    	OutputStream dest = null;
	    	try {
	    		 dest = new FileOutputStream(outputFile);
	    		 src = context.getContentResolver().openInputStream(inputFileUri);
	    		 int len = -1;
	    		 byte[] bt = new byte[1024];
	    		 while((len = (src.read(bt))) != -1) {
	    			 dest.write(bt,0,len);
	    		 }
	    		 dest.flush();
	     		 dest.close();
	     		 src.close();
	    	} catch(IOException e) {
	    		e.printStackTrace();
	    	}
    	}
    	return outputFile;
    }
    // Aurora xuyong 2015-07-29 added for bug #14494 end
    // Aurora xuyong 2015-10-12 modified for bug #16730 start
    public static void showErrorDialog(final AuroraActivity activity,
            String title, String message) {
    // Aurora xuyong 2015-10-12 modified for bug #16730 end
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(activity);

//        builder.setIcon(R.drawable.ic_sms_mms_not_delivered);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    // Aurora xuyong 2015-10-12 added for bug #16730 end
                	((ComposeMessageActivity)activity).setDialogShow(false);
                    // Aurora xuyong 2015-10-12 added for bug #16730 end
                    dialog.dismiss();
                }
            }
        });
        if (!activity.isFinishing()) {
            builder.show();
            // Aurora xuyong 2015-10-12 added for bug #16730 end
            ((ComposeMessageActivity)activity).setDialogShow(true);
            // Aurora xuyong 2015-10-12 added for bug #16730 end
        }
    }

    /**
     * The quality parameter which is used to compress JPEG images.
     */
    public static final int IMAGE_COMPRESSION_QUALITY = 95;
    /**
     * The minimum quality parameter which is used to compress JPEG images.
     */
    public static final int MINIMUM_IMAGE_COMPRESSION_QUALITY = 50;

    /**
     * Message overhead that reduces the maximum image byte size.
     * 5000 is a realistic overhead number that allows for user to also include
     * a small MIDI file or a couple pages of text along with the picture.
     */
    public static final int MESSAGE_OVERHEAD = 5000;

    public static void resizeImageAsync(final Context context,
            final Uri imageUri, final Handler handler,
            final ResizeImageResultCallback cb,
            final boolean append) {

        // Show a progress toast if the resize hasn't finished
        // within one second.
        // Stash the runnable for showing it away so we can cancel
        // it later if the resize completes ahead of the deadline.
        final Runnable showProgress = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, R.string.compressing, Toast.LENGTH_SHORT).show();
            }
        };
        // Schedule it for one second from now.
        handler.postDelayed(showProgress, 1000);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final PduPart part;
                try {
                    UriImage image = new UriImage(context, imageUri);
                    // Aurora xuyong 2014-06-09 added for bug #5482 start
                    if (image == null) {
                        return;
                    }
                    // Aurora xuyong 2014-06-09 added for bug #5482 end
                    int widthLimit = MmsConfig.getMaxImageWidth();
                    int heightLimit = MmsConfig.getMaxImageHeight();
                    // In mms_config.xml, the max width has always been declared larger than the max
                    // height. Swap the width and height limits if necessary so we scale the picture
                    // as little as possible.
                    if (image.getHeight() > image.getWidth()) {
                        int temp = widthLimit;
                        widthLimit = heightLimit;
                        heightLimit = temp;
                    }

                    //m0
                    /*part = image.getResizedImageAsPart(
                        widthLimit,
                        heightLimit,
                        MmsConfig.getMaxMessageSize() - MESSAGE_OVERHEAD);
                    */
                    part = image.getResizedImageAsPart(
                            widthLimit,
                            heightLimit,
                            MmsConfig.getUserSetMmsSizeLimit(true) - MESSAGE_OVERHEAD);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            cb.onResizeResult(part, append);
                        }
                    });
                } catch (IllegalArgumentException e){
                    Log.e(TAG, "Unexpected IllegalArgumentException.", e);
                } finally {
                    // Cancel pending show of the progress toast if necessary.
                    handler.removeCallbacks(showProgress);
                }
            }
        }).start();
    }

    public static void resizeImage(final Context context, final Uri imageUri, final Handler handler,
            final ResizeImageResultCallback cb, final boolean append, boolean showToast) {

        // Show a progress toast if the resize hasn't finished
        // within one second.
        // Stash the runnable for showing it away so we can cancel
        // it later if the resize completes ahead of the deadline.
        final Runnable showProgress = new Runnable() {
            public void run() {
                Toast.makeText(context, R.string.compressing, Toast.LENGTH_SHORT).show();
            }
        };
        if (showToast) {
            handler.post(showProgress);
//            handler.postDelayed(showProgress, 1000);
        }
        final PduPart part;
        try {
            UriImage image = new UriImage(context, imageUri);
            // Aurora xuyong 2014-06-09 added for bug #5482 start
            if (image == null) {
                return;
            }
            // Aurora xuyong 2014-06-09 added for bug #5482 end
            part = image.getResizedImageAsPart(MmsConfig.getMaxImageWidth(), MmsConfig.getMaxImageHeight(), MmsConfig
                    .getUserSetMmsSizeLimit(true)
                - MESSAGE_OVERHEAD);
            cb.onResizeResult(part, append);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unexpected IllegalArgumentException.", e);
        }finally {
            // Cancel pending show of the progress toast if necessary.
            handler.removeCallbacks(showProgress);
        }
    }
    // Aurora xuyong 2014-03-06 modified for bug #2819 start
    public static AuroraAlertDialog showDiscardDraftConfirmDialog(Context context,
            OnClickListener listener) {
        //gionee gaoj 2013-4-1 modified for CR00788343 start
        return new AuroraAlertDialog.Builder(context)//, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
//                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.discard_message)
                .setMessage(R.string.discard_message_reason)
                .setPositiveButton(R.string.yes, listener)
                .setNegativeButton(R.string.no, null).create();
     // Aurora xuyong 2014-03-06 modified for bug #2819 end
        //gionee gaoj 2013-4-1 modified for CR00788343 end
    }

    public static String getLocalNumber() {
        if (null == sLocalNumber) {
            sLocalNumber = MmsApp.getApplication().getTelephonyManager().getLine1Number();
        }
        return sLocalNumber;
    }

    public static boolean isLocalNumber(String number) {
        if (number == null) {
            return false;
        }

        // we don't use Mms.isEmailAddress() because it is too strict for comparing addresses like
        // "foo+caf_=6505551212=tmomail.net@gmail.com", which is the 'from' address from a forwarded email
        // message from Gmail. We don't want to treat "foo+caf_=6505551212=tmomail.net@gmail.com" and
        // "6505551212" to be the same.
        if (number.indexOf('@') >= 0) {
            return false;
        }

        //m0
        //return PhoneNumberUtils.compare(number, getLocalNumber());
        // add for gemini
        if (MmsApp.mGnMultiSimMessage) {
            return PhoneNumberUtils.compare(number, getLocalNumberGemini(GnPhone.GEMINI_SIM_1))
                   || PhoneNumberUtils.compare(number, getLocalNumberGemini(GnPhone.GEMINI_SIM_2));
        } else {
            return PhoneNumberUtils.compare(number, getLocalNumber());
        }
        //m1
    }

    public static void handleReadReport(final Context context,
            final Collection<Long> threadIds,
            final int status,
            final Runnable callback) {
        StringBuilder selectionBuilder = new StringBuilder(Mms.MESSAGE_TYPE + " = "
                + PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF
                + " AND " + Mms.READ + " = 0"
                + " AND " + Mms.READ_REPORT + " = " + PduHeaders.VALUE_YES);

        String[] selectionArgs = null;
        if (threadIds != null) {
            String threadIdSelection = null;
            StringBuilder buf = new StringBuilder();
            selectionArgs = new String[threadIds.size()];
            int i = 0;

            for (long threadId : threadIds) {
                if (i > 0) {
                    buf.append(" OR ");
                }
                buf.append(Mms.THREAD_ID).append("=?");
                selectionArgs[i++] = Long.toString(threadId);
            }
            threadIdSelection = buf.toString();

            selectionBuilder.append(" AND (" + threadIdSelection + ")");
        }

        final Cursor c = SqliteWrapper.query(context, context.getContentResolver(),
                        Mms.Inbox.CONTENT_URI, new String[] {Mms._ID, Mms.MESSAGE_ID},
                        selectionBuilder.toString(), selectionArgs, null);

        if (c == null) {
            return;
        }

        final Map<String, String> map = new HashMap<String, String>();
        try {
            if (c.getCount() == 0) {
                if (callback != null) {
                    callback.run();
                }
                return;
            }

            while (c.moveToNext()) {
                Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, c.getLong(0));
                map.put(c.getString(1), AddressUtils.getFrom(context, uri));
            }
        } finally {
            c.close();
        }

        OnClickListener positiveListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (final Map.Entry<String, String> entry : map.entrySet()) {
                    MmsMessageSender.sendReadRec(context, entry.getValue(),
                                                 entry.getKey(), status);
                }

                if (callback != null) {
                    callback.run();
                }
                dialog.dismiss();
            }
        };

        OnClickListener negativeListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (callback != null) {
                    callback.run();
                }
                dialog.dismiss();
            }
        };

        OnCancelListener cancelListener = new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (callback != null) {
                    callback.run();
                }
                dialog.dismiss();
            }
        };

        confirmReadReportDialog(context, positiveListener,
                                         negativeListener,
                                         cancelListener);
    }

    //gionee gaoj 2012-3-22 added for CR00555790 start
    public static void handleReadReport(final Context context,
            final long threadId,
            final int status,
            final Runnable callback) {
        //Gionee <gaoj> <2013-06-03> add for CR00820192 begin
        //mms4.2 we[mtk] do not support reply read report when deleteing without read.
        if (MmsApp.mGnMessageSupport) {
            if (callback != null) {
                // Aurora liugj 2014-01-06 modified for bath-delete optimize start
                new Handler().post(callback);
                //callback.run();
                // Aurora liugj 2014-01-06 modified for bath-delete optimize end
            }
            return;
        }
        //Gionee <gaoj> <2013-06-03> add for CR00820192 end
        String selection = Mms.MESSAGE_TYPE + " = " + PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF
            + " AND " + Mms.READ + " = 0"
            + " AND " + Mms.READ_REPORT + " = " + PduHeaders.VALUE_YES;

        if (threadId != -1) {
            selection = selection + " AND " + Mms.THREAD_ID + " = " + threadId;
        }

        final Cursor c = SqliteWrapper.query(context, context.getContentResolver(),
                        Mms.Inbox.CONTENT_URI, new String[] {Mms._ID, Mms.MESSAGE_ID},
                        selection, null, null);

        if (c == null) {
            return;
        }

        final Map<String, String> map = new HashMap<String, String>();
        try {
            if (c.getCount() == 0) {
                if (callback != null) {
                    callback.run();
                }
                return;
            }

            while (c.moveToNext()) {
                Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, c.getLong(0));
                map.put(c.getString(1), AddressUtils.getFrom(context, uri));
            }
        } finally {
            c.close();
        }

        OnClickListener positiveListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                for (final Map.Entry<String, String> entry : map.entrySet()) {
                    MmsMessageSender.sendReadRec(context, entry.getValue(),
                                                 entry.getKey(), status);
                }

                if (callback != null) {
                    callback.run();
                }
                dialog.dismiss();
            }
        };

        OnClickListener negativeListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (callback != null) {
                    callback.run();
                }
                dialog.dismiss();
            }
        };

        OnCancelListener cancelListener = new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                if (callback != null) {
                    callback.run();
                }
            }
        };

        confirmReadReportDialog(context, positiveListener,
                                         negativeListener,
                                         cancelListener);
    }

    //gionee gaoj 2012-3-22 added for CR00555790 end
    
    private static void confirmReadReportDialog(Context context,
            OnClickListener positiveListener, OnClickListener negativeListener,
            OnCancelListener cancelListener) {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(R.string.confirm);
        builder.setMessage(R.string.message_send_read_report);
        builder.setPositiveButton(R.string.yes, positiveListener);
        builder.setNegativeButton(R.string.no, negativeListener);
        builder.setOnCancelListener(cancelListener);
        builder.show();
    }

    public static String extractEncStrFromCursor(Cursor cursor,
            int columnRawBytes, int columnCharset) {
        String rawBytes = cursor.getString(columnRawBytes);
        int charset = cursor.getInt(columnCharset);

        if (TextUtils.isEmpty(rawBytes)) {
            return "";
        } else if (charset == CharacterSets.ANY_CHARSET) {
            return rawBytes;
        } else {
            return new EncodedStringValue(charset, PduPersister.getBytes(rawBytes)).getString();
        }
    }

    private static String extractEncStr(Context context, EncodedStringValue value) {
        if (value != null) {
            return value.getString();
        } else {
            Log.d(TAG, "extractEncStr EncodedStringValue is null");
            return "";
        }
    }

    public static ArrayList<String> extractUris(URLSpan[] spans) {
        int size = spans.length;
        ArrayList<String> accumulator = new ArrayList<String>();

        for (int i = 0; i < size; i++) {
            accumulator.add(spans[i].getURL());
        }
        return accumulator;
    }

    /**
     * Play/view the message attachments.
     * TOOD: We need to save the draft before launching another activity to view the attachments.
     *       This is hacky though since we will do saveDraft twice and slow down the UI.
     *       We should pass the slideshow in intent extra to the view activity instead of
     *       asking it to read attachments from database.
     * @param context
     * @param msgUri the MMS message URI in database
     * @param slideshow the slideshow to save
     * @param persister the PDU persister for updating the database
     * @param sendReq the SendReq for updating the database
     */
    public static void viewMmsMessageAttachment(Context context, Uri msgUri,
            SlideshowModel slideshow) {
        viewMmsMessageAttachment(context, msgUri, slideshow, 0);
    }
    // Aurora xuyong 2013-12-30 added for aurora's ne feature start
    public static void viewMmsMessageAttachment(Context context, Uri msgUri,
            SlideshowModel slideshow, String slideSubject, String slideTimeStamp) {
        viewMmsMessageAttachment(context, msgUri, slideshow, 0, slideSubject, slideTimeStamp);
    }
    
    private static void viewMmsMessageAttachment(Context context, Uri msgUri,
            SlideshowModel slideshow, int requestCode, String slideSubject, String slideTimeStamp) {
        if (MmsApp.mGnMessageSupport && msgUri == null) {
            return ;
        }
        boolean isSimple = (slideshow == null) ? false : slideshow.isSimple();
        if(mUnicomCustom) {
            isSimple = false;
        }
            
        SlideModel slide = null;
        if (slideshow != null){
            // If a slideshow was provided, save it to disk first.
            PduPersister persister = PduPersister.getPduPersister(context);
            try {
                PduBody pb = slideshow.toPduBody();
                persister.updateParts(msgUri, pb);
                slideshow.sync(pb);
            } catch (MmsException e) {
                    Log.e(TAG, "Unable to save message for preview");
                    return;
            }
            slide = slideshow.get(0);
        }
        if (isSimple && slide!=null) {
            MessageUtils.viewSimpleSlideshow(context, slideshow);
        } else {
            Intent intent;
            if (MmsApp.mGnMessageSupport) {
                intent =new Intent(context, SlidesBrowserActivity.class);
            } else {
                if ((isSimple && slide.hasAudio()) || (requestCode == AttachmentEditor.MSG_PLAY_AUDIO)) {//play the only audio directly
                    intent =new Intent(context, SlideshowActivity.class);
                } else {
                    intent = new Intent(context, MmsPlayerActivity.class);
                }
            }
            intent.setData(msgUri);
            intent.putExtra("subject", slideSubject);
            intent.putExtra("timestamp", slideTimeStamp);
            if (requestCode > 0 && context instanceof AuroraActivity) {
                ((AuroraActivity)context).startActivityForResult(intent, requestCode);
            } else {
                context.startActivity(intent);
            }
        }
    }
    // Aurora xuyong 2013-12-30 added for aurora's ne feature end

    private static void viewMmsMessageAttachment(Context context, Uri msgUri,
            SlideshowModel slideshow, int requestCode) {
        // gionee zhouyj 2012-11-22 add CR00733251 start 
        if (MmsApp.mGnMessageSupport && msgUri == null) {
            return ;
        }
        // gionee zhouyj 2012-11-22 add CR00733251 end 
        boolean isSimple = (slideshow == null) ? false : slideshow.isSimple();
        // gionee zhouyj 2012-06-12 add for CR00623647 start 
        if(mUnicomCustom) {
            isSimple = false;
        }
        // gionee zhouyj 2012-06-12 add for CR00623647 end 
        Log.d(TAG, "isSimple" + isSimple + "slideshow null:" + (slideshow == null));
//m0
        /*if (isSimple) {
            // In attachment-editor mode, we only ever have one slide.
            MessageUtils.viewSimpleSlideshow(context, slideshow);
        } else {
            // If a slideshow was provided, save it to disk first.
            if (slideshow != null) {
                PduPersister persister = PduPersister.getPduPersister(context);
                try {
                    PduBody pb = slideshow.toPduBody();
                    persister.updateParts(msgUri, pb);
                    slideshow.sync(pb);
                } catch (MmsException e) {
                    Log.e(TAG, "Unable to save message for preview");
                    return;
                }
            }*/
            
        SlideModel slide = null;
        if (slideshow != null){
            // If a slideshow was provided, save it to disk first.
            PduPersister persister = PduPersister.getPduPersister(context);
            try {
                PduBody pb = slideshow.toPduBody();
                persister.updateParts(msgUri, pb);
                slideshow.sync(pb);
            } catch (MmsException e) {
                    Log.e(TAG, "Unable to save message for preview");
                    return;
            }
            slide = slideshow.get(0);
        }
        //gionee gaoj 2012-5-8 modified for CR00588986 start
        if (isSimple && slide!=null) {
            //gionee gaoj 2012-5-8 modified for CR00588986 end
            // In attachment-editor mode, we only ever have one slide.
            MessageUtils.viewSimpleSlideshow(context, slideshow);
//m1
        } else {
            // Launch the slideshow activity to play/view.
            Intent intent;
            // gionee zhouyj 2012-05-14 modified for CR00585826 start
            if (MmsApp.mGnMessageSupport) {
                //intent =new Intent(context, SlideshowActivity.class);
                intent =new Intent(context, SlidesBrowserActivity.class);
                // Aurora xuyong 2014-08-28 added for aurora's new feature start
                intent.putExtra("timestamp", context.getString(R.string.aurora_mms_editing));
                // Aurora xuyong 2014-08-28 added for aurora's new feature end
            } else {
            // gionee zhouyj 2012-05-14 modified for CR00585826 end
                if ((isSimple && slide.hasAudio()) || (requestCode == AttachmentEditor.MSG_PLAY_AUDIO)) {//play the only audio directly
                    intent =new Intent(context, SlideshowActivity.class);
                } else {
                    intent = new Intent(context, MmsPlayerActivity.class);
                }
            // gionee zhouyj 2012-04-28 modified for CR00585826 start
            }
            // gionee zhouyj 2012-04-28 modified for CR00585826 end
            intent.setData(msgUri);
            if (requestCode > 0 && context instanceof AuroraActivity) {
                ((AuroraActivity)context).startActivityForResult(intent, requestCode);
            } else {
                context.startActivity(intent);
            }
        }
    }

    public static void viewMmsMessageAttachment(Context context, WorkingMessage msg,
            int requestCode) {
        SlideshowModel slideshow = msg.getSlideshow();
        //gionee gaoj 2012-5-8 added for CR00588986 start
        if (MmsApp.mGnMessageSupport) {
            Uri uri = msg.saveAsMms(false);
            viewMmsMessageAttachment(context, uri, slideshow);
        } else {
        //gionee gaoj 2012-5-8 added for CR00588986 end
        if (slideshow == null) {
            throw new IllegalStateException("msg.getSlideshow() == null");
        }
        
        SlideModel slide = slideshow.get(0);
        if (slideshow.isSimple() && slide!=null && !slide.hasAudio()) {
            MessageUtils.viewSimpleSlideshow(context, slideshow);
        } else {
            Uri uri = msg.saveAsMms(false);
            if (uri != null) {
                // Pass null for the slideshow paramater, otherwise viewMmsMessageAttachment
                // will persist the slideshow to disk again (we just did that above in saveAsMms)
                viewMmsMessageAttachment(context, uri, null, requestCode);
            }
        }
        //gionee gaoj 2012-5-8 added for CR00588986 start
        }
        //gionee gaoj 2012-5-8 added for CR00588986 end
    }

    /**
     * Debugging
     */
    public static void writeHprofDataToFile(){
        String filename = Environment.getExternalStorageDirectory() + "/mms_oom_hprof_data";
        try {
            android.os.Debug.dumpHprofData(filename);
            Log.i(TAG, "##### written hprof data to " + filename);
        } catch (IOException ex) {
            Log.e(TAG, "writeHprofDataToFile: caught " + ex);
        }
    }

    // An alias (or commonly called "nickname") is:
    // Nickname must begin with a letter.
    // Only letters a-z, numbers 0-9, or . are allowed in Nickname field.
    public static boolean isAlias(String string) {
        if (!MmsConfig.isAliasEnabled()) {
            return false;
        }

        int len = string == null ? 0 : string.length();

        if (len < MmsConfig.getAliasMinChars() || len > MmsConfig.getAliasMaxChars()) {
            return false;
        }

        if (!Character.isLetter(string.charAt(0))) {    // Nickname begins with a letter
            return false;
        }
        for (int i = 1; i < len; i++) {
            char c = string.charAt(i);
            if (!(Character.isLetterOrDigit(c) || c == '.')) {
                return false;
            }
        }

        return true;
    }

    /**
     * Given a phone number, return the string without syntactic sugar, meaning parens,
     * spaces, slashes, dots, dashes, etc. If the input string contains non-numeric
     * non-punctuation characters, return null.
     */
    private static String parsePhoneNumberForMms(String address) {
        StringBuilder builder = new StringBuilder();
        int len = address.length();

        for (int i = 0; i < len; i++) {
            char c = address.charAt(i);

            // accept the first '+' in the address
            if (c == '+' && builder.length() == 0) {
                builder.append(c);
                continue;
            }

            if (Character.isDigit(c)) {
                builder.append(c);
                continue;
            }

            if (numericSugarMap.get(c) == null) {
                return null;
            }
        }
        return builder.toString();
    }

    /**
     * Returns true if the address passed in is a valid MMS address.
     */
    public static boolean isValidMmsAddress(String address) {
        String retVal = parseMmsAddress(address);
        //m0
        //return (retVal != null);
        return (retVal != null && !retVal.equals(""));
        //m1
    }

    /**
     * parse the input address to be a valid MMS address.
     * - if the address is an email address, leave it as is.
     * - if the address can be parsed into a valid MMS phone number, return the parsed number.
     * - if the address is a compliant alias address, leave it as is.
     */
    public static String parseMmsAddress(String address) {
        // if it's a valid Email address, use that.
        if (Mms.isEmailAddress(address)) {
            return address;
        }

        // if we are able to parse the address to a MMS compliant phone number, take that.
        String retVal = parsePhoneNumberForMms(address);
        if (retVal != null) {
            return retVal;
        }

        // if it's an alias compliant address, use that.
        if (isAlias(address)) {
            return address;
        }

        // it's not a valid MMS address, return null
        return null;
    }

    private static void log(String msg) {
        Log.d(TAG, "[MsgUtils] " + msg);
    }
    
    //a0
    private static String sLocalNumber2;
    
    public static Uri saveBitmapAsPart(Context context, Uri messageUri, Bitmap bitmap)
        throws MmsException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, IMAGE_COMPRESSION_QUALITY, os);

        PduPart part = new PduPart();

        part.setContentType("image/jpeg".getBytes());
        String contentId = "Image" + System.currentTimeMillis();
        part.setContentLocation((contentId + ".jpg").getBytes());
        part.setContentId(contentId.getBytes());
        part.setData(os.toByteArray());

        Uri retVal = PduPersister.getPduPersister(context).persistPart(part,
                            ContentUris.parseId(messageUri));

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("saveBitmapAsPart: persisted part with uri=" + retVal);
        }

        return retVal;
    }
    
    public static String getLocalNumberGemini(int simId) {
        // convert sim id to slot id
        int slotId = SIMInfo.getSlotById(MmsApp.getApplication().getApplicationContext(), simId);
        if (GnPhone.GEMINI_SIM_1 == slotId) {
            if (null == sLocalNumber) {
                sLocalNumber = GnTelephonyManager.getLine1NumberGemini(slotId);
            }
            Log.d(MmsApp.TXN_TAG, "getLocalNumberGemini, Sim ID=" + simId + ", slot ID=" + slotId +", lineNumber=" + sLocalNumber);

            return sLocalNumber;
        } else if (GnPhone.GEMINI_SIM_2 == slotId) {
            if (null == sLocalNumber2) {
                sLocalNumber2 = GnTelephonyManager.getLine1NumberGemini(slotId);
            }
            Log.d(MmsApp.TXN_TAG, "getLocalNumberGemini, Sim ID=" + simId + ", slot ID=" + slotId +", lineNumber=" + sLocalNumber);
            return sLocalNumber2;
        } else {
            Log.e(MmsApp.TXN_TAG, "getLocalNumberGemini, illegal slot ID");
            return null;
        }
    }
    
    // add for gemini
    public static void handleReadReportGemini(final Context context,
            final long threadId,
            final int status,
            final Runnable callback) {
        String selection = Mms.MESSAGE_TYPE + " = " + PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF
            + " AND " + Mms.READ + " = 0"
            + " AND " + Mms.READ_REPORT + " = " + PduHeaders.VALUE_YES;

        if (threadId != -1) {
            selection = selection + " AND " + Mms.THREAD_ID + " = " + threadId;
        }

        final Cursor c = SqliteWrapper.query(context, context.getContentResolver(),
                        Mms.Inbox.CONTENT_URI, new String[] {Mms._ID, Mms.MESSAGE_ID, Mms.SIM_ID},
                        selection, null, null);

        if (c == null) {
            return;
        }

        final Map<String, String> map = new HashMap<String, String>();
        try {
            if (c.getCount() == 0) {
                if (callback != null) {
                    callback.run();
                }
                return;
            }

            while (c.moveToNext()) {
                Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, c.getLong(0));
                map.put(c.getString(1), AddressUtils.getFrom(context, uri));
            }
        } finally {
            c.close();
        }

        OnClickListener positiveListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                for (final Map.Entry<String, String> entry : map.entrySet()) {
                    MmsMessageSender.sendReadRec(context, entry.getValue(),
                                                 entry.getKey(), status);
                }

                if (callback != null) {
                    callback.run();
                }
            }
        };

        OnClickListener negativeListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (callback != null) {
                    callback.run();
                }
            }
        };

        OnCancelListener cancelListener = new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                if (callback != null) {
                    callback.run();
                }
            }
        };

        confirmReadReportDialog(context, positiveListener,
                                         negativeListener,
                                         cancelListener);
    }
    
    public static void viewMmsMessageAttachmentMini(Context context, Uri msgUri,
            SlideshowModel slideshow) {
        if (msgUri == null){
            return;
        }
        boolean isSimple = (slideshow == null) ? false : slideshow.isSimple();
        if (slideshow != null){
            // If a slideshow was provided, save it to disk first.
            PduPersister persister = PduPersister.getPduPersister(context);
            try {
                PduBody pb = slideshow.toPduBody();
                persister.updateParts(msgUri, pb);
                slideshow.sync(pb);
            } catch (MmsException e) {
                Log.e(TAG, "Unable to save message for preview");
                return;
            }
        }

        // Launch the slideshow activity to play/view.
        Intent intent;
        if (isSimple && (slideshow != null) && slideshow.get(0).hasAudio()) {
            intent = new Intent(context, SlideshowActivity.class);
        } else {
            intent = new Intent(context, MmsPlayerActivity.class);
        }
        intent.setData(msgUri);
        context.startActivity(intent);
    }   
    
    //wappush: add this function to handle the url string, if it does not contain the http or https schema, then add http schema manually.
    public static String CheckAndModifyUrl(String url){
        if(url==null){
            return null;
        }

        Uri uri = Uri.parse(url);
        if(uri.getScheme() != null ){
            return url;
        }

        return "http://" + url;
    }

    public static void selectRingtone(Context context, int requestCode) {
        if (context instanceof AuroraActivity) {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
//Gionee guoyx 20120906 removed for CR00681081 begin
//            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_INCLUDE_DRM, false);
//Gionee guoyx 20120906 remove for CR00681081 end
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
                    context.getString(R.string.select_audio));
            ((AuroraActivity) context).startActivityForResult(intent, requestCode);
        }
    }

    public static Map<Integer, CharSequence> simInfoMap = new HashMap<Integer, CharSequence>();
    //gionee gaoj 2012-5-2 added for CR00582563 start
    public static Map<Integer, SIMInfo> gnSimInfoMap = new HashMap<Integer, SIMInfo>();
    public static boolean mUnicomCustom = MmsApp.isUnicomOperator();
    public static boolean mShowDigitalSlot = android.os.SystemProperties.get("ro.gn.operator.showdigitalslot").equals("yes");
    //gionee gaoj 2012-5-2 added for CR00582563 end
    // Gionee lihuafang 2012-06-02 add for CR00613974 begin
    public static boolean mShowSlot = android.os.SystemProperties.get("ro.gn.operator.showslot").equals("yes");
    // Gionee lihuafang 2012-06-02 add for CR00613974 end

    //Gionee:tianxiaolong 2012.8.2 modify for CR00663594 begin
    public static final boolean gnFlyFlag = android.os.SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY");
    //Gionee:tianxiaolong 2012.8.2 modify for CR00663594 end

   //Gionee qinkai 2012-09-05 added for CR00679226 start
   private static final boolean gnOverseaflag = SystemProperties.get("ro.gn.oversea.product").equals("yes"); 
   //Gionee qinkai 2012-09-05 added for CR00679226 end
    
    public static CharSequence getSimInfo(Context context, int simId){
        Log.d(TAG, "getSimInfo simId = " + simId);
        //add this code for more safty
        if (simId == -1) {
            return "";
        }
        if (simInfoMap.containsKey(simId)) {
            Log.d(TAG, "MessageUtils.getSimInfo(): getCache");
            return simInfoMap.get(simId);
        }
        //get sim info
        SIMInfo simInfo = SIMInfo.getSIMInfoById(context, simId);
        if(null != simInfo){
            //gionee gaoj 2012-5-2 added for CR00582563 start
            if (MmsApp.mGnMessageSupport) {
                gnSimInfoMap.put(simId, simInfo);
            }
            //gionee gaoj 2012-5-2 added for CR00582563 end
            String displayName = simInfo.mDisplayName;

            // Gionee lihuafang 20120503 add for CR00588600 begin
            if (MmsApp.mGnMultiSimMessage
                    && mShowSlot) {
                if (simInfo.mSlot == 0) {
                    if (mUnicomCustom || mShowDigitalSlot) {
                        displayName = context.getString(R.string.gn_slot_1) + displayName;
                    } else {
                        displayName = context.getString(R.string.gn_slot_a) + displayName;
                    }
                } else if (simInfo.mSlot == 1) {
                    if (mUnicomCustom || mShowDigitalSlot) {
                        displayName = context.getString(R.string.gn_slot_2) + displayName;
                    } else {
                        displayName = context.getString(R.string.gn_slot_b) + displayName;
                    }
                }
            }
            // Gionee lihuafang 20120503 add for CR00588600 end
            Log.d(TAG, "SIMInfo simId=" + simInfo.mSimId + " mDisplayName=" + displayName);

            if(null == displayName){
                simInfoMap.put(simId, "");
                return "";
            }

            SpannableStringBuilder buf = new SpannableStringBuilder();
            buf.append(" ");
            if (displayName.length() < 20) {
                buf.append(displayName);
            } else {
                buf.append(displayName.substring(0,8) + "..." + displayName.substring(displayName.length()-9, displayName.length()-1));
            }
            buf.append(" ");

            //set background image
            int colorRes = (simInfo.mSlot >= 0) ? simInfo.mSimBackgroundRes : -1/*R.drawable.sim_background_locked*/;
              // Aurora liugj 2013-11-04 added for not founf resource start 
            /*if (colorRes == 0) {
                colorRes = R.drawable.sim_background_locked;
              }*/
              // Aurora liugj 2013-11-04 added for not founf resource end
            Drawable drawable = context.getResources().getDrawable(colorRes);
            buf.setSpan(new GnBackgroundImageSpan(colorRes, drawable), 0, buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //set simInfo color
            int color = context.getResources().getColor(R.color.siminfo_color);
            buf.setSpan(new ForegroundColorSpan(color), 0, buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //buf.setSpan(new StyleSpan(Typeface.BOLD),0, buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            simInfoMap.put(simId, buf);
            return buf;
        }
        simInfoMap.put(simId, "");
        //gionee gaoj 2012-5-2 added for CR00582563 start
        if (MmsApp.mGnMessageSupport) {
            gnSimInfoMap.put(simId, null);
        }
        //gionee gaoj 2012-5-2 added for CR00582563 end
        return "";
    }

    public static void addNumberOrEmailtoContact(final String numberOrEmail, final int REQUEST_CODE,
            final AuroraActivity activity) {
        if (!TextUtils.isEmpty(numberOrEmail)) {
            String message = activity.getResources().getString(R.string.add_contact_dialog_message, numberOrEmail);
            AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(activity).setTitle(numberOrEmail).setMessage(message);
            AuroraAlertDialog dialog = builder.create();
            dialog.setButton(AuroraAlertDialog.BUTTON_POSITIVE, activity.getResources().getString(
                    R.string.add_contact_dialog_existing), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                    
                    //gionee gaoj 2013-4-2 added for CR00792780 start
                    intent.setComponent(new ComponentName("com.android.contacts",
                    "com.android.contacts.activities.ContactSelectionActivity"));
                    //gionee gaoj 2013-4-2 added for CR00792780 end
                    
                    intent.setType(Contacts.CONTENT_ITEM_TYPE);
                    if (Mms.isEmailAddress(numberOrEmail)) {
                        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, numberOrEmail);
                    } else {
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, numberOrEmail);
                    }
                    if (REQUEST_CODE > 0) {
                        activity.startActivityForResult(intent, REQUEST_CODE);
                    } else {
                        activity.startActivity(intent);
                    }
                }
            });

            dialog.setButton(AuroraAlertDialog.BUTTON_NEGATIVE, activity.getResources()
                    .getString(R.string.add_contact_dialog_new), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    final Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                    
                    //gionee gaoj 2013-4-2 added for CR00792780 start
                    intent.setComponent(new ComponentName("com.android.contacts",
                    "com.android.contacts.activities.ContactEditorActivity"));
                    //gionee gaoj 2013-4-2 added for CR00792780 end
                    
                    if (Mms.isEmailAddress(numberOrEmail)) {
                        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, numberOrEmail);
                    } else {
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, numberOrEmail);
                    }
                    if (REQUEST_CODE > 0) {
                        activity.startActivityForResult(intent, REQUEST_CODE);
                    } else {
                        activity.startActivity(intent);
                    }
                }
            });
            dialog.show();
        } else {
            
        }
    }
    public static boolean checkUriContainsDrm(Context context, Uri uri) {
        String uripath = convertUriToPath(context, uri);
        String extName = uripath.substring(uripath.lastIndexOf('.') + 1);
        if (extName.equals("dcf")) {
            return true;
        }
        return false;
    }
    private static String convertUriToPath(Context context, Uri uri) {
        String path = null;
        if (null != uri) {
            String scheme = uri.getScheme();
            if (null == scheme || scheme.equals("")
                    || scheme.equals(ContentResolver.SCHEME_FILE)) {
                path = uri.getPath();
            } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                String[] projection = new String[] { MediaStore.MediaColumns.DATA };
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver().query(uri,
                            projection, null, null, null);
                    if (null == cursor || 0 == cursor.getCount()
                            || !cursor.moveToFirst()) {
                        throw new IllegalArgumentException(
                                "Given Uri could not be found"
                                        + " in media store");
                    }
                    int pathIndex = cursor
                            .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    path = cursor.getString(pathIndex);
                } catch (SQLiteException e) {
                    throw new IllegalArgumentException(
                            "Given Uri is not formatted in a way "
                                    + "so that it can be found in media store.");
                } finally {
                    if (null != cursor) {
                        cursor.close();
                    }
                }
            } else {
                throw new IllegalArgumentException(
                        "Given Uri scheme is not supported");
            }
        }
        return path;
    }

    /**
     * Return the current storage status.
     */
    public static String getStorageStatus(Context context) {
        // we need count only
        final String[] PROJECTION = new String[] {
            BaseColumns._ID, Mms.MESSAGE_SIZE
        };
        final ContentResolver cr = context.getContentResolver();
        final Resources res = context.getResources();
        Cursor cursor = null;
        
        StringBuilder buffer = new StringBuilder();
        // Mms count
        cursor = cr.query(Mms.CONTENT_URI, PROJECTION, null, null, null);
        int mmsCount = 0;
        if (cursor != null) {
            mmsCount = cursor.getCount();
        }
        buffer.append(res.getString(R.string.storage_dialog_mms, mmsCount));
        buffer.append("\n");
        //Mms size 
        long size = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                size += cursor.getInt(1);
            } while (cursor.moveToNext());
            cursor.close();
        }
        buffer.append(res.getString(R.string.storage_dialog_mms_size) + getHumanReadableSize(size));
        buffer.append("\n");
        // Sms count
        cursor = cr.query(Sms.CONTENT_URI, PROJECTION, null, null, null);
        int smsCount = 0;
        if (cursor != null) {
            smsCount = cursor.getCount();
            cursor.close();
        }
        buffer.append(res.getString(R.string.storage_dialog_sms, smsCount));
        buffer.append("\n");
        // Attachment size
//        final String sizeTag = getHumanReadableSize(getAttachmentsSize(cr));
//        buffer.append(res.getString(R.string.storage_dialog_attachments) + sizeTag);
//        buffer.append("\n");
        // Database size
        final File db = new File("/data/data/com.android.providers.telephony/databases/mmssms.db");
        final long dbsize = db.length();
        buffer.append(res.getString(R.string.storage_dialog_database) + getHumanReadableSize(dbsize));
        buffer.append("\n");
        // Available space
        final StatFs datafs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        final long availableSpace = datafs.getAvailableBlocks() * datafs.getBlockSize();
        buffer.append(res.getString(R.string.storage_dialog_available_space) + getHumanReadableSize(availableSpace));
        return buffer.toString();
    }

    private static long getAttachmentsSize(ContentResolver cr) {
        String[] projs = new String[] {
            Mms.Part._DATA
        };
        // TODO: is there a predefined Uri like Mms.CONTENT_URI?
        final Uri part = Uri.parse("content://mms/part/");
        Cursor cursor = cr.query(part, projs, null, null, null);
        long size = 0;
        if (cursor == null || !cursor.moveToFirst()) {
            Log.e(TAG, "getAttachmentsSize, cursor is empty or null");
            return size;
        }
        Log.i(TAG, "getAttachmentsSize, count " + cursor.getCount());
        do {
            final String data = cursor.getString(0);
            if (data != null) {
                File file = new File(data);
                size += file.length();
            }
        } while (cursor.moveToNext());
        if (cursor != null) {
            cursor.close();
        }
        return size;
    }

    public static String getHumanReadableSize(long size) {
        String tag;
        float fsize = (float) size;
        if (size < 1024L) {
            tag = String.valueOf(size) + "B";
        } else if (size < 1024L*1024L) {
            fsize /= 1024.0f;
            tag = String.format(Locale.ENGLISH, "%.2f", fsize) + "KB";
        } else {
            fsize /= 1024.0f * 1024.0f;
            tag = String.format(Locale.ENGLISH, "%.2f", fsize) + "MB";
        }
        return tag;
    }

    public static int getSimStatusResource(int state) {
        Log.i(TAG, "SIM state is " + state);
        // gionee zhouyj 2013-02-27 add for CR00773355 start 
        if (MmsApp.mGnMessageSupport) {
            return GnTelephonyManager.getSIMStateIcon(state);
        }
        // gionee zhouyj 2013-02-27 add for CR00773355 end 
        switch (state) {
           /* *//** 1, RADIOOFF : has SIM/USIM inserted but not in use . *//*
            case GnPhone.SIM_INDICATOR_RADIOOFF:
                return R.drawable.sim_radio_off;

            *//** 2, LOCKED : has SIM/USIM inserted and the SIM/USIM has been locked. *//*
            case GnPhone.SIM_INDICATOR_LOCKED:
                return R.drawable.sim_locked;

            *//** 3, INVALID : has SIM/USIM inserted and not be locked but failed to register to the network. *//*
            case GnPhone.SIM_INDICATOR_INVALID:
                return R.drawable.sim_invalid;

            *//** 4, SEARCHING : has SIM/USIM inserted and SIM/USIM state is Ready and is searching for network. *//*
            case GnPhone.SIM_INDICATOR_SEARCHING:
                return R.drawable.sim_searching;

            *//** 6, ROAMING : has SIM/USIM inserted and in roaming service(has no data connection). *//*  
            case GnPhone.SIM_INDICATOR_ROAMING:
                return R.drawable.sim_roaming;

            *//** 7, CONNECTED : has SIM/USIM inserted and in normal service(not roaming) and data connected. *//*
            case GnPhone.SIM_INDICATOR_CONNECTED:
                return R.drawable.sim_connected;

            *//** 8, ROAMINGCONNECTED = has SIM/USIM inserted and in roaming service(not roaming) and data connected.*//*
            case GnPhone.SIM_INDICATOR_ROAMINGCONNECTED:
                return R.drawable.sim_roaming_connected;

            *//** -1, UNKNOWN : invalid value *//*
            case GnPhone.SIM_INDICATOR_UNKNOWN:

            *//** 0, ABSENT, no SIM/USIM card inserted for this phone *//*
            case GnPhone.SIM_INDICATOR_ABSENT:

            *//** 5, NORMAL = has SIM/USIM inserted and in normal service(not roaming and has no data connection). *//*
            case GnPhone.SIM_INDICATOR_NORMAL:*/
            default:
                return GnPhone.SIM_INDICATOR_UNKNOWN;
        }
    }

    public static int getSimStatus(int id, List<SIMInfo> simInfoList, GnTelephonyManagerEx telephonyManager) {
        int slotId = simInfoList.get(id).mSlot;
        if (slotId != -1) {
            return telephonyManager.getSimIndicatorStateGemini(slotId);
        }
        return -1;
    }

    public static boolean is3G(int id, List<SIMInfo> simInfoList) {
        int slotId = simInfoList.get(id).mSlot;
        Log.i(TAG, "SIMInfo.getSlotById id: " + id + " slotId: " + slotId);
        if (slotId == get3GCapabilitySIM()) {
            return true;
        }
        return false;
    }

    public static int get3GCapabilitySIM() {
        int retval = -1;
        // gionee zhouyj 2012-06-27 add for CR00627813 start 
        // Gionee lihuafang 2012-07-05 remove for CR00637513 begin
        /*
        if(MmsApp.mGnMessageSupport) 
            retval = 0;
        */
        // Gionee lihuafang 2012-07-05 remove for CR00637513 end
        // gionee zhouyj 2012-06-27 add for CR00627813 end 
        if(FeatureOption.MTK_GEMINI_3G_SWITCH) {
            ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
            try {
                retval = ((telephony == null) ? -1 : GnITelephony.get3GCapabilitySIM(telephony));
            } catch(Exception e) {
                Log.e(TAG, "get3GCapabilitySIM(): throw RemoteException");
            }
        }
        return retval;
    }

    public static boolean canAddToContacts(Contact contact) {
        // There are some kind of automated messages, like STK messages, that we don't want
        // to add to contacts. These names begin with special characters, like, "*Info".
        final String name = contact.getName();
        if (!TextUtils.isEmpty(contact.getNumber())) {
            char c = contact.getNumber().charAt(0);
            if (isSpecialChar(c)) {
                return false;
            }
        }
        if (!TextUtils.isEmpty(name)) {
            char c = name.charAt(0);
            if (isSpecialChar(c)) {
                return false;
            }
        }
        if (!(Mms.isEmailAddress(name) || (Mms.isPhoneNumber(name) || isPhoneNumber(name)) ||
                MessageUtils.isLocalNumber(contact.getNumber()))) {     // Handle "Me"
            return false;
        }
        return true;
    }

    private static boolean isPhoneNumber(String num) {
        num = num.trim();
        if (TextUtils.isEmpty(num)) {
            return false;
        }
        final char[] digits = num.toCharArray();
        for (char c : digits) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSpecialChar(char c) {
        return c == '*' || c == '%' || c == '$';
    }
    //a1

    //MTK_OP03_PROTECT_START
    /**
     * Get sms encoding type set by user.
     * @param context
     * @return encoding type
     */
    public static int getSmsEncodingType(Context context) {
        int type = SmsMessage.ENCODING_UNKNOWN;
        SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(context);
        String encodingType = null;
        encodingType = prefs.getString(MessagingPreferenceActivity.SMS_INPUT_MODE, "Automatic");

        if ("Unicode".equals(encodingType)) {
            type = SmsMessage.ENCODING_16BIT;
        } else if ("GSM alphabet".equals(encodingType)) {
            type = SmsMessage.ENCODING_7BIT;
        }
        return type;
    }
    //MTK_OP03_PROTECT_END

    //add for cmcc dir ui begin
    public static void replyMessage(Context context, String address) {
        Intent intent = new Intent();
        intent.putExtra("address", address);
        intent.putExtra("showinput", true);
        intent.setClassName(context, "com.android.mms.ui.ComposeMessageActivity");
        context.startActivity(intent);
    }

    public static void confirmDeleteMessage(final AuroraActivity activity, final Uri msgUri) {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(activity);
        builder.setTitle(R.string.confirm_dialog_title);
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setCancelable(true);
        builder.setMessage(R.string.confirm_delete_message);
        builder.setPositiveButton(R.string.delete, 
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        SqliteWrapper.delete(activity.getApplicationContext(), activity.getContentResolver(), msgUri, null, null);
                                        dialog.dismiss();
                                        activity.finish();
                                    }                                          
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    public static String getMmsDetail(Context context, Uri uri, int size, int msgBox) {
        Resources res = context.getResources();
        MultimediaMessagePdu msg;
        try {
            msg = (MultimediaMessagePdu) PduPersister.getPduPersister(
                    context).load(uri);
        } catch (MmsException e) {
            Log.e(TAG, "Failed to load the message: " + uri, e);
            return res.getString(R.string.cannot_get_details);
        }
        
        StringBuilder details = new StringBuilder();
        // Message Type: Text message.
        details.append(res.getString(R.string.message_type_label));
        details.append(res.getString(R.string.multimedia_message));

        if (msg instanceof RetrieveConf) {
            // From: ***
            String from = extractEncStr(context, ((RetrieveConf) msg).getFrom());
            details.append('\n');
            details.append(res.getString(R.string.from_label));
            details.append(!TextUtils.isEmpty(from)? from:
                                  res.getString(R.string.hidden_sender_address));
        }

        // To: ***
        details.append('\n');
        details.append(res.getString(R.string.to_address_label));
        EncodedStringValue[] to = msg.getTo();
        if (to != null) {
            details.append(EncodedStringValue.concat(to));
        }
        else {
            Log.w(TAG, "recipient list is empty!");
        }

        // Bcc: ***
        if (msg instanceof SendReq) {
            EncodedStringValue[] values = ((SendReq) msg).getBcc();
            if ((values != null) && (values.length > 0)) {
                details.append('\n');
                details.append(res.getString(R.string.bcc_label));
                details.append(EncodedStringValue.concat(values));
            }
        }

        // Date: ***
        details.append('\n');
        if (msgBox == Mms.MESSAGE_BOX_DRAFTS) {
            details.append(res.getString(R.string.saved_label));
        } else if (msgBox == Mms.MESSAGE_BOX_INBOX) {
            details.append(res.getString(R.string.received_label));
        } else {
            details.append(res.getString(R.string.sent_label));
        }

        details.append(MessageUtils.formatTimeStampString(
                context, msg.getDate() * 1000L, true));

        // Subject: ***
        details.append('\n');
        details.append(res.getString(R.string.subject_label));

        EncodedStringValue subject = msg.getSubject();
        if (subject != null) {
            String subStr = subject.getString();
            // Message size should include size of subject.
            size += subStr.length();
            details.append(subStr);
        }

        // Priority: High/Normal/Low
        details.append('\n');
        details.append(res.getString(R.string.priority_label));
        details.append(getPriorityDescription(context, msg.getPriority()));

        // Message size: *** KB
        details.append('\n');
        details.append(res.getString(R.string.message_size_label));
        details.append((size - 1)/1024 + 1);
        details.append(res.getString(R.string.kilobyte));

        return details.toString();
    }

    public static void updateNotification(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                MessagingNotification.blockingUpdateNewMessageIndicator(context, false, false);
                MessagingNotification.updateSendFailedNotification(context);
                MessagingNotification.updateDownloadFailedNotification(context);
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                    WapPushMessagingNotification.blockingUpdateNewMessageIndicator(context, false);
                }
                CBMessagingNotification.updateNewMessageIndicator(context);
            }
        }).start();
    }
    //add for cmcc dir ui end

    public static void addRemoveOldMmsThread(Runnable r) {
        mRemoveOldMmsThread = new Thread(r);
    }

    public static void asyncDeleteOldMms() {
        if (mRemoveOldMmsThread != null) {
            mRemoveOldMmsThread.start();
            mRemoveOldMmsThread = null;
        }
    }

    public static String detectCountry() {
        try {
            CountryDetector detector =
                (CountryDetector) MmsApp.getApplication().getSystemService(Context.COUNTRY_DETECTOR);
            final Country country = detector.detectCountry();
            if(country != null) {
                return country.getCountryIso();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatNumber(String number) {
        String match = null;
        match = PhoneNumberUtils.formatNumber(number, detectCountry());
        return match;
    }
//gionee gaoj 2012-3-22 added for CR00555790 start
    // Aurora xuyong 2015-04-16 added for yulore feature start
    private static boolean isAllNumber(String number) {
        char[] numbers = number.toCharArray();
        for (char c : numbers) {
            if ((c < '0' || c > '9') && c != '+') {
                return false;
            }
        }
        return true;
    }
    
    private static boolean startWithNum(String string) {
        char[] ca =  string.toCharArray();
        return  (ca[0] > '0' && ca[0] < '9') || ca[0] == '+';
    }
    // Aurora xuyong 2015-04-16 added for yulore feature end
    // Aurora xuyong 2014-07-19 modified for sougou start
    public static String getNumAreaFromAora(Context context, String number) {
        return Utils.getAreaName(context, number, Utils.RIGION_NAME_INDEX);
    }
    // Aurora xuyong 2015-04-16 added for yulore feature start
    public static String getNumAreaFromAora(final Context context, final Handler handler, final String addr, final String title) {
        if (handler != null) {
            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    String location = Utils.getAreaName(context, addr, Utils.RIGION_NAME_INDEX);
                    Contact contact = Contact.get(addr, true);
                    String subTitle = null;
                    if (contact.existsInDatabase()) {
                        if (location == null || "".equals(location)) {
                            subTitle = addr;
                        } else {
                            subTitle = addr + "  " + location;
                        }
                        // Aurora xuyong 2015-07-07 added for product review start
                        if (isAllNumber(title)) {
                            if (startWithNum(subTitle)) {
                                subTitle = " " + subTitle;
                            } else {
                                if (subTitle.contains(" ")) {
                                    subTitle = " " + subTitle;
                                }
                            }
                        }
                        // Aurora xuyong 2015-07-07 added for product review end
                    } else {
                        if (location != null && !"".equals(location)) {
                            subTitle = location;
                            if (isAllNumber(title)) {
                                if (startWithNum(subTitle)) {
                                    subTitle = " " + subTitle;
                                } else {
                                    if (subTitle.contains(" ")) {
                                        subTitle = " " + subTitle;
                                    }
                                }
                            }
                        } else {
                            subTitle = addr;
                            if (isAllNumber(title)) {
                                subTitle = " " + subTitle;
                            }
                        }
                    }
                    Message msg = handler.obtainMessage(ComposeMessageActivity.UPDATE_NUMBER_AREA);
                    msg.obj = subTitle;
                    msg.sendToTarget();
                }
                
            }).start();
        }
        return addr;
    }
    // Aurora xuyong 2015-04-16 added for yulore feature end
    // Aurora xuyong 2014-07-19 modified for sougou end
    /*public static String getRightNumArea(NumAreaInfo info) {
        String numArea = null;
        String numAreaHead = null;
        String numAreaBase = null;

        if (null == info) {
            return null;
        }
        numArea = info.toString();

        if (null != info.getiHeadInfo()) {
            numAreaHead = info.getiHeadInfo();
        }
        if (null != info.getiBaseInfo()) {
            numAreaBase = info.getiBaseInfo();
        }
        if (null != numArea) {
            if (null != numAreaHead && null != numAreaBase) {
                // Aurora xuyong 2013-09-25 modified for aurora's new feature start
                // Aurora xuyong 2013-10-17 modified for aurora's new feature start
                if (numAreaHead.length() > 0) {
                    numArea = numAreaHead + " " + numAreaBase;
                } else {
                    numArea = numAreaBase;
                }
                // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                // Aurora xuyong 2013-09-25 modified for aurora's new feature end
                if (numAreaBase.equals(numAreaHead)) {
                    numArea = numAreaBase;
                }
            } else if (null == numAreaHead && null != numAreaBase) {
                numArea = numAreaBase;
            } else if (null == numAreaBase && null != numAreaHead) {
                numArea = numAreaHead;
            }
        }
        return numArea;
    }*/
    public static void showSaveDraftConfirmDialog(Context context,
            OnClickListener listener) {
        new AuroraAlertDialog.Builder(context)//, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
//                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.gn_draft_operation)
                .setMessage(R.string.gn_draft_operation_reason)
                .setPositiveButton(R.string.gn_draft_save, listener)
                .setNeutralButton(R.string.discard, listener)
                .setNegativeButton(R.string.no, null)
                .show();
    }
    public static int getScreenWidth(Context context, DisplayMetrics dm) {
        int screenWidth = 0;
        boolean isPortrait = (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        dm = context.getApplicationContext().getResources().getDisplayMetrics();
        if (isPortrait) {
            screenWidth = dm.widthPixels;
        } else {
            screenWidth = dm.heightPixels;
        }
        return screenWidth;
    }
    
    public static float getRatio(Context context, DisplayMetrics dm) {
        WindowManager mWindowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        return 1.0f * dm.densityDpi / DisplayMetrics.DENSITY_MEDIUM;
    }

    //static SIMInfo mSimInfo = null;

    public static void setSimImageBg(Context context, int simId, ImageView imgView) {
        if (MmsApp.mGnMultiSimMessage) {
            SIMInfo siminfo = null;
            //gionee gaoj 2012-5-2 modified for CR00582563 start
            if (gnSimInfoMap.containsKey(simId)) {
                siminfo = gnSimInfoMap.get(simId);
            } else {
                siminfo = SIMInfo.getSIMInfoById(context, simId);
                gnSimInfoMap.put(simId, siminfo);
            }
            //gionee gaoj 2012-5-2 modified for CR00582563 end
            if (null != siminfo) {
//                imgView.setImageResource(getSimDrawbleId(siminfo.mSlot + 1, siminfo.mColor));
                imgView.setPadding(0, 0, 0, 0);
                imgView.setVisibility(View.VISIBLE);
            } else {
                imgView.setVisibility(View.GONE);
            } 
        } else {
            imgView.setVisibility(View.GONE);
        }
    }

    // Aurora yudingmin 2014-10-27 added for sim start
    private static SIMInfo getSIMInfo(Context context, long simId){
        SIMInfo simInfo = MmsApp.mSimInfoMap.get(simId);
        if(simInfo == null){
            simInfo = SIMInfo.getSIMInfoById(context, simId);
            MmsApp.mSimInfoMap.put(simId, simInfo);
        }
        return simInfo;
    }
    // Aurora xuyong 2014-11-28 added for bug #10174 start
    private static SIMInfo getNotifySIMInfo(Context context, long simId){
        return SIMInfo.getSIMInfoById(context, simId);
    }
    // Aurora xuyong 2014-11-28 added for bug #10174 end
    // Aurora yudingmin 2014-10-27 added for sim end
    // Aurora xuyong 2014-11-17 added for bug #9803 start
    public static void removeSIMInfo(SIMInfo simInfo) {
        if (simInfo != null) {
            long id = simInfo.mSimId;
            if (MmsApp.mSimInfoMap.get(id) != null) {
                MmsApp.mSimInfoMap.remove(id);
            }
        }
    }
    // Aurora xuyong 2014-12-23 added for aurora's new feature start
    public static void clearSIMInfos() {
        if (MmsApp.mSimInfoMap != null && MmsApp.mSimInfoMap.size() > 0) {
            MmsApp.mSimInfoMap.clear();
        }
    }
    // Aurora xuyong 2014-12-23 added for aurora's new feature end
    public static void addSIMInfo(SIMInfo simInfo) {
        if (simInfo != null) {
            long id = simInfo.mSimId;
            if (MmsApp.mSimInfoMap.get(id) != null) {
                MmsApp.mSimInfoMap.remove(id);
            }
            MmsApp.mSimInfoMap.put(id, simInfo);
        }
    }
    // Aurora xuyong 2014-11-17 added for bug #9803 end
    public static CharSequence getZeroSimInfo(Context context, int simId){
        boolean isEnglish = false;
        //get sim info
        SIMInfo simInfo = SIMInfo.getSIMInfoById(context, simId);
        if(null != simInfo){
            String displayName = simInfo.mDisplayName;
            /*if (MmsApp.DEBUG) {
                Log.d(TAG, "SIMInfo simId=" + simInfo.mSimId + " mDisplayName=" + displayName);
            }*/
            if(null == displayName){
                simInfoMap.put(simId, "");
                return "";
            }

            if (displayName.getBytes().length != displayName.length()) {
                isEnglish = false;
            } else {
                isEnglish = true;
            }

            SpannableStringBuilder buf = new SpannableStringBuilder();
            buf.append(" ");
            if(displayName.length() <= 18){
                if (isEnglish) {
                    buf.append(displayName);
                } else {
                    if (displayName.length() > 10) {
                        buf.append(displayName.substring(0, 10) + "...");
                    } else {
                        buf.append(displayName);
                    }
                }
            }else{
                if (isEnglish) {
                    buf.append(displayName.substring(0, 18) + "...");
                } else {
                    buf.append(displayName.substring(0, 10) + "...");
                }
            }
            buf.append(" ");

            //set background image
            int colorRes = simInfo.mSimBackgroundRes;
            Drawable drawable = context.getResources().getDrawable(colorRes);
            buf.setSpan(new GnBackgroundImageSpan(colorRes,drawable), 0, buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //set simInfo color
            int color = context.getResources().getColor(R.color.siminfo_color);
            buf.setSpan(new ForegroundColorSpan(color), 0, buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //buf.setSpan(new StyleSpan(Typeface.BOLD),0, buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            simInfoMap.put(simId, buf);
            return buf;
        }
        simInfoMap.put(simId, "");
        return "";
    }
    
    private static long sLastcurTimeMillis = 0;
    private static String sToday; 
    private static String sYesterday;
    public static String newformatGNTime(Context context, long l) {
        String sRet = null;
        //String sToday;// = DateFormat.format("yyyy-MM-dd", System.currentTimeMillis()).toString();
        //String sThisyear;// = DateFormat.format("yyyy", System.currentTimeMillis()).toString();
        //String sYesterday;// = DateFormat.format("yyyy-MM-dd",
                //System.currentTimeMillis() - 86400 * 1000).toString();
        String sTemp = null;

        String strTimeFormat = Settings.System.getString(
                context.getContentResolver(), Settings.System.TIME_12_24);
        boolean isZh = Locale.getDefault().getLanguage().equals("zh");
        boolean monthTag = false;
        boolean yearTag = false;
        
        if (sLastcurTimeMillis == 0 || System.currentTimeMillis()  - sLastcurTimeMillis > 500) {
            sLastcurTimeMillis = System.currentTimeMillis();  
            
            if (isZh) {
                sToday = DateFormat.format(context.getResources().getString(R.string.gn_date_format),
                        System.currentTimeMillis()).toString();
                sYesterday = DateFormat.format(
                        context.getResources().getString(R.string.gn_date_format),
                        System.currentTimeMillis() - 86400 * 1000).toString();
            } else {
                sToday = DateFormat.format("yyyy-MM-dd", System.currentTimeMillis()).toString();
    //            sThisyear = DateFormat.format("yyyy", System.currentTimeMillis()).toString();
                sYesterday = DateFormat.format("yyyy-MM-dd",
                        System.currentTimeMillis() - 86400 * 1000).toString();
            }
        }
        String sPrefix = "";
        String sTime = "";
        String sDate = "";
        String sZhAPM = "";

        if (0 != l) {
            if (isZh) {
                sTemp = DateFormat.format(
                        context.getResources().getString(R.string.gn_date_format), l).toString();
            } else {
                sTemp = DateFormat.format("yyyy-MM-dd", l).toString();
            }
            
            if (sTemp.startsWith(sToday)) {
                // show hh:mm
                sPrefix = "";
            } else if (sTemp.startsWith(sYesterday)) {
                // show
                sPrefix = context.getResources().getString(R.string.gn_yesterday);
            } else if (sTemp.startsWith(sToday.substring(0, 4))) {
                // show MM-dd hh:mm
                if (isZh) {
                    sDate = sTemp.substring(5);
                } else {
                    sDate = DateFormat.format("MM/dd", l).toString();
                }
                monthTag = true;
            } else {
                sDate = DateFormat.getDateFormat(context).format(l);
                yearTag = true;
            }

            if (monthTag) {
                sRet = sDate;
            } else if (yearTag) {
                sRet = sDate;
            } else {
                if (null != strTimeFormat && strTimeFormat.equals("12")) {
                    if (Locale.getDefault().getLanguage().equals("zh")) {
                        sTime = "hh:mm";
                        String sHour = DateFormat.format("kk", l).toString();
                        Long lHour = Long.valueOf(sHour);
                        // Gionee: 20121107 xuyongji add for CR00725775 begin
                        if (gnCommonAPM) {
                            if (0 <= lHour && 12 > lHour) {
                                sZhAPM = context.getResources().getString(R.string.gn_am);
                            } else if (12 <= lHour) {
                                sZhAPM = context.getResources().getString(R.string.gn_pm);
                            }
                        } else {
                        // Gionee: 20121107 xuyongji add for CR00725775 end
                            if (0 == lHour) {
                                sZhAPM = context.getResources().getString(R.string.gn_midnight);
                            } else if (1 <= lHour && 6 > lHour) {
                                sZhAPM = context.getResources().getString(R.string.gn_dawn);
                            } else if (6 <= lHour && 12 > lHour) {
                                sZhAPM = context.getResources().getString(R.string.gn_am);
                            } else if (12 == lHour) {
                                sZhAPM = context.getResources().getString(R.string.gn_noon);
                            } else if (13 <= lHour && 18 > lHour) {
                                sZhAPM = context.getResources().getString(R.string.gn_pm);
                            } else if (18 == lHour) {
                                sZhAPM = context.getResources().getString(R.string.gn_dusk);
                            } else {
                                sZhAPM = context.getResources().getString(R.string.gn_night);
                            }
                        // Gionee: 20121107 xuyongji add for CR00725775 begin
                        }
                        // Gionee: 20121107 xuyongji add for CR00725775 end
                    } else {
                        sTime = "hh:mm aaa";
                    }

                } else if (null != strTimeFormat && strTimeFormat.equals("24")) {
                    sTime = "kk:mm";
                } else {
                    if (isZh) {
                        sTime = "hh:mm";
                        String sHour = DateFormat.format("kk", l).toString();
                        Long lHour = Long.valueOf(sHour);
                        // Gionee: 20121107 xuyongji add for CR00725775 begin
                        if (gnCommonAPM) {
                            if (0 <= lHour && 12 > lHour) {
                                sZhAPM = context.getResources().getString(R.string.gn_am);
                            } else if (12 <= lHour) {
                                sZhAPM = context.getResources().getString(R.string.gn_pm);
                            }
                        } else {
                        // Gionee: 20121107 xuyongji add for CR00725775 end
                            if (0 == lHour) {
                                sZhAPM = context.getResources().getString(R.string.gn_midnight);
                            } else if (1 <= lHour && 6 > lHour) {
                                sZhAPM = context.getResources().getString(R.string.gn_dawn);
                            } else if (6 <= lHour && 12 > lHour) {
                                sZhAPM = context.getResources().getString(R.string.gn_am);
                            } else if (12 == lHour) {
                                sZhAPM = context.getResources().getString(R.string.gn_noon);
                            } else if (13 <= lHour && 18 > lHour) {
                                sZhAPM = context.getResources().getString(R.string.gn_pm);
                            } else if (18 == lHour) {
                                sZhAPM = context.getResources().getString(R.string.gn_dusk);
                            } else {
                                sZhAPM = context.getResources().getString(R.string.gn_night);
                            }
                        // Gionee: 20121107 xuyongji add for CR00725775 begin
                        }
                        // Gionee: 20121107 xuyongji add for CR00725775 end
                    } else {
                        sTime = "hh:mm aaa";
                    }
                }
                
                if (TextUtils.isEmpty(sPrefix) && TextUtils.isEmpty(sDate)) {
                    sRet = sZhAPM + " " + DateFormat.format(sTime, l).toString();
                } else if (TextUtils.isEmpty(sPrefix) && TextUtils.isEmpty(sDate)
                        && TextUtils.isEmpty(sZhAPM)) {
                    sRet = DateFormat.format(sTime, l).toString();
                } else {
                    if (isZh) {
                        sRet = sPrefix + sDate + sZhAPM + " "
                                + DateFormat.format(sTime, l).toString();
                    } else {
                        sRet = sPrefix + sDate + " " + sZhAPM + " "
                                + DateFormat.format(sTime, l).toString();
                    }
                }
            }
        }
        return sRet;
    }

    private static java.text.DateFormat sDateFormate = null;
    public static String detailFormatGNTime(Context context, long l) {
        String sRet = null;
        String sTemp = null;
        
        String strTimeFormat = android.provider.Settings.System.getString(
                context.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        String sTime = "";
        String sDate = "";
        String sZhAPM = "";
        boolean isZh = Locale.getDefault().getLanguage().equals("zh");
        if (0 != l) {
            if (sDateFormate == null) {
                sDateFormate = DateFormat.getDateFormat(context);
            }
            sTemp = sDateFormate.format(l);
            sDate = sTemp;
            if (null != strTimeFormat && strTimeFormat.equals("12")) {
                if (isZh) {
                    sTime = "hh:mm";
                    String sHour = DateFormat.format("kk", l).toString();
                    Long lHour = Long.valueOf(sHour);
                    // Gionee: 20121107 xuyongji add for CR00725775 begin
                    if (gnCommonAPM) {
                        if (0 <= lHour && 12 > lHour) {
                            sZhAPM = context.getResources().getString(R.string.gn_am);
                        } else if (12 <= lHour) {
                            sZhAPM = context.getResources().getString(R.string.gn_pm);
                        }
                    } else {
                    // Gionee: 20121107 xuyongji add for CR00725775 end
                        if (0 == lHour) {
                            sZhAPM = context.getResources().getString(R.string.gn_midnight);
                        } else if (1 <= lHour && 6 > lHour) {
                            sZhAPM = context.getResources().getString(R.string.gn_dawn);
                        } else if (6 <= lHour && 12 > lHour) {
                            sZhAPM = context.getResources().getString(R.string.gn_am);
                        } else if (12 == lHour) {
                            sZhAPM = context.getResources().getString(R.string.gn_noon);
                        } else if (13 <= lHour && 18 > lHour) {
                            sZhAPM = context.getResources().getString(R.string.gn_pm);
                        } else if (18 == lHour) {
                            sZhAPM = context.getResources().getString(R.string.gn_dusk);
                        } else {
                            sZhAPM = context.getResources().getString(R.string.gn_night);
                        }
                    // Gionee: 20121107 xuyongji add for CR00725775 begin
                    }
                    // Gionee: 20121107 xuyongji add for CR00725775 end
                } else {
                    sTime = "hh:mm aaa";
                }

            } else if (null != strTimeFormat && strTimeFormat.equals("24")) {
                sTime = "kk:mm";
            } else {
                if (isZh) {
                    sTime = "hh:mm";
                    String sHour = DateFormat.format("kk", l).toString();
                    Long lHour = Long.valueOf(sHour);
                    // Gionee: 20121107 xuyongji add for CR00725775 begin
                    if (gnCommonAPM) {
                        if (0 <= lHour && 12 > lHour) {
                            sZhAPM = context.getResources().getString(R.string.gn_am);
                        } else if (12 <= lHour) {
                            sZhAPM = context.getResources().getString(R.string.gn_pm);
                        }
                    } else {
                    // Gionee: 20121107 xuyongji add for CR00725775 end
                        if (0 == lHour) {
                            sZhAPM = context.getResources().getString(R.string.gn_midnight);
                        } else if (1 <= lHour && 6 > lHour) {
                            sZhAPM = context.getResources().getString(R.string.gn_dawn);
                        } else if (6 <= lHour && 12 > lHour) {
                            sZhAPM = context.getResources().getString(R.string.gn_am);
                        } else if (12 == lHour) {
                            sZhAPM = context.getResources().getString(R.string.gn_noon);
                        } else if (13 <= lHour && 18 > lHour) {
                            sZhAPM = context.getResources().getString(R.string.gn_pm);
                        } else if (18 == lHour) {
                            sZhAPM = context.getResources().getString(R.string.gn_dusk);
                        } else {
                            sZhAPM = context.getResources().getString(R.string.gn_night);
                        }
                    // Gionee: 20121107 xuyongji add for CR00725775 begin
                    }
                    // Gionee: 20121107 xuyongji add for CR00725775 end
                } else {
                    sTime = "hh:mm aaa";
                }
            }

            if (!sZhAPM.equals("")) {
                sRet = sDate + " " + sZhAPM + " " + DateFormat.format(sTime, l).toString();
            } else {
                sRet = sDate + " " + DateFormat.format(sTime, l).toString();
            }

        }

        return sRet;
    }
/*
    static int getSimDrawbleId(int slot, int color){
        int[][] drawbleIds = { 
                {
                    R.drawable.gn_sim_unkown_color0,
                    R.drawable.gn_sim_unkown_color1,
                    R.drawable.gn_sim_unkown_color2,
                    R.drawable.gn_sim_unkown_color3 
                                                    },
                {
                    R.drawable.gn_sim_a_color0,
                    R.drawable.gn_sim_a_color1,
                    R.drawable.gn_sim_a_color2,
                    R.drawable.gn_sim_a_color3 
                                                },
                {
                    R.drawable.gn_sim_b_color0,
                    R.drawable.gn_sim_b_color1,
                    R.drawable.gn_sim_b_color2,
                    R.drawable.gn_sim_b_color3 
                                                }
                              };
        return drawbleIds[slot][color];
    }*/
    //gionee gaoj 2012-3-22 added for CR00555790 end
     
    //ALPS00289861
    /**
     * Get an unique name . It is different with the existed names.
     * 
     * @param names
     * @param fileName
     * @return
     */
    public static String getUniqueName(String names[], String fileName) {
        if (names == null || names.length == 0) {
            return fileName;
        }
        int mIndex = 0;
        String tempName = "";
        String finalName = fileName;
        String extendion = "";
        String fileNamePrefix = "";
        int fileCount = 0;
        while (mIndex < names.length) {
            tempName = names[mIndex];
            if (tempName != null && tempName.equals(finalName)) {
                fileCount++;
                int tempInt = fileName.lastIndexOf(".");
                extendion = fileName.substring(tempInt,fileName.length());
                fileNamePrefix = fileName.substring(0,tempInt);
                finalName = fileNamePrefix + "(" + fileCount + ")" + extendion;
                mIndex = 0;
            } else {
                mIndex++;
            }
        }
        return finalName;
    }

    //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
    static private int sLasThemeTag = 0;
    static private int mSimTextColor = 0;
    //Gionee <gaoj> <2013-05-13> added for CR00811367 end

    //gionee gaoj 2012-5-24 added for CR00588933 start
    public static void setSimTextBg(Context context, int simId, TextView textView, boolean isOut) {

        //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
        if (mSimTextColor == 0 || sLasThemeTag != MmsApp.sThemeChangTag) {
            if (MmsApp.mLightTheme) {
                mSimTextColor = context.getResources().getColor(R.color.gn_msg_simcard_color_dark);
            } else {
                mSimTextColor = context.getResources().getColor(R.color.gn_msg_simcard_color_white);
            }
            sLasThemeTag = MmsApp.sThemeChangTag;
        }
        //Gionee <gaoj> <2013-05-13> added for CR00811367 end
        if (MmsApp.mGnMultiSimMessage) {
            SIMInfo siminfo = null;
            if (gnSimInfoMap.containsKey(simId)) {
                siminfo = gnSimInfoMap.get(simId);
            } else {
                siminfo = SIMInfo.getSIMInfoById(context, simId);
                gnSimInfoMap.put(simId, siminfo);
            }

            //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
            if (MmsApp.mGnPerfList) {
                textView.setTextColor(mSimTextColor);
            } else {
                 //Gionee <gaoj> <2013-05-13> added for CR00811367 end
            //gionee gaoj 2013-3-21 modified for CR00787217 start
            //gionee gaoj 2012-6-25 added for CR00628364 start
            if (MmsApp.mDarkStyle) {
                //gionee gaoj 2012-9-10 modified for CR00689513 start
                textView.setTextColor(context.getResources().getColor(R.color.gn_msg_simcard_color_white));
//                textView.setTextColor(Color.GRAY);
                //gionee gaoj 2012-9-10 modified for CR00689513 end
            } else if (MmsApp.mLightTheme) {
                textView.setTextColor(context.getResources().getColor(R.color.gn_msg_simcard_color_dark));
            }
            //gionee gaoj 2012-6-25 added for CR00628364 end
            //gionee gaoj 2013-3-21 modified for CR00787217 end
            //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
            }
            //Gionee <gaoj> <2013-05-13> added for CR00811367 end
            String text = null;
            if (null != siminfo) {
                if (siminfo.mSlot != -1) {
                    if (siminfo.mSlot == 0) {
                        if(mUnicomCustom || mShowDigitalSlot) {
                            text = context.getString(isOut ? R.string.gn_card_1_out : R.string.gn_card_1_in);
                        } else {
                            text = context.getString(isOut ? R.string.gn_card_a_out : R.string.gn_card_a_in);
                        }
                    } else {
                        if(mUnicomCustom || mShowDigitalSlot) {
                            text = context.getString(isOut ? R.string.gn_card_2_out : R.string.gn_card_2_in);
                        } else {
                            text = context.getString(isOut ? R.string.gn_card_b_out : R.string.gn_card_b_in);
                        }
                    }
                } else {
                    text = context.getString(isOut ? R.string.gn_card_unknow_out : R.string.gn_card_unknow_in);
                }
                if (siminfo.mSimBackgroundRes != 0 && siminfo.mSlot != -1) {

                    Bitmap cardBitmap = BitmapFactory.decodeResource(context.getResources(), siminfo.mSimBackgroundRes);
                    int color = cardBitmap.getPixel(3, 3);

                    SpannableStringBuilder style = new SpannableStringBuilder(text);

                    //Gionee:tianxiaolong 2012.8.2 modify for CR00663594 begin
                    if(gnFlyFlag != true){
                         //Gionee qinkai 2012-09-05 added for CR00679226 start
                         if(gnOverseaflag == true){
                               if(siminfo.mSlot != -1){
                                //Gionee:xuyongji 2012.9.20 add for CR00692588 begin
                                       if(Locale.getDefault().getLanguage().equals("zh"))
                                       {
                                           style.setSpan(new ForegroundColorSpan(color), 
                                            0, 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                                       }
                                       else
                                       {
                                           style.setSpan(new ForegroundColorSpan(color), 
                                            text.length() - 1, text.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                                       }  
                                //Gionee:xuyongji 2012.9.20 add for CR00692588 end
                               }
                               else{
                                       style.setSpan(new ForegroundColorSpan(color), 
                                       text.length() - 3, text.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                               } 
                        }
                        else{
                                style.setSpan(new ForegroundColorSpan(color), 
                                    0, 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        }
                        //Gionee qinkai 2012-09-05 added for CR00679226 end
                    }
                    //Gionee:tianxiaolong 2012.8.2 modify for CR00663594 end
                    textView.setText(style);
                } else {
                    textView.setText(text);
                }
                textView.setPadding(0, 0, 0, 0);
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
            } 
        } else {
            textView.setVisibility(View.GONE);
        }
    }
    //gionee gaoj 2012-5-24 added for CR00588933 end

    //gionee gaoj 2012-8-7 added for CR00671408 start
    public static ResolveInfo getResolveInfo(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        List<ResolveInfo> rList = null;
        if (intent != null) {
            rList = pm.queryIntentActivities(intent, PackageManager.GET_RECEIVERS);
        }
        if ((rList != null) && (rList.size() > 0)) {
            return rList.get(0);
        } else {
            return null;
        }
    }
    //gionee gaoj 2012-8-7 added for CR00671408 end

    //gionee gaoj 2013-1-21 added for CR00764025 start
    public static boolean getIntentActivities(Context context, Intent intent) {
        if (intent != null) {
            PackageManager pm = context.getPackageManager();
            final List<ResolveInfo> matches = pm.queryIntentServices(intent, PackageManager.GET_SERVICES);
            // Pick first match, otherwise best found
            ResolveInfo bestResolve = null;
            final int size = matches.size();
            if (size > 0) {
             return true;
            }
        }
        return false;
    }
    //gionee gaoj 2013-1-21 added for CR00764025 end

    //gionee wangym 2012-11-22 add for CR00735223 start   
    private final static float DEFAULT_TEXT_SIZE = 20;
    private final static float MIN_TEXT_SIZE = 10;
    private final static float MAX_TEXT_SIZE = 32;
    public static float getTextSize(Context context){
        //SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(context);
        float size = DEFAULT_TEXT_SIZE;//sp.getFloat(MessagingPreferenceActivity.TEXT_SIZE, DEFAULT_TEXT_SIZE);
        if(size < MIN_TEXT_SIZE){
            size = MIN_TEXT_SIZE;
        }else if(size > MAX_TEXT_SIZE){
            size = MAX_TEXT_SIZE;
        }
        return size;
    }
    
    public static void setTextSize(Context context, float size){        
        if(size < MIN_TEXT_SIZE){
            size = MIN_TEXT_SIZE;
        }else if(size > MAX_TEXT_SIZE){
            size = MAX_TEXT_SIZE;
        }
       /* SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(MessagingPreferenceActivity.TEXT_SIZE, size);
        editor.commit();*/
    }
    //gionee wangym 2012-11-22 add for CR00735223 end
    
    //Gionee <zhouyj> <2013-04-25> add for CR00802357 start
    public static boolean isPackageExist(Context context, String packageName) {
        PackageInfo packageInfo = null;
        try { 
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
            Log.e(TAG, "isPackageFound   " + packageName + " is not found!");
        }
        return packageInfo != null;
    }
    
    public static String getPackageVerson(Context context, String packageName) {
        PackageInfo packageInfo = null;
        try { 
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
            Log.e(TAG, "getPackageVerson   " + packageName + " is not found!");
        }
        if (packageInfo != null) {
            return packageInfo.versionName;
        }
        return null;
    }
    //Gionee <zhouyj> <2013-04-25> add for CR00802357 end
    
    //Gionee <lwzh> <2013-04-29> add for CR00803696 begin
    public static String gnGetRingtoneTile(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }
        
        Cursor cursor = null;
        ContentResolver res = context.getContentResolver();
        
        String title = null;

        String authority = uri.getAuthority();
        if (MediaStore.AUTHORITY.equals(authority)) {
            String[] MEDIA_COLUMNS = new String[] {
                    MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.TITLE
            };
            cursor = res.query(uri, MEDIA_COLUMNS, null, null, null);
        }

        try {
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                return cursor.getString(2);
            } else {
                // title =
                // context.getResources().getString(R.string.gnDefaultLabel);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return title;
    }
    
    public static boolean isRingtoneExist(Context context, Uri uri) {
        if (context == null || uri == null) {
            return false;
        }
        
        try {
            AssetFileDescriptor fd = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            if (fd == null) {
                return false;
            } else {
                try {
                    fd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
    //Gionee <lwzh> <2013-04-29> add for CR00803696 end
    // Aurora xuyong 2014-05-23 added for multisim feature start
    // Aurora xuyong 2014-06-06 added for bug #5369 start
    //private static final int mSimIconType = 0;
    private static final int mSimBigIconType = 1;
    //private static final int mSimPresendType = 2;
    // Aurora xuyong 2014-06-19 added for multisim feature start
    private static final int mSimNotificationType = 3;
    // Aurora xuyong 2014-06-19 added for multisim feature end
    // Aurora xuyong 2014-07-29 added for aurora's new feature start
    private static final int mSendMsgType = 4;
    // Aurora xuyong 2014-07-29 added for aurora's new feature end
    // Aurora xuyong 2014-06-06 added for bug #5369 end
    /*public static int getSimIcon(Context mContext, int simId) {
        int result = -1;
        if(simId > 0){
            int slot = 0;
            // Aurora yudingmin 2014-10-27 modified for sim start
            SIMInfo simInfo = getSIMInfo(mContext, simId);
            // Aurora yudingmin 2014-10-27 modified for sim end
            if (simInfo != null) {
              // Aurora xuyong 2014-06-06 modified for bug #5369 start       
                result = color2resId(simInfo.mColor, mSimIconType);
              // Aurora xuyong 2014-06-06 modified for bug #5369 end
                slot = simInfo.mSlot;
            }
               if(result == -1) {    
                if(slot == 1) {
                    result = R.drawable.aurora_sim2_thumbnail;
                } else {
                    result = R.drawable.aurora_sim1_thumbnail;
                }    
            }
        }
        
        return result;
    }*/
    
    public static int getSimBigIcon(Context mContext, int simId) {
        int result = -1;
        if(simId > 0){
            // Aurora xuyong 2014-12-23 modified for aurora's new feature start
            int slot = -1;
            // Aurora xuyong 2014-12-23 modified for aurora's new feature end
            // Aurora yudingmin 2014-10-27 modified for sim start
            SIMInfo simInfo = getSIMInfo(mContext, simId);
            // Aurora yudingmin 2014-10-27 modified for sim end
            if (simInfo != null) {          
                // Aurora xuyong 2014-06-06 modified for bug #5369 start
                // Aurora xuyong 2014-12-23 deleted for aurora's new feature start
                //result = color2resId(simInfo.mColor, mSimBigIconType);
                // Aurora xuyong 2014-12-23 deleted for aurora's new feature end
                // Aurora xuyong 2014-06-06 modified for bug #5369 end
                slot = simInfo.mSlot;
            }
            // Aurora xuyong 2014-09025 deleted for android 4.4 feature start
            // Aurora xuyong 2014-12-23 modified for aurora's new feature start
            if(result == -1) {
            // Aurora xuyong 2014-12-23 modified for aurora's new feature end
                if(slot == 1) {
                    result = R.drawable.aurora_sim2;
                } else if (slot == 0) {
                    result = R.drawable.aurora_sim1;
                }   
            }
            // Aurora xuyong 2014-12-23 deleted for aurora's new feature start
            //*/
            // Aurora xuyong 2014-12-23 deleted for aurora's new feature end
            // Aurora xuyong 2014-09025 deleted for android 4.4 feature end
        }
        return result;
    }
    // Aurora xuyong 2014-07-29 added for aurora's new feature start
    private static boolean mImageEnabled = false;
    public static int getSendIcon(Context mContext, int simId, boolean isEnabled) {
        mImageEnabled = isEnabled;
        int result = -1;
        // Aurora xuyong 2014-12-23 modified for aurora's new feature start
        int slot = -1;
        // Aurora xuyong 2014-12-23 modified for aurora's new feature end
        // Aurora yudingmin 2014-10-27 modified for sim start
        SIMInfo simInfo = getSIMInfo(mContext, simId);
        // Aurora yudingmin 2014-10-27 modified for sim end
        if (simInfo != null) {
            // Aurora xuyong 2014-12-23 deleted for aurora's new feature start        
            //result = color2resId(simInfo.mColor, mSendMsgType);
            // Aurora xuyong 2014-12-23 deleted for aurora's new feature end
            slot = simInfo.mSlot;
        }
        
        if(result == -1) {    
            if(slot == 1) {
                result = isEnabled ? R.drawable.aurora_send_sim2 : R.drawable.aurora_send_sim2_trans;
            } else if (slot == 0) {
                result = isEnabled ? R.drawable.aurora_send_sim1 : R.drawable.aurora_send_sim1_trans;
            }   
        }
        
        return result;
    }
    // Aurora xuyong 2014-07-29 added for aurora's new feature end
    // Aurora xuyong 2014-06-06 added for bug #5369 start
    /*public static int getSimPreIcon(Context mContext, int simId) {
        int result = -1;
        int slot = 0;
        // Aurora yudingmin 2014-10-27 modified for sim start
        SIMInfo simInfo = getSIMInfo(mContext, simId);
        // Aurora yudingmin 2014-10-27 modified for sim end
        if (simInfo != null) {          
            result = color2resId(simInfo.mColor, mSimPresendType);
            slot = simInfo.mSlot;
        }
        
        if(result == -1) {    
            if(slot == 1) {
                result = R.drawable.aurora_sim2_presend;
            } else {
                result = R.drawable.aurora_sim1_presend;
            }   
        }
        
        return result;
    }*/
    // Aurora xuyong 2014-06-06 added for bug #5369 end
    // Aurora xuyong 2014-06-19 added for multisim feature start
    public static int getSimNotifiIcon(Context mContext, int simId) {
        int result = -1;
        // Aurora xuyong 2014-12-23 modified for aurora's new feature start
        int slot = -1;
        // Aurora xuyong 2014-12-23 modified for aurora's new feature end
        // Aurora yudingmin 2014-10-27 modified for sim start
        // Aurora xuyong 2014-11-28 modified for bug #10174 start
        SIMInfo simInfo = getNotifySIMInfo(mContext, simId);
        // Aurora xuyong 2014-11-28 modified for bug #10174 end
        // Aurora yudingmin 2014-10-27 modified for sim end
        if (simInfo != null) {     
            // Aurora xuyong 2014-12-23 deleted for aurora's new feature start 
            //result = color2resId(simInfo.mColor, mSimNotificationType);
            // Aurora xuyong 2014-12-23 deleted for aurora's new feature end
            slot = simInfo.mSlot;
        }
           if(result == -1) {    
            if(slot == 1) {
                result = R.drawable.aurora_sim2_notification;
            } else if (slot == 0) {
                result = R.drawable.aurora_sim1_notification;
            }    
        }
        
        return result;
    }
    // Aurora xuyong 2014-06-19 added for multisim feature end
    public static int color2resId(int color, int type) {
        int result = -1 ;
       // Aurora xuyong 2014-06-06 modified for bug #5369 start
        switch (type) {
        /*case mSimIconType:
            switch (color) {
            case 0: {
                result = R.drawable.aurora_sim1_thumbnail;
                break;
            }
            case 1: {
                result = R.drawable.aurora_sim2_thumbnail;
                break;
            }         
            case 2: {
                result = R.drawable.aurora_sim_net_thumbnail;
                break;
            }
            case 3: {
                result = R.drawable.aurora_sim_family_thumbnail;
                break;
            }
            case 4: {
                result = R.drawable.aurora_sim_office_thumbnail;
                break;
            }         
            case 5: {
                result = R.drawable.aurora_sim_phone_thumbnail;
                break;
            }
            }
            break;*/
        case mSimBigIconType:
            switch (color) {
            case 0: {
                result = R.drawable.aurora_sim1;
                break;
            }
            case 1: {
                result = R.drawable.aurora_sim2;
                break;
            }         
            /*case 2: {
                result = R.drawable.aurora_sim_net;
                break;
            }
            case 3: {
                result = R.drawable.aurora_sim_family;
                break;
            }
            case 4: {
                result = R.drawable.aurora_sim_office;
                break;
            }         
            case 5: {
                result = R.drawable.aurora_sim_phone;
                break;
            }*/
            }
            break;
        /*case mSimPresendType:
            switch (color) {
            case 0: {
                result = R.drawable.aurora_sim1_presend_selector;
                break;
            }
            case 1: {
                result = R.drawable.aurora_sim2_presend_selector;
                break;
            }         
            case 2: {
                result = R.drawable.aurora_net_presend_selector;
                break;
            }
            case 3: {
                result = R.drawable.aurora_family_presend_selector;
                break;
            }
            case 4: {
                result = R.drawable.aurora_office_presend_selector;
                break;
            }         
            case 5: {
                result = R.drawable.aurora_phone_presend_selector;
                break;
            }
            }
            break;*/
          // Aurora xuyong 2014-06-06 modified for bug #5369 end
      // Aurora xuyong 2014-06-19 added for multisim feature start
        case mSimNotificationType:
            switch (color) {
            case 0: {
                result = R.drawable.aurora_sim1_notification;
                break;
            }
            case 1: {
                result = R.drawable.aurora_sim2_notification;
                break;
            }         
            /*case 2: {
                result = R.drawable.aurora_sim_net_notification;
                break;
            }
            case 3: {
                result = R.drawable.aurora_sim_family_notification;
                break;
            }
            case 4: {
                result = R.drawable.aurora_sim_office_notification;
                break;
            }         
            case 5: {
                result = R.drawable.aurora_sim_phone_notification;
                break;
            }*/
            }
            break;
       // Aurora xuyong 2014-07-29 added for aurora's new feature start
        case mSendMsgType:
            switch (color) {
            case 0: {
                result = mImageEnabled ? R.drawable.aurora_send_sim1 : R.drawable.aurora_send_sim1_trans;
                break;
            }
            case 1: {
                result = mImageEnabled ? R.drawable.aurora_send_sim2 : R.drawable.aurora_send_sim2_trans;
                break;
            }/*         
            case 2: {
                result = mImageEnabled ? R.drawable.aurora_send_net : R.drawable.aurora_send_net_trans;
                break;
            }
            case 3: {
                result = mImageEnabled ? R.drawable.aurora_send_family : R.drawable.aurora_send_family_trans;
                break;
            }
            case 4: {
                result = mImageEnabled ? R.drawable.aurora_send_office : R.drawable.aurora_send_office_trans;
                break;
            }         
            case 5: {
                result = mImageEnabled ? R.drawable.aurora_send_phone : R.drawable.aurora_send_phone_trans;
                break;
            }*/
            }
            break;
        // Aurora xuyong 2014-07-29 added for aurora's new feature end
        // Aurora xuyong 2014-06-19 added for multisim feature end
        }

        return result;
    }
    // Aurora xuyong 2014-05-23 added for multisim feature end
}

/**
 * The common code about delete progress dialogs.
 */
class DeleteProgressDialogUtil {
    /**
     * Gets a delete progress dialog.
     * @param context the activity context.
     * @return the delete progress dialog.
     */
    public static AuroraProgressDialog getProgressDialog(Context context) {
        AuroraProgressDialog dialog = new AuroraProgressDialog(context);
        dialog.setProgressStyle(AuroraProgressDialog.STYLE_SPINNER);
        dialog.setMessage(context.getString(R.string.deleting));
        dialog.setMax(1); /* default is one complete */
        return dialog;
    }
}

