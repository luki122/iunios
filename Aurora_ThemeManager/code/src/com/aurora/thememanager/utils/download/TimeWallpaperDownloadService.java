package com.aurora.thememanager.utils.download;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aurora.thememanager.entities.Theme;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

public class TimeWallpaperDownloadService extends Service {
private static final String TAG = "DownloadService";

public static final String DOWNLOAD_DATA = "download_data";

/**
 * 下载操作
 */
public static final String DOWNLOAD_OPERATION = "download_operation"; 
/**
 *  开始下载
 */
public static final int OPERATION_START_DOWNLOAD = 100; 
/**
 *  暂停下载
 */
public static final int OPERATION_PAUSE_DOWNLOAD = 101; 
/**
 *  继续下载
 */
public static final int OPERATION_CONTINUE_DOWNLOAD = 102; 
/**
 *  继续或暂停下载
 */
public static final int OPERATION_PAUSE_CONTINUE_DOWNLOAD = 103; 
/**
 *  取消下载
 */
public static final int OPERATION_CANCLE_DOWNLOAD = 104; 
/**
 * /网络改变
 */
public static final int OPERATION_NETWORK_CHANGE = 105; 
/**
 *  网络改变为手机网络需要暂停情况
 */
public static final int OPERATION_NETWORK_MOBILE_PAUSE = 106; 
/**
 *  网络改变为手机网络需要继续下载情况
 */
public static final int OPERATION_NETWORK_MOBILE_CONTINUE = 107; 
/**
 *  处理检查操作
 */
private static final int HANDEL_CHECK = 200; 

/**
 *  上下文对象
 */
private static Context sContext; 

/**
 *   正在下载的任务下载器
 */
private static Map<Integer, FileDownloader> sDownloaders;
/**
 * 当前操作的下载器
 */
private FileDownloader mCurrentDownloader; 
/**
 * 当前操作的下载数据
 */
private DownloadData mCurrentDownloadData; 
/**
 * 数据库操作对象
 */
private static DatabaseController mDownloadDaseController; 

/**
 * 是否正在下载
 */
private boolean mDownloading = false;
/**
 * 没有在下载时, 已发送广播的次数
 */
private int noDownloadingSend = 0; 
/**
 * 是否发送通知标识, 两秒一次
 */
private boolean notificationFlag = false;
private int notiId = 0x12345789;
private Notification mNotification;

private static List<DownloadInitListener> sInitListenerList;
private static List<DownloadUpdateListener> sUpdateListenerList;


@Override
public void onCreate() {
	super.onCreate();
	Log.i(TAG, "setvice onCreate");

	
	if (isNeedInitData()) {
		sContext = this;
		sDownloaders = new ConcurrentHashMap<Integer, FileDownloader>();
		mDownloadDaseController = DatabaseController.getController(this, DatabaseController.TYPE_TIME_WALLPAPER_DOWNLOAD);
		mDownloadDaseController.openDatabase();

		/*
		 * Service被创建时, 先从数据库找到已有任务, 并放入到列表中
		 */
		List<Integer> downloadIds = mDownloadDaseController.getAllId();
		for (int id : downloadIds) {
			DownloadData downloadData = mDownloadDaseController.getDownloadData(id);
			int status = mDownloadDaseController.getStatus(id);
			if (status < FileDownloader.STATUS_APPLY_WAIT) {
				DownloadCallback listener = new DownloadCallback() {
					@Override
					public void onDownload(int downloadDataId, int status, 
							long downloadSize, long fileSize) {
						/*
						 *  当下载完成时, 发送下载完成广播
						 */
						if (status == FileDownloader.STATUS_APPLY_WAIT|| (downloadSize == fileSize && fileSize != 0)) {
							Intent finish = new Intent(DownloadAction.ACTION_DOWNLOAD_BROADCAST);
							finish.putExtra(DownloadAction.KEY.KEY_DOWNLOADED_DATA_ID, downloadDataId);
							sContext.sendBroadcast(finish);
						}
					}
				};
				String dirPath = "";
				String dirFromDb = mDownloadDaseController.getFileDir(id);
				if (!TextUtils.isEmpty(dirFromDb)) {
					dirPath = dirFromDb;
				} else {
//					dirPath = FilePathUtil.getAPKFilePath(this);
				}
				FileDownloader downloader = new FileDownloader(this,downloadData, new File(dirPath), 
						listener, FileDownloader.TYPE_NORMAL);
				sDownloaders.put(downloadData.downloadId, downloader);
			}
		}
		
		/*
		 * 告知Service已经加载完成
		 */
		if (sInitListenerList != null) {
			for (DownloadInitListener listener : sInitListenerList) {
				listener.onFinishInit();
			}
			sInitListenerList.clear();
			sInitListenerList = null;
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
	/*
	 * 获取要操作的数据信息
	 */
	if (intent != null) {
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			mCurrentDownloadData = (DownloadData) bundle.get(DOWNLOAD_DATA);
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
	
	if (sInitListenerList != null) {
		sInitListenerList.clear();
	}
	sInitListenerList = null;
	
	mHandle.removeMessages(HANDEL_CHECK);
}

private Handler mHandle = new Handler() {
	@Override
	public void handleMessage(Message msg) {
		switch(msg.what) {
		case HANDEL_CHECK:
			notificationFlag = !notificationFlag;
			mDownloading = false;

			List<Integer> finishKey = new ArrayList<Integer>(); // 已完成任务的所有key列表

			for (int key : sDownloaders.keySet()) {
				FileDownloader downloader = sDownloaders.get(key);
				/*
				 *  如果有正在下载的任务, 则把标识设置为true
				 */
				int status = downloader.getStatus();
				if (status == FileDownloader.STATUS_DOWNLOADING
						|| status == FileDownloader.STATUS_CONNECTING
						|| status == FileDownloader.STATUS_WAIT
						|| status == FileDownloader.STATUS_CONNECT_RETRY) {
					mDownloading = true;
					noDownloadingSend = 0;
				}
				
				// 把已完成的任务key加入到已完成列表中
				if (downloader.getStatus() >= FileDownloader.STATUS_APPLY_WAIT) {
					finishKey.add(key);
				}
			}
			/*
			 * 对已完成的任务进行操作
			 */
			for (int key : finishKey) {
				sDownloaders.remove(key);
				
			}

			/*
			 *  发送更新广播
			 */
			if (mDownloading) {
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
				/*
				 * 调用更新服务进行更新操作
				 */
//				AutoUpdateService.continueAutoUpdate(context);
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
	/*
	 * 开始下载
	 */
	case OPERATION_START_DOWNLOAD:
		// 判断在列表中是否存在这个下载器, 若没有的话, 创建一个下载器
		Log.d(TAG, "downloadId:"+mCurrentDownloadData.downloadId);
		if (sDownloaders.containsKey(mCurrentDownloadData.downloadId)) {
			mCurrentDownloader = sDownloaders.get(mCurrentDownloadData.downloadId);
			download(mCurrentDownloader);
		} else {
			Theme theme = (Theme)mCurrentDownloadData;
			File dir = new File(FilePathUtil.getDownloadPath(theme.type));
			DownloadCallback listener = new DownloadCallback() {
				@Override
				public void onDownload(int downloadDataId, int status,
						long downloadSize, long fileSize) {
					    /*
					     * 当下载完成时, 发送下载完成广播
					     */
					if (status == FileDownloader.STATUS_APPLY_WAIT
							|| (downloadSize == fileSize && fileSize != 0)) {
						Intent finish = new Intent(DownloadAction.ACTION_DOWNLOAD_BROADCAST);
						finish.putExtra(DownloadAction.KEY.KEY_DOWNLOADED_DATA_ID, downloadDataId);
						sContext.sendBroadcast(finish);
					}
				}
			};
			FileDownloader fileDownloader = new FileDownloader(this,mCurrentDownloadData, dir, listener, FileDownloader.TYPE_NORMAL);
			mCurrentDownloader = fileDownloader;
			if (mCurrentDownloader.getStatus() < FileDownloader.STATUS_APPLY_WAIT) {
				sDownloaders.put(mCurrentDownloadData.downloadId, fileDownloader);
				download(mCurrentDownloader);
			}
		}
		break;
	/*
	 *  暂停下载
	 */
	case OPERATION_PAUSE_DOWNLOAD:
		mCurrentDownloader = sDownloaders.get(mCurrentDownloadData.downloadId);
		if (mCurrentDownloader != null) {
			mCurrentDownloader.pause();
		}
		break;
	/*
	 *  继续下载
	 */
	case OPERATION_CONTINUE_DOWNLOAD:
		mCurrentDownloader = sDownloaders.get(mCurrentDownloadData.downloadId);
		if (mCurrentDownloader != null) {
			download(mCurrentDownloader);
		}
		break;
	/*
	 *  暂停或继续下载
	 */
	case OPERATION_PAUSE_CONTINUE_DOWNLOAD:
		mCurrentDownloader = sDownloaders.get(mCurrentDownloadData.downloadId);
		if (mCurrentDownloader != null) {
			int status = mCurrentDownloader.getStatus();
			if (status == FileDownloader.STATUS_DOWNLOADING
					|| status == FileDownloader.STATUS_CONNECTING
					|| status == FileDownloader.STATUS_WAIT
					|| status == FileDownloader.STATUS_NO_NETWORK
					|| status == FileDownloader.STATUS_CONNECT_RETRY) {
				mCurrentDownloader.pause();
			} else {
				download(mCurrentDownloader);
			}
		}
		updateDownloadProgress();
		break;
	/*
	 *  取消下载
	 */
	case OPERATION_CANCLE_DOWNLOAD:
		mCurrentDownloader = sDownloaders.get(mCurrentDownloadData.downloadId);
		if (mCurrentDownloader != null) {
			mCurrentDownloader.cancel();
		} else {
			if (mCurrentDownloadData.fileName != null) {
				File file = new File(mCurrentDownloadData.fileDir, mCurrentDownloadData.fileName);
				file.delete();
			}
			mDownloadDaseController.delete(mCurrentDownloadData.downloadId);
		}
		updateDownloadProgress();
//		cancleNotify(currentDownloadData.downloadId);
		break;
	case OPERATION_NETWORK_CHANGE:
		if (NetWorkStatusUtils.isWifiNetwork(sContext)) {
			List<FileDownloader> list = new ArrayList<FileDownloader>();
			for (int key : sDownloaders.keySet()) {
				FileDownloader downloader = sDownloaders.get(key);
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
	
			for (int key : sDownloaders.keySet()) {
				FileDownloader downloader = sDownloaders.get(key);
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
		for (int key : sDownloaders.keySet()) {
			FileDownloader downloader = sDownloaders.get(key);
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
		for (int key : sDownloaders.keySet()) {
			FileDownloader downloader = sDownloaders.get(key);
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
			comparison = (int) (lhs.getDownloadData().downloadId - rhs
					.getDownloadData().downloadId);
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
					downloader.getDownloadData().downloadId)) {
		AutoUpdateService.getDownloaders().get(downloader.getDownloadData().downloadId).cancel();
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
private void sendDownloadingNotify() {}

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
	if (mDownloadDaseController == null || sDownloaders == null
			|| sContext == null) {
		return true;
	}
	return false;
}


public static DatabaseController getDownloadController() {
	return mDownloadDaseController;
}

public static Map<Integer, FileDownloader> getDownloaders(Context context) {
	if (null == sDownloaders) {
		sDownloaders = new ConcurrentHashMap<Integer, FileDownloader>();
		if (context.getApplicationContext() != null) {
			Intent i = new Intent(context.getApplicationContext(), TimeWallpaperDownloadService.class);
			context.getApplicationContext().startService(i);
		}
	}
	return sDownloaders;
}

/**
 * 刷新进度
 * 
 */
public static void updateDownloadProgress() {
	Log.i(TAG, "updateDownloadProgress()");
	if (sUpdateListenerList != null) {
		for (DownloadUpdateListener updateListener : sUpdateListenerList) {
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
	Log.d(TAG, "checkInit:"+isNeedInitData());
	if (isNeedInitData()) {
		if (sInitListenerList == null) {
			sInitListenerList = new ArrayList<DownloadInitListener>();
		}
		if (serviceListener != null) {
			sInitListenerList.add(serviceListener);
		}
		Intent initIntent = new Intent(context, TimeWallpaperDownloadService.class);
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
	if (sUpdateListenerList == null) {
		sUpdateListenerList = new ArrayList<DownloadUpdateListener>();
	}
	if (updateListener != null) {
		sUpdateListenerList.add(updateListener);
	}
}

/**
 * 取消刷新监听
 * 
 * @param updateListener
 */
public static void unRegisterUpdateListener(DownloadUpdateListener updateListener) {
	if (sUpdateListenerList != null && updateListener != null) {
		sUpdateListenerList.remove(updateListener);
	}
}

/**
 * 开始下载请求方法
 * 
 * @param context
 * @param downloadData
 */
public static void startDownload(Context context, DownloadData downloadData) {
	Intent startDownload = new Intent(context, TimeWallpaperDownloadService.class);
	Bundle startDownloadBundle = new Bundle();
	startDownloadBundle.putParcelable(TimeWallpaperDownloadService.DOWNLOAD_DATA, downloadData);
	startDownloadBundle.putInt(TimeWallpaperDownloadService.DOWNLOAD_OPERATION, 
			TimeWallpaperDownloadService.OPERATION_START_DOWNLOAD);
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
	Intent startDownload = new Intent(context, TimeWallpaperDownloadService.class);
	Bundle startDownloadBundle = new Bundle();
	startDownloadBundle.putParcelable(TimeWallpaperDownloadService.DOWNLOAD_DATA, downloadData);
	startDownloadBundle.putInt(TimeWallpaperDownloadService.DOWNLOAD_OPERATION, 
			TimeWallpaperDownloadService.OPERATION_PAUSE_CONTINUE_DOWNLOAD);
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
	Intent startDownload = new Intent(context, TimeWallpaperDownloadService.class);
	Bundle startDownloadBundle = new Bundle();
	startDownloadBundle.putParcelable(TimeWallpaperDownloadService.DOWNLOAD_DATA, downloadData);
	startDownloadBundle.putInt(TimeWallpaperDownloadService.DOWNLOAD_OPERATION, TimeWallpaperDownloadService.OPERATION_CANCLE_DOWNLOAD);
	startDownload.putExtras(startDownloadBundle);
	context.startService(startDownload);
}

/**
 * 关机暂停正在下载任务
 */
public static void pauseAllDownloads() {
    if (sDownloaders != null && sDownloaders.size() > 0) {
        for (int key : sDownloaders.keySet()) {
            FileDownloader downloader = sDownloaders.get(key);
            downloader.shutdownPause();
        }
    }
}}


