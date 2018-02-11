
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;

import gn.com.android.mmitest.TestUtils;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.app.Activity;
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

public class PressureTest extends Activity implements OnClickListener {
    private TextView tv;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "PressureTest";

    private SensorManager mSensorMgr;
    private Sensor mPessSensor;
    private TextView pressureNum;
    private RelativeLayout mParent;
    private boolean mIsPressure;
    
    
    private SensorEventListener mPressureListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
        	
        	pressureNum.setText(event.values[0] + "");
        	//Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00919781 begin
        	int tmp = (int)event.values[0];
        	if(tmp >= 980 && tmp <= 1040){
        		mRightBtn.setEnabled(true);
        	}
           //Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00919781 end
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
        
        setContentView(R.layout.pressure_test);
        
        
        pressureNum = (TextView) findViewById(R.id.pressure_num);
        mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
       // mRightBtn.setEnabled(true);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
       
        
        mPessSensor = mSensorMgr.getSensorList(Sensor.TYPE_PRESSURE).get(0);
        mIsPressure = mSensorMgr.registerListener(mPressureListener, mPessSensor, SensorManager.SENSOR_DELAY_FASTEST);
        if (false == mIsPressure) {
            try {
                Thread.sleep(300);
                mIsPressure = mSensorMgr.registerListener(mPressureListener, mPessSensor,
                        SensorManager.SENSOR_DELAY_FASTEST);
            } catch (InterruptedException e) {

            }
            if (false == mIsPressure) {
            	pressureNum.setText("初始化距离传感器失败");
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (true == mIsPressure) {
            mSensorMgr.unregisterListener(mPressureListener);
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
