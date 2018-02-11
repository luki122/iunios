package com.aurora.account.bean;

public class AppConfigInfo {

	// 应用显示名
	private String app_name;
	// 应用包名
	private String app_packagename;
	// 应用uri
	private String app_uri;
	//同步开关是否打开
	private boolean isSync;
	// 应用type sms-短信
	private String app_type;
	// 最后同步时间
	private long syncTime;
	// 是否账户切换
	private boolean isSwitch;
	
	private boolean isrepeatSync;
	
	// 是否应用自身同步
	private boolean app_syncself;
	
	public String getApp_name() {
		return app_name;
	}

	public void setApp_name(String app_name) {
		this.app_name = app_name;
	}

	public String getApp_packagename() {
		return app_packagename;
	}

	public void setApp_packagename(String app_packagename) {
		this.app_packagename = app_packagename;
	}

	public String getApp_uri() {
		return app_uri;
	}

	public void setApp_uri(String app_uri) {
		this.app_uri = app_uri;
	}

	public String getApp_type() {
		return app_type;
	}

	public void setApp_type(String app_type) {
		this.app_type = app_type;
	}

	public boolean isSync() {
		return isSync;
	}

	public void setSync(boolean isSync) {
		this.isSync = isSync;
	}

	public long getSyncTime() {
		return syncTime;
	}

	public void setSyncTime(long syncTime) {
		this.syncTime = syncTime;
	}

	public boolean isSwitch() {
		return isSwitch;
	}

	public void setSwitch(boolean isSwitch) {
		this.isSwitch = isSwitch;
	}

	public boolean isIsrepeatSync() {
		return isrepeatSync;
	}

	public void setIsrepeatSync(boolean isrepeatSync) {
		this.isrepeatSync = isrepeatSync;
	}

	public boolean isApp_syncself() {
		return app_syncself;
	}

	public void setApp_syncself(boolean app_syncself) {
		this.app_syncself = app_syncself;
	}

}
