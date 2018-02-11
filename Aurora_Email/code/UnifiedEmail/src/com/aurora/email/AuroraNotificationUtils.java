package com.aurora.email;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import com.android.mail.R;
import com.android.mail.ui.MailActivity;
import com.aurora.email.AuroraComposeActivity.AuroraSendStatus;
import com.aurora.email.AuroraComposeActivity.AuroraSendStatusCallback;

public class AuroraNotificationUtils {

	private static AuroraNotificationUtils install;
	private NotificationManager mNotificationManager;
	// private Notification mNotification;
	private NotificationCompat.Builder mBuilder;
	private String packageName;
	private static final int SEND_STATUS = 1000;
	private static final int SEND_FAIL_STATUS = 101;
	private boolean isSendEmail = false; // 是否正在发送email
	private boolean isEnterActivity = false; // 是否进入activity
	private Handler mHandler = new Handler();
	private AuroraSendStatusCallback mSendStatusCallback = null;
	
	public AuroraNotificationUtils(Context context) {
		mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		isEnterActivity = false;
		isSendEmail = false;
		packageName = context.getPackageName();
	}

	public static AuroraNotificationUtils getInstance(Context context) {
		//Log.d("chenhl", "getInstance()!!....:" + install);
		if (install == null) {
			install = new AuroraNotificationUtils(context);
		}
		return install;
	}

	public void showSendFailNotification(Context context) {
	//	Log.d("chenhl", "showSendFailNotification()!!....1");
		if(mSendStatusCallback!=null){
			mSendStatusCallback.onSendStatusChanged(AuroraSendStatus.AURORAEMAIL_SEND_FAIL);
		}
		isSendEmail = false;
		mBuilder = new NotificationCompat.Builder(context);
		if (mBuilder == null || mNotificationManager == null) {
			return;
		}
		Intent intent = new Intent(Intent.ACTION_VIEW);
		
		PendingIntent pendingintent = PendingIntent.getActivity(context, 0,
				intent, 0);
		RemoteViews views = new RemoteViews(packageName,
				R.layout.aurora_send_fail_notify);

		mBuilder.setSmallIcon(R.drawable.aurora_email_send_fail_icon);
		mBuilder.setContentTitle("test");
		mBuilder.setContentText("fail").setAutoCancel(true).setOngoing(true);
		mBuilder.setContent(views).setContentIntent(pendingintent)
				.setTicker(context.getString(R.string.aurora_send_failed));
		mNotificationManager.cancel(SEND_STATUS);
		//mNotificationManager.notify(SEND_FAIL_STATUS, mBuilder.build());
	}

	public void showSendSuccessNotification(Context context) {
		isSendEmail = false;
		if(mSendStatusCallback!=null){
			mSendStatusCallback.onSendStatusChanged(AuroraSendStatus.AURORAEMAIL_SEND_SUCCES);
		}
		if (isEnterActivity) {
			return;
		}
		mBuilder = new NotificationCompat.Builder(context);
		if (mBuilder == null || mNotificationManager == null) {
			return;
		}
		mBuilder.setSmallIcon(R.drawable.aurora_email_send_success_icon);
		mBuilder.setContentTitle("test");
		mBuilder.setContentText("success").setAutoCancel(true).setOngoing(true).setTicker(context.getString(R.string.aurora_send_success));
		mNotificationManager.notify(SEND_STATUS, mBuilder.build());
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if(mNotificationManager!=null)
				mNotificationManager.cancel(SEND_STATUS);
			}
		}, 3000);
		//Log.d("chenhl", "showSendSuccessNotification...");
	}

	public void showSendingNotification(Context context) {
		//Log.d("chenhl", "showSendingNotification...isSendEmail:"+isSendEmail+" isEnterActivity:"+isEnterActivity);
		if (isEnterActivity || !isSendEmail) {
			return;
		}
		mBuilder = new NotificationCompat.Builder(context);
		if (mBuilder == null || mNotificationManager == null) {
			return;
		}
		mBuilder.setSmallIcon(R.drawable.aurora_email_sending_icon);
		mBuilder.setContentTitle("test");
		mBuilder.setContentText("sending").setAutoCancel(true).setOngoing(true);
		mNotificationManager.notify(SEND_STATUS, mBuilder.build());
		//Log.d("chenhl", "showSendingNotification...end");
	}

	public void setAuroraSendStatusCallback(AuroraSendStatusCallback callback){
		mSendStatusCallback = callback;
	}
	
	public void setEntryState(boolean is) {
		isEnterActivity = is;
		if(isEnterActivity){
			mNotificationManager.cancel(SEND_STATUS);
		}
	}

	public void setSendingState(boolean is){
		isSendEmail=is;
	}
	
	public boolean isSendingEmail() {
		return isSendEmail;
	}
}
