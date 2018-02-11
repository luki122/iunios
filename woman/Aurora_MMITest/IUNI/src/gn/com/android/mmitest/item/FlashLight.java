package gn.com.android.mmitest.item;



import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.hardware.Camera;

public class FlashLight extends Activity implements OnClickListener {
    Button mToneBt;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "FlashLight";
    private Camera mCamera;
    Camera.Parameters mParameters = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
       // lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);
	setContentView(R.layout.common_textview);
	mRightBtn = (Button) findViewById(R.id.right_btn);
	mRightBtn.setOnClickListener(this);
        mRightBtn.setEnabled(true);
	mWrongBtn = (Button) findViewById(R.id.wrong_btn);
	mWrongBtn.setOnClickListener(this);
	mRestartBtn = (Button) findViewById(R.id.restart_btn);
	mRestartBtn.setOnClickListener(this);
	TextView titleTv = (TextView) findViewById(R.id.test_title);
	titleTv.setText(R.string.test_title);
    }

    @Override
    public void onResume() {
        super.onResume();
	openFlash();
    }
    public void openFlash() {
        if (null == mCamera) {
            try {
                mCamera = Camera.open();  
                mCamera.startPreview();
                mParameters = mCamera.getParameters();
                String currFlashMode = mParameters.getFlashMode();
                if (currFlashMode == null
                        || (!currFlashMode.equals(Camera.Parameters.FLASH_MODE_TORCH))) {
                	mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(mParameters);
                } 
             }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        		if(mCamera!= null){
	    Log.i(TAG,"onPausemCamera!= null");
	    mCamera.stopPreview();
        mCamera.release();
        mCamera = null;}
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);//false，按键则会变成灰色的，按上去也没反应
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
                Log.d(TAG,"zhangxiaowei -restart");
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
