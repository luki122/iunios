package com.android.gallery3d.util;

import com.android.gallery3d.R;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.widget.Toast;

public class NetworkUtil {

	public static boolean showNoNetworkToast(Context context) {
		boolean hasNetwork = checkNetwork(context);
		if( ! hasNetwork) {
			Toast.makeText(context, R.string.aurora_network_not_available, Toast.LENGTH_SHORT).show();
		}
		return ! hasNetwork;
	}
	
	public static boolean showNotWifiNetworkToast(Context context) {
		boolean wifiNetwork = checkWifiNetwork(context);
		if(! wifiNetwork) {
			Toast.makeText(context, R.string.aurora_network_not_wifi, Toast.LENGTH_SHORT).show();
		}
		return ! wifiNetwork;
	}
	
	public static boolean checkNetwork(Context context) {
		ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo net = conn.getActiveNetworkInfo();
		if (net != null && net.isConnected()) {		
			return true;
		}
		return false;
	}
	
	public static boolean checkWifiNetwork(Context context) {
		ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected()) {		
			return true;
		}
		return false;
	}
	
	public static long getTotalRxBytes(Context context) {
		return TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) == 
				TrafficStats.UNSUPPORTED ? 0 :(TrafficStats.getTotalRxBytes());//单位为B
	}
	
}
