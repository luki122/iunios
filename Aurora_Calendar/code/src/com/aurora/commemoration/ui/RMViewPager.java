package com.aurora.commemoration.ui;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class RMViewPager extends ViewPager {

    public static final String TAG = "RMViewPager";

    private HashMap<Integer, Object> mObjs = new LinkedHashMap<Integer, Object>();
    private static final float SCALE_MAX = 0.9f;
    private float preX = 0;
    private int moveLength = 0;

    public RMViewPager(Context context) {
        this(context, null);
    }

    public RMViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClipChildren(false);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            preX = event.getX();
        } else {
            if (Math.abs(event.getX() - preX) > 4) {
            } else {
                preX = event.getX();
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    private State mState = State.IDLE;
    private int oldPage;

    private View mLeft;
    private View mRight;
    private float mTrans;
    private float mScale;

    private enum State {
        IDLE,
        GOING_LEFT,
        GOING_RIGHT
    }

    protected void animateStack(View left, View right, float positionOffset, int positionOffsetPixels) {
        if (mState != State.IDLE) {
            if (right != null) {
                manageLayer(right, true);
                mScale = (1 - SCALE_MAX) * positionOffset + SCALE_MAX;
                mTrans = -getWidth() - getPageMargin() + positionOffsetPixels;
                right.setScaleX(mScale);
                right.setScaleY(mScale);
                right.setTranslationX(mTrans);
            }
            if (left != null) {
                left.bringToFront();
            }
        }
    }

    private void manageLayer(View v, boolean enableHardware) {
        int layerType = enableHardware ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
        if (layerType != v.getLayerType())
            v.setLayerType(layerType, null);
    }

    private void disableHardwareLayer() {
        View v;
        for (int i = 0; i < getChildCount(); i++) {
            v = getChildAt(i);
            if (v.getLayerType() != View.LAYER_TYPE_NONE)
                v.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }

    protected void animateFade(View left, View right, float positionOffset) {
        if (left != null) {
            left.setAlpha(1 - positionOffset);
        }
        if (right != null) {
            right.setAlpha(positionOffset);
        }
    }

//    @Override
//    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//        oldPage = getCurrentItem();
//        int length = positionOffsetPixels - moveLength;
//        moveLength = positionOffsetPixels;
//        if (length > 0) {
//            mState = State.GOING_RIGHT;
//        } else {
//            mState = State.GOING_LEFT;
//        }
//
//        float effectOffset = isSmall(positionOffset) ? 0 : positionOffset;
//        mLeft = findViewFromObject(position);
//        mRight = findViewFromObject(position + 1);
//        animateFade(null, mRight, effectOffset);
//        animateStack(mLeft, mRight, effectOffset, positionOffsetPixels);
//
//        if (effectOffset == 0) {
//            disableHardwareLayer();
//            mState = State.IDLE;
//        }
//        super.onPageScrolled(position, positionOffset, positionOffsetPixels);
//    }

    private boolean isSmall(float positionOffset) {
        return Math.abs(positionOffset) < 0.0001;
    }

    public void setObjectForPosition(Object obj, int position) {
        mObjs.put(Integer.valueOf(position), obj);
    }

    public void clearAll() {
        mObjs.clear();
    }

    public View findViewFromObject(int position) {
        Object o = mObjs.get(Integer.valueOf(position));
        if (o == null) {
            return null;
        }
        PagerAdapter a = getAdapter();
        View v;
        for (int i = 0; i < getChildCount(); i++) {
            v = getChildAt(i);
            if (a.isViewFromObject(v, o)) {
                mObjs.remove(Integer.valueOf(position));
                return v;
            }
        }
        return null;
    }

}