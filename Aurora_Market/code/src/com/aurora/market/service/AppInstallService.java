package com.aurora.market.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.aurora.market.db.AppDownloadDao;
import com.aurora.market.db.AutoUpdateDao;
import com.aurora.market.install.AppInstall;
import com.aurora.market.install.InstallNotification;
import com.aurora.market.model.DownloadData;

public class AppInstallService extends Service {

	private static final String TAG = "AppInstallService";
	
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_AUTO_UPDATE = 1;

	private static Context context; // 上下文对象

	private static Map<Integer, AppInstall> installs; // 正在安装的任务
	public static final String DOWNLOAD_DATA = "download_data";
	public static final String TYPE = "type";
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "oncreate");
		if (isNeedInitData()) {
			context = this;
			installs = new ConcurrentHashMap<Integer, AppInstall>();
			InstallNotification.init(context);
			AppDownloadDao appDownloadDao = new AppDownloadDao(this);
			appDownloadDao.openDatabase();
			// Service被创建时, 先从数据库找到已有任务, 并放入到列表中
			List<DownloadData> appIds = appDownloadDao.getUninstallApp();
			appDownloadDao.closeDatabase();
			for (DownloadData downloadData : appIds) {
				Log.i(TAG, "the uninstall id="+downloadData.getApkId());
				AppInstall install = new AppInstall(downloadData, context, TYPE_NORMAL);
				installs.put(downloadData.getApkId(), install);
				install(install);
			}
			
			AutoUpdateDao autoUpdateDao = new AutoUpdateDao(this);
			autoUpdateDao.openDatabase();
			// Service被创建时, 先从数据库找到已有任务, 并放入到列表中
			List<DownloadData> appAuto = autoUpdateDao.getUninstallApp();
			autoUpdateDao.closeDatabase();
			for (DownloadData downloadData : appAuto) {
				AppInstall install = new AppInstall(downloadData, context, TYPE_AUTO_UPDATE);
				installs.put(downloadData.getApkId(), install);
				install(install);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 获取要操作的数据信息
		Log.i(TAG, "onStartCommand");
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				DownloadData currentDownloadData = (DownloadData) bundle
						.get(DOWNLOAD_DATA);
				if(null == currentDownloadData)
				{
					Log.i(TAG, "onStartCommand the downloaddata is null");
					return super.onStartCommand(intent, flags, startId);
				}
				int type = bundle.getInt(TYPE, TYPE_NORMAL);
				if (installs.containsKey(currentDownloadData.getApkId())) {
					AppInstall currentInstall = installs
							.get(currentDownloadData.getApkId());
					install(currentInstall);
				} else {

					/*DownloadData downloadData = AppDownloadService
							.getAppDownloadDao().getDownloadData(
									currentDownloadData.getApkId());*/

					AppInstall install = new AppInstall(currentDownloadData, this, type);
					installs.put(currentDownloadData.getApkId(), install);
					install(install);

				}

			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	/**
	 * 开始下载请求方法
	 * 
	 * @param context
	 * @param downloadData
	 */
	public static void startInstall(Context context, DownloadData downloadData, int type) {
		Intent startDownload = new Intent(context, AppInstallService.class);
		Bundle startDownloadBundle = new Bundle();
		startDownloadBundle.putParcelable(AppDownloadService.DOWNLOAD_DATA, downloadData);
		startDownloadBundle.putInt(TYPE, type);
		startDownload.putExtras(startDownloadBundle);
		context.startService(startDownload);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	/**
	 * 安装
	 * 
	 * @param downloader
	 */
	private void install(AppInstall install) {
		// 开始安装
		install.InstallApp();
	}
	
	/**
	 * 检查Service数据是否构建完成
	 * 
	 * @param context
	 * @param serviceListener
	 */
	public static void checkInit(Context context) {
		Log.i(TAG, "checkInit");
		if (isNeedInitData()) {
			Log.i(TAG, "checkInit1");
			Intent initIntent = new Intent(context, AppInstallService.class);
			context.startService(initIntent);
		} 
	}

	public static Map<Integer, AppInstall> getInstalls() {
		return installs;
	}
	
	/**
	 * 获取是否需要重新构建Service的数据(静态变量可能会被回收)
	 * 
	 * @return
	 */
	private static boolean isNeedInitData() {
		if (installs == null || context == null) {
			return true;
		}
		return false;
	}

}
