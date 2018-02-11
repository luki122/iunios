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
package com.gionee.mms.ui;

import java.lang.reflect.Field;

import android.content.Context;
import android.util.AttributeSet;
import android.support.v4.view.ViewPager;


public class TabViewPager extends ViewPager {
    public TabViewPager(Context context) {
        super(context);
    }

    public TabViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPrivateField(this, "mOffscreenPageLimit", 0);
    }

    /**
     * ViewPager inherits ViewGroup's default behavior of delayed clicks
     * on its children, but in order to make the dialpad more responsive we
     * disable that here. The Call Log and Favorites tabs are both
     * ListViews which delay their children anyway, as desired to prevent
     * seeing pressed states flashing while scrolling lists
     */
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public void setPrivateField(Object object, String name, Object value) {
        try {
            Class thisClass = ViewPager.class;
            Field thisField = thisClass.getDeclaredField(name);
            thisField.setAccessible(true);
            thisField.set(object, value);
            thisField.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

