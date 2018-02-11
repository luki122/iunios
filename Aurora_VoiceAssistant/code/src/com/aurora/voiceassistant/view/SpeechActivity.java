package com.aurora.voiceassistant.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.aurora.voiceassistant.R;
import com.aurora.voiceassistant.model.Recognizer;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.Theme;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.MediaStore.Audio;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class SpeechActivity extends Activity {
	private Context context;
	private AnimationListener animationListener =null;
	private Animation animInternalAmplify;
	private Animation animExternalAmplify;
	private Animation animRotate;
	private ObjectAnimator tran_rotate;
	private boolean isCancel;
	private boolean transFlag ;
	private  Recognizer recognizer =null;
	private int speechStep = Recognizer.MSG_ON_RESULT;
	
	private final int ERROR_NETWORK_TIMEOUT  = 1;//连接网络超时
	private final int ERROR_NETWORK_STATUS_CODE = 2;//网络异常且超重试次数
	private final int ERROR_AUDIO = 3;//录音任务错误
	private final int ERROR_SERVER = 4;//后端服务器错误
	private final int ERROR_CLIENT = 5;//客户端错误
	private final int ERROR_SPEECH_TIMEOUT = 6;//未检测到有效语音
	private final int ERROR_NO_MATCH  =7;//	无解码结果
	private final int ERROR_RECOGNIZER_BUSY = 8;//服务器繁忙
	private final int ERROR_INSUFFICIENT_PERMISSIONS = 9;//禁止操作
	private final int ERROR_PREPROCESS = 10;//预处理任务错误
	private final int ERROR_NETWORK_UNAVAILABLE = 11;//网络不可达
	private final int ERROR_NETWORK_PROTOCOL= 12;//网络协议错误
	private final int ERROR_NETWORK_IO = 13;//网络IO错误
	
	//private Timer extAnimTimer;
	private FrameLayout mSpeechLayout;
	private FrameLayout.LayoutParams layoutParams;
	private FrameLayout roundLayout;
	private DrawView mDrawView, mDrawView1, mDrawView2,mDrawView3;
	private float recordingCircleX;
	private float recordingCircleY;
	private float outterCircle;
	private float innerCircle;
	
	private DrawRingView mRingView;
	private ImageView recordingImageBg;
	private AnimationDrawable recordingInnerAnimEnter = null;
	private AnimationDrawable recordingInnerAnimHolding = null;
	private Animator recordingAnimator = null;
	private Animation loadingImageAnimation = null;

	//录音音乐
	private SoundPool soundPool;
	private SparseIntArray musicArray;
	
	private static final String NAVI_KEY_HIDE  = "navigation_key_hide"; // Settings.System 对应的键值
	private static boolean platform_U2;
	
	private TextView offlineVoiceInputTips;
	private String SHOW_VOICE_INPUT_TIPS = "showtips";
	private IntentFilter mIntentFilter = null;
	
	//shigq fix bug #11677 start
	private AudioManager mAudioManager;
	//shigq fix bug #11677 end
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vs_speech_layout);
		
		//Offline start
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(SHOW_VOICE_INPUT_TIPS);
		registerReceiver(mBroadcastReceiver, mIntentFilter);
		//Offline end
		
		platform_U2 = getResources().getBoolean(R.bool.platform_type_U2);
		
		mSpeechLayout = (FrameLayout) findViewById(R.id.vs_speech_main);
		layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
		context = this;
		isCancel = false;
		recognizer = new Recognizer(context,mHandler);
		
		soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
		musicArray = new SparseIntArray();
		musicArray.put(0, soundPool.load("/system/media/audio/ui/recordingstart.ogg", 1));
		musicArray.put(1, soundPool.load("/system/media/audio/ui/recordingend.ogg", 1));
		
		recordingImageBg = (ImageView)findViewById(R.id.speech_img_bg);
		
		/*Button btn = (Button)findViewById(R.id.speech_close_btn);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				animCancel();
				speechTranslation(false);
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						Intent intent = getIntent();
						intent.putExtra("retCode", 1);
						SpeechActivity.this.setResult(1, intent);
						closeDialog();
					}
				}, 500);
			}
		});*/
		
		ImageView image1 = (ImageView)findViewById(R.id.speech_img1);
		image1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				animCancel();
				if (recognizer == null) {
					recognizer = new Recognizer(context,mHandler);
				}
				recognizer.stopRecord();
				speechTranslation(false);
			}
		});
		
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				speechTranslation(true);
			}
		}, 200);
//		speechTranslation(true);
		
		mDrawView = new DrawView(this);
		mDrawView1 = new DrawView(this);
//		mDrawView2 = new DrawView(this);
//		mDrawView3 = new DrawView(this);
		mRingView = new DrawRingView(this);
		
		roundLayout = (FrameLayout)findViewById(R.id.roundview);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		roundLayout.addView(mDrawView, params);
		roundLayout.addView(mDrawView1, params);
//		roundLayout.addView(mDrawView2, params);
		roundLayout.addView(mRingView, params);
		
		roundLayout.requestLayout();
		
		recordingCircleX = context.getResources().getDimension(R.dimen.vs_recording_circle_X);
		recordingCircleY = context.getResources().getDimension(R.dimen.vs_recording_circle_Y);
		
		outterCircle = context.getResources().getDimension(R.dimen.vs_recording_circle_outter_radius);
		innerCircle = context.getResources().getDimension(R.dimen.vs_recording_circle_inner_radius);
		
		offlineVoiceInputTips = (TextView) findViewById(R.id.vs_offline_voice_input_tips);
		
		//shigq fix bug #11677 start
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		//shigq fix bug #11677 end
	}
	/*
	private Handler mTimerHandler = new Handler() 
	{
		@Override
		public void handleMessage(Message msg) 
		{
			super.handleMessage(msg);
			if(0 == msg.what)
			{
				externalAnimPlay();
			}
		}
	};*/
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		 switch (keyCode) {  
		 	case KeyEvent.KEYCODE_BACK:
		 		Intent intent = getIntent();
				intent.putExtra("retCode", 1);
				SpeechActivity.this.setResult(1, intent);
				closeDialog();
		 		return true;
		 }
		return super.onKeyUp(keyCode, event);
	}

	private void play(int soundId){
		AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		float maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float curVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		float volRto = curVol / maxVol;
		if(soundPool == null){
			return;
		}
		soundPool.play(soundId, volRto, volRto, 1, 0, 1);	// soundId: sound resource; volLto=volRto: volume of sound; priority=1: only this sound play; 
															// loop=0: play once; speed=1: normal speed
							
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			speechStep = msg.what;
			switch (msg.what) {
				case Recognizer.MSG_READY_START:
//					play(musicArray.get(0)); //语音准备开始
					break;
				case Recognizer.MSG_ON_START:
					
					break;
				case Recognizer.MSG_ON_ENDOFSPEECH:
					Log.e("iuni-ht", "----------------MSG_ON_ENDOFSPEECH-------------------------------");
					play(musicArray.get(1)); //语音接收结束	
					rotateAnimPlay();
					break;
				case Recognizer.MSG_ON_RESULT:
					rotateAnimCancel();
					speechTranslation(false);
					String ret;
					//Offline start
					boolean isOnLine = true;
					if (msg.obj instanceof String) {
						rotateAnimCancel();
						speechTranslation(false);
						ret = (String)msg.obj;
						isOnLine = false;
					} else {
						final List<List<String>> results = (List<List<String>>)msg.obj;
						rotateAnimCancel();
						speechTranslation(false);
						ret = showResults(results);
						if(soundPool != null){
							soundPool.release();
							Log.e("iuni-ht", "----------------MSG_ON_RESULT-------soundPool.release();------------------------");
						}
					}
					//Offline end
					if(ret != null) {
						Intent intent = getIntent();
						intent.putExtra("retCode", 0);
						intent.putExtra("content", ret);
						intent.putExtra("isOnLine", isOnLine);
						SpeechActivity.this.setResult(1, intent);
					} else {
						Intent intent = getIntent();
						intent.putExtra("retCode", -1);
						intent.putExtra("content", "其他异常!");
						SpeechActivity.this.setResult(1, intent);
					}
					if(soundPool != null){
						soundPool.release();
						Log.e("iuni-ht", "----------------MSG_ON_RESULT-------soundPool.release();------------------------");
					}
					break;
				case Recognizer.MSG_ON_ERROR:
					rotateAnimCancel();
					
					String str = null;
					int errCode = (Integer)msg.obj;
					if(ERROR_SPEECH_TIMEOUT == errCode) {
						str = "未检测到有效语音!";
					} else if(ERROR_NETWORK_TIMEOUT == errCode || ERROR_NETWORK_STATUS_CODE == errCode ||
							ERROR_NETWORK_UNAVAILABLE == errCode || ERROR_NETWORK_PROTOCOL == errCode || 
							ERROR_RECOGNIZER_BUSY == errCode) {
						
						str = "网络异常!";
					} else if(ERROR_AUDIO == errCode) {
						str = "录音错误!";
					} else {
						str = "其他异常!";
					}
					if(soundPool != null){
						soundPool.release();
					}
					speechTranslation(false);
					Intent intent = getIntent();
					intent.putExtra("retCode", -1);
					intent.putExtra("content",str);
					SpeechActivity.this.setResult(1, intent);
					if(soundPool != null){
						soundPool.release();
						Log.e("iuni-ht", "-------------------MSG_ON_ERROR----soundPool.release();------------------------");
					}
					break;
			}
		}
	};
	
	private String showResults(List<List<String>> results) {
        String result = "";
        for (List<String> list : results) {
        	 result += list.get(0);
        }
        if(result.length()>0) {
        	return result;
        }
        return null;
    }
	
	private void speechTranslation(boolean flag) {
		float offset = context.getResources().getDimension(R.dimen.vs_recording_button_offset);
		transFlag = flag;
		ValueAnimator fallAnim = null;
		
		LinearLayout animLayout =  (LinearLayout)findViewById(R.id.speech_anim);
		
		if (transFlag) {
			Log.d("DEBUG", "speechTranslation-------transflag = "+transFlag);
			Log.d("DEBUG", "speechTranslation-------musicArray.get(0) = "+musicArray.get(0));
			play(musicArray.get(0)); //语音准备开始
		}
		
		if(flag) {
			fallAnim = ObjectAnimator.ofFloat(animLayout, "translationY",-offset);
		} else {
			fallAnim = ObjectAnimator.ofFloat(animLayout, "translationY",0);
		}
		
		// 设置fallAnim动画的持续时间
		fallAnim.setDuration(500);
		// 设置fallAnim动画的插值方式：加速插值
		fallAnim.setInterpolator(new DecelerateInterpolator());
		// 为fallAnim动画添加监听器：
		fallAnim.addListener(translateAnimListener);
		
		fallAnim.start();
		
//		fallAnim.addListener(translateAnimListener);
	}
	
	
	public void closeDialog() {
		if(null != recognizer) {
			recognizer.cancelListening();
			recognizer = null;
		}
		if(null != animationListener) {
			animationListener.onAnimationEnd(null);
		}
		finish();
		overridePendingTransition(0, R.anim.vs_speech_acti_exit);
	}
	
	private void animCancel() {
		isCancel = true;
		/*
		if(null != extAnimTimer)
		{
			extAnimTimer.cancel();
			extAnimTimer = null;
		}*/
		
		if (null != recordingInnerAnimEnter) {
			if (recordingInnerAnimEnter.isRunning()) {
				recordingInnerAnimEnter.stop();
			}
			recordingInnerAnimEnter = null;
		}
		if (null != recordingInnerAnimHolding) {
			if (recordingInnerAnimHolding.isRunning()) {
				recordingInnerAnimHolding.stop();
			}
			recordingInnerAnimHolding = null;
		}
		if (null != recordingAnimator) {
			recordingAnimator.cancel();
			recordingAnimator = null;
		}
		
		if(null != animExternalAmplify) {
			animExternalAmplify.cancel();
			animExternalAmplify =null;
		}
		if(null != animInternalAmplify) {
			animInternalAmplify.cancel();
			animInternalAmplify =null;
		}
		/*ImageView image3 =  (ImageView)findViewById(R.id.speech_img3);
		image3.clearAnimation();
		image3.setVisibility(View.GONE);
		ImageView image4 =  (ImageView)findViewById(R.id.speech_img4);
		image4.clearAnimation();
		image4.setVisibility(View.GONE);*/
	}
	
	private AnimationDrawable getRecordingInnerEnterAnimation() {
		recordingImageBg = (ImageView)findViewById(R.id.speech_img_bg);
		recordingImageBg.setBackgroundResource(R.anim.vs_recording_inner_enter_anim);
		recordingInnerAnimEnter = (AnimationDrawable) recordingImageBg.getBackground();
		return recordingInnerAnimEnter;
	}
	
	private AnimationDrawable getRecordingInnerHoldingAnimation() {
		recordingImageBg = (ImageView)findViewById(R.id.speech_img_bg);
		recordingImageBg.setBackgroundResource(R.anim.vs_recording_inner_holding_anim);
		recordingInnerAnimHolding = (AnimationDrawable) recordingImageBg.getBackground();
		return recordingInnerAnimHolding;
	}
	
	private void rotateAnimCancel() {
		animCancel();
		if(null != animRotate) {
			animRotate.cancel();
			animRotate =null;
		}
		ImageView image2 =  (ImageView)findViewById(R.id.speech_img2);
		image2.clearAnimation();
		image2.setVisibility(View.GONE);
	}
	
	private void internalAnimPlay() {
		if(isCancel) return;
		/*ImageView image3 =  (ImageView)findViewById(R.id.speech_img3);
		image3.setVisibility(View.VISIBLE);
		animInternalAmplify = AnimationUtils.loadAnimation(context, R.anim.vs_speech_internal_amplify);
		animInternalAmplify.setAnimationListener(internalAmplifyAnimListener);
		image3.setAnimation(animInternalAmplify);*/
		
		
	}
	
	private void externalAnimPlay() {
		/*if(isCancel) return;
		ImageView image4 =  (ImageView)findViewById(R.id.speech_img4);
		image4.setVisibility(View.VISIBLE);
		animExternalAmplify = AnimationUtils.loadAnimation(context, R.anim.vs_speech_external_amplify);
		animExternalAmplify.setAnimationListener(externalAmplifyAnimListener);
		image4.setAnimation(animExternalAmplify);*/
		
		recordingAnimator = getRecordingAnimator();
		recordingAnimator.start();
		
	}
	
	//旋转动画
	private void rotateAnimPlay() {
		//先取消其他动画
		animCancel();
		
		final ImageView image2 = (ImageView) findViewById(R.id.speech_img2);
		image2.setVisibility(View.VISIBLE);
		image2.setBackgroundResource(R.anim.vs_speech_translating);
		AnimationDrawable aniDrawable = (AnimationDrawable) image2.getBackground();
		aniDrawable.setOneShot(true);
		aniDrawable.start();
		
		tran_rotate = ObjectAnimator.ofFloat(image2, "rotation", 0f,360f);
		tran_rotate.setDuration(500);
		tran_rotate.setInterpolator(new LinearInterpolator());
		tran_rotate.setRepeatCount(50);
		tran_rotate.setStartDelay(750);
		tran_rotate.start();
		tran_rotate.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				image2.setBackgroundResource(R.drawable.vs_speech_translating_25);
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
		
		/*ImageView image2 =  (ImageView)findViewById(R.id.speech_img2);
		image2.setVisibility(View.VISIBLE);
		animRotate = AnimationUtils.loadAnimation(context, R.anim.vs_speech_rotate);
		animRotate.setAnimationListener(rotateAnimListener);
		image2.setAnimation(animRotate);*/
	}
	
	private AnimatorListener translateAnimListener = new AnimatorListener() {
		@Override
		public void onAnimationStart(Animator animation) {
			restoreNavigationbarBackgroundColor();
			
			if (!transFlag) {
//				recordingImageBg.setVisibility(View.GONE);
				roundLayout.removeAllViews();
			} else {
				recordingImageBg.setVisibility(View.VISIBLE);
			}
			
			//shigq fix bug #11677 start
			mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
			//shigq fix bug #11677 end
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) {
			
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
			if(transFlag) {
//				play(musicArray.get(0)); //语音准备开始
				
//				internalAnimPlay();
				recordingInnerAnimEnter = getRecordingInnerEnterAnimation();
				recordingInnerAnimHolding = getRecordingInnerHoldingAnimation();
				recordingInnerAnimEnter.start();
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (null != recordingInnerAnimHolding) {
							recordingInnerAnimHolding.start();
						}
					}
				}, 500);
				externalAnimPlay();
				/*
				extAnimTimer  = new Timer();
				extAnimTimer.schedule(new TimerTask() 
				{
					@Override
					public void run() 
					{
						Message msg = Message.obtain();
						msg.what = 0;
						mTimerHandler.sendMessage(msg);
					}
				}, 1000, 2000);*/
				
				//shigq add
				if (recognizer == null) {
					recognizer = new Recognizer(context,mHandler);
				}
				//shigq add
				recognizer.startListening();
			} else {
				closeDialog();
			}
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private AnimationListener internalAmplifyAnimListener = new AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {
			
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
			
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			externalAnimPlay();
			
		}
	};
	
	private AnimationListener externalAmplifyAnimListener = new AnimationListener() {
		
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
			ImageView image4 =  (ImageView)findViewById(R.id.speech_img4);
			image4.setVisibility(View.GONE);
			internalAnimPlay();
		}
	};
	
	
	/*private AnimationListener rotateAnimListener = new AnimationListener() {
		
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
			
		}
	};*/
	
	private AnimationListener loadingAnimListener = new AnimationListener() {
		
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
			
		}
	};
	
	
	public Animator getRecordingAnimator() {
		ArrayList<Animator> mAnimatorArray = new ArrayList<Animator>();

		Animator mAnimator1 = getRecordingCircleViewAnimator(mDrawView);
		mAnimatorArray.add(mAnimator1);
		
		Animator mRingAnimator = getRecordingRingViewAnimator(mRingView);
		mRingAnimator.setStartDelay(200);	//200
		mAnimatorArray.add(mRingAnimator);

		Animator mAnimator2 = getRecordingCircleViewAnimator(mDrawView1);
		mAnimator2.setStartDelay(400);
		mAnimatorArray.add(mAnimator2);
		
		/*Animator mAnimator3 = getRecordingCircleViewAnimator(mDrawView2);
		mAnimator3.setStartDelay(300);
		mAnimatorArray.add(mAnimator3);*/
		
		AnimatorSet mAnimatorSet = new AnimatorSet();
		mAnimatorSet.playTogether(mAnimatorArray);
		
		mAnimatorSet.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator arg0) {
//				Log.d("DEBUG", "getRecordingAnimator----------- onAnimationStart");
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
//				Log.d("DEBUG", "getRecordingAnimator----------- onAnimationRepeat");
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
//				Log.d("DEBUG", "getRecordingAnimator----------- onAnimationEnd");
				if (recordingAnimator != null) {
					recordingAnimator.setStartDelay(350);
					recordingAnimator.start();
				}
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
//				Log.d("DEBUG", "getRecordingAnimator----------- onAnimationCancel");
				
			}
		});
		
		return mAnimatorSet;
	}
	
	public Animator getRecordingCircleViewAnimator(final DrawView view) {
		//draw one circle at once
		final DrawView.CircleInfo circleInfo = new DrawView(context).new CircleInfo();
		//circleInfo.setX(540f);
		circleInfo.setX(recordingCircleX);
//        circleInfo.setY(1790f);		//1790f for full screen mode
		//circleInfo.setY(1715f);		//1715f for no full screen mode, it needs to delete the gap = 25*3 = 75f
		circleInfo.setY(recordingCircleY);
        circleInfo.setColor(Color.GRAY);
        
        ValueAnimator animator = ValueAnimator.ofFloat(outterCircle, innerCircle);
        animator.setDuration(500);	//500
        animator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float value = (Float)arg0.getAnimatedValue();
				
				//draw the circle with a series of ratio and add to list
				/*DrawView.CircleInfo circleInfo = new DrawView(context).new CircleInfo();
		        circleInfo.setX(540f);
		        circleInfo.setY(1720f);
		        circleInfo.setRadius(value);
		        circleInfo.setColor(Color.RED);
		        mDrawView.mCircleInfos.add(circleInfo);*/
				
		        circleInfo.setRadius(value);
		        view.setCircleInfo(circleInfo);
		        view.setAlpha(Math.abs((value - 100)/200 - 1));		//the alpha from 0 to 1
		        if (value == 100f) {
		        	view.setAlpha(0f);
		        }
				
		        view.invalidate();                    //  使画布重绘
			}
		});
		
		return animator;
	}
	
	public Animator getRecordingRingViewAnimator(final DrawRingView ringView) {
		
		final DrawRingView.RingInfo ringInfo = new DrawRingView(context).new RingInfo();
		//ringInfo.setX(540f);
		ringInfo.setX(recordingCircleX);
//		ringInfo.setY(1790f);		//1790f for full screen mode
		//ringInfo.setY(1715f);		//1715f for no full screen mode, it needs to delete the gap = 25*3 = 75f
		ringInfo.setY(recordingCircleY);
		ringInfo.setColor(Color.GRAY);
		
        ValueAnimator animator = ValueAnimator.ofFloat(outterCircle, innerCircle);
        animator.setDuration(500);		//500
        
        animator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float value = (Float)arg0.getAnimatedValue();
				
				ringInfo.setOuterRadius(value);
				ringView.setRingInfo(ringInfo);
//				ringView.setAlpha(Math.abs((value - 100)/200 - 1));		//the alpha from 0 to 1
				ringView.setAlpha(Math.abs((300 - value)/400));		//the alpha from 0 to 0.5
				if (value == 100f) {
		        	ringView.setAlpha(0f);
		        }
								
				ringView.invalidate();                    //  使画布重绘
			}
		});
        
        ValueAnimator innerAnimator = ValueAnimator.ofFloat(outterCircle, innerCircle);
        innerAnimator.setDuration(350);
        innerAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float value = (Float)arg0.getAnimatedValue();
				
				ringInfo.setInnerRadius(value);
				ringView.setRingInfo(ringInfo);
			}
		});
        
        AnimatorSet mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(animator, innerAnimator);
        return mAnimatorSet;
	}
	
//	public Animation getLoadingImageAnimator(ImageView imageView) {
//		imageView.setVisibility(View.VISIBLE);
//		Animation loadingImageAnimation = AnimationUtils.loadAnimation(context, R.anim.vs_speech_rotate);
//		loadingImageAnimation.setAnimationListener(loadingAnimListener);
//		imageView.setAnimation(loadingImageAnimation);
//		
//		
//
//		return loadingImageAnimation;
//	}
	
	public class DrawView extends View {  
		
		//  保存绘制历史
	    public List<CircleInfo> mCircleInfos = new ArrayList<DrawView.CircleInfo>();
	    
	    public CircleInfo circleInfo;
	    
		// 保存实心圆相关信息的类
	    public class CircleInfo {
	        private float x;                //  圆心横坐标
	        private float y;                //  圆心纵坐标
	        private float radius;            //  半径
	        private int color;            //  画笔的颜色
	        
	        public CircleInfo() {
	        	
	        }

	        public float getX() {
	            return x;
	        }
	        
	        public void setX(float x) {
	            this.x = x;
	        }
	        
	        public float getY() {
	            return y;
	        }
	        
	        public void setY(float y) {
	            this.y = y;
	        }
	        
	        public float getRadius() {
	            return radius;
	        }
	        
	        public void setRadius(float radius) {
	            this.radius = radius;
	        }
	        
	        public int getColor() {
	            return color;
	        }
	        
	        public void setColor(int color) {
	            this.color = color;
	        }
	    }
	    
		public DrawView(Context context) {  
		    super(context);  
		}
		
		public void setCircleInfo(CircleInfo c) {
			circleInfo = c;
		}
		
		@Override  
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);  
			
			//  根据保存的绘制历史重绘所有的实心圆
	        /*for (CircleInfo circleInfo : mCircleInfos) {
	            Paint paint = new Paint();
	            if (circleInfo != null) {
		            paint.setColor(circleInfo.getColor());	//  设置画笔颜色
		            paint.setStyle(Paint.Style.STROKE);		//  绘制空心圆
		            canvas.drawCircle(circleInfo.getX(), circleInfo.getY(), circleInfo.getRadius(), paint);
	            }
	        }*/
			
			//draw one circle at once
			Paint paint = new Paint();
            if (circleInfo != null) {
            	paint.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了
	            paint.setColor(circleInfo.getColor());	//  设置画笔颜色
	            paint.setStyle(Paint.Style.STROKE);		//  绘制空心圆
	            paint.setStrokeWidth(2);
	            paint.setAlpha(160);
	            canvas.drawCircle(circleInfo.getX(), circleInfo.getY(), circleInfo.getRadius(), paint);
            }
			
		}
	}
	
	public class DrawRingView extends View {  
	    public RingInfo ringInfo;
	    
	    public class RingInfo {
	        private float x;                //  圆心横坐标
	        private float y;                //  圆心纵坐标
	        private float outerRadius;            //  半径
	        private float innerRadius;            //  半径
	        private int color;            //  画笔的颜色
	        
	        public RingInfo() {
	        	
	        }

	        public float getX() {
	            return x;
	        }
	        
	        public void setX(float x) {
	            this.x = x;
	        }
	        
	        public float getY() {
	            return y;
	        }
	        
	        public void setY(float y) {
	            this.y = y;
	        }
	        
	        public float getInnerRadius() {
	            return innerRadius;
	        }
	        
	        public void setInnerRadius(float radius) {
	            this.innerRadius = radius;
	        }
	        
	        public float getOuterRadius() {
	            return outerRadius;
	        }
	        
	        public void setOuterRadius(float radius) {
	            this.outerRadius = radius;
	        }
	        
	        public int getColor() {
	            return color;
	        }
	        
	        public void setColor(int color) {
	            this.color = color;
	        }
	    }
	    
		public DrawRingView(Context context) {  
		    super(context);  
		}
		
		public void setRingInfo(RingInfo c) {
			ringInfo = c;
		}
		
		@Override  
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);  
			
			Paint paint = new Paint();
            if (ringInfo != null) {
	            paint.setColor(ringInfo.getColor());	//  设置画笔颜色
	            paint.setStyle(Paint.Style.STROKE);		//  绘制空心圆
//	            paint.setStrokeWidth(2);
//	            canvas.drawCircle(ringInfo.getX(), ringInfo.getY(), ringInfo.getRadius(), paint);
	            
	            //绘制内圆  
//	            this.paint.setARGB(155, 167, 190, 206);
//	            this.paint.setStrokeWidth(2);
//	            canvas.drawCircle(ringInfo.getX(), ringInfo.getY(), ringInfo.getInnerRadius(), paint);  
	            
	            paint.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了
	            
	            //绘制圆环  
//	            this.paint.setARGB(255, 212 ,225, 233);  
//	            this.paint.setStrokeWidth(ringWidth);
	            float ringWidth = ringInfo.getOuterRadius() - ringInfo.getInnerRadius();
	            paint.setStrokeWidth(ringWidth);
	            paint.setAlpha(160);
	            canvas.drawCircle(ringInfo.getX(), ringInfo.getY(), ringInfo.getInnerRadius() + ringWidth, paint); 
	            
	            
	            
	            //绘制外圆  
//	            this.paint.setARGB(155, 167, 190, 206);  
//	            this.paint.setStrokeWidth(2);  
//	            canvas.drawCircle(ringInfo.getX(), ringInfo.getY(), ringInfo.getOuterRadius(), paint);
            }
			
		}
	}
	
	public void clearSpeechLayoutMargin() {
		//for Android5.0 adaption start
		    //VERSION_CODES.LOLLIPOP = 21
//		if (VERSION.SDK_INT > 18) {
		if (VERSION.SDK_INT > 18 && VERSION.SDK_INT < 21) {
		//for Android5.0 adaption end
		
			layoutParams.bottomMargin = 0;
			mSpeechLayout.setLayoutParams(layoutParams);
		}
	}
		
	/**set Navigation bar background to dark*/
	private void setNavigationbarBackgroundColor(){
		//for Android5.0 adaption start
		//VERSION_CODES.LOLLIPOP = 21
//		if (VERSION.SDK_INT > 18) {
		if (VERSION.SDK_INT > 18 && VERSION.SDK_INT < 21) {
		//for Android5.0 adaption end
			
	        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN);
		}
	}
	
	/**restore Navigation bar background */
	public void restoreNavigationbarBackgroundColor(){
		//for Android5.0 adaption start
		//VERSION_CODES.LOLLIPOP = 21
//		if (VERSION.SDK_INT > 18) {
		if (VERSION.SDK_INT > 18 && VERSION.SDK_INT < 21) {
		//for Android5.0 adaption end
			
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
	        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(soundPool != null){
			soundPool.release();
		}
		//Offline start
		/*if(mPocketAPI != null) {
			mPocketAPI.destroy();
		}*/
		
		mIntentFilter = null;
		unregisterReceiver(mBroadcastReceiver);
		//Offline end
		
		//shigq fix bug #11677 start
		mAudioManager.abandonAudioFocus(null);
		//shigq fix bug #11677 end
	}

	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			String actionString = intent.getAction();
			if (actionString.equals(SHOW_VOICE_INPUT_TIPS)) {
				boolean isShow = intent.getBooleanExtra("showTipsState", false);
				Log.d("DEBUG", "isshow =================== "+isShow);
				offlineVoiceInputTips.setVisibility(View.VISIBLE);
			}
		}
		
	};
}
