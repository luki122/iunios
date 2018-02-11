package com.android.aurora;

import android.util.Log;

public class AuroraLog {

	private static final boolean isDebug = true;
	public static final String TAG="Aurora_MediaScanner";

	public static void eLog(String TAG, String msg) {
		if (isDebug) {
			Log.e(AuroraLog.TAG, TAG+">>>"+msg);
		}
	}

	public static void vLog(String TAG, String msg) {
		if (isDebug) {
			Log.v(AuroraLog.TAG, TAG+">>>"+msg);
		}
	}

	public static void iLog(String TAG, String msg) {
		if (isDebug) {
			Log.i(AuroraLog.TAG, TAG+">>>"+msg);
		}
	}

	public static void dLog(String TAG, String msg) {
		if (isDebug) {
			Log.d(AuroraLog.TAG, TAG+">>>"+msg);
		}
	}
}
