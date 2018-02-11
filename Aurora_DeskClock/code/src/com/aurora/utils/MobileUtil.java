package com.aurora.utils;

import com.android.deskclock.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

public class MobileUtil {

	private static String i1 = "i1";
	
	private static boolean isIModel(){
		Log.e("jadon", android.os.Build.MODEL);
		return android.os.Build.MODEL.contains(i1);
	}
	
	public static LinearLayout getAppWidgetTimeLayout(Context context){
		
		LayoutInflater inflater = LayoutInflater.from(context);
		
		if(isIModel())
		{
			return (LinearLayout) inflater.inflate(R.layout.update_app_widget_time_i1, null, false);
		}else{
			return (LinearLayout)inflater.inflate(R.layout.update_app_widget_time, null, false);
		}
	}
	
	public static RemoteViews getAppWidgetTimeRemoteViews(Context context)
	{
		if(isIModel())
		{
			return new RemoteViews(context.getPackageName(),R.layout.update_appwidget_time_i1);
		}else{
			return new RemoteViews(context.getPackageName(),R.layout.update_appwidget_time);
		}
	}
}
