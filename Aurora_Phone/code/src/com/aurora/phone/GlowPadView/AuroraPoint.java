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
	private int r = 150;
	
	private int mRejectColor =   Color.rgb(246,66,67);
	private int mAnswerColor =   Color.rgb(14,188,125);
	private int mRadius = 4*3;
    private Paint mRejectPaint, mAnswerPaint;


    public AuroraPoint(Context context) {
        mPaint = new Paint();
        mPaint.setFilterBitmap(true);
        mPaint.setColor(Color.rgb(255, 255, 255)); // TODO: make configurable
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
             
        r = context.getResources().getDimensionPixelSize(R.dimen.aurora_point_radius_not_touch);        
        
    	mRadius = context.getResources().getDimensionPixelSize(R.dimen.aurora_handle_point_radius);
     	mRejectPaint = new Paint();
     	mRejectPaint.setColor(mRejectColor);
     	mRejectPaint.setAntiAlias(true);
     	mAnswerPaint = new Paint();
     	mAnswerPaint.setColor(mAnswerColor);
     	mAnswerPaint.setAntiAlias(true);
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
        mRejectPaint.setAlpha((int) Math.round(mAlpha * 255f));
        canvas.drawCircle(0, 0, mRadius, mRejectPaint);
        canvas.translate(0, 2 * r);
        mAnswerPaint.setAlpha((int) Math.round(mAlpha * 255f));
        canvas.drawCircle(0, 0, mRadius, mAnswerPaint);
        
        
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
