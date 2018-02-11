package com.aurora.account.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.aurora.account.R;
import com.aurora.account.util.Log;

public class SyncProgressView extends FrameLayout {
	
	public final String TAG = "SyncProgressView";
	
	public static final int STATUS_NORMAL = 1;		// 普通模式
	public static final int STATUS_PROGRESS = 2;	// 进度模式
	
	private final int PROGRESS_TIME = 500;		// 进度动画时间
	private final int ANIM_DURING_TIME = 1500;	// 切换状态动画时间

	private int mStatus;
	
	private boolean runningStartAnim = false;	// 是否正在进行开始动画
	private boolean runningEndAnim = false;		// 是否正在进行结束动画
	
	private boolean needRunStartAnim = false;	// 是否需要结束动画完成时马上运行开始动画
	private boolean needRunEndAnim = false;		// 是否需要开始动画完成时马上运行结束动画
	
	private ImageView imageView;
	private RoundProgressView roundProgressView;
	
	public SyncProgressView(Context context) {
		super(context);
		initView();
	}

	public SyncProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	private void initView() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.view_sync_progress, this);
		imageView = (ImageView) view.findViewById(R.id.imageView);
		roundProgressView = (RoundProgressView) view.findViewById(R.id.roundProgressView);
		
		setStatus(STATUS_NORMAL);
	}
	
	/**
	* @Title: setImage
	* @Description: TODO 设置图片
	* @param @param drawable
	* @return void
	* @throws
	 */
	public void setImage(Drawable drawable) {
		if (imageView != null) {
			imageView.setImageDrawable(drawable);
		}
	}
	
	/**
	* @Title: setImage
	* @Description: TODO 设置图片
	* @param @param bitmap
	* @return void
	* @throws
	 */
	public void setImage(Bitmap bitmap) {
		if (imageView != null) {
			imageView.setImageBitmap(bitmap);
		}
	}
	
	/**
	* @Title: setImage
	* @Description: TODO 设置图片
	* @param @param resId
	* @return void
	* @throws
	 */
	public void setImage(int resId) {
		if (imageView != null) {
			imageView.setImageResource(resId);
		}
	}
	
	/**
	* @Title: startProgress
	* @Description: TODO 开始进入进度动画
	* @param 
	* @return void
	* @throws
	 */
	public void startProgress() {
		if (runningEndAnim) {
			needRunStartAnim = true;
			return;
		}
		
		if (mStatus == STATUS_NORMAL && !runningStartAnim) {
			AlphaAnimation alphaHide = new AlphaAnimation(1.0f, 0f);
			alphaHide.setDuration(ANIM_DURING_TIME);
			
			AnimationSet showSet = new AnimationSet(true);
			
			AlphaAnimation alphaShow = new AlphaAnimation(0f, 1.0f);
			alphaShow.setDuration(ANIM_DURING_TIME);
			
			ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, 
					Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f);
			scaleAnimation.setDuration(ANIM_DURING_TIME);
			
			showSet.addAnimation(alphaShow);
			showSet.addAnimation(scaleAnimation);
			
			showSet.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					runningStartAnim = true;
					roundProgressView.setVisibility(View.VISIBLE);
					roundProgressView.setProgress(0);
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					runningStartAnim = false;
					setStatus(STATUS_PROGRESS);
					
					if (needRunEndAnim) {
						Log.i(TAG, "needRunEndAnim run endProgress");
						needRunEndAnim = false;
						endProgress();
					}
				}
			});
			
			imageView.startAnimation(alphaHide);
			roundProgressView.startAnimation(showSet);
		}
	}
	
	/**
	* @Title: endProgress
	* @Description: TODO 结束进度动画
	* @param 
	* @return void
	* @throws
	 */
	public void endProgress() {
		if (runningStartAnim) {
			needRunEndAnim = true;
			return;
		}
		
		if (mStatus == STATUS_PROGRESS && !runningEndAnim) {
			AlphaAnimation alphaHide = new AlphaAnimation(1.0f, 0f);
			alphaHide.setDuration(ANIM_DURING_TIME);
			
			AlphaAnimation alphaShow = new AlphaAnimation(0f, 1.0f);
			alphaShow.setDuration(ANIM_DURING_TIME);
			
			alphaShow.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					runningEndAnim = true;
					imageView.setVisibility(View.VISIBLE);
//					roundProgressView.setProgressAnim(100, PROGRESS_TIME);
					roundProgressView.setProgressAndHide(100, PROGRESS_TIME, ANIM_DURING_TIME);
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					runningEndAnim = false;
					setStatus(STATUS_NORMAL);
					
					if (needRunStartAnim) {
						Log.i(TAG, "needRunStartAnim run startProgress");
						needRunStartAnim = false;
						startProgress();
					}
				}
			});
			
			imageView.startAnimation(alphaShow);
			roundProgressView.startAnimation(alphaHide);
		}
	}
	
	/**
	* @Title: setProgress
	* @Description: TODO 设置进度
	* @param @param progress
	* @return void
	* @throws
	 */
	public void setProgress(int progress) {
		roundProgressView.setProgress(progress);
	}
	
	/**
	* @Title: setProgressAnim
	* @Description: TODO 设置进度（带动画）
	* @param @param progress
	* @return void
	* @throws
	 */
	public void setProgressAnim(int progress) {
		roundProgressView.setProgressAnim(progress, PROGRESS_TIME);
	}
	
	/**
	* @Title: setStatus
	* @Description: TODO 设置状态
	* @param @param status
	* @return void
	* @throws
	 */
	public void setStatus(int status) {
		if (mStatus == status) {
			return;
		}
		mStatus = status;
		imageView.clearAnimation();
		roundProgressView.clearAnimation();
		
		switch (status) {
		case STATUS_NORMAL:
			imageView.setVisibility(View.VISIBLE);
			roundProgressView.setVisibility(View.GONE);
			break;
		case STATUS_PROGRESS:
			imageView.setVisibility(View.GONE);
			roundProgressView.setVisibility(View.VISIBLE);
			break;
		}
	}
	
	/**
	* @Title: getStatus
	* @Description: TODO 获取状态
	* @param @return
	* @return int
	* @throws
	 */
	public int getStatus() {
		return mStatus;
	}

}
