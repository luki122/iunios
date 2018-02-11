package com.aurora.launcher;

import android.animation.Animator.AnimatorListener;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class CustomMenuProxy extends FrameLayout implements
		CustomMenu.IDissMissBackGround {
	private CustomMenu mCustomMenu;
	private TextView mTextView;
	private ValueAnimator mEnterValueAnimator;
	private ValueAnimator mExitValueAnimator;
	
	private Launcher mLauncher;
	
	private AnimatorUpdateListener mEnterAnimatorUpdateListener = new AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			float r = ((Float) (animation.getAnimatedValue())).floatValue();
			mTextView.setAlpha(r);
		}
	};
	private AnimatorUpdateListener mExitAnimatorUpdateListener = new AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			float r = ((Float) (animation.getAnimatedValue())).floatValue();
			mTextView.setAlpha(r);
		}
	};

	public CustomMenuProxy(Context context) {
		this(context, null);
		mLauncher = (Launcher)context;
	}

	public CustomMenuProxy(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		mLauncher = (Launcher)context;
	}

	public CustomMenuProxy(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mCustomMenu = new CustomMenu(context);
		mCustomMenu.setCallBack(this);
		mTextView = new TextView(context);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		mTextView.setLayoutParams(params);
		mTextView.setBackgroundColor(0x66000000);
		addView(mTextView);
		mTextView.setVisibility(View.GONE);
		mEnterValueAnimator = createAnimator(0.0f, 1.0f);
		mEnterValueAnimator.addListener(mEnterAnimatorListener);
		mEnterValueAnimator.addUpdateListener(mEnterAnimatorUpdateListener);
		mExitValueAnimator = createAnimator(1.0f, 0.0f);
		mExitValueAnimator.addListener(mExitAnimatorListener);
		mExitValueAnimator.addUpdateListener(mExitAnimatorUpdateListener);
	}

	public boolean isShowing() {
		return mCustomMenu.isShowing();
	}

	public void dismissCustomMenu() {
		mCustomMenu.dismiss();
	}

	public void showAtLocation(View parent, int gravity, int x, int y) {
		hideBackGround(false);
		mCustomMenu.showAtLocation(parent, gravity, x, y);
	}

	public void setAnimationStyle(int id) {
		mCustomMenu.setAnimationStyle(id);
	}

	public void setCustomMenuItemListener(OnItemClickListener l) {
		mCustomMenu.setCustomMenuItemListener(l);
	}

	public View getTextView() {
		return mTextView;
	}

	public CustomMenu getCustomMenu() {
		return mCustomMenu;
	}

	@Override
	public void onCallBack() {
		hideBackGround(true);
		
	}

	private void hideBackGround(boolean b) {
		if (b) {
			mEnterValueAnimator.cancel();
			mExitValueAnimator.cancel();
			mExitValueAnimator.setStartDelay(20);
			mExitValueAnimator.start();
		} else {
			mEnterValueAnimator.cancel();
			mExitValueAnimator.cancel();
			mExitValueAnimator.setStartDelay(20);
			mEnterValueAnimator.start();
		}
	}

	private ValueAnimator createAnimator(float... values) {
		ValueAnimator animator = new ValueAnimator();
		animator.setFloatValues(values);
		animator.setDuration(300);
		return animator;
	}

	AnimatorListener mEnterAnimatorListener = new AnimatorListener() {
		@Override
		public void onAnimationStart(Animator animation) {
			mTextView.setAlpha(0.0f);
			mTextView.setVisibility(View.VISIBLE);
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

		@Override
		public void onAnimationEnd(Animator animation) {
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			mTextView.setAlpha(0.0f);
			mTextView.setVisibility(View.GONE);
		}
	};
	AnimatorListener mExitAnimatorListener = new AnimatorListener() {
		@Override
		public void onAnimationStart(Animator animation) {
			mTextView.setAlpha(1.0f);
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			mLauncher.restoreNavigationbarBackgroundColor();
			mTextView.setVisibility(View.GONE);
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			mLauncher.restoreNavigationbarBackgroundColor();
			mTextView.setVisibility(View.GONE);
		}
	};

}
