package com.gionee.mms.ui;

import com.android.mms.MmsApp;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class GnTextView extends TextView{
    int mLargeTextType = MmsApp.mFontSizeType;
    
    public GnTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGnTextSize();
    }

    public GnTextView(Context context) {
        this(context, null);
    }

    @Override
    public void setTextAppearance(Context context, int resid) {
        super.setTextAppearance(context, resid);
        setGnTextSize();
    }

    @Override
    public void setTextSize(float size) {
        // TODO Auto-generated method stub
        super.setTextSize(size);
        setGnTextSize();
    }

    private void setGnTextSize() {
        if (mLargeTextType > 0) {
            switch (mLargeTextType) {
                case 1:
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize() * 12 / 11);
                    break;
                case 2:
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize() * 14 / 11);
                    break;
            }
        }
    }
}