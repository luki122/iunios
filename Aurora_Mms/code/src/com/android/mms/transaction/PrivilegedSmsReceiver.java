/*
 * Copyright (C) 2008 Google Inc.
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//Gionee:tianxiaolong 2012.5.5 add for CR00596608 begin
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.os.SystemProperties;
import com.android.internal.telephony.ITelephony;
// Aurora xuyong 2014-07-02 added for reject feature start
import com.android.mms.MmsApp;
// Aurora xuyong 2014-09-09 added for 4.4 feature start
import com.aurora.mms.util.Utils;
// Aurora xuyong 2014-09-09 added for 4.4 feature end
// Aurora xuyong 2014-07-02 added for reject feature end
//Gionee:tianxiaolong 2012.5.5 add for CR00596608 end
import com.gionee.internal.telephony.GnPhone;
/**
 * This class exists specifically to allow us to require permissions checks on SMS_RECEIVED
 * broadcasts that are not applicable to other kinds of broadcast messages handled by the
 * SmsReceiver base class.
 */
public class PrivilegedSmsReceiver extends SmsReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Pass the message to the base class implementation, noting that it
        // was permission-checked on the way in.
       // Aurora xuyong 2014-09-09 added for 4.4 feature start
        if (Utils.hasKitKat() && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            return;
        }
       // Aurora xuyong 2014-09-09 added for 4.4 feature end
       // Aurora xuyong 2014-07-02 added for reject feature start
        if (MmsApp.sHasRejectFeature) {
            abortBroadcast();
        }
       // Aurora xuyong 2014-07-02 added for reject feature end
        onReceiveWithPrivilege(context, intent, true);
        
        //Gionee:tianxiaolong 2012.5.5 add for CR00596608 begin
        final boolean isBeepSoundInCall = SystemProperties.get("ro.gn.sms.beep.sound.in.call").equals("yes");
        if ((isBeepSoundInCall == true) && (phoneIsInUse() == true)) {
            ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2);
            Log.d("MMSLog", "SmsReceiver,toneGenerator beep...");
        }
        //Gionee:tianxiaolong 2012.5.5 add for CR00596608 end


    }

    //Gionee:tianxiaolong 2012.5.5 add for CR00596608 begin
    private boolean phoneIsInUse() {
        boolean phoneInUse = false;
        try {
        if (GnPhone.phone != null) phoneInUse = !GnPhone.phone.isIdle();
        } catch (RemoteException e) {
        Log.d("MMSLog", "phone.isIdle() failed", e);
        }
        return phoneInUse;
    }
    //Gionee:tianxiaolong 2012.5.5 add for CR00596608 end
}
