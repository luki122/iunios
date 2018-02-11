package com.aurora.thememanager.utils;

import android.content.Context;

public class DensityUtils {
	
	/**
	 * get current screen width
	 * @param context
	 * @return width of current density screen
	 */
	public static int getScreenWidth(Context context){
		return context.getResources().getDisplayMetrics().widthPixels;
	}
	
	/**
	 * get current screen height
	 * @param context
	 * @return height of current density screen
	 */
	public static  int getScreenHeight(Context context){
		return context.getResources().getDisplayMetrics().heightPixels;
	}

}
