package com.secure.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.secure.data.AppInfo;
import com.secure.model.AppInfoModel;
import com.secure.model.ConfigModel;
import com.secure.provider.AppInfosProvider;
import com.secure.service.IAidlService.Stub;
import com.secure.utils.ApkUtils;
import com.secure.utils.ServiceUtils;

public class AidlService extends Service {
	private static final int FUNC_OF_closeApkNetwork = 1;
	private static final int FUNC_OF_openApkNetwork = 2;
	private static final int FUNC_OF_wantGetDataFromContentProvider = 3;
		
	private MyHandler handler;
	private Context context;

	@Override
	public void onCreate() {
		handler = new MyHandler(Looper.getMainLooper());
		context = AidlService.this;
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		ServiceUtils.startServiceIfNeed(this);
		return binder;	
	}
	
	private class MyHandler extends Handler{		
		public MyHandler(Looper looper){
           super(looper);
        }

		@Override
	    public void handleMessage(Message msg) {  
			AppInfo appInfo;
			AppInfoModel appInfoModel = ConfigModel.getInstance(context).getAppInfoModel();
			switch(msg.what){
			case FUNC_OF_closeApkNetwork:
				appInfo = appInfoModel.findAppInfo((String)msg.obj);
		        ApkUtils.closeApkNetwork(AidlService.this,appInfo);
				break;
			case FUNC_OF_openApkNetwork:
				appInfo = appInfoModel.findAppInfo((String)msg.obj);
		        ApkUtils.openApkNetwork(AidlService.this,appInfo);
				break;
			case FUNC_OF_wantGetDataFromContentProvider:
	        	if(appInfoModel.isAlreadyGetAllAppInfo()){
	        		AppInfosProvider.notifyChangeForNetManageApp(context,null);		
	    	    }else{
	    	    	if(!appInfoModel.isDuringGetAllAppInfo()){
	    	    		appInfoModel.wantGetAllAppInfo(); 	    		
	    	    	}		    
	    	    }
				break;
			}
	    }
	}
		
	private IAidlService.Stub binder = new Stub(){		
		/**
		 * 关闭指定应用的联网权限
		 * @param packageName
		 * @param permId
		 * @return
		 */
		public boolean closeApkNetwork(String packageName) throws RemoteException {
			if(handler != null){
				Message msg = new Message();
				msg.what = FUNC_OF_closeApkNetwork;
				msg.obj = packageName;
				handler.sendMessage(msg);
			}
			return true;
		}
		
		/**
		 * 打开指定应用的联网权限
		 * @param packageName
		 * @return
		 */
        public boolean openApkNetwork(String packageName) throws RemoteException {
        	if(handler != null){
				Message msg = new Message();
				msg.what = FUNC_OF_openApkNetwork;
				msg.obj = packageName;
				handler.sendMessage(msg);
			}
			return true;
		}
        
        public void wantGetDataFromContentProvider() throws RemoteException{
        	if(handler != null){
				Message msg = new Message();
				msg.what = FUNC_OF_wantGetDataFromContentProvider;
				handler.sendMessage(msg);
			}
        }
	};	
}
