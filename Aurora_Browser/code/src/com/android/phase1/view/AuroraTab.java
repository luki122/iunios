/**
 * Vulcan created this file in 2015年3月30日 上午9:59:12 .
 */
package com.android.phase1.view;

import android.graphics.Canvas;
import android.graphics.Picture;
import android.os.Bundle;
import android.webkit.WebView;

import com.android.browser.R;
import com.android.browser.Tab;
import com.android.browser.WebViewController;

/**
 * Vulcan created AuroraTab in 2015年3月30日 .
 * 
 */
public class AuroraTab extends Tab {
	public final static String TAG = "AuroraTab";

	/**
	 * @param wvcontroller
	 * @param w
	 */
	public AuroraTab(WebViewController wvcontroller, WebView w) {
		super(wvcontroller, w);
	}

	/**
	 * @param wvcontroller
	 * @param state
	 */
	public AuroraTab(WebViewController wvcontroller, Bundle state) {
		super(wvcontroller, state);
	}

	/**
	 * @param wvcontroller
	 * @param w
	 * @param state
	 */
	public AuroraTab(WebViewController wvcontroller, WebView w, Bundle state) {
		super(wvcontroller, w, state);
	}

	/* (non-Javadoc)
	 * @see com.android.browser.Tab#getCaptureWidth()
	 */
	@Override
	public int getCaptureWidth() {
    	return mContext.getResources().getDimensionPixelSize(
                R.dimen.aurora_tab_thumbnail_width);
	}

	/* (non-Javadoc)
	 * @see com.android.browser.Tab#getCaptureHeight()
	 */
	@Override
	public int getCaptureHeight() {
    	return mContext.getResources().getDimensionPixelSize(
                R.dimen.aurora_tab_thumbnail_height);
	}

	/* (non-Javadoc)
	 * @see com.android.browser.Tab#capture()
	 */
	@Override
	protected void capture() {
		// TODO Auto-generated method stub
		super.capture();
/*		Canvas c = new Canvas(mCapture);
		c.drawRGB(255, 255, 255);
		c.setBitmap(null);*/
	}

	/* (non-Javadoc)
	 * @see com.android.browser.Tab#onNewPicture(android.webkit.WebView, android.graphics.Picture)
	 */
	@Override
	public void onNewPicture(WebView view, Picture picture) {
		//控制多窗口标签是否实时刷新和保存图片
		//postCapture();
	}
	

	


}
