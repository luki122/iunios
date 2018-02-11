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

package com.android.phone;

import java.util.Map;
import java.util.Set;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.ServiceState;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyCapabilities;

import java.util.HashSet;

import android.graphics.BitmapFactory;


public class PrivateNotificationMgr {
    private static final String LOG_TAG = "PrivateNotificationMgr";
    protected static final boolean DBG =
//            (PhoneGlobals.DBG_LEVEL >= 1) && (SystemProperties.getInt("ro.debuggable", 0) == 1);
    		(PhoneGlobals.DBG_LEVEL >= 1);
    // Do not check in with VDBG = true, since that may write PII to the system log.
    protected static final boolean VDBG = true;

    protected static final String[] CALL_LOG_PROJECTION = new String[] {
        Calls._ID,
        Calls.NUMBER,
        Calls.DATE,
        Calls.DURATION,
        Calls.TYPE,
        "simid"
    };


    /** The singleton PrivateNotificationMgr instance. */
    protected static PrivateNotificationMgr sInstance;

    protected PhoneGlobals mApp;
    private Phone mPhone;
    protected CallManager mCM;

    protected Context mContext;
    protected NotificationManager mNotificationManager;
    private Toast mToast;
    private boolean mShowingSpeakerphoneIcon;
    private boolean mShowingMuteIcon;


    // used to track the missed call counter, default to 0.
    private int mNumberMissedCalls = 0;

    // Query used to look up caller-id info for the "call log" notification.
    protected QueryHandler mQueryHandler = null;
    protected static final int CALL_LOG_TOKEN = -1;
    private static final int CONTACT_TOKEN = -2;
//aurora add zhouxiaobing 20140512 start for CallerInfo
    public static final String UNKNOWN_NUMBER = "-1";
    public static final String PRIVATE_NUMBER = "-2";
    public static final String PAYPHONE_NUMBER = "-3";
//aurora add zhouxiaobing 20140512 end for CallerInfo
    public static int mSimId = -1; 
    
    static final int NOTIFICATION_BASE = 17000 + 3 + 100;
    static final int HANGUP_PRIVATE_RINGING_CALL_NOTIFICATION = NOTIFICATION_BASE+ 1;
    static final int MISSED_CALL_NOTIFICATION = NOTIFICATION_BASE + 2;

    /**
     * Private constructor (this is a singleton).
     * @see init()
     */
    protected PrivateNotificationMgr(PhoneGlobals app) {
        mApp = app;
        mContext = app;
        mNotificationManager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        mPhone = app.phone;
        mCM = app.mCM;
    }

    static PrivateNotificationMgr init(PhoneGlobals app) {
        synchronized (PrivateNotificationMgr.class) {
            if (sInstance == null) {
                sInstance = new PrivateNotificationMgr(app);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            return sInstance;
        }
    }

    //切换到隐私空间时候发送未读的未接来电通知
    protected void updateNotificationsWhenGoToPrivateMode() {
        if (DBG) log("updateNotificationsAtStartup()...");        
        
        if(AuroraPrivacyUtils.getCurrentAccountId() <= 0) {
        	return;
        }

        mQueryHandler = new QueryHandler(mContext.getContentResolver());

        StringBuilder where = new StringBuilder("type=");
        where.append(Calls.MISSED_TYPE);
        where.append(" AND new=1");
        where.append(" AND privacy_id = " + AuroraPrivacyUtils.getCurrentAccountId());
        // start the query
        if (DBG) log("- start call log query...");
        mQueryHandler.startQuery(CALL_LOG_TOKEN, null, Calls.CONTENT_URI,  CALL_LOG_PROJECTION,
                where.toString(), null, "date ASC");


    }

    /** The projection to use when querying the phones table */
    static final String[] PHONES_PROJECTION = new String[] {
        PhoneLookup.NUMBER,
        PhoneLookup.DISPLAY_NAME,
        PhoneLookup._ID
    };

    /**
     * Class used to run asynchronous queries to re-populate the notifications we care about.
     * There are really 3 steps to this:
     *  1. Find the list of missed calls
     *  2. For each call, run a query to retrieve the caller's name.
     *  3. For each caller, try obtaining photo.
     */
    
    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
    
    
    protected class QueryHandler extends AsyncQueryHandler{

        private class NotificationInfo {
            public String name;
            public String number;
            public String type;
            public long date;
        }

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
                case CALL_LOG_TOKEN:
                    if (DBG) log("call log query complete.");

                    // initial call to retrieve the call list.
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            // for each call in the call log list, create
                            // the notification object and query contacts
                            NotificationInfo n = getNotificationInfo (cursor);

                            if (DBG) log("query contacts for number: " + n.number);

                            mQueryHandler.startQuery(CONTACT_TOKEN, n,
                                    Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, n.number),
                                    PHONES_PROJECTION, null, null, PhoneLookup.NUMBER);
                        }

                        if (DBG) log("closing call log cursor.");
                        cursor.close();
                    }
                    break;
                case CONTACT_TOKEN:
                    if (DBG) log("contact query complete.");

                    // subqueries to get the caller name.
                    if ((cursor != null) && (cookie != null)){
                        NotificationInfo n = (NotificationInfo) cookie;

                        Uri personUri = null;
                        if (cursor.moveToFirst()) {
                            n.name = cursor.getString(
                                    cursor.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
                            long person_id = cursor.getLong(
                                    cursor.getColumnIndexOrThrow(PhoneLookup._ID));
                            if (DBG) {
                                log("contact :" + n.name + " found for phone: " + n.number
                                        + ". id : " + person_id);
                            }
                            personUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, person_id);
                        }

                        notifyPrivateMissedCall(n.name, n.number, n.type, null, null, n.date, AuroraPrivacyUtils.getCurrentAccountId());

                        if (DBG) log("closing contact cursor.");
                        cursor.close();
                    }
                    break;
                default:
            }
        }

        private final NotificationInfo getNotificationInfo(Cursor cursor) {
            NotificationInfo n = new NotificationInfo();
            n.name = null;
            mSimId = cursor.getInt(cursor.getColumnIndexOrThrow("simid"));
            n.number = cursor.getString(cursor.getColumnIndexOrThrow(Calls.NUMBER));
            n.type = cursor.getString(cursor.getColumnIndexOrThrow(Calls.TYPE));
            n.date = cursor.getLong(cursor.getColumnIndexOrThrow(Calls.DATE));

            if ( (n.number.equals(UNKNOWN_NUMBER)) ||//aurora change zhouxiaobing 20140512
                 (n.number.equals(PRIVATE_NUMBER)) ||
                 (n.number.equals(PAYPHONE_NUMBER)) ) {
                n.number = null;
            }

            if (DBG) log("NotificationInfo constructed for number: " + n.number);

            return n;
        }
    }

  
    protected static void configureLedNotification(Notification note) {
    	note.ledARGB=0xff00ff00;
    	note.ledOnMS=500;
    	note.ledOffMS=2100;
    	note.flags|=Notification.FLAG_SHOW_LIGHTS; 
    }


    /** Returns an intent to be invoked when the missed call notification is cleared. */
    private PendingIntent createClearPrivateMissedCallsIntent() {
        Intent intent = new Intent(mContext, ClearPrivateRingingCallService.class);
        intent.setAction(ClearPrivateRingingCallService.ACTION_CLEAR_MISSED_CALLS);
        return PendingIntent.getService(mContext, 0, intent, 0);
    }

 
    void cancelPrivateMissedCallNotification() {
   	    if (DBG) log("cancelPrivateMissedCallNotification()");
        mNumberMissedCalls = 0;
        mNotificationManager.cancel(MISSED_CALL_NOTIFICATION);
        PhoneGlobals.getInstance().notificationMgr.stopI2MissCallLed();
        ReminderUtils.updatePhoneReminder();
    }

//    private int mHangupPivateCallCountFake = 0;
//    private int mHangupBlackSmsCountFake = 0;
    void notifyHangupPrivateRingingCallFake(long privateId) {     	
   	    if (DBG) log("notifyHangupPrivateRingingCallFake()");
//   	    mHangupPivateCallCountFake++;
   	    final Notification.Builder builder = new Notification.Builder(mContext);    	 
        Drawable largeIcon = mContext.getResources().getDrawable(R.drawable.pirvate_noti_fake);
        builder.setLargeIcon(((BitmapDrawable) largeIcon).getBitmap());
        builder.setUsesChronometer(false);
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);        
        builder.setDeleteIntent(createClearHangupPrivateRingCallIntent());
        builder.setContentIntent(ManagePrivate.createGotoPrivateCalllogIntentFake(mContext));
        String expandedText;           
//        if (DBG) log("notifyHangupPrivateRingingCallFake() mHangupPivateCallCount =" + mHangupPivateCallCountFake);
//        expandedText = mContext.getString(R.string.aurora_hangup_private_ringing_call_content, mHangupPivateCallCountFake);
        expandedText = "";
        builder.setContentTitle(AuroraPrivacyUtils.getPrivateRingNotificationText(privateId));
        builder.setContentText(expandedText);
//        Notification notification = new Notification.BigTextStyle(builder).bigText(expandedText).build();
        Notification notification = builder.build();
        notification.icon = R.drawable.aurora_stat_notify_missed_call2;
        mNotificationManager.notify(HANGUP_PRIVATE_RINGING_CALL_NOTIFICATION, notification);
   }
    
       
//    private int mHangupPivateCallCount = 0;
//    void notifyHangupPrivateRingingCallReal() {     	
//   	    if (DBG) log("notifyHangupPrivateRingingCallReal()");
//   	    mHangupPivateCallCount++;
//   	    final Notification.Builder builder = new Notification.Builder(mContext);    	 
//        Drawable largeIcon = mContext.getResources().getDrawable(R.drawable.private_call_icon);
//        builder.setLargeIcon(((BitmapDrawable) largeIcon).getBitmap());
//        builder.setUsesChronometer(false);
//        builder.setWhen(System.currentTimeMillis());
//        builder.setAutoCancel(true);        
//        builder.setDeleteIntent(createClearHangupPrivateRingCallIntent());
//        builder.setContentIntent(PhoneGlobals.createGotoPrivateCalllogIntentFake(mContext));
//        String expandedText;           
//        if (DBG) log("notifyHangupPrivateRingingCallReal() mHangupPivateCallCount =" + mHangupPivateCallCount);
//        expandedText = mContext.getString(R.string.aurora_hangup_private_ringing_call_content, mHangupPivateCallCount);
//        builder.setContentTitle(AuroraPhoneUtils.getPrivateRingNotificationText());
//        builder.setContentText(expandedText);
//        Notification notification = new Notification.BigTextStyle(builder).bigText(expandedText).build();
//        notification.icon = R.drawable.reject_launcher;
//        mNotificationManager.notify(HANGUP_PRIVATE_RINGING_CALL_NOTIFICATION, notification);
//   }
    
    private PendingIntent createClearHangupPrivateRingCallIntent() {
        Intent intent = new Intent(mContext, ClearPrivateRingingCallService.class);
        intent.setAction(ClearPrivateRingingCallService.ACTION_CLEAR_HANGUP_PRIVATE_RINGING_CALLS);
        return PendingIntent.getService(mContext, 0, intent, 0);
    }
    
    void cancelHangupPrivateRingingCallNotification() {
   	    if (DBG) log("cancelHangupPrivateRingingCallNotification()");
//   	    mHangupPivateCallCountFake = 0;
//   	    mHangupPivateCallCount = 0;
        mNotificationManager.cancel(HANGUP_PRIVATE_RINGING_CALL_NOTIFICATION);
    }
    
    
    void notifyPrivateMissedCall(String name, String number, String type, Drawable photo, Bitmap photoIcon, long date, long privateId) {
    	
    	if(NotificationMgr.useCustomView) {
    		notifyPrivateMissedCallCustom(name, number, type, photo, photoIcon, date, privateId);
    		return;
    	}

    	
    	
        final Intent callLogIntent = createPrivateCallLogIntent();

        if (VDBG) {
            log("notifyPrivateMissedCall(). name: " + name + ", number: " + number
                + ", label: " + type + ", photo: " + photo + ", photoIcon: " + photoIcon
                + ", date: " + date);
        }

        int titleResId;
        String expandedText, callName;

        mNumberMissedCalls++;

        if (name != null && TextUtils.isGraphic(name)) {
            callName = name;
        } else if (!TextUtils.isEmpty(number)){
            callName = number;
        } else {
            callName = mContext.getString(R.string.unknown);
        }

        if (mNumberMissedCalls == 1) {
            titleResId = R.string.notification_missedCallTitle;
            expandedText = callName;
        } else {
            titleResId = R.string.notification_missedCallsTitle;
            expandedText = mContext.getString(R.string.notification_missedCallsMsg,
                    mNumberMissedCalls);
        }

        String tickerText = "";
        if (mNumberMissedCalls == 1) {
        	tickerText = mContext.getString(R.string.notification_missedCallTicker, callName);
        } else {
        	tickerText = mContext.getString(R.string.notification_missedCallsMsg, mNumberMissedCalls);
        }
        
        
        Notification.Builder builder = new Notification.Builder(mContext);
        //aurora change liguangyu 20131108 for missing call notification start
        builder.setSmallIcon(R.drawable.aurora_stat_notify_missed_call)
        //aurora change liguangyu 20131108 for missing call notification end
                .setTicker(tickerText)
                .setWhen(date)
                .setContentTitle(mContext.getText(titleResId))
                .setContentText(expandedText)
                .setContentIntent(PendingIntent.getActivity(mContext, 0, callLogIntent, 0))
                .setAutoCancel(true)
                .setDeleteIntent(createClearPrivateMissedCallsIntent());

        // Simple workaround for issue 6476275; refrain having actions when the given number seems
        // not a real one but a non-number which was embedded by methods outside (like
        // PhoneUtils#modifyForSpecialCnapCases()).
        // TODO: consider removing equals() checks here, and modify callers of this method instead.
        if (mNumberMissedCalls == 1
                && !TextUtils.isEmpty(number)
                && !TextUtils.equals(number, mContext.getString(R.string.private_num))
                && !TextUtils.equals(number, mContext.getString(R.string.unknown))){
            if (DBG) log("Add actions with the number " + number);

            //aurora change liguangyu 20131108 for missing call notification start
            builder.addAction(R.drawable.aurora_ic_lockscreen_answer_normal,
                    mContext.getString(R.string.notification_missedCall_call_back),
                    PhoneGlobals.getCallBackPendingIntent(mContext, number));

            builder.addAction(R.drawable.aurora_ic_text_holo_dark_button,
                    mContext.getString(R.string.notification_missedCall_message),
                    PhoneGlobals.getPrivateSendSmsFromNotificationPendingIntent(mContext, number, privateId));
            //aurora change liguangyu 20131108 for missing call notification end

            //aurora change liguangyu 20131112 for photo start
            if (photoIcon != null) {
            	photoIcon = RoundBitmapUtils.toRoundBitmap(photoIcon);
                builder.setLargeIcon(photoIcon);
            } else if (photo instanceof BitmapDrawable) {
            	Bitmap bitmap = ((BitmapDrawable)photo).getBitmap();
//            	bitmap = RoundBitmapUtils.toRoundBitmap(bitmap);
                builder.setLargeIcon(((BitmapDrawable) photo).getBitmap());
            	builder.setLargeIcon(bitmap);
            } else {
            	Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher_phone);
            	builder.setLargeIcon(bitmap);
            }
            //aurora change liguangyu 20131112 for photo end
        } else {
            if (DBG) {
                log("Suppress actions. number: " + number + ", missedCalls: " + mNumberMissedCalls);
            }
        }
                
    
        Notification notification = builder.getNotification();
        configureLedNotification(notification);
        mNotificationManager.notify(MISSED_CALL_NOTIFICATION, notification);
        
        ReminderUtils.insertPhoneReminder(expandedText);
        
    }
    
    void cancelAllNotification() {
        mNumberMissedCalls = 0;
        mNotificationManager.cancel(MISSED_CALL_NOTIFICATION);
//        mNotificationManager.cancel(HANGUP_PRIVATE_RINGING_CALL_NOTIFICATION);
        updateNotificationsWhenGoToPrivateMode();
        PhoneGlobals.getInstance().notificationMgr.stopI2MissCallLed();
    }
    
    private Intent createPrivateCallLogIntent() {
        Intent intent = new Intent("com.aurora.privacymanage.GOTO_CALL_PRIVACY_MODULE");
        return intent;
    }
    
    private void notifyPrivateMissedCallCustom(String name, String number, String type, Drawable photo, Bitmap photoIcon, long date, long privateId) {
        final Intent callLogIntent = createPrivateCallLogIntent();

        if (VDBG) {
            log("notifyPrivateMissedCallCustom(). name: " + name + ", number: " + number
                + ", label: " + type + ", photo: " + photo + ", photoIcon: " + photoIcon
                + ", date: " + date);
        }

        int titleResId;
        String expandedText, callName;

        mNumberMissedCalls++;

        if (name != null && TextUtils.isGraphic(name)) {
            callName = name;
        } else if (!TextUtils.isEmpty(number)){
            callName = number;
        } else {
            callName = mContext.getString(R.string.unknown);
        }

        if (mNumberMissedCalls == 1) {
            titleResId = R.string.notification_missedCallTitle;
            expandedText = callName;
        } else {
            titleResId = R.string.notification_missedCallsTitle;
            expandedText = mContext.getString(R.string.notification_missedCallsMsg,
                    mNumberMissedCalls);
        }

        String tickerText = "";
        if (mNumberMissedCalls == 1) {
        	tickerText = mContext.getString(R.string.notification_missedCallTicker, callName);
        } else {
        	tickerText = mContext.getString(R.string.notification_missedCallsMsg, mNumberMissedCalls);
        }
        
        Notification.Builder builder = new Notification.Builder(mContext);
        builder.setSmallIcon(R.drawable.aurora_stat_notify_missed_call)
                .setTicker(tickerText)
                .setWhen(date)
                .setAutoCancel(true)
                .setDeleteIntent(createClearPrivateMissedCallsIntent());
        Notification notification = builder.getNotification();
        RemoteViews contentView = new RemoteViews(mContext.getPackageName(), R.layout.custom_misscall_notification); 
        // Aurora xuyong 2015-08-28 modified for bug #15926 start
//        contentView.setImageViewResource(R.id.image, R.drawable.aurora_stat_privacy_notify_missed_call2);
        Bitmap notifyIcon = BitmapFactory.decodeResource(mApp.getResources(), R.drawable.aurora_stat_privacy_notify_missed_call2); 
//        notifyIcon = RoundBitmapUtils.toRoundBitmap(notifyIcon);
    	contentView.setImageViewBitmap(R.id.image, notifyIcon);
        // Aurora xuyong 2015-08-28 modified for bug #15926 end
        contentView.setTextViewText(R.id.title, mContext.getText(titleResId)); 
        contentView.setTextViewText(R.id.text, expandedText);
        if(PhoneUtils.isMultiSimEnabled() && mNumberMissedCalls == 1) {
            contentView.setImageViewResource(R.id.aurora_sim_slot, SimIconUtils.getSimIconNotification(mSimId));
		     contentView.setViewVisibility(R.id.aurora_sim_slot, View.VISIBLE);
		 } else {
			 contentView.setImageViewResource(R.id.aurora_sim_slot, 0);
		     contentView.setViewVisibility(R.id.aurora_sim_slot, View.GONE);
		 }
        // Aurora xuyong 2015-08-28 deleted for bug #15926 start
        //contentView.setViewVisibility(R.id.private_image, View.VISIBLE);
        // Aurora xuyong 2015-08-28 deleted for bug #15926 end
        
        contentView.setOnClickPendingIntent(R.id.main_content, PendingIntent.getActivity(mContext, 0, callLogIntent, 0));
        
        if(!TextUtils.isEmpty(number)
                && !TextUtils.equals(number, mContext.getString(R.string.private_num))
                && !TextUtils.equals(number, mContext.getString(R.string.unknown))){
            if (DBG) log("Add actions with the number " + number);
            contentView.setOnClickPendingIntent(R.id.dial, PhoneGlobals.getCallBackPendingIntent(mContext, number));
            contentView.setOnClickPendingIntent(R.id.sms, PhoneGlobals.getPrivateSendSmsFromNotificationPendingIntent(mContext, number, privateId));
        } 
        
        notification.contentView = contentView; 
        configureLedNotification(notification);
        mNotificationManager.notify(MISSED_CALL_NOTIFICATION, notification);
        
        ReminderUtils.insertPhoneReminder(expandedText);
        
    }
    
    
}
