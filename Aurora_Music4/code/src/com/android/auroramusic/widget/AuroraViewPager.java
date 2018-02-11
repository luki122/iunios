package com.android.auroramusic.widget;



import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;



public class AuroraViewPager extends ViewPager {

	private boolean mbNotScrolled = false;
	private GestureDetector mDetector = null;
	private static final int GESTURE_MIN_DISTANCE = 20;   
	private static final int GESTURE_MIN_VELOCITY = 20;
	
	
	public AuroraViewPager(Context context) {
		this(context, null);
	}

	public AuroraViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		mbNotScrolled = false;
		mDetector = new GestureDetector(context, new AuroraMusicGesture());
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		
		try {
			if (mbNotScrolled) {
				return mDetector.onTouchEvent(event);
			}
			boolean result = super.onInterceptTouchEvent(event);
		
			return result;
		} catch (Exception e) {
		}
		
		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean result = super.dispatchTouchEvent(ev);
		
		return result;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (mbNotScrolled) {
			return false;
		}
		return super.onTouchEvent(event);
	}

	public void setViewPageOnScrolled(boolean bscroll) {
		
		this.mbNotScrolled = bscroll;
		return;
	}
	
	private class AuroraMusicGesture extends GestureDetector.SimpleOnGestureListener{

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
			if (e1 != null && e2 != null) {
				if (Math.abs(e1.getX()-e2.getX()) > GESTURE_MIN_DISTANCE && Math.abs(velocityX) > GESTURE_MIN_VELOCITY) {
					return true;
				}
			}
			return false;
		}
		
	}
	
}
