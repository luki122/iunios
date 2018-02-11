package com.android.gallery3d.xcloudalbum.tools.cache.http;

import java.util.List;

import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.baidu.xcloud.pluginAlbum.AlbumConfig;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

import android.content.Context;
import android.text.TextUtils;

public class HttpCacheManager {
	private LruHttpMemoryCache memoryCache;
	private FileHttpCache fileCache;
	private static final String TAG ="HttpCacheManager";

	private static HttpCacheManager cacheManager;

	public static HttpCacheManager getInstance(Context context) {
		if (cacheManager == null) {
			cacheManager = new HttpCacheManager(context);
		}
		return cacheManager;
	}
	
	

	public HttpCacheManager(Context context) {
		super();
		this.memoryCache = LruHttpMemoryCache.getInstance();
		this.fileCache = FileHttpCache.getInstance(context);
	}



	public List<CommonFileInfo> getFromCache(String key) {
		if (TextUtils.isEmpty(key)) {
			return null;
		}
		List<CommonFileInfo> objects = memoryCache.getHttpFromMemCache(key);
		if (objects != null) {
			return objects;
		}
		objects = fileCache.getFileCache(key);
		if (objects != null) {
			memoryCache.addLruCache(key, objects);
			return objects;
		}
		return null;

	}

	public void saveCache(String key, List<CommonFileInfo> objects) {
		if (TextUtils.isEmpty(key) || objects == null) {
			return;
		}
		
		memoryCache.addLruCache(key, objects);
		fileCache.saveHttpByLru(key, objects);
	}
	
	public void removeCache(String key){
		if (TextUtils.isEmpty(key)) {
			return;
		}
		memoryCache.removeLruCache(key);
		fileCache.removeHttpCache(key);
		
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clearFileCache();
	}
}
