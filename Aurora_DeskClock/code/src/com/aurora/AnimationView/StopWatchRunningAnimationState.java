package com.aurora.AnimationView;

import java.util.HashMap;
import java.util.Map;

import com.aurora.AnimationView.AnimationState.OnHourGlassAnimationCompleteListener;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;
import android.util.Log;
public  class StopWatchRunningAnimationState extends StopWatchAnimationStateBase {	

    	
	public  StopWatchRunningAnimationState(Context context, Handler h,AuroraHourGlassView view) {
		super(context,h, view);
	}
	
	public  void onDraw(Canvas canvas) {
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG));
		mView.rotateCenterY = mView.BigRadius;
		mView.panRotate.setTranslate(mView.rotateCenterX +mDotRadius, mView.rotateCenterY +mDotRadius);
        canvas.concat(mView.panRotate);
        
    	int number = mView.mLightRunningPoints;
    	for (int i= number > 52  ? 2*(number-53) :0; i < 2*52; i+=2) {
    		canvas.drawCircle(mView.ptsDraw[i], mView.ptsDraw[i+1], mDotRadius, mView.mDarkPaint);
    	}
    	for (int i=2*number + 104; i < 2*mView.COUNT; i+=2) {
    		canvas.drawCircle(mView.ptsDraw[i], mView.ptsDraw[i+1], mDotRadius, mView.mDarkPaint);
    	}
        if(number > 0) {
        	for (int i=0; i < 2*number; i+=2) {
        		canvas.drawCircle(mView.ptsDraw[(i + 104)%(2*mView.COUNT)], mView.ptsDraw[(i+105)%(2*mView.COUNT)], mDotRadius, mView.mLightPaint);
        	}	        	
        }       
     
	}

	

    
}

