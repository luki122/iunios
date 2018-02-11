/**
 * 
 */
package com.yulore.superyellowpage.lib;

import android.content.Context;

public class YuloreResourceMap {

	public static int getMenuId(Context paramContext, String paramString) {
		return paramContext.getResources().getIdentifier(paramString, "menu", paramContext.getPackageName());
	}
	
	public static int getLayoutId(Context paramContext, String paramString) {
		return paramContext.getResources().getIdentifier(paramString, "layout",
				paramContext.getPackageName());
	}

	public static int getStringId(Context paramContext, String paramString) {
		return paramContext.getResources().getIdentifier(paramString, "string",
				paramContext.getPackageName());
	}

	public static int getDrawableId(Context paramContext, String paramString) {
		return paramContext.getResources().getIdentifier(paramString,
				"drawable", paramContext.getPackageName());
	}

	public static int getStyleId(Context paramContext, String paramString) {
		return paramContext.getResources().getIdentifier(paramString, "style",
				paramContext.getPackageName());
	}

	public static int getViewId(Context paramContext, String paramString) {
		return paramContext.getResources().getIdentifier(paramString, "id",
				paramContext.getPackageName());
	}

	public static int getColorId(Context paramContext, String paramString) {
		 return paramContext.getResources().getIdentifier(paramString,
		 "color", paramContext.getPackageName());
	}

	public static int getDimenId(Context paramContext, String paramString) {
		return paramContext.getResources().getIdentifier(paramString, "dimen",
				paramContext.getPackageName());
	}

	public static int getAnimId(Context paramContext, String paramString) {
		return paramContext.getResources().getIdentifier(paramString, "anim",
				paramContext.getPackageName());
	}
	
	public static int getRawId(Context paramContext, String paramString) {
		return paramContext.getResources().getIdentifier(paramString, "raw",
				paramContext.getPackageName());
	}
}
