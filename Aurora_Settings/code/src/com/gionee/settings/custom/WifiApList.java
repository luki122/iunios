package com.gionee.settings.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.NetworkInfo;
import android.bluetooth.BluetoothDevice;
import com.android.settings.R;

public class WifiApList extends AuroraActivity implements DialogInterface.OnDismissListener {
	
	private final int CONTENT = 9;
	
	private List<String> to_finished_events =  new ArrayList<String>();
	private static String[] TO_FINISH_EVENTS = new String[] {
			Intent.ACTION_CLOSE_SYSTEM_DIALOGS,
			BluetoothDevice.ACTION_PAIRING_REQUEST, /*Intent.ACTION_BATTERY_LOW, */
			"com.android.deskclock.ALARM_ALERT",
			WifiManager.WIFI_STATE_CHANGED_ACTION,
			"close.wifi.dialog"};
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
			String action = intent.getAction();
			if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
				NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if(info.isConnected()){
					WifiApList.this.finish();
				}
			}
			
			if (to_finished_events.contains(action)) {
				if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
					if (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -9) == WifiManager.WIFI_STATE_DISABLING)
						WifiApList.this.finish();
				} else { 
					WifiApList.this.finish();
				}
			}
		}
		
	};

	IntentFilter filter;
	
    @SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        to_finished_events.addAll(Arrays.asList(TO_FINISH_EVENTS));
        filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        for ( String it : to_finished_events) {
        	filter.addAction(it);
        }
        this.registerReceiver(receiver, filter);
        this.showDialog(CONTENT);
        
    }

	@Override
	public void onDismiss(DialogInterface dialog) {
		// TODO Auto-generated method stub
		this.finish();
	}

    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = this.getLayoutInflater();
        AuroraAlertDialog dialog = null;

        switch (id) {
        case CONTENT:
            AuroraAlertDialog.Builder builder =  new AuroraAlertDialog.Builder(this);
            builder.setTitle("WLAN 设置");
            builder.setView(inflater.inflate(R.layout.ap_dialog, null));
            builder.setOnDismissListener(this);
            dialog = builder.create();
             break;

        }
        return dialog;
    }
    
    protected void onStop () {
    	super.onStop();
    	this.unregisterReceiver(receiver);
    	this.finish();
    }

}
