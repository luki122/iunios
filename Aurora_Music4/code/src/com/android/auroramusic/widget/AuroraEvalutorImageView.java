package com.android.auroramusic.widget;

import java.util.Random;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AuroraEvalutorImageView extends ImageView {
	private static final String TAG = "AuroraAnimationImageView";
	private Context mContext = null;
	private float mCurrentDegree = 0f;
	private int mAlaph;
	private int mType;
	private int direction;
	private Random mRandom;
	private float scale = 0.5f;
	private int scaleflag = 0;

	public AuroraEvalutorImageView(Context context) {
		this(context, null, 0);
	}

	public AuroraEvalutorImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AuroraEvalutorImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

		mContext = context;
		mCurrentDegree = 0;
		mRandom = new Random();
		/*
		 * mContext = context; mMatrix = new Matrix(); mCenterWidth =
		 * this.getWidth()/2; mCenterHeight = this.getHeight()/2;
		 */
	}

	public void initDegree() {
		mCurrentDegree = 0;
		return;
	}

	public void setStartAnimation(boolean animation) {
		invalidate();
		return;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Drawable drawable = getDrawable();
		if (drawable == null)
			return;

		Rect bounds = drawable.getBounds();
		int w = bounds.right - bounds.left;
		int h = bounds.bottom - bounds.top;
		if (w == 0 || h == 0)
			return;

		if(scaleflag==0&&scale<1){
				scale += 0.2;
		}else{
				if(scaleflag==1&&scale>0.8){
					scale -= 0.08;
			}
		}
		if (direction == 0) {
			if (mType == 0)
				mCurrentDegree += mRandom.nextInt(9)*0.1f;
			else
				mCurrentDegree += mRandom.nextInt(6)*0.1f;
		} else {
			if (mType == 0)
				mCurrentDegree -= mRandom.nextInt(9)*0.1f;
			else
				mCurrentDegree -= mRandom.nextInt(6)*0.1f;
		}
		if(mCurrentDegree>30){
			mCurrentDegree = 30;
		}
		else if(mCurrentDegree<-30){
			mCurrentDegree = -30;
		}
		int left = getPaddingLeft();
		int top = getPaddingTop();
		int right = getPaddingRight();
		int bottom = getPaddingBottom();
		int width = getWidth() - left - right;
		int height = getHeight() - top - bottom;
		drawable.setAlpha(mAlaph);
//		canvas.translate(left + width / 2, top + height / 2);
//		canvas.rotate(mCurrentDegree);
//		canvas.scale(scale,scale, left + width / 2, top + height / 2);
//		canvas.translate(-w / 2, -h / 2);
		drawable.draw(canvas);
	}

	public void refresh(int alaph, int type,int scaleType) {
		mAlaph = alaph;
		mType = type;
		this.scaleflag = scaleType;
		invalidate();
	}

	public void setStartValue(int direction) {
		this.direction = direction;
		mCurrentDegree = 0;
		scale = 0.5f;
	}
}
