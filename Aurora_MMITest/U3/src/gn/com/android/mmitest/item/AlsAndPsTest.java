
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
import android.os.SystemProperties;
import gn.com.android.mmitest.Model;


public class AlsAndPsTest extends Activity implements OnClickListener {
    private TextView tv;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "AlsAndPsTest";

    private SensorManager mSensorMgr;

    private Sensor mLSensor;
    private Sensor mPSensor;
    
    //Gionee liss 20111215 add mLightProTitle for CR00478802 start    
    private TextView mLightNum, mProximityNum, mLightProTitle,colourNum;
    //Gionee liss 20111215 add mLightProTitle for CR00478802 end

    private RelativeLayout mParent;

    private boolean mIsClose, mIsFar;

    private boolean mIsDark;
    
    private boolean mIsLightRight, mIsProximityRight;
    
    private static final int CAL_FAIL = 0;
    private static final int CAL_SUCCESS = 1;
    
    public boolean isCalSucess;
    
  //Gionee zhangxiaowei 20131109 add for CR00946202 start
    private boolean mIsScreenBright= false;
    private boolean mIsScreenBrightStatus = false;
  //Gionee zhangxiaowei 20131109 add for CR00946202 end
    private SensorEventListener mLightListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            mLightNum.setText(event.values[0] + "");
           // colourNum.setText(event.values[1] + "");;
            // if (false == mIsInit && event.values[0] < 5) {
            // mIsInit = true;
            // return;
            // }
            /** xiaolin
            //Gionee liss 20111215 add for CR00478802 start
            if (com.mediatek.featureoption.FeatureOption.GN_OVERSEA_PRODUCT && event.values[0] < 50) {
                mIsDark = true;
             //Gionee liss 20111215 add for CR00478802 end

            } else */  if(event.values[0] < 5) {
                mIsDark = true;
            } else {
                mIsDark = false;
            }
        
            //Gionee liss 20111215 add for CR00478802 start
            //if (true == mIsFar && true == mIsClose && event.values[0] < 5) {
            //Gionee xiaolin 20120227 modify for CR00534606 start 
            if (true == mIsClose && true == mIsDark && isCalSucess) {
            //Gionee xiaolin 20120227 modify for CR00534606 end
            //Gionee liss 20111215 add for CR00478802 end
                mParent.setBackgroundColor(Color.GREEN);
                mRightBtn.setEnabled(true);
            } else {
                mParent.setBackgroundColor(Color.BLACK);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

    };

    SensorEventListener mProximityListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            int i = (int) event.values[0];
            Log.d(TAG, "i = " + i);
            mIsClose = (i == 0 ? true : false);
            //Gionee xiaolin 20120227 modify for CR00534606 start
            if (true == mIsDark && true == mIsClose  && isCalSucess) {
            //Gionee xiaolin 20120227 modify for CR00534606 end
                mParent.setBackgroundColor(Color.GREEN);
                mRightBtn.setEnabled(true);
            } else {
                mParent.setBackgroundColor(Color.BLACK);
            }
            
            if (0 != i)
            	i = 1;
            mProximityNum.setText(i + "");
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
        //lp.dispatchAllKey = 1;

        getWindow().setAttributes(lp);
        
        setContentView(R.layout.light_proximity);
        
        // Gionee liss 20111215 add for CR00478802 start
        mLightProTitle = (TextView) findViewById(R.id.light_proximity_title);
        
        /** xiaolin
        if (com.mediatek.featureoption.FeatureOption.GN_OVERSEA_PRODUCT) {
            mLightProTitle.setText(getResources().getString(
                    R.string.light_proximity_title_105));
        }
        */
        
        // Gionee liss 20111215 add for CR00478802 end
        
        mLightNum = (TextView) findViewById(R.id.light_num);
        mProximityNum = (TextView) findViewById(R.id.proximity_num);
      //  colourNum  =(TextView) findViewById(R.id.colournum);
        mParent = (RelativeLayout) findViewById(R.id.light_proximity_rl);
        mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
      //Gionee zhangxiaowei 20131109 add for CR00946202 start
        updateSettings();
       
        if(mIsScreenBright == true){
             
        Settings.System.putInt(this.getContentResolver(),
        Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
            
        mIsScreenBrightStatus =true;
        
        }
        
        isCalSucess = true;

        Model mModel = Model.getInstance();
        if(!mModel.isU3() &&  !mModel.isI1()){
        	return;
        }

      //Gionee zhangxiaowei 20131109 add for CR00946202 end
        String a = readPsValue();
        Log.e(TAG,"ps_result = " + readPsValue());

        if( a.equals("0")){
       int result =  SensorTest.runNativeSensorTest(40, 0, 5, true, true);
       Log.d(TAG,"result = "+ result );
       
       if(result == 0 ){
    	   
    	   showDialog(CAL_SUCCESS);
       }else {
    	   isCalSucess = false;
    	   showDialog(CAL_FAIL);
	}
        }
        
    }
    
  //Gionee zhangxiaowei 20131109 add for CR00946202 start
    public void updateSettings() {
    	mIsScreenBright = isRespirationLampNotificationOn();
	}
	public boolean isRespirationLampNotificationOn() {
		boolean result = false;
	result = Settings.System.getInt(this.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, 0) != 0;
		return result;
    }
	//Gionee zhangxiaowei 20131109 add for CR00946202 end
    
    
	private String readPsValue() {
        BufferedReader bufferReader = null;
        String mFileName = "/persist/ps_calib";
        String line , line1,line2 = null;
        try {
                bufferReader = new BufferedReader(new FileReader(mFileName));
                line = bufferReader.readLine();
                line1 = bufferReader.readLine();
                line2 = bufferReader.readLine();
                Log.e(TAG,"readPsValue = " + line2);
        } catch (Exception e) {
                e.printStackTrace();
                return null;
        }   
 
        return line2;
}

    
//add by zhangxiaowei start
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch(id) {
            case CAL_FAIL:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("fail to calibrate!")
                .setCancelable(false)
                .setPositiveButton("ok", null);
                dialog = builder.create();
                break;
       /*     case CAL_SUCCESS:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("success to calibrate!")
                .setCancelable(false)
                .setPositiveButton("ok", null);
                dialog = builder1.create();*/
        }
        return dialog;
    }
    //add by zhangxiaowei end
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
        
        try{
              mPSensor = mSensorMgr.getSensorList(Sensor.TYPE_PROXIMITY).get(0);
            }catch(Exception e){
            	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            	builder.setTitle(R.string.proximity);
            	builder.setMessage(R.string.get_proximity_fail);
            	builder.create().show();
         }

        mIsProximityRight = mSensorMgr.registerListener(mProximityListener, mPSensor, SensorManager.SENSOR_DELAY_FASTEST);
        if (false == mIsProximityRight) {
            try {
                Thread.sleep(300);
                mIsProximityRight = mSensorMgr.registerListener(mProximityListener, mPSensor,
                        SensorManager.SENSOR_DELAY_FASTEST);
            } catch (InterruptedException e) {

            }
            if (false == mIsProximityRight) {
                mProximityNum.setText(R.string.init_proximity_sensor_fail);
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (true == mIsLightRight) {
            mSensorMgr.unregisterListener(mLightListener);
        }
        if (true == mIsProximityRight) {
            mSensorMgr.unregisterListener(mProximityListener);
        }
 
      //Gionee zhangxiaowei 20131109 add for CR00946202 start
        if(mIsScreenBrightStatus){
            
        	Settings.System.putInt(this.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
        }
      //Gionee zhangxiaowei 20131109 add for CR00946202 end
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
