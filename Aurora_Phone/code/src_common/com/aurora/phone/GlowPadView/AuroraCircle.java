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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.FloatMath;
import android.util.Log;

public class AuroraCircle {
    private static final float MIN_POINT_SIZE = 2.0f;
    private static final float MAX_POINT_SIZE = 4.0f;
    private static final int INNER_POINTS = 8;
    private static final String TAG = "AuroraCircle";
    private float mCenterX;
    private float mCenterY;
    private Paint mPaint;
    private float mScale = 1.0f;
    private static final float PI = (float) Math.PI;

    // These allow us to have multiple concurrent animations.
    WaveManager waveManager = new WaveManager();

    public class WaveManager {
        private float radius = 50;
        private float radius2 = 50;
        private float alpha = 0.0f;

        public void setRadius(float r) {
            radius = r;
        }

        public float getRadius() {
            return radius;
        }        

        public void setAlpha(float a) {
            alpha = a;
        }

        public float getAlpha() {
            return alpha;
        }
    };


    public AuroraCircle() {
        mPaint = new Paint();
        mPaint.setFilterBitmap(true);
        mPaint.setColor(Color.rgb(255, 255, 255)); // TODO: make configurable
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
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
        canvas.scale(mScale, mScale, mCenterX, mCenterY);
        mPaint.setAlpha((int)(waveManager.getAlpha() * 255));
        canvas.drawCircle(mCenterX, mCenterY, waveManager.getRadius()/2, mPaint);
        canvas.restore();
    }

}
