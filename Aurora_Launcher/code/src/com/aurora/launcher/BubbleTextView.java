package com.aurora.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.aurora.plugin.DynIconPlg;
import com.aurora.util.DeviceProperties;


/**
 * 
 * @author xiejun Application Icon 设计 Add
 * 
 */
public class BubbleTextView extends ShadowTextView {
    public static int REAL_ICON_HEIGHT = -1;
    public static int REAL_ICON_HEIGHT_INCLUDE_ALPHA_ZOME = -1;
    private float factor = 1.0f;
    
	public BubbleTextView(Context context) {
		super(context);
	}

	public BubbleTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BubbleTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void init() {
		super.init();
		boolean b = DeviceProperties.isNeedScale();
		factor = 1.0f;
		if(b)factor = 0.93f;
		REAL_ICON_HEIGHT =(int) (getResources().getDimensionPixelSize(R.dimen.app_icon_real_size)*factor);
		REAL_ICON_HEIGHT_INCLUDE_ALPHA_ZOME = (int)(getResources().getDimensionPixelSize(R.dimen.app_icon_size)*factor);
	}
	
	public boolean doTouch(MotionEvent event){
		return super.onTouchEvent(event);
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		final int scrollX = getScrollX();
		final int scrollY = getScrollY();
		final Resources res = getContext().getResources();
		
		int width = REAL_ICON_HEIGHT;
		int realWidth = REAL_ICON_HEIGHT_INCLUDE_ALPHA_ZOME;
		int widthOfmCheckDrawable = 0;
		int heightOfmCheckDrawable = 0;
		Drawable mSignDrawable = null;
		float desiredWidth = getTextDediredWiddth();
		if (mIsChecked) {
			mSignDrawable = res.getDrawable(R.drawable.check);
			widthOfmCheckDrawable = mSignDrawable.getIntrinsicWidth();
			heightOfmCheckDrawable = mSignDrawable.getIntrinsicHeight();
			int left = (getWidth()-width)/2+width-widthOfmCheckDrawable/2-1;
			int top = getPaddingTop()+(realWidth-width)/2-widthOfmCheckDrawable/2+1;
			top = top<=0?0:top;
			int right = left+widthOfmCheckDrawable;
			int buttom = top+widthOfmCheckDrawable;
			if(right>getWidth()){
				left = getWidth()-widthOfmCheckDrawable;
				right = getWidth();
			}
			mSignDrawable.setBounds(left, top, right, buttom);
		} else {
			if (mIsNewApp) {
				/*
				mSignDrawable = res.getDrawable(R.drawable.sign_new_app);
				widthOfmCheckDrawable = mSignDrawable.getIntrinsicWidth();
				heightOfmCheckDrawable = mSignDrawable.getIntrinsicHeight();
				int left = (getWidth()-width)/2+width-widthOfmCheckDrawable/2-1;
				int top = getPaddingTop()+(realWidth-width)/2-widthOfmCheckDrawable/2+1;
				top = top<=0?0:top;
				int right = left+widthOfmCheckDrawable;
				int buttom = top+widthOfmCheckDrawable;
				if(right>getWidth()){
					left = getWidth()-widthOfmCheckDrawable;
					right = getWidth();
				}
				mSignDrawable.setBounds(left, top, right, buttom);
				*/
				if(getCurrentTextColor() != getResources().getColor(
						android.R.color.transparent)){
					mSignDrawable = res.getDrawable(R.drawable.new_dock);
					widthOfmCheckDrawable = mSignDrawable.getIntrinsicWidth();
					heightOfmCheckDrawable = mSignDrawable.getIntrinsicHeight();
					int drawablePadding=getCompoundDrawablePadding();
					int left = (getWidth()-(int)desiredWidth)/2-getPaddingLeft()-10;
					if(left<0)left=0;
					int top = getPaddingTop() + realWidth + drawablePadding+4;
					int right = left+widthOfmCheckDrawable;
					int buttom = top+heightOfmCheckDrawable;
					mSignDrawable.setBounds(left, top, right, buttom);
				}
			}
		}
		if (mSignDrawable != null) {
			if ((scrollX | scrollY) == 0) {
				mSignDrawable.draw(canvas);
			} else {
				canvas.translate(scrollX, scrollY);
				mSignDrawable.draw(canvas);
				canvas.translate(-scrollX, -scrollY);
			}
		}
	}

	private boolean mIsCheckable = false;
	private boolean mIsChecked = false;
	private boolean mIsNewApp = false;
	public void setChecked() {
		mIsChecked = !mIsChecked;
		invalidate();
	}

	public void setChecked(boolean checked) {
		if (mIsChecked == checked)
			return;
		mIsChecked = checked;
		invalidate();
	}
	
	public boolean isChecked(){
		return mIsChecked;
	}

	public void setNewApp(boolean isNewApp) {
		if (mIsNewApp == isNewApp)
			return;
		mIsNewApp = isNewApp;
		invalidate();
	}

	public void setCheckable() {
		mIsCheckable = true;
		invalidate();
	}

	public void clearCheckFlag() {
		mIsChecked = false;
		invalidate();
	}

	//vulcan added in 2014-6-6
	//fucntion: dynamic icon such as clock, calendor and weather.
	public DynIconPlg mDynIconPlg = null;

	@Override
	protected void onAttachedToWindow() {

		super.onAttachedToWindow();
		if (mDynIconPlg != null) {
			Log.d("vulcan-iconlist","BubbleTextView:onAttachedToWindow, this is: " + this);
			mDynIconPlg.onAttachedToWindow();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mDynIconPlg != null) {
			Log.d("vulcan-iconlist","BubbleTextView:onDetachedFromWindow, this is: " + this);
			mDynIconPlg.onDetachedFromWindow();
		}
	}
	// AURORA-END:
}
