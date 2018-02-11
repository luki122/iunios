package com.aurora.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class QuickIndexLayout extends RelativeLayout {

	private boolean mDirection;

	private int mHeight;

	private Scroller mScroller;

	public QuickIndexLayout(Context context) {
		super(context);
	}

	public QuickIndexLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mScroller = new Scroller(getContext(), new ScrollInterpolator());
	}

	public Scroller getScroller() {
		return mScroller;
	}

	@Override
	public void scrollTo(int x, int y) {
		setTranslationY(y);
	}

	@Override
	public void computeScroll() {
		computeScrollHelper();
	}

	protected boolean computeScrollHelper() {
		if (mScroller.computeScrollOffset()) {
			if (getScrollY() != mScroller.getCurrY() || getTranslationY() != 0) { // make
				scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			}
			if (getQuickIndexLayoutDirection()) {
				float temp = (getTranslationY() * 2.0f)
						/ Math.abs(getQuickIndexLayoutHeigh());
				setQuickIndexLayoutAlpha(temp);
			} else {
				setAlpha(1.0f);
			}
			invalidate();
			return true;
		}
		// Log.d("DEBUG", "the TranslationY = "+getTranslationY());
		return false;
	}

	private static class ScrollInterpolator implements Interpolator {
		public ScrollInterpolator() {
		}

		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t * t * t * t * t + 1; // default :t*t*t*t*t +1;
		}
	}

	public void setDirection(boolean d) {
		mDirection = d;
	}

	public boolean getQuickIndexLayoutDirection() {
		return mDirection;
	}

	public void setQuickIndexLayoutHeight(int h) {
		mHeight = h;
	}

	public int getQuickIndexLayoutHeigh() {
		return mHeight;
	}

	private void setQuickIndexLayoutAlpha(float progress) {
		progress = Math.min(progress, 1.0f);
		progress = Math.max(progress, -1.0f);
		float alpha = 1 - Math.abs(progress);
		setAlpha(alpha);
	}
}
