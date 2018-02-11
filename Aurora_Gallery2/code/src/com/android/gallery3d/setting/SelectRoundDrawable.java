package com.android.gallery3d.setting;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

public class SelectRoundDrawable extends Drawable {

	private Paint mPaint;
	private Bitmap mBitmap;
	private RectF mRect;

	public SelectRoundDrawable(Bitmap bitmap) {
		mBitmap = bitmap;

		// 初始化BitmapShader
		BitmapShader mBitmapShader = new BitmapShader(bitmap,
				Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setShader(mBitmapShader);
	
	}

	@Override
	public void setBounds(int left, int top, int right, int bottom) {
		super.setBounds(left, top, right, bottom);
		// 在设置边界方法中，创建RectF
		mRect = new RectF(left, top, right, bottom);
	}

	@Override
	public void draw(Canvas canvas) {
		// 用设置了shader的画笔画圆角矩形
		canvas.drawRoundRect(mRect, 90, 90, mPaint);

	}

	/**
	 * 当布局为wrap_content时，默认返回图片的宽度
	 * 
	 * @return 图片宽度
	 */
	@Override
	public int getIntrinsicWidth() {
		return mBitmap.getWidth();
	}

	/**
	 * 当布局为wrap_content时，默认返回图片的高度
	 * 
	 * @return 图片高度
	 */
	@Override
	public int getIntrinsicHeight() {
		return mBitmap.getHeight();
	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);

	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);

	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
}