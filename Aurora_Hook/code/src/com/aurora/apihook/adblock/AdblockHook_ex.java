package com.aurora.apihook.adblock;

import android.content.Context;
import android.view.View;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;

public class AdblockHook_ex implements Hook {

//	private static final String TAG=AdblockHook_ex.class.getName();
	
	public void after_View(MethodHookParam param){
		Context context = (Context)param.args[0];
		View view = (View)param.thisObject;
		auroraAdblockFunc(context,view);
	}

    private void auroraAdblockFunc(Context context,View object){
    	AuroraAdBlockHost.getInstance(context).auroraHideAdView(context,object);
    }
}
