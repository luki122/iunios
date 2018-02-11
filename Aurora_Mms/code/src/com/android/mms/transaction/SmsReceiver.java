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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony.Sms.Intents;
import android.util.Log;
import android.os.PowerManager;
import com.gionee.internal.telephony.GnPhone;
import com.android.mms.MmsApp;

//Gionee linggz: 2012-8-20 add for CR00678779 begin
import android.os.SystemProperties;
//Gionee linggz: 2012-8-20 add for CR00678779 end
/**
 * Handle incoming SMSes.  Just dispatches the work off to a Service.
 */
public class SmsReceiver extends BroadcastReceiver {
    static final Object mStartingServiceSync = new Object();
    static PowerManager.WakeLock mStartingService;
    private static SmsReceiver sInstance;
    //Gionee linggz: 2012-8-20 add for CR00678779 begin
    private static final boolean gnVHHflag = SystemProperties.get("ro.gn.oversea.custom").equals("VIETNAM_VHH"); 
    //Gionee linggz: 2012-8-20 add for CR00678779 end
    //Gionee tangzepeng 2012-10-09 add for CR00709806 begin
    private static final boolean gnPTflag = SystemProperties.get("ro.gn.oversea.custom").equals("INDONESIA_POLYTRON"); 
    //Gionee tangzepeng 2012-10-09 add for CR00709806 end
    public static SmsReceiver getInstance() {
        if (sInstance == null) {
            sInstance = new SmsReceiver();
        }
        return sInstance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        onReceiveWithPrivilege(context, intent, false);
    }

    protected void onReceiveWithPrivilege(Context context, Intent intent, boolean privileged) {
        // If 'privileged' is false, it means that the intent was delivered to the base
        // no-permissions receiver class.  If we get an SMS_RECEIVED message that way, it
        // means someone has tried to spoof the message by delivering it outside the normal
        // permission-checked route, so we just ignore it.
        if (!privileged && intent.getAction().equals(Intents.SMS_RECEIVED_ACTION)) {
            return;
        }

        Log.d(MmsApp.TXN_TAG, "SmsReceiver: onReceiveWithPrivilege(). Slot Id = " 
            + Integer.toString(intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1), 10)
            +", Action = " + intent.getAction()
            +", Third_Response = " + intent.getStringExtra(MessageSender.Third_Response)
            +", result = " + getResultCode());
        intent.setClass(context, SmsReceiverService.class);
        intent.putExtra("result", getResultCode());
        beginStartingService(context, intent);
    }
    // Aurora xuyong 2014-09-25 added for INDIA REQUIREMENT start
    private static boolean needShowComingSms(Intent intent) {
        String action = intent.getAction();
        return MmsApp.mHasIndiaFeature && (action.equals("android.provider.Telephony.SMS_DELIVER") || action.equals("android.provider.Telephony.SMS_RECEIVED"));
    }
    // Aurora xuyong 2014-09-25 added for INDIA REQUIREMENT end
    // N.B.: <code>beginStartingService</code> and
    // <code>finishStartingService</code> were copied from
    // <code>com.android.calendar.AlertReceiver</code>.  We should
    // factor them out or, even better, improve the API for starting
    // services under wake locks.

    /**
     * Start the service to process the current event notifications, acquiring
     * the wake lock before returning to ensure that the service will run.
     */
    public static void beginStartingService(Context context, Intent intent) {
        // Aurora xuyong 2014-09-25 added for INDIA REQUIREMENT start
         boolean needScreenOn = needShowComingSms(intent);
        // Aurora xuyong 2014-09-25 added for INDIA REQUIREMENT end
        synchronized (mStartingServiceSync) {
            if (mStartingService == null) {
                PowerManager pm =
                    (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        //Gionee linggz: 2012-8-20 modify for CR00678779 begin
        //Gionee tangzepeng 2012-10-09 modify for CR00709806 begin
                // Aurora xuyong 2014-09-25 modified for INDIA REQUIREMENT start
                if((gnVHHflag == true) || (gnPTflag == true) || needScreenOn){
                // Aurora xuyong 2014-09-25 modified for INDIA REQUIREMENT end
                    mStartingService = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
                            "StartingAlertService");
                    mStartingService.setReferenceCounted(true);

                }else{
                mStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "StartingAlertService");
                mStartingService.setReferenceCounted(false);
                }
        //Gionee tangzepeng 2012-10-09 modify for CR00709806 end
        //Gionee linggz: 2012-8-20 modify for CR00678779 end
            }
        //Gionee linggz: 2012-8-20 modify for CR00678779 begin
        //Gionee tangzepeng 2012-10-09 modify for CR00709806 begin
            if(gnVHHflag == true){
                mStartingService.acquire(6000);
            // Aurora xuyong 2014-09-25 modified for INDIA REQUIREMENT start
            }else if(gnPTflag == true || needScreenOn){
            // Aurora xuyong 2014-09-25 modified for INDIA REQUIREMENT end
        mStartingService.acquire(3000);
        }else{
            mStartingService.acquire();
            }
        //Gionee tangzepeng 2012-10-09 modify for CR00709806 end
        //Gionee linggz: 2012-8-20 modify for CR00678779 end
            context.startService(intent);
        }
    }

    /**
     * Called back by the service when it has finished processing notifications,
     * releasing the wake lock if the service is now stopping.
     */
    public static void finishStartingService(Service service, int startId) {
        Log.d(MmsApp.TXN_TAG, "Sms finishStartingService");
        synchronized (mStartingServiceSync) {
            if (mStartingService != null) {
                if (service.stopSelfResult(startId)) {
        //Gionee linggz: 2012-8-20 modify for CR00678779 begin
        //Gionee tangzepeng 2012-10-09 modify for CR00709806 begin
                    if(!((gnVHHflag == true) || (gnPTflag == true))){
                    mStartingService.release();
                    }
        //Gionee tangzepeng 2012-10-09 modify for CR00709806 end
        //Gionee linggz: 2012-8-20 modify for CR00678779 end
                }
            }
        }
    }
}
