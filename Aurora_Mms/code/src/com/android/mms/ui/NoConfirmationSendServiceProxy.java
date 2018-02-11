// Aurora xuyong 2014-02-17 created for bug #2348
/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.ui;

import android.app.Service;
// Aurora xuyong 2014-07-02 added for bug #6272 start
import android.content.Context;
// Aurora xuyong 2014-07-02 added for bug #6272 end
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.android.mms.data.Conversation;
import com.android.mms.transaction.MessageSender;
import com.android.mms.transaction.SmsMessageSender;
// M: add for gemini
import com.android.mms.MmsApp;
import com.gionee.internal.telephony.GnPhone;
import com.aurora.featureoption.FeatureOption;


// Aurora xuyong 2014-07-02 added for bug #6272 start
import gionee.provider.GnTelephony.SIMInfo;

import com.gionee.internal.telephony.GnPhone;
// Aurora xuyong 2014-07-02 added for bug #6272 end

/**
 * Respond to a special intent and send an SMS message without the user's intervention.
 */
public class NoConfirmationSendServiceProxy extends Service {
    public static final String SEND_NO_CONFIRM_INTENT_ACTION =
        "android.intent.action.RESPOND_VIA_MESSAGE";
    private static final String TAG = "Mms/NoConfirmationSendServiceProxy";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        ComposeMessageActivity.log("NoConfirmationSendServiceProxy onStartCommand");

        String action = intent.getAction();
        if (!SEND_NO_CONFIRM_INTENT_ACTION.equals(action)) {
            ComposeMessageActivity.log("NoConfirmationSendServiceProxy onStartCommand wrong action: " +
                    action);
            stopSelf();
            return START_NOT_STICKY;
        }
        Bundle extras = intent.getExtras();
        if (extras == null) {
            ComposeMessageActivity.log("Called to send SMS but no extras");
            stopSelf();
            return START_NOT_STICKY;
        }

        String message = extras.getString(Intent.EXTRA_TEXT);

        Uri intentUri = intent.getData();
        String recipients = Conversation.getRecipients(intentUri);

        if (TextUtils.isEmpty(recipients) || TextUtils.isEmpty(message)) {
            ComposeMessageActivity.log("Recipient(s) and/or message cannot be empty");
            stopSelf();
            return START_NOT_STICKY;
        }
        String[] dests = TextUtils.split(recipients, ";");

        // Using invalid threadId 0 here. When the message is inserted into the db, the
        // provider looks up the threadId based on the recipient(s).
        long threadId = 0;
        // M: extend for gemini
        // Aurora yudingmin 2014-12-09 added for thread's app result start
        String response = extras.getString(MessageSender.Third_Response);
        // Aurora yudingmin 2014-12-09 added for thread's app result end
        SmsMessageSender smsMessageSender;
        if (MmsApp.mGnMultiSimMessage) {
            // Aurora xuyong 2014-05-29 modified for bug #5141 start
            // Aurora xuyong 2014-07-02 modified for bug #6272 start
              int simId = getSimIdBySlot(getApplicationContext(), extras.getInt("simId"));
            smsMessageSender = new SmsMessageSender(this, dests,
                    // Aurora xuyong 2014-11-25 modified for bug #10052 start
                    message, threadId, simId, true);
                    // Aurora xuyong 2014-11-25 modified for bug #10052 end
            // Aurora xuyong 2014-07-02 modified for bug #6272 end
            // Aurora xuyong 2014-05-29 modified for bug #5141 end
        } else {
            smsMessageSender = new SmsMessageSender(this, dests,
                    // Aurora xuyong 2014-11-25 modified for bug #10052 start
                    message, threadId, true);
                    // Aurora xuyong 2014-11-25 modified for bug #10052 end
        }
        // Aurora yudingmin 2014-12-09 added for thread's app result start
        if(!TextUtils.isEmpty(response)){
            Log.v(TAG, "Third_Response start is " + response);
            smsMessageSender.setThirdResponse(response);
        }
        // Aurora yudingmin 2014-12-09 added for thread's app result end
        try {
            // This call simply puts the message on a queue and sends a broadcast to start
            // a service to send the message. In queing up the message, however, it does
            // insert the message into the DB.
            smsMessageSender.sendMessage(threadId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS message, threadId=" + threadId, e);
        }

        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    // Aurora xuyong 2014-07-02 added for bug #6272 start
    private int getSimIdBySlot(Context context, int slot) {
        int simId = 0;
        switch (slot) {
            case GnPhone.GEMINI_SIM_1:
                 SIMInfo info1 = SIMInfo.getSIMInfoBySlot(context
                          , GnPhone.GEMINI_SIM_1);
                 if (info1 != null) {
                     simId = (int)(info1.mSimId);
                  }
                 break;
            case GnPhone.GEMINI_SIM_2:
                 SIMInfo info2 = SIMInfo.getSIMInfoBySlot(context
                          , GnPhone.GEMINI_SIM_2);
                 if (info2 != null) {
                     simId = (int)(info2.mSimId);
                  }
                 break;
        }
        return simId;
    }
    // Aurora xuyong 2014-07-02 added for bug #6272 end
}
