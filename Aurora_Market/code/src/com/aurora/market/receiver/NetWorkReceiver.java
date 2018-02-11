package com.aurora.market.receiver;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.aurora.market.activity.setting.UpdateSettingsPreferenceActivity;
import com.aurora.market.install.InstallNotification;
import com.aurora.market.model.DownloadData;
import com.aurora.market.service.AppDownloadService;
import com.aurora.market.service.AutoUpdateService;
import com.aurora.market.util.DataFromUtils;
import com.aurora.market.util.Globals;
import com.aurora.market.util.SystemUtils;
import com.aurora.market.util.TimeUtils;

public class NetWorkReceiver extends BroadcastReceiver {
	private SharedPreferences sp;

	public boolean isForeground(String PackageName, Context context) {
		// Get the Activity Manager
		ActivityManager manager = (ActivityManager) context
				.getSystemService(context.ACTIVITY_SERVICE);

		// Get a list of running tasks, we are only interested in the last one,
		// the top most so we give a 1 as parameter so we only get the topmost.
		List<ActivityManager.RunningTaskInfo> task = manager.getRunningTasks(1);

		// Get the info we need for comparison.
		ComponentName componentInfo = task.get(0).topActivity;

		// Check if it matches our package name.
		if (componentInfo.getPackageName().equals(PackageName))
			return true;

		// If not then our app is not on the foreground.
		return false;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		int isCurrentNetStatus = SystemUtils.isNetStatus(context);
		Log.i("NetWorkReceiver", "the wifi status changer isCurrentNetStatus="+isCurrentNetStatus);
		sp = context.getSharedPreferences(Globals.SHARED_WIFI_UPDATE,
				context.MODE_APPEND);
		SharedPreferences perf = PreferenceManager
				.getDefaultSharedPreferences(context);
		final Editor ed = sp.edit();
		int netstatus = sp.getInt(Globals.SHARED_NETSTATUS_KEY_ISEXITS, 0);
	/*	boolean isfirst = sp.getBoolean(Globals.SHARED_MOBILE_DISCONNECT_COUNT,
				false);*/

		 // .isNetworkConnected(context);//
		if(netstatus ==isCurrentNetStatus )
		{
			return;
		}
	

		if ((netstatus == 1) && (isCurrentNetStatus ==2)) {
		/*	ed.putBoolean(Globals.SHARED_WIFI_DISCONNECT_ISEXITS, true);
			ed.putBoolean(Globals.SHARED_MOBILE_DISCONNECT_COUNT, true);
			ed.commit();*/

			Intent networkChange = new Intent(context, AppDownloadService.class);
			networkChange.putExtra(AppDownloadService.DOWNLOAD_OPERATION,
					AppDownloadService.OPERATION_NETWORK_MOBILE_PAUSE);
			context.startService(networkChange);
		/*	if (isForeground("com.aurora.market", context)) {
				Intent finish = new Intent(
						Globals.BROADCAST_ACTION_DOWNLOAD_PAUSE);
				context.sendBroadcast(finish);
			}*/

		} 
		else if((netstatus ==2) && (isCurrentNetStatus ==1))
		{
			Intent networkChange = new Intent(context, AppDownloadService.class);
			networkChange.putExtra(AppDownloadService.DOWNLOAD_OPERATION,
					AppDownloadService.OPERATION_NETWORK_MOBILE_CONTINUE);
			context.startService(networkChange);
		}
		else if((netstatus ==0) && (isCurrentNetStatus ==2))
		{
			Intent networkChange = new Intent(context, AppDownloadService.class);
			networkChange.putExtra(AppDownloadService.DOWNLOAD_OPERATION,
					AppDownloadService.OPERATION_NETWORK_MOBILE_PAUSE);
			context.startService(networkChange);
		}
		
		/*else if ((iswifi) && (!isMobile)) {
			boolean isDownload = sp.getBoolean(
					Globals.SHARED_MOBILE_STATUS_ISDOWNLOAD, false);
			if (isDownload) {
				Intent networkChange = new Intent(context,
						AppDownloadService.class);
				networkChange.putExtra(AppDownloadService.DOWNLOAD_OPERATION,
						AppDownloadService.OPERATION_NETWORK_MOBILE_CONTINUE);
				context.startService(networkChange);
			} else {
				Intent networkChange = new Intent(context,
						AppDownloadService.class);
				networkChange.putExtra(AppDownloadService.DOWNLOAD_OPERATION,
						AppDownloadService.OPERATION_NETWORK_MOBILE_PAUSE);
				context.startService(networkChange);
			}

		} */else {
			Intent networkChange = new Intent(context, AppDownloadService.class);
			networkChange.putExtra(AppDownloadService.DOWNLOAD_OPERATION,
					AppDownloadService.OPERATION_NETWORK_CHANGE);
			context.startService(networkChange);
		}
		ed.putInt(Globals.SHARED_NETSTATUS_KEY_ISEXITS, isCurrentNetStatus);
		ed.commit();
		if (SystemUtils.isWifiNetwork(context)) {
			

			final boolean bl = perf.getBoolean("software_auto_update_tip_key", false);
			final boolean bl2 = UpdateSettingsPreferenceActivity.getPreferenceValue(context, 
					UpdateSettingsPreferenceActivity.WIFI_AUTO_UPGRADE_KEY);
			
			if (!bl && !bl2)
				return;
			// 获取提示升级时间
			String time = sp.getString(
					Globals.SHARED_WIFI_APPUPDATE_KEY_UPDATETIME, "0");
			if (!time.equals(TimeUtils.getStringDateShort())) {
				final Context m_context = context;
				new Thread() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						DataFromUtils up_data = new DataFromUtils();
						ArrayList<DownloadData> down_data = up_data
								.getUpdateData(m_context);

						if ((null != down_data) && (down_data.size() > 0)) {
							if (bl) {
								InstallNotification.sendUpdateNotify(down_data);
							}
							if (bl2) {
								AutoUpdateService.setDownloadList(down_data);
							}
							
							ed.putString(
									Globals.SHARED_WIFI_APPUPDATE_KEY_UPDATETIME,
									TimeUtils.getStringDateShort());
							ed.commit();
						}

					}

				}.start();
			}
		}
	}

}
