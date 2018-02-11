package com.aurora.note.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * @author lWX192606 2014-2-26 
 * TODO 自定义ViewPager , 解决滑动冲突
 */
public class PicViewPager extends ViewPager {

	private static final String TAG = "PicViewPager";

	public PicViewPager(Context context) {
		super(context);
	}

	public PicViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		try {
			return super.onInterceptTouchEvent(ev);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "hacky viewpager error1");
			return false;
		} catch (ArrayIndexOutOfBoundsException e) {
			Log.e(TAG, "hacky viewpager error2");
			return false;
		}
	}
}