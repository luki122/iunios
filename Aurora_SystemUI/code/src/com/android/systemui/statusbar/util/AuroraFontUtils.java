package com.android.systemui.statusbar.util;

import android.widget.TextView;
import android.graphics.Typeface;

public abstract class AuroraFontUtils {
	public static final String TAG = "AuroraFontUtils";

	public static void setNumType(TextView v){
		if(null != v){
			v.setTypeface(Typeface.createFromFile("/system/fonts/number.ttf"));
		}
	}
	
}
