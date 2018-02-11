package com.aurora.apihook.resolver;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;


public class ResolverActivityHook implements Hook{

	private static final String TAG="ResolverActivityHook";
	public void before_ComponentName(MethodHookParam param){
		if(param.args.length == 2){
			Object tempName =  param.args[1];
			if(tempName instanceof String){
				String comName = (String) tempName;
				if("com.android.internal.app.ResolverActivity".equals(comName)){
					ClassHelper.setObjectField(param.thisObject, "mClass", "com.android.internal.app.AuroraResolverActivity");
				}
			}
		}
	}
}
