/**
 * Vulcan created this file in 2015年4月9日 下午3:54:53 .
 */
package com.android.phase1.activity;

import com.android.browser.BrowserActivity;
import com.android.browser.R;
import com.android.phase1.model.CinemaBoxCreatePara;
import com.android.phase1.view.CinemaBox;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import aurora.app.AuroraActivity;

/**
 * Vulcan created CinemaBoxPage in 2015年4月9日 .
 * 
 */
public class CinemaBoxPage extends CinemaBox {
	
	
	
	public static class BoxActivity extends AuroraActivity {
		
/*		public static final String LAUNCH_PARA_SCREEN_WIDTH = "screenWidth";
		public static final String LAUNCH_PARA_SCREEN_HEIGHT = "screenHeight";
		public static final String LAUNCH_PARA_WEB_TOP = "webTop";
		public static final String LAUNCH_PARA_WEB_BOTTOM = "webBottom";
		public static final String LAUNCH_PARA_CINEMA_ITEMS = "cinemaItems";*/
		
		/**
		 * 
		 */
		public BoxActivity() {
			super();
		}

		/* (non-Javadoc)
		 * @see aurora.app.AuroraActivity#onCreate(android.os.Bundle)
		 */
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.aurora_cinema_box_page);
			mRootView = (ViewGroup)findViewById(R.id.aurora_cinema_box_page_root);
			mCinemaBoxCreatePara.setRootView(mRootView);
			mCinemaBoxCreatePara.initAnim();
			
			mCinemaBoxCreatePara.setExtra(this);

		}
		

		
		/* (non-Javadoc)
		 * @see aurora.app.AuroraActivity#onResume()
		 */
		@Override
		protected void onResume() {
			super.onResume();
			mCinemaBoxCreatePara.performAnim();
		}



		public ViewGroup mRootView;
		
		private CinemaBoxCreatePara mCinemaBoxCreatePara = CinemaBoxCreatePara.getInstance();
		
	}
	
	
	

	public CinemaBoxPage() {
		mCinemaBoxCreatePara.getCinemaItemList().clear();
	}




	@Override
	public void playAnim(Activity a) {
		mCinemaBoxCreatePara.setHostCinemaBox(this);
		mCinemaBoxCreatePara.setMainActivity(a);
		Intent intent = new Intent(a, BoxActivity.class);
		//a.overridePendingTransition(0, 0);
		a.startActivity(intent);
	}




	@Override
	public void attachToViewGroup(ViewGroup vg) {
	}




	@Override
	public void dettachFromViewGroup(ViewGroup vg) {
		final BoxActivity ba;
		Object o = mCinemaBoxCreatePara.getExtra();
		if(o instanceof BoxActivity) {
			ba = (BoxActivity)o;
			//ba.overridePendingTransition(0, 0);
			//ba.finish();
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					startMainActivity(ba);
					
				}
			}, 80);
			//ba.overridePendingTransition(R.anim.cinema_box_in, R.anim.cinema_box_out);
		}
		
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月11日 下午12:06:06 .
	 */
	private void startMainActivity(Activity a) {
		Intent intent = new Intent(a,BrowserActivity.class);
		a.startActivity(intent);
		a.overridePendingTransition(R.anim.cinema_box_in, R.anim.cinema_box_out);
//		a.overridePendingTransition(0,0);
		
		Activity mainActivity = mCinemaBoxCreatePara.getMainActivity();
		mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		return;
	}


	private Handler mHandler = new Handler();
}
