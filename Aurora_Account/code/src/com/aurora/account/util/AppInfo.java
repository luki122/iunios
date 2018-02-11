package com.aurora.account.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;

public class AppInfo {
	// Context context ;
	private PackageManager pm;

	public AppInfo(Context context) {
		// this.context = context;
		pm = context.getPackageManager();
	}

	/*
	 * 获取程序 图标
	 */
	public Drawable getAppIcon(String packname) {
		try {
			ApplicationInfo info = pm.getApplicationInfo(packname, 0);
			return info.loadIcon(pm);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * 获取程序的版本号
	 */
	public String getAppVersion(String packname) {

		try {
			PackageInfo packinfo = pm.getPackageInfo(packname, 0);
			return packinfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * 获取程序的名字
	 */
	public String getAppName(String packname) {
		try {
			ApplicationInfo info = pm.getApplicationInfo(packname, 0);
			return info.loadLabel(pm).toString();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * 获取程序的权限
	 */
	public String[] getAppPremission(String packname) {
		try {
			PackageInfo packinfo = pm.getPackageInfo(packname,
					PackageManager.GET_PERMISSIONS);
			// 获取到所有的权限
			return packinfo.requestedPermissions;

		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;

		}
	}

	/*
	 * 获取程序的签名
	 */
	public String getAppSignature(String packname) {
		try {
			PackageInfo packinfo = pm.getPackageInfo(packname,
					PackageManager.GET_SIGNATURES);
			// 获取到所有的权限
			return packinfo.signatures[0].toCharsString();

		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
}