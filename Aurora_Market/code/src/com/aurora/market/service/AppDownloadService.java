package com.aurora.market.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.aurora.market.R;
import com.aurora.market.marketApp;
import com.aurora.market.activity.setting.DownloadManagerActivity;
import com.aurora.market.db.AppDownloadDao;
import com.aurora.market.download.DownloadInitListener;
import com.aurora.market.download.DownloadStatusListener;
import com.aurora.market.download.DownloadUpdateListener;
import com.aurora.market.download.FileDownloader;
import com.aurora.market.download.FilePathUtil;
import com.aurora.market.model.DownloadData;
import com.aurora.market.util.Globals;
import com.aurora.market.util.SystemUtils;


public class AppDownloadService extends Service {
	
	private static final String TAG = "AppDownloadService";
	
	public static final String DOWNLOAD_DATA = "download_data";
	
	public static final String DOWNLOAD_OPERATION = "download_operation"; // 下载操作
	public static final int OPERATION_START_DOWNLOAD = 100; // 开始下载
	public static final int OPERATION_PAUSE_DOWNLOAD = 101; // 暂停下载
	public static final int OPERATION_CONTINUE_DOWNLOAD = 102; // 继续下载
	public static final int OPERATION_PAUSE_CONTINUE_DOWNLOAD = 103; // 继续或暂停下载
	public static final int OPERATION_CANCLE_DOWNLOAD = 104; // 取消下载
	public static final int OPERATION_NETWORK_CHANGE = 105; // 网络改变
	public static final int OPERATION_NETWORK_MOBILE_PAUSE = 106; // 网络改变为手机网络需要暂停情况下
	public static final int OPERATION_NETWORK_MOBILE_CONTINUE = 107; // 网络改变为手机网络需要暂停情况下
	private static final int HANDEL_CHECK = 200; // 处理检查操作
	
	private static Context context; // 上下文对象
	
	private static Map<Integer, FileDownloader> downloaders; // 正在下载的任务下载器
	private FileDownloader currentDownloader; // 当前操作的下载器
	private DownloadData currentDownloadData; // 当前操作的下载数据
	private static AppDownloadDao appDownloadDao; // 数据库操作对象
	
	private boolean downloading = false; // 是否正在下载
	private int noDownloadingSend = 0; // 没有在下载时, 已发送广播的次数
	private boolean notificationFlag = false; // 是否发送通知标识, 两秒一次
	private int notiId = 0x12345789;
	private Notification notification;
	
	private static List<DownloadInitListener> initListenerList;
	private static List<DownloadUpdateListener> updateListenerList;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "setvice onCreate");

		
		if (isNeedInitData()) {
			context = this;
			downloaders = new ConcurrentHashMap<Integer, FileDownloader>();
			appDownloadDao = new AppDownloadDao(this);
			appDownloadDao.openDatabase();

			// Service被创建时, 先从数据库找到已有任务, 并放入到列表中
			List<Integer> appIds = appDownloadDao.getAllappId();
			for (int appId : appIds) {
				DownloadData downloadData = appDownloadDao.getDownloadData(appId);
				int status = appDownloadDao.getStatus(appId);
				if (status < FileDownloader.STATUS_INSTALL_WAIT) {
					DownloadStatusListener listener = new DownloadStatusListener() {
						@Override
						public void onDownload(int appId, int status, 
								long downloadSize, long fileSize) {
							// 当下载完成时, 发送下载完成广播
							if (status == FileDownloader.STATUS_INSTALL_WAIT
									|| (downloadSize == fileSize && fileSize != 0)) {
								Intent finish = new Intent(
									Globals.BROADCAST_ACTION_DOWNLOAD);
								context.sendBroadcast(finish);
								
								AppInstallService.startInstall(context,
										appDownloadDao.getDownloadData(appId), AppInstallService.TYPE_NORMAL);
							}
						}
					};
					String dirPath = "";
					String dirFromDb = appDownloadDao.getFileDir(appId);
					if (!TextUtils.isEmpty(dirFromDb)) {
						dirPath = dirFromDb;
					} else {
						dirPath = FilePathUtil.getAPKFilePath(this);
					}
					FileDownloader downloader = new FileDownloader(downloadData, new File(dirPath), 
							listener, FileDownloader.TYPE_NORMAL);
					downloaders.put(downloadData.getApkId(), downloader);
				}
			}
			
			// 告知Service已经加载完成
			if (initListenerList != null) {
				for (DownloadInitListener listener : initListenerList) {
					listener.onFinishInit();
				}
				initListenerList.clear();
				initListenerList = null;
			}
		}

		mHandle.sendEmptyMessage(HANDEL_CHECK);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 获取要操作的数据信息
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				currentDownloadData = (DownloadData) bundle.get(DOWNLOAD_DATA);

				// 获取操作指令
				int operation = bundle.getInt(DOWNLOAD_OPERATION);
				handleOperation(operation);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "setvice onDestroy");
		
		if (initListenerList != null) {
			initListenerList.clear();
		}
		initListenerList = null;
		
		mHandle.removeMessages(HANDEL_CHECK);
	}

	private Handler mHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case HANDEL_CHECK:
				notificationFlag = !notificationFlag;
				downloading = false;

				List<Integer> finishKey = new ArrayList<Integer>(); // 已完成任务的所有key列表

				// 遍历任务列表, 进行相应操作
				for (int key : downloaders.keySet()) {
					FileDownloader downloader = downloaders.get(key);
					// 如果有正在下载的任务, 则把标识设置为true
					int status = downloader.getStatus();
					if (status == FileDownloader.STATUS_DOWNLOADING
							|| status == FileDownloader.STATUS_CONNECTING
							|| status == FileDownloader.STATUS_WAIT
							|| status == FileDownloader.STATUS_CONNECT_RETRY) {
						downloading = true;
						noDownloadingSend = 0;
					}
					
					// 把已完成的任务key加入到已完成列表中
					if (downloader.getStatus() >= FileDownloader.STATUS_INSTALL_WAIT) {
						finishKey.add(key);
					}
				}
				// 对已完成的任务进行操作
				for (int key : finishKey) {
					downloaders.remove(key);
					
				}

				// 发送更新广播
				if (downloading) {
					noDownloadingSend = 0;
					updateDownloadProgress();
					// 如果标示为true, 则发送通知
					if (notificationFlag) {
						sendDownloadingNotify();
					}
				} else if (noDownloadingSend < 1) { // 没有任务正在下载, 且广播发送次数小于1时
					noDownloadingSend++;
					updateDownloadProgress();
					cancleDownloadingNotify();
				} else {
					stopSelf();
					
					AutoUpdateService.continueAutoUpdate(context);
					return;
				}

				mHandle.sendEmptyMessageDelayed(HANDEL_CHECK, 1000);
				break;
			}
		}
	};
	
	/**
	 * 处理指令
	 * @param operation
	 */
	private void handleOperation(int operation) {
		switch (operation) {
		// 开始下载
		case OPERATION_START_DOWNLOAD:
			// 判断在列表中是否存在这个下载器, 若没有的话, 创建一个下载器
			if (downloaders.containsKey(currentDownloadData.getApkId())) {
				currentDownloader = downloaders.get(currentDownloadData
						.getApkId());
				download(currentDownloader);
			} else {
				File dir = new File(FilePathUtil.getAPKFilePath(this));
				DownloadStatusListener listener = new DownloadStatusListener() {
					@Override
					public void onDownload(int appId, int status,
							long downloadSize, long fileSize) {
//						// 当下载完成时, 发送下载完成广播
						if (status == FileDownloader.STATUS_INSTALL_WAIT
								|| (downloadSize == fileSize && fileSize != 0)) {
							Intent finish = new Intent(
									Globals.BROADCAST_ACTION_DOWNLOAD);
								context.sendBroadcast(finish);
							
							AppInstallService.startInstall(context,
									appDownloadDao.getDownloadData(appId), AppInstallService.TYPE_NORMAL);
						}
					}
				};
				FileDownloader fileDownloader = new FileDownloader(currentDownloadData, dir, listener, FileDownloader.TYPE_NORMAL);
				currentDownloader = fileDownloader;
				if (currentDownloader.getStatus() < FileDownloader.STATUS_INSTALL_WAIT) {
					downloaders.put(currentDownloadData.getApkId(), fileDownloader);
					download(currentDownloader);
				}
			}
			break;
		// 暂停下载
		case OPERATION_PAUSE_DOWNLOAD:
			currentDownloader = downloaders.get(currentDownloadData.getApkId());
			if (currentDownloader != null) {
				currentDownloader.pause();
			}
			break;
		// 继续下载
		case OPERATION_CONTINUE_DOWNLOAD:
			currentDownloader = downloaders.get(currentDownloadData.getApkId());
			if (currentDownloader != null) {
				download(currentDownloader);
			}
			break;
		// 暂停或继续下载
		case OPERATION_PAUSE_CONTINUE_DOWNLOAD:
			currentDownloader = downloaders.get(currentDownloadData.getApkId());
			if (currentDownloader != null) {
				int status = currentDownloader.getStatus();
				if (status == FileDownloader.STATUS_DOWNLOADING
						|| status == FileDownloader.STATUS_CONNECTING
						|| status == FileDownloader.STATUS_WAIT
						|| status == FileDownloader.STATUS_NO_NETWORK
						|| status == FileDownloader.STATUS_CONNECT_RETRY) {
					currentDownloader.pause();
				} else {
					download(currentDownloader);
				}
			}
			updateDownloadProgress();
			break;
		// 取消下载
		case OPERATION_CANCLE_DOWNLOAD:
			currentDownloader = downloaders.get(currentDownloadData.getApkId());
			if (currentDownloader != null) {
				currentDownloader.cancel();
			} else {
				if (currentDownloadData.getFileName() != null) {
					File file = new File(currentDownloadData.getFileDir(), currentDownloadData.getFileName());
					file.delete();
				}
				appDownloadDao.delete(currentDownloadData.getApkId());
			}
			updateDownloadProgress();
//			cancleNotify(currentDownloadData.getApkId());
			break;
		case OPERATION_NETWORK_CHANGE:
			if (SystemUtils.isWifiNetwork(context)) {
				List<FileDownloader> list = new ArrayList<FileDownloader>();
				for (int key : downloaders.keySet()) {
					FileDownloader downloader = downloaders.get(key);
					list.add(downloader);
				}
				sortList(list);
				for (FileDownloader downloader : list) {
					// 找到网络错误状态的任务, 开始进行下载
					if ((downloader.getStatus() == FileDownloader.STATUS_NO_NETWORK)) {
						download(downloader);
					}
				}
			} else { // 网络变为不可用时
		
				for (int key : downloaders.keySet()) {
					FileDownloader downloader = downloaders.get(key);
					int status = downloader.getStatus();
					if (status == FileDownloader.STATUS_CONNECTING
							|| status == FileDownloader.STATUS_DOWNLOADING
							|| status == FileDownloader.STATUS_WAIT
							|| status == FileDownloader.STATUS_PAUSE_NEED_CONTINUE) {
						downloader.setStatus(FileDownloader.STATUS_NO_NETWORK);
					}
				}
			}
			break;
		case OPERATION_NETWORK_MOBILE_PAUSE:
			for (int key : downloaders.keySet()) {
				FileDownloader downloader = downloaders.get(key);
				int status = downloader.getStatus();
				if (status == FileDownloader.STATUS_CONNECTING
						|| status == FileDownloader.STATUS_DOWNLOADING
						|| status == FileDownloader.STATUS_NO_NETWORK
						|| status == FileDownloader.STATUS_WAIT) {
					downloader.setStatus(FileDownloader.STATUS_PAUSE_NEED_CONTINUE);
				}
			}
			
			break;
		case OPERATION_NETWORK_MOBILE_CONTINUE:
			List<FileDownloader> list = new ArrayList<FileDownloader>();
			//List<FileDownloader> list_pause = new ArrayList<FileDownloader>();
			for (int key : downloaders.keySet()) {
				FileDownloader downloader = downloaders.get(key);
			/*	if((downloader.getStatus() == FileDownloader.STATUS_CONNECTING)||(downloader.getStatus() == FileDownloader.STATUS_DOWNLOADING)||(downloader.getStatus() == FileDownloader.STATUS_FAIL))
				{
					downloader.pause();
					list_pause.add(downloader);
				}*/
				
				list.add(downloader);
			}
			sortList(list);
			/*for(FileDownloader downloader : list_pause)
			{
				download(downloader);
			}*/
			
			for (FileDownloader downloader : list) {
				// 找到网络错误状态的任务, 开始进行下载
				if ((downloader.getStatus() == FileDownloader.STATUS_NO_NETWORK)
						|| (downloader.getStatus() == FileDownloader.STATUS_PAUSE_NEED_CONTINUE)) {
					download(downloader);
				}
			}
			
			break;	

		}
	}
	
	/**
	 * 对列表进行排序
	 * 
	 * @param list
	 */
	private void sortList(List<FileDownloader> list) {
		Collections.sort(list, new Comparator<FileDownloader>() {
			@Override
			public int compare(FileDownloader lhs, FileDownloader rhs) {
				int comparison;

				// 对创建时间进行排序
				comparison = (int) (lhs.getCreateTime() - rhs.getCreateTime());
				if (comparison != 0)
					return comparison;

				// 对SWID进行排序
				comparison = (int) (lhs.getDownloadData().getApkId() - rhs
						.getDownloadData().getApkId());
				if (comparison != 0)
					return comparison;

				return 0;
			}
		});
	}
	
	/**
	 * 下载
	 * 
	 * @param downloader
	 */
	private void download(FileDownloader downloader) {
		AutoUpdateService.pauseAutoUpdate();
		if (AutoUpdateService.getDownloaders() != null &&
				AutoUpdateService.getDownloaders().containsKey(
						downloader.getDownloadData().getApkId())) {
			AutoUpdateService.getDownloaders().get(downloader.getDownloadData().getApkId()).cancel();
		}
		
		// 开始下载
		if (downloader != null) {
			int status = downloader.getStatus();
			if (status == FileDownloader.STATUS_DOWNLOADING
					|| status == FileDownloader.STATUS_CONNECTING
					|| status == FileDownloader.STATUS_WAIT
				/*	|| status == FileDownloader.STATUS_NO_NETWORK*/
					|| status == FileDownloader.STATUS_CONNECT_RETRY) {
				return;
			} 
			downloader.downloadFile();
		}
	}
	
	/**
	 * 发送正在下载的广播
	 * 
	 */
	private void sendDownloadingNotify() {
		if (notification == null) {
			notification = new Notification();
			notification.icon = android.R.drawable.stat_sys_download;
			notification.contentView = new RemoteViews(
					context.getPackageName(), R.layout.notification_download);
			// 将此通知放到通知栏的"Ongoing"即"正在运行"组中
			notification.flags = Notification.FLAG_ONGOING_EVENT;
			// 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
			// notification.flags |= Notification.FLAG_NO_CLEAR;
		}
		StringBuffer title = new StringBuffer();
		int downloadSize = 0;
		int fileSize = 0;
		int appCount = 0;
		for (int key : downloaders.keySet()) {
			FileDownloader downloader = downloaders.get(key);
			if (downloader.getStatus() == FileDownloader.STATUS_CONNECTING
					|| downloader.getStatus() == FileDownloader.STATUS_DOWNLOADING) {
				if (title.length() != 0) {
					title.append("，");
				}
				appCount ++;
				title.append(downloader.getDownloadData().getApkName());
				downloadSize += downloader.getDownloadSize();
				fileSize += downloader.getFileSize();
			}
		}
		
		notification.contentView.setTextViewText(R.id.app_sum, appCount+getString(R.string.notification_status_dis_download));
		notification.contentView.setTextViewText(R.id.title, title.toString());
		int progress = (int) ((downloadSize * 1.0f) / fileSize * 100);
		notification.contentView.setTextViewText(R.id.tv_info, progress + "%");
		notification.contentView.setProgressBar(R.id.download_progress_notifi, 100, progress,false);
		Intent intent = new Intent(context, DownloadManagerActivity.class);
		notification.contentIntent = PendingIntent.getActivity(context, notiId,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		if (title.length() != 0) {
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(notiId, notification);
		}
	}
	
	/**
	 * 取消正在下载广播
	 * 
	 */
	private void cancleDownloadingNotify() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(notiId);
	}
	
	/**
	 * 获取是否需要重新构建Service的数据(静态变量可能会被回收)
	 * 
	 * @return
	 */
	private static boolean isNeedInitData() {
		if (appDownloadDao == null || downloaders == null
				|| context == null) {
			return true;
		}
		return false;
	}
	
	//=================================已下为public方法=================================//

	public static AppDownloadDao getAppDownloadDao() {
		return appDownloadDao;
	}
	
	public static Map<Integer, FileDownloader> getDownloaders() {
		if (null == downloaders) {
			downloaders = new ConcurrentHashMap<Integer, FileDownloader>();
			if (marketApp.getInstance().getApplicationContext() != null) {
				Intent i = new Intent(marketApp.getInstance().getApplicationContext(), AppDownloadService.class);
				marketApp.getInstance().getApplicationContext().startService(i);
			}
		}
		return downloaders;
	}
	
	/**
	 * 刷新进度
	 * 
	 */
	public static void updateDownloadProgress() {
		Log.i(TAG, "updateDownloadProgress()");
		if (updateListenerList != null) {
			for (DownloadUpdateListener updateListener : updateListenerList) {
				if (updateListener != null) {
					updateListener.downloadProgressUpdate();
				}
			}
		}
	}
	
	/**
	 * 检查Service数据是否构建完成
	 * 
	 * @param context
	 * @param serviceListener
	 */
	public static void checkInit(Context context, DownloadInitListener serviceListener) {
		if (isNeedInitData()) {
			if (initListenerList == null) {
				initListenerList = new ArrayList<DownloadInitListener>();
			}
			if (serviceListener != null) {
				initListenerList.add(serviceListener);
			}
			Intent initIntent = new Intent(context, AppDownloadService.class);
			context.startService(initIntent);
		} else {
			if (serviceListener != null) {
				serviceListener.onFinishInit();
			}
		}
	}
	
	/**
	 * 注册刷新监听
	 * 
	 * @param updateListener
	 */
	public static void registerUpdateListener(DownloadUpdateListener updateListener) {
		if (updateListenerList == null) {
			updateListenerList = new ArrayList<DownloadUpdateListener>();
		}
		if (updateListener != null) {
			updateListenerList.add(updateListener);
		}
	}
	
	/**
	 * 取消刷新监听
	 * 
	 * @param updateListener
	 */
	public static void unRegisterUpdateListener(DownloadUpdateListener updateListener) {
		if (updateListenerList != null && updateListener != null) {
			updateListenerList.remove(updateListener);
		}
	}
	
	/**
	 * 开始下载请求方法
	 * 
	 * @param context
	 * @param downloadData
	 */
	public static void startDownload(Context context, DownloadData downloadData) {
		Intent startDownload = new Intent(context, AppDownloadService.class);
		Bundle startDownloadBundle = new Bundle();
		startDownloadBundle.putParcelable(AppDownloadService.DOWNLOAD_DATA, downloadData);
		startDownloadBundle.putInt(AppDownloadService.DOWNLOAD_OPERATION, 
				AppDownloadService.OPERATION_START_DOWNLOAD);
		startDownload.putExtras(startDownloadBundle);
		context.startService(startDownload);
	}
	
	/**
	 * 暂停或继续请求方法
	 * 
	 * @param context
	 * @param downloadData
	 */
	public static void pauseOrContinueDownload(Context context, DownloadData downloadData) {
		Intent startDownload = new Intent(context, AppDownloadService.class);
		Bundle startDownloadBundle = new Bundle();
		startDownloadBundle.putParcelable(AppDownloadService.DOWNLOAD_DATA, downloadData);
		startDownloadBundle.putInt(AppDownloadService.DOWNLOAD_OPERATION, 
				AppDownloadService.OPERATION_PAUSE_CONTINUE_DOWNLOAD);
		startDownload.putExtras(startDownloadBundle);
		context.startService(startDownload);
	}
	
	/**
	 * 取消任务请求方法
	 * 
	 * @param context
	 * @param downloadData
	 */
	public static void cancelDownload(Context context, DownloadData downloadData) {
		Intent startDownload = new Intent(context, AppDownloadService.class);
		Bundle startDownloadBundle = new Bundle();
		startDownloadBundle.putParcelable(AppDownloadService.DOWNLOAD_DATA, downloadData);
		startDownloadBundle.putInt(AppDownloadService.DOWNLOAD_OPERATION, AppDownloadService.OPERATION_CANCLE_DOWNLOAD);
		startDownload.putExtras(startDownloadBundle);
		context.startService(startDownload);
	}

    /**
     * 关机暂停正在下载任务
     */
    public static void pauseAllDownloads() {
        if (downloaders != null && downloaders.size() > 0) {
            for (int key : downloaders.keySet()) {
                FileDownloader downloader = downloaders.get(key);
                downloader.shutdownPause();
            }
        }
    }
}
