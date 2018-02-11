package com.aurora.thememanager.utils.download;

import java.io.File;

public class FileUtils {
	
	/**
	 * 删除文件
	 * 
	 * @param file
	 * @return
	 */
	public static boolean deleteFile(File file) {
		if (file != null && file.exists()) {
			return file.delete();
		}
		return false;
	}

	/**
	 * 删除文件夹或者文件
	 * 
	 * @param dirOrFile
	 */
	public static void deleteDirOrFile(File dirOrFile) {
		if (dirOrFile.isDirectory()) {
			File[] files = dirOrFile.listFiles();
			if (files != null) {
				for (File file : files) {
					deleteDirOrFile(file);
				}
			}
		}
		dirOrFile.delete();
	}
}


