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

public class AuroraPointWhenTouch {
    private static final float MIN_POINT_SIZE = 2.0f;
    private static final float MAX_POINT_SIZE = 4.0f;
    private static final int INNER_POINTS = 8;
    private static final String TAG = "AuroraPointWhenTouch";
    private float mCenterX;
    private float mCenterY;
    private Paint mPaint;
    private float mTranslationY = 0.0f;
    private static final float PI = (float) Math.PI;
    private Drawable[] mDrawable;
	private int r = 150;
	private int gap = 24;
	private int pr = 14;

	
	private int mColor, mRedColor;
	private int mIndex;
	AlphaManager mAlphaManager = new AlphaManager();
	ScaleManager mScaleManager = new ScaleManager();

    public AuroraPointWhenTouch(Context context, int index) {
    	mIndex = index;
    	 mColor = PhoneGlobals.getInstance().getResources().getColor(R.color.aurora_green_color_v2);
         mRedColor = PhoneGlobals.getInstance().getResources().getColor(R.color.aurora_end_call_color_v2);
        mPaint = new Paint();
        mPaint.setColor(mColor);
        mPaint.setAntiAlias(true);
        r = context.getResources().getDimensionPixelSize(R.dimen.aurora_point_radius_touch);  
        gap = context.getResources().getDimensionPixelSize(R.dimen.aurora_point_touch_gap);
        pr = context.getResources().getDimensionPixelSize(R.dimen.aurora_point_radius);  
        
        Log.i("guangyu" , "pr = " + pr);
        
        restoreY();
       
    }
    
    public class AlphaManager {
		private float alpha = 0.0f;

		public void setAlpha(float a) {
			alpha = a;
		}

		public float getAlpha() {
			return alpha;
		}
	};
	
	 public class ScaleManager {
			private float scale = 0.0f;

			public void setScale(float a) {
				scale = a;
			}

			public float getScale() {
				return scale;
			}
		};
    
    
    
    public int getIndex() {
    	return mIndex;
    }
    

    public void setCenter(float x, float y) {
        mCenterX = x;
        mCenterY = y;
    }
    

    public void setY(float y) {
        mTranslationY = y;
    }
    
    
    public void restoreY() {
    	switch(mIndex) {
	      	case 0:{
	      		setY(-r);
	      		break;
	      	}
	      	case 1:{
	      		setY(-r - gap);
	      		break;
	      	}
	      	case 2:{
	      		setY(-r - 2 * gap);
	      		break;
	      	}
	     }
      
    }
    
    public float getY() {
        return mTranslationY;
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
    	Log.i(TAG, "alpha = " + mAlphaManager.getAlpha() + " mScale = " + mScaleManager.getScale());
        canvas.save(Canvas.MATRIX_SAVE_FLAG);        
        canvas.translate(mCenterX, mTranslationY + mCenterY);
        canvas.scale(mScaleManager.getScale(), mScaleManager.getScale()); 
        int a = (int)(mAlphaManager.getAlpha() * 255);
        mPaint.setColor(mRedColor);
        mPaint.setAlpha(a);      
        canvas.drawCircle(0, 0, pr/2, mPaint);     
        canvas.restore();
        
        canvas.save(Canvas.MATRIX_SAVE_FLAG);        
        canvas.translate(mCenterX,  mCenterY - mTranslationY );
        canvas.scale(mScaleManager.getScale(), mScaleManager.getScale());   
        mPaint.setColor(mColor);
        mPaint.setAlpha(a);      
        canvas.drawCircle(0, 0, pr/2, mPaint);
        canvas.restore();
    }
    
    public int getWidth(Drawable drawable) {
        return drawable != null ? drawable.getIntrinsicWidth() : 0;
    }

    public int getHeight(Drawable drawable) {
        return drawable != null ? drawable.getIntrinsicHeight() : 0;
    }

}
