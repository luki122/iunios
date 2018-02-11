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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.browser.Controller;
import com.android.browser.R;

/**
 * 用来实现层叠视图的效果
 * @author JimXia
 *
 * @date 2015年1月20日 下午2:28:45
 */
public class StackViewLayout extends FrameLayout implements
        StackView.StackViewCallbacks, StackViewScroller.TaskStackViewScrollerCallbacks,
        ViewPool.ViewPoolConsumer<StackView, View> {
    
    private static final String TAG = "StackViewLayout";

    /** The TaskView callbacks */
    public static interface StackViewLayoutCallbacks {
        void onStackViewClicked(StackViewLayout svl, View v);
        void onStackViewDismissed(View v);
        void onAllStackViewDismissed();
        void onStackViewTransformed(StackView sv, View contentView);
    }

    private List<View> mContentViewList;
    StackViewLayoutAlgorithm mLayoutAlgorithm;
    private StackViewScroller mStackScroller;
    private StackViewTouchHandler mTouchHandler;
    private StackViewLayoutCallbacks mCb;
    private ViewPool<StackView, View> mViewPool;
    private ArrayList<StackViewTransform> mCurrentTaskTransforms = new ArrayList<StackViewTransform>();

    // Optimizations
    private int mStackViewsAnimationDuration;
    private boolean mStackViewsDirty = true;
    private boolean mAwaitingFirstLayout = true;
    private int[] mTmpVisibleRange = new int[2];
    private Rect mTmpRect = new Rect();
    private StackViewTransform mTmpTransform = new StackViewTransform();
    private HashMap<View, StackView> mTmpStackViewMap = new HashMap<View, StackView>();

    private static final int ADD_OR_REMOVE_VIEW_DURATION = 200;
    private int mAddViewAnimationDuration = 0;
    private int mRemoveViewAnimationDuration = ADD_OR_REMOVE_VIEW_DURATION;
    
    public StackViewLayout(Context context) {
        this(context, null);
    }

    public StackViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mViewPool = new ViewPool<StackView, View>(context, this);
        mLayoutAlgorithm = new StackViewLayoutAlgorithm(context);
        mStackScroller = new StackViewScroller(context, mLayoutAlgorithm);
        mStackScroller.setCallbacks(this);
        mTouchHandler = new StackViewTouchHandler(context, this, mStackScroller);
    }
    
    public void setContentViewList(List<View> contentViewList) {
    	setContentViewList(contentViewList, mAddViewAnimationDuration);
    }
    
    public void setContentViewList(List<View> contentViewList, int animDuration) {
        if (contentViewList == null/* || contentViewList.isEmpty()*/) {
            throw new IllegalArgumentException("contentViewList is null or empty!!!");
        }
        Log.d("vscroll","setContentViewList contentViewList.size()=" + contentViewList.size());
        mContentViewList = contentViewList;
        requestSynchronizeStackViewsWithModel(animDuration);
        requestLayout();
    }
    
    public void setAddViewAnimationDuration(int addViewAnimationDuration) {
        if (addViewAnimationDuration <= 0) {
            addViewAnimationDuration = ADD_OR_REMOVE_VIEW_DURATION;
        }
        mAddViewAnimationDuration = addViewAnimationDuration;
    }
    
    public void setRemoveViewAnimationDuration(int removeViewAnimationDuration) {
        if (removeViewAnimationDuration <= 0) {
            removeViewAnimationDuration = ADD_OR_REMOVE_VIEW_DURATION;
        }
        mRemoveViewAnimationDuration = removeViewAnimationDuration;
    }

    public void setCallbacks(StackViewLayoutCallbacks cb) {
        mCb = cb;
    }

    /** Requests that the views be synchronized with the model */
    private void requestSynchronizeStackViewsWithModel() {
        requestSynchronizeStackViewsWithModel(0);
    }
        
    private void requestSynchronizeStackViewsWithModel(int duration) {
        if (!mStackViewsDirty) {
            invalidate();
            mStackViewsDirty = true;
        }
        if (mAwaitingFirstLayout) {
            // Skip the animation if we are awaiting first layout
            mStackViewsAnimationDuration = 0;
        } else {
            mStackViewsAnimationDuration = Math.max(mStackViewsAnimationDuration, duration);
        }
    }

    /** Finds the child view given a specific task. */
    private StackView getChildViewForTask(View v) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            StackView tv = (StackView) getChildAt(i);
            if (tv.getContentView() == v) {
                return tv;
            }
        }
        
        return null;
    }

    /**
     * Gets the stack transforms of a list of tasks, and returns the visible range of tasks.
     */
    private boolean updateStackTransforms(ArrayList<StackViewTransform> taskTransforms,
                                       List<View> viewList,
                                       float stackScroll,
                                       int[] visibleRangeOut,
                                       boolean boundTranslationsToRect) {

        int taskTransformCount = taskTransforms.size();
        int taskCount = viewList.size();
        int frontMostVisibleIndex = -1;
        int backMostVisibleIndex = -1;

        if (taskTransformCount < taskCount) {
            // If there are less transforms than tasks, then add as many transforms as necessary
            for (int i = taskTransformCount; i < taskCount; i++) {
                taskTransforms.add(new StackViewTransform());
            }
        } else if (taskTransformCount > taskCount) {
            // If there are more transforms than tasks, then just subset the transform list
            taskTransforms.subList(0, taskCount);
        }

        // Update the stack transforms
        StackViewTransform prevTransform = null;
        for (int i = taskCount - 1; i >= 0; i--) {
            StackViewTransform transform = mLayoutAlgorithm.getStackTransform(mContentViewList.get(i),
                    stackScroll, taskTransforms.get(i), prevTransform);
            if (transform.visible) {
                if (frontMostVisibleIndex < 0) {
                    frontMostVisibleIndex = i;
                }
                backMostVisibleIndex = i;
            } else {
                if (backMostVisibleIndex != -1) {
                    // We've reached the end of the visible range, so going down the rest of the
                    // stack, we can just reset the transforms accordingly
                    while (i >= 0) {
                        taskTransforms.get(i).reset();
                        i--;
                    }
                    break;
                }
            }

            if (boundTranslationsToRect) {
                transform.translationY = Math.min(transform.translationY,
                        mLayoutAlgorithm.mViewRect.bottom);
            }
            prevTransform = transform;
        }
        if (visibleRangeOut != null) {
            visibleRangeOut[0] = frontMostVisibleIndex;
            visibleRangeOut[1] = backMostVisibleIndex;
        }
        return frontMostVisibleIndex != -1 && backMostVisibleIndex != -1;
    }

    /** Synchronizes the views with the model */
    private boolean synchronizeStackViewsWithModel() {
        if (mStackViewsDirty) {
            float stackScroll = mStackScroller.getStackScroll();
            int[] visibleRange = mTmpVisibleRange;
            boolean isValidVisibleRange = updateStackTransforms(mCurrentTaskTransforms, mContentViewList,
                    stackScroll, visibleRange, false);

            // Return all the invisible children to the pool
            mTmpStackViewMap.clear();
            int childCount = getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                StackView tv = (StackView) getChildAt(i);
                View view = tv.getContentView();
                int taskIndex = mContentViewList.indexOf(view);
                if (isValidVisibleRange &&
                        (visibleRange[1] <= taskIndex && taskIndex <= visibleRange[0])) {
                    mTmpStackViewMap.put(view, tv);
                } else {
                    mViewPool.returnViewToPool(tv);
                }
            }

            // Pick up all the newly visible children and update all the existing children
            for (int i = visibleRange[0]; isValidVisibleRange && i >= visibleRange[1]; i--) {
                View view = mContentViewList.get(i);
                StackViewTransform transform = mCurrentTaskTransforms.get(i);
                StackView tv = mTmpStackViewMap.get(view);
                int taskIndex = mContentViewList.indexOf(view);
                
                Log.d("vscroll",
                		String.format("synchronizeStackViewsWithModel, transform = %s",transform.toString()));

                if (tv == null) {
                    tv = mViewPool.pickUpViewFromPool(view, view);

                    if (mStackViewsAnimationDuration > 0) {
                        // For items in the list, put them in start animating them from the
                        // approriate ends of the list where they are expected to appear
                        if (Float.compare(transform.p, 0f) <= 0) {
                            mLayoutAlgorithm.getStackTransform(0f, 0f, mTmpTransform, null);
                        } else {
                            mLayoutAlgorithm.getStackTransform(1f, 0f, mTmpTransform, null);
                        }
                        tv.updateViewPropertiesToTaskTransform(mTmpTransform, 0);
                    }
                }

                // Animate the task into place
                tv.updateViewPropertiesToTaskTransform(mCurrentTaskTransforms.get(taskIndex),
                        mStackViewsAnimationDuration);
            }

            // Reset the request-synchronize params
            mStackViewsAnimationDuration = 0;
            mStackViewsDirty = false;
            return true;
        }
        return false;
    }

    /** Updates the min and max virtual scroll bounds */
    private void updateMinMaxScroll(boolean boundScrollToNewMinMax) {
        // Compute the min and max scroll values
        mLayoutAlgorithm.computeMinMaxScroll(mContentViewList);

        // Debug logging
        if (boundScrollToNewMinMax) {
            mStackScroller.boundScroll();
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        int childCount = getChildCount();
        if (childCount > 0) {
            StackView backMostTask = (StackView) getChildAt(0);
            StackView frontMostTask = (StackView) getChildAt(childCount - 1);
            event.setFromIndex(mContentViewList.indexOf(backMostTask.getContentView()));
            event.setToIndex(mContentViewList.indexOf(frontMostTask.getContentView()));
//            event.setContentDescription(frontMostTask.getContentView());
        }
        event.setItemCount(mContentViewList.size());
        event.setScrollY(mStackScroller.mScroller.getCurrY());
        event.setMaxScrollY(mStackScroller.progressToScrollRange(mLayoutAlgorithm.mMaxScrollP));
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mTouchHandler.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mTouchHandler.onTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        mStackScroller.computeScroll();
        // Synchronize the views
        synchronizeStackViewsWithModel();
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SCROLLED);
    }

    /** Computes the stack and task rects */
    private void computeRects(int windowWidth, int windowHeight) {
        // Compute the rects in the stack algorithm
        mLayoutAlgorithm.computeRects(windowWidth, windowHeight);

        // Update the scroll bounds
        updateMinMaxScroll(false);
    }

    /**
     * This is called with the full window width and height to allow stack view children to
     * perform the full screen transition down.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if(!Controller.screenPortrait) {
        	int temp = width;
        	width = height;
        	height = temp;
        }
        
        Log.d("vscroll",String.format("onMeasure: width/height(%d,%d), mAwaitingFirstLayout = %b",
        		width, height,
        		mAwaitingFirstLayout));
        
        computeRects(width, height);

        // If this is the first layout, then scroll to the front of the stack and synchronize the
        // stack views immediately to load all the views
        if (mAwaitingFirstLayout) {
            mStackScroller.setStackScrollToInitialState();
            requestSynchronizeStackViewsWithModel();
            synchronizeStackViewsWithModel();
        }

        // Measure each of the TaskViews
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            StackView tv = (StackView) getChildAt(i);
            
            TextView titleNavBar = (TextView)tv.findViewById(R.id.title);
            String strNavBar = titleNavBar.getText().toString();
            Log.d("vscroll", 
            		String.format("onMeasure: %d) strNavBar = %s, Y = %f",
            				i, strNavBar, titleNavBar.getY()));
            
            if (tv.getBackground() != null) {
                tv.getBackground().getPadding(mTmpRect);
            } else {
                mTmpRect.setEmpty();
            }
            tv.measure(
                MeasureSpec.makeMeasureSpec(
                        mLayoutAlgorithm.mTaskRect.width() + mTmpRect.left + mTmpRect.right,
                        MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(
                        mLayoutAlgorithm.mTaskRect.height() + mTmpRect.top + mTmpRect.bottom,
                        MeasureSpec.EXACTLY));
        }

        setMeasuredDimension(width, height);
    }

    /**
     * This is called with the size of the space not including the top or right insets, or the
     * search bar height in portrait (but including the search bar width in landscape, since we want
     * to draw under it.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // Layout each of the children
        int childCount = getChildCount();
        final int pWidth = getWidth();
        for (int i = 0; i < childCount; i++) {
            StackView tv = (StackView) getChildAt(i);
            if (tv.getBackground() != null) {
                tv.getBackground().getPadding(mTmpRect);
            } else {
                mTmpRect.setEmpty();
            }
//            left = mLayoutAlgorithm.mTaskRect.left - mTmpRect.left;
            left = (pWidth - tv.getMeasuredWidth()) >>> 1;
            top = mLayoutAlgorithm.mTaskRect.top - mTmpRect.top;
            tv.layout(left,
                    top,
                    left + tv.getMeasuredWidth(),
                    top + tv.getMeasuredHeight());
            
            TextView titleNavBar = (TextView)tv.findViewById(R.id.title);
            String strNavBar = titleNavBar.getText().toString();
            Log.d("vscroll", 
            		String.format("onLayout: %d) strNavBar = %s, (%f,%f,%d,%d), (%d,%d,%d,%d)",
            				i, strNavBar, 
            				tv.getX(),
            				tv.getY(),
            				tv.getWidth(),
            				tv.getHeight(),
            				left,top,left + tv.getMeasuredWidth(),top + tv.getMeasuredHeight()));
        }
        
        Log.d("vscroll",String.format("onLayout: mAwaitingFirstLayout = %b", mAwaitingFirstLayout));

        if (mAwaitingFirstLayout) {
            mAwaitingFirstLayout = false;
            onFirstLayout();
        }
    }

    /** Handler for the first layout. */
    private void onFirstLayout() {
        int offscreenY = mLayoutAlgorithm.mViewRect.bottom -
                (mLayoutAlgorithm.mTaskRect.top - mLayoutAlgorithm.mViewRect.top);

        int childCount = getChildCount();
        // Prepare the first view for its enter animation
        for (int i = childCount - 1; i >= 0; i--) {
            StackView tv = (StackView) getChildAt(i);
            tv.prepareEnterRecentsAnimation(offscreenY);
        }
    }

    boolean isTransformedTouchPointInView(float x, float y, View child) {
        return isTransformedTouchPointInView(x, y, child, null);
    }

    /**** ViewPoolConsumer Implementation ****/

    @Override
    public StackView createView(Context context) {
        return new StackView(context);
    }

    @Override
    public void prepareViewToEnterPool(StackView tv) {
        removeView(tv);
        tv.resetViewProperties();
    }

    @Override
    public void prepareViewToLeavePool(StackView tv, View task, boolean isNewView) {
        // Rebind the task and request that this task's data be filled into the TaskView
        tv.setContentView(task);

        // Find the index where this task should be placed in the stack
        int insertIndex = -1;
        int taskIndex = mContentViewList.indexOf(task);
        if (taskIndex != -1) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View tvView = ((StackView) getChildAt(i)).getContentView();
                if (taskIndex < mContentViewList.indexOf(tvView)) {
                    insertIndex = i;
                    break;
                }
            }
        }

        if (isNewView) {
            // Set the callbacks and listeners for this new view
            tv.setTouchEnabled(true);
            tv.setCallbacks(this);
        }
        addView(tv, insertIndex);
    }

    @Override
    public boolean hasPreferredData(StackView tv, View preferredData) {
        return (tv.getContentView() == preferredData);
    }

    /**** TaskViewCallbacks Implementation ****/

    @Override
    public void onStackViewClicked(View v) {
        if (mCb != null) {
            mCb.onStackViewClicked(this, v);
        } else {
            //Toast.makeText(getContext(), ((TextView)v).getText(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onStackViewDismissed(StackView stv) {
        View v = stv.getContentView();
        // Remove the task from the view
        mContentViewList.remove(v);
        View newFrontMostView = null;
        if (mContentViewList.size() > 0) {
            newFrontMostView = mContentViewList.get(mContentViewList.size() - 1);
        }
        onStackViewRemoved(v, newFrontMostView);
    }
    
    @Override
    public void onStackViewTransformed(StackView stv) {
        if (mCb != null) {
            mCb.onStackViewTransformed(stv, stv.getContentView());
        }
    }
    
    /**
     * 删除指定的View
     * @param contentView
     */
    public void dismissChild(View contentView) {
        StackView sv = getChildViewForTask(contentView);
        if (sv != null && mTouchHandler != null && mTouchHandler.mSwipeHelper != null) {
            mTouchHandler.mSwipeHelper.dismissChild(sv);
        } else {
            Log.e(TAG, "Jim, sv: " + sv + ", mTouchHandler: " + mTouchHandler);
            if (mTouchHandler != null) {
                Log.e(TAG, "Jim, mTouchHandler.mSwipeHelper: " + mTouchHandler.mSwipeHelper);
            }
        }
    }
    
    public void addView2List(View view, int animDuration) {
        if (mContentViewList == null) {
            mContentViewList = new ArrayList<View>();
        }
        mContentViewList.add(view);
        updateMinMaxScroll(true);
        
        View anchorView = mContentViewList.get(mContentViewList.size() - 1);
        float anchorTaskScroll = mLayoutAlgorithm.getStackScrollForTask(anchorView);
        mStackScroller.setStackScroll(anchorTaskScroll);
        mStackScroller.boundScroll();
        requestSynchronizeStackViewsWithModel(animDuration);
    }
    
    /**处理滑动删除某个StackRootView之后的界面刷新*/
    private void onStackViewRemoved(View removedView, View newFrontMostView) {
        // Remove the view associated with this task, we can't rely on updateTransforms
        // to work here because the task is no longer in the list
        StackView stv = getChildViewForTask(removedView);
        if (stv != null) {
            mViewPool.returnViewToPool(stv);
        }

        // Notify the callback that we've removed the task and it can clean up after it
        if (mCb != null) {
            mCb.onStackViewDismissed(removedView);
        }

        // Get the stack scroll of the task to anchor to (since we are removing something, the front
        // most task will be our anchor task)
        View anchorView = null;
        float prevAnchorTaskScroll = 0;
        boolean pullStackForward = mContentViewList.size() > 0;
        if (pullStackForward) {
            anchorView = mContentViewList.get(mContentViewList.size() - 1);
            prevAnchorTaskScroll = mLayoutAlgorithm.getStackScrollForTask(anchorView);
        }

        // Update the min/max scroll and animate other task views into their new positions
        updateMinMaxScroll(true);

        // Offset the stack by as much as the anchor task would otherwise move back
        if (pullStackForward) {
            float anchorTaskScroll = mLayoutAlgorithm.getStackScrollForTask(anchorView);
            mStackScroller.setStackScroll(mStackScroller.getStackScroll() + (anchorTaskScroll
                    - prevAnchorTaskScroll));
            mStackScroller.boundScroll();
        }

        // Animate all the tasks into place
        requestSynchronizeStackViewsWithModel(mRemoveViewAnimationDuration);

        // Update the new front most task
        if (newFrontMostView != null) {
            StackView frontTv = getChildViewForTask(newFrontMostView);
            if (frontTv != null) {
                frontTv.setContentView(newFrontMostView);
            }
        }

        // If there are no remaining tasks, then either unfilter the current stack, or just close
        // the activity if there are no filtered stacks
        if (mContentViewList.size() == 0 && mCb != null) {
            mCb.onAllStackViewDismissed();
        }
    }

    /**** TaskStackViewScroller.TaskStackViewScrollerCallbacks ****/
    @Override
    public void onScrollChanged(float p) {
        requestSynchronizeStackViewsWithModel();
        postInvalidateOnAnimation();
    }
    
    /**
     * 
     * Vulcan created this method in 2015年1月31日 下午3:46:41 .
     */
    public void finishScroller() {
    	mStackScroller.finishScroller();
    }
    
    /**
     * 
     * Vulcan created this method in 2015年1月31日 下午3:47:52 .
     * @return
     */
    public int getScrollerX() {
    	return mStackScroller.getScrollerX();
    }
    
    /**
     * 
     * Vulcan created this method in 2015年1月31日 下午3:47:56 .
     * @return
     */
    public int getScrollerY() {
    	return mStackScroller.getScrollerY();
    }
    
    /**
     * 
     * Vulcan created this method in 2015年2月2日 下午2:41:34 .
     * @param v
     * @return
     */
    public int getChildIndex(View v) {
        if (v == null || mContentViewList == null) {
            return -1;
        }
        
        return mContentViewList.indexOf(v);
    }
}