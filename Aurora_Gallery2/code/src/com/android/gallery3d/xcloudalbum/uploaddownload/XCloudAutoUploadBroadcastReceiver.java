package com.android.gallery3d.xcloudalbum.uploaddownload;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.gallery3d.util.Globals;

public class XCloudAutoUploadBroadcastReceiver extends BroadcastReceiver {
	public static final String TAG = "XCloudAutoUploadBroadcastReceiver";
	public static final String XCLOUD_AUTO_UPLOAD = "XCLOUD_AUTO_UPLOAD";
	public static final String XCLOUD_AUTO_UPLOAD_FROM_BROADCAST = "XCLOUD_AUTO_UPLOAD_FROM_BROADCAST";
	public static final String XCLOUD_AUTO_UPLOAD_ACTION = "com.aurora.gallery.upload";
	public static final String XCLOUD_AUTO_UPLOAD_NOTIFY_USER_CENTER_ACTION = "com.aurora.gallery.notify.time";
	public static final String XCLOUD_AUTO_UPLOAD_NOTIFY_USER_CENTER_KEY_TIME = "time";

	//wenyongzhe
	public static boolean wifiAndCharging = false;
	
	// key: time value:2015-5-27 14:30:22

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (Globals.OVERSEA_VERSION)
			return;// paul add
		String action = intent.getAction();
		Log.i(TAG, "XCLOUD_AUTO_UPLOAD_ACTION onReceive"+action);
//wenyongzhe 2016.1.31
		if (action.equals(XCLOUD_AUTO_UPLOAD_ACTION)) {
			Log.i(TAG, "XCLOUD_AUTO_UPLOAD_ACTION onReceive");
			Intent serviceIntent = new Intent(context,
					XCloudAutoUploadService.class);
			serviceIntent.setAction(XCLOUD_AUTO_UPLOAD);
			serviceIntent.putExtra(XCLOUD_AUTO_UPLOAD_FROM_BROADCAST, true);
			context.startService(serviceIntent);
		}
		
		//wenyongzhe 2016.1.31 new_ui
//		if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
//			IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//			Intent batteryStatus = context.registerReceiver(null, ifilter);
//			
//			int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
//			boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||  status == BatteryManager.BATTERY_STATUS_FULL;
//			int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
//			boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
//			boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
//			if (isCharging || usbCharge ||acCharge ) {
//				wifiAndCharging = true;//wenyongzhe2016.2.23 
//				Intent serviceIntent = new Intent(context,
//						XCloudAutoUploadService.class);
//				serviceIntent.setAction(XCLOUD_AUTO_UPLOAD);
//				serviceIntent.putExtra(XCLOUD_AUTO_UPLOAD_FROM_BROADCAST, true);
//				context.startService(serviceIntent);
//			}
//		}
//		if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
//			wifiAndCharging = false;//wenyongzhe2016.2.23 
////			Intent serviceIntent = new Intent(context,XCloudAutoUploadService.class);
////			context.stopService(serviceIntent);
//		}
//		
//		//wenyongzhe 2016.3.4 start
//		if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
//			State wifiState = null;  
//	        State mobileState = null;  
//	        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
//	        wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();  
//	        mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();  
//	        if (wifiState != null && mobileState != null  
//	                && State.CONNECTED != wifiState  
//	                && State.CONNECTED == mobileState) {  
//	            // 手机网络连接成功  
//	        	wifiAndCharging = false;
//	        } else if (wifiState != null && mobileState != null  
//	                && State.CONNECTED != wifiState  
//	                && State.CONNECTED != mobileState) {  
//	            // 手机没有任何的网络  
//	        	wifiAndCharging = false;
//	        } else if (wifiState != null && State.CONNECTED == wifiState) {  
//	            // 无线网络连接成功  
//	        	
//	        	IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//				Intent batteryStatus = context.registerReceiver(null, ifilter);
//				
//				int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
//				boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||  status == BatteryManager.BATTERY_STATUS_FULL;
//				int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
//				boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
//				boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
//				
//				if (isCharging || usbCharge ||acCharge ) {
//					wifiAndCharging = true;//wenyongzhe2016.2.23 
//					Intent serviceIntent = new Intent(context,
//							XCloudAutoUploadService.class);
//					serviceIntent.setAction(XCLOUD_AUTO_UPLOAD);
//					serviceIntent.putExtra(XCLOUD_AUTO_UPLOAD_FROM_BROADCAST, true);
//					context.startService(serviceIntent);
//				}
//	        }  
//		}
		//wenyongzhe 2016.3.4 end
		
	}

	public static void sendNotifyUserCenter(Context context) {

		Intent intent = new Intent(XCLOUD_AUTO_UPLOAD_NOTIFY_USER_CENTER_ACTION);
		long time = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(time);
		String str = sdf.format(curDate);
		intent.putExtra(XCLOUD_AUTO_UPLOAD_NOTIFY_USER_CENTER_KEY_TIME, str);
		context.sendBroadcast(intent);
		// Log.i(TAG,
		// "XCLOUD_AUTO_UPLOAD_ACTION sendNotifyUserCenter ------date: " + str);
	}

}
