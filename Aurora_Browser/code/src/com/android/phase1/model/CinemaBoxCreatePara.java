/**
 * Vulcan created this file in 2015年4月10日 上午10:34:26 .
 */
package com.android.phase1.model;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.ViewGroup;

import com.android.phase1.view.CinemaBox;
import com.android.phase1.view.CinemaBox.CinemaItem;
import com.android.phase1.view.CinemaBox.CinemaListener;

/**
 * Vulcan created CinemaBoxCreatePara in 2015年4月10日 .
 * 
 */
public class CinemaBoxCreatePara {
	
	private static final int INVALID_PARA = -1;

	/**
	 * 
	 */
	private CinemaBoxCreatePara() {
	}

	private Activity mMainActivity = null;
	private ViewGroup mRootView = null;
	private Bitmap mTitleBar = null;
	private Point mTitleBarPos = null;
	private Bitmap mToolBar = null;
	private Point mToolBarPos = null;
	private int mWidth = INVALID_PARA;
	private int mHeight = INVALID_PARA;
	private int mWebTop = INVALID_PARA;
	private int mWebBottom = INVALID_PARA;
	private final List<CinemaItem> mCinemaItemList = new ArrayList<CinemaItem>();
	private CinemaBox mHostCinemaBox;
	private CinemaListener mCinemaListener;
	
	private Object mExtra = null;;
	
	
	/**
	 * @return the mWidth
	 */
	public int getWidth() {
		return mWidth;
	}

	/**
	 * @param mWidth the mWidth to set
	 */
	public void setWidth(int mWidth) {
		this.mWidth = mWidth;
	}

	/**
	 * @return the mHeight
	 */
	public int getHeight() {
		return mHeight;
	}

	/**
	 * @param mHeight the mHeight to set
	 */
	public void setHeight(int mHeight) {
		this.mHeight = mHeight;
	}

	/**
	 * @return the mWebTop
	 */
	public int getWebTop() {
		return mWebTop;
	}

	/**
	 * @param mWebTop the mWebTop to set
	 */
	public void setWebTop(int mWebTop) {
		this.mWebTop = mWebTop;
	}

	/**
	 * @return the mWebBottom
	 */
	public int getWebBottom() {
		return mWebBottom;
	}

	/**
	 * @param mWebBottom the mWebBottom to set
	 */
	public void setWebBottom(int mWebBottom) {
		this.mWebBottom = mWebBottom;
	}

	/**
	 * @return the mCinemaItemList
	 */
	public List<CinemaItem> getCinemaItemList() {
		return mCinemaItemList;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 上午10:41:55 .
	 * @param l
	 */
	public void setCinemaItemList(List<CinemaItem> l) {
		mCinemaItemList.clear();
		mCinemaItemList.addAll(l);
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 上午11:39:30 .
	 * @param ci
	 */
	public void addCinemaItem(CinemaItem ci) {
		mCinemaItemList.add(ci);
		return;
	}

	public static CinemaBoxCreatePara getInstance() {
		if(sInstance == null) {
			sInstance = new CinemaBoxCreatePara();
		}
		return sInstance;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 上午11:15:39 .
	 * @return
	 */
	public CinemaBox getHostCinemaBox() {
		return mHostCinemaBox;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月11日 上午11:51:42 .
	 */
	public void initAnim() {
		mHostCinemaBox.initAnim(mMainActivity,
				mWidth, 
				mHeight,
				mCinemaListener,
				mRootView);
	}
	
	public void performAnim() {
		mHostCinemaBox.performAnim();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 下午12:08:01 .
	 * @param root
	 */
	public void setRootView(ViewGroup root) {
		mRootView = root;
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 下午12:08:31 .
	 * @param ctx
	 */
	public void setMainActivity(Activity a) {
		mMainActivity = a;
	}
	
	public Activity getMainActivity() {
		return mMainActivity;
	}
	
	
	
	public CinemaListener getCinemaListener() {
		return mCinemaListener;
	}

	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 下午2:22:15 .
	 * @param cl
	 */
	public void setCinemaListener(CinemaListener cl) {
		mCinemaListener = cl;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 下午1:47:15 .
	 * @param cb
	 */
	public void setHostCinemaBox(CinemaBox cb) {
		mHostCinemaBox = cb;
	}

	public Object getExtra() {
		return mExtra;
	}

	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 下午2:29:53 .
	 * @param mExtra
	 */
	public void setExtra(Object mExtra) {
		this.mExtra = mExtra;
	}

	public Bitmap getTitleBarBmp() {
		return mTitleBar;
	}

	public void setTitleBarBmp(Bitmap mTitleBar, Point pos) {
		this.mTitleBar = mTitleBar;
		this.mTitleBarPos = pos;
	}
	
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月11日 下午4:09:34 .
	 * @return
	 */
	public Point getTitleBarPos() {
		return mTitleBarPos;
	}

	public Bitmap getToolBarBmp() {
		return mToolBar;
	}

	public void setToolBarBmp(Bitmap mToolBar, Point pos) {
		this.mToolBar = mToolBar;
		this.mToolBarPos = pos;
	}

	/**
	 * @return the mToolBarPos
	 */
	public Point getToolBarPos() {
		return mToolBarPos;
	}

	/**
	 * @param mToolBarPos the mToolBarPos to set
	 */
	public void setToolBarPos(Point mToolBarPos) {
		this.mToolBarPos = mToolBarPos;
	}

	private static CinemaBoxCreatePara sInstance = null;

}
