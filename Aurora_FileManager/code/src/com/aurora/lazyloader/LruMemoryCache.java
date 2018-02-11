package com.aurora.lazyloader;

import android.graphics.Bitmap;
import android.util.LruCache;

public class LruMemoryCache {

	private static LruMemoryCache instance;

	public static LruMemoryCache getInstance() {
		if (instance == null) {
			instance = new LruMemoryCache();
		}
		return instance;
	}

	public LruMemoryCache() {
		super();
	}

	private final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
	private final int cacheSize = maxMemory / 8;
	private LruCache<String, Bitmap> mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
		@Override
		protected int sizeOf(String key, Bitmap bitmap) {
			return bitmap.getByteCount() / 1024;
		}
	};

	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (key != null && bitmap != null && !bitmap.isRecycled() && getBitmapFromMemCache(key) == null) {
			// Log.e("LruMemoryCache",
			// "mMemoryCache.size()==" + mMemoryCache.size() + " <= "
			// + mMemoryCache.maxSize());
			mMemoryCache.put(key, bitmap);
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

	public void clear() {
		if (mMemoryCache == null) {
			return;
		}
		mMemoryCache.evictAll();
	}
}
