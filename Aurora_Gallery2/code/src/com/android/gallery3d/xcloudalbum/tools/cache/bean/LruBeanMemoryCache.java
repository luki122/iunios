package com.android.gallery3d.xcloudalbum.tools.cache.bean;

import java.util.List;

import android.text.TextUtils;
import android.util.LruCache;

import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;

public class LruBeanMemoryCache {

	private static LruBeanMemoryCache instance;

	public static LruBeanMemoryCache getInstance() {
		if (instance == null) {
			instance = new LruBeanMemoryCache();
		}
		return instance;
	}

	private final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
	private final int cacheSize = maxMemory / 8;
	// 请求相册目录缓存
	private LruCache<String, List<FileTaskStatusBean>> beanCache = new LruCache<String, List<FileTaskStatusBean>>(
			cacheSize);

	// key==URL
	public void addLruCache(String key, List<FileTaskStatusBean> infos) {
		if (!TextUtils.isEmpty(key) && infos != null
				&& getBeanFromMemCache(key) == null) {
			beanCache.put(key, infos);
		}
	}

	public void removeLruCache(String key) {
		if (!TextUtils.isEmpty(key)) {
			beanCache.remove(key);
		}
	}

	public List<FileTaskStatusBean> getBeanFromMemCache(String key) {
		return beanCache.get(key);
	}

	public void clear() {
		beanCache.evictAll();
	}
}
