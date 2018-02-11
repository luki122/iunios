/**
 * Vulcan created this file in 2014年11月12日 上午9:41:15 .
 */
package com.privacymanage.activity;

import java.util.ArrayList;
import java.util.Iterator;

import com.privacymanage.utils.LogUtils;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Vulcan created CinemaMan in 2014年11月12日 .
 * 
 */
public class CinemaMan {
	
	Activity mHostContext = null;

	/**
	 * 
	 */
	public CinemaMan(Activity activity) {
		mHostContext = activity;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 下午5:37:57 .
	 */
	public void playAnimHowEnter() {
		Log.d("vfirst","playAnimHowEnter");
		Animation animHandset = (Animation)AnimationUtils.loadAnimation(mHostContext, ResIdMan.ANIM_HOW_ENTER_HANDSET);
		ImageView ivHandset = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HANDSET);
		
		Animation animPlanet1 = AnimationUtils.loadAnimation(mHostContext, ResIdMan.ANIM_HOW_ENTER_PLANET1);
		ImageView ivPlanet1 = (ImageView)findViewById(ResIdMan.IMAGEVIEW_PLANET1);
		
		Animation animPlanet2 = AnimationUtils.loadAnimation(mHostContext, ResIdMan.ANIM_HOW_ENTER_PLANET2);
		ImageView ivPlanet2 = (ImageView)findViewById(ResIdMan.IMAGEVIEW_PLANET2);
		
		Animation animRocket = AnimationUtils.loadAnimation(mHostContext, ResIdMan.ANIM_HOW_ENTER_ROCKET);
		ImageView ivRocket = (ImageView)findViewById(ResIdMan.IMAGEVIEW_ROCKET);
		
		Animation animTextHowEnter= AnimationUtils.loadAnimation(mHostContext, ResIdMan.ANIM_TEXT_HOW_ENTER);
		TextView tvHowEnter = (TextView)findViewById(ResIdMan.TEXTVIEW_HOW_ENTER);
		
		Animation animTextEnterMethod= AnimationUtils.loadAnimation(mHostContext, ResIdMan.ANIM_TEXT_ENTER_METHOD);
		TextView tvEnterMethod = (TextView)findViewById(ResIdMan.TEXTVIEW_ENTER_METHOD);
		
		TextView[] tvHandsetDialNumbers = findViewsHandsetDialNumber();
		AlphaAnimation animDialNumAlpha0 = (AlphaAnimation)AnimationUtils.loadAnimation(mHostContext, ResIdMan.ANIM_HOW_ENTER_DIAL_NUMBER_ALPHA);
		final long firstOffset = animDialNumAlpha0.getStartOffset();
		final long duration = animDialNumAlpha0.getDuration();
		long thisOffset = firstOffset;
		for(int ii = 0; ii < tvHandsetDialNumbers.length; ii ++) {
			AlphaAnimation animDialNumAlpha = new AlphaAnimation(0,1);
			animDialNumAlpha.setStartOffset(thisOffset);
			animDialNumAlpha.setDuration(duration);
			AnimationSet animDialNumber = new AnimationSet(false);
			animDialNumber.addAnimation(animDialNumAlpha);
			tvHandsetDialNumbers[ii].startAnimation(animDialNumber);
			thisOffset += duration;
		}

		ivHandset.startAnimation(animHandset);
		ivPlanet1.startAnimation(animPlanet1);
		ivPlanet2.startAnimation(animPlanet2);
		ivRocket.startAnimation(animRocket);
		tvHowEnter.startAnimation(animTextHowEnter);
		tvEnterMethod.startAnimation(animTextEnterMethod);

		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年12月17日 下午3:41:51 .
	 */
	public void playAnimHowProtect() {
		Log.d("vfirst","playAnimHowProtect");
		
		mCinemaHowProtect.startBySystem();
		mCinemaHowProtectLeftTextIn.startBySystem();

		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 上午9:58:10 .
	 * @param callerType
	 */
	private void createCinemas(int launchMode) {
		mTextCinemaSetListByPage.addAll(createTextCinemaListByPage(launchMode));
		mTextCinemaSetListByPage.add(mTextCinemaInPageNoUse);
		mMainCinemaListByPage.addAll(createMainCinemaListByPage(launchMode));
		mMainCinemaListByPage.add(mCinemaNoUse);
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 下午4:57:44 .
	 * @param callerType
	 */
	public void create(int launchMode) {
		if (!mCinemaIsCreated) {
			createCinemas(launchMode);
			createAnimsByCallerType(launchMode);
			mCinemaIsCreated = true;
		}
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 下午5:00:29 .
	 * @return
	 */
	public boolean cinemaIsCreated() {
		return mCinemaIsCreated;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 下午5:03:03 .
	 * @return
	 */
	public boolean cinemaIsPlaying() {
		return mCinemaIsPlaying;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 下午1:07:17 .
	 * @param page
	 */
	public void startPageCinema(int page) {
		startPageMainCinema(page);
		startPageTextCinemaRTC(page);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年12月18日 上午10:47:06 .
	 * @param page
	 * @param bySystem
	 */
	public void startPageCinema(int page, boolean bySystem) {
		startPageMainCinema(page,bySystem);
		startPageTextCinemaRTC(page, bySystem);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 上午10:08:51 .
	 * Hide the main cinema and text cinema in one page
	 * @param page
	 */
	public void hidePage(int page) {
		mMainCinemaListByPage.get(page).hide();
		for(Cinema c: mTextCinemaSetListByPage.get(page)) {
			c.hide();
		}
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 上午10:16:23 .
	 * @param page
	 */
	public void startPageMainCinema(int page) {
		mMainCinemaListByPage.get(page).start();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年12月18日 上午10:47:35 .
	 * @param page
	 * @param bySystem
	 */
	public void startPageMainCinema(int page, boolean bySystem) {
		if (bySystem) {
			mMainCinemaListByPage.get(page).startBySystem();
		} else {
			mMainCinemaListByPage.get(page).start();
		}
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 上午10:49:49 .
	 * @param page
	 */
	public void hidePageMainCinema(int page) {
		mMainCinemaListByPage.get(page).hide();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 上午10:18:17 .
	 * @param page
	 * @param cinemaIndex
	 */
	public void startPageTextCinema(int page, int cinemaIndex) {
		mTextCinemaSetListByPage.get(page)[cinemaIndex].start();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年12月18日 上午10:50:42 .
	 * @param page
	 * @param cinemaIndex
	 * @param bySystem
	 */
	public void startPageTextCinema(int page, int cinemaIndex, boolean bySystem) {
		if(bySystem) {
			LogUtils.printWithLogCat("vopt", "startBySystem: cinema = " + mTextCinemaSetListByPage.get(page)[cinemaIndex]);
			mTextCinemaSetListByPage.get(page)[cinemaIndex].startBySystem();
		}
		else {
			mTextCinemaSetListByPage.get(page)[cinemaIndex].start();
		}
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 上午10:27:12 .
	 * @param page
	 * @param cinemaIndex
	 */
	public void hidePageTextCinema(int page, int cinemaIndex) {
		mTextCinemaSetListByPage.get(page)[cinemaIndex].hide();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 上午10:19:40 .
	 * @param page
	 */
	public void startPageTextCinemaLTC(int page) {
		hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_CENTER_TO_LEFT);
		hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_RIGHT_TO_CENTER);
		hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_CENTER_TO_RIGHT);
		startPageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_LEFT_TO_CENTER);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 上午10:44:22 .
	 * @param page
	 */
	public void startPageTextCinemaRTC(int page) {
		hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_CENTER_TO_LEFT);
		startPageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_RIGHT_TO_CENTER);
		hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_CENTER_TO_RIGHT);
		hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_LEFT_TO_CENTER);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年12月18日 上午10:49:50 .
	 * @param page
	 * @param bySystem
	 */
	public void startPageTextCinemaRTC(int page, boolean bySystem) {
		hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_CENTER_TO_LEFT);
		startPageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_RIGHT_TO_CENTER, bySystem);
		hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_CENTER_TO_RIGHT);
		hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_LEFT_TO_CENTER);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 上午10:44:38 .
	 * @param page
	 */
	public void startPageTextCinemaCTL(int page) {
		startPageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_CENTER_TO_LEFT);
		//hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_RIGHT_TO_CENTER);
		//hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_CENTER_TO_RIGHT);
		//hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_LEFT_TO_CENTER);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 上午10:45:07 .
	 * @param page
	 */
	public void startPageTextCinemaCTR(int page) {
		//hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_CENTER_TO_LEFT);
		//hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_RIGHT_TO_CENTER);
		startPageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_CENTER_TO_RIGHT);
		//hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_LEFT_TO_CENTER);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 上午11:04:53 .
	 * @param page
	 * @param percent
	 */
	public void setPageMainCinemePlayProgress(int page, long percent) {
		mMainCinemaListByPage.get(page).setCurrentPlayProgress(percent);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 上午11:07:45 .
	 * @param page
	 * @param percent
	 */
	public void setPageTextCinemaPlayProgress(int page,int cinemaIndex, long percent) {
		mTextCinemaSetListByPage.get(page)[cinemaIndex].setCurrentPlayProgress(percent);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 上午11:13:30 .
	 * @param page
	 * @param percent
	 */
	public void setPageTextCinemaPlayProgressCTL(int page, long percent) {
		setPageTextCinemaPlayProgress(page,Cinema.TEXT_CINEMA_INDEX_CENTER_TO_LEFT,percent);
		//hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_RIGHT_TO_CENTER);
		//hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_CENTER_TO_RIGHT);
		//hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_LEFT_TO_CENTER);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 上午11:14:09 .
	 * @param page
	 * @param percent
	 */
	public void setPageTextCinemaPlayProgressRTC(int page, long percent) {
		
		//hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_CENTER_TO_LEFT);
		setPageTextCinemaPlayProgress(page,Cinema.TEXT_CINEMA_INDEX_RIGHT_TO_CENTER, percent);
		//hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_CENTER_TO_RIGHT);
		//hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_LEFT_TO_CENTER);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 下午6:05:23 .
	 * @param page
	 * @param percent
	 */
	public void setPageTextCinemaPlayProgressCTR(int page, long percent) {
		
		//hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_CENTER_TO_LEFT);
		setPageTextCinemaPlayProgress(page,Cinema.TEXT_CINEMA_INDEX_CENTER_TO_RIGHT, percent);
		//hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_CENTER_TO_RIGHT);
		//hidePageTextCinema(page,Cinema.TEXT_CINEMA_INDEX_LEFT_TO_CENTER);
	}
	
	protected class CinemaHowEnterRightTextIn extends  CinemaHowEnterText {
		public CinemaHowEnterRightTextIn() {
			super();
			mTextStartPosition = TEXT_START_POSITION_RIGHT;
			mTextEndPosition = TEXT_START_POSITION_CENTER;
		}
	}
	private class CinemaHowEnterLeftTextIn extends  CinemaHowEnterText {
		public CinemaHowEnterLeftTextIn() {
			super();
			mTextStartPosition = TEXT_START_POSITION_LEFT;
			mTextEndPosition = TEXT_START_POSITION_CENTER;
		}
	}
	private class CinemaHowEnterTextToRight extends  CinemaHowEnterText {
		public CinemaHowEnterTextToRight() {
			super();
			mTextStartPosition = TEXT_START_POSITION_CENTER;
			mTextEndPosition = TEXT_START_POSITION_RIGHT;
		}
	}
	public class CinemaHowEnterTextToLeft extends  CinemaHowEnterText {
		public CinemaHowEnterTextToLeft() {
			super();
			mTextStartPosition = TEXT_START_POSITION_CENTER;
			mTextEndPosition = TEXT_START_POSITION_LEFT;
		}
	}
	
	private class CinemaHowEnterText extends Cinema {
		
		/**
		 * 
		 * @param start
		 * @param end
		 */
		private CinemaHowEnterText() {
			super();
			mCinemaMan = CinemaMan.this;
		}

		@Override
		protected void createAnim() {
			final boolean isIn = textIsIn();
			final float alphaStart = isIn?0f:1f;
			final float alphaEnd = isIn?1f:0f;
			
			//tvEnterMethod.getLeft() + tvEnterMethod.getWidth() * 0.3f * mTextStartPosition,

			//text how enter
			TextView tvHowEnter = (TextView)findViewById(ResIdMan.TEXTVIEW_HOW_ENTER);
			ObjectAnimator animTextViewHowEnterOffsetX = ObjectAnimator.ofFloat(tvHowEnter, "x", 
																		tvHowEnter.getLeft() + tvHowEnter.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextStartPosition,
																		tvHowEnter.getLeft() + tvHowEnter.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextEndPosition);
			mAnimatorList.add(animTextViewHowEnterOffsetX);
			
			ObjectAnimator animTextViewHowEnterAlpha = ObjectAnimator.ofFloat(tvHowEnter, "alpha", alphaStart, alphaEnd);
			mAnimatorList.add(animTextViewHowEnterAlpha);
			
			//text enter method
			TextView tvEnterMethod = (TextView)findViewById(ResIdMan.TEXTVIEW_ENTER_METHOD);
			ObjectAnimator animTextViewEnterMethodOffsetX = ObjectAnimator.ofFloat(tvEnterMethod, "x", 
					tvEnterMethod.getLeft() + tvEnterMethod.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextStartPosition,
					tvEnterMethod.getLeft() + tvEnterMethod.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextEndPosition);
			mAnimatorList.add(animTextViewEnterMethodOffsetX);

			ObjectAnimator animTextViewEnterMethodAlpha = ObjectAnimator.ofFloat(tvEnterMethod, "alpha", alphaStart, alphaEnd);
			mAnimatorList.add(animTextViewEnterMethodAlpha);
			
			//uniform duration and interpolator
			for(Animator anim: mAnimatorList) {
				anim.setInterpolator(new DecelerateInterpolator());
				anim.setDuration(DURATION_BASE);
			}

			//adjust again
			animTextViewHowEnterAlpha.setInterpolator(ALPHA_INTERPOLATOR);

			animTextViewEnterMethodAlpha.setInterpolator(ALPHA_INTERPOLATOR);
			animTextViewEnterMethodOffsetX.setStartDelay(TEXT_ANIM_DELAY);
			animTextViewEnterMethodOffsetX.setDuration(DURATION_BASE);
		}
		
	}
	
	private class CinemaHowEnter extends Cinema {
		
		final long dialNumAnimDuration = 20;
		final long animDuration = DURATION_BASE;
		final float animOffsetX = -100f;
		final float animOffsetY = 100f;
		final float animRotate = -90f;
		
		public CinemaHowEnter() {
			mCinemaType = CINEMA_TYPE_MAIN;
			mCinemaMan = CinemaMan.this;
		}
		
		
		@Override
		protected long getDuration() {
			return animDuration + dialNumAnimDuration * 8;
		}

		/**
		 * 
		 * Vulcan created this method in 2014年10月17日 下午3:16:25 .
		 */
		protected void createAnim() {
			
			//stars' animation 
			ImageView ivStars = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HOW_ENTER_STARS);
			ObjectAnimator animStarsAlpha = ObjectAnimator.ofFloat(ivStars, "alpha", 0f, 1f);
			mAnimatorList.add(animStarsAlpha);

			//handset's animation
			ImageView ivHandset = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HANDSET);
			ObjectAnimator animHandsetAlpha = ObjectAnimator.ofFloat(ivHandset, "alpha", 0f, 1f);
			mAnimatorList.add(animHandsetAlpha);
			
			ObjectAnimator animHandsetRotate = ObjectAnimator.ofFloat(ivHandset, "rotation", animRotate, 0);
			mAnimatorList.add(animHandsetRotate);
			//Log.d("vanim",String.format("createAnim: pivotX = %f,pivotY = %f" ,ivHandset.getPivotX(), ivHandset.getPivotY()));
			
			//planet1's animation
			ImageView ivPlanet1 = (ImageView)findViewById(ResIdMan.IMAGEVIEW_PLANET1);
			ObjectAnimator animPlanet1OffsetX = ObjectAnimator.ofFloat(ivPlanet1, "x", animOffsetX + ivPlanet1.getLeft() , ivPlanet1.getLeft());
			mAnimatorList.add(animPlanet1OffsetX);
			
			ObjectAnimator animPlanet1Alpha = ObjectAnimator.ofFloat(ivPlanet1, "alpha", 0f, 1f);
			mAnimatorList.add(animPlanet1Alpha);
			
			//planet2's animation
			ImageView ivPlanet2 = (ImageView)findViewById(ResIdMan.IMAGEVIEW_PLANET2);
			ObjectAnimator animPlanet2OffsetX = ObjectAnimator.ofFloat(ivPlanet2, "x", ivPlanet2.getLeft() + animOffsetX, ivPlanet2.getLeft());
			mAnimatorList.add(animPlanet2OffsetX);
			
			ObjectAnimator animPlanet2Alpha = ObjectAnimator.ofFloat(ivPlanet2, "alpha", 0f, 1f);
			mAnimatorList.add(animPlanet2Alpha);
			
			//rocket's animation
			ImageView ivRocket = (ImageView)findViewById(ResIdMan.IMAGEVIEW_ROCKET);
			ObjectAnimator animRocketOffsetX = ObjectAnimator.ofFloat(ivRocket, "x", ivRocket.getLeft() + animOffsetX, ivRocket.getLeft());
			mAnimatorList.add(animRocketOffsetX);
			
			ObjectAnimator animRocketOffsetY = ObjectAnimator.ofFloat(ivRocket, "y", ivRocket.getTop() + animOffsetY, ivRocket.getTop());
			mAnimatorList.add(animRocketOffsetY);
			
			ObjectAnimator animRocketAlpha = ObjectAnimator.ofFloat(ivRocket, "alpha", 0f, 1f);
			mAnimatorList.add(animRocketAlpha);
			
			//button's animation
			Button btnToNextPage = (Button)findViewById(ResIdMan.BUTTON_TO_DIALING_PAD);
			ObjectAnimator animButtonToPrivacySpaceAlpha = ObjectAnimator.ofFloat(btnToNextPage, "alpha", 0f, 1f);
			mAnimatorList.add(animButtonToPrivacySpaceAlpha);
			
			if(btnToNextPage == null) {
				throw new RuntimeException("btnToNextPage is null");
			}

			
			//uniform duration and interpolator
			for(Animator anim: mAnimatorList) {
				anim.setInterpolator(new DecelerateInterpolator());
				anim.setDuration(animDuration);
			}
			
			//dial numbers
			TextView[] tvHandsetDialNumbers = findViewsHandsetDialNumber();
			final long firstOffset = animDuration;
			final long duration = dialNumAnimDuration;
			long thisOffset = firstOffset;
			for(int ii = 0; ii < tvHandsetDialNumbers.length; ii ++) {
				ObjectAnimator animDialNumAlpha = ObjectAnimator.ofFloat(tvHandsetDialNumbers[ii], "Alpha", 0f, 1f);
				tvHandsetDialNumbers[ii].setAlpha(0f);
				animDialNumAlpha.setStartDelay(thisOffset);
				animDialNumAlpha.setDuration(duration);
				animDialNumAlpha.setInterpolator(new DecelerateInterpolator());
				mAnimatorList.add(animDialNumAlpha);
				thisOffset += duration;
			}
			
			setupPlayingNumListener();
			return;
		}
		



	}
	
	class CinemaHowProtectRightTextIn extends CinemaHowProtectText {
		public CinemaHowProtectRightTextIn() {
			super();
			mTextStartPosition = TEXT_START_POSITION_RIGHT;
			mTextEndPosition = TEXT_START_POSITION_CENTER;
		}
	}
	private class CinemaHowProtectLeftTextIn extends CinemaHowProtectText {
		public CinemaHowProtectLeftTextIn() {
			super();
			mTextStartPosition = TEXT_START_POSITION_LEFT;
			mTextEndPosition = TEXT_START_POSITION_CENTER;
		}
	}
	private class CinemaHowProtectTextToRight extends CinemaHowProtectText {
		public CinemaHowProtectTextToRight() {
			super();
			mTextStartPosition = TEXT_START_POSITION_CENTER;
			mTextEndPosition = TEXT_START_POSITION_RIGHT;
		}
	}
	private class CinemaHowProtectTextToLeft extends CinemaHowProtectText {
		public CinemaHowProtectTextToLeft() {
			super();
			mTextStartPosition = TEXT_START_POSITION_CENTER;
			mTextEndPosition = TEXT_START_POSITION_LEFT;
		}
	}
	
	class CinemaHowProtectText extends Cinema {
		
		public CinemaHowProtectText() {
			super();
			mCinemaMan = CinemaMan.this;
		}

		@Override
		protected void createAnim() {
			
			final boolean isIn = textIsIn();
			final float alphaStart = isIn?0f:1f;
			final float alphaEnd = isIn?1f:0f;
			
			//tvHowProtect.getLeft() + tvHowProtect.getWidth() * 0.3f * mTextEndPosition,
			
			//text how protect
			TextView tvHowProtect = (TextView)findViewById(ResIdMan.TEXTVIEW_HOW_PROTECT);
			ObjectAnimator animTextViewHowProtectOffsetX = ObjectAnimator.ofFloat(tvHowProtect, "x", 
																		tvHowProtect.getLeft() + tvHowProtect.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextStartPosition,
																		tvHowProtect.getLeft() + tvHowProtect.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextEndPosition);
			mAnimatorList.add(animTextViewHowProtectOffsetX);
			
			ObjectAnimator animTextViewHowProtectAlpha = ObjectAnimator.ofFloat(tvHowProtect, "alpha", alphaStart, alphaEnd);
			mAnimatorList.add(animTextViewHowProtectAlpha);
			
			//text enter method
			TextView tvProtectMethod = (TextView)findViewById(ResIdMan.TEXTVIEW_PROTECT_METHOD);
			ObjectAnimator animTextViewEnterMethodOffsetX = ObjectAnimator.ofFloat(tvProtectMethod, "x",
					tvProtectMethod.getLeft() + tvProtectMethod.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextStartPosition,
					tvProtectMethod.getLeft() + tvProtectMethod.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextEndPosition);
			mAnimatorList.add(animTextViewEnterMethodOffsetX);

			ObjectAnimator animTextViewProtectMethodAlpha = ObjectAnimator.ofFloat(tvProtectMethod, "alpha", alphaStart, alphaEnd);
			mAnimatorList.add(animTextViewProtectMethodAlpha);

			//uniform duration and interpolator
			for(Animator anim: mAnimatorList) {
				anim.setInterpolator(new DecelerateInterpolator());
				anim.setDuration(DURATION_BASE);
			}
			
			
			//adjust again
			animTextViewHowProtectAlpha.setInterpolator(ALPHA_INTERPOLATOR);

			animTextViewProtectMethodAlpha.setInterpolator(ALPHA_INTERPOLATOR);
			animTextViewEnterMethodOffsetX.setStartDelay(TEXT_ANIM_DELAY);
			animTextViewEnterMethodOffsetX.setDuration(DURATION_BASE);
			
		}
	}
	
	
	class CinemaHowProtect extends Cinema {
		
		private long animOffsetX = X_OFFSET_BASE * (-1);
		private long animDuration = DURATION_BASE;
		
		public CinemaHowProtect() {
			mCinemaType = CINEMA_TYPE_MAIN;
			mCinemaMan = CinemaMan.this;
		}

		/**
		 * 
		 * Vulcan created this method in 2014年10月17日 下午3:16:25 .
		 */
		protected void createAnim() {
			
			//stars' animation 
			ImageView ivStars = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HOW_PROTECT_STARS);
			ObjectAnimator animStarsAlpha = ObjectAnimator.ofFloat(ivStars, "alpha", 0f, 1f);
			mAnimatorList.add(animStarsAlpha);
			
			//planet1's animation,delay = 0ms
			ImageView ivPlanet1 = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HOW_PROTECT_PLANET1);
			ObjectAnimator animPlanet1OffsetX = ObjectAnimator.ofFloat(ivPlanet1, "x", ivPlanet1.getLeft() - 50 , ivPlanet1.getLeft());
			animPlanet1OffsetX.setStartDelay(0);
			mAnimatorList.add(animPlanet1OffsetX);
			
			ObjectAnimator animPlanet1Alpha = ObjectAnimator.ofFloat(ivPlanet1, "alpha", 0f, 1f);
			animPlanet1Alpha.setStartDelay(0);
			mAnimatorList.add(animPlanet1Alpha);
			
			//planet2's animation,delay = 0ms
			ImageView ivPlanet2 = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HOW_PROTECT_PLANET2);
			ObjectAnimator animPlanet2OffsetX = ObjectAnimator.ofFloat(ivPlanet2, "x", ivPlanet2.getLeft() - 50, ivPlanet2.getLeft());
			animPlanet2OffsetX.setStartDelay(0);
			mAnimatorList.add(animPlanet2OffsetX);
			
			ObjectAnimator animPlanet2Alpha = ObjectAnimator.ofFloat(ivPlanet2, "alpha", 0f, 1f);
			animPlanet2Alpha.setStartDelay(0);
			mAnimatorList.add(animPlanet2Alpha);
			
			//sms's animation,delay=80ms
			ImageView ivSMS = (ImageView)findViewById(ResIdMan.IMAGEVIEW_SMS);
			ObjectAnimator animSMSOffsetX = ObjectAnimator.ofFloat(ivSMS, "x", ivSMS.getLeft() + animOffsetX, ivSMS.getLeft());
			animSMSOffsetX.setStartDelay(80);
			mAnimatorList.add(animSMSOffsetX);

			ObjectAnimator animSMSAlpha = ObjectAnimator.ofFloat(ivSMS, "alpha", 0f, 1f);
			animSMSAlpha.setStartDelay(80);
			mAnimatorList.add(animSMSAlpha);
			
			//video's animation,delay=80ms
			ImageView ivVideo = (ImageView)findViewById(ResIdMan.IMAGEVIEW_VIDEO);
			ObjectAnimator animVideoOffsetX = ObjectAnimator.ofFloat(ivVideo, "x", ivVideo.getLeft() + animOffsetX, ivVideo.getLeft());
			animVideoOffsetX.setStartDelay(80);
			mAnimatorList.add(animVideoOffsetX);

			ObjectAnimator animVideoAlpha = ObjectAnimator.ofFloat(ivVideo, "alpha", 0f, 1f);
			animVideoAlpha.setStartDelay(80);
			mAnimatorList.add(animVideoAlpha);
			
			//app's animation,delay=80ms
			ImageView ivApp = (ImageView)findViewById(ResIdMan.IMAGEVIEW_APP);
			ObjectAnimator animAppOffsetX = ObjectAnimator.ofFloat(ivApp, "x", ivApp.getLeft() + animOffsetX, ivApp.getLeft());
			animAppOffsetX.setStartDelay(80);
			mAnimatorList.add(animAppOffsetX);

			ObjectAnimator animAppAlpha = ObjectAnimator.ofFloat(ivApp, "alpha", 0f, 1f);
			animAppAlpha.setStartDelay(80);
			mAnimatorList.add(animAppAlpha);
			
			//Address book's animation,delay=160ms
			ImageView ivAddressBook = (ImageView)findViewById(ResIdMan.IMAGEVIEW_ADDRESS_BOOK);
			ObjectAnimator animAddressBookOffsetX = ObjectAnimator.ofFloat(ivAddressBook, "x", ivAddressBook.getLeft() + animOffsetX, ivAddressBook.getLeft());
			animAddressBookOffsetX.setStartDelay(160);
			mAnimatorList.add(animAddressBookOffsetX);

			ObjectAnimator animAddressBookAlpha = ObjectAnimator.ofFloat(ivAddressBook, "alpha", 0f, 1f);
			animAddressBookAlpha.setStartDelay(160);
			mAnimatorList.add(animAddressBookAlpha);
			
			//album's animation,delay = 0ms
			ImageView ivAlbum = (ImageView)findViewById(ResIdMan.IMAGEVIEW_ALBUM);
			ObjectAnimator animAlbumOffsetX = ObjectAnimator.ofFloat(ivAlbum, "x", ivAlbum.getLeft() + animOffsetX, ivAlbum.getLeft());
			animAlbumOffsetX.setStartDelay(0);
			mAnimatorList.add(animAlbumOffsetX);

			ObjectAnimator animAlbumAlpha = ObjectAnimator.ofFloat(ivAlbum, "alpha", 0f, 1f);
			animAlbumAlpha.setStartDelay(0);
			mAnimatorList.add(animAlbumAlpha);
			
			//call log's animation,delay = 160ms
			ImageView ivCallLog = (ImageView)findViewById(ResIdMan.IMAGEVIEW_CALL_LOG);
			ObjectAnimator animCallLogOffsetX = ObjectAnimator.ofFloat(ivCallLog, "x", ivCallLog.getLeft() + animOffsetX, ivCallLog.getLeft());
			animCallLogOffsetX.setStartDelay(160);
			mAnimatorList.add(animCallLogOffsetX);

			ObjectAnimator animCallLogAlpha = ObjectAnimator.ofFloat(ivCallLog, "alpha", 0f, 1f);
			animCallLogAlpha.setStartDelay(160);
			mAnimatorList.add(animCallLogAlpha);
			
			//button's animation
			final Button btnToNextPage = (Button)findViewById(ResIdMan.BUTTON_NEXT_STEP);
			ObjectAnimator animButtonToPrivacySpaceAlpha = ObjectAnimator.ofFloat(btnToNextPage, "alpha", 0f, 1f);
			animButtonToPrivacySpaceAlpha.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animator) {
					btnToNextPage.setClickable(false);
				}

				@Override
				public void onAnimationEnd(Animator animator) {
					btnToNextPage.setClickable(true);
				}

				@Override
				public void onAnimationCancel(Animator animator) {

				}

				@Override
				public void onAnimationRepeat(Animator animator) {

				}
			});
			mAnimatorList.add(animButtonToPrivacySpaceAlpha);
			
			if(btnToNextPage == null) {
				throw new RuntimeException("btnToNextPage is null");
			}
			
			//uniform duration and interpolator
			for(Animator anim: mAnimatorList) {
				anim.setInterpolator(new DecelerateInterpolator());
				anim.setDuration(animDuration);
				//anim.setStartDelay(80);
			}

			return;
		}
		
	}
	
	private class CinemaHowExitTextToLeft extends CinemaHowExitText {
		public CinemaHowExitTextToLeft() {
			super();
			mTextStartPosition = TEXT_START_POSITION_CENTER;
			mTextEndPosition = TEXT_START_POSITION_LEFT;
		}
	}
	private class CinemaHowExitRightTextIn extends CinemaHowExitText {
		public CinemaHowExitRightTextIn() {
			super();
			mTextStartPosition = TEXT_START_POSITION_RIGHT;
			mTextEndPosition = TEXT_START_POSITION_CENTER;
		}
	}
	private class CinemaHowExitLeftTextIn extends CinemaHowExitText {
		public CinemaHowExitLeftTextIn() {
			super();
			mTextStartPosition = TEXT_START_POSITION_LEFT;
			mTextEndPosition = TEXT_START_POSITION_CENTER;
		}
	}
	private class CinemaHowExitTextToRight extends CinemaHowExitText {
		public CinemaHowExitTextToRight() {
			super();
			mTextStartPosition = TEXT_START_POSITION_CENTER;
			mTextEndPosition = TEXT_START_POSITION_RIGHT;
		}
	}

	private class CinemaHowExitText extends Cinema {
		
		private CinemaHowExitText() {
			super();
			mCinemaMan = CinemaMan.this;
		}
		
		@Override
		protected void createAnim() {
			final boolean isIn = textIsIn();
			final float alphaStart = isIn?0f:1f;
			final float alphaEnd = isIn?1f:0f;

			//tvHowExit.getLeft() + tvHowExit.getWidth() * 0.3f * mTextEndPosition,
			
			//text how exit
			TextView tvHowExit = (TextView)findViewById(ResIdMan.TEXTVIEW_HOW_EXIT);
			ObjectAnimator animTextViewHowExitOffsetX = ObjectAnimator.ofFloat(tvHowExit, "x", 
																		tvHowExit.getLeft() + tvHowExit.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextStartPosition,
																		tvHowExit.getLeft() + tvHowExit.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextEndPosition);
			mAnimatorList.add(animTextViewHowExitOffsetX);
			
			ObjectAnimator animTextViewHowExitAlpha = ObjectAnimator.ofFloat(tvHowExit, "alpha", alphaStart, alphaEnd);
			mAnimatorList.add(animTextViewHowExitAlpha);

			//text exit method
			TextView tvExitMethod = (TextView)findViewById(ResIdMan.TEXTVIEW_EXIT_METHOD);
			ObjectAnimator animTextViewExitMethodOffsetX = ObjectAnimator.ofFloat(tvExitMethod, "x", 
																		tvExitMethod.getLeft() + tvExitMethod.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextStartPosition,
																		tvExitMethod.getLeft() + tvExitMethod.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextEndPosition);
			mAnimatorList.add(animTextViewExitMethodOffsetX);

			ObjectAnimator animTextViewExitMethodAlpha = ObjectAnimator.ofFloat(tvExitMethod, "alpha", alphaStart, alphaEnd);
			mAnimatorList.add(animTextViewExitMethodAlpha);
			
			//uniform duration and interpolator
			for(Animator anim: mAnimatorList) {
				anim.setInterpolator(new DecelerateInterpolator());
				anim.setDuration(DURATION_BASE);
			}
			
			
			//adjust again
			animTextViewHowExitAlpha.setInterpolator(ALPHA_INTERPOLATOR);
			
			animTextViewExitMethodAlpha.setInterpolator(ALPHA_INTERPOLATOR);
			animTextViewExitMethodOffsetX.setStartDelay(TEXT_ANIM_DELAY);
			animTextViewExitMethodOffsetX.setDuration(DURATION_BASE);
			
		}
		
	}
	

	private class CinemaHowExit extends Cinema {
		
		final long animOffsetX = -100;
		final long animOffsetY = -100;
		final long animDuration = DURATION_BASE;
		
		public CinemaHowExit() {
			mCinemaType = CINEMA_TYPE_MAIN;
			mCinemaMan = CinemaMan.this;
		}

		/**
		 * 
		 * Vulcan created this method in 2014年10月17日 下午3:16:25 .
		 */
		protected void createAnim() {
			
			//u3's animation
			ImageView ivU3 = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HOW_EXIT_U3);
			ObjectAnimator animU3OffsetX = ObjectAnimator.ofFloat(ivU3, "x", animOffsetX + ivU3.getLeft() , ivU3.getLeft());
			mAnimatorList.add(animU3OffsetX);
			
			ObjectAnimator animU3OffsetY = ObjectAnimator.ofFloat(ivU3, "y", animOffsetY + ivU3.getTop() , ivU3.getTop());
			mAnimatorList.add(animU3OffsetY);
			
			ObjectAnimator animU3Alpha = ObjectAnimator.ofFloat(ivU3, "alpha", 0f, 1f);
			mAnimatorList.add(animU3Alpha);
			
			//handset's animation
			ImageView ivHandset = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HOW_EXIT_HANDSET);
			ObjectAnimator animHandsetOffsetX = ObjectAnimator.ofFloat(ivHandset, "x", ivHandset.getLeft() - animOffsetX , ivHandset.getLeft());
			mAnimatorList.add(animHandsetOffsetX);
			
			ObjectAnimator animHandsetOffsetY = ObjectAnimator.ofFloat(ivHandset, "y",  ivHandset.getTop() - animOffsetY , ivHandset.getTop());
			mAnimatorList.add(animHandsetOffsetY);
			
			ObjectAnimator animHandsetAlpha = ObjectAnimator.ofFloat(ivHandset, "alpha", 0f, 1f);
			mAnimatorList.add(animHandsetAlpha);
			
			//falling stars' animation
			ImageView ivFallingStars = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HOW_EXIT_FALLING_STARS);
			ObjectAnimator animFallingStarsAlpha = ObjectAnimator.ofFloat(ivFallingStars, "alpha", 0f, 1f);
			mAnimatorList.add(animFallingStarsAlpha);
			
			//stars' animation
			ImageView ivStars = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HOW_EXIT_STARS);
			ObjectAnimator animStarsAlpha = ObjectAnimator.ofFloat(ivStars, "alpha", 0f, 1f);
			mAnimatorList.add(animStarsAlpha);
			
			//button's animation
			Button btnToNextPage = (Button)findViewById(ResIdMan.BUTTON_I_SEE);
			ObjectAnimator animButtonToPrivacySpaceAlpha = ObjectAnimator.ofFloat(btnToNextPage, "alpha", 0f, 1f);
			mAnimatorList.add(animButtonToPrivacySpaceAlpha);
			
			if(btnToNextPage == null) {
				throw new RuntimeException("btnToNextPage is null");
			}
			
			//uniform duration and interpolator
			for(Animator anim: mAnimatorList) {
				anim.setInterpolator(new DecelerateInterpolator());
				anim.setDuration(animDuration);
			}
			
			setupPlayingNumListener();
			return;
		}
		
	}
	

	private class CinemaHowDeleteTextToLeft extends CinemaHowDeleteText {
		public CinemaHowDeleteTextToLeft() {
			super();
			mTextStartPosition = TEXT_START_POSITION_CENTER;
			mTextEndPosition = TEXT_START_POSITION_LEFT;
		}
	}
	private class CinemaHowDeleteRightTextIn extends CinemaHowDeleteText {
		public CinemaHowDeleteRightTextIn() {
			super();
			mTextStartPosition = TEXT_START_POSITION_RIGHT;
			mTextEndPosition = TEXT_START_POSITION_CENTER;
		}
	}
	private class CinemaHowDeleteLeftTextIn extends CinemaHowDeleteText {
		public CinemaHowDeleteLeftTextIn() {
			super();
			mTextStartPosition = TEXT_START_POSITION_LEFT;
			mTextEndPosition = TEXT_START_POSITION_CENTER;
		}
	}
	private class CinemaHowDeleteTextToRight extends CinemaHowDeleteText {
		public CinemaHowDeleteTextToRight() {
			super();
			mTextStartPosition = TEXT_START_POSITION_CENTER;
			mTextEndPosition = TEXT_START_POSITION_RIGHT;
		}
	}
	
	private class CinemaHowDeleteText extends Cinema {
		private CinemaHowDeleteText() {
			super();
			mCinemaMan = CinemaMan.this;
		}
		@Override
		protected void createAnim() {
			
			final boolean isIn = textIsIn();
			final float alphaStart = isIn?0f:1f;
			final float alphaEnd = isIn?1f:0f;
			
			//tvDeleteMethod.getLeft() + tvDeleteMethod.getWidth() * 0.3f * mTextEndPosition,
			
			//text how delete
			TextView tvHowDelete = (TextView)findViewById(ResIdMan.TEXTVIEW_HOW_DELETE);
			ObjectAnimator animTextViewHowDeleteOffsetX = ObjectAnimator.ofFloat(tvHowDelete, "x", 
					tvHowDelete.getLeft() + tvHowDelete.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextStartPosition,
					tvHowDelete.getLeft() + tvHowDelete.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextEndPosition);
			mAnimatorList.add(animTextViewHowDeleteOffsetX);
			
			ObjectAnimator animTextViewHowDeleteAlpha = ObjectAnimator.ofFloat(tvHowDelete, "alpha", alphaStart, alphaEnd);
			mAnimatorList.add(animTextViewHowDeleteAlpha);

			//text delete method
			TextView tvDeleteMethod = (TextView)findViewById(ResIdMan.TEXTVIEW_DELETE_METHOD);
			ObjectAnimator animTextViewDeleteMethodOffsetX = ObjectAnimator.ofFloat(tvDeleteMethod, "x", 
					tvDeleteMethod.getLeft() + tvDeleteMethod.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextStartPosition,
					tvDeleteMethod.getLeft() + tvDeleteMethod.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextEndPosition);
			mAnimatorList.add(animTextViewDeleteMethodOffsetX);

			ObjectAnimator animTextViewDeleteMethodAlpha = ObjectAnimator.ofFloat(tvDeleteMethod, "alpha", alphaStart, alphaEnd);
			mAnimatorList.add(animTextViewDeleteMethodAlpha);
			
			//uniform duration and interpolator
			for(Animator anim: mAnimatorList) {
				anim.setInterpolator(new DecelerateInterpolator());
				anim.setDuration(DURATION_BASE);
			}
			
			
			//adjust again
			animTextViewHowDeleteAlpha.setInterpolator(ALPHA_INTERPOLATOR);
			
			animTextViewDeleteMethodAlpha.setInterpolator(ALPHA_INTERPOLATOR);
			animTextViewDeleteMethodOffsetX.setStartDelay(TEXT_ANIM_DELAY);
			animTextViewDeleteMethodOffsetX.setDuration(DURATION_BASE);
			
		}
		
	}

	private class CinemaHowDelete extends Cinema {
		final long animDuration = DURATION_BASE;
		final long animOffsetX = -100;
		final long animOffsetY = 100;
		
		public CinemaHowDelete() {
			mCinemaType = CINEMA_TYPE_MAIN;
			mCinemaMan = CinemaMan.this;
		}
		
		protected void createAnim() {
			
			//privacy home's animation
			ImageView ivPrivacyHome = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HOW_DELETE_PRIVACY_HOME);
			ObjectAnimator animPrivacyHomeOffsetX = ObjectAnimator.ofFloat(ivPrivacyHome, "x", animOffsetX + ivPrivacyHome.getLeft() , ivPrivacyHome.getLeft());
			mAnimatorList.add(animPrivacyHomeOffsetX);

			ObjectAnimator animPrivacyHomeAlpha = ObjectAnimator.ofFloat(ivPrivacyHome, "alpha", 0f, 1f);
			mAnimatorList.add(animPrivacyHomeAlpha);
			
			//privacy arrow's animation
			//ImageView ivArrow = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HOW_DELETE_ARROW);
			//ObjectAnimator animArrowOffsetX = ObjectAnimator.ofFloat(ivArrow, "x", animOffsetX * 0.3f + ivArrow.getLeft() , ivArrow.getLeft());
			//mAnimatorList.add(animArrowOffsetX);

			//ObjectAnimator animArrowAlpha = ObjectAnimator.ofFloat(ivArrow, "alpha", 0f, 1f);
			//mAnimatorList.add(animArrowAlpha);

			//privacy setting's animation
			ImageView ivPrivacySetting = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HOW_DELETE_PRIVACY_SETTING);
			ObjectAnimator animPrivacySettingOffsetX = ObjectAnimator.ofFloat(ivPrivacySetting, "x", ivPrivacySetting.getLeft() + animOffsetX * 0.6f , ivPrivacySetting.getLeft());
			mAnimatorList.add(animPrivacySettingOffsetX);

			ObjectAnimator animPrivacySettingAlpha = ObjectAnimator.ofFloat(ivPrivacySetting, "alpha", 0f, 1f);
			mAnimatorList.add(animPrivacySettingAlpha);
			
			//finger's animation
			ImageView ivFinger = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HOW_DELETE_FINGER);
			ObjectAnimator animFingerOffsetX = ObjectAnimator.ofFloat(ivFinger, "y", ivFinger.getTop() + animOffsetY, ivFinger.getTop());
			mAnimatorList.add(animFingerOffsetX);
			
			ObjectAnimator animFingerAlpha = ObjectAnimator.ofFloat(ivFinger, "alpha", 0f, 1f);
			mAnimatorList.add(animFingerAlpha);
			
			//click shadow's animation
			ImageView ivClickShadow = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HOW_DELETE_CLICK_SHADOW);
			ObjectAnimator animClickShadowAlpha = ObjectAnimator.ofFloat(ivClickShadow, "alpha", 0f, 1f);
			mAnimatorList.add(animClickShadowAlpha);
			
			//uniform duration and interpolator
			for(Animator anim: mAnimatorList) {
				anim.setInterpolator(new DecelerateInterpolator());
				anim.setDuration(animDuration);
			}
			
			//adjust finger
			animFingerOffsetX.setStartDelay(animDuration * 5/8);
			animFingerOffsetX.setDuration(animDuration * 2/8);
			animFingerAlpha.setStartDelay(animDuration * 5/8);
			animFingerAlpha.setDuration(animDuration * 2/8);
			
			//adjust click shadow
			animClickShadowAlpha.setStartDelay(animDuration * 7/8);
			animClickShadowAlpha.setDuration(animDuration * 1/20);
			setupPlayingNumListener();
			return;
		}
		
	}
	
	private class CinemaHowGetbackTextToLeft extends CinemaHowGetbackText {
		public CinemaHowGetbackTextToLeft() {
			super();
			mTextStartPosition = TEXT_START_POSITION_CENTER;
			mTextEndPosition = TEXT_START_POSITION_LEFT;
		}
	}
	private class CinemaHowGetbackRightTextIn extends CinemaHowGetbackText {
		public CinemaHowGetbackRightTextIn() {
			super();
			mTextStartPosition = TEXT_START_POSITION_RIGHT;
			mTextEndPosition = TEXT_START_POSITION_CENTER;
		}
	}
	private class  CinemaHowGetbackLeftTextIn extends CinemaHowGetbackText {
		public  CinemaHowGetbackLeftTextIn() {
			super();
			mTextStartPosition = TEXT_START_POSITION_LEFT;
			mTextEndPosition = TEXT_START_POSITION_CENTER;
		}
	}
	private class  CinemaHowGetbackTextToRight extends CinemaHowGetbackText {
		public CinemaHowGetbackTextToRight() {
			super();
			mTextStartPosition = TEXT_START_POSITION_CENTER;
			mTextEndPosition = TEXT_START_POSITION_RIGHT;
		}
	}
	
	private class CinemaHowGetbackText extends Cinema {
		private CinemaHowGetbackText() {
			super();
			mCinemaMan = CinemaMan.this;
		}
		@Override
		protected void createAnim() {
			
			final boolean isIn = textIsIn();
			final float alphaStart = isIn?0f:1f;
			final float alphaEnd = isIn?1f:0f;
			
			//tvGetbackMethod.getLeft() + tvGetbackMethod.getWidth() * 0.3f * mTextEndPosition,
			
			//text how get back password
			TextView tvHowGetback = (TextView)findViewById(ResIdMan.TEXTVIEW_HOW_GETBACK_PASSWORD);
			ObjectAnimator animTextViewHowGetbackOffsetX = ObjectAnimator.ofFloat(tvHowGetback, "x", 
					tvHowGetback.getLeft() + tvHowGetback.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextStartPosition,
					tvHowGetback.getLeft() + tvHowGetback.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextEndPosition);
			mAnimatorList.add(animTextViewHowGetbackOffsetX);
			
			ObjectAnimator animTextViewHowGetbackAlpha = ObjectAnimator.ofFloat(tvHowGetback, "alpha", alphaStart, alphaEnd);
			mAnimatorList.add(animTextViewHowGetbackAlpha);

			//text getback password method
			TextView tvGetbackMethod = (TextView)findViewById(ResIdMan.TEXTVIEW_GETBACK_PASSWORD_METHOD);
			ObjectAnimator animTextViewGetbackMethodOffsetX = ObjectAnimator.ofFloat(tvGetbackMethod, "x", 
					tvGetbackMethod.getLeft() + tvGetbackMethod.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextStartPosition,
					tvGetbackMethod.getLeft() + tvGetbackMethod.getWidth() * TEXT_ANIM_OFFSET_RATE * mTextEndPosition);
			mAnimatorList.add(animTextViewGetbackMethodOffsetX);

			ObjectAnimator animTextViewGetbackMethodAlpha = ObjectAnimator.ofFloat(tvGetbackMethod, "alpha", alphaStart, alphaEnd);
			mAnimatorList.add(animTextViewGetbackMethodAlpha);
			
			//uniform duration and interpolator
			for(Animator anim: mAnimatorList) {
				anim.setInterpolator(new DecelerateInterpolator());
				anim.setDuration(DURATION_BASE);
			}
			
			
			//adjust again
			animTextViewHowGetbackAlpha.setInterpolator(ALPHA_INTERPOLATOR);
			
			animTextViewGetbackMethodAlpha.setInterpolator(ALPHA_INTERPOLATOR);
			animTextViewGetbackMethodOffsetX.setStartDelay(TEXT_ANIM_DELAY);
			animTextViewGetbackMethodOffsetX.setDuration(DURATION_BASE);
			
		}
		
	}


	private class CinemaHowGetback extends Cinema {
		
		public CinemaHowGetback() {
			mCinemaType = CINEMA_TYPE_MAIN;
			mCinemaMan = CinemaMan.this;
		}

		protected void createAnim() {
			
			//u3 desktop''s animation
			ImageView ivDesktop = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HOW_GETBACK_U3_DESKTOP);
			ObjectAnimator animDesktopOffsetX = ObjectAnimator.ofFloat(ivDesktop, "x", ivDesktop.getLeft() - X_OFFSET_BASE , ivDesktop.getLeft());
			mAnimatorList.add(animDesktopOffsetX);

			ObjectAnimator animDesktopAlpha = ObjectAnimator.ofFloat(ivDesktop, "alpha", 0f, 1f);
			mAnimatorList.add(animDesktopAlpha);

			//create home''s animation
			ImageView ivCreateHome = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HOW_GETBACK_CREATE_HOME);
			ObjectAnimator animCreateHomeOffsetX = ObjectAnimator.ofFloat(ivCreateHome, "x", ivCreateHome.getLeft() - X_OFFSET_BASE * 0.6f , ivCreateHome.getLeft());
			mAnimatorList.add(animCreateHomeOffsetX);

			ObjectAnimator animCreateHomeAlpha = ObjectAnimator.ofFloat(ivCreateHome, "alpha", 0f, 1f);
			mAnimatorList.add(animCreateHomeAlpha);
			
			//finger's animation
			ImageView ivFinger = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HOW_GETBACK_FINGER);
			ObjectAnimator animFingerOffsetY = ObjectAnimator.ofFloat(ivFinger, "y", ivFinger.getTop() + Y_OFFSET_BASE, ivFinger.getTop());
			mAnimatorList.add(animFingerOffsetY);
			
			ObjectAnimator animFingerAlpha = ObjectAnimator.ofFloat(ivFinger, "alpha", 0f, 1f);
			mAnimatorList.add(animFingerAlpha);
			
			//click shadow's animation
			ImageView ivClickShadow = (ImageView)findViewById(ResIdMan.IMAGEVIEW_HOW_GETBACK_CLICK_SHADOW);
			ObjectAnimator animClickShadowAlpha = ObjectAnimator.ofFloat(ivClickShadow, "alpha", 0f, 1f);
			mAnimatorList.add(animClickShadowAlpha);
			
			//uniform duration and interpolator
			for(Animator anim: mAnimatorList) {
				anim.setInterpolator(new DecelerateInterpolator());
				anim.setDuration(DURATION_BASE);
			}
			
			//adjust finger
			animFingerOffsetY.setStartDelay(DURATION_BASE * 5/8);
			animFingerOffsetY.setDuration(DURATION_BASE * 2/8);
			animFingerAlpha.setStartDelay(DURATION_BASE * 5/8);
			animFingerAlpha.setDuration(DURATION_BASE * 2/8);
			
			//adjust click shadow
			animClickShadowAlpha.setStartDelay(DURATION_BASE * 7/8);
			animClickShadowAlpha.setDuration(DURATION_BASE * 1/20);
			setupPlayingNumListener();
			return;
		}
		
	}
	
	private class CinemaNoUse extends Cinema {

		@Override
		protected void createAnim() {
		}
		
		@Override
		public boolean isPlaying() {
			return false;
		}
		
	}
	
	//page 1: how enter
	protected CinemaHowEnter mCinemaHowEnter = new CinemaHowEnter();
	protected CinemaHowEnterText mCinemaHowEnterRightTextIn = new CinemaHowEnterRightTextIn();
	protected CinemaHowEnterText mCinemaHowEnterLeftTextIn = new CinemaHowEnterLeftTextIn();
	protected CinemaHowEnterText mCinemaHowEnterTextToLeft = new CinemaHowEnterTextToLeft();
	protected CinemaHowEnterText mCinemaHowEnterTextToRight = new CinemaHowEnterTextToRight();
	
	//page 2: how protect
	protected CinemaHowProtect mCinemaHowProtect = new CinemaHowProtect();
	protected CinemaHowProtectText mCinemaHowProtectRightTextIn = new CinemaHowProtectRightTextIn();
	protected CinemaHowProtectText mCinemaHowProtectTextToLeft = new CinemaHowProtectTextToLeft();
	protected CinemaHowProtectText mCinemaHowProtectLeftTextIn = new CinemaHowProtectLeftTextIn();
	protected CinemaHowProtectText mCinemaHowProtectTextToRight = new CinemaHowProtectTextToRight();

	//page 3: how exit
	protected CinemaHowExit mCinemaHowExit = new CinemaHowExit();
	protected CinemaHowExitText mCinemaHowExitRightTextIn = new CinemaHowExitRightTextIn();
	protected CinemaHowExitText mCinemaHowExitTextToLeft = new CinemaHowExitTextToLeft();
	protected CinemaHowExitText mCinemaHowExitLeftTextIn = new CinemaHowExitLeftTextIn();
	protected CinemaHowExitText mCinemaHowExitTextToRight = new CinemaHowExitTextToRight();

	//page 4: how delete
	protected CinemaHowDelete mCinemaHowDelete = new CinemaHowDelete();
	protected CinemaHowDeleteText mCinemaHowDeleteRightTextIn = new CinemaHowDeleteRightTextIn();
	protected CinemaHowDeleteText mCinemaHowDeleteTextToLeft = new CinemaHowDeleteTextToLeft();
	protected CinemaHowDeleteText mCinemaHowDeleteLeftTextIn = new CinemaHowDeleteLeftTextIn();
	protected CinemaHowDeleteText mCinemaHowDeleteTextToRight = new CinemaHowDeleteTextToRight();
	
	//page 5: how get back password
	protected CinemaHowGetback mCinemaHowGetback= new CinemaHowGetback();
	protected CinemaHowGetbackText mCinemaHowGetbackRightTextIn = new CinemaHowGetbackRightTextIn();
	protected CinemaHowGetbackText mCinemaHowGetbackTextToLeft = new CinemaHowGetbackTextToLeft();
	protected CinemaHowGetbackText mCinemaHowGetbackLeftTextIn = new CinemaHowGetbackLeftTextIn();
	protected CinemaHowGetbackText mCinemaHowGetbackTextToRight = new CinemaHowGetbackTextToRight();
	
	//empty cinema:
	final protected CinemaNoUse mCinemaNoUse = new CinemaNoUse();
	
	//organise the cinemas by page, ordered by type
	//cinema position in one page
	//1.out to left
	//2.right in
	//3.out to right
	//4.left in
	final protected Cinema[] mTextCinemaSetInPageHowEnter = new Cinema[] {
			mCinemaHowEnterTextToLeft,
			mCinemaHowEnterRightTextIn,
			mCinemaHowEnterTextToRight,
			mCinemaHowEnterLeftTextIn
	};
	final protected Cinema[] mTextCinemaSetInPageHowProtect = new Cinema[] {
			mCinemaHowProtectTextToLeft,
			mCinemaHowProtectRightTextIn,
			mCinemaHowProtectTextToRight,
			mCinemaHowProtectLeftTextIn
	};
	final protected Cinema[] mTextCinemaSetInPageHowExit = new Cinema[] {
			mCinemaHowExitTextToLeft,
			mCinemaHowExitRightTextIn,
			mCinemaHowExitTextToRight,
			mCinemaHowExitLeftTextIn
	};
	final protected Cinema[] mTextCinemaSetInPageHowDelete = new Cinema[] {
			mCinemaHowDeleteTextToLeft,
			mCinemaHowDeleteRightTextIn,
			mCinemaHowDeleteTextToRight,
			mCinemaHowDeleteLeftTextIn
	};
	final protected Cinema[] mTextCinemaSetInPageHowGetback = new Cinema[] {
			mCinemaHowGetbackTextToLeft,
			mCinemaHowGetbackRightTextIn,
			mCinemaHowGetbackTextToRight,
			mCinemaHowGetbackLeftTextIn
	};
	final protected Cinema[] mTextCinemaInPageNoUse = new Cinema[] {
			mCinemaNoUse,
			mCinemaNoUse,
			mCinemaNoUse,
			mCinemaNoUse
	};


	//hold all the animations in the user guide
	//protected Cinema[] mPageMainCinema;
	//protected Cinema[] mPageTextInCinema;
	//protected Cinema[] mPageTextOutCinema;
	final protected ArrayList<Cinema[]> mTextCinemaSetListByPage = new ArrayList<Cinema[]>();
	final protected ArrayList<Cinema> mMainCinemaListByPage = new ArrayList<Cinema>();
	
	final protected Cinema[] mCinemaListForCreateDone = new Cinema[] 
		{mCinemaHowEnter,mCinemaHowEnterRightTextIn,mCinemaHowEnterLeftTextIn,mCinemaHowEnterTextToLeft,mCinemaHowEnterTextToRight,
			mCinemaHowProtect,mCinemaHowProtectRightTextIn,mCinemaHowProtectLeftTextIn,mCinemaHowProtectTextToLeft,mCinemaHowProtectTextToRight,
			mCinemaHowExit,mCinemaHowExitRightTextIn,mCinemaHowExitLeftTextIn,mCinemaHowExitTextToLeft,mCinemaHowExitTextToRight};
	
	final protected Cinema[] mCinemaListForHelp = new Cinema[] 
			{mCinemaHowEnter,mCinemaHowEnterRightTextIn,mCinemaHowEnterLeftTextIn,mCinemaHowEnterTextToLeft,mCinemaHowEnterTextToRight,
				mCinemaHowProtect,mCinemaHowProtectRightTextIn,mCinemaHowProtectLeftTextIn,mCinemaHowProtectTextToLeft,mCinemaHowProtectTextToRight,
				mCinemaHowExit,mCinemaHowExitRightTextIn,mCinemaHowExitLeftTextIn,mCinemaHowExitTextToLeft,mCinemaHowExitTextToRight,
				mCinemaHowDelete,mCinemaHowDeleteRightTextIn,mCinemaHowDeleteLeftTextIn,mCinemaHowDeleteTextToLeft,mCinemaHowDeleteTextToRight,
				mCinemaHowGetback,mCinemaHowGetbackRightTextIn,mCinemaHowGetbackLeftTextIn,mCinemaHowGetbackTextToLeft,mCinemaHowGetbackTextToRight};
	final protected ArrayList<Cinema> mPlayingCinemaList = new ArrayList<Cinema>();
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月30日 上午11:36:04 .
	 */
	public void onCinemaTick() {
		Cinema cinema = null;
		Iterator<Cinema> cenimaIterator = mPlayingCinemaList.iterator();
		while(cenimaIterator.hasNext()) {
			cinema = cenimaIterator.next();
			if(!cinema.isPlaying()) {
				cenimaIterator.remove();
				continue;
			}
			long newPercent = cinema.nextFrame(Cinema.CINEMA_FRAME_PERIOD);
			if(newPercent >= Cinema.PLAY_PROGRESS_MAX
				&& !cinema.isBackward()) {
				cinema.pause();
				cinema.setCurrentPlayProgress(Cinema.PLAY_PROGRESS_MAX);
			}
			else if(newPercent <= Cinema.PLAY_PROGRESS_MIN
					&& cinema.isBackward()) {
				cinema.pause();
				cinema.setCurrentPlayProgress(Cinema.PLAY_PROGRESS_MIN);
			}
		}
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月23日 上午11:38:47 .
	 * @param callerType
	 * @return
	 */
	protected Cinema[] createPageMainCinema(int callerType) {
		if (callerType == UserGuide.LAUNCH_MODE_CREATE_DONE) {
			return new Cinema[] { mCinemaHowEnter, mCinemaHowProtect,
					mCinemaHowExit, null };
		} else if (callerType == UserGuide.LAUNCH_MODE_HELP) {
			return new Cinema[] { mCinemaHowEnter, mCinemaHowProtect,
					mCinemaHowExit, mCinemaHowDelete, mCinemaHowGetback,null };
		}
		return new Cinema[] { mCinemaHowEnter, mCinemaHowProtect,
				mCinemaHowExit, mCinemaHowDelete, mCinemaHowGetback,null };
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月11日 下午5:42:57 .
	 * @return
	 */
	protected ArrayList<Cinema[]> createTextCinemaListByPage(int launchMode) {
		//cinema position in one page
		//1.out to left
		//2.right in
		//3.out to right
		//4.left in
		
		ArrayList<Cinema[]> cinemaSetList = new ArrayList<Cinema[]>();
		if (launchMode == UserGuide.LAUNCH_MODE_CREATE_DONE
				|| launchMode == UserGuide.LAUNCH_MODE_SHOW_PAGE_HOW_EXIT) {
			cinemaSetList.add(mTextCinemaSetInPageHowProtect);
			cinemaSetList.add(mTextCinemaSetInPageHowEnter);
			cinemaSetList.add(mTextCinemaSetInPageHowExit);
		}
		else {
			cinemaSetList.add(mTextCinemaSetInPageHowEnter);
			cinemaSetList.add(mTextCinemaSetInPageHowProtect);
			cinemaSetList.add(mTextCinemaSetInPageHowExit);
			cinemaSetList.add(mTextCinemaSetInPageHowDelete);
			cinemaSetList.add(mTextCinemaSetInPageHowGetback);
		}

		return cinemaSetList;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月11日 下午6:06:02 .
	 * @param callerType
	 * @return
	 */
	protected ArrayList<Cinema> createMainCinemaListByPage(int launchMode) {
		ArrayList<Cinema> cinemaSetList = new ArrayList<Cinema>();
		if (launchMode == UserGuide.LAUNCH_MODE_CREATE_DONE
				||launchMode == UserGuide.LAUNCH_MODE_SHOW_PAGE_HOW_EXIT) {
			cinemaSetList.add(mCinemaHowProtect);
			cinemaSetList.add(mCinemaHowEnter);
			cinemaSetList.add(mCinemaHowExit);
			//cinemaSetList.add(mCinemaNoUse);
		}
		else {
			cinemaSetList.add(mCinemaHowEnter);
			cinemaSetList.add(mCinemaHowProtect);
			cinemaSetList.add(mCinemaHowExit);
			cinemaSetList.add(mCinemaHowDelete);
			cinemaSetList.add(mCinemaHowGetback);
			//cinemaSetList.add(mCinemaNoUse);
		}

		return cinemaSetList;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 上午9:52:12 .
	 * @param resId
	 * @return
	 */
	private View findViewById(int resId) {
		return mHostContext.findViewById(resId);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月23日 下午4:29:54 .
	 * @param callerType
	 */
	private void createAnimsByCallerType(int launchMode) {
		if(launchMode == UserGuide.LAUNCH_MODE_CREATE_DONE
				|| launchMode == UserGuide.LAUNCH_MODE_SHOW_PAGE_HOW_EXIT) {
			createAnimsForCreateDone();
		}
		else if(launchMode == UserGuide.LAUNCH_MODE_HELP) {
			createAnimsForHelp();
		}
		else {
			createAnimsForHelp();
		}
		return;
	}

	/**
	 * 
	 * Vulcan created this method in 2014年10月23日 下午4:29:58 .
	 */
	protected void createAnimsForHelp() {
		for(Cinema c: mCinemaListForHelp) {
			c.createAnim();
			c.hide();
		}
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月23日 下午4:30:01 .
	 */
	protected void createAnimsForCreateDone() {
		
		for(Cinema c: mCinemaListForCreateDone) {
			c.createAnim();
			c.hide();
		}

		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月15日 上午10:49:02 .
	 * @return
	 */
	private TextView[] findViewsHandsetDialNumber() {
		LinearLayout llHandsetNumber = (LinearLayout)findViewById(ResIdMan.LAYOUT_VIEW_HANDSET_PRIVACY_ENTRANCE);
		int count = llHandsetNumber.getChildCount();
		TextView[] viewList = new TextView[count];
		for(int ii = 0;ii < count;ii ++) {
			viewList[ii] = (TextView)llHandsetNumber.getChildAt(ii);
		}
		return viewList;
	}
	
	protected boolean mCinemaIsPlaying = false;
	private boolean mCinemaIsCreated = false;

}
