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
		mCacheManager = CacheManager.getInstance();
		mContext = context;
		mRequestQueue = RequestQueue.newRequestQueue(mContext,mCacheManager.getThemeDiskCache());
	}

	/**
	 * add a request into request queue to execute
	 * @param url
	 * @param listener
	 */
	public synchronized void request(String url,Listener<JSONObject> listener,JSONObject requestParams) {
		JsonObjectRequest json  = new JsonObjectRequest(url, requestParams, listener);
		if(!SystemUtils.hasNetwork()){
			json.setCacheExpireTime(TimeUnit.MINUTES, 1);
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
