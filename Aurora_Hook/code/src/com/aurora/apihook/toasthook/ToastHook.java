package com.aurora.apihook.toasthook;

import java.lang.reflect.Field;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;


public class ToastHook  implements Hook {
	private static final String TAG = "ToastHook";
	
	private boolean isCustomView = false;
	
	public void before_setView(MethodHookParam param){
		isCustomView = true;
	}

	public void before_handleShow(MethodHookParam param) {
		Log.e(TAG, "before_handleShow+"+isCustomView);
		ClassHelper.setIntField(param.thisObject,"mGravity",Gravity.TOP|Gravity.CENTER_HORIZONTAL);
		ClassHelper.setIntField(param.thisObject,"mY",0);
		ClassHelper.setIntField(param.thisObject,"mX",0);
		WindowManager.LayoutParams mParam = (LayoutParams) ClassHelper.getObjectField(param.thisObject,"mParams");
		if(mParam != null){
			Log.e(TAG, "mParam != null");
			mParam.height = WindowManager.LayoutParams.WRAP_CONTENT;
			mParam.width = isCustomView?WindowManager.LayoutParams.WRAP_CONTENT:WindowManager.LayoutParams.MATCH_PARENT;
//			mParam.format = PixelFormat.TRANSLUCENT;
			mParam.windowAnimations = com.aurora.R.style.AnimationToast;
			mParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
			mParam.setTitle("Toast");
			mParam.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
		                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
		                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
			ClassHelper.setObjectField(param.thisObject, "mParams", mParam);
		}
       
	}
	
	
	
	
	
	
	
	
}
