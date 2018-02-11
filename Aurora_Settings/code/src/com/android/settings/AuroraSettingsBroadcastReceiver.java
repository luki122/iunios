package com.android.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import com.android.settings.wifi.AuroraAutoChangeAp;
import com.android.settings.wifi.WifiSettings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;

public class AuroraSettingsBroadcastReceiver  extends BroadcastReceiver{
	private  final static String TAG = "AuroraSettingsInit"; 
	private UsbManager mUsbManager = null;
	private AudioManager mAudioManager = null;
	private Context mContext = null;
	
	private static final String GLOVE_USB_Toggle_PATH = "sys/class/i2c-dev/i2c-2/device/2-0020/input/input1/charger_enable";
	private static final String AURORA_USB_MTP = "mtp,diag,adb";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
        if(mAudioManager == null){
        	mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		}
        
        if(mUsbManager == null){
        	mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		}
		
        if(mContext == null){
        	mContext = context;
        }
        
		String action = intent.getAction();
		Log.v(TAG, "----onReceive-----action==="+action);
		if (action.equals(UsbManager.ACTION_USB_STATE)) {
			boolean connected = intent.getExtras().getBoolean(UsbManager.USB_CONNECTED);
			Log.v(TAG, "*****USB_CONNECTED**** = " + connected);
			if(!connected){
				setMtpMode();
			}
			// glove

			File gloveFile = new File(GLOVE_USB_Toggle_PATH);
			if(gloveFile.exists()){
				writePreferenceClick(connected, GLOVE_USB_Toggle_PATH);
			}
			
			//GLOVE_USB_Toggle_PATH
			//  update the autotime
		}else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
			NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                    WifiManager.EXTRA_NETWORK_INFO);
			boolean autoTimeEnabled = getAutoState(Settings.Global.AUTO_TIME);
	        boolean autoTimeZoneEnabled = getAutoState(Settings.Global.AUTO_TIME_ZONE);
           if(info.isConnected() && autoTimeEnabled && autoTimeZoneEnabled){
        	   Log.i("qy", "AuroraLightSensorService --> Wifi isConnected()");
        	   
        	Timer t = new Timer();
   			TimerTask task = new TimerTask(){  
   				      public void run() {  
   				   Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AUTO_TIME,
                           0);
            	   Settings.Global.putInt(
            			   mContext.getContentResolver(), Settings.Global.AUTO_TIME_ZONE,  0);
            	   Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AUTO_TIME,
                           1);
            	   Settings.Global.putInt(
            			   mContext.getContentResolver(), Settings.Global.AUTO_TIME_ZONE,  1); 	    				    	 
   				    	  
   				   }  
   				 }; 
   			  t.schedule(task,1000);
           }
        
		}//Aurora penggangding 2014-09-01 begin
		else if(action.equals(WifiManager.RSSI_CHANGED_ACTION) || action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
		{
			WifiSettings.PrintLog("pgd","  scan_ result action: "+action.toString());
			doAutoChangeThread();
		//Aurora penggangding 2014-09-01 end
		}
	
	}
	
	private void setMtpMode() {
		//wolfu change usb diag function
		String deviceName = SystemProperties.get("ro.product.name");
		if (deviceName.contains("IUNI")) {

            	 boolean haveimei = false;
                 TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                 String imei = tm.getDeviceId();
                 if(imei !=null  ){
                	 if(imei.length()>6){
                		 haveimei = true;
                	 }                 	
             	}
			Log.v(TAG, "*****setMtpMode**** haveimei = " + haveimei);

			if (!haveimei) 
			{
				if(!mUsbManager.isFunctionEnabled("diag"))
				{
			Log.v(TAG, "*****setMtpMode**** setCurrentFunction = " + haveimei);
				mUsbManager.setCurrentFunction(AURORA_USB_MTP, true);
				}
			} else {
				mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MTP, true);
			}
		} else 
		{
			mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MTP, true);
		}
	}
	
	private void writePreferenceClick(boolean isChecked, String filePath) {
		FileOutputStream out = null;
		File outFile = null;
		try {
			outFile = new File(filePath);
			out = new FileOutputStream(outFile);
			if (isChecked) {
				out.write("1\n".getBytes());
			} else {
				out.write("0\n".getBytes());
			}
			if (null != out) {
				out.flush();
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean getAutoState(String name) {
		try {
			return Settings.Global.getInt(mContext.getContentResolver(), name) > 0;
		} catch (SettingNotFoundException snfe) {
			return false;
		}
	}
	
	// Aurora penggangding 2014-09-01 begin
		private void autoChangeAp() {
			final AuroraAutoChangeAp changeAp = new AuroraAutoChangeAp(mContext);
			changeAp.compareSSSIDLevel();
		}

		private Runnable autoChangeRun = new Runnable() {
			@Override
			public void run() {
				autoChangeAp();
			}
		};

		private void doAutoChangeThread() {
			final Thread autoChangethread = new Thread(autoChangeRun);
			autoChangethread.start();
		}

		// Aurora penggangding 2014-09-01 end
}