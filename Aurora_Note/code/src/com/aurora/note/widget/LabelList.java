/**
 * 
 */
package com.aurora.note.widget;

import java.util.LinkedList;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.FrameLayout;

import com.aurora.note.R;

/**
 * 标签列表
 * 
 * @author JimXia
 * @date 2014-4-10 下午3:59:20
 */
public class LabelList extends FrameLayout {
    
    private static final int MIN_SPACE = 20; // px, 水平方向两个view之间最小间隔距离
    
    /**
     * 两个标签之间的水平间距
     */
    private int mHorizontalSpace;
    
	/**
	 * 垂直方向的间距
	 */
	private int mVerticalSpace;
	
	private Adapter mAdapter;
	private final DataSetObserver mDataSetObserver = new LabelDataSetObserver();
	private LinkedList<View> mRecycleBin = new LinkedList<View>();
	
	public LabelList(Context context) {
		super(context);
	}

	public LabelList(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LabelList, 0, 0);
		int n = a.getIndexCount();
		for (int i = 0; i < n; i ++) {
			int attr = a.getIndex(i);
			switch (attr) {
			    case R.styleable.LabelList_horizontalSpace:
                    mHorizontalSpace = a.getDimensionPixelSize(attr, MIN_SPACE);
                    break;
				case R.styleable.LabelList_verticalSpace:
					mVerticalSpace = a.getDimensionPixelSize(attr, MIN_SPACE);
					break;
				default:
					break;
			}
		}
		a.recycle();
	}

	public LabelList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setAdapter(Adapter adapter) {
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}
		mAdapter = adapter;
		if (adapter != null) {
			adapter.registerDataSetObserver(mDataSetObserver);
		}
		refreshView();
	}
	
	/**
	 * 设置水平方向的间距
	 */
	public void setHorizontalSpace(int horizontalSpace) {
	    if (horizontalSpace < 0) {
	        throw new RuntimeException("Horizontal space must greater or equal than zero.");
	    }
	    mHorizontalSpace = horizontalSpace;
	    refreshView();
	}
	
	/**
	 * 设置垂直方向的间距
	 * @param verticalSpace
	 */
	public void setVerticalSpace(int verticalSpace) {
		if (verticalSpace < 0) {
			throw new RuntimeException("Vertical space must greater or equal than zero.");
		}
		mVerticalSpace = verticalSpace;
		refreshView();
	}
	
	private void refreshView() {
		recycleAllChildViews();
		removeAllViews();
		setupView();
		requestLayout();
	}
	
	/**
	 * 回收所有的子视图对象
	 */
	private void recycleAllChildViews() {
		final int childViewCount = getChildCount();
		final LinkedList<View> recycleBin = mRecycleBin;
		for (int i = 0; i < childViewCount; i ++) {
			recycleBin.add(getChildAt(i));
		}
	}
	
	/**
	 * 根据Adapter重新构造所有的子视图
	 */
	private void setupView() {
		final LinkedList<View> recycleBin = mRecycleBin;
		final Adapter adapter = mAdapter;
		final int labelCount = adapter.getCount();
		for (int i = 0; i < labelCount; i ++) {
			View convertView = null;
			if (!recycleBin.isEmpty()) {
				convertView = recycleBin.removeFirst();
			}
			addView(adapter.getView(i, convertView, this));
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int childCount = getChildCount();
		if (childCount > 0) {
			int widthMeasureMode = MeasureSpec.getMode(widthMeasureSpec);
			int width = 0;
			switch (widthMeasureMode) {
				case MeasureSpec.EXACTLY:
				case MeasureSpec.AT_MOST:
					width = MeasureSpec.getSize(widthMeasureSpec);
					if (width <= 0) {
						width = getScreenWidth();
					}
					break;
				default:
					width = getScreenWidth();
					break;
			}
			
			final int paddingLeft = getPaddingLeft();
			final int paddingTop = getPaddingTop();
			final int availableWidth = width - paddingLeft - getPaddingRight();
			
			int rowCount = 0; // 统计有多少行
			final int rowHeight = getChildAt(0).getMeasuredHeight(); // 每一行的行高
			final int vSpace = mVerticalSpace; // 垂直方向的间距
			
			int colCount = 0; // 统计某一行有多少列
			int colStartIndex = -1; // 某一行的第一列的视图的索引
			int hSpace = mHorizontalSpace; // 两个标签之间的水平间距
			
			int temp = 0; // 统计某一行的子视图的宽度和
			int index = 0; // 视图索引
			while (index < childCount) {
				colCount = 0;
				temp = 0;
				colStartIndex = -1;
				
				while (index < childCount && temp < availableWidth) {
					View view = getChildAt(index ++);
					if (view.getVisibility() == View.GONE) {
						continue;
					}
					if (colStartIndex == -1) {
						colStartIndex = index - 1; // 因为上边index每次用完之后都自增了
					}
					temp += view.getMeasuredWidth() + hSpace;
					colCount ++;
				}
				
				if (colCount > 1 && (temp - availableWidth > hSpace)) {
					// 摆放colCount个子视图之后，宽度超过了标签列表的宽度，减少一个
					colCount --;
					index --;
				}
				
				int left = paddingLeft;
				int top = paddingTop + rowCount * (rowHeight + vSpace);
				int right = 0;
				for (int i = colStartIndex; i < index; i ++) {
					View view = getChildAt(i);
					if (view.getVisibility() != View.GONE) {
						right = left + view.getMeasuredWidth();
						view.layout(left, top, right, top + view.getMeasuredHeight());
						left = right + hSpace;
					}
				}
				rowCount ++;
			}
			
			int measuredHeight = paddingTop + rowCount * rowHeight + (rowCount - 1) * vSpace + getPaddingBottom();
			setMeasuredDimension(getMeasuredWidth(), measuredHeight);
		}
	}
	
	private int getScreenWidth() {
		return getResources().getDisplayMetrics().widthPixels;
	}

	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
	}
	
	private class LabelDataSetObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			refreshView();
		}

		@Override
		public void onInvalidated() {
			refreshView();
		}		
	}
}