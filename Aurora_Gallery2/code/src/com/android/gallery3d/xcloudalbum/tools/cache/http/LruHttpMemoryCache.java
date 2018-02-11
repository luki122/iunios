package com.android.gallery3d.xcloudalbum.tools.cache.http;

import java.util.List;
import java.util.WeakHashMap;

import android.text.TextUtils;
import android.util.LruCache;

import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

public class LruHttpMemoryCache {

	private static final String TAG = "LruHttpMemoryCache";
	private static LruHttpMemoryCache instance;

	public static LruHttpMemoryCache getInstance() {
		if (instance == null) {
			instance = new LruHttpMemoryCache();
		}
		return instance;
	}

	private final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
	private final int cacheSize = maxMemory / 8;
	// 请求相册目录缓存
	private LruCache<String, List<CommonFileInfo>> albumCache = new LruCache<String, List<CommonFileInfo>>(
			cacheSize){};

//	private WeakHashMap<String, List<CommonFileInfo>> albumCache = new WeakHashMap<String, List<CommonFileInfo>>();
	// key==URL
	public void addLruCache(String key, List<CommonFileInfo> infos) {
		if (!TextUtils.isEmpty(key) && infos != null
				&& getHttpFromMemCache(key) == null) {
			synchronized (albumCache) {
				albumCache.put(key, infos);
			}
//			LogUtil.d(TAG, "addLruCache albumCache::" + albumCache.size()
//					+ " key::" + key+" cacheSize::"+cacheSize);
		}
	}

	public void removeLruCache(String key) {
		if (!TextUtils.isEmpty(key)) {
			synchronized (albumCache) {
				albumCache.remove(key);
			}
//			LogUtil.d(TAG, "removeLruCache albumCache::" + albumCache.size()
//					+ " key::" + key);
		}
	}

	public List<CommonFileInfo> getHttpFromMemCache(String key) {
//		LogUtil.d(TAG, "getHttpFromMemCache albumCache::" + albumCache.size()
//				+ " key::" + key);
		synchronized (albumCache) {
			return albumCache.get(key);
		}
	}

	public void clear() {
		albumCache.evictAll();
	}
}
