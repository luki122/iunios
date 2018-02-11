package com.gionee.autommi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.net.ServerSocket;
import java.net.Socket;
import android.os.PowerManager;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import android.provider.Settings;


public class AutoMMI extends Application {
    private static final String TAG = "AutoMMI";
	private WifiManager wifiManager;
    private BluetoothAdapter btAdapter;
    private PowerManager powerManager;
    private static String RERORD_FILE_PATH = "/data/amt/amt_record";
    private static String LINESEP = "\n";
	private File record = new File(RERORD_FILE_PATH);
	private ServerSocket server;
	private Socket channel;
	private static int SCREENTIMEOUT = 1800000;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		wifiManager.setWifiEnabled(true);
		btAdapter.enable();
		powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		Settings.System.putInt(getContentResolver(), SCREEN_OFF_TIMEOUT, SCREENTIMEOUT); 
		
		if (record.exists()) {
			Log.e(TAG, " should create the file with \" adb shell touch /data/amt/amt_record\"");
		} 
		
		try {
			server = new ServerSocket(9999);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public void recordResult(String tag, String content, String result) {
		 String strfile = preprocess(tag);
		 if(null == strfile) {
			 strfile = "";
		 }

		try {
			Log.d(TAG, "---begin to record----");
			FileOutputStream fos = new FileOutputStream(record);
			fos.write((strfile + tag + "," + content + "," + result + LINESEP).getBytes());
			fos.close();
			Log.d(TAG, "---finish record");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "---FileNotFoundException---");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG,"---IOException---");
			e.printStackTrace();
		}
	}
    
	private String preprocess(String tag) {
		// TODO Auto-generated method stub
		try {
			FileInputStream fis = new FileInputStream(RERORD_FILE_PATH);
			int len = fis.available();
			byte[] bytes = new byte[len];
			fis.read(bytes);
			fis.close();
			String res = new String(bytes);
			
			int start;
			if(-1 != (start = res.indexOf(tag))) {
				int end = res.indexOf('\n', start);
				String sub = res.substring(start, end+1);
				return res.replace(sub, "");
			} else {
				return res;
			}

		} catch (FileNotFoundException 
				e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}



	public Socket getChannel() {
		if ( null == channel) {
			try {
				Log.d(TAG, "---Socket begin----");
				channel = server.accept();
				Log.d(TAG, "---Socket done !---");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return channel;
	}
}
