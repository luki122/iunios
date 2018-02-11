package com.secure.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import com.privacymanage.data.AidlAccountData;
import com.secure.activity.NetForbidHintActivity;
import com.secure.data.AppInfo;
import com.secure.data.AutoStartData;
import com.secure.data.MyArrayList;
import com.secure.data.NetForbidHintData;
import com.secure.data.PermissionInfo;
import com.secure.data.PrivacyAppData;
import com.secure.interfaces.Observer;
import com.secure.interfaces.PrivacyAppObserver;
import com.secure.interfaces.PrivacyAppSubject;
import com.secure.interfaces.Subject;
import com.secure.model.AdScanModel;
import com.secure.model.AppInfoModel;
import com.secure.model.AuroraPrivacyManageModel;
import com.secure.model.AutoStartModel;
import com.secure.model.CacheSizeModel;
import com.secure.model.ConfigModel;
import com.secure.model.DefSoftModel;
import com.secure.model.GetRecomPermsModel;
import com.secure.model.LBEmodel;
import com.secure.model.LBEmodel.BindServiceCallback;
import com.secure.model.NetForbidHintModel;
import com.secure.utils.ApkUtils;
import com.secure.utils.LogUtils;
import com.secure.utils.NetworkUtils;
import com.secure.utils.StringUtils;
import com.secure.utils.mConfig;
import com.secure.view.OptimizeNotification;

import java.util.HashMap;
import java.util.List;

public class WatchDogService extends Service implements Observer,PrivacyAppObserver{
    private static final String TAG = "WatchDogService";
    
	private ActivityManager mActivityManager;
	private boolean isFromBootReceiver = false;
	private String installPkgName = null;
	
	@Override
	public void onCreate() {
		LogUtils.printWithLogCat(
				WatchDogService.class.getName(), 
				"onCreate");
		isFromBootReceiver = false;
		ConfigModel.getInstance(this).getAppInfoModel().attach(this);
		AuroraPrivacyManageModel.getInstance(this).attach(this);
		try {
			ActivityManagerNative.getDefault().registerProcessObserver(mProcessObserver);
        } catch (Exception e) { }
		super.onCreate();		 
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		LogUtils.printWithLogCat(
				WatchDogService.class.getName(), 
				"onStart");
		if(intent != null && intent.getExtras() != null){
			if("BootReceiver".equals(intent.getExtras().getString("come_from"))){
				isFromBootReceiver = true;
			}
		}
		
		sureBindLBEService();
		super.onStart(intent, startId);
	}
	
	/**
	 * 确保绑定lbe的服务
	 */
	private void sureBindLBEService(){
		if(LBEmodel.getInstance(this).isBindLBEService()){
			loadData();
		}else{
			LBEmodel.getInstance(this).bindLBEService(new LBEmodel.BindServiceCallback(){
				@Override
				public void callback(boolean result) {
					loadData();				
				}}
			);
		} 
	}

	private void loadData(){
		//在这里加载数据，是为了保证数据的长久有效			
		if(ConfigModel.getInstance(this).getAppInfoModel().isAlreadyGetAllAppInfo()){
		    updateOfInit(ConfigModel.getInstance(this).getAppInfoModel());
	    }else{
	    	if(!ConfigModel.getInstance(this).getAppInfoModel().isDuringGetAllAppInfo()){
	    		ConfigModel.getInstance(this).getAppInfoModel().wantGetAllAppInfo(); 	    		
	    	}		    
	    }
	}
		
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		try {
		  ActivityManagerNative.getDefault().unregisterProcessObserver(mProcessObserver);
		} catch (Exception e) { }
		AuroraPrivacyManageModel.getInstance(this).detach(this);
		ConfigModel.getInstance(this).getAppInfoModel().detach(this);
		super.onDestroy();
	}
	
	@Override
	public void updateOfInit(Subject subject) {
		LogUtils.printWithLogCat(
				WatchDogService.class.getName(), 
				"updateOfInit");
		
		if(DefSoftModel.getInstance(this).isNeedInit()){
			DefSoftModel.getInstance(this).initOrUpdateThread();
		}
		
		GetRecomPermsModel.getInstance(this).applicationStart();
		
		AutoStartModel.getInstance(this).applicationStart();	//change 20140215
		
		AdScanModel.getInstance(this);
	}

	@Override
	public void updateOfInStall(Subject subject, String pkgName) {
		LogUtils.printWithLogCat(
				WatchDogService.class.getName(), 
				"updateOfInStall");
		installPkgName = pkgName;
		
		LogUtils.printWithLogCat(TAG, "Jim, updateOfInStall, pkgName: " + pkgName);
		AppInfo appInfo = ConfigModel.getInstance(this).getAppInfoModel().findAppInfo(pkgName);
		if (appInfo == null) {
		    Log.e(TAG, "Jim, appInfo for package: " + pkgName + " is null.");
		}
		 
		if(DefSoftModel.getInstance() != null){
			 DefSoftModel.getInstance(this).initOrUpdateThread();
		}
		
		if(CacheSizeModel.getInstance() != null){
			CacheSizeModel.getInstance(this).dealInstallApp(appInfo);
		} 
		
		if(GetRecomPermsModel.getInstance() != null){
			GetRecomPermsModel.getInstance(this).inStallApp(appInfo);
		}
		
		if(AutoStartModel.getInstance() != null){
			AutoStartModel.getInstance(this).inStallApp(appInfo);
		}else{
			//因为对新安装的应用要配置自启动的属性，所以必须调用，调用完以后再清空该对象
			AutoStartModel.getInstance(this).inStallApp(appInfo);
			AutoStartModel.releaseObject();
		}
		
		AdScanModel.getInstance(this).scanForInStall(pkgName);
	}
	
	@Override
	public void updateOfCoverInStall(Subject subject, String pkgName) {
		LogUtils.printWithLogCat(
				WatchDogService.class.getName(), 
				"updateOfCoverInStall");
		AppInfo appInfo = ConfigModel.getInstance(this).getAppInfoModel().findAppInfo(pkgName);
		if(DefSoftModel.getInstance() != null){
			 DefSoftModel.getInstance(this).initOrUpdateThread();
		}
		
		if(CacheSizeModel.getInstance() != null){
			CacheSizeModel.getInstance(this).dealCoverInstallApp(appInfo);
		} 
		
		if(AutoStartModel.getInstance() != null){
			AutoStartModel.getInstance(this).coverInStallApp(appInfo);
		}else{
			//因为对新安装的应用要配置自启动的属性，所以必须调用，调用完以后再清空该对象
			AutoStartModel.getInstance(this).coverInStallApp(appInfo);
			AutoStartModel.releaseObject();
		}
		
		AdScanModel.getInstance(this).scanForCoverInStall(pkgName);
	}

	@Override
	public void updateOfUnInstall(Subject subject, String pkgName) {	
		if(DefSoftModel.getInstance() != null){
			DefSoftModel.getInstance(this).initOrUpdateThread();
		}
		
		if(CacheSizeModel.getInstance() != null){
			CacheSizeModel.getInstance(this).dealUninstallApp(pkgName);
		}
	
        if(GetRecomPermsModel.getInstance() != null){
        	GetRecomPermsModel.getInstance(this).unInStallApp(pkgName);
		}
        if(AutoStartModel.getInstance() != null){
        	AutoStartModel.getInstance(this).unInStallApp(pkgName);	
		}	
        
        AdScanModel.getInstance(this).scanForUnInstall(pkgName);
        
        if(pkgName != null && !ApkUtils.canFindAppInfo(this, pkgName)){
        	LogUtils.printWithLogCat(
        			WatchDogService.class.getName(),"UnInstall pkg="+pkgName);
        	/**
        	 * 由于卸载和覆盖安装都会调用updateOfUnInstall()，
        	 * 所以此处要判断该应用是否真的卸载了。
        	 * 如果是真的卸载，才执行删除隐私应用的逻辑
        	 */
            MyArrayList<String> pkgList = new MyArrayList<String>();
            pkgList.add(pkgName);
            AuroraPrivacyManageModel.getInstance(this).dealUnInstallApp(pkgList);
        }
	}

	@Override
	public void updateOfRecomPermsChange(Subject subject) {
		LogUtils.printWithLogCat(
				WatchDogService.class.getName(), 
				"updateOfRecomPermsChange");
		AppInfoModel appInfoModel = (AppInfoModel)subject;
		if(appInfoModel != null){
			if(isFromBootReceiver){//Service是不是刚刚创建
				if(appInfoModel.getThirdPartyAppsInfo().getDangerAppsNum()>0){
					OptimizeNotification.notify(this);
				}
			}else if(installPkgName != null){//是不是刚刚安装一个应用
				AppInfo appInfo = appInfoModel.findAppInfo(installPkgName);
				if(appInfo != null && appInfo.getIsNeedOptimize()){
					OptimizeNotification.notify(this);
				}		
			}
		}		
		isFromBootReceiver = false;
		installPkgName = null;
	}
	
	@Override
	public void updateOfExternalAppAvailable(Subject subject,List<String> pkgList) {
		DefSoftModel.getInstance(this).initOrUpdateThread();	
		if(CacheSizeModel.getInstance() != null){
			CacheSizeModel.getInstance(this).externalAppAvailable(pkgList);	
		}				
		AutoStartModel.getInstance(this).externalAppAvailable(pkgList);	
		AdScanModel.getInstance(this).scanForExternalAppAvailable(pkgList);
		AuroraPrivacyManageModel.getInstance(this).dealExternalAppAvailable(pkgList);
	}
	
	@Override
	public void updateOfExternalAppUnAvailable(Subject subject,List<String> pkgList) {
		DefSoftModel.getInstance(this).initOrUpdateThread();
		if(CacheSizeModel.getInstance() != null){
			CacheSizeModel.getInstance(this).externalAppUnAvailable(pkgList);
		}						
		AutoStartModel.getInstance(this).externalAppUnAvailable(pkgList);	
		AdScanModel.getInstance(this).scanForExternalAppUnAvailable(pkgList);
		AuroraPrivacyManageModel.getInstance(this).dealExternalAppUnAvailable(pkgList);
	}
	
	@Override
	public void updateOfPrivacyAppInit(PrivacyAppSubject subject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfPrivacyAppAdd(PrivacyAppSubject subject,
			List<PrivacyAppData> PrivacyAppList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfPrivacyAppDelete(PrivacyAppSubject subject,
			List<PrivacyAppData> PrivacyAppList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfPrivacyAccountSwitch(PrivacyAppSubject subject,
			AidlAccountData accountData) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void updateOfDeletePrivacyAccount(PrivacyAppSubject subject,
			AidlAccountData accountData) {
		// TODO Auto-generated method stub	
	}
	
	/**
	 * 监听应用开启
	 */
	private IProcessObserver mProcessObserver = new IProcessObserver.Stub() {
        @Override
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        	/**
        	1.点击酷狗音乐
        	onForegroundActivitiesChanged ，pid:8568, uid:10091, foregroundActivities:true
        	onForegroundActivitiesChanged ，pid:844, uid:1000, foregroundActivities:false

        	2.退出酷狗音乐
        	onForegroundActivitiesChanged ，pid:844, uid:1000, foregroundActivities:true
        	onForegroundActivitiesChanged ，pid:8568, uid:10091, foregroundActivities:false
        	 */
        	 	           
        	if(Process.myPid() == pid && foregroundActivities){ //本应用启动
        		AutoStartModel.getInstance(WatchDogService.this).applicationStart();   		
        	}
        	
        	if(mConfig.isAutoStartControlReceive){
            	String packageName = ApkUtils.getPackageName(WatchDogService.this, uid);
            	dealAutoStart(packageName,foregroundActivities);
        	}        	

            if(uid != 1000 && foregroundActivities){
            	handler1.sendEmptyMessage(uid);
            }           
        }

        /*@Override*/
        public void onImportanceChanged(int pid, int uid, int importance) { }

        @Override
        public void onProcessDied(int pid, int uid) {
            //进程死亡
        	handler2.sendEmptyMessage(uid);
        }

        /*@Override*/
        public void onProcessStateChanged(int pid, int uid, int procState) throws RemoteException {
        }
    };
    
    private Handler handler1 = new Handler() {
	    @Override
	    public void handleMessage(Message msg) { 
	    	String[] packages = ApkUtils.getPackagesForUid(WatchDogService.this, msg.what);
	    	int indexOfCurRunPkg = -1;
	    	int len = packages==null?0:packages.length;
	    	for(int i=0;i<len;i++){
	    		if(isAppStart(packages[i])){
	    			indexOfCurRunPkg = i;
	    			break;
		    	}
	    	}
	    	if(indexOfCurRunPkg < 0){
	    		return ;
	    	}
	    	final String packageName = packages[indexOfCurRunPkg];
        	AppInfo appInfo = ApkUtils.getAppFullInfo(WatchDogService.this, packageName);
        	if(appInfo == null){
        		return ;
        	}
        	
        	int netState = NetworkUtils.getNetState(WatchDogService.this);
        	if(netState == NetworkUtils.NET_TYPE_NONE){
        		return ;
        	}else if(netState == NetworkUtils.NET_TYPE_WIFI){
        		PermissionInfo permissionInfo = appInfo.getWifiPermissionInfo();
            	if(permissionInfo == null || permissionInfo.getIsOpen()){
            		return ;
            	}
        	}else if(netState == NetworkUtils.NET_TYPE_MOBILE){
        		PermissionInfo permissionInfo = appInfo.getNetPermissionInfo();
            	if(permissionInfo == null || permissionInfo.getIsOpen()){
            		return ;
            	}
        	}else{
        		return ;
        	}
        	
        	//判断需不需要弹禁止联网的弹框
        	NetForbidHintData netForbidHintData = NetForbidHintModel.
        			getInstance(WatchDogService.this).getNetForbidHintData(packageName);
        	if(netForbidHintData != null){
        		if(netState == NetworkUtils.NET_TYPE_WIFI && 
        				!netForbidHintData.getNeedHintForWifi()){
        			return ;
        		}else if(netState == NetworkUtils.NET_TYPE_MOBILE && 
        				!netForbidHintData.getNeedHintForSim()){
        			return ;
        		}
        	}
        	
        	if(LBEmodel.getInstance(WatchDogService.this).isBindLBEService()){
	    		startNetRemindDialog(WatchDogService.this,packageName);
			}else{
				LBEmodel.getInstance(WatchDogService.this).bindLBEService(new BindServiceCallback(){
					@Override
					public void callback(boolean result) {
						startNetRemindDialog(WatchDogService.this,packageName);
					}}
				);
			}	
	    }
	};
    
//    private Handler handler1 = new Handler() {
//	    @Override
//	    public void handleMessage(Message msg) { 
//	    	final String packageName = ApkUtils.getPackageName(WatchDogService.this, msg.what); 
//	    	if(!isAppStart(packageName)){
//	    		return ;
//	    	}
//        	AppInfo appInfo = ApkUtils.getAppFullInfo(WatchDogService.this, packageName);
//        	if(appInfo == null){
//        		return ;
//        	}
//        	
//        	int netState = NetworkUtils.getNetState(WatchDogService.this);
//        	if(netState == NetworkUtils.NET_TYPE_NONE){
//        		return ;
//        	}else if(netState == NetworkUtils.NET_TYPE_WIFI){
//        		PermissionInfo permissionInfo = appInfo.getWifiPermissionInfo();
//            	if(permissionInfo == null || permissionInfo.getIsOpen()){
//            		return ;
//            	}
//        	}else if(netState == NetworkUtils.NET_TYPE_MOBILE){
//        		PermissionInfo permissionInfo = appInfo.getNetPermissionInfo();
//            	if(permissionInfo == null || permissionInfo.getIsOpen()){
//            		return ;
//            	}
//        	}else{
//        		return ;
//        	}
//        	
//        	//判断需不需要弹禁止联网的弹框
//        	NetForbidHintData netForbidHintData = NetForbidHintModel.
//        			getInstance(WatchDogService.this).getNetForbidHintData(packageName);
//        	if(netForbidHintData != null){
//        		if(netState == NetworkUtils.NET_TYPE_WIFI && 
//        				!netForbidHintData.getNeedHintForWifi()){
//        			return ;
//        		}else if(netState == NetworkUtils.NET_TYPE_MOBILE && 
//        				!netForbidHintData.getNeedHintForSim()){
//        			return ;
//        		}
//        	}
//        	
//        	if(LBEmodel.getInstance(WatchDogService.this).isBindLBEService()){
//	    		startNetRemindDialog(WatchDogService.this,packageName);
//			}else{
//				LBEmodel.getInstance(WatchDogService.this).bindLBEService(new BindServiceCallback(){
//					@Override
//					public void callback(boolean result) {
//						startNetRemindDialog(WatchDogService.this,packageName);
//					}}
//				);
//			}	
//	    }
//	};
    
    private void startNetRemindDialog(final Context context,final String packageName){	
		 if(context == null || packageName == null){
				return ;
		 }
		 
		 final Handler handler = new Handler() {
			   @Override
			   public void handleMessage(Message msg) {
				    //因为LEB会弹出对话框，所以隐藏掉这个对话框
//					 Intent intent = new Intent();
//				 	 intent.setClass(context, NetForbidHintActivity.class);
//				 	 intent.putExtra("packageName", packageName);
//				 	 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				 	 context.startActivity(intent); 
			   }
			};
			
			new Thread() {
				@Override
				public void run() {				
					try {
					  Thread.sleep(3000);
				    } catch (InterruptedException e) {
					  e.printStackTrace();
				    }
		            handler.sendEmptyMessage(1);   
				}
			}.start();  
	}
    
    private Handler handler2 = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	    	String[] packages = ApkUtils.getPackagesForUid(WatchDogService.this, msg.what);
	    	int len = packages==null?0:packages.length;
	    	for(int i=0;i<len;i++){	    		
	    		if(packages[i] != null && packages[i].equals(NetForbidHintActivity.curPackageName)){
		    		if(NetForbidHintActivity.instance != null){
		    			NetForbidHintActivity.instance.finish();
		    		}
		    		break;
		    	}
	    	}	    	
	    }
    };
    
//    private Handler handler2 = new Handler() {
//	    @Override
//	    public void handleMessage(Message msg) { 
//	    	String packageName = ApkUtils.getPackageName(WatchDogService.this, msg.what); 	
//	    	if(packageName != null && packageName.equals(NetForbidHintActivity.curPackageName)){
//	    		if(NetForbidHintActivity.instance != null){
//	    			NetForbidHintActivity.instance.finish();
//	    		}
//	    	}
//	    }
//    };
    
    /**
     * 获取当前运行应用的包名
     * @return
     */
    private String getCurRunningAppPackage(){
    	if(mActivityManager == null) {
			mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);  
		}
	    List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);  
	    if(rti == null || 
	    		rti.size() == 0 || 
	    		rti.get(0).topActivity == null){
	    	return null;
	    }
	    return rti.get(0).topActivity.getPackageName();
    }
    
    /** 
	 * 判断指定应用当前是不是已经启动了
	 */  
	private boolean isAppStart(String packageName){  
		if(packageName == null){
			return false;
		}
		if(mActivityManager == null) {
			mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);  
		}
	    List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);  
	    if(rti == null || 
	    		rti.size() == 0 || 
	    		rti.get(0).topActivity == null){
	    	return false;
	    }
	    if(packageName.equals(rti.get(0).topActivity.getPackageName())){
	    	return true;
	    }else{
	    	return false;
	    } 
	}  
/*********************处理应用开机自启动begin********************************/    
    
    /**
     * 存放 打开了“不允许开机自启应用广播“的map
     */
    private HashMap<String,AutoStartData> openedAppReceiveMap;
    
    /**
     * 在使用应用的时候，被关掉的广播要重新打开；
     * 在退出应用时，原本禁止自启动的应用，要重新关掉所有广播
     * @param packageName
     * @param foregroundActivities
     */
    private void dealAutoStart(String packageName,boolean foregroundActivities){
    	AutoStartModel instance = AutoStartModel.getInstance();
    	if(StringUtils.isEmpty(packageName) || instance == null){
    		return ;
    	}
    	LogUtils.printWithLogCat(
				WatchDogService.class.getName(), 
				packageName+" state is "+foregroundActivities);
		
		if(foregroundActivities){//启动一个应用
			AutoStartData autoStartData = instance.getAutoStartData(packageName);
			if(autoStartData != null && !autoStartData.getIsOpen()){
				if(openedAppReceiveMap == null){
					openedAppReceiveMap = new HashMap<String,AutoStartData>();
				}
				openedAppReceiveMap.put(packageName, autoStartData);
				ApkUtils.openApkAutoStart(this, autoStartData,packageName);
				LogUtils.printWithLogCat(
						WatchDogService.class.getName(), 
						"OPEN RECEIVE OF "+packageName);
			}			
		}else{//关闭一个应用
//			if(openedAppReceiveMap != null){
//				AutoStartData autoStartData = openedAppReceiveMap.get(packageName);
//				if(autoStartData != null){
//					ApkUtils.closeApkAutoStart(this, autoStartData);
//					openedAppReceiveMap.remove(packageName);
//					LogUtils.printWithLogCat(
//							WatchDogService.class.getName(), 
//							"CLOSE RECEIVE OF "+packageName);
//				}
//			}
		}
    }
}
