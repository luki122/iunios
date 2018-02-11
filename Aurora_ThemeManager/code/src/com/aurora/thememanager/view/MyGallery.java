package com.aurora.thememanager.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

public class MyGallery extends Gallery {
	private FlingView mFlingView;
	public static int FLING_TIME = 5000;

	public void setmSettingsAdvert(FlingView mFlingView) {
		this.mFlingView = mFlingView;
	}

	private int flingLength = getResources().getDisplayMetrics().widthPixels;
	public static final int LEFT = 0;
	public static final int RIGHT = 1;

	public MyGallery(Context context) {
		super(context);
	}

	public MyGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MyGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mFlingView.setFling(event.getAction() == MotionEvent.ACTION_UP);
		return super.onTouchEvent(event);
	}
	
	public static interface FlingView{
		public  void setFling(boolean fling);
	}

	public void slide(int direction) {
		if (direction == RIGHT)
			this.onFling(null, null, -flingLength, 0);
		else {
			this.onFling(null, null, flingLength, 0);
		}
	}

	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
		return e2.getX() > e1.getX();
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		int kEvent;
		if (e1 != null && e2 != null && isScrollingLeft(e1, e2)) {
			// Check if scrolling left
			kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
		} else {
			// Otherwise scrolling right
			kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
		}
		onKeyDown(kEvent, null);
		return true;

	}
}