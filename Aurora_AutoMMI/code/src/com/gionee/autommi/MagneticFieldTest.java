package com.gionee.autommi;
import com.gionee.autommi.AutoMMI;
import com.gionee.autommi.BaseActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MagneticFieldTest extends BaseActivity implements SensorEventListener {

	private SensorManager sensorManager;
    private Sensor mFSensor;
    public static final String TAG = "MagneticFieldTest";
    private boolean flag;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.v("adb_MagneticFieldTest", "--------onCreate-------");
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);

		try{
			mFSensor = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
	       }catch(Exception e){
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setTitle(R.string.magnetic_field);
	        	builder.setMessage(R.string.get_magnetic_field_fail);
	        	builder.create().show();
	       }
		
		((AutoMMI)getApplication()).recordResult(TAG, "", "0");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		boolean result = sensorManager.registerListener(this, mFSensor, SensorManager.SENSOR_DELAY_NORMAL);
		if(result){
			Log.v("adb_MagneticFieldTest", "--------onResume-----true--");
		}else{
			Log.v("adb_MagneticFieldTest", "--------onResume-----false--");
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.v("adb_MagneticFieldTest", "--------onPause-------");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.v("adb_MagneticFieldTest", "--------onStop-------");
		//sensorManager.unregisterListener(this);
		//this.finish();
	}
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v("adb_MagneticFieldTest", "--------onDestroy-------");
		sensorManager.unregisterListener(this);
	}

	
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		Log.v("adb_MagneticFieldTest", "--------onSensorChanged------flag-----"+flag);
		if (!flag) {
			flag = true;
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			String res = "" + x + "|" + y + "|" + z;
			Log.v("adb_MagneticFieldTest", "--------onSensorChanged-----2222222222--");
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
