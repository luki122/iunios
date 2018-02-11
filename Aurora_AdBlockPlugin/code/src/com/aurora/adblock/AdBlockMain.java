package com.aurora.adblock;

import java.util.HashMap;
import java.util.List;

import com.adblock.data.AidlAdData;
import com.secure.service.IAdBlockService;
import com.secure.service.IAdBlockServiceCallback;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * 知识点：通过打印日志发现，如果IAdBlockService所在的进程被杀死或者强行结束后，
 * 会与IAdBlockService服务解绑，如果IAdBlockService所在的进程重新启动，那么此时会自动与IAdBlockService服务重新绑定。
 * @author chengrq
 */
public class AdBlockMain {
	static Object sGlobalLock = new Object();
	private static AdBlockMain instance;
	private Context context;
	private String curPkgName;
	private IAdBlockService iAdBlockService = null;
	private HashMap<String ,AidlAdData> adDataMap = null;//包名对应包含的广告类名
	private final Object mLock = new Object();
	private final String TAG = AdBlockMain.class.toString();
		
	public static AdBlockMain getInstance(Context context) {
		 synchronized (sGlobalLock) {
			if (instance == null) {
				instance = new AdBlockMain(context);
			}
			return instance;
		 }
	}
	
	private AdBlockMain(Context context) {
		this.context = context.getApplicationContext();
		curPkgName = this.context.getPackageName();
		if(curPkgName == null){
			curPkgName = "";
		}
		adDataMap = new HashMap<String ,AidlAdData>();
		bindNetFlowService();
	}
	public static Intent getExplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }
	/**
	 * 绑定服务
	 */
	private void bindNetFlowService(){
		new Thread() {
			@Override
			public void run() {	
				try{
					context.bindService(getExplicitIntent(context,new Intent(
							IAdBlockService.class.getName())),connection,Context.BIND_AUTO_CREATE);
				}catch(Exception e){
					LogUtils.printWithLogCat(TAG,"bind result=" + e.toString());
					e.printStackTrace();
				}  
			}
		}.start();		
	}
		
	private ServiceConnection connection = new ServiceConnection(){
		public void onServiceConnected(ComponentName name, IBinder service) {
			iAdBlockService = IAdBlockService.Stub.asInterface(service);			
			LogUtils.printWithLogCat(TAG,"onServiceConnected ,pkg="+curPkgName);	
			try {
				if(iAdBlockService != null){
					iAdBlockService.registerCallback(iAdBlockServiceCallback,curPkgName);	
				}	
				getAdClassFromSecureManage();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
		}

		public void onServiceDisconnected(ComponentName name) {
			LogUtils.printWithLogCat(TAG,"onServiceDisconnected ,pkg="+curPkgName);
			
			try {
				if(iAdBlockService != null){
					iAdBlockService.unregisterCallback(curPkgName);	
				}			
            } catch (RemoteException e) {
                e.printStackTrace();
            }finally{
            	iAdBlockService = null;	
            }					
		}
	};
	
	/**
	 * 从应用管理中获取需要拦截的类名
	 */
    private void getAdClassFromSecureManage(){
    	if(iAdBlockService != null){
			try{
				iAdBlockService.getDataFromSecure(curPkgName);
			}catch(RemoteException e){
				e.printStackTrace();
			}
		}
	}
    
    /**
     * @param pkgName
     * @return
     */
    public AidlAdData getAidlAdData(){
    	if(curPkgName == null || curPkgName.equals("")){
    		return null;
    	}
    	synchronized(mLock){
			return adDataMap.get(curPkgName);
		} 
    }
	
	private IAdBlockServiceCallback iAdBlockServiceCallback  = new IAdBlockServiceCallback.Stub(){
		
		@Override
		public void addAidlAdData(AidlAdData aidlAdData) throws RemoteException {
			LogUtils.printWithLogCat(TAG,"addAidlAdData pkg= "+curPkgName);
			if(aidlAdData != null && curPkgName.equals(aidlAdData.getPkgName())){
				synchronized(mLock){
					adDataMap.put(aidlAdData.getPkgName(), aidlAdData);
				}			
			}		
		}

		@Override
		public void deleteAidlAdData(String pkgName) throws RemoteException {
			LogUtils.printWithLogCat(TAG,"deleteAidlAdData pkg= "+curPkgName);
			if(pkgName != null){
				synchronized(mLock){
					adDataMap.remove(pkgName);
				}
			}
		}
	};
}
