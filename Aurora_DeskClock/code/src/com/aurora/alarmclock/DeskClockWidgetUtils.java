package com.aurora.alarmclock;

import java.util.Calendar;
import java.util.Locale;

import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.View;

import com.android.deskclock.Log;

public class DeskClockWidgetUtils {
	//aurora add by tangjun 2014.4.14 start
	private static Typeface auroraWidgetFace;
	private static Typeface auroraWidgetFace2;
	private static final String WIDGET_TYPEFACE_FONT_1 = "/system/fonts/Roboto-Thin.ttf";
	private static final String WIDGET_TYPEFACE_FONT_2 = "/system/fonts/Roboto-Light.ttf";

	static {
		Log.e("static dai ma kuai");
		auroraCreateTitleFont( );
		auroraCreateTitleFontEx( );

	}

	private static Typeface auroraCreateTitleFont( ) {
		try {

			auroraWidgetFace = Typeface.createFromFile(WIDGET_TYPEFACE_FONT_2);
		} catch (Exception e) {
			// TODO: handle exception
			e.getCause();
			e.printStackTrace();
			auroraWidgetFace = null;
		}

		return auroraWidgetFace;
	}

	private static Typeface auroraCreateTitleFontEx( ) {

		try {

			auroraWidgetFace2 = Typeface.createFromFile(WIDGET_TYPEFACE_FONT_2);
		} catch (Exception e) {
			// TODO: handle exception
			e.getCause();
			e.printStackTrace();
			auroraWidgetFace2 = null;
		}

		return auroraWidgetFace2;
	}

	public static Typeface auroraGetTitleFontTypeFace()
	{
		if(auroraWidgetFace == null)
			auroraWidgetFace = auroraCreateTitleFont();
		return 	auroraWidgetFace;
	}

	public static Typeface auroraGetTitleFontTypeFaceEx()
	{
		if(auroraWidgetFace2 == null)
			auroraWidgetFace2 = auroraCreateTitleFontEx();
		return 	auroraWidgetFace2;
	}
	
    public static boolean getCurrentLocale(Locale locale) {
    	
    	boolean isChinese = true;
    	
    	if(!locale.getLanguage().equals("zh")){
    		isChinese = false;
    	}
    	return isChinese;
    }
	//aurora add by tangjun 2014.4.14 end
}
