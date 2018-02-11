package com.android.gallery3d.fragmentapp;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;



/*
 * 3.0以前，android支持两种动画模式，tween animation,frame animation，在android3.0中又引入了一个新的动画系统：property animation
AnimationSet提供了一个把多个动画组合成一个组合的机制，并可设置组中动画的时序关系，如同时播放，顺序播放等
 */

/*
 * interpolator 被用来修饰动画效果，定义动画的变化率，可以使存在的动画效果可以 accelerated(加速)，decelerated(减速),repeated(重复),bounced(弹跳);
 * AccelerateDecelerateInterpolator 在动画开始与介绍的地方速率改变比较慢，在中间的时候加速
     AccelerateInterpolator  在动画开始的地方速率改变比较慢，然后开始加速
   AnticipateInterpolator 开始的时候向后然后向前甩
   AnticipateOvershootInterpolator 开始的时候向后然后向前甩一定值后返回最后的值
   BounceInterpolator   动画结束的时候弹起
   CycleInterpolator 动画循环播放特定的次数，速率改变沿着正弦曲线
   DecelerateInterpolator 在动画开始的地方快然后慢
     LinearInterpolator   以常量速率改变
     OvershootInterpolator    向前甩一定值后再回到原来位置
 */
public class AnimationInterpolator {

	private OnViewAnimationListener mAnimationListener = null;
	public final int rela1 = Animation.RELATIVE_TO_SELF;
	public final int rela2 = Animation.RELATIVE_TO_PARENT;
	public final int DEFAULT_DEGREE = -1;
	private int mtype = -1;
	public final int FADE_IN = 1;
	public final int FADE_OUT = 2;
	public final int SLIDE_IN = 3;
	public final int SLIDE_OUT = 4;
	public final int SCALE_IN = 5;
	public final int SCALE_OUT = 6;
	public final int ROTATE_IN = 7;
	public final int ROTATE_OUT = 8;
	public final int SCALEROTATE_IN = 9;
	public final int SCALEROTATE_OUT = 10;
	public final int SLIDEFADE_IN = 11;
	public final int SLIDEFADE_OUT = 12;
	

	public AnimationInterpolator()
	{
	}

	public interface OnViewAnimationListener{
		public void onViewAnimationEnd(int type);
		public void onViewAnimationStart(int type);
	}
	
	public void SetOnViewAnimationListener(OnViewAnimationListener onListener) {
		this.mAnimationListener = onListener;
	}
	
	private class OnAnimationListener implements AnimationListener
	{
		private View view;

		public OnAnimationListener(View view)
		{
			this.view = view;
		}

		@Override
		public void onAnimationStart(Animation animation)
		{
			// this.view.setVisibility(View.VISIBLE);
			if (mAnimationListener != null) {
				mAnimationListener.onViewAnimationStart(mtype);
			}
		}

		@Override
		public void onAnimationEnd(Animation animation)
		{
			//this.view.setVisibility(View.GONE);
			if (mAnimationListener != null) {
				mAnimationListener.onViewAnimationEnd(mtype);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation)
		{
		}
	}

	private void animation_setEffect(Animation animation, int interpolatorType, long durationMillis, long delayMillis)
	{
		switch (interpolatorType)
		{
		case 0:
			animation.setInterpolator(new LinearInterpolator());
			break;
		case 1:
			animation.setInterpolator(new AccelerateInterpolator());
			break;
		case 2:
			animation.setInterpolator(new DecelerateInterpolator());
			break;
		case 3:
			animation.setInterpolator(new AccelerateDecelerateInterpolator());
			break;
		case 4:
			animation.setInterpolator(new BounceInterpolator());
			break;
		case 5:
			animation.setInterpolator(new OvershootInterpolator());
			break;
		case 6:
			animation.setInterpolator(new AnticipateInterpolator());
			break;
		case 7:
			animation.setInterpolator(new AnticipateOvershootInterpolator());
			break;
		default:
			break;
		}
		animation.setDuration(durationMillis);
		animation.setStartOffset(delayMillis);
	}

	private void animation_baseIn(View view, Animation animation, long durationMillis, long delayMillis)
	{
		animation_setEffect(animation, DEFAULT_DEGREE, durationMillis, delayMillis);
		animation.setAnimationListener(new OnAnimationListener(view));
		view.setVisibility(View.VISIBLE);
		view.startAnimation(animation);
	}

	private void animation_baseOut(View view, Animation animation, long durationMillis, long delayMillis)
	{
		animation_setEffect(animation, DEFAULT_DEGREE, durationMillis, delayMillis);
		animation.setAnimationListener(new OnAnimationListener(view));
		view.startAnimation(animation);
	}
	
	public void animation_show(View view)
	{
		view.setVisibility(View.VISIBLE);
	}
	
	public void animation_hide(View view)
	{
		view.setVisibility(View.GONE);
	}
	
	public void animation_transparent(View view)
	{
		view.setVisibility(View.INVISIBLE);
	}
	
	public void animation_fadeIn(View view, long durationMillis, long delayMillis)
	{
		AlphaAnimation animation = new AlphaAnimation(0, 1);
		animation_baseIn(view, animation, durationMillis, delayMillis);
		mtype = FADE_IN;
	}

	public void animation_fadeOut(View view, long durationMillis, long delayMillis)
	{
		AlphaAnimation animation = new AlphaAnimation(1, 0);
		animation_baseOut(view, animation, durationMillis, delayMillis);
		mtype = FADE_OUT;
	}

	public void animation_slideIn(View view, long durationMillis, long delayMillis)
	{
		TranslateAnimation animation = new TranslateAnimation(rela2, 1, rela2, 0, rela2, 0, rela2, 0);
		animation_baseIn(view, animation, durationMillis, delayMillis);
		mtype = SLIDE_IN;
	}

	public void animation_slideOut(View view, long durationMillis, long delayMillis)
	{
		TranslateAnimation animation = new TranslateAnimation(rela2, 0, rela2, -1, rela2, 0, rela2, 0);
		animation_baseOut(view, animation, durationMillis, delayMillis);
		mtype = SLIDE_OUT;
	}

	public void animation_scaleIn(View view, long durationMillis, long delayMillis)
	{
		ScaleAnimation animation = new ScaleAnimation(0, 1, 0, 1, rela2, 0.5f, rela2, 0.5f);
		animation_baseIn(view, animation, durationMillis, delayMillis);
		mtype = SCALE_IN;
	}

	public void animation_scaleOut(View view, long durationMillis, long delayMillis)
	{
		ScaleAnimation animation = new ScaleAnimation(1, 0, 1, 0, rela2, 0.5f, rela2, 0.5f);
		animation_baseOut(view, animation, durationMillis, delayMillis);
		mtype = SCALE_OUT;
	}

	public void animation_rotateIn(View view, long durationMillis, long delayMillis)
	{
		RotateAnimation animation = new RotateAnimation(-90, 0, rela1, 0, rela1, 1);
		animation_baseIn(view, animation, durationMillis, delayMillis);
		mtype = ROTATE_IN;
	}

	public void animation_rotateOut(View view, long durationMillis, long delayMillis)
	{
		RotateAnimation animation = new RotateAnimation(0, 90, rela1, 0, rela1, 1);
		animation_baseOut(view, animation, durationMillis, delayMillis);
		mtype = ROTATE_OUT;
	}

	public void animation_scaleRotateIn(View view, long durationMillis, long delayMillis)
	{
		ScaleAnimation animation1 = new ScaleAnimation(0, 1, 0, 1, rela1, 0.5f, rela1, 0.5f);
		RotateAnimation animation2 = new RotateAnimation(0, 360, rela1, 0.5f, rela1, 0.5f);
		AnimationSet animation = new AnimationSet(false);
		animation.addAnimation(animation1);
		animation.addAnimation(animation2);
		animation_baseIn(view, animation, durationMillis, delayMillis);
		mtype = SCALEROTATE_IN;
	}

	public void animation_scaleRotateOut(View view, long durationMillis, long delayMillis)
	{
		ScaleAnimation animation1 = new ScaleAnimation(1, 0, 1, 0, rela1, 0.5f, rela1, 0.5f);
		RotateAnimation animation2 = new RotateAnimation(0, 360, rela1, 0.5f, rela1, 0.5f);
		AnimationSet animation = new AnimationSet(false);
		animation.addAnimation(animation1);
		animation.addAnimation(animation2);
		animation_baseOut(view, animation, durationMillis, delayMillis);
		mtype = SCALEROTATE_OUT;
	}

	public void animation_slideFadeIn(View view, long durationMillis, long delayMillis)
	{
		TranslateAnimation animation1 = new TranslateAnimation(rela2, 1, rela2, 0, rela2, 0, rela2, 0);
		AlphaAnimation animation2 = new AlphaAnimation(0, 1);
		AnimationSet animation = new AnimationSet(false);
		animation.addAnimation(animation1);
		animation.addAnimation(animation2);
		animation_baseIn(view, animation, durationMillis, delayMillis);
		mtype = SLIDEFADE_IN;
	}

	public void animation_slideFadeOut(View view, long durationMillis, long delayMillis)
	{
		TranslateAnimation animation1 = new TranslateAnimation(rela2, 0, rela2, -1, rela2, 0, rela2, 0);
		AlphaAnimation animation2 = new AlphaAnimation(1, 0);
		AnimationSet animation = new AnimationSet(false);
		animation.addAnimation(animation1);
		animation.addAnimation(animation2);
		animation_baseOut(view, animation, durationMillis, delayMillis);
		mtype = SLIDEFADE_OUT;
	}

	public void animation_translate(View view, long durationMillis, long delayMillis)
	{
		TranslateAnimation animation = new TranslateAnimation(rela1, -1, rela1, 0, rela1, -1, rela1, 0);
		animation_setEffect(animation, 2, durationMillis, delayMillis);
		view.setVisibility(View.VISIBLE);
		view.startAnimation(animation);
		mtype = SLIDE_IN;
	}
}
