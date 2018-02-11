/**
 * 作为后续开发专门放BroadcastReceiver的包
 */
package com.aurora.account.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aurora.account.util.CommonUtil;

/**
 * 监听开机启动广播
 * @author JimXia
 *
 * @date 2014年10月22日 上午10:44:13
 */
public class BootCompletedReceiver extends BroadcastReceiver {
	private static final String TAG = "BootCompletedReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Action:"+intent.getAction());
		CommonUtil.setAutoSyncAlarm();
		CommonUtil.checkAndSetAppBackupAlarm();
	}
}
