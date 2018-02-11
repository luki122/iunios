package com.android.gallery3d.plugin.tuYa.drawings;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class PathLine extends Drawing {
    private static final float TOUCH_TOLERANCE = 4;
    private Path mPath = null;
    private float mX, mY;

    public PathLine(Paint paint) {
        super(paint);
        mPath = new Path();
    }

    @Override
    public void apply(Canvas canvas) {
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public void fingerDown(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    @Override
    public void fingerMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    @Override
    public void fingerUp(float x, float y) {
        mPath.lineTo(mX, mY);
    }
}
