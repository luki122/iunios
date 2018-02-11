package com.aurora.ota.reporter.appnumber;

import gn.com.android.update.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import android.graphics.drawable.Drawable;


public class PakageInfoProvider {
	private static final String TAG = "PakageInfoProvider";
	private Context context;
	private List<AppInfo> appInfos;
	private AppInfo appInfo;

	public PakageInfoProvider(Context context) {
		super();
		this.context = context;
	}

	public List<AppInfo> getAppInfo() {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> pakageinfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		appInfos = new ArrayList<AppInfo>();
		for (PackageInfo packageInfo : pakageinfos) {
			appInfo = new AppInfo();
			
			// 获取应用程序的名称，不是包名，而是清单文件中的label name
			String str_name = packageInfo.applicationInfo.loadLabel(pm).toString();
			appInfo.setAppName(str_name);

			// 获取应用程序的版本号码
			String version = packageInfo.versionName;
			appInfo.setAppVersion(version);

			// 获取应用程序的快捷方式图标
			Drawable drawable = packageInfo.applicationInfo.loadIcon(pm);
			appInfo.setDrawable(drawable);

			// 获取应用程序是否是第三方应用程序
			appInfo.setIsUserApp(filterApp(packageInfo.applicationInfo));
			
			//给一同程序设置包名
			appInfo.setPackageName(packageInfo.packageName);

			LogUtils.logv(TAG, "Version : " + version + "App_Name : "+ str_name);
			appInfos.add(appInfo);
			appInfo = null;
		}

		return appInfos;
	}

	/**
	 * 三方应用程序的过滤器
	 * 
	 * @param info
	 * @return true 三方应用 false 系统应用
	 */
	public boolean filterApp(ApplicationInfo info) {
		if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
			//本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了
			return true;
		} else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
			// 代表的用户的应用
			return true;
		}
		return false;
	}

}