package com.aurora.puremanager.data;

import java.util.ArrayList;

import android.content.pm.ApplicationInfo;

//import com.lbe.security.service.sdkhelper.SDKConstants;  modify by gaoming
//import com.aurora.puremanager.data.PermissionInfo;
import com.aurora.puremanager.utils.ApkUtils;

public class AppInfo extends BaseData{
	/**
	 * 下面参数在使用的时候初始化
	 */
	private long ApkTotalNetBytes;//apk消耗的总流量
	private long ApkTotalBackgroundNetBytes;//apk消耗的后台偷跑总流量
	private boolean isInstalled = true;

    /**
     * 注意：下面所有的数据都必须在初始化的时候赋值
     */
	private String appName;
	private String appNamePinYin;
	private String appVersion;
	private int versionCode;
	private String packageName;
	private int uid;
//	private ArrayList<PermissionInfo> permissionList = null;	
	private boolean isUserApp = true;
	private boolean isNeedOptimize = false;
	private boolean isHaveNetworkingPermission = false;//是否有联网权限
	private boolean isSysWhiteApp = false;//是否是系统白名单应用
	private boolean isHome;
	private boolean isHaveLauncher;
	private long memorysize;
	private boolean isBlockAd = false;//是否拦截广告
	private ApplicationInfo appInfo;

	//纯净后台相关
	private boolean isStopAutoStart = false;//禁止自启
	private boolean isStopWakeup = false;//自动休眠
	private boolean isAutoSleep = false;//自动休眠

	public AppInfo() {
		super("AppInfo");
	}
	
	public void updateObject(AppInfo appInfo){
		if(appInfo != null){
			ApkTotalNetBytes = appInfo.getApkTotalNetBytes();
			isInstalled = appInfo.getIsInstalled();
			appName = appInfo.getAppName();
			appNamePinYin = appInfo.getAppNamePinYin();
			appVersion = appInfo.getAppVersion();
			versionCode = appInfo.getVersionCode();
			packageName = appInfo.getPackageName();
			uid = appInfo.getUid();
			isUserApp = appInfo.getIsUserApp();
//			isNeedOptimize = appInfo.getIsNeedOptimize();
//			isHaveNetworkingPermission = appInfo.getIsHaveNetworkingPermission();
			isSysWhiteApp = appInfo.getIsSysWhiteApp();
			isHome = appInfo.isHome();
			isHaveLauncher = appInfo.isHaveLauncher();
			
			/*ArrayList<PermissionInfo> tmpPermissionList = new ArrayList<PermissionInfo>();
			if(appInfo.getPermission() != null){
				for(int i=0;i<appInfo.getPermission().size();i++){
					addToList(tmpPermissionList,appInfo.getPermission().get(i));
				}
			}
			if(permissionList == null){
				permissionList = new ArrayList<PermissionInfo>();
			}else{
				permissionList.clear();
			}		
			for(int i=0;i<tmpPermissionList.size();i++){
				permissionList.add(tmpPermissionList.get(i));
			}		*/
		}
	}
	
	/*private void addToList(ArrayList<PermissionInfo> toPermissionInfo,
			PermissionInfo needAddPermissionInfo){
        int needAddPerId = needAddPermissionInfo.permId;
		if(permissionList != null){
			for(int i=0;i<permissionList.size();i++){
				PermissionInfo oldInfo = permissionList.get(i);
				if(needAddPerId == oldInfo.permId){
					oldInfo.updateObject(needAddPermissionInfo);
					needAddPermissionInfo = oldInfo;
					break;
				}
			}	
		}
		toPermissionInfo.add(needAddPermissionInfo);
	}*/
	
	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	public void setUid(int uid){
		this.uid = uid;
	}
	
	public int getUid(){
		return this.uid;
	}
	
	/**
	 * 应用申请的权限
	 * @param permission
	 */
	/*public void setPermission(ArrayList<PermissionInfo> permissionList){
		this.permissionList = permissionList;
	}*/
	
	/**
	 * 应用申请的权限
	 * @return
	 */
	/*public ArrayList<PermissionInfo> getPermission(){
		if(!ApkUtils.isLBEServiceConnect()){
			return null;
		}
		return this.permissionList;
	}*/
	
	public String getAppNamePinYin(){
		return this.appNamePinYin;
	}
	
	public void setAppNamePinYin(String appNamePinYin){
		this.appNamePinYin = appNamePinYin;
	}

	public String getAppName() {
		return appName;
	}

	/**
	 * 是不是第三方应用
	 * @return true：是第三方应用 false：不是第三方应用
	 */
	public Boolean getIsUserApp() {
		return isUserApp;
	}

	/**
	 * 是不是第三方应用
	 * @param isUserApp 是第三方应用 false：不是第三方应用
	 */
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
	
	public int getVersionCode(){
		return this.versionCode;
	}
	
	public void setVersionCode(int versionCode){
		this.versionCode = versionCode;
	}
	
	/**
	 * 是否需要优化（优化即：推荐关闭的权限应该关闭，推荐打开的权限应该打开）
	 * @param isNeedOptimize true:表示需要优化
	 */
	public void setIsNeedOptimize (boolean isNeedOptimize){
		this.isNeedOptimize = isNeedOptimize;
	}
	
	/**
	 * 是否需要优化（优化即：推荐关闭的权限应该关闭，推荐打开的权限应该打开）
	 * @return isNeedOptimize true:表示需要优化
	 */
	/*public boolean getIsNeedOptimize(){
		if(!ApkUtils.isLBEServiceConnect()){
			return false;
		}
		return this.isNeedOptimize;
	}*/
	
	/**
	 * 是否有联网权限
	 * @param isHaveNetworkingPermission
	 */
	public void setIsHaveNetworkingPermission(boolean isHaveNetworkingPermission){
		this.isHaveNetworkingPermission = isHaveNetworkingPermission;
	}
	
	/**
	 * 是否有联网权限
	 * @return
	 */
	/*public boolean getIsHaveNetworkingPermission(){
		if(!ApkUtils.isLBEServiceConnect()){
			return false;
		}
		return this.isHaveNetworkingPermission;
	}*/
	
	/**
	 * 获取2G/3G联网权限
	 * @return
	 */
	/*public synchronized PermissionInfo getNetPermissionInfo(){
		if(!ApkUtils.isLBEServiceConnect()){
			return null;
		}
		
		PermissionInfo permissionInfo = null;
		int size = getPermission()==null?0:getPermission().size();
		for(int i=0;i<size;i++){
			PermissionInfo tmp = getPermission().get(i);
//			if(tmp != null && tmp.permId == SDKConstants.PERM_ID_NETDEFAULT){
//				permissionInfo = tmp;
//				break;
//			} modify by gaoming
		}
		return permissionInfo;
	}*/
	
	/**
	 * 获取WIFI联网权限
	 * @return
	 */
	/*public synchronized PermissionInfo getWifiPermissionInfo(){
		if(!ApkUtils.isLBEServiceConnect()){
			return null;
		}
		
		PermissionInfo permissionInfo = null;
		int size = getPermission()==null?0:getPermission().size();
		for(int i=0;i<size;i++){
			PermissionInfo tmp = getPermission().get(i);
//			if(tmp != null && tmp.permId == SDKConstants.PERM_ID_NETWIFI){
//				permissionInfo = tmp;
//				break;
//			}  modify by gaoming
		}
		return permissionInfo;
	}*/
	
	/**
	 * 判断2G/3G联网权限是否打开（前提是该应用申请了联网权限）
	 * @param context
	 * @param appInfo
	 * @return
	 */
	/*public synchronized boolean getIsNetPermissionOpen(){
		PermissionInfo permissionInfo = getNetPermissionInfo();
        if(permissionInfo == null){
        	return false;
        }else{
        	return permissionInfo.getIsOpen();
        }
	}*/
	
	/**
	 * apk消耗的总流量，（在使用的时候初始化）
	 * @param ApkTotalNetBytes
	 */
    public void setApkTotalNetBytes(long ApkTotalNetBytes){
    	this.ApkTotalNetBytes = ApkTotalNetBytes;
    }
    
    /**
     * apk消耗的总流量，（在使用的时候初始化）
     * @return
     */
    public long getApkTotalNetBytes(){
    	return this.ApkTotalNetBytes;
    }
    
	/**
	 * apk消耗的后台偷跑总流量，（在使用的时候初始化）
	 * @param ApkTotalNetBytes
	 */
    public void setApkTotalBackgroundNetBytes(long ApkTotalBackgroundNetBytes){
    	this.ApkTotalBackgroundNetBytes = ApkTotalBackgroundNetBytes;
    }
    
    /**
     * apk消耗的后台偷跑总流量，（在使用的时候初始化）
     * @return
     */
    public long getApkTotalBackgroundNetBytes(){
    	return this.ApkTotalBackgroundNetBytes;
    }
    
   /**
    * 针对sd可以插拔的手机，一个应用状态sd卡中，如果把sd热拔出，对应的应用状态就会变成没有安装
    * @param isInstalled
    */
    public void setIsInstalled(boolean isInstalled){
    	this.isInstalled = isInstalled;
    }
    
    /**
     * 针对sd可以插拔的手机，一个应用状态sd卡中，如果把sd热拔出，对应的应用状态就会变成没有安装
     * @return
     */
    public boolean getIsInstalled(){
    	return this.isInstalled;
    }
    
    /**
     * 是否是系统白名单应用
     * @param isSysWhiteApp
     */
    public void setIsSysWhiteApp(boolean isSysWhiteApp){
    	this.isSysWhiteApp = isSysWhiteApp;
    }
    
    /**
     * 是否是系统白名单应用
     * @return
     */
    public boolean getIsSysWhiteApp(){
    	return this.isSysWhiteApp;
    }
    
    /**
     * 是否是桌面应用（暂用于系统应用）
     * @return
     */
    public boolean isHome(){
    	return this.isHome;
    }
    
    /**
     * 是否是桌面应用（暂用于系统应用）
     * @param isHome
     */
    public void setIsHome(boolean isHome){
    	this.isHome = isHome;
    }
    
    /**
     * 是否有Launcher属性（暂用于系统应用）
     * @return
     */
    public boolean isHaveLauncher(){
    	return this.isHaveLauncher;
    }
    
    /**
     * 是否有Launcher属性（暂用于系统应用）
     * @param isHaveLauncher
     */
    public void setIsHaveLauncher(boolean isHaveLauncher){
    	this.isHaveLauncher = isHaveLauncher;
    }
    
    /**
     * 运行时，占用的内存大小
     * @param memorysize
     */
    public void setMemorySize(long memorysize){
    	this.memorysize = memorysize;
    }
    
    /**
     * 运行时，占用的内存大小
     * @return
     */
    public long getMemorySize(){
    	return this.memorysize;
    }
	
    /**
     * 是否拦截广告
     * @param isBlockAd
     */
	public void setIsBlockAd(boolean isBlockAd){
		this.isBlockAd = isBlockAd;
	}
	
	/**
	 * 是否拦截广告
	 * @return true:拦截；false：不拦截
	 */
	public boolean getIsBlockAd(){
		return this.isBlockAd;
	}
	
	public void setApplicationInfo(ApplicationInfo appInfo){
		this.appInfo = appInfo;
	}
	
	public ApplicationInfo getApplicationInfo(){
		return this.appInfo;
	}

	public boolean isStopAutoStart() {
		return isStopAutoStart;
	}

	public void setIsStopAutoStart(boolean isStopAutoStart) {
		this.isStopAutoStart = isStopAutoStart;
	}

	public boolean isStopWakeup() {
		return isStopWakeup;
	}

	public void setIsStopWakeup(boolean isStopWakeup) {
		this.isStopWakeup = isStopWakeup;
	}

	public boolean isAutoSleep() {
		return isAutoSleep;
	}

	public void setIsAutoSleep(boolean isAutoSleep) {
		this.isAutoSleep = isAutoSleep;
	}
}
