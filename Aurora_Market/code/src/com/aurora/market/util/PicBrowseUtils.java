package com.aurora.market.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment.InstantiationException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class PicBrowseUtils {

	public static List<ImageView> mPicImgVList = new ArrayList<ImageView>();
	// private static Activity mActivity;
	// private static ImageView mCurAnimImgV;
	// private static ImageView mCurDisplayImgV;
	private static int mPicIndex;
	// private static int mStatusHeight;
	// private static int[] mLocation;
	// private static int[] mDimension;
	private static int mDefaultLocation[] = new int[2];
	private static int[] mDimen = new int[2];

	// private static float mScreenWidth;
	// private static float mScreenHeight;

	public static void resetImgVContainer() {
		mPicImgVList.clear();

	}

	/*
	 * public static void initUtils(Activity pActivity,ImageView pAnimImg,
	 * ImageView pDisplayImg, int pPicIndex){ mActivity = pActivity;
	 * mCurAnimImgV = pAnimImg; mCurDisplayImgV = pDisplayImg; mPicIndex =
	 * pPicIndex;
	 * 
	 * DisplayMetrics dm = new DisplayMetrics();
	 * mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
	 * 
	 * mScreenWidth = dm.widthPixels; mScreenHeight = dm.heightPixels;
	 * 
	 * mStatusHeight = getStatusHeight(mActivity); }
	 */

	public static void addImgV(ImageView pPicImgV) {

		int[] location = new int[2];

		final Rect rect = new Rect();
		pPicImgV.getHitRect(rect);
		pPicImgV.getLocationOnScreen(location);

		mPicImgVList.add(pPicImgV);
	}

	public static void setDefaultPicIndex(int pIndex) {

		mPicIndex = pIndex;
		// mPicImgVList.get(pIndex).getHitRect(rect);
		try {
			ImageView imageView = mPicImgVList.get(pIndex);
			if (imageView != null) {
				imageView.getLocationOnScreen(mDefaultLocation);
				mDimen[0] = mPicImgVList.get(pIndex).getWidth();
				mDimen[1] = mPicImgVList.get(pIndex).getHeight();
			}
		} catch (Exception e) {
			Log.e("picbrowseutil", "内存被回收了");
		}

	}

	public static int getDefaultPicIndex() {
		return mPicIndex;
	}

	public static int[] getCurImgVLoc(int pIndex) {
		int[] location = new int[2];

		final Rect rect = new Rect();
		try {

			// ImageView imageView = mPicImgVList.get(pIndex);
			mPicImgVList.get(pIndex).getHitRect(rect);
			mPicImgVList.get(pIndex).getLocationOnScreen(location);

		} catch (Exception e) {
			Log.e("picbrowseutil", "内存被回收了");
			location = mDefaultLocation;
		}

		return location;
	}

	public static int[] getCurImgVDimension(int pIndex) {
		int[] dimen = new int[2];
		try {
			dimen[0] = mPicImgVList.get(pIndex).getWidth();
			dimen[1] = mPicImgVList.get(pIndex).getHeight();
		} catch (Exception e) {
			Log.e("picbrowseutil", "内存被回收了");
			dimen = mDimen;
		}
		return dimen;
	}

	public static Drawable getImgVByIndex(int pIndex) {

		return mPicImgVList.get(pIndex).getDrawable();

	}

	/*
	 * private static void initImgVDimenAndLoc(){ mLocation =
	 * getCurImgVLoc(mPicIndex); mLocation[1] = mLocation[1] - mStatusHeight;
	 * 
	 * mDimension = getCurImgVDimension(mPicIndex);
	 * 
	 * Log.v("aurora.jiangmx aniamtion", "imageview Yposition: " +
	 * mLocation[1]); Log.v("aurora.jiangmx aniamtion", "imageview width: " +
	 * mDimension[0]);
	 * 
	 * Log.v("aurora.jiangmx aniamtion",
	 * "-----------------------------------------------");
	 * 
	 * }
	 * 
	 * private static void initAnimImgVDimenAndLoc(){
	 * 
	 * mCurAnimImgV.setX(mLocation[0]); mCurAnimImgV.setY(mLocation[1]);
	 * 
	 * Log.v("aurora.jiangmx", "locationX: " + mLocation[0] + "locationY: " +
	 * mLocation[1]);
	 * 
	 * mCurAnimImgV.setLayoutParams(new
	 * RelativeLayout.LayoutParams(mDimension[0], mDimension[1]));
	 * mCurAnimImgV.setVisibility(View.VISIBLE);
	 * 
	 * }
	 * 
	 * public static void startPicBrowseAnim(){ initImgVDimenAndLoc();
	 * initAnimImgVDimenAndLoc();
	 * 
	 * float startX = (mLocation[0] + mDimension[0]*1.0f/2)/mDimension[0]; float
	 * startY = (mLocation[1] + mDimension[1]*1.0f/2)/mDimension[1];
	 * 
	 * final float deltX = mScreenWidth/mDimension[0]; final float deltY =
	 * mScreenHeight/mDimension[1]; //可能不够准确
	 * 
	 * float moveX = mScreenWidth/2 - (mLocation[0] * 1.0f+
	 * mDimension[0]*1.0f/2); float moveY = mScreenHeight/2 - (mLocation[1] *
	 * 1.0f + mDimension[1]*1.0f/2);
	 * 
	 * // ScaleAnimation lScaleAnim = new ScaleAnimation(0f, deltX, 1f, deltY);
	 * AnimationSet lSetAnimation = new AnimationSet(true);
	 * 
	 * // TranslateAnimation lTranAnim = new TranslateAnimation(
	 * Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT,
	 * moveX/mScreenWidth, // Animation.RELATIVE_TO_PARENT,0,
	 * Animation.RELATIVE_TO_PARENT, moveY/mScreenHeight );
	 * 
	 * TranslateAnimation lTranAnim = new TranslateAnimation(
	 * Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
	 * moveX/mDimension[0], Animation.RELATIVE_TO_SELF,0,
	 * Animation.RELATIVE_TO_SELF, moveY/mDimension[1] );
	 * 
	 * lTranAnim.setDuration(300);
	 * 
	 * ScaleAnimation lScaleAnim = new ScaleAnimation(1f, deltX, 1f, deltY,
	 * Animation.RELATIVE_TO_SELF, startX, Animation.RELATIVE_TO_SELF, startY);
	 * lScaleAnim.setDuration(300);
	 * 
	 * Log.v("aurora.jiangmx startAnimation", "TranslateAnimation moveX/: " +
	 * moveX/mDimension[0] + "moveY/ : " + moveX/mDimension[1] );
	 * Log.v("aurora.jiangmx startAnimation", "ScaleAnimation deltX: " + deltX+
	 * "deltY : " + deltY + " startX: " + startX + " startY: " + startY) ;
	 * 
	 * lSetAnimation.addAnimation(lScaleAnim); // 这两个动画，add顺序颠倒过来就不能获得想要的效果
	 * lSetAnimation.addAnimation(lTranAnim);
	 * 
	 * // lSetAnimation.setFillAfter(true);
	 * lSetAnimation.setAnimationListener(new AnimationListener() {
	 * 
	 * @Override public void onAnimationStart(Animation animation) { // TODO
	 * Auto-generated method stub
	 * 
	 * }
	 * 
	 * @Override public void onAnimationRepeat(Animation animation) { // TODO
	 * Auto-generated method stub
	 * 
	 * }
	 * 
	 * @Override public void onAnimationEnd(Animation animation) { // TODO
	 * Auto-generated method stub mCurAnimImgV.setVisibility(View.GONE); //
	 * mCurDisplayImgV.setBackgroundResource(R.drawable.meitu_test);
	 * mCurDisplayImgV.setLayoutParams(new RelativeLayout.LayoutParams(
	 * LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	 * mCurDisplayImgV.setVisibility(View.VISIBLE); } });
	 * 
	 * mCurAnimImgV.startAnimation(lSetAnimation);
	 * 
	 * } public static void startBackAnimation(){
	 * 
	 * int lWidth = mCurDisplayImgV.getWidth(); int lHeight =
	 * mCurDisplayImgV.getHeight();
	 * 
	 * final Rect rect = new Rect(); mCurDisplayImgV.getHitRect(rect);
	 * 
	 * float startX = lWidth*1.0f/2; float startY = lHeight*1.0f/2;
	 * 
	 * final float deltX = mDimension[0]/mScreenWidth; final float deltY =
	 * mDimension[1]/(mScreenHeight - mStatusHeight);
	 * 
	 * float lDisplayPicX = mCurDisplayImgV.getX(); float lDisplayPicY =
	 * mCurDisplayImgV.getY();
	 * 
	 * float moveX = (mLocation[0] * 1.0f+ mDimension[0]*1.0f/2) - (lDisplayPicX
	 * + mCurDisplayImgV.getWidth()*1.0f/2); float moveY = (mLocation[1] +
	 * mDimension[1]*1.0f/2) - (lDisplayPicY +
	 * mCurDisplayImgV.getHeight()*1.0f/2); // float moveX =(picLocation[0] *
	 * 1.0f+ mOriWidth*1.0f/2) - mScreenWidth/2 ; // float moveY
	 * =(picLocation[1] + mOriHeight*1.0f/2) - (mScreenHeight -
	 * mStatusHeight)/2;
	 * 
	 * Log.v("aurora.jiangmx", "Screent width: " + mScreenWidth +
	 * " Screen Height: " + mScreenHeight); Log.v("aurora.jiangmx", "startX: " +
	 * startX + "startY: " + startY); Log.v("aurora.jiangmx", "moveX: " + moveX
	 * + "moveY: " + moveY); Log.v("aurora.jiangmx", "movex/lWidth: " +
	 * moveX/lWidth); Log.v("aurora.jiangmx", "movey/lHeight: " +
	 * moveX/lHeight); Log.v("aurora.jiangmx", "deltX: " + deltX);
	 * Log.v("aurora.jiangmx", "deltY: " + deltY);
	 * 
	 * AnimationSet lSetAnimation = new AnimationSet(true);
	 * 
	 * TranslateAnimation lTranAnim = new TranslateAnimation(
	 * Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, moveX/lWidth,
	 * Animation.RELATIVE_TO_SELF,0, Animation.RELATIVE_TO_SELF, moveY/lHeight
	 * );
	 * 
	 * lTranAnim.setDuration(300);
	 * 
	 * ScaleAnimation lScaleAnim = new ScaleAnimation(1f, deltX, 1f, deltY,
	 * Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
	 * 
	 * lScaleAnim.setDuration(300);
	 * 
	 * // lSetAnimation.setFillAfter(true);
	 * lSetAnimation.addAnimation(lScaleAnim);
	 * lSetAnimation.addAnimation(lTranAnim);
	 * lSetAnimation.setAnimationListener(new AnimationListener() {
	 * 
	 * @Override public void onAnimationStart(Animation animation) { // TODO
	 * Auto-generated method stub
	 * 
	 * }
	 * 
	 * @Override public void onAnimationRepeat(Animation animation) { // TODO
	 * Auto-generated method stub
	 * 
	 * }
	 * 
	 * @Override public void onAnimationEnd(Animation animation) { // TODO
	 * Auto-generated method stub mCurDisplayImgV.setVisibility(View.GONE);
	 * mActivity.finish(); } }); mCurDisplayImgV.startAnimation(lSetAnimation);
	 * 
	 * }
	 */

	/**
	 * 
	 * @param activity
	 * @return > 0 success; <= 0 fail
	 */
	public static int getStatusHeight(Activity activity) {
		int statusHeight = 0;
		Rect localRect = new Rect();
		activity.getWindow().getDecorView()
				.getWindowVisibleDisplayFrame(localRect);
		statusHeight = localRect.top;
		if (0 == statusHeight) {
			Class<?> localClass;
			try {
				localClass = Class.forName("com.android.internal.R$dimen");
				Object localObject;
				localObject = localClass.newInstance();

				int i5 = Integer.parseInt(localClass
						.getField("status_bar_height").get(localObject)
						.toString());
				statusHeight = activity.getResources()
						.getDimensionPixelSize(i5);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (java.lang.InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return statusHeight;
	}
}
