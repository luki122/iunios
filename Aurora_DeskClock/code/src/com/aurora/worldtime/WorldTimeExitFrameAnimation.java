package com.aurora.worldtime;


import java.io.InputStream;

import com.android.deskclock.AnalogClock;
import com.android.deskclock.Log;

import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;

/**
 * 自定义帧动画，防止在加载过大过多图片时出现内存溢出的情况
 */
public class WorldTimeExitFrameAnimation {

    private Handler handler;
    private View view;
    private AnalogClock analogClock;
    private AnimationEndListener animationImageListener;

    private int[] durations;
    private int frameCount;
    
    private boolean isRun;
    private boolean fillAfter;
    private int currentFrame;   
    private int animImgs[];
    final int each_img_during_time = 10;
    
    private InputStream is = null;
    private BitmapFactory.Options opts = new BitmapFactory.Options();
	private Drawable[] drawable;
	private LoadDrawableThread loadDrawableThead;
	private boolean isFirstLoadDrawable = true;
    
    private Runnable nextFrameRun = new Runnable() {
        public void run() {
            if(!isRun) {
                end();
                return;
            }
            nextFrame();
        }
    };

    public WorldTimeExitFrameAnimation(View view, int animImgs[]) {
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
        //this.fillAfter = true;
        
        for(int i = 0; i < frameCount; i++) {
            durations[i] = each_img_during_time;
        }
    }
    
    public void start() {
    	Log.e("----isRun = " + isRun);
        if(isRun) {
            return;
        }
        this.isRun = true;
        this.currentFrame = -1;
        analogClock.setDialNotNeed(true);
//        if(animationImageListener != null) {
//            animationImageListener.onAnimationStart();
//        }
        startGetDrawable( );
        nextFrame();
    }
    
    private final class LoadDrawableThread extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			for( int i = 3; i < animImgs.length; i++ ) {

				if ( drawable[i] == null ) {
			        try {
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

    private void nextFrame() {
        if(currentFrame == frameCount - 1) {
        	end();
            return;
        }
        
        currentFrame ++;
        
        changeFrame(currentFrame);
        
        handler.postDelayed(nextFrameRun, durations[currentFrame]);
    }

    private void changeFrame(int frameIndex) {
		if ( drawable[frameIndex] == null ) {
	        try {
				is = view.getResources().openRawResource(animImgs[frameIndex]);
				drawable[frameIndex] = Drawable.createFromResourceStream(
		        		view.getResources(), null, is, "src", opts);
		        
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		view.setBackground(drawable[frameIndex]);
        analogClock.setHourAndMinuteAnim(frameIndex, false);
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
