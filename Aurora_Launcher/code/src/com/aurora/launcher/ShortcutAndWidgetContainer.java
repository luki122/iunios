/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.aurora.launcher;

import java.util.ArrayList;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class ShortcutAndWidgetContainer extends ViewGroup {
    static final String TAG = "CellLayoutChildren";

    // These are temporary variables to prevent having to allocate a new object just to
    // return an (x, y) value from helper functions. Do NOT use them to maintain other state.
    private final int[] mTmpCellXY = new int[2];

    private final WallpaperManager mWallpaperManager;

    private int mCellWidth;
    private int mCellHeight;

    private int mWidthGap;
    private int mHeightGap;
    
    private int mLastCountX;
    private float mScaleOverFour;
    private float mPreScaleX = 1.0f;
    private float mPreScaleY = 1.0f;

    public ShortcutAndWidgetContainer(Context context) {
        super(context);
        mWallpaperManager = WallpaperManager.getInstance(context);
        mScaleOverFour = context.getResources().getInteger(R.integer.hotseat5_scale)/100f;
    }

    public void setCellDimensions(int cellWidth, int cellHeight, int widthGap, int heightGap ) {
        mCellWidth = cellWidth;
        mCellHeight = cellHeight;
        mWidthGap = widthGap;
        mHeightGap = heightGap;
    }

    public View getChildAt(int x, int y) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();

            if ((lp.cellX <= x) && (x < lp.cellX + lp.cellHSpan) &&
                    (lp.cellY <= y) && (y < lp.cellY + lp.cellVSpan)) {
                return child;
            }
        }
        return null;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
    	int mCountX = getChildCount();
    	if (getParent().getParent() instanceof Hotseat) {
    		Log.v("hotseat5", "mCountX="+mCountX);
			if (mLastCountX == 4 && mCountX == 5) {
				mPreScaleX = getChildAt(0).getScaleX();
				mPreScaleY = getChildAt(0).getScaleY();
				for (int i = getChildCount() - 1; i >= 0; i--) {
	                View child = getChildAt(i);
	                child.setScaleX(mScaleOverFour);
	                child.setScaleY(mScaleOverFour);
	            }
			} else if (mLastCountX == 5 && mCountX == 4) {
				for (int i = getChildCount() - 1; i >= 0; i--) {
	                View child = getChildAt(i);
	                child.setScaleX(mPreScaleX);
	                child.setScaleY(mPreScaleY);
	            }
			}else if (mCountX == 5) {
				for (int i = getChildCount() - 1; i >= 0; i--) {
	                View child = getChildAt(i);
	                child.setScaleX(mScaleOverFour);
	                child.setScaleY(mScaleOverFour);
	            }
			}
		}
    	mLastCountX = mCountX;
    	
        @SuppressWarnings("all") // suppress dead code warning
        final boolean debug = false;
        if (debug) {
            // Debug drawing for hit space
            Paint p = new Paint();
            p.setColor(0x6600FF00);
            for (int i = getChildCount() - 1; i >= 0; i--) {
                final View child = getChildAt(i);
                final CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();

                canvas.drawRect(lp.x, lp.y, lp.x + lp.width, lp.y + lp.height, p);
            }
        }
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSpecSize, heightSpecSize);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
			measureChild(child);
        }
    }

    public void setupLp(CellLayout.LayoutParams lp) {
        lp.setup(mCellWidth, mCellHeight, mWidthGap, mHeightGap);
    }

    public void measureChild(View child) {
        final int cellWidth = mCellWidth;
        final int cellHeight = mCellHeight;
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();

        lp.setup(cellWidth, cellHeight, mWidthGap, mHeightGap);
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
        int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height,
                MeasureSpec.EXACTLY);
        child.measure(childWidthMeasureSpec, childheightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();

                int childLeft = lp.x;
                int childTop = lp.y;
                child.layout(childLeft, childTop, childLeft + lp.width, childTop + lp.height);

                if (lp.dropped) {
                    lp.dropped = false;

                    final int[] cellXY = mTmpCellXY;
                    getLocationOnScreen(cellXY);
                    mWallpaperManager.sendWallpaperCommand(getWindowToken(),
                            WallpaperManager.COMMAND_DROP,
                            cellXY[0] + childLeft + lp.width / 2,
                            cellXY[1] + childTop + lp.height / 2, 0, null);
                }
            }
        }
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        if (child != null) {
            Rect r = new Rect();
            child.getDrawingRect(r);
            requestRectangleOnScreen(r);
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        // Cancel long press for all children
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.cancelLongPress();
        }
    }

    @Override
    protected void setChildrenDrawingCacheEnabled(boolean enabled) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            view.setDrawingCacheEnabled(enabled);
            // Update the drawing caches
            if (!view.isHardwareAccelerated() && enabled) {
                view.buildDrawingCache(true);
            }
        }
    }

    @Override
    protected void setChildrenDrawnWithCacheEnabled(boolean enabled) {
        super.setChildrenDrawnWithCacheEnabled(enabled);
    }

    // Aurora <jialf> <2013-09-10> add for dock data begin
	public void setCellDimensions(int cellWidth, int cellHeight, int widthGap,
			int heightGap, boolean formHotseat) {
		setCellDimensions(cellWidth, cellHeight, widthGap, heightGap);
		mFromHotseat = formHotseat;
	}
	
    public void measureHotseatChild(View child, int count, int width) {
		if (count <= 0)
			return;
		final int cellWidth = (int) (width / count);
        final int cellHeight = mCellHeight;
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();

        mCellWidth = cellWidth;
        
        lp.setup(cellWidth, cellHeight, mWidthGap, mHeightGap);
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
        int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height,
                MeasureSpec.EXACTLY);
        child.measure(childWidthMeasureSpec, childheightMeasureSpec);
    }
    
    private boolean mFromHotseat;
    // Aurora <jialf> <2013-09-10> add for dock data end
    
    /**
     * get all the folder icon in the page
     * @return return a list of all the folder. return value is not null always.
     */
    public ArrayList<FolderIcon> getAllFodlerIcon() {
    	ArrayList<FolderIcon> folderList = new ArrayList<FolderIcon>();
    	final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if(child instanceof FolderIcon) {
            	folderList.add((FolderIcon)child);
            }
        }
        
        return folderList;
    }
}
