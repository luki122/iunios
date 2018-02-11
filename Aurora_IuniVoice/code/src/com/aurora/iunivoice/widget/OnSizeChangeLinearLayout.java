package com.aurora.iunivoice.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 用来监听onSizeChanged事件
 * @author JimXia
 *
 * @date 2014年12月23日 上午11:19:41
 */
public class OnSizeChangeLinearLayout extends LinearLayout {
    private OnSizeChangedListener mListener;
    
    public OnSizeChangeLinearLayout(Context context) {
        this(context, null);
    }

    public OnSizeChangeLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OnSizeChangeLinearLayout(Context context, AttributeSet attrs, int defStyle) {
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