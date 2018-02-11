package com.aurora.market.download;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class FilePathUtil {

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
	 * 获取存放自动更新软件的位置
	 * 
	 * @return
	 */
	public static String getAutoUpdateFilePath(Context ctx) {
		String pathString = "";
		if (FileUtil.isExistSDcard()) {
			pathString = getSDcardAutoUpdatePath();
		} else {
			pathString = getDataAutoUpdatePath(ctx);
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
		return Environment.getExternalStorageDirectory()
				+ "/market/apk/";
	}

	/**
	 * 获取内部存储软件的位置
	 * 
	 * @param ctx
	 * @return
	 */
	private static String getDataSoftWarePath(Context ctx) {
		return "/data/data/" + ctx.getPackageName() + "/cache/apk/";

	}
	
	/**
	 * 获取SD卡存放自动更新软件的位置
	 * 
	 * @return
	 */
	private static String getSDcardAutoUpdatePath() {
		return Environment.getExternalStorageDirectory()
				+ "/market/autoupdate/";
	}

	/**
	 * 获取内部存储自动更新软件的位置
	 * 
	 * @param ctx
	 * @return
	 */
	private static String getDataAutoUpdatePath(Context ctx) {
		return "/data/data/" + ctx.getPackageName() + "/cache/autoupdate/";

	}

}
