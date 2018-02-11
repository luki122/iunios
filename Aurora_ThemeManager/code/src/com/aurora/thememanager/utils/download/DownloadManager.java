package com.aurora.thememanager.utils.download;

import java.io.File;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.utils.SystemUtils;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.themehelper.ThemeManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Toast;
import aurora.app.AuroraAlertDialog;

import com.aurora.thememanager.R;

public class DownloadManager {
	
	private static final String TAG = "DownloadManager";
	
	private static Object sync = new Object();
	private static ThreadPoolExecutor threadPool = null;
	private static int corePoolSize = 3;
	private static BlockingQueue<Runnable> workQueue;
	private static RejectedExecutionHandler handler;
	
	private DownloadStatusCallback mCallBack;
	
	private Context mContext;
	
	private int mType = Theme.TYPE_THEME_PKG;

	public static ThreadPoolExecutor getThreadPoolExecutor() {
		synchronized (sync) {
			if (threadPool == null) {
				workQueue = new LinkedBlockingQueue<Runnable>();
				handler = new ThreadPoolExecutor.DiscardOldestPolicy();
				threadPool = new ThreadPoolExecutor(corePoolSize, corePoolSize, 1,
						TimeUnit.SECONDS, workQueue, handler);
			}
		}
		return threadPool;
	}

	
	
	
	
	
	
	public static void setCorePoolSize(int size) {
		corePoolSize = size;
		if (threadPool != null) {
			threadPool.setCorePoolSize(corePoolSize);
		}
	}

	public void setApplied(DownloadData data){/*
		Theme theme = (Theme)data;
		 DatabaseController dbController = null;
		 if(theme.type == Theme.TYPE_RINGTONG){
			 dbController = RingtongDownloadService.getDownloadController();
		 }else if(theme.type == Theme.TYPE_THEME_PKG){
			 dbController = DownloadService.getDownloadController();
		 }else if(theme.type == Theme.TYPE_TIME_WALLPAPER){
			 dbController = TimeWallpaperDownloadService.getDownloadController();
		 }else{
			 dbController = WallpaperDownloadService.getDownloadController();
		 }
		    if(dbController == null){
		    	dbController = DatabaseController.getController(mContext, DatabaseController.TYPE_DOWNLOAD);
		    	dbController.openDatabase();
		    }
		    dbController.setAppApplied(data.downloadId);
		    updateProgress(data);
	*/}
	
	public void setUnApplied(int dataId){
		 DatabaseController dbController = DownloadService.getDownloadController();
		    if(dbController == null){
		    	dbController = DatabaseController.getController(mContext, DatabaseController.TYPE_DOWNLOAD);
		    	dbController.openDatabase();
		    }
		    dbController.setUnAppApplied(dataId);
	}
	
	public void setCallBack(DownloadStatusCallback callback){
		this.mCallBack = callback;
	}
	
	public DownloadManager(Context context){
		mContext = context;
	}
	public void setType(int type){
		mType = type;
	}
	
	public void updateProgress(DownloadData data) {
		updateProgress(data, null);
	}
	
	public void updateProgress(DownloadData data, OnClickListener onClickListener) {
		// 检测是否使用
	    
		Theme theme = (Theme)data;
		int type = theme.type;
		setType(type);
		FileDownloader downloader = null;
		 DatabaseController dbController = null;
		 DownloadData tempData = null;
		 if(type == Theme.TYPE_RINGTONG){
			 dbController = RingtongDownloadService.getDownloadController();
			 downloader =  RingtongDownloadService.getDownloaders(mContext).get(data.downloadId);
		 }else if(type == Theme.TYPE_THEME_PKG){
			 dbController = DownloadService.getDownloadController();
			 downloader =  DownloadService.getDownloaders(mContext).get(data.downloadId);
		 }else if(type == Theme.TYPE_TIME_WALLPAPER){
			 dbController = TimeWallpaperDownloadService.getDownloadController();
			 downloader =  TimeWallpaperDownloadService.getDownloaders(mContext).get(data.downloadId);
		 }else{
			 dbController = WallpaperDownloadService.getDownloadController();
			 downloader =  WallpaperDownloadService.getDownloaders(mContext).get(data.downloadId);
		 }
		 boolean appied  = false;
		 if(dbController != null){
			 dbController.openDatabase();
			 appied  = dbController.getApplied(data.downloadId);
			 tempData = dbController.getDownloadData(data.downloadId);
			 
		 }
	    Log.d(TAG, "versionCode:"+data.versionCode+"   themeId:"+data.downloadId+ "status:"+" hasDownloader:"+(downloader != null));
		// 未使用的情况
		if (!appied) {
			
			
			// 如果下载器任务存在, 显示各状态信息
			Log.d(TAG, "hasDownloader:"+(downloader != null)+"  themeId:"+data.downloadId);
			if (downloader != null) {
				
				int status = downloader.getStatus();
				Log.d(TAG, "download status:"+status+"  themeId:"+data.downloadId);
				if (status == FileDownloader.STATUS_PAUSE
						||status == FileDownloader.STATUS_PAUSE_NEED_CONTINUE) {
					showOperationContinue( downloader);
				} else if (status == FileDownloader.STATUS_DOWNLOADING
						|| status == FileDownloader.STATUS_CONNECTING
						|| status == FileDownloader.STATUS_NO_NETWORK
						|| status == FileDownloader.STATUS_WAIT) {
					showOperationDownloading(downloader);
				} else if (status == FileDownloader.STATUS_FAIL) {
					showOperationRetry(downloader);
				} else if (status == FileDownloader.STATUS_APPLY_WAIT) {
					showWaitApply(data);
				} else {
					if (status < FileDownloader.STATUS_APPLY_WAIT) {
						showOperationDownload(data);
					}
				}
			} else { // 任务完成或者没有记录
				if (tempData == null  ) {
					showOperationDownload(data);
				} else {
					Log.d(TAG, "tempCode:"+tempData.versionCode+"  dataVersionCode:"+data.versionCode);
					if (tempData.versionCode== data.versionCode) {
						int status = tempData.status;
						if (status == FileDownloader.STATUS_APPLY_WAIT) {		// 等待安装
							if(dataFileExists(tempData)){
								showWaitApply(tempData);
							}else{
								showOperationDownload(data);
							}
						} else if (status == FileDownloader.STATUS_APPLING) {	// 安装中
							showAppling( tempData);
						} else if (status == FileDownloader.STATUS_APPLY_FAIL
								|| status == FileDownloader.STATUS_APPLIED) {	// 安装成功或者安装失败
							if (dataFileExists(tempData)) {	// 查看数据库中该任务状态是否为完成, 并且文件是存在的
								 boolean app  = dbController.getApplied(data.downloadId);
								if (!app) {
									showWaitApply(tempData);
								} else {
									showOperationApplied(tempData);
								}
							} else {
								showOperationDownload(data);
							}
						} else {	// 条件不符合则显示下载
							showOperationDownload(data);
						}
					} else {
						dbController.updateStatus(data.downloadId, FileDownloader.STATUS_UPDATE);
						showOperationUpdate(data, null);
					}
				}
			}
		} else {
			// 这里判断是否为最新版本
			DownloadData appliedData = dbController.getDownloadData(data.downloadId);
//			if (data.versionCode> appliedData.versionCode) { // 不是最新版本
				// 如果下载器任务存在, 显示各状态信息
			
				if (downloader != null) {
					int status = downloader.getStatus();
					if (status == FileDownloader.STATUS_PAUSE
							||status == FileDownloader.STATUS_PAUSE_NEED_CONTINUE) {
						showOperationContinue(downloader);
					} else if (status == FileDownloader.STATUS_DOWNLOADING
							|| status == FileDownloader.STATUS_CONNECTING
							|| status == FileDownloader.STATUS_NO_NETWORK
							|| status == FileDownloader.STATUS_WAIT) {
						showOperationDownloading( downloader);
					} else if (status == FileDownloader.STATUS_FAIL) {
						showOperationRetry(downloader);
					} else {
						if (status < FileDownloader.STATUS_APPLY_WAIT) {
							dbController.updateStatus(data.downloadId, FileDownloader.STATUS_UPDATE);
							showOperationUpdate( data, onClickListener);
						}
					}
				} else { // 任务完成或者没有记录
					  Log.d(TAG, "appied:"+appied+"   themeId:"+data.downloadId+ "status:"+tempData.status+" hasDownloader:"+(downloader != null));
					if (tempData == null) {
						showOperationDownload(data);
					} else {
						if (tempData.versionCode== data.versionCode) {
							int status = tempData.status;
							if (status == FileDownloader.STATUS_APPLY_WAIT ) {		// 等待安装
								if(dataFileExists(tempData)){
									if(appied){
										showOperationApplied(tempData);
									}else{
										showWaitApply(tempData);
									}
									
								}else{
									showOperationDownload(data);
								}
							} else if (status == FileDownloader.STATUS_APPLING) {	// 安装中
								showAppling( data);
							}else if (status == FileDownloader.STATUS_APPLY_FAIL
									|| status == FileDownloader.STATUS_APPLIED) {	// 安装成功或者安装失败
								
								if (dataFileExists(tempData)) {	// 查看数据库中该任务状态是否为完成, 并且文件是存在的
									if ( data.versionCode> tempData.versionCode) {
										showWaitApply(tempData);
									} else {
										showOperationApplied( tempData);
									}
								} else {
									dbController.updateStatus(data.downloadId, FileDownloader.STATUS_UPDATE);
									showOperationUpdate( data, onClickListener);
								}
							} else {	// 条件不符合则显示更新
								dbController.updateStatus(data.downloadId, FileDownloader.STATUS_UPDATE);
								showOperationUpdate( data, onClickListener);
							}
						} else {
							dbController.updateStatus(data.downloadId, FileDownloader.STATUS_UPDATE);
							showOperationUpdate( data, onClickListener);
						}
					}
				}
				/*} else { // 如果是最新版本
				showOperationApplied(appliedData);
			}*/
		}
		
	}
	
	private boolean dataFileExists(DownloadData data){
		String fileDir = data.fileDir;
		fileDir = fileDir == null ? "" : fileDir;
		String fileName = data.fileName;
		fileName = fileName == null ? "" : fileName;
		final File file = new File(fileDir, fileName);
		return file.exists();
	}
	
	

	private void showOperationUpdate(DownloadData data,
			OnClickListener onClickListener) {
		// TODO Auto-generated method stub
		
		if(mCallBack != null){
			mCallBack.showOperationUpdate(data, onClickListener);
		}
	}

	private synchronized void  showOperationApplied(DownloadData data) {
		// TODO Auto-generated method stub
		if (mCallBack != null) {
			mCallBack.showOperationApplied(data);
		}
	}

	private void showAppling(DownloadData data) {
		// TODO Auto-generated method stub
		if (mCallBack != null) {
			mCallBack.showAppling(data);
		}
	}

	private void showOperationDownload(DownloadData data) {
		// TODO Auto-generated method stub
		if (mCallBack != null) {
			mCallBack.showOperationDownload(data);
		}
	}

	private void showWaitApply(DownloadData data) {
		// TODO Auto-generated method stub
	
		
		if (mCallBack != null) {
			mCallBack.showWaitApply(data);
		}
	}

	private void showOperationRetry(FileDownloader downloader) {
		// TODO Auto-generated method stub
		long downloadSize = downloader.getDownloadSize();
		long fileSize = downloader.getFileSize();
		int progress = 0;
		if (fileSize != 0) {
			progress = (int) ((downloadSize * 1.0) / fileSize * 100);
		}
		if (mCallBack != null) {
			mCallBack.showOperationRetry(downloader,progress);
		}
	}

	private void showOperationDownloading(FileDownloader downloader) {
		// TODO Auto-generated method stub
		long downloadSize = downloader.getDownloadSize();
		long fileSize = downloader.getFileSize();
		int progress = 0;
		if (fileSize != 0) {
			progress = (int) ((downloadSize * 1.0) / fileSize * 100);
		}
		if (mCallBack != null) {
			mCallBack.showOperationDownloading(downloader,progress);
		}
	}

	private void showOperationContinue(FileDownloader downloader) {
		// TODO Auto-generated method stub
		long downloadSize = downloader.getDownloadSize();
		long fileSize = downloader.getFileSize();
		int progress = 0;
		if (fileSize != 0) {
			progress = (int) ((downloadSize * 1.0) / fileSize * 100);
		}
		if (mCallBack != null) {
			mCallBack.showOperationContinue(downloader,progress);
		}
	}
	
	public void doOperationContinue(final FileDownloader downloader,final Context context){
		if (!SystemUtils.isDownload(context)) {
			AuroraAlertDialog mWifiConDialog = new AuroraAlertDialog.Builder(
					context, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
					.setTitle(
							context.getResources().getString(
									R.string.dialog_prompt))
					.setMessage(
							context.getResources().getString(
									R.string.no_wifi_download_message))
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(
										DialogInterface dialog,
										int which) {
									
									SharedPreferences sp = PreferenceManager
											.getDefaultSharedPreferences(context);
									Editor ed = sp.edit();
									ed.putBoolean("wifi_download_key", false);
									ed.commit();
									continueDownload(downloader,context);
								}

							}).create();
			mWifiConDialog.show();

		} else if (!SystemUtils.hasNetwork()) {
			Toast.makeText(context, context.getString(R.string.no_network_download_toast), Toast.LENGTH_SHORT).show();
		} else {
			continueDownload(downloader,context);
		}
		
	}
	
	private void continueDownload(final FileDownloader downloader,final Context context){
		int status = downloader.getStatus();
		if (status == FileDownloader.STATUS_PAUSE_NEED_CONTINUE) {
			AuroraAlertDialog mWifiConDialog = new AuroraAlertDialog.Builder(
					context, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
					.setTitle(context.getResources().getString(R.string.dialog_prompt))
					.setMessage(context.getResources().getString(
									R.string.downloadman_continue_download_by_mobile))
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										DialogInterface dialog,
										int which) {
									
									
									pauseOrContinue(context, downloader);
								}

							}).create();
			mWifiConDialog.show();
		} else {

			pauseOrContinue(context, downloader);
			
		}
	}
	
	
	
	private void pauseOrContinue(Context context,FileDownloader downloader){

		switch (mType) {
		case Theme.TYPE_RINGTONG:
			RingtongDownloadService.pauseOrContinueDownload(context,downloader.getDownloadData());
			break;
		case Theme.TYPE_THEME_PKG:
			DownloadService.pauseOrContinueDownload(context,downloader.getDownloadData());
			break;
		case Theme.TYPE_TIME_WALLPAPER:
			TimeWallpaperDownloadService.pauseOrContinueDownload(context,downloader.getDownloadData());
			break;
		case Theme.TYPE_WALLPAPER:
			WallpaperDownloadService.pauseOrContinueDownload(context,downloader.getDownloadData());
			break;

		default:
			break;
		}
	}
	
	
	
	

}
