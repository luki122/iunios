package com.privacymanage.utils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;

public class ApkUtils {    
    /**
     * 获取Activity的icon
     * @param context
     * @param packageName
     * @return
     */
    public static synchronized Drawable getActivityIcon(Context context,
    		String pkgName,String className){
    	return getActivityIcon(context,getActivityInfo(context,pkgName,className));
    }
    
    /**
     * 获取Activity的icon
     * @param context
     * @param packageName
     * @return
     */
    public static synchronized Drawable getActivityIcon(Context context,ActivityInfo activityInfo){
    	if(context == null || activityInfo == null){
			return null;
		}
    		
        PackageManager pm = context.getPackageManager();          
        Drawable appIcon = activityInfo.loadIcon(pm);  
        return appIcon;
    }
          
    /**
     * 获取Activity的名字
     * @param context
     * @param applicationInfo
     * @return
     */
    public static String getActivityLabel(Context context,
    		String pkgName,String className){
    	return getActivityLabel(context,getActivityInfo(context,pkgName,className));
    }
    
    /**
     * 获取Activity的名字
     * @param context
     * @param applicationInfo
     * @return
     */
    public static String getActivityLabel(Context context,ActivityInfo activityInfo){
    	 if(context == null || activityInfo == null){
    		return null;
    	 }
    	 PackageManager pm = context.getPackageManager();
    	 CharSequence label = activityInfo.loadLabel(pm);
    	 String name = label != null ? label.toString() : activityInfo.packageName;
         return name;
    }
    
    public static synchronized ActivityInfo getActivityInfo(Context context,
    		String pkgName,String className){
    	ActivityInfo activityInfo = null;
    	if(context == null || pkgName == null || className == null){
    		return activityInfo;
    	}
    	PackageManager pm = context.getPackageManager();
        try {
        	PackageInfo packageInfo = pm.getPackageInfo(pkgName,PackageManager.GET_ACTIVITIES);  
        	ActivityInfo[] activityInfos = packageInfo.activities;
        	int size = activityInfos==null?0:activityInfos.length;
        	for(int i=0;i<size;i++){
        		ActivityInfo tmp = activityInfos[i];
        		if(tmp == null){
        			continue ;
        		}
        		if(className.equals(tmp.name)){
        			activityInfo = tmp;
        		}
        	}
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }      
        return activityInfo;    	
    }

	public static boolean isLowVersion() {
		int version = android.os.Build.VERSION.SDK_INT;
		if (version <= 19) {
			return true;
		}
		return false;
	}
}
