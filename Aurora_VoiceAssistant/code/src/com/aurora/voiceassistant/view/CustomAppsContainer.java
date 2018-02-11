package com.aurora.voiceassistant.view;

import java.text.AttributedCharacterIterator.Attribute;

import android.R.integer;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.aurora.voiceassistant.*;

public class CustomAppsContainer extends ViewGroup {
	private int mCellCountX;
	private int mCellWidth;
	private int mCellHeight;
	private int mRightPadding;
	private int mTopPadding;
	private int childCount;
	
	public CustomAppsContainer(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CustomAppsContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
		childCount = getChildCount();
        
        mCellCountX = (int) getResources().getInteger(R.integer.vs_offline_apps_count_x);
        mCellWidth = (int) getResources().getDimension(R.dimen.vs_offline_apps_cell_width);
        mCellHeight = (int) getResources().getDimension(R.dimen.vs_offline_apps_cell_height);
        mTopPadding = (int) getResources().getDimension(R.dimen.vs_offline_apps_topPadding);
        mRightPadding = (int) getResources().getDimension(R.dimen.vs_offline_apps_rightPadding);

		setMeasuredDimension(MeasureSpec.makeMeasureSpec(mCellWidth*mCellCountX + mRightPadding*(mCellCountX - 1), MeasureSpec.EXACTLY),
							 MeasureSpec.makeMeasureSpec(mCellHeight*(childCount%mCellCountX == 0? childCount/mCellCountX : (childCount/mCellCountX + 1)) + 
									 					 childCount/mCellCountX*mTopPadding, MeasureSpec.EXACTLY));
		
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);
			/*final LayoutParams lp = (LayoutParams) child.getLayoutParams();
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
			}*/
			
			child.measure(MeasureSpec.makeMeasureSpec(mCellWidth, MeasureSpec.EXACTLY),
						  MeasureSpec.makeMeasureSpec(mCellHeight, MeasureSpec.EXACTLY));
		}
	}

	@Override
	protected void onLayout(boolean flag, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int left = 0;
		int top = 0;
		int row = 0;
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			int width = child.getMeasuredWidth();
			int height = child.getMeasuredHeight();
			
			row = i/mCellCountX;
			if (i%mCellCountX == 0) {
				left = 0;
				top = (mTopPadding + height)*row;
			}
			child.layout(left, top, width + left, height + top);
			left = left + width + mRightPadding;
		}
	}

}
