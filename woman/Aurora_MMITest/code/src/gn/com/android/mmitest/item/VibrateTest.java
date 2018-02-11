package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class VibrateTest extends Activity implements Button.OnClickListener{
    private Vibrator mVibrator;
    private Button mRightBtn, mWrongBtn, mStopBtn, mRestartBtn;
    private static final String TAG = "VibrateTest";
	//Gionee zhangxiaowei 20130905 add for CR00888208 start
    private boolean mIsVibrator = false;
    private boolean mVibratorStatus = false;
	//Gionee zhangxiaowei 20130905 add for CR00888208 start
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
       // lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);

        setContentView(R.layout.common_textview);
//        mStopBtn = (Button) findViewById(R.id.test_button);
//        mStopBtn.setText(R.string.stop_vibrate);
//        mStopBtn.setOnClickListener(this);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        /*
        
        //Gionee zhangxiaowei 20130905 add for CR00888208 start
        updateSettings();
        if(mIsVibrator == false){
        	AmigoSettings.putInt(this.getContentResolver(),
        			AmigoSettings.SWITCH_VIBRATION_ENABLED, 1);
        	mVibratorStatus =true;
        
        }
        //Gionee zhangxiaowei 20130905 add for CR00888208 end
        */
       mVibrator.vibrate(new long[] {1000, 2000}, 0);
       // mVibrator.vibrate(new long[] {1, 5000}, 0);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mRightBtn.setEnabled(true);
    }
    //Gionee zhangxiaowei 20130905 add for CR00888208 start
    public void updateSettings() {
    	mIsVibrator = isRespirationLampNotificationOn();
	}
	public boolean isRespirationLampNotificationOn() {
		boolean result = false;
		/*
	result = AmigoSettings.getInt(this.getContentResolver(),
			AmigoSettings.SWITCH_VIBRATION_ENABLED, 0) != 0;
			*/
		return result;
    }
	//Gionee zhangxiaowei 20130905 add for CR00888208 end
    @Override
    public void onStop() {
        super.onStop();
        mVibrator.cancel();
        /*
		//Gionee zhangxiaowei 20130905 add for CR00888208 start
        if(mVibratorStatus){
        	AmigoSettings.putInt(this.getContentResolver(),
        			AmigoSettings.SWITCH_VIBRATION_ENABLED, 0);
        	
        }
		//Gionee zhangxiaowei 20130905 add for CR00888208 end
		 */
        mVibrator = null;
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
    public boolean dispatchKeyEvent (KeyEvent event) {
        return true;
    }
}
