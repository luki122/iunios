package com.aurora.weatherwidget;


import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import totalcount.AddCountHelp;

import com.aurora.weatherforecast.R;
import com.aurora.weatherforecast.AuroraWeatherMain;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherWidgetProvider extends AppWidgetProvider{  


	public static final String TAG = "555555"; 
	private String[] days;
	private Calendar calendar;
	private String mFormat;
	public final static String M24 = "kk:mm";
	public final static String M12 = "hh:mm";
	private static int[] DIGITS = new int[] { R.drawable.weather_widget_time0, R.drawable.weather_widget_time1,
			R.drawable.weather_widget_time2, R.drawable.weather_widget_time3, R.drawable.weather_widget_time4,
			R.drawable.weather_widget_time5, R.drawable.weather_widget_time6, R.drawable.weather_widget_time7,
			R.drawable.weather_widget_time8, R.drawable.weather_widget_time9 };

	@Override  
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,  
			int[] appWidgetIds) {  


		Log.e(TAG, "---WeatherWidgetProvider onUpdate----");

		context.startService(new Intent("com.aurora.WEATHER_WIDGET_SERVICE"));

		super.onUpdate(context, appWidgetManager, appWidgetIds);    
        AddCountHelp.addCount(AddCountHelp.ADD_WIDGET, context);
	}

	@Override  
	public void onReceive(Context context, Intent intent) {  
		// TODO Auto-generated method stub  
		super.onReceive(context, intent);

		//Log.e(TAG, "---WeatherWidgetProvider onReceive----");
		//context.startService(new Intent("com.aurora.WEATHER_WIDGET_SERVICE"));
	}  


	/**
	 * 第一个Widget组件启动时触发
	 */ 
	public void onEnabled(Context context){ 

		//Log.e(TAG, "---WeatherWidgetProvider onEnabled----");
		//context.startService(new Intent("com.aurora.WEATHER_WIDGET_SERVICE"));
	} 

	/**
	 * 最后一个Widget组件关闭时触发
	 */ 
	public void onDisabled(Context context){ 
		//Log.e(TAG, "---WeatherWidgetProvider onDisabled----");

		context.stopService(new Intent("com.aurora.WEATHER_WIDGET_SERVICE"));
	} 

	/**
	 * 任一Widget组件被删除时触发
	 */ 
	public void onDeleted(Context context, int[] appWidgetIds){ 
		//Log.e(TAG, "---WeatherWidgetProvider onDeleted----");
	}

}  