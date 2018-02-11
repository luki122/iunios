package com.aurora.apihook.alarmmanager;



import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.utils.AuroraLog;
import android.content.Context;
import android.app.PendingIntent;
import android.util.Slog;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;

public class AlarmManagerServiceHook implements Hook {

      private static final String TAG="AlarmManagerServiceHook";
      public void before_setImplLocked(MethodHookParam param)//4.4
      {
          //param.setResult(null);
        Slog.w(TAG, "hahajlu is need wake up1...");
        Context mContext=(Context) ClassHelper.getObjectField(param.thisObject, "mContext");
        int type=((Integer)param.args[0]).intValue();
        PendingIntent  operation=(PendingIntent)param.args[6];
        type = AuroraAlarmManager.auroraAllowSetAlarm(mContext, type, operation);
        if (-1 == type)
        {    
            Slog.w(TAG, "block alarm...");
            param.setResult(null);
        }    
        if (-2 == type)
        {    
            Slog.w(TAG, "the screen is off, non white ...");
            param.setResult(null);
        }    
        
        Slog.w(TAG, "hahajlu is need wake up2...");
      }	

      public void before_setRepeating(MethodHookParam param)//4.2 4.3
      {
          //param.setResult(null);
        Slog.w(TAG, "hahajlu is need wake up1...");
        Context mContext=(Context) ClassHelper.getObjectField(param.thisObject, "mContext");
        int type=((Integer)param.args[0]).intValue();
        PendingIntent  operation=(PendingIntent)param.args[3];
        type = AuroraAlarmManager.auroraAllowSetAlarm(mContext, type, operation);
        if (-1 == type)
        {    
            Slog.w(TAG, "block alarm...");
            param.setResult(null);
        }    
        if (-2 == type)
        {    
            Slog.w(TAG, "the screen is off, non white ...");
            param.setResult(null);
        }    
        
        Slog.w(TAG, "hahajlu is need wake up2...");
      }	

}
