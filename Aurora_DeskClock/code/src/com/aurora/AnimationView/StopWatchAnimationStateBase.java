package com.aurora.AnimationView;

import java.util.HashMap;
import java.util.Map;

import com.aurora.AnimationView.AnimationState.OnHourGlassAnimationCompleteListener;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import com.aurora.utils.DensityUtil;
public  class StopWatchAnimationStateBase extends AnimationStateBase {	

	protected int mDotRadius=4  ;

	
    	
	public  StopWatchAnimationStateBase(Context context, Handler h,AuroraHourGlassView view) {
		super(context,h, view);	
		//dip px 转换，秒表光圈小点的大小
		mDotRadius=  DensityUtil.dip2px( context, 1)+1;						
	}
		

    
}

