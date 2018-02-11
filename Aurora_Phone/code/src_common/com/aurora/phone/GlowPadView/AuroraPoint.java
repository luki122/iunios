/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.phone;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.FloatMath;
import android.util.Log;

public class AuroraPoint {
    private static final float MIN_POINT_SIZE = 2.0f;
    private static final float MAX_POINT_SIZE = 4.0f;
    private static final int INNER_POINTS = 8;
    private static final String TAG = "AuroraPoint";
    private float mCenterX;
    private float mCenterY;
    private Paint mPaint;
    private float mScale = 1.0f;
    private static final float PI = (float) Math.PI;
    private Drawable[] mDrawable;
	private int r = 150;


    public AuroraPoint(Context context) {
        mPaint = new Paint();
        mPaint.setFilterBitmap(true);
        mPaint.setColor(Color.rgb(255, 255, 255)); // TODO: make configurable
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        
        mDrawable = new Drawable[2];
        mDrawable[0] = context.getResources().getDrawable(R.drawable.aurora_point_reject);
        mDrawable[1] = context.getResources().getDrawable(R.drawable.aurora_point_reject);        
   
        r = context.getResources().getDimensionPixelSize(R.dimen.aurora_point_radius_not_touch);
        
        mDrawable[0].setBounds(0, 0,
        		mDrawable[0].getIntrinsicWidth(), mDrawable[0].getIntrinsicHeight());
        mDrawable[1].setBounds(0, 0,
        		mDrawable[1].getIntrinsicWidth(), mDrawable[1].getIntrinsicHeight());
    }

    public void setCenter(float x, float y) {
        mCenterX = x;
        mCenterY = y;
    }
    


    public void setScale(float scale) {
        mScale  = scale;
    }

    public float getScale() {
        return mScale;
    }

    private static float hypot(float x, float y) {
        return FloatMath.sqrt(x*x + y*y);
    }

    private static float max(float a, float b) {
        return a > b ? a : b;
    }

    private float interp(float min, float max, float f) {
        return min + (max - min) * f;
    }

    public void draw(Canvas canvas) {
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.translate(mCenterX, mCenterY);
        canvas.scale(mScale, mScale, mCenterX, mCenterY);
        
        canvas.translate(0, -r);
        canvas.translate(-0.5f * getWidth(mDrawable[1]), -0.5f * getHeight(mDrawable[1]));
        mDrawable[1].setAlpha((int) Math.round(mAlpha * 255f));
        mDrawable[1].draw(canvas);
        canvas.translate(0, 2 * r);
        mDrawable[0].setAlpha((int) Math.round(mAlpha * 255f));
        mDrawable[0].draw(canvas);
        canvas.restore();
    }
    
    public int getWidth(Drawable drawable) {
        return drawable != null ? drawable.getIntrinsicWidth() : 0;
    }

    public int getHeight(Drawable drawable) {
        return drawable != null ? drawable.getIntrinsicHeight() : 0;
    }

    private float mAlpha = 1.0f;
    
    public void setAlpha(float alpha) {
        mAlpha = alpha;
    }
    
    public float getAlpha() {
        return mAlpha;
    }
}
