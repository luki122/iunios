
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

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
        
        private boolean mIsDark;
    
    private SensorEventListener mLightListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            mLightNum.setText(event.values[0] + "");
            if(event.values[0] < 5) {
                mIsDark = true;
            } else {
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
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
        getWindow().setAttributes(lp);
        
        setContentView(R.layout.ps_test);
        
        mLightNum = (TextView) findViewById(R.id.light_num);
	mParent = (RelativeLayout) findViewById(R.id.light_proximity_rl);
	mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        mLSensor = mSensorMgr.getSensorList(Sensor.TYPE_LIGHT).get(0);
        mIsLightRight = mSensorMgr.registerListener(mLightListener, mLSensor, SensorManager.SENSOR_DELAY_FASTEST);
        if (false == mIsLightRight) {
            try {
                Thread.sleep(300);
                mIsLightRight = mSensorMgr.registerListener(mLightListener, mLSensor,
                        SensorManager.SENSOR_DELAY_FASTEST);
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
