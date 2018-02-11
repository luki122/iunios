package com.secure.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoSlitherViewPager extends ViewPager {
    public NoSlitherViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoSlitherViewPager(Context context) {
        super(context);
    }   
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {    	            
        return false;
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) { 
    	return false;
    }
}
