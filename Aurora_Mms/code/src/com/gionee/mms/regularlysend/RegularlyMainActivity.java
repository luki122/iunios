package com.gionee.mms.regularlysend;

import java.util.Calendar;

import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.data.Conversation;
import com.android.mms.data.WorkingMessage;
import com.android.mms.model.SlideshowModel;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.GenericPdu;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.android.mms.ui.MessagingPreferenceActivity;

import aurora.preference.AuroraPreferenceManager;
import gionee.provider.GnTelephony.Mms;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
import com.aurora.android.mms.pdu.EncodedStringValue;
import com.aurora.android.mms.pdu.PduBody;
import com.aurora.android.mms.pdu.PduPersister;
import com.aurora.android.mms.pdu.SendReq;
import com.aurora.android.mms.pdu.PduHeaders;
//Aurora xuyong 2013-11-15 modified for google adapt end
import gionee.provider.GnTelephony.MmsSms.PendingMessages;

import android.R.anim;
import android.R.integer;
import aurora.app.AuroraActivity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import gionee.provider.GnTelephony.Sms;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;

public class RegularlyMainActivity {
    final String ADDRESS = "address";
    final String DATE = "date";
    final String READ = "read";
    final String STATUS = "status";
    final String TYPE = "type";
    final String SUBJECT = "sbuject";
    final String BODY = "body";
    final String SEEN = "seen";
    final String THREAD_ID = "thread_id";
    final String SIM_ID = "sim_id";
    private Calendar mCalendar;
    private boolean mIsRegularly;

    private long mMmsThreadid;
    private Uri mMessageUri;
    private SlideshowModel mSlideshow;
    private Context mContext;
    private MessageInsertListener mInsertListener = null;
    private static int mNumberRegularly = 0;

    public interface MessageInsertListener {
        void onSmsInsertFinish(boolean isFinish);

        void onMmsInsertFinish(boolean isFinish);
    }

    public void setMessageInsertListener(MessageInsertListener l) {
        mInsertListener = l;
    }

    public void setCalendar(Calendar calendar) {
        // TODO Auto-generated method stub
        mCalendar = calendar;
    }

    public Calendar getCalendar() {
        return this.mCalendar;
    }

    public void setIsRegularly(boolean b) {
        // TODO Auto-generated method stub
        mIsRegularly = b;
    }

    public boolean getIsRegularly() {
        return this.mIsRegularly;
    }

    public void insertSms(Context context, String[] addrs, String text,
            int simid) {
        for (int i = 0; i < addrs.length; i++) {
            addSmsToDb(context, addrs[i], text, simid);
        }
        if (mInsertListener != null) {
            mInsertListener.onSmsInsertFinish(true);
        }
//        addTimetoAlarmManager(context, mCalendar);
    }

    public void insertMms(Context context, String[] addrs, String text,
            int simId, CharSequence subject, Uri uri,
            SlideshowModel slideshow) {
        mMessageUri = uri;
        mSlideshow = slideshow;
        mContext = context;
        Log.d("RegSendMsg", "RegularlyMainActivity  insertMms addrs = "+ addrs.toString());
        PduPersister persister = PduPersister.getPduPersister(mContext);
        SendReq sendReq = makeSendReq(mCalendar.getTimeInMillis(), addrs, subject);
        if (mMessageUri == null) {
            mMessageUri = createDraftMmsMessage(persister, sendReq, mSlideshow);
        } else {
            updateDraftMmsMessage(mMessageUri, persister, mSlideshow, sendReq);
        }

        try {
            PduPersister p = PduPersister.getPduPersister(mContext);
            if (mMessageUri != null) {
                GenericPdu pdu = p.load(mMessageUri);

                if (pdu.getMessageType() != PduHeaders.MESSAGE_TYPE_SEND_REQ) {
                    throw new MmsException("Invalid message: " + pdu.getMessageType());
                }

                sendReq = (SendReq) pdu;
            }
            updatePreferencesHeadersGemini(sendReq, simId);
            sendReq.setDate(mCalendar.getTimeInMillis() / 1000L);
            sendReq.setMessageSize(slideshow.getCurrentSlideshowSize());

            p.updateHeaders(mMessageUri, sendReq);

            // Move the message into MMS Outbox
            Uri sendUri = p.move(mMessageUri, Mms.Outbox.CONTENT_URI);

            ContentValues values = new ContentValues(1);
            values.put(Mms.SIM_ID, simId);
            SqliteWrapper.update(mContext, mContext.getContentResolver(), sendUri, values, null, null);

            // set pending message sim id
            long msgId = ContentUris.parseId(sendUri);

            Uri.Builder uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
            uriBuilder.appendQueryParameter("protocol", "mms");
            uriBuilder.appendQueryParameter("message", String.valueOf(msgId));

            Cursor cr = SqliteWrapper.query(mContext, mContext.getContentResolver(),
                    uriBuilder.build(), null, null, null, null);
            if (cr != null) {
                try {
                    if ((cr.getCount() == 1) && cr.moveToFirst()) {
                        ContentValues valuesforPending = new ContentValues();
                        valuesforPending.put(PendingMessages.SIM_ID, simId);
                        int columnIndex = cr.getColumnIndexOrThrow(
                                        PendingMessages._ID);
                        long id = cr.getLong(columnIndex);
                        SqliteWrapper.update(mContext, mContext.getContentResolver(),
                                        PendingMessages.CONTENT_URI,
                                        valuesforPending, PendingMessages._ID + "=" + id, null);
                    }else{
                        Log.d("RegSendMsg", "can not find message to set pending sim id, msgId=" + msgId);
                    }
                }finally {
                    cr.close();
                }
            }
        } catch (MmsException e) {
            // TODO: handle exception
        }

        if (mInsertListener != null) {
            mInsertListener.onMmsInsertFinish(true);
        }
//        addTimetoAlarmManager(mContext, mCalendar);
    }

    public static SendReq makeSendReq(long date, String[] addStrings,
            CharSequence subject) {
        // TODO Auto-generated method stub String[] dests = conv.getRecipients().getNumbers(false /*don't scrub for MMS address */);
        String[] dests = addStrings;

        SendReq req = new SendReq();
        EncodedStringValue[] encodedNumbers = EncodedStringValue.encodeStrings(dests);
        if (encodedNumbers != null) {
            req.setTo(encodedNumbers);
        }

        if (!TextUtils.isEmpty(subject)) {
            req.setSubject(new EncodedStringValue(subject.toString()));
        }

        req.setDate(date / 1000L);

        return req;
    }

    private Uri createDraftMmsMessage(PduPersister persister, SendReq sendReq,
            SlideshowModel slideshow) {

             if (slideshow == null){
                 return null;
             }
             try {
                 PduBody pb = slideshow.toPduBody();
                 sendReq.setBody(pb);
                 Uri res = persister.persist(sendReq, Mms.Draft.CONTENT_URI);
                 slideshow.sync(pb);
                 return res;
             } catch (MmsException e) {
                 return null;
             }
             catch (IllegalArgumentException e){
                 return null;
             }
    }

    private void updateDraftMmsMessage(Uri uri, PduPersister persister,
            SlideshowModel slideshow, SendReq sendReq) {
        try {
            persister.updateHeaders(uri, sendReq);
        } catch (IllegalArgumentException e) {
            Log.d("RegularlyMain", "updateDraftMmsMessage: cannot update message " + uri);
        }

        if (slideshow == null) {
            Thread.dumpStack();
            Log.e("RegularlyMain", "updateDraftMmsMessage, sendreq " + sendReq);
            return;
        }
        final PduBody pb = slideshow.toPduBody();

        try {
            persister.updateParts(uri, pb);
        } catch (MmsException e) {
            Log.e("RegularlyMain", "updateDraftMmsMessage: cannot update message " + uri);
        }

        slideshow.sync(pb);
    
    }

    private static final boolean DEFAULT_DELIVERY_REPORT_MODE  = false;
    private static final boolean DEFAULT_READ_REPORT_MODE      = false;
    private static final long    DEFAULT_EXPIRY_TIME     = 7 * 24 * 60 * 60;
    private void updatePreferencesHeadersGemini(SendReq sendReq, int simId) throws MmsException {
        SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(mContext);
        sendReq.setExpiry(prefs.getLong(
                MessagingPreferenceActivity.EXPIRY_TIME, DEFAULT_EXPIRY_TIME));
        String priority = prefs.getString(MessagingPreferenceActivity.PRIORITY, "Normal");
        if (priority.equals("High")) {
            sendReq.setPriority(PduHeaders.PRIORITY_HIGH);
        } else if (priority.equals("Low")) {
            sendReq.setPriority(PduHeaders.PRIORITY_LOW);
        } else {
            sendReq.setPriority(PduHeaders.PRIORITY_NORMAL);
        }
        // Delivery report.
        // Aurora xuyong 2014-06-09 modified for bug #5554 start
        boolean dr = prefs.getBoolean(MessagingPreferenceActivity.MMS_DELIVERY_REPORT_MODE,
        // Aurora xuyong 2014-06-09 modified for bug #5554 end
                DEFAULT_DELIVERY_REPORT_MODE);
        sendReq.setDeliveryReport(dr?PduHeaders.VALUE_YES:PduHeaders.VALUE_NO);

        // Read report.
        boolean rr = prefs.getBoolean(Integer.toString(simId)+ "_" + MessagingPreferenceActivity.READ_REPORT_MODE,
                DEFAULT_READ_REPORT_MODE);
        sendReq.setReadReport(rr?PduHeaders.VALUE_YES:PduHeaders.VALUE_NO);
    }

    public void addSmsToDb(Context context, String addr, String text,
            int simid) {
        Log.d("RegSendMsg", "RegularlyMainActivity.addSmsToDb.addr = "+ addr);
        Uri insertedUri = null;
        ContentValues insertValues = new ContentValues(6);
        insertValues.put(Sms.ADDRESS, addr);
        insertValues.put(Sms.DATE, mCalendar.getTimeInMillis());
        insertValues.put(Sms.BODY, text);
        insertValues.put(Sms.SIM_ID, simid);
        insertValues.put(Sms.TYPE, -1);
        insertValues.put(Sms.READ, 1);
        insertValues.put(Sms.SEEN, 1);
        insertValues.put("import_sms", true);
        insertedUri = SqliteWrapper.insert(context, context.getContentResolver(), Sms.CONTENT_URI, insertValues);
    }

    public void addTimetoAlarmManager(Context context, long date) {
        int code = (int) date;
        Intent intent = new Intent("com.gionee.mms.regularsend");
        intent.putExtra("alarmtime", date);
        Log.d("RegSendMsg", "RegularlyMainActivity  date = "+ date);
        PendingIntent pi = PendingIntent.getBroadcast(context, code, intent,0);

        AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, date, pi);
    }

    //gionee gaoj 2012-8-21 added for CR00678407 start
    public void updateSmsRegularlyTime(Context context, Uri uri, long date) {
        Log.d("RegSendMsg", "RegularlyMainActivity   updateSmsRegularlyTime   uri = "+ uri);
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues(1);
        values.put("date", date);
        resolver.update(uri, values, null, null);
        addTimetoAlarmManager(context, date);
    }

    public void updateMmsRegularlyTime(Context context, Uri uri, long  date) throws MmsException{
        Log.d("RegSendMsg", "RegularlyMainActivity   updateMmsRegularlyTime   uri = "+ uri);
        PduPersister p = PduPersister.getPduPersister(context);
        GenericPdu pdu = p.load(uri);
        //gionee gaoj 2012-9-18 added for CR00693731 start
        if (pdu.getMessageType() != PduHeaders.MESSAGE_TYPE_SEND_REQ) {
            throw new MmsException("Invalid message: " + pdu.getMessageType());
        }
        //gionee gaoj 2012-9-18 added for CR00693731 end
        SendReq sendReq = (SendReq) pdu;
        sendReq.setDate(date / 1000L);
        // gionee zhouyj 2012-12-07 add for CR00692425 start 
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues(1);
        values.put("date", date / 1000L);
        resolver.update(uri, values, null, null);
        // gionee zhouyj 2012-12-07 add for CR00692425 end 
        addTimetoAlarmManager(context, date);
    }
    //gionee gaoj 2012-8-21 added for CR00678407 end

    //gionee gaoj 2012-8-21 added for CR00678365 start
    public static void reSetAlarmManager(Context context, long date) {
        int code = (int) date;
        Intent intent = new Intent("com.gionee.mms.regularsend");
        intent.putExtra("alarmtime", date);
        Log.d("RegSendMsg", "RegularlyMainActivity  reSetAlarmManager code = "+ code);
        Log.d("RegSendMsg", "RegularlyMainActivity  reSetAlarmManager date = "+ date);
        PendingIntent pi = PendingIntent.getBroadcast(context, code, intent,0);

        AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, date, pi);
    }
    //gionee gaoj 2012-8-21 added for CR00678365 end
}