package com.aurora.account.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.aurora.account.service.BackgroundWorkService;
import com.aurora.account.service.ExtraFileUpService;
import com.aurora.account.util.AccountPreferencesUtil;
import com.aurora.account.util.SystemUtils;
import com.aurora.account.util.TimeUtils;

public class NetWorkReceiver extends BroadcastReceiver {
    private static final String TAG = "NetWorkReceiver";
    
    /**没网*/
    private static final int NO_NETWORK = 0;
    /**WIFI*/
    private static final int NETWORK_WIFI = 1;
    /**移动网络*/
    private static final int NETWORK_MOBILE = 2;
    
//    private static final long _DAY = 24 * 3600 * 1000;
//    private static final int MAX_DAYS = 5;

	@Override
	public void onReceive(Context context, Intent intent) {
		int currentNetStatus = SystemUtils.getCurrentNetStatus();
		Log.i(TAG, "Jim, the wifi status changer currentNetStatus: "+currentNetStatus);
		switch (currentNetStatus) {
		    case NO_NETWORK: // 没网
		        Log.d(TAG, "Jim, the network is disconnected.");
		        // 网络断开了，如果当前正在同步则暂停同步
		       /* if (ExtraFileUpService.isSyncing()) {
                    ExtraFileUpService.pauseOperation(context);
                }*/
		        break;
		    case NETWORK_WIFI: // WIFI
		        performPendingTask(context);
		        if (!doSyncIfNeeded(context)) {
		            Log.d(TAG, "Jim, current network is wifi, no need to sync");
		        }
		        break;
		    case NETWORK_MOBILE: // 移动网络
		        performPendingTask(context);
		        if (AccountPreferencesUtil.getInstance(context).isWifiSyncOnly()) {
		            // 只在wifi下同步
		            if (ExtraFileUpService.isSyncing()) {
		                // 切换到移动网络了，需要暂停当前同步
	                    ExtraFileUpService.pauseOperation(context,0);
		            }
		        } else {
		            if (!doSyncIfNeeded(context)) {
		                Log.d(TAG, "Jim, current network is mobile, no need to sync");
		            }
		        }
		        break;
		}
	}
	
	/**
	 * 执行上次没有执行完的依赖网络的任务
	 * @param context
	 */
	private void performPendingTask(Context context) {
	    AccountPreferencesUtil pref = AccountPreferencesUtil.getInstance(context);
	    if (!TextUtils.isEmpty(pref.getPendingUserId()) &&
	            !TextUtils.isEmpty(pref.getPendingUserKey())) {
	        Intent backgroundTask = new Intent(context, BackgroundWorkService.class);
	        backgroundTask.putExtra(BackgroundWorkService.EXTRA_COMMAND, BackgroundWorkService.COMMAND_PERFORM_LOGOUT);
	        context.startService(backgroundTask);
	    }
	}
	
	private boolean doSyncIfNeeded(Context context) {
	    if (ExtraFileUpService.isPaused()) {
	        ExtraFileUpService.continueOperation(context);
	        Log.d(TAG, "Jim, sync status: paused -> resumed");
	        return true;
	    } else if (ExtraFileUpService.canSyncNow()) {
            long lastSyncDate = AccountPreferencesUtil.getInstance(context).getSyncDate();
            long now = TimeUtils.getSyncDate();
            Log.d(TAG, "Jim, lastSyncDate: " + lastSyncDate + ", now: " + now);
            if ((now - lastSyncDate) != 0 /*>= MAX_DAYS * _DAY*/) {
                // 超过指定时间没有同步，启动同步
                Intent startSync = new Intent(StartSyncReceiver.ACTION);
                context.sendBroadcast(startSync);
                Log.d(TAG, "Jim, send broadcast " + StartSyncReceiver.ACTION);
                return true;
            } else {
                Log.d(TAG, "Jim, sync one time every day.");
            }
        } else if (ExtraFileUpService.isSyncing()) {
            Log.d(TAG, "Jim, sync is ongoing.");
            return true;
        }
	    
	    return false;
	}
}
