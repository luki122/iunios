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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.FloatMath;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

public class AuroraDialingPoint {
    private static final String TAG = "AuroraDialingPoint";
    private Paint mPaint;
	
	private int mRadius = 2;
	private int mIndex;
	
	   
    public int getIndex() {
    	return mIndex;
    }    

    public AuroraDialingPoint(Context context, int index) {
    	mIndex = index;
        mPaint = new Paint();
        mPaint.setFilterBitmap(true);
        mPaint.setColor(0x99414142); // TODO: make configurable
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);                      
    }



    public void draw(Canvas canvas) {
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
//        canvas.translate(mCenterX, mCenterY);
        mPaint.setAlpha((int) Math.round(mAlpha * 255f));
        canvas.drawCircle(2 + mIndex * 10, 6, mRadius, mPaint);               
        canvas.restore();
    }
    

    private float mAlpha = 1.0f;
    
    public void setAlpha(float alpha) {
        mAlpha = alpha;
    }
    
    public float getAlpha() {
        return mAlpha;
    }
    

}
