package com.aurora.tools;

import android.text.TextUtils;
import android.util.LruCache;

public class LruMemoryCacheByInteger {
	private static LruMemoryCacheByInteger instance;

	public static LruMemoryCacheByInteger getInstance() {
		if (instance == null) {
			instance = new LruMemoryCacheByInteger();
		}
		return instance;
	}

	public LruMemoryCacheByInteger() {
		super();
	}

	private final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
	private final int cacheSize = maxMemory / 8;
	private LruCache<String, Integer> mMemoryCache = new LruCache<String, Integer>(
			cacheSize) {
		@Override
		protected int sizeOf(String key, Integer integer) {
			return integer.byteValue() / 1024;
		}
	};

	public void addBitmapToMemoryCache(String key, Integer integer) {
		if (key != null && integer != null) {
			mMemoryCache.put(key, integer);
		}
	}
	
	public void removeMemoryCache(String key){
		if(!TextUtils.isEmpty(key)&&mMemoryCache!=null){
			mMemoryCache.remove(key);
		}
	}
	
	public void removeAllMemoryCache(){
		if(mMemoryCache!=null){
			mMemoryCache.evictAll();
		}
	}

	public Integer getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}
}
