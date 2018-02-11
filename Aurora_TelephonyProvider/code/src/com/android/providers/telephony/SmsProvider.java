/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.providers.telephony;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
// Aurora xuyong 2014-10-28 added for privacy feature start
import android.database.sqlite.SQLiteException;
// Aurora xuyong 2014-10-28 added for privacy feature end
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
// Aurora xuyong 2014-06-11 added for android4.4 new feature start
import android.os.Binder;
// Aurora xuyong 2014-06-11 added for android4.4 new feature end
// Aurora xuyong 2014-06-17 added for adapt for both 4.3 & 4.4 feature start
import android.os.Build;
// Aurora xuyong 2014-06-17 added for adapt for both 4.3 & 4.4 feature end
import android.os.SystemProperties;
import android.provider.Contacts;
import android.provider.Telephony;
import android.provider.ContactsContract.Data;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.TextBasedSmsColumns;
import android.provider.Telephony.CanonicalAddressesColumns;
import android.provider.Telephony.ThreadsColumns;
import gionee.provider.GnTelephony.Threads;
import android.telephony.SmsManager;
//Aurora xuyong 2013-11-16 added for google adapt start
import gionee.telephony.GnSmsMessage;
//Aurora xuyong 2013-11-16 added for google adapt end
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
//import com.mediatek.xlog.Xlog;





import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
//Aurora xuyong 2014-12-03 added for debug start
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//Aurora xuyong 2014-12-03 added for debug end

import com.aurora.featureoption.FeatureOption;
import com.android.internal.telephony.SmsHeader;
import com.android.providers.utils.AuroraSimIdMatching;
import com.google.android.mms.pdu.PduHeaders;

import gionee.telephony.gemini.GnGeminiSmsManager;
import gionee.telephony.GnSmsMessage;

import com.gionee.internal.telephony.GnPhone;
import com.privacymanage.service.AuroraPrivacyUtils;

import gionee.provider.GnTelephony;
import gionee.provider.GnTelephony.SIMInfo;
// gionee zhouyj 2012-05-22 add for CR00607938 start
import android.telephony.PhoneNumberUtils;
import android.os.SystemProperties;




// gionee zhouyj 2012-05-22 add for CR00607938 end
// gionee zhouyj 2012-11-16 add for CR00733163 start 
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import android.os.ParcelFileDescriptor;
import android.content.res.AssetFileDescriptor;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.OperationApplicationException;
//gionee zhouyj 2012-11-16 add for CR00733163 end 
import gionee.telephony.GnTelephonyManager;
import android.os.SystemProperties;

public class SmsProvider extends ContentProvider {
    private static final Uri NOTIFICATION_URI = Uri.parse("content://sms");
    private static final Uri ICC_URI = Uri.parse("content://sms/icc");
    private static final Uri ICC_URI_GEMINI = Uri.parse("content://sms/icc2");
    static final String TABLE_SMS = "sms";
    private static final String TABLE_RAW = "raw";
    private static final String TABLE_SR_PENDING = "sr_pending";
    private static final String TABLE_WORDS = "words";
    private static final String FOR_MULTIDELETE = "ForMultiDelete";
    private static final String FOR_FOLDERMODE_MULTIDELETE = "ForFolderMultiDelete";
    private static final Integer ONE = Integer.valueOf(1);
    private static final String ALL_SMS = "999999";
    private static final String[] CONTACT_QUERY_PROJECTION =
            new String[] { Contacts.Phones.PERSON_ID };
    private static final int PERSON_ID_COLUMN = 0;
    private static final int NORMAL_NUMBER_MAX_LENGTH = 15;
    private static final String[] CANONICAL_ADDRESSES_COLUMNS_2 =
    new String[] { CanonicalAddressesColumns._ID, CanonicalAddressesColumns.ADDRESS };
    // Aurora xuyong 2014-07-03 added for reject feature start
    private static final boolean sHasRejectFeature = SystemProperties.get("ro.aurora.reject.support").equals("yes");
    // Aurora xuyong 2014-07-03 added for reject feature end
    // gionee zhouyj 2012-05-22 add for CR00607938 start
    //gionee guoyangxu 20120606 modified for CR00622143 start
    private boolean mIsMsgBox = MmsSmsDatabaseHelper.mIsMsgBox;
    //gionee guoyangxu 20120606 modified for CR00622143 end
    private static final int MIN_MATCH = SystemProperties.getInt("ro.gn.match.numberlength", 7);
    private boolean mCancelImport = false;
    private boolean mImportNormal = false;
    // gionee zhouyj 2012-05-22 add for CR00607938 end
    // gionee zhouyj 2012-07-31 add for CR00652308 start 
    private static final boolean mGnSmsBackupSupport = SystemProperties.get("ro.gn.msgbox.prop").equals("yes") &&
        SystemProperties.get("ro.gn.export.import.support").equals("yes");
    // gionee zhouyj 2012-07-31 add for CR00652308 end 

    //gionee gaoj 2012-9-20 added for CR00699291 start
    private static final String GN_FOR_MULTIDELETE = "GnForMultiDelete";
    //gionee gaoj 2012-9-20 added for CR00699291 end

    //Gionee guoyx 20130227 added for CR00772069 begin
    public static final boolean mQcMultiSimEnabled = GnTelephonyManager.isMultiSimEnabled();
    public static final boolean mGnMultiSimMessage = mQcMultiSimEnabled || FeatureOption.MTK_GEMINI_SUPPORT;
    //Gionee guoyx 20130227 added for CR00772069 end

    /**
     * These are the columns that are available when reading SMS
     * messages from the ICC.  Columns whose names begin with "is_"
     * have either "true" or "false" as their values.
     */
    private final static String[] ICC_COLUMNS = new String[] {
        // N.B.: These columns must appear in the same order as the
        // calls to add appear in convertIccToSms.
        "service_center_address",       // getServiceCenterAddress
        "address",                      // getDisplayOriginatingAddress
        "message_class",                // getMessageClass
        "body",                         // getDisplayMessageBody
        "date",                         // getTimestampMillis
        "status",                       // getStatusOnIcc
        "index_on_icc",                 // getIndexOnIcc
        "is_status_report",             // isStatusReportMessage
        "transport_type",               // Always "sms".
        "type",                         // Always MESSAGE_TYPE_ALL.
        "locked",                       // Always 0 (false).
        "error_code",                   // Always 0
        "_id",
        GnTelephony.GN_SIM_ID/*"sim_id"*/                        // sim id
    };

    @Override
    public boolean onCreate() {
        mOpenHelper = MmsSmsDatabaseHelper.getInstance(getContext());
        // Aurora yudingmin 2014-11-19 added for bug #9304 start
        mAuroraSimIdMatching = AuroraSimIdMatching.getInstance(getContext());
        // Aurora yudingmin 2014-11-19 added for bug #9304 end
        return true;
    }
    // Aurora xuyong 2014-07-18 added for bug #5921 start
    private boolean mNeedRebuildCursor = false;
    // Aurora xuyong 2014-07-18 added for bug #5921 end
    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection,
            String[] selectionArgs, String sort) {
        // Aurora xuyong 2014-12-03 added for debug start
        final String originSelection = selection;
        // Aurora xuyong 2014-12-03 added for debug end
 //       Xlog.d(TAG, "query begin");
        // gionee zhouyj 2012-07-31 add for CR00652308 start 
        if (mGnSmsBackupSupport) {
            mImportNormal = true;
        }
        // Aurora xuyong 2014-07-18 added for bug #5921 start
        if (selection != null && selection.contains("sub_id")) {
            mNeedRebuildCursor = true;
            selection = selection.replaceAll("sub_id", "sim_id");
            }
        if (projectionIn != null && projectionIn.length > 0) {
            int itemNum = projectionIn.length;
            for (int i = 0; i < itemNum; i++) {
                if (projectionIn[i].equals("sub_id")) {
                    projectionIn[i] = "sim_id";
                }
            }
         }
        // Aurora xuyong 2014-07-18 added for bug #5921 end
        // gionee zhouyj 2012-07-31 add for CR00652308 end 
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // Generate the body of the query.
        int match = sURLMatcher.match(url);
        switch (match) {
            case SMS_ALL:
                constructQueryForBox(qb, Sms.MESSAGE_TYPE_ALL);
                break;

            case SMS_UNDELIVERED:
                constructQueryForUndelivered(qb);
                break;

            case SMS_FAILED:
                constructQueryForBox(qb, Sms.MESSAGE_TYPE_FAILED);
                break;

            case SMS_QUEUED:
                constructQueryForBox(qb, Sms.MESSAGE_TYPE_QUEUED);
                break;

            case SMS_INBOX:
                constructQueryForBox(qb, Sms.MESSAGE_TYPE_INBOX);
                break;

            case SMS_SENT:
                constructQueryForBox(qb, Sms.MESSAGE_TYPE_SENT);
                break;

            case SMS_DRAFT:
                constructQueryForBox(qb, Sms.MESSAGE_TYPE_DRAFT);
                break;

            case SMS_OUTBOX:
                constructQueryForBox(qb, Sms.MESSAGE_TYPE_OUTBOX);
                break;

            case SMS_ALL_ID:
                qb.setTables(TABLE_SMS);
                qb.appendWhere("(_id = " + url.getPathSegments().get(0) + ")");
                break;

            case SMS_INBOX_ID:
            case SMS_FAILED_ID:
            case SMS_SENT_ID:
            case SMS_DRAFT_ID:
            case SMS_OUTBOX_ID:
                qb.setTables(TABLE_SMS);
                qb.appendWhere("(_id = " + url.getPathSegments().get(1) + ")");
                break;

            case SMS_CONVERSATIONS_ID:
                int threadID;

                try {
                    threadID = Integer.parseInt(url.getPathSegments().get(1));
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.d(TAG, "query conversations: threadID=" + threadID);
                    }
                }
                catch (Exception ex) {
                    Log.e(TAG,
                          "Bad conversation thread id: "
                          + url.getPathSegments().get(1));
                    return null;
                }

                qb.setTables(TABLE_SMS);
                qb.appendWhere("thread_id = " + threadID);
                if (null == mOpenHelper) {
                    return null;
                }
                MmsSmsDatabaseHelper.updateThread(mOpenHelper.getWritableDatabase(), threadID); 
                break;

            case SMS_CONVERSATIONS:
                qb.setTables("sms, (SELECT thread_id AS group_thread_id, MAX(date)AS group_date,"
                       + "COUNT(*) AS msg_count FROM sms GROUP BY thread_id) AS groups");
                qb.appendWhere("sms.thread_id = groups.group_thread_id AND sms.date ="
                       + "groups.group_date");
                qb.setProjectionMap(sConversationProjectionMap);
                break;

            case SMS_RAW_MESSAGE:
                qb.setTables("raw");
                break;

            case SMS_STATUS_PENDING:
                qb.setTables("sr_pending");
                break;

            case SMS_ATTACHMENT:
                qb.setTables("attachments");
                break;

            case SMS_ATTACHMENT_ID:
                qb.setTables("attachments");
                qb.appendWhere(
                        "(sms_id = " + url.getPathSegments().get(1) + ")");
                break;

            case SMS_QUERY_THREAD_ID:
                qb.setTables("canonical_addresses");
                if (projectionIn == null) {
                    projectionIn = sIDProjection;
                }
                break;

            case SMS_STATUS_ID:
                qb.setTables(TABLE_SMS);
                qb.appendWhere("(_id = " + url.getPathSegments().get(1) + ")");
                break;

            case SMS_ALL_ICC:
                if(mGnMultiSimMessage) {//guoyx 20130227
                    return getAllMessagesFromIcc(GnPhone.GEMINI_SIM_1);
                } else {
                    return getAllMessagesFromIcc();
                }

            case SMS_ICC:
                String messageIndexString = url.getPathSegments().get(1);

                if(mGnMultiSimMessage) {//guoyx 20130227
                    return getSingleMessageFromIcc(messageIndexString, GnPhone.GEMINI_SIM_1);
                }
                else {
                return getSingleMessageFromIcc(messageIndexString);
                }

            case SMS_ALL_ICC_GEMINI:
                return getAllMessagesFromIcc(GnPhone.GEMINI_SIM_2);

            case SMS_ICC_GEMINI:
                String messageIndexString_1 = url.getPathSegments().get(1);

                return getSingleMessageFromIcc(messageIndexString_1, GnPhone.GEMINI_SIM_2);


            default:
                Log.e(TAG, "Invalid request: " + url);
                return null;
        }

        String orderBy = null;

        if (!TextUtils.isEmpty(sort)) {
            orderBy = sort;
        } else if (qb.getTables().equals(TABLE_SMS)) {
            orderBy = Sms.DEFAULT_SORT_ORDER;
        }
 //       Xlog.d(TAG, "query getReadbleDatabase");
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
 //       Xlog.d(TAG, "query getReadbleDatabase qb.query begin");
        // Aurora xuyong 2014-07-02 added for reject feature start
        // Aurora xuyong 2014-07-03 added for reject feature start
        if (MmsSmsDatabaseHelper.sHasRejectFeature) {
            if (qb != null && qb.getTables().equals("sms")) {
                if (selection == null || selection.length() == 0) {
                    selection = "reject = 0";
                } else if (!selection.contains("reject")) {
                // Aurora xuyong 2014-08-07 modified for bug #7069 start
                    selection = "reject = 0 AND " + selection;
                // Aurora xuyong 2014-08-07 modified for bug #7069 end
                }
            }
        }
        // Aurora xuyong 2014-11-04 added for bug #9637 start
        if (MmsSmsDatabaseHelper.sHasPrivacyFeature) {
            if (qb != null && qb.getTables().equals("sms")) {
                if (selection == null || selection.length() == 0) {
                    selection = " is_privacy = 0 ";
                } else if (!selection.contains("is_privacy")) {
                    selection = "is_privacy = 0 AND " + selection;
                }
            }
        }
        // Aurora xuyong 2014-11-04 added for bug #9637 end
        // Aurora xuyong 2014-07-03 added for reject feature end
        // Aurora xuyong 2014-07-02 added for reject feature end
        // Aurora xuyong 2014-12-03 added for debug start
        if (originSelection == null && projectionIn != null && projectionIn.length == 1) {
            if (projectionIn[0].contains("from")) {
                String regex = "from( )*threads";
                Pattern p = Pattern.compile(regex);
                Matcher matcher = p.matcher(projectionIn[0]);
                if (matcher.find()) {
                    projectionIn[0] = projectionIn[0].replaceAll("where", "where is_privacy = 0 AND ");
                }
            }
        }
     // Aurora xuyong 2014-12-03 added for debug end
        Cursor ret = qb.query(db, projectionIn, selection, selectionArgs,
                              null, null, orderBy);
 //       Xlog.d(TAG, "query getReadbleDatabase qb.query end");
        // TODO: Since the URLs are a mess, always use content://sms
        ret.setNotificationUri(getContext().getContentResolver(),
                NOTIFICATION_URI);
        // Aurora xuyong 2014-07-18 modified for bug #5921 start
        return rebuildCursor(ret);
        // Aurora xuyong 2014-07-18 modified for bug #5921 end
    }
    // Aurora xuyong 2014-07-18 added for bug #5921 start
    private Cursor rebuildCursor(Cursor cursor) {
        if (!mNeedRebuildCursor) {
            return cursor;
        }
        MatrixCursor mtCursor = null;
        if (cursor != null && !cursor.isClosed()) {
            int columnCount = cursor.getColumnCount();
            String[] project = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                String columnName = cursor.getColumnName(i);
                if (columnName.equals("sim_id")) {
                   project[i] = "sub_id";
                } else {
                   project[i] = columnName;
                }
            }
            mtCursor = new MatrixCursor(project, cursor.getCount());
            if (cursor.moveToFirst()) {
                if (cursor.getPosition() == -1) {
                    cursor.moveToNext();
                }
                Object[] firstItemDetail = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    firstItemDetail[i] = cursor.getString(i);
                }
                mtCursor.addRow(firstItemDetail);
                while (cursor.moveToNext()) {
                    Object[] itemDetail = new Object[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        itemDetail[i] = cursor.getString(i);
                    }
                    mtCursor.addRow(itemDetail);
                }
                cursor.moveToFirst();
            }
        }
        mNeedRebuildCursor = false;
        if (mtCursor == null) {
            return cursor;
        } else {
          //Aurora xuyong 2014-09-02 added for whitelist feature start
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
          }
          //Aurora xuyong 2014-09-02 added for whitelist feature end
            return mtCursor;
        }
    }
    // Aurora xuyong 2014-07-18 added for bug #5921 end
    private Object[] convertIccToSms(SmsMessage message, 
            ArrayList<String> concatSmsIndexAndBody, int id, 
            int simId) {
        Object[] row = new Object[14];
        // N.B.: These calls must appear in the same order as the
        // columns appear in ICC_COLUMNS.
        row[0] = message.getServiceCenterAddress();
        
        // check message status and set address
        if ((message.getStatusOnIcc() == SmsManager.STATUS_ON_ICC_READ) ||
               (message.getStatusOnIcc() == SmsManager.STATUS_ON_ICC_UNREAD)) {
            row[1] = message.getDisplayOriginatingAddress();
        } else {
            row[1] = GnSmsMessage.getDisplayOriginatingAddress(message);//message.getDestinationAddress();
        }

        String concatSmsIndex = null;
        String concatSmsBody = null;
        if (null != concatSmsIndexAndBody) {
            concatSmsIndex = concatSmsIndexAndBody.get(0);
            concatSmsBody = concatSmsIndexAndBody.get(1);
        }
        
        row[2] = String.valueOf(message.getMessageClass());
        row[3] = concatSmsBody == null ? message.getDisplayMessageBody() : concatSmsBody;
        row[4] = message.getTimestampMillis();
        row[5] = message.getStatusOnIcc();
        row[6] = concatSmsIndex == null ? message.getIndexOnIcc() : concatSmsIndex;
        row[7] = message.isStatusReportMessage();
        row[8] =  "sms";
        row[9] = TextBasedSmsColumns.MESSAGE_TYPE_ALL;
        row[10] = 0;      // locked
        row[11] = 0;      // error_code
        row[12] = id;
        row[13] = simId;
        return row;
    }

    private Object[] convertIccToSms(SmsMessage message, ArrayList<String> concatSmsIndexAndBody) {
        return convertIccToSms(message, concatSmsIndexAndBody, -1, -1);
    }
    
    private Object[] convertIccToSms(SmsMessage message,int id, int simId) {
        return convertIccToSms(message, null,id, simId);
    }
    
    private Object[] convertIccToSms(SmsMessage message) {
        return convertIccToSms(message, null, -1, -1);
    }

    /**
     * Return a Cursor containing just one message from the ICC.
     */
    private Cursor getSingleMessageFromIcc(String messageIndexString) {
        try {
            int messageIndex = Integer.parseInt(messageIndexString);
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<SmsMessage> messages = smsManager.getAllMessagesFromIcc();

            if (messages == null || messages.isEmpty()) {
   //             Xlog.e(TAG, "getSingleMessageFromIcc messages is null");
                return null;
            }
            SmsMessage message = messages.get(messageIndex);
            if (message == null) {
                throw new IllegalArgumentException(
                        "Message not retrieved. ID: " + messageIndexString);
            }
            MatrixCursor cursor = new MatrixCursor(ICC_COLUMNS, 1);
            cursor.addRow(convertIccToSms(message, 0, 0));
            return withIccNotificationUri(cursor);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Bad SMS ICC ID: " + messageIndexString);
        }
    }

    /**
     * Return a Cursor containing just one message from the ICC.
     */
    private Cursor getSingleMessageFromIcc(String messageIndexString, int slotId) {
        try {
            int messageIndex = Integer.parseInt(messageIndexString);
            // Aurora xuyong 2014-06-11 modified for android4.4 new feature start
            ArrayList<SmsMessage> messages = null;
            long token = Binder.clearCallingIdentity();
            try {
                messages = GnGeminiSmsManager.getAllMessagesFromIccGemini(slotId);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
            // Aurora xuyong 2014-06-11 modified for android4.4 new feature end
            if (messages == null || messages.isEmpty()) {
    //            Xlog.e(TAG, "getSingleMessageFromIcc messages is null");
                return null;
            }
            MatrixCursor cursor = new MatrixCursor(ICC_COLUMNS, 1);
            SmsMessage message = messages.get(messageIndex);
            if (message == null) {
                throw new IllegalArgumentException(
                        "Message not retrieved. ID: " + messageIndexString);
            }

            // convert slotId to simId
            SIMInfo si = SIMInfo.getSIMInfoBySlot(getContext(), slotId);
            if (null == si) {
   //             Xlog.e(TAG, "getSingleMessageFromIcc:SIMInfo is null for slot " + slotId);
                return null;
            }
            cursor.addRow(convertIccToSms(message, 0, (int)si.mSimId));
            if(slotId == GnPhone.GEMINI_SIM_1) {
                return withIccNotificationUri(cursor, GnPhone.GEMINI_SIM_1);
            }
            else {
                return withIccNotificationUri(cursor, GnPhone.GEMINI_SIM_2);
            }
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Bad SMS ICC ID: " + messageIndexString);
        }
    }
    // Aurora xuyong 2014-06-17 added for adapt for both 4.3 & 4.4 feature start
    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= 0x13; //Build.VERSION_CODES.KITKAT;
    }
    // Aurora xuyong 2014-06-17 added for adapt for both 4.3 & 4.4 feature end
    /**
     * Return a Cursor listing all the messages stored on the ICC.
     */
    private Cursor getAllMessagesFromIcc() {
        SmsManager smsManager = SmsManager.getDefault();
        // Aurora xuyong 2014-06-11 added for android4.4 new feature start
        ArrayList<SmsMessage> messages = null;
        // use phone app permissions to avoid UID mismatch in AppOpsManager.noteOp() call
        // Aurora xuyong 2014-06-17 modified for adapt for both 4.3 & 4.4 feature start
        long token = -1l;
        if (hasKitKat()) {
            token = Binder.clearCallingIdentity();
        }
        try {
            messages = smsManager.getAllMessagesFromIcc();
        } finally {
            if (hasKitKat()) {
                Binder.restoreCallingIdentity(token);
            }
        }
        // Aurora xuyong 2014-06-17 modified for adapt for both 4.3 & 4.4 feature end
        // Aurora xuyong 2014-06-11 added for android4.4 new feature end
        if (messages == null || messages.isEmpty()) {
//            Xlog.e(TAG, "getAllMessagesFromIcc messages is null");
            return null;
        }
        // gionee zhouyj 2012-05-22 add for CR00607938 start
        if (mIsMsgBox) {
            sortMsgListByTime(messages);
        }
        // gionee zhouyj 2012-05-22 add for CR00607938 end
        final int count = messages.size();
        MatrixCursor cursor = new MatrixCursor(ICC_COLUMNS, count);
        ArrayList<String> concatSmsIndexAndBody = null;
        boolean showInOne = true;
        String optr = SystemProperties.get("ro.operator.optr");
        //MTK_OP01_PROTECT_START
        showInOne = !"OP01".equals(optr);
        //MTK_OP01_PROTECT_END

        for (int i = 0; i < count; i++) {
            concatSmsIndexAndBody = null;
            SmsMessage message = messages.get(i);
            if (message != null) {
                if (showInOne) {
                    //Aurora xuyong 2013-11-16 modified for google adapt start
                    SmsHeader smsHeader = GnSmsMessage.getInstance().getUserDataHeader(message);
                    //Aurora xuyong 2013-11-16 modified for google adapt end
                    if (null != smsHeader && null != smsHeader.concatRef) {
                        concatSmsIndexAndBody = getConcatSmsIndexAndBody(messages, i);
                    }
                }
               cursor.addRow(convertIccToSms(message, concatSmsIndexAndBody, i, -1));
            }
        }
        return withIccNotificationUri(cursor);
    }

    private Cursor getAllMessagesFromIcc(int slotId) {
 //       Xlog.d(TAG, "getAllMessagesFromIcc slotId =" + slotId);
       // Aurora xuyong 2014-06-11 modified for android4.4 new feature start
        // use phone app permissions to avoid UID mismatch in AppOpsManager.noteOp() call
        // Aurora xuyong 2014-06-17 modified for adapt for both 4.3 & 4.4 feature start
        long token = -1l;
        if (hasKitKat()) {
            token = Binder.clearCallingIdentity();
        }
        ArrayList<SmsMessage> messages = null;
        try {
            messages = GnGeminiSmsManager.getAllMessagesFromIccGemini(slotId);
        } finally {
            if (hasKitKat()) {
                Binder.restoreCallingIdentity(token);
            }
        }
        // Aurora xuyong 2014-06-17 modified for adapt for both 4.3 & 4.4 feature end
        // Aurora xuyong 2014-06-11 modified for android4.4 new feature end
        if (messages == null || messages.isEmpty()) {
 //           Xlog.e(TAG, "getAllMessagesFromIcc messages is null");
            return null;
        }
        // gionee zhouyj 2012-05-22 add for CR00607938 start
        if (mIsMsgBox) {
            sortMsgListByTime(messages);
        }
        // gionee zhouyj 2012-05-22 add for CR00607938 end
        ArrayList<String> concatSmsIndexAndBody = null;

        // convert slotId to simId
        SIMInfo si = SIMInfo.getSIMInfoBySlot(getContext(), slotId);
        if (null == si) {
 //           Xlog.d(TAG, "getSingleMessageFromIcc:SIMInfo is null for slot " + slotId);
            return null;
        }
        int count = messages.size();
        MatrixCursor cursor = new MatrixCursor(ICC_COLUMNS, count);
        boolean showInOne = true;
        String optr = SystemProperties.get("ro.operator.optr");
        //MTK_OP01_PROTECT_START
        showInOne = !"OP01".equals(optr);
        //MTK_OP01_PROTECT_END
        
        for (int i = 0; i < count; i++) {
            concatSmsIndexAndBody = null;
            SmsMessage message = messages.get(i);
            if (message != null) {
                if (showInOne) {
                    //Aurora xuyong 2013-11-16 modified for google adapt start
                    SmsHeader smsHeader = GnSmsMessage.getInstance().getUserDataHeader(message);
                    //Aurora xuyong 2013-11-16 modified for google adapt end
                    if (null != smsHeader && null != smsHeader.concatRef) {
                        concatSmsIndexAndBody = getConcatSmsIndexAndBody(messages, i);
                    }
                }
                cursor.addRow(convertIccToSms(message, concatSmsIndexAndBody, i, (int)si.mSimId));
            }
        }
        if(slotId == GnPhone.GEMINI_SIM_1) {
            return withIccNotificationUri(cursor, GnPhone.GEMINI_SIM_1);
        } else {
            return withIccNotificationUri(cursor, GnPhone.GEMINI_SIM_2);
        }
    }

    private ArrayList<String> getConcatSmsIndexAndBody(ArrayList<SmsMessage> messages, int index) {
        int totalCount = messages.size();
        int refNumber = 0;
        int msgCount = 0;
        ArrayList<String> indexAndBody = new ArrayList<String>();
        StringBuilder smsIndex = new StringBuilder();
        StringBuilder smsBody = new StringBuilder();
        ArrayList<SmsMessage> concatMsg = null;
        SmsMessage message = messages.get(index);
        if (message != null) {
            //Aurora xuyong 2013-11-16 modified for google adapt start
            SmsHeader smsHeader = GnSmsMessage.getInstance().getUserDataHeader(message);
            //Aurora xuyong 2013-11-16 modified for google adapt end
            if (null != smsHeader && null != smsHeader.concatRef) {
                msgCount = smsHeader.concatRef.msgCount;
                refNumber = smsHeader.concatRef.refNumber;
            }
        }

        concatMsg = new ArrayList<SmsMessage>();
        concatMsg.add(message);
        
        for (int i = index + 1; i < totalCount; i++) {
            SmsMessage sms = messages.get(i);
            if (sms != null) {
                //Aurora xuyong 2013-11-16 modified for google adapt start
                SmsHeader smsHeader = GnSmsMessage.getInstance().getUserDataHeader(sms);
                //Aurora xuyong 2013-11-16 modified for google adapt end
                if (null != smsHeader && null != smsHeader.concatRef && refNumber == smsHeader.concatRef.refNumber) {
                    concatMsg.add(sms);
                    messages.set(i, null);
                    if (msgCount == concatMsg.size()) {
                        break;
                    }
                }
            }
        }

        int concatCount = concatMsg.size();
        for (int k = 0; k < msgCount; k++) {
            for (int j = 0; j < concatCount; j++) {
                SmsMessage sms = concatMsg.get(j);
                //Aurora xuyong 2013-11-16 modified for google adapt start
                SmsHeader smsHeader = GnSmsMessage.getInstance().getUserDataHeader(sms);
                //Aurora xuyong 2013-11-16 modified for google adapt end
                if (k == smsHeader.concatRef.seqNumber -1) {
                    smsIndex.append(sms.getIndexOnIcc());
                    smsIndex.append(";");
                    smsBody.append(sms.getDisplayMessageBody());
                    break;
                }
            }
        }

//        Xlog.d(TAG, "concatenation sms index:" + smsIndex.toString());
//        Xlog.d(TAG, "concatenation sms body:" + smsBody.toString());
        indexAndBody.add(smsIndex.toString());
        indexAndBody.add(smsBody.toString());

        return indexAndBody;
    }

    private Cursor withIccNotificationUri(Cursor cursor) {
        cursor.setNotificationUri(getContext().getContentResolver(), ICC_URI);
        return cursor;
    }

    private Cursor withIccNotificationUri(Cursor cursor, int slotId) {
        if(slotId == GnPhone.GEMINI_SIM_1) {
            cursor.setNotificationUri(getContext().getContentResolver(), ICC_URI);
        }
        else {
            cursor.setNotificationUri(getContext().getContentResolver(), ICC_URI_GEMINI);
        }
        return cursor;
    }


    private void constructQueryForBox(SQLiteQueryBuilder qb, int type) {
        qb.setTables(TABLE_SMS);

        if (type != Sms.MESSAGE_TYPE_ALL) {
            qb.appendWhere("type=" + type);
        }
    }

    private void constructQueryForUndelivered(SQLiteQueryBuilder qb) {
        qb.setTables(TABLE_SMS);

        qb.appendWhere("(type=" + Sms.MESSAGE_TYPE_OUTBOX +
                       " OR type=" + Sms.MESSAGE_TYPE_FAILED +
                       " OR type=" + Sms.MESSAGE_TYPE_QUEUED + ")");
    }

    @Override
    public String getType(Uri url) {
        switch (url.getPathSegments().size()) {
        case 0:
            return VND_ANDROID_DIR_SMS;
            case 1:
                try {
                    Integer.parseInt(url.getPathSegments().get(0));
                    return VND_ANDROID_SMS;
                } catch (NumberFormatException ex) {
                    return VND_ANDROID_DIR_SMS;
                }
            case 2:
                // TODO: What about "threadID"?
                if (url.getPathSegments().get(0).equals("conversations")) {
                    return VND_ANDROID_SMSCHAT;
                } else {
                    return VND_ANDROID_SMS;
                }
        }
        return null;
    }
    
    // gionee zhouyj 2012-05-22 add for CR00607938 start
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        // TODO Auto-generated method stub
        switch (sURLMatcher.match(uri)) {
            case SMS_IMPORT_ALL:
                final ContentValues[] insertValues = values;
                int keep = Integer.valueOf(uri.toString().substring(uri.toString().lastIndexOf("/") + 1));
                mCancelImport = false;
                mImportNormal = false;
                long start = System.currentTimeMillis();
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                ArrayList<String> recipientIds = new ArrayList<String>();
                try {
                    db.beginTransaction();
                    for (int i=0;i<insertValues.length && !mCancelImport;i++) {
                        if (mImportNormal && db.inTransaction()) {
                            db.setTransactionSuccessful();
                            db.endTransaction();
                            mImportNormal = false;
                        }
                        if (i>0 && PhoneNumberUtils.compare(insertValues[i].getAsString("address"), (insertValues[i-1].getAsString("address")))) {
                            insertValues[i].put("thread_id", insertValues[i-1].getAsInteger("thread_id"));
                        } else {
                            String where = "address= '" + insertValues[i].getAsString("address") + "'";
                            if (insertValues[i].getAsString("address").length() > MIN_MATCH) {
                                where = where + " OR " + "address like '%" + insertValues[i].getAsString("address").substring(insertValues[i].getAsString("address").length() - MIN_MATCH) + "'";
                            }
                            Cursor cursor = db.query("canonical_addresses", null, where, null, null, null, null);
                            if (null != cursor && cursor.getCount() > 0) {
                                cursor.moveToFirst();
                                int threadId = cursor.getInt(0);
                                Cursor mCursor = db.query("threads", null, "recipient_ids='" + String.valueOf(threadId) + "'", null, null, null, null);
                                if (null == mCursor || mCursor.getCount() < 1) {
                                    ContentValues value = new ContentValues();
                                    value.put("recipient_ids", String.valueOf(threadId));
                                    threadId = (int) db.insert("threads", "_id", value);
                                } else {
                                    mCursor.moveToFirst();
                                    threadId = mCursor.getInt(0);
                                }
                                insertValues[i].put("thread_id", threadId);
                                mCursor.close();
                            } else {
                                ContentValues value = new ContentValues();
                                value.put("address", insertValues[i].getAsString("address"));
                                int threadId = (int) db.insert("canonical_addresses", "_id", value);
                                ContentValues mValues = new ContentValues();
                                mValues.put("recipient_ids", String.valueOf(threadId));
                                threadId = (int) db.insert("threads", "_id", mValues);
                                insertValues[i].put("thread_id", threadId);
                            }
                            if (!cursor.isClosed()) {
                                cursor.close();
                            }
                            recipientIds.add(String.valueOf(insertValues[i].getAsInteger("thread_id")));
                        }
                        long rowID = db.insert(TABLE_SMS, "_id", insertValues[i]);
                        ContentValues cv = new ContentValues();
                        cv.put(Telephony.MmsSms.WordsTable.ID, rowID);
                        cv.put(Telephony.MmsSms.WordsTable.INDEXED_TEXT, insertValues[i].getAsString("body"));
                        cv.put(Telephony.MmsSms.WordsTable.SOURCE_ROW_ID, rowID);
                        cv.put(Telephony.MmsSms.WordsTable.TABLE_ID, 1);
                        db.insert(TABLE_WORDS, Telephony.MmsSms.WordsTable.INDEXED_TEXT, cv);
                    }
                    Log.e("SmsProvider", " ...delete sms and update threads cost time: " + (System.currentTimeMillis() - start));
                    if (db.inTransaction()) {
                        db.setTransactionSuccessful();
                    }
                } catch (Exception e) {
                    Log.d(TAG,"import has some error!!!!!");
                    e.printStackTrace();
                } finally {
                    mCancelImport = false;
                    if (db.inTransaction()) {
                        db.endTransaction();
                    }
                    mImportNormal = false;
                }
                Log.e("SmsProvider","insert count: " + insertValues.length + " ...trasction cost time: " + (System.currentTimeMillis() - start));
                return insertValues.length;
            case SMS_IMPORT_CANCEL:
                mCancelImport = true;
                Log.d(TAG, "cancel import.............." + mCancelImport);
                return 0;
                // gionee zhouyj 2012-08-27 add for CR00667581 start
            case SMS_IMPORT_CHANGE_MODE:
                mImportNormal = true;
                return 0;
                // gionee zhouyj 2012-08-27 add for CR00667581 end
            default:
                break;
        }
        return super.bulkInsert(uri, values);
    }
    
    /* Add a new method to sort the messages from SIM Card */
    private void sortMsgListByTime(ArrayList<SmsMessage> msgList) {
        if(msgList == null || msgList.isEmpty())
            return ;
        int size = msgList.size();
        int count = 0;
        for (int i = 0; i < size; i++) {
            SmsMessage currMsg = msgList.get(i);

            if (currMsg != null) {
                SmsMessage maxMsg = currMsg;
                int maxIndex = i;

                for (int j = i + 1; j < size; j++) {
                    SmsMessage cmpMsg = msgList.get(j);

                    if (cmpMsg != null) {
                        if (cmpMsg.getTimestampMillis() < maxMsg.getTimestampMillis()) {
                        maxMsg = cmpMsg;
                        maxIndex = j;
                        }
                    }
                }

                if (maxIndex != count) {
                    msgList.set(count, maxMsg);
                }

                if (maxIndex != i) {
                    msgList.set(maxIndex, currMsg);
                }

                count++;
            }
        }
    }
    // gionee zhouyj 2012-05-22 add for CR00607938 end

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        // Aurora xuyong 2014-11-24 added for apdate huawei honor 6 start
        if (initialValues != null && initialValues.containsKey("network_type")) {
            initialValues.remove("network_type");
        }
        // Aurora xuyong 2014-11-24 added for apdate huawei honor 6 end
//        Xlog.d(TAG, "insert begin");
         // Aurora liugj 2013-09-17 modified for iuni-4.3 adapter start
         if (initialValues.containsKey("sub_id")) {
          // Aurora xuyong 2014-05-29 modified for multisim feature start
            Log.e("MmsDebug", "these values contain sub_id key, we should translate it to sim_id key");
            initialValues.put("sim_id", initialValues.get("sub_id").toString());
          // Aurora xuyong 2014-05-29 modified for multisim feature end
            initialValues.remove("sub_id");
        }
        // Aurora xuyong 2015-03-10 added for bug #12109 start
        if (initialValues.containsKey("pri")) {
            initialValues.remove("pri");
        }
        // Aurora xuyong 2014-03-10 added for bug #12109 end
        //
         // Aurora yudingmin 2014-11-19 added for bug #9304 start
         if(initialValues.containsKey("sim_id")){
             String sim_id = mAuroraSimIdMatching.getAuroraSimId(initialValues.get("sim_id").toString());
             if(!TextUtils.isEmpty(sim_id)){
                 initialValues.put("sim_id", sim_id);
             }
         }
         // Aurora yudingmin 2014-11-19 added for bug #9304 end
         Log.e("MmsDebug", "sim_id = " + initialValues.get("sim_id"));
        // Aurora liugj 2013-09-17 modified for iuni-4.3 adapter end
        // gionee zhouyj 2012-07-31 add for CR00652308 start 
        if (mGnSmsBackupSupport) {
            mImportNormal = true;
        }
        // gionee zhouyj 2012-07-31 add for CR00652308 end 
        ContentValues values;
        long rowID = -1;
        int type = Sms.MESSAGE_TYPE_ALL;
        // for import sms only
        boolean importSms = false;
        int match = sURLMatcher.match(url);
        String table = TABLE_SMS;

        switch (match) {
            case SMS_ALL:
                Integer typeObj = initialValues.getAsInteger(Sms.TYPE);
                if (typeObj != null) {
                    type = typeObj.intValue();
                } else {
                    // default to inbox
                    type = Sms.MESSAGE_TYPE_INBOX;
                }
                break;

            case SMS_INBOX:
                // Aurora xuyong 2014-12-05 added for debug start
                Context context = this.getContext();
                if (MmsSmsDatabaseHelper.forbidOP(context)) {
                    return null;
                }
                // Aurora xuyong 2014-12-05 added for debug end
                type = Sms.MESSAGE_TYPE_INBOX;
                break;

            case SMS_FAILED:
                type = Sms.MESSAGE_TYPE_FAILED;
                break;

            case SMS_QUEUED:
                type = Sms.MESSAGE_TYPE_QUEUED;
                break;
            // Aurora xuyong 2014-11-25 added for bug #10052 start
            case SMS_QUEUED_VIA:
                type = Sms.MESSAGE_TYPE_QUEUED;
                break;
            // Aurora xuyong 2014-11-25 added for bug #10052 end
            case SMS_SENT:
                type = Sms.MESSAGE_TYPE_SENT;
                break;

            case SMS_DRAFT:
                type = Sms.MESSAGE_TYPE_DRAFT;
                break;

            case SMS_OUTBOX:
                type = Sms.MESSAGE_TYPE_OUTBOX;
                break;

            case SMS_RAW_MESSAGE:
                table = "raw";
                // Aurora xuyong 2014-10-21 added for samsung note II special case start
                if (initialValues.containsKey("sim_slot")) {
                    int slotId = initialValues.getAsInteger("sim_slot");
                    initialValues.remove("sim_slot");
                    SIMInfo info = SIMInfo.getSIMInfoBySlot(getContext(), slotId);
                    initialValues.put("sim_id", info.mSimId);
                }
                // Aurora xuyong 2014-10-21 added for samsung note II special case end
                break;

            case SMS_STATUS_PENDING:
                table = "sr_pending";
                break;

            case SMS_ATTACHMENT:
                table = "attachments";
                break;

            case SMS_NEW_THREAD_ID:
                table = "canonical_addresses";
                break;

            default:
                Log.e(TAG, "Invalid request: " + url);
                return null;
        }
 //       Xlog.d(TAG, "insert match url end"); 
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
//        Xlog.d(TAG, "insert mOpenHelper.getWritableDatabase end"); 
        if (table.equals(TABLE_SMS)) {
            boolean addDate = false;
            boolean addType = false;

            // Make sure that the date and type are set
            if (initialValues == null) {
                values = new ContentValues(1);
                addDate = true;
                addType = true;
            } else {
                values = new ContentValues(initialValues);

                if (!initialValues.containsKey(Sms.DATE)) {
                    addDate = true;
                }

                if (!initialValues.containsKey(Sms.TYPE)) {
                    addType = true;
                }
                if (initialValues.containsKey("import_sms")) {
                    importSms = true;
                    values.remove("import_sms");
                    // Aurora xuyong 2015-01-04 deleted for bug #10832 start
                    //db.beginTransaction();
                    // Aurora xuyong 2015-01-04 deleted for bug #10832 end
                }
            }

            if (addDate) {
                values.put(Sms.DATE, new Long(System.currentTimeMillis()));
            } else {
                Long date = values.getAsLong(Sms.DATE);
                values.put(Sms.DATE, date);
//                Xlog.d(TAG, "insert sms date "+ date);
            }

            if (addType && (type != Sms.MESSAGE_TYPE_ALL)) {
                values.put(Sms.TYPE, Integer.valueOf(type));
            }

            // thread_id
            Long threadId = values.getAsLong(Sms.THREAD_ID);
            // Aurora xuyong 2014-10-28 added for privacy feature start
            Cursor prePrivacyResult = null;
            try {
                prePrivacyResult = db.query("threads", new String[] {"is_privacy"}, " _id = ? ", new String[] { String.valueOf(threadId)}, null, null, null);
                if (prePrivacyResult != null && prePrivacyResult.moveToFirst()) {
                    values.put("is_privacy", prePrivacyResult.getLong(0));
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                if (prePrivacyResult != null && !prePrivacyResult.isClosed()) {
                    prePrivacyResult.close();
                }
            }
            // Aurora xuyong 2014-10-28 added for privacy feature end
            String address = values.getAsString(Sms.ADDRESS);
            if (((threadId == null) || (threadId == 0)) && (address != null)) {
                long id = 0;
                if (importSms){
                // Aurora xuyong 2014-10-23 modified for privacy feature start
                    // Aurora xuyong 2015-01-04 modified for bug #10832 start
                    db.beginTransaction();
                    try {
                        long privacy = 0l;
                        // Aurora xuyong 2014-11-25 modified for bug #10052 start
                        if (type == Sms.MESSAGE_TYPE_INBOX || match == SMS_QUEUED_VIA) {
                        // Aurora xuyong 2014-11-25 modified for bug #10052 end
                            privacy = AuroraPrivacyUtils.getFristPrivacyId(getContext(), address);
                        }
                        if (MmsSmsDatabaseHelper.sHasPrivacyFeature) {
                            id = getThreadIdInternal(address, db, privacy);
                        } else {
                            id = getThreadIdInternal(address, db);
                        }
                        db.setTransactionSuccessful();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        db.endTransaction();
                    }
                    // Aurora xuyong 2015-01-04 modified for bug #10832 end
                } else {
                    long privacy = 0l;
                    // Aurora xuyong 2014-11-25 modified for bug #10052 start
                    if (type == Sms.MESSAGE_TYPE_INBOX || match == SMS_QUEUED_VIA) {
                    // Aurora xuyong 2014-11-25 modified for bug #10052 end
                        privacy = AuroraPrivacyUtils.getFristPrivacyId(getContext(), address);
                    }
                    Set<String> recipients = new HashSet<String>();
                    recipients.add(address);
                    if (MmsSmsDatabaseHelper.sHasPrivacyFeature) {
                        id = AuroraPrivacyUtils.getOrCreateThreadId(getContext(), recipients, privacy);
                    } else {
                        id = Threads.getOrCreateThreadId(getContext(), address);
                    }
                }
                // Aurora xuyong 2014-10-23 modified for privacy feature end
                values.put(Sms.THREAD_ID, id);
//                Xlog.d(TAG, "insert getContentResolver getOrCreateThreadId end id = " + id);
                // Aurora xuyong 2014-10-28 added for privacy feature start
                Cursor privacyResult = null;
                try {
                    privacyResult = db.query("threads", new String[] {"is_privacy"}, " _id = ? ", new String[] { String.valueOf(id)}, null, null, null);
                    if (privacyResult != null && privacyResult.moveToFirst()) {
                        values.put("is_privacy", privacyResult.getLong(0));
                    }
                } catch (SQLiteException e) {
                    e.printStackTrace();
                } finally {
                    if (privacyResult != null && !privacyResult.isClosed()) {
                        privacyResult.close();
                    }
                }
                // Aurora xuyong 2014-10-28 added for privacy feature end
            }

            // If this message is going in as a draft, it should replace any
            // other draft messages in the thread.  Just delete all draft
            // messages with this thread ID.  We could add an OR REPLACE to
            // the insert below, but we'd have to query to find the old _id
            // to produce a conflict anyway.
            if (values.getAsInteger(Sms.TYPE) == Sms.MESSAGE_TYPE_DRAFT) {
                db.delete(TABLE_SMS, "thread_id=? AND type=?",
                        new String[] { values.getAsString(Sms.THREAD_ID),
                                       Integer.toString(Sms.MESSAGE_TYPE_DRAFT) });
            }

            if (type == Sms.MESSAGE_TYPE_INBOX) {
                // Look up the person if not already filled in.
                if ((values.getAsLong(Sms.PERSON) == null) && (!TextUtils.isEmpty(address))) {
                    Cursor cursor = null;
                    Uri uri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL,
                            Uri.encode(address));
                    try {
  //                      Xlog.d(TAG, "insert getContentResolver query contact begin"); 
                        cursor = getContext().getContentResolver().query(
                                uri,
                                CONTACT_QUERY_PROJECTION,
                                null, null, null);
    //                    Xlog.d(TAG, "insert getContentResolver query contact end"); 
                        if (cursor != null && cursor.moveToFirst()) {
                            Long id = Long.valueOf(cursor.getLong(PERSON_ID_COLUMN));
                            values.put(Sms.PERSON, id);
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "insert: query contact uri " + uri + " caught ", ex);
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            } else {
                // Mark all non-inbox messages read.
                values.put(Sms.READ, ONE);
            }
        } else {
            if (initialValues == null) {
                values = new ContentValues(1);
            } else {
                values = initialValues;
            }
        }

        rowID = db.insert(table, "body", values);
        //Aurora yudingmin 2014-11-03 added for sync feature start
        if(TABLE_SMS.equals(table) && rowID >= 0){
            insertAfterSms(rowID);
        }
        //Aurora yudingmin 2014-11-03 added for sync feature end
 //       Xlog.d(TAG, "insert table body end"); 
        if (!importSms){
            setThreadStatus(db, values, 0);
        }
        // Don't use a trigger for updating the words table because of a bug
        // in FTS3.  The bug is such that the call to get the last inserted
        // row is incorrect.
        if (table == TABLE_SMS) {
            // Update the words table with a corresponding row.  The words table
            // allows us to search for words quickly, without scanning the whole
            // table;
//            Xlog.d(TAG, "insert TABLE_WORDS begin"); 
            ContentValues cv = new ContentValues();
            cv.put(Telephony.MmsSms.WordsTable.ID, rowID);
            cv.put(Telephony.MmsSms.WordsTable.INDEXED_TEXT, values.getAsString("body"));
            cv.put(Telephony.MmsSms.WordsTable.SOURCE_ROW_ID, rowID);
            cv.put(Telephony.MmsSms.WordsTable.TABLE_ID, 1);
            db.insert(TABLE_WORDS, Telephony.MmsSms.WordsTable.INDEXED_TEXT, cv);
 //           Xlog.d(TAG, "insert TABLE_WORDS end"); 
        }
        // Aurora xuyong 2015-01-04 deleted for bug #10832 start
/*        if (importSms){
            db.setTransactionSuccessful();
            db.endTransaction();
        }*/
        // Aurora xuyong 2015-01-04 deleted for bug #10832 end
        // Aurora xuyong 2014-09-19 added for aurora reject feature start
        if (initialValues != null && initialValues.containsKey("reject") && initialValues.getAsInteger("reject").intValue() == 1) {
            getContext().getContentResolver().notifyChange(MmsSmsProvider.REJECT_NOTIFY_URI, null);
        }
        // Aurora xuyong 2014-10-30 modified for privacy feature start
        if (match == SMS_INBOX && values != null && values.containsKey("is_privacy")) {
            // Aurora xuyong 2014-11-10 modified for privacy feature start
            long notifyPriValue = values.getAsLong("is_privacy").longValue();
            if (notifyPriValue != AuroraPrivacyUtils.getCurrentAccountId() && notifyPriValue > 0) {
            // Aurora xuyong 2014-11-10 modified for privacy feature end
                getContext().getContentResolver().notifyChange(MmsSmsProvider.PRIVACY_NOTIFY_URI, null);
            }
        }
        // Aurora xuyong 2014-10-30 modified for privacy feature end
        // Aurora xuyong 2014-09-19 added for aurora reject feature end
        if (rowID > 0) {
            Uri uri = Uri.parse("content://" + table + "/" + rowID);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.d(TAG, "insert " + uri + " succeeded");
            }
            if (!importSms){
                notifyChange(uri);
            }
            return uri;
        } else {
            Log.e(TAG,"insert: failed! " + values.toString());
        }
 //       Xlog.d(TAG, "insert end");
        return null;
    }
    
    //Aurora yudingmin 2014-11-03 added for sync feature start
    static int deleteMessages(Context context, SQLiteDatabase db,
            String selection, String[] selectionArgs) {
        deleteBeforeSms(context, selection, selectionArgs);
        return db.delete(TABLE_SMS, selection, selectionArgs);
    }
    
    private int deleteBeforeSms(String selection, String[] selectionArgs){
        return getContext().getContentResolver().delete(MmsSmsSyncProvider.SYNC_SMS_UPDATE_URI, selection, selectionArgs);
    }
    private static int deleteBeforeSms(Context context, String selection, String[] selectionArgs){
        return context.getContentResolver().delete(MmsSmsSyncProvider.SYNC_SMS_UPDATE_URI, selection, selectionArgs);
    }
    private int deleteBeforeSmsId(int msgId){
        return getContext().getContentResolver().delete(Uri.withAppendedPath(MmsSmsSyncProvider.SYNC_SMS_UPDATE_URI, String.valueOf(msgId)), null, null);
    }
    private int updateBeforeSms(String selection, String[] selectionArgs){
        return getContext().getContentResolver().update(MmsSmsSyncProvider.SYNC_SMS_UPDATE_URI, null, selection, selectionArgs);
    }
    private Uri insertAfterSms(long rowId){
        Uri sync_mms_id = Uri.withAppendedPath(MmsSmsSyncProvider.SYNC_SMS_UPDATE_URI, String.valueOf(rowId));
        return getContext().getContentResolver().insert(sync_mms_id, null);
    }
    //Aurora yudingmin 2014-11-03 added for sync feature end

    private void setThreadStatus(SQLiteDatabase db, ContentValues values, int value) {
        ContentValues statusContentValues = new ContentValues(1);
        statusContentValues.put(Threads.STATUS, value);
        db.update("threads", statusContentValues, "_id=" + values.getAsLong(Sms.THREAD_ID), null);
    }
    
    private long getThreadIdInternal(String recipient, SQLiteDatabase db) {
        String THREAD_QUERY;
        if (FeatureOption.MTK_WAPPUSH_SUPPORT) {
            THREAD_QUERY = "SELECT _id FROM threads " + "WHERE type<>"
                    + Threads.WAPPUSH_THREAD + " AND recipient_ids=?";
        } else {
            THREAD_QUERY = "SELECT _id FROM threads " + "WHERE recipient_ids=?";
        }
        long recipientId = getRecipientId(recipient, db);
        String[] selectionArgs = new String[] { String.valueOf(recipientId) };
        Cursor cursor = db.rawQuery(THREAD_QUERY, selectionArgs);
        try {
              if (cursor != null && cursor.getCount() == 0) {
                   cursor.close();
                   Log.d(TAG, "getThreadId: create new thread_id for recipients " + recipient);
                   return insertThread(recipientId, db);
               } else if (cursor.getCount() == 1){
                      if (cursor.moveToFirst()) {
                       return cursor.getLong(0);
                   }
               } else {
                   Log.w(TAG, "getThreadId: why is cursorCount=" + cursor.getCount());
               }
        } finally {
            cursor.close();
        }

        return 0;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private long getThreadIdInternal(String recipient, SQLiteDatabase db, long privacy) {
        String THREAD_QUERY;
        if (FeatureOption.MTK_WAPPUSH_SUPPORT) {
            THREAD_QUERY = "SELECT _id FROM threads " + "WHERE type<>"
                    + Threads.WAPPUSH_THREAD + " AND recipient_ids=? AND privacy = ?";
        } else {
            THREAD_QUERY = "SELECT _id FROM threads " + "WHERE recipient_ids=? AND privacy = ?";
        }
        long recipientId = getRecipientId(recipient, db);
        String[] selectionArgs = new String[] { String.valueOf(recipientId), String.valueOf(privacy)};
        Cursor cursor = db.rawQuery(THREAD_QUERY, selectionArgs);
        try {
              if (cursor != null && cursor.getCount() == 0) {
                   cursor.close();
                   Log.d(TAG, "getThreadId: create new thread_id for recipients " + recipient);
                   return insertThread(recipientId, db, privacy);
               } else if (cursor.getCount() == 1){
                      if (cursor.moveToFirst()) {
                       return cursor.getLong(0);
                   }
               } else {
                   Log.w(TAG, "getThreadId: why is cursorCount=" + cursor.getCount());
               }
        } finally {
            cursor.close();
        }

        return 0;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    /**
     * Insert a record for a new thread.
     */
    private long insertThread(long recipientIds, SQLiteDatabase db) {
        ContentValues values = new ContentValues(4);

        long date = System.currentTimeMillis();
        values.put(ThreadsColumns.DATE, date - date % 1000);
        values.put(ThreadsColumns.RECIPIENT_IDS, recipientIds);
        values.put(ThreadsColumns.MESSAGE_COUNT, 0);
        return db.insert("threads", null, values);
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private long insertThread(long recipientIds, SQLiteDatabase db, long privacy) {
        ContentValues values = new ContentValues(4);

        long date = System.currentTimeMillis();
        values.put(ThreadsColumns.DATE, date - date % 1000);
        values.put(ThreadsColumns.RECIPIENT_IDS, recipientIds);
        values.put(ThreadsColumns.MESSAGE_COUNT, 0);
        values.put("is_privacy", privacy);
        return db.insert("threads", null, values);
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    private long getRecipientId(String address, SQLiteDatabase db) {
         if (!address.equals(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR)) {
             long id = getSingleAddressId(address, db);
             if (id != -1L) {
                 return id;
             } else {
                 Log.e(TAG, "getAddressIds: address ID not found for " + address);
             }
         }
         return 0;
    }
    /**
     * Return the canonical address ID for this address.
     */
    private long getSingleAddressId(String address, SQLiteDatabase db) {
        boolean isEmail = Mms.isEmailAddress(address);
        boolean isPhoneNumber = Mms.isPhoneNumber(address);
        // We lowercase all email addresses, but not addresses that aren't numbers, because
        // that would incorrectly turn an address such as "My Vodafone" into "my vodafone"
        // and the thread title would be incorrect when displayed in the UI.
        String refinedAddress = isEmail ? address.toLowerCase() : address;
        String selection = "address=?";
        String[] selectionArgs;
        long retVal = -1L;
        if (!isPhoneNumber || (address != null && address.length() > NORMAL_NUMBER_MAX_LENGTH)) {
            selectionArgs = new String[] { refinedAddress };
        } else {
            selection += " OR " + String.format(Locale.ENGLISH, "PHONE_NUMBERS_EQUAL(address, ?, %d)", 0);
            selectionArgs = new String[] { refinedAddress, refinedAddress };
        }
        Cursor cursor = null;

        try {
            cursor = db.query("canonical_addresses", CANONICAL_ADDRESSES_COLUMNS_2,
                    selection, selectionArgs, null, null, null);

            if (cursor.getCount() == 0) {
                retVal = insertCanonicalAddresses(db, refinedAddress);
 //               Xlog.d(TAG, "getSingleAddressId: insert new canonical_address for " + address + ", _id=" + retVal);
                return retVal;
            } else {
                while (cursor.moveToNext()) {
                    String currentNumber = cursor.getString(cursor.getColumnIndexOrThrow(CanonicalAddressesColumns.ADDRESS));
 //                   Xlog.d(TAG, "getSingleAddressId(): currentNumber != null ? " + (currentNumber != null));
                    if (currentNumber != null) {
                        if (refinedAddress.equals(currentNumber) || currentNumber.length() <= NORMAL_NUMBER_MAX_LENGTH) {
                            retVal = cursor.getLong(cursor.getColumnIndexOrThrow(CanonicalAddressesColumns._ID));
 //                           Xlog.d(TAG, "getSingleAddressId(): get exist id=" + retVal);
                            break;
                        }
                    }
                }
                if (retVal == -1) {
                    retVal = insertCanonicalAddresses(db, refinedAddress);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return retVal;
    }

    private long insertCanonicalAddresses(SQLiteDatabase db, String refinedAddress) {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(CanonicalAddressesColumns.ADDRESS, refinedAddress);
        return db.insert("canonical_addresses", CanonicalAddressesColumns.ADDRESS, contentValues);
    }
    
    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
        int deletedRows = 0;
        Uri deleteUri = null;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        //gionee gaoj 2012-9-20 added for CR00699291 start
        if (where != null && whereArgs != null && whereArgs[0].equals(GN_FOR_MULTIDELETE)) {
            deletedRows = deleteMultSmsMsg(where, whereArgs);
            // Aurora xuyong 2014-08-16 added for aurora's new feature start
            if (deletedRows > 0) {
                notifyChange(url);
                getContext().getContentResolver().delete(Threads.OBSOLETE_THREADS_URI, null, null);
            }
            // Aurora xuyong 2014-08-16 added for aurora's new feature end
            return deletedRows;
        }
        //gionee gaoj 2012-9-20 added for CR00699291 end
        if (where != null) {
            if (where.equals(FOR_MULTIDELETE)) {
  //              Xlog.d(TAG, "delete FOR_MULTIDELETE");
                db.beginTransaction();
                // Aurora xuyong 2015-01-04 modified for bug #10832 start
                try {
                    int message_id = 0;
                    deletedRows = 0;
                    for (int i=0; i<whereArgs.length; i++) {
                        deleteUri = null;
                        if (whereArgs[i] == null) {
                            //return deletedRows;
                        } else {
                            message_id = Integer.parseInt(whereArgs[i]);
                            deleteUri = ContentUris.withAppendedId(url, message_id);
                            Log.i(TAG, "message_id is " + message_id);
                            deletedRows += deleteOnce(deleteUri, null, null);    
                        }
                    }
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                }
                // Aurora xuyong 2015-01-04 modified for bug #10832 end
            } else if (where.equals(FOR_FOLDERMODE_MULTIDELETE)){
  //              Xlog.d(TAG, "delete folder mode FOR_MULTIDELETE");
                String boxType = "0";
                if(whereArgs != null && whereArgs.length > 0){
                    if ("4".equals(whereArgs[whereArgs.length-1])) {
                        boxType = "(" + Sms.MESSAGE_TYPE_OUTBOX + "," + 
                        Sms.MESSAGE_TYPE_FAILED + "," + Sms.MESSAGE_TYPE_QUEUED +")"; //outbox,failed,queue
                    } else {
                        boxType = "(" + whereArgs[whereArgs.length-1] + ")";
                    }
                }
                String unSelectids = getSmsIds(whereArgs);
                String finalSelection = String.format("type IN %s AND _id NOT IN %s", boxType, unSelectids);
 //               Xlog.d(TAG, "delete folder mode FOR_MULTIDELETE finalSelection = "+ finalSelection);
                String unSelectWordids = getWordIds(db, boxType, unSelectids);
                db.execSQL(String.format("delete from words where _id NOT IN %s AND table_to_use=1", unSelectWordids));
                //Aurora yudingmin 2014-11-03 added for sync feature start
                deleteBeforeSms(finalSelection, null);
                //Aurora yudingmin 2014-11-03 added for sync feature end
                deletedRows = db.delete(TABLE_SMS, finalSelection, null);
  //              Xlog.d(TAG, "delete folder mode FOR_MULTIDELETE end unSelectids " + unSelectids);
            } else {
                deletedRows = deleteOnce(url, where, whereArgs);
            }
        } else {
            deletedRows = deleteOnce(url, where, whereArgs);
        }
        if (deletedRows > 0) {
            notifyChange(url);
            getContext().getContentResolver().delete(Threads.OBSOLETE_THREADS_URI, null, null);
        }
        return deletedRows;
    }

    //gionee gaoj 2012-9-20 added for CR00699291 start
    private int deleteMultSmsMsg(String where, String[] whereArgs) {
        // TODO Auto-generated method stub

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int thread_id = Integer.parseInt(whereArgs[1]);

        // Delete the specified message.
        Log.d("Test", "   thread_id = "+thread_id+"   where = "+where);
        //Aurora yudingmin 2014-11-03 added for sync feature start
        where = "_id=" + where;
        deleteBeforeSms(where, null);
        int rows = db.delete("sms", where, null);
        //Aurora yudingmin 2014-11-03 added for sync feature end
        if (thread_id > 0) {
            // Update its thread.
            MmsSmsDatabaseHelper.updateThread(db, thread_id);
        }
        return rows;
    }
    //gionee gaoj 2012-9-20 added for CR00699291 end

    //get the select id from sms to delete words
    private String getWordIds(SQLiteDatabase db, String boxType, String selectionArgs){
        StringBuffer content = new StringBuffer("(");
        String res = "";
        String rawQuery = String.format("select _id from sms where _id NOT IN (select _id from sms where type IN %s AND _id NOT IN %s)", boxType, selectionArgs);
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(rawQuery, null);
            if (cursor == null || cursor.getCount() == 0){
                return "()";
            }
            if (cursor.moveToFirst()) {
                do {
                    content.append(cursor.getInt(0));
                    content.append(",");
                } while (cursor.moveToNext());
                res = content.toString();
                if (!TextUtils.isEmpty(content) && res.endsWith(",")) {
                    res = res.substring(0, res.lastIndexOf(","));
                } 
                res += ")";
            }
//            Xlog.d(TAG, "getWordIds cursor content = " + res + " COUNT " + cursor.getCount());
    
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return res;
    }

    private String getSmsIds(String[] selectionArgs){
        StringBuffer content = new StringBuffer("(");
        String res = "";
        if (selectionArgs == null || selectionArgs.length <= 1){
            return "()";
        }
        
        for (int i = 0; i < selectionArgs.length - 2; i++){
            if (selectionArgs[i] == null){
                break;
            }
            content.append(selectionArgs[i]);
            content.append(",");
        }
        if (selectionArgs[selectionArgs.length-2] != null){
           content.append(selectionArgs[selectionArgs.length-2]);
        }
        res = content.toString();
        if (res.endsWith(",")) {
            res = res.substring(0, res.lastIndexOf(","));
        }
        res += ")";
        return res;
    }
    
    public int deleteOnce(Uri url, String where, String[] whereArgs) {
        int count;
        int match = sURLMatcher.match(url);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (match) {
            case SMS_ALL:
                //Aurora yudingmin 2014-11-03 added for sync feature start
                deleteBeforeSms(where, whereArgs);
                //Aurora yudingmin 2014-11-03 added for sync feature end
                count = db.delete(TABLE_SMS, where, whereArgs);
                if (count != 0) {
                    // Don't update threads unless something changed.
                    MmsSmsDatabaseHelper.updateAllThreads(db, where, whereArgs);
                }
                break;

            case SMS_ALL_ID:
                try {
                    count = 0;
                    int message_id = Integer.parseInt(url.getPathSegments().get(0));
                    //Aurora yudingmin 2014-11-03 added for sync feature start
                    deleteBeforeSmsId(message_id);
                    //Aurora yudingmin 2014-11-03 added for sync feature end
                    count = MmsSmsDatabaseHelper.deleteOneSms(db, message_id);
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        "Bad message id: " + url.getPathSegments().get(0));
                }
                break;

            case SMS_CONVERSATIONS_ID:
                int threadID;

                try {
                    threadID = Integer.parseInt(url.getPathSegments().get(1));
                } catch (Exception ex) {
                    throw new IllegalArgumentException(
                            "Bad conversation thread id: "
                            + url.getPathSegments().get(1));
                }

                // delete the messages from the sms table
                where = DatabaseUtils.concatenateWhere("thread_id=" + threadID, where);
                //Aurora yudingmin 2014-11-03 added for sync feature start
                deleteBeforeSms(where, whereArgs);
                //Aurora yudingmin 2014-11-03 added for sync feature end
                count = db.delete(TABLE_SMS, where, whereArgs);
                MmsSmsDatabaseHelper.updateThread(db, threadID);
                break;

            case SMS_RAW_MESSAGE:
                count = db.delete("raw", where, whereArgs);
                break;

            case SMS_STATUS_PENDING:
                count = db.delete("sr_pending", where, whereArgs);
                break;

            case SMS_ICC:
                String messageIndexString = url.getPathSegments().get(1);
                if (mGnMultiSimMessage) {//guoyx 20130227
                    Log.i(TAG, "Delete Sim1 SMS id: " + messageIndexString);
                    return deleteMessageFromIcc(messageIndexString,
                            GnPhone.GEMINI_SIM_1);
                } else {
                    return deleteMessageFromIcc(messageIndexString);
                }
            case SMS_ICC_GEMINI:
                String messageIndexString_1 = url.getPathSegments().get(1);
                Log.i(TAG, "Delete Sim2 SMS id: " + messageIndexString_1);
                return deleteMessageFromIcc(messageIndexString_1, GnPhone.GEMINI_SIM_2);
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
/*
        if (count > 0) {
            notifyChange(url);
        }
        */
        return count;
    }

    /**
     * Delete the message at index from ICC.  Return true iff
     * successful.
     */
    private int deleteMessageFromIcc(String messageIndexString) {
        SmsManager smsManager = SmsManager.getDefault();
        // Aurora xuyong 2014-06-11 modified for android4.4 new feature start
        // use phone app permissions to avoid UID mismatch in AppOpsManager.noteOp() call
        // Aurora xuyong 2014-06-17 modified for adapt for both 4.3 & 4.4 feature start
        long token = -1l;
        if (hasKitKat()) {
            // Aurora xuyong 2014-07-02 modified for bug #6305 start
              token = Binder.clearCallingIdentity();
            // Aurora xuyong 2014-07-02 modified for bug #6305 end
        }
        // Aurora xuyong 2014-06-17 modified for adapt for both 4.3 & 4.4 feature end
        try {
            // For Delete all,MTK FW support delete all using messageIndex = -1;
            if (messageIndexString.equals(ALL_SMS)) {
                return smsManager.deleteMessageFromIcc(-1) ? 1 : 0;
            } else {
            return smsManager.deleteMessageFromIcc(
                Integer.parseInt(messageIndexString)) ? 1 : 0;
            }
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Bad SMS ICC ID: " + messageIndexString);
        }  finally {
            ContentResolver cr = getContext().getContentResolver();

            cr.notifyChange(ICC_URI, null);
            // Aurora xuyong 2014-06-17 modified for adapt for both 4.3 & 4.4 feature start
            if (hasKitKat()) {
                Binder.restoreCallingIdentity(token);
            }
            // Aurora xuyong 2014-06-17 modified for adapt for both 4.3 & 4.4 feature end
        }
        // Aurora xuyong 2014-06-11 modified for android4.4 new feature end
    }

    /**
     * Delete the message at index from ICC.  Return true iff
     * successful.
     */
    private int deleteMessageFromIcc(String messageIndexString, int slotId) {
       // Aurora xuyong 2014-06-11 modified for android4.4 new feature start
        // Aurora xuyong 2014-06-17 modified for adapt for both 4.3 & 4.4 feature start
         long token = -1l;
        if (hasKitKat()) {
            // Aurora xuyong 2014-07-02 modified for bug #6305 start
              token = Binder.clearCallingIdentity();
            // Aurora xuyong 2014-07-02 modified for bug #6305 end
        }
        // Aurora xuyong 2014-06-17 modified for adapt for both 4.3 & 4.4 feature end
        try {
            // For Delete all,MTK FW support delete all using messageIndex = -1;
            if (messageIndexString.equals(ALL_SMS)) {
                //Gionee guoyx 20130227 added for CR00772069 begin
                if (mQcMultiSimEnabled) {
                    ArrayList<SmsMessage> messages = GnGeminiSmsManager.getAllMessagesFromIccGemini(slotId);
                    int total = messages.size();
                    int result = 0;
                    for (SmsMessage message : messages) {
                        result += GnGeminiSmsManager.deleteMessageFromIccGemini(message.getIndexOnIcc(), slotId) ? 1 : 0;
                    }
                    Log.d(TAG, "deleteMessageFromIcc slot:" + slotId + " message total:" 
                           + total + " delete total:" + result);
                    return result;
                } else {
                    return GnGeminiSmsManager.deleteMessageFromIccGemini(-1, slotId) ? 1 : 0;
                }
                //Gionee guoyx 20130227 added for CR00772069 end
            } else {
                return GnGeminiSmsManager.deleteMessageFromIccGemini(
                         Integer.parseInt(messageIndexString), slotId) ? 1 : 0;
            }
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Bad SMS ICC ID: " + messageIndexString);
        }  finally {
            ContentResolver cr = getContext().getContentResolver();
            if (slotId == 0) {
                cr.notifyChange(ICC_URI, null);
            } else {
                cr.notifyChange(ICC_URI_GEMINI, null);
            }
            // Aurora xuyong 2014-06-17 modified for adapt for both 4.3 & 4.4 feature start
            if (hasKitKat()) {
                Binder.restoreCallingIdentity(token);
            }
            // Aurora xuyong 2014-06-17 modified for adapt for both 4.3 & 4.4 feature end
        }
        // Aurora xuyong 2014-06-11 modified for android4.4 new feature end
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
//        Xlog.d(TAG, "update begin");
          // Aurora liugj 2013-09-17 modified for iuni-4.3 adapter start
         if (values.containsKey("sub_id")) {
            values.remove("sub_id");
        }
        // Aurora liugj 2013-09-17 modified for iuni-4.3 adapter end
        // gionee zhouyj 2012-07-31 add for CR00652308 start 
        if (mGnSmsBackupSupport) {
            mImportNormal = true;
        }
        // gionee zhouyj 2012-07-31 add for CR00652308 end 
        int count = 0;
        String table = TABLE_SMS;
        String extraWhere = null;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sURLMatcher.match(url)) {
            case SMS_RAW_MESSAGE:
                table = TABLE_RAW;
                break;

            case SMS_STATUS_PENDING:
                table = TABLE_SR_PENDING;
                break;

            case SMS_ALL:
            case SMS_FAILED:
            case SMS_QUEUED:
            case SMS_INBOX:
            case SMS_SENT:
            case SMS_DRAFT:
            case SMS_OUTBOX:
            case SMS_CONVERSATIONS:
                break;
            case SMS_ALL_ID:
                extraWhere = "_id=" + url.getPathSegments().get(0);
                break;

            case SMS_INBOX_ID:
            case SMS_FAILED_ID:
            case SMS_SENT_ID:
            case SMS_DRAFT_ID:
            case SMS_OUTBOX_ID:
            // gionee zhouyj 2012-07-06 add for CR00637457 start
            case SMS_STARS:
            // gionee zhouyj 2012-07-06 add for CR00637457 end
                extraWhere = "_id=" + url.getPathSegments().get(1);
                break;

            case SMS_CONVERSATIONS_ID: {
                String threadId = url.getPathSegments().get(1);

                try {
                    Integer.parseInt(threadId);
                } catch (Exception ex) {
                    Log.e(TAG, "Bad conversation thread id: " + threadId);
                    break;
                }

                extraWhere = "thread_id=" + threadId;
                break;
            }

            case SMS_STATUS_ID:
                extraWhere = "_id=" + url.getPathSegments().get(1);
                break;

            default:
                throw new UnsupportedOperationException(
                        "URI " + url + " not supported");
        }

        where = DatabaseUtils.concatenateWhere(where, extraWhere);
        //Aurora yudingmin 2014-11-03 added for sync feature start
        if(table.equals(TABLE_SMS)){
            updateBeforeSms(where, whereArgs);
        }
        //Aurora yudingmin 2014-11-03 added for sync feature end
        count = db.update(table, values, where, whereArgs);

        if (count > 0) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.d(TAG, "update " + url + " succeeded");
            }
            notifyChange(url);
        }
//        Xlog.d(TAG, "update end");
        return count;
    }
    
    // gionee zhouyj 2012-11-22 add for CR00733163 start 
    @Override
    public AssetFileDescriptor openAssetFile(Uri uri, String mode)
        throws FileNotFoundException {
        return super.openAssetFile(uri, mode);
    }
    
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        // TODO Auto-generated method stub
        if(null!=uri && uri.equals(Uri.parse("content://sms/syncTag.txt"))){
            String dir = getContext().getFilesDir().getAbsolutePath();
            String source = dir+File.separator+uri.getEncodedPath();  
            File path=new File(source);
            int imode = 0;
            if (mode.contains("w")) {
                imode |= ParcelFileDescriptor.MODE_WRITE_ONLY;
                if (!path.exists()) {
                    try {
                        path.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (mode.contains("r"))
                imode |= ParcelFileDescriptor.MODE_READ_ONLY;
            return ParcelFileDescriptor.open(path, imode);
        }
        return super.openFile(uri, mode);
    }
    
    @Override
    public ContentProviderResult[] applyBatch(
            ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        // TODO Auto-generated method stub
        Log.i("TAG", "SmsProvider   applyBatch");
        if (operations == null || operations.size() < 1) {
            return null;
        }
        int type = operations.get(0).getType();
        if (type == ContentProviderOperation.TYPE_INSERT) {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            db.beginTransaction();
            try {
                for (int i = 0; i < numOperations; i++) {
                    ContentProviderOperation op = operations.get(i);
                    ContentValues value = op.resolveValueBackReferences(results, i);
                    // Aurora xuyong 2014-03-20 modified for bug #3138 start
                    String addressValue = value.getAsString("address");
                    addressValue = PhoneNumberUtils.stripSeparators(addressValue);
                    
                    String where = "address= '" + addressValue + "'";
                    if (addressValue.length() > MIN_MATCH) {
                        where = where + " OR " + "address like '%" + addressValue.substring(addressValue.length() - MIN_MATCH) + "'";
                    // Aurora xuyong 2014-03-20 modified for bug #3138 end
                    }
                    Cursor cursor = db.query("canonical_addresses", null, where, null, null, null, null);
                    if (null != cursor && cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        int threadId = cursor.getInt(0);
                        Cursor c = db.query("threads", null, "recipient_ids='" + String.valueOf(threadId) + "'", null, null, null, null);
                        if (null == c || c.getCount() < 1) {
                            ContentValues val = new ContentValues();
                            val.put("recipient_ids", String.valueOf(threadId));
                            threadId = (int) db.insert("threads", "_id", val);
                        } else {
                            c.moveToFirst();
                            threadId = c.getInt(0);
                        }
                        value.put("thread_id", threadId);
                        if (c != null) c.close();
                    } else {
                        ContentValues val = new ContentValues();
                        // Aurora xuyong 2014-03-20 modified for bug #3138 start
                        val.put("address", addressValue);
                        // Aurora xuyong 2014-03-20 modified for bug #3138 end
                        int threadId = (int) db.insert("canonical_addresses", "_id", val);
                        ContentValues val_id = new ContentValues();
                        val_id.put("recipient_ids", String.valueOf(threadId));
                        threadId = (int) db.insert("threads", "_id", val_id);
                        value.put("thread_id", threadId);
                    }
                    if (cursor !=null && !cursor.isClosed()) {
                        cursor.close();
                    }
                    
                    long rowID = db.insert(TABLE_SMS, "_id", value);
                    ContentValues cv = new ContentValues();
                    cv.put(Telephony.MmsSms.WordsTable.ID, rowID);
                    cv.put(Telephony.MmsSms.WordsTable.INDEXED_TEXT, value.getAsString("body"));
                    cv.put(Telephony.MmsSms.WordsTable.SOURCE_ROW_ID, rowID);
                    cv.put(Telephony.MmsSms.WordsTable.TABLE_ID, 1);
                    db.insert(TABLE_WORDS, Telephony.MmsSms.WordsTable.INDEXED_TEXT, cv);
                    results[i] = new ContentProviderResult(Uri.parse("content://sms/" + rowID));
                }
                if (db.inTransaction()) {
                    db.setTransactionSuccessful();
                }
            } catch (Exception e) {
                Log.i(TAG, "SmsProvider   applyBatch   Exception e = " + e);
            } finally {
                db.endTransaction();
            }
            return results;
        } else {
            return super.applyBatch(operations);
        }
    }
    // gionee zhouyj 2012-11-22 add for CR00733163 end 

    private void notifyChange(Uri uri) {
        ContentResolver cr = getContext().getContentResolver();
        cr.notifyChange(uri, null);
        cr.notifyChange(MmsSms.CONTENT_URI, null);
        cr.notifyChange(Uri.parse("content://mms-sms/conversations/"), null);
    }

    private SQLiteOpenHelper mOpenHelper;
    // Aurora yudingmin 2014-11-19 added for bug #9304 start
    private AuroraSimIdMatching mAuroraSimIdMatching;
    // Aurora yudingmin 2014-11-19 added for bug #9304 end

    private final static String TAG = "SmsProvider";
    private final static String VND_ANDROID_SMS = "vnd.android.cursor.item/sms";
    private final static String VND_ANDROID_SMSCHAT =
            "vnd.android.cursor.item/sms-chat";
    private final static String VND_ANDROID_DIR_SMS =
            "vnd.android.cursor.dir/sms";

    private static final HashMap<String, String> sConversationProjectionMap =
            new HashMap<String, String>();
    private static final String[] sIDProjection = new String[] { "_id" };

    private static final int SMS_ALL = 0;
    private static final int SMS_ALL_ID = 1;
    private static final int SMS_INBOX = 2;
    private static final int SMS_INBOX_ID = 3;
    private static final int SMS_SENT = 4;
    private static final int SMS_SENT_ID = 5;
    private static final int SMS_DRAFT = 6;
    private static final int SMS_DRAFT_ID = 7;
    private static final int SMS_OUTBOX = 8;
    private static final int SMS_OUTBOX_ID = 9;
    private static final int SMS_CONVERSATIONS = 10;
    private static final int SMS_CONVERSATIONS_ID = 11;
    private static final int SMS_RAW_MESSAGE = 15;
    private static final int SMS_ATTACHMENT = 16;
    private static final int SMS_ATTACHMENT_ID = 17;
    private static final int SMS_NEW_THREAD_ID = 18;
    private static final int SMS_QUERY_THREAD_ID = 19;
    private static final int SMS_STATUS_ID = 20;
    private static final int SMS_STATUS_PENDING = 21;
    private static final int SMS_ALL_ICC = 22;
    private static final int SMS_ICC = 23;
    private static final int SMS_FAILED = 24;
    private static final int SMS_FAILED_ID = 25;
    private static final int SMS_QUEUED = 26;
    private static final int SMS_UNDELIVERED = 27;
    private static final int SMS_ALL_ICC_GEMINI = 28;
    private static final int SMS_ICC_GEMINI = 29;
    // Gionee fangbin 20111108 added for CR00423907 start
    private static final int SMS_IMPORT_ALL = 30;
    // Gionee fangbin 20111108 added for CR00423907 end
    // Gionee fangbin 20111128 added for CR00448323 start
    private static final int SMS_IMPORT_CANCEL = 31;
    // Gionee fangbin 20111128 added for CR00448323 end
    // gionee zhouyj 2012-07-06 add for CR00637457 start
    private static final int SMS_STARS      = 32;
    // gionee zhouyj 2012-07-06 add for CR00637457 end
    // gionee zhouyj 2012-08-27 add for CR00667581 start
    private static final int SMS_IMPORT_CHANGE_MODE = 33;
    // gionee zhouyj 2012-08-27 add for CR00667581 end
    // Aurora xuyong 2014-11-25 added for bug #10052 start
    private static final int SMS_QUEUED_VIA = 34;
    // Aurora xuyong 2014-11-25 added for bug #10052 end
    private static final UriMatcher sURLMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURLMatcher.addURI("sms", null, SMS_ALL);
        sURLMatcher.addURI("sms", "#", SMS_ALL_ID);
        sURLMatcher.addURI("sms", "inbox", SMS_INBOX);
        sURLMatcher.addURI("sms", "inbox/#", SMS_INBOX_ID);
        sURLMatcher.addURI("sms", "sent", SMS_SENT);
        sURLMatcher.addURI("sms", "sent/#", SMS_SENT_ID);
        sURLMatcher.addURI("sms", "draft", SMS_DRAFT);
        sURLMatcher.addURI("sms", "draft/#", SMS_DRAFT_ID);
        sURLMatcher.addURI("sms", "outbox", SMS_OUTBOX);
        sURLMatcher.addURI("sms", "outbox/#", SMS_OUTBOX_ID);
        sURLMatcher.addURI("sms", "undelivered", SMS_UNDELIVERED);
        sURLMatcher.addURI("sms", "failed", SMS_FAILED);
        sURLMatcher.addURI("sms", "failed/#", SMS_FAILED_ID);
        sURLMatcher.addURI("sms", "queued", SMS_QUEUED);
        // Aurora xuyong 2014-11-25 added for bug #10052 start
        sURLMatcher.addURI("sms", "queued/via", SMS_QUEUED_VIA);
        // Aurora xuyong 2014-11-25 added for bug #10052 end
        sURLMatcher.addURI("sms", "conversations", SMS_CONVERSATIONS);
        sURLMatcher.addURI("sms", "conversations/*", SMS_CONVERSATIONS_ID);
        sURLMatcher.addURI("sms", "raw", SMS_RAW_MESSAGE);
        sURLMatcher.addURI("sms", "attachments", SMS_ATTACHMENT);
        sURLMatcher.addURI("sms", "attachments/#", SMS_ATTACHMENT_ID);
        sURLMatcher.addURI("sms", "threadID", SMS_NEW_THREAD_ID);
        sURLMatcher.addURI("sms", "threadID/*", SMS_QUERY_THREAD_ID);
        sURLMatcher.addURI("sms", "status/#", SMS_STATUS_ID);
        sURLMatcher.addURI("sms", "sr_pending", SMS_STATUS_PENDING);
        sURLMatcher.addURI("sms", "icc", SMS_ALL_ICC);
        sURLMatcher.addURI("sms", "icc/#", SMS_ICC);
        //we keep these for not breaking old applications
        sURLMatcher.addURI("sms", "sim", SMS_ALL_ICC);
        sURLMatcher.addURI("sms", "sim/#", SMS_ICC);

        sURLMatcher.addURI("sms", "icc2", SMS_ALL_ICC_GEMINI);
        sURLMatcher.addURI("sms", "icc2/#", SMS_ICC_GEMINI);
        //we keep these for not breaking old applications
        sURLMatcher.addURI("sms", "sim2", SMS_ALL_ICC_GEMINI);
        sURLMatcher.addURI("sms", "sim2/#", SMS_ICC_GEMINI);
        
        // Gionee fangbin 20111108 added for CR00423907 start
        sURLMatcher.addURI("sms", "import/#", SMS_IMPORT_ALL);
        // Gionee fangbin 20111108 added for CR00423907 end
        // Gionee fangbin 20111128 added for CR00448323 start
        sURLMatcher.addURI("sms", "cancel_import", SMS_IMPORT_CANCEL);
        // Gionee fangbin 20111128 added for CR00448323 end
        // gionee zhouyj 2012-07-06 add for CR00637457 start
        sURLMatcher.addURI("sms", "star_more/*", SMS_STARS);
        // gionee zhouyj 2012-07-06 add for CR00637457 end
        // gionee zhouyj 2012-08-27 add for CR00667581 start
        sURLMatcher.addURI("sms", "import_change_mode", SMS_IMPORT_CHANGE_MODE);
        // gionee zhouyj 2012-08-27 add for CR00667581 end

        sConversationProjectionMap.put(Sms.Conversations.SNIPPET,
            "sms.body AS snippet");
        sConversationProjectionMap.put(Sms.Conversations.THREAD_ID,
            "sms.thread_id AS thread_id");
        sConversationProjectionMap.put(Sms.Conversations.MESSAGE_COUNT,
            "groups.msg_count AS msg_count");
        sConversationProjectionMap.put("delta", null);
    }
}
