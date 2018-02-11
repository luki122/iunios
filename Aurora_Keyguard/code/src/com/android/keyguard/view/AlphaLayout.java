package com.android.keyguard.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class AlphaLayout extends FrameLayout {

    private int mAlpha;
    private boolean mBgAlpha = false;

    public AlphaLayout(Context context) {
        this(context, null);
    }

    public AlphaLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlphaLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
        mAlpha = 255;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int i = 0;
        if (mAlpha != 255) {
            i = canvas.saveLayerAlpha(0.0F, 0.0F, getWidth(), getHeight(), mAlpha, 20);
            if (mBgAlpha) {
                Drawable localDrawable = getBackground();
                if (localDrawable != null)
                    localDrawable.setAlpha(mAlpha);
            }
        }
        super.dispatchDraw(canvas);
        canvas.restoreToCount(i);
    }

    public void setAlpha(int alpha) {
        mAlpha = alpha;
        invalidate();
    }

}
