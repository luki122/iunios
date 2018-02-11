package com.aurora.change.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CropViewPager extends ViewPager {

    private boolean isCanScroll = true;

    public CropViewPager(Context context) {
        super(context);
    }

    public CropViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        return false;
    }
    
}
