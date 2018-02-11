package com.aurora.AnimationView;

import java.util.HashMap;
import java.util.Map;

import com.android.deskclock.R;
import com.aurora.AnimationView.AnimationState.OnHourGlassAnimationCompleteListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;
import android.util.Log;
public  class InitAnimationState extends AnimationStateBase {	

	Bitmap b;
	
	public  InitAnimationState(Context context, Handler h,AuroraHourGlassView view) {
		super(context,h, view);
		mStepNumber = 0;
		int resID = mContext.getResources().getIdentifier("hourglass_enter_01", "drawable", "com.android.deskclock"); 
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
				int resID = mContext.getResources().getIdentifier("hourglass_enter_" + index, "drawable", "com.android.deskclock"); 
			    b = BitmapFactory.decodeResource(mContext.getResources(), resID); 
			} else {
				if(mListener!=null) {
					mListener.onHourGlassAnimationComplete();
				}
			}
		}
    };
    

}

