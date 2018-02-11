package com.android.providers.telephony;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Path;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
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
import com.google.android.mms.pdu.PduHeaders;
//import android.provider.Telephony.MmsSms.PendingMessages;
import gionee.provider.GnTelephony.MmsSms.PendingMessages;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import gionee.provider.GnTelephony;
import gionee.provider.GnTelephony.SimInfo;

public class MmsSmsSyncProvider extends ContentProvider {
    private final static String TAG = "MmsSmsSyncProvider";

    public static final String TABLE_SYNC = "messagesync";
    public static final String PART_SYNC = "partsync";
    public static final String SYNC_VIEW = "syncview";
    public static final int MMS_TYPE = 0;
    public static final int SMS_TYPE = 1;

    private static final String MMS_SMS_AUTHORITY = "mms-sms";

    private final Uri MMS_SMS_URI = Uri.parse("content://" + MMS_SMS_AUTHORITY);
    private final Uri THREAD_ID_URI = Uri.withAppendedPath(MMS_SMS_URI,
            "threadID");
    
    Uri SIM_ID_URI = Uri.parse("content://telephony/siminfo");

    private static final UriMatcher URI_MATCHER = new UriMatcher(
            UriMatcher.NO_MATCH);
    private static final String AUTHORITY = "mms-sms-sync";
    private static final Uri MMS_SMS_SYNC_URI = Uri.parse("content://"
            + AUTHORITY);

    public static final Uri SYNC_SMS_UPDATE_URI = Uri.withAppendedPath(
            MMS_SMS_SYNC_URI, "sync_sms_update");
    public static final Uri SYNC_MMS_UPDATE_URI = Uri.withAppendedPath(
            MMS_SMS_SYNC_URI, "sync_mms_update");
    public static final Uri SYNC_PART_UPDATE_URI = Uri.withAppendedPath(
            MMS_SMS_SYNC_URI, "sync_part_update");

    public static final Uri SYNC_SMS_URI = Uri.withAppendedPath(
            MMS_SMS_SYNC_URI, "sync_sms");
    public static final Uri SYNC_MMS_URI = Uri.withAppendedPath(
            MMS_SMS_SYNC_URI, "sync_mms");
    public static final Uri SYNC_PART_URI = Uri.withAppendedPath(
            MMS_SMS_SYNC_URI, "sync_part");

    public static final Uri SYNC_URI = Uri.withAppendedPath(MMS_SMS_SYNC_URI,
            "sync");
    public static final Uri PARTURI = Uri.withAppendedPath(MMS_SMS_SYNC_URI,
            "part");
    
    public static class Sync{
        //sync表字段
        public static final String _ID = "_id";
        public static final String MSG_TYPE = "msg_type";
        public static final String MSG_ID = "msg_id";
        public static final String ISDELETE = "isdelete";
        public static final String DIRTY = "dirty";
        public static final String UPDATE_DATE = "update_date";
        public static final String LOCAL_FLAG = "local_flag";
        public static final String SYNC_ID = "sync_id";
        //短信和彩信共同项
        public static final String THREAD_ID = "thread_id";
        public static final String DATE = "date";
        public static final String DATE_SENT = "date_sent";
        public static final String STATUS = "status";
        public static final String MESSAGE_SIZE = "m_size";
        public static final String READ = "read";
        public static final String SUBJECT = "sub";
        public static final String LOCKED = "locked";
        public static final String SIM_ID = "sim_id";
        public static final String SERVICE_CENTER = "service_center";
        public static final String SEEN = "seen";
        public static final String STAR = "star";
        public static final String UPDATE_THREADS = "update_threads";
        public static final String REJECT = "reject";
        public static final String MESSAGE_TYPE = "m_type";
        public static final String IS_PRIVACY = "is_privacy";
        public static final String WEATHER_INFO = "weather_info";
        
        //mms
        public static final String MESSAGE_BOX = "msg_box";
        public static final String MESSAGE_ID = "m_id";
        public static final String SUBJECT_CHARSET = "sub_cs";
        public static final String CONTENT_TYPE = "ct_t";
        public static final String CONTENT_LOCATION = "ct_l";
        public static final String EXPIRY = "exp";
        public static final String MESSAGE_CLASS = "m_cls";
        public static final String MMS_VERSION = "v";
        public static final String PRIORITY = "pri";
        public static final String READ_REPORT = "rr";
        public static final String REPORT_ALLOWED = "rpt_a";
        public static final String RESPONSE_STATUS = "resp_st";
        public static final String TRANSACTION_ID = "tr_id";
        public static final String RETRIEVE_STATUS = "retr_st";
        public static final String RETRIEVE_TEXT = "retr_txt";
        public static final String RETRIEVE_TEXT_CHARSET = "retr_txt_cs";
        public static final String READ_STATUS = "read_status";
        public static final String CONTENT_CLASS = "ct_cls";
        public static final String RESPONSE_TEXT = "resp_txt";
        public static final String DELIVERY_TIME = "d_tm";
        public static final String DELIVERY_REPORT = "d_rpt";
        
        //sms
        public static final String ADDRESS = "address";
        public static final String PERSON = "person";
        public static final String PROTOCOL = "protocol";
        public static final String REPLY_PATH_PRESENT = "reply_path_present";
        public static final String BODY = "body";
        public static final String ERROR_CODE = "error_code";
    }
    
    public static class SyncPart{
        public static final String _ID = "_id";
        public static final String PART_ID = "part_id";
        public static final String ISDELETE = "isdelete";
        public static final String DIRTY = "dirty";
        public static final String SYNC_ID = "sync_id";
    }
    
    // (isdelete=1 and thread_id is null) or (is_privacy=0 and dirty=1 and local_flag is not null and ((msg_type=0 and ((status is null and msg_box=1) or msg_box=2)) or (msg_type=1 and (m_type=1 or m_type=2))) and reject=0)
    private static final String SYNC_UP_QUREY_SQL =
            "(" + Sync.ISDELETE + "=1 and " +
            Sync.THREAD_ID + " is null) or (" +
            Sync.IS_PRIVACY + "=0 and " +
            Sync.DIRTY + "=1 and " +
            Sync.LOCAL_FLAG + " is not null and " +
            "((" + Sync.MSG_TYPE + "=" + MMS_TYPE + " and ((" +
            Sync.STATUS + " is null and " +
            Sync.MESSAGE_BOX + "=" + android.provider.Telephony.Mms.MESSAGE_BOX_INBOX + ") or " +
            Sync.MESSAGE_BOX + "=" + android.provider.Telephony.Mms.MESSAGE_BOX_SENT + ")) or " +
            "(" + Sync.MSG_TYPE + "=" + SMS_TYPE + " and (" +
            Sync.MESSAGE_TYPE + "=1 or " +
            Sync.MESSAGE_TYPE + "=2))) and " +
            Sync.REJECT + "=0)";
    
    public static final String CREATE_PART_SYNC_SQL =
        "CREATE TABLE " + PART_SYNC + " ( " + "" +
        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
        SyncPart.PART_ID + " INTEGER DEFAULT -1 , " + 
        SyncPart.ISDELETE + " INTEGER NOT NULL DEFAULT 0, " + 
        SyncPart.DIRTY + " INTEGER NOT NULL DEFAULT 1, " + 
        SyncPart.SYNC_ID + " TEXT UNIQUE );";
    
    public static final String CREATE_TABLE_SYNC_SQL =
            "CREATE TABLE " + TABLE_SYNC + " ( " + "" +
            Sync._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
            Sync.MSG_TYPE + " INTEGER NOT NULL, " + 
            Sync.MSG_ID + " INTEGER NOT NULL, " + 
            Sync.ISDELETE + " INTEGER NOT NULL DEFAULT 0, " + 
            Sync.DIRTY + " INTEGER NOT NULL DEFAULT 1, " + 
            Sync.UPDATE_DATE + " INTEGER NOT NULL, " + 
            Sync.LOCAL_FLAG + " TEXT, " + 
            Sync.SYNC_ID + " TEXT UNIQUE);";
    
    public static final String CREATE_TABLE_SYNC_OLD_SQL =
            "CREATE TABLE " + TABLE_SYNC + " ( " + "" +
            Sync._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
            Sync.MSG_TYPE + " INTEGER NOT NULL, " + 
            Sync.MSG_ID + " INTEGER NOT NULL, " + 
            Sync.ISDELETE + " INTEGER NOT NULL DEFAULT 0, " + 
            Sync.DIRTY + " INTEGER NOT NULL DEFAULT 1, " + 
            Sync.UPDATE_DATE + " INTEGER NOT NULL, " + 
            Sync.SYNC_ID + " TEXT UNIQUE);";
    
    public static final String CREATE_SYNC_VIEW_OLD_SQL = 
            "CREATE VIEW " + SYNC_VIEW + " AS " +
            "SELECT " +
            //sync表为共同项，不需要转换
            Sync._ID + "," +
            Sync.MSG_TYPE + "," +
            Sync.MSG_ID + "," +
            Sync.ISDELETE + "," +
            Sync.DIRTY + "," +
            Sync.UPDATE_DATE + "," +
            Sync.SYNC_ID + "," +
            //共同项，需要转换
            Mms.THREAD_ID + " AS " + Sync.THREAD_ID + "," +
            Mms.DATE + " AS " + Sync.DATE + "," +
            Mms.DATE_SENT + " AS " + Sync.DATE_SENT + "," +
            Mms.STATUS + " AS " + Sync.STATUS + "," +
            Mms.MESSAGE_SIZE + " AS " + Sync.MESSAGE_SIZE + "," +
            Mms.READ + " AS " + Sync.READ + "," +
            Mms.SUBJECT + " AS " + Sync.SUBJECT + "," +
            Mms.LOCKED + " AS " + Sync.LOCKED + "," +
            Mms.SIM_ID + " AS " + Sync.SIM_ID + "," +
            Mms.SERVICE_CENTER + " AS " + Sync.SERVICE_CENTER + "," +
            Mms.SEEN + " AS " + Sync.SEEN + "," +
            Mms.STAR + " AS " + Sync.STAR + "," +
            "update_threads" + " AS " + Sync.UPDATE_THREADS + "," +
            "reject" + " AS " + Sync.REJECT + "," +
            Mms.MESSAGE_TYPE + " AS " + Sync.MESSAGE_TYPE + "," +
            "is_privacy" + " AS " + Sync.IS_PRIVACY + "," +
            "weather_info" + " AS " + Sync.WEATHER_INFO + "," +
            //mms不需要转换
            Sync.MESSAGE_BOX + "," +
            Sync.MESSAGE_ID + "," +
            Sync.SUBJECT_CHARSET + "," +
            Sync.CONTENT_TYPE + "," +
            Sync.CONTENT_LOCATION + "," +
            Sync.EXPIRY + "," +
            Sync.MESSAGE_CLASS + "," +
            Sync.MMS_VERSION + "," +
            Sync.PRIORITY + "," +
            Sync.READ_REPORT + "," +
            Sync.REPORT_ALLOWED + "," +
            Sync.RESPONSE_STATUS + "," +
            Sync.TRANSACTION_ID + "," +
            Sync.RETRIEVE_STATUS + "," +
            Sync.RETRIEVE_TEXT + "," +
            Sync.RETRIEVE_TEXT_CHARSET + "," +
            Sync.READ_STATUS + "," +
            Sync.CONTENT_CLASS + "," +
            Sync.RESPONSE_TEXT + "," +
            Sync.DELIVERY_TIME + "," +
            Sync.DELIVERY_REPORT + "," +
            //sms需要全部为null
            "NULL AS " + Sync.ADDRESS + "," +
            "NULL AS " + Sync.PERSON + "," +
            "NULL AS " + Sync.PROTOCOL + "," +
            "NULL AS " + Sync.REPLY_PATH_PRESENT + "," +
            "NULL AS " + Sync.BODY + "," +
            "NULL AS " + Sync.ERROR_CODE + " FROM " +
            "(SELECT * FROM " + TABLE_SYNC + " LEFT JOIN " + MmsProvider.TABLE_PDU +
            " ON " + TABLE_SYNC + "." + Sync.MSG_ID + "=" + MmsProvider.TABLE_PDU + "." + Mms._ID +
            " WHERE " + Sync.MSG_TYPE + "=" + MMS_TYPE + ")" +
            " UNION ALL " +
            "SELECT " +
            //sync表为共同项，不需要转换
            Sync._ID + "," +
            Sync.MSG_TYPE + "," +
            Sync.MSG_ID + "," +
            Sync.ISDELETE + "," +
            Sync.DIRTY + "," +
            Sync.UPDATE_DATE + "," +
            Sync.SYNC_ID + "," +
            //共同项，需要转换
            "thread_id" + " AS " + Sync.THREAD_ID + "," +
            "date" + " AS " + Sync.DATE + "," +
            "date_sent" + " AS " + Sync.DATE_SENT + "," +
            "status" + " AS " + Sync.STATUS + "," +
            "m_size" + " AS " + Sync.MESSAGE_SIZE + "," +
            "read" + " AS " + Sync.READ + "," +
            "subject" + " AS " + Sync.SUBJECT + "," +
            "locked" + " AS " + Sync.LOCKED + "," +
            "sim_id" + " AS " + Sync.SIM_ID + "," +
            "service_center" + " AS " + Sync.SERVICE_CENTER + "," +
            "seen" + " AS " + Sync.SEEN + "," +
            "star" + " AS " + Sync.STAR + "," +
            "update_threads" + " AS " + Sync.UPDATE_THREADS + "," +
            "reject" + " AS " + Sync.REJECT + "," +
            "type" + " AS " + Sync.MESSAGE_TYPE + "," +
            "is_privacy" + " AS " + Sync.IS_PRIVACY + "," +
            "weather_info" + " AS " + Sync.WEATHER_INFO + "," +
            //mms需要全部为null
            "NULL AS " + Sync.MESSAGE_BOX + "," +
            "NULL AS " + Sync.MESSAGE_ID + "," +
            "NULL AS " + Sync.SUBJECT_CHARSET + "," +
            "NULL AS " + Sync.CONTENT_TYPE + "," +
            "NULL AS " + Sync.CONTENT_LOCATION + "," +
            "NULL AS " + Sync.EXPIRY + "," +
            "NULL AS " + Sync.MESSAGE_CLASS + "," +
            "NULL AS " + Sync.MMS_VERSION + "," +
            "NULL AS " + Sync.PRIORITY + "," +
            "NULL AS " + Sync.READ_REPORT + "," +
            "NULL AS " + Sync.REPORT_ALLOWED + "," +
            "NULL AS " + Sync.RESPONSE_STATUS + "," +
            "NULL AS " + Sync.TRANSACTION_ID + "," +
            "NULL AS " + Sync.RETRIEVE_STATUS + "," +
            "NULL AS " + Sync.RETRIEVE_TEXT + "," +
            "NULL AS " + Sync.RETRIEVE_TEXT_CHARSET + "," +
            "NULL AS " + Sync.READ_STATUS + "," +
            "NULL AS " + Sync.CONTENT_CLASS + "," +
            "NULL AS " + Sync.RESPONSE_TEXT + "," +
            "NULL AS " + Sync.DELIVERY_TIME + "," +
            "NULL AS " + Sync.DELIVERY_REPORT + "," +
            //sms不需要转换
            Sync.ADDRESS + "," +
            Sync.PERSON + "," +
            Sync.PROTOCOL + "," +
            Sync.REPLY_PATH_PRESENT + "," +
            Sync.BODY + "," +
            Sync.ERROR_CODE + " FROM " +
            "(SELECT * FROM " + TABLE_SYNC + " LEFT JOIN " + SmsProvider.TABLE_SMS +
            " ON " + TABLE_SYNC + "." + Sync.MSG_ID + "=" + SmsProvider.TABLE_SMS + "." + "_id" +
            " WHERE " + Sync.MSG_TYPE + "=" + SMS_TYPE + ")" +
            " ORDER BY " + Sync._ID + " ASC";
    
    public static final String CREATE_SYNC_VIEW_SQL = 
            "CREATE VIEW " + SYNC_VIEW + " AS " +
            "SELECT " +
            //sync表为共同项，不需要转换
            Sync._ID + "," +
            Sync.MSG_TYPE + "," +
            Sync.MSG_ID + "," +
            Sync.ISDELETE + "," +
            Sync.DIRTY + "," +
            Sync.UPDATE_DATE + "," +
            Sync.LOCAL_FLAG + "," +
            Sync.SYNC_ID + "," +
            //共同项，需要转换
            Mms.THREAD_ID + " AS " + Sync.THREAD_ID + "," +
            Mms.DATE + " AS " + Sync.DATE + "," +
            Mms.DATE_SENT + " AS " + Sync.DATE_SENT + "," +
            Mms.STATUS + " AS " + Sync.STATUS + "," +
            Mms.MESSAGE_SIZE + " AS " + Sync.MESSAGE_SIZE + "," +
            Mms.READ + " AS " + Sync.READ + "," +
            Mms.SUBJECT + " AS " + Sync.SUBJECT + "," +
            Mms.LOCKED + " AS " + Sync.LOCKED + "," +
            Mms.SIM_ID + " AS " + Sync.SIM_ID + "," +
            Mms.SERVICE_CENTER + " AS " + Sync.SERVICE_CENTER + "," +
            Mms.SEEN + " AS " + Sync.SEEN + "," +
            Mms.STAR + " AS " + Sync.STAR + "," +
            "update_threads" + " AS " + Sync.UPDATE_THREADS + "," +
            "reject" + " AS " + Sync.REJECT + "," +
            Mms.MESSAGE_TYPE + " AS " + Sync.MESSAGE_TYPE + "," +
            "is_privacy" + " AS " + Sync.IS_PRIVACY + "," +
            "weather_info" + " AS " + Sync.WEATHER_INFO + "," +
            //mms不需要转换
            Sync.MESSAGE_BOX + "," +
            Sync.MESSAGE_ID + "," +
            Sync.SUBJECT_CHARSET + "," +
            Sync.CONTENT_TYPE + "," +
            Sync.CONTENT_LOCATION + "," +
            Sync.EXPIRY + "," +
            Sync.MESSAGE_CLASS + "," +
            Sync.MMS_VERSION + "," +
            Sync.PRIORITY + "," +
            Sync.READ_REPORT + "," +
            Sync.REPORT_ALLOWED + "," +
            Sync.RESPONSE_STATUS + "," +
            Sync.TRANSACTION_ID + "," +
            Sync.RETRIEVE_STATUS + "," +
            Sync.RETRIEVE_TEXT + "," +
            Sync.RETRIEVE_TEXT_CHARSET + "," +
            Sync.READ_STATUS + "," +
            Sync.CONTENT_CLASS + "," +
            Sync.RESPONSE_TEXT + "," +
            Sync.DELIVERY_TIME + "," +
            Sync.DELIVERY_REPORT + "," +
            //sms需要全部为null
            "NULL AS " + Sync.ADDRESS + "," +
            "NULL AS " + Sync.PERSON + "," +
            "NULL AS " + Sync.PROTOCOL + "," +
            "NULL AS " + Sync.REPLY_PATH_PRESENT + "," +
            "NULL AS " + Sync.BODY + "," +
            "NULL AS " + Sync.ERROR_CODE + " FROM " +
            "(SELECT * FROM " + TABLE_SYNC + " LEFT JOIN " + MmsProvider.TABLE_PDU +
            " ON " + TABLE_SYNC + "." + Sync.MSG_ID + "=" + MmsProvider.TABLE_PDU + "." + Mms._ID +
            " WHERE " + Sync.MSG_TYPE + "=" + MMS_TYPE + ")" +
            " UNION ALL " +
            "SELECT " +
            //sync表为共同项，不需要转换
            Sync._ID + "," +
            Sync.MSG_TYPE + "," +
            Sync.MSG_ID + "," +
            Sync.ISDELETE + "," +
            Sync.DIRTY + "," +
            Sync.UPDATE_DATE + "," +
            Sync.LOCAL_FLAG + "," +
            Sync.SYNC_ID + "," +
            //共同项，需要转换
            "thread_id" + " AS " + Sync.THREAD_ID + "," +
            "date" + " AS " + Sync.DATE + "," +
            "date_sent" + " AS " + Sync.DATE_SENT + "," +
            "status" + " AS " + Sync.STATUS + "," +
            "m_size" + " AS " + Sync.MESSAGE_SIZE + "," +
            "read" + " AS " + Sync.READ + "," +
            "subject" + " AS " + Sync.SUBJECT + "," +
            "locked" + " AS " + Sync.LOCKED + "," +
            "sim_id" + " AS " + Sync.SIM_ID + "," +
            "service_center" + " AS " + Sync.SERVICE_CENTER + "," +
            "seen" + " AS " + Sync.SEEN + "," +
            "star" + " AS " + Sync.STAR + "," +
            "update_threads" + " AS " + Sync.UPDATE_THREADS + "," +
            "reject" + " AS " + Sync.REJECT + "," +
            "type" + " AS " + Sync.MESSAGE_TYPE + "," +
            "is_privacy" + " AS " + Sync.IS_PRIVACY + "," +
            "weather_info" + " AS " + Sync.WEATHER_INFO + "," +
            //mms需要全部为null
            "NULL AS " + Sync.MESSAGE_BOX + "," +
            "NULL AS " + Sync.MESSAGE_ID + "," +
            "NULL AS " + Sync.SUBJECT_CHARSET + "," +
            "NULL AS " + Sync.CONTENT_TYPE + "," +
            "NULL AS " + Sync.CONTENT_LOCATION + "," +
            "NULL AS " + Sync.EXPIRY + "," +
            "NULL AS " + Sync.MESSAGE_CLASS + "," +
            "NULL AS " + Sync.MMS_VERSION + "," +
            "NULL AS " + Sync.PRIORITY + "," +
            "NULL AS " + Sync.READ_REPORT + "," +
            "NULL AS " + Sync.REPORT_ALLOWED + "," +
            "NULL AS " + Sync.RESPONSE_STATUS + "," +
            "NULL AS " + Sync.TRANSACTION_ID + "," +
            "NULL AS " + Sync.RETRIEVE_STATUS + "," +
            "NULL AS " + Sync.RETRIEVE_TEXT + "," +
            "NULL AS " + Sync.RETRIEVE_TEXT_CHARSET + "," +
            "NULL AS " + Sync.READ_STATUS + "," +
            "NULL AS " + Sync.CONTENT_CLASS + "," +
            "NULL AS " + Sync.RESPONSE_TEXT + "," +
            "NULL AS " + Sync.DELIVERY_TIME + "," +
            "NULL AS " + Sync.DELIVERY_REPORT + "," +
            //sms不需要转换
            Sync.ADDRESS + "," +
            Sync.PERSON + "," +
            Sync.PROTOCOL + "," +
            Sync.REPLY_PATH_PRESENT + "," +
            Sync.BODY + "," +
            Sync.ERROR_CODE + " FROM " +
            "(SELECT * FROM " + TABLE_SYNC + " LEFT JOIN " + SmsProvider.TABLE_SMS +
            " ON " + TABLE_SYNC + "." + Sync.MSG_ID + "=" + SmsProvider.TABLE_SMS + "." + "_id" +
            " WHERE " + Sync.MSG_TYPE + "=" + SMS_TYPE + ")" +
            " ORDER BY " + Sync._ID + " ASC";

    private static final int URI_SYNC_UP = 0;
    private static final int URI_SYNC_UP_COUNT_ID = 1;
    private static final int URI_SYNC_UP_LIMIT_ID = 2;
    private static final int URI_SYNC_UP_COUNT_SIZE = 3;
    private static final int URI_SYNC_UP_RESULT = 4;
    private static final int URI_SYNC_UP_RESULT_MULTI = 5;
    private static final int URI_SYNC_DOWN = 6;
    private static final int URI_SYNC_DOWN_MULTI = 7;
    private static final int URI_SYNC_PART = 8;
    private static final int URI_SYNC_PART_SYNC_ID = 9;
    private static final int URI_SYNC_PART_PART_ID = 10;
    private static final int URI_SYNC_SMS_UPDATE_ID = 11;
    private static final int URI_SYNC_MMS_UPDATE_ID = 12;
    private static final int URI_SYNC_PART_UPDATE_ID = 13;
    private static final int URI_SYNC_PART_UPDATE = 14;
    private static final int URI_SYNC_SMS_UPDATE = 15;
    private static final int URI_SYNC_MMS_UPDATE = 16;

    private static final int URI_SYNC = 17;
    private static final int URI_SYNC_ID = 18;
    private static final int URI_PART = 19;
    private static final int URI_PART_SYNC_ID = 20;
    private static final int URI_SYNC_ACCESSORY = 21;
    private static final int URI_SYNC_CONTINUE = 22;
    private static final int URI_SYNC_CONTINUE_ID = 23;
    
    private static final int URI_CLEAN_ACCOUNT = 24;
    private static final int URI_INIT_ACCOUNT = 25;
    private static final int URI_INIT_ACCOUNT_MULTI = 26;
    
    private static final int URI_CLEAN_DATA = 27;
    private static final int URI_IS_FIRST_SYNC = 28;

    private static final String[] SYNC_SELECTION_ARGS = new String[] { "body",
            "accessory", "info" };

    static {
        URI_MATCHER.addURI(AUTHORITY, "sync_up", URI_SYNC_UP);
        URI_MATCHER.addURI(AUTHORITY, "sync_up/#", URI_SYNC_UP_COUNT_ID);
        URI_MATCHER.addURI(AUTHORITY, "sync_up/#/#", URI_SYNC_UP_LIMIT_ID);
        URI_MATCHER.addURI(AUTHORITY, "sync_up_size", URI_SYNC_UP_COUNT_SIZE);
        URI_MATCHER.addURI(AUTHORITY, "sync_up_result", URI_SYNC_UP_RESULT);
        URI_MATCHER.addURI(AUTHORITY, "sync_up_result_multi", URI_SYNC_UP_RESULT_MULTI);
        URI_MATCHER.addURI(AUTHORITY, "sync_down", URI_SYNC_DOWN);
        URI_MATCHER.addURI(AUTHORITY, "sync_down_multi", URI_SYNC_DOWN_MULTI);
        URI_MATCHER.addURI(AUTHORITY, "sync_part", URI_SYNC_PART);
        URI_MATCHER.addURI(AUTHORITY, "sync_part/#", URI_SYNC_PART_SYNC_ID);
        URI_MATCHER.addURI(AUTHORITY, "#/sync_part", URI_SYNC_PART_PART_ID);
        URI_MATCHER.addURI(AUTHORITY, "sync_sms_update/#",
                URI_SYNC_SMS_UPDATE_ID);
        URI_MATCHER.addURI(AUTHORITY, "sync_mms_update/#",
                URI_SYNC_MMS_UPDATE_ID);
        URI_MATCHER.addURI(AUTHORITY, "sync_part_update/#",
                URI_SYNC_PART_UPDATE_ID);
        URI_MATCHER.addURI(AUTHORITY, "sync_sms_update", URI_SYNC_SMS_UPDATE);
        URI_MATCHER.addURI(AUTHORITY, "sync_mms_update", URI_SYNC_MMS_UPDATE);
        URI_MATCHER.addURI(AUTHORITY, "sync_part_update", URI_SYNC_PART_UPDATE);
        URI_MATCHER.addURI(AUTHORITY, "sync/#", URI_SYNC_ID);
        URI_MATCHER.addURI(AUTHORITY, "sync", URI_SYNC);
        URI_MATCHER.addURI(AUTHORITY, "part/#", URI_PART_SYNC_ID);
        URI_MATCHER.addURI(AUTHORITY, "part", URI_PART);
        URI_MATCHER.addURI(AUTHORITY, "accessory", URI_SYNC_ACCESSORY);
        URI_MATCHER.addURI(AUTHORITY, "sync_up_continue/#", URI_SYNC_CONTINUE_ID);
        URI_MATCHER.addURI(AUTHORITY, "clean_account", URI_CLEAN_ACCOUNT);
        URI_MATCHER.addURI(AUTHORITY, "init_account", URI_INIT_ACCOUNT);
        URI_MATCHER.addURI(AUTHORITY, "init_account_multi", URI_INIT_ACCOUNT_MULTI);
        URI_MATCHER.addURI(AUTHORITY, "clean_data", URI_CLEAN_DATA);
        URI_MATCHER.addURI(AUTHORITY, "is_first_sync", URI_IS_FIRST_SYNC);
    }
    private SQLiteOpenHelper mOpenHelper;
    
    private Handler mHandler;
    private static final int SYNC_NOTIFY = 0;
    private static final int CLEAN_UP_ALL_DATA = 1;
    private static final int CLEAN_UP_DATA = 2;
    private static final int CLEAN_UP_SYNC_PART = 3;
    private static final int CLEAN_UP_SYNC_DB = 4;
    private static final String SYNC_NOTIFY_ACTION = "com.aurora.account.START_SYNC";
    
    private final String TEMP_USED_FILE = "temp_used_file";
    private SharedPreferences mSp = null;
    private Set<String> tempUsedFile = new HashSet<String>();

    @Override
    public boolean onCreate() {
        mOpenHelper = MmsSmsDatabaseHelper.getInstance(getContext());
        HandlerThread mBackgroundThread = new HandlerThread("ContactsProviderWorker",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mBackgroundThread.start();
        mSp = getContext().getSharedPreferences("mms", Context.MODE_PRIVATE);
        tempUsedFile = mSp.getStringSet(TEMP_USED_FILE, tempUsedFile);
        mHandler = new Handler(mBackgroundThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                performBackgroundTask(msg.what, msg.obj);
            }
        };
        mHandler.sendEmptyMessage(CLEAN_UP_SYNC_PART);
        mHandler.sendEmptyMessage(CLEAN_UP_ALL_DATA);
        mHandler.sendEmptyMessage(CLEAN_UP_SYNC_DB);
        return true;
    }
    
    public static String createLocalFlag(String createId, long createTime){
        Random random = new Random();
        return createId + "," + random.nextInt() + "," + createTime;
    }

    private void performBackgroundTask(int what, Object obj) {
        // TODO Auto-generated method stub
        switch (what) {
        case SYNC_NOTIFY:
            Intent intent = new Intent(SYNC_NOTIFY_ACTION);
            intent.putExtra("packageName", "com.android.mms");
            getContext().sendBroadcast(intent);
            break;
        case CLEAN_UP_ALL_DATA:
            cleanUpAllData();
            break;
        case CLEAN_UP_DATA:
            String path = (String) obj;
            cleanUpData(new File(path));
            break;
        case CLEAN_UP_SYNC_PART:
        	cleanUpSyncPart();
        	break;
        case CLEAN_UP_SYNC_DB:
        	cleanUpSyncDb();
        	break;
        }
    }

    private void cleanUpAllData(){
        File fileDir = getContext().getDir("parts", Context.MODE_PRIVATE);
        File[] files = fileDir.listFiles();
        if (files == null) {
            return;
        }
        HashMap<String, File> allFile = new HashMap<String, File>(files.length);
        for (File file : files) {
            allFile.put(file.getAbsolutePath(), file);
        }
        Cursor cursor = mOpenHelper.getReadableDatabase().query(MmsProvider.TABLE_PART, new String[]{Part._DATA}, Part._DATA + " is not null", null, null, null, null);
        HashSet<String> usedData = null;
        if(cursor != null){
            usedData = new HashSet<String>(cursor.getCount());
            while(cursor.moveToNext()){
                usedData.add(cursor.getString(0));
            }
            cursor.close();
        }
        Set<String> unUsedData = allFile.keySet();
        if(usedData != null){
            unUsedData.removeAll(usedData);
        }
        unUsedData.removeAll(tempUsedFile);
        Iterator<String> iterator = unUsedData.iterator();
        while (iterator.hasNext()){
            String path = iterator.next();
            cleanUpData(allFile.get(path));
        }
    }
    
    private void cleanUpSyncPart(){
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete(PART_SYNC, SyncPart.PART_ID + " not in (select " + Part._ID + " from " + MmsProvider.TABLE_PART + ")", null);
    }
    
    private void cleanUpData(File file){
        boolean isFileDelete = file.delete();
    }

    private boolean isSyncDbOk() {
    	return isSmsSyncDbOk() && isMmsSyncDbOk();
	}
    
    private boolean isSmsSyncDbOk() {
    	boolean isOk = false;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    	Cursor maxSyncSms = db.query(TABLE_SYNC, new String[]{"max(" + Sync.MSG_ID + ") as id"}, Sync.MSG_TYPE + " = " + SMS_TYPE + " and " + Sync.ISDELETE + " <> 1", null, null, null, null);
    	Cursor maxSms = db.query(SmsProvider.TABLE_SMS, new String[]{"max(_id) as id"}, null, null, null, null, null);
    	if(maxSyncSms != null && maxSms != null){
    		if(maxSyncSms.moveToFirst()){
    			if(maxSms.moveToFirst()){
    				if(maxSyncSms.getLong(0) == maxSms.getLong(0)){
    	    			isOk = true; //最大的id相等，表明数据OK
    				}
    			}
    		} else if(!maxSms.moveToFirst()){
    			isOk = true; //都没有数据，表明数据OK
    		}
    	}
		if(maxSyncSms != null){
			maxSyncSms.close();
		}
		if(maxSms != null){
			maxSms.close();
		}
    	return isOk;
    }
    
    private boolean isMmsSyncDbOk() {
    	boolean isOk = false;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    	Cursor maxSyncMms = db.query(TABLE_SYNC, new String[]{"max(" + Sync.MSG_ID + ") as id"}, Sync.MSG_TYPE + " = " + MMS_TYPE + " and " + Sync.ISDELETE + " <> 1", null, null, null, null);
    	Cursor maxMms = db.query(MmsProvider.TABLE_PDU, new String[]{"max(_id) as id"}, null, null, null, null, null);
    	if(maxSyncMms != null && maxMms != null){
    		if(maxSyncMms.moveToFirst()){
    			if(maxMms.moveToFirst()){
    				if(maxSyncMms.getLong(0) == maxMms.getLong(0)){
    	    			isOk = true; //最大的id相等，表明数据OK
    				}
    			}
    		} else if(!maxMms.moveToFirst()){
    			isOk = true; //都没有数据，表明数据OK
    		}
    	}
		if(maxSyncMms != null){
			maxSyncMms.close();
		}
		if(maxMms != null){
			maxMms.close();
		}
    	return isOk;
    }
    
    private void cleanUpSyncDb(){
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete(TABLE_SYNC, Sync.SYNC_ID + " is null and " + Sync.ISDELETE + " = 1", null); //已经删除的，不需要同步的，直接删除
        ContentValues values = new ContentValues();
        values.put(Sync.ISDELETE, 1);
        values.put(Sync.DIRTY, 1);
		db.update(TABLE_SYNC, values, Sync.ISDELETE + " <> 1 and " + Sync.MSG_TYPE + "=" + SMS_TYPE + " and " + Sync.MSG_ID + " not in (select _id from " + SmsProvider.TABLE_SMS + ")", null); //短信中没有的，标记删除
		db.update(TABLE_SYNC, values, Sync.ISDELETE + " <> 1 and " + Sync.MSG_TYPE + "=" + MMS_TYPE + " and " + Sync.MSG_ID + " not in (select _id from " + MmsProvider.TABLE_PDU + ")", null);//彩信中没有的，标记删除
		Cursor sms = db.query(SmsProvider.TABLE_SMS, new String[]{"_id"}, "_id not in (select " + Sync.MSG_ID + " from " + TABLE_SYNC + " where " + Sync.MSG_TYPE + "=" + SMS_TYPE + ")", null, null, null, null); //短信中有的，添加
		if(sms != null){
			while(sms.moveToNext()){
				values.clear();
	            values.put(Sync.MSG_TYPE, SMS_TYPE);
	            values.put(Sync.MSG_ID, sms.getLong(0));
	            values.put(Sync.UPDATE_DATE, System.currentTimeMillis());
	            long id = db.insert(TABLE_SYNC, null, values);
	            if(id > 0){
		            String localFlag = createLocalFlag(String.valueOf(id), System.currentTimeMillis());
					values.clear();
					values.put(Sync.LOCAL_FLAG, localFlag);
		            db.update(TABLE_SYNC, values, Sync._ID + "=" + id, null);
	            }
			}
			sms.close();
		}
		Cursor mms = db.query(MmsProvider.TABLE_PDU, new String[]{"_id"}, "_id not in (select " + Sync.MSG_ID + " from " + TABLE_SYNC + " where " + Sync.MSG_TYPE + "=" + MMS_TYPE + ")", null, null, null, null); //短信中有的，添加
		if(mms != null){
			while(mms.moveToNext()){
				long pduId = mms.getLong(0);
				values.clear();
	            values.put(Sync.MSG_TYPE, MMS_TYPE);
	            values.put(Sync.MSG_ID, pduId);
	            values.put(Sync.UPDATE_DATE, System.currentTimeMillis());
	            long id = db.insert(TABLE_SYNC, null, values);
	            if(id > 0){
		            String localFlag = createLocalFlag(String.valueOf(id), System.currentTimeMillis());
					values.clear();
					values.put(Sync.LOCAL_FLAG, localFlag);
		            db.update(TABLE_SYNC, values, Sync._ID + "=" + id, null);
	            }
                Cursor partCursor = db.query(MmsProvider.TABLE_PART, new String[]{Part._ID, Part._DATA}, Part.MSG_ID + "=" + pduId + " and " + Part._DATA + " is not null", null, null, null, null);
                if(partCursor != null){
                	while(partCursor.moveToNext()){
                		 if(!TextUtils.isEmpty(partCursor.getString(1))){
                             values.clear();
                             values.put(SyncPart.PART_ID, partCursor.getString(0));
                             db.insert(PART_SYNC, null, values);
                		 }
                	}
                	partCursor.close();
                }
			}
			mms.close();
		}
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;
        boolean isRealDelete = true;
        String table = "";
        ContentValues values = new ContentValues();
        boolean isNeedFromSync = false;
        switch (URI_MATCHER.match(uri)) {
        case URI_SYNC_MMS_UPDATE:
            isRealDelete = false;
            selection = Sync.MSG_TYPE + "=" + MMS_TYPE + " and " + Sync.MSG_ID + " in (select _id from " + MmsProvider.TABLE_PDU
                    + " where " + selection + ")";
            values.put(Sync.ISDELETE, 1);
            values.put(Sync.DIRTY, 1);
            values.put(Sync.UPDATE_DATE, System.currentTimeMillis());
            count = db.update(TABLE_SYNC, values, selection, selectionArgs);
            mHandler.sendEmptyMessage(CLEAN_UP_SYNC_PART);
            isNeedFromSync = true;
            break;
        case URI_SYNC_SMS_UPDATE:
            isRealDelete = false;
            selection = Sync.MSG_TYPE + "=" + SMS_TYPE + " and " + Sync.MSG_ID + " in (select _id from " + SmsProvider.TABLE_SMS
                    + " where " + selection + ")";
            values.put(Sync.ISDELETE, 1);
            values.put(Sync.DIRTY, 1);
            values.put(Sync.UPDATE_DATE, System.currentTimeMillis());
            count = db.update(TABLE_SYNC, values, selection, selectionArgs);
            isNeedFromSync = true;
            break;
        case URI_SYNC_SMS_UPDATE_ID:
            isRealDelete = false;
            String smsId = uri.getPathSegments().get(1);
            selection = Sync.MSG_TYPE + "=" + SMS_TYPE + " and " + Sync.MSG_ID + " = " + smsId;
            values.put(Sync.ISDELETE, 1);
            values.put(Sync.DIRTY, 1);
            values.put(Sync.UPDATE_DATE, System.currentTimeMillis());
            count = db.update(TABLE_SYNC, values, selection, selectionArgs);
            isNeedFromSync = true;
            break;
        case URI_SYNC_PART:
            table = PART_SYNC;
            break;
        case URI_SYNC:
            table = TABLE_SYNC;
            break;
        case URI_CLEAN_DATA:
            isRealDelete = false;
            values.put(Sync.DIRTY, 1);
            values.putNull(Sync.SYNC_ID);
            db.update(TABLE_SYNC, values, null, null);
            db.delete(TABLE_SYNC, Sync.SYNC_ID + " is null and " + Sync.ISDELETE + " = 1", null);
            Cursor cursor = db.query("threads", new String[]{"_id"}, "reject = 0 and is_privacy = 0", null, null, null, null);
            StringBuffer threadIds = new StringBuffer();
            int idCount = 0;
            if(cursor != null){
                if(cursor.moveToFirst()){
                    threadIds.append(cursor.getString(0));
                    idCount++;
                    while(cursor.moveToNext()){
                        threadIds.append(",");
                        threadIds.append(cursor.getString(0));
                        idCount++;
                    }
                }
                cursor.close();
            }
            if(idCount == 1){
                Uri deleteAllUri = Uri.parse("content://mms-sms/conversations/" + threadIds.toString());
                count = getContext().getContentResolver().delete(deleteAllUri, null,  null);
            } else if (idCount > 1) {
                Uri deleteAllUri = Uri.parse("content://mms-sms/conversations/" + threadIds.toString());
                count = getContext().getContentResolver().delete(deleteAllUri, null,  new String[]{"OneLast"});
            }
            break;
        case URI_CLEAN_ACCOUNT:
            isRealDelete = false;
            values.put(Sync.DIRTY, 1);
            values.putNull(Sync.SYNC_ID);
            count = db.update(TABLE_SYNC, values, null, null);
            values.clear();
            values.put(SyncPart.DIRTY, 1);
            values.putNull(SyncPart.SYNC_ID);
            count = db.update(PART_SYNC, values, null, null);
            int deleteCount = db.delete(TABLE_SYNC, Sync.SYNC_ID + " is null and " + Sync.ISDELETE + " = 1", null);
            count = count - deleteCount;
            break;
        }
        if(isNeedFromSync){  //如果还没有sync过的条数delete掉了，sync数据库中也直接delete掉
            int deleteCount = db.delete(TABLE_SYNC, Sync.SYNC_ID + " is null and " + Sync.ISDELETE + " = 1", null);
            if(deleteCount < count){
                notifySync();
            }
        }
        if (isRealDelete) {
            count = db.delete(table, selection, selectionArgs);
        }
        return count;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (values == null) {
            values = new ContentValues();
        }
        String table = null;
        long id = -1;
        boolean isNeedNotifySync = false;
        switch (URI_MATCHER.match(uri)) {
        case URI_SYNC_UP:
            break;
        case URI_SYNC_PART_SYNC_ID:
            table = PART_SYNC;
            String syncId = uri.getPathSegments().get(1);
            values.put(SyncPart.SYNC_ID, syncId);
            break;
        case URI_SYNC_PART_PART_ID:
            table = PART_SYNC;
            String partId = uri.getPathSegments().get(0);
            values.put(SyncPart.PART_ID, partId);
            break;
        case URI_SYNC_MMS_UPDATE_ID:
            table = TABLE_SYNC;
            String mmsId = uri.getPathSegments().get(1);
            values.put(Sync.MSG_TYPE, MMS_TYPE);
            values.put(Sync.MSG_ID, mmsId);
            values.put(Sync.UPDATE_DATE, System.currentTimeMillis());
            isNeedNotifySync = true;
            break;
        case URI_SYNC_SMS_UPDATE_ID:
            table = TABLE_SYNC;
            String smsId = uri.getPathSegments().get(1);
            values.put(Sync.MSG_TYPE, SMS_TYPE);
            values.put(Sync.MSG_ID, smsId);
            values.put(Sync.UPDATE_DATE, System.currentTimeMillis());
            isNeedNotifySync = true;
            break;
        case URI_SYNC_PART_UPDATE_ID:
            table = PART_SYNC;
            partId = uri.getPathSegments().get(1);
            values.put(SyncPart.PART_ID, partId);
            break;
        case URI_SYNC_UP_RESULT:
            if (values.containsKey("id")) {
                String resultid = values.getAsString("id");
                String syncid = values.getAsString("syncid");
                String accessory = values.getAsString("accessory");
                long date = values.getAsLong("date");
                int result = values.getAsInteger("result");
                int count = syncUpResultOne(db, resultid, syncid, accessory, date, result);
                if(count > 0){
                    return Uri.withAppendedPath(uri, "true");
                }
            }
            return Uri.withAppendedPath(uri, "false");
        case URI_SYNC_UP_RESULT_MULTI:
            if (values.containsKey("multi")) {
                int count = 0;
                String multi = values.getAsString("multi");
                MyJsonObject multiObject = null;
                try{
                    multiObject = new MyJsonObject(multi);
                } catch (JSONException e){
                }
                if(multiObject != null){
                    JSONArray jsonArray = null;
                    try{
                        jsonArray = multiObject.getJSONArray("sycndata");
                    } catch (JSONException e){
                    }
                    if(jsonArray != null && jsonArray.length() > 0){
                        for(int i = 0; i < jsonArray.length(); i++){
                            MyJsonObject jsonObject = null;
                            try{
                                JSONObject  object = jsonArray.getJSONObject(i);
                                jsonObject = MyJsonObject.createFromJsonObject(object);
                            } catch (JSONException e){
                                
                            }
                            if(jsonObject != null){
                                String resultid = jsonObject.getString("id");
                                String accessory = null;
                                String syncid = jsonObject.getString("syncid");
                                int op = jsonObject.getInt("op");
                                long date = jsonObject.getLong("date");
                                int result = jsonObject.getInt("result");
                                MyJsonObject accessoryJson = null;
                                try{
                                    accessoryJson = MyJsonObject.createFromJsonObject(jsonObject.getJSONObject("accOjb"));
                                } catch (JSONException e){
                                    
                                }
                                MyJsonObject bodyJson = null;
                                try{
                                    bodyJson = new MyJsonObject(jsonObject.getString("body"));
                                } catch (JSONException e){
                                }
                                if(accessoryJson != null){
                                    if(accessoryJson != null){
                                        accessory = accessoryJson.getString("accessory");
                                    }
                                }
                                if(bodyJson != null){
                                    if(syncUpResultOne(db, bodyJson.getString("id"), bodyJson.getString("syncid"), accessory,  bodyJson.getLong("date"),  bodyJson.getInt("result")) > 0){
                                        count++;
                                    } else {
                                        Log.i(TAG, "URI_SYNC_UP_RESULT_MULTI bodyJson is " + bodyJson.toString());
                                    }
                                } else {
                                    Log.i(TAG, "URI_SYNC_UP_RESULT_MULTI bodyJson is null");
                                }
                            }
                        }
                        if(count == jsonArray.length()){
                            return Uri.withAppendedPath(uri, "true");
                        }
                    }
                }
            }
            return Uri.withAppendedPath(uri, "false");
        case URI_SYNC_DOWN:
            if (values.containsKey("body")) {
                Map<String, Integer> gn_sim_id_map = new HashMap<String, Integer>();
                Cursor sim_id_cursor = getContext().getContentResolver().query(SIM_ID_URI, new String[]{SimInfo._ID, SimInfo.ICC_ID}, null, null, null);
                if(sim_id_cursor != null){
                    while(sim_id_cursor.moveToNext()){
                        int simId = sim_id_cursor.getInt(0);
                        String SimIccId = sim_id_cursor.getString(1);
                        gn_sim_id_map.put(SimIccId, simId);
                    }
                    sim_id_cursor.close();
                }
                String body = values.getAsString("body");
                String accessory = values.getAsString("accessory");
                String syncid = values.getAsString("syncid");
                int count = syncDownOne(db, body, accessory, syncid, gn_sim_id_map);
                if(count > 0){
                    return Uri.withAppendedPath(uri, "true");
                }
            }
            return Uri.withAppendedPath(uri, "false");
        case URI_SYNC_DOWN_MULTI:
            if (values.containsKey("multi")) {
                int count = 0;
                String multi = values.getAsString("multi");
                JSONArray jsonArray = null;
                try{
                    jsonArray = new JSONArray(multi);
                } catch (JSONException e){
                    
                }
                if(jsonArray != null && jsonArray.length() > 0){
                    Map<String, Integer> gn_sim_id_map = new HashMap<String, Integer>();
                    Cursor sim_id_cursor = getContext().getContentResolver().query(SIM_ID_URI, new String[]{SimInfo._ID, SimInfo.ICC_ID}, null, null, null);
                    if(sim_id_cursor != null){
                        while(sim_id_cursor.moveToNext()){
                            int simId = sim_id_cursor.getInt(0);
                            String SimIccId = sim_id_cursor.getString(1);
                            gn_sim_id_map.put(SimIccId, simId);
                        }
                        sim_id_cursor.close();
                    }
                    db.beginTransaction();
                    try{
                        for(int i = 0; i < jsonArray.length(); i++){
                            MyJsonObject jsonObject = null;
                            try{
                                JSONObject object = jsonArray.getJSONObject(i);
                                jsonObject = MyJsonObject.createFromJsonObject(object);
                            } catch (JSONException e){
                                
                            }
                            if(jsonObject != null){
                                String accOjb = jsonObject.getString("accOjb");
                                String accessory = null;
                                String syncid = jsonObject.getString("syncid");
                                String body = jsonObject.getString("body");
                                int op = jsonObject.getInt("op");
                                if(!TextUtils.isEmpty(accOjb)){
                                    MyJsonObject accessoryJson = null;
                                    try{
                                        accessoryJson = new MyJsonObject(accOjb);
                                    } catch (JSONException e){
                                        
                                    }
                                    if(accessoryJson != null){
                                        accessory = accessoryJson.getString("accessory");
                                    }
                                }
                                if(syncDownOne(db, body, accessory, syncid, gn_sim_id_map) > 0){
                                    count++;
                                } else {
                                    Log.i(TAG, "URI_SYNC_DOWN_MULTI jsonObject is " + jsonObject.toString());
                                    break;
                                }
                            } else {
                                Log.i(TAG, "URI_SYNC_DOWN_MULTI jsonObject is null");
                                break;
                            }
                        }
                        if(count == jsonArray.length()){
                            db.setTransactionSuccessful();
                            return Uri.withAppendedPath(uri, "true");
                        }
                    } finally {
                        db.endTransaction();
                    }
                }
            }
            return Uri.withAppendedPath(uri, "false");
        case URI_INIT_ACCOUNT:
        {
            Builder builder = uri.buildUpon();
            String syncid = values.getAsString("syncid");
            String localFlag = values.getAsString("localFlag");
            long date = values.getAsLong("date");
            int isdelete = values.getAsInteger("isdelete");
            if(!TextUtils.isEmpty(syncid) && !TextUtils.isEmpty(localFlag)){
                if(!syncInitOne(db, syncid, localFlag, date, isdelete)){
                    MyJsonObject result = new MyJsonObject();
                    MyJsonObject bodyResult = new MyJsonObject();
                    bodyResult.put("syncid", syncid);
                    result.put("body", bodyResult);
                    builder.appendQueryParameter("results",
                            result.toString());
                }
            }
            return builder.build();
        }
        case URI_INIT_ACCOUNT_MULTI:
        {
            Builder builder = uri.buildUpon();
            if (values.containsKey("multi")) {
                int count = 0;
                String multi = values.getAsString("multi");
                JSONArray jsonArray = null;
                try{
                    jsonArray = new JSONArray(multi);
                } catch (JSONException e){
                    
                }
                if(jsonArray != null && jsonArray.length() > 0){
                    for(int i = 0; i < jsonArray.length(); i++){
                        MyJsonObject jsonObject = null;
                        try{
                            JSONObject object = jsonArray.getJSONObject(i);
                            jsonObject = MyJsonObject.createFromJsonObject(object);
                        } catch (JSONException e){
                            
                        }
                        if(jsonObject != null){
                            String accOjb = jsonObject.getString("accOjb");
                            String accessory = null;
                            String body = jsonObject.getString("body");
                            if (!TextUtils.isEmpty(body)) {
                                MyJsonObject bodyJson = null;
                                try {
                                    bodyJson = new MyJsonObject(body);
                                } catch (JSONException e) {
                                    Log.e(TAG, "URI_SYNC_DOWN body wrong:" + body + ";e = "
                                            + e.toString());
                                }
                                if(bodyJson != null){
                                    String syncid = bodyJson.getString("syncid");
                                    String localFlag = bodyJson.getString("localFlag");
                                    long date = bodyJson.getLong("date");
                                    int isdelete = bodyJson.getInt("isdelete");
                                    if(!TextUtils.isEmpty(syncid) && !TextUtils.isEmpty(localFlag)){
                                        if(!syncInitOne(db, syncid, localFlag, date, isdelete)){
                                            MyJsonObject result = new MyJsonObject();
                                            MyJsonObject bodyResult = new MyJsonObject();
                                            bodyResult.put("syncid", syncid);
                                            result.put("body", bodyResult);
                                            builder.appendQueryParameter("results",
                                                    result.toString());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return builder.build();
        }
        }
        id = db.insert(table, null, values);
        if(TABLE_SYNC.equals(table) && id > 0){
            String localFlag = createLocalFlag(String.valueOf(id), System.currentTimeMillis());
            ContentValues updateValues = new ContentValues();
            updateValues.put(Sync.LOCAL_FLAG, localFlag);
            db.update(TABLE_SYNC, updateValues, Sync._ID + "=" + id, null);
        }
        if (isNeedNotifySync) {
            notifySync();
        }
        return Uri.withAppendedPath(uri, String.valueOf(id));
    }

    private int updateMms(SQLiteDatabase db, long msgid, MyJsonObject jsonObject, String accessory, Map<String, Integer> gn_sim_id_map) {
        int count = 0;
        JSONArray addrJsonArray = null;
        try {
            addrJsonArray = jsonObject.getJSONArray("addrs");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (addrJsonArray != null) {
            ArrayList<AddrObject> addrArray = new ArrayList<AddrObject>(
                    addrJsonArray.length());
            for (int i = 0; i < addrJsonArray.length(); i++) {
                JSONObject addrJsonObject = null;
                try {
                    addrJsonObject = addrJsonArray.getJSONObject(i);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (addrJsonObject != null) {
                    addrArray.add(new AddrObject(addrJsonObject));
                }
            }
            if (addrArray.size() > 0) {
                int messageBox = jsonObject.getInt(Mms.MESSAGE_BOX);
                Uri.Builder threadIdBuilder = THREAD_ID_URI.buildUpon();
                if(messageBox == android.provider.Telephony.Mms.MESSAGE_BOX_INBOX){
                    for (int i = 0; i < addrArray.size(); i++) {
                        String address = addrArray.get(i).address;
                        int addressType = Integer.valueOf(addrArray.get(i).type);
                        if(addressType == PduHeaders.FROM){
                            threadIdBuilder.appendQueryParameter("recipient",
                                    address);
                            break; //每条彩信的发送者应该只有一个，没必要过多循环
                        }
                    }
                } else if(messageBox == android.provider.Telephony.Mms.MESSAGE_BOX_SENT){
                    for (int i = 0; i < addrArray.size(); i++) {
                        String address = addrArray.get(i).address;
                        int addressType = Integer.valueOf(addrArray.get(i).type);
                        if(addressType == PduHeaders.TO){
                            threadIdBuilder.appendQueryParameter("recipient",
                                    address);
                        }
                    }
                }
                Uri threadIdUri = threadIdBuilder.build();
                ContentResolver contentResolver = getContext()
                        .getContentResolver();
                Cursor threadIdCursor = contentResolver.query(threadIdUri,
                        new String[] { android.provider.BaseColumns._ID },
                        null, null, null);
                if (threadIdCursor != null) {
                    if (threadIdCursor.moveToFirst()) {
                        long threadId = threadIdCursor.getLong(0);
                        long date = jsonObject.getLong("createTime");
                        ContentValues values = new ContentValues();
                        values.put(Mms.THREAD_ID, threadId);
                        values.put(Mms.DATE, date);
                        values.put(Mms.DATE_SENT,
                                jsonObject.getString(Mms.DATE_SENT));
                        values.put(Mms.MESSAGE_BOX,
                                jsonObject.getString(Mms.MESSAGE_BOX));
                        values.put(Mms.READ, jsonObject.getString(Mms.READ));
                        values.put(Mms.MESSAGE_ID,
                                jsonObject.getString(Mms.MESSAGE_ID));
                        values.put(Mms.SUBJECT,
                                jsonObject.getString(Mms.SUBJECT));
                        values.put(Mms.SUBJECT_CHARSET,
                                jsonObject.getString(Mms.SUBJECT_CHARSET));
                        values.put(Mms.CONTENT_TYPE,
                                jsonObject.getString(Mms.CONTENT_TYPE));
                        values.put(Mms.CONTENT_LOCATION,
                                jsonObject.getString(Mms.CONTENT_LOCATION));
                        values.put(Mms.EXPIRY,
                                jsonObject.getString(Mms.EXPIRY));
                        values.put(Mms.MESSAGE_CLASS,
                                jsonObject.getString(Mms.MESSAGE_CLASS));
                        values.put(Mms.MESSAGE_TYPE,
                                jsonObject.getString(Mms.MESSAGE_TYPE));
                        values.put(Mms.MMS_VERSION,
                                jsonObject.getString(Mms.MMS_VERSION));
                        values.put(Mms.MESSAGE_SIZE,
                                jsonObject.getString(Mms.MESSAGE_SIZE));
                        values.put(Mms.PRIORITY,
                                jsonObject.getString(Mms.PRIORITY));
                        values.put(Mms.READ_REPORT,
                                jsonObject.getString(Mms.READ_REPORT));
                        values.put(Mms.REPORT_ALLOWED,
                                jsonObject.getString(Mms.REPORT_ALLOWED));
                        values.put(Mms.RESPONSE_STATUS,
                                jsonObject.getString(Mms.RESPONSE_STATUS));
                        values.put(Mms.STATUS,
                                jsonObject.getString(Mms.STATUS));
                        values.put(Mms.TRANSACTION_ID,
                                jsonObject.getString(Mms.TRANSACTION_ID));
                        values.put(Mms.RETRIEVE_STATUS,
                                jsonObject.getString(Mms.RETRIEVE_STATUS));
                        values.put(Mms.RETRIEVE_TEXT,
                                jsonObject.getString(Mms.RETRIEVE_TEXT));
                        values.put(Mms.RETRIEVE_TEXT_CHARSET, jsonObject
                                .getString(Mms.RETRIEVE_TEXT_CHARSET));
                        values.put(Mms.READ_STATUS,
                                jsonObject.getString(Mms.READ_STATUS));
                        values.put(Mms.CONTENT_CLASS,
                                jsonObject.getString(Mms.CONTENT_CLASS));
                        values.put(Mms.RESPONSE_TEXT,
                                jsonObject.getString(Mms.RESPONSE_TEXT));
                        values.put(Mms.DELIVERY_TIME,
                                jsonObject.getString(Mms.DELIVERY_TIME));
                        values.put(Mms.DELIVERY_REPORT,
                                jsonObject.getString(Mms.DELIVERY_REPORT));
                        values.put(Mms.LOCKED,
                                jsonObject.getString(Mms.LOCKED));
                        int simId = getSimId(gn_sim_id_map, jsonObject.getString(Mms.SIM_ID));
                        if(simId > 0){
                            values.put(Mms.SIM_ID,simId);
                        }
                        values.put("service_center",
                                jsonObject.getString("service_center"));
                        values.put(Mms.SEEN, jsonObject.getString(Mms.SEEN));
                        values.put("update_threads",
                                jsonObject.getString("update_threads"));
                        values.put("reject", jsonObject.getString("reject"));
                        values.put("weather_info", jsonObject.getString("weather_info"));
                        db.beginTransaction();
                        try{
                            db.delete(MmsProvider.TABLE_ADDR, Addr.MSG_ID + "=" + msgid, null);
                            db.delete(MmsProvider.TABLE_PART, Part.MSG_ID + "=" + msgid, null);
                            count = db.update(MmsProvider.TABLE_PDU, values, Mms._ID + "=" + msgid, null);
                            if(count > 0){
                                int addrCount = insertAddrs(db, msgid, addrArray);
                                if (addrCount < addrArray.size()) {
                                    count = 0;
                                } else {
                                    JSONArray jsonArray = null;
                                    try {
                                        jsonArray = jsonObject.getJSONArray("parts");
                                    } catch (JSONException e) {

                                    }
                                    if (jsonArray != null
                                            && jsonArray.length() > 0) {
                                        int accessoryCount = insertPartFromDown(db,
                                                msgid, jsonArray, accessory);
                                        if (accessoryCount < jsonArray
                                                .length()) {
                                            count = 0;
                                            Log.e(TAG,"parts insert failed.");
                                        } else {
                                            db.setTransactionSuccessful();
                                        }
                                    } else {
                                        count = 0;
                                    }
                                }
                            }
                        } catch(Exception e){
                            count = 0;
                        } finally{
                            db.endTransaction();
                        }
                    }
                    threadIdCursor.close();
                }
            }
        } else {
            Log.e(TAG,"get addrs failed");
        }
        return count;
    }

    private long insertMms(SQLiteDatabase db, MyJsonObject jsonObject, String accessory, Map<String, Integer> gn_sim_id_map) {
        long id = -1;
        JSONArray addrJsonArray = null;
        try {
            addrJsonArray = jsonObject.getJSONArray("addrs");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (addrJsonArray != null) {
            ArrayList<AddrObject> addrArray = new ArrayList<AddrObject>(
                    addrJsonArray.length());
            for (int i = 0; i < addrJsonArray.length(); i++) {
                JSONObject addrJsonObject = null;
                try {
                    addrJsonObject = addrJsonArray.getJSONObject(i);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (addrJsonObject != null) {
                    addrArray.add(new AddrObject(addrJsonObject));
                }
            }
            if (addrArray.size() > 0) {
                int messageBox = jsonObject.getInt(Mms.MESSAGE_BOX);
                Uri.Builder threadIdBuilder = THREAD_ID_URI.buildUpon();
                if(messageBox == android.provider.Telephony.Mms.MESSAGE_BOX_INBOX){
                    for (int i = 0; i < addrArray.size(); i++) {
                        String address = addrArray.get(i).address;
                        int addressType = Integer.valueOf(addrArray.get(i).type);
                        if(addressType == PduHeaders.FROM){
                            threadIdBuilder.appendQueryParameter("recipient",
                                    address);
                            break; //每条彩信的发送者应该只有一个，没必要过多循环
                        }
                    }
                } else if(messageBox == android.provider.Telephony.Mms.MESSAGE_BOX_SENT){
                    for (int i = 0; i < addrArray.size(); i++) {
                        String address = addrArray.get(i).address;
                        int addressType = Integer.valueOf(addrArray.get(i).type);
                        if(addressType == PduHeaders.TO){
                            threadIdBuilder.appendQueryParameter("recipient",
                                    address);
                        }
                    }
                }
                Uri threadIdUri = threadIdBuilder.build();
                ContentResolver contentResolver = getContext()
                        .getContentResolver();
                Cursor threadIdCursor = contentResolver.query(threadIdUri,
                        new String[] { android.provider.BaseColumns._ID },
                        null, null, null);
                if (threadIdCursor != null) {
                    if (threadIdCursor.moveToFirst()) {
                        long threadId = threadIdCursor.getLong(0);
                        long date = jsonObject.getLong("createTime");
                        ContentValues values = new ContentValues();
                        values.put(Mms.THREAD_ID, threadId);
                        values.put(Mms.DATE, date);
                        values.put(Mms.DATE_SENT,
                                jsonObject.getString(Mms.DATE_SENT));
                        values.put(Mms.MESSAGE_BOX,
                                jsonObject.getString(Mms.MESSAGE_BOX));
                        values.put(Mms.READ, jsonObject.getString(Mms.READ));
                        values.put(Mms.MESSAGE_ID,
                                jsonObject.getString(Mms.MESSAGE_ID));
                        values.put(Mms.SUBJECT,
                                jsonObject.getString(Mms.SUBJECT));
                        values.put(Mms.SUBJECT_CHARSET,
                                jsonObject.getString(Mms.SUBJECT_CHARSET));
                        values.put(Mms.CONTENT_TYPE,
                                jsonObject.getString(Mms.CONTENT_TYPE));
                        values.put(Mms.CONTENT_LOCATION,
                                jsonObject.getString(Mms.CONTENT_LOCATION));
                        values.put(Mms.EXPIRY,
                                jsonObject.getString(Mms.EXPIRY));
                        values.put(Mms.MESSAGE_CLASS,
                                jsonObject.getString(Mms.MESSAGE_CLASS));
                        values.put(Mms.MESSAGE_TYPE,
                                jsonObject.getString(Mms.MESSAGE_TYPE));
                        values.put(Mms.MMS_VERSION,
                                jsonObject.getString(Mms.MMS_VERSION));
                        values.put(Mms.MESSAGE_SIZE,
                                jsonObject.getString(Mms.MESSAGE_SIZE));
                        values.put(Mms.PRIORITY,
                                jsonObject.getString(Mms.PRIORITY));
                        values.put(Mms.READ_REPORT,
                                jsonObject.getString(Mms.READ_REPORT));
                        values.put(Mms.REPORT_ALLOWED,
                                jsonObject.getString(Mms.REPORT_ALLOWED));
                        values.put(Mms.RESPONSE_STATUS,
                                jsonObject.getString(Mms.RESPONSE_STATUS));
                        values.put(Mms.STATUS,
                                jsonObject.getString(Mms.STATUS));
                        values.put(Mms.TRANSACTION_ID,
                                jsonObject.getString(Mms.TRANSACTION_ID));
                        values.put(Mms.RETRIEVE_STATUS,
                                jsonObject.getString(Mms.RETRIEVE_STATUS));
                        values.put(Mms.RETRIEVE_TEXT,
                                jsonObject.getString(Mms.RETRIEVE_TEXT));
                        values.put(Mms.RETRIEVE_TEXT_CHARSET, jsonObject
                                .getString(Mms.RETRIEVE_TEXT_CHARSET));
                        values.put(Mms.READ_STATUS,
                                jsonObject.getString(Mms.READ_STATUS));
                        values.put(Mms.CONTENT_CLASS,
                                jsonObject.getString(Mms.CONTENT_CLASS));
                        values.put(Mms.RESPONSE_TEXT,
                                jsonObject.getString(Mms.RESPONSE_TEXT));
                        values.put(Mms.DELIVERY_TIME,
                                jsonObject.getString(Mms.DELIVERY_TIME));
                        values.put(Mms.DELIVERY_REPORT,
                                jsonObject.getString(Mms.DELIVERY_REPORT));
                        values.put(Mms.LOCKED,
                                jsonObject.getString(Mms.LOCKED));
                        int simId = getSimId(gn_sim_id_map, jsonObject.getString(Mms.SIM_ID));
                        if(simId > 0){
                            values.put(Mms.SIM_ID,simId);
                        }
                        values.put("service_center",
                                jsonObject.getString("service_center"));
                        values.put(Mms.SEEN, jsonObject.getString(Mms.SEEN));
                        values.put("update_threads",
                                jsonObject.getString("update_threads"));
                        values.put("reject", jsonObject.getString("reject"));
                        values.put("weather_info", jsonObject.getString("weather_info"));
                        long pudId = db.insert(MmsProvider.TABLE_PDU, null, values);
                        if (pudId >= 0) {
                            int addrCount = insertAddrs(db, pudId, addrArray);
                            if (addrCount < addrArray.size()) {
                                // 回退pud
                                db.delete(MmsProvider.TABLE_PDU, Mms._ID + "=" + pudId, null);
                                Log.e(TAG,"addrs insert failed.");
                            } else {
                                JSONArray jsonArray = null;
                                try {
                                    jsonArray = jsonObject.getJSONArray("parts");
                                } catch (JSONException e) {

                                }
                                if (jsonArray != null
                                        && jsonArray.length() > 0) {
                                    int accessoryCount = insertPartFromDown(db,
                                            pudId, jsonArray, accessory);
                                    if (accessoryCount < jsonArray
                                            .length()) {
                                        // 回退pud和addr
                                        db.delete(MmsProvider.TABLE_ADDR, Addr.MSG_ID + "=" + pudId, null);
                                        db.delete(MmsProvider.TABLE_PDU, Mms._ID + "=" + pudId, null);
                                        Log.e(TAG,"parts insert failed.");
                                    } else {
                                        id = pudId;
                                    }
                                }
                            }
                        }
                    }
                    threadIdCursor.close();
                }
            }
        } else {
            Log.e(TAG,"get addrs failed");
        }
        if(id > 0){
            tempUsedFile.clear();
            Editor et = mSp.edit();
            et.putStringSet(TEMP_USED_FILE, tempUsedFile);
            et.apply();
        }
        return id;
    }
    
    private int getSimId(Map<String, Integer> gn_sim_id_map, String icc){
        int id = -1;
        if(!TextUtils.isEmpty(icc)){
            if(gn_sim_id_map.containsKey(icc)){
                id = gn_sim_id_map.get(icc);
            } else {
                ContentValues values = new ContentValues();
                values.put(SimInfo.ICC_ID, icc);
                Uri simIdUri = getContext().getContentResolver().insert(SIM_ID_URI, values);
                if(simIdUri != null){
                    int simId = Integer.valueOf(simIdUri.getLastPathSegment());
                    if(simId > 0){
                        gn_sim_id_map.put(icc, simId);
                        id = simId;
                    }
                }
            }
        }
        return id;
    }

    private int insertAddrs(SQLiteDatabase db, long pudid, ArrayList<AddrObject> addrArray) {
        int cout = 0;
        ContentValues values = new ContentValues();
        for (int i = 0; i < addrArray.size(); i++) {
            values.clear();
            AddrObject addrObject = addrArray.get(i);
            values.put(Addr.MSG_ID, pudid);
            values.put(Addr.CONTACT_ID, addrObject.contactId);
            values.put(Addr.ADDRESS, addrObject.address);
            values.put(Addr.TYPE, addrObject.type);
            values.put(Addr.CHARSET, addrObject.charset);
            values.put("reject", addrObject.reject);
            if (db.insert(MmsProvider.TABLE_ADDR, null, values) <= 0) {
                break;
            }
            cout++;
        }
        if (cout < addrArray.size()) {
            db.delete(MmsProvider.TABLE_ADDR, Addr.MSG_ID + "=" + pudid, null);
        }
        return cout;
    }

    private int updateSms(SQLiteDatabase db, long msgid, MyJsonObject bodyJson, Map<String, Integer> gn_sim_id_map) {
        int count = 0;
        String address = bodyJson.getString("address");
        if (!TextUtils.isEmpty(address)) {
            Uri.Builder threadIdBuilder = THREAD_ID_URI.buildUpon();
            threadIdBuilder.appendQueryParameter("recipient", address);
            Uri threadIdUri = threadIdBuilder.build();
            ContentResolver contentResolver = getContext().getContentResolver();
            Cursor threadIdCursor = contentResolver.query(threadIdUri,
                    new String[] { android.provider.BaseColumns._ID }, null,
                    null, null);
            if (threadIdCursor != null) {
                if (threadIdCursor.moveToFirst()) {
                    long threadId = threadIdCursor.getLong(0);
                    ContentValues values = new ContentValues();
                    values.put("address", address);
                    values.put("thread_id", threadId);
                    values.put("m_size", bodyJson.getString("m_size"));
                    values.put("person", bodyJson.getString("person"));
                    values.put("date", bodyJson.getString("createTime"));
                    values.put("date_sent", bodyJson.getString("date_sent"));
                    values.put("protocol", bodyJson.getString("protocol"));
                    values.put("read", bodyJson.getString("read"));
                    values.put("status", bodyJson.getString("status"));
                    values.put("type", bodyJson.getString("type"));
                    values.put("reply_path_present",
                            bodyJson.getString("reply_path_present"));
                    values.put("subject", bodyJson.getString("subject"));
                    values.put("body", bodyJson.getString("body"));
                    values.put("service_center",
                            bodyJson.getString("service_center"));
                    values.put("locked", bodyJson.getString("locked"));
                    int simId = getSimId(gn_sim_id_map, bodyJson.getString(GnTelephony.GN_SIM_ID));
                    if(simId > 0){
                        values.put(GnTelephony.GN_SIM_ID,simId);
                    }
                    values.put("error_code", bodyJson.getString("error_code"));
                    values.put("seen", bodyJson.getString("seen"));
                    values.put("update_threads",
                            bodyJson.getString("update_threads"));
                    values.put("reject", bodyJson.getString("reject"));
                    values.put("weather_info", bodyJson.getString("weather_info"));
                    count = db.update(SmsProvider.TABLE_SMS, values, "_id = " + msgid, null);
                }
                threadIdCursor.close();
            }
        }
        return count;
    }

    private long insertSms(SQLiteDatabase db, MyJsonObject bodyJson, Map<String, Integer> gn_sim_id_map) {
        long id = -1;
        String address = bodyJson.getString("address");
        if (!TextUtils.isEmpty(address)) {
            Uri.Builder threadIdBuilder = THREAD_ID_URI.buildUpon();
            threadIdBuilder.appendQueryParameter("recipient", address);
            Uri threadIdUri = threadIdBuilder.build();
            ContentResolver contentResolver = getContext().getContentResolver();
            Cursor threadIdCursor = contentResolver.query(threadIdUri,
                    new String[] { android.provider.BaseColumns._ID }, null,
                    null, null);
            if (threadIdCursor != null) {
                if (threadIdCursor.moveToFirst()) {
                    long threadId = threadIdCursor.getLong(0);
                    ContentValues values = new ContentValues();
                    values.put("address", address);
                    values.put("thread_id", threadId);
                    values.put("m_size", bodyJson.getString("m_size"));
                    values.put("person", bodyJson.getString("person"));
                    values.put("date", bodyJson.getString("createTime"));
                    values.put("date_sent", bodyJson.getString("date_sent"));
                    values.put("protocol", bodyJson.getString("protocol"));
                    values.put("read", bodyJson.getString("read"));
                    values.put("status", bodyJson.getString("status"));
                    values.put("type", bodyJson.getString("type"));
                    values.put("reply_path_present",
                            bodyJson.getString("reply_path_present"));
                    values.put("subject", bodyJson.getString("subject"));
                    values.put("body", bodyJson.getString("body"));
                    values.put("service_center",
                            bodyJson.getString("service_center"));
                    values.put("locked", bodyJson.getString("locked"));
                    int simId = getSimId(gn_sim_id_map, bodyJson.getString(GnTelephony.GN_SIM_ID));
                    if(simId > 0){
                        values.put(GnTelephony.GN_SIM_ID,simId);
                    }
                    values.put("error_code", bodyJson.getString("error_code"));
                    values.put("seen", bodyJson.getString("seen"));
                    values.put("update_threads",
                            bodyJson.getString("update_threads"));
                    values.put("reject", bodyJson.getString("reject"));
                    values.put("weather_info", bodyJson.getString("weather_info"));
                    id = db.insert(SmsProvider.TABLE_SMS, null, values);
                }
                threadIdCursor.close();
            }
        }
        return id;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        String table = "";
        String limit = "";
        switch (URI_MATCHER.match(uri)) {
        case URI_SYNC:
            table = TABLE_SYNC;
            break;
        case URI_SYNC_ID:
            String syncId = uri.getPathSegments().get(1);
            selection = TextUtils.isEmpty(selection) ? Sync.SYNC_ID + " = " + syncId
                    : selection + " and " + Sync.SYNC_ID + " = " + syncId;
            table = TABLE_SYNC;
            break;
        case URI_PART_SYNC_ID:
            String partSyncId = uri.getPathSegments().get(1);
            selection = TextUtils.isEmpty(selection) ? SyncPart.SYNC_ID + " = " + partSyncId
                    : selection + " and " + SyncPart.SYNC_ID + " = " + partSyncId;
            table = PART_SYNC;
            break;
        case URI_SYNC_ACCESSORY:
            String id = uri.getQueryParameter("syncid");
            String path = uri.getQueryParameter("path");
            return getSyncPath(id, path);
        case URI_SYNC_UP_LIMIT_ID:
            selection = SYNC_UP_QUREY_SQL;
            limit = uri.getPathSegments().get(1) + ","
                    + uri.getPathSegments().get(2);
            return getSyncUpQuery(db, selection, selectionArgs, sortOrder,
                    limit);
        case URI_SYNC_UP_COUNT_ID:
            selection = SYNC_UP_QUREY_SQL;
            limit = uri.getLastPathSegment();
            return getSyncUpQuery(db, selection, selectionArgs, sortOrder,
                    limit);
        case URI_SYNC_UP:
            selection = SYNC_UP_QUREY_SQL;
            return getSyncUpQuery(db, selection, selectionArgs, sortOrder,
                    limit);
        case URI_SYNC_CONTINUE_ID:
            String continueId = uri.getLastPathSegment();
            return getSyncUpContinueQuery(db, selection, selectionArgs, sortOrder,
                    continueId);
        case URI_SYNC_UP_COUNT_SIZE:
        	if(!isSyncDbOk()) {
        		cleanUpSyncDb();
        	}
            return db.query(SYNC_VIEW,
                    new String[] { "count() as 'size'" }, SYNC_UP_QUREY_SQL,
                    null, null, null, null);
        case URI_SYNC_PART_PART_ID:
            String partId = uri.getPathSegments().get(0);
            Cursor partIdCursor = db.query(PART_SYNC, null, SyncPart.PART_ID + " = ?",
                    new String[] { partId }, null, null, null);
            if (partIdCursor == null || !partIdCursor.moveToFirst()) {
                Uri insertPartIdUri = insert(uri, null);
                long insertPartId = Long.valueOf(insertPartIdUri
                        .getLastPathSegment());
                if (insertPartId >= 0) {
                    partIdCursor = db.query(PART_SYNC, null, SyncPart.PART_ID +" = ?",
                            new String[] { partId }, null, null, null);
                }
            }
            return partIdCursor;
        case URI_IS_FIRST_SYNC:
            return getIsFirstSync();
        }
        Cursor cursor = db.query(table, projection, selection, selectionArgs,
                null, null, sortOrder);
        return cursor;
    }

    private Cursor getIsFirstSync() {
        MatrixCursor cursor = new MatrixCursor(new String[]{"path"},
                1);
        Object[] rawObject = new Object[1];
        boolean isFirstSync = mSp.getBoolean("is_first_sync", false);
        rawObject[0] = !isFirstSync;
        cursor.addRow(rawObject);
        if(!isFirstSync){
            Editor et = mSp.edit();
            et.putBoolean("is_first_sync", true);
            et.commit();
        }
        return cursor;
    }

    private Cursor getSyncPath(String syncid, String path) {
        MatrixCursor cursor = new MatrixCursor(new String[]{"path"},
                1);
        Object[] rawObject = new Object[1];
        File pathFile = new File(path);
        String name = pathFile.getName();
        File fileDir = getContext().getDir("parts", Context.MODE_PRIVATE);
        File file = new File(fileDir, name);
        if (file.exists()) {
            int i = 0;
            do {
                String temp = String.valueOf(i++);
                file = new File(fileDir, temp + name);
            } while (file.exists());
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            Log.w(TAG, "file " + file.getAbsolutePath()
                    + " can't be create new file.");
        }
        if (file.exists()) {
            rawObject[0] = file.getAbsolutePath();
        } else {
            rawObject[0] = null;
        }
        cursor.addRow(rawObject);
        return cursor;
    }
    
    private Cursor getSyncUpContinueQuery(SQLiteDatabase db, String selection,
            String[] selectionArgs, String sortOrder, String continueId) {
        MatrixCursor sync_up = null;
        selection = TextUtils.isEmpty(selection)?Sync._ID + "=" + continueId:selection + " and " + Sync._ID + "=" + continueId;
        Cursor sync_up_1 = db.query(SYNC_VIEW, null, selection, selectionArgs,
                null, null, sortOrder);
        Object[] rawObject = null;
        if (sync_up_1 != null) {
            if(sync_up_1.getCount() > 0 && sync_up_1.moveToFirst()){
                SparseArray< String> gn_sim_id_map = new SparseArray< String>();
                Cursor sim_id_cursor = getContext().getContentResolver().query(SIM_ID_URI, new String[]{SimInfo._ID, SimInfo.ICC_ID}, null, null, null);
                if(sim_id_cursor != null){
                    while(sim_id_cursor.moveToNext()){
                        int simId = sim_id_cursor.getInt(0);
                        String SimIccId = sim_id_cursor.getString(1);
                        gn_sim_id_map.put(simId, SimIccId);
                    }
                    sim_id_cursor.close();
                }
                rawObject = getRawObjectFromSyncUpCursor(sync_up_1,
                        gn_sim_id_map);
                if (rawObject == null) {
                    rawObject = getDeleteSyncBody(Long.valueOf(continueId), -1, -1L, "-1", System.currentTimeMillis()/1000, "");
                }
            } else {
                rawObject = getDeleteSyncBody(Long.valueOf(continueId), -1, -1L, "-1", System.currentTimeMillis()/1000, "");
            }
            sync_up_1.close();
        } else {
            rawObject = getDeleteSyncBody(Long.valueOf(continueId), -1, -1L, "-1", System.currentTimeMillis()/1000, "");
        }
        if(rawObject != null){
            sync_up = new MatrixCursor(SYNC_SELECTION_ARGS, 1);
            sync_up.addRow(rawObject);
        }
        return sync_up;
    }

    private Cursor getSyncUpQuery(SQLiteDatabase db, String selection,
            String[] selectionArgs, String sortOrder, String limit) {
        MatrixCursor sync_up = null;
        Cursor sync_up_1 = db.query(SYNC_VIEW, null, selection, selectionArgs,
                null, null, sortOrder, limit);
        if (sync_up_1 != null) {
            if(sync_up_1.getCount() > 0){
                sync_up = new MatrixCursor(SYNC_SELECTION_ARGS,
                        sync_up_1.getCount());
                SparseArray< String> gn_sim_id_map = new SparseArray< String>();
                Cursor sim_id_cursor = getContext().getContentResolver().query(SIM_ID_URI, new String[]{SimInfo._ID, SimInfo.ICC_ID}, null, null, null);
                if(sim_id_cursor != null){
                    while(sim_id_cursor.moveToNext()){
                        int simId = sim_id_cursor.getInt(0);
                        String SimIccId = sim_id_cursor.getString(1);
                        gn_sim_id_map.put(simId, SimIccId);
                    }
                    sim_id_cursor.close();
                }
                while (sync_up_1.moveToNext()) {
                    Object[] rawObject = getRawObjectFromSyncUpCursor(sync_up_1,
                            gn_sim_id_map);
                    if (rawObject != null) {
                        sync_up.addRow(rawObject);
                    } else {
                        Log.e(TAG,
                                "can't get rawObject, msgtype = "
                                        + sync_up_1.getString(sync_up_1
                                                .getColumnIndex("msgtype"))
                                        + ";msgid= "
                                        + sync_up_1.getString(sync_up_1
                                                .getColumnIndex("msgid")));
                    }
                }
            } else {
                sync_up = new MatrixCursor(SYNC_SELECTION_ARGS,
                        0);
            }
            sync_up_1.close();
        }
        return sync_up;
    }

    private Object[] getRawObjectFromSyncUpCursor(Cursor cursor,
            SparseArray< String> gn_sim_id_map) {
        Object[] rawObject = null;
        int isDelete = cursor.getInt(cursor.getColumnIndex(Sync.ISDELETE));
        Long id = cursor.getLong(cursor.getColumnIndex(Sync._ID));
        Long msgid = cursor.getLong(cursor.getColumnIndex(Sync.MSG_ID));
        String syncid = cursor.getString(cursor.getColumnIndex(Sync.SYNC_ID));
        int msgtype = cursor.getInt(cursor.getColumnIndex(Sync.MSG_TYPE));
        long date = cursor.getLong(cursor.getColumnIndex(Sync.UPDATE_DATE));
        String localFlag = cursor.getString(cursor.getColumnIndex(Sync.LOCAL_FLAG));
        switch (isDelete) {
        case 0:
            switch (msgtype) {
            case MMS_TYPE:
                rawObject = getMMSObjectFromSyncUpCursor(cursor, id, msgid, syncid, date, localFlag, gn_sim_id_map);
                break;
            case SMS_TYPE:
                rawObject = getSMSObjectFromSyncUpCursor(cursor, id, msgid, syncid, date, localFlag, gn_sim_id_map);
                break;
            }
            break;
        case 1:
            rawObject = getDeleteSyncBody(id, msgtype, msgid, syncid, date, localFlag);
            break;
        }
        return rawObject;
    }

    private Object[] getDeleteSyncBody(long id, int msgtype, long msgid, String syncid, long date, String localFlag) {
        Object[] rawObject = new Object[3];
        MyJsonObject object = new MyJsonObject();
        object.put("id", id);
        object.put("msgtype", msgtype);
        object.put("msgid", msgid);
        object.put("syncid", syncid);
        object.put("isdelete", 1);
        object.put("localFlag", localFlag);
        object.put("date", date);
        rawObject[0] = object.toString();
        rawObject[1] = null;
        rawObject[2] = getInfo(id, 2, date, syncid);
        return rawObject;
    }
    
    private String getInfo(long id, int op, long date, String syncId){
        MyJsonObject object = new MyJsonObject();
        object.put("id", id);
        object.put("op", op);
        object.put("syncid", syncId);
        object.put("date", date);
        return object.toString();
    }

    private Object[] getMMSObjectFromSyncUpCursor(Cursor cursor, long id, long msgid, String syncid, long date, String localFlag,
            SparseArray< String> gn_sim_id_map) {
        Object[] rawObject = null;
        SQLiteDatabase qb = mOpenHelper.getReadableDatabase();
        rawObject = new Object[3];
        long DATE = cursor.getLong(cursor.getColumnIndex(Sync.DATE));
        long DATE_SENT = cursor.getInt(cursor
                .getColumnIndex(Sync.DATE_SENT));
        String MESSAGE_BOX = cursor.getString(cursor
                .getColumnIndex(Sync.MESSAGE_BOX));
        String READ = cursor.getString(cursor.getColumnIndex(Sync.READ));
        String MESSAGE_ID = cursor.getString(cursor
                .getColumnIndex(Sync.MESSAGE_ID));
        String SUBJECT = cursor.getString(cursor
                .getColumnIndex(Sync.SUBJECT));
        String SUBJECT_CHARSET = cursor.getString(cursor
                .getColumnIndex(Sync.SUBJECT_CHARSET));
        String CONTENT_TYPE = cursor.getString(cursor
                .getColumnIndex(Sync.CONTENT_TYPE));
        String CONTENT_LOCATION = cursor.getString(cursor
                .getColumnIndex(Sync.CONTENT_LOCATION));
        String EXPIRY = cursor.getString(cursor
                .getColumnIndex(Sync.EXPIRY));
        String MESSAGE_CLASS = cursor.getString(cursor
                .getColumnIndex(Sync.MESSAGE_CLASS));
        String MESSAGE_TYPE = cursor.getString(cursor
                .getColumnIndex(Sync.MESSAGE_TYPE));
        String MMS_VERSION = cursor.getString(cursor
                .getColumnIndex(Sync.MMS_VERSION));
        String MESSAGE_SIZE = cursor.getString(cursor
                .getColumnIndex(Sync.MESSAGE_SIZE));
        String PRIORITY = cursor.getString(cursor
                .getColumnIndex(Sync.PRIORITY));
        String READ_REPORT = cursor.getString(cursor
                .getColumnIndex(Sync.READ_REPORT));
        String REPORT_ALLOWED = cursor.getString(cursor
                .getColumnIndex(Sync.REPORT_ALLOWED));
        String RESPONSE_STATUS = cursor.getString(cursor
                .getColumnIndex(Sync.RESPONSE_STATUS));
        String STATUS = cursor.getString(cursor
                .getColumnIndex(Sync.STATUS));
        String TRANSACTION_ID = cursor.getString(cursor
                .getColumnIndex(Sync.TRANSACTION_ID));
        String RETRIEVE_STATUS = cursor.getString(cursor
                .getColumnIndex(Sync.RETRIEVE_STATUS));
        String RETRIEVE_TEXT = cursor.getString(cursor
                .getColumnIndex(Sync.RETRIEVE_TEXT));
        String RETRIEVE_TEXT_CHARSET = cursor.getString(cursor
                .getColumnIndex(Sync.RETRIEVE_TEXT_CHARSET));
        String READ_STATUS = cursor.getString(cursor
                .getColumnIndex(Sync.READ_STATUS));
        String CONTENT_CLASS = cursor.getString(cursor
                .getColumnIndex(Sync.CONTENT_CLASS));
        String RESPONSE_TEXT = cursor.getString(cursor
                .getColumnIndex(Sync.RESPONSE_TEXT));
        String DELIVERY_TIME = cursor.getString(cursor
                .getColumnIndex(Sync.DELIVERY_TIME));
        String DELIVERY_REPORT = cursor.getString(cursor
                .getColumnIndex(Sync.DELIVERY_REPORT));
        String LOCKED = cursor.getString(cursor
                .getColumnIndex(Sync.LOCKED));
        int SIM_ID = cursor.getInt(cursor
                .getColumnIndex(Sync.SIM_ID));
        String service_center = cursor.getString(cursor
                .getColumnIndex(Sync.SERVICE_CENTER));
        String SEEN = cursor.getString(cursor.getColumnIndex(Sync.SEEN));
        String update_threads = cursor.getString(cursor
                .getColumnIndex(Sync.UPDATE_THREADS));
        String reject = cursor.getString(cursor
                .getColumnIndex(Sync.REJECT));
        String weatherInfo = cursor.getString(cursor
                .getColumnIndex(Sync.WEATHER_INFO));
        JSONArray addrs = getAddrs(qb, msgid);
        JSONArray parts = getParts(qb, msgid, rawObject);
        MyJsonObject object = new MyJsonObject();
        object.put("id", id);
        object.put("date", date);
        object.put("syncid", syncid);
        object.put("isdelete", 0);
        object.put("msgid", msgid);
        object.put("msgtype", MMS_TYPE);
        object.put("createTime", DATE);
        object.put("localFlag", localFlag);
        object.put(Mms.DATE_SENT, DATE_SENT);
        object.put(Mms.MESSAGE_BOX, MESSAGE_BOX);
        object.put(Mms.READ, READ);
        object.put(Mms.MESSAGE_ID, MESSAGE_ID);
        object.put(Mms.SUBJECT, SUBJECT);
        object.put(Mms.SUBJECT_CHARSET, SUBJECT_CHARSET);
        object.put(Mms.CONTENT_TYPE, CONTENT_TYPE);
        object.put(Mms.CONTENT_LOCATION, CONTENT_LOCATION);
        object.put(Mms.EXPIRY, EXPIRY);
        object.put(Mms.MESSAGE_CLASS, MESSAGE_CLASS);
        object.put(Mms.MESSAGE_TYPE, MESSAGE_TYPE);
        object.put(Mms.MMS_VERSION, MMS_VERSION);
        object.put(Mms.MESSAGE_SIZE, MESSAGE_SIZE);
        object.put(Mms.PRIORITY, PRIORITY);
        object.put(Mms.READ_REPORT, READ_REPORT);
        object.put(Mms.REPORT_ALLOWED, REPORT_ALLOWED);
        object.put(Mms.RESPONSE_STATUS, RESPONSE_STATUS);
        object.put(Mms.STATUS, STATUS);
        object.put(Mms.TRANSACTION_ID, TRANSACTION_ID);
        object.put(Mms.RETRIEVE_STATUS, RETRIEVE_STATUS);
        object.put(Mms.RETRIEVE_TEXT, RETRIEVE_TEXT);
        object.put(Mms.RETRIEVE_TEXT_CHARSET, RETRIEVE_TEXT_CHARSET);
        object.put(Mms.READ_STATUS, READ_STATUS);
        object.put(Mms.CONTENT_CLASS, CONTENT_CLASS);
        object.put(Mms.RESPONSE_TEXT, RESPONSE_TEXT);
        object.put(Mms.DELIVERY_TIME, DELIVERY_TIME);
        object.put(Mms.DELIVERY_REPORT, DELIVERY_REPORT);
        object.put(Mms.LOCKED, LOCKED);
        object.put(Mms.SIM_ID, gn_sim_id_map.get(SIM_ID));
        object.put("service_center", service_center);
        object.put(Mms.SEEN, SEEN);
        object.put("update_threads", update_threads);
        object.put("reject", reject);
        object.put("addrs", addrs);
        object.put("parts", parts);
        object.put("weather_info", weatherInfo);
        rawObject[0] = object.toString();
        if(TextUtils.isEmpty(syncid)){
            rawObject[2] = getInfo(id, 0, date, syncid);
        } else {
            rawObject[2] = getInfo(id, 1, date, syncid);
        }
        return rawObject;
    }

    private JSONArray getAddrs(SQLiteDatabase qb, long pduId) {
        JSONArray jsonArray = null;
        Cursor cursor = qb.query(MmsProvider.TABLE_ADDR, null, Addr.MSG_ID + "=" + pduId, null, null, null, null);
        if (cursor != null) {
            jsonArray = new JSONArray();
            while (cursor.moveToNext()) {
                String CONTACT_ID = cursor.getString(cursor
                        .getColumnIndex(Addr.CONTACT_ID));
                String ADDRESS = cursor.getString(cursor
                        .getColumnIndex(Addr.ADDRESS));
                String TYPE = cursor.getString(cursor.getColumnIndex(Addr.TYPE));
                String CHARSET = cursor
                        .getString(cursor.getColumnIndex(Addr.CHARSET));
                String reject = cursor.getString(cursor.getColumnIndex("reject"));
                MyJsonObject object = new MyJsonObject();
                object.put(Addr.CONTACT_ID, CONTACT_ID);
                object.put(Addr.ADDRESS, ADDRESS);
                object.put(Addr.TYPE, TYPE);
                object.put(Addr.CHARSET, CHARSET);
                object.put("reject", reject);
                jsonArray.put(object);
            }
            cursor.close();
        }
        return jsonArray;
    }

    private JSONArray getParts(SQLiteDatabase qb, long pduId, Object[] rawObject) {
        JSONArray jsonArray = null;
        Cursor cursor = qb.query(MmsProvider.TABLE_PART, null, Part.MSG_ID + "=" + pduId, null, null, null, null);
        JSONArray accessoryJsonArray = new JSONArray();
        if (cursor != null) {
            jsonArray = new JSONArray();
            while (cursor.moveToNext()) {
                long _ID = cursor.getLong(cursor.getColumnIndex(Part._ID));
                String SEQ = cursor.getString(cursor.getColumnIndex(Part.SEQ));
                String CONTENT_TYPE = cursor.getString(cursor
                        .getColumnIndex(Part.CONTENT_TYPE));
                String NAME = cursor
                        .getString(cursor.getColumnIndex(Part.NAME));
                String CHARSET = cursor
                        .getString(cursor.getColumnIndex(Part.CHARSET));
                String CONTENT_DISPOSITION = cursor.getString(cursor
                        .getColumnIndex(Part.CONTENT_DISPOSITION));
                String FILENAME = cursor.getString(cursor
                        .getColumnIndex(Part.FILENAME));
                String CONTENT_ID = cursor.getString(cursor
                        .getColumnIndex(Part.CONTENT_ID));
                String CONTENT_LOCATION = cursor.getString(cursor
                        .getColumnIndex(Part.CONTENT_LOCATION));
                String CT_START = cursor.getString(cursor
                        .getColumnIndex(Part.CT_START));
                String CT_TYPE = cursor.getString(cursor
                        .getColumnIndex(Part.CT_TYPE));
                String _DATA = cursor.getString(cursor
                        .getColumnIndex(Part._DATA));
                String TEXT = cursor
                        .getString(cursor.getColumnIndex(Part.TEXT));
                String reject = cursor.getString(cursor.getColumnIndex("reject"));
                MyJsonObject object = new MyJsonObject();
                object.put(Part.SEQ, SEQ);
                object.put(Part.CONTENT_TYPE, CONTENT_TYPE);
                object.put(Part.NAME, NAME);
                object.put(Part.CHARSET, CHARSET);
                object.put(Part.CONTENT_DISPOSITION, CONTENT_DISPOSITION);
                object.put(Part.FILENAME, FILENAME);
                object.put(Part.CONTENT_ID, CONTENT_ID);
                object.put(Part.CONTENT_LOCATION, CONTENT_LOCATION);
                object.put(Part.CT_START, CT_START);
                object.put(Part.CT_TYPE, CT_TYPE);
                object.put(Part._DATA, _DATA);
                object.put(Part.TEXT, TEXT);
                object.put("reject", reject);
                object.put(Part._ID, _ID);
                // 附件判断
                if (!TextUtils.isEmpty(_DATA)) {// 有附件
                    Uri syncPartidUri = Uri.withAppendedPath(
                            Uri.withAppendedPath(MMS_SMS_SYNC_URI,
                                    String.valueOf(_ID)), "sync_part");
                    Cursor partSyncCursor = query(syncPartidUri, null, null,
                            null, null);
                    if (partSyncCursor != null) {
                        MyJsonObject accessoryObject = null;
                        int isdelete = partSyncCursor.getInt(partSyncCursor
                                .getColumnIndex(SyncPart.ISDELETE));
                        int dirty = partSyncCursor.getInt(partSyncCursor
                                .getColumnIndex(SyncPart.DIRTY));
                        String syncid = partSyncCursor.getString(partSyncCursor
                                .getColumnIndex(SyncPart.SYNC_ID));
                        switch (isdelete) {
                        case 0:
                            if (dirty == 1) {
                                accessoryObject = getAccessoryObject(syncid,
                                        _ID, _DATA);
                            }
                            break;
                        case 1:
                            if (!TextUtils.isEmpty(syncid)) {
                                object.put("syncid", syncid);
                                object.put("isdelete", 1);
                            } else {
                                delete(SYNC_PART_URI, "syncid = ?",
                                        new String[] {syncid});
                            }
                            break;
                        }
                        partSyncCursor.close();
                        if (accessoryObject != null) {
                            accessoryJsonArray.put(accessoryObject);
                        }
                    }
                }
                jsonArray.put(object);
            }
            cursor.close();
        }
        if(accessoryJsonArray.length() > 0){
            MyJsonObject object = new MyJsonObject();
            object.put("accessory", accessoryJsonArray);
            rawObject[1] = object.toString();
        } else {
            rawObject[1] = null;
        }
        return jsonArray;
    }

    private MyJsonObject getAccessoryDeleteObject(String syncid) {
        MyJsonObject accessoryObject = new MyJsonObject();
        accessoryObject.put("syncid", syncid);
        accessoryObject.put("isdelete", 1);
        return accessoryObject;
    }

    private MyJsonObject getAccessoryObject(String syncid, long id, String data) {
        MyJsonObject accessoryObject = null;
        File file = new File(data);
        if (file != null && file.exists()) {
            accessoryObject = new MyJsonObject();
            accessoryObject.put("accessoryid", id);
            accessoryObject.put("syncid", syncid);
            accessoryObject.put("type", "providerFile");
            accessoryObject.put("path", data);
        }
        return accessoryObject;
    }

    private Object[] getSMSObjectFromSyncUpCursor(Cursor cursor, long id, long msgid, String syncid, long opdate, String localFlag,
            SparseArray< String> gn_sim_id_map) {
        Object[] rawObject = new Object[3];
        String address = cursor.getString(cursor
                .getColumnIndex(Sync.ADDRESS));
        String m_size = cursor.getString(cursor
                .getColumnIndex(Sync.MESSAGE_SIZE));
        String person = cursor.getString(cursor
                .getColumnIndex(Sync.PERSON));
        long date = cursor.getLong(cursor.getColumnIndex(Sync.DATE));
        String date_sent = cursor.getString(cursor
                .getColumnIndex(Sync.DATE_SENT));
        String protocol = cursor.getString(cursor
                .getColumnIndex(Sync.PROTOCOL));
        String read = cursor.getString(cursor.getColumnIndex(Sync.READ));
        String status = cursor.getString(cursor
                .getColumnIndex(Sync.STATUS));
        String type = cursor.getString(cursor.getColumnIndex(Sync.MESSAGE_TYPE));
        String reply_path_present = cursor.getString(cursor
                .getColumnIndex(Sync.REPLY_PATH_PRESENT));
        String subject = cursor.getString(cursor
                .getColumnIndex(Sync.SUBJECT));
        String body = cursor.getString(cursor
                .getColumnIndex(Sync.BODY));
        String service_center = cursor.getString(cursor
                .getColumnIndex(Sync.SERVICE_CENTER));
        String locked = cursor.getString(cursor
                .getColumnIndex(Sync.LOCKED));
        int gn_sim_id = cursor.getInt(cursor
                .getColumnIndex(Sync.SIM_ID));
        String error_code = cursor.getString(cursor
                .getColumnIndex(Sync.ERROR_CODE));
        String seen = cursor.getString(cursor.getColumnIndex(Sync.SEEN));
        String update_threads = cursor.getString(cursor
                .getColumnIndex(Sync.UPDATE_THREADS));
        String reject = cursor.getString(cursor
                .getColumnIndex(Sync.REJECT));
        String weatherInfo = cursor.getString(cursor
                .getColumnIndex(Sync.WEATHER_INFO));
        MyJsonObject object = new MyJsonObject();
        object.put("id", id);
        object.put("syncid", syncid);
        object.put("msgid", msgid);
        object.put("isdelete", 0);
        object.put("msgtype", SMS_TYPE);
        object.put("localFlag", localFlag);
        object.put("address", address);
        object.put("m_size", m_size);
        object.put("person", person);
        object.put("date", opdate);
        object.put("createTime", date);
        object.put("date_sent", date_sent);
        object.put("protocol", protocol);
        object.put("read", read);
        object.put("status", status);
        object.put("type", type);
        object.put("reply_path_present", reply_path_present);
        object.put("subject", subject);
        object.put("body", body);
        object.put("service_center", service_center);
        object.put("locked", locked);
        object.put("gn_sim_id", gn_sim_id_map.get(gn_sim_id));
        object.put("error_code", error_code);
        object.put("seen", seen);
        object.put("update_threads", update_threads);
        object.put("reject", reject);
        object.put("weather_info", weatherInfo);
        rawObject[0] = object.toString();
        rawObject[1] = null;
        if(TextUtils.isEmpty(syncid)){
            rawObject[2] = getInfo(id, 0, opdate, syncid);
        } else {
            rawObject[2] = getInfo(id, 1, opdate, syncid);
        }
        return rawObject;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String table = "";
        int count = -1;
        if (values == null) {
            values = new ContentValues();
        }
        boolean isNeedNotifySync = false;
        switch (URI_MATCHER.match(uri)) {
        case URI_SYNC_PART_SYNC_ID:
            String sync_part_id = uri.getLastPathSegment();
            count = db.update(PART_SYNC, values, "syncid = ?",
                    new String[] { sync_part_id });
            if (count <= 0) {
                Uri sync_part_id_insert = insert(uri, values);
                count = Integer.valueOf(sync_part_id_insert
                        .getLastPathSegment());
            }
            break;
        case URI_SYNC_MMS_UPDATE:
            table = TABLE_SYNC;
            selection = Sync.MSG_TYPE + "=" + MMS_TYPE + " and " + Sync.MSG_ID + " in (select _id from " + MmsProvider.TABLE_PDU
                    + " where " + selection + ")";
            values.put(Sync.DIRTY, 1);
            values.put(Sync.UPDATE_DATE, System.currentTimeMillis());
            isNeedNotifySync = true;
            break;
        case URI_SYNC_MMS_UPDATE_ID:
            table = TABLE_SYNC;
            String mmsId = uri.getPathSegments().get(1);
            selection = Sync.MSG_TYPE + "=" + MMS_TYPE + " and " + Sync.MSG_ID + " = " + mmsId;
            values.put(Sync.DIRTY, 1);
            values.put(Sync.UPDATE_DATE, System.currentTimeMillis());
            isNeedNotifySync = true;
            break;
        case URI_SYNC_SMS_UPDATE:
            table = TABLE_SYNC;
            selection = Sync.MSG_TYPE + "=" + SMS_TYPE + " and " + Sync.MSG_ID + " in (select _id from " + SmsProvider.TABLE_SMS
                    + " where " + selection + ")";
            values.put(Sync.DIRTY, 1);
            values.put(Sync.UPDATE_DATE, System.currentTimeMillis());
            isNeedNotifySync = true;
            break;
        case URI_SYNC_PART_UPDATE:
            table = PART_SYNC;
            selection = SyncPart.PART_ID + " in (select _id from " + MmsProvider.TABLE_PART
                    + " where " + selection + ")";
            values.put(SyncPart.DIRTY, 1);
            break;
        case URI_SYNC_PART_UPDATE_ID:
            table = PART_SYNC;
            String partId = uri.getPathSegments().get(1);
            selection = SyncPart.PART_ID + " = " + partId;
            values.put(SyncPart.DIRTY, 1);
            break;
        case URI_SYNC_ID:
            table = PART_SYNC;
            String syncId = uri.getPathSegments().get(1);
            selection = SyncPart.SYNC_ID + " = " + syncId;
            break;
        case URI_SYNC:
            table = TABLE_SYNC;
            break;
        case URI_PART:
            table = PART_SYNC;
            break;
        }
        count = db.update(table, values, selection, selectionArgs);
        if (isNeedNotifySync) {
            notifySync();
        }
        return count;
    }
    
    private int syncUpResultOne(SQLiteDatabase db, String id, String syncid, String accessory, long date, int result){
        int count = -1;
        switch(result){
        case 1://修改成功
            if(!TextUtils.isEmpty(id) && !TextUtils.isEmpty(id)){
                count = db.delete(TABLE_SYNC, Sync.SYNC_ID + " = " + syncid + " and " + Sync.ISDELETE + " = 1", null);
                if (count <= 0) {
                    if(!TextUtils.isEmpty(accessory)){
                        JSONArray accessoryJSONArray = null;
                        try{
                            accessoryJSONArray = new JSONArray(accessory);
                        } catch (JSONException e){
                            
                        }
                        if(accessoryJSONArray != null && accessoryJSONArray.length() > 0){
                            updatePartFromUpResult(db, accessoryJSONArray);
                        }
                    }
                    Cursor cursor = db.query(TABLE_SYNC, new String[]{Sync.UPDATE_DATE}, Sync._ID + " = " + id, null, null, null, null);
                    if(cursor != null){
                    	if(cursor.moveToFirst()){
                    		long updateDate = cursor.getLong(0);
                    		if(updateDate != date){
                                ContentValues syncResultValues = new ContentValues();
                                syncResultValues.put(Sync.SYNC_ID, syncid);
                                count = db.update(TABLE_SYNC, syncResultValues, Sync._ID + " = " + id, null);
                    		} else {
                                ContentValues syncResultValues = new ContentValues();
                                syncResultValues.put(Sync.SYNC_ID, syncid);
                                syncResultValues.put(Sync.DIRTY, 0);
                                count = db.update(TABLE_SYNC, syncResultValues, Sync._ID + " = " + id, null);
                    		}
                    	}
                    	cursor.close();
                    }
                }
            } else {
                Log.e(TAG, "URI_SYNC_UP_RESULT  wrong:id="
                        + id + ";syncid = " + syncid);
            }
            break;
        case 2://修改失败，数据在其他设备上已被修改
            ContentValues syncResultValues = new ContentValues();
            syncResultValues.put(Sync.ISDELETE, 0);
            syncResultValues.put(Sync.DIRTY, 0);
            count = db.update(TABLE_SYNC, syncResultValues, Sync._ID + " = " + id + " and " + Sync.UPDATE_DATE + "=" + date, null);
            break;
        case 3://修改失败，数据已被清除
            Cursor cursor = db.query(TABLE_SYNC, new String[]{Sync.ISDELETE}, Sync._ID + " = " + id, null, null, null, null);
            if(cursor != null){
            	if(cursor.moveToFirst()){
            		int isdelete = cursor.getInt(0);
            		if(isdelete == 1){
            			count = db.delete(TABLE_SYNC, Sync._ID + " = " + id, null);
            		} else {
                        syncResultValues = new ContentValues();
                        syncResultValues.putNull(Sync.SYNC_ID);
                        count = db.update(TABLE_SYNC, syncResultValues, Sync._ID + " = " + id, null);
            		}
            	}
            	cursor.close();
            }
            break;
        }
        return count;
    }
    
    private int updatePartFromUpResult(SQLiteDatabase db, JSONArray accessoryJSONArray){
        int count = 0;
        if(accessoryJSONArray != null){
            ContentValues values = new ContentValues();
            for(int i = 0; i < accessoryJSONArray.length(); i++){
                JSONObject accessoryObject = null;
                try{
                    accessoryObject = accessoryJSONArray.getJSONObject(i);
                } catch (JSONException e) {
                    
                }
                if(accessoryObject != null){
                    long partid = -1;
                    String syncid = null;
                    try{
                        partid = accessoryObject.getLong("accessoryid");
                        syncid = accessoryObject.getString("syncid");
                    } catch (JSONException e) {
                        
                    }
                    if(partid > 0 && !TextUtils.isEmpty(syncid)){
                        values.clear();
                        values.put(SyncPart.SYNC_ID, syncid);
                        values.put(SyncPart.DIRTY, 0);
                        if(db.update(PART_SYNC, values, SyncPart.PART_ID + " = " + partid, null) > 0) {
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }
    
    private boolean syncInitOne(SQLiteDatabase db, String syncid, String localFlag, long date, int isdelete){
        Cursor cursor = db.query(TABLE_SYNC, null, Sync.LOCAL_FLAG + "='" + localFlag + "'", null, null, null, null);
        boolean isInit = false;
        if(cursor != null){
            if(isdelete == 1){
                if(cursor.moveToFirst()){
                    long localDate = cursor.getLong(cursor.getColumnIndex(Sync.UPDATE_DATE));
                    long id = cursor.getLong(cursor.getColumnIndex(Sync._ID));
                    if(localDate > date){
                        isInit = true;
                        ContentValues values = new ContentValues();
                        values.put(Sync.SYNC_ID, syncid);
                        values.put(Sync.DIRTY, 1);
                        db.update(TABLE_SYNC, values , Sync._ID + "=" + id, null);
                    } else {
                        int msgType = cursor.getInt(cursor.getColumnIndex(Sync.MSG_TYPE));
                        long msgId = cursor.getLong(cursor.getColumnIndex(Sync.MSG_ID));
                        int count = 0;
                        switch (msgType) {
                        case MMS_TYPE:
                            db.beginTransaction();
                            try{
                                db.delete(MmsProvider.TABLE_ADDR, Addr.MSG_ID + " = " + msgId, null);
                                db.delete(MmsProvider.TABLE_PART, Part.MSG_ID + " = " + msgId, null);
                                db.delete(MmsProvider.TABLE_PDU, "_id = " + msgId, null);
                                count = db.delete(TABLE_SYNC, Sync._ID + "=" + id, null);
                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }
                            break;
                        case SMS_TYPE:
                            db.delete(SmsProvider.TABLE_SMS, "_id = " + msgId, null);
                            count = db.delete(TABLE_SYNC, Sync._ID + "=" + id, null);
                            break;
                        }
                        if(count > 0) {
                            isInit = true;
                        } else {
                            ContentValues values = new ContentValues();
                            values.put(Sync.SYNC_ID, syncid);
                            values.put(Sync.DIRTY, 1);
                            db.update(TABLE_SYNC, values , Sync._ID + "=" + id, null);
                            isInit = false;
                        }
                    }
                } else {
                    isInit = true;
                }
            } else {
                if(cursor.moveToFirst()){
                    long localDate = cursor.getLong(cursor.getColumnIndex(Sync.UPDATE_DATE));
                    long id = cursor.getLong(cursor.getColumnIndex(Sync._ID));
                    if(localDate > date){
                        isInit = true;
                        ContentValues values = new ContentValues();
                        values.put(Sync.SYNC_ID, syncid);
                        values.put(Sync.DIRTY, 1);
                        db.update(TABLE_SYNC, values , Sync._ID + "=" + id, null);
                    } else if (localDate < date) {
                        isInit = false;
                        ContentValues values = new ContentValues();
                        values.put(Sync.SYNC_ID, syncid);
                        values.put(Sync.DIRTY, 0);
                        db.update(TABLE_SYNC, values , Sync._ID + "=" + id, null);
                    } else {
                        isInit = true;
                        ContentValues values = new ContentValues();
                        values.put(Sync.SYNC_ID, syncid);
                        values.put(Sync.DIRTY, 0);
                        db.update(TABLE_SYNC, values , Sync._ID + "=" + id, null);
                    }
                }
            }
            cursor.close();
        }
        return isInit;
    }
    
    private int syncDownOne(SQLiteDatabase db, String body, String accessory, String syncId, Map<String, Integer> gn_sim_id_map){
        int count = -1;
        if (body != null) {
            MyJsonObject bodyJson = null;
            try {
                bodyJson = new MyJsonObject(body);
            } catch (JSONException e) {
                Log.e(TAG, "URI_SYNC_DOWN body wrong:" + body + ";e = "
                        + e.toString());
            }
            if (bodyJson != null) {
                Log.i(TAG,"syncDownOne bodyJson ="+bodyJson.toString());
                syncId = bodyJson.getString("syncid");
                if (!TextUtils.isEmpty(syncId)) {
                    int isdelete = bodyJson.getInt("isdelete");
                    if(isdelete == 1){
                        Cursor cursor = db.query(SYNC_VIEW, new String[]{Sync.MSG_ID, Sync.MSG_TYPE, Sync.THREAD_ID}, Sync.SYNC_ID + "=" + syncId, null, null, null, null);
                        if(cursor != null){
                            if(cursor.moveToNext()){
                                long msgId = cursor.getLong(0);
                                int msgType = cursor.getInt(1);
                                long threadId = cursor.getInt(2);
                                if(threadId > 0){
                                    switch (msgType) {
                                    case MMS_TYPE:
                                        db.beginTransaction();
                                        try{
                                            db.delete(MmsProvider.TABLE_ADDR, Addr.MSG_ID + " = " + msgId, null);
                                            db.delete(MmsProvider.TABLE_PART, Part.MSG_ID + " = " + msgId, null);
                                            count = db.delete(MmsProvider.TABLE_PDU, "_id = " + msgId, null);
                                            if(count > 0){
                                                MmsSmsDatabaseHelper.updateThread(db, threadId);
                                                count = db.delete(TABLE_SYNC, Sync.SYNC_ID + "=" + syncId, null);
                                            }
                                            db.setTransactionSuccessful();
                                        } finally {
                                            db.endTransaction();
                                        }
                                        break;
                                    case SMS_TYPE:
                                        count = db.delete(SmsProvider.TABLE_SMS, "_id = " + msgId, null);
                                        if(count > 0){
                                            MmsSmsDatabaseHelper.updateThread(db, threadId);
                                            count = db.delete(TABLE_SYNC, Sync.SYNC_ID + "=" + syncId, null);
                                        }
                                        break;
                                    }
                                } else {
                                    count = db.delete(TABLE_SYNC, Sync.SYNC_ID + "=" + syncId, null);
                                }
                            } else {
                                count = 1;
                            }
                            cursor.close();
                        }
                    } else {
                        int msgType = bodyJson.getInt("msgtype");
                        switch (msgType) {
                        case MMS_TYPE:
                            count = updateOrInsertMMSFromDown(db, syncId, bodyJson,
                                    accessory, gn_sim_id_map);
                            break;
                        case SMS_TYPE:
                            count = updateOrInsertSMSFromDown(db, syncId, bodyJson, gn_sim_id_map);
                            break;
                        }
                    }
                } else {
                    Log.e(TAG, "URI_SYNC_DOWN bodyJson wrong:"
                            + bodyJson.toString()
                            + ";there is no syncid");
                }
            } else {
                Log.i(TAG,"syncDownOne bodyJson is null");
            }
        }
        return count;
    }

    private int updateOrInsertMMSFromDown(SQLiteDatabase db, String syncid, MyJsonObject bodyJson,
            String accessory, Map<String, Integer> gn_sim_id_map) {
        int count = -1;
        Cursor cursor = query(SYNC_URI, null, Sync.SYNC_ID + " = " + syncid, null, null);
        long date = bodyJson.getLong("date");
        String localFlag = bodyJson.getString("localFlag");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                long localDate = cursor.getLong(cursor.getColumnIndex(Sync.UPDATE_DATE));
                if(localDate > date){
                    count = 1;
                } else {
                    int isDelete = cursor.getInt(cursor.getColumnIndex(Sync.ISDELETE));
                    long id = cursor.getLong(cursor.getColumnIndex(Sync._ID));
                    if(isDelete == 1){
                        long msgid = insertMms(db, bodyJson, accessory, gn_sim_id_map);
                        if (msgid > 0) {
                            ContentValues values = new ContentValues();
                            values.put(Sync.DIRTY, 0);
                            values.put(Sync.ISDELETE, 0);
                            values.put(Sync.UPDATE_DATE, date);
                            values.put(Sync.MSG_ID, msgid);
                            count = db.update(TABLE_SYNC, values, Sync._ID + "=" + id, null);
                            if (count <= 0) {
                                // 回退pud和addr和part
                                db.delete(MmsProvider.TABLE_PART, Part.MSG_ID + "=" + msgid, null);
                                db.delete(MmsProvider.TABLE_ADDR, Addr.MSG_ID + "=" + msgid, null);
                                db.delete(MmsProvider.TABLE_PDU, Mms._ID + "=" + msgid, null);
                            }
                        }
                    } else {
                        long msgid = cursor.getLong(cursor.getColumnIndex(Sync.MSG_ID));
                        count = updateMms(db, msgid, bodyJson, accessory, gn_sim_id_map);
                        if (count > 0) {
                            ContentValues values = new ContentValues();
                            values.put(Sync.DIRTY, 0);
                            values.put(Sync.ISDELETE, 0);
                            values.put(Sync.UPDATE_DATE, date);
                            count = db.update(TABLE_SYNC, values, Sync._ID + "=" + id, null);
                            if (count <= 0) {
                                // 回退pud和addr和part
                                db.delete(MmsProvider.TABLE_PART, Part.MSG_ID + "=" + msgid, null);
                                db.delete(MmsProvider.TABLE_ADDR, Addr.MSG_ID + "=" + msgid, null);
                                db.delete(MmsProvider.TABLE_PDU, Mms._ID + "=" + msgid, null);
                            }
                        }
                    }
                }
            } else {
                long msgid = insertMms(db, bodyJson, accessory, gn_sim_id_map);
                if (msgid >= 0) {
                    ContentValues values = new ContentValues();
                    values.put(Sync.DIRTY, 0);
                    values.put(Sync.ISDELETE, 0);
                    values.put(Sync.MSG_TYPE, MMS_TYPE);
                    values.put(Sync.MSG_ID, msgid);
                    values.put(Sync.SYNC_ID, syncid);
                    values.put(Sync.UPDATE_DATE, date);
                    values.put(Sync.LOCAL_FLAG, localFlag);
                    if (db.insert(TABLE_SYNC, null, values) >= 0) {
                        count = 1;
                    } else {
                        Log.e(TAG,"mms sync insert failed");
                        // 回退pud和addr和part
                        db.delete(MmsProvider.TABLE_PART, Part.MSG_ID + "=" + msgid, null);
                        db.delete(MmsProvider.TABLE_ADDR, Addr.MSG_ID + "=" + msgid, null);
                        db.delete(MmsProvider.TABLE_PDU, Mms._ID + "=" + msgid, null);
                    }
                }
            }
            cursor.close();
        }
        return count;
    }

    private int insertPartFromDown(SQLiteDatabase db, long msgid, JSONArray jsonArray,
            String accessory) {
        int count = 0;
        if(jsonArray != null && jsonArray.length() > 0){
            JSONArray accessoryArray = null;
            try{
                accessoryArray = new JSONArray(accessory);
            } catch (JSONException e) {
                
            }
            HashMap<String, MyJsonObject> accessoryMap = null;
            if(accessoryArray != null){
                accessoryMap = new HashMap<String, MyJsonObject>(accessoryArray.length());
                for(int i = 0; i < accessoryArray.length(); i++){
                    MyJsonObject object = null;
                    try {
                        object = MyJsonObject.createFromJsonObject(accessoryArray.getJSONObject(i));
                    } catch (JSONException e) {

                    }
                    if(object != null){
                        accessoryMap.put(object.getString("accessoryid"), object);
                    }
                }
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                MyJsonObject part = null;
                try {
                    part = MyJsonObject.createFromJsonObject(jsonArray
                            .getJSONObject(i));
                } catch (JSONException e) {

                }
                if (part != null) {
                    String _DATA = part.getString(Part._DATA);
                    String id = part.getString("_id");
                    if (!TextUtils.isEmpty(_DATA)) {
                        if (TextUtils.isEmpty(id)) {
                            Log.e(TAG,"part id is null");
                            break;
                        }
                        MyJsonObject accessoryObject = accessoryMap.get(id);
                        if(accessoryObject == null){
                            Log.e(TAG,"part accessoryObject is null");
                            break;
                        }
                        String temppath = accessoryObject.getString("new_path");
                        String syncid = accessoryObject.getString("syncid");
                        String data = temppath;
                        if(TextUtils.isEmpty(temppath)){
                            Log.e(TAG,"part temppath is null");
                            break;
                        }
                        File tempFile = new File(temppath);
                        if (!tempFile.exists()) {
                            // 附件下载出问题了
                            Log.e(TAG," part file is wrong");
                            break;
                        }
                        long partid = insertPart(db, msgid, part, data);
                        if (partid <= 0) {
                            Log.e(TAG," insert data part is failed");
                            break;
                        } else {
                            if( insertPartSync(db, partid, syncid) <= 0) {
                                Log.e(TAG," insert part sync is failed");
                                break;
                            }
                        }
                    } else {
                        if (insertPart(db, msgid, part, _DATA) <= 0) {
                            Log.e(TAG," insert part is failed");
                            break;
                        }
                    }
                } else {
                    Log.e(TAG,"part is null");
                    break;
                }
                count++;
            }
            if (count < jsonArray.length()) {
                db.delete(MmsProvider.TABLE_PART, Part.MSG_ID + "=" + msgid, null);
            }
        }
        return count;
    }
    
    private long insertPartSync(SQLiteDatabase db, long id, String syncid){
        ContentValues values = new ContentValues();
        values.put(SyncPart.PART_ID, id);
        values.put(SyncPart.DIRTY, 0);
        values.put(SyncPart.SYNC_ID, syncid);
        return db.insert(PART_SYNC, null, values);
    }

    private long insertPart(SQLiteDatabase db, long pudId, MyJsonObject part, String data) {
        ContentValues values = new ContentValues();
        values.put(Part.MSG_ID, pudId);
        values.put(Part._DATA, data);
        values.put(Part.SEQ, part.getString(Part.SEQ));
        values.put(Part.CONTENT_TYPE, part.getString(Part.CONTENT_TYPE));
        values.put(Part.NAME, part.getString(Part.NAME));
        values.put(Part.CHARSET, part.getString(Part.CHARSET));
        values.put(Part.CONTENT_DISPOSITION,
                part.getString(Part.CONTENT_DISPOSITION));
        values.put(Part.FILENAME, part.getString(Part.FILENAME));
        values.put(Part.CONTENT_ID, part.getString(Part.CONTENT_ID));
        values.put(Part.CONTENT_LOCATION, part.getString(Part.CONTENT_LOCATION));
        values.put(Part.CT_START, part.getString(Part.CT_START));
        values.put(Part.CT_TYPE, part.getString(Part.CT_TYPE));
        values.put(Part.TEXT, part.getString(Part.TEXT));
        values.put("reject", part.getString("reject"));
        return db.insert(MmsProvider.TABLE_PART, null, values);
    }

    private int updatePart(SQLiteDatabase db, String syncid, int partid, MyJsonObject part, String data) {
        ContentValues values = new ContentValues();
        values.put(Part._DATA, data);
        values.put(Part.SEQ, part.getString(Part.SEQ));
        values.put(Part.CONTENT_TYPE, part.getString(Part.CONTENT_TYPE));
        values.put(Part.NAME, part.getString(Part.NAME));
        values.put(Part.CHARSET, part.getString(Part.CHARSET));
        values.put(Part.CONTENT_DISPOSITION,
                part.getString(Part.CONTENT_DISPOSITION));
        values.put(Part.FILENAME, part.getString(Part.FILENAME));
        values.put(Part.CONTENT_ID, part.getString(Part.CONTENT_ID));
        values.put(Part.CONTENT_LOCATION, part.getString(Part.CONTENT_LOCATION));
        values.put(Part.CT_START, part.getString(Part.CT_START));
        values.put(Part.CT_TYPE, part.getString(Part.CT_TYPE));
        values.put(Part.TEXT, part.getString(Part.TEXT));
        values.put("reject", part.getString("reject"));
        return db.update(MmsProvider.TABLE_PART, values, Part._ID + "=" + partid, null);
    }

    private int updateOrInsertSMSFromDown(SQLiteDatabase db, String syncid, MyJsonObject bodyJson, Map<String, Integer> gn_sim_id_map) {
        int count = -1;
        Cursor cursor = query(SYNC_URI, null, Sync.SYNC_ID + " = " + syncid, null, null);
        long date = bodyJson.getLong("date");
        String localFlag = bodyJson.getString("localFlag");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                long localDate = cursor.getLong(cursor.getColumnIndex(Sync.UPDATE_DATE));
                if(localDate > date){
                    count = 1;
                } else {
                    long id = cursor.getLong(cursor.getColumnIndex(Sync._ID));
                    int isDelete = cursor.getInt(cursor.getColumnIndex(Sync.ISDELETE));
                    if(isDelete == 1){
                        long msgid = insertSms(db, bodyJson, gn_sim_id_map);
                        if (msgid >= 0) {
                            ContentValues values = new ContentValues();
                            values.put(Sync.DIRTY, 0);
                            values.put(Sync.ISDELETE, 0);
                            values.put(Sync.UPDATE_DATE, date);
                            values.put(Sync.MSG_ID, msgid);
                            count = db.update(TABLE_SYNC, values, Sync._ID + "=" + id, null);
                            if (count <= 0) {
                                // 回退sms
                                db.delete(SmsProvider.TABLE_SMS, "_id=" + msgid, null);
                            }
                        }
                    } else {
                        long msgid = cursor.getLong(cursor.getColumnIndex(Sync.MSG_ID));
                        count = updateSms(db, msgid, bodyJson, gn_sim_id_map);
                        if (count > 0) {
                            ContentValues values = new ContentValues();
                            values.put(Sync.DIRTY, 0);
                            values.put(Sync.ISDELETE, 0);
                            values.put(Sync.UPDATE_DATE, date);
                            count = db.update(TABLE_SYNC, values, Sync._ID + "=" + id, null);
                            if (count <= 0) {
                                // 回退sms
                                db.delete(SmsProvider.TABLE_SMS, "_id=" + msgid, null);
                            }
                        }
                    }
                }
            } else {
                long msgid = insertSms(db, bodyJson, gn_sim_id_map);
                if (msgid >= 0) {
                    ContentValues values = new ContentValues();
                    values.put(Sync.DIRTY, 0);
                    values.put(Sync.ISDELETE, 0);
                    values.put(Sync.MSG_TYPE, SMS_TYPE);
                    values.put(Sync.MSG_ID, msgid);
                    values.put(Sync.SYNC_ID, syncid);
                    values.put(Sync.UPDATE_DATE, date);
                    values.put(Sync.LOCAL_FLAG, localFlag);
                    if (db.insert(TABLE_SYNC, null, values) >= 0) {
                        count = 1;
                    } else {
                        // 回退sms
                        db.delete(SmsProvider.TABLE_SMS, "_id=" + msgid, null);
                    }
                }
            }
            cursor.close();
        }
        return count;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        Log.w(TAG, "openFile uri = " + uri + ";mode = " + mode);
        switch (URI_MATCHER.match(uri)) {
        case URI_SYNC_ACCESSORY:
            File fileDir = getContext().getDir("parts", Context.MODE_PRIVATE);
            if (!fileDir.exists() && fileDir.mkdir()) {
                return null;
            }
            String path = uri.getQueryParameter("path");
            File pathFile = new File(path);
            if ("w".equals(mode)) {
                if (pathFile.exists()) {
                    tempUsedFile.add(path);
                    Editor et = mSp.edit();
                    et.putStringSet(TEMP_USED_FILE, tempUsedFile);
                    et.commit();
                    return ParcelFileDescriptor.open(pathFile,
                            ParcelFileDescriptor.MODE_WRITE_ONLY|ParcelFileDescriptor.MODE_APPEND);
                }
            } else if ("r".equals(mode)) {
                if (pathFile.exists()) {
                    return ParcelFileDescriptor.open(pathFile,
                            ParcelFileDescriptor.MODE_READ_ONLY);
                }
            }
            break;
        }
        return null;
    }

    private class AddrObject {
        public int msgId;
        public String contactId;
        public String address;
        public String type;
        public String charset;
        public String reject;

        public AddrObject(JSONObject addrJsonObject) {
            try {
                msgId = addrJsonObject.getInt("msg_id");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                contactId = addrJsonObject.getString("contact_id");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                address = addrJsonObject.getString("address");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                type = addrJsonObject.getString("type");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                charset = addrJsonObject.getString("charset");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                reject = addrJsonObject.getString("reject");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public JSONObject toJSONObject() {
            MyJsonObject object = new MyJsonObject();
            object.put("msgId", msgId);
            object.put("contact_id", contactId);
            object.put("address", address);
            object.put("type", type);
            object.put("charset", charset);
            object.put("reject", reject);
            return object;
        }
    }

    private class PartObject {
    }

    private static class MyJsonObject extends JSONObject {
        public static MyJsonObject createFromJsonObject(JSONObject object) {
            MyJsonObject newObject = null;
            if (object != null) {
                JSONArray names = object.names();
                int count = names.length();
                String[] newNames = new String[count];
                for (int i = 0; i < count; i++) {
                    try {
                        newNames[i] = names.getString(i);
                    } catch (JSONException e) {
                        Log.w(TAG, "object =  " + object.toString()
                                + " can't get String. i = " + i);
                    }
                }
                try {
                    newObject = new MyJsonObject(object, newNames);
                } catch (JSONException e) {
                    Log.w(TAG, "object =  " + object.toString()
                            + " can't be changed to MyJsonObject.");
                }
            }
            return newObject;
        }

        public MyJsonObject() {
            super();
        }

        public MyJsonObject(String jsonString) throws JSONException {
            super(jsonString);
        }

        public MyJsonObject(JSONObject object, String[] names)
                throws JSONException {
            super(object, names);
        }

        public MyJsonObject put(String name, int value) {
            try {
                super.put(name, value);
            } catch (JSONException e) {
                Log.w(TAG, "MyJsonObject put name = " + name + ";value = "
                        + value + ";e = " + e.toString());
            }
            return this;
        }

        public MyJsonObject put(String name, String value) {
            try {
                super.put(name, value);
            } catch (JSONException e) {
                Log.w(TAG, "MyJsonObject put name = " + name + ";value = "
                        + value + ";e = " + e.toString());
            }
            return this;
        }

        public MyJsonObject put(String name, boolean value) {
            try {
                super.put(name, value);
            } catch (JSONException e) {
                Log.w(TAG, "MyJsonObject put name = " + name + ";value = "
                        + value + ";e = " + e.toString());
            }
            return this;
        }

        public MyJsonObject put(String name, long value) {
            try {
                super.put(name, value);
            } catch (JSONException e) {
                Log.w(TAG, "MyJsonObject put name = " + name + ";value = "
                        + value + ";e = " + e.toString());
            }
            return this;
        }

        public MyJsonObject put(String name, Object value) {
            try {
                super.put(name, value);
            } catch (JSONException e) {
                Log.w(TAG, "MyJsonObject put name = " + name + ";value = "
                        + value + ";e = " + e.toString());
            }
            return this;
        }

        public Object get(String name) {
            Object object = null;
            try {
                object = super.get(name);
            } catch (JSONException e) {
                Log.w(TAG,
                        "MyJsonObject get name = " + name + ";e = "
                                + e.toString());
            }
            return object;
        }

        public String getString(String name) {
            String object = null;
            try {
                object = super.getString(name);
            } catch (JSONException e) {
                Log.w(TAG,
                        "MyJsonObject get name = " + name + ";e = "
                                + e.toString());
            }
            return object;
        }

        public long getLong(String name) {
            long object = -1;
            try {
                object = super.getLong(name);
            } catch (JSONException e) {
                Log.w(TAG,
                        "MyJsonObject get name = " + name + ";e = "
                                + e.toString());
            }
            return object;
        }

        public int getInt(String name) {
            int object = -1;
            try {
                object = super.getInt(name);
            } catch (JSONException e) {
                Log.w(TAG,
                        "MyJsonObject get name = " + name + ";e = "
                                + e.toString());
            }
            return object;
        }
    }

    private void notifySync() {
        if (mHandler.hasMessages(SYNC_NOTIFY)) {
            mHandler.removeMessages(SYNC_NOTIFY);
        }
        mHandler.sendEmptyMessageDelayed(SYNC_NOTIFY, 300000);
    }

}
