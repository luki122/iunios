package com.aurora.market.install;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.aurora.market.R;
import com.aurora.market.activity.module.MarketUpdateActivity;
import com.aurora.market.activity.setting.DownloadManagerActivity;
import com.aurora.market.download.ApkUtil;
import com.aurora.market.model.DownloadData;
import com.aurora.market.service.AppInstallService;

/*
 * @author 张伟
 */

public class InstallNotification {

	private static String TAG = "InstallNotification";
	private static Context mContext;

	private static int notiId = 0x23456789;
	private static int notiId_finished = 0x22456789;
	private static int notiId_finished_auto = 0x23456789;
	private static int notiId_failed = 0x24456789;
	private static int notiId_update = 0x25456789;
	private static Notification notification;
	private static Notification notification_finished;
	private static Notification notification_finished_auto;
	private static Notification notification_failed;
	private static Notification notification_update;
	public static ArrayList<String> install_success = new ArrayList<String>();
	public static ArrayList<String> install_success_auto = new ArrayList<String>();
	public static ArrayList<String> install_failed = new ArrayList<String>();

	public static void init(Context context) {

		mContext = context;

	}

	/**
	 * 发送正在安装的广播
	 * 
	 */
	public static void sendInstallingNotify() {
		if (notification == null) {
			notification = new Notification();
			notification.icon = android.R.drawable.stat_sys_download;
			notification.contentView = new RemoteViews(
					mContext.getPackageName(), R.layout.notification_install);
			// 将此通知放到通知栏的"Ongoing"即"正在运行"组中
			notification.flags = Notification.FLAG_ONGOING_EVENT;
			// 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
			// notification.flags |= Notification.FLAG_NO_CLEAR;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
		}
		StringBuffer title = new StringBuffer();
		int downloadSize = 0;
		int fileSize = 0;
		int appCount = 0;
		for (int key : AppInstallService.getInstalls().keySet()) {
			AppInstall downloader = AppInstallService.getInstalls().get(key);

			if (title.length() != 0) {
				title.append("，");
			}
			appCount++;
			title.append(downloader.getDownloadData().getApkName());
		}

		notification.contentView.setTextViewText(R.id.app_sum, appCount
				+ mContext.getString(R.string.notification_status_dis_install));
		notification.contentView.setTextViewText(R.id.title, title.toString());

		Intent intent = new Intent(mContext, DownloadManagerActivity.class);

		notification.contentIntent = PendingIntent.getActivity(mContext, 1,
				intent, 0);
		if (title.length() != 0) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(notiId, notification);
		}
	}

	/**
	 * 取消正在安装广播
	 * 
	 */
	public static void cancleInsatllingNotify() {
		if (null != mContext) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(notiId);
		}
		notification = null;
	}

	/**
	 * 发送安装完成的广播
	 * 
	 */
	public static void sendInstalledNotify(String apkname,String packageName) {
		if (notification_finished == null) {
			notification_finished = new Notification();
			notification_finished.icon = android.R.drawable.stat_sys_download;
			notification_finished.contentView = new RemoteViews(
					mContext.getPackageName(), R.layout.notification_installed);
			// 将此通知放到通知栏的"Ongoing"即"正在运行"组中
			// notification_finished.flags = Notification.FLAG_ONGOING_EVENT;
			// 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
			// notification_finished.flags |= Notification.FLAG_NO_CLEAR;
			notification_finished.flags |= Notification.FLAG_AUTO_CANCEL;
			install_success.clear();

			install_success.add(apkname);
		}
		StringBuffer title = new StringBuffer();

		int appCount = 0;
		for (String appName : install_success) {

			if (title.length() != 0) {
				title.append("，");
			}
			appCount++;
			title.append(appName);
		}

		if (appCount == 1) {
			notification_finished.contentView
					.setTextViewText(
							R.id.app_sum,
							title
									+ mContext
											.getString(R.string.notification_status_installed));
			notification_finished.contentView.setTextViewText(R.id.title,
					mContext.getString(R.string.notification_status_click_dis));
		} else {
			notification_finished.contentView
					.setTextViewText(
							R.id.app_sum,
							appCount
									+ mContext
											.getString(R.string.notification_status_dis_installed));
			notification_finished.contentView.setTextViewText(R.id.title,
					title.toString());
		}
		Intent intent;
		if(appCount == 1)
		{
			/*PackageManager packageManager = mContext.getPackageManager();
			intent = packageManager.getLaunchIntentForPackage(packageName);*/
			
			
		   intent = new Intent(mContext, CleanUpIntent.class);
			intent.setAction("notification_installed_one");
			intent.putExtra("pkgName", packageName);
			notification_finished.contentIntent = PendingIntent.getBroadcast(
					mContext, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				
		}
		else
		{
			intent = new Intent(mContext, DownloadManagerActivity.class);
			intent.putExtra("openinstall", 1);
			notification_finished.contentIntent = PendingIntent.getActivity(
					mContext, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}

	
		
		
		
		Intent intent1 = new Intent(mContext, CleanUpIntent.class);
		intent1.setAction("notification_installed_cancelled");
		notification_finished.deleteIntent = PendingIntent.getBroadcast(
				mContext, 4, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
		if (title.length() != 0) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(notiId_finished, notification_finished);
		}
	}

	/**
	 * 取消安装完成广播
	 * 
	 */
	public static void cancleInsatlledNotify() {
		if (null != mContext) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(notiId_finished);
		}
		install_success.clear();
		notification_finished = null;
	}

	/**
	 * 发送自动更新安装完成的广播
	 * 
	 */
	public static void sendAutoUpdateInstalledNotify(String apkname) {
		Log.i(TAG, "sendAutoUpdateInstalledNotify: " + apkname);
		if (notification_finished_auto == null) {
			notification_finished_auto = new Notification();
			notification_finished_auto.icon = android.R.drawable.stat_sys_download;
			notification_finished_auto.contentView = new RemoteViews(
					mContext.getPackageName(), R.layout.notification_installed);
			// 将此通知放到通知栏的"Ongoing"即"正在运行"组中
			// notification_finished.flags = Notification.FLAG_ONGOING_EVENT;
			// 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
			// notification_finished.flags |= Notification.FLAG_NO_CLEAR;
			notification_finished_auto.flags |= Notification.FLAG_AUTO_CANCEL;
			install_success_auto.clear();
			install_success_auto.add(apkname);
		}
		StringBuffer title = new StringBuffer();

		int appCount = 0;
		for (String appName : install_success_auto) {

			if (title.length() != 0) {
				title.append("，");
			}
			appCount++;
			title.append(appName);
		}

		notification_finished_auto.contentView.setTextViewText(R.id.app_sum,
				mContext.getString(R.string.notification_status_dis_auto_upate,
						appCount));
		notification_finished_auto.contentView.setTextViewText(R.id.title,
				title.toString());

		Intent intent = new Intent(mContext, MarketUpdateActivity.class);
		intent.putExtra("openinstall", 1);
		notification_finished_auto.contentIntent = PendingIntent.getActivity(
				mContext, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		if (title.length() != 0) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(notiId_finished_auto,
					notification_finished_auto);
		}
	}

	/**
	 * 取消自动更新安装完成的广播
	 * 
	 */
	public static void cancleFinishedAutodNotify() {
		if (null != mContext) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(notiId_finished_auto);
		}
		notification_finished_auto = null;
	}

	/**
	 * 发送有应用需要更新的广播
	 * 
	 */
	public static void sendUpdateNotify(ArrayList<DownloadData> upLists) {
		if (notification_update == null) {
			notification_update = new Notification();
			notification_update.icon = android.R.drawable.stat_sys_download;
			notification_update.contentView = new RemoteViews(
					mContext.getPackageName(), R.layout.notification_update);
			// 将此通知放到通知栏的"Ongoing"即"正在运行"组中
			// notification_finished.flags = Notification.FLAG_ONGOING_EVENT;
			// 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
			// notification_finished.flags |= Notification.FLAG_NO_CLEAR;
			notification_update.flags |= Notification.FLAG_AUTO_CANCEL;

		}
		StringBuffer title = new StringBuffer();

		int appCount = upLists.size();
		for (DownloadData listitem : upLists) {

			if (title.length() != 0) {
				title.append("，");
			}
			title.append(listitem.getApkName());
		}

		if (appCount == 1) {
			notification_update.contentView.setTextViewText(R.id.app_sum, title
					+ mContext.getString(R.string.notification_status_update));
			notification_update.contentView.setTextViewText(R.id.title,
					mContext.getString(R.string.notification_status_click_dis));

		} else {
			notification_update.contentView
					.setTextViewText(
							R.id.app_sum,
							appCount
									+ mContext
											.getString(R.string.notification_status_dis_update));
			notification_update.contentView.setTextViewText(R.id.title,
					title.toString());
		}

		Intent intent1 = new Intent(mContext, DownloadManagerActivity.class);
		Bundle bundle = new Bundle();

		bundle.putParcelableArrayList("updatedata", upLists);// /putParcelable("parcelableUser",

		intent1.putExtras(bundle);

		PendingIntent pentnet = PendingIntent.getActivity(mContext, 6, intent1,
				PendingIntent.FLAG_UPDATE_CURRENT);

		notification_update.contentView.setOnClickPendingIntent(
				R.id.image_update, pentnet);

		Intent intent = new Intent(mContext, MarketUpdateActivity.class);

		intent.putExtra("update_count", appCount);
		// intent.putParcelableArrayListExtra("updatedata", upLists);
		notification_update.contentIntent = PendingIntent.getActivity(mContext,
				7, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// if (title.length() != 0) {
		NotificationManager notificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(notiId_update, notification_update);
		// }
	}

	/**
	 * 取消有应用需要更新的广播
	 * 
	 */
	public static void cancleUpdateNotify() {
		if (null != mContext) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(notiId_update);
		}
		notification_update = null;
	}

	/**
	 * 发送安装失败的广播
	 * 
	 */
	public static void sendInstallFailedNotify(String apkname, String packageName) {
		if (notification_failed == null) {
			notification_failed = new Notification();
			notification_failed.icon = android.R.drawable.stat_sys_download;
			notification_failed.contentView = new RemoteViews(
					mContext.getPackageName(), R.layout.notification_installed);
			// 将此通知放到通知栏的"Ongoing"即"正在运行"组中
			// notification_failed.flags = Notification.FLAG_ONGOING_EVENT;
			// 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
			// notification_failed.flags |= Notification.FLAG_NO_CLEAR;
			notification_failed.flags |= Notification.FLAG_AUTO_CANCEL;
			install_failed.clear();
			install_failed.add(apkname);
		}
		StringBuffer title = new StringBuffer();

		int appCount = 0;
		for (String appName : install_failed) {

			if (title.length() != 0) {
				title.append("，");
			}
			appCount++;
			title.append(appName);
		}

		if (appCount == 1) {
			notification_failed.contentView
					.setTextViewText(
							R.id.app_sum,
							title
									+ mContext
											.getString(R.string.notification_status_installfailed));
			notification_failed.contentView.setTextViewText(R.id.title,
					mContext.getString(R.string.notification_status_click_dis));
		} else {
			notification_failed.contentView
					.setTextViewText(
							R.id.app_sum,
							appCount
									+ mContext
											.getString(R.string.notification_status_dis_installfailed));
			notification_failed.contentView.setTextViewText(R.id.title,
					title.toString());
		}

		Intent intent = new Intent(mContext, DownloadManagerActivity.class);
		intent.putExtra("openinstall", 2);
		intent.putExtra("packageName", packageName);
		notification_failed.contentIntent = PendingIntent.getActivity(mContext,
				8, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		Intent intent1 = new Intent(mContext, CleanUpIntent.class);
		intent1.setAction("notification_failed_cancelled");
		notification_failed.deleteIntent = PendingIntent.getBroadcast(mContext,
				9, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
		if (title.length() != 0) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(notiId_failed, notification_failed);
		}
	}

	/**
	 * 取消安装失败的广播
	 * 
	 */
	public static void cancleInstallFailedNotify() {
		if (null != mContext) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(notiId_failed);
		}
		install_failed.clear();
		notification_failed = null;
	}
}
