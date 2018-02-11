package com.netmanage.animation;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;


/**
 * 罗府的方法，值得学习
 */
public class FrameAnimation {

    private Handler handler;
    private View view;
    private AnimationDrawable drawable;
    private AnimationImageListener animationImageListener;
    
    private FrameCallback[] callbacks;
    private Drawable[] frames;
    private int[] durations;
    private int frameCount;
    
    private boolean isRun;
    private boolean fillAfter;
    private boolean isOneShot;
    private boolean isLimitless;
    private int repeatTime;
    
    private int currentRepeat;
    private int currentFrame;
    private int currentTime;
    
    private Runnable nextFrameRun = new Runnable() {
        public void run() {
            if(!isRun) {
                end();
                return;
            }
            currentTime += durations[currentFrame];
            if(callbacks[currentFrame] != null) {
                callbacks[currentFrame].onFrameEnd(currentFrame);
            }
            nextFrame();
        }
    };

    public FrameAnimation(View view) {
        if(view == null) {
            throw new NullPointerException("target view is null");
        }
        if(view.getBackground() == null || !(view.getBackground() instanceof AnimationDrawable)) {
            throw new NotAnimationDrawableException("target view must has an background");
        }
        this.view = view;
        this.handler = new Handler();
        init();
    }
    
    private void init() {
        this.drawable = (AnimationDrawable) view.getBackground();
        this.frameCount = drawable.getNumberOfFrames();
        this.frames = new Drawable[frameCount];
        this.durations = new int[frameCount];
        this.callbacks = new FrameCallback[frameCount];
        this.isRun = false;
        this.fillAfter = false;
        this.isOneShot = true;
        this.isLimitless = false;
        this.repeatTime = 2;
        
        for(int i = 0; i < frameCount; i++) {
            frames[i] = drawable.getFrame(i);
            durations[i] = drawable.getDuration(i);
        }
    }
    
    public void start() {
        Log.e("luofu", "startAnimation");
        if(isRun) {
            return;
        }
        this.isRun = true;
        this.currentRepeat = -1;
        this.currentFrame = -1;
        this.currentTime = 0;
        if(animationImageListener != null) {
            Log.e("luofu", "onAnimationStart");
            animationImageListener.onAnimationStart();
        }
        startProcess();
    }
    
    public void stop() {
        this.isRun = false;
    }
    
    private void startProcess() {
        this.currentFrame = -1;
        this.currentTime = 0;
        this.currentRepeat ++;
        if(animationImageListener != null) {
            animationImageListener.onRepeat(currentRepeat);
        }
        nextFrame();
    }

    private void endProcess() {
        if(isOneShot || (!isLimitless && currentRepeat >= repeatTime - 1) || !isRun) {
            end();
        } else {
            startProcess();
        }
    }
    
    private void end() {
        if(!fillAfter && frameCount > 0) {
            view.setBackgroundDrawable(frames[0]);
        }
        if(animationImageListener != null) {
            animationImageListener.onAnimationEnd();
        }
        this.isRun = false;
    }

    private void nextFrame() {
        if(currentFrame == frameCount - 1) {
            endProcess();
            return;
        }
        
        currentFrame ++;
        
        changeFrame(currentFrame);
        
        handler.postDelayed(nextFrameRun, durations[currentFrame]);
    }

    private void changeFrame(int frameIndex) {
        view.setBackgroundDrawable(frames[frameIndex]);
        frames[frameIndex] = null;
        if(animationImageListener != null) {
            animationImageListener.onFrameChange(currentRepeat, frameIndex, currentTime);
        }
        
        if(callbacks[currentFrame] != null) {
            callbacks[currentFrame].onFrameStart(frameIndex);
        }
    }
    
    public int getSumDuration() {
        int sumDuration = 0;
        for(int duration : durations) {
            sumDuration += duration;
        }
        return sumDuration;
    }

    public boolean isOneShot() {
        return isOneShot;
    }

    public void setOneShot(boolean isOneShot) {
        this.isOneShot = isOneShot;
    }

    public boolean isFillAfter() {
        return fillAfter;
    }

    public void setFillAfter(boolean fillAfter) {
        this.fillAfter = fillAfter;
    }

    public boolean isLimitless() {
        return isLimitless;
    }

    public void setLimitless(boolean isLimitless) {
        if(isLimitless) {
            setOneShot(false);
        }
        this.isLimitless = isLimitless;
    }
    
    public void addFrameCallback(int index, FrameCallback callback) {
        this.callbacks[index] = callback;
    }

    public void setAnimationImageListener(AnimationImageListener animationImageListener) {
        this.animationImageListener = animationImageListener;
    }

    public int getRepeatTime() {
        return repeatTime;
    }

    public void setRepeatTime(int repeatTime) {
        this.repeatTime = repeatTime;
    }
    
    public interface AnimationImageListener{
        public void onAnimationStart();
        public void onAnimationEnd();
        public void onRepeat(int repeatIndex);
        public void onFrameChange(int repeatIndex, int frameIndex, int currentTime);
    }
    
    public interface FrameCallback {
        public void onFrameStart(int startTime);
        public void onFrameEnd(int endTime);
    }
}
