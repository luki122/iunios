/**
 * Vulcan created this file in 2015年3月16日 下午6:03:51 .
 */
package com.android.phase1.cinema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.android.phase1.cinema.CinemaMan.CinemaListener;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;


/**
 * Vulcan created Cinema in 2015年3月16日 . 统一定义不同类型的动画的控制接口
 */

public abstract class Cinema {
	protected View mContentView;
	protected View mFixedTitlebarContainer;
	protected View mToolBarContainer;
	private float lastProgress = 0f;
	public static int animatorNumb=0;

	protected ArrayList<ValueAnimator> animatorList = new ArrayList<ValueAnimator>();

	/**
	 * 
	 */
	public Cinema(Context context, View contentView,
			View fixedTitlebarContainer, View toolBarContainer) {
		mContentView = contentView;
		mFixedTitlebarContainer = fixedTitlebarContainer;
		mToolBarContainer = toolBarContainer;
	}

	protected void start(final CinemaListener cinemaListener) {
		AnimatorSet as = new AnimatorSet();
		Collection<Animator> c = new HashSet<Animator>();
		for(ValueAnimator va: this.animatorList) {
			c.add(va);
		}
		as.playTogether(c);
		as.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
				cinemaListener.onCinemaEnd(Cinema.this);
				animatorNumb=0;
			}

			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub

			}
		});
		as.start();
		animatorNumb=animatorNumb+1;
		for(ValueAnimator va: this.animatorList) {
			va.setCurrentPlayTime((long)(lastProgress * va.getDuration()));
		}

	}

	public abstract void create(Context context);

	protected void setProgress(float progress) {

       lastProgress = progress;
		for (ValueAnimator va : animatorList) {
			long time = (long) (progress * va.getDuration());
			va.setCurrentPlayTime(time);
		}
	}

	protected void changeProgress(float progress) {
		float mProgress = lastProgress + progress;
		lastProgress = mProgress;
		for (ValueAnimator va : animatorList) {
			long time = (long) (mProgress * va.getDuration());
			va.setCurrentPlayTime(time);
		}
	}

	/**
	 * 
	 * Vulcan created this method in 2015年3月23日 下午4:30:12 .
	 * @return
	 */
	protected float getProgress() {
		return lastProgress;
	}

	protected boolean progressIsEnd() {
		int f = Float.compare(lastProgress, 1f);
		if (f >= 0) {
			return true;
		}

		return false;
	}

	protected boolean progressIsStart() {
		int f = Float.compare(lastProgress, 0f);
		if (f <= 0) {
			return true;
		}
		return false;
	}

}
