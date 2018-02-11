/*
 * Copyright (C) 2010-2012 TENCENT Inc.All Rights Reserved.
 *
 * FileName: BannerItem
 *
 * Description:  海报图中每项数据
 *
 * History:
 *  1.0   kodywu (kodytx@gmail.com) 2010-11-30   Create
 */
package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;

public class BannerItem {

	private String appName;
	private String versionCode;
	private String versionName;
	private String force_update;
	private String downloadServer;
	private String apkFile;
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getVersionCode() {
		return versionCode;
	}
	public void setVersionCode(String versionCode) {
		this.versionCode = versionCode;
	}
	public String getVersionName() {
		return versionName;
	}
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getForce_update() {
		return force_update;
	}
	public void setForce_update(String force_update) {
		this.force_update = force_update;
	}
	public String getDownloadServer() {
		return downloadServer;
	}
	public void setDownloadServer(String downloadServer) {
		this.downloadServer = downloadServer;
	}
	public String getApkFile() {
		return apkFile;
	}
	public void setApkFile(String apkFile) {
		this.apkFile = apkFile;
	}
	
	
}
