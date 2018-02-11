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
public  class StopWatchInitAnimationState extends StopWatchAnimationStateBase {	
       
    int mLightPoints = 10;
    boolean mDirection = true;
    
    public final static int ANIMATION_INIT = 1;
    public final static int ANIMATION_INIT2 = 2;
	
	public  StopWatchInitAnimationState(Context context, Handler h,AuroraHourGlassView view) {
		super(context,h, view);
	}
	
	public  void onDraw(Canvas canvas) {
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG));
        mView.rotateCenterY = mView.BigRadius;        
		mView.panRotate.setTranslate(mView.rotateCenterX +mDotRadius, mView.rotateCenterY +mDotRadius);
        canvas.concat(mView.panRotate);
        
        if(mInitStep == ANIMATION_INIT2) {
	      	for (int i=0; i < 2*52; i+=2) {
	    		canvas.drawCircle(mView.ptsDraw[i], mView.ptsDraw[i+1], mDotRadius, mView.mDarkPaint);
	    	}
	    	for (int i=2*mLightPoints + 104; i < 2*mView.COUNT; i+=2) {
	    		canvas.drawCircle(mView.ptsDraw[i], mView.ptsDraw[i+1], mDotRadius, mView.mDarkPaint);
	    	}
	        if(mLightPoints > 0) {
	        	for (int i=0; i < 2*mLightPoints; i+=2) {
	        		canvas.drawCircle(mView.ptsDraw[i + 104], mView.ptsDraw[i+105], mDotRadius, mView.mLightPaint);
	        	}	        	
	        }
        } else {        	       
        	int start = 104%(2*mView.COUNT) ;
        	for (int i=start; i <start + 10*(10-mStepNumber); i+=2) {
        		canvas.drawCircle(mView.ptsDraw[i%(2*mView.COUNT)], mView.ptsDraw[(i + 1)%(2*mView.COUNT)], mDotRadius, mView.mDarkPaint);
        	}	
        	for (int i=start + 10*(10-mStepNumber); i <start + 20*(10-mStepNumber); i+=2) {
        		canvas.drawCircle(mView.ptsDraw[i%(2*mView.COUNT)], mView.ptsDraw[(i + 1)%(2*mView.COUNT)], mDotRadius, mView.mLightPaint);        	
        	}	
        	if(mStepNumber == 0) {
        		for(int i =96 ;i<104; i+=2)
        			canvas.drawCircle(mView.ptsDraw[i], mView.ptsDraw[i + 1], mDotRadius, mView.mLightPaint); 
        	}
        }
	}

	
	public void startAnimation(OnHourGlassAnimationCompleteListener listener) {
		mListener = listener;
		mHandler.post(mRunnable);
	}
	
	public void cancelAnimation() {
		mHandler.removeCallbacks(mRunnable);
		mHandler.removeCallbacks(mRunnable2);
		mLightPoints = 0;
		mView.invalidate();
	}
	
    Runnable mRunnable = new Runnable() {
		public void run() {	
			mInitStep = ANIMATION_INIT;
			mStepNumber --;
		    mView.invalidate();	
		    if(mStepNumber>0) {
		    	mHandler.postDelayed(this,13);
		    } else {
		    	mLightPoints = 0;
		    	mHandler.post(mRunnable2);
		    }
		}
    };
	
    
    int directionChangeTimes = 0;  
    Runnable mRunnable2 = new Runnable() {
		public void run() {	
			    mInitStep = ANIMATION_INIT2;
			    int top = directionChangeTimes*2 + 4;
		        if(mLightPoints==top) {
					mDirection = false;
					directionChangeTimes++;
				} else if(mLightPoints==1) {
					mDirection = true;
					directionChangeTimes = directionChangeTimes %3;
				} 
				mLightPoints += 1 * (mDirection ? 1 : -1 );
			    mView.invalidate();	
				mHandler.postDelayed(this , (int)(250 * (1 -mLightPoints / 8f)));	
	    	
		}
    };
}

