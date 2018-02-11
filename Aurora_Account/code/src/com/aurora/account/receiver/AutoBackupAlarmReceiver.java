package com.aurora.account.receiver;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.aurora.account.activity.LoginActivity;
import com.aurora.account.bean.AppConfigInfo;
import com.aurora.account.util.BooleanPreferencesUtil;
import com.aurora.account.util.CommonUtil;
import com.aurora.account.util.Globals;
import com.aurora.account.util.Log;
import com.aurora.account.util.SystemUtils;

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
		
			if (CommonUtil.checkPhotoBackupHasBind(context)) {	// 是否已绑定
				Intent i = new Intent(Globals.ACTION_PHOTO_DO_SYNC);
				context.sendBroadcast(i);
			} else {
				List<AppConfigInfo> list = SystemUtils.getAppConfigInfo(context);
            	for (AppConfigInfo info : list) {
            		if (info.getApp_packagename().equals(Globals.GALLERY_PACKAGE_NAME)) {
            			if (info.isSync()) {
            				SystemUtils.updateAppConfigInfo(context,
            						info.getApp_packagename(), false);
            				CommonUtil.checkAndSetAppBackupAlarm();
            			}
            			break;
            		}
            	}
			}
			
		} else if (intent != null && !TextUtils.isEmpty(intent.getAction()) && intent.getAction().equals(OTHER_ACTION)) {
			// 相关otherApp操作
		}
		
	}

}
