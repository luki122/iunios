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
public  class StopWatchEndAnimationState extends StopWatchAnimationStateBase {	
       
    
    int mDarkPoints;
	
	public  StopWatchEndAnimationState(Context context, Handler h,AuroraHourGlassView view) {
		super(context,h, view);
	}
	
	public  void onDraw(Canvas canvas) {
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG));
        mView.rotateCenterY = mView.BigRadius;        
		mView.panRotate.setTranslate(mView.rotateCenterX +mDotRadius, mView.rotateCenterY +mDotRadius);
        canvas.concat(mView.panRotate);
        
  		canvas.drawCircle(mView.ptsDraw[0], mView.ptsDraw[1], mDotRadius, mView.mDarkPaint);
  		canvas.drawCircle(mView.ptsDraw[52], mView.ptsDraw[53], mDotRadius, mView.mDarkPaint);
  		canvas.drawCircle(mView.ptsDraw[104], mView.ptsDraw[105], mDotRadius, mView.mDarkPaint);
  		canvas.drawCircle(mView.ptsDraw[156], mView.ptsDraw[157], mDotRadius, mView.mDarkPaint);
    	for (int i=2*mDarkPoints+2; i <= 2*25; i+=2) {
    		canvas.drawCircle(mView.ptsDraw[i], mView.ptsDraw[i+1], mDotRadius, mView.mDarkPaint);
    		canvas.drawCircle(mView.ptsDraw[i+52], mView.ptsDraw[i+53], mDotRadius, mView.mDarkPaint);
    		canvas.drawCircle(mView.ptsDraw[i+104], mView.ptsDraw[i+105], mDotRadius, mView.mDarkPaint);
    		canvas.drawCircle(mView.ptsDraw[i+156], mView.ptsDraw[i+157], mDotRadius, mView.mDarkPaint);
    	}
	}

	
	public void startAnimation(OnHourGlassAnimationCompleteListener listener) {
		mListener = listener;
		mHandler.post(mRunnable);
	}
	
	public void cancelAnimation() {
		mHandler.removeCallbacks(mRunnable);
	}
	
    Runnable mRunnable = new Runnable() {
		public void run() {	
				mDarkPoints+=2;
				mView.invalidate();
				if(mDarkPoints<25) {
					mHandler.postDelayed(this , 12);
				} else  {
//					mDarkPoints = 0;
					if(mListener!=null) {
						mListener.onHourGlassAnimationComplete();
					}
				}	
	    	
		}
    };
}

