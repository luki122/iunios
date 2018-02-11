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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import aurora.widget.AuroraEditText;

import com.android.mms.R;
/**
 * A custom text editor that helps automatically dismiss the activity along with the soft
 * keyboard.
 */
public class SearchEditText extends AuroraEditText {

    private Drawable mWorkingSpinner;

    public SearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mWorkingSpinner = context.getResources().getDrawable(-1/*R.drawable.search_spinner*/);
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
