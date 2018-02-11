/*
 * Copyright (c) 2013 Qualcomm Technologies, Inc.  All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.qualcomm.listen.voicewakeup;

import android.content.*;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class BootupReceiver extends BroadcastReceiver {
	private final static String TAG = "BootupReceiver";
	private final static String MYTAG = "iht";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		Log.v(MYTAG, "action::::::::::::::::::::::::::"+action);
		
		if(action.equals("com.aurora.voiceassistant.UNSERVICE")){
			
			Intent intentService = new Intent(context, VwuService.class);
			context.stopService(intentService);
			
		}else if(action.equals("com.aurora.voiceassistant.STSERVICE")){
			
			Intent intentService = new Intent(context, VwuService.class);
			context.startService(intentService);
			
		}
		else{
			new Thread(new StartRunnable(context)).start();
		}
	}

	private class StartRunnable implements Runnable {
		private final Context mContext;

		public StartRunnable(Context context) {
			mContext = context;
		}

		public void run() {
			while(true) {
				String state = Environment.getExternalStorageState();
				//Log.v(TAG, "getExternalStorageState ... - " + state);
				Log.v(MYTAG, "******************开机自启动**********************"+state);
				if(Environment.MEDIA_CHECKING.equals(state)) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					boolean bool = false;
					try {
						bool = Utils.readFile("/data/data/com.aurora.voiceassistant/switch.txt", "wakeup");
						//Global.getInstance().setEnableListen(bool);
						//Global.getInstance().setEnableVoiceWakeup(bool);
					} catch (Exception e) {
						e.printStackTrace();
					}

					Log.v(MYTAG, "启动服务.............bool:"+bool);
					//启动服务
					if(bool){
						Intent intent = new Intent("com.qualcomm.listen.voicewakeup.REMOTE_SERVICE"); //启动Service
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						mContext.startService(intent);
					}
					break;
				}
			}
		}
	}

}
