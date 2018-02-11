package com.aurora.apihook.touch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IHardwareService;
import android.os.ServiceManager;
import android.provider.Settings;
import android.os.RemoteException;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.database.ContentObserver;

public class AuroraButtonKeyLight {
	public static final String TAG = "AuroraButtonKeyLight";
	
	private Context mContext;
	IHardwareService mLight;
	private boolean mButtonLightEnabled;

	private ContentObserver mButtonLightObserver = new ContentObserver(
			new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			mButtonLightEnabled = Settings.System.getInt(
					mContext.getContentResolver(), "button_key_light", 1) == 0 ? false
					: true;
		}
	};

	public AuroraButtonKeyLight(Context context) {
		mContext = context;
		mButtonLightEnabled = Settings.System.getInt(
				mContext.getContentResolver(), "button_key_light", 1) == 0 ? false
				: true;
		if (mButtonLightEnabled) {
			mLight = IHardwareService.Stub.asInterface(ServiceManager
					.getService("hardware"));

			if (mLight == null)
				mButtonLightEnabled = false;
		}

		mContext.getContentResolver().registerContentObserver(
				Settings.System.getUriFor("button_key_light"), true,
				mButtonLightObserver);
		
		IntentFilter filter = new IntentFilter();
    	//filter.addAction(Intent.ACTION_SCREEN_ON);
    	filter.addAction(Intent.ACTION_SCREEN_OFF);	
    	mContext.registerReceiver(mScreenReceiver, filter);
	}

	public void handleEvent(MotionEvent event) {
		if (mButtonLightEnabled && event.getAction() == MotionEvent.ACTION_UP) {
			try {
				if(mLight != null){
					   mLight.setButtonLightEnabled(true);
				}
			} catch (RemoteException e) {
				Log.v(TAG,
						"remote call for turn off button light failed.");
			}
		}
	}
	
	private final BroadcastReceiver mScreenReceiver =  new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
				Log.v(TAG,
						"ACTION_SCREEN_OFF.");
			String action = intent.getAction();
             if(Intent.ACTION_SCREEN_OFF.equals(action)){
    			try {
    				if(mLight != null){
    					   mLight.setButtonLightEnabled(false);
    				}
    			} catch (RemoteException e) {
    				Log.v(TAG,
    						"-------onReceive----remote call for turn off button light failed.");
    			}
    		}
		}
	};
	
}