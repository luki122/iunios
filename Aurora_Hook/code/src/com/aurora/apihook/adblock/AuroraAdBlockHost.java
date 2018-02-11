package com.aurora.apihook.adblock;

import com.aurora.adblock.AdBlockClass;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.view.View;

public class AuroraAdBlockHost {	
	static Object sGlobalLock = new Object();
	static AuroraAdBlockHost sInstance;
	private final static String TAG =AuroraAdBlockHost.class.getName();

	private AuroraAdBlockInterface adBlockObject = null;	
	private boolean isUserApp;
	
	static public AuroraAdBlockHost getInstance(Context context) {
        synchronized (sGlobalLock) {
            if (sInstance == null) {
            	Log.i(TAG,"new AuroraAdBlockHost");
                sInstance = new AuroraAdBlockHost(context);
            }
            return sInstance;
        }
    }
	 
	public AuroraAdBlockHost(Context context){		
		isUserApp = isUserApp(context.getApplicationInfo());
		if(isUserApp){
			adBlockObject = new AdBlockClass();
		}		
	}
	
	/**
	 * 判断是否为广告视图，如果是，则隐藏；       
     * 添加的地方：
     * （1）public View(Context context)的末尾
     * （2）public View(Context context, AttributeSet attrs, int defStyle)的末尾
     * （3）public void setVisibility(int visibility)的末尾
	 * @param context
	 * @param object
	 * @return
	 */
     public boolean auroraHideAdView(Context context,View object){
    	 if(!isUserApp){
    		return false; 
    	 }
    	   	  
		 if(adBlockObject != null){
    		 return adBlockObject.auroraHideAdView(context,object);  
    	 }else{
    		 return false;
    	 }
     }	 
     
 	 private boolean isSystemApp(ApplicationInfo info) {
		if(info == null){
			return false;
		}
        return ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0);  
     }  
  
 	 private boolean isSystemUpdateApp(ApplicationInfo info) {  
    	if(info == null){
			return false;
		}
        return ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);  
     }  
  
 	 private boolean isUserApp(ApplicationInfo info) {
    	if(info == null){
			return false;
		}
        return (!isSystemApp(info) && !isSystemUpdateApp(info));  
     }
}
