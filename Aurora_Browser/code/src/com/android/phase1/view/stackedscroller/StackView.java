/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.phase1.view.stackedscroller;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;

/**
 * 层叠View的根视图
 * @author JimXia
 *
 * @date 2015年1月20日 下午3:20:37
 */
public class StackView extends FrameLayout implements View.OnClickListener {
    
    /** The TaskView callbacks */
    interface StackViewCallbacks {
        void onStackViewClicked(View v);
        void onStackViewDismissed(StackView stv);
        void onStackViewTransformed(StackView stv);
    }

    float mTaskProgress;
    ObjectAnimator mTaskProgressAnimator;
    float mMaxDimScale;
    int mDim;
    AccelerateInterpolator mDimInterpolator = new AccelerateInterpolator(1f);

    Paint mLayerPaint = new Paint();

    View mContent;
    StackViewCallbacks mCb;
    
    private static final int mStackMaxDim = 96;
    private static final boolean mUseHardwareLayers = false;
    private static final boolean mFakeShadows = true;

    // Optimizations
    ValueAnimator.AnimatorUpdateListener mUpdateDimListener =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setTaskProgress((Float) animation.getAnimatedValue());
                }
            };


    public StackView(Context context) {
        this(context, null);
    }

    public StackView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StackView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public StackView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr/*, defStyleRes*/);
        mMaxDimScale = mStackMaxDim / 255f;
        setTaskProgress(getTaskProgress());
        setDim(getDim());
        if (mFakeShadows) {
            setBackground(new FakeShadowDrawable(context));
        }
    }
    
    public void setContentView(View contentView) {
        mContent = contentView;
        removeAllViews();
        addView(contentView);
    }
    
    public View getContentView() {
        return mContent;
    }

    /** Set callback */
    void setCallbacks(StackViewCallbacks cb) {
        mCb = cb;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        
        int widthWithoutPadding = width - paddingLeft - paddingRight;
        int heightWithoutPadding = height - paddingTop - paddingBottom;

        // Measure the content
        if (mContent != null) {
            mContent.measure(MeasureSpec.makeMeasureSpec(widthWithoutPadding, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(heightWithoutPadding, MeasureSpec.AT_MOST));
            width = mContent.getMeasuredWidth() + paddingLeft + paddingRight;
            height = mContent.getMeasuredHeight() + paddingTop + paddingBottom;
        }

        setMeasuredDimension(width, height);
    }
    
    private final AnimatorListener mAnimatorListener = new AnimatorListener() {
        
        @Override
        public void onAnimationStart(Animator animator) {
        }
        
        @Override
        public void onAnimationRepeat(Animator animator) {
        }
        
        @Override
        public void onAnimationEnd(Animator animator) {
            if (mCb != null) {
                mCb.onStackViewTransformed(StackView.this);
            }
        }
        
        @Override
        public void onAnimationCancel(Animator animator) {
        }
    };

    void updateViewPropertiesToTaskTransform(StackViewTransform toTransform, int duration) {
        // Apply the transform
        toTransform.applyToTaskView(this, duration, false, mAnimatorListener);
//        if (duration == 0) {
//            if (mCb != null) {
//                mCb.onStackViewTransformed(this);
//            }
//        }

        // Update the task progress
        if (mTaskProgressAnimator != null) {
            mTaskProgressAnimator.removeAllListeners();
            mTaskProgressAnimator.cancel();
        }
        if (duration <= 0) {
            setTaskProgress(toTransform.p);
        } else {
            mTaskProgressAnimator = ObjectAnimator.ofFloat(this, "taskProgress", toTransform.p);
            mTaskProgressAnimator.setDuration(duration);
            mTaskProgressAnimator.addUpdateListener(mUpdateDimListener);
            mTaskProgressAnimator.start();
        }
    }

    /** Resets this view's properties */
    void resetViewProperties() {
        setDim(0);
        StackViewTransform.reset(this);
    }

    /** Prepares this task view for the enter-recents animations.  This is called earlier in the
     * first layout because the actual animation into recents may take a long time. */
    void prepareEnterRecentsAnimation(int offscreenY) {
        int initialDim = getDim();
        // Apply the current dim
        setDim(initialDim);
    }

    /** Sets the current task progress. */
    public void setTaskProgress(float p) {
        mTaskProgress = p;
        updateDimFromTaskProgress();
    }

    /** Returns the current task progress. */
    public float getTaskProgress() {
        return mTaskProgress;
    }

    /** Returns the current dim. */
    public void setDim(int dim) {
        mDim = dim;
        
        if (mUseHardwareLayers) {
            // Defer setting hardware layers if we have not yet measured, or there is no dim to draw
            if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0) {
                int inverse = 255 - mDim;
                PorterDuffColorFilter dimColorFilter =
                        new PorterDuffColorFilter(
                                Color.argb(0xFF, inverse, inverse, inverse),
                                PorterDuff.Mode.MULTIPLY);
                mLayerPaint.setColorFilter(dimColorFilter);
                mContent.setLayerType(LAYER_TYPE_HARDWARE, mLayerPaint);
            }
        }
    }

    /** Returns the current dim. */
    public int getDim() {
        return mDim;
    }

    /** Compute the dim as a function of the scale of this view. */
    int getDimFromTaskProgress() {
        float dim = mMaxDimScale * mDimInterpolator.getInterpolation(1f - mTaskProgress);
        return (int) (dim * 255);
    }

    /** Update the dim as a function of the scale of this view. */
    void updateDimFromTaskProgress() {
        setDim(getDimFromTaskProgress());
    }

    /** Enables/disables handling touch on this task view. */
    void setTouchEnabled(boolean enabled) {
        setOnClickListener(enabled ? this : null);
    }

    /**** View.OnClickListener Implementation ****/
    @Override
     public void onClick(final View v) {
        if (mCb != null) {
            mCb.onStackViewClicked(mContent);
        }
    }
}
