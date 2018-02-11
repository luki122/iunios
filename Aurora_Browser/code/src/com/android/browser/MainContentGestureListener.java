package com.android.browser;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.MotionEvent;
import android.widget.ImageView;

class MainContentGestureListener implements OnGestureListener {
	private UiController controller;
	private ImageView im1;
	private ImageView im2;
	private Activity mActivity;
	private int width;
	private int height;
	private BrowserWebView mTarget;
	private int mSlop;

	public void setMyGestureListener(Activity activity,
			UiController mUiController, ImageView image1, ImageView image2) {
		this.mActivity = activity;
		this.controller = mUiController;
		this.im1 = image1;
		this.im2 = image2;
		WindowManager wm = (WindowManager) mActivity
				.getSystemService(mActivity.WINDOW_SERVICE);

		width = wm.getDefaultDisplay().getWidth();
		height = wm.getDefaultDisplay().getHeight();
		ViewConfiguration config = ViewConfiguration.get(mActivity);
		mSlop = config.getScaledTouchSlop();

	}

	public void setTarget(BrowserWebView v) {

		if (mTarget == v) {

			return;
		}

		if (mTarget == null) {

			mTarget = v;
		}
	}

	public void onUp() {
		reSet1();
		reSet2();
		Log.i("xie1", "_________onUp_____________________");
	}

	// @Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

		Log.i("xie1", "_________onFling_____________________" + velocityX
				+ "    " + velocityY);
		Log.i("xie1", "_________onFling__________2___________" + e1.getX()
				+ "             " + e2.getX());

		if ((e1.getX() < 60 || e1.getX() > width - 60)&&e1.getX() - e2.getX() > 200 && Math.abs(velocityX) > 80
				&& Math.abs(velocityX)+1000 > Math.abs(velocityY)) {
			Log.i("xie1", "______11111___________"
					+ controller.getCurrentTab().canGoForward());
			if(BaseUi.isScreenOriatationPortrait(mActivity)) {
				controller.getCurrentTab().goForward();
			}

		} else if ((e1.getX() < 60 || e1.getX() > width - 60)&&e2.getX() - e1.getX() > 200 && Math.abs(velocityX) > 80
				&& Math.abs(velocityX)+1000 > Math.abs(velocityY)) {
			Log.i("xie1", "_______222___________"
					+ controller.getCurrentTab().canGoBack());
			if(BaseUi.isScreenOriatationPortrait(mActivity)) {
				controller.getCurrentTab().goBack();
			}

		}
		reSet1();
		reSet2();
		return false;
	}

	// @Override
	public boolean onDown(MotionEvent e) {

		return false;
	}

	// @Override
	public void onLongPress(MotionEvent e) {
	}

	// @Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		

		if (Math.abs(distanceX) > Math.abs(distanceY)
				&& Math.abs(e1.getX() - e2.getX()) > mSlop * 3) {

			Log.i("xie12",
					"_________onScroll_____2________y________" + e1.getX()
							+ "          " + e2.getX());

			if(e1.getX() < 60){
				if (im1.getTranslationX() - distanceX <= 0 && BaseUi.isScreenOriatationPortrait(mActivity)) {
					reSet2();
					im1.setTranslationX(im1.getTranslationX() - distanceX);
				}
				
			
			}
			if( e1.getX() > width - 60){
				
				if (im2.getTranslationX() - distanceX >= 0 && BaseUi.isScreenOriatationPortrait(mActivity)) {
					reSet1();						
					im2.setTranslationX(im2.getTranslationX() - distanceX);

				}
				
			}
			
//			if (e1.getX() < 60 || e1.getX() > width - 60) {
//				if (distanceX > 0) {
//					if (im2.getTranslationX() - distanceX >= 0) {
//						reSet1();
//						Log.i("xie11", "_____dddddddd_________________");
//						im2.setTranslationX(im2.getTranslationX() - distanceX);
//					}
//
//				} else {
//					if (im1.getTranslationX() - distanceX <= 0) {
//						reSet2();
//						im1.setTranslationX(im1.getTranslationX() - distanceX);
//					}
//				}
//
//			} 
//			else {
//
//				if (mTarget != null && mTarget.getScale() != 3.0
//						&& mTarget.getScale() > 1.0) {
//					Log.i("xie12",
//							"________电脑版____________________"
//									+ mTarget.getScale() + mSlop);
//
//				} else {
//
//					if (distanceX > 0) {
//						if (im2.getTranslationX() - distanceX >= 0) {
//							reSet1();
//							Log.i("xie11", "_____dddddddd_________________");
//							im2.setTranslationX(im2.getTranslationX()
//									- distanceX);
//						}
//
//					} else {
//						if (im1.getTranslationX() - distanceX <= 0) {
//							reSet2();
//							im1.setTranslationX(im1.getTranslationX()
//									- distanceX);
//						}
//					}
//
//				}
//
//			}

		}

		return false;
	}

	public void onShowPress(MotionEvent e) {
	}

	public boolean onSingleTapUp(MotionEvent e) {
		Log.i("xie1", "_________onSingleTapUp_____________________");

		reSet1();
		reSet2();
		return false;
	}

	public void changeStatueOfForwardBackword() {
		if(controller.getCurrentTab()!=null){
		if (controller.getCurrentTab().canGoBack()) {

			im1.setImageResource(R.drawable.for_bac_left);

		} else {

			im1.setImageResource(R.drawable.no_for_bac);

		}
		if (controller.getCurrentTab().canGoForward()) {

			im2.setImageResource(R.drawable.for_bac_right);
		} else {

			im2.setImageResource(R.drawable.no_for_bac);
		}
		}
	}

	public void reSet1() {

		ObjectAnimator animator = ObjectAnimator.ofFloat(im1, "translationX",
				im1.getTranslationX(), -im1.getWidth());

		animator.setDuration(200);
		animator.start();
		animator.setInterpolator(AnimationUtils.loadInterpolator(
				controller.getActivity(), android.R.interpolator.linear));

	}

	public void reSet2() {

		ObjectAnimator animator = ObjectAnimator.ofFloat(im2, "translationX",
				im2.getTranslationX(), im2.getWidth());

		animator.setDuration(200);
		animator.start();
		animator.setInterpolator(AnimationUtils.loadInterpolator(
				controller.getActivity(), android.R.interpolator.linear));

	}
} 