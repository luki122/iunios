package com.gionee.settings.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.content.BroadcastReceiver;
import android.content.Intent;

import com.android.settings.R;

public class BtDevDialog extends AuroraActivity implements
		DialogInterface.OnDismissListener, View.OnClickListener {
	private final int CONTENT = 9;
	private Button scanBtn;
	private BluetoothAdapter btAdapter;
	private StateMachine processor;
	private String TAG = "BtDevDialog" ;
	
	private BroadcastReceiver btReceiver;
	private IntentFilter btFilter;
	
	private List<String> to_finished_events =  new ArrayList<String>();
	private static String[] TO_FINISH_EVENTS = new String[] {
			Intent.ACTION_CLOSE_SYSTEM_DIALOGS,
		    "com.android.deskclock.ALARM_ALERT",
		    "close.bt.dialog",};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		this.showDialog(CONTENT);
		btReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
					if (processor != null)
						processor.setState(State.STOP);
				}
                if(Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
                    if (intent.getBooleanExtra("state", false) == true) {
					    BtDevDialog.this.finish();
                    }
                }
				if (to_finished_events.contains(intent.getAction())) {
					BtDevDialog.this.finish();
				}			
			}	
		};
		
		btFilter = new IntentFilter();
		btFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		btFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		to_finished_events.addAll(Arrays.asList(TO_FINISH_EVENTS));
        for ( String it : to_finished_events) {
        	btFilter.addAction(it);
        }

	}
    

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onResume();
		this.registerReceiver(btReceiver, btFilter);
		
	}



	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		this.unregisterReceiver(btReceiver);
		this.finish();
	}


	@Override
	public void onDismiss(DialogInterface dialog) {
		// TODO Auto-generated method stub
		this.finish();
	}

	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = this.getLayoutInflater();
		AuroraAlertDialog dialog = null;

		switch (id) {
		case CONTENT:
		    AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
			builder.setTitle("蓝牙");
			View v = inflater.inflate(R.layout.bt_dialog, null);
			scanBtn = (Button) v.findViewById(R.id.scan_custom);
			processor = new StateMachine(scanBtn, State.SCAN);
			scanBtn.setOnClickListener(this);
			Button stopBtn = (Button) v.findViewById(R.id.cancel_custom);
			stopBtn.setOnClickListener(this);
			builder.setView(v);
			builder.setOnDismissListener(this);
			builder.setCancelable(false);
			dialog = builder.create();
			break;

		}
		return dialog;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.d(TAG, "---onClick(View v)---");
		if (v.getId() == R.id.scan_custom) {
			Log.d(TAG, ""+v.getId());
			if (null == processor) {
				processor = new StateMachine((Button) v, State.SCAN);
			}
			processor.onClick();
		}
		if (v.getId() ==  R.id.cancel_custom) {
			this.finish();
		}
	}

	private class StateMachine {

		private State state;
		private Button button;

		public StateMachine(Button button, State state) {
			this.button = button;
			this.state = state;
		}

		public void onClick() {
			if (state == State.SCAN) {
				stopBTScan();
				setState(State.STOP);
			} else if (state == State.STOP) {
				startBTScan();
				setState(State.SCAN);
			}
		}

		public void setState(State st) {
			state = st;
			if (st == State.SCAN)
				button.setText(R.string.stop_custom);
			if (st == State.STOP)
				button.setText(R.string.scan_custom);
		}
	}

	private enum State {
		STOP, SCAN
	};

	private void stopBTScan() {
		// TODO Auto-generated method stub
		btAdapter.cancelDiscovery();

	}

	private void startBTScan() {
		// TODO Auto-generated method stub
		btAdapter.startDiscovery();

	}

}
