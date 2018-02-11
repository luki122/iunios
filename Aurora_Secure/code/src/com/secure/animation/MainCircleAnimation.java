package com.secure.animation;

import com.secure.view.MainCircleLayout.AnimationCallBack;
import com.secure.view.MainCircleView;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextSwitcher;
import android.widget.TextView;

public class MainCircleAnimation extends Animation {

	private AnimationListener listener;
//	private MainCircleView mainCircleView;
//	private TextSwitcher appsNum;
//	private int totalAppsNum;
//	private String curTextStr = null;
	
	private AnimationCallBack callBack;

//	public MainCircleAnimation(MainCircleView mainCircleView,TextSwitcher appsNum,int totalAppsNum) {
//		this.mainCircleView = mainCircleView;
//		this.appsNum = appsNum;
//		this.totalAppsNum = totalAppsNum;
//	}
	
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
//		mainCircleView.updateAnimView(interpolatedTime);
//		
//		String tmpStr = ""+(int)(totalAppsNum*interpolatedTime);
//		if(!tmpStr.equals(curTextStr)){
//			curTextStr = tmpStr;
//			appsNum.setText(curTextStr);
//		}
	}
}
