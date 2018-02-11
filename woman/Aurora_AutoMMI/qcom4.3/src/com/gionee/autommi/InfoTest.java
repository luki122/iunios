package com.gionee.autommi;

import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import android.os.SystemProperties;
import java.io.IOException;
import com.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qcrilhook.QcRilHookCallback;

public class InfoTest extends BaseActivity {
	
    private final String TAG = "InfoTest";
	private TelephonyManager teleMgr;
	 QcNvItems nvItems ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		teleMgr  = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE); 
		nvItems = new QcNvItems(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		String softVer = SystemProperties.get("ro.gn.gnznvernumber");
        if("eng".equals(SystemProperties.get("ro.build.type"))) {
            softVer += "_eng";
        }   
        String devID = teleMgr.getDeviceId();
        String sn = null;
	    try {
            sn = nvItems.getEgmrResult();
       } catch (IOException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
        String info = softVer + "|" + devID + "|" + sn;
		((AutoMMI)getApplication()).recordResult(TAG, info, "2");
		Toast.makeText(this, info, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		this.finish();
	}
	
}
