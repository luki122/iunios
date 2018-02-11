package com.aurora.puremanager.utils;

import com.aurora.puremanager.receive.BootReceiver;
import com.aurora.puremanager.utils.ApkUtils;
import android.content.Context;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 * 广告插件扫描模块
 */
public class GetPkgSizeUtils{
    /**
     * 第一次调用pm.getPackageSizeInfo()需要睡眠的时间，
     * 由于Note3重启手机后立即去获取应用的空间信息会导致PackageManager堵塞，
     * 所以在第一次调用pm.getPackageSizeInfo()先睡眠一段时间，可以防止堵塞的发生。
     * 通过测试发现，从收到开机广播，30s后获取应用的大小，是不会堵塞的。
     */
    static final int SLEEP_TIME_FOR_FIRST = 30000;
    
    static Object sGlobalLock = new Object();
    static GetPkgSizeUtils sInstance;
    private final Object mLock = new Object();
    private final HandlerThread mBackgroundThread;
    private final BackgroundHandler mBackgroundHandler;  
    private final PackageManager pm;
    private boolean isCanGetSizeInfo;
       
    static public GetPkgSizeUtils getInstance(Context context) {
        synchronized (sGlobalLock) {
            if (sInstance == null) {
                sInstance = new GetPkgSizeUtils(context);
            }
            return sInstance;
        }
    }

    private GetPkgSizeUtils(Context context) {	
    	isCanGetSizeInfo = false;
        mBackgroundThread = new HandlerThread("GetPkgSizeUtils:Background");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());   
        pm = context.getPackageManager();  
    }
      
    public void queryPacakgeSize(
            String pkgName,
            IPackageStatsObserver.Stub observer){
    	synchronized (mLock){	
    		Message msg = mBackgroundHandler.obtainMessage();
    		msg.obj = new IncomingData(pkgName,observer);
			mBackgroundHandler.sendMessage(msg);
		}
    }
        
    final class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
        	try {
        		if(!isCanGetSizeInfo){
        			long curTime = System.currentTimeMillis();
        			if(curTime>BootReceiver.startSysTimeMillis &&
        					BootReceiver.startSysTimeMillis > 0){
        				long needSleepTime =SLEEP_TIME_FOR_FIRST-(curTime-BootReceiver.startSysTimeMillis);
        				if(needSleepTime>0){
        					Thread.sleep(needSleepTime);
        				}
        			}
        			isCanGetSizeInfo = true;
        		}
        		IncomingData incomingData =(IncomingData)msg.obj;         		
   			    pm.getPackageSizeInfo(incomingData.pkgName, incomingData.observer);
	   		}catch(Exception ex){
	       		ex.printStackTrace() ;
	       	} 
	    };
    }
   
	public static void releaseObject(){
		if(sInstance != null){			
			sInstance = null;
		}	
	}
	
	final class IncomingData{
		String pkgName;
		IPackageStatsObserver.Stub observer;
		public IncomingData(String pkgName,IPackageStatsObserver.Stub observer){
			this.pkgName = pkgName;
			this.observer = observer;
		}
	}
}
