package com.gionee.autommi;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.BatteryManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BatteryTest extends BaseActivity {
	public static final String TAG = "BatteryTest";
	private BroadcastReceiver battInfoRec;
	private String[] result = new String[4];
	private boolean[] flag = new boolean[2];
    private static final String chargeCurNodePath = "/sys/class/power_supply/battery/BatteryAverageCurrent";
    private static final String charegeVolNodepath = "/sys/class/power_supply/battery/ChargerVoltage";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		battInfoRec = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
					Integer a = intent.getIntExtra(BatteryManager.EXTRA_STATUS,  -1);
					Integer b = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
					result[0] = a.toString();
					result[1] = b.toString();
					flag[0] = true;
					getChargeInfo();
					
					if(flag[0] && flag[1]) {
						String content = "";
						for(int i = 0 ; i <  result.length; i++) {
							content += result[i];
	                        content += (i == result.length - 1) ? "" :"|";
						}
						((AutoMMI)getApplication()).recordResult(TAG, content, "2");
					}
				}	
			}
		};
	}

	protected void getChargeInfo() {
		// TODO Auto-generated method stub
		String c = extractNodeInfo(chargeCurNodePath);
		String v = extractNodeInfo(charegeVolNodepath);
		if(c != null && v != null ) {
			result[2] = c;
			result[3] = v;
			flag[1] = true;
		} else {
			flag[1] = false;
		}
	}

	private String extractNodeInfo(String path) {
		// TODO Auto-generated method stub
	    try {
			InputStream is = new FileInputStream(path);
			int len = is.available();
			byte[] bytes = new byte[len];
			is.read(bytes);
			return new String(bytes).trim();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		this.registerReceiver(battInfoRec, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		this.unregisterReceiver(battInfoRec);
		this.finish();
	}

}
