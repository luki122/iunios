package com.aurora.datauiapi.data.bean;


public class AppUpgradeInfo {


	//应用ID，在服务器端数据库中存储的ID
	private int id;
	//应用名称
	private String title;
	//应用的包名
	private String packageName;
	//服务器端应用的大小
	private int appSize;
	private String appSizeStr;
	//客户端应用的大小
	private int clientAppSize;
	//客户端应用的版本名称
	private String clientVersionName;
	//版本名
	private String versionName;
	//客户端版本号
	private int clientVersionCode;
	//版本号
	private int versionCode;
	//应用下载地址
	private String downloadURL;
	//更新的描述
	private String changelog;
	//描述
	private String description;
	//应用的一组图标地址，px256表示256像素
	private IconItem icons = new IconItem();
	
	private int forceUpFlag;

	public String getChangelog() {
		return changelog;
	}

	public void setChangelog(String changelog) {
		this.changelog = changelog;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public int getAppSize() {
		return appSize;
	}

	public void setAppSize(int appSize) {
		this.appSize = appSize;
	}

	public String getClientVersionName() {
		return clientVersionName;
	}

	public void setClientVersionName(String clientVersionName) {
		this.clientVersionName = clientVersionName;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public int getClientVersionCode() {
		return clientVersionCode;
	}

	public void setClientVersionCode(int clientVersionCode) {
		this.clientVersionCode = clientVersionCode;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getDownloadURL() {
		return downloadURL;
	}

	public void setDownloadURL(String downloadURL) {
		this.downloadURL = downloadURL;
	}

	public IconItem getIcons() {
		return icons;
	}

	public void setIcons(IconItem icons) {
		this.icons = icons;
	}

	public int getClientAppSize() {
		return clientAppSize;
	}

	public void setClientAppSize(int clientAppSize) {
		this.clientAppSize = clientAppSize;
	}

	public String getAppSizeStr() {
		return appSizeStr;
	}

	public void setAppSizeStr(String appSizeStr) {
		this.appSizeStr = appSizeStr;
	}

	public int getForceUpFlag() {
		return forceUpFlag;
	}

	public void setForceUpFlag(int forceUpFlag) {
		this.forceUpFlag = forceUpFlag;
	}



}
