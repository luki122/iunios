package com.android.gallery3d.xcloudalbum.uploaddownload;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.android.gallery3d.util.Globals;

public class XCloudAutoUploadBroadcastReceiver extends BroadcastReceiver{
	public static final String TAG = "XCloudAutoUploadBroadcastReceiver";
	public static final String XCLOUD_AUTO_UPLOAD = "XCLOUD_AUTO_UPLOAD";
	public static final String XCLOUD_AUTO_UPLOAD_FROM_BROADCAST = "XCLOUD_AUTO_UPLOAD_FROM_BROADCAST";
	public static final String XCLOUD_AUTO_UPLOAD_ACTION = "com.aurora.gallery.upload"; 
	public static final String XCLOUD_AUTO_UPLOAD_NOTIFY_USER_CENTER_ACTION = "com.aurora.gallery.notify.time";
	public static final String XCLOUD_AUTO_UPLOAD_NOTIFY_USER_CENTER_KEY_TIME = "time";
	//key: time   value:2015-5-27 14:30:22
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(Globals.OVERSEA_VERSION) return;//paul add
		String action = intent.getAction();
		if(action.equals(XCLOUD_AUTO_UPLOAD_ACTION)) {
			Log.i(TAG, "XCLOUD_AUTO_UPLOAD_ACTION onReceive");
			Intent serviceIntent = new Intent(context, XCloudAutoUploadService.class);
			serviceIntent.setAction(XCLOUD_AUTO_UPLOAD);
			serviceIntent.putExtra(XCLOUD_AUTO_UPLOAD_FROM_BROADCAST, true);
			context.startService(serviceIntent);
		}
	}
	
	public static void sendNotifyUserCenter(Context context) {
		
		Intent intent = new Intent(XCLOUD_AUTO_UPLOAD_NOTIFY_USER_CENTER_ACTION);
		long time = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(time); 
		String str = sdf.format(curDate);
		intent.putExtra(XCLOUD_AUTO_UPLOAD_NOTIFY_USER_CENTER_KEY_TIME, str);
		context.sendBroadcast(intent);
		//Log.i(TAG, "XCLOUD_AUTO_UPLOAD_ACTION sendNotifyUserCenter ------date: " + str);
	}

}
