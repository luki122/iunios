package com.aurora.note.activity.record;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * 给指定的view播放指定的图片序列，模拟帧动画，但不用一次把所有的帧图片都加载到内存中
 * @author JimXia
 * 2014年7月10日 上午11:12:43
 */
public class FrameAnimationUtil {
    private static final String TAG = "FrameAnimationUtil";
    
    private static final int ANIMATION_DURATION = 25; // 40 frame per second
    
    private final Handler mHandler = new Handler();
    
    private int mAnimationDrawableResIds[];
    private int mAnimationIndex = Integer.MIN_VALUE;
    private boolean mOneShot = false; // 从第一帧播放到最后一帧，不循环播放
    private boolean mReverse = false; // 从最后一帧放到第一帧
    
    private WeakReference<View> mAnimationView;
    
    private FrameAnimationListener mAnimationListener;
    
    public FrameAnimationUtil(int[] frameAnimationResIds, boolean oneShot, boolean reverse, View animationView) {
        mAnimationDrawableResIds = frameAnimationResIds;
        mOneShot = oneShot;
        mReverse = reverse;
        mAnimationView = new WeakReference<View>(animationView);
    }
    
    public void setAnimationListener(FrameAnimationListener listener) {
        mAnimationListener = listener;
    }
    
    public void startAnimation() {
        if (mAnimationDrawableResIds != null && mAnimationDrawableResIds.length > 0) {
            nextFrame();
        }
    }
    
    public void stopAnimation() {
        if (mAnimationIndex != Integer.MIN_VALUE) {
            mHandler.removeCallbacks(mAnimationRunnable);
            mAnimationIndex = Integer.MIN_VALUE;
        }
    }
    
    public void reset() {
        mAnimationIndex = Integer.MIN_VALUE;
    }
    
    private void nextFrame() {
        if (mOneShot && ((!mReverse && mAnimationIndex > mAnimationDrawableResIds.length - 1) ||
                (mReverse && (mAnimationIndex != Integer.MIN_VALUE && mAnimationIndex < 0)))) {
            if (mAnimationListener != null) {
                mAnimationListener.onAnimationEnd();
            }
            return;
        }        
        
        if (mAnimationIndex < 0) {
            if (!mReverse) {
                mAnimationIndex = 0;
            } else {
                mAnimationIndex = mAnimationDrawableResIds.length - 1;
            }
        }
        
        if (mAnimationIndex > mAnimationDrawableResIds.length - 1) {
            mAnimationIndex = 0;
        }
        
        View animationView = mAnimationView.get();
        if (animationView != null) {
//            long beginTime;
            if (animationView instanceof ImageView) {
                ImageView iv = (ImageView) animationView;
//                beginTime = System.currentTimeMillis();
                iv.setImageResource(mAnimationDrawableResIds[mAnimationIndex]);
//                Log.d(TAG, "Jim, setImageResource use time: " + (System.currentTimeMillis() - beginTime) +
//                        ", res id: " + mAnimationDrawableResIds[mAnimationIndex]);
            } else {
                animationView.setBackgroundResource(mAnimationDrawableResIds[mAnimationIndex]);
            }
            if (!mReverse) {
                mAnimationIndex ++;
            } else {
                mAnimationIndex --;
            }
            mHandler.postDelayed(mAnimationRunnable, ANIMATION_DURATION);
        }
    }
    
    private final Runnable mAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            long beginTime = System.currentTimeMillis();
            nextFrame();
            Log.d(TAG, "Jim, nextFrame use time: " + (System.currentTimeMillis() - beginTime));
        }
    };
    
    public static interface FrameAnimationListener {
        void onAnimationEnd();
    }
}
