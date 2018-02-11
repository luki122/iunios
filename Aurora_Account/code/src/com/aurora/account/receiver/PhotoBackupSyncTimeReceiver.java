package com.aurora.account.receiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.aurora.account.AccountApp;
import com.aurora.account.util.AccountPreferencesUtil;
import com.aurora.account.util.Globals;
import com.aurora.account.util.Log;
import com.aurora.account.util.SystemUtils;

public class PhotoBackupSyncTimeReceiver extends BroadcastReceiver {
	
	public static final String TAG = "PhotoBackupSyncTimeReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.i(TAG, "get PhotoBackupSyncTime");
		
//		key: time   value:2015-5-27 14:30:22
		String time = intent.getStringExtra(Globals.GALLERY_SYNC_TIME_KEY);
		
		Log.i(TAG, "time: " + time);
		Log.i(TAG, "timeLong: " + getLongTime(time));
		
		SystemUtils.updateAppSyncTime(context, Globals.GALLERY_PACKAGE_NAME, getLongTime(time));
		
		// 保存最后更新时间
		AccountPreferencesUtil pref = AccountPreferencesUtil
				.getInstance(AccountApp.getInstance());
		pref.setLastSyncFinishedTime(getLongTime(time));
	}
	
	private long getLongTime(String time) {
		long t = 0l;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date d = dateFormat.parse(time);
			t = d.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return t;
	}

}
