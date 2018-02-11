/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Vibrator;
import android.util.Log;
import android.os.SystemVibrator;

public class CenterTargetDrawable extends TargetDrawable {
    private static final String TAG = "CenterTargetDrawable";
    private int mBgcolor =   Color.rgb(14,188,125);
    private int mRadius = 36*3;
    private Paint mBgPaint;
    
    public CenterTargetDrawable(Resources res, int resId) {
    	super(res, resId);
    	mRadius = res.getDimensionPixelSize(R.dimen.aurora_handle_item_radius);
     	mBgPaint = new Paint();
    	mBgPaint.setColor(mBgcolor);
    	mBgPaint.setAntiAlias(true);
    }

    public CenterTargetDrawable(TargetDrawable other) {
    	super(other);  
    }

    public void draw(Canvas canvas) {    
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(getScaleX(), getScaleY(), mTranslationX + mPositionX, mTranslationY + mPositionY);
        canvas.translate(mPositionX, mPositionY);
        mBgPaint.setAlpha((int) Math.round(mAlpha * 255f));
        canvas.drawCircle(0, 0, mRadius, mBgPaint);
        canvas.restore();
//        super.draw(canvas);
        
        if (mDrawable == null || !mEnabled) {
            return;
        }
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
//        canvas.scale(getScaleX(), getScaleY(), mTranslationX + mPositionX, mTranslationY + mPositionY);
    
//        Log.i("guangyulirotate" , "mRotation =  " + mRotation);
        canvas.rotate(mRotation > -30.0f ? mRotation : (-mRotation-60.0f), mTranslationX + mPositionX, mTranslationY + mPositionY);
        canvas.translate(mTranslationX + mPositionX, mTranslationY + mPositionY);
        canvas.translate(-0.5f * getWidth(), -0.5f * getHeight());
        mDrawable.setAlpha((int) Math.round(mAlpha * 255f));
        mDrawable.draw(canvas);
        canvas.restore();
    }
}
