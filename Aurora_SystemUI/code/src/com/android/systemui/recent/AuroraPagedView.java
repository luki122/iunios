/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.systemui.recent;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;
import com.android.systemui.R;


import java.util.ArrayList;
import android.animation.AnimatorSet;

//Aurora <tongyh> <2013-12-13> add recents rubbish animation begin
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;
//Aurora <tongyh> <2013-12-13> add recents rubbish animation end
import android.widget.FrameLayout;
import android.view.LayoutInflater;

import com.android.systemui.statusbar.BaseStatusBar;

import android.content.res.Configuration;
import android.view.Gravity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Intent;

import com.android.systemui.totalCount.CountUtil;

/**
 * An abstraction of the original Workspace which supports browsing through a
 * sequential list of "pages"
 */


public abstract class AuroraPagedView extends ViewGroup{
    private static final String TAG = "AuroraPagedView";
    private static final boolean DEBUG = false;
    protected static final int INVALID_PAGE = -1;

	protected static final int PAGE_VIEW_NUM = 4;
	
    // Aurora <Felix.Duan> <2014-9-9> <BEGIN> Fix BUG #7413. Pull down to lock icon distance transfer to DPI units
    private static float CLEAR_LOCK_OFFSET_Y;
    // Aurora <Felix.Duan> <2014-9-9> <END> Fix BUG #7413. Pull down to lock icon distance transfer to DPI units

    // the min drag distance for a fling to register, to prevent random page shifts
    private static final int MIN_LENGTH_FOR_FLING = 25;

    protected static final int PAGE_SNAP_ANIMATION_DURATION = 550;
    protected static final int MAX_PAGE_SNAP_DURATION = 750;
    protected static final int SLOW_PAGE_SNAP_ANIMATION_DURATION = 950;
    protected static final float NANOTIME_DIV = 1000000000.0f;

    private static final float OVERSCROLL_ACCELERATE_FACTOR = 2;
    private static final float OVERSCROLL_DAMP_FACTOR = 0.14f;

    private static final float RETURN_TO_ORIGINAL_PAGE_THRESHOLD = 0.33f;
    // The page is moved more than halfway, automatically move to the next page on touch up.
    private static final float SIGNIFICANT_MOVE_THRESHOLD = 0.4f;

    // The following constants need to be scaled based on density. The scaled versions will be
    // assigned to the corresponding member variables below.
    private static final int FLING_THRESHOLD_VELOCITY = 500;
    private static final int MIN_SNAP_VELOCITY = 1500;
    private static final int MIN_FLING_VELOCITY = 250;

    static final int AUTOMATIC_PAGE_SPACING = -1;

    protected int mFlingThresholdVelocity;
    protected int mMinFlingVelocity;
    protected int mMinSnapVelocity;

    protected float mDensity;
    protected float mSmoothingTime;

    protected boolean mFirstLayout = true;

    protected int mCurrentPage;
    protected int mNextPage = INVALID_PAGE;
    protected int mMaxScrollX;
    protected Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private View mMovingChildView;

    protected float mTouchX;
    protected float mTouchY;
    
    private float mDownMotionX;
    private float mDownMotionY;//systemui

    protected float mLastMotionX;
    protected float mLastMotionY;
		
    protected float mTotalMotionX;	
    protected float mTotalMotionY;

    protected float mLastMotionXRemainder;
    protected float mLastMotionYRemainder;


    private int mLastScreenCenter = -1;
    private int[] mChildOffsets;
    private int[] mChildRelativeOffsets;
    private int[] mChildOffsetsWithLayoutScale;

    protected final static int TOUCH_STATE_REST = 0;
    protected final static int TOUCH_STATE_SCROLLING = 1;
    protected final static int TOUCH_STATE_PREV_PAGE = 2;
    protected final static int TOUCH_STATE_NEXT_PAGE = 3;
    
    protected final static int TOUCH_STATE_CLEAR_LOCK = 4;
    
    protected final static float ALPHA_QUANTIZE_LEVEL = 0.0001f;

    protected int mTouchState = TOUCH_STATE_REST;
    protected boolean mForceScreenScrolled = false;

    // Aurora <Felix.Duan> <2014-3-28> <BEGIN> Optimize remove task animation v2
    // Aurora <felix.duan> <2014-3-20> <BEGIN> Optimize remove task animation
    private static final int TRASH_ANIMATION_DURATION1 = 250;//300;       // When one item to animatem to animate
    private static final int TRASH_ANIMATION_DURATION2 = 350;//400;       // When items <= 4 items to animate
    private static final int TRASH_ANIMATION_DURATION3 = 450;//500;       // When items > 4 to animate
    private static final int TRASH_ANIMATION_LOCKED_DURATION = 300; // Locked items animation duration
    private static final int OFFSET_STEP = 255;//260;               // Offset between two animation views
    private static final int OFFSET_BASE = 295;                     // Offset base position of X axis
    // Aurora <Felix.Duan> <2014-9-18> <BEGIN> Add animation of swipe up icon on recents panel
    private static int RECENT_PAGE_PADDING;         // Visible part of recent page padding
    private static int TRASH_ANIMATION_OFFSET_Y_START;  // Offset start position of Y axis
    private static int TRASH_ANIMATION_OFFSET_Y_END;    // Offset end position of Y axis

    private static int TRASH_ANIMATION_OFFSET_X_START;  // Offset start position of Y axis
    private static int TRASH_ANIMATION_OFFSET_X_END;    // Offset end position of Y axis

    // Aurora <Felix.Duan> <2014-9-18> <END> Add animation of swipe up icon on recents panel
    private static final int ON_STAGE_X = 805;//800;
    private static final int OFF_STAGE_X = ON_STAGE_X + OFFSET_STEP;//3000;//1200;//1030;
    // Aurora <felix.duan> <2014-3-20> <END> Optimize remove task animation
    // Aurora <Felix.Duan> <2014-3-28> <END> Optimize remove task animation v2

    protected OnLongClickListener mLongClickListener;
    private boolean isHasLock = false;
    protected ViewGroup.OnHierarchyChangeListener mHierarchyChangeListener = new ViewGroup.OnHierarchyChangeListener(){

        @Override
        public void onChildViewAdded(View parent, View child) {
            // This ensures that when children are added, they get the correct transforms / alphas
            // in accordance with any scroll effects.
            mForceScreenScrolled = true;
            invalidate();
            invalidateCachedOffsets();
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {
        }
    	
    };
    protected boolean mAllowLongPress = true;

    protected int mTouchSlop;
    private int mPagingTouchSlop;
    private int mMaximumVelocity;
    private int mMinimumWidth;
    protected int mPageSpacing;
    protected int mPageLayoutPaddingTop;
    protected int mPageLayoutPaddingBottom;
    protected int mPageLayoutPaddingLeft;
    protected int mPageLayoutPaddingRight;
    protected int mPageLayoutWidthGap;
    protected int mPageLayoutHeightGap;
    protected int mCellCountX = 0;
    protected int mCellCountY = 0;
    protected boolean mCenterPagesVertically;
    protected boolean mAllowOverScroll = true;
    protected int mUnboundedScrollX;
    protected int mUnboundedScrollY;
    protected int[] mTempVisiblePagesRange = new int[2];
    protected boolean mForceDrawAllChildrenNextFrame;

    // mOverScrollX is equal to getScrollX() when we're within the normal scroll range. Otherwise
    // it is equal to the scaled overscroll position. We use a separate value so as to prevent
    // the screens from continuing to translate beyond the normal bounds.
    protected int mOverScrollX;

    // parameter that adjusts the layout to be optimized for pages with that scale factor
    protected float mLayoutScale = 1.0f;

    protected static final int INVALID_POINTER = -1;

    protected int mActivePointerId = INVALID_POINTER;

    private PageSwitchListener mPageSwitchListener;

    protected ArrayList<Boolean> mDirtyPageContent;

    // If true, syncPages and syncPageItems will be called to refresh pages
    protected boolean mContentIsRefreshable = true;

    // If true, modify alpha of neighboring pages as user scrolls left/right
    protected boolean mFadeInAdjacentScreens = false;

    // It true, use a different slop parameter (pagingTouchSlop = 2 * touchSlop) for deciding
    // to switch to a new page
    protected boolean mUsePagingTouchSlop = true;

    // If true, the subclass should directly update scrollX itself in its computeScroll method
    // (SmoothPagedView does this)
    protected boolean mDeferScrollUpdate = false;

    protected boolean mIsPageMoving = false;

    // All syncs and layout passes are deferred until data is ready.
    protected boolean mIsDataReady = false;

    // Scrolling indicator
    private ValueAnimator mScrollIndicatorAnimator;
    private View mScrollIndicator;
    private int mScrollIndicatorPaddingLeft;
    private int mScrollIndicatorPaddingRight;
    private boolean mHasScrollIndicator = true;
    private boolean mShouldShowScrollIndicator = false;
    private boolean mShouldShowScrollIndicatorImmediately = false;
    protected static final int sScrollIndicatorFadeInDuration = 150;
    protected static final int sScrollIndicatorFadeOutDuration = 650;
    protected static final int sScrollIndicatorFlashDuration = 650;
    private boolean mScrollingPaused = false;

    // If set, will defer loading associated pages until the scrolling settles
    private boolean mDeferLoadAssociatedPagesUntilScrollCompletes;

	private int mMinScrollX;
	//Aurora <tongyh> <2013-12-15> solve touch force close begin
//	private boolean mNeedToClean = false;
	public boolean mNeedToClean = false;

	public void setmNeedToClean(boolean mNeedToClean) {
		this.mNeedToClean = mNeedToClean;
	}
	
	public boolean getmNeedToClean() {
		return mNeedToClean;
	}

	//Aurora <tongyh> <2013-12-15> solve touch force close end
	//Aurora <tongyh> <2013-12-13> add recents rubbish animation begin
	private AuroraRubbishView recentsRubbish = null;
//	private AnimationDrawable recentsRubbishAnimDrawable = null;
	//Aurora <tongyh> <2013-12-13> add recents rubbish animation end
	//Aurora <tongyh> <2013-12-13> add recents rubbish animation begin
    // Aurora <Felix.Duan> <2014-3-28> <BEGIN> Optimize remove task animation v2
    // private ArrayList<View> mUnlockedViews = new ArrayList<View>();
    // private ArrayList<View> mLockedViews = new ArrayList<View>();
	// List of current tasks, both locked & unlocked
	private ArrayList<AuroraRecentlItemView> mIconViews = new ArrayList<AuroraRecentlItemView>();
    // Aurora <Felix.Duan> <2014-3-28> <END> Optimize remove task animation v2
	private FrameLayout recents;
	private ImageView rubbishView;
	private Context mContext;
	//Aurora <tongyh> <2013-12-13> add recents rubbish animation end
    public interface PageSwitchListener {
        void onPageSwitch(View newPage, int newPageIndex);
    }

    public AuroraPagedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("1218", "AuroraPagedView----construct");
        mContext = context;
        mPageLayoutPaddingTop = 0;
        mPageLayoutPaddingBottom =  0;
        mPageLayoutPaddingLeft =  0;
        mPageLayoutPaddingRight =  0;
        mPageLayoutWidthGap =  0;
        mPageLayoutHeightGap =  0;
        mScrollIndicatorPaddingLeft =  0;
        mScrollIndicatorPaddingRight = 0;
           
        setHapticFeedbackEnabled(false);
        init();
    }


    /**
     * Initializes various states for this workspace.
     */
    protected void init() {
    	Log.d("1218", "AuroraPagedView----init()");
        mDirtyPageContent = new ArrayList<Boolean>();
        mDirtyPageContent.ensureCapacity(32);
        mScroller = new Scroller(getContext(), new ScrollInterpolator());
        mCurrentPage = 0;
        mCenterPagesVertically = true;

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mPagingTouchSlop = configuration.getScaledPagingTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mDensity = getResources().getDisplayMetrics().density;

        mFlingThresholdVelocity = (int) (FLING_THRESHOLD_VELOCITY * mDensity);
        mMinFlingVelocity = (int) (MIN_FLING_VELOCITY * mDensity);
        mMinSnapVelocity = (int) (MIN_SNAP_VELOCITY * mDensity);
        setOnHierarchyChangeListener(mHierarchyChangeListener);

		mNeedToClean = false;
		
        // Aurora <Felix.Duan> <2014-9-9> <BEGIN> Fix BUG #7413. Pull down to lock icon distance transfer to DPI units
        CLEAR_LOCK_OFFSET_Y = getResources().getDimensionPixelSize(R.dimen.aurora_clear_lock_offset_y);
        // Aurora <Felix.Duan> <2014-9-9> <END> Fix BUG #7413. Pull down to lock icon distance transfer to DPI units

        // Aurora <Felix.Duan> <2014-9-18> <BEGIN> Add animation of swipe up icon on recents panel
        RECENT_PAGE_PADDING = getResources().getDimensionPixelSize(R.dimen.recent_page_padding);
		updateOffsetValues();

    }

	private boolean orientationChangeFlag = false;

	int clearViewMeasureWidth;
	int clearViewMeasureHeight;
	int pageNormalViewMeasureWidth;
	int pageNormalViewMeasureHeight;

	int clearViewMeasureWidthLand;
	int clearViewMeasureHeightLand;
	int pageNormalViewMeasureWidthLand;
	int pageNormalViewMeasureHeightLand;


	//as orientation changed, the value must be different
	private void updateOffsetValues(){
		TRASH_ANIMATION_OFFSET_Y_START = getResources().getDimensionPixelSize(R.dimen.recents_iconview_top_padding);
        TRASH_ANIMATION_OFFSET_Y_END = getResources().getDimensionPixelSize(R.dimen.recents_iconview_top_padding);
        TRASH_ANIMATION_OFFSET_X_START = getResources().getDimensionPixelSize(R.dimen.recents_iconview_left_padding_land);
        TRASH_ANIMATION_OFFSET_X_END = getResources().getDimensionPixelSize(R.dimen.recents_iconview_left_padding_land);

		clearViewMeasureWidth = getResources().getDimensionPixelSize(R.dimen.recent_clearview_width);
		clearViewMeasureHeight = getResources().getDimensionPixelSize(R.dimen.recent_clearview_height);
		pageNormalViewMeasureWidth = getResources().getDimensionPixelSize(R.dimen.recent_pagenormal_width);
		pageNormalViewMeasureHeight = getResources().getDimensionPixelSize(R.dimen.recent_pagenormal_height);

		clearViewMeasureWidthLand = getResources().getDimensionPixelSize(R.dimen.recent_clearview_width_land);
		clearViewMeasureHeightLand = getResources().getDimensionPixelSize(R.dimen.recent_clearview_height_land);
		pageNormalViewMeasureWidthLand = getResources().getDimensionPixelSize(R.dimen.recent_pagenormal_width_land);
		pageNormalViewMeasureHeightLand = getResources().getDimensionPixelSize(R.dimen.recent_pagenormal_height_land);

	}

    public void setPageSwitchListener(PageSwitchListener pageSwitchListener) {
        mPageSwitchListener = pageSwitchListener;
        if (mPageSwitchListener != null) {
            mPageSwitchListener.onPageSwitch(getPageAt(mCurrentPage), mCurrentPage);
        }
    }

    /**
     * Called by subclasses to mark that data is ready, and that we can begin loading and laying
     * out pages.
     */
    protected void setDataIsReady() {
        mIsDataReady = true;
    }
    protected boolean isDataReady() {
        return mIsDataReady;
    }

    /**
     * Returns the index of the currently displayed page.
     *
     * @return The index of the currently displayed page.
     */
    int getCurrentPage() {
        return mCurrentPage;
    }
    int getNextPage() {
        return (mNextPage != INVALID_PAGE) ? mNextPage : mCurrentPage;
    }

    int getPageCount() {
        return getChildCount();
    }

    View getPageAt(int index) {
        View view = getChildAt(index);
        if (view == null) {
            // Log.e("felix", "AuroraPgedView.DEBUG getPageAt(" + index + ") is null.   getChildCount() = " + getChildCount());
        }
        return view;
    }

    protected int indexToPage(int index) {
        return index;
    }

    /**
     * Updates the scroll of the current page immediately to its final scroll position.  We use this
     * in CustomizePagedView to allow tabs to share the same PagedView while resetting the scroll of
     * the previous tab page.
     */
    protected void updateCurrentPageScroll() {
        // If the current page is invalid, just reset the scroll position to zero
        int newX = 0;
		int newY = 0;
        if (0 <= mCurrentPage && mCurrentPage < getPageCount()) {
            int offset = getChildOffset(mCurrentPage);
            int relOffset = getRelativeChildOffset(mCurrentPage);
			if(isOrientationPortrait()){
				newX = offset - relOffset;
				newY = 0;
			}else{
				newX = 0;
				newY = relOffset - offset ;
			}
        }
        scrollTo(newX, newY);
        mScroller.setFinalX(newX);
        mScroller.forceFinished(true);
    }

    /**
     * Called during AllApps/Home transitions to avoid unnecessary work. When that other animation
     * ends, {@link #resumeScrolling()} should be called, along with
     * {@link #updateCurrentPageScroll()} to correctly set the final state and re-enable scrolling.
     */
    void pauseScrolling() {
        mScroller.forceFinished(true);
        cancelScrollingIndicatorAnimations();
        mScrollingPaused = true;
    }

    /**
     * Enables scrolling again.
     * @see #pauseScrolling()
     */
    void resumeScrolling() {
        mScrollingPaused = false;
    }
    /**
     * Sets the current page.
     */
    void setCurrentPage(int currentPage) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        // don't introduce any checks like mCurrentPage == currentPage here-- if we change the
        // the default
        if (getChildCount() == 0) {
            return;
        }


        mCurrentPage = Math.max(0, Math.min(currentPage, getPageCount() - 1));
        updateCurrentPageScroll();
        updateScrollingIndicator();
        notifyPageSwitchListener();
        invalidate();
    }

    protected void notifyPageSwitchListener() {
        if (mPageSwitchListener != null) {
            mPageSwitchListener.onPageSwitch(getPageAt(mCurrentPage), mCurrentPage);
        }
    }

    protected void pageBeginMoving() {
        if (!mIsPageMoving) {
            mIsPageMoving = true;
            onPageBeginMoving();
        }
    }

    protected void pageEndMoving() {
        if (mIsPageMoving) {
            mIsPageMoving = false;
            onPageEndMoving();
        }
    }

    protected boolean isPageMoving() {
        return mIsPageMoving;
    }

    // a method that subclasses can override to add behavior
    protected void onPageBeginMoving() {
    }

    // a method that subclasses can override to add behavior
    protected void onPageEndMoving() {
    }

    /**
     * Registers the specified listener on each page contained in this workspace.
     *
     * @param l The listener used to respond to long clicks.
     */

	private int lastOrientation = getResources().getConfiguration().orientation;
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		invalidateCachedOffsets();
		resetOffset();
		requestLayout();
		lastOrientation = newConfig.orientation;
	}

	private void resetOffset(){
		mFirstLayout = true;
		mUnboundedScrollX = 0;
		mUnboundedScrollY = 0;
		mOverScrollX = 0;
		mCurrentPage = getPageCount()>1 ? 1 : 0;
	}

    @Override
    public void scrollBy(int x, int y) {
		if(isOrientationPortrait()){
			scrollTo(mUnboundedScrollX + x, getScrollY() + y);
		} else {
			scrollTo(getScrollX() + x, mUnboundedScrollY + y);
		}
    }

    @Override
    public void scrollTo(int x, int y) {
   		
        mUnboundedScrollX = x;
        mUnboundedScrollY = y;
		if(isOrientationPortrait()){
			if (x < mMinScrollX) {
            	super.scrollTo(mMinScrollX, y);
            	if (mAllowOverScroll) {
                	overScroll(x - mMinScrollX);
            	}
	        } else if (x > mMaxScrollX) {
	            super.scrollTo(mMaxScrollX, y);
	            if (mAllowOverScroll) {
	                overScroll(x - mMaxScrollX);
	            }
	        } else {
	            mOverScrollX = x;
	            super.scrollTo(x, y);
	        }
		}else{
			if (y > -mMinScrollX) {
	            super.scrollTo(x, -mMinScrollX);
	            if (mAllowOverScroll) {
	                overScroll(y + mMinScrollX);
	            }
	        } else if (y < -mMaxScrollX) {
	            super.scrollTo(x, -mMaxScrollX);
	            if (mAllowOverScroll) {
	                overScroll(y + mMaxScrollX);
	            }
	        } else {
	            mOverScrollX = y;
	            super.scrollTo(x, y);
	        }
		}
        mTouchX = x;
		mTouchY = y;
        mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
        // Aurora <Felix.Duan> <2014-3-28> <BEGIN> Optimize remove task animation v2
        // Open or close rubbish bin on scroll
        if (recentsRubbish != null)
            recentsRubbish.rotate(mOverScrollX);
        // Aurora <Felix.Duan> <2014-3-28> <END> Optimize remove task animation v2
    }

    // we moved this functionality to a helper function so SmoothPagedView can reuse it
    protected boolean computeScrollHelper() {
        if (mScroller.computeScrollOffset()) {
            // Don't bother scrolling if the page does not need to be moved
            if (getScrollX() != mScroller.getCurrX()
                || getScrollY() != mScroller.getCurrY()
                || mOverScrollX != (isOrientationPortrait() ? mScroller.getCurrX() : mScroller.getCurrY())) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            invalidate();
            return true;
        } else if (mNextPage != INVALID_PAGE) {
            mCurrentPage = Math.max(0, Math.min(mNextPage, getPageCount() - 1));
            mNextPage = INVALID_PAGE;
            notifyPageSwitchListener();

            // Load the associated pages if necessary
            if (mDeferLoadAssociatedPagesUntilScrollCompletes) {
                loadAssociatedPages(mCurrentPage);
                mDeferLoadAssociatedPagesUntilScrollCompletes = false;
            }

            // We don't want to trigger a page end moving unless the page has settled
            // and the user has stopped scrolling
            if (mTouchState == TOUCH_STATE_REST) {
                pageEndMoving();
            }

            // Notify the user when the page changes
            AccessibilityManager accessibilityManager = (AccessibilityManager)
                    getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (accessibilityManager.isEnabled()) {
                AccessibilityEvent ev =
                    AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_SCROLLED);
                ev.getText().add(getCurrentPageDescription());
                sendAccessibilityEventUnchecked(ev);
            }

			if(mNeedToClean){
				isHasLock = false;
                // Aurora <Felix.Duan> <2014-4-10> <BEGIN> Always play locked view darken animation
		        loadLockedOrUnlockedViews();
				// removeUnlockedViews();
                // Aurora <Felix.Duan> <2014-4-10> <END> Always play locked view darken animation
				//Aurora <tongyh> <2013-12-15> solve touch force close begin
				if(isHasLock){
					mNeedToClean = false;
				}
//				mNeedToClean = false;
				//Aurora <tongyh> <2013-12-15> solve touch force close end
			}
			
            return true;
        }
        return false;
    }

    @Override
    public void computeScroll() {
        computeScrollHelper();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
android.util.Log.e("AuroraPagedView", "\nPagedViewOnMeasure onMeasure mIsDataReady: " + mIsDataReady);
        if (!mIsDataReady) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
        }

        // Return early if we aren't given a proper dimension
        if (widthSize <= 0 || heightSize <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        /* Allow the height to be set as WRAP_CONTENT. This allows the particular case
         * of the All apps view on XLarge displays to not take up more space then it needs. Width
         * is still not allowed to be set as WRAP_CONTENT since many parts of the code expect
         * each page to have the same width.
         */
        int maxChildHeight = 0;

        final int verticalPadding = getPaddingTop() + getPaddingBottom();
        final int horizontalPadding = getPaddingLeft() + getPaddingRight();

		int clearMeasureWidth = clearViewMeasureWidth;
		int clearMeasureHeight = clearViewMeasureHeight;
		int pageMeasureWidth = pageNormalViewMeasureWidth;
		int pageMeasureHeight = pageNormalViewMeasureHeight;

		if(!isOrientationPortrait()){
			clearMeasureWidth = clearViewMeasureWidthLand;
			clearMeasureHeight = clearViewMeasureHeightLand;
			pageMeasureWidth = pageNormalViewMeasureWidthLand;
			pageMeasureHeight = pageNormalViewMeasureHeightLand;
		}


        // The children are given the same width and height as the workspace
        // unless they were set to WRAP_CONTENT
        if (DEBUG) Log.d(TAG, "PagedView.onMeasure(): " + widthSize + ", " + heightSize);
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            // disallowing padding in paged view (just pass 0)
            final View child = getPageAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int childWidthMode;
            if (lp.width == LayoutParams.WRAP_CONTENT) {
                childWidthMode = MeasureSpec.AT_MOST;
            } else {
                childWidthMode = MeasureSpec.EXACTLY;
            }

            int childHeightMode;
            if (lp.height == LayoutParams.WRAP_CONTENT) {
                childHeightMode = MeasureSpec.AT_MOST;
            } else {
                childHeightMode = MeasureSpec.EXACTLY;
            }

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(pageMeasureWidth, childWidthMode);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(pageMeasureHeight, childHeightMode);
			if(i == 0){
				childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(clearMeasureWidth, childWidthMode);
        		childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(clearMeasureHeight, childHeightMode);
			}
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            maxChildHeight = Math.max(maxChildHeight, child.getMeasuredHeight());
            if (DEBUG) Log.d(TAG, "\tmeasure-child" + i + ": " + child.getMeasuredWidth() + ", "
                    + child.getMeasuredHeight());
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = maxChildHeight + verticalPadding;
        }

        setMeasuredDimension(widthSize, heightSize);

        // We can't call getChildOffset/getRelativeChildOffset until we set the measured dimensions.
        // We also wait until we set the measured dimensions before flushing the cache as well, to
        // ensure that the cache is filled with good values.
        invalidateCachedOffsets();

        if (childCount > 0) {
            if (DEBUG) Log.d(TAG, "getRelativeChildOffset(): " + getMeasuredWidth() + ", "
                    + getChildWidth(0));

            // Calculate the variable page spacing if necessary
            if (mPageSpacing == AUTOMATIC_PAGE_SPACING) {
                // The gap between pages in the PagedView should be equal to the gap from the page
                // to the edge of the screen (so it is not visible in the current screen).  To
                // account for unequal padding on each side of the paged view, we take the maximum
                // of the left/right gap and use that as the gap between each page.
                int offset = getRelativeChildOffset(0);
                int spacing = Math.max(offset, widthSize - offset -
                        getChildAt(0).getMeasuredWidth());
                setPageSpacing(spacing);
            }
        }

        updateScrollingIndicatorPosition();
        if (childCount > 0) {
            mMaxScrollX = getChildOffset(childCount - 1) - getRelativeChildOffset(childCount - 1);
			if(childCount > 1)
				mMinScrollX = getChildOffset(1);
			else
				mMinScrollX = 0;
        } else {
            mMaxScrollX = 0;
			mMinScrollX = 0;
        }
      //Aurora <tongyh> <2013-12-13> add recents rubbish animation begin
//        auroraInitRubbishAnim();
      //Aurora <tongyh> <2013-12-13> add recents rubbish animation end
    }

    protected void scrollToNewPageWithoutMovingPages(int newCurrentPage) {
        int newX = getChildOffset(newCurrentPage) - getRelativeChildOffset(newCurrentPage);
        int delta = newX - getScrollX();

        final int pageCount = getChildCount();
        for (int i = 0; i < pageCount; i++) {
            View page = (View) getPageAt(i);
            page.setX(page.getX() + delta);
        }
        setCurrentPage(newCurrentPage);
    }

    // A layout scale of 1.0f assumes that the pages, in their unshrunken state, have a
    // scale of 1.0f. A layout scale of 0.8f assumes the pages have a scale of 0.8f, and
    // tightens the layout accordingly
    public void setLayoutScale(float childrenScale) {
        mLayoutScale = childrenScale;
        invalidateCachedOffsets();

        // Now we need to do a re-layout, but preserving absolute X and Y coordinates
        int childCount = getChildCount();
        float childrenX[] = new float[childCount];
        float childrenY[] = new float[childCount];
        for (int i = 0; i < childCount; i++) {
            final View child = getPageAt(i);
            childrenX[i] = child.getX();
            childrenY[i] = child.getY();
        }
        // Trigger a full re-layout (never just call onLayout directly!)
        int widthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
        requestLayout();
        measure(widthSpec, heightSpec);
        layout(getLeft(), getTop(), getRight(), getBottom());
        for (int i = 0; i < childCount; i++) {
            final View child = getPageAt(i);
            child.setX(childrenX[i]);
            child.setY(childrenY[i]);
        }

        // Also, the page offset has changed  (since the pages are now smaller);
        // update the page offset, but again preserving absolute X and Y coordinates
        scrollToNewPageWithoutMovingPages(mCurrentPage);
    }

    public void setPageSpacing(int pageSpacing) {
        mPageSpacing = pageSpacing;
        invalidateCachedOffsets();
    }

	private boolean isOrientationPortrait(){
		int orientation = mContext.getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT) return true;
		return false;
	}

	private int landStartY = getResources().getDimensionPixelSize(R.dimen.recent_page_land_startY);
	private int landPaddingStart = getResources().getDimensionPixelSize(R.dimen.recent_page_land_padding_start);
	
	private void landLayout(int left, int top, int right, int bottom){
        if (DEBUG) Log.d(TAG, "PagedView.onLayout()");
        final int verticalPadding = getPaddingTop() + getPaddingBottom();
        final int childCount = getChildCount();
        /*int childTop = getHeight();
 		int childLeft = getPaddingLeft();*/

		int paddingStart = landPaddingStart; //720p 4dp
		int childTop = landStartY; //360dp width match_parent
		int childLeft = 0; // 104dp

        for (int i = 0; i < childCount; i++) {
            final View child = getPageAt(i);
            if (child.getVisibility() != View.GONE) {
                /*final int childWidth = getScaledMeasuredWidth(child);
                final int childHeight = child.getMeasuredHeight();*/

				int childWidth = child.getMeasuredWidth();// 208dp
		        int childHeight = child.getMeasuredHeight();

                child.layout(childLeft, childTop - childHeight,
                        childLeft + childWidth, childTop);
                childTop = childTop - (childHeight + mPageSpacing);

            }
        }

        if (mFirstLayout && mCurrentPage >= 0 && mCurrentPage < getChildCount()) {
            setHorizontalScrollBarEnabled(false);
            updateCurrentPageScroll();
            setHorizontalScrollBarEnabled(true);
            mFirstLayout = false;
        }
	}
	
	private void porLayout(int left, int top, int right, int bottom){
        if (DEBUG) Log.d(TAG, "PagedView.onLayout()");
        final int verticalPadding = getPaddingTop() + getPaddingBottom();
        final int childCount = getChildCount();
        int childLeft = getRelativeChildOffset(0);

        for (int i = 0; i < childCount; i++) {
            final View child = getPageAt(i);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = getScaledMeasuredWidth(child);
                final int childHeight = child.getMeasuredHeight();
                int childTop = getPaddingTop();
                if (mCenterPagesVertically) {
                    childTop += ((getMeasuredHeight() - verticalPadding) - childHeight) / 2;
                }

                if (DEBUG) Log.d(TAG, "\tlayout-child" + i + ": " + childLeft + ", " + childTop);
                child.layout(childLeft, childTop,
                        childLeft + child.getMeasuredWidth(), childTop + childHeight);
                childLeft += childWidth + mPageSpacing;
            }
        }

        if (mFirstLayout && mCurrentPage >= 0 && mCurrentPage < getChildCount()) {
            setHorizontalScrollBarEnabled(false);
            updateCurrentPageScroll();
            setHorizontalScrollBarEnabled(true);
            mFirstLayout = false;
        }
	}
	
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!mIsDataReady) {
            return;
        }
android.util.Log.e("AuroraPagedView", "\nAurora Paged View onLayout ");

		if(isOrientationPortrait()){
			porLayout(left, top, right, bottom);
		} else {
			landLayout(left, top, right, bottom);
		}
    }

    protected void screenScrolled(int screenCenter) {
        if (isScrollingIndicatorEnabled()) {
            updateScrollingIndicator();
        }
        boolean isInOverscroll = mOverScrollX < mMinScrollX || mOverScrollX > mMaxScrollX;

        if (mFadeInAdjacentScreens && !isInOverscroll) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child != null) {
                    float scrollProgress = getScrollProgress(screenCenter, child, i);
                    float alpha = 1 - Math.abs(scrollProgress);
                    child.setAlpha(alpha);
                }
            }
            invalidate();
        }
    }



    protected void invalidateCachedOffsets() {
        int count = getChildCount();
        if (count == 0) {
            mChildOffsets = null;
            mChildRelativeOffsets = null;
            mChildOffsetsWithLayoutScale = null;
            return;
        }

        mChildOffsets = new int[count];
        mChildRelativeOffsets = new int[count];
        mChildOffsetsWithLayoutScale = new int[count];
        for (int i = 0; i < count; i++) {
            mChildOffsets[i] = -1;
            mChildRelativeOffsets[i] = -1;
            mChildOffsetsWithLayoutScale[i] = -1;
        }
    }

    protected int getChildOffset(int index) {

        int[] childOffsets = Float.compare(mLayoutScale, 1f) == 0 ?
                mChildOffsets : mChildOffsetsWithLayoutScale;
        if (childOffsets != null && childOffsets[index] != -1) {
            return childOffsets[index];
        } else {
            if (getChildCount() == 0)
                return 0;

            int offset = getRelativeChildOffset(0);
			if(isOrientationPortrait()){
	            for (int i = 0; i < index; ++i) {
					View child = getChildAt(i);
					if(i == 0){
						offset += clearViewMeasureWidth;
					}else{
						offset += pageNormalViewMeasureWidth;
					}
					//offset += child.getMeasuredWidth();
				}
			}else {
	            for (int i = 0; i < index; ++i) {
					View child = getChildAt(i);
					if(i == 0){
						offset += clearViewMeasureHeightLand;
					}else{
						offset += pageNormalViewMeasureHeightLand;
					}
					//offset += child.getMeasuredHeight();
				}	
			}

            if (childOffsets != null) {
                childOffsets[index] = offset;
            }
            return offset;
        }
    }

    protected int getRelativeChildOffset(int index) {
        if (mChildRelativeOffsets != null && mChildRelativeOffsets[index] != -1) {
            return mChildRelativeOffsets[index];
        } else {
			int padding = 0;
			int offset = 0;
			if(isOrientationPortrait()){
		        padding = getPaddingLeft() + getPaddingRight();
		        offset = getPaddingLeft() + (getMeasuredWidth() - padding - getChildWidth(index)) / 2;
			} else {
				padding = getPaddingTop() + getPaddingBottom();
				offset = getPaddingTop() + (getMeasuredHeight() - padding - getChildHeight(index)) / 2;
			}

			if(index == 0){
				offset = 0;
			}

            if (mChildRelativeOffsets != null) {
                mChildRelativeOffsets[index] = offset;
            }
            return offset;
        }
    }

    protected int getScaledMeasuredWidth(View child) {
        // This functions are called enough times that it actually makes a difference in the
        // profiler -- so just inline the max() here
        final int measuredWidth = child.getMeasuredWidth();
        final int minWidth = mMinimumWidth;
        final int maxWidth = (minWidth > measuredWidth) ? minWidth : measuredWidth;
        return (int) (maxWidth * mLayoutScale + 0.5f);
    }

    protected int getScaledMeasuredHeight(View child) {
        // This functions are called enough times that it actually makes a difference in the
        // profiler -- so just inline the max() here
        final int measuredWidth = child.getMeasuredHeight();
        final int minWidth = mMinimumWidth;
        final int maxWidth = (minWidth > measuredWidth) ? minWidth : measuredWidth;
        return (int) (maxWidth * mLayoutScale + 0.5f);
    }

    protected void getVisiblePages(int[] range) {
        final int pageCount = getChildCount();

        if (pageCount > 0) {
            final int screenWidth = getMeasuredWidth();
            int leftScreen = 0;
            int rightScreen = 0;
            View currPage = getPageAt(leftScreen);
            while (leftScreen < pageCount - 1 &&
                    currPage.getX() + currPage.getWidth() -
                    currPage.getPaddingRight() < getScrollX()) {
                leftScreen++;
                currPage = getPageAt(leftScreen);
            }
            rightScreen = leftScreen;
            currPage = getPageAt(rightScreen + 1);
            while (rightScreen < pageCount - 1 &&
                    currPage.getX() - currPage.getPaddingLeft() < getScrollX() + screenWidth) {
                rightScreen++;
                currPage = getPageAt(rightScreen + 1);
            }
            range[0] = leftScreen;
            range[1] = rightScreen;
        } else {
            range[0] = -1;
            range[1] = -1;
        }
    }

    protected boolean shouldDrawChild(View child) {
        return child.getAlpha() > 0;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int halfScreenSize = getMeasuredWidth() / 2;
        // mOverScrollX is equal to getScrollX() when we're within the normal scroll range.
        // Otherwise it is equal to the scaled overscroll position.
        int screenCenter = mOverScrollX + halfScreenSize;

        if (screenCenter != mLastScreenCenter || mForceScreenScrolled) {
            // set mForceScreenScrolled before calling screenScrolled so that screenScrolled can
            // set it for the next frame
            mForceScreenScrolled = false;
            screenScrolled(screenCenter);
            mLastScreenCenter = screenCenter;
        }

        // Find out which screens are visible; as an optimization we only call draw on them
        final int pageCount = getChildCount();
        if (pageCount > 0) {
            getVisiblePages(mTempVisiblePagesRange);
            final int leftScreen = mTempVisiblePagesRange[0];
            final int rightScreen = mTempVisiblePagesRange[1];
            if (leftScreen != -1 && rightScreen != -1) {
                final long drawingTime = getDrawingTime();
                // Clip to the bounds
                canvas.save();
                canvas.clipRect(getScrollX(), getScrollY(), getScrollX() + getRight() - getLeft(),
                        getScrollY() + getBottom() - getTop());

                for (int i = getChildCount() - 1; i >= 0; i--) {
                    final View v = getPageAt(i);
                    if (mForceDrawAllChildrenNextFrame ||
                               (leftScreen <= i && i <= rightScreen && shouldDrawChild(v))) {
                        drawChild(canvas, v, drawingTime);
                    }
                }
                mForceDrawAllChildrenNextFrame = false;
                canvas.restore();
            }
        }
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        int page = indexToPage(indexOfChild(child));
        if (page != mCurrentPage || !mScroller.isFinished()) {
            snapToPage(page);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        int focusablePage;
        if (mNextPage != INVALID_PAGE) {
            focusablePage = mNextPage;
        } else {
            focusablePage = mCurrentPage;
        }
        View v = getPageAt(focusablePage);
        if (v != null) {
            return v.requestFocus(direction, previouslyFocusedRect);
        }
        return false;
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        if (direction == View.FOCUS_LEFT) {
            if (getCurrentPage() > 0) {
                snapToPage(getCurrentPage() - 1);
                return true;
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (getCurrentPage() < getPageCount() - 1) {
                snapToPage(getCurrentPage() + 1);
                return true;
            }
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (mCurrentPage >= 0 && mCurrentPage < getPageCount()) {
            getPageAt(mCurrentPage).addFocusables(views, direction, focusableMode);
        }
        if (direction == View.FOCUS_LEFT) {
            if (mCurrentPage > 0) {
                getPageAt(mCurrentPage - 1).addFocusables(views, direction, focusableMode);
            }
        } else if (direction == View.FOCUS_RIGHT){
            if (mCurrentPage < getPageCount() - 1) {
                getPageAt(mCurrentPage + 1).addFocusables(views, direction, focusableMode);
            }
        }
    }

    /**
     * If one of our descendant views decides that it could be focused now, only
     * pass that along if it's on the current page.
     *
     * This happens when live folders requery, and if they're off page, they
     * end up calling requestFocus, which pulls it on page.
     */
    @Override
    public void focusableViewAvailable(View focused) {
        View current = getPageAt(mCurrentPage);
        View v = focused;
        while (true) {
            if (v == current) {
                super.focusableViewAvailable(focused);
                return;
            }
            if (v == this) {
                return;
            }
            ViewParent parent = v.getParent();
            if (parent instanceof View) {
                v = (View)v.getParent();
            } else {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            // We need to make sure to cancel our long press if
            // a scrollable widget takes over touch events
            final View currentPage = getPageAt(mCurrentPage);
            currentPage.cancelLongPress();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    /**
     * Return true if a tap at (x, y) should trigger a flip to the previous page.
     */
    protected boolean hitsPreviousPage(float x, float y) {
        return (x < getRelativeChildOffset(mCurrentPage) - mPageSpacing);
    }

    /**
     * Return true if a tap at (x, y) should trigger a flip to the next page.
     */
    protected boolean hitsNextPage(float x, float y) {
        return  (x > (getMeasuredWidth() - getRelativeChildOffset(mCurrentPage) + mPageSpacing));
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onTouchEvent will be called and we do the actual
         * scrolling there.
         */
        acquireVelocityTrackerAndAddMovement(ev);

        // Skip touch handling if there are no pages to swipe
        if (getChildCount() <= 0) return super.onInterceptTouchEvent(ev);

		if(mNeedToClean) return true;
        /*
         * Shortcut the most recurring case: the user is in the dragging
         * state and he is moving his finger.  We want to intercept this
         * motion.
         */
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) &&
                (mTouchState == TOUCH_STATE_SCROLLING)) {
            return true;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */
                if (mActivePointerId != INVALID_POINTER) {
                    determineScrollingStart(ev);
                    break;
                }
                // if mActivePointerId is INVALID_POINTER, then we must have missed an ACTION_DOWN
                // event. in that case, treat the first occurence of a move event as a ACTION_DOWN
                // i.e. fall through to the next case (don't break)
                // (We sometimes miss ACTION_DOWN events in Workspace because it ignores all events
                // while it's small- this was causing a crash before we checked for INVALID_POINTER)
            }

            case MotionEvent.ACTION_DOWN: {
            	
                final float x = ev.getX();
                final float y = ev.getY();
                // Remember location of down touch
                mDownMotionX = x;
                mDownMotionY = y;
                mLastMotionX = x;
                mLastMotionY = y;
                mLastMotionXRemainder = 0;
				mLastMotionYRemainder = 0;
                mTotalMotionX = 0;
				mTotalMotionY = 0;
                mActivePointerId = ev.getPointerId(0);
                mAllowLongPress = true;
                
                /*
                 * If being flinged and user touches the screen, initiate drag;
                 * otherwise don't.  mScroller.isFinished should be false when
                 * being flinged.
                 */
                final int xDist = Math.abs(mScroller.getFinalX() - mScroller.getCurrX());
                final boolean finishedScrolling = (mScroller.isFinished() || xDist < mTouchSlop);
                if (finishedScrolling) {
                    mTouchState = TOUCH_STATE_REST;
                    mScroller.abortAnimation();
                } else {
                    mTouchState = TOUCH_STATE_SCROLLING;
                }

                // check if this can be the beginning of a tap on the side of the pages
                // to scroll the current page
                /*if (mTouchState != TOUCH_STATE_PREV_PAGE && mTouchState != TOUCH_STATE_NEXT_PAGE) {
                    if (getChildCount() > 0) {
                        if (hitsPreviousPage(x, y)) {
                            mTouchState = TOUCH_STATE_PREV_PAGE;
                        } else if (hitsNextPage(x, y)) {
                            mTouchState = TOUCH_STATE_NEXT_PAGE;
                        }
                    }
                }*/
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                mAllowLongPress = false;
                mActivePointerId = INVALID_POINTER;
                releaseVelocityTracker();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                releaseVelocityTracker();
                break;
        }

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return mTouchState != TOUCH_STATE_REST;
    }

    protected void determineScrollingStart(MotionEvent ev) {
        determineScrollingStart(ev, 1.0f);
    }

    /*
     * Determines if we should change the touch state to start scrolling after the
     * user moves their touch point too far.
     */
    protected void determineScrollingStart(MotionEvent ev, float touchSlopScale) {
        /*
         * Locally do absolute value. mLastMotionX is set to the y value
         * of the down event.
         */
        final int pointerIndex = ev.findPointerIndex(mActivePointerId);
        if (pointerIndex == -1) {
            return;
        }
        final float x = ev.getX(pointerIndex);
        final float y = ev.getY(pointerIndex);
        final int xDiff = (int) Math.abs(x - mLastMotionX);
        final int yDiff = (int) Math.abs(y - mLastMotionY);

        final int touchSlop = Math.round(touchSlopScale * mTouchSlop);
        boolean xPaged = xDiff > mPagingTouchSlop;
		boolean yPaged = yDiff > mPagingTouchSlop; 
        boolean xMoved = xDiff > touchSlop;
        boolean yMoved = yDiff > touchSlop;
		
		if(isOrientationPortrait()){
			if(yMoved && mCurrentPage != 0 && yDiff > xDiff){//systemui
	        	AuroraPage layout = (AuroraPage) getPageAt(mCurrentPage);
	        	mMovingChildView = layout.getChildOnPageAtPoint(x,y);
	        	if(mMovingChildView != null){
	        		mTouchState = TOUCH_STATE_CLEAR_LOCK;
	        		cancelCurrentPageLongPress();
	        		return;
	        	}
	        }
	        if (xMoved || xPaged || yMoved) {
	            if (mUsePagingTouchSlop ? xPaged : xMoved) {
	                // Scroll if the user moved far enough along the X axis
	                mTouchState = TOUCH_STATE_SCROLLING;
	                mTotalMotionX += Math.abs(mLastMotionX - x);
	                mLastMotionX = x;
	                mLastMotionXRemainder = 0;
	                mTouchX = getScrollX();
	                mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
	                pageBeginMoving();
	            }
	            // Either way, cancel any pending longpress
	            cancelCurrentPageLongPress();
	        }
		} else {		
		        //lock or remove for Direct X 
	        if(xMoved && mCurrentPage != 0 && xDiff > yDiff){//systemui
	        	AuroraPage layout = (AuroraPage) getPageAt(mCurrentPage);
	        	mMovingChildView = layout.getChildOnPageAtPoint(x,y);
	        	if(mMovingChildView != null){
	        		mTouchState = TOUCH_STATE_CLEAR_LOCK;
	        		cancelCurrentPageLongPress();
	        		return;
	        	}
	        }
			//这块代码主要修改了 应用上锁/删除 的触发操作  end

			//这块代码主要修改了 应用翻页 的触发操作  start
	        if (xMoved || yPaged || yMoved) {
	            if (mUsePagingTouchSlop ? yPaged : yMoved) {
	                // Scroll if the user moved far enough along the X axis
	                mTouchState = TOUCH_STATE_SCROLLING;
	                mTotalMotionX += Math.abs(mLastMotionX - x);
					mTotalMotionY += Math.abs(mLastMotionY - y);
	                mLastMotionX = x;
	                mLastMotionY = y;
	                mLastMotionXRemainder = 0;
					mLastMotionYRemainder = 0;
	                mTouchX = getScrollX();
					mTouchY = getScrollY();
	                mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
	                pageBeginMoving();
	            }
	            // Either way, cancel any pending longpress
	            cancelCurrentPageLongPress();
			}
			//这块代码主要修改了 应用翻页 的触发操作  end
		}


    }

    protected void cancelCurrentPageLongPress() {
        if (mAllowLongPress) {
            mAllowLongPress = false;
            // Try canceling the long press. It could also have been scheduled
            // by a distant descendant, so use the mAllowLongPress flag to block
            // everything
            final View currentPage = getPageAt(mCurrentPage);
            if (currentPage != null) {
                currentPage.cancelLongPress();
            }
        }
    }

    protected float getScrollProgress(int screenCenter, View v, int page) {
        final int halfScreenSize = getMeasuredWidth() / 2;

        int totalDistance = getScaledMeasuredWidth(v) + mPageSpacing;
        int delta = screenCenter - (getChildOffset(page) -
                getRelativeChildOffset(page) + halfScreenSize);

        float scrollProgress = delta / (totalDistance * 1.0f);
        scrollProgress = Math.min(scrollProgress, 1.0f);
        scrollProgress = Math.max(scrollProgress, -1.0f);
        return scrollProgress;
    }

    // This curve determines how the effect of scrolling over the limits of the page dimishes
    // as the user pulls further and further from the bounds
    private float overScrollInfluenceCurve(float f) {
        f -= 1.0f;
        return f * f * f + 1.0f;
    }

    protected void acceleratedOverScroll(float amount) {
        int screenSize = getMeasuredWidth();

        // We want to reach the max over scroll effect when the user has
        // over scrolled half the size of the screen
        float f = OVERSCROLL_ACCELERATE_FACTOR * (amount / screenSize);

        if (f == 0) return;

        // Clamp this factor, f, to -1 < f < 1
        if (Math.abs(f) >= 1) {
            f /= Math.abs(f);
        }

        int overScrollAmount = (int) Math.round(f * screenSize);
        if (amount < 0) {
            mOverScrollX = overScrollAmount;
            super.scrollTo(0, getScrollY());
        } else {
            mOverScrollX = mMaxScrollX + overScrollAmount;
            super.scrollTo(mMaxScrollX, getScrollY());
        }
        invalidate();
    }

    protected void dampedOverScroll(float amount) {
		if(isOrientationPortrait()){
			dampedOverScrollPor(amount);
		}else{
			dampedOverScrollLand(amount);	
		}
	}
	protected void dampedOverScrollLand(float amount) {
        int screenSize = landStartY;
        float f = (amount / screenSize);
        if (f == 0) return;
        f = f / (Math.abs(f)) * (overScrollInfluenceCurve(Math.abs(f)));

        // Clamp this factor, f, to -1 < f < 1
        if (Math.abs(f) >= 1) {
            f /= Math.abs(f);
        }
        if (amount < 0) {
        	int overScrollAmount = (int) Math.round(OVERSCROLL_DAMP_FACTOR * f * screenSize);
            //mOverScrollX = -mMinScrollX + overScrollAmount;
			mOverScrollX = -mMaxScrollX + overScrollAmount;
        } else {
			int overScrollAmount = (int) Math.round(0.3 * f * screenSize);
            //mOverScrollX = -mMaxScrollX + overScrollAmount;
			mOverScrollX = -mMinScrollX + overScrollAmount;
			if(mOverScrollX > 0) mOverScrollX = 0;
            //super.scrollTo(mMaxScrollX, getScrollY());
        }
		//super.scrollTo(mOverScrollX, getScrollY());
		super.scrollTo(getScrollX(), mOverScrollX);
        invalidate();
    }

    protected void dampedOverScrollPor(float amount) {
        int screenSize = getMeasuredWidth();
        float f = (amount / screenSize);
        if (f == 0) return;
        f = f / (Math.abs(f)) * (overScrollInfluenceCurve(Math.abs(f)));

        // Clamp this factor, f, to -1 < f < 1
        if (Math.abs(f) >= 1) {
            f /= Math.abs(f);
        }

        
        if (amount < 0) {
			int overScrollAmount = (int) Math.round(0.3 * f * screenSize);
            mOverScrollX = mMinScrollX + overScrollAmount;
			if(mOverScrollX < 0) mOverScrollX = 0;
            //super.scrollTo(0, getScrollY());
        } else {
        	int overScrollAmount = (int) Math.round(OVERSCROLL_DAMP_FACTOR * f * screenSize);
            mOverScrollX = mMaxScrollX + overScrollAmount;
            //super.scrollTo(mMaxScrollX, getScrollY());
        }
		super.scrollTo(mOverScrollX, getScrollY());
        invalidate();
    }

    protected void overScroll(float amount) {
        dampedOverScroll(amount);
    }

    protected float maxOverScroll() {
        // Using the formula in overScroll, assuming that f = 1.0 (which it should generally not
        // exceed). Used to find out how much extra wallpaper we need for the over scroll effect
        float f = 1.0f;
        f = f / (Math.abs(f)) * (overScrollInfluenceCurve(Math.abs(f)));
        return OVERSCROLL_DAMP_FACTOR * f;
    }

    //systemui
    private ObjectAnimator createTranslationAnimation(View v, float x,float y) {

        ObjectAnimator anim = ObjectAnimator.ofFloat(v, isOrientationPortrait() ? "translationY" : "translationX", isOrientationPortrait() ? y : x);
        return anim;
		/*
    	PropertyValuesHolder xMove = PropertyValuesHolder.ofFloat("translationX",x);
    	PropertyValuesHolder yMove = PropertyValuesHolder.ofFloat("translationY",y);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(v, xMove,yMove);
        return anim;
        */
    }
    //Aurora <tongyh> <2013-12-13> add Alpha animated slide up and down begin
//	public void snapChild(final View animView, float x,float y) {
    public void snapChild(final View animView, float x, float y, boolean flag) {
        ObjectAnimator anim = createTranslationAnimation(animView, x, y);
        int duration = 200;
        anim.setDuration(duration);
        anim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidateGlobalRegion(animView);
            }
        });
        if(flag){
        	anim.start();
        	
        }else{
        	ObjectAnimator alphaAnim = createAlphaAnimation(animView);
            alphaAnim.setDuration(duration);
            AnimatorSet animSet = new AnimatorSet();
            animSet.play(anim).with(alphaAnim);
            animSet.start();
        }
//        anim.start();
		//Aurora <tongyh> <2013-12-13> add Alpha animated slide up and down end
    }
    



	// invalidate the view's own bounds all the way up the view hierarchy
    public static void invalidateGlobalRegion(View view) {
        invalidateGlobalRegion(
            view,
            new RectF(view.getLeft(), view.getTop(), view.getRight(), view.getBottom()));
    }

    // invalidate a rectangle relative to the view's coordinate system all the way up the view
    // hierarchy
    public static void invalidateGlobalRegion(View view, RectF childBounds) {
        //childBounds.offset(view.getTranslationX(), view.getTranslationY());

        while (view.getParent() != null && view.getParent() instanceof View) {
            view = (View) view.getParent();
            view.getMatrix().mapRect(childBounds);
            view.invalidate((int) Math.floor(childBounds.left),
                            (int) Math.floor(childBounds.top),
                            (int) Math.ceil(childBounds.right),
                            (int) Math.ceil(childBounds.bottom));

        }
    }
    
    
    protected void  moveChildView(View v,float deltaX,float deltaY){
    	//v.setTranslationX(deltaX);
        // Aurora <Felix.Duan> <2014-9-18> <BEGIN> Add animation of swipe up icon on recents panel
        // Limits move up delta
		if(isOrientationPortrait()){
			v.setTranslationY(deltaY < (-RECENT_PAGE_PADDING) ? (-RECENT_PAGE_PADDING) : deltaY);
	        // Aurora <Felix.Duan> <2014-9-18> <END> Add animation of swipe up icon on recents panel
	    	//Aurora <tongyh> <2013-12-13> add Alpha animated slide up and down begin
	    	if(deltaY < 0){
	    		float dy = Math.abs(deltaY)/255;
	    		float mAlpha = Math.max(0.3f, (0.8f - dy));
	    		v.setAlpha(mAlpha);
	    	}
		} else {
			v.setTranslationX(deltaX < (-RECENT_PAGE_PADDING) ? (-RECENT_PAGE_PADDING) : deltaX);
	    	if(deltaX < 0){
	    		float dy = Math.abs(deltaX)/255;
	    		float mAlpha = Math.max(0.3f, (0.8f - dy));
	    		v.setAlpha(mAlpha);
	    	}
		}
    	//Aurora <tongyh> <2013-12-13> add Alpha animated slide up and down end
        invalidateGlobalRegion(v);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Skip touch handling if there are no pages to swipe
        if (getChildCount() <= 0) return super.onTouchEvent(ev);

        // Aurora <Felix.Duan> <2014-4-28> <BEGIN> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw
        // Animation not finished, not allow scroll
        if (!mScrollable) {
            if (BaseStatusBar.FELIXDBG) Log.d("felix", "AuroraPagedView onTouchEvent() mScrollable = false");
            return true;
        }
        // Aurora <Felix.Duan> <2014-4-28> <END> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw

		if(mNeedToClean) return true;

        // Aurora <Felix.Duan> <2014-9-18> <BEGIN> Add animation of swipe up icon on recents panel
        // Don`t handle events at invisible upper part
        if (isOrientationPortrait() && ev.getAction() == MotionEvent.ACTION_DOWN
            && ev.getY() < (RECENT_PAGE_PADDING))
                return super.onTouchEvent(ev);
        else if (!isOrientationPortrait() && ev.getAction() == MotionEvent.ACTION_DOWN
            && ev.getX() < (RECENT_PAGE_PADDING))
                return super.onTouchEvent(ev);
				
        // Aurora <Felix.Duan> <2014-9-18> <END> Add animation of swipe up icon on recents panel
        acquireVelocityTrackerAndAddMovement(ev);

        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            /*
             * If being flinged and user touches, stop the fling. isFinished
             * will be false if being flinged.
             */
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }

            // Remember where the motion event started
            mDownMotionX = mLastMotionX = ev.getX();
            
            mDownMotionY = mLastMotionY = ev.getY();
            
            mLastMotionXRemainder = 0;
			mLastMotionYRemainder = 0;
            mTotalMotionX = 0;
			mTotalMotionY = 0;
            mActivePointerId = ev.getPointerId(0);
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                pageBeginMoving();
            }
            
            break;

        case MotionEvent.ACTION_MOVE:
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                // Scroll to follow the motion event
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float deltaX = mLastMotionX + mLastMotionXRemainder - x;
                mTotalMotionX += Math.abs(deltaX);
		final float y = ev.getY(pointerIndex);
                final float deltaY = mLastMotionY + mLastMotionYRemainder - y;
                mTotalMotionY += Math.abs(deltaY);
				float tempX = deltaX;
				float tempY = deltaY;
				final boolean isOriLand = !isOrientationPortrait();
                
                // Only scroll and update mLastMotionX if we have moved some discrete amount.  We
                // keep the remainder because we are actually testing if we've moved from the last
                // scrolled position (which is discrete).
                if (Math.abs(!isOriLand ? deltaX : deltaY) >= 1.0f) {
                    mTouchX += deltaX;
					mTouchY += deltaY;
                    mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                    if (!mDeferScrollUpdate) {
						if(!isOriLand){
							tempX = deltaX;
							tempY = 0;
						} else {
							tempX = 0;
							tempY = deltaY;
						}
					
                        scrollBy((int) tempX, (int) tempY);
                        //Aurora <tongyh> <2013-12-13> add recents rubbish animation begin
                        auroraInitRubbishAnim();
                      //Aurora <tongyh> <2013-12-13> add recents rubbish animation end
//                        recentsUpdateRubbish(mOverScrollX);

                        // Aurora <Felix.Duan> <2014-3-28> <BEGIN> Optimize remove task animation v2
                        // Move to scrollTo()
                        // recentsRubbish.rotate(mOverScrollX);
                        // Aurora <Felix.Duan> <2014-3-28> <END> Optimize remove task animation v2
                        if (DEBUG) Log.d(TAG, "onTouchEvent().Scrolling: " + deltaX);
                    } else {
                        invalidate();
                    }
                    mLastMotionX = x;
                    mLastMotionY = y;
                    mLastMotionXRemainder = deltaX - (int) deltaX;
					mLastMotionYRemainder = deltaY - (int) deltaY;
                } else {
                    awakenScrollBars();
                }
            } else if (mTouchState == TOUCH_STATE_CLEAR_LOCK) {//systemui
            	 final int pointerIndex = ev.findPointerIndex(mActivePointerId);
            	 final float x = ev.getX(pointerIndex);
            	 final float y = ev.getY(pointerIndex);
            	 final float deltaX = x - mDownMotionX;
            	 final float deltaY = y - mDownMotionY;
            	 moveChildView(mMovingChildView,deltaX,deltaY);

            }else {
                determineScrollingStart(ev);
            }
            break;

        case MotionEvent.ACTION_UP:
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                final int activePointerId = mActivePointerId;
                final int pointerIndex = ev.findPointerIndex(activePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityX = (int) velocityTracker.getXVelocity(activePointerId);
               int velocityY = (int) velocityTracker.getYVelocity(activePointerId);

                final int deltaX = (int) (x - mDownMotionX);
                final int deltaY = (int) (y - mDownMotionY);
                final int pageWidth = getScaledMeasuredWidth(getPageAt(mCurrentPage));
				final int pageHeight = getScaledMeasuredHeight(getPageAt(mCurrentPage));
                boolean isSignificantMove = Math.abs(deltaX) > pageWidth *
                        SIGNIFICANT_MOVE_THRESHOLD;

                mTotalMotionX += Math.abs(mLastMotionX + mLastMotionXRemainder - x);
				mTotalMotionY += Math.abs(mLastMotionY + mLastMotionYRemainder - y);

                boolean isFling = isOrientationPortrait() && mTotalMotionX > MIN_LENGTH_FOR_FLING &&
                        Math.abs(velocityX) > mFlingThresholdVelocity;						
				
			   	int delta = deltaX;
				int velocity = velocityX;
				int pageDistance = pageWidth;
				if(!isOrientationPortrait()){
			   		isFling = mTotalMotionY > MIN_LENGTH_FOR_FLING &&
                        Math.abs(velocityY) > mFlingThresholdVelocity;
					delta = deltaY;
					velocity = velocityY;
					pageDistance = pageHeight;
					isSignificantMove = Math.abs(deltaY) > pageHeight *
                        SIGNIFICANT_MOVE_THRESHOLD;
				}
                // In the case that the page is moved far to one direction and then is flung
                // in the opposite direction, we use a threshold to determine whether we should
                // just return to the starting page, or if we should skip one further.
                boolean returnToOriginalPage = false;
                if (Math.abs(delta) > pageDistance * RETURN_TO_ORIGINAL_PAGE_THRESHOLD &&
                        Math.signum(velocity) != Math.signum(delta) && isFling) {
                    returnToOriginalPage = true;
                }

				if(isOrientationPortrait()){
					if(mOverScrollX < mMinScrollX){
						returnToOriginalPage = true;
						if(mOverScrollX <= 0){
							mNeedToClean = true;
						}
					}
				} else {
					if(mOverScrollX > -mMinScrollX){
						returnToOriginalPage = true;
						if(mOverScrollX >= 0){
							mNeedToClean = true;
						}
					}
				}
				
                int finalPage;
				int pageNextOrPre = 1; // 1 means next page, -1 means pre page for land. por Opposite.
				if(!isOrientationPortrait()){
					pageNextOrPre = -1;
				}
                // We give flings precedence over large moves, which is why we short-circuit our
                // test for a large move if a fling has been registered. That is, a large
                // move to the left and fling to the right will register as a fling to the right.
// Aurora <Felix.Duan> <2014-4-16> <BEGIN> Fix BUG #4244. Orphan icon view left seen due to two finger interact logic flaw
                if (mNeedToClean || ((isSignificantMove && delta > 0 && !isFling) ||
                        (isFling && velocity > 0)) && (isOrientationPortrait() ? (mCurrentPage > 0) : (mCurrentPage < getChildCount() - 1))) {
// Aurora <Felix.Duan> <2014-4-16> <END> Fix BUG #4244. Orphan icon view left seen due to two finger interact logic flaw
                    finalPage = returnToOriginalPage ? mCurrentPage : mCurrentPage - pageNextOrPre;
                    if(mNeedToClean){
						// Aurora <Steve.Tang> 2015-03-02, OneKey Clear Count.start
						CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_010, 1);
						// Aurora <Steve.Tang> 2015-03-02, OneKey Clear Count.end
            			removeUnlockedViews();
            		}else{
            			if(finalPage == 0 && getChildCount() > 1){
            				finalPage = mCurrentPage;
            			}
            			snapToPageWithVelocity(finalPage, velocity);
            		}
                } else if (((isSignificantMove && delta < 0 && !isFling) ||
                        (isFling && velocity < 0)) && (!isOrientationPortrait() ? (mCurrentPage > 0) : (mCurrentPage < getChildCount() - 1))) {
                    finalPage = returnToOriginalPage ? mCurrentPage : mCurrentPage + pageNextOrPre;
                    snapToPageWithVelocity(finalPage, velocity);
                } else {
                    snapToDestination();
                }
            } else if (mTouchState == TOUCH_STATE_CLEAR_LOCK) {//systemui
                final int activePointerId = mActivePointerId;
                final int pointerIndex = ev.findPointerIndex(activePointerId);
            	final float y = ev.getY(pointerIndex);
            	final float deltaY = y - mDownMotionY;
            	final float x = ev.getX(pointerIndex);
            	final float deltaX = x - mDownMotionX;
				
				float delta = deltaY;
				
				if(!isOrientationPortrait()){
					delta = deltaX;
				}

            	if(delta > CLEAR_LOCK_OFFSET_Y){
            		//lock
            		boolean locked = ((AuroraRecentlItemView)mMovingChildView).setLocked();
//            		snapChild(mMovingChildView,0,0);
					// Aurora <Steve.Tang> 2015-03-02. Lock&UnLock App Count.start
					if(locked){ //doing lock
						CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_007, 1);
					} else { //doing unlock
						CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_008, 1);
					}
						
					// Aurora <Steve.Tang> 2015-03-02. Lock&UnLock App Count.end
                    // Aurora <Felix.Duan> <2014-10-9> <BEGIN> Fix BUG #8954. Reset to full opacity.
            		snapChild(mMovingChildView, 0, 0, false);
                    // Aurora <Felix.Duan> <2014-10-9> <END> Fix BUG #8954. Reset to full opacity.
            	}else if(delta < 0 && -delta > CLEAR_LOCK_OFFSET_Y){
            		//clear
            		ViewGroup cur_page = (ViewGroup)getPageAt(mCurrentPage);

					releaseVelocityTracker();
					
                    // Aurora <Felix.Duan> <2014-9-27> <BEGIN> Fix BUG #8720 & BUG #8104. NPE when remove item on null page.
                    if (cur_page != null) {
            		    cur_page.removeView(mMovingChildView);
						// Aurora <Steve.Tang> 2015-03-02. Remove Apps Count.start
						CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_009, 1);
						// Aurora <Steve.Tang> 2015-03-02. Remove Apps Count.end
					    int index = mCurrentPage + 1;
            		    ViewGroup next_page = (ViewGroup)getPageAt(index);
					    ViewGroup perv_page = cur_page;
            		    while(null != next_page){
            		    	View moveView =  next_page.getChildAt(0);
            		    	((AuroraPageNormal)next_page).tempRemoveView(moveView);
            		    	perv_page.addView(moveView);
            		    	if(0 == next_page.getChildCount()){
            		    		this.removeView(next_page);
					    		break;
            		    	}
					    	perv_page = next_page;
					    	next_page = (ViewGroup)getPageAt(++index);
            		    }
        			    if(0 == cur_page.getChildCount()){
        			    	this.removeView(cur_page);
        			    	if(getChildCount() == 1){
        			    		mNeedToClean = true;
        			    		return true;
        			    	}else{
        			    		snapToDestination();
        			    	}
        			    }
                    }
                    // Aurora <Felix.Duan> <2014-9-27> <END> Fix BUG #8720 & BUG #8104. NPE when remove item on null page.
            		//layout.requestLayout();
            	}else{
//            		snapChild(mMovingChildView,0,0);
            		snapChild(mMovingChildView, 0, 0, false);
            	}
            }
            else if (mTouchState == TOUCH_STATE_PREV_PAGE) {
                // at this point we have not moved beyond the touch slop
                // (otherwise mTouchState would be TOUCH_STATE_SCROLLING), so
                // we can just page
                int nextPage = Math.max(0, mCurrentPage - 1);
				if(0 == nextPage){
					snapToDestination();
				}else if (nextPage != mCurrentPage) {
                    snapToPage(nextPage);
                } else {
                    snapToDestination();
                }
            } else if (mTouchState == TOUCH_STATE_NEXT_PAGE) {
                // at this point we have not moved beyond the touch slop
                // (otherwise mTouchState would be TOUCH_STATE_SCROLLING), so
                // we can just page
                int nextPage = Math.min(getChildCount() - 1, mCurrentPage + 1);
                if(0 == nextPage){
					snapToDestination();
				}else if (nextPage != mCurrentPage) {
                    snapToPage(nextPage);
                } else {
                    snapToDestination();
                }
            } else {
                onUnhandledTap(ev);
            }
            mTouchState = TOUCH_STATE_REST;
            mActivePointerId = INVALID_POINTER;
            releaseVelocityTracker();
            break;

        case MotionEvent.ACTION_CANCEL:
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                snapToDestination();
            }
			// Aurora <Steve.Tang> 2014-11-07 fix 7902, reset childview state as action_cancel. start
			if(mTouchState == TOUCH_STATE_CLEAR_LOCK && mMovingChildView != null){
				snapChild(mMovingChildView, 0, 0, false);
			}
			// Aurora <Steve.Tang> 2014-11-07 fix 7902, reset childview state as action_cancel. end
            mTouchState = TOUCH_STATE_REST;
            mActivePointerId = INVALID_POINTER;
            releaseVelocityTracker();
            break;

        case MotionEvent.ACTION_POINTER_UP:
            onSecondaryPointerUp(ev);
            break;
        }

        return true;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_SCROLL: {
                    // Handle mouse (or ext. device) by shifting the page depending on the scroll
                    final float vscroll;
                    final float hscroll;
                    if ((event.getMetaState() & KeyEvent.META_SHIFT_ON) != 0) {
                        vscroll = 0;
                        hscroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                    } else {
                        vscroll = -event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                        hscroll = event.getAxisValue(MotionEvent.AXIS_HSCROLL);
                    }
                    if (hscroll != 0 || vscroll != 0) {
                        if (hscroll > 0 || vscroll > 0) {
                            scrollRight();
                        } else {
                            scrollLeft();
                        }
                        return true;
                    }
                }
            }
        }
        return super.onGenericMotionEvent(event);
    }

    private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = mDownMotionX = ev.getX(newPointerIndex);
            mLastMotionY = mDownMotionY = ev.getY(newPointerIndex);
            mLastMotionXRemainder = 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    protected void onUnhandledTap(MotionEvent ev) {
		snapToDestination();
	}

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        int page = indexToPage(indexOfChild(child));
        if (page >= 0 && page != getCurrentPage() && !isInTouchMode()) {
            snapToPage(page);
        }
    }

    protected int getChildIndexForRelativeOffset(int relativeOffset) {
        final int childCount = getChildCount();
        int left;
        int right;
        for (int i = 0; i < childCount; ++i) {
            left = getRelativeChildOffset(i);
            right = (left + getScaledMeasuredWidth(getPageAt(i)));
            if (left <= relativeOffset && relativeOffset <= right) {
                return i;
            }
        }
        return -1;
    }

    protected int getChildWidth(int index) {
        // This functions are called enough times that it actually makes a difference in the
        // profiler -- so just inline the max() here
        final int measuredWidth = getPageAt(index).getMeasuredWidth();
        final int minWidth = mMinimumWidth;
        return (minWidth > measuredWidth) ? minWidth : measuredWidth;
    }

    protected int getChildHeight(int index) {
        // This functions are called enough times that it actually makes a difference in the
        // profiler -- so just inline the max() here
        final int measuredHeight = getPageAt(index).getMeasuredHeight();
        final int minHeight = mMinimumWidth;
        return (minHeight > measuredHeight) ? minHeight : measuredHeight;
    }


    int getPageNearestToCenterOfScreen() {
		boolean isOriLand = !isOrientationPortrait();
        int minDistanceFromScreenCenter = Integer.MAX_VALUE;
        int minDistanceFromScreenCenterIndex = -1;
        int screenCenter = !isOriLand ? (getScrollX() + (getMeasuredWidth() / 2)) : (Math.abs(getScrollY()) + (getMeasuredHeight() / 2));
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View layout = (View) getPageAt(i);
            int childWidth = getScaledMeasuredWidth(layout);
			int childHeight = getScaledMeasuredHeight(layout);
            int halfChildWidth = (childWidth / 2);
			int halfChildHeight = (childHeight / 2);
			
            int childCenter = getChildOffset(i) + (!isOriLand ? halfChildWidth : halfChildHeight);
            int distanceFromScreenCenter = Math.abs(childCenter - screenCenter);
            if (distanceFromScreenCenter < minDistanceFromScreenCenter) {
                minDistanceFromScreenCenter = distanceFromScreenCenter;
                minDistanceFromScreenCenterIndex = i;
            }
        }
		if(0 == minDistanceFromScreenCenterIndex){
			minDistanceFromScreenCenterIndex = 1;
		}
        return minDistanceFromScreenCenterIndex;
    }

    protected void snapToDestination() {
        snapToPage(getPageNearestToCenterOfScreen(), PAGE_SNAP_ANIMATION_DURATION);
    }

    private static class ScrollInterpolator implements Interpolator {
        public ScrollInterpolator() {
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return t*t*t*t*t + 1;
        }
    }

    // We want the duration of the page snap animation to be influenced by the distance that
    // the screen has to travel, however, we don't want this duration to be effected in a
    // purely linear fashion. Instead, we use this method to moderate the effect that the distance
    // of travel has on the overall snap duration.
    float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    protected void snapToPageWithVelocity(int whichPage, int velocity) {
        whichPage = Math.max(0, Math.min(whichPage, getChildCount() - 1));
		boolean isOriLand = !isOrientationPortrait(); 
        int halfScreenSize = (!isOriLand ? getMeasuredWidth() : getMeasuredHeight())/ 2;

        if (DEBUG) Log.d(TAG, "snapToPage.getChildOffset(): " + getChildOffset(whichPage));
        if (DEBUG) Log.d(TAG, "snapToPageWithVelocity.getRelativeChildOffset(): "
                + getMeasuredWidth() + ", " + getChildWidth(whichPage));
        final int newX = getChildOffset(whichPage) - getRelativeChildOffset(whichPage);
        int delta = 0;
		if(!isOriLand){
			delta = newX - mUnboundedScrollX;
		} else {
			delta = -(newX + mUnboundedScrollY);
		}
        int duration = 0;

        if (Math.abs(velocity) < mMinFlingVelocity) {
            // If the velocity is low enough, then treat this more as an automatic page advance
            // as opposed to an apparent physical response to flinging
            snapToPage(whichPage, PAGE_SNAP_ANIMATION_DURATION);
            return;
        }

        // Here we compute a "distance" that will be used in the computation of the overall
        // snap duration. This is a function of the actual distance that needs to be traveled;
        // we keep this value close to half screen size in order to reduce the variance in snap
        // duration as a function of the distance the page needs to travel.
        float distanceRatio = Math.min(1f, 1.0f * Math.abs(delta) / (2 * halfScreenSize));
        float distance = halfScreenSize + halfScreenSize *
                distanceInfluenceForSnapDuration(distanceRatio);

        velocity = Math.abs(velocity);
        velocity = Math.max(mMinSnapVelocity, velocity);

        // we want the page's snap velocity to approximately match the velocity at which the
        // user flings, so we scale the duration by a value near to the derivative of the scroll
        // interpolator at zero, ie. 5. We use 4 to make it a little slower.
        duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        duration = Math.min(duration, MAX_PAGE_SNAP_DURATION);

        snapToPage(whichPage, delta, duration);
    }

    protected void snapToPage(int whichPage) {
        snapToPage(whichPage, PAGE_SNAP_ANIMATION_DURATION);
    }

    protected void snapToPage(int whichPage, int duration) {
        whichPage = Math.max(0, Math.min(whichPage, getPageCount() - 1));

        int newX = getChildOffset(whichPage) - getRelativeChildOffset(whichPage);
		
		boolean isOriLand = !isOrientationPortrait(); 

        final int sX = !isOriLand ? mUnboundedScrollX : mUnboundedScrollY;
        final int delta = !isOriLand ? (newX - sX) : (-(sX + newX));
		
        snapToPage(whichPage, delta, duration);
    }

    protected void snapToPage(int whichPage, int delta, int duration) {
        mNextPage = whichPage;

        View focusedChild = getFocusedChild();
        if (focusedChild != null && whichPage != mCurrentPage &&
                focusedChild == getPageAt(mCurrentPage)) {
            focusedChild.clearFocus();
        }

        pageBeginMoving();
        awakenScrollBars(duration);
        if (duration == 0) {
            duration = Math.abs(delta);
        }

        if (!mScroller.isFinished()) mScroller.abortAnimation();
		if(isOrientationPortrait()){
        	mScroller.startScroll(mUnboundedScrollX, 0, delta, 0, duration);		
		} else {
			mScroller.startScroll(0, mUnboundedScrollY, 0, delta, duration);
		}

        // Load associated pages immediately if someone else is handling the scroll, otherwise defer
        // loading associated pages until the scroll settles
        if (mDeferScrollUpdate) {
            loadAssociatedPages(mNextPage);
        } else {
            mDeferLoadAssociatedPagesUntilScrollCompletes = true;
        }
        notifyPageSwitchListener();
        invalidate();
    }

    public void scrollLeft() {
        if (mScroller.isFinished()) {
            if (mCurrentPage > 0) snapToPage(mCurrentPage - 1);
        } else {
            if (mNextPage > 0) snapToPage(mNextPage - 1);
        }
    }

    public void scrollRight() {
        if (mScroller.isFinished()) {
            if (mCurrentPage < getChildCount() -1) snapToPage(mCurrentPage + 1);
        } else {
            if (mNextPage < getChildCount() -1) snapToPage(mNextPage + 1);
        }
    }

    public int getPageForView(View v) {
        int result = -1;
        if (v != null) {
            ViewParent vp = v.getParent();
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                if (vp == getPageAt(i)) {
                    return i;
                }
            }
        }
        return result;
    }

    /**
     * @return True is long presses are still allowed for the current touch
     */
    public boolean allowLongPress() {
        return mAllowLongPress;
    }

    /**
     * Set true to allow long-press events to be triggered, usually checked by
     * {@link Launcher} to accept or block dpad-initiated long-presses.
     */
    public void setAllowLongPress(boolean allowLongPress) {
        mAllowLongPress = allowLongPress;
    }

    public static class SavedState extends BaseSavedState {
        int currentPage = -1;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(currentPage);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
    //systemui

    // Aurora <Felix.Duan> <2014-3-28> <BEGIN> Optimize remove task animation v2
    /**
     * Build trash animation of a icon view.
     * @param index of @mIconViews
     * @param totalDuration of all animation
     * @return AnimationSet of this view
     * 
     * @author Felix.Duan
     * @date 2014-3-28
     */
    private AnimatorSet buildTrashAnimationSet(final int index,
            final int totalDuration) {
        AuroraRecentlItemView view = mIconViews.get(index);
        if (view.isLocked())
            return buildLockedTrashAnimationSet(view, index);
        else
            return buildUnlockedTrashAnimationSet(view, index, totalDuration);
    }

    /**
     * Build trash animation of a locked icon view.
     * Including:
     *      1. darken animation
     *      2. hold position animation.
     * @param view to animate
     * @param index of this view
     * @return AnimationSet of this view
     * 
     * @author Felix.Duan
     * @date 2014-3-28
     */
    private AnimatorSet buildLockedTrashAnimationSet(
            AuroraRecentlItemView view, final int index) {
  		updateOffsetValues();
		if(isOrientationPortrait()){
			return buildLockedTrashAnimationSetPortrait(view, index);
		}else{
			return buildLockedTrashAnimationSetLand(view, index);
		}

    }

    private AnimatorSet buildLockedTrashAnimationSetLand(
            AuroraRecentlItemView view, final int index) {
        Log.d("felix", "AuroraPagedView.DEBUG buildLockedTrashAnimationSet() " + index + "  " + view.mName);
        int offset = calLockTrashAnimationStartOffsetY(index);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(
                createTranslationYAnimation(view, offset, offset,
                        TRASH_ANIMATION_LOCKED_DURATION),
                createLockViewAlphaAnimation(view, true));

        return animSet;
    }

    private AnimatorSet buildLockedTrashAnimationSetPortrait(
            AuroraRecentlItemView view, final int index) {
        Log.d("felix", "AuroraPagedView.DEBUG buildLockedTrashAnimationSet() " + index + "  " + view.mName);
        int offset = calLockTrashAnimationStartOffsetX(index);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(
                createTranslationXAnimation(view, offset, offset,
                        TRASH_ANIMATION_LOCKED_DURATION),
                createLockViewAlphaAnimation(view, true));

        return animSet;
    }

    /**
     * Build trash animation of a unlocked icon view
     * Including:
     *      1. Move from current position to rubbish bin
     *      2. Scale from 100% size to 10%
     *      3. Alpha change from opaque to transparent
     * Views starts off screen also includes:
     *      1. Hide view until show time
     *      2. Enter stage animation from right side
     * 
     * @param view to animate
     * @param index of this view
     * @param totalDuration of all animation
     * @return AnimationSet of this view
     * 
     * @author Felix.Duan
     * @date 2014-3-28
     */
    private AnimatorSet buildUnlockedTrashAnimationSet(
            final AuroraRecentlItemView view, final int index,
            final int totalDuration){
		updateOffsetValues();
		if(isOrientationPortrait()){
			return buildUnlockedTrashAnimationSetPortrait(view, index, totalDuration);
		}else{
			return buildUnlockedTrashAnimationSetLand(view, index, totalDuration);
		}

	}

    private AnimatorSet buildUnlockedTrashAnimationSetPortrait(
            final AuroraRecentlItemView view, final int index,
            final int totalDuration) {
        Log.d("felix", "AuroraPagedView.DEBUG buildUnlockedTrashAnimationSet() " + index + "  " + view.mName);
        int moveDuration = 0; // Move to rubbish bin
        int enterDuration = 0; // Enter stage from right side
        int hideDuration = 0; // Hide view until show time
        int tick = totalDuration / countUnlockedIcons(); // time unit

        if (animateStartInStage(index)) {
            moveDuration = tick
                    * (countUnlockedIcons() - indexOfUnlockedViews(index));
            enterDuration = 0;
            hideDuration = 0;
        } else {
            moveDuration = tick * 3; // 3 ticks for 3 offsets
            enterDuration = tick; // 1 tick for 1 offset
            hideDuration = tick
                    * (countUnlockedIcons() - indexOfUnlockedViews(index))
                    - moveDuration - enterDuration;
            if (hideDuration < 0) hideDuration = 0;
        }


        int offsetX = calUnlockTrashAnimationStartOffsetX(index);

        //Log.d(TAG, "buildUnlockedTrashAnimationSet() [" + index
        //        + "]  view = " + view.mName + "\n\tstep = " + tick
        //        + "\n\tmoveDuration = " + moveDuration + "\n\tholdDuration = "
        //        + hideDuration + "\n\tenterDuration = " + enterDuration
        //        + "\n\toffsetX = " + offsetX);

        // On stage animation
        AnimatorSet animSet = new AnimatorSet();
        ObjectAnimator onStageAnimX = null;
        ObjectAnimator enterStageAnimX = null;
        onStageAnimX = createTranslationXAnimation(view, offsetX, 0,
                moveDuration);
        onStageAnimX.setInterpolator(new LinearInterpolator());
        animSet.play(onStageAnimX)
                .with(createTranslationYAnimation(view,
                        TRASH_ANIMATION_OFFSET_Y_START,
                        TRASH_ANIMATION_OFFSET_Y_END, moveDuration))
                .with(createScaleXAnimation(view, moveDuration))
                .with(createScaleYAnimation(view, moveDuration))
                .with(createAlphaDisappearAnimation(view, moveDuration));

        // Enter stage animation
        if (!animateStartInStage(index)) {
            enterStageAnimX = createTranslationXAnimation(view, OFF_STAGE_X,
                    ON_STAGE_X, enterDuration);
            enterStageAnimX.setInterpolator(new LinearInterpolator());
            animSet.play(enterStageAnimX)
                    .with(createVisibilityAlphaAnimation(view, true,
                            enterDuration))
                    .with(createTranslationYAnimation(view,
                            TRASH_ANIMATION_OFFSET_Y_START,
                            TRASH_ANIMATION_OFFSET_Y_START, enterDuration));
            // delay animation orderly
            if (hideDuration > 0)
                enterStageAnimX.setStartDelay(hideDuration);

            // hide views until it`s show time
            view.setVisibility(View.INVISIBLE);
            enterStageAnimX.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                    super.onAnimationStart(animation);
                }
            });

            // Play onStageAnimX after enterStageAnimX
            animSet.play(onStageAnimX).after(enterStageAnimX);
        }

        // Aurora <Felix.Duan> <2014-4-10> <BEGIN> Always play locked view darken animation
        //// Clean up on last animation end
        //if (indexOfUnlockedViews(index) == 0) {
        //    onTrashAnimationEnd(onStageAnimX);
        //}
        // Aurora <Felix.Duan> <2014-4-10> <END> Always play locked view darken animation
        return animSet;
    }

    private AnimatorSet buildUnlockedTrashAnimationSetLand(
            final AuroraRecentlItemView view, final int index,
            final int totalDuration) {
        Log.d("felix", "AuroraPagedView.DEBUG buildUnlockedTrashAnimationSet() " + index + "  " + view.mName);
        int moveDuration = 0; // Move to rubbish bin
        int enterDuration = 0; // Enter stage from right side
        int hideDuration = 0; // Hide view until show time
        int tick = totalDuration / countUnlockedIcons(); // time unit

        if (animateStartInStage(index)) {
            moveDuration = tick
                    * (countUnlockedIcons() - indexOfUnlockedViews(index));
            enterDuration = 0;
            hideDuration = 0;
        } else {
            moveDuration = tick * 3; // 3 ticks for 3 offsets
            enterDuration = tick; // 1 tick for 1 offset
            hideDuration = tick
                    * (countUnlockedIcons() - indexOfUnlockedViews(index))
                    - moveDuration - enterDuration;
            if (hideDuration < 0) hideDuration = 0;
        }


        int offsetX = calUnlockTrashAnimationStartOffsetY(index);
        //Log.d(TAG, "buildUnlockedTrashAnimationSet() [" + index
        //        + "]  view = " + view.mName + "\n\tstep = " + tick
        //        + "\n\tmoveDuration = " + moveDuration + "\n\tholdDuration = "
        //        + hideDuration + "\n\tenterDuration = " + enterDuration
        //        + "\n\toffsetX = " + offsetX);

        // On stage animation
        AnimatorSet animSet = new AnimatorSet();
        ObjectAnimator onStageAnimY = null;
        ObjectAnimator enterStageAnimY = null;

        onStageAnimY = createTranslationYAnimation(view, offsetX, 0,
                moveDuration);
        onStageAnimY.setInterpolator(new LinearInterpolator());

        animSet.play(onStageAnimY)
                .with(createTranslationXAnimation(view,
                        TRASH_ANIMATION_OFFSET_X_START,
                        TRASH_ANIMATION_OFFSET_X_END, moveDuration))
                .with(createScaleXAnimation(view, moveDuration))
                .with(createScaleYAnimation(view, moveDuration))
                .with(createAlphaDisappearAnimation(view, moveDuration));

        // Enter stage animation
        if (!animateStartInStage(index)) {


            enterStageAnimY = createTranslationYAnimation(view, OFF_STAGE_X,
                    ON_STAGE_X, enterDuration);
            enterStageAnimY.setInterpolator(new LinearInterpolator());
            animSet.play(enterStageAnimY)
                    .with(createVisibilityAlphaAnimation(view, true,
                            enterDuration))
                    .with(createTranslationXAnimation(view,
                            TRASH_ANIMATION_OFFSET_X_START,
                            TRASH_ANIMATION_OFFSET_X_START, enterDuration));
            // delay animation orderly
            if (hideDuration > 0)
                enterStageAnimY.setStartDelay(hideDuration);

            // hide views until it`s show time
            view.setVisibility(View.INVISIBLE);
            enterStageAnimY.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                    super.onAnimationStart(animation);
                }
            });

            // Play onStageAnimY after enterStageAnimY
            animSet.play(onStageAnimY).after(enterStageAnimY);
        }

        // Aurora <Felix.Duan> <2014-4-10> <BEGIN> Always play locked view darken animation
        //// Clean up on last animation end
        //if (indexOfUnlockedViews(index) == 0) {
        //    onTrashAnimationEnd(onStageAnimY);
        //}
        // Aurora <Felix.Duan> <2014-4-10> <END> Always play locked view darken animation
        return animSet;
    }

    /**
     * Delete tasks & views on animation end
     * @param anim to invoke, should be the last finished one
     * 
     * @author Felix.Duan
     * @date 2014-3-28
     */
    private void onTrashAnimationEnd(Animator anim) {
        anim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                Log.d("felix", "AuroraPagedView.DEBUG onTrashAnimationEnd()  onAnimationEnd()");
                // Locked views line-up back to normal state
                AnimatorSet lockedLineUpAnimSet = new AnimatorSet();
                AuroraRecentlItemView view = null;

				if(isOrientationPortrait()){
		            for (int i = 0; i < mIconViews.size(); i++) {
		                view = mIconViews.get(i);
		                if (view.isLocked()) {
		                    int offset = calLockTrashAnimationStartOffsetX(i);
		                    lockedLineUpAnimSet.playTogether(
		                            createLockViewAlphaAnimation(view, false),
		                            createTranslationXAnimation(view, offset, 0,
		                                    TRASH_ANIMATION_LOCKED_DURATION));
		                }
		            }
				} else {
		            for (int i = 0; i < mIconViews.size(); i++) {
		                view = mIconViews.get(i);
		                if (view.isLocked()) {
		                    int offset = calLockTrashAnimationStartOffsetY(i);
		                    lockedLineUpAnimSet.playTogether(
		                            createLockViewAlphaAnimation(view, false),
		                            createTranslationYAnimation(view, offset, 0,
		                                    TRASH_ANIMATION_LOCKED_DURATION));
		                }
		            }
				}
                lockedLineUpAnimSet.start();

                Log.d("felix", "AuroraPagedView.DEBUG onAnimationEnd() countUnlockedIcons() = "
                        + countUnlockedIcons());
                // Delete unlocked recent tasks & views
                for (int i = 0; i < countUnlockedIcons(); i++) {
                    if (recents.indexOfChild(getUnLockedView(i)) != -1) {
                        Log.d("felix", "AuroraPagedView.DEBUG onAnimationEnd() i = " + i
                                + "  " + getUnLockedView(i).mName);
                        recents.removeView(getUnLockedView(i));
                        deleteRecentsTast(getUnLockedView(i));
                    }
                }
                // Aurora <Felix.Duan> <2014-8-8> <BEGIN> Remove orphan views
                removeOrphanViews();
                // Aurora <Felix.Duan> <2014-8-8> <END> Remove orphan views
				// Aurora <Steve.Tang> <2014-11-18> reset clean flag. resolve 8724. start
				mNeedToClean = false;
				// Aurora <Steve.Tang> <2014-11-18> reset clean flag. resolve 8724. end
                mIconViews.clear();
                snapToDestination();
            }
        });
    }

    /**
     * Calculate X axis offset of unlocked view 
     * @param index of unlocked view in @mIconViews
     * @return int value offset
     * 
     * @author Felix.Duan
     * @date 2014-3-28
     */
    private int calUnlockTrashAnimationStartOffsetX(final int index) {
        if (animateStartInStage(index)) {
            // Last 3, at most, unlocked views starts on the stage
            return OFFSET_BASE
                    + ((mIconViews.size() - index - 1) * OFFSET_STEP);
        } else {
            // Others starts off the stage, will play enter stage animation
            return ON_STAGE_X;
        }
    }
    private int calUnlockTrashAnimationStartOffsetY(final int index) {
        if (animateStartInStage(index)) {
            // Last 3, at most, unlocked views starts on the stage
            return -OFFSET_BASE
                    - ((mIconViews.size() - index -1) * OFFSET_STEP);
        } else {
            // Others starts off the stage, will play enter stage animation
            return -ON_STAGE_X;
        }
    }

    /**
     * Calculate X axis offset of locked view
     * @param index of unlocked view in @mIconViews
     * @return int value offset
     * 
     * @author Felix.Duan
     * @date 2014-3-28
     */
    private int calLockTrashAnimationStartOffsetX(final int index) {
        if (animateStartInStage(index)) {
            // Last 3, at most, unlocked views starts on the stage
            // Unlocked view makes locked view offset to right
            int offset = 0;
            for (int i = index; i < mIconViews.size(); ++i) {
                if (!mIconViews.get(i).isLocked()) offset++;
            }
            return offset * OFFSET_STEP;
        } else {
            // Others starts off the stage
            return OFF_STAGE_X;
        }
    }
    private int calLockTrashAnimationStartOffsetY(final int index) {
        if (animateStartInStage(index)) {
            // Last 3, at most, unlocked views starts on the stage
            // Unlocked view makes locked view offset to right
            int offset = 0;
            for (int i = index; i < mIconViews.size(); ++i) {
                if (!mIconViews.get(i).isLocked()) offset++;
            }
            return -offset * OFFSET_STEP;
        } else {
            // Others starts off the stage
            return -OFF_STAGE_X;
        }
    }

    /**
     * Check if a view is in stage when animation starts
     * @param index of view to check
     * @return true if in stage
     * 
     * @author Felix.Duan
     * @date 2014-3-28
     */
    private boolean animateStartInStage(final int index) {
        // Last 3 is in stage when animation starts
        return ((mIconViews.size() - index - 1) < 3);
    }

    /**
     * Count unlocked views in @mIconViews
     * @return the count
     * 
     * @author Felix.Duan
     * @date 2014-3-28
     */
    private int countUnlockedIcons() {
        int count = 0;
        for (AuroraRecentlItemView icon : mIconViews) {
            if (!icon.isLocked()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Count locked views in @mIconViews
     * @return the count
     * 
     * @author Felix.Duan
     * @date 2014-3-28
     */
    private int countLockedIcons() {
        int count = 0;
        for (AuroraRecentlItemView icon : mIconViews) {
            if (icon.isLocked()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Find out unlocked view`s order among it`s own kind in @mIconViews.
     * Used to determine animation duration of unlocked views.
     * Precondition : legal unlock index
     * 
     * @param index of an unlocked view in @mIconViews
     * @return index in unlocked views. Return -1 if illegal
     * 
     * @author Felix.Duan
     * @date 2014-3-28
     */
    private int indexOfUnlockedViews(final int index) {
        // illegal check
        if (index < 0 || index > mIconViews.size() || countUnlockedIcons() < 1)
            return -1;
        int unLockedIndex = -1;
        for (int i = 0; i <= index; i++) {
            if (!mIconViews.get(i).isLocked()) {
                unLockedIndex++;
            }
        }
        return unLockedIndex;
    }

    /**
     * Add @AuriraRecentlItemView to @mIconViews
     * @param icon to add
     * @return true if success, false if already in the list
     * 
     * @author Felix.Duan
     * @date 2014-3-28
     */
    private boolean addIconToList(AuroraRecentlItemView icon) {
        if (mIconViews.contains(icon))
            return false;
        else
            return mIconViews.add(icon);
    }

    /**
     * Get unlocked view of specified index of it`s own kind.
     * Precondition : legal unlock index
     * @param index in unlocked views
     * @return view of the index, null in case not found
     * 
     * @author Felix.Duan
     * @date 2014-3-28
     */
    private AuroraRecentlItemView getUnLockedView(final int index) {
        boolean illegal = (index < 0) || (index > countUnlockedIcons());

        if (!illegal) {
            int n = 0;
            for (AuroraRecentlItemView icon : mIconViews) {
                if ((icon.isLocked() == false) && (index == n++)) {
                    return icon;
                }
            }
        }

        // Should never be here. Indexing must be wrong!
        try {
            throw new NullPointerException("AuroraRecentlItemView " + index
                    + " not found!");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // Aurora <Felix.Duan> <2014-3-28> <END> Optimize remove task animation v2

    public void removeUnlockedViews(){
        Log.d("felix", "AuroraPagedView.DEBUG removeUnlockedViews()");
    	//Aurora <tongyh> <2013-12-13> add recents rubbish animation begin
//    	int pageCount = getChildCount();
//    	AuroraRecentlItemView childView;
//    	///////////////////////////////
    	
    	loadLockedOrUnlockedViews();
        // Aurora <Felix.Duan> <2014-4-10> <BEGIN> Always play locked view darken animation
        // Aurora <Felix.Duan> <2014-3-28> <BEGIN> Optimize remove task animation v2
    	//if (countUnlockedIcons() == 0) {
        //    Log.d(TAG, "removeUnlockedViews()  abort");
    	//    return;
    	//}
        // Aurora <Felix.Duan> <2014-4-10> <END> Always play locked view darken animation
        // Aurora <Felix.Duan> <2014-3-28> <END> Optimize remove task animation v2
    	trashSoundEffect();

    	ViewGroup page = (ViewGroup)getPageAt(0);
    	ArrayList<AnimatorSet> al = new ArrayList<AnimatorSet>();

        // Aurora <Felix.Duan> <2014-3-28> <BEGIN> Optimize remove task animation v2
        int totalDuration = TRASH_ANIMATION_DURATION3; // default
        if (countUnlockedIcons() == 1) {
            totalDuration = TRASH_ANIMATION_DURATION1;
        } else if (countUnlockedIcons() <= 4) {
            totalDuration = TRASH_ANIMATION_DURATION2;
        } else {
            totalDuration = TRASH_ANIMATION_DURATION3;
        }

        // Aurora <Felix.Duan> <2014-4-10> <BEGIN> Always play locked view darken animation
        boolean onlyLockedViews = countUnlockedIcons() == 0;
        for(int i =0 ; i < mIconViews.size(); i ++){
            AnimatorSet set = buildTrashAnimationSet(i, totalDuration);
            if (set != null) {
                if (onlyLockedViews && i == 0) {
                    onTrashAnimationEnd(set);
                } else if (!((AuroraRecentlItemView) mIconViews.get(i))
                        .isLocked() && indexOfUnlockedViews(i) == 0) {
                    onTrashAnimationEnd(set);
                }

                al.add(set);
            }
        // Aurora <Felix.Duan> <2014-4-10> <END> Always play locked view darken animation
        }

        
        for(int i =0 ; i < al.size(); i ++){
            (al.get(i)).start();
        }
        // Aurora <Felix.Duan> <2014-3-28> <END> Optimize remove task animation v2
    	
//    	if(auroraInitExitRubbishAnim()){
//    		Log.d("1219", "auroraInitExitRubbishAnim()");
//    		recentsRubbishAnimDrawable.start();
//    	}
    	

    }

    protected void loadAssociatedPages(int page) {
        loadAssociatedPages(page, false);
    }
    protected void loadAssociatedPages(int page, boolean immediateAndOnly) {
        if (mContentIsRefreshable) {
            final int count = getChildCount();
            if (page < count) {
                int lowerPageBound = getAssociatedLowerPageBound(page);
                int upperPageBound = getAssociatedUpperPageBound(page);
                if (DEBUG) Log.d(TAG, "loadAssociatedPages: " + lowerPageBound + "/"
                        + upperPageBound);
                // First, clear any pages that should no longer be loaded
                for (int i = 0; i < count; ++i) {
                    AuroraPage layout = (AuroraPage) getPageAt(i);
                    if ((i < lowerPageBound) || (i > upperPageBound)) {
                        if (layout.getPageChildCount() > 0) {
                            layout.removeAllViewsOnPage();
                        }
                        mDirtyPageContent.set(i, true);
                    }
                }
                // Next, load any new pages
                for (int i = 0; i < count; ++i) {
                    if ((i != page) && immediateAndOnly) {
                        continue;
                    }
                    if (lowerPageBound <= i && i <= upperPageBound) {
                        if (mDirtyPageContent.get(i)) {
                            syncPageItems(i, (i == page) && immediateAndOnly);
                            mDirtyPageContent.set(i, false);
                        }
                    }
                }
            }
        }
    }

    protected int getAssociatedLowerPageBound(int page) {
        return Math.max(0, page - 1);
    }
    protected int getAssociatedUpperPageBound(int page) {
        final int count = getChildCount();
        return Math.min(page + 1, count - 1);
    }

    /**
     * This method is called ONLY to synchronize the number of pages that the paged view has.
     * To actually fill the pages with information, implement syncPageItems() below.  It is
     * guaranteed that syncPageItems() will be called for a particular page before it is shown,
     * and therefore, individual page items do not need to be updated in this method.
     */
    public abstract void syncPages();

    
    
    /**
     * This method is called to synchronize the items that are on a particular page.  If views on
     * the page can be reused, then they should be updated within this method.
     */
    public abstract void syncPageItems(int page, boolean immediate);

    protected void invalidatePageData() {
        invalidatePageData(-1, false);
    }
    protected void invalidatePageData(int currentPage) {
        invalidatePageData(currentPage, false);
    }
    protected void invalidatePageData(int currentPage, boolean immediateAndOnly) {
        if (!mIsDataReady) {
            return;
        }

        if (mContentIsRefreshable) {
            // Force all scrolling-related behavior to end
            mScroller.forceFinished(true);
            mNextPage = INVALID_PAGE;

            // Update all the pages
            syncPages();

            // We must force a measure after we've loaded the pages to update the content width and
            // to determine the full scroll width
            measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));

            // Set a new page as the current page if necessary
            if (currentPage > -1) {
                setCurrentPage(Math.min(getPageCount() - 1, currentPage));
            }

            // Mark each of the pages as dirty
            final int count = getChildCount();
            mDirtyPageContent.clear();
            for (int i = 0; i < count; ++i) {
                mDirtyPageContent.add(true);
            }

            // Load any pages that are necessary for the current window of views
            loadAssociatedPages(mCurrentPage, immediateAndOnly);
            requestLayout();
        }
    }

    protected View getScrollingIndicator() {
        // We use mHasScrollIndicator to prevent future lookups if there is no sibling indicator
        // found
    	/*
        if (mHasScrollIndicator && mScrollIndicator == null) {
            ViewGroup parent = (ViewGroup) getParent();
            if (parent != null) {
                mScrollIndicator = (View) (parent.findViewById(R.id.paged_view_indicator));
                mHasScrollIndicator = mScrollIndicator != null;
                if (mHasScrollIndicator) {
                    mScrollIndicator.setVisibility(View.VISIBLE);
                }
            }
        }
        return mScrollIndicator;
        */
    	return null;
    }

    protected boolean isScrollingIndicatorEnabled() {
    	return false;
        //return true;
    }

    Runnable hideScrollingIndicatorRunnable = new Runnable() {
        @Override
        public void run() {
            hideScrollingIndicator(false);
        }
    };
    protected void flashScrollingIndicator(boolean animated) {
        removeCallbacks(hideScrollingIndicatorRunnable);
        showScrollingIndicator(!animated);
        postDelayed(hideScrollingIndicatorRunnable, sScrollIndicatorFlashDuration);
    }


	protected void showScrollingIndicator(boolean immediately) {
        mShouldShowScrollIndicator = true;
        mShouldShowScrollIndicatorImmediately = true;
        if (getChildCount() <= 1) return;
        if (!isScrollingIndicatorEnabled()) return;

        mShouldShowScrollIndicator = false;
        getScrollingIndicator();
        if (mScrollIndicator != null) {
            // Fade the indicator in
            updateScrollingIndicatorPosition();
            mScrollIndicator.setVisibility(View.VISIBLE);
            cancelScrollingIndicatorAnimations();
            if (immediately || mScrollingPaused) {
                mScrollIndicator.setAlpha(1f);
            } else {
                mScrollIndicatorAnimator = AuroraAnimUtils.ofFloat(mScrollIndicator, "alpha", 1f);
                mScrollIndicatorAnimator.setDuration(sScrollIndicatorFadeInDuration);
                mScrollIndicatorAnimator.start();
            }
        }
    }


	protected void cancelScrollingIndicatorAnimations() {
        if (mScrollIndicatorAnimator != null) {
            mScrollIndicatorAnimator.cancel();
        }
    }


	protected void hideScrollingIndicator(boolean immediately) {
        if (getChildCount() <= 1) return;
        if (!isScrollingIndicatorEnabled()) return;

        getScrollingIndicator();
        if (mScrollIndicator != null) {
            // Fade the indicator out
            updateScrollingIndicatorPosition();
            cancelScrollingIndicatorAnimations();
            if (immediately || mScrollingPaused) {
                mScrollIndicator.setVisibility(View.INVISIBLE);
                mScrollIndicator.setAlpha(0f);
            } else {
                mScrollIndicatorAnimator = AuroraAnimUtils.ofFloat(mScrollIndicator, "alpha", 0f);
                mScrollIndicatorAnimator.setDuration(sScrollIndicatorFadeOutDuration);
                mScrollIndicatorAnimator.addListener(new AnimatorListenerAdapter() {
                    private boolean cancelled = false;
                    @Override
                    public void onAnimationCancel(android.animation.Animator animation) {
                        cancelled = true;
                    }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!cancelled) {
                            mScrollIndicator.setVisibility(View.INVISIBLE);
                        }
                    }
                });
                mScrollIndicatorAnimator.start();
            }
        }
    }

    /**
     * To be overridden by subclasses to determine whether the scroll indicator should stretch to
     * fill its space on the track or not.
     */
    protected boolean hasElasticScrollIndicator() {
        return true;
    }

    private void updateScrollingIndicator() {
        if (getChildCount() <= 1) return;
        if (!isScrollingIndicatorEnabled()) return;

        getScrollingIndicator();
        if (mScrollIndicator != null) {
            updateScrollingIndicatorPosition();
        }
        if (mShouldShowScrollIndicator) {
            showScrollingIndicator(mShouldShowScrollIndicatorImmediately);
        }
    }

    private void updateScrollingIndicatorPosition() {
        if (!isScrollingIndicatorEnabled()) return;
        if (mScrollIndicator == null) return;
        int numPages = getChildCount();
        int pageWidth = getMeasuredWidth();
        int lastChildIndex = Math.max(0, getChildCount() - 1);
        int maxScrollX = getChildOffset(lastChildIndex) - getRelativeChildOffset(lastChildIndex);
        int trackWidth = pageWidth - mScrollIndicatorPaddingLeft - mScrollIndicatorPaddingRight;
        int indicatorWidth = mScrollIndicator.getMeasuredWidth() -
                mScrollIndicator.getPaddingLeft() - mScrollIndicator.getPaddingRight();

        float offset = Math.max(0f, Math.min(1f, (float) getScrollX() / maxScrollX));
        int indicatorSpace = trackWidth / numPages;
        int indicatorPos = (int) (offset * (trackWidth - indicatorSpace)) + mScrollIndicatorPaddingLeft;
        if (hasElasticScrollIndicator()) {
            if (mScrollIndicator.getMeasuredWidth() != indicatorSpace) {
                mScrollIndicator.getLayoutParams().width = indicatorSpace;
                mScrollIndicator.requestLayout();
            }
        } else {
            int indicatorCenterOffset = indicatorSpace / 2 - indicatorWidth / 2;
            indicatorPos += indicatorCenterOffset;
        }
        mScrollIndicator.setTranslationX(indicatorPos);
    }

    public void showScrollIndicatorTrack() {
    }

    public void hideScrollIndicatorTrack() {
    }

    /* Accessibility */
	@Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setScrollable(getPageCount() > 1);
        if (getCurrentPage() < getPageCount() - 1) {
            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        }
        if (getCurrentPage() > 0) {
            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setScrollable(true);
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            event.setFromIndex(mCurrentPage);
            event.setToIndex(mCurrentPage);
            event.setItemCount(getChildCount());
        }
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (super.performAccessibilityAction(action, arguments)) {
            return true;
        }
        switch (action) {
            case AccessibilityNodeInfo.ACTION_SCROLL_FORWARD: {
                if (getCurrentPage() < getPageCount() - 1) {
                    scrollRight();
                    return true;
                }
            } break;
            case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD: {
                if (getCurrentPage() > 0) {
                    scrollLeft();
                    return true;
                }
            } break;
        }
        return false;
    }

    protected String getCurrentPageDescription() {
    	/*
        return String.format(getContext().getString(R.string.default_scroll_format),
                 getNextPage() + 1, getChildCount());
                 */
    	String str = Integer.toString(getNextPage() + 1) + ","+Integer.toString(getChildCount());
        return str;
    }

    @Override
    public boolean onHoverEvent(android.view.MotionEvent event) {
        return true;
    }
    
  //Aurora <tongyh> <2013-12-13> add Alpha animated slide up and down begin
    private ObjectAnimator createAlphaAnimation(View v) {

        // Aurora <Felix.Duan> <2014-10-9> <BEGIN> Fix BUG #8954. Reset to full opacity.
        ObjectAnimator anim = ObjectAnimator.ofFloat(v,"alpha", 1.0f);
        // Aurora <Felix.Duan> <2014-10-9> <END> Fix BUG #8954. Reset to full opacity.
        return anim;
    }
  //Aurora <tongyh> <2013-12-13> add Alpha animated slide up and down end

  //Aurora <tongyh> <2013-12-13> add recents rubbish animation begin
    private void auroraInitRubbishAnim()
	{
    	ViewGroup child = (ViewGroup)getPageAt(0);
		if(child != null && child.getChildAt(0) != null)
		{
			if(recentsRubbish == null){
				recentsRubbish = (AuroraRubbishView)child.findViewById(R.id.clear_all);
//				recentsRubbish.setImageResource(R.drawable.aurora_recents_rubbish_anim);
//				recentsRubbishAnimDrawable = (AnimationDrawable)recentsRubbish.getDrawable();
			}
		}
	}
    
    /*private void recentsUpdateRubbish(float deltaX){
    	if(recentsRubbish != null && recentsRubbishAnimDrawable != null){
    		deltaX = Math.min(deltaX, 270);
    		int index = 0;
    		try {
				index = 23 - (int)Math.abs(deltaX)/8;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				index = 23;
			}
//    		recentsRubbish.setImageLevel(index);
    		if(index < 0){
    			index = 0;
    		}
    		if(index > 23){
    			index = 23;
    		}
    		recentsRubbish.setImageDrawable(recentsRubbishAnimDrawable.getFrame(index));
    	}
    }*/
  //Aurora <tongyh> <2013-12-13> add recents rubbish animation end
    
  //Aurora <tongyh> <2013-12-20> add Absorb garbage animation begin
    public void loadLockedOrUnlockedViews(){
        Log.d("felix", "AuroraPagedView.DEBUG loadLockedOrUnlockedViews()");
    	recents = ((FrameLayout)(getParent()));
    	int pageCount = getChildCount();
    	AuroraRecentlItemView childView;
//    	ViewGroup page = (ViewGroup)getPageAt(1);
//        childView = (AuroraRecentlItemView) page.getChildAt(0);
//    	page.removeView(childView);
    	/*for(int i = pageCount - 1;i >= 1;--i){
    		ViewGroup page = (ViewGroup)getPageAt(i);
    		int childCount = page.getChildCount();
    		for(int j = childCount - 1;j >= 0;--j){
    			childView = (AuroraRecentlItemView) page.getChildAt(j);
    			if(childView.isLocked()){
    				isHasLock = true;
    				mLockedViews.add(childView);
    			}else{
    				((AuroraPageNormal)page).tempRemoveView(childView);
    				addViewToAnimation(childView);
    				mUnlockedViews.add(childView);
    			}
    		}
    	}*/
    	lockIitemViewLocationSort();
// Aurora <Felix.Duan> <2014-4-10> <BEGIN> Always play locked view darken animation
//    	if(countUnlockedIcons() > 0){
////    		ViewGroup page = (ViewGroup)getPageAt(0);
////    		rubbishView = (ImageView) page.getChildAt(0);
////    		page.removeView(rubbishView);
////    		addRubbishView(rubbishView);
////    		Log.d("1219", "countUnlockedIcons() = " + countUnlockedIcons());
////    		snapToDestination();
//    	}else{
//    		snapToDestination();
////    		return;
//    	}
// Aurora <Felix.Duan> <2014-4-10> <END> Always play locked view darken animation
    	
    }
    
    private void addViewToAnimation(AuroraRecentlItemView childView){
    	DragLayer.LayoutParams lp;
    	if (childView.getLayoutParams() instanceof DragLayer.LayoutParams) {
    		lp = (DragLayer.LayoutParams) childView.getLayoutParams();
    	} else {
    		lp = new DragLayer.LayoutParams(childView.getWidth(), childView.getHeight());
    	}
    	lp.x = (int)childView.getX();
    	lp.y = (int)childView.getY();
    	lp.width = childView.getWidth();
    	lp.height = childView.getHeight();
		if(isOrientationPortrait()){
			lp.gravity = Gravity.TOP;
		}else{
			lp.gravity = Gravity.BOTTOM;
		}
    	lp.customPosition = true;
    	recents.addView(childView, lp);
    }
    
    private void addRubbishView(ImageView childView){
    	DragLayer.LayoutParams lp;
    	if (childView.getLayoutParams() instanceof DragLayer.LayoutParams) {
    		lp = (DragLayer.LayoutParams) childView.getLayoutParams();
    	} else {
    		lp = new DragLayer.LayoutParams(childView.getWidth(), childView.getHeight());
    	}
    	lp.x = (int)childView.getX();
    	lp.y = (int)childView.getY();
    	lp.width = childView.getWidth();
    	lp.height = childView.getHeight();
    	lp.customPosition = true;
    	recents.addView(childView, lp);
    }
    
    // Aurora <Felix.Duan> <2014-3-28> <BEGIN> Optimize remove task animation v2
    private ObjectAnimator createLockViewAlphaAnimation(View v,boolean darken) {
    	float alp = 0f;
    	float toAlp = 0f;
    	if(darken){
    		alp = 1.0f;
    		toAlp = 0.1f;
    	}else{
    		alp = 0.1f;
    		toAlp = 1.0f;
    	}
    	return ObjectAnimator.ofFloat(v,"alpha", alp, toAlp).setDuration(TRASH_ANIMATION_LOCKED_DURATION);
    }
    
    // Aurora <felix.duan> <2014-3-20> <BEGIN> Optimize remove task animation
    private ObjectAnimator createTranslationXAnimation(View v,float start, float end, int duration) {
        return ObjectAnimator.ofFloat(v,"translationX", start, end).setDuration(duration);
    }

    private ObjectAnimator createTranslationYAnimation(View v, float start, float end, int duration) {
        return ObjectAnimator.ofFloat(v, "translationY", start, end).setDuration(duration);
    }

    private ObjectAnimator createAlphaDisappearAnimation(View v, int duration) {
        return ObjectAnimator.ofFloat(v, "alpha", 1.0f, 0f)
                .setDuration(duration);
    }
    
    /**
     * Fully Show or fully hide a view
     * 
     * @param v to manipulate
     * @param visible of the view
     * @param duration of this animation
     * @return ObjectAnimator
     * 
     * @author Felix.Duan
     * @date 2014-3-28
     */
    private ObjectAnimator createVisibilityAlphaAnimation(View v, boolean visible, int duration) {
        float alpha = (visible == true) ? 1f : 0f;
        return ObjectAnimator.ofFloat(v, "alpha", alpha, alpha).setDuration(duration);
    }
    // Aurora <Felix.Duan> <2014-3-28> <END> Optimize remove task animation v2

    private ObjectAnimator createScaleXAnimation(View v, int duration) {
        return ObjectAnimator.ofFloat(v, "scaleX", 0.1f).setDuration(duration);
    }

    private ObjectAnimator createScaleYAnimation(View v, int duration) {
        return ObjectAnimator.ofFloat(v, "scaleY", 0.1f).setDuration(duration);
    }
    // Aurora <felix.duan> <2014-3-20> <END> Optimize remove task animation
    
    public abstract void deleteRecentsTast(View v);
    
    /*private boolean auroraInitExitRubbishAnim()
	{
    	ViewGroup child = (ViewGroup)getPageAt(0);
		if(child != null && child.getChildAt(0) != null)
		{
			if(recentsRubbish == null){
				recentsRubbish = (ImageView)child.findViewById(R.id.clear_all);
				recentsRubbish.setImageResource(R.drawable.aurora_recents_rubbish_anim_exit);
				recentsRubbishAnimDrawable = (AnimationDrawable)recentsRubbish.getDrawable();
			}
			return true;
		}else{
			return false;
		}
	}*/
  //Aurora <tongyh> <2013-12-20> add Absorb garbage animation end
    public void lockIitemViewLocationSort(){
        // Aurora <Felix.Duan> <2014-3-28> <BEGIN> Optimize remove task animation v2
        mIconViews.clear();
        // Aurora <Felix.Duan> <2014-3-28> <END> Optimize remove task animation v2
    	int pageCount = getChildCount();
    	AuroraRecentlItemView ActualChildView;
    	for(int i = pageCount - 1;i >= 1;--i){
    		ViewGroup actualPaget = (ViewGroup)getPageAt(i);
    		int childCount = actualPaget.getChildCount();
    		for(int j = childCount - 1;j >= 0;--j){
    			ActualChildView = (AuroraRecentlItemView) actualPaget.getChildAt(j);
    			if(ActualChildView.isLocked()){
    				isHasLock = true;
    				addIconToList(ActualChildView);
    			}else{
    				
    				((AuroraPageNormal)actualPaget).tempRemoveView(ActualChildView);
    				addViewToAnimation(ActualChildView);
    				addIconToList(ActualChildView);
					int index = i + 1;
            		ViewGroup next_page = (ViewGroup)getPageAt(index);
					ViewGroup perv_page = actualPaget;
            		while(null != next_page){
            			View moveView =  next_page.getChildAt(0);
            			((AuroraPageNormal)next_page).tempRemoveView(moveView);
            			perv_page.addView(moveView);
            			if(0 == next_page.getChildCount()){
            				this.removeView(next_page);
							break;
            			}
						perv_page = next_page;
						next_page = (ViewGroup)getPageAt(++index);
            		}
        			if(0 == actualPaget.getChildCount()){
        				this.removeView(actualPaget);
        				if(getChildCount() == 1){
        					return;
        				}else{
        					//snapToDestination();
        					break;
        				}

        			}
    			}
    		}
    			//Aurora <tongyh> <2013-12-13> add recents rubbish animation end
    	}
    }

    /**
     * Play sound effect when removing unlocked views.
     * Precondition : (countUnlockedIcons() > 0)
     * 
     * @author: felix.duan
     * @date: 2014-3-17
     */
    private void trashSoundEffect() {
        Log.d(TAG, "trashSoundEffect()  countUnlockedIcons() = " + countUnlockedIcons());
        if (countUnlockedIcons() == 0)
            return;

        // Aurora <felix.duan> <2014-3-20> <BEGIN> Capability to mute trash sound effect  
        // Mute if SOUND_EFFECTS_ENABLED is disabled
        if (Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.SOUND_EFFECTS_ENABLED, 0) == 0)
            return;
        // Aurora <felix.duan> <2014-3-20> <END> Capability to mute trash sound effect 

        AsyncTask<Void, Void, Void> playTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                Log.d(TAG, "trashSoundEffect()");
                MediaPlayer mp = new MediaPlayer();
                try {
                    mp.setDataSource("/system/media/audio/ui/CacheClear.ogg"); // Hard coded
                    mp.setAudioStreamType(AudioManager.STREAM_SYSTEM);
                    mp.setOnCompletionListener(new OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Log.d(TAG, "trashSoundEffect() clean up");
                            if (mp != null) {
                                mp.reset();
                                mp.release();
                                mp = null;
                            }
                        }

                    });
                    mp.prepare();
                    mp.start();
                } catch (Exception e) {
                    Log.d(TAG, "trashSoundEffect() exception : \n" + e);
                    // clean up
                    if (mp != null) {
                        mp.reset();
                        mp.release();
                        mp = null;
                    }
                }
                return null;
            }

        };
        playTask.execute();
    }

    // Aurora <Felix.Duan> <2014-4-28> <BEGIN> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw
    // Aurora <Felix.Duan> <2014-4-10> <BEGIN> Disable AuroraPagedView scroll before full page loaded.
    /**
     * Set this view scrollable state.
     * Pull up @HandlerBar, invoke this view to show up.
     * At this time we load only one page of task icons instead of
     * full pages. When user scroll, this view behaves like there
     * is only one page. This is misleading.
     * 
     * Just disable scrolling before we load full pages.
     * Re enable scrolling after pages loaded.
     * 
     * TODO need a better solution than this.
     * 
     * @author Felix.Duan.
     * @date 2014-4-10
     */
    private boolean mScrollable = true;
    public void setScrollable(boolean scrollable) {
        if (BaseStatusBar.FELIXDBG) Log.d("felix", "AuroraPagedView.DEBUG setScrollable() " + scrollable);
        mScrollable = scrollable;
    }

    public boolean isScrollable() {
        if (BaseStatusBar.FELIXDBG) Log.d("felix", "AuroraPagedView.DEBUG isScrollable() " + mScrollable);
        return mScrollable;
    }
    // Aurora <Felix.Duan> <2014-4-10> <END> Disable AuroraPagedView scroll before full page loaded.
    // Aurora <Felix.Duan> <2014-4-28> <END> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw

    // Aurora <Felix.Duan> <2014-8-8> <BEGIN> Remove orphan views
    /**
     * Remove any AuroraRecentlItemView under DragLayer.
     * Normally, there`s nothing to do.
     * But, somehow there are orphan views
     * Deal with that.
     *
     * @author  Felix.Duan
     * @date    2014-8-12
     */
    private void removeOrphanViews() {
        View view;
        int count = recents.getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            view = recents.getChildAt(i);
            if (AuroraRecentlItemView.class.isInstance(view)) {
                Log.d("felix","AuroraPagedView.DEBUG removeOrphanViews() instanceof AuroraRecentlItemView!");
                recents.removeView(view);
            }
        }
    }
    // Aurora <Felix.Duan> <2014-8-8> <END> Remove orphan views

}
