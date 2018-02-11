package com.aurora.tools;

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
		if (0 < timeD && timeD < 500) {
			return true;
		}
		lastClickTime2 = time;
		return false;
	}
	
	private static long lastClickTime3;

	public static boolean isFastClick() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime3;
		if (0 < timeD && timeD < 1000) {
			return true;
		}
		lastClickTime3 = time;
		return false;
	}
	private  long lastClickTime4;

	public  boolean isFastClick(long t) {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime4;
		if (0 < timeD && timeD < t) {
			return true;
		}
		lastClickTime4 = time;
		return false;
	}
}
