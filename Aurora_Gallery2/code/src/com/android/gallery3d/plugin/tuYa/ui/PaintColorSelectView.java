package com.android.gallery3d.plugin.tuYa.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.gallery3d.common.Utils;

public class PaintColorSelectView extends LinearLayout implements View.OnClickListener {

    public interface PaintColorListener {
        public void onPaintColorSelected(int viewId);
    }

    private PaintColorListener mPaintColorListener;
    private int mPaintColorId;

    public PaintColorSelectView(Context context) {
        this(context, null);
    }

    public PaintColorSelectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaintColorSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        if (mPaintColorId > 0) {
            updateSelectView();
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId != mPaintColorId) {
            mPaintColorId = viewId;
            notifyPaintColorListener(viewId);
            updateSelectView();
        }
    }

    public void init(int viewId) {
        mPaintColorId = viewId;
        updateSelectView();
    }

    public void updateSelectView(int viewId) {
        if (viewId != mPaintColorId) {
            mPaintColorId = viewId;
            updateSelectView();
        }
    }

    private void notifyPaintColorListener(int viewId) {
        if (mPaintColorListener != null) {
            mPaintColorListener.onPaintColorSelected(viewId);
        }
    }

    private void updateSelectView() {
        final int tabCount = getChildCount();
        for (int i = 0; i < tabCount; i++) {
            final View child = getChildAt(i);
            setColorSelected((ViewGroup) child, (child.getId() == mPaintColorId));
        }
        invalidate();
    }

    public void setPaintColorListener(PaintColorListener listener) {
        mPaintColorListener = listener;
    }

    private void setColorSelected(ViewGroup group, boolean selected) {
        final int childCount = group.getChildCount();
        Utils.assertTrue(childCount == 2);
        group.getChildAt(0).setSelected(selected);
        group.getChildAt(1).setVisibility(selected ? VISIBLE : GONE);
    }


}
