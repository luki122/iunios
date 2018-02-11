/**
 * Vulcan created this file in 2014年11月12日 上午9:24:06 .
 */
package com.privacymanage.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.privacymanage.utils.LogUtils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

/**
 * Vulcan created Cinema in 2014年11月12日 .
 * 
 */
public abstract class Cinema {
	public static final Interpolator ALPHA_INTERPOLATOR = new LinearInterpolator();
	public static final float TEXT_ANIM_OFFSET_RATE = 0.2f;
	public static final int DURATION_BASE = 600;
	public static final int TEXT_ANIM_DELAY = 20;
	public static final int X_OFFSET_BASE = 100;
	public static final int Y_OFFSET_BASE = 100;
	public static final long PLAY_PROGRESS_MAX = 100;
	public static  final long PLAY_PROGRESS_MIN = 0;
	//perhaps we need adjust where we are from and to.
	public static final int TEXT_START_POSITION_LEFT = -1;
	public static final int TEXT_START_POSITION_CENTER = 0;
	public static final int TEXT_START_POSITION_RIGHT = 1;
	public static final long CINEMA_FRAME_RATE = 50;
	public static final long CINEMA_FRAME_PERIOD = 1000/CINEMA_FRAME_RATE;
	
	public static final int CINEMA_TYPE_MAIN = 0;
	public static final int CINEMA_TYPE_TEXT = 1;
	
	//1.out to left
	//2.right in
	//3.out to right
	//4.left in
	public static final int TEXT_CINEMA_INDEX_CENTER_TO_LEFT = 0;
	public static final int TEXT_CINEMA_INDEX_RIGHT_TO_CENTER = 1;
	public static final int TEXT_CINEMA_INDEX_CENTER_TO_RIGHT = 2;
	public static final int TEXT_CINEMA_INDEX_LEFT_TO_CENTER = 3;
	

	
	/**
	 * 
	 * Vulcan created this method in 2014年11月10日 下午5:32:28 .
	 * @param i
	 */
	protected void updateTextAnimInterpolator() {
		Interpolator finalInterpolator = new LinearInterpolator();
/*			if(mPlayingBackwards) {
			finalInterpolator = new AccelerateInterpolator();
		}
		else {
			finalInterpolator = new DecelerateInterpolator();
		}*/
		for(ObjectAnimator anim: mAnimatorList) {
			if(anim.getTarget() instanceof TextView) {
				anim.setInterpolator(finalInterpolator);
			}
		}
		return;
	}
	
	public boolean isPlaying() {
		return mIsPlaying;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月7日 下午4:08:49 .
	 * @return
	 */
	public boolean isBackward() {
		return this.mPlayingBackwards;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月30日 上午10:59:01 .
	 * @param period
	 */
	protected long nextFrame(long period) {
/*			float elapsedTimeRate = ((float)period)/DURATION_BASE;
		float distanceRate = new DecelerateInterpolator().getInterpolation(elapsedTimeRate);
		long distance = (long)(distanceRate * DURATION_BASE);
		long offsetPercent = (distance * 100) / getDuration();
		long newPercent = mPercentPlayProgress + offsetPercent * (mPlayingBackwards?-1:1);
		return newPercent;*/
		
		if(getDuration() == 0) {
			throw new RuntimeException(this.toString());
		}

		long currentPlayingPercent = getPercentPlayProgress();
		long offsetPlayingPercent = period * 100 / getDuration();
		long newPercent = currentPlayingPercent + offsetPlayingPercent * (mPlayingBackwards?-1:1);
		setCurrentPlayProgress(newPercent);
		return newPercent;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月24日 下午5:26:21 .
	 * @param newPercent
	 */
	protected void onPlayingProgressChange(long newPercent) {
		mPercentPlayProgress = newPercent;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月24日 下午5:26:18 .
	 * @return
	 */
	public long getPercentPlayProgress() {
		return mPercentPlayProgress;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月24日 下午5:26:14 .
	 * @return
	 */
	public long getRemainingPlayTime() {
		return getDuration() - getDuration() * getPercentPlayProgress() /100;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月24日 下午5:53:49 .
	 * @return
	 */
	public long getDonePlayTime() {
		return getDuration() * getPercentPlayProgress() /100;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月30日 上午11:49:40 .
	 */
	protected void onAnimationStart() {
		
		mCinemaMan.mPlayingCinemaList.add(this);
		
		mNumPlayingCinema ++;
		if(mNumPlayingCinema > 0) {
			mCinemaMan.mCinemaIsPlaying  = true;
		}
		else {
			mCinemaMan.mCinemaIsPlaying  = false;
		}
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月30日 上午11:50:39 .
	 */
	protected void onAnimationEnd() {
		
		mNumPlayingCinema --;
		if(mNumPlayingCinema > 0) {
			mCinemaMan.mCinemaIsPlaying = true;
		}
		else {
			mCinemaMan.mCinemaIsPlaying = false;
		}
		
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月22日 下午1:13:39 .
	 */
	protected void setupPlayingNumListener() {}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月20日 下午4:44:43 .
	 */
	public void start() {
		pause();
		mIsPlaying = true;
		onAnimationStart();
		mPlayingBackwards = false;
		
		if (this instanceof CinemaMan.CinemaHowProtect) {
			LogUtils.printWithLogCat("vor", 
					String.format("start: progress[%d] %s",
							mPercentPlayProgress,
							this));
		}
		
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年12月17日 下午5:07:17 .
	 */
	public void startBySystem() {
		final Collection<Animator> animCollection = new ArrayList<Animator>(mAnimatorList);
		AnimatorSet animSet = new AnimatorSet();
		animSet.playTogether(animCollection);//mAnimatorList
		animSet.start();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月24日 下午5:21:42 .
	 * @param delayMillis
	 * @param handler
	 */
	public void startDelayed(long delayMillis) {
		
		pause();
		
		mMsgHandler.postDelayed(mDelayedStart, delayMillis);

		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月30日 上午11:20:20 .
	 */
	public void pause() {

		clearDalayedRunnable();
		if(mIsPlaying) {
			mIsPlaying = false;
			onAnimationEnd();
		}
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月11日 下午6:11:37 .
	 */
	public void hide() {

/*		if (mCinemaType == CINEMA_TYPE_TEXT) {
			if (textIsIn()) {
				setCurrentPlayProgress(PLAY_PROGRESS_MIN);
			} else {
				setCurrentPlayProgress(PLAY_PROGRESS_MAX);
			}
		}
		else {
			if (pictureIsIn()) {
				setCurrentPlayProgress(PLAY_PROGRESS_MIN);
			} else {
				setCurrentPlayProgress(PLAY_PROGRESS_MAX);
			}
		}*/
		
		View v = null;
		for(ObjectAnimator oa: mAnimatorList) {
			 if(oa.getTarget() instanceof View) {
				 v = (View)oa.getTarget();
				 v.setAlpha(0f);
			 }
		}
		
		pause();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月24日 下午3:16:03 .
	 */
	protected void reverseStart() {
		
		pause();
		
		mIsPlaying = true;
		mPlayingBackwards = true;
		onAnimationStart();
		
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月10日 上午9:34:14 .
	 */
	protected void clearDalayedRunnable() {
		mMsgHandler.removeCallbacks(mDelayedStart);
		mMsgHandler.removeCallbacks(mDelayedReverseStart);
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月24日 下午6:00:26 .
	 * @param delayMillis
	 * @param handler
	 */
	protected void reverseDelayed(long delayMillis) {
		
		pause();

		mMsgHandler.postDelayed(mDelayedReverseStart, delayMillis);
		return;
	}
	
	/**
	 * Vulcan created this method in 2014年11月10日 下午3:08:11 .
	 * @param timeMs
	 */
	protected void setCurrentPlayTime(long timeMs) {
		long playTime = timeMs;
		
		for(ObjectAnimator anim: mAnimatorList) {
			long delay = anim.getStartDelay();
			long finalPlayTime = playTime - delay;
			if(finalPlayTime <= 0) {
				finalPlayTime = 0;
			}
			else if(finalPlayTime >= anim.getDuration()) {
				finalPlayTime = anim.getDuration();
			}
			anim.setCurrentPlayTime(finalPlayTime);
		}
		return;
	}

	/**
	 * 
	 * Vulcan created this method in 2014年10月17日 下午3:16:21 .
	 * @param playTime
	 */
	protected void setCurrentPlayProgress(long percent) {
		onPlayingProgressChange(percent);
		long playTime = percent * getDuration() / 100;			
		setCurrentPlayTime(playTime);
		
		if (this instanceof CinemaMan.CinemaHowProtectRightTextIn) {
			//LogUtils.printWithLogCat("vor", String.format("progress[%d] %s",percent,this));
		}
		
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月22日 下午1:48:52 .
	 * @return
	 */
	protected long getDuration() {
		long duration = 0;
		for(Animator anim: mAnimatorList) {
			if(duration < anim.getDuration() + anim.getStartDelay()) {
				duration = anim.getDuration() + anim.getStartDelay();
			}
		}
		return duration;
	}

	abstract protected void createAnim();
	
	public Cinema() {
		mTextStartPosition = TEXT_START_POSITION_CENTER;
		mTextEndPosition = TEXT_START_POSITION_RIGHT;
	}

	/**
	 * 
	 * Vulcan created this method in 2014年11月11日 下午3:37:50 .
	 * @return
	 */
	public boolean textIsIn() {
		return mTextEndPosition == TEXT_START_POSITION_CENTER;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 上午10:37:46 .
	 * @return
	 */
	public boolean pictureIsIn() {
		return true;
	}
	
	
	protected int mCinemaType = CINEMA_TYPE_TEXT;
	private int mNumPlayingCinema = 0;
	protected final HashSet<ObjectAnimator> mAnimatorList = new HashSet<ObjectAnimator>();
	protected long mPercentPlayProgress = 0;
	protected boolean mPlayingBackwards = false;
	protected boolean mIsPlaying = false;
	protected int mTextStartPosition = TEXT_START_POSITION_CENTER;
	protected int mTextEndPosition = TEXT_START_POSITION_CENTER;
	//protected long mElapsedTimeMs = 0;
	private final Runnable mDelayedStart = new Runnable() {

		@Override
		public void run() {
			start();
		}
		
	};
	
	private final Runnable mDelayedReverseStart = new Runnable() {

		@Override
		public void run() {
			reverseStart();
		}
		
	};
	
	private Handler mMsgHandler = new Handler();
	protected CinemaMan mCinemaMan = null;
}
