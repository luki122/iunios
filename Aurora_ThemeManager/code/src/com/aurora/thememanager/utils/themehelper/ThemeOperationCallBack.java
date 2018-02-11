package com.aurora.thememanager.utils.themehelper;

import android.content.Context;

public interface ThemeOperationCallBack {
	
	/**
	 * implemets  this method to get status of apply theme 
	 * @param success
	 * @param statusCode
	 */
	public void onCompleted(boolean success,int statusCode);
	
	/**
	 * progress for apply new theme
	 * @param progress
	 */
	public void onProgressUpdate(int progress);

	
	public Context getContext();
}
