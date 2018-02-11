package com.netmanage.utils;

import java.io.File;                                                                                               
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;   
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;   

public class StorageUtil {
	private static StorageUtil instance;
	private static final int ERROR = -1;
	private Context context;
	private AtomicBoolean isDuringUpdate = new AtomicBoolean(false);
	private BroadcastReceiver mExternalStorageReceiver;
	private long AvailableInternalMemorySize;
	private long AvailableExternalMemorySize;
	
	private List<Handler> callBackHandlers = new ArrayList<Handler>();
		
	private StorageUtil(Context context) {
		this.context = context;	
		startWatchingExternalStorage();
	}

	public static synchronized StorageUtil getInstance(Context context) {
		if (instance == null) {
			instance = new StorageUtil(context);
		}
		return instance;
	}  
	
	 /**
     * 注册观察者对象
     * @param observer
     */
    public void attach(Handler callBackHandler){
   	   try{
   		 callBackHandlers.add(callBackHandler);
   	   }catch(Exception e){
   		 e.printStackTrace();
   	   }   	 
    }
    
    /**
     * 删除观察者对象
     * @param observer
     */
    public void detach(Handler callBackHandler){
	   	 try{
	   	     callBackHandlers.remove(callBackHandler);
	   	 }catch(Exception e){
	   		 e.printStackTrace();
	   	 } 
    }
    
    private void startWatchingExternalStorage() {
        mExternalStorageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            	initOrUpdateThread();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        if(context != null){
        	context.registerReceiver(mExternalStorageReceiver, filter);
        }     
        initOrUpdateThread();
    }

    private void stopWatchingExternalStorage() {
    	try{
    		if(mExternalStorageReceiver != null && context != null){
        		context.unregisterReceiver(mExternalStorageReceiver);
        		mExternalStorageReceiver = null;
        	}
    	}catch(Exception e){
    		
    	} 	
    }
    
    /**
	 * 当前数据是不是正在初始化
	 * @return
	 */
	public boolean isDuringUpdate(){
		if(isDuringUpdate.get() ){
			return true;
		}else{
			return false;
		}	
	}
    
    /**
	 * 在子线程中初始化或更新数据
	 */
	private void initOrUpdateThread(){
		if(isDuringUpdate.get()){
			return ;
		}
		isDuringUpdate.set(true);
		new Thread() {
			@Override
			public void run() {	
				AvailableInternalMemorySize = intAvailableInternalMemorySize();
				AvailableExternalMemorySize = initAvailableExternalMemorySize();
				updateHandler.sendEmptyMessage(1);
				isDuringUpdate.set(false);
			}
		}.start();  
	}
	
	private final Handler updateHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	    	for(Handler handler : callBackHandlers){
				handler.sendEmptyMessage(0);
	   	    }
	    }
	};
	
	/**
	 * 内部存储器剩余空间
	 * @param AvailableInternalMemorySize
	 */
	public long getAvailableInternalMemorySize(){
		return this.AvailableInternalMemorySize ;
	}
	
	/**
	 * 外部存储器剩余空间
	 * @param AvailableExternalMemorySize
	 */
	public long getAvailableExternalMemorySize(){
		return this.AvailableExternalMemorySize;
	}
	
    /**
     * SDCARD是否存
     */
    private boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取手机内部剩余存储空间,（耗时操作，不要经常条用）
     * @return
     */
    private long intAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * 获取手机内部总的存储空间,（耗时操作，不要经常条用）
     * @return
     */
    private long initTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * 获取SDCARD剩余存储空间,（耗时操作，不要经常条用）
     * @return
     */
    private long initAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return ERROR;
        }
    }

    /**
     * 获取SDCARD总的存储空间,（耗时操作，不要经常条用）
     * @return
     */
    private long initTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } else {
            return ERROR;
        }
    }
    
    public static void releaseObject(){
    	if(instance != null){
    		instance.stopWatchingExternalStorage();
    		instance.context = null;
    	}
		instance = null;
	}
}
