package com.aurora.thememanager.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.aurora.change.imagecache.ImageResizer;
import com.aurora.change.imagecache.ImageWorker;
import com.aurora.change.model.PictureInfo;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.LogFactory;
import com.aurora.internet.RequestQueue;
import com.aurora.internet.cache.BitmapImageCache;
import com.aurora.internet.request.ImageRequest;
import com.aurora.internet.toolbox.ImageLoader;
import com.aurora.internetimage.NetworkImageView;
import com.aurora.thememanager.activity.Action;
import com.aurora.thememanager.cache.CacheManager;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.themeloader.ImageLoaderImpl;
import com.aurora.thememanager.widget.DownloadButton;
import com.aurora.thememanager.widget.NetworkRoundedImageView;
import com.aurora.thememanager.widget.ProgressBtn;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.aurora.thememanager.R;

public class WallpaperPreviewAdapter extends PagerAdapter {

	private static final String TAG = "TimeWallpaperPreviewAdapter";
	private ViewPager mViewPager;
	private Context mContext;
	private List<View> mViews;
	private LayoutInflater mInflater;
	private List<Theme> mThemeList;

	protected RequestQueue mQueue;


	protected ImageLoader mImageLoader;

	
	private CacheManager mCache;
	
	public WallpaperPreviewAdapter(Context context, List<Theme> themeList) {
		mContext = context;
		mThemeList = themeList;
		mCache = CacheManager.getInstance();
		mViews = initLayout(themeList);
		mQueue = RequestQueue.newRequestQueue(mContext, mCache.getThemeDiskCache());
		mImageLoader = new ImageLoaderImpl(mQueue, mCache.getBitmapCache(),
				mContext.getResources(), mContext.getAssets()){
			@Override
			public void makeRequest(ImageRequest request) {
				// TODO Auto-generated method stub
				super.makeRequest(request);
				request.setCacheExpireTime(TimeUnit.MINUTES, 1);
			}
		};
	}

	public WallpaperPreviewAdapter(Context context, String type,
			List<String> extraList) {
		mContext = context;
		mViews = initViews(extraList);
	}

	@Override
	public int getCount() {
		if (mViews != null) {
			return mViews.size();
		}
		return 0;
	}

	@Override
	public Object instantiateItem(View container, int position) {
		ViewPager viewPager = ((ViewPager) container);
		viewPager.addView(mViews.get(position));
		refreshData(position);
		return mViews.get(position);
	}

	public ImageLoader getImageLoader() {
		return mImageLoader;
	}

	public void stopQueue() {
		if (mQueue != null) {
			mQueue.stop();
		}
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		ViewPager viewPager = (ViewPager) container;
		NetworkImageView mImageView = (NetworkImageView) mViews.get(position)
				.findViewById(R.id.wallpaper_preview_item_background);
		if (mImageView != null) {
			mImageView.setImageDrawable(null);
		}
		viewPager.removeView(mViews.get(position));
	}

	public void setViewPager(ViewPager viewPager) {
		mViewPager = viewPager;
	}

	public List<View> initLayout(List<Theme> themeList) {
		int size = themeList.size();
		// log.d("initLayout:size=" + size);
		List<View> views = new ArrayList<View>();
		for (int i = 0; i < size; i++) {
			View view = View.inflate(mContext,
					R.layout.time_wallpaper_preview_item, null);
			views.add(view);
		}
		return views;
	}

	public List<View> initViews(List<String> list) {
		int size = 0;
		if (list != null) {
			size += list.size();
		}
		List<View> views = new ArrayList<View>();
		for (int i = 0; i < size; i++) {
			View view = View.inflate(mContext,
					R.layout.time_wallpaper_preview_item, null);
			views.add(view);
		}
		return views;
	}

	public void refreshData(int position) {
		// log.d("refresData");
		View view = mViews.get(position);
		NetworkImageView iv = (NetworkImageView) view
				.findViewById(R.id.wallpaper_preview_item_background);
		iv.setImageUrl(mThemeList.get(position).previews[0], getImageLoader());
	}

	class ViewHolder {
		ImageView mBackground;
		ImageView mForeground;
	}

	public void clearData() {
		// mImageLoader.clearCache();
		if (mViews != null) {
			mViews.clear();
			mViews = null;
		}
		stopQueue();

	}

	public void onPause() {
		
	}

	public void onResume() {
	}


}
