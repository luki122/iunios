package com.aurora.apihook.server;

import android.content.pm.ApplicationInfo;
import android.app.AppGlobals;
import android.os.Process;
import android.os.UserHandle;
import android.os.RemoteException;
import android.util.Log;
import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;

/**
 * Hook Notification applyStandardTemplate/
 * 
 * @author: Rock.Tong
 * @date: 2015-01-19
 */
public class NotificationManagerServiceHook{
	
	public void after_isUidSystem(MethodHookParam param) {
		int uid = (Integer)param.args[0];
		final int appid = UserHandle.getAppId(uid);
	    ApplicationInfo ai = null;
	    try {
	        ai = AppGlobals.getPackageManager()
					.getApplicationInfo("com.android.systemui", 0, UserHandle.getCallingUserId());
	    } catch (RemoteException e) {
	    }
	    if(ai == null){
	    	param.setResult(appid == Process.SYSTEM_UID || appid == Process.PHONE_UID || uid == 0);
	    }else{
	    	param.setResult(appid == Process.SYSTEM_UID || appid == Process.PHONE_UID || uid == 0 || uid == ai.uid);
	    }
    }
	
}