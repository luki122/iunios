package com.aurora.downloader.util;

public class ButtonUtil {

	private long lastClickTime;

	/**
	 * 防止双击
	 * 
	 * @return
	 */
	public boolean isFastDoubleClick() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		if (0 < timeD && timeD < 500) {
			return true;
		}
		lastClickTime = time;
		return false;
	}

	private static long lastClickTime2;

	public static boolean isFastDouble() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime2;
		if (0 < timeD && timeD < 200) {
			return true;
		}
		lastClickTime2 = time;
		return false;
	}
}
