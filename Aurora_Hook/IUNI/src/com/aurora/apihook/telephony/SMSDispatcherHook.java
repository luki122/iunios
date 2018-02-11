package com.aurora.apihook.telephony;
// Aurora xuyong 2014-11-06 created for Android 4.2/4.3 MMS Hook 
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.os.PowerManager;
import android.provider.Telephony.Sms.Intents;
import android.util.Log;

import com.android.internal.telephony.SMSDispatcher;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;

import com.aurora.apihook.XC_MethodHook.MethodHookParam;

public class SMSDispatcherHook implements Hook {
    
    public void before_dispatch(MethodHookParam param) {
        if (param != null) {
            param.setResult(null);
        }
        Context mContext = (Context) ClassHelper.getObjectField(param.thisObject, "mContext");
        PowerManager.WakeLock mWakeLock = (PowerManager.WakeLock) ClassHelper.getObjectField(param.thisObject, "mWakeLock");
        mWakeLock.acquire(5000);
        auroraOpeIntent((Intent)param.args[0]);
        if (param.args.length == 3) {
            BroadcastReceiver resultReceiver = (BroadcastReceiver) ClassHelper.getObjectField(param.thisObject, "mResultReceiver");
            mContext.sendOrderedBroadcast((Intent)param.args[0], (String)param.args[1], ((Integer)param.args[2]).intValue(), resultReceiver,
                    (SMSDispatcher)param.thisObject, -1, null, null);
        } else if (param.args.length == 4) {
            mContext.sendOrderedBroadcast((Intent)param.args[0], (String)param.args[1], ((Integer)param.args[2]).intValue(), (BroadcastReceiver)param.args[3],
                    (SMSDispatcher)param.thisObject, -1, null, null);
        }
    }

    
    private void auroraOpeIntent(Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intents.SMS_RECEIVED_ACTION)) {
            intent.setComponent(new ComponentName("com.android.mms", "com.android.mms.transaction.PrivilegedSmsReceiver"));
        } else if (action.equals(Intents.WAP_PUSH_RECEIVED_ACTION)) {
            intent.setComponent(new ComponentName("com.android.mms", "com.android.mms.transaction.PushReceiver"));
        } else if (action.equals(Intents.SMS_CB_RECEIVED_ACTION) || action.equals(Intents.SMS_EMERGENCY_CB_RECEIVED_ACTION) || action.equals("android.provider.Telephony.CB_SMS_RECEIVED")) {
            intent.setComponent(new ComponentName("com.android.mms", "com.android.mms.transaction.CBMessageReceiver"));
        }
    }
    
}

