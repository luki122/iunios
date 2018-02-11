package com.aurora.utils;

import com.android.deskclock.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
public class NotificationOperate {

	private static NotificationManager notificationManager;
	private static Notification notification;
	@SuppressWarnings("deprecation")
	public static void createNotifaction(Context context,int tabNum,String contentTitle,String contentText){
		if(notificationManager == null){
			notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		int icon = R.drawable.alarm_left_icon;
        // Gionee baorui 2013-02-18 modify for CR00771526 begin
        /*
        if (tabNum == 1) {
            icon = R.drawable.chronometer_notification;
        } else if (tabNum == 2) {
            icon = R.drawable.stopwatch_notification;
        }
        */
        if (tabNum == 101) {
            icon = R.drawable.chronometer_notification;
        } else if (tabNum == 102) {
            icon = R.drawable.stopwatch_notification;
        }
        // Gionee baorui 2013-02-18 modify for CR00771526 end
		notification = new Notification(icon,contentTitle,System.currentTimeMillis());
		notification.flags |= Notification.FLAG_NO_CLEAR;
		Intent intent=new Intent(context,context.getClass());
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
        // Gionee baorui 2013-02-19 modify for CR00771861 begin
        // intent.putExtra("tabNum", tabNum);
        if (tabNum == 101) {
            intent.putExtra("tabNum", 3);
        } else if (tabNum == 102) {
            intent.putExtra("tabNum", 2);
        } else {
            intent.putExtra("tabNum", tabNum);
        }
        // Gionee baorui 2013-02-19 modify for CR00771861 end
	    PendingIntent pendingIntent=PendingIntent.getActivity(context, tabNum, intent, PendingIntent.FLAG_UPDATE_CURRENT);   
	    notification.setLatestEventInfo(context, contentTitle, contentText, pendingIntent);   
	    notificationManager.notify(tabNum, notification);
	}
	public static void cancelNotification(Context context,int tabNum){
		if(notificationManager == null){
			notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		notificationManager.cancel(tabNum);
	}
//	public static void cancelAllNoti(Context context){
//		if(notificationManager == null){
//			notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
//		}
//		notificationManager.cancelAll();
//	}
	
}
