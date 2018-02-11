package com.gionee.autommi;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class LSensorTest extends BaseActivity implements SensorEventListener {
	public static final String TAG = "LSensorTest";
    private Sensor lSensor;
    private SensorManager sensorManager;
    private boolean pass;
    private boolean lightFlag;
    private boolean darkFlag;
    private TextView tip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tip);
		tip = (TextView) this.findViewById(R.id.tip); 
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		lSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		((AutoMMI)getApplication()).recordResult(TAG, "", "0");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		sensorManager.registerListener(this, lSensor, SensorManager.SENSOR_DELAY_UI);
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
		tip.setText("光感值 ： " + event.values[0]);
		int value = (int) event.values[0];
		if (5 < value) {
			darkFlag = true;
	    } else  {
	    	lightFlag = true;
	    } 
		if(darkFlag && lightFlag && !pass) {
			pass = true;
			((AutoMMI)getApplication()).recordResult(TAG, "", "1");
		}
	}
}
