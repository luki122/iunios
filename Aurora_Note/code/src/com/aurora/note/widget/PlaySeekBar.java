package com.aurora.note.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.aurora.note.R;
import com.aurora.note.bean.MarkInfo;
import com.aurora.utils.DensityUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 播放录音界面专用的SeekBar
 * @author JimXia
 * 2014-8-7 上午11:08:11
 */
public class PlaySeekBar extends SeekBar {
    private final ArrayList<MarkInfo> mWholeMarkInfo = new ArrayList<MarkInfo>();
    private Paint mMarkPaint;
    
    public PlaySeekBar(Context context) {
        this(context, null);
    }

    public PlaySeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlaySeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mMarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMarkPaint.setColor(context.getResources().getColor(R.color.play_seek_bar_mark_color));
        mMarkPaint.setStrokeWidth(DensityUtil.dip2px(context, 1.3f));
    }
    
    public void addMarkInfo(MarkInfo markInfo) {
        if (markInfo != null) {
            mWholeMarkInfo.add(markInfo);
            sortMarkInfo();
            invalidate();
        }
    }
    
    public void addAllMarkInfo(ArrayList<MarkInfo> allMarkInfo) {
        if (allMarkInfo != null && !allMarkInfo.isEmpty()) {
            mWholeMarkInfo.addAll(allMarkInfo);
            sortMarkInfo();
            invalidate();
        }
    }
    
    @SuppressWarnings("unchecked")
    public ArrayList<MarkInfo> getAllMarkInfo() {
        ArrayList<MarkInfo> wholeMarkInfo = null;
        synchronized (mWholeMarkInfo) {
            wholeMarkInfo = (ArrayList<MarkInfo>) mWholeMarkInfo.clone();
        }
        return wholeMarkInfo;
    }
    
    private void sortMarkInfo() {
        Collections.sort(mWholeMarkInfo, new Comparator<MarkInfo>() {
            @Override
            public int compare(MarkInfo lhs, MarkInfo rhs) {
                if (lhs == null && rhs == null) {
                    return 0;
                }
                
                if (lhs == null && rhs != null) {
                    return -1;
                }
                
                if (lhs != null && rhs == null) {
                    return 1;
                }
                
                if (lhs.getMarkElpasedTime() < rhs.getMarkElpasedTime()) {
                    return -1;
                }
                
                if (lhs.getMarkElpasedTime() > rhs.getMarkElpasedTime()) {
                    return 1;
                }
                
                return 0;
            }
        });
    }
    
    public int getMarkNum() {
        return mWholeMarkInfo.size();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        final ArrayList<MarkInfo> wholeMarkInfo = mWholeMarkInfo;
        if (!wholeMarkInfo.isEmpty()) {
            final int duration = getMax();
            final int measuredWidth = getMeasuredWidth();
            final int measuredHeight = getMeasuredHeight();
            final float markHeight = DensityUtil.dip2px(getContext(), 3f);
            final float yTop = (measuredHeight + markHeight) / 2f;
            for (int i = 0, size = wholeMarkInfo.size(); i < size; i ++) {
                MarkInfo info = wholeMarkInfo.get(i);
                if (info != null) {
                    float x = info.getMarkElpasedTime() * 1.0f / duration * measuredWidth;
                    canvas.drawLine(x, yTop, x, yTop + markHeight, mMarkPaint);
                }
            }
        }
    }
}
