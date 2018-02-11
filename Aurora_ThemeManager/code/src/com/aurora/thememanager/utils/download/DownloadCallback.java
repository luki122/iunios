package com.aurora.thememanager.utils.download;

/**
 * 下载状态监听 
 */
public interface DownloadCallback {
	
	/**
	 * 下载数据的下载状态更新监听
	 * @param downloadDataId
	 * @param status
	 * @param downloadSize
	 * @param fileSize
	 */
	public void onDownload(int downloadDataId, int status, long downloadSize, long fileSize);
	
}
