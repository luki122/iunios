package com.android.gallery3d.xcloudalbum.tools;

import android.util.Log;

public class LogUtil {
	private static final boolean isDebug = true;
	private static final String TAG = "Aurora_Gallery";

	public static void d(String STAG, String msg) {
		if (isDebug) {
			Log.d(TAG + ":" + STAG, msg);
		}
	}

	public static void e(String STAG, String msg) {
		Log.e(TAG + ":" + STAG, msg);
	}
	
	public static void dump(String STAG){
		Log.d(TAG+":"+STAG, "dump",new Throwable());
	}

}
