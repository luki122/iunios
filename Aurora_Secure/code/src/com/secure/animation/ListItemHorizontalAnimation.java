package com.secure.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

public class ListItemHorizontalAnimation extends Animation {

	private int mType;
	public final static int RIGHT = 0;
	public final static int LEFT = 1;	
	private RelativeLayout.LayoutParams mLayoutParamsOfMidView;	
	private View childLeftView;
	private View childMidView;
	private View childRightView;
	private int leftLayoutWidth;
	private AnimationListener listener;

	/**
	 * 
	 * @param childMidView
	 * @param leftLayoutWidth
	 * @param type RIGHT = 0,LEFT = 1;
	 */
	public ListItemHorizontalAnimation(View childMidView,View childRightView,int leftLayoutWidth,int type) {
		this.childMidView = childMidView;
		this.childRightView = childRightView;
		this.leftLayoutWidth = leftLayoutWidth;
		mLayoutParamsOfMidView = ((RelativeLayout.LayoutParams) childMidView.getLayoutParams());
		mType = type;
		if(mType == RIGHT) {
			mLayoutParamsOfMidView.leftMargin = 0;
		} else {
			mLayoutParamsOfMidView.leftMargin = leftLayoutWidth;
		}
	}
	
	/**
	 * 设置动画监听器
	 */
	public void setAnimationListener(AnimationListener listener){
		this.listener = listener;
	};

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);	
		if(listener != null){
			if(interpolatedTime == 0){
				childRightView.setVisibility(View.VISIBLE);
				listener.onAnimationStart(this);
			}else{
				listener.onAnimationEnd(this);
			}
		}
		
		if (interpolatedTime < 1.0f) {
			if(mType == RIGHT) {
				childRightView.setAlpha(1-interpolatedTime);
				mLayoutParamsOfMidView.leftMargin = (int)(leftLayoutWidth * interpolatedTime);
			} else {
				childRightView.setAlpha(interpolatedTime);
				mLayoutParamsOfMidView.leftMargin = leftLayoutWidth-(int)(leftLayoutWidth*interpolatedTime);
			}
			childMidView.requestLayout();
		} else {
			if(mType == RIGHT) {
				childRightView.setVisibility(View.INVISIBLE);
				mLayoutParamsOfMidView.leftMargin = leftLayoutWidth;
				childMidView.requestLayout();
			} else {
				mLayoutParamsOfMidView.leftMargin = 0;
				childMidView.requestLayout();
			}
		}
	}
}
