package com.secure.data;

import android.content.pm.ResolveInfo;

public class SameCategoryAppInfo extends BaseData{
	private ResolveInfo resolveInfo;
	private boolean isSysApp = false;
	
	public SameCategoryAppInfo() {
		super("SameCategoryAppInfo");
	}
	
	public ResolveInfo getResolveInfo(){
		return this.resolveInfo;
	}
	
	public void setResolveInfo(ResolveInfo resolveInfo){
		this.resolveInfo = resolveInfo;
	}
	
	/**
	 * 是不是IUNI系统自带的应用
	 * @param isSysApp
	 */
	public void setIsSysApp(boolean isSysApp){
		this.isSysApp = isSysApp;
	}
	
	/**
	 * 是不是IUNI系统自带的应用
	 * @return
	 */
	public boolean getIsSysApp(){
		return this.isSysApp;
	}
}
