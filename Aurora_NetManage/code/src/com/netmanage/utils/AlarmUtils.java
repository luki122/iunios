package com.netmanage.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.content.Context;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import com.netmanage.receive.AlarmReceiver;

public class AlarmUtils {
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static String TAG = AlarmUtils.class.getName();	
	private static final int Alert_HOUR = 9;
	private static final int Alert_MINUTE = 0;
	private static final int Alert_SECOND = 0;
	private static final long Alert_SPACE = 24*60*60*1000;
	
	public static void setNextAlert(Context context){
		 Context mContext = context.getApplicationContext();
		 
		 Calendar calendar = Calendar.getInstance();	
		 int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
         calendar.set(Calendar.HOUR_OF_DAY, Alert_HOUR);
         calendar.set(Calendar.MINUTE, Alert_MINUTE);
         calendar.set(Calendar.SECOND, Alert_SECOND);
         
         long alertTime = 0;
		 if(nowHour<Alert_HOUR){
			 alertTime = calendar.getTimeInMillis();
		 }else{
			 alertTime = calendar.getTimeInMillis()+Alert_SPACE;
		 }
		 
         Intent intent = new Intent(context,AlarmReceiver.class);
         PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 
        		 0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
         
         AlarmManager am = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
         am.set(AlarmManager.RTC_WAKEUP, alertTime, pendingIntent);
         Log.i(TAG,"set Alarm time "+sdf.format(alertTime));
	}
	
	public static void disableAlarm(Context context){
		 Context mContext = context.getApplicationContext();
		
		 Intent intent = new Intent(mContext, AlarmReceiver.class);
         PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 
        		 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
         AlarmManager am = (AlarmManager)mContext.getSystemService(mContext.ALARM_SERVICE);
         am.cancel(pendingIntent);
	}
	
	/**
	 * 判断当前的闹钟广播是不是有效广播，
	 * 因为当把时间调整到 超过 闹钟设定的时间，也会发送一个闹钟广播，此时的闹钟广播为无效广播
	 * @return
	 */
	public static boolean isValidAlarmReceiver(){
		 Calendar calendar = Calendar.getInstance();	
		 int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
		 int nowMin = calendar.get(Calendar.MINUTE);
		 if(Alert_HOUR == nowHour && Alert_MINUTE == nowMin){
			 return true;
		 }else{
			 return false;
		 }
	}
}
