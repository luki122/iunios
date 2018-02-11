package com.aurora.market.download;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

public class ApkUtil {
	
	/**
	 * 安装APK
	 * 
	 * @param ctx
	 * @param file
	 */
	public static void installApp(Context context, File file) {
//		execMethod(ctx,file.getAbsolutePath());
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri data = Uri.fromFile(file);
		String type = "application/vnd.android.package-archive";
		intent.setDataAndType(data, type);
		context.startActivity(intent);
	}
	
	/**
	 * 根据包名打开软件
	 * 
	 * @param activity
	 * @param packagename
	 */
	public static void openApp(Context context, String packagename) {
		Intent intent;
		PackageManager packageManager = context.getPackageManager();
		try {
			intent = packageManager.getLaunchIntentForPackage(packagename);
			if (intent != null) {
				context.startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
