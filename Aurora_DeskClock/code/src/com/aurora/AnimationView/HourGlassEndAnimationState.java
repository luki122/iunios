package com.aurora.AnimationView;

import java.util.HashMap;
import java.util.Map;

import com.aurora.AnimationView.AnimationState.OnHourGlassAnimationCompleteListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
public  class HourGlassEndAnimationState extends AnimationStateBase {	
    
    
	Bitmap b;
	
	public  HourGlassEndAnimationState(Context context, Handler h,AuroraHourGlassView view) {
		super(context,h, view);
		mStepNumber = 0;
		int resID = mContext.getResources().getIdentifier("hourglass_disappear_01", "drawable", "com.android.deskclock"); 
	    b = BitmapFactory.decodeResource(mContext.getResources(), resID); 
	}
	
	public  void onDraw(Canvas canvas) {
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG));
		canvas.drawBitmap(b, 0, 0, null);
	
	}

	
	public void startAnimation(OnHourGlassAnimationCompleteListener listener) {
		mListener = listener;
		mHandler.post(mRunnable);
	}
	
	public void cancelAnimation() {
		mHandler.removeCallbacks(mRunnable);
		if(b != null) {
			b.recycle();
			b = null;
		}
	}
	
    Runnable mRunnable = new Runnable() {
		public void run() {      	    
			mStepNumber+=2;	 
			mView.invalidate();
			if(mStepNumber<60) {
				mHandler.postDelayed(this , 15);
				String index;
				if(mStepNumber<10) {
					index="0"+ mStepNumber;
				} else {
					index= String.valueOf(mStepNumber);
				}
				int resID = mContext.getResources().getIdentifier("hourglass_disappear_" + index, "drawable", "com.android.deskclock");
				if(b != null) {
					b.recycle();
					b = null;
				}
			    b = BitmapFactory.decodeResource(mContext.getResources(), resID); 
			} else {
				if(mListener!=null) {
					mListener.onHourGlassAnimationComplete();
				}
			}
		}
    };

}

