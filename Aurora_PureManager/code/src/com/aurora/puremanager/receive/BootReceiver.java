package com.aurora.puremanager.receive;

import com.aurora.puremanager.model.PersistentModel;
import com.aurora.puremanager.service.WatchDogService;
import com.aurora.puremanager.utils.LogUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    public static long startSysTimeMillis = 0;
	@Override
	public void onReceive(final Context context, Intent intent) {
		LogUtils.printWithLogCat(BootReceiver.class.getName(), "onReceive");
		PersistentModel.getInstance().systemBoot();
		Intent intentOfService = new Intent(context,WatchDogService.class);
		context.startService(intentOfService);
		startSysTimeMillis = System.currentTimeMillis();
	}	
}

   
