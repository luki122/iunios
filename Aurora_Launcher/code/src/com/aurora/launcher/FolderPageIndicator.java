package com.aurora.launcher;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.aurora.launcher.R;

/**
 * 
 * @author hao jingjing
 * 
 */

@SuppressLint("NewApi")
public class FolderPageIndicator extends LinearLayout {
	private LayoutInflater mInflater;
	private int width;
	private int height;
	private int count = 0;
	private int currentPos = 0;
	private List<View> views = new ArrayList<View>();
	
	private PagedView mPagedView;
	
	public FolderPageIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FolderPageIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		mInflater = LayoutInflater.from(context);

		
		width = getResources().getDimensionPixelSize(
				R.dimen.Page_indicator_width);
		height = getResources().getDimensionPixelSize(
				R.dimen.Page_indicator_height);
	}

	public FolderPageIndicator(Context context) {
		super(context);
	}
	
	public void setCurrentPosition(int pos) {
//		Log.e("HJJ", "FolderPagedView currentPos:" + currentPos + ",pos:" + pos);
		//if(pos == currentPos) return;
		currentPos = pos;

		int size = views.size();
		if (pos < 0 || pos >= size) {
			return;
		}
		for (int i = 0; i < size; i++) {
			View point = views.get(i);
			if (i == pos) {
				point.setActivated(true);
			} else {
				point.setActivated(false);
			}
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		for (int i = 0; i < getChildCount(); i++) {
			final View child = getChildAt(i);
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
			child.measure(
					MeasureSpec.makeMeasureSpec(width, childWidthMode),
					MeasureSpec.makeMeasureSpec(height, childHeightMode));
		}
	}
	
	public void next() {
		setCurrentPosition(currentPos + 1);
	}

	public void previous() {
		setCurrentPosition(currentPos - 1);
	}
	
	public void setCount(int count) {
		this.count = count;
		this.currentPos = mPagedView.getCurrentPage();
	}
	
	public int getDesiredWidth() {
		return views.size()*width;
	}
	
	public int getDesiredHeight() {
		if(count == 0){
			return 0;
		} else {
			return height + getPaddingTop();
		}
	}
	
	public void setPageview(PagedView v) {
		mPagedView = v;
	}
	
	public void initPoints(Context context, int count) {
		this.count = count;
		for (int i = 0; i < count; i++) {
			View view = mInflater.inflate(R.layout.item_indicator, null);
			views.add(view);
			addView(view);
		}
	}
	
	public void updateVisibility(){
		if(views.size() > 1 && getVisibility() != View.VISIBLE){
			setVisibility(View.VISIBLE);
		} 
	}
	
	public void updatePoints(Context context, int childCount) {
		if (views.size() < childCount) {
			for(int i = views.size(); i<childCount; i++){
				View view = mInflater.inflate(R.layout.item_indicator, null);
				views.add(view);
				addView(view);
			}
		} else if(views.size() > childCount){
			for(int i = childCount; i < views.size(); i++){
				View point = views.get(i);
				views.remove(point);
				removeView(point);
			}
		}
		updateVisibility();
	}
}
