package com.netmanage.service;

import java.util.List;

import com.netmanage.data.CorrectFlowBySmsData;
import com.netmanage.model.ConfigModel;
import com.netmanage.model.NetModel;
import com.netmanage.model.NotificationModel;
import com.netmanage.utils.ApkUtils;
import com.netmanage.utils.NetworkUtils;
import com.netmanage.utils.Utils;
import com.netmanage.utils.mConfig;
import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.app.ActivityManagerNative;
import android.app.IProcessObserver;

public class WatchDogService extends Service{
	private final String TAG = WatchDogService.class.getName();
	private ActivityManager mActivityManager;
	
	@Override
	public void onCreate() {	
		try {
			ActivityManagerNative.getDefault().registerProcessObserver(mProcessObserver);
        } catch (Exception e) { }
		
		super.onCreate();		 
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {		
		NetModel.getInstance(this).resetNetInfo(); 
		startService(new Intent(this, SmsService.class));
		super.onStart(intent, startId);
	}
		
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {	
		try {
		  ActivityManagerNative.getDefault().unregisterProcessObserver(mProcessObserver);
		} catch (Exception e) { }
		super.onDestroy();
	}
			
	/**
	 * 监听应用开启
	 */
	private IProcessObserver mProcessObserver = new IProcessObserver.Stub(){
        @Override
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities){
            if(foregroundActivities){
            	handler.sendEmptyMessage(pid);
            }
        }

        /*@Override*/
        public void onImportanceChanged(int pid, int uid, int importance){}
        
        public void onProcessStateChanged(int pid, int uid, int procState){}

        @Override
        public void onProcessDied(int pid, int uid) { }
    };
	
	private Handler handler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
    		String curImsi = Utils.getImsi(WatchDogService.this);
	    	if(NetworkUtils.getNetState(WatchDogService.this) == NetworkUtils.NET_TYPE_MOBILE ||
	    			Process.myPid() == msg.what ){
	    		NetModel.getInstance(WatchDogService.this).resetNetInfo(); 
	    		NotificationModel.getInstance(WatchDogService.this).checkFlowNotify();
	    	}else if(curImsi != null && !curImsi.equals(
    				NetModel.getInstance(WatchDogService.this).getImsiOfLastGetFlow())){
    			NetModel.getInstance(WatchDogService.this).resetNetInfo(); 
    		}  
	    	NotificationModel.getInstance(WatchDogService.this).checkSimChangeNotify(curImsi);
	    	CorrectFlowBySmsData.getInstance(WatchDogService.this).checkAndDealSimChange(curImsi);	
	    }
	};
}
