package com.aurora.iunivoice.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.aurora.iunivoice.activity.MineSettingActivity;
import com.aurora.iunivoice.service.SystemPushMsgService;
import com.aurora.iunivoice.service.SystemTipService;
import com.aurora.iunivoice.utils.Util;

public class ServiceHelpBroadCast extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (!SystemTipService.isServiceAlive) {
			context.startService(Util.createExplicitFromImplicitIntent(context, new Intent(SystemTipService.SYSTEM_TIP_SERVICE_ACTION)));
		}
		
		if(isOpenSystemPushMsg(context))
		{
		      context.startService(Util.createExplicitFromImplicitIntent(context, new Intent(SystemPushMsgService.SYSTEM_PUSHMSG_SERVICE_ACTION)));
		}
	}

	private boolean isOpenSystemPushMsg(Context context){
		SharedPreferences sp = context.getSharedPreferences(MineSettingActivity.IUNIVOICE_SETTING_NAME, Context.MODE_PRIVATE);
		return sp.getBoolean(MineSettingActivity.IS_SYSTEM_MSG_PUSH_KEY, true);
	}
	
}
