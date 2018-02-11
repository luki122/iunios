package com.aurora.apihook.pms;

import com.aurora.apihook.XC_MethodHook.MethodHookParam;
import com.aurora.apihook.ClassHelper;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

public class PowerManagerServiceHook {
	
	public void before_updateDisplayPowerStateLocked(MethodHookParam param){
		//Log.v("gary", "-----before_updateDisplayPowerStateLocked----begin");
		Context mContext = (Context) ClassHelper.getObjectField(param.thisObject, "mContext");
		int value = Settings.System.getInt(mContext.getContentResolver(),
		        android.provider.Settings.System.SCREEN_BRIGHTNESS, 100);
		ClassHelper.setIntField(param.thisObject, "mScreenBrightnessSettingDefault",value);
		//Log.v("gary", "-----before_updateDisplayPowerStateLocked----end");
	}
	
	
}