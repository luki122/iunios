package com.aurora.thememanager.utils.download;

import android.os.Parcelable;

public abstract class DownloadData implements Parcelable {

	/**
	 * 下载ID
	 */
	public int downloadId;
	
	/**
	 * 下载URL
	 */
	public String downloadPath;
	
	/**
	 * 文件版本号
	 */
	public int versionCode;

	/*
	 *  以下字段只有在数据库查找时才会有
	 */
	
	/**
	 * 下载状态
	 */
	public int status; 
	/**
	 * 文件存放目录
	 */
	public String fileDir;
	/**
	 * 文件名称
	 */
	public String fileName; 
	/**
	 * 任务完成时间
	 */
	public long finishTime; 

}
