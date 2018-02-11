package com.aurora.account.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.aurora.account.R;
import com.aurora.account.activity.LoginActivity;
import com.aurora.account.activity.SyncAccountActivity;
import com.aurora.account.util.TimeUtils;

/*
 * @author 张伟
 */

public class WarnNotification {

//	private static String TAG = "InstallNotification";

	private static int notiId = 0x23456789;
	private static Notification notification;

	/**
	 * 发送联系人未同步消息提醒
	 * 
	 */
	public static void sendContactsNotify(Context context) {
		if (notification == null) {
			notification = new Notification();
			notification.icon = android.R.drawable.stat_sys_download;
			notification.contentView = new RemoteViews(
			        context.getPackageName(), R.layout.notification_warn);
			// 将此通知放到通知栏的"Ongoing"即"正在运行"组中
			// notification_failed.flags = Notification.FLAG_ONGOING_EVENT;
			// 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
			// notification_failed.flags |= Notification.FLAG_NO_CLEAR;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;

		}

		notification.contentView.setTextViewText(R.id.app_time,
				TimeUtils.getCurrentTime1());

		Intent intent = new Intent(context, LoginActivity.class);
		intent.putExtra(SyncAccountActivity.EXTRA_KEY_DIS_WARN, true);
		
		notification.contentIntent = PendingIntent.getActivity(context, 8,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(notiId, notification);

	}

}
