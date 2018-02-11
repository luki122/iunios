
//Aurora <SQF> <2014-6-4>  add this file

package com.android.gallery3d.filtershow.category;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;

public class CustomHorizontalScrollView extends HorizontalScrollView{
	public CustomHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View v = getChildAt(i);
			if(v instanceof CategoryTrack) {
				getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
			}
		}
	}
	
	
}
