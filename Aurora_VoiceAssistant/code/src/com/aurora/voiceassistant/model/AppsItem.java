package com.aurora.voiceassistant.model;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class AppsItem {
	String mAppName;
	Intent mAppIntent;
	Drawable mAppDrawable;
	
	public AppsItem(String name, Intent intent, Drawable drawable) {
		mAppName = name;
		mAppIntent = intent;
		mAppDrawable = drawable;
	}
	
	public String getAppName() {
		return mAppName;
	}
	public Intent getAppIntent() {
		return mAppIntent;
	}
	public Drawable getAppDrawable() {
		return mAppDrawable;
	}
}
