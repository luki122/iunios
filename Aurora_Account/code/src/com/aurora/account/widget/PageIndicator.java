/**
 * 
 */
package com.aurora.account.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.aurora.account.R;
import com.aurora.utils.DensityUtil;

/**
 * 用来表示ViewPager一共有多少页，当前选中的是哪一页
 * 
 * @author JimXia
 * @date 2014年11月14日 下午3:57:16
 */
public class PageIndicator extends View {
    private Drawable mSelectedIndicatorDrawable;
    private Drawable mUnselectedIndicatorDrawable;
    
    private int mTotalPages = -1;
    private int mCurrentPage = 0;
    
    private static final int PADDING_DIP = 15; //dp
    private int mPadding;
    private final Rect mDrawableBound = new Rect();
    
    private int mSelectedIndicatorDrawableWidth;
    private int mSelectedIndicatorDrawableHeight;
    private int mUnselectedIndicatorDrawableWidth;
    private int mUnselectedIndicatorDrawableHeight;
    
    /**
     * @param context
     */
    public PageIndicator(Context context) {
        super(context);
        init(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public PageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public PageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    private void init(Context context) {
        final Resources res = getResources();
        mSelectedIndicatorDrawable = res.getDrawable(R.drawable.selected_page_indicator);
        mUnselectedIndicatorDrawable = res.getDrawable(R.drawable.unselected_page_indicator);
        
        mPadding = DensityUtil.dip2px(context, PADDING_DIP);
        mSelectedIndicatorDrawableWidth = mSelectedIndicatorDrawable.getIntrinsicWidth();
        mSelectedIndicatorDrawableHeight = mSelectedIndicatorDrawable.getIntrinsicHeight();
        mUnselectedIndicatorDrawableWidth = mUnselectedIndicatorDrawable.getIntrinsicWidth();
        mUnselectedIndicatorDrawableHeight = mUnselectedIndicatorDrawable.getIntrinsicHeight();
    }
    
    /**
     * 设置总页数
     * @param totalPages
     */
    public void setTotalPages(int totalPages) {
        if (totalPages <= 0) {
            throw new IllegalArgumentException("total pages must be greater than 0");
        }
        mTotalPages = totalPages;
        invalidate();
    }
    
    /**
     * 设置当前页的索引
     * @param currentPage
     */
    public void setCurrentPage(int currentPage) {
        if (currentPage < 0) {
            throw new IllegalArgumentException("current page must be greater or equal than 0");
        }
        
        if (currentPage >= mTotalPages) {
            throw new IllegalArgumentException("current page must be less than total pages");
        }
        
        mCurrentPage = currentPage;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTotalPages > 0) {
            int width = (mTotalPages - 1) * mPadding + (mTotalPages - 1) * mUnselectedIndicatorDrawableWidth +
                    mSelectedIndicatorDrawableWidth + getPaddingStart() + getPaddingEnd();
            int height = Math.max(mSelectedIndicatorDrawableHeight, mUnselectedIndicatorDrawableHeight) +
                    getPaddingTop() + getPaddingBottom();
            setMeasuredDimension(width, height);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (mTotalPages > 0) {
            final int width = getWidth();
            final int height = getHeight();
            
            Rect bound = mDrawableBound;
            int left = getPaddingStart();
            int selectedIndicatorTop = (height - mSelectedIndicatorDrawableHeight) / 2;
            int unselectedIndicatorTop = (height - mUnselectedIndicatorDrawableHeight) / 2;
            Drawable drawable;
            for (int i = 0; i < mTotalPages && left < width; i ++) {
                if (i == mCurrentPage) {
                    bound.set(left, selectedIndicatorTop, left + mSelectedIndicatorDrawableWidth,
                            selectedIndicatorTop + mSelectedIndicatorDrawableHeight);
                    left = left + mSelectedIndicatorDrawableWidth + mPadding;
                    drawable = mSelectedIndicatorDrawable;
                } else {
                    bound.set(left, unselectedIndicatorTop, left + mUnselectedIndicatorDrawableWidth,
                            unselectedIndicatorTop + mUnselectedIndicatorDrawableHeight);
                    left = left + mUnselectedIndicatorDrawableWidth + mPadding;
                    drawable = mUnselectedIndicatorDrawable;
                }
                drawable.setBounds(bound);
                drawable.draw(canvas);
            }
        }
    }    
}
