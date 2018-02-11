package com.secure.data;

public class AppsInfo extends MyArrayList<BaseData>{
  
   public int getDangerAppsNum(){
	   int num = 0;		   
	   for(int i=0;i<size();i++){
		    AppInfo appInfo = (AppInfo)get(i);
	   		if(appInfo == null || 
	   				!appInfo.getIsInstalled() || 
	   				appInfo.getPermission() == null || 
	   				appInfo.getPermission().size() == 0){
	   			continue;
	   		}
	   		
	   		if(appInfo.getIsNeedOptimize()){
	   			num++;
	   		}
	   }
	   return num;
   }
   
   public AppInfo getAppInfoByName(String packageName){
	   AppInfo appInfo = null;
	   
	   for(int i = 0;i < size();i++)
	   {
		   appInfo = (AppInfo)get(i);
		   if(appInfo.getPackageName().equals(packageName))
		   {
			   return appInfo;
		   }
	   }
	   return null;
   }
   
   public int getForbidNetworkNum(){
	   int num = 0;
	   for(int i=0;i<size();i++){
		    AppInfo appInfo = (AppInfo)get(i);
		    if(appInfo == null || !appInfo.getIsInstalled()){
    			continue;
    		}
    		if(appInfo.getIsHaveNetworkingPermission()){
    			if(!appInfo.getIsNetPermissionOpen()){
    				num++;
        		}
    		} 
	   }
	   return num;
   }
}
