package com.aurora.AnimationView;

import java.util.HashMap;
import java.util.Map;

import com.aurora.AnimationView.AnimationState.OnHourGlassAnimationCompleteListener;
import com.aurora.timer.TimerFragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;
import android.util.Log;
public  class HourGlassStartingAnimationState extends HourGlassRunningPauseAnimationState {	       
	
	int mHourglassStep = 0;
	public static final String TAG = "HourGlassStartingAnimationState";
//	Bitmap b;
	
	public  HourGlassStartingAnimationState(Context context, Handler h,AuroraHourGlassView view) {
		super(context,h, view);	
	    
	}
	
	public  void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	    Log.i(TAG, "HourGlassStartingAnimationState ondraw");  

 		double g = 2*(RADIUS + 3.5*delta -H)/0.09;
 		double h= g*Math.pow(mHourglassStep*0.03, 2)/2;
 		mView.oval.set(width/2-delta/4, (int)(height/2-1.75*delta), width/2+delta/4, (int)(height/2-1.75*delta + h));
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
			}else {
				if(mListener != null) {
					mListener.onHourGlassAnimationComplete();
				}
			}
		}
    };
}

