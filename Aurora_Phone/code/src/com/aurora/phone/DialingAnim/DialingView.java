package com.android.phone;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class DialingView extends View {

	private AuroraDialingPoint[] mAuroraDialingPoint;

	public DialingView(Context context) {
		this(context, null);
	}

	public DialingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mAuroraDialingPoint = new AuroraDialingPoint[3];
        for(int i = 0; i<3; i++) {
        	mAuroraDialingPoint[i] = new AuroraDialingPoint(context, i); 
        	Animator a = getPointAlphaAnimator(mAuroraDialingPoint[i], 200 * (i % 3));
			mWaveAnimationsV2.add(a);
        }
	}

	public void startAnim() {
		mWaveAnimationsV2.cancel();
		mWaveAnimationsV2.start();
	}

	public void stopAnim() {
		mWaveAnimationsV2.cancel();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		for (AuroraDialingPoint ap : mAuroraDialingPoint) {
			ap.draw(canvas);
		}
	}

	private Animator getPointAlphaAnimator(AuroraDialingPoint ap, long delay) {
		ap.setAlpha(0);
		AccelerateDecelerateInterpolator alphaInterpolator = new AccelerateDecelerateInterpolator();
		AnimatorUpdateListener al = mUpdateListener;
		ObjectAnimator mAlphaAnimation1 = getAnimator(ap, "Alpha", 0, 1, delay,
				500, alphaInterpolator, al);
		ObjectAnimator mAlphaAnimation2 = getAnimator(ap, "Alpha", 1, 0, delay,
				500, alphaInterpolator, al);
		AnimatorSet bouncer = new AnimatorSet();
		bouncer.playSequentially(mAlphaAnimation1, mAlphaAnimation2);	
		return bouncer;
	}

	private float mFactor = 1.0f;
	  private AnimatorUpdateListener mUpdateListener = new AnimatorUpdateListener() {
	        public void onAnimationUpdate(ValueAnimator animation) {
	            invalidate();
	        }
	    };

	private ObjectAnimator getAnimator(Object obj, String prop, float v1,
			float v2, long delay, long duration, TimeInterpolator ti,
			AnimatorUpdateListener l) {
		ArrayList<PropertyValuesHolder> props = new ArrayList<PropertyValuesHolder>(
				1);
		props.add(PropertyValuesHolder.ofFloat(prop, v1, v2));
		ObjectAnimator mAnimation = ObjectAnimator.ofPropertyValuesHolder(obj,
				props.toArray(new PropertyValuesHolder[1]));
		mAnimation.setDuration((long) (duration * mFactor));
		mAnimation.setStartDelay((long) (delay * mFactor));
		mAnimation.setInterpolator(ti);
		if (l != null) {
			mAnimation.addUpdateListener(l);
		}
		return mAnimation;
	}

	private AnimationBundleV2 mWaveAnimationsV2 = new AnimationBundleV2();

	private class AnimationBundleV2 extends ArrayList<Animator> {
		private static final long serialVersionUID = 0xA84D78726F127468L;
		private boolean mSuspended;

		public void start() {
			if (mSuspended)
				return; // ignore attempts to start animations
			final int count = size();
			for (int i = 0; i < count; i++) {
				Animator anim = get(i);
				anim.start();
			}
		}

		public void cancel() {
			final int count = size();
			for (int i = 0; i < count; i++) {
				Animator anim = get(i);
				anim.cancel();
			}
//			clear();
		}

		public void stop() {
			final int count = size();
			for (int i = 0; i < count; i++) {
				Animator anim = get(i);
				anim.end();
			}
//			clear();
		}

		public void setSuspended(boolean suspend) {
			mSuspended = suspend;
		}
	};
}
