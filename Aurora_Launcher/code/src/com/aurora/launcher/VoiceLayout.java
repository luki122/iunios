package com.aurora.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class VoiceLayout extends FrameLayout {

	private Scroller mScroller;

	/**
	 * value true will indicated that will exit Voice Layout. also Value false
	 * means enter Voice Layout
	 */
	private boolean mDirection;

	private int mHeight;

	public VoiceLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public VoiceLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
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
			if (getScrollY() != mScroller.getCurrY() || getTranslationY() != 0) {
				scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			}
			if (getVoiceLayoutDirection()) {
				float temp = (getTranslationY() * 1.3f)
						/ Math.abs(getVoiceLayoutHeigh());
				setVoiceLayoutAlpha(temp);
			} else {
				setAlpha(1.0f);
			}
			invalidate();
			return true;
		}
		return false;
	}

	private static class ScrollInterpolator implements Interpolator {
		public ScrollInterpolator() {
		}

		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t * t * t * t * t + 1;
		}
	}

	public void setDirection(boolean d) {
		mDirection = d;
	}

	public boolean getVoiceLayoutDirection() {
		return mDirection;
	}

	public void setVoiceLayoutHeight(int h) {
		mHeight = h;
	}

	public int getVoiceLayoutHeigh() {
		return mHeight;
	}

	private void setVoiceLayoutAlpha(float progress) {
		progress = Math.min(progress, 1.0f);
		progress = Math.max(progress, -1.0f);
		float alpha = 1 - Math.abs(progress);
		setAlpha(alpha);
	}
}
