package com.aurora.puremanager.receive;

import com.aurora.puremanager.utils.StorageUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * 监听SD卡插拔事件
 */
public class SDcardPlugExtractReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		StorageUtil.getInstance(context).initOrUpdateThread();		
	}
}

   
