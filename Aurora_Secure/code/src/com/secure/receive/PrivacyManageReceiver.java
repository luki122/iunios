package com.secure.receive;

import com.secure.model.AuroraPrivacyManageModel;
import com.secure.utils.LogUtils;
import com.privacymanage.data.AidlAccountData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PrivacyManageReceiver extends BroadcastReceiver{
	private final String TAG = PrivacyManageReceiver.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) { 
		LogUtils.printWithLogCat(TAG, "action:"+intent.getAction());
		AidlAccountData accountData = (AidlAccountData)(intent.getParcelableExtra("account"));
		if("com.aurora.privacymanage.SWITCH_ACCOUNT".equals(intent.getAction())){
			AuroraPrivacyManageModel.getInstance(context).switchAccount(accountData);
		}else if("com.aurora.privacymanage.DELETE_ACCOUNT".equals(intent.getAction())){
			boolean delete = intent.getBooleanExtra("delete", false);
			LogUtils.printWithLogCat(TAG, "delete:"+delete);
			AuroraPrivacyManageModel.getInstance(context).deleteAccount(accountData, delete);
		}
	}	
}

   
