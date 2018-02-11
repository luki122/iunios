package com.aurora.adblock;

import java.text.SimpleDateFormat;
import android.util.Log;

public class LogUtils {
	private static Boolean isLogCatPrint = true;
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

	public static void printWithLogCat(Boolean flag, String filter,String content) {
		if (flag) {
			Log.i(filter, sdf.format(System.currentTimeMillis())+" ---- "+content);
		}
	}

	public static void printWithLogCat(String filter, String content) {
		printWithLogCat(isLogCatPrint, filter, content);
	}

	public static void setLogOnOff(boolean flag) {
		isLogCatPrint = flag;
	}
}
