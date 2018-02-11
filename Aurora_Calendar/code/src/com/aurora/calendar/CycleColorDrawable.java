package com.aurora.calendar;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class CycleColorDrawable extends ColorDrawable {

    private ColorState mState;
    private final Paint mPaint;
    private boolean mMutated;

    private int mCenterX = 50;
    private int mCenterY = 50;
    private int mRadius = 50;
    /**
     * Creates a new black ColorDrawable.
     */
    public CycleColorDrawable() {
        this(null);
    }

    /**
     * Creates a new ColorDrawable with the specified color.
     *
     * @param color The color to draw.
     */
    public CycleColorDrawable(int color) {
        this(null);
        setColor(color);
    }

    private CycleColorDrawable(ColorState state) {
        mState = new ColorState(state);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | mState.mChangingConfigurations;
    }

    /**
     * A mutable BitmapDrawable still shares its Bitmap with any other Drawable
     * that comes from the same resource.
     *
     * @return This drawable.
     */
    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mState = new ColorState(mState);
            mMutated = true;
        }
        return this;
    }
    
    public void setCenter(int centerX, int centerY) {
        mCenterX = centerX;
        mCenterY = centerY;
    }

    public void setRadius(int radius){
        mRadius = radius;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if ((mState.mUseColor >>> 24) != 0) {
            mPaint.setColor(mState.mUseColor);
            // canvas.drawRect(getBounds(), mPaint);
            canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
        }
    }

    /**
     * Gets the drawable's color value.
     *
     * @return int The color to draw.
     */
    public int getColor() {
        return mState.mUseColor;
    }

    /**
     * Sets the drawable's color value. This action will clobber the results of prior calls to
     * {@link #setAlpha(int)} on this object, which side-affected the underlying color.
     *
     * @param color The color to draw.
     */
    public void setColor(int color) {
        if (mState.mBaseColor != color || mState.mUseColor != color) {
            invalidateSelf();
            mState.mBaseColor = mState.mUseColor = color;
        }
    }

    /**
     * Returns the alpha value of this drawable's color.
     *
     * @return A value between 0 and 255.
     */
    public int getAlpha() {
        return mState.mUseColor >>> 24;
    }

    /**
     * Sets the color's alpha value.
     *
     * @param alpha The alpha value to set, between 0 and 255.
     */
    public void setAlpha(int alpha) {
        alpha += alpha >> 7;   // make it 0..256
        int baseAlpha = mState.mBaseColor >>> 24;
        int useAlpha = baseAlpha * alpha >> 8;
        int oldUseColor = mState.mUseColor;
        mState.mUseColor = (mState.mBaseColor << 8 >>> 8) | (useAlpha << 24);
        if (oldUseColor != mState.mUseColor) {
            invalidateSelf();
        }
    }

    /**
     * Setting a color filter on a ColorDrawable has no effect.
     *
     * @param colorFilter Ignore.
     */
    public void setColorFilter(ColorFilter colorFilter) {
    }

    public int getOpacity() {
        switch (mState.mUseColor >>> 24) {
            case 255:
                return PixelFormat.OPAQUE;
            case 0:
                return PixelFormat.TRANSPARENT;
        }
        return PixelFormat.TRANSLUCENT;
    }

    /*
    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs);

        TypedArray a = r.obtainAttributes(attrs, com.android.internal.R.styleable.ColorDrawable);

        int color = mState.mBaseColor;
        color = a.getColor(com.android.internal.R.styleable.ColorDrawable_color, color);
        mState.mBaseColor = mState.mUseColor = color;

        a.recycle();
    }
    */

    @Override
    public ConstantState getConstantState() {
        mState.mChangingConfigurations = getChangingConfigurations();
        return mState;
    }

    final static class ColorState extends ConstantState {
        int mBaseColor; // base color, independent of setAlpha()
        int mUseColor;  // basecolor modulated by setAlpha()
        int mChangingConfigurations;

        ColorState(ColorState state) {
            if (state != null) {
                mBaseColor = state.mBaseColor;
                mUseColor = state.mUseColor;
                mChangingConfigurations = state.mChangingConfigurations;
            }
        }

        @Override
        public Drawable newDrawable() {
            return new CycleColorDrawable(this);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new CycleColorDrawable(this);
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
    }
}
