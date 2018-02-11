package com.android.keyguard.view;

import com.android.keyguard.utils.LockScreenUtils;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class MyViewPager extends ViewPager{
	
	public MyViewPager(Context context) {
		super(context);
	}
	
	public MyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
    public boolean dispatchTouchEvent(MotionEvent event) {
		int action = event.getAction();
		if (action == MotionEvent.ACTION_MOVE) {
			if (LockScreenUtils.getInstance(getContext()).isSecure()) {
				int count = this.getChildCount();
				Log.d("liugj2", "MyViewPager======dispatchTouchEvent======"+count);
				for (int i = 0; i < count; i++) {
					this.getChildAt(i).dispatchTouchEvent(event);
				}
				return true;
			}
		}
		return super.dispatchTouchEvent(event);
	}
}
