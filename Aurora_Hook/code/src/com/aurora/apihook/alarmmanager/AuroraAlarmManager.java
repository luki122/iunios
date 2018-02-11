package com.aurora.apihook.alarmmanager;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import android.util.Slog;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.*;
import android.os.PowerManager;

class AuroraAlarmManager
{

    private static final String TAG = "AuroraAlarmManager";

    public static String auroraGetWakeupApkValue(String pkgName) {

        String apkValue = "";

        try {
            BufferedReader bufReader = new BufferedReader(new FileReader("/system/etc/wakeup_apk_list"), 1024);
            String temp = null;
            int count = 0;
            while ((temp = bufReader.readLine()) != null) {
                //Slog.v(TAG, "isWakeupApk temp = " + temp + " count = " + count);
                count++;
                if (temp.contains(pkgName)) {
                    int index = temp.indexOf("=");
                    apkValue = temp.substring(index + 1, temp.length());
                    apkValue = apkValue.trim();
                    Slog.v(TAG, "isWakeupApk apkValue = " + apkValue + " pkgName = " + pkgName);

                    break;
                }
            }
        }
        catch (FileNotFoundException e) {
            Slog.e(TAG, "wakeup_apk_list not found, e = " + e);
        }
        catch (IOException e) {
            Slog.e(TAG, "read wakeup_apk_list error, e = " + e);
        }

        if (apkValue != null) {
            Slog.v(TAG, "isWakeupApk return value = " + apkValue);
            //            return apkValue.equals("1");
        }

        return apkValue;
    }

    public static boolean isSystemApp(PackageInfo pInfo) {  
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);  
    }

    public static boolean isSystemUpdateApp(PackageInfo pInfo) {  
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);  
    }  

    //wangyongliang start
    //apkValue
    //1: white list
    //2: not black, not white list
    //3: balck list
    public static int auroraAllowSetAlarm(Context ctx, int type, PendingIntent operation) {

        String pkgName = operation.getTargetPackage();
        PowerManager pm = (PowerManager)ctx.getSystemService(ctx.POWER_SERVICE);

        Slog.w(TAG, pkgName + " alarm type:" + type + "\n");
        try {
            PackageInfo pkInfo = ctx.getPackageManager().getPackageInfo(pkgName, 0); 

            if (isSystemApp(pkInfo) || isSystemUpdateApp(pkInfo))
            {
                Slog.w(TAG, "system application do nothing!!!!!!!!!!!");
                return type;
            }
        }catch(NameNotFoundException e) {  
            Slog.w(TAG, "can't find packageInfo");
        }

        if (pkgName.equals("android")) {
            Slog.w(TAG, "do nothing");
            return type;
        }

        String apkvalue = auroraGetWakeupApkValue(pkgName);
        Slog.w(TAG, apkvalue);
        //white lite
        if (apkvalue.equals("1"))
        {
            Slog.w(TAG, "white list");
            return type;
        }
       //black list 
        if (apkvalue.equals("3"))
        {
            Slog.w(TAG, "black list");
            return -1;
        }

       Slog.w(TAG, "non black non white list");
       //non white, not black list
       //follow wake up
       if (type == AlarmManager.RTC_WAKEUP)
       {
           type = AlarmManager.RTC;
	   Slog.w(TAG, "type = AlarmManager.RTC");
       }
       
       if (type == AlarmManager.ELAPSED_REALTIME_WAKEUP)
       {
           type = AlarmManager.ELAPSED_REALTIME;
           Slog.w(TAG, "type = AlarmManager.ELAPSED_REALTIME");
       }

       Slog.w(TAG, pkgName + " alarm type = " + type + "\n");
       
       if(!pm.isScreenOn())
       {
           Slog.w(TAG, "the screen is off, non white...");
           return -2;
       }

       return type;

    }

}
