package com.aurora.thememanager.cache;

import java.io.File;

import com.aurora.internet.cache.BitmapImageCache;
import com.aurora.internet.cache.DiskCache;
import com.aurora.thememanager.utils.ThemeConfig;

public class CacheManager {
	
	/**
	 * 图片缓存
	 */
    public static final int CACHE_IMAGES = 0;
	/**
	 * 主题包缓存
	 */
	public static final int CACHE_NORMAL_REQUEST = 1;
	
	/**
	 * 铃声缓存
	 */
	public static final int CACHE_RINGTONG = 2;
	
	/**
	 * 壁纸缓存
	 */
	public static final int CACHE_WALLPAPER = 3;
	
	
	
	private static CacheManager mInstance;
	
	private DiskCache mPreiewDiskCache;
	
	private File mPreviewCacheDir = new File(ThemeConfig.THEME_CACHE_PATH, "preview/");
	private File mThemeCacheDir = new File(ThemeConfig.THEME_CACHE_PATH, "theme_cache/");
	private File mWallPaperCacheDir = new File(ThemeConfig.THEME_CACHE_PATH, "wallpaper_cache/");
	private File mRingtongCacheDir = new File(ThemeConfig.THEME_CACHE_PATH, "ringtong_cache/");

	/**
	 * 内存缓存大小
	 */
	private int mMemCacheSize = 20*1024*1204;//20M
	
	/**
	 * 磁盘缓存大小
	 */
	private int mDiskCacheSize = 100 * 1024 * 1024; // 100MB
	
	private DiskCache mThemeDiskCache;
	private DiskCache mWallpaperDiskCache;
	private DiskCache mRingtongDiskCache;
	
	private BitmapImageCache mImageCache;
	
	private CacheManager(){
		mPreiewDiskCache = 	new DiskCache(mPreviewCacheDir,mDiskCacheSize);
		mThemeDiskCache = new DiskCache(mThemeCacheDir,mDiskCacheSize);
		mImageCache = new BitmapImageCache(mMemCacheSize);
		mWallpaperDiskCache = 	new DiskCache(mWallPaperCacheDir,mDiskCacheSize);
		mRingtongDiskCache = new DiskCache(mRingtongCacheDir,mDiskCacheSize);
	}

	
	public static CacheManager getInstance(){
		synchronized (CacheManager.class) {
			if(mInstance == null){
				mInstance = new CacheManager();
			}
			return mInstance;
		}
	}
	
	public BitmapImageCache getBitmapCache(){
		return mImageCache;
	}
	
	public int getCacheSize(){
		return mDiskCacheSize;
	}
	
	
	public DiskCache getPreviewDiskCache(){
		return mPreiewDiskCache;
	}
	
	public DiskCache getThemeDiskCache(){
		
		return mThemeDiskCache;
	}
	
	public DiskCache getWallPaperDiskCache(){
		return mWallpaperDiskCache;
	}
	
	public DiskCache getRingtongDiskCache(){
		return mRingtongDiskCache;
	}
	
	
}
