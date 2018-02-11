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
package com.android.mms.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class LinearLayoutFilter extends LinearLayout {

    FilterListener listener;

    public void setFilter(FilterListener fl) {
        listener = fl;
    }

    public LinearLayoutFilter(Context arg0) {
        super(arg0);
    }

    public LinearLayoutFilter(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(listener != null) {
            if(true == listener.onFilterLayout(changed, left, top, right, bottom)) {
                return;
            }
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int measureSpecWidth, int measureSpecHeight) {
       if(listener != null) {
           if(true == listener.onFilterMeasure(measureSpecWidth, measureSpecHeight)) {
               super.setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
               return;
           }
       }
       super.onMeasure(measureSpecWidth, measureSpecHeight);
    }

    public static interface FilterListener{
        public boolean onFilterMeasure(int measureSpecWidth,  int measureSpecHeight);
        public boolean onFilterLayout(boolean changed, int left, int top, int right, int bottom);
    }
}