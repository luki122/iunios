package com.gionee.autommi;

import java.net.Socket;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;


public class VibratorTest extends BaseActivity {
	private static final String DURA = "dura";
	public static String TAG = "VibratorTest";
	Vibrator vibrator;
	int duration;
	
	Socket cmdSocket;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		Intent it = this.getIntent();
		String dura = it.getStringExtra(DURA);
		if(null != dura) {
			duration = Integer.parseInt(dura) * 1000;
		}
	}


	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		vibrator.vibrate(duration);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		vibrator.cancel();
		finish();
	}

}
