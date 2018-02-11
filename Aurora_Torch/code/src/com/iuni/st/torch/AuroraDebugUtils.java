package com.iuni.st.torch;

import android.util.Log;

public final class AuroraDebugUtils {

	public static boolean DEBUGABLE = true;
	private static final String TAG = "TORCH";
	
	public static void torchLogd(String tag, String info){
		if(DEBUGABLE) Log.e(TAG, tag + " => " + info);
	}
	
	public static void torchLoge(String tag, String info){
		if(DEBUGABLE) Log.e(TAG, tag + " => " + info);
	}
}
