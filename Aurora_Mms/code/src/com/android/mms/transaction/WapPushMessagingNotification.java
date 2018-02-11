/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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

package com.android.mms.transaction;
//Aurora xuyong 2013-11-15 modified for google adapt start
import static com.aurora.android.mms.pdu.PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND;
import static com.aurora.android.mms.pdu.PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF;
//Aurora xuyong 2013-11-15 modified for google adapt end

import com.android.mms.R;
import com.android.mms.LogTag;
import com.android.mms.data.Contact;
// Aurora xuyong 2013-11-15 added for bug #752 start
import com.android.mms.ui.ComposeMessageActivity;
// Aurora xuyong 2013-11-15 added for bug #752 end
import com.android.mms.ui.WPMessageActivity;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.MessagingPreferenceActivity;

import android.provider.BaseColumns;
import gionee.provider.GnTelephony;
import com.android.mms.util.AddressUtils;
import com.android.mms.util.DownloadManager;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.EncodedStringValue;
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.android.mms.pdu.PduPersister;
import com.aurora.android.mms.util.SqliteWrapper;
//Aurora xuyong 2013-11-15 modified for google adapt end

import android.app.Notification;
import android.app.NotificationManager;
import com.gionee.mms.adaptor.NotificationPlus;
import com.gionee.mms.adaptor.NotificationManagerPlus;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import aurora.preference.AuroraPreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.media.AudioManager;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

//add for cmcc dir ui mode
import com.android.mms.MmsConfig;

//gionee gaoj 2012-4-10 added for CR00555790 start
import com.android.mms.MmsApp;
import com.android.mms.data.Conversation;
//gionee gaoj 2012-4-10 added for CR00555790 end

//gionee gaoj 2012-6-11 added for CR00623393 start
import android.os.PowerManager;
//gionee gaoj 2012-6-11 added for CR00623393 end
//Gionee zengxuanhui 20120914 add for CR00692293 begin
import com.gionee.internal.telephony.GnPhone;
import android.os.SystemProperties;
// Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
//import com.mediatek.audioprofile.AudioProfileManager;
// Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
//Gionee zengxuanhui 20120914 add for CR00692293 end
/**
 * This class is used to update the notification indicator. It will check whether
 * there are unread messages. If yes, it would show the notification indicator,
 * otherwise, hide the indicator.
 */
public class WapPushMessagingNotification {
    private static final String TAG = "Mms/WapPush";

    private static final int NOTIFICATION_ID = 127;
    public static final int SL_AUTOLAUNCH_NOTIFICATION_ID = 5577;

    private static final Uri URL_MESSAGES = GnTelephony.WapPush.CONTENT_URI;

    // Gionee fengjianyi 2012-09-12 modify for CR00692545 start
    // This must be consistent with the column constants below.
    private static final String[] WAPPUSH_STATUS_PROJECTION = new String[] {
            GnTelephony.WapPush.THREAD_ID, GnTelephony.WapPush.DATE,
            GnTelephony.WapPush._ID, GnTelephony.WapPush.URL, GnTelephony.WapPush.TEXT, 
            GnTelephony.WapPush.ADDR, GnTelephony.WapPush.SIM_ID};
    // Gionee fengjianyi 2012-09-12 modify for CR00692545 end

    private static final int COLUMN_THREAD_ID   = 0;
    private static final int COLUMN_DATE        = 1;
    private static final int COLUMN_WAPPUSH_ID = 2;
    private static final int COLUMN_WAPPUSH_URL    = 3;
    private static final int COLUMN_WAPPUSH_TEXT    = 4;
    private static final int COLUMN_WAPPUSH_ADDRESS    = 5;
    
    // Gionee fengjianyi 2012-09-12 add for CR00692545 start
    private static final int COLUMN_WAPPUSH_SIMID    = 6;
    // Gionee fengjianyi 2012-09-12 add for CR00692545 end

    //gionee gaoj 2012-6-11 added for CR00623393 start
    private static final int WAKE_LOCK_TIMEOUT = 5000;
    //gionee gaoj 2012-6-11 added for CR00623393 end

    //Gionee zengxuanhui 20120914 add for CR00692293 begin
    private static int mReceivedSmsSimId = GnPhone.GEMINI_SIM_1;
    private static final boolean gnGeminiRingtoneSupport = 
            SystemProperties.get("ro.gn.gemini.ringtone.support").equals("yes");
    //Gionee zengxuanhui 20120914 add for CR00692293 end

    //gionee <gaoj> <2013-06-29> add for CR00826240 begin
    private static final String ACTION_NEW_EVENT = "gionee.infozone.action.NEW_EVENT";
    private static final String EXTRA_PKG = "package";
    private static final String EXTRA_CLS = "wgt_cls";
    //gionee <gaoj> <2013-06-29> add for CR00826240 end
    
    private static final String NEW_INCOMING_SM_CONSTRAINT =
            "(" + GnTelephony.WapPush.SEEN + " = 0)";

    private static final WapPushNotificationInfoComparator INFO_COMPARATOR =
            new WapPushNotificationInfoComparator();

    private WapPushMessagingNotification() {
    }
 
    /**
     * Checks to see if there are any "unseen" messages or delivery
     * reports.  Shows the most recent notification if there is one.
     * Does its work and query in a worker thread.
     *
     * @param context the context to use
     */
    
    public static void nonBlockingUpdateNewMessageIndicator(final Context context, final boolean isNew){
        Log.d(TAG, "nonBlockingUpdateNewMessageIndicator");
        new Thread(new Runnable() {
            public void run() {
                blockingUpdateNewMessageIndicator(context, isNew);
            }
        }).start();      
    }
    
    /**
     * Checks to see if there are any unread messages or delivery
     * reports.  Shows the most recent notification if there is one.
     *
     * @param context the context to use
     * @param isNew if notify a new message comes, it should be true, otherwise, false.
     */
    public static void blockingUpdateNewMessageIndicator(Context context, boolean isNew) {
        SortedSet<WapPushNotificationInfo> accumulator =
                new TreeSet<WapPushNotificationInfo>(INFO_COMPARATOR);
        Set<Long> threads = new HashSet<Long>(4);

        //gionee gaoj 2012-6-11 added for CR00623393 start
        /*PowerManager.WakeLock wakelock = null;
        AudioManager audiomanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        boolean isSilentMode = (audiomanager.getRingerMode() == AudioManager.RINGER_MODE_SILENT);
        if (MmsApp.mGnMessageSupport) {
            if (isSilentMode) {
                PowerManager powermanager = (PowerManager) context
                        .getSystemService(Context.POWER_SERVICE);
                wakelock = powermanager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP, "blockingUpdateNewMessageIndicator");
                wakelock.setReferenceCounted(true);
            }
        }*/
        //gionee gaoj 2012-6-11 added for CR00623393 end
        // Aurora liugj 2013-11-27 added for bug-1041 start
          // Aurora liugj 2013-12-10 deleted for ignore bug-1041 start
        /*PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        if (!isScreenOn) {
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
            // 点亮屏幕
            // Aurora xuyong 2013-12-09 modified for bug #1156 start
            wl.acquire(5000);
            // Aurora xuyong 2013-12-09 modified for bug #1156 end
         }*/
          // Aurora liugj 2013-12-10 deleted for ignore bug-1041 end
        // Aurora liugj 2013-11-27 added for bug-1041 end
        int count = 0;
        count += accumulateNotificationInfo(
                accumulator, getWapPushNewMessageNotificationInfo(context, threads));

        cancelNotification(context, NOTIFICATION_ID);
        if (!accumulator.isEmpty()) {
            accumulator.first().deliver(context, isNew, count, threads.size());
        }
        //gionee gaoj 2012-6-11 added for CR00623393 start
        /*if (MmsApp.mGnMessageSupport && isSilentMode) {
            if (wakelock != null) {
                wakelock.acquire(WAKE_LOCK_TIMEOUT);
            }
        }*/
        //gionee gaoj 2012-6-11 added for CR00623393 end
        
        //gionee <gaoj> <2013-06-29> add for CR00826240 begin
        if (MmsApp.mGnLockScrnSupport && isNew && count > 0 && !MmsApp.mIsSafeModeSupport) {
            Intent newEvent = new Intent(ACTION_NEW_EVENT);
            //package name
            newEvent.putExtra(EXTRA_PKG, "com.android.mms");
            //app widget class name with full path 
            newEvent.putExtra(EXTRA_CLS, "com.android.mms.widget.MmsWidgetProvider");
            context.sendBroadcast(newEvent);
        }
        //gionee <gaoj> <2013-06-29> add for CR00826240 end
    }
    
    public static void updateAllNotifications(Context context){
        nonBlockingUpdateNewMessageIndicator(context,false);
    }
    
    private static final int accumulateNotificationInfo(
            SortedSet set, WapPushNotificationInfo info) {
        if (info != null) {
            set.add(info);
            return info.mCount;
        }
        return 0;
    }

    private static final class WapPushNotificationInfo {
        public Intent mClickIntent;
        public String mDescription;
        public int mIconResourceId;
        public CharSequence mTicker;
        public long mTimeMillis;
        public String mTitle;
        public int mCount;
        //add for cmcc dir ui mode
        public Uri mUri;

        public WapPushNotificationInfo(
                Intent clickIntent, String description, int iconResourceId,
                CharSequence ticker, long timeMillis, String title, int count, Uri uri) {
            mClickIntent = clickIntent;
            mDescription = description;
            mIconResourceId = iconResourceId;
            mTicker = ticker;
            mTimeMillis = timeMillis;
            mTitle = title;
            mCount = count;
            mUri = uri;
        }

        public void deliver(Context context, boolean isNew, int count, int uniqueThreads) {
            updateNotification(
                    context, mClickIntent, mDescription, mIconResourceId, isNew,
                    (isNew? mTicker : null), // only display the ticker if the message is new
                    mTimeMillis, mTitle, count, uniqueThreads, mUri);
        }

        public long getTime() {
            return mTimeMillis;
        }
    }

    private static final class WapPushNotificationInfoComparator
            implements Comparator<WapPushNotificationInfo> {
        public int compare(
                WapPushNotificationInfo info1, WapPushNotificationInfo info2) {
            return Long.signum(info2.getTime() - info1.getTime());
        }
    }

    public static final WapPushNotificationInfo getWapPushNewMessageNotificationInfo(
            Context context, Set<Long> threads) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = SqliteWrapper.query(context, resolver, URL_MESSAGES,
                            WAPPUSH_STATUS_PROJECTION, NEW_INCOMING_SM_CONSTRAINT,
                            null, GnTelephony.WapPush.DATE + " desc");

        if (cursor == null) {
            return null;
        }
        
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            // TODO get the description by query the table.
            long messageId = cursor.getLong(COLUMN_WAPPUSH_ID);
            String url = cursor.getString(COLUMN_WAPPUSH_URL);
            String text = cursor.getString(COLUMN_WAPPUSH_TEXT);
            String address = cursor.getString(COLUMN_WAPPUSH_ADDRESS);
            long threadId = cursor.getLong(COLUMN_THREAD_ID);
            long timeMillis = cursor.getLong(COLUMN_DATE);
            
            // Gionee fengjianyi 2012-09-19 add for CR00692545 start
            //Gionee <zhouyj> <2013-05-16> modify for CR00788320 begin
            if (MmsApp.mIsVoiceSupportEnable && VoiceNotificationTransaction.getVoiceConfig()) {
            //Gionee <zhouyj> <2013-05-16> modify for CR00788320 end
                int simid = cursor.getInt(COLUMN_WAPPUSH_SIMID);
                //Gionee <zhouyj> <2013-04-28> modify for CR00802651 begin
                VoiceNotificationTransaction.setSpeakMessage(simid, 0, address, "push", null);
                //Gionee <zhouyj> <2013-04-28> modify for CR00802651 end
            }
            // Gionee fengjianyi 2012-09-19 add for CR00692545 end
            
            //get body
            String body;
            if(text!=null&&!text.equals("")){
                body = text;
            }else{
                body = url;
            }
            
            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mEncryption) {
                int encryption = Conversation.queryThreadId(context, threadId);
                if (encryption == 1) {
                    body = context.getString(R.string.gn_confirm_encryption);
                }
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
            WapPushNotificationInfo info = getNewMessageNotificationInfo(
                    address, body, context, -1/*R.drawable.stat_notify_wappush*/,
                    null, threadId, timeMillis, cursor.getCount(),
                    URL_MESSAGES.buildUpon().appendPath(Long.toString(messageId)).build());
            
            /*
             * for auto-lanuch;
             * if sl or si's text is null, it should be automatically lanuched.
             */
            if(text==null||text.equals("")){
                info.mClickIntent.putExtra("URL",url);
            }
            
            threads.add(threadId);
            while (cursor.moveToNext()) {
                threads.add(cursor.getLong(COLUMN_THREAD_ID));
            }

            return info;
        } finally {
            cursor.close();
        }
    }

    private static final WapPushNotificationInfo getNewMessageNotificationInfo(
            String address,
            String body,
            Context context,
            int iconResourceId,
            String subject,
            long threadId,
            long timeMillis,
            int count,
            Uri uri) {
        //Intent clickIntent = null;
        // Aurora xuyong 2013-11-15 modified for bug #752 start
        Intent clickIntent = ComposeMessageActivity.createIntent(context, threadId);
        // Aurora xuyong 2013-11-15 modified for bug #752 end
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        String senderInfo = buildTickerMessage(
                context, address, null, null).toString();
        String senderInfoName = senderInfo.substring(
                0, senderInfo.length() - 2);
        CharSequence ticker = buildTickerMessage(
                context, address, subject, body);

        return new WapPushNotificationInfo(
                clickIntent, body, iconResourceId, ticker, timeMillis,
                senderInfoName, count, uri);
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager nm = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);

        nm.cancel(notificationId);
        
        // Gionee fengjianyi 2012-09-19 add for CR00692545 start
        if (VoiceNotificationTransaction.isSpeaking() && notificationId == NOTIFICATION_ID) {
            VoiceNotificationTransaction.stopSpeak();
        }
        // Gionee fengjianyi 2012-09-19 add for CR00692545 end
    }

    private static void updateNotification(
            Context context,
            Intent clickIntent,
            String description,
            int iconRes,
            boolean isNew,
            CharSequence ticker,
            long timeMillis,
            String title,
            int messageCount,
            int uniqueThreadCount,
            Uri uri) {
        SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(context);

        if (!sp.getBoolean(MessagingPreferenceActivity.NOTIFICATION_ENABLED, true)) {
            return;
        }

        Notification notification = new Notification(iconRes, ticker, timeMillis);

        // If we have more than one unique thread, change the title (which would
        // normally be the contact who sent the message) to a generic one that
        // makes sense for multiple senders, and change the Intent to take the
        // user to the conversation list instead of the specific thread.
        if (uniqueThreadCount > 1) {
            title = context.getString(R.string.notification_multiple_title);
//            clickIntent = new Intent(Intent.ACTION_MAIN);
//            clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//            clickIntent.setType("vnd.android-dir/mms-sms");
            clickIntent.putExtra("THREAD_COUNT",uniqueThreadCount);
        }

        //add for cmcc dir ui mode
        if (MmsConfig.getMmsDirMode()) {
            clickIntent = new Intent(Intent.ACTION_MAIN);
            clickIntent.setFlags(//Intent.FLAG_ACTIVITY_NEW_TASK
                     Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // Aurora liugj 2013-11-06 modified for aurora's new feature start
            //clickIntent.putExtra("floderview_key", 0);// need show inbox
            clickIntent.setClassName("com.android.mms", "com.aurora.mms.ui.AuroraConvListActivity");
                // Aurora liugj 2013-11-06 modified for aurora's new feature end
        }
        // If there is more than one message, change the description (which
        // would normally be a snippet of the individual message text) to
        // a string indicating how many unread messages there are.
        if (messageCount > 1) {
            description = context.getString(R.string.notification_multiple,
                    Integer.toString(messageCount));
        }
        //add for cmcc dir ui mode
        else {
            if (MmsConfig.getMmsDirMode()) {
               // open the only wappush directly
                clickIntent.putExtra("msg_type", 3);
                clickIntent.setFlags(0);//clear the flag.
                clickIntent.setData(uri);
                    // Aurora liugj 2013-11-06 modified for aurora's new feature start
                clickIntent.setClassName("com.android.mms", "com.aurora.mms.ui.AuroraConvListActivity");
                    // Aurora liugj 2013-11-06 modified for aurora's new feature end
            }
        }

        // Make a startActivity() PendingIntent for the notification.
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Update the notification.
        notification.setLatestEventInfo(context, title, description, pendingIntent);

        if (isNew) {
            String ringtoneStr = sp.getString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE, null);
            //Gionee zengxuanhui 20120914 add for CR00692293 begin
            if (gnGeminiRingtoneSupport && mReceivedSmsSimId == GnPhone.GEMINI_SIM_2) {
                ringtoneStr = sp.getString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE2, null);
            }
            //Gionee zengxuanhui 20120914 add for CR00692293 end
            // Gionee fengjianyi 2012-09-19 modify for CR00692545 start
            //Uri ringtone = TextUtils.isEmpty(ringtoneStr) ? null : Uri.parse(ringtoneStr);
            Uri ringtone = null;
            if (MmsApp.mIsVoiceSupportEnable && VoiceNotificationTransaction.getVoiceConfig()) {
                VoiceNotificationTransaction.speakRemindInfo();
            } else {
                ringtone = TextUtils.isEmpty(ringtoneStr) ? null : Uri.parse(ringtoneStr);
            }
            // Gionee fengjianyi 2012-09-19 modify for CR00692545 end
            
            MessagingNotification.processNotificationSound(context, notification, ringtone);
            // add for CTA 5.3.3
            NotificationPlus notiPlus = new NotificationPlus.Builder(context)
                    .setTitle(context.getString(R.string.new_message))
                    .setMessage(context.getString(R.string.notification_multiple, Integer.toString(messageCount)))
                    .setPositiveButton(context.getString(R.string.view), pendingIntent)
                    .create();
            NotificationManagerPlus.notify(1, notiPlus);
        }

        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        // Aurora xuyong 2014-08-13 added for 1+ notification start
        notification.ledARGB = 0xff00ff00;
        notification.ledOnMS = 500;
        notification.ledOffMS = 2000;
        // Aurora xuyong 2014-08-13 added for 1+ notification end
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        NotificationManager nm = (NotificationManager)
            context.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(NOTIFICATION_ID, notification);
    }

    protected static CharSequence buildTickerMessage(
            Context context, String address, String subject, String body) {
        String displayAddress = Contact.get(address, true).getName();
        
        StringBuilder buf = new StringBuilder(
                displayAddress == null
                ? ""
                : displayAddress.replace('\n', ' ').replace('\r', ' '));
        buf.append(':').append(' ');

        int offset = buf.length();
        if (!TextUtils.isEmpty(subject)) {
            subject = subject.replace('\n', ' ').replace('\r', ' ');
            buf.append(subject);
            buf.append(' ');
        }

        if (!TextUtils.isEmpty(body)) {
            body = body.replace('\n', ' ').replace('\r', ' ');
            buf.append(body);
        }

        SpannableString spanText = new SpannableString(buf.toString());
        spanText.setSpan(new StyleSpan(Typeface.BOLD), 0, offset,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spanText;
    }
    
    
    //SL auto lanuch notification
    public static boolean notifySlAutoLanuchMessage(Context context, String url) {
        Log.d(TAG, "notifySlAutoLanuchMessage");
        SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(context);
        Notification notification = new Notification();
        boolean enabled = sp.getBoolean(MessagingPreferenceActivity.NOTIFICATION_ENABLED, true);
        Log.d(TAG, "notifySlAutoLanuchMessage, enabled = "+enabled);        
        if (!enabled) {
            return false;
        }
        String ringtoneStr = sp.getString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE, null);
        Uri ringtone = TextUtils.isEmpty(ringtoneStr) ? null : Uri.parse(ringtoneStr);
        MessagingNotification.processNotificationSound(context, notification, ringtone);
        notification.tickerText = url;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.ledARGB = 0xff00ff00;
        notification.ledOnMS = 500;
        notification.ledOffMS = 2000;
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(SL_AUTOLAUNCH_NOTIFICATION_ID, notification);
        return true;
    }
    
    //Gionee zengxuanhui 20120914 add for CR00692293 begin
    public static void setIncomingSmsSimId(int simId)  {
        Log.d(TAG, "simId="+simId);
        if (simId == GnPhone.GEMINI_SIM_1 || simId == GnPhone.GEMINI_SIM_2) {
            mReceivedSmsSimId = simId;
        }
    }
    //Gionee zengxuanhui 20120914 add for CR00692293 end
}
