package com.android.gallery3d.plugin.tuYa.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class DrawingSelectView extends LinearLayout implements View.OnClickListener {

    public interface DrawingListener {
        public void onDrawingSelected(int drawingViewId);
    }

    private DrawingListener mDrawingListener;
    private int mCurDrawingId;

    public DrawingSelectView(Context context) {
        this(context, null);
    }

    public DrawingSelectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawingSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final int tabCount = getChildCount();
        for (int i = 0; i < tabCount; i++) {
            final View child = getChildAt(i);
            child.setOnClickListener(this);
        }
        if (mCurDrawingId > 0) {
            updateSelectView();
        }
    }

    public void init(int viewId) {
        mCurDrawingId = viewId;
        updateSelectView();
    }

    public void updateSelectView(int viewId) {
        if (viewId != mCurDrawingId) {
            mCurDrawingId = viewId;
            updateSelectView();
        }
    }

    private void updateSelectView() {
        final int tabCount = getChildCount();
        for (int i = 0; i < tabCount; i++) {
            final View child = getChildAt(i);
            child.setSelected(child.getId() == mCurDrawingId);
        }
        invalidate();
    }

    @Override
    public void onClick(View v) {
        int drawingViewId = v.getId();
        if (drawingViewId != mCurDrawingId) {
            mCurDrawingId = drawingViewId;
            notifyDrawingListener(drawingViewId);
            updateSelectView();
        }
    }

    public void setDrawingListener(DrawingListener listener) {
        mDrawingListener = listener;
    }

    private void notifyDrawingListener(int drawingViewId) {
        if (mDrawingListener != null) {
            mDrawingListener.onDrawingSelected(drawingViewId);
        }
    }

}
