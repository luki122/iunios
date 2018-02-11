package com.aurora.apihook.resolver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;

public class ChooserActivityHook implements Hook{

	private static final String TAG="ChooserActivityHook";
	private static final String CHOOSER_ACTION = "android.intent.action.iuniCHOOSER";
	private static final String CHOOSER_HEAD = "android.intent.action";
	private static final String CHOOSER_TAIL = "CHOOSER";
	public void before_startActivityAsUser(MethodHookParam param){
		int index = -1;
		for(int i = 0;i < param.args.length;i++){
			Object arg = param.args[i];
				if(arg instanceof Intent){
					index = i;
					String action = ((Intent)arg).getAction();
					if(TextUtils.isEmpty(action)){
						return;
					}
					if(action.startsWith(CHOOSER_HEAD) &&action.endsWith(CHOOSER_TAIL) ){
						((Intent)param.args[index]).setAction(CHOOSER_ACTION);
					}
					break;
				}
		}
	}
	
}
