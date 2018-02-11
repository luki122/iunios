package com.aurora.puremanager.model;

import java.util.HashMap;
import java.util.List;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.AppsInfo;
import com.aurora.puremanager.data.Constants;
import com.aurora.puremanager.provider.AuroraAppInfosProvider;
//import com.aurora.puremanager.provider.PermissionProvider;	modify by gaoming
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.LogUtils;
import android.content.Context;
import android.content.pm.PackageInfo;

public class AppInfoProvider {
	private final String TAG = AppInfoProvider.class.getName();
	private final Context context;
	private final AppsInfo sysAppsInfo;//系统级应用
	private final Object mLock = new Object();
	
	/**
	 * 所有三方应用（包括隐私应用）
	 */
	private final AppsInfo thirdPartyAppsInfo;
	
	/**
	 * 除去非当前账户下的隐私应用
	 */
	private final AppsInfo thirdPartyAppsInfoOfNoPricacyApp;
	
    private final RecordInfoForThirdApp recordInfoForThirdApp;

	public AppInfoProvider(Context context) {
	    this.context = context;
	    sysAppsInfo = new AppsInfo();	    
	    thirdPartyAppsInfo = new AppsInfo();
	    thirdPartyAppsInfoOfNoPricacyApp = new AppsInfo();
	    recordInfoForThirdApp = new RecordInfoForThirdApp();
	}
	
	/**
	 * 获取总应用数（系统应用+第三方应用(除去非当前账户下的隐私应用)）
	 * @return
	 */
	public int getTotalAppNum(){		
		return sysAppsInfo.size()+ getThirdPartyAppsInfo().size();		
	};
	
	/**
	 * 系统级应用
	 * @return 返回值不会为null
	 */
	public AppsInfo getSysAppsInfo(){
		return this.sysAppsInfo;
	}
	
	/**
	 * 第三方应用
	 * @return
	 */
	public AppsInfo getAllThirdPartyAppsInfo(){
		return this.thirdPartyAppsInfo; 
	}
	
	/**
	 * 第三方应用(除去非当前账户下的隐私应用)
	 * @return 返回值不会为null
	 */
	public AppsInfo getThirdPartyAppsInfo(){
//        long curAccountId = AuroraPrivacyManageModel.getInstance(context).getCurAccountId();
        synchronized (mLock) {
			if(thirdPartyAppsInfoOfNoPricacyApp.size() == 0 ||
				recordInfoForThirdApp.hashCodeOfThirdApp != thirdPartyAppsInfo.hashCode() ||
				recordInfoForThirdApp.modifyOfThirdApp!= thirdPartyAppsInfo.getLastModifyTime()/* ||
				recordInfoForThirdApp.accountId != curAccountId*/ ){
				
			    thirdPartyAppsInfoOfNoPricacyApp.clear();
			    LogUtils.printWithLogCat(TAG,"reset thirdPartyApps begin");
			
				for(int i=0;i<thirdPartyAppsInfo.size();i++){
					AppInfo tmpAppInfo = (AppInfo)thirdPartyAppsInfo.get(i);
					if(tmpAppInfo == null){
						continue;
					}
//					if(!AuroraPrivacyManageModel.getInstance(context).
//							isOtherAccountPrivacyApp(tmpAppInfo.getPackageName())){
						thirdPartyAppsInfoOfNoPricacyApp.add(tmpAppInfo);
//						LogUtils.printWithLogCat(TAG,"apk="+tmpAppInfo.getPackageName());
//					}	
				}	
				LogUtils.printWithLogCat(TAG,"total size="+thirdPartyAppsInfoOfNoPricacyApp.size());
				LogUtils.printWithLogCat(TAG,"reset thirdPartyApps end");
				recordInfoForThirdApp.hashCodeOfThirdApp = thirdPartyAppsInfo.hashCode();
				recordInfoForThirdApp.modifyOfThirdApp = thirdPartyAppsInfo.getLastModifyTime();
//				recordInfoForThirdApp.accountId = curAccountId;
			}
			return thirdPartyAppsInfoOfNoPricacyApp;
		}		
	}
	
	/**
	 * 获取所有应用的信息
	 * @return true：更新了数据库 ； false：没有更新数据库 
	 */
	public boolean initAllAppsInfo(List<PackageInfo> packages){
		boolean isUpdateProvider = false;//是否更新数据库
				
		HashMap<String,AppInfo> providerAppInfoMap = new HashMap<String,AppInfo>();
		
		List<AppInfo> providerAppInfoList = AuroraAppInfosProvider.queryAllAppsInfo(context);	
		if(providerAppInfoList != null){
			for(int i=0;i<providerAppInfoList.size();i++){
				AppInfo tmpAppInfo = providerAppInfoList.get(i);
				providerAppInfoMap.put(tmpAppInfo.getPackageName(), tmpAppInfo);
			}
		}
		
		AppsInfo tmpSysAppsInfo = new AppsInfo();
		AppsInfo tmpThirdPartyAppsInfo = new AppsInfo();
				
		int packagesSize = packages == null?0:packages.size();
		LogUtils.printWithLogCat(TAG,"list num="+packagesSize);
		for(int i=0;i<packagesSize;i++){
		   PackageInfo packageInfo = packages.get(i);  
           AppInfo providerAppInfo = providerAppInfoMap.get(packageInfo.packageName);        
          if(providerAppInfo != null && 
          		 providerAppInfo.getVersionCode() == packageInfo.versionCode){
          	    if(providerAppInfo.getIsUserApp()){
          	    	addForInitAllAppsInfo(tmpThirdPartyAppsInfo,providerAppInfo);
    			}else{
    				providerAppInfo.setIsHome(ApkUtils.isHome(context,packageInfo.packageName));
    				providerAppInfo.setIsHaveLauncher(
    						ApkUtils.isHaveLauncher(context,packageInfo.packageName));
    				addForInitAllAppsInfo(tmpSysAppsInfo,providerAppInfo);
    			}  	 
            }else{
          	   AppInfo appInfo = null;
          	   if(ApkUtils.filterApp(packageInfo.applicationInfo) && (!packageInfo.packageName.equals("com.baidu.map.location"))){
          		  appInfo = ApkUtils.getAppFullInfo(context, packageInfo);
   		    	  if(appInfo == null){
   		    		continue;
   		    	  }
   		    	  appInfo.setIsSysWhiteApp(false);
   		    	  addForInitAllAppsInfo(tmpThirdPartyAppsInfo,appInfo);
               }else{           	 
              	  if(isInSysAppWhiteList(packageInfo.packageName)){
              		 appInfo = ApkUtils.getAppFullInfo(context, packageInfo);
              		 if(appInfo == null){
         		    		continue;
         		    	 }
              		 appInfo.setIsSysWhiteApp(true);
                   }else{
                  	 appInfo = ApkUtils.getAppBasicInfo(context, packageInfo);
                  	 if(appInfo == null){
         		    		continue;
         		    	 }
                  	 appInfo.setIsSysWhiteApp(false);
                   }
              	   addForInitAllAppsInfo(tmpSysAppsInfo,appInfo);
                }
               
          	    AuroraAppInfosProvider.insertOrUpdateDate(context, appInfo);
                isUpdateProvider = true;
            } 
		  }
		  resetDataList(tmpSysAppsInfo,tmpThirdPartyAppsInfo);		   
	      LogUtils.printWithLogCat(TAG,"end num="+(thirdPartyAppsInfo.size()+sysAppsInfo.size()));
	      return isUpdateProvider;
	}
	
	private void resetDataList(AppsInfo tmpSysAppsInfo,AppsInfo tmpThirdPartyAppsInfo){
		sysAppsInfo.clear();
		synchronized (mLock) {
			thirdPartyAppsInfo.clear();
		}		
		
		if(tmpSysAppsInfo != null){
			for(int i=0;i<tmpSysAppsInfo.size();i++){
				sysAppsInfo.add(tmpSysAppsInfo.get(i));
			}
		}
		
		if(tmpThirdPartyAppsInfo != null){
			synchronized (mLock) {
				for(int i=0;i<tmpThirdPartyAppsInfo.size();i++){
					thirdPartyAppsInfo.add(tmpThirdPartyAppsInfo.get(i));
				}
			}		
		}
	}
	
	private void addForInitAllAppsInfo(AppsInfo toAppsInfo,AppInfo needAddAppInfo){
		String needAddPkgName = needAddAppInfo.getPackageName();
		
		AppsInfo oldAppsInfo;
		if(needAddAppInfo.getIsUserApp()){
			oldAppsInfo = thirdPartyAppsInfo;
		}else{
			oldAppsInfo = sysAppsInfo;
		}
		
		if(oldAppsInfo != null){
			for(int i=0;i<oldAppsInfo.size();i++){
				AppInfo oldAppInfo = (AppInfo)oldAppsInfo.get(i);
				if(needAddPkgName.equals(oldAppInfo.getPackageName())){
					oldAppInfo.updateObject(needAddAppInfo);
					needAddAppInfo = oldAppInfo;
					break;
				}
			}
		}		
		toAppsInfo.add(needAddAppInfo);
	}
	
	/**
	 * 在原有应用信息的基础上，增加一些应用
	 * @param pkgList
	 */
	public synchronized void addAppsInfo(List<String> pkgList){
		if(pkgList == null || pkgList.size() == 0){
			return ;
		}
		
		PackageInfo packageInfo;
		AppInfo providerAppInfo;
		for(int i=0;i<pkgList.size();i++){
			packageInfo = ApkUtils.getPackageInfo(context, pkgList.get(i));
			if(packageInfo == null){
				continue ;
			}
			
            providerAppInfo = AuroraAppInfosProvider.getAppsInfo(context, packageInfo.packageName);        
            if(providerAppInfo != null && 
          		providerAppInfo.getVersionCode() == packageInfo.versionCode){
          	    if(providerAppInfo.getIsUserApp()){
          	    	synchronized (mLock) {
          	    		addForAddAppsInfo(thirdPartyAppsInfo,providerAppInfo);
          	    	}          	    	
    			}else{
    				providerAppInfo.setIsHome(ApkUtils.isHome(context,packageInfo.packageName));
    				providerAppInfo.setIsHaveLauncher(
    						ApkUtils.isHaveLauncher(context,packageInfo.packageName));
    				addForAddAppsInfo(sysAppsInfo,providerAppInfo);
    			}  	 
            }else{	
          	    AppInfo appInfo = null;
          	    if(ApkUtils.filterApp(packageInfo.applicationInfo)){
          		   appInfo = ApkUtils.getAppFullInfo(context, packageInfo);
   		    	   if(appInfo == null){
   		    		 continue;
   		    	   }
   		    	   appInfo.setIsSysWhiteApp(false); 
   		    	   synchronized (mLock) {
   		    		  addForAddAppsInfo(thirdPartyAppsInfo,appInfo);
				   } 		    	   
                }else{           	 
              	   if(isInSysAppWhiteList(packageInfo.packageName)){
              		   appInfo = ApkUtils.getAppFullInfo(context, packageInfo);
              		   if(appInfo == null){
         		    		continue;
         		       }
              		   appInfo.setIsSysWhiteApp(true);
                   }else{
                  	  appInfo = ApkUtils.getAppBasicInfo(context, packageInfo);
                  	  if(appInfo == null){
     		    		continue;
     		    	  }
                  	  appInfo.setIsSysWhiteApp(false);
                   }
              	   addForAddAppsInfo(sysAppsInfo,appInfo);
                }
               
          	    AuroraAppInfosProvider.insertOrUpdateDate(context, appInfo);
            } 
		}
	}
	
	private void addForAddAppsInfo(AppsInfo toAppsInfo,AppInfo needAddAppInfo){
		String needAddPkgName = needAddAppInfo.getPackageName();	
		for(int i=0;i<toAppsInfo.size();i++){
			AppInfo oldAppInfo = (AppInfo)toAppsInfo.get(i);
			if(oldAppInfo == null){
				continue ;
			}
			if(needAddPkgName.equals(oldAppInfo.getPackageName())){
				oldAppInfo.updateObject(needAddAppInfo);
				return ;
			}
		}		
		toAppsInfo.add(needAddAppInfo);
	}
	
	/**
	 * 在原有应用信息的基础上，删除一些应用
	 * @param pkgList
	 */
	public void removeAppsInfo(List<String> pkgList){
		if(pkgList == null || pkgList.size() == 0){
			return ;
		}
		
		String pkgName = null;
		for(int i=0;i<pkgList.size();i++){
			pkgName = pkgList.get(i);
			synchronized (mLock) {
				if(!rmoveAppInfo(thirdPartyAppsInfo,pkgName)){
					rmoveAppInfo(sysAppsInfo,pkgName);
				}
			}			
		}
	}
	
	private boolean rmoveAppInfo(AppsInfo fromAppsInfo,String needRemovePkg){
		boolean result = false;
		if(fromAppsInfo == null || needRemovePkg == null){
			return result ;
		}
		
		AppInfo checkAppInfo = null;
		for(int i=0;i<fromAppsInfo.size();i++){
			checkAppInfo = (AppInfo)fromAppsInfo.get(i);
			if(checkAppInfo != null && 
					needRemovePkg.equals(checkAppInfo.getPackageName())){
				fromAppsInfo.remove(i);
				result = true;
				break;
			}
		}
		return result;
	}
		
	/**
	 * 是否在系统应用显示白名单中
	 * @param packageName
	 * @return true:在白名单中 false：没有在白名单中
	 */
	private boolean isInSysAppWhiteList(String packageName){
		for(int i=0;i<Constants.sysAppWhiteList.length;i++){
			if(Constants.sysAppWhiteList[i].equals(packageName)){
				return true;
			}
		}
		return false;		
	}
	
	/**
	 * 为当前可用三方应用记录必要的信息
	 * @author chengrq
	 *
	 */
	final static class RecordInfoForThirdApp{
		/**
		 * 记录获取“当前可用三方应用“时ThirdPartyAppsInfo的hashCode
		 */
		int hashCodeOfThirdApp;
		
		/**
		 * 记录获取“当前可用三方应用“时ThirdPartyAppsInfo的最后修改时间
		 */
		long modifyOfThirdApp;
		
		/**
		 * 记录获取“当前可用三方应用“时的隐私帐号
		 */
		long accountId;		
	}
} 
