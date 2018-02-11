package com.aurora.ota.reporter.appnumber;

import android.graphics.drawable.Drawable;

public class AppInfo {

	private String appName;
	private String appVersion;
	private Drawable drawable;
	private Boolean isUserApp;
	private String packageName;

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getAppName() {
		return appName;
	}

	public Boolean getIsUserApp() {
		return isUserApp;
	}

	public void setIsUserApp(Boolean isUserApp) {
		this.isUserApp = isUserApp;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public Drawable getDrawable() {
		return drawable;
	}

	public void setDrawable(Drawable drawable) {
		this.drawable = drawable;
	}

}
