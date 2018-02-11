package com.aurora.thememanager.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.aurora.internet.RequestQueue;
import com.aurora.internet.cache.BitmapImageCache;
import com.aurora.internet.toolbox.ImageLoader;
import com.aurora.thememanager.cache.CacheManager;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.themeloader.ImageLoaderImpl;
import com.aurora.thememanager.utils.themeloader.PictureLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public abstract class AbsThemeAdapter extends BaseAdapter {

	protected int mItemLayoutId;
	protected Context mContext;
	protected LayoutInflater mInflater;
	
	
	protected List<Theme> mDatas = new ArrayList<Theme>();
	
	
	protected RequestQueue mQueue;
	
	protected final int mImageCacheSize = ThemeConfig.HttpConfig.DISKCACHE_SIZE;
	
	
	protected ImageLoader mImageLoader;
	
	private CacheManager mCacheManager;
	
	public AbsThemeAdapter(Context context){
		mContext = context;
		mCacheManager = CacheManager.getInstance();
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mQueue = RequestQueue.newRequestQueue(mContext, mCacheManager.getThemeDiskCache());
		mImageLoader = new ImageLoaderImpl(mQueue, mCacheManager.getBitmapCache(), mContext.getResources(), mContext.getAssets()){
			@Override
			public void makeRequest(
					com.aurora.internet.request.ImageRequest request) {
				// TODO Auto-generated method stub
				super.makeRequest(request);
				request.setCacheExpireTime(TimeUnit.MINUTES, 30);
			}
		};
	}
	
	public ImageLoader getImageLoader(){
		return mImageLoader;
	}
	
	public void stopQueue(){
		if(mQueue != null){
			mQueue.stop();
		}
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDatas.size();
	}

	public Theme getTheme(int index){
		return mDatas.get(index);
	}
	@Override
	public Theme getItem(int index) {
		// TODO Auto-generated method stub
		return mDatas.get(index);
	}

	@Override
	public long getItemId(int itemId) {
		// TODO Auto-generated method stub
		return itemId;
	}

	public void setItemLayoutId(int layoutId){
		mItemLayoutId = layoutId;
	}
	
	public  void addData(Theme data){
		
		if(data == null){
			return;
		}
		synchronized (mDatas) {
			//if(!mDatas.contains(data)){
				mDatas.add(data);
				updateData();
			//}
		}
		
		
	}
	
	public  void deleteData(Theme data){
		if(data == null){
			return;
		}
		synchronized (mDatas) {
			if(mDatas.contains(data)){
				mDatas.remove(data);
				updateData();
			}
		}
		
	}
	
	
	public abstract void updateData();
	
	
	
	

}
