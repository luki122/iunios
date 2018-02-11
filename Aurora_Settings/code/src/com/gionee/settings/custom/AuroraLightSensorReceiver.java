package com.gionee.settings.custom;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.internal.statusbar.IStatusBarService;
import com.android.settings.AuroraLightSensorService;

import android.os.ServiceManager;
import android.util.Log;

import android.os.SystemProperties;

public class AuroraLightSensorReceiver extends BroadcastReceiver {
	protected final String INFO = "info";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			Log.e("wolfu", "SystemProperties.set ctl.stop bootanim ");		
        	SystemProperties.set("ctl.stop", "bootanim");
			Intent it = new Intent(intent);
			it.setClass(context,AuroraLightSensorService.class);
			context.startService(it);
		}
		Log.i("qy", "AuroraLightSensorReceiver--->onReceive()");		

	}

}
