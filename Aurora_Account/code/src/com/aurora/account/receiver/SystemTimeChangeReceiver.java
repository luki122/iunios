package com.aurora.account.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aurora.account.util.CommonUtil;

/**
* @ClassName: SystemTimeChangeReceiver
* @Description: TODO 系统时间改变的Receiver
*
 */
public class SystemTimeChangeReceiver extends BroadcastReceiver {
	
	private static final String TAG = "SystemTimeChangeReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.i(TAG, "onReceive()");
		
		if (intent != null && intent.getAction().equals("android.intent.action.TIME_SET")) {
			Log.i(TAG, "receive action: android.intent.action.TIME_SET");
			
			CommonUtil.checkAndSetAppBackupAlarm();
		}
		
	}

}
