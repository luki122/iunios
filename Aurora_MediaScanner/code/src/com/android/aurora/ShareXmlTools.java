package com.android.aurora;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ShareXmlTools {

	private static final String Xml = "Scanner";
	private static final String bootTime = "bootTime";
	private static final String scannerTime = "scannerTime";
	private static final String scannerTag = "scannerTag";
	private static final String entryTag = "entryTag";
	
	private static final String XML = "AppVersion";

	public static void saveAppVersion(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(XML,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(XML, "1");
		editor.commit();
	}

	public static String getAppVersion(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(XML,
				Context.MODE_PRIVATE);
		return preferences.getString(XML, "0");
	}

	public synchronized static void setBootTime(Context context, long time) {
		if (context == null) {
			return;
		}
		SharedPreferences sharedPreferences = context.getSharedPreferences(Xml,
				Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putLong(bootTime, time);
		editor.commit();
	}

	public synchronized static void setScannerTime(Context context, long time) {
		if (context == null) {
			return;
		}
		SharedPreferences sharedPreferences = context.getSharedPreferences(Xml,
				Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putLong(scannerTime, time);
		editor.commit();
	}

	public synchronized static long getScannerTime(Context context) {
		if (context == null) {
			return 0;
		}
		SharedPreferences sharedPreferences = context.getSharedPreferences(Xml,
				Context.MODE_PRIVATE);
		return sharedPreferences.getLong(scannerTime, 0);
	}

	public synchronized static long getBootTime(Context context) {
		if (context == null) {
			return 0;
		}
		SharedPreferences sharedPreferences = context.getSharedPreferences(Xml,
				Context.MODE_PRIVATE);
		return sharedPreferences.getLong(bootTime, 0);
	}
	
	public synchronized static void setBootScannerTag(Context context,boolean tag){
		if (context == null) {
			return ;
		}
		SharedPreferences sharedPreferences = context.getSharedPreferences(Xml,
				Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(scannerTag, tag);
		editor.commit();
	}
	
	public synchronized static boolean getBootScannerTag(Context context) {
		if (context == null) {
			return false;
		}
		SharedPreferences sharedPreferences = context.getSharedPreferences(Xml,
				Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean(scannerTag, false);
	}
	
	public synchronized static void setEntryTag(Context context,boolean tag){
		if (context == null) {
			return ;
		}
		SharedPreferences sharedPreferences = context.getSharedPreferences(Xml,
				Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(entryTag, tag);
		editor.commit();
	}
	
	public synchronized static boolean getEntryTag(Context context) {
		if (context == null) {
			return false;
		}
		SharedPreferences sharedPreferences = context.getSharedPreferences(Xml,
				Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean(entryTag, false);
	}

}
