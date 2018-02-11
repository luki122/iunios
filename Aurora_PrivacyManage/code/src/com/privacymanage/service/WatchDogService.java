package com.privacymanage.service;

import com.privacymanage.data.AccountData;
import com.privacymanage.data.AidlAccountData;
import com.privacymanage.interfaces.AccountObserver;
import com.privacymanage.model.AccountModel;
import com.privacymanage.utils.ApkUtils;
import com.privacymanage.utils.LogUtils;
import com.aurora.privacymanage.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

public class WatchDogService extends Service implements AccountObserver{	
	private final String TAG = WatchDogService.class.getName();
	private final String NOTIFY_BTN_KEY = "notifyBtn";
	private final int ID_OF_KEEP_PRIVACY_SPACE = 1000;
	private final int NOTIFICATION_ID_OF_Normal = 10000;
	private  NotificationManager notiManager;
	private  RemoteViews remoteView;
	private  PendingIntent notifyBtnPendingIntent;
	
	@Override
	public void onCreate() {		
		initNotify();
		registerReceiverFunc();
		AccountModel.getInstance().attach(this);		
		super.onCreate();		 
	}
	
	private void registerReceiverFunc(){
		IntentFilter mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
		registerReceiver(mScreenOffReceiver, mScreenOffFilter);	
		mScreenOffFilter.setPriority(android.content.IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
	}
	
	private void initNotify(){
		notiManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		Intent intent = new Intent(this,WatchDogService.class);
		intent.putExtra(NOTIFY_BTN_KEY,"change_keep_state");
		notifyBtnPendingIntent = PendingIntent.getService(this,0,intent, 0);
		
		remoteView = new RemoteViews(getPackageName(),R.layout.quit_privacy_space_notify_view);
		updateNotifyView();
		remoteView.setOnClickPendingIntent(R.id.keepBtn, notifyBtnPendingIntent);
		remoteView.setOnClickPendingIntent(R.id.removeBtn, notifyBtnPendingIntent);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null && intent.getStringExtra(NOTIFY_BTN_KEY) != null){
			AccountModel.getInstance().changeIsKeepPrivacySpace();
			updateNotifyView();
			if (ApkUtils.isLowVersion()) {
				showNotification();
				showNormalLow();
			} else {
				showNormalHigh();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
			
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}	

	@Override
	public void onDestroy() {
		AccountModel.getInstance().detach(this);
		if(mScreenOffReceiver != null){
			unregisterReceiver(mScreenOffReceiver);
			mScreenOffReceiver = null;
		}
		super.onDestroy();
	}

	@Override
	public void switchAccount(AccountData accountData) {
		if(accountData != null && 
				accountData.getAccountId() != AccountData.NOM_ACCOUNT){
			updateNotifyView();
			if (ApkUtils.isLowVersion()) {
				showNotification();
				showNormalLow();
			} else {
				showNormalHigh();
			}
		}else{
			notiManager.cancel(ID_OF_KEEP_PRIVACY_SPACE);
			notiManager.cancel(NOTIFICATION_ID_OF_Normal);
		}
	}

	@Override
	public void deleteAccount(AccountData accountData, boolean delete) {
		notiManager.cancel(ID_OF_KEEP_PRIVACY_SPACE);	
		notiManager.cancel(NOTIFICATION_ID_OF_Normal);
	}
	
	private void updateNotifyView(){
		if(AccountModel.getInstance().getIsKeepPrivacySpace()){
			remoteView.setTextViewText(R.id.titleText, getString(R.string.keep_privacy_space) );
			remoteView.setTextViewText(R.id.subText, getString(R.string.lock_screen_not_quit_privacy_space));
			
			remoteView.setViewVisibility(R.id.keepBtn, View.INVISIBLE);
			remoteView.setViewVisibility(R.id.removeBtn, View.VISIBLE);
		}else{
			remoteView.setTextViewText(R.id.titleText, getString(R.string.no_keep_privacy_space) );
			remoteView.setTextViewText(R.id.subText, getString(R.string.lock_screen_quit_privacy_space));
			
			remoteView.setViewVisibility(R.id.keepBtn, View.VISIBLE);
			remoteView.setViewVisibility(R.id.removeBtn, View.INVISIBLE);
		}
	}
	
	private void showNotification() {	
		Notification noti = new Notification();
		noti.flags |= Notification.FLAG_ONGOING_EVENT;
		noti.icon = R.drawable.aurora_sock_01;
		noti.contentView = remoteView;		
		notiManager.notify(ID_OF_KEEP_PRIVACY_SPACE, noti);
	}
	
	/**
	 * 单纯的用于通知栏闪烁的锁图标
	 */
	private void showNormalLow() {
		Notification notification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.aurora_systemui_aurora_sock_anim)
				.setTicker(null)
				.setContentTitle(" ")
				.setSubText(" ")
				.setAutoCancel(true)
				.build();
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notiManager.notify(NOTIFICATION_ID_OF_Normal, notification);
	}

	private void showNormalHigh() {
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.aurora_systemui_aurora_sock_anim)
                .setContent(remoteView)
                .build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notiManager.notify(NOTIFICATION_ID_OF_Normal, notification);
    }
	
	private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LogUtils.printWithLogCat(TAG, "receive android.intent.action.SCREEN_OFF");			
			if(!AccountModel.getInstance().getIsKeepPrivacySpace()){
				AccountModel.getInstance().quitPrivacyAccount();
			}			
		}
	};
}
