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

public class AuroraPointWhenTouchInit {
	private static final float MIN_POINT_SIZE = 2.0f;
	private static final float MAX_POINT_SIZE = 4.0f;
	private static final int INNER_POINTS = 8;
	private static final String TAG = "AuroraPointWhenTouchInit";
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
	final private int[] anim = { 0, 0, 0, 1, 1, 1, 3, 2, 1, 1};

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

	public AuroraPointWhenTouchInit(Context context) {
		mPaint = new Paint[4];
		mPaint[0] = new Paint();
		mPaint[0].setColor(0x00FFFFFF);
		mPaint[1] = new Paint();
		mPaint[1].setColor(0x33FFFFFF);
		mPaint[2] = new Paint();
		mPaint[2].setColor(0x7FFFFFFF);
		mPaint[3] = new Paint();
		mPaint[3].setColor(0xCCFFFFFF);

		r = context.getResources().getDimensionPixelSize(
				R.dimen.aurora_point_radius_touch);
		gap = context.getResources().getDimensionPixelSize(
				R.dimen.aurora_point_touch_gap);
		pr = context.getResources().getDimensionPixelSize(
				R.dimen.aurora_point_radius);

		Log.i("guangyu", "pr = " + pr);

	}

	public void setCenter(float x, float y) {
		mCenterX = x;
		mCenterY = y;
	}

	public void setScale(float scale) {
		mScale = scale;
	}

	public float getScale() {
		return mScale;
	}


	public void draw(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.translate(mCenterX, mCenterY);
		canvas.scale(mScale, mScale, mCenterX, mCenterY);

		canvas.translate(0, -r);

//		int x = anim[mPointManager.getPoint()];
//		int y = anim[(mPointManager.getPoint() + 1)];
//		int z = anim[(mPointManager.getPoint() + 2)];
		int z = 1, y = 2, x = 3;
		float alphax = 0f;
		float alphay = 0f;
		float alphaz = 0f;
		if(mPointManager.getAlpha() <= 1) {
			alphaz = mPointManager.getAlpha();
		} else if(mPointManager.getAlpha() > 1 && mPointManager.getAlpha() <=2) {
			alphaz = 1f; 
			alphay = mPointManager.getAlpha() -1; 	
		} else {
			alphaz = 1f; 
			alphay = 1f;
			alphax = mPointManager.getAlpha() -2 ;
		}
		
		mPaint[z].setAlpha((int) Math.round(alphaz * 255f * 0.2));
		mPaint[y].setAlpha((int) Math.round(alphay * 255f * 0.5));
		mPaint[x].setAlpha((int) Math.round(alphax * 255f * 0.8));

		canvas.translate(0, -gap - pr);
		canvas.drawCircle(0, 0, pr / 2, mPaint[z]);
		canvas.translate(0, -gap - 2 * pr);
		canvas.drawCircle(0, 0, pr / 2, mPaint[y]);
		canvas.translate(0, -gap - 2 * pr);
		canvas.drawCircle(0, 0, pr / 2, mPaint[x]);

		canvas.translate(0, 4 * gap + 2 * r + 6 * pr);
		canvas.drawCircle(0, 0, pr / 2, mPaint[z]);
		canvas.translate(0, gap + 2 * pr);
		canvas.drawCircle(0, 0, pr / 2, mPaint[y]);
		canvas.translate(0, gap + 2 * pr);
		canvas.drawCircle(0, 0, pr / 2, mPaint[x]);

		canvas.restore();
	}

	public int getWidth(Drawable drawable) {
		return drawable != null ? drawable.getIntrinsicWidth() : 0;
	}

	public int getHeight(Drawable drawable) {
		return drawable != null ? drawable.getIntrinsicHeight() : 0;
	}

}
