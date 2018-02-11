package com.gionee.autommi;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import android.os.Bundle;
import android.os.SystemProperties;

public class SoftwareVersionTest extends BaseActivity {
	private static final String EXTRA_VER = "ver";
    public static final String TAG = "SoftwareVersionTest";
    private String targetVer;
    private String currVer;
    private  String sn ;
    private char[] content = {'2', '2', '2', '2', '2', '2', '2'};
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		targetVer = this.getIntent().getStringExtra(EXTRA_VER);
		sn = tm.getSN();
		currVer = SystemProperties.get("ro.gn.gnznvernumber");
		if ("eng".equals(SystemProperties.get("ro.build.type"))) {
			currVer += "_eng";
		}
		process();
	}
	private void process() {
		// TODO Auto-generated method stub
		if(targetVer.equalsIgnoreCase(currVer))
			content[0] = '1';
		else
			content[0] = '0';
		
		if(null != sn) {
			char[] barcodes = sn.toCharArray();
			// GSM BT
			if(barcodes.length > 62 && '1' == barcodes[60] && '0' == barcodes[61])
				content[1] = '1';
			if(barcodes.length > 62 && '0' == barcodes[60] && '1' == barcodes[61])
				content[1] = '0';
			
			//GSM FT
			if( barcodes.length >= 63 && 'P' == barcodes[62])
				content[2] = '1';
			if ( barcodes.length >= 63 && 'F' == barcodes[62])
				content[2] = '0';
			
			//WCDMA BT
			if (barcodes.length >= 59 && 'P' == barcodes[58])
				content[3] = '1';
			if (barcodes.length >= 59 && 'F' == barcodes[58])
				content[3] = '0';
			
			//WCDMA FT
			if(barcodes.length >=60 && 'P' == barcodes[59])
				content[4] = '1';
			if(barcodes.length >=60 && 'F' == barcodes[59])
				content[4] = '0';
			
			//TD BT
			if (barcodes.length >= 47 && 'P' == barcodes[46])
				content[5] = '1';
			if (barcodes.length >= 47 && 'F' == barcodes[46])
				content[5] = '0';
			//TD FT
			if (barcodes.length >= 48 && 'P' == barcodes[47])
				content[6] = '1';
			if (barcodes.length >= 48 && 'F' == barcodes[47])
				content[6] = '0';
			
			String cnt = new String(content);
			Toast.makeText(this, cnt, Toast.LENGTH_LONG).show();
			Integer result;  
			if(SystemProperties.getBoolean("gn.mmi.tdscdma", false))
				result = cnt.equalsIgnoreCase("1111111")? 1: 0;
			else
				result =  cnt.equalsIgnoreCase("1111122")? 1: 0;
			
			((AutoMMI)getApplication()).recordResult(TAG, cnt + "|" + sn.subSequence(0, 18), result.toString());
			
		}
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		finish();
	}

}
