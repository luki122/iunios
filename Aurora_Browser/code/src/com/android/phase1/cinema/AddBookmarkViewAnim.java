package com.android.phase1.cinema;

import com.android.browser.Controller;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class AddBookmarkViewAnim {

	//正常显示
	public static final int NORMAL_SHOW = 0;
	//正常隐藏
	public static final int NORMAL_HIDE = NORMAL_SHOW + 1;
	//弹出键盘时上移
	public static final int NORMAL_UP = NORMAL_HIDE + 1;
	//收起键盘时下移
	public static final int NORMAL_DOWN = NORMAL_UP + 1;
	//点击保存时向下移动并缩小
	public static final int TRANS_AND_HIDE = NORMAL_DOWN + 1;
	//键盘是否显示
	public static boolean keyboardShowing;

	//弹出框
	private LinearLayout addBookmarkView;
	private boolean mAddBkmkViewDownHide;
	private Controller mController;
	private ImageView bookmark;
	
	// 是否禁止下次动画
	public boolean doNotAnim;
	
	private int topMargin;
	private int bottomMargin;
	
	
	public AddBookmarkViewAnim(LinearLayout addBookmarkView,Controller controller) {
		super();
		this.addBookmarkView = addBookmarkView;
		this.mController = controller;
	}

	/**
	 * 播放动画
	 * @param animKind 动画类型
	 */
	public void startAnim(int animKind) {
		Animation animation = createAnim(animKind,0,0);
		if(animKind == NORMAL_HIDE || animKind == NORMAL_DOWN) {
			doNotAnim = true;
		}
		if (animation != null) {
			addBookmarkView.startAnimation(animation);
			if(animKind == NORMAL_HIDE) {
				playBackgroundAnim(200);
			}
		}
	}
	
	/**
	 * 播放动画
	 * @param animKind 动画类型，这个函数目前主要处理TRANS_AND_HIDE动画
	 * @param bookmark 工具条中显示星星的ImageView
	 */
	public void startAnim(int animKind, ImageView bookmark) {
		int[] location = new int[2];
		bookmark.getLocationOnScreen(location);
		Animation animation = createAnim(animKind,location[0],location[1] + bookmark.getHeight() / 2);
		if(animKind == TRANS_AND_HIDE) {
			doNotAnim = true;
		}
		if (animation != null) {
			addBookmarkView.startAnimation(animation);
			if(animKind == TRANS_AND_HIDE) {
				playBackgroundAnim(550);
			}
		}
		this.bookmark = bookmark;
	}
	
	/**
	 * 当弹出框消失的时候，播放弹出框后蒙灰层的动画，让其渐渐消失
	 * @param duration
	 */
	private void playBackgroundAnim(int duration) {
		AlphaAnimation animation = createAlphaAnimation(1.0f, 0, duration);
		LinearLayout layout = (LinearLayout) addBookmarkView.getParent();
		layout.startAnimation(animation);
	}

	/**
	 * 创建动画
	 * @param animKind 动画类型
	 * @param aimX 用于确定TRANS_AND_HIDE动画的目标移动位置（X轴方向），其它的动画传0就行了。
	 * @param aimY 用于确定TRANS_AND_HIDE动画的目标移动位置（Y轴方向），其它的动画传0就行了。
	 * @return
	 */
	private Animation createAnim(int animKind, int aimX, int aimY) {
		switch (animKind) {
		case NORMAL_SHOW: {
			setViewCenter();
			
			ScaleAnimation scaleAnimation = new ScaleAnimation(0.5f, 1.0f,
					0.5f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			scaleAnimation.setDuration(100);

			AlphaAnimation alphaAnimation = createAlphaAnimation(0.5f, 1.0f, 75);

			AnimationSet animationSet = new AnimationSet(false);
			animationSet.addAnimation(scaleAnimation);
			animationSet.addAnimation(alphaAnimation);
			return animationSet;
		}

		case NORMAL_HIDE: {
			ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.5f,
					1.0f, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			scaleAnimation.setDuration(150);

			AlphaAnimation alphaAnimation = createAlphaAnimation(1.0f, 0.5f, 150);

			AnimationSet animationSet = new AnimationSet(false);
			animationSet.addAnimation(scaleAnimation);
			animationSet.addAnimation(alphaAnimation);
			animationSet.setFillAfter(true);
			animationSet.setAnimationListener(new BookmarkAnimationListener(
					animKind));
			return animationSet;
		}

		case NORMAL_UP: {
			TranslateAnimation translateAnimation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
					0, Animation.RELATIVE_TO_SELF, 0,
					Animation.RELATIVE_TO_SELF, -0.5f);
			translateAnimation.setDuration(200);
			translateAnimation.setFillAfter(true);
			LinearLayout.LayoutParams params = (LayoutParams) addBookmarkView
					.getLayoutParams();
			topMargin = params.topMargin;
			bottomMargin = params.bottomMargin;
			
			translateAnimation
					.setAnimationListener(new BookmarkAnimationListener(animKind));
			return translateAnimation;
		}
		
		case NORMAL_DOWN: {
			TranslateAnimation translateAnimation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
					0, Animation.RELATIVE_TO_SELF, 0,
					Animation.RELATIVE_TO_SELF, 0.5f);
			translateAnimation.setDuration(200);
			translateAnimation.setFillAfter(true);
			translateAnimation
					.setAnimationListener(new BookmarkAnimationListener(animKind));
			return translateAnimation;
		}
		
		case TRANS_AND_HIDE: {
			int location[] = new int[2];
			addBookmarkView.getLocationOnScreen(location);
			AnimationSet animationSet = new AnimationSet(false);
			
			//缩放动画
			ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.1f,
					1.0f, 0.1f, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			animationSet.addAnimation(scaleAnimation);
			
			//透明度动画
			AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0);
			animationSet.addAnimation(alphaAnimation);
			
			//位移动画
			TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, aimY - (location[1] + addBookmarkView.getHeight() / 2));
			animationSet.addAnimation(translateAnimation);
			
			animationSet.setFillAfter(true);
			animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
			animationSet.setDuration(500);
			animationSet.setAnimationListener(new BookmarkAnimationListener(
					TRANS_AND_HIDE));
			return animationSet;
		}

		default:
			break;
		}
		return null;
	}

	public void setViewUp() {
		LinearLayout.LayoutParams params = (LayoutParams) addBookmarkView
				.getLayoutParams();

		params.setMargins(params.leftMargin, topMargin
				- addBookmarkView.getHeight() / 2, params.rightMargin,
				bottomMargin);
		addBookmarkView.clearAnimation();
		addBookmarkView.setLayoutParams(params);
	}
	
	public void setViewCenter() {
		LinearLayout.LayoutParams params = (LayoutParams) addBookmarkView
				.getLayoutParams();
		params.setMargins(params.leftMargin, 0, params.rightMargin, 0);
		params.gravity = Gravity.CENTER;
		addBookmarkView.setLayoutParams(params);
	}

	/**
	 * 动画事件监听器
	 * @author leo
	 *
	 */
	private class BookmarkAnimationListener implements AnimationListener {

		private int animationKind;

		BookmarkAnimationListener(int animationKind) {
			this.animationKind = animationKind;
		}

		@Override
		public void onAnimationStart(Animation animation) {

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			switch (this.animationKind) {
			case NORMAL_HIDE: {
				LinearLayout layout = (LinearLayout) addBookmarkView.getParent();
				layout.setVisibility(View.GONE);
				mController.getCurrentTopWebView().requestFocus();
				doNotAnim = false;
				break;
			}

			case NORMAL_UP: {
				setViewUp();
				break;
			}
			
			case NORMAL_DOWN: {
				LinearLayout.LayoutParams params = (LayoutParams) addBookmarkView
						.getLayoutParams();

				params.setMargins(params.leftMargin, 0, params.rightMargin, 0);
				params.gravity = Gravity.CENTER;
				addBookmarkView.clearAnimation();
				addBookmarkView.setLayoutParams(params);
				doNotAnim = false;
				if(mAddBkmkViewDownHide) {
					startAnim(NORMAL_HIDE);
					mAddBkmkViewDownHide = false;
				}else {
					addBookmarkView.requestFocus();
				}
				break;
			}
			
			case TRANS_AND_HIDE: {
				((LinearLayout) addBookmarkView.getParent())
				.setVisibility(View.GONE);
				mController.getCurrentTopWebView().requestFocus();
				doNotAnim = false;
				playAddBookmarkStarAnim();
				break;
			}
			
			default:
				break;
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

	}
	
	/**
	 * 播放底部工具条星星图标动画
	 */
	private void playAddBookmarkStarAnim() {
		//设置星星背景
		this.bookmark.setBackgroundResource(this.mController.addbookmarkBg);
		//将原imageview中显示的星星去掉
		this.bookmark.setImageBitmap(null);
		AnimationDrawable drawable = (AnimationDrawable) this.bookmark.getBackground();
		
		//计算动画总时间
		int totalTime = 0;
        for(int i=0; i<drawable.getNumberOfFrames(); i++){ 
        	totalTime += drawable.getDuration(i); 
        } 
		
        //在动画启动之前先调用stop，要不然动画只会播一次
		drawable.stop();
		drawable.start();
		
		//动画结束事件
		new Handler().postDelayed(new Runnable() { 

            public void run() { 
            	bookmark.setImageResource(mController.addbookmarkSrc);
            	bookmark.setBackgroundResource(0);
            	bookmark.invalidate();
            } 
        }, totalTime); 
	}
	
	/**
	 * 生成透明度动画
	 * @param start 起始透明度
	 * @param end 结束透明度
	 * @param duration 持续时间
	 * @return
	 */
	private AlphaAnimation createAlphaAnimation(float start, float end, int duration) {
		AlphaAnimation animation = new AlphaAnimation(start, end);
		animation.setDuration(duration);
		return animation;
	}

	public void setAddBkmkViewDownHide(boolean addBkmkViewDownHide) {
		this.mAddBkmkViewDownHide = addBkmkViewDownHide;
	}

}
