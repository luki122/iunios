package com.aurora.note.util;

import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

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

	public static final String WIFI_LOCK_TAG = "tencent.qqlive.wifi.lock";

	/**
	 * @uml.property  name="mWifiLock"
	 * @uml.associationEnd  
	 */
	private WifiManager.WifiLock mWifiLock = null;

	/**
	 * @uml.property  name="mManager"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private final WifiManager mManager;

	private static WifiHelper mInstance = null;

	private WifiHelper(Context context) {
		this.mManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		mInstance = this;
	}

	public static WifiHelper getInstance(Context context) {
		if (mInstance == null)
			new WifiHelper(context);
		return mInstance;
	}

	public void connect(final String accessPoint) {
		Log.d(TAG, "trying to connect to AP:" + accessPoint);
		final List<WifiConfiguration> hosts = mManager.getConfiguredNetworks();
		int networkId = -1;
		for (WifiConfiguration conf : hosts) {
			Log.d(TAG, "trying host:" + conf.SSID);
			if (conf.SSID.equalsIgnoreCase("\"" + accessPoint + "\"")) {
				networkId = conf.networkId;
				Log.d(TAG, "found hosts AP in Android with ID:" + networkId);
				break;
			}
		}
		mManager.enableNetwork(networkId, true);
	}

	public void enableWifi(final boolean b) {
		mManager.setWifiEnabled(b);
	}

	public void aquireWifiLock() {
		if (mWifiLock == null) {
			Log.d(TAG, "creating new WifiLock");
			mWifiLock = mManager.createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_LOCK_TAG);
		}
		if (!mWifiLock.isHeld()) {
			Log.d(TAG, "aquiring WifiLock");
			mWifiLock.acquire();
		}
	}

	public WifiLock getNewWifiLock(String lock_postfix) {
		return mManager.createWifiLock(WifiManager.WIFI_MODE_FULL,
				WIFI_LOCK_TAG + lock_postfix);
	}

	public void releaseWifiLock() {
		if (mWifiLock != null && mWifiLock.isHeld()) {
			Log.d(TAG, "releasing WifiLock");
			mWifiLock.release();
		}
	}

	public int getWifiState() {
		switch (mManager.getWifiState()) {
		case WifiManager.WIFI_STATE_UNKNOWN:
			Log.d(TAG, "WIFI_STATE_UNKOWN");
			return WIFI_STATE_UNKNOWN;
		case WifiManager.WIFI_STATE_DISABLED:
		case WifiManager.WIFI_STATE_DISABLING:
			Log.d(TAG, "WIFI_STATE_DISABLED");
			return WIFI_STATE_DISABLED;
		case WifiManager.WIFI_STATE_ENABLING:
		case WifiManager.WIFI_STATE_ENABLED:
			final WifiInfo info = mManager.getConnectionInfo();
			if (info != null && info.getSSID() != null) {
				Log.d(TAG, "WIFI_STATE_CONNECTED to " + info.getSSID());
				return WIFI_STATE_CONNECTED;
			}
			Log.d(TAG, "WIFI_STATE_ENABLED");
			return WIFI_STATE_ENABLED;
		}
		return -1;
	}

	public static void assertWifiState(Context context)
			throws WifiStateException, NoNetworkException {
		if (context != null) {
			if (!SystemUtils.hasNetwork()) {
				throw new NoNetworkException();
			}
			
			/*final int state = WifiHelper.getInstance(context).getWifiState();
			switch (state) {
			case WifiHelper.WIFI_STATE_DISABLED:
			case WifiHelper.WIFI_STATE_UNKNOWN:
				throw new WifiStateException(state);
			}*/
		}
	}

	public static class NoNetworkException extends Exception {
		private static final long serialVersionUID = -300859290934884233L;
		public NoNetworkException() {
			super("This application requires network access. Enable mobile network or Wi-Fi to download data.");
		}
	}

	/**
	 * @author  kodywu
	 */
	public static class WifiStateException  extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3588074771970912287L;
		/**
		 * @uml.property  name="state"
		 */
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

		public WifiStateException(String detailMessage, Throwable throwable, int state) {
			super(detailMessage, throwable);
			this.state = state;
		}

		/**
		 * @return
		 * @uml.property  name="state"
		 */
		public int getState() {
			return state;
		}

	}

}