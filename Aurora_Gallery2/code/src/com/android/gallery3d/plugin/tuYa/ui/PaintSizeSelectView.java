package com.android.gallery3d.plugin.tuYa.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class PaintSizeSelectView extends LinearLayout implements View.OnClickListener {

    public interface PaintSizeListener {
        public void onPaintSizeSelected(int viewId);
    }

    private PaintSizeListener mPaintSizeListener;
    private int mPaintSizeId;

    public PaintSizeSelectView(Context context) {
        this(context, null);
    }

    public PaintSizeSelectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaintSizeSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        if (mPaintSizeId > 0) {
            updateSelectView();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPaintSizeListener = null;
    }

    public void init(int viewId) {
        mPaintSizeId = viewId;
        updateSelectView();
    }

    public void updateSelectView(int viewId) {
        if (viewId != mPaintSizeId) {
            mPaintSizeId = viewId;
            updateSelectView();
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId != mPaintSizeId) {
            mPaintSizeId = viewId;
            notifyPaintColorListener(viewId);
            updateSelectView();
        }
    }

    private void notifyPaintColorListener(int viewId) {
        if (mPaintSizeListener != null) {
            mPaintSizeListener.onPaintSizeSelected(viewId);
        }
    }

    private void updateSelectView() {
        final int tabCount = getChildCount();
        for (int i = 0; i < tabCount; i++) {
            final View child = getChildAt(i);
            child.setSelected(child.getId() == mPaintSizeId);
        }
        invalidate();
    }

    public void setPaintSizeListener(PaintSizeListener listener) {
        mPaintSizeListener = listener;
    }

}
