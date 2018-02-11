package com.aurora.thememanager.utils.download;

/**
 * 下载状态监听 
 */
public interface DownloadStatusListener {
	
	public void onDownload(int appId, int status, long downloadSize, long fileSize);
	
}
