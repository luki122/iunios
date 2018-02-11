package com.secure.model;

import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.AutoStartData;
import com.secure.data.MyArrayList;
import com.secure.receive.BootReceiver;
import com.secure.utils.LogUtils;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;

/**
 * 常驻进程应用的管理
 */
public class PersistentModel{
	private static PersistentModel instance;
	private MyArrayList<String> persistentAppList;
	private boolean isNeedDealedPersiApp;//标记是否需要处理常驻进程的应用（在系统启动后需要处理）
	private Object lock = new Object();
	private final String TAG = PersistentModel.class.getName();
	
	/**
	 * 必须在UI线程中初始化,如果instance为null，则会创建一个
	 * @param context
	 * @return
	 */
	public static synchronized PersistentModel getInstance() {
		if (instance == null) {
			instance = new PersistentModel();
		}
		return instance;
	}
	
	private PersistentModel(){
		persistentAppList = new MyArrayList<String>();
		isNeedDealedPersiApp = false;
	}
	
	/**
	 * 系统开机后，调用该方法初始化一些数据
	 */
	public void systemBoot(){
		synchronized (lock) {
			isNeedDealedPersiApp = true;
		}
	}
	
	/**
	 * 获取开机后有没有处理常驻进程的应用
	 * @return true:表示需要处理
	 *         false：表示不需要处理
	 */
	private boolean getIsNeedDealedPersiApp(){
		synchronized (lock) {
			return this.isNeedDealedPersiApp;
		}	
	}
	
	/**
	 * 处理常驻进程的三方应用
	 * @param context
	 */
	public void dealPersistentApp(Context context){
		if(!getIsNeedDealedPersiApp() || context == null){
			return ;
		}
		final ActivityManager activityManager = 
				(ActivityManager)context.getApplicationContext().
				getSystemService(Context.ACTIVITY_SERVICE);
		new Thread() {
			@Override
			public void run() { 
				collectPersistentApp();
				stopPersistentApp(activityManager);
				persistentAppList.clear();//释放资源
				synchronized (lock) {
					isNeedDealedPersiApp = false;
				}
			}
		}.start();	
	}
	
	/**
	 * 收集常驻进程的应用
	 */
	private void collectPersistentApp(){
		persistentAppList.clear();
		ConfigModel instance = ConfigModel.getInstance();
		if(instance == null){
			return ;
		}
		AppsInfo userAppsInfo = instance.getAppInfoModel().getThirdPartyAppsInfo();
    	int size = userAppsInfo == null?0:userAppsInfo.size(); 	
    	for(int i=0;i<size;i++){
    		AppInfo appInfo = (AppInfo)userAppsInfo.get(i);
    		if(appInfo == null || 
    				!appInfo.getIsInstalled()){
    			continue;
    		}
    		if(isPersistentApp(appInfo.getApplicationInfo())){
    			persistentAppList.add(appInfo.getPackageName());
    			LogUtils.printWithLogCat(TAG, "pkg:"+appInfo.getPackageName()+" is persistent app");
    		}		
    	}
	}
	
	/**
	 * 停止常驻进程的应用
	 * @param activityManager
	 */
	private void stopPersistentApp(ActivityManager activityManager){
		AutoStartModel instance = AutoStartModel.getInstance();
		for(int i=0;i<persistentAppList.size();i++){
			String pkgName = persistentAppList.get(i);
			if(pkgName == null){
				continue ;
			}
			AutoStartData autoStartData;
			if(instance != null){
				autoStartData = instance.getAutoStartData(pkgName);
				if(autoStartData != null && autoStartData.getIsOpen()){
	    			continue ;
	    		}
			}
			LogUtils.printWithLogCat(TAG, "pkg:"+pkgName+" is stoped ");
			try{
				activityManager.forceStopPackage(pkgName);	
			}catch(Exception e){
				e.printStackTrace();
			}				
		}
	}	
	
	/**
	 * 判断一个应用是不是常驻进程的应用
	 * @param info
	 * @return
	 */
	private boolean isPersistentApp(ApplicationInfo info){
		if(info == null){
			return false;
		}
		if((info.flags & ApplicationInfo.FLAG_PERSISTENT) != 0){
		   //表示persistent被设置成了true
			return true;
		}else{
		   //表示persistent被设置成了false，或者没有设置
			return false;
		}
	}
}
