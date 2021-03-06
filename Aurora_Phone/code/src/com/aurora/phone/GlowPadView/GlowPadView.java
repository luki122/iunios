/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.phone;

import android.animation.*;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.animation.TypeEvaluator;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import com.android.internal.R;

import java.util.ArrayList;

import android.os.Build;
import android.os.Handler;
import android.view.animation.*;

/**
 * A re-usable widget containing a center, outer ring and wave animation.
 */
public class GlowPadView extends View {
    private static final String TAG = "GlowPadView";
    private static final boolean DEBUG = false;
    
//	static boolean mIsUseNewUI = Build.VERSION.SDK_INT <= 19;
	static boolean mIsUseNewUI = false;

    // Wave state machine
    private static final int STATE_IDLE = 0;
    private static final int STATE_START = 1;
    private static final int STATE_FIRST_TOUCH = 2;
    private static final int STATE_TRACKING = 3;
    private static final int STATE_SNAP = 4;
    private static final int STATE_FINISH = 5;
    
    private static int mState = STATE_IDLE;

    // Animation properties.
    private static final float SNAP_MARGIN_DEFAULT = 20.0f; // distance to ring before we snap to it

    public interface OnTriggerListener {
        int NO_HANDLE = 0;
        int CENTER_HANDLE = 1;
        public void onGrabbed(View v, int handle);
        public void onReleased(View v, int handle);
        public void onTrigger(View v, int target);
        public void onGrabbedStateChange(View v, int handle);
        public void onFinishFinalAnimation();
    }

    // Tuneable parameters for animation
    private static final int WAVE_ANIMATION_DURATION = 2000;
    private static final int RETURN_TO_HOME_DELAY = 1200;
    private static final int RETURN_TO_HOME_DURATION = 200;
//    private static final int HIDE_ANIMATION_DELAY = 200;
    private static final int HIDE_ANIMATION_DELAY = 0;
//    private static final int HIDE_ANIMATION_DURATION = 200;
    private static final int HIDE_ANIMATION_DURATION = 240;
    private static final int SHOW_ANIMATION_DURATION = 240;
    private static final int SHOW_ANIMATION_DELAY = 0;
    private static final int INITIAL_SHOW_HANDLE_DURATION = 200;
    private static final int REVEAL_GLOW_DELAY = 0;
    private static final int REVEAL_GLOW_DURATION = 0;

    private static final float TAP_RADIUS_SCALE_ACCESSIBILITY_ENABLED = 1.3f;
    private static final float TARGET_SCALE_EXPANDED = 1.0f;
    private static final float TARGET_SCALE_COLLAPSED = 0.8f;
    private static final float RING_SCALE_EXPANDED = 1.0f;
    private static final float RING_SCALE_COLLAPSED = 0.5f;

    private ArrayList<TargetDrawable> mTargetDrawables = new ArrayList<TargetDrawable>();
    private AnimationBundle mWaveAnimations = new AnimationBundle();
    private AnimationBundleV2 mWaveAnimationsV2 = new AnimationBundleV2();
    private AnimationBundle mTargetAnimations = new AnimationBundle();
    private AnimationBundle mGlowAnimations = new AnimationBundle();
    private ArrayList<String> mTargetDescriptions;
    private ArrayList<String> mDirectionDescriptions;
    private OnTriggerListener mOnTriggerListener;
    private CenterTargetDrawable mHandleDrawable;
    private TargetDrawable mOuterRing;
    private Vibrator mVibrator;

    private int mFeedbackCount = 3;
    private int mVibrationDuration = 0;
    private int mGrabbedState;
    private int mActiveTarget = -1;
    private float mGlowRadius;
    private float mGlowRadiusDiffer;
    private float mWaveCenterX;
    private float mWaveCenterY;
    private int mMaxTargetHeight;
    private int mMaxTargetWidth;
    private float mRingScaleFactor = 1f;
    private boolean mAllowScaling;

    private float mOuterRadius = 0.0f;
    private float mSnapMargin = 0.0f;
    private float mFirstItemOffset = 0.0f;
    private boolean mMagneticTargets = false;
    private boolean mDragging;
    private int mNewTargetResources;

    private class AnimationBundle extends ArrayList<Tweener> {
        private static final long serialVersionUID = 0xA84D78726F127468L;
        private boolean mSuspended;

        public void start() {
            if (mSuspended) return; // ignore attempts to start animations
            final int count = size();
            for (int i = 0; i < count; i++) {
                Tweener anim = get(i);
                anim.animator.start();
            }
        }

        public void cancel() {
            final int count = size();
            for (int i = 0; i < count; i++) {
                Tweener anim = get(i);
                anim.animator.cancel();
            }
            clear();
        }

        public void stop() {
            final int count = size();
            for (int i = 0; i < count; i++) {
                Tweener anim = get(i);
                anim.animator.end();
            }
            clear();
        }

        public void setSuspended(boolean suspend) {
            mSuspended = suspend;
        }
    };

    private AnimatorListener mResetListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animator) {
            switchToState(STATE_IDLE, mWaveCenterX, mWaveCenterY);
            dispatchOnFinishFinalAnimation();
        }
    };

    private AnimatorListener mResetListenerWithPing = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animator) {
            ping();
            switchToState(STATE_IDLE, mWaveCenterX, mWaveCenterY);
            dispatchOnFinishFinalAnimation();
        }
    };

    private AnimatorUpdateListener mUpdateListener = new AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator animation) {
            invalidate();
        }
    };

    private boolean mAnimatingTargets;
    private AnimatorListener mTargetUpdateListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animator) {
            if (mNewTargetResources != 0) {
                internalSetTargetResources(mNewTargetResources);
                mNewTargetResources = 0;
                hideTargets(false, false);
            }
            mAnimatingTargets = false;
        }
    };
    private int mTargetResourceId;
    private int mTargetDescriptionsResourceId;
    private int mDirectionDescriptionsResourceId;
    private boolean mAlwaysTrackFinger;
    private int mHorizontalInset;
    private int mVerticalInset;
    private int mGravity = Gravity.TOP;
    private boolean mInitialLayout = true;
    private Tweener mBackgroundAnimator;
    private PointCloud mPointCloud;
    private AuroraCircle[] AuroraCircle;
    private AuroraRing[] AuroraRing;
    private AuroraPoint AuroraPoint;
    private AuroraPointWhenTouch[] mAuroraPointWhenTouch;
    private AuroraPointWhenTouchInit AuroraPointWhenTouchInit;
    private float mInnerRadius;
    private int mPointerId;

    public GlowPadView(Context context) {
        this(context, null);
    }

    public GlowPadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = context.getResources();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GlowPadView);
        mInnerRadius = a.getDimension(R.styleable.GlowPadView_innerRadius, mInnerRadius);
        mOuterRadius = a.getDimension(R.styleable.GlowPadView_outerRadius, mOuterRadius);
        mSnapMargin = a.getDimension(R.styleable.GlowPadView_snapMargin, mSnapMargin);
        mFirstItemOffset = (float) Math.toRadians(
                a.getFloat(R.styleable.GlowPadView_firstItemOffset,
                        (float) Math.toDegrees(mFirstItemOffset)));
        mVibrationDuration = a.getInt(R.styleable.GlowPadView_vibrationDuration,
                mVibrationDuration);
        mFeedbackCount = a.getInt(R.styleable.GlowPadView_feedbackCount,
                mFeedbackCount);
        mAllowScaling = a.getBoolean(R.styleable.GlowPadView_allowScaling, false);
        TypedValue handle = a.peekValue(R.styleable.GlowPadView_handleDrawable);
        mHandleDrawable = new CenterTargetDrawable(res, handle != null ? handle.resourceId : 0);
        mHandleDrawable.setState(TargetDrawable.STATE_INACTIVE);
        mOuterRing = new TargetDrawable(res,
                getResourceId(a, R.styleable.GlowPadView_outerRingDrawable));

        mAlwaysTrackFinger = a.getBoolean(R.styleable.GlowPadView_alwaysTrackFinger, false);
        mMagneticTargets = a.getBoolean(R.styleable.GlowPadView_magneticTargets, mMagneticTargets);

        int pointId = getResourceId(a, R.styleable.GlowPadView_pointDrawable);
        Drawable pointDrawable = pointId != 0 ? res.getDrawable(pointId) : null;
        mGlowRadius = a.getDimension(R.styleable.GlowPadView_glowRadius, 0.0f);
        mGlowRadiusDiffer = context.getResources().getDimensionPixelSize(com.android.phone.R.dimen.aurora_glowpadview_target_radius_differ);
        

        TypedValue outValue = new TypedValue();

        // Read array of target drawables
        if (a.getValue(R.styleable.GlowPadView_targetDrawables, outValue)) {
            internalSetTargetResources(outValue.resourceId);
        }
        if (mTargetDrawables == null || mTargetDrawables.size() == 0) {
            throw new IllegalStateException("Must specify at least one target drawable");
        }

        // Read array of target descriptions
        if (a.getValue(R.styleable.GlowPadView_targetDescriptions, outValue)) {
            final int resourceId = outValue.resourceId;
            if (resourceId == 0) {
                throw new IllegalStateException("Must specify target descriptions");
            }
            setTargetDescriptionsResourceId(resourceId);
        }

        // Read array of direction descriptions
        if (a.getValue(R.styleable.GlowPadView_directionDescriptions, outValue)) {
            final int resourceId = outValue.resourceId;
            if (resourceId == 0) {
                throw new IllegalStateException("Must specify direction descriptions");
            }
            setDirectionDescriptionsResourceId(resourceId);
        }

        mGravity = a.getInt(R.styleable.GlowPadView_gravity, Gravity.TOP);

        a.recycle();

        setVibrateEnabled(mVibrationDuration > 0);

        assignDefaultsIfNeeded();

        mPointCloud = new PointCloud(pointDrawable);
        mPointCloud.makePointCloud(mInnerRadius, mOuterRadius);
        mPointCloud.glowManager.setRadius(mGlowRadius);
        AuroraCircle = new AuroraCircle[2];
        AuroraCircle[0] = new AuroraCircle();
        AuroraCircle[1] = new AuroraCircle();
        AuroraRing = new AuroraRing[2];
        AuroraRing[0]= new AuroraRing();
        AuroraRing[1]= new AuroraRing();
        AuroraPoint = new AuroraPoint(context); 
        mAuroraPointWhenTouch = new AuroraPointWhenTouch[3];
        for(int i = 0; i<3; i++) {
            mAuroraPointWhenTouch[i] = new AuroraPointWhenTouch(context, i); 
        }
        AuroraPointWhenTouchInit =  new AuroraPointWhenTouchInit(context);
        
        mSP = PhoneGlobals.getInstance().getApplicationContext()
                .getSharedPreferences("com.android.phone_preferences", Context.MODE_PRIVATE); 
        
        mInnerRingRadiusStart = context.getResources().getDimensionPixelSize(com.android.phone.R.dimen.aurora_glow_anim_internal_ring_radius_start);
        mInnerRingRadiusEnd = context.getResources().getDimensionPixelSize(com.android.phone.R.dimen.aurora_glow_anim_internal_ring_radius_end);
        mOutRingRadiusStart = context.getResources().getDimensionPixelSize(com.android.phone.R.dimen.aurora_glow_anim_out_ring_radius_start);
        mOutRingRadiusEnd = context.getResources().getDimensionPixelSize(com.android.phone.R.dimen.aurora_glow_anim_out_ring_radius_end);
        
        mTargetMoveStep1 = context.getResources().getDimensionPixelSize(com.android.phone.R.dimen.aurora_ringing_target_move_1);
        mTargetMoveStep2 = context.getResources().getDimensionPixelSize(com.android.phone.R.dimen.aurora_ringing_target_move_2);
        
        mPointMoveStep = context.getResources().getDimensionPixelSize(com.android.phone.R.dimen.aurora_point_move);
    }

    private int getResourceId(TypedArray a, int id) {
        TypedValue tv = a.peekValue(id);
        return tv == null ? 0 : tv.resourceId;
    }

    private void dump() {
        Log.v(TAG, "Outer Radius = " + mOuterRadius);
        Log.v(TAG, "SnapMargin = " + mSnapMargin);
        Log.v(TAG, "FeedbackCount = " + mFeedbackCount);
        Log.v(TAG, "VibrationDuration = " + mVibrationDuration);
        Log.v(TAG, "GlowRadius = " + mGlowRadius);
        Log.v(TAG, "WaveCenterX = " + mWaveCenterX);
        Log.v(TAG, "WaveCenterY = " + mWaveCenterY);
    }

    public void suspendAnimations() {
        mWaveAnimations.setSuspended(true);
        mWaveAnimationsV2.setSuspended(true);
        mTargetAnimations.setSuspended(true);
        mGlowAnimations.setSuspended(true);
    }

    public void resumeAnimations() {
        mWaveAnimations.setSuspended(false);
        mWaveAnimationsV2.setSuspended(false);
        mTargetAnimations.setSuspended(false);
        mGlowAnimations.setSuspended(false);
        mWaveAnimations.start();
        mTargetAnimations.start();
        mGlowAnimations.start();
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        // View should be large enough to contain the background + handle and
        // target drawable on either edge.
        return (int) (Math.max(mOuterRing.getWidth(), 2 * mOuterRadius) + mMaxTargetWidth);
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        // View should be large enough to contain the unlock ring + target and
        // target drawable on either edge
        return (int) (Math.max(mOuterRing.getHeight(), 2 * mOuterRadius) + mMaxTargetHeight);
    }

    /**
     * This gets the suggested width accounting for the ring's scale factor.
     */
    protected int getScaledSuggestedMinimumWidth() {
        return (int) (mRingScaleFactor * Math.max(mOuterRing.getWidth(), 2 * mOuterRadius)
                + mMaxTargetWidth);
    }

    /**
     * This gets the suggested height accounting for the ring's scale factor.
     */
    protected int getScaledSuggestedMinimumHeight() {
        return (int) (mRingScaleFactor * Math.max(mOuterRing.getHeight(), 2 * mOuterRadius)
                + mMaxTargetHeight);
    }

    private int resolveMeasured(int measureSpec, int desired)
    {
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(specSize, desired);
                break;
            case MeasureSpec.EXACTLY:
            default:
                result = specSize;
        }
        return result;
    }

    private void switchToState(int state, float x, float y) {
    	mState = state;
    	mWhenTouchListener.whenTouch(isTouchState());
        switch (state) {
            case STATE_IDLE:
                deactivateTargets();
                hideGlow(0, 0, 0.0f, null);
                startBackgroundAnimation(0, 0.0f);
                mHandleDrawable.setState(TargetDrawable.STATE_INACTIVE);
                mHandleDrawable.setAlpha(1.0f);
                break;

            case STATE_START:
                startBackgroundAnimation(0, 0.0f);
                break;

            case STATE_FIRST_TOUCH:
//                mHandleDrawable.setAlpha(0.0f);
                deactivateTargets();
                showTargets(true);
                startBackgroundAnimation(INITIAL_SHOW_HANDLE_DURATION, 1.0f);
                setGrabbedState(OnTriggerListener.CENTER_HANDLE);
                if (AccessibilityManager.getInstance(mContext).isEnabled()) {
                    announceTargets();
                }
                break;

            case STATE_TRACKING:
                mHandleDrawable.setAlpha(0.0f);
                showGlow(REVEAL_GLOW_DURATION , REVEAL_GLOW_DELAY, 1.0f, null);
                break;

            case STATE_SNAP:
                // TODO: Add transition states (see list_selector_background_transition.xml)
                mHandleDrawable.setAlpha(0.0f);
                showGlow(REVEAL_GLOW_DURATION , REVEAL_GLOW_DELAY, 0.0f, null);
                break;

            case STATE_FINISH:
                doFinish();
                break;
        }
    }

    private void showGlow(int duration, int delay, float finalAlpha,
            AnimatorListener finishListener) {
        mGlowAnimations.cancel();
        mGlowAnimations.add(Tweener.to(mPointCloud.glowManager, duration,
                "ease", Ease.Cubic.easeIn,
                "delay", delay,
                "alpha", finalAlpha,
                "onUpdate", mUpdateListener,
                "onComplete", finishListener));
        mGlowAnimations.start();
    }

    private void hideGlow(int duration, int delay, float finalAlpha,
            AnimatorListener finishListener) {
        mGlowAnimations.cancel();
        mGlowAnimations.add(Tweener.to(mPointCloud.glowManager, duration,
                "ease", Ease.Quart.easeOut,
                "delay", delay,
                "alpha", finalAlpha,
                "x", 0.0f,
                "y", 0.0f,
                "onUpdate", mUpdateListener,
                "onComplete", finishListener));
        mGlowAnimations.start();
    }

    private void deactivateTargets() {
        final int count = mTargetDrawables.size();
        for (int i = 0; i < count; i++) {
            TargetDrawable target = mTargetDrawables.get(i);
            target.setState(TargetDrawable.STATE_INACTIVE);
        }
        mActiveTarget = -1;
    }

    /**
     * Dispatches a trigger event to listener. Ignored if a listener is not set.
     * @param whichTarget the target that was triggered.
     */
    private void dispatchTriggerEvent(int whichTarget) {
        vibrate();
        if (mOnTriggerListener != null) {
            mOnTriggerListener.onTrigger(this, whichTarget);
        }
    }

    private void dispatchOnFinishFinalAnimation() {
        if (mOnTriggerListener != null) {
            mOnTriggerListener.onFinishFinalAnimation();
        }
    }

    private void doFinish() {
        final int activeTarget = mActiveTarget;
        final boolean targetHit =  activeTarget != -1;

        if (targetHit) {
            if (DEBUG) Log.v(TAG, "Finish with target hit = " + targetHit);

            highlightSelected(activeTarget);

            // Inform listener of any active targets.  Typically only one will be active.
            hideGlow(RETURN_TO_HOME_DURATION, RETURN_TO_HOME_DELAY, 0.0f, mResetListener);
            dispatchTriggerEvent(activeTarget);
            if (!mAlwaysTrackFinger) {
                // Force ring and targets to finish animation to final expanded state
                mTargetAnimations.stop();
            }
        } else {
            // Animate handle back to the center based on current state.
            hideGlow(HIDE_ANIMATION_DURATION, 0, 0.0f, mResetListenerWithPing);
            hideTargets(true, false);
        }

        setGrabbedState(OnTriggerListener.NO_HANDLE);
    }

    private void highlightSelected(int activeTarget) {
        // Highlight the given target and fade others
        mTargetDrawables.get(activeTarget).setState(TargetDrawable.STATE_ACTIVE);
        hideUnselected(activeTarget);
    }

    private void hideUnselected(int active) {
        for (int i = 0; i < mTargetDrawables.size(); i++) {
            if (i != active) {
                mTargetDrawables.get(i).setAlpha(0.0f);
            }
        }
    }

    private void hideTargets(boolean animate, boolean expanded) {
        mTargetAnimations.cancel();
        updateTargetPositions(mWaveCenterX, mWaveCenterY);
        // Note: these animations should complete at the same time so that we can swap out
        // the target assets asynchronously from the setTargetResources() call.
        mAnimatingTargets = animate;
        final int duration = animate ? HIDE_ANIMATION_DURATION : 0;
        final int delay = animate ? HIDE_ANIMATION_DELAY : 0;

        final float targetScale = expanded ?
                TARGET_SCALE_EXPANDED : TARGET_SCALE_COLLAPSED;
        final int length = mTargetDrawables.size();
        final TimeInterpolator interpolator = Ease.Cubic.easeOut;
        for (int i = 0; i < length; i++) {
            TargetDrawable target = mTargetDrawables.get(i);
            target.setAlpha(1.0f);
            target.setState(TargetDrawable.STATE_INACTIVE);
            mTargetAnimations.add(Tweener.to(target, duration,
                    "ease", interpolator,
                    "alpha", 0.0f,
                    "scaleX", targetScale,
                    "scaleY", targetScale,
                    "delay", delay,
                    "onUpdate", mUpdateListener));
        }

//        float ringScaleTarget = expanded ?
//                RING_SCALE_EXPANDED : RING_SCALE_COLLAPSED;
//        ringScaleTarget *= mRingScaleFactor;
//        mTargetAnimations.add(Tweener.to(mOuterRing, duration,
//                "ease", interpolator,
//                "alpha", 0.0f,
//                "scaleX", ringScaleTarget,
//                "scaleY", ringScaleTarget,
//                "delay", delay,
//                "onUpdate", mUpdateListener));
        
        AuroraPoint.setAlpha(0.0f);
        mTargetAnimations.add(Tweener.to(AuroraPoint, duration,
                "ease", mShowTargetPathInterpolator,
                "alpha", new float[]{0.0f, 1.0f},
                "delay", delay,
                "onUpdate", mUpdateListener,
                "onComplete", mTargetUpdateListener));

        mTargetAnimations.start();
    }

    private final PathInterpolator mShowTargetPathInterpolator = new PathInterpolator(0.2f, 1, 0, 1);
    private final PathInterpolator mShowTargetPositionPathInterpolator = new PathInterpolator(0.6f, 0, 0.4f, 1);
    private int  mTargetMoveStep1;
    private int  mTargetMoveStep2;
    private void showTargets(boolean animate) {
        mTargetAnimations.stop();
        mAnimatingTargets = animate;
        final int delay = animate ? SHOW_ANIMATION_DELAY : 0;
        final int duration = animate ? SHOW_ANIMATION_DURATION : 0;
     

        float ringScale = mRingScaleFactor * RING_SCALE_EXPANDED;
        mTargetAnimations.add(Tweener.to(mOuterRing, duration,
                "ease", Ease.Cubic.easeOut,
                "alpha", 1.0f,
                "scaleX", ringScale,
                "scaleY", ringScale,
                "delay", delay,
      "onUpdate", mUpdateListener,
                "onComplete", mTargetUpdateListener));
        
        //1&&2 
       Tweener centerhideTweener = Tweener.to(mHandleDrawable, duration,
                "ease", mShowTargetPathInterpolator,
                "alpha", new float[]{1.0f, 0.0f},
                "delay", delay,
                "onUpdate", mUpdateListener,
                "onComplete",
                new AnimatorListenerAdapter() {
			        	public void onAnimationStart(Animator animator) {
			        		mHandleDrawable.setAlpha(1.0f);  
						}
	        	
                    public void onAnimationEnd(Animator animator) {
                		mHandleDrawable.setAlpha(0.0f);  
                    }
                });  
     	mTargetAnimations.add(centerhideTweener);
     	
     	//3.
     	mTargetAnimations.add(Tweener.to(AuroraRing[0].waveManager, duration,
                "ease", mShowTargetPathInterpolator,
                "delay", delay,
                "alpha", new float[]{1.0f, 0},
                "radius", new float[]{mInnerRingRadiusEnd ,mInnerRingRadiusEnd*0.7f},
                "radius2", new float[]{mOutRingRadiusEnd ,mOutRingRadiusEnd * 0.7f},
                "onUpdate", mUpdateListener,
                "onComplete",
                new AnimatorListenerAdapter() {
        			public void onAnimationStart(Animator animator) {
        		    	AuroraRing[0].waveManager.setAlpha(1.0f);
        		        AuroraRing[0].waveManager.setRadius(mInnerRingRadiusEnd);
        		        AuroraRing[0].waveManager.setRadius2(mOutRingRadiusEnd);
        			}
        	
                    public void onAnimationEnd(Animator animator) {
                       	AuroraRing[0].waveManager.setRadius(0.0f);
                      	AuroraRing[0].waveManager.setRadius2(0.0f);
                    	AuroraRing[0].waveManager.setAlpha(0.0f);
                    }
                }));     
           
           //4  
                Tweener centerPointShowTweener = Tweener.to(mPointCloud.waveManager, duration,
                        "ease", new BounceInterpolator(),
                        "alpha", new float[]{0.0f, 1.0f},
                        "delay", delay,
                        "onUpdate", mUpdateListener,
                        "onComplete",
                        new AnimatorListenerAdapter() {
        			        	public void onAnimationStart(Animator animator) {
        			        		mPointCloud.waveManager.setAlpha(0.0f);  
        						}
        	        	
                            public void onAnimationEnd(Animator animator) {
                            	mPointCloud.waveManager.setAlpha(1.0f);  
                            }
                        });  
             	mTargetAnimations.add(centerPointShowTweener);
                
             //5... 7..
                mTargetAnimations.add(Tweener.to(AuroraPoint, duration,
                        "ease", mShowTargetPathInterpolator,
                        "alpha", new float[]{1.0f, 0.0f},
                        "delay", delay,
                        "onUpdate", mUpdateListener,
                        "onComplete", new AnimatorListenerAdapter() {
    			        	public void onAnimationStart(Animator animator) {
    			        		AuroraPoint.setAlpha(1.0f);  
    						}
    	        	
                        public void onAnimationEnd(Animator animator) {
                        	AuroraPoint.setAlpha(0.0f);  
                        }
                    }));

                
                
                //6 & 8
                final int length = mTargetDrawables.size();
                for (int i = 0; i < length; i++) {
                    final TargetDrawable target = mTargetDrawables.get(i);
                    target.setState(TargetDrawable.STATE_INACTIVE); 
                    target.setAlpha(1.0f);
                    mTargetAnimations.add(Tweener.to(target, duration,
                            "ease", mShowTargetPathInterpolator,
                            "scaleX", new float[]{0.0f, 1.0f},
                            "scaleY", new float[]{0.0f, 1.0f}, 
                            "Y", new float[]{ target.getY(), target.getY() * 1.2f},
                            "delay", delay,
                            "onUpdate", mUpdateListener,
                            "onComplete",
                            new AnimatorListenerAdapter() {
			                    	public void onAnimationStart(Animator animator) {
			                    		target.setScaleX(0.0f);  
			                    		target.setScaleY(0.0f);  
									}
			                    	
			                        public void onAnimationEnd(Animator animator) {
			                        	target.setScaleX(1.0f);  
			                    		target.setScaleY(1.0f);  
			                    	   	Tweener SecondPositionTweener = Tweener.to(target, duration/2,
	                                            "ease", mShowTargetPositionPathInterpolator,
	                                            "Y", new float[]{ target.getY() , target.getY() * 0.92f},
	                                            "delay", delay,
	                                            "onUpdate", mUpdateListener);
	                                	 mTargetAnimations.add(SecondPositionTweener);                    
	                                	  SecondPositionTweener.animator.start();
			                        }
                            }));
                            
                }
                
        
              //9 &10 
//        AuroraPointWhenTouchInit.mPointManager.setAlpha(0);
//        Tweener pointTweener = Tweener.to(AuroraPointWhenTouchInit.mPointManager, (long)(duration * 1.2),
//                "ease", Ease.Linear.easeNone,
//                "delay", delay,
//                "alpha", 3f,
//                "onUpdate", mUpdateListener,
//                "onComplete",
//                new AnimatorListenerAdapter() {
//                    public void onAnimationEnd(Animator animator) {
//                    	AuroraPointWhenTouchInit.mPointManager.setAlpha(0);
//                    	mWaveAnimations.cancel();
//                    	ping();
//                    }
//                });
//        pointTweener.animator.setRepeatCount(ValueAnimator.INFINITE);
//        mTargetAnimations.add(pointTweener);    
        startInitAnimationWhenTouchV2() ;

        mTargetAnimations.start();
    }

    private void vibrate() {
        final boolean hapticEnabled = Settings.System.getIntForUser(
                mContext.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 1,
                UserHandle.USER_CURRENT) != 0;
        if (mVibrator != null && hapticEnabled) {
            mVibrator.vibrate(mVibrationDuration);
        }
    }

    private ArrayList<TargetDrawable> loadDrawableArray(int resourceId) {
        Resources res = getContext().getResources();
        TypedArray array = res.obtainTypedArray(resourceId);
        final int count = array.length();
        ArrayList<TargetDrawable> drawables = new ArrayList<TargetDrawable>(count);
        for (int i = 0; i < count; i++) {
            TypedValue value = array.peekValue(i);
            TargetDrawable target = new TargetDrawable(res, value != null ? value.resourceId : 0);
            drawables.add(target);
        }
        array.recycle();
        return drawables;
    }

    private void internalSetTargetResources(int resourceId) {
        final ArrayList<TargetDrawable> targets = loadDrawableArray(resourceId);
        mTargetDrawables = targets;
        mTargetResourceId = resourceId;

        int maxWidth = mHandleDrawable.getWidth();
        int maxHeight = mHandleDrawable.getHeight();
        final int count = targets.size();
        for (int i = 0; i < count; i++) {
            TargetDrawable target = targets.get(i);
            maxWidth = Math.max(maxWidth, target.getWidth());
            maxHeight = Math.max(maxHeight, target.getHeight());
        }
        if (mMaxTargetWidth != maxWidth || mMaxTargetHeight != maxHeight) {
            mMaxTargetWidth = maxWidth;
            mMaxTargetHeight = maxHeight;
            requestLayout(); // required to resize layout and call updateTargetPositions()
        } else {
            updateTargetPositions(mWaveCenterX, mWaveCenterY);
            updatePointCloudPosition(mWaveCenterX, mWaveCenterY);
        }
    }

    /**
     * Loads an array of drawables from the given resourceId.
     *
     * @param resourceId
     */
    public void setTargetResources(int resourceId) {
        if (mAnimatingTargets) {
            // postpone this change until we return to the initial state
            mNewTargetResources = resourceId;
        } else {
            internalSetTargetResources(resourceId);
        }
    }

    public int getTargetResourceId() {
        return mTargetResourceId;
    }

    /**
     * Sets the resource id specifying the target descriptions for accessibility.
     *
     * @param resourceId The resource id.
     */
    public void setTargetDescriptionsResourceId(int resourceId) {
        mTargetDescriptionsResourceId = resourceId;
        if (mTargetDescriptions != null) {
            mTargetDescriptions.clear();
        }
    }

    /**
     * Gets the resource id specifying the target descriptions for accessibility.
     *
     * @return The resource id.
     */
    public int getTargetDescriptionsResourceId() {
        return mTargetDescriptionsResourceId;
    }

    /**
     * Sets the resource id specifying the target direction descriptions for accessibility.
     *
     * @param resourceId The resource id.
     */
    public void setDirectionDescriptionsResourceId(int resourceId) {
        mDirectionDescriptionsResourceId = resourceId;
        if (mDirectionDescriptions != null) {
            mDirectionDescriptions.clear();
        }
    }

    /**
     * Gets the resource id specifying the target direction descriptions.
     *
     * @return The resource id.
     */
    public int getDirectionDescriptionsResourceId() {
        return mDirectionDescriptionsResourceId;
    }

    /**
     * Enable or disable vibrate on touch.
     *
     * @param enabled
     */
    public void setVibrateEnabled(boolean enabled) {
        if (enabled && mVibrator == null) {
            mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        } else {
            mVibrator = null;
        }
    }

    /**
     * Starts wave animation.
     *
     */
    public void ping() {
        if (mFeedbackCount > 0) {
            boolean doWaveAnimation = true;
            final AnimationBundle waveAnimations = mWaveAnimations;

            // Don't do a wave if there's already one in progress
            if (waveAnimations.size() > 0 && waveAnimations.get(0).animator.isRunning()) {
                long t = waveAnimations.get(0).animator.getCurrentPlayTime();
                if (t < WAVE_ANIMATION_DURATION/2) {
                    doWaveAnimation = false;
                }
            }

            if (doWaveAnimation) {
            	if(isTouchState() && mDragging) {
            		startWaveAnimationWhenTouchV2();
            	} else {
            		startWaveAnimationV2();
            	}
            }
        }
    }

    private void stopAndHideWaveAnimation() {
        mWaveAnimations.cancel();
        mWaveAnimationsV2.cancel();
        mPointCloud.waveManager.setAlpha(0.0f);
        AuroraCircle[0].waveManager.setAlpha(0.0f);
        AuroraCircle[1].waveManager.setAlpha(0.0f);
        AuroraRing[0].waveManager.setAlpha(0.0f);
        AuroraRing[1].waveManager.setAlpha(0.0f);
    }

    private void startWaveAnimation() {
        mWaveAnimations.cancel();
        AuroraCircle[0].waveManager.setAlpha(0.1f);
//        mPointCloud.waveManager.setRadius(mHandleDrawable.getWidth()/2.0f);
        AuroraCircle[0].waveManager.setRadius(2.0f * mInnerRadius);
        mWaveAnimations.add(Tweener.to(AuroraCircle[0].waveManager, (long)(WAVE_ANIMATION_DURATION * 0.75),
                "ease", Ease.Linear.easeNone,
                "delay", 0,
                "alpha", 0.0f,
                "radius", 3.0f * mOuterRadius,
                "onUpdate", mUpdateListener,
                "onComplete",
                new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                       	AuroraCircle[0].waveManager.setRadius(0.0f);
                    	AuroraCircle[0].waveManager.setAlpha(0.0f);
                    }
                }));
        
        
        AuroraCircle[1].waveManager.setAlpha(0.1f);
        AuroraCircle[1].waveManager.setRadius(2.0f * mInnerRadius);
        mWaveAnimations.add(Tweener.to(AuroraCircle[1].waveManager, (long)(WAVE_ANIMATION_DURATION * 0.75),
                "ease", Ease.Linear.easeNone,
                "delay", (long)WAVE_ANIMATION_DURATION * 0.25,
                "alpha", 0.0f,
                "radius", 3.0f * mOuterRadius,
                "onUpdate", mUpdateListener,
                "onComplete",
                new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                       	AuroraCircle[1].waveManager.setRadius(0.0f);
                    	AuroraCircle[1].waveManager.setAlpha(0.0f);
                    }
                }));
        
        
        mHandleDrawable.setScaleX(1.0f);
        mHandleDrawable.setScaleY(1.0f);
        Tweener handleTweener = Tweener.to(mHandleDrawable, WAVE_ANIMATION_DURATION,
                "ease", Ease.Linear.easeNone,
                "scaleX", 1.1f,
                "scaleY", 1.1f,
                "delay", 0,
                "onUpdate", mUpdateListener,
                "onComplete",
                new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                     	mHandleDrawable.setScaleX(1.0f);
                     	mHandleDrawable.setScaleY(1.0f);
                    }
                });    
        PropertyValuesHolder[] pvh = handleTweener.animator.getValues();
        TypeEvaluator evaluator = new AuroraHandleTypeEvaluator();
        for(PropertyValuesHolder p: pvh) {
        	p.setEvaluator(evaluator);
        }
        mWaveAnimations.add(handleTweener);
        mWaveAnimations.start();
    }
   
    private Handler mHandler = new Handler();
    private int mInnerRingRadiusStart;
    private int mInnerRingRadiusEnd;
    private int  mOutRingRadiusStart;
    private int  mOutRingRadiusEnd;
    private final PathInterpolator mScalePathInterpolator = new PathInterpolator(0.65f, 0, 0.7f, 1);
    private final PathInterpolator mScalePathInterpolator2 = new PathInterpolator(0.3f, 0, 0.15f, 1);
    private final PathInterpolator mRotatePathInterpolator = new PathInterpolator(0.55f, 0, 0.45f, 1);
    private void startWaveAnimationV2() {
        mWaveAnimations.cancel();

        mWaveAnimations.add(Tweener.to(AuroraRing[0].waveManager, (long)(WAVE_ANIMATION_DURATION * 0.7),
                "ease", Ease.Linear.easeNone,
                "delay", 0,
                "alpha", new float[]{0.5f, 0},
                "radius", new float[]{mInnerRingRadiusStart ,mInnerRingRadiusEnd},
                "radius2", new float[]{mOutRingRadiusStart ,mOutRingRadiusEnd},
                "onUpdate", mUpdateListener,
                "onComplete",
                new AnimatorListenerAdapter() {
		        	public void onAnimationStart(Animator animator) {
		                AuroraRing[0].waveManager.setAlpha(0.5f);
		                AuroraRing[0].waveManager.setRadius(mInnerRingRadiusStart);
		                AuroraRing[0].waveManager.setRadius2(mOutRingRadiusStart);
					}
        	
                    public void onAnimationEnd(Animator animator) {
                       	AuroraRing[0].waveManager.setRadius(0.0f);
                      	AuroraRing[0].waveManager.setRadius2(0.0f);
                    	AuroraRing[0].waveManager.setAlpha(0.0f);
                    }
                }));
        

        mWaveAnimations.add(Tweener.to(AuroraRing[1].waveManager, (long)(WAVE_ANIMATION_DURATION * 0.7),
                "ease", Ease.Linear.easeNone,
                "delay", 840,
                "alpha", new float[]{0.5f, 0},
                "radius", new float[]{mInnerRingRadiusStart ,mInnerRingRadiusEnd},
                "radius2", new float[]{mOutRingRadiusStart ,mOutRingRadiusEnd},
                "onUpdate", mUpdateListener,
                "onComplete",
                new AnimatorListenerAdapter() {
        			public void onAnimationStart(Animator animator) {
        		    	AuroraRing[1].waveManager.setAlpha(0.5f);
        		        AuroraRing[1].waveManager.setRadius(mInnerRingRadiusStart);
        		        AuroraRing[1].waveManager.setRadius2(mOutRingRadiusStart);
        			}
        	
                    public void onAnimationEnd(Animator animator) {
                       	AuroraRing[1].waveManager.setRadius(0.0f);
                      	AuroraRing[1].waveManager.setRadius2(0.0f);
                    	AuroraRing[1].waveManager.setAlpha(0.0f);
                    }
                }));         
        
 
        Tweener handleTweener = Tweener.to(mHandleDrawable, (long)(WAVE_ANIMATION_DURATION * 0.64f * 0.5f),
                "ease", mScalePathInterpolator ,
                "scaleX",  new float[]{1, 0.8f},
                "scaleY", new float[]{1, 0.8f},
                "delay", 0,
                "onUpdate", mUpdateListener,
                "onComplete",
                new AnimatorListenerAdapter() {
        	
		        	public void onAnimationStart(Animator animator) {
		        	       mHandleDrawable.setScaleX(1.0f);
		        	        mHandleDrawable.setScaleY(1.0f);
					}
        	
                    public void onAnimationEnd(Animator animator) {
                      	if(mWaveAnimations.size() == 0) return;
                      	
                     	mHandleDrawable.setScaleX(0.8f);
                     	mHandleDrawable.setScaleY(0.8f);
                   
                   	  Tweener SecondHandleTweener = Tweener.to(mHandleDrawable, (long)(WAVE_ANIMATION_DURATION - WAVE_ANIMATION_DURATION * 0.64f * 0.5f),
                              "ease", mScalePathInterpolator2,
                              "scaleX",  new float[]{0.8f, 1},
                              "scaleY", new float[]{0.8f, 1},
                              "delay", 640,
                              "onUpdate", mUpdateListener,
                              "onComplete",
                              new AnimatorListenerAdapter() {
            			        	public void onAnimationStart(Animator animator) {
            			        		mHandleDrawable.setScaleX(0.8f);
            		                 	mHandleDrawable.setScaleY(0.8f);       
            						}
            	        	
                                  public void onAnimationEnd(Animator animator) {
                                   	mHandleDrawable.setScaleX(1.0f);
                                   	mHandleDrawable.setScaleY(1.0f);
                                  }
                              });  
                   	         SecondHandleTweener.animator.start();
                   	         mWaveAnimations.add(SecondHandleTweener);
                    }
                });    
        
           
        ArrayList<PropertyValuesHolder> props = new ArrayList<PropertyValuesHolder>(1);
        props.add(PropertyValuesHolder.ofFloat("Rotation",  0.0f,  -60.0f));
        ObjectAnimator mRotateAnimation = ObjectAnimator.ofPropertyValuesHolder(mHandleDrawable,
                props.toArray(new PropertyValuesHolder[1]));
        mRotateAnimation.setDuration((long)(WAVE_ANIMATION_DURATION * 0.16f * 0.5f));
        mRotateAnimation.setRepeatCount(4);
        mRotateAnimation.setInterpolator(mRotatePathInterpolator);
        mRotateAnimation.addUpdateListener(mUpdateListener);
        mRotateAnimation.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animator) {
				mHandleDrawable.setRotation(0.0f);
			}
	    });
        
	     mWaveAnimations.add(new Tweener(mRotateAnimation));
        
        mWaveAnimations.add(handleTweener);
        mWaveAnimations.start();
    }
    
    private void startWaveAnimationWhenTouch() {
    	if(mAnimatingTargets) {
    		return;
    	}    	
        mWaveAnimations.cancel();
        int[] values = new int[2];
        values[0] = 0;
//        values[1] = 1000;
        values[1] = 11;
//        for(AuroraPointWhenTouch ap : mAuroraPointWhenTouch) {
//        	ap.mPointManager.setPoint(0);
//        }
//        Tweener pointTweener = Tweener.to(AuroraPointWhenTouch.mPointManager, (long)(WAVE_ANIMATION_DURATION * 1),
//                "ease", Ease.Linear.easeNone,
//                "delay", 0,
//                "point", values,
//                "onUpdate", mUpdateListener,
//                "onComplete",
//                new AnimatorListenerAdapter() {
//                    public void onAnimationEnd(Animator animator) {
//                        AuroraPointWhenTouch.mPointManager.setPoint(0);
//                    }
//                });
////        pointTweener.animator.setRepeatCount(ValueAnimator.INFINITE);
//        mWaveAnimations.add(pointTweener);    
//        mWaveAnimations.start();
    }
    

    /**
     * Resets the widget to default state and cancels all animation. If animate is 'true', will
     * animate objects into place. Otherwise, objects will snap back to place.
     *
     * @param animate
     */
    public void reset(boolean animate) {
        mGlowAnimations.stop();
        mTargetAnimations.stop();
        startBackgroundAnimation(0, 0.0f);
        stopAndHideWaveAnimation();
        hideTargets(animate, false);
        hideGlow(0, 0, 0.0f, null);
        Tweener.reset();
    }

    private void startBackgroundAnimation(int duration, float alpha) {
        final Drawable background = getBackground();
        if (mAlwaysTrackFinger && background != null) {
            if (mBackgroundAnimator != null) {
                mBackgroundAnimator.animator.cancel();
            }
            mBackgroundAnimator = Tweener.to(background, duration,
                    "ease", Ease.Cubic.easeIn,
                    "alpha", (int)(255.0f * alpha),
                    "delay", SHOW_ANIMATION_DELAY);
            mBackgroundAnimator.animator.start();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	
    	boolean defIncomingTouchSwitch = this.getResources().getBoolean(com.android.phone.R.bool.aurora_def_incoming_touch_switch);
  	    boolean touchSwitch = mSP != null && mSP.getBoolean("aurora_incoming_touch_switch", defIncomingTouchSwitch);
  		if(touchSwitch && !PreventTouchManager.mIsTouchEnable && !isTouchState()) {
  			Log.v(TAG, "onTouchEvent return PreventTouchManager");
  			return true;
  		}
  			
    	
        final int action = event.getActionMasked();
        boolean handled = false;
        switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                if (DEBUG) Log.v(TAG, "*** DOWN ***");
                handleDown(event);
                handleMove(event);
                handled = true;
                break;

            case MotionEvent.ACTION_MOVE:
                if (DEBUG) Log.v(TAG, "*** MOVE ***");
                handleMove(event);
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                if (DEBUG) Log.v(TAG, "*** UP ***");
                handleMove(event);
                handleUp(event);
                handled = true;
                break;

            case MotionEvent.ACTION_CANCEL:
                if (DEBUG) Log.v(TAG, "*** CANCEL ***");
                handleMove(event);
                handleCancel(event);
                handled = true;
                break;

        }
        
    	 
        invalidate();
        return handled ? true : super.onTouchEvent(event);
    }
    

    private void updateGlowPosition(float x, float y) {
        float dx = x - mOuterRing.getX();
        float dy = y - mOuterRing.getY();
        dx *= 1f / mRingScaleFactor;
        dy *= 1f / mRingScaleFactor;
//        mPointCloud.glowManager.setX(mOuterRing.getX() + dx);
//        mPointCloud.glowManager.setY(mOuterRing.getY() + dy);
        mPointCloud.glowManager.setY(y);
    }

    private void handleDown(MotionEvent event) {
        int actionIndex = event.getActionIndex();
        float eventX = event.getX(actionIndex);
        float eventY = event.getY(actionIndex);
        switchToState(STATE_START, eventX, eventY);
        if (!trySwitchToFirstTouchState(eventX, eventY)) {
            mDragging = false;
        } else {
            mPointerId = event.getPointerId(actionIndex);
            updateGlowPosition(eventX, eventY);
        }
    }

    private void handleUp(MotionEvent event) {
        if (DEBUG && mDragging) Log.v(TAG, "** Handle RELEASE");
        int actionIndex = event.getActionIndex();
        if (event.getPointerId(actionIndex) == mPointerId) {
            switchToState(STATE_FINISH, event.getX(actionIndex), event.getY(actionIndex));
        }
    }

    private void handleCancel(MotionEvent event) {
        if (DEBUG && mDragging) Log.v(TAG, "** Handle CANCEL");

        // Drop the active target if canceled.
        mActiveTarget = -1; 

        int actionIndex = event.findPointerIndex(mPointerId);
        actionIndex = actionIndex == -1 ? 0 : actionIndex;
        switchToState(STATE_FINISH, event.getX(actionIndex), event.getY(actionIndex));
    }

    private void handleMove(MotionEvent event) {
        int activeTarget = -1;
        final int historySize = event.getHistorySize();
        ArrayList<TargetDrawable> targets = mTargetDrawables;
        int ntargets = targets.size();
        float x = 0.0f;
        float y = 0.0f;
        float activeAngle = 0.0f;
        int actionIndex = event.findPointerIndex(mPointerId);

        if (actionIndex == -1) {
            return;  // no data for this pointer
        }

        for (int k = 0; k < historySize + 1; k++) {
            float eventX = k < historySize ? event.getHistoricalX(actionIndex, k)
                    : event.getX(actionIndex);
            float eventY = k < historySize ? event.getHistoricalY(actionIndex, k)
                    : event.getY(actionIndex);
            // tx and ty are relative to wave center
            float tx = eventX - mWaveCenterX;
            float ty = eventY - mWaveCenterY;
            float touchRadius = (float) Math.sqrt(dist2(tx, ty));
            final float scale = touchRadius > mOuterRadius ? mOuterRadius / touchRadius : 1.0f;
            float limitX = tx * scale;
            float limitY = ty * scale;
            double angleRad = Math.atan2(-ty, tx);

            if (!mDragging) {
                trySwitchToFirstTouchState(eventX, eventY);
            }

            if (mDragging) {
                // For multiple targets, snap to the one that matches
                final float snapRadius = mRingScaleFactor * mOuterRadius - mSnapMargin;
                final float snapDistance2 = snapRadius * snapRadius;
                // Find first target in range
                for (int i = 0; i < ntargets; i++) {
                    TargetDrawable target = targets.get(i);

                    double targetMinRad = mFirstItemOffset + (i - 0.5) * 2 * Math.PI / ntargets;
                    double targetMaxRad = mFirstItemOffset + (i + 0.5) * 2 * Math.PI / ntargets;
                    if (target.isEnabled()) {
                        boolean angleMatches =
                            (angleRad > targetMinRad && angleRad <= targetMaxRad) ||
                            (angleRad + 2 * Math.PI > targetMinRad &&
                             angleRad + 2 * Math.PI <= targetMaxRad) ||
                            (angleRad - 2 * Math.PI > targetMinRad &&
                             angleRad - 2 * Math.PI <= targetMaxRad);
                        if (angleMatches && (dist2(tx, ty) > snapDistance2)) {
                            activeTarget = i;
                            activeAngle = (float) -angleRad;
                        }
                    }
                }
            }
            x = limitX;
            y = limitY;
        }

        if (!mDragging) {
            return;
        }

        if (activeTarget != -1) {
            switchToState(STATE_SNAP, x,y);
            updateGlowPosition(x, y);
        } else {
            switchToState(STATE_TRACKING, x, y);
            updateGlowPosition(x, y);
        }

        if (mActiveTarget != activeTarget) {
            // Defocus the old target
            if (mActiveTarget != -1) {
                TargetDrawable target = targets.get(mActiveTarget);
                if (target.hasState(TargetDrawable.STATE_FOCUSED)) {
                    target.setState(TargetDrawable.STATE_INACTIVE);
                }
                if (mMagneticTargets) {
                    updateTargetPosition(mActiveTarget, mWaveCenterX, mWaveCenterY);
                }
            }
            // Focus the new target
            if (activeTarget != -1) {
                TargetDrawable target = targets.get(activeTarget);
                if (target.hasState(TargetDrawable.STATE_FOCUSED)) {
                    target.setState(TargetDrawable.STATE_FOCUSED);
                }
                if (mMagneticTargets) {
                    updateTargetPosition(activeTarget, mWaveCenterX, mWaveCenterY, activeAngle);
                }
                if (AccessibilityManager.getInstance(mContext).isEnabled()) {
                    String targetContentDescription = getTargetDescription(activeTarget);
                    announceForAccessibility(targetContentDescription);
                }
            }
        }
        mActiveTarget = activeTarget;
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        if (AccessibilityManager.getInstance(mContext).isTouchExplorationEnabled()) {
            final int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    event.setAction(MotionEvent.ACTION_DOWN);
                    break;
                case MotionEvent.ACTION_HOVER_MOVE:
                    event.setAction(MotionEvent.ACTION_MOVE);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    event.setAction(MotionEvent.ACTION_UP);
                    break;
            }
            onTouchEvent(event);
            event.setAction(action);
        }
        super.onHoverEvent(event);
        return true;
    }

    /**
     * Sets the current grabbed state, and dispatches a grabbed state change
     * event to our listener.
     */
    private void setGrabbedState(int newState) {
        if (newState != mGrabbedState) {
            if (newState != OnTriggerListener.NO_HANDLE) {
                vibrate();
            }
            mGrabbedState = newState;
            if (mOnTriggerListener != null) {
                if (newState == OnTriggerListener.NO_HANDLE) {
                    mOnTriggerListener.onReleased(this, OnTriggerListener.CENTER_HANDLE);
                } else {
                    mOnTriggerListener.onGrabbed(this, OnTriggerListener.CENTER_HANDLE);
                }
                mOnTriggerListener.onGrabbedStateChange(this, newState);
            }
        }
    }

    private boolean trySwitchToFirstTouchState(float x, float y) {
        final float tx = x - mWaveCenterX;
        final float ty = y - mWaveCenterY;
        if (mAlwaysTrackFinger || dist2(tx,ty) <= getScaledGlowRadiusSquared()) {
            if (DEBUG) Log.v(TAG, "** Handle HIT");
            switchToState(STATE_FIRST_TOUCH, x, y);
            updateGlowPosition(tx, ty);
            mDragging = true;
            return true;
        }
        return false;
    }

    private void assignDefaultsIfNeeded() {
        if (mOuterRadius == 0.0f) {
            mOuterRadius = Math.max(mOuterRing.getWidth(), mOuterRing.getHeight())/2.0f;
        }
        if (mSnapMargin == 0.0f) {
            mSnapMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    SNAP_MARGIN_DEFAULT, getContext().getResources().getDisplayMetrics());
        }
        if (mInnerRadius == 0.0f) {
            mInnerRadius = mHandleDrawable.getWidth() / 10.0f;
        }
    }

    private void computeInsets(int dx, int dy) {
        final int layoutDirection = getLayoutDirection();
        final int absoluteGravity = Gravity.getAbsoluteGravity(mGravity, layoutDirection);

        switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.LEFT:
                mHorizontalInset = 0;
                break;
            case Gravity.RIGHT:
                mHorizontalInset = dx;
                break;
            case Gravity.CENTER_HORIZONTAL:
            default:
                mHorizontalInset = dx / 2;
                break;
        }
        switch (absoluteGravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.TOP:
                mVerticalInset = 0;
                break;
            case Gravity.BOTTOM:
                mVerticalInset = dy;
                break;
            case Gravity.CENTER_VERTICAL:
            default:
                mVerticalInset = dy / 2;
                break;
        }
    }

    /**
     * Given the desired width and height of the ring and the allocated width and height, compute
     * how much we need to scale the ring.
     */
    private float computeScaleFactor(int desiredWidth, int desiredHeight,
            int actualWidth, int actualHeight) {

        // Return unity if scaling is not allowed.
        if (!mAllowScaling) return 1f;

        final int layoutDirection = getLayoutDirection();
        final int absoluteGravity = Gravity.getAbsoluteGravity(mGravity, layoutDirection);

        float scaleX = 1f;
        float scaleY = 1f;

        // We use the gravity as a cue for whether we want to scale on a particular axis.
        // We only scale to fit horizontally if we're not pinned to the left or right. Likewise,
        // we only scale to fit vertically if we're not pinned to the top or bottom. In these
        // cases, we want the ring to hang off the side or top/bottom, respectively.
        switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.LEFT:
            case Gravity.RIGHT:
                break;
            case Gravity.CENTER_HORIZONTAL:
            default:
                if (desiredWidth > actualWidth) {
                    scaleX = (1f * actualWidth - mMaxTargetWidth) /
                            (desiredWidth - mMaxTargetWidth);
                }
                break;
        }
        switch (absoluteGravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.TOP:
            case Gravity.BOTTOM:
                break;
            case Gravity.CENTER_VERTICAL:
            default:
                if (desiredHeight > actualHeight) {
                    scaleY = (1f * actualHeight - mMaxTargetHeight) /
                            (desiredHeight - mMaxTargetHeight);
                }
                break;
        }
        return Math.min(scaleX, scaleY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();
        int computedWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
        int computedHeight = resolveMeasured(heightMeasureSpec, minimumHeight);

        mRingScaleFactor = computeScaleFactor(minimumWidth, minimumHeight,
                computedWidth, computedHeight);

        int scaledWidth = getScaledSuggestedMinimumWidth();
        int scaledHeight = getScaledSuggestedMinimumHeight();
        

        computeInsets(computedWidth - scaledWidth, computedHeight - scaledHeight);
        setMeasuredDimension(computedWidth, computedHeight);
    }

    private float getRingWidth() {
        return mRingScaleFactor * Math.max(mOuterRing.getWidth(), 2 * mOuterRadius);
    }

    private float getRingHeight() {
        return mRingScaleFactor * Math.max(mOuterRing.getHeight(), 2 * mOuterRadius);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        final int width = right - left;
        final int height = bottom - top;

        // Target placement width/height. This puts the targets on the greater of the ring
        // width or the specified outer radius.
        final float placementWidth = getRingWidth();
        final float placementHeight = getRingHeight();
//        float newWaveCenterX = mHorizontalInset
//                + Math.max(width, mMaxTargetWidth + placementWidth) / 2;
      float newWaveCenterX =  width / 2;
        float newWaveCenterY = mVerticalInset
                + Math.max(height, + mMaxTargetHeight + placementHeight) / 2;
        
        Log.v("onLayout", "height =  " + height + " ,mMaxTargetHeight =" + mMaxTargetHeight);

        if (mInitialLayout) {
            stopAndHideWaveAnimation();
            hideTargets(false, false);
            mInitialLayout = false;
        }

        mOuterRing.setPositionX(newWaveCenterX);
        mOuterRing.setPositionY(newWaveCenterY);

        mPointCloud.setScale(mRingScaleFactor);
        AuroraCircle[0].setScale(mRingScaleFactor);
        AuroraCircle[1].setScale(mRingScaleFactor);
        AuroraRing[0].setScale(mRingScaleFactor);
        AuroraRing[1].setScale(mRingScaleFactor);
        AuroraPoint.setScale(mRingScaleFactor);
        for(AuroraPointWhenTouch ap : mAuroraPointWhenTouch) {
            ap.mScaleManager.setScale(mRingScaleFactor);
        }

        AuroraPointWhenTouchInit.setScale(mRingScaleFactor);
        
        

        mHandleDrawable.setPositionX(newWaveCenterX);
        mHandleDrawable.setPositionY(newWaveCenterY);

        updateTargetPositions(newWaveCenterX, newWaveCenterY);
        updatePointCloudPosition(newWaveCenterX, newWaveCenterY);
        updateGlowPosition(newWaveCenterX, newWaveCenterY);

        mWaveCenterX = newWaveCenterX;
        mWaveCenterY = newWaveCenterY;

        if (DEBUG) dump();
    }

    private void updateTargetPosition(int i, float centerX, float centerY) {
        final float angle = getAngle(getSliceAngle(), i);
        updateTargetPosition(i, centerX, centerY, angle);
    }

    private void updateTargetPosition(int i, float centerX, float centerY, float angle) {
        final float placementRadiusX = getRingWidth() / 2;
        final float placementRadiusY = getRingHeight() / 2;
        if (i >= 0) {
            ArrayList<TargetDrawable> targets = mTargetDrawables;
            final TargetDrawable targetIcon = targets.get(i);
            targetIcon.setPositionX(centerX);
            targetIcon.setPositionY(centerY);
            targetIcon.setX(placementRadiusX * (float) Math.cos(angle));
            targetIcon.setY(placementRadiusY * (float) Math.sin(angle));
        }
    }
    
    private void auroraUpdateTargetPosition(int i, float centerX, float centerY, float angle) {
        final float placementRadiusX = getRingWidth() / 2;
        final float placementRadiusY = getRingHeight() / 2;        
        if (i >= 0) {
            ArrayList<TargetDrawable> targets = mTargetDrawables;
            final TargetDrawable targetIcon = targets.get(i);
            targetIcon.setPositionX(centerX);
            targetIcon.setPositionY(centerY);
            if(i != 1) {
	            targetIcon.setX(placementRadiusX * (float) Math.cos(angle));
	            targetIcon.setY(placementRadiusY * (float) Math.sin(angle));
            } else {
                targetIcon.setX((placementRadiusX - mGlowRadiusDiffer) * (float) Math.cos(angle));
	            targetIcon.setY((placementRadiusY - mGlowRadiusDiffer) * (float) Math.sin(angle));
            }
        }
    }

    private void updateTargetPositions(float centerX, float centerY) {
        updateTargetPositions(centerX, centerY, false);
    }

    private void updateTargetPositions(float centerX, float centerY, boolean skipActive) {
        final int size = mTargetDrawables.size();
        final float alpha = getSliceAngle();
        // Reposition the target drawables if the view changed.
        for (int i = 0; i < size; i++) {
            if (!skipActive || i != mActiveTarget) {
                updateTargetPosition(i, centerX, centerY, getAngle(alpha, i));
            }
        }
    }

    private float getAngle(float alpha, int i) {
        return mFirstItemOffset + alpha * i;
    }

    private float getSliceAngle() {
        return (float) (-2.0f * Math.PI / mTargetDrawables.size());
    }

    private void updatePointCloudPosition(float centerX, float centerY) {
        mPointCloud.setCenter(centerX, centerY);
        AuroraCircle[0].setCenter(centerX, centerY);
        AuroraCircle[1].setCenter(centerX, centerY);
        AuroraRing[0].setCenter(centerX, centerY);
        AuroraRing[1].setCenter(centerX, centerY);
        AuroraPoint.setCenter(centerX, centerY);   
        for(AuroraPointWhenTouch ap : mAuroraPointWhenTouch) {
            ap.setCenter(centerX, centerY);  
        }
        AuroraPointWhenTouchInit.setCenter(centerX, centerY);  
        
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //AuroraRing  AuroraPoint AuroraPointWhenTouchInit
        if(isTouchState() && mDragging) {
        	if(mAnimatingTargets) {
        		AuroraRing[0].draw(canvas);
        		AuroraPoint.draw(canvas);
//            	AuroraPointWhenTouchInit.draw(canvas);
        	} else {
        		 for(AuroraPointWhenTouch ap : mAuroraPointWhenTouch) {
        			 ap.draw(canvas);
        		 }
        	}
            mPointCloud.draw(canvas);
        } else {
//            AuroraCircle[0].draw(canvas);
//            AuroraCircle[1].draw(canvas);
        	AuroraRing[0].draw(canvas);
        	AuroraRing[1].draw(canvas);
        	AuroraPoint.draw(canvas);
        }

//        mOuterRing.draw(canvas);
        final int ntargets = mTargetDrawables.size();
        for (int i = 0; i < ntargets; i++) {
            TargetDrawable target = mTargetDrawables.get(i);
            if (target != null) {
                target.draw(canvas);
            }
        }
        mHandleDrawable.draw(canvas);
    }

    public void setOnTriggerListener(OnTriggerListener listener) {
        mOnTriggerListener = listener;
    }

    private float square(float d) {
        return d * d;
    }

    private float dist2(float dx, float dy) {
        return dx*dx + dy*dy;
    }

    private float getScaledGlowRadiusSquared() {
        final float scaledTapRadius;
        if (AccessibilityManager.getInstance(mContext).isEnabled()) {
            scaledTapRadius = TAP_RADIUS_SCALE_ACCESSIBILITY_ENABLED * mGlowRadius;
        } else {
            scaledTapRadius = mGlowRadius;
        }
        return square(scaledTapRadius);
    }

    private void announceTargets() {
        StringBuilder utterance = new StringBuilder();
        final int targetCount = mTargetDrawables.size();
        for (int i = 0; i < targetCount; i++) {
            String targetDescription = getTargetDescription(i);
            String directionDescription = getDirectionDescription(i);
            if (!TextUtils.isEmpty(targetDescription)
                    && !TextUtils.isEmpty(directionDescription)) {
                String text = String.format(directionDescription, targetDescription);
                utterance.append(text);
            }
        }
        if (utterance.length() > 0) {
            announceForAccessibility(utterance.toString());
        }
    }

    private String getTargetDescription(int index) {
        if (mTargetDescriptions == null || mTargetDescriptions.isEmpty()) {
            mTargetDescriptions = loadDescriptions(mTargetDescriptionsResourceId);
            if (mTargetDrawables.size() != mTargetDescriptions.size()) {
                Log.w(TAG, "The number of target drawables must be"
                        + " equal to the number of target descriptions.");
                return null;
            }
        }
        return mTargetDescriptions.get(index);
    }

    private String getDirectionDescription(int index) {
        if (mDirectionDescriptions == null || mDirectionDescriptions.isEmpty()) {
            mDirectionDescriptions = loadDescriptions(mDirectionDescriptionsResourceId);
            if (mTargetDrawables.size() != mDirectionDescriptions.size()) {
                Log.w(TAG, "The number of target drawables must be"
                        + " equal to the number of direction descriptions.");
                return null;
            }
        }
        return mDirectionDescriptions.get(index);
    }

    private ArrayList<String> loadDescriptions(int resourceId) {
        TypedArray array = getContext().getResources().obtainTypedArray(resourceId);
        final int count = array.length();
        ArrayList<String> targetContentDescriptions = new ArrayList<String>(count);
        for (int i = 0; i < count; i++) {
            String contentDescription = array.getString(i);
            targetContentDescriptions.add(contentDescription);
        }
        array.recycle();
        return targetContentDescriptions;
    }

    public int getResourceIdForTarget(int index) {
        final TargetDrawable drawable = mTargetDrawables.get(index);
        return drawable == null ? 0 : drawable.getResourceId();
    }

    public void setEnableTarget(int resourceId, boolean enabled) {
        for (int i = 0; i < mTargetDrawables.size(); i++) {
            final TargetDrawable target = mTargetDrawables.get(i);
            if (target.getResourceId() == resourceId) {
                target.setEnabled(enabled);
                break; // should never be more than one match
            }
        }
    }

    /**
     * Gets the position of a target in the array that matches the given resource.
     * @param resourceId
     * @return the index or -1 if not found
     */
    public int getTargetPosition(int resourceId) {
        for (int i = 0; i < mTargetDrawables.size(); i++) {
            final TargetDrawable target = mTargetDrawables.get(i);
            if (target.getResourceId() == resourceId) {
                return i; // should never be more than one match
            }
        }
        return -1;
    }

    private boolean replaceTargetDrawables(Resources res, int existingResourceId,
            int newResourceId) {
        if (existingResourceId == 0 || newResourceId == 0) {
            return false;
        }

        boolean result = false;
        final ArrayList<TargetDrawable> drawables = mTargetDrawables;
        final int size = drawables.size();
        for (int i = 0; i < size; i++) {
            final TargetDrawable target = drawables.get(i);
            if (target != null && target.getResourceId() == existingResourceId) {
                target.setDrawable(res, newResourceId);
                result = true;
            }
        }

        if (result) {
            requestLayout(); // in case any given drawable's size changes
        }

        return result;
    }

    /**
     * Searches the given package for a resource to use to replace the Drawable on the
     * target with the given resource id
     * @param component of the .apk that contains the resource
     * @param name of the metadata in the .apk
     * @param existingResId the resource id of the target to search for
     * @return true if found in the given package and replaced at least one target Drawables
     */
    public boolean replaceTargetDrawablesIfPresent(ComponentName component, String name,
                int existingResId) {
        if (existingResId == 0) return false;

        boolean replaced = false;
        if (component != null) {
            try {
                PackageManager packageManager = mContext.getPackageManager();
                // Look for the search icon specified in the activity meta-data
                Bundle metaData = packageManager.getActivityInfo(
                        component, PackageManager.GET_META_DATA).metaData;
                if (metaData != null) {
                    int iconResId = metaData.getInt(name);
                    if (iconResId != 0) {
                        Resources res = packageManager.getResourcesForActivity(component);
                        replaced = replaceTargetDrawables(res, existingResId, iconResId);
                    }
                }
            } catch (NameNotFoundException e) {
                Log.w(TAG, "Failed to swap drawable; "
                        + component.flattenToShortString() + " not found", e);
            } catch (Resources.NotFoundException nfe) {
                Log.w(TAG, "Failed to swap drawable from "
                        + component.flattenToShortString(), nfe);
            }
        }
        if (!replaced) {
            // Restore the original drawable
            replaceTargetDrawables(mContext.getResources(), existingResId, existingResId);
        }
        return replaced;
    }
    
    private boolean isTouchState() {
    	return mState != STATE_FINISH && mState != STATE_IDLE;
    }
    
    private InCallScreen mInCallScreen;

    public void setInCallScreen(InCallScreen c) {
	       mInCallScreen = c;
	}
    
    private WhenTouchListener mWhenTouchListener = null;
    public interface WhenTouchListener {
        public void whenTouch(boolean touch);
    }
    
    public void setWhenTouchListener(WhenTouchListener listener) {
    	mWhenTouchListener = listener;
    }
    
    private static SharedPreferences mSP = null;
    
    private void startWaveAnimationWhenTouchV2Internal() {
   
            Log.v(TAG, "startWaveAnimationWhenTouchV2  111"); 
        mWaveAnimationsV2.cancel();
      
        for(AuroraPointWhenTouch ap:mAuroraPointWhenTouch) {
        	ap.restoreY();
        	int index  = ap.getIndex();
        	int sign = -1;
        	Animator a = getPointAlphaAnimator(ap, 250 * (index % 3));
        	Animator y = getPointYAnimator(ap, 250 * (index % 3), sign * mPointMoveStep * ((index % 3) + 1));
        	Animator s = getPointScaleAnimator(ap, 250 * (index % 3));
        	mWaveAnimationsV2.add(a);    
        	mWaveAnimationsV2.add(y);    
        	mWaveAnimationsV2.add(s);    
        }
        mWaveAnimationsV2.start();
        
    }
    
    private void startWaveAnimationWhenTouchV2() {
       	if(mAnimatingTargets) {
    		return;
    	}    	
      	mFactor = 1.0f;
    	startWaveAnimationWhenTouchV2Internal();
     }
    
    private void startInitAnimationWhenTouchV2() {
    	mFactor = 0.7f;
    	startWaveAnimationWhenTouchV2Internal();
    }
    
    
    private float mPointMoveStep = 30;
    private final PathInterpolator mPointScalePathInterpolator = new PathInterpolator(0.45f, 0, 0.6f, 0.3f);
    private final PathInterpolator mPointYPathInterpolator = new PathInterpolator(0.7f, 0, 0.7f, 1);
    
    
    
    private Animator getPointAlphaAnimator(AuroraPointWhenTouch ap, long delay) {        	    	
        //0.08s~0.28s~0.28s~0.08s   0~30%~100~30%~0
        ap.mAlphaManager.setAlpha(0.01f);
    	AccelerateDecelerateInterpolator alphaInterpolator = new AccelerateDecelerateInterpolator();
    	AnimatorUpdateListener al = mUpdateListener;
         ObjectAnimator mAlphaAnimation1 = getAnimator(ap.mAlphaManager, "alpha", 0.01f, 0.3f, delay,
        		 80, alphaInterpolator, al);
         ObjectAnimator mAlphaAnimation2 = getAnimator(ap.mAlphaManager,  "alpha", 0.3f, 1.0f, 0,
        		 280, alphaInterpolator, al);
         ObjectAnimator mAlphaAnimation3 = getAnimator(ap.mAlphaManager, "alpha",  1.0f, 0.3f, 0,
        		280, alphaInterpolator, al);
         ObjectAnimator mAlphaAnimation4 = getAnimator(ap.mAlphaManager, "alpha",  0.3f, 0.01f, 0,
        		 80, alphaInterpolator, al);
         AnimatorSet bouncer = new AnimatorSet();    
         bouncer. playSequentially(mAlphaAnimation1, mAlphaAnimation2, mAlphaAnimation3, mAlphaAnimation4);   
        
 	     return bouncer;
    }
    

    private Animator getPointYAnimator(AuroraPointWhenTouch ap, long delay, float movedistance) {        	    	
  
    	AnimatorUpdateListener al = mUpdateListener;
         ObjectAnimator mYAnimation = getAnimator(ap, "Y", ap.getY(), ap.getY() + movedistance , 0,
        		 1000, mPointYPathInterpolator, al);

        
 	     return mYAnimation;
    }
    
    private Animator getPointScaleAnimator(AuroraPointWhenTouch ap, long delay) {        	    	    
    	AnimatorUpdateListener al = mUpdateListener;
         //52%~100%~52% duration 0.36s  0.36s
         ap.mScaleManager.setScale(0.52f);
         ObjectAnimator mScaleAnimation1 = getAnimator(ap.mScaleManager, "scale",  0.52f, 1.0f, delay,
         		360, mPointScalePathInterpolator, al);
          ObjectAnimator mScaleAnimation2 = getAnimator(ap.mScaleManager, "scale",  1.0f, 0.52f, 0,
         		 360, mPointScalePathInterpolator, al);         
         AnimatorSet bouncer = new AnimatorSet();       
         bouncer. playSequentially(mScaleAnimation1,mScaleAnimation2); 
        
 	     return bouncer;
    }
    
  
    
 	 private float mFactor = 1.0f;
    private ObjectAnimator getAnimator(Object obj, String prop, float v1, float v2, long delay, long duration, TimeInterpolator ti, AnimatorUpdateListener l) {
    	 ArrayList<PropertyValuesHolder> props = new ArrayList<PropertyValuesHolder>(1);
         props.add(PropertyValuesHolder.ofFloat(prop,  v1,  v2));
    	 ObjectAnimator mAnimation = ObjectAnimator.ofPropertyValuesHolder(obj,
                 props.toArray(new PropertyValuesHolder[1]));
         mAnimation.setDuration((long)(duration * mFactor));
         mAnimation.setStartDelay((long)(delay *  mFactor));
         mAnimation.setInterpolator(ti);
         if(l != null) {
        	 mAnimation.addUpdateListener(l);
         }
         return mAnimation;
    }
    
    
    private class AnimationBundleV2 extends ArrayList<Animator> {
        private static final long serialVersionUID = 0xA84D78726F127468L;
        private boolean mSuspended;

        public void start() {
            if (mSuspended) return; // ignore attempts to start animations
            final int count = size();
            for (int i = 0; i < count; i++) {
            	Animator anim = get(i);
                anim.start();
            }
        }

        public void cancel() {
            final int count = size();
            for (int i = 0; i < count; i++) {
            	Animator anim = get(i);
                anim.cancel();
            }
            clear();
        }

        public void stop() {
            final int count = size();
            for (int i = 0; i < count; i++) {
            	Animator anim = get(i);
                anim.end();
            }
            clear();
        }

        public void setSuspended(boolean suspend) {
            mSuspended = suspend;
        }
    };
    
    
 
}
