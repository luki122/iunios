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

import java.util.ArrayList;

import com.android.mms.MmsApp;
import com.android.mms.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class GnViewSlideIndicator extends LinearLayout{

    int mIndex = 0;
    ArrayList<Drawable> mDrawableList = new ArrayList<Drawable>(3);
    Drawable mDefaultSelected;
    Drawable mDefaultUnselected;
    
    public GnViewSlideIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a =
            context.obtainStyledAttributes(attrs, R.styleable.GnViewSlideIndicator,
                    0, 0);
        mDefaultSelected = a.getDrawable(R.styleable.GnViewSlideIndicator_gnSelected);
        mDefaultUnselected = a.getDrawable(R.styleable.GnViewSlideIndicator_gnUnselected);
        
        final Resources resources = context.getResources();
        /*if(null == mDefaultSelected){
            if (MmsApp.mDarkStyle) {
                mDefaultSelected = resources.getDrawable(R.drawable.gn_slideindicator_selected_dark);
            } else {
                mDefaultSelected = resources.getDrawable(R.drawable.gn_slideindicator_selected);
            }
        }
        if(null == mDefaultUnselected){
            //gionee gaoj 2012-6-23 modified for CR00626407 start
            if (MmsApp.mDarkStyle) {
                mDefaultUnselected = resources.getDrawable(R.drawable.gn_slideindicator_unselected);
            } else if (MmsApp.mLightTheme) {
                mDefaultUnselected = resources.getDrawable(R.drawable.gn_slideindicator_dark_unselected);
            }
            //gionee gaoj 2012-6-23 modified for CR00626407 end
        }*/
        a.recycle();
    }

    public GnViewSlideIndicator(Context context) {
        super(context);
    }
    
    public int getSelectedIndex(){
        return mIndex;
    }
    
    public void select(int index){
        if(index != mIndex){
            swapDrawable(index);
            swapDrawable(mIndex);
            mIndex = index;
        }
    }
    
    public void clear(){
        mDrawableList.clear();
        removeAllViews();
        mIndex = 0;
    }
    
    public void removeIndicator(int index){
        removeViewAt(index);
        mDrawableList.remove(index);
    }
    
    public void addIndicator(Drawable selected, Drawable unselected){
        if(null == selected){
            selected = mDefaultSelected;
        }
        if(null == unselected){
            unselected = mDefaultUnselected;
        }
        
        ImageView v = new ImageView(getContext());
        Drawable willInList;
        if(mIndex == getChildCount()){
            v.setImageDrawable(selected);
            willInList = unselected;
        }else{
            v.setImageDrawable(unselected);
            willInList = selected;
        }
        mDrawableList.add(willInList);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                );
        addView(v, lp);
    }
    
    private void swapDrawable(int index) {
        ImageView child = (ImageView)getChildAt(index);
        Drawable willInView = mDrawableList.get(index);
        Drawable willInList = child.getDrawable();
        mDrawableList.remove(index);
        mDrawableList.add(index, willInList);
        child.setImageDrawable(willInView);
    }
    
}
