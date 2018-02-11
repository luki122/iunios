package com.aurora.worldtime;

import com.android.deskclock.Log;

import android.graphics.drawable.AnimationDrawable;

public class WorldTimeAnimationDrawable extends AnimationDrawable{
	
	
    public interface OnFrameAnimationCompleteListener {
        void onFrameAnimationComplete();
        void onFrameAnimationRun(int index);
    }
	int N;
	int index;
	OnFrameAnimationCompleteListener mListener;
	
    public WorldTimeAnimationDrawable() {
        super();
    }
	
    public void start(OnFrameAnimationCompleteListener listener) {
    	if ( isRunning() ) {
    		stop();
    	}
    	//Log.e("AuroraAnimationDrawable start");
    	index = 0;
    	super.start();
    	mListener = listener;
        N = getNumberOfFrames();
    	//Log.e("AuroraAnimationDrawable start n= " + N); 
    }
	
   public void run() {
		//Log.e("AuroraAnimationDrawable run"); 
        super.run();
        index ++;
		//Log.e("AuroraAnimationDrawable run index = " + index); 
        if(index==N) {
        	stop();
        	if(mListener != null) {
        		mListener.onFrameAnimationComplete();
        	}
        }
        if(mListener != null) {
    		mListener.onFrameAnimationRun(index);
    	}
   }
}