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

public class AuroraRing extends AuroraCircle {   
	private int mColor;
     private float mRingWidth, mr, mR;
	
    public AuroraRing() {
        super();
        mColor = PhoneGlobals.getInstance().getResources().getColor(R.color.aurora_green_color_v2);
        mPaint.setColor(mColor); 
        mPaint.setStyle(Paint.Style.STROKE);
    }

    public void draw(Canvas canvas) {
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(mScale, mScale, mCenterX, mCenterY);
        mPaint.setAlpha((int)(waveManager.getAlpha() * 255));
        mr = waveManager.getRadius();
        mR =  waveManager.getRadius2();
        mRingWidth = mR - mr;
        mPaint.setStrokeWidth(mRingWidth); 
        canvas.drawCircle(mCenterX, mCenterY, mr + mRingWidth/2, mPaint);
        canvas.restore();
    }

}
