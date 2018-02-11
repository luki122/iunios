package com.aurora.note.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aurora.note.R;

/**
 * 用来显示标签的容器
 * @author JimXia
 * 2014-5-4 下午5:38:51
 */
public class LabelLinearLayout extends LinearLayout {
    private static final String TAG = LabelLinearLayout.class.getSimpleName();
    
    public LabelLinearLayout(Context context) {
        super(context);
    }

    public LabelLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LabelLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }    

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        int width = getMeasuredWidth();
        if (width > 0) {
            final int childCount = getChildCount();
            int availableWidth = width;
//            Log.d(TAG, "Jim, child count: " + childCount + ", availableWidth: " + availableWidth);
            if (availableWidth > 0) {
                int newWidth = 0;
                TextView label1ViewTv = null;
                TextView separateTv = null;
                TextView label2ViewTv = null;
                for (int i = 0; i < childCount; i ++) {
                    View childView = getChildAt(i);
                    if (childView != null && childView.getVisibility() != View.GONE) {
                        int id = childView.getId();
                        newWidth += childView.getMeasuredWidth();
                        LayoutParams llp = (LayoutParams) childView.getLayoutParams();
                        if (llp != null) {
                            newWidth = newWidth + llp.leftMargin + llp.rightMargin;
                        }
                        if (id == R.id.note_label1_tv && childView instanceof TextView) {
                            label1ViewTv = (TextView) childView;
                        } else if (id == R.id.note_label2_tv && childView instanceof TextView) {
                            label2ViewTv = (TextView) childView;
                        } else if (id == R.id.note_label_sep_tv && childView instanceof TextView) {
                            separateTv = (TextView) childView;
                        } else {
                            availableWidth -= childView.getMeasuredWidth();
                            if (llp != null) {
                                availableWidth = availableWidth - llp.leftMargin - llp.rightMargin;
                            }
                        }
                    }
                }
                
                if (label1ViewTv != null && label2ViewTv != null && separateTv != null) {
                    int widthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    final int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    label1ViewTv.measure(widthSpec, heightSpec);
                    label2ViewTv.measure(widthSpec, heightSpec);
                    separateTv.measure(widthSpec, heightSpec);
                    availableWidth -= separateTv.getMeasuredWidth();                    
                    int label1Width = label1ViewTv.getMeasuredWidth();
                    int label2Width = label2ViewTv.getMeasuredWidth();
                    final int maxWidth = availableWidth / 2;
//                    Log.d(TAG, "Jim, label1 width: " + label1Width + ", label2 width: " + label2Width +
//                            ", availableWidth: " + availableWidth + ", maxWidth: " + maxWidth);
                    if ((label1Width + label2Width) >= availableWidth) {
                        newWidth = width;
                        if (label1Width > maxWidth && label2Width > maxWidth) {
                            widthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY);
                            label1ViewTv.measure(widthSpec, heightSpec);
                            label2ViewTv.measure(widthSpec, heightSpec);
                            Log.d(TAG, "Jim, label1 new width: " + label1ViewTv.getMeasuredWidth() +
                                    ", label2 new width: " + label2ViewTv.getMeasuredWidth());
                        } else if (label1Width > maxWidth) {
                            widthSpec = MeasureSpec.makeMeasureSpec(availableWidth - label2Width, MeasureSpec.EXACTLY);
                            label1ViewTv.measure(widthSpec, heightSpec);
                            Log.d(TAG, "Jim, label1 new width: " + label1ViewTv.getMeasuredWidth());
                        } else if (label2Width > maxWidth) {
                            widthSpec = MeasureSpec.makeMeasureSpec(availableWidth - label1Width, MeasureSpec.EXACTLY);
                            label2ViewTv.measure(widthSpec, heightSpec);
                            Log.d(TAG, "Jim, label2 new width: " + label2ViewTv.getMeasuredWidth());
                        } else {
                            Log.e(TAG, "Jim, label1 width: " + label1Width + ", label2 width: " + label2Width + ", max width: " + maxWidth);
                        }
                    } else {
                        Log.e(TAG, "Jim, label1 width: " + label1Width + ", label2 width: " + label2Width + ", max width: " + maxWidth);
                    }
                }
                
                if (newWidth > 0 && newWidth != width) {
                    setMeasuredDimension(newWidth, getMeasuredHeight());
                }
            }
        }        
    }
}
