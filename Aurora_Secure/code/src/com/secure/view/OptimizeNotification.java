package com.secure.view;

import com.secure.activity.MainActivity;
import com.secure.activity.PermissionManageActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.aurora.secure.R;

/**
 * 权限优化相关提示
 * 提示条件：
 * 1.手机重启，检测到有权限优化时；
 * 2.安装一个新应用时，该应用有权限优化时；
 */
public class OptimizeNotification {
	private static final int NOTIFY_ID_OF_Optimize=2;
	private static NotificationManager myNotiManager;
	private static Handler handler = new Handler();
	
	public static void notify(final Context context){
		handler.postDelayed(new Runnable() {			
			@Override
			public void run() {
				OptimizeNotification.notify(context,
						PermissionManageActivity.class,
						false,
						R.drawable.ic_launcher,
						context.getString(R.string.optimize_notify),
						context.getString(R.string.optimize_notify_hint));
				
			}
		}, 1000);
	}
	
	
	
	/**
	 * 
	 * @param context
	 * @param toActivity
	 * @param autoCancel
	 * @param drawId
	 * @param title
	 * @param text
	 */
	private static void notify(
			Context context,
			Class toActivity,
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
		if(toActivity != null){
		    notifyIntent=new Intent(context,toActivity);  
		    notifyIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
		}else{
			notifyIntent=new Intent(Intent.ACTION_MAIN);
			notifyIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			notifyIntent.setClass(context, MainActivity.class);
			notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
					Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		}	
		
		PendingIntent appIntent=PendingIntent.getActivity(context,0,notifyIntent,0);
	    
	    Notification notification =new Notification();
	    notification.flags |= Notification.FLAG_AUTO_CANCEL;
	    notification.icon=drawId; 
	    notification.tickerText=text;
//	    notification.defaults=Notification.DEFAULT_SOUND;
	    notification.defaults |= Notification.DEFAULT_SOUND;
	    notification.setLatestEventInfo(context,title,text,appIntent);
	    myNotiManager.notify(NOTIFY_ID_OF_Optimize,notification);
	}
}
