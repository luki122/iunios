package com.android.keyguard.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class AlphaBackground extends View {

    public AlphaBackground(Context context) {
        this(context, null);
    }

    public AlphaBackground(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlphaBackground(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAlpha(int alpha){
        getBackground().setAlpha(alpha);
        invalidate();
    }
    
}
