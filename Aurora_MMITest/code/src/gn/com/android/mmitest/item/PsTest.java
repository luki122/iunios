package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PsTest extends Activity implements OnClickListener {
	private TextView tv;

	private Button mRightBtn, mWrongBtn, mRestartBtn;

	private static final String TAG = "PsTest";

	private SensorManager mSensorMgr;

	private Sensor mLSensor;

	private TextView mLightNum;

	private RelativeLayout mParent;

	private boolean mIsLightRight;

	private static final int CAL_FAIL = 0;
	private static final int CAL_SUCCESS = 1;

	private boolean mIsScreenBright = false;
	private boolean mIsScreenBrightStatus = false;
	
	private boolean mIsDark;
	
	private SensorEventListener mLightListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			mLightNum.setText(event.values[0] + "");
			Log.v("gary", "event.values[0]==="+event.values[0]);
		    if(event.values[0] < 5) {
	                mIsDark = true;
	        }else{
	                mIsDark = false;
	        }
			
			if(mIsDark){
				 mRightBtn.setEnabled(true);
				 mParent.setBackgroundColor(Color.GREEN);
			 }else{
				 mParent.setBackgroundColor(Color.BLACK);
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

		getWindow().setAttributes(lp);
		setContentView(R.layout.ps_test);

		String a = readPsValue();
		Log.e(TAG, "ps_result = " + readPsValue());

		mLightNum = (TextView) findViewById(R.id.light_num);
		mParent = (RelativeLayout) findViewById(R.id.light_proximity_rl);
		mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);

		mRightBtn = (Button) findViewById(R.id.right_btn);
		mRightBtn.setOnClickListener(this);
		mWrongBtn = (Button) findViewById(R.id.wrong_btn);
		mWrongBtn.setOnClickListener(this);
		mRestartBtn = (Button) findViewById(R.id.restart_btn);
		mRestartBtn.setOnClickListener(this);
		
		/*
		updateSettings();

		if (mIsScreenBright == true) {

			Settings.System.putInt(this.getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
			mIsScreenBrightStatus = true;

		}
		
		//if (a.equals("0")) {
		    int value = Settings.System.getInt(this.getContentResolver(),
	                Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
	        Log.v("gary","-----Settings.System.SCREEN_BRIGHTNESS_MODE-----===="+value);
		
			int result = SensorTest.runNativeSensorTest(40, 1, 5, true, true);
			Log.d(TAG, "result = " + result);

			if (result == 0) {
				showDialog(CAL_SUCCESS);
			} else {
				showDialog(CAL_FAIL);
			}
		//}*/

	}

	public void updateSettings() {
		mIsScreenBright = isRespirationLampNotificationOn();
	}

	public boolean isRespirationLampNotificationOn() {
		boolean result = false;
		result = Settings.System.getInt(this.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE, 0) != 0;
		return result;
	}


	private String readPsValue() {
		BufferedReader bufferReader = null;
		String mFileName = "/persist/ps_calib";
		String line, line1, line2 = null;
		try {
			bufferReader = new BufferedReader(new FileReader(mFileName));
			line = bufferReader.readLine();
			line1 = bufferReader.readLine();
			line2 = bufferReader.readLine();
			Log.e(TAG, "readPsValue = " + line2);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return line2;
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case CAL_FAIL:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("fail to calibrate!").setCancelable(false)
					.setPositiveButton("ok", null);
			dialog = builder.create();
			break;
		}
		return dialog;
	}

	@Override
	protected void onResume() {
		super.onResume();
		 try{
			  mLSensor = mSensorMgr.getSensorList(Sensor.TYPE_LIGHT).get(0);
	        }catch(Exception e){
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setTitle(R.string.PsTest);
	        	builder.setMessage(R.string.get_ps_fail);
	        	builder.create().show();
	        }

		mIsLightRight = mSensorMgr.registerListener(mLightListener, mLSensor,
				SensorManager.SENSOR_DELAY_FASTEST);
		if (false == mIsLightRight) {
			try {
				Thread.sleep(300);
				mIsLightRight = mSensorMgr.registerListener(mLightListener,
						mLSensor, SensorManager.SENSOR_DELAY_FASTEST);
			} catch (InterruptedException e) {

			}
			if (false == mIsLightRight) {
				mLightNum.setText(R.string.init_light_sensor_fail);
			}
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		if (true == mIsLightRight) {
			mSensorMgr.unregisterListener(mLightListener);
		}

		if (mIsScreenBrightStatus) {

			Settings.System.putInt(this.getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.right_btn: {
			mRightBtn.setEnabled(false);
			mWrongBtn.setEnabled(false);
			mRestartBtn.setEnabled(false);
			TestUtils.rightPress(TAG, this);
			break;
		}

		case R.id.wrong_btn: {
			mRightBtn.setEnabled(false);
			mWrongBtn.setEnabled(false);
			mRestartBtn.setEnabled(false);
			TestUtils.wrongPress(TAG, this);
			break;
		}

		case R.id.restart_btn: {
			mRightBtn.setEnabled(false);
			mWrongBtn.setEnabled(false);
			mRestartBtn.setEnabled(false);
			TestUtils.restart(this, TAG);
			break;
		}
		}

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return true;
	}
}
