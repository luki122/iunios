package com.aurora.puremanager.animation;

import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.aurora.puremanager.view.MainCircleLayout.AnimationCallBack;

public class MainCircleAnimation extends Animation {

	private AnimationListener listener;
	
	private AnimationCallBack callBack;
	
	public MainCircleAnimation(AnimationCallBack callBack){
		this.callBack = callBack;
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
				listener.onAnimationStart(this);
			}else if(interpolatedTime == 1){
				listener.onAnimationEnd(this);
			}
		}
		
		if(callBack != null){
			callBack.callBack(interpolatedTime, t);
		}
	}
}
