package com.aurora.account;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.aurora.account.util.CommonUtil;
import com.aurora.account.util.Globals;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

public class AccountApp extends Application {
	private List<WeakReference<Activity>> activityList = new LinkedList<WeakReference<Activity>>();
	private static AccountApp instance;

	@Override
	public void onCreate() {

		instance = this;
		Log.i("marketApp", "oncreate");
		super.onCreate();

		CommonUtil.setAutoSyncAlarm();
		initImageLoader(getApplicationContext());
		
		File fl = new File(Environment.getExternalStorageDirectory()
				+ "/accounttest1234567890");
		if (fl.isDirectory()) {
			if (fl.listFiles().length == 1) {
				Globals.HTTP_REQUEST_URL = "http://" + fl.list()[0].toString()
						+ "/account";

				Globals.HTTPS_REQUEST_URL = "https://"
						+ fl.list()[0].toString() + "/account";
			} else {
				Globals.HTTP_REQUEST_URL = Globals.HTTP_REQUEST_TEST_URL;
				Globals.HTTPS_REQUEST_URL = Globals.HTTPS_REQUEST_TEST_URL;
			}
		} else {
			Globals.HTTP_REQUEST_URL = Globals.HTTP_REQUEST_DEFAULT_URL;
			Globals.HTTPS_REQUEST_URL = Globals.HTTPS_REQUEST_DEFAULT_URL;
		}
	}


	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you
		// may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true).cacheOnDisk(true).build();
		File cacheDir = StorageUtils.getOwnCacheDirectory(context,
				"account/Cache");
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).defaultDisplayImageOptions(defaultOptions)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCache(new UnlimitedDiscCache(cacheDir))
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.diskCacheSize(100 * 1024 * 1024)

				.memoryCache(new LruMemoryCache(20 * 1024 * 1024))
				// .memoryCache(new WeakMemoryCache())
				.memoryCacheSize(20 * 1024 * 1024).writeDebugLogs() // Remove
																	// for
																	// release
																	// app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	// 单例模式中获取唯一的ExitApplication 实例
	public static AccountApp getInstance() {
		/*
		 * if (null == instance) { instance = new marketApp(); }
		 */
		return instance;

	}

	// 添加Activity 到容器中
	public void addActivity(Activity activity) {
		activityList.add(new WeakReference<Activity>(activity));
	}

	// 遍历所有Activity 并finish

	public void exit() {
		for (WeakReference<Activity> activity : activityList) {

			if (null != activity.get())
				activity.get().finish();
		}
		activityList.clear();
		System.exit(0);
	}
}
