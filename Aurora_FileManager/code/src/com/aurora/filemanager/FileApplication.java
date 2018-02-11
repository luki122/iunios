package com.aurora.filemanager;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Application;
import android.os.SystemClock;

import com.aurora.dbutil.FileCategoryHelper.FileCategory;
import com.aurora.lazyloader.ImageLoader;
import com.aurora.tools.LogUtil;
import com.aurora.tools.FileInfo;
import com.aurora.tools.OperationAction;

public class FileApplication extends Application {
	private static final String TAG ="FileApplication";

	/**
	 * {@link AuroraStorageDetailActivity}
	 */
	private ConcurrentHashMap<FileCategory, List<FileInfo>> hashMap;
	

	public ConcurrentHashMap<FileCategory, List<FileInfo>> getHashMap() {
		return hashMap;
	}


	public void setHashMap(ConcurrentHashMap<FileCategory, List<FileInfo>> hashMap) {
		this.hashMap = hashMap;
	}


	@Override
	public void onCreate() {
		super.onCreate();
//		long t1 = SystemClock.currentThreadTimeMillis();
//		ImageLoader mImageLoader = new ImageLoader(getApplicationContext());
//		mImageLoader.clearCache();
		OperationAction operationAction = new OperationAction(
				getApplicationContext(), true);
		operationAction.sendBroadcastScanByPath();
//		long t2 = SystemClock.currentThreadTimeMillis();
//		LogUtil.d(TAG, "Application time :"+(t2-t1));

	}
	

}
