package com.aurora.datauiapi.data.bean;

public class AppUpdateInfo {


	// 应用的包名
	private String packageName;
	// 版本号
	private int versionCode;
	// 版本名
	private String versionName;

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}



}
