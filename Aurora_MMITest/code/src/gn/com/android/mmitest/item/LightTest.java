
package gn.com.android.mmitest.item;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class LightTest extends Activity implements View.OnClickListener {
    private static final String LEDS_FLASHLIGHT = "/sys/devices/platform/tricolor leds and flashlight/leds/flashlight/brightness";
    private static final String BUTTON_BACKLIGHT = "/sys/devices/platform/gn_pmic_leds/leds/button-backlight/brightness";

	private Button mRightBtn, mWrongBtn;

    private static final String TAG = "LightTest";

    private TextView mNoteTv;


    final int ID_LED = 31415;

    boolean mIsFlashOpen, mRLedPress, mGLedPress, mBLedPress, mFlashPress;

    int mPressCount;

	private Runtime run;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
        //lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);
        
        setContentView(R.layout.common_textview_norestart);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);

        TextView contentView = (TextView) findViewById(R.id.test_content);
        contentView.setText(R.string.led_light_note);
    }

    @Override
    public void onStart() {
        turnLightOn();
        super.onStart();
        
    }
    
    @Override
    public void onStop() {
        turnLightOff();;
        super.onStop();
       
    }
    
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
                break;
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
    
    
    private void turnLightOn()   {
    	
		try {
			FileOutputStream fos = new FileOutputStream(LEDS_FLASHLIGHT);
			byte[] cmd = {'1'};
			fos.write(cmd);
			fos = new FileOutputStream(BUTTON_BACKLIGHT);
			fos.write(cmd);
			mRightBtn.setEnabled(true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void turnLightOff() {
		try {
			FileOutputStream fos = new FileOutputStream(LEDS_FLASHLIGHT);
			byte[] cmd = {'0'};
			fos.write(cmd);
			fos = new FileOutputStream(BUTTON_BACKLIGHT);
			fos.write(cmd);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
