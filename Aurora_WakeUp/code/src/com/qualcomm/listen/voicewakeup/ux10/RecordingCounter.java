/*
 * Copyright (c) 2013 Qualcomm Technologies, Inc.  All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.qualcomm.listen.voicewakeup.ux10;

import com.qualcomm.listen.voicewakeup.Global;
import com.qualcomm.listen.voicewakeup.R;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;


public class RecordingCounter {
    private static final String TAG = "ListenLog.RecordingCounter";
    private static final String MYTAG = "iht";
    private Context context;
    private final static int MAX_ITEM_COUNT = 5;
	private ImageView[] views;
	private int recordingCounter = 0;
	//动画view
	private ImageView imageView;
	//private Animation imageViewAni;
	private Animator ani;
	
	public RecordingCounter(Context context, ImageView[] views, ImageView imageView) throws Exception {
		this.views = views;
		this.context = context;
		this.imageView = imageView;
		if (MAX_ITEM_COUNT != views.length ) {
			throw new Exception("Invalid argument");
		}
		initCounter();
	}

	//初始化图片，灰色数字
	private void initCounter() {
		recordingCounter = 0;
		/*views[0].setBackgroundResource(R.drawable.recording_steps_off);
		views[0].setImageResource(R.drawable.ic_1_gray);
		views[1].setBackgroundResource(R.drawable.recording_steps_off);
		views[1].setImageResource(R.drawable.ic_2_gray);
		views[2].setBackgroundResource(R.drawable.recording_steps_off);
		views[2].setImageResource(R.drawable.ic_3_gray);
		views[3].setBackgroundResource(R.drawable.recording_steps_off);
		views[3].setImageResource(R.drawable.ic_4_gray);
		views[4].setBackgroundResource(R.drawable.recording_steps_off);
		views[4].setImageResource(R.drawable.ic_5_gray);*/
		
		views[0].setImageResource(R.drawable.vs_wake_unrecord);
		views[1].setImageResource(R.drawable.vs_wake_unrecord);
		views[2].setImageResource(R.drawable.vs_wake_unrecord);
		views[3].setImageResource(R.drawable.vs_wake_unrecord);
		views[4].setImageResource(R.drawable.vs_wake_unrecord);		
		
		//录音动画
		//imageView.setImageBitmap(createBitmap(400, 400));
		//imageViewAni = AnimationUtils.loadAnimation(context, R.anim.recording_scale);
		//imageView.startAnimation(imageViewAni);
		
		ani = animator(imageView);
		//ani.start();
		
		//setImageFocusChange();
	}

	// Sets the current training image to blue when training  //正在录音时，显示蓝色数字
	private void setImageFocusChange() {
		if (MAX_ITEM_COUNT <= recordingCounter) {
			return;
		}

		/*int resourceId = 0;
		switch (recordingCounter) {
			case 0:	resourceId = R.drawable.ic_1_blue; break;
			case 1:	resourceId = R.drawable.ic_2_blue; break;
			case 2:	resourceId = R.drawable.ic_3_blue; break;
			case 3:	resourceId = R.drawable.ic_4_blue; break;
			case 4:	resourceId = R.drawable.ic_5_blue; break;
		}

		views[recordingCounter].setBackgroundResource(R.drawable.recording_step_focused);
		views[recordingCounter].setImageResource(resourceId);*/
		
		//正在录音的时候
		startRecordingAnimation();
		
	}

	// Sets the current training image to white when done training //结束录音，显示白色数字
	private void setImageDone() {
		if (MAX_ITEM_COUNT <= recordingCounter) {
			return;
		}

		/*int resourceId = 0;
		switch (recordingCounter) {
			case 0:	resourceId = R.drawable.ic_1_white; break;
			case 1:	resourceId = R.drawable.ic_2_white; break;
			case 2:	resourceId = R.drawable.ic_3_white; break;
			case 3:	resourceId = R.drawable.ic_4_white; break;
			case 4:	resourceId = R.drawable.ic_5_white; break;
		}

		views[recordingCounter].setBackgroundResource(R.drawable.recording_steps_done);
		views[recordingCounter].setImageResource(resourceId);*/
		
		//views[recordingCounter].clearAnimation();
		
		//views[recordingCounter].setImageResource(R.drawable.vs_wake_surecord);
		if(recordingCounter == 4){
			//views[recordingCounter].setImageDrawable(context.getResources().getDrawable(R.drawable.vs_wake_surecord));
			//views[recordingCounter].setBackground(null);
			successedAni(views[recordingCounter], true);
		}else{
			successedAni(views[recordingCounter], false);
		}
	}

	// Sets the current training image to white when training fails  //当失败时
	private void setImageFailed() {
		if (MAX_ITEM_COUNT <= recordingCounter) {
			return;
		}

		/*int resourceId = 0;
		switch (recordingCounter) {
			case 0:	resourceId = R.drawable.ic_1_white; break;
			case 1:	resourceId = R.drawable.ic_2_white; break;
			case 2:	resourceId = R.drawable.ic_3_white; break;
			case 3:	resourceId = R.drawable.ic_4_white; break;
			case 4:	resourceId = R.drawable.ic_5_white; break;
		}

		views[recordingCounter].setBackgroundResource(R.drawable.recording_steps_failed);
		views[recordingCounter].setImageResource(resourceId);*/
		
		//views[recordingCounter].clearAnimation();
		//views[recordingCounter].setImageResource(R.drawable.vs_wake_errecord);
		errorAni(views[recordingCounter]);
	}

	// Updates the UI if training was successful   //当成功时，进行下一项
	public void updateRecordingResult(boolean isGoodRecording) {
		//停止动画
		views[recordingCounter].setBackground(null);
		views[recordingCounter].setImageDrawable(null);
		views[recordingCounter].clearAnimation();
		views[recordingCounter].invalidate();

		//imageView.clearAnimation();
		//imageView.invalidate();
		
		if (isGoodRecording) {
			setImageDone();
			recordingCounter++;
			//setImageFocusChange();
		}else {
            Global.getInstance().discardLastUserRecording();
			setImageFailed();
		}
	}
	
	//Homekey-->onPause
	public void stopTheAnimation(){
		if(MAX_ITEM_COUNT <= recordingCounter){
			return;
		}
		views[recordingCounter].setBackground(null);
		views[recordingCounter].setImageResource(R.drawable.vs_wake_unrecord); 
		views[recordingCounter].clearAnimation();
		views[recordingCounter].invalidate();
		imageView.clearAnimation();
	}

	//开启动画
	public void startRecordingAnimation(){
		
		//首先获得View,设定动画Image
		//views[recordingCounter].setImageResource(R.drawable.vs_wake_recording_train);
		views[recordingCounter].setBackgroundResource(R.drawable.vs_wake_recording_train);
		
		//录音动画
		views[recordingCounter].setImageDrawable(null);
		ani.start();
		
		//views[recordingCounter].startAnimation(createAni());
		createAni(views[recordingCounter]);
	}
	
	
	private void createAni(ImageView view){
		/*
		//第一阶段，1.隐现
		ObjectAnimator alphaA = new ObjectAnimator().ofFloat(view, "alpha", 0.0f,1.0f);
		alphaA.setDuration(500);
		
		//第一阶段，2.缩放
		ObjectAnimator scaleA = new ObjectAnimator().ofFloat(view, "scaleX", 0.0f, 1.0f);
		ObjectAnimator scaleB = new ObjectAnimator().ofFloat(view, "scaleY", 0.0f, 1.0f);
		scaleA.setDuration(500);
		scaleB.setDuration(500);
		
		//第一阶段，3.旋转
		ObjectAnimator rotateA = new ObjectAnimator().ofFloat(view, "rotation", 0, 360);
		rotateA.setDuration(500);
		rotateA.setRepeatCount(8);
		
		AnimatorSet aSet = new AnimatorSet();
		aSet.playTogether(alphaA, scaleA, scaleB, rotateA);
		aSet.start();*/
		
		Animation ani = AnimationUtils.loadAnimation(context, R.anim.recording);
		view.startAnimation(ani);
		/*RotateAnimation rotateAni; rotateAni = new RotateAnimation(0f, 360f, 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotateAni.setInterpolator(new AccelerateDecelerateInterpolator());
		rotateAni.setDuration(850);
		rotateAni.setRepeatCount(100);*/
		
		//return rotateAni;
	}
	
	private void successedAni(final ImageView view, boolean isLast){
		//播放帧动画
		if(isLast){
			view.setBackgroundResource(R.drawable.recorded_last_success);
		}else{
			view.setBackgroundResource(R.drawable.recorded_success);
		}
		//view.setBackgroundResource(R.drawable.recorded_success);
		AnimationDrawable aniDrawable = (AnimationDrawable) view.getBackground();
		aniDrawable.setOneShot(true);
		aniDrawable.start();
		//view.setImageDrawable(null);
		
		//播放结束后，缩小
		Animation ani = AnimationUtils.loadAnimation(context, R.anim.record_success_scale);
		if(isLast){
			ani.setStartOffset(100);
			ani.setDuration(100);
		}else{
			ani.setStartOffset(400);
		}
		ani.setFillAfter(false);
		view.startAnimation(ani);
		ani.setAnimationListener(new Animation.AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				view.setImageDrawable(context.getResources().getDrawable(R.drawable.vs_wake_surecord));
				view.setBackground(null);
			}
		});
	}
	
	private void errorAni(ImageView view){
		view.setBackgroundResource(R.drawable.recorded_error);
		AnimationDrawable aniDrawable = (AnimationDrawable) view.getBackground();
		aniDrawable.setOneShot(true);
		aniDrawable.start();
		//view.setImageDrawable(null);
	}
	
	public boolean isFinished() {
	    Log.v(MYTAG, "isFinished: recordingCounter= " + recordingCounter);
		return MAX_ITEM_COUNT <= recordingCounter;
	}
	
	private Bitmap createBitmap(float radius){
		Bitmap bit = Bitmap.createBitmap((int)radius, (int)radius, Config.ARGB_8888);
		
		Canvas canvas = new Canvas(bit);
		//canvas.drawColor(Color.parseColor("#f2f2f2")); //画布没有颜色，即透明
		
		Paint paint = new Paint();
		//paint.setColor(Color.parseColor("#019c73"));
		paint.setColor(context.getResources().getColor(R.color.sound_model_compound_success));
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		
		canvas.drawCircle(radius/2, radius/2, radius/2, paint);		
		return bit;
	}
	
	private int irate = 0;
	private double fradius = 0;
	private Animator animator(final ImageView view){
		ValueAnimator ani = ValueAnimator.ofFloat(0, 180);
		ani.setDuration(1500);
		ani.setInterpolator(new DecelerateInterpolator());
		ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				
				//float value = (Float) animation.getAnimatedValue();
				//double f1 = 0.40*Math.sin(1*Math.toRadians(value) + 0.2);
				//double f2 = 0.60*Math.sin(1*Math.toRadians(value)); // *(600 - 450) + 450
				//double f = (f1+f2)*(600 - 450) + 450;
				//double f = Math.sin(Math.toRadians(value))*(650 - 450) + 450;
				
				float outter = context.getResources().getDimension(R.dimen.recording_outter);
				float inner = context.getResources().getDimension(R.dimen.recording_inner);
				//fradius = Math.random()*(490-450)+450;
				fradius = Math.random()*(outter-inner)+inner;
				if(irate%3 == 0){
					view.setImageBitmap(createBitmap((float)fradius));
				}
				irate++;
			}
		});
		ani.addListener(new ValueAnimator.AnimatorListener() {
			
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
				irate = 0;
				//ValueAnimator ani = ValueAnimator.ofFloat((float)fradius, 360);
				float end = context.getResources().getDimension(R.dimen.recording_end);
				ValueAnimator ani = ValueAnimator.ofFloat((float)fradius, end);
				ani.setDuration(500);
				ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						// TODO Auto-generated method stub
						float value = (Float) animation.getAnimatedValue();
						view.setImageBitmap(createBitmap(value));
					}
				});
				ani.start();
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		return ani;
	}

	
	class Circle {
		
		private float cx;
		private float cy;
		private float radius;
		
		public Circle(float cx, float cy, float radius){
			this.cx = cx;
			this.cy = cy;
			this.radius = radius;
		}

		public float getCx() {
			return cx;
		}

		public float getCy() {
			return cy;
		}

		public float getRadius() {
			return radius;
		}

	}
	
	
}
