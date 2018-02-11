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

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
// Aurora xuyong 2014-12-05 added for debug start
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
// Aurora xuyong 2014-12-05 added for debug end
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
// Aurora xuyong 2014-12-05 added for debug start
import android.provider.Settings;
// Aurora xuyong 2014-12-05 added for debug end
//import android.provider.Telephony;
import gionee.provider.GnTelephony;
//import android.provider.Telephony.Mms;
import gionee.provider.GnTelephony.Mms;
import android.provider.Telephony.MmsSms;
//import android.provider.Telephony.Sms;
import gionee.provider.GnTelephony.Sms;
//import android.provider.Telephony.Threads;
import gionee.provider.GnTelephony.Threads;
import android.provider.Telephony.Mms.Addr;
import android.provider.Telephony.Mms.Part;
import android.provider.Telephony.Mms.Rate;
//import android.provider.Telephony.MmsSms.PendingMessages;
import gionee.provider.GnTelephony.MmsSms.PendingMessages;
import android.text.TextUtils;
import android.util.Log;
//import com.mediatek.xlog.Xlog;







import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.PduHeaders;
import com.privacymanage.service.AuroraPrivacyUtils;
import com.android.providers.telephony.MmsSmsSyncProvider.Sync;
import com.android.providers.telephony.MmsSmsSyncProvider.SyncPart;
//Add for WAP Push
import com.aurora.featureoption.FeatureOption;

import gionee.provider.GnTelephony.WapPush;
//gionee gaoj 2012-3-27 added for CR00555790 start
// Aurora xuyong 2014-12-05 added for debug start
import android.os.Binder;
// Aurora xuyong 2014-12-05 added for debug end
import android.os.SystemProperties;
//gionee gaoj 2012-3-27 added for CR00555790 end

public class MmsSmsDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "MmsSmsDatabaseHelper";
    static final String TABLE_CELLBROADCAST = "cellbroadcast";
    // Aurora xuyong 2014-10-23 added for privacy feature start
    public static final boolean sHasPrivacyFeature = true;
    public static final boolean sHasRejectFeature = SystemProperties.get("ro.aurora.reject.support").equals("yes");
    // Aurora xuyong 2014-10-23 added for privacy feature end
    private static final String UPDATE_THREAD_READ_COUNT =  
                        "  UPDATE threads SET readcount = " +
                        "  (SELECT count(_id)FROM "+
                        "  (SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM sms "+
                        "    WHERE ((read=1) AND thread_id = new.thread_id AND (type != 3)) "+
                        "  UNION SELECT DISTINCT date * 1000 AS normalized_date, pdu._id, read "+
                        "  FROM pdu LEFT JOIN pending_msgs ON pdu._id = pending_msgs.msg_id "+
                        "  WHERE ((read=1) AND thread_id = new.thread_id AND msg_box != 3 AND (msg_box != 3 "+
                        "        AND (m_type = 128 OR m_type = 132 OR m_type = 130)))" +
                        "   UNION SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM cellbroadcast "+
                        "   WHERE ((read=1) AND thread_id = new.thread_id) ORDER BY normalized_date ASC))  "+                      
                        "  WHERE threads._id = new.thread_id; ";

    private static final String SMS_UPDATE_THREAD_READ_BODY =
                        "  UPDATE threads SET read = " +
                        "    CASE (SELECT COUNT(*)" +
                        "          FROM sms" +
                        "          WHERE " + Sms.READ + " = 0" +
                        "            AND " + Sms.THREAD_ID + " = threads._id)" +
                        "      WHEN 0 THEN 1" +
                        "      ELSE 0" +
                        "    END" +
                        "  WHERE threads._id = new." + Sms.THREAD_ID + "; ";

    private static final String UPDATE_THREAD_COUNT_ON_NEW =
                        "  UPDATE threads SET message_count = " +
                        "     (SELECT COUNT(sms._id) FROM sms LEFT JOIN threads " +
                        "      ON threads._id = " + Sms.THREAD_ID +
                        "      WHERE " + Sms.THREAD_ID + " = new.thread_id" +
                        "        AND sms." + Sms.TYPE + " != 3) + " +
                        "     (SELECT COUNT(pdu._id) FROM pdu LEFT JOIN threads " +
                        "      ON threads._id = " + Mms.THREAD_ID +
                        "      WHERE " + Mms.THREAD_ID + " = new.thread_id" +
                        "        AND (m_type=132 OR m_type=130 OR m_type=128)" +
                        "        AND " + Mms.MESSAGE_BOX + " != 3) " +
                        "  WHERE threads._id = new.thread_id; ";

    private static final String UPDATE_THREAD_COUNT_ON_OLD =
                        "  UPDATE threads SET message_count = " +
                        "     (SELECT COUNT(sms._id) FROM sms LEFT JOIN threads " +
                        "      ON threads._id = " + Sms.THREAD_ID +
                        "      WHERE " + Sms.THREAD_ID + " = old.thread_id" +
                        "        AND sms." + Sms.TYPE + " != 3) + " +
                        "     (SELECT COUNT(pdu._id) FROM pdu LEFT JOIN threads " +
                        "      ON threads._id = " + Mms.THREAD_ID +
                        "      WHERE " + Mms.THREAD_ID + " = old.thread_id" +
                        "        AND (m_type=132 OR m_type=130 OR m_type=128)" +
                        "        AND " + Mms.MESSAGE_BOX + " != 3) " +
                        "  WHERE threads._id = old.thread_id; ";

    private static final String SMS_UPDATE_THREAD_DATE_SNIPPET_COUNT_ON_UPDATE =
                        "BEGIN" +
                        "  UPDATE threads SET" +
                        "    date = new." + Sms.DATE + ", " +
                        "    snippet = new." + Sms.BODY + ", " +
                        //gionee gaoj 2012-3-27 added for CR00555790 start
                        GnTelephony.GN_SIM_ID + " = new." + Sms.SIM_ID +", " +
                        //gionee gaoj 2012-3-27 added for CR00555790 end
                        "    snippet_cs = 0" +
                        "  WHERE threads._id = new." + Sms.THREAD_ID + "; " +
                        UPDATE_THREAD_COUNT_ON_NEW +
                        SMS_UPDATE_THREAD_READ_BODY +
                        UPDATE_THREAD_READ_COUNT+
                        "END;";//

    private static final String PDU_UPDATE_THREAD_CONSTRAINTS =
                        "  WHEN new." + Mms.MESSAGE_TYPE + "=" +
                        PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF +
                        "    OR new." + Mms.MESSAGE_TYPE + "=" +
                        PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND +
                        "    OR new." + Mms.MESSAGE_TYPE + "=" +
                        PduHeaders.MESSAGE_TYPE_SEND_REQ + " ";

    // When looking in the pdu table for unread messages, only count messages that
    // are displayed to the user. The constants are defined in PduHeaders and could be used
    // here, but the string "(m_type=132 OR m_type=130 OR m_type=128)" is used throughout this
    // file and so it is used here to be consistent.
    //     m_type=128   = MESSAGE_TYPE_SEND_REQ
    //     m_type=130   = MESSAGE_TYPE_NOTIFICATION_IND
    //     m_type=132   = MESSAGE_TYPE_RETRIEVE_CONF
    private static final String PDU_UPDATE_THREAD_READ_BODY =
                        "  UPDATE threads SET read = " +
                        "    CASE (SELECT COUNT(*)" +
                        "          FROM " + MmsProvider.TABLE_PDU +
                        "          WHERE " + Mms.READ + " = 0" +
                        "            AND " + Mms.THREAD_ID + " = threads._id " +
                        "            AND (m_type=132 OR m_type=130 OR m_type=128)) " +
                        "      WHEN 0 THEN 1" +
                        "      ELSE 0" +
                        "    END" +
                        "  WHERE threads._id = new." + Mms.THREAD_ID + "; ";

        //gionee gaoj 2012-3-27 added for CR00555790 start
    private static final String PDU_UPDATE_THREAD_SIM_ID_ON_UPDATE=               
                       "BEGIN" +
                       "  UPDATE threads SET " +
                       GnTelephony.GN_SIM_ID + " = new." + Mms.SIM_ID + 
                       "  WHERE threads._id = new." + Mms.THREAD_ID + "; " +
                       "END;";
        //gionee gaoj 2012-3-27 added for CR00555790 end

    private static final String PDU_UPDATE_THREAD_DATE_SNIPPET_COUNT_ON_UPDATE =
                        "BEGIN" +
                        "  UPDATE threads SET" +
                        "    date = (strftime('%s','now') * 1000), " +
                        "    snippet = new." + Mms.SUBJECT + ", " +
                         //gionee gaoj 2012-3-27 added for CR00555790 start
                         GnTelephony.GN_SIM_ID + " = new." + Mms.SIM_ID +", " +
                         //gionee gaoj 2012-3-27 added for CR00555790 end
                        "    snippet_cs = new." + Mms.SUBJECT_CHARSET +
                        "  WHERE (new.m_type=132 OR new.m_type=130 OR new.m_type=128) AND threads._id = new." + Mms.THREAD_ID + "; " +
                        UPDATE_THREAD_COUNT_ON_NEW +
                        PDU_UPDATE_THREAD_READ_BODY +
                        UPDATE_THREAD_READ_COUNT+
                        "END;";//

    private static final String UPDATE_THREAD_SNIPPET_SNIPPET_CS_ON_DELETE =
                        "  UPDATE threads SET snippet = " +
                        "   (SELECT snippet FROM" +
                        "     (SELECT date * 1000 AS date, sub AS snippet, thread_id FROM pdu WHERE m_type=132 OR m_type=130 OR m_type=128" +
                        "      UNION SELECT date, body AS snippet, thread_id FROM sms)" +
                        "    WHERE thread_id = OLD.thread_id ORDER BY date DESC LIMIT 1) " +
                        "  WHERE threads._id = OLD.thread_id; " +
                        //gionee gaoj 2012-3-27 added for CR00555790 start
                        "  UPDATE threads SET " + GnTelephony.GN_SIM_ID +" = " +
                        "  (SELECT "+GnTelephony.GN_SIM_ID +" FROM" + 
                        "    (SELECT date * 1000 AS date, " + GnTelephony.GN_SIM_ID + ", thread_id FROM pdu" +
                        "    WHERE m_type=132 OR m_type=130 OR m_type=128" +
                        "      UNION SELECT date, " + GnTelephony.GN_SIM_ID + ", thread_id FROM sms)" +
                        "    WHERE thread_id = OLD.thread_id ORDER BY date DESC LIMIT 1) " +
                        "  WHERE threads._id = OLD.thread_id; " +
                        //gionee gaoj 2012-3-27 added for CR00555790 end
                        "  UPDATE threads SET snippet_cs = " +
                        "   (SELECT snippet_cs FROM" +
                        "     (SELECT date * 1000 AS date, sub_cs AS snippet_cs, thread_id FROM pdu WHERE m_type=132 OR m_type=130 OR m_type=128" +
                        "      UNION SELECT date, 0 AS snippet_cs, thread_id FROM sms)" +
                        "    WHERE thread_id = OLD.thread_id ORDER BY date DESC LIMIT 1) " +
                        "  WHERE threads._id = OLD.thread_id; ";


    // When a part is inserted, if it is not text/plain or application/smil
    // (which both can exist with text-only MMSes), then there is an attachment.
    // Set has_attachment=1 in the threads table for the thread in question.
    private static final String PART_UPDATE_THREADS_ON_INSERT_TRIGGER =
                        "CREATE TRIGGER update_threads_on_insert_part " +
                        " AFTER INSERT ON part " +
                        " WHEN new.ct != 'text/plain' AND new.ct != 'application/smil' " +
                        " BEGIN " +
                        "  UPDATE threads SET has_attachment=1 WHERE _id IN " +
                        "   (SELECT pdu.thread_id FROM part JOIN pdu ON pdu._id=part.mid " +
                        "     WHERE part._id=new._id LIMIT 1); " +
                        " END";

    // When the 'mid' column in the part table is updated, we need to run the trigger to update
    // the threads table's has_attachment column, if the part is an attachment.
    private static final String PART_UPDATE_THREADS_ON_UPDATE_TRIGGER =
                        "CREATE TRIGGER update_threads_on_update_part " +
                        " AFTER UPDATE of " + Part.MSG_ID + " ON part " +
                        " WHEN new.ct != 'text/plain' AND new.ct != 'application/smil' " +
                        " BEGIN " +
                        "  UPDATE threads SET has_attachment=1 WHERE _id IN " +
                        "   (SELECT pdu.thread_id FROM part JOIN pdu ON pdu._id=part.mid " +
                        "     WHERE part._id=new._id LIMIT 1); " +
                        " END";


    // When a part is deleted (with the same non-text/SMIL constraint as when
    // we set has_attachment), update the threads table for all threads.
    // Unfortunately we cannot update only the thread that the part was
    // attached to, as it is possible that the part has been orphaned and
    // the message it was attached to is already gone.
    private static final String PART_UPDATE_THREADS_ON_DELETE_TRIGGER =
                        "CREATE TRIGGER update_threads_on_delete_part " +
                        " AFTER DELETE ON part " +
                        " WHEN old.ct != 'text/plain' AND old.ct != 'application/smil' " +
                        " BEGIN " +
                        "  UPDATE threads SET has_attachment = " +
                        "   CASE " +
                        "    (SELECT COUNT(*) FROM part JOIN pdu " +
                        "     WHERE pdu.thread_id = threads._id " +
                        "     AND part.ct != 'text/plain' AND part.ct != 'application/smil' " +
                        "     AND part.mid = pdu._id)" +
                        "   WHEN 0 THEN 0 " +
                        "   ELSE 1 " +
                        "   END; " +
                        " END";

    // When the 'thread_id' column in the pdu table is updated, we need to run the trigger to update
    // the threads table's has_attachment column, if the message has an attachment in 'part' table
    private static final String PDU_UPDATE_THREADS_ON_UPDATE_TRIGGER =
                        "CREATE TRIGGER update_threads_on_update_pdu " +
                        " AFTER UPDATE of thread_id ON pdu " +
                        " BEGIN " +
                        "  UPDATE threads SET has_attachment=1 WHERE _id IN " +
                        "   (SELECT pdu.thread_id FROM part JOIN pdu " +
                        "     WHERE part.ct != 'text/plain' AND part.ct != 'application/smil' " +
                        "     AND part.mid = pdu._id);" +
                        " END";
    // CB:update date && snippet && count of threads table.
    
    private static final String CB_UPDATE_THREAD_READ_BODY = "  UPDATE threads SET read = "
        + "    CASE (SELECT COUNT(*)"
        + "          FROM cellbroadcast"
        + "          WHERE "
        + GnTelephony.CbSms.READ
        + " = 0"
        + "            AND "
        + GnTelephony.CbSms.THREAD_ID
        + " = threads._id)"
        + "      WHEN 0 THEN 1"
        + "      ELSE 0"
        + "    END"
        + "  WHERE threads._id = new."
        + GnTelephony.CbSms.THREAD_ID + "; ";
    
    private static final String CB_UPDATE_THREAD_COUNT_ON_NEW = "  UPDATE threads SET message_count = "
        + "     (SELECT COUNT(cellbroadcast._id) FROM cellbroadcast LEFT JOIN threads "
        + "      ON threads._id = "
        + GnTelephony.CbSms.THREAD_ID
        + "      WHERE "
        + GnTelephony.CbSms.THREAD_ID
        + " = new.thread_id )" + "  WHERE threads._id = new.thread_id; ";
    
    private static final String CB_UPDATE_THREAD_DATE_SNIPPET_COUNT_ON_UPDATE = "BEGIN"
            + "  UPDATE threads SET"
            + "    date = (strftime('%s','now') * 1000), "
            + "    type= "
            + Threads.CELL_BROADCAST_THREAD
            + ", "
            + "    snippet = new."
            + GnTelephony.CbSms.BODY
            + " "
            + "  WHERE threads._id = new."
            + GnTelephony.CbSms.THREAD_ID
            + "; "
            + CB_UPDATE_THREAD_COUNT_ON_NEW +UPDATE_THREAD_READ_COUNT+ CB_UPDATE_THREAD_READ_BODY + "END;";//
    
    private static final String CB_UPDATE_THREAD_COUNT_ON_OLD =
        "  UPDATE threads SET message_count = " +
        "     (SELECT COUNT(cellbroadcast._id) FROM cellbroadcast LEFT JOIN threads " +
        "      ON threads._id = " + GnTelephony.CbSms.THREAD_ID +
        "      WHERE " + GnTelephony.CbSms.THREAD_ID + " = old.thread_id)" +
        "  WHERE threads._id = old.thread_id; ";
    
    private static final String CB_UPDATE_THREAD_SNIPPET_ON_DELETE =
        "  UPDATE threads SET snippet = " +
        "   (SELECT body FROM" +
        "     (SELECT date, body, thread_id FROM cellbroadcast)" +
        "    WHERE thread_id = OLD.thread_id ORDER BY date DESC LIMIT 1) " +
        "  WHERE threads._id = OLD.thread_id; ";
    
    private static final String CB_UPDATE_THREAD_DATE_ON_DELETE =
        "  UPDATE threads SET date = " +
        "   (SELECT date FROM" +
        "     (SELECT date, body, thread_id FROM cellbroadcast)" +
        "    WHERE thread_id = OLD.thread_id ORDER BY date DESC LIMIT 1) " +
        "  WHERE threads._id = OLD.thread_id; ";
    
        //gionee gaoj 2012-3-27 added for CR00555790 start
   //gionee guoyangxu 20120606 modified for CR00622143 start
   public static final boolean mIsMsgBox = SystemProperties.get("ro.gn.msgbox.prop").equals("yes");
   //gionee guoyangxu 20120606 modified for CR00622143 end
   private static final String WAPPUSH_UPDATE_THREAD_COUNT_ON_NEW =
                        "  UPDATE threads SET message_count = " +
                        "     (SELECT COUNT(wappush._id) FROM wappush LEFT JOIN threads " +
                        "      ON threads._id = " + WapPush.THREAD_ID +
                        "      WHERE " + WapPush.THREAD_ID + " = new.thread_id)" +
                        "  WHERE threads._id = new.thread_id; ";

   private static final String WAPPUSH_UPDATE_THREAD_DATE_SNIPPET_COUNT_ON_UPDATE =
                        "BEGIN" +
                        "  UPDATE threads SET" +
                        "    date = (strftime('%s','now') * 1000), " +
                        "    snippet = new." + WapPush.TEXT + ", " +
                        GnTelephony.GN_SIM_ID + " = new." + WapPush.SIM_ID +", " +
                        "    snippet_cs = 0" +
                        "  WHERE threads._id = new." + WapPush.THREAD_ID + "; " +
                        WAPPUSH_UPDATE_THREAD_COUNT_ON_NEW +
                        "END;";
        //gionee gaoj 2012-3-27 added for CR00555790 end
    
    private static MmsSmsDatabaseHelper sInstance = null;
    private static boolean sTriedAutoIncrement = false;
    private static boolean sFakeLowStorageTest = false;     // for testing only

    static final String DATABASE_NAME = "mmssms.db";
     // Aurora liugj 2014-01-06 modified for bath-delete optimize start
    // Aurora xuyong 2014-04-09 modified for aurora's new feature start
    // Auarora xuyong 2014-04-09 modified for Reject feature start
    // Auarora xuyong 2014-06-18 modified for Reject feature start
    // Auarora xuyong 2014-06-19 modified for database upgrade start
    // Aurora xuyong 2014-07-03 modified for reject feature start
    // Aurora xuyong 2014-07-04 modified for aurora's new feature start
    // Aurora xuyong 2014-07-05 modified for reject feature start
    // Aurora xuyong 2014-07-09 modified for reject feature start
    // Aurora xuyong 2014-07-19 modified for bug #6656 start
    // Aurora xuyong 2014-07-21 modified for reject feature start
    // Aurora xuyong 2014-07-23 modified for database upgrade start
    // Aurora xuyong 2014-07-29 modified for database upgrade start
    // Aurora xuyong 2014-08-05 modified for reject start
    //Aurora xuyong 2014-09-02 modified for whitelist feature start
    // Aurora xuyong 2014-10-23 modified for privacy feature start
    //Aurora yudingmin 2014-11-03 modified for sync feature start
    static final int DATABASE_VERSION = 700005;
    //Aurora yudingmin 2014-11-03 modified for sync feature end
    // Aurora xuyong 2014-10-23 modified for privacy feature end
    //Aurora xuyong 2014-09-02 modified for whitelist feature end
    // Aurora xuyong 2014-08-05 modified for reject end
    // Aurora xuyong 2014-07-29 modified for database upgrade end
    // Aurora xuyong 2014-07-23 modified for database upgrade end
    // Aurora xuyong 2014-07-21 modified for reject feature end
    // Aurora xuyong 2014-07-19 modified for bug #6656 end
    // Aurora xuyong 2014-07-09 modified for reject feature end
    // Aurora xuyong 2014-07-05 modified for reject feature end
    // Aurora xuyong 2014-07-04 modified for aurora's new feature end
    // Aurora xuyong 2014-07-03 modified for reject feature end
    // Auarora xuyong 2014-06-19 modified for database upgrade end
    // Auarora xuyong 2014-06-18 modified for Reject feature end
    // Auarora xuyong 2014-04-09 modified for Reject feature end
    // Aurora xuyong 2014-04-09 modified for aurora's new feature end
     // Aurora liugj 2014-01-06 modified for bath-delete optimize end
    private final Context mContext;
    private LowStorageMonitor mLowStorageMonitor;


    private MmsSmsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        mContext = context;
        // Aurora xuyong 2014-10-23 added for privacy feature start
        if (sHasPrivacyFeature) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    initPrivacyStatus();
                }
                
            }).start();
        }
        // Aurora xuyong 2014-10-23 added for privacy feature end
        // Aurora xuyong 2014-11-28 added for bug #10093 start
        this.getWritableDatabase().enableWriteAheadLogging();
        // Aurora xuyong 2014-11-28 added for bug #10093 end
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private void initPrivacyStatus() {
        AuroraPrivacyUtils.bindService(mContext);
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end

    /**
     * Return a singleton helper for the combined MMS and SMS
     * database.
     */
    /* package */ static synchronized MmsSmsDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MmsSmsDatabaseHelper(context);
        }
        return sInstance;
    }
    
    // Aurora xuyong 2014-10-23 added for privacy feature start
    public static void updatePrivacyNum(SQLiteDatabase db, long threadId) {
        long privacy = 0;
        Cursor result1 = db.query("threads", new String[] { "is_privacy" }, "_id = ?", new String[] { "" + threadId }, null, null, null);
        if (result1 != null && result1.moveToFirst()) {
            privacy = result1.getLong(0);
        }
        if (result1 != null && !result1.isClosed()) {
            result1.close();
        }
        Cursor result2 = db.query("threads", new String[] { "is_privacy" }, "is_privacy = ?", new String[] { "" + privacy }, null, null, null);
        if (result2 != null && result2.moveToFirst()) {
            AuroraPrivacyUtils.setPrivacy(result2.getCount(), privacy);
        }
        if (result2 != null && !result2.isClosed()) {
            result2.close();
        }
    }
    
    public static void resetPrivacyNum() {
        AuroraPrivacyUtils.resetPrivacyNumOfAllAccount();
    }
    // Aurora xuyong 2014-11-11 added for bug #9742 start
    public static void addPrivacyNum(SQLiteDatabase db, long privacy) {
        int count = 0;
        Cursor result2 = db.query("threads", new String[] { "is_privacy" }, "is_privacy = ? AND NOT ((snippet is null) AND (snippet_cs not null AND snippet_cs = 0) AND (has_attachment = 0) AND (message_count = 0))", new String[] { "" + privacy }, null, null, null);
        if (result2 != null && result2.moveToFirst()) {
            count = result2.getCount();
        }
        ++count;
        AuroraPrivacyUtils.setPrivacy(count, privacy);
    }
    // Aurora xuyong 2014-11-11 added for bug #9742 end
    public static void updateAllPrivacyNum(final SQLiteDatabase db) {
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                db.beginTransaction();
                // Aurora xuyong 2015-07-06 added for bug #14054 start
                Cursor result1 = null;
                Cursor result2 = null;
                // Aurora xuyong 2015-07-06 added for bug #14054 end
                try {
                	// Aurora xuyong 2015-07-06 modified for bug #14054 start
                    result1 = db.rawQuery("SELECT DISTINCT is_privacy FROM threads WHERE is_privacy > 0;", null);
                    // Aurora xuyong 2015-07-06 modified for bug #14054 end
                // Aurora xuyong 2014-11-28 modified for bug#10175 start
                    //if (result1 == null || result1.getCount() <= 0) {
                        resetPrivacyNum();
                    // Aurora xuyong 2014-11-10 modified for privacy feature start
                        //return;
                    // Aurora xuyong 2014-11-10 modified for privacy feature end
                    //}
                // Aurora xuyong 2014-11-28 modified for bug#10175 end
                    if (result1 != null) {
                        while (result1.moveToNext()) {
                            long privacy = result1.getLong(0);
                            // Aurora xuyong 2014-11-10 modified for privacy feature start
                            // Aurora xuyong 2015-07-06 modified for bug #14054 start
                            result2 = db.query("threads", new String[] { "is_privacy" }, "is_privacy = ? AND NOT ((snippet is null) AND (snippet_cs not null AND snippet_cs = 0) AND (has_attachment = 0) AND (message_count = 0))", new String[] { "" + privacy }, null, null, null);
                            // Aurora xuyong 2015-07-06 modified for bug #14054 end
                            // Aurora xuyong 2014-11-10 modified for privacy feature end
                            if (result2 != null && result2.moveToFirst()) {
                                AuroraPrivacyUtils.setPrivacy(result2.getCount(), privacy);
                            }
                            if (result2 != null && !result2.isClosed()) {
                                result2.close();
                            }
                        }
                    }
                    if (result1 != null && !result1.isClosed()) {
                        result1.close();
                    } 
                    db.setTransactionSuccessful();
                } catch (SQLiteException e) {
                    e.printStackTrace();
                } finally {
                	// Aurora xuyong 2015-07-06 added for bug #14054 start
                	if (result1 != null && !result1.isClosed()) {
                        result1.close();
                    }
                	if (result2 != null && !result2.isClosed()) {
                        result2.close();
                    }
                	// Aurora xuyong 2015-07-06 added for bug #14054 end
                    db.endTransaction();
                }
            }
            
        }).start();
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    // Aurora xuyong 2014-12-05 added for debug start
    private static int getUidFromPackgeName(Context context, String packgeName){
        int uid = -1;
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packgeName, PackageManager.GET_ACTIVITIES);
            uid = ai.uid;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return uid;
    }

    private static String getDefaultSmsPackage(Context context) {
        String defaultApplication = Settings.Secure.getString(context.getContentResolver(), "sms_default_application");
        return defaultApplication;
    }
    
    public static boolean forbidOP(Context context) {
        int callingUid = Binder.getCallingUid();
        int defaultSmsUid = getUidFromPackgeName(context, getDefaultSmsPackage(context));
        int auroraSmsUid = getUidFromPackgeName(context, "com.android.mms");
        if (callingUid == defaultSmsUid || callingUid == auroraSmsUid) {
            return false;
        } else {
            return true;
        }
    }
    // Aurora xuyong 2014-12-05 added for debug end
    public static void updateThread(SQLiteDatabase db, long thread_id) {
        if (thread_id < 0) {
            updateAllThreads(db, null, null);
            return;
        }

        //if it's a wappush thread, it doesn't need to be updated here;
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            Cursor pushCursor = db.rawQuery("select * from threads where type=" + Threads.WAPPUSH_THREAD + " AND _id=" + thread_id, null);
            if(pushCursor!=null){
                try {
                    if(pushCursor.getCount()!=0){
                        return;
                    }
                } finally {
                    pushCursor.close();
                }
            }
        }
        
        // Delete the row for this thread in the threads table if
        // there are no more messages attached to it in either
        // the sms or pdu tables.
        int rows = 0;
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            rows = db.delete("threads",
                  "_id = ? AND type <> ? AND _id NOT IN" +
                  "          (SELECT thread_id FROM sms " +
                  "           UNION SELECT thread_id FROM pdu)",
                  new String[] { String.valueOf(thread_id), String.valueOf(Threads.WAPPUSH_THREAD) });
        }else{
            rows = db.delete("threads",
                  "_id = ? AND _id NOT IN" +
                  "          (SELECT thread_id FROM sms " +
                  "           UNION SELECT thread_id FROM pdu)",
                  new String[] { String.valueOf(thread_id) });
        }
        if (rows > 0) {
            // If this deleted a row, let's remove orphaned canonical_addresses and get outta here
            //gionee gaoj 2012-4-20 added for CR00555790 start
            //removeOrphanedAddresses(db);
            //gionee gaoj 2012-4-20 added for CR00555790 end
            return;
        }
        // Update the message count in the threads table as the sum
        // of all messages in both the sms and pdu tables.
        db.execSQL(
            "  UPDATE threads SET message_count = " +
            "     (SELECT COUNT(sms._id) FROM sms LEFT JOIN threads " +
            "      ON threads._id = " + Sms.THREAD_ID +
            "      WHERE " + Sms.THREAD_ID + " = " + thread_id +
            // Aurora xuyong 2014-07-03 modified for reject feature start
            "        AND sms." + Sms.TYPE + " != 3 AND sms.reject = 0) + " +
            // Aurora xuyong 2014-07-03 modified for reject feature end
            "     (SELECT COUNT(pdu._id) FROM pdu LEFT JOIN threads " +
            "      ON threads._id = " + Mms.THREAD_ID +
            "      WHERE " + Mms.THREAD_ID + " = " + thread_id +
            "        AND (m_type=132 OR m_type=130 OR m_type=128)" +
            // Aurora xuyong 2014-07-03 modified for reject feature start
            "        AND " + Mms.MESSAGE_BOX + " != 3 AND pdu.reject = 0) " +
            // Aurora xuyong 2014-07-03 modified for reject feature end
            "  WHERE threads._id = " + thread_id + ";");

        // Update the date and the snippet (and its character set) in
        // the threads table to be that of the most recent message in
        // the thread.
        db.execSQL(
            "  UPDATE threads" +
            "  SET" +
            "  date =" +
            "    (SELECT date FROM" +
            // Aurora xuyong 2014-07-03 modified for reject feature start
            "        (SELECT date * 1000 AS date, thread_id FROM pdu WHERE pdu.reject = 0" +
            "         UNION SELECT date, thread_id FROM sms WHERE sms.reject = 0)" +
            // Aurora xuyong 2014-07-03 modified for reject feature end
            "     WHERE thread_id = " + thread_id + " ORDER BY date DESC LIMIT 1)," +
            "  snippet =" +
            "    (SELECT snippet FROM" +
            // Aurora xuyong 2014-07-03 modified for reject feature start
            "        (SELECT date * 1000 AS date, sub AS snippet, thread_id FROM pdu WHERE (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0" +
            "         UNION SELECT date, body AS snippet, thread_id FROM sms WHERE sms.reject = 0)" +
            // Aurora xuyong 2014-07-03 modified for reject feature end
            "     WHERE thread_id = " + thread_id + " ORDER BY date DESC LIMIT 1), " +
            //gionee gaoj 2012-3-27 added for CR00555790 start
            GnTelephony.GN_SIM_ID + " =" +
            "    (SELECT " + GnTelephony.GN_SIM_ID + " FROM" +
            "        (SELECT date * 1000 AS date, " + GnTelephony.GN_SIM_ID + ", thread_id FROM pdu" +
            // Aurora xuyong 2014-07-03 modified for reject feature start
            "     WHERE (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0" + 
            "         UNION SELECT date, " + GnTelephony.GN_SIM_ID + ", thread_id FROM sms WHERE sms.reject = 0)" +
            // Aurora xuyong 2014-07-03 modified for reject feature end
            "     WHERE thread_id = " + thread_id + " ORDER BY date DESC LIMIT 1)," +
              //gionee gaoj 2012-3-27 added for CR00555790 end
            "  snippet_cs =" +
            "    (SELECT snippet_cs FROM" +
            // Aurora xuyong 2014-07-03 modified for reject feature start
            "        (SELECT date * 1000 AS date, sub_cs AS snippet_cs, thread_id FROM pdu WHERE (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0" +
            "         UNION SELECT date, 0 AS snippet_cs, thread_id FROM sms WHERE sms.reject = 0)" +
            // Aurora xuyong 2014-07-03 modified for reject feature end
            "     WHERE thread_id = " + thread_id + " ORDER BY date DESC LIMIT 1)" +
            "  WHERE threads._id = " + thread_id + ";");

        // Update the error column of the thread to indicate if there
        // are any messages in it that have failed to send.
        // First check to see if there are any messages with errors in this thread.
        String query = "SELECT thread_id FROM sms WHERE type=" +
            GnTelephony.Sms.MESSAGE_TYPE_FAILED +
            " AND thread_id = " + thread_id +
                                " LIMIT 1";
        int setError = 0;
        Cursor c = db.rawQuery(query, null);
        if (c != null) {
            try {
                setError = c.getCount();    // Because of the LIMIT 1, count will be 1 or 0.
                if(setError == 0){
                    // select all _id from pdu of the thread
                    String mms_query = "SELECT _id FROM pdu WHERE thread_id = " + thread_id +
                        " AND m_type = " + PduHeaders.MESSAGE_TYPE_SEND_REQ;
                    Cursor c_mms = db.rawQuery(mms_query, null);
                    if ( c_mms != null) {
                        try {
                            if (c_mms.moveToFirst()) {
                                int count = c_mms.getCount();
                                Cursor c_pending = null;
                                // for all the _id, check the err_type in pending_msgs
                                for (int i = 0; i < count; ++i) {
                                    int msg_id = c_mms.getInt(0);
                                    String pending_query = "SELECT err_type FROM pending_msgs WHERE err_type >= " + MmsSms.ERR_TYPE_GENERIC_PERMANENT + " AND msg_id = " + msg_id ;
                                    c_pending = db.rawQuery(pending_query, null);
                                    if (c_pending != null) {
                                        try {
                                            if (c_pending.getCount() != 0) {
                                                setError = 1;
                                                break;
                                             }
                                        } finally {
                                           c_pending.close();
                                        }
                                    }
                                    c_mms.moveToNext();
                                }
                            }

                        }finally {
                            c_mms.close();
                        }
                    }
                }
            } finally {
                c.close();
            }
        }
        // What's the current state of the error flag in the threads table?
        String errorQuery = "SELECT error FROM threads WHERE _id = " + thread_id;
        c = db.rawQuery(errorQuery, null);
        if (c != null) {
            try {
                if (c.moveToNext()) {
                    int curError = c.getInt(0);
                    if (curError != setError) {
                        // The current thread error column differs, update it.
                        db.execSQL("UPDATE threads SET error=" + setError +
                                " WHERE _id = " + thread_id);
                    }
                }
            } finally {
                c.close();
            }
        }
        // Aurora xuyong 2014-10-23 added for privacy feature start
        updatePrivacyNum(db, thread_id);
        // Aurora xuyong 2014-10-23 added for privacy feature end
    }

    public static void updateAllThreads(SQLiteDatabase db, String where, String[] whereArgs) {
        if (where == null) {
            where = "";
        } else {
            where = "WHERE (" + where + ")";
        }
        String query = "SELECT _id FROM threads WHERE _id IN " +
                       "(SELECT DISTINCT thread_id FROM sms " + where + ")";
        Cursor c = db.rawQuery(query, whereArgs);
        if (c != null) {
            try {
                while (c.moveToNext()) {
                    updateThread(db, c.getInt(0));
                }
            } finally {
                c.close();
            }
        }
        // TODO: there are several db operations in this function. Lets wrap them in a
        // transaction to make it faster.
        // remove orphaned threads
        
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            db.delete("threads",
                "_id NOT IN (SELECT DISTINCT thread_id FROM sms " +
                "UNION SELECT DISTINCT thread_id FROM pdu)" + " AND " + Threads.TYPE + " <> " + Threads.WAPPUSH_THREAD, null);
        }else{
        db.delete("threads",
                "_id NOT IN (SELECT DISTINCT thread_id FROM sms " +
                "UNION SELECT DISTINCT thread_id FROM pdu)", null);
        }

        // remove orphaned canonical_addresses
        //gionee gaoj 2012-4-20 added for CR00555790 start
        //removeOrphanedAddresses(db);
        //gionee gaoj 2012-4-20 added for CR00555790 end
    }

    private static void removeOrphanedAddresses(SQLiteDatabase db) {
        final Cursor c = db.rawQuery("SELECT DISTINCT recipient_ids FROM threads", null);
        final StringBuilder recipientIds = new StringBuilder();
        final String separator = ",";
        try {
            if (c != null && c.moveToFirst()) {
                do {
                    String id = c.getString(0);
                    if (!TextUtils.isEmpty(id)) {
                        id = id.trim();
                        if (!TextUtils.isEmpty(id)) {
                            recipientIds.append(id.replaceAll(" ", separator));
                            recipientIds.append(separator);
                        }
                    }
                } while (c.moveToNext());
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        String ids = recipientIds.toString();
        if (!TextUtils.isEmpty(ids) && ids.endsWith(separator)) {
            ids = ids.substring(0, ids.lastIndexOf(separator));
        } 
        if(!TextUtils.isEmpty(ids) && ids.startsWith(separator)){
            ids = ids.substring(1, ids.length());
        }
//        Xlog.d(TAG, "recipient ids = " + ids);
        db.delete("canonical_addresses",
                //"_id NOT IN (SELECT DISTINCT recipient_ids FROM threads)", null);
                "_id NOT IN (" + ids + ")", null);
    }

    public static int deleteOneSms(SQLiteDatabase db, int message_id) {
        int thread_id = -1;
        // Find the thread ID that the specified SMS belongs to.
        Cursor c = db.query("sms", new String[] { "thread_id" },
                            "_id=" + message_id, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                thread_id = c.getInt(0);
            }
            c.close();
        }

        // Delete the specified message.
        int rows = db.delete("sms", "_id=" + message_id, null);
        if (thread_id > 0) {
            // Update its thread.
            updateThread(db, thread_id);
        }
        return rows;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            createWapPushTables(db);
        }

        createMmsTables(db);
        createSmsTables(db);
        createCommonTables(db);
        createCBTables(db);
        createCommonTriggers(db);
        createMmsTriggers(db);
        createWordsTables(db);
        createIndices(db);
        createQuickText(db);
        //Aurora xuyong 2014-09-02 added for whitelist feature start
        initWhiteListTable(db);
        //Aurora xuyong 2014-09-02 added for whitelist feature end
        //Aurora yudingmin 2014-11-03 added for sync feature start
        createMmsSmsSync(db);
        createMmsSmsSyncView(db);
        //Aurora yudingmin 2014-11-03 added for sync feature end
        // Aurora xuyong 2014-10-23 added for privacy feature start
        initCurrentNumberIdTable(db);
        // Aurora xuyong 2014-10-23 added for privacy feature end
    }

    // When upgrading the database we need to populate the words
    // table with the rows out of sms and part.
    private void populateWordsTable(SQLiteDatabase db) {
        final String TABLE_WORDS = "words";
        {
            Cursor smsRows = db.query(
                    "sms",
                    new String[] { Sms._ID, Sms.BODY },
                    null,
                    null,
                    null,
                    null,
                    null);
            try {
                if (smsRows != null) {
                    smsRows.moveToPosition(-1);
                    ContentValues cv = new ContentValues();
                    while (smsRows.moveToNext()) {
                        cv.clear();

                        long id = smsRows.getLong(0);        // 0 for Sms._ID
                        String body = smsRows.getString(1);  // 1 for Sms.BODY

                        cv.put(GnTelephony.MmsSms.WordsTable.ID, id);
                        cv.put(GnTelephony.MmsSms.WordsTable.INDEXED_TEXT, body);
                        cv.put(GnTelephony.MmsSms.WordsTable.SOURCE_ROW_ID, id);
                        cv.put(GnTelephony.MmsSms.WordsTable.TABLE_ID, 1);
                        db.insert(TABLE_WORDS, GnTelephony.MmsSms.WordsTable.INDEXED_TEXT, cv);
                    }
                }
            } finally {
                if (smsRows != null) {
                    smsRows.close();
                }
            }
        }

        {
            Cursor mmsRows = db.query(
                    "part",
                    new String[] { Part._ID, Part.TEXT },
                    "ct = 'text/plain'",
                    null,
                    null,
                    null,
                    null);
            try {
                if (mmsRows != null) {
                    mmsRows.moveToPosition(-1);
                    ContentValues cv = new ContentValues();
                    while (mmsRows.moveToNext()) {
                        cv.clear();

                        long id = mmsRows.getLong(0);         // 0 for Part._ID
                        String body = mmsRows.getString(1);   // 1 for Part.TEXT

                        cv.put(GnTelephony.MmsSms.WordsTable.ID, id);
                        cv.put(GnTelephony.MmsSms.WordsTable.INDEXED_TEXT, body);
                        cv.put(GnTelephony.MmsSms.WordsTable.SOURCE_ROW_ID, id);
                        cv.put(GnTelephony.MmsSms.WordsTable.TABLE_ID, 1);
                        db.insert(TABLE_WORDS, GnTelephony.MmsSms.WordsTable.INDEXED_TEXT, cv);
                    }
                }
            } finally {
                if (mmsRows != null) {
                    mmsRows.close();
                }
            }
        }
    }

    private void createWordsTables(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE VIRTUAL TABLE words USING FTS3 (_id INTEGER PRIMARY KEY, index_text TEXT, source_id INTEGER, table_to_use INTEGER);");

            // monitor the sms table
            // NOTE don't handle inserts using a trigger because it has an unwanted
            // side effect:  the value returned for the last row ends up being the
            // id of one of the trigger insert not the original row insert.
            // Handle inserts manually in the provider.
            db.execSQL("CREATE TRIGGER sms_words_update AFTER UPDATE ON sms BEGIN UPDATE words " +
                    " SET index_text = NEW.body WHERE (source_id=NEW._id AND table_to_use=1); " +
                    " END;");
            db.execSQL("CREATE TRIGGER sms_words_delete AFTER DELETE ON sms BEGIN DELETE FROM " +
                    "  words WHERE source_id = OLD._id AND table_to_use = 1; END;");

            // monitor the mms table
            db.execSQL("CREATE TRIGGER mms_words_update AFTER UPDATE ON part BEGIN UPDATE words " +
                    " SET index_text = NEW.text WHERE (source_id=NEW._id AND table_to_use=2); " +
                    " END;");
            db.execSQL("CREATE TRIGGER mms_words_delete AFTER DELETE ON part BEGIN DELETE FROM " +
                    " words WHERE source_id = OLD._id AND table_to_use = 2; END;");

            populateWordsTable(db);
        } catch (Exception ex) {
            Log.e(TAG, "got exception creating words table: " + ex.toString());
        }
    }

    private void createIndices(SQLiteDatabase db) {
        createThreadIdIndex(db);
    }

    private void createThreadIdIndex(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE INDEX IF NOT EXISTS typeThreadIdIndex ON sms" +
            " (type, thread_id);");
        } catch (Exception ex) {
            Log.e(TAG, "got exception creating indices: " + ex.toString());
        }
    }
    //Aurora xuyong 2014-09-02 added for whitelist feature start
    private int[] defaultWhiteListNumbers = new int[] {
            10086, 10010, 10000, 95555,
            95566, 95533, 95588, 95558,
            95599, 95568, 95595, 95559, 
            95508, 95528, 95501, 95577, 
            95561
    };
    
    private void initWhiteListTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + WhiteListProvider.WHITELIST_TABLE + ";");
        db.execSQL("CREATE TABLE " + WhiteListProvider.WHITELIST_TABLE + " ( " + "" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                "number TEXT NOT NULL UNIQUE );");
        for (int item : defaultWhiteListNumbers) {
            db.execSQL("INSERT INTO " + WhiteListProvider.WHITELIST_TABLE + 
                    " ( number ) VALUES ( " + item + " );"); 
        }
    }
    //Aurora xuyong 2014-09-02 added for whitelist feature end
    
    //Aurora yudingmin 2014-11-03 added for sync feature start
    private void createMmsSmsSync(SQLiteDatabase db) {
        db.execSQL(MmsSmsSyncProvider.CREATE_TABLE_SYNC_SQL);
        db.execSQL(MmsSmsSyncProvider.CREATE_PART_SYNC_SQL);
    }
    private void createMmsSmsSyncOld(SQLiteDatabase db) {
        db.execSQL(MmsSmsSyncProvider.CREATE_TABLE_SYNC_OLD_SQL);
        db.execSQL(MmsSmsSyncProvider.CREATE_PART_SYNC_SQL);
    }

    private void createMmsSmsSyncView(SQLiteDatabase db) {
        db.execSQL(MmsSmsSyncProvider.CREATE_SYNC_VIEW_SQL);
    }
    //Aurora yudingmin 2014-11-03 added for sync feature end
    
    // Aurora xuyong 2014-10-23 added for privacy feature start
    /* table columns indication:
     * _id primary key
     * is_privacy        Id of a privacy account which the address corresponds to
     * thread_id         Id of a conversation which the address belongs to
     * cur_opt_id        Id of the current conversation
     * status            current conv set 1, else 0
     * number            The recipient's address
    */
    private final String PRICACY_TABLE = "privacy_table";
    private void initCurrentNumberIdTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + PRICACY_TABLE + ";");
        db.execSQL("CREATE TABLE " + PRICACY_TABLE + " ( " + "" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                "is_privacy INTEGER DEFAULT -1, " + 
                "cur_opt_thread_id INTEGER DEFAULT -1, " + 
                "thread_id INTEGER DEFAULT -1, " + 
                "status INTEGER DEFAULT -1, " + 
                "number TEXT NOT NULL);");
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    private void createMmsTables(SQLiteDatabase db) {
        // N.B.: Whenever the columns here are changed, the columns in
        // {@ref MmsSmsProvider} must be changed to match.
        db.execSQL("CREATE TABLE " + MmsProvider.TABLE_PDU + " (" +
                   Mms._ID + " INTEGER PRIMARY KEY," +
                   Mms.THREAD_ID + " INTEGER," +
                   Mms.DATE + " INTEGER," +
                   Mms.DATE_SENT + " INTEGER DEFAULT 0," +
                   Mms.MESSAGE_BOX + " INTEGER," +
                   Mms.READ + " INTEGER DEFAULT 0," +
                   Mms.MESSAGE_ID + " TEXT," +
                   Mms.SUBJECT + " TEXT," +
                   Mms.SUBJECT_CHARSET + " INTEGER," +
                   Mms.CONTENT_TYPE + " TEXT," +
                   Mms.CONTENT_LOCATION + " TEXT," +
                   Mms.EXPIRY + " INTEGER," +
                   Mms.MESSAGE_CLASS + " TEXT," +
                   Mms.MESSAGE_TYPE + " INTEGER," +
                   Mms.MMS_VERSION + " INTEGER," +
                   Mms.MESSAGE_SIZE + " INTEGER," +
                   Mms.PRIORITY + " INTEGER," +
                   Mms.READ_REPORT + " INTEGER," +
                   Mms.REPORT_ALLOWED + " INTEGER," +
                   Mms.RESPONSE_STATUS + " INTEGER," +
                   Mms.STATUS + " INTEGER," +
                   Mms.TRANSACTION_ID + " TEXT," +
                   Mms.RETRIEVE_STATUS + " INTEGER," +
                   Mms.RETRIEVE_TEXT + " TEXT," +
                   Mms.RETRIEVE_TEXT_CHARSET + " INTEGER," +
                   Mms.READ_STATUS + " INTEGER," +
                   Mms.CONTENT_CLASS + " INTEGER," +
                   Mms.RESPONSE_TEXT + " TEXT," +
                   Mms.DELIVERY_TIME + " INTEGER," +
                   Mms.DELIVERY_REPORT + " INTEGER," +
                   Mms.LOCKED + " INTEGER DEFAULT 0," +
                   Mms.SIM_ID + " INTEGER DEFAULT -1," +
                   "service_center TEXT," +
                   Mms.SEEN + " INTEGER DEFAULT 0" +
                    //gionee gaoj 2012-3-27 added for CR00555790 start
                   "," + "star" + " INTEGER DEFAULT 0" +
                    //gionee gaoj 2012-3-27 added for CR00555790 end
                   // Auarora xuyong 2014-04-09 modified for Reject feature start
                   // Aurora xuyong 2014-07-21 modified for reject feature start
                   ", update_threads INTEGER DEFAULT 0" +
                   // Aurora xuyong 2014-07-21 modified for reject feature end
                   "," + "reject" + " INTEGER DEFAULT 0" +
                   // Aurora xuyong 2014-10-23 added for privacy feature start
                   "," + "is_privacy" + " INTEGER DEFAULT 0" +
                   // Aurora xuyong 2014-10-23 added for privacy feature end
                   // Auarora xuyong 2014-04-09 modified for Reject feature end
                   "," + "weather_info" + " TEXT DEFAULT NULL" +
                   ");");

        db.execSQL("CREATE TABLE " + MmsProvider.TABLE_ADDR + " (" +
                   Addr._ID + " INTEGER PRIMARY KEY," +
                   Addr.MSG_ID + " INTEGER," +
                   Addr.CONTACT_ID + " INTEGER," +
                   Addr.ADDRESS + " TEXT," +
                   Addr.TYPE + " INTEGER," +
                   // Aurora xuyong 2014-07-03 modified for reject feature start
                   Addr.CHARSET + " INTEGER," +
                   // Aurora xuyong 2014-10-23 added for privacy feature start
                   "is_privacy" + " INTEGER DEFAULT 0," +
                   // Aurora xuyong 2014-10-23 added for privacy feature end
                   "reject INTEGER DEFAULT 0);");
                   // Aurora xuyong 2014-07-03 modified for reject feature end

        db.execSQL("CREATE TABLE " + MmsProvider.TABLE_PART + " (" +
                   Part._ID + " INTEGER PRIMARY KEY," +
                   Part.MSG_ID + " INTEGER," +
                   Part.SEQ + " INTEGER DEFAULT 0," +
                   Part.CONTENT_TYPE + " TEXT," +
                   Part.NAME + " TEXT," +
                   Part.CHARSET + " INTEGER," +
                   Part.CONTENT_DISPOSITION + " TEXT," +
                   Part.FILENAME + " TEXT," +
                   Part.CONTENT_ID + " TEXT," +
                   Part.CONTENT_LOCATION + " TEXT," +
                   Part.CT_START + " INTEGER," +
                   Part.CT_TYPE + " TEXT," +
                   Part._DATA + " TEXT," +
                   // Aurora xuyong 2014-07-03 modified for reject feature start
                   Part.TEXT + " TEXT, " +
                   // Aurora xuyong 2014-10-23 added for privacy feature start
                   "is_privacy" + " INTEGER DEFAULT 0, " +
                   // Aurora xuyong 2014-10-23 added for privacy feature end
                   "reject INTEGER DEFAULT 0);");
                   // Aurora xuyong 2014-07-03 modified for reject feature end

        db.execSQL("CREATE TABLE " + MmsProvider.TABLE_RATE + " (" +
                   Rate.SENT_TIME + " INTEGER);");

        db.execSQL("CREATE TABLE " + MmsProvider.TABLE_DRM + " (" +
                   BaseColumns._ID + " INTEGER PRIMARY KEY," +
                   "_data TEXT);");
    }

    private void createMmsTriggers(SQLiteDatabase db) {
        // Cleans up parts when a MM is deleted.
        db.execSQL("CREATE TRIGGER part_cleanup DELETE ON " + MmsProvider.TABLE_PDU + " " +
                   "BEGIN " +
                   "  DELETE FROM " + MmsProvider.TABLE_PART +
                   "  WHERE " + Part.MSG_ID + "=old._id;" +
                   "END;");

        // Cleans up address info when a MM is deleted.
        db.execSQL("CREATE TRIGGER addr_cleanup DELETE ON " + MmsProvider.TABLE_PDU + " " +
                   "BEGIN " +
                   "  DELETE FROM " + MmsProvider.TABLE_ADDR +
                   "  WHERE " + Addr.MSG_ID + "=old._id;" +
                   "END;");

        // Delete obsolete delivery-report, read-report while deleting their
        // associated Send.req.
        db.execSQL("CREATE TRIGGER cleanup_delivery_and_read_report " +
                   "AFTER DELETE ON " + MmsProvider.TABLE_PDU + " " +
                   "WHEN old." + Mms.MESSAGE_TYPE + "=" + PduHeaders.MESSAGE_TYPE_SEND_REQ + " " +
                   "BEGIN " +
                   "  DELETE FROM " + MmsProvider.TABLE_PDU +
                   "  WHERE (" + Mms.MESSAGE_TYPE + "=" + PduHeaders.MESSAGE_TYPE_DELIVERY_IND +
                   "    OR " + Mms.MESSAGE_TYPE + "=" + PduHeaders.MESSAGE_TYPE_READ_ORIG_IND +
                   ")" +
                   "    AND " + Mms.MESSAGE_ID + "=old." + Mms.MESSAGE_ID + "; " +
                   "END;");

        // Update threads table to indicate whether attachments exist when
        // parts are inserted or deleted.
        db.execSQL(PART_UPDATE_THREADS_ON_INSERT_TRIGGER);
        db.execSQL(PART_UPDATE_THREADS_ON_UPDATE_TRIGGER);
        db.execSQL(PART_UPDATE_THREADS_ON_DELETE_TRIGGER);
        db.execSQL(PDU_UPDATE_THREADS_ON_UPDATE_TRIGGER);
    }

    private void createSmsTables(SQLiteDatabase db) {
        // N.B.: Whenever the columns here are changed, the columns in
        // {@ref MmsSmsProvider} must be changed to match.
        db.execSQL("CREATE TABLE sms (" +
                   "_id INTEGER PRIMARY KEY," +
                   "thread_id INTEGER," +
                   "address TEXT," +
                   "m_size INTEGER," +
                   "person INTEGER," +
                   "date INTEGER," +
                   "date_sent INTEGER DEFAULT 0," +
                   "protocol INTEGER," +
                   "read INTEGER DEFAULT 0," +
                   "status INTEGER DEFAULT -1," + // a TP-Status value
                                                  // or -1 if it
                                                  // status hasn't
                                                  // been received
                   "type INTEGER," +
                   "reply_path_present INTEGER," +
                   "subject TEXT," +
                   "body TEXT," +
                   "service_center TEXT," +
                   "locked INTEGER DEFAULT 0, " +
                   GnTelephony.GN_SIM_ID + " INTEGER DEFAULT -1," +
                   "error_code INTEGER DEFAULT 0," +
                   "seen INTEGER DEFAULT 0" +
                    //gionee gaoj 2012-3-27 added for CR00555790 start
                   ",star INTEGER DEFAULT 0" +
                    //gionee gaoj 2012-3-27 added for CR00555790 end
                   // Auarora xuyong 2014-06-18 added for Reject feature start
                   // Aurora xuyong 2014-07-21 added for reject feature start
                   ", update_threads INTEGER DEFAULT 0 " + 
                   // Aurora xuyong 2014-07-21 added for reject feature end
                    ",reject INTEGER DEFAULT 0" +
                   // Aurora xuyong 2014-10-23 added for privacy feature start
                    ",is_privacy INTEGER DEFAULT 0" +
                   // Aurora xuyong 2014-10-23 added for privacy feature end
                   // Auarora xuyong 2014-06-18 added for Reject feature end
                   "," + "weather_info" + " TEXT DEFAULT NULL" +
                   ");");

        
        /**
         * This table is used by the SMS dispatcher to hold
         * incomplete partial messages until all the parts arrive.
         */
        db.execSQL("CREATE TABLE raw (" +
                   "_id INTEGER PRIMARY KEY," +
                   "date INTEGER," +
                   "reference_number INTEGER," + // one per full message
                   "count INTEGER," + // the number of parts
                   "sequence INTEGER," + // the part number of this message
                   "destination_port INTEGER," +
                   "address TEXT, " +
                   "sub_id LONG DEFAULT -1, " +
                   GnTelephony.GN_SIM_ID + " INTEGER DEFAULT 0," +
                   "pdu TEXT," +
                   /// M: for ct new feature of concatenated sms @{
                   "recv_time INTEGER," +
                   "upload_flag INTEGER" +
                   /// M: @}
                   ");"); // the raw PDU for this part

        db.execSQL("CREATE TABLE attachments (" +
                   "sms_id INTEGER," +
                   "content_url TEXT," +
                   "offset INTEGER);");

        /**
         * This table is used by the SMS dispatcher to hold pending
         * delivery status report intents.
         */
        db.execSQL("CREATE TABLE sr_pending (" +
                   "reference_number INTEGER," +
                   "action TEXT," +
                   "data TEXT);");
    }

    private void createWapPushTables(SQLiteDatabase db) {
        /*
         * create wap push tables
         */
        if (FeatureOption.MTK_WAPPUSH_SUPPORT) {
            db.execSQL("CREATE TABLE wappush ("
                    + "_id INTEGER PRIMARY KEY,"
                    + "thread_id INTEGER," // thread id
                    + "address TEXT NOT NULL," // sender address
                    + "service_center TEXT NOT NULL," // service center address
                    + "seen INTEGER DEFAULT 0," // seen status 0:unseen,1:seen
                    + "read INTEGER DEFAULT 0," // read status 0:unread,1:read
                    + "locked INTEGER DEFAULT 0," // lock status
                                                    // 0:unlocked,1:locked
                    + "error INTEGER DEFAULT 0," // expire status
                                                    // 0:unexpired,1:expired
                    + GnTelephony.GN_SIM_ID + " INTEGER DEFAULT 0," // gemini
                    + "date INTEGER," // receive time
                    + "type INTEGER DEFAULT 0," // 0:SI,1:SL
                    + "siid TEXT," //
                    + "url TEXT," + "action INTEGER," + "created INTEGER,"
                    + "expiration INTEGER," + "text TEXT" 
        //gionee gaoj 2012-3-27 added for CR00555790 start
                    + ",star INTEGER DEFAULT 0"
                // Aurora xuyong 2014-07-05 added for reject feature start
                    + ",reject INTEGER DEFAULT 0"
                // Aurora xuyong 2014-10-23 added for privacy feature start
                    + ",is_privacy INTEGER DEFAULT 0"
                // Aurora xuyong 2014-10-23 added for privacy feature end
                // Aurora xuyong 2014-07-05 added for reject feature end
                    + ",weather_info" + " TEXT DEFAULT NULL"
                    + ");");
        //gionee gaoj 2012-3-27 added for CR00555790 end
        }
    }
 
    //gionee gaoj 2012-3-27 added for CR00555790 start
    private void createQuickText(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS quicktext;");
        db.execSQL("CREATE TABLE quicktext (" +
                "_id INTEGER PRIMARY KEY," +
                "text TEXT);");
         ContentValues cv = null;
         int id;
         id = R.array.default_quick_texts;
         String[] default_quick_texts = mContext.getResources().getStringArray(id);

         int mDefaultQuickTextCount = default_quick_texts.length;
         for (int i = 0; i < mDefaultQuickTextCount; i++) {
             cv = new ContentValues(); 
             cv.put("text",default_quick_texts[i]);       
             db.insert("quicktext",null,cv);
         }
    }
    private void createCBTables(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_CELLBROADCAST
                + "(_id INTEGER PRIMARY KEY," + GnTelephony.GN_SIM_ID + " INTEGER,"
                + "locked INTEGER DEFAULT 0,"
                + "body TEXT," + "channel_id INTEGER," + "thread_id INTEGER,"
                // Aurora xuyong 2014-07-03 modified for reject feature start
                // Aurora xuyong 2014-07-18 modified for reject feature start
                // Aurora xuyong 2014-07-21 modified for reject feature start
                // Aurora xuyong 2014-10-23 modified for privacy feature start
                + "read INTEGER DEFAULT 0," + "seen INTEGER DEFAULT 0," + "date_sent INTEGER DEFAULT 0," + "date INTEGER,"+"star INTEGER DEFAULT 0,"+ "update_threads INTEGER DEFAULT 0, " + " is_privacy INTEGER DEFAULT 0, " + "weather_info TEXT DEFAULT NULL," + " reject INTEGER DEFAULT 0);");
                // Aurora xuyong 2014-10-23 modified for privacy feature end
                // Aurora xuyong 2014-07-21 modified for reject feature end
                // Aurora xuyong 2014-07-18 modified for reject feature end
                // Aurora xuyong 2014-07-03 modified for reject feature end
    }
   //gionee gaoj 2012-3-27 added for CR00555790 end
    private void createCommonTables(SQLiteDatabase db) {
        // TODO Ensure that each entry is removed when the last use of
        // any address equivalent to its address is removed.

        /**
         * This table maps the first instance seen of any particular
         * MMS/SMS address to an ID, which is then used as its
         * canonical representation.  If the same address or an
         * equivalent address (as determined by our Sqlite
         * PHONE_NUMBERS_EQUAL extension) is seen later, this same ID
         * will be used. The _id is created with AUTOINCREMENT so it
         * will never be reused again if a recipient is deleted.
         */
        db.execSQL("CREATE TABLE canonical_addresses (" +
                   "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                   "address TEXT);");

        /**
         * This table maps the subject and an ordered set of recipient
         * IDs, separated by spaces, to a unique thread ID.  The IDs
         * come from the canonical_addresses table.  This works
         * because messages are considered to be part of the same
         * thread if they have the same subject (or a null subject)
         * and the same set of recipients.
         */
        db.execSQL("CREATE TABLE threads (" +
                   Threads._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                   Threads.DATE + " INTEGER DEFAULT 0," +
                   Threads.MESSAGE_COUNT + " INTEGER DEFAULT 0," +
                   Threads.READCOUNT + " INTEGER DEFAULT 0," +
                   Threads.RECIPIENT_IDS + " TEXT," +
                   Threads.SNIPPET + " TEXT," +
                   Threads.SNIPPET_CHARSET + " INTEGER DEFAULT 0," +
                   Threads.READ + " INTEGER DEFAULT 1," +
                   Threads.TYPE + " INTEGER DEFAULT 0," +
                   Threads.ERROR + " INTEGER DEFAULT 0," +
                   Threads.HAS_ATTACHMENT + " INTEGER DEFAULT 0," +
                   // Aurora xuyong 2014-04-09 modified for aurora's new feature start
                   "unread_count INTEGER DEFAULT 0," +
                   // Aurora xuyong 2014-04-09 modified for aurora's new feature end
                   Threads.STATUS + " INTEGER DEFAULT 0," +
        //gionee gaoj 2012-3-27 added for CR00555790 start
                   GnTelephony.GN_SIM_ID + " INTEGER DEFAULT 1," +
                        // Aurora liugj 2014-01-06 modified for bath-delete optimize start
                   "encryption" +" INTEGER DEFAULT 0," +
                   // Aurora xuyong 2014-06-12 modified for reject feature start
                   "deleted" + " INTEGER DEFAULT 0," + 
                   // Aurora xuyong 2014-07-03 modified for reject feature start
                   "reject" + " INTEGER DEFAULT 0, " +
                   // Aurora xuyong 2014-10-23 added for privacy feature start
                   "is_privacy" + " INTEGER DEFAULT 0, " +
                   // Aurora xuyong 2014-10-23 added for privacy feature end
                   "concurrent_resume" + " INTEGER DEFAULT 0);");
                   // Aurora xuyong 2014-07-03 modified for reject feature end
                   // Aurora xuyong 2014-06-12 modified for reject feature END
                        // Aurora liugj 2014-01-06 modified for bath-delete optimize end
        //gionee gaoj 2012-3-27 added for CR00555790 end

        /**
         * This table stores the queue of messages to be sent/downloaded.
         */
        db.execSQL("CREATE TABLE " + MmsSmsProvider.TABLE_PENDING_MSG +" (" +
                   PendingMessages._ID + " INTEGER PRIMARY KEY," +
                   PendingMessages.PROTO_TYPE + " INTEGER," +
                   PendingMessages.MSG_ID + " INTEGER," +
                   PendingMessages.MSG_TYPE + " INTEGER," +
                   PendingMessages.ERROR_TYPE + " INTEGER," +
                   PendingMessages.ERROR_CODE + " INTEGER," +
                   PendingMessages.RETRY_INDEX + " INTEGER NOT NULL DEFAULT 0," +
                   PendingMessages.DUE_TIME + " INTEGER," +
                   PendingMessages.SIM_ID + " INTEGER DEFAULT 0," +
                   PendingMessages.LAST_TRY + " INTEGER);");
        //gionee gaoj 2012-3-27 added for CR00555790 start
        db.execSQL("CREATE TABLE recent_contact (" +
                   "_id INTEGER PRIMARY KEY," +
                   "date INTEGER," +
                   "number TEXT);");
        //gionee gaoj 2012-3-27 added for CR00555790 end
    }

     //gionee gaoj 2012-3-27 added for CR00555790 start
    /*private void createQuickText(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE quicktext (" +
                "_id INTEGER PRIMARY KEY," +
                "text TEXT);");
    }
    private void createCBTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CELLBROADCAST
                + "(_id INTEGER PRIMARY KEY," + "sim_id INTEGER,"
                + "locked INTEGER DEFAULT 0,"
                + "body TEXT," + "channel_id INTEGER," + "thread_id INTEGER,"
                + "read INTEGER DEFAULT 0," + "seen INTEGER DEFAULT 0," + "date_sent INTEGER DEFAULT 0," + "date INTEGER);");
    }*/
    //gionee gaoj 2012-3-27 added for CR00555790 end
    // TODO Check the query plans for these triggers.
    private void createCommonTriggers(SQLiteDatabase db) {
        // Updates threads table whenever a message is added to pdu.
        db.execSQL("CREATE TRIGGER pdu_update_thread_on_insert AFTER INSERT ON " +
                   MmsProvider.TABLE_PDU + " " +
                   PDU_UPDATE_THREAD_CONSTRAINTS +
                   PDU_UPDATE_THREAD_DATE_SNIPPET_COUNT_ON_UPDATE);

        // Updates threads table whenever a message is added to sms.
        db.execSQL("CREATE TRIGGER sms_update_thread_on_insert AFTER INSERT ON sms " +
                   SMS_UPDATE_THREAD_DATE_SNIPPET_COUNT_ON_UPDATE);

        //gionee gaoj 2012-3-27 added for CR00555790 start
        //update for sim_id for threads table whenever a message is added to pdu
        if (mIsMsgBox) {
            db.execSQL("CREATE TRIGGER pdu_update_thread_sim_id_on_update AFTER " +
                 "  UPDATE OF " + Mms.SIM_ID + 
                 "  ON " + MmsProvider.TABLE_PDU + " " +
                 PDU_UPDATE_THREAD_CONSTRAINTS +
                 PDU_UPDATE_THREAD_SIM_ID_ON_UPDATE);
        }
        //gionee gaoj 2012-3-27 added for CR00555790 end

        // Updates threads table whenever a message in pdu is updated.
        db.execSQL("CREATE TRIGGER pdu_update_thread_date_subject_on_update AFTER" +
                   "  UPDATE OF " + Mms.DATE + ", " + Mms.SUBJECT + ", " + Mms.MESSAGE_BOX +
                   "  ON " + MmsProvider.TABLE_PDU + " " +
                   PDU_UPDATE_THREAD_CONSTRAINTS +
                   PDU_UPDATE_THREAD_DATE_SNIPPET_COUNT_ON_UPDATE);

        // Updates threads table whenever a message in sms is updated.
        db.execSQL("CREATE TRIGGER sms_update_thread_date_subject_on_update AFTER" +
                   "  UPDATE OF " + Sms.DATE + ", " + Sms.BODY + ", " + Sms.TYPE +
                   "  ON sms " +
                   SMS_UPDATE_THREAD_DATE_SNIPPET_COUNT_ON_UPDATE);

        // Updates threads table whenever a message in pdu is updated.
        db.execSQL("CREATE TRIGGER pdu_update_thread_read_on_update AFTER" +
                   "  UPDATE OF " + Mms.READ +
                   "  ON " + MmsProvider.TABLE_PDU + " " +
                   PDU_UPDATE_THREAD_CONSTRAINTS +
                   "BEGIN " +
                   PDU_UPDATE_THREAD_READ_BODY +
                   UPDATE_THREAD_READ_COUNT+
                   "END;");

        // Updates threads table whenever a message in sms is updated.
        db.execSQL("CREATE TRIGGER sms_update_thread_read_on_update AFTER" +
                   "  UPDATE OF " + Sms.READ +
                   "  ON sms " +
                   "BEGIN " +
                   SMS_UPDATE_THREAD_READ_BODY +
                   UPDATE_THREAD_READ_COUNT+
                   "END;");

        // Update threads table whenever a message in pdu is deleted
        db.execSQL("CREATE TRIGGER pdu_update_thread_on_delete " +
                   "AFTER DELETE ON pdu " +
                   "BEGIN " +
                   "  UPDATE threads SET " +
                   "     date = (strftime('%s','now') * 1000)" +
                   "  WHERE threads._id = old." + Mms.THREAD_ID + "; " +
                   UPDATE_THREAD_COUNT_ON_OLD +
                   UPDATE_THREAD_SNIPPET_SNIPPET_CS_ON_DELETE +
                   "END;");

        //When the wap push message is updated, update thread's read status
        if (FeatureOption.MTK_WAPPUSH_SUPPORT){
        //gionee gaoj 2012-3-27 added for CR00555790 start
         // Updates threads table whenever a message is added to wappush.
         if (mIsMsgBox) {
            db.execSQL("CREATE TRIGGER wappush_update_thread_on_insert AFTER INSERT ON wappush " +
                       WAPPUSH_UPDATE_THREAD_DATE_SNIPPET_COUNT_ON_UPDATE);    
         } 
        //gionee gaoj 2012-3-27 added for CR00555790 end    
            db.execSQL("CREATE TRIGGER wappush_update_thread_on_update AFTER" +
                    "  UPDATE OF " + WapPush.READ +
                    "  ON wappush " +
                    "BEGIN " +
                    "  UPDATE threads SET read = " +
                    "    CASE (SELECT COUNT(*)" +
                    "          FROM wappush" +
                    "          WHERE " + WapPush.READ + " = 0" +
                    "            AND " + WapPush.THREAD_ID + " = threads._id)" +
                    "      WHEN 0 THEN 1" +
                    "      ELSE 0" +
                    "    END" +
                    "  WHERE threads._id = new." + WapPush.THREAD_ID + "; " + 
                    "END;");
        }
        // When the last message in a thread is deleted, these
        // triggers ensure that the entry for its thread ID is removed
        // from the threads table.
        if (FeatureOption.MTK_WAPPUSH_SUPPORT) {
        db.execSQL("CREATE TRIGGER delete_obsolete_threads_pdu " +
                   "AFTER DELETE ON pdu " +
                   "BEGIN " +
                   "  DELETE FROM threads " +
                   "  WHERE " +
                   "    _id = old.thread_id " +
                   "    AND _id NOT IN " +
                   "    (SELECT thread_id FROM sms " +
                   "    UNION SELECT thread_id FROM wappush " +
                   "     UNION SELECT thread_id from pdu " +
                   "     UNION SELECT thread_id from cellbroadcast); " +
                   "END;");
        // As of DATABASE_VERSION 55, we've removed these triggers that delete empty threads.
        // These triggers interfere with saving drafts on brand new threads. Instead of
        // triggers cleaning up empty threads, the empty threads should be cleaned up by
        // an explicit call to delete with Threads.OBSOLETE_THREADS_URI.

        db.execSQL("CREATE TRIGGER delete_obsolete_threads_when_update_pdu " +
                   "AFTER UPDATE OF " + Mms.THREAD_ID + " ON pdu " +
                   "WHEN old." + Mms.THREAD_ID + " != new." + Mms.THREAD_ID + " " +
                   "BEGIN " +
                   "  DELETE FROM threads " +
                   "  WHERE " +
                   "    _id = old.thread_id " +
                   "    AND _id NOT IN " +
                   "    (SELECT thread_id FROM sms " +
                    "    UNION SELECT thread_id FROM wappush " +
                   "     UNION SELECT thread_id from pdu " +
                   "     UNION SELECT thread_id from cellbroadcast); " +
                   "END;");

        } else {
        db.execSQL("CREATE TRIGGER delete_obsolete_threads_pdu " +
                   "AFTER DELETE ON pdu " +
                   "BEGIN " +
                   "  DELETE FROM threads " +
                   "  WHERE " +
                   "    _id = old.thread_id " +
                   "    AND _id NOT IN " +
                   "    (SELECT thread_id FROM sms " +
                   "     UNION SELECT thread_id from pdu " +
                   "     UNION SELECT thread_id from cellbroadcast); " +
                   "END;");

        db.execSQL("CREATE TRIGGER delete_obsolete_threads_when_update_pdu " +
                   "AFTER UPDATE OF " + Mms.THREAD_ID + " ON pdu " +
                   "WHEN old." + Mms.THREAD_ID + " != new." + Mms.THREAD_ID + " " +
                   "BEGIN " +
                   "  DELETE FROM threads " +
                   "  WHERE " +
                   "    _id = old.thread_id " +
                   "    AND _id NOT IN " +
                   "    (SELECT thread_id FROM sms " +
                   "     UNION SELECT thread_id from pdu " +
                   "     UNION SELECT thread_id from cellbroadcast); " +
                   "END;");
        }

        // Insert pending status for M-Notification.ind or M-ReadRec.ind
        // when they are inserted into Inbox/Outbox.
        db.execSQL("CREATE TRIGGER insert_mms_pending_on_insert " +
                   "AFTER INSERT ON pdu " +
                   "WHEN new." + Mms.MESSAGE_TYPE + "=" + PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND +
                   "  OR new." + Mms.MESSAGE_TYPE + "=" + PduHeaders.MESSAGE_TYPE_READ_REC_IND +
                   " " +
                   "BEGIN " +
                   "  INSERT INTO " + MmsSmsProvider.TABLE_PENDING_MSG +
                   "    (" + PendingMessages.PROTO_TYPE + "," +
                   "     " + PendingMessages.MSG_ID + "," +
                   "     " + PendingMessages.MSG_TYPE + "," +
                   "     " + PendingMessages.ERROR_TYPE + "," +
                   "     " + PendingMessages.ERROR_CODE + "," +
                   "     " + PendingMessages.RETRY_INDEX + "," +
                   "     " + PendingMessages.DUE_TIME + ") " +
                   "  VALUES " +
                   "    (" + MmsSms.MMS_PROTO + "," +
                   "      new." + BaseColumns._ID + "," +
                   "      new." + Mms.MESSAGE_TYPE + ",0,0,0,0);" +
                   "END;");

        // Insert pending status for M-Send.req when it is moved into Outbox.
        db.execSQL("CREATE TRIGGER insert_mms_pending_on_update " +
                   "AFTER UPDATE ON pdu " +
                   "WHEN new." + Mms.MESSAGE_TYPE + "=" + PduHeaders.MESSAGE_TYPE_SEND_REQ +
                   "  AND new." + Mms.MESSAGE_BOX + "=" + Mms.MESSAGE_BOX_OUTBOX +
                   "  AND old." + Mms.MESSAGE_BOX + "!=" + Mms.MESSAGE_BOX_OUTBOX + " " +
                   "BEGIN " +
                   "  INSERT INTO " + MmsSmsProvider.TABLE_PENDING_MSG +
                   "    (" + PendingMessages.PROTO_TYPE + "," +
                   "     " + PendingMessages.MSG_ID + "," +
                   "     " + PendingMessages.MSG_TYPE + "," +
                   "     " + PendingMessages.ERROR_TYPE + "," +
                   "     " + PendingMessages.ERROR_CODE + "," +
                   "     " + PendingMessages.RETRY_INDEX + "," +
                   "     " + PendingMessages.DUE_TIME + ") " +
                   "  VALUES " +
                   "    (" + MmsSms.MMS_PROTO + "," +
                   "      new." + BaseColumns._ID + "," +
                   "      new." + Mms.MESSAGE_TYPE + ",0,0,0,0);" +
                   "END;");

        // When a message is moved out of Outbox, delete its pending status.
        db.execSQL("CREATE TRIGGER delete_mms_pending_on_update " +
                   "AFTER UPDATE ON " + MmsProvider.TABLE_PDU + " " +
                   "WHEN old." + Mms.MESSAGE_BOX + "=" + Mms.MESSAGE_BOX_OUTBOX +
                   "  AND new." + Mms.MESSAGE_BOX + "!=" + Mms.MESSAGE_BOX_OUTBOX + " " +
                   "BEGIN " +
                   "  DELETE FROM " + MmsSmsProvider.TABLE_PENDING_MSG +
                   "  WHERE " + PendingMessages.MSG_ID + "=new._id; " +
                   "END;");

        // Delete pending status for a message when it is deleted.
        db.execSQL("CREATE TRIGGER delete_mms_pending_on_delete " +
                   "AFTER DELETE ON " + MmsProvider.TABLE_PDU + " " +
                   "BEGIN " +
                   "  DELETE FROM " + MmsSmsProvider.TABLE_PENDING_MSG +
                   "  WHERE " + PendingMessages.MSG_ID + "=old._id; " +
                   "END;");

        // TODO Add triggers for SMS retry-status management.

        // Update the error flag of threads when the error type of
        // a pending MM is updated.
        db.execSQL("CREATE TRIGGER update_threads_error_on_update_mms " +
                   "  AFTER UPDATE OF err_type ON pending_msgs " +
                   "  WHEN (OLD.err_type < 10 AND NEW.err_type >= 10 AND NEW.proto_type = " + MmsSms.MMS_PROTO + " AND NEW.msg_type = " + PduHeaders.MESSAGE_TYPE_SEND_REQ + ")" +
                   "    OR (OLD.err_type >= 10 AND NEW.err_type < 10) " +
                   "BEGIN" +
                   "  UPDATE threads SET error = " +
                   "    CASE" +
                   "      WHEN NEW.err_type >= 10 THEN error + 1" +
                   "      ELSE error - 1" +
                   "    END " +
                   "  WHERE _id =" +
                   "   (SELECT DISTINCT thread_id" +
                   "    FROM pdu" +
                   "    WHERE _id = NEW.msg_id); " +
                   "END;");

        // Update the error flag of threads when delete pending message.
        db.execSQL("CREATE TRIGGER update_threads_error_on_delete_mms " +
                   "  BEFORE DELETE ON pdu" +
                   "  WHEN OLD._id IN (SELECT DISTINCT msg_id" +
                   "                   FROM pending_msgs" +
                   "                   WHERE err_type >= 10 AND msg_type = 128) " +
                   "BEGIN " +
                   "  UPDATE threads SET error = error - 1" +
                   "  WHERE _id = OLD.thread_id; " +
                   "END;");

        // Update the error flag of threads while moving an MM out of Outbox,
        // which was failed to be sent permanently.
        db.execSQL("CREATE TRIGGER update_threads_error_on_move_mms " +
                   "  BEFORE UPDATE OF msg_box ON pdu " +
                   "  WHEN (OLD.msg_box = 4 AND NEW.msg_box != 4) " +
                   "  AND (OLD._id IN (SELECT DISTINCT msg_id" +
                   "                   FROM pending_msgs" +
                   "                   WHERE err_type >= 10)) " +
                   "BEGIN " +
                   "  UPDATE threads SET error = error - 1" +
                   "  WHERE _id = OLD.thread_id; " +
                   "END;");

        // Update the error flag of threads after a text message was
        // failed to send/receive.
        db.execSQL("CREATE TRIGGER update_threads_error_on_update_sms " +
                   "  AFTER UPDATE OF type ON sms" +
                   "  WHEN (OLD.type != 5 AND NEW.type = 5)" +
                   "    OR (OLD.type = 5 AND NEW.type != 5) " +
                   "BEGIN " +
                   "  UPDATE threads SET error = " +
                   "    CASE" +
                   "      WHEN NEW.type = 5 THEN error + 1" +
                   "      ELSE error - 1" +
                   "    END " +
                   "  WHERE _id = NEW.thread_id; " +
                   "END;");
        
        // Triggers for CB
        db.execSQL("CREATE TRIGGER cb_update_thread_on_insert AFTER INSERT ON cellbroadcast "
                + CB_UPDATE_THREAD_DATE_SNIPPET_COUNT_ON_UPDATE);
        db.execSQL("CREATE TRIGGER cb_update_thread_read_on_update AFTER"
                + "  UPDATE OF " + GnTelephony.CbSms.READ + "  ON cellbroadcast "
                + "BEGIN " + CB_UPDATE_THREAD_READ_BODY+UPDATE_THREAD_READ_COUNT + "END;"); 
        // Update threads table whenever a message in messages is deleted
        db.execSQL("CREATE TRIGGER cb_update_thread_on_delete " +
                   "AFTER DELETE ON cellbroadcast " +
                   "BEGIN " +
                   "  UPDATE threads SET " +
                   "     date = (strftime('%s','now') * 1000)" +
                   "  WHERE threads._id = old." + GnTelephony.CbSms.THREAD_ID + "; " +
                   CB_UPDATE_THREAD_COUNT_ON_OLD +
                   CB_UPDATE_THREAD_SNIPPET_ON_DELETE +
                   CB_UPDATE_THREAD_DATE_ON_DELETE +
                   "END;");
        // Aurora xuyong 2014-07-03 added for reject feature start
        addNewCommonTrigger(db);
        // Aurora xuyong 2014-07-03 added for reject feature end
        // Aurora xuyong 2014-10-23 added for privacy feature start
        addPrivacyTrigger(db);
        // Aurora xuyong 2014-10-23 added for privacy feature end
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion
                + " to " + currentVersion + ".");

        switch (oldVersion) {
        case 40:
            if (currentVersion <= 40) {
                return;
            }

            db.beginTransaction();
            try {
                upgradeDatabaseToVersion41(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // fall through
        case 41:
            if (currentVersion <= 41) {
                return;
            }

            db.beginTransaction();
            try {
                upgradeDatabaseToVersion42(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // fall through
        case 42:
            if (currentVersion <= 42) {
                return;
            }

            db.beginTransaction();
            try {
                upgradeDatabaseToVersion43(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // fall through
        case 43:
            if (currentVersion <= 43) {
                return;
            }

            db.beginTransaction();
            try {
                upgradeDatabaseToVersion44(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // fall through
        case 44:
            if (currentVersion <= 44) {
                return;
            }

            db.beginTransaction();
            try {
                upgradeDatabaseToVersion45(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // fall through
        case 45:
            if (currentVersion <= 45) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion46(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // fall through
        case 46:
            if (currentVersion <= 46) {
                return;
            }

            db.beginTransaction();
            try {
                upgradeDatabaseToVersion47(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // fall through
        case 47:
            if (currentVersion <= 47) {
                return;
            }

            db.beginTransaction();
            try {
                upgradeDatabaseToVersion48(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // fall through
        case 48:
            if (currentVersion <= 48) {
                return;
            }

            db.beginTransaction();
            try {
                createWordsTables(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // fall through
        case 49:
            if (currentVersion <= 49) {
                return;
            }
            db.beginTransaction();
            try {
                createThreadIdIndex(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break; // force to destroy all old data;
            } finally {
                db.endTransaction();
            }
            // fall through
        case 50:
            if (currentVersion <= 50) {
                return;
            }

            db.beginTransaction();
            try {
                upgradeDatabaseToVersion51(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // fall through
        case 51:
            if (currentVersion <= 51) {
                return;
            }
            // 52 was adding a new meta_data column, but that was removed.
            // fall through
        case 52:
            if (currentVersion <= 52) {
                return;
            }

            db.beginTransaction();
            try {
                upgradeDatabaseToVersion53(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // fall through
        case 53:
            if (currentVersion <= 53) {
                return;
            }

            db.beginTransaction();
            try {
                upgradeDatabaseToVersion530100(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
           // fall through
        case 530100:
            if (currentVersion <= 530100) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion530200(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
   //             Xlog.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // fall through
        case 530200:
            if (currentVersion <= 530200) {
                return;
            }

            db.beginTransaction();
            try {
                upgradeDatabaseToVersion530300(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
    //            Xlog.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // fall through
        case 530300:
            if (currentVersion <= 530300) {
                return;
            }

            db.beginTransaction();
            try {
                upgradeDatabaseToVersion540000(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
 //               Xlog.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // fall through

        case 540000:
            if (currentVersion <= 540000) {
                return;
            }

            db.beginTransaction();
            try {
                upgradeDatabaseToVersion550000(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
//                Xlog.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
          // Aurora liugj 2014-01-06 modified for bath-delete optimize start
        case 550000:
            if (currentVersion <= 550000) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion550100(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // Aurora xuyong 2014-07-31 modified for database upgrade start
            //return;
            // Aurora xuyong 2014-07-31 modified for database upgrade end
        // Aurora xuyong 2014-04-09 added for aurora's new feature start
        case 550100:
            if (currentVersion <= 550100) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion550101(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // Aurora xuyong 2014-07-31 modified for database upgrade start
            //return;
            // Aurora xuyong 2014-07-31 modified for database upgrade end
        // Aurora xuyong 2014-04-09 added for aurora's new feature end
        // Auarora xuyong 2014-04-09 added for Reject feature start
        case 550101:
            if (currentVersion <= 550101) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion550102(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // Aurora xuyong 2014-07-31 modified for database upgrade start
            //return;
            // Aurora xuyong 2014-07-31 modified for database upgrade end
        // Auarora xuyong 2014-04-09 added for Reject feature end
        // Auarora xuyong 2014-06-18 added for Reject feature start
        case 550102:
            if (currentVersion <= 550102) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion550103(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // Aurora xuyong 2014-07-31 modified for database upgrade start
            //return;
            // Aurora xuyong 2014-07-31 modified for database upgrade end
         // Auarora xuyong 2014-06-18 added for Reject feature end
        // Auarora xuyong 2014-06-19 modified for database upgrade start
        case 550103:
            if (currentVersion <= 550103) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion550104(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // Aurora xuyong 2014-07-31 modified for database upgrade start
            //return;
            // Aurora xuyong 2014-07-31 modified for database upgrade end
        // Aurora xuyong 2014-07-03 added for reject feature start
        case 550104:
            if (currentVersion <= 550104) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion550105(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // Aurora xuyong 2014-07-31 modified for database upgrade start
            //return;
            // Aurora xuyong 2014-07-31 modified for database upgrade end
        // Aurora xuyong 2014-07-04 added for aurora's new feature start
        case 550105:
            if (currentVersion <= 550105) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion550106(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // Aurora xuyong 2014-07-31 modified for database upgrade start
            //return;
            // Aurora xuyong 2014-07-31 modified for database upgrade end
         // Aurora xuyong 2014-07-05 added for reject feature start
        case 550106:
            if (currentVersion <= 550106) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion600000(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // Aurora xuyong 2014-07-31 modified for database upgrade start
            //return;
            // Aurora xuyong 2014-07-31 modified for database upgrade end
        // Aurora xuyong 2014-07-09 added for reject feature start
        case 600000:
            if (currentVersion <= 600000) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion600001(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // Aurora xuyong 2014-07-31 modified for database upgrade start
            //return;
            // Aurora xuyong 2014-07-31 modified for database upgrade end
        // Aurora xuyong 2014-07-19 added for bug #6656 start
        case 600001:
            if (currentVersion <= 600001) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion600002(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // Aurora xuyong 2014-07-31 modified for database upgrade start
            //return;
            // Aurora xuyong 2014-07-31 modified for database upgrade end
        // Aurora xuyong 2014-07-21 added for reject feature start
        case 600002:
            if (currentVersion <= 600002) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion600003(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // Aurora xuyong 2014-07-31 modified for database upgrade start
            //return;
            // Aurora xuyong 2014-07-31 modified for database upgrade end
        // Aurora xuyong 2014-07-23 modified for database upgrade start
        case 600003:
            if (currentVersion <= 600003) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion600004(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // Aurora xuyong 2014-07-31 modified for database upgrade start
            //return;
            // Aurora xuyong 2014-07-31 modified for database upgrade end
        // Aurora xuyong 2014-07-29 modified for database upgrade start
        case 600004:
            if (currentVersion <= 600004) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion600005(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
        // Aurora xuyong 2014-08-05 modified for reject start
        case 600005:
            if (currentVersion <= 600005) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion600006(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
        //Aurora xuyong 2014-09-02 added for whitelist feature start
        case 600006:
            if (currentVersion <= 600006) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion600007(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
        // Aurora xuyong 2014-10-23 added for privacy feature start
        case 600007:
            if (currentVersion <= 600007) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion700000(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
         // Aurora xuyong 2014-10-23 added for privacy feature end
         //Aurora xuyong 2014-09-02 added for whitelist feature end
            //Aurora yudingmin 2014-11-03 added for sync feature start
        case 700000:
        {
            if (currentVersion <= 700000) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion700001(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
        }
        case 700001:
        {
            if (currentVersion <= 700001) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion700002(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
        }
        case 700002:
        {
            if (currentVersion <= 700002) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion700003(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
        }
        case 700003:
        {
            if (currentVersion <= 700003) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion700004(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
        }
        case 700004:
        {
            if (currentVersion <= 700004) {
                return;
            }
            db.beginTransaction();
            try {
                upgradeDatabaseToVersion700005(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
        }
            //Aurora yudingmin 2014-11-03 added for sync feature end
            return;
         // Aurora xuyong 2014-08-05 modified for reject end
         // Aurora xuyong 2014-07-29 modified for database upgrade end
         // Aurora xuyong 2014-07-23 modified for database upgrade end
         // Aurora xuyong 2014-07-21 added for reject feature end
         // Aurora xuyong 2014-07-19 added for bug #6656 end
         // Aurora xuyong 2014-07-09 added for reject feature end
          // Aurora xuyong 2014-07-05 added for reject feature end
         // Aurora xuyong 2014-07-04 added for aurora's new feature end
         // Aurora xuyong 2014-07-03 added for reject feature end
         // Auarora xuyong 2014-06-19 modified for database upgrade end
        }
          // Aurora liugj 2014-01-06 modified for bath-delete optimize end

        Log.e(TAG, "Destroying all old data.");
        dropAll(db);
        onCreate(db);
    }

    private void dropAll(SQLiteDatabase db) {
        // Clean the database out in order to start over from scratch.
        // We don't need to drop our triggers here because SQLite automatically
        // drops a trigger when its attached database is dropped.
        // Aurora xuyong 2014-07-18 modified for reject feature start
        db.execSQL("DROP TABLE IF EXISTS canonical_addresses;");
        db.execSQL("DROP TABLE IF EXISTS threads;");
        db.execSQL("DROP TABLE IF EXISTS " + MmsSmsProvider.TABLE_PENDING_MSG + ";");
        // Aurora xuyong 2014-07-18 modified for reject feature end
        // Aurora xuyong 2014-07-07 modified for drop table recent_contact start
        db.execSQL("DROP TABLE IF EXISTS recent_contact;");
        // Aurora xuyong 2014-07-07 modified for drop table recent_contact end
        // Aurora xuyong 2014-07-18 modified for reject feature start
        db.execSQL("DROP TABLE IF EXISTS sms;");
        // Aurora xuyong 2014-07-18 modified for reject feature end
        
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
          // Aurora xuyong 2014-07-18 modified for reject feature start
            db.execSQL("DROP TABLE IF EXISTS wappush;");
          // Aurora xuyong 2014-07-18 modified for reject feature end
        }
        // Aurora xuyong 2014-07-18 modified for reject feature start
        db.execSQL("DROP TABLE IF EXISTS raw;");
        db.execSQL("DROP TABLE IF EXISTS attachments;");
        db.execSQL("DROP TABLE IF EXISTS thread_ids;");
        db.execSQL("DROP TABLE IF EXISTS sr_pending;");
        // Aurora xuyong 2014-07-18 modified for reject feature end
        db.execSQL("DROP TABLE IF EXISTS " + MmsProvider.TABLE_PDU + ";");
        db.execSQL("DROP TABLE IF EXISTS " + MmsProvider.TABLE_ADDR + ";");
        db.execSQL("DROP TABLE IF EXISTS " + MmsProvider.TABLE_PART + ";");
        db.execSQL("DROP TABLE IF EXISTS " + MmsProvider.TABLE_RATE + ";");
        db.execSQL("DROP TABLE IF EXISTS " + MmsProvider.TABLE_DRM + ";");
        // Aurora xuyong 2014-07-18 added for reject feature start
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CELLBROADCAST + ";");
        // Aurora xuyong 2014-07-18 added for reject feature end
        // Aurora yudingmin 2014-11-06 added for sync feature start
        db.execSQL("DROP TABLE IF EXISTS " + MmsSmsSyncProvider.TABLE_SYNC + ";");
        // Aurora yudingmin 2014-11-06 added for sync feature end
    }

    private void upgradeDatabaseToVersion41(SQLiteDatabase db) {
        db.execSQL("DROP TRIGGER IF EXISTS update_threads_error_on_move_mms");
        db.execSQL("CREATE TRIGGER update_threads_error_on_move_mms " +
                   "  BEFORE UPDATE OF msg_box ON pdu " +
                   "  WHEN (OLD.msg_box = 4 AND NEW.msg_box != 4) " +
                   "  AND (OLD._id IN (SELECT DISTINCT msg_id" +
                   "                   FROM pending_msgs" +
                   "                   WHERE err_type >= 10)) " +
                   "BEGIN " +
                   "  UPDATE threads SET error = error - 1" +
                   "  WHERE _id = OLD.thread_id; " +
                   "END;");
    }

    private void upgradeDatabaseToVersion42(SQLiteDatabase db) {
        db.execSQL("DROP TRIGGER IF EXISTS sms_update_thread_on_delete");
        db.execSQL("DROP TRIGGER IF EXISTS delete_obsolete_threads_sms");
        db.execSQL("DROP TRIGGER IF EXISTS update_threads_error_on_delete_sms");
    }

    private void upgradeDatabaseToVersion43(SQLiteDatabase db) {
        // Add 'has_attachment' column to threads table.
        db.execSQL("ALTER TABLE threads ADD COLUMN has_attachment INTEGER DEFAULT 0");

        updateThreadsAttachmentColumn(db);

        // Add insert and delete triggers for keeping it up to date.
        db.execSQL(PART_UPDATE_THREADS_ON_INSERT_TRIGGER);
        db.execSQL(PART_UPDATE_THREADS_ON_DELETE_TRIGGER);
    }

    private void upgradeDatabaseToVersion44(SQLiteDatabase db) {
        updateThreadsAttachmentColumn(db);

        // add the update trigger for keeping the threads up to date.
        db.execSQL(PART_UPDATE_THREADS_ON_UPDATE_TRIGGER);
    }

    private void upgradeDatabaseToVersion45(SQLiteDatabase db) {
        // Add 'locked' column to sms table.
        db.execSQL("ALTER TABLE sms ADD COLUMN " + Sms.LOCKED + " INTEGER DEFAULT 0");

        // Add 'locked' column to pdu table.
        db.execSQL("ALTER TABLE pdu ADD COLUMN " + Mms.LOCKED + " INTEGER DEFAULT 0");
        
        //gionee gaoj 2012-3-27 added for CR00555790 start
        // Add 'locked' column to sms table.
        db.execSQL("ALTER TABLE sms ADD COLUMN " + "star" + " INTEGER DEFAULT 0");
    
        // Add 'locked' column to pdu table.
        db.execSQL("ALTER TABLE pdu ADD COLUMN " + "star" + " INTEGER DEFAULT 0");
        //gionee gaoj 2012-3-27 added for CR00555790 end
    }

    
    private void upgradeDatabaseToVersion46(SQLiteDatabase db) {
        // add the "text" column for caching inline text (e.g. strings) instead of
        // putting them in an external file
        db.execSQL("ALTER TABLE part ADD COLUMN " + Part.TEXT + " TEXT");

        Cursor textRows = db.query(
                "part",
                new String[] { Part._ID, Part._DATA, Part.TEXT},
                "ct = 'text/plain' OR ct == 'application/smil'",
                null,
                null,
                null,
                null);
        ArrayList<String> filesToDelete = new ArrayList<String>();
        try {
            db.beginTransaction();
            if (textRows != null) {
                int partDataColumn = textRows.getColumnIndex(Part._DATA);

                // This code is imperfect in that we can't guarantee that all the
                // backing files get deleted.  For example if the system aborts after
                // the database is updated but before we complete the process of
                // deleting files.
                while (textRows.moveToNext()) {
                    String path = textRows.getString(partDataColumn);
                    if (path != null) {
                        try {
                            InputStream is = new FileInputStream(path);
                            byte [] data = new byte[is.available()];
                            is.read(data);
                            EncodedStringValue v = new EncodedStringValue(data);
                            db.execSQL("UPDATE part SET " + Part._DATA + " = NULL, " +
                                    Part.TEXT + " = ?", new String[] { v.getString() });
                            is.close();
                            filesToDelete.add(path);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            for (String pathToDelete : filesToDelete) {
                try {
                    (new File(pathToDelete)).delete();
                } catch (SecurityException ex) {
                    Log.e(TAG, "unable to clean up old mms file for " + pathToDelete, ex);
                }
            }
            if (textRows != null) {
                textRows.close();
            }
        }
    }

    private void upgradeDatabaseToVersion47(SQLiteDatabase db) {
        updateThreadsAttachmentColumn(db);

        // add the update trigger for keeping the threads up to date.
        db.execSQL(PDU_UPDATE_THREADS_ON_UPDATE_TRIGGER);
    }

    private void upgradeDatabaseToVersion48(SQLiteDatabase db) {
        // Add 'error_code' column to sms table.
        db.execSQL("ALTER TABLE sms ADD COLUMN error_code INTEGER DEFAULT 0");
    }

    private void upgradeDatabaseToVersion51(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE sms add COLUMN seen INTEGER DEFAULT 0");
        db.execSQL("ALTER TABLE pdu add COLUMN seen INTEGER DEFAULT 0");

        try {
            // update the existing sms and pdu tables so the new "seen" column is the same as
            // the "read" column for each row.
            ContentValues contentValues = new ContentValues();
            contentValues.put("seen", 1);
            int count = db.update("sms", contentValues, "read=1", null);
            Log.d(TAG, "[MmsSmsDb] upgradeDatabaseToVersion51: updated " + count +
                    " rows in sms table to have READ=1");
            count = db.update("pdu", contentValues, "read=1", null);
            Log.d(TAG, "[MmsSmsDb] upgradeDatabaseToVersion51: updated " + count +
                    " rows in pdu table to have READ=1");
        } catch (Exception ex) {
            Log.e(TAG, "[MmsSmsDb] upgradeDatabaseToVersion51 caught ", ex);
        }
    }
 
    private void upgradeDatabaseToVersion53(SQLiteDatabase db) {
        db.execSQL("DROP TRIGGER IF EXISTS pdu_update_thread_read_on_update");

        // Updates threads table whenever a message in pdu is updated.
        db.execSQL("CREATE TRIGGER pdu_update_thread_read_on_update AFTER" +
                   "  UPDATE OF " + Mms.READ +
                   "  ON " + MmsProvider.TABLE_PDU + " " +
                   PDU_UPDATE_THREAD_CONSTRAINTS +
                   "BEGIN " +
                   PDU_UPDATE_THREAD_READ_BODY +
                   "END;");
    }
     private void upgradeDatabaseToVersion530100(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + MmsProvider.TABLE_PDU +" ADD COLUMN " + "service_center" + " TEXT");
    }
    
    private void upgradeDatabaseToVersion530200(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE threads" +" ADD COLUMN " + "readcount" + " INTEGER"); 
        
        db.execSQL("DROP TRIGGER IF EXISTS sms_update_thread_on_insert");
        // Updates threads table whenever a message is added to sms.
        db.execSQL("CREATE TRIGGER sms_update_thread_on_insert AFTER INSERT ON sms " +
                   SMS_UPDATE_THREAD_DATE_SNIPPET_COUNT_ON_UPDATE);
        
        db.execSQL("DROP TRIGGER IF EXISTS sms_update_thread_date_subject_on_update");
     // Updates threads table whenever a message in sms is updated.
        db.execSQL("CREATE TRIGGER sms_update_thread_date_subject_on_update AFTER" +
                   "  UPDATE OF " + Sms.DATE + ", " + Sms.BODY + ", " + Sms.TYPE +
                   "  ON sms " +
                   SMS_UPDATE_THREAD_DATE_SNIPPET_COUNT_ON_UPDATE);

        db.execSQL("DROP TRIGGER IF EXISTS sms_update_thread_read_on_update");
     // Updates threads table whenever a message in sms is updated.
        db.execSQL("CREATE TRIGGER sms_update_thread_read_on_update AFTER" +
                   "  UPDATE OF " + Sms.READ +
                   "  ON sms " +
                   "BEGIN " +
                   SMS_UPDATE_THREAD_READ_BODY +
                   UPDATE_THREAD_READ_COUNT+
                   "END;");
        
        db.execSQL("DROP TRIGGER IF EXISTS pdu_update_thread_on_insert");
        // Updates threads table whenever a message is added to pdu.
        db.execSQL("CREATE TRIGGER pdu_update_thread_on_insert AFTER INSERT ON " +
                   MmsProvider.TABLE_PDU + " " +
                   PDU_UPDATE_THREAD_CONSTRAINTS +
                   PDU_UPDATE_THREAD_DATE_SNIPPET_COUNT_ON_UPDATE);
        
        db.execSQL("DROP TRIGGER IF EXISTS pdu_update_thread_date_subject_on_update");
        // Updates threads table whenever a message in pdu is updated.
        db.execSQL("CREATE TRIGGER pdu_update_thread_date_subject_on_update AFTER" +
                   "  UPDATE OF " + Mms.DATE + ", " + Mms.SUBJECT + ", " + Mms.MESSAGE_BOX +
                   "  ON " + MmsProvider.TABLE_PDU + " " +
                   PDU_UPDATE_THREAD_CONSTRAINTS +
                   PDU_UPDATE_THREAD_DATE_SNIPPET_COUNT_ON_UPDATE);        
        
        db.execSQL("DROP TRIGGER IF EXISTS pdu_update_thread_read_on_update");
        // Updates threads table whenever a message in pdu is updated.
        db.execSQL("CREATE TRIGGER pdu_update_thread_read_on_update AFTER" +
            "  UPDATE OF " + Mms.READ +
            "  ON " + MmsProvider.TABLE_PDU + " " +
            PDU_UPDATE_THREAD_CONSTRAINTS +
            "BEGIN " +
            PDU_UPDATE_THREAD_READ_BODY +
            UPDATE_THREAD_READ_COUNT+
            "END;");
        
        db.execSQL("DROP TRIGGER IF EXISTS cb_update_thread_on_insert");
        // Triggers for CB
        db.execSQL("CREATE TRIGGER cb_update_thread_on_insert AFTER INSERT ON cellbroadcast "
                + CB_UPDATE_THREAD_DATE_SNIPPET_COUNT_ON_UPDATE);
        
        db.execSQL("DROP TRIGGER IF EXISTS cb_update_thread_read_on_update");
        db.execSQL("CREATE TRIGGER cb_update_thread_read_on_update AFTER"
            + "  UPDATE OF " + GnTelephony.CbSms.READ + "  ON cellbroadcast "
            + "BEGIN " + CB_UPDATE_THREAD_READ_BODY+UPDATE_THREAD_READ_COUNT + "END;"); 

    }

    private void upgradeDatabaseToVersion530300(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE threads ADD COLUMN status INTEGER DEFAULT 0"); 
    }
    private void upgradeDatabaseToVersion540000(SQLiteDatabase db) {
        // Add 'date_sent' column to sms table.
        db.execSQL("ALTER TABLE sms ADD COLUMN " + Sms.DATE_SENT + " INTEGER DEFAULT 0");

        // Add 'date_sent' column to pdu table.
        db.execSQL("ALTER TABLE pdu ADD COLUMN " + Mms.DATE_SENT + " INTEGER DEFAULT 0");
   
        // Add 'date_sent' column to cb table.
        db.execSQL("ALTER TABLE cellbroadcast ADD COLUMN date_sent INTEGER DEFAULT 0");
   
    }

    private void upgradeDatabaseToVersion550000(SQLiteDatabase db) {
        // Drop removed triggers
        db.execSQL("DROP TRIGGER IF EXISTS delete_obsolete_threads_pdu");
        db.execSQL("DROP TRIGGER IF EXISTS delete_obsolete_threads_when_update_pdu");
    }
    
     // Aurora liugj 2014-01-06 modified for bath-delete optimize start
    private void upgradeDatabaseToVersion550100(SQLiteDatabase db) {
        // Add 'deleted' column to threads table.
        db.execSQL("ALTER TABLE threads ADD COLUMN deleted INTEGER DEFAULT 0");
    }
     // Aurora liugj 2014-01-06 modified for bath-delete optimize end
    // Aurora xuyong 2014-04-09 added for aurora's new feature start
    private void upgradeDatabaseToVersion550101(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE threads ADD COLUMN unread_count INTEGER DEFAULT 0");
    }
    // Aurora xuyong 2014-04-09 added for aurora's new feature end
    // Auarora xuyong 2014-06-12 added for Reject feature start
    private void upgradeDatabaseToVersion550102(SQLiteDatabase db) {
        // Aurora xuyong 2014-07-29 modified for database upgrade start
        checkTablesHasRejectColumns(db);
        try {
            addNewCommonTrigger(db);
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
        // Aurora xuyong 2014-07-29 modified for database upgrade end
    }
    // Auarora xuyong 2014-06-12 added for Reject feature end
    // Auarora xuyong 2014-06-18 added for Reject feature start
    private void upgradeDatabaseToVersion550103(SQLiteDatabase db) {
        // Aurora xuyong 2014-07-29 modified for database upgrade start
        checkTablesHasRejectColumns(db);
        try {
            addNewCommonTrigger(db);
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
        // Aurora xuyong 2014-07-29 modified for database upgrade end
    }
    // Auarora xuyong 2014-06-18 added for Reject feature end
    // Auarora xuyong 2014-06-19 modified for database upgrade start
    private void upgradeDatabaseToVersion550104(SQLiteDatabase db) {
        // Aurora xuyong 2014-07-29 modified for database upgrade start
        checkTablesHasRejectColumns(db);
        try {
            addNewCommonTrigger(db);
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
        // Aurora xuyong 2014-07-29 modified for database upgrade end
    }
    // Aurora xuyong 2014-07-03 added for reject feature start
    private void upgradeDatabaseToVersion550105(SQLiteDatabase db) {
        // Aurora xuyong 2014-07-29 modified for database upgrade start
        checkTablesHasRejectColumns(db);
        try {
            addNewCommonTrigger(db);
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
        // Aurora xuyong 2014-07-29 modified for database upgrade end
    }
    // Aurora xuyong 2014-07-04 added for aurora's new feature start
    private void upgradeDatabaseToVersion550106(SQLiteDatabase db) {
        // Aurora xuyong 2014-07-29 modified for database upgrade start
        checkTablesHasRejectColumns(db);
        try {
            addNewCommonTrigger(db);
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
        // Aurora xuyong 2014-07-29 modified for database upgrade end
    }
    // Aurora xuyong 2014-07-04 added for aurora's new feature end
     // Aurora xuyong 2014-07-05 added for reject feature start
    private void upgradeDatabaseToVersion600000(SQLiteDatabase db) {
        // Aurora xuyong 2014-07-29 modified for database upgrade start
        checkTablesHasRejectColumns(db);
        try {
            addNewCommonTrigger(db);
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
        // Aurora xuyong 2014-07-29 modified for database upgrade end
    }
     // Aurora xuyong 2014-07-05 added for reject feature end
    // Aurora xuyong 2014-07-09 added for reject feature start
    private void upgradeDatabaseToVersion600001(SQLiteDatabase db) {
        // Aurora xuyong 2014-07-29 modified for database upgrade start
        checkTablesHasRejectColumns(db);
        try {
            addNewCommonTrigger(db);
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
        // Aurora xuyong 2014-07-29 modified for database upgrade end
    }
    // Aurora xuyong 2014-07-19 added for bug #6656 start
    private void upgradeDatabaseToVersion600002(SQLiteDatabase db) {
        // Aurora xuyong 2014-07-29 modified for database upgrade start
        checkTablesHasRejectColumns(db);
        try {
            addNewCommonTrigger(db);
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
        // Aurora xuyong 2014-07-29 modified for database upgrade end
    }
    // Aurora xuyong 2014-07-21 added for reject feature start
    private void upgradeDatabaseToVersion600003(SQLiteDatabase db) {
        // Aurora xuyong 2014-07-29 modified for database upgrade start
        checkTablesHasRejectColumns(db);
        try {
            addNewCommonTrigger(db);
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
        // Aurora xuyong 2014-07-29 modified for database upgrade end
    }
    // Aurora xuyong 2014-07-23 modified for database upgrade start
    private void upgradeDatabaseToVersion600004(SQLiteDatabase db) {
        checkTablesHasRejectColumns(db);
        try {
            addNewCommonTrigger(db);
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
    }
    // Aurora xuyong 2014-07-23 modified for database upgrade end
    // Aurora xuyong 2014-07-21 added for reject feature end
    // Aurora xuyong 2014-07-19 added for bug #6656 end
 // Aurora xuyong 2014-07-29 added for database upgrade start
    private void upgradeDatabaseToVersion600005(SQLiteDatabase db) {
        checkTablesHasRejectColumns(db);
        try {
            addNewCommonTrigger(db);
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
    }
    // Aurora xuyong 2014-08-05 added for reject start
    private void upgradeDatabaseToVersion600006(SQLiteDatabase db) {
        checkTablesHasRejectColumns(db);
        try {
            addNewCommonTrigger(db);
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
    }
    // Aurora xuyong 2014-08-05 added for reject end
    //Aurora xuyong 2014-09-02 added for whitelist feature start
    private void upgradeDatabaseToVersion600007(SQLiteDatabase db) {
        initWhiteListTable(db);
    }
    //Aurora xuyong 2014-09-02 added for whitelist feature end
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private void upgradeDatabaseToVersion700000(SQLiteDatabase db) {
        checkTablesHasPrivacyColumns(db);
        initCurrentNumberIdTable(db);
        addPrivacyTrigger(db);
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end

            //Aurora yudingmin 2014-11-03 added for sync feature start
    private void upgradeDatabaseToVersion700001(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE pdu ADD COLUMN weather_info TEXT DEFAULT NULL;");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE sms ADD COLUMN weather_info TEXT DEFAULT NULL;");
        } catch (SQLiteException e) {
            e.printStackTrace();        
        }
        try {
            db.execSQL("ALTER TABLE cellbroadcast ADD COLUMN weather_info TEXT DEFAULT NULL;");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE wappush ADD COLUMN weather_info TEXT DEFAULT NULL;");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE " + MmsSmsSyncProvider.TABLE_SYNC + " ADD COLUMN " + MmsSmsSyncProvider.Sync.LOCAL_FLAG + " TEXT;");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        createMmsSmsSyncOld(db);
        Cursor cursor = db.query(MmsProvider.TABLE_PDU, new String[]{Mms._ID}, null, null, null, null, null);
        ContentValues values = new ContentValues();
        if(cursor != null){
            while(cursor.moveToNext()){
                long pduId = cursor.getLong(0);
                Cursor partCursor = db.query(MmsProvider.TABLE_PART, new String[]{Part._ID, Part._DATA}, Part.MSG_ID + "=" + pduId + " and " + Part._DATA + " is not null", null, null, null, null);
                values.clear();
                values.put(Sync.MSG_TYPE, MmsSmsSyncProvider.MMS_TYPE);
                values.put(Sync.MSG_ID, pduId);
                values.put(Sync.UPDATE_DATE, System.currentTimeMillis());
                db.beginTransaction();
                try{
                    db.insert(MmsSmsSyncProvider.TABLE_SYNC, null, values);
                    if(partCursor != null){
                        while(partCursor.moveToNext()){
                            if(!TextUtils.isEmpty(partCursor.getString(1))){
                                values.clear();
                                values.put(SyncPart.PART_ID, partCursor.getString(0));
                                db.insert(MmsSmsSyncProvider.PART_SYNC, null, values);
                            }
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    if(partCursor != null && !partCursor.isClosed()){
                        partCursor.close();
                    }
                    db.endTransaction();
                }
            }
            cursor.close();
        }
        cursor = db.query(SmsProvider.TABLE_SMS, null, null, null, null, null, null);
        if(cursor != null){
            while(cursor.moveToNext()){
                values.clear();
                values.put(Sync.MSG_TYPE, MmsSmsSyncProvider.SMS_TYPE);
                values.put(Sync.MSG_ID, cursor.getString(0));
                values.put(Sync.UPDATE_DATE, System.currentTimeMillis());
                db.insert(MmsSmsSyncProvider.TABLE_SYNC, null, values);
            }
            cursor.close();
        }
        db.execSQL(MmsSmsSyncProvider.CREATE_SYNC_VIEW_OLD_SQL);
    }
    
    private void upgradeDatabaseToVersion700002(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + MmsSmsSyncProvider.TABLE_SYNC + " ADD COLUMN " + MmsSmsSyncProvider.Sync.LOCAL_FLAG + " TEXT;");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        db.execSQL("DROP VIEW IF EXISTS " + MmsSmsSyncProvider.SYNC_VIEW);
        createMmsSmsSyncView(db);
    }
    
    private void upgradeDatabaseToVersion700003(SQLiteDatabase db){
        Cursor cursor = db.query(MmsSmsSyncProvider.TABLE_SYNC, new String[]{MmsSmsSyncProvider.Sync._ID}, null, null, null, null, null);
        if(cursor != null){
            if(cursor.getCount() > 0){
                ContentValues values = new ContentValues();
                long createTime = System.currentTimeMillis();
                while(cursor.moveToNext()){
                    values.clear();
                    String id = cursor.getString(0);
                    String localFlag = MmsSmsSyncProvider.createLocalFlag(id, createTime);
                    values.put(Sync.LOCAL_FLAG, localFlag);
                    db.update(MmsSmsSyncProvider.TABLE_SYNC, values, MmsSmsSyncProvider.Sync._ID + "=" + id, null);
                }
            }
            cursor.close();
        }
    }
            //Aurora yudingmin 2014-11-03 added for sync feature end
    
    private void upgradeDatabaseToVersion700004(SQLiteDatabase db){
        try {
            db.execSQL("ALTER TABLE pdu ADD COLUMN weather_info TEXT DEFAULT NULL;");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE sms ADD COLUMN weather_info TEXT DEFAULT NULL;");
        } catch (SQLiteException e) {
            e.printStackTrace();        
        }
        try {
            db.execSQL("ALTER TABLE cellbroadcast ADD COLUMN weather_info TEXT DEFAULT NULL;");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE wappush ADD COLUMN weather_info TEXT DEFAULT NULL;");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE " + MmsSmsSyncProvider.TABLE_SYNC + " ADD COLUMN " + MmsSmsSyncProvider.Sync.LOCAL_FLAG + " TEXT;");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        db.execSQL("DROP VIEW IF EXISTS " + MmsSmsSyncProvider.SYNC_VIEW);
        createMmsSmsSyncView(db);
    }
    
    private void upgradeDatabaseToVersion700005(SQLiteDatabase db){
        try {
            db.execSQL("ALTER TABLE raw ADD COLUMN sub_id LONG DEFAULT -1");
            db.execSQL("ALTER TABLE raw ADD COLUMN recv_time INTEGER");
            db.execSQL("ALTER TABLE raw ADD COLUMN upload_flag INTEGER");
        } catch (SQLiteException e) {
            e.printStackTrace();        
        }
        db.execSQL("DROP VIEW IF EXISTS " + MmsSmsSyncProvider.SYNC_VIEW);
        createMmsSmsSyncView(db);
    }
    
 // Aurora xuyong 2014-07-29 added for database upgrade start
    private void checkTablesHasRejectColumns(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE pdu ADD COLUMN reject INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE sms ADD COLUMN reject INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            e.printStackTrace();        
        }
        try {
            db.execSQL("ALTER TABLE threads ADD COLUMN reject INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            e.printStackTrace();        
        }
        try {
            db.execSQL("ALTER TABLE threads ADD COLUMN concurrent_resume INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE cellbroadcast ADD COLUMN reject INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE part ADD COLUMN reject INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE addr ADD COLUMN reject INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE wappush ADD COLUMN reject INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
       // Aurora xuyong 2014-07-23 modified for database upgrade start
        try {
            db.execSQL("ALTER TABLE sms ADD COLUMN update_threads INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE pdu ADD COLUMN update_threads INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE cellbroadcast ADD COLUMN update_threads INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
       // Aurora xuyong 2014-07-23 modified for database upgrade end
       // Aurora xuyong 2014-07-30 added for database upgrade start
        try {
            db.execSQL("ALTER TABLE threads ADD COLUMN deleted INTEGER DEFAULT 0");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE threads ADD COLUMN unread_count INTEGER DEFAULT 0");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
       // Aurora xuyong 2014-07-30 added for database upgrade end
    }
    // Aurora xuyong 2014-07-09 added for reject feature end
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private void checkTablesHasPrivacyColumns(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE pdu ADD COLUMN is_privacy INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE sms ADD COLUMN is_privacy INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            e.printStackTrace();        
        }
        try {
            db.execSQL("ALTER TABLE threads ADD COLUMN is_privacy INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            e.printStackTrace();        
        }
        try {
            db.execSQL("ALTER TABLE cellbroadcast ADD COLUMN is_privacy INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE part ADD COLUMN is_privacy INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE addr ADD COLUMN is_privacy INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
        try {
            db.execSQL("ALTER TABLE wappush ADD COLUMN is_privacy INTEGER DEFAULT 0;");
        } catch (SQLiteException e) {
            // if the old table has the column, catch this exception.
            e.printStackTrace();
        }
    }
    
    private void addPrivacyTrigger(SQLiteDatabase db) {
        try {
            final String update_addr_privacy_after_pdu = "UPDATE addr SET is_privacy = NEW.is_privacy WHERE msg_id = NEW._id; ";
            final String update_part_privacy_after_pdu = "UPDATE part SET is_privacy = NEW.is_privacy WHERE mid = NEW._id; ";
            db.execSQL("CREATE TRIGGER refer_update_privacy_on_mms " +
                    "AFTER UPDATE OF is_privacy ON pdu " +
                    "BEGIN " +
                        update_addr_privacy_after_pdu +
                        update_part_privacy_after_pdu +
                    "END");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    private void addNewCommonTrigger(SQLiteDatabase db) {
        try {
          // Aurora xuyong 2014-07-21 modified for reject feature start
            final String update_sms_reject_after_thread  = " UPDATE sms SET reject = NEW.reject, update_threads = 1 WHERE (type = 1 AND thread_id = NEW._id); ";
            final String update_pdu_reject_after_thread  = " UPDATE pdu SET reject = NEW.reject, update_threads = 1 WHERE (msg_box = 1 AND thread_id = NEW._id); ";
            final String update_cel_reject_after_thread  = " UPDATE cellbroadcast SET reject = NEW.reject, update_threads= 1 WHERE thread_id = NEW._id; ";
          // Aurora xuyong 2014-07-21 modified for reject feature end
            db.execSQL("DROP TRIGGER IF EXISTS refer_update_reject_on_thread;");
          // Aurora xuyong 2014-08-05 deleted for reject start
            /*db.execSQL("CREATE TRIGGER refer_update_reject_on_thread " +
                     "AFTER UPDATE OF reject, concurrent_resume ON threads " +
                   "WHEN NEW.concurrent_resume = 1 " +
                     "BEGIN " +
                         update_sms_reject_after_thread +
                         update_pdu_reject_after_thread +
                         update_cel_reject_after_thread +
                   "END;");*/
          // Aurora xuyong 2014-08-05 deleted for reject end
            final String update_addr_reject_after_pdu = "UPDATE addr SET reject = NEW.reject WHERE msg_id = NEW._id; ";
            final String update_part_reject_after_pdu = "UPDATE part SET reject = NEW.reject WHERE mid = NEW._id; ";
            db.execSQL("DROP TRIGGER IF EXISTS refer_update_reject_on_mms;");
            db.execSQL("CREATE TRIGGER refer_update_reject_on_mms " +
                    "AFTER UPDATE OF reject ON pdu " +
                    "BEGIN " +
                        update_addr_reject_after_pdu +
                        update_part_reject_after_pdu +
                    "END");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        try {
            db.execSQL("DROP TRIGGER IF EXISTS sms_update_on_reject;");
            db.execSQL("CREATE TRIGGER sms_update_on_reject" +
                       " AFTER UPDATE OF reject ON sms " +
                   // Aurora xuyong 2014-07-21 added for reject feature start
                       " WHEN NEW.update_threads = 0 " +
                   // Aurora xuyong 2014-07-21 added for reject feature end
                       " BEGIN" +
                       "  UPDATE threads SET date = (SELECT date FROM (" +
                       "           SELECT date * 1000 AS date, thread_id FROM pdu WHERE (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0 " +
                       "           UNION" +
                       "           SELECT date, thread_id FROM sms WHERE sms.reject = 0) WHERE thread_id = NEW.thread_id ORDER BY date DESC LIMIT 1) " +
                       "  WHERE threads._id = NEW.thread_id; " +
                       "  UPDATE threads SET message_count = " +
                       "     (SELECT COUNT(sms._id) FROM sms LEFT JOIN threads ON threads._id = thread_id WHERE thread_id = NEW.thread_id AND sms.type != 3 AND sms.reject = 0)" +
                       "      +      " +
                       "     (SELECT COUNT(pdu._id) FROM pdu LEFT JOIN threads ON threads._id = thread_id WHERE thread_id = NEW.thread_id AND (m_type=132 OR m_type=130 OR m_type=128) AND msg_box != 3 AND pdu.reject = 0) " +
                       "  WHERE threads._id = NEW.thread_id; " +
                       "  UPDATE threads SET read = " +
                       "       CASE ((SELECT COUNT(*) FROM sms WHERE read = 0 AND thread_id = threads._id AND sms.reject = 0) + (SELECT COUNT(*) FROM pdu WHERE read = 0 AND thread_id = threads._id AND (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0))" +
                       "       WHEN 0 THEN 1" +
                       "       ELSE 0" +
                       "       END" +
                       "  WHERE threads._id = NEW.thread_id;" +
                       "  UPDATE threads SET readcount = (SELECT count(_id) FROM (" +
                       "                  SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM sms WHERE ((read=1) AND thread_id = NEW.thread_id AND (type != 3) AND sms.reject = 0) " +
                       "                  UNION" +
                       "                  SELECT DISTINCT date * 1000 AS normalized_date, pdu._id, read FROM pdu LEFT JOIN pending_msgs ON pdu._id = pending_msgs.msg_id WHERE (pdu.reject = 0 AND (read=1) AND thread_id = NEW.thread_id AND msg_box != 3 AND (msg_box != 3 AND (m_type = 128 OR m_type = 132 OR m_type = 130))) " +
                       "                  UNION" +
                       "                  SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM cellbroadcast WHERE ((read=1) AND thread_id = NEW.thread_id AND cellbroadcast.reject = 0) ORDER BY normalized_date ASC))" +
                       "  WHERE threads._id = NEW.thread_id;" +
                       "  UPDATE threads SET snippet = (SELECT snippet FROM (" +
                       "           SELECT date * 1000 AS date, sub AS snippet, thread_id FROM pdu WHERE (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0 " +
                       "           UNION" +
                       "           SELECT date, body AS snippet, thread_id FROM sms WHERE sms.reject = 0) WHERE thread_id = NEW.thread_id ORDER BY date DESC LIMIT 1) " +
                       "  WHERE threads._id = NEW.thread_id;  " +
                       "  UPDATE threads SET snippet_cs = (SELECT snippet_cs FROM (SELECT date * 1000 AS date, sub_cs AS snippet_cs, thread_id FROM pdu WHERE (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0 " +
                       "                                      UNION" +
                       "                                      SELECT date, 0 AS snippet_cs, thread_id FROM sms WHERE sms.reject = 0) " +
                       "                                      WHERE thread_id = NEW.thread_id ORDER BY date DESC LIMIT 1) " +
                       "  WHERE threads._id = NEW.thread_id;" +
                    // Aurora xuyong 2014-07-05 modified for reject feature start
                       "  UPDATE threads SET has_attachment = 1 WHERE _id IN (SELECT pdu.thread_id FROM part JOIN pdu WHERE part.ct != 'text/plain' AND part.ct != 'application/smil' AND part.mid = pdu._id); " +
                       "  UPDATE threads SET sim_id = (SELECT sim_id FROM (" +
                       "           SELECT date * 1000 AS date, sim_id, thread_id FROM pdu WHERE (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0 " +
                       "           UNION" +
                       "           SELECT date, sim_id, thread_id FROM sms WHERE sms.reject = 0) WHERE thread_id = NEW.thread_id ORDER BY date DESC LIMIT 1) " +
                       "  WHERE threads._id = NEW.thread_id; " +
                    // Aurora xuyong 2014-07-05 modified for reject feature end
                       " END;");
        } catch (SQLiteException e) {
            e.printStackTrace();    
        }
        try {
            db.execSQL("DROP TRIGGER IF EXISTS pdu_update_thread_on_reject;");
            db.execSQL("CREATE TRIGGER pdu_update_thread_on_reject " +
                       " AFTER UPDATE OF reject ON pdu " +
                   // Aurora xuyong 2014-07-21 modified for reject feature start
                       " WHEN (NEW.m_type=132 OR NEW.m_type=130 OR NEW.m_type=128) AND NEW.update_threads = 0 " +
                   // Aurora xuyong 2014-07-21 modified for reject feature end
                       " BEGIN " +
                       "     UPDATE threads SET date = (SELECT date FROM (" +
                       "           SELECT date * 1000 AS date, thread_id FROM pdu WHERE (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0 " +
                       "           UNION" +
                       "           SELECT date, thread_id FROM sms WHERE sms.reject = 0) WHERE thread_id = NEW.thread_id ORDER BY date DESC LIMIT 1) " + 
                       "     WHERE threads._id = NEW.thread_id;" + 
                       "     UPDATE threads SET message_count = (SELECT COUNT(sms._id) FROM sms LEFT JOIN threads ON threads._id = thread_id WHERE thread_id = NEW.thread_id AND sms.type != 3 AND sms.reject = 0)" +
                       "                                         +  " +
                       "                                        (SELECT COUNT(pdu._id) FROM pdu LEFT JOIN threads ON threads._id = thread_id WHERE thread_id = NEW.thread_id AND (m_type=132 OR m_type=130 OR m_type=128) AND msg_box != 3 AND pdu.reject = 0)" +
                       "     WHERE threads._id = NEW.thread_id; " +
                       "     UPDATE threads SET read = " +
                       "                CASE ((SELECT COUNT(*) FROM pdu WHERE read = 0 AND thread_id = threads._id AND (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0) + (SELECT COUNT(*) FROM sms WHERE read = 0 AND thread_id = threads._id AND sms.reject = 0)) " +
                       "                WHEN 0 THEN 1    " +
                       "                ELSE 0" +
                       "                END" +
                       "     WHERE threads._id = NEW.thread_id; " +
                       "     UPDATE threads SET readcount = (SELECT count(_id) FROM (" +
                       "             SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM sms WHERE ((read=1) AND thread_id = NEW.thread_id AND (type != 3) AND sms.reject = 0) " +
                       "             UNION" +
                       "             SELECT DISTINCT date * 1000 AS normalized_date, pdu._id, read FROM pdu LEFT JOIN pending_msgs ON pdu._id = pending_msgs.msg_id WHERE (pdu.reject = 0 AND (read=1) AND thread_id = NEW.thread_id AND msg_box != 3 AND (msg_box != 3 AND (m_type = 128 OR m_type = 132 OR m_type = 130)))  " +
                       "             UNION" +
                       "             SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM cellbroadcast WHERE ((read=1) AND thread_id = NEW.thread_id AND cellbroadcast.reject = 0) ORDER BY normalized_date ASC))" +
                       "     WHERE threads._id = NEW.thread_id;" +
                       "     UPDATE threads SET snippet = (SELECT snippet FROM (" +
                       "              SELECT date * 1000 AS date, sub AS snippet, thread_id FROM pdu WHERE (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0 " +
                       "              UNION" +
                       "              SELECT date, body AS snippet, thread_id FROM sms WHERE sms.reject = 0) WHERE thread_id = NEW.thread_id ORDER BY date DESC LIMIT 1) " +
                       "     WHERE threads._id = NEW.thread_id;  " +
                       "     UPDATE threads SET snippet_cs = (SELECT snippet_cs FROM (SELECT date * 1000 AS date, sub_cs AS snippet_cs, thread_id FROM pdu WHERE (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0 " +
                       "                                      UNION" +
                       "                                      SELECT date, 0 AS snippet_cs, thread_id FROM sms WHERE sms.reject = 0) " +
                       "                                      WHERE thread_id = NEW.thread_id ORDER BY date DESC LIMIT 1) " +
                       "     WHERE threads._id = NEW.thread_id;" +
                       "     UPDATE threads SET has_attachment = 1 WHERE _id IN (SELECT pdu.thread_id FROM part JOIN pdu WHERE part.ct != 'text/plain' AND part.ct != 'application/smil' AND part.mid = pdu._id); " +
                    // Aurora xuyong 2014-07-05 modified for reject feature start
                       "     UPDATE threads SET sim_id = (SELECT sim_id FROM (" +
                       "           SELECT date * 1000 AS date, sim_id, thread_id FROM pdu WHERE (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0 " +
                       "           UNION" +
                       "           SELECT date, sim_id, thread_id FROM sms WHERE sms.reject = 0) WHERE thread_id = NEW.thread_id ORDER BY date DESC LIMIT 1) " +
                       "     WHERE threads._id = NEW.thread_id; " +
                    // Aurora xuyong 2014-07-05 modified for reject feature end
                       " END;");
        } catch (Exception e) {
            e.printStackTrace();
        }
        changeTriggerForReject(db);
    }
    
    private void changeTriggerForReject(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TRIGGER IF EXISTS sms_update_thread_date_subject_on_update;");
            db.execSQL("CREATE TRIGGER sms_update_thread_date_subject_on_update" +
                       " AFTER  UPDATE OF date, body, type  ON sms" +
                       " BEGIN" +
                       "  UPDATE threads SET date = new.date, snippet = new.body, sim_id = new.sim_id, snippet_cs = 0 WHERE threads._id = new.thread_id AND NEW.reject = 0; " +
                       "  UPDATE threads SET message_count = " +
                       "     (SELECT COUNT(sms._id) FROM sms LEFT JOIN threads ON threads._id = thread_id WHERE thread_id = new.thread_id AND sms.type != 3 AND sms.reject = 0)" +
                       "      +      " +
                       "     (SELECT COUNT(pdu._id) FROM pdu LEFT JOIN threads ON threads._id = thread_id WHERE thread_id = new.thread_id AND (m_type=132 OR m_type=130 OR m_type=128) AND msg_box != 3 AND pdu.reject = 0) " +
                       "  WHERE threads._id = new.thread_id; " +
                       "  UPDATE threads SET read = " +
                       "       CASE (SELECT COUNT(*) FROM sms WHERE read = 0 AND thread_id = threads._id AND sms.reject = 0)" +
                       "       WHEN 0 THEN 1" +
                       "       ELSE 0" +
                       "       END" +
                       "  WHERE threads._id = new.thread_id;" +
                       "  UPDATE threads SET readcount = (SELECT count(_id) FROM (" +
                       "                  SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM sms WHERE ((read=1) AND thread_id = new.thread_id AND (type != 3) AND sms.reject = 0) " +
                       "                  UNION" +
                       "                  SELECT DISTINCT date * 1000 AS normalized_date, pdu._id, read FROM pdu LEFT JOIN pending_msgs ON pdu._id = pending_msgs.msg_id WHERE (pdu.reject = 0 AND (read=1) AND thread_id = new.thread_id AND msg_box != 3 AND (msg_box != 3 AND (m_type = 128 OR m_type = 132 OR m_type = 130))) " +
                       "                  UNION" +
                       "                  SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM cellbroadcast WHERE ((read=1) AND thread_id = new.thread_id AND cellbroadcast.reject = 0) ORDER BY normalized_date ASC))" +
                       "  WHERE threads._id = new.thread_id;" +
                       " END;");
        } catch (SQLiteException e) {
            e.printStackTrace();    
        }
        try {
            db.execSQL("DROP TRIGGER IF EXISTS sms_update_thread_on_insert;");
            db.execSQL("CREATE TRIGGER sms_update_thread_on_insert" +
                       " AFTER INSERT ON sms" +
                       " BEGIN " +
                       "     UPDATE threads SET date = new.date, snippet = new.body, sim_id = new.sim_id, snippet_cs = 0  WHERE threads._id = new.thread_id AND NEW.reject = 0;" +
                       "     UPDATE threads SET message_count = " +
                       "         (SELECT COUNT(sms._id) FROM sms LEFT JOIN threads ON threads._id = thread_id WHERE thread_id = new.thread_id AND sms.type != 3 AND sms.reject = 0)" +
                       "          +      " +
                       "         (SELECT COUNT(pdu._id) FROM pdu LEFT JOIN threads ON threads._id = thread_id WHERE thread_id = new.thread_id AND (m_type=132 OR m_type=130 OR m_type=128) AND msg_box != 3 AND pdu.reject = 0) " +
                       "  WHERE threads._id = new.thread_id;" +
                       "     UPDATE threads SET read = " +
                       "         CASE (SELECT COUNT(*) FROM sms WHERE read = 0 AND thread_id = threads._id AND sms.reject = 0) " +
                       "         WHEN 0 THEN 1   " +
                       "         ELSE 0 " +
                       "         END  " +
                       "     WHERE threads._id = new.thread_id; " +
                       "     UPDATE threads SET readcount = (SELECT count(_id) FROM (" +
                       "                 SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM sms WHERE ((read=1) AND thread_id = new.thread_id AND (type != 3) AND sms.reject = 0) " +
                       "                 UNION" +
                       "                 SELECT DISTINCT date * 1000 AS normalized_date, pdu._id, read FROM pdu LEFT JOIN pending_msgs ON pdu._id = pending_msgs.msg_id WHERE (pdu.reject = 0 AND (read=1) AND thread_id = new.thread_id AND msg_box != 3 AND (msg_box != 3 AND (m_type = 128 OR m_type = 132 OR m_type = 130)))" +
                       "                 UNION" +
                       "                 SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM cellbroadcast WHERE ((read=1) AND thread_id = new.thread_id AND cellbroadcast.reject = 0) ORDER BY normalized_date ASC)) " +
                       "     WHERE threads._id = new.thread_id;" +
                       " END;");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        try {
            db.execSQL("DROP TRIGGER IF EXISTS sms_update_thread_read_on_update;");
            db.execSQL("CREATE TRIGGER sms_update_thread_read_on_update" +
                       " AFTER UPDATE OF read ON sms" +
                       " BEGIN  " +
                       "     UPDATE threads SET read = " +
                       "            CASE (SELECT COUNT(*) FROM sms WHERE read = 0 AND thread_id = threads._id AND sms.reject = 0) " +
                       "            WHEN 0 THEN 1 " +
                       "            ELSE 0  " +
                       "            END " +
                       "     WHERE threads._id = new.thread_id;  " +
                       "     UPDATE threads SET readcount = (SELECT count(_id) FROM (" +
                       "                 SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM sms WHERE ((read=1) AND thread_id = new.thread_id AND (type != 3) AND sms.reject = 0) " +
                       "                 UNION" +
                       "                 SELECT DISTINCT date * 1000 AS normalized_date, pdu._id, read FROM pdu LEFT JOIN pending_msgs ON pdu._id = pending_msgs.msg_id WHERE (pdu.reject = 0 AND (read=1) AND thread_id = new.thread_id AND msg_box != 3 AND (msg_box != 3 AND (m_type = 128 OR m_type = 132 OR m_type = 130))) " +
                       "                 UNION " +
                       "                 SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM cellbroadcast WHERE ((read=1) AND thread_id = new.thread_id AND cellbroadcast.reject = 0) ORDER BY normalized_date ASC))" +
                       "     WHERE threads._id = new.thread_id;" +
                       " END;");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        try {
            db.execSQL("DROP TRIGGER IF EXISTS pdu_update_thread_date_subject_on_update;");
            db.execSQL("CREATE TRIGGER pdu_update_thread_date_subject_on_update" +
                       " AFTER UPDATE OF date, sub, msg_box ON pdu" +
                       " WHEN new.m_type=132 OR new.m_type=130 OR new.m_type=128" +
                       " BEGIN " +
                       "     UPDATE threads SET date = (strftime('%s','now') * 1000), snippet = new.sub, sim_id = new.sim_id, snippet_cs = new.sub_cs WHERE (new.m_type=132 OR new.m_type=130 OR new.m_type=128) AND threads._id = new.thread_id AND NEW.reject = 0;" +
                       "     UPDATE threads SET message_count = (SELECT COUNT(sms._id) FROM sms LEFT JOIN threads ON threads._id = thread_id WHERE thread_id = new.thread_id AND sms.type != 3 AND sms.reject = 0)" +
                       "                                         +  " +
                       "                                        (SELECT COUNT(pdu._id) FROM pdu LEFT JOIN threads ON threads._id = thread_id WHERE thread_id = new.thread_id AND (m_type=132 OR m_type=130 OR m_type=128) AND msg_box != 3 AND pdu.reject = 0)" +
                       "     WHERE threads._id = new.thread_id; " +
                       "     UPDATE threads SET read = " +
                       "                CASE (SELECT COUNT(*) FROM pdu WHERE read = 0 AND thread_id = threads._id AND (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0) " +
                       "                WHEN 0 THEN 1    " +
                       "                ELSE 0" +
                       "                END" +
                       "     WHERE threads._id = new.thread_id; " +
                       "     UPDATE threads SET readcount = (SELECT count(_id) FROM (" +
                       "             SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM sms WHERE ((read=1) AND thread_id = new.thread_id AND (type != 3) AND sms.reject = 0) " +
                       "             UNION" +
                       "             SELECT DISTINCT date * 1000 AS normalized_date, pdu._id, read FROM pdu LEFT JOIN pending_msgs ON pdu._id = pending_msgs.msg_id WHERE (pdu.reject = 0 AND (read=1) AND thread_id = new.thread_id AND msg_box != 3 AND (msg_box != 3 AND (m_type = 128 OR m_type = 132 OR m_type = 130)))  " +
                       "             UNION" +
                       "             SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM cellbroadcast WHERE ((read=1) AND thread_id = new.thread_id AND cellbroadcast.reject = 0) ORDER BY normalized_date ASC))" +
                       "     WHERE threads._id = new.thread_id;" +
                       " END;");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            db.execSQL("DROP TRIGGER IF EXISTS pdu_update_thread_on_delete;");
            db.execSQL("CREATE TRIGGER pdu_update_thread_on_delete" +
                       " AFTER DELETE ON pdu" +
                       " BEGIN  " +
                   // Aurora xuyong 2014-07-19 modified for bug #6656 start
                       "     UPDATE threads SET date = (SELECT date FROM (" +
                       "           SELECT date * 1000 AS date, thread_id FROM pdu WHERE (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0 " +
                       "           UNION" +
                       "           SELECT date, thread_id FROM sms WHERE sms.reject = 0) WHERE thread_id = OLD.thread_id ORDER BY date DESC LIMIT 1) " + 
                       "     WHERE threads._id = OLD.thread_id;" +
                   // Aurora xuyong 2014-07-19 modified for bug #6656 end
                       "     UPDATE threads SET message_count = (" +
                       "                SELECT COUNT(sms._id) FROM sms LEFT JOIN threads ON threads._id = thread_id WHERE thread_id = old.thread_id AND sms.type != 3 AND sms.reject = 0)" +
                       "                +" +
                       "               (SELECT COUNT(pdu._id) FROM pdu LEFT JOIN threads ON threads._id = thread_id WHERE thread_id = old.thread_id AND (m_type=132 OR m_type=130 OR m_type=128) AND msg_box != 3 AND pdu.reject = 0) " +
                       "     WHERE threads._id = old.thread_id; " +
                       "     UPDATE threads SET snippet = (SELECT snippet FROM (" +
                       "              SELECT date * 1000 AS date, sub AS snippet, thread_id FROM pdu WHERE (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0 " +
                       "              UNION" +
                       "              SELECT date, body AS snippet, thread_id FROM sms WHERE sms.reject = 0) WHERE thread_id = OLD.thread_id ORDER BY date DESC LIMIT 1) " +
                       "     WHERE threads._id = OLD.thread_id;  " +
                       "     UPDATE threads SET sim_id = (SELECT sim_id FROM (SELECT date * 1000 AS date, sim_id, thread_id FROM pdu WHERE (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0" +
                       "                                                      UNION" +
                       "                                                      SELECT date, sim_id, thread_id FROM sms WHERE sms.reject = 0)" +
                       "                                                      WHERE thread_id = OLD.thread_id ORDER BY date DESC LIMIT 1) " +
                       "     WHERE threads._id = OLD.thread_id;  " +
                       "     UPDATE threads SET snippet_cs = (SELECT snippet_cs FROM (SELECT date * 1000 AS date, sub_cs AS snippet_cs, thread_id FROM pdu WHERE (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0 " +
                       "                                      UNION" +
                       "                                      SELECT date, 0 AS snippet_cs, thread_id FROM sms WHERE sms.reject = 0) " +
                       "                                      WHERE thread_id = OLD.thread_id ORDER BY date DESC LIMIT 1) " +
                       "     WHERE threads._id = OLD.thread_id;" +
                       " END;");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            db.execSQL("DROP TRIGGER IF EXISTS pdu_update_thread_on_insert;");
            db.execSQL("CREATE TRIGGER pdu_update_thread_on_insert" +
                       " AFTER INSERT ON pdu " +
                       " WHEN new.m_type=132 OR new.m_type=130 OR new.m_type=128" +
                       " BEGIN " +
                       "     UPDATE threads SET date = (strftime('%s','now') * 1000), snippet = new.sub, sim_id = new.sim_id, snippet_cs = new.sub_cs WHERE (new.m_type=132 OR new.m_type=130 OR new.m_type=128) AND threads._id = new.thread_id AND NEW.reject = 0;" +
                       "     UPDATE threads SET message_count = (SELECT COUNT(sms._id) FROM sms LEFT JOIN threads ON threads._id = thread_id WHERE thread_id = new.thread_id AND sms.type != 3 AND sms.reject = 0)" +
                       "                                         + " +
                       "                                        (SELECT COUNT(pdu._id) FROM pdu LEFT JOIN threads ON threads._id = thread_id WHERE thread_id = new.thread_id AND (m_type=132 OR m_type=130 OR m_type=128) AND msg_box != 3 AND pdu.reject = 0) " +
                       "     WHERE threads._id = new.thread_id;" +
                       "     UPDATE threads SET read = " +
                       "                    CASE (SELECT COUNT(*) FROM pdu WHERE read = 0 AND thread_id = threads._id AND (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0)" +
                       "                    WHEN 0 THEN 1  " +
                       "                    ELSE 0 " +
                       "                    END" +
                       "     WHERE threads._id = new.thread_id;  " +
                       "     UPDATE threads SET readcount = (SELECT count(_id) FROM (" +
                       "                               SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM sms WHERE ((read=1) AND thread_id = new.thread_id AND (type != 3) AND sms.reject = 0) " +
                       "                               UNION" +
                       "                               SELECT DISTINCT date * 1000 AS normalized_date, pdu._id, read FROM pdu LEFT JOIN pending_msgs ON pdu._id = pending_msgs.msg_id WHERE (pdu.reject = 0 AND (read=1) AND thread_id = new.thread_id AND msg_box != 3 AND (msg_box != 3 AND (m_type = 128 OR m_type = 132 OR m_type = 130))) " +
                       "                               UNION" +
                       "                               SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM cellbroadcast WHERE ((read=1) AND thread_id = new.thread_id AND cellbroadcast.reject = 0) ORDER BY normalized_date ASC))  " +
                       "     WHERE threads._id = new.thread_id;" +
                       " END;");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            db.execSQL("DROP TRIGGER IF EXISTS pdu_update_thread_read_on_update;");
            db.execSQL("CREATE TRIGGER pdu_update_thread_read_on_update" +
                       " AFTER UPDATE OF read ON pdu " +
                       " WHEN new.m_type=132 OR new.m_type=130 OR new.m_type=128" +
                       " BEGIN  " +
                       "     UPDATE threads SET read = " +
                       "               CASE (SELECT COUNT(*) FROM pdu WHERE read = 0 AND thread_id = threads._id AND (m_type=132 OR m_type=130 OR m_type=128) AND pdu.reject = 0) " +
                       "               WHEN 0 THEN 1 " +
                       "               ELSE 0  " +
                       "               END " +
                       "     WHERE threads._id = new.thread_id;  " +
                       "     UPDATE threads SET readcount = (SELECT count(_id) FROM (" +
                       "                             SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM sms WHERE ((read=1) AND thread_id = new.thread_id AND (type != 3) AND sms.reject = 0) " +
                       "                             UNION " +
                       "                             SELECT DISTINCT date * 1000 AS normalized_date, pdu._id, read FROM pdu LEFT JOIN pending_msgs ON pdu._id = pending_msgs.msg_id WHERE (pdu.reject = 0 AND (read=1) AND thread_id = new.thread_id AND msg_box != 3 AND (msg_box != 3 AND (m_type = 128 OR m_type = 132 OR m_type = 130))) " +
                       "                             UNION " +
                       "                             SELECT DISTINCT date * 1 AS normalized_date, _id, read FROM cellbroadcast WHERE ((read=1) AND thread_id = new.thread_id AND cellbroadcast.reject = 0) ORDER BY normalized_date ASC)) " +
                       "     WHERE threads._id = new.thread_id;" +
                       " END;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Aurora xuyong 2014-07-03 added for reject feature end
    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = super.getWritableDatabase();

        if (!sTriedAutoIncrement) {
            sTriedAutoIncrement = true;
            boolean hasAutoIncrementThreads = hasAutoIncrement(db, "threads");
            boolean hasAutoIncrementAddresses = hasAutoIncrement(db, "canonical_addresses");
            Log.d(TAG, "[getWritableDatabase] hasAutoIncrementThreads: " + hasAutoIncrementThreads +
                    " hasAutoIncrementAddresses: " + hasAutoIncrementAddresses);
            boolean autoIncrementThreadsSuccess = true;
            boolean autoIncrementAddressesSuccess = true;
            if (!hasAutoIncrementThreads) {
                db.beginTransaction();
                try {
                    if (false && sFakeLowStorageTest) {
                        Log.d(TAG, "[getWritableDatabase] mFakeLowStorageTest is true " +
                                " - fake exception");
                        throw new Exception("FakeLowStorageTest");
                    }
                    upgradeThreadsTableToAutoIncrement(db);     // a no-op if already upgraded
                    db.setTransactionSuccessful();
                } catch (Throwable ex) {
                    Log.e(TAG, "Failed to add autoIncrement to threads;: " + ex.getMessage(), ex);
                    autoIncrementThreadsSuccess = false;
                } finally {
                    db.endTransaction();
                }
            }
            if (!hasAutoIncrementAddresses) {
                db.beginTransaction();
                try {
                    if (false && sFakeLowStorageTest) {
                        Log.d(TAG, "[getWritableDatabase] mFakeLowStorageTest is true " +
                        " - fake exception");
                        throw new Exception("FakeLowStorageTest");
                    }
                    upgradeAddressTableToAutoIncrement(db);     // a no-op if already upgraded
                    db.setTransactionSuccessful();
                } catch (Throwable ex) {
                    Log.e(TAG, "Failed to add autoIncrement to canonical_addresses: " +
                            ex.getMessage(), ex);
                    autoIncrementAddressesSuccess = false;
                } finally {
                    db.endTransaction();
                }
            }
            if (autoIncrementThreadsSuccess && autoIncrementAddressesSuccess) {
                if (mLowStorageMonitor != null) {
                    // We've already updated the database. This receiver is no longer necessary.
                    Log.d(TAG, "Unregistering mLowStorageMonitor - we've upgraded");
                    mContext.unregisterReceiver(mLowStorageMonitor);
                    mLowStorageMonitor = null;
                }
            } else {
                if (sFakeLowStorageTest) {
                    sFakeLowStorageTest = false;
                }

                // We failed, perhaps because of low storage. Turn on a receiver to watch for
                // storage space.
                if (mLowStorageMonitor == null) {
                    Log.d(TAG, "[getWritableDatabase] turning on storage monitor");
                    mLowStorageMonitor = new LowStorageMonitor();
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_LOW);
                    intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
                    mContext.registerReceiver(mLowStorageMonitor, intentFilter);
                }
            }
        }
        return db;
    }

    // Determine whether a particular table has AUTOINCREMENT in its schema.
    private boolean hasAutoIncrement(SQLiteDatabase db, String tableName) {
        boolean result = false;
        String query = "SELECT sql FROM sqlite_master WHERE type='table' AND name='" +
                        tableName + "'";
        Cursor c = db.rawQuery(query, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    String schema = c.getString(0);
                    result = schema != null ? schema.contains("AUTOINCREMENT") : false;
                    Log.d(TAG, "[MmsSmsDb] tableName: " + tableName + " hasAutoIncrement: " +
                            schema + " result: " + result);
                }
            } finally {
                c.close();
            }
        }
        return result;
    }

    // upgradeThreadsTableToAutoIncrement() is called to add the AUTOINCREMENT keyword to
    // the threads table. This could fail if the user has a lot of conversations and not enough
    // storage to make a copy of the threads table. That's ok. This upgrade is optional. It'll
    // be called again next time the device is rebooted.
    private void upgradeThreadsTableToAutoIncrement(SQLiteDatabase db) {
        if (hasAutoIncrement(db, "threads")) {
            Log.d(TAG, "[MmsSmsDb] upgradeThreadsTableToAutoIncrement: already upgraded");
            return;
        }
        Log.d(TAG, "[MmsSmsDb] upgradeThreadsTableToAutoIncrement: upgrading");

        // Make the _id of the threads table autoincrement so we never re-use thread ids
        // Have to create a new temp threads table. Copy all the info from the old table.
        // Drop the old table and rename the new table to that of the old.
        db.execSQL("CREATE TABLE threads_temp (" +
                   Threads._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                   Threads.DATE + " INTEGER DEFAULT 0," +
                   Threads.MESSAGE_COUNT + " INTEGER DEFAULT 0," +
                   Threads.READCOUNT + " INTEGER DEFAULT 0," +
                   Threads.RECIPIENT_IDS + " TEXT," +
                   Threads.SNIPPET + " TEXT," +
                   Threads.SNIPPET_CHARSET + " INTEGER DEFAULT 0," +
                   Threads.READ + " INTEGER DEFAULT 1," +
                   Threads.TYPE + " INTEGER DEFAULT 0," +
                   Threads.ERROR + " INTEGER DEFAULT 0," +
                   Threads.HAS_ATTACHMENT + " INTEGER DEFAULT 0," +
                   Threads.STATUS + " INTEGER DEFAULT 0);");

        db.execSQL("INSERT INTO threads_temp SELECT * from threads;");
        db.execSQL("DROP TABLE threads;");
        db.execSQL("ALTER TABLE threads_temp RENAME TO threads;");
    }

    // upgradeAddressTableToAutoIncrement() is called to add the AUTOINCREMENT keyword to
    // the canonical_addresses table. This could fail if the user has a lot of people they've
    // messaged with and not enough storage to make a copy of the canonical_addresses table.
    // That's ok. This upgrade is optional. It'll be called again next time the device is rebooted.
    private void upgradeAddressTableToAutoIncrement(SQLiteDatabase db) {
        if (hasAutoIncrement(db, "canonical_addresses")) {
            Log.d(TAG, "[MmsSmsDb] upgradeAddressTableToAutoIncrement: already upgraded");
            return;
        }
        Log.d(TAG, "[MmsSmsDb] upgradeAddressTableToAutoIncrement: upgrading");

        // Make the _id of the canonical_addresses table autoincrement so we never re-use ids
        // Have to create a new temp canonical_addresses table. Copy all the info from the old
        // table. Drop the old table and rename the new table to that of the old.
        db.execSQL("CREATE TABLE canonical_addresses_temp (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "address TEXT);");

        db.execSQL("INSERT INTO canonical_addresses_temp SELECT * from canonical_addresses;");
        db.execSQL("DROP TABLE canonical_addresses;");
        db.execSQL("ALTER TABLE canonical_addresses_temp RENAME TO canonical_addresses;");
    }

    private class LowStorageMonitor extends BroadcastReceiver {

        public LowStorageMonitor() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.d(TAG, "[LowStorageMonitor] onReceive intent " + action);

            if (Intent.ACTION_DEVICE_STORAGE_OK.equals(action)) {
                sTriedAutoIncrement = false;    // try to upgrade on the next getWriteableDatabase
            }
        }
    }

    private void updateThreadsAttachmentColumn(SQLiteDatabase db) {
        // Set the values of that column correctly based on the current
        // contents of the database.
        db.execSQL("UPDATE threads SET has_attachment=1 WHERE _id IN " +
                   "  (SELECT DISTINCT pdu.thread_id FROM part " +
                   "   JOIN pdu ON pdu._id=part.mid " +
                   "   WHERE part.ct != 'text/plain' AND part.ct != 'application/smil')");
    }
}
