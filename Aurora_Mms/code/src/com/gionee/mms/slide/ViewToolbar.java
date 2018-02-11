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

import com.android.mms.R;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.util.Log;
/**
 * The base class is used to support toolbar popped up form views.
 *
 */
public abstract class ViewToolbar {
    //Gionee CR00311071 jipengfei 20110730 start
    private static final int TOLERANCE_TOUCH = 3;
    private static final int TOOLBAR_ITEM_PADDING_LEFT_AND_RIGHT = 12;
    private static final int TOOLBAR_ITEM_PADDING_BOTTOM = 3;
    private static final int TOOLBAR_POSITION_OFFSET_TO_SCREEN_LEFT = 14;
    private static final int TOOLBAR_POSITION_OFFSET_TO_SCREEN_RIGHT = 14;
    private static final int TOOLBAR_ARROW_OFFSET_TO_EDGE = 15;
    //Gionee CR00311071 jipengfei 20110730 end
    protected View mHostView;
    protected Context mContext;

    protected WindowManager mWindowManager;
    protected WindowManager.LayoutParams mLayoutParams = null;
    protected LayoutInflater mLayoutInflater;
    protected ViewGroup mToolbarGroup;
    protected View mToolbarView;
    protected ImageView mToolbarPositionArrowView;

    protected boolean mShowing = false;

    private Drawable mLeftDrawable;
    private int mCenterDrawableResId;
    private Drawable mRightDrawable;
    private Drawable mSingleDrawable;
    private Drawable mArrowAboveDrawable;
    private Drawable mArrowBelowDrawable;

    private int mStatusBarHeight;
    private int mToolbarPositionArrowWidth;
    private int mToolbarPositionArrowHeight;

    private int mPositionX, mPositionY;

    protected int mToleranceTouch;
    protected int mToolbarItemPaddingLeftAndRight;
    protected int mToolbarItemPaddingBottom;

    public ViewToolbar(View hostView) {
        this.mHostView = hostView;
        this.mContext = mHostView.getContext();

        // initial resources
        Resources resources = mHostView.getResources();
        //mLeftDrawable = resources.getDrawable(R.drawable.gn_text_toolbar_left);
        //mCenterDrawableResId = R.drawable.gn_text_toolbar_center;
        //mRightDrawable = resources.getDrawable(R.drawable.gn_text_toolbar_right);
        //mSingleDrawable = resources.getDrawable(R.drawable.gn_text_toolbar_single);
        //mArrowAboveDrawable = resources.getDrawable(R.drawable.gn_text_toolbar_position_arrow_above);
        //mArrowBelowDrawable = resources.getDrawable(R.drawable.gn_text_toolbar_position_arrow_below);
        // initial window manager
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        calculateTolerance();
        mStatusBarHeight = resources.getDimensionPixelSize(R.dimen.status_bar_height);
        // initial tool bar and it's items.
        mLayoutInflater = LayoutInflater.from(mContext);
        mToolbarView = mLayoutInflater.inflate(R.layout.gn_text_toolbar, null);
        mToolbarGroup = (ViewGroup) mToolbarView.findViewById(R.id.gn_toolbar_group);
        //Gionee CR00311071 jipengfei 20110730: comment out this line, it has nonsense
        //mToolbarGroup.setPadding(2, 2, 2, 2);
        mToolbarPositionArrowView = (ImageView) mToolbarView.findViewById(R.id.gn_toolbar_position_arrow);
       
        // calculate initial size of tool bar.
        mToolbarView.measure(0, 0);
        mToolbarPositionArrowWidth = mToolbarPositionArrowView.getMeasuredWidth();
        mToolbarPositionArrowHeight = mToolbarPositionArrowView.getMeasuredHeight();
    }

    /**
     * @return Whether the toolbar is showing.
     */
    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Show toolbar at assigned position relative to left-top of screen.
     * @param screenX
     * @param screenY
     * @param selected
     */
    public void show(int screenX, int screenY, boolean selected) {
        if (!mShowing) {
            showInternal(screenX, screenY, 0, selected);
        }
    }

    /**
     * Move toolbar to assigned position relative to left-top of screen.
     * @param screenX
     * @param screenY
     */
    public void move(int screenX, int screenY, boolean selected) {
        if (mShowing) {
            moveInternal(screenX, screenY, 0, selected);
        }
    }

    /**
     * Hide the toolbar.
     */
    public void hide() {
        if (mShowing) {
            try {
                //Gionee CR00311071 jipengfei 20110730: comment out this line, it has nonsense
                //mToolbarGroup.setPadding(2, 2, 2, 2);
                mToolbarPositionArrowView.setPadding(0, 0, 0, 0);
                mWindowManager.removeViewImmediate(mToolbarView);
            } finally {
                // set showing flag whether hiding view is successful.
                mShowing = false;
            }
        }
    }

    /**
     * Update items of toolbar.
     */
    protected abstract void updateToolbarItems();

    protected void showInternal(int screenX, int screenY, int cursorLineHeight, boolean selected) {
        // update tool bar.
        update();
        if (mToolbarGroup.getChildCount() < 1) {
            hide();
            return;
        }
        prepare(screenX, screenY, cursorLineHeight, selected);
        // reposition the toolbar.
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.token = mHostView.getWindowToken();
        lp.x = mPositionX;
        lp.y = mPositionY;
        lp.width = LayoutParams.WRAP_CONTENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
            lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        } else {
            lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED;
        }
        lp.packageName = mContext.getPackageName();
        mLayoutParams = lp;
        mWindowManager.addView(mToolbarView, lp);
        // set showing flag
        mShowing = true;
    }

    protected void moveInternal(int screenX, int screenY, int cursorLineHeight, boolean selected) {
        if (mToolbarGroup.getChildCount() < 1) {
            hide();
            return;
        }
        prepare(screenX, screenY, cursorLineHeight, selected);
        // reposition the toolbar.
        WindowManager.LayoutParams lp = mLayoutParams;
        lp.x = mPositionX;
        lp.y = mPositionY;
        mWindowManager.updateViewLayout(mToolbarView, lp);
    }

    private void prepare(int screenX, int screenY, int cursorLineHeight, boolean selected) {
        // calculate the size of tool bar.
        mToolbarView.measure(0, 0);
        // calculate the position of tool bar.
        boolean aboveCursor = calculatePosition(screenX, screenY, cursorLineHeight, selected);
        // set position of arrow representing the trigger point. 
        int paddingLeft = screenX - mPositionX - mToolbarPositionArrowWidth / 2;
        
        //Gionee CR00311071 jipengfei 20110730: modify the calculations of paddingLeft start
        paddingLeft = Math.max(TOOLBAR_ARROW_OFFSET_TO_EDGE + TOOLBAR_POSITION_OFFSET_TO_SCREEN_LEFT, paddingLeft);
        paddingLeft = Math.min(mToolbarGroup.getMeasuredWidth() - mToolbarPositionArrowWidth - TOOLBAR_ARROW_OFFSET_TO_EDGE - TOOLBAR_POSITION_OFFSET_TO_SCREEN_RIGHT, paddingLeft);
        if (aboveCursor) {
            mToolbarPositionArrowView.setImageDrawable(mArrowBelowDrawable);
            //mToolbarGroup.setPadding(2, 2, 2, 2);
            mToolbarPositionArrowView.setPadding(paddingLeft, mToolbarGroup.getMeasuredHeight() - mArrowBelowDrawable.getIntrinsicHeight(), 0, 0);
        } else {
            mToolbarPositionArrowView.setImageDrawable(mArrowAboveDrawable);
            //mToolbarGroup.setPadding(2, 0, 2, 2);
            mToolbarPositionArrowView.setPadding(paddingLeft, 0, 0, 0);
        }
        //Gionee CR00311071 jipengfei 20110730: modify the calculations of paddingLeft end
    }

    private void update() {
        updateToolbarItems();
        // set drawable of items.
        int childCount = mToolbarGroup.getChildCount();
        if (childCount >= 2) {
            for (int i = 0; i < childCount; i++) {
                View view = mToolbarGroup.getChildAt(i);
                if (i == 0) {
                    view.setBackgroundDrawable(mLeftDrawable);
                    view.setPadding(mToolbarItemPaddingLeftAndRight * 2 + 1, 0, mToolbarItemPaddingLeftAndRight, mToolbarItemPaddingBottom);
                } else if (i == childCount - 1) {
                    view.setBackgroundDrawable(mRightDrawable);
                    view.setPadding(mToolbarItemPaddingLeftAndRight, 0, mToolbarItemPaddingLeftAndRight * 2, mToolbarItemPaddingBottom);
                } else {
                    view.setBackgroundResource(mCenterDrawableResId);
                    view.setPadding(mToolbarItemPaddingLeftAndRight, 0, mToolbarItemPaddingLeftAndRight + 1, mToolbarItemPaddingBottom);
                }
            }
        } else if (childCount == 1) {
            View view = mToolbarGroup.getChildAt(0);
            view.setBackgroundDrawable(mSingleDrawable);
            view.setPadding(mToolbarItemPaddingLeftAndRight * 2, 0, mToolbarItemPaddingLeftAndRight * 2, mToolbarItemPaddingBottom);
        }
    }

    private boolean calculatePosition(int screenX, int screenY, int cursorLineHeight, boolean selected) {
        // ginoee gaoj 2011-11-28 added for CR00405180 start
       boolean aboveCursor = true;
      
    // calculate x
    int x;
    int px = screenX - mHostView.getRootView().getScrollX();
    int half = mToolbarGroup.getMeasuredWidth() / 2;
    int displayWidth = mWindowManager.getDefaultDisplay().getWidth();
    if (px + half < displayWidth) {
        x = px - half;
    } else {
        x = displayWidth - mToolbarGroup.getMeasuredWidth();
    }
    mPositionX = Math.max(0, x);
    
    // calculate y
    int y;
    int py = screenY - mHostView.getRootView().getScrollY();
    int th = mToolbarGroup.getMeasuredHeight() + mToolbarPositionArrowHeight;
    int lh = cursorLineHeight / 2;
    if (py - th - lh < mStatusBarHeight) {
        y = py + lh + (selected ? mToleranceTouch : 0) + 2;
        aboveCursor = false;
    } else {
        y = py - th - lh - (selected ? mToleranceTouch : 0) + 6;
        aboveCursor = true;
    }
    mPositionY = Math.max(mStatusBarHeight, y);
    return aboveCursor;
        // ginoee gaoj 2011-11-28 added for CR00405180 end
        /*boolean aboveCursor = true;
        // calculate x
        int px = screenX - mHostView.getRootView().getScrollX();
        int half = mToolbarGroup.getMeasuredWidth() / 2;
        int displayWidth = mWindowManager.getDefaultDisplay().getWidth();
        //Gionee CR00311071 jipengfei 20110730: modify the calculations of mPositionX start
        int leftEdge = px - half;
        int rightEdge = px + half;
        if(leftEdge < -TOOLBAR_POSITION_OFFSET_TO_SCREEN_LEFT){
            mPositionX = -TOOLBAR_POSITION_OFFSET_TO_SCREEN_LEFT;
        }else if(rightEdge > displayWidth + TOOLBAR_POSITION_OFFSET_TO_SCREEN_RIGHT){
            mPositionX = displayWidth + TOOLBAR_POSITION_OFFSET_TO_SCREEN_RIGHT - mToolbarGroup.getMeasuredWidth();
        }else{
            mPositionX = px - half;
        }
        
        // calculate y
        int py = screenY - mHostView.getRootView().getScrollY();
        int th = mToolbarGroup.getMeasuredHeight() + mToolbarPositionArrowHeight;
        int lh = cursorLineHeight;
        if (py - th - lh < mStatusBarHeight) {
            mPositionY = py + lh + (selected ? mToleranceTouch : 0);
            aboveCursor = false;
        } else {
            mPositionY = py - th - (selected ? mToleranceTouch : 0);
            aboveCursor = true;
        }
        //Gionee CR00311071 jipengfei 20110730: modify the calculations of mPositionX end
        return aboveCursor;*/
    }

    private void calculateTolerance() {
        DisplayMetrics dm = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getMetrics(dm);
        float ratio = 1.0f * dm.densityDpi / DisplayMetrics.DENSITY_MEDIUM;
        mToleranceTouch = Math.round(TOLERANCE_TOUCH * ratio);
        mToolbarItemPaddingLeftAndRight = Math.round(TOOLBAR_ITEM_PADDING_LEFT_AND_RIGHT * ratio);
        mToolbarItemPaddingBottom = Math.round(TOOLBAR_ITEM_PADDING_BOTTOM * ratio);
    }

}
