package com.aurora.apihook.keyguard;

import android.os.Build;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.widget.ILockSettings;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;

public class LockPatternUtilsHook implements Hook{
	private static final String TAG = "LockPatternUtilsHook";
	//public static final String HOOK_CLASS_NAME = "com.android.internal.widget.LockPatternUtils";
	
	public void before_checkPassword(MethodHookParam param) {
		String device = Build.BRAND;
		Log.d(TAG, TAG + "---before_checkPassword..." + device);
		if (device != null && device.equals("samsung") && Build.VERSION.SDK_INT > 18) {
			final int userId = (Integer) ClassHelper.callMethod(param.thisObject, "getCurrentOrCallingUserId");
	        try {
	        	ILockSettings settings = (ILockSettings)ClassHelper.callMethod(param.thisObject, "getLockSettings");
	        	param.setResult(settings.checkPassword((String)param.args[0], userId));
	        } catch (RemoteException re) {
	        	param.setResult(true);
	        }
		}
	}
}
