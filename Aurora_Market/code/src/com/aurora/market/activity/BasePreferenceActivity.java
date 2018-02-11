/**   
 * @Title: BaseActivity.java
 * @Package com.aurora.market.activity
 * @Description: TODO(用一句话描述该文件做什么)
 * @author jason
 * @date 2014年6月26日 上午9:36:04
 * @version V1.0
 */
package com.aurora.market.activity;

import java.util.List;

import com.aurora.market.R;
import com.aurora.market.service.AppDownloadService;
import com.aurora.market.util.Globals;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.preference.AuroraPreferenceActivity;

/**
 * @ClassName: BaseActivity
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author jason
 * @date 2014年6月26日 上午9:36:04
 * 
 */
public class BasePreferenceActivity extends AuroraPreferenceActivity {
	private SharedPreferences sp;
	private MyBroadcastReciver broadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		/*sp = getSharedPreferences(Globals.SHARED_WIFI_APPUPDATE, MODE_APPEND);
		initBroadCast();*/

	}

	public boolean isForeground(String PackageName) {
		// Get the Activity Manager
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

		// Get a list of running tasks, we are only interested in the last one,
		// the top most so we give a 1 as parameter so we only get the topmost.
		List<ActivityManager.RunningTaskInfo> task = manager.getRunningTasks(1);

		// Get the info we need for comparison.
		ComponentName componentInfo = task.get(0).topActivity;

		// Check if it matches our package name.
		if (componentInfo.getPackageName().equals(PackageName))
			return true;

		// If not then our app is not on the foreground.
		return false;
	}
/*	private void openNetDialog()
	{
		final Editor ed = sp.edit();
		boolean isDisconnect = sp.getBoolean(
				Globals.SHARED_WIFI_DISCONNECT_ISEXITS, false);
		if (isDisconnect) {
			ed.putBoolean(Globals.SHARED_WIFI_DISCONNECT_ISEXITS, false);
			ed.commit();
			AuroraAlertDialog mWifiConDialog = new AuroraAlertDialog.Builder(
					BasePreferenceActivity.this, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
					.setTitle(
							BasePreferenceActivity.this.getResources().getString(
									R.string.dialog_prompt))
					.setMessage(
							BasePreferenceActivity.this.getResources().getString(
									R.string.no_wifi_download_message))
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									ed.putBoolean(
											Globals.SHARED_MOBILE_STATUS_ISDOWNLOAD,
											false);
									ed.commit();
								}

							})
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									ed.putBoolean(
											Globals.SHARED_MOBILE_STATUS_ISDOWNLOAD,
											true);
									ed.commit();
									SharedPreferences perf = PreferenceManager
											.getDefaultSharedPreferences(BasePreferenceActivity.this);
									Editor ed1 = perf.edit();
									ed1.putBoolean("wifi_download_key", false);
									ed1.commit();
									Intent networkChange = new Intent(BasePreferenceActivity.this, AppDownloadService.class);
									networkChange.putExtra(AppDownloadService.DOWNLOAD_OPERATION,
											AppDownloadService.OPERATION_NETWORK_MOBILE_CONTINUE);
									BasePreferenceActivity.this.startService(networkChange);
								}

							}).create();
			
			 * mWifiConDialog.getWindow().setType(
			 * WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			 
			mWifiConDialog.show();
		}
	}*/
	private void initBroadCast() {
		// TODO Auto-generated method stub
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Globals.BROADCAST_ACTION_DOWNLOAD_PAUSE);
		broadcastReceiver = new MyBroadcastReciver();
		this.registerReceiver(broadcastReceiver, intentFilter);
	}

	private class MyBroadcastReciver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			/*String action = intent.getAction();
			if (!isForeground("com.aurora.market"))
				return;
			if (action.equals(Globals.BROADCAST_ACTION_DOWNLOAD_PAUSE)) {
				openNetDialog();

			}*/
		}

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		//this.unregisterReceiver(broadcastReceiver);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		//openNetDialog();

	}

}
