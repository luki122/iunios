package com.aurora.apihook.pms;

import java.util.ArrayList;
import java.util.List;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ParceledListSlice;
import android.os.Binder;
import android.os.Process;
import android.util.Log;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;

public class PmsHook implements Hook {

	private static final String TAG=PmsHook.class.getName();
	
    public void after_getPackageInfo(MethodHookParam param){   	
    	PackageInfo result =(PackageInfo)param.getResult(); 
    	if(result == null){
    		return ;
    	}
    	int callingUid = Binder.getCallingUid();
		if(auroraIsPrivacyApp(result,callingUid,param)){
			param.setResult(null);
		}else{
			param.setResult(result);
		}
	}
    
    public void after_getApplicationInfo(MethodHookParam param){   	
    	ApplicationInfo result =(ApplicationInfo)param.getResult(); 
    	if(result == null){
    		return ;
    	}
    	int callingUid = Binder.getCallingUid();
		if(auroraIsPrivacyApp(result,callingUid,param)){
			param.setResult(null);
		}else{
			param.setResult(result);
		}
	}
    
    public void after_getInstalledPackages(MethodHookParam param){   	
    	ParceledListSlice<PackageInfo> result =(ParceledListSlice<PackageInfo>)param.getResult(); 
    	if(result == null){
    		return ;
    	}
    	int callingUid = Binder.getCallingUid();
    	List<PackageInfo> needDeleteList = new ArrayList<PackageInfo>(); 
    	List<PackageInfo> resultList = result.getList(); 	
    	int size = resultList==null?0:resultList.size();	
    	for(int i=0;i<size;i++){
    		PackageInfo info = resultList.get(i);
    		if(auroraIsPrivacyApp(info,callingUid,param)){
    			needDeleteList.add(info);
    		}
    	}
    	for(int i=0;i<needDeleteList.size();i++){
    		resultList.remove(needDeleteList.get(i));
    	}
    	param.setResult(result);
	}
	
    public void after_getInstalledApplications(MethodHookParam param){   	
    	ParceledListSlice<ApplicationInfo> result =(ParceledListSlice<ApplicationInfo>)param.getResult(); 
    	if(result == null){
    		return ;
    	}
    	int callingUid = Binder.getCallingUid();
    	List<ApplicationInfo> needDeleteList = new ArrayList<ApplicationInfo>(); 
    	List<ApplicationInfo> resultList = result.getList(); 	
    	int size = resultList==null?0:resultList.size();	
    	for(int i=0;i<size;i++){
    		ApplicationInfo info = resultList.get(i);
    		if(auroraIsPrivacyApp(info,callingUid,param)){
    			needDeleteList.add(info);
    		}
    	}
    	for(int i=0;i<needDeleteList.size();i++){
    		resultList.remove(needDeleteList.get(i));
    	}
    	param.setResult(result);
	}

    /**
     * 判断当前应用是不是隐私应用
     * @param packageInfo
     * @param userId
     * @return
     */
    private boolean auroraIsPrivacyApp(PackageInfo packageInfo,
    		int callingUid,MethodHookParam param){
    	if(packageInfo == null){
    		return false;
    	}
    	return auroraIsPrivacyApp(packageInfo.applicationInfo,callingUid,param);
    }
    
    /**
     * 判断当前应用是不是隐私应用
     * @param packageInfo
     * @param userId
     * @return
     */
    private boolean auroraIsPrivacyApp(ApplicationInfo applicationInfo,
    		int callingUid,MethodHookParam param){
    	if(applicationInfo == null || 
    			auroraIsSystemApp(applicationInfo) || 
    			callingUid == Process.SYSTEM_UID){
    		return false;
    	}
    	
    	if(callingUid == auroraGetPackageinstallerUid(param)){
    		return false;
    	}else if(callingUid == auroraGetLbeSecurityUid(param)){
    		return false;
    	}
    	
    	if(applicationInfo.enabled){
    		return false;//当前应用可用，表示不是隐私应用
    	}else{
    		return true;
    	}    	
    }
    
    /**
     * 判断当前应用是不是系统应用
     * @param info
     * @return
     */
	private boolean auroraIsSystemApp(ApplicationInfo info) {
		if(info == null){
			return false;
		}
		if((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0 || 
				(info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0){
			return true;
		}else{
			return false;
		}
    }
	
	private static int auroraPackageinstallerUid = 0;
	private static int auroraLbeSecurityUid = 0;
	
	/**
	 * 获取安装管理器的uid 
	 * @return
	 */
    private int auroraGetPackageinstallerUid(MethodHookParam param){
    	if(auroraPackageinstallerUid == 0){
    		Object resultObject = ClassHelper.callMethod(param.thisObject,
    				"getApplicationInfo",
    				"com.android.packageinstaller",0,0);
    		if(resultObject != null){
    			ApplicationInfo ai = (ApplicationInfo)resultObject;
    			auroraPackageinstallerUid = ai.uid;
    		}
    	}
    	return auroraPackageinstallerUid;
    }
    
    /**
	 * 获取LBE授权管理的uid 
	 * @return
	 */
    private int auroraGetLbeSecurityUid(MethodHookParam param){
    	if(auroraLbeSecurityUid == 0){	        
	        Object resultObject = ClassHelper.callMethod(param.thisObject,
    				"getApplicationInfo",
    				"com.lbe.security.iunios",0,0);
    		if(resultObject != null){
    			ApplicationInfo ai = (ApplicationInfo)resultObject;
    			auroraPackageinstallerUid = ai.uid;
    		}
    	}
    	return auroraLbeSecurityUid;
    }
}
