package com.aurora.note.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 新建备忘界面的根布局，用来监听onSizeChanged事件
 * @author JimXia
 * 2014-5-12 上午10:37:15
 */
public class NewNoteRelativeLayout extends RelativeLayout {

    private OnSizeChangedListener mListener;

    public NewNoteRelativeLayout(Context context) {
        this(context, null);
    }

    public NewNoteRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewNoteRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        notifyOnSizeChanged(w, h, oldw, oldh);
    }

    private void notifyOnSizeChanged(int w, int h, int oldw, int oldh) {
        if (mListener != null) {
            mListener.onSizeChanged(w, h, oldw, oldh);
        }
    }

    public void setOnSizeChangedListener(OnSizeChangedListener listener) {
        mListener = listener;
    }

    public static interface OnSizeChangedListener {
        void onSizeChanged(int w, int h, int oldw, int oldh);
    }

}