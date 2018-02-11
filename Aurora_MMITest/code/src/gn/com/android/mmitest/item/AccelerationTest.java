
package gn.com.android.mmitest.item;

import java.util.Timer;
import java.util.TimerTask;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class AccelerationTest extends Activity implements SensorEventListener, OnClickListener {
    private TextView mTitleTv;

    private TextView mContentTv;

    private ImageView mArrowView;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static String TAG = "AccelerationTest";

    private SensorManager mSensorMgr;

    private Sensor mSensor;

    private float[] mValues;

    private Timer mTimer;

    private static final int DELAY_TIME = 500;

    private int mSuccessNum;

    private boolean mIsAccRight;
    
    private boolean mIsLeftSuccess, mIsRightSuccess, mIsTopSuccess, mIsBottomSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acceleration_test);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
        //lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);

        mArrowView = (ImageView) findViewById(R.id.arraw);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mTitleTv.setText(R.string.acceleration_note);
        mContentTv = (TextView) findViewById(R.id.test_content);
        mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        try{
        	mSensor = mSensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        }catch(Exception e){
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setTitle(R.string.acceleration);
        	builder.setMessage(R.string.get_acceleration_fail);
        	builder.create().show();
        }
        
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsAccRight = mSensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (false == mIsAccRight) {
            try {
                Thread.sleep(300);
                mIsAccRight = mSensorMgr.registerListener(this, mSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            } catch (InterruptedException e) {

            }
        }

        mSuccessNum = 0;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (mValues != null) {
                            AccelerationTest.this.showArrow(mValues[SensorManager.DATA_X],
                                    mValues[SensorManager.DATA_Y], mValues[SensorManager.DATA_Z]);
                        }
                    }
                });
            }
        }, 0, DELAY_TIME);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        StringBuffer sb = new StringBuffer();
        sb.append("x = " + event.values[SensorManager.DATA_X] + "\n");
        sb.append("y = " + event.values[SensorManager.DATA_Y] + "\n");
        sb.append("z = " + event.values[SensorManager.DATA_Z] + "\n");
        mContentTv.setText(sb.toString());
        mValues = event.values;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPause() {
        super.onPause();
        if (true == mIsAccRight) {
            mSensorMgr.unregisterListener(this);
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

    private void showArrow(float x, float y, float z) {
        if (x > 8) {
            mArrowView.setImageResource(R.drawable.arrow_right);
            if (false == mIsLeftSuccess) {
                mSuccessNum++;
                mIsLeftSuccess = true;
            }
        } else if (x < -8) {
            mArrowView.setImageResource(R.drawable.arrow_left);
            if (false == mIsRightSuccess) {
                mSuccessNum++;
                mIsRightSuccess = true;
            }
        } else if (y > 8) {
            mArrowView.setImageResource(R.drawable.arrow_up);
            if (false == mIsBottomSuccess) {
                mSuccessNum++;
                mIsBottomSuccess = true;
            }
        } else if (y < -8) {
            mArrowView.setImageResource(R.drawable.arrow_down);
            if (false == mIsTopSuccess) {
                mSuccessNum++;
                mIsTopSuccess = true;
            }
        } else if (z < -8 || z > 8) {
            mArrowView.setImageResource(R.drawable.arrow);
        }

        if (4 == mSuccessNum) {
            mRightBtn.setEnabled(true);
            mSuccessNum++;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
