package com.aurora.thememanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.aurora.change.data.AdapterDataFactory;
import com.aurora.change.model.NextDayPictureInfo;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.ImageLoaderHelper;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.FileUtils;
import com.aurora.thememanager.utils.download.AutoUpdateService;
import com.aurora.thememanager.utils.download.DownloadService;
import com.aurora.thememanager.utils.download.RingtongDownloadService;
import com.aurora.thememanager.utils.download.TimeWallpaperDownloadService;
import com.aurora.thememanager.utils.download.WallpaperDownloadService;
import com.aurora.thememanager.utils.themehelper.ThemeAllHelper;
import com.aurora.thememanager.utils.themehelper.ThemeFontsHelper;
import com.aurora.thememanager.utils.themehelper.ThemeRingtongHelper;
import com.aurora.thememanager.utils.themehelper.ThemeTimeWallpaperHelper;
import com.aurora.thememanager.utils.themehelper.ThemeWallPaperHelper;
import com.aurora.thememanager.utils.themeloader.PictureLoader;
import com.aurora.utils.Utils2Icon;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

public class ThemeManagerApplication extends Application {

	public static HashMap<String, String> mThemeHelperMap = new HashMap<String, String>();

	private List<Activity> mActivities = new ArrayList<Activity>();
	
	static {
		mThemeHelperMap.put(ThemeConfig.THEME_ALL, ThemeAllHelper.class.getName());
		mThemeHelperMap.put(ThemeConfig.THEME_WALLPAPER,
				ThemeWallPaperHelper.class.getName());
		mThemeHelperMap.put(ThemeConfig.THEME_FONTS,
				ThemeFontsHelper.class.getName());
		mThemeHelperMap.put(ThemeConfig.THEME_RINGTONG,
				ThemeRingtongHelper.class.getName());
		mThemeHelperMap.put(ThemeConfig.THEME_TIMES,
				ThemeTimeWallpaperHelper.class.getName());
	}


	public static synchronized ThemeManagerApplication getInstance(Context context){
		
		return (ThemeManagerApplication) context.getApplicationContext();
	}
	
	private static ThemeManagerApplication application;
	
	
	public static synchronized ThemeManagerApplication getInstance() {

		return application;
	}
	
	
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
			FileUtils.createThemeDirs();
			DownloadService.checkInit(this, null);
			
			TimeWallpaperDownloadService.checkInit(this, null);
			
			WallpaperDownloadService.checkInit(this, null);
			
			RingtongDownloadService.checkInit(this, null);

			AutoUpdateService.startAutoUpdate(this,0);
			PictureLoader.initImageLoader(getApplicationContext());
			initWallpaperData();
			application = this;
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
	
	
	
	
		public void registerActivity(Activity activity){
			if(!mActivities.contains(activity)){
				mActivities.add(activity);
			}
		}
	
	
	
		public void finishThemeManager(){
			if(mActivities .size() > 0){
				for(Activity activity:mActivities){
					activity.finish();
				}
			}
		}
	
	
	
	
	
	
	
	
	

}
