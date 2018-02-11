package com.aurora.setupwizard.service;

import java.io.File;
import java.util.List;

import com.aurora.setupwizard.utils.ApkUtil;
import com.aurora.setupwizard.utils.Constants;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

public class InstallService extends IntentService {

	private int mCount = 0;
	private int mCurrent = 0;

	public InstallService() {
		super("");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent == null) {
			return;
		}
		String packageName = intent.getStringExtra("packageName");
		String apkPath = intent.getStringExtra("apkPath");
		mCount = intent.getIntExtra("count", 0);
		if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(apkPath)) {
			return;
		}
		File apk = new File(apkPath);
		if (!apk.exists()) {
			return;
		}
		ApkUtil.intstallApp(this, packageName, apk,
				new PackageInstallObserver());
	}

	class PackageInstallObserver extends IPackageInstallObserver.Stub {
		public void packageInstalled(String packageName, int returnCode) {
			mCurrent++;
			if (mCount == mCurrent) {
				File file = new File(Constants.APK_FOLDER);
				if (file != null)
					Log.v("lmjssjj", "delete file");
				ApkUtil.deleteFile(file);
				if (isBackground(getApplicationContext())) {
					Log.v("lmjssjj", "forceStopPackage");
					ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
					am.forceStopPackage(getPackageName());
				} else {
					Intent intent = new Intent(
							"com.aurora.setupwizard.azcomplete");
					sendBroadcast(intent);
				}
			}
		}
	}

	public static boolean isBackground(Context context) {

		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(context.getPackageName())) {
				if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
					Log.i("后台", appProcess.processName);
					return true;
				} else {
					Log.i("前台", appProcess.processName);
					return false;
				}
			}
		}
		return false;
	}

}
