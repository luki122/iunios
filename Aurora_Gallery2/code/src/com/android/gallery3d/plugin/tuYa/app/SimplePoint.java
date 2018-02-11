package com.android.gallery3d.plugin.tuYa.app;

/**
 * Created by gaojt on 14-10-20.
 */
public class SimplePoint {

    private float mX = -1;
    private float mY = -1;

    public SimplePoint() {

    }

    public SimplePoint(float x, float y) {
        mX = x;
        mY = y;
    }

    public boolean isEmpty() {
        return mX < 0 || mY < 0;
    }

    public void setEmpty() {
        mX = -1;
        mY = -1;
    }

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }

    public void setXY(float x, float y) {
        mX = x;
        mY = y;
    }
}
