package com.aurora.market.receiver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.aurora.market.db.InstalledAppDao;
import com.aurora.market.install.InstallAppManager;
import com.aurora.market.model.InstalledAppInfo;
import com.aurora.market.service.AppDownloadService;
import com.aurora.market.service.AutoUpdateService;
import com.aurora.market.util.Globals;


public class PackageReceive extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		AutoUpdateService.stopAutoUpdate(context);
		AutoUpdateService.startAutoUpdate(context,1);
		// 检查自身有没在列表中，没有的话加入到列表
		InstalledAppDao dao = new InstalledAppDao(context);
		dao.openDatabase();
		if (action.equals("android.intent.action.PACKAGE_ADDED")) {
			String packagename = intent.getDataString();
			packagename = packagename.substring(packagename.indexOf(":") + 1,
					packagename.length());

			PackageManager pm = context.getPackageManager();
			PackageInfo pinfo = null;
			try {
				pinfo = pm.getPackageInfo(packagename, 0);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			if (pinfo != null) {
	
				ApplicationInfo appInfo = pinfo.applicationInfo;
				InstalledAppInfo installedAppInfo = new InstalledAppInfo();
				installedAppInfo.setName(pinfo.applicationInfo.loadLabel(pm)
						.toString());
				installedAppInfo.setIconId(appInfo.icon);
				installedAppInfo.setPackageName(appInfo.packageName);
				installedAppInfo.setVersionCode(pinfo.versionCode);
				installedAppInfo.setVersion(pinfo.versionName);
				installedAppInfo.setApkPath(appInfo.sourceDir);
				setAppFlag(installedAppInfo, appInfo);
				
				if (dao.getInstalledAppInfo(packagename) != null) {
					dao.updateInstalledApp(installedAppInfo);
				} else {
					dao.insert(installedAppInfo);
				}
			}

		} else if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
			//Log.e("liumx", "change action---------------"+action);
			
			String packagename = intent.getDataString();
			//Log.e("liumx", "packagename---------------"+packagename);
			packagename = packagename.substring(packagename.indexOf(":") + 1,
					packagename.length());

			PackageManager pm = context.getPackageManager();
			PackageInfo pinfo = null;
			try {
				pinfo = pm.getPackageInfo(packagename, 0);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			if (pinfo != null) {
				
				ApplicationInfo appInfo = pinfo.applicationInfo;
				InstalledAppInfo installedAppInfo = new InstalledAppInfo();
				installedAppInfo.setName(pinfo.applicationInfo.loadLabel(pm)
						.toString());
				installedAppInfo.setIconId(appInfo.icon);
				installedAppInfo.setPackageName(appInfo.packageName);
				installedAppInfo.setVersionCode(pinfo.versionCode);
				installedAppInfo.setVersion(pinfo.versionName);
				installedAppInfo.setApkPath(appInfo.sourceDir);
				setAppFlag(installedAppInfo, appInfo);
				
				if (dao.getInstalledAppInfo(packagename) != null) {
					dao.updateInstalledApp(installedAppInfo);
				} else {
					dao.insert(installedAppInfo);
				}
		
				context.sendBroadcast(new Intent(Globals.MARKET_UPDATE_ACTION));
			} else {
				dao.deleteInstalledApp(packagename);

				context.sendBroadcast(new Intent(Globals.MARKET_UPDATE_ACTION));
			}

		} else if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
			String packagename = intent.getDataString();
			packagename = packagename.substring(packagename.indexOf(":") + 1,
					packagename.length());

			PackageManager pm = context.getPackageManager();
			PackageInfo pinfo = null;
			try {
				pinfo = pm.getPackageInfo(packagename, 0);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			if (pinfo != null) {
				
				ApplicationInfo appInfo = pinfo.applicationInfo;
				InstalledAppInfo installedAppInfo = new InstalledAppInfo();
				installedAppInfo.setName(pinfo.applicationInfo.loadLabel(pm)
						.toString());
				installedAppInfo.setIconId(appInfo.icon);
				installedAppInfo.setPackageName(appInfo.packageName);
				installedAppInfo.setVersionCode(pinfo.versionCode);
				installedAppInfo.setVersion(pinfo.versionName);
				installedAppInfo.setApkPath(appInfo.sourceDir);
				setAppFlag(installedAppInfo, appInfo);
				
				dao.updateInstalledApp(installedAppInfo);
		
				context.sendBroadcast(new Intent(Globals.MARKET_UPDATE_ACTION));
			}

		} else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
			String packagename = intent.getDataString();
			packagename = packagename.substring(packagename.indexOf(":") + 1,
					packagename.length());
			
	
			
			dao.deleteInstalledApp(packagename);

			context.sendBroadcast(new Intent(Globals.MARKET_UPDATE_ACTION));
		}

	
		InstalledAppInfo selfInfo = dao.getInstalledAppInfo(context
				.getPackageName());
		if (selfInfo == null) {
			PackageManager pm = context.getPackageManager();
			PackageInfo pinfo = null;
			try {
				pinfo = pm.getPackageInfo(context.getPackageName(), 0);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			if (pinfo != null) {
				ApplicationInfo appInfo = pinfo.applicationInfo;
				InstalledAppInfo installedAppInfo = new InstalledAppInfo();
				installedAppInfo.setName(pinfo.applicationInfo.loadLabel(pm)
						.toString());
				installedAppInfo.setIconId(appInfo.icon);
				installedAppInfo.setPackageName(appInfo.packageName);
				installedAppInfo.setVersionCode(pinfo.versionCode);
				installedAppInfo.setVersion(pinfo.versionName);
				installedAppInfo.setApkPath(appInfo.sourceDir);
				setAppFlag(installedAppInfo, appInfo);
				dao.insert(installedAppInfo);
			}
		}
		dao.closeDatabase();
		
		updateListData(context);

		AppDownloadService.updateDownloadProgress();
	}

	private void updateListData(Context context) {
		InstalledAppDao dao = new InstalledAppDao(context);
		dao.openDatabase();
		List<InstalledAppInfo> installedAppList = dao.getInstalledAppList();
		Map<String, InstalledAppInfo> installedAppMap = new HashMap<String, InstalledAppInfo>();
		for (InstalledAppInfo info : installedAppList) {
			installedAppMap.put(info.getPackageName(), info);
		}
		dao.closeDatabase();
		InstallAppManager.setInstalledAppList(installedAppList);
		InstallAppManager.setInstalledAppMap(installedAppMap);
	}

	private void setAppFlag(InstalledAppInfo installedAppInfo,
			ApplicationInfo appInfo) {
		if ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
			// 代表的是系统的应用,但是被用户升级了. 用户应用
			installedAppInfo.setAppFlag(InstalledAppInfo.FLAG_UPDATE);
		} else if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
			// 代表的用户的应用
			installedAppInfo.setAppFlag(InstalledAppInfo.FLAG_USER);
		} else {
			// 系统应用
			installedAppInfo.setAppFlag(InstalledAppInfo.FLAG_SYSTEM);
		}
	}

}
