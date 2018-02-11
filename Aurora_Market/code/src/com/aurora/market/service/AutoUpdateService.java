package com.aurora.market.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.KeyguardManager;
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

import com.aurora.market.activity.setting.UpdateSettingsPreferenceActivity;
import com.aurora.market.db.AutoUpdateDao;
import com.aurora.market.download.DownloadStatusListener;
import com.aurora.market.download.FileDownloader;
import com.aurora.market.download.FilePathUtil;
import com.aurora.market.model.DownloadData;
import com.aurora.market.util.DataFromUtils;
import com.aurora.market.util.SystemUtils;

public class AutoUpdateService extends Service {
	
	public static final String TAG = "AutoUpdateService";
	
	public static final String DOWNLOAD_DATA = "download_data";
	
	public static final String DOWNLOAD_OPERATION = "download_operation"; // 下载操作
	
	public static final int OPERATION_AUTO_UPDATE = 97;	// 自动更新
	public static final int OPERATION_CONTIUNE_AUTO_UPDATE = 98; // 继续自动更新
	public static final int OPERATION_STOP_AUTO_UPDATE = 99;
	
	public static final int OPERATION_START_DOWNLOAD = 100; // 开始下载
	public static final int OPERATION_PAUSE_DOWNLOAD = 101; // 暂停下载
	public static final int OPERATION_CONTINUE_DOWNLOAD = 102; // 继续下载
	public static final int OPERATION_PAUSE_CONTINUE_DOWNLOAD = 103; // 继续或暂停下载
	public static final int OPERATION_CANCLE_DOWNLOAD = 104; // 取消下载
	public static final int OPERATION_NETWORK_CHANGE = 105; // 网络改变
	
	private static final int HANDEL_CHECK = 200; // 处理检查操作
	private static final int HANDEL_AUTO_UPDATE = 201;	// 处理自动更新
	private static final int HANDEL_CLEAR = 202;
	
	private static Context context; // 上下文对象
	
	private static Map<Integer, FileDownloader> downloaders; // 正在下载的任务下载器
	private FileDownloader currentDownloader; // 当前操作的下载器
	private DownloadData currentDownloadData; // 当前操作的下载数据
	private static AutoUpdateDao autoUpdateDao; // 数据库操作对象
	private MyBroadcastReciver broadcastReceiver;
	private boolean downloading = false; // 是否正在下载
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if (isNeedInitData()) {
			context = this;
			downloaders = new ConcurrentHashMap<Integer, FileDownloader>();
			autoUpdateDao = new AutoUpdateDao(this);
			autoUpdateDao.openDatabase();

			// Service被创建时, 先从数据库找到已有任务, 并放入到列表中
			List<Integer> appIds = autoUpdateDao.getAllappId();
			for (int appId : appIds) {
				DownloadData downloadData = autoUpdateDao.getDownloadData(appId);
				int status = autoUpdateDao.getStatus(appId);
				if (status < FileDownloader.STATUS_INSTALL_WAIT) {
					DownloadStatusListener listener = new DownloadStatusListener() {
						@Override
						public void onDownload(int appId, int status, 
								long downloadSize, long fileSize) {
							// 当下载完成时, 发送下载完成广播
							if (status == FileDownloader.STATUS_INSTALL_WAIT
									|| (downloadSize == fileSize && fileSize != 0)) {
								
								AppInstallService.startInstall(context,
										autoUpdateDao.getDownloadData(appId), AppInstallService.TYPE_AUTO_UPDATE);
							}
						}
					};
					String dirPath = "";
					String dirFromDb = autoUpdateDao.getFileDir(appId);
					if (!TextUtils.isEmpty(dirFromDb)) {
						dirPath = dirFromDb;
					} else {
						dirPath = FilePathUtil.getAutoUpdateFilePath(context);
					}
					FileDownloader downloader = new FileDownloader(downloadData, new File(dirPath), 
							listener, FileDownloader.TYPE_AUTO_UPDATE);
					downloaders.put(downloadData.getApkId(), downloader);
				}
			}
		}
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		broadcastReceiver = new MyBroadcastReciver();
		this.registerReceiver(broadcastReceiver, intentFilter);
		mHandle.sendEmptyMessageDelayed(HANDEL_CHECK, 5000);
	}
	private class MyBroadcastReciver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i(TAG, "zhangwei the action="+action);
			if(action.equals(Intent.ACTION_SCREEN_ON))
			{
				AutoUpdateService.pauseAutoUpdate();
			}
			else if(action.equals(Intent.ACTION_SCREEN_OFF))
			{
				AutoUpdateService.continueAutoUpdate(context);
			}
		}

	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(broadcastReceiver);
		mHandle.removeMessages(HANDEL_CHECK);
	}

	private Handler mHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case HANDEL_CHECK:
				Log.i(TAG, "AutoUpdateService HANDEL_CHECK");
				downloading = false;

				List<Integer> finishKey = new ArrayList<Integer>(); // 已完成任务的所有key列表

				if (downloaders != null) {
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
				}

				// 发送更新广播
				if (!downloading) {
					//stopSelf();
					return;
				}

				mHandle.sendEmptyMessageDelayed(HANDEL_CHECK, 5000);
				break;
			case HANDEL_AUTO_UPDATE:
				Log.i(TAG, "HANDEL_AUTO_UPDATE");
				List<DownloadData> udateList = (List<DownloadData>) msg.obj;
				
				if (null == downloaders) {
					Log.i(TAG, "HANDEL_AUTO_UPDATE downloaders null");
					return;
				}
				
				// 先删除已存在但已不在应用更新列表中的任务
				for (int key : downloaders.keySet()) {
					FileDownloader downloader = downloaders.get(key);
					boolean exists = false;
					for (DownloadData data : udateList) {
						if (downloader.getDownloadData().getApkId() == data.getApkId()) {
							exists = true;
							break;
						}
					}
					if (!exists) {
						downloader.cancel();
						downloaders.remove(downloader);
					}
				}
				
				for (DownloadData data : udateList) {
					// 下载模块没有此任务
					if (!AppDownloadService.getDownloaders().containsKey(data.getApkId())) {
						if (!downloaders.containsKey(data.getApkId())) {
							Log.i(TAG, data.toString());
							File dir = new File(FilePathUtil.getAutoUpdateFilePath(context));
							DownloadStatusListener listener = new DownloadStatusListener() {
								@Override
								public void onDownload(int appId, int status,
										long downloadSize, long fileSize) {
									if (status == FileDownloader.STATUS_INSTALL_WAIT
											|| (downloadSize == fileSize && fileSize != 0)) {
										
										AppInstallService.startInstall(context,
												autoUpdateDao.getDownloadData(appId), AppInstallService.TYPE_AUTO_UPDATE);
									}
								}
							};
							FileDownloader fileDownloader = new FileDownloader(data, dir, listener, FileDownloader.TYPE_AUTO_UPDATE);
							downloaders.put(data.getApkId(), fileDownloader);
						}
	 				}
				}
			/*	for (int key : downloaders.keySet()) {
					FileDownloader downloader = downloaders.get(key);
					download(downloader);
				}*/
				break;
			case HANDEL_CLEAR:
				//stopSelf();
//				downloaders = null;
//				autoUpdateDao = null;
//				context = null;
				break;
			}
		}
	};

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
	
	/**
	 * 处理指令
	 * @param operation
	 */
	private void handleOperation(int operation) {
		switch (operation) {
		// 自动更新
		case OPERATION_AUTO_UPDATE:
			new Thread() {
				@Override
				public void run() {
					Log.i(TAG, "run thread");
					DataFromUtils dataFromUtils = new DataFromUtils();
					List<DownloadData> udateList = dataFromUtils.getUpdateData(context);
					if (udateList != null && udateList.size() > 0) {
						mHandle.sendMessage(mHandle.obtainMessage(HANDEL_AUTO_UPDATE, udateList));
					}
				}
			}.start();
			
			break;
		case OPERATION_CONTIUNE_AUTO_UPDATE:
			continueAutoUpdate();
			break;
		case OPERATION_STOP_AUTO_UPDATE:
			mHandle.sendEmptyMessage(HANDEL_CLEAR);
			break;
		// 开始下载
		case OPERATION_START_DOWNLOAD:
			// 判断在列表中是否存在这个下载器, 若没有的话, 创建一个下载器
			if (downloaders.containsKey(currentDownloadData.getApkId())) {
				currentDownloader = downloaders.get(currentDownloadData
						.getApkId());
				download(currentDownloader);
			} else {
				File dir = new File(FilePathUtil.getAutoUpdateFilePath(this));
				DownloadStatusListener listener = new DownloadStatusListener() {
					@Override
					public void onDownload(int appId, int status,
							long downloadSize, long fileSize) {
//						// 当下载完成时, 发送下载完成广播
						if (status == FileDownloader.STATUS_INSTALL_WAIT
								|| (downloadSize == fileSize && fileSize != 0)) {
							
							AppInstallService.startInstall(context,
									autoUpdateDao.getDownloadData(appId), AppInstallService.TYPE_AUTO_UPDATE);
						}
					}
				};
				FileDownloader fileDownloader = new FileDownloader(currentDownloadData, dir, 
						listener, FileDownloader.TYPE_AUTO_UPDATE);
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
			break;
		// 取消下载
		case OPERATION_CANCLE_DOWNLOAD:
			currentDownloader = downloaders.get(currentDownloadData.getApkId());
			if (currentDownloader != null) {
				currentDownloader.cancel();
			} else {
				File file = new File(currentDownloadData.getFileDir(), currentDownloadData.getFileName());
				file.delete();
				autoUpdateDao.delete(currentDownloadData.getApkId());
			}
			break;
		case OPERATION_NETWORK_CHANGE:
			if (SystemUtils.hasNetwork()) {
				List<FileDownloader> list = new ArrayList<FileDownloader>();
				for (int key : downloaders.keySet()) {
					FileDownloader downloader = downloaders.get(key);
					list.add(downloader);
				}
				sortList(list);
				for (FileDownloader downloader : list) {
					// 找到网络错误状态的任务, 开始进行下载
					if (downloader.getStatus() == FileDownloader.STATUS_NO_NETWORK) {
						download(downloader);
					}
				}
			} else { // 网络变为不可用时
				for (int key : downloaders.keySet()) {
					FileDownloader downloader = downloaders.get(key);
					int status = downloader.getStatus();
					if (status == FileDownloader.STATUS_WAIT) {
						downloader.setStatus(FileDownloader.STATUS_NO_NETWORK);
					}
				}
				for (int key : downloaders.keySet()) {
					FileDownloader downloader = downloaders.get(key);
					int status = downloader.getStatus();
					if (status == FileDownloader.STATUS_CONNECTING
							|| status == FileDownloader.STATUS_DOWNLOADING
							|| status == FileDownloader.STATUS_WAIT) {
						downloader.setStatus(FileDownloader.STATUS_NO_NETWORK);
					}
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
	 * 获取是否需要重新构建Service的数据(静态变量可能会被回收)
	 * 
	 * @return
	 */
	private static boolean isNeedInitData() {
		if (autoUpdateDao == null || downloaders == null
				|| context == null) {
			return true;
		}
		return false;
	}
	
	/**
	 * 继续自动更新
	 * 
	 * @return
	 */
	private void continueAutoUpdate() {
		if (downloaders != null && downloaders.size() > 0) {
			List<FileDownloader> list = new ArrayList<FileDownloader>();
			for (int key : downloaders.keySet()) {
				FileDownloader downloader = downloaders.get(key);
				list.add(downloader);
			}
			sortList(list);
			
			for (FileDownloader downloader : list) {
				download(downloader);
			}
		}
	}
	
//=================================已下为public方法=================================//
	
	/**
	 * 检查Service数据是否构建完成
	 * 
	 * @param context
	 * @param serviceListener
	 */
	public static void checkInit(Context context) {
		if (isNeedInitData()) {
		
			Intent initIntent = new Intent(context, AutoUpdateService.class);
			context.startService(initIntent);
		} 
	}
	
	public static AutoUpdateDao getAutoUpdateDao() {
		return autoUpdateDao;
	}
	
	public static Map<Integer, FileDownloader> getDownloaders() {
		return downloaders;
	}
	
	public static void startAutoUpdate(Context context,int type) {
		Log.i(TAG, "run startAutoUpdate()");
		
		if((type == 0) && UpdateSettingsPreferenceActivity.getPreferenceValue(context, 
				UpdateSettingsPreferenceActivity.WIFI_AUTO_UPGRADE_KEY) && SystemUtils.isWifiNetwork(context))
		{
			Intent startDownload = new Intent(context, AutoUpdateService.class);
			Bundle startDownloadBundle = new Bundle();
			startDownloadBundle.putInt(AutoUpdateService.DOWNLOAD_OPERATION, 
					AutoUpdateService.OPERATION_AUTO_UPDATE);
			startDownload.putExtras(startDownloadBundle);
			context.startService(startDownload);
		
		}
		else if((type == 1) && UpdateSettingsPreferenceActivity.getPreferenceValue(context, 
				UpdateSettingsPreferenceActivity.WIFI_AUTO_UPGRADE_KEY))
		{
				Intent startDownload = new Intent(context, AutoUpdateService.class);
				Bundle startDownloadBundle = new Bundle();
				startDownloadBundle.putInt(AutoUpdateService.DOWNLOAD_OPERATION, 
						AutoUpdateService.OPERATION_AUTO_UPDATE);
				startDownload.putExtras(startDownloadBundle);
				context.startService(startDownload);
		}
		
	}
	
	public static void pauseAutoUpdate() {
		Log.i(TAG, "run pauseAutoUpdate()");
		
		if (downloaders != null && downloaders.size() > 0) {
			for (int key : downloaders.keySet()) {
				FileDownloader downloader = downloaders.get(key);
				downloader.pause();
			}
		}
	}
	
	public static void continueAutoUpdate(Context context) {
		Log.i(TAG, "run continueAutoUpdate()");
		
		KeyguardManager mKeyguardManager = (KeyguardManager)context.getSystemService(KEYGUARD_SERVICE);
		 
		boolean ifKeyG = mKeyguardManager.inKeyguardRestrictedInputMode();
		
		if (UpdateSettingsPreferenceActivity.getPreferenceValue(context, 
				UpdateSettingsPreferenceActivity.WIFI_AUTO_UPGRADE_KEY)
				&& SystemUtils.isWifiNetwork(context) && (ifKeyG)) {
			Log.i(TAG, "run continueAutoUpdate()1");
			Intent startDownload = new Intent(context, AutoUpdateService.class);
			Bundle startDownloadBundle = new Bundle();
			startDownloadBundle.putInt(AutoUpdateService.DOWNLOAD_OPERATION, 
					AutoUpdateService.OPERATION_CONTIUNE_AUTO_UPDATE);
			startDownload.putExtras(startDownloadBundle);
			context.startService(startDownload);
		}
		
	}
	
	public static void stopAutoUpdate(Context context) {
		Log.i(TAG, "run stopAutoUpdate()");
		
		if (downloaders != null && downloaders.size() > 0) {
			for (int key : downloaders.keySet()) {
				FileDownloader downloader = downloaders.get(key);
				downloader.cancel();
			}
		}
		
		Intent startDownload = new Intent(context, AutoUpdateService.class);
		Bundle startDownloadBundle = new Bundle();
		startDownloadBundle.putInt(AutoUpdateService.DOWNLOAD_OPERATION, 
				AutoUpdateService.OPERATION_STOP_AUTO_UPDATE);
		startDownload.putExtras(startDownloadBundle);
		context.startService(startDownload);
	}
	
	/**
	 * 开始下载请求方法
	 * 
	 * @param context
	 * @param downloadData
	 */
	public static void startDownload(Context context, DownloadData downloadData) {
		Intent startDownload = new Intent(context, AutoUpdateService.class);
		Bundle startDownloadBundle = new Bundle();
		startDownloadBundle.putParcelable(AutoUpdateService.DOWNLOAD_DATA, downloadData);
		startDownloadBundle.putInt(AutoUpdateService.DOWNLOAD_OPERATION, 
				AutoUpdateService.OPERATION_START_DOWNLOAD);
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
		Intent startDownload = new Intent(context, AutoUpdateService.class);
		Bundle startDownloadBundle = new Bundle();
		startDownloadBundle.putParcelable(AutoUpdateService.DOWNLOAD_DATA, downloadData);
		startDownloadBundle.putInt(AutoUpdateService.DOWNLOAD_OPERATION, 
				AutoUpdateService.OPERATION_PAUSE_CONTINUE_DOWNLOAD);
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
		Intent startDownload = new Intent(context, AutoUpdateService.class);
		Bundle startDownloadBundle = new Bundle();
		startDownloadBundle.putParcelable(AutoUpdateService.DOWNLOAD_DATA, downloadData);
		startDownloadBundle.putInt(AutoUpdateService.DOWNLOAD_OPERATION, AutoUpdateService.OPERATION_CANCLE_DOWNLOAD);
		startDownload.putExtras(startDownloadBundle);
		context.startService(startDownload);
	}
	
	/**
	* @Title: setDownloadList
	* @Description: 设置下载任务
	* @param @param list
	* @return void
	* @throws
	 */
	public static void setDownloadList(List<DownloadData> list) {
		Log.i(TAG, "setDownloadList run, size: " + list.size());
		
		List<DownloadData> udateList = list;
		
		if (null == downloaders) {
			Log.i(TAG, "HANDEL_AUTO_UPDATE downloaders null");
			downloaders = new ConcurrentHashMap<Integer, FileDownloader>();
		}
		
		// 先删除已存在但已不在应用更新列表中的任务
		for (int key : downloaders.keySet()) {
			FileDownloader downloader = downloaders.get(key);
			boolean exists = false;
			for (DownloadData data : udateList) {
				if (downloader.getDownloadData().getApkId() == data.getApkId()) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				downloader.cancel();
				downloaders.remove(downloader);
			}
		}
		
		for (DownloadData data : udateList) {
			// 下载模块没有此任务
			if (!AppDownloadService.getDownloaders().containsKey(data.getApkId())) {
				if (!downloaders.containsKey(data.getApkId())) {
					Log.i(TAG, data.toString());
					File dir = new File(FilePathUtil.getAutoUpdateFilePath(context));
					DownloadStatusListener listener = new DownloadStatusListener() {
						@Override
						public void onDownload(int appId, int status,
								long downloadSize, long fileSize) {
							if (status == FileDownloader.STATUS_INSTALL_WAIT
									|| (downloadSize == fileSize && fileSize != 0)) {
								
								AppInstallService.startInstall(context,
										autoUpdateDao.getDownloadData(appId), AppInstallService.TYPE_AUTO_UPDATE);
							}
						}
					};
					FileDownloader fileDownloader = new FileDownloader(data, dir, listener, FileDownloader.TYPE_AUTO_UPDATE);
					downloaders.put(data.getApkId(), fileDownloader);
				}
			}
		}
	}
	
}
