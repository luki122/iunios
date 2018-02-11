/**
 * Vulcan created this file in 2015年3月16日 下午6:03:36 .
 */
package com.android.phase1.cinema;

import android.content.Context;
import android.view.View;
import android.webkit.WebView;

/**
 * Vulcan created CinemaMan in 2015年3月16日 .
 * 管理不同的Cinema，提供查询接口查询指定类型动画的实例
 * 
 */
public class CinemaMan {
	public static final int CINEMA_TYPE_WEBVIEW = 0;
	public static final int CINEMA_TYPE_INIT_BROWSE = 1;
	public static final int CINEMA_TYPE_BROWSE_END = 2;
	public static final int CINEMA_TYPE_END_REPLAY = 3;
	public static final int CINEMA_TYPE_REPLAY_INIT = 4;
	public static final int CINEMA_TYPE_BROWSE_REPLAY = 5;
	public static final int CINEMA_TYPE_REPLAY_BROWSE = 6;
	public static final int CINEMA_TYPE_BROWSE_INIT = 7;
	
	
	
	
	public interface CinemaListener {
		public void onCinemaEnd(Cinema cinema);
	}

	/**
	 * 
	 */
	public CinemaMan(Context context, View web, View titlebar, View toolbar) {

		//mWebview = web;

		mWebCinema = new WebViewCinema(context, web, titlebar, toolbar);
		mIBCinema = new InitBrowCinema(context, web, titlebar, toolbar);
		mBECinema = new BrowEndCinema(context, web, titlebar, toolbar);
		mERCinema = new EndRepCinema(context, web, titlebar, toolbar);
		mRICinema = new RepInitCinema(context, web, titlebar, toolbar);
		mBRCinema = new BrowRepCinema(context, web, titlebar, toolbar);
		mRBCinema=new RepBrowCinema(context, web, titlebar, toolbar);
		mBICinema=new BrowInitCinema(context, web, titlebar, toolbar);
		mCinemaList = new Cinema[] { mWebCinema, mIBCinema, mBECinema,
				mERCinema, mRICinema,mBRCinema ,mRBCinema,mBICinema};

		for (Cinema c : mCinemaList) {
			c.create(context);
		}

	}

	/**
	 * 
	 * Vulcan created this method in 2015年3月17日 下午2:51:54 .
	 * 
	 * @param cinemaType
	 * @param progress
	 */
	public void setCinemaProgress(int cinemaType, float progress) {
		mCinemaList[cinemaType].setProgress(progress);
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月23日 下午4:30:32 .
	 * @param cinemaType
	 * @return
	 */
	public float getCinemaProgress(int cinemaType) {
		return mCinemaList[cinemaType].getProgress();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月19日 下午2:33:13 .
	 * @param cinemaType
	 * @param progress
	 */
	public void changeCinemaProgress(int cinemaType, float progress) {
		mCinemaList[cinemaType].changeProgress(progress);
	}

	/**
	 * start specified cinema right now from current position Vulcan created
	 * this method in 2015年3月17日 下午3:10:47 .
	 * 
	 * @param cinemaType
	 */
	public void startCinema(int cinemaType, CinemaListener cinemaListener) {
	
		//mCinemaList[cinemaType].setProgress(0f);
		mCinemaList[cinemaType].start(cinemaListener);
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月19日 下午2:24:25 .
	 * @param cinemaType
	 * @return
	 */
	public  boolean isProgressFull(int cinemaType) {
		return mCinemaList[cinemaType].progressIsEnd();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月19日 下午2:25:52 .
	 * @param cinemaType
	 * @return
	 */
	public boolean isProgressZero(int cinemaType) {
		return mCinemaList[cinemaType].progressIsStart();
	}

	private final WebViewCinema mWebCinema;
	private final InitBrowCinema mIBCinema;

	private final BrowEndCinema mBECinema;
	private final EndRepCinema mERCinema;
	private final RepInitCinema mRICinema;
	private final BrowRepCinema mBRCinema;
	private final RepBrowCinema mRBCinema;
	private final BrowInitCinema mBICinema;

	/**
	 * 
	 * Vulcan created this method in 2015年3月17日 下午4:13:53 .
	 * 
	 * @param cinemaType
	 * @param reverse
	 *            decide if we hope to play reversely
	 * @param cinemaListener
	 */
	public void startCinema(int cinemaType, boolean reverse,
			CinemaListener cinemaListener) {
		return;
	}

	/**
	 * 
	 * Vulcan created this method in 2015年3月18日 下午6:14:01 .
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public boolean webviewAtBottom(WebView mWebview) {

		float webViewContentHeight = mWebview.getContentHeight()
				* mWebview.getScale();

		float webViewCurrentHeight = (mWebview.getHeight() + mWebview
				.getScrollY());

		if (Math.abs(webViewContentHeight - webViewCurrentHeight) <= 4f) {
			return true;

		} else {
			return false;
		}

	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月20日 下午2:04:59 .
	 * @param webview
	 * @return
	 */
	public boolean webviewAtTop(WebView webview) {
		if(webview.getScrollY()<=4){
			return true;
			
		}else{
			return false;	
		}
		
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月19日 下午2:47:32 .
	 * @return
	 */
	public boolean isAnyPlaying() {
		if(Cinema.animatorNumb>=1){
			return true;	
		}
		return false;
	}

	private Cinema[] mCinemaList;
	//private final View mWebview;

}
