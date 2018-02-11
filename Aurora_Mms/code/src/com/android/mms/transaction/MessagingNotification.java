/*
 * Copyright (C) 2008 Esmertec AG.
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

package com.android.mms.transaction;
//Aurora xuyong 2013-11-15 modified for google adapt start
import static com.aurora.android.mms.pdu.PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND;
import static com.aurora.android.mms.pdu.PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF;
//Aurora xuyong 2013-11-15 modified for google adapt end
//Gionee guoyangxu 20120509 add for CR00594172 begin
import com.android.mms.MmsApp;
//Gionee guoyangxu 20120509 add for CR00594172 end
import com.android.mms.R;
import com.android.mms.LogTag;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ConversationList;
// Aurora xuyong 2014-06-19 added for multisim feature start
import com.android.mms.ui.MessageUtils;
// Aurora xuyong 2014-06-19 added for multisim feature end
import com.android.mms.ui.MessagingPreferenceActivity;
import com.android.mms.util.AddressUtils;
import com.android.mms.util.DownloadManager;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.EncodedStringValue;
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.android.mms.pdu.PduPersister;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.aurora.featureoption.FeatureOption;
// Aurora xuyong 2016-03-10 added for bug #20942 start
import android.app.Activity;
// Aurora xuyong 2016-03-10 added for bug #20942 end
import android.database.sqlite.SqliteWrapper;

import android.app.Notification;
import android.app.NotificationManager;
//Gionee guoyangxu 20120920 modified for MTK-QCOMM begin
// Aurora xuyong 2016-03-10 added for bug #20942 start
import com.aurora.mms.ui.ConvFragment;
// Aurora xuyong 2016-03-10 added for bug #20942 end
import com.gionee.mms.adaptor.NotificationPlus;
import com.gionee.mms.adaptor.NotificationManagerPlus;
//Gionee guoyangxu 20120920 modified for MTK-QCOMM end
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.AudioManager;
//Gionee guoyangxu 20120509 modified for CR00594172 begin
import android.media.RingtoneManager;
//Gionee guoyangxu 20120509 modified for CR00594172 end
import android.net.Uri;
// Aurora xuyong 2016-03-10 added for bug #20942 start
import android.os.Bundle;
// Aurora xuyong 2016-03-10 added for bug #20942 end
import android.os.Handler;
import aurora.preference.AuroraPreferenceManager;
import android.provider.Settings;
import android.provider.Telephony.Mms;
import gionee.provider.GnTelephony;
import android.provider.Telephony.Sms;
// Aurora xuyong 2015-01-16 added for aurora's new feature start
import android.support.v4.app.NotificationCompat;
// Aurora xuyong 2015-01-16 added for aurora's new feature end
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
// Aurora xuyong 2014-06-19 added for multisim feature start
import android.widget.RemoteViews;
import android.widget.TextView;
// Aurora xuyong 2014-06-19 added for multisim feature end
import android.widget.Toast;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
//Gionee guoyangxu 20120507 add for CR00594172 begin
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
//Gionee guoyangxu 20120507 add for CR00594172 end

// add for cmcc dir ui begin
import com.android.mms.MmsConfig;
// add for cmcc dir ui end
//Gionee guoyangxu 20120621 modified for CR00625902 begin
import com.gionee.mms.importexport.ConfigConstantUtils;
//Gionee guoyangxu 20120621 modified for CR00625902 end
//gionee gaoj 2012-4-10 added for CR00555790 start
import com.gionee.mms.ui.TabActivity;
//gionee gaoj 2012-4-10 added for CR00555790 end

//gionee gaoj 2012-6-11 added for CR00623393 start
import android.os.PowerManager;
//gionee gaoj 2012-6-11 added for CR00623393 end
//Gionee zengxuanhui 20120809 add for CR00672106 begin
import com.gionee.internal.telephony.GnPhone;
// Aurora xuyong 2014-10-30 added for privacy feature start
import com.privacymanage.service.AuroraPrivacyUtils;
// Aurora xuyong 2014-10-30 added for privacy feature end
import android.os.SystemProperties;
// Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
//import com.mediatek.audioprofile.AudioProfileManager;
// Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
import gionee.provider.GnTelephony.SIMInfo;
//Gionee zengxuanhui 20120809 add for CR00672106 end
//GIONEE:wangfei 2012-09-03 add for CR00686851 begin
import java.text.SimpleDateFormat;
import java.util.Date;
//GIONEE:wangfei 2012-09-03 add for CR00686851 end

//gionee gaoj 2013-3-11 added for CR00782858 start
import com.android.mms.widget.MmsWidgetProvider;
import com.aurora.mms.ui.AuroraConvListActivity;
import com.aurora.mms.util.Utils;
//gionee gaoj 2013-3-11 added for CR00782858 end
/**
 * This class is used to update the notification indicator. It will check whether
 * there are unread messages. If yes, it would show the notification indicator,
 * otherwise, hide the indicator.
 */
public class MessagingNotification {
    private static final String TAG = "MessagingNotification";
    public static Uri sMessageUri = null;
    private static final int NOTIFICATION_ID = 123;
    public static final int NOTIFICATION_PRIVACY_ID = 124;
    // Gionee: 20120918 chenrui add for CR00696600 begin
    private static final int MISS_MSG_NOTIFICATION_ID = 321;
    // Gionee: 20120918 chenrui add for CR00696600 end
    public static final int MESSAGE_FAILED_NOTIFICATION_ID = 789;
    public static final int DOWNLOAD_FAILED_NOTIFICATION_ID = 531;
    public static final int CLASS_ZERO_NOTIFICATION_ID = 5566;
    //Gionee guoyangxu 20120517 add for CR00594172 begin
    private static boolean mMsgSdRing = MmsApp.mGnMessageSdRingSupport;
    //Gionee guoyangxu 20120517 add for CR00594172 end

    // Gionee fengjianyi 2012-09-12 modify for CR00692545 start
    // This must be consistent with the column constants below.
    private static final String[] MMS_STATUS_PROJECTION = new String[] {
        // Aurora xuyong 2014-10-30 modified for privacy feature start
        Mms.THREAD_ID, Mms.DATE, Mms._ID, Mms.SUBJECT, Mms.SUBJECT_CHARSET, GnTelephony.Mms.SIM_ID, "is_privacy" };
        // Aurora xuyong 2014-10-30 modified for privacy feature end

    // This must be consistent with the column constants below.
    private static final String[] SMS_STATUS_PROJECTION = new String[] {
        // Aurora xuyong 2014-10-30 modified for privacy feature start
        Sms.THREAD_ID, Sms.DATE, Sms.ADDRESS, Sms.SUBJECT, Sms.BODY, Sms._ID, GnTelephony.Sms.SIM_ID, "is_privacy" };
        // Aurora xuyong 2014-10-30 modified for privacy feature end
    // Gionee fengjianyi 2012-09-12 modify for CR00692545 end

    // These must be consistent with MMS_STATUS_PROJECTION and
    // SMS_STATUS_PROJECTION.
    private static final int COLUMN_THREAD_ID   = 0;
    private static final int COLUMN_DATE        = 1;
    private static final int COLUMN_MMS_ID      = 2;
    private static final int COLUMN_SMS_ADDRESS = 2;
    private static final int COLUMN_SUBJECT     = 3;
    private static final int COLUMN_SUBJECT_CS  = 4;
    private static final int COLUMN_SMS_BODY    = 4;
    // add this for cmcc dir ui mode
    private static final int COLUMN_SMS_ID      = 5;

    // Gionee fengjianyi 2012-09-12 add for CR00692545 start
    private static final int COLUMN_MMS_SIMID = 5;
    private static final int COLUMN_SMS_SIMID = 6;
    // Gionee fengjianyi 2012-09-12 add for CR00692545 end
    // Aurora xuyong 2014-10-30 added for privacy feature start
    private static final int COLUMN_MMS_PRIVACY = 6;
    private static final int COLUMN_SMS_PRIVACY = 7;
    // Aurora xuyong 2014-10-30 added for privacy feature end
    //gionee gaoj 2012-6-11 added for CR00623393 start
    private static final int WAKE_LOCK_TIMEOUT = 5000;
    //gionee gaoj 2012-6-11 added for CR00623393 end

    //Gionee zengxuanhui 20120809 add for CR00672106/CR00681011 begin
    private static int mReceivedSmsSimId = GnPhone.GEMINI_SIM_1;
    private static final boolean gnGeminiRingtoneSupport = SystemProperties.get("ro.gn.gemini.ringtone.support").equals("yes");
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
    //private static AudioProfileManager mProfileManager = null;
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
    //Gionee zengxuanhui 20120809 add for CR00672106/CR00681011 end

    //GIONEE:wangfei 2012-09-03 add for CR00686851 begin
    public static int sentSmsSimId = GnPhone.GEMINI_SIM_1;
    public static final boolean gnNgmflag = SystemProperties.get("ro.gn.oversea.custom").equals("ITALY_NGM");    
    //GIONEE:wangfei 2012-09-03 add for CR00686851 end
    
    //gionee <gaoj> <2013-06-14> add for CR00826240 begin
    private static final String ACTION_NEW_EVENT = "gionee.infozone.action.NEW_EVENT";
    private static final String EXTRA_PKG = "package";
    private static final String EXTRA_CLS = "wgt_cls";
    //gionee <gaoj> <2013-06-14> add for CR00826240 end

    private static final String NEW_INCOMING_SM_CONSTRAINT =
            "(" + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_INBOX
            + " AND " + Sms.SEEN + " = 0)";

    private static final String NEW_DELIVERY_SM_CONSTRAINT =
        "((" + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_SENT
        + " OR " + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_OUTBOX + ")"
        + " AND " + "(" + Sms.STATUS + " = " + Sms.STATUS_COMPLETE
        + " OR " + Sms.STATUS + " = " + Sms.STATUS_PENDING + "))";

    private static final String NEW_INCOMING_MM_CONSTRAINT =
            "(" + Mms.MESSAGE_BOX + "=" + Mms.MESSAGE_BOX_INBOX
            + " AND " + Mms.SEEN + "=0"
            + " AND (" + Mms.MESSAGE_TYPE + "=" + MESSAGE_TYPE_NOTIFICATION_IND
            + " OR " + Mms.MESSAGE_TYPE + "=" + MESSAGE_TYPE_RETRIEVE_CONF + "))";

    private static final MmsSmsNotificationInfoComparator INFO_COMPARATOR =
            new MmsSmsNotificationInfoComparator();

    private static final Uri UNDELIVERED_URI = Uri.parse("content://mms-sms/undelivered");


    private final static String NOTIFICATION_DELETED_ACTION =
            "com.android.mms.NOTIFICATION_DELETED_ACTION";

    public static class OnDeletedReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                Log.d(TAG, "[MessagingNotification] clear notification: mark all msgs seen");
            }

            Conversation.markAllConversationsAsSeen(context);
        }
    };
    private static OnDeletedReceiver sNotificationDeletedReceiver = new OnDeletedReceiver();
    private static Intent sNotificationOnDeleteIntent;
    private static Handler mToastHandler = new Handler();

    private MessagingNotification() {
    }

    public static void init(Context context) {
        // set up the intent filter for notification deleted action
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NOTIFICATION_DELETED_ACTION);
        context.registerReceiver(sNotificationDeletedReceiver, intentFilter);

        // initialize the notification deleted action
        sNotificationOnDeleteIntent = new Intent(NOTIFICATION_DELETED_ACTION);
    }

    /**
     * Checks to see if there are any "unseen" messages or delivery
     * reports.  Shows the most recent notification if there is one.
     * Does its work and query in a worker thread.
     *
     * @param context the context to use
     */
    public static void nonBlockingUpdateNewMessageIndicator(final Context context,
            final boolean isNew,
            final boolean isStatusMessage) {
        Log.d(TAG, "nonBlockingUpdateNewMessageIndicator, isNew = " + isNew + ", isStatusMessage = " + isStatusMessage) ;
        new Thread(new Runnable() {
            public void run() {
                blockingUpdateNewMessageIndicator(context, isNew, isStatusMessage);
            }
        }).start();
    }
    // Aurora xuyong 2014-09-02 added for bug #8068 start
    public static void nonBlockingUpdateNewMessageIndicator(final Context context,
            final boolean isNew,
            final boolean isStatusMessage, final boolean notNotity) {
        Log.d(TAG, "nonBlockingUpdateNewMessageIndicator, isNew = " + isNew + ", isStatusMessage = " + isStatusMessage) ;
        new Thread(new Runnable() {
            public void run() {
                blockingUpdateNewMessageIndicator(context, isNew, isStatusMessage, notNotity);
            }
        }).start();
    }
    // Aurora xuyong 2014-09-02 added for bug #8068 end
    // Aurora xuyong 2014-08-23 added for bug #7909 start
    public static boolean sIsRejectMsg = false;
    
    public static void setIsRejectMsg(boolean isReject) {
        sIsRejectMsg = isReject;
    }
    // Aurora xuyong 2014-08-23 added for bug #7909 end
    /**
     * Checks to see if there are any "unseen" messages or delivery
     * reports.  Shows the most recent notification if there is one.
     *
     * @param context the context to use
     * @param isNew if notify a new message comes, it should be true, otherwise, false.
     */
    public static void blockingUpdateNewMessageIndicator(Context context, boolean isNew,
            boolean isStatusMessage) {
    // Aurora xuyong 2014-09-02 modified for bug #8068 start
        blockingUpdateNewMessageIndicator(context, isNew, isStatusMessage, true);
    }
    
    public static void blockingUpdateNewMessageIndicator(Context context, boolean isNew,
            boolean isStatusMessage, boolean notify) {
    // Aurora xuyong 2014-09-02 modified for bug #8068 end
       // Aurora xuyong 2014-08-23 added for bug #7909 start
        if (sIsRejectMsg) {
            sIsRejectMsg = false;
            return;
        }
       // Aurora xuyong 2014-08-23 added for bug #7909 end
        Log.d(TAG, "blockingUpdateNewMessageIndicator, isNew = " + isNew);
        // gionee zhouyj 2013-03-21 add for CR00783944 start
        if (MmsApp.mIsSafeModeSupport) {
            Log.i(TAG, "In SaveMode return blockingUpdateNewMessageIndicator");
            return ;
        }
        // gionee zhouyj 2013-03-21 add for CR00783944 end
        SortedSet<MmsSmsNotificationInfo> accumulator =
                new TreeSet<MmsSmsNotificationInfo>(INFO_COMPARATOR);
        MmsSmsDeliveryInfo delivery = null;
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
                accumulator, getMmsNewMessageNotificationInfo(context, threads));
        count += accumulateNotificationInfo(
                accumulator, getSmsNewMessageNotificationInfo(context, threads));
        // Aurora xuyong 2014-11-07 modified for bug #9636 start
        SortedSet<MmsSmsNotificationInfo> accumulatorPri =
                new TreeSet<MmsSmsNotificationInfo>(INFO_COMPARATOR);
        MmsSmsDeliveryInfo deliveryPri = null;
        Set<Long> threadsPri = new HashSet<Long>(4);
        
        
        int countPri = 0;
        countPri += accumulateNotificationInfo(
                accumulatorPri, getPrivacyMmsNewMessageNotificationInfo(context, threadsPri));
        countPri += accumulateNotificationInfo(
                accumulatorPri, getPrivacySmsNewMessageNotificationInfo(context, threadsPri));
        if (count > 0) {
            cancelNotification(context, NOTIFICATION_ID);
            if (!accumulator.isEmpty()) {
                if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    Log.d(TAG, "blockingUpdateNewMessageIndicator: count=" + count +
                            ", isNew=" + isNew);
                }
                // Aurora xuyong 2014-09-02 modified for bug #8068 start
                accumulator.first().deliver(context, isNew, count, threads.size(), notify);
                // Aurora xuyong 2014-09-02 modified for bug #8068 end
            }
    
            // And deals with delivery reports (which use Toasts). It's safe to call in a worker
            // thread because the toast will eventually get posted to a handler.
            delivery = getSmsNewDeliveryInfo(context);
            if (delivery != null) {
                delivery.deliver(context, isStatusMessage);
            }
        // Aurora xuyong 2014-11-07 modified for bug #9636 end
        //gionee gaoj 2012-6-11 added for CR00623393 start
        /*if (MmsApp.mGnMessageSupport && isSilentMode) {
            if (wakelock != null) {
                wakelock.acquire(WAKE_LOCK_TIMEOUT);
            }
        }*/
        //gionee gaoj 2012-6-11 added for CR00623393 end

        //gionee <gaoj> <2013-06-14> add for CR00826240 begin
/*        if (MmsApp.mGnLockScrnSupport && isNew && count > 0 && !MmsApp.mIsSafeModeSupport) {
            Intent newEvent = new Intent(ACTION_NEW_EVENT);
            //package name
            newEvent.putExtra(EXTRA_PKG, "com.android.mms");
            //app widget class name with full path 
            newEvent.putExtra(EXTRA_CLS, "com.android.mms.widget.MmsWidgetProvider");
            context.sendBroadcast(newEvent);
        }*/
        //gionee <gaoj> <2013-06-14> add for CR00826240 end
        // Aurora xuyong 2014-11-07 added for bug #9636 start
        } else {
            cancelNotification(context, NOTIFICATION_ID);
        }
        if(countPri > 0) {
            // Aurora xuyong 2014-11-20 modified for bug #9902 start
            cancelNotification(context, NOTIFICATION_PRIVACY_ID);
            if (!accumulatorPri.isEmpty()) {
                if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    Log.d(TAG, "blockingUpdateNewMessageIndicator: count=" + countPri +
                            ", isNew=" + isNew);
                }
                // Aurora xuyong 2014-09-02 modified for bug #8068 start
                accumulatorPri.first().deliver(context, isNew, countPri, threadsPri.size(), notify);
                // Aurora xuyong 2014-09-02 modified for bug #8068 end
            }
            deliveryPri = getPrivacySmsNewDeliveryInfo(context);
            if (deliveryPri != null) {
                deliveryPri.deliver(context, isStatusMessage);
            }
            // Aurora xuyong 2014-11-20 modified for bug #9902 end
        } else {
            cancelNotification(context, NOTIFICATION_PRIVACY_ID);
        }
        // Aurora xuyong 2014-11-07 added for bug #9636 end
    }

    /**
     * Updates all pending notifications, clearing or updating them as
     * necessary.
     */
    public static void blockingUpdateAllNotifications(final Context context) {
        nonBlockingUpdateNewMessageIndicator(context, false, false);
        updateSendFailedNotification(context);
        updateDownloadFailedNotification(context);
        CBMessagingNotification.updateNewMessageIndicator(context);

        //gionee gaoj 2013-3-11 added for CR00782858 start
        // Aurora liugj 2013-11-07 deleted for hide widget start
        //MmsWidgetProvider.notifyDatasetChanged(context);
          // Aurora liugj 2013-11-07 deleted for hide widget end
        //gionee gaoj 2013-3-11 added for CR00782858 end
    }

    private static final int accumulateNotificationInfo(
            SortedSet set, MmsSmsNotificationInfo info) {
        Log.d(TAG, "accumulateNotificationInfo");
        if (info != null) {
            set.add(info);

            return info.mCount;
        }

        return 0;
    }

    private static final class MmsSmsDeliveryInfo {
        public CharSequence mTicker;
        public long mTimeMillis;

        public MmsSmsDeliveryInfo(CharSequence ticker, long timeMillis) {
            mTicker = ticker;
            mTimeMillis = timeMillis;
        }

        public void deliver(Context context, boolean isStatusMessage) {
            updateDeliveryNotification(
                    context, isStatusMessage, mTicker, mTimeMillis);
        }
    }

    private static final class MmsSmsNotificationInfo {
        public Intent mClickIntent;
        public String mDescription;
        public int mIconResourceId;
        public CharSequence mTicker;
        public long mTimeMillis;
        public String mTitle;
        public int mCount;
        public int mType;
        public Uri mUri;
        public long mPrivacy = 0l;
        public boolean mIsContact = false;

        public MmsSmsNotificationInfo(
                Intent clickIntent, String description, int iconResourceId,
                CharSequence ticker, long timeMillis, String title, int count, int type, Uri uri, boolean isContact) {
            mClickIntent = clickIntent;
            mDescription = description;
            mIconResourceId = iconResourceId;
            mTicker = ticker;
            mTimeMillis = timeMillis;
            mTitle = title;
            mCount = count;
            mType = type;
            mUri = uri;
            mIsContact = isContact;
        }
        
        public MmsSmsNotificationInfo(
                Intent clickIntent, String description, int iconResourceId,
                CharSequence ticker, long timeMillis, String title, int count, int type, Uri uri, boolean isContact, long privacy) {
            mClickIntent = clickIntent;
            mDescription = description;
            mIconResourceId = iconResourceId;
            mTicker = ticker;
            mTimeMillis = timeMillis;
            mTitle = title;
            mCount = count;
            mType = type;
            mUri = uri;
            mIsContact = isContact;
            mPrivacy = privacy;
        }

        // Aurora xuyong 2014-09-02 modified for bug #8068 start
        public void deliver(Context context, boolean isNew, int count, int uniqueThreads, boolean notify) {
        // Aurora xuyong 2014-09-02 modified for bug #8068 end
            Log.d(TAG, "deliver");            
            updateNotification(
                    context, mClickIntent, mDescription, mIconResourceId, isNew,
                    (isNew? mTicker : null), // only display the ticker if the message is new
                    // Aurora xuyong 2014-09-02 modified for bug #8068 start
                    mTimeMillis, mTitle, count, uniqueThreads, mType, mUri, mIsContact, notify, mPrivacy);
                    // Aurora xuyong 2014-09-02 modified for bug #8068 end
        }

        public long getTime() {
            return mTimeMillis;
        }
    }

    private static final class MmsSmsNotificationInfoComparator
            implements Comparator<MmsSmsNotificationInfo> {
        public int compare(
                MmsSmsNotificationInfo info1, MmsSmsNotificationInfo info2) {
            return Long.signum(info2.getTime() - info1.getTime());
        }
    }
    // Aurora xuyong 2014-11-07 added for bug #9636 start
    private static final MmsSmsNotificationInfo getPrivacyMmsNewMessageNotificationInfo(
            Context context, Set<Long> threads) {
        // Aurora xuyong 2015-01-16 added for aurora's new feature start
        if (AuroraPrivacyUtils.getCurrentAccountId() <= 0) {
            return null;
        }
        // Aurora xuyong 2015-01-16 added for aurora's new feature end
        Log.d(TAG, "getPrivacyMmsNewMessageNotificationInfo");
        ContentResolver resolver = context.getContentResolver();
        
        String newComingMmsCons =
                "(" + Mms.MESSAGE_BOX + "=" + Mms.MESSAGE_BOX_INBOX
                + " AND " + Mms.SEEN + "=0"
                + " AND (" + Mms.MESSAGE_TYPE + "=" + MESSAGE_TYPE_NOTIFICATION_IND
                + " OR " + Mms.MESSAGE_TYPE + "=" + MESSAGE_TYPE_RETRIEVE_CONF
                + ") AND is_privacy = " + AuroraPrivacyUtils.getCurrentAccountId() + ")";
        Cursor cursor = SqliteWrapper.query(context, resolver, Mms.CONTENT_URI,
                        MMS_STATUS_PROJECTION, newComingMmsCons,
                        null, Mms.DATE + " desc");
        
        if (cursor == null) {
            return null;
        }

        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            long msgId = cursor.getLong(COLUMN_MMS_ID);
            // Aurora xuyong 2014-10-30 added for privacy feature start
            long privacyValue = cursor.getLong(COLUMN_MMS_PRIVACY);
            // Aurora xuyong 2014-10-30 added for privacy feature end
            Uri msgUri = Mms.CONTENT_URI.buildUpon().appendPath(
                    Long.toString(msgId)).build();
            String address = AddressUtils.getFrom(context, msgUri);
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            Contact contact = null;
            if (MmsApp.sHasPrivacyFeature) {
                    long privacy = Utils.getFristPrivacyId(context, address);
                    contact = Contact.get(address, false, privacy);
            } else {
                contact = Contact.get(address, false);
            }
            // Aurora xuyong 2014-10-23 modified for privacy feature end
            if (contact.getSendToVoicemail()) {
                // don't notify
                return null;
            }

            if(TextUtils.isEmpty(address)){
                address = context.getText(android.R.string.unknownName).toString();
            }
            String subject = getMmsSubject(
                    cursor.getString(COLUMN_SUBJECT), cursor.getInt(COLUMN_SUBJECT_CS));
            long threadId = cursor.getLong(COLUMN_THREAD_ID);
            long timeMillis = cursor.getLong(COLUMN_DATE) * 1000;
            
            //Gionee guoyangxu 2012-10-11 modified for CR00709756 begin
            // Gionee fengjianyi 2012-09-19 add for CR00692545 start
            //Gionee <zhouyj> <2013-05-21> modify for CR00788320 begin
            if (MmsApp.mIsVoiceSupportEnable && VoiceNotificationTransaction.getVoiceConfig()) {
            //Gionee <zhouyj> <2013-05-21> modify for CR00788320 end
                int simid = cursor.getInt(COLUMN_MMS_SIMID);
                int slotId = SIMInfo.getSlotById(context, simid);
                //Gionee <zhouyj> <2013-04-28> modify for CR00802651 begin
                if(slotId == 1){
                    Log.d("fengjianyi", "mIsVoiceSupportEnable new MMS simid=" + GnPhone.GEMINI_SIM_2 + " address=" + address);
                    VoiceNotificationTransaction.setSpeakMessage(GnPhone.GEMINI_SIM_2, 0, address, "mms", null);
                } else if (slotId == 0) {
                    Log.d("fengjianyi", "mIsVoiceSupportEnable new MMS simid=" + GnPhone.GEMINI_SIM_1 + " address=" + address);
                    VoiceNotificationTransaction.setSpeakMessage(GnPhone.GEMINI_SIM_1, 0, address, "mms", null);
                }
                //Gionee <zhouyj> <2013-07-17> modify for CR00834585 begin
                else if (!MmsApp.mGnMultiSimMessage){
                    VoiceNotificationTransaction.setSpeakMessage(slotId, 0, address, "mms", null);
                }
                //Gionee <zhouyj> <2013-07-17> modify for CR00834585 end
                //Gionee <zhouyj> <2013-04-28> modify for CR00802651 end
            }
            // Gionee fengjianyi 2012-09-19 add for CR00692545 end
            //Gionee guoyangxu 2012-10-11 modified for CR00709756 end
            //Gionee zengxuanhui 20120921 add for CR00696079 begin
            // Aurora xuyong 2014-06-20 modified for bug #5203 start
            if (MmsApp.mGnMultiSimMessage) {
            // Aurora xuyong 2014-06-20 modified for bug #5203 end
                int simId = cursor.getInt(COLUMN_MMS_SIMID);
                int slotId = SIMInfo.getSlotById(context, simId);
                Log.d("zengxuanhui", "mms-->simId="+simId+",slotId="+slotId);
                if(slotId == 1){
                    setIncomingSmsSimId(GnPhone.GEMINI_SIM_2);
                }else{
                    setIncomingSmsSimId(GnPhone.GEMINI_SIM_1);
                }              
            }
            //Gionee zengxuanhui 20120921 add for CR00696079 end
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                Log.d(TAG, "getMmsNewMessageNotificationInfo: count=" + cursor.getCount() +
                        ", first addr = " + address + ", thread_id=" + threadId);
            }

             //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mEncryption) {
                int encryption = Conversation.queryThreadId(context, threadId);
                if (encryption == 1) {
                    subject = context.getString(R.string.gn_confirm_encryption);
                }
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
            // Aurora xuyong 2014-10-30 added for privacy feature start
            MmsSmsNotificationInfo info = null;
            if (MmsApp.sHasPrivacyFeature) {
                info = getNewMessageNotificationInfoWithPrivacy(
                        address, subject, context,
                        R.drawable.aurora_stat_notify_sms, null, threadId,
                        timeMillis, cursor.getCount(), 1, msgUri, privacyValue);
            } else {
                info = getNewMessageNotificationInfo(
                        address, subject, context,
                        R.drawable.aurora_stat_notify_sms, null, threadId,
                        timeMillis, cursor.getCount(), 1, msgUri);
            }
            // Aurora xuyong 2014-10-30 added for privacy feature end
            threads.add(threadId);
            while (cursor.moveToNext()) {
                threads.add(cursor.getLong(COLUMN_THREAD_ID));
            }

            return info;
        } finally {
            cursor.close();
        }
    }
    // Aurora xuyong 2014-11-07 added for bug #9636 end
    private static final MmsSmsNotificationInfo getMmsNewMessageNotificationInfo(
            Context context, Set<Long> threads) {
        Log.d(TAG, "getMmsNewMessageNotificationInfo");
        ContentResolver resolver = context.getContentResolver();

        // This query looks like this when logged:
        // I/Database(  147): elapsedTime4Sql|/data/data/com.android.providers.telephony/databases/
        // mmssms.db|0.362 ms|SELECT thread_id, date, _id, sub, sub_cs FROM pdu WHERE ((msg_box=1
        // AND seen=0 AND (m_type=130 OR m_type=132))) ORDER BY date desc
        // Aurora xuyong 2014-10-30 modified for privacy feature start
        Cursor cursor = SqliteWrapper.query(context, resolver, Mms.CONTENT_URI,
                            MMS_STATUS_PROJECTION, NEW_INCOMING_MM_CONSTRAINT,
                            null, Mms.DATE + " desc");
        // Aurora xuyong 2014-10-30 modified for privacy feature end
        if (cursor == null) {
            return null;
        }

        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            long msgId = cursor.getLong(COLUMN_MMS_ID);
            // Aurora xuyong 2014-10-30 added for privacy feature start
            long privacyValue = cursor.getLong(COLUMN_MMS_PRIVACY);
            // Aurora xuyong 2014-10-30 added for privacy feature end
            Uri msgUri = Mms.CONTENT_URI.buildUpon().appendPath(
                    Long.toString(msgId)).build();
            String address = AddressUtils.getFrom(context, msgUri);
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            Contact contact = null;
            if (MmsApp.sHasPrivacyFeature) {
                    long privacy = Utils.getFristPrivacyId(context, address);
                    contact = Contact.get(address, false, privacy);
            } else {
                contact = Contact.get(address, false);
            }
            // Aurora xuyong 2014-10-23 modified for privacy feature end
            if (contact.getSendToVoicemail()) {
                // don't notify
                return null;
            }

            if(TextUtils.isEmpty(address)){
                address = context.getText(android.R.string.unknownName).toString();
            }
            String subject = getMmsSubject(
                    cursor.getString(COLUMN_SUBJECT), cursor.getInt(COLUMN_SUBJECT_CS));
            long threadId = cursor.getLong(COLUMN_THREAD_ID);
            long timeMillis = cursor.getLong(COLUMN_DATE) * 1000;
            
            //Gionee guoyangxu 2012-10-11 modified for CR00709756 begin
            // Gionee fengjianyi 2012-09-19 add for CR00692545 start
            //Gionee <zhouyj> <2013-05-21> modify for CR00788320 begin
            if (MmsApp.mIsVoiceSupportEnable && VoiceNotificationTransaction.getVoiceConfig()) {
            //Gionee <zhouyj> <2013-05-21> modify for CR00788320 end
                int simid = cursor.getInt(COLUMN_MMS_SIMID);
                int slotId = SIMInfo.getSlotById(context, simid);
                //Gionee <zhouyj> <2013-04-28> modify for CR00802651 begin
                if(slotId == 1){
                    Log.d("fengjianyi", "mIsVoiceSupportEnable new MMS simid=" + GnPhone.GEMINI_SIM_2 + " address=" + address);
                    VoiceNotificationTransaction.setSpeakMessage(GnPhone.GEMINI_SIM_2, 0, address, "mms", null);
                } else if (slotId == 0) {
                    Log.d("fengjianyi", "mIsVoiceSupportEnable new MMS simid=" + GnPhone.GEMINI_SIM_1 + " address=" + address);
                    VoiceNotificationTransaction.setSpeakMessage(GnPhone.GEMINI_SIM_1, 0, address, "mms", null);
                }
                //Gionee <zhouyj> <2013-07-17> modify for CR00834585 begin
                else if (!MmsApp.mGnMultiSimMessage){
                    VoiceNotificationTransaction.setSpeakMessage(slotId, 0, address, "mms", null);
                }
                //Gionee <zhouyj> <2013-07-17> modify for CR00834585 end
                //Gionee <zhouyj> <2013-04-28> modify for CR00802651 end
            }
            // Gionee fengjianyi 2012-09-19 add for CR00692545 end
            //Gionee guoyangxu 2012-10-11 modified for CR00709756 end
            //Gionee zengxuanhui 20120921 add for CR00696079 begin
            // Aurora xuyong 2014-06-20 modified for bug #5203 start
            if (MmsApp.mGnMultiSimMessage) {
            // Aurora xuyong 2014-06-20 modified for bug #5203 end
                int simId = cursor.getInt(COLUMN_MMS_SIMID);
                int slotId = SIMInfo.getSlotById(context, simId);
                Log.d("zengxuanhui", "mms-->simId="+simId+",slotId="+slotId);
                if(slotId == 1){
                    setIncomingSmsSimId(GnPhone.GEMINI_SIM_2);
                }else{
                    setIncomingSmsSimId(GnPhone.GEMINI_SIM_1);
                }              
            }
            //Gionee zengxuanhui 20120921 add for CR00696079 end
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                Log.d(TAG, "getMmsNewMessageNotificationInfo: count=" + cursor.getCount() +
                        ", first addr = " + address + ", thread_id=" + threadId);
            }

             //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mEncryption) {
                int encryption = Conversation.queryThreadId(context, threadId);
                if (encryption == 1) {
                    subject = context.getString(R.string.gn_confirm_encryption);
                }
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
            // Aurora xuyong 2014-10-30 added for privacy feature start
            MmsSmsNotificationInfo info = null;
            if (MmsApp.sHasPrivacyFeature) {
                info = getNewMessageNotificationInfoWithPrivacy(
                        address, subject, context,
                        R.drawable.aurora_stat_notify_sms, null, threadId,
                        timeMillis, cursor.getCount(), 1, msgUri, privacyValue);
            } else {
                info = getNewMessageNotificationInfo(
                        address, subject, context,
                        R.drawable.aurora_stat_notify_sms, null, threadId,
                        timeMillis, cursor.getCount(), 1, msgUri);
            }
            // Aurora xuyong 2014-10-30 added for privacy feature end
            threads.add(threadId);
            while (cursor.moveToNext()) {
                threads.add(cursor.getLong(COLUMN_THREAD_ID));
            }

            return info;
        } finally {
            cursor.close();
        }
    }
    // Aurora xuyong 2014-11-07 added for bug #9636 start
    private static final MmsSmsDeliveryInfo getPrivacySmsNewDeliveryInfo(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = sMessageUri;
        if (uri == null){
            uri = Sms.CONTENT_URI;
        }
        String newDeliverySmCons =
                "((" + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_SENT
                + " OR " + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_OUTBOX + ")"
                + " AND " + "(" + Sms.STATUS + " = " + Sms.STATUS_COMPLETE
                + " OR " + Sms.STATUS + " = " + Sms.STATUS_PENDING
                + ") AND is_privacy = " + AuroraPrivacyUtils.getCurrentAccountId() + ")";
        Cursor cursor = SqliteWrapper.query(context, resolver, uri,
                        SMS_STATUS_PROJECTION, newDeliverySmCons,
                        null, Sms.DATE);
        sMessageUri = null;
        if (cursor == null)
            return null;

        try {
            if (!cursor.moveToLast())
            return null;

            String address = cursor.getString(COLUMN_SMS_ADDRESS);
            long timeMillis = 3000;

            //GIONEE:wangfei 2012-09-03 add for CR00686851 begin
            if (gnNgmflag)
            {
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm dd-MM");       
                Date curDate = new Date(System.currentTimeMillis());      
                String curtime = context.getString(R.string.gn_delivery_report_at) + " " + formatter.format(curDate);  
                address = Contact.get(address, true, 0).getName() + " " + curtime;
            }
            //GIONEE:wangfei 2012-09-03 add for CR00686851 end
            // Aurora xuyong 2013-12-17 added for aurora's new feature start
            address = Contact.get(address, true, 0).getName();
            // Aurora xuyong 2013-12-17 added for aurora's new feature end
            return new MmsSmsDeliveryInfo(String.format(
                context.getString(R.string.delivery_toast_body), address),
                timeMillis);

        } finally {
            cursor.close();
        }
    }
    // Aurora xuyong 2014-11-07 added for bug #9636 end

    private static final MmsSmsDeliveryInfo getSmsNewDeliveryInfo(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = sMessageUri;
        if (uri == null){
            uri = Sms.CONTENT_URI;
        }
        // Aurora xuyong 2014-10-30 modified for privacy feature start
        Cursor cursor = SqliteWrapper.query(context, resolver, uri,
                    SMS_STATUS_PROJECTION, NEW_DELIVERY_SM_CONSTRAINT,
                    null, Sms.DATE);
        // Aurora xuyong 2014-10-30 modified for privacy feature end
        sMessageUri = null;
        if (cursor == null)
            return null;

        try {
            if (!cursor.moveToLast())
            return null;

            String address = cursor.getString(COLUMN_SMS_ADDRESS);
            long timeMillis = 3000;

            //GIONEE:wangfei 2012-09-03 add for CR00686851 begin
            if (gnNgmflag)
            {
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm dd-MM");       
                Date curDate = new Date(System.currentTimeMillis());      
                String curtime = context.getString(R.string.gn_delivery_report_at) + " " + formatter.format(curDate);  
                address = Contact.get(address, true, 0).getName() + " " + curtime;
            }
            //GIONEE:wangfei 2012-09-03 add for CR00686851 end
            // Aurora xuyong 2013-12-17 added for aurora's new feature start
            address = Contact.get(address, true, 0).getName();
            // Aurora xuyong 2013-12-17 added for aurora's new feature end
            return new MmsSmsDeliveryInfo(String.format(
                context.getString(R.string.delivery_toast_body), address),
                timeMillis);

        } finally {
            cursor.close();
        }
    }
    // Aurora xuyong 2014-11-07 added for bug #9636 start
    private static final MmsSmsNotificationInfo getPrivacySmsNewMessageNotificationInfo(
            Context context, Set<Long> threads) {
        // Aurora xuyong 2015-01-16 added for aurora's new feature start
        if (AuroraPrivacyUtils.getCurrentAccountId() <= 0) {
            return null;
        }
        // Aurora xuyong 2015-01-16 added for aurora's new feature end
        Log.d(TAG, "getPrivacySmsNewMessageNotificationInfo");            
        ContentResolver resolver = context.getContentResolver();
        String newIncommingSmCons =
                "(" + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_INBOX
                + " AND " + Sms.SEEN + " = 0"
                + " AND is_privacy = " + AuroraPrivacyUtils.getCurrentAccountId() + ")";
         Cursor cursor = SqliteWrapper.query(context, resolver, Sms.CONTENT_URI,
                            SMS_STATUS_PROJECTION, newIncommingSmCons,
                            null, Sms.DATE + " desc");
        if (cursor == null) {
            return null;
        }

        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            // Aurora xuyong 2014-10-30 added for privacy feature start
            long privacyValue = cursor.getLong(COLUMN_SMS_PRIVACY);
            // Aurora xuyong 2014-10-30 added for privacy feature end
            String address = cursor.getString(COLUMN_SMS_ADDRESS);
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            Contact contact = null;
            if (MmsApp.sHasPrivacyFeature) {
                long privacy = Utils.getFristPrivacyId(context, address);
                contact = Contact.get(address, false, privacy);
            } else {
                contact = Contact.get(address, false);
            }
            // Aurora xuyong 2014-10-23 modified for privacy feature end
            if (contact.getSendToVoicemail()) {
                // don't notify
                return null;
            }
  
            if(TextUtils.isEmpty(address)){
                address = context.getText(android.R.string.unknownName).toString();
            }
            String body = cursor.getString(COLUMN_SMS_BODY);
            long threadId = cursor.getLong(COLUMN_THREAD_ID);
            long timeMillis = cursor.getLong(COLUMN_DATE);
            
            //Gionee guoyangxu 2012-10-11 modified for CR00709756 begin
            // Gionee fengjianyi 2012-09-19 add for CR00692545 start
            //Gionee <zhouyj> <2013-05-16> modify for CR00788320 begin
            if (MmsApp.mIsVoiceSupportEnable && VoiceNotificationTransaction.getVoiceConfig()) {
            //Gionee <zhouyj> <2013-05-16> modify for CR00788320 end
                int simid = cursor.getInt(COLUMN_SMS_SIMID);
                int slotId = SIMInfo.getSlotById(context, simid);
                //Gionee <zhouyj> <2013-04-28> modify for CR00802651 begin
                int id = cursor.getInt(COLUMN_SMS_ID);
                if(slotId == 1){
                    Log.d("fengjianyi", "mIsVoiceSupportEnable new SMS simid=" + GnPhone.GEMINI_SIM_2 + " address=" + address);
                    VoiceNotificationTransaction.setSpeakMessage(GnPhone.GEMINI_SIM_2, id, address, "sms", body);
                } else if(slotId == 0){
                    Log.d("fengjianyi", "mIsVoiceSupportEnable new SMS simid=" + GnPhone.GEMINI_SIM_1 + " address=" + address);
                    VoiceNotificationTransaction.setSpeakMessage(GnPhone.GEMINI_SIM_1, id, address, "sms", body);
                } else {
                    Log.d("fengjianyi", "sim slot invalid:slotId = " + slotId);
                    VoiceNotificationTransaction.setSpeakMessage(slotId, id, address, "sms", body);
                }
                //Gionee <zhouyj> <2013-04-28> modify for CR00802651 end
            }
            // Gionee fengjianyi 2012-09-19 add for CR00692545 end
            //Gionee guoyangxu 2012-10-11 modified for CR00709756 end
            //Gionee zengxuanhui 20120921 add for CR00696079 begin
            // Aurora xuyong 2014-06-20 modified for bug #5203 start
            if (MmsApp.mGnMultiSimMessage) {
            // Aurora xuyong 2014-06-20 modified for bug #5203 end
                int simId = cursor.getInt(COLUMN_SMS_SIMID);
                int slotId = SIMInfo.getSlotById(context, simId);
                Log.d("zengxuanhui", "sms-->simId="+simId+",slotId="+slotId);
                if(slotId == 1){
                    setIncomingSmsSimId(GnPhone.GEMINI_SIM_2);
                }else{
                    setIncomingSmsSimId(GnPhone.GEMINI_SIM_1);
                }              
            }
            //Gionee zengxuanhui 20120921 add for CR00696079 end
            //if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) 
            {
                Log.d(TAG, "getSmsNewMessageNotificationInfo: count=" + cursor.getCount() +
                        ", first addr=" + address + ", thread_id=" + threadId);
            }

             //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mEncryption) {
                int encryption = Conversation.queryThreadId(context, threadId);
                if (encryption == 1) {
                    body = context.getString(R.string.gn_confirm_encryption);
                }
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
            // Aurora xuyong 2014-10-30 added for privacy feature start
            MmsSmsNotificationInfo info = null;
            if (MmsApp.sHasPrivacyFeature) {
                info = getNewMessageNotificationInfoWithPrivacy(
                        address, body, context, R.drawable.aurora_stat_notify_sms,
                        null, threadId, timeMillis, cursor.getCount(),
                        // add two args for cmcc dir ui mode
                        0, Sms.CONTENT_URI.buildUpon().appendPath(Long.toString(cursor.getLong(COLUMN_SMS_ID))).build(),
                        privacyValue);
            } else {
                info = getNewMessageNotificationInfo(
                        address, body, context, R.drawable.aurora_stat_notify_sms,
                        null, threadId, timeMillis, cursor.getCount(),
                        // add two args for cmcc dir ui mode
                        0, Sms.CONTENT_URI.buildUpon().appendPath(Long.toString(cursor.getLong(COLUMN_SMS_ID))).build());
            }
            // Aurora xuyong 2014-10-30 added for privacy feature end

            threads.add(threadId);
            while (cursor.moveToNext()) {
                threads.add(cursor.getLong(COLUMN_THREAD_ID));
            }

            return info;
        } finally {
            cursor.close();
        }
    }
    // Aurora xuyong 2014-11-07 added for bug #9636 end
    private static final MmsSmsNotificationInfo getSmsNewMessageNotificationInfo(
            Context context, Set<Long> threads) {
        Log.d(TAG, "getSmsNewMessageNotificationInfo");            
        ContentResolver resolver = context.getContentResolver();
        // Aurora xuyong 2014-10-30 modified for privacy feature start
        Cursor cursor = SqliteWrapper.query(context, resolver, Sms.CONTENT_URI,
                    SMS_STATUS_PROJECTION, NEW_INCOMING_SM_CONSTRAINT,
                    null, Sms.DATE + " desc");
        // Aurora xuyong 2014-10-30 modified for privacy feature end
        if (cursor == null) {
            return null;
        }

        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            // Aurora xuyong 2014-10-30 added for privacy feature start
            long privacyValue = cursor.getLong(COLUMN_SMS_PRIVACY);
            // Aurora xuyong 2014-10-30 added for privacy feature end
            String address = cursor.getString(COLUMN_SMS_ADDRESS);
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            Contact contact = null;
            if (MmsApp.sHasPrivacyFeature) {
                long privacy = Utils.getFristPrivacyId(context, address);
                contact = Contact.get(address, false, privacy);
            } else {
                contact = Contact.get(address, false);
            }
            // Aurora xuyong 2014-10-23 modified for privacy feature end
            if (contact.getSendToVoicemail()) {
                // don't notify
                return null;
            }
  
            if(TextUtils.isEmpty(address)){
                address = context.getText(android.R.string.unknownName).toString();
            }
            String body = cursor.getString(COLUMN_SMS_BODY);
            long threadId = cursor.getLong(COLUMN_THREAD_ID);
            long timeMillis = cursor.getLong(COLUMN_DATE);
            
            //Gionee guoyangxu 2012-10-11 modified for CR00709756 begin
            // Gionee fengjianyi 2012-09-19 add for CR00692545 start
            //Gionee <zhouyj> <2013-05-16> modify for CR00788320 begin
            if (MmsApp.mIsVoiceSupportEnable && VoiceNotificationTransaction.getVoiceConfig()) {
            //Gionee <zhouyj> <2013-05-16> modify for CR00788320 end
                int simid = cursor.getInt(COLUMN_SMS_SIMID);
                int slotId = SIMInfo.getSlotById(context, simid);
                //Gionee <zhouyj> <2013-04-28> modify for CR00802651 begin
                int id = cursor.getInt(COLUMN_SMS_ID);
                if(slotId == 1){
                    Log.d("fengjianyi", "mIsVoiceSupportEnable new SMS simid=" + GnPhone.GEMINI_SIM_2 + " address=" + address);
                    VoiceNotificationTransaction.setSpeakMessage(GnPhone.GEMINI_SIM_2, id, address, "sms", body);
                } else if(slotId == 0){
                    Log.d("fengjianyi", "mIsVoiceSupportEnable new SMS simid=" + GnPhone.GEMINI_SIM_1 + " address=" + address);
                    VoiceNotificationTransaction.setSpeakMessage(GnPhone.GEMINI_SIM_1, id, address, "sms", body);
                } else {
                    Log.d("fengjianyi", "sim slot invalid:slotId = " + slotId);
                    VoiceNotificationTransaction.setSpeakMessage(slotId, id, address, "sms", body);
                }
                //Gionee <zhouyj> <2013-04-28> modify for CR00802651 end
            }
            // Gionee fengjianyi 2012-09-19 add for CR00692545 end
            //Gionee guoyangxu 2012-10-11 modified for CR00709756 end
            //Gionee zengxuanhui 20120921 add for CR00696079 begin
            // Aurora xuyong 2014-06-20 modified for bug #5203 start
            if (MmsApp.mGnMultiSimMessage) {
            // Aurora xuyong 2014-06-20 modified for bug #5203 end
                int simId = cursor.getInt(COLUMN_SMS_SIMID);
                int slotId = SIMInfo.getSlotById(context, simId);
                Log.d("zengxuanhui", "sms-->simId="+simId+",slotId="+slotId);
                if(slotId == 1){
                    setIncomingSmsSimId(GnPhone.GEMINI_SIM_2);
                }else{
                    setIncomingSmsSimId(GnPhone.GEMINI_SIM_1);
                }              
            }
            //Gionee zengxuanhui 20120921 add for CR00696079 end
            //if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) 
            {
                Log.d(TAG, "getSmsNewMessageNotificationInfo: count=" + cursor.getCount() +
                        ", first addr=" + address + ", thread_id=" + threadId);
            }

             //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mEncryption) {
                int encryption = Conversation.queryThreadId(context, threadId);
                if (encryption == 1) {
                    body = context.getString(R.string.gn_confirm_encryption);
                }
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
            // Aurora xuyong 2014-10-30 added for privacy feature start
            MmsSmsNotificationInfo info = null;
            if (MmsApp.sHasPrivacyFeature) {
                info = getNewMessageNotificationInfoWithPrivacy(
                        address, body, context, R.drawable.aurora_stat_notify_sms,
                        null, threadId, timeMillis, cursor.getCount(),
                        // add two args for cmcc dir ui mode
                        0, Sms.CONTENT_URI.buildUpon().appendPath(Long.toString(cursor.getLong(COLUMN_SMS_ID))).build(),
                        privacyValue);
            } else {
                info = getNewMessageNotificationInfo(
                        address, body, context, R.drawable.aurora_stat_notify_sms,
                        null, threadId, timeMillis, cursor.getCount(),
                        // add two args for cmcc dir ui mode
                        0, Sms.CONTENT_URI.buildUpon().appendPath(Long.toString(cursor.getLong(COLUMN_SMS_ID))).build());
            }
            // Aurora xuyong 2014-10-30 added for privacy feature end

            threads.add(threadId);
            while (cursor.moveToNext()) {
                threads.add(cursor.getLong(COLUMN_THREAD_ID));
            }

            return info;
        } finally {
            cursor.close();
        }
    }

    private static final MmsSmsNotificationInfo getNewMessageNotificationInfo(
            String address,
            String body,
            Context context,
            int iconResourceId,
            String subject,
            long threadId,
            long timeMillis,
            int count,
            int type,
            Uri uri) {
        Log.d(TAG, "getNewMessageNotificationInfo");
        Intent clickIntent = ComposeMessageActivity.createIntent(context, threadId);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);

    	Contact contact = Contact.get(address, true, Utils.getFristPrivacyId(context, address));
        String senderInfo = buildTickerMessage(
        		contact, null, null).toString();
        String senderInfoName = senderInfo.substring(
                0, senderInfo.length() - 2);
        CharSequence ticker = buildTickerMessage(
        		contact, subject, body);

        return new MmsSmsNotificationInfo(
                clickIntent, body, iconResourceId, ticker, timeMillis,
                senderInfoName, count, type, uri, !TextUtils.isEmpty(contact.getNameOnly()));
    }
    // Aurora xuyong 2014-10-30 added for privacy feature start
    private static final MmsSmsNotificationInfo getNewMessageNotificationInfoWithPrivacy(
            String address,
            String body,
            Context context,
            int iconResourceId,
            String subject,
            long threadId,
            long timeMillis,
            int count,
            int type,
            Uri uri,
            long privacy) {
        Log.d(TAG, "getNewMessageNotificationInfo");
        Intent clickIntent = ComposeMessageActivity.createIntent(context, threadId);
        clickIntent.putExtra("is_privacy", privacy);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);

    	Contact contact = Contact.get(address, true, Utils.getFristPrivacyId(context, address));
        String senderInfo = buildTickerMessage(
        		contact, null, null).toString();
        String senderInfoName = senderInfo.substring(
                0, senderInfo.length() - 2);
        CharSequence ticker = buildTickerMessage(
        		contact, subject, body);

        return new MmsSmsNotificationInfo(
                clickIntent, body, iconResourceId, ticker, timeMillis,
                senderInfoName, count, type, uri, !TextUtils.isEmpty(contact.getNameOnly()), privacy);
    }
    // Aurora xuyong 2014-10-30 added for privacy feature end
    public static void cancelNotification(Context context, int notificationId) {
        Log.d(TAG, "cancelNotification, notificationId:" + notificationId);
        NotificationManager nm = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        nm.cancel(notificationId);
    }

    private static void updateDeliveryNotification(final Context context,
                                                   boolean isStatusMessage,
                                                   final CharSequence message,
                                                   final long timeMillis) {
        if (!isStatusMessage) {
            return;
        }

        //gionee gaoj 2012-8-24 modified for CR00680478 start
//        if (!MessagingPreferenceActivity.getNotificationEnabled(context)) {
//            return;
//        }
        //gionee gaoj 2012-8-24 modified for CR00680478 end

        mToastHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(context, message, (int)timeMillis).show();
            }
        });
        
        //GIONEE:wangfei 2012-09-03 add for CR00686851 begin
        if(gnNgmflag == true){
            int delicon = R.drawable.aurora_stat_notify_sms;
            String deltitle = context.getString(R.string.delivery_report_activity);
            Notification notification = new Notification(delicon, message, System.currentTimeMillis()); //System.currentTimeMillis()
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        
            // Update the notification.
            notification.setLatestEventInfo(context, deltitle, message, pendingIntent);
            
            AudioManager audioManager =
                (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

            SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(context);
            //Gionee <guoyx> <2013-04-18> modified for CR00796598 begin
            // Aurora liugj 2013-11-14 modified for Ringtone bug start
            // Aurora xuyong 2014-11-12 modified for aurora's new feature start
            Uri ringtoneStr = getRingtoneUri();
            // Aurora xuyong 2014-11-12 modified for aurora's new feature end
            if (gnGeminiRingtoneSupport == true && sentSmsSimId == GnPhone.GEMINI_SIM_2) {
                // Aurora xuyong 2014-11-12 modified for aurora's new feature start
                ringtoneStr = getRingtoneUri();
                // Aurora xuyong 2014-11-12 modified for aurora's new feature end
            }
            // Aurora liugj 2013-11-14 modified for Ringtone bug end

            if (mMsgSdRing){
                applySoundToNotification(context, notification, ringtoneStr, sp);
            } else if (ringtoneStr != null) {
                notification.sound = ringtoneStr;
            } else {
                notification.sound = null;
                Log.i(TAG, "Get actual default ringtone uri is null, that mean silence sound.");
            }
            //Gionee <guoyx> <2013-04-18> modified for CR00796598 end
            // processNotificationSound(context, notification, ringtone);
            processNotificationSound(context, notification);

            notification.defaults |= Notification.DEFAULT_LIGHTS;
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

            nm.notify(130, notification);
        }
        
        //GIONEE:wangfei 2012-09-03 add for CR00686851 end

    }
    // Aurora xuyong 2014-11-12 added for aurora's new feature start
    private static Uri getRingtoneUri() {
        String ringtoneUri = Settings.System.getString(MmsApp.getApplication().getApplicationContext().getContentResolver(), "sms_sound");
        if (ringtoneUri != null) {
            return Uri.parse(ringtoneUri);
        } else {
            return RingtoneManager.getActualDefaultRingtoneUri(MmsApp.getApplication().getApplicationContext(), 
                    RingtoneManager.TYPE_NOTIFICATION);
        }
    }
    // Aurora xuyong 2014-11-12 added for aurora's new feature end
    // Gionee: 20120918 chenrui add for CR00696600 begin
    public static void updateMissMsgNotification(Context context) {
        Log.d(TAG, "updateMissMsgNotification");
        Log.d("crtest", "updateMissMsgNotification");

        if (!MessagingPreferenceActivity.getNotificationEnabled(context)) {
            return;
        }

        //Notification notification = new Notification(iconRes, ticker, timeMillis);
        Notification notification = new Notification();

        SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(context);
        //Gionee <guoyx> <2013-04-18> modified for CR00796598 begin
         // Aurora liugj 2013-11-14 modified for Ringtone bug start
        // Aurora xuyong 2014-11-12 modified for aurora's new feature start
        Uri ringtoneStr = getRingtoneUri();
        // Aurora xuyong 2014-11-12 modified for aurora's new feature end
        if (gnGeminiRingtoneSupport == true && sentSmsSimId == GnPhone.GEMINI_SIM_2) {
            // Aurora xuyong 2014-11-12 modified for aurora's new feature start
            ringtoneStr = getRingtoneUri();
            // Aurora xuyong 2014-11-12 modified for aurora's new feature end
        }
         // Aurora liugj 2013-11-14 modified for Ringtone bug end
        
        if (mMsgSdRing){
            applySoundToNotification(context, notification, ringtoneStr, sp);
        } else if (ringtoneStr != null) {
            notification.sound = ringtoneStr;
        } else {
            notification.sound = null;
            Log.i(TAG, "Get actual default ringtone uri is null, that mean silence sound.");
        }
        //Gionee <guoyx> <2013-04-18> modified for CR00796598 end
        //processNotificationSound(context, notification);
        processMissMsgNotificationSound(context, notification);

        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        // Aurora xuyong 2014-08-13 added for 1+ notification start
        notification.ledARGB=0xff00ff00;
        notification.ledOnMS=500;
        notification.ledOffMS=2100;
        // Aurora xuyong 2014-08-13 added for 1+ notification end
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(MISS_MSG_NOTIFICATION_ID, notification);
    }
    // Gionee: 20120918 chenrui add for CR00696600 end
    // Aurora xuyong 2014-06-19 added for multisim feature start
    private static int setProperSImIconId(Context context, int slotId) {
        int simId = -1;
        switch(slotId) {
            case GnPhone.GEMINI_SIM_1:
                SIMInfo info1 = SIMInfo.getSIMInfoBySlot(context, GnPhone.GEMINI_SIM_1);
                if (info1 != null) {
                    simId = (int)(info1.mSimId);
                }
                break;
            case GnPhone.GEMINI_SIM_2:
                SIMInfo info2 = SIMInfo.getSIMInfoBySlot(context, GnPhone.GEMINI_SIM_2);
                  if (info2 != null) {
                      simId = (int)(info2.mSimId);
                  }
                break;
        }  
        int drawableId = MessageUtils.getSimNotifiIcon(context, simId);
        return drawableId;
    }
    // Aurora xuyong 2014-06-19 added for multisim feature end
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
            int type,
            Uri uri,
            boolean isContact) {
    // Aurora xuyong 2014-09-02 modified for bug #8068 start
        updateNotification(context, clickIntent, description, iconRes, isNew, ticker,
                timeMillis, title, messageCount, uniqueThreadCount, type, uri, isContact, true, 0l);
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
            int type,
            Uri uri,
            boolean isContact,
            boolean notify,
            long privacy) {
     // Aurora xuyong 2014-09-02 modified for bug #8068 end
        Log.d(TAG, "updateNotification");
        // Aurora xuyong 2016-03-10 added for bug #20942 start
        final String address = title;
        int index = Utils.isNotificationMsg(context, address, description) ? 1 : 0;
        context.getSharedPreferences(ConvFragment.CONV_NOTIFICATION, Activity.MODE_PRIVATE).edit().putInt(ConvFragment.CONV_NOTIFICATION_INDEX,
                index).commit();
        // Aurora xuyong 2016-03-10 added for bug #20942 end
        if (!MessagingPreferenceActivity.getNotificationEnabled(context)) {
            return;
        }
        // Aurora xuyong 2015-01-16 modified for aurora's new feature start
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(title).setContentText(description).setSmallIcon(iconRes).setTicker(ticker)
                .setWhen(timeMillis);
        
        Notification notification;
        // Aurora xuyong 2014-08-13 added for 1+ notification start
        if(MmsApp.mProductModel != null && MmsApp.mProductModel.contains("N1")){ //n1
        	// Aurora yudingmin 2015-04-30 added for ni notification start
            if(isContact){
                mBuilder.setLights(0xFF008A00, 260, 260);
                Log.i(TAG, "notifycation ni product is contact.");
            } else {
                mBuilder.setLights(0xFF00008A, 520, 520);
                Log.i(TAG, "notifycation ni product is not contact.");
            }
            notification = mBuilder.build();
            // Aurora yudingmin 2015-04-30 end for ni notification start
        } else {
        	notification = mBuilder.build();
            notification.ledARGB=0xff00ff00;
            notification.ledOnMS=500;
            notification.ledOffMS=2100;
            notification.defaults |= Notification.DEFAULT_LIGHTS;
        }
        // Aurora xuyong 2014-08-13 added for 1+ notification end
        // Aurora xuyong 2015-01-16 modified for aurora's new feature end
        // If we have more than one unique thread, change the title (which would
        // normally be the contact who sent the message) to a generic one that
        // makes sense for multiple senders, and change the Intent to take the
        // user to the conversation list instead of the specific thread.
        if (uniqueThreadCount > 1) {
            title = context.getString(R.string.notification_multiple_title);
            clickIntent = new Intent(Intent.ACTION_MAIN);

            clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            clickIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                    // Aurora liugj 2013-11-08 modified for aurora's new feature start
                clickIntent.setClassName("com.android.mms", "com.aurora.mms.ui.AuroraConvListActivity");
                    // Aurora liugj 2013-11-08 modified for aurora's new feature end
            } else {
              //gionee gaoj 2012-4-10 added for CR00555790 end
                // Aurora liugj 2013-11-08 modified for aurora's new feature start
            clickIntent.setClassName("com.android.mms", "com.aurora.mms.ui.AuroraConvListActivity");
                // Aurora liugj 2013-11-08 modified for aurora's new feature end
            //gionee gaoj 2012-4-10 added for CR00555790 start
            }
           //gionee gaoj 2012-4-10 added for CR00555790 end
            //clickIntent.setType("vnd.android-dir/mms-sms");
            // Aurora xuyong 2016-03-10 added for bug #20942 start
            Bundle bundle = new Bundle();
            bundle.putInt("notification_index", index);
            clickIntent.putExtras(bundle);
            // Aurora xuyong 2016-03-10 added for bug #20942 end
        }

        // add for cmcc dir ui begin
        //in dir mode, we commonly go into inbox activity
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
        // add for cmcc dir ui end
        
        // If there is more than one message, change the description (which
        // would normally be a snippet of the individual message text) to
        // a string indicating how many "unseen" messages there are.
        if (messageCount > 1) {
            description = context.getString(R.string.notification_multiple,
                    Integer.toString(messageCount));
        }
        // add else for cmcc dir ui mode
        else {
            if ((type == 0) && MmsConfig.getMmsDirMode()) {
                // open the only sms directly
                clickIntent.putExtra("msg_type", 1);
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
        // Aurora xuyong 2014-06-19 modified for multisim feature start
        // Aurora xuyong 2014-06-20 modified for bug #5203 start
        // Aurora xuyong 2014-11-21 deleted for bug #9970 start
        //if (MmsApp.mGnMultiSimMessage && uniqueThreadCount == 1) {
        // Aurora xuyong 2014-11-21 deleted for bug #9970 end
        // Aurora xuyong 2014-06-20 modified for bug #5203 end
            // Aurora xuyong 2014-11-05 modified for bug #9638 start
            RemoteViews remoteViews = null;
            if (privacy > 0) {
                remoteViews = new RemoteViews(context.getPackageName(), R.layout.aurora_multi_sim_privacy_notification);
            } else {
                remoteViews = new RemoteViews(context.getPackageName(), R.layout.aurora_multi_sim_notification);
            }
            // Aurora xuyong 2014-11-05 modified for bug #9638 end
            // Aurora xuyong 2014-09-10 modified for bug #8638 start
            if (messageCount == 1) {
                // Aurora xuyong 2014-11-21 modified for bug #9970 start
                if (MmsApp.mGnMultiSimMessage) {
                    remoteViews.setImageViewResource(R.id.aurora_sim_thumb, setProperSImIconId(context, mReceivedSmsSimId));
                } else {
                    remoteViews.setImageViewResource(R.id.aurora_sim_thumb, -1);
                }
                // Aurora xuyong 2014-11-21 modified for bug #9970 end
            }
            // Aurora xuyong 2014-09-10 modified for bug #8638 end
            remoteViews.setTextViewText(R.id.aurora_address, title);
            remoteViews.setTextViewText(R.id.aurora_body, description);
            remoteViews.setTextViewText(R.id.aurora_date, MessageUtils.formatAuroraTimeStampString(context, timeMillis, false));
            notification.contentView = remoteViews;
            notification.contentIntent = pendingIntent;
        // Aurora xuyong 2014-11-21 deleted for bug #9970 start
        /*} else {
            notification.setLatestEventInfo(context, title, description, pendingIntent);
        }*/
        // Aurora xuyong 2014-11-21 deleted for bug #9970 end
        // Aurora xuyong 2014-06-19 modified for multisim feature end
        if (isNew) {
            SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(context);
            //Gionee <guoyx> <2013-04-18> modified for CR00796598 begin
              // Aurora liugj 2013-11-14 modified for Ringtone bug start
            // Aurora xuyong 2014-11-12 modified for aurora's new feature start
            Uri ringtoneStr = getRingtoneUri();
            // Aurora xuyong 2014-11-12 modified for aurora's new feature end
            //Gionee zengxuanhui 20120809 add for CR00672106 begin
            if (gnGeminiRingtoneSupport && mReceivedSmsSimId == GnPhone.GEMINI_SIM_2) {
                // Aurora xuyong 2014-11-12 modified for aurora's new feature start
                ringtoneStr = getRingtoneUri();
                // Aurora xuyong 2014-11-12 modified for aurora's new feature end
             // Aurora liugj 2013-11-14 modified for Ringtone bug end

            }
            //Gionee zengxuanhui 20120809 add for CR00672106 end
            //Gionee <guoyx> <2013-04-18> modified for CR00796598 end
            //Gionee guoyangxu 20120507 modified for CR00594172 begin
            // Gionee fengjianyi 2012-09-19 modify for CR00692545 start
            /*
            if (mMsgSdRing){
                applySoundToNotification(context, notification, ringtoneStr, sp);
            } else {
                notification.sound = TextUtils.isEmpty(ringtoneStr) ? null : Uri
                        .parse(ringtoneStr);
            }
            */
             //Gionee <zhouyj> <2013-04-28> modify for CR00802651 begin
            if (MmsApp.mGnVoiceHelperSupport) {
                PowerManager pManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                Log.i(VoiceHelperReceiver.TAG, "updateNotification   MmsApp.mIsVoiceSupportEnable = " + MmsApp.mIsVoiceSupportEnable 
                        + ", VoiceNotificationTransaction.getVoiceConfig() = " + VoiceNotificationTransaction.getVoiceConfig() 
                        + ", pManager.isScreenOn() = " + pManager.isScreenOn() + ", VoiceHelperReceiver.isVoiceHelperError() = "
                        + VoiceHelperReceiver.isVoiceHelperError());
                if (MmsApp.mIsVoiceSupportEnable && VoiceNotificationTransaction.getVoiceConfig() && 
                        !pManager.isScreenOn() && !VoiceHelperReceiver.isVoiceHelperError()) {
                    notification.sound = null;
                    VoiceNotificationTransaction.speakRemindInfo();
                } else if (mMsgSdRing){
                    applySoundToNotification(context, notification, ringtoneStr, sp);
                } else if (ringtoneStr != null){
                    notification.sound = ringtoneStr;
                } else {
                    notification.sound = null;
                    Log.i(TAG, "Get actual default ringtone uri is null, that mean silence sound.");
                }
                VoiceHelperReceiver.resetVoiceHelperFlag();
            } else {
                //Gionee <guoyx> <2013-04-18> modified for CR00796598 begin
                if (MmsApp.mIsVoiceSupportEnable && VoiceNotificationTransaction.getVoiceConfig()) {
                    // Gionee fengjianyi 2012-09-22 add for CR00699769 start
                    notification.sound = null;
                    // Gionee fengjianyi 2012-09-22 add for CR00699769 end
                    VoiceNotificationTransaction.speakRemindInfo();
                } else if (mMsgSdRing){
                    applySoundToNotification(context, notification, ringtoneStr, sp);
                } else if (ringtoneStr != null){
                    notification.sound = ringtoneStr;
                } else {
                    notification.sound = null;
                    Log.i(TAG, "Get actual default ringtone uri is null, that mean silence sound.");
                }
                //Gionee <guoyx> <2013-04-18> modified for CR00796598 end
            }
             //Gionee <zhouyj> <2013-04-28> modify for CR00802651 end
            // Gionee fengjianyi 2012-09-19 modify for CR00692545 end
            // Uri ringtone = TextUtils.isEmpty(ringtoneStr) ? null : Uri.parse(ringtoneStr);
            // processNotificationSound(context, notification, ringtone);
            processNotificationSound(context, notification);
            //Gionee guoyangxu 20120507 modified for CR00594172 end
            // add for CTA 5.3.3
            NotificationPlus notiPlus = new NotificationPlus.Builder(context)
                    .setTitle(context.getString(R.string.new_message))
                    .setMessage(context.getString(R.string.notification_multiple, Integer.toString(messageCount)))
                    .setPositiveButton(context.getString(R.string.view), pendingIntent)
                    .create();
            NotificationManagerPlus.notify(1, notiPlus);
        }

        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        // Aurora xuyong 2014-09-02 modified for bug #8068 start
        if (!notify) {
            notification.vibrate = null;
            notification.sound = null;
        }
        // Aurora xuyong 2014-09-02 modified for bug #8068 end
        // set up delete intent
        //Gionee <guoyx> <2013-06-21> add for CR00819360 begin
        if (sNotificationOnDeleteIntent == null) {
            sNotificationOnDeleteIntent = new Intent(NOTIFICATION_DELETED_ACTION); 
        }
        //Gionee <guoyx> <2013-06-21> add for CR00819360 end
        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, sNotificationOnDeleteIntent, 0);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (privacy > 0) {
            nm.notify(NOTIFICATION_PRIVACY_ID, notification);
        } else {
            nm.notify(NOTIFICATION_ID, notification);
        }
    }

  protected static void processNotificationSound(Context context, Notification notification, Uri ringtone) {
        int state = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (FeatureOption.MTK_BRAZIL_CUSTOMIZATION_CLARO && state != TelephonyManager.CALL_STATE_IDLE) {
            /* in call or in ringing */
            
            /* ringtone on, and into music mode */
            notification.audioStreamType = AudioManager.STREAM_MUSIC;
        }
        if (audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION) == AudioManager.VIBRATE_SETTING_ON) {
            /* vibrate on */
            // Aurora xuyong 2014-01-08 modified for notification's vibrate start
            //notification.defaults |= Notification.DEFAULT_VIBRATE;
            notification.vibrate = new long[]{0, 400, 100, 200, 100, 200};
            // Aurora xuyong 2014-01-08 modified for notification's vibrate end
        }
        
        notification.sound = ringtone;
        
    }
  
  //Gionee guoyangxu 20120507 add for CR00594172 begin
  protected static void processNotificationSound(Context context, Notification notification) {
      int state = MmsApp.getApplication().getTelephonyManager().getCallState();
      AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
      //Gionee <guoyx> <2013-04-24> modified for CR00799298 begin
    //Gionee guoyangxu 20120813 modified for CR00664253 begin
      if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
          Log.i(TAG, "in phone call ring the Notification.DEFAULT_SOUND");
          notification.audioStreamType = AudioManager.STREAM_VOICE_CALL;
          notification.defaults |= Notification.DEFAULT_SOUND;
// below is another way to ring the tone.          
//          ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
//          toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2);
      } else if (FeatureOption.MTK_BRAZIL_CUSTOMIZATION_CLARO && state != TelephonyManager.CALL_STATE_IDLE) {
          /* in call or in ringing */
          
          /* ringtone on, and into music mode */
          notification.audioStreamType = AudioManager.STREAM_MUSIC;
      }
      //Gionee guoyangxu 20120813 modified for CR00664253 end
      //Gionee <guoyx> <2013-04-24> modified for CR00799298 end
      // Aurora xuyong 2013-12-06 modified for vibrate bug start
      if (audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION) == AudioManager.VIBRATE_SETTING_ON
              || Settings.System.getInt(context.getContentResolver(),Settings.System.VIBRATE_WHEN_RINGING, 0) != 0) {
      // Aurora xuyong 2013-12-06 modified for vibrate bug end
          /* vibrate on */
          // Aurora xuyong 2014-01-08 modified for notification's vibrate start
          //notification.defaults |= Notification.DEFAULT_VIBRATE;
          notification.vibrate = new long[]{0, 400, 100, 200, 100, 200};
          // Aurora xuyong 2014-01-08 modified for notification's vibrate end
      }
      
  }
  //Gionee guoyangxu 20120507 add for CR00594172 end

    // Gionee: 20120918 chenrui add for CR00696600 begin
    protected static void processMissMsgNotificationSound(Context context, Notification notification) {
        int state = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION) == AudioManager.VIBRATE_SETTING_ON) {
            if (TelephonyManager.CALL_STATE_IDLE == state) {
                /* vibrate on */
                // Aurora xuyong 2014-01-08 modified for notification's vibrate start
                //notification.defaults |= Notification.DEFAULT_VIBRATE;
                notification.vibrate = new long[]{0, 400, 100, 200, 100, 200};
                // Aurora xuyong 2014-01-08 modified for notification's vibrate end
            }
        }

        if (state != TelephonyManager.CALL_STATE_IDLE) {
            notification.sound = null;
        }
    }
    // Gionee: 20120918 chenrui add for CR00696600 end

    protected static CharSequence buildTickerMessage(
    		Contact contact, String subject, String body) {
        // Aurora xuyong 2014-11-10 modified for bug #9720 start
        String displayAddress = contact.getName();
        // Aurora xuyong 2014-11-10 modified for bug #9720 end

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

    private static String getMmsSubject(String sub, int charset) {
        return TextUtils.isEmpty(sub) ? ""
                : new EncodedStringValue(charset, PduPersister.getBytes(sub)).getString();
    }

    public static void notifyDownloadFailed(Context context, long threadId) {
        Log.d(TAG, "notifyDownloadFailed");                    
        notifyFailed(context, true, threadId, false);
    }

    public static void notifySendFailed(Context context) {
        Log.d(TAG, "notifySendFailed");
        notifyFailed(context, false, 0, false);
    }

    public static void notifySendFailed(Context context, boolean noisy) {
        Log.d(TAG, "notifySendFailed, noisy = " + noisy);
        notifyFailed(context, false, 0, noisy);
    }

    private static void notifyFailed(Context context, boolean isDownload, long threadId,
                                     boolean noisy) {
        // TODO factor out common code for creating notifications
        boolean enabled = MessagingPreferenceActivity.getNotificationEnabled(context);

        if (!enabled) {
            return;
        }

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Strategy:
        // a. If there is a single failure notification, tapping on the notification goes
        //    to the compose view.
        // b. If there are two failure it stays in the thread view. Selecting one undelivered
        //    thread will dismiss one undelivered notification but will still display the
        //    notification.If you select the 2nd undelivered one it will dismiss the notification.

        long[] msgThreadId = {0, 1};    // Dummy initial values, just to initialize the memory
        int totalFailedCount = getUndeliveredMessageCount(context, msgThreadId);
        // Aurora xuyong 2013-10-23 deleted for bug #135 start
        /*if (totalFailedCount == 0 && !isDownload) {
            return;
        }*/
        // Aurora xuyong 2013-10-23 deleted for bug #135 end
        // The getUndeliveredMessageCount method puts a non-zero value in msgThreadId[1] if all
        // failures are from the same thread.
        // If isDownload is true, we're dealing with 1 specific failure; therefore "all failed" are
        // indeed in the same thread since there's only 1.
        boolean allFailedInSameThread = (msgThreadId[1] != 0) || isDownload;

        Intent failedIntent;
        Notification notification = new Notification();
        String title;
        String description;
        if (totalFailedCount > 1) {
            description = context.getString(R.string.notification_failed_multiple,
                    Integer.toString(totalFailedCount));
            title = context.getString(R.string.notification_failed_multiple_title);
        } else {
            title = isDownload ?
                        context.getString(R.string.message_download_failed_title) :
                        context.getString(R.string.message_send_failed_title);

            description = context.getString(R.string.message_failed_body);
        }

        if (allFailedInSameThread) {
                // Aurora liugj 2013-11-18 modified for bug-693 start
            //failedIntent = new Intent(context, ComposeMessageActivity.class);
            if (isDownload) {
                    failedIntent = new Intent(context, ComposeMessageActivity.class);
                // When isDownload is true, the valid threadId is passed into this function.
                failedIntent.putExtra("failed_download_flag", true);
                failedIntent.putExtra("thread_id", threadId);
            } else {
               /* threadId = msgThreadId[0];
                failedIntent.putExtra("undelivered_flag", true);*/
                failedIntent = new Intent(context, AuroraConvListActivity.class);
            }
            //failedIntent.putExtra("thread_id", threadId);
              // Aurora liugj 2013-11-18 modified for bug-693 end
        } else {
            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                    // Aurora liugj 2013-11-06 modified for aurora's new feature start
                failedIntent = new Intent(context, AuroraConvListActivity.class);
                    // Aurora liugj 2013-11-06 modified for aurora's new feature end
            } else {
             //gionee gaoj 2012-4-10 added for CR00555790 end
            failedIntent = new Intent(context, AuroraConvListActivity.class);
            //gionee gaoj 2012-4-10 added for CR00555790 start
            }
             //gionee gaoj 2012-4-10 added for CR00555790 end
        }
        //add for cmcc dir ui mode begin
        if (MmsConfig.getMmsDirMode()) {
            if (isDownload) {
                failedIntent = new Intent(Intent.ACTION_MAIN);
                failedIntent.setFlags(//Intent.FLAG_ACTIVITY_NEW_TASK
                     Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    // Aurora liugj 2013-11-06 modified for aurora's new feature start
                //failedIntent.putExtra("floderview_key", 0);// need show inbox
                failedIntent.setClassName("com.android.mms", "com.aurora.mms.ui.AuroraConvListActivity");
                    // Aurora liugj 2013-11-06 modified for aurora's new feature end
            } else {
                failedIntent = new Intent(Intent.ACTION_MAIN);
                failedIntent.setFlags(//Intent.FLAG_ACTIVITY_NEW_TASK
                     Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    // Aurora liugj 2013-11-06 modified for aurora's new feature start
                //failedIntent.putExtra("floderview_key", 1);// need show outbox
                failedIntent.setClassName("com.android.mms", "com.aurora.mms.ui.AuroraConvListActivity");
                    // Aurora liugj 2013-11-06 modified for aurora's new feature end
            }
        } else {
        //add for cmcc dir ui mode end
            failedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, failedIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.icon = R.drawable.aurora_stat_notify_sms_failed;

        notification.tickerText = title;
        // Aurora xuyong 2013-11-18 modified for bug #693 start
        notification.setLatestEventInfo(context, title, description, pendingIntent);
        // Aurora xuyong 2013-11-18 modified for bug #693 end
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        if (noisy) {
            //Gionee guoyangxu 20120704 modified for CR00632240 begin
            notification.defaults |= Notification.DEFAULT_SOUND;
            processNotificationSound(context, notification);
            //Gionee guoyangxu 20120704 modified for CR00632240 end
        }

        if (isDownload) {
            nm.notify(DOWNLOAD_FAILED_NOTIFICATION_ID, notification);
        } else {
            nm.notify(MESSAGE_FAILED_NOTIFICATION_ID, notification);
        }
    }

    /**
     * Query the DB and return the number of undelivered messages (total for both SMS and MMS)
     * @param context The context
     * @param threadIdResult A container to put the result in, according to the following rules:
     *  threadIdResult[0] contains the thread id of the first message.
     *  threadIdResult[1] is nonzero if the thread ids of all the messages are the same.
     *  You can pass in null for threadIdResult.
     *  You can pass in a threadIdResult of size 1 to avoid the comparison of each thread id.
     */
    private static int getUndeliveredMessageCount(Context context, long[] threadIdResult) {
        Log.d(TAG, "getUndeliveredMessageCount");
        String where = "read=0";
        //add for cmcc dir mode begin
        if (MmsConfig.getMmsDirMode()) {
            where = "read=0 and seen=0";
        }
        //add for cmcc dir mode end
        Cursor undeliveredCursor = SqliteWrapper.query(context, context.getContentResolver(),
                UNDELIVERED_URI, new String[] { Mms.THREAD_ID }, where, null, null);
        if (undeliveredCursor == null) {
            return 0;
        }
        int count = undeliveredCursor.getCount();
        try {
            if (threadIdResult != null && undeliveredCursor.moveToFirst()) {
                threadIdResult[0] = undeliveredCursor.getLong(0);

                if (threadIdResult.length >= 2) {
                    // Test to see if all the undelivered messages belong to the same thread.
                    long firstId = threadIdResult[0];
                    while (undeliveredCursor.moveToNext()) {
                        if (undeliveredCursor.getLong(0) != firstId) {
                            firstId = 0;
                            break;
                        }
                    }
                    threadIdResult[1] = firstId;    // non-zero if all ids are the same
                }
            }
        } finally {
            undeliveredCursor.close();
        }
        return count;
    }

    public static void updateSendFailedNotification(Context context) {
        Log.d(TAG, "updateSendFailedNotification");
        if (getUndeliveredMessageCount(context, null) < 1) {
            cancelNotification(context, MESSAGE_FAILED_NOTIFICATION_ID);
        } else {
            notifySendFailed(context);      // rebuild and adjust the message count if necessary.
        }
    }

    /**
     *  If all the undelivered messages belong to "threadId", cancel the notification.
     */
    public static void updateSendFailedNotificationForThread(Context context, long threadId) {
        long[] msgThreadId = {0, 0};
        if (getUndeliveredMessageCount(context, msgThreadId) > 0
                && msgThreadId[0] == threadId
                && msgThreadId[1] != 0) {
            cancelNotification(context, MESSAGE_FAILED_NOTIFICATION_ID);
        }
    }

    private static int getDownloadFailedMessageCount(Context context) {
        // Look for any messages in the MMS Inbox that are of the type
        // NOTIFICATION_IND (i.e. not already downloaded) and in the
        // permanent failure state.  If there are none, cancel any
        // failed download notification.
        Cursor c = SqliteWrapper.query(context, context.getContentResolver(),
                Mms.Inbox.CONTENT_URI, null,
                Mms.MESSAGE_TYPE + "=" +
                    String.valueOf(PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) +
                " AND " + Mms.STATUS + "=" +
                    String.valueOf(DownloadManager.STATE_PERMANENT_FAILURE),
                null, null);
        if (c == null) {
            return 0;
        }
        int count = c.getCount();
        c.close();
        return count;
    }

    public static void updateDownloadFailedNotification(Context context) {
        if (getDownloadFailedMessageCount(context) < 1) {
            cancelNotification(context, DOWNLOAD_FAILED_NOTIFICATION_ID);
        }
    }

    public static boolean isFailedToDeliver(Intent intent) {
        return (intent != null) && intent.getBooleanExtra("undelivered_flag", false);
    }

    public static boolean isFailedToDownload(Intent intent) {
        return (intent != null) && intent.getBooleanExtra("failed_download_flag", false);
    }

    public static boolean notifyClassZeroMessage(Context context, String address) {
        Log.d(TAG, "notifyClassZeroMessage");
        SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(context);

        boolean enabled = sp.getBoolean(MessagingPreferenceActivity.NOTIFICATION_ENABLED, true);
        Log.d(TAG, "notifyClassZeroMessage, enabled = "+enabled);        
        if (!enabled) {
            return false;
        }

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification();
        //Gionee <guoyx> <2013-04-18> modified for CR00796598 begin
         // Aurora liugj 2013-11-14 modified for Ringtone bug start
        // Aurora xuyong 2014-11-12 modified for aurora's new feature start
        Uri ringtoneStr = getRingtoneUri();
        // Aurora xuyong 2014-11-12 modified for aurora's new feature end 
        //Gionee zengxuanhui 20120809 add for CR00672106 begin
        if (gnGeminiRingtoneSupport && mReceivedSmsSimId == GnPhone.GEMINI_SIM_2) {
            // Aurora xuyong 2014-11-12 modified for aurora's new feature start
            ringtoneStr = getRingtoneUri();
            // Aurora xuyong 2014-11-12 modified for aurora's new feature end
         // Aurora liugj 2013-11-14 modified for Ringtone bug end
        }
        //Gionee zengxuanhui 20120809 add for CR00672106 end    
        
        if (mMsgSdRing){
            applySoundToNotification(context, notification, ringtoneStr, sp);
        } else if (ringtoneStr != null){
            notification.sound = ringtoneStr;
        } else {
            notification.sound = null;
            Log.i(TAG, "Get actual default ringtone uri is null, that mean silence sound.");
        }
        //Gionee <guoyx> <2013-04-18> modified for CR00796598 end
        // processNotificationSound(context, notification, ringtone);
        processNotificationSound(context, notification);
        //Gionee guoyangxu 20120507 modified for CR00594172 end

        notification.tickerText = address;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.ledARGB = 0xff00ff00;
        notification.ledOnMS = 500;
        notification.ledOffMS = 2000;
        nm.notify(CLASS_ZERO_NOTIFICATION_ID, notification);
        return true;
    }
    //Gionee zengxuanhui 20120825 add for CR00678840 begin
    public static boolean isRingtoneExist(Context context, Uri uri) {
        try {
            AssetFileDescriptor fd = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            if (fd == null) {
                return false;
            } else {
                try {
                    fd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
    //Gionee zengxuanhui 20120825 add for CR00678840 end
    //Gionee <guoyx> <2013-04-18> modified for CR00796598 begin
    //Gionee guoyangxu 20120507 add for CR00594172 begin
    private static void applySoundToNotification(Context context, Notification notification, 
            Uri ringtoneUri, SharedPreferences sp){
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        //mProfileManager = (AudioProfileManager)context.getSystemService(Context.AUDIOPROFILE_SERVICE);
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        //Gionee zengxuanhui 20120806 modify for CR00664477 begin
        if (ringtoneUri != null && !ringtoneUri.equals("silence")){
        //Gionee zengxuanhui 20120806 modify for CR00664477 end
            Log.d(TAG, "applySoundToNotification ringtoneUri:" + ringtoneUri.toString());
            //Gionee zengxuanhui 20120825 modify for CR00678840 begin
            boolean validSoundUri = isRingtoneExist(context, ringtoneUri);
            //Gionee zengxuanhui 20120825 modify for CR00678840 end
            if (validSoundUri){
                Log.d(TAG, "applySoundToNotification has uri set ringtoneUri");
                notification.sound = ringtoneUri;
            }else {
                //Gionee guoyangxu 20120621 modified for CR00625902 begin
                //
                // Gionee xiongjiaxin 2012-6-20 modified for CR00625680 start
                //Gionee <guoyx> <2013-08-05> modify for CR00845647 begin
                //Gionee zengxuanhui 20120825 modify for CR00678840 begin
                // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
                /*if(mProfileManager != null){
                    notification.sound = mProfileManager.getDefaultRingtone(AudioProfileManager.TYPE_MMS);
                    Log.i(TAG, "applySoundToNotification not exist, get MMS DefaultRingtone");
                }else{*/
                // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
                    notification.defaults |= Notification.DEFAULT_SOUND;
                    Log.i(TAG, "applySoundToNotification not exist, use Notification.DEFAULT_SOUND");
                // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
                //}
                // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
                //Gionee zengxuanhui 20120825 modify for CR00678840 end
                //Gionee <guoyx> <2013-08-05> modify for CR00845647 end
                if (ConfigConstantUtils.hasSdcardShared()) {
                    Log.i(TAG, "sd card is shared.");
                } else {
                    // Gionee xiongjiaxin 2012-7-5 added for CR00633171 start
                    // Log.i(TAG,"update default notification ring to the sms.");
                    // sp.edit().putString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE,
                    // Settings.System.DEFAULT_NOTIFICATION_URI.toString()).commit();
                    // Gionee xiongjiaxin 2012-7-5 added for CR00633171 end
                }
                // Gionee xiongjiaxin 2012-6-20 modified for CR00625680 end
                //Gionee guoyangxu 20120621 modified for CR00625902 end
                
            }
        } else {
            Log.d(TAG, "applySoundToNotification ringtoneUri is null, slience sound.");
            //Gionee zengxuanhui 20120806 modify for CR00664477 begin
            if(ringtoneUri == null){
//                //Gionee zengxuanhui 20120825 modify for CR00681011/CR00718095 begin
//                Uri rUri = null;
//                if(mProfileManager != null && gnGeminiRingtoneSupport){
//                    if(mReceivedSmsSimId == GnPhone.GEMINI_SIM_1){
//                        rUri = mProfileManager.getRingtoneUri(mProfileManager.getActiveProfileKey(), 
//                                RingtoneManager.TYPE_MMS);
//                    }else{
//                        rUri = mProfileManager.getRingtoneUri(mProfileManager.getActiveProfileKey(), 
//                                RingtoneManager.TYPE_MMS2);
//                    }
//                    if(rUri == null){
//                        Log.d(TAG, "applySoundToNotification get profile DB ringtone null,get default!");
//                        rUri = mProfileManager.getDefaultRingtone(AudioProfileManager.TYPE_MMS);
//                    }
//                }else{
//                    rUri = Settings.System.DEFAULT_MMS_URI;
//                }
//                notification.sound = rUri;
//                if(rUri == null){
//                    return;
//                }
//                if(gnGeminiRingtoneSupport && (mReceivedSmsSimId == GnPhone.GEMINI_SIM_2)){
//                    sp.edit().putString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE2,
//                            rUri.toString()).commit();
//                }else{
//                    sp.edit().putString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE,
//                            rUri.toString()).commit();
//                }
//                //Gionee zengxuanhui 20120825 modify for CR00681011/CR00718095 end
                notification.sound = null;
            }else{
                notification.sound = null;
                if(gnGeminiRingtoneSupport && (mReceivedSmsSimId == GnPhone.GEMINI_SIM_2)){
                    sp.edit().putString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE2,
                            "silence").commit();
                }else{
                    sp.edit().putString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE,
                            "silence").commit();
                }
            }
            //Gionee zengxuanhui 20120806 modify for CR00664477 end
        }
    }
    //Gionee guoyangxu 20120507 add for CR00594172 end
    //Gionee <guoyx> <2013-04-18> modified for CR00796598 end
    
    //Gionee zengxuanhui 20120809 add for CR00672106 begin
    public static void setIncomingSmsSimId(int simId)  {
        Log.d(TAG, "simId="+simId);
        if (simId == GnPhone.GEMINI_SIM_1 || simId == GnPhone.GEMINI_SIM_2) {
            mReceivedSmsSimId = simId;
        }
    }
    //Gionee zengxuanhui 20120809 add for CR00672106 end

    //GIONEE:wangfei 2012-09-03 add for CR00686851 begin
    public static void setOutgoingSmsSimId(int simId)  {
        if (simId == GnPhone.GEMINI_SIM_1 || simId == GnPhone.GEMINI_SIM_2) {
            sentSmsSimId = simId;
        }
    }
    //GIONEE:wangfei 2012-09-03 add for CR00686851 end
}
