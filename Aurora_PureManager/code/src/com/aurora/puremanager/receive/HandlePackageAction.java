package com.aurora.puremanager.receive;

import com.aurora.puremanager.model.ConfigModel;
import com.aurora.puremanager.model.DefSoftModel;
import com.aurora.puremanager.provider.AuroraAppInfosProvider;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.LogUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

public class HandlePackageAction extends BroadcastReceiver {	
	private static ReceiveData lastReceiveData = null;
	private String TAG = HandlePackageAction.class.getName();
	
	@Override
	public void onReceive(Context context, Intent intent) {  		
		if(intent != null && intent.getData() != null){
			String packageName = intent.getData().getSchemeSpecificPart();
			dealFunc1(context,packageName,intent.getAction());
		}	      
    } 
	
	private static final String CTS_PACKAGE_NAME = "android.tests.devicesetup";
	private static final String VERIFT_PACKAGE_NAME = "com.android.cts.verifier";
	/**
	 * 不区分覆盖安装，把覆盖安装看成先卸载，再安装
	 * @param context
	 * @param packageName
	 * @param action
	 */
	private void dealFunc1(Context context,String packageName,String action){
    	LogUtils.printWithLogCat(TAG,"PKG="+packageName+", ACTION="+action);
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)){
                //当进行CTS测试的时候，关闭LBE权限管理，以防弹出对话框
        	if(packageName.equals(CTS_PACKAGE_NAME)||packageName.equals(VERIFT_PACKAGE_NAME))
        	{
//        		LBEmodel.getInstance(context).closeLBEFunc();
        		return;
        	}
        	installOrCoverApk(context,packageName,true);
        }else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)){
        	unInstallApk(context,packageName);
        }else if(Intent.ACTION_PACKAGE_CHANGED.equals(action)){
    		if(DefSoftModel.getInstance() != null){
				 DefSoftModel.getInstance(context).initOrUpdateThread();
			}	
        }
	}
	
	/**
	 * 区分覆盖安装
	 * @param context
	 * @param packageName
	 * @param action
	 */
	private void dealFunc2(Context context,String packageName,String action){
	    /**
         * 1.卸载一个应用，对应的广播：
         *   Intent.ACTION_PACKAGE_REMOVED （注：在系统中找不到该应的ApplicationInfo）
         *   Intent.ACTION_PACKAGE_DATA_CLEARED
         *   
         * 2.安装一个应用，对应的广播：
         *   Intent.ACTION_PACKAGE_ADDED
         *   
         * 3.覆盖安装一个应用：
         *   Intent.ACTION_PACKAGE_REMOVED （注：在系统中找到该应的ApplicationInfo）
         *   Intent.ACTION_PACKAGE_ADDED
         *   Intent.ACTION_PACKAGE_REPLACED
         *   
         * 4.清除一个应用的用户数据：
         *   Intent.ACTION_PACKAGE_RESTARTED
         *   Intent.ACTION_PACKAGE_DATA_CLEARED
         *   
         * 5.一个应用被强制停止：
         *  Intent.ACTION_PACKAGE_RESTARTED
         */
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)){
        	if(lastReceiveData != null && 
        			packageName.equals(lastReceiveData.packageName)){//表示覆盖安装
        		lastReceiveData = null;
        		installOrCoverApk(context,packageName,false);
        	}else{//表示安装
        		installOrCoverApk(context,packageName,true);
        	}
        	LogUtils.printWithLogCat(TAG,"PKG="+packageName+", Intent.ACTION_PACKAGE_ADDED");
        }else if (Intent.ACTION_PACKAGE_CHANGED.equals(action)){  
        	LogUtils.printWithLogCat(TAG,"PKG="+packageName+", Intent.ACTION_PACKAGE_CHANGED");
        }else if(Intent.ACTION_PACKAGE_DATA_CLEARED.equals(action)){
        	LogUtils.printWithLogCat(TAG,"PKG="+packageName+", Intent.ACTION_PACKAGE_DATA_CLEARED");
        }else if(Intent.ACTION_PACKAGE_FIRST_LAUNCH.equals(action)){
        	LogUtils.printWithLogCat(TAG,"PKG="+packageName+", Intent.ACTION_PACKAGE_FIRST_LAUNCH"); 
        }else if(Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(action)){
        	LogUtils.printWithLogCat(TAG,"PKG="+packageName+", Intent.ACTION_PACKAGE_FULLY_REMOVED"); 
        }else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)){
        	LogUtils.printWithLogCat(TAG,"PKG="+packageName+", Intent.ACTION_PACKAGE_REMOVED");
        	boolean isCanFindInfo = canFindAppInfo(context,packageName);
        	if(isCanFindInfo){//表示覆盖安装
        		lastReceiveData = new ReceiveData();
        		lastReceiveData.action = action;
        		lastReceiveData.packageName = packageName;
        	}else{//表示卸载
        		unInstallApk(context,packageName);
        	}
        }else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)){ 
        	LogUtils.printWithLogCat(TAG,"PKG="+packageName+", Intent.ACTION_PACKAGE_REPLACED");
        }else if (Intent.ACTION_PACKAGE_RESTARTED.equals(action)){  
        	LogUtils.printWithLogCat(TAG,"PKG="+packageName+", Intent.ACTION_PACKAGE_RESTARTED");
        }
	}
	
	/**
	 * 是否能够在系统中找到ApplicationInfo
	 * @param context
	 * @param packageName
	 * @return
	 */
	private boolean canFindAppInfo(Context context,String packageName){
		if(ApkUtils.getApplicationInfo(context, packageName) != null){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 表示安装应用 或 覆盖安装应用
	 * @param context
	 * @param packageName
	 * @param isInstall
	 */
    private void installOrCoverApk(final Context context,final String packageName,final boolean isInstall){
    	LogUtils.printWithLogCat(TAG, 
				"安装应用："+packageName);
    	final Handler handler = new Handler() {
    	    @Override
    	    public void handleMessage(Message msg) {
    	    	installOrCoverApkWhitUpdateProvider(context,packageName,isInstall);
	        	/*if(LBEmodel.getInstance(context).isBindLBEService()){
	        		installOrCoverApkWhitUpdateProvider(context,packageName,isInstall);  					
	    		}else{
	    			LBEmodel.getInstance(context).bindLBEService(new BindServiceCallback(){
	    				@Override
	    				public void callback(boolean result) {
	    					installOrCoverApkWhitUpdateProvider(context,packageName,isInstall);  	
	    				}}
	    			);
	    		} 	 */   	
    	   }
    	};
    	new Thread() {
			@Override
			public void run() {	
				try{				
					Thread.sleep(1000);//必须要延时
				}catch(Exception e){
					e.printStackTrace();
				}
	            handler.sendEmptyMessage(0);   
			}
		}.start(); 
    }
    
    private void installOrCoverApkWhitUpdateProvider(final Context context,
    		final String packageName,
    		final boolean isInstall){
    	ConfigModel.getInstance(context).getAppInfoModel().installOrCoverPackage(packageName);
    }
    
    /**
     * 表示卸载应用
     * @param context
     * @param packageName
     */
    private void unInstallApk(Context context,String packageName){
    	LogUtils.printWithLogCat(TAG, 
				"卸载应用："+packageName);
    	AuroraAppInfosProvider.deleteDate(context, packageName);
        AuroraAppInfosProvider.notifyChangeForNetManageApp(context, null);
        
        ConfigModel instance = ConfigModel.getInstance();
	    if(instance != null){
	    	instance.getAppInfoModel().UninstallPackage(packageName);
	    } 
    }
    
    private class ReceiveData{
    	String action;
    	String packageName;
    }
}
