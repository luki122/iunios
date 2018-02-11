package com.android.gallery3d.xcloudalbum.tools;

import android.util.Log;


public class LogUtil {
	private static final boolean isDebug = true;
	private static final String TAG="JXH";
	public static void d(String STAG,String msg){
		if(isDebug){
			Log.d(TAG+":"+STAG, msg);
		}
	}

}
