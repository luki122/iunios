package com.gionee.calendar.view;

import com.android.calendar.AllInOneActivity;

import android.content.Context;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
//Gionee <jiating> <2013-04-24> modify for CR00000000  begin 
public class GNAnimationutils {
	
	
	public static Animation initSlidingAnimation(Context context,int startX , int toX,int startY,int toY){
		Log.i("initSlidingAnimation"+"startX="+startX+"toX="+toX+"startY="+startY+"toY="+toY);
		final AllInOneActivity  mAllInOneActivity=(AllInOneActivity)context;
		Animation mAnimation = new TranslateAnimation(startX,
				toX, startY, toY);
		mAnimation.setInterpolator(AnimationUtils.loadInterpolator(
				context,
				android.R.anim.accelerate_decelerate_interpolator));
		mAnimation.setDuration(300);
		mAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				Log.i( "onAnimationStart");
				//mAllInOneActivity.setMenuing(true);

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				Log.i( "onAnimationRepeat");
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				Log.i( "onAnimationEnd");
				//mAllInOneActivity.setAllInOneContentLayout();

			}
		});
		return mAnimation;
	}

	public static Animation getAgendaListItemAnimation(Context context, int position) {
        TranslateAnimation listItemAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 1,
                Animation.RELATIVE_TO_PARENT, 0,
                Animation.RELATIVE_TO_PARENT, 0,
                Animation.RELATIVE_TO_PARENT, 0);

        listItemAnimation.setDuration(450);
        listItemAnimation.setStartOffset(50 * position);
        listItemAnimation.setInterpolator(
                AnimationUtils.loadInterpolator(context, android.R.anim.accelerate_decelerate_interpolator));
		return listItemAnimation;
	}

	public static Animation gotoFuture(Context context){
		TranslateAnimation myAnimation_Translate = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, -1,
				Animation.RELATIVE_TO_PARENT, 0,
				Animation.RELATIVE_TO_PARENT, 0);

		myAnimation_Translate.setDuration(450);
		myAnimation_Translate.setFillBefore(true);
		myAnimation_Translate.setInterpolator(AnimationUtils
				.loadInterpolator(context,
						android.R.anim.accelerate_decelerate_interpolator));
		return myAnimation_Translate;
	}

	public static Animation gotoLast(Context context){
		TranslateAnimation myAnimation_Translate = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, -1,
				Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_PARENT, 0,
				Animation.RELATIVE_TO_PARENT, 0);

		myAnimation_Translate.setDuration(450);
		myAnimation_Translate.setFillAfter(true);
		myAnimation_Translate.setInterpolator(AnimationUtils
				.loadInterpolator(context,
						android.R.anim.accelerate_decelerate_interpolator));
		return myAnimation_Translate;
	}

    public static Animation changeAlphaAnimation(Context context){
        AlphaAnimation animation = new AlphaAnimation(1, 0);
        animation.setDuration(270);
        return animation;
    }

    public static Animation showTodayAnimation(Context context){
        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setDuration(300);
        return animation;
    }

	public static Animation backTodayAnumation(Context context){
		TranslateAnimation myAnimation_Translate = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 1,
				Animation.RELATIVE_TO_PARENT, 0,
				Animation.RELATIVE_TO_PARENT, 0,
				Animation.RELATIVE_TO_PARENT, 0);

		myAnimation_Translate.setDuration(300);

		myAnimation_Translate.setInterpolator(AnimationUtils
				.loadInterpolator(context,
						android.R.anim.accelerate_decelerate_interpolator));
		return myAnimation_Translate;
	}
	
	
	public static Animation editEventAddReminderClickMoreAnumation(Context context){
		TranslateAnimation myAnimation_Translate = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_PARENT, 0,
				Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_PARENT, 0);

		myAnimation_Translate.setDuration(1000);

		myAnimation_Translate.setInterpolator(AnimationUtils
				.loadInterpolator(context,
						android.R.anim.accelerate_decelerate_interpolator));
		return myAnimation_Translate;
	}
	public static Animation editEventClickMoreAnumation(Context context){
		TranslateAnimation myAnimation_Translate = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_PARENT, 0,
				Animation.RELATIVE_TO_SELF, -1,
				Animation.RELATIVE_TO_PARENT, 0);

		myAnimation_Translate.setDuration(500);

		myAnimation_Translate.setInterpolator(AnimationUtils
				.loadInterpolator(context,
						android.R.anim.accelerate_decelerate_interpolator));
		return myAnimation_Translate;
	}

}
//Gionee <jiating> <2013-04-24> modify for CR00000000  end