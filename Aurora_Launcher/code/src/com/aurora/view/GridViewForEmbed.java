package com.aurora.view;

import com.aurora.launcher.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.GridView;

public class GridViewForEmbed extends GridView { 
	private int hideHeaderPaddingLeft; // px
	private int hideHeaderPaddingRight; // px
	private int hideHeaderHorizontalSpacing; // px
	private int normalHeaderPaddingLeft; // px
	private int normalHeaderPaddingRight; // px

    public GridViewForEmbed(Context context, AttributeSet attrs) { 
        super(context, attrs); 
        normalHeaderPaddingLeft = getResources().getDimensionPixelOffset(R.dimen.quick_index_grid_normal_padding_left);
        normalHeaderPaddingRight = getResources().getDimensionPixelOffset(R.dimen.quick_index_grid_normal_padding_right);
        hideHeaderPaddingLeft = getResources().getDimensionPixelOffset(R.dimen.quick_index_grid_hide_padding_left);
        hideHeaderPaddingRight = getResources().getDimensionPixelOffset(R.dimen.quick_index_grid_hide_padding_right);
        hideHeaderHorizontalSpacing = getResources().getDimensionPixelOffset(R.dimen.quick_index_grid_hide_horizontal_spacing);
        /**add it by Hazel*/
        setHapticFeedbackEnabled(true);
    } 

    public GridViewForEmbed(Context context) { 
        super(context); 
    } 

    public GridViewForEmbed(Context context, AttributeSet attrs, int defStyle) { 
        super(context, attrs, defStyle); 
    } 

    public void adjustSpaceForSearch(boolean hideHeader) {
    	if(hideHeader) {
    		if(getPaddingLeft() != hideHeaderPaddingLeft){
    			// Log.e("HJJ", "padding adjust is called for hide, getPaddingLeft:" + getPaddingLeft());
    			setPadding(hideHeaderPaddingLeft, 0, hideHeaderPaddingRight, 0);
    			setHorizontalSpacing(hideHeaderHorizontalSpacing);
    		}
    	} else {
    		if(getPaddingLeft() != normalHeaderPaddingLeft){
    			// Log.e("HJJ", "padding adjust is called for normal, getPaddingLeft:" + getPaddingLeft());
    			setPadding(normalHeaderPaddingLeft, 0, normalHeaderPaddingRight, 0);
    			setHorizontalSpacing(0);
    		}
    	}
    }
    
    @Override 
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { 

        int expandSpec = MeasureSpec.makeMeasureSpec( 
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST); 
        super.onMeasure(widthMeasureSpec, expandSpec); 
    } 
}
