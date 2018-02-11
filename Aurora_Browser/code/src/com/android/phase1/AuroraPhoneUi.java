/**
 * Vulcan created this file in 2015年3月31日 上午10:49:26 .
 */
package com.android.phase1;

import java.util.List;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.android.browser.BrowserActivity;
import com.android.browser.NavTabView;
import com.android.browser.PhoneUi;
import com.android.browser.R;
import com.android.browser.Tab;
import com.android.browser.UiController;
import com.android.phase1.activity.CinemaBoxPage;
import com.android.phase1.view.AuroraNavTabView;
import com.android.phase1.view.CinemaBox;
import com.android.phase1.view.CinemaBox.CinemaItem;
import com.android.phase1.view.CinemaBox.CinemaListener;
import com.android.phase1.view.StackedNavScreen;
import com.android.phase1.view.stackedscroller.StackView;

/**
 * Vulcan created AuroraPhoneUi in 2015年3月31日 .
 * 
 */
public class AuroraPhoneUi extends PhoneUi {

	/**
	 * @param browser
	 * @param controller
	 */
	public AuroraPhoneUi(Activity browser, UiController controller) {
		super(browser, controller);
	}
	/**
	 * 
	 * Vulcan created this method in 2015年3月31日 下午5:29:58 .
	 * @param v
	 * @return
	 */
	@SuppressWarnings("unused")
	private View getAncestorStackView(View v) {
		View current = v;
		while(current != null) {
			if(current instanceof StackView) {
				return current;
			}
			
			if(!(current.getParent() instanceof View)) {
				throw new RuntimeException("non-view type if found!!!");
			}
			
			current = (View)current.getParent();
		}
		throw new RuntimeException("StackView is not found!!!");
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月22日 下午3:15:17 .
	 * @param position
	 * @param animate
	 * @param callParent
	 */
	public void hideNavScreen(int position, boolean animate, boolean callParent) {
		if(callParent) {
			super.hideNavScreen(position, animate);
		}
		else {
			hideNavScreen(position,animate);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.android.browser.PhoneUi#hideNavScreen(int, boolean)
	 */
	public void hideNavScreen(int position, boolean animate) {
		
//		onHideNavScreen(position);
		
		mShowNav = false;
        if (!showingNavScreen()) return;
        
        final Tab tab = mUiController.getTabControl().getTab(position);
        if ((tab == null) || !animate) {
            if (tab != null) {
                setActiveTab(tab);
            } else if (mTabControl.getTabCount() > 0) {
                // use a fallback tab
                setActiveTab(mTabControl.getCurrentTab());
            }
            mContentView.setVisibility(View.VISIBLE);
            finishAnimateOut();
            return;
        }
        NavTabView tabview = (NavTabView) mNavScreen.getTabView(position);
        if (tabview == null) {
            if (mTabControl.getTabCount() > 0) {
                // use a fallback tab
                setActiveTab(mTabControl.getCurrentTab());
            }
            mContentView.setVisibility(View.VISIBLE);
            finishAnimateOut();
            return;
        }
        
        mUiController.setBlockEvents(true);
        mUiController.setActiveTab(tab);
        mContentView.setVisibility(View.VISIBLE);
        
		Rect r = new Rect();
		getActiveTab().getWebView().getGlobalVisibleRect(r);
        final CinemaBox cb = new CinemaBoxPage();
        
		StackedNavScreen sns = (StackedNavScreen)mNavScreen;
		List<View> tabList = sns.getNavTabViews();
        
		View tibleBar;
		View mainWin;
		Log.d(LOGTAG,"hideNavScreen: >>>>>>>>>>>");
		for(View v: tabList) {
			CinemaItem ci = new CinemaItem();
			tibleBar = v.findViewById(R.id.titlebar);
			tibleBar.getGlobalVisibleRect(ci.mTitleBarRect);
			Log.d(LOGTAG,"hideNavScreen: ci.mTitleBarRect = " + ci.mTitleBarRect);
			mainWin = v.findViewById(R.id.tab_view);
			mainWin.getGlobalVisibleRect(ci.mMainWinRect);
			Log.d(LOGTAG,"hideNavScreen: ci.mMainWinRect = " + ci.mMainWinRect);
			
			if(v instanceof AuroraNavTabView) {
				ci.mAuroraNavTabView = (AuroraNavTabView)v;
				Tab t1 = mUiController.getTabControl().getTab(position);
				Tab t2 = sns.getTabByNavTabView(v);
				if(t1 == t2) {
					ci.mIsSelected = true;
				}

			}
			cb.addItem(ci);
		}
		Log.d(LOGTAG,"hideNavScreen: <<<<<<<<<<<<");
		
		CinemaListener cl = new CinemaListener() {
			
			@Override
			public void onCinemaEnd() {
				cb.dettachFromViewGroup(mCustomViewContainer);
		        finishAnimateOut();
		        mUiController.setBlockEvents(false);
			}

			@Override
			public void onCinemaStart() {
				
				Handler h = new Handler();
				h.post(new Runnable() {

					@Override
					public void run() {
						cb.attachToViewGroup(mCustomViewContainer);
				        mNavScreen.finishScroller();
						mNavScreen.setVisibility(View.GONE);
						mCustomViewContainer.invalidate();
					}
					
				});
				

			}
		};
		

		
/*		Point pos = new Point();
		pos.x = 0;
		pos.y = 0;
		cb.setTitleBarBmp(getBitmapOfTitleBarNStatusBar(), pos);*/

		cb.setTitleBarBmp(getViewBitmap(mFixedTitlebarContainer),
				getViewPosOnScreen(mFixedTitlebarContainer));
		cb.setToolBarBmp(getViewBitmap(toolBarContainer),
				getViewPosOnScreen(toolBarContainer));
		cb.setBoxSize(mCustomViewContainer.getWidth(), mCustomViewContainer.getHeight());
		cb.setWebviewTopBottom(getWebViewTop(),getWebViewBottom());
		cb.setCinemaListener(cl);
		cb.playAnim(getActivity());

		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月21日 下午5:56:48 .
	 * @return
	 */
	private Bitmap getBitmapOfTitleBarNStatusBar() {
		FrameLayout fl = (FrameLayout) mActivity.getWindow()
        .getDecorView().findViewById(android.R.id.content);
		
		Rect titleBarRect = new Rect();
		mFixedTitlebarContainer.getGlobalVisibleRect(titleBarRect);
		int w = titleBarRect.width();
		int h = titleBarRect.height() + getViewPosOnScreen(mFixedTitlebarContainer).y;
        
        
        
        
        
        fl.clearFocus();
        fl.setPressed(false);

        boolean willNotCache = fl.willNotCacheDrawing();
        fl.setWillNotCacheDrawing(false);

        int color = fl.getDrawingCacheBackgroundColor();
        fl.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
        	fl.destroyDrawingCache();
        }
        fl.buildDrawingCache();
        Bitmap cacheBitmap = fl.getDrawingCache();
        if (cacheBitmap == null) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap,0,0,w,h);

        fl.destroyDrawingCache();
        fl.setWillNotCacheDrawing(willNotCache);
        fl.setDrawingCacheBackgroundColor(color);

        return bitmap;

	}
	
	/* (non-Javadoc)
	 * @see com.android.browser.PhoneUi#finishAnimateOut()
	 */
	@Override
	protected void finishAnimateOut() {
        mTabControl.setOnThumbnailUpdatedListener(null);
        mNavScreen.setVisibility(View.GONE);
        mCustomViewContainer.setAlpha(1f);
        mCustomViewContainer.setVisibility(View.GONE);
        return;
	}
	/**
	 * 
	 * Vulcan created this method in 2015年4月11日 下午4:06:41 .
	 * @param v
	 * @return
	 */
	private Point getViewPosOnScreen(View v) {
		Point titleBarPos = new Point();
		Rect r = new Rect();
		v.getGlobalVisibleRect(r);
		titleBarPos.x = r.left;
		titleBarPos.y = r.top;
		return titleBarPos;
	}
	
    private Bitmap safeCreateBitmap(int width, int height) {
        if (width <= 0 || height <= 0) {
            Log.w(LOGTAG, "safeCreateBitmap failed! width: " + width
                    + ", height: " + height);
            return null;
        }
        return Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    }
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月21日 下午4:38:35 .
	 * @param view
	 * @return
	 */
	private Bitmap getViewBitmap(View view) {
		Rect viewRect = new Rect();
		view.getGlobalVisibleRect(viewRect);
		int w = viewRect.width();
		int h = viewRect.height();
		
		Bitmap bmp = safeCreateBitmap(w,h);
        Canvas c = new Canvas(bmp);
        view.draw(c);
        c.setBitmap(null);
		return bmp;
	}
	
	/**
	 * NavScreen隐藏前调用此方法
	 * Vulcan created this method in 2015年3月28日 下午3:28:31 .
	 * @param position
	 */
	protected void onHideNavScreen(int position) {
		
		//mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		
		changeStatusBar((BrowserActivity)getActivity(),true);
		
		Tab tab = mUiController.getTabControl().getTab(position);
		if(tab != null) {
			tab.setShowError(tab.getWebView());
		}
	}

	/* (non-Javadoc)
	 * @see com.android.browser.BaseUi#onShowTitleBar()
	 */
	@Override
	public void onShowTitleBar() {
		mUrlBarAutoShowManager.onAskTitleBar();
		super.onShowTitleBar();
	}

	

	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 上午11:51:54 .
	 * @return
	 */
	private int getWebViewTop() {
		Rect r = new Rect();
		getActiveTab().getWebView().getGlobalVisibleRect(r);
		return r.top;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月10日 上午11:52:17 .
	 * @return
	 */
	private int getWebViewBottom() {
		Rect r = new Rect();
		getActiveTab().getWebView().getGlobalVisibleRect(r);
		return r.bottom;
	}

}
