/*
 * Copyright (C) 2008 ZXing authors
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

package com.aurora.voiceassistant.view;

import com.google.zxing.ResultPoint;
import com.google.zxing.camera.CameraManager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.graphics.drawable.BitmapDrawable;

import java.util.Collection;
import java.util.HashSet;

import com.aurora.voiceassistant.*;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

	private static final int[] SCANNER_ALPHA = { 0, 64, 128, 192, 255, 192,
			128, 64 };
//	private static final long ANIMATION_DELAY = 100L;
	private static final long ANIMATION_DELAY = 2L;
	private static final int OPAQUE = 0xFF;

	/**
	 * 四个绿色边角对应的长度
	 */
//	private int ScreenRate;

	/**
	 * 四个绿色边角对应的宽度
	 */
	// private static final int CORNER_WIDTH = 10;

	/**
	 * 扫描框中的中间线的宽度
	 */
	private static final int MIDDLE_LINE_WIDTH = 6;

	/**
	 * 扫描框中的中间线的与扫描框左右的间隙
	 */
	private static final int MIDDLE_LINE_PADDING = 5;

	/**
	 * 中间那条线每次刷新移动的距离
	 */
	// private static final int SPEEN_DISTANCE = 5;
	private static final int SPEEN_DISTANCE = 8;

	/**
	 * 中间滑动线的最顶端位置
	 */
	private int slideTop;

	/**
	 * 中间滑动线的最底端位置
	 */
	private int slideBottom;

	private final Paint paint;
	private Bitmap resultBitmap;
	private final int maskColor;
	private final int resultColor;
//	private final int frameColor;
//	private final int laserColor;
//	private final int resultPointColor;
//	private int scannerAlpha;
//	private Collection<ResultPoint> possibleResultPoints;
//	private Collection<ResultPoint> lastPossibleResultPoints;

	private Resources resources;
	private int borderWidth;
	private int borderHeight;
	private int scanningLineHeight;
//	private String scanningTips;
//	private int scanningTipSize;
//	private float scanningTipsMarginTop;

	private boolean drawViewDone = false;

	// This constructor is used when the class is built from an XML resource.
	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// Initialize these once for performance rather than calling them every
		// time in onDraw().
		paint = new Paint();
		resources = getResources();
		maskColor = resources.getColor(R.color.vs_barcode_scanning_background);
		resultColor = resources.getColor(R.color.vs_barcode_result_view);
//		frameColor = resources.getColor(R.color.viewfinder_frame);
//		laserColor = resources.getColor(R.color.viewfinder_laser);
//		scannerAlpha = 0;

		borderWidth = (int) resources.getDimension(R.dimen.vs_barcode_scanning_border_width);
		borderHeight = (int) resources.getDimension(R.dimen.vs_barcode_scanning_border_height);
		scanningLineHeight = ((BitmapDrawable)(getResources().getDrawable(R.drawable.vs_barcode_scanning_line))).getBitmap().getHeight();
//		scanningTips = resources.getString(R.string.scanning_tips);
//		scanningTipSize = (int) resources.getDimension(R.dimen.scanning_tips_size);
//		scanningTipsMarginTop = resources.getDimension(R.dimen.scanning_tips_margin_top);

		// result point will be delete later
//		resultPointColor = resources.getColor(R.color.possible_result_points);
//		possibleResultPoints = new HashSet<ResultPoint>(5);

	}
	
	@Override
	public void onDraw(Canvas canvas) {
		Rect frame = CameraManager.get().getFramingRect();
		
		if (frame == null) {
			frame = new Rect(0, 0, getWidth(), getHeight());
//			return;
		}
		
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		
		//draw the marsk until camera has been opened and drawView has been invalidated completely
		if (!drawViewDone) {
			paint.setColor(Color.BLACK);
			canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
			
		} else {
			paint.setColor(maskColor);
			canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

			// Draw the exterior (i.e. outside the framing rect) darkened
			paint.setColor(resultBitmap != null ? resultColor : maskColor);
			canvas.drawRect(0, 0, width, frame.top, paint);
			canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
			canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
			canvas.drawRect(0, frame.bottom + 1, width, height, paint);
			
			invalidate();

		}
		
		if (resultBitmap != null) {
			// Draw the opaque result bitmap over the scanning rectangle
			// paint.setAlpha(OPAQUE);
			// canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);

		} else {

			// Draw a two pixel solid black border inside the framing rect
			/*paint.setColor(frameColor);
			canvas.drawRect(frame.left, frame.top, frame.right + 1, frame.top + 2, paint);
			canvas.drawRect(frame.left, frame.top + 2, frame.left + 2, frame.bottom - 1, paint);
			canvas.drawRect(frame.right - 1, frame.top, frame.right + 1, frame.bottom - 1, paint);
			canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1, frame.bottom + 1, paint);*/

			/*paint.setColor(Color.RED);
		    Rect rect = CameraManager.get().getFramingRectInPreview();
		    canvas.drawRect(rect.left, rect.top, rect.right + 1, rect.top + 2, paint);
		    canvas.drawRect(rect.left, rect.top + 2, rect.left + 2, rect.bottom - 1, paint);
		    canvas.drawRect(rect.right - 1, rect.top, rect.right + 1, rect.bottom - 1, paint);
		    canvas.drawRect(rect.left, rect.bottom - 1, rect.right + 1, rect.bottom + 1, paint);*/
			
			//draw the marsk until camera has been opened and drawView has been invalidated completely
			if (drawViewDone) {
				// 画扫描框边上的角，总共8个部分
				paint.setColor(resources.getColor(R.color.vs_barcode_scanning_border_frame));
				canvas.drawRect(frame.left, frame.top, frame.left + borderWidth, frame.top + borderHeight, paint);
				canvas.drawRect(frame.left, frame.top, frame.left + borderHeight, frame.top + borderWidth, paint);
				canvas.drawRect(frame.right - borderWidth, frame.top, frame.right, frame.top + borderHeight, paint);
				canvas.drawRect(frame.right - borderHeight, frame.top, frame.right, frame.top + borderWidth, paint);
				canvas.drawRect(frame.left, frame.bottom - borderHeight, frame.left + borderWidth, frame.bottom, paint);
				canvas.drawRect(frame.left, frame.bottom - borderWidth + 0, frame.left + borderHeight, frame.bottom, paint);
				canvas.drawRect(frame.right - borderWidth + 0, frame.bottom - borderHeight, frame.right, frame.bottom, paint);
				canvas.drawRect(frame.right - borderHeight + 0, frame.bottom - borderWidth, frame.right, frame.bottom, paint);
	
				// 绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
				slideTop += SPEEN_DISTANCE;
				if (slideTop <= frame.top) {
					slideTop = frame.top;
				}
				if (slideTop >= frame.bottom) {
					slideTop = frame.top;
				}
				
				Rect lineRect = new Rect();
				lineRect.left = frame.left;
				lineRect.right = frame.right;
				lineRect.top = slideTop;
				lineRect.bottom = slideTop + scanningLineHeight;
				canvas.drawBitmap(((BitmapDrawable)(getResources().getDrawable(R.drawable.vs_barcode_scanning_line))).getBitmap(), null, lineRect, paint);

			}
			
			//single line
			/*canvas.drawRect(frame.left + MIDDLE_LINE_PADDING, slideTop - MIDDLE_LINE_WIDTH / 2, 
					frame.right - MIDDLE_LINE_PADDING, slideTop + MIDDLE_LINE_WIDTH / 2, paint);*/
			
			//scanning tips
			/*paint.setColor(Color.WHITE);
			paint.setTextSize(scanningTipSize);
			float textWidth = paint.measureText(scanningTips);
			canvas.drawText(scanningTips, (width - textWidth)/2, (float) (frame.bottom + scanningTipsMarginTop), paint);*/

			// delete the yellow point when get preview from camera at scanning
			/*Collection<ResultPoint> currentPossible = possibleResultPoints;
			Collection<ResultPoint> currentLast = lastPossibleResultPoints;
			if (currentPossible.isEmpty()) { 
				lastPossibleResultPoints = null;
			} else {
				possibleResultPoints = new HashSet<ResultPoint>(5);
				lastPossibleResultPoints = currentPossible;
			 	paint.setAlpha(OPAQUE); paint.setColor(resultPointColor); 
			 	for (ResultPoint point : currentPossible) {
			 		canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, paint); 
			 		} 
			 	} 
			if (currentLast != null) {
				paint.setAlpha(OPAQUE / 2); paint.setColor(resultPointColor); 
				for	(ResultPoint point : currentLast) { 
					canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
				} 
			}*/

			// Request another update at the animation interval, but only
			// repaint the laser line,
			// not the entire viewfinder mask.
			postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
		}
	}

	public void drawViewfinder() {
		drawViewDone = false;
		resultBitmap = null;
		invalidate();
		//make sure the operation of invalidating has done
		if (getHandler() != null) {
			getHandler().postDelayed(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					drawViewDone = true;
				}
			}, 180);
		}
		
	}

	/**
	 * Draw a bitmap with the result points highlighted instead of the live
	 * scanning display.
	 * 
	 * @param barcode
	 *            An image of the decoded barcode.
	 */
	public void drawResultBitmap(Bitmap barcode) {
		resultBitmap = barcode;
		invalidate();
	}

	/*public void addPossibleResultPoint(ResultPoint point) {
		possibleResultPoints.add(point);
	}*/

}
