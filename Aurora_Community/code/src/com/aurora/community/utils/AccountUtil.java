package com.aurora.community.utils;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.aurora.community.CommunityApp;
import com.aurora.community.activity.account.LoginActivity;

public class AccountUtil {
	
	private static AccountUtil instance = null;
	
	private boolean iuniOS = false;
	
	private AccountUtil() {
		new Thread() {
			@Override
			public void run() {
				iuniOS = checkIuniOs(CommunityApp.getInstance());
			}
		}.start();
	}
	
	public static AccountUtil getInstance() {
		if (instance == null) {
			instance = new AccountUtil();
		}
		return instance;
	}

	public boolean isIuniOS() {
		return iuniOS;
	}
	
	public void startLogin(Activity activity) {
		if (iuniOS) {
			Uri uri = Uri.parse("openaccount://com.aurora.account.login");
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.putExtra("type", 1);
			intent.setData(uri);
			// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			activity.startActivityForResult(intent, Globals.REQUEST_LOGIN_CODE);
		} else {
			Intent intent = new Intent(activity, LoginActivity.class);
			intent.putExtra("type", 1);
			activity.startActivity(intent);
//			activity.startActivityForResult(intent, Globals.REQUEST_LOGIN_LOCAL_CODE);
		}
	}
	
	public boolean isLogin() {
		boolean login = false;
		if (iuniOS) {
			login = AccountHelper.mLoginStatus;
		} else {
			BooleanPreferencesUtil bPref = BooleanPreferencesUtil
					.getInstance(CommunityApp.getInstance());
			login = bPref.hasLogin();
		}
		return login;
	}
	
	private boolean checkIuniOs(Context context) {
		final PackageManager packageManager = context.getPackageManager();
		// 获取所有已安装程序的包信息
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
		for (int i = 0; i < pinfo.size(); i++) {
			if (pinfo.get(i).packageName.equalsIgnoreCase("com.aurora.account"))
				return true;
		}
		return false;
	}

}
