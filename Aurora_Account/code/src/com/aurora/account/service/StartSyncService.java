/**
 * 
 */
package com.aurora.account.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.aurora.account.AccountApp;
import com.aurora.account.receiver.BatteryReceiver;
import com.aurora.account.service.ExtraFileUpService.SyncStatus;
import com.aurora.account.util.CommonUtil;
import com.aurora.account.util.FileLog;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * 处理启动同步的命令
 * 
 * @author JimXia
 * @date 2014年10月23日 上午10:09:41
 */
public class StartSyncService extends Service {
    private static final String TAG = "StartSyncService";
    
    public static final String EXTRA_MODULE = "module";
    public static final String EXTRA_COMMAND = "command";
    
    public static final int COMMAND_UNKNOW = -1;
    public static final int COMMAND_START_SYNC = 1;
    public static final int COMMAND_RESUME_SYNC = 2;
    
    public static final String MODULE_ALL = "all";
    public static final String MODULE_RESUME_SYNC = "resume_sync";
    
    private final Object mLock = new Object();
    
    private final BroadcastReceiver mSyncDoneReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Jim, action: " + action);
            if (ExtraFileUpService.ACTION_SYNC_DONE.equals(action)) {
                if (mPendingTasks.isEmpty()) {
                    // 所有同步请求都已经处理完，结束该服务
                    stopSelf();
                } else {
                    if (mBatteryReceiver.checkBattery()) {
                        continueSync();
                    } else {
                        Log.d(TAG, "ACTION_SYNC_DONE, battery is low, wait...");
                    }
                }
            }
        }
    };
    
    /**
     * 获取电池电量信息
     * @author JimXia
     *
     * @date 2014年10月24日 下午4:10:44
     */
    private final BatteryReceiver mBatteryReceiver = new BatteryReceiver(new BatteryReceiver.OnBatteryChangedCallback() {
        @Override
        public void onBatteryChanged(BatteryReceiver receiver, int batteryScale, int batteryLevel) {
            if (!mPendingTasks.isEmpty()) {
                if (receiver.checkBattery()) {
                    continueSync();
                } else {
                    Log.d(TAG, "ACTION_BATTERY_CHANGED, battery is low, wait...");
                }
            } else {
                stopSelf();
            }
        }
    });
    
    private final LinkedHashMap<String, Runnable> mPendingTasks = new LinkedHashMap<String, Runnable>();
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Jim, onCreate");
        registerBatteryReceiver();
        registerSyncDoneReceiver();
    }
    
    private void registerBatteryReceiver() {
        mBatteryReceiver.registerReceiver(this);
    }
    
    private void unregisterBatteryReceiver() {
        mBatteryReceiver.unregisterReceiver(this);
    }
    
    private void registerSyncDoneReceiver() {
        IntentFilter intentFilter = new IntentFilter(ExtraFileUpService.ACTION_SYNC_DONE);
        registerReceiver(mSyncDoneReceiver, intentFilter);
    }
    
    private void unregisterSyncDoneReceiver() {
        unregisterReceiver(mSyncDoneReceiver);
    }
    
    private class StartSyncTask implements Runnable {
        private String mModule;
        private int mCommand;
        
        public StartSyncTask(int command, String module) {
            mModule = module;
            mCommand = command;
        }
        
        @Override
        public void run() {
            handleCommand(mCommand, mModule);
        }
    }
    
    private void handleCommand(int command, String module) {
        Log.d(TAG, "Jim, onStartCommand, module: " + module + ", command: " + command);
        if (command == COMMAND_START_SYNC) {
            if(ExtraFileUpService.canSyncNow() && mBatteryReceiver.checkBattery()) {
                ExtraFileUpService.setSyncStatus(SyncStatus.SYNC_STATUS_SYNCING);
                
                // 显示通知栏同步动画
        		CommonUtil.showSyncNotification(this);
        		FileLog.i(TAG, "  call showSyncNotification in handleCommand()");
                
                ModuleDataWorker worker = new ModuleDataWorker(
                        AccountApp.getInstance() , 0,
                        (TextUtils.isEmpty(module) || MODULE_ALL.equals(module)) ? "": module);
                new Thread(worker).start();
            } else {
                // 同步正在进行或者电量不足
                synchronized (mLock) {
                    if (module.equals(MODULE_ALL)) {
                        if (!mPendingTasks.isEmpty()) {
                            mPendingTasks.clear();
                        }
                        mPendingTasks.put(module, new StartSyncTask(command, module));
                    } else {
                        if (!mPendingTasks.containsKey(module)) {
                            mPendingTasks.put(module, new StartSyncTask(command, module));
                        }
                    }
                }
            }
        } else if (command == COMMAND_RESUME_SYNC) {
            if (mBatteryReceiver.checkBattery()) {
                ExtraFileUpService.continueOperation(this);
            } else {
                CommonUtil.setResumeSyncAlarm();
                stopSelf();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        
        final int command = intent.getIntExtra(EXTRA_COMMAND, COMMAND_UNKNOW);
        String module = intent.getStringExtra(EXTRA_MODULE);
        if (TextUtils.isEmpty(module)) {
            if (command == COMMAND_START_SYNC) {
                module = MODULE_ALL;
            } else if (command == COMMAND_RESUME_SYNC) {
                module = MODULE_RESUME_SYNC;
            } else {
                Log.e(TAG, "Jim, invalid sync request, command: " + command);
            }
        }
        if (mBatteryReceiver.canCheckBattery()) {
            handleCommand(command, module);
        } else {
            if (command == COMMAND_RESUME_SYNC && !mPendingTasks.isEmpty()) {
                // 恢复同步，同步当前处于暂停状态，恢复之前不能处理其他同步请求，如果有其他请求，这里直接清掉
                mPendingTasks.clear();
            }
            mPendingTasks.put(module, new StartSyncTask(command, module));
        }
        
        return super.onStartCommand(intent, flags, startId);
    }
    
    private void continueSync() {
        Iterator<String> keyIterator = mPendingTasks.keySet().iterator();
        if (keyIterator.hasNext()) {
            String nextKey = keyIterator.next();
            Runnable task = mPendingTasks.remove(nextKey);
            task.run();
        } else {
            Log.d(TAG, "Jim, all pending tasks are finished.");
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Jim, onDestroy");
        if (!mPendingTasks.isEmpty()) {
            Log.w(TAG, "Jim, there are still some pending tasks have not been handled, count: " + mPendingTasks.size() +
                    ", battery scale: " + mBatteryReceiver.getBatteryScale() + ", battery level: " + mBatteryReceiver.getBatteryLevel());
        }
        unregisterBatteryReceiver();
        unregisterSyncDoneReceiver();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
