/**
 * 
 */
package com.aurora.note.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.aurora.note.R;

/**
 * 分享界面底部工具栏容器
 * @author JimXia
 *
 * @date 2015年3月23日 下午4:38:53
 */
public class BottomToolLayout extends FrameLayout {
    
    private static final int MIN_SPACE = 20; // px, 垂直方向行间距
    
	/**
	 * 垂直方向的行间距
	 */
	private int mVerticalSpace;
	
	public BottomToolLayout(Context context) {
		super(context);
	}

	public BottomToolLayout(Context context, AttributeSet attrs) {
	    this(context, attrs, 0);
	}

	public BottomToolLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BottomToolLayout, 0, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i ++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.BottomToolLayout_verticalSpace:
                    mVerticalSpace = a.getDimensionPixelSize(attr, MIN_SPACE);
                    break;
                default:
                    break;
            }
        }
        a.recycle();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int childCount = getChildCount();
		if (childCount > 0) {
		    int visibleChildCount = 0;
		    View view = null;
		    for (int i = 0; i < childCount; i ++) {
		        view = getChildAt(i);
		        if (view.getVisibility() != View.GONE) {
		            visibleChildCount ++;
		        }
		    }
		    
		    if (visibleChildCount == 0) {
		        return;
		    }
		    
		    final int paddingLeft = getPaddingLeft();
            final int paddingTop = getPaddingTop();
            final int availableWidth = getMeasuredWidth() - paddingLeft - getPaddingRight();
            
            final int colCount = 3;
            int rowCount = visibleChildCount / colCount; // 有多少行
            int extra = visibleChildCount % colCount;
            int lastRowColCount; // 最后一行的列数，正常为colCount
            if (extra > 1) {
                lastRowColCount = extra;
                rowCount ++;
            } else {
                if (rowCount == 0) {
                    rowCount = 1;
                    lastRowColCount = extra;
                } else {
                    lastRowColCount = colCount + extra;
                }
            }
            
            // 这里做个特殊处理，如果只有5列，则一行显示
            if (visibleChildCount <= 5) {
                rowCount = 1;
                lastRowColCount = visibleChildCount;
            }
            
            int left;
            int top = paddingTop;
            final int childWidthMeasureSpecNormal = MeasureSpec.makeMeasureSpec(availableWidth / colCount,
                    MeasureSpec.EXACTLY);
            final int childWidthMeasureSpecLastRow = MeasureSpec.makeMeasureSpec(availableWidth / lastRowColCount,
                    MeasureSpec.EXACTLY);
            int measuredWidth;
            int measuredHeight;
            int maxMeasuredHeight;
            
            int index = 0;
            for (int i = 1; i <= rowCount; i ++) {
                int cCount = (i == rowCount) ? lastRowColCount: colCount;
                int childWidthMeasureSpec = (i == rowCount) ? childWidthMeasureSpecLastRow:
                    childWidthMeasureSpecNormal;
                
                left = paddingLeft;
                maxMeasuredHeight = 0;
                
                for (int j = 0; j < cCount; j ++) {
                    do {
                        view = null;
                        if (index < childCount) {
                            view = getChildAt(index ++);
                        }
                    } while (view.getVisibility() == View.GONE);
                    
                    if (view != null) {
                        view.measure(childWidthMeasureSpec,
                                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                        measuredWidth = view.getMeasuredWidth();
                        measuredHeight = view.getMeasuredHeight();
                        maxMeasuredHeight = Math.max(maxMeasuredHeight, measuredHeight);
                        view.layout(left, top, left + measuredWidth, top + measuredHeight);
                        left += measuredWidth;
                    }
                }
                
                top += maxMeasuredHeight + mVerticalSpace;
            }
                        
			int finalMeasuredHeight = top - mVerticalSpace + getPaddingBottom();
			setMeasuredDimension(getMeasuredWidth(), finalMeasuredHeight);
		}
	}
	
	@Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }
}