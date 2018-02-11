package com.aurora.thememanager.activity;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import aurora.view.ViewPager;
import aurora.view.ViewPager.OnPageChangeListener;

import com.aurora.internet.RequestQueue;
import com.aurora.internet.cache.BitmapImageCache;
import com.aurora.internet.cache.DiskCache;
import com.aurora.internet.cache.DiskCache.Entry;
import com.aurora.internet.toolbox.ImageLoader;
import com.aurora.internetimage.NetworkImageView;
import com.aurora.thememanager.R;
import com.aurora.thememanager.cache.CacheManager;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.themeloader.ImageLoaderImpl;

public class ThemePreviewsShowActivity extends BaseActivity implements OnPageChangeListener{
	
	private static final int MSG_LOAD_PREVIEWS = 0;

	private int mDefaultPosition;
	private String[] mPreviews;

	private ViewPager mViewPager;
	
	private List<NetworkImageView> mImages = new ArrayList<NetworkImageView>();
	
	
	private RequestQueue mQueue;
	private ImageLoader mInternetImageLoader;
	
	private PreviewPagerAdapter mAdapter;
	
	private CacheManager mCacheManager;
	
	private boolean mFromLocal = false;
	
	
	private Handler mHander  = new Handler(){
			public void handleMessage(android.os.Message msg) {
				if(msg.what == MSG_LOAD_PREVIEWS){
					loadPreviews();
				}
			};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mCacheManager = CacheManager.getInstance();
		Intent intent = getIntent();
		if(intent != null){
			mPreviews = intent.getStringArrayExtra(Action.KEY_SHOW_PREIVEW_PICTURE_URL);
			mFromLocal = intent.getBooleanExtra(ThemeConfig.KEY_FOR_APPLY_FROM_LOACAL, false);
			mDefaultPosition = intent.getIntExtra(Action.KEY_SHOW_PREIVEW_PICTURE_INDEX, 0);
		}
		mQueue = RequestQueue.newRequestQueue(this, mCacheManager.getPreviewDiskCache());
		mInternetImageLoader = new ImageLoaderImpl(mQueue, new BitmapImageCache(mCacheManager.getCacheSize()), getResources(), getAssets());
		
		
		setContentView(R.layout.activity_show_previews_pager);
		mViewPager = (ViewPager)findViewById(R.id.previews_pager);
		mAdapter = new PreviewPagerAdapter();
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setOnPageChangeListener(this);
		mHander.sendEmptyMessage(MSG_LOAD_PREVIEWS);
		
	}
	
	private void loadPreviews() {
		if(mPreviews != null && mPreviews.length > 0){
			int count = mPreviews.length;
			
		for(int i = 0 ;i< count;i++){
			NetworkImageView image = new NetworkImageView(this);
			if(mFromLocal){
				image.setImageUrl(ImageLoaderImpl.RES_SDCARD+mPreviews[i], mInternetImageLoader);
			}else{
				image.setImageUrl(mPreviews[i], mInternetImageLoader);
			}
			
			image.setScaleType(ScaleType.FIT_XY);
			mImages.add(image);
			mAdapter.notifyDataSetChanged();
		}
		}
		mViewPager.setCurrentItem(mDefaultPosition);
	}
	
	
	class PreviewPagerAdapter extends aurora.view.PagerAdapter {

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {

			return arg0 == arg1;
		}

		@Override
		public int getCount() {

			return mImages.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position,
				Object object) {
			container.removeView(mImages.get(position));

		}

		@Override
		public int getItemPosition(Object object) {

			return super.getItemPosition(object);
		}


		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mImages.get(position),getWidth(), getHeight());
			return mImages.get(position);
		}

	}



	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		
	};
	
	
	
	
	
	
	

}
