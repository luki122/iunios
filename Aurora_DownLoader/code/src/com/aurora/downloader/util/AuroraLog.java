package com.aurora.downloader.util;

import android.util.Log;

public class AuroraLog {

	private static final boolean isDebug = true;

	public static void log(String TAG, String msg) {
		if (isDebug) {
			Log.d(TAG, msg);
		}
	}
	public static void elog(String TAG, String msg) {
		if (isDebug) {
			Log.e(TAG, msg);
		}
	}
}
