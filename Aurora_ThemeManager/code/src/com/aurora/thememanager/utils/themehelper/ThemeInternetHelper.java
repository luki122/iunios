package com.aurora.thememanager.utils.themehelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.aurora.internet.InternetError;
import com.aurora.internet.Listener;
import com.aurora.internet.Request;
import com.aurora.internet.RequestQueue;
import com.aurora.internet.cache.DiskCache;
import com.aurora.internet.request.JsonObjectRequest;
import com.aurora.thememanager.cache.CacheManager;
import com.aurora.thememanager.utils.SystemUtils;
import com.aurora.thememanager.utils.ThemeConfig;

public class ThemeInternetHelper {
	
	

	private RequestQueue mRequestQueue;


	private Context mContext;

	private ArrayList<JsonObjectRequest> mRequests = new ArrayList<JsonObjectRequest>();
	
	private CacheManager mCacheManager;
	
	public ThemeInternetHelper(Context context) {
		this(context,CacheManager.CACHE_NORMAL_REQUEST);
	}
	
	public ThemeInternetHelper(Context context,int cacheType){
		mCacheManager = CacheManager.getInstance();
		mContext = context;
		DiskCache cache = null;
		switch (cacheType) {
		case CacheManager.CACHE_IMAGES:
			cache = mCacheManager.getPreviewDiskCache();
			break;
		case CacheManager.CACHE_NORMAL_REQUEST:
			cache = mCacheManager.getThemeDiskCache();
			break;
		case CacheManager.CACHE_RINGTONG:
			cache = mCacheManager.getRingtongDiskCache();
			break;
		case CacheManager.CACHE_WALLPAPER:
			cache = mCacheManager.getWallPaperDiskCache();
			break;

		default:
			cache = mCacheManager.getThemeDiskCache();
			break;
		}
		mRequestQueue = RequestQueue.newRequestQueue(mContext,cache);
	}

	/**
	 * add a request into request queue to execute
	 * @param url
	 * @param listener
	 */
	public synchronized void request(String url,Listener<JSONObject> listener,JSONObject requestParams) {
		request(url, listener, requestParams, true);
	}
	
	/**
	 * add a request into request queue to execute
	 * @param url
	 * @param listener
	 * @param needCache current request need cache or not
	 */
	public synchronized void request(String url,Listener<JSONObject> listener,JSONObject requestParams,boolean needCache) {
		JsonObjectRequest json  = new JsonObjectRequest(url, requestParams, listener);
		if(needCache){
			json.setCacheExpireTime(TimeUnit.MINUTES, 30);
		}
		mRequests.add(json);
	}
	
	public void clearRequest(){
		mRequests.clear();
	}
	
	public void startRequest(){
		if(mRequests.size() < 1){
			return;
		}
		for(JsonObjectRequest json:mRequests){
			mRequestQueue.add(json);
		}
	}

	
	
	
	public void stopRequest(){
		mRequests.clear();
		if(mRequestQueue != null){
			mRequestQueue.stop();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
