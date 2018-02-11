/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */
package com.gionee.mms.slide;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.widget.ViewAnimator;
import android.view.ViewConfiguration;

public class GnViewSlideShower extends ViewAnimator implements GestureDetector.OnGestureListener {
    final private static String TAG = "GnViewSlideShower";
    final private static int SWITCH_DURATION = 200;
    
    private final int SLIDE_LEFT = 1;

    private final int SLIDE_RIGHT = 2;

    
    private final int STATE_IDLE = 0;
    
    private final int STATE_SLIDING = 1;

    private final int STATE_DRAG_ANIMATION_START = 2;

    private final int STATE_DRAG_ANIMATION_END = 3;

    private int mState = STATE_IDLE;

    private int mPointerId = Integer.MIN_VALUE;

    private int mLeftViewX;

    private int mRightViewX;

    private int mCurViewX;

    private GnSimpleTranslateAnimation mLeftAni;

    private GnSimpleTranslateAnimation mCurAni;

    private GnSimpleTranslateAnimation mRightAni;

    private GestureDetector mGestureDetector;

    private ViewSlideListener mViewSlideListener;

    private int mVelocityValve;
    
    
    public GnViewSlideShower(Context context) {
        super(context);
        initialize();
    }

    public GnViewSlideShower(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        mLeftAni = new GnSimpleTranslateAnimation();
        mCurAni = new GnSimpleTranslateAnimation();
        mRightAni = new GnSimpleTranslateAnimation();

        mGestureDetector = new GestureDetector(this.getContext(), this);
        mGestureDetector.setIsLongpressEnabled(false);
        
        Animation.AnimationListener aniListener = new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(TAG, "onAnimationEnd");
                assert (mState != STATE_IDLE);
                if (mState == STATE_SLIDING) {
                    mState = STATE_IDLE;
                } else if (mState == STATE_DRAG_ANIMATION_START) {
                    mState = STATE_DRAG_ANIMATION_END;
                }

            }
        };
        mLeftAni.setAnimationListener(aniListener);
        mCurAni.setAnimationListener(aniListener);
        mRightAni.setAnimationListener(aniListener);

        mLeftAni.setInterpolator(new OvershootInterpolator(1.0f));
        mCurAni.setInterpolator(new OvershootInterpolator(1.0f));
        mRightAni.setInterpolator(new OvershootInterpolator(1.0f));

        setFocusable(true);
        
        final ViewConfiguration configuration = ViewConfiguration.get(this.getContext());
        mVelocityValve = configuration.getScaledMinimumFlingVelocity();
    }

    private void setAnimationDuration(long duration) {
        mCurAni.setDuration(duration);
        mLeftAni.setDuration(duration);
        mRightAni.setDuration(duration);
    }

    public void fillRightSlideAnimation() {
        View leftView = getLeftView();
        int curDstX = getWidth();
        int leftDstX = 0;
        Log.d(TAG, "fillRightSlideAnimation: curDstX=" + curDstX + ";leftDstX=" + leftDstX);

        if (leftView != null) {
            mLeftAni.reset(mLeftViewX, leftDstX, 0, 0);
        } else {
            curDstX = 0;
        }

        mCurAni.reset(mCurViewX, curDstX, 0, 0);
        setAnimationDuration(SWITCH_DURATION);
    }

    public void fillLeftSlideAnimation() {
        View rightView = getRightView();
        int curDstX = -getWidth();
        int rightDstX = 0;

        if (rightView != null) {
            mRightAni.reset(mRightViewX, rightDstX, 0, 0);
        } else {
            curDstX = 0;
        }

        mCurAni.reset(mCurViewX, curDstX, 0, 0);
        setAnimationDuration(SWITCH_DURATION);
    }

    public void fillOriginSlideAnimation() {
        View leftView = getLeftView();
        View rightView = getRightView();
        int curDstX = 0;
        int leftDstX = -getWidth();
        int rightDstX = getWidth();

        mCurAni.reset(mCurViewX, curDstX, 0, 0);
        if (leftView != null) {
            mLeftAni.reset(mLeftViewX, leftDstX, 0, 0);
        }
        if (rightView != null) {
            mRightAni.reset(mRightViewX, rightDstX, 0, 0);
        }
        setAnimationDuration(100);
    }

    public void fillStepAnimation(int stepX) {
        View leftView = getLeftView();
        View rightView = getRightView();
        int curDstX = mCurViewX + stepX;
        int leftDstX = mLeftViewX + stepX;
        int rightDstX = mRightViewX + stepX;
        int fence = getWidth() / 2;

        if (leftView != null) {
            mLeftAni.reset(mLeftViewX, leftDstX, 0, 0);
        } else {
            curDstX = Math.min(curDstX, fence);
        }
        if (rightView != null) {
            mRightAni.reset(mRightViewX, rightDstX, 0, 0);
        } else {
            curDstX = Math.max(curDstX, -fence);
        }
        mCurAni.reset(mCurViewX, curDstX, 0, 0);
        setAnimationDuration(0);

        mCurViewX = curDstX;
        mLeftViewX = leftDstX;
        mRightViewX = rightDstX;

        Log.d(TAG, "fillStepAnimation: curDstX=" + curDstX + ";leftDstX=" + leftDstX
                + ";rightDstX=" + rightDstX + ";stepX=" + stepX);
    }

    public void fillStillStepAnimation() {
        View leftView = getLeftView();
        View rightView = getRightView();
        int curDstX = mCurViewX;
        int leftDstX = mLeftViewX;
        int rightDstX = mRightViewX;

        mCurAni.reset(mCurViewX, curDstX, 0, 0);
        if (leftView != null) {
            mLeftAni.reset(mLeftViewX, leftDstX, 0, 0);
        }
        if (rightView != null) {
            mRightAni.reset(mRightViewX, rightDstX, 0, 0);
        }
        setAnimationDuration(0);
        Log.d(TAG, "fillStillAnimation: curDstX=" + curDstX + ";leftDstX=" + leftDstX
                + ";rightDst=" + rightDstX);
    }

    private boolean canSlideLeft() {
        int curChildIndex = getDisplayedChild();
        if (curChildIndex < getChildCount() - 1) {
            return true;
        }
        return false;
    }

    private boolean canSlideRight() {
        int curChildIndex = getDisplayedChild();
        if (curChildIndex > 0) {
            return true;
        }
        return false;
    }

    private void letOtherGone(){
        View leftView = getLeftView();
        View rightView = getRightView();
        if (null != leftView)
            leftView.setVisibility(View.GONE);
        if (null != rightView)
            rightView.setVisibility(View.GONE);
    }
    
    public void slideRight() {
        mState = STATE_SLIDING;
        // Aurora xuyong 2014-07-29 deleted for bug #7004 start
        //fillRightSlideAnimation();

        //setInAnimation(mLeftAni);
        //setOutAnimation(mCurAni);
        // Aurora xuyong 2014-07-29 deleted for bug #7004 end
        letOtherGone();        
        showPrevious();
    }

    public void slideLeft() {
        mState = STATE_SLIDING;
        // Aurora xuyong 2014-07-29 deleted for bug #7004 start
        //fillLeftSlideAnimation();

        //setInAnimation(mRightAni);
        //setOutAnimation(mCurAni);
        // Aurora xuyong 2014-07-29 deleted for bug #7004 end
        letOtherGone();
        showNext();
    }

    public void slideOrigin() {
        Log.d(TAG, "slideOrigin");
        mState = STATE_SLIDING;
        fillOriginSlideAnimation();
        assert (mState == STATE_DRAG_ANIMATION_START || mState == STATE_DRAG_ANIMATION_END);
        Animation outAnimation;
        View outView;
        if (mCurViewX < 0) {
            outAnimation = mRightAni;
            outView = getRightView();
        } else {
            outAnimation = mLeftAni;
            outView = getLeftView();
        }
        if (outView != null) {
            outView.startAnimation(outAnimation);
        }
        View curView = getCurrentView();
        // Aurora xuyong 2014-07-29 deleted for bug #7004 start
        //curView.startAnimation(mCurAni);
        // Aurora xuyong 2014-07-29 deleted for bug #7004 end
        letOtherGone();
    }

    public void trySlideLeft() {
        if (true == canSlideLeft()) {
            slideLeft();
        } else {
            slideOrigin();
        }
    }

    public void trySlideRight() {
        if (true == canSlideRight()) {
            slideRight();
        } else {
            slideOrigin();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        Log.d(TAG, "dispatchTouchEvent received Event");
        boolean ret = this.handleTouchEvent(e);
        if(e.getActionMasked() == MotionEvent.ACTION_MOVE){
            ret = true;//forbid its child to scroll
        }
        
        if(Math.abs( getSlideOrDragingOffset() ) > 1){
            //clear selection of children
            e.setAction(MotionEvent.ACTION_CANCEL);
        }
        return super.dispatchTouchEvent(e) || ret;
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        if (mState == STATE_DRAG_ANIMATION_END) {
            scheduleLayoutAnimation();
            fillStillStepAnimation();
            View curView = getCurrentView();
            View leftView = getLeftView();
            View rightView = getRightView();

            mCurAni.setStartTime(Animation.START_ON_FIRST_FRAME);
            curView.setAnimation(mCurAni);
            if (leftView != null) {
                mLeftAni.setStartTime(Animation.START_ON_FIRST_FRAME);
                // Aurora xuyong 2014-07-29 deleted for bug #7004 start
                //leftView.setAnimation(mLeftAni);
                // Aurora xuyong 2014-07-29 deleted for bug #7004 end
                leftView.setVisibility(View.VISIBLE);
            }
            if (rightView != null) {
                mRightAni.setStartTime(Animation.START_ON_FIRST_FRAME);
                // Aurora xuyong 2014-07-29 deleted for bug #7004 start
                //rightView.setAnimation(mRightAni);
                // Aurora xuyong 2014-07-29 deleted for bug #7004 end
                rightView.setVisibility(View.VISIBLE);
            }
        }
        super.dispatchDraw(canvas);
    }
    
    public boolean handleTouchEvent(MotionEvent e) {
        Log.d(TAG, "onTouchEvent: ");
        if (getChildCount() <= 0) {
            return super.onTouchEvent(e);
        }

        boolean bRet = false;
        if (mState == STATE_SLIDING) {
            Log.d(TAG, "onTouchEvent:  filterout");
            bRet = true;
        } else {
            if (true == mGestureDetector.onTouchEvent(e)) {
                bRet = true;
            } else {
                if (mPointerId >= 0) {
                    int action = e.getActionMasked();
                    if (e.findPointerIndex(mPointerId) >= 0
                            && (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP)) {
                        slideToCorrectPos();
                        bRet = true;
                    }
                }
            }
        }
        return bRet;
    }

    @Override
    public void setDisplayedChild(int whichChild){
        mState = STATE_IDLE;
        int currentIndex = getDisplayedChild();
        super.setDisplayedChild(whichChild);
        notifyViewSlide(currentIndex, whichChild);
    }
    
    @Override
    public void removeAllViews() {
        int currentIndex = getDisplayedChild();
        super.removeAllViews();
        int newIndex = getDisplayedChild();
        notifyViewSlide(currentIndex, newIndex);
        //-1 means all
        notifySlideRemove(-1);
    }
    
    @Override
    public void removeView(View view) {
        int currentIndex = getDisplayedChild();
        int rmIndex = indexOfChild(view);
        if(rmIndex >= 0){
            int newIndex = getDisplayedChild();
            notifySlideRemove(rmIndex);
            notifyViewSlide(currentIndex, newIndex);
        }
    }
    
    @Override
    public void removeViewAt(int index) {
        super.removeViewAt(index);
        notifySlideRemove(index);
    }
    
    private void notifyViewSlide(int oldIndex, int newIndex) {
        //int nChildren = getChildCount();
        mViewSlideListener.onViewSlide(newIndex, oldIndex);
    }

    private void notifySlideRemove(int rmIndex){
        mViewSlideListener.onRemove(rmIndex);
    }
    
    private int getSlideOrDragingDirection() {
        if (mCurViewX < 0) {
            return SLIDE_LEFT;
        } else if (mCurViewX > 0) {
            return SLIDE_RIGHT;
        } else {
            return STATE_IDLE;
        }
    }

    private int getSlideOrDragingOffset() {
        return mCurViewX;
    }

    private void slideToCorrectPos() {
        int dragOffset = getSlideOrDragingOffset();
        int absOffset = Math.abs(dragOffset);
        if (absOffset > 50) {
            int direction = getSlideOrDragingDirection();
            if (SLIDE_LEFT == direction) {
                trySlideLeft();
            } else if (SLIDE_RIGHT == direction) {
                trySlideRight();
            }
        } else {
            slideOrigin();
        }
    }

    private View getRightView() {
        int curChildIndex = getDisplayedChild();
        if (curChildIndex < getChildCount() - 1) {
            return getChildAt(curChildIndex + 1);
        }
        return null;
    }

    private View getLeftView() {
        int curChildIndex = getDisplayedChild();
        if (curChildIndex > 0) {
            return getChildAt(curChildIndex - 1);
        }
        return null;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        assert (mState == STATE_IDLE);
        mPointerId = e.getPointerId(0);
        mCurViewX = 0;
        mLeftViewX = -getWidth();
        mRightViewX = getWidth();

        mState = STATE_DRAG_ANIMATION_END;
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d(TAG, "onFling:velocityX=" + velocityX + "velocityY=" + velocityY);
        int direction = getSlideOrDragingDirection();
        if (velocityX > 0) {
            if (direction == SLIDE_LEFT || velocityX < mVelocityValve) {
                slideOrigin();
            } else {
                trySlideRight();
            }
        } else if (velocityX < 0) {
            if (direction == SLIDE_RIGHT || velocityX > -mVelocityValve) {
                slideOrigin();
            } else {
                trySlideLeft();
            }
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // assert(mState == STATE_DRAGING);
        Log.d(TAG, "onScroll:distanceX=" + distanceX + ";distanceY=" + distanceY);
        mState = STATE_DRAG_ANIMATION_START;
        // Aurora xuyong 2014-07-29 deleted for bug #7004 start
        //fillStepAnimation(-(int) distanceX);
        // Aurora xuyong 2014-07-29 deleted for bug #7004 end
        View curView = getCurrentView();
        View leftView = getLeftView();
        View rightView = getRightView();
        // Aurora xuyong 2014-07-29 deleted for bug #7004 start
        //curView.startAnimation(mCurAni);
        // Aurora xuyong 2014-07-29 deleted for bug #7004 end
        
        if (leftView != null) {
            // Aurora xuyong 2014-07-29 deleted for bug #7004 start
            //leftView.startAnimation(mLeftAni);
            // Aurora xuyong 2014-07-29 deleted for bug #7004 end
        }
        if (rightView != null) {
            // Aurora xuyong 2014-07-29 deleted for bug #7004 start
            //rightView.startAnimation(mRightAni);
            // Aurora xuyong 2014-07-29 deleted for bug #7004 end
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    public void setViewSlideListener(ViewSlideListener listener) {
        mViewSlideListener = listener;
    }

    public static interface ViewSlideListener {
        public void onViewSlide(int slideIn, int slideOut);
        public void onRemove(int rmIndex);
    }
}
