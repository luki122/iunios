package com.aurora.view;

import com.aurora.launcher.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

public class DefMarginImageView extends ImageView{
	private int mMarginRight = 0;
	public DefMarginImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mMarginRight = context.getResources().getDimensionPixelOffset(R.dimen.quick_index_shadow_image_margin_right);
	}

	public void resetMarginValue(boolean fill){
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)getLayoutParams();
		if(fill) {
			params.rightMargin = 0;
		} else {
			params.rightMargin = mMarginRight;
		}
	}
}
