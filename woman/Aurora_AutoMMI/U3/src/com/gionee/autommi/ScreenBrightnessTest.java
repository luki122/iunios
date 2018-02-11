
package com.gionee.autommi;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;



public class ScreenBrightnessTest extends BaseActivity {
    View mColorView;
    private static final String TAG = "ScreenBrightnessTest";
    private static final String PATH_BUTTON_BACK_LIGHT = "/sys/class/leds/button-backlight/brightness";
    


    //WindowManager.LayoutParams mlp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON; 
        //getWindow().setAttributes(lp);

        lp.screenBrightness = 1.0f;
        lp.buttonBrightness = 1.0f;
        getWindow().setAttributes(lp);
        
        mColorView = new View(this);
        mColorView.setBackgroundColor(Color.WHITE);
        setContentView(mColorView);
    }
    @Override
    public void onResume() {
        super.onResume();
        //TestUtils.acquireWakeLock(this);
        setBtnBackLight(5);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        setBtnBackLight(0);
        //TestUtils.releaseWakeLock();
    }

    @Override
    public void onStop() {
        super.onStop();
	ScreenBrightnessTest.this.finish();
	Log.i("aaaa","onStop");
    }

    private void setBtnBackLight(int value) {
        Log.d(TAG, "setBtnBackLight : " + value);
        try {
            OutputStream os = new FileOutputStream(PATH_BUTTON_BACK_LIGHT);
            os.write(("" + value).getBytes());
        } catch  (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }   
    }

}
