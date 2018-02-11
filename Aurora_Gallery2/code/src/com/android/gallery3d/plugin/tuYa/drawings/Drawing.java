package com.android.gallery3d.plugin.tuYa.drawings;

import android.graphics.Canvas;
import android.graphics.Paint;

public abstract class Drawing {
    protected float mStartX;
    protected float mStartY;
    protected float mEndX;
    protected float mEndY;
    protected Paint mPaint;

    public Drawing(Paint paint) {
        mPaint = paint;
    }

    public abstract void apply(Canvas canvas);

    public void fingerDown(float x, float y) {
        mStartX = x;
        mStartY = y;
        mEndX = x;
        mEndY = y;
    }

    public void fingerMove(float x, float y) {
        mEndX = x;
        mEndY = y;
    }

    public void fingerUp(float x, float y) {
        mEndX = x;
        mEndY = y;
    }

    @Override
    public String toString() {
        return "mStartX = " + mStartX + ",mStartY = " + mStartY + ",mEndX = " + mEndX + ",mEndY = " + mEndY;
    }
}