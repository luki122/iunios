package com.gionee.autommi.composition;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.Toast;

import com.gionee.autommi.AutoMMI;
import com.gionee.autommi.BaseActivity;
import com.gionee.autommi.AccelerationTest;
import com.gionee.autommi.VibratorTest;

public class AccVibTest extends BaseActivity implements SensorEventListener{
	
	private SensorManager sensorManager;
	private Sensor sensor;
	private boolean flag;
	
	private static final String DURA = "dura";
	Vibrator vibrator;
	int duration;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);	
		((AutoMMI)getApplication()).recordResult(AccelerationTest.TAG, "", "0");
		
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
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		vibrator.cancel();
		finish();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if(!flag) {
			flag = true;
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			String res = "" + x + "|" + y + "|" + z;
			((AutoMMI) getApplication()).recordResult(AccelerationTest.TAG, res, "1");
			Toast.makeText(this, res, Toast.LENGTH_LONG).show();
		}	
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

}
