/*
 * Copyright (C) 2011 The Android Open Source Project
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
import android.R.integer;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.aurora.launcher.CellLayout.LayoutParams;

public class Hotseat extends FrameLayout {
    @SuppressWarnings("unused")
    private static final String TAG = "Hotseat";

    private Launcher mLauncher;
    private CellLayout mContent;

    private int mCellCountX;
    private int mCellCountY;
    private int mAllAppsButtonRank;

    private boolean mTransposeLayoutWithOrientation;
    private boolean mIsLandscape;
    
    public static final int TRANSLATION_Y =59;
    
    //AURORA-START::App Index::Shi guiqiang::20140111
    private Scroller mScroller;
	//AURORA-END::App Index::Shi guiqiang::20140111

    public Hotseat(Context context) {
        this(context, null);
    }

    public Hotseat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Hotseat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Hotseat, defStyle, 0);
        Resources r = context.getResources();
        mCellCountX = a.getInt(R.styleable.Hotseat_cellCountX, -1);
        mCellCountY = a.getInt(R.styleable.Hotseat_cellCountY, -1);
        mAllAppsButtonRank = r.getInteger(R.integer.hotseat_all_apps_index);
        mTransposeLayoutWithOrientation = 
                r.getBoolean(R.bool.hotseat_transpose_layout_with_orientation);
        mIsLandscape = context.getResources().getConfiguration().orientation ==
            Configuration.ORIENTATION_LANDSCAPE;
        //AURORA-START::App Index::Shi guiqiang::20140111
        mScroller = new Scroller(getContext(),new ScrollInterpolator());
		//AURORA-END::App Index::Shi guiqiang::20140111
    }

    public void setup(Launcher launcher) {
        mLauncher = launcher;
        // Aurora <jialf> <2013-10-31> remove for fix bug #323 begin
        // setOnKeyListener(new HotseatIconKeyEventListener());
        // Aurora <jialf> <2013-10-31> remove for fix bug #323 end
    }

    CellLayout getLayout() {
        return mContent;
    }
  
    private boolean hasVerticalHotseat() {
        return (mIsLandscape && mTransposeLayoutWithOrientation);
    }

    /* Get the orientation invariant order of the item in the hotseat for persistence. */
    int getOrderInHotseat(int x, int y) {
        return hasVerticalHotseat() ? (mContent.getCountY() - y - 1) : x;
    }
    /* Get the orientation specific coordinates given an invariant order in the hotseat. */
    int getCellXFromOrder(int rank) {
        return hasVerticalHotseat() ? 0 : rank;
    }
    int getCellYFromOrder(int rank) {
        return hasVerticalHotseat() ? (mContent.getCountY() - (rank + 1)) : 0;
    }
    public boolean isAllAppsButtonRank(int rank) {
        return rank == mAllAppsButtonRank;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mCellCountX < 0) mCellCountX = LauncherModel.getCellCountX();
        if (mCellCountY < 0) mCellCountY = LauncherModel.getCellCountY();
        mContent = (CellLayout) findViewById(R.id.layout);
        mContent.setGridSize(mCellCountX, mCellCountY);
        mContent.setIsHotseat(true);

        // Aurora <jialf> <2013-09-10> remove for dock data begin
        // resetLayout();
        // Aurora <jialf> <2013-09-10> remove for dock data end
    }

	// Aurora <jialf> <2013-10-08> add for Dock data begin
	public void calculateAuroraWidth() {
		int screenWith = mLauncher.getmScreenWidth()
				- mContent.getPaddingLeft() - mContent.getPaddingRight();
		mCellCountX = LauncherModel.getmHotseatChildCount();
		int cellWidth;
		if (mCellCountX != 0) {
			cellWidth = screenWith / mCellCountX;
		} else {
			cellWidth = screenWith;
		}
		mContent.setAuroraGridWidth(cellWidth, mCellCountX);
	}
	// Aurora <jialf> <2013-10-08> add for Dock data end
    
    void resetLayout() {
        mContent.removeAllViewsInLayout();

        // Add the Apps button
        Context context = getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        BubbleTextView allAppsButton = (BubbleTextView)
                inflater.inflate(R.layout.application, mContent, false);
        allAppsButton.setCompoundDrawablesWithIntrinsicBounds(null,
                context.getResources().getDrawable(R.drawable.all_apps_button_icon), null, null);
        allAppsButton.setContentDescription(context.getString(R.string.all_apps_button_label));
        allAppsButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mLauncher != null &&
                    (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
                    mLauncher.onTouchDownAllAppsButton(v);
                }
                return false;
            }
        });

        allAppsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                if (mLauncher != null) {
                    mLauncher.onClickAllAppsButton(v);
                }
            }
        });

        // Note: We do this to ensure that the hotseat is always laid out in the orientation of
        // the hotseat in order regardless of which orientation they were added
        int x = getCellXFromOrder(mAllAppsButtonRank);
        int y = getCellYFromOrder(mAllAppsButtonRank);
        CellLayout.LayoutParams lp = new CellLayout.LayoutParams(x,y,1,1);
        lp.canReorder = false;
        mContent.addViewToCellLayout(allAppsButton, -1, 0, lp, true);
    }
    
    //Aurora-start:xiejun
    private int mChildOffset;
    private int mChildRelativeOffset;
    private int mChildOffsetWithLayoutScale;
    protected float mLayoutScale = 1.0f;
    private int mMinimumWidth;
    public void scale(float fact,boolean scale){
    	setLayoutScale(fact);
		if(scale){
			getLayout().setScaleX(0.97f);
			getLayout().setScaleY(0.97f);
			getLayout().setTranslationY(getContext().getResources().getInteger(R.integer.hotseat_translation_y));
			setChildTextVisable(false);
		}else{
			getLayout().setScaleX(1.0f);
			getLayout().setScaleY(1.0f);
			getLayout().setTranslationY(0);
			setChildTextVisable(true);
		}

	}
    public void setLayoutScale(float childrenScale) {
        mLayoutScale = childrenScale;
        invalidateCachedOffsets();
        // Now we need to do a re-layout, but preserving absolute X and Y coordinates
        float childrenX= getLayout().getX();
        float childrenY= getLayout().getY();
        
        // Trigger a full re-layout (never just call onLayout directly!)
        int widthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
        requestLayout();
        measure(widthSpec, heightSpec);
        layout(getLeft(), getTop(), getRight(), getBottom());
        getLayout().setX(childrenX);
        getLayout().setY(childrenY);
    }
    protected void invalidateCachedOffsets() {
         mChildOffset = -1;
         mChildRelativeOffset = -1;
         mChildOffsetWithLayoutScale = -1;
    }
    public void setChildTextVisable(boolean visable){
    	//hotseat5 remove text
    	visable = false;
    	int childCount = mContent.getShortcutsAndWidgets().getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = mContent.getShortcutsAndWidgets().getChildAt(i);
			if(child instanceof BubbleTextView){
				if(visable){
					((BubbleTextView) child).setTextColor(getResources()
							.getColor(R.color.workspace_icon_text_color));
		    	}else{
		    		((BubbleTextView) child).setTextColor(getResources()
							.getColor(android.R.color.transparent));
		    	}
			}else if(child instanceof FolderIcon){
				((FolderIcon) child).setTextVisible(visable);
			}
		}
    }
    //Aurora:xiejun:Animator for hotseat start
	Animator getAnimator(float factor, boolean scale, boolean animated,
			boolean isAddwieget, int duration, Runnable callback) {
		AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet()
				: null;
		int trancationY = isAddwieget ? (scale ? (getHeight()+170) : 0)
				: (scale ? getContext().getResources().getInteger(R.integer.hotseat_translation_y) : 0);
		float alpha = isAddwieget ? (scale ? 0.0f : 1.0f) : 1.0f;
		boolean itemTittleVisable = scale ? false : true;
		if (animated) {
			LauncherViewPropertyAnimator a = new LauncherViewPropertyAnimator(
					getLayout());
			a.setDuration(duration);
			a.scaleX(factor);
			a.scaleY(factor);
			a.translationY(trancationY);
			a.alpha(alpha);
			getLayout().setPivotX(getLayout().getWidth()/2);
			getLayout().setPivotY(getLayout().getHeight()/2);
			anim.play(a);
			setChildTextVisable(itemTittleVisable);
		} else {
			getLayout().setScaleX(factor);
			getLayout().setScaleY(factor);
			getLayout().setAlpha(alpha);
			getLayout().setTranslationY(trancationY);
			setChildTextVisable(itemTittleVisable);
		}
		return anim;
	}
    //Aurora:xiejun:Animator for hotseat end
    /*
    protected int getChildOffset(int index) {
        int childOffset = Float.compare(mLayoutScale, 1f) == 0 ?
                mChildOffset : mChildOffsetWithLayoutScale;
        
        if(mChildOffset!=-1){
        	return mChildOffset;
        }else{
        	int offset = getRelativeChildOffset(0);
        	offset += getScaledMeasuredWidth(getChildAt(index));
        	 return offset;
        }
    }
    protected int getScaledMeasuredWidth(View child) {
        final int measuredWidth = child.getMeasuredWidth();
        final int minWidth = mMinimumWidth;
        final int maxWidth = (minWidth > measuredWidth) ? minWidth : measuredWidth;
        return (int) (maxWidth * mLayoutScale + 0.5f);
    }
    protected int getChildWidth(int index) {
        final int measuredWidth = getChildAt(index).getMeasuredWidth();
        final int minWidth = mMinimumWidth;
        return (minWidth > measuredWidth) ? minWidth : measuredWidth;
    }
    protected int getRelativeChildOffset(int index) {
        if (mChildRelativeOffset != -1) {
            return mChildRelativeOffset;
        } else {
            final int padding = getPaddingLeft() + getPaddingRight();
            final int offset = getPaddingLeft() +
                    (getMeasuredWidth() - padding - getChildWidth(index)) / 2;
            mChildRelativeOffset=offset;
            return offset;
        }
    }
    */
    //Aurora-start:end

    // Aurora <jialf> <2013-09-10> remove for dock data begin
	void resetAuroraLayout() {
		mContent.removeAllViewsInLayout();
	}
    // Aurora <jialf> <2013-09-10> remove for dock data end
	
	//AURORA-START::App Index::Shi guiqiang::20140111
	public Scroller getScroller() {
		return mScroller;
	}
	
	@Override
    public void scrollTo(int x, int y) {
//		setTranslationX(x);
		super.scrollTo(x, y);
    }
	
	@Override
    public void computeScroll() {
        computeScrollHelper();
    }
	
    protected boolean computeScrollHelper() {
        if (mScroller.computeScrollOffset()) {
 //    	Log.d("DEBUG", "the getScrollX is "+getScrollX()+" the getCurrX is "+mScroller.getCurrX());
            if (getScrollX() != mScroller.getCurrX()) {
            	scrollTo(mScroller.getCurrX(), 0);
            }
            invalidate();
            return true;
        }
//    Log.d("DEBUG", "@@@@@@@@@@@@the TranslationX = "+getTranslationX());
        return false;
    }
	//AURORA-END::App Index::Shi guiqiang::20140111
    private static class ScrollInterpolator implements Interpolator {
        public ScrollInterpolator() {
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return t*t*t*t*t + 1 ;
        }
    }
}
