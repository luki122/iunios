package com.aurora.change;

import java.util.ArrayList;

import com.aurora.change.data.AdapterDataFactory;
import com.aurora.change.model.NextDayPictureInfo;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.ImageLoaderHelper;

import android.R.integer;
import android.app.Application;
import android.util.DisplayMetrics;
import android.util.Log;

// Aurora liugj 2014-09-24 created for bug-8151
public class AuroraChangeApp extends Application{
	//shigq add start
	ImageLoaderHelper mImageLoaderHelper;
	private ArrayList<NextDayPictureInfo> mPictureList = new ArrayList<NextDayPictureInfo>();
	private int width;
	private int height;
	private boolean isMobileData = false;
	//shigq add end
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("Wallpaper_DEBUG", "AuroraChangeApp-------onCreat ");
		initWallpaperData();
		
		//shigq add start
		initImageLoaderConfiguration();
		//shigq add end
	}
		
	private void initWallpaperData() {
		AdapterDataFactory dataFactory = new AdapterDataFactory(getApplicationContext(), Consts.WALLPAPER_LOCKSCREEN_TYPE);
        dataFactory.initWallpaperItems();
        dataFactory.clearData();
        dataFactory = null;
    }
	
	//shigq add start
	private void initImageLoaderConfiguration() {
		mImageLoaderHelper = new ImageLoaderHelper(getApplicationContext());
		final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        Log.d("Wallpaper_DEBUG", "AuroraChangeApp-------initImageLoaderConfiguration---width = "+width+" height = "+height);
		mImageLoaderHelper.setupCustomizedConfiguration(width, height, Consts.NEXTDAY_WALLPAPER_TEMP);
	}
	
	public ImageLoaderHelper getImageLoaderHelper() {
		if (mImageLoaderHelper == null) {
			mImageLoaderHelper = new ImageLoaderHelper(this);
		}
		return mImageLoaderHelper;
	}
	
	public ArrayList<NextDayPictureInfo> getNextDayPictureInfoList() {
		return mPictureList;
	}
	
	public int getDisplayWidth() {
		return width;
	}
	
	public int getDisplayHeight() {
		return height;
	}
	
	public void setIsMobileData(boolean b) {
		isMobileData = b;
	}
	
	public boolean getIsMobileData() {
		return isMobileData;
	}
	//shigq add end
	
}
