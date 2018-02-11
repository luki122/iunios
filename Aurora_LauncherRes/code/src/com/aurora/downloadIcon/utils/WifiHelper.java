package com.aurora.downloadIcon.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiHelper {
	public static final String TAG = "WifiHelper";
	/**
	 * WiFi is disabled or disabling
	 */
	public static final int WIFI_STATE_DISABLED = WifiManager.WIFI_STATE_DISABLED;
	/**
	 * Wifi is enabled but not connected
	 */
	public static final int WIFI_STATE_ENABLED = WifiManager.WIFI_STATE_ENABLED;
	/**
	 * There was an error enabling or disabling WiFi...
	 */
	public static final int WIFI_STATE_UNKNOWN = WifiManager.WIFI_STATE_UNKNOWN;
	/**
	 * WiFi is enabled and connected.
	 */
	public static final int WIFI_STATE_CONNECTED = 5;

	private final WifiManager mManager;

	private static WifiHelper mInstance = null;

	private WifiHelper(Context context) {
		this.mManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		mInstance = this;
	}

	public static WifiHelper getInstance(Context context) {
		if (mInstance == null)
			new WifiHelper(context);
		return mInstance;
	}

	public int getWifiState() {
		switch (mManager.getWifiState()) {
		case WifiManager.WIFI_STATE_UNKNOWN:
			return WIFI_STATE_UNKNOWN;
		case WifiManager.WIFI_STATE_DISABLED:
		case WifiManager.WIFI_STATE_DISABLING:
			return WIFI_STATE_DISABLED;
		case WifiManager.WIFI_STATE_ENABLING:
		case WifiManager.WIFI_STATE_ENABLED:
			final WifiInfo info = mManager.getConnectionInfo();
			if (info != null && info.getSSID() != null) {
				return WIFI_STATE_CONNECTED;
			}
			return WIFI_STATE_ENABLED;
		}
		return -1;
	}

	public static void assertWifiState(Context context)
			throws WifiStateException {
		if (context != null) {
			final int state = WifiHelper.getInstance(context).getWifiState();
			switch (state) {
			case WifiHelper.WIFI_STATE_DISABLED:
			case WifiHelper.WIFI_STATE_UNKNOWN:
				throw new WifiStateException(state);
			}
		}
	}

	public static class WifiStateException extends Exception {
		private static final long serialVersionUID = 3588074771970912287L;
		private int state;

		public WifiStateException(int state) {
			this.state = state;
		}

		public WifiStateException(String detailMessage, int state) {
			super(detailMessage);
			this.state = state;
		}

		public WifiStateException(Throwable throwable, int state) {
			super(throwable);
			this.state = state;
		}

		public WifiStateException(String detailMessage, Throwable throwable,
				int state) {
			super(detailMessage, throwable);
			this.state = state;
		}

		public int getState() {
			return state;
		}

	}

}
