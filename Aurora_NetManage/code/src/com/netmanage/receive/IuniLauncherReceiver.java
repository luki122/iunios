package com.netmanage.receive;

import com.netmanage.utils.ApkUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class IuniLauncherReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		  Log.i(IuniLauncherReceiver.class.getName(),"onReceive");
		  ApkUtils.sendBroasdcastToLauncher(context);
	}	
}

   
