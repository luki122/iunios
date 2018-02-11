package com.aurora.mediascanner;

import android.app.Application;

public class ScannerApplication extends Application {

	private static String lastStorage;

	/**
	 * @return the lastStorage
	 */
	public static String getLastStorage() {
		return lastStorage;
	}

	/**
	 * @param lastStorage
	 *            the lastStorage to set
	 */
	public static void setLastStorage(String lastStorage) {
		ScannerApplication.lastStorage = lastStorage;
	}
}
