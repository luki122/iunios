package com.gionee.autommi;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class WifiTest extends BaseActivity {
	private static final String EXTRA_BSSID = "bssid";
	public static final String TAG = "WifiTest";
	String targetBssid;
	BroadcastReceiver resRec;
	WifiManager wifiManager;
	private boolean found;
	private String level;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Intent it = this.getIntent();
		targetBssid = it.getStringExtra(EXTRA_BSSID);
		if( null != targetBssid) {
			Toast.makeText(this, "bssid : " + targetBssid, Toast.LENGTH_LONG).show();
		} 
        
		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		resRec = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				if (found == true)
					return;
				if(intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
					if(analyzeScanResults()) {
						found = true;
						((AutoMMI)getApplication()).recordResult(TAG, level, "1");
					} else {
						((AutoMMI)getApplication()).recordResult(TAG, "", "0");
					};
				}
			}
			
		};
        
		((AutoMMI)getApplication()).recordResult(TAG, "", "0");
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		this.registerReceiver(resRec, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifiManager.startScan();	
	}
    
	private boolean analyzeScanResults() {
		// TODO Auto-generated method stub
		List<ScanResult>  rs = wifiManager.getScanResults();
		for (ScanResult i : rs) {
			Log.d(TAG,i.BSSID);
			Log.d(TAG, ""+i.level);
			if(targetBssid.equalsIgnoreCase(i.BSSID)) {
				level = String.valueOf(i.level);
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		this.unregisterReceiver(resRec);
		this.finish();
	}
}
