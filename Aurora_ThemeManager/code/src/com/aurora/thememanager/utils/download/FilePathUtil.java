package com.aurora.thememanager.utils.download;

import java.io.File;

import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.utils.ThemeConfig;

import android.content.Context;
import android.os.Environment;


public class FilePathUtil {
	
	public static final String DOWNLOAD_PATH="download";
	
	public static final String AUTO_UPDATE_PATH="update";
	/**
	 * 检测SDcard是否存在
	 * 
	 * @return
	 */
	public static boolean isExistSDcard() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 获取存放软件的位置
	 * 
	 * @return
	 */
	public static String getDownloadPath(int type) {
		String pathString = "";
		if (isExistSDcard()) {
			pathString = getSDcardPath(type);
		} 
		File temp = new File(pathString);
		if (!temp.exists()) {
			temp.mkdirs();
		}
		return pathString;
	}
	
	public static String getAutoUpdatePath(int type){
		String pathString = "";
		if (isExistSDcard()) {
			pathString = getSDcardPath(type);
		} 
		File temp = new File(pathString);
		if (!temp.exists()) {
			temp.mkdirs();
		}
		return pathString;
	}
	
	private static String getSDcardPath(int type) {
		switch (type) {
		case Theme.TYPE_RINGTONG:
			
			return ThemeConfig.THEME_RINGTONG_DOWNLLOAD_PATH;
		case Theme.TYPE_THEME_PKG:
			
			return ThemeConfig.THEME_DOWNLLOAD_PATH;
		case Theme.TYPE_TIME_WALLPAPER:
			
			return ThemeConfig.THEME_TIME_WALLPAPER_DOWNLLOAD_PATH;
		case Theme.TYPE_WALLPAPER:
			
			return ThemeConfig.THEME_WALL_PAPER_DOWNLLOAD_PATH;

		default:
			break;
		}
		return ThemeConfig.THEME_DOWNLLOAD_PATH;
	}
	
	

}
