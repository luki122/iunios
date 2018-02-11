package com.aurora.weatherwidget;

import java.util.Calendar;
import java.util.Locale;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.IBinder;
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

import com.aurora.weatherforecast.R;
import com.aurora.weatherforecast.AuroraWeatherMain;
import com.aurora.weatherforecast.WeatherMainFragment;

import datas.CityListShowData;
import datas.CityListShowItem;
import datas.DynamicDeskIconBrocdcastReceiver;
import datas.WeatherAnimInfo;
import datas.WeatherData;

public class WeatherWidgetService extends Service{

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

	private int mWidgetWeatherIcon = -1;
	private String mWidgetWeatherType;
	private StringBuffer mStringBuffer;
	public static final String defaultString = "未知 未知 --/--";

	public static boolean isServerRunning = false;
	
	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver()  
	{  
		@Override  
		public void onReceive(Context context, Intent intent)  
		{  
			
			ReicverHolder holder = new ReicverHolder();
			holder.context = context;
			holder.intent = intent;
			Message msg = new Message();
			msg.obj = holder;
			handler.sendMessage(msg);
		}  
	};

	private class ReicverHolder{
		Context context;
		Intent intent;
	}
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			ReicverHolder holder = (ReicverHolder) msg.obj;
			Context context = holder.context;
			Intent intent = holder.intent;
			String action = intent.getAction();  
			if (action.equals(WeatherMainFragment.UPDATE_BROADCAST_ACTION)) {  
				getWeatherData(context, intent);
			} 
			notifyWidget(context);
		};
	};
	
	
	private void getWeatherData( Context context, Intent intent ) {

		String cityName = intent.getStringExtra("cityname");

		WeatherAnimInfo weatherAnimInfo = CityListShowData.getInstance(context).getWeatherAnimInfo();

		String weatherType = intent.getStringExtra("weatherType");
		String curTemp = String.valueOf(intent.getIntExtra("curTemp", 0));

		mWidgetWeatherType = weatherAnimInfo.getWidgetWeatherTypeString(weatherType);
		if (mWidgetWeatherType == null) {
			mWidgetWeatherType = context.getResources().getString(R.string.weather_default);
		}
		mWidgetWeatherIcon = weatherAnimInfo.getWidgetWeatherIcon(weatherType);

		mStringBuffer = new StringBuffer(cityName);
		mStringBuffer.append("  ");
		mStringBuffer.append(mWidgetWeatherType);
		mStringBuffer.append("  ");
		mStringBuffer.append(curTemp);
	}

	@Override  
	public void onCreate()  
	{  
		//Log.e(TAG, "WeatherWidgetService onCreate()");  
		super.onCreate();  

		IntentFilter filter = new IntentFilter();  
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(WeatherMainFragment.UPDATE_BROADCAST_ACTION);
		registerReceiver(mIntentReceiver, filter);
		isServerRunning = true;
	}



	@Override  
	public void onStart(Intent intent, int startId)  
	{  
		super.onStart(intent, startId);  
		//String action = intent.getAction();  
		//Log.e(TAG, "WeatherWidgetService onStart() ");  

		//getWeatherData( );
		//notifyWidget(this);  
	} 

	private void sendBroadCastToUpdateWeather() {
		Intent tmpIntent = new Intent(DynamicDeskIconBrocdcastReceiver.DYNAMIC_WEATHER_ICON);
		tmpIntent.putExtra("comfrom", "widget");
		sendBroadcast(tmpIntent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		//Log.e(TAG, "WeatherWidgetService onStartCommand() ");
		sendBroadCastToUpdateWeather( );
		notifyWidget(this) ;
		return Service.START_STICKY;
	}

	private void notifyWidget(Context context) {

		//Log.e(TAG, "---WeatherWidgetService notifyWidget----");

		days = context.getResources().getStringArray(R.array.daysChinese);
		calendar = Calendar.getInstance();

		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.update_weatherwidget_layout);

		mFormat = android.text.format.DateFormat.is24HourFormat(context) ? M24 : M12;
		CharSequence newTime = DateFormat.format(mFormat, calendar);

		String weekDay = String.valueOf(calendar.get(Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DAY_OF_MONTH) +
				"日   " + days[calendar.get(Calendar.DAY_OF_WEEK) - 1];

		int hour = 0;
		if ( mFormat.equals(M24) ) {
			hour = calendar.get(Calendar.HOUR_OF_DAY);
		} else {
			hour = calendar.get(Calendar.HOUR);
			if ( hour == 0 ) {
				hour = 12;
			}
		}
		int minute = calendar.get(Calendar.MINUTE);

		remoteViews.setImageViewResource(R.id.hourimage1, DIGITS[hour / 10]);
		remoteViews.setImageViewResource(R.id.hourimage2, DIGITS[hour % 10]);
		remoteViews.setImageViewResource(R.id.minuteimage1, DIGITS[minute / 10]);
		remoteViews.setImageViewResource(R.id.minuteimage2, DIGITS[minute % 10]);
		remoteViews.setTextViewText(R.id.txtall, weekDay);

		if ( mWidgetWeatherIcon != -1 ) {
			remoteViews.setImageViewResource(R.id.weatherimage, mWidgetWeatherIcon); 
		}

		if ( mStringBuffer != null ) {
			remoteViews.setTextViewText(R.id.weatherdetail, mStringBuffer);
		}

//		if (WeatherWidgetUtils.getCurrentLocale(Locale.getDefault()) ) {
//			remoteViews.setViewVisibility(R.id.txtall, View.VISIBLE);
//		} else {
//			remoteViews.setViewVisibility(R.id.txtall, View.GONE);
//		}
		
		Intent intent = new Intent(); 
		ComponentName comp = new ComponentName("com.android.deskclock", "com.android.deskclock.AlarmClock");  
   	 	intent.setComponent(comp);   
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);  
		remoteViews.setOnClickPendingIntent(R.id.layout2, pendingIntent); 

		intent = new Intent(context, AuroraWeatherMain.class); 
		pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);  
		remoteViews.setOnClickPendingIntent(R.id.layout3, pendingIntent); 

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		appWidgetManager.updateAppWidget(new ComponentName(this, WeatherWidgetProvider.class),
				remoteViews);
	}

	@Override  
	public void onDestroy()  
	{  
		//Log.e(TAG, "WeatherWidgetService onDestroy()");  

		unregisterReceiver(mIntentReceiver);  
		isServerRunning = false;
		super.onDestroy();  
	}  
	@Override  
	public IBinder onBind(Intent intent)  
	{  
		//Log.e(TAG, "WeatherWidgetService onBind()");  
		return null;  
	}  
}
