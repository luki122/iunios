package com.aurora.voiceassistant.view;

import com.aurora.voiceassistant.R;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class RequestAnimation extends RelativeLayout {

	//加载动画
	private View view;
	/*private ImageView pic_red;
	private ImageView pic_yel;
	private ImageView pic_gre;*/
	private ImageView searchingImage1;
	private ImageView searchingImage2;
	private ImageView searchingImage3;
	private AnimatorSet allSet;
	private boolean runing;
	
	public RequestAnimation(Context context) {
		this(context, null, 0);
	}
	
	public RequestAnimation(Context context, AttributeSet attri){
		this(context, attri, 0);
	}

	public RequestAnimation(Context context, AttributeSet attri, int deStyle){
		super(context, attri, deStyle);
		
		view = LayoutInflater.from(getContext()).inflate(R.layout.vs_request_dialog_animation, this);
		//获取动画对象
		initView();
	}
	
	private void initView(){
		/*pic_red = (ImageView) view.findViewById(R.id.pic_red);
		pic_yel = (ImageView) view.findViewById(R.id.pic_yel);
		pic_gre = (ImageView) view.findViewById(R.id.pic_gre);*/
		
		searchingImage1 = (ImageView) view.findViewById(R.id.vs_searching_image1);
		searchingImage2 = (ImageView) view.findViewById(R.id.vs_searching_image2);
		searchingImage3 = (ImageView) view.findViewById(R.id.vs_searching_image3);
		
		allSet = isSearchingAnimation();
//		initViewAnimation();
	}
	
	private AnimatorSet imageViewProcessExpand(View view){
		AnimatorSet aSet = new AnimatorSet();
		
		ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0, 1.0f);
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0, 1.0f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0, 1.0f);
		
		aSet.playTogether(alpha, scaleX, scaleY);
		return aSet;
	}
	
	private AnimatorSet imageViewProcessNarrow(View view){
		AnimatorSet aSet = new AnimatorSet();
		
		ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0f);
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0f);
		
		aSet.playTogether(alpha, scaleX, scaleY);
		return aSet;
	}
	
	private AnimatorSet isSearchingAnimation(){
		
		//点1-->凸显
		AnimatorSet img1_expand = imageViewProcessExpand(searchingImage1);
		img1_expand.setInterpolator(new DecelerateInterpolator());
		img1_expand.setDuration(500);
		//img1_expand.start();
		
		//点1-->缩隐；点2-->凸显
		AnimatorSet img1_narrow = imageViewProcessNarrow(searchingImage1);
		img1_narrow.setInterpolator(new AccelerateInterpolator());
		img1_narrow.setDuration(500);
		img1_narrow.setStartDelay(500);
		//img1_narrow.start();
		
		AnimatorSet img2_expand = imageViewProcessExpand(searchingImage2);
		img2_expand.setInterpolator(new DecelerateInterpolator());
		img2_expand.setDuration(500);
		img2_expand.setStartDelay(250);
		//img2_expand.start();
		
		//d点2-->缩隐；点3凸显
		AnimatorSet img2_narrow = imageViewProcessNarrow(searchingImage2);
		img2_narrow.setInterpolator(new AccelerateInterpolator());
		img2_narrow.setDuration(500);
		img2_narrow.setStartDelay(750);
		
		AnimatorSet img3_expand = imageViewProcessExpand(searchingImage3);
		img3_expand.setDuration(500);
		img3_expand.setStartDelay(500);
		
		//点3-->缩隐
		AnimatorSet img3_narrow = imageViewProcessNarrow(searchingImage3);
		img3_narrow.setDuration(500);
		img3_narrow.setStartDelay(1000);
		
		//总动画
		AnimatorSet aSet = new AnimatorSet();
		aSet.playTogether(img1_expand, img1_narrow, img2_expand, img2_narrow, img3_expand, img3_narrow);
		//aSet.start();
		aSet.addListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				animation.start();
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		return aSet;
	}
	
	/*private void initViewAnimation(){
		//Red-第一阶段
		ObjectAnimator reds = ObjectAnimator.ofFloat(pic_red, "alpha", 0.2f, 1);
		reds.setDuration(500);
		reds.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				pic_red.setAlpha(0.2f);
				pic_red.setImageDrawable(getResources().getDrawable(R.drawable.vs_speech_loading_red));
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		
		//Red-第二阶段
		ObjectAnimator rede = ObjectAnimator.ofFloat(pic_red, "alpha", 1, 0.2f);
		rede.setDuration(500);
		
		//Yel-第一阶段
		ObjectAnimator yels = ObjectAnimator.ofFloat(pic_yel, "alpha", 0.2f, 1);
		yels.setDuration(500);
		
		AnimatorSet rede_yelsSet = new AnimatorSet();
		rede_yelsSet.playTogether(rede,yels);
		rede_yelsSet.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				pic_yel.setAlpha(0.3f);
				pic_yel.setImageDrawable(getResources().getDrawable(R.drawable.vs_speech_loading_yellow));
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				pic_red.setImageDrawable(getResources().getDrawable(R.drawable.vs_speech_loading_gray));
				pic_red.setAlpha(1.0f);
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});

		//yel第二阶段
		ObjectAnimator yele = ObjectAnimator.ofFloat(pic_yel, "alpha", 1, 0.2f);
		yele.setDuration(500);
		
		//Gre-第一阶段
		ObjectAnimator gres = ObjectAnimator.ofFloat(pic_gre, "alpha", 0.2f, 1);
		gres.setDuration(500);
		
		AnimatorSet yele_gresSet = new AnimatorSet();
		yele_gresSet.playTogether(yele, gres);
		yele_gresSet.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				pic_gre.setAlpha(0.3f);
				pic_gre.setImageDrawable(getResources().getDrawable(R.drawable.vs_speech_loading_green));
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				pic_yel.setImageDrawable(getResources().getDrawable(R.drawable.vs_speech_loading_gray));
				pic_yel.setAlpha(1.0f);
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		ObjectAnimator gree = ObjectAnimator.ofFloat(pic_gre, "alpha", 1, 0.2f);
		gree.setDuration(500);
		gree.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				pic_gre.setImageDrawable(getResources().getDrawable(R.drawable.vs_speech_loading_gray));
				pic_gre.setAlpha(1.0f);
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		
		//总动画
		allSet = new AnimatorSet();
		allSet.playSequentially(reds, rede_yelsSet, yele_gresSet, gree);
		allSet.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				animation.start();
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
	}*/
	
	public void startRequestAni(){
		if(allSet != null){
			if(allSet.isRunning()){
				allSet.end();
			}
			allSet.start();
		}
	}
	
	public void endRequestAni(){
		if(allSet != null){
			if(allSet.isStarted()){
				allSet.end();
			}
		}
	}
	
	public void destoryAni(){
		if(allSet != null){
			allSet.cancel();
		}
	}

	public boolean isRuning(){
		if(allSet != null){
			return allSet.isRunning();
		}
		return false;
	}

	public void setRuning(boolean runing) {
		this.runing = runing;
	}
	
}
