package com.gionee.mms.regularlysend;

import com.android.mms.MmsApp;
import com.android.mms.R.string;
import com.android.mms.model.SlideshowModel;
import com.android.mms.transaction.DefaultRetryScheme;
import com.android.mms.transaction.MessageSender;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.MmsMessageSender;
import com.android.mms.transaction.SmsMessageSender;
import com.android.mms.transaction.SmsSingleRecipientSender;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.EncodedStringValue;
import com.aurora.android.mms.pdu.MultimediaMessagePdu;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.android.mms.util.Recycler;
import com.android.mms.util.SendingProgressTokenManager;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.provider.Telephony.Sms;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.android.mms.pdu.PduPersister;
//Aurora xuyong 2013-11-15 modified for google adapt end
import android.provider.Telephony.MmsSms.PendingMessages;
import gionee.provider.GnTelephony.Mms;
import gionee.provider.GnTelephony;
import android.provider.Telephony.MmsSms;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.SendReq;
//Aurora xuyong 2013-11-15 modified for google adapt end

import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SqliteWrapper;

public class AlarmReceiver extends BroadcastReceiver{

    // This must match SEND_PROJECTION.
    private Context mContext;
    private static final int SEND_COLUMN_ID         = 0;
    private static final int SEND_COLUMN_THREAD_ID  = 1;
    private static final int SEND_COLUMN_ADDRESS    = 2;
    private static final int SEND_COLUMN_BODY       = 3;
    private static final int SEND_COLUMN_STATUS     = 4;
    private static final int SEND_COLUMN_SIMID      = 5;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        mContext = context;
        Bundle bundle = intent.getExtras();
        long date = bundle.getLong("alarmtime");
        Log.d("RegSendMsg", "AlarmReceiver     onReceive    date = "+date);
        QuerysendSmsMessage(date);
        QuerysendMmsMessage(date);
    }

    public static final String[] SEND_PROJECTION = new String[] {
        Sms._ID,        //0
        Sms.THREAD_ID,  //1
        Sms.ADDRESS,    //2
        Sms.BODY,       //3
        Sms.STATUS,     //4
        GnTelephony.GN_SIM_ID      //5 "sim_id"
    };

    private void QuerysendSmsMessage(long date) {
        // TODO Auto-generated method stub
        //gionee gaoj 2012-10-25 added for CR00717652 start
        int position = -1;
        //gionee gaoj 2012-10-25 added for CR00717652 end
        boolean success = true;
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(Sms.CONTENT_URI, SEND_PROJECTION, "date = " + date, null, null);
            if (c != null) {
                Log.d("RegSendMsg", "AlarmReceiver  QuerysendSmsMessage   c.getCount() = "+c.getCount());
            }
            if (c != null && c.getCount() > 0) {
                //gionee gaoj 2012-9-7 modified for CR00688083 start
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    //gionee gaoj 2012-9-7 modified for CR00688083 end
                    String msgText = c.getString(SEND_COLUMN_BODY);
                    String address = c.getString(SEND_COLUMN_ADDRESS);
                    int threadId = c.getInt(SEND_COLUMN_THREAD_ID);
                    int status = c.getInt(SEND_COLUMN_STATUS);
                    int msgId = c.getInt(SEND_COLUMN_ID);
                    int simid = c.getInt(SEND_COLUMN_SIMID);
                    Uri msgUri = ContentUris.withAppendedId(Sms.CONTENT_URI, msgId);

                    Log.d("RegSendMsg", "AlarmReceiver  QuerysendSmsMessage   c.getCount() > 0   address = "+address);
                    SmsMessageSender sender = new SmsSingleRecipientSender(mContext,
                            address, msgText, threadId, status == Sms.STATUS_PENDING,
                            msgUri, simid);

                    try {
                      //Gionee <guoyx> <2013-05-27> modify for CR00819767 begin
                        if (MmsApp.mGnMultiSimMessage) {
                            sender.sendMessageGemini(SendingProgressTokenManager.NO_TOKEN, simid);
                        } else {
                            sender.sendMessage(SendingProgressTokenManager.NO_TOKEN);
                        }
                      //Gionee <guoyx> <2013-05-27> modify for CR00819767 end
                    } catch (MmsException e) {
                        Log.d("RegSendMsg", "AlarmReceiver  QuerysendSmsMessage send faile");
                        success = false;
                        //gionee gaoj 2012-10-25 added for CR00717652 start
                        position = c.getPosition();
                        //gionee gaoj 2012-10-25 added for CR00717652 end
                    }
                }
            }
        } finally {
            if (!success) {
                //gionee gaoj 2012-10-25 added for CR00717652 start
                c.moveToPosition(position);
                //gionee gaoj 2012-10-25 added for CR00717652 end
                int msgId = c.getInt(SEND_COLUMN_ID);
                Uri msgUri = ContentUris.withAppendedId(Sms.CONTENT_URI, msgId);
                messageFailedToSend(msgUri, SmsManager.RESULT_ERROR_GENERIC_FAILURE);
            }
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
    }

    private void messageFailedToSend(Uri uri, int error) {
        // TODO Auto-generated method stub
        Sms.moveMessageToFolder(mContext, uri, Sms.MESSAGE_TYPE_FAILED, error);
        // update sms status when failed. this Sms.STATUS is used for delivery report.
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(Sms.STATUS, Sms.STATUS_FAILED);
        SqliteWrapper.update(mContext, mContext.getContentResolver(), uri, contentValues, null, null);

        MessagingNotification.notifySendFailed(mContext, true);
    }

    private static final String[] MMS_OUTBOX_PROJECTION = {
        Mms._ID,                // 0
        Mms.SUBJECT,            // 1
        Mms.SUBJECT_CHARSET,    // 2
        Mms.SIM_ID,              // 3
        Mms.THREAD_ID            // 4
    };

    private void QuerysendMmsMessage(long date) {
        // TODO Auto-generated method stub
        long id;
        Uri messageUri = null;
        String subject = null;
        int simId = -1;
        int threadId = -1;
        SlideshowModel slideshow = null;
        Cursor cursor = null;
        SendReq sendReq = null;
        ContentResolver cr = mContext.getContentResolver();
        date = date / 1000L;
        final String selection = Mms.DATE + " = " + date;
        try {
            cursor = SqliteWrapper.query(mContext, cr,
                    Mms.Outbox.CONTENT_URI, MMS_OUTBOX_PROJECTION,
                    selection, null, null);
            if (cursor != null) {
                Log.d("RegSendMsg", "AlarmReceiver  QuerysendMmsMessage   cursor.getCount() > 0 = "+cursor.getCount());
            }
            if (cursor != null && cursor.getCount() > 0) {
                //gionee gaoj 2012-9-7 modified for CR00688083 start
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    //gionee gaoj 2012-9-7 modified for CR00688083 end
                    id = cursor.getLong(0);
                    messageUri = Mms.CONTENT_URI.buildUpon().appendPath(
                            Long.toString(id)).build();
                    if (!TextUtils.isEmpty(subject)) {
                        EncodedStringValue v = new EncodedStringValue(
                                cursor.getInt(2),
                                PduPersister.getBytes(cursor.getString(1)));
                        subject = v.getString();
                    }
                    simId = cursor.getInt(3);
                    threadId = cursor.getInt(4);

                    Log.d("RegSendMsg", "AlarmReceiver  QuerysendMmsMessage   messageUri = "+messageUri);
                    try {
                        PduPersister p = PduPersister.getPduPersister(mContext);
                        MultimediaMessagePdu msg = (MultimediaMessagePdu) p.load(messageUri);
                        slideshow = SlideshowModel.createFromPduBody(mContext, msg.getBody());

                        MessageSender sender = new MmsMessageSender(mContext, messageUri,
                                slideshow.getCurrentSlideshowSize());

                        //Gionee <guoyx> <2013-05-31> modify for CR00819767 begin
                        if (MmsApp.mGnMultiSimMessage) {
                            if (!sender.sendMessageGemini(threadId, simId)) {
                                // The message was sent through SMS protocol, we should
                                // delete the copy which was previously saved in MMS drafts.
                                SqliteWrapper.delete(mContext, mContext.getContentResolver(), messageUri, null, null);
                            }
                        } else {
                            if (!sender.sendMessage(threadId)) {
                                // The message was sent through SMS protocol, we should
                                // delete the copy which was previously saved in MMS drafts.
                                SqliteWrapper.delete(mContext, mContext.getContentResolver(), messageUri, null, null);
                            }
                        }
                        //Gionee <guoyx> <2013-05-31> modify for CR00819767 end

                        // Make sure this thread isn't over the limits in message count
                        Recycler.getMmsRecycler().deleteOldMessagesByThreadId(mContext, threadId);
                    } catch (Exception e) {
                        Log.d("mms", "Failed to send message: " + messageUri + ", threadId=" + threadId, e);
                    }
                }
            }
            
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }
}
