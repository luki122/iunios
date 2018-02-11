package com.aurora.gesture;

import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;

public class AuroraGesture {
	private Context mContext;
	final static float MAX_SWIPE_ANGLE = (float) Math.PI / 3;
	private static int GESTURE_SLOP = 300;
	private static final int GESTURE_FLAG_NONE = 0;
	private static final int GESTURE_FLAG_DOWN = 1;
	private static final int GESTURE_FLAG_UP = 2;
	public AuroraGesture(Context context){
		mContext = context;
	}
	
	private  int detect(float startX,float startY,float endX,float endY){
		float deltaX = endX-startX;
		float deltaY = endY-startY;
		float deltaXABS = Math.abs(deltaX);
		float deltaYABS = Math.abs(deltaY);
		Log.i("xiejun", "deltaYABS = " + deltaYABS);
		if(deltaYABS<GESTURE_SLOP)return GESTURE_FLAG_NONE;
		float theta =0;
		if(deltaX == 0){
			theta = (float)Math.PI/2;
		}else{
			float slope = deltaYABS / deltaXABS;
			theta= (float) Math.atan(slope);
		}
		if(theta>MAX_SWIPE_ANGLE&&deltaY>0)return GESTURE_FLAG_DOWN;
		return GESTURE_FLAG_UP;
	}
	
	public boolean gestureSwip(float startX,float startY,float endX,float endY){
		int gestureFlag = detect(startX, startY, endX, endY);
		boolean result = false;
		switch (gestureFlag) {
		case GESTURE_FLAG_NONE:
			result =false;
			break;
		case GESTURE_FLAG_DOWN:
			expandNotify();
			result = true;
			break;
		case GESTURE_FLAG_UP:
			result = true;
			break;
		default:
			break;
		}
		return result;
	}
	public  void expandNotify() {
		// TODO Auto-generated method stub
    	int currentApiVersion = android.os.Build.VERSION.SDK_INT;
		try {
			Object service = mContext.getSystemService("statusbar");
			Class<?> statusbarManager = Class
					.forName("android.app.StatusBarManager");
			Method expand = null;
			if (service != null) {
				if (currentApiVersion <= 16) {
					expand = statusbarManager.getMethod("expand");
					expand.setAccessible(true);
					expand.invoke(service);
				} else {
					expand = statusbarManager
							.getMethod("expandNotificationsPanel");
				}
				expand.setAccessible(true);
				expand.invoke(service);
			}

		} catch (Exception e) {
		}
	}
	

}
