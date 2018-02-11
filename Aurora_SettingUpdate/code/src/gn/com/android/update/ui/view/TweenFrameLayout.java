package gn.com.android.update.ui.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import gn.com.android.update.R;

public class TweenFrameLayout extends FrameLayout {
	private Context mContext;
	private FrameLayout.LayoutParams mLayoutParams;
	
    private ImageView mSamllCelcir; //小圈
    private ImageView mSamllRing;   //小环
    private ImageView mBigRing;     //大环
    private ImageView mBigCelcir;   //大圈
	
	private AlphaAnimation mAlpha;
	private ScaleAnimation mSmallCelcirAnim; 
	private ScaleAnimation mBigLinearAnim;
	private ScaleAnimation mBigRingOvershoot;
	private ScaleAnimation mBigCelcirOvershoot;
	
	private AnimationSet mSamllCelcirAnim;
	private AnimationSet mBigRingAnim;
	private AnimationSet mBigCelcirAnim;
	private Animation.AnimationListener mAnimationListener;
	
	public TweenFrameLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public TweenFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public TweenFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	
	public void init(Context context){
		mContext = context;
		initView();
		initAnimation();
	}
	
	private  void initView(){
		mLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,Gravity.CENTER); 
		mSamllCelcir = new ImageView(mContext);
		mSamllCelcir.setBackgroundResource(R.drawable.aurora_small_circel);
		
		mSamllRing = new ImageView(mContext);
		mSamllRing.setBackgroundResource(R.drawable.aurora_small_ring);
		
		mBigRing = new ImageView(mContext);
		mBigRing.setBackgroundResource(R.drawable.aurora_big_ring);
		
		mBigCelcir = new ImageView(mContext);
		mBigCelcir.setBackgroundResource(R.drawable.aurora_big_circel);
		
		addView(mSamllCelcir,mLayoutParams);
		addView(mSamllRing,mLayoutParams);
		addView(mBigRing,mLayoutParams);
		addView(mBigCelcir,mLayoutParams);
	}
	
	private  void initAnimation(){
		mAlpha =  new AlphaAnimation(0, 1);
		mAlpha.setDuration(800);

		mSmallCelcirAnim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		mSmallCelcirAnim.setDuration(450);
		mSmallCelcirAnim.setInterpolator(new OvershootInterpolator(2f));

		mBigLinearAnim = new ScaleAnimation(0.2f, 1.0f, 0.2f, 1.0f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		mBigLinearAnim.setDuration(650);
		mBigLinearAnim.setInterpolator(new LinearInterpolator());
		
		mBigRingOvershoot = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		mBigRingOvershoot.setDuration(850);
		mBigRingOvershoot.setInterpolator(new OvershootInterpolator(2.5f));
		
		
		mBigCelcirOvershoot = new ScaleAnimation(0.6f, 1.0f, 0.6f, 1.0f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		mBigCelcirOvershoot.setDuration(1100);
		mBigCelcirOvershoot.setInterpolator(new OvershootInterpolator(2.2f));
		
		
		mSamllCelcirAnim = new AnimationSet(false);
		mSamllCelcirAnim.addAnimation(mSmallCelcirAnim);
		
		mBigRingAnim = new AnimationSet(false);
		mBigRingAnim.addAnimation(mBigLinearAnim);
		mBigRingAnim.addAnimation(mBigRingOvershoot);
		
		mBigCelcirAnim = new AnimationSet(false);
		mBigCelcirAnim.addAnimation(mAlpha);
		mBigCelcirAnim.addAnimation(mBigCelcirOvershoot);
		
	}
	
	public void setAnimationListener(Animation.AnimationListener listener){
		mAnimationListener = listener;
	}
	
	public void startAnim() {
		mSamllCelcirAnim.setAnimationListener(mAnimationListener);
		mSamllCelcir.startAnimation(mSamllCelcirAnim);
		mBigRing.startAnimation(mBigRingAnim);
		mBigCelcir.startAnimation(mBigCelcirAnim);
	}

	public void stopAnim(){
		
		mSamllCelcirAnim.setAnimationListener(null);
		if(mSamllCelcirAnim.hasStarted() && !mSamllCelcirAnim.hasEnded()){
			mSamllCelcirAnim.cancel();
			mSamllCelcirAnim.reset();
			mSamllCelcir.clearAnimation();
		}
		
		if(mBigRingAnim.hasStarted() && !mBigRingAnim.hasEnded()){
			mBigRingAnim.cancel();
			mBigRingAnim.reset();
			mBigRing.clearAnimation();
		}
		
		if(mBigCelcirAnim.hasStarted() && !mBigCelcirAnim.hasEnded()){
			mBigCelcirAnim.cancel();
			mBigCelcirAnim.reset();
			mBigCelcir.clearAnimation();
		}
	}
	
}