package com.secure.utils;

import java.io.File;                                                                                               
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import android.content.Context;
import android.os.Environment;   
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;   
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.IMountService;
import android.os.ServiceManager;

public class StorageUtil {
	private static StorageUtil instance;
	public static final int ERROR = -1;
	private Context context;
	private String TAG = StorageUtil.class.getName();
	private AtomicBoolean isDuringUpdate = new AtomicBoolean(false);
	
	private long AvailableInternalSDMemorySize;
	private long totalInternalSDMemorySize;
	
	private long AvailableExternalSDMemorySize;
	private long totalExternalSDMemorySize;
	
	private boolean isInternalSDMemorySizeAvailable;
	private boolean isExternalSDMemorySizeAvailable;
	
	private long lastGetInfoTime;
	private final int GetInfoTimeInterval = 100;//10000;
	
	private List<Handler> callBackHandlers = new ArrayList<Handler>();
		
	private StorageUtil(Context context) {
		this.context = context.getApplicationContext();	
		lastGetInfoTime = 0;
		initOrUpdateThread();
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
	public void initOrUpdateThread(){
		//为了防止在E6上出现卡的情况，设置两次获取存储信息的时间间隔必须大于GetInfoTimeTimeInterval
		if(System.currentTimeMillis()-lastGetInfoTime<GetInfoTimeInterval){
			return ;
		}
		lastGetInfoTime = System.currentTimeMillis();
		
		if(isDuringUpdate.get()){
			return ;
		}
		isDuringUpdate.set(true);
		new Thread() {
			@Override
			public void run() {	
				LogUtils.printWithLogCat(TAG, "GET Memory Size");
				
				AvailableInternalSDMemorySize = ERROR;
				AvailableExternalSDMemorySize = ERROR;
				totalInternalSDMemorySize = ERROR;
				totalExternalSDMemorySize = ERROR;
				
                isInternalSDMemorySizeAvailable = false;
                isExternalSDMemorySizeAvailable = false;			                
                useAndroidFunc(context);                
				updateHandler.sendEmptyMessage(1);
				isDuringUpdate.set(false);
			}
		}.start();  
	}
	
	/**
	 * 用金立提供的方法获取
	 */
	private void useGioneeFunc(){
		initInternalSDMemorySize(context);
		initExternalSDMemorySize(context);
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
	 * 内部SD剩余空间
	 * @param AvailableInternalMemorySize
	 */
	public long getAvailableInternalMemorySize(){
		return AvailableInternalSDMemorySize;
	}
	
	/**
	 * 内部存储总空间
	 * @return
	 */
	public long getTotalInternalSDMemorySize(){
		return totalInternalSDMemorySize;
	}
	
	/**
	 * 外部SD剩余空间
	 * @param AvailableExternalMemorySize
	 */
	public long getAvailableExternalMemorySize(){
		return AvailableExternalSDMemorySize;
	}
	
	/**
	 * 外部存储总空间
	 * @return
	 */
	public long getTotalExternalSDMemorySize(){
		return totalExternalSDMemorySize;
	}
	
	/**
	 * 内部SD卡是否可用
	 * @return
	 */
	public boolean isInternalSDMemorySizeAvailable(){
		return this.isInternalSDMemorySizeAvailable;
	}
	
	/**
	 * 外部sd卡是否可用
	 * @return
	 */
	public boolean isExternalSDMemorySizeAvailable(){
		return this.isExternalSDMemorySizeAvailable;
	}
		
    /**
     * SDCARD是否存
     */
    private boolean externalMemoryAvailable(){
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }
  
    /**
     * 获取内部SDCARD存储信息
     */
    private void initInternalSDMemorySize(Context context){ 					
    	if(context == null){
    		return ;
    	}
    	try{
    	   String pathStr = gionee.os.storage.GnStorageManager
    				.getInstance(context).getInternalStoragePath(); 
    	   
    	   if(pathStr != null){
    		  isInternalSDMemorySizeAvailable=true;
    		  android.os.StatFs stat = new android.os.StatFs(pathStr);
         	 
              long blockSize = stat.getBlockSize();
              long availableBlocks = stat.getAvailableBlocks();         
              AvailableInternalSDMemorySize = stat.getAvailableBlocks() * blockSize;
              totalInternalSDMemorySize = stat.getBlockCount() * blockSize; 
    	   }
    	}catch(Exception e){
           e.printStackTrace();
    	}
    }
    
    /**
     * 获取外部SDCARD存储信息
     */
    private void initExternalSDMemorySize(Context context){ 			
    	if(context == null){
    		return ;
    	}
    	try{
		  String pathStr = gionee.os.storage.GnStorageManager
					.getInstance(context).getExternalStoragePath(); 
          if(pathStr != null){
        	  isExternalSDMemorySizeAvailable=true;
          	  StatFs stat = new StatFs(pathStr);
              long blockSize = stat.getBlockSize();
              long availableBlocks = stat.getAvailableBlocks();         
              AvailableExternalSDMemorySize = stat.getAvailableBlocks() * blockSize;
              totalExternalSDMemorySize = stat.getBlockCount() * blockSize;   
          }
    	}catch(Exception e){
           e.printStackTrace();
    	}
    }
    
    public static void releaseObject(){
    	if(instance != null){
    		if(mConfig.SET_NULL_OF_CONTEXT){
				instance.context = null;
			}	
    	}
		instance = null;
	}
    
    /***********************新的获取方式***************************/
    private IMountService mountService ;
    private StorageManager mStorageManager;
       
	private void useAndroidFunc(Context context){				
		if(context == null){
			return ;
		}
		if(mStorageManager==null){
			mStorageManager = (StorageManager)
					context.getSystemService(Context.STORAGE_SERVICE);
		}		
		StorageVolume[] volumeList = null;
		try{
			volumeList = mStorageManager.getVolumeList();
		}catch(Exception e){
			e.printStackTrace();
		}
		int size = volumeList==null?0:volumeList.length;
		LogUtils.printWithLogCat(TAG, "volumeList Size="+size);
		for(int i=0;i<size;i++){
			StorageVolume tmp = volumeList[i];
			if(tmp == null || tmp.getPath() == null){
				continue;
			}
			if(!sdIsMounted(tmp.getPath())){
				LogUtils.printWithLogCat(TAG, "Path="+tmp.getPath()+" Is umounte");
				continue;
			}
			try{
            	android.os.StatFs stat = new android.os.StatFs(tmp.getPath());     
            	long blockSize = stat.getBlockSize();                
                long availableSize = stat.getAvailableBlocks() * blockSize;
                long totalSize = stat.getBlockCount() * blockSize; 
                LogUtils.printWithLogCat(TAG, "Path="+tmp.getPath()+
                		",availableSize="+availableSize+
                		",totalSize="+totalSize);
                if(i==0){
                    isInternalSDMemorySizeAvailable = true;        
    				AvailableInternalSDMemorySize = availableSize;
    				totalInternalSDMemorySize = totalSize;
    			}else{
    			    isExternalSDMemorySizeAvailable = true;
    				AvailableExternalSDMemorySize =+ availableSize;
    				totalExternalSDMemorySize =+ totalSize;
    			}
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	} 
//			LogUtils.printWithLogCat(TAG,
//            		"AvailableInternalSDMemorySize="+AvailableInternalSDMemorySize+
//            		",totalInternalSDMemorySize="+totalInternalSDMemorySize +
//            		",AvailableExternalSDMemorySize="+AvailableExternalSDMemorySize+
//            		",totalExternalSDMemorySize="+totalExternalSDMemorySize );
		}		
	}
	
	/**
	 * 判断当前sd的路径是否挂在
	 * @param mount
	 * @return
	 */
	private boolean sdIsMounted(String mount) {
		try {
			if(mountService == null){
				mountService = IMountService.Stub
						.asInterface(ServiceManager.getService("mount"));
			}
			
			if (mountService.getVolumeState(mount).equals(
					android.os.Environment.MEDIA_MOUNTED)) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
    
}
