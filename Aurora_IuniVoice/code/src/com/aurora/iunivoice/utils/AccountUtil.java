package com.aurora.iunivoice.utils;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.aurora.iunivoice.IuniVoiceApp;
import com.aurora.iunivoice.activity.account.LoginActivity;
import com.aurora.iunivoice.activity.account.RegisterActivity;

public class AccountUtil {

	private static AccountUtil instance = null;

	private boolean iuniOS = false;

	private AccountUtil() {
//		new Thread() {
//			@Override
//			public void run() {
//				iuniOS = checkIuniOs(IuniVoiceApp.getInstance());
//			}
//		}.start();
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
			// activity.startActivityForResult(intent,
			// Globals.REQUEST_LOGIN_LOCAL_CODE);
		}
	}

	public boolean isLogin() {
		boolean login = false;
		if (iuniOS) {
			login = AccountHelper.mLoginStatus;
		} else {
			BooleanPreferencesUtil bPref = BooleanPreferencesUtil
					.getInstance(IuniVoiceApp.getInstance());
			login = bPref.hasLogin();
		}
		return login;
	}

	public void register(Context context) {
//		if (isIuniOS()) {
//			Intent i = new Intent();
//			ComponentName component = new ComponentName("com.aurora.account",
//					"com.aurora.account.activity.RegisterActivity");
//			i.setComponent(component);
//			context.startActivity(i);
//		} else {
			Intent intent = new Intent(context, RegisterActivity.class);
			context.startActivity(intent);
		//}
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
