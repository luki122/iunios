package com.netmanage.view;

import com.aurora.netmanage.R;
import com.netmanage.utils.FlowUtils;
import com.netmanage.view.MaskedImage.ProcessChangeCallBack;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class EnterAnimationView extends FrameLayout implements ProcessChangeCallBack{			
	public static final int FISRT_ANI_WAIT_TIME = (int)(0.6*3*33);//(int)(0.6*25*33);//500;
	public static final int FISRT_ANI_DURING_TIME =13*33;// 21*33;//500;
	public static final int SECOND_ANI_WAIT_TIME = (int)(0.6*1*33);//(int)(0.6*23*33);//300;
	public static final int SECOND_ANI_DURING_TIME = 12*33;//600;
	public static final int PROCESS_ANI_WAIT_TIME = 0;//300;
	public static final int PROCESS_ANI_DURING_TIME = (int)(0.7*36*33);//46*33;//600;	
	public static final int UNIT_ANI_WAIT_TIME = (int)(0.6*24*33);//200;
	public static final int UNIT_ANI_DURING_TIME = 10*33;//500;
	public static final int FLOW_VALUE_ANI_WAIT_TIME = (int)(0.6*24*33+2*33);	
	public static final int FLOW_VALUE_ANI_DURING_TIME = (int)(8.8*33);

	private boolean isCircleUseRedImage;
	private ImageView firstView;
	private ImageView secondView;
	private ImageView thridView;
	private ImageView flowUnitImg;
	private ProgressView progressView;
 		 		
	public EnterAnimationView(Context context) {
		super(context);
	}
	
	public EnterAnimationView(Context context, AttributeSet attrs) {
	    super(context, attrs);	
	}
	
	public void initAnimState(){
		firstView = (ImageView)findViewById(R.id.firstView);
		secondView = (ImageView)findViewById(R.id.secondView);
		thridView = (ImageView)findViewById(R.id.thridView);
		flowUnitImg = (ImageView)findViewById(R.id.flowUnitImg);
		progressView = (ProgressView)findViewById(R.id.progressWaveImg);	
		
		setViewAlpha(firstView,0,0);
		setViewAlpha(secondView,0,0);
		setViewAlpha(thridView,255,0);
		setViewAlpha(flowUnitImg,0,0);
		progressView.initAnim();
	}
	
	public void startAnim(final long totalFlow,final long progressFlow){ 		
		if(FlowUtils.isWarningProgress(getContext())){
			firstView.setBackgroundResource(R.drawable.progress_red_circle);
			secondView.setBackgroundResource(R.drawable.progress_red_circle_in);
			isCircleUseRedImage = true;
		}else{
			firstView.setBackgroundResource(R.drawable.progress_green_circle);
			secondView.setBackgroundResource(R.drawable.progress_green_circle_in);
			isCircleUseRedImage = false;
		}
		
		//播放外圈动画	
		setViewAlpha(firstView,0,0);		
		final Animation ani1 = AnimationUtils.loadAnimation(getContext(),R.anim.ani_first_wave_bg);
		ani1.setDuration(FISRT_ANI_DURING_TIME);
		ani1.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) { }
			@Override
			public void onAnimationRepeat(Animation animation) { }
			@Override
			public void onAnimationStart(Animation animation) {
				setViewAlpha(firstView,255,255);
			}		
		});		
		firstView.postDelayed(new Runnable() {			
			@Override
			public void run() {
				firstView.startAnimation(ani1);		
			}
		}, FISRT_ANI_WAIT_TIME);
		
		//播放中圈动画	
		setViewAlpha(secondView,0,0);	
		final Animation ani2 = AnimationUtils.loadAnimation(getContext(),R.anim.ani_second_wave_bg);
		ani2.setDuration(SECOND_ANI_DURING_TIME);
		ani2.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) { }
			@Override
			public void onAnimationRepeat(Animation animation) { }
			@Override
			public void onAnimationStart(Animation animation) { 	
				setViewAlpha(secondView,255,255);
			}		
		});		
		secondView.postDelayed(new Runnable() {			
			@Override
			public void run() {
				secondView.startAnimation(ani2);		
			}
		}, SECOND_ANI_WAIT_TIME);
		
		//播放单位图标动画
		final Animation aniFlowUnitImg = AnimationUtils.loadAnimation(getContext(),R.anim.ani_flow_unit_img);
		aniFlowUnitImg.setDuration(UNIT_ANI_DURING_TIME);
		aniFlowUnitImg.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) { }
			@Override
			public void onAnimationRepeat(Animation animation) { }
			@Override
			public void onAnimationStart(Animation animation) {
				setViewAlpha(flowUnitImg,255,255);
			}		
		});
		flowUnitImg.postDelayed(new Runnable() {			
			@Override
			public void run() {
				flowUnitImg.startAnimation(aniFlowUnitImg);		
			}
		}, UNIT_ANI_WAIT_TIME);		
				
		//播放水波进度条动画
		progressView.setProcessChangeCallBack(this);
		progressView.setProgress(true,totalFlow,progressFlow);	
	}

	float changeAlphaMinPrecess = 0.3f;
	float changeAlphaMaxPrecess = 1f;
	
	@Override
	public void callBack(int progress, int tmpProgress,boolean isWarningProgress) {
		if(firstView == null || thridView == null){
			return ;
		}
		if(isWarningProgress){
			if(!isCircleUseRedImage){
				firstView.setBackgroundResource(R.drawable.progress_red_circle);
				secondView.setBackgroundResource(R.drawable.progress_red_circle_in);
				isCircleUseRedImage = true;
			}
			setViewAlpha(thridView,255,0);
		 }else{
			if(isCircleUseRedImage){
				firstView.setBackgroundResource(R.drawable.progress_green_circle);
				secondView.setBackgroundResource(R.drawable.progress_green_circle_in);
				isCircleUseRedImage = false;
			}
			float processPrecess = 1.0f*tmpProgress/progress;		
			if(processPrecess > changeAlphaMinPrecess){
				float alphaScale = (processPrecess-changeAlphaMinPrecess)/
						(changeAlphaMaxPrecess-changeAlphaMinPrecess);
				setViewAlpha(thridView,255-(int)(255*alphaScale),(int)(255*alphaScale));
			}
		 }	
	}
	
	@SuppressLint("NewApi")
	private void setViewAlpha(ImageView view,int backgroundAlpha,int imageAlpha){
		Drawable backDrawable = view.getBackground();
		if(backDrawable != null){
			backDrawable.setAlpha(backgroundAlpha);//设置background的透明度
		}		
		view.setImageAlpha(imageAlpha);//设置src的透明度
	}
}
