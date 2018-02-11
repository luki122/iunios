/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */
package com.gionee.mms.slide;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.android.mms.R;

public class GnViewSlideHost extends FrameLayout{
    private GnViewSlideIndicator mIndicator;
    private GnViewSlideShower mShower;
    private OnPageChangeListener mPageListener;
    
    public GnViewSlideHost(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViewSlideHost(context);
    }
    
    public GnViewSlideHost(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public GnViewSlideHost(Context context) {
        super(context);
        initViewSlideHost(context);
    }
    
    private void initViewSlideHost( Context context ) {
        setFocusableInTouchMode(true);
    }
    
    public GnViewSlideShower getSlideShower(){
        return mShower;
    }
    
    public GnViewSlideIndicator getSlideIndicator(){
        return mIndicator;
    }
    
    public void setOnPageChangeListener(OnPageChangeListener listener){
        mPageListener = listener;
    }
    
    protected void onSetUp(){
    }
    
    public void setup() {
        mIndicator = (GnViewSlideIndicator) findViewById(R.id.gn_slide_indicator);
        if (mIndicator == null) {
            throw new RuntimeException(
                    "Your GnViewSlideHost must have a GnViewSlideIndicator whose id attribute is 'android.R.id.gn_slide_indicator'");
        }
        
        mShower = (GnViewSlideShower) findViewById(R.id.gn_slide_shower);
        if (mShower == null) {
            throw new RuntimeException(
                    "Your GnViewSlideShower must have a GnViewSideShower whose id attribute is "
                            + "'android.R.id.gn_slide_shower'");
        }
        mShower.setViewSlideListener(new GnViewSlideShower.ViewSlideListener() {
            @Override
            public void onViewSlide(int slideIn, int slideOut) {
                mIndicator.select(slideIn);
                if(mPageListener != null){
                    mPageListener.onPageChangeListener(slideIn, slideOut);
                }
            }

            @Override
            public void onRemove(int rmIndex) {
                if(rmIndex < 0){
                    mIndicator.clear();
                }else{
                    mIndicator.removeIndicator(rmIndex);
                }
            }
        });
        
        onSetUp();
        
        if(!mShower.hasFocus()){
            mShower.requestFocus();
        }
    }
    
    public void selectSlide(int index){
        mShower.setDisplayedChild(index);
        if(!mShower.hasFocus()){
            mShower.requestFocus();
        }
    }
    
    public void addSlidePage(View child, Drawable selected, Drawable unselected){
        mShower.addView(child);
        mIndicator.addIndicator(selected, unselected);
    }
    
    public void addSlidePage(View child){
        addSlidePage(child, null, null);
    }
    
    public static interface OnPageChangeListener{
        public void onPageChangeListener(int newIndex, int oldIndex);
    }
}
