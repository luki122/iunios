package com.aurora.iunivoice.utils;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class FilePathUtil {

	public static final String BASE = "IuniVoice";
	public static final String CACHE_SDCARD = "Cache";
	public static final String IMAGE_CACHE_SDCARD = "ImageCache";

	public static final String APP_UPDATE_DIR_NAME = "update";// 应用更新位置
	public static final String INTERNAL_STORAGE = "data";
	public static final String INTERNAL_CACHE = "cache";

	public static String getSDCardCachePath() {
		return BASE + File.separator + CACHE_SDCARD;
	}

	public static String getImageCacheSDcardPath() {
		return getSDCardCachePath() + File.separator + IMAGE_CACHE_SDCARD;
	}

	/**
	 * 获取存放软件的位置
	 * 
	 * @return
	 */
	public static String getAPKFilePath(Context ctx) {
		String pathString = "";
		if (FileUtil.isExistSDcard()) {
			pathString = getSDcardSoftWarePath();
		} else {
			pathString = getDataSoftWarePath(ctx);
		}
		File temp = new File(pathString);
		if (!temp.exists()) {
			temp.mkdirs();
		}
		return pathString;
	}

	/**
	 * 获取SD卡存放软件的位置
	 * 
	 * @return
	 */
	private static String getSDcardSoftWarePath() {
		return Environment.getExternalStorageDirectory() + File.separator
				+ BASE + File.separator + APP_UPDATE_DIR_NAME+File.separator;
	}

	/**
	 * 获取内部存储软件的位置
	 * 
	 * @param ctx
	 * @return
	 */
	private static String getDataSoftWarePath(Context ctx) {
		return File.separator + INTERNAL_STORAGE + File.separator
				+ INTERNAL_STORAGE + File.separator + ctx.getPackageName()
				+ File.separator + INTERNAL_CACHE + File.separator
				+ APP_UPDATE_DIR_NAME + File.separator;
	}

}