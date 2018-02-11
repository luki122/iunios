package com.gionee.autommi;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class PSensorTest extends BaseActivity implements SensorEventListener {
    public static String TAG = "PSensorTest";
    private Sensor pSensor;
    private SensorManager sensorManager;
    private boolean pass;
    private boolean farFlag;
    private boolean nearFlag;
    private TextView tip;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tip);
		tip = (TextView) this.findViewById(R.id.tip);
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		pSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		((AutoMMI)getApplication()).recordResult(TAG, "", "0");
		
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		sensorManager.registerListener(this, pSensor, SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		sensorManager.unregisterListener(this);
		this.finish();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		tip.setText("距离值 ： " + event.values[0]);
		int value = (int) event.values[0];
		if(0 == value) {
			nearFlag = true;
		} else if (1 == value) {
			farFlag = true;
		} else {
			// Something is wrong
		}
		if(nearFlag && farFlag && !pass) {
			pass = true;
			((AutoMMI)getApplication()).recordResult(TAG, "", "1");
		}
	}

}
