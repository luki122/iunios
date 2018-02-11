package com.aurora.thememanager.fragments;

import org.json.JSONObject;

import android.util.Log;

import com.aurora.internet.InternetError;
import com.aurora.internet.Listener;

public class JsonHttpListener extends Listener<JSONObject> {

	private HttpCallBack<JSONObject> mCallBack;

	public JsonHttpListener(HttpCallBack<JSONObject> callback) {
		// TODO Auto-generated constructor stub
		mCallBack = callback;
	}

	@Override
	public void onSuccess(JSONObject response) {
		if (mCallBack != null) {
			mCallBack.onSuccess(response);
		}
	}

	@Override
	public void onNetworking() {
		// TODO Auto-generated method stub
		super.onNetworking();
		if (mCallBack != null) {
			mCallBack.onNetworking();
		}
	}

	@Override
	public void onError(InternetError error) {
		// TODO Auto-generated method stub

		super.onError(error);
		if (mCallBack != null) {
			mCallBack.onError(error);
		}
	}

	@Override
	public void onPreExecute() {
		// TODO Auto-generated method stub

		super.onPreExecute();
		if (mCallBack != null) {
			mCallBack.onPreExecute();
		}
	}

	@Override
	public void onProgressChange(long fileSize, long downloadedSize) {
		// TODO Auto-generated method stub
		super.onProgressChange(fileSize, downloadedSize);
		if (mCallBack != null) {
			mCallBack.onProgressChange(fileSize, downloadedSize);
			
		}
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		super.onFinish();
		if (mCallBack != null) {
			mCallBack.onFinish();
		}
	}

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		super.onCancel();
		if (mCallBack != null) {
			mCallBack.onCancel();
		}
	}

	@Override
	public void onUsedCache() {
		// TODO Auto-generated method stub
		super.onUsedCache();
		if (mCallBack != null) {
			mCallBack.onUsedCache();
		}
	}

	@Override
	public void onRetry() {
		// TODO Auto-generated method stub
		super.onRetry();
		if (mCallBack != null) {
			mCallBack.onRetry();
		}
	}

}
