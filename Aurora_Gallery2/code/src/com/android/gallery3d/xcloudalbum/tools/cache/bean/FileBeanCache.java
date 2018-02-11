package com.android.gallery3d.xcloudalbum.tools.cache.bean;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;

public class FileBeanCache {

	private final String TAG = "SQF_LOG";
	private DiskLruBeanCache diskLruCache;
	private File cacheDir;
	private static final int DISK_MAX_SIZE = 20 * 1024 * 1024;// SD 20MB

	private static FileBeanCache instance;

	public static FileBeanCache getInstance(Context context) {
		if (instance == null) {
			instance = new FileBeanCache(context);
		}
		return instance;
	}

	public FileBeanCache(Context context) {
		super();
		try {
			cacheDir = DiskLruBeanCache.getDiskCacheDir(context, "bean");// /storage/emulated/0/Android/data/com.android.gallery3d/cache/bean
			diskLruCache = DiskLruBeanCache.openCache(cacheDir, DISK_MAX_SIZE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<FileTaskStatusBean> getFileCache(String key) {
		if (diskLruCache == null) {
			Log.d(TAG, "diskLruCache is null");
			return null;
		}
		return diskLruCache.get(key);
	}

	public void saveBeanByLru(String key, List<FileTaskStatusBean> objects) {
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
	
	public void removeBeanCache(String key){
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
