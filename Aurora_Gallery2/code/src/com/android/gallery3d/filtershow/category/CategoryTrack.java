/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.gallery3d.filtershow.category;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import com.android.gallery3d.R;

public class CategoryTrack extends LinearLayout {

    //Aurora <SQF> <2014-08-30>  for NEW_UI begin
    //ORIGINALLY:
	//private CategoryAdapter mAdapter;
    //SQF MODIFIED TO:
	private AuroraCategoryAdapter mAdapter;
    //Aurora <SQF> <2014-08-30>  for NEW_UI end
    
    private int mElemSize;
    private View mSelectedView;
    private float mStartTouchY;
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            if (getChildCount() != mAdapter.getCount()) {
                fillContent();
            } else {
                invalidate();
            }
        }
        @Override
        public void onInvalidated() {
            super.onInvalidated();
            fillContent();
        }
    };

    public CategoryTrack(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CategoryTrack);
        //Aurora <SQF> <2014-08-30>  for NEW_UI begin
        //ORIGINALLY:
        //mElemSize = a.getDimensionPixelSize(R.styleable.CategoryTrack_iconSize, 0);
        //SQF MODIFIED TO:
        
        //Aurora <SQF> <2014-08-30>  for NEW_UI end
        
    }
    //Aurora <SQF> <2014-08-30>  for NEW_UI begin
    //ORIGINALLY:
    /*
    public void setAdapter(CategoryAdapter adapter) {
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mDataSetObserver);
        fillContent();
    }
    */
    //SQF MODIFIED TO:
    public void setAdapter(AuroraCategoryAdapter adapter) {
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mDataSetObserver);
        fillContent();
    }
    //Aurora <SQF> <2014-08-30>  for NEW_UI end
    

    public void fillContent() {
    	/*
        removeAllViews();
        mAdapter.setItemWidth(mElemSize);
        mAdapter.setItemHeight(LayoutParams.MATCH_PARENT);
        int n = mAdapter.getCount();
        for (int i = 0; i < n; i++) {
            View view = mAdapter.getView(i, null, this);
            addView(view, i);
        }
        */
        requestLayout();
    }

    @Override
    public void invalidate() {
        for (int i = 0; i < this.getChildCount(); i++) {
            View child = getChildAt(i);
            child.invalidate();
        }
    }

    
    //Aurora <SQF> <2014-6-4>  for NEW_UI begin
    /*
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
		int childLeft = 0;
		final int childCount = getChildCount();
		final int childWidth = getMeasuredWidth() / childCount;
		Log.i("SQF_LOG", "CategoryTrack::onLayout---> changed:" + changed + " [l, t, r, b]: (" + l + ", " + t + ", " + r + ", " + b + ")" + " childCount:" + childCount);
		for (int i = 0; i < childCount; i++) {
			final View childView = getChildAt(i);
			if (childView.getVisibility() != View.GONE) {
				childLeft = i * childWidth;
				childView.layout(childLeft, 0, childLeft + childWidth, childView.getMeasuredHeight());
				Log.i("SQF_LOG", "CategoryTrack::onLayout l, t, r, b--> " + childLeft + " " + 0 + " " + (childLeft + childWidth) + " " + childView.getMeasuredHeight());
			}
		}
		
	}
	*/
	//Aurora <SQF> <2014-6-4>  for NEW_UI end
}
