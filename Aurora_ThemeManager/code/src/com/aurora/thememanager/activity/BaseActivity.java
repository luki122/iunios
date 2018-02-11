package com.aurora.thememanager.activity;

import com.aurora.thememanager.ThemeManagerApplication;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.preference.PreferenceManager;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

public abstract class BaseActivity extends AuroraActivity{
	
	protected ImageLoader mImageLoader = ImageLoader.getInstance();
	
	protected AuroraActionBar mActionBar;
	
	protected PreferenceManager mPrefManager;
	
	public ThemeManagerApplication mApp;
	
	private int mWidth,mHeight;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mPrefManager = PreferenceManager.getInstance(this);
		mWidth = getResources().getDisplayMetrics().widthPixels;
		mHeight = getResources().getDisplayMetrics().heightPixels;
		mApp = (ThemeManagerApplication) getApplication();
	}
	
	
	public int getWidth(){
		return mWidth;
	}
	
	public int getHeight(){
		return mHeight;
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		mImageLoader.clearMemoryCache();
		mImageLoader.clearDiscCache();
	}
	
	
	protected long getCurrentTheme(){
		return mPrefManager.getLong(PreferenceManager.KEY_CURRENT_THEME);
	}
	
	protected void saveTheme(Theme theme){
		mPrefManager.saveLong(PreferenceManager.KEY_CURRENT_THEME, theme.themeId);
	}

}
