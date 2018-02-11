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

package com.android.incallui;

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
    private Paint[] mPaint;
    private float mScale = 1.0f;
    private static final float PI = (float) Math.PI;
    private Drawable[] mDrawable;
    private int[] mAlpha;
	private int r = 150;
	private int gap = 24;
	private int pr = 14;
	final private int[] anim = {0, 0, 0 ,1 ,2};

    // These allow us to have multiple concurrent animations.
    PointManager mPointManager = new PointManager();

    public class PointManager {
        private int point = 0;
        private float alpha = 0.0f;

        public void setPoint(int r) {
        	point = r;
        }

        public int getPoint() {
            return point;
        }        

        public void setAlpha(float a) {
            alpha = a;
        }

        public float getAlpha() {
            return alpha;
        }
    };

    public AuroraPointWhenTouch(Context context) {
        mPaint = new Paint[3];
//        mPaint.setFilterBitmap(true);
//        mPaint.setColor(Color.rgb(255, 255, 255)); // TODO: make configurable
//        mPaint.setAntiAlias(true);
//        mPaint.setDither(true);
        mPaint[0] = new Paint();
        mPaint[0].setColor(0x33FFFFFF);
        mPaint[2] = new Paint();
        mPaint[2].setColor(0x7FFFFFFF);
        mPaint[1] = new Paint();
        mPaint[1].setColor(0xCCFFFFFF);
        
//        mDrawable = new Drawable[3];
//        mDrawable[0] = context.getResources().getDrawable(R.drawable.aurora_point_touch_1);
//        mDrawable[1] = context.getResources().getDrawable(R.drawable.aurora_point_touch_2);
//        mDrawable[2] = context.getResources().getDrawable(R.drawable.aurora_point_touch_3);
//        mAlpha =  new int[3];
   
        r = context.getResources().getDimensionPixelSize(R.dimen.aurora_point_radius_touch);
        gap = context.getResources().getDimensionPixelSize(R.dimen.aurora_point_touch_gap);
        pr = context.getResources().getDimensionPixelSize(R.dimen.aurora_point_radius);  
        
        Log.i("guangyu" , "pr = " + pr);
        
//        mDrawable[0].setBounds(0, 0,
//        		mDrawable[0].getIntrinsicWidth(), mDrawable[0].getIntrinsicHeight());
//        mDrawable[1].setBounds(0, 0,
//        		mDrawable[1].getIntrinsicWidth(), mDrawable[1].getIntrinsicHeight());
//        mDrawable[2].setBounds(0, 0,
//        		mDrawable[2].getIntrinsicWidth(), mDrawable[2].getIntrinsicHeight());
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
//        canvas.translate(-0.5f * getWidth(mDrawable[0]), -0.5f * getHeight(mDrawable[0]));

//        if(mPointManager.getPoint() > 800) {
//        	 mAlpha[0] = 255;
//        	 mAlpha[1] = 255;
//        	 mAlpha[2] = (int)((mPointManager.getPoint() - 800) * 0.78);
//        } else if(mPointManager.getPoint() > 600) {
//	       	 mAlpha[0] = 255;
//	    	 mAlpha[1] = 255;
//	    	 mAlpha[2] = 0;
//        } else if (mPointManager.getPoint() > 400) {
//        	mAlpha[0] = 255;
//        	mAlpha[1] = (int)((mPointManager.getPoint() - 400) * 0.78);
//        	mAlpha[2] = 0;
//        } else if (mPointManager.getPoint() > 200) {
//        	mAlpha[0] = 255;
//        	mAlpha[1] = 0;
//        	mAlpha[2] = 0;
//        } else {
//        	mAlpha[0] = (int)(mPointManager.getPoint() * 0.78);
//        	mAlpha[1] = 0;
//        	mAlpha[2] = 0;
//        }
//        canvas.translate(0, -gap - pr);
//        mDrawable[0].setAlpha(mAlpha[0]);
//        mDrawable[0].draw(canvas);
//        canvas.translate(0, -gap - 2 * pr);
//        mDrawable[1].setAlpha(mAlpha[1]);
//        mDrawable[1].draw(canvas);
//        canvas.translate(0, -gap - 2 * pr);
//        mDrawable[2].setAlpha(mAlpha[2]);
//        mDrawable[2].draw(canvas);
//        
//        canvas.translate(0, 4 * gap + 2 * r + 6 * pr);
//        mDrawable[0].setAlpha(mAlpha[0]);
//        mDrawable[0].draw(canvas);
//        canvas.translate(0, gap + 2 * pr);
//        mDrawable[1].setAlpha(mAlpha[1]);
//        mDrawable[1].draw(canvas);
//        canvas.translate(0, gap + 2 * pr);
//        mDrawable[2].setAlpha(mAlpha[2]);
//        mDrawable[2].draw(canvas);
        int x = anim[mPointManager.getPoint() % 5];
        int y = anim[(mPointManager.getPoint() + 1) % 5];
        int z = anim[(mPointManager.getPoint() + 2) % 5];
        
      canvas.translate(0, -gap - pr);
      canvas.drawCircle(0, 0, pr/2 , mPaint[z]);
      canvas.translate(0, -gap - 2 * pr);
      canvas.drawCircle(0, 0, pr/2 , mPaint[y]);
      canvas.translate(0, -gap - 2 * pr);
      canvas.drawCircle(0, 0, pr/2, mPaint[x]);
      
      canvas.translate(0, 4 * gap + 2 * r + 6 * pr);
      canvas.drawCircle(0, 0, pr/2, mPaint[z]);
      canvas.translate(0, gap + 2 * pr);
      canvas.drawCircle(0, 0, pr/2, mPaint[y]);
      canvas.translate(0, gap + 2 * pr);
      canvas.drawCircle(0, 0, pr/2, mPaint[x]);
        
        
        canvas.restore();
    }
    
    public int getWidth(Drawable drawable) {
        return drawable != null ? drawable.getIntrinsicWidth() : 0;
    }

    public int getHeight(Drawable drawable) {
        return drawable != null ? drawable.getIntrinsicHeight() : 0;
    }

}
