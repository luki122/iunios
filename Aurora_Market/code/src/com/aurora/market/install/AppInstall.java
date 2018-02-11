package com.aurora.market.install;

import java.io.File;
import java.util.concurrent.ThreadPoolExecutor;

import android.content.Context;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.aurora.market.R;
import com.aurora.market.download.FileDownloader;
import com.aurora.market.model.DownloadData;
import com.aurora.market.service.AppDownloadService;
import com.aurora.market.service.AppInstallService;
import com.aurora.market.service.AutoUpdateService;
import com.aurora.market.util.SystemUtils;

public class AppInstall implements Runnable {

	public static final String TAG = "AppInstall";
	public static final int HANDLE_UPDATE = 100;
	public static final int HANDLE_SHOW_TOAST = 101;

	private DownloadData downloadData;
	private int type;
	private Context mContext;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case HANDLE_UPDATE:
				AppDownloadService.updateDownloadProgress();
				break;
			case HANDLE_SHOW_TOAST:
				Toast.makeText(mContext, (String)msg.obj, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	/**
	 * 构建文件下载器
	 * 
	 * @param downloadData
	 *            下载信息
	 * @param fileSaveDir
	 *            文件保存目录
	 */
	public AppInstall(DownloadData downloadData, Context mContext, int type) {
		init(downloadData, mContext, type);
	}

	@Override
	public void run() {
		/*
		 * Intent finish = new Intent( Globals.INSTALL_BROADCAST);
		 * finish.putExtra("packegename", downloadData.getPackageName());
		 * mContext.sendBroadcast(finish);
		 */
		Log.i(TAG, "run0");
		if ((null != downloadData)
				&& (downloadData.getStatus() == FileDownloader.STATUS_INSTALL_WAIT)) {
			Log.i(TAG, "run1");
			
			
			if (type == AppInstallService.TYPE_NORMAL) {
				if(null == AppDownloadService.getAppDownloadDao())
					return;
				AppDownloadService.getAppDownloadDao().updateStatus(
						downloadData.getApkId(), FileDownloader.STATUS_INSTALLING);
				handler.sendEmptyMessage(HANDLE_UPDATE);
			} else {
				if(null == AutoUpdateService.getAutoUpdateDao())
					return;
				AutoUpdateService.getAutoUpdateDao().updateStatus(
						downloadData.getApkId(), FileDownloader.STATUS_INSTALLING);
			}
			downloadData.setStatus(FileDownloader.STATUS_INSTALLING);
		}

		startInstall();
	}

	/**
	 * 构建FileDownloader信息
	 * 
	 * @param downloadData
	 * @param fileSaveDir
	 * @param listener
	 */
	private void init(DownloadData downloadData, Context mContext, int type) {
		Log.i(TAG,
				"Install init: id->" + downloadData.getApkId() + " name->"
						+ downloadData.getApkName() + " packageName->"
						+ downloadData.getPackageName() + " status->"
						+ downloadData.getStatus());

		this.downloadData = downloadData;
		this.mContext = mContext;
		this.type = type;
	}

	/**
	 * 开始安装
	 * 
	 */
	private void startInstall() {
		InstallNotification.sendInstallingNotify();
		Log.i(TAG, downloadData.getApkName() + "->startInstall()");

		String fileDir = downloadData.getFileDir();
		fileDir = fileDir == null ? "" : fileDir;
		String fileName = downloadData.getFileName();
		fileName = fileName == null ? "" : fileName;

		Log.i(TAG, "fileName=" + fileName + "fileName=" + fileName);
		final File file = new File(fileDir, fileName);
		PackageInstallObserver observer = new PackageInstallObserver();

		SystemUtils.intstallApp(mContext, downloadData.getPackageName(), file,
				observer);
	}

	

	class PackageInstallObserver extends IPackageInstallObserver.Stub {
		public void packageInstalled(String packageName, int returnCode) {

			Log.i(TAG, "callback PackageInstallObserver the returnCode="
					+ returnCode);
			if ((null != downloadData)
					&& (downloadData.getStatus() == FileDownloader.STATUS_INSTALLING)) {
				
				if (type == AppInstallService.TYPE_NORMAL) {
					AppInstallService.getInstalls().keySet().remove(downloadData.getApkId());
					if (AppInstallService.getInstalls().size() == 0)
						InstallNotification.cancleInsatllingNotify();
					
					if (returnCode != PackageManager.INSTALL_SUCCEEDED) {
						downloadData.setStatus(FileDownloader.STATUS_INSTALLFAILED);
						AppDownloadService.getAppDownloadDao().updateStatus(
								downloadData.getApkId(), FileDownloader.STATUS_INSTALLFAILED);
						if(!InstallNotification.install_failed.contains(downloadData.getApkName()))
						{
							InstallNotification.install_failed.add(downloadData.getApkName());
							InstallNotification.sendInstallFailedNotify(downloadData.getApkName(),downloadData.getPackageName());
						}
						
						String msg = downloadData.getApkName() + mContext.getString(R.string.downloadman_install_failed);
						handler.sendMessage(handler.obtainMessage(HANDLE_SHOW_TOAST, msg));
					} else {
						if (!SystemUtils.isHold(mContext)) {
							String fileName = downloadData.getFileDir()+"/"+downloadData.getFileName();
							if(!TextUtils.isEmpty(fileName))
							{
								File file = new File(fileName);
								file.delete();
							}
						}
						downloadData.setStatus(FileDownloader.STATUS_INSTALLED);
						AppDownloadService.getAppDownloadDao().updateStatus(
								downloadData.getApkId(),
								FileDownloader.STATUS_INSTALLED);
						if(!InstallNotification.install_success.contains(downloadData.getApkName()))
						{
							InstallNotification.install_success.add(downloadData.getApkName());
							InstallNotification.sendInstalledNotify(downloadData.getApkName(),downloadData.getPackageName());
						}
					}
				} else {	// 自动更新的安装
					AppInstallService.getInstalls().remove(downloadData.getApkId());
					if (AppInstallService.getInstalls().size() == 0)
						InstallNotification.cancleInsatllingNotify();
					
					if (returnCode != PackageManager.INSTALL_SUCCEEDED) {
						downloadData.setStatus(FileDownloader.STATUS_INSTALLFAILED);
						AutoUpdateService.getAutoUpdateDao().updateStatus(
								downloadData.getApkId(), FileDownloader.STATUS_INSTALLFAILED);
						
						AutoUpdateService.cancelDownload(mContext, downloadData);
					} else {
						String fileName = downloadData.getFileDir()+downloadData.getFileName();
						if(!TextUtils.isEmpty(fileName))
						{
							File file = new File(fileName);
							file.delete();
						}
						downloadData.setStatus(FileDownloader.STATUS_INSTALLED);
						AutoUpdateService.getAutoUpdateDao().updateStatus(
								downloadData.getApkId(),
								FileDownloader.STATUS_INSTALLED);
						
						InstallNotification.install_success_auto.add(downloadData.getApkName());
						InstallNotification.sendAutoUpdateInstalledNotify(downloadData.getApkName());
					}
				}
				
				handler.sendEmptyMessage(HANDLE_UPDATE);
			}

			InstallNotification.sendInstallingNotify();
		}
	}

	/**
	 * 获取DownloadData
	 * 
	 * @return
	 */
	public DownloadData getDownloadData() {
		return downloadData;
	}

	// ============对外控制方法开始=============//

	/**
	 * 下载文件
	 * 
	 */
	public void InstallApp() {
		ThreadPoolExecutor threadPool = AppInstallManage
				.getThreadPoolExecutor();
		threadPool.execute(this);
	}

}
