package com.secure.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.utils.ApkUtils;
import com.secure.utils.LogUtils;

import android.content.Context;
import android.content.pm.PackageStats;
import android.content.pm.IPackageStatsObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

/**
 * @author Administrator
 *
 */
public class CacheSizeModel {   
	private static CacheSizeModel instance;
	private final int HANDLER_TYPE_OF_GET_SIZE = 1;
	private final int HANDLER_TYPE_OF_RESULT = 2;
	private Context context;
	private HashMap<String ,PackageStats> packageStatsMap = null; 
	private PkgSizeObserver observer;
	private Handler callBackHandler;
	private List<String> needGetSizeAppList;
	private boolean isStopThread;
	private AtomicBoolean isCanGetNextSize ;	
	private MainUIHandler mainUIHandler;
	private AtomicBoolean isRunInitAllApkCacheSize ;	
	
	private CacheSizeModel(Context context) {
		this.context = context.getApplicationContext();
		packageStatsMap = new HashMap<String ,PackageStats> ();
		observer = new PkgSizeObserver();
		needGetSizeAppList = new ArrayList<String>();
		isStopThread = false;
		isCanGetNextSize = new AtomicBoolean(true);
		isRunInitAllApkCacheSize = new AtomicBoolean(false);
		mainUIHandler = new MainUIHandler(Looper.getMainLooper());
		getAppsSizeFunc();
	}

	/**
	 * 如果instance为null，不会创建
	 * @return
	 */
	public static synchronized CacheSizeModel getInstance() {
		return instance;
	}
	
	/**
	 * 如果instance为null，则会创建一个
	 * @param context
	 * @return
	 */
	public static synchronized CacheSizeModel getInstance(Context context) {
		if (instance == null) {
			instance = new CacheSizeModel(context);
		}
		return instance;
	}
	
	/**
	 * 设置更新应用缓存的回调Handler
	 * @param callBackHandler
	 */
	public void setCallBackHandler(Handler callBackHandler){
		this.callBackHandler = callBackHandler;
	}
	
	/**
	 * 当前是不是正在获取应用缓存
	 * @return
	 */
	public boolean getIsDuringGetCacheSize(){
		synchronized (needGetSizeAppList){
			if(needGetSizeAppList.size() == 0){
				return false;
			}else{
				return true;
			}
		}		
	}
	
	/**
	 * 获取保存有应用缓存的map
	 * @return 注意：返回值不可能为null
	 */
	public HashMap<String ,PackageStats> getPackageStatsMap(){
		return this.packageStatsMap;
	}
	
	/**
	 * 是否需要初始化
	 * @return
	 */
	public boolean isNeedInit(){
		synchronized (packageStatsMap){
			if(packageStatsMap.size()>0 && isRunInitAllApkCacheSize.get()){
				return false;
			}else{
				return true;
			}
		} 		
	}
	
	/**
	 * 更新所有应用的缓存
	 */
	public void initAllApkCacheSize(){
		isRunInitAllApkCacheSize.set(true);
		AppsInfo userAppsInfo = ConfigModel.getInstance(context).getAppInfoModel().getThirdPartyAppsInfo();
    	if(userAppsInfo != null){
    		for(int i=0;i<userAppsInfo.size();i++){
        		AppInfo appInfo = (AppInfo)userAppsInfo.get(i);
        		if(appInfo == null || !appInfo.getIsInstalled()){
        			continue;
        		}
        		addNeedGetSizeData(appInfo.getPackageName());
        	}
    	}
    	
    	AppsInfo sysAppsInfo = ConfigModel.getInstance(context).getAppInfoModel().getSysAppsInfo();
    	if(sysAppsInfo != null){
    		for(int i=0;i<sysAppsInfo.size();i++){
        		AppInfo appInfo = (AppInfo)sysAppsInfo.get(i);
        		if(appInfo == null || !appInfo.getIsInstalled() || !appInfo.getIsSysWhiteApp()){
        			continue;
        		}
        		addNeedGetSizeData(appInfo.getPackageName());
        	}
    	}	
	}
	
	/**
	 * 处理安装app的情况
	 * @param appInfo
	 */
	public void dealInstallApp(AppInfo appInfo){
		if(appInfo != null){
			addNeedGetSizeData(appInfo.getPackageName());
		}
	}
	
	/**
	 * 处理覆盖安装app的情况
	 * @param appInfo
	 */
	public void dealCoverInstallApp(AppInfo appInfo){		
		if(appInfo != null){
			addNeedGetSizeData(appInfo.getPackageName());
		}
	}
	
	/**
	 * 处理删除app的情况
	 */
	public void dealUninstallApp(String pkgName){
		if(packageStatsMap == null || pkgName == null){
			return ;
		}
		
		synchronized (packageStatsMap){
			if(packageStatsMap.containsKey(pkgName)){
				packageStatsMap.remove(pkgName);
			}
		}
		
		if(callBackHandler != null){
			callBackHandler.sendEmptyMessage(0);
		}	
	}
	
	/**
	 * 安装在sd卡中的应用变的可用
	 * @param pkgList
	 */
	public void externalAppAvailable(List<String> pkgList){
		if(pkgList != null){
			for(int i=0;i<pkgList.size();i++){
				addNeedGetSizeData(pkgList.get(i));
			}
		}
	}
	
	/**
	 * 安装在sd卡中的应用变的不可用
	 * @param pkgList
	 */
	public void externalAppUnAvailable(List<String> pkgList){
		if(pkgList == null || 
				packageStatsMap == null){
			return ;
		}
			
        for(int i=0;i<pkgList.size();i++){
        	synchronized (packageStatsMap){
            	if(packageStatsMap.containsKey(pkgList.get(i))){
    				packageStatsMap.remove(pkgList.get(i));
    			}
        	}
		}					
		
		if(callBackHandler != null){
			callBackHandler.sendEmptyMessage(0);
		}
	}
	
	private void addNeedGetSizeData(String pkg){
		if(pkg != null){
			synchronized(needGetSizeAppList){			 
				needGetSizeAppList.add(pkg);
				if(needGetSizeAppList.size()>0){
					needGetSizeAppList.notify();
				}
			}
		}		
	}
	
	private void deleteNeedGetSizeData(String pkg){
		if(pkg != null){
			synchronized (needGetSizeAppList) {
				needGetSizeAppList.remove(pkg);
				if(needGetSizeAppList.size() == 0){
					if(callBackHandler != null){
						callBackHandler.sendEmptyMessage(0);
					}
				}
			}
		}				
	}
	
	/**
	 * 获取应用的空间信息
	 */
	private void getAppsSizeFunc(){		
		new Thread() {
			@Override
			public void run() {
				String packageName ;
				while(!isStopThread){									
					if (!isCanGetNextSize.get()) {
		            	try{
		            		synchronized (isCanGetNextSize) {
		            			LogUtils.printWithLogCat(
		            					CacheSizeModel.class.getName(),
		            					"isCanGetNextSize.wait()");
			            		isCanGetNextSize.wait();  
		            		}            		               		
		            	}catch(Exception e){
		            		e.printStackTrace();
		            	}    
		            }
					
					synchronized (needGetSizeAppList) {
			            if (needGetSizeAppList.size() == 0) {
			            	try{
			            		LogUtils.printWithLogCat(
			            				CacheSizeModel.class.getName(),
		            					"needGetSizeAppList.wait()");
			            		needGetSizeAppList.wait();                 		
			            	}catch(Exception e){
			            		e.printStackTrace();
			            	}    
			            }		            
			        }
					packageName = needGetSizeAppList.get(0);
					
					LogUtils.printWithLogCat(
							CacheSizeModel.class.getName(),
        					"getAppsSizeFunc pkg ="+packageName);
					
					if(ApkUtils.isAppInstalled(context, packageName)){
						isCanGetNextSize.set(false);
						Message msg = new Message();
						msg.obj = packageName;
						msg.what = HANDLER_TYPE_OF_GET_SIZE;
						mainUIHandler.sendMessage(msg);
					}else{
						deleteNeedGetSizeData(packageName);
					}
			    }
			}
		}.start();	
	}
	
	public class PkgSizeObserver extends IPackageStatsObserver.Stub{
		@Override
		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
				throws RemoteException {
			 ApkUtils.dealPackageStats(pStats);
			 Message msg = new Message();
        	 msg.obj = pStats;
        	 msg.what = HANDLER_TYPE_OF_RESULT;
        	 mainUIHandler.sendMessage(msg);			
		}
	}
	
	private class MainUIHandler extends Handler{		
		public MainUIHandler(Looper looper){
           super(looper);
        }

		@Override
	    public void handleMessage(Message msg) {
			switch(msg.what){
			case HANDLER_TYPE_OF_GET_SIZE:
				ApkUtils.queryPacakgeSize(context, (String)msg.obj, observer);
				break;
			case HANDLER_TYPE_OF_RESULT:
				PackageStats pStats = (PackageStats)msg.obj;
				synchronized (packageStatsMap) {
					packageStatsMap.put(pStats.packageName, pStats);
				}
				deleteNeedGetSizeData(pStats.packageName);	
				isCanGetNextSize.set(true);
				synchronized (isCanGetNextSize) {
					if(isCanGetNextSize.get()){
						isCanGetNextSize.notify();
					}
				}
				break;
			}		
	    }
	}
		
	/**
	 * 更新指定app的缓存信息
	 * @param appInfo
	 * @param pStats
	 */
	public void updateAppCache(String pkgName,PackageStats pStats){
		if(pkgName == null || 
				pStats == null ||
				packageStatsMap == null){
			return ;
		}
			
		synchronized (packageStatsMap) {
			if(packageStatsMap.containsKey(pkgName)){
				packageStatsMap.put(pkgName, pStats);
			}
		}				
	}
	
	public static void releaseObject(){
		if(instance != null){
			synchronized (instance.packageStatsMap) {
				instance.packageStatsMap.clear();
			}			
			instance.isStopThread = true;
		}
		instance = null;
	}
}
