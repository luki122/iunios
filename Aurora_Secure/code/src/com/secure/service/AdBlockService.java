package com.secure.service;

import java.util.HashMap;
import java.util.List;
import com.adblock.data.AppAdData;
import com.secure.data.AppInfo;
import com.secure.data.MyArrayList;
import com.secure.interfaces.AdObserver;
import com.secure.interfaces.AdSubject;
import com.secure.model.AdScanModel;
import com.secure.model.ConfigModel;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class AdBlockService extends Service implements AdObserver{
	private ConfigModel configModel = null;
	private HashMap <String,IAdBlockServiceCallback> mCallbackMap;
	final Object mLock = new Object();
	
    @Override
	public void onCreate() {
    	AdScanModel.getInstance(this).attach(this);
    	mCallbackMap = new HashMap <String,IAdBlockServiceCallback>();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		AdScanModel.getInstance(this).detach(this);
		synchronized (mLock){	
			mCallbackMap.clear();
		}	
		super.onDestroy();
	}

	@Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }
    
    private final IAdBlockService.Stub mBinder = new IAdBlockService.Stub() {
		@Override
		public void unregisterCallback(String clientPkgName)
				throws RemoteException {
			synchronized (mLock){	
				mCallbackMap.remove(clientPkgName);
			}		
		}

		@Override
		public void registerCallback(IAdBlockServiceCallback mCallback,
				String clientPkgName) throws RemoteException {
			synchronized (mLock){	
				mCallbackMap.put(clientPkgName,mCallback);
			}		
		}

		@Override
		public void getDataFromSecure(String clientPkgName) throws RemoteException {
			if(AdScanModel.getInstance(AdBlockService.this).isInitEnd()){
				getClientAdInfo(clientPkgName);
			}			
		}
    };
    
    private IAdBlockServiceCallback getCallback(String clientPkgName){
    	synchronized (mLock){	
			return mCallbackMap.get(clientPkgName);
		}
    }
    
    /**
     * 判断指定应用的广告拦截功能是否打开
     * @param pkg
     * @return
     */
    private boolean getIsBlocked(String pkg){
		if(configModel == null){
			configModel = ConfigModel.getInstance();
			if(configModel == null){
				return false;
			}
		}
				
		AppInfo appInfo = configModel.getAppInfoModel().findAppInfo(pkg, true);
		if(appInfo != null && appInfo.getIsBlockAd()){
			return true;
		}else{
			return false;
		}		
    }
    
    /**
     * 查找指定客户端的广告信息
     * @param clientPkgName
     */
    private void getClientAdInfo(String clientPkgName){
    	updateOfInStall(AdScanModel.getInstance(AdBlockService.this),clientPkgName);
    }

	@Override
	public void updateOfInit(AdSubject subject) {		
		MyArrayList<String> keyList = ((AdScanModel)subject).getKeyList();
		for(int i=0;i<keyList.size();i++){
			AppAdData appAdData = ((AdScanModel)subject).getAppAdData(keyList.get(i));
			if(appAdData != null && getIsBlocked(appAdData.getPkgName())){
				addAidlAdData(appAdData);
			}					
		}
	}

	@Override
	public void updateOfInStall(AdSubject subject, String pkgName) {	
		if(pkgName == null){
			return ;
		}
		AppAdData appAdData = ((AdScanModel)subject).getAppAdData(pkgName);
		if(appAdData != null && getIsBlocked(appAdData.getPkgName())){
			addAidlAdData(appAdData);
		}
	}

	@Override
	public void updateOfCoverInStall(AdSubject subject, String pkgName) {
		updateOfInStall(subject,pkgName);		
	}

	@Override
	public void updateOfUnInstall(AdSubject subject, String pkgName) {	
		if(pkgName != null){
			deleteAidlAdData(pkgName);
		}
	}

	@Override
	public void updateOfExternalAppAvailable(AdSubject subject,
			List<String> pkgList) {	
		int size = pkgList==null?0:pkgList.size();
		for(int i=0;i<size;i++){
			AppAdData appAdData = ((AdScanModel)subject).getAppAdData(pkgList.get(i));
			if(appAdData != null && getIsBlocked(appAdData.getPkgName())){
				addAidlAdData(appAdData);
			}					
		}	
	}

	@Override
	public void updateOfExternalAppUnAvailable(AdSubject subject,
			List<String> pkgList) {
		int size = pkgList==null?0:pkgList.size();	
		for(int i=0;i<size;i++){
			String pkgName = pkgList.get(i);
			deleteAidlAdData(pkgName);
		}
	}

	@Override
	public void updateOfSwitchChange(AdSubject subject, String pkgName,boolean swtich) {
		if(pkgName == null){
			return ;
		}
		if(swtich){
			AppAdData appAdData = ((AdScanModel)subject).getAppAdData(pkgName);
			if(appAdData !=null){						
				addAidlAdData(appAdData);
			}				
		}else{					
			deleteAidlAdData(pkgName);
		}
	}

	@Override
	public void updateOfAdLibUpdate(AdSubject subject) {
		updateOfInit(subject);
	}

	@Override
	public void updateOfManualUpdate(AdSubject subject) {
		updateOfInit(subject);		
	}
	
	private void addAidlAdData(AppAdData appAdData){
		IAdBlockServiceCallback mCallback = getCallback(appAdData.getPkgName());
		if(mCallback != null){
			try {
				mCallback.addAidlAdData(appAdData.createAidlAdData());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void deleteAidlAdData(String clientPkgName){
		IAdBlockServiceCallback mCallback = getCallback(clientPkgName);
		if(mCallback != null){
			try {
				mCallback.deleteAidlAdData(clientPkgName);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
}

    