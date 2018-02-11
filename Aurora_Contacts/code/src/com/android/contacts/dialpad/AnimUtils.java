/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.contacts.dialpad;

import android.R.integer;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

import java.lang.Float;

public class AnimUtils {
	public static final int DEFAULT_DURATION = -1;
	public static final int NO_DELAY = 0;

	public static final Interpolator EASE_IN = new PathInterpolator(0.0f, 0.0f, 0.2f, 1.0f);
	public static final Interpolator EASE_OUT = new PathInterpolator(0.4f, 0.0f, 1.0f, 1.0f);
	public static final Interpolator EASE_OUT_EASE_IN = new PathInterpolator(0.4f, 0, 0.2f, 1);

	public static final Interpolator CURVE_SHOW = new PathInterpolator(0.2f, 0.9f, 0.0f, 1.0f);
	public static final Interpolator CURVE_HIDE = new PathInterpolator(0.45f, 0.0f, 0.6f, 0.3f);
	public static final Interpolator CURVE_EASE = new PathInterpolator(0.25f, 0.1f, 0.25f, 1.0f);
	
	public static final Interpolator EASE_OUT_EASE_IN1 = new PathInterpolator(0.25f,0.1f,0.25f,1.0f);
	public static final Interpolator EASE_IN1 = new PathInterpolator(0.45f,0.0f,0.6f,0.3f);
	public static final Interpolator EASE_OUT1 = new PathInterpolator(0.2f,0.9f,0.0f,1.0f);

	private static final String TAG = "AnimUtils";

	public static class AnimationCallback {
		public void onAnimationEnd() {}
		public void onAnimationCancel() {}
	}



	public static void move(View view, int duration,int delay,Interpolator interpolator,boolean isHorizontal, int start,int end,AnimatorListenerAdapter listener) {
		ViewPropertyAnimator animator = view.animate();
		if(isHorizontal){
			view.setTranslationX(start);
			animator.translationX(end);
		}else{
			view.setTranslationY(start);
			animator.translationY(end);
		}
		animator.setInterpolator(interpolator)
		.setStartDelay(delay)
		.setDuration(duration)
		.setListener(listener)
		.start();
	}

	public static void crossFadeViews(View fadeIn, View fadeOut, int duration) {
		fadeIn(fadeIn, duration);
		fadeOut(fadeOut, duration);
	}

	public static void fadeOut(View fadeOut, int duration) {
		fadeOut(fadeOut, duration, 0,null,null,View.GONE);
	}

	public static void fadeOut(View fadeOut, int duration,Interpolator interpolator,int visible) {
		fadeOut(fadeOut, duration, 0,null,interpolator,visible);
	}

	public static void fadeOut(final View fadeOut, int durationMs,int delay,
			final AnimationCallback callback,Interpolator interpolator,final int visible) {
		fadeOut.setAlpha(1);
		final ViewPropertyAnimator animator = fadeOut.animate();
		animator.cancel();
		if(interpolator!=null){
			animator.setInterpolator(interpolator);
		}
		animator.alpha(0).withLayer().setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				fadeOut.setVisibility(visible);
				if (callback != null) {
					callback.onAnimationEnd();
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				fadeOut.setVisibility(visible);
				fadeOut.setAlpha(0);
				if (callback != null) {
					callback.onAnimationCancel();
				}
			}
		});
		if (durationMs != DEFAULT_DURATION) {
			animator.setDuration(durationMs);			
		}
		animator.setStartDelay(delay);
		animator.start();
	}

	public static void fadeIn(View fadeIn, int durationMs) {
		fadeIn(fadeIn, durationMs, NO_DELAY, null,null);
	}

	public static void fadeIn(View fadeIn, int durationMs,Interpolator interpolator) {
		fadeIn(fadeIn, durationMs, NO_DELAY, null,interpolator);
	}

	public static void fadeIn(final View fadeIn, int durationMs, int delay,
			final AnimationCallback callback,
			final Interpolator interpolator) {
		fadeIn.setAlpha(0);
		final ViewPropertyAnimator animator = fadeIn.animate();
		animator.cancel();

		animator.setStartDelay(delay);
		if(interpolator!=null){
			animator.setInterpolator(interpolator);
		}
		animator.alpha(1).withLayer().setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				fadeIn.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				fadeIn.setAlpha(1);
				if (callback != null) {
					callback.onAnimationCancel();
				}
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (callback != null) {
					callback.onAnimationEnd();
				}
			}
		});
		if (durationMs != DEFAULT_DURATION) {
			animator.setDuration(durationMs);
		}
		animator.start();
	}

	/**
	 * Scales in the view from scale of 0 to actual dimensions.
	 * @param view The view to scale.
	 * @param durationMs The duration of the scaling in milliseconds.
	 * @param startDelayMs The delay to applying the scaling in milliseconds.
	 */
	public static void scaleIn(final View view, int durationMs, int startDelayMs) {
		scaleIn(view, durationMs, startDelayMs,null);
	}

	public static void scaleIn(final View view, int durationMs, int startDelayMs,Interpolator interpolator){
		AnimatorListenerAdapter listener = (new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				view.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				view.setScaleX(1);
				view.setScaleY(1);
			}
		});
		scaleInternal(view, 0 /* startScaleValue */, 1 /* endScaleValue */, durationMs,
				startDelayMs, listener, interpolator==null?EASE_IN:interpolator);
	}


	/**
	 * Scales out the view from actual dimensions to 0.
	 * @param view The view to scale.
	 * @param durationMs The duration of the scaling in milliseconds.
	 */
	public static void scaleOut(final View view, int durationMs) {
		scaleOut(view, durationMs,null,View.GONE);
	}

	public static void scaleOut(final View view, int durationMs,int visible) {
		scaleOut(view, durationMs,null,visible);
	}
	
	public static void scaleOut(final View view, int durationMs,int delay,Interpolator interpolator,final int visible) {
		AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				view.setVisibility(visible);
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				view.setVisibility(visible);
				view.setScaleX(0);
				view.setScaleY(0);
			}
		};

		scaleInternal(view, 1 /* startScaleValue */, 0 /* endScaleValue */, durationMs,
				delay, listener, interpolator==null?EASE_OUT:interpolator);
	}

	public static void scaleOut(final View view, int durationMs,Interpolator interpolator,final int visible) {
		AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				view.setVisibility(visible);
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				view.setVisibility(visible);
				view.setScaleX(0);
				view.setScaleY(0);
			}
		};

		scaleInternal(view, 1 /* startScaleValue */, 0 /* endScaleValue */, durationMs,
				NO_DELAY, listener, interpolator==null?EASE_OUT:interpolator);
	}

	private static void scaleInternal(final View view, int startScaleValue, int endScaleValue,
			int durationMs, int startDelay, AnimatorListenerAdapter listener,
			Interpolator interpolator) {
		Log.d(TAG,"view:"+view+" startScaleValue:"+startScaleValue+" endScaleValue:"+endScaleValue+" durationMs:"+durationMs+" startDelay:"+startDelay+" interpolator:"+interpolator);
		view.setScaleX(startScaleValue);
		view.setScaleY(startScaleValue);

		final ViewPropertyAnimator animator = view.animate();
		animator.cancel();

		animator.setInterpolator(interpolator)
		.scaleX(endScaleValue)
		.scaleY(endScaleValue)
		.setListener(listener)
		.withLayer();

		if (durationMs != DEFAULT_DURATION) {
			animator.setDuration(durationMs);
		}
		animator.setStartDelay(startDelay);

		animator.start();
	}

	/**
	 * Animates a view to the new specified dimensions.
	 * @param view The view to change the dimensions of.
	 * @param newWidth The new width of the view.
	 * @param newHeight The new height of the view.
	 */
	public static void changeDimensions(final View view, final int newWidth, final int newHeight) {
		ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);

		final int oldWidth = view.getWidth();
		final int oldHeight = view.getHeight();
		final int deltaWidth = newWidth - oldWidth;
		final int deltaHeight = newHeight - oldHeight;

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				Float value = (Float) animator.getAnimatedValue();

				view.getLayoutParams().width = (int) (value * deltaWidth + oldWidth);
				view.getLayoutParams().height = (int) (value * deltaHeight + oldHeight);
				view.requestLayout();
			}
		});
		animator.start();
	}
}
