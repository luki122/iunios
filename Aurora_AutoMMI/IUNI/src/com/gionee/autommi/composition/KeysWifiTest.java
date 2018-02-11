package com.gionee.autommi.composition;

import com.gionee.autommi.AutoMMI;
import com.gionee.autommi.BaseActivity;
import com.gionee.autommi.KeysTest;
import com.gionee.autommi.R;
import com.gionee.autommi.WifiTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.widget.TextView;
import android.media.AudioManager;

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
import android.os.SystemProperties;

public class KeysWifiTest extends BaseActivity {
	String[] keyCodes;
	String[] keyValues;
	Map<String,String> keyMap = new HashMap<String,String>();
	Set<String> acceptSet = new HashSet<String>();
	PowerManager pm;
	AudioManager am;
	private boolean pass;
	private boolean getEarPhonePhone;
	private TextView content;
	private String cvalue = new String();
	
	private static final String EXTRA_BSSID = "bssid";
	String targetBssid;
	BroadcastReceiver resRec;
	WifiManager wifiManager;
	private boolean found;
	private String level;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.display);
		content = (TextView) this.findViewById(R.id.keys);
		preprocessKeys();
	    
	    if(keyCodes.length != keyValues.length)
	    	System.exit(-1);
	    
		for(int i = 0; i < keyCodes.length; i++) {
			keyMap.put(keyCodes[i], keyValues[i]);
		}
		pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		((AutoMMI)getApplication()).recordResult(KeysTest.TAG, "", "0");
		
		Intent it = this.getIntent();
		targetBssid = it.getStringExtra(EXTRA_BSSID);
		if( null != targetBssid) {
			Toast.makeText(this, "bssid : " + targetBssid, Toast.LENGTH_LONG).show();
		} 
        
		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		resRec = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				if (found == true)
					return;
				if(intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
					if(analyzeScanResults()) {
						found = true;
						((AutoMMI)getApplication()).recordResult(WifiTest.TAG, level, "1");
					} else {
						((AutoMMI)getApplication()).recordResult(WifiTest.TAG, "", "0");
					};
				}
			}
			
		};
        
		((AutoMMI)getApplication()).recordResult(WifiTest.TAG, "", "0");
	}
	
	private void preprocessKeys() {
		keyCodes = this.getResources().getStringArray(R.array.key_test_keys);
	    keyValues = this.getResources().getStringArray(R.array.key_test_values);
	    if(!SystemProperties.getBoolean("gn.autommi.keytest.camerea", false)){
	    	removeTestItem("27");
	    }
	}
	
	private void removeTestItem(String key){
    	List<String> t = new ArrayList<String>(Arrays.<String>asList(keyCodes));
    	int i = t.indexOf(key);
    	if (i == -1) {
    		return;
    	}
    	t.remove(i);
    	keyCodes = t.toArray(new String[1]);
    	t = new ArrayList<String>(Arrays.<String>asList(keyValues));
    	t.remove(i);
    	keyValues = t.toArray(new String[1]);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		this.registerReceiver(resRec, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifiManager.startScan();
		am.setMode(AudioManager.MODE_IN_CALL);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		this.unregisterReceiver(resRec);
		am.setMode(AudioManager.MODE_NORMAL);
		this.finish();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		String code = String.valueOf(event.getKeyCode());
		if (!keyMap.keySet().contains(code)) {
			return true;
		}
		if (!acceptSet.contains(code)) {
			acceptSet.add(code);
            cvalue += keyMap.get(code) + " : OK" + "\n";
			content.setText(cvalue);
			if (acceptSet.equals(keyMap.keySet())) {
				pass = true;
				((AutoMMI) getApplication()).recordResult(KeysTest.TAG, "", "1");
			}
		}
		return true;
	}
	
	private boolean analyzeScanResults() {
		// TODO Auto-generated method stub
		List<ScanResult>  rs = wifiManager.getScanResults();
		for (ScanResult i : rs) {
			Log.d(WifiTest.TAG,i.BSSID);
			Log.d(WifiTest.TAG, ""+i.level);
			if(targetBssid.equalsIgnoreCase(i.BSSID)) {
				level = String.valueOf(i.level);
				return true;
			}
		}
		return false;
	}
}
