package com.aurora.puremanager.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.aurora.puremanager.model.PersistentModel;
import com.aurora.puremanager.service.AutoSleepService;
import com.aurora.puremanager.service.WatchDogService;
import com.aurora.puremanager.traffic.NetworkControlUtil;
import com.aurora.puremanager.utils.LogUtils;

public class BootReceiver extends BroadcastReceiver {
    public static long startSysTimeMillis = 0;

    @Override
    public void onReceive(final Context context, Intent intent) {
        LogUtils.printWithLogCat(BootReceiver.class.getName(), "onReceive");

        PersistentModel.getInstance().systemBoot();
        Intent intentOfService = new Intent(context, WatchDogService.class);
        context.startService(intentOfService);
        Intent sleepServiceIntent = new Intent(context, AutoSleepService.class);
        context.startService(sleepServiceIntent);
        startSysTimeMillis = System.currentTimeMillis();
        
        // 开机成功后重新设置联网权限 BUG #19445（未找到具体原因，先这样解决问题）
        new NetworkControlUtil().resetAppControl(context);
    }
}

   
