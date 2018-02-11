package com.aurora.apihook.assetmanager;

import android.util.Log;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;


public class AssetManagerHook implements Hook{

	private static final String AURORA_FRAMEWORK_RES_PATH = "/system/framework/aurora-framework-res.apk";
	public void after_AssetManager(MethodHookParam param){
		Object assetManager = param.thisObject;
		if(assetManager != null){
			ClassHelper.callMethod(assetManager, "addAssetPath", AURORA_FRAMEWORK_RES_PATH);
//			  Log.e("addRes", "Add aurora-framework-res.apk   222222222222");
		}
	}
	
}
