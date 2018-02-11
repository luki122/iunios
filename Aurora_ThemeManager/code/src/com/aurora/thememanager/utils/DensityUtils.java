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
	
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
