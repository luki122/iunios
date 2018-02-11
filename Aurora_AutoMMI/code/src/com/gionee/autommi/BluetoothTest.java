package com.gionee.autommi;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Filter;

public class BluetoothTest extends BaseActivity {
	private static final String EXTRA_MAC = "mac";
	public static final String TAG = "BluetoothTest";
	private BluetoothAdapter btAdapter;
	private BroadcastReceiver sentinel, resultProc;
	private String expDev;
	private boolean found;

	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		
		Intent it = this.getIntent();
		expDev = it.getStringExtra(EXTRA_MAC);
		
		sentinel = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub

			}		
		};
		
		resultProc = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
				if ( rssi >= 128) {
					rssi -= 256;
				}
				Log.d(TAG, dev.getAddress());
				Log.d(TAG, " "+ rssi); 
				if(expDev.equalsIgnoreCase(dev.getAddress())) {
					found = true;
					((AutoMMI)getApplication()).recordResult(TAG, "" + rssi,"1");
				}
			}		
		};
		
		((AutoMMI)getApplication()).recordResult(TAG, "", "0");
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		this.registerReceiver(sentinel, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		this.registerReceiver(resultProc, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		btAdapter.startDiscovery();
	}
    
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		this.unregisterReceiver(sentinel);
		this.unregisterReceiver(resultProc);
		btAdapter.cancelDiscovery();
		this.finish();
	}


}
