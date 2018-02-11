package com.gionee.autommi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class CallTest extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.v("adb_calltest","----onCreate---11111111-");
		Intent callIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
		callIntent.setData(Uri.parse("tel:112"));
		this.startActivity(callIntent);
		Log.v("adb_calltest","----onCreate---22222222-");
		this.finish();
	}
}
