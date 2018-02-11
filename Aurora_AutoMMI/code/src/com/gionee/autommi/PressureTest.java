package com.gionee.autommi;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class PressureTest extends BaseActivity implements SensorEventListener {
	private TextView tip;
	private SensorManager sensorManager;
	private Sensor sensor;
	public static String TAG = "PressureTest";
	private String res;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tip);
		tip = (TextView) this.findViewById(R.id.tip);
		sensorManager = (SensorManager) this
				.getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		if (null == sensor) {
			Toast.makeText(this, "failed to get Pressure sensor", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		((AutoMMI) getApplication()).recordResult(TAG, "", "0");
		sensorManager.registerListener(this, sensor,
				SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		sensorManager.unregisterListener(this);
		this.finish();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		float v = event.values[0];
		tip.setText("" + v);
		if (null == res) {
			res = "" + v;
			if (v >= 980 && v <= 1040) {
				((AutoMMI) getApplication()).recordResult(TAG, res, "1");
			} else {
				((AutoMMI) getApplication()).recordResult(TAG, res, "0");
			}
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

}
