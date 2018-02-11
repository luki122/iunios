package com.secure.model;

import android.content.Context;
import com.adblock.data.AdProviderData;
import com.secure.data.MyArrayList;
import com.secure.provider.AdLibProvider;
import com.secure.request.http.GetAdLibModel;
import com.secure.request.http.HttpModel;
import com.secure.utils.MySharedPref;
import com.secure.utils.mConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 广告库获取与维护模块
 */
public class AdLibModel {
    static Object sGlobalLock = new Object();
    static AdLibModel sInstance;

    Context mApplicationContext;
    private HashMap <String,AdProviderData> adLibMap = new HashMap <String,AdProviderData>();
    final Object mLock = new Object();
    private GetAdLibModel getAdLibModel ;
    private AtomicBoolean isDuringUpdateLibFromNet = new AtomicBoolean();
       
    static public AdLibModel getInstance(Context context) {
        synchronized (sGlobalLock) {
            if (sInstance == null) {
                sInstance = new AdLibModel(context);
            }
            return sInstance;
        }
    }

    private AdLibModel(Context context) {	
    	isDuringUpdateLibFromNet.set(false);
        mApplicationContext = context.getApplicationContext();
        getAdLibModel = new GetAdLibModel(context);
        initDataFromSqlit();
    }
    
    private void initDataFromSqlit(){
    	ArrayList<AdProviderData> adLibList = AdLibProvider.queryAllAdProvider(mApplicationContext);
    	int size = adLibList==null?0:adLibList.size();
    	if(size == 0){
    		MySharedPref.saveAlreadyReadAssetsAdlib(mApplicationContext, false);
    		MySharedPref.saveAdLibVersion(mApplicationContext, 0);
    	}
    	for(int i=0;i<size;i++){
    		AdProviderData adProviderData = adLibList.get(i);
    		if(adProviderData == null){
    			continue;
    		}
    		synchronized(adLibMap){
    			adLibMap.put(adProviderData.getProviderName(), adProviderData);
    		} 		
    	}   	
    }
    
    /**
     * 从服务器上获取特征库
     */
    public void initOrUpdateFromNet(){
    	if(isDuringUpdateLibFromNet.get()){
    		return ;
    	}
    	isDuringUpdateLibFromNet.set(true);
    	new Thread() {
    		@Override
    		public void run() { 
    			updateLibFromAssets();
    			updateLibFromNet();
    			isDuringUpdateLibFromNet.set(false);
    		}
    	}.start();
    }
    
    /**
     * 读取assets中的adlib数据
     */
    private void updateLibFromAssets(){
    	if(!MySharedPref.getAlreadyReadAssetsAdlib(mApplicationContext)){
        	getAdLibModel.readLocalAdLib();	
        	updateLib(mApplicationContext,
        			getAdLibModel.getNeedDeleteProviderName(),
        			getAdLibModel.getNeedAddOrModifyProvider());	
    	}	
    }
    
    /**
     * 联网更新数据
     */
    private void updateLibFromNet(){
    	getAdLibModel.postRequest();
    	if(!HttpModel.SUCCESS_CODE.equals(getAdLibModel.getErrorCode())){
    		return ;    	  		
    	}	
    	updateLib(mApplicationContext,
    			getAdLibModel.getNeedDeleteProviderName(),
    			getAdLibModel.getNeedAddOrModifyProvider());		
    }
    
    private void updateLib(Context context,
    		MyArrayList<String> needDeleteProviderName,
    		MyArrayList<AdProviderData> needAddOrModifyProvider ){
    	if(context == null){
    		return ;
    	}  	
    	boolean libUpdate = false;	
		int sizeDelete = needDeleteProviderName==null?0:needDeleteProviderName.size();
		for(int i=0;i<sizeDelete;i++){
			String providerName = needDeleteProviderName.get(i);
			if(providerName == null){
				continue ;
			}
			libUpdate = true;
			AdLibProvider.deleteDate(context, providerName);
			synchronized(adLibMap){
    			adLibMap.remove(providerName);
    		}
		}
		
		int sizeAddOrModify = needAddOrModifyProvider == null?0:needAddOrModifyProvider.size();
		for(int i=0;i<sizeAddOrModify;i++){
			AdProviderData adProviderData = needAddOrModifyProvider.get(i);
			if(adProviderData == null){
				continue;
			}
			libUpdate = true;
			AdLibProvider.insertDate(context, adProviderData);
			synchronized(adLibMap){
    			adLibMap.put(adProviderData.getProviderName(), adProviderData);
    		}
		}
		if(libUpdate){
			AdScanModel.getInstance(context).scanForAdLibUpdate();
		}		
    }
         
    public HashMap <String,AdProviderData> getAdLibMap(){
	   synchronized(adLibMap){
		   return this.adLibMap;
	   }	  
    }
    
    public AdProviderData getAdProviderData(String providerName){
    	if(providerName == null){
    		return null;
    	}
    	
    	synchronized(adLibMap){
  		   return adLibMap.get(providerName);
  	    }
    }
    
    /**
     * 获取appsAdMap的key列表
     * @return 返回不会为null
     */
    public MyArrayList<String> getKeyList(){
    	MyArrayList<String> keyList = new MyArrayList<String>();
    	synchronized(adLibMap){		
			Set<String> pkgNames = adLibMap.keySet();
 		    for (String pkg: pkgNames){
 		    	keyList.add(pkg);
 		    } 
		}
    	return keyList;
    }
    
    /**
     * 获取adProviderList
     * @return 返回不会为null,每次调用都会创建新的MyArrayList对象
     */
    public MyArrayList<AdProviderData> getAdProviderList(){  
    	MyArrayList<AdProviderData> adProviderList = new MyArrayList<AdProviderData>();    	
    	MyArrayList<String> keyList = getKeyList();  	
    	AdProviderData adProviderData = null;    	
    	for(int i=0;i<keyList.size();i++){
    		synchronized(adLibMap){		
    			adProviderData = adLibMap.get(keyList.get(i));
    		}
    		if(adProviderData != null){
    			adProviderList.add(adProviderData);
    		}
    	}
    	return adProviderList;    	
    }
        
	public static void releaseObject(){
		if(sInstance != null){
			
			if(mConfig.SET_NULL_OF_CONTEXT){
				sInstance.mApplicationContext = null;
			}
			synchronized(sInstance.adLibMap){
				sInstance.adLibMap.clear();
			}					
			sInstance = null;
		}	
	}
}
