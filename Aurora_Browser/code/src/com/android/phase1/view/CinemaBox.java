/**
 * Vulcan created this file in 2015年4月9日 下午4:51:19 .
 */
package com.android.phase1.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.android.browser.R;
import com.android.phase1.model.CinemaBoxCreatePara;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;


/**
 * Vulcan created CinemaBox in 2015年4月9日 .
 * 
 */
public abstract class CinemaBox {
	
	public static final int DURATION_MULTI = 1;
	public static final int DURATION = 400;
	public static final int TOOLBAR_DELAY = 250;
	
	public static final String TAG = "CinemaBox";

	public static class CinemaItem{
		public boolean mIsSelected;
		public AuroraNavTabView mAuroraNavTabView;
		public ImageView mTitleBar;
		public ImageView mMainWin;
		public final Rect mTitleBarRect = new Rect();
		public final Rect mMainWinRect = new Rect();
		

		public void setScaleFactor(float sf) {
	        mScale = sf;
	        Matrix m = new Matrix();
	        m.postScale(sf,sf);
	        mMainWin.setImageMatrix(m);
	    }


	    public float getScaleFactor() {
	        return mScale;
	    }
	    
	    private float mScale;

	}
	
	public interface CinemaListener {
		public void onCinemaEnd();
		public void onCinemaStart();
	}
	

	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 上午10:49:44 .
	 * @param item
	 */
	public void addItem(CinemaItem item) {
		mCinemaBoxCreatePara.getCinemaItemList().add(item);
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 上午11:42:10 .
	 */
	public void clearCinemaItemList() {
		mCinemaBoxCreatePara.getCinemaItemList().clear();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 上午10:59:24 .
	 * @return
	 */
	public List<CinemaItem> getCinemaItemList() {
		return mCinemaBoxCreatePara.getCinemaItemList();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 上午11:02:18 .
	 * @return
	 */
	public int getScreenWidth() {
		return mCinemaBoxCreatePara.getWidth();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 上午11:02:39 .
	 * @return
	 */
	public int getScreenHeight() {
		return mCinemaBoxCreatePara.getHeight();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 下午6:14:04 .
	 */
	@SuppressWarnings("unused")
	private void resort() {
		CinemaItem selected = null;
		for(CinemaItem ci: mCinemaItemList) {
			if(isSelectedItem(ci)) {
				selected = ci;
				break;
			}
		}
		if(selected != null) {
			mCinemaItemList.remove(selected);
			mCinemaItemList.add(selected);
		}
		
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月11日 上午11:50:25 .
	 */
	public void performAnim() {
		mAnimatorSet.start();
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月9日 下午5:42:17 .
	 * @param ctx
	 * @param w
	 * @param h
	 * @param cl
	 */
	public void initAnim(Context ctx, int w, int h, CinemaListener cl, final ViewGroup root) {
		
		//resort();
		
		inflate(ctx,w,h, root);
		
		final CinemaListener cinemaListener = cl;
		final List<ObjectAnimator> l = new ArrayList<ObjectAnimator>();
		for(CinemaItem ci: mCinemaItemList) {
			ObjectAnimator[] oaList = null;
			if(isSelectedItem(ci)) {
				oaList = createMainWinShowAnim(ci,w,h);
			}
			else {
				oaList = createMainWinExitAnim(ci,w,h);
			}
			l.addAll(Arrays.asList(oaList));
		}
		
		l.addAll(Arrays.asList(createTitltNToolBarAnim()));
		
		AnimatorSet as = new AnimatorSet();
		ObjectAnimator oaList[] = new ObjectAnimator[l.size()];
		l.toArray(oaList);
		as.playTogether(oaList);
		AnimatorListener al = new AnimatorListener() {

			
			@Override public void onAnimationRepeat(Animator arg0) {}
			@Override public void onAnimationCancel(Animator arg0) {}

			@Override
			public void onAnimationEnd(Animator arg0) {
				if(cinemaListener != null) {
					cinemaListener.onCinemaEnd();
				}
				//root.animate().setDuration(5000).alpha(0f).start();
			}
			
			@Override
			public void onAnimationStart(Animator arg0) {
				if(cinemaListener != null) {
					cinemaListener.onCinemaStart();
				}
			}

		};
		//as.setDuration(400);
		//as.setInterpolator(new DecelerateInterpolator());
		as.addListener(al);
		mAnimatorSet = as;
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月9日 下午5:26:21 .
	 * @param ctx
	 * @param w
	 * @param h
	 */
	@SuppressLint("InflateParams")
	private void inflate(Context ctx,int w, int h, ViewGroup root) {
		
		//status bar
		ViewGroup rl2 = (ViewGroup)LayoutInflater.from(ctx).inflate(R.layout.aurora_cinema_box_item,null);
		root.addView(rl2,w,h);

		ImageView ivMockStatusBar = (ImageView)rl2.findViewById(R.id.aurora_cinema_box_tab_title);
		
		RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams)ivMockStatusBar.getLayoutParams();
		lp2.width = mCinemaBoxCreatePara.getTitleBarBmp().getWidth();
		lp2.height = mCinemaBoxCreatePara.getTitleBarPos().y + mCinemaBoxCreatePara.getTitleBarBmp().getHeight();
		lp2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp2.setMarginsRelative(0,0 , 0, 0);
		ivMockStatusBar.setBackgroundResource(R.drawable.urlbar_bg);
		//ivMockStatusBar.setBackgroundResource(R.color.red);
		ivMockStatusBar.setAlpha(0f);
		mMockStatusBar = ivMockStatusBar;
		
		for(CinemaItem ci: mCinemaItemList) {
			ViewGroup rl = (ViewGroup)LayoutInflater.from(ctx).inflate(R.layout.aurora_cinema_box_item,null);
			
			root.addView(rl,w,h);

			ImageView ivTitle = (ImageView)rl.findViewById(R.id.aurora_cinema_box_tab_title);
			ImageView ivMainWin = (ImageView)rl.findViewById(R.id.aurora_cinema_box_tab_mainwin);

			ci.mMainWin = ivMainWin;
			ci.mTitleBar = ivTitle;

			ivTitle.setLeft(ci.mTitleBarRect.left);
			ivTitle.setTop(ci.mTitleBarRect.top);
			ivTitle.setRight(ci.mTitleBarRect.right);
			ivTitle.setBottom(ci.mTitleBarRect.bottom);
			ivTitle.invalidate();
			//rl.addView(ivTitle,ci.mTitleBarRect.width(),ci.mTitleBarRect.height());
			
			Log.d(TAG, 
					String.format("inflate: rl = %s, ivTitle = %s, (%d,%d,%d,%d)",
							rl.hashCode(),ivTitle.hashCode(),
							ci.mTitleBarRect.left,
							ci.mTitleBarRect.top,
							ci.mTitleBarRect.right,
							ci.mTitleBarRect.bottom));
			
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)ivMainWin.getLayoutParams();
			lp.width = ci.mMainWinRect.width();
			lp.height = ci.mMainWinRect.height();
			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			lp.setMarginsRelative(ci.mMainWinRect.left, ci.mMainWinRect.top, 0, 0);
			ivMainWin.setLayoutParams(lp);
			ivMainWin.setImageBitmap(ci.mAuroraNavTabView.getScreenshot());
			//ivMainWin.setBackgroundResource(R.color.red);
			Log.d(TAG, 
					String.format("inflate: isSel = %b, top =%d getScreenshot = %s",
							ci.mIsSelected,
							ci.mTitleBarRect.top,
							ci.mAuroraNavTabView.getScreenshot()));
			ci.setScaleFactor(ci.mMainWinRect.width() / (float)ci.mMainWin.getDrawable().getIntrinsicWidth());
		}
		
		//titlebar & toolbar
		ViewGroup rl1 = (ViewGroup)LayoutInflater.from(ctx).inflate(R.layout.aurora_cinema_box_item,null);
		root.addView(rl1,w,h);

		ImageView ivTitleBar = (ImageView)rl1.findViewById(R.id.aurora_cinema_box_tab_title);
		ImageView ivToolBar = (ImageView)rl1.findViewById(R.id.aurora_cinema_box_tab_mainwin);
		
		RelativeLayout.LayoutParams lp1 = (RelativeLayout.LayoutParams)ivTitleBar.getLayoutParams();
		lp1.width = mCinemaBoxCreatePara.getTitleBarBmp().getWidth();
		lp1.height = mCinemaBoxCreatePara.getTitleBarBmp().getHeight();
		lp1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		lp1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp1.setMarginsRelative(mCinemaBoxCreatePara.getTitleBarPos().x,
				mCinemaBoxCreatePara.getTitleBarPos().y, 0, 0);
		ivTitleBar.setImageBitmap(mCinemaBoxCreatePara.getTitleBarBmp());
		ivTitleBar.setAlpha(0f);
		mTitleBar = ivTitleBar;
		
		lp1 = (RelativeLayout.LayoutParams)ivToolBar.getLayoutParams();
		lp1.width = mCinemaBoxCreatePara.getToolBarBmp().getWidth();
		lp1.height = mCinemaBoxCreatePara.getToolBarBmp().getHeight();
		lp1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		lp1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp1.setMarginsRelative(mCinemaBoxCreatePara.getToolBarPos().x,
				mCinemaBoxCreatePara.getToolBarPos().y, 0, 0);
		ivToolBar.setImageBitmap(mCinemaBoxCreatePara.getToolBarBmp());
		ivToolBar.setAlpha(0f);
		mToolBar = ivToolBar;
		
		

		
		
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 下午4:05:35 .
	 * @param ci
	 * @param w
	 * @param h
	 * @return
	 */
	private ObjectAnimator[] createMainWinExitAnim(CinemaItem ci, int w, int h) {
		final ImageView mainWin = ci.mMainWin;
		final int yFrom = ci.mMainWinRect.top;
		final int yTo = h;

		ObjectAnimator[] sideAnims = new ObjectAnimator[] {
				ObjectAnimator.ofFloat(mainWin, "y", yFrom, yTo),
				ObjectAnimator.ofFloat(mainWin, "alpha", 1f, 0f)
		};
		
		for(ObjectAnimator oa: sideAnims) {
			oa.setDuration(DURATION * DURATION_MULTI);
		}

		return sideAnims;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月11日 下午3:55:17 .
	 * @return
	 */
	private ObjectAnimator[] createTitltNToolBarAnim() {
		ObjectAnimator[] sideAnims = new ObjectAnimator[] {
				ObjectAnimator.ofFloat(mTitleBar, "alpha", 0f, 1f),
				ObjectAnimator.ofFloat(mToolBar, "alpha", 0f, 1f),
				ObjectAnimator.ofFloat(mMockStatusBar, "alpha", 0f, 1f)
		};
		
		for(ObjectAnimator oa: sideAnims) {
			oa.setStartDelay(TOOLBAR_DELAY * DURATION_MULTI);
			oa.setDuration((DURATION - TOOLBAR_DELAY) * DURATION_MULTI);
		}

		return sideAnims;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月9日 下午5:27:42 .
	 * @param ci
	 * @param w
	 * @param h
	 * @return
	 */
	private ObjectAnimator[] createMainWinShowAnim(CinemaItem ci, int w, int h) {
		final ImageView mainWin = ci.mMainWin;
		final int leftFrom = ci.mMainWinRect.left;
		final int rightFrom = ci.mMainWinRect.right;
		final int topFrom = ci.mMainWinRect.top;
		final int bottomFrom = ci.mMainWinRect.bottom;

		final int leftTo = 0;
		final int rightTo = w;
		final int topTo = mCinemaBoxCreatePara.getWebTop();
		final int bottomTo = mCinemaBoxCreatePara.getWebBottom();

		final float scaleFactor1 =  ci.mMainWinRect.width() / (float)ci.mMainWin.getDrawable().getIntrinsicWidth();
		final float scaleFactor2 =  w / (float)ci.mMainWin.getDrawable().getIntrinsicWidth();

		ci.setScaleFactor(scaleFactor2);

		ObjectAnimator[] sideAnims = new ObjectAnimator[] {
				ObjectAnimator.ofFloat(ci, "scaleFactor", scaleFactor1, scaleFactor2),
				ObjectAnimator.ofInt(mainWin, "left", leftFrom, leftTo),
				ObjectAnimator.ofInt(mainWin, "right", rightFrom, rightTo),
				ObjectAnimator.ofInt(mainWin, "top", topFrom, topTo),
				ObjectAnimator.ofInt(mainWin, "bottom", bottomFrom, bottomTo)
		};
		
		for(ObjectAnimator oa: sideAnims) {
			oa.setDuration(DURATION * DURATION_MULTI);
		}

		return sideAnims;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 上午11:45:38 .
	 * @param w
	 * @param h
	 */
	public void setBoxSize(int w, int h) {
		mCinemaBoxCreatePara.setWidth(w);
		mCinemaBoxCreatePara.setHeight(h);
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 上午11:47:16 .
	 * @param t
	 * @param b
	 */
	public void setWebviewTopBottom(int t, int b) {
		mCinemaBoxCreatePara.setWebTop(t);
		mCinemaBoxCreatePara.setWebBottom(b);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 下午2:21:35 .
	 * @param cl
	 */
	public void setCinemaListener(CinemaListener cl) {
		mCinemaBoxCreatePara.setCinemaListener(cl);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 下午4:12:34 .
	 * @param ci
	 * @return
	 */
	public boolean isSelectedItem(CinemaItem ci) {
		return ci.mIsSelected;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月11日 下午2:56:08 .
	 * @param b
	 */
	public void setTitleBarBmp(Bitmap b, Point pos) {
		mCinemaBoxCreatePara.setTitleBarBmp(b,pos);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月11日 下午4:58:29 .
	 * @param b
	 * @param pos
	 */
	public void setToolBarBmp(Bitmap b, Point pos) {
		mCinemaBoxCreatePara.setToolBarBmp(b,pos);
	}
	
	
	
	public abstract void playAnim(Activity a);
	public abstract void attachToViewGroup(ViewGroup vg);
	public abstract void dettachFromViewGroup(ViewGroup vg);
	protected final CinemaBoxCreatePara mCinemaBoxCreatePara = CinemaBoxCreatePara.getInstance();
	protected final List<CinemaItem> mCinemaItemList = mCinemaBoxCreatePara.getCinemaItemList();
	protected ImageView mTitleBar = null;
	protected ImageView mToolBar = null;
	protected ImageView mMockStatusBar = null;
	protected AnimatorSet mAnimatorSet = null;
	
}
