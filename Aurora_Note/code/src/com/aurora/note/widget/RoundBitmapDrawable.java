package com.aurora.note.widget;

import android.content.res.Resources;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;

import com.aurora.note.R;

/**
 * 带圆角和边框的BitmapDrawable
 * 
 * @author JimXia
 * @date 2015年1月14日 上午11:41:40
 */
public class RoundBitmapDrawable extends BitmapDrawable {
    private BitmapDrawable mDrawable;
    private Paint mLinePaint;
    private Paint mBitmapPaint;
    
    private final RectF mRectF = new RectF();
    private float mRoundRadius = 50;
    
    private boolean mNeedBorder = true;
    
    public RoundBitmapDrawable(Resources res) {
        super(res);
        
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG/* | Paint.DITHER_FLAG*/);
        mLinePaint.setColor(Color.RED);
        mLinePaint.setStyle(Style.STROKE);
        
        mDrawable = (BitmapDrawable) res.getDrawable(R.drawable.ic_launcher);
    }
    
    public void setBitmapDrawable(BitmapDrawable bd) {
        if (bd == null) {
            throw new NullPointerException("bd cannot be null");
        }
        mDrawable = bd;
        mBitmapPaint = null;
        invalidateSelf();
    }
    
    public void setBorderColor(int color) {
        mLinePaint.setColor(color);
        invalidateSelf();
    }
    
    public void setBorderWidth(float width) {
        mLinePaint.setStrokeWidth(width);
        invalidateSelf();
    }
    
    public void setRoundRadius(float roundRadius) {
        mRoundRadius = roundRadius;
        invalidateSelf();
    }
    
    public void setBorderVisible(boolean visible) {
        mNeedBorder = visible;
        invalidateSelf();
    }
    
    public boolean isBorderVisible() {
        return mNeedBorder;
    }
    
    public float getRoundRadius() {
        return mRoundRadius;
    }
    
    public BitmapDrawable getBitmapDrawable() {
        return mDrawable;
    }
    
    public int getBorderColor() {
        return mLinePaint.getColor();
    }
    
    public float getBorderWidth() {
        return mLinePaint.getStrokeWidth();
    }
    
    public Paint getBorderPaint() {
        return mLinePaint;
    }

    @Override
    public void draw(Canvas canvas) {
        final Rect rect = getBounds();
        mRectF.set(rect);
        final float roundPx = mRoundRadius;
        
        if (mBitmapPaint == null) {
            mBitmapPaint = mDrawable.getPaint();
            if (mBitmapPaint.getShader() == null) {
                mBitmapPaint.setShader(new BitmapShader(mDrawable.getBitmap(),
                mDrawable.getTileModeX(), mDrawable.getTileModeY()));
            }
        }
        canvas.drawRoundRect(mRectF, roundPx, roundPx, mBitmapPaint);
        if (mNeedBorder) {
            canvas.drawRoundRect(mRectF, roundPx, roundPx, mLinePaint);
        }
    }
}
