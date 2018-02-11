package com.android.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class ApkUtils {	

    
    /**
     * 根据权限名获取权限详细信息
     * @param context
     * @param permissionName
     * @return
     */
    public static PermissionInfo getPermissionInfo(Context context , String permissionName){
    	if(context == null || permissionName == null){
    		return null;
    	}
    	
    	PackageManager pm = context.getPackageManager();
    	PermissionInfo permissionInfo = null;
		try {
			permissionInfo = pm.getPermissionInfo(permissionName, PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
    	return permissionInfo;
    }
    
    public static boolean isDangerPermission(PermissionInfo permissionInfo){
    	if(permissionInfo == null){
    		return false;
    	}
    	
    	if(permissionInfo.protectionLevel == PermissionInfo.PROTECTION_DANGEROUS){
    		return true;
    	}else{
    		return false;
    	}
    } 
}
