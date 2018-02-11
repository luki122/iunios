package com.secure.view;

import com.aurora.secure.R;
import com.secure.animation.TweensAnimCallBack;
import com.secure.animation.TweensAnimation;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import aurora.app.AuroraActivity;

public class AllAppPopupWindow extends PopupWindow {
	private View mMenuView;
	private LinearLayout choiceSortWayLayout;
	private Activity context;
	private ColorDrawable cd ;
	private TweensAnimation alphaInAnim = null;
	private TweensAnimation alphaOutAnim = null;
	private final int ANIM_DURING_TIME = 230;

	public AllAppPopupWindow(Activity context,OnClickListener itemsOnClick) {
		super(context);
		setWidth(LayoutParams.FILL_PARENT);
		setHeight(LayoutParams.FILL_PARENT);
		setFocusable(true);
		cd = new ColorDrawable(0x45000000);
		cd.setAlpha(0);//全透明
		setBackgroundDrawable(cd);
		
		this.context = context;
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.all_app_popup_window, null);
		//让布局从屏幕最顶上开始布局
		mMenuView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		updateMarginTop(mMenuView.findViewById(R.id.choiceSortWayLayout));
		setContentView(mMenuView);
		
		mMenuView.setOnTouchListener(new OnTouchListener() {		
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_UP){	
					dismiss();
				}
				return true;
			}
		});
		
		choiceSortWayLayout = (LinearLayout)mMenuView.findViewById(R.id.choiceSortWayLayout);	
		mMenuView.findViewById(R.id.SORT_BY_USER_APP).setOnClickListener(itemsOnClick);
		mMenuView.findViewById(R.id.SORT_BY_SYS_APP).setOnClickListener(itemsOnClick);
		mMenuView.findViewById(R.id.SORT_BY_SYS_SUBGROUP).setOnClickListener(itemsOnClick);
		mMenuView.findViewById(R.id.SORT_BY_RunningProcesses).setOnClickListener(itemsOnClick);
	}
	
	/**
	 * 更新popupwindow的topmargin，topmarin等于aurora actionbar的高度，这样就不用每次都在
	 * 布局文件中写了，也不用区分不同平台了
	 * @param contentView
	 */
	private void updateMarginTop(View contentView){
		if(contentView == null){
			return;
		}
		
		FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) contentView.getLayoutParams();
		params.topMargin = ((AuroraActivity)context).getAuroraActionBar().getMeasuredHeight();
		
		contentView.setLayoutParams(params);
	}

	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		super.showAtLocation(parent, gravity, x, y);
		choiceSortWayLayout.setVisibility(View.INVISIBLE);
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

	private void startBgAlphaInAnim(){  
		if(alphaInAnim == null){
			alphaInAnim = new TweensAnimation(new TweensAnimCallBack(){
				public void callBack(float interpolatedTime, Transformation t) {
					cd.setAlpha((int)(255*interpolatedTime));
					setBackgroundDrawable(cd);
			}});
			alphaInAnim.setDuration(ANIM_DURING_TIME);
			alphaInAnim.setInterpolator(new LinearInterpolator());
		}
		mMenuView.clearAnimation();
		mMenuView.startAnimation(alphaInAnim);
	}
	
	private void startMenuExpandAnim(){
		Animation animation1 = AnimationUtils.loadAnimation(context,R.anim.all_app_pop_expand_anim);
		animation1.setInterpolator(new LinearInterpolator());
		animation1.setDuration(ANIM_DURING_TIME);
		animation1.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) { }
			@Override
			public void onAnimationRepeat(Animation animation) { }
			@Override
			public void onAnimationStart(Animation animation) { 
				choiceSortWayLayout.setVisibility(View.VISIBLE);
			}		
		});
		choiceSortWayLayout.clearAnimation();
		choiceSortWayLayout.startAnimation(animation1);
	}
	
	private void startBgAlphaOutAnim(){  
		if(alphaOutAnim == null){
			alphaOutAnim = new TweensAnimation(new TweensAnimCallBack(){
				public void callBack(float interpolatedTime, Transformation t) {
					cd.setAlpha(255-(int)(255*interpolatedTime));
					setBackgroundDrawable(cd);
			}});
			alphaOutAnim.setDuration(ANIM_DURING_TIME);
			alphaOutAnim.setInterpolator(new LinearInterpolator());
		}
		mMenuView.clearAnimation();
		mMenuView.startAnimation(alphaOutAnim);
	}
	
	private void startMenuCloseAnim(){
		Animation animation1 = AnimationUtils.loadAnimation(context,R.anim.all_app_pop_close_anim);
		animation1.setInterpolator(new LinearInterpolator());
		animation1.setDuration(ANIM_DURING_TIME);
		animation1.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) {		
				choiceSortWayLayout.setVisibility(View.INVISIBLE);
				AllAppPopupWindow.super.dismiss();	
			}
			@Override
			public void onAnimationRepeat(Animation animation) { }
			@Override
			public void onAnimationStart(Animation animation) { }		
		});
		choiceSortWayLayout.clearAnimation();
		choiceSortWayLayout.startAnimation(animation1);
	}	
}
