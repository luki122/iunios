package com.secure.model;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * @author Administrator
 *
 */
public class ConfigModel {   
	private static ConfigModel instance;
	private AppInfoModel appInfoModel;
	private PackageManager pm ;
	private Context context;

	
	private ConfigModel(Context context) {
		this.context = context.getApplicationContext();
        appInfoModel = new AppInfoModel(context.getApplicationContext());
	}
	
	/**
	 * 如果instance为null，不会创建
	 * @return 返回值有可能为空
	 */
	public static synchronized ConfigModel getInstance(){
		return instance;
	}

	/**
	 * 如果instance为null，则会创建一个
	 * @param context
	 * @return 返回值不可能为null
	 */
	public static synchronized ConfigModel getInstance(Context context) {
		if (instance == null) {
			instance = new ConfigModel(context);
		}
		return instance;
	}
	
	/**
	 * @return 返回值不可能为null
	 */
	public AppInfoModel getAppInfoModel(){
		return appInfoModel;
	}
	
	public PackageManager getPackageManager(){
		if(pm == null){
			pm = context.getPackageManager();
		}
		return pm;
	}
	
	public static void releaseObject(){
		if(instance != null){
			if(instance.appInfoModel != null){
				instance.appInfoModel.releaseObject();
			}
		}
		instance = null;
	}
}
