package com.aurora.launcher;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class AuroraBootCompleteReceiver extends BroadcastReceiver {
	
	private final static String TAG = "AuroraBootCompleteReceiver";
	
	private LauncherApplication mApp;
	private LauncherModel mModel;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		mApp = (LauncherApplication) context.getApplicationContext();
		mModel = mApp.getModel();
		final String action = intent.getAction();
		
		Log.i(TAG, "action = " + action);
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			boolean exReady = mModel.ismAuroraExternalReady();
			if (exReady)
				return;
			if (mApp.getExternalStoragePath() == null) {
		    	if(mApp.isIuniDevice()) {
					String spKey = LauncherApplication.getSharedPreferencesKey();
			        SharedPreferences sp = mApp.getSharedPreferences(spKey, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sp.edit();
		            editor.putBoolean(LauncherApplication.FIRST_BOOT, true);
		            editor.commit();
		    		// mModel.setmSdcardMounted(false);
		            setModelState();
		    		return;
		    	}
		        // Aurora <jialf> <2014-02-25> modify for fix bug #2467 begin
				String path = mApp.getInternalStoragePath();
				mModel.setmSdcardMounted(mApp.checkSDCardMount(path));
				LauncherApplication.logVulcan.print("in ACTION_BOOT_COMPLETED setmSdcardMounted to " + mModel.ismSdcardMounted());
				LauncherApplication.logVulcan.print("in ACTION_BOOT_COMPLETED,path = " + path);
		        // Aurora <jialf> <2014-02-25> modify for fix bug #2467 end
			} else {
		        // Aurora <jialf> <2014-01-08> modify for fix bug #1769 begin
				setModelState();
		        // Aurora <jialf> <2014-01-08> modify for fix bug #1769 end
			}
		} else if (Intent.ACTION_SHUTDOWN.equals(action)) {
			Log.i(TAG, "shut down ...");
			String spKey = LauncherApplication.getSharedPreferencesKey();
	        SharedPreferences sp = mApp.getSharedPreferences(spKey, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sp.edit();
            editor.remove(LauncherApplication.APPLICATIONS_AVAILABLE);
            editor.remove(LauncherApplication.FIRST_BOOT);
            editor.commit();
		}
	}
	
	private void setModelState() {
		File file = new File("/mnt/asec");
		if (file.exists()) {
        	File[] list = file.listFiles();
			if (list != null && list.length > 0) {
				Log.i(TAG, "setModelState() : exists app install on Sdcard ...");
				mModel.setmSdcardMounted(false);
				LauncherApplication.logVulcan.print("in setModelState setmSdcardMounted to false");
			} else {
				Log.i(TAG, "setModelState() : no app install on Sdcard ...");
				mModel.setmSdcardMounted(true);
				LauncherApplication.logVulcan.print("in setModelState setmSdcardMounted to true");
			}
		} else {
			mModel.setmSdcardMounted(true);
			LauncherApplication.logVulcan.print("in setModelState setmSdcardMounted to true");
		}
	}

}
