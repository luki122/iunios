package com.gionee.settings.custom;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.content.ActivityNotFoundException;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class WifiScanResultRec extends BaseReceiver {

	private final String TAG = "WifiScanResultRecTag";
	private final String CONTROL = "custom_wifi_enable";
  private final String CMPTIME = "cmp_time";

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sp = context.getSharedPreferences(INFO,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();

		// TODO Auto-generated method stub
		if ("com.gionee.wifi.enable".equals(intent.getAction())) {
			Log.d(TAG, "---com.gionee.wifi.enable---");
			editor.putBoolean(CONTROL, true);
            editor.putLong(CMPTIME, System.currentTimeMillis());
			editor.commit();
		} else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent
				.getAction()) && sp.getBoolean(CONTROL, false)) {
			if (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -9) == WifiManager.WIFI_STATE_ENABLED) {
				WifiManager wm = (WifiManager) context
						.getSystemService(Context.WIFI_SERVICE);
				wm.startScan();
			}

		} else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent
				.getAction()) && sp.getBoolean(CONTROL, false)) {
            if ( System.currentTimeMillis() - sp.getLong(CMPTIME, 0)> 3500) {
				editor.putBoolean(CONTROL, false);
				editor.commit();
                return ;
            }
			if (checkFrontActivity(context) ) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				ConnectivityManager cm = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo info = cm
						.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (info.getState() == NetworkInfo.State.CONNECTING) {
					collapseStatusBar();
					return;
				}
		
				try {
					context.startActivity(new Intent(
							"com.android.settings.apdialog")
							.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					context.sendBroadcast(new Intent("close.bt.dialog"));
                    Thread td = new Thread() {
                        public void run() {
                            try {
                                Thread.sleep(1200); 
                            } catch (Exception ex) {
                            }
                            collapseStatusBar();
                        }
                     };
                     td.start();
				} catch (ActivityNotFoundException ex) {
					ex.printStackTrace();
				}  finally {
					editor.putBoolean(CONTROL, false);
					editor.commit();
				}
								
			}

		}
	}

}
