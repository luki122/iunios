package com.aurora.puremanager.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.aurora.utils.Utils2Icon;
//import com.lbe.security.service.sdkhelper.SDKConstants; modify by gaoming
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.AppsInfo;
import com.aurora.puremanager.data.AutoStartData;
import com.aurora.puremanager.data.SameCategoryAppInfo;
//import com.aurora.puremanager.data.AutoStartData;	modify by gaoming
import com.aurora.puremanager.data.BaseData;
import com.aurora.puremanager.data.Constants;
import com.aurora.puremanager.data.MyArrayList;
//import com.aurora.puremanager.data.MyPkgPermission;	modify by gaoming
//import com.aurora.puremanager.data.PermissionInfo;
//import com.aurora.puremanager.data.SameCategoryAppInfo;	modify by gaoming
//import com.aurora.puremanager.interfaces.PermissionSubject;
import com.aurora.puremanager.model.AppInfoModel;
import com.aurora.puremanager.model.AutoStartModel;
//import com.aurora.puremanager.model.AutoStartModel;	modify by gaoming
import com.aurora.puremanager.model.ConfigModel;
import com.aurora.puremanager.provider.open.AutoStartAppProvider;
//import com.aurora.puremanager.model.GetRecomPermsModel;	modify by gaoming
//import com.aurora.puremanager.model.LBEmodel;	modify by gaoming
//import com.aurora.puremanager.provider.open.AutoStartAppProvider; modify by gaoming

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ApkUtils {
	
	/**
     * 通过包名获取UID
     * @param packageName
     * @return
     */
    public static int getUidByPackageName(Context context,String packageName){
        int uid = -1;
        try {
	        PackageManager pm = context.getPackageManager();
	        ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
	        if(ai != null){
	        	 uid = ai.uid;
	        }   
	    } catch (NameNotFoundException e) {
	        e.printStackTrace();
	    }
        return uid;
    }
	
    /**
     * 获取应用申请的权限
     * @param context
     * @param packageName
     * @return 注意返回值不可能为 null
     */
//    public static synchronized ArrayList<PermissionInfo> getPermission(
//    		Context context , 
//    		String packageName){
//				return null; 
//    	return LBEmodel.getInstance(context).getPermList(packageName);modify by gaoming
//    }
    
    /**
     * 获取应用的icon
     * @param context
     * @param packageName
     * @return
     */
    public static synchronized Drawable getApkIcon(Context context ,String packageName){
    	Drawable icon = Utils2Icon.getInstance(context).getIconDrawable(
				packageName,
				Utils2Icon.INTER_SHADOW);
    	if(icon != null){
    		return icon;
    	}
    	
    	ApplicationInfo applicationInfo = getApplicationInfo(context,packageName);
    	if(applicationInfo == null){
			return null;
		}  		
        PackageManager pm = context.getPackageManager();   
        //下面这行代码，在E6机器上如果快速多次调用，会出现程序异常卡死退出（即使放在子线程也同样如此），让人蛋疼
        return pm.getApplicationIcon(applicationInfo);
    }
    
    /**
     * 获取应用的icon
     * @param context
     * @param packageName
     * @return
     */
    public static synchronized Drawable getApkIcon(Context context ,ResolveInfo resolveInfo){
    	if(context == null || resolveInfo == null){
			return null;
		}
    		
        PackageManager pm = context.getPackageManager();          
        Drawable appIcon = Utils2Icon.getInstance(context).getIconDrawable(
        		resolveInfo,
        		Utils2Icon.INTER_SHADOW);
        if(appIcon == null){
       	   appIcon = resolveInfo.loadIcon(pm);      	 
        }       
        return appIcon;
    }
    
//    public static int getPermissionState(Context context,String packageName,PermissionInfo permInfo){
//		return 0;
//    	return LBEmodel.getInstance(context).getPermissionState(packageName, permInfo);
//    }
    
    /**
     * 获取应用对应的名字
     * @param context
     * @param applicationInfo
     * @return
     */
    public static String getApkName(Context context , ApplicationInfo applicationInfo){
    	 if(context == null || applicationInfo == null){
    		return null;
    	 }
    	 PackageManager pm = ConfigModel.getInstance(context).getPackageManager();
    	//下面这行代码，在E6机器上如果快速多次调用，会出现程序异常卡死退出（即使放在子线程也同样如此），让人蛋疼
    	 CharSequence label = applicationInfo.loadLabel(pm);
    	 String apkName = label != null ? label.toString() : applicationInfo.packageName;
         return apkName;
    }
    
    /**
     * 获取应用第一安装的时间
     * @param context
     * @param packageName
     * @return
     */
    public static synchronized long getApkFirstInstallTime(Context context , String packageName){   	
    	if(context == null || StringUtils.isEmpty(packageName)){
    		return 0;
    	}
    	PackageManager pm = context.getPackageManager();
        try {
        	PackageInfo packageInfo = pm.getPackageInfo(packageName,0);     
        	return packageInfo.firstInstallTime;   	
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }      
    }
    
    /**
     * 获取应用版本
     * @param context
     * @param packageName
     * @return
     */
    public static synchronized String getApkVersion(Context context , String packageName){   	
    	if(context == null || StringUtils.isEmpty(packageName)){
    		return "";
    	}
    	PackageManager pm = context.getPackageManager();
        try {
        	PackageInfo packageInfo = pm.getPackageInfo(packageName,0);     	
        	return packageInfo.versionName;   	
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }      
    }
    
    /**
     * 获取应用版本号
     * @param context
     * @param packageName
     * @return
     */
    public static synchronized int getApkVersionCode(Context context , String packageName){   	
    	if(context == null || StringUtils.isEmpty(packageName)){
    		return -1;
    	}
    	PackageManager pm = context.getPackageManager();
        try {
        	PackageInfo packageInfo = pm.getPackageInfo(packageName,0);     	
        	return packageInfo.versionCode;   	
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }      
    }
    
    /**
     * 获取应用的service信息
     * @param context
     * @param packageName
     * @return
     */
    public static synchronized ServiceInfo[] getApkServiceInfos(Context context , String packageName){
    	if(context == null || StringUtils.isEmpty(packageName)){
    		return null;
    	}
    	PackageManager pm = context.getPackageManager();
        try {
        	PackageInfo packageInfo = pm.getPackageInfo(packageName,PackageManager.GET_SERVICES);     	
        	return packageInfo.services;   	
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return null;
        } 
    }
    
    /**
     * 获取应用的Receive信息
     * @param context
     * @param packageName
     * @return
     */
    public static synchronized ActivityInfo[] getApkReceiveInfos(Context context , String packageName){
    	if(context == null || StringUtils.isEmpty(packageName)){
    		return null;
    	}
    	PackageManager pm = context.getPackageManager();
        try {
        	PackageInfo packageInfo = pm.getPackageInfo(packageName,PackageManager.GET_RECEIVERS);     	
        	return packageInfo.receivers;   	
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return null;
        } 
    }
    
    /**
     * 判断apk是不是已经安装
     * @param context
     * @param packageName
     * @return
     */
    public static synchronized boolean isAppInstalled(Context context,String packageName) {
    	if(context == null || StringUtils.isEmpty(packageName)){
    		return false;
    	}
    	PackageManager pm = context.getPackageManager();
    	boolean installed =false;
    	try {
    		pm.getPackageInfo(packageName,0);
    		installed =true;
    	} catch(NameNotFoundException e) {
    		installed =false;
    	}
    	return installed;
    }
    
    /**
	 * 是否能够在系统中找到ApplicationInfo
	 * @param context
	 * @param packageName
	 * @return
	 */
    public static synchronized boolean canFindAppInfo(Context context,String packageName){
		if(ApkUtils.getApplicationInfo(context, packageName) != null){
			return true;
		}else{
			return false;
		}
	}
    
    /**
     * 根据包名获取ApplicationInfo
     * @param context
     * @param packageName
     * @return
     */
    public static synchronized ApplicationInfo getApplicationInfo(Context context,String packageName){
    	if(context == null || StringUtils.isEmpty(packageName)){
    		return null;
    	}
    	ApplicationInfo appinfo = null;
		try {
			appinfo = context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return appinfo;
    } 
    
    /**
     * 根据包名获取ApplicationInfo
     * @param context
     * @param packageName
     * @return
     */
    public static synchronized PackageInfo getPackageInfo(Context context,String packageName){
    	if(context == null || StringUtils.isEmpty(packageName)){
    		return null;
    	}
    	
    	PackageInfo packageInfo = null;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return packageInfo;
    } 
    
    /**
     * 根据uid 获取应用的包名
     * @param context
     * @param uid
     * @return
     */
    public static synchronized String getPackageName(Context context,int uid){
    	if(context == null){
    		return null;
    	}
    	String packageName = "";
		try {
			packageName = context.getPackageManager().getNameForUid(uid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return packageName;
    } 
    
    /**
     * 获取指定UID对应的所有包名
     * @param context
     * @param uid
     * @return
     */
    public static synchronized String[] getPackagesForUid(Context context,int uid){
    	if(context == null){
    		return null;
    	}
    	try{
    		return context.getPackageManager().getPackagesForUid(uid);
    	}catch (Exception e) {
    		e.printStackTrace();
			return null;
		}
    }
    
    /**
	 * 三方应用程序的过滤器
	 * @param info
	 * @return true 三方应用 false 系统应用
	 */
	public static synchronized boolean filterApp(Context context,String packageName) {		
		return filterApp(getApplicationInfo(context,packageName));
		
	}
    
    /**
	 * 三方应用程序的过滤器
	 * @param info
	 * @return true 三方应用 false 系统应用
	 */
	public static synchronized boolean filterApp(ApplicationInfo info) {
		if(info == null){
			return false;
		}
		return isUserApp(info);
	}
	
	public static boolean isSystemApp(ApplicationInfo info) {
		if(info == null){
			return false;
		}
        return ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0);  
    }  
  
    public static boolean isSystemUpdateApp(ApplicationInfo info) {  
    	if(info == null){
			return false;
		}
        return ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);  
    }  
  
    public static boolean isUserApp(ApplicationInfo info) {
    	if(info == null){
			return false;
		}
        return (!isSystemApp(info) && !isSystemUpdateApp(info));  
    }
	
	/**
	 * 该软件是否需要进行权限优化(需要优化即：推荐关闭的权限应该关闭，推荐打开的权限应该打开)
	 * @param appInfo
	 * @return true:表示需要进行权限优化
	 */
//	public static synchronized boolean isNeedOptimize(Context context,AppInfo appInfo){
//		if(appInfo == null ||
//				!appInfo.getIsInstalled() || 
//				appInfo.getPermission() == null){
//			return false;
//		}
		
//		for(int i =0;i < appInfo.getPermission().size();i++){
//			PermissionInfo tmpPermissionInfo = appInfo.getPermission().get(i);
			/*if(isDangerPermission(context,appInfo,tmpPermissionInfo)){
				if(tmpPermissionInfo.getIsOpen()){
				   return true;
				}
			}else{	 modify by gaoming*/			
				//如果用户已经关掉了一个应用的联网权限，按照产品需求不需要重新打开
//				if(tmpPermissionInfo.permId != SDKConstants.PERM_ID_NETDEFAULT 
//						&& !tmpPermissionInfo.getIsOpen() && tmpPermissionInfo.permId != SDKConstants.PERM_ID_SENDMMS){
//					return true;
//				}modify by gaoming
//			}
//		}	
//        return false;
//	}
	
	/**
	 * 判断与LBE的权限管理功能是否可用
	 * @return
	 */
//	public static synchronized boolean isLBEServiceConnect(){
//		return false;		
//		LBEmodel instance = LBEmodel.getInstance();
//    	if(instance == null){
//			return false;
//		}else{
//			return instance.isOpenedLBEFunc();
//		}	modify by gaoming
//    }
	
	/**
	 * 更新所有应用的权限
	 */
	/*public static synchronized void updateAllAppsPermission(Context context){
		if(context == null){
			return ;
		}
		AppInfoModel appInfoModel = ConfigModel.getInstance(context).getAppInfoModel();
		AppsInfo userAppsInfo = appInfoModel.getThirdPartyAppsInfo();   	
    	if(userAppsInfo != null){
    		for(int i=0;i<userAppsInfo.size();i++){
        		AppInfo appInfo = (AppInfo)userAppsInfo.get(i);
        		if(appInfo == null ||
        				!appInfo.getIsInstalled()){
        			continue;
        		}
        		if(appInfo.getPermission() == null || appInfo.getPermission().size() == 0){
        			appInfo.setPermission(getPermission(context,appInfo.getPackageName()));	
        		}       		
        		if(appInfo.getPermission() != null && appInfo.getPermission().size() > 0){
        			updateAppPermission(context,appInfo);
        		}       		
        	}
    	}
    	
    	AppsInfo sysAppsInfo = appInfoModel.getSysAppsInfo();
    	if(sysAppsInfo != null){
    		for(int i=0;i<sysAppsInfo.size();i++){
        		AppInfo appInfo = (AppInfo)sysAppsInfo.get(i);
        		if(appInfo == null ||
        				!appInfo.getIsInstalled()||
        				!appInfo.getIsSysWhiteApp()){
        			continue;
        		}
        		if(appInfo.getPermission() != null && appInfo.getPermission().size() > 0){
        			updateAppPermission(context,appInfo);
        		}
        	}
    	}
	}*/
	
	/**
	 * 更新指定应用的权限,判断有没有联网权限，判断需不需要优化
	 * @param appInfo
	 */
	/*public static synchronized void updateAppPermission(Context context,AppInfo appInfo){
		if(context == null || appInfo == null){
			return ;
		}
		
	    if(appInfo.getIsUserApp()){ 			 
 			if(isNeedOptimize(context,appInfo)){
 				appInfo.setIsNeedOptimize(true);
 			}else{
 				appInfo.setIsNeedOptimize(false); 
 			}
 	    }
 	    
		if(isCanLinkNetApp(context,appInfo)){
			appInfo.setIsHaveNetworkingPermission(true);
			if(!appInfo.getIsUserApp()){
				*//**
				 * 由于系统应用没有扫描权限，所以对于有联网权限的系统应用，在这里添加联网权限的数据
				 *//*
				ArrayList<PermissionInfo> permList = new ArrayList<PermissionInfo>();
				PermissionInfo tmp = new PermissionInfo();
//				tmp.permId = SDKConstants.PERM_ID_NETDEFAULT;
//				LBEmodel.getInstance(context).getPermissionState(appInfo.getPackageName(), tmp); modify by gaoming
				permList.add(tmp);
				appInfo.setPermission(permList);
			}
		}else{
			appInfo.setIsHaveNetworkingPermission(false);
		}
	}*/
	
	/**
	 * 获取应用的完整属性,包含权限属性
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static synchronized AppInfo getAppFullInfo(Context context,
			String packageName){		
		if(context == null || StringUtils.isEmpty(packageName)){
			return null;
		}
		
		PackageInfo packageInfo = getPackageInfo(context,packageName);	
		return getAppFullInfo(context,packageInfo);
	}
	
	/**
	 * 获取应用的完整属性,包含权限属性
	 * @param context
	 * @param packageInfo
	 * @return
	 */
	public static synchronized AppInfo getAppFullInfo(Context context,
			PackageInfo packageInfo){		
		
		AppInfo appInfo = getAppBasicInfo(context,packageInfo);
		if(appInfo == null){
			return null;
		}
    	/*appInfo.setPermission(getPermission(context,appInfo.getPackageName()));	 
		if(isNeedOptimize(context,appInfo)){
			appInfo.setIsNeedOptimize(true);
		}else{
			appInfo.setIsNeedOptimize(false); 
		}*/
		if(isCanLinkNetApp(context,appInfo)){
			appInfo.setIsHaveNetworkingPermission(true);
			if(!appInfo.getIsUserApp()){
				/**
				 * 由于系统应用没有扫描权限，所以对于有联网权限的系统应用，在这里添加联网权限的数据
				 */
//				ArrayList<PermissionInfo> permList = new ArrayList<PermissionInfo>();
//				PermissionInfo tmp = new PermissionInfo();
//				tmp.permId = SDKConstants.PERM_ID_NETDEFAULT;
//				LBEmodel.getInstance(context).getPermissionState(appInfo.getPackageName(), tmp);modify by gaoming
//				permList.add(tmp);
//				appInfo.setPermission(permList);
			}
		}else{
			appInfo.setIsHaveNetworkingPermission(false);
		}	
		return appInfo;
	}
	
	/**
	 * 获取应用的基本属性,不包含权限属性
	 * @param context
	 * @param packageInfo
	 * @return
	 */
	public static synchronized AppInfo getAppBasicInfo(Context context,
			PackageInfo packageInfo){
		if(context == null || packageInfo == null){
			return null;
		} 
        AppInfo appInfo = new AppInfo();       
        appInfo.setApplicationInfo(packageInfo.applicationInfo);   
 	    appInfo.setPackageName(packageInfo.packageName);
 	    appInfo.setUid(packageInfo.applicationInfo.uid);
 	    appInfo.setVersionCode(packageInfo.versionCode);
 	    
 	    if(filterApp(packageInfo.applicationInfo)&&(!packageInfo.packageName.equals("com.baidu.map.location"))){
 	    	appInfo.setIsUserApp(true);
 	    }else{
 	    	appInfo.setIsUserApp(false);
 	    	appInfo.setIsHome(isHome(context,packageInfo.packageName));
 	    	appInfo.setIsHaveLauncher(isHaveLauncher(context,packageInfo.packageName));
 	    }
 	    return appInfo;
	}
	
	/**
	 * 初始化应用的名称以及名称对应的拼音。
	 * 由于要处理多国语言，所以应用的名称以及名称对应的拼音需要不断更新
	 * 该方法有可能会引起堵塞，尽量放在子线程中使用
	 * @param context
	 * @param appInfo
	 */
	public static synchronized void initAppNameInfo(Context context,AppInfo appInfo){
		if(appInfo == null){
			return ;
		} 
		String appName = ApkUtils.getApkName(context,appInfo.getApplicationInfo());
        appInfo.setAppName(appName);
        appInfo.setAppNamePinYin(Utils.getSpell(appName));		
	}
	
	/**
	 * 是否有Launcher属性
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static synchronized boolean isHaveLauncher(Context context,String packageName){       
        ResolveInfo resolveInfo = getApkMainResolveInfo(context,packageName);
        if (resolveInfo != null) {
            return true;
        }else{
        	return false;
        }
    }
	
	/**
	 * 获取应用的主界面
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static synchronized ResolveInfo getApkMainResolveInfo(Context context,String packageName){
		if(context == null || packageName == null){
			return null;
		}
    	Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(packageName);
        List<ResolveInfo> homes = context.getPackageManager().queryIntentActivities(intent, 0);
        if ((homes != null && homes.size() > 0)) {
            return homes.get(0);
        }else{
        	return null;
        }
    }
  
    /**
     * 是否是桌面应用
     * @param context
     * @param packageName
     * @return
     */
    public static synchronized boolean isHome(Context context,String packageName){
    	if(context == null || packageName == null){
			return false;
		}
    	Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setPackage(packageName);
        List<ResolveInfo> homes = context.getPackageManager().queryIntentActivities(intent, 0);
        if ((homes != null && homes.size() > 0)) {
            return true;
        }else{
        	return false;
        }
    }

	/**
	 * 判断当前权限是不是危险权限
	 * @param appInfo
	 * @param permissionInfo
	 * @return
	 */
	/*public static synchronized boolean isDangerPermission(
			Context context,
			AppInfo appInfo,
			PermissionInfo permissionInfo){
		if(context == null || 
				appInfo == null ||
				!appInfo.getIsInstalled() || 
				permissionInfo == null){
			return false;
		}	
		
		return isDangerPermission(context,appInfo.getPackageName(),permissionInfo.permId);
	}*/
	
	/**
	 * 判断当前权限是不是危险权限
	 * @param appInfo
	 * @param permissionInfo
	 * @return
	 */
	/*public static synchronized boolean isDangerPermission(
			Context context,
			String pkgName,
			int permId){
		if(context == null ){
			return false;
		}	
		
		MyPkgPermission myPkgPermission = GetRecomPermsModel.
				getInstance(context).getPkgPermission(pkgName);
		if(myPkgPermission == null){
			return false;
		}
		
		if((permId & myPkgPermission.getPermissionsReject())
				== permId){
			return true;
		}else{
			return false;
		}
	}*/
	
	/**
	 * 优化某一个apk中所有权限
	 * @param context
	 * @param appInfo
	 */
//	public static synchronized void OptimizeOneApkAllPermission(Context context,
//			AppInfo appInfo){
//		if(context == null ||
//				appInfo == null ||
//				!appInfo.getIsInstalled() || 
//				appInfo.getPermission() == null){
//			return ;
//		}
//		
//		for(int i =0;i < appInfo.getPermission().size();i++){
//			PermissionInfo permissionInfo = appInfo.getPermission().get(i);
//			if(permissionInfo == null){
//				continue;
//			}
			/*if(isDangerPermission(context,appInfo,permissionInfo)){
				//关闭风险权限
				if(permissionInfo.getIsOpen()){
					closeApkAppointPermission(context,appInfo,permissionInfo);
				}
			}else{ modify by gaoming*/
				//打开普通权限
				//如果用户已经关掉了一个应用的联网权限，按照产品需求不需要重新打开
//				if(permissionInfo.permId != SDKConstants.PERM_ID_NETDEFAULT 
//						&& !permissionInfo.getIsOpen()){
//					openApkAppointPermission(context,appInfo,permissionInfo);
//				} modify by gaoming
//			}
//		}		
//	}
	
	/**
	 * 关掉某个apk中指定的某个权限
	 * @param context
	 * @param appInfo
	 */
//	public static synchronized void closeApkAppointPermission(Context context,
//			AppInfo appInfo,
//			PermissionInfo permissionInfo){
//		if(context == null ||
//				appInfo == null ||
//				!appInfo.getIsInstalled() ||
//				permissionInfo == null){
//			return ;
//		}
			
		/*if(LBEmodel.getInstance(context).closePermission(
				appInfo.getPackageName(),
				permissionInfo)){		
			if(appInfo.getIsUserApp()){
				updateNeedOptimizeInfo(context,appInfo);
			}
			PermissionSubject.getInstance().notifyObserversOfPermsStateChangeFunc(appInfo,permissionInfo);				
		}	modify by gaoming */
//	}
	
	/**
	 * 打开某个apk中指定的询问权限
	 * @param context
	 * @param appInfo
	 */
//	public static synchronized void promptApkAppointPermission(Context context,
//			AppInfo appInfo,
//			PermissionInfo permissionInfo){
//		if(context == null ||
//				appInfo == null ||
//				!appInfo.getIsInstalled() ||
//				permissionInfo == null){
//			return ;
//		}
		
		/*if(LBEmodel.getInstance(context).promptPermission(
				appInfo.getPackageName(),
				permissionInfo)){		
			if(appInfo.getIsUserApp()){
				updateNeedOptimizeInfo(context,appInfo);
			}				
			PermissionSubject.getInstance().notifyObserversOfPermsStateChangeFunc(appInfo,permissionInfo);	
		}	modify by gaoming*/
//	}
	
	
	/**
	 * 打开某个apk中指定的某个权限
	 * @param context
	 * @param appInfo
	 */
//	public static synchronized void openApkAppointPermission(Context context,
//			AppInfo appInfo,
//			PermissionInfo permissionInfo){
//		if(context == null ||
//				appInfo == null ||
//				!appInfo.getIsInstalled() ||
//				permissionInfo == null){
//			return ;
//		}
		
		/*if(LBEmodel.getInstance(context).openPermission(
				appInfo.getPackageName(),
				permissionInfo)){
			
			if(appInfo.getIsUserApp()){
				updateNeedOptimizeInfo(context,appInfo);
			}				
			PermissionSubject.getInstance().notifyObserversOfPermsStateChangeFunc(appInfo,permissionInfo);	
		}	modify by gaoming*/
//	}
	
	/**
	 * 更新软件是否需要优化的相关数据
	 * @param context
	 * @param appInfo
	 */
	public static synchronized void updateNeedOptimizeInfo(Context context,
			AppInfo appInfo){
		if(context == null ||
				appInfo == null ||
				!appInfo.getIsInstalled()){
			return ;
		}
		
//		if(isNeedOptimize(context,appInfo)){//该软件当前状态需要优化
//			if(appInfo.getIsNeedOptimize()){//该软件过去状态需要优化
//				return ;
//			}else{//该软件过去状态不需要优化
//				appInfo.setIsNeedOptimize(true);
//			}
//		}else{//该软件当前状态不需要优化
//			if(appInfo.getIsNeedOptimize()){//该软件过去状态需要优化
//				appInfo.setIsNeedOptimize(false);
//			}else{//该软件过去状态不需要优化
//				return ;
//			}
//		}
	}
		
	/**
	 * 关闭某个软件的自启动功能
	 * @param context
	 * @param appInfo
	 */
	public static synchronized void closeApkAutoStart(Context context,AppInfo appInfo){
		if(context == null || 
				appInfo == null ||
				!appInfo.getIsInstalled()){
			return ;
		}
		AutoStartData autoStartData = AutoStartModel.getInstance(context).
				getAutoStartData(appInfo.getPackageName());
		closeApkAutoStart(context,autoStartData, appInfo.getPackageName());		
	}
	
	/**
	 * 关闭某个软件的自启动功能
	 * @param context
	 * @param appInfo
	 */
	public static synchronized void closeApkAutoStart(Context context,AutoStartData autoStartData, String pkgName){
		if(context == null || autoStartData == null){
			return ;
		}
		ComponentName tmpComponent = null;
		
		//开机启动
		ResolveInfo resolveInfo = autoStartData.getBootReceiveResolveInfo();
    	if(resolveInfo != null){
    		tmpComponent = new ComponentName(
	    			resolveInfo.activityInfo.packageName,
	    			resolveInfo.activityInfo.name);
	    	
	    	setComponentEnabledState(context,
					tmpComponent,
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
    	} 
    	
    	List<ResolveInfo> resolveInfoList = autoStartData.getResolveInfoList();
    	if(resolveInfoList != null){
    		for(ResolveInfo tmpresolveInfo:resolveInfoList){
	    		if(tmpresolveInfo == null){
	    			continue;
	    		}
	    		tmpComponent = new ComponentName(
	    				tmpresolveInfo.activityInfo.packageName,
	    				tmpresolveInfo.activityInfo.name);
		    	
		    	setComponentEnabledState(context,
						tmpComponent,
						PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
	    	}
    	}
    	    	
    	//service
    	ServiceInfo[] serviceInfos = autoStartData.getServiceInfo();
    	if(serviceInfos != null){
    		for(ServiceInfo serviceInfo:serviceInfos){
	    		if(serviceInfo == null){
	    			continue;
	    		}
	    		tmpComponent = new ComponentName(serviceInfo.packageName,serviceInfo.name);
	    		setComponentEnabledState(context,
						tmpComponent,
						PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
	    	}
    	} 
    	
    	//receive
    	ActivityInfo[] receiveInfos = autoStartData.getReceiveInfo();
    	if(receiveInfos != null){
    		for(ActivityInfo receiveInfo:receiveInfos){
	    		if(receiveInfo == null){
	    			continue;
	    		}
	    		tmpComponent = new ComponentName(receiveInfo.packageName,receiveInfo.name);
	    		setComponentEnabledState(context,
						tmpComponent,
						PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
	    	}
    	} 
    	
/*    	LogUtils.printWithLogCat("vautostart", 
    			String.format("closeApkAutoStart: autostart flag of %s is set to false",pkgName));*/
    	
    	autoStartData.setIsOpen(false);
    	
    	HashSet<String> autostartApps = AutoStartAppProvider.loadAutoStartAppListInDB(context);
    	autoStartData.setAutoStartOfUser(autostartApps.contains(pkgName));
    	
	}
	
	/**
	 * 打开某个软件的自启动功能
	 * @param context
	 * @param appInfo
	 */
	public static synchronized void openApkAutoStart(Context context,AppInfo appInfo){
		if(context == null || 
				appInfo == null ||
				!appInfo.getIsInstalled()){
			return ;
		}
		
		AutoStartData autoStartData = AutoStartModel.getInstance(context).
				getAutoStartData(appInfo.getPackageName());
		openApkAutoStart(context,autoStartData,appInfo.getPackageName());
	}
	
	/**
	 * 打开某个软件的自启动功能
	 * @param context
	 * @param appInfo
	 */
	public static synchronized void openApkAutoStart(Context context,AutoStartData autoStartData,String pkgName){
		if(context == null || 
				autoStartData == null){
			return ;
		}
				
		ComponentName tmpComponent = null;
		
		//开机启动
		ResolveInfo resolveInfo = autoStartData.getBootReceiveResolveInfo();
    	if(resolveInfo != null){
    		tmpComponent = new ComponentName(
	    			resolveInfo.activityInfo.packageName,
	    			resolveInfo.activityInfo.name);
	    	
	    	setComponentEnabledState(context,
					tmpComponent,
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
    	}
    	
    	List<ResolveInfo> resolveInfoList = autoStartData.getResolveInfoList();
    	if(resolveInfoList != null){
    		for(ResolveInfo tmpresolveInfo:resolveInfoList){
	    		if(tmpresolveInfo == null){
	    			continue;
	    		}
	    		tmpComponent = new ComponentName(
	    				tmpresolveInfo.activityInfo.packageName,
	    				tmpresolveInfo.activityInfo.name);
		    	
		    	setComponentEnabledState(context,
						tmpComponent,
						PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
	    	}
    	}
    	
    	//service
    	ServiceInfo[] serviceInfos = autoStartData.getServiceInfo();
    	if(serviceInfos != null){
    		for(ServiceInfo serviceInfo:serviceInfos){
	    		if(serviceInfo == null){
	    			continue;
	    		}
	    		tmpComponent = new ComponentName(serviceInfo.packageName,serviceInfo.name);
	    		setComponentEnabledState(context,
						tmpComponent,
						PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
	    	}
    	}
    	
    	
    	//receive
    	ActivityInfo[] receiveInfos = autoStartData.getReceiveInfo();
    	if(receiveInfos != null){
    		for(ActivityInfo receiveInfo:receiveInfos){
	    		if(receiveInfo == null){
	    			continue;
	    		}
	    		tmpComponent = new ComponentName(receiveInfo.packageName,receiveInfo.name);
	    		setComponentEnabledState(context,
						tmpComponent,
						PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
	    	}
    	}
    	
    	LogUtils.printWithLogCat("vautostart", 
    			String.format("openApkAutoStart: autostart flag of %s is set to true",pkgName));
    	
    	autoStartData.setIsOpen(true);
    	
    	HashSet<String> autostartApps = AutoStartAppProvider.loadAutoStartAppListInDB(context);
    	autoStartData.setAutoStartOfUser(autostartApps.contains(pkgName));
	}
	
	/**
	 * 判断当前app是否有联网权限
	 * @param pm
	 * @param info
	 * @return true：有联网权限 false：没有联网权限
	 */
	private static synchronized boolean isCanLinkNetApp(Context context,AppInfo appInfo){
		if(context == null || 
				appInfo == null ||
				!appInfo.getIsInstalled()){
			return false;
		}	
//		return LBEmodel.getInstance(context).isHaveNetworkApp(appInfo.getPackageName());
		return false;
	}
	
	/**
	 * 关闭应用连接2G/3G网络的权限
	 */
//	public static synchronized void closeApkNetwork(Context context,AppInfo appInfo){
//		if(context == null || 
//				appInfo == null ||
//				!appInfo.getIsInstalled()){
//    		return ;
//    	}
//		PermissionInfo permissionInfo = null;
//		int size = appInfo.getPermission()==null?0:appInfo.getPermission().size();
//		for(int i=0;i<size;i++){
//			PermissionInfo tmp = appInfo.getPermission().get(i);
//			if(tmp != null && tmp.permId == SDKConstants.PERM_ID_NETDEFAULT){
//				permissionInfo = tmp;
//				break;
//			}modify by gaoming
//		}
//		closeApkAppointPermission(context,appInfo,permissionInfo);		
//	}
	
	/**
	 * 打开应用连接2G/3G网络的权限
	 */
//	public static synchronized void openApkNetwork(Context context,AppInfo appInfo){
//		if(context == null || 
//				appInfo == null ||
//				!appInfo.getIsInstalled()){
//    		return ;
//    	}
//		PermissionInfo permissionInfo = null;
//		int size = appInfo.getPermission()==null?0:appInfo.getPermission().size();
//		for(int i=0;i<size;i++){
//			PermissionInfo tmp = appInfo.getPermission().get(i);
//			if(tmp != null && tmp.permId == SDKConstants.PERM_ID_NETDEFAULT){
//				permissionInfo = tmp;
//				break;
//			} modify by gaoming
//		}
//		openApkAppointPermission(context,appInfo,permissionInfo);
//	}
	 
	/**
	 * 查询同类别的应用
	 */
	 public static synchronized MyArrayList<BaseData> querySameCategoryApp(Context context,Intent intent) {
		 MyArrayList<BaseData> listData = new MyArrayList<BaseData>();
		 if(context == null || intent == null){
			 return listData;
		 }
	     PackageManager pm = context.getPackageManager();

	     List<ResolveInfo> resolveInfos = null;
	     try{
	    	 resolveInfos = pm.queryIntentActivities(intent, 
	        		 PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_RESOLVED_FILTER);
	     }catch(Exception e){
	    	 e.printStackTrace();
	     }
         if(resolveInfos == null){
        	 return listData; 
         }
         
         int n = resolveInfos.size();
         for (int i = 0; i < n; i++) {
             ResolveInfo r0 = resolveInfos.get(i);
             for (int j = 1; j < n; j++) {
                 ResolveInfo riInfo = resolveInfos.get(j);
                 if (r0.priority != riInfo.priority || r0.isDefault != riInfo.isDefault) {
                     while (j < n) {
                    	 resolveInfos.remove(j);
                         n--;
                     }
                 }
             }
         }
         
         for (int i = 0; i < resolveInfos.size(); i++) {     
        	 ResolveInfo reInfo = resolveInfos.get(i);   	 
             SameCategoryAppInfo appInfo = new SameCategoryAppInfo();
             appInfo.setResolveInfo(reInfo);             
             if(reInfo.activityInfo != null){
            	 String pkg = reInfo.activityInfo.packageName;
            	 if(!isUserApp(getApplicationInfo(context,pkg)) &&
            			Constants.isPackageNameInList(Constants.iuniPackageNameList,pkg)){
            		 appInfo.setIsSysApp(true);
            	 }else{
            		 appInfo.setIsSysApp(false);
            	 }
             }else{
            	 appInfo.setIsSysApp(false);
             } 
             if(appInfo.getIsSysApp()){//如果是系统应用则把该应用放在第一的位置
            	 listData.add(0,appInfo);
             }else{
            	 listData.add(appInfo);
             }
         } 
         
         if(listData.size()>0){
        	 //增加取消默认启动软件的item
        	 SameCategoryAppInfo appInfo = new SameCategoryAppInfo();       
             appInfo.setIsSysApp(false);
             listData.add(0,appInfo);
         }
         return listData;
     }
	 
	 /**
	  * 清除当前设置的默认启动软件
	  * @param context
	  * @param packageName
	  */
	 public static synchronized  void clearDefStartApp(Context context,String packageName){
		 if(context == null || StringUtils.isEmpty(packageName) ){
			 return ;
		 }	
		 LogUtils.printWithLogCat("vhm2", String.format("clearDefStartApp: packageName = %s", packageName));
		 context.getPackageManager().clearPackagePreferredActivities(packageName);
	 }
	
    /**
     * 查询软件的如下数据
     * cacheSize  
	 * dataSize   
	 * codeSize  
     * @param context
     * @param pkgName
     * @param observer
     */
    public static synchronized void queryPacakgeSize(Context context,
    		                             String pkgName,
    		                             IPackageStatsObserver.Stub observer){
    	if(context == null || 
    			StringUtils.isEmpty(pkgName)||
    			observer == null){
    		return ;
    	}
//		PackageManager pm = context.getPackageManager();  
//		try {
//			 pm.getPackageSizeInfo(pkgName, observer);
//		}catch(Exception ex){
//    		ex.printStackTrace() ;
//    	} 
    	GetPkgSizeUtils.getInstance(context).queryPacakgeSize(pkgName, observer);
    }
    
   
    
    /**
     * 清除应用的缓存数据
     * @param context
     * @param pkgName
     * @param observer
     */
    public static synchronized void ClearAppCacheData(Context context,
            String pkgName,
            IPackageDataObserver.Stub observer){
    	
    	if(context == null || 
    			StringUtils.isEmpty(pkgName)||
    			observer == null){
    		return ;
    	}
    	PackageManager pm = context.getPackageManager();  
		try {			
			pm.deleteApplicationCacheFiles(pkgName, observer);
		}catch(Exception ex){
    		ex.printStackTrace() ;
    	}     	
    }
    
    /**
     * 静默卸载app
     * @param context
     * @param pkgName
     * @param observer
     */
    public static synchronized void SilenceUninstallApp(Context context,
            String pkgName,
            IPackageDeleteObserver.Stub observer){
    	
    	if(context == null || 
    			StringUtils.isEmpty(pkgName)){
    		return ;
    	}
    	PackageManager pm = context.getPackageManager();  
		try {			
			pm.deletePackage(pkgName, observer, 0);
		}catch(Exception ex){
    		ex.printStackTrace() ;
    	}     	
    }
    
    /**
     * 清除应用的用户数据
     * @param context
     * @param pkgName
     * @param observer
     */
    public static synchronized boolean ClearAppUserData(Context context,
            String pkgName,
            IPackageDataObserver.Stub observer){
    	
    	boolean flag = false;
    	
    	if(context == null || 
    			StringUtils.isEmpty(pkgName)||
    			observer == null){
    		return flag;
    	}
    	
    	ActivityManager am = (ActivityManager)
    			context.getSystemService(Context.ACTIVITY_SERVICE);
		try {			
			flag = am.clearApplicationUserData(pkgName, observer);
		}catch(Exception ex){
    		ex.printStackTrace() ;
    	} 
		return flag;
    }
    
    /**
     * 指定ComponentName 的状态
     * @param context
     * @param tmpCpName
     * @param targetState 
     *  (1)PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
     *  (2)PackageManager.COMPONENT_ENABLED_STATE_ENABLED
     *  (3)PackageManager.COMPONENT_ENABLED_STATE_DISABLED
     */
    public static boolean setComponentEnabledState(Context context, 
    		ComponentName tmpCpName, 
    		int targetState){
    	boolean result = true;
		PackageManager pm = context.getPackageManager();
		try {
			if(isAppInstalled(context, tmpCpName.getPackageName())){
				pm.setComponentEnabledSetting(tmpCpName, targetState, PackageManager.DONT_KILL_APP);
			}			
		} catch(Exception e) {
		    e.printStackTrace();
		}
		return result;
	}
    
    /**
     * 获取指定ComponentName 的状态
     * @param context
     * @param tmpCpName
     * @return true：打开  false关闭
     */
    public static boolean getComponentEnabledState(Context context, 
    	        	ComponentName tmpCpName)throws Exception{
    	if(context == null || tmpCpName == null){
    		return false;
    	}
    	
    	PackageManager pm = context.getPackageManager(); 

    	int autoStartState = pm.getComponentEnabledSetting(tmpCpName);
    	
    	if(autoStartState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED){
    		return false;
    	}else{
    		return true;
    	}
    }
    
    /**
     * 获取当前可用输入法列表
     * @param context
     * @return
     */
    public static List<InputMethodInfo> getEnabledInputMethodList(Context context){
    	if(context == null){
    		return null;	
    	}
        InputMethodManager mImm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> mImis = mImm.getEnabledInputMethodList();
        return mImis;
    }
    
    /**
     * 设置默认输入法
     * @param context
     * @param inputMethodId
     * @return
     */
    public static boolean setDefaultInputMethod(Context context,String inputMethodId){
    	if(context == null || inputMethodId == null){
    		return false;
    	}
    	
    	return Settings.Secure.putString(context.getContentResolver(), 
    			Settings.Secure.DEFAULT_INPUT_METHOD,
    			inputMethodId);  	
    }
    
    /**
     * 获取当前默认输入法的ID
     * @param context
     * @return
     */
    public static String getCurrentInputMethodId(Context context){
    	if(context == null){
    		return null;
    	}
    	String currentInputMethodId = Settings.Secure.getString(context.getContentResolver(), 
    			Settings.Secure.DEFAULT_INPUT_METHOD);
    	return currentInputMethodId;
    }
    
    /**
     * 由于系统存在一个bug：应用的缓存无论怎么清除都会保留12K的空间。
     * 后来和产品商量决定在显示上去规避这个bug，即只要缓存剩余12K的就直接显示为0B。
     * @param pStats
     */
    public static synchronized void dealPackageStats(PackageStats pStats){
    	if(pStats == null){
    		return ;
    	}
    	
    	if(pStats.cacheSize+pStats.externalCacheSize == 12*1024){//1024 = 1kb
			pStats.cacheSize = 0;
			pStats.externalCacheSize  = 0;
		}
    }
}
