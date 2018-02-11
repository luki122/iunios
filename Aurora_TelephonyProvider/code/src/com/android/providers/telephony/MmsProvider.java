/*
 * Copyright (C) 2007 The Android Open Source Project
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
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
// Aurora xuyong 2014-07-18 added for bug #5921 start
import android.database.MatrixCursor;
// Aurora xuyong 2014-07-18 added for bug #5921 end
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Binder;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.provider.Telephony;
import gionee.provider.GnTelephony;
import android.provider.ContactsContract.Data;
import android.provider.Telephony.CanonicalAddressesColumns;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Mms.Addr;
import android.provider.Telephony.Mms.Part;
import android.provider.Telephony.Mms.Rate;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
//import com.mediatek.xlog.Xlog;




import com.android.providers.utils.AuroraSimIdMatching;
import com.google.android.mms.pdu.PduHeaders;
import com.privacymanage.service.AuroraPrivacyUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
//gionee gaoj 2012-9-20 added for CR00699291 start
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
//gionee gaoj 2012-9-20 added for CR00699291 end




import android.os.SystemProperties;

/**
 * The class to provide base facility to access MMS related content,
 * which is stored in a SQLite database and in the file system.
 */
public class MmsProvider extends ContentProvider {
    static final String TABLE_PDU  = "pdu";
    static final String TABLE_ADDR = "addr";
    static final String TABLE_PART = "part";
    static final String TABLE_RATE = "rate";
    static final String TABLE_DRM  = "drm";
    static final String TABLE_WORDS = "words";
    private static final String FOR_MULTIDELETE = "ForMultiDelete";

    //gionee gaoj 2012-9-20 added for CR00699291 start
    private static final String GN_FOR_MULTIDELETE = "GnForMultiDelete";
    //gionee gaoj 2012-9-20 added for CR00699291 end
    // Aurora xuyong 2014-07-03 added for reject feature start
    private static final boolean sHasRejectFeature = SystemProperties.get("ro.aurora.reject.support").equals("yes");
    // Aurora xuyong 2014-07-03 added for reject feature end
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
    public Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
//        Xlog.d(TAG, "query uri = " + uri);
        // Generate the body of the query.
        // Aurora xuyong 2014-07-18 added for bug #5921 start
        if (selection != null && selection.contains("sub_id")) {
            mNeedRebuildCursor = true;
            selection = selection.replaceAll("sub_id", "sim_id");
            }
        if (projection != null && projection.length > 0) {
            for (String pro : projection) {
                if (pro.contains("sub_id")) {
                    mNeedRebuildCursor = true;
                    pro = pro.replaceAll("sub_id", "sim_id");
                }
            }
         }
        // Aurora xuyong 2014-07-18 added for bug #5921 end
        int match = sURLMatcher.match(uri);
        if (LOCAL_LOGV) {
            Log.v(TAG, "Query uri=" + uri + ", match=" + match);
        }

        switch (match) {
            case MMS_ALL:
                constructQueryForBox(qb, Mms.MESSAGE_BOX_ALL);
                // Aurora xuyong 2014-07-24 added for bug #6627 start
                if (MmsSmsDatabaseHelper.sHasRejectFeature) {
                    if (qb != null && qb.getTables().equals("pdu")) {
                        if (selection == null || selection.length() == 0) {
                            selection = "reject = 0";
                        } else if (!selection.contains("reject")) {
                       // Aurora xuyong 2014-08-07 modified for bug #7069 start
                            selection = "reject = 0 AND " + selection;
                       // Aurora xuyong 2014-08-07 modified for bug #7069 end
                        }
                    }
                }
                // Aurora xuyong 2014-07-24 added for bug #6627 end
                // Aurora xuyong 2014-11-04 added for bug #9637 start
                if (MmsSmsDatabaseHelper.sHasPrivacyFeature) {
                    if (qb != null && qb.getTables().equals("pdu")) {
                        if (selection == null || selection.length() == 0) {
                            selection = " is_privacy = 0 ";
                        } else if (!selection.contains("is_privacy")) {
                            selection = "is_privacy = 0 AND " + selection;
                        }
                    }
                }
                // Aurora xuyong 2014-11-04 added for bug #9637 end
                break;
            case MMS_INBOX:
                constructQueryForBox(qb, Mms.MESSAGE_BOX_INBOX);
                break;
            case MMS_SENT:
                constructQueryForBox(qb, Mms.MESSAGE_BOX_SENT);
                break;
            case MMS_DRAFTS:
                constructQueryForBox(qb, Mms.MESSAGE_BOX_DRAFTS);
                break;
            case MMS_OUTBOX:
                constructQueryForBox(qb, Mms.MESSAGE_BOX_OUTBOX);
                break;
            case MMS_ALL_ID:
                qb.setTables(TABLE_PDU);
                qb.appendWhere(Mms._ID + "=" + uri.getPathSegments().get(0));
                break;
            case MMS_INBOX_ID:
            case MMS_SENT_ID:
            case MMS_DRAFTS_ID:
            case MMS_OUTBOX_ID:
                qb.setTables(TABLE_PDU);
                qb.appendWhere(Mms._ID + "=" + uri.getPathSegments().get(1));
                qb.appendWhere(" AND " + Mms.MESSAGE_BOX + "="
                        + getMessageBoxByMatch(match));
                break;
            case MMS_ALL_PART:
                qb.setTables(TABLE_PART);
                break;
            case MMS_MSG_PART:
                qb.setTables(TABLE_PART);
                qb.appendWhere(Part.MSG_ID + "=" + uri.getPathSegments().get(0));
                break;
            case MMS_PART_ID:
                qb.setTables(TABLE_PART);
                qb.appendWhere(Part._ID + "=" + uri.getPathSegments().get(1));
                break;
            case MMS_MSG_ADDR:
                qb.setTables(TABLE_ADDR);
                qb.appendWhere(Addr.MSG_ID + "=" + uri.getPathSegments().get(0));
                break;
            case MMS_REPORT_STATUS:
                /*
                   SELECT DISTINCT address,
                                   T.delivery_status AS delivery_status,
                                   T.read_status AS read_status
                   FROM addr
                   INNER JOIN (SELECT P1._id AS id1, P2._id AS id2, P3._id AS id3,
                                      ifnull(P2.st, 0) AS delivery_status,
                                      ifnull(P3.read_status, 0) AS read_status
                               FROM pdu P1
                               INNER JOIN pdu P2
                               ON P1.m_id = P2.m_id AND P2.m_type = 134
                               LEFT JOIN pdu P3
                               ON P1.m_id = P3.m_id AND P3.m_type = 136
                               UNION
                               SELECT P1._id AS id1, P2._id AS id2, P3._id AS id3,
                                      ifnull(P2.st, 0) AS delivery_status,
                                      ifnull(P3.read_status, 0) AS read_status
                               FROM pdu P1
                               INNER JOIN pdu P3
                               ON P1.m_id = P3.m_id AND P3.m_type = 136
                               LEFT JOIN pdu P2
                               ON P1.m_id = P2.m_id AND P2.m_type = 134) T
                   ON (msg_id = id2 AND type = 151)
                   OR (msg_id = id3 AND type = 137)
                   WHERE T.id1 = ?;
                 */
                qb.setTables("addr INNER JOIN (SELECT P1._id AS id1, P2._id" +
                             " AS id2, P3._id AS id3, ifnull(P2.st, 0) AS" +
                             " delivery_status, ifnull(P3.read_status, 0) AS" +
                             " read_status FROM pdu P1 INNER JOIN pdu P2 ON" +
                             " P1.m_id=P2.m_id AND P2.m_type=134 LEFT JOIN" +
                             " pdu P3 ON P1.m_id=P3.m_id AND P3.m_type=136" +
                             " UNION SELECT P1._id AS id1, P2._id AS id2, P3._id" +
                             " AS id3, ifnull(P2.st, 0) AS delivery_status," +
                             " ifnull(P3.read_status, 0) AS read_status FROM" +
                             " pdu P1 INNER JOIN pdu P3 ON P1.m_id=P3.m_id AND" +
                             " P3.m_type=136 LEFT JOIN pdu P2 ON P1.m_id=P2.m_id" +
                             " AND P2.m_type=134) T ON (msg_id=id2 AND type=151)" +
                             " OR (msg_id=id3 AND type=137)");
                qb.appendWhere("T.id1 = " + uri.getLastPathSegment());
                qb.setDistinct(true);
                break;
            case MMS_REPORT_REQUEST:
                /*
                   SELECT address, d_rpt, rr
                   FROM addr join pdu on pdu._id = addr.msg_id
                   WHERE pdu._id = messageId AND addr.type = 151
                 */
                qb.setTables(TABLE_ADDR + " join " +
                        TABLE_PDU + " on pdu._id = addr.msg_id");
                qb.appendWhere("pdu._id = " + uri.getLastPathSegment());
                qb.appendWhere(" AND " + "addr.type = " + PduHeaders.TO);
                break;
            case MMS_SENDING_RATE:
                qb.setTables(TABLE_RATE);
                break;
            case MMS_DRM_STORAGE_ID:
                qb.setTables(TABLE_DRM);
                qb.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment());
                break;
            case MMS_THREADS:
                qb.setTables("pdu group by thread_id");
                break;
            default:
                Log.e(TAG, "query: invalid request: " + uri);
                return null;
        }

        String finalSortOrder = null;
        if (TextUtils.isEmpty(sortOrder)) {
            if (qb.getTables().equals(TABLE_PDU)) {
                finalSortOrder = Mms.DATE + " DESC";
            } else if (qb.getTables().equals(TABLE_PART)) {
                finalSortOrder = Part.SEQ;
            }
        } else {
            finalSortOrder = sortOrder;
        }
 //       Xlog.d(TAG, "query getReadableDatabase begin");
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
 //       Xlog.d(TAG, "query getReadableDatabase query begin");
        Cursor ret = qb.query(db, projection, selection,
                selectionArgs, null, null, finalSortOrder);
  //      Xlog.d(TAG, "query getReadableDatabase query end");
        if (ret != null){
   //         Xlog.d(TAG, "query getReadableDatabase query end cursor count =" + ret.getCount());
        }
        // TODO: Does this need to be a URI for this provider.
        ret.setNotificationUri(getContext().getContentResolver(), uri);
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
    private void constructQueryForBox(SQLiteQueryBuilder qb, int msgBox) {
        qb.setTables(TABLE_PDU);

        if (msgBox != Mms.MESSAGE_BOX_ALL) {
            qb.appendWhere(Mms.MESSAGE_BOX + "=" + msgBox);
        }
    }

    @Override
    public String getType(Uri uri) {
        int match = sURLMatcher.match(uri);
        switch (match) {
            case MMS_ALL:
            case MMS_INBOX:
            case MMS_SENT:
            case MMS_DRAFTS:
            case MMS_OUTBOX:
                return VND_ANDROID_DIR_MMS;
            case MMS_ALL_ID:
            case MMS_INBOX_ID:
            case MMS_SENT_ID:
            case MMS_DRAFTS_ID:
            case MMS_OUTBOX_ID:
                return VND_ANDROID_MMS;
            case MMS_PART_ID: {
                Cursor cursor = mOpenHelper.getReadableDatabase().query(
                        TABLE_PART, new String[] { Part.CONTENT_TYPE },
                        Part._ID + " = ?", new String[] { uri.getLastPathSegment() },
                        null, null, null);
                if (cursor != null) {
                    try {
                        if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                            return cursor.getString(0);
                        } else {
                            Log.e(TAG, "cursor.count() != 1: " + uri);
                        }
                    } finally {
                        cursor.close();
                    }
                } else {
                    Log.e(TAG, "cursor == null: " + uri);
                }
                return "*/*";
            }
            case MMS_ALL_PART:
            case MMS_MSG_PART:
            case MMS_MSG_ADDR:
            default:
                return "*/*";
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Aurora xuyong 2015-05-22 added for cts test start
    	if (MmsSmsDatabaseHelper.forbidOP(this.getContext())) {
    		return null;
    	}
        // Aurora xuyong 2015-05-22 added for cts test end
//        Xlog.d(TAG, "insert uri = " + uri);
        // Aurora xuyong 2014-11-24 added for apdate huawei honor 6 start
        if (values != null && values.containsKey("network_type")) {
            values.remove("network_type");
        }
        // Aurora xuyong 2014-11-24 added for apdate huawei honor 6 end
        int msgBox = Mms.MESSAGE_BOX_ALL;
        boolean notify = true;

        int match = sURLMatcher.match(uri);
        if (LOCAL_LOGV) {
            Log.v(TAG, "Insert uri=" + uri + ", match=" + match);
        }

        String table = TABLE_PDU;
        switch (match) {
            case MMS_ALL:
                Object msgBoxObj = values.getAsInteger(Mms.MESSAGE_BOX);
                if (msgBoxObj != null) {
                    msgBox = (Integer) msgBoxObj;
                }
                else {
                    // default to inbox
                    msgBox = Mms.MESSAGE_BOX_INBOX;
                }
                break;
            case MMS_INBOX:
                // Aurora xuyong 2014-12-05 added for debug start
                Context context = this.getContext();
                if (MmsSmsDatabaseHelper.forbidOP(context)) {
                    return null;
                }
                // Aurora xuyong 2014-12-05 added for debug end
                if (values.containsKey("need_notify")){
                    notify = values.getAsBoolean("need_notify");
                }
                msgBox = Mms.MESSAGE_BOX_INBOX;
                break;
            case MMS_SENT:
                msgBox = Mms.MESSAGE_BOX_SENT;
                break;
            case MMS_DRAFTS:
                msgBox = Mms.MESSAGE_BOX_DRAFTS;
                break;
            case MMS_OUTBOX:
                msgBox = Mms.MESSAGE_BOX_OUTBOX;
                break;
            case MMS_MSG_PART:
                notify = false;
                table = TABLE_PART;
                break;
            case MMS_MSG_ADDR:
                notify = false;
                table = TABLE_ADDR;
                break;
            case MMS_SENDING_RATE:
                notify = false;
                table = TABLE_RATE;
                break;
            case MMS_DRM_STORAGE:
                notify = false;
                table = TABLE_DRM;
                break;
            default:
                Log.e(TAG, "insert: invalid request: " + uri);
                return null;
        }
//        Xlog.d(TAG, "insert getWritebleDatabase table = " + table);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
 //       Xlog.d(TAG, "insert getWritebleDatabase end");
        ContentValues finalValues;
        Uri res = Mms.CONTENT_URI;
        long rowId;

        if (values.containsKey("need_notify")){
            values.remove("need_notify");
        } 
        // Aurora xuyong 2013-11-16 modified for google adapt start
        if (values.containsKey("text_only")) {
            values.remove("text_only");
        }
        // Aurora xuyong 2013-11-16 modified for google adapt end

        if (table.equals(TABLE_PDU)) {
            boolean addDate = !values.containsKey(Mms.DATE);
            boolean addMsgBox = !values.containsKey(Mms.MESSAGE_BOX);

            // Filter keys we don't support yet.
            filterUnsupportedKeys(values);

            // TODO: Should initialValues be validated, e.g. if it
            // missed some significant keys?
            finalValues = new ContentValues(values);
             // Aurora yudingmin 2014-11-19 added for bug #9304 start
               if (finalValues.containsKey("sub_id")) {
                    finalValues.put("sim_id", finalValues.get("sub_id").toString());
                    finalValues.remove("sub_id");
             }
             if(finalValues.containsKey("sim_id")){
                 String sim_id = mAuroraSimIdMatching.getAuroraSimId(finalValues.get("sim_id").toString());
                 if(!TextUtils.isEmpty(sim_id)){
                    finalValues.put("sim_id", sim_id);
                 }
             }
             // Aurora yudingmin 2014-11-19 added for bug #9304 end
            long timeInMillis = System.currentTimeMillis();

            if (addDate) {
                finalValues.put(Mms.DATE, timeInMillis / 1000L);
            }

            if (addMsgBox && (msgBox != Mms.MESSAGE_BOX_ALL)) {
                finalValues.put(Mms.MESSAGE_BOX, msgBox);
            }

            if (msgBox != Mms.MESSAGE_BOX_INBOX) {
                // Mark all non-inbox messages read.
                finalValues.put(Mms.READ, 1);
            }

            // thread_id
            Long threadId = values.getAsLong(Mms.THREAD_ID);
            String address = values.getAsString(CanonicalAddressesColumns.ADDRESS);
            if (((threadId == null) || (threadId == 0)) && (address != null)) {
             // Aurora xuyong 2014-10-23 modified for privacy feature start
                long privacy = 0l;
                if (msgBox == Mms.MESSAGE_BOX_INBOX) {
                    privacy = AuroraPrivacyUtils.getFristPrivacyId(getContext(), address);
                }
                Set<String> recipients = new HashSet<String>();
                recipients.add(address);
                threadId = AuroraPrivacyUtils.getOrCreateThreadId(getContext(), recipients, privacy);
             // Aurora xuyong 2014-10-23 modified for privacy feature end
                finalValues.put(Mms.THREAD_ID, threadId);
            }
            // Aurora xuyong 2014-10-28 added for privacy feature start
            Cursor privacyResult = null;
            try {
                privacyResult = db.query("threads", new String[] {"is_privacy"}, " _id = ? ", new String[] { String.valueOf(threadId)}, null, null, null);
                if (privacyResult != null && privacyResult.moveToFirst()) {
                    finalValues.put("is_privacy", privacyResult.getLong(0));
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                if (privacyResult != null && !privacyResult.isClosed()) {
                    privacyResult.close();
                }
            }
            // Aurora xuyong 2014-10-28 added for privacy feature end

            if ((rowId = db.insert(table, null, finalValues)) <= 0) {
                Log.e(TAG, "MmsProvider.insert: failed! " + finalValues);
                return null;
            } else {
            //Aurora yudingmin 2014-11-03 added for sync feature start
                if(finalValues.containsKey(Mms.THREAD_ID)){
                    insertAfterPud(rowId);
                }
            //Aurora yudingmin 2014-11-03 added for sync feature end
            }

            setThreadStatus(db, values, 0);
            res = Uri.parse(res + "/" + rowId);
            
            // Aurora xuyong 2014-10-30 added for privacy feature start
            if (match == MMS_INBOX && finalValues != null && finalValues.containsKey("is_privacy")) {
                // Aurora xuyong 2014-11-10 modified for privacy feature start
                long notifyPriValue = finalValues.getAsLong("is_privacy").longValue();
                if (notifyPriValue != AuroraPrivacyUtils.getCurrentAccountId() && notifyPriValue > 0) {
                // Aurora xuyong 2014-11-10 modified for privacy feature end
                    getContext().getContentResolver().notifyChange(MmsSmsProvider.PRIVACY_NOTIFY_URI, null);
                }
            }
            // Aurora xuyong 2014-10-30 added for privacy feature end

        } else if (table.equals(TABLE_ADDR)) {
            finalValues = new ContentValues(values);
            // Aurora xuyong 2014-10-28 added for privacy feature start
            String pduId1 = uri.getPathSegments().get(0);
            finalValues.put(Addr.MSG_ID, pduId1);
            
            Cursor pduResult1 = null;
            try {
                pduResult1 = db.query("pdu", new String[] {"is_privacy"}, " _id = ? ", new String[] { String.valueOf(pduId1)}, null, null, null);
                if (pduResult1 != null && pduResult1.moveToFirst()) {
                    finalValues.put("is_privacy", pduResult1.getLong(0));
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                if (pduResult1 != null && !pduResult1.isClosed()) {
                    pduResult1.close();
                }
            }
            // Aurora xuyong 2014-10-28 added for privacy feature end
            if ((rowId = db.insert(table, null, finalValues)) <= 0) {
                Log.e(TAG, "Failed to insert address: " + finalValues);
                return null;
            }

            res = Uri.parse(res + "/addr/" + rowId);
        } else if (table.equals(TABLE_PART)) {
            finalValues = new ContentValues(values);
            // Aurora xuyong 2014-10-28 added for privacy feature start
            String pduId2 = uri.getPathSegments().get(0);
            // Aurora xuyong 2014-10-28 added for privacy feature end
            if (match == MMS_MSG_PART) {
                finalValues.put(Part.MSG_ID, uri.getPathSegments().get(0));
            }
            // Aurora xuyong 2014-10-28 added for privacy feature start
            Cursor pduResult2 = null;
            String privacyValue = null;
            try {
                pduResult2 = db.query("pdu", new String[] {"is_privacy"}, " _id = ? ", new String[] { String.valueOf(pduId2)}, null, null, null);
                if (pduResult2 != null && pduResult2.moveToFirst()) {
                    privacyValue = pduResult2.getString(0);
                    finalValues.put("is_privacy", privacyValue);
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                if (pduResult2 != null && !pduResult2.isClosed()) {
                    pduResult2.close();
                }
            }
            // Aurora xuyong 2014-10-28 added for privacy feature end
            String contentType = values.getAsString("ct");

            // text/plain and app application/smil store their "data" inline in the
            // table so there's no need to create the file
            boolean plainText = "text/plain".equals(contentType);
            boolean smilText = "application/smil".equals(contentType);
            if (!plainText && !smilText) {
                // Generate the '_data' field of the part with default
                // permission settings.
                String path = getContext().getDir("parts", 0).getPath()
                + "/PART_" + System.currentTimeMillis();

                finalValues.put(Part._DATA, path);

                File partFile = new File(path);
                if (!partFile.exists()) {
                    try {
                        if (!partFile.createNewFile()) {
                            throw new IllegalStateException(
                                    "Unable to create new partFile: " + path);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "createNewFile", e);
                        throw new IllegalStateException(
                                "Unable to create new partFile: " + path);
                    }
                }
            }

            if ((rowId = db.insert(table, null, finalValues)) <= 0) {
                Log.e(TAG, "MmsProvider.insert: failed! " + finalValues);
                return null;
            } else {
                //Aurora yudingmin 2014-11-03 added for sync feature start
                if (match == MMS_MSG_PART) {
                    if(finalValues.containsKey(Part._DATA) && !TextUtils.isEmpty(finalValues.getAsString(Part._DATA))){
                        insertAfterPart(rowId);
                    }
                    String msgid = uri.getPathSegments().get(0);
                    updateBeforePudId(msgid);
                }
                //Aurora yudingmin 2014-11-03 added for sync feature end
            }

            res = Uri.parse(res + "/part/" + rowId);

            // Don't use a trigger for updating the words table because of a bug
            // in FTS3.  The bug is such that the call to get the last inserted
            // row is incorrect.
            if (plainText) {
                // Update the words table with a corresponding row.  The words table
                // allows us to search for words quickly, without scanning the whole
                // table;
                ContentValues cv = new ContentValues();

                // we're using the row id of the part table row but we're also using ids
                // from the sms table so this divides the space into two large chunks.
                // The row ids from the part table start at 2 << 32.
                cv.put(Telephony.MmsSms.WordsTable.ID, (2 << 32) + rowId);
                cv.put(Telephony.MmsSms.WordsTable.INDEXED_TEXT, values.getAsString("text"));
                cv.put(Telephony.MmsSms.WordsTable.SOURCE_ROW_ID, rowId);
                cv.put(Telephony.MmsSms.WordsTable.TABLE_ID, 2);
                db.insert(TABLE_WORDS, Telephony.MmsSms.WordsTable.INDEXED_TEXT, cv);
            }

        } else if (table.equals(TABLE_RATE)) {
            long now = values.getAsLong(Rate.SENT_TIME);
            long oneHourAgo = now - 1000 * 60 * 60;
            // Delete all unused rows (time earlier than one hour ago).
            db.delete(table, Rate.SENT_TIME + "<=" + oneHourAgo, null);
            db.insert(table, null, values);
        } else if (table.equals(TABLE_DRM)) {
            String path = getContext().getDir("parts", 0).getPath()
                    + "/PART_" + System.currentTimeMillis();
            finalValues = new ContentValues(1);
            finalValues.put("_data", path);

            File partFile = new File(path);
            if (!partFile.exists()) {
                try {
                    if (!partFile.createNewFile()) {
                        throw new IllegalStateException(
                                "Unable to create new file: " + path);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "createNewFile", e);
                    throw new IllegalStateException(
                            "Unable to create new file: " + path);
                }
            }

            if ((rowId = db.insert(table, null, finalValues)) <= 0) {
                Log.e(TAG, "MmsProvider.insert: failed! " + finalValues);
                return null;
            }
            res = Uri.parse(res + "/drm/" + rowId);
        } else {
            throw new AssertionError("Unknown table type: " + table);
        }

        if (notify) {
    //        Xlog.d(TAG, "insert getWritebleDatabase notify");
            notifyChange();
        }
        return res;
    }

    //Aurora yudingmin 2014-11-03 added for sync feature start
    private static int deleteBeforePud(Context context, String selection, String[] selectionArgs){
        return context.getContentResolver().delete(MmsSmsSyncProvider.SYNC_MMS_UPDATE_URI, selection, selectionArgs);
    }
    private int deleteBeforePud(String selection, String[] selectionArgs){
        return deleteBeforePud(getContext(), selection, selectionArgs);
    }
    private int updateBeforePud(String selection, String[] selectionArgs){
        return getContext().getContentResolver().update(MmsSmsSyncProvider.SYNC_MMS_UPDATE_URI, null, selection, selectionArgs);
    }
    private int updateBeforePudId(String id){
        return getContext().getContentResolver().update(Uri.withAppendedPath(MmsSmsSyncProvider.SYNC_MMS_UPDATE_URI, id), null, null, null);
    }
    private int updateBeforePart(String selection, String[] selectionArgs){
        return getContext().getContentResolver().update(MmsSmsSyncProvider.SYNC_PART_UPDATE_URI, null, selection, selectionArgs);
    }
    private int updateBeforePartId(String id){
        return getContext().getContentResolver().update(Uri.withAppendedPath(MmsSmsSyncProvider.SYNC_PART_UPDATE_URI, id), null, null, null);
    }
    private Uri insertAfterPud(long rowId){
        Uri sync_mms_id = Uri.withAppendedPath(MmsSmsSyncProvider.SYNC_MMS_UPDATE_URI, String.valueOf(rowId));
        return getContext().getContentResolver().insert(sync_mms_id, null);
    }
    private Uri insertAfterPart(long rowId){
        Uri sync_mms_id = Uri.withAppendedPath(MmsSmsSyncProvider.SYNC_PART_UPDATE_URI, String.valueOf(rowId));
        return getContext().getContentResolver().insert(sync_mms_id, null);
    }
    //Aurora yudingmin 2014-11-03 added for sync feature end
    
    private void setThreadStatus(SQLiteDatabase db, ContentValues values, int value) {
        ContentValues statusContentValues = new ContentValues(1);
        statusContentValues.put(GnTelephony.Threads.STATUS, value);
        db.update("threads", statusContentValues, "_id=" + values.getAsLong(Mms.THREAD_ID), null);
    }
    
    private int getMessageBoxByMatch(int match) {
        switch (match) {
            case MMS_INBOX_ID:
            case MMS_INBOX:
                return Mms.MESSAGE_BOX_INBOX;
            case MMS_SENT_ID:
            case MMS_SENT:
                return Mms.MESSAGE_BOX_SENT;
            case MMS_DRAFTS_ID:
            case MMS_DRAFTS:
                return Mms.MESSAGE_BOX_DRAFTS;
            case MMS_OUTBOX_ID:
            case MMS_OUTBOX:
                return Mms.MESSAGE_BOX_OUTBOX;
            default:
                throw new IllegalArgumentException("bad Arg: " + match);
        }
    }

    //change implement for MulitDelete
    @Override
    public int delete(Uri uri, String selection,
            String[] selectionArgs) {
//        Xlog.d(TAG, "delete");
        int deletedRows = 0;
        boolean notify = false;
        Uri deleteUri = null;
        int match = sURLMatcher.match(uri);
//        Xlog.d(TAG, "delete getWritableDatabase");
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
//        Xlog.d(TAG, "delete getWritableDatabase end");
        switch (match) {
            case MMS_ALL_ID:
            case MMS_INBOX_ID:
            case MMS_SENT_ID:
            case MMS_DRAFTS_ID:
            case MMS_OUTBOX_ID:
            case MMS_ALL:
            case MMS_INBOX:
            case MMS_SENT:
            case MMS_DRAFTS:
            case MMS_OUTBOX:
                notify = true;
            break;
        }
        //gionee gaoj 2012-9-20 added for CR00699291 start
        if (selection != null && selectionArgs != null && selectionArgs[0].equals(GN_FOR_MULTIDELETE)) {
            deletedRows = deleteMultMmsMsg(uri, selection);
            // Aurora xuyong 2014-08-16 added for aurora's new feature start
            if ((deletedRows > 0) && notify) {
                notifyChange();
                MmsSmsDatabaseHelper.updateAllThreads(db, null, null);
            }
            // Aurora xuyong 2014-08-16 added for aurora's new feature end
            return deletedRows;
        }
        //gionee gaoj 2012-9-20 added for CR00699291 end
        if (selection!=null) {
            if (selection.equals(FOR_MULTIDELETE)) {
                int message_id = 0;
                deletedRows = 0;
//                Xlog.d(TAG, "delete beginTransaction");
                db.beginTransaction();
                // Aurora xuyong 2015-01-04 modified for bug #10832 start
                try {
                    for (int i=0; i<selectionArgs.length; i++) {
                        deleteUri = null;
                        if (selectionArgs[i] == null) {
                            //return deletedRows;
                        } else {
                            message_id = Integer.parseInt(selectionArgs[i]);
                            deleteUri = ContentUris.withAppendedId(uri, message_id);
                            //Log.i(TAG, "message_id is " + message_id);
                            deletedRows += deleteOnce(deleteUri, null, null);    
                        }
                    }
                    db.setTransactionSuccessful();
//                Xlog.d(TAG, "delete endTransaction");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                }
                // Aurora xuyong 2015-01-04 modified for bug #10832 end
            } else {
 //               Xlog.d(TAG, "delete deleteOnce");
                deletedRows = deleteOnce(uri, selection, selectionArgs);
            }
        } else {
            deletedRows = deleteOnce(uri, selection, selectionArgs);
        }

        // Aurora xuyong 2014-07-26 modified for bug #6895 start
        if ((deletedRows > 0) && notify) {
            notifyChange();
            MmsSmsDatabaseHelper.updateAllThreads(db, null, null);
        }
        // Aurora xuyong 2014-07-26 modified for bug #6895 end
        return deletedRows;
    }
    
    //gionee gaoj 2012-9-20 added for CR00699291 start
    private int deleteMultMmsMsg(Uri uri, String selection) {
        // TODO Auto-generated method stub

        Log.d("Test", "deleteMultMmsMsg selection = "+selection);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        LinkedList<String> numberList = new LinkedList<String>(Arrays.asList(selection.split(" OR _id = ")));
        for (int i = 0; i < numberList.size(); i++) {
            deleteParts(db, Part.MSG_ID + " = ?",
                    new String[] { numberList.get(i) });
        }

        String finalSelection = Mms._ID + "=" + selection;
        //Aurora yudingmin 2014-11-03 added for sync feature start
        deleteBeforePud(finalSelection, null);
        //Aurora yudingmin 2014-11-03 added for sync feature end
        int count = db.delete(TABLE_PDU, finalSelection, null);
        /*if (count > 0) {
            Uri deleteUri = null;
            long threadid = -1;
            for (int j = 0; j < numberList.size(); j++) {
                threadid = Integer.parseInt(numberList.get(j));
                deleteUri = ContentUris.withAppendedId(uri, threadid);
                Log.d("Test", "mms   deleteUri = "+deleteUri);
                Intent intent = new Intent(Mms.Intents.CONTENT_CHANGED_ACTION);
                intent.putExtra(Mms.Intents.DELETED_CONTENTS, deleteUri);
                getContext().sendBroadcast(intent);
            }
        }*/
        return count;
    }
    //gionee gaoj 2012-9-20 added for CR00699291 end

    public int deleteOnce(Uri uri, String selection,
            String[] selectionArgs) {
 //       Xlog.d(TAG, "deleteOnce begin");
        int match = sURLMatcher.match(uri);
        if (LOCAL_LOGV) {
            Log.v(TAG, "Delete uri=" + uri + ", match=" + match);
        }

        String table, extraSelection = null;
        boolean notify = false;

        switch (match) {
            case MMS_ALL_ID:
            case MMS_INBOX_ID:
            case MMS_SENT_ID:
            case MMS_DRAFTS_ID:
            case MMS_OUTBOX_ID:
                notify = true;
                table = TABLE_PDU;
                extraSelection = Mms._ID + "=" + uri.getLastPathSegment();
                break;
            case MMS_ALL:
            case MMS_INBOX:
            case MMS_SENT:
            case MMS_DRAFTS:
            case MMS_OUTBOX:
                notify = true;
                table = TABLE_PDU;
                if (match != MMS_ALL) {
                    int msgBox = getMessageBoxByMatch(match);
                    extraSelection = Mms.MESSAGE_BOX + "=" + msgBox;
                }
                break;
            case MMS_ALL_PART:
                table = TABLE_PART;
                break;
            case MMS_MSG_PART:
                table = TABLE_PART;
                extraSelection = Part.MSG_ID + "=" + uri.getPathSegments().get(0);
                break;
            case MMS_PART_ID:
                table = TABLE_PART;
                extraSelection = Part._ID + "=" + uri.getPathSegments().get(1);
                break;
            case MMS_MSG_ADDR:
                table = TABLE_ADDR;
                extraSelection = Addr.MSG_ID + "=" + uri.getPathSegments().get(0);
                break;
            case MMS_DRM_STORAGE:
                table = TABLE_DRM;
                break;
            default:
                Log.w(TAG, "No match for URI '" + uri + "'");
                return 0;
        }

        String finalSelection = concatSelections(selection, extraSelection);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int deletedRows = 0;

        if (TABLE_PDU.equals(table)) {
            deletedRows = deleteMessages(getContext(), db, finalSelection,
                                         selectionArgs, uri);
        } else if (TABLE_PART.equals(table)) {
            deletedRows = deleteParts(db, finalSelection, selectionArgs);
        } else if (TABLE_DRM.equals(table)) {
            deletedRows = deleteTempDrmData(db, finalSelection, selectionArgs);
        } else {
            deletedRows = db.delete(table, finalSelection, selectionArgs);
        }
/*
        if ((deletedRows > 0) && notify) {
            notifyChange();
        }
        */
//        Xlog.d(TAG, "deleteOnce end");
        return deletedRows;
    }

    static int deleteMessages(Context context, SQLiteDatabase db,
            String selection, String[] selectionArgs, Uri uri) {
        Cursor cursor = db.query(TABLE_PDU, new String[] { Mms._ID },
                selection, selectionArgs, null, null, null);
        if (cursor == null) {
            return 0;
        }

        try {
            if (cursor.getCount() == 0) {
                return 0;
            }

            while (cursor.moveToNext()) {
                deleteParts(db, Part.MSG_ID + " = ?",
                        new String[] { String.valueOf(cursor.getLong(0)) });
            }
        } finally {
            cursor.close();
        }

        //Aurora yudingmin 2014-11-03 added for sync feature start
        deleteBeforePud(context, selection, selectionArgs);
        //Aurora yudingmin 2014-11-03 added for sync feature end
        int count = db.delete(TABLE_PDU, selection, selectionArgs);
        if (count > 0) {
            Intent intent = new Intent(Mms.Intents.CONTENT_CHANGED_ACTION);
            intent.putExtra(Mms.Intents.DELETED_CONTENTS, uri);
            if (LOCAL_LOGV) {
                Log.v(TAG, "Broadcasting intent: " + intent);
            }
            context.sendBroadcast(intent);
        }
        return count;
    }

    private static int deleteParts(SQLiteDatabase db, String selection,
            String[] selectionArgs) {
        return deleteDataRows(db, TABLE_PART, selection, selectionArgs);
    }

    private static int deleteTempDrmData(SQLiteDatabase db, String selection,
            String[] selectionArgs) {
        return deleteDataRows(db, TABLE_DRM, selection, selectionArgs);
    }

    private static int deleteDataRows(SQLiteDatabase db, String table,
            String selection, String[] selectionArgs) {
        // Aurora xuyong 2015-07-29 modified for bug #14494 start
        Cursor cursor = db.query(table, new String[] { "_data", "ct" },
                selection, selectionArgs, null, null, null);
        // Aurora xuyong 2015-07-29 modified for bug #14494 end
        if (cursor == null) {
            // FIXME: This might be an error, ignore it may cause
            // unpredictable result.
            return 0;
        }

        try {
            if (cursor.getCount() == 0) {
                return 0;
            }

            while (cursor.moveToNext()) {
                try {
                    // Delete the associated files saved on file-system.
                    String path = cursor.getString(0);
                    if (path != null) {
                        new File(path).delete();
                    }
                    // Aurora xuyong 2015-07-29 added for bug #14494 start
                    String contentType = cursor.getString(1);
                    path = getPrefixAndSuffix(path, contentType);
                    // Delte the associated shared files
                    // reference to class: com.android.mms.ui.MessageUtils
                    // method: copyPartsToOutputFile
                    if (path != null) {
                        new File(path).delete();
                    }
                    Log.e(TAG, "\"" + path + "\" has been deleted!");
                    // Aurora xuyong 2015-07-29 added for bug #14494 end
                } catch (Throwable ex) {
                    Log.e(TAG, ex.getMessage(), ex);
                }
            }
        } finally {
            cursor.close();
        }

        return db.delete(table, selection, selectionArgs);
    }
    // Aurora xuyong 2015-07-29 added for bug #14494 start
    private static String getPrefixAndSuffix(String path, String contentType) {
    	String cacheDir = "/storage/emulated/0/Android/data/com.android.mms/cache/shared_files/";
    	String[] items = contentType.split("/");
    	String prefix = items[0];
    	Log.e(TAG, "viewSimpleSildeshow. prefix:" + prefix);
    	String suffix = items[items.length - 1];
    	Log.e(TAG, "viewSimpleSildeshow. suffix:" + suffix);
    	// for the special format 3gpp whose real format is 3gp 
    	if ("3gpp".equals(suffix)) {
    		suffix = "3gp";
    	}
    	if (path != null) {
			// format code from such pattern ---> /data/data/com.android.providers.telephony/app_parts/PART_1438164006213
			// we get the tail serial numbers as the new tempfile name
			String[] item = path.split("_");
			path = item[item.length - 1];
		}
    	return cacheDir + prefix + "_" + path + "." + suffix;
    }
    // Aurora xuyong 2015-07-29 added for bug #14494 end

    @Override
    public int update(Uri uri, ContentValues values,
            String selection, String[] selectionArgs) {
        // Aurora xuyong 2015-05-22 added for cts test start
    	if (MmsSmsDatabaseHelper.forbidOP(this.getContext())) {
    		return 0;
    	}
        // Aurora xuyong 2015-05-22 added for cts test end
      // Aurora xuyong 2014-10-28 added for privacy feature start
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
      // Aurora xuyong 2014-10-28 added for privacy feature end
        int match = sURLMatcher.match(uri);
        if (LOCAL_LOGV) {
            Log.v(TAG, "Update uri=" + uri + ", match=" + match);
        }
        // Aurora xuyong 2014-09-19 added for aurora reject feature start
        boolean mIsNewComingMms = false;
        if (values != null && values.containsKey("newComing")) {
            if (values.getAsInteger("newComing").intValue() == 1) {
                mIsNewComingMms = true;
            }
            values.remove("newComing");
        }
        // Aurora xuyong 2014-09-19 added for aurora reject feature end
 //       Xlog.d(TAG, "update");
        boolean notify = false;
        String msgId = null;
        String table;

        switch (match) {
            case MMS_ALL_ID:
            case MMS_INBOX_ID:
            case MMS_SENT_ID:
            case MMS_DRAFTS_ID:
            case MMS_OUTBOX_ID:
                msgId = uri.getLastPathSegment();
            // fall-through
            case MMS_ALL:
            case MMS_INBOX:
            case MMS_SENT:
            case MMS_DRAFTS:
            case MMS_OUTBOX:
                notify = true;
                table = TABLE_PDU;
                break;
            case MMS_MSG_PART:
            case MMS_PART_ID:
                if (values.containsKey("need_notify")){
                    notify = values.getAsBoolean("need_notify");
                }
                table = TABLE_PART;
                break;
            default:
                Log.w(TAG, "Update operation for '" + uri + "' not implemented.");
                return 0;
        }

        if (values.containsKey("need_notify")){
            values.remove("need_notify");
        }

        String extraSelection = null;
        ContentValues finalValues;
        if (table.equals(TABLE_PDU)) {
            // Filter keys that we don't support yet.
            filterUnsupportedKeys(values);
            finalValues = new ContentValues(values);

            if (msgId != null) {
                extraSelection = Mms._ID + "=" + msgId;
            }
        } else if (table.equals(TABLE_PART)) {
            finalValues = new ContentValues(values);

            switch (match) {
                case MMS_MSG_PART:
                    extraSelection = Part.MSG_ID + "=" + uri.getPathSegments().get(0);
                    break;
                case MMS_PART_ID:
                    extraSelection = Part._ID + "=" + uri.getPathSegments().get(1);
                    break;
                default:
                    break;
            }
        } else {
            return 0;
        }

        String finalSelection = concatSelections(selection, extraSelection);
 //       Xlog.d(TAG, "getWritableDatabase");
        // Aurora xuyong 2014-10-28 deleted for privacy feature start
        //SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        // Aurora xuyong 2014-10-28 deleted for privacy feature end
 //       Xlog.d(TAG, "getWritableDatabase end");
        //Aurora yudingmin 2014-11-03 added for sync feature start
        ArrayList<String> updateMsgIds = new ArrayList<String>();
        if(table.equals(TABLE_PDU)){
            updateBeforePud(finalSelection, selectionArgs);
        } else if(table.equals(TABLE_PART)){
            switch (match) {
            case MMS_MSG_PART:
                updateMsgIds.add(uri.getPathSegments().get(0));
                updateBeforePart(finalSelection, selectionArgs);
                break;
            case MMS_PART_ID:
                Cursor part_message = query(uri, null, null, null, null);
                if(part_message != null && part_message.moveToFirst()){
                    String updateMsgId = part_message.getString(part_message.getColumnIndex(Part.MSG_ID));
                    updateMsgIds.add(updateMsgId);
                    part_message.close();
                }
                updateBeforePartId(uri.getPathSegments().get(1));
                break;
            default:
                break;
           }
        }
        //Aurora yudingmin 2014-11-03 added for sync feature end
        int count = db.update(table, finalValues, finalSelection, selectionArgs);
        // Aurora xuyong 2014-10-28 added for privacy feature start
        if (table.equals(TABLE_PDU) && msgId != null) {
            Cursor threadIdCursor = null;
            Cursor privacyCursor = null;
            db.beginTransaction();
            try {
                threadIdCursor = db.query("pdu", new String[] { "thread_id" }, " _id = ? ", new String[] { String.valueOf(msgId)}, null, null, null);
                if (threadIdCursor != null && threadIdCursor.moveToFirst()) {
                    long threadId = threadIdCursor.getLong(0);
                    privacyCursor = db.query("threads", new String[] { "is_privacy" }, " _id = ? ", new String[] { String.valueOf(threadId)}, null, null, null);
                    if (privacyCursor != null && privacyCursor.moveToFirst()) {
                        int privacy = privacyCursor.getInt(0);
                        finalValues.put("is_privacy", privacy);
                        db.execSQL("UPDATE pdu SET is_privacy = " + privacy + " WHERE _id = " + msgId);
                        db.execSQL("UPDATE addr SET is_privacy = " + privacy + " WHERE msg_id = " + msgId);
                        db.execSQL("UPDATE part SET is_privacy = " + privacy + " WHERE mid = " + msgId);
                        db.setTransactionSuccessful();
                    }
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
                if(threadIdCursor != null && !threadIdCursor.isClosed()) {
                    threadIdCursor.close();
                }
                if(privacyCursor != null && !privacyCursor.isClosed()) {
                    privacyCursor.close();
                }
            }
        }
        //Aurora yudingmin 2014-11-03 added for sync feature start
        if(updateMsgIds != null && updateMsgIds.size() > 0){
            for(int i = 0; i < updateMsgIds.size(); i++){
                updateBeforePudId(updateMsgIds.get(i));
            }
        }
        //Aurora yudingmin 2014-11-03 added for sync feature end
        // Aurora xuyong 2014-10-28 added for privacy feature end
        if (notify && (count > 0)) {
            notifyChange();
        }
        // Aurora xuyong 2014-09-19 added for aurora reject feature start
        if (mIsNewComingMms && values != null && values.containsKey("reject") && values.getAsInteger("reject").intValue() == 1) {
            getContext().getContentResolver().notifyChange(MmsSmsProvider.REJECT_NOTIFY_URI, null);
        }
        // Aurora xuyong 2014-09-19 added for aurora reject feature end
        return count;
    }

    private ParcelFileDescriptor getTempStoreFd() {
        String fileName = GnTelephony.Mms.ScrapSpace.SCRAP_FILE_PATH;
        ParcelFileDescriptor pfd = null;

        try {
            File file = new File(fileName);

            // make sure the path is valid and directories created for this file.
            File parentFile = file.getParentFile();
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                Log.e(TAG, "[MmsProvider] getTempStoreFd: " + parentFile.getPath() +
                        "does not exist!");
                return null;
            }

            pfd = ParcelFileDescriptor.open(file,
                    ParcelFileDescriptor.MODE_READ_WRITE
                            | android.os.ParcelFileDescriptor.MODE_CREATE);
        } catch (Exception ex) {
            Log.e(TAG, "getTempStoreFd: error creating pfd for " + fileName, ex);
        }

        return pfd;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        // if the url is "content://mms/takePictureTempStore", then it means the requester
        // wants a file descriptor to write image data to.

        ParcelFileDescriptor fd;
        int match = sURLMatcher.match(uri);

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.d(TAG, "openFile: uri=" + uri + ", mode=" + mode);
        }

        switch (match) {
            case MMS_SCRAP_SPACE:
                fd = getTempStoreFd();
                break;

            default:
                fd = openFileHelper(uri, mode);
        }

        return fd;
    }

    private void filterUnsupportedKeys(ContentValues values) {
        // Some columns are unsupported.  They should therefore
        // neither be inserted nor updated.  Filter them out.
        values.remove(Mms.DELIVERY_TIME_TOKEN);
        values.remove(Mms.SENDER_VISIBILITY);
        values.remove(Mms.REPLY_CHARGING);
        values.remove(Mms.REPLY_CHARGING_DEADLINE_TOKEN);
        values.remove(Mms.REPLY_CHARGING_DEADLINE);
        values.remove(Mms.REPLY_CHARGING_ID);
        values.remove(Mms.REPLY_CHARGING_SIZE);
        values.remove(Mms.PREVIOUSLY_SENT_BY);
        values.remove(Mms.PREVIOUSLY_SENT_DATE);
        values.remove(Mms.STORE);
        values.remove(Mms.MM_STATE);
        values.remove(Mms.MM_FLAGS_TOKEN);
        values.remove(Mms.MM_FLAGS);
        values.remove(Mms.STORE_STATUS);
        values.remove(Mms.STORE_STATUS_TEXT);
        values.remove(Mms.STORED);
        values.remove(Mms.TOTALS);
        values.remove(Mms.MBOX_TOTALS);
        values.remove(Mms.MBOX_TOTALS_TOKEN);
        values.remove(Mms.QUOTAS);
        values.remove(Mms.MBOX_QUOTAS);
        values.remove(Mms.MBOX_QUOTAS_TOKEN);
        values.remove(Mms.MESSAGE_COUNT);
        values.remove(Mms.START);
        values.remove(Mms.DISTRIBUTION_INDICATOR);
        values.remove(Mms.ELEMENT_DESCRIPTOR);
        values.remove(Mms.LIMIT);
        values.remove(Mms.RECOMMENDED_RETRIEVAL_MODE);
        values.remove(Mms.RECOMMENDED_RETRIEVAL_MODE_TEXT);
        values.remove(Mms.STATUS_TEXT);
        values.remove(Mms.APPLIC_ID);
        values.remove(Mms.REPLY_APPLIC_ID);
        values.remove(Mms.AUX_APPLIC_ID);
        values.remove(Mms.DRM_CONTENT);
        values.remove(Mms.ADAPTATION_ALLOWED);
        values.remove(Mms.REPLACE_ID);
        values.remove(Mms.CANCEL_ID);
        values.remove(Mms.CANCEL_STATUS);

        // Keys shouldn't be inserted or updated.
        values.remove(Mms._ID);
    }

    private void notifyChange() {
        getContext().getContentResolver().notifyChange(
                MmsSms.CONTENT_URI, null);
    }

    private final static String TAG = "MmsProvider";
    private final static String VND_ANDROID_MMS = "vnd.android/mms";
    private final static String VND_ANDROID_DIR_MMS = "vnd.android-dir/mms";
    private final static boolean DEBUG = false;
    private final static boolean LOCAL_LOGV = false;

    private static final int MMS_ALL                      = 0;
    private static final int MMS_ALL_ID                   = 1;
    private static final int MMS_INBOX                    = 2;
    private static final int MMS_INBOX_ID                 = 3;
    private static final int MMS_SENT                     = 4;
    private static final int MMS_SENT_ID                  = 5;
    private static final int MMS_DRAFTS                   = 6;
    private static final int MMS_DRAFTS_ID                = 7;
    private static final int MMS_OUTBOX                   = 8;
    private static final int MMS_OUTBOX_ID                = 9;
    private static final int MMS_ALL_PART                 = 10;
    private static final int MMS_MSG_PART                 = 11;
    private static final int MMS_PART_ID                  = 12;
    private static final int MMS_MSG_ADDR                 = 13;
    private static final int MMS_SENDING_RATE             = 14;
    private static final int MMS_REPORT_STATUS            = 15;
    private static final int MMS_REPORT_REQUEST           = 16;
    private static final int MMS_DRM_STORAGE              = 17;
    private static final int MMS_DRM_STORAGE_ID           = 18;
    private static final int MMS_THREADS                  = 19;
    private static final int MMS_SCRAP_SPACE              = 20;

    private static final UriMatcher
            sURLMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURLMatcher.addURI("mms", null,         MMS_ALL);
        sURLMatcher.addURI("mms", "#",          MMS_ALL_ID);
        sURLMatcher.addURI("mms", "inbox",      MMS_INBOX);
        sURLMatcher.addURI("mms", "inbox/#",    MMS_INBOX_ID);
        sURLMatcher.addURI("mms", "sent",       MMS_SENT);
        sURLMatcher.addURI("mms", "sent/#",     MMS_SENT_ID);
        sURLMatcher.addURI("mms", "drafts",     MMS_DRAFTS);
        sURLMatcher.addURI("mms", "drafts/#",   MMS_DRAFTS_ID);
        sURLMatcher.addURI("mms", "outbox",     MMS_OUTBOX);
        sURLMatcher.addURI("mms", "outbox/#",   MMS_OUTBOX_ID);
        sURLMatcher.addURI("mms", "part",       MMS_ALL_PART);
        sURLMatcher.addURI("mms", "#/part",     MMS_MSG_PART);
        sURLMatcher.addURI("mms", "part/#",     MMS_PART_ID);
        sURLMatcher.addURI("mms", "#/addr",     MMS_MSG_ADDR);
        sURLMatcher.addURI("mms", "rate",       MMS_SENDING_RATE);
        sURLMatcher.addURI("mms", "report-status/#",  MMS_REPORT_STATUS);
        sURLMatcher.addURI("mms", "report-request/#", MMS_REPORT_REQUEST);
        sURLMatcher.addURI("mms", "drm",        MMS_DRM_STORAGE);
        sURLMatcher.addURI("mms", "drm/#",      MMS_DRM_STORAGE_ID);
        sURLMatcher.addURI("mms", "threads",    MMS_THREADS);
        sURLMatcher.addURI("mms", "scrapSpace", MMS_SCRAP_SPACE);
    }

    private SQLiteOpenHelper mOpenHelper;
    // Aurora yudingmin 2014-11-19 added for bug #9304 start
    private AuroraSimIdMatching mAuroraSimIdMatching;
    // Aurora yudingmin 2014-11-19 added for bug #9304 end

    private static String concatSelections(String selection1, String selection2) {
        if (TextUtils.isEmpty(selection1)) {
            return selection2;
        } else if (TextUtils.isEmpty(selection2)) {
            return selection1;
        } else {
            return selection1 + " AND " + selection2;
        }
    }
}

