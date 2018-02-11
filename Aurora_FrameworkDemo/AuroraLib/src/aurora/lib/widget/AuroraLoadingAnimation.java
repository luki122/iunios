package aurora.lib.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class AuroraLoadingAnimation {

	
	private AnimatorSet mAnimationSet;
	
	private int mDuration = 500;
	
	private int mStartDelay = 250;
	
	public void setAnimatorSet(AnimatorSet allSet){
		this.mAnimationSet = allSet;
	}
	
	public AnimatorSet setupAnimation(View v1, View v2, View v3) {

		AnimatorSet img1_expand = imageViewProcessExpand(v1);
		img1_expand.setInterpolator(new DecelerateInterpolator());
		img1_expand.setDuration(mDuration);

		AnimatorSet img1_narrow = imageViewProcessNarrow(v1);
		img1_narrow.setInterpolator(new AccelerateInterpolator());
		img1_narrow.setDuration(mDuration);
		img1_narrow.setStartDelay(mStartDelay*2);

		AnimatorSet img2_expand = imageViewProcessExpand(v2);
		img2_expand.setInterpolator(new DecelerateInterpolator());
		img2_expand.setDuration(mDuration);
		img2_expand.setStartDelay(mStartDelay);

		AnimatorSet img2_narrow = imageViewProcessNarrow(v2);
		img2_narrow.setInterpolator(new AccelerateInterpolator());
		img2_narrow.setDuration(mDuration);
		img2_narrow.setStartDelay(mStartDelay*3);

		AnimatorSet img3_expand = imageViewProcessExpand(v3);
		img3_expand.setDuration(mDuration);
		img3_expand.setStartDelay(mStartDelay*2);

		AnimatorSet img3_narrow = imageViewProcessNarrow(v3);
		img3_narrow.setDuration(mDuration);
		img3_narrow.setStartDelay(mStartDelay*4);

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

	public void start() {
		if (mAnimationSet != null) {
			if (mAnimationSet.isRunning()) {
				mAnimationSet.end();
				mAnimationSet.cancel();
			}
			mAnimationSet.start();
		}
	}

	public void stop() {
		if (mAnimationSet != null) {
			if (mAnimationSet.isStarted() || mAnimationSet.isRunning()) {
				mAnimationSet.end();
				mAnimationSet.cancel();
			}
		}
	}

	public boolean isRunning() {
		if (mAnimationSet != null) {
			return mAnimationSet.isRunning();
		}
		return false;
	}
}
