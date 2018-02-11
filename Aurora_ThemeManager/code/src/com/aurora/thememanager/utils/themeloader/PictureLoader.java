package com.aurora.thememanager.utils.themeloader;

import java.io.File;

import android.content.Context;
import android.widget.ImageView;

import com.aurora.thememanager.R;
import com.aurora.thememanager.utils.ThemeConfig;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class PictureLoader {
	
	public static final String PREFIX_LOCAL = "file:///mnt";
	
	public static DisplayImageOptions createImageLoaderOption(){
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
		.cacheInMemory(true)
		.cacheOnDisk(true)
		.showStubImage(R.drawable.item_default_bg)
		.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
		.bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
		.build();
		
		return defaultOptions;
	}
	
	public static  void initImageLoader(Context context) {
		
		File cacheDir = StorageUtils.getOwnCacheDirectory(context,
				ThemeConfig.IMAGE_LOAD_CACHE_DIR);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).defaultDisplayImageOptions(createImageLoaderOption())
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCache(new UnlimitedDiscCache(cacheDir))
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.diskCacheSize(100 * 1024 * 1024)
				
				.memoryCache(new LruMemoryCache(20 * 1024 * 1024))
				// .memoryCache(new WeakMemoryCache())
				.memoryCacheSize(20 * 1024 * 1024).writeDebugLogs() // Remove
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}
	
	public static ImageLoader getImageLoader(){
		return ImageLoader.getInstance();
	}

	
	public static void displayImage(String uri,ImageView imageView,ImageLoadingListener listener){
		ImageLoader loader = getImageLoader();
		DisplayImageOptions option = createImageLoaderOption();
		loader.displayImage(uri, imageView,option,listener);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
