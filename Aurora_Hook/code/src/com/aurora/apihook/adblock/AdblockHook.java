package com.aurora.apihook.adblock;

import android.content.Context;
import android.view.View;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;

public class AdblockHook implements Hook {
//	private static final String TAG=AdblockHook.class.getName();	
	public void after_View(MethodHookParam param){
		Context context = (Context)param.args[0];
		View view = (View)param.thisObject;
		auroraAdblockFunc(context,view);
	}
	
	public void after_setVisibility(MethodHookParam param){
		int visibility = (Integer)param.args[0];
		View view = (View)param.thisObject;
		Context context = view.getContext();
		auroraAdblockFuncForSetVisib(context,view,visibility);
	}
    
    private void auroraAdblockFuncForSetVisib(Context context,View object,int visibility){
    	if(visibility == View.GONE){
    		return ;
    	}
    	auroraAdblockFunc(context,object);
    }
    
    private void auroraAdblockFunc(Context context,View object){
    	AuroraAdBlockHost.getInstance(context).auroraHideAdView(context,object);
    }
}
