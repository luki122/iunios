package com.android.settings.lscreen;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import android.content.pm.ResolveInfo;

import android.content.pm.PackageManager.NameNotFoundException;

import android.graphics.drawable.Drawable;

import com.aurora.utils.Utils2Icon;

public class ApkUtils {
	
    
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
}
