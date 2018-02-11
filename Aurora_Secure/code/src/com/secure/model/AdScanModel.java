/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.secure.model;

import com.adblock.data.AdProviderData;
import com.adblock.data.AppAdData;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.MyArrayList;
import com.secure.fragment.AdScanFragment;
import com.secure.interfaces.AdSubject;
import com.secure.provider.AppAdInfoProvider;
import com.secure.provider.UseOperateInfoProvider;
import com.secure.totalCount.TotalCount;
import com.secure.utils.ApkUtils;
import com.secure.utils.MySharedPref;
import com.secure.utils.mConfig;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 广告插件扫描模块
 */
public class AdScanModel extends AdSubject{
    static Object sGlobalLock = new Object();
    static AdScanModel sInstance;

    static final int MSG_INIT= 1;
    static final int MSG_INSTALL_APP = 2;   
    static final int MSG_COVER_APP = 3;
    static final int MSG_UNINSTALL_APP = 4;
    static final int MSG_EXTER_APP_AVAILABLE = 5;
    static final int MSG_EXTER_APP_UNAVAILABLE = 6;
    static final int MSG_ADLIB_UPDATE = 7;//广告库更新
    static final int MSG_MANUAL_UPDATE = 8;//用户手动更新
    static final int MSG_CHANGE_BLOCKED_STATE = 9;//改变广告的拦截状态
    static final int MANUAL_EACH_SCAN_SHORTEST_TIME = 300;//350;//手动扫描，单个应用扫描的最短时间

    Context mApplicationContext;
    private HashMap <String,AppAdData> appsAdMap = new HashMap <String,AppAdData>();
    private HashMap <String,PathClassLoader> pathClassLoaderMap= new HashMap <String,PathClassLoader>();
    
    final Object mLock = new Object();
    
    boolean isInitEnd; 
    boolean isDuringManualScan;
    private AtomicBoolean mNeedStopManualUpdate = new AtomicBoolean();
    private UIHandler mUIhandler;
    final HandlerThread mBackgroundThread;
    final BackgroundHandler mBackgroundHandler;  
    private Handler manualScanCallBackHandler;
       
    static public AdScanModel getInstance(Context context) {
        synchronized (sGlobalLock) {
            if (sInstance == null) {
                sInstance = new AdScanModel(context);
            }
            return sInstance;
        }
    }

    private AdScanModel(Context context) {	
    	mNeedStopManualUpdate.set(false);
        mApplicationContext = context.getApplicationContext();

        isInitEnd = false;
        isDuringManualScan = false;
        mUIhandler = new UIHandler(Looper.getMainLooper());
        mBackgroundThread = new HandlerThread("AdScanModel:Background");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());    
    }
    
    final class UIHandler extends Handler{		
 		public UIHandler(Looper looper){
            super(looper);
        }
 		@Override
 	    public void handleMessage(Message msg) {  
 			switch (msg.what) {
 	            case MSG_INIT:
 	            	synchronized (mLock) {
 	                   if (!isInitEnd) {
 	                	  isInitEnd = true;
 	                   }
 	                } 
 	            	notifyObserversOfInit();
 	            	break;
 	            case MSG_INSTALL_APP:
 	            	notifyObserversOfInStall((String)msg.obj); 
 	            	break;
 	            case MSG_COVER_APP:
 	            	notifyObserversOfCoverInStall((String)msg.obj); 
 	            	break;
 	            case MSG_UNINSTALL_APP:
 	            	notifyObserversOfUnInstall((String)msg.obj); 
 	            	break;       
 	            case MSG_EXTER_APP_AVAILABLE:
 	            	notifyObserversOfExternalAppAvailable((List<String>)msg.obj); 
 	            	break;
 	            case MSG_EXTER_APP_UNAVAILABLE:
 	            	notifyObserversOfExternalAppUnAvailable((List<String>)msg.obj); 
 	            	break;
 	            case MSG_ADLIB_UPDATE:
 	            	notifyObserversOfAdLibUpdate();
 	            	break;
 	           case MSG_MANUAL_UPDATE:
 	        	    notifyObserversOfManualUpdate();	        	   
	            	break;
 	           case MSG_CHANGE_BLOCKED_STATE:
 	        	    notifyObserversOfSwitchChange((String)msg.obj,msg.arg1==1?true:false);
 	        	    break;
 	        }
 	    }
 	}
        
    /**
	 * 初始化
	 */
	public void scanForInit(){
		synchronized (mLock){		
			mBackgroundHandler.sendEmptyMessage(MSG_INIT);
		}		
	}
	
	/**
	 * 安装一个应用
	 * @param packageName
	 */
	public void scanForInStall(String pkgName){
		synchronized (mLock){
			Message msg = mBackgroundHandler.obtainMessage();
			msg.what = MSG_INSTALL_APP;
			msg.obj = pkgName;
			mBackgroundHandler.sendMessage(msg);	
		}
	}
	
	/**
	 * 覆盖安装一个应用
	 * @param appInfo
	 */
	public void scanForCoverInStall(String pkgName){
		synchronized (mLock){
			Message msg = mBackgroundHandler.obtainMessage();
			msg.what = MSG_COVER_APP;
			msg.obj = pkgName;
			mBackgroundHandler.sendMessage(msg);	
		}
	}
	
	/**
	 * 删除一个应用
	 * @param packageName
	 */
	public void scanForUnInstall(String pkgName){
		synchronized (mLock){
			Message msg = mBackgroundHandler.obtainMessage();
			msg.what = MSG_UNINSTALL_APP;
			msg.obj = pkgName;
			mBackgroundHandler.sendMessage(msg);	
		}
	}
	
	/**
	 * 安装在外部sd卡中的应用变得可用
	 * @param pkgList
	 */
	public void scanForExternalAppAvailable(List<String> pkgList){
		synchronized (mLock){
			Message msg = mBackgroundHandler.obtainMessage();
			msg.what = MSG_EXTER_APP_AVAILABLE;
			msg.obj = pkgList;
			mBackgroundHandler.sendMessage(msg);	
		}
	}
		
	/**
	 * 安装在外部sd卡中的应用变得不可用
	 * @param pkgList
	 */
	public void scanForExternalAppUnAvailable(List<String> pkgList){
		synchronized (mLock){
			Message msg = mBackgroundHandler.obtainMessage();
			msg.what = MSG_EXTER_APP_UNAVAILABLE;
			msg.obj = pkgList;
			mBackgroundHandler.sendMessage(msg);	
		}		
	}
	
	/**
	 * 广告库更新
	 */
	public void scanForAdLibUpdate(){
        //do nothing
	}
	
	/**
	 * 用户手动扫描
	 */
	public void scanForManualUpdate(Handler callBackHandler,MyArrayList<AppInfo> needScanAppList){
		if(callBackHandler == null){
			return ;
		}
		synchronized (mLock){
			manualScanCallBackHandler = callBackHandler;
			if(isDuringManualScan){
				return ;
			}		
			isDuringManualScan = true;
			mBackgroundHandler.removeMessages(MSG_INIT);
			mBackgroundHandler.removeMessages(MSG_INSTALL_APP);
			mBackgroundHandler.removeMessages(MSG_COVER_APP);
			mBackgroundHandler.removeMessages(MSG_UNINSTALL_APP);			
			mBackgroundHandler.removeMessages(MSG_EXTER_APP_AVAILABLE);
			mBackgroundHandler.removeMessages(MSG_EXTER_APP_UNAVAILABLE);
			mBackgroundHandler.removeMessages(MSG_ADLIB_UPDATE);	
			mBackgroundHandler.removeMessages(MSG_MANUAL_UPDATE);	
			
			Message msg = mBackgroundHandler.obtainMessage();
			msg.what = MSG_MANUAL_UPDATE;
			msg.obj = needScanAppList;
			mBackgroundHandler.sendMessage(msg);	
		}
	}
	
    /**
     * 停止手动更新
     */
    public void stopManualUpdate() {
    	synchronized (mLock){	
			mBackgroundHandler.removeMessages(MSG_MANUAL_UPDATE);	
		}
    	mNeedStopManualUpdate.set(true);
    }

	/**
	 * 应用广告拦截的开关状态改变
	 * @param appInfo
	 * @param switchState true:表示拦截，false：表示不拦截
	 */
	public void setSwitch(AppInfo appInfo,boolean switchState){
		if(appInfo == null || mApplicationContext == null){
			return ;
		}
		appInfo.setIsBlockAd(switchState);

		UseOperateInfoProvider.UpdateAdBlockState(mApplicationContext, appInfo);
		
		Message mUIhandlerMsg = mUIhandler.obtainMessage();
        mUIhandlerMsg.what = MSG_CHANGE_BLOCKED_STATE;
        mUIhandlerMsg.obj = appInfo.getPackageName();
        mUIhandlerMsg.arg1 = switchState?1:0;
        mUIhandler.sendMessage(mUIhandlerMsg);		
	}
	
    final class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
        	boolean needSendUpdateHandler = false;
            switch (msg.what) {
	            case MSG_INIT:
	            	needSendUpdateHandler = updateForInit(mApplicationContext);
	            	break;
	            case MSG_INSTALL_APP:
	            	needSendUpdateHandler=updateForInStall((String)msg.obj);
	            	break;
	            case MSG_COVER_APP:
	            	needSendUpdateHandler=updateForCoverInStall((String)msg.obj);
	            	break;
	            case MSG_UNINSTALL_APP:
	            	needSendUpdateHandler=updateForUnInstall((String)msg.obj);
	            	break;       
	            case MSG_EXTER_APP_AVAILABLE:
	            	needSendUpdateHandler=updateForExternalAppAvailable((List<String>)msg.obj);
	            	break;
	            case MSG_EXTER_APP_UNAVAILABLE:
	            	needSendUpdateHandler=updateForExternalAppUnAvailable((List<String>)msg.obj);
	            	break;
	            case MSG_ADLIB_UPDATE:
	            case MSG_MANUAL_UPDATE:
	            	mNeedStopManualUpdate.set(false);
	            	ArrayList <AppAdData> tmpAppAdDataList = getAppAdDataList();
	            	updateForManualUpdate((MyArrayList<AppInfo>)msg.obj);
	            	MySharedPref.saveAlreadyScanAd(mApplicationContext, true);
	            	synchronized (mLock) {
	            		isDuringManualScan = false;
	 	            }
	            	boolean isCompleteScanAdApp = MySharedPref.getIsCompleteScanAdApp(mApplicationContext);
	            	if(mNeedStopManualUpdate.get() && isCompleteScanAdApp){//最终结果用上次扫描的结果
	            		synchronized (appsAdMap){
            	   		   appsAdMap.clear();
            	   	    }	            		 
	            		for(int i=0;i<tmpAppAdDataList.size();i++){
	            			AppAdData appAdData = tmpAppAdDataList.get(i);
	            			synchronized (appsAdMap){
 	             	   		   appsAdMap.put(appAdData.getPkgName(), appAdData);
 	             	   	    }            			
	            		}
	            	}else{//最终结果用本次扫描的结果
	            		needSendUpdateHandler=true;
	            		AppAdInfoProvider.clearData(mApplicationContext);
	            		ArrayList <AppAdData> appAdDataList = getAppAdDataList();
	            		for(int i=0;i<appAdDataList.size();i++){
	            			AppAdInfoProvider.insertOrUpdateDate(mApplicationContext,appAdDataList.get(i));        			
	            		}            		
	            		if(!isCompleteScanAdApp && !mNeedStopManualUpdate.get()){
	            			MySharedPref.saveIsCompleteScanAdApp(mApplicationContext, true);
	            		}
	            	}	  
	            	tmpAppAdDataList.clear();		            	
	            	if(manualScanCallBackHandler != null){
						manualScanCallBackHandler.sendEmptyMessage(
								AdScanFragment.UIHandler_MSG_TYPE_OF_COMPLETE);
					}
	            	break;
	        }
            
            if(needSendUpdateHandler){
                Message mUIhandlerMsg = mUIhandler.obtainMessage();
                mUIhandlerMsg.what = msg.what;
                mUIhandlerMsg.obj = msg.obj;
                mUIhandler.sendMessage(mUIhandlerMsg);
            }
	    };
    }
    
    /**
     * 从数据库中取数据
     * @param context
     * @return
     */
	private boolean updateForInit(Context context){
		ConfigModel configModel = ConfigModel.getInstance();
		if(configModel == null || context == null){
			return false;
		}
		AppsInfo userAppsInfo = configModel.getAppInfoModel().getThirdPartyAppsInfo();
		if(userAppsInfo != null){
			for(int i=0;i<userAppsInfo.size();i++){
				AppInfo appInfo = (AppInfo)userAppsInfo.get(i);
				if(appInfo == null){
					continue ;
				}
				AppAdData appAdData = AppAdInfoProvider.getAppAdData(
						context, appInfo.getPackageName());
				if(appAdData != null){
					synchronized (appsAdMap){				
						appsAdMap.put(appInfo.getPackageName(), appAdData);
					}
				}					
			}
		}
		return true;
	}
	
	private boolean updateForInStall(String pkgName){
		ConfigModel configModel = ConfigModel.getInstance();
		if(configModel == null){
			return false;
		}
		AppAdData appAdData = scanAppFunc(mApplicationContext,
				configModel.getAppInfoModel().findAppInfo(pkgName, true));
		if(appAdData != null){
			AppAdInfoProvider.insertOrUpdateDate(mApplicationContext,appAdData);
			return true;
		}else{
			return false;
		}
	}
	
	private boolean updateForCoverInStall(String pkgName){
		return updateForInStall(pkgName);
	}

	private boolean updateForUnInstall(String pkgName){	
	    boolean isDeleteSucess;
		synchronized (appsAdMap){
			if(appsAdMap.remove(pkgName) != null){
				isDeleteSucess = true;
			}else{
				isDeleteSucess = false;
			}    		 
    	}
		if(isDeleteSucess){
			AppAdInfoProvider.deleteDate(mApplicationContext, pkgName);
			return true;
		}else{
			return false;
		}		
	}
	
	private boolean updateForExternalAppAvailable(List<String> pkgList){
		boolean isHaveAd = false;
		ConfigModel configModel = ConfigModel.getInstance();
		if(configModel == null){
			return isHaveAd;
		}
		
		int size = pkgList==null?0:pkgList.size();
		for(int i=0;i<size;i++){
			AppAdData appAdData = scanAppFunc(mApplicationContext,
					configModel.getAppInfoModel().findAppInfo(pkgList.get(i), true));		
			if(appAdData != null){
				AppAdInfoProvider.insertOrUpdateDate(mApplicationContext,appAdData);
				isHaveAd = true;
			}
		}
		return isHaveAd;
	}
		
	private boolean updateForExternalAppUnAvailable(List<String> pkgList){
		boolean isNeedUpdate = false;
		int size = pkgList==null?0:pkgList.size();	
		boolean deleteSuccess;
		for(int i=0;i<size;i++){
			synchronized (appsAdMap){
				 if(appsAdMap.remove(pkgList.get(i)) != null){
					 deleteSuccess = true;
				 }else{
					 deleteSuccess = false;
				 }
		   	}
			if(deleteSuccess){
				AppAdInfoProvider.deleteDate(mApplicationContext, pkgList.get(i));
				isNeedUpdate = true;
			}			
		}  
		return isNeedUpdate;
	}
	
	/**
	 * 用户手动更新，重新扫描
	 */
	private void updateForManualUpdate(MyArrayList<AppInfo> needScanAppList){		
		synchronized (appsAdMap){
   		   appsAdMap.clear();
   	    }
		
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		int size = needScanAppList==null?0:needScanAppList.size();
		if(size == 0){
			if(manualScanCallBackHandler != null){
				manualScanCallBackHandler.sendEmptyMessage(AdScanFragment.UIHandler_MSG_TYPE_OF_COMPLETE);
			}
			return ;
		}
		for(int i=0;i<size;i++){
			if(mNeedStopManualUpdate.get()){
				if(manualScanCallBackHandler != null){
					manualScanCallBackHandler.sendEmptyMessage(AdScanFragment.UIHandler_MSG_TYPE_OF_STOP);
				}
				break;
			}
			AppInfo appInfo = (AppInfo)needScanAppList.get(i);
			if(appInfo == null){
				continue ;
			}
			if(manualScanCallBackHandler != null){
				Message msg = manualScanCallBackHandler.obtainMessage();
				msg.obj = appInfo;
				msg.what = AdScanFragment.UIHandler_MSG_TYPE_OF_UPDATE;
				manualScanCallBackHandler.sendMessage(msg);
			}
			long beginTime = System.currentTimeMillis();
			scanAppFunc(mApplicationContext,appInfo);
			long duringTime = System.currentTimeMillis() - beginTime;
			if(duringTime < MANUAL_EACH_SCAN_SHORTEST_TIME && (i==0 || i<size-1)){
				try {
					Thread.sleep(MANUAL_EACH_SCAN_SHORTEST_TIME - duringTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}			
		}
	}
		
	/**
	 * 扫描指定的app中是否有广告插件
	 * @param context
	 * @param appInfo
	 * @return 返回带有广告插件的应用信息
	 */
	private AppAdData scanAppFunc(Context context,AppInfo appInfo){			
		if(context == null ||
				appInfo == null || 
				!appInfo.getIsUserApp()){
			return null;
		}	  		 
 
		PackageInfo packageInfo = ApkUtils.getPackageInfo(context,appInfo.getPackageName());
		if(packageInfo == null){
			return null;
		}
		DexFile df = null;
		try {  
			df = new DexFile(packageInfo.applicationInfo.sourceDir);
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }
		if(df == null){
			return null;
		}
		Enumeration<String> classNameEnumeration = df.entries(); 
		if(classNameEnumeration == null){
			return null;
		}
		MyArrayList<AdProviderData> adProviderList = AdLibModel.getInstance(context).getAdProviderList();
		String NO_DEAL1="android.support";
		String NO_DEAL2="$";
		while (classNameEnumeration.hasMoreElements()) {//遍历出所有类  
            String className = null; 
            try{
            	className = (String) classNameEnumeration.nextElement(); 
            }catch(NoSuchElementException e){
            	e.printStackTrace();
            	className = null; 
            }
            if(className == null || 
            		className.startsWith(NO_DEAL1) || 
            		className.contains(NO_DEAL2)){
            	continue;
            }
            checkClassIsAd(appInfo,className,adProviderList); 
        }
	
		synchronized (appsAdMap){
			return appsAdMap.get(appInfo.getPackageName());
		}	
    }
	
	/**
	 * 扫描app中是否有指定的广告插件
	 * @param appInfo
	 * @param className
	 * @param adProviderList
	 */
	private void checkClassIsAd(AppInfo appInfo,String className,MyArrayList<AdProviderData> adProviderList){
		for(int i=0;i<adProviderList.size();i++){
			AdProviderData adProviderData = adProviderList.get(i);		
			if(adProviderData.isAdClass(className)){
            	synchronized (appsAdMap){
            		AppAdData appAdData = appsAdMap.get(appInfo.getPackageName());
					if(appAdData == null){
						appAdData = new AppAdData();
						appAdData.setPkgName(appInfo.getPackageName());
						appAdData.setVersionCode(appInfo.getVersionCode());
						appAdData.addAdProviderData(adProviderData);					
						appsAdMap.put(appInfo.getPackageName(), appAdData);
					}else{
						appAdData.addAdProviderData(adProviderData);
					}
				}
            	/**
            	 * 一个应用已经包含某个“广告提供商“后，
            	 * 该应用后面的类就没有必要继续扫描该“广告提供商“了
            	 */
            	adProviderList.remove(i);
				break;
            }
		}
	}

	/**
	 * 初始化是否结束
	 * @return
	 */
    public boolean isInitEnd() {
    	synchronized (mLock) {
            return isInitEnd;
        }
    }
    
    public AppAdData getAppAdData(String pkgName){
    	synchronized (appsAdMap){
    		return appsAdMap.get(pkgName);
    	}
    }
    
    /**
     * 获取appsAdMap的key列表
     * @return 返回不可能为null
     */
    public MyArrayList<String> getKeyList(){
    	synchronized(appsAdMap){
    		MyArrayList<String> keyList = new MyArrayList<String>();
			Set<String> pkgNames = appsAdMap.keySet();
 		    for (String pkg: pkgNames){
 		    	keyList.add(pkg);
 		    } 
 		    return keyList;
		}   	
    } 
    
    /**
     * 将AppsAdMap中的内容转化成ArrayList输入
     * @return 返回值不可能为null
     */
    private ArrayList <AppAdData> getAppAdDataList(){
    	ArrayList <AppAdData> appAdDataList = new ArrayList <AppAdData>();
    	MyArrayList<String> keyList = getKeyList();
    	AppAdData appAdData = null;
    	for(int i=0;i<keyList.size();i++){
    		synchronized (appsAdMap){
			   appAdData =  appsAdMap.get(keyList.get(i));
	   	    }
    		if(appAdData != null){
    			appAdDataList.add(appAdData);
    		}            		
    	}
    	return appAdDataList;
    }
    
    /**
     * 获取当前含有广告插件的应用个数
     * @return
     */
    public int getAdApkNum(){
    	synchronized(appsAdMap){		
    		return appsAdMap.size();
		}
    }
    
    /**
     * 获取当前含有广告插件的应用个数
     * @return
     */
    public synchronized int getAdApkNumForMainActivity(){
    	int num = 0;
    	AppInfoModel appInfoModel = ConfigModel.getInstance(mApplicationContext).getAppInfoModel();
    	MyArrayList<String> keyList = getKeyList();
    	for(int i=0;i<keyList.size();i++){
    		if(null != appInfoModel.findAppInfo(keyList.get(i), true)){
    			num ++;
    		}
    	}
    	return num;
    }
    
    /**
     * 判断指定应用是否含有广告插件 
     * @param pkgName
     * @return
     */
    public boolean isAdApp(String pkgName){
    	if(pkgName == null){
    		return false;
    	}
    	synchronized(appsAdMap){		
    		return appsAdMap.containsKey(pkgName);
		}
    }
    
	public static void releaseObject(){
		if(sInstance != null){			
			if(mConfig.SET_NULL_OF_CONTEXT){
				sInstance.mApplicationContext = null;
			}
			synchronized (sInstance.appsAdMap){
				sInstance.appsAdMap.clear();
			}
			
			synchronized (sInstance.pathClassLoaderMap) {
				sInstance.pathClassLoaderMap.clear();
			}
			sInstance = null;
		}	
	}
}
