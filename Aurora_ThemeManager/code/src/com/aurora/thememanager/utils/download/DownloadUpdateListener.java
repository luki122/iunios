package com.aurora.thememanager.utils.download;

/**
 * 下载更新监听器
 * @author alexluo
 *
 */
public interface DownloadUpdateListener {

	/**
	 * 下载过程中会调用这个方法去更新界面和状态
	 * 等
	 */
	public void downloadProgressUpdate();
	
}
