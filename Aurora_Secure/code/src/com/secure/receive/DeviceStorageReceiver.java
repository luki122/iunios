package com.secure.receive;

import com.secure.activity.CacheManageActivity;
import com.secure.activity.MainActivity;
import com.secure.data.AppInfo;
import com.secure.data.MainActivityItemData;
import com.secure.data.StorageLowNotifyData;
import com.secure.interfaces.Observer;
import com.secure.interfaces.Subject;
import com.secure.utils.LogUtils;
import com.secure.utils.MySharedPref;
import com.secure.utils.StorageUtil;
import com.secure.utils.Utils;
import com.secure.view.StorageNotification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import com.aurora.secure.R;

/**
 * 监听存储空间是否不足
 */
public class DeviceStorageReceiver extends BroadcastReceiver { 
	private final int MAX_NOTIFY_TIMES = 3;
	private final long NOTIFY_SPACE_TIME = 3*24*60*60*1000;
	private Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		
		StorageUtil.getInstance(context).attach(updateHandler);
		if(StorageUtil.getInstance(context).isDuringUpdate()){
    		//wait,由于耗时不太多所以这里不显示wait动画
    	}else{
    		updateHandler.sendEmptyMessage(1);
    	}
	}
	
    private final Handler updateHandler = new Handler() {
    	@Override
	    public void handleMessage(Message msg) {
    		if(context == null || 
    				!StorageUtil.getInstance(context).isInternalSDMemorySizeAvailable()){
    			return ;
    		}
    		long internalAvailable = StorageUtil.getInstance(context).getAvailableInternalMemorySize();
    		long externalAvailable = StorageUtil.getInstance(context).getAvailableExternalMemorySize();
			long internalTotal = StorageUtil.getInstance(context).getTotalInternalSDMemorySize();
			long externalTotal = StorageUtil.getInstance(context).getTotalExternalSDMemorySize();
			if(internalAvailable == StorageUtil.ERROR) internalAvailable = 0;
			if(externalAvailable == StorageUtil.ERROR) externalAvailable = 0;
			if(internalTotal == StorageUtil.ERROR) internalTotal = 0;
			if(externalTotal == StorageUtil.ERROR) externalTotal = 0;
				
			long available = internalAvailable+externalAvailable;
			long total = internalTotal+externalTotal;
			
			if(1.0*available/total>0.1){//空间充足			
				LogUtils.printWithLogCat(
						DeviceStorageReceiver.class.getName(), 
						"Intent.ACTION_DEVICE_STORAGE_OK");
				resetDate(context);
			}else{//空间不足							
				LogUtils.printWithLogCat(
						DeviceStorageReceiver.class.getName(), 
						"Intent.ACTION_DEVICE_STORAGE_LOW");
				StorageLowNotifyData data = MySharedPref.getStorageLowNotifyData(context);
				if(!data.getIsAlreadyLow()){
					myNotify(context);
					updateDate(context,data);
				}else{
					if(System.currentTimeMillis() < data.getLastNotifyTime()){//时间往前调，则重置原来数据
						resetDate(context);
						myNotify(context);
						updateDate(context,data);
						return ;
					}
					
					if(data.getAlreadyNotifyTimes()<MAX_NOTIFY_TIMES){
						if(System.currentTimeMillis() - data.getLastNotifyTime() >= NOTIFY_SPACE_TIME){
							myNotify(context);
							updateDate(context,data);
						}
					}
				}
			}
	    }
    };
	
	private void myNotify(Context context){
		StorageNotification.notify(context,
        		CacheManageActivity.class,
				false,
				R.drawable.ic_launcher,
				context.getString(R.string.storage_low),
				context.getString(R.string.storage_notify_msg));
	}
	
	/**
	 * 更新数据
	 * @param context
	 * @param data
	 */
	private void updateDate(Context context,StorageLowNotifyData data){
		data.setIsAlreadyLow(true);
		data.setAlreadyNotifyTimes(data.getAlreadyNotifyTimes()+1);
		data.setLastNotifyTime(System.currentTimeMillis());
		MySharedPref.saveStorageLowNotifyData(context, data);
	}
	
	/**
	 * 重置数据
	 * @param context
	 */
	private void resetDate(final Context context){
		StorageLowNotifyData data = new StorageLowNotifyData();
		data.setIsAlreadyLow(false);
		data.setAlreadyNotifyTimes(0);
		data.setLastNotifyTime(0);
		MySharedPref.saveStorageLowNotifyData(context, data);
	}

}

   
