package com.aurora.weatherforecast;

import com.aurora.weatherwidget.WeatherWidgetService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;
import android.util.Log;

public class WeatherStartReciver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("jadon", "is server running = "+WeatherWidgetService.isServerRunning);
        if(!WeatherWidgetService.isServerRunning)
        {
        	Intent service = new Intent("com.aurora.WEATHER_WIDGET_SERVICE");
        	context.startService(service);
        }
	}

}
