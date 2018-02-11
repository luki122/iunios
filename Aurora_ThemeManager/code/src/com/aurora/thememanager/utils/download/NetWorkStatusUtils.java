package com.aurora.thememanager.utils.download;



import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

public class NetWorkStatusUtils {

	private static final String TAG = "NetWorkStatusUtils";
	
	
	/**
	 * 获取是否有网络
	 * 
	 * @return
	 */
	public static boolean hasNetwork(Context context) {
		if (context != null) {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
					.getState();
			State mobileState = cm.getNetworkInfo(
					ConnectivityManager.TYPE_MOBILE).getState();
			if (wifiState == State.CONNECTED || mobileState == State.CONNECTED) {
				return true;
			}else{
				return false;
			}
		}else{
			return true;
		}
	}

	public static int getConnectingType(Context context) {
		ConnectivityManager mConnectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		TelephonyManager mTelephony = (TelephonyManager) context
				.getSystemService(context.TELEPHONY_SERVICE);

		NetworkInfo info = mConnectivity.getActiveNetworkInfo();

		if (info == null || !mConnectivity.getBackgroundDataSetting()) {
			return -1;
		}

		int netType = info.getType();
		int netSubtype = info.getSubtype();

		if (netType == ConnectivityManager.TYPE_WIFI) {
			return DownloadAction.NET_TYPE.NETWORK_WIFI;
		} else {
			if ((netSubtype == TelephonyManager.NETWORK_TYPE_GPRS)
					|| (netSubtype == TelephonyManager.NETWORK_TYPE_EDGE)
					|| (netSubtype == TelephonyManager.NETWORK_TYPE_CDMA)) {
				return DownloadAction.NET_TYPE.NETWORK_2G;
			} else {
				return DownloadAction.NET_TYPE.NETWORK_3G;
			}
		}

	}
	
	public static boolean isWifiNetwork(Context context) {
		if (!NetWorkStatusUtils.isNetworkConnected(context)) {
			// 当前网络获取判断，如无网络连接，直接后台日志
			Log.d(TAG, "isWifiNetwork None network");
			return false;
		}
		// 连接后判断当前WIFI
		if (NetWorkStatusUtils.getConnectingType(context) == DownloadAction.NET_TYPE.NETWORK_WIFI) {
			return true;
		} else {
			return false;
		}
	}
	
	public static int isNetStatus(Context context) {
		if (!NetWorkStatusUtils.isNetworkConnected(context)) {
			// 当前网络获取判断，如无网络连接，直接后台日志
			Log.d(TAG, "isWifiNetwork None network");
			return 0;
		}
		// 连接后判断当前WIFI
		if (NetWorkStatusUtils.getConnectingType(context) == DownloadAction.NET_TYPE.NETWORK_WIFI) {
			return 1;
		} else {
			return 2;
		}
	}
	
	public static boolean isAirplaneModeOn(Context context) {
		return Settings.System.getInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}

	public static boolean hasActiveNetwork(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService("connectivity");
		if (connectivityManager.getActiveNetworkInfo() != null) {
			return connectivityManager.getActiveNetworkInfo().isAvailable();
		} else {
			return false;
		}
	}

	public static boolean isNetworkReady(Context context) {
		TelephonyManager manager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		return (manager.getNetworkType() != TelephonyManager.NETWORK_TYPE_UNKNOWN);
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			return false;
		} else {
			NetworkInfo[] info = manager.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].isAvailable()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			return false;
		} else {
			NetworkInfo[] info = manager.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isWifiEnabled(Context context) {
		WifiManager manager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		return (manager == null ? false : manager.isWifiEnabled());
	}

	
}
