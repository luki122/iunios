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

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.ui.ManageSimMessages;
import gionee.app.GnNotification;
import android.app.Notification;
import com.gionee.internal.telephony.GnPhone;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.provider.Telephony;
import com.aurora.featureoption.FeatureOption;
// Aurora xuyong 2015-08-13 added for bug #15613 start
import com.aurora.mms.ui.AuroraManageSimMessages;
import com.aurora.mms.ui.AuroraMultiSimManageActivity;
// Aurora xuyong 2015-08-13 added for bug #15613 end
import gionee.provider.GnTelephony.SIMInfo;
//gionee yewq 2012-11-15 modify for CR00723841 begin
import android.media.AudioManager;
//gionee yewq 2012-11-15 modify for CR00723841 end
/**
 * Receive Intent.SIM_FULL_ACTION.  Handle notification that SIM is full.
 */
public class SimFullReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Settings.Secure.getInt(context.getContentResolver(),
            Settings.Secure.DEVICE_PROVISIONED, 0) == 1 &&
            Telephony.Sms.Intents.SIM_FULL_ACTION.equals(intent.getAction())) {
            
            NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
            // Aurora xuyong 2015-08-13 modified for bug #15613 start
            Intent viewSimIntent = null;
            int slotId = -1;
            if (MmsApp.mGnMultiSimMessage) {
            	viewSimIntent = new Intent(context, AuroraMultiSimManageActivity.class);
                slotId = intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, GnPhone.GEMINI_SIM_1);
                viewSimIntent.putExtra("SlotId", slotId);
            } else {
            	viewSimIntent = new Intent(context, AuroraManageSimMessages.class);
            }
            // Aurora xuyong 2015-08-13 modified for bug #15613 end
            //viewSimIntent.setAction(Intent.ACTION_VIEW);
            viewSimIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, viewSimIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            GnNotification notification = new GnNotification();
            notification.icon = R.drawable.aurora_stat_notify_sms_failed;
            // add for gemini
            if (MmsApp.mGnMultiSimMessage) {
                SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(context, slotId);
                if (simInfo != null) {
                    notification.simId = simInfo.mSimId;
                    notification.simInfoType = 1;
                }
            }
            notification.tickerText = context.getString(R.string.sim_full_title);
            
            notification.defaults = GnNotification.DEFAULT_ALL;
            //gionee yewq 2012-11-15 modify for CR00723841 begin
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION) != AudioManager.VIBRATE_SETTING_ON) {
                // Aurora xuyong 2014-01-08 modified for notification's vibrate start
                //notification.defaults &= ~Notification.DEFAULT_VIBRATE;
                notification.vibrate = new long[]{0, 400, 100, 200, 100, 200};
                // Aurora xuyong 2014-01-08 modified for notification's vibrate end
            }
            //gionee yewq 2012-11-15 modify for CR00723841 end

            notification.setLatestEventInfo(
                    context, notification.tickerText, 
                    context.getString(R.string.sim_full_body),
                    pendingIntent);
            nm.notify(ManageSimMessages.SIM_FULL_NOTIFICATION_ID, notification);
       }
    }

}
