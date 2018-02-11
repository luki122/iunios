package com.netmanage.view;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * 存储空间不足相关提示
 *
 */
public class MyNotification {
	private static NotificationManager myNotiManager;
	
	/**
	 * @param context
	 * @param toActivity
	 * @param autoCancel
	 * @param drawId
	 * @param title
	 * @param text
	 */
	public static void notify(
			int id,
			Context context,
			ComponentName componentName,
			boolean autoCancel,
			int drawId,
			String title,
			String text){
		if(context == null || text == null){
			return ;
		}
		
		if(myNotiManager == null){
			myNotiManager=(NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);	
		}		
		
		Intent notifyIntent = null;
		if(componentName != null){
		    notifyIntent=new Intent();  
		    notifyIntent.setComponent(componentName);
		    notifyIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
		}else{
			notifyIntent=new Intent(Intent.ACTION_MAIN);
			notifyIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
					Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		}	
		
		PendingIntent appIntent=PendingIntent.getActivity(context,0,notifyIntent,0);
	    
	    Notification notification =new Notification();
	    notification.flags |= Notification.FLAG_AUTO_CANCEL;
	    notification.icon=drawId; 
	    notification.tickerText=text;
	    notification.defaults=Notification.DEFAULT_SOUND;
	    notification.setLatestEventInfo(context,title,text,appIntent);
	    myNotiManager.notify(id,notification);
	}
}
