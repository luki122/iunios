
package com.android.gallery3d.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtil {

	public static int getInt(Context context, String strKey, int nDefault) {
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		int nRet = preference.getInt(strKey, nDefault);
		return nRet;
	}

	public static void setInt(Context context, String strKey, int nValue) {
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		preference.edit().putInt(strKey, nValue).commit();
	}

	public static String getString(Context context, String strKey, String strDefault) {
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		String strRet = preference.getString(strKey, strDefault);
		return strRet;
	}

	public static void setString(Context context, String strKey, String strValue) {
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		preference.edit().putString(strKey, strValue).commit();
	}

	public static boolean getBoolean(Context context, String strKey, boolean bDefault) {
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		boolean bRet = preference.getBoolean(strKey, bDefault);
		return bRet;
	}

	public static void setBoolean(Context context, String strKey, boolean bValue) {
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		preference.edit().putBoolean(strKey, bValue).commit();
	}

	public static float getFloat(Context context, String strKey, float fDefault) {
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		float fRet = preference.getFloat(strKey, fDefault);
		return fRet;
	}

	public static void setFloat(Context context, String strKey, float fValue) {
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		preference.edit().putFloat(strKey, fValue).commit();
	}

	public static long getLong(Context context, String strKey, long lDefault) {
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		long lRet = preference.getLong(strKey, lDefault);
		return lRet;
	}

	public static void setLong(Context context, String strKey, long lValue) {
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		preference.edit().putLong(strKey, lValue).commit();
	}
	
}
