package com.android.phone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Settings;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
import android.telephony.ServiceState;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.CallerInfoAsyncQuery;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.TelephonyCapabilities;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.IccCard;
import android.provider.Telephony.SIMInfo;

import android.app.NotificationManagerPlus;
import android.app.NotificationPlus;

import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.phone.CallFeaturesSetting;
import com.android.phone.Constants;
import com.mediatek.phone.PhoneFeatureConstants.FeatureOption;
import com.mediatek.phone.Worker;
import com.mediatek.telephony.TelephonyManagerEx;

public class MtkNotificationMgr extends NotificationMgr {
    private static final String LOG_TAG = "MtkNotificationMgr";
    
    
    private boolean CALL_FORWARD_INDICATOR_SIM1 = false; /* 0 : disable, 0x01 : enable */ 
    private boolean CALL_FORWARD_INDICATOR_SIM2 = false; /* 0 : disable, 0x01 : enable */ 
    static final int CALL_FORWARD_NOTIFICATION_EX = 10;
    static final int VOICEMAIL_NOTIFICATION_2 = 20;
    
    /**
     * Private constructor (this is a singleton).
     * @see init()
     */
    private MtkNotificationMgr(PhoneGlobals app) {
        super(app);
    }
    
    /* package */ static NotificationMgr init(PhoneGlobals app) {
        synchronized (MtkNotificationMgr.class) {
            if (sInstance == null) {
                sInstance = new MtkNotificationMgr(app);
                // Update the notifications that need to be touched at startup.
                sInstance.updateNotificationsAtStartup();
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            return sInstance;
        }
    }
    
    /**
     * Shows the "data disconnected due to roaming" notification, which
     * appears when you lose data connectivity because you're roaming and
     * you have the "data roaming" feature turned off.
     */
    /* package */ void showDataDisconnectedRoaming(int simId) {
        if (DBG) log("showDataDisconnectedRoaming()...");
        Intent intent = null;
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {
            intent = new Intent();
            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.gemini.SimDataRoamingSettings"));
        }
        else
        {
            intent = new Intent(mContext,
                    com.android.phone.MobileNetworkSettings.class);
        }

        Notification notification = new Notification(
                android.R.drawable.stat_sys_warning, // icon
                null, // tickerText
                System.currentTimeMillis());
        notification.setLatestEventInfo(
                mContext, // Context
                mContext.getString(R.string.roaming), // expandedTitle
                mContext.getString(R.string.roaming_reenable_message), // expandedText
                PendingIntent.getActivity(mContext, 0, intent, 0)); // contentIntent

        mNotificationManager.notify(
                DATA_DISCONNECTED_ROAMING_NOTIFICATION,
                notification);
    }
    
    void resetMissedCallNumber() {
        // reset the number of missed calls to 0, not need to cancel notification yet.
        mNumberMissedCalls = 0;
        notifyMissedCallCount();
        resetNewCallsFlag();
    }
    
    private void notifyMissedCallCount() {
        // send broadcast message to inform launcher and others
        
        Runnable r = new Runnable() {
            public void run() {
                if (DBG) log("current thread = " + Thread.currentThread());
                if (DBG) log("notifyMissedCallCount start......");
                Intent newIntent = new Intent(Intent.MTK_ACTION_UNREAD_CHANGED);
                newIntent.putExtra(Intent.MTK_EXTRA_UNREAD_NUMBER, mNumberMissedCalls);
                newIntent.putExtra(Intent.MTK_EXTRA_UNREAD_COMPONENT,
                                   new ComponentName(Constants.CONTACTS_PACKAGE,
                                                     Constants.CONTACTS_DIALTACTS_ACTIVITY));
                mContext.sendBroadcast(newIntent);
                android.provider.Settings.System.putInt(mContext.getContentResolver(),
                                                        Constants.CONTACTS_UNREAD_KEY,
                                                        Integer.valueOf(mNumberMissedCalls));
                if (DBG) log("notifyMissedCallCount end......");
            }
        };
        
        Worker w = Worker.getWorkerInstance();
        if (w != null) {
            w.prepair();
            w.postJob(r);
        } else {
            Intent newIntent = new Intent(Intent.MTK_ACTION_UNREAD_CHANGED);
            newIntent.putExtra(Intent.MTK_EXTRA_UNREAD_NUMBER, mNumberMissedCalls);
            newIntent.putExtra(Intent.MTK_EXTRA_UNREAD_COMPONENT,
                               new ComponentName(Constants.CONTACTS_PACKAGE,
                                                 Constants.CONTACTS_DIALTACTS_ACTIVITY));
            mContext.sendBroadcast(newIntent);
            android.provider.Settings.System.putInt(mContext.getContentResolver(),
                                                    Constants.CONTACTS_UNREAD_KEY,
                                                    Integer.valueOf(mNumberMissedCalls));
        }
    }
    
    private void resetNewCallsFlag() {
        // Mark all "new" missed calls as not new anymore
        StringBuilder where = new StringBuilder("type=");
        where.append(Calls.MISSED_TYPE);
        where.append(" AND new=1");
        
        ContentValues values = new ContentValues(1);
        values.put(Calls.NEW, "0");
        mContext.getContentResolver().update(Calls.CONTENT_URI,
            values, where.toString(), null);
    }
    
    /**
     * Updates the message waiting indicator (voicemail) notification.
     *
     * @param visible true if there are messages waiting
     */
    /* package */ void updateMwi(boolean visible, int simId) {
        if (DBG) log("updateMwi(): " + visible);
        if (DBG) log("updateMwi(): " + visible + "simId:" + simId);

        Notification notification = null;
        Intent intent = null;
        PendingIntent pendingIntent = null;

        if (visible) {
            int resId = android.R.drawable.stat_notify_voicemail;

            // This Notification can get a lot fancier once we have more
            // information about the current voicemail messages.
            // (For example, the current voicemail system can't tell
            // us the caller-id or timestamp of a message, or tell us the
            // message count.)

            // But for now, the UI is ultra-simple: if the MWI indication
            // is supposed to be visible, just show a single generic
            // notification.

            //String notificationTitle = mContext.getString(R.string.notification_voicemail_title);
            
            String notificationTitle = mContext.getString(R.string.notification_voicemail);

            String vmNumber;

            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                vmNumber = TelephonyManagerEx.getDefault().getVoiceMailNumber(simId);
            } else {
                vmNumber = mPhone.getVoiceMailNumber();
            }

            if (DBG) log("- got vm number: '" + vmNumber + "'");

            // Watch out: vmNumber may be null, for two possible reasons:
            //
            //   (1) This phone really has no voicemail number
            //
            //   (2) This phone *does* have a voicemail number, but
            //       the SIM isn't ready yet.
            //
            // Case (2) *does* happen in practice if you have voicemail
            // messages when the device first boots: we get an MWI
            // notification as soon as we register on the network, but the
            // SIM hasn't finished loading yet.
            //
            // So handle case (2) by retrying the lookup after a short
            // delay.

            boolean iccRecordloaded = false;

            if (FeatureOption.MTK_GEMINI_SUPPORT == true)
            {
                iccRecordloaded = ((GeminiPhone)mPhone).getIccRecordsLoadedGemini(simId);
            }
            else
            {
                iccRecordloaded = mPhone.getIccRecordsLoaded();
            }

            if ((vmNumber == null) && !iccRecordloaded) {
                if (DBG) log("- Null vm number: SIM records not loaded (yet)...");

                // TODO: rather than retrying after an arbitrary delay, it
                // would be cleaner to instead just wait for a
                // SIM_RECORDS_LOADED notification.
                // (Unfortunately right now there's no convenient way to
                // get that notification in phone app code.  We'd first
                // want to add a call like registerForSimRecordsLoaded()
                // to Phone.java and GSMPhone.java, and *then* we could
                // listen for that in the CallNotifier class.)

                // Limit the number of retries (in case the SIM is broken
                // or missing and can *never* load successfully.)
                if (mVmNumberRetriesRemaining-- > 0) {
                    if (DBG) log("  - Retrying in " + VM_NUMBER_RETRY_DELAY_MILLIS + " msec...");
                    ((MtkCallNotifier)mApp.notifier).sendMwiChangedDelayed(VM_NUMBER_RETRY_DELAY_MILLIS, simId);
                    return;
                } else {
                    Log.w(LOG_TAG, "NotificationMgr.updateMwi: getVoiceMailNumber() failed after "
                          + MAX_VM_NUMBER_RETRIES + " retries; giving up.");
                    // ...and continue with vmNumber==null, just as if the
                    // phone had no VM number set up in the first place.
                }
            }

          /*if (TelephonyCapabilities.supportsVoiceMessageCount(mPhone)) {
                int vmCount = mPhone.getVoiceMessageCount();
                String titleFormat = mContext.getString(R.string.notification_voicemail_title_count);
                notificationTitle = String.format(titleFormat, vmCount);
            }*/

            String notificationText;
            notificationText = mContext.getString(R.string.notification_voicemail_title);
/*            if (TextUtils.isEmpty(vmNumber)) {
                notificationText = mContext.getString(
                        R.string.notification_voicemail_no_vm_number);
            } else {
                notificationText = String.format(
                        mContext.getString(R.string.notification_voicemail_text_format),
                        PhoneNumberFormatUtilEx.formatNumber(vmNumber)PhoneNumberUtils.formatNumber(vmNumber));
            }*/

            /*intent = new Intent(Intent.ACTION_CALL,
                    Uri.fromParts(Constants.SCHEME_VOICEMAIL, "", null));*/
            intent = new Intent();
            if (!TextUtils.isEmpty(vmNumber)) {
                intent.putExtra("voicemail_number", vmNumber);
            } else {
                intent.putExtra("voicemail_number", "");
            }

            if (simId == Phone.GEMINI_SIM_2) {
                intent.setAction("VoiceMailSIM2");
            } else {
                intent.setAction("VoiceMailSIM");
            }
            /*Intent intent = new Intent(Intent.ACTION_CALL,
                    Uri.fromParts(Constants.SCHEME_VOICEMAIL, "", null));*/

            intent.setComponent(new ComponentName("com.android.phone", "com.mediatek.phone.VoicemailDialog"));
            log("updateMwi(): new intent CALL, simId: " + simId);
            intent.putExtra(Phone.GEMINI_SIM_ID_KEY, simId);

            pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // !!!!! Need consider again, both google and MTK modify below
            /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            Uri ringtoneUri;
            String uriString = prefs.getString(
                    CallFeaturesSetting.BUTTON_VOICEMAIL_NOTIFICATION_RINGTONE_KEY, null);
            if (!TextUtils.isEmpty(uriString)) {
                ringtoneUri = Uri.parse(uriString);
            } else {
                ringtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI;
            }

            Notification.Builder builder = new Notification.Builder(mContext);
            builder.setSmallIcon(resId)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setContentIntent(pendingIntent)
                    .setSound(ringtoneUri);
            notification = builder.getNotification();

            String vibrateWhen = prefs.getString(
                    CallFeaturesSetting.BUTTON_VOICEMAIL_NOTIFICATION_VIBRATE_WHEN_KEY, "never");
            boolean vibrateAlways = vibrateWhen.equals("always");
            boolean vibrateSilent = vibrateWhen.equals("silent");
            AudioManager audioManager =
                    (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            boolean nowSilent = audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
            if (vibrateAlways || (vibrateSilent && nowSilent)) {
                notification.defaults |= Notification.DEFAULT_VIBRATE;
            }

            notification.flags |= Notification.FLAG_NO_CLEAR;*/

            notification = new Notification(
                    resId,  // icon
                    null, // tickerText
                    System.currentTimeMillis()  // Show the time the MWI notification came in,
                                                // since we don't know the actual time of the
                                                // most recent voicemail message
                    );
            notification.setLatestEventInfo(
                    mContext,  // context
                    notificationTitle,  // contentTitle
                    notificationText,  // contentText
                    pendingIntent  // contentIntent
                    );
            
            //Tell notification manager that we want to display the sim info
            notification.simId = getSimId(simId);
            notification.simInfoType = 3;
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            
            notification.flags |= Notification.FLAG_NO_CLEAR;
            configureLedNotification(notification);
            if (simId == Phone.GEMINI_SIM_2) {
                mNotificationManager.notify(VOICEMAIL_NOTIFICATION_2, notification);
            } else {
            mNotificationManager.notify(VOICEMAIL_NOTIFICATION, notification);
            }
        } else {
            if (simId == Phone.GEMINI_SIM_2) {
                mNotificationManager.cancel(VOICEMAIL_NOTIFICATION_2);
            } else {
                mNotificationManager.cancel(VOICEMAIL_NOTIFICATION);
            }
        }
    }

    /**
     * Updates the message call forwarding indicator notification.
     *
     * @param visible true if there are messages waiting
     */
    /* package */ void updateCfi(boolean visible, int simId) {
        if (DBG) log("updateCfi(): " + visible + "simId:" + simId);
        int notifyId = CALL_FORWARD_NOTIFICATION;
        //Notification notification = null;
        
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {
            if (simId == Phone.GEMINI_SIM_1)
            {
                CALL_FORWARD_INDICATOR_SIM1 = true;
            }
            else if (simId == Phone.GEMINI_SIM_2)
            {
                CALL_FORWARD_INDICATOR_SIM2 = true;
                notifyId = CALL_FORWARD_NOTIFICATION_EX;
            }
            log("CALL_FORWARD_INDICATOR - sim1: " + CALL_FORWARD_INDICATOR_SIM1 + ",sim2:" + CALL_FORWARD_INDICATOR_SIM2); 
        }
            
        if (visible) {
            // If Unconditional Call Forwarding (forward all calls) for VOICE
            // is enabled, just show a notification.  We'll default to expanded
            // view for now, so the there is less confusion about the icon.  If
            // it is deemed too weird to have CF indications as expanded views,
            // then we'll flip the flag back.

            // TODO: We may want to take a look to see if the notification can
            // display the target to forward calls to.  This will require some
            // effort though, since there are multiple layers of messages that
            // will need to propagate that information.

            Notification notification;
            final boolean showExpandedNotification = true;
            if (showExpandedNotification) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //For enhancement, we always take user to the call feature setting
                intent.setClassName(Constants.PHONE_PACKAGE, CallFeaturesSetting.class.getName());

                intent.putExtra(Phone.GEMINI_SIM_ID_KEY, simId);
          
                notification = new Notification(
                        android.R.drawable.stat_sys_phone_call_forward,  // icon
                        null, // tickerText
                        0); // The "timestamp" of this notification is meaningless;
                            // we only care about whether CFI is currently on or not.
                notification.setLatestEventInfo(
                        mContext, // context
                        mContext.getString(R.string.labelCF), // expandedTitle
                        mContext.getString(R.string.sum_cfu_enabled_indicator), // expandedText
                        PendingIntent.getActivity(mContext, 0, intent, 0)); // contentIntent
            } else {
                notification = new Notification(
                        android.R.drawable.stat_sys_phone_call_forward,  // icon
                        null,  // tickerText
                        System.currentTimeMillis()  // when
                        );
            }

            notification.flags |= Notification.FLAG_ONGOING_EVENT;  // also implies FLAG_NO_CLEAR
            notification.simId = getSimId(simId);
            notification.simInfoType = 3;
            mNotificationManager.notify(
                    notifyId,
                    notification);
        } else {
            mNotificationManager.cancel(notifyId);
        }
    }
    
    private long getSimId(int slot) {
        SIMInfo info = com.mediatek.phone.SIMInfoWrapper.getDefault().getSimInfoBySlot(slot);//SIMInfo.getSIMInfoBySlot(PhoneApp.getInstance().getApplicationContext(), slot);
        if (info != null) {
            return info.mSimId;
        }
        
        return -1;
    }
    
    
    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
    
    
}