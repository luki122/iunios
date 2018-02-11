package com.android.gallery3d.xcloudalbum.tools.cache.http;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

public class FileHttpCache {

	private final String TAG = "FileHttpCache";
	private DiskLruHttpCache diskLruCache;
	private File cacheDir;
	private static final int DISK_MAX_SIZE = 20 * 1024 * 1024;// SD 20MB

	private static FileHttpCache instance;

	public static FileHttpCache getInstance(Context context) {
		if (instance == null) {
			instance = new FileHttpCache(context);
		}
		return instance;
	}

	public FileHttpCache(Context context) {
		super();
		try {
			cacheDir = DiskLruHttpCache.getDiskCacheDir(context, "http");
			diskLruCache = DiskLruHttpCache.openCache(cacheDir, DISK_MAX_SIZE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<CommonFileInfo> getFileCache(String key) {
		if (diskLruCache == null) {
			Log.d(TAG, "diskLruCache is null");
			return null;
		}
		return diskLruCache.get(key);
	}

	public void saveHttpByLru(String key, List<CommonFileInfo> objects) {
		if (diskLruCache == null) {
			Log.d(TAG, "diskLruCache is null");
			return;
		}
		if (key != null && objects != null) {
			synchronized (diskLruCache) {
				if (!diskLruCache.containsKey(key)) {
					diskLruCache.put(key, objects);
					
				}
			}
		}
	}
	
	public void removeHttpCache(String key){
		if (diskLruCache == null) {
			Log.d(TAG, "diskLruCache is null");
			return;
		}
		diskLruCache.removeCache(key);
	}

	public void clearFileCache() {
		if (diskLruCache == null) {
			Log.d(TAG, "diskLruCache is null");
			return;
		}
		diskLruCache.clearCache();
	}
}
