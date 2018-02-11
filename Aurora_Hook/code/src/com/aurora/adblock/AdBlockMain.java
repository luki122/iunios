package com.aurora.adblock;

import java.util.HashMap;
import com.adblock.data.AidlAdData;
import com.secure.service.IAdBlockService;
import com.secure.service.IAdBlockServiceCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * 知识点：通过打印日志发现，如果IAdBlockService所在的进程被杀死或者强行结束后，
 * 会与IAdBlockService服务解绑，如果IAdBlockService所在的进程重新启动，那么此时会自动与IAdBlockService服务重新绑定。
 */
public class AdBlockMain {
	static Object sGlobalLock = new Object();
	private static AdBlockMain instance;
	private Context context;
	private String curPkgName;
	private IAdBlockService iAdBlockService = null;
	private HashMap<String ,AidlAdData> adDataMap = null;//包名对应包含的广告类名
	private final Object mLock = new Object();
	private final String TAG = AdBlockMain.class.getName();
		
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
		
	/**
	 * 绑定服务
	 */
	private void bindNetFlowService(){
		new Thread() {
			@Override
			public void run() {	
				try{
					context.bindService(new Intent(
							IAdBlockService.class.getName()),connection,Context.BIND_AUTO_CREATE);
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
