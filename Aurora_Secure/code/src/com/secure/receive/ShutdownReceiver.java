package com.secure.receive;

import com.secure.model.AuroraPrivacyManageModel;
import com.secure.model.AutoStartModel;
import com.secure.utils.LogUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShutdownReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) { 
		LogUtils.printWithLogCat(ShutdownReceiver.class.getName(), "关机中...");
		AutoStartModel.getInstance(context).shutDown();
		
		AuroraPrivacyManageModel instanceOfPrivacy = AuroraPrivacyManageModel.getInstance();
		if(instanceOfPrivacy != null){
			instanceOfPrivacy.shutDown();
		}
	}	
}

   
