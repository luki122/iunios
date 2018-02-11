package com.aurora.iunivoice.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.aurora.iunivoice.R;


public class AuroraMultipleChoiceDialogEditText extends AuroraEditText implements AnimationListener{

	private Animation mShowAnimation;
	
	private Animation mHideAnimation;
	
	
	public AuroraMultipleChoiceDialogEditText(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initAnimation();
	}

	public AuroraMultipleChoiceDialogEditText(Context context,
			AttributeSet attrs) {
		super(context, attrs);
		initAnimation();
		// TODO Auto-generated constructor stub
	}

	public AuroraMultipleChoiceDialogEditText(Context context,
			AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initAnimation();
	}
	
	private void initAnimation(){
		mShowAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.aurora_multiple_choice_dialog_edit_enter);
		mShowAnimation.setAnimationListener(this);
	}
	
	@Override
	public void setVisibility(int visibility) {
		// TODO Auto-generated method stub
		if(visibility == View.VISIBLE){
			startAnimation(mShowAnimation);
		}else{
			super.setVisibility(View.GONE);
		}
		
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		super.setVisibility(View.VISIBLE);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		setFocusable(true);
		requestFocus();
		
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	

}
