package com.android.gallery3d.plugin.tuYa.drawings;


import android.graphics.Canvas;
import android.graphics.Paint;

public class Circle extends Drawing {

    public Circle(Paint paint) {
        super(paint);
    }

    @Override
    public void apply(Canvas canvas) {
        float dx = Math.abs(mStartX - mEndX) / 2;
        float dy = Math.abs(mStartY - mEndY) / 2;
        float x = Math.min(mStartX, mEndX) + dx;
        float y = Math.min(mStartY, mEndY) + dy;
        float radius = (float) Math.sqrt(dx * dx + dy * dy);
        canvas.drawCircle(x, y, radius, mPaint);
    }
}
