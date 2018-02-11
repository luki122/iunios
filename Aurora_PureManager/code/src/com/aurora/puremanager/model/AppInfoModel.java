package com.aurora.puremanager.model;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telecom.Log;

import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.AppsInfo;
import com.aurora.puremanager.data.Constants;
//import com.aurora.puremanager.interfaces.PermissionSubject;
import com.aurora.puremanager.interfaces.Subject;
import com.aurora.puremanager.provider.AuroraAppInfosProvider;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.LogUtils;
import com.aurora.puremanager.utils.StringUtils;

/**
 * 获取应用属性
 */
public class AppInfoModel extends Subject{
	private AppInfoProvider mPakageInfoProvider;
	private Context context;
	private GetAllAppInfoHandler getAllAppInfoHandler;
	private boolean isStopThread;
	private List <UpdateData> updateAppList;
	
	/**
	 * 获取应用属性
	 * @param context
	 */	
	public AppInfoModel(Context context){
		this.context = context;
		mPakageInfoProvider = new AppInfoProvider(context);
		getAllAppInfoHandler = new GetAllAppInfoHandler(Looper.getMainLooper());
		isStopThread = false;
		updateAppList = new ArrayList<UpdateData>();
//	    PermissionSubject.getInstance();
		getAllAppInfoFunc();
	}
	
	/**
	 * 加载所有应用的信息
	 */
	public void wantGetAllAppInfo(){
		if(context != null){	
			UpdateData updateData = new UpdateData(UpdateData.GET_ALL_APP_INFO);
			updateData.setPackageInfos(context.getPackageManager().getInstalledPackages(0));
			addUpdateData(updateData);	
		}		
	} 
	
	/**
	 * 当安装一个应用时，调用这个接口更新应用数据
	 * @param packageName
	 */
	public void installOrCoverPackage(String pkgName){
		if(pkgName == null || StringUtils.isEmpty(pkgName)){
			return ;
		}
		List<String> pkgList = new ArrayList<String>();
		pkgList.add(pkgName);
		UpdateData updateData = new UpdateData(UpdateData.INSTALLER_APP);
		updateData.setPkgNameList(pkgList);
		addUpdateData(updateData);	
	}
	
	/**
	 * 当卸载一个应用时，调用这个接口更新应用数据
	 * @param packageName
	 */
	public void UninstallPackage(String pkgName){
		if(pkgName == null || StringUtils.isEmpty(pkgName)){
			return ;
		}
		List<String> pkgList = new ArrayList<String>();
		pkgList.add(pkgName);
		
		UpdateData updateData = new UpdateData(UpdateData.UNINSTALLER_APP);
		updateData.setPkgNameList(pkgList);
		addUpdateData(updateData);	
	}
	
	/**
	 * 安装在sd卡中的应用变的可用
	 * @param pkgList
	 */
	public void externalAppAvailable(List<String> pkgList){
		UpdateData updateData = new UpdateData(UpdateData.EXTERNAL_APP_AVAILABLE);
		updateData.setPkgNameList(pkgList);
		addUpdateData(updateData);	
	}
	
	/**
	 * 安装在sd卡中的应用变的不可用
	 * @param pkgList
	 */
	public void externalAppUnAvailable(List<String> pkgList){
		UpdateData updateData = new UpdateData(UpdateData.EXTERNAL_APP_NOT_AVAILABLE);
		updateData.setPkgNameList(pkgList);
		addUpdateData(updateData);
	}
	
	private void addUpdateData(UpdateData updateData){
		synchronized(updateAppList){			 
			updateAppList.add(updateData);
			LogUtils.printWithLogCat(AppInfoModel.class.getName(), 
					"Count="+updateAppList.size()+",type="+updateData.getType());
			if(updateAppList.size()>0){
				updateAppList.notify();
			}
		}
	}
		
	private void getAllAppInfoFunc(){
		new Thread() {
			@Override
			public void run() {
				UpdateData updateData = null;
				while(!isStopThread){					
					synchronized (updateAppList) {
	                    if (updateAppList.size() == 0) {
	                    	try{
	                    		LogUtils.printWithLogCat(AppInfoModel.class.getName(), 
	                    				"updateAppList.wait()");
	                    		updateAppList.wait();                 		
	                    	}catch(Exception e){
	                    		e.printStackTrace();
	                    	}               	
	                    }
	                    updateData = updateAppList.get(0);
	                }
					
					if(updateData.getType() == UpdateData.GET_ALL_APP_INFO){
						mPakageInfoProvider.initAllAppsInfo(updateData.getPackageInfos());
					}else if(updateData.getType() == UpdateData.INSTALLER_APP){
						mPakageInfoProvider.addAppsInfo(updateData.getPkgNameList());
					}else if(updateData.getType() == UpdateData.UNINSTALLER_APP){
						mPakageInfoProvider.removeAppsInfo(updateData.getPkgNameList());
					}else if(updateData.getType() == UpdateData.EXTERNAL_APP_AVAILABLE){
						mPakageInfoProvider.addAppsInfo(updateData.getPkgNameList());
					}else if(updateData.getType() == UpdateData.EXTERNAL_APP_NOT_AVAILABLE){
						mPakageInfoProvider.removeAppsInfo(updateData.getPkgNameList());
					}
					
					Message msg = new Message();
					msg.obj = updateData;
					getAllAppInfoHandler.sendMessage(msg);	
					
					synchronized (updateAppList) {
						LogUtils.printWithLogCat(AppInfoModel.class.getName(), 
								"updateAppInfoFunc,Count="+updateAppList.size());
						updateAppList.remove(updateData);
					}
			    }
			}
		}.start();	
	}
	
	private class GetAllAppInfoHandler extends Handler{		
		public GetAllAppInfoHandler(Looper looper){
           super(looper);
        }

		@Override
	    public void handleMessage(Message msg) { 
			UpdateData updateData = (UpdateData)msg.obj;
			if(updateData == null){
				return ;
			}
			
			if(updateData.getType() == UpdateData.GET_ALL_APP_INFO){
				mPakageInfoProvider.getSysAppsInfo();
			    mPakageInfoProvider.getThirdPartyAppsInfo();
			    if(mPakageInfoProvider.getTotalAppNum()>0){
				   AuroraAppInfosProvider.notifyChangeForNetManageApp(context,null);	  
				   notifyObserversOfInit();
			    }
			}else if(updateData.getType() == UpdateData.INSTALLER_APP){
				AppInfo appInfo = findAppInfo(updateData.getPkgNameList().get(0));
				if(appInfo != null){	    
				    AuroraAppInfosProvider.notifyChangeForNetManageApp(context, appInfo);		
				}	
				notifyObserversOfInStall(updateData.getPkgNameList().get(0));
			}else if(updateData.getType() == UpdateData.UNINSTALLER_APP){
				notifyObserversOfUnInstall(updateData.getPkgNameList().get(0));
			}else if(updateData.getType() == UpdateData.EXTERNAL_APP_AVAILABLE){
				AuroraAppInfosProvider.notifyChangeForNetManageApp(context,null);	
				notifyObserversOfExternalAppAvailable(updateData.getPkgNameList());
			}else if(updateData.getType() == UpdateData.EXTERNAL_APP_NOT_AVAILABLE){
				AuroraAppInfosProvider.notifyChangeForNetManageApp(context,null);	
				notifyObserversOfExternalAppUnAvailable(updateData.getPkgNameList());
			}						
	    }
	}
	
	/**
	 * 查找这个应用的对应数据
	 * @param appsInfo
	 * @param packageName
	 * @return
	 */
	public AppInfo findAppInfo(String packageName){		
		return findAppInfo(packageName,
				ApkUtils.isUserApp(ApkUtils.getApplicationInfo(context, packageName))&&(!packageName.equals("com.baidu.map.location")));
	}
	
	/**
	 * 查找这个应用的对应数据
	 * @param appsInfo
	 * @param packageName
	 * @return
	 */
	public AppInfo findAppInfo(String packageName,boolean isUserApp){
		AppInfo appInfo = null;
		if(isUserApp){
			appInfo = getAppInfo(mPakageInfoProvider.getThirdPartyAppsInfo(),packageName);
		}else{
			appInfo = getAppInfo(mPakageInfoProvider.getSysAppsInfo(),packageName);
		}
		
		if(appInfo == null){
			//表示此时应用信息还没有加载，则先加载再获取
			ArrayList<String> pkgList = new ArrayList<String>();
			pkgList.add(packageName);
			mPakageInfoProvider.addAppsInfo(pkgList);
			if(isUserApp){
				appInfo = getAppInfo(mPakageInfoProvider.getThirdPartyAppsInfo(),packageName);
			}else{
				appInfo = getAppInfo(mPakageInfoProvider.getSysAppsInfo(),packageName);
			}
		}
		return appInfo;
	}
		
	/**
	 * 获取AppInfo
	 * @param appsInfo
	 * @param packageName
	 * @return
	 */
	private AppInfo getAppInfo(AppsInfo appsInfo,String packageName){
		if(appsInfo == null || packageName == null){
			return null;
		}
		
		for(int i=0;i<appsInfo.size();i++){
			AppInfo appInfo = (AppInfo)appsInfo.get(i);
			if(appInfo == null){
				continue;
			}
			if(packageName.equals(appInfo.getPackageName())){
				return appInfo;
			}
		}
		return null;	
	}
	
	/**
	 * LBE推荐的权限配置更新
	 */
	public void recomPermsChange(){
		resetNeedOptimizeData();
		notifyObserversOfRecomPermsChange();
	}
	
	/**
	 * 重置应用是否需要优化的相关数据
	 */
	private void resetNeedOptimizeData(){
		for(int i=0;i<mPakageInfoProvider.getThirdPartyAppsInfo().size();i++){
			AppInfo appInfo = (AppInfo)mPakageInfoProvider.getThirdPartyAppsInfo().get(i);
			if(appInfo == null){
				continue;
			}
//			if(ApkUtils.isNeedOptimize(context,appInfo)){
// 				appInfo.setIsNeedOptimize(true);
// 			}else{
// 				appInfo.setIsNeedOptimize(false); 
// 			}
		}
	}
	
	/**
	 * 之前是否已经获取了所有应用的信息
	 * @return true：之前已经获取； false：之前没有获取
	 */
	public boolean isAlreadyGetAllAppInfo(){	
		int appNumOfAlreadyGet = mPakageInfoProvider.getSysAppsInfo().size() +
				mPakageInfoProvider.getAllThirdPartyAppsInfo().size();
//		int appNumOfAlreadyGet = loadApp(context) + loadThirdApp(context);
		
		int appNumOfAll = context.getPackageManager().getInstalledPackages(0).size();
		if(appNumOfAlreadyGet == appNumOfAll){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 当前是不是正在获取所有应用的信息
	 * @return true：正在获取； false：没有获取
	 */
	public boolean isDuringGetAllAppInfo(){		
		synchronized (updateAppList) {
			if(updateAppList.size()>0){
				return true;
			}else{
				return false;
			}
		}
	}
	
	/**
	 * 第三方应用总数
	 * @return
	 */
	public int getUserAppsNum(){
		int num = 0;
		for(int i=0;i<mPakageInfoProvider.getThirdPartyAppsInfo().size();i++){
    		AppInfo appInfo = (AppInfo)mPakageInfoProvider.getThirdPartyAppsInfo().get(i);
    		if(appInfo == null || !appInfo.getIsInstalled()){
    			continue;
    		}
    		num++;
    	}
		return num;	
	}
	
	/**
	 * 系统应用总数
	 * @return
	 */
	public int getSysAppsNum(){
		int num = 0;
		for(int i=0;i<mPakageInfoProvider.getSysAppsInfo().size();i++){
			AppInfo appInfo = (AppInfo)mPakageInfoProvider.getSysAppsInfo().get(i);
			if(appInfo != null && 
				(appInfo.isHaveLauncher() ||
						appInfo.isHome()  ||
            			Constants.isPackageNameInList(Constants.showInSysAppList, 
            					appInfo.getPackageName()))
					){
				num++;
			}				
		}
		return num;
	}
	
	/**
	 * 系统级应用
	 * @return
	 */
	public AppsInfo getSysAppsInfo(){
		return mPakageInfoProvider.getSysAppsInfo();
	}
	
	/**
	 * 第三方应用
	 * @return 返回值不会为null
	 */
	public AppsInfo getThirdPartyAppsInfo(){
		return mPakageInfoProvider.getThirdPartyAppsInfo();
	}	
	
	public void releaseObject(){
		mPakageInfoProvider.getSysAppsInfo().releaseObject();
		mPakageInfoProvider.getThirdPartyAppsInfo().releaseObject();
		isStopThread = true;
	}	
	
	class UpdateData{
		public static final int GET_ALL_APP_INFO = 1;
		public static final int INSTALLER_APP = 2;
		public static final int UNINSTALLER_APP = 3;
		public static final int EXTERNAL_APP_AVAILABLE = 4;
		public static final int EXTERNAL_APP_NOT_AVAILABLE = 5;
		
		int type;
		List<PackageInfo> PackageInfos;
		List<String> pkgNameList;

		public UpdateData(int type){
			this.type = type;
		}
		
		/**
		 * 设置从PackageManager中获取的所有应用的信息
		 * @param PackageInfos
		 */
		public void setPackageInfos(List<PackageInfo> PackageInfos){
			this.PackageInfos = PackageInfos;
		}
		
		
		public int getType(){
			return this.type;
		}
		
		/**
		 * 设置从PackageManager中获取的所有应用的信息
		 * @return
		 */
		public List<PackageInfo> getPackageInfos(){
			return this.PackageInfos;
		}
		
		public void setPkgNameList(List<String> pkgNameList){
			this.pkgNameList = pkgNameList;
		}
		
		public List<String> getPkgNameList(){
			return this.pkgNameList;
		}
	}
	
	/***
	 * 获取系统应用列表
	 */
	/*private int  loadApp(Context context) {
		int appSize = 0;
		PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
		for (int i = 0; i < packageInfoList.size(); i++) {
			PackageInfo pak = (PackageInfo) packageInfoList.get(i);
			// 判断是否为系统预装的应用
			if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) > 0) {
				appSize++;
				 String[] mPermission;
				// 第三方应用
				String packagename = pak.applicationInfo.packageName;
				String name = packageManager.getApplicationLabel(pak.applicationInfo).toString();
				Drawable icon = packageManager.getApplicationIcon(pak.applicationInfo);
			} 
		}
		return appSize;
	}*/
	
	/***
	 * 获取第三方应用列表
	 */
	/*private int  loadThirdApp(Context context) {
		int appSize = 0;
		PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
		for (int i = 0; i < packageInfoList.size(); i++) {
			PackageInfo pak = (PackageInfo) packageInfoList.get(i);
			// 判断是否为系统预装的应用
			if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0) {
				appSize++;
				 String[] mPermission;
				// 第三方应用
				String packagename = pak.applicationInfo.packageName;
				String name = packageManager.getApplicationLabel(pak.applicationInfo).toString();
				Drawable icon = packageManager.getApplicationIcon(pak.applicationInfo);
			} 
		}
		return appSize;
	}*/
}
