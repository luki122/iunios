package com.aurora.mediascanner;

import android.util.Log;

public class LogUtil {

	private static final boolean isDebug = true;
	public static final String TAG="Aurora_MediaScanner";

	public static void e(String TAG, String msg) {
		if (isDebug) {
			Log.e(LogUtil.TAG, TAG+"_"+msg);
		}
	}

	public static void d(String TAG, String msg) {
		if (isDebug) {
			Log.d(LogUtil.TAG, TAG+"_"+msg);
		}
	}
}
