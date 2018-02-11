package com.aurora.note.widget;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;

import com.aurora.note.NoteApp;
import com.aurora.note.R;

/**
 * 用来显示录音的ImageSpan
 * @author JimXia
 * 2014-6-26 下午5:43:39
 */
public class NoteSoundSpan extends NoteImageSpan {
    private boolean mIsPlaying = false;
    private String mRecordName;
    private String mRecordTime;
    
    private Paint mRecordNamePaint;
    private Paint mRecordTimePaint;
    
    private static final int SELECTED_DRAWABLE_ALPHA = 150;
    private int mOriginalAlpha = 255;
    
    private static int mX;
    private static int mYForRecordName;
    private static int mYForRecordTime;
    
    static {
        final Resources res = NoteApp.ysApp.getResources();
        mX = res.getDimensionPixelSize(R.dimen.new_note_record_text_padding_left);
        mYForRecordName = res.getDimensionPixelSize(R.dimen.new_note_record_name_y);
        mYForRecordTime = res.getDimensionPixelSize(R.dimen.new_note_record_time_y);
    }
    
    public NoteSoundSpan(Drawable d, Drawable selectedDrawable, String source, Type type,
            int rightExtraSpace, int leftPadding, int topPadding, String recordName, String recordTime) {
        super(d, selectedDrawable, source, type, rightExtraSpace, leftPadding, topPadding);
        mRecordName = recordName;
        mRecordTime = recordTime;
        
        final Resources res = NoteApp.ysApp.getResources();
        mRecordNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);// 设置画笔
        mRecordNamePaint.setTextSize(res.getDimensionPixelSize(R.dimen.new_note_record_name_font_size));
        mRecordNamePaint.setColor(0xffbda69a);
        
        mRecordTimePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);// 设置画笔
        mRecordTimePaint.setTextSize(res.getDimensionPixelSize(R.dimen.new_note_record_time_font_size));
        mRecordTimePaint.setColor(0xff7f5f47);
        mRecordTimePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mRecordTimePaint.setStrokeWidth(1.5f);
        
        if (d != null) {
            if (d instanceof BitmapDrawable) {
                mOriginalAlpha = ((BitmapDrawable) d).getPaint().getAlpha();
            } else if (d instanceof NinePatchDrawable) {
                mOriginalAlpha = ((NinePatchDrawable) d).getPaint().getAlpha();
            }
        }
    }
    
    @Override
    public void setSelected(boolean selected) {
        mSelected = selected;
        if (selected) {
            getDrawable().setAlpha(SELECTED_DRAWABLE_ALPHA);
            mRecordNamePaint.setAlpha(SELECTED_DRAWABLE_ALPHA);
            mRecordTimePaint.setAlpha(SELECTED_DRAWABLE_ALPHA);
        } else {
            getDrawable().setAlpha(mOriginalAlpha);
            mRecordNamePaint.setAlpha(mOriginalAlpha);
            mRecordTimePaint.setAlpha(mOriginalAlpha);
        }
    }
    
    public void setIsPlaying(boolean isPlaying) {
        mIsPlaying = isPlaying;
    }

    @Override
    public Drawable getDrawable() {
        if (mIsPlaying) {
            return mSelectedDrawable;
        }
        
        return mDrawable;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y,
            int bottom, Paint paint) {
        super.draw(canvas, text, start, end, x, top, y, bottom, paint);        
                    
        final int xOffset = getLeft() - mLeftPadding;
        final int yOffset = getTop() - mTopPadding;
        canvas.drawText(mRecordName, xOffset + mX, yOffset + mYForRecordName, mRecordNamePaint);
        canvas.drawText(mRecordTime, xOffset + mX, yOffset + mYForRecordTime, mRecordTimePaint);
    }
}