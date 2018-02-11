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

 /* Gionee: 20120918 chenrui add for CR00696600 begin */

package com.android.mms.transaction;

import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION;
import static android.provider.Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import android.os.SystemClock;
import android.provider.Settings;
import gionee.provider.GnSettings;
import android.content.AsyncQueryHandler;
import android.app.AlarmManager;
import android.app.PendingIntent;


/**
 * This service essentially plays the role of a "worker thread", allowing us to store
 * incoming messages to the database, update notifications, etc. without blocking the
 * main thread that AlertMissMsgReceiver runs on.
 */
public class AlertMissMsgReceiverService extends Service {
    private static final String TAG = "AlertMissMsgReceiverService";

    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    
    public static final String ALERT_MISS_MSG_ACTION =
        "com.android.mms.transaction.ALERT_MISS_MSG";

    private static int mMissedMsgCount = 0;

    /**
     * if not auto stop the alert, set ALERT_TIMES = 0
     */
    private static final int ALERT_TIMES = 0;
    private static int mAlertCounter = 0;

    private final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
    private final Uri MMS_CONTENT_URI = Uri.parse("content://mms");
    private final Uri WAPPUSH_CONTENT_URI = Uri.parse("content://wappush");

    private MissMsgQueryHandler mHandler = null;

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {    
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        /**
         * Handle incoming transaction requests.
         * The incoming requests are initiated by the MMSC Server or by the MMS Client itself.
         */
        @Override
        public void handleMessage(Message msg) {
            int serviceId = msg.arg1;
            Intent intent = (Intent)msg.obj;
            
            Log.v(TAG, "handleMessage serviceId: " + serviceId + " intent: " + intent);
            
            if (intent != null) {
                String action = intent.getAction();

                Log.v(TAG, "handleMessage action: " + action);

                if (ACTION_BOOT_COMPLETED.equals(action)) {
                    handleBootCompleted();
                } else if (SMS_RECEIVED_ACTION.equals(action) || WAP_PUSH_RECEIVED_ACTION.equals(action)) {
                    handleMsgReceived();
                } else if (ALERT_MISS_MSG_ACTION.equals(action)) {
                    handleAlertMissMsg();
                }
            }
            
            // NOTE: We MUST not call stopSelf() directly, since we need to
            // make sure the wake lock acquired by AlertReceiver is released.
            AlertMissMsgReceiver.finishStartingService(AlertMissMsgReceiverService.this, serviceId);
        }
    }

    private void handleAlertMissMsg() {
        Context context = getApplicationContext();
        mHandler = new MissMsgQueryHandler(context);
        mHandler.startQuery();

        boolean alertMissMsg = Settings.System.getInt(context.getContentResolver(), 
                GnSettings.System.ALERT_MISS_MSG, 0) != 0;

        if (mMissedMsgCount > 0 && alertMissMsg && (0 == ALERT_TIMES || mAlertCounter++ < ALERT_TIMES)) {
            MessagingNotification.updateMissMsgNotification(context);
        } else {
            cancelAlertAlarm();
        }
    }

    private void handleMsgReceived() {
        setAlertAlarm();
    }

    private void handleBootCompleted() {
        setAlertAlarm();
    }

    private void setAlertAlarm() {
        Context context = getApplicationContext();
        mAlertCounter = 0;
        enableAlert(context);
    }

    private void cancelAlertAlarm() {
        Context context = getApplicationContext();
        disableAlert(context);
    }

    private void enableAlert(Context context) {
        
        AlarmManager am = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(ALERT_MISS_MSG_ACTION);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                context, 0, intent, 0);

        int type = AlarmManager.ELAPSED_REALTIME_WAKEUP;
        
        int alertMissMsgInterval = Settings.System.getInt(context.getContentResolver(), 
                GnSettings.System.ALERT_MISS_MSG_INTERVAL, 1);
        
        int alertFreq = alertMissMsgInterval;

        long interval = alertFreq * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + interval;
        am.setInexactRepeating(type, triggerAtTime, interval, alarmIntent);
    }

    private void disableAlert(Context context) {
        AlarmManager am = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(ALERT_MISS_MSG_ACTION);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                context, 0, intent, 0);

        am.cancel(alarmIntent);
    }

    class MissMsgQueryHandler {

        Context mContext = null;
        int smsCount = 0;
        int mmsCount = 0;
        int pushCount = 0;

        private MissMsgQueryHandler(Context context) {
            mContext = context;
        }

        public void startQuery() {

            Cursor cursor = null;
            ContentResolver cr = mContext.getContentResolver();
            try {
                cursor = cr.query(SMS_CONTENT_URI, null,
                        "read=0", null, null);
                if (cursor != null && cursor.getCount() >= 0) {
                    smsCount = cursor.getCount();
                    Log.v(TAG, "query smsCount: " + smsCount);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            try {
                cursor = cr.query(MMS_CONTENT_URI, null,
                        "read=0", null, null);
                if (cursor != null && cursor.getCount() >= 0) {
                    mmsCount = cursor.getCount();
                    Log.v(TAG, "query mmsCount: " + mmsCount);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            try {
                cursor = cr.query(WAPPUSH_CONTENT_URI, null,
                        "read=0", null, null);
                if (cursor != null && cursor.getCount() >= 0) {
                    pushCount = cursor.getCount();
                    Log.v(TAG, "query pushCount: " + pushCount);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }
            
            mMissedMsgCount = smsCount + mmsCount + pushCount;
        }

    }

}

/* Gionee: 20120918 chenrui add for CR00696600 end */
