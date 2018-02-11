
package gn.com.android.mmitest.item;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class ScreenBrightnessTest extends Activity implements View.OnClickListener{
    View mColorView;
    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private static final String TAG = "ScreenBrightnessTest";
    private static final String BUTTON_BACKLIGHT = "/sys/class/leds/kpdbl-pwm-1/brightness";
    
//    private Handler mBrightnessHandler;
//    private Runnable mBrightnessRunnable;
    private static final int TIME_CHANGE = 1000;

    int mCount;
    WindowManager.LayoutParams mlp;
    
    // Gionee xiaolin 20120629 add for CR00632129 start 
    private boolean stopTurnOn;
    Handler btnLightCtrl ;
    // Gionee xiaolin 20120629 add for CR00632129 end
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON; 
        //lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);
        

        mColorView = new View(this);
        mlp = getWindow().getAttributes();
        mlp.screenBrightness = 1.0f;
        getWindow().setAttributes(mlp);
        mColorView.setBackgroundColor(Color.WHITE);
        setContentView(mColorView);
        
        // Gionee xiaolin 20120629 add for CR00632129 start
        btnLightCtrl = new Handler() {
            private final int on = 1;
            private final int off = 0;
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case on:
                        toggleButtonLight(on);
                        if (!stopTurnOn) {
                            this.sendEmptyMessageDelayed(on, 500);
                        }
                        break;
                    case off:
                        toggleButtonLight(off);
                        break;
                }
            }
        };
        // Gionee xiaolin 20120629 add for CR00632129 end
    }
    
    @Override
    public void onStart() {
        super.onStart();
        mCount = 0;
		
        // Gionee xiaolin 20120629 modify for CR00632129 start
        stopTurnOn = false;
        btnLightCtrl.sendEmptyMessage(1);
		// Gionee xiaolin 20120629 modify for CR00632129
    }
    
    @Override
    public void onStop() {
        // Gionee xiaolin 20120629 modify for CR00632129 start
        stopTurnOn = true;
        btnLightCtrl.removeMessages(1);
        btnLightCtrl.sendEmptyMessage(0);
        // Gionee xiaolin 20120629 modify for CR00632129 end
        super.onStop();
        
    }
    
    // Gionee xiaolin 20120629 add for CR00632129 start
    private void toggleButtonLight(int mode) {
        if ( 1 != mode  && 0 != mode)
            return;
         byte[] cmd ;
        if (mode == 1)
         cmd = "120".getBytes();
        else
         cmd = "0".getBytes();
            
        //byte[] cmd = {md};
        Log.d(TAG, "begin to write : ");
        try {
            FileOutputStream fos = new FileOutputStream(BUTTON_BACKLIGHT);
            fos.write(cmd);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Gionee xiaolin 20120629 add for CR00632129 end
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            switch (mCount) {
                case 0:
                    mlp.screenBrightness = 0.75f;
                    getWindow().setAttributes(mlp);
                    mCount++;
                    break;
                case 1:
                    mlp.screenBrightness = 0.5f;
                    getWindow().setAttributes(mlp);
                    mCount++;
                    break;
                case 2:
                    mlp.screenBrightness = 0.25f;
                    getWindow().setAttributes(mlp);
                    mCount++;
                    break;
                case 3:
                    mlp.screenBrightness = 0.01f;
                    getWindow().setAttributes(mlp);
                    mCount++;
                    break;
                case 4:
                    mCount++;
                    if (true == TestUtils.mIsAutoMode|| true == TestUtils.mIsAutoMode_2) {
                          ScreenBrightnessTest.this.setContentView(R.layout.common_textview);
                          mlp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
                          getWindow().setAttributes(mlp);
                          mRightBtn = (Button) findViewById(R.id.right_btn);
                          mRightBtn.setOnClickListener(ScreenBrightnessTest.this);
                          mRightBtn.setEnabled(true);
                          mWrongBtn = (Button) findViewById(R.id.wrong_btn);
                          mWrongBtn.setOnClickListener(ScreenBrightnessTest.this);
                          mRestartBtn = (Button) findViewById(R.id.restart_btn);
                          mRestartBtn.setOnClickListener(this);
                    } else {
                        finish();
                    }
                    break;
            }
        }
        return true;
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
