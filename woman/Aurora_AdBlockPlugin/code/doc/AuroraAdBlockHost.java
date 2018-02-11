package android.view;

import dalvik.system.DexClassLoader;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class AuroraAdBlockHost {
	
	static Object sGlobalLock = new Object();
	static AuroraAdBlockHost sInstance;
	final static String PluginPkg = "com.aurora.adblock";
	final static String PluginClassName = "com.aurora.adblock.AdBlockClass";
	private final static String TAG =AuroraAdBlockHost.class.toString();

	private AuroraAdBlockInterface adBlockObject = null;	
	private final Object mLock = new Object();
	private boolean isUserApp;
	
	static public AuroraAdBlockHost getInstance(Context context) {
        synchronized (sGlobalLock) {
            if (sInstance == null) {
                sInstance = new AuroraAdBlockHost(context);
            }
            return sInstance;
        }
    }
	 
	public AuroraAdBlockHost(Context context){		
		isUserApp = isUserApp(context.getApplicationInfo());
		if(isUserApp){
			loadPlugin(context);
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
    	 
//不用每次检测要不要加载插件，如果插件不存在，每次去加载会导致界面异常的卡顿    	 
//    	 boolean needloadPlugin = false;
//    	 synchronized (mLock){
//    		 if(adBlockObject == null){
//    			 needloadPlugin = true;
//    		 }
//    	 }
//    	 if(needloadPlugin){
//    		 loadPlugin(context); 
//    	 }
    	  
		 if(adBlockObject != null){
    		 return adBlockObject.auroraHideAdView(context,object);  
    	 }else{
    		 return false;
    	 }
     }
	 
     /**
      * 加载插件
      */
     @TargetApi(9)
	 private void loadPlugin(Context context){
    	if(context == null){
    		return ;
    	}
    	context = context.getApplicationContext();
    	
    	PackageInfo packageInfo = getPackageInfo(context,PluginPkg);
    	if(packageInfo == null){
    		Log.i(TAG,"pkg= "+PluginPkg+" is not install");
    		return ;
    	}
    	
    	String dexPath = packageInfo.applicationInfo.sourceDir;
    	String dexOutputDir = context.getApplicationInfo().dataDir;
    	String libPath = packageInfo.applicationInfo.nativeLibraryDir;
    	
    	DexClassLoader cl = new DexClassLoader(dexPath,
    			dexOutputDir,
    			libPath,
    			this.getClass().getClassLoader());   
    	try{
			Class<?> clazz = cl.loadClass(PluginClassName);
			synchronized (mLock){
				adBlockObject = (AuroraAdBlockInterface)clazz.newInstance();
			}			
		}catch(Exception e){
			e.printStackTrace();
			Log.i(TAG,"loadPlugin error:"+e.toString());
		}    	
     }
	 
	
	 /**
      * 根据包名获取ApplicationInfo
      * @param context
      * @param packageName
      * @return
      */
     private PackageInfo getPackageInfo(Context context,String packageName){
    	if(context == null || packageName == null){
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
