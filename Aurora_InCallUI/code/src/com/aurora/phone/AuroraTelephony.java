package com.android.incallui;

import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.BaseColumns;
import android.provider.Telephony.BaseMmsColumns;
import android.provider.Telephony.TextBasedSmsColumns;

import android.provider.Telephony;
import android.telephony.SmsMessage;
import java.util.ArrayList;
import java.util.List;
import android.text.TextUtils;
import android.content.ContentUris;
import android.database.DatabaseUtils;
import android.telephony.TelephonyManager;
import android.content.res.Resources;
import android.util.Log;

public class AuroraTelephony {
    public static String GN_SIM_ID = "sim_id";//Telephony.Sms.SUB_ID;

    public static class CbSms implements BaseColumns, TextBasedCbSmsColumns {

        public static  Uri CONTENT_URI = Uri.parse("content://cb/messages");
        public static  Uri ADDRESS_URI = Uri.parse("content://cb/addresses");
        public static  String DEFAULT_SORT_ORDER = "date DESC";
        
        public static class CanonicalAddressesColumns {

            public static  String ADDRESS = "address";

        }
        
        public static class Conversations {

            public static  Uri CONTENT_URI = Uri.parse("content://cb/threads");;
            public static  String MESSAGE_COUNT = "msg_count";
            public static  String SNIPPET = "snippet";
            public static  String ADDRESS_ID = "address_id";

        }

        public static class CbChannel {
            public static  Uri CONTENT_URI = Uri.parse("content://cb/channel");

            public static  String NUMBER = "number";

            public static  String NAME = "name";
			
			public static  String ENABLE = "enable";
        }
        
        public static  class Intents {
            public static  String CB_SMS_RECEIVED_ACTION = "android.provider.Telephony.CB_SMS_RECEIVED";
 

        }

    }
    
    /**
     * Base columns for tables that contain text based CbSMSs.
     */
    public interface TextBasedCbSmsColumns {

        /**
         * The SIM ID which indicated which SIM the CbSMS comes from
         * Reference to Telephony.SIMx
         * <P>Type: INTEGER</P>
         */
        public static  String SIM_ID = GN_SIM_ID;//"sim_id";

        /**
         * The channel ID of the message
         * which is the message identifier defined in the Spec. 3GPP TS 23.041
         * <P>Type: INTEGER</P>
         */
        public static  String CHANNEL_ID = "channel_id";

        /**
         * The date the message was sent
         * <P>Type: INTEGER (long)</P>
         */
        public static  String DATE = "date";

        /**
         * Has the message been read
         * <P>Type: INTEGER (boolean)</P>
         */
        public static  String READ = "read";

        /**
         * The body of the message
         * <P>Type: TEXT</P>
         */
        public static  String BODY = "body";
        
        /**
         * The thread id of the message
         * <P>Type: INTEGER</P>
         */
        public static  String THREAD_ID = "thread_id";

        /**
         * Indicates whether this message has been seen by the user. The "seen" flag will be
         * used to figure out whether we need to throw up a statusbar notification or not.
         */
        public static  String SEEN = "seen";

        /**
         * Has the message been locked?
         * <P>Type: INTEGER (boolean)</P>
         */
        public static  String LOCKED = "locked";
    }

    public static class Sms implements TextBasedSmsColumns{

        public static  String _ID = Telephony.Sms._ID;
        public static  String THREAD_ID = Telephony.Sms.THREAD_ID;
        public static  String ADDRESS = Telephony.Sms.ADDRESS;
        public static  String BODY = Telephony.Sms.BODY;
        public static  String STATUS = Telephony.Sms.STATUS;
        public static  Uri CONTENT_URI = Telephony.Sms.CONTENT_URI;
        public static  int STATUS_PENDING = Telephony.Sms.STATUS_PENDING;
        public static  String TYPE = Telephony.Sms.TYPE;
        public static  int MESSAGE_TYPE_SENT = Telephony.Sms.MESSAGE_TYPE_SENT;
        public static  int MESSAGE_TYPE_OUTBOX = Telephony.Sms.MESSAGE_TYPE_OUTBOX;
        public static  int MESSAGE_TYPE_QUEUED = Telephony.Sms.MESSAGE_TYPE_QUEUED;
        public static  int MESSAGE_TYPE_FAILED = Telephony.Sms.MESSAGE_TYPE_FAILED;
        public static  int STATUS_FAILED = Telephony.Sms.STATUS_FAILED;
        public static  String PROTOCOL = Telephony.Sms.PROTOCOL;
        public static  String SIM_ID = GN_SIM_ID;//"sim_id";//android.provider.Telephony.Sms;
        public static  String ERROR_CODE = Telephony.Sms.ERROR_CODE;
        public static  String DATE = Telephony.Sms.DATE;
        public static  String READ = Telephony.Sms.READ;
        public static  String LOCKED = Telephony.Sms.LOCKED;
        public static  String SERVICE_CENTER = Telephony.Sms.SERVICE_CENTER;
        public static  String STAR = "star";//android.provider.Telephony.Sms.STAR;
        public static  String DATE_SENT = "date_sent";//android.provider.Telephony.Sms.SEND_DATE;
        
        /**
         * Indicates whether this message has been seen by the user. The "seen" flag will be
         * used to figure out whether we need to throw up a statusbar notification or not.
         */
        public static  String SEEN = "seen";
        public static boolean moveMessageToFolder(Context context, Uri uri,
                int messageTypeSent, int error) {
            return Telephony.Sms.moveMessageToFolder(context, uri, messageTypeSent, error);
        }
        
        public static class Inbox {

            public static  String ADDRESS = Telephony.Sms.Inbox.ADDRESS;
            public static  String DATE = Telephony.Sms.Inbox.DATE;
            public static  String PROTOCOL = Telephony.Sms.Inbox.PROTOCOL;
            public static  String READ = Telephony.Sms.Inbox.READ;
            public static  String SIM_ID = GN_SIM_ID;//"sim_id";//android.provider.Telephony.Sms.Inbox.SIM_ID;
            public static  String SEEN = Telephony.Sms.Inbox.SEEN;
            public static  String SUBJECT = Telephony.Sms.Inbox.SUBJECT;
            public static  String REPLY_PATH_PRESENT = Telephony.Sms.Inbox.REPLY_PATH_PRESENT;
            public static  String SERVICE_CENTER = Telephony.Sms.Inbox.SERVICE_CENTER;
            public static  String BODY = Telephony.Sms.Inbox.BODY;
            public static  Uri CONTENT_URI = Telephony.Sms.Inbox.CONTENT_URI;
            public static Uri addMessage(ContentResolver mContentResolver, String address2,
                    String body2, String subject, Long date2, boolean b, int mSimId) {
                // TODO Auto-generated method stub
                return Telephony.Sms.Inbox.addMessage(mContentResolver, address2, body2, subject, date2, b);

            }
            public static Uri addMessage(ContentResolver mContentResolver, String address2,
                    String body2, String subject, Long date2, boolean b) {
                return Telephony.Sms.Inbox.addMessage(mContentResolver, address2, body2, subject, date2, b);
            }
            
            public static  String THREAD_ID = "thread_id";
            
            public static Uri addMessage(ContentResolver mContentResolver,
                    String address2, String body2, String subject2,
                    String serviceCenter, Long date2, boolean b) {
                return addMessage(mContentResolver,address2,body2,subject2,serviceCenter,date2,b,0);//TODO by guoyangxu
            }
			//Gionee guoyx 20130225 modified for CR00772795 begin
            public static Uri addMessage(ContentResolver Content,
                    String address, String body, String subject,
                    String serviceCenter, Long date, boolean read, int simId) {
                return Telephony.Sms.Inbox.addMessage(Content,address, 
                        body, subject, date, read);
            }
			//Gionee guoyx 20130225 modified for CR00772795 end

        }
        public static class Sent {

            public static Uri addMessage(ContentResolver mContentResolver, String address,
                    String body, String subject, Long date, int mSimId) {
                return Telephony.Sms.Sent.addMessage(mContentResolver, address, body, subject, date);
            }

            public static Uri addMessage(ContentResolver mContentResolver, String address,
                    String body, String subject, Long date) {
                return Telephony.Sms.Sent.addMessage(mContentResolver, address, body, subject, date);
            }

            public static Uri addMessage(ContentResolver mContentResolver,
                    String address, String body, String subject,
                    String serviceCenter, Long date) {
                return null;// TODO by guoyangxu
                
            }

            //Gionee guoyx 20130225 modified for CR00772795 begin
            public static Uri addMessage(ContentResolver Content,
                    String address, String body, String subject,
                    String serviceCenter, Long date, int simId) {
                int sub = simId - 1;
                return Telephony.Sms.Sent.addMessage(Content,address, 
                        body, subject, date);
            }
			//Gionee guoyx 20130225 modified for CR00772795 end

        }

        public static class Intents {

            //TODO to zengxh: you need to modify for MTK platform.
            public static SmsMessage[] getMessagesFromIntent(Intent intent) {
                // TODO Auto-generated method stub
                android.telephony.SmsMessage msg[] = Telephony.Sms.Intents.getMessagesFromIntent(intent);
//                GnSmsMessage retMsg[] = new GnSmsMessage[msg.length];
//                for (int i = 0; i < msg.length; i++) {
////                     Gionee jialf 20120105 added for CR00494057 start
//                    retMsg[i] = new GnSmsMessage();
////                     Gionee jialf 20120105 added for CR00494057 end
//                    retMsg[i].setTelephonyMsg(msg[i]);
//                }

                return msg;
            }
            public static  String SMS_CB_RECEIVED_ACTION =
                    "android.provider.Telephony.SMS_CB_RECEIVED";
            
            public static  String SMS_EMERGENCY_CB_RECEIVED_ACTION =
                    "android.provider.Telephony.SMS_EMERGENCY_CB_RECEIVED";
        }

        public static boolean isOutgoingFolder(int mBoxId) {
            // TODO Auto-generated method stub
            return Telephony.Sms.isOutgoingFolder(mBoxId);
        }
        
        public static  Cursor query(ContentResolver cr, String[] projection,
                String where, String orderBy) {
             //Gionee guoyx 20130222 modified for CR00772186 begin
            return Telephony.Sms.query(cr, projection, where, orderBy);
			//Gionee guoyx 20130222 modified for CR00772186 end
        }

    }

    public static class Mms implements BaseMmsColumns {

        public static  String THREAD_ID = Telephony.Mms.THREAD_ID;
        public static  String READ = Telephony.Mms.READ;
        public static  String SIM_ID = GN_SIM_ID;//"sim_id";//Telephony.Mms.SIM_ID;
        public static  String SEND_DATE = "send_date";//Telephony.Mms.SEND_DATE;
        public static  String MESSAGE_ID = Telephony.Mms.MESSAGE_ID;
        public static  Uri CONTENT_URI = Telephony.Mms.CONTENT_URI;
        public static  String CONTENT_LOCATION = Telephony.Mms.CONTENT_LOCATION;
        public static  String MESSAGE_TYPE = Telephony.Mms.MESSAGE_TYPE;
        public static  String LOCKED = Telephony.Mms.LOCKED;
        public static  String MESSAGE_SIZE = Telephony.Mms.MESSAGE_SIZE;
        public static  String DATE = Telephony.Mms.DATE;
        public static  String RESPONSE_STATUS =Telephony.Mms.RESPONSE_STATUS;
        public static  String SUBJECT = Telephony.Mms.SUBJECT;
        public static  String SUBJECT_CHARSET = Telephony.Mms.SUBJECT_CHARSET;
        public static  String MESSAGE_BOX = Telephony.Mms.MESSAGE_BOX;
        public static  String DELIVERY_REPORT = Telephony.Mms.DELIVERY_REPORT;
        public static  String READ_REPORT = Telephony.Mms.READ_REPORT;
        public static  String STAR = "star";//android.provider.Telephony.Mms.THREAD_ID;
        public static final int MESSAGE_BOX_INBOX = Telephony.Mms.MESSAGE_BOX_INBOX;
        public static final int MESSAGE_BOX_DRAFTS = Telephony.Mms.MESSAGE_BOX_DRAFTS;
        public static final int MESSAGE_BOX_OUTBOX = Telephony.Mms.MESSAGE_BOX_OUTBOX;
        public static String _ID = "_id";
        public static String DATE_SENT = "date_sent";
        public static String SERVICE_CENTER = "service_center";
        public static boolean isPhoneNumber(String mAddress) {
            // TODO Auto-generated method stub
            return Telephony.Mms.isPhoneNumber(mAddress);
        }
        public static boolean isEmailAddress(String mAddress) {
            // TODO Auto-generated method stub
            return Telephony.Mms.isEmailAddress(mAddress);
        }

        public static class Inbox {

            public static Uri CONTENT_URI = Telephony.Mms.Inbox.CONTENT_URI;

        }

        public static class Part {

            public static String _DATA = Telephony.Mms.Part._DATA;

        }
        
        public static class ScrapSpace {
            /**
             * The content:// style URL for this table
             */
            public static Uri CONTENT_URI = Uri.parse("content://mms/scrapSpace");

            /**
             * This is the scrap file we use to store the media attachment when the user
             * chooses to capture a photo to be attached . We pass {#link@Uri} to the Camera app,
             * which streams the captured image to the uri. Internally we write the media content
             * to this file. It's named '.temp.jpg' so Gallery won't pick it up.
             */
            public static String SCRAP_FILE_PATH = "/sdcard/mms/scrapSpace/.temp.jpg";
        }
        
        public static class Draft {
            public static Uri
                    CONTENT_URI = Uri.parse("content://mms/drafts");
            
            public static String DEFAULT_SORT_ORDER = "date DESC";
        }
        
        public static class Outbox {
            /**
             * The content:// style URL for this table
             */
            public static Uri
                    CONTENT_URI = Uri.parse("content://mms/outbox");

            /**
             * The default sort order for this table
             */
            public static String DEFAULT_SORT_ORDER = "date DESC";
        }

    }
    public static class MmsSms {
        /**
         * The column to distinguish SMS &amp; MMS messages in query results.
         */
        public static String TYPE_DISCRIMINATOR_COLUMN =
                "transport_type";
        public static class PendingMessages {

            public static String SIM_ID = "pending_sim_id";//android.provider.Telephony.MmsSms.PendingMessages.SIM_ID;
            public static String _ID = Telephony.MmsSms.PendingMessages._ID;
            public static Uri CONTENT_URI = Telephony.MmsSms.PendingMessages.CONTENT_URI;
            public static String MSG_TYPE = Telephony.MmsSms.PendingMessages.MSG_TYPE;
            public static String MSG_ID = Telephony.MmsSms.PendingMessages.MSG_ID;
            public static String ERROR_TYPE = Telephony.MmsSms.PendingMessages.ERROR_TYPE;
            public static String RETRY_INDEX = Telephony.MmsSms.PendingMessages.RETRY_INDEX;
            public static String PROTO_TYPE = Telephony.MmsSms.PendingMessages.PROTO_TYPE;
            public static String ERROR_CODE = Telephony.MmsSms.PendingMessages.ERROR_CODE;
            public static String DUE_TIME = Telephony.MmsSms.PendingMessages.DUE_TIME;
            public static String LAST_TRY = Telephony.MmsSms.PendingMessages.LAST_TRY;

        }

        public static class WordsTable {

            public static String ID = Telephony.MmsSms.WordsTable.ID;
            public static  String INDEXED_TEXT = Telephony.MmsSms.WordsTable.INDEXED_TEXT;
            public static  String SOURCE_ROW_ID = Telephony.MmsSms.WordsTable.SOURCE_ROW_ID;
            public static  String TABLE_ID = Telephony.MmsSms.WordsTable.TABLE_ID;

        }

        public static  Uri CONTENT_URI_QUICKTEXT = Uri.parse("content://mms-sms/quicktext");
    }
    public static class Threads{
        public static String _ID = Telephony.Threads._ID;
        public static String MESSAGE_COUNT = Telephony.Threads.MESSAGE_COUNT;
        public static Uri CONTENT_URI = Telephony.Threads.CONTENT_URI;
        public static final int WAPPUSH_THREAD = 2;//android.provider.Telephony.Threads.WAPPUSH_THREAD;
        public static final int CELL_BROADCAST_THREAD = 3;//android.provider.Telephony.Threads.CELL_BROADCAST_THREAD;
        public static Uri OBSOLETE_THREADS_URI = Telephony.Threads.OBSOLETE_THREADS_URI;
        public static String SNIPPET = Telephony.Threads.SNIPPET;
        public static String DATE = Telephony.Threads.DATE;
        public static String RECIPIENT_IDS = Telephony.Threads.RECIPIENT_IDS;
        public static String SNIPPET_CHARSET = Telephony.Threads.SNIPPET_CHARSET;
        public static String READ = Telephony.Threads.READ;
        public static String ERROR = Telephony.Threads.ERROR;
        public static String HAS_ATTACHMENT = Telephony.Threads.HAS_ATTACHMENT;
        public static String TYPE = Telephony.Threads.TYPE;
        public static String SIM_ID = GN_SIM_ID;//"sim_id";//android.provider.Telephony.Threads.SIM_ID;
        public static int BROADCAST_THREAD = 1;
        public static String READCOUNT = "readcount";
        public static String STATUS = "status";
        public static String ENCRYPTION = "encryption";
        public static long getOrCreateThreadId(Context context, HashSet<String> recipients) {
            // TODO Auto-generated method stub
            return Telephony.Threads.getOrCreateThreadId(context, recipients);
        }
        
        public static long getOrCreateThreadId(Context context, String recipients) {
            // TODO Auto-generated method stub
            return Telephony.Threads.getOrCreateThreadId(context, recipients);
        }
        //Gionee guoyx 20130312 add for CR00779734 begin
        public static final int IP_MESSAGE_GUIDE_THREAD = 10;
        //Gionee guoyx 20130312 add for CR00779734 end
    }

    public static class Carriers {

        public static String TYPE = Telephony.Carriers.TYPE;
        public static String MMSC = Telephony.Carriers.MMSC;
        public static String MMSPROXY = Telephony.Carriers.MMSPROXY;
        public static String MMSPORT = Telephony.Carriers.MMSPORT;
        public static String APN = Telephony.Carriers.APN;
        public static Uri CONTENT_URI = Telephony.Carriers.CONTENT_URI;
        public static String NUMERIC = Telephony.Carriers.NUMERIC;
        public static String MCC = Telephony.Carriers.MCC;
        public static String MNC = Telephony.Carriers.MNC;
        public static String NAME = Telephony.Carriers.NAME;
        public static String USER = Telephony.Carriers.USER;
        public static String SERVER = Telephony.Carriers.SERVER;
        public static String PASSWORD = Telephony.Carriers.PASSWORD;
        public static String PORT = Telephony.Carriers.PORT;
        public static String PROXY = Telephony.Carriers.PROXY;
        public static String AUTH_TYPE = Telephony.Carriers.AUTH_TYPE;
        public static String ROAMING_PROTOCOL = "roaming_protocol";// android.provider.Telephony.Carriers.ROAMING_PROTOCOL;
        public static Uri CONTENT_URI_DM = Uri.parse("content://telephony/carriers_dm");// android.provider.Telephony.Carriers.CONTENT_URI_DM;
        public static String _ID = Telephony.Carriers._ID;
        public static String CURRENT = Telephony.Carriers.CURRENT;
        
        public static String PROTOCOL = "protocol";
        public static String BEARER = "bearer";
        public static String CARRIER_ENABLED = "carrier_enabled";
        public static String OMACPID = "omacpid";
        public static String NAPID = "napid";
        public static String PROXYID = "proxyid";
        public static String SOURCE_TYPE = "sourcetype";
        public static String CSD_NUM = "csdnum";
        
        public static String SPN = "spn";
        public static String IMSI = "imsi";
        public static String PNN = "pnn";
 
        public static class SIM1Carriers {
            //Gionee guoyx 20130311 modified for CR00778966 begin
            public static Uri CONTENT_URI = Telephony.Carriers.CONTENT_URI;
//                Uri.parse("content://telephony/carriers_sim1"); 
            //Gionee guoyx 20130311 modified for CR00778966 end
        }
        
        public static class SIM2Carriers {
            //Gionee guoyx 20130311 modified for CR00778966 begin
            public static Uri CONTENT_URI = Telephony.Carriers.CONTENT_URI;
//                Uri.parse("content://telephony/carriers_sim2");
            //Gionee guoyx 20130311 modified for CR00778966 end
        }

        
        public static class GeminiCarriers {
            //Gionee guoyx 20130311 modified for CR00778966 begin
            public static Uri CONTENT_URI = Telephony.Carriers.CONTENT_URI;
            //Uri.parse("content://telephony/carriers_gemini");
            //Gionee guoyx 20130311 modified for CR00778966 begin
            public static Uri CONTENT_URI_DM = Uri.parse("content://telephony/carriers_dm_gemini"); 

        }

    }

    public static class WapPush {

        public static String DEFAULT_SORT_ORDER = "date ASC";
        public static Uri CONTENT_URI = Uri.parse("content://wappush");
        public static Uri CONTENT_URI_SI = Uri.parse("content://wappush/si");
        public static Uri CONTENT_URI_SL = Uri.parse("content://wappush/sl");
        public static Uri CONTENT_URI_THREAD = Uri.parse("content://wappush/thread_id");

        public static String THREAD_ID = "thread_id";
        public static String ADDR = "address";
        public static String SERVICE_ADDR = "service_center";
        public static String READ = "read";
        public static String DATE = "date";
        public static String TYPE = "type";
        public static String SIID = "siid";
        public static String URL = "url";
        public static String CREATE = "created";
        public static String EXPIRATION = "expiration";
        public static String ACTION = "action";
        public static String TEXT = "text";
        public static String SIM_ID = GN_SIM_ID;//"sim_id";
        public static String LOCKED = "locked";
        public static String ERROR = "error";
        public static int TYPE_SI = 0;
        public static int TYPE_SL = 1;
        public static String _ID= "_id";
        public static String SEEN = "seen";
        
        public static int STATUS_SEEN = 1;
        public static int STATUS_UNSEEN = 0;
        
        public static int STATUS_READ = 1;
        public static int STATUS_UNREAD = 0;
        public static int STATUS_LOCKED = 1;
        public static int STATUS_UNLOCKED = 0;

        // Gionee Linfeng Jia 2011-06-12 added for CR00272690 start
        public static int STATUS_STAR = 1;
        public static int STATUS_UNSTAR = 0;
        
    }
	
    public static class GprsInfo {

        public static Uri CONTENT_URI = Uri.parse("content://telephony/gprsinfo");
        public static String SIM_ID = GN_SIM_ID;//"sim_id";
        public static String GPRS_IN = "gprs_in";
        public static String GPRS_OUT = "gprs_out";

        public static String _ID = "_ID";
    }

  //MTK-START [mtk04070][111121][ALPS00093395]MTK added
    public static class SimInfo implements BaseColumns{
        public static Uri CONTENT_URI = 
            Uri.parse("content://telephony/siminfo");
        
        public static String DEFAULT_SORT_ORDER = "name ASC";
        public static String ICC_ID = "icc_id";
        public static String DISPLAY_NAME = "display_name";
        public static int DEFAULT_NAME_MIN_INDEX = 01;
        public static int DEFAULT_NAME_MAX_INDEX= 99;
//        public static int DEFAULT_NAME_RES = com.mediatek.internal.R.string.new_sim;
        public static int DEFAULT_NAME_RES = -1;
        public static String NUMBER = "number";
        public static String DISPLAY_NUMBER_FORMAT = "display_number_format";
        public static final int DISPLAY_NUMBER_NONE = 0;
        public static final int DISPLAY_NUMBER_FIRST = 1;
        public static final int DISPLAY_NUMBER_LAST = 2;
        public static final int DISLPAY_NUMBER_DEFAULT = DISPLAY_NUMBER_NONE;

        public static String COLOR = "color";
        public static int COLOR_1 = 0;
        public static int COLOR_2 = 1;
        public static int COLOR_3 = 2;
        public static int COLOR_4 = 3;
        public static int COLOR_5 = 4;
        public static int COLOR_6 = 5;
        public static int COLOR_7 = 6;
        public static int COLOR_8 = 7;
        public static int COLOR_DEFAULT = COLOR_1;

        public static String DATA_ROAMING = "data_roaming";
        public static int DATA_ROAMING_ENABLE = 1;
        public static int DATA_ROAMING_DISABLE = 0;
        public static int DATA_ROAMING_DEFAULT = DATA_ROAMING_DISABLE;

        public static String SLOT = "slot";
        public static int SLOT_NONE = -1;
        
        public static int ERROR_GENERAL = -1;
        public static int ERROR_NAME_EXIST = -2;
        

        public static String WAP_PUSH = "wap_push";
        public static int WAP_PUSH_DEFAULT = -1;
        public static int WAP_PUSH_DISABLE = 0;
        public static int WAP_PUSH_ENABLE = 1;
        
        /**
         * <P>Type: INT</P>
         */
        public static String NAME_SOURCE = "name_source";
        public static int DEFAULT_SOURCE = 0;
        public static int SIM_SOURCE = 1;
        public static int USER_INPUT = 2;
        
    }
    
    public static int[] SIMBackgroundRes = new int[] {
        0,//com.android.internal.R.drawable.zzz_sim_background_blue, //46002
        0,//com.android.internal.R.drawable.zzz_sim_background_orange, //46001
        0,//com.android.internal.R.drawable.zzz_sim_background_green, 
        0//com.android.internal.R.drawable.zzz_sim_background_purple
    };
    
    public static class SIMInfo {
        public long mSimId = 0L;
        public String mICCId;
        public static  int mSimContactPhotoRes = 0;

        public String mDisplayName = "";
        public String mNumber = "";
        public int mDispalyNumberFormat = SimInfo.DISLPAY_NUMBER_DEFAULT;
        public int mColor;
        public int mDataRoaming = SimInfo.DATA_ROAMING_DEFAULT;
        public int mSlot = 0;
        public int mSimBackgroundRes = SIMBackgroundRes[SimInfo.COLOR_DEFAULT];//0;
        public int mWapPush = -1;
        private SIMInfo() {
        }
        
        public static class ErrorCode {
            public static int ERROR_GENERAL = -1;
            public static int ERROR_NAME_EXIST = -2;
        }
        private static SIMInfo fromCursor(Cursor cursor) {
            SIMInfo info = new SIMInfo();
            info.mSimId = cursor.getLong(cursor.getColumnIndexOrThrow(SimInfo._ID));
            info.mICCId = cursor.getString(cursor.getColumnIndexOrThrow(SimInfo.ICC_ID));
            info.mDisplayName = cursor.getString(cursor.getColumnIndexOrThrow(SimInfo.DISPLAY_NAME));
            info.mNumber = cursor.getString(cursor.getColumnIndexOrThrow(SimInfo.NUMBER));
            info.mDispalyNumberFormat = cursor.getInt(cursor.getColumnIndexOrThrow(SimInfo.DISPLAY_NUMBER_FORMAT));
            info.mColor = cursor.getInt(cursor.getColumnIndexOrThrow(SimInfo.COLOR));
            info.mDataRoaming = cursor.getInt(cursor.getColumnIndexOrThrow(SimInfo.DATA_ROAMING));
            info.mSlot = cursor.getInt(cursor.getColumnIndexOrThrow(SimInfo.SLOT));
            int size = SIMBackgroundRes.length;
            if (info.mColor >= 0 && info.mColor < size) {
                info.mSimBackgroundRes = SIMBackgroundRes[info.mColor];
            }
            info.mWapPush = cursor.getInt(cursor.getColumnIndexOrThrow(SimInfo.WAP_PUSH));
            return info;
        }
        
        //Gionee 20130106 guoyx add for Qualcomm Multi Sim begin
        private static SIMInfo fromIndex(int index) {
            SIMInfo info = new SIMInfo();
            info.mSimId = index + 1;//sim1:1  sim2:2
            info.mICCId = TelephonyManager.getDefault().getSimSerialNumber();
            info.mDisplayName = TelephonyManager.getDefault().getSimOperatorName();//"sim " + index;//
            info.mNumber = TelephonyManager.getDefault().getLine1Number();
            info.mDispalyNumberFormat = 0;
            
            String simOperator = TelephonyManager.getDefault().getSimOperator();
            if ("46001".equals(simOperator)) 
                info.mColor = 1; //orange
            else if ("46002".equals(simOperator))
                info.mColor = 0; //blue
            else if ("46003".equals(simOperator))
                info.mColor = 2; //green
            else 
                info.mColor = 3; //purple
            
            info.mDataRoaming = 0;
            info.mSlot = index; //slot1:0  slot2:1
            int size = SIMBackgroundRes.length;
            if (info.mColor >= 0 && info.mColor < size) {
                info.mSimBackgroundRes = SIMBackgroundRes[info.mColor];
            }
            info.mWapPush = -1;
            return info;
        }
        //Gionee 20130106 guoyx add for Qualcomm Multi Sim end
        
        /**
         * 
         * @param ctx
         * @return the array list of Current SIM Info
         */
        public static List<SIMInfo> getInsertedSIMList(Context ctx) {
            ArrayList<SIMInfo> simList = new ArrayList<SIMInfo>();
            // Gionee lihuafang 2012-05-31 modify for CR00613892 begin
            /*
            Cursor cursor = ctx.getContentResolver().query(SimInfo.CONTENT_URI, 
                    null, SimInfo.SLOT + "!=" + SimInfo.SLOT_NONE, null, null);
            */
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                Cursor cursor = ctx.getContentResolver().query(SimInfo.CONTENT_URI, 
                        null, SimInfo.SLOT + "!=" + SimInfo.SLOT_NONE, null, "slot");
                // Gionee lihuafang 2012-05-31 modify for CR00613892 end
                try {
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            simList.add(SIMInfo.fromCursor(cursor));
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } else {
                TelephonyManager tm = TelephonyManager.getDefault();
                for (int i = 0; i < 1/*tm.getPhoneCount()*/; i++) {
                	// Gionee fengjianyi 2013-03-06 modify for CR00775170 start
                    if (tm.hasIccCard()){
                        simList.add(SIMInfo.fromIndex(i));
                    }
                	// Gionee fengjianyi 2013-03-06 modify for CR00775170 end
                }
            }
            return simList;
        }
        
        /**
         * 
         * @param ctx
         * @return array list of all the SIM Info include what were used before
         */
        public static List<SIMInfo> getAllSIMList(Context ctx) {
            ArrayList<SIMInfo> simList = new ArrayList<SIMInfo>();
			boolean is_singlesim=true;
            if (is_singlesim) {
                Cursor cursor = ctx.getContentResolver().query(SimInfo.CONTENT_URI, 
                        null, null, null, null);
                try {
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            simList.add(SIMInfo.fromCursor(cursor));
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } else {
                TelephonyManager tm = TelephonyManager.getDefault();
                for (int i = 0; i < 1/*tm.getPhoneCount()*/; i++) {
                    if ((tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT)/* &&
                            (tm.getSimState(i) != TelephonyManager.SIM_STATE_DEACTIVATED)*/){
                        simList.add(SIMInfo.fromIndex(i));
                    }
                }
            }
            return simList;
        }
        
        /**
         * 
         * @param ctx
         * @param SIMId the unique SIM id
         * @return SIM-Info, maybe null
         */
        public static SIMInfo getSIMInfoById(Context ctx, long SIMId) {
//            if (SIMId <= 0 ) return null;
            Cursor cursor = ctx.getContentResolver().query(ContentUris.withAppendedId(SimInfo.CONTENT_URI, SIMId), 
                    null, null, null, null);
            try {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        return SIMInfo.fromCursor(cursor);
                    }
                }
            } catch(Exception e){}finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return null;
/*            int slotId = (int)SIMId -1;
            TelephonyManager tm = TelephonyManager.getDefault();
            if ((tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT)/*
                    && (tm.getSimState(slotId) != TelephonyManager.SIM_STATE_DEACTIVATED)) {
                return SIMInfo.fromIndex(slotId);
            } else {
                return null;
            }*/
        }
        
        /**
         * 
         * @param ctx
         * @param SIMName the Name of the SIM Card
         * @return SIM-Info, maybe null
         */
        public static SIMInfo getSIMInfoByName(Context ctx, String SIMName) {
            if (SIMName == null) return null;
            Cursor cursor = ctx.getContentResolver().query(SimInfo.CONTENT_URI, 
                    null, SimInfo.DISPLAY_NAME + "=?", new String[]{SIMName}, null);
            try {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        return SIMInfo.fromCursor(cursor);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return null;
        }
        
        /**
         * @param ctx
         * @param cardSlot
         * @return The SIM-Info, maybe null
         */
        public static SIMInfo getSIMInfoBySlot(Context ctx, int cardSlot) {
            if (cardSlot < 0) return null;
            Cursor cursor = ctx.getContentResolver().query(SimInfo.CONTENT_URI, 
                    null, SimInfo.SLOT + "=?", new String[]{String.valueOf(cardSlot)}, null);
            try {
                if (cursor != null) {
                   if (cursor.moveToFirst()) {
                        return SIMInfo.fromCursor(cursor);
                    }
                }
            }catch(Exception e){} finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return null;
/*            TelephonyManager tm = TelephonyManager.getDefault();
            if (cardSlot >= 0 
                    &&(tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT)/*
                    && (tm.getSimState(cardSlot) != TelephonyManager.SIM_STATE_DEACTIVATED)) {
                return SIMInfo.fromIndex(cardSlot);
            } else {
                return null;
            }*/
        }
        
        /**
         * @param ctx
         * @param iccid 
         * @return The SIM-Info, maybe null
         */
        public static SIMInfo getSIMInfoByICCId(Context ctx, String iccid) {
            if (iccid == null) return null;
            Cursor cursor = ctx.getContentResolver().query(SimInfo.CONTENT_URI, 
                    null, SimInfo.ICC_ID + "=?", new String[]{iccid}, null);
            try {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        return SIMInfo.fromCursor(cursor);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return null;
        }
        
        /**
         * @param ctx
         * @param SIMId
         * @return the slot of the SIM Card, -1 indicate that the SIM card is missing
         */
        public static int getSlotById(Context ctx, long SimId) {
//            if (SimId <= 0 ) return SimInfo.SLOT_NONE;
            Cursor cursor = ctx.getContentResolver().query(ContentUris.withAppendedId(SimInfo.CONTENT_URI, SimId), 
                    new String[]{SimInfo.SLOT}, null, null, null);
            try {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        return cursor.getInt(0);
                    }
                }
            } catch(Exception e){}finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
          return SimInfo.SLOT_NONE;
//            int slotId = (int)SimId - 1;
//            return  slotId;
        }
        public static int getIdBySlot(Context ctx, int  slot) {
            
            Cursor cursor = ctx.getContentResolver().query(SimInfo.CONTENT_URI, 
                    new String[]{SimInfo._ID}, SimInfo.SLOT+" = "+slot, null, null);
            try {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        return cursor.getInt(0);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
//            int slotId = (int)SimId - 1;
//            return  slotId;
          return 0;
        }        
        /**
         * @param ctx
         * @param SIMName
         * @return the slot of the SIM Card, -1 indicate that the SIM card is missing
         */
        public static int getSlotByName(Context ctx, String SIMName) {
            if (SIMName == null) return SimInfo.SLOT_NONE;
            Cursor cursor = ctx.getContentResolver().query(SimInfo.CONTENT_URI, 
                    new String[]{SimInfo.SLOT}, SimInfo.DISPLAY_NAME + "=?", new String[]{SIMName}, null);
            try {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        return cursor.getInt(0);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return SimInfo.SLOT_NONE;
        }
        
        /**
         * @param ctx
         * @return current SIM Count
         */
        public static int getInsertedSIMCount(Context ctx) {
            Cursor cursor = ctx.getContentResolver().query(SimInfo.CONTENT_URI, 
                    null, SimInfo.SLOT + "!=" + SimInfo.SLOT_NONE, null, null);
            try {
                if (cursor != null) {
                    return cursor.getCount();
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return 0;
/*            int count = 0;
            TelephonyManager tm = TelephonyManager.getDefault();
            for (int i = 0; i < 1/*tm.getPhoneCount(); i++) {
                if ((tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) /*&&
                        (tm.getSimState(i) != TelephonyManager.SIM_STATE_DEACTIVATED)){
                    count++;
                }
            }
            return count;*/
            
        }
        
        /**
         * @param ctx
         * @return the count of all the SIM Card include what was used before
         */
        public static int getAllSIMCount(Context ctx) {
            Cursor cursor = ctx.getContentResolver().query(SimInfo.CONTENT_URI, 
                    null, null, null, null);
            try {
                if (cursor != null) {
                    return cursor.getCount();
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return 0;
        }
        
        /**
         * set display name by SIM ID
         * @param ctx
         * @param displayName
         * @param SIMId
         * @return -1 means general error, -2 means the name is exist. >0 means success
         */
        public static int setDisplayName(Context ctx, String displayName, long SIMId) {
            if (displayName == null || SIMId <= 0) return ErrorCode.ERROR_GENERAL;
            Cursor cursor = ctx.getContentResolver().query(SimInfo.CONTENT_URI, 
                    new String[]{SimInfo._ID}, SimInfo.DISPLAY_NAME + "=?", new String[]{displayName}, null);
            try {
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        return ErrorCode.ERROR_NAME_EXIST;
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            ContentValues value = new ContentValues(1);
            value.put(SimInfo.DISPLAY_NAME, displayName);
            return ctx.getContentResolver().update(ContentUris.withAppendedId(SimInfo.CONTENT_URI, SIMId), 
                    value, null, null);
        }
        
        /**
         * @param ctx
         * @param number
         * @param SIMId
         * @return >0 means success
         */
        public static int setNumber(Context ctx, String number, long SIMId) {
            if (number == null || SIMId <= 0) return -1;
            ContentValues value = new ContentValues(1);
            value.put(SimInfo.NUMBER, number);
            return ctx.getContentResolver().update(ContentUris.withAppendedId(SimInfo.CONTENT_URI, SIMId), 
                    value, null, null);
        }
        
        /**
         * 
         * @param ctx
         * @param color
         * @param SIMId
         * @return >0 means success
         */
        public static int setColor(Context ctx, int color, long SIMId) {
            int size = SIMBackgroundRes.length;
            if (color < 0 || SIMId <= 0 || color >= size) return -1;
            ContentValues value = new ContentValues(1);
            value.put(SimInfo.COLOR, color);
            return ctx.getContentResolver().update(ContentUris.withAppendedId(SimInfo.CONTENT_URI, SIMId), 
                    value, null, null);
        }
        
        /**
         * set the format.0: none, 1: the first four digits, 2: the last four digits.
         * @param ctx
         * @param format
         * @param SIMId
         * @return >0 means success
         */
        public static int setDispalyNumberFormat(Context ctx, int format, long SIMId) {
            if (format < 0 || SIMId <= 0) return -1;
            ContentValues value = new ContentValues(1);
            value.put(SimInfo.DISPLAY_NUMBER_FORMAT, format);
            return ctx.getContentResolver().update(ContentUris.withAppendedId(SimInfo.CONTENT_URI, SIMId), 
                    value, null, null);
        }
        
        /**
         * set data roaming.0:Don't allow data when roaming, 1:Allow data when roaming
         * @param ctx
         * @param roaming
         * @param SIMId
         * @return >0 means success
         */
        public static int setDataRoaming(Context ctx, int roaming, long SIMId) {
            if (roaming < 0 || SIMId <= 0) return -1;
            ContentValues value = new ContentValues(1);
            value.put(SimInfo.DATA_ROAMING, roaming);
            return ctx.getContentResolver().update(ContentUris.withAppendedId(SimInfo.CONTENT_URI, SIMId), 
                    value, null, null);
        }
        
        /**
         * set the wap push flag
         * @return >0 means success
         */
        public static int setWAPPush(Context ctx, int enable, long SIMId) {
            if (enable > 1 || enable < -1 || SIMId <= 0) {
                return -1;
            }
            ContentValues value = new ContentValues(1);
            value.put(SimInfo.WAP_PUSH, enable);
            return ctx.getContentResolver().update(ContentUris.withAppendedId(SimInfo.CONTENT_URI, SIMId),
                    value, null, null);
        }
        /**
         * Insert the ICC ID and slot if needed
         * @param ctx
         * @param ICCId
         * @param slot
         * @return
         */
        public static Uri insertICCId(Context ctx, String ICCId, int slot) {
            if (ICCId == null) {
                throw new IllegalArgumentException("ICCId should not null.");
            }
            Uri uri;
            ContentResolver resolver = ctx.getContentResolver();
            String selection = SimInfo.ICC_ID + "=?";
            Cursor cursor = resolver.query(SimInfo.CONTENT_URI, new String[]{SimInfo._ID, SimInfo.SLOT}, selection, new String[]{ICCId}, null);
            try {
                if (cursor == null || !cursor.moveToFirst()) {
                    ContentValues values = new ContentValues();
                    values.put(SimInfo.ICC_ID, ICCId);
                    values.put(SimInfo.COLOR, -1);
                    values.put(SimInfo.SLOT, slot);
                    uri = resolver.insert(SimInfo.CONTENT_URI, values);
                    //setDefaultName(ctx, ContentUris.parseId(uri), null);
                } else {
                    long simId = cursor.getLong(0);
                    int oldSlot = cursor.getInt(1);
                    uri = ContentUris.withAppendedId(SimInfo.CONTENT_URI, simId);
                    if (slot != oldSlot) {
                        ContentValues values = new ContentValues(1);
                        values.put(SimInfo.SLOT, slot);
                        resolver.update(uri, values, null, null);
                    } 
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            
            return uri;
        }
        
        public static int setDefaultName(Context ctx, long simId, String name) {
            if (simId <= 0)
                return ErrorCode.ERROR_GENERAL;
            String default_name = ctx.getString(SimInfo.DEFAULT_NAME_RES);
            ContentResolver resolver = ctx.getContentResolver();
            Uri uri = ContentUris.withAppendedId(SimInfo.CONTENT_URI, simId);
            if (name != null) {
                int result = setDisplayName(ctx, name, simId);
                if (result > 0) {
                    return result;
                }
            }
            int index = getAppropriateIndex(ctx, simId, name);
            String suffix = getSuffixFromIndex(index);
            ContentValues value = new ContentValues(1);
            String display_name = (name == null ? default_name + " " + suffix : name + " " + suffix);
            value.put(SimInfo.DISPLAY_NAME, display_name);
            return ctx.getContentResolver().update(uri, value, null, null);
        }
        
        private static String getSuffixFromIndex(int index) {
            if (index < 10) {
                return "0" + index;
            } else {
                return String.valueOf(index);
            }
            
        }
        private static int getAppropriateIndex(Context ctx, long simId, String name) {
            String default_name = ctx.getString(SimInfo.DEFAULT_NAME_RES);
            StringBuilder sb = new StringBuilder(SimInfo.DISPLAY_NAME + " LIKE ");
            if (name == null) {
                DatabaseUtils.appendEscapedSQLString(sb, default_name + '%');
            } else {
                DatabaseUtils.appendEscapedSQLString(sb, name + '%');
            }
            sb.append(" AND (");
            sb.append(SimInfo._ID + "!=" + simId);
            sb.append(")");
            
            Cursor cursor = ctx.getContentResolver().query(SimInfo.CONTENT_URI, new String[]{SimInfo._ID, SimInfo.DISPLAY_NAME},
                    sb.toString(), null, SimInfo.DISPLAY_NAME);
            ArrayList<Long> array = new ArrayList<Long>();
            int index = SimInfo.DEFAULT_NAME_MIN_INDEX;
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String display_name = cursor.getString(1);
                    
                    if (display_name != null) {
                        int length = display_name.length();
                        if (length >= 2) {
                            String sub = display_name.substring(length -2);
                            if (TextUtils.isDigitsOnly(sub)) {
                                long value = Long.valueOf(sub);
                                array.add(value);
                            }
                        }
                    }
                }
                cursor.close();
            }
            for (int i = SimInfo.DEFAULT_NAME_MIN_INDEX; i <= SimInfo.DEFAULT_NAME_MAX_INDEX; i++) {
                if (array.contains((long)i)) {
                    continue;
                } else {
                    index = i;
                    break;
                }
            }
            return index;
        }
    }
    
    public static class CdmaCallOptions implements BaseColumns {
        /**
         * The content:// style URL for this table
         */
        public static Uri CONTENT_URI =
            Uri.parse("content://cdma/calloption");

        /**
         * The default sort order for this table
         */
        public static String DEFAULT_SORT_ORDER = "name ASC";

        public static String NAME = "name";

        public static String MCC = "mcc";

        public static String MNC = "mnc";

        public static String NUMERIC = "numeric";

        public static String NUMBER = "number";

        public static String TYPE = "type";

        public static String CATEGORY = "category";

        public static String STATE = "state";

    }
    
    /**
     * record the avoid CDMA current system network log
     *
     */
    public static class AvoidNetWork implements BaseColumns {
        /**
         * The content:// style URL for this table
         */
        public static Uri CONTENT_URI =
                Uri.parse("content://cdma/avoid_net");

        /**
         * The default sort order for this table
         */
        public static String MCC = "mcc";

        public static String MNC = "mnc";

    }
}

    
    
    



