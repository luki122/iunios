package com.aurora.account.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.aurora.account.util.BooleanPreferencesUtil;
import com.aurora.account.util.Globals;
import com.aurora.account.util.Log;

public class AutoBackupAlarmReceiver extends BroadcastReceiver {
	
	private static final String TAG = "AutoBackupAlarmReceiver";
	
	public static final String PHOTO_ACTION = "com.aurora.account.PHOTOBACKUPALARM";
	// 其它，现在没有，先省略
	public static final String OTHER_ACTION = "com.aurora.account.OTHER";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.i(TAG, "receive AutoBackupAlarmReceiver");
		
		if (!BooleanPreferencesUtil.getInstance(context).hasLogin()) {
	        Log.e(TAG, "Not login, ignore autobackup command.");
	        return;
	    }
		
		if (intent != null && !TextUtils.isEmpty(intent.getAction()) && intent.getAction().equals(PHOTO_ACTION)) {
			
			Log.i(TAG, "sendBroadcast Globals.ACTION_PHOTO_DO_SYNC: " + Globals.ACTION_PHOTO_DO_SYNC);
			
			Intent i = new Intent(Globals.ACTION_PHOTO_DO_SYNC);
			context.sendBroadcast(i);
			
		} else if (intent != null && !TextUtils.isEmpty(intent.getAction()) && intent.getAction().equals(OTHER_ACTION)) {
			// 相关otherApp操作
		}
		
	}

}
