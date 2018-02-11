package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;

public class MagneticFieldTest extends Activity implements SensorEventListener, OnClickListener {
    private TextView mTitleTv;
    private TextView mContentTv;
    private ImageView mImageView;
    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private boolean mIsAccuracyRight;
    private static String TAG = "MagneticFieldTest";
    SensorManager mSensorMgr;
    Sensor mSensor;
    StringBuilder mBuilder;
    Resources mRs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
       // lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);
        // Gionee xiaolin 20120522 modify for CR00601846 start   
        mIsAccuracyRight = false;
        try {
            int accuracy = 2;
            Intent intent = getPackageManager().getLaunchIntentForPackage("jlzn.com.android.compass");
            intent.putExtra("referenceValue", accuracy);
            startActivity(intent);
            MagneticFieldTest.this.setContentView(R.layout.common_textview);
            mRightBtn = (Button) findViewById(R.id.right_btn);
            mRightBtn.setOnClickListener(this);
            mRightBtn.setEnabled(true);
            mWrongBtn = (Button) findViewById(R.id.wrong_btn);
            mWrongBtn.setOnClickListener(this);
            mRestartBtn = (Button) findViewById(R.id.restart_btn);
            mRestartBtn.setOnClickListener(this);
        } catch (Exception ex) {
            setContentView(R.layout.acceleration_test);
            mTitleTv = (TextView) findViewById(R.id.test_title);
            mTitleTv.setText(R.string.magnetic_field_test);
            mContentTv = (TextView) findViewById(R.id.test_content);
            mImageView= (ImageView) findViewById(R.id.arraw);
            mImageView.setImageResource(R.drawable.calibration_8);
            mContentTv.setText(R.string.magnetic_field_test);
            mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
            try{
            	mSensor = mSensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
            }catch(Exception e){
            	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            	builder.setTitle(R.string.magnetic_field);
            	builder.setMessage(R.string.get_magnetic_field_fail);
            	builder.create().show();
            }
 
            mBuilder = new StringBuilder();
            mRightBtn = (Button) findViewById(R.id.right_btn);
            mWrongBtn = (Button) findViewById(R.id.wrong_btn);
            mRightBtn.setOnClickListener(this);
            mWrongBtn.setOnClickListener(this);
            mRestartBtn = (Button) findViewById(R.id.restart_btn);
            mRestartBtn.setOnClickListener(this);
            mRs = this.getResources();
        }
    }
    
    protected void onResume() {
        super.onResume();
        if (null != mSensorMgr)
            mSensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        // TODO Auto-generated method stub
        if (false == mIsAccuracyRight) {
            if (3 == event.accuracy || 2 == event.accuracy) {
                mTitleTv.setText(mRs.getString(R.string.magnetic_field) + " --- " + mRs.getString(R.string.test_right));
                mImageView.setVisibility(View.INVISIBLE);
                mRightBtn.setEnabled(true);
                mIsAccuracyRight = true;
            }
        }

        if (mBuilder.length() > 1) {
            mBuilder.delete(0, mBuilder.length() - 1);
        }
        mBuilder.append("x = " +event.values[SensorManager.DATA_X] + "\n");
        mBuilder.append("y = " +event.values[SensorManager.DATA_Y] + "\n");
        mBuilder.append("z = " +event.values[SensorManager.DATA_Z] + "\n");
        mBuilder.append("event.accuracy = " +  event.accuracy + "\n");
        mContentTv.setText(mBuilder.toString());      
    }
    // Gionee xiaolin 20120522 modify for CR00601846 end
    
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
    public boolean dispatchKeyEvent (KeyEvent event) {
        return true;
    }  
}
