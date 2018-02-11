/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.gallery3d.filtershow.crop;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.Log;

public abstract class CropDrawingUtils {

	public static void drawRuleOfThird(Canvas canvas, RectF bounds) {
        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.argb(128, 233, 233, 233));
        p.setStrokeWidth(2);
        float stepX = bounds.width() / 3.0f;
        float stepY = bounds.height() / 3.0f;
        float x = bounds.left + stepX;
        float y = bounds.top + stepY;
        for (int i = 0; i < 2; i++) {
            canvas.drawLine(x, bounds.top, x, bounds.bottom, p);
            x += stepX;
        }
        for (int j = 0; j < 2; j++) {
            canvas.drawLine(bounds.left, y, bounds.right, y, p);
            y += stepY;
        }
    }
	
    public static void drawRuleOfThird(Canvas canvas, RectF bounds, int alpha) {
        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.argb(alpha / 2, 233, 233, 233));//e9e9e9-->(233,233,233)
        p.setStrokeWidth(2);
        float stepX = bounds.width() / 3.0f;
        float stepY = bounds.height() / 3.0f;
        float x = bounds.left + stepX;
        float y = bounds.top + stepY;
        for (int i = 0; i < 2; i++) {
            canvas.drawLine(x, bounds.top, x, bounds.bottom, p);
            x += stepX;
        }
        for (int j = 0; j < 2; j++) {
            canvas.drawLine(bounds.left, y, bounds.right, y, p);
            y += stepY;
        }
    }

    public static void drawCropRect(Canvas canvas, RectF bounds) {
        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.parseColor("#e9e9e9"));//p.setColor(Color.WHITE);
        p.setStrokeWidth(DRAW_WIDTH);
        canvas.drawRect(bounds, p);
    }

    public static void drawCropRect(Canvas canvas, RectF bounds, int alpha) {
        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.parseColor("#e9e9e9"));//p.setColor(Color.WHITE);
        p.setStrokeWidth(DRAW_WIDTH);
        p.setAlpha(alpha);
        canvas.drawRect(bounds, p);
    }

    //paul add start
    private static final int DRAW_WIDTH = 4;
    private static final int HALF_DRAW_WIDTH = DRAW_WIDTH >> 1;
        
    /*
    public static void drawCorners(Canvas canvas, RectF bounds, float wOffset, float hOffset, int alpha) {
        float[] p1 = {
                bounds.left + wOffset, bounds.top + wOffset,
                bounds.right - wOffset, bounds.top + wOffset,
                bounds.right - wOffset, bounds.bottom - wOffset,
                bounds.left + wOffset, bounds.bottom - wOffset
        };

        float[] p2 = {
				p1[0] + hOffset, p1[1],
                p1[2], p1[3] + hOffset,
                p1[4] - hOffset, p1[5],
				p1[6], p1[7] - hOffset
        };

        float[] p3 = {
				p1[0], p1[1] + hOffset,
                p1[2] - hOffset, p1[3],
                p1[4], p1[5] - hOffset,
				p1[6] + hOffset, p1[7]
        };
		
		float offset = wOffset + hOffset;
        float[] p4 = {
                bounds.left + offset, bounds.top,
                bounds.right, bounds.top + offset,
                bounds.right - offset, bounds.bottom,
                bounds.left, bounds.bottom - offset
        };
		
        float[] p5 = {
                bounds.left, bounds.top + offset,
                bounds.right - offset, bounds.top,
                bounds.right, bounds.bottom - offset,
                bounds.left + offset, bounds.bottom
        };


        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.parseColor("#e9e9e9"));//p.setColor(Color.WHITE);
        p.setStrokeWidth(DRAW_WIDTH);
		p.setAlpha(alpha);
        for (int i = 0; i < 8; i += 2) {
            canvas.drawLine(p4[i], p4[i+1], p2[i], p2[i+1] + HALF_DRAW_WIDTH, p);
			canvas.drawLine(p2[i] + HALF_DRAW_WIDTH, p2[i+1], p1[i] - HALF_DRAW_WIDTH, p1[i+1], p);
			canvas.drawLine(p1[i], p1[i+1] - HALF_DRAW_WIDTH, p3[i], p3[i+1] + HALF_DRAW_WIDTH, p);
			canvas.drawLine(p3[i] + HALF_DRAW_WIDTH, p3[i+1], p5[i], p5[i+1], p);
        }

    }
    */
        
	public static void drawCorners(Canvas canvas, RectF bounds, float wOffset, float hOffset, int alpha) {
		float[] p1 = { 
				bounds.left + wOffset, bounds.top + wOffset,
				bounds.right - wOffset, bounds.top + wOffset,
				bounds.right - wOffset, bounds.bottom - wOffset,
				bounds.left + wOffset, bounds.bottom - wOffset };

		float[] p2 = { 
				p1[0] + hOffset, p1[1], 
				p1[2], p1[3] + hOffset,
				p1[4] - hOffset, p1[5], 
				p1[6], p1[7] - hOffset };

		float[] p3 = { 
				p1[0], p1[1] + hOffset, 
				p1[2] - hOffset, p1[3], 
				p1[4], p1[5] - hOffset, 
				p1[6] + hOffset, p1[7] };

		float offset = wOffset + hOffset;
		float[] p4 = { 
				bounds.left + offset, bounds.top, 
				bounds.right, bounds.top + offset, 
				bounds.right - offset, bounds.bottom,
				bounds.left, bounds.bottom - offset };

		float[] p5 = { 
				bounds.left, bounds.top + offset, 
				bounds.right - offset,
				bounds.top, bounds.right, bounds.bottom - offset,
				bounds.left + offset, bounds.bottom };

		Paint p = new Paint();
		p.setStyle(Paint.Style.STROKE);
		p.setColor(Color.parseColor("#e9e9e9"));// p.setColor(Color.WHITE);
		p.setStrokeWidth(DRAW_WIDTH);
		p.setAlpha(alpha);

		// draw leftTop:
		canvas.drawLine(p4[0], p4[1], p2[0], p2[1] + HALF_DRAW_WIDTH, p);
		canvas.drawLine(p2[0], p2[1], p1[0] - HALF_DRAW_WIDTH, p1[1], p);
		canvas.drawLine(p1[0], p1[1], p3[0], p3[1] + HALF_DRAW_WIDTH, p);
		canvas.drawLine(p3[0], p3[1], p5[0], p5[1], p);

		// draw rightTop:
		canvas.drawLine(p4[2], p4[3], p2[2] - HALF_DRAW_WIDTH, p2[3], p);
		canvas.drawLine(p2[2], p2[3], p1[2], p1[3] - HALF_DRAW_WIDTH, p);
		canvas.drawLine(p1[2], p1[3], p3[2] - HALF_DRAW_WIDTH, p3[3], p);
		canvas.drawLine(p3[2], p3[3], p5[2], p5[3], p);

		// draw rightBottom:
		canvas.drawLine(p4[4], p4[5], p2[4], p2[5] - HALF_DRAW_WIDTH, p);
		canvas.drawLine(p2[4], p2[5], p1[4] + HALF_DRAW_WIDTH, p1[5], p);
		canvas.drawLine(p1[4], p1[5], p3[4], p3[5] - HALF_DRAW_WIDTH, p);
		canvas.drawLine(p3[4], p3[5], p5[4], p5[5], p);

		// draw leftBottom:
		canvas.drawLine(p4[6], p4[7], p2[6] + HALF_DRAW_WIDTH, p2[7], p);
		canvas.drawLine(p2[6], p2[7], p1[6], p1[7] + HALF_DRAW_WIDTH, p);
		canvas.drawLine(p1[6], p1[7], p3[6] + HALF_DRAW_WIDTH, p3[7], p);
		canvas.drawLine(p3[6], p3[7], p5[6], p5[7], p);
	}
	//paul add end

    public static void drawShade(Canvas canvas, RectF bounds) {
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.BLACK & 0x88000000);

        RectF r = new RectF();
        r.set(0,0,w,bounds.top - HALF_DRAW_WIDTH);
        canvas.drawRect(r, p);
        r.set(0,bounds.top - HALF_DRAW_WIDTH ,bounds.left - HALF_DRAW_WIDTH,h);
        canvas.drawRect(r, p);
        r.set(bounds.left - HALF_DRAW_WIDTH ,bounds.bottom + HALF_DRAW_WIDTH,w,h);
        canvas.drawRect(r, p);
        r.set(bounds.right + HALF_DRAW_WIDTH,bounds.top - HALF_DRAW_WIDTH,w,bounds.bottom + HALF_DRAW_WIDTH);
        canvas.drawRect(r, p);
    }

    public static void drawIndicator(Canvas canvas, Drawable indicator, int indicatorSize,
            float centerX, float centerY) {
        int left = (int) centerX - indicatorSize / 2;
        int top = (int) centerY - indicatorSize / 2;
        indicator.setBounds(left, top, left + indicatorSize, top + indicatorSize);
        indicator.draw(canvas);
    }

    public static void drawIndicators(Canvas canvas, Drawable cropIndicator, int indicatorSize,
            RectF bounds, boolean fixedAspect, int selection) {
        boolean notMoving = (selection == CropObject.MOVE_NONE);
        if (fixedAspect) {
            if ((selection == CropObject.TOP_LEFT) || notMoving) {
                drawIndicator(canvas, cropIndicator, indicatorSize, bounds.left, bounds.top);
            }
            if ((selection == CropObject.TOP_RIGHT) || notMoving) {
                drawIndicator(canvas, cropIndicator, indicatorSize, bounds.right, bounds.top);
            }
            if ((selection == CropObject.BOTTOM_LEFT) || notMoving) {
                drawIndicator(canvas, cropIndicator, indicatorSize, bounds.left, bounds.bottom);
            }
            if ((selection == CropObject.BOTTOM_RIGHT) || notMoving) {
                drawIndicator(canvas, cropIndicator, indicatorSize, bounds.right, bounds.bottom);
            }
        } else {
            if (((selection & CropObject.MOVE_TOP) != 0) || notMoving) {
                drawIndicator(canvas, cropIndicator, indicatorSize, bounds.centerX(), bounds.top);
            }
            if (((selection & CropObject.MOVE_BOTTOM) != 0) || notMoving) {
                drawIndicator(canvas, cropIndicator, indicatorSize, bounds.centerX(), bounds.bottom);
            }
            if (((selection & CropObject.MOVE_LEFT) != 0) || notMoving) {
                drawIndicator(canvas, cropIndicator, indicatorSize, bounds.left, bounds.centerY());
            }
            if (((selection & CropObject.MOVE_RIGHT) != 0) || notMoving) {
                drawIndicator(canvas, cropIndicator, indicatorSize, bounds.right, bounds.centerY());
            }
        }
    }

    public static void drawWallpaperSelectionFrame(Canvas canvas, RectF cropBounds, float spotX,
            float spotY, Paint p, Paint shadowPaint) {
        float sx = cropBounds.width() * spotX;
        float sy = cropBounds.height() * spotY;
        float cx = cropBounds.centerX();
        float cy = cropBounds.centerY();
        RectF r1 = new RectF(cx - sx / 2, cy - sy / 2, cx + sx / 2, cy + sy / 2);
        float temp = sx;
        sx = sy;
        sy = temp;
        RectF r2 = new RectF(cx - sx / 2, cy - sy / 2, cx + sx / 2, cy + sy / 2);
        canvas.save();
        canvas.clipRect(cropBounds);
        canvas.clipRect(r1, Region.Op.DIFFERENCE);
        canvas.clipRect(r2, Region.Op.DIFFERENCE);
        canvas.drawPaint(shadowPaint);
        canvas.restore();
        Path path = new Path();
        path.moveTo(r1.left, r1.top);
        path.lineTo(r1.right, r1.top);
        path.moveTo(r1.left, r1.top);
        path.lineTo(r1.left, r1.bottom);
        path.moveTo(r1.left, r1.bottom);
        path.lineTo(r1.right, r1.bottom);
        path.moveTo(r1.right, r1.top);
        path.lineTo(r1.right, r1.bottom);
        path.moveTo(r2.left, r2.top);
        path.lineTo(r2.right, r2.top);
        path.moveTo(r2.right, r2.top);
        path.lineTo(r2.right, r2.bottom);
        path.moveTo(r2.left, r2.bottom);
        path.lineTo(r2.right, r2.bottom);
        path.moveTo(r2.left, r2.top);
        path.lineTo(r2.left, r2.bottom);
        canvas.drawPath(path, p);
    }

    public static void drawShadows(Canvas canvas, Paint p, RectF innerBounds, RectF outerBounds) {
        canvas.drawRect(outerBounds.left, outerBounds.top, innerBounds.right, innerBounds.top, p);
        canvas.drawRect(innerBounds.right, outerBounds.top, outerBounds.right, innerBounds.bottom,
                p);
        canvas.drawRect(innerBounds.left, innerBounds.bottom, outerBounds.right,
                outerBounds.bottom, p);
        canvas.drawRect(outerBounds.left, innerBounds.top, innerBounds.left, outerBounds.bottom, p);
    }

    public static Matrix getBitmapToDisplayMatrix(RectF imageBounds, RectF displayBounds) {
        Matrix m = new Matrix();
        CropDrawingUtils.setBitmapToDisplayMatrix(m, imageBounds, displayBounds);
        return m;
    }

    public static boolean setBitmapToDisplayMatrix(Matrix m, RectF imageBounds,
            RectF displayBounds) {
        m.reset();
        return m.setRectToRect(imageBounds, displayBounds, Matrix.ScaleToFit.CENTER);
    }

    public static boolean setImageToScreenMatrix(Matrix dst, RectF image,
            RectF screen, int rotation) {
        RectF rotatedImage = new RectF();
        dst.setRotate(rotation, image.centerX(), image.centerY());
        if (!dst.mapRect(rotatedImage, image)) {
            return false; // fails for rotations that are not multiples of 90
                          // degrees
        }
        boolean rToR = dst.setRectToRect(rotatedImage, screen, Matrix.ScaleToFit.CENTER);
        boolean rot = dst.preRotate(rotation, image.centerX(), image.centerY());
        return rToR && rot;
    }

}
