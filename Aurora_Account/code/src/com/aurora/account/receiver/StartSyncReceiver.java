package com.aurora.account.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.aurora.account.service.ExtraFileUpService;
import com.aurora.account.service.StartSyncService;
import com.aurora.account.util.AccountPreferencesUtil;
import com.aurora.account.util.BooleanPreferencesUtil;
import com.aurora.account.util.Globals;
import com.aurora.account.util.SystemUtils;

public class StartSyncReceiver extends BroadcastReceiver {
    private static final String TAG = "StartSyncReceiver";
    
    public static final String ACTION = "com.aurora.account.START_SYNC";
    public static final String PARAMS_PACKAGE_NAME = "packageName";
    public static final String PARAMS_SIZE = "size";
    public static final String PARAMS_RESUME_SYNC = "resumeSync"; // 这个参数传true表示暂停之后恢复同步
    
	@Override
	public void onReceive(Context context, Intent intent) {
		String action= intent.getAction();
		boolean resumeSync = intent.getBooleanExtra(PARAMS_RESUME_SYNC, false);
		String packageName = intent.getStringExtra(PARAMS_PACKAGE_NAME);
		Log.d(TAG, "Jim, action: " + action + ", resumeSync: " + resumeSync +
		        ", packageName: " + packageName);
		
		if (action.equals(ACTION)) {
		    if (!BooleanPreferencesUtil.getInstance(context).hasLogin()) {
		        Log.e(TAG, "Jim, Not login, ignore sync command.");
		        return;
		    }
		    
		    if (!SystemUtils.isNetworkConnected()) {
		        Log.e(TAG, "Jim, Network is not available, ignore sync command.");
		        return;
		    }
		    		    
		    if (AccountPreferencesUtil.getInstance(context).isWifiSyncOnly() &&
		            SystemUtils.getConnectingType() != Globals.NETWORK_WIFI) {
		        Log.e(TAG, "Jim, Network is not wifi but wifi sync only, ignore sync command.");
                return;
		    }
		    
		    if (ExtraFileUpService.isSyncing()) {
		        // 正在同步的时候，不处理新的同步请求，也不要排队，不然界面上会出现同步完了之后，立马又自动启动同步，很奇怪
		        Log.e(TAG, "Jim, currently is syncing, ignore new sync command.");
                return;
		    }
		    
		    if (resumeSync && !ExtraFileUpService.isPaused()) {
                // 恢复暂停的同步
                Log.e(TAG, "Jim, request to resume sync but the sync is not paused, ignore sync command.");
                return;
            } else if (!resumeSync && ExtraFileUpService.isPaused()) {
                // 同步当前处于暂停状态，而且不是要恢复同步，不处理新的同步请求
                Log.e(TAG, "Jim, last sync is paused, ignore sync command.");
                return;
            }
		    
		    if (!TextUtils.isEmpty(packageName)) {
		        // 同步指定模块
		        if (!SystemUtils.isPackageNameValid(packageName)) {
		            Log.e(TAG, "Jim, " + packageName + " is invalid, ignore sync command.");
                    return;
		        }
		        SharedPreferences sts = context.getSharedPreferences(Globals.SHARED_MODULE_SYNC,
		                Context.MODE_PRIVATE);
		        boolean syncEnabled = sts.getBoolean(packageName, true);
		        if (!syncEnabled) {
		            Log.e(TAG, "Jim, sync for " + packageName + " is turn off, ignore sync command.");
		            if(packageName.equals("com.android.contacts"))
		               {
		            	 int size = intent.getIntExtra(PARAMS_SIZE, 0);
		 
		            	 if(size >= 10)
		            	 {
		 					WarnNotification.sendContactsNotify(context);
		            	 }
		               }
	               return;
		        }
		    }
		    
		    startSync(context, packageName, resumeSync);
//	        setNextAutoBackup(context);
		}
	}
	
	private static void startSync(Context context, String packageName, boolean resumeSync) {
	    Intent startSyncIntent = new Intent(context, StartSyncService.class);
        startSyncIntent.putExtra(StartSyncService.EXTRA_MODULE, packageName);
        if (resumeSync) {
            startSyncIntent.putExtra(StartSyncService.EXTRA_COMMAND, StartSyncService.COMMAND_RESUME_SYNC);
        } else {
            startSyncIntent.putExtra(StartSyncService.EXTRA_COMMAND, StartSyncService.COMMAND_START_SYNC);
        }
        context.startService(startSyncIntent);
	}
	
	/**设置下一次提醒*/
//    private void setNextAutoBackup(Context context) {
//        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//        Calendar c = Calendar.getInstance();
//        int[] times = TimeUtils.getTime(AccountPreferencesUtil.getAutoSyncTime());
//        c.set(Calendar.HOUR_OF_DAY, times[0]);
//        c.set(Calendar.MINUTE, times[1]);
//        c.set(Calendar.SECOND, 00);
//        c.set(Calendar.MILLISECOND, 00);
//        c.add(Calendar.DAY_OF_MONTH, 1);
//        PendingIntent autoBackupIntent=PendingIntent.getBroadcast(context, 0, new Intent(AppSyncReceiver.ACTION), 0);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), autoBackupIntent);
//    }
}