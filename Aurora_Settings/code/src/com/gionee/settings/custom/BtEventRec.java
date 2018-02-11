package com.gionee.settings.custom;

import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.bluetooth.BluetoothAdapter;
import android.os.ServiceManager;
import com.android.internal.statusbar.IStatusBarService;
import android.os.SystemProperties;
import android.util.Log;

public class BtEventRec extends BaseReceiver {

	private final String CONTROL = "com.gionee.bt.enable";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		SharedPreferences sp = context.getSharedPreferences(INFO,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();

		if ("com.gionee.bt.enable".equals(intent.getAction())) {
			editor.putBoolean(CONTROL, true);
			editor.commit();
		}

		if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
			if (BluetoothAdapter.STATE_ON == intent.getIntExtra(
					BluetoothAdapter.EXTRA_STATE, -9)) {

				if (checkFrontActivity(context)
						&& sp.getBoolean(CONTROL, false)) {
					collapseStatusBar();
					try {
						context.startActivity(new Intent(
								"com.android.settings.btdevdialog")
								.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
						context.sendBroadcast(new Intent("close.wifi.dialog"));
					} catch (ActivityNotFoundException ex) {
						ex.printStackTrace();
					} finally {
						editor.putBoolean(CONTROL, false);
						editor.commit();
					}
				}
			}
		}
	}

}
