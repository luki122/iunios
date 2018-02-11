package com.aurora.downloader.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 *  判断网络连接 的工具类
 */
public class NetWorkConectUtil {
	/**
	 * 判断是否具有网络连接
	 * 
	 * @param context
	 * @return
	 */
	public static final boolean hasNetWorkConection(Context context) {
		// 获取连接活动管理器
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// 获取连接的网络信息
		final NetworkInfo networkInfo = connectivityManager
				.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isAvailable());
	}

	/**
	 * 判断是否具有wifi连接
	 * 
	 * @param context
	 * @return
	 */
	public static final boolean hasWifiConnection(Context context) {
		// 获取连接活动管理器
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo networkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return (networkInfo != null && networkInfo.isAvailable());
	}

	/**
	 * 判断是否有GPRS连接
	 * 
	 * @param context
	 * @return
	 */
	public static final boolean hasGPRSConnection(Context context) {
		// 获取连接活动管理器
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo networkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		return (networkInfo != null && networkInfo.isAvailable());
	}

	/**
	 * 判断网络连接类型
	 * 
	 * @param context
	 * @return
	 */
	public static final int getNetworkConnectionType(Context context) {
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo wifiNetWorkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		final NetworkInfo mobileNetWorkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (wifiNetWorkInfo != null && wifiNetWorkInfo.isAvailable()) {
			return ConnectivityManager.TYPE_WIFI;
		} else if (mobileNetWorkInfo != null && mobileNetWorkInfo.isAvailable()) {
			return ConnectivityManager.TYPE_MOBILE;
		} else {
			return -1;
		}
	}
}