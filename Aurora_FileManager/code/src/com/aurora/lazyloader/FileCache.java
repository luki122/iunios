package com.aurora.lazyloader;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.text.TextUtils;

import com.aurora.tools.LogUtil;
import com.aurora.tools.Util;

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
			LogUtil.e(TAG, "cacheDir=="+cacheDir.getAbsolutePath());
			diskLruCache = DiskLruCache.openCache(cacheDir, DISK_MAX_SIZE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Bitmap getFileCache(String url) {
		if (diskLruCache == null) {
			LogUtil.e(TAG, "diskLruCache is null");
			return null;
		}
		return diskLruCache.get(url);
	}

	public void saveBitmapByLru(String key, Bitmap bitmap) {
		if (diskLruCache == null||TextUtils.isEmpty(key)) {
			LogUtil.e(TAG, "diskLruCache is null or Key is null");
			return;
		}
		try {
			if(Util.getExtFromFilename(key).equalsIgnoreCase("png")){
				diskLruCache.setCompressParams(CompressFormat.PNG);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String url = FileCache.urlToMD5(key);
		if (url != null && bitmap != null) {
			synchronized (diskLruCache) {
				if (!diskLruCache.containsKey(url)) {
					diskLruCache.put(url, bitmap);
					// Log.e(TAG, "saveBitmapByLru="+url);
				}
			}
		}
	}

	public void clearFileCache() {
		if (diskLruCache == null) {
			LogUtil.e(TAG, "diskLruCache is null");
			return;
		}
		diskLruCache.clearCache();
	}

	public static String urlToMD5(String url) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return MD5(url, md);
	}

	public static String MD5(String strSrc, MessageDigest md) {
		byte[] bt = strSrc.getBytes();
		md.update(bt);
		String strDes = bytes2Hex(md.digest()); // to HexString
		return strDes;
	}

	private static String bytes2Hex(byte[] bts) {
		StringBuffer des = new StringBuffer();
		String tmp = null;
		for (int i = 0; i < bts.length; i++) {
			tmp = (Integer.toHexString(bts[i] & 0xFF));
			if (tmp.length() == 1) {
				des.append("0");
			}
			des.append(tmp);
		}
		return des.toString();
	}

}
