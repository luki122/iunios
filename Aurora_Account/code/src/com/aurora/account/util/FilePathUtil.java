package com.aurora.account.util;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class FilePathUtil {

	/**
	 * 获取存放下载附件的位置
	 * 
	 * @return
	 */
	public static String getExtraFilePath(Context ctx) {
		String pathString = "";
		if (FileUtil.isExistSDcard()) {
			pathString = getSDcardExtraFilePath();
		} else {
			pathString = getDataExtraFilePath(ctx);
		}
		File temp = new File(pathString);
		if (!temp.exists()) {
			temp.mkdirs();
		}
		return pathString;
	}

	/**
	 * 获取SD卡存放下载附件的位置
	 * 
	 * @return
	 */
	private static String getSDcardExtraFilePath() {
		return Environment.getExternalStorageDirectory()
				+ "/account/extraFile/";
	}

	/**
	 * 获取内部存储下载附件的位置
	 * 
	 * @param ctx
	 * @return
	 */
	private static String getDataExtraFilePath(Context ctx) {
		return "/data/data/" + ctx.getPackageName() + "/cache/extraFile/";

	}

}
