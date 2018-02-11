package com.aurora.worldtime;


import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import com.android.deskclock.AnalogClock;
import com.android.deskclock.Log;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.View;

/**
 * 自定义帧动画，防止在加载过大过多图片时出现内存溢出的情况
 */
public class WorldTimeEnterFrameAnimation {

    private Handler handler;
    private Handler handler2;
    private View view;
    private AnalogClock analogClock;
    private AnimationEndListener animationImageListener;

    private int[] durations;
    private int frameCount;
    
    private boolean isRun;
    private boolean isRun2;
    private boolean fillAfter;
    private int currentFrame;
    private int currentFrame2;  
    
    private int animImgs[];
    final int each_img_during_time = 10;
    
    private InputStream is = null;
    private BitmapFactory.Options opts = new BitmapFactory.Options();
	private Drawable[] drawable;
	private LoadDrawableThread loadDrawableThead;
	private boolean isFirstLoadDrawable = true;
	  
    
    public Runnable nextFrameRun = new Runnable() {
        public void run() {
            if(!isRun) {
                end();
                return;
            }
            nextFrame();
        }
    };
    public Runnable nextFrameRun2 = new Runnable() {
        public void run() {
            if(!isRun2) {
                end2();
                return;
            }
            nextFrame2();
        }
    };
    

    

    public WorldTimeEnterFrameAnimation(View view, int animImgs[]) {
        if(view == null) {
            throw new NullPointerException("target view is null");
        }
        
        if(animImgs == null || animImgs.length ==0){
        	throw new NullPointerException("animImgs is null");
        }
        
        this.drawable= new Drawable[animImgs.length]; 
        
        this.view = view;
        this.analogClock = (AnalogClock)view;
        this.animImgs = animImgs;
        this.handler = new Handler();
        this.handler2 = new Handler();
        
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
		opts.inPurgeable = true;
		opts.inInputShareable = true;
		opts.inSampleSize = 2;
		
        init();
    }
    
    private void init() {
        this.frameCount = animImgs.length;
        this.durations = new int[frameCount];
        this.isRun = false;
        this.isRun2 = false;
        this.fillAfter = true;
        
        for(int i = 0; i < frameCount; i++) {
            durations[i] = each_img_during_time;
        }
    }
    
    public void start() {
    	Log.e("----isRun = " + isRun);
        if(isRun) {
            return;
        }
        if(isRun2) {
            return;
        }
        this.isRun = true;
        this.isRun2 = true;
        this.currentFrame = -1;
        this.currentFrame2 = -1;
        analogClock.setDialNotNeed(true);
//        if(animationImageListener != null) {
//            animationImageListener.onAnimationStart();
//        }
        startGetDrawable( );
        //优化表针动画，把表针动画和背景动画分开，  nextFrame2();
        nextFrame2();
        nextFrame();
     
      
    }
   
    
    private final class LoadDrawableThread extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			for( int i = 3; i < animImgs.length; i++ ) {

				if ( drawable[i] == null ) {
			        try {
			        	if ( i == frameCount - 1 ) {
			        		opts.inSampleSize = 1;
			        	}
						is = view.getResources().openRawResource(animImgs[i]);
						drawable[i] = Drawable.createFromResourceStream(
				        		view.getResources(), null, is, "src", opts);	
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
    }
    
    /**
     * 新开线程加载图片
     */
    private void startGetDrawable( ) {
    	if ( loadDrawableThead == null ) {
    		loadDrawableThead = new LoadDrawableThread();
    	}
    	if ( isFirstLoadDrawable ) {
    		Log.e("-----first  startGetDrawable---------");
    		loadDrawableThead.start();
    		isFirstLoadDrawable = false;
    	}
    }
    
    public void stop() {
        this.isRun = false;
        this.isRun2 = false;
    }    
    private void end() {
        if(!fillAfter && frameCount > 0) {
            view.setBackground(null);
        }
        if(animationImageListener != null) {
            animationImageListener.onAnimationEnd();
        }
        this.isRun = false;
    }
    private void end2() {
        if(!fillAfter && frameCount > 0) {
            view.setBackground(null);
        }
        if(animationImageListener != null) {
            animationImageListener.onAnimationEnd();
        }
        this.isRun2 = false;
    }

    private void nextFrame() {
        if(currentFrame == frameCount - 1) {
        	end();
            return;
        }
        
        currentFrame ++;
        
        changeFrame(currentFrame);
     
       handler.postDelayed(nextFrameRun, durations[currentFrame]);
    }
    private void nextFrame2() {
        if(currentFrame2 == frameCount - 1) {
        	end2();
            return;
        }
        
        currentFrame2 ++;
        
        changeFrame2(currentFrame2);
     
       handler2.post(nextFrameRun2);
    }
   
    
    public void stopHandler( ) {
    	this.isRun = false;
    	this.isRun2 = false;
    	handler.removeCallbacks(nextFrameRun);
    	handler2.removeCallbacks(nextFrameRun2);
    	
    }
   

    private void changeFrame(int frameIndex) {
		if ( drawable[frameIndex] == null ) {
	        try {
	        	if ( frameIndex == frameCount - 1 ) {
	        		opts.inSampleSize = 1;
	        	}
				is = view.getResources().openRawResource(animImgs[frameIndex]);
				drawable[frameIndex] = Drawable.createFromResourceStream(
		        		view.getResources(), null, is, "src", opts);	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		view.setBackground(drawable[frameIndex]);
     //   analogClock.setHourAndMinuteAnim(frameCount - 1 - frameIndex, true);
    }
    private void changeFrame2(int frameIndex) {
	
		
        analogClock.setHourAndMinuteAnim(frameCount - 1 - frameIndex, true);
    }

    public boolean isFillAfter() {
        return fillAfter;
    }

    public void setFillAfter(boolean fillAfter) {
        this.fillAfter = fillAfter;
    }

    public void setAnimationImageListener(AnimationEndListener animationImageListener) {
        this.animationImageListener = animationImageListener;
    }
}
