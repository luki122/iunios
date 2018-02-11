package com.aurora.AnimationView;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.os.Handler;
public abstract class AnimationState {
	
    public interface OnHourGlassAnimationCompleteListener {
        void onHourGlassAnimationComplete();
    }
     
	int mStepNumber = 10;
	
	 int mInitStep = 0;
    
	Handler mHandler ;
	
	Map<String, Object> mMap;
	
	AuroraHourGlassView mView;
	
	OnHourGlassAnimationCompleteListener mListener;
	
	Context mContext;
	
	public AnimationState(Context context, Handler h, AuroraHourGlassView view) {
		mHandler = h;
		mMap = new HashMap<String, Object>();
		mView = view;
		mContext = context;
	}
	
	public abstract void onDraw(Canvas canvas) ;
	public abstract Map<String, Object> getState() ;
	public abstract void setState(Map<String, Object> map) ;
	public abstract void startAnimation(OnHourGlassAnimationCompleteListener listener) ;
	public abstract void cancelAnimation() ;
}