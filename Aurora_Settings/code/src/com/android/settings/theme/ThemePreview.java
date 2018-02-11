package com.android.settings.theme;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import android.os.Handler;
import android.view.View.OnTouchListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;

public class ThemePreview extends AuroraActivity implements
		OnPageChangeListener {
	public static final String TAG = "ThemeSettings";
	
	private AuroraActionBar mAuroraActionBar;
	private ViewPager mViewPager;
	private static final int ACTION_BTN_DONE = 1;

	private int[] imgIdArray;
	private ImageView[] mImageViews;

	public static final int SHOW_ACTION = 0;
	public static final int HIDE_ACTION = 1;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case SHOW_ACTION:
				mAuroraActionBar.setVisibility(View.VISIBLE);
				mHandler.sendEmptyMessageDelayed(HIDE_ACTION, 3000);
				break;
			case HIDE_ACTION:
				if(mAuroraActionBar != null){
					mAuroraActionBar.setVisibility(View.GONE);
				}
				break;
			}
			
		}
		
		
	};
	
	OnTouchListener mTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			mHandler.removeMessages(HIDE_ACTION);
			mHandler.removeMessages(SHOW_ACTION);
			
			mHandler.sendEmptyMessage(SHOW_ACTION);
			return false;
		}
	};

	private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case ACTION_BTN_DONE:

				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setAuroraPicContentView(R.layout.theme_preview);
		
		Intent intent = getIntent();
		
		String type = intent.getStringExtra("theme_tpye");
		Log.v(TAG, "----onCreate---------type-------===="+type);
		
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		imgIdArray = new int[] { R.drawable.one, R.drawable.two };
		mImageViews = new ImageView[imgIdArray.length];
		for (int i = 0; i < imgIdArray.length; i++) {
			ImageView imageView = new ImageView(this);
			mImageViews[i] = imageView;
			imageView.setBackgroundResource(imgIdArray[i]);
		}
		mViewPager.setAdapter(new MyAdapter());
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setOnTouchListener(mTouchListener);

		initAuroraActionBar();
	}

	private void initAuroraActionBar() {
		mAuroraActionBar = getAuroraActionBar();
		mAuroraActionBar.setDisplayHomeAsUpEnabled(true);
		mAuroraActionBar.getBackground().setAlpha(140);
		updateAuroraActionBarTitle(1);
		if (true) {
			//mAuroraActionBar.addItem(R.drawable.aurora_action_btn_done,
				//	ACTION_BTN_DONE, "action_done_btn");
			mAuroraActionBar
					.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
		}
		mHandler.sendEmptyMessageDelayed(HIDE_ACTION, 3000);
		
	}

	private void updateAuroraActionBarTitle(int currentPos) {
		String title = "" + currentPos + "/" + mImageViews.length;
		mAuroraActionBar.setTitle(title);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mHandler.sendEmptyMessage(HIDE_ACTION);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mHandler.removeMessages(HIDE_ACTION);
		mHandler.removeMessages(SHOW_ACTION);
		mHandler.sendEmptyMessage(SHOW_ACTION);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		updateAuroraActionBarTitle(position + 1);
	}

	public class MyAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return imgIdArray.length;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(View container, int position) {
			// TODO Auto-generated method stub
			try {
				((ViewPager) container).addView(mImageViews[position
						% mImageViews.length], 0);
			} catch (Exception e) {
				// handler something
			}
			return mImageViews[position % mImageViews.length];
		}

	}

}