package com.aurora.AnimationView;

import java.util.HashMap;
import java.util.Map;

import com.aurora.AnimationView.AnimationState.OnHourGlassAnimationCompleteListener;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
public  class AnimationStateBase extends AnimationState {	

    	
	public  AnimationStateBase(Context context, Handler h,AuroraHourGlassView view) {
		super(context,h, view);
	}
	
	public  void onDraw(Canvas canvas) {             
	}

	public  Map<String, Object> getState() {
		return mMap;
	}
	
	public  void setState(Map<String, Object> map) {
		mMap.putAll(map);
	}
	
	public void startAnimation(OnHourGlassAnimationCompleteListener listener) {
	}
	
	public void cancelAnimation() {
	}
	

    
}

