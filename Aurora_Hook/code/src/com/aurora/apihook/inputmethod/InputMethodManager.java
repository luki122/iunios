package com.aurora.apihook.inputmethod;

import android.content.Context;
import android.content.Intent;
import android.os.Message;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;


public class InputMethodManager implements Hook{
	static final int MSG_SHOW_SOFT_INPUT = 1020;
	static final int MSG_HIDE_SOFT_INPUT = 1030;
	public void before_handleMessage(MethodHookParam param){
		Message msg = (Message) param.args[0];
		Context context = (Context) ClassHelper.getObjectField(param.thisObject, "mContext");
		if(msg != null){
			switch (msg.what) {
			case MSG_SHOW_SOFT_INPUT:
				auroraSendShowBroadcast(context);
				break;
			case MSG_HIDE_SOFT_INPUT:
				auroraSendHideBroadcast(context);
				break;

			default:
				break;
			}
		}
	}
	
	 private void auroraSendShowBroadcast(Context context){
		 context.sendBroadcast(new Intent("android.intent.action.ACTION_INPUT_METHOD_SHOW"));
	    }
	    
	    private void auroraSendHideBroadcast(Context context){
	    	context.sendBroadcast(new Intent("android.intent.action.ACTION_INPUT_METHOD_HIDE"));

	    }
	
}
