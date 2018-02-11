/*
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

package com.android.providers.telephony;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
// Aurora xuyong 2014-08-14 added for aurora's new feature start
import android.database.CursorIndexOutOfBoundsException;
// Aurora xuyong 2014-08-14 added for aurora's new feature end
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
// Aurora xuyong 2014-08-05 added for reject start
import android.database.sqlite.SQLiteException;
// Aurora xuyong 2014-08-05 added for reject end
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
// Aurora xuyong 2014-11-06 added for bug #9685 start
import android.provider.ContactsContract.Data;
// Aurora xuyong 2014-11-06 added for bug #9685 end
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Telephony;
import android.provider.Telephony.CanonicalAddressesColumns;
//import android.provider.Telephony.Mms;
import gionee.provider.GnTelephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
//import android.provider.Telephony.Threads;
import gionee.provider.GnTelephony.Threads;
import android.provider.Telephony.ThreadsColumns;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.provider.Telephony.Sms.Conversations;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
//import com.mediatek.xlog.Xlog;
import gionee.provider.GnTelephony.Mms;

import com.google.android.mms.pdu.PduHeaders;
import com.privacymanage.service.AuroraPrivacyUtils;
//Add for WapPush
import android.content.ContentUris;
//import android.provider.Telephony.WapPush;
import gionee.provider.GnTelephony.WapPush;
import com.aurora.featureoption.FeatureOption;

//gionee gaoj 2012-3-27 added for CR00555790 start
import android.database.DataSetObserver;
import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.database.MatrixCursor;
import android.content.SharedPreferences;
//gionee gaoj 2012-3-27 added for CR00555790 end

import android.os.SystemProperties;

/**
 * This class provides the ability to query the MMS and SMS databases
 * at the same time, mixing messages from both in a single thread
 * (A.K.A. conversation).
 *
 * A virtual column, MmsSms.TYPE_DISCRIMINATOR_COLUMN, may be
 * requested in the projection for a query.  Its value is either "mms"
 * or "sms", depending on whether the message represented by the row
 * is an MMS message or an SMS message, respectively.
 *
 * This class also provides the ability to find out what addresses
 * participated in a particular thread.  It doesn't support updates
 * for either of these.
 *
 * This class provides a way to allocate and retrieve thread IDs.
 * This is done atomically through a query.  There is no insert URI
 * for this.
 *
 * Finally, this class provides a way to delete or update all messages
 * in a thread.
 */
public class MmsSmsProvider extends ContentProvider {
   // Aurora xuyong 2014-09-19 added for aurora reject feature start
    // we use this uri to notify the obsevers to refresh the notifications triggered by the rejected messages. 
    public static final Uri REJECT_NOTIFY_URI = Uri.parse("content://reject/notify");
    public static final Uri PRIVACY_NOTIFY_URI = Uri.parse("content://privacy/notify");
   // Aurora xuyong 2014-09-19 added for aurora reject feature end
    private static final UriMatcher URI_MATCHER =
            new UriMatcher(UriMatcher.NO_MATCH);
    private static final String LOG_TAG = "MmsSmsProvider";
    private static final String WAPPUSH_TAG = "WapPush/Provider";
    private static final boolean DEBUG = false;
    // Aurora xuyong 2014-07-03 added for reject feature start
    private static final boolean sHasRejectFeature = SystemProperties.get("ro.aurora.reject.support").equals("yes");
    // Aurora xuyong 2014-07-03 added for reject feature end
    private static final String NO_DELETES_INSERTS_OR_UPDATES =
            "MmsSmsProvider does not support deletes, inserts, or updates for this URI.";
    private static final int URI_CONVERSATIONS                     = 0;
    private static final int URI_CONVERSATIONS_MESSAGES            = 1;
    private static final int URI_CONVERSATIONS_RECIPIENTS          = 2;
    private static final int URI_MESSAGES_BY_PHONE                 = 3;
    private static final int URI_THREAD_ID                         = 4;
    // Aurora xuyong 2014-07-14 added for aurora's new feature start
    private static final int URI_THREAD_IDS                        = 41;
    // Aurora xuyong 2014-07-14 added for aurora's new feature end
    private static final int URI_CANONICAL_ADDRESS                 = 5;
    private static final int URI_PENDING_MSG                       = 6;
    private static final int URI_COMPLETE_CONVERSATIONS            = 7;
    private static final int URI_UNDELIVERED_MSG                   = 8;
    private static final int URI_CONVERSATIONS_SUBJECT             = 9;
    private static final int URI_NOTIFICATIONS                     = 10;
    private static final int URI_OBSOLETE_THREADS                  = 11;
    private static final int URI_DRAFT                             = 12;
    private static final int URI_CANONICAL_ADDRESSES               = 13;
    private static final int URI_SEARCH                            = 14;
    private static final int URI_SEARCH_SUGGEST                    = 15;
    private static final int URI_FIRST_LOCKED_MESSAGE_ALL          = 16;
    private static final int URI_FIRST_LOCKED_MESSAGE_BY_THREAD_ID = 17;
    private static final int URI_MESSAGE_ID_TO_THREAD              = 18;
    private static final int URI_QUICK_TEXT                        = 19;
    private static final int URI_MESSAGES_INBOX                    = 20;
    private static final int URI_MESSAGES_OUTBOX                   = 21;
    private static final int URI_MESSAGES_SENTBOX                  = 22;
    private static final int URI_MESSAGES_DRAFTBOX                 = 23;
    private static final int URI_RECIPIENTS_NUMBER                 = 24;
    private static final int URI_SEARCH_FOLDER                     = 25;
    private static final int URI_STATUS                            = 26;
    private static final int URI_CELLBROADCAST                     = 27;
    private static final int NORMAL_NUMBER_MAX_LENGTH              = 15;

     //gionee gaoj 2012-3-27 added for CR00555790 start
    private static final int URI_RECENT_CONTACT                    = 30;
    private static final int URI_FIRST_STARED_MESSAGE_ALL = 31;
    private static final int URI_FIRST_STARED_MESSAGE_BY_THREAD_ID = 32;
    private static final int URI_ENCRYPTION                        = 33;
    private static final int URI_SMS_PSW                           = 34;
    // gionee zhouyj 2012-06-28 add for CR00628704 start 
    private static final int URI_CONVERSATIONSES                   = 35;
    // gionee zhouyj 2012-06-28 add for CR00628704 end
    // Aurora xuyong 2014-07-02 added for reject feature start
    private static final int AURORA_REJECT_CONVERSATIONS           = 36;
    // Aurora xuyong 2014-08-05 modified for reject start
    private static final int AURORA_CHANGE_ALL_MESSAGES            = 37;
    // Aurora xuyong 2014-08-05 modified for reject end
    // Aurora xuyong 2014-08-14 added for aurora's new feature start
    private static final int URI_QUICKQUERY_CONVERSATIONS_MESSAGES = 38;
    // Aurora xuyong 2014-08-14 added for aurora's new feature end
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private static final int URI_PRIVACY_ACCOUNT_ID_OP             = 39;
    private static final int URI_PRIVACY_ACCOUNT_CHANGE            = 40;
    private static final int URI_PRIVACY_ACCOUNT_ADDRESS           = 42;
    // Aurora xuyong 2014-10-23 added for privacy feature end
    // Aurora xuyong 2014-07-02 added for reject feature end
    //Aurora yudingmin 2014-11-05 added for aurora special feature start
    private static final int URI_AURORA_SPECIAL_FEATURE           = 43;
    //Aurora yudingmin 2014-11-05 added for aurora special feature end
    // Aurora xuyong 2014-12-03 added for debug start
    private static final int URI_AURORA_UPDATETHREADS           = 44;
    // Aurora xuyong 2014-12-03 added for debug end
    private boolean mEncryption = SystemProperties.get("ro.gn.encryption.prop").equals("yes");
    //gionee guoyangxu 20120606 modified for CR00622143 start
    private boolean mMsgBox = MmsSmsDatabaseHelper.mIsMsgBox;
    //gionee guoyangxu 20120606 modified for CR00622143 end
    //gionee gaoj 2012-3-27 added for CR00555790 end
    // Gionee lixiaohu 20120802 add for CR00663721 start
    private static final boolean gnFLYflag = SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY"); 
    // Gionee lixiaohu 20120802 add for CR00663721 end

    //Gionee qinkai 2012-09-13 added for CR00692926 start
    private static final boolean gnNgmflag = SystemProperties.get("ro.gn.oversea.custom").equals("ITALY_NGM");
    //Gionee qinkai 2012-09-13 added for CR00692926 end

    /**
     * the name of the table that is used to store the queue of
     * messages(both MMS and SMS) to be sent/downloaded.
     */
    public static final String TABLE_PENDING_MSG = "pending_msgs";

    /**
     * the name of the table that is used to store the canonical addresses for both SMS and MMS.
     */
    private static final String TABLE_CANONICAL_ADDRESSES = "canonical_addresses";
    /**
     * the name of the table quicktext
     */
    private static final String TABLE_QUICK_TEXT = "quicktext";
    
    private static final String TABLE_CELLBROADCAST = "cellbroadcast";

    private static final String TABLE_THREADS = "threads";

     //gionee gaoj 2012-3-27 added for CR00555790 start
    private static final String TABLE_RECENT_CONTACT_ADDRESSES = "recent_contact";
     //gionee gaoj 2012-3-27 added for CR00555790 end
    
    // These constants are used to construct union queries across the
    // MMS and SMS base tables.

    // These are the columns that appear in both the MMS ("pdu") and
    // SMS ("sms") message tables.
    
     //gionee gaoj 2012-3-27 added for CR00555790 start
    private static final String[] MMS_SMS_COLUMNS =
            { BaseColumns._ID, Mms.DATE, Mms.DATE_SENT, Mms.READ, Mms.THREAD_ID, Mms.LOCKED, Mms.SIM_ID,"star"};
     //gionee gaoj 2012-3-27 added for CR00555790 end

    // These are the columns that appear only in the MMS message
    // table.
    private static final String[] MMS_ONLY_COLUMNS = {
        Mms.CONTENT_CLASS, Mms.CONTENT_LOCATION, Mms.CONTENT_TYPE,
        Mms.DELIVERY_REPORT, Mms.EXPIRY, Mms.MESSAGE_CLASS, Mms.MESSAGE_ID,
        Mms.MESSAGE_SIZE, Mms.MESSAGE_TYPE, Mms.MESSAGE_BOX, Mms.PRIORITY,
        Mms.READ_STATUS, Mms.RESPONSE_STATUS, Mms.RESPONSE_TEXT,
        Mms.RETRIEVE_STATUS, Mms.RETRIEVE_TEXT_CHARSET, Mms.REPORT_ALLOWED,
        Mms.READ_REPORT, Mms.STATUS, Mms.SUBJECT, Mms.SUBJECT_CHARSET,
        Mms.TRANSACTION_ID, Mms.MMS_VERSION, Mms.SERVICE_CENTER };

    // These are the columns that appear only in the SMS message
    // table.
    private static final String[] SMS_ONLY_COLUMNS =
            { "address", "body", "person", "reply_path_present",
              "service_center", "status", "subject", "type", "error_code", "weather_info"};
    private static final String[] CB_ONLY_COLUMNS =
            { "channel_id" };

    // These are all the columns that appear in the "threads" table.
    private static final String[] THREADS_COLUMNS = {
        BaseColumns._ID,
        ThreadsColumns.DATE,
        ThreadsColumns.RECIPIENT_IDS,
        ThreadsColumns.MESSAGE_COUNT
    };

    private static final String[] CANONICAL_ADDRESSES_COLUMNS_1 =
            new String[] { CanonicalAddressesColumns.ADDRESS };

    private static final String[] CANONICAL_ADDRESSES_COLUMNS_2 =
            new String[] { CanonicalAddressesColumns._ID,
                    CanonicalAddressesColumns.ADDRESS };

     //gionee gaoj 2012-3-27 added for CR00555790 start
    private static final String[] RECENT_CONTACT_COLUMNSRECENT_CONTACT_COLUMNS =
            new String[] { "_id","number"};
     //gionee gaoj 2012-3-27 added for CR00555790 end

    // These are all the columns that appear in the MMS and SMS
    // message tables.
    private static final String[] UNION_COLUMNS =
            new String[MMS_SMS_COLUMNS.length
                       + MMS_ONLY_COLUMNS.length
                       + SMS_ONLY_COLUMNS.length];

    // These are all the columns that appear in the MMS table.
    private static final Set<String> MMS_COLUMNS = new HashSet<String>();

    // These are all the columns that appear in the SMS table.
    private static final Set<String> SMS_COLUMNS = new HashSet<String>();
    private static final Set<String> CB_COLUMNS = new HashSet<String>();

    private static final String VND_ANDROID_DIR_MMS_SMS =
            "vnd.android-dir/mms-sms";

    private static final String[] ID_PROJECTION = { BaseColumns._ID };

    private static final String[] STATUS_PROJECTION = { Threads.STATUS };
    
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static final String SMS_CONVERSATION_CONSTRAINT = "(" +
            Sms.TYPE + " != " + Sms.MESSAGE_TYPE_DRAFT + ")";

    private static final String MMS_CONVERSATION_CONSTRAINT = "(" +
            Mms.MESSAGE_BOX + " != " + Mms.MESSAGE_BOX_DRAFTS + " AND (" +
            Mms.MESSAGE_TYPE + " = " + PduHeaders.MESSAGE_TYPE_SEND_REQ + " OR " +
            Mms.MESSAGE_TYPE + " = " + PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF + " OR " +
            Mms.MESSAGE_TYPE + " = " + PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND + "))";
    private static final String SELF_ITEM_KEY = "Self_Item_Key";
    private static final String AUTHORITY = "mms-sms";
    
    // gionee zhouyj 2012-11-05 add for CR00723896 start 
    private static final Uri PICK_PHONE_EMAIL_URI = Uri
        .parse("content://com.android.contacts/data/phone_email");
    private static final Uri PICK_PHONE_EMAIL_FILTER_URI = Uri.withAppendedPath(
            PICK_PHONE_EMAIL_URI, "filter");
    // gionee zhouyj 2012-11-05 add for CR00723896 end 

    static {
        URI_MATCHER.addURI(AUTHORITY, "conversations", URI_CONVERSATIONS);
        URI_MATCHER.addURI(AUTHORITY, "complete-conversations", URI_COMPLETE_CONVERSATIONS);

        // In these patterns, "#" is the thread ID.
        URI_MATCHER.addURI(
                AUTHORITY, "conversations/#", URI_CONVERSATIONS_MESSAGES);
        // Aurora xuyong 2014-08-14 added for aurora's new feature start
        URI_MATCHER.addURI(
                AUTHORITY, "conversations/#/quickquery", URI_QUICKQUERY_CONVERSATIONS_MESSAGES);
        // Aurora xuyong 2014-08-14 added for aurora's new feature end
        // Aurora xuyong 2014-10-23 added for privacy feature start
        //Aurora yudingmin 2014-11-05 added for aurora special feature start
        URI_MATCHER.addURI(
                AUTHORITY, "aurora_special_feature", URI_AURORA_SPECIAL_FEATURE);
        //Aurora yudingmin 2014-11-05 added for aurora special feature end
        // Aurora xuyong 2014-12-03 added for debug start
        URI_MATCHER.addURI(AUTHORITY, "conversations/#/updatethreads", URI_AURORA_UPDATETHREADS);
        // Aurora xuyong 2014-12-03 added for debug end
        URI_MATCHER.addURI(
                AUTHORITY, "privacy-account", URI_PRIVACY_ACCOUNT_ID_OP);
        URI_MATCHER.addURI(
                AUTHORITY, "privacy-account-change", URI_PRIVACY_ACCOUNT_CHANGE);
        URI_MATCHER.addURI(
                AUTHORITY, "privacy-account-address", URI_PRIVACY_ACCOUNT_ADDRESS);
        // Aurora xuyong 2014-10-23 added for privacy feature end
        URI_MATCHER.addURI(
                AUTHORITY, "conversations/#/recipients",
                URI_CONVERSATIONS_RECIPIENTS);

        URI_MATCHER.addURI(
                AUTHORITY, "conversations/#/subject",
                URI_CONVERSATIONS_SUBJECT);

        // URI for deleting obsolete threads.
        URI_MATCHER.addURI(AUTHORITY, "conversations/obsolete", URI_OBSOLETE_THREADS);

        URI_MATCHER.addURI(
                AUTHORITY, "messages/byphone/*",
                URI_MESSAGES_BY_PHONE);

        // In this pattern, two query parameter names are expected:
        // "subject" and "recipient."  Multiple "recipient" parameters
        // may be present.
        URI_MATCHER.addURI(AUTHORITY, "threadID", URI_THREAD_ID);
        // Aurora xuyong 2014-07-14 added for aurora's new feature start
        URI_MATCHER.addURI(AUTHORITY, "threadIDs", URI_THREAD_IDS);
        // Aurora xuyong 2014-07-14 added for aurora's new feature end
        // Use this pattern to query the canonical address by given ID.
        URI_MATCHER.addURI(AUTHORITY, "canonical-address/#", URI_CANONICAL_ADDRESS);

        // Use this pattern to query all canonical addresses.
        URI_MATCHER.addURI(AUTHORITY, "canonical-addresses", URI_CANONICAL_ADDRESSES);

        URI_MATCHER.addURI(AUTHORITY, "search", URI_SEARCH);
        URI_MATCHER.addURI(AUTHORITY, "searchSuggest", URI_SEARCH_SUGGEST);
        URI_MATCHER.addURI(AUTHORITY, "searchFolder", URI_SEARCH_FOLDER);
        // In this pattern, two query parameters may be supplied:
        // "protocol" and "message." For example:
        //   content://mms-sms/pending?
        //       -> Return all pending messages;
        //   content://mms-sms/pending?protocol=sms
        //       -> Only return pending SMs;
        //   content://mms-sms/pending?protocol=mms&message=1
        //       -> Return the the pending MM which ID equals '1'.
        //
        URI_MATCHER.addURI(AUTHORITY, "pending", URI_PENDING_MSG);

        // Use this pattern to get a list of undelivered messages.
        URI_MATCHER.addURI(AUTHORITY, "undelivered", URI_UNDELIVERED_MSG);

        // Use this pattern to see what delivery status reports (for
        // both MMS and SMS) have not been delivered to the user.
        URI_MATCHER.addURI(AUTHORITY, "notifications", URI_NOTIFICATIONS);

        URI_MATCHER.addURI(AUTHORITY, "draft", URI_DRAFT);

        URI_MATCHER.addURI(AUTHORITY, "locked", URI_FIRST_LOCKED_MESSAGE_ALL);

        URI_MATCHER.addURI(AUTHORITY, "locked/#", URI_FIRST_LOCKED_MESSAGE_BY_THREAD_ID);

        URI_MATCHER.addURI(AUTHORITY, "quicktext", URI_QUICK_TEXT);
        
        URI_MATCHER.addURI(AUTHORITY, "cellbroadcast", URI_CELLBROADCAST);
        
        URI_MATCHER.addURI(AUTHORITY, "conversations/status/#", URI_STATUS);

        URI_MATCHER.addURI(AUTHORITY, "messageIdToThread", URI_MESSAGE_ID_TO_THREAD);

        URI_MATCHER.addURI(AUTHORITY, "inbox", URI_MESSAGES_INBOX);
        
        URI_MATCHER.addURI(AUTHORITY, "outbox", URI_MESSAGES_OUTBOX);
        
        URI_MATCHER.addURI(AUTHORITY, "sentbox", URI_MESSAGES_SENTBOX);
        
        URI_MATCHER.addURI(AUTHORITY, "draftbox", URI_MESSAGES_DRAFTBOX);
        
        URI_MATCHER.addURI(AUTHORITY, "thread_id/#", URI_RECIPIENTS_NUMBER);

     //gionee gaoj 2012-3-27 added for CR00555790 start
        URI_MATCHER.addURI(AUTHORITY, "recent_contact", URI_RECENT_CONTACT);

         URI_MATCHER.addURI(AUTHORITY, "stared",URI_FIRST_STARED_MESSAGE_ALL);

         URI_MATCHER.addURI(AUTHORITY, "stared/#",URI_FIRST_STARED_MESSAGE_BY_THREAD_ID);

        URI_MATCHER.addURI(AUTHORITY,"encryption/*",URI_ENCRYPTION);
        URI_MATCHER.addURI(AUTHORITY,"smspsw/*",URI_SMS_PSW);
        // gionee zhouyj 2012-06-28 add for CR00628704 start 
        URI_MATCHER.addURI(AUTHORITY,"conversations/*",URI_CONVERSATIONSES);
        // gionee zhouyj 2012-06-28 add for CR00628704 end 
        // Aurora xuyong 2014-07-02 added for reject feature start
        URI_MATCHER.addURI(AUTHORITY,"conversations_reject/#", AURORA_REJECT_CONVERSATIONS);
        // Aurora xuyong 2014-08-05 modified for reject start
        URI_MATCHER.addURI(AUTHORITY,"conversations_resume_all/#", AURORA_CHANGE_ALL_MESSAGES);
        // Aurora xuyong 2014-08-05 modified for reject end
        // Aurora xuyong 2014-07-02 added for reject feature end
     //gionee gaoj 2012-3-27 added for CR00555790 end
        initializeColumnSets();
    }

    private SQLiteOpenHelper mOpenHelper;

    private boolean mUseStrictPhoneNumberComparation;
    // Aurora xuyong 2014-07-08 added for adapt laidiantong start
    private static final String[] ALL_THREADS_PROJECTION = {
        Threads._ID, 
        Threads.DATE, 
        Threads.MESSAGE_COUNT, 
        Threads.RECIPIENT_IDS,
        Threads.SNIPPET, 
        Threads.SNIPPET_CHARSET, 
        Threads.READ, 
        Threads.ERROR,
        Threads.HAS_ATTACHMENT, 
        Threads.TYPE, 
        Threads.READCOUNT, 
        Threads.STATUS,
        Threads.SIM_ID, 
        Threads.ENCRYPTION
    };
    
    private static final String[] PROJECTION = new String[] {
        // TODO: should move this symbol into com.android.mms.telephony.Telephony.
        MmsSms.TYPE_DISCRIMINATOR_COLUMN,
        BaseColumns._ID,
        Conversations.THREAD_ID,
        // For SMS
        Sms.ADDRESS,
        Sms.BODY,
        Sms.DATE,
        Sms.READ,
        Sms.TYPE,
        Sms.STATUS,
        Sms.ERROR_CODE,
        // For MMS
        Mms.SUBJECT,
        Mms.SUBJECT_CHARSET,
        Mms.DATE,
        Mms.READ,
        Mms.MESSAGE_TYPE,
        Mms.MESSAGE_BOX,
        Mms.DELIVERY_REPORT,
        Mms.READ_REPORT,
        PendingMessages.ERROR_TYPE,
        //a1
    };
    // Aurora xuyong 2014-07-08 added for adapt laidiantong end
    @Override
    public boolean onCreate() {
        mOpenHelper = MmsSmsDatabaseHelper.getInstance(getContext());
        mUseStrictPhoneNumberComparation =
            getContext().getResources().getBoolean(
                    com.android.internal.R.bool.config_use_strict_phone_number_comparation);
        return true;
    }
    // Aurora xuyong 2014-07-18 added for bug #5921 start
    private boolean mNeedRebuildCursor = false;
    // Aurora xuyong 2014-07-18 added for bug #5921 end
    @Override
    public Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
       // Aurora xuyong 2014-09-11 added for bug #8175 start
        final String fSelection = selection;
       // Aurora xuyong 2014-09-11 added for bug #8175 end
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = null;
        // Aurora xuyong 2014-07-18 added for bug #5921 start
        if (selection != null && selection.contains("sub_id")) {
            mNeedRebuildCursor = true;
            selection = selection.replaceAll("sub_id", "sim_id");
            }
        if (projection != null && projection.length > 0) {
            int itemNum = projection.length;
            for (int i = 0; i < itemNum; i++) {
                if (projection[i].equals("sub_id")) {
                    projection[i] = "sim_id";
                }
            }
         }
        // Aurora xuyong 2014-07-18 added for bug #5921 end
 //       Xlog.d(LOG_TAG, "query uri = " + uri);
        switch(URI_MATCHER.match(uri)) {
            // Aurora xuyong 2014-12-03 added for debug start
            case URI_AURORA_UPDATETHREADS:
                String updateReadThreadId = uri.getPathSegments().get(1);
                Cursor updateResult = null;
                try {
                    updateResult = db.rawQuery("SELECT read FROM threads WHERE _id = ?", new String[]{ updateReadThreadId });
                    if (updateResult != null && updateResult.getCount() > 0 && updateResult.moveToFirst()) {
                        boolean needUpdate = (updateResult.getInt(0) == 0);
                        if (needUpdate) {
                            MmsSmsDatabaseHelper.updateThread(db, Long.parseLong(updateReadThreadId));
                        }
                    }
                } catch (SQLiteException e) {
                    e.printStackTrace();
                } finally {
                    if (updateResult != null && !updateResult.isClosed()) {
                        updateResult.close();
                    }
                }
            // Aurora xuyong 2014-12-03 added for debug end
            // Aurora xuyong 2014-10-23 added for privacy feature start
            case URI_PRIVACY_ACCOUNT_CHANGE:
                 return db.query("threads", projection, selection, selectionArgs, null, null, null);
            case URI_PRIVACY_ACCOUNT_ADDRESS:
                return db.query("canonical_addresses", projection, selection, selectionArgs, null, null, null);
            case URI_PRIVACY_ACCOUNT_ID_OP:
                return db.query("privacy_table", projection, selection, selectionArgs, null, null, null);
           // Aurora xuyong 2014-10-23 added for privacy feature end
           // Aurora xuyong 2014-08-14 added for aurora's new feature start
            case URI_QUICKQUERY_CONVERSATIONS_MESSAGES:
                if (MmsSmsDatabaseHelper.sHasRejectFeature) {
                    if (selection == null || selection.length() == 0) {
                        selection = " reject = 0 ";
                    } else if (!selection.contains("reject")) {
                        selection = "reject = 0 AND " + selection;
                    }
                }
             // Aurora xuyong 2014-10-23 added for privacy feature start
                if (MmsSmsDatabaseHelper.sHasPrivacyFeature) {
                     if (selection == null || selection.length() == 0) {
                        selection = " is_privacy = 0 ";
                    } else if (!selection.contains("is_privacy")) {
                        selection = "is_privacy = 0 AND " + selection;
                    }
                }
              // Aurora xuyong 2014-10-23 added for privacy feature end
                cursor = getConversationMessages(uri.getPathSegments().get(1), projection,
                       selection, "normalized_date DESC LIMIT 20");
                int resultCount = cursor.getCount();
                int columnCount = cursor.getColumnCount();
                MatrixCursor rebuildCursor = new MatrixCursor(projection, resultCount);
                if (cursor.moveToLast()) {
                    Object[] itemDetaiLast = new Object[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        try {
                            itemDetaiLast[i] = cursor.getString(i);
                        } catch (CursorIndexOutOfBoundsException e) {
                            Log.e("CIOEC", "current position = " + i + ", cursor.getColumnCount() = " + cursor.getColumnCount());
                        }
                    }
                    rebuildCursor.addRow(itemDetaiLast);
                    while (cursor.moveToPrevious()) {
                        Object[] itemDetail = new Object[columnCount];
                        for (int i = 0; i < columnCount; i++) {
                            try {
                                itemDetail[i] = cursor.getString(i);
                            } catch (CursorIndexOutOfBoundsException e) {
                                Log.e("CIOEC", "current position = " + i + ", cursor.getColumnCount() = " + cursor.getColumnCount());
                            }
                        }
                        rebuildCursor.addRow(itemDetail);
                    }
                  //Aurora xuyong 2014-09-02 added for whitelist feature start
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                  //Aurora xuyong 2014-09-02 added for whitelist feature end
                    cursor = rebuildCursor;
                }
                break;
            // Aurora xuyong 2014-08-14 added for aurora's new feature end
            case URI_COMPLETE_CONVERSATIONS:
                cursor = getCompleteConversations(projection, selection, sortOrder);
                break;
            case URI_CONVERSATIONS:
                String simple = uri.getQueryParameter("simple");
                    // Aurora liugj 2014-01-06 modified for bath-delete optimize start
                // Aurora xuyong 2014-07-08 added for adapt laidiantong start
                if (!MmsSmsDatabaseHelper.sHasRejectFeature) {
                    if (selection == null) {
                        selection = " deleted = 0";
                    } else if (selection.equals("deleted = 1")) {
                        
                    } else {
                        selection += " AND deleted = 0";
                    }
                } else {
                     if (selection == null || selection.length() == 0) {
                         selection = "NOT ((snippet is null) AND (snippet_cs not null AND snippet_cs = 0) AND (has_attachment = 0) AND (message_count = 0) AND (type = 0))";
                         selection = "(" + selection + ") AND (NOT (message_count = 0 AND (reject = 1 OR date IS NULL)))";    
                     }
                }
                // Aurora xuyong 2014-10-23 added for privacy feature start
                if (MmsSmsDatabaseHelper.sHasPrivacyFeature) {
                     if (selection == null || selection.length() == 0) {
                        selection = " is_privacy = 0 ";
                    } else if (!selection.contains("is_privacy")) {
                        selection = "is_privacy = 0 AND " + selection;
                    }
                }
                // Aurora xuyong 2014-10-23 added for privacy feature end
                // Aurora xuyong 2014-07-08 added for adapt laidiantong end
                // Aurora liugj 2014-01-06 modified for bath-delete optimize end
                if ((simple != null) && simple.equals("true")) {
                    String threadType = uri.getQueryParameter("thread_type");
                    if (!TextUtils.isEmpty(threadType)) {
                        selection = concatSelections(
                                selection, Threads.TYPE + "=" + threadType);
                    }
                    // Aurora xuyong 2014-07-08 added for adapt laidiantong start
                    if (MmsSmsDatabaseHelper.sHasRejectFeature) {
                        // Aurora xuyong 2015-12-25 modified for bug #9197 start
                        if (projection != null) {
                            int count = projection.length;
                            // Aurora xuyong 2014-09-11 added for bug #8175 start
                            if ((fSelection == null || fSelection.length() <= 0) && count == 1) {
                            // Aurora xuyong 2014-09-11 added for bug #8175 end
                                projection = ALL_THREADS_PROJECTION;
                            }
                        // Aurora xuyong 2015-12-25 modified for bug #9197 end
                        }
                    }
                    // Aurora xuyong 2014-07-08 added for adapt laidiantong end
                    cursor = getSimpleConversations(
                            projection, selection, selectionArgs, sortOrder);
                    
                } else {
                    cursor = getConversations(
                            projection, selection, sortOrder);
                }
                break;
            case URI_CONVERSATIONS_MESSAGES:
                // Aurora xuyong 2014-07-02 added for reject feature start
                 // Aurora xuyong 2014-07-03 added for reject feature start
                    if (MmsSmsDatabaseHelper.sHasRejectFeature) {
                        if (selection == null || selection.length() == 0) {
                            selection = " reject = 0 ";
                        } else if (!selection.contains("reject")) {
                       // Aurora xuyong 2014-08-07 modified for bug #7069 start
                            selection = "reject = 0 AND " + selection;
                       // Aurora xuyong 2014-08-07 modified for bug #7069 end
                        }
                    }
                // Aurora xuyong 2014-10-23 added for privacy feature start
                    if (MmsSmsDatabaseHelper.sHasPrivacyFeature) {
                         if (selection == null || selection.length() == 0) {
                            selection = " is_privacy = 0 ";
                        } else if (!selection.contains("is_privacy")) {
                            selection = "is_privacy = 0 AND " + selection;
                        }
                    }
                // Aurora xuyong 2014-10-23 added for privacy feature end
                 // Aurora xuyong 2014-07-03 added for reject feature end
                // Aurora xuyong 2014-07-02 added for reject feature end
                        // Aurora xuyong 2014-07-08 added for adapt laidiantong start
                     if (MmsSmsDatabaseHelper.sHasRejectFeature) {
                        int count = projection.length;
                        if (count < PROJECTION.length) {
                            projection = PROJECTION;
                        }
                     }
                    // Aurora xuyong 2014-07-08 added for adapt laidiantong end
                cursor = getConversationMessages(uri.getPathSegments().get(1), projection,
                        selection, sortOrder);
                // Aurora xuyong 2014-10-25 added for privacy feature start
                if (sortOrder != null && sortOrder.contains(" normalized_date DESC LIMIT ")) {
                    int resultCountP = cursor.getCount();
                    int columnCountP = cursor.getColumnCount();
                    MatrixCursor rebuildCursorP = new MatrixCursor(projection, resultCountP);
                    if (cursor.moveToLast()) {
                        Object[] itemDetaiLast = new Object[columnCountP];
                        for (int i = 0; i < columnCountP; i++) {
                            try {
                                itemDetaiLast[i] = cursor.getString(i);
                            } catch (CursorIndexOutOfBoundsException e) {
                                Log.e("CIOEC", "current position = " + i + ", cursor.getColumnCount() = " + cursor.getColumnCount());
                            }
                        }
                        rebuildCursorP.addRow(itemDetaiLast);
                        while (cursor.moveToPrevious()) {
                            Object[] itemDetail = new Object[columnCountP];
                            for (int i = 0; i < columnCountP; i++) {
                                try {
                                    itemDetail[i] = cursor.getString(i);
                                } catch (CursorIndexOutOfBoundsException e) {
                                    Log.e("CIOEC", "current position = " + i + ", cursor.getColumnCount() = " + cursor.getColumnCount());
                                }
                            }
                            rebuildCursorP.addRow(itemDetail);
                        }
                        if (cursor != null && !cursor.isClosed()) {
                            cursor.close();
                        }
                        cursor = rebuildCursorP;
                    }
                }
                // Aurora xuyong 2014-10-25 added for privacy feature end
                break;
            case URI_CONVERSATIONS_RECIPIENTS:
                cursor = getConversationById(
                        uri.getPathSegments().get(1), projection, selection,
                        selectionArgs, sortOrder);
                break;
            case URI_CONVERSATIONS_SUBJECT:
                cursor = getConversationById(
                        uri.getPathSegments().get(1), projection, selection,
                        selectionArgs, sortOrder);
                break;
            case URI_MESSAGES_BY_PHONE:
                cursor = getMessagesByPhoneNumber(
                        uri.getPathSegments().get(2), projection, selection, sortOrder);
                break;
            case URI_THREAD_ID:
                List<String> recipients = uri.getQueryParameters("recipient");
                // Aurora xuyong 2014-10-23 added for privacy feature start
                List<String> privacyId = uri.getQueryParameters("is_privacy");
                if (privacyId == null || privacyId.size() <= 0) {
                    privacyId = new ArrayList<String>();
                    privacyId.add(new String("0"));
                }
                // Aurora xuyong 2014-10-23 added for privacy feature end
                //if WAP Push is supported, SMS and WAP Push from same sender will be put in diferent threads
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                    if(!uri.getQueryParameters("wappush").isEmpty()){
                        // Aurora xuyong 2014-07-14 modified for aurora's new feature start
                        // Aurora xuyong 2014-10-23 added for privacy feature start
                        cursor = getWapPushThreadId(recipients, true, Long.parseLong(privacyId.get(0)));
                        // Aurora xuyong 2014-10-23 added for privacy feature end
                        // Aurora xuyong 2014-07-14 modified for aurora's new feature end
                    } else {
                        // Aurora xuyong 2014-07-14 modified for aurora's new feature start
                        // Aurora xuyong 2014-10-23 modified for privacy feature start
                        cursor = getThreadId(recipients, true, Long.parseLong(privacyId.get(0)));
                        // Aurora xuyong 2014-10-23 modified for privacy feature end
                        // Aurora xuyong 2014-07-14 modified for aurora's new feature end
                    }
                    break;
                }
                // Aurora xuyong 2014-07-14 modified for aurora's new feature start
                // Aurora xuyong 2014-10-23 modified for privacy feature start
                cursor = getThreadId(recipients, true, Long.parseLong(privacyId.get(0)));
                // Aurora xuyong 2014-10-23 modified for privacy feature end
                // Aurora xuyong 2014-07-14 modified for aurora's new feature end
                break;
            // Aurora xuyong 2014-07-14 added for aurora's new feature start
            case URI_THREAD_IDS:
                List<String> recipientsCheck = uri.getQueryParameters("recipient");
             // Aurora xuyong 2014-10-23 added for privacy feature start
                List<String> privacyIds = uri.getQueryParameters("is_privacy");
                if (privacyIds == null || privacyIds.size() <= 0) {
                    privacyIds = new ArrayList<String>();
                    privacyIds.add(new String("0"));
             }
             // Aurora xuyong 2014-10-23 added for privacy feature end
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                    if(!uri.getQueryParameters("wappush").isEmpty()){
                        // Aurora xuyong 2014-10-23 modified for privacy feature start
                        cursor = getWapPushThreadId(recipientsCheck, false, Long.parseLong(privacyIds.get(0)));
                        // Aurora xuyong 2014-10-23 modified for privacy feature end
                    } else {
                        // Aurora xuyong 2014-10-23 modified for privacy feature start
                        cursor = getThreadId(recipientsCheck, false, Long.parseLong(privacyIds.get(0)));
                        // Aurora xuyong 2014-10-23 modified for privacy feature end
                    }
                    break;
                }
                // Aurora xuyong 2014-10-23 modified for privacy feature start
                cursor = getThreadId(recipientsCheck, false, Long.parseLong(privacyIds.get(0)));
                // Aurora xuyong 2014-10-23 modified for privacy feature end
                break;
            // Aurora xuyong 2014-07-14 added for aurora's new feature end
            case URI_CANONICAL_ADDRESS: {
                String extraSelection = "_id=" + uri.getPathSegments().get(1);
                String finalSelection = TextUtils.isEmpty(selection)
                        ? extraSelection : extraSelection + " AND " + selection;
                cursor = db.query(TABLE_CANONICAL_ADDRESSES,
                        CANONICAL_ADDRESSES_COLUMNS_1,
                        finalSelection,
                        selectionArgs,
                        null, null,
                        sortOrder);
                break;
            }
            case URI_CANONICAL_ADDRESSES:
                cursor = db.query(TABLE_CANONICAL_ADDRESSES,
                        CANONICAL_ADDRESSES_COLUMNS_2,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder);
                break;
            case URI_SEARCH_SUGGEST: {
                // gionee zhouyj 2012-11-05 modify for CR00723896 start 
                if (       sortOrder != null
                        || selection != null
                        || selectionArgs != null
                        || projection != null) {
                    throw new IllegalArgumentException(
                            "do not specify sortOrder, selection, selectionArgs, or projection" +
                            "with this query");
                }
                String searchString = uri.getQueryParameter("pattern");
                String pattern = "%" + searchString + "%";
             //   Xlog.d(LOG_TAG, "search suggest pattern is: " + searchString);
                if (searchString.trim().equals("") || searchString == null) {
                    cursor = null;
                } else {
                    HashMap<String,String> contactRes = getContactsByNumber(searchString);
                    String searchContacts = searchContacts(searchString, contactRes);
                    String smsIdQuery = null;
                    smsIdQuery = String.format("SELECT _id FROM sms WHERE thread_id " + searchContacts);
                    String smsIn = queryIdAndFormatIn(db, smsIdQuery);
                    String mmsIdQuery = String.format("SELECT part._id FROM part JOIN pdu " +
                            " ON part.mid=pdu._id " +
                            " WHERE part.ct='text/plain' AND pdu.thread_id " + searchContacts);
                    String mmsIn = queryIdAndFormatIn(db, mmsIdQuery);
                    String wpIdQuery = String.format("SELECT _id FROM wappush where thread_id " + searchContacts);
                    String wpIn = queryIdAndFormatIn(db,wpIdQuery);
                    String query = null;
                    if(mEncryption) {
                        query = String.format("SELECT DISTINCT index_text AS snippet " +
                                " FROM (SELECT * FROM (SELECT * FROM sms join threads WHERE ( sms.thread_id=threads._id AND threads.encryption=0))) as sms join words on sms._id=words.source_id  WHERE (index_text LIKE '%s') " +
                                " OR (source_id " + smsIn + " AND table_to_use=1) " +
                                " OR (source_id " + mmsIn + " AND table_to_use=2) " +
                                            " OR (source_id " + wpIn + " AND table_to_use=3) " +
                                " ORDER BY snippet LIMIT 50", pattern);
                    } else {
                        query = String.format("SELECT DISTINCT index_text AS snippet " +
                                " FROM words WHERE (index_text LIKE '%s') " +
                                " OR (source_id " + smsIn + " AND table_to_use=1) " +
                                " OR (source_id " + mmsIn + " AND table_to_use=2) " +
                                            " OR (source_id " + wpIn + " AND table_to_use=3) " +
                                " ORDER BY snippet LIMIT 50", pattern);
                    }
                    Log.i("zhouyj", "query = " + query);
                    cursor = db.rawQuery(query, null);
                 //   Xlog.d(LOG_TAG, "search suggestion cursor count is : " + cursor.getCount());
                }
                // gionee zhouyj 2012-11-05 modify for CR00723896 start 
                break;
            }
            case URI_MESSAGE_ID_TO_THREAD: {
                // Given a message ID and an indicator for SMS vs. MMS return
                // the thread id of the corresponding thread.
                try {
                    long id = Long.parseLong(uri.getQueryParameter("row_id"));
                    switch (Integer.parseInt(uri.getQueryParameter("table_to_use"))) {
                        case 1:  // sms
                            cursor = db.query(
                                "sms",
                                new String[] { "thread_id" },
                                "_id=?",
                                new String[] { String.valueOf(id) },
                                null,
                                null,
                                null);
                            break;
                        case 2:  // mms
                            String mmsQuery =
                                "SELECT thread_id FROM pdu,part WHERE ((part.mid=pdu._id) AND " +
                                "(part._id=?))";
                            cursor = db.rawQuery(mmsQuery, new String[] { String.valueOf(id) });
                            break;
                    }
                } catch (NumberFormatException ex) {
                    // ignore... return empty cursor
                }
                break;
            }
            case URI_SEARCH: {
                if (       sortOrder != null
                        || selection != null
                        || selectionArgs != null
                        || projection != null) {
                    throw new IllegalArgumentException(
                            "do not specify sortOrder, selection, selectionArgs, or projection" +
                            "with this query");
                }
                // Aurora xuyong 2014-02-13 added for bug #11290 start
                // When the words table is empty, we couldn't get any result from the
                // multi—table inquires such as 'SELECT * FROM pdu,addr,part,words',
                // so we add a special record here.
                Cursor wordsCursor = null;
                int wordsItemCount = 0;
                try {
                    // Aurora xuyong 2015-07-03 modified for aurora's new feature start
                    wordsCursor = db.rawQuery("SELECT * FROM words WHERE table_to_use = 2;", null);
                    // Aurora xuyong 2015-07-03 modified for aurora's new feature end
                    if (wordsCursor != null) {
                        wordsItemCount = wordsCursor.getCount();
                        if (wordsItemCount <= 0) {
                            db.execSQL("INSERT INTO words(table_to_use) VALUES(2);");
                        }
                    }
                } finally {
                    if (wordsCursor != null && !wordsCursor.isClosed()) {
                        wordsCursor.close();
                    }
                }
                // Aurora xuyong 2014-02-13 added for bug #11290 end
                // This code queries the sms and mms tables and returns a unified result set
                // of text matches.  We query the sms table which is pretty simple.  We also
                // query the pdu, part and addr table to get the mms result.  Note that we're
                // using a UNION so we have to have the same number of result columns from
                // both queries.

                String pattern = uri.getQueryParameter("pattern");
                String searchContacts = searchContacts(pattern);
                String searchString = "%" + pattern + "%";
                // Aurora xuyong 2014-02-13 modified for bug #11290 start
                String smsProjection = "sms._id as _id,thread_id,address,body,date,null as sub,null as sub_cs,1 as auroramsgtype," +
                        "index_text,words._id";
                String mmsProjection = "pdu._id,thread_id,addr.address,part.text as " + "" +
                                "body,pdu.date,pdu.sub,pdu.sub_cs,2 as auroramsgtype,index_text,words._id";
                // Aurora xuyong 2014-02-13 modified for bug #11290 end
                // search on the words table but return the rows from the corresponding sms table
                //gionee gaoj 2012-3-27 added for CR00555790 start
                String smsQuery = null;
                // Aurora xuyong 2015-08-18 added for bug #15803 start
                String privacySelectionArgs = "0, " + AuroraPrivacyUtils.getCurrentAccountId();
                // Aurora xuyong 2015-08-18 added for bug #15803 end
                if (mEncryption) {
                    smsQuery = String.format("SELECT %s  FROM  "+
                            " (SELECT * FROM (SELECT * FROM sms join threads WHERE ( sms.thread_id=threads._id AND threads.encryption=0))) as sms join words "+
                            " WHERE ((sms.body LIKE ? OR thread_id %s) AND sms._id=words.source_id AND words.table_to_use=1)",smsProjection,searchContacts);
                } else {
                 smsQuery = String.format(
                        // Aurora xuyong 2015-08-18 modified for bug #15803 start
                        "SELECT %s FROM sms,words WHERE sms.reject == 0 AND sms.is_privacy IN (%s) AND ((sms.body LIKE ? OR thread_id %s)" +
                        // Aurora xuyong 2015-08-18 modified for bug #15803 end
                        " AND sms._id=words.source_id AND words.table_to_use=1) ",
                        smsProjection,
                        // Aurora xuyong 2015-08-18 added for bug #15803 start
                        privacySelectionArgs,
                        // Aurora xuyong 2015-08-18 added for bug #15803 end
                        searchContacts);
                }
                //gionee gaoj 2012-3-27 added for CR00555790 end

                // search on the words table but return the rows from the corresponding parts table

                //gionee gaoj 2012-3-27 added for CR00555790 start
                String mmsQuery = null;
                if (mEncryption) {
                    mmsQuery = String.format(
                            "SELECT %s FROM " +
                            "(SELECT * FROM (SELECT * FROM pdu JOIN threads WHERE (pdu.thread_id = threads._id AND threads.encryption = 0))) as "+
                            "pdu,part,addr,words WHERE ((part.mid=pdu._id) AND " +
                            "(addr.msg_id=pdu._id) AND " +
                            "(((addr.type=%d) AND (pdu.msg_box == %d)) OR " +
                            "((addr.type=%d) AND (pdu.msg_box != %d))) AND " +
                            "(part.ct='text/plain') AND " +
                            "(part.text LIKE ? OR thread_id %s) AND " +
                            "(part._id = words.source_id) AND " +
                            "(words.table_to_use=2))",
                            mmsProjection,
                            PduHeaders.FROM,
                            Mms.MESSAGE_BOX_INBOX,
                            PduHeaders.TO,
                            Mms.MESSAGE_BOX_INBOX,
                            searchContacts);
                } else {
                 mmsQuery = String.format(
                        // Aurora xuyong 2015-08-18 modified for bug #15803 start
                        "SELECT %s FROM pdu,part,addr,words WHERE pdu.reject == 0 AND pdu.is_privacy IN (%s) AND ((part.mid=pdu._id) AND " +
                        // Aurora xuyong 2015-08-18 modified for bug #15803 end
                        "(addr.msg_id=pdu._id) AND " +
                        "(((addr.type=%d) AND (pdu.msg_box == %d)) OR " +
                        "((addr.type=%d) AND (pdu.msg_box != %d))) AND " +
                        // Aurora xuyong 2014-02-13 deleted for bug #11290 start
                        // Aurora xuyong 2015-08-06 modified for bug #15045 start
                        "((part.ct='text/plain' AND part.text LIKE ? AND (part._id = words.source_id)) OR thread_id %s) AND " +
                        // Aurora xuyong 2015-08-06 modified for bug #15045 end
                        // Aurora xuyong 2014-02-13 deleted for bug #11290 end
                        // Aurora xuyong 2015-08-06 modified for bug #15045 start
                        //"(part.text LIKE ? OR thread_id %s) AND " +
                        // Aurora xuyong 2015-08-06 modified for bug #15045 end
                        // Aurora xuyong 2014-02-13 deleted for bug #11290 start
                        //"(part._id = words.source_id) AND " +
                        // Aurora xuyong 2014-02-13 deleted for bug #11290 end
                        "(words.table_to_use=2))",
                        mmsProjection,
                        // Aurora xuyong 2015-08-18 added for bug #15803 start
                        privacySelectionArgs,
                        // Aurora xuyong 2015-08-18 added for bug #15803 end
                        PduHeaders.FROM,
                        Mms.MESSAGE_BOX_INBOX,
                        PduHeaders.TO,
                        Mms.MESSAGE_BOX_INBOX,
                        searchContacts);
                }
                //gionee gaoj 2012-3-27 added for CR00555790 end

                /*
                 * search wap push
                 * table words is not used
                 * field index_text and _id are just used for union operation.
                 */
                // Aurora xuyong 2014-02-13 modified for bug #11290 start
                String wappushProjection = "wappush._id as _id,thread_id,address, coalesce(text||' '||url,text,url) as body,date,null as sub,null as sub_cs,1 as auroramsgtype," +
                // Aurora xuyong 2014-02-13 modified for bug #11290 end
                "0 as index_text,0 as _id";
                //gionee gaoj 2012-3-27 added for CR00555790 start
                String wappushQuery = null;
                if (mEncryption) {
                    wappushQuery = String.format(
                            "SELECT %s FROM "
                            + " (SELECT * FROM (SELECT * FROM wappush JOIN threads WHERE (wappush.thread_id = threads._id AND threads.encryption = 0))) as"
                            + " wappush WHERE (body LIKE ?  OR thread_id %s)",
                            wappushProjection,
                            searchContacts);
                } else {
                wappushQuery = String.format(
                        "SELECT %s FROM wappush WHERE (body LIKE ? OR thread_id %s)",
                        wappushProjection,
                        searchContacts);
                }
                //gionee gaoj 2012-3-27 added for CR00555790 end
                
                // join the results from sms and part (mms)
                //FeatureOption.MTK_WAPPUSH_SUPPORT
                String rawQuery = null;
                if(!FeatureOption.MTK_WAPPUSH_SUPPORT){
                    rawQuery = String.format(
                            "SELECT * FROM (%s UNION %s) GROUP BY %s ORDER BY %s",
                            smsQuery,
                            mmsQuery,
                            "thread_id",
                            "thread_id ASC, date DESC");
                }else{
                    rawQuery = String.format(
                            "SELECT * FROM (%s UNION %s UNION %s) GROUP BY %s ORDER BY %s ",
                            smsQuery,
                            wappushQuery,
                            mmsQuery,
                            "thread_id",
                            // Aurora xuyong 2015-08-06 modified for bug #15045 start
                            "date DESC");
                            // Aurora xuyong 2015-08-06 modified for bug #15045 end
                }

                try {
                    if (!FeatureOption.MTK_WAPPUSH_SUPPORT) {
                        cursor = db.rawQuery(rawQuery, new String[] {searchString, searchString});
                    } else {
                        cursor = db.rawQuery(rawQuery, new String[] {searchString, searchString, searchString});
                    }
                } catch (Exception ex) {
                    Log.e(LOG_TAG, "got exception: " + ex.toString());
                    return null;                    
                }
                break;
            }
            case URI_SEARCH_FOLDER: {
                if (       sortOrder != null
                        || selection != null
                        || selectionArgs != null
                        || projection != null) {
                    throw new IllegalArgumentException(
                            "do not specify sortOrder, selection, selectionArgs, or projection" +
                            "with this query");
                }

                // This code queries the sms and mms tables and returns a unified result set
                // of text matches.  We query the sms table which is pretty simple.  We also
                // query the pdu, part and addr table to get the mms result.  Note that we're
                // using a UNION so we have to have the same number of result columns from
                // both queries.

                String pattern = uri.getQueryParameter("pattern");
                String searchContacts = searchContacts(pattern);
                String searchString = "%" + pattern + "%";
                String smsProjection = "sms._id as _id,thread_id,address,body,date," +
                "index_text,words._id,1 as msg_type";
                String mmsProjection = "pdu._id,thread_id,addr.address,part.text as " + "" +
                        "body,pdu.date,index_text,words._id,2 as msg_type";

                // search on the words table but return the rows from the corresponding sms table
                String smsQuery = String.format(
                        "SELECT %s FROM sms,words WHERE ((sms.body LIKE ? OR thread_id %s)" +
                        " AND sms._id=words.source_id AND words.table_to_use=1) ",
                        smsProjection,
                        searchContacts);

                // search on the words table but return the rows from the corresponding parts table
                String mmsQuery = String.format(
                        "SELECT %s FROM pdu,part,addr,words WHERE ((part.mid=pdu._id) AND " +
                        "(addr.msg_id=pdu._id) AND " +
                        "(((addr.type=%d) AND (pdu.msg_box == %d)) OR " +
                        "((addr.type=%d) AND (pdu.msg_box != %d))) AND " +
                        "(part.ct='text/plain') AND " +
                        "(part.text LIKE ? OR thread_id %s) AND " +
                        "(part._id = words.source_id) AND " +
                        "(words.table_to_use=2))",
                        mmsProjection,
                        PduHeaders.FROM,
                        Mms.MESSAGE_BOX_INBOX,
                        PduHeaders.TO,
                        Mms.MESSAGE_BOX_INBOX,
                        searchContacts);
                /*
                 * search wap push
                 * table words is not used
                 * field index_text and _id are just used for union operation.
                 */
                String wappushProjection = "wappush._id as _id,thread_id,address, coalesce(text||' '||url,text,url) as body,date," +
                "0 as index_text,0 as _id,3 as msg_type";
                String wappushQuery = String.format(
                        "SELECT %s FROM wappush WHERE (body LIKE ? OR thread_id %s)",
                        wappushProjection,
                        searchContacts);
                
                // join the results from sms and part (mms)
                //FeatureOption.MTK_WAPPUSH_SUPPORT
                String rawQuery = null;
                if(!FeatureOption.MTK_WAPPUSH_SUPPORT){
                    rawQuery = String.format(
                            "SELECT * FROM (%s UNION %s) ORDER BY %s",
                            smsQuery,
                            mmsQuery,
                            "date DESC");
                }else{
                    rawQuery = String.format(
                            "SELECT * FROM (%s UNION %s UNION %s) ORDER BY %s ",
                            smsQuery,
                            wappushQuery,
                            mmsQuery,
                            "date DESC");
                } 

                try {
                    if(!FeatureOption.MTK_WAPPUSH_SUPPORT){
                        cursor = db.rawQuery(rawQuery, new String[] {searchString, searchString});
                    }else{
                        cursor = db.rawQuery(rawQuery, new String[] {searchString, searchString, searchString});
                    }
                } catch (Exception ex) {
                    Log.e(LOG_TAG, "got exception: " + ex.toString());
                    return null;                    
                }
                break;
            }
            case URI_PENDING_MSG: {
                String protoName = uri.getQueryParameter("protocol");
                String msgId = uri.getQueryParameter("message");
                int proto = TextUtils.isEmpty(protoName) ? -1
                        : (protoName.equals("sms") ? MmsSms.SMS_PROTO : MmsSms.MMS_PROTO);

                String extraSelection = (proto != -1) ?
                        (PendingMessages.PROTO_TYPE + "=" + proto) : " 0=0 ";
                if (!TextUtils.isEmpty(msgId)) {
                    extraSelection += " AND " + PendingMessages.MSG_ID + "=" + msgId;
                }

                String finalSelection = TextUtils.isEmpty(selection)
                        ? extraSelection : ("(" + extraSelection + ") AND " + selection);
                String finalOrder = TextUtils.isEmpty(sortOrder)
                        ? PendingMessages.DUE_TIME : sortOrder;
                cursor = db.query(TABLE_PENDING_MSG, null,
                        finalSelection, selectionArgs, null, null, finalOrder);
                break;
            }
            case URI_UNDELIVERED_MSG: {
                cursor = getUndeliveredMessages(projection, selection,
                        selectionArgs, sortOrder);
                break;
            }
            case URI_DRAFT: {
                cursor = getDraftThread(projection, selection, sortOrder);
                break;
            }
            case URI_FIRST_LOCKED_MESSAGE_BY_THREAD_ID: {
                long threadId;
                try {
                    threadId = Long.parseLong(uri.getLastPathSegment());
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "Thread ID must be a long.");
                    break;
                }
                cursor = getFirstLockedMessage(projection, "thread_id=" + Long.toString(threadId),
                        sortOrder);
                break;
            }
            case URI_FIRST_LOCKED_MESSAGE_ALL: {
                cursor = getFirstLockedMessage(projection, selection, sortOrder);
                break;
            }
            case URI_QUICK_TEXT: {
                cursor = db.query(TABLE_QUICK_TEXT, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case URI_STATUS:{
                long threadId;
                try {
                    threadId = Long.parseLong(uri.getLastPathSegment());
               //     Xlog.d(LOG_TAG, "query URI_STATUS Thread ID is " + threadId);
                } catch (NumberFormatException e) {
              //      Xlog.e(LOG_TAG, "Thread ID must be a long.");
                    break;
                }
                cursor = db.query(TABLE_THREADS, STATUS_PROJECTION,
                        "_id=" + Long.toString(threadId), null, null, null, sortOrder);
            //    Xlog.d(LOG_TAG, "query URI_STATUS ok");
                break;
            }
            case URI_MESSAGES_INBOX: {
                cursor = getInboxMessage(db);
                break;
            }
            case URI_MESSAGES_OUTBOX: {
                cursor = getOutboxMessage(db);
                break;
            }
            case URI_MESSAGES_SENTBOX: {
                cursor = getSentboxMessage(db);
                break;
            }
            case URI_MESSAGES_DRAFTBOX: {
                cursor = getDraftboxMessage(db);
                break;
            }
            
            case URI_RECIPIENTS_NUMBER: {
                cursor = getRecipientsNumber(uri.getPathSegments().get(1));
                break;
            }
             //gionee gaoj 2012-3-27 added for CR00555790 start
            case URI_RECENT_CONTACT:
                cursor = db.query(TABLE_RECENT_CONTACT_ADDRESSES,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder); 
                break;  
            case URI_FIRST_STARED_MESSAGE_BY_THREAD_ID: {
                long threadId;
                try {
                    threadId = Long.parseLong(uri.getLastPathSegment());
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "Thread ID must be a long.");
                    break;
                }
                cursor = getFirstStarMessage(projection,
                        "thread_id=" + Long.toString(threadId), null, sortOrder);
                break;
            }
            case URI_FIRST_STARED_MESSAGE_ALL: {
                cursor = getFirstStarMessage(projection, selection, selectionArgs,
                        sortOrder);
                break;
            }
            case URI_SMS_PSW: {
                String psw = getContext().getSharedPreferences("MsgEncryption", 0).getString("password", null);
                MatrixCursor c = new MatrixCursor(new String[]{"psw"});
                c.addRow(new String[]{psw});
                return c;
            }
             //gionee gaoj 2012-3-27 added for CR00555790 end

            default:
                throw new IllegalStateException("Unrecognized URI:" + uri);
        }
     //   Xlog.d(LOG_TAG, "query end");
        cursor.setNotificationUri(getContext().getContentResolver(), MmsSms.CONTENT_URI);
        // Aurora xuyong 2014-07-18 modified for bug #5921 start
        return rebuildCursor(cursor);
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
    private String getSmsProjection(){
        String smsProjection = 
                "sms._id as _id," +
                "sms.thread_id as thread_id," +
                "sms.address as address," +
                "sms.body as body," +
                "sms.date as date," +
                "sms.read as read," +
                "1 as msg_type," +
                "sms.status as status," +
                "0 as attachment," +
                "0 as m_type," +
                "sms.sim_id as "+ Mms.SIM_ID +"," +
                "sms.type as box_type," +
                "0 as sub_cs";
        return smsProjection;
    }
    
    private String getSmsDraftProjection(){
        String smsProjection = 
                "sms._id as _id," +
                "sms.thread_id as thread_id," +
                "threads.recipient_ids as address," +
                "sms.body as body," +
                "sms.date as date," +
                "sms.read as read," +
                "1 as msg_type," +
                "sms.status as status," +
                "0 as attachment," +
                "0 as m_type," +
                "sms.sim_id as " + Mms.SIM_ID + "," +
                "sms.type as box_type," +
                "0 as sub_cs";
        return smsProjection;
    }
    
    private String getMmsProjection(){
        String mmsProjection = 
            "pdu._id as _id," +
            "pdu.thread_id as thread_id," +
            "threads.recipient_ids as address," +
            "pdu.sub as body," +
            "pdu.date * 1000 as date," +
            "pdu.read as read," +
            "2 as msg_type," +
            "pending_msgs.err_type as status," +
            "(part.ct!='text/plain' AND part.ct!='application/smil') as attachment," +
            "pdu.m_type as m_type," +
            "pdu.sim_id as " + Mms.SIM_ID + "," +
            "pdu.msg_box as box_type," +
            "pdu.sub_cs as sub_cs";
        return mmsProjection;
    }
    
    private String getFinalProjection(){
        String finalProjection = "_id,thread_id,address,body,date,read,msg_type,status," +
                "MAX(attachment) as attachment,m_type," + Mms.SIM_ID + ",box_type,sub_cs";
        return finalProjection;
    }
    
    private Cursor getInboxMessage(SQLiteDatabase db) {
         String cbProjection = "cellbroadcast._id as _id,cellbroadcast.thread_id as thread_id,threads.recipient_ids as address," +
                 "cellbroadcast.body,cellbroadcast.date as date,cellbroadcast.read as read,4 as msg_type,0 as status,0 as attachment,0 as m_type" +
                 ",cellbroadcast.sim_id as " + Mms.SIM_ID + ",0 as box_type,0 as sub_cs";
         String smsQuery = String.format("SELECT %s FROM sms WHERE sms.type=1", getSmsProjection());
         String mmsQuery = String.format(
                 "SELECT %s FROM threads,pending_msgs,pdu left join part ON pdu._id = part.mid WHERE pdu.msg_box = 1 AND (pdu.m_type=130 OR pdu.m_type=132) AND pending_msgs.msg_id=pdu._id " +
                 "AND pdu.thread_id=threads._id", getMmsProjection());
         String mmsNotInPendingQuery = String.format("SELECT pdu._id as _id,pdu.thread_id as thread_id,threads.recipient_ids as address," +
                 "pdu.sub as body,pdu.date * 1000 as date,pdu.read as read,2 as msg_type,0 as status,(part.ct!='text/plain' AND part.ct!='application/smil' " +
                        ") as attachment,pdu.m_type as m_type,pdu.sim_id as " + Mms.SIM_ID + ",pdu.msg_box as box_type,pdu.sub_cs as sub_cs FROM " +
                 "threads,pdu left join part ON pdu._id = part.mid WHERE pdu.msg_box = 1 AND (pdu.m_type=130 OR pdu.m_type=132) AND pdu.thread_id=threads._id AND" +
                 " pdu._id NOT IN( SELECT pdu._id FROM pdu, pending_msgs WHERE pending_msgs.msg_id=pdu._id)");
         String mmsNotInPartQeury = String.format("SELECT pdu._id as _id,pdu.thread_id as thread_id,threads.recipient_ids as address,pdu.sub as body," +
                 "pdu.date * 1000 as date,pdu.read as read,2 as msg_type,pending_msgs.err_type as status,0 as attachment,pdu.m_type as m_type,pdu.sim_id as " + Mms.SIM_ID + "," +
                 "pdu.msg_box as box_type,pdu.sub_cs as sub_cs FROM pdu,threads,pending_msgs WHERE pdu.msg_box = 1 AND (pdu.m_type=130 OR pdu.m_type=132) AND " +
                 "pending_msgs.msg_id=pdu._id AND pdu.thread_id=threads._id AND pdu._id NOT IN (SELECT pdu._id FROM pdu,part WHERE pdu._id=part.mid)");
         String mmsNotInBothQeury = String.format("SELECT pdu._id as _id,pdu.thread_id as thread_id,threads.recipient_ids as address,pdu.sub as body,pdu.date * 1000 as date," +
                 "pdu.read as read,2 as msg_type,0 as status,0 as attachment,pdu.m_type as m_type,pdu.sim_id as " + Mms.SIM_ID + ",pdu.msg_box as box_type,pdu.sub_cs as sub_cs FROM pdu," +
                 "threads WHERE pdu.msg_box = 1 AND (pdu.m_type=130 OR pdu.m_type=132) AND pdu.thread_id=threads._id AND pdu._id NOT IN " +
                 "(SELECT pdu._id FROM pdu,part WHERE pdu._id=part.mid UNION SELECT pdu._id FROM pdu, pending_msgs WHERE pending_msgs.msg_id=pdu._id)");
         String cbQuery = String.format("SELECT %s FROM cellbroadcast,threads WHERE cellbroadcast.thread_id=threads._id", cbProjection);
         String rawQuery = null;
         if(!FeatureOption.MTK_WAPPUSH_SUPPORT){
             rawQuery = String.format(
                     "SELECT %s FROM (%s UNION %s UNION %s UNION %s UNION %s UNION %s) GROUP BY _id,msg_type ORDER BY %s",
                     getFinalProjection(),
                     smsQuery,
                     mmsQuery,
                     mmsNotInPendingQuery,
                     mmsNotInPartQeury,
                     mmsNotInBothQeury,
                     cbQuery,
                     "date DESC");
         } else {
            String wappushProjection = "wappush._id as _id,thread_id,address,coalesce(text||' '||url,text,url) as body," +
                    "date,read,3 as msg_type,0 as status,0 as attachment,0 as m_type,wappush.sim_id as " + Mms.SIM_ID + ",0 as box_type,0 as sub_cs";
              String wappushQuery = String.format("SELECT %s FROM wappush", wappushProjection);
              rawQuery = String.format(
                      "SELECT %s FROM (%s UNION %s UNION %s UNION %s UNION %s UNION %s UNION %s) GROUP BY _id,msg_type ORDER BY %s",
                      getFinalProjection(),
                      smsQuery,
                      mmsQuery,
                      mmsNotInPendingQuery,
                      mmsNotInPartQeury,
                      mmsNotInBothQeury,
                      cbQuery,
                      wappushQuery,
                      "date DESC");
              
         }
        // Log.d(LOG_TAG, "getInboxMessage rawQuery ="+rawQuery);
         return db.rawQuery(rawQuery, null);
    }

    private Cursor getOutboxMessage(SQLiteDatabase db) {
        String smsQuery = String.format("SELECT %s FROM sms WHERE (sms.type=4 OR sms.type=5 OR sms.type=6)",
                getSmsProjection());
        String mmsQuery = String.format("SELECT %s FROM threads,pending_msgs,pdu left join part ON pdu._id = part.mid WHERE" +
                " (pdu.msg_box = 4 AND pdu.m_type=128 AND pdu.thread_id=threads._id AND pending_msgs.msg_id=pdu._id)", getMmsProjection());
        String rawQuery = String.format("SELECT %s FROM (%s UNION %s) GROUP BY _id,msg_type ORDER BY %s", getFinalProjection(), smsQuery, mmsQuery, "date DESC");
       // Log.d(LOG_TAG, "getOutboxMessage rawQuery =" + rawQuery);
        return db.rawQuery(rawQuery, null);
    }

    private Cursor getSentboxMessage(SQLiteDatabase db) {
        String mmsProjection = "pdu._id as _id,pdu.thread_id as thread_id,threads.recipient_ids as address,pdu.sub as body," +
                "pdu.date * 1000 as date,pdu.read as read,2 as msg_type,0 as status," +
                "(part.ct!='text/plain' AND part.ct!='application/smil') as attachment," +
                "pdu.m_type as m_type,pdu.sim_id as " + Mms.SIM_ID + ",pdu.msg_box as box_type,pdu.sub_cs as sub_cs";

        String smsQuery = String.format("SELECT %s FROM sms WHERE sms.type=2", getSmsProjection());
        String mmsQuery = String.format("SELECT %s FROM threads,pdu left join part ON pdu._id = part.mid WHERE pdu.msg_box=2 AND pdu.m_type=128" +
                " AND pdu.thread_id=threads._id", mmsProjection);
        String rawQuery = String.format("SELECT %s FROM (%s UNION %s) GROUP BY _id,msg_type ORDER BY %s", getFinalProjection(),
                smsQuery, mmsQuery, "date DESC");
       // Log.d(LOG_TAG, "getSentboxMessage rawQuery =" + rawQuery);
        return db.rawQuery(rawQuery, null);
    }

    private Cursor getDraftboxMessage(SQLiteDatabase db) {
        String mmsProjection = "pdu._id as _id,pdu.thread_id as thread_id,threads.recipient_ids as address," +
         "pdu.sub as body,pdu.date * 1000 as date,pdu.read as read,2 as msg_type,0 as status," +
         "(part.ct!='text/plain' AND part.ct!='application/smil') as attachment," +
         "pdu.m_type as m_type,pdu.sim_id as " + Mms.SIM_ID + ",pdu.msg_box as box_type,pdu.sub_cs as sub_cs";

        String smsQuery = String.format("SELECT %s FROM sms,threads WHERE sms.type=3 " +
                "AND sms.thread_id=threads._id", getSmsDraftProjection());
        String mmsQuery = String.format("SELECT %s FROM threads,pdu left join part ON pdu._id = part.mid WHERE pdu.msg_box = 3 " +
                "AND pdu.thread_id=threads._id", mmsProjection);
        String rawQuery = String.format("SELECT %s FROM (%s UNION %s) GROUP BY _id,msg_type ORDER BY %s", getFinalProjection(),
                smsQuery, mmsQuery, "date DESC");
       // Log.d(LOG_TAG, "getDraftboxMessage rawQuery =" + rawQuery);
        return db.rawQuery(rawQuery, null);
    }

    // through threadid to get the recipient number.
    private Cursor getRecipientsNumber(String threadId) {
         
        String outerQuery = String.format("SELECT recipient_ids FROM threads WHERE _id = " + threadId);
        Log.d(LOG_TAG, "getRecipientsNumber " + outerQuery);
        return mOpenHelper.getReadableDatabase().rawQuery(outerQuery, EMPTY_STRING_ARRAY);
    }

    private String searchContacts(String pattern) {
     //   Xlog.d(LOG_TAG,"searchContacts pattern = "+pattern);
        String in = " IN ";
        Builder builder = Phone.CONTENT_FILTER_URI.buildUpon();
        builder.appendPath(pattern);      // Builder will encode the query
        Log.d(LOG_TAG, "searchContacts uri = " + builder.build().toString());
        Cursor cursor = getContext().getContentResolver().query(builder.build(), 
                new String[] {Phone.DISPLAY_NAME, Phone.NUMBER}, null, null, null);
        
        /* query the related contact numbers and name */
        HashMap<String,String> contacts = new HashMap<String,String>();
        //gionee gaoj 2012-7-12 modified for CR00640808 start
        Log.d(LOG_TAG, "searchContacts getContentResolver query contact 1 cursor ");
        //gionee gaoj 2012-7-12 modified for CR00640808 end
        try {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String number = getValidNumber(cursor.getString(1));
                //Xlog.d(LOG_TAG,"searchContacts number = " + number + " name = " + name);
                contacts.put(number,name);
            }
        } finally {
            cursor.close();
        }
        Log.d(LOG_TAG, "searchContacts getContentResolver query contact 2");
        /* query the related thread ids */
        Set<Long> threadIds = new HashSet<Long>();
        cursor = mOpenHelper.getReadableDatabase().rawQuery(
                "SELECT " + Threads._ID + "," + Threads.RECIPIENT_IDS + " FROM threads", null);
        try {
            while (cursor.moveToNext()) {
                Long threadId = cursor.getLong(0);
                Set<String> recipients = searchRecipients(cursor.getString(1));
                for (String recipient : recipients) {
                   // Log.d(LOG_TAG, "searchContacts cursor recipient " + recipient);
                    if (recipient.contains(pattern) || likeContacts(contacts, pattern, recipient)) {
              //          Xlog.d(LOG_TAG,"searchContacts likeContacts(contacts, recipient) "+recipient);
                        threadIds.add(threadId);
                        break;
                    }
                }
            }
        } finally {
            cursor.close();
        }
        
        /* to IN sql */
        in += threadIds.toString();
        in = in.replace('[', '(');
        in = in.replace(']', ')');
     //   Xlog.d(LOG_TAG,"searchContacts in = "+in);
        return in;
    }
    
    public static String getValidNumber(String numberOrEmail) {
        if (numberOrEmail == null) {
            return null;
        }
       // Xlog.d(LOG_TAG, "Contact.getValidNumber(): numberOrEmail=" + numberOrEmail);
        String workingNumberOrEmail = new String(numberOrEmail);
        workingNumberOrEmail = workingNumberOrEmail.replaceAll(" ", "").replaceAll("-", "");
        if (numberOrEmail.equals(SELF_ITEM_KEY) || Mms.isEmailAddress(numberOrEmail)) {
            //Xlog.d(LOG_TAG, "Contact.getValidNumber(): The number is me or Email.");
            return numberOrEmail;
        } else if (PhoneNumberUtils.isWellFormedSmsAddress(workingNumberOrEmail)) {
          //  Xlog.d(LOG_TAG, "Contact.getValidNumber(): Number without space and '-' is a well-formed number for sending sms.");
            return workingNumberOrEmail;
        } else {
           // Xlog.d(LOG_TAG, "Contact.getValidNumber(): Unknown formed number");
            workingNumberOrEmail = PhoneNumberUtils.stripSeparators(workingNumberOrEmail);
            workingNumberOrEmail = PhoneNumberUtils.formatNumber(workingNumberOrEmail);
            if (numberOrEmail.equals(workingNumberOrEmail)) {
           //     Xlog.d(LOG_TAG, "Contact.getValidNumber(): Unknown formed number, but the number without local number formatting is a well-formed number.");
                return PhoneNumberUtils.stripSeparators(workingNumberOrEmail);
            } else {
                return numberOrEmail;
            }
        }
    }

    /* match the cantacts name*/
    private boolean likeContacts(HashMap<String,String> contacts,String pattern, String recipient) {
        if (contacts == null ||contacts.isEmpty()) {
           //Xlog.d(LOG_TAG,"likeContacts is null");
           return false;
        }
        Iterator iter = contacts.entrySet().iterator();
        while (iter.hasNext()){
           Map.Entry entry = (Map.Entry) iter.next();
           String number = (String) entry.getKey();
           String name = (String) entry.getValue();
           if (number.equals(recipient)) {
             // Xlog.d(LOG_TAG,"name.contains(pattern) name = "+name+" number = "+number);
              return true;
          }
        }
        return false;
    }
    
    private Set<String> searchRecipients(String recipientIds) {
        /* link the recipient ids to the addresses */
        Set<String> recipients = new HashSet<String>();
        for (String id : recipientIds.split(" ")) {
            /* search the canonical address */
            Cursor cursor = mOpenHelper.getReadableDatabase().rawQuery(
                    "SELECT address FROM canonical_addresses WHERE _id=?", new String[] {id});
            try {
                if (cursor == null || cursor.getCount() == 0) {
           //         Xlog.d(LOG_TAG, "searchRecipients cursor is null");
                    break;
                }
                cursor.moveToFirst();
                String address = cursor.getString(0);
                if (!address.trim().isEmpty()) {
                    recipients.add(cursor.getString(0));
                }
            } finally {
                cursor.close();
            }
        }
        return recipients;
    }

    /**
     * Return the canonical address ID for this address.
     */
    private long getSingleAddressId(String address) {
        boolean isEmail = Mms.isEmailAddress(address);
        boolean isPhoneNumber = Mms.isPhoneNumber(address);

        // We lowercase all email addresses, but not addresses that aren't numbers, because
        // that would incorrectly turn an address such as "My Vodafone" into "my vodafone"
        // and the thread title would be incorrect when displayed in the UI.
        String refinedAddress = isEmail ? address.toLowerCase() : address;
        String selection = "address=?";
        String[] selectionArgs;
        long retVal = -1L;
    //    Xlog.d(LOG_TAG, "refinedAddress = " + refinedAddress);
        if (!isPhoneNumber || (address != null && address.length() > NORMAL_NUMBER_MAX_LENGTH)) {
            selectionArgs = new String[] { refinedAddress };
        } else {
            selection += " OR " + String.format(Locale.ENGLISH, "PHONE_NUMBERS_EQUAL(address, ?, %d)",
                        (mUseStrictPhoneNumberComparation ? 1 : 0));
            selectionArgs = new String[] { refinedAddress, refinedAddress };
        }
    //    Xlog.i(LOG_TAG, "selection: " + selection);
        Cursor cursor = null;

        try {
            SQLiteDatabase db = mOpenHelper.getReadableDatabase();
             //gionee gaoj 2012-3-27 added for CR00555790 start
            if (mMsgBox) {
                String[] strProjection = {BaseColumns._ID, "address"};
                cursor = db.query(
                        "canonical_addresses", strProjection,
                        selection, selectionArgs, null, null, null);

                cursor.moveToPosition(-1);
                while(cursor.moveToNext()) {
                    String addr = cursor.getString(1);
                    if (PhoneNumberUtils.compare(addr, refinedAddress)) {
                        Log.i(LOG_TAG, "gn same addr + refinedAddress = " + addr + "+" + refinedAddress);
                        retVal =  cursor.getLong(0);
                        break;
                    }
                }
                // Gionee lixiaohu 20120802 add for CR00663721 start
                if (gnFLYflag&& !(!isPhoneNumber || (address != null && address.length() > NORMAL_NUMBER_MAX_LENGTH))) {
                    if (retVal== -1L) {
                        String refinedAddress1 = "8"+refinedAddress;
                        selectionArgs = new String[] { refinedAddress1, refinedAddress1 };
                        cursor = db.query(
                            "canonical_addresses", strProjection,
                            selection, selectionArgs, null, null, null);

                        cursor.moveToPosition(-1);
                        while(cursor.moveToNext()) {
                            String addr = cursor.getString(1);
                            if (PhoneNumberUtils.compare(addr, refinedAddress)) {
                                Log.i(LOG_TAG, "gn same addr + refinedAddress = " + addr + "+" + refinedAddress);
                                retVal =  cursor.getLong(0);
                                break;
                            }
                        }
                    }

                    if (retVal== -1L) {
                        String refinedAddress2 = "+7"+refinedAddress;
                        selectionArgs = new String[] { refinedAddress2, refinedAddress2 };
                        cursor = db.query(
                            "canonical_addresses", strProjection,
                            selection, selectionArgs, null, null, null);

                        cursor.moveToPosition(-1);
                        while(cursor.moveToNext()) {
                            String addr = cursor.getString(1);
                            if (PhoneNumberUtils.compare(addr, refinedAddress)) {
                                Log.i(LOG_TAG, "gn same addr + refinedAddress = " + addr + "+" + refinedAddress);
                                retVal =  cursor.getLong(0);
                                break;
                            }
                        }
                    }
                }
                // Gionee lixiaohu 20120802 add for CR00663721 end

                //Gionee qinkai 2012-09-13 add for CR00692926 start
                if (gnNgmflag && !(!isPhoneNumber || (address != null && address.length() > NORMAL_NUMBER_MAX_LENGTH))) {
                    if (retVal== -1L) {
                        String refinedAddress2 = "+39"+refinedAddress;
                        selectionArgs = new String[] { refinedAddress2, refinedAddress2 };
                        cursor = db.query(
                            "canonical_addresses", strProjection,
                            selection, selectionArgs, null, null, null);

                        cursor.moveToPosition(-1);
                        while(cursor.moveToNext()) {
                            String addr = cursor.getString(1);
                            if (PhoneNumberUtils.compare(addr, refinedAddress)) {
                                retVal =  cursor.getLong(0);
                                break;
                            }
                        }
                    }
                }
                //Gionee qinkai 2012-09-13 add for CR00692926 end

                if (retVal== -1L) {
                    ContentValues contentValues = new ContentValues(1);
                    contentValues.put(CanonicalAddressesColumns.ADDRESS, refinedAddress);

                    db = mOpenHelper.getWritableDatabase();
                    retVal = db.insert("canonical_addresses",
                            CanonicalAddressesColumns.ADDRESS, contentValues);

                    Log.d(LOG_TAG, "gn getSingleAddressId: insert new canonical_address for " +
                            /*address*/ "xxxxxx" + ", _id=" + retVal);
                }

                return retVal;
            } else {
             //gionee gaoj 2012-3-27 added for CR00555790 end
            cursor = db.query(
                    "canonical_addresses", CANONICAL_ADDRESSES_COLUMNS_2,
                    selection, selectionArgs, null, null, null);

            if (cursor.getCount() == 0) {
                retVal = insertCanonicalAddresses(mOpenHelper, refinedAddress);
       //         Xlog.d(LOG_TAG, "getSingleAddressId: insert new canonical_address for " +
      //                  /*address*/ "xxxxxx" + ", _id=" + retVal);
                return retVal;
            } else {
       //         Xlog.d(LOG_TAG, "getSingleAddressId(): number matched count is " + cursor.getCount());
                while (cursor.moveToNext()) {
                    String currentNumber = cursor.getString(cursor.getColumnIndexOrThrow(CanonicalAddressesColumns.ADDRESS));
         //           Xlog.d(LOG_TAG, "getSingleAddressId(): currentNumber != null ? " + (currentNumber != null));
                    if (currentNumber != null) {
           //             Xlog.d(LOG_TAG, "getSingleAddressId(): refinedAddress=" + refinedAddress + ", currentNumber=" + currentNumber);
           //             Xlog.d(LOG_TAG, "getSingleAddressId(): currentNumber.length() > 15 ?= " + (currentNumber.length() > NORMAL_NUMBER_MAX_LENGTH));
                        if (refinedAddress.equals(currentNumber) || currentNumber.length() <= NORMAL_NUMBER_MAX_LENGTH) {
                            retVal = cursor.getLong(cursor.getColumnIndexOrThrow(CanonicalAddressesColumns._ID));
          //                  Xlog.d(LOG_TAG, "getSingleAddressId(): get exist id=" + retVal);
                            break;
                        }
                    }
                }
                if (retVal == -1) {
                    retVal = insertCanonicalAddresses(mOpenHelper, refinedAddress);
                }
            }
             //gionee gaoj 2012-3-27 added for CR00555790 start
            }
             //gionee gaoj 2012-3-27 added for CR00555790 end
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return retVal;
    }

    private long insertCanonicalAddresses(SQLiteOpenHelper openHelper, String refinedAddress) {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(CanonicalAddressesColumns.ADDRESS, refinedAddress);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.insert("canonical_addresses", CanonicalAddressesColumns.ADDRESS, contentValues);
    }

    /**
     * Return the canonical address IDs for these addresses.
     */
    private Set<Long> getAddressIds(List<String> addresses) {
        Set<Long> result = new HashSet<Long>(addresses.size());

        for (String address : addresses) {

             //gionee gaoj 2012-3-27 added for CR00555790 start
            if (mMsgBox && address.equals("gn_draft_address_token")) {
                continue;
            }
             //gionee gaoj 2012-3-27 added for CR00555790 end

            if (!address.equals(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR)) {
                long id = getSingleAddressId(address);
                if (id != -1L) {
                    result.add(id);
                } else {
                    Log.e(LOG_TAG, "getAddressIds: address ID not found for " + address);
                }
            }
        }
        return result;
    }

    /**
     * Return a sorted array of the given Set of Longs.
     */
    private long[] getSortedSet(Set<Long> numbers) {
        int size = numbers.size();
        long[] result = new long[size];
        int i = 0;

        for (Long number : numbers) {
            result[i++] = number;
        }

        if (size > 1) {
            Arrays.sort(result);
        }

        return result;
    }

    /**
     * Return a String of the numbers in the given array, in order,
     * separated by spaces.
     */
    private String getSpaceSeparatedNumbers(long[] numbers) {
        int size = numbers.length;
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < size; i++) {
            if (i != 0) {
                buffer.append(' ');
            }
            buffer.append(numbers[i]);
        }
        return buffer.toString();
    }

    /**
     * Insert a record for a new thread.
     */
    // Aurora xuyong 2014-10-23 modified for privacy feature start
    private long insertThread(String recipientIds, int numberOfRecipients) {
    // Aurora xuyong 2014-10-23 modified for privacy feature end
        ContentValues values = new ContentValues(4);

        long date = System.currentTimeMillis();
        values.put(ThreadsColumns.DATE, date - date % 1000);
        values.put(ThreadsColumns.RECIPIENT_IDS, recipientIds);
        values.put("status", 1);
        if (numberOfRecipients > 1) {
            values.put(Threads.TYPE, Threads.BROADCAST_THREAD);
        }
        values.put(ThreadsColumns.MESSAGE_COUNT, 0);

        long result = mOpenHelper.getWritableDatabase().insert("threads", null, values);
        Log.d(LOG_TAG, "insertThread: created new thread_id " + result +
                " for recipientIds " + recipientIds);

        //getContext().getContentResolver().notifyChange(MmsSms.CONTENT_URI, null);
        // Aurora xuyong 2014-10-23 added for privacy feature start
        return result;
        // Aurora xuyong 2014-10-23 added for privacy feature start
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private long insertThread(String recipientIds, int numberOfRecipients, long privacy) {
        ContentValues values = new ContentValues(4);

        long date = System.currentTimeMillis();
        values.put(ThreadsColumns.DATE, date - date % 1000);
        values.put(ThreadsColumns.RECIPIENT_IDS, recipientIds);
        values.put("status", 1);
        values.put("is_privacy", privacy);
        if (numberOfRecipients > 1) {
            values.put(Threads.TYPE, Threads.BROADCAST_THREAD);
        }
        values.put(ThreadsColumns.MESSAGE_COUNT, 0);

        long result = mOpenHelper.getWritableDatabase().insert("threads", null, values);
        Log.d(LOG_TAG, "insertThread: created new thread_id " + result +
                " for recipientIds " + recipientIds);
        // Aurora xuyong 2014-11-10 modified for privacy feature start
        if (privacy > 0) {
            // Aurora xuyong 2014-11-11 modified for bug #9742 start
            MmsSmsDatabaseHelper.addPrivacyNum(mOpenHelper.getReadableDatabase(), privacy);
            // Aurora xuyong 2014-11-11 modified for bug #9742 end
        }
        // Aurora xuyong 2014-11-10 modified for privacy feature end
        //getContext().getContentResolver().notifyChange(MmsSms.CONTENT_URI, null);
        return result;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    private static String THREAD_QUERY;
    // Aurora xuyong 2014-08-25 added for aurora's feature start
    // Aurora xuyong 2014-10-23 modified for privacy feature start
    private static String THREAD_QUERY_WITH_MSG_COUNT;
    // Aurora xuyong 2014-10-23 modified for privacy feature end
    // Aurora xuyong 2014-08-25 added for aurora's feature end
    //Add query parameter "type" so that SMS & WAP Push Message from same sender will be put in different threads.
    static{
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
              // Aurora liugj 2014-01-06 modified for bath-delete optimize start
            THREAD_QUERY = "SELECT _id FROM threads " + "WHERE type<>" + Threads.WAPPUSH_THREAD + " AND recipient_ids=? AND deleted = 0";
        }else{
            THREAD_QUERY = "SELECT _id FROM threads " + "WHERE recipient_ids=? AND deleted = 0";
              // Aurora liugj 2014-01-06 modified for bath-delete optimize end
        }
        // Aurora xuyong 2014-08-25 added for aurora's feature start
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            THREAD_QUERY_WITH_MSG_COUNT = "SELECT _id, message_count FROM threads " + "WHERE type<>" + Threads.WAPPUSH_THREAD + " AND recipient_ids=? AND deleted = 0";
        }else{
            THREAD_QUERY_WITH_MSG_COUNT = "SELECT _id, message_count FROM threads " + "WHERE recipient_ids=? AND deleted = 0";
              // Aurora liugj 2014-01-06 modified for bath-delete optimize end
        }
        // Aurora xuyong 2014-08-25 added for aurora's feature end
    }

    // Aurora xuyong 2014-10-23 added for privacy feature start
    private String rebuildPrivaySelection(String orig, long privacy) {
        return orig + " AND is_privacy = " + privacy;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    /**
     * Return the thread ID for this list of
     * recipients IDs.  If no thread exists with this ID, create
     * one and return it.  Callers should always use
     * Threads.getThreadId to access this information.
     */
    // Aurora xuyong 2014-07-14 modified for aurora's new feature start
    // Aurora xuyong 2014-10-23 modified for privacy feature start
    private synchronized Cursor getThreadId(List<String> recipients, boolean needInsert, long privacy) {
    // Aurora xuyong 2014-10-23 modified for privacy feature end
    // Aurora xuyong 2014-07-14 modified for aurora's new feature end
        Set<Long> addressIds = getAddressIds(recipients);
        String recipientIds = "";

             //gionee gaoj 2012-3-27 added for CR00555790 start
        boolean isDraf = isDraft(recipients);
             //gionee gaoj 2012-3-27 added for CR00555790 end

        // optimize for size==1, which should be most of the cases
        if (addressIds.size() == 1) {
            for (Long addressId : addressIds) {
                recipientIds = Long.toString(addressId);
            }
        } else {
            recipientIds = getSpaceSeparatedNumbers(getSortedSet(addressIds));
        }

             //gionee gaoj 2012-3-27 added for CR00555790 start
        if (mMsgBox && isDraf) {
            recipientIds = recipientIds.concat(" gn_draft_address_token");
            Log.w(LOG_TAG, "getThreadId: is gn draft recipientIs " + recipientIds);
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            long id = gnInsertThread(recipientIds, recipients.size(), privacy);
            // Aurora xuyong 2014-10-23 modified for privacy feature end
            String[] gnSelectionArgs = new String[] { Long.toString(id) };
              // Aurora liugj 2014-01-06 modified for bath-delete optimize start
            String GN_THREAD_QUERY = "SELECT _id FROM threads " + "WHERE date=? AND deleted = 0";
              // Aurora liugj 2014-01-06 modified for bath-delete optimize end

            SQLiteDatabase db = mOpenHelper.getReadableDatabase();
            db = mOpenHelper.getReadableDatabase();
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            Cursor cursor = db.rawQuery(rebuildPrivaySelection(GN_THREAD_QUERY, privacy), gnSelectionArgs);
            // Aurora xuyong 2014-10-23 modified for privacy feature end
            Log.w(LOG_TAG, "GN_DRAFT after gnInsertThread cursorCount=" + cursor.getCount() + " " + Long.toString(id));

            return cursor;
        }
             //gionee gaoj 2012-3-27 added for CR00555790 end

        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.d(LOG_TAG, "getThreadId: recipientIds (selectionArgs) =" + recipientIds);
        }

        String[] selectionArgs = new String[] { recipientIds };
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        // Aurora xuyong 2014-08-25 modified for aurora's feature start
        Cursor cursor = null; 
        if (needInsert) {
          // Aurora xuyong 2014-10-23 modified for privacy feature start
            cursor = db.rawQuery(rebuildPrivaySelection(THREAD_QUERY, privacy), selectionArgs);
          // Aurora xuyong 2014-10-23 modified for privacy feature end
        } else {
          // Aurora xuyong 2014-10-23 modified for privacy feature start
            cursor = db.rawQuery(rebuildPrivaySelection(THREAD_QUERY_WITH_MSG_COUNT, privacy), selectionArgs);
          // Aurora xuyong 2014-10-23 modified for privacy feature end
        }
        // Aurora xuyong 2014-08-25 modified for aurora's feature end
        // Aurora xuyong 2014-07-14 modified for aurora's new feature start
        // Aurora xuyong 2014-10-23 modified for privacy feature start
        if (cursor != null && cursor.getCount() == 0 && needInsert) {
        // Aurora xuyong 2014-10-23 modified for privacy feature end
        // Aurora xuyong 2014-07-14 modified for aurora's new feature end
            cursor.close();

            Log.d(LOG_TAG, "getThreadId: create new thread_id for recipients " + recipients);
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            long id = insertThread(recipientIds, recipients.size(), privacy);
            // Aurora xuyong 2014-10-23 modified for privacy feature end
            db = mOpenHelper.getReadableDatabase();  // In case insertThread closed it
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            cursor = db.rawQuery(rebuildPrivaySelection(THREAD_QUERY, privacy), selectionArgs);
            // Aurora xuyong 2014-10-23 modified for privacy feature end
        }
        if (cursor.getCount() > 1) {
            Log.w(LOG_TAG, "getThreadId: why is cursorCount=" + cursor.getCount());
        }

        return cursor;
    }
    
    /**
     * Insert a record for a new wap push thread.
     */
    private void insertWapPushThread(String recipientIds, int numberOfRecipients) {
        ContentValues values = new ContentValues(4);

        long date = System.currentTimeMillis();
        values.put(ThreadsColumns.DATE, date - date % 1000);
        values.put(ThreadsColumns.RECIPIENT_IDS, recipientIds);
        values.put(ThreadsColumns.TYPE, Threads.WAPPUSH_THREAD);

        long result = mOpenHelper.getWritableDatabase().insert("threads", null, values);
     //   Xlog.d(WAPPUSH_TAG, "insertThread: created new thread_id " + result +
    //            " for recipientIds " + recipientIds);
    //    Xlog.w(WAPPUSH_TAG,"insertWapPushThread!");
        getContext().getContentResolver().notifyChange(MmsSms.CONTENT_URI, null);
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private void insertWapPushThread(String recipientIds, int numberOfRecipients, long privacy) {
        ContentValues values = new ContentValues(4);

        long date = System.currentTimeMillis();
        values.put(ThreadsColumns.DATE, date - date % 1000);
        values.put(ThreadsColumns.RECIPIENT_IDS, recipientIds);
        values.put(ThreadsColumns.TYPE, Threads.WAPPUSH_THREAD);
        values.put("is_privacy", privacy);

        long result = mOpenHelper.getWritableDatabase().insert("threads", null, values);
     //   Xlog.d(WAPPUSH_TAG, "insertThread: created new thread_id " + result +
    //            " for recipientIds " + recipientIds);
    //    Xlog.w(WAPPUSH_TAG,"insertWapPushThread!");
        // Aurora xuyong 2014-11-10 modified for privacy feature start
        if (privacy > 0) {
            // Aurora xuyong 2014-11-11 modified for bug #9742 start
            MmsSmsDatabaseHelper.addPrivacyNum(mOpenHelper.getReadableDatabase(), privacy);
            // Aurora xuyong 2014-11-11 modified for bug #9742 end
        }
        // Aurora xuyong 2014-11-10 modified for privacy feature end
        getContext().getContentResolver().notifyChange(MmsSms.CONTENT_URI, null);
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    /*
    private void insertCellBroadcastThread(String recipientIds, int numberOfRecipients) {
        ContentValues values = new ContentValues(4);

        long date = System.currentTimeMillis();
        values.put(ThreadsColumns.DATE, date - date % 1000);
        values.put(ThreadsColumns.RECIPIENT_IDS, recipientIds);
        values.put(ThreadsColumns.TYPE, Threads.WAPPUSH_THREAD);

        long result = mOpenHelper.getWritableDatabase().insert("threads", null, values);
        Log.d("PUSH", "insertThread: created new thread_id " + result +
                " for recipientIds " + recipientIds);
        Log.w("PUSH","insertCellBroadcastThread!");
        getContext().getContentResolver().notifyChange(MmsSms.CONTENT_URI, null);
    }
    */
    /**
     * Return the wappush thread ID for this list of
     * recipients IDs.  If no thread exists with this ID, create
     * one and return it. It should only be called for wappush
     * 
     */
    // Aurora xuyong 2014-07-14 modified for aurora's new feature start
    // Aurora xuyong 2014-10-23 modified for privacy feature start
    private synchronized Cursor getWapPushThreadId(List<String> recipients, boolean needInsert, long privacy) {
    // Aurora xuyong 2014-10-23 modified for privacy feature end
    // Aurora xuyong 2014-07-14 modified for aurora's new feature end
        Set<Long> addressIds = getAddressIds(recipients);
        String recipientIds = "";
        
        // optimize for size==1, which should be most of the cases
        if (addressIds.size() == 1) {
            for (Long addressId : addressIds) {
                recipientIds = Long.toString(addressId);
            }
        } else {
            recipientIds = getSpaceSeparatedNumbers(getSortedSet(addressIds));
        }

        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.d(LOG_TAG, "getWapPushThreadId: recipientIds (selectionArgs) =" + recipientIds);
        }
        // Aurora liugj 2014-01-06 modified for bath-delete optimize start
        String queryString = "SELECT _id FROM threads " + "WHERE type=" + Threads.WAPPUSH_THREAD + " AND recipient_ids=? AND deleted = 0";
          // Aurora liugj 2014-01-06 modified for bath-delete optimize end
        String[] selectionArgs = new String[] { recipientIds };
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        // Aurora xuyong 2014-10-23 modified for privacy feature start
        Cursor cursor = db.rawQuery(rebuildPrivaySelection(queryString, privacy), selectionArgs);
        // Aurora xuyong 2014-10-23 modified for privacy feature end
        // Aurora xuyong 2014-07-14 modified for aurora's new feature start
        // Aurora xuyong 2014-10-23 modified for privacy feature start
        if (cursor != null && cursor.getCount() == 0 && needInsert) {
        // Aurora xuyong 2014-10-23 modified for privacy feature end
        // Aurora xuyong 2014-07-14 modified for aurora's new feature end
            cursor.close();

            Log.d(LOG_TAG, "getWapPushThreadId: create new thread_id for recipients " + recipients);
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            insertWapPushThread(recipientIds, recipients.size(), privacy);
            // Aurora xuyong 2014-10-23 modified for privacy feature end

            db = mOpenHelper.getReadableDatabase();  // In case insertThread closed it
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            cursor = db.rawQuery(rebuildPrivaySelection(queryString, privacy), selectionArgs);
            // Aurora xuyong 2014-10-23 modified for privacy feature end
        }

        if (cursor.getCount() > 1) {
            Log.w(LOG_TAG, "getWapPushThreadId: why is cursorCount=" + cursor.getCount());
        }

        return cursor;
    }
    /*
    private synchronized Cursor getCellBroadcastThreadId(List<String> recipients) {
        Set<Long> addressIds = getAddressIds(recipients);
        String recipientIds = "";

        // optimize for size==1, which should be most of the cases
        if (addressIds.size() == 1) {
            for (Long addressId : addressIds) {
                recipientIds = Long.toString(addressId);
            }
        } else {
            recipientIds = getSpaceSeparatedNumbers(getSortedSet(addressIds));
        }

        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.d(LOG_TAG, "getCellBroadcastThreadId: recipientIds (selectionArgs) =" + recipientIds);
        }
        
        String queryString = "SELECT _id FROM threads " + "WHERE type=" + Threads.BROADCAST_THREAD + " AND recipient_ids=?";
        String[] selectionArgs = new String[] { recipientIds };
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, selectionArgs);

        if (cursor.getCount() == 0) {
            cursor.close();

            Log.d(LOG_TAG, "getCellBroadcastThreadId: create new thread_id for recipients " + recipients);
            insertCellBroadcastThread(recipientIds, recipients.size());

            db = mOpenHelper.getReadableDatabase();  // In case insertThread closed it
            cursor = db.rawQuery(queryString, selectionArgs);
        }
        
        if (cursor.getCount() > 1) {
            Log.w(LOG_TAG, "getCellBroadcastThreadId: why is cursorCount=" + cursor.getCount());
        }
        
        return cursor;
    }
    */
    private static String concatSelections(String selection1, String selection2) {
        if (TextUtils.isEmpty(selection1)) {
            return selection2;
        } else if (TextUtils.isEmpty(selection2)) {
            return selection1;
        } else {
            return selection1 + " AND " + selection2;
        }
    }

    /**
     * If a null projection is given, return the union of all columns
     * in both the MMS and SMS messages tables.  Otherwise, return the
     * given projection.
     */
    private static String[] handleNullMessageProjection(
            String[] projection) {
        return projection == null ? UNION_COLUMNS : projection;
    }

    /**
     * If a null projection is given, return the set of all columns in
     * the threads table.  Otherwise, return the given projection.
     */
    private static String[] handleNullThreadsProjection(
            String[] projection) {
        return projection == null ? THREADS_COLUMNS : projection;
    }

    /**
     * If a null sort order is given, return "normalized_date ASC".
     * Otherwise, return the given sort order.
     */
    private static String handleNullSortOrder (String sortOrder) {
        return sortOrder == null ? "normalized_date ASC" : sortOrder;
    }

    /**
     * Return existing threads in the database.
     */
    private Cursor getSimpleConversations(String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        return mOpenHelper.getReadableDatabase().query("threads", projection,
                selection, selectionArgs, null, null, " date DESC");
    }

    /**
     * Return the thread which has draft in both MMS and SMS.
     *
     * Use this query:
     *
     *   SELECT ...
     *     FROM (SELECT _id, thread_id, ...
     *             FROM pdu
     *             WHERE msg_box = 3 AND ...
     *           UNION
     *           SELECT _id, thread_id, ...
     *             FROM sms
     *             WHERE type = 3 AND ...
     *          )
     *   ;
     */
    private Cursor getDraftThread(String[] projection, String selection,
            String sortOrder) {
        String[] innerProjection = new String[] {BaseColumns._ID, Conversations.THREAD_ID};
        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setTables(MmsProvider.TABLE_PDU);
        smsQueryBuilder.setTables(SmsProvider.TABLE_SMS);

        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerProjection,
                MMS_COLUMNS, 1, "mms",
                concatSelections(selection, Mms.MESSAGE_BOX + "=" + Mms.MESSAGE_BOX_DRAFTS),
                null, null);
        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerProjection,
                SMS_COLUMNS, 1, "sms",
                concatSelections(selection, Sms.TYPE + "=" + Sms.MESSAGE_TYPE_DRAFT),
                null, null);
        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = unionQueryBuilder.buildUnionQuery(
                new String[] { mmsSubQuery, smsSubQuery }, null, null);

        SQLiteQueryBuilder outerQueryBuilder = new SQLiteQueryBuilder();

        outerQueryBuilder.setTables("(" + unionQuery + ")");

        String outerQuery = outerQueryBuilder.buildQuery(
                projection, null, null, null, sortOrder, null);

        return mOpenHelper.getReadableDatabase().rawQuery(outerQuery, EMPTY_STRING_ARRAY);
    }

    /**
     * Return the most recent message in each conversation in both MMS
     * and SMS.
     *
     * Use this query:
     *
     *   SELECT ...
     *     FROM (SELECT thread_id AS tid, date * 1000 AS normalized_date, ...
     *             FROM pdu
     *             WHERE msg_box != 3 AND ...
     *             GROUP BY thread_id
     *             HAVING date = MAX(date)
     *           UNION
     *           SELECT thread_id AS tid, date AS normalized_date, ...
     *             FROM sms
     *             WHERE ...
     *             GROUP BY thread_id
     *             HAVING date = MAX(date))
     *     GROUP BY tid
     *     HAVING normalized_date = MAX(normalized_date);
     *
     * The msg_box != 3 comparisons ensure that we don't include draft
     * messages.
     */
    private Cursor getConversations(String[] projection, String selection,
            String sortOrder) {
        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setTables(MmsProvider.TABLE_PDU);
        smsQueryBuilder.setTables(SmsProvider.TABLE_SMS);

        String[] columns = handleNullMessageProjection(projection);
        String[] innerMmsProjection = makeProjectionWithDateAndThreadId(
                UNION_COLUMNS, 1000);
        String[] innerSmsProjection = makeProjectionWithDateAndThreadId(
                UNION_COLUMNS, 1);
        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerMmsProjection,
                MMS_COLUMNS, 1, "mms",
                concatSelections(selection, MMS_CONVERSATION_CONSTRAINT),
                "thread_id", "date = MAX(date)");
        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerSmsProjection,
                SMS_COLUMNS, 1, "sms",
                concatSelections(selection, SMS_CONVERSATION_CONSTRAINT),
                "thread_id", "date = MAX(date)");
        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = unionQueryBuilder.buildUnionQuery(
                new String[] { mmsSubQuery, smsSubQuery }, null, null);

        SQLiteQueryBuilder outerQueryBuilder = new SQLiteQueryBuilder();

        outerQueryBuilder.setTables("(" + unionQuery + ")");

        String outerQuery = outerQueryBuilder.buildQuery(
                columns, null, "tid",
                "normalized_date = MAX(normalized_date)", sortOrder, null);

        return mOpenHelper.getReadableDatabase().rawQuery(outerQuery, EMPTY_STRING_ARRAY);
    }

    /**
     * Return the first locked message found in the union of MMS
     * and SMS messages.
     *
     * Use this query:
     *
     *  SELECT _id FROM pdu GROUP BY _id HAVING locked=1 UNION SELECT _id FROM sms GROUP
     *      BY _id HAVING locked=1 LIMIT 1
     *
     * We limit by 1 because we're only interested in knowing if
     * there is *any* locked message, not the actual messages themselves.
     */
    private Cursor getFirstLockedMessage(String[] projection, String selection,
            String sortOrder) {
        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder wappushQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setTables(MmsProvider.TABLE_PDU);
        smsQueryBuilder.setTables(SmsProvider.TABLE_SMS);

        String[] idColumn = new String[] { BaseColumns._ID };

        // NOTE: buildUnionSubQuery *ignores* selectionArgs
        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, idColumn,
                null, 1, "mms",
                selection,
                BaseColumns._ID, "locked=1");

        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, idColumn,
                null, 1, "sms",
                selection,
                BaseColumns._ID, "locked=1");

        String wappushSubQuery = null;
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            wappushQueryBuilder.setTables(WapPushProvider.TABLE_WAPPUSH);
            wappushSubQuery = wappushQueryBuilder.buildUnionSubQuery(
                    MmsSms.TYPE_DISCRIMINATOR_COLUMN, idColumn,
                    null, 1, "wappush",
                    selection,
                    BaseColumns._ID, "locked=1");
        }

        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = null;
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            unionQuery = unionQueryBuilder.buildUnionQuery(
                    new String[] { mmsSubQuery, smsSubQuery, wappushSubQuery }, null, "1");
        }else{
            unionQuery = unionQueryBuilder.buildUnionQuery(
                    new String[] { mmsSubQuery, smsSubQuery }, null, "1");
        }

        Cursor cursor = mOpenHelper.getReadableDatabase().rawQuery(unionQuery, EMPTY_STRING_ARRAY);

        if (DEBUG) {
       //     Xlog.v(LOG_TAG, "getFirstLockedMessage query: " + unionQuery);
       //     Xlog.v(LOG_TAG, "cursor count: " + cursor.getCount());
        }
        return cursor;
    }

//gionee gaoj 2012-3-27 added for CR00555790 start
    /**
     * Return the first stared message found in the union of MMS
     * and SMS messages.
     *
     * Use this query:
     *
     *  SELECT _id FROM pdu GROUP BY _id HAVING star=1 UNION SELECT _id FROM sms GROUP
     *      BY _id HAVING star=1 LIMIT 1
     *
     * We limit by 1 because we're only interested in knowing if
     * there is *any* star message, not the actual messages themselves.
     */
    private Cursor getFirstStarMessage(String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder wappushQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setTables(MmsProvider.TABLE_PDU);
        smsQueryBuilder.setTables(SmsProvider.TABLE_SMS);

        String[] idColumn = new String[] { BaseColumns._ID };

        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, idColumn,
                null, 1, "mms",
                selection, selectionArgs,
                BaseColumns._ID, "star=1");

        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, idColumn,
                null, 1, "sms",
                selection, selectionArgs,
                BaseColumns._ID, "star=1");

        String wappushSubQuery = null;
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            wappushQueryBuilder.setTables(WapPushProvider.TABLE_WAPPUSH);
            wappushSubQuery = wappushQueryBuilder.buildUnionSubQuery(
                    MmsSms.TYPE_DISCRIMINATOR_COLUMN, idColumn,
                    null, 1, "wappush",
                    selection, selectionArgs,
                    BaseColumns._ID, "star=1");
        }

        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = null;
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            unionQuery = unionQueryBuilder.buildUnionQuery(
                    new String[] { mmsSubQuery, smsSubQuery, wappushSubQuery }, null, "1");
        }else{
            unionQuery = unionQueryBuilder.buildUnionQuery(
                    new String[] { mmsSubQuery, smsSubQuery}, null, "1");
        }

        Cursor cursor = mOpenHelper.getReadableDatabase().rawQuery(unionQuery, EMPTY_STRING_ARRAY);

        if (DEBUG) {
            Log.v("MmsSmsProvider", "getFirstLockedMessage query: " + unionQuery);
            Log.v("MmsSmsProvider", "cursor count: " + cursor.getCount());
        }
        return cursor;
    }
//gionee gaoj 2012-3-27 added for CR00555790 end
    
    /**
     * Return every message in each conversation in both MMS
     * and SMS.
     */
    private Cursor getCompleteConversations(String[] projection,
            String selection, String sortOrder) {
        String unionQuery = buildConversationQuery(projection, selection, sortOrder);

        return mOpenHelper.getReadableDatabase().rawQuery(unionQuery, EMPTY_STRING_ARRAY);
    }

    /**
     * Add normalized date and thread_id to the list of columns for an
     * inner projection.  This is necessary so that the outer query
     * can have access to these columns even if the caller hasn't
     * requested them in the result.
     */
    private String[] makeProjectionWithDateAndThreadId(
            String[] projection, int dateMultiple) {
        int projectionSize = projection.length;
        String[] result = new String[projectionSize + 2];

        result[0] = "thread_id AS tid";
        result[1] = "date * " + dateMultiple + " AS normalized_date";
        for (int i = 0; i < projectionSize; i++) {
            result[i + 2] = projection[i];
        }
        return result;
    }

    /**
     * Return the union of MMS and SMS messages for this thread ID.
     */
    private Cursor getConversationMessages(
            String threadIdString, String[] projection, String selection,
            String sortOrder) {
        try {
            Long.parseLong(threadIdString);
        } catch (NumberFormatException exception) {
            Log.e(LOG_TAG, "Thread ID must be a Long.");
            return null;
        }

        String finalSelection = concatSelections(
                selection, "thread_id = " + threadIdString);
        String unionQuery = buildConversationQuery(projection, finalSelection, sortOrder);

        return mOpenHelper.getReadableDatabase().rawQuery(unionQuery, EMPTY_STRING_ARRAY);
    }

    /**
     * Return the union of MMS and SMS messages whose recipients
     * included this phone number.
     *
     * Use this query:
     *
     * SELECT ...
     *   FROM pdu, (SELECT _id AS address_id
     *              FROM addr
     *              WHERE (address='<phoneNumber>' OR
     *              PHONE_NUMBERS_EQUAL(addr.address, '<phoneNumber>', 1/0)))
     *             AS matching_addresses
     *   WHERE pdu._id = matching_addresses.address_id
     * UNION
     * SELECT ...
     *   FROM sms
     *   WHERE (address='<phoneNumber>' OR PHONE_NUMBERS_EQUAL(sms.address, '<phoneNumber>', 1/0));
     */
    private Cursor getMessagesByPhoneNumber(
            String phoneNumber, String[] projection, String selection,
            String sortOrder) {
        String escapedPhoneNumber = DatabaseUtils.sqlEscapeString(phoneNumber);
        String finalMmsSelection =
                concatSelections(
                        selection,
                        "pdu._id = matching_addresses.address_id");
        String finalSmsSelection =
                concatSelections(
                        selection,
                        "(address=" + escapedPhoneNumber + " OR PHONE_NUMBERS_EQUAL(address, " +
                        escapedPhoneNumber + 
                        (mUseStrictPhoneNumberComparation ? ", 1))" : ", 0))"));
        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setDistinct(true);
        smsQueryBuilder.setDistinct(true);
        mmsQueryBuilder.setTables(
                MmsProvider.TABLE_PDU +
                ", (SELECT _id AS address_id " +
                "FROM addr WHERE (address=" + escapedPhoneNumber +
                " OR PHONE_NUMBERS_EQUAL(addr.address, " +
                escapedPhoneNumber + 
                (mUseStrictPhoneNumberComparation ? ", 1))) " : ", 0))) ") +
                "AS matching_addresses");
        smsQueryBuilder.setTables(SmsProvider.TABLE_SMS);

        String[] columns = handleNullMessageProjection(projection);
        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, columns, MMS_COLUMNS,
                0, "mms", finalMmsSelection, null, null);
        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, columns, SMS_COLUMNS,
                0, "sms", finalSmsSelection, null, null);
        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = unionQueryBuilder.buildUnionQuery(
                new String[] { mmsSubQuery, smsSubQuery }, sortOrder, null);

        return mOpenHelper.getReadableDatabase().rawQuery(unionQuery, EMPTY_STRING_ARRAY);
    }

    /**
     * Return the conversation of certain thread ID.
     */
    private Cursor getConversationById(
            String threadIdString, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        try {
            Long.parseLong(threadIdString);
        } catch (NumberFormatException exception) {
            Log.e(LOG_TAG, "Thread ID must be a Long.");
            return null;
        }

        String extraSelection = "_id=" + threadIdString;
        String finalSelection = concatSelections(selection, extraSelection);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        String[] columns = handleNullThreadsProjection(projection);

        queryBuilder.setDistinct(true);
        queryBuilder.setTables("threads");
        return queryBuilder.query(
                mOpenHelper.getReadableDatabase(), columns, finalSelection,
                selectionArgs, sortOrder, null, null);
    }

    private static String joinPduAndPendingMsgTables() {
        return MmsProvider.TABLE_PDU + " LEFT JOIN " + TABLE_PENDING_MSG
                + " ON pdu._id = pending_msgs.msg_id";
    }

    private static String[] createMmsProjection(String[] old) {
        String[] newProjection = new String[old.length];
        for (int i = 0; i < old.length; i++) {
            if (old[i].equals(BaseColumns._ID)) {
                newProjection[i] = "pdu._id";
            } else {
                newProjection[i] = old[i];
            }
        }
        return newProjection;
    }

    private Cursor getUndeliveredMessages(
            String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        String[] mmsProjection = createMmsProjection(projection);

        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setTables(joinPduAndPendingMsgTables());
        smsQueryBuilder.setTables(SmsProvider.TABLE_SMS);

        String finalMmsSelection = concatSelections(
                selection, Mms.MESSAGE_BOX + " = " + Mms.MESSAGE_BOX_OUTBOX);
        String finalSmsSelection = concatSelections(
                selection, "(" + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_OUTBOX
                + " OR " + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_FAILED
                + " OR " + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_QUEUED + ")");

        String[] smsColumns = handleNullMessageProjection(projection);
        String[] mmsColumns = handleNullMessageProjection(mmsProjection);
        String[] innerMmsProjection = makeProjectionWithDateAndThreadId(
                mmsColumns, 1000);
        String[] innerSmsProjection = makeProjectionWithDateAndThreadId(
                smsColumns, 1);

        Set<String> columnsPresentInTable = new HashSet<String>(MMS_COLUMNS);
        columnsPresentInTable.add("pdu._id");
        columnsPresentInTable.add(PendingMessages.ERROR_TYPE);
        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerMmsProjection,
                columnsPresentInTable, 1, "mms", finalMmsSelection,
                null, null);
        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerSmsProjection,
                SMS_COLUMNS, 1, "sms", finalSmsSelection,
                null, null);
        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = unionQueryBuilder.buildUnionQuery(
                new String[] { smsSubQuery, mmsSubQuery }, null, null);

        SQLiteQueryBuilder outerQueryBuilder = new SQLiteQueryBuilder();

        outerQueryBuilder.setTables("(" + unionQuery + ")");

        String outerQuery = outerQueryBuilder.buildQuery(
                smsColumns, null, null, null, sortOrder, null);

        return mOpenHelper.getReadableDatabase().rawQuery(outerQuery, EMPTY_STRING_ARRAY);
    }

    /**
     * Add normalized date to the list of columns for an inner
     * projection.
     */
    private static String[] makeProjectionWithNormalizedDate(
            String[] projection, int dateMultiple) {
        int projectionSize = projection.length;
        String[] result = new String[projectionSize + 1];

        result[0] = "date * " + dateMultiple + " AS normalized_date";
        System.arraycopy(projection, 0, result, 1, projectionSize);
        return result;
    }

    private static String buildConversationQuery(String[] projection,
            String selection, String sortOrder) {
        String[] mmsProjection = createMmsProjection(projection);

        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder cbQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setDistinct(true);
        smsQueryBuilder.setDistinct(true);
        cbQueryBuilder.setDistinct(true);
        mmsQueryBuilder.setTables(joinPduAndPendingMsgTables());
        smsQueryBuilder.setTables(SmsProvider.TABLE_SMS);
        cbQueryBuilder.setTables("cellbroadcast");

        String[] smsColumns = handleNullMessageProjection(projection);
        String[] mmsColumns = handleNullMessageProjection(mmsProjection);
        String[] cbColumns = handleNullMessageProjection(projection);
        String[] innerMmsProjection = makeProjectionWithNormalizedDate(mmsColumns, 1000);
        String[] innerSmsProjection = makeProjectionWithNormalizedDate(smsColumns, 1);
        String[] innerCbProjection = makeProjectionWithNormalizedDate(cbColumns, 1);

        Set<String> columnsPresentInTable = new HashSet<String>(MMS_COLUMNS);
        columnsPresentInTable.add("pdu._id");
        columnsPresentInTable.add(PendingMessages.ERROR_TYPE);

        String mmsSelection = concatSelections(selection,
                                Mms.MESSAGE_BOX + " != " + Mms.MESSAGE_BOX_DRAFTS);
        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerMmsProjection,
                columnsPresentInTable, 0, "mms",
                concatSelections(mmsSelection, MMS_CONVERSATION_CONSTRAINT),
                null, null);
        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerSmsProjection, SMS_COLUMNS,
                0, "sms", concatSelections(selection, SMS_CONVERSATION_CONSTRAINT),
                null, null);
        String cbSubQuery = cbQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerCbProjection, CB_COLUMNS,
                0, "cellbroadcast", selection, null, null);
        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = unionQueryBuilder.buildUnionQuery(
                new String[] { smsSubQuery, mmsSubQuery, cbSubQuery },
                handleNullSortOrder(sortOrder), null);

        SQLiteQueryBuilder outerQueryBuilder = new SQLiteQueryBuilder();

        outerQueryBuilder.setTables("(" + unionQuery + ")");

        return outerQueryBuilder.buildQuery(
                smsColumns, null, null, null, sortOrder, null);
    }

    @Override
    public String getType(Uri uri) {
        return VND_ANDROID_DIR_MMS_SMS;
    }

    @Override
    public int delete(Uri uri, String selection,
            String[] selectionArgs) {
    //    Xlog.d(LOG_TAG, "delete uri = " + uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Context context = getContext();
        int affectedRows = 0;

        switch(URI_MATCHER.match(uri)) {
            // Aurora xuyong 2014-10-23 added for privacy feature start
            case URI_PRIVACY_ACCOUNT_CHANGE:
                // Aurora xuyong 2014-01-29 added for bug #11459 start
                Cursor changeCursor = null;
                try {
                    changeCursor = context.getContentResolver().query(Uri.parse("content://mms-sms/privacy-account-change"), new String[] {"_id"}, selection, selectionArgs, null);
                    if (changeCursor.moveToFirst()) {
                        long threadId = changeCursor.getLong(0);
                        db.beginTransaction();
                        db.execSQL("DELETE FROM sms WHERE thread_id = " + threadId + ";");
                        db.execSQL("DELETE FROM pdu WHERE thread_id = " + threadId + ";");
                        db.setTransactionSuccessful();
                        db.endTransaction();
                    }
                } finally {
                    if (changeCursor != null && !changeCursor.isClosed()) {
                        changeCursor.close();
                    }
                }
                // Aurora xuyong 2014-01-29 added for bug #11459 end
                return db.delete("threads", selection, selectionArgs);
            case URI_PRIVACY_ACCOUNT_ID_OP:
                return db.delete("privacy_table", selection, selectionArgs);
            // Aurora xuyong 2014-10-23 added for privacy feature end
            case URI_CONVERSATIONS_MESSAGES:
                long threadId;
                try {
                    threadId = Long.parseLong(uri.getLastPathSegment());
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "Thread ID must be a long.");
                    break;
                }
                affectedRows = deleteConversation(uri, selection, selectionArgs);
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
              //      Xlog.i(WAPPUSH_TAG,"delete Thread"+threadId);
                    affectedRows += context.getContentResolver().delete(ContentUris.withAppendedId(WapPush.CONTENT_URI_THREAD,threadId),selection,selectionArgs);
                }
                MmsSmsDatabaseHelper.updateThread(db, threadId);
                // Aurora xuyong 2014-10-23 modified for privacy feature start
                MmsSmsDatabaseHelper.updateAllPrivacyNum(db);
                // Aurora xuyong 2014-10-23 modified for privacy feature end
                break;
            case URI_CONVERSATIONS:
           //     Xlog.d(LOG_TAG, "delete URI_CONVERSATIONS begin");
                db.execSQL("DELETE FROM words;");
                // Aurora xuyong 2014-07-04 added for reject feature start
                if (MmsSmsDatabaseHelper.sHasRejectFeature) {
                    if (selection == null || selection.length() == 0) {
                        selection = " reject = 0 ";
                    } else if (!selection.contains("reject")) {
                   // Aurora xuyong 2014-08-07 modified for bug #7069 start
                        selection = "reject = 0 AND " + selection;
                   // Aurora xuyong 2014-08-07 modified for bug #7069 end
                    }
                }
                // Aurora xuyong 2014-10-23 added for privacy feature start
                if (MmsSmsDatabaseHelper.sHasPrivacyFeature) {
                     if (selection == null || selection.length() == 0) {
                        selection = " is_privacy = 0 ";
                    } else if (!selection.contains("is_privacy")) {
                        selection = "is_privacy = 0 AND " + selection;
                    }
                }
                // Aurora xuyong 2014-10-23 added for privacy feature end
                // Aurora xuyong 2014-07-04 added for reject feature end
                affectedRows = MmsProvider.deleteMessages(context, db,
                                        selection, selectionArgs, uri)
                        //Aurora yudingmin 2014-11-03 modified for sync feature start
                        + SmsProvider.deleteMessages(context, db, selection,selectionArgs)
                        //Aurora yudingmin 2014-11-03 modified for sync feature end
                        + db.delete("cellbroadcast", selection, selectionArgs);
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                    affectedRows += context.getContentResolver().delete(WapPush.CONTENT_URI,selection,selectionArgs);
                }
           //     Xlog.d(LOG_TAG, "delete URI_CONVERSATIONS end");
                MmsSmsDatabaseHelper.updateAllThreads(db, null, null);
                // Aurora xuyong 2014-10-23 added for privacy feature start
                MmsSmsDatabaseHelper.updateAllPrivacyNum(db);
                // Aurora xuyong 2014-10-23 added for privacy feature start
                break;
                // gionee zhouyj 2012-06-28 add for CR00628704 start 
            case URI_CONVERSATIONSES:
                affectedRows = deleteConversations(uri, selection, selectionArgs);
                // Aurora xuyong 2014-10-23 added for privacy feature start
                MmsSmsDatabaseHelper.updateAllPrivacyNum(db);
                // Aurora xuyong 2014-10-23 added for privacy feature end
                break;
                // gionee zhouyj 2012-06-28 add for CR00628704 end 
            case URI_OBSOLETE_THREADS:
                String delSelectionString = "_id NOT IN (SELECT DISTINCT thread_id FROM sms " 
                    + "UNION SELECT DISTINCT thread_id FROM cellbroadcast "
                    + "UNION SELECT DISTINCT thread_id FROM pdu) AND (status <> 1)";
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                    delSelectionString = "_id NOT IN (SELECT DISTINCT thread_id FROM sms "
                    + "UNION SELECT DISTINCT thread_id FROM cellbroadcast "
                    + "UNION SELECT DISTINCT thread_id FROM pdu UNION SELECT DISTINCT thread_id FROM wappush) AND (status <> 1)";
                }
                affectedRows = db.delete("threads", delSelectionString, null);
                affectedRows += db.delete("threads",
                                        "recipient_ids = \"\"", null);
                MmsSmsDatabaseHelper.updateAllPrivacyNum(db);
                break;
            case URI_QUICK_TEXT: 
                affectedRows = db.delete(TABLE_QUICK_TEXT, selection, selectionArgs);
                MmsSmsDatabaseHelper.updateAllPrivacyNum(db);
                break;
                
            case URI_CELLBROADCAST: 
                affectedRows = db.delete(TABLE_CELLBROADCAST, selection, selectionArgs);
                MmsSmsDatabaseHelper.updateAllPrivacyNum(db);
                break;
                
             //gionee gaoj 2012-3-27 added for CR00555790 start
            case URI_RECENT_CONTACT:
                // Delete the specified recent contact
                Log.d("MmsSmsProvider", "delete OP : table = " + TABLE_RECENT_CONTACT_ADDRESSES + ", selection = "+ selection);
                affectedRows = db.delete(TABLE_RECENT_CONTACT_ADDRESSES, selection, selectionArgs);
                MmsSmsDatabaseHelper.updateAllPrivacyNum(db);
                break; 
             //gionee gaoj 2012-3-27 added for CR00555790 end
            default:
                throw new UnsupportedOperationException(NO_DELETES_INSERTS_OR_UPDATES);
        }

        if (affectedRows > 0) {
            context.getContentResolver().notifyChange(MmsSms.CONTENT_URI, null);
        }
   //     Xlog.d(LOG_TAG, "delete end");
        return affectedRows;
    }

    /**
     * Delete the conversation with the given thread ID.
     */
    private int deleteConversation(Uri uri, String selection, String[] selectionArgs) {
        // Aurora xuyong 2014-07-04 added for reject feature start
         if (MmsSmsDatabaseHelper.sHasRejectFeature) {
             if (selection == null || selection.length() == 0) {
                 selection = " reject = 0 ";
             } else if (!selection.contains("reject")) {
             // Aurora xuyong 2014-08-07 modified for bug #7069 start
                 selection = "reject = 0 AND " + selection;
             // Aurora xuyong 2014-08-07 modified for bug #7069 end
             }
        }
        // Aurora xuyong 2014-07-04 added for reject feature end
        String threadId = uri.getLastPathSegment();

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String finalSelection = concatSelections(selection, "thread_id = " + threadId);
        
        return MmsProvider.deleteMessages(getContext(), db, finalSelection,
                                          selectionArgs, uri)
                //Aurora yudingmin 2014-11-03 modified for sync feature start
                + SmsProvider.deleteMessages(getContext(), db, finalSelection,selectionArgs)
                //Aurora yudingmin 2014-11-03 modified for sync feature end
                + db.delete("cellbroadcast", finalSelection, selectionArgs);
    }
    
    // gionee zhouyj 2012-07-10 modify for CR00628704 start 
    private int deleteConversations(Uri uri, String selection, String[] selectionArgs) {
        // Aurora xuyong 2014-07-04 added for reject feature start
         if (MmsSmsDatabaseHelper.sHasRejectFeature) {
             if (selection == null || selection.length() == 0) {
                 selection = " reject = 0 ";
             } else if (!selection.contains("reject")) {
             // Aurora xuyong 2014-08-07 modified for bug #7069 start
                 selection = "reject = 0 AND " + selection;
             // Aurora xuyong 2014-08-07 modified for bug #7069 end
             }
        }
        // Aurora xuyong 2014-07-04 added for reject feature end
        //gionee gaoj 2012-10-15 added for CR00705539 start
        int key = -1;
        if (selectionArgs != null){
            if (selectionArgs[0].equals("OneLast")) {
                key = 0;
            } else if (selectionArgs[0].equals("MultLast")) {
                key = 1;
            }
            selectionArgs = null;
        }
        //gionee gaoj 2012-10-15 added for CR00705539 end
        String conversationsIds = uri.getPathSegments().get(1);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        // gionee zhouyj 2012-07-10 modify for CR00664287 start 
          // Aurora liugj 2014-01-06 modified for bath-delete optimize start
        String finalSelection = concatSelections(selection, "thread_id IN ( " + conversationsIds + " )");
          // Aurora liugj 2014-01-06 modified for bath-delete optimize end
        // gionee zhouyj 2012-07-10 modify for CR00664287 end 
        int ret = 0;
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            ret = getContext().getContentResolver().delete(WapPush.CONTENT_URI,finalSelection,selectionArgs);
        }
        ret += MmsProvider.deleteMessages(getContext(), db, finalSelection,
                selectionArgs, uri)
                //Aurora yudingmin 2014-11-06 modified for sync feature start
                + SmsProvider.deleteMessages(getContext(), db, finalSelection,selectionArgs)
                //Aurora yudingmin 2014-11-06 modified for sync feature end
                + db.delete("cellbroadcast", finalSelection, selectionArgs);
          // Aurora liugj 2014-01-06 modified for bath-delete optimize start
        String[] ids = conversationsIds.split(",");
          // Aurora liugj 2014-01-06 modified for bath-delete optimize end
        //gionee gaoj 2012-10-15 modified for CR00705539 start
        if (key == 0) {
            if(ids.length > 100) {
                MmsSmsDatabaseHelper.updateAllThreads(db, null, null);
            } else {
                long id = 0;
                for(int i = 0; i < ids.length; i++) {
                    try {
                        id = Long.parseLong(ids[i]);
                        MmsSmsDatabaseHelper.updateThread(db, id);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        } else if (key == 1) {
            MmsSmsDatabaseHelper.updateAllThreads(db, null, null);
        }
        //gionee gaoj 2012-10-15 added for CR00705539 end
        return ret;
    }
    // gionee zhouyj 2012-07-10 modify for CR00628704 end 

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = URI_MATCHER.match(uri);
        switch (match) {
           // Aurora xuyong 2014-10-23 added for privacy feature start
            case URI_PRIVACY_ACCOUNT_ID_OP:
                db.insertOrThrow("privacy_table", null, values);
                /*Cursor result = db.query("threads", new String[] {"is_privacy"}, " is_privacy = ", new String[] { values.getAsString("is_privacy")}, null, null, null);
                if (result != null && result >= 0) {
                    AuroraPrivacyUtils.setPrivacy(result.getCount(), result.getLong(0))
                }*/
                MmsSmsDatabaseHelper.updateAllPrivacyNum(db);
                return uri;
            // Aurora xuyong 2014-10-23 added for privacy feature end
            case URI_QUICK_TEXT:
                db.insertOrThrow("quicktext", null, values);
                MmsSmsDatabaseHelper.updateAllPrivacyNum(db);
                return uri;

             //gionee gaoj 2012-3-27 added for CR00555790 start
            case URI_SMS_PSW: {
                String strPassword = uri.getPathSegments().get(1);
                if (strPassword != null) {
                    SharedPreferences user = getContext().getSharedPreferences("MsgEncryption", 0);
                    SharedPreferences.Editor editor = user.edit();
                    editor.putString("password", strPassword);
                    editor.commit();
                }
                MmsSmsDatabaseHelper.updateAllPrivacyNum(db);
                return uri;
            }
            case URI_RECENT_CONTACT:
                //insert the content to recnet contact table 
                ContentValues initValues;
                boolean addDate = false;
                if(values == null){
                   addDate = true;
                   initValues = new ContentValues(1);
                }
                else{
                   initValues = new ContentValues(values);
                   if(!values.containsKey("date"))
                      addDate = true;
                }
                if(addDate) {
                   initValues.put("date", new Long(System.currentTimeMillis()));
                }
                Long rowID = db.insert(TABLE_RECENT_CONTACT_ADDRESSES, "body", initValues);
                MmsSmsDatabaseHelper.updateAllPrivacyNum(db);
                if (rowID > 0) {
                    Uri uriReturn = Uri.parse("content://mms-sms//recent_contact/" + rowID);
                    return uriReturn;
                } else{
                    return null;
                }
             //gionee gaoj 2012-3-27 added for CR00555790 end
            default:
                throw new UnsupportedOperationException(NO_DELETES_INSERTS_OR_UPDATES);
        }
    }
    /*
    private ContentValues internalInsertMessages(ContentValues initialValues) {
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        Long threadId = values.getAsLong(Telephony.CbSms.THREAD_ID);
        String address = values.getAsString(Telephony.CbSms.CHANNEL_ID);
        if (((threadId == null) || (threadId == 0)) && (address != null)) {
            values.put(Telephony.CbSms.THREAD_ID,
                    getCellBroadcastThreadId(address));
        }

        if (values.containsKey(Telephony.CbSms.SIM_ID) == false) {
            values.put(Telephony.CbSms.SIM_ID, "");
        }
        if (values.containsKey(Telephony.CbSms.BODY) == false) {
            values.put(Telephony.CbSms.BODY, "");
        }
        if (values.containsKey(Telephony.CbSms.CHANNEL_ID) == false) {
            values.put(Telephony.CbSms.CHANNEL_ID, -1);
        }
        if (values.containsKey(Telephony.CbSms.READ) == false) {
            values.put(Telephony.CbSms.READ, 0);
        }
        if (values.containsKey(Telephony.CbSms.DATE) == false) {
            values.put(Telephony.CbSms.DATE, 0);
        }
        if (values.containsKey(Telephony.CbSms.THREAD_ID) == false) {
            values.put(Telephony.CbSms.THREAD_ID, 0);
        }
        return values;
    }
*/
    // Aurora xuyong 2014-08-05 added for reject start
    private void changeAllMessages(SQLiteDatabase db, String value, String threadId) {
        db.beginTransaction();
        try {
            db.execSQL("UPDATE sms SET reject = " + value + ", update_threads = 1 WHERE (type = 1 AND thread_id = " + threadId + ");");
            db.execSQL("UPDATE pdu SET reject = " + value + ", update_threads = 1 WHERE (msg_box = 1 AND thread_id = " + threadId + ");");
            db.execSQL("UPDATE cellbroadcast SET reject = " + value + ", update_threads= 1 WHERE thread_id = " + threadId + ";");
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }
    // Aurora xuyong 2014-08-05 added for reject end
    // Aurora xuyong 2014-08-05 added for reject start
    private void updateSmsMmsCell(SQLiteDatabase db, String rejectValue, String updateThreadId) {
        db.beginTransaction();
        try {
            db.execSQL("UPDATE sms SET reject = " + rejectValue + ", update_threads = 1 WHERE (type = 1 AND thread_id = " + updateThreadId + ");");
            db.execSQL("UPDATE pdu SET reject = " + rejectValue + ", update_threads = 1 WHERE (msg_box = 1 AND thread_id = " + updateThreadId + ");");
            db.execSQL("UPDATE cellbroadcast SET reject = " + rejectValue + ", update_threads= 1 WHERE thread_id = " + updateThreadId + ";");
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }
    // Aurora xuyong 2014-08-05 added for reject end
    @Override
    public int update(Uri uri, ContentValues values,
            String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int affectedRows = 0;
 //       Xlog.d(LOG_TAG, "update URI is " + uri);
        switch(URI_MATCHER.match(uri)) {
        //Aurora yudingmin 2014-11-05 added for aurora special feature start
        case URI_AURORA_SPECIAL_FEATURE:
            return updateSpecialFeature(values);
            //Aurora yudingmin 2014-11-05 added for aurora special feature end
            // Aurora xuyong 2014-10-23 added for privacy feature start
            case URI_PRIVACY_ACCOUNT_CHANGE:
                if (values.containsKey("threadId")) {
                    String changedId = values.getAsString("threadId");
                    String newId = values.getAsString("is_privacy");
                    values.remove("threadId");
                    try {
                        db.beginTransaction();
                        db.execSQL("UPDATE sms SET is_privacy = " + newId + " WHERE thread_id = " + changedId + "; ");
                        db.execSQL("UPDATE pdu SET is_privacy = " + newId + " WHERE thread_id = " + changedId + "; ");
                        db.execSQL("UPDATE cellbroadcast SET is_privacy = " + newId + " WHERE thread_id = " + changedId + "; ");
                        db.setTransactionSuccessful();
                    } catch (SQLiteException e) {
                        e.printStackTrace();
                    } finally {
                        db.endTransaction();
                    }
                }
                int result = db.update("threads", values, selection, selectionArgs);
                MmsSmsDatabaseHelper.updateAllPrivacyNum(db);
                return result;
            case URI_PRIVACY_ACCOUNT_ID_OP:
                return db.update("privacy_table", values, selection, selectionArgs);
            // Aurora xuyong 2014-10-23 added for privacy feature end
            // Aurora xuyong 2014-08-05 added for reject start
            case AURORA_CHANGE_ALL_MESSAGES:
                String resumeThreadId = uri.getPathSegments().get(1);
                if (values.containsKey("reject")) {
                    String rejectVaule = values.getAsString("reject");
                    changeAllMessages(db, rejectVaule, resumeThreadId);
                    // Aurora yudingmin 2014-12-25 added for bug #10720 start
                    getContext().getContentResolver().notifyChange(
                            MmsSms.CONTENT_URI, null);
                    // Aurora yudingmin 2014-12-25 added for bug #10720 end
                }
                MmsSmsDatabaseHelper.updateThread(db, Long.parseLong(resumeThreadId));
                break;
            // Aurora xuyong 2014-08-05 added for reject end
            // Aurora xuyong 2014-07-02 added for reject feature start
            case AURORA_REJECT_CONVERSATIONS:
                String updateThreadId = uri.getPathSegments().get(1);
                String updateSelection = concatSelections(selection, "_id IN ( " + updateThreadId + " )");
                affectedRows = db.update(TABLE_THREADS, values, updateSelection, null);
             // Aurora xuyong 2014-08-05 added for reject start
                if (values.containsKey("reject")) {
                    String rejectVaule = values.getAsString("reject");
                // Aurora xuyong 2014-08-06 modified for bug #7293 start
                    if (values.containsKey("concurrent_resume") && values.getAsInteger("concurrent_resume").intValue() == 1) {
                       updateSmsMmsCell(db, rejectVaule, updateThreadId);
                    }
                // Aurora xuyong 2014-08-06 modified for bug #7293 end
                }
             // Aurora xuyong 2014-08-05 added for reject end
             // Aurora xuyong 2014-07-21 added for reject feature start
                MmsSmsDatabaseHelper.updateThread(db, Long.parseLong(updateThreadId));
             // Aurora xuyong 2014-07-21 added for reject feature end
                break;
            // Aurora xuyong 2014-07-02 added for reject feature end
            case URI_CONVERSATIONS_MESSAGES:
                String threadIdString = uri.getPathSegments().get(1);
                affectedRows = updateConversation(threadIdString, values,
                        selection, selectionArgs);
                MmsSmsDatabaseHelper.updateAllPrivacyNum(db);
                break;

                // Aurora liugj 2014-01-06 added for bath-delete optimize start
            case URI_CONVERSATIONSES:
                String conversationsIds = uri.getPathSegments().get(1);
                String finalSelection2 = concatSelections(selection, "_id IN ( " + conversationsIds + " )");
                affectedRows = db.update(TABLE_THREADS, values, finalSelection2, null);
                MmsSmsDatabaseHelper.updateAllPrivacyNum(db);
                break;
                // Aurora liugj 2014-01-06 added for bath-delete optimize end

            case URI_PENDING_MSG:
                affectedRows = db.update(TABLE_PENDING_MSG, values, selection, null);
                break;

            case URI_CANONICAL_ADDRESS: {
                String extraSelection = "_id=" + uri.getPathSegments().get(1);
                String finalSelection = TextUtils.isEmpty(selection)
                        ? extraSelection : extraSelection + " AND " + selection;

                affectedRows = db.update(TABLE_CANONICAL_ADDRESSES, values, finalSelection, null);
                break;
            }
            case URI_QUICK_TEXT: 
                affectedRows = db.update(TABLE_QUICK_TEXT, values, 
                        selection, selectionArgs);
                break;
            case URI_STATUS:{
                long threadId;
                try {
                    threadId = Long.parseLong(uri.getLastPathSegment());
             //       Xlog.d(LOG_TAG, "update URI_STATUS Thread ID is " + threadId);
                } catch (NumberFormatException e) {
            //        Xlog.e(LOG_TAG, "Thread ID must be a long.");
                    break;
                }

                affectedRows = db.update(TABLE_THREADS, values, "_id = " + Long.toString(threadId), null);
             //   Xlog.d(LOG_TAG, "update URI_STATUS ok");
                break;
            }

             //gionee gaoj 2012-3-27 added for CR00555790 start
                case URI_ENCRYPTION:
                 String encryptionthreadid = uri.getPathSegments().get(1);
                 if (!encryptionthreadid.equals("-1")) {
                     selection = "_id=" +encryptionthreadid;
                 }
                     affectedRows = db.update(TABLE_THREADS, values,
                                selection, selectionArgs);
                 break;
             //gionee gaoj 2012-3-27 added for CR00555790 end
            default:
                throw new UnsupportedOperationException(
                        NO_DELETES_INSERTS_OR_UPDATES);
        }

        if (affectedRows > 0) {
            getContext().getContentResolver().notifyChange(
                    MmsSms.CONTENT_URI, null);
        }
    //    Xlog.d(LOG_TAG, "update end ");
        return affectedRows;
    }

    //Aurora yudingmin 2014-11-05 added for aurora special feature start
    private int updateSpecialFeature(ContentValues values){
            if(values.containsKey("feature_name")){
                String featurName = values.getAsString("feature_name");
                if ("reject".equals(featurName)) {
                    return updateRejectFeature(values);
                } else if ("privacy".equals(featurName)) {
                    return updatePrivacyFeature(values);
                }
            }
            return 0;
    }
    
    private int updateRejectFeature(ContentValues values){
        int count = 0;
        final int BLACK_OPE_INSERT = 1;
        final int BLACK_OPE_UPDATE = 2;
        final int BLACK_OPE_DELETE = 3;
        // Aurora xuyong 2014-07-22 modified for bug #6742 start
        final int operation;
        if(values.containsKey("insert_update_delete")){
            operation = values.getAsInteger("insert_update_delete");
        } else {
            operation = 0;
        }
        final int isBlack;
        if(values.containsKey("isblack")){
            isBlack = values.getAsInteger("isblack");
        } else {
            isBlack = -2;
        }
      // Aurora xuyong 2014-07-07 added for bug #6433 start
        final int reject;
        if(values.containsKey("rejected")){
            reject = values.getAsInteger("rejected");
        } else {
            reject = 3;
        }
      // Aurora xuyong 2014-07-07 added for bug #6433 end
        final String number;
        if(values.containsKey("number")){
            number = values.getAsString("number");
        } else {
            number = "";
        }
      // Aurora xuyong 2014-07-22 modified for bug #6742 end
        switch (operation) {
            case BLACK_OPE_INSERT:
            // Aurora xuyong 2014-07-22 modified for bug #6742 start
            // Aurora xuyong 2014-07-07 added for bug #6433 start
                final List<String> recipientsSetInsert = new ArrayList<String>();
                recipientsSetInsert.add(number);
                long threadIdInsert = 0;
                Cursor cursorInsert = getThreadId(recipientsSetInsert, true, 0);
                if(cursorInsert != null){
                    if(cursorInsert.moveToFirst()){
                        threadIdInsert = cursorInsert.getLong(cursorInsert.getColumnIndex("_id"));
                    }
                    cursorInsert.close();
                }
                if (threadIdInsert > 0) {
                // Aurora xuyong 2014-07-19 modified for bug #6626 start
                    final Uri threadUriInsert = Uri.parse(
                // Aurora xuyong 2014-07-19 modified for bug #6626 end
                            "content://mms-sms/conversations_reject/" + threadIdInsert);
                // Aurora xuyong 2014-07-19 modified for bug #6626 start
                    final ContentValues threadValuesInsert = new ContentValues();
                // Aurora xuyong 2014-07-19 modified for bug #6626 end
                    threadValuesInsert.put("reject", 1);
                    if (reject > 1) {
                        threadValuesInsert.put("concurrent_resume", 1);
                    } else {
                        threadValuesInsert.put("concurrent_resume", 0);
                    }
                // Aurora xuyong 2014-07-19 modified for bug #6626 start
               // Aurora xuyong 2014-07-21 modified for reject feature start
                    
                            // TODO Auto-generated method stub
                    count = update(threadUriInsert, threadValuesInsert, null, null);
                        }
                // Aurora xuyong 2014-07-21 modified for reject feature end
                // Aurora xuyong 2014-07-19 modified for bug #6626 end
                break;
            // Aurora xuyong 2014-07-07 added for bug #6433 end
            case BLACK_OPE_UPDATE:
            case BLACK_OPE_DELETE:
                final List<String> recipientsSet = new ArrayList<String>();
                recipientsSet.add(number);
                long threadId = 0;
                Cursor cursor = getThreadId(recipientsSet, true, 0);
                if(cursor != null){
                    if(cursor.moveToFirst()){
                        threadId = cursor.getLong(cursor.getColumnIndex("_id"));
                    }
                    cursor.close();
                }
                if (threadId > 0) {
                // Aurora xuyong 2014-07-19 modified for bug #6626 start
                    final Uri threadUri = Uri.parse(
                // Aurora xuyong 2014-07-19 modified for bug #6626 end
                            "content://mms-sms/conversations_reject/" + threadId);
                // Aurora xuyong 2014-07-19 modified for bug #6626 start
                    final ContentValues threadValues = new ContentValues();
                // Aurora xuyong 2014-07-19 modified for bug #6626 end
                // Aurora xuyong 2014-07-04 modified for reject feature start
                    switch(isBlack) {
                        case -1:
                            threadValues.put("reject", 0);
                            threadValues.put("concurrent_resume", 0);
                            break;
                        case 0:
                            threadValues.put("reject", 0);
                            threadValues.put("concurrent_resume", 1);
                            break;
                        case 1:
                            threadValues.put("reject", 1);
                            threadValues.put("concurrent_resume", 1);
                            break;
                 // Aurora xuyong 2014-07-04 modified for reject feature end
                    }
                // Aurora xuyong 2014-07-19 modified for bug #6626 start
               // Aurora xuyong 2014-07-21 modified for reject feature start
                    
                            // TODO Auto-generated method stub
                    count = update(threadUri, threadValues, null, null);
                        }
                // Aurora xuyong 2014-07-22 modified for bug #6742 end
                // Aurora xuyong 2014-07-21 modified for reject feature end
               // Aurora xuyong 2014-07-19 modified for bug #6626 end
                break;
            default:
                break;
        }
        return count;
    }
    // Aurora xuyong 2014-11-06 added for bug #9685 start
    private boolean isStranger(Context context, String number) {
        String existSelection = "PHONE_NUMBERS_EQUAL(data1, " + number + ", 0)";
        Cursor existResult = null;
        try {
            existResult = context.getContentResolver().query(Data.CONTENT_URI, new String[]{"_id"}, existSelection, null, null);
            if (existResult != null && existResult.moveToFirst()) {
                return false;
            }
        } finally {
            if (existResult != null && existResult.isClosed()) {
                existResult.close();
            }
        }
        return true;
    }
    // Aurora xuyong 2014-11-06 added for bug #9685 end
    private int updatePrivacyFeature(ContentValues values){
        Uri sUri1 = Uri.parse("content://mms-sms/privacy-account-change");
        int count = 0;
        long oldId;
        if(values.containsKey("old_account_id")){
            oldId = values.getAsLong("old_account_id");
        } else {
            oldId = -1;
        }
        final long newId;
        if(values.containsKey("new_account_id")){
            newId = values.getAsLong("new_account_id");
        } else {
            newId = -1;
        }
        final String number;
        if(values.containsKey("privacy_number")){
            number = values.getAsString("privacy_number");
        } else {
            number = null;
        }
        if (number == null || oldId == newId) {
            // do nothing
            return 0;
        }
        // Aurora xuyong 2014-11-06 modified for bug #9685 start
        boolean isStranger = isStranger(this.getContext(), number);
        if (oldId == -1 && !isStranger) {
        // Aurora xuyong 2014-11-06 modified for bug #9685 end
            // add a privacy recipient manually
            // do nothing
        // Aurora xuyong 2014-11-06 modified for bug #9685 start
        } else if (oldId == 0 || newId == 0 || (oldId == -1 && isStranger)) {
            if (oldId == -1 && isStranger) {
                oldId = 0;
            }
        // Aurora xuyong 2014-11-06 modified for bug #9685 end
            // add a privacy recipient from contact list
            // need update
            ContentValues values1 = new ContentValues();
            values1.put("is_privacy", newId);
            int recipientId1 = getIdByNumber(getContext(), number);
            // Aurora xuyong 2014-10-30 modified for bug #9450 start
            Cursor cursor = null;
            // Aurora xuyong 2014-10-30 modified for bug #9450 end
            int threadId = -1;
            try {
                // Aurora xuyong 2014-10-30 added for bug #9450 start
                cursor = getContext().getContentResolver().query(sUri1, 
                        new String[] { "_id" },
                        "is_privacy = ? AND recipient_ids = ?",
                        new String[] { "" + oldId, "" + recipientId1},
                        null);
                // Aurora xuyong 2014-10-30 added for bug #9450 end
                if (cursor != null && cursor.moveToFirst()) {
                    threadId = cursor.getInt(0);
                }
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
            values1.put("threadId", threadId);
            getContext().getContentResolver().update(sUri1, values1, "_id = ?", new String[] { "" + threadId});
        } else if (newId == -1) {
            // delete a privacy record
            int recipientId2 = getIdByNumber(getContext(), number);
            getContext().getContentResolver().delete(sUri1, "is_privacy = ? AND recipient_ids = ?", new String[] { "" + oldId, "" + recipientId2});
        }
        MmsSmsDatabaseHelper.updateAllPrivacyNum(mOpenHelper.getReadableDatabase());
        return count;
    }
    
    private int getIdByNumber(Context context, String number) {
        final Uri sUri2 = Uri.parse("content://mms-sms/privacy-account-address");
        // Aurora xuyong 2014-10-30 modified for bug #9450 start
        Cursor cursor = null;
        // Aurora xuyong 2014-10-30 modified for bug #9450 end
        try {
            // Aurora xuyong 2014-10-30 added for bug #9450 start
            cursor = getContext().getContentResolver().query(sUri2, new String[] { "_id" }, "PHONE_NUMBERS_EQUAL(address, " + number + ", 0)", null, null);
            // Aurora xuyong 2014-10-30 added for bug #9450 end
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return -1;
    }
    //Aurora yudingmin 2014-11-05 added for aurora special feature end

    private int updateConversation(
            String threadIdString, ContentValues values, String selection,
            String[] selectionArgs) {
        try {
            Long.parseLong(threadIdString);
        } catch (NumberFormatException exception) {
            Log.e(LOG_TAG, "Thread ID must be a Long.");
            return 0;
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String finalSelection = concatSelections(selection, "thread_id=" + threadIdString);
        return db.update(MmsProvider.TABLE_PDU, values, finalSelection, selectionArgs)
                + db.update("sms", values, finalSelection, selectionArgs)
                        + db.update("cellbroadcast", values, finalSelection, selectionArgs);
    }

    /**
     * Construct Sets of Strings containing exactly the columns
     * present in each table.  We will use this when constructing
     * UNION queries across the MMS and SMS tables.
     */
    private static void initializeColumnSets() {
        int commonColumnCount = MMS_SMS_COLUMNS.length;
        int mmsOnlyColumnCount = MMS_ONLY_COLUMNS.length;
        int smsOnlyColumnCount = SMS_ONLY_COLUMNS.length;
        int cbOnlyColumnCount = CB_ONLY_COLUMNS.length;
        Set<String> unionColumns = new HashSet<String>();

        for (int i = 0; i < commonColumnCount; i++) {
            MMS_COLUMNS.add(MMS_SMS_COLUMNS[i]);
            SMS_COLUMNS.add(MMS_SMS_COLUMNS[i]);
            CB_COLUMNS.add(MMS_SMS_COLUMNS[i]);
            unionColumns.add(MMS_SMS_COLUMNS[i]);
        }
        for (int i = 0; i < mmsOnlyColumnCount; i++) {
            MMS_COLUMNS.add(MMS_ONLY_COLUMNS[i]);
            unionColumns.add(MMS_ONLY_COLUMNS[i]);
        }
        for (int i = 0; i < smsOnlyColumnCount; i++) {
            SMS_COLUMNS.add(SMS_ONLY_COLUMNS[i]);
            unionColumns.add(SMS_ONLY_COLUMNS[i]);
        }
        for (int i = 0; i < cbOnlyColumnCount; i++) {
            CB_COLUMNS.add(CB_ONLY_COLUMNS[i]);
            //unionColumns.add(CB_ONLY_COLUMNS[i]);
        }
        int i = 0;
        for (String columnName : unionColumns) {
            UNION_COLUMNS[i++] = columnName;
        }
    }
//gionee gaoj 2012-3-27 added for CR00555790 start
    private long gnInsertThread(String recipientIds, int numberOfRecipients) {
        ContentValues values = new ContentValues(4);

        long date = System.currentTimeMillis();
        values.put(ThreadsColumns.DATE, date - date % 1000);
        values.put(ThreadsColumns.RECIPIENT_IDS, recipientIds);
        values.put(ThreadsColumns.MESSAGE_COUNT, 0);

        long result = mOpenHelper.getWritableDatabase().insert("threads", null, values);
        Log.i(LOG_TAG, "GN_DRAFT gn_insertThread: created new thread_id " + result +
                " for recipientIds " + /*recipientIds*/ "xxxxxxx " + Long.toString(date - date % 1000));

        getContext().getContentResolver().notifyChange(MmsSms.CONTENT_URI, null);

        return date - date % 1000;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private long gnInsertThread(String recipientIds, int numberOfRecipients, long privacy) {
        ContentValues values = new ContentValues(4);

        long date = System.currentTimeMillis();
        values.put(ThreadsColumns.DATE, date - date % 1000);
        values.put(ThreadsColumns.RECIPIENT_IDS, recipientIds);
        values.put(ThreadsColumns.MESSAGE_COUNT, 0);
        values.put("is_privacy", privacy);

        long result = mOpenHelper.getWritableDatabase().insert("threads", null, values);
        Log.i(LOG_TAG, "GN_DRAFT gn_insertThread: created new thread_id " + result +
                " for recipientIds " + /*recipientIds*/ "xxxxxxx " + Long.toString(date - date % 1000));

        getContext().getContentResolver().notifyChange(MmsSms.CONTENT_URI, null);

        return date - date % 1000;
    }
     // Aurora xuyong 2014-10-23 added for privacy feature end
    private boolean isDraft(List<String> addresses) {

        for (String address : addresses) {
            if (address.equals("gn_draft_address_token")) {
                return true;
            }
        }

        return false;
    }
//gionee gaoj 2012-3-27 added for CR00555790 end
    
    // gionee zhouyj 2012-11-05 add for CR00723896 start 
    private HashMap<String,String> getContactsByNumber(String pattern){
        Builder builder = PICK_PHONE_EMAIL_FILTER_URI.buildUpon();
        builder.appendPath(pattern);      /// M:  Builder will encode the query
         Log.d(LOG_TAG, "getContactsByNumber uri = " + builder.build().toString());
        Cursor cursor = null;
        
        /// M: query the related contact numbers and name
        HashMap<String,String> contacts = new HashMap<String,String>();
        
        try {
            cursor = getContext().getContentResolver().query(builder.build(), 
                new String[] {Phone.DISPLAY_NAME_PRIMARY, Phone.NUMBER}, null, null, "sort_key");
            Log.d(LOG_TAG, "getContactsByNumber getContentResolver query contact 1 cursor " + cursor.getCount());
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String number = getValidNumber(cursor.getString(1));
          //      Xlog.d(LOG_TAG,"getContactsByNumber number = " + number + " name = " + name);
                contacts.put(number,name);
            }
        } catch (IllegalArgumentException ex) {
            Log.d(LOG_TAG,ex.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            
        }
         return contacts;
    }
    
    private String searchContacts(String pattern, HashMap<String,String> contactRes) { 
        String in = " IN ";
        /* query the related thread ids */
        Set<Long> threadIds = new HashSet<Long>();
        Cursor cursor = mOpenHelper.getReadableDatabase().rawQuery(
                "SELECT " + Threads._ID + "," + Threads.RECIPIENT_IDS + " FROM threads", null);
        try {
            while (cursor.moveToNext()) {
                Long threadId = cursor.getLong(0);
                Set<String> recipients = searchRecipients(cursor.getString(1));
                for (String recipient : recipients) {
                   // Log.d(LOG_TAG, "searchContacts cursor recipient " + recipient);
                    if (recipient.contains(pattern) || likeContacts(contactRes, pattern, recipient)) {
                        threadIds.add(threadId);
                        break;
                    }
                }
            }
        } finally {
            cursor.close();
        }
        Log.d(LOG_TAG, "searchContacts getContentResolver query recipient");
        /* to IN sql */
        in += threadIds.toString();
        in = in.replace('[', '(');
        in = in.replace(']', ')');
      //  Xlog.d(LOG_TAG,"searchContacts in = "+in);
        return in;
    }
    
    private String queryIdAndFormatIn(SQLiteDatabase db, String sql) {
        Cursor cursor = null;
      //  Xlog.d(LOG_TAG, "queryIdAndFormatIn sql is: " + sql);
        if (sql != null && sql.trim() != "") {
            cursor = db.rawQuery(sql, null);
        }
        if (cursor == null) {
            return " IN () ";
        }
        try {
       //     Xlog.d(LOG_TAG, "queryIdAndFormatIn Cursor count is: " + cursor.getCount());
            Set<Long> ids = new HashSet<Long>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(0);
                ids.add(id);
            }
            /* to IN sql */
            String in = " IN ";
            in += ids.toString();
            in = in.replace('[', '(');
            in = in.replace(']', ')');
     //       Xlog.d(LOG_TAG,"queryIdAndFormatIn, In = " + in);
            return in;
        } finally {
            cursor.close();
        }
    }
    // gionee zhouyj 2012-11-05 add for CR00723896 end 
}
