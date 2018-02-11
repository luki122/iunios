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

import com.secure.data.AppInfo;
import com.secure.data.Constants;
import com.secure.utils.ApkUtils;
import com.secure.utils.Utils;
import com.secure.utils.mConfig;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Debug.MemoryInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//import android.app.ActivityManagerNative;

/**
 * Singleton for retrieving and monitoring the state about all running
 * applications/processes/services.
 */
public class RunningState {
    static Object sGlobalLock = new Object();
    static RunningState sInstance;

    static final int MSG_DELETE_CONTENTS = 1;
    static final int MSG_UPDATE_CONTENTS = 2;
    static final long CONTENTS_UPDATE_DELAY = 3800;

    Context mApplicationContext;
    final ActivityManager mAm;
    final PackageManager mPm;

    OnRefreshUiListener mRefreshUiListener;
    public final ArrayList<AppInfo> mRunningProcesses = new ArrayList<AppInfo>();
    private HashMap <String,ProcessInfoData> processInfoMap = new HashMap <String,ProcessInfoData>();
    private final ArrayList<String> recordRemovePkgList = new ArrayList<String>();
    final Object mLock = new Object();
    
    boolean mResumed;
    boolean mHaveData;  
    private UIHandler mUIhandler;
    final HandlerThread mBackgroundThread;
    final BackgroundHandler mBackgroundHandler;
    
    /**
	 * 当前是不是奇数次更新
	 */
	boolean updateTimeOfOdd;
    
    static public RunningState getInstance(Context context) {
        synchronized (sGlobalLock) {
            if (sInstance == null) {
                sInstance = new RunningState(context);
            }
            return sInstance;
        }
    }

    private RunningState(Context context) {
        mApplicationContext = context.getApplicationContext();
        mAm = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        mPm = context.getPackageManager();
        mResumed = false;
        mHaveData = false;
        updateTimeOfOdd = true;
        mUIhandler = new UIHandler(Looper.getMainLooper());
        mBackgroundThread = new HandlerThread("RunningState:Background");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());       
    }
    
    private class UIHandler extends Handler{		
		public UIHandler(Looper looper){
           super(looper);
        }

		@Override
	    public void handleMessage(Message msg) {  
			 if (mRefreshUiListener != null) {
                 mRefreshUiListener.onRefreshUi(msg.what==1?true:false);
             }
	    }
	}
    
    final class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_DELETE_CONTENTS:
            	if(deleteFunc((String)msg.obj)){
            		mUIhandler.sendEmptyMessage(1);
            	}
            	break;
            case MSG_UPDATE_CONTENTS:
                synchronized (mLock) {
                    if (!mResumed) {
                        return;
                    }
                }
                if(update(mApplicationContext, mAm)){
                	mUIhandler.sendEmptyMessage(1);
                }                                  
                break;
            }
            
            removeMessages(MSG_UPDATE_CONTENTS);
            msg = obtainMessage(MSG_UPDATE_CONTENTS);
            sendMessageDelayed(msg, CONTENTS_UPDATE_DELAY);  
        }
    };

    static public interface OnRefreshUiListener {
        public void onRefreshUi(boolean change);
    }

    public void resume(OnRefreshUiListener listener) {
        synchronized (mLock) {
            mResumed = true;
            mRefreshUiListener = listener;
            
            mBackgroundHandler.removeMessages(MSG_DELETE_CONTENTS);
            mBackgroundHandler.removeMessages(MSG_UPDATE_CONTENTS);
            mBackgroundHandler.sendEmptyMessage(MSG_UPDATE_CONTENTS);
        }
    }
    
    /**
     * 删除一个应用的信息，使用场景：强行停止一个应用，卸载一个应用
     * @param pkg
     */
    public void deleteProcess(String pkg){
    	if(pkg != null){
    		synchronized (mLock) {
                mBackgroundHandler.removeMessages(MSG_DELETE_CONTENTS);
                mBackgroundHandler.removeMessages(MSG_UPDATE_CONTENTS);
                
                Message msg = mBackgroundHandler.obtainMessage();
                msg.what = MSG_DELETE_CONTENTS;
                msg.obj = pkg;
                mBackgroundHandler.sendMessage(msg);
            }
    	}
    }

    public boolean hasData() {
        synchronized (mLock) {
            return mHaveData;
        }
    }

    void waitForData() {
        synchronized (mLock) {
            while (!mHaveData) {
                try {
                    mLock.wait(0);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public void pause() {
        synchronized (mLock) {
            mResumed = false;
            mRefreshUiListener = null;
        }
    }
    
    private boolean update(Context context, ActivityManager am) {
        boolean changed = false;        
        if(context == null || am == null || ConfigModel.getInstance() == null){
        	return changed;
        }
        updateTimeOfOdd = updateTimeOfOdd?false:true;
                     
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();       
        final int NP = processes != null ? processes.size() : 0;
        for(int i=0; i<NP; i++){
        	ActivityManager.RunningAppProcessInfo appProcess = processes.get(i);    	
       	    ApplicationInfo ai = ensureLabel(mPm,appProcess);
//        	if(ai != null && !Constants.isPackageNameInList(
//        			Constants.NOT_SHOW_RUNNINGA_APP, ai.packageName)){     		
//        		MemoryInfo[] memoryinfos = am.getProcessMemoryInfo(new int[]{appProcess.pid});
//        		long memorysize = memoryinfos[0].getTotalPss()*1024;
//        		ProcessInfoData processInfoData = processInfoMap.get(ai.packageName);
//        		if(processInfoData == null){
//        			processInfoData = new ProcessInfoData(ai.packageName,
//        					ApkUtils.getApkName(context,ai),
//        					memorysize,
//        					ApkUtils.isUserApp(ai));
//        			processInfoMap.put(ai.packageName, processInfoData);
//        		}else if(processInfoData.updateTimeOfOdd != updateTimeOfOdd){
//        			processInfoData.size = memorysize;
//        		}else{
//        			processInfoData.size = processInfoData.size+memorysize;
//        		}
//        		processInfoData.updateTimeOfOdd = updateTimeOfOdd;
//        	} 
       	    
	       	 if(isNeedShowInRunningAppList(ai)){     		
	     		MemoryInfo[] memoryinfos = am.getProcessMemoryInfo(new int[]{appProcess.pid});
	     		long memorysize = memoryinfos[0].getTotalPss()*1024;
	     		ProcessInfoData processInfoData = processInfoMap.get(ai.packageName);
	     		if(processInfoData == null){
	     			processInfoData = new ProcessInfoData(ai.packageName,
	     					ApkUtils.getApkName(context,ai),
	     					memorysize,
	     					ApkUtils.isUserApp(ai));
	     			processInfoMap.put(ai.packageName, processInfoData);
	     		}else if(processInfoData.updateTimeOfOdd != updateTimeOfOdd){
	     			processInfoData.size = memorysize;
	     		}else{
	     			processInfoData.size = processInfoData.size+memorysize;
	     		}
	     		processInfoData.updateTimeOfOdd = updateTimeOfOdd;
	     	}
        }

        AppInfoModel appInfoModel = ConfigModel.getInstance().getAppInfoModel();   
        int oldNum = mRunningProcesses.size();
        mRunningProcesses.clear(); 
        recordRemovePkgList.clear();
	    for (String pkg : processInfoMap.keySet()){ 
	    	ProcessInfoData processInfoData = processInfoMap.get(pkg);
	    	if(processInfoData.updateTimeOfOdd == updateTimeOfOdd){
	    		AppInfo appInfo = appInfoModel.findAppInfo(pkg, processInfoMap.get(pkg).isUserApp);
	    		if(appInfo != null && 
	    				(appInfo.getMemorySize()==0 ||
	    				!appInfo.getPackageName().equals(Utils.getOwnPackageName(context)))){
	    			String oldMemorySizeStr = Utils.dealMemorySize(context, 
	    					appInfo.getMemorySize(),"%.1f"); 
		    		String newMemorySizeStr = Utils.dealMemorySize(context, 
		    				processInfoMap.get(pkg).size,"%.1f"); 
			    	if(oldMemorySizeStr == null || 
			    			!oldMemorySizeStr.equals(newMemorySizeStr)){
			    		appInfo.setMemorySize(processInfoMap.get(pkg).size);
			    		changed = true;
			    	}
	    		}	    			    	
		    	mRunningProcesses.add(appInfo);
	    	}else{
	    		recordRemovePkgList.add(pkg);
	    		changed = true;
	    	}	    	
	    }
	    
	    for(int i=0;i<recordRemovePkgList.size();i++){
	    	processInfoMap.remove(recordRemovePkgList.get(i));
	    }
   
        if(mRunningProcesses.size() != oldNum){
        	changed = true;
        }
        synchronized (mLock) {
            if (!mHaveData) {
                mHaveData = true;
                mLock.notifyAll();
            }
        }       
        return changed;
    }
    
    /**
     * 判断某个app是否显示在当前正在运行的应用列表中
     * 判断标准：显示的应用改为“个人应用+系统应用+系统组件白名单”
     * @param ai
     * @return
     */
    private boolean isNeedShowInRunningAppList(ApplicationInfo ai){
    	if(ai == null){
    		return false;
    	}
    	if(ApkUtils.isUserApp(ai)){
    		return true;
    	}else{
    		AppInfoModel appInfoModel = ConfigModel.getInstance().getAppInfoModel();   
    		AppInfo appInfo = appInfoModel.findAppInfo(ai.packageName, false);
    		if(appInfo == null ||
    				!appInfo.getIsInstalled()){
    			return false;
    		}
    		if(appInfo.isHaveLauncher() || 
    			appInfo.isHome() ||
    			Constants.isPackageNameInList(Constants.showInSysAppList,appInfo.getPackageName()) ||
    			Constants.isPackageNameInList(Constants.SHOW_RUNNINGA_APP_FOR_SYS_APP_SUB,
    					appInfo.getPackageName())){
    			return true;
    		}else{
    			return false;
    		} 
    	}
    }
    
    private boolean deleteFunc(String pkg) {
    	boolean change = false;
    	if(pkg == null){
    		return change;
    	}
    	for(int i =0;i< mRunningProcesses.size() ; i++){
    		AppInfo appInfo = mRunningProcesses.get(i);
    		if(appInfo == null){
    			continue ;
    		}
    		if(pkg.equals(appInfo.getPackageName())){
    			change = true;
    			mRunningProcesses.remove(i);
    		}
    	}   
        return change;
    }
    
    private class ProcessInfoData{
    	public ProcessInfoData(String pkg,String appName,long size,boolean isUserApp ){
    		this.pkg = pkg;
    		this.appName = appName;
    		this.size = size;
    		this.isUserApp = isUserApp;
    	}
    	String pkg = null;
    	String appName = null;
    	long size = 0;
    	boolean isUserApp;
    	/**
    	 * 当前是不是次数此更新
    	 */
    	boolean updateTimeOfOdd;
    }
       
    ApplicationInfo ensureLabel(PackageManager pm,ActivityManager.RunningAppProcessInfo appProcess) {       
        try {
            ApplicationInfo ai = pm.getApplicationInfo(appProcess.processName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            if (ai.uid == appProcess.uid) {
                return ai;
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        
        // If we couldn't get information about the overall
        // process, try to find something about the uid.
        String[] pkgs = pm.getPackagesForUid(appProcess.uid);
        
        // If there is one package with this uid, that is what we want.
        if (pkgs.length == 1) {
            try {
                ApplicationInfo ai = pm.getApplicationInfo(pkgs[0],
                        PackageManager.GET_UNINSTALLED_PACKAGES);
                return ai;
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        
        // If there are multiple, see if one gives us the official name
        // for this uid.
        for (String name : pkgs) {
            try {
                PackageInfo pi = pm.getPackageInfo(name, 0);
                if (pi.sharedUserLabel != 0) {
                    CharSequence nm = pm.getText(name,
                            pi.sharedUserLabel, pi.applicationInfo);
                    if (nm != null) {                  
                        return pi.applicationInfo;
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        
        // Finally... whatever, just pick the first package's name.
        try {
            ApplicationInfo ai = pm.getApplicationInfo(pkgs[0],
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return ai;
        } catch (PackageManager.NameNotFoundException e) {
        }
        
        return null;
    }
    
	public static void releaseObject(){
		if(sInstance != null){
			sInstance.mRunningProcesses.clear();
			sInstance.processInfoMap.clear();
			
			if(mConfig.SET_NULL_OF_CONTEXT){
				sInstance.mApplicationContext = null;
			}
			
			sInstance = null;
		}	
	}
}
