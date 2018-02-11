package com.privacymanage.receive;

import com.privacymanage.model.AccountModel;
import com.privacymanage.service.WatchDogService;
import com.privacymanage.utils.LogUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EnterPrivacySpaceReceiver extends BroadcastReceiver {
	private final long MAX_TIME_INTERVAL = 10*1000;
	private final String TAG = EnterPrivacySpaceReceiver.class.getName();
	
	@Override
	public void onReceive(final Context context, Intent intent) {
		context.startService(new Intent(context,WatchDogService.class));
		if(intent == null){
			LogUtils.printWithLogCat(TAG,"EnterPrivacySpaceReceiver is Invalid for intent is null");
			return ;
		}
		
		long curTime = System.currentTimeMillis();
		long timeOfSendReceive = intent.getLongExtra("time", 0);
		if(curTime - timeOfSendReceive > MAX_TIME_INTERVAL){
			LogUtils.printWithLogCat(TAG,"EnterPrivacySpaceReceiver is Invalid for time out");
			return ;
		}
		String password = intent.getStringExtra("password");
		if(password != null){
			LogUtils.printWithLogCat(TAG,"EnterPrivacySpaceReceiver is valid");
			AccountModel.getInstance().enterPrivacyAccount(password,true);
		}else{
			LogUtils.printWithLogCat(TAG,"EnterPrivacySpaceReceiver is Invalid for password is null");
		}
	}	
}

   
