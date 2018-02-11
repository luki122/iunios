package com.aurora.account.download;

/**
 * 下载状态监听 
 */
public interface DownloadStatusListener {
	
	public void onDownload(String path, int status, long downloadSize, long fileSize);
	
}
