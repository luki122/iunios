package com.gionee.autommi.composition;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.gionee.autommi.AutoMMI;
import com.gionee.autommi.BaseActivity;
import com.gionee.autommi.FMTest;
import com.gionee.autommi.MagneticFieldTest;
import com.caf.fmradio.IFMRadioService;

public class MagFmTest extends BaseActivity implements SensorEventListener{

	private String TAG = "FMTest";
	private IFMRadioService mService;
	private RemoteServiceConnection mConnection;
	private boolean mIsBind;
	private static final String EXTRA_FM = "fm";
	private AudioManager mAM;
	float frequency;
	
    private SensorManager sensorManager;
    private Sensor mFSensor;
    private boolean flag;


	private class RemoteServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className, IBinder service) {
			try {
				mService = IFMRadioService.Stub.asInterface(service);
				if (null == mService) {
					Log.e(TAG, "Error: null interface");
				} else {
					mService.fmOn();
					mService.tune((int) (107.5f * 1000));
					Log.d(TAG, " ---fmOn()---");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				Log.e(TAG, "IFMRadioService RemoteException");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		mFSensor = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
		((AutoMMI)getApplication()).recordResult(MagneticFieldTest.TAG, "", "0");
	}

	@Override
	public void onResume() {

		super.onResume();
		Intent it = this.getIntent();
		String fmDev = it.getStringExtra(EXTRA_FM);
		Log.i(TAG, "fmDev =" + fmDev);
		if (fmDev != null) {
			frequency = Float.parseFloat(fmDev);
		}
		Log.i(TAG, "frequency =" + frequency);
		mConnection = new RemoteServiceConnection();
		if (null != mAM) {
//			int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_FM);
//			mAM.setStreamVolume(AudioManager.STREAM_FM, maxVol, 0);
		}
		if (false == mIsBind) {
			mIsBind = bindService(
					new Intent("com.caf.fmradio.IFMRadioService"), mConnection,
					Context.BIND_AUTO_CREATE);
			if (true == mIsBind) {

			} else {
				Log.e(TAG, "bindService fail");
			}
		}

	    sensorManager.registerListener(this, mFSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "onPause(); ");
		try {
			if (null != mService && true == mIsBind) {
				unbindService(mConnection);
				mIsBind = false;
				mService = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		sensorManager.unregisterListener(this);
		this.finish();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return true;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (!flag) {
			flag = true;
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			String res = "" + x + "|" + y + "|" + z;
			((AutoMMI) getApplication()).recordResult(MagneticFieldTest.TAG, res, "1");
			Toast.makeText(this, res, Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
}
