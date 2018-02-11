package com.android.gallery3d.xcloudalbum.tools.cache.bean;

import java.util.List;

import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;

import android.content.Context;
import android.text.TextUtils;

public class BeanCacheManager {
	private LruBeanMemoryCache memoryCache;
	private FileBeanCache fileCache;
	private static final String TAG ="BeanCacheManager";

	private static BeanCacheManager cacheManager;

	public static BeanCacheManager getInstance(Context context) {
		if (cacheManager == null) {
			cacheManager = new BeanCacheManager(context);
		}
		return cacheManager;
	}
	
	

	public BeanCacheManager(Context context) {
		super();
		this.memoryCache = LruBeanMemoryCache.getInstance();
		this.fileCache = FileBeanCache.getInstance(context);
	}



	public List<FileTaskStatusBean> getFromCache(String key) {
		if (TextUtils.isEmpty(key)) {
			return null;
		}
		List<FileTaskStatusBean> objects = memoryCache.getBeanFromMemCache(key);
		if (objects != null) {
//			LogUtil.d(TAG, "memoryCache cache");
			return objects;
		}
		objects = fileCache.getFileCache(key);
		if (objects != null) {
//			LogUtil.d(TAG, "fileCache cache");
			return objects;
		}
		return null;

	}

	public void saveCache(String key, List<FileTaskStatusBean> objects) {
		if (TextUtils.isEmpty(key) || objects == null) {
			return;
		}
		memoryCache.addLruCache(key, objects);
		fileCache.saveBeanByLru(key, objects);
	}
	
	public void removeCache(String key){
		if (TextUtils.isEmpty(key)) {
			return;
		}
		memoryCache.removeLruCache(key);
		fileCache.removeBeanCache(key);
		
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clearFileCache();
	}
}
