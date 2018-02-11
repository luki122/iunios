package com.aurora.thememanager.utils.download;

import android.view.View.OnClickListener;

/**
 * 下载状态监听 
 */
public interface DownloadStatusCallback {
	
	/**
	 * 显示更新状态
	 * @param data
	 * @param onClickListener
	 */
	public void showOperationUpdate(DownloadData data,
			OnClickListener onClickListener);

	/**
	 * 显示已应用
	 * @param data
	 */
	public void showOperationApplied(DownloadData data);

	/**
	 * 显示正在应用
	 * @param data
	 */
	public void showAppling(DownloadData data) ;

	/**
	 * 显示下载
	 * @param data
	 */
	public void showOperationDownload(DownloadData data) ;

	/**
	 * 显示安装
	 * @param data
	 */
	public void showWaitApply(DownloadData data) ;
	
	/**
	 * 显示重试
	 * @param downloader
	 */
	public void showOperationRetry(FileDownloader downloader,int progress) ;

	/**
	 * 显示正在下载
	 * @param downloader
	 */
	public void showOperationDownloading(FileDownloader downloader,int progress) ;

	/**
	 * 显示继续下载
	 * @param downloader
	 */
	public void showOperationContinue(FileDownloader downloader,int progress) ;
	
}
