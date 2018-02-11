package com.aurora.voiceassistant.view;

import com.aurora.voiceassistant.R;
import android.animation.Animator.AnimatorListener;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class CustomMenuProxy extends FrameLayout implements
		CustomMenu.IDissMissBackGround {
	private CustomMenu mCustomMenu;
	public TextView mTextView;
	private ValueAnimator mEnterValueAnimator;
	private ValueAnimator mExitValueAnimator;
	private MainActivity mainActivity;

	private LayoutParams params;
	private int height;
	
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
	}

	public CustomMenuProxy(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CustomMenuProxy(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mCustomMenu = new CustomMenu(context);
		mCustomMenu.setCallBack(this);
		mTextView = new TextView(context);
		
//		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
		mTextView.setLayoutParams(params);
		mTextView.setBackgroundColor(0x99000000);

		addView(mTextView);
		mTextView.setVisibility(View.GONE);
		mEnterValueAnimator = createAnimator(0.0f, 1.0f);
		//shigq
//		mEnterValueAnimator = createAnimator(0.0f, 0.01f);
		
		mEnterValueAnimator.addListener(mEnterAnimatorListener);
		mEnterValueAnimator.addUpdateListener(mEnterAnimatorUpdateListener);
		mExitValueAnimator = createAnimator(1.0f, 0.0f);
		//shigq
//		mExitValueAnimator = createAnimator(0.01f, 0.0f);
		
		mExitValueAnimator.addListener(mExitAnimatorListener);
		mExitValueAnimator.addUpdateListener(mExitAnimatorUpdateListener);
	}
	
	public void getMainActivity(MainActivity m) {
		mainActivity = m;
	}

	public boolean isShowing() {
		Log.i("xiejun", "custom menu isShowing");
		return mCustomMenu.isShowing();
	}

	public void dismissCustomMenu() {
		Log.i("xiejun", "custom menu dissmiss");
		mCustomMenu.dismiss();
	}

	public void showAtLocation(View parent, int gravity, int x, int y) {
		Log.i("xiejun", "custom menu showAtLocation :( "+mTextView.getWidth()+"  ,  "+mTextView.getHeight()+" )");
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
		if (!mainActivity.getClearScreenFlag()) {
			hideBackGround(true);
		}
//		hideBackGround(true);
	}

	public void hideBackGround(boolean b) {
		Log.d("DEBUG", "hideBackGround======================"+b);
		if (b) {
			mEnterValueAnimator.cancel();
			mExitValueAnimator.cancel();
			mExitValueAnimator.start();
		} else {
			mEnterValueAnimator.cancel();
			mExitValueAnimator.cancel();
			mEnterValueAnimator.start();
		}
	}

	private ValueAnimator createAnimator(float... values) {
		ValueAnimator animator = new ValueAnimator();
		animator.setFloatValues(values);
		animator.setInterpolator(new LinearInterpolator());
//		animator.setDuration(300);
		//shigq
		animator.setDuration(0);
		return animator;
	}

	AnimatorListener mEnterAnimatorListener = new AnimatorListener() {
		@Override
		public void onAnimationStart(Animator animation) {
			mTextView.setAlpha(1.0f);
			mTextView.setVisibility(View.VISIBLE);
			Log.e("linp", "###############################mEnterAnimatorListener");
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

		@Override
		public void onAnimationEnd(Animator animation) {
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			mTextView.setAlpha(1.0f);
			mTextView.setVisibility(View.GONE);
		}
	};
	AnimatorListener mExitAnimatorListener = new AnimatorListener() {
		@Override
		public void onAnimationStart(Animator animation) {
			mTextView.setAlpha(1.0f);
			Intent intent = new Intent("com.aurora.voiceassistant.ACTION_ADD_NAVIGATION_TRANSLUCENT_FLAG");
			mContext.sendBroadcast(intent);
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			mTextView.setVisibility(View.GONE);
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			mTextView.setVisibility(View.GONE);
		}
	};

}
