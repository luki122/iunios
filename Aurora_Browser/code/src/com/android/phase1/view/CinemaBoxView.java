/**
 * Vulcan created this file in 2015年4月7日 上午11:42:31 .
 */
package com.android.phase1.view;


import com.android.browser.R;
import com.android.phase1.AuroraPhoneUi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Vulcan created CinemaBoxView in 2015年4月7日 .
 * 
 */
public class CinemaBoxView extends CinemaBox{
	

	
	public ViewGroup mRootView;

	@SuppressLint("InflateParams")
	public CinemaBoxView(Context ctx, AuroraPhoneUi apu) {
		mRootView = (ViewGroup)LayoutInflater.from(ctx).inflate(R.layout.aurora_cinema_box,null);
	}



	@Override
	public void attachToViewGroup(ViewGroup vg) {
        if (mRootView.getParent() == null) {
            vg.addView(mRootView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
		return;
	}

	@Override
	public void dettachFromViewGroup(ViewGroup vg) {
		vg.removeView(mRootView);
	}



	@Override
	public void playAnim(Activity ctx) {
		//playAnimInternal(ctx, w, h, cl, root);
	}



}
