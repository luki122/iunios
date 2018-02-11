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

package com.android.contacts.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import aurora.widget.AuroraEditText;

/**
 * A custom text editor that helps automatically dismiss the activity along with the soft
 * keyboard.
 */
public class GnSearchEditText extends AuroraEditText {

    private Drawable mWorkingSpinner;

    public GnSearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mWorkingSpinner = context.getResources().
            getDrawable(com.android.internal.R.drawable.search_spinner);
        setCompoundDrawablesWithIntrinsicBounds(null, null, mWorkingSpinner, null);
        mWorkingSpinner.setVisible(false, false);
    }

    //MTK80736
    public void setLoading(boolean loading) {
        Log.i("SearchEditText", "loading " + loading);
        mWorkingSpinner.setAlpha(loading ? 255 : 0);
        mWorkingSpinner.setVisible(loading, false);
        mWorkingSpinner.invalidateSelf();
    }
}
