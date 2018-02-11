package com.android.settings.lscreen;

import java.util.ArrayList;


import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;


public class AppInfo extends BaseData{
	
    /**
     * 注意：下面所有的数据都必须在初始化的时候赋值
     */

	private String appName;
	private String appNamePinYin;
	private String packageName;
	
	public AppInfo() {
		super("AppInfo");
	}
	
	public void updateObject(AppInfo appInfo){
		if(appInfo != null){
			appName = appInfo.getAppName();
			appNamePinYin = appInfo.getAppNamePinYin();
			packageName = appInfo.getPackageName();

		}
	}
	
	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	public String getAppNamePinYin(){
		return this.appNamePinYin;
	}
	
	public void setAppNamePinYin(String appNamePinYin){
		this.appNamePinYin = appNamePinYin;
	}

	public String getAppName() {
		return appName;
	}


	public void setAppName(String appName) {
		this.appName = appName;
	}
}
