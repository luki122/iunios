package com.aurora.AnimationView;

import android.graphics.drawable.AnimationDrawable;

public class AuroraAnimationDrawable extends AnimationDrawable{
	
	
    public interface OnFrameAnimationCompleteListener {
        void onFrameAnimationComplete();
    }
	int N;
	int index;
	OnFrameAnimationCompleteListener mListener;
	
    public AuroraAnimationDrawable() {
        super();
    }
	
    public void start(OnFrameAnimationCompleteListener listener) {
    	com.android.deskclock.Log.i("AuroraAnimationDrawable start");
    	index = 0;
    	super.start();
    	mListener = listener;
        N = getNumberOfFrames();
    	com.android.deskclock.Log.i("AuroraAnimationDrawable start n= " + N); 
    }
	
   public void run() {
		com.android.deskclock.Log.i("AuroraAnimationDrawable run"); 
        super.run();
        index ++;
		com.android.deskclock.Log.i("AuroraAnimationDrawable run index = " + index); 
        if(index==N) {
        	stop();
        	if(mListener != null) {
        		mListener.onFrameAnimationComplete();
        	}
        }
   }
}