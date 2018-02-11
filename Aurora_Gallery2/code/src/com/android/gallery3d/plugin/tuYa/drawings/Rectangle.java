package com.android.gallery3d.plugin.tuYa.drawings;


import android.graphics.Canvas;
import android.graphics.Paint;

public class Rectangle extends Drawing {

    public Rectangle(Paint paint) {
        super(paint);
    }

    @Override
    public void apply(Canvas canvas) {
        canvas.drawRect(Math.min(mStartX, mEndX), Math.min(mStartY, mEndY), Math.max(mStartX, mEndX),
                Math.max(mStartY, mEndY), mPaint);
    }

}
