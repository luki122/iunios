package com.netmanage.utils;

import com.netmanage.data.ConfigData;
import com.netmanage.model.ConfigModel;
import com.netmanage.model.CorrectFlowModel;
import com.netmanage.model.NetModel;
import com.netmanage.view.MaskedImage;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class ApkUtils {
	private final static String TAG = ApkUtils.class.getName();
	
	/**
     * 通过包名获取UID
     * @param packageName
     * @return
     */
    public static int getUidByPackageName(Context context,String packageName){
        int uid = -1;
        try {
	        PackageManager pm = context.getPackageManager();
	        ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);
	        uid = ai.uid;
	    } catch (NameNotFoundException e) {
	        e.printStackTrace();
	    }
        return uid;
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
        	PackageInfo packageInfo = pm.getPackageInfo(packageName,PackageManager.GET_PERMISSIONS);     	
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
        	PackageInfo packageInfo = pm.getPackageInfo(packageName,PackageManager.GET_PERMISSIONS);     	
        	return packageInfo.versionName;   	
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "";
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
    		pm.getPackageInfo(packageName,PackageManager.GET_ACTIVITIES);
    		installed =true;
    	} catch(PackageManager.NameNotFoundException e) {
    		installed =false;
    	}
    	return installed;
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
		
//		if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
//			// 代表的是系统的应用,但是被用户升级了. 用户应用
//			return true;
//		} else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
//			// 代表的用户的应用
//			return true;
//		}
//		return false;
		return isUserApp(info);
	}
	
	public static boolean isSystemApp(ApplicationInfo info) {  
        return ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0);  
    }  
  
    public static boolean isSystemUpdateApp(ApplicationInfo info) {  
        return ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);  
    }  
  
    public static boolean isUserApp(ApplicationInfo info) {  
        return (!isSystemApp(info) && !isSystemUpdateApp(info));  
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
	 * 发送广播，通知应用程序的权限状态改变了
	 * @param context
	 * @param packageName
	 */
	public static synchronized void sendBroadcastForPermissionStateChange(
			Context context,String packageName){
		if(context == null || packageName == null){
			return ;
		}
		Intent intent = new Intent(mConfig.PERMISSION_STATE_CHANGE_ACTION);
		intent.putExtra("package", packageName);
		intent.putExtra("fromPackage", Utils.getOwnPackageName(context));
		context.sendBroadcast(intent);
	}
	
	/**
	 * 获取当前已经使用的流量值
	 * @param context
	 * @return
	 */
	public static synchronized long getUsedFlow(Context context){
		long usedFlow = 0;
		if(context == null){
			return usedFlow;
		}
		String curImsi = Utils.getImsi(context);
		if (!StringUtils.isEmpty(curImsi)) {// 当前没有插入sim卡时，永远显示为0
			usedFlow = CorrectFlowModel.getInstance(context).getCorrectFlow()*1024+
					NetModel.getInstance(context).getTotalMoblieFlowForStatistics()/1024;
		}
		return usedFlow;
	}
	
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
    	 PackageManager pm = context.getPackageManager();
    	//下面这行代码，在E6机器上如果快速多次调用，会出现程序异常卡死退出（即使放在子线程也同样如此），让人蛋疼
    	 CharSequence label = applicationInfo.loadLabel(pm);
    	 String apkName = label != null ? label.toString() : applicationInfo.packageName;
         return apkName;
    }
	
	/**
	 * 向桌面发送广告，告知现在流量信息
	 * @param context
	 */
	public static synchronized void sendBroasdcastToLauncher(Context context){
		if(context == null){
			return ;
		}
		ConfigModel configModel = ConfigModel.getInstance();
		if(configModel == null){
			return ;
		}
		ConfigData configData = configModel.getConfigData();  
			
		int percent = 0;
		String flowNumStr = null;
        long usedFlow = getUsedFlow(context);
        long totalFlow = 0;
    	if(configData.isSetedFlowPackage()){		   		 	
    		totalFlow = configData.getMonthlyFlow()*1024;
    		long remainderFlow = totalFlow -usedFlow;
    		percent = MaskedImage.getProgressValue(totalFlow, remainderFlow);
    		flowNumStr = MaskedImage.getFlowNumStr(totalFlow, remainderFlow);
    	}else{
    		percent = MaskedImage.getProgressValue(0, usedFlow);
    		flowNumStr = MaskedImage.getFlowNumStr(0, usedFlow);
    	}
    	boolean isTrafficPlanSet = configData.isSetedFlowPackage();
    	boolean isTrafficPlanUsedOut = usedFlow>totalFlow;
        boolean shouldAlarm = FlowUtils.isWarningProgress(context);
    	
		Intent intent = new Intent(mConfig.LAUNCHER_NET_ICON_UPDATE_ACTION);
		intent.putExtra("percent", percent);
		intent.putExtra("flowNumStr",flowNumStr);
		intent.putExtra("isTrafficPlanSet", isTrafficPlanSet);
		intent.putExtra("isTrafficPlanUsedOut", isTrafficPlanUsedOut);
		intent.putExtra("shouldAlarm", shouldAlarm);
		context.sendBroadcast(intent);
		Log.i(TAG,"send to launcher percent="+percent+
				",flowNum="+flowNumStr+
				",isTrafficPlanSet="+isTrafficPlanSet+
				",isTrafficPlanUsedOut="+isTrafficPlanUsedOut+
				",shouldAlarm="+shouldAlarm);
	}
}
