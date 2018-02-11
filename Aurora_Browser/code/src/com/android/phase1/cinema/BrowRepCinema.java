package com.android.phase1.cinema;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.android.browser.R;

public class BrowRepCinema  extends Cinema{
	private ObjectAnimator oa;
	private ObjectAnimator oa2;
	//private ValueAnimator oa3;
	public static final int CINEMA_TIME = 400;


	public BrowRepCinema(Context context,View contentView, View fixedTitlebarContainer,
			View toolBarContainer) {
		super(context,contentView, fixedTitlebarContainer, toolBarContainer);
		// TODO Auto-generated constructor stub
		
	}

	

	



	@Override
	public void create(Context context) {
		// TODO Auto-generated method stub
		int titlebarHeight = context.getResources().getDimensionPixelSize(
				R.dimen.aurora_browser_titlebar_height);
		int toolbarHeight = context.getResources().getDimensionPixelSize(
				R.dimen.aurora_setting_bar_height);
		oa = titleBarAndToolBarAnimator(-titlebarHeight, 0, CINEMA_TIME);
		oa2 = toolBarAndToolBarAnimator(toolbarHeight, 0, CINEMA_TIME);
		//oa3 = webViewContentAnimator(0, -toolbarHeight, CINEMA_TIME);
		animatorList.add(oa);
		animatorList.add(oa2);
		//animatorList.add(oa3);
		//as.play(oa).with(oa2);
		
		
	}
	public ObjectAnimator titleBarAndToolBarAnimator(float from, float to,
			int time) {

		float[] f = new float[2];
		f[0] = from;
		f[1] = to;
		ObjectAnimator animator1 = ObjectAnimator.ofFloat(
				mFixedTitlebarContainer, "Y", f);
		animator1.setInterpolator(new AccelerateDecelerateInterpolator());
		animator1.setDuration(time);
		return animator1;

	}

	public ObjectAnimator toolBarAndToolBarAnimator(float from, float to,
			int time) {

		float[] f = new float[2];
		f[0] = from;
		f[1] = to;
		ObjectAnimator animator1 = ObjectAnimator.ofFloat(mToolBarContainer,
				"translationY", f);
		animator1.setInterpolator(new AccelerateDecelerateInterpolator());
		animator1.setDuration(time);
		return animator1;
	}

	public ValueAnimator webViewContentAnimator(final int from, int to, int time) {
		ValueAnimator anim = ValueAnimator.ofInt(from, to);
		anim.setDuration(time);
		anim.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				int frameValue = (Integer) arg0.getAnimatedValue();
				mContentView.setY(frameValue);
				mContentView.invalidate();
			}
		});
		return anim;
	}
	

}
