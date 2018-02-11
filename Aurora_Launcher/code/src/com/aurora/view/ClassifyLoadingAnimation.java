package com.aurora.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class ClassifyLoadingAnimation {

	
	private AnimatorSet allSet;
	
	public void setAnimatorSet(AnimatorSet allSet){
		this.allSet = allSet;
	}
	
	/**set up loading animation and referenced from voice assistant project */
	public AnimatorSet setupAnimation(View v1, View v2, View v3) {

		AnimatorSet img1_expand = imageViewProcessExpand(v1);
		img1_expand.setInterpolator(new DecelerateInterpolator());
		img1_expand.setDuration(500);

		AnimatorSet img1_narrow = imageViewProcessNarrow(v1);
		img1_narrow.setInterpolator(new AccelerateInterpolator());
		img1_narrow.setDuration(500);
		img1_narrow.setStartDelay(500);

		AnimatorSet img2_expand = imageViewProcessExpand(v2);
		img2_expand.setInterpolator(new DecelerateInterpolator());
		img2_expand.setDuration(500);
		img2_expand.setStartDelay(250);

		AnimatorSet img2_narrow = imageViewProcessNarrow(v2);
		img2_narrow.setInterpolator(new AccelerateInterpolator());
		img2_narrow.setDuration(500);
		img2_narrow.setStartDelay(750);

		AnimatorSet img3_expand = imageViewProcessExpand(v3);
		img3_expand.setDuration(500);
		img3_expand.setStartDelay(500);

		AnimatorSet img3_narrow = imageViewProcessNarrow(v3);
		img3_narrow.setDuration(500);
		img3_narrow.setStartDelay(1000);

		AnimatorSet aSet = new AnimatorSet();
		aSet.playTogether(img1_expand, img1_narrow, img2_expand, img2_narrow,
				img3_expand, img3_narrow);
		aSet.addListener(new Animator.AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				animation.start();
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub

			}
		});
		return aSet;
	}

	private AnimatorSet imageViewProcessExpand(View view) {
		AnimatorSet aSet = new AnimatorSet();

		ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0, 1.0f);
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0, 1.0f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0, 1.0f);

		aSet.playTogether(alpha, scaleX, scaleY);
		return aSet;
	}

	private AnimatorSet imageViewProcessNarrow(View view) {
		AnimatorSet aSet = new AnimatorSet();

		ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0f);
		ObjectAnimator scaleX = ObjectAnimator
				.ofFloat(view, "scaleX", 1.0f, 0f);
		ObjectAnimator scaleY = ObjectAnimator
				.ofFloat(view, "scaleY", 1.0f, 0f);

		aSet.playTogether(alpha, scaleX, scaleY);
		return aSet;
	}

	public void startRequestAni() {
		//mClassifyAnimationLayout.setVisibility(View.VISIBLE);
		if (allSet != null) {
			if (allSet.isRunning()) {
				allSet.end();
				allSet.cancel();
			}
			allSet.start();
		}
	}

	public void endRequestAni() {
		//mClassifyAnimationLayout.setVisibility(View.INVISIBLE);
		if (allSet != null) {
			if (allSet.isStarted() || allSet.isRunning()) {
				allSet.end();
				allSet.cancel();
			}
		}
	}

	public boolean isRequestAniRunning() {
		if (allSet != null) {
			return allSet.isRunning();
		}
		return false;
	}
}
