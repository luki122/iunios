package com.aurora.downloader.util;

import java.io.File;

public class FileUtil {

	public static boolean deleteFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return true;
		}
		try {
			return file.delete();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
}
