package com.android.deskclock.guardian;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class GuardService extends Service {
	
	private boolean mFlag;
	private Handler mHandler = new Handler();
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		

		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
			String action = intent.getAction();
			//Log.d("cjslog", "onReceive " + action);
			
			if (action.equals("com.android.deskclock.TIMEWIDGETDISABLED")) {
				//Log.d("cjslog", "stopself");
				stopSelf();
				return;
			}
			
			mFlag = true;
			if (action.equals("com.android.widget.WIDGET_TIME_CHANGE")) {
				mFlag = false;
				Intent intent2 = new Intent("com.android.deskclock.GUARDSURVIVAL");
				sendBroadcast(intent2);
				return;
			}
			
			if (action.equals(Intent.ACTION_TIME_TICK)) {
				mHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {

						// TODO Auto-generated method stub
						if (mFlag) {
							Intent intent= new Intent("com.aurora.UPDATE_SERVICE_WIDGET");
							intent.setPackage("com.android.deskclock");
							try {
								Context deskclock = getApplicationContext().createPackageContext("com.android.deskclock", Context.CONTEXT_IGNORE_SECURITY);
								//deskclock.startService(intent);
								//intent.setAction("com.aurora.UPDATE_SERVICE_WIDGET");
								deskclock.startService(intent);
							} catch (NameNotFoundException e) {
								// TODO: handle exception
							}
						}
					}
				}, 500);
			}
			

		
		}
	};

	


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("cjslog", "GuardService onCreate");
		IntentFilter intentFilter = new IntentFilter("com.android.widget.WIDGET_TIME_CHANGE");
		intentFilter.addAction(Intent.ACTION_TIME_TICK);
		intentFilter.addAction("com.android.deskclock.TIMEWIDGETDISABLED");
		registerReceiver(mBroadcastReceiver, intentFilter);
		SharedPreferences sharedPreferences = getSharedPreferences("servicestate", Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean("timewidgetexsist", true);
		editor.apply();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d("cjslog", "GuardService onStartCommand");
		return Service.START_NOT_STICKY;
	}
	
	@Override
	public void onDestroy() {
		Log.d("cjslog", "GuardService onDestroy");
		// TODO Auto-generated method stub
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}
	
}
