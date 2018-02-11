package com.android.gallery3d.xcloudalbum.tools.cache.image;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class FileCache {
	private final String TAG = "FileCache";
	private DiskLruCache diskLruCache;
	private File cacheDir;
	private static final int DISK_MAX_SIZE = 20 * 1024 * 1024;// SD 20MB

	private static FileCache instance;

	public static FileCache getInstance(Context context) {
		if (instance == null) {
			instance = new FileCache(context);
		}
		return instance;
	}

	public FileCache(Context context) {
		super();
		try {
			cacheDir = DiskLruCache.getDiskCacheDir(context, "imageCache");
			diskLruCache = DiskLruCache.openCache(cacheDir, DISK_MAX_SIZE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Bitmap getFileCache(String url) {
		if (diskLruCache == null) {
			Log.d(TAG, "diskLruCache is null");
			return null;
		}
		return diskLruCache.get(url);
	}

	public void saveBitmapByLru(String key, Bitmap bitmap) {
		if (diskLruCache == null) {
			Log.d(TAG, "diskLruCache is null");
			return;
		}
		if (key != null && bitmap != null) {
			synchronized (diskLruCache) {
				if (!diskLruCache.containsKey(key)) {
					try {
						if(Utils.getExtFromFilename(key).equalsIgnoreCase("png")){
							diskLruCache.setCompressParams(CompressFormat.PNG);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					diskLruCache.put(key, bitmap);
					
				}
			}
		}
	}

	public void clearFileCache() {
		if (diskLruCache == null) {
			Log.d(TAG, "diskLruCache is null");
			return;
		}
		diskLruCache.clearCache();
	}


}
