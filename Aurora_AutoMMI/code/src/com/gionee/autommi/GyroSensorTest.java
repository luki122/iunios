package com.gionee.autommi;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class GyroSensorTest extends BaseActivity implements SensorEventListener{
   
	
	private TextView tip;
	private SensorManager sensorManager;
	private Sensor sensor;
	public static String TAG = "GyroTest";
    private String res;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tip);
		Log.v("adb_GyroSensorTest","----onCreate----");
		tip = (TextView) this.findViewById(R.id.tip);
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		if (null == sensor) {
			Log.v("adb_GyroSensorTest","----onCreate---sensor is null---");
			this.finish();
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.v("adb_GyroSensorTest","----onStart---11111-");
		((AutoMMI)getApplication()).recordResult(TAG, "", "0");
		boolean registResult = sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
		if(registResult){
			Log.v("adb_GyroSensorTest","----onStart---22222-");
		}else{
			Log.v("adb_GyroSensorTest","----onStart---33333333-");
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.v("adb_GyroSensorTest", "--------onStop-------");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v("adb_GyroSensorTest", "--------onDestroy-------");
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		Log.v("adb_GyroSensorTest","----onSensorChanged----");
        float x = event.values[0]; 
        float y = event.values[1];
        float z = event.values[2]; 
		String t = "X : " + x + "\n"
				    + "Y : " + y + "\n"
				    + "Z : " + z + "\n";		
		tip.setText(t);
        if (null == res) {
        	 Log.v("adb_GyroSensorTest","----onSensorChanged---111111111-");
             res = "" + x + "|" + y + "|" + z;
		    ((AutoMMI)getApplication()).recordResult(TAG, res, "1");
		    
		    this.finish();
        }
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

}
