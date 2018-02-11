package com.aurora.note;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.aurora.note.util.Globals;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class NoteApp extends Application {

	public static NoteApp ysApp;

	private List<Activity> activityList = new LinkedList<Activity>();

	@Override
	public void onCreate() {
		ysApp = this;
		super.onCreate();
		initImageLoader(getApplicationContext());
	}

	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true).build();
		File cacheDir = StorageUtils.getOwnCacheDirectory(context, Globals.CACHE_DIR_NAME);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.defaultDisplayImageOptions(defaultOptions)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.discCache(new UnlimitedDiscCache(cacheDir))
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.discCacheSize(50 * 1024 * 1024)
				//.memoryCache(new LruMemoryCache(6 * 1024 * 1024))
				.memoryCache(new WeakMemoryCache())
				.memoryCacheSize(6 * 1024 * 1024)
				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	// 单例模式中获取唯一的ExitApplication 实例
	public static NoteApp getInstance() {
		return ysApp;
	}

	// 添加Activity 到容器中
	public void addActivity(Activity activity) {
		activityList.add(activity);
	}

	// 遍历所有Activity 并finish
	public void exit() {
		// 回设亮度
//		if(m_content != null) {
//			m_content.resetBrightnessValue();
//		}
		for (Activity activity : activityList) {
			activity.finish();
		}

		System.exit(0);
	}

}
