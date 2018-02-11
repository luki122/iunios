package com.aurora.AnimationView;

import java.util.HashMap;
import java.util.Map;

import com.android.deskclock.Log;
import com.aurora.AnimationView.AnimationState.OnHourGlassAnimationCompleteListener;
import com.aurora.timer.TimerFragment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
public  class HourGlassEndingAnimationState extends HourGlassRunningPauseAnimationState {	       
	
	int mHourglassStep = 0;
	
	public  HourGlassEndingAnimationState(Context context, Handler h,AuroraHourGlassView view) {
		super(context,h, view);
	}
	
	public  void onDraw(Canvas canvas) {
		super.onDraw(canvas);

     	double g = 2*(RADIUS + 3.5*delta-H)/0.09;
 		double h= g*Math.pow(mHourglassStep*0.03, 2)/2;
 		mView.oval.set(width/2-delta/4, (int)(height/2-1.75*delta + h), width/2+delta/4, (int)(height-1.5*delta-H));
       	canvas.drawRect(mView.oval, mView.mRoundPaint);
    
     	
        
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
				
  			mHourglassStep++;
  			mView.invalidate();
				if(mHourglassStep<10) {
					mHandler.postDelayed(this , 30);
				} else {
					if(mListener!=null) {
						mListener.onHourGlassAnimationComplete();
					}
				}
       
		}
    };
}

