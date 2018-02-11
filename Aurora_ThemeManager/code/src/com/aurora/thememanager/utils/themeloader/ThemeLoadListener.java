package com.aurora.thememanager.utils.themeloader;

import com.aurora.thememanager.entities.Theme;

/**
 * listener for load theme
 * @author alexluo
 *
 */
public interface ThemeLoadListener {

	
	void onStartLoad();
	
	void onProgress(Integer... progress);
	
	void onSuccess(boolean success,int status,Theme theme);
	
}
