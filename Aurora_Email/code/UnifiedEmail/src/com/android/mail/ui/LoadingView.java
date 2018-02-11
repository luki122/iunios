package com.android.mail.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class LoadingView extends ImageView implements Runnable {

	int mDegree = 0;

	private Handler mHandler = null;
	private final int mPeriod = 80;
	
	public LoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.setScaleType(ScaleType.CENTER);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int nWidth = getWidth()/2;
		int nHeight = getHeight()/2;
		canvas.rotate(mDegree, nWidth, nHeight);
		Drawable drawable = getDrawable();
		if (drawable != null) {
			drawable.draw(canvas);
		}
	}
	
	@Override
	public void run() {
		mDegree += 30;
		mDegree %= 360;
		postInvalidate();
		if (mHandler != null) {
			mHandler.postDelayed(this, mPeriod);
		}
	}
	
	/*
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		mDegree = 0;
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
		
		if (visibility == View.VISIBLE) {
			mHandler = new Handler();
			mHandler.postDelayed(this, mPeriod);
		}
		
		// TODO Auto-generated method stub
		super.onWindowVisibilityChanged(visibility);
	}
	*/
	
	public void startLoading() {
		stopLoading();
		if(this.getVisibility() != View.VISIBLE) return;
		mHandler = new Handler();
		mHandler.postDelayed(this, mPeriod);
	}
	
	public void stopLoading() {
		mDegree = 0;
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
	}
}
