package com.gionee.autommi;

import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class AccelerationTest extends BaseActivity implements
		SensorEventListener {

	private SensorManager sensorManager;
	private Sensor sensor;
	private boolean flag;
	public static String TAG = "AccelerationTest";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.v("adb_AccelerationTest", "----------onCreate------------");
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		
		try{
			 sensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
	       }catch(Exception e){
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setTitle(R.string.acceleration);
	        	builder.setMessage(R.string.get_acceleration_fail);
	        	builder.create().show();
	       }
		
		((AutoMMI)getApplication()).recordResult(TAG, "", "0");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		boolean result = sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
		if(result){
			Log.v("adb_AccelerationTest", "----------onResume----------true-1111111-");
		}else{
			Log.v("adb_AccelerationTest", "----------onResume----------false-222222-");
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.v("adb_AccelerationTest", "--------onPause-------");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.v("adb_AccelerationTest", "--------onStop-------");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v("adb_AccelerationTest", "--------onDestroy-------");
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		Log.v("adb_AccelerationTest", "----------onSensorChanged----------flag-----"+flag);
		if(!flag) {
			flag = true;
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			String res = "" + x + "|" + y + "|" + z;
			Log.v("adb_AccelerationTest", "----------onSensorChanged----------2222-----");
			((AutoMMI) getApplication()).recordResult(TAG, res, "1");
			Toast.makeText(this, res, Toast.LENGTH_LONG).show();
			this.finish();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

}
