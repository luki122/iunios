package com.aurora.apihook.telephony;
//Aurora xuyong 2014-11-06 created for Android 4.4 MMS Hook
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;


public class SmsApplicationHook implements Hook {
    
    public void before_getDefaultSmsApplication(MethodHookParam param) {
        getProperDefaultApplication(param, 0);
    }
    
    public void before_getDefaultMmsApplication(MethodHookParam param) {
        getProperDefaultApplication(param, 1);
    }
    
    public void before_getDefaultRespondViaMessageApplication(MethodHookParam param) {
        getProperDefaultApplication(param, 2);
    }
    
    public void before_getDefaultSendToApplication(MethodHookParam param) {
        getProperDefaultApplication(param, 3);
    }
    
    public void before_shouldWriteMessageForPackage(MethodHookParam param) {
        if (param.args[0] == null) param.setResult(true);
        String defaultSmsPackage = null;
        try {
            ComponentName component = (ComponentName) ClassHelper.callStaticMethod(
                    Class.forName("com.android.internal.telephony.SmsApplication"),
                    "getDefaultSmsApplication",
                    new Object[]{(Context)param.args[1],
                    false});
            if (component != null) {
                defaultSmsPackage = component.getPackageName();
            }
    
            if ((defaultSmsPackage == null || !defaultSmsPackage.equals((String)param.args[0])) &&
                    !((String)param.args[0]).equals("com.android.bluetooth")) {
                // To write the message for someone other than the default SMS and BT app
                // As some other apps may send or receive msg by avoiding android 4.4 feature
                // we don't need write msg for package here, so we don't return true anymore.
                // Aurora xuyong 2014-09-25 modified for upper reason start
                param.setResult(false);
                // Aurora xuyong 2014-09-25 modified for upper reason end
            }
            param.setResult(false);
        } catch (java.lang.ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    private void getProperDefaultApplication(MethodHookParam param, int index) {
        ComponentName component = null;
        try {
            Object smsApplicationData = ClassHelper.callStaticMethod(
                    Class.forName("com.android.internal.telephony.SmsApplication"),
                    "getApplication",
                    new Object[] {(Context)param.args[0], ((Boolean)param.args[1]).booleanValue()});
            String packageName = (String)ClassHelper.getObjectField(smsApplicationData, "mPackageName");
            String className = null;
            switch(index) {
                case 0:
                    className = (String)ClassHelper.getObjectField(smsApplicationData, "mSmsReceiverClass");
                    break;
                case 1:
                    className = (String)ClassHelper.getObjectField(smsApplicationData, "mMmsReceiverClass");
                    break;
                case 2:
                    className = (String)ClassHelper.getObjectField(smsApplicationData, "mRespondViaMessageClass");
                    break;
                case 3:
                    className = (String)ClassHelper.getObjectField(smsApplicationData, "mSendToClass");
                    break;
            }
            if (smsApplicationData != null) {
                component = new ComponentName(packageName, className);
            }
            auroraRebuildDefaultApp(component, index);
            if (param != null) {
                param.setResult(component);
            }
        } catch (java.lang.ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    private static void auroraRebuildDefaultApp(ComponentName cn, int type) {
        if (cn == null || cn.getPackageName().equals("com.android.mms")) {
            switch(type) {
            case 0:
               // default sms
               cn = new ComponentName("com.android.mms", "com.android.mms.transaction.PrivilegedSmsReceiver");
               break;
            case 1:
               // default mms 
               cn = new ComponentName("com.android.mms", "com.android.mms.transaction.PushReceiver");
               break;
            case 2:
               // default via
               cn = new ComponentName("com.android.mms", "com.android.mms.ui.NoConfirmationSendServiceProxy");
               break;
            case 3:
               // default sendto
               cn = new ComponentName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
               break;
            }
        }
    }
}
