package com.android.keyguard.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class AuroraKeyguardBg extends ImageView {
    
    public AuroraKeyguardBg(Context context) {
        this(context, null);
    }

    public AuroraKeyguardBg(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AuroraKeyguardBg(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

}
