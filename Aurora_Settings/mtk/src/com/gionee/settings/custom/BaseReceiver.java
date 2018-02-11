package com.gionee.settings.custom;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.internal.statusbar.IStatusBarService;
import android.os.ServiceManager;

public class BaseReceiver extends BroadcastReceiver {
	protected final String INFO = "info";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

	}
	
	protected boolean checkFrontActivity(Context context) {
		// TODO Auto-generated method stub
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTasks = am.getRunningTasks(1);
		if (runningTasks.size() > 0) {
			String className = runningTasks.get(0).topActivity.getClassName();
			if ("com.android.settings.SubSettings".equals(className)
					|| "com.android.settings.wifi.WifiSettings"
							.equals(className)
					|| "com.android.settings.wifi.WifiPickerActivity"
							.equals(className)
					|| "com.android.phone.InCallScreen".equals(className)
					|| className.contains("gn.com.android.mmitest")
					|| className.contains("com.gionee.autommi")) {
				return false;
			}
		}
		return true;
	}

	protected void collapseStatusBar() {
		// TODO Auto-generated method stub collapsePanels()
		IStatusBarService statusBarService = IStatusBarService.Stub
				.asInterface(ServiceManager
						.getService(Context.STATUS_BAR_SERVICE));
		try {
			statusBarService.collapsePanels();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
