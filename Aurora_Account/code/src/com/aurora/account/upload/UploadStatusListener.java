package com.aurora.account.upload;

/**
 * 下载状态监听 
 */
public interface UploadStatusListener {
	
	public void onDownload(String path, int status, long downloadSize, long fileSize);
	
}
