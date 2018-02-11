package com.aurora.calendar.period;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.android.calendar.R;
import com.android.calendar.Utils;

public class AuroraMorePopupWindow extends PopupWindow {

	private View mMenuView;
	private LinearLayout moreActionsView;
	private Context context;
	private ColorDrawable cd ;
	private TweensAnimation alphaInAnim = null;
	private TweensAnimation alphaOutAnim = null;
	private final int ANIM_DURING_TIME = 120;

	public AuroraMorePopupWindow(Context context, OnClickListener itemsOnClick, boolean isPeriod) {
		super(context);

		setWidth(LayoutParams.MATCH_PARENT);
		setHeight(LayoutParams.MATCH_PARENT);
		setFocusable(true);
		cd = new ColorDrawable(0x45000000);
		cd.setAlpha(0);//全透明
		setBackgroundDrawable(cd);

		this.context = context;

		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.aurora_more_popup_window, null);
		setContentView(mMenuView);

		mMenuView.setOnTouchListener(new OnTouchListener() {		
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_UP){	
					dismiss();
				}
				return true;
			}
		});

		moreActionsView = (LinearLayout) mMenuView.findViewById(R.id.more_actions_layout);	
		mMenuView.findViewById(R.id.remember_day_entry).setOnClickListener(itemsOnClick);

		Button periodBtn = (Button) mMenuView.findViewById(R.id.show_or_hide_period);
		periodBtn.setOnClickListener(itemsOnClick);

		if (isPeriod) {
			periodBtn.setText(R.string.aurora_period_hide);
		}

		if (!Utils.isWomenEnvironment()) {
			periodBtn.setVisibility(View.GONE);
		}
	}

	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		super.showAtLocation(parent, gravity, x, y);
		moreActionsView.setVisibility(View.INVISIBLE);
		mMenuView.postDelayed(new Runnable() {					
			@Override
			public void run() {	
				startBgAlphaInAnim();
				startMenuExpandAnim();
			}
		}, 20);	
	}

	@Override
	public void dismiss() {
		startBgAlphaOutAnim();
		startMenuCloseAnim();
	}

	private void startBgAlphaInAnim() {  
		if(alphaInAnim == null){
			alphaInAnim = new TweensAnimation(new TweensAnimCallBack() {
				public void callBack(float interpolatedTime, Transformation t) {
					cd.setAlpha((int)(255 * interpolatedTime));
					setBackgroundDrawable(cd);
			}});
			alphaInAnim.setDuration(ANIM_DURING_TIME);
			alphaInAnim.setInterpolator(new LinearInterpolator());
		}
		mMenuView.clearAnimation();
		mMenuView.startAnimation(alphaInAnim);
	}

	private void startMenuExpandAnim() {
		Animation animation1 = AnimationUtils.loadAnimation(context, R.anim.more_pop_expand_anim);
		animation1.setInterpolator(new LinearInterpolator());
		animation1.setDuration(ANIM_DURING_TIME);
		animation1.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) { }
			@Override
			public void onAnimationRepeat(Animation animation) { }
			@Override
			public void onAnimationStart(Animation animation) { 
				moreActionsView.setVisibility(View.VISIBLE);
			}		
		});
		moreActionsView.clearAnimation();
		moreActionsView.startAnimation(animation1);
	}

	private void startBgAlphaOutAnim(){  
		if(alphaOutAnim == null){
			alphaOutAnim = new TweensAnimation(new TweensAnimCallBack() {
				public void callBack(float interpolatedTime, Transformation t) {
					cd.setAlpha(255 - (int)(255 * interpolatedTime));
					setBackgroundDrawable(cd);
			}});
			alphaOutAnim.setDuration(ANIM_DURING_TIME);
			alphaOutAnim.setInterpolator(new LinearInterpolator());
		}
		mMenuView.clearAnimation();
		mMenuView.startAnimation(alphaOutAnim);
	}

	private void startMenuCloseAnim() {
		Animation animation1 = AnimationUtils.loadAnimation(context, R.anim.more_pop_close_anim);
		animation1.setInterpolator(new LinearInterpolator());
		animation1.setDuration(ANIM_DURING_TIME);
		animation1.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				moreActionsView.setVisibility(View.INVISIBLE);
				AuroraMorePopupWindow.super.dismiss();
			}
			@Override
			public void onAnimationRepeat(Animation animation) { }
			@Override
			public void onAnimationStart(Animation animation) { }		
		});
		moreActionsView.clearAnimation();
		moreActionsView.startAnimation(animation1);
	}

}
