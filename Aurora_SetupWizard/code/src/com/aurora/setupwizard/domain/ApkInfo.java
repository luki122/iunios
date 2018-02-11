package com.aurora.setupwizard.domain;

import android.graphics.drawable.Drawable;

public class ApkInfo {
	
	private String apkPath;
	private String apkIconPath;
	private String apkName;
	private String apkPackageName;
	private Drawable apkIcon;
	private String apkDescription;
	private boolean isCheck;
	public String getApkPath() {
		return apkPath;
	}
	public void setApkPath(String apkPath) {
		this.apkPath = apkPath;
	}
	public String getApkName() {
		return apkName;
	}
	public void setApkName(String apkName) {
		this.apkName = apkName;
	}
	public String getApkPackageName() {
		return apkPackageName;
	}
	public void setApkPackageName(String apkPackageName) {
		this.apkPackageName = apkPackageName;
	}
	public Drawable getApkIcon() {
		return apkIcon;
	}
	public void setApkIcon(Drawable apkIcon) {
		this.apkIcon = apkIcon;
	}
	public boolean isCheck() {
		return isCheck;
	}
	public void setCheck(boolean isCheck) {
		this.isCheck = isCheck;
	}
	public String getApkDescription() {
		return apkDescription;
	}
	public void setApkDescription(String apkDescription) {
		this.apkDescription = apkDescription;
	}
	public String getApkIconPath() {
		return apkIconPath;
	}
	public void setApkIconPath(String apkIconPath) {
		this.apkIconPath = apkIconPath;
	}
	
}
