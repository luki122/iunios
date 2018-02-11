/*
 * @author 张伟
 */
package com.aurora.calendar.util;

import java.io.File;


public class Globals {

	public static final String APP_ID = "wx823c8bd45ff91f39";
	public static final String QQ_APP_ID = "1104520657";
	public static final String FILE_PROTOCOL = "file://";
	public static final String LOG_DIR_NAME = "/Calendar/log";
	//图片类型
	public static final String IMAGE_TYPE = "jpg,gif,jpeg,bmp,png";
	
	//wifi网络类型标志
	public static final int NETWORK_WIFI = 0;
	//2G网络类型标志
	public static final int NETWORK_2G = 1;
	//3G网络类型标志
	public static final int NETWORK_3G = 2;
	
	// 生成图片路径
	public static final File PIC_DIR = new File(
			"/sdcard" + "/Calendar/picture");

	public static final String PIC_PATH = "/Calendar/picture";
	
}