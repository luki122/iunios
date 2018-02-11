package com.aurora.apihook.wifiservice;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;

import android.content.Context;
import android.content.Intent;
import android.util.Log;



public class WifiServiceHook{
	private static final String TAG = "gary";
	
	 public void after_WifiService(MethodHookParam param){
		 Log.v(TAG,"---after_WifiService---begin");
		 Context mContext = (Context) ClassHelper.getObjectField(param.thisObject, "mContext");
		 Intent qyIntent= new Intent("com.android.settings.AuroraLightSensorService");
		 mContext.startService(qyIntent);
		 Log.v(TAG,"---after_WifiService---end");
	 }
}