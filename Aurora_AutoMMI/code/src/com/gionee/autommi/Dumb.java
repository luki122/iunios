package com.gionee.autommi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.os.SystemProperties;
import android.view.WindowManager;
import static  com.gionee.autommi.BaseActivity.PERSIST_RADIO_DISPATCH_ALL_KEY;

public class Dumb extends Activity {

	private static final String TAG = "Dumb";

    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON; 
        getWindow().setAttributes(lp);
    }

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		SystemProperties.set(PERSIST_RADIO_DISPATCH_ALL_KEY, "false");	
	}
}
