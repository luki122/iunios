/**
 * Vulcan created this file in 2014年12月15日 下午4:22:29 .
 */
package com.privacymanage.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * Vulcan created UserGuidePager in 2014年12月15日 .
 * 
 */
public class UserGuidePager extends ViewPager {
	
	boolean mEnableTouchEvent = true;

	/**
	 * @param context
	 */
	public UserGuidePager(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public UserGuidePager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(mEnableTouchEvent) {
			return super.onInterceptTouchEvent(ev);
		}
		return true;
	}
	
	
	public boolean onTouchEvent(MotionEvent ev) {
		if(mEnableTouchEvent) {
			return super.onTouchEvent(ev);
		}
		return true;
	}
	
	public void setFlagEnableTouchEvent(boolean b) {
		mEnableTouchEvent = b;
	}
	
    /**
     * Set the currently selected page.
     *
     * @param item Item index to select
     * @param smoothScroll True to smoothly scroll to the new item, false to transition immediately
     * @param velocity 
     */
    public void setCurrentItem(int item, boolean smoothScroll, int velocity) {
        mPopulatePending = false;
        setCurrentItemInternal(item, smoothScroll, false , velocity);
    }

}
